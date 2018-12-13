package com.trackersurvey.happynavi;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.trackersurvey.adapter.ListBaseAdapter2;
import com.trackersurvey.adapter.ListBaseAdapter2.BackImageListener;
import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.db.PointOfInterestDBHelper;
import com.trackersurvey.entity.PointOfInterestData;
import com.trackersurvey.adapter.TraceListAdapter;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.httpconnection.PostPointOfInterestData;
import com.trackersurvey.httpconnection.PostPointOfInterestDataEn;
import com.trackersurvey.model.MyCommentModel;
import com.trackersurvey.photoview.SlideListView;

import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * �鿴��������
 * 
 * @author Eaa
 * @version 2015��12��4�� ����2:38:33
 */
public class MyCommentActivity extends Activity implements OnClickListener {

	private static final int REQUEST_PICTURE = 1;
	private MyLinearLayout back;
	private TextView titleText; // �����ı�
	private Button titleButton; // ����ȷ�ϰ�ť
	private SlideListView lView; // ����������б�
	private String bgImageName = "bgImage.jpg";

	// �����ҵ��������õ�sharedPreferences
	private  SharedPreferences myCommentSetting;
	// ������������ģ��
	private MyCommentModel myComment;
	// listView��������
	private ListBaseAdapter2 listAdapter;
	private PointOfInterestData behaviourData,durationData, partnerNumData, relationData;
	private PointOfInterestDBHelper helper = null;
	private String URL_GETPOI = null;
	private Cursor cursor = null;

	/**
	 * onCreate�ӱ������ݿ��ȡ���۲���ʾ ����������ݿ�գ���ʾͬ���ƶ�����
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mycomment_layout);
		
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
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		myCommentSetting = getSharedPreferences("myCommentSetting",
				Activity.MODE_PRIVATE);
		myCommentSetting.edit().commit();

		// Log.d("Eaa", "OnCreate");
		// ���ñ��������
		// ������ذ�ť��������Activity
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		// titleBackOff = (ImageButton) findViewById(R.id.header_left_btn);
		// ��������
		titleText = (TextView) findViewById(R.id.header_text);
		titleText.setText(getResources().getString(R.string.myalbum));
		// �Ҳ���ť���ɼ�
		titleButton = (Button) findViewById(R.id.header_right_btn);
		titleButton.setText(getResources().getString(R.string.refresh));
		// titleButton.setVisibility(View.INVISIBLE);

		// titleBackOff.setOnClickListener(this);
		titleButton.setOnClickListener(this);
		URL_GETPOI = Common.url + "requestInfo.aspx";
		helper = new PointOfInterestDBHelper(this);//�������POI���ݿ�
		int l = TabHost_Main.l;
		if(l==0){			
			initPOI();
		}
		if(l==1){
			initPOIEN();
		}
		Common.createFileDir();
		initModel();
//		initAlbum();
		initAdapter();

		// �ҵ����� �����е�ListView
		lView = (SlideListView) findViewById(R.id.albumlistview);
		lView.initSlideMode(SlideListView.MOD_RIGHT);

		lView.setAdapter(listAdapter);
		lView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
						|| scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					if ((view.getLastVisiblePosition() == view.getCount() - 1)
							&&myComment.cloudMore()) {
						Log.i("Eaa", "�Զ���������,size="+view.getCount() );
						myComment.autoAddtoList();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});

	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("album", "mycommentacitvity destory");
		myComment.stopModel();
		myComment = null;
		listAdapter = null;
		super.onDestroy();
	}

	/**
	 * ��ʼ������ģ�ͣ����ö�Model�ļ���
	 */
	private void initModel() {
		myComment = new MyCommentModel(this,"album");
		// ���������ı�
		myComment
				.setmChangeBackgroud(new MyCommentModel.ChangeBackgroudListener() {
					@Override
					public void onBackgroudChanged() {
						// TODO Auto-generated method stub
						updateUI();
					}
				});

		// ������������
		myComment.setmDownComment(new MyCommentModel.DownCommentListener() {

			@Override
			public void onCommentDownload(int msg) {
				// TODO Auto-generated method stub
				if(msg ==0){
					updateUI();
				}else{
					modelTips(msg);
				}
				
				
			}
		});

		//����ɾ�����۵Ľ��
		myComment.setmDeleteComment(new MyCommentModel.DeleteCommentListener() {
			@Override
			public void onCommentDeleted(int msg) {
				// TODO Auto-generated method stub
				switch (msg) {
				case 0: {
					lView.slideBack();
					updateUI();
					Toast.makeText(MyCommentActivity.this,R.string.tips_deletesuccess,
							Toast.LENGTH_SHORT).show();
					break;
				}
				case 1: {
					Toast.makeText(MyCommentActivity.this,R.string.tips_deletefail_dberror,
							Toast.LENGTH_SHORT).show();
					break;
				}
				case 2: {
					Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,
							Toast.LENGTH_SHORT).show();
					break;
				}
				case 3: {
					Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,
							Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					break;
				}
			}
		});

		// ����������������ListBaseAdapter��
	}

