package com.trackersurvey.happynavi;



import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.amap.api.maps.model.UrlTileProvider;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.trackersurvey.adapter.DialogAdapter;
import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.db.PointOfInterestDBHelper;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.FileInfo;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.PointOfInterestData;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ShareToWeChat;
import com.trackersurvey.helper.SportTypeDialog;
import com.trackersurvey.helper.StepDetector;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostCheckVersion;
import com.trackersurvey.httpconnection.PostEndTrail;
import com.trackersurvey.httpconnection.PostPointOfInterestData;
import com.trackersurvey.httpconnection.PostPointOfInterestDataCz;
import com.trackersurvey.httpconnection.PostPointOfInterestDataEn;
import com.trackersurvey.service.CommentUploadService;
import com.trackersurvey.service.DownloadService;
import com.trackersurvey.service.GpsTrace;
import com.trackersurvey.service.GpsTrace.LocalBinder;
import com.trackersurvey.service.StepCounterService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements LocationSource,
AMapLocationListener,OnGeocodeSearchListener,
 OnClickListener, OnMapClickListener, OnMarkerClickListener{
	private TextView text_step, text_location, text_calculate;
	private ImageButton layer;
	private ImageButton startTrail;
	private ImageButton pauseTrail;
	private ImageButton endTrail;
	private ImageButton takephoto;
	private ImageButton toolbox;
	private ImageButton returnToMap;
	private ProgressDialog proDialog = null;
	
	private AMap aMap;
	private MapView mapView;
	private Marker interestpoint;          //响应用户点击的位置
	private Marker startMarker=null;            //起点标记
	private Marker endMarker=null;				//终点标记
	private Polyline polyline=null;				//轨迹连线
	private PolylineOptions options;		//轨迹线属性
	
	private Polyline polyline2 = null;		//手动画线对象（2017-7-16）
	private PolylineOptions options2;		//手动画线设置对象
	
	private Polygon polyGon = null;			//手动画多边形对象（2017-7-18）
	private PolygonOptions polyGonOptions;	//手动画多边形设置对象
	
	
	private OnLocationChangedListener mListener;
	
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	
	private GeocodeSearch geocoderSearch;
	private LatLonPoint latLonPoint;       		 //用户点击的位置的经纬度
	private LatLng finalLatLng=new LatLng(0,0);  //轨迹终点
	private String addressName;	
	private LatLng currentLatLng;				 //定位到的当前位置
	private AMapLocation currentAlocation;
	private List<LatLng> points;				 //轨迹点的成员
	//private TimeValue timevalues=null;		 //service时间参数
	
	
	private boolean isstart=false;
	private boolean ispause=false;
	private boolean isend=true;
	private boolean bound_trace=false;
	//private boolean bound_upload=false;
	private boolean istimeset=false;
	private boolean iscountstep=false;
	private boolean isShowNonLocDlg = false; //无法定位对话框是否正在显示
	
	public  static final int MENU_ROUTE=0;
	public  static final int MENU_NAVI=1;
	private int maptype=0;
	//String[] title=new String[]{"划船","爬山","骑行","跑步","滑雪","轮滑","游泳","徒步","散步","仅记录轨迹","取消"};
	private long traceNo=0;
	private int total_step = 0;   //走的总步数
	private TraceData tracedata=new TraceData();
	private StepData stepdata=new StepData();
	private List<GpsData> tracegps=new ArrayList<GpsData>();
	private MyBroadcastReciver myreveiver=null;//用于接收后台发送的定位广播
	private AccuracyBroadcastReciver accuracyReciver=null;
	private BroadcastReceiver connectionReceiver = null; // 用于监听网络状态变化的广播
	
	private GpsTrace traceService;
	//private CommentUploadService commentUploadService;
	private Intent locService;
	private Intent commentService;
	private Intent stepService ;
	private Intent updateService;
	private Thread stepThread;
	private TraceDBHelper helper;
	private SharedPreferences sp;  //存储基本配置信息 如账号、密码
	private  SharedPreferences uploadCache;//存储待上传的评论信息
	//private FileInfo fileInfo = null;//新版apk文件
	
	private  final String MY_ACTION="android.intent.action.LOCATION_RECEIVER";
	private  final String PULLREFRESH_ACTION="android.intent.action.PULLREFRESH_RECEIVER";
	private  final String ACCURACY_ACTION="android.intent.action.ACCURACY_RECEIVER";
	//private static final String URL_STARTTRAIL=Common.url+"reqTraceNo.aspx";
	private  String URL_ENDTRAIL=null;
	//private static final String URL_GET4TIME=Common.url+"request.aspx";
	private  String URL_CHECKUPDATE=null;
	private String URL_GETPOI = null;
	public   final int REQUSET_COMMENT = 1;
	private PointOfInterestData behaviourData,durationData, partnerNumData, relationData;
	private PointOfInterestDBHelper helper2 = null;
	private Cursor cursor;
	private int checkedItem = 0;
	private double currentLongitude, currentLatitude, currentAltitude, currentLongitude1, currentLatitude1, currentAltitude1;
	private String[] degreeLngArr, minuteLngArr, secondLngArr, degreeLatArr, minuteLatArr, secondLatArr;
	private String degreeLngStr, minuteLngStr, secondLngStr, degreeLatStr, minuteLatStr, secondLatStr, currentAltitudeStr;
	private double minuteLng1, secondLng1, minuteLat1, secondLat1;
	private PopupWindow mPopupWindow;
	
	private Marker marker;					//手动画线的标注点对象（2017-7-16）
	private MarkerOptions markerOption;		//手动画线的标注点设置对象
	private List<Marker> markList;			//手动画线的标注点对象集合（在onCreat方法中初始化）
	private List<LatLng> points2;			//手动画线的经纬度点集合（在onCreat方法中初始化）
	private List<Polyline> polylineList;	//手动画线的画线对象集合（在onCreat方法中初始化）
	private List<Polygon> polyGonList;		//手动画多边形的对象集合（在onCreat方法中初始化）（2017-7-18）
	private boolean distanceMode = false;	//测距离模式
	private boolean areaMode = false;		//测面积模式
	private float distance;
	private float totalDistance;			//总距离
	private float area;
	private float totalArea;				//总面积
	//private ImageButton deletePoint;		//删除当前测量点
	
	private RelativeLayout showDistance;	//测量模式界面
	private RelativeLayout showTips;
	private TileProvider tileProvider;		//瓦片提供者，用于转换地图图层
	private TileOverlayOptions tileOverlayOptions;
	private TileOverlay tileOverlay;
	
	private ListView dialogList;
	private ArrayList<HashMap<String,Object>> dialogListItem;
	//private LinearLayout dialogLayout;
	private View view;
	private AlertDialog dialog;
	/**
	 * BDMAP
	 
	// 是否显示bdmap
		boolean openBaidu = false;
		// 声明baidumap控件 百度
		public com.baidu.mapapi.map.MapView bdMapView = null;
		public BaiduMap mBaiduMap;
		private BDMap bdmap;
	private Context bdcontext;
	* */	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getWindow().setFormat(PixelFormat.TRANSLUCENT);//据说能解决黑屏,试了没效果
		super.onCreate(savedInstanceState);
		/**bdcontext=this.getApplicationContext();
		SDKInitializer.initialize(bdcontext);*/
		
		setContentView(R.layout.activity_main);
		AppManager.getAppManager().addActivity(this);
		ShareToWeChat.registToWeChat(getApplicationContext() );
		
		text_step=(TextView)findViewById(R.id.tv_step);
		text_step.setVisibility(View.INVISIBLE);
		//text_longitude = (TextView) findViewById(R.id.tv_longitude);
		//text_latitude = (TextView) findViewById(R.id.tv_latitude);
		text_location = (TextView) findViewById(R.id.tv_location);
		text_calculate = (TextView) findViewById(R.id.tv_distance);
		layer=(ImageButton) findViewById(R.id.imgbtn_layer);
		toolbox = (ImageButton) findViewById(R.id.imgbtn_toolbox);
		returnToMap = (ImageButton) findViewById(R.id.iv_return);
		//deletePoint = (ImageButton) findViewById(R.id.iv_delete);
		showDistance = (RelativeLayout) findViewById(R.id.calculate_distance);
		showTips = (RelativeLayout) findViewById(R.id.calculate_tips);
		startTrail=(ImageButton) findViewById(R.id.imgbtn_starttrail);
		pauseTrail=(ImageButton) findViewById(R.id.imgbtn_pausetrail);
		endTrail=(ImageButton) findViewById(R.id.imgbtn_endtrail);
		takephoto=(ImageButton) findViewById(R.id.imgbtn_takephoto);
		showDistance.setVisibility(View.INVISIBLE);
		showTips.setVisibility(View.INVISIBLE);
		startTrail.setVisibility(View.VISIBLE);
		pauseTrail.setVisibility(View.INVISIBLE);
		endTrail.setVisibility(View.INVISIBLE);
//		takephoto.setVisibility(View.INVISIBLE);
		layer.setOnClickListener(this);
		toolbox.setOnClickListener(this);
		startTrail.setOnClickListener(this);
		pauseTrail.setOnClickListener(this);
		endTrail.setOnClickListener(this);
		takephoto.setOnClickListener(this);
		returnToMap.setOnClickListener(this);
		//deletePoint.setOnClickListener(this);
		/**bdlocation=(ImageButton) findViewById(R.id.bdlocaion);
		bdlocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bdmap.setBDCenter();
			}
		});*/
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		
		points2 = new ArrayList<LatLng>();			//手动画线的经纬度点的集合初始化（2017-7-16）
		markList = new ArrayList<Marker>();			//手动画线的标注点的集合初始化
		polylineList = new ArrayList<Polyline>();	//手动画线的对象的集合初始化
		polyGonList = new ArrayList<Polygon>();		//手动画多边形的对象集合初始化
		
		//text_distance.setText("总距离：" + String.valueOf(totalDistance) + "m");
		
		helper=new TraceDBHelper(this);
		helper2 = new PointOfInterestDBHelper(this);//创建POI数据库
		// sp 初始化 
        sp=getSharedPreferences("config",MODE_PRIVATE);//私有参数
		initAmap();
		
		//initBDMap();
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		URL_ENDTRAIL=Common.url+"reqTraceNo.aspx";
		URL_CHECKUPDATE=Common.url+"request.aspx";
		URL_GETPOI = Common.url + "requestInfo.aspx";
		int l = TabHost_Main.l;
		if(l==0){			
			initPOI();//下载添加兴趣点下拉列表选项内容
		}
		if(l==1){
			initPOIEN();//英文版
		}
		if(l==2) {
			initPOICZ();// 捷克语版
		}
//		if(!polylineList.isEmpty()){
//			for(int i = 0;i<polylineList.size();i++){
//				distance = AMapUtils.calculateLineDistance(polylineList.get(polylineList.size()-1), finalLatLng);
//			}
//		}
	}
	// 初始化百度图层
	/**
			public void initBDMap() {
				// 百度地图
				bdMapView = (com.baidu.mapapi.map.MapView) findViewById(R.id.bmapmainView);
				mBaiduMap = bdMapView.getMap();
				
				bdMapView.setVisibility(View.GONE);
				// 开启定位图层
				//mBaiduMap.setMyLocationEnabled(true);// ！！！！
				int childCount = bdMapView.getChildCount();
				View zoom = null;
				for (int i = 0; i < childCount; i++) {
					View child = bdMapView.getChildAt(i);
					if (child instanceof ZoomControls) {
						zoom = child;
						break;
					}
				}
				zoom.setVisibility(View.GONE);//隐藏放大缩小按钮
				bdmap=new BDMap(mBaiduMap,bdMapView,bdcontext);
				//bdmap.start();
			}*/
	/**
	* 初始化AMap对象
	*/
	private void initAmap() {
		//获取屏幕分辨率
		//SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		Common.winWidth = sp.getInt(LoginActivity.winWidth, 720);
		Common.winHeight = sp.getInt(LoginActivity.winHeight, 1280);
		Common.ppiScale = sp.getFloat(LoginActivity.PPISCALE, 1.5f);
		//创建相册文件夹
		Common.createFileDir();
		// 定义网络广播监听
		connectionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				
				NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
				if(netInfo!=null && netInfo.isConnected()){
					//有网络连接
					if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
						//wifi连接
						/**后期设置中加入“wifi状态下自动更新”选项，此处加入bool量判断是否自动检查
						 **/
						Log.i("phonelog", "当前WiFi连接");
						Common.isWiFiConnected=true;
						if(Common.isUpdationg&&Common.fileInfo!=null){
							Log.i("phonelog", "WiFi下继续下载");
							// 通知Service继续下载
							updateService = new Intent(MainActivity.this, DownloadService.class);
							updateService.setAction(DownloadService.ACTION_START);
							updateService.putExtra("fileInfo", Common.fileInfo);
							startService(updateService);
						}else{
							if(Common.isAutoUpdate(getApplicationContext())){
								Log.i("phonelog", "wifi下检查更新");
								String version = null;
								if(Common.version == null ||Common.version.equals("")){
									version = Common.getAppVersionName(getApplicationContext());
								}else{
									version = Common.version;
								}
								if(version !=null && !version.equals("")){
									PostCheckVersion checkversion=new PostCheckVersion(updatehandler, URL_CHECKUPDATE,
											Common.getDeviceId(getApplicationContext()),version);
									checkversion.start();
								}
							}
							else{
								Log.i("phonelog", "自动检查更新关闭");
							}
						}
						
						
					}
					else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
						// connect network,读取本地sharedPreferences文件，上传之前未完成上传的部分
						Log.i("phonelog", "当前GPRS数据连接");
						Common.isWiFiConnected=false;
						Log.i("phonelog", "WiFi连接断开");

						
						if (Common.isUpdationg&&Common.fileInfo!=null) {
							// 通知Service暂停下载
							updateService = new Intent(MainActivity.this, DownloadService.class);
							updateService.setAction(DownloadService.ACTION_STOP);
							updateService.putExtra("fileInfo", Common.fileInfo);
							startService(updateService);
							Log.i("phonelog", "发送暂停命令");
						}
					}else{
						Common.isWiFiConnected=false;
						Log.i("phonelog", "WiFi连接断开");
					}
				}else{
					//无网络连接
					Log.i("phonelog", "Main，当前无网络");
					Common.isWiFiConnected=false;
					
					if(Common.isUpdationg&&Common.fileInfo!=null){
						// 通知Service暂停下载
						updateService = new Intent(MainActivity.this, DownloadService.class);
						updateService.setAction(DownloadService.ACTION_STOP);
						updateService.putExtra("fileInfo", Common.fileInfo);
						startService(updateService);
						Log.i("phonelog", "发送暂停命令");
					}
					if(!Common.checkGPS(getApplicationContext())){
						//网络没开，gps没开，无法定位，提示
						if(!isShowNonLocDlg){
							isShowNonLocDlg=true;
							boolean isShowBadLoc = sp.getBoolean("isShowBadLoc", true);
							if(isShowBadLoc){
								Log.i("phonelog", "网络没开，gps没开，无法定位，提示");
								
								showDlg_badloc();
							}
						}
					}
				}
				
			}
		};
		
		//注册监听网络连接状态广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		MainActivity.this.registerReceiver(connectionReceiver, intentFilter);
		uploadCache = getSharedPreferences("uploadCache", Activity.MODE_PRIVATE);
		
		
		if (aMap == null) {
			aMap = mapView.getMap();
			geocoderSearch = new GeocodeSearch(this);
			geocoderSearch.setOnGeocodeSearchListener(this);
			registerListener();
			setUpMap();
			if(!Common.isVisiter()){
			setUpService();
			}
		}
