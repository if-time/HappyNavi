package com.trackersurvey.helper;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.trackersurvey.entity.FileInfo;
import com.trackersurvey.entity.TimeValue;
import com.trackersurvey.happynavi.LoginActivity;
import com.trackersurvey.happynavi.MainActivity;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.Register;
import com.trackersurvey.httpconnection.PostOnOffline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public  class Common {
	//public static final int version=1;
	public static final String version="1.8.7";
	public static boolean isUpdationg=false;
	public static boolean isNetConnected=true;
	public static boolean isWiFiConnected=false;
	public static boolean isRecording=false;
	public static boolean isHighAccuracy=true;
	public static int layerid_main=0;
	public static int layerid_path=0;
	public static String url = "http://219.218.118.176:8090/Communication/";
	public static String url_heart = "http://219.218.118.176:8090/Communication/heartbeat.aspx";
//	public static String url="http://211.87.235.102:8092/Communication/";
	//public static String url_heart = "http://211.87.235.102:8092/Communication/heartbeat.aspx";
	public static String url_crash = url+"upExceptionInfo.aspx";
	
	public static String url_file = "http://219.218.118.176:8089/Mobile/";
	//public static String url_file = "http://211.87.235.102:8081/Mobile/";
	
	public static  String URL_UPEVENT = url + "upComment.aspx";
	public static  String URL_UPFILE = url_file + "upFile.aspx";
	public static  String URL_DOWNEVENT = url_file + "downComment.aspx";
	public static  String URL_DOWNFILE = url_file + "downFile.aspx";
	public static  String URL_DELETEEVENT = url_file+"userChgCmt.aspx";
	public static String url_wx = "http://www.lisoft.com.cn/Share/PoMobile.ashx?";
	public static  String WX_APP_ID = "wx79c37ea773c35a23";//"wxbe3210c55c8d7e64";
	
	//public static String userId1 = "0";
	//public static int traceNo = 0;
	//public static String pic = "";
	public static String NickName = "";
	public static AMapLocation aLocation=null;
	public static FileInfo fileInfo = null;//�°�apk�ļ�
	public static int winWidth = 720;
	public static int winHeight = 1080;
	public static int decodeImgWidth = 720;
	public static int decodeImgHeight = 1280;
	public static double ppiScale = 1.5;
	public static final String APPLICATION_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/HappyNavi/";
	public static final String LOG_PATH = APPLICATION_DIR+"log/";
	public static final String PHOTO_PATH = APPLICATION_DIR+"file/";
	public static final String GROUPHEAD_PATH = APPLICATION_DIR+"grouphead/";
	public static final String CACHEPHOTO_PATH = APPLICATION_DIR+"fileCache/";
	public static final String DOWNLOAD_APP_PATH = APPLICATION_DIR+"downloads/";
	/** 
	 * ���ص�ǰ����汾�� 
	 */  
	public static String getAppVersionName(Context context) {  
	    String versionName = "";  
	    try {  
	        // ---get the package info---  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);  
	        versionName = pi.versionName;  
	       // versioncode = pi.versionCode;
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	        Log.e("VersionInfo", "Exception", e);  
	    }  
	    return versionName;  
	}
//	/**
//	 * �ж��Ƿ�Ϊ�����û�
//	 */
//	public static boolean getAppVersionTime(Context context){
//		PackageInfo pi = null;
//		try {
//			PackageManager pm = context.getPackageManager();  
//			pi = pm.getPackageInfo(context.getPackageName(), 0);
//		} catch (NameNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        return pi.firstInstallTime == pi.lastUpdateTime;//����true��������Ǹ����û�
//	}
	/* 
	* ����ת�� 
	*/  
	public static String transformTime2(long ms) {  
	              
	             int ss = 1000;  
	             int mi = ss * 60;  
	             int hh = mi * 60;  
	             int dd = hh * 24;  
	  
	             long days = ms / dd;  
	             long hours = (ms - days * dd) / hh;  
	             long mins = (ms - days * dd - hours * hh) / mi;  
	             long seconds = (ms - days * dd - hours * hh - mins * mi) / ss;  
	             String duration_text="";
	             if(days!=0){
						duration_text+=days+"��";
					}
					if(hours!=0){
						duration_text+=hours+"Сʱ";
					}
					if(mins!=0){
						duration_text+=mins+"��";
					}
					//if(seconds!=0){
						duration_text+=seconds+"��";
					//}
	            
	             return duration_text;  
	   }  
	public static Bitmap scaleBitmap(String imageFile, int winWidth,
			int winHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; //
		BitmapFactory.decodeFile(imageFile, options);

		options.inSampleSize = calculateInSampleSize(options, winWidth,
				winHeight);

		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(imageFile, options);
	}

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
	/**
	 * �õ�һ����ʽ����ʱ��
	 * 
	 * @param time
	 *            ʱ�� ��
	 * @return ʱ���֣���
	 */
	public static String transformTime(long ms) {
	     int ss = 1000;  
         int mi = ss * 60;  
         int hh = mi * 60;  
         int dd = hh * 24;  

         long days = ms / dd;  
         long hour = (ms - days * dd) / hh;  
         long minute = (ms - days * dd - hour * hh) / mi;  
         long second = (ms - days * dd - hour * hh - minute * mi) / ss;

		// ��������ʾ��λ
		// String strMillisecond = "" + (millisecond / 10);
		// ����ʾ��λ
		String strSecond = ("00" + second)
				.substring(("00" + second).length() - 2);
		// ����ʾ��λ
		String strMinute = ("00" + minute)
				.substring(("00" + minute).length() - 2);
		// ʱ��ʾ��λ
		String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

		return strHour + ":" + strMinute + ":" + strSecond;
		// + strMillisecond;
	}
	public static String transformDistance(double m){
		String distance_txt="";
		distance_txt+=Double.parseDouble(new DecimalFormat("######0.00").format(m/1000));
		return distance_txt;
	}
	public static String transformSpeed(double s){
		String speed_txt="";
		speed_txt+=Double.parseDouble(new DecimalFormat("######0.00").format(s*3600));
		return speed_txt;
	}
	/**
	 * ���("yyyy-MM-dd HH:mm:ss.SSS")��ʽ�ĵ�ǰʱ��
	 * 
	 * @return
	 */
	public static String currentTimeMill() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date curDate = new Date(System.currentTimeMillis());

		return sdf.format(curDate);

	}

	/**
	 * ���("yyyy-MM-dd HH:mm:ss")��ʽ�ĵ�ǰʱ��
	 * 
	 * @return
	 */
	public static String currentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());

		return sdf.format(curDate);

	}
	/**
	 * ���("yyyy-MM-dd")��ʽ�ĵ�ǰʱ��
	 * 
	 * @return
	 */
	public static String currentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date curDate = new Date(System.currentTimeMillis());

		return sdf.format(curDate);

	}
	/**
	 * ����������
	 * 
	 * @return
	 */
	public static int currentYearnum() {
		
		Date curDate = new Date(System.currentTimeMillis());

		return curDate.getYear()+1900;

	}
	/**
	 * ��������·�
	 * 
	 * @return
	 */
	public static int currentMonthnum() {
		
		Date curDate = new Date(System.currentTimeMillis());

		return curDate.getMonth()+1;

	}
	/**
	 * ��ý����Ǽ���
	 * 
	 * @return
	 */
	public static int currentDaynum() {
		
		Date curDate = new Date(System.currentTimeMillis());

		return curDate.getDate();

	}
	/**
	 * ��ø���("yyyyMM")��ʽ��ʱ�����
	 * 
	 * @return
	 */
	public static int getYear(String dateTime) {
		
		Date curDate = null;
		try {
			curDate = new SimpleDateFormat("yyyyMM").parse(dateTime);
			return curDate.getYear()+1900;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}
	/**
	 * ��ø���("yyyyMM")��ʽ��ʱ�����
	 * 
	 * @return
	 */
	public static int getMonth(String dateTime) {
		
		Date curDate = null;
		try {
			curDate = new SimpleDateFormat("yyyyMM").parse(dateTime);
			return curDate.getMonth()+1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}
	
	/**
	 * ���("MMdd")��ʽ�ĵ�ǰʱ��
	 * 
	 * @return
	 */
	public static String currentDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
		Date curDate = new Date(System.currentTimeMillis());

		return sdf.format(curDate);

	}
	
	
	
	/**
	 * ��("yyyy-MM-dd HH:mm:ss")��ʽ��ʱ��ת��1970.1.1����ʱ�̵�����
	 */
	public static long timeStamp(String dateTime) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime)
						.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
	}
	
	/**
	 * ���("MM/dd")��ʽ�ĵ�ǰʱ��
	 * 
	 * @return
	 */
	public static String mdTime(String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		Date curDate = null;
		try {
			curDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
			return sdf.format(curDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return " ";
	}
	
	/**
	 * ���㲢��ʽ��doubles��ֵ��������λ��Ч����
	 * 
	 * @param doubles
	 * @return ���ص�ǰ·��
	 */
	public static String formatDouble(Context context,Double doubles) {
		DecimalFormat format = new DecimalFormat("####.##");
		String distanceStr = format.format(doubles);
		return distanceStr.equals(context.getString(R.string.zero)) ? context.getString(R.string.double_zero)
				: distanceStr;
	}

	/**
	 * �ж�����״̬
	 * @param context
	 * @return ���û���������ӷ���-1�����򷵻���������
	 */
	public static int checkNetworkState(Context context){
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
			if(networkInfo == null){
				return -1;	
			}
			//1.�ж��Ƿ����������� 
			if(networkInfo.isAvailable()){
				//2.��ȡ��ǰ�������ӵ�������Ϣ
				int networkType = networkInfo.getType();
				if(ConnectivityManager.TYPE_WIFI == networkType){
					return ConnectivityManager.TYPE_WIFI;
				}else if(ConnectivityManager.TYPE_MOBILE == networkType){
					return ConnectivityManager.TYPE_MOBILE;
				}
			}
		}
		return -1;
	}
	public static void createFileDir(){
		// Ӧ�ÿռ��ļ���
				File destDir = new File(Common.APPLICATION_DIR);
				//Log.i("Eaa_mkdirs",Common.APPLICATION_DIR);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				//Ӧ�ñ����ļ����±����ļ��е����ļ���
				destDir = new File(Common.PHOTO_PATH);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				//Ӧ�ÿռ�����ͼ�ļ���
				destDir = new File(Common.CACHEPHOTO_PATH);
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				String path = Common.GROUPHEAD_PATH;      
		        File dir = new File(path);      
		        if (!dir.exists()) {      
		            dir.mkdirs();      
		        }
	}
	/**
	 * �ж�GPS�Ƿ���
	 */
	public static boolean checkGPS(Context context) {//���ж�GPS�Ƿ���
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // �ж�GPSģ���Ƿ��������û������
        if (!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
        	return false;
        }
        else{
        	return true;
        }
    }
	public static void DialogForVisiter(Context context){
		final Context fianlContext=context;
		AlertDialog alert=new AlertDialog.Builder(context).create();
		alert.setTitle(context.getResources().getString(R.string.tip));
		alert.setMessage(context.getResources().getString(R.string.tips_register));
		alert.setButton(DialogInterface.BUTTON_NEGATIVE,context.getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		alert.setButton(DialogInterface.BUTTON_POSITIVE,context.getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(fianlContext, LoginActivity.class);
				fianlContext.startActivity(intent);
				
			}
		});
		alert.show();
	}
	/**
	 * ��ʾ�������Ի���
	 */
	public static void showDialog(ProgressDialog proDialog,String title,String message) {
		if(proDialog==null){
			return;
		}
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
	public static void dismissDialog(ProgressDialog proDialog) {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}
	
	
	public static  String  getUserId(Context context){
		
		SharedPreferences sp=context.getSharedPreferences("config",Context.MODE_PRIVATE);//˽�в���
		String lastId=sp.getString("lastid", "0");
		
		return lastId;
	}
	public static String getHeadPhoto(Context context){
		
		SharedPreferences sp=context.getSharedPreferences("config",Context.MODE_PRIVATE);//˽�в���
		String headphoto=sp.getString("headphoto", "");
				
		return headphoto;
	}
	public static void writeHeadphoto(Context context,String headphoto){
		SharedPreferences sp=context.getSharedPreferences("config",Context.MODE_PRIVATE);//˽�в���
		Editor editor = sp.edit();
		editor.putString("headphoto", headphoto);
		editor.commit();
	}
	//�ж��ο����
		public static  boolean isVisiter()
		
		{
			
			return "???".equals(Common.NickName);
			
			
		}
		//�ж����պϷ�
		
		public static boolean isLegalBirth(String DATE1) {
			   
		    
			Calendar c1 = Calendar.getInstance();
			// ������
			int year = c1.get(Calendar.YEAR);
			// ����·�
			int month = c1.get(Calendar.MONTH) + 1;
			// �������
			int date = c1.get(Calendar.DATE);
			
			String now=year+"-"+month+"-"+date;
			
			
		    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		    try {
		        Date dt1 = df.parse(DATE1);
		        Date dt2 = df.parse(now);
		        if (dt1.getTime() > dt2.getTime()) {
		           
		            return false;
		        } else if (dt1.getTime() <= dt2.getTime()) {
		           
		            return true;
		        } 
		    } catch (Exception exception) {
		        exception.printStackTrace();
		    }
			return false;
		   
		}
		/**
		 * ʶ�����Դ��־�� 
		 * 1��IMEI��imei�� �� 
		 * 2��wifi mac��ַ��wifi�� �� 
		 * 3�� ���кţ�sn���� 
		 * 
		 * */
		public static String setDeviceId(Context context) {  
			  StringBuilder deviceId = new StringBuilder();  
			    
			  try {
				  
				  //IMEI��imei��  
				  TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
				  String imei = tm.getDeviceId();  
				  if(!imei.equals("")&&imei!=null){  
				        
				      deviceId.append(imei);  
				      Log.i("phonelog","getDeviceId by imei: "+deviceId.toString());  
				      return deviceId.toString();  
				  }  
			    //wifi mac��ַ  
			    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
			    WifiInfo info = wifi.getConnectionInfo();  
			    String wifiMac = info.getMacAddress();  
			    if(!wifiMac.equals("")&&wifiMac!=null){  
			       
			      deviceId.append(wifiMac);  
			      Log.i("phonelog","getDeviceId by mac: "+deviceId.toString());  
			      return deviceId.toString();  
			    }  
			   
			    //���кţ�sn��  
			    String sn = tm.getSimSerialNumber();  
			    if(!sn.equals("")&&sn!=null){  
			       
			      deviceId.append(sn);  
			      Log.i("phonelog","getDeviceId by sim: "+deviceId.toString());  
			      return deviceId.toString();  
			    }  
			   
			  } catch (Exception e) {  
			    e.printStackTrace();  
			    Log.i("phonelog","getDeviceId error "); 
			  }  
			  deviceId.append("unknown");
			  Log.i("phonelog","getDeviceId by default: "+deviceId.toString());
			  return deviceId.toString();  
	}  	
	public static String getDeviceId(Context context) {
		SharedPreferences sp=context.getSharedPreferences("config",Context.MODE_PRIVATE);//˽�в���
		String deviceId=sp.getString("deviceid", "null");
		
		return deviceId;
	}
	/**
	 * ��ȡ�豸����+�ͺ�
	 * */
	public static String getDeviceName() {
		if(Build.BRAND==null||Build.BRAND.equals("")||
				Build.MODEL==null||Build.MODEL.equals("")){
			return "unknown";
		}
		return Build.BRAND+"-"+Build.MODEL;
	}
	/**
	 * ��ȡ�豸����
	 * */
	public static String getDeviceBrand() {
		if(Build.BRAND==null||Build.BRAND.equals("")){
			return "unknown";
		}
		return Build.BRAND;
	}
	public static boolean isAutoUpdate(Context context){
		String updateSwitchKey = context.getResources().getString(R.string.auto_update_switch_key);  
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(updateSwitchKey,true);
	}
	public static boolean isOnlyWifiUploadLoc(Context context){
		String uploadSwitchKey = context.getResources().getString(R.string.auto_upload_switch_key);  
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(uploadSwitchKey,false);
	}
	public static boolean isOnlyWifiUploadPic(Context context){
		String uploadSwitchKey = context.getResources().getString(R.string.auto_uploadpic_switch_key);  
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(uploadSwitchKey,true);
	}
	public static int getTimeUpdateFrequency(Context context){//����ʱ���������Ƶ�� ��λ s
		String updateFrequencyKey = context.getResources().getString(R.string.auto_update_frequency_key);  
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int frequency= Integer.parseInt(settings.getString(updateFrequencyKey, "60"));//��λ ����
		return frequency*60;// s
	}
	public static void setFourTime(Context context,TimeValue timevalue){
		if(timevalue==null){
			return;
		}
		String uploadFrequencyKey = context.getResources().getString(R.string.auto_upload_frequency_key);
        String rec_loc_FrequencyKey = context.getResources().getString(R.string.auto_record_loc_frequency_key);
        String norec_loc_FrequencyKey = context.getResources().getString(R.string.auto_norecord_loc_frequency_key);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putInt(uploadFrequencyKey, timevalue.getUploadTime());
		editor.putInt(rec_loc_FrequencyKey, timevalue.getRecTime());
		editor.putInt(norec_loc_FrequencyKey, timevalue.getNoRecTime());
		editor.putString("LastPostTime", timevalue.getLastTime());
		editor.commit();
	}
	public static void setPostTime(Context context,String lastPostTime){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = settings.edit();
		editor.putString("LastPostTime", lastPostTime);
		editor.commit();
	}
	public static int getUploadFrequenct(Context context){//�����ϴ�ʱ���� s
		String uploadFrequencyKey = context.getResources().getString(R.string.auto_upload_frequency_key);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getInt(uploadFrequencyKey, 30);
	}
	public static int getRecLocFrequenct(Context context){//���ؼ�¼��λʱ���� s
		String rec_loc_FrequencyKey = context.getResources().getString(R.string.auto_record_loc_frequency_key);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getInt(rec_loc_FrequencyKey, 5);
	}
	public static int getNoRecLocFrequenct(Context context){//���طǼ�¼��λʱ���� s
		String norec_loc_FrequencyKey = context.getResources().getString(R.string.auto_norecord_loc_frequency_key);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getInt(norec_loc_FrequencyKey, 10);
	}
	public static ArrayList<String> getAddressList(){
		if(aLocation!=null){
			ArrayList<String> addressList=new ArrayList<String>();
			addressList.add(aLocation.getPoiName());
			addressList.add(aLocation.getProvince()+aLocation.getCity());
			addressList.add(aLocation.getCity()+aLocation.getDistrict());
			
			addressList.add(aLocation.getCity()+aLocation.getDistrict()+aLocation.getRoad());
			addressList.add(aLocation.getAddress());
			
			return addressList;
		}
		else{
			return null;
		}
	}
	 public static void sendOffline(String deviceId, Context ct){
			String location = "";
			if(aLocation!=null){
				location = aLocation.getLongitude()+";"+aLocation.getLatitude();
			}	
			Log.i("LogDemo","���ߣ�"+Common.getUserId(ct)+",λ����Ϣ"+location);
			PostOnOffline offline = new PostOnOffline(url+"request.aspx",
					Common.getUserId(ct),location,"Offline",deviceId);
			offline.start();
			
		}
}
