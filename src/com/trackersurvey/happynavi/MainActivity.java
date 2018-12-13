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
	private Marker interestpoint;          //��Ӧ�û������λ��
	private Marker startMarker=null;            //�����
	private Marker endMarker=null;				//�յ���
	private Polyline polyline=null;				//�켣����
	private PolylineOptions options;		//�켣������
	
	private Polyline polyline2 = null;		//�ֶ����߶���2017-7-16��
	private PolylineOptions options2;		//�ֶ��������ö���
	
	private Polygon polyGon = null;			//�ֶ�������ζ���2017-7-18��
	private PolygonOptions polyGonOptions;	//�ֶ�����������ö���
	
	
	private OnLocationChangedListener mListener;
	
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	
	private GeocodeSearch geocoderSearch;
	private LatLonPoint latLonPoint;       		 //�û������λ�õľ�γ��
	private LatLng finalLatLng=new LatLng(0,0);  //�켣�յ�
	private String addressName;	
	private LatLng currentLatLng;				 //��λ���ĵ�ǰλ��
	private AMapLocation currentAlocation;
	private List<LatLng> points;				 //�켣��ĳ�Ա
	//private TimeValue timevalues=null;		 //serviceʱ�����
	
	
	private boolean isstart=false;
	private boolean ispause=false;
	private boolean isend=true;
	private boolean bound_trace=false;
	//private boolean bound_upload=false;
	private boolean istimeset=false;
	private boolean iscountstep=false;
	private boolean isShowNonLocDlg = false; //�޷���λ�Ի����Ƿ�������ʾ
	
	public  static final int MENU_ROUTE=0;
	public  static final int MENU_NAVI=1;
	private int maptype=0;
	//String[] title=new String[]{"����","��ɽ","����","�ܲ�","��ѩ","�ֻ�","��Ӿ","ͽ��","ɢ��","����¼�켣","ȡ��"};
	private long traceNo=0;
	private int total_step = 0;   //�ߵ��ܲ���
	private TraceData tracedata=new TraceData();
	private StepData stepdata=new StepData();
	private List<GpsData> tracegps=new ArrayList<GpsData>();
	private MyBroadcastReciver myreveiver=null;//���ڽ��պ�̨���͵Ķ�λ�㲥
	private AccuracyBroadcastReciver accuracyReciver=null;
	private BroadcastReceiver connectionReceiver = null; // ���ڼ�������״̬�仯�Ĺ㲥
	
	private GpsTrace traceService;
	//private CommentUploadService commentUploadService;
	private Intent locService;
	private Intent commentService;
	private Intent stepService ;
	private Intent updateService;
	private Thread stepThread;
	private TraceDBHelper helper;
	private SharedPreferences sp;  //�洢����������Ϣ ���˺š�����
	private  SharedPreferences uploadCache;//�洢���ϴ���������Ϣ
	//private FileInfo fileInfo = null;//�°�apk�ļ�
	
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
	
	private Marker marker;					//�ֶ����ߵı�ע�����2017-7-16��
	private MarkerOptions markerOption;		//�ֶ����ߵı�ע�����ö���
	private List<Marker> markList;			//�ֶ����ߵı�ע����󼯺ϣ���onCreat�����г�ʼ����
	private List<LatLng> points2;			//�ֶ����ߵľ�γ�ȵ㼯�ϣ���onCreat�����г�ʼ����
	private List<Polyline> polylineList;	//�ֶ����ߵĻ��߶��󼯺ϣ���onCreat�����г�ʼ����
	private List<Polygon> polyGonList;		//�ֶ�������εĶ��󼯺ϣ���onCreat�����г�ʼ������2017-7-18��
	private boolean distanceMode = false;	//�����ģʽ
	private boolean areaMode = false;		//�����ģʽ
	private float distance;
	private float totalDistance;			//�ܾ���
	private float area;
	private float totalArea;				//�����
	//private ImageButton deletePoint;		//ɾ����ǰ������
	
	private RelativeLayout showDistance;	//����ģʽ����
	private RelativeLayout showTips;
	private TileProvider tileProvider;		//��Ƭ�ṩ�ߣ�����ת����ͼͼ��
	private TileOverlayOptions tileOverlayOptions;
	private TileOverlay tileOverlay;
	
	private ListView dialogList;
	private ArrayList<HashMap<String,Object>> dialogListItem;
	//private LinearLayout dialogLayout;
	private View view;
	private AlertDialog dialog;
	/**
	 * BDMAP
	 
	// �Ƿ���ʾbdmap
		boolean openBaidu = false;
		// ����baidumap�ؼ� �ٶ�
		public com.baidu.mapapi.map.MapView bdMapView = null;
		public BaiduMap mBaiduMap;
		private BDMap bdmap;
	private Context bdcontext;
	* */	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//getWindow().setFormat(PixelFormat.TRANSLUCENT);//��˵�ܽ������,����ûЧ��
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
		mapView.onCreate(savedInstanceState);// �˷���������д
		
		points2 = new ArrayList<LatLng>();			//�ֶ����ߵľ�γ�ȵ�ļ��ϳ�ʼ����2017-7-16��
		markList = new ArrayList<Marker>();			//�ֶ����ߵı�ע��ļ��ϳ�ʼ��
		polylineList = new ArrayList<Polyline>();	//�ֶ����ߵĶ���ļ��ϳ�ʼ��
		polyGonList = new ArrayList<Polygon>();		//�ֶ�������εĶ��󼯺ϳ�ʼ��
		
		//text_distance.setText("�ܾ��룺" + String.valueOf(totalDistance) + "m");
		
		helper=new TraceDBHelper(this);
		helper2 = new PointOfInterestDBHelper(this);//����POI���ݿ�
		// sp ��ʼ�� 
        sp=getSharedPreferences("config",MODE_PRIVATE);//˽�в���
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
			initPOI();//���������Ȥ�������б�ѡ������
		}
		if(l==1){
			initPOIEN();//Ӣ�İ�
		}
		if(l==2) {
			initPOICZ();// �ݿ����
		}