//		Spinner spinner = (Spinner) findViewById(R.id.layers_spinner);
//		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//				this, R.array.layers_array,
//				android.R.layout.simple_spinner_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		spinner.setAdapter(adapter);
//		spinner.setOnItemSelectedListener(this);
	}
	/**
	 * 注册监听
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(MainActivity.this);
		aMap.setOnMarkerClickListener(MainActivity.this);
		//aMap.setOnInfoWindowClickListener(MainActivity.this);
		//aMap.setInfoWindowAdapter(MainActivity.this);
	}
	/**
	* 设置一些amap的属性
	*/
	private void setUpMap() {
		
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 255, 255, 255));// 设置圆形的填充颜色
		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setCompassEnabled(true);  //启用罗盘
		aMap.getUiSettings().setScaleControlsEnabled(true);//启用比例尺
		aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//设置缩放按钮在右侧中间位置
		//aMap.getUiSettings().setZoomGesturesEnabled(false);//屏蔽双击放大地图操作
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
		double lastLongitude = sp.getFloat("lastLongitude", 0);
		double lastLatitude = sp.getFloat("lastLatitude", 0);
		if(lastLongitude>0 && lastLatitude>0){
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lastLatitude,lastLongitude)));
		}
		points =  new ArrayList<LatLng>();//创建位置列表
		
		interestpoint=aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
	}
	/**
	* 设置service的参数
	*/
	public void setUpService(){
		//注册监听后台定位情况的广播，用于更新轨迹显示
		myreveiver=new MyBroadcastReciver();//生成一个MyBroadcastReciver对象
		IntentFilter filter=new IntentFilter();//生成一个IntentFilter对象
		filter.addAction(MY_ACTION);//为IntentFilter添加Action
		MainActivity.this.registerReceiver(myreveiver, filter);//注册广播
		//注册精度监听广播
		accuracyReciver = new AccuracyBroadcastReciver();
		IntentFilter accuracyfilter=new IntentFilter();//生成一个IntentFilter对象
		accuracyfilter.addAction(ACCURACY_ACTION);//为IntentFilter添加Action
		MainActivity.this.registerReceiver(accuracyReciver, accuracyfilter);//注册广播
		
		locService= new Intent(MainActivity.this,  GpsTrace.class);//=new Intent(MainActivity.this, GpsTrace.class)
		commentService = new Intent(MainActivity.this,CommentUploadService.class);
		startService(commentService);
		//locService.putExtra("userId", Common.userId);
		MainActivity.this.getApplicationContext().bindService(locService,
				conn, Context.BIND_AUTO_CREATE);
		//MainActivity.this.getApplicationContext().bindService(uploadService,
		//		conn_comment, Context.BIND_AUTO_CREATE);
		stepService= new Intent(MainActivity.this, StepCounterService.class);
		/*
		 * MainActivity.this.getApplicationContext().
		 * 在TabActivy的TabHost中的Activity如果需要bindService的话
		 * ，需要先调用getApplicationContext()获取其所属的Activity的上下文环境才能正常bindService
		 */
		/**
		PostTimeValues gettime = new PostTimeValues(handler, URL_GET4TIME,
				Common.getUserId(getApplicationContext()));
		gettime.start();
		*/
		if (stepThread == null) {

			stepThread = new Thread() {// 子线程用于监听当前步数的变化

				@Override
				public void run() {
					super.run();
					
					while (iscountstep) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (StepCounterService.FLAG) {
							Message msg = Message.obtain();
							
							msg.what=9;
							handler.sendMessage(msg);// 通知主线程
						}
					}
				}
			};
			
		}
		//startService(locService);
		/**
		 判断上次轨迹记录是否意外中断，如果有以外中断，提醒用户是否继续记录
		 */
		
		traceNo=helper.getUnStopStatusExists(Common.getUserId(this));
		if(traceNo!=0){//存在中断的轨迹,0是轨迹号
			CustomDialog.Builder builder = new CustomDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.tip));
			builder.setMessage(getResources().getString(R.string.tips_interrupttrace_msg));
			builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					helper.updateStatus( traceNo, 2,Common.getUserId(MainActivity.this));
					
					dialog.dismiss();
				}
			});
			builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					tracedata=helper.queryfromTrailbytraceNo(traceNo,Common.getUserId(MainActivity.this));
					initStartInfo();
					
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
		else{//没有中断的轨迹
			
		}
		boolean isShowBGRGuide = sp.getBoolean("isShowBGRGuide", true);
		if(isShowBGRGuide){
			//指引用户如何添加白名单
			showBGRunGuide();
		}
	}
	/**
	public void initTime(){
		if(timevalues==null){
			//traceService.getToWork();//临时加的，要删
		}
		else{
			istimeset=true;
			traceService.changeLastTime(timevalues.getLastTime());
			traceService.changeGpsTime(timevalues.getNoRecTime());
			traceService.changeUploadTime(timevalues.getUploadTime());
			if(!traceService.isworking()){
				traceService.getToWork();
			}
		}
	}
	*/
	
	private ServiceConnection conn=new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder ibinder) {//连接建立成功时调用
			
			Log.i("LogDemo", "onServiceConnected");
			LocalBinder binder=(LocalBinder) ibinder;
			traceService=binder.getService();
			
			bound_trace=true;
			/**
			initTime();
			*/
			if(!traceService.isWorking()){
				traceService.getToWork();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			//Log.i("LogDemo", "onServiceDisconnected");
			bound_trace=false;
		}
	};
