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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class getPasssword extends Thread  {
	private Handler mHandler;
	private String url;
	private String deviceId,userId;
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public getPasssword(Handler mHandler, String url,String deviceId,String userId)
	{
		this.mHandler = mHandler;
		this.url=url;
		this.deviceId=deviceId;
	    this.userId = userId;
	}
	public void run()
	{
		//把注册资料分隔开 
		
		//requestType = reqPsw
		params.add(new BasicNameValuePair("requestType", "reqPsw"));
		params.add(new BasicNameValuePair("userId", userId));
		params.add(new BasicNameValuePair("deviceId", deviceId));
	    Post();
		
	}
	public void Post()
	{
		Message msg = Message.obtain();
        try {  
        	//Log.i("LogDemo", "建立连接");
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
        	//HttpEntity httpEntity = new UrlEncodedFormEntity(params);  
        	httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
           // Log.i("LogDemo", "运行"); 
            HttpClient httpClient = new DefaultHttpClient();  
            //Log.i("LogDemo", "运行"); 
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
          
            //Log.i("LogDemo", "发送连接");  
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
            	//Log.i("LogDemo", "SC_OK");  
                BufferedReader bin = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String result = bin.readLine();	
               //Log.i("LogDemo", "getPasssword,readLine:"+result);  
                if(result!=null){
                    if(result.equals("error")){
                		msg.what = 1;
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
            
            	msg.what = 10;
            	 
 			    msg.obj = "提交失败!";
 			    mHandler.sendMessage(msg);	 
            }  
        } catch (UnsupportedEncodingException e) {  
            // TODO Auto-generated catch block 
        	 
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (ClientProtocolException e) {  
        	 
            // TODO Auto-generated catch block 
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } catch (IOException e) {  
        	  
            // TODO Auto-generated catch block  
        	msg.what = 10;
			msg.obj = "提交失败！";
			mHandler.sendMessage(msg);
            e.printStackTrace();  
        } 
		
	}
}
