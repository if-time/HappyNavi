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
	private SharedPreferences sp;//androidϵͳ���������ݴ�����һ�������API
	long lastClick; //�û��ϴε���ʱ��
	private ProgressDialog proDialog = null;
	private int l = TabHost_Main.l;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
			/**
			 * �������װӦ��֮�����˴�
			 Ȼ��home�� ���ʱ��Ӧ�ó�������̨ �ҵ���ֻ������ʱ��ͼƬ����Ӧ�ó��� ���������ٴ����� �����Ǻ�̨�ĳ��򷵻ص�ǰ̨��
			
			 * */
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
		            //�������activity
		            finish();
		            return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		AppManager.getAppManager().addActivity(this);
		
		// sp ��ʼ�� 
        sp=getSharedPreferences("config",MODE_PRIVATE);//˽�в���
        //�Ƿ��һ��ʹ��Ӧ��
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
		if(deviceid.equals("null")){//û�������豸id����Ҫ����
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
        	Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//�����ϴε�¼��id������id�����룬��ת
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
	    
	    
		
		
        //��ȡSP�洢������
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
		//�����ε���ظ�����activity
        if (System.currentTimeMillis() - lastClick <= 1000)  
        {  
            //Log.i("LogDemo", "��̫��"); 
            return;  
        }  
        lastClick = System.currentTimeMillis();  
        //Log.i("LogDemo", "��Ӧ���");
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
		if(s_id.equals("")||s_pwd.equals(""))//�ж��˺����벻��Ϊ��
	    {
	    	  
	    	 Toast.makeText(this,getResources().getString(R.string.idpwdcannotnull), Toast.LENGTH_SHORT).show();
	    }
	    else
	    {
		    	//��ѡ ��¼�û���������
				//��ȡ��һ�������ļ��༭��
			    Editor editor=sp.edit();
				//Log.i("checked", "1");
			    editor.putString("id", s_id);
				//Log.i("checked", "2");
			    editor.putString("psw",s_pwd);
			    //Log.i("checked", "3");
		        editor.commit();//�����ݱ��浽sp��
		        //Log.i("checked", "4");
			   //Toast.makeText(getApplicationContext(), "�Ա���", 1).show();
		      
	
	    	//�ж��Ƿ����MD5
				//���汾��С��"1.2.5"�򲻼���
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
		Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//��½�ɹ���ת
		startActivity(intent);
		//finish();
	
	}
	 @Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			// TODO Auto-generated method stub
			if(event.getKeyCode()== KeyEvent.KEYCODE_BACK&&event.getAction() == KeyEvent.ACTION_DOWN)//��������ȱһ����
			{
				AppManager.getAppManager().AppExit(getApplicationContext());
				//finish();
	            return true; 
			}
			else{
				return super.dispatchKeyEvent(event);
			}
		}

	 //ʵ��Ӧ�����岻��ϵͳ����
	public Resources getResources() {  
	    Resources res = super.getResources();    
	    Configuration config=new Configuration();    
	    config.setToDefaults();    
	    res.updateConfiguration(config,res.getDisplayMetrics() );  
	    return res;  
	}  
	
	
	
	
	//��½������Ϣ
	private Handler handler_login = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			dismissDialog();
			switch (msg.what) {
			case 0:// ��½�ɹ�
				Editor editor=sp.edit();
				editor.putString("lastid", s_id);
				
				//Common.userId=s_id;
				result = (msg.obj.toString().trim());
				try {
					result=Des.decrypt(result);
				} catch (Exception e1) {
					// TODO �Զ����ɵ� catch ��
					e1.printStackTrace();
				}
				
				 // Log.i("LogDemo", "dereadLine:"+result);  
				
				String[] get=result.split("!");
				if(get.length>=3){
	        		
	        		try {
	        			//Common.pic=get[1];//��ͼƬ�ַ���
						Common.NickName=get[2];//�ǳ�
						
						
						editor.putString("nickname", get[2]);
		        		editor.putString("headphoto", get[1]);
	        		} catch (Exception e) {
						// TODO �Զ����ɵ� catch ��
						e.printStackTrace();
					}
	        		editor.putString("nickname", get[2]);
	        		editor.putString("headphoto", get[1]);
				}
				editor.commit();
				result=null;
				Intent intent=new Intent(LoginActivity.this,TabHost_Main.class);//��½�ɹ���ת
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
	
	
	//���ע�����Ľ���
	private Handler handler_register_information = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:// ���ע�������Ϣ�ɹ�
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
			case 10:// ����ʧ��

				//information = (msg.obj.toString().trim());
				ToastUtil.show(LoginActivity.this, getResources().getString(R.string.tips_netdisconnect));
				
				break;

			}
		}

	};
	
	/**
	 * ��ʾ�������Ի���
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
	 * ���ؽ������Ի���
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}
	
	/**
	 * ֻ�ڰ�װ���һ��ʹ��Ӧ��ִ��
	 */
	void firstTimeDone(){
		//��ȡ�û��ֻ���Ļ�ֱ��ʡ�ppi��dip����,д��sharedPreference
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