//		private ServiceConnection conn_comment=new ServiceConnection(){
//
//			@Override
//			public void onServiceConnected(ComponentName componentName, IBinder ibinder) {//连接建立成功时调用
//				
//				Log.i("upfile", "conn_comment onServiceConnected");
//				CommentBinder binder=(CommentBinder) ibinder;
//				commentUploadService=binder.getService();
//				bound_upload = true;
//				
//			}
//
//			@Override
//			public void onServiceDisconnected(ComponentName componentName) {
//				//Log.i("LogDemo", "onServiceDisconnected");
//				bound_upload = false;
//			}};
	public class AccuracyBroadcastReciver extends BroadcastReceiver {
			 
			public AccuracyBroadcastReciver(){
				
			}
			@Override
			public void onReceive(Context context, Intent intent) {
				Common.isHighAccuracy=false;
				ToastUtil.show(MainActivity.this, getResources().getString(R.string.tips_accuracy_msg));
			}
	}
	public class MyBroadcastReciver extends BroadcastReceiver {
		 
		public MyBroadcastReciver(){
			
		}
		@Override
		public void onReceive(Context context, Intent intent) {
//			if(Common.getUserId(getApplicationContext()).length()<10){
//				Intent mintent=new Intent();
//				mintent.setClass(MainActivity.this, LoginActivity.class);
//				mintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(mintent);
//				finish();
//			}
			if (mListener!=null&&Common.aLocation != null&&Common.aLocation.getErrorCode()==0) {
				
				mListener.onLocationChanged(Common.aLocation);// 显示系统小蓝点
				//Log.i("trailadapter", "main----onLocationChanged");
				//currentAlocation=aLocation;
				currentLatLng=new LatLng(Common.aLocation.getLatitude(),Common.aLocation.getLongitude());
				
/*--------------------------------------------author:zhanghao 2017-7-------------------------------------------------*/
				//在地图页面显示当前位置的经纬度信息
				currentLongitude = Common.aLocation.getLongitude();	//经度
				currentLatitude = Common.aLocation.getLatitude();	//纬度
				currentAltitude = Common.aLocation.getAltitude();	//海拔
				
				currentAltitudeStr = String.valueOf(currentAltitude);
				BigDecimal currentLongitudeTemp = new BigDecimal(currentLongitude);
				currentLongitude1 = currentLongitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				BigDecimal currentLatitudeTemp = new BigDecimal(currentLatitude);
				currentLatitude1 = currentLatitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				BigDecimal currentAltitudeTemp = new BigDecimal(currentAltitude);
				currentAltitude1 = currentAltitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				degreeLngArr = String.valueOf(currentLongitude).split("[.]");	//换算经度
				degreeLngStr = degreeLngArr[0];									//度
				minuteLng1 = Double.parseDouble("0."+degreeLngArr[1])*60;
				minuteLngArr = String.valueOf(minuteLng1).split("[.]");
				minuteLngStr = minuteLngArr[0];									//分
				secondLng1 = Double.parseDouble("0."+minuteLngArr[1])*60;
				secondLngArr = String.valueOf(secondLng1).split("[.]");
				secondLngStr = secondLngArr[0];									//秒
				
				degreeLatArr = String.valueOf(currentLatitude).split("[.]");	//换算纬度
				degreeLatStr = degreeLatArr[0];									//度
				minuteLat1 = Double.parseDouble("0."+degreeLatArr[1])*60;
				minuteLatArr = String.valueOf(minuteLat1).split("[.]");
				minuteLatStr = minuteLatArr[0];									//分
				secondLat1 = Double.parseDouble("0."+minuteLatArr[1])*60;
				secondLatArr = String.valueOf(secondLat1).split("[.]");
				secondLatStr = secondLatArr[0];									//秒
				//显示当前位置的经纬度
				/*if(currentLongitude > 0){
					text_longitude.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "E"+"；");
				}else if(currentLongitude < 0){
					text_longitude.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "W"+"；");
				}
				if(currentLatitude > 0){
					text_latitude.setText("纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "N");
				}else if(currentLatitude < 0){
					text_latitude.setText("纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "S");
				}*/
				/*if(currentLongitude > 0 && currentLatitude > 0){
					text_location.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "E；"
							+"纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "N");
				}
				if(currentLongitude > 0 && currentLatitude < 0){
					text_location.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "E；"
							+"纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "S");
				}
				if(currentLatitude < 0 && currentLatitude > 0){
					text_location.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "W；"
							+"纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "N");
				}
				if(currentLatitude < 0 && currentLatitude < 0){
					text_location.setText("经度："+degreeLngStr + "°" + minuteLngStr + "′" + secondLngStr + "″" + "W；"
							+"纬度："+degreeLatStr + "°" + minuteLatStr + "′" + secondLatStr + "″" + "S");
				}*/
				if(currentAltitude>0){					
					if(currentLongitude > 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E；"+
					getResources().getString(R.string.latitude)+currentLatitude1+"N；"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLongitude > 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E；"+
					getResources().getString(R.string.latitude)+currentLatitude1+"S；"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLatitude < 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W；"+
					getResources().getString(R.string.latitude)+currentLatitude1+"N；"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLatitude < 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W；"+
					getResources().getString(R.string.latitude)+currentLatitude1+"S；"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
				}else{
					if(currentLongitude > 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E；"
					+getResources().getString(R.string.latitude)+currentLatitude1+"N");
					}
					if(currentLongitude > 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E；"
					+getResources().getString(R.string.latitude)+currentLatitude1+"S");
					}
					if(currentLatitude < 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W；"
					+getResources().getString(R.string.latitude)+currentLatitude1+"N");
					}
					if(currentLatitude < 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W；"
					+getResources().getString(R.string.latitude)+currentLatitude1+"S");
					}
				}
/*-------------------------------------------------------------------------------------------------------------------*/
				Editor editor = sp.edit();
		        editor.putFloat("lastLongitude", (float) currentLatLng.longitude);
		        editor.putFloat("lastLatitude", (float) currentLatLng.latitude);
		        
		        editor.commit();
				if(Common.isRecording){
					aMap.moveCamera(CameraUpdateFactory.changeLatLng(currentLatLng));
					if(isstart&&isend){//开启新的轨迹记录
						//aMap.clear();
						//clearTrace();
						
						isend=false;
						
						//Log.i("LogDemo", "开画");
					}
					if(refreshTrace()){
						if(!points.isEmpty()){
							points.clear();
						}
						for(int i=0;i<tracegps.size();i++){
							LatLng point = new LatLng(tracegps.get(i).getLatitude(),tracegps.get(i).getLongitude());
							points.add(point);
							if(points.size() >1){
								if(point.equals(points.get(points.size() -2))){//新记录的点与上一个点相同，删除
										points.remove(points.size() -1);
										//Log.i("LogDemo", "removepoint");
								}	
								finalLatLng=points.get(points.size() -1);
							}
						}
						tracegps.clear();
						drawPoints(points);//高德绘制轨迹
						//bdmap.drowTrace(points,0);//百度绘制轨迹,不标记终点
					}			
				}
			}
	
		}
	}
	// 地图测量和路径规划对话框
	private void initDialogData(){
		
		dialogListItem = new ArrayList<HashMap<String,Object>>();
//		int[] iconArray = new int[] {R.drawable.cal_dis, R.drawable.cal_area, R.drawable.pathplanning};
		int[] iconArray = new int[] {R.drawable.cal_dis, R.drawable.cal_area};
		String[] textArray = getResources().getStringArray(R.array.toolpopmenu);
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("icon", iconArray[0]);
		map.put("text", textArray[0]);
		dialogListItem.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", iconArray[1]);
		map.put("text", textArray[1]);
		dialogListItem.add(map);
//		map = new HashMap<String, Object>();
//		map.put("icon", iconArray[2]);
//		map.put("text", textArray[2]);
//		dialogListItem.add(map);
	}
	// 地图测量和路径规划对话框设置
	private void setupDialogView(){
		LayoutInflater inflater = LayoutInflater.from(this);
		//dialogLayout = (LinearLayout) inflater.inflate(R.layout.dialog_tools, null);
		view = inflater.inflate(R.layout.dialog_tools, null);
		dialogList = (ListView) view.findViewById(R.id.dialog_list);
		
		DialogAdapter adapter = new DialogAdapter(this, dialogListItem);
		dialogList.setAdapter(adapter);
		dialogList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					dialog.dismiss();
					showDistance.setVisibility(View.VISIBLE);
					showTips.setVisibility(View.VISIBLE);
					text_calculate.setText(getResources().getString(R.string.total_distance) + ":" + String.valueOf(totalDistance) + "m");
					distanceMode = true;
					if(areaMode){
						areaMode = false;
						if(marker != null){
							for(int i = markList.size()-1;i>=0;i--){
								markList.get(i).remove();
								markList.remove(i);
							}
						}
						if(polyGon != null){
							for(int i = polyGonList.size()-1;i>=0;i--){
								polyGonList.get(i).remove();
								polyGonList.remove(i);
							}
						}
						points2.clear();
						polyGonList.clear();
						totalArea = 0;
					}
					break;
				case 1:
					dialog.dismiss();
					showDistance.setVisibility(View.VISIBLE);
					showTips.setVisibility(View.VISIBLE);
					text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea) + getResources().getString(R.string.square_meter));
					areaMode = true;
					if(distanceMode){
						distanceMode = false;
						if(marker != null){
							for(int i = 0;i<markList.size();i++){
								markList.get(i).remove();
							}
						}
						if(polyline2 != null){
							for(int i = 0;i<polylineList.size();i++){
								polylineList.get(i).remove();
							}
						}
						points2.clear();
						polylineList.clear();
						totalDistance = 0;
					}
					break;
