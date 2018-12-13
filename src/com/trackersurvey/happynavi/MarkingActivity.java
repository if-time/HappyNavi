package com.trackersurvey.happynavi;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.TextMoveLayout;
import com.trackersurvey.service.CommentUploadService;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;



/**
 * 7.27 �� ͣ�ã�Ǩ����ShowTraceFragment
 * */
public class MarkingActivity extends Activity implements OnMarkerClickListener, OnInfoWindowClickListener,
		OnMapLoadedListener, OnClickListener, InfoWindowAdapter {
	//title
	private MyLinearLayout back;
	private TextView title;
	private Button titleRightBtn;
	
	private MapView mapView;
	private AMap aMap;
	private Polyline polyline;
	//private List<GpsData> keyPoints = new ArrayList<GpsData>();// ��ʾʱ��Ĺؼ���
	private List<GpsData> arrayTrace = new ArrayList<GpsData>();// �켣��ϸ��Ϣ
	private List<LatLng> arrayLatlng = new ArrayList<LatLng>();//�켣��γ����Ϣ
	private List<LatLng> arrayMarkLatlng = new ArrayList<LatLng>();//��ע��γ����Ϣ
	private List<Long> arrayMillSeconds = new ArrayList<Long>();//��ȡ���Ĺ켣��ʱ����Ϣ
	private LatLng markLatLng = null;//ѡ�����ӱ�ע��λ��
	private long startMills = 0;
	private long endMills = 0;
	private SeekBar seekbar = null;
	private ImageView  mark_left, mark_right;
	private String startTimeStr = "00:00:00";
	private String endTimeStr = "00:00:00";
	private TextView moveText, startTime, endTime;

	/**
	 * �켣��ʱ��
	 */
	private int totalSeconds = 0;
	private int currentProgress = 0;
	//private int nowProgress;
	/**
	 * ��Ļ���
	 */
	private int screenWidth;

	/**
	 * �Զ��������϶���һ���ƶ��Ŀռ�
	 */
	private TextMoveLayout textMoveLayout;

	private ViewGroup.LayoutParams layoutParams;
	/**
	 * �ж������ƶ�����
	 */
	private float moveStep = 0;
	private Marker selectedMarker = null;
	private final int REQUESTMARK = 0x11;
	//private CommentUploadService commentUploadService;
	//private Intent uploadService;
	//private boolean bound_upload=false;
	private  SharedPreferences uploadCache;//�洢���ϴ���������Ϣ
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mark);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		
		
		mapView = (MapView) findViewById(R.id.mark_amapView);
		mapView.onCreate(savedInstanceState);
		initView();
		
	}

	/**
	 * ��ʼ���ؼ�
	 */
	private void initView() {
		if (aMap == null) {
			aMap = mapView.getMap();

		}
		aMap.setOnMapLoadedListener(this);
		
		aMap.setOnMarkerClickListener(this);// ���õ��marker�¼�������
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.addmarker));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setText(getResources().getString(R.string.confirm));
		titleRightBtn.setOnClickListener(this);
		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		moveText = new TextView(this);
		moveText.setBackgroundColor(Color.WHITE);
		moveText.setTextColor(Color.rgb(0, 161, 229));
		moveText.setTextSize(16);
		layoutParams = new ViewGroup.LayoutParams(screenWidth, 50);
		textMoveLayout = (TextMoveLayout) findViewById(R.id.textLayout);
		textMoveLayout.addView(moveText, layoutParams);
		moveText.layout(0, 20, screenWidth, 80);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		startTime = (TextView) findViewById(R.id.start_time);
		endTime = (TextView) findViewById(R.id.end_time);
		mark_left = (ImageView) findViewById(R.id.mark_left);
		mark_right = (ImageView) findViewById(R.id.mark_right);
		mark_left.setOnClickListener(this);
		mark_right.setOnClickListener(this);
		mark_left.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				seekbar.setProgress(currentProgress - 100);
				return true;
			}
		});
		mark_right.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				seekbar.setProgress(currentProgress + 100);
				return true;
			}
		});
		//�󶨱���ϴ�����
		//uploadService = new Intent(MarkingActivity.this,CommentUploadService.class);
		//this.bindService(uploadService,conn_comment, Context.BIND_AUTO_CREATE);
		uploadCache = getSharedPreferences("uploadCache", Activity.MODE_PRIVATE);
		
	}
	/**
	 * ��ʼ���켣
	 */
	private void initTrace(){
		Intent intent = getIntent();
		String jsonTrace = intent.getStringExtra("jsonTrace");
		String jsonLatLng = intent.getStringExtra("jsonLatLng");
		String jsonMarkLatLng = intent.getStringExtra("jsonMarkLatLng");
		String jsonMillsTime = intent.getStringExtra("jsonMillsTime");
		arrayTrace = GsonHelper.parseJsonToList(jsonTrace, GpsData.class);// jsonתarraylist
		arrayLatlng = GsonHelper.parseJsonToList(jsonLatLng, LatLng.class);
		arrayMarkLatlng = GsonHelper.parseJsonToList(jsonMarkLatLng, LatLng.class);
		arrayMillSeconds = GsonHelper.parseJsonToList(jsonMillsTime, Long.class);
		Log.i("mark", "arrayTrace size = "+arrayTrace.size());
		
		if (arrayTrace.size() > 0) {
//			for(int i = 0;i <arrayTrace.size(); i++){
//				LatLng point=new LatLng(arrayTrace.get(i).getLatitude(),arrayTrace.get(i).getLongitude());
//		    	arrayLatlng.add(point);
//		    	arrayMillSeconds.add(praseStrToMillsecond(arrayTrace.get(i).getcreateTime()));
//			}
			AMap_drawpath(arrayLatlng);
			initSeekbar();
		}
	}
	private void initSeekbar() {
		startTimeStr = arrayTrace.get(0).getcreateTime();
		startTime.setText(startTimeStr);
		endTimeStr = arrayTrace.get(arrayTrace.size()-1).getcreateTime();
		endTime.setText(endTimeStr);
		moveText.setText(startTimeStr);
		// totalSeconds = totalSeconds(startTimeStr, endTimeStr);
		long duration=0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{ //����ʱ���
			Date d1 = df.parse(startTimeStr); 
			Date d2 = df.parse(endTimeStr); 
			startMills = d1.getTime();
			endMills = d2.getTime();
			duration = endMills - startMills; 
			totalSeconds = (int) (duration/1000);
		}catch (Exception e){
			totalSeconds = arrayLatlng.size();
		} 
		
		seekbar.setEnabled(true);
		seekbar.setMax(totalSeconds);
		seekbar.setProgress(0);
		moveStep = (float) (((float) screenWidth / (float) totalSeconds) * 0.6);
		/**
		 * setListener
		 */
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
		//���Ĭ�ϵ�ѡ��marker
		MarkerOptions options1 = new MarkerOptions();
		options1.position(arrayLatlng.get(0))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark));
		if(selectedMarker != null){
			selectedMarker.remove();
			selectedMarker = null;
		}
		selectedMarker = aMap.addMarker(options1);
	}
//	private ServiceConnection conn_comment=new ServiceConnection(){
//
//		@Override
//		public void onServiceConnected(ComponentName componentName, IBinder ibinder) {//���ӽ����ɹ�ʱ����
//			// TODO Auto-generated method stub
//			
//			Log.i("upfile", "conn_comment onServiceConnected");
//			CommentBinder binder=(CommentBinder) ibinder;
//			commentUploadService=binder.getService();
//			bound_upload = true;
//			
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName componentName) {
//			// TODO Auto-generated method stub
//			//Log.i("LogDemo", "onServiceDisconnected");
//			bound_upload = false;
//		}};
	// ��·��
	public void AMap_drawpath(List<LatLng> points) {
		Log.i("mark", "points size = " + points.size());
		
		aMap.clear();// �ѵ�ͼ�����б�����
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16));

		PolylineOptions options;
		options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// ��ʼ���켣����
		options.addAll(points);
		polyline = aMap.addPolyline(options);

		aMap.addMarker(new MarkerOptions().position(points.get(0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));// ����������
		aMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));// ����յ�����
		for(int i = 0;i<arrayMarkLatlng.size();i++){
			aMap.addMarker(new MarkerOptions()
					.position(arrayMarkLatlng.get(i))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
					.anchor(0.5f, 0.5f));
		}
		
		// ��������maker��ʾ�ڵ�ǰ���������ͼ��
		// include(points.get(0)).include(points.get(points.size()-1)).build()
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < points.size(); i++) {
			builder.include(points.get(i));
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
	}

	
	/**
	 * ����������д
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
//		if(bound_upload){
//			this.unbindService(conn_comment);
//			bound_upload=false;
//		} 
//		if(null!=uploadService){
//			stopService(uploadService);
//		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.title_back: {
				finish();
				break;
			}
			case R.id.header_right_btn: {
				markLatLng = new LatLng(arrayLatlng.get(praseProgressToPosition(currentProgress)).latitude,
						arrayLatlng.get(praseProgressToPosition(currentProgress)).longitude);
				Intent intent = new Intent();
				intent.setClass(MarkingActivity.this, CommentActivity.class);
				intent.putExtra("longitude",markLatLng.longitude);
				intent.putExtra("latitude",markLatLng.latitude);
				intent.putExtra("altitude", arrayTrace.get(praseProgressToPosition(currentProgress)).getAltitude());
				intent.putExtra("placeName","");
				intent.putExtra("createTime",arrayTrace.get(praseProgressToPosition(currentProgress)).getcreateTime());
				intent.putExtra("traceNo", arrayTrace.get(praseProgressToPosition(currentProgress)).gettraceNo());
				startActivityForResult(intent,REQUESTMARK);
				break;
			}
			case R.id.mark_left:
				seekbar.setProgress(currentProgress - 10);
				break;
			case R.id.mark_right:
				seekbar.setProgress(currentProgress + 10);
				break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// �������Activity���صĽ�������ݸý������ͼƬ�ϴ�
			case REQUESTMARK:
				arrayMarkLatlng.add(markLatLng);
				aMap.addMarker(new MarkerOptions()
						.position(markLatLng)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker))
						.anchor(0.5f, 0.5f));
				Log.i("mark", "upmark");
				// �����wifi���磬ֱ�ӿ�ʼ�ϴ�
				if (Common.checkNetworkState(getApplicationContext()) == ConnectivityManager.TYPE_WIFI) {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.tips_uploadinbgbegin),
							Toast.LENGTH_SHORT).show();
					String commentTime = data.getStringExtra("createTime");
					Intent intent = new Intent(MarkingActivity.this,CommentUploadService.class);
					intent.putExtra("createTime", commentTime);
					startService(intent);
					//commentUploadService.uploadComment(Common.getUserId(getApplicationContext()),
					//		commentTime);
				}// ����������������ӣ���һ��ʹ��ѯ���Ƿ��ϴ�
				else if (Common.checkNetworkState(getApplicationContext()) == ConnectivityManager.TYPE_MOBILE
						&& !Common.isOnlyWifiUploadPic(MarkingActivity.this)) {
					SharedPreferences sp = getSharedPreferences("config",
							MODE_PRIVATE);
					if (sp.getInt(LoginActivity.mobConnectFirst, 0) == 0) {
						Editor edit = sp.edit();
						edit.putInt(LoginActivity.mobConnectFirst, 1);
						edit.commit();
						CustomDialog.Builder builder = new CustomDialog.Builder(this);
						builder.setMessage(getResources().getString(
								R.string.mobconnect_tips));
						builder.setTitle(getResources().getString(R.string.tip));
						builder.setPositiveButton(
								getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String commentTime = data
												.getStringExtra("createTime");
										Intent intent = new Intent(MarkingActivity.this,CommentUploadService.class);
										intent.putExtra("createTime", commentTime);
										startService(intent);
//										commentUploadService.uploadComment(
//												Common.getUserId(getApplicationContext()),
//												commentTime);
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(
								getResources().getString(R.string.cancl),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String commentTime = data
												.getStringExtra("createTime");
										String userId = Common
												.getUserId(getApplicationContext());
										SharedPreferences.Editor editor = uploadCache
												.edit();
										editor.putString(commentTime, userId);
										editor.commit();
										dialog.dismiss();
									}
								});
						builder.create().show();
					}else{
						String commentTime = data
								.getStringExtra("createTime");
						Intent intent = new Intent(MarkingActivity.this,CommentUploadService.class);
						intent.putExtra("createTime", commentTime);
						startService(intent);
						//commentUploadService.uploadComment(
						//		Common.getUserId(getApplicationContext()),
						//		commentTime);
					}
				} else {
					// ���û�����磬���ϴ������Ϣд��cache�ļ�
					Toast.makeText(this,getResources().getString(R.string.tips_uploadpic_nonet),
							Toast.LENGTH_SHORT).show();
					String commentTime = data.getStringExtra("createTime");
					String userId = Common.getUserId(getApplicationContext());
					SharedPreferences.Editor editor = uploadCache.edit();
					editor.putString(commentTime, userId);
					editor.commit();
				}
				break;
			}
		}
	}
	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
		initTrace();
		//initSeekbar();

	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	

	private class OnSeekBarChangeListenerImp implements SeekBar.OnSeekBarChangeListener {

		

		// �����������϶�
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			currentProgress = progress;
			moveText.layout((int) (progress * moveStep), 20, screenWidth, 80);
			moveText.setText(praseProgressToStr(progress));
			MarkerOptions options1 = new MarkerOptions();
			
			if(progress>=0){
				options1.position(arrayLatlng.get(praseProgressToPosition(progress)))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark));
				if(selectedMarker != null){
					selectedMarker.remove();
					selectedMarker = null;
				}
				selectedMarker = aMap.addMarker(options1);
				
			}
	
		}

		// ��ʾ�������տ�ʼ�϶�����ʼ�϶�ʱ�򴥷��Ĳ���
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		// ֹͣ�϶�ʱ��
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * ��������ʱ��֮�������
	 */

	private  int totalSeconds(String startTime, String endTime) {

		String[] st = startTime.split(":");
		String[] et = endTime.split(":");

		int st_h = Integer.valueOf(st[0]);
		int st_m = Integer.valueOf(st[1]);
		int st_s = Integer.valueOf(st[2]);

		int et_h = Integer.valueOf(et[0]);
		int et_m = Integer.valueOf(et[1]);
		int et_s = Integer.valueOf(et[2]);

		int totalSeconds = (et_h - st_h) * 3600 + (et_m - st_m) * 60 + (et_s - st_s);

		return totalSeconds;

	}

	/**
	 * ���ݵ�ǰѡ���������ԭʱ���
	 * 
	 * @param args
	 */

	private String getCheckTimeBySeconds(int progress, String startTime) {

		String return_h = "", return_m = "", return_s = "";

		String[] st = startTime.split(":");// �ָ��ַ���
		int st_h = 0;
		int st_m = 0;
		int st_s = 0;
		try{
			st_h = Integer.valueOf(st[0]);// ����ʼʱ���ַ����ĵ�1��ð��ǰ�Ĳ��ִ�ת��Ϊint����ֵ
			st_m = Integer.valueOf(st[1]);
			st_s = Integer.valueOf(st[2]);
		}catch(NumberFormatException e ){
			return "error";
		}
		int h = progress / 3600; // ���ӵ�Сʱλ�õ���

		int m = (progress % 3600) / 60; // ���ӵķ���λ�õ���

		int s = progress % 60; // ���ӵ���λ�õ���

		if ((s + st_s) >= 60) {
			// ��ʼʱ��������ʱ�����λ�õ���>=60

			int tmpSecond = (s + st_s) % 60;

			m = m + 1;

			if (tmpSecond >= 10) {
				// ʣ�����λ�õ���>=10
				return_s = tmpSecond + "";
			} else {
				// ʣ�����λ�õ���<10
				return_s = "0" + (tmpSecond);
			}

		} else {
			if ((s + st_s) >= 10) {
				return_s = s + st_s + "";
			} else {
				return_s = "0" + (s + st_s);
			}

		}

		if ((m + st_m) >= 60) {

			int tmpMin = (m + st_m) % 60;

			h = h + 1;

			if (tmpMin >= 10) {
				return_m = tmpMin + "";
			} else {
				return_m = "0" + (tmpMin);
			}

		} else {
			if ((m + st_m) >= 10) {
				return_m = (m + st_m) + "";
			} else {
				return_m = "0" + (m + st_m);
			}

		}

		if ((st_h + h) < 10) {
			return_h = "0" + (st_h + h);
		} else {
			return_h = st_h + h + "";
		}

		return return_h + ":" + return_m + ":" + return_s;
	}
	private long praseStrToMillsecond(String time){
		long duration=0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{ //����ʱ��
			Date d1 = df.parse(time); 
			duration = d1.getTime();
		}catch (Exception e){
			
		}
		return duration;
	}
	private String praseProgressToStr(int progress){
		long duration=0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{ //����ʱ���
			 
			
			duration = startMills+progress*1000;
			
			Date d2 = new Date(duration);
			return df.format(d2);
		}catch (Exception e){
			return "error";
		} 
		
	}
	private int praseProgressToPosition(int progress){
		int position = 0;
		long currentMills = startMills+(progress)*1000;
		
		for(int i = 0;i<arrayMillSeconds.size();i++){
			long mills = arrayMillSeconds.get(i);
			
			if(mills >= currentMills && mills > 0){
				position = i;
				break;
			}
		}
		//Log.i("mark","progress = "+progress+ ",current pos = "+position);
		return position;
	}
}
