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
	private BroadcastReceiver connectionReceiver = null; // ���ڼ�������״̬�仯�Ĺ㲥
	private BroadcastReceiver GPSStatusReceiver = null; // ���ڽ���gps״̬�仯�Ĺ㲥
	private ContentObserver mObserver;
	private TraceDBHelper helper;
	private SharedPreferences settings ;
	
	//private LocationManagerProxy mAMapLocationManager;�ɰ涨λ����
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	private long lastRecordTime = 0;
	private long currentRecordTime = 0;
	private LatLng currentLatlng;//LatLng(double latitude, double longitude)����ǰλ�õ�
	private LatLng lastLatlng=new LatLng(0,0);//��һ��λ�õ�
	private AMapLocation lastgpsaloc=null;
	//private int count=0;//onlocationchange�����仯С��5��ʱִ�� +1���ۼ�6��(1����)�����۾���仯��Σ����ϴ���
	private int atomTime=1000;//��λʱ�䣬����ʱ�������Ϊ�˵�λʱ��ı�����
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
	private TimeValue timevalues=null;			//serviceʱ�����
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
			case 0://�ϴ�GPS�ɹ�
				Log.i("LogDemo","�ϴ��ɹ�");
				if(datalist!=null&&datalist.size()>0){
					lastPostTime=datalist.get(datalist.size()-1).getcreateTime();
					Log.i("LogDemo","���һ���ϴ�λ�õ�ʱ��:"+lastPostTime);
				}
				else{
					lastPostTime=Common.currentTime();
					Log.i("LogDemo","���һ���ϴ�λ�õ�ʱ��:"+lastPostTime);
				} 
				datalist=null;
				Common.setPostTime(getApplicationContext(), lastPostTime);
				break;
			case 1://�ϴ�GPSʧ��
				Log.i("LogDemo","�ϴ�ʧ��");
				datalist=null;
				//showmessage("�ϴ�ʧ��");
				break;
			case 2://�ϴ�δ�ϴ����Ĺ켣�ɹ�
				Log.i("LogDemo","δ�ϴ����Ĺ켣�ϴ��ɹ�");
				
				for(int i=0;i<traceno_up.size();i++){
					helper.updateStatus(traceno_up.get(i), 0,Common.getUserId(getApplicationContext()));
				}
				helper.deleteStatus();
				
				traceno_up=null;
				break;
			case 3://�ϴ�δ�ϴ����Ĺ켣ʧ��
				Log.i("LogDemo","δ�ϴ����Ĺ켣�ϴ�ʧ��");
				traceno_up=null;
				break;
			case 4://�ϴ�EVENT�ɹ�
				Log.i("LogDemo","�ϴ�EVENT�ɹ�");
				//showmessage("�ϴ�ʧ��");
				break;
			case 5://�ϴ�EVENTʧ��
				Log.i("LogDemo","�ϴ�EVENTʧ��");
				//showmessage("�ϴ�ʧ��");
				break;
			case 10:
				Log.i("LogDemo","�������,�ϴ�λ������ʧ��"+msg.obj.toString());
				//showmessage("�������,�ϴ�λ������ʧ��");
				break;
			}
		}
	};
	Runnable runnable=new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//showmessage("��ʼ�ϴ�");
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
			Log.i("LogDemo", "��ȡ4��ʱ��");
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
						//����δ�ı�
					}
					else{
						if(recFrequency!=timevalues.getRecTime()){
							//��¼״̬��λ����仯�����¶�λ
							changeGpsTime(timevalues.getRecTime());
							
						}
						/*else if(!isDraw&&norecFrequency!=timevalues.getNoRecTime()) {
							//�Ǽ�¼״̬�¶�λ����仯�����¶�λ
							changeGpsTime(timevalues.getNoRecTime());
						}*/
						recFrequency=timevalues.getRecTime();
						//norecFrequency=timevalues.getNoRecTime();
					}
					changeLastTime(timevalues.getLastTime());
					Common.setFourTime(getApplicationContext(), timevalues);
					Log.i("LogDemo", "timeHandler,����ʱ�����"+msg.obj.toString().trim());
				}
				break;
			case 5:
				Log.i("LogDemo", "��ȡ4��ʱ��ʧ�ܣ�����Ĭ��ʱ��");
				break;
			case 11:
				Log.i("LogDemo", "��ȡʱ��������������쳣"+msg.obj.toString().trim());
				break;
			}
		}
	};
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		 //Log.i("LogDemo", "oncreate");
		super.onCreate();
		//Ϊcontent://sms�����ݸı�ע�������  
		helper=new TraceDBHelper(this);
		mObserver=new SmsObserver(new Handler());
		GpsTrace.this.getContentResolver().registerContentObserver(Uri.parse  
                ("content://sms"), true,mObserver ); 
		
		//ע��֪ͨ������¼��㲥
		IntentFilter intentFile = new IntentFilter();
		intentFile.addAction("foreground");
		foregroundReceiver = new BringToFrontReceiver();
		GpsTrace.this.registerReceiver(foregroundReceiver, intentFile);
		//���õ�ǰ����Ϊǰ̨������߷���������
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
		//��ȡ�ϴ�ʱ����
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
		// ע������㲥����
			connectionReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
					if (netInfo!=null && netInfo.isConnected()) {
						// connect network,��ȡ����sharedPreferences�ļ����ϴ�֮ǰδ����ϴ��Ĳ���
						Log.i("phonelog","��̨��������");
						Common.isNetConnected=true;
						//destoryMClient();
						if(mlocationClient==null){
							setupMClient();
							Log.i("phonelog", "��̨������λ");
						}
						if(!isUpLoadThreadWork){
							handler.postDelayed(runnable,3000);//1���ִ��һ��runnable.
						}
						isUpLoadThreadWork=true;
						
						
						
					} else {
						Log.i("phonelog", "��̨������");
						// unconnect network
						// do nothing
						Common.isNetConnected=false;
						if(!Common.checkGPS(context)){
							destoryMClient();
							Log.i("phonelog", "��̨���ٶ�λ");
						}
						handler.removeCallbacks(runnable);
						isUpLoadThreadWork=false;
					}
				}
			};
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			GpsTrace.this.registerReceiver(connectionReceiver, intentFilter);
			//gps״̬�㲥
			GPSStatusReceiver = new BroadcastReceiver(){

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					String action =  intent.getAction();
					if(action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
						if(Common.checkGPS(context)){
							Log.i("phonelog","gps����");
							if(mlocationClient==null){
								setupMClient();
								Log.i("phonelog", "��̨������λ");
							}
						}
						else{
							Log.i("phonelog","gps�ر�");
							if(!Common.isNetConnected){
								destoryMClient();
								Log.i("phonelog", "��̨���ٶ�λ");
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
	     am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 300000, pi);//�ײ⣬�ں���1��4.2���ϼ��Ϊ5���ӣ�С��5���ӵ�������Ч���ں��ף�4.4����������Ч��
	     wakeLockUtil.acquireWakeLock();
	     isAlarmWork=true;
	}
	public void uploadGPS(){
//		if(Common.getUserId(getApplicationContext()).length()<10){
//			return;
//		}
		if(!Common.isWiFiConnected){
			if(Common.isOnlyWifiUploadLoc(getApplicationContext())){
				Log.i("LogDemo","���ý�wifi�ϴ�����ǰ��wifi����");
				return;
			}
		}
		Log.i("LogDemo","�����ϴ�����,from��"+lastPostTime);
		
		datalist=helper.queryfromGpsbylasttime(lastPostTime,Common.getUserId(getApplicationContext()));
		eventdatalist=helper.queryfromEventsbylasttime(lastPostTime,Common.getUserId(getApplicationContext()));
		traceno_up=helper.getUnUploadStatusExists(Common.getUserId(getApplicationContext()));
				
		if(datalist.size()>0){
			
			gpsData=GsonHelper.toJson(datalist);
			PostGpsData gpsDataThread=new PostGpsData(mhandler,URL_GPSDATA,gpsData,Common.getDeviceId(getApplicationContext()));
			gpsDataThread.start();
			gpsData=null;
		}else{
			Log.i("LogDemo","û�����ݣ��ϴ�ָ���ַ���");
			PostGpsData gpsDataThread=new PostGpsData(mhandler,URL_GPSDATA,Common.getUserId(getApplicationContext()),"GPSisNull",Common.getDeviceId(getApplicationContext()));
			gpsDataThread.start();
		}
		
		if(eventdatalist.size()>0){
			Log.i("LogDemo","���¼�");
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
			Log.i("trailadapter","�Զ��ϴ��Ĺ켣��"+traceData_up+","+stepsdata_up);
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
		Log.i("LogDemo","����ʱ�������λ����Ϣ"+location);
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
		Log.i("LogDemo","���ߣ�"+userid+",λ����Ϣ"+location);
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
		Log.i("LogDemo","��������"+userid+",λ����Ϣ"+location);
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
		//���ö�λ����
		mlocationClient.setLocationListener(GpsTrace.this);
		//����Ϊ�߾��ȶ�λģʽ
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		mLocationOption.setSensorEnable(true);
		mLocationOption.setInterval(locFrequency*atomTime);
		//���ö�λ����
		mlocationClient.setLocationOption(mLocationOption);
		Log.i("backloc", "setupMClient,interval:"+locFrequency);
		// �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
		// ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����stopLocation()������ȡ����λ����
		// �ڶ�λ�������ں��ʵ��������ڵ���onDestroy()����
		// �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������stopLocation()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
		mlocationClient.startLocation();
	}
	
	class LocationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.i("LogDemo", "LocationReceiver");
			if (mlocationClient == null) {
				if(Common.isNetConnected||Common.checkGPS(context)){
				setupMClient();
				Log.i("backloc", "LocationReceiver ������λ");
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
	        //��ȡActivityManager  
	        ActivityManager mAm = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);  
	        //��õ�ǰ���е�task  
	        List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);  
	        for (ActivityManager.RunningTaskInfo rti : taskList) {  
	            //�ҵ���ǰӦ�õ�task��������task��ջ��activity���ﵽ�����л���ǰ̨  
	            if(rti.topActivity.getPackageName().equals(context.getPackageName())) {  
	                mAm.moveTaskToFront(rti.id,0);  
	                return;  
	            }  
	        }  
	        //��û���ҵ����е�task���û�������task��ϵͳ�ͷţ�����������mainactivity  
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
		Log.i("backloc", "��λ���-->"+locFrequency);
	}
	
	public void changeStatus(boolean isDraw){
		//showmessage("���켣->"+isDraw);
		this.isDraw=isDraw;
		 if(isDraw){//�����ʼ��¼�켣�󣬻�ȡ���ĵ�һ��GPS��ֱ�Ӽ�¼�����ڱȽ�����һ����ľ���
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
		//showmessage("�켣�ű䶯->"+traceNo);
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
			Log.i("backloc", "id�쳣:"+Common.getUserId(getApplicationContext()));	
			return;
		}
		// TODO Auto-generated method stub
		 
		 if(aLocation != null&&aLocation.getErrorCode()==0){
			if(Common.isHighAccuracy&&aLocation.getAccuracy()>500.0){
					//Toast.makeText(getApplicationContext(), "���ȳ���500m", Toast.LENGTH_SHORT);
				Log.i("backloc", "���ȳ���500m");	
				if(!isAccuracyAlarm){//���㲥 �Ƿ���õ;���
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
				 	 Log.i("backloc", "�������µ�gps��");
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
				 double speed = 0;//��λ m/s
				 if(!lastLatlng.equals(new LatLng(0,0))&&lastRecordTime > 0){
					 speed = dis/((currentRecordTime-lastRecordTime)/1000);
				 }
				 Log.i("backloc","�ٶȣ�"+speed);
				 if((dis>=minDistance&&speed<100.0)||isFirstRecord){//�����û��������յ㾭γ�ȼ����������룬�˾���Ϊ��Խ϶̵ľ��룬��λ�ס�
				 //����仯����5���ٶ�С��100��ÿ��ʱ����¼
					 lastRecordTime = currentRecordTime;
					 lastLatlng=currentLatlng;
					 isFirstRecord=false;
					 
						 //Log.i("LogDemo", "���������ȡ�ĵ�");
					 if (aLocation.getLatitude() != 0 && aLocation.getLongitude() != 0){
						 GpsData data=new GpsData();
						 data.setuserId(Common.getUserId(getApplicationContext()));
						 data.setLongitude(aLocation.getLongitude());
						 data.setLatitude(aLocation.getLatitude());
						 data.setAltitude(aLocation.getAltitude());
						 data.setspeed(aLocation.getSpeed());
						 data.settraceNo(traceNo);
						 
						 if(helper.insertintoGps(data)==0){
							 //showmessage("�ɹ�����һ��λ�ü�¼");
							 Log.i("backloc","�ɹ�����һ��λ�ü�¼:��γ�ȣ�("+aLocation.getLongitude()+","
							 +aLocation.getLatitude()+")"+"���Σ�"+aLocation.getAltitude()+"�ٶ�:"
							 +aLocation.getSpeed()+"���ȣ�"+aLocation.getAccuracy());
						 }
						 else{
							 //showmessage("���ݿ��쳣");
							 Log.i("backloc","���ݿ��쳣");
						 }
					 }
					 
					
					//��λ���֪ͨǰ̨
						 Intent intent = new Intent();  
				         intent.setAction(MY_ACTION);  
				         sendBroadcast(intent);
			        
					
				 }
				 else{
					 
					 Log.i("backloc", "����仯����5�ף������¼");
				 }
		 }
		 else{
			 String errText = "��̨��λʧ��," + aLocation.getErrorCode()+ ": " + aLocation.getErrorInfo();
				Log.e("AmapErr",errText);
			
			//Toast.makeText(getApplicationContext(), "��̨��λʧ��", Toast.LENGTH_SHORT); 
		 }
		
	}
	 //һ���̳���ContentObserver�ļ�������  
    class SmsObserver extends ContentObserver{  
  
        public SmsObserver(Handler handler) {  
            super(handler);  
            // TODO Auto-generated constructor stub  
        }  
        @Override  
        public void onChange(boolean selfChange) {  
            // TODO Auto-generated method stub  
            //��ѯ���������еĶ���  
        	Cursor cursor;
        	try{
        		cursor=getContentResolver().query(Uri.parse(  
	                    "content://sms/outbox"), null, null, null, null);  
        	}catch(SecurityException e){
        		return;
        	}
            //������ѯ�����ȡ�û����ڷ��͵Ķ���  
            while (cursor.moveToNext()) {  
                //StringBuffer sb=new StringBuffer();  
                //��ȡ���ŵķ��͵�ַ  
                //sb.append("���͵�ַ��"+cursor.getString(cursor.getColumnIndex("address")));  
                //��ȡ���ŵı���  
                //sb.append("\n���⣺"+cursor.getString(cursor.getColumnIndex("subject")));  
                //��ȡ���ŵ�����  
                //sb.append("\n���ݣ�"+cursor.getString(cursor.getColumnIndex("body")));   
                //��ȡ���ŵķ���ʱ��  
                Date date=new Date(cursor.getLong(cursor.getColumnIndex("date")));  
                //��ʽ������Ϊ��λ������  
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
                //sb.append("\nʱ�䣺"+sdf.format(date));  
                String timestr=sdf.format(date);
                Log.i("phonelog","��ѯ�����ڷ��Ͷ��ţ�"+timestr);  
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
				Log.i(TAG,"�����¼�"+createTime+",����:"+EventType+"("+Common.aLocation.getLongitude()+","+Common.aLocation.getLatitude()+")");
			}else{
				Log.i(TAG,"�����¼�"+createTime+",����:"+EventType);
			}
			 
		 }
		 else{
			 
			 Log.i(TAG,"���ݿ��쳣");
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
			Log.i("LogDemo", "��̨unregisterReceiver(locationReceiver)");
		}
		if(foregroundReceiver!=null){
			GpsTrace.this.unregisterReceiver(foregroundReceiver);
			Log.i("LogDemo", "��̨unregisterReceiver(foregroundReceiver)");
		}
		if(connectionReceiver!=null){
			GpsTrace.this.unregisterReceiver(connectionReceiver);
			Log.i("LogDemo", "��̨unregisterReceiver(connectionReceiver)");
		}
		if(GPSStatusReceiver!=null){
			GpsTrace.this.unregisterReceiver(GPSStatusReceiver);
			Log.i("LogDemo", "��̨unregisterReceiver(GPSStatusReceiver)");
		}
		destoryMClient();
		if(wakeLockUtil!=null){
			wakeLockUtil.releaseWakeLock();
			Log.i("LogDemo", "releaseWakeLock");
		}
		GpsTrace.this.getContentResolver().unregisterContentObserver(mObserver);
		stopForeground(true);
		super.onDestroy();
		Log.i("LogDemo", "service�ر�");
	}

	
}