//				case 2:
//					dialog.dismiss();
//					Intent intent = new Intent();
//					intent.setClass(getApplicationContext(), DrawnByHandActivity.class);
//					startActivity(intent);
//					break;

				default:
					break;
				}
			}
		});
	}
	
	public void drawPoints(List<LatLng> points){
		
		int size = points.size();
		//Toast.makeText(MainActivity.this, "drawPoints,size="+size, 0).show();
		LatLng pt = points.get(size-1);
		if(size == 1){
			
			if(startMarker!=null){
				startMarker.remove();
			}
			
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pt, 16));
			startMarker=aMap.addMarker(new MarkerOptions().position(pt).title(getResources().getString(R.string.starticon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
			
		}
		
		else{
			if(startMarker==null){
				startMarker=aMap.addMarker(new MarkerOptions().position(points.get(0)).title(getResources().getString(R.string.starticon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pt, 16));
			}

			
			options = new PolylineOptions().width(15).geodesic(true).color(Color.GREEN);//初始化轨迹属性Color.argb(255, 100, 100, 100)灰色
			
			options.addAll(points);
			//polyline.setPoints(options.getPoints());
			if(polyline!=null){
				polyline.remove();
			}
			polyline = aMap.addPolyline(options);
			
		}
	}

	public void clearTrace(){
		//Log.i("LogDemo", "clear trace");
		if(startMarker!=null){
			startMarker.remove();
		}
		if(endMarker!=null){
			endMarker.remove();
		}
		if(polyline!=null){
			polyline.remove();
			polyline=null;
			//Log.i("LogDemo", "clear polyline!!!");
		}
		//mBaiduMap.clear();
	}
	// 清除地图测量时画的线
	public void clearTrace2(){
		if(polyline2!=null){
			polyline2.remove();
			points2.clear();
			polyline2 = null;
		}
	}
	/**
	* 定位成功后回调函数
	*/
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
//		if(Common.getUserId(getApplicationContext()).length()<10){
//			Intent intent=new Intent();
//			intent.setClass(MainActivity.this, LoginActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//			finish();
//		}
//		if (mListener != null && aLocation != null&&aLocation.getErrorCode()==0) {
//			
//			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
//			Log.i("trailadapter", "main----onLocationChanged");
//			currentAlocation=aLocation;
//			currentLatLng=new LatLng(aLocation.getLatitude(),aLocation.getLongitude());
//			
//		}
//		else{
//			String errText = "定位失败," + aLocation.getErrorCode()+ ": " + aLocation.getErrorInfo();
//			//Log.e("AmapErr",errText);
//		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		/**case R.id.imgbtn_layer:
			changeLayer();
			break;*/
		//切换地图图层
		case R.id.imgbtn_layer:
			final String[] items = {getResources().getString(R.string.amap_vector),
	getResources().getString(R.string.amap_satallite), getResources().getString(R.string.google_map), 
	getResources().getString(R.string.google_map_satallite), getResources().getString(R.string.google_map_landform)};
			Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(getResources().getString(R.string.choosemaptype));
			builder2.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						dialog.dismiss();
						aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						break;
					case 1:
						dialog.dismiss();
						aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						break;
					case 2:
						dialog.dismiss();
						//利用地图瓦片显示谷歌标准地图
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						tileProvider = new UrlTileProvider(256, 256) {
							
							@Override
							public URL getTileUrl(int x, int y, int zoom) {
								try {
			                        return new URL(String.format("http://mt2.google.cn/vt/lyrs=m@198&hl=zh-CN&gl=cn&src=app&x=%d&y=%d&z=%d&s=", x, y, zoom));
			                    } catch (MalformedURLException e) {
			                        e.printStackTrace();
			                    }
								return null;
							}
						};
						tileOverlayOptions = new TileOverlayOptions()
		                        .tileProvider(tileProvider)
		                        .diskCacheEnabled(true)
		                        .diskCacheDir("/storage/emulated/0/demo/cache")
		                        .diskCacheSize(100000)
		                        .memoryCacheEnabled(true)
		                        .memCacheSize(100000);
						tileOverlay = aMap.addTileOverlay(tileOverlayOptions);
						break;
					case 3:
						dialog.dismiss();
						//谷歌卫星地图
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						tileProvider = new UrlTileProvider(256, 256) {
							
							@Override
							public URL getTileUrl(int x, int y, int zoom) {
								try {
			                        return new URL(String.format("http://mt2.google.cn/vt/lyrs=y@198&hl=zh-CN&gl=cn&src=app&x=%d&y=%d&z=%d&s=", x, y, zoom));
			                    } catch (MalformedURLException e) {
			                        e.printStackTrace();
			                    }
								return null;
							}
						};
						tileOverlayOptions = new TileOverlayOptions()
		                        .tileProvider(tileProvider)
		                        .diskCacheEnabled(true)
		                        .diskCacheDir("/storage/emulated/0/demo/cache")
		                        .diskCacheSize(100000)
		                        .memoryCacheEnabled(true)
		                        .memCacheSize(100000);
						tileOverlay = aMap.addTileOverlay(tileOverlayOptions);
						break;
						
					case 4:
						dialog.dismiss();
						//谷歌卫星地图
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						tileProvider = new UrlTileProvider(256, 256) {
							
							@Override
							public URL getTileUrl(int x, int y, int zoom) {
								try {
			                        return new URL(String.format("http://mt2.google.cn/vt/lyrs=p@198&hl=zh-CN&gl=cn&src=app&x=%d&y=%d&z=%d&s=", x, y, zoom));
			                    } catch (MalformedURLException e) {
			                        e.printStackTrace();
			                    }
								return null;
							}
						};
						tileOverlayOptions = new TileOverlayOptions()
		                        .tileProvider(tileProvider)
		                        .diskCacheEnabled(true)
		                        .diskCacheDir("/storage/emulated/0/demo/cache")
		                        .diskCacheSize(100000)
		                        .memoryCacheEnabled(true)
		                        .memCacheSize(100000);
						tileOverlay = aMap.addTileOverlay(tileOverlayOptions);
						break;
					}
					checkedItem = which;
				}
			});
//			builder2.setItems(items, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					switch (which) {
//					case 0:
//						dialog.dismiss();
//						aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
//						break;
//					case 1:
//						dialog.dismiss();
//						aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
//						break;
//					}
//				}
//			});
			builder2.create().show(); // 创建对话框并显示
			break;
/*-----------------------------------------author:zhanghao 2017-7-------------------------------------------------*/
		//为了增加测量和路径规划的功能
		//工具箱按钮
		case R.id.imgbtn_toolbox:
			/*if(mPopupWindow == null){
				initPopupWindow();
			}
			if (!mPopupWindow.isShowing()) {
				mPopupWindow.showAsDropDown(toolbox);
				mPopupWindow.update();
				// backgroundAlpha(0.7f);
			}*/
			initDialogData();
			setupDialogView();
			dialog = new AlertDialog.Builder(this).create();
			dialog.setView(view);
			dialog.show();
			
			break;
		//退出测量模式
		case R.id.iv_return:
			if(distanceMode){				
				showDistance.setVisibility(View.INVISIBLE);
				showTips.setVisibility(View.INVISIBLE);
				distanceMode = false;
				if(marker != null){								//清除所有用于测距的点标记
					for(int i = markList.size()-1;i>=0;i--){
						markList.get(i).remove();
						markList.remove(i);
					}
				}
				if(polyline2 != null){							//清除所有用于测距的线标记
					for(int i = polylineList.size()-1;i>=0;i--){
						polylineList.get(i).remove();
						polylineList.remove(i);
					}
				}
				points2.clear();
				polylineList.clear();
				totalDistance = 0;
				text_calculate.setText(getResources().getString(R.string.total_distance) + ":" + String.valueOf(totalDistance) + "m");
			}
			if(areaMode){
				showDistance.setVisibility(View.INVISIBLE);
				showTips.setVisibility(View.INVISIBLE);
				
				areaMode = false;
				if(marker != null){								//清除所有用于测距的点标记
					for(int i = markList.size()-1;i>=0;i--){
						markList.get(i).remove();
						markList.remove(i);
					}
				}
				if(polyGon != null){							//清除所有用于测面积的多边形标记
					for(int i = polyGonList.size()-1;i>=0;i--){
						polyGonList.get(i).remove();
					}
				}
				points2.clear();
				polyGonList.clear();
				totalArea = 0;
				text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea) + getResources().getString(R.string.square_meter));
			}
			break;
		/*case R.id.iv_delete:
			if(distanceMode){
				if(marker != null){
					markList.get(markList.size()-1).remove();
					markList.remove(markList.size()-1);
				}
				if(points2.size() != 0){
					points2.remove(points2.size()-1);
				}
				if(polylineList.size()!=0){
					polylineList.get(polylineList.size()-1).remove();
					polylineList.remove(polylineList.size()-1);
				}
				totalDistance -= distance;
				if(totalDistance < 1000){					
					text_calculate.setText("总距离：" + String.valueOf(totalDistance) + "m");
				}else{
					text_calculate.setText("总距离：" + String.valueOf(totalDistance/1000) + "km");
				}
			}
			
			if(areaMode){
				if(marker != null){
					markList.get(markList.size()-1).remove();
					markList.remove(markList.size()-1);
				}
				if(points2.size() != 0){
					points2.remove(points2.size()-1);
				}
				if(polyGon != null){
					polyGonList.get(polyGonList.size()-1).remove();
					polyGonList.remove(polyGonList.size()-1);
				}
				totalArea -= area;
				if(area<1000000){
					text_calculate.setText("总面积：" + String.valueOf(totalArea) + "平方米");
				}else{
					text_calculate.setText("总面积：" + String.valueOf(totalArea/1000000) + "平方公里");
				}
			}
			break;*/
