package com.trackersurvey.happynavi;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.MyLinearLayout;

import com.trackersurvey.httpconnection.PostAdvice;
import com.trackersurvey.httpconnection.PostAdvicePic;

import com.trackersurvey.photoview.SelectedTreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

import android.util.Base64;
import android.util.DisplayMetrics;
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

import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class Advice extends Activity implements OnClickListener {
	public static final int REQUEST_PICTURE = 1;
	public static final int REQUEST_TAKEPIC = 2;
	public static final int REQUEST_PLACE = 3;
	public static final int REQUEST_VIDEO = 4;
	public static final int REQUEST_CAMERA = 5;

	// gradView使用的多个HashMap的一个常用key
	private static final String keyOfBitmap = "itemImage";
	private static final int gviewCol = 3; // gridView列数

	private GridView gview; // 网格显示缩略图
	private ArrayList<HashMap<String, Object>> imageItem; // gridView 组件集
	private int itemNo; // gridView中组件个数
	private SimpleAdapter simpleAdapter;

	// 选择的多张照片的id和Uri
	private SelectedTreeMap selectPictures;
	private static ArrayList<String> pathImage; // 选择的所有图片路径
	private static ArrayList<String> cacheImages;
	private static String cachaVideo;
	private ArrayList<String> selectImages; // 选择的图片的绝对路径
	private int hasVideo = -1;

	
	private String placeName = "";
	private String commentText;
	
	private String videoPath = null; // 选择的视频的绝对路径
	private String cameraPath = null;
	private String cacheFileName;
	private  String cacheVideo;
	private int colWidth; // GridVied 列宽

	// 界面上下各个按钮
	private MyLinearLayout back; // 后退
	private Button confirm; // 确认
	//private ImageView photo; // 调用相机拍照
	//private ImageView smallVideo; // 拍摄小视频
	private ImageView imagePlace; // 位置图标
	private TextView textOfPlace; // 选择位置
	private EditText editComment;

	private String recentPhoto;
	private File file;
	private File cacheFile;
	
	
	String current_time="";
	private	String post_advice_url = null;//"http://219.218.118.176:8090/Communication/upFeedBack.aspx";
	private	String post_advice_pic_url = null;//"http://219.218.118.176/Mobile/upFeedBackPic.aspx";	
	
@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 自定义标题栏
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.advice);
		
		// 切换中英文
		//-----------------------------------------------------//
		Resources resources = getResources();
		Configuration configure = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TabHost_Main.l==0){
			configure.locale = Locale.CHINESE;
		}
		if(TabHost_Main.l==1){
			configure.locale = Locale.ENGLISH;
		}
		if(TabHost_Main.l==2) {
			configure.locale = new Locale("cs", "CZ");
		}
		resources.updateConfiguration(configure, dm);
		//-----------------------------------------------------//
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.advice_title);
		AppManager.getAppManager().addActivity(this);
		
		colWidth = (Common.winWidth - gviewCol * 8) / gviewCol - 10; // 列宽适配屏幕宽度
		gview = (GridView) findViewById(R.id.gridPicture);
		
		itemNo = 0;
		pathImage = new ArrayList<String>();
		cacheImages = new ArrayList<String>();
		imageItem = new ArrayList<HashMap<String, Object>>();
		addSymbol();
		selectImages = new ArrayList<String>();

		// 用adapter控制gridView
		simpleAdapter = new SimpleAdapter(getApplicationContext(), imageItem,
				R.layout.griditem_addpic, new String[] { "itemImage" },
				new int[] { R.id.imageViewItem });

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
		// 点击监听，点击加号添加新图片，点击缩略图看大图
		gview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position == itemNo) {
					Toast.makeText(Advice.this, R.string.addpicture,
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(Advice.this,
							PictureBrowser.class);
					if (selectPictures != null) {
						intent.putExtra(PictureBrowser.RESULT_URIS,
								selectPictures);
					}
					intent.putExtra("hasVideo", hasVideo);
					startActivityForResult(intent, REQUEST_PICTURE);
				} else if (position == hasVideo) {
					Uri uri = Uri.fromFile(new File(videoPath));
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(uri, "video/*");
					startActivity(intent);
				} else {
					// 显示大图
					//Toast.makeText(Advice.this, "看大图",
					//		Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(Advice.this,
							SelectedPictureActivity.class);
					intent.putStringArrayListExtra(
							SelectedPictureActivity.PIC_PATH, pathImage);
					int imgPos =  position ;
					if(hasVideo != -1 && imgPos >hasVideo){
						imgPos--;
					}
					intent.putExtra(SelectedPictureActivity.PIC_POSITION, imgPos);
					startActivity(intent);

				}

			}
		});

		gview.setLongClickable(true);
		// 长按缩略图删除
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

		file = new File(Common.APPLICATION_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(Common.PHOTO_PATH);
		cacheFile = new File(Common.CACHEPHOTO_PATH);
		// 创建文件夹
		if (!file.exists()) {
			file.mkdirs();
		}
		if (!cacheFile.exists()) {
			cacheFile.mkdirs();
		}

		editComment = (EditText) findViewById(R.id.editComment);
		// 设置各个按钮
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		// backoff = (ImageButton) findViewById(R.id.header_left_btn);
		// 点击返回按钮，结束本Activity
		confirm = (Button) findViewById(R.id.advice_submit);
		// 点击确认按钮，上传评论
		//photo = (ImageView) findViewById(R.id.comment_photo);
		// 点击photo按钮，打开相机拍照，在onActivityReult方法中将照片加入到GridView中
		// 选择视频
	//	smallVideo = (ImageView) findViewById(R.id.comment_video);
		// 地点
		imagePlace = (ImageView) findViewById(R.id.imageView_place);
		textOfPlace = (TextView) findViewById(R.id.textView_place);

		textOfPlace.setText(placeName);

		// backoff.setOnClickListener(this);
		confirm.setOnClickListener(this);
		//photo.setOnClickListener(this);
		///smallVideo.setOnClickListener(this);
		imagePlace.setOnClickListener(this);
		textOfPlace.setOnClickListener(this);
		
		//file = new File(Common.PHOTO_PATH);
	}
	// 上传图片 字节流
			public static String testUpload(String path) {
				try {

					String srcUrl = path; // "/mnt/sdcard/"; //路径
					// String fileName = PhotoName+".jpg"; //文件名
					FileInputStream fis = new FileInputStream(srcUrl);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[65536];
					int count = 0;
					while ((count = fis.read(buffer)) >= 0) {
						baos.write(buffer, 0, count);
					}
					String uploadBuffer = new String(Base64.encode(baos.toByteArray(),
							Base64.DEFAULT)); // 进行Base64编码

					fis.close();// 这两行原来没有
					baos.flush();
					return uploadBuffer;

				} catch (Exception e) {
					e.printStackTrace();
				}
				// return soapObject;
				return null;

			}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("Eaa", " resultCode:" + resultCode + " data" + data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICTURE: {
				Intent intent = data;
				selectPictures = intent
						.getParcelableExtra(PictureBrowser.RESULT_URIS);
				TreeMap<Long, Uri> selectpics = selectPictures.getTreeMap();
				Set<Long> keys = selectpics.keySet();

				// 清空已有图片，重新添加
				pathImage.clear();
				// 删除本地缩略图
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
				if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
					Log.i("TestFile",
							"SD card is not avaiable/writeable right now.");
					Toast.makeText(getApplicationContext(),
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
					Toast.makeText(Advice.this,R.string.tips_takepicfail,
							Toast.LENGTH_SHORT).show();
				}

				break;
			}
			case REQUEST_PLACE: {
				placeName = data.getExtras().getString("place");
				Log.i("placeName", placeName);
				textOfPlace.setText(placeName);
				break;
			}
			case REQUEST_VIDEO: {
				if (null == data) {
					break;
				}
				Log.i("Eaa_video", data.toString());

				try {
					Uri uri = data.getData();
					Log.i("Eaa_Uri", uri.toString());
					if (!TextUtils.isEmpty(uri.getAuthority())
							|| !TextUtils.isEmpty(uri.getScheme())) {
						// 查询选择视频
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Video.Media.DATA },
								null, null, null);
						if (null != cursor) {
							// 光标移动至开头 获取图片路径
							cursor.moveToFirst();
							videoPath = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.Video.Media.DATA));
							cursor.close();
							Log.i("Eaa_videoPaht", videoPath);
						} else {
							videoPath = Uri.decode(uri.getEncodedPath());
						}

						// 如果选择视频是否大于20M，不能上传
						long fileSize = new File(videoPath).length();
						if (fileSize > 1024 * 1024 * 20) {
							Toast.makeText(getApplicationContext(),
									R.string.tips_selectvideo, Toast.LENGTH_LONG).show();
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
						break;
					}
					//Log.i("Eaa_camera", data.toString());
					Uri uri = data.getData();
					if (!TextUtils.isEmpty(uri.getAuthority())
							|| !TextUtils.isEmpty(uri.getScheme())) {
						// 查询选择视频
						Cursor cursor = getContentResolver().query(uri,
								new String[] { MediaStore.Video.Media.DATA },
								null, null, null);
						if (null != cursor) {
							// 光标移动至开头 获取图片路径
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
						Toast.makeText(Advice.this,
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
			//Toast.makeText(Advice.this, getResources().getString(R.string.tips_docancle), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(Advice.this, getResources().getString(R.string.tips_dofail),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(selectImages != null && imageItem!=null){
			Log.i("comment", "before resume hasViewo=" + hasVideo + ",itemNo=" + itemNo
				+",selectImages size "+selectImages.size()+",imageItem size "+imageItem.size()+",pathImage size "+pathImage.size());
		}
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
				// 生成缩略图
				if(null == addbmp){
					continue;
				}
				addbmp = ThumbnailUtils.extractThumbnail(addbmp, colWidth,
						colWidth);
				// 将缩略图写入应用的图片缓存文件夹
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

				// 原图与缩略图成对出现，成对添加，成对删除
				pathImage.add(selectImages.get(i));
				cacheImages.add(cacheFileName);

				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(keyOfBitmap, addbmp); // 添加要显示的缩略图到GridView
				imageItem.add(map);
				itemNo++;
			}
			// 使加号后推
			if (itemNo < 9) {
				addSymbol();
			}
			simpleAdapter.notifyDataSetChanged();
			// 刷新后释放防止手机休眠后自动添加
			selectImages.clear();
		}
		if(selectImages != null && imageItem!=null){
			Log.i("comment", "after resume hasViewo=" + hasVideo + ",itemNo=" + itemNo
				+",selectImages size "+selectImages.size()+",imageItem size"+imageItem.size()+",pathImage size "+pathImage.size());
		}
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
		// 删除本地缩略图
		for (int i = 0; i < cacheImages.size(); i++) {
			new File(cacheImages.get(i)).delete();
		}
		if (hasVideo >= 0) {
			new File(cachaVideo).delete();
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
	 * Dialog对话框提示用户删除操作 position为删除图片位置
	 */
	protected void dialog(final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(Advice.this);
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
							new File(cachaVideo).delete();
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
							// 成对删除
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

	

	// 向gridView中添加＋号
	private void addSymbol() {
		Bitmap bmp;
		// 加载图片
		if (itemNo < 9) {
			bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.add_img);
			// 获得图片缩略图
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(keyOfBitmap, bmp);
			imageItem.add(map);
		}
	}

	private void addVideoThumb(String videoPath) {
		// 添加视频缩略图
		Bitmap bmp;
		HashMap<String, Object> map;
		if (null != videoPath) {
			if (itemNo < 9 && itemNo < imageItem.size()) {
				imageItem.remove(itemNo);//删掉后面的+号
			}
			if (hasVideo < 0) {
				bmp = ThumbnailUtils.createVideoThumbnail(videoPath,
						MediaStore.Images.Thumbnails.MINI_KIND);
				map = new HashMap<String, Object>();
				hasVideo = itemNo;
				Log.d("Eaa", "add hasVideo=" + hasVideo);
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
	 * Advice中各个组件的点击事件
	 * 
	 * @param v
	 *            点击的组件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_back:
			Advice.this.finish();
			break;
			
		case R.id.advice_submit:
			commentText = editComment.getText().toString().trim();
			if ("".equals(commentText) || commentText == null) {
				Toast.makeText(Advice.this,R.string.tips_advicesubmit,
						Toast.LENGTH_SHORT).show();
				break;
			}
			

			StringBuffer sb=new StringBuffer();
			
			
			current_time=Common.currentTime();
			
			sb.append(Common.getUserId(this));
			sb.append("分隔符");
			sb.append(current_time);
			sb.append("分隔符");
			//sb.append(placeName);
			//sb.append("分隔符");
			sb.append(commentText);
			sb.append("分隔符");
			
			int pic_num=pathImage.size();
			Log.i("sub_pic_num",pic_num+"");
			
			sb.append(pic_num);
			sb.append("分隔符");
			
			
//			for(int i=0;i<pic_num;i++)
//			{	
//			Log.i("sub_pic",pathImage.get(i));
//	          sb.append(testUpload(pathImage.get(i)));	
//	          sb.append("分隔符");
//			}
			
			if(Common.url == null ||Common.url.equals("")){
				Common.url = getResources().getString(R.string.url);
			}
			post_advice_url = Common.url+"upFeedBack.aspx";
			Log.i("submit", sb.toString());
			PostAdvice pa = new PostAdvice(handler_postadvice, post_advice_url,
					sb.toString(),Common.getDeviceId(getApplicationContext()));
		    pa.start();
			
			break;
		
	

		case R.id.imageView_place:
			// 点击图标和点击文字效果相同
		case R.id.textView_place:
			Intent intent = new Intent(Advice.this, ConfirmPlace.class);
			placeName = (String) textOfPlace.getText();
			intent.putExtra("place", placeName);
			startActivityForResult(intent, REQUEST_PLACE);
			break;
		default:
			break;
		}
	}

	// 压缩图片
		private String comepressAFile( String fileName) {
			String path = fileName;
			
		

			String fileStr = null;
		
				Log.e("图片", "缩放图片后上传");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true; // 只读图片属性，不读像素数据
				BitmapFactory.decodeFile(path, options);
				// 如果需要缩放
				if ((options.inSampleSize = calculateInSampleSize(options,
						Common.winWidth, Common.winHeight)) > 1) {
					options.inJustDecodeBounds = false; // 读压缩图片
					Bitmap bmp = BitmapFactory.decodeFile(path, options);
					File tempFile = new File(path + "temp");

					// 生成缩后的图，写入本地，用完后删除
					BufferedOutputStream bos;
					try {
						bos = new BufferedOutputStream(new FileOutputStream(
								tempFile));
						bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);
						bos.flush();
						bos.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					fileStr = testUpload(tempFile.getAbsolutePath());
					tempFile.delete(); // 删除缩放图
				} else { // 如果不需要缩放，直接读原图
					fileStr = testUpload(path);
				}
				return fileStr;
			
			
		

		}
	
	
	
	
	
	// 计算图片上传图片需要缩放的比例
		public static int calculateInSampleSize(BitmapFactory.Options options,
				int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float) height / (float) reqHeight);
				} else {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				}
			}

			if (inSampleSize < 1) {
				inSampleSize = 1;
			}
			return inSampleSize;
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//上传建议的接收
		private Handler handler_postadvice = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					// 上传建议信息成功  在里面传图片
					String information = (msg.obj.toString().trim());
					Log.i("advice", information);
					
				
					
					int pic_num=pathImage.size();
					
					Log.i("sub_pic_num",pic_num+"");
					
					for(int i=0;i<pic_num;i++)
					{	
						Log.i("sub_pic",pathImage.get(i));                                     
						String str=Common.getUserId(Advice.this)+"分隔符"+current_time+"分隔符" +i+ "分隔符"+    (comepressAFile(pathImage.get(i)));	
						if(Common.url_file == null ||Common.url_file.equals("")){
							Common.url_file = getResources().getString(R.string.url_file);
						}
						post_advice_pic_url = Common.url_file + "upFeedBackPic.aspx";
				      	PostAdvicePic pap = new PostAdvicePic(handler_postadvicepic, post_advice_pic_url,str);
					    pap.start();
					}
					Toast.makeText(Advice.this, R.string.tips_advicesuccess, Toast.LENGTH_SHORT).show();
					finish();
					
					
				    break;

				case 1:// 不成功但连接了

					Toast.makeText(Advice.this,R.string.tips_postfail, Toast.LENGTH_SHORT).show();
					
					break;
				case 10:// 连接失败

					Toast.makeText(Advice.this,R.string.tips_netdisconnect, Toast.LENGTH_SHORT).show();
					
					break;

				}
			}

		};
	
	
	
	
		//上传图片的接收
				private Handler handler_postadvicepic = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						switch (msg.what) {
						case 0:
							// 上传图片成功  
		 					
							break;

						case 1:
							break;
						case 10:// 连接失败

						
							break;

						}
					}

				};
			
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 将图片添加到系统媒体库中
	 * 
	 * @author Eaa
	 *
	 */
	public class InsertPicToMediastore extends
			AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			// 图片路径
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