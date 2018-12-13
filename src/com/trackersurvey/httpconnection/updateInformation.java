package com.trackersurvey.httpconnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.Des;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class updateInformation extends Thread {
	private Handler mHandler;
	private String url, informationData,deviceId,version,userId;
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();

	public updateInformation(Handler mHandler, String url,
			String informationData,String deviceId,String version,String userId) {
		this.mHandler = mHandler;
		this.url = url;
		this.informationData = informationData;
		this.deviceId=deviceId;
		this.version = version;
		this.userId = userId;
	}

	public void run() {
		// ��ע�����Ϸָ���

		String rd[] = informationData.split("!");
		// naem id pass data

		// �ж��Ƿ����DES
		// ���汾��С��"1.3.3"�򲻼���
		boolean jiami = (version.compareTo("1.3.3") >= 0);
		
			
		if (jiami) {

			try {
				params.add(new BasicNameValuePair("userId", userId));
				params.add(new BasicNameValuePair("nickName", Des
						.encrypt(rd[0])));
				params.add(new BasicNameValuePair("pswStr", rd[1]));
				params.add(new BasicNameValuePair("birthday", Des
						.encrypt(rd[2])));
				params.add(new BasicNameValuePair("picStr", rd[3]));
				params.add(new BasicNameValuePair("deviceId",deviceId));
				for (int i = 4; i < rd.length; i++) {

					params.add(new BasicNameValuePair((i - 3) + "", Des.encrypt(rd[i])));

				}
			} catch (Exception e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}

		} else {
			params.add(new BasicNameValuePair("userId", userId));
			params.add(new BasicNameValuePair("nickName", rd[0]));
			params.add(new BasicNameValuePair("pswStr", rd[1]));
			params.add(new BasicNameValuePair("birthday", rd[2]));
			params.add(new BasicNameValuePair("picStr", rd[3]));

			for (int i = 4; i < rd.length; i++) {

				params.add(new BasicNameValuePair((i - 3) + "", rd[i]));

			}

		}
		Post();

	}

	public void Post() {
		Message msg = Message.obtain();
		try {
			// Log.i("LogDemo", "��������");
			HttpEntity httpEntity = new UrlEncodedFormEntity(params, "utf-8");
			// HttpEntity httpEntity = new UrlEncodedFormEntity(params);
			httpRequest = new HttpPost(url);
			httpRequest.setEntity(httpEntity);
			// Log.i("LogDemo", "����");
			HttpClient httpClient = new DefaultHttpClient();
			// Log.i("LogDemo", "����");
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			// Log.i("LogDemo", "��������");
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// Log.i("LogDemo", "SC_OK");
				BufferedReader bin = new BufferedReader(new InputStreamReader(
						httpResponse.getEntity().getContent()));
				String result = bin.readLine();
				// Log.i("LogDemo", "readLine:"+result);
				if (result != null) {
					if (result.equals("error")) {
						msg.what = 1;
					}

					else {
						msg.what = 0;
					}
					msg.obj = result;
					mHandler.sendMessage(msg);
				} else {
					msg.what = 1;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}

			}

			else {
				
				msg.what = 10;

				msg.obj = "�ύʧ��!";
				mHandler.sendMessage(msg);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// Log.i("LogDemo", "ʧ��");
			msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// Log.i("LogDemo", "ʧ��");
			// TODO Auto-generated catch block
			msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
			e.printStackTrace();
		} catch (IOException e) {
			// Log.i("LogDemo", "ʧ��");
			// TODO Auto-generated catch block
			msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
			e.printStackTrace();
		}

	}
}
