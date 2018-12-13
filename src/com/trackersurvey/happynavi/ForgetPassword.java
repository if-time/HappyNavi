package com.trackersurvey.happynavi;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.MD5Util;
import com.trackersurvey.httpconnection.getForgetPassword;
import com.trackersurvey.httpconnection.postNewPassword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class ForgetPassword extends Activity {
	EditText id;
	//EditText ps_information;
	String getInformation;
	String s_id;
	String s_ps_information;
	String post_newpassword_url=null;
	String getforgetpossword_url = null;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.forgetpassword);
		AppManager.getAppManager().addActivity(this);
		id = (EditText) findViewById(R.id.forget_id);
		//ps_information = (EditText) findViewById(R.id.forget_information);
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		post_newpassword_url = Common.url+"request.aspx";
		getforgetpossword_url = Common.url+"request.aspx";
	}

	public void getPassword(View v) {
		s_id = id.getText().toString();
		//s_ps_information = ps_information.getText().toString();

		if (s_id.contains("!") ||
			s_id.contains("?") || 
			s_id.contains("#") ||
            s_ps_information.contains("!") ||
            s_ps_information.contains("?")||
            s_ps_information.contains("#")
           )
		{
			Toast.makeText(this, getResources().getString(R.string.tips_inputillegal), Toast.LENGTH_SHORT).show();
		}
			
		else{
			
			if(s_id==null||
			   s_id.equals("")||
			   s_ps_information==null||
			   s_ps_information.equals(""))
			{
				Toast.makeText(this,getResources().getString(R.string.tips_inputcannotnull), Toast.LENGTH_SHORT).show();
			}	
			else
			{
				getForgetPassword gfp=new getForgetPassword(handler_forgetpassword, getforgetpossword_url, s_id,
						Common.getDeviceId(getApplicationContext()));
		        gfp.start();
			
			}
			
		}
			

	}

	
	
	
	public void pop()
	{
		//Log.i("yes","yes");
		
		AlertDialog ad=new AlertDialog.Builder(ForgetPassword.this).create();
		 ad.setTitle(getResources().getString(R.string.tips_pleaseinput));
		 //Log.i("yes","yes-on");
		 final EditText et=new EditText(ForgetPassword.this);
		 ad.setView(et);
		 ad.setTitle(getResources().getString(R.string.tips_inputnewpwd));
		 
		 ad.setButton(getResources().getString(R.string.submit), new  DialogInterface.OnClickListener() {
			
			
			public void onClick(DialogInterface dialog, int which) {
			  String newPsw=et.getText().toString();
				
				if(newPsw.equals(""))
			  {
				  Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_pwdnull),
							 Toast.LENGTH_SHORT).show();
			  }
			  
			  else
			  {
				  if(newPsw.contains("!")||
				     newPsw.contains("?")||
					 newPsw.contains("#"))
				  {
					 Toast.makeText(ForgetPassword.this, getResources().getString(R.string.tips_inputillegal),
								 Toast.LENGTH_SHORT).show();
					  
				  }
				  {
					  
					//判断是否进行MD5
						//若版本号小于"1.2.5"则不加密
					  String version = null;
						if(Common.version == null ||Common.version.equals("")){
							version = Common.getAppVersionName(getApplicationContext());
						}else{
							version = Common.version;
						}
						boolean jiami = true;
						if(version != null && !version.equals("")){
							jiami=( version.compareTo("1.2.5")>=0);
						}
						
						
					  String postPsw;
					  if(jiami)
						postPsw=MD5Util.string2MD5(newPsw);
					  else
						  postPsw=newPsw;
						
						
					  postNewPassword pnp = new postNewPassword(handler_postNewPassword, post_newpassword_url,s_id, postPsw,Common.getDeviceId(getApplicationContext()));
					    pnp.start();  
					  
				  }
				  
				  
				  
			  }
			
			}
		});
		 
		ad.show();
	}
	
	
	
	
	
	
	// 接受密码提示消息
	private Handler handler_forgetpassword = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:// 获得密码提示信息成功

				String result = (msg.obj.toString().trim());
				
				getInformation=result;
 
//判断是否进行MD5
	//若版本号小于"1.2.5"则不加密
	boolean jiami=( Common.version.compareTo("1.2.5")>=0);
	
	String zhongjianliang;
	if(jiami)
	zhongjianliang=MD5Util.string2MD5(s_ps_information);
	else
	zhongjianliang=s_ps_information;
  
				if(getInformation.equals(  zhongjianliang)) 
				
				{
					
			
				 pop();
				
				
				
				}
				else{
					Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_secpwderror),
							 Toast.LENGTH_SHORT).show();
				}
				 
				 
				 
				break;

			case 1:
				Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_postfail),
						Toast.LENGTH_SHORT).show();

				break;
			case 4:
				result = (msg.obj.toString().trim());

				Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_idnotexist),
						Toast.LENGTH_SHORT).show();

				break;
			case 5:
				result = (msg.obj.toString().trim());

				Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_pwdtiperrpor),
						Toast.LENGTH_SHORT).show();

				break;
			case 10:
				result = (msg.obj.toString().trim());

				Toast.makeText(ForgetPassword.this,getResources().getString(R.string.tips_netdisconnect),
						Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};

	private Handler handler_postNewPassword = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:// 获得密码成功
				String result = (msg.obj.toString().trim());
				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				//result = (msg.obj.toString().trim());

			Toast.makeText(ForgetPassword.this, "修改密码成功" ,Toast.LENGTH_SHORT).show();
			Intent intent=new Intent(ForgetPassword.this,LoginActivity.class);
			startActivity(intent);		
 
				break;

			case 1:// 注册不成功但连接了

				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				

				Toast.makeText(ForgetPassword.this, "服务器忙，请稍后再试",
						Toast.LENGTH_SHORT).show();
				break;
			case 10:// 连接失败

				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				

				Toast.makeText(ForgetPassword.this, "连接服务器失败，请检查网络连接" ,
						Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};
	
	
}
