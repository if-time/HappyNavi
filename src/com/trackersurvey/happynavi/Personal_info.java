package com.trackersurvey.happynavi;



import java.util.Locale;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.Des;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.httpconnection.getMyInformation;
import com.trackersurvey.httpconnection.getPasssword;
import com.trackersurvey.httpconnection.getRegisterInformation;
import com.trackersurvey.httpconnection.getRegisterInformationEn;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;


public class Personal_info extends Activity implements OnClickListener,OnTouchListener{
	private MyLinearLayout back;
	private TextView title;
	private Button titleRightBtn;
	private MyLinearLayout mydata;
//	private MyLinearLayout mygroup;
	private MyLinearLayout changepwd;
	private MyLinearLayout sportdata;
	private MyLinearLayout logout;
	
	
	private ProgressDialog proDialog = null;
	
	private long lastClick; //用户上次单击时间
	private String information;
	private String register_information;
	private String passowrd_information;
	private String my_information_url = null;
	private String get_password_url=null;
	private String register_information_url = null;
	private int l = TabHost_Main.l;
	private SharedPreferences sp;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//切换中英文
		//----------------------------------------------------//
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.personal_info);
		
		Resources resources = getResources();
		Configuration configure = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TabHost_Main.l==0){
			configure.locale = Locale.CHINESE;
		}
		if(TabHost_Main.l==1){
			configure.locale = Locale.ENGLISH;
		}
		if(TabHost_Main.l==2) {
			configure.locale = new Locale("cs", "CZ");
		}
		resources.updateConfiguration(configure, dm);
		//----------------------------------------------------//
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		
		
		
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.person_info));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setVisibility(View.INVISIBLE);
		
		mydata=(MyLinearLayout) findViewById(R.id.Linearlayout_myinfo);
		mydata.setOnClickListener(this);
		mydata.setOnTouchListener(this);
