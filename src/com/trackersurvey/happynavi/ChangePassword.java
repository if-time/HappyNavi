package com.trackersurvey.happynavi;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.MD5Util;
import com.trackersurvey.httpconnection.postNewPassword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ChangePassword  extends Activity{
	private LinearLayout back;
	private ProgressDialog proDialog=null;
	EditText et1,et2,et3;
	String old_password;
	//String post_newpassword_url=Common.url+"request.aspx";
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.change_password);
		AppManager.getAppManager().addActivity(this);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		// 接收到的登陆界面显示信息
		 old_password = bundle.getString("old_psw","0");

	//	Log.i("password", information);
		//Log.i("password", "hello");
		
		
		back=(LinearLayout)findViewById(R.id.changepwd_back);
		et1=(EditText) findViewById(R.id.psw);
		et2=(EditText) findViewById(R.id.psw2);
		et3=(EditText) findViewById(R.id.psw3);
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});	
		if (proDialog == null){
			proDialog = new ProgressDialog(this);
		}
	}

public void submit(View v)
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
	
	String old=et1.getText().toString();
	String new_1=et2.getText().toString();
	String new_2=et3.getText().toString();
	//md5转换 进行判断
	if(jiami)
	 old=MD5Util.string2MD5(old);
	
	
	
	
	if(old.equals(old_password))
	{
		
		if(new_1.equals("")||new_1==null||new_2.equals("")||new_2==null)
		{
			Toast.makeText(this,getResources().getString(R.string.tips_npwdnull),Toast.LENGTH_SHORT).show();
		}
		
		else
			if(new_1.equals(new_2))
			{
//				String show=old  +"!"+new_1+"!"+new_2; 
//				Toast.makeText(this, show,Toast.LENGTH_SHORT).show();
			
			if(new_1.contains("!")||new_1.contains("?")||new_1.contains("#"))
			{
				Toast.makeText(this,getResources().getString(R.string.tips_npwdillegal),Toast.LENGTH_SHORT).show();
				
			}
			else{
				String newPass;
				if(jiami)
					 newPass=MD5Util.string2MD5(new_1);
				else{
					 newPass=(new_1);
				}
				Common.showDialog(proDialog,getResources().getString(R.string.tip),
						getResources().getString(R.string.tips_changing));
				String post_newpassword_url=null;
				if(Common.url != null && !Common.url.equals("")){
					post_newpassword_url = Common.url + "request.aspx";
				}else{
					post_newpassword_url = getResources().getString(R.string.url)+"request.aspx";
				}
				postNewPassword pnp = new postNewPassword(handler_postNewPassword, post_newpassword_url,Common.getUserId(this),newPass,Common.getDeviceId(getApplicationContext()));
			    pnp.start();
			}
			
			}
			else
			{
				Toast.makeText(this,R.string.tips_npwddiffer,Toast.LENGTH_SHORT).show();
			}
			
	}
	
	else{
		
		Toast.makeText(this,getResources().getString(R.string.tips_opwderror),Toast.LENGTH_SHORT).show();
	}
	
	
	

}
	
	
	
	
private Handler handler_postNewPassword = new Handler() {

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		
		case 0:// 获得密码成功
			Common.dismissDialog(proDialog);
			String result = (msg.obj.toString().trim());
			Toast.makeText(ChangePassword.this,getResources().getString(R.string.tips_pwdchangeok) ,Toast.LENGTH_SHORT).show();
			finish();
  
			break;

		case 1:// 注册不成功但连接了
			Common.dismissDialog(proDialog);
			Toast.makeText(ChangePassword.this,getResources().getString(R.string.tips_postfail),
					Toast.LENGTH_SHORT).show();
			break;
		case 10:// 连接失败
			Common.dismissDialog(proDialog);
			Toast.makeText(ChangePassword.this,getResources().getString(R.string.tips_netdisconnect),
					Toast.LENGTH_SHORT).show();
			break;

		}
	}

};

	
	
	
}
