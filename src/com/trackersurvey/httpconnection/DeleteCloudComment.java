package com.trackersurvey.httpconnection;


import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.helper.Common;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DeleteCloudComment extends Thread {
	private Handler mHandler;
	private String url;
	private String userID;
	private String dateTime,dateTime_start,dateTime_end;
	private String deviceId;
	private boolean isDelMulti;//�Ƿ�ͬʱɾ�������Ȥ��
	private RequestParams params;
	private Message msg;
	PhotoDBHelper dbHelper;
	HttpUtils httpSend;
	Cursor cursor;

	public DeleteCloudComment(Context context,Handler handler, String url, String userID,
			String dateTime,String deviceId) {
		params = new RequestParams();
		this.mHandler = handler;
		this.url = url;
		this.userID = userID;
		this.dateTime = dateTime;
		this.deviceId = deviceId;
		isDelMulti = false;
		params.addBodyParameter("requestType","delComment");
		params.addBodyParameter("userId", userID);
		params.addBodyParameter("createTime", dateTime);
		params.addBodyParameter("deviceId",deviceId);
		
		msg = Message.obtain();
		dbHelper = new PhotoDBHelper(context, PhotoDBHelper.DBWRITE);
	}
	public DeleteCloudComment(Context context,Handler handler, String url, String userID,
			String dateTime_start,String dateTime_end,String deviceId) {
		params = new RequestParams();
		this.mHandler = handler;
		this.url = url;
		this.userID = userID;
		this.dateTime_start = dateTime_start;
		this.dateTime_end = dateTime_end;
		this.deviceId = deviceId;
		isDelMulti = true;
		params.addBodyParameter("requestType","delMultiComment");
		params.addBodyParameter("userId", userID);
		params.addBodyParameter("startTime", dateTime_start);
		params.addBodyParameter("endTime", dateTime_end);
		params.addBodyParameter("deviceId",deviceId);
		
		
		msg = Message.obtain();
		dbHelper = new PhotoDBHelper(context, PhotoDBHelper.DBWRITE);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		httpSend = new HttpUtils();

		httpSend.send(HttpMethod.POST, url, params, new eventDeleteCallBack());

	}
	
	/**
	 * �����ݿ���ɾ�����¼�
	 * @return
	 */
	int  deleteFromDB(){
		int result = -1;
		if(isDelMulti){
			
		}else{
			 result =dbHelper.deleteEvent(dateTime,userID);
		}
		dbHelper.closeDB();
		if(result !=0){
			return -1;
		}
		return 0;
	}
	

	// �ص��������ϴ��Ƿ�ɹ�
	class eventDeleteCallBack extends RequestCallBack<String> {
		@Override
		public void onFailure(HttpException arg0, String result) {
			//Log.i("�ϴ�ʧ��", arg0 + "|||" + result);
			msg.what = 3;
			msg.obj = result;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			String result = responseInfo.result;
			// ����ֵ��һ��ID
			if ("ok".equals(result)) { // ����ɹ�
				//Log.i("result", result + " |*| " + "commentOK");
				//ɾ���¼��ɹ�
				if(deleteFromDB() ==0){
					msg.what = 0;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}else{
					//ɾ�� �¼�ʧ��
					msg.what = 1;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			} else {
				//����������ֵ����ȷ
				msg.what = 2;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		}
	}

}
