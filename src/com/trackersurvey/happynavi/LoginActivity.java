package com.trackersurvey.happynavi;



import java.io.File;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.Des;
import com.trackersurvey.helper.MD5Util;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.GetRegisterInformationCz;
import com.trackersurvey.httpconnection.PostLoginData;
import com.trackersurvey.httpconnection.getRegisterInformation;
import com.trackersurvey.httpconnection.getRegisterInformationEn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{
	public static final String times ="timeCount";
	public static final String winWidth = "winWidth";
	public static final String winHeight = "winHeight";
	public static final String PPISCALE = "ppiScale";
	public String login_url = null;
	public String register_information_url = null;
	public static final String mobConnectFirst = "mobConnectFirstUse";
	public String result;
	public String information;
	public EditText uid;
	public EditText pwd;
	public CheckBox remeber_pwd;
	public CheckBox agree_protocol;
	public TextView appVersion;
    //public TextView forgetpassword;
    public TextView protocal;
    public Button register;
    public Button login;
	String show;
	public String s_id;
	private SharedPreferences sp;//android系统下用于数据存贮的一个方便的API
	long lastClick; //用户上次单击时间
	private ProgressDialog proDialog = null;
	private int l = TabHost_Main.l;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
			/**
			 * 解决《安装应用之后点击了打开
			 然后按home键 这个时候应用程序进入后台 我点击手机桌面的时候图片启动应用程序 程序重新再次启动 而不是后台的程序返回到前台》
			
			 * */
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
		            //结束你的activity
		            finish();
		            return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		AppManager.getAppManager().addActivity(this);
		
		// sp 初始化 
        sp=getSharedPreferences("config",MODE_PRIVATE);//私有参数
        //是否第一次使用应用
        int timeCount = sp.getInt(times, 0);
        if(timeCount == 0){
        	firstTimeDone();
        }
        Editor editor = sp.edit();
        editor.putInt(times, timeCount+1);
        editor.commit();
        
		String lastId=sp.getString("lastid", "0");
		String nickname=sp.getString("nickname", "");
		String headphoto=sp.getString("headphoto", "");
		String deviceid=sp.getString("deviceid", "null");
		if(deviceid.equals("null")){//没有设置设备id，需要设置
			editor.putString("deviceid", Common.setDeviceId(getApplicationContext()));
			editor.commit();
		}
		Log.i("phonelog","getDeviceName :"+Common.getDeviceName()); 
        if(!lastId.equals("0")){
        	//Common.userId=lastId;
        	if(!nickname.equals("")){
        		Common.NickName=nickname;
        	}
        	if(!headphoto.equals("")){
        		//Common.pic=headphoto;
        	}
        	Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//检验上次登录的id，存在id和密码，跳转
			startActivity(intent);
			
        }
		uid = (EditText) findViewById(R.id.et_userid);
		pwd = (EditText) findViewById(R.id.et_password);
		//remeber_pwd=(CheckBox) findViewById(R.id.checkBox);
		agree_protocol=(CheckBox) findViewById(R.id.agree);
		appVersion=(TextView) findViewById(R.id.app_version);
		String version = null;
		if(Common.version == null ||Common.version.equals("")){
			version = Common.getAppVersionName(getApplicationContext());
		}else{
			version = Common.version;
		}
		appVersion.setText("V"+version);
		//forgetpassword=(TextView) findViewById(R.id.forgetpassword);
		protocal=(TextView) findViewById(R.id.protocal);
		protocal.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
	    register=(Button) findViewById(R.id.btn_register);
	    login=(Button) findViewById(R.id.btn_login);
	    register.setOnClickListener(this);
	    login.setOnClickListener(this);
	    
//	    forgetpassword.setOnClickListener(new OnClickListener() {
//			
//	    	
//			public void onClick(View v) {
//				Intent intent=new Intent(LoginActivity.this,ForgetPassword.class);
//				startActivity(intent);		
//				
//			}
//		});
		protocal.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			AlertDialog alert =new AlertDialog.Builder(LoginActivity.this).create();
				alert.setTitle(getResources().getString(R.string.tips_dlgtle_protocol));
				alert.setMessage(getResources().getString(R.string.tips_dlgmsg_protocol1)
						+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol2)
						+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol3)
						+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol4)
						+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol5));
				alert.setButton(DialogInterface.BUTTON_NEGATIVE,getResources().getString(R.string.close),  new DialogInterface.OnClickListener() {
			
		
			public void onClick(DialogInterface dialog, int which) {
										
			}
		});
				
				
				
				alert.show();
				
			}
		});
	    
	    
		
		
        //获取SP存储的数据
        String saveId=sp.getString("id", "");
        String savePsw=sp.getString("psw", "");
        uid.setText(saveId);
        pwd.setText(savePsw);
		
        if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
        login_url = Common.url+"userLogin.aspx";
        register_information_url = Common.url+"reqRegInfo.aspx";
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//解决多次点击重复创建activity
        if (System.currentTimeMillis() - lastClick <= 1000)  
        {  
            //Log.i("LogDemo", "点太快"); 
            return;  
        }  
        lastClick = System.currentTimeMillis();  
        //Log.i("LogDemo", "响应点击");
        switch(v.getId()){
        case R.id.btn_register:
        	if(l==0){
        		register_new();
        	}
        	if(l==1){
        		register_new_en();
        	}
        	if(l==2) {
        		register_new_cz();
        	}
        	break;
        case R.id.btn_login:
        	
        	submit();
        	break;
        }
	}
	
	public void register_new()
	{
		getRegisterInformation gri = new getRegisterInformation(handler_register_information, register_information_url,
				Common.getDeviceId(getApplicationContext()));
	    gri.start();
		
	}
	
	public void register_new_en(){
		getRegisterInformationEn gri_en = new getRegisterInformationEn(handler_register_information, register_information_url,
				Common.getDeviceId(getApplicationContext()));
		gri_en.start();
	}
	
	public void register_new_cz(){
		GetRegisterInformationCz gri_cz = new GetRegisterInformationCz(handler_register_information, register_information_url,
				Common.getDeviceId(getApplicationContext()));
		gri_cz.start();
	}
	
	public void submit()
	{
		
		String s_pwd;
		s_id=uid.getText().toString();
		s_pwd=pwd.getText().toString();
		String s_pwd_md5=MD5Util.string2MD5(s_pwd);
		if(s_id.equals("")||s_pwd.equals(""))//判断账号密码不能为空
	    {
	    	  
	    	 Toast.makeText(this,getResources().getString(R.string.idpwdcannotnull), Toast.LENGTH_SHORT).show();
	    }
	    else
	    {
		    	//勾选 记录用户名和密码
				//获取到一个参数文件编辑器
			    Editor editor=sp.edit();
				//Log.i("checked", "1");
			    editor.putString("id", s_id);
				//Log.i("checked", "2");
			    editor.putString("psw",s_pwd);
			    //Log.i("checked", "3");
		        editor.commit();//把数据保存到sp里
		        //Log.i("checked", "4");
			   //Toast.makeText(getApplicationContext(), "以保存", 1).show();
		      
	
	    	//判断是否进行MD5
				//若版本号小于"1.2.5"则不加密
				boolean jiami=( Common.version.compareTo("1.2.5")>=0);
				if(jiami)
	    	    {show=s_id+"!"+s_pwd_md5; 
	    	    }
				else{
					show=s_id+"!"+s_pwd; 	
					
				}
	    	if(agree_protocol.isChecked())
	    	{
	    	showDialog(getResources().getString(R.string.tips_dlgtle_login),
	    			getResources().getString(R.string.tips_dlgmsg_login));
	    	//Log.i("phonelog", s_pwd+"--->"+s_pwd_md5);
	    	PostLoginData pld=new PostLoginData(handler_login, login_url, show,Common.getDeviceId(getApplicationContext()));
	    	//PostLoginData pld=new PostLoginData(handler_login, "http://211.87.235.120:8080/footPrint/user/login", show,Common.getDeviceId(getApplicationContext()));
	    	Log.i("LoginMsg", show);
	        pld.start();
	    	}else
	    	{
	    		Toast.makeText(this,getResources().getString(R.string.tips_agreeprotocol), Toast.LENGTH_SHORT).show();
	    		
	    	}
	        
	     }
		
	}
	
	
	
	public void visiterlogin(View v)
	{
		
	
		Common.NickName="???";
		Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//登陆成功跳转
		startActivity(intent);
		//finish();
	
	}
	 @Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			// TODO Auto-generated method stub
			if(event.getKeyCode()== KeyEvent.KEYCODE_BACK&&event.getAction() == KeyEvent.ACTION_DOWN)//两个条件缺一不可
			{
				AppManager.getAppManager().AppExit(getApplicationContext());
				//finish();
	            return true; 
			}
			else{
				return super.dispatchKeyEvent(event);
			}
		}

	 //实现应用字体不受系统控制
	public Resources getResources() {  
	    Resources res = super.getResources();    
	    Configuration config=new Configuration();    
	    config.setToDefaults();    
	    res.updateConfiguration(config,res.getDisplayMetrics() );  
	    return res;  
	}  
	
	
	
	
	//登陆接受消息
	private Handler handler_login = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			dismissDialog();
			switch (msg.what) {
			case 0:// 登陆成功
				Editor editor=sp.edit();
				editor.putString("lastid", s_id);
				
				//Common.userId=s_id;
				result = (msg.obj.toString().trim());
				try {
					result=Des.decrypt(result);
				} catch (Exception e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
				
				 // Log.i("LogDemo", "dereadLine:"+result);  
				
				String[] get=result.split("!");
				if(get.length>=3){
	        		
	        		try {
	        			//Common.pic=get[1];//存图片字符串
						Common.NickName=get[2];//昵称
						
						
						editor.putString("nickname", get[2]);
		        		editor.putString("headphoto", get[1]);
	        		} catch (Exception e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
	        		editor.putString("nickname", get[2]);
	        		editor.putString("headphoto", get[1]);
				}
				editor.commit();
				result=null;
				Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//登陆成功跳转
				startActivity(intent);
				//finish();
				break;

			case 1:

				Toast.makeText(LoginActivity.this, getResources().getString(R.string.tips_postfail),0).show();
				
				  
				
				break;
			case 4:
				//result = (msg.obj.toString().trim());

				
				ToastUtil.show(LoginActivity.this,getResources().getString(R.string.tips_loginfail_noid));
				  
				
				break;
			case 5:
				//result = (msg.obj.toString().trim());

				ToastUtil.show(LoginActivity.this,getResources().getString(R.string.tips_loginfail_nopwd));
				  
				
				break;
			case 10:
				//result = (msg.obj.toString().trim());

				ToastUtil.show(LoginActivity.this,getResources().getString(R.string.tips_netdisconnect));
				break;
				
				
			case 6:
				//result = (msg.obj.toString().trim());

				
				
				  
				
				break;
				

			}
		}

	};
	
	
	//获得注册界面的接收
	private Handler handler_register_information = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:// 获得注册界面信息成功
				information = (msg.obj.toString().trim());
				
				Intent intent=new Intent(LoginActivity.this,Register.class);
				

				Bundle bundle=new Bundle();
				bundle.putCharSequence("information", information);
			    intent.putExtras(bundle);
				if(information!=null){
			       startActivity(intent);
			    }
			    information=null;
				break;

			case 1:
				//information = (msg.obj.toString().trim());
				ToastUtil.show(LoginActivity.this, getResources().getString(R.string.tips_postfail));
				
				break;
			case 10:// 连接失败

				//information = (msg.obj.toString().trim());
				ToastUtil.show(LoginActivity.this, getResources().getString(R.string.tips_netdisconnect));
				
				break;

			}
		}

	};
	
	/**
	 * 显示进度条对话框
	 */
	public void showDialog(String title,String message) {
		if (proDialog == null)
			proDialog = new ProgressDialog(this);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setIndeterminate(false);
		proDialog.setCancelable(false);
		proDialog.setTitle(title);
		proDialog.setMessage(message);
		proDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}
	
	/**
	 * 只在安装后第一次使用应用执行
	 */
	void firstTimeDone(){
		//获取用户手机屏幕分辨率、ppi与dip比率,写入sharedPreference
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Editor editorSp = sp.edit();
		editorSp.putInt(winWidth, dm.widthPixels);
		editorSp.commit();
		editorSp.putInt(winHeight, dm.heightPixels);
		editorSp.commit();
		
		float scale = getApplicationContext().getResources().getDisplayMetrics().density;  
		editorSp.putFloat(PPISCALE, scale);
		editorSp.putInt(mobConnectFirst, 0);
		editorSp.commit();
	
		
	}
	
}
