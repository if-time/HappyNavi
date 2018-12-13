package com.trackersurvey.happynavi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.CommentMediaFiles;
import com.trackersurvey.entity.InterestMarkerData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.DropDownListView;
import com.trackersurvey.helper.DropDownListViewShort;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.photoview.SelectedTreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class CommentActivity extends Activity implements OnClickListener {
	public  final int REQUEST_PICTURE = 1;//�������ѡ��Ƭ
	public  final int REQUEST_TAKEPIC = 2;//�����������
	public  final int REQUEST_PLACE = 3;//ѡ���༭����
	public  final int REQUEST_VIDEO = 4;//�������ѡ��¼��
	public  final int REQUEST_CAMERA = 5;//�������¼��
	public  final String SHAREDFILES = "uploadFiles";
	// gridViewʹ�õĶ��HashMap��һ������key
	private  final String keyOfBitmap = "itemImage";
	private  final int gviewCol = 3; // gridView����

	private GridView gview; // ������ʾ����ͼ
	private ArrayList<HashMap<String, Object>> imageItem; // gridView �����
	private int itemNo; // gridView���������
	private SimpleAdapter simpleAdapter;

	// ѡ��Ķ�����Ƭ��id��Uri
	private SelectedTreeMap selectPictures;
	private  ArrayList<String> pathImage; // ѡ�������ͼƬ·��
	private  ArrayList<String> cacheImages;
	private ArrayList<String> durationData, behaviourData, partnerNumData, relationData;
	private  String cacheVideo;
	private ArrayList<String> selectImages; // ѡ���ͼƬ�ľ���·��
	private int hasVideo = -1;

	private InterestMarkerData comment; // �û��¼�
	private String createTime = null;
	private double longitude = 117.1466090000; // ��Ҫ��ͼ���ܻ�õ�4������
	private double latitude = 36.6731060000;
	private double altitude = 114;
	private String placeName = "ʵ����";
	private String commentText;
	private int feeling; //����
	private int behaviour; //��Ϊ����
	private int stay;	//ͣ��ʱ��
	private int companion;//ͬ������
	private int relation; //��ϵ
	private long traceNo = 0;
	private int fileNum;
	private String videoPath = null; // ѡ�����Ƶ�ľ���·��

	// �û�����ý���ļ�
	private CommentMediaFiles files[];

	private int colWidth; // GridVied �п�

	// �������¸�����ť
	private MyLinearLayout back; // ����
	private Button confirm; // ȷ��
	private ImageView photo; // �����������
	private ImageView smallVideo; // ����С��Ƶ
	private ImageView imagePlace; // λ��ͼ��
	private TextView textOfPlace; // ѡ��λ��
	private EditText editComment;

	private String recentPhoto;
	private String cacheFileName;

	private PhotoDBHelper dbHelper;
	private TraceDBHelper traceHelper;

	private DropDownListView behaviourList;
	private DropDownListViewShort durationList, partnerNumberList, relationList;//��������
	private RadioGroup mood;
	private RadioButton rb_happy, rb_general, rb_unhappy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// �Զ��������
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.comment_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		// ��ȡMainActivity�����ľ�γ�� ���� traceno���ص���Ϣ
		Intent mainIntent = getIntent();
		longitude = mainIntent.getDoubleExtra("longitude", longitude);
		latitude = mainIntent.getDoubleExtra("latitude", latitude);
		altitude = mainIntent.getDoubleExtra("altitude", altitude);
		traceNo = mainIntent.getLongExtra("traceNo", traceNo);
		placeName = mainIntent.getStringExtra("placeName");
		
		behaviourData = new ArrayList<String>();
		durationData = new ArrayList<String>();
		partnerNumData = new ArrayList<String>();
		relationData = new ArrayList<String>();
		behaviourData = mainIntent.getStringArrayListExtra("behaviour");
		durationData = mainIntent.getStringArrayListExtra("duration");
		partnerNumData = mainIntent.getStringArrayListExtra("partnerNum");
		relationData = mainIntent.getStringArrayListExtra("relation");
		//Log.i("behaviourData", behaviourData.toString());
		
		if(mainIntent.hasExtra("createTime")){
			createTime = mainIntent.getStringExtra("createTime");
			Log.i("mark", "createTime from marking :"+createTime);
		}else{
			createTime = null;
			Log.i("comment", "createTime from main ");
		}
		colWidth = (Common.winWidth - gviewCol * 8) / gviewCol - 10; // �п�������Ļ���
		gview = (GridView) findViewById(R.id.gridPicture);

		itemNo = 0;
		pathImage = new ArrayList<String>();
		cacheImages = new ArrayList<String>();
		imageItem = new ArrayList<HashMap<String, Object>>();
		selectImages = new ArrayList<String>();
		addSymbol();
		
		// ��adapter����gridView
		simpleAdapter = new SimpleAdapter(getApplicationContext(), imageItem,
				R.layout.griditem_addpic, new String[] { "itemImage" },
				new int[] { R.id.imageViewItem });
/**�������ԣ�http://blog.csdn.net/gongzhuo1987/article/details/47868355
 * SimpleAdapter��������ݰ󶨣�����̫���ڼ򵥣�����ImageView�ؼ���
 * Ĭ��ֻ�ܵ���setViewImage((ImageView)v,(Integer)data);
 * �Լ�setViewImage(((ImageView)v,text);�����������һ��ͼƬ�Ļ���Ĭ�ϵĴ����ǲ�֧�ֵģ�
 * Ĭ�ϵĴ���ֻ���������ֻ����ַ���(���־��Ǳ��ص���Դid��R.drawable.pic��������Դ�ļ�id������String�ַ���)��
 * ���������ͼƬ��Ҳ���Ǳ���ͼƬ����ʱĬ�ϵĴ�����޷�����Ҫ���ˡ�
 * �����Ҫ�и߼������ã���ô�ͱ�����ViewBinder����ȥ�����ˡ�ViewBinder��ʵ�������û����ӹ�v�Ĵ���
 * */
		simpleAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView i = (ImageView) view;
					i.setMinimumHeight(colWidth);
					i.setMinimumWidth(colWidth);
					i.setImageBitmap((Bitmap) data);
					return true;
				}
				return false;
			}
		});

		gview.setAdapter(simpleAdapter);
		// �������������Ӻ������ͼƬ���������ͼ����ͼ
		gview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position == itemNo) {//�������+�� ѡ����Ƭ
					Toast.makeText(CommentActivity.this, R.string.addpicture,
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(CommentActivity.this,
							PictureBrowser.class);
					if (selectPictures != null) {//�Ѿ�ѡ�����Ƭ����PictureBrowserĬ�ϴ�
						intent.putExtra(PictureBrowser.RESULT_URIS,
								selectPictures);
					}
					intent.putExtra("hasVideo", hasVideo);
					startActivityForResult(intent, REQUEST_PICTURE);
				} else if (position == hasVideo) {//���������Ƶ ������ֻ��һ����Ƶ��
					Uri uri = Uri.fromFile(new File(videoPath));
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(uri, "video/*");
					startActivity(intent);
				} else {//���������Ƭ
					// ��ʾ��ͼ
					//Toast.makeText(CommentActivity.this, "����ͼ",
					//		Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(CommentActivity.this,
							SelectedPictureActivity2.class);
					intent.putStringArrayListExtra(
							SelectedPictureActivity2.PIC_PATH, pathImage);
					int imgPos =  position ;
					if(hasVideo != -1 && imgPos >hasVideo){
						imgPos--;
					}
					intent.putExtra(SelectedPictureActivity2.PIC_POSITION, imgPos);
					startActivity(intent);

				}

			}
		});

		gview.setLongClickable(true);
		// ��������ͼɾ��
		gview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position < itemNo) {
					dialog(position);
					return true;
				}
				return false;
			}
		});

		editComment = (EditText) findViewById(R.id.editComment);
		// ���ø�����ť
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		// backoff = (ImageButton) findViewById(R.id.header_left_btn);
		// ������ذ�ť��������Activity
		confirm = (Button) findViewById(R.id.header_right_btn);
		// ���ȷ�ϰ�ť���ϴ�����
		photo = (ImageView) findViewById(R.id.comment_photo);
		// ���photo��ť����������գ���onActivityReult�����н���Ƭ���뵽GridView��
		// ѡ����Ƶ
		smallVideo = (ImageView) findViewById(R.id.comment_video);
		// �ص�
		imagePlace = (ImageView) findViewById(R.id.imageView_place);
		textOfPlace = (TextView) findViewById(R.id.textView_place);

		textOfPlace.setText(placeName);

		mood = (RadioGroup) findViewById(R.id.rg_mood);
		rb_happy = (RadioButton) findViewById(R.id.rb_happy);
		rb_general = (RadioButton) findViewById(R.id.rb_general);
		rb_unhappy = (RadioButton) findViewById(R.id.rb_unhappy);

		// backoff.setOnClickListener(this);
		confirm.setOnClickListener(this);
		photo.setOnClickListener(this);
		smallVideo.setOnClickListener(this);
		imagePlace.setOnClickListener(this);
		textOfPlace.setOnClickListener(this);

		mood.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if (checkedId == rb_happy.getId()) {
					feeling = 0;
				}
				if (checkedId == rb_general.getId()) {
					feeling = 1;
				}
				if (checkedId == rb_unhappy.getId()) {
					feeling = 2;
				}
			}
		});