/*------------------------------------------------------------------------------------------------------------------*/
		case R.id.imgbtn_starttrail:
			if(Common.isVisiter()){
				Common.DialogForVisiter(MainActivity.this);
				return;
			}	
			if(isend){//开启新的记录
				//Toast.makeText(MainActivity.this, "开始新纪录",Toast.LENGTH_SHORT).show();
				startTrail();			
			}
			else{//继续记录
				Toast.makeText(MainActivity.this, "继续记录",Toast.LENGTH_SHORT).show();
				traceService.changeTraceNo(traceNo);
				traceService.changeStatus(true);
				if(tracedata.getSportType()==1){
					//轨迹类型为步行，记录步数
					traceService.changeSportType(true);
				}
				/**
				if(timevalues!=null){
					traceService.changeGpsTime(timevalues.getRecTime());//采集时间改为记录时的采集时间，加快
				
				}
				else{
					traceService.changeGpsTime(5);
				}*/
				//traceService.changeGpsTime(Common.getRecLocFrequenct(getApplicationContext()));
				startTrail.setVisibility(View.INVISIBLE);
				//pauseTrail.setVisibility(View.VISIBLE);
//				takephoto.setVisibility(View.VISIBLE);
				endTrail.setVisibility(View.VISIBLE);
				isstart=true;
				ispause=false;
				isend=false;
			}
			break;
		case R.id.imgbtn_pausetrail:
			Toast.makeText(MainActivity.this, "暂停记录",Toast.LENGTH_SHORT).show();
			startTrail.setVisibility(View.VISIBLE);
			//pauseTrail.setVisibility(View.INVISIBLE);
			endTrail.setVisibility(View.VISIBLE);
			traceService.changeTraceNo(0);
			/**
			if(timevalues==null){
				traceService.changeGpsTime(10);
			}
			else{
				traceService.changeGpsTime(timevalues.getNoRecTime());
			}
			*/
			//traceService.changeGpsTime(Common.getNoRecLocFrequenct(getApplicationContext()));
			traceService.changeStatus(false);
			if(tracedata.getSportType()==1){
				//轨迹类型为步行，暂停记录步数
				traceService.changeSportType(false);
			}
			isstart=false;
			ispause=true;
			isend=false;
			
			//Log.i("LogDemo", "暂停记录");
			break;
		case R.id.imgbtn_endtrail:
			//Toast.makeText(MainActivity.this, "结束记录",Toast.LENGTH_SHORT).show();
			CustomDialog.Builder builder = new CustomDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.end));
			builder.setMessage(getResources().getString(R.string.tips_endtracedlg_msg));
			builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					endTrail();