//		if(!polylineList.isEmpty()){
//			for(int i = 0;i<polylineList.size();i++){
//				distance = AMapUtils.calculateLineDistance(polylineList.get(polylineList.size()-1), finalLatLng);
//			}
//		}
	}
	// ��ʼ���ٶ�ͼ��
	/**
			public void initBDMap() {
				// �ٶȵ�ͼ
				bdMapView = (com.baidu.mapapi.map.MapView) findViewById(R.id.bmapmainView);
				mBaiduMap = bdMapView.getMap();
				
				bdMapView.setVisibility(View.GONE);
				// ������λͼ��
				//mBaiduMap.setMyLocationEnabled(true);// ��������
				int childCount = bdMapView.getChildCount();
				View zoom = null;
				for (int i = 0; i < childCount; i++) {
					View child = bdMapView.getChildAt(i);
					if (child instanceof ZoomControls) {
						zoom = child;
						break;
					}
				}
				zoom.setVisibility(View.GONE);//���طŴ���С��ť
				bdmap=new BDMap(mBaiduMap,bdMapView,bdcontext);
				//bdmap.start();
			}*/
	/**
	* ��ʼ��AMap����
	*/
	private void initAmap() {
		//��ȡ��Ļ�ֱ���
		//SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		Common.winWidth = sp.getInt(LoginActivity.winWidth, 720);
		Common.winHeight = sp.getInt(LoginActivity.winHeight, 1280);
		Common.ppiScale = sp.getFloat(LoginActivity.PPISCALE, 1.5f);
		//��������ļ���
		Common.createFileDir();
		// ��������㲥����
		connectionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				
				NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
				if(netInfo!=null && netInfo.isConnected()){
					//����������
					if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
						//wifi����
						/**���������м��롰wifi״̬���Զ����¡�ѡ��˴�����bool���ж��Ƿ��Զ����
						 **/
						Log.i("phonelog", "��ǰWiFi����");
						Common.isWiFiConnected=true;
						if(Common.isUpdationg&&Common.fileInfo!=null){
							Log.i("phonelog", "WiFi�¼�������");
							// ֪ͨService��������
							updateService = new Intent(MainActivity.this, DownloadService.class);
							updateService.setAction(DownloadService.ACTION_START);
							updateService.putExtra("fileInfo", Common.fileInfo);
							startService(updateService);
						}else{
							if(Common.isAutoUpdate(getApplicationContext())){
								Log.i("phonelog", "wifi�¼�����");
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
								Log.i("phonelog", "�Զ������¹ر�");
							}
						}
						
						
					}
					else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
						// connect network,��ȡ����sharedPreferences�ļ����ϴ�֮ǰδ����ϴ��Ĳ���
						Log.i("phonelog", "��ǰGPRS��������");
						Common.isWiFiConnected=false;
						Log.i("phonelog", "WiFi���ӶϿ�");

						
						if (Common.isUpdationg&&Common.fileInfo!=null) {
							// ֪ͨService��ͣ����
							updateService = new Intent(MainActivity.this, DownloadService.class);
							updateService.setAction(DownloadService.ACTION_STOP);
							updateService.putExtra("fileInfo", Common.fileInfo);
							startService(updateService);
							Log.i("phonelog", "������ͣ����");
						}
					}else{
						Common.isWiFiConnected=false;
						Log.i("phonelog", "WiFi���ӶϿ�");
					}
				}else{
					//����������
					Log.i("phonelog", "Main����ǰ������");
					Common.isWiFiConnected=false;
					
					if(Common.isUpdationg&&Common.fileInfo!=null){
						// ֪ͨService��ͣ����
						updateService = new Intent(MainActivity.this, DownloadService.class);
						updateService.setAction(DownloadService.ACTION_STOP);
						updateService.putExtra("fileInfo", Common.fileInfo);
						startService(updateService);
						Log.i("phonelog", "������ͣ����");
					}
					if(!Common.checkGPS(getApplicationContext())){
						//����û����gpsû�����޷���λ����ʾ
						if(!isShowNonLocDlg){
							isShowNonLocDlg=true;
							boolean isShowBadLoc = sp.getBoolean("isShowBadLoc", true);
							if(isShowBadLoc){
								Log.i("phonelog", "����û����gpsû�����޷���λ����ʾ");
								
								showDlg_badloc();
							}
						}
					}
				}
				
			}
		};
		
		//ע�������������״̬�㲥
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
	 * ע�����
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(MainActivity.this);
		aMap.setOnMarkerClickListener(MainActivity.this);
		//aMap.setOnInfoWindowClickListener(MainActivity.this);
		//aMap.setInfoWindowAdapter(MainActivity.this);
	}
	/**
	* ����һЩamap������
	*/
	private void setUpMap() {
		
		// �Զ���ϵͳ��λС����
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// ����С�����ͼ��
		myLocationStyle.strokeColor(Color.BLACK);// ����Բ�εı߿���ɫ
		myLocationStyle.radiusFillColor(Color.argb(100, 255, 255, 255));// ����Բ�ε������ɫ
		// myLocationStyle.anchor(int,int)//����С�����ê��
		myLocationStyle.strokeWidth(1.0f);// ����Բ�εı߿��ϸ
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.getUiSettings().setCompassEnabled(true);  //��������
		aMap.getUiSettings().setScaleControlsEnabled(true);//���ñ�����
		aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//�������Ű�ť���Ҳ��м�λ��
		//aMap.getUiSettings().setZoomGesturesEnabled(false);//����˫���Ŵ��ͼ����
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
		double lastLongitude = sp.getFloat("lastLongitude", 0);
		double lastLatitude = sp.getFloat("lastLatitude", 0);
		if(lastLongitude>0 && lastLatitude>0){
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lastLatitude,lastLongitude)));
		}
		points =  new ArrayList<LatLng>();//����λ���б�
		
		interestpoint=aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
	}
	/**
	* ����service�Ĳ���
	*/
	public void setUpService(){
		//ע�������̨��λ����Ĺ㲥�����ڸ��¹켣��ʾ
		myreveiver=new MyBroadcastReciver();//����һ��MyBroadcastReciver����
		IntentFilter filter=new IntentFilter();//����һ��IntentFilter����
		filter.addAction(MY_ACTION);//ΪIntentFilter���Action
		MainActivity.this.registerReceiver(myreveiver, filter);//ע��㲥
		//ע�ᾫ�ȼ����㲥
		accuracyReciver = new AccuracyBroadcastReciver();
		IntentFilter accuracyfilter=new IntentFilter();//����һ��IntentFilter����
		accuracyfilter.addAction(ACCURACY_ACTION);//ΪIntentFilter���Action
		MainActivity.this.registerReceiver(accuracyReciver, accuracyfilter);//ע��㲥
		
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
		 * ��TabActivy��TabHost�е�Activity�����ҪbindService�Ļ�
		 * ����Ҫ�ȵ���getApplicationContext()��ȡ��������Activity�������Ļ�����������bindService
		 */
		/**
		PostTimeValues gettime = new PostTimeValues(handler, URL_GET4TIME,
				Common.getUserId(getApplicationContext()));
		gettime.start();
		*/
		if (stepThread == null) {

			stepThread = new Thread() {// ���߳����ڼ�����ǰ�����ı仯

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
							handler.sendMessage(msg);// ֪ͨ���߳�
						}
					}
				}
			};
			
		}
		//startService(locService);
		/**
		 �ж��ϴι켣��¼�Ƿ������жϣ�����������жϣ������û��Ƿ������¼
		 */
		
		traceNo=helper.getUnStopStatusExists(Common.getUserId(this));
		if(traceNo!=0){//�����жϵĹ켣,0�ǹ켣��
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
		else{//û���жϵĹ켣
			
		}
		boolean isShowBGRGuide = sp.getBoolean("isShowBGRGuide", true);
		if(isShowBGRGuide){
			//ָ���û������Ӱ�����
			showBGRunGuide();
		}
	}
	/**
	public void initTime(){
		if(timevalues==null){
			//traceService.getToWork();//��ʱ�ӵģ�Ҫɾ
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
		public void onServiceConnected(ComponentName componentName, IBinder ibinder) {//���ӽ����ɹ�ʱ����
			
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
//			public void onServiceConnected(ComponentName componentName, IBinder ibinder) {//���ӽ����ɹ�ʱ����
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
				
				mListener.onLocationChanged(Common.aLocation);// ��ʾϵͳС����
				//Log.i("trailadapter", "main----onLocationChanged");
				//currentAlocation=aLocation;
				currentLatLng=new LatLng(Common.aLocation.getLatitude(),Common.aLocation.getLongitude());
				
/*--------------------------------------------author:zhanghao 2017-7-------------------------------------------------*/
				//�ڵ�ͼҳ����ʾ��ǰλ�õľ�γ����Ϣ
				currentLongitude = Common.aLocation.getLongitude();	//����
				currentLatitude = Common.aLocation.getLatitude();	//γ��
				currentAltitude = Common.aLocation.getAltitude();	//����
				
				currentAltitudeStr = String.valueOf(currentAltitude);
				BigDecimal currentLongitudeTemp = new BigDecimal(currentLongitude);
				currentLongitude1 = currentLongitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				BigDecimal currentLatitudeTemp = new BigDecimal(currentLatitude);
				currentLatitude1 = currentLatitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				BigDecimal currentAltitudeTemp = new BigDecimal(currentAltitude);
				currentAltitude1 = currentAltitudeTemp.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				degreeLngArr = String.valueOf(currentLongitude).split("[.]");	//���㾭��
				degreeLngStr = degreeLngArr[0];									//��
				minuteLng1 = Double.parseDouble("0."+degreeLngArr[1])*60;
				minuteLngArr = String.valueOf(minuteLng1).split("[.]");
				minuteLngStr = minuteLngArr[0];									//��
				secondLng1 = Double.parseDouble("0."+minuteLngArr[1])*60;
				secondLngArr = String.valueOf(secondLng1).split("[.]");
				secondLngStr = secondLngArr[0];									//��
				
				degreeLatArr = String.valueOf(currentLatitude).split("[.]");	//����γ��
				degreeLatStr = degreeLatArr[0];									//��
				minuteLat1 = Double.parseDouble("0."+degreeLatArr[1])*60;
				minuteLatArr = String.valueOf(minuteLat1).split("[.]");
				minuteLatStr = minuteLatArr[0];									//��
				secondLat1 = Double.parseDouble("0."+minuteLatArr[1])*60;
				secondLatArr = String.valueOf(secondLat1).split("[.]");
				secondLatStr = secondLatArr[0];									//��
				//��ʾ��ǰλ�õľ�γ��
				/*if(currentLongitude > 0){
					text_longitude.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "E"+"��");
				}else if(currentLongitude < 0){
					text_longitude.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "W"+"��");
				}
				if(currentLatitude > 0){
					text_latitude.setText("γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "N");
				}else if(currentLatitude < 0){
					text_latitude.setText("γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "S");
				}*/
				/*if(currentLongitude > 0 && currentLatitude > 0){
					text_location.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "E��"
							+"γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "N");
				}
				if(currentLongitude > 0 && currentLatitude < 0){
					text_location.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "E��"
							+"γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "S");
				}
				if(currentLatitude < 0 && currentLatitude > 0){
					text_location.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "W��"
							+"γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "N");
				}
				if(currentLatitude < 0 && currentLatitude < 0){
					text_location.setText("���ȣ�"+degreeLngStr + "��" + minuteLngStr + "��" + secondLngStr + "��" + "W��"
							+"γ�ȣ�"+degreeLatStr + "��" + minuteLatStr + "��" + secondLatStr + "��" + "S");
				}*/
				if(currentAltitude>0){					
					if(currentLongitude > 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E��"+
					getResources().getString(R.string.latitude)+currentLatitude1+"N��"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLongitude > 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E��"+
					getResources().getString(R.string.latitude)+currentLatitude1+"S��"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLatitude < 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W��"+
					getResources().getString(R.string.latitude)+currentLatitude1+"N��"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
					if(currentLatitude < 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W��"+
					getResources().getString(R.string.latitude)+currentLatitude1+"S��"+getResources().getString(R.string.altitude)+currentAltitude1+"m");
					}
				}else{
					if(currentLongitude > 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E��"
					+getResources().getString(R.string.latitude)+currentLatitude1+"N");
					}
					if(currentLongitude > 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"E��"
					+getResources().getString(R.string.latitude)+currentLatitude1+"S");
					}
					if(currentLatitude < 0 && currentLatitude > 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W��"
					+getResources().getString(R.string.latitude)+currentLatitude1+"N");
					}
					if(currentLatitude < 0 && currentLatitude < 0){
						text_location.setText(getResources().getString(R.string.longitude)+currentLongitude1+"W��"
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
					if(isstart&&isend){//�����µĹ켣��¼
						//aMap.clear();
						//clearTrace();
						
						isend=false;
						
						//Log.i("LogDemo", "����");
					}
					if(refreshTrace()){
						if(!points.isEmpty()){
							points.clear();
						}
						for(int i=0;i<tracegps.size();i++){
							LatLng point = new LatLng(tracegps.get(i).getLatitude(),tracegps.get(i).getLongitude());
							points.add(point);
							if(points.size() >1){
								if(point.equals(points.get(points.size() -2))){//�¼�¼�ĵ�����һ������ͬ��ɾ��
										points.remove(points.size() -1);
										//Log.i("LogDemo", "removepoint");
								}	
								finalLatLng=points.get(points.size() -1);
							}
						}
						tracegps.clear();
						drawPoints(points);//�ߵ»��ƹ켣
						//bdmap.drowTrace(points,0);//�ٶȻ��ƹ켣,������յ�
					}			
				}
			}
	
		}
	}
	// ��ͼ������·���滮�Ի���
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
	// ��ͼ������·���滮�Ի�������
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

			
			options = new PolylineOptions().width(15).geodesic(true).color(Color.GREEN);//��ʼ���켣����Color.argb(255, 100, 100, 100)��ɫ
			
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
	// �����ͼ����ʱ������
	public void clearTrace2(){
		if(polyline2!=null){
			polyline2.remove();
			points2.clear();
			polyline2 = null;
		}
	}
	/**
	* ��λ�ɹ���ص�����
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
//			mListener.onLocationChanged(aLocation);// ��ʾϵͳС����
//			Log.i("trailadapter", "main----onLocationChanged");
//			currentAlocation=aLocation;
//			currentLatLng=new LatLng(aLocation.getLatitude(),aLocation.getLongitude());
//			
//		}
//		else{
//			String errText = "��λʧ��," + aLocation.getErrorCode()+ ": " + aLocation.getErrorInfo();
//			//Log.e("AmapErr",errText);
//		}
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		/**case R.id.imgbtn_layer:
			changeLayer();
			break;*/
		//�л���ͼͼ��
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
						aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ʸ����ͼģʽ
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						break;
					case 1:
						dialog.dismiss();
						aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// ���ǵ�ͼģʽ
						if(tileOverlay!=null){
							tileOverlay.remove();
							//tileOverlay.clearTileCache();
						}
						break;
					case 2:
						dialog.dismiss();
						//���õ�ͼ��Ƭ��ʾ�ȸ��׼��ͼ
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
						//�ȸ����ǵ�ͼ
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
						//�ȸ����ǵ�ͼ
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
//						aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ʸ����ͼģʽ
//						break;
//					case 1:
//						dialog.dismiss();
//						aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// ���ǵ�ͼģʽ
//						break;
//					}
//				}
//			});
			builder2.create().show(); // �����Ի�����ʾ
			break;