/*--------------------------------------------------------------------------------------*/
		behaviourList = (DropDownListView) findViewById(R.id.drop_down_list_view_behaviour);
		durationList = (DropDownListViewShort) findViewById(R.id.drop_down_list_view_duration);
		partnerNumberList = (DropDownListViewShort) findViewById(R.id.drop_down_list_view_partner_number);
		relationList = (DropDownListViewShort) findViewById(R.id.drop_down_list_view_relationship);
		
//		ArrayList<String> list_duration = new ArrayList<String>();
//		ArrayList<String> list_behaviour = new ArrayList<String>();
//		ArrayList<String> list_partnerNumber = new ArrayList<String>();
//		ArrayList<String> list_relationship = new ArrayList<String>();
//		String[] durationArray = getResources().getStringArray(R.array.durationarray);
//		String[] behaviourArray = getResources().getStringArray(R.array.behaviourarray);
//		String[] partnerArray = getResources().getStringArray(R.array.partnernumarray);
//		String[] relationArray = getResources().getStringArray(R.array.relationarray);
//		for(int i = 0;i<durationArray.length;i++){
//			list_duration.add(durationArray[i]);
//		}
//		for(int i = 0;i<behaviourArray.length;i++){
//			list_behaviour.add(behaviourArray[i]);
//		}
//		for(int i = 0;i<partnerArray.length;i++){
//			list_partnerNumber.add(partnerArray[i]);
//		}
//		for(int i = 0;i<relationArray.length;i++){
//			list_relationship.add(relationArray[i]);
//		}
//���������ԭ������strings.xml�ļ��У�����ͨ��������������أ��������ݿ⣬�ٴ����ݿ��в�ѯ�����ݵ�����
		
		try {
			behaviourList.setItemsData(behaviourData);
			durationList.setItemsData(durationData);
			partnerNumberList.setItemsData(partnerNumData);
			relationList.setItemsData(relationData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*--------------------------------------------------------------------------------------*/
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("Eaa", " resultCode:" + resultCode + " data=" + data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICTURE: {
				Intent intent = data;
				selectPictures = intent.getParcelableExtra(PictureBrowser.RESULT_URIS);
				TreeMap<Long, Uri> selectpics = selectPictures.getTreeMap();
				Set<Long> keys = selectpics.keySet();

				// �������ͼƬ���������
				pathImage.clear();
				// ɾ����������ͼ
				for (int i = 0; i < cacheImages.size(); i++) {
					new File(cacheImages.get(i)).delete();
				}
				cacheImages.clear();
				if (hasVideo >= 0) {
					HashMap<String, Object> video = imageItem.get(hasVideo);
					imageItem.clear();
					imageItem.add(video);
					hasVideo = 0;
					itemNo = 1;
				} else {
					imageItem.clear();
					itemNo = 0;
				}
				selectImages.clear();
				
				Cursor cursor = null;
				String[] projection = { MediaStore.Images.Media.DATA };
				for (Long key : keys) {
					Uri uri = selectpics.get(key);
					cursor = getContentResolver().query(uri, projection, null,
							null, null);
					cursor.moveToFirst();
					String picPath = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					selectImages.add(picPath);
					cursor.close();
					//Log.i("Eaa", "picPath = " + picPath);
				}
				addSymbol();
				break;
			}
			case REQUEST_TAKEPIC: {
				String sdStatus = Environment.getExternalStorageState();
				if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����
					Log.i("TestFile","SD card is not avaiable/writeable right now.");
					Toast.makeText(CommentActivity.this,
							R.string.cantusingsdcard, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (recentPhoto!=null && new File(recentPhoto).exists()) {
					String path = recentPhoto;

					InsertPicToMediastore insertImg = new InsertPicToMediastore();
					insertImg.execute(new Object[] { getApplicationContext(),
							path, Common.currentTime() });

					selectImages.add(recentPhoto);
				} else {
					Toast.makeText(CommentActivity.this,R.string.tips_takepicfail,
							Toast.LENGTH_SHORT).show();
				}

				break;
			}
			case REQUEST_PLACE: {
				placeName = data.getExtras().getString("place");
				//Log.i("placeName", placeName);
				textOfPlace.setText(placeName);
				break;
			}
			case REQUEST_VIDEO: {
				if (null == data) {
					Toast.makeText(getApplicationContext(),getResources().getString(R.string.tips_choosevideofail),
							Toast.LENGTH_SHORT).show();
					videoPath = null;
					return;
				}
				//Log.i("Eaa_video", data.toString());

				try {
					Uri uri = data.getData();
					//Log.i("Eaa_Uri", uri.toString());
					if (!TextUtils.isEmpty(uri.getAuthority())
							|| !TextUtils.isEmpty(uri.getScheme())) {
						// ��ѯѡ����Ƶ
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Video.Media.DATA },
								null, null, null);
						if (null != cursor) {
							
							// ����ƶ�����ͷ ��ȡͼƬ·��
							cursor.moveToFirst();
							videoPath = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.Video.Media.DATA));
							cursor.close();
							//Log.i("Eaa_videoPaht", videoPath);
						} else {
							videoPath = Uri.decode(uri.getEncodedPath());
						}

						// ���ѡ����Ƶ�Ƿ����20M�������ϴ�
						long fileSize = new File(videoPath).length();
						if (fileSize > 1024 * 1024 * 20) {
							Toast.makeText(getApplicationContext(),
									getResources().getString(R.string.tips_selectvideo), Toast.LENGTH_SHORT).show();
							videoPath = null;
							return;
						}
						addVideoThumb(videoPath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case REQUEST_CAMERA: {
				try {
					if (null == data) {
						Toast.makeText(CommentActivity.this,
								getResources().getString(R.string.tips_takevideofail), Toast.LENGTH_SHORT)
								.show();
						break;
					}
					//Log.i("Eaa_camera", data.toString());
					Uri uri = data.getData();
					if (!TextUtils.isEmpty(uri.getAuthority())
							|| !TextUtils.isEmpty(uri.getScheme())) {
						// ��ѯѡ����Ƶ
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Video.Media.DATA },
								null, null, null);
						if (null != cursor) {
							// ����ƶ�����ͷ ��ȡͼƬ·��
							cursor.moveToFirst();
							videoPath = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.Video.Media.DATA));
							cursor.close();
							//Log.i("Eaa_videoPaht", videoPath);
						} else {
							videoPath = Uri.decode(uri.getEncodedPath());
						}

					}

					if (new File(videoPath).exists()) {
						addVideoThumb(videoPath);
					} else {
						Toast.makeText(CommentActivity.this,
								getResources().getString(R.string.tips_takevideofail), Toast.LENGTH_LONG)
								.show();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			default:
				break;
			}
		} else if (resultCode == RESULT_CANCELED) {
			//Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_docancle), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_dofail),
					Toast.LENGTH_LONG).show();
		}
		Log.i("comment", "onActivityResult hasViewo=" + hasVideo + ",itemNo=" + itemNo
				+",selectImages size "+selectImages.size()+",imageItem size"+imageItem.size()+",pathImage size "+pathImage.size());
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//if(selectImages != null && imageItem!=null){
			//Log.i("comment", "before resume hasViewo=" + hasVideo + ",itemNo=" + itemNo
			//	+",selectImages size "+selectImages.size()+",imageItem size "+imageItem.size()+",pathImage size "+pathImage.size());
		//}
		if (selectImages.size() > 0) {
			if (itemNo < 9 && itemNo < imageItem.size()) {
				imageItem.remove(itemNo);
			}
			for (int i = 0; i < selectImages.size(); i++) {
				Bitmap addbmp = null;
				try{
					addbmp = Common.scaleBitmap(selectImages.get(i),
						Common.winWidth, Common.winHeight);
				}catch(OutOfMemoryError e){
	            	
	            }
				// ��������ͼ
				if(null == addbmp){
					continue;
				}
				addbmp = ThumbnailUtils.extractThumbnail(addbmp, colWidth,
						colWidth);
				// ������ͼд��Ӧ�õ�ͼƬ�����ļ���
				String imageName;

				imageName = Common.currentTime().trim();

				cacheFileName = Common.CACHEPHOTO_PATH + "cache" + imageName
						+ i + ".jpg";
				BufferedOutputStream fos;
				try {
					fos = new BufferedOutputStream(new FileOutputStream(
							cacheFileName));
					addbmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// ԭͼ������ͼ�ɶԳ��֣��ɶ���ӣ��ɶ�ɾ��
				pathImage.add(selectImages.get(i));
				cacheImages.add(cacheFileName);

				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(keyOfBitmap, addbmp); // ���Ҫ��ʾ������ͼ��GridView
				imageItem.add(map);
				itemNo++;
			}
			// ʹ�Ӻź���
			if (itemNo < 9) {
				addSymbol();
			}
			simpleAdapter.notifyDataSetChanged();
			// ˢ�º��ͷŷ�ֹ�ֻ����ߺ��Զ����
			selectImages.clear();
		}
//		if(selectImages != null && imageItem!=null){
//			Log.i("comment", "after resume hasViewo=" + hasVideo + ",itemNo=" + itemNo
//				+",selectImages size "+selectImages.size()+",imageItem size"+imageItem.size()+",pathImage size "+pathImage.size());
//		}
		super.onResume();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putString("recentPhoto", recentPhoto);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		recentPhoto = savedInstanceState.getString("recentPhoto");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// ɾ����������ͼ
		for (int i = 0; i < cacheImages.size(); i++) {
			new File(cacheImages.get(i)).delete();
		}
		if (hasVideo >= 0) {
			new File(cacheVideo).delete();
		}
		
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		for(int i = 0;i<imageItem.size();i++){
			if(i == hasVideo){
				continue;
				
			}
			Bitmap bitmap = (Bitmap) imageItem.get(i).get(keyOfBitmap);
			if(bitmap != null){
				bitmap.recycle();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	/*
	 * Dialog�Ի�����ʾ�û�ɾ������ positionΪɾ��ͼƬλ��
	 */
	protected void dialog(final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(CommentActivity.this);
		builder.setMessage(getResources().getString(R.string.areyousuredelete));
		builder.setTitle(getResources().getString(R.string.tips));
		builder.setPositiveButton(getResources().getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (position == hasVideo) {
							imageItem.remove(position);
							hasVideo = -1;
							itemNo--;
							videoPath = null;
							new File(cacheVideo).delete();
							simpleAdapter.notifyDataSetChanged();
						} else {
							int pos = position;
							if(hasVideo != -1 && pos >hasVideo){
								pos--;
							}
							if(pos >= imageItem.size() || pos >= pathImage.size()){
								Log.i("comment","delete error :"+pos+"/"+pathImage.size());
								return;
							}
							Log.i("comment","delete  :"+pos+"/"+pathImage.size());
							
							imageItem.remove(position);
							// �ɶ�ɾ��
							String path = pathImage.get(pos);
							StringBuffer buff = new StringBuffer();
							buff.append("(").append(Images.ImageColumns.DATA)
									.append("=").append("'" + path + "'")
									.append(")");
							Cursor cursor = getContentResolver().query(
									Images.Media.EXTERNAL_CONTENT_URI,
									new String[] { Images.ImageColumns._ID },
									buff.toString(), null, null);
							if (cursor.moveToNext()) {
								long id = cursor.getLong(0);
								TreeMap<Long, Uri> tree = selectPictures
										.getTreeMap();
								tree.remove(id);
								selectPictures.setTreeMap(tree);
							}
							cursor.close();
							pathImage.remove(pos);
							new File(cacheImages.get(pos)).delete();
							cacheImages.remove(pos);
							simpleAdapter.notifyDataSetChanged();
							itemNo--;
							
							if (pos < hasVideo) {
								hasVideo--;
								//Log.d("Eaa", "hasVideo=" + hasVideo);
							}
							
						}
						if(itemNo == 8){
							addSymbol();
						}
						Log.i("comment", "delete hasViewo=" + hasVideo + ",itemNo=" + itemNo
								+",selectImages size "+selectImages.size()+",imageItem size"+imageItem.size()+",pathImage size "+pathImage.size());
						
						
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancl),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// ��ʼ�������¼�
	private void initEvent() {
		behaviour = behaviourList.getSelectedPosition();
		stay = durationList.getSelectedPosition();
		companion = partnerNumberList.getSelectedPosition();
		relation = relationList.getSelectedPosition();
		comment = new InterestMarkerData();
		// �����¼�����
		comment.setCreatetime(createTime);
		comment.setLongitude(longitude);
		comment.setLatitude(latitude);
		comment.setAltitude(altitude);
		comment.setPlaceName(placeName);
		comment.setContent(commentText);
		comment.setFileNum(fileNum);
		comment.setTraceNo(traceNo);
		comment.setVideoCount(0);
		comment.setAudioCount(0);
		comment.setUserId(Common.getUserId(getApplicationContext()));
		comment.setFeeling(feeling);
		comment.setBehaviour(behaviour);
		comment.setDuration(stay);
		comment.setCompanion(companion);
		comment.setRelationship(relation);
		// ��Ҫ�ϴ����ļ���ӵ�sharedPreferences
		SharedPreferences uploadFiles = getSharedPreferences(SHAREDFILES,
				Activity.MODE_PRIVATE);
		Editor edit = uploadFiles.edit();

		Log.i("comment", "placeName=" + placeName + "|comment=" + commentText
				+ "|fileNum=" + fileNum+"|feeling" + feeling + "|behaviour="+behaviour+"|stay="+stay
				+"|companion="+companion+"|relation="+relation);

		// �����ļ�����
		for (int i = 0; i < fileNum; i++) { // ͼƬ
			files[i] = new CommentMediaFiles();
			files[i].setFileNo(i);
			files[i].setDateTime(createTime);
			files[i].setFileName(pathImage.get(i));
			files[i].setThumbnailName(cacheImages.get(i));
			files[i].setType(1);
			edit.putString(
					createTime + File.separator + i + File.separator + 1
							+ File.separator
							+ Common.getUserId(getApplicationContext()),
					pathImage.get(i));
		}
		edit.commit();
		// ��Ƶ
		if (videoPath != null) {
			comment.setVideoCount(1);
			files[fileNum] = new CommentMediaFiles();
			files[fileNum].setFileNo(fileNum);
			files[fileNum].setDateTime(createTime);
			files[fileNum].setFileName(videoPath);
			files[fileNum].setThumbnailName(cacheVideo);
			files[fileNum].setType(2);
			comment.setFileNum(fileNum + 1);
			edit.putString(
					createTime + File.separator + fileNum + File.separator + 2
							+ File.separator
							+ Common.getUserId(getApplicationContext()),
					videoPath);
		}
		edit.commit();
	}

	private void commitEvent() {
		dbHelper = new PhotoDBHelper(this, PhotoDBHelper.DBWRITE);//����Ȥ�����ݿ�
		traceHelper = new TraceDBHelper(this);//�켣��Ϣ���ݿ�
		TraceData tracedata = new TraceData();
		tracedata = traceHelper.queryfromTrailbytraceNo(comment.getTraceNo(), comment.getUserId());
		if(tracedata!=null){
			tracedata.setPoiCount(tracedata.getPoiCount()+1);
			Log.i("traceee", "tracedata = "+tracedata.getPoiCount()+tracedata.getCalorie()+tracedata.getDistance()+tracedata.getDuration()+tracedata.getEndTime()+tracedata.getShareType()+tracedata.getSportType()+tracedata.getStartTime()+tracedata.getTraceName()+tracedata.getTraceNo()+tracedata.getUserID());
			traceHelper.updatetrail(tracedata, comment.getTraceNo(), comment.getUserId());			
		}
		int result = dbHelper.insertEvent(comment);
		//Log.i("Eaa", "comment result=" + result);
		for (int i = 0; i < itemNo; i++) {
			dbHelper.inserFile(files[i]);
		}
		dbHelper.closeDB();
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		// EventData
		bundle.putString("createTime", comment.getCreatetime());
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);

	}

	// ��gridView����ӣ���
	private void addSymbol() {
		Bitmap bmp;
		// ����ͼƬ
		if (itemNo < 9) {
			bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.add_img);
			// ���ͼƬ����ͼ
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(keyOfBitmap, bmp);
			imageItem.add(map);
		}
		
	}

	private void addVideoThumb(String videoPath) {
		// �����Ƶ����ͼ
		Bitmap bmp;
		HashMap<String, Object> map;
		if (null != videoPath) {
			if (itemNo < 9 && itemNo < imageItem.size()) {
				imageItem.remove(itemNo);//ɾ�������+��
			}
			if (hasVideo < 0) {
				bmp = ThumbnailUtils.createVideoThumbnail(videoPath,
						MediaStore.Images.Thumbnails.MINI_KIND);
				map = new HashMap<String, Object>();
				hasVideo = itemNo;
				//Log.d("Eaa_addVideo", "add hasVideo=" + hasVideo);
				itemNo++;
			} else {
				map = imageItem.remove(hasVideo);
				bmp = ThumbnailUtils.createVideoThumbnail(videoPath,
						MediaStore.Images.Thumbnails.MINI_KIND);
			}

			bmp = ThumbnailUtils.extractThumbnail(bmp, colWidth, colWidth);

			String videoName = Common.currentTime().trim();
			cacheFileName = Common.CACHEPHOTO_PATH + "cacheVideo" + videoName
					+ ".jpg";
			Bitmap videoPlay = BitmapFactory.decodeResource(getResources(),
					R.drawable.video_play);
			Bitmap newBmp = bmp.copy(Bitmap.Config.RGB_565, true);
			Canvas canvas = new Canvas(newBmp);
			canvas.drawBitmap(bmp, 0, 0, null);
			canvas.drawBitmap(videoPlay,
					bmp.getWidth() / 2 - videoPlay.getWidth() / 2,
					bmp.getHeight() / 2 - videoPlay.getHeight() / 2, null);

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			bmp = newBmp;
			map.put(keyOfBitmap, bmp);
			imageItem.add(hasVideo, map);
			addSymbol();
			BufferedOutputStream bos;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(
						cacheFileName));
				bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			simpleAdapter.notifyDataSetChanged();
			cacheVideo = cacheFileName;
		}
	}

	/**
	 * CommentActivity�и�������ĵ���¼�
	 * 
	 * @param v
	 *            ��������
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_back:
			CommentActivity.this.finish();
			break;
		case R.id.header_right_btn:
			// ���ύʱ��Ϊ����ʱ��
			commentText = editComment.getText().toString().trim();
			if ("".equals(commentText) || commentText == null) {
				Toast.makeText(CommentActivity.this,getResources().getString(R.string.tips_commentsubmit),
						Toast.LENGTH_SHORT).show();
				break;
			}
			if(behaviourList.getSelectedPosition() == -1){
				Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_selectbehaviour),
						Toast.LENGTH_SHORT).show();
				break;
			}
			if(durationList.getSelectedPosition() == -1){
				Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_selectduration),
						Toast.LENGTH_SHORT).show();
				break;
			}
			if(partnerNumberList.getSelectedPosition() == -1){
				Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_selectpartnernum),
						Toast.LENGTH_SHORT).show();
				break;
			}
			if(relationList.getSelectedPosition() == -1){
				Toast.makeText(CommentActivity.this, getResources().getString(R.string.tips_selectrelationship),
						Toast.LENGTH_SHORT).show();
				break;
			}
			if(createTime == null){
				createTime = Common.currentTime();

			}
			
			fileNum = hasVideo < 0 ? itemNo : itemNo - 1;
			files = new CommentMediaFiles[fileNum + 2];
			initEvent();
			commitEvent();
			this.finish();
			break;
		case R.id.comment_photo://������հ�ť
			if (itemNo < 9) {
				String status = Environment.getExternalStorageState();
				Log.i("Eaa", "status:"+status);
				if (status.equals(Environment.MEDIA_MOUNTED)) {
					try {
						File imgDirs = new File(Common.PHOTO_PATH);
						if (!imgDirs.exists()) {
							if (imgDirs.mkdirs()) {
								Toast.makeText(CommentActivity.this,
										getResources().getString(R.string.tips_savepath) + Common.PHOTO_PATH,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(CommentActivity.this,
										getResources().getString(R.string.tips_norighttomkdir), Toast.LENGTH_SHORT)
										.show();
								return;
							}
						}
						File imageName = new File(imgDirs, "HappyNavi_"
								+ Common.currentTime().trim() + ".jpg");
						imageName.createNewFile();
						recentPhoto = imageName.getAbsolutePath();
						Uri uri = Uri.fromFile(imageName);
					
						Log.i("Eaa", "_photoPath_"+recentPhoto);
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.Images.Media.ORIENTATION, 1);
						//�趨����ʱ�ֻ�����Ϊ����
						//�趨��Ƭ�����·��
						intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
						startActivityForResult(intent, REQUEST_TAKEPIC);
					} catch (ActivityNotFoundException e) {
						// TODO Auto-generated catch block
						Toast.makeText(CommentActivity.this,getResources().getString(R.string.tips_findsdfail),
								Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						Toast.makeText(CommentActivity.this,R.string.tips_createfilefail,
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(CommentActivity.this,getResources().getString(R.string.tips_nosd),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(CommentActivity.this, R.string.nomorethan9,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.comment_video:
			int videoNum = 0;
			if (hasVideo >= 0) {
				videoNum = 1;
				Toast.makeText(CommentActivity.this,getResources().getString(R.string.tips_selectvideoagain),
						Toast.LENGTH_SHORT).show();
			}

			if (itemNo -videoNum < 9) {
//				Toast.makeText(CommentActivity.this, "ѡ����Ƶ", Toast.LENGTH_SHORT)
//						.show();

				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				CharSequence[] items = getResources().getStringArray(R.array.way_selectvideo);
				builder.setTitle(getResources().getString(R.string.tips_dlgtle_selectvideo));
				builder.setSingleChoiceItems(items, 0,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO �Զ����ɵķ������
								switch (which) {
								case 0:
									// �����������
									Intent intent = new Intent();
									intent.setAction("android.media.action.VIDEO_CAPTURE");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.putExtra(
											MediaStore.EXTRA_VIDEO_QUALITY, 1);
									intent.putExtra(
											MediaStore.EXTRA_DURATION_LIMIT,
											200000);
									intent.putExtra(
											MediaStore.EXTRA_SIZE_LIMIT,
											20 * 1024 * 1024);
									startActivityForResult(intent,
											REQUEST_CAMERA);

									break;
								case 1: {
									// �ļ�ѡ��
									Intent intent2 = new Intent(
											Intent.ACTION_GET_CONTENT);
									intent2.setType("video/*");
									startActivityForResult(intent2,
											REQUEST_VIDEO);
									break;
								}
								default:
									break;
								}
								dialog.dismiss();
								;

							}

						});
				builder.create().show();

			} else {
				Toast.makeText(CommentActivity.this, R.string.nomorethan9,
						Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.imageView_place:
			// ���ͼ��͵������Ч����ͬ
		case R.id.textView_place:
			Intent intent = new Intent(CommentActivity.this, ConfirmPlace.class);
			placeName = (String) textOfPlace.getText();
			intent.putExtra("place", placeName);
			startActivityForResult(intent, REQUEST_PLACE);
			break;
		default:
			break;
		}
	}

	/**
	 * ��ͼƬ��ӵ�ϵͳý�����
	 * 
	 * @author Eaa
	 *
	 */
	public class InsertPicToMediastore extends
			AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			// ͼƬ·��
			Context context = (Context) params[0];
			String path = (String) params[1];
			long time = Common.timeStamp((String) params[2]);
			File imgFile = new File(path);
			Uri uri = Uri.fromFile(imgFile);

			ContentValues values = new ContentValues();
			ContentResolver resolver = context.getContentResolver();
			values.put(MediaStore.Images.ImageColumns.DATA, path);
			values.put(MediaStore.Images.ImageColumns.TITLE,
					uri.getLastPathSegment());
			values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME,
					uri.getLastPathSegment());
			values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, time);
			values.put(MediaStore.Images.ImageColumns.DATE_ADDED, time / 1000);
			values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED,
					time / 1000);
			values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpg");
			Uri mediaUri = resolver.insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

			resolver.update(mediaUri, values, null, null);

			return mediaUri;
		}

		@Override
		protected void onPostExecute(Object result) {
			Uri insertUri = (Uri) result;
			Cursor cursor = getContentResolver().query(insertUri,
					new String[] { Images.ImageColumns._ID }, null, null, null);
			if (cursor.moveToNext()) {
				long id = cursor.getLong(0);
				if (selectPictures != null) {
					TreeMap<Long, Uri> tree = selectPictures.getTreeMap();
					tree.put(id, insertUri);
					selectPictures.setTreeMap(tree);
				} else {
					selectPictures = new SelectedTreeMap();
					TreeMap<Long, Uri> tree = new TreeMap<Long, Uri>();
					tree.put(id, insertUri);
					selectPictures.setTreeMap(tree);
				}

			}
			cursor.close();
			super.onPostExecute(result);
		}
	}
}