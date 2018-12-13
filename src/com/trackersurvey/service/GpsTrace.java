package com.trackersurvey.service;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.PhoneEvents;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TimeValue;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.TabHost_Main;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.WakeLockUtil;
import com.trackersurvey.httpconnection.PostEndTrail;
import com.trackersurvey.httpconnection.PostGpsData;
import com.trackersurvey.httpconnection.PostOnOffline;
import com.trackersurvey.httpconnection.PostPhoneEvents;
import com.trackersurvey.httpconnection.PostTimeValues;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GpsTrace extends Service implements AMapLocationListener{
	private final IBinder binder=new LocalBinder();
	private Handler msgshowhandler;
	private AlarmManager am;
	//private PowerManager.WakeLock wl = null;
	private WakeLockUtil wakeLockUtil;
	private PendingIntent pi;
	LocationReceiver locationReceiver=null;
	BringToFrontReceiver foregroundReceiver=null;
	private BroadcastReceiver connectionReceiver = null; // 用于监听网络状态变化的广播
	private BroadcastReceiver GPSStatusReceiver = null; // 用于接收gps状态变化的广播
	private ContentObserver mObserver;
	private TraceDBHelper helper;
	private SharedPreferences settings ;
	
	//private LocationManagerProxy mAMapLocationManager;旧版定位方法
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	private long lastRecordTime = 0;
	private long currentRecordTime = 0;
	private LatLng currentLatlng;//LatLng(double latitude, double longitude)，当前位置点
	private LatLng lastLatlng=new LatLng(0,0);//上一个位置点
	private AMapLocation lastgpsaloc=null;
	//private int count=0;//onlocationchange里距离变化小于5米时执行 +1，累计6次(1分钟)，无论距离变化如何，必上传。
	private int atomTime=1000;//单位时间，其他时间变量皆为此单位时间的倍数。
	//private int collectTime=10*atomTime;
	//private int uploadTime=30*atomTime;
	private int locFrequency=10;
	private int recFrequency=5;
	private float minNwalkDistance=5.0f;
	//private float maxNwalkDistance=500.0f;
	private float minWalkDistance=5.0f;
	//private float maxWalkDistance=50.0f;
	private float minDistance=5.0f;
	private float maxDistance=500.0f;
	//private int norecFrequency=10;
	private TimeValue timevalues=null;			//service时间参数
	private boolean isDraw=false;
	private boolean isWalk=false;
	private boolean isAlarmWork=false;
	private boolean isFirstLocated=true;
	private boolean isFirstRecord=true;
	private boolean isUpLoadThreadWork=false;
	private boolean isAccuracyAlarm=false;
	private String lastPostTime="2016-03-01 00:00:00";
	private ArrayList<GpsData> datalist=new ArrayList<GpsData>();
	private ArrayList<GpsData> datalist_up=new ArrayList<GpsData>();
	private List<TraceData> trails_up=new ArrayList<TraceData>();
	private TraceData trail_up=new TraceData();
	private List<Long> traceno_up=new ArrayList<Long>();
	
	private List<StepData> steps_up=new ArrayList<StepData>();
	private StepData step_up=new StepData();
	private ArrayList<PhoneEvents> eventdatalist=new ArrayList<PhoneEvents>();
	private String gpsData;
	private String gpsData_up;
	private String traceData_up;
	private String stepsdata_up;
	private String eventsdata;
	
	private long traceNo=0;
	private static final String MY_ACTION="android.intent.action.LOCATION_RECEIVER";
	private static final String ACCURACY_ACTION="android.intent.action.ACCURACY_RECEIVER";
	private  String URL_GPSDATA=null;
	private  String URL_EVENTDATA=null;
	private  String URL_ENDTRAIL=null;
	private  String URL_GET4TIME=null;
	private  final String TAG = "phonelog";
	private final int LOCATION_TYPE_GPS=1;
	
	
	Handler handler=new Handler();
	Handler mhandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 0://上传GPS成功
				Log.i("LogDemo","上传成功");
				if(datalist!=null&&datalist.size()>0){
					lastPostTime=datalist.get(datalist.size()-1).getcreateTime();
					Log.i("LogDemo","最近一次上传位置的时间:"+lastPostTime);
				}
				else{
					lastPostTime=Common.currentTime();
					Log.i("LogDemo","最近一次上传位置的时间:"+lastPostTime);
				} 
				datalist=null;
				Common.setPostTime(getApplicationContext(), lastPostTime);
				break;
			case 1://上传GPS失败
				Log.i("LogDemo","上传失败");
				datalist=null;
				//showmessage("上传失败");
				break;
			case 2://上传未上传过的轨迹成功
				Log.i("LogDemo","未上传过的轨迹上传成功");
				
				for(int i=0;i<traceno_up.size();i++){
					helper.updateStatus(traceno_up.get(i), 0,Common.getUserId(getApplicationContext()));
				}
				helper.deleteStatus();
				
				traceno_up=null;
				break;
			case 3://上传未上传过的轨迹失败
				Log.i("LogDemo","未上传过的轨迹上传失败");
				traceno_up=null;
				break;
			case 4://上传EVENT成功
				Log.i("LogDemo","上传EVENT成功");
				//showmessage("上传失败");
				break;
			case 5://上传EVENT失败
				Log.i("LogDemo","上传EVENT失败");
				//showmessage("上传失败");
				break;
			case 10:
				Log.i("LogDemo","网络错误,上传位置数据失败"+msg.obj.toString());
				//showmessage("网络错误,上传位置数据失败");
				break;
			}
		}
	};
	Runnable runnable=new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//showmessage("开始上传");
			uploadGPS();
			sendHeartbeat();
			handler.postDelayed(this,
					Common.getUploadFrequenct(getApplicationContext())*atomTime);
		}
	};
	Runnable timeRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i("LogDemo", "获取4个时间");
			checkTimevalue();
			timeHandler.postDelayed(this,
					Common.getTimeUpdateFrequency(getApplicationContext())*atomTime);
		}
	};
	Handler timeHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 4:
				if(msg.obj.toString().trim()!=null&&msg.obj.toString().trim()!=""){
					timevalues=GsonHelper.parseJson(msg.obj.toString().trim(), TimeValue.class);
					if(recFrequency==timevalues.getRecTime()){
						//参数未改变
					}
					else{
						if(recFrequency!=timevalues.getRecTime()){
							//记录状态定位间隔变化，重新定位
							changeGpsTime(timevalues.getRecTime());
							
						}
						/*else if(!isDraw&&norecFrequency!=timevalues.getNoRecTime()) {
							//非记录状态下定位间隔变化，重新定位
							changeGpsTime(timevalues.getNoRecTime());
						}*/
						recFrequency=timevalues.getRecTime();
						//norecFrequency=timevalues.getNoRecTime();
					}
					changeLastTime(timevalues.getLastTime());
					Common.setFourTime(getApplicationContext(), timevalues);
					Log.i("LogDemo", "timeHandler,更新时间参数"+msg.obj.toString().trim());
				}
				break;
			case 5:
				Log.i("LogDemo", "获取4个时间失败，开启默认时间");
				break;
			case 11:
				Log.i("LogDemo", "获取时间参数发生网络异常"+msg.obj.toString().trim());
				break;
			}
		}
	};
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		 //Log.i("LogDemo", "oncreate");
		super.onCreate();
		//为content://sms的数据改变注册监听器  
		helper=new TraceDBHelper(this);
		mObserver=new SmsObserver(new Handler());
		GpsTrace.this.getContentResolver().registerContentObserver(Uri.parse  
                ("content://sms"), true,mObserver ); 
		
		//注册通知栏点击事件广播
		IntentFilter intentFile = new IntentFilter();
		intentFile.addAction("foreground");
		foregroundReceiver = new BringToFrontReceiver();
		GpsTrace.this.registerReceiver(foregroundReceiver, intentFile);
		//设置当前服务为前台服务，提高服务存活能力
		/*Intent notificationIntent = new Intent(this, TabHost_Main.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		*/
		Intent notificationIntent = new Intent();
		notificationIntent.setAction("foreground");
		Notification notification = new Notification.Builder(this)
				.setContentTitle(getText(R.string.notification_title))
				.setContentText(getText(R.string.notification_message))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
				.build();
		
		startForeground(0x110, notification);
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//获取上次时间间隔
		lastPostTime=settings.getString("LastPostTime", "2016-03-01 00:00:00");
		recFrequency=Common.getRecLocFrequenct(getApplicationContext());
		locFrequency=recFrequency;
		//norecFrequency=Common.getNoRecLocFrequenct(getApplicationContext());
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		URL_GPSDATA=Common.url+"upLocation.aspx";
		URL_EVENTDATA=Common.url+"upEvent.aspx";
		URL_ENDTRAIL=Common.url+"reqTraceNo.aspx";
		URL_GET4TIME=Common.url+"request.aspx";
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		 Log.i("LogDemo", "onstartcommand");
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void getToWork(){
		 
		 wakeLockUtil = new WakeLockUtil(this);
		 
		 IntentFilter intentFile = new IntentFilter();
		 intentFile.addAction("repeating");
		 locationReceiver = new LocationReceiver();
		 GpsTrace.this.registerReceiver(locationReceiver, intentFile);
		// 注册网络广播监听
			connectionReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
					if (netInfo!=null && netInfo.isConnected()) {
						// connect network,读取本地sharedPreferences文件，上传之前未完成上传的部分
						Log.i("phonelog","后台：有网络");
						Common.isNetConnected=true;
						//destoryMClient();
						if(mlocationClient==null){
							setupMClient();
							Log.i("phonelog", "后台启动定位");
						}
						if(!isUpLoadThreadWork){
							handler.postDelayed(runnable,3000);//1秒后执行一次runnable.
						}
						isUpLoadThreadWork=true;
						
						
						
					} else {
						Log.i("phonelog", "后台无网络");
						// unconnect network
						// do nothing
						Common.isNetConnected=false;
						if(!Common.checkGPS(context)){
							destoryMClient();
							Log.i("phonelog", "后台销毁定位");
						}
						handler.removeCallbacks(runnable);
						isUpLoadThreadWork=false;
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			GpsTrace.this.registerReceiver(connectionReceiver, intentFilter);
			//gps状态广播
			GPSStatusReceiver = new BroadcastReceiver(){

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					String action =  intent.getAction();
					if(action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
						if(Common.checkGPS(context)){
							Log.i("phonelog","gps开启");
							if(mlocationClient==null){
								setupMClient();
								Log.i("phonelog", "后台启动定位");
							}
						}
						else{
							Log.i("phonelog","gps关闭");
							if(!Common.isNetConnected){
								destoryMClient();
								Log.i("phonelog", "后台销毁定位");
							}
						}
					}
				}
				
			};
			IntentFilter intentFilter_gps = new IntentFilter();
			intentFilter_gps.addAction(LocationManager.MODE_CHANGED_ACTION);
			intentFilter_gps.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
			GpsTrace.this.registerReceiver(GPSStatusReceiver, intentFilter_gps);
			
		 Intent intent = new Intent();  
		 intent.setAction("repeating"); 
	     pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	     
	     am = (AlarmManager)getSystemService(ALARM_SERVICE);
	     am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 300000, pi);//亲测，在红米1（4.2）上间隔为5分钟，小于5分钟的设置无效，在红米（4.4）上设置有效。
	     wakeLockUtil.acquireWakeLock();
	     isAlarmWork=true;
	}
	public void uploadGPS(){
//		if(Common.getUserId(getApplicationContext()).length()<10){
//			return;
//		}
		if(!Common.isWiFiConnected){
			if(Common.isOnlyWifiUploadLoc(getApplicationContext())){
				Log.i("LogDemo","设置仅wifi上传，当前非wifi连接");
				return;
			}
		}
		Log.i("LogDemo","周期上传数据,from："+lastPostTime);
		
		datalist=helper.queryfromGpsbylasttime(lastPostTime,Common.getUserId(getApplicationContext()));
		eventdatalist=helper.queryfromEventsbylasttime(lastPostTime,Common.getUserId(getApplicationContext()));
		traceno_up=helper.getUnUploadStatusExists(Common.getUserId(getApplicationContext()));
				
		if(datalist.size()>0){
			
			gpsData=GsonHelper.toJson(datalist);
			PostGpsData gpsDataThread=new PostGpsData(mhandler,URL_GPSDATA,gpsData,Common.getDeviceId(getApplicationContext()));
			gpsDataThread.start();
			gpsData=null;
		}else{
			Log.i("LogDemo","没有数据，上传指定字符串");
			PostGpsData gpsDataThread=new PostGpsData(mhandler,URL_GPSDATA,Common.getUserId(getApplicationContext()),"GPSisNull",Common.getDeviceId(getApplicationContext()));
			gpsDataThread.start();
		}
		
		if(eventdatalist.size()>0){
			Log.i("LogDemo","有事件");
			eventsdata=GsonHelper.toJson(eventdatalist);
			PostPhoneEvents eventsDataThread=new PostPhoneEvents(mhandler,URL_EVENTDATA,eventsdata,Common.getDeviceId(getApplicationContext()));
			eventsDataThread.start();
			eventdatalist=null;
			eventsdata=null;
		}
		
		if(traceno_up.size()>0){
			for(int i=0;i<traceno_up.size();i++){
				trail_up=helper.queryfromTrailbytraceNo(traceno_up.get(i),Common.getUserId(getApplicationContext()));
				trails_up.add(trail_up);
				if(trail_up.getSportType()==1){
					step_up=helper.querryformstepsbyTraceNo(traceno_up.get(i),Common.getUserId(getApplicationContext()));
					steps_up.add(step_up);
					step_up=null;
				}
				trail_up=null;
				datalist_up=helper.queryfromGpsbytraceNo(traceno_up.get(i),Common.getUserId(getApplicationContext()));
				gpsData_up=GsonHelper.toJson(datalist_up);
				PostGpsData gpsDataThread=new PostGpsData(mhandler,URL_GPSDATA,gpsData_up,Common.getDeviceId(getApplicationContext()));
				gpsDataThread.start();
				datalist_up=null;
				gpsData_up=null;
			}
			traceData_up=GsonHelper.toJson(trails_up);
			stepsdata_up="";
			if(steps_up.size()>0){
				stepsdata_up=GsonHelper.toJson(steps_up);
			}
			Log.i("trailadapter","自动上传的轨迹："+traceData_up+","+stepsdata_up);
			PostEndTrail endTrailThread=new PostEndTrail(mhandler,URL_ENDTRAIL,traceData_up,stepsdata_up,Common.getDeviceId(getApplicationContext()));
			endTrailThread.start();
			trails_up.clear();
			steps_up.clear();
			traceData_up=null;
			stepsdata_up=null;
		}
		
		
	}
	public void checkTimevalue(){
		String location = "";
		if(currentLatlng!=null){
			location = currentLatlng.longitude+";"+currentLatlng.latitude;
		}	
		Log.i("LogDemo","请求时间参数，位置信息"+location);
		PostTimeValues gettime = new PostTimeValues(timeHandler, URL_GET4TIME,
					Common.getUserId(getApplicationContext()),location,Common.getDeviceId(getApplicationContext()));
		gettime.start();
		
	}
	public void sendOnline(){
//		if(Common.getUserId(getApplicationContext()).length()<10){
//			return;
//		}
		String location = "";
		if(currentLatlng!=null){
			location = currentLatlng.longitude+";"+currentLatlng.latitude;
		}	
		String userid=Common.getUserId(getApplicationContext());
		Log.i("LogDemo","上线，"+userid+",位置信息"+location);
		PostOnOffline online = new PostOnOffline(URL_GET4TIME,
				userid,location,"Online",Common.getDeviceId(getApplicationContext()));
		online.start();
		
	}
	public void sendHeartbeat(){
//		if(Common.getUserId(getApplicationContext()).length()<10){
//			return;
//		}
		String location = "";
		if(currentLatlng!=null){
			location = currentLatlng.longitude+";"+currentLatlng.latitude;
		}	
		String userid=Common.getUserId(getApplicationContext());
		Log.i("LogDemo","心跳包，"+userid+",位置信息"+location);
		if(Common.url_heart == null ||Common.url_heart.equals("")){
			Common.url_heart = getResources().getString(R.string.url_heart);
		}
		PostOnOffline heart = new PostOnOffline(Common.url_heart,
				userid,location,"HeartBeat",Common.getDeviceId(getApplicationContext()));
		heart.start();
		
	}
	public void setupMClient(){
		mlocationClient = new AMapLocationClient(GpsTrace.this);
		mLocationOption = new AMapLocationClientOption();
		//设置定位监听
		mlocationClient.setLocationListener(GpsTrace.this);
		//设置为高精度定位模式
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		mLocationOption.setSensorEnable(true);
		mLocationOption.setInterval(locFrequency*atomTime);
		//设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
		Log.i("backloc", "setupMClient,interval:"+locFrequency);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用onDestroy()方法
		// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
		mlocationClient.startLocation();
	}
	
	class LocationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.i("LogDemo", "LocationReceiver");
			if (mlocationClient == null) {
				if(Common.isNetConnected||Common.checkGPS(context)){
				setupMClient();
				Log.i("backloc", "LocationReceiver 启动定位");
				}
			}
			else{
				
				if(mlocationClient.isStarted()){}
				else{
					mlocationClient.startLocation();
					Log.i("backloc", "LocationReceiver mlocationClient is not started,start!");
				}
			}
		}
		
	} 
	class BringToFrontReceiver extends BroadcastReceiver {  
	    
	    public BringToFrontReceiver() {  
	    }  
	  
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        //获取ActivityManager  
	        ActivityManager mAm = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);  
	        //获得当前运行的task  
	        List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);  
	        for (ActivityManager.RunningTaskInfo rti : taskList) {  
	            //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台  
	            if(rti.topActivity.getPackageName().equals(context.getPackageName())) {  
	                mAm.moveTaskToFront(rti.id,0);  
	                return;  
	            }  
	        }  
	        //若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity  
	        Intent resultIntent = new Intent(context, TabHost_Main.class);  
	        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);  
	        context.startActivity(resultIntent);  
	  
	    }  
	}   
	public class LocalBinder extends Binder{
		public GpsTrace getService(){
			return GpsTrace.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		// Log.i("LogDemo", "onBind");
		return binder;
	}
	
	public void changeLastTime(String lasttime){
		lastPostTime=lasttime;
		Log.i("backloc", "lastPostTime->"+lastPostTime);
	}
	/**
	public void changeUploadTime(int time){
		uploadTime=time*atomTime;
		Log.i("LogDemo", "uploadTime->"+uploadTime);
	}
	*/
	public void changeGpsTime(int time){
		locFrequency=time;
		if(mlocationClient == null){}
		else{
			destoryMClient();
			if(Common.isNetConnected||Common.checkGPS(getApplicationContext())){
			setupMClient();
			Log.i("LogDemo", "changeGpsTime");
			}
		}
		Log.i("backloc", "定位间隔-->"+locFrequency);
	}
	
	public void changeStatus(boolean isDraw){
		//showmessage("画轨迹->"+isDraw);
		this.isDraw=isDraw;
		 if(isDraw){//点击开始记录轨迹后，获取到的第一个GPS点直接记录，不在比较与上一个点的距离
			destoryMClient();
			if(Common.isNetConnected||Common.checkGPS(getApplicationContext())){
				setupMClient();
			}
			lastLatlng=new LatLng(0,0);
			isFirstRecord=true;
			Common.isRecording=true;
		 }
		 else{
			 Common.isRecording=false;
		 }
		Log.i("backloc", "isDraw->"+isDraw);
	}
	public void changeSportType(boolean isWalk){
		this.isWalk=isWalk;
		 if(isWalk){
			 minDistance=minWalkDistance;
			 //maxDistance=maxWalkDistance;
		 }
		 else{
			 minDistance=minNwalkDistance;
			 //maxDistance=maxNwalkDistance;
		 }
		Log.i("backloc", "isWalk->"+isWalk);
	}
	public void changeTraceNo(long traceNo){
		//showmessage("轨迹号变动->"+traceNo);
		Log.i("backloc", "traceNo->"+traceNo);
		this.traceNo=traceNo;
	}
	public boolean isWorking(){
		return isAlarmWork;
	}
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if(Common.getUserId(getApplicationContext()).length()<10){
			isFirstLocated=true;
			Log.i("backloc", "id异常:"+Common.getUserId(getApplicationContext()));	
			return;
		}
		// TODO Auto-generated method stub
		 
		 if(aLocation != null&&aLocation.getErrorCode()==0){
			if(Common.isHighAccuracy&&aLocation.getAccuracy()>500.0){
					//Toast.makeText(getApplicationContext(), "精度超过500m", Toast.LENGTH_SHORT);
				Log.i("backloc", "精度超过500m");	
				if(!isAccuracyAlarm){//发广播 是否采用低精度
					isAccuracyAlarm=true;
					Intent aintent = new Intent();  
			        aintent.setAction(ACCURACY_ACTION);  
			        Log.i("backloc", "send accuracybroadcast");	
			        sendBroadcast(aintent);
				}
				return;
			}
			 Common.aLocation=aLocation;
//			 if(aLocation.getLocationType()==LOCATION_TYPE_GPS||!isWalk){
				 if(aLocation.getLocationType()==LOCATION_TYPE_GPS){
				 	 Log.i("backloc", "保存最新的gps点");
					 lastgpsaloc=aLocation;
				 }
				 currentRecordTime = System.currentTimeMillis();
				 currentLatlng = new LatLng(aLocation.getLatitude(), aLocation.getLongitude());
				 if(isFirstLocated){
					 timeHandler.postDelayed(timeRunnable,0);
					 sendOnline();
					 isFirstLocated=false;
				 }
				 float dis=AMapUtils.calculateLineDistance(currentLatlng, lastLatlng);
				 double speed = 0;//单位 m/s
				 if(!lastLatlng.equals(new LatLng(0,0))&&lastRecordTime > 0){
					 speed = dis/((currentRecordTime-lastRecordTime)/1000);
				 }
				 Log.i("backloc","速度："+speed);
				 if((dis>=minDistance&&speed<100.0)||isFirstRecord){//根据用户的起点和终点经纬度计算两点间距离，此距离为相对较短的距离，单位米。
				 //距离变化大于5米速度小于100米每秒时，记录
					 lastRecordTime = currentRecordTime;
					 lastLatlng=currentLatlng;
					 isFirstRecord=false;
					 
						 //Log.i("LogDemo", "采用网络获取的点");
					 if (aLocation.getLatitude() != 0 && aLocation.getLongitude() != 0){
						 GpsData data=new GpsData();
						 data.setuserId(Common.getUserId(getApplicationContext()));
						 data.setLongitude(aLocation.getLongitude());
						 data.setLatitude(aLocation.getLatitude());
						 data.setAltitude(aLocation.getAltitude());
						 data.setspeed(aLocation.getSpeed());
						 data.settraceNo(traceNo);
						 
						 if(helper.insertintoGps(data)==0){
							 //showmessage("成功插入一条位置记录");
							 Log.i("backloc","成功插入一条位置记录:经纬度：("+aLocation.getLongitude()+","
							 +aLocation.getLatitude()+")"+"海拔："+aLocation.getAltitude()+"速度:"
							 +aLocation.getSpeed()+"精度："+aLocation.getAccuracy());
						 }
						 else{
							 //showmessage("数据库异常");
							 Log.i("backloc","数据库异常");
						 }
					 }
					 
					
					//定位结果通知前台
						 Intent intent = new Intent();  
				         intent.setAction(MY_ACTION);  
				         sendBroadcast(intent);
			        
					
				 }
				 else{
					 
					 Log.i("backloc", "距离变化不足5米，不予记录");
				 }
		 }
		 else{
			 String errText = "后台定位失败," + aLocation.getErrorCode()+ ": " + aLocation.getErrorInfo();
				Log.e("AmapErr",errText);
			
			//Toast.makeText(getApplicationContext(), "后台定位失败", Toast.LENGTH_SHORT); 
		 }
		
	}
	 //一个继承自ContentObserver的监听器类  
    class SmsObserver extends ContentObserver{  
  
        public SmsObserver(Handler handler) {  
            super(handler);  
            // TODO Auto-generated constructor stub  
        }  
        @Override  
        public void onChange(boolean selfChange) {  
            // TODO Auto-generated method stub  
            //查询发送向箱中的短信  
        	Cursor cursor;
        	try{
        		cursor=getContentResolver().query(Uri.parse(  
	                    "content://sms/outbox"), null, null, null, null);  
        	}catch(SecurityException e){
        		return;
        	}
            //遍历查询结果获取用户正在发送的短信  
            while (cursor.moveToNext()) {  
                //StringBuffer sb=new StringBuffer();  
                //获取短信的发送地址  
                //sb.append("发送地址："+cursor.getString(cursor.getColumnIndex("address")));  
                //获取短信的标题  
                //sb.append("\n标题："+cursor.getString(cursor.getColumnIndex("subject")));  
                //获取短信的内容  
                //sb.append("\n内容："+cursor.getString(cursor.getColumnIndex("body")));   
                //获取短信的发送时间  
                Date date=new Date(cursor.getLong(cursor.getColumnIndex("date")));  
                //格式化以秒为单位的日期  
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
                //sb.append("\n时间："+sdf.format(date));  
                String timestr=sdf.format(date);
                Log.i("phonelog","查询到正在发送短信："+timestr);  
                saveEvents(timestr, 0);
                
            }  
            super.onChange(selfChange);  
        }  
          
    }
    public void saveEvents(String createTime,int EventType){
//    	if(Common.getUserId(getApplicationContext()).length()<10){
//			return;
//		}
		PhoneEvents event=null;
		if(Common.aLocation!=null){
			 event= new PhoneEvents(Common.getUserId(getApplicationContext()),createTime,EventType,
				Common.aLocation.getLongitude(),Common.aLocation.getLatitude(),Common.aLocation.getAltitude());
		}
		else{
			event= new PhoneEvents(Common.getUserId(getApplicationContext()),createTime,EventType,0,0,0);
		}
		
		if(helper.insertintoEvents(event)==0){
			if(Common.aLocation!=null){
				Log.i(TAG,"存入事件"+createTime+",类型:"+EventType+"("+Common.aLocation.getLongitude()+","+Common.aLocation.getLatitude()+")");
			}else{
				Log.i(TAG,"存入事件"+createTime+",类型:"+EventType);
			}
			 
		 }
		 else{
			 
			 Log.i(TAG,"数据库异常");
		 }
		
	}
	public void showmessage(final String str){
		
		msgshowhandler=new Handler(Looper.getMainLooper());
		msgshowhandler.post(new Runnable(){
		public void run(){
		Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
		}
		});
	}
	public void destoryMClient(){
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		handler.removeCallbacks(runnable);
		timeHandler.removeCallbacks(timeRunnable);
		if(isAlarmWork){
			am.cancel(pi);
			isAlarmWork=false;
		}
		if(locationReceiver!=null){
			GpsTrace.this.unregisterReceiver(locationReceiver);
			Log.i("LogDemo", "后台unregisterReceiver(locationReceiver)");
		}
		if(foregroundReceiver!=null){
			GpsTrace.this.unregisterReceiver(foregroundReceiver);
			Log.i("LogDemo", "后台unregisterReceiver(foregroundReceiver)");
		}
		if(connectionReceiver!=null){
			GpsTrace.this.unregisterReceiver(connectionReceiver);
			Log.i("LogDemo", "后台unregisterReceiver(connectionReceiver)");
		}
		if(GPSStatusReceiver!=null){
			GpsTrace.this.unregisterReceiver(GPSStatusReceiver);
			Log.i("LogDemo", "后台unregisterReceiver(GPSStatusReceiver)");
		}
		destoryMClient();
		if(wakeLockUtil!=null){
			wakeLockUtil.releaseWakeLock();
			Log.i("LogDemo", "releaseWakeLock");
		}
		GpsTrace.this.getContentResolver().unregisterContentObserver(mObserver);
		stopForeground(true);
		super.onDestroy();
		Log.i("LogDemo", "service关闭");
	}

	
}