/*-----------------------------------------author:zhanghao 2017-7-------------------------------------------------*/
		//Ϊ�����Ӳ�����·���滮�Ĺ���
		//�����䰴ť
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
		//�˳�����ģʽ
		case R.id.iv_return:
			if(distanceMode){				
				showDistance.setVisibility(View.INVISIBLE);
				showTips.setVisibility(View.INVISIBLE);
				distanceMode = false;
				if(marker != null){								//����������ڲ��ĵ���
					for(int i = markList.size()-1;i>=0;i--){
						markList.get(i).remove();
						markList.remove(i);
					}
				}
				if(polyline2 != null){							//����������ڲ����߱��
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
				if(marker != null){								//����������ڲ��ĵ���
					for(int i = markList.size()-1;i>=0;i--){
						markList.get(i).remove();
						markList.remove(i);
					}
				}
				if(polyGon != null){							//����������ڲ�����Ķ���α��
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
					text_calculate.setText("�ܾ��룺" + String.valueOf(totalDistance) + "m");
				}else{
					text_calculate.setText("�ܾ��룺" + String.valueOf(totalDistance/1000) + "km");
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
					text_calculate.setText("�������" + String.valueOf(totalArea) + "ƽ����");
				}else{
					text_calculate.setText("�������" + String.valueOf(totalArea/1000000) + "ƽ������");
				}
			}
			break;*/
/*------------------------------------------------------------------------------------------------------------------*/
		case R.id.imgbtn_starttrail:
			if(Common.isVisiter()){
				Common.DialogForVisiter(MainActivity.this);
				return;
			}	
			if(isend){//�����µļ�¼
				//Toast.makeText(MainActivity.this, "��ʼ�¼�¼",Toast.LENGTH_SHORT).show();
				startTrail();			
			}
			else{//������¼
				Toast.makeText(MainActivity.this, "������¼",Toast.LENGTH_SHORT).show();
				traceService.changeTraceNo(traceNo);
				traceService.changeStatus(true);
				if(tracedata.getSportType()==1){
					//�켣����Ϊ���У���¼����
					traceService.changeSportType(true);
				}
				/**
				if(timevalues!=null){
					traceService.changeGpsTime(timevalues.getRecTime());//�ɼ�ʱ���Ϊ��¼ʱ�Ĳɼ�ʱ�䣬�ӿ�
				
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
			Toast.makeText(MainActivity.this, "��ͣ��¼",Toast.LENGTH_SHORT).show();
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
				//�켣����Ϊ���У���ͣ��¼����
				traceService.changeSportType(false);
			}
			isstart=false;
			ispause=true;
			isend=false;
			
			//Log.i("LogDemo", "��ͣ��¼");
			break;
		case R.id.imgbtn_endtrail:
			//Toast.makeText(MainActivity.this, "������¼",Toast.LENGTH_SHORT).show();
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
			
			//Log.i("LogDemo", "������¼");
			break;
		case R.id.imgbtn_takephoto:
			if(Common.isVisiter()){
				Common.DialogForVisiter(MainActivity.this);
				return;
			}
			//��POI���ݿ���ȡ����
			ArrayList<String> behaviour = helper2.getBehaviour();
			ArrayList<String> duration = helper2.getDuration();
			ArrayList<String> partnerNum = helper2.getPartnerNum();
			ArrayList<String> relation = helper2.getRelation();
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, CommentActivity.class);
			/**
			 * ���ݾ�γ�� ���� traceno���ص�
			 * */
			if(Common.aLocation!=null){//���Ȳ���gpsλ��
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
			//������Ȥ�����ݣ��ַ������飩�������Ȥ��ҳ��
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
		// ׼��listview������
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
							marker = aMap.addMarker(markerOption);//���һ����ע
							markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
							
							points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
							options2 = new PolylineOptions().width(15).geodesic(true).color(Color.GREEN);//�����߶εĿ�ȡ���ɫ
							options2.addAll(points2);
							polyline2 = aMap.addPolyline(options2);//����һ���߶�
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
			aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ʸ����ͼģʽ
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
					 * ���ù켣��Ϣ
					 * */
					traceNo=System.currentTimeMillis();
					tracedata.setTraceName(dialog.gettraceName());
					tracedata.setSportType(pos);
					tracedata.setStartTime(Common.currentTime());
					initStartInfo();
					
					ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_starttrace));
					ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_addmark));
					//showDialog("��������..","������..���Ժ�....");
					//PostStartTrail startTrailThread=new PostStartTrail(handler,URL_STARTTRAIL,Common.userId);
					//startTrailThread.start();
				}
				else{
					//Toast.makeText(MainActivity.this, "ȡ��", 0).show();
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
		Log.i("LogDemo", "starttrail,traceNo��"+traceNo+",id:"+Common.getUserId(this));
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
			//�켣����Ϊ���У���¼����
			traceService.changeSportType(true);
			
			StepDetector.CURRENT_SETP =helper.querryformstepsbyTraceNo(traceNo,Common.getUserId(this)).getsteps();
			
			total_step=StepDetector.CURRENT_SETP;
			startService(stepService);
			iscountstep=true;
			new Thread(stepThread).start();
			text_step.setVisibility(View.VISIBLE);
			text_step.setText(getResources().getString(R.string.steplable1)+"��"+total_step);
		}
		/**
		if(timevalues!=null){
			traceService.changeGpsTime(timevalues.getRecTime());//�ɼ�ʱ���Ϊ��¼ʱ�Ĳɼ�ʱ�䣬�ӿ�
		
		}
		else{
			traceService.changeGpsTime(5);
		}*/
		//traceService.changeGpsTime(Common.getRecLocFrequenct(getApplicationContext()));
	}
	public void endTrail(){
		/**
		 * ���ؼ�¼trace���ݣ��Ǿ���λ��
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
			//�켣����Ϊ���У������켣ʱiswalk��Ϊfalse��ֹͣ��¼����
			iscountstep=false;//�����߳�
			traceService.changeSportType(false);//�Ƿ�������Ϊ��
			stopService(stepService);//�����Ʋ�����
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
			try{ //����ʱ���
				Date d1 = df.parse(tracedata.getStartTime()); 
				Date d2 = df.parse(tracedata.getEndTime()); 
				duration = d2.getTime() - d1.getTime(); 
				
			}catch (Exception e){
				return false;
			} 
			tracedata.setDuration(duration);
			//�������
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
			//ToastUtil.show(MainActivity.this, "δ�ɼ���λ����Ϣ���켣������");
			
			return false;
		}
	}
	/**
	 * ʵ�ʵĲ���
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
		//���أ�kg�������루�����1.036 
		return (int)(60*1.036*distance/1000);
	}
	private int calculateCalorie_Ride(double distance,long duration){
		//ʱ��(km/h)������(kg)��1.05���˶�ʱ��(h)
		return (int)(60*1.05*distance/1000);
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {

			switch(msg.what){
			case 0://��ȡ�켣�ųɹ�
				//dismissDialog();
				//if(msg.obj.toString().trim()!=null&&msg.obj.toString().trim()!=""){
					
				//}
				//ToastUtil.show(MainActivity.this, "��ʼ��¼");
				break;
			case 1://��ȡ�켣��ʧ��
				//dismissDialog();
				//Toast.makeText(MainActivity.this, "��ʼʧ�ܣ�������",Toast.LENGTH_SHORT).show();
				break;
			case 2://������¼�켣�ɹ�
				dismissDialog();
				
				helper.updateStatus( traceNo, 0,Common.getUserId(MainActivity.this));
				helper.deleteStatus();
				//�ƶ������켣���ѣ���ʾ�û�����ˢ��
				Intent intent = new Intent();  
		        intent.setAction(PULLREFRESH_ACTION);  
		        sendBroadcast(intent);
				ToastUtil.show(MainActivity.this,getResources().getString(R.string.tips_recordsuccess));
				break;
			case 3://������¼�켣ʧ��
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
			case 4://��ȡ4��ʱ��ɹ�
				/**
				if(msg.obj.toString().trim()!=null&&msg.obj.toString().trim()!=""){
					//Gson gson=new Gson();
					//timevalues=GsonHelper.fromJson(msg.obj.toString().trim(),new TypeToken<TimeValue>(){}.getType());
					timevalues=GsonHelper.parseJson(msg.obj.toString().trim(), TimeValue.class);
					//�����ڴ˴��������²�����ԭ��service������δ����,....Ҳ�п��������ѽ���
					if(bound_trace&&!istimeset){//service�ѽ�������������û��ȡʱ������δ����ʱ��
						initTime();
					}
				}
				*/
				//Toast.makeText(MainActivity.this, "",Toast.LENGTH_SHORT).show();
				break;
			case 5://��ȡ4��ʱ��ʧ��
				/**
				if(!traceService.isworking()){
					traceService.getToWork();
				}
				Log.i("LogDemo", "��ȡ4��ʱ��ʧ�ܣ�����Ĭ��ʱ��");
				*/
				break;
			case 9://���²���
				countStep();
				text_step.setText(getResources().getString(R.string.steplable1)+"��"+total_step);
				if(tracedata.getSportType()==1){
					stepdata.setsteps(total_step);
					
					helper.updatesteps(stepdata, traceNo,Common.getUserId(MainActivity.this));
					
				}
				break;
			case 10:
				dismissDialog();
				
				break;
			case 11://endtrail �ϴ�ʧ�ܣ�ԭ������δ����
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
				//Log.i("LogDemo","�и���,"+ url);
				if(updatestr.length>=5){
					String version=updatestr[0];
					String time=updatestr[1];
					String url=updatestr[2];
					String detail=updatestr[3];
					String size=updatestr[4];
					Log.i("LogDemo","�и���,"+version+size+time+ url+detail);
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
							// ֪ͨService��ʼ����
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
				//Log.i("phonelog", "�Զ���⣬��������,"+msg.obj.toString().trim());
				break;
			case 2://
				//Log.i("phonelog","������ʧ��,"+ msg.obj.toString().trim());
				break;
			case 10:
				//Log.i("phonelog","�������쳣,"+ msg.obj.toString().trim());
				break;
			}
		}
	};
	//���ݸ�PostPointOfInterestData��handler1
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
								//�����ݲ��뵽POI���ݿ���
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
	 * ��������Activity�ķ��ؽ��
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
			// �������Activity���صĽ�������ݸý������ͼƬ�ϴ�
			case REQUSET_COMMENT:
				
				// �����wifi���磬ֱ�ӿ�ʼ�ϴ�
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
				}// ����������������ӣ���һ��ʹ��ѯ���Ƿ��ϴ�
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
			default:
				break;
			}
		}
	}

	

	/*@Override
	public void onInfoWindowClick(Marker marker) {
		ToastUtil.show(this, "������infoWindow����" + marker.getTitle());
	}*/

	@Override
	public boolean onMarkerClick(Marker marker) {
		//ToastUtil.show(this,marker.getTitle());
		return false;
	}

	/**
	 * ��ͼ����¼������ӿڵ�ʵ�ַ���
	 */
	@Override
	public void onMapClick(LatLng latlng) {
		/*Log.i("LogDemo", "onmapclick:("+latlng.latitude+","+latlng.longitude+")");
		showDialog("","���ڻ�ȡ��ַ");
		latLonPoint=new LatLonPoint(latlng.latitude, latlng.longitude);
		final LatLonPoint finallatLonPoint=latLonPoint;
		RegeocodeQuery query = new RegeocodeQuery(finallatLonPoint, 200,
				GeocodeSearch.AMAP);// ��һ��������ʾһ��Latlng���ڶ�������ʾ��Χ�����ף�������������ʾ�ǻ�������ϵ����GPSԭ������ϵ
		geocoderSearch.getFromLocationAsyn(query);// ����ͬ��������������
		*/
		if(distanceMode){
			if(points2.size() == 0){
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng).title("���");
				marker = aMap.addMarker(markerOption);//���һ����ע
				markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
				points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
			}else{
				showTips.setVisibility(View.INVISIBLE);
				points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
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
				marker = aMap.addMarker(markerOption);//���һ����ע
				marker.showInfoWindow();
				markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
				/*for(int i = 0;i<markList.size();i++){				
				markList.get(i).showInfoWindow();
			}*/
				
				options2 = new PolylineOptions().width(15).geodesic(true).color(Color.argb(255, 184, 49, 170));//�����߶εĿ�ȡ���ɫ
				options2.addAll(points2);
				polyline2 = aMap.addPolyline(options2);//����һ���߶�
				polylineList.add(polyline2);
			}
		}
		
		if(areaMode){
			if(points2.size() <= 1){
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng);
				marker = aMap.addMarker(markerOption);//���һ����ע
				markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
				points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
			}else if(points2.size() == 2){
				showTips.setVisibility(View.INVISIBLE);
				markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latlng);
				marker = aMap.addMarker(markerOption);//���һ����ע
				markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
				
				points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
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
				area = (float) Math.sqrt(s*(s-a)*(s-b)*(s-c));//����-�ؾ��ع�ʽ-��֪���������������
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
				marker = aMap.addMarker(markerOption);//���һ����ע
				markList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
				
				points2.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�points2������
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
	 * ��������ص�
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		dismissDialog();
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				addressName = result.getRegeocodeAddress().getCity()+","+result.getRegeocodeAddress().getPois().get(0)
						+ "����";
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
	 * �Զ���info window����
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
				ToastUtil.show(MainActivity.this, "���������۰�ť");
			}
			
		});*/
	}
	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//menu.add(0, MENU_ROUTE, 1, "·�߹滮");
		//menu.add(0, MENU_NAVI, 2, "����");
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
	* ���λ
	*/
	@Override
	public void activate(OnLocationChangedListener listener) {
		
		mListener = listener;
		/**
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(getApplicationContext());
			mLocationOption = new AMapLocationClientOption();
			//���ö�λ����
			mlocationClient.setLocationListener(this);
			//����Ϊ�߾��ȶ�λģʽ
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			//���ö�λʱ����
			mLocationOption.setInterval(Common.getRecLocFrequenct(getApplicationContext())*1000);
			//���ö�λ����
			mlocationClient.setLocationOption(mLocationOption);
			// �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
			// ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����stopLocation()������ȡ����λ����
			// �ڶ�λ�������ں��ʵ��������ڵ���onDestroy()����
			// �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������stopLocation()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
			mlocationClient.startLocation();
			
		}
		*/
		
		/*if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
			//mAMapLocationManager.setGpsEnable(false);
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2�汾��������������true��ʾ��϶�λ�а���gps��λ��false��ʾ�����綨λ��Ĭ����true Location
			 * API��λ����GPS�������϶�λ��ʽ
			 * ����һ�������Ƕ�λprovider���ڶ�������ʱ�������2000���룬������������������λ���ף����ĸ������Ƕ�λ������
			 */
			/*mAMapLocationManager.requestLocationData(
					LocationProviderProxy.AMapNetwork, 10000, 20, this);
		}*/
	}
	
	/**
	* ֹͣ��λ
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
	private void showBGRunGuide() {//��ʾ�û���Ӧ�ü��뱣������
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
	private void showDlg_badloc() {//�޷���λʱ������ʾ
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

                        // ת���ֻ����ý��棬�û�����GPS
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0); // ������ɺ󷵻ص�ԭ���Ľ���
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
	//�;���ʱ������ʾ�Ի����û�ѡ���Ƿ���õ;���λ��
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
	 * ��ʾ�������Ի���
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
	 * ���ؽ������Ի���
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}

	
	/**
	* ����������д
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
	* ����������д
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
//	 * ѡ��ʸ����ͼ�����ǵ�ͼ�¼�����Ӧ
//	 */
//	private void setLayer(String layerName) {
//		if (layerName.equals(getString(R.string.normal))) {
//			aMap.setMapType(AMap.MAP_TYPE_NORMAL);// ʸ����ͼģʽ
//		} else if (layerName.equals(getString(R.string.satellite))) {
//			aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// ���ǵ�ͼģʽ
//		}
//	}
//	@Override
//	public void onNothingSelected(AdapterView<?> parent) {
//		
//	}
	private void initPOI(){
		//�ӷ���������ͣ��ʱ������Ϊ���͡�ͬ����������ϵ��ѡ�������
		PostPointOfInterestData pointOfInterest = new PostPointOfInterestData(handler1, URL_GETPOI);
		pointOfInterest.start();
	}
	private void initPOIEN(){
		//Ӣ�İ�
		PostPointOfInterestDataEn pointOfInterestEn = new PostPointOfInterestDataEn(handler1, URL_GETPOI);
		pointOfInterestEn.start();
	}
	private void initPOICZ(){
		//Ӣ�İ�
		PostPointOfInterestDataCz pointOfInterestCz = new PostPointOfInterestDataCz(handler1, URL_GETPOI);
		pointOfInterestCz.start();
	}
}