	/**
	 * ��ʼ��������������ListView��item��������ļ���
	 */
	private void initAdapter() {
		listAdapter = new ListBaseAdapter2(this, myComment,myComment.getItems(),"album");

		// ��������ͼƬ������������ͼƬ
		listAdapter.setOnBackImageChange(new BackImageListener() {
			
			@Override
			public void backImageClick() {
				// TODO Auto-generated method stub
				changeBackImg();
			}
		});
		//����ɾ����ť
		listAdapter.setDeleCommListener(new ListBaseAdapter2.DeleCommListener() {
			
			@Override
			public void clickDelete(String dateTime, int position) {
				// TODO Auto-generated method stub
				deleteEvent(dateTime, position);
			}
		});
		
	}

	private void initAlbum(){
		PhotoDBHelper phelper = new PhotoDBHelper(this, PhotoDBHelper.DBREAD);
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		String dateTime = Common.currentTime();
		cursor = phelper.selectEvent(null,
				PhotoDBHelper.COLUMNS_UE[10] + "=" + Common.getUserId(this),
				null, null, null, "datetime(" + PhotoDBHelper.COLUMNS_UE[0] + ") desc");
		if(Common.isNetConnected&&(cursor.getCount()==0)){
			myComment.downloadAlbum(dateTime);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// Log.i("Eaa", "onResume");
		super.onResume();
	}

	/**
	 * myCommentActivity�и�������ĵ���¼�
	 * 
	 * @param v
	 *            ��������
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_back: {
			MyCommentActivity.this.finish();
			break;
		}
		case R.id.header_right_btn: {
			// ˢ������
			String dateTime = Common.currentTime();
			myComment.downloadAlbum(dateTime);
			break;
		}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICTURE: {
				Uri uri = data.getData();
				if (!uri.getAuthority().isEmpty()
						|| !TextUtils.isEmpty(uri.getScheme())) {
					// ��ѯѡ��ͼƬ
					Cursor cursor = getContentResolver().query(uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, null, null);
					// ���� û�ҵ�ѡ��ͼƬ
					String selectImage = null;
					if (null != cursor) {
						// ����ƶ�����ͷ ��ȡͼƬ·��
						cursor.moveToFirst();
						selectImage = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
						cursor.close();
					} else {
						selectImage = Uri.decode(uri.getEncodedPath());
					}
					// ��ȡѡ��ͼƬ��BitMap
					try {
					Bitmap selectBack = Common.scaleBitmap(selectImage,
							Common.winWidth, Common.winHeight);
					selectBack = ThumbnailUtils.extractThumbnail(selectBack,
							Common.winWidth, Common.winHeight / 3);
					// ��Bitmap�浽����
					
						FileOutputStream fos = openFileOutput(bgImageName,
								Context.MODE_PRIVATE);
						selectBack
								.compress(Bitmap.CompressFormat.JPEG, 70, fos);
						// Log.d("Eaa", "����ͼƬ��Ӧ�ÿռ�");
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
						e.printStackTrace();
					}

					myComment.changeBackgroud(bgImageName);
				}
				break;
			}
			}
		}
	}

	/**
	 * ����ϵͳ���ѡ��һ����Ƭ�滻����ͼƬ
	 */
	private void changeBackImg() {
		Toast.makeText(MyCommentActivity.this, R.string.addpicture,
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);

		startActivityForResult(intent, REQUEST_PICTURE);
	}

	/**
	 * �����Ի������û�ѡ���Ƿ�ȷ��ɾ��һ������
	 * @param dateTime
	 * @param position
	 */
	private void deleteEvent(final String dateTime, final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(
						getResources().getString(
								R.string.tips_deletedlgmsg_album));
		builder.setPositiveButton(getResources().getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								deleteComment(dateTime, position);
							}
						});
		builder.setNegativeButton(getResources().getString(R.string.cancl),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
		builder.create().show();

	}
	
	/**
	 * ˢ�½���
	 */
	private void updateUI(){
		
		listAdapter.setItems(myComment.getItems());
		listAdapter.notifyDataSetChanged();
	}
	
	
	/**
	 * ����Model�����Ļص�������Toast�����û�
	 */
	private void modelTips(int msg) {
		switch (msg) {
		case 0:
			Toast.makeText(MyCommentActivity.this,R.string.tips_getcommentsuccess,Toast.LENGTH_SHORT).show();
			break;
		case -2:
			Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,Toast.LENGTH_SHORT).show();
			break;
		case -1:
			Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,Toast.LENGTH_SHORT).show();
			break;
		case 1:// ��ȡ���ݲ��ɹ���������
			//Log.i("Eaa", "��ȡ�ƶ�����ʱ��ʾ" + "������æ�����Ժ�����");
			Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,Toast.LENGTH_SHORT).show();
			break;
		case 8:
			//��ѯ����
			Toast.makeText(MyCommentActivity.this,R.string.tips_postfail,Toast.LENGTH_SHORT).show();
			break;
		case 10:// ����ʧ��
			Toast.makeText(MyCommentActivity.this,R.string.tips_netdisconnect,Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}
	

	/**
	 * ֪ͨģ��ɾ��һ������
	 */
	private void deleteComment(String dateTime, int listPosition) {
		myComment.deleteComment(dateTime, listPosition);
	}
	//���ݸ�PostPointOfInterestData��handler1
		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					if(msg.obj!=null){
						String[] poiStr = msg.obj.toString().trim().split("#");
						String[] behaviourStr = poiStr[1].split("[$]");
						String[] durationStr = poiStr[0].split("[$]");
						String[] partnerNumStr = poiStr[2].trim().split("[$]");
						String[] relationStr = poiStr[3].trim().split("[$]");
						behaviourData = new PointOfInterestData();
						durationData = new PointOfInterestData();
						partnerNumData = new PointOfInterestData();
						relationData = new PointOfInterestData();
						//Log.i("poiStr", poiStr[0]);
						helper.delete();
						try {
							for(int i = 0;i<behaviourStr.length;i++){
								behaviourData.setKey(i);
								behaviourData.setValue(behaviourStr[i]);
								helper.insertBehaviour(behaviourData);
							}
							for(int i = 0;i<durationStr.length;i++){
								durationData.setKey(i);
								durationData.setValue(durationStr[i]);
								//�����ݲ��뵽POI���ݿ���
								helper.insertDuration(durationData);
							}
							for(int i = 0;i<partnerNumStr.length;i++){
								partnerNumData.setKey(i);
								partnerNumData.setValue(partnerNumStr[i]);
								helper.insertPartnerNum(partnerNumData);
							}
							for(int i = 0;i<relationStr.length;i++){
								relationData.setKey(i);
								relationData.setValue(relationStr[i]);
								helper.insertPartnerRelation(relationData);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					break;
				case 1:

					break;
				}
			}
		};
		private void initPOI(){
			//�ӷ���������ͣ��ʱ������Ϊ���͡�ͬ����������ϵ��ѡ�������
			PostPointOfInterestData pointOfInterest = new PostPointOfInterestData(handler, URL_GETPOI);
			pointOfInterest.start();
		}
		private void initPOIEN(){
			//Ӣ�İ�
			PostPointOfInterestDataEn pointOfInterestEn = new PostPointOfInterestDataEn(handler, URL_GETPOI);
			pointOfInterestEn.start();
		}
}

