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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class getMyInformation  extends Thread  {//�������Ϣ
	private Handler mHandler;
	private String url,id;
	private HttpPost httpRequest;
	private String deviceId;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public getMyInformation (Handler mHandler, String url,String id,String deviceId)
	{
		this.mHandler = mHandler;
		this.url=url;
		this.id=id;
	    this.deviceId=deviceId;
	}
	public void run()
	{
		
		   
		params.add(new BasicNameValuePair("userId", id)); 
		params.add(new BasicNameValuePair("deviceId", deviceId));
		params.add(new BasicNameValuePair("requestType", "userInfoEncrypt")); 
		//params.add(new BasicNameValuePair("requestType", "userInfo")); 
	  

		
	        Post();
		
	}
	public void Post()
	{
	//Log.i("login","login");	
		Message msg = Message.obtain();
        try {  
        	//Log.i("LogDemo", "��������");
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
            httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
              
            HttpClient httpClient = new DefaultHttpClient();  
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
            //Log.i("LogDemo", "��������");  
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
            	//Log.i("LogDemo", "SC_OK");  
                BufferedReader bin = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String result = bin.readLine();	
               // Log.i("LogDemo", "getMyInformation��readLine:"+result);  
                if(result!=null){
                    if(result.equals("errorId")){
                		msg.what = 4;
                	}
                    else if(result.equals("errorPassword")){
                		msg.what = 5;
                	}
                	else{
                	msg.what = 0;
                	}
                    msg.obj = result;
     			    mHandler.sendMessage(msg);
                }else{
                	msg.what = 1;
                	msg.obj = result;
            	
     			    mHandler.sendMessage(msg);
                }
                
            }
            
            else{  
            	//Log.i("LogDemo", "����ʧ��"); 
            	msg.what = 10;
            	 
 			    msg.obj = "�ύʧ��!";
 			    mHandler.sendMessage(msg);	 
            }  
        } catch (UnsupportedEncodingException e) {  
            // TODO Auto-generated catch block 
        	msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (ClientProtocolException e) {  
            // TODO Auto-generated catch block 
        	msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
        	msg.what = 10;
			msg.obj = "�ύʧ�ܣ�";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } 
		
	}
}