//					takephoto.setVisibility(View.INVISIBLE);
					dialog.dismiss();
				}
			});
			builder.create().show();
			
			//Log.i("LogDemo", "结束记录");
			break;
		case R.id.imgbtn_takephoto:
			if(Common.isVisiter()){
				Common.DialogForVisiter(MainActivity.this);
				return;
			}
			//从POI数据库中取数据
			ArrayList<String> behaviour = helper2.getBehaviour();
			ArrayList<String> duration = helper2.getDuration();
			ArrayList<String> partnerNum = helper2.getPartnerNum();
			ArrayList<String> relation = helper2.getRelation();
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, CommentActivity.class);
			/**
			 * 传递经纬度 海拔 traceno，地点
			 * */
			if(Common.aLocation!=null){//首先采用gps位置
				intent.putExtra("longitude",Common.aLocation.getLongitude());
				intent.putExtra("latitude", Common.aLocation.getLatitude());
				intent.putExtra("altitude", Common.aLocation.getAltitude());
				
				intent.putExtra("placeName",Common.aLocation.getCity()+Common.aLocation.getRoad());
			}else{
				if(currentAlocation!=null){
					intent.putExtra("longitude",currentAlocation.getLongitude());
					intent.putExtra("latitude", currentAlocation.getLatitude());
					intent.putExtra("altitude", currentAlocation.getAltitude());
					
					intent.putExtra("placeName",currentAlocation.getCity()+currentAlocation.getRoad());
				}
			}
			intent.putExtra("traceNo", traceNo);
			// startActivity(intent);
			//传递兴趣点数据（字符串数组）到添加兴趣点页面
			try {
				intent.putStringArrayListExtra("behaviour", behaviour);
				intent.putStringArrayListExtra("duration", duration);
				intent.putStringArrayListExtra("partnerNum", partnerNum);
				intent.putStringArrayListExtra("relation", relation);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			startActivityForResult(intent, REQUSET_COMMENT);

		}
	}
	private void initPopupWindow() {
		// TODO Auto-generated method stub
		View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.mark_menu, null);
		mPopupWindow = new PopupWindow(contentView);
		mPopupWindow.setWidth(400);
		mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		
		ListView lv_menu = (ListView) contentView.findViewById(R.id.lv_menu);
		// 准备listview的数据
		List<Map<String, Object>> menu_data = new ArrayList<Map<String, Object>>();

		int[] menu_image = new int[] {  R.drawable.cal_dis , R.drawable.cal_area, R.drawable.pathplanning};
		Map<String, Object> map1 = new HashMap<String, Object>();
		String[] menuArray = getResources().getStringArray(R.array.toolpopmenu);
		map1.put("image", menu_image[0]);
		map1.put("item", menuArray[0]);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("image", menu_image[1]);
		map2.put("item", menuArray[1]);
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put("image", menu_image[2]);
		map3.put("item", menuArray[2]);
		
		menu_data.add(map1);
		menu_data.add(map2);
		menu_data.add(map3);
		
		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), menu_data, R.layout.mark_menu_items,
				new String[] { "image", "item" }, new int[] { R.id.iv_menu_item, R.id.tv_menu_item });
		lv_menu.setAdapter(adapter);
		lv_menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					mPopupWindow.dismiss();
					showDistance.setVisibility(View.VISIBLE);
					showTips.setVisibility(View.VISIBLE);
					text_calculate.setText(getResources().getString(R.string.total_distance) + ":" + String.valueOf(totalDistance) + "m");
					distanceMode = true;
					if(areaMode){
						areaMode = false;
						if(marker != null){
							for(int i = markList.size()-1;i>=0;i--){
								markList.get(i).remove();
								markList.remove(i);
							}
						}
						if(polyGon != null){
							for(int i = polyGonList.size()-1;i>=0;i--){
								polyGonList.get(i).remove();
								polyGonList.remove(i);
							}
						}
						points2.clear();
						polyGonList.clear();
						totalArea = 0;
					}
					/*aMap.setOnMapClickListener(new OnMapClickListener() {
						
						@Override
						public void onMapClick(LatLng latlng) {
							markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker)).position(latlng);
							marker = aMap.addMarker(markerOption);//添加一个标注
							markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
							
							points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
							options2 = new PolylineOptions().width(15).geodesic(true).color(Color.GREEN);//设置线段的宽度、颜色
							options2.addAll(points2);
							polyline2 = aMap.addPolyline(options2);//画出一条线段
							polylineList.add(polyline2);
						}
					});*/
					break;
				case 1:
					mPopupWindow.dismiss();
					showDistance.setVisibility(View.VISIBLE);
					showTips.setVisibility(View.VISIBLE);
					text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea) + getResources().getString(R.string.square_meter));
					areaMode = true;
					if(distanceMode){
						distanceMode = false;
						if(marker != null){
							for(int i = 0;i<markList.size();i++){
								markList.get(i).remove();
							}
						}
						if(polyline2 != null){
							for(int i = 0;i<polylineList.size();i++){
								polylineList.get(i).remove();
							}
						}
						points2.clear();
						polylineList.clear();
						totalDistance = 0;
					}
					break;

				case 2:
					mPopupWindow.dismiss();
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), DrawnByHandActivity.class);
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		});
		
	}
	/**
	 * 
	public void changeLayer(){
		final MapLayerDialog layerdialog=new MapLayerDialog(MainActivity.this,R.style.dlg_maplayer,0);
		Window window=layerdialog.getWindow();
		window.setGravity(Gravity.CENTER);
		layerdialog.setCancelable(false);
		layerdialog.show();
		layerdialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface Dialog) {
				 maptype=layerdialog.getMapType();
				 setMapVisibility(maptype);
			}
		});
	}
	
	public void setMapVisibility(int maptype){
		if(maptype==0){
			bdlocation.setVisibility(View.GONE);
			bdMapView.setVisibility(View.GONE);
			mapView.setVisibility(View.VISIBLE);
			aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
			bdmap.stop();
		}
		else if(maptype==1){
			bdlocation.setVisibility(View.GONE);
			bdMapView.setVisibility(View.GONE);
			mapView.setVisibility(View.VISIBLE);
			aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
			bdmap.stop();
		}
		else if(maptype==2){
			
				mapView.setVisibility(View.GONE);
				bdlocation.setVisibility(View.VISIBLE);
				bdMapView.setVisibility(View.VISIBLE);
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				bdmap.start();
		}
		else if(maptype==3){
			
				mapView.setVisibility(View.GONE);
				bdlocation.setVisibility(View.VISIBLE);
				bdMapView.setVisibility(View.VISIBLE);
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				bdmap.start();
		}
	}
	*/
	public void startTrail(){
		
		final SportTypeDialog dialog=new SportTypeDialog(MainActivity.this,R.style.dlg_sporttype);
		dialog.setCancelable(false);
		dialog.show();
		dialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface Dialog) {
				int  pos=dialog.getposition();
				if(pos!=-1){
					//Toast.makeText(MainActivity.this, title[pos], 0).show();
					/*
					 * 设置轨迹信息
					 * */
					traceNo=System.currentTimeMillis();
					tracedata.setTraceName(dialog.gettraceName());
					tracedata.setSportType(pos);
					tracedata.setStartTime(Common.currentTime());
					initStartInfo();
					
					ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_starttrace));
					ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_addmark));
					//showDialog("正在请求..","请求中..请稍后....");
					//PostStartTrail startTrailThread=new PostStartTrail(handler,URL_STARTTRAIL,Common.userId);
					//startTrailThread.start();
				}
				else{
					//Toast.makeText(MainActivity.this, "取消", 0).show();
				}
			}
			
		});
		 
	}
	public void initStartInfo(){
		if(!traceService.isWorking()){
			traceService.getToWork();
		}
		aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
		//bdmap.setLocationStyle(1);
		tracedata.setUserID(Common.getUserId(this));
		stepdata.setuserId(Common.getUserId(this));
		
		tracedata.setShareType(0);
		
		tracedata.setTraceNo(traceNo);
		
		stepdata.settraceNo(traceNo);
		Log.i("LogDemo", "starttrail,traceNo："+traceNo+",id:"+Common.getUserId(this));
		startTrail.setVisibility(View.INVISIBLE);
		//pauseTrail.setVisibility(View.VISIBLE);
		takephoto.setVisibility(View.VISIBLE);
		endTrail.setVisibility(View.VISIBLE);
		isstart=true;
		ispause=false;
		//isend=false;
		
		traceService.changeTraceNo(traceNo);
		traceService.changeStatus(true);
		if(tracedata.getSportType()==1){
			//轨迹类型为步行，记录步数
			traceService.changeSportType(true);
			
			StepDetector.CURRENT_SETP =helper.querryformstepsbyTraceNo(traceNo,Common.getUserId(this)).getsteps();
			
			total_step=StepDetector.CURRENT_SETP;
			startService(stepService);
			iscountstep=true;
			new Thread(stepThread).start();
			text_step.setVisibility(View.VISIBLE);
			text_step.setText(getResources().getString(R.string.steplable1)+"："+total_step);
		}
		/**
		if(timevalues!=null){
			traceService.changeGpsTime(timevalues.getRecTime());//采集时间改为记录时的采集时间，加快
		
		}
		else{
			traceService.changeGpsTime(5);
		}*/
		//traceService.changeGpsTime(Common.getRecLocFrequenct(getApplicationContext()));
	}
	public void endTrail(){
		/**
		 * 本地记录trace内容，非具体位置
		 * */
		if(refreshTrace()){
			showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_dlgmsg_login));
			points.clear();
			traceService.uploadGPS();
			List<TraceData> tracelist=new ArrayList<TraceData>();
			tracelist.add(tracedata);
			String traceInfo=GsonHelper.toJson(tracelist);
			String stepInfo;
			if(tracedata.getSportType()==1){
			List<StepData> steplist=new ArrayList<StepData>();
			steplist.add(stepdata);
			stepInfo=GsonHelper.toJson(steplist);
			}
			else{
				stepInfo="";
			}
			//Log.i("LogDemo", "endtrail,"+traceInfo+","+stepInfo);
			
			helper.updateStatus( traceNo, 2,Common.getUserId(this));
			
			PostEndTrail endTrailThread=new PostEndTrail(handler,URL_ENDTRAIL,traceInfo,stepInfo,Common.getDeviceId(getApplicationContext()));
			endTrailThread.start();
			endMarker=aMap.addMarker(new MarkerOptions().position(finalLatLng).title(getResources().getString(R.string.endicon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
			//bdmap.markEnd(finalLatLng);
		}
		else{
			ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_recorderror_nogps));
		}
		//traceNo=0;
		traceService.changeTraceNo(0);
		traceService.changeStatus(false);
		if(tracedata.getSportType()==1){
			//轨迹类型为步行，结束轨迹时iswalk置为false，停止记录步数
			iscountstep=false;//结束线程
			traceService.changeSportType(false);//是否步行设置为否
			stopService(stepService);//结束计步服务
			//stepThread.stop();
			//handler.removeCallbacks(stepThread);
		}
		/**
		if(timevalues==null){
			traceService.changeGpsTime(10);
		}
		else{
			traceService.changeGpsTime(timevalues.getNoRecTime());
		}
		*/
		//traceService.changeGpsTime(Common.getNoRecLocFrequenct(getApplicationContext()));
		startTrail.setVisibility(View.VISIBLE);
		//pauseTrail.setVisibility(View.INVISIBLE);
		endTrail.setVisibility(View.INVISIBLE);
		text_step.setVisibility(View.INVISIBLE);
		isstart=false;
		ispause=false;
		isend=true;
		clearTrace();
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		//bdmap.setLocationStyle(0);
	}
	public boolean refreshTrace(){
		
		tracegps=helper.queryfromGpsbytraceNo(traceNo,Common.getUserId(this));
		//Gson tracejosn=new Gson();
		if(tracegps.size()>0){
			
			tracedata.setEndTime(Common.currentTime());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long duration=0;
			try{ //计算时间差
				Date d1 = df.parse(tracedata.getStartTime()); 
				Date d2 = df.parse(tracedata.getEndTime()); 
				duration = d2.getTime() - d1.getTime(); 
				
			}catch (Exception e){
				return false;
			} 
			tracedata.setDuration(duration);
			//计算距离
			double distance=0.0;
			if(tracegps.size()>1){
				for(int i=0;i<tracegps.size()-1;i++){
					distance+=AMapUtils.calculateLineDistance(new LatLng(tracegps.get(i).getLatitude(),tracegps.get(i).getLongitude()),
							new LatLng(tracegps.get(i+1).getLatitude(),tracegps.get(i+1).getLongitude()));
				}
			}
			tracedata.setDistance(distance);
			
			if(tracedata.getSportType()==1){
				stepdata.setsteps(total_step);
				helper.updatesteps(stepdata, traceNo,Common.getUserId(this));
				tracedata.setCalorie(calculateCalorie_Walk(distance));
			}
			if(tracedata.getSportType()==2){
				tracedata.setCalorie(calculateCalorie_Ride(distance, duration));
				
			}
			PhotoDBHelper photoHelper = new PhotoDBHelper(this, PhotoDBHelper.DBREAD);
			if(cursor != null && !cursor.isClosed()){
				cursor.close();
			}
			cursor = photoHelper.selectEvent(null, PhotoDBHelper.COLUMNS_UE[10] + "="
					+ Common.getUserId(this)+" and datetime("
							+ PhotoDBHelper.COLUMNS_UE[0] + ") between '"+tracedata.getStartTime()+
							"' and '"+tracedata.getEndTime()+"'", null, null, null, null);
			
			int poiCount = cursor.getCount();
			tracedata.setPoiCount(poiCount);
			
			helper.updatetrail(tracedata,traceNo,Common.getUserId(this));
			helper.updateStatus( traceNo, 1,Common.getUserId(this));
			
			return true;
		}
		else{
			//ToastUtil.show(MainActivity.this, "未采集到位置信息，轨迹不保存");
			
			return false;
		}
	}
	/**
	 * 实际的步数
	 */
	private void countStep() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			total_step = StepDetector.CURRENT_SETP;
		} else {
			total_step = StepDetector.CURRENT_SETP +1;
		}

		total_step = StepDetector.CURRENT_SETP;
	}
	private int calculateCalorie_Walk(double distance){
		//体重（kg）×距离（公里）×1.036 
		return (int)(60*1.036*distance/1000);
	}
	private int calculateCalorie_Ride(double distance,long duration){
		//时速(km/h)×体重(kg)×1.05×运动时间(h)
		return (int)(60*1.05*distance/1000);
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case 0://获取轨迹号成功
				//dismissDialog();
				//if(msg.obj.toString().trim()!=null&&msg.obj.toString().trim()!=""){
					
				//}
				//ToastUtil.show(MainActivity.this, "开始记录");
				break;
			case 1://获取轨迹号失败
				//dismissDialog();
				//Toast.makeText(MainActivity.this, "开始失败，请重试",Toast.LENGTH_SHORT).show();
				break;
			case 2://结束记录轨迹成功
				dismissDialog();
				
				helper.updateStatus( traceNo, 0,Common.getUserId(MainActivity.this));
				helper.deleteStatus();
				//云端新增轨迹提醒，提示用户下拉刷新
				Intent intent = new Intent();  
		        intent.setAction(PULLREFRESH_ACTION);  
		        sendBroadcast(intent);
				ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_recordsuccess));
				break;
			case 3://结束记录轨迹失败
				dismissDialog();
				//Toast.makeText(MainActivity.this, getResources().getString(R.string.tips_recordsuccess_postfail),Toast.LENGTH_SHORT).show();
				CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
				builder.setTitle(getResources().getString(R.string.tip));
				builder.setMessage(getResources().getString(R.string.tips_uploadtracedlg_msgfail));
				builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setPositiveButton(getResources().getString(R.string.tryagain),new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						traceService.uploadGPS();
						dialog.dismiss();
					}
				});
				builder.create().show();
				break;
			case 4://获取4个时间成功
				/**
				if(msg.obj.toString().trim()!=null&&msg.obj.toString().trim()!=""){
					//Gson gson=new Gson();
					//timevalues=GsonHelper.fromJson(msg.obj.toString().trim(),new TypeToken<TimeValue>(){}.getType());
					timevalues=GsonHelper.parseJson(msg.obj.toString().trim(), TimeValue.class);
					//不得在此处进行如下操作，原因：service连接尚未建立,....也有可能连接已建立
					if(bound_trace&&!istimeset){//service已建立，但是由于没获取时间所以未设置时间
						initTime();
					}
				}
				*/
				//Toast.makeText(MainActivity.this, "",Toast.LENGTH_SHORT).show();
				break;
			case 5://获取4个时间失败
				/**
				if(!traceService.isworking()){
					traceService.getToWork();
				}
				Log.i("LogDemo", "获取4个时间失败，开启默认时间");
				*/
				break;
			case 9://更新步数
				countStep();
				text_step.setText(getResources().getString(R.string.steplable1)+"："+total_step);
				if(tracedata.getSportType()==1){
					stepdata.setsteps(total_step);
					
					helper.updatesteps(stepdata, traceNo,Common.getUserId(MainActivity.this));
					
				}
				break;
			case 10:
				dismissDialog();
				
				break;
			case 11://endtrail 上传失败，原因：网络未连接
				dismissDialog();
				Toast.makeText(MainActivity.this,getResources().getString(R.string.tips_uploadtracedlg_msgnonet),Toast.LENGTH_LONG).show();
				
				break;
			}
		}
		
	};
	private Handler updatehandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0://
				
				final String[] updatestr=msg.obj.toString().trim().split("&");
				//Log.i("LogDemo","有更新,"+ url);
				if(updatestr.length>=5){
					String version=updatestr[0];
					String time=updatestr[1];
					String url=updatestr[2];
					String detail=updatestr[3];
					String size=updatestr[4];
					Log.i("LogDemo","有更新,"+version+size+time+ url+detail);
					CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
					builder.setTitle(getResources().getString(R.string.tips_updatedlg_tle));
					builder.setMessage(getResources().getString(R.string.tips_updatedlg_msg1)+"\n"
							+getResources().getString(R.string.tips_updatedlg_msg2)+version+"\n"
							+getResources().getString(R.string.tips_updatedlg_msg3)+size+"\n"
							+getResources().getString(R.string.tips_updatedlg_msg4)+time+"\n"
							+getResources().getString(R.string.tips_updatedlg_msg5)+detail+"\n"
							+getResources().getString(R.string.tips_updatedlg_msg6));
					builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Common.fileInfo=new FileInfo(0, updatestr[2]
				            		
				            		,"HappyNavi"+updatestr[0]+".apk", 0, 0);//User/userDownApk.aspx
							// 通知Service开始下载
							updateService = new Intent(MainActivity.this, DownloadService.class);
							updateService.setAction(DownloadService.ACTION_START);
							updateService.putExtra("fileInfo", Common.fileInfo);
							startService(updateService);
							Common.isUpdationg=true;
							ToastUtil.show(getApplicationContext(), getResources().getString(R.string.tips_gotodownnewapk));
							
						}
					});
					builder.create().show();
				}
				
				break;
			case 1://
				//Log.i("phonelog", "自动检测，已是最新,"+msg.obj.toString().trim());
				break;
			case 2://
				//Log.i("phonelog","检查更新失败,"+ msg.obj.toString().trim());
				break;
			case 10:
				//Log.i("phonelog","检查更新异常,"+ msg.obj.toString().trim());
				break;
			}
		}
	};
	//传递给PostPointOfInterestData的handler1
		private Handler handler1 = new Handler() {
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
						helper2.delete();
						try {
							for(int i = 0;i<behaviourStr.length;i++){
								behaviourData.setKey(i);
								behaviourData.setValue(behaviourStr[i]);
								helper2.insertBehaviour(behaviourData);
							}
							for(int i = 0;i<durationStr.length;i++){
								durationData.setKey(i);
								durationData.setValue(durationStr[i]);
								//将数据插入到POI数据库中
								helper2.insertDuration(durationData);
							}
							for(int i = 0;i<partnerNumStr.length;i++){
								partnerNumData.setKey(i);
								partnerNumData.setValue(partnerNumStr[i]);
								helper2.insertPartnerNum(partnerNumData);
							}
							for(int i = 0;i<relationStr.length;i++){
								relationData.setKey(i);
								relationData.setValue(relationStr[i]);
								helper2.insertPartnerRelation(relationData);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					break;
				case 1:

					break;
				}
			}
		};
	
	
	/**
	 * 请求其它Activity的返回结果
	 * 
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// 如果评论Activity返回的结果，根据该结果进行图片上传
			case REQUSET_COMMENT:
				
				// 如果有wifi网络，直接开始上传
				if (Common.checkNetworkState(getApplicationContext()) == ConnectivityManager.TYPE_WIFI) {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.tips_uploadinbgbegin),
							Toast.LENGTH_SHORT).show();
					String commentTime = data.getStringExtra("createTime");
					commentService.putExtra("createTime", commentTime);
					startService(commentService);
					//commentUploadService.uploadComment(Common.getUserId(getApplicationContext()),
					//		commentTime);
				}// 如果是数据流量连接，第一次使用询问是否上传
				else if (Common.checkNetworkState(getApplicationContext()) == ConnectivityManager.TYPE_MOBILE
						&& !Common.isOnlyWifiUploadPic(MainActivity.this)) {
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
										commentService.putExtra("createTime", commentTime);
										startService(commentService);
										//commentUploadService.uploadComment(
										//		Common.getUserId(getApplicationContext()),
										//		commentTime);
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
						commentService.putExtra("createTime", commentTime);
						startService(commentService);
						//commentUploadService.uploadComment(
						//		Common.getUserId(getApplicationContext()),
						//		commentTime);
					}
				} else {
					// 如果没有网络，将上传相关信息写到cache文件
					Toast.makeText(this,getResources().getString(R.string.tips_uploadpic_nonet),
							Toast.LENGTH_SHORT).show();
					String commentTime = data.getStringExtra("createTime");
					String userId = Common.getUserId(getApplicationContext());
					SharedPreferences.Editor editor = uploadCache.edit();
					editor.putString(commentTime, userId);
					editor.commit();
				}
				break;
			default:
				break;
			}
		}
	}

	

	/*@Override
	public void onInfoWindowClick(Marker marker) {
		ToastUtil.show(this, "你点击了infoWindow窗口" + marker.getTitle());
	}*/

	@Override
	public boolean onMarkerClick(Marker marker) {
		//ToastUtil.show(this,marker.getTitle());
		return false;
	}

	/**
	 * 地图点击事件监听接口的实现方法
	 */
	@Override
	public void onMapClick(LatLng latlng) {
		/*Log.i("LogDemo", "onmapclick:("+latlng.latitude+","+latlng.longitude+")");
		showDialog("","正在获取地址");
		latLonPoint=new LatLonPoint(latlng.latitude, latlng.longitude);
		final LatLonPoint finallatLonPoint=latLonPoint;
		RegeocodeQuery query = new RegeocodeQuery(finallatLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火星坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
		*/
		if(distanceMode){
			if(points2.size() == 0){
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng).title("起点");
				marker = aMap.addMarker(markerOption);//添加一个标注
				markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
				points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
			}else{
				showTips.setVisibility(View.INVISIBLE);
				points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
				LatLng curLatLng = latlng;
				LatLng preLatLng = points2.get(points2.size()-2);
				distance = 0;
				if(preLatLng != curLatLng){				
					distance = AMapUtils.calculateLineDistance(curLatLng, preLatLng);
				}
				totalDistance += distance;
				Log.i("1000", String.valueOf(totalDistance));
				if(totalDistance < 1000){					
					text_calculate.setText(getResources().getString(R.string.total_distance) + ":" + String.valueOf(totalDistance) + "m");
				}else{
					text_calculate.setText(getResources().getString(R.string.total_distance) + ":" + String.valueOf(totalDistance/1000) + "km");
				}
				
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng).title(getResources().getString(R.string.to_the_previous_point) + ":").snippet(String.valueOf(distance)+"m");
				marker = aMap.addMarker(markerOption);//添加一个标注
				marker.showInfoWindow();
				markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
				/*for(int i = 0;i<markList.size();i++){				
				markList.get(i).showInfoWindow();
			}*/
				
				options2 = new PolylineOptions().width(15).geodesic(true).color(Color.argb(255, 184, 49, 170));//设置线段的宽度、颜色
				options2.addAll(points2);
				polyline2 = aMap.addPolyline(options2);//画出一条线段
				polylineList.add(polyline2);
			}
		}
		
		if(areaMode){
			if(points2.size() <= 1){
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng);
				marker = aMap.addMarker(markerOption);//添加一个标注
				markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
				points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
			}else if(points2.size() == 2){
				showTips.setVisibility(View.INVISIBLE);
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng);
				marker = aMap.addMarker(markerOption);//添加一个标注
				markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
				
				points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
				polyGonOptions = new PolygonOptions().fillColor(Color.argb(127, 0, 0, 255));
				polyGonOptions.addAll(points2);
				polyGon = aMap.addPolygon(polyGonOptions);
				polyGonList.add(polyGon);
				
				float a,b,c,s;
				LatLng curLatLng = latlng;
				LatLng preLatLng = points2.get(points2.size()-2);
				LatLng prepreLatLng = points2.get(points2.size()-3);
				a = AMapUtils.calculateLineDistance(curLatLng, preLatLng);
				b = AMapUtils.calculateLineDistance(preLatLng, prepreLatLng);
				c = AMapUtils.calculateLineDistance(curLatLng, prepreLatLng);
				s = (a + b + c)/2;
				area = (float) Math.sqrt(s*(s-a)*(s-b)*(s-c));//海伦-秦九韶公式-已知三角形三边求面积
				totalArea += area;
				if(area<1000000){
					text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea) + getResources().getString(R.string.square_meter));
					Log.i("10000", String.valueOf(totalArea)+"m2");
				}else{
					text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea/1000000) + getResources().getString(R.string.square_kilometer));
					Log.i("10000", String.valueOf(totalArea/1000000)+"km2");
				}
			}else{
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng);
				marker = aMap.addMarker(markerOption);//添加一个标注
				markList.add(marker);//将每一次添加标注的mark对象添加到markList集合中
				
				points2.add(latlng);//将每一次添加标注的经纬度对象latlng添加到points2集合中
				polyGonOptions = new PolygonOptions().fillColor(Color.argb(127, 0, 0, 255));
				polyGonOptions.addAll(points2);
				polyGon.remove();
				polyGon = aMap.addPolygon(polyGonOptions);
				polyGonList.add(polyGon);
				
				float a,b,c,s;
				LatLng curLatLng = latlng;
				LatLng preLatLng = points2.get(points2.size()-2);
				LatLng prepreLatLng = points2.get(points2.size()-3);
				a = AMapUtils.calculateLineDistance(curLatLng, preLatLng);
				b = AMapUtils.calculateLineDistance(preLatLng, prepreLatLng);
				c = AMapUtils.calculateLineDistance(curLatLng, prepreLatLng);
				s = (a + b + c)/2;
				area = (float) Math.sqrt(s*(s-a)*(s-b)*(s-c));
				totalArea += area;
				if(area<1000000){
					text_calculate.setText(getResources().getString(R.string.total_area) + ":"+ String.valueOf(totalArea) + getResources().getString(R.string.square_meter));
					Log.i("10000", String.valueOf(totalArea)+"m2");
				}else{
					text_calculate.setText(getResources().getString(R.string.total_area) + ":" + String.valueOf(totalArea/1000000) + getResources().getString(R.string.square_kilometer));
					Log.i("10000", String.valueOf(totalArea/1000000)+"km2");
				}
			}
		}
	}
	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				addressName = result.getRegeocodeAddress().getCity()+","+result.getRegeocodeAddress().getPois().get(0)
						+ "附近";
				//aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AMapUtil.convertToLatLng(latLonPoint), 15));
				latLonPoint=result.getRegeocodeAddress().getPois().get(0).getLatLonPoint();
				interestpoint.setTitle(result.getRegeocodeAddress().getPois().get(0)+"");
				interestpoint.setPosition(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()));
				interestpoint.showInfoWindow();
				
				ToastUtil.show(MainActivity.this, addressName);
			} else {
				ToastUtil.show(MainActivity.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(MainActivity.this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(MainActivity.this, R.string.error_key);
		} else {
			ToastUtil.show(MainActivity.this,
					getString(R.string.error_other) + rCode);
		}
	}
	/*@Override
	public View getInfoContents(Marker marker) {
		
		return null;
		
	}*/

	/*@Override
	public View getInfoWindow(Marker marker) {
		View infoWindow = getLayoutInflater().inflate(
				R.layout.custom_info_window, null);
		//Log.i("LogDemo", "getInfoWindow");
		if(!toolboxOpened){			
			render(marker, infoWindow);
		}
		return infoWindow;
	}*/
	/**
	 * 自定义info window窗口
	 */
	public void render(Marker marker, View view) {
		ImageView imageView = (ImageView) view.findViewById(R.id.badge);
		imageView.setImageResource(R.drawable.ic_photo);
		String title = marker.getTitle();
		TextView titleUi = ((TextView) view.findViewById(R.id.title));
		if (title != null) {
			SpannableString titleText = new SpannableString(title);
			titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
					titleText.length(), 0);
			titleUi.setTextSize(15);
			titleUi.setText(titleText);
			//Log.i("LogDemo", "title"+title);
		} else {
			titleUi.setText("");
		}
		String snippet = marker.getSnippet();
		TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
		if (snippet != null) {
			SpannableString snippetText = new SpannableString(snippet);
			snippetText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
					snippetText.length(), 0);
			snippetUi.setTextSize(20);
			snippetUi.setText(snippetText);
		} else {
			snippetUi.setText("");
		}
		/*Button comment=((Button) view.findViewById(R.id.comments));
		comment.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ToastUtil.show(MainActivity.this, "你点击了评论按钮");
			}
			
		});*/
	}
	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add(0, MENU_ROUTE, 1, "路线规划");
		//menu.add(0, MENU_NAVI, 2, "导航");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENU_ROUTE:
			//Intent intent=new Intent();
			//intent.setClass(MainActivity.this,RoutePlanning.class);
			//startActivity(intent);
			break;
		case MENU_NAVI:
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	* 激活定位
	*/
	@Override
	public void activate(OnLocationChangedListener listener) {
		
		mListener = listener;
		/**
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(getApplicationContext());
			mLocationOption = new AMapLocationClientOption();
			//设置定位监听
			mlocationClient.setLocationListener(this);
			//设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//设置定位时间间隔
			mLocationOption.setInterval(Common.getRecLocFrequenct(getApplicationContext())*1000);
			//设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mlocationClient.startLocation();
			
		}
		*/
		
		/*if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			//mAMapLocationManager.setGpsEnable(false);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			/*mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 10000, 20, this);
		}*/
	}
	
	/**
	* 停止定位
	*/
	@Override
	public void deactivate() {
		
		mListener = null;
		/*if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destroy();
		}
		mAMapLocationManager = null;*/
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}
	private void showBGRunGuide() {//提示用户将应用加入保护名单
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.tip));
		
		builder.setMessage(getResources().getString(R.string.tips_bgrunguide));
	    builder.setIsShowChebox(true);    		
		builder.setCheckBox(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				 	Editor editor = sp.edit();
			        editor.putBoolean("isShowBGRGuide", !isChecked);
			        editor.commit();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.confirm),
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    	
                    	if(Common.isNetConnected){
							startActivity(new Intent(MainActivity.this,BGRunningGuide.class));
							
						}else{
							ToastUtil.show(MainActivity.this, getResources().getString(R.string.tips_netdisconnect));
							
						}
                        
                    }
                    
                });
		builder.setNegativeButton(getResources().getString(R.string.close), new android.content.DialogInterface.OnClickListener() {
             
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                
            }
        } );
        builder.create().show();
    }
	private void showDlg_badloc() {//无法定位时弹出提示
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.tip));
		
		builder.setMessage(getResources().getString(R.string.tips_cannotloc));
	    builder.setIsShowChebox(true);    		
		builder.setCheckBox(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = sp.edit();
		        editor.putBoolean("isShowBadLoc", !isChecked);
		        editor.commit();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.open),
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        // 转到手机设置界面，用户设置GPS
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
                        arg0.dismiss();
                        isShowNonLocDlg = false;
                    }
                    
                });
		builder.setNegativeButton(getResources().getString(R.string.cancl), new android.content.DialogInterface.OnClickListener() {
             
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                isShowNonLocDlg = false;
            }
        } );
        builder.create().show();
    }
	//低精度时弹出提示对话框，用户选择是否采用低精度位置
	private void showDlg_LowAccuracy(){
		CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(getResources().getString(R.string.tips_accuracydlg_msg));
		builder.setNegativeButton(getResources().getString(R.string.no),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.yes),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Common.isHighAccuracy=false;
				dialog.dismiss();
			}
		}); 
		builder.create().show();
	}

	
	/**
	 * 显示进度条对话框
	 */
	public void showDialog(String title,String message) {
		if (proDialog == null)
			proDialog = new ProgressDialog(this);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setIndeterminate(false);
		proDialog.setCancelable(true);
		proDialog.setTitle(title);
		proDialog.setMessage(message);
		proDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}

	
	/**
	* 方法必须重写
	*/
	@Override
	protected void onResume() {
		 
		
		super.onResume();
	
			mapView.onResume();
			//bdMapView.onResume();
			//if(Common.isNetConnected){
				//activate(mListener);
			//}
			//Log.i("trailadapter", "onresume");
			//setMapVisibility(maptype);
	}
	 
	/**
	* 方法必须重写
	*/
	@Override
	protected void onPause() {
		//setMapVisibility(-1);
		super.onPause();
		mapView.onPause();
		//bdMapView.onPause();
		//deactivate();
		//Log.i("trailadapter", "onPause");
	}
	 
	/**
	* 方法必须重写
	*/
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
		
	}
	
	/**
	* 方法必须重写
	*/
	@Override
	protected void onDestroy() {
		super.onDestroy();
		iscountstep=false;
		deactivate();
		if(null != mlocationClient){
			mlocationClient.onDestroy();
		}
		//bdmap.stop();
		mapView.onDestroy();
		//bdMapView.onDestroy();
		if(bound_trace){
			this.getApplicationContext().unbindService(conn);
			bound_trace=false;
		} 
//		if(bound_upload){
//			this.getApplicationContext().unbindService(conn_comment);
//			bound_upload=false;
//		} 
		Common.isUpdationg=false;
		if(null!=locService){
			stopService(locService);
		}
		if(null!=commentService){
			stopService(commentService);
		}
		if(null!=updateService){
			stopService(updateService);
		}
		if(null!=stepService){
			stopService(stepService);
		}
		if(null!=myreveiver){
			MainActivity.this.unregisterReceiver(myreveiver);
		}
		if(null!=accuracyReciver){
			MainActivity.this.unregisterReceiver(accuracyReciver);
		}
		if(null!=connectionReceiver){
			MainActivity.this.unregisterReceiver(connectionReceiver);
		}
		Log.i("LogDemo", "Main-onDestroy");

	}
//	@Override
//	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//		if (aMap != null) {
//			setLayer((String) parent.getItemAtPosition(position));
//		}
//	}
//	/**
//	 * 选择矢量地图和卫星地图事件的响应
//	 */
//	private void setLayer(String layerName) {
//		if (layerName.equals(getString(R.string.normal))) {
//			aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
//		} else if (layerName.equals(getString(R.string.satellite))) {
//			aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
//		}
//	}
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//		
//	}
	private void initPOI(){
		//从服务器下载停留时长、行为类型、同伴人数、关系等选项的数据
		PostPointOfInterestData pointOfInterest = new PostPointOfInterestData(handler1, URL_GETPOI);
		pointOfInterest.start();
	}
	private void initPOIEN(){
		//英文版
		PostPointOfInterestDataEn pointOfInterestEn = new PostPointOfInterestDataEn(handler1, URL_GETPOI);
		pointOfInterestEn.start();
	}
	private void initPOICZ(){
		//英文版
		PostPointOfInterestDataCz pointOfInterestCz = new PostPointOfInterestDataCz(handler1, URL_GETPOI);
		pointOfInterestCz.start();
	}
}