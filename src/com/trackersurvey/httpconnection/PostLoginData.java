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
import org.apache.http.params.CoreConnectionPNames;

import com.trackersurvey.helper.Common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PostLoginData extends Thread  {
	private Handler mHandler;
	private String url,LoginData,id,pwd,deviceId;
	private HttpPost httpRequest;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();  
	
	public PostLoginData(Handler mHandler, String url,String LoginData,String deviceId)
	{
		this.mHandler = mHandler;
		this.url=url;
		this.LoginData=LoginData;
	    this.deviceId=deviceId;
	}
	public void run()
	{
		//�ѵ�½���Ϸָ��� id,pwd,
		String rd[]=LoginData.split("!");
		   
		params.add(new BasicNameValuePair("userID", rd[0])); 
		params.add(new BasicNameValuePair("password", rd[1]));  
		params.add(new BasicNameValuePair("deviceId", deviceId));
		
	        Post();
		
	}
	public void Post()
	{
	
		Message msg = Message.obtain();
        try {  
        	//Log.i("LogDemo", "��������");
            HttpEntity httpEntity = new UrlEncodedFormEntity(params,"utf-8");  
            httpRequest = new HttpPost(url);
            httpRequest.setEntity(httpEntity);  
              
            HttpClient httpClient = new DefaultHttpClient(); 
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,8000);//����ʱ��
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);//���ݴ���ʱ��
           
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
            //Log.i("LogDemo", "��������");  
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
            	//Log.i("LogDemo", "SC_OK");  
                BufferedReader bin = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String result = bin.readLine();	
                //Log.i("LogDemo", "PostLoginData,readLine:"+result);  
                if(result!=null){
                    if(result.equals("errorId")){
                		msg.what = 4;
                	}
                    else if(result.equals("errorPassword")){
                		msg.what = 5;
                	}
                	else if(result.equals("ok"))
                	{
                
                	msg.what = 0;
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
