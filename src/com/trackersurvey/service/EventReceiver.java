package com.trackersurvey.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.PhoneEvents;
import com.trackersurvey.helper.Common;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {
	
	private static final String TAG = "phonelog";
    private static boolean mIncomingFlag = false;
    private static String mIncomingNumber = null;
    
    private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context=context;
		Log.i(TAG, "================================");
		Log.i(TAG, "intent=>"+intent.getAction());
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");       
		Date curDate=new Date(System.currentTimeMillis());//��ȡ��ǰʱ��       
		String strdate=formatter.format(curDate);   
		Log.i(TAG, "time=>"+strdate);
		//����绰
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            mIncomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i(TAG, "call OUT:" + phoneNumber);
            saveEvents(strdate,2);
            
        } 
        //�յ�����
        else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
        	Log.i(TAG, "SMS_RECEIVED");
        	saveEvents(strdate,1);
        }
        //����
        else if(intent.getAction().equals("android.hardware.action.NEW_PICTURE")){
        	Log.i(TAG, "�����¼�");
        	saveEvents(strdate,4);
        }
        //¼��
        else if(intent.getAction().equals("android.hardware.action.NEW_VIDEO")){
        	Log.i(TAG, "¼���¼�");
        	saveEvents(strdate,5);
        }
       //����
        else if(intent.getAction().equals("android.intent.action.PHONE_STATE")){
            
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            mIncomingNumber = intent.getStringExtra("incoming_number");
            switch (tManager.getCallState()) {
            
	            case TelephonyManager.CALL_STATE_RINGING://����
	            	if(mIncomingNumber!=null&&!mIncomingNumber.equals("")){
	            		mIncomingFlag=true;
		                Log.i(TAG, "Ringing :" + mIncomingNumber);
		            }
	                break;
	            case TelephonyManager.CALL_STATE_OFFHOOK://����
	                if(mIncomingFlag&&mIncomingNumber!=null&&!mIncomingNumber.equals("")){
	                	Log.i(TAG, "incoming ACCEPT :" + mIncomingNumber);
	                	saveEvents(strdate,3);
	                }
	                break;
	            case TelephonyManager.CALL_STATE_IDLE://�Ҷ�
	            	if(mIncomingNumber!=null&&!mIncomingNumber.equals("")){
	            		Log.i(TAG, "incoming IDLE"+ mIncomingNumber);
	            	}
	                break;
            }
        }
    }
	
	public void saveEvents(String createTime,int EventType){
		if(context == null){
			return;
		}
		PhoneEvents event=null;
		if(Common.aLocation!=null){
			 event= new PhoneEvents(Common.getUserId(context),createTime,EventType,
				Common.aLocation.getLongitude(),Common.aLocation.getLatitude(),Common.aLocation.getAltitude());
		}
		else{
			event= new PhoneEvents(Common.getUserId(context),createTime,EventType,0,0,0);
		}
		try{
			TraceDBHelper helper=new TraceDBHelper(context);
			if(helper.insertintoEvents(event)==0){
				 
				 Log.i(TAG,"�����¼�"+createTime+",����:"+EventType+"("+Common.aLocation.getLongitude()+","+Common.aLocation.getLatitude()+")");
			 }
			 else{
				 
				 Log.i(TAG,"���ݿ��쳣");
			 }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