//		mygroup=(MyLinearLayout) findViewById(R.id.Linearlayout_mygroup);
//		mygroup.setOnClickListener(this);
//		mygroup.setOnTouchListener(this);
		changepwd=(MyLinearLayout) findViewById(R.id.Linearlayout_changepwd);
		changepwd.setOnClickListener(this);
		changepwd.setOnTouchListener(this);
		sportdata=(MyLinearLayout) findViewById(R.id.Linearlayout_sportdata);
		sportdata.setOnClickListener(this);
		sportdata.setOnTouchListener(this);
		logout=(MyLinearLayout) findViewById(R.id.Linearlayout_logout);
		logout.setOnClickListener(this);
		logout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(getResources().getColor(R.color.red_pressed));
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundColor(getResources().getColor(R.color.red));
					break;
				default:
					break;
				}
				return false;
			}
		});
		
		sp=getSharedPreferences("config", MODE_PRIVATE);
		
		if (proDialog == null){
			proDialog = new ProgressDialog(this);
		}
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		my_information_url = Common.url+"request.aspx";
		get_password_url=Common.url+"request.aspx";
		register_information_url = Common.url+"reqRegInfo.aspx";
		if(l==0){			
			getRegisterInformation gri = new getRegisterInformation(handler_register_information, register_information_url,
					Common.getDeviceId(getApplicationContext()));
			gri.start();
		}
		if(l==1){
			getRegisterInformationEn gri_en = new getRegisterInformationEn(handler_register_information, register_information_url,
					Common.getDeviceId(getApplicationContext()));
			gri_en.start();
		}
		
		
	}
	//获得登陆界面的接收
			private Handler handler_register_information = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					switch (msg.what) {
					case 0:// 获得注册界面信息成功
						register_information = (msg.obj.toString().trim());
						try {
							register_information=Des.decrypt(register_information);
						} catch (Exception e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
						Common.dismissDialog(proDialog);
						
						break;

					case 1:// 注册不成功但连接了

						
						Common.dismissDialog(proDialog);
						Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_postfail),
								Toast.LENGTH_SHORT).show();
						break;
					case 10:// 连接失败

						
						
						Common.dismissDialog(proDialog);
						Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_netdisconnect) ,
								Toast.LENGTH_SHORT).show();
						break;

					}
				}

			};

			
	//接受个人信息
			private Handler handler_my_information = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					switch (msg.what) {
					case 0:// 获得注册界面信息成功
						information = (msg.obj.toString().trim());
						
						String eninformations = null;
						try {
							eninformations=Des.decrypt(information);
						} catch (Exception e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
						String informations[] =eninformations.split("[?]");
						if(informations.length<=1){
							Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_postfail),
									Toast.LENGTH_SHORT).show();
							return;
						}
						Intent intent=new Intent(Personal_info.this,MyInformation.class);
						

						Bundle bundle=new Bundle();
						bundle.putCharSequence("register_information", register_information);
						bundle.putCharSequence("information", information);
					    intent.putExtras(bundle);
					    
					    Common.dismissDialog(proDialog);
					    if(information!=null){
					       startActivity(intent);
					    }
					    information=null;
						break;

					case 1:
						Common.dismissDialog(proDialog);
						Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_postfail),
								Toast.LENGTH_SHORT).show();
						break;
					case 10:// 连接失败

						
						Common.dismissDialog(proDialog);
						Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_netdisconnect) ,
								Toast.LENGTH_SHORT).show();
						break;

					}
				}

			};
			
			//获得密码的接收
				private Handler handler_getPassword = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						switch (msg.what) {
						case 0:// 获得密码成功
							passowrd_information = (msg.obj.toString().trim());
							Common.dismissDialog(proDialog);
						
							Intent intent2=new Intent(Personal_info.this,ChangePassword.class);//登陆成功跳转
							  
				            Bundle bundle2=new Bundle();
							bundle2.putCharSequence("old_psw",passowrd_information );
							intent2.putExtras(bundle2);
								  
						    startActivity(intent2);
						    passowrd_information=null;
							  
							break;

						case 1:// 不成功但连接了

							
							Common.dismissDialog(proDialog);
							Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_postfail),
									Toast.LENGTH_SHORT).show();
							break;
						case 10:// 连接失败

							
							Common.dismissDialog(proDialog);
							Toast.makeText(Personal_info.this,getResources().getString(R.string.tips_netdisconnect),
									Toast.LENGTH_SHORT).show();
							break;

						}
					}

				};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(getResources().getColor(R.color.listitem_pressed));
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundColor(getResources().getColor(R.color.listitem_normal));
					break;
				default:
					break;
				}
				return false;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (System.currentTimeMillis() - lastClick <= 1000)  
		        {  
		           //Log.i("LogDemo", "点太快");
		            return;  
		        }  
		        lastClick = System.currentTimeMillis();
				switch (v.getId()) {
				case R.id.title_back:
					finish();
					break;
				case R.id.Linearlayout_myinfo:
					Common.showDialog(proDialog,getResources().getString(R.string.tip), getResources().getString(R.string.tips_dlgmsg_init));
					getMyInformation gmi = new getMyInformation(handler_my_information, 
							my_information_url,Common.getUserId(getApplicationContext()),Common.getDeviceId(getApplicationContext()));
				    gmi.start();
					break;
//				case R.id.Linearlayout_mygroup:
//					startActivity(new Intent(Personal_info.this,MyGroupActivity.class));
//					
//					break;
				case R.id.Linearlayout_changepwd:
					Common.showDialog(proDialog,getResources().getString(R.string.tip),getResources().getString(R.string.tips_dlgmsg_init));
					getPasssword gp = new getPasssword(handler_getPassword, get_password_url,
							Common.getDeviceId(getApplicationContext()),Common.getUserId(getApplicationContext()));
				    gp.start();
					break;
				case R.id.Linearlayout_sportdata:
					startActivity(new Intent(Personal_info.this,PersonalSportData.class));
					
					break;
				case R.id.Linearlayout_logout:
					String msg=getResources().getString(R.string.exitdlg1);
					if(Common.isRecording){
						msg=getResources().getString(R.string.exitdlg2);
					}
					CustomDialog.Builder builder_logout = new CustomDialog.Builder(Personal_info.this);
					builder_logout.setTitle(getResources().getString(R.string.exit));
					builder_logout.setMessage(msg);
					builder_logout.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					builder_logout.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Common.sendOffline(Common.getDeviceId(getApplicationContext()),Personal_info.this);
							//Common.userId="0";
							Common.layerid_main=0;
							Editor editor=sp.edit();
							
							editor.putString("lastid", "0");
							editor.putString("headphoto", "");
							editor.commit();
							
							//AppManager.getAppManager().finishActivity(MainActivity.class);
							Intent intent=new Intent();
							intent.setClass(Personal_info.this, StartPage.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							dialog.dismiss();
						}
					});
					builder_logout.create().show();
					break;
				
				}
			}
}
