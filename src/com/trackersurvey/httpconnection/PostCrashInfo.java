package com.trackersurvey.httpconnection;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import com.trackersurvey.entity.ExceptionInfo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PostCrashInfo extends Thread{
	//private Handler mHandler;
	private String url;
	private ExceptionInfo info;
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public PostCrashInfo(String url,ExceptionInfo info) {
		
		this.url=url;
		this.info = info;
	}
	
    public void run() {
    	
        params.add(new BasicNameValuePair("UserId", info.userID));  
        params.add(new BasicNameValuePair("DeviceName", info.DeviceName)); 
        params.add(new BasicNameValuePair("CreateTime", info.CreateTime));
        params.add(new BasicNameValuePair("ExceptionInfo", info.ExceptionInfo));
        params.add(new BasicNameValuePair("VersionName", info.VersionName));
        Post();
	}
    private void Post(){
    	//Message msg = Message.obtain();
        try {  
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
            httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
            
            HttpClient httpClient = new DefaultHttpClient();  
            
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,8000);//连接时间
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);//数据传输时间
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
              
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
               Log.i("phonelog", "异常上传成功");
            }else{
            	Log.i("phonelog", "异常上传失败");
            }
        }catch(Exception e){
        	e.printStackTrace();
        	
        }
    
               
    }
}