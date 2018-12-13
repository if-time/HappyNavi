package com.trackersurvey.happynavi;




import java.util.Locale;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.offlinemap.OfflineMapActivity;

import android.app.Activity;

import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.widget.ImageView;

import android.widget.TextView;



public class Personal_main extends Activity implements OnClickListener,OnTouchListener{
	
	private MyLinearLayout headinfo;
	private MyLinearLayout personalInfo;
	private MyLinearLayout myGroup;
	private MyLinearLayout myAlbum;
	private MyLinearLayout offlineMap;
	private MyLinearLayout settings;
	private MyLinearLayout about;
	private ImageView user_img;
	//private Button changeUser;
	private TextView user_name;
	private TextView user_id;
	private TextView divider;
	private SharedPreferences sp;
	public static Personal_main instance = null;
	//private FileInfo fileInfo = null;//新版apk文件
	//private ProgressDialog proDialog = null;
	int select=0;
	long lastClick; //用户上次单击时间
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.personal_main);
		
		// 切换中英文
		//------------------------------------------------------//
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
		//------------------------------------------------------//
		
		AppManager.getAppManager().addActivity(this);
		
		if(Common.isVisiter()){
			Common.DialogForVisiter(this);
			
		}
		else{
			init();
		}
	}
	public void init(){
			    headinfo=(MyLinearLayout) findViewById(R.id.Linearlayout_headinfo);
			    headinfo.setOnClickListener(this);
			    headinfo.setOnTouchListener(this);
			    personalInfo=(MyLinearLayout) findViewById(R.id.Linearlayout_personalinfo);
			    personalInfo.setOnClickListener(this);
			    personalInfo.setOnTouchListener(this);
			    myGroup = (MyLinearLayout) findViewById(R.id.Linearlayout_mygroup);
			    myGroup.setOnClickListener(this);
			    myGroup.setOnTouchListener(this);
			    myAlbum=(MyLinearLayout) findViewById(R.id.Linearlayout_album);
			    myAlbum.setOnClickListener(this);
			    myAlbum.setOnTouchListener(this);
			    offlineMap=(MyLinearLayout) findViewById(R.id.Linearlayout_offlinemap);
			    offlineMap.setOnClickListener(this);
			    offlineMap.setOnTouchListener(this);
			    settings=(MyLinearLayout) findViewById(R.id.Linearlayout_setting);
			    settings.setOnClickListener(this);
			    settings.setOnTouchListener(this);
			    about=(MyLinearLayout) findViewById(R.id.Linearlayout_about);
			    about.setOnClickListener(this);
			    about.setOnTouchListener(this);
			    sp=getSharedPreferences("config",MODE_PRIVATE);
				
				Bitmap img;
				String headphoto=sp.getString("headphoto", "");
				if(headphoto==null||headphoto.equals("")||headphoto.equals("null"))
				{
					img=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_register);
				}
				else{
					byte[] decode1 = Base64.decode(headphoto,Base64.DEFAULT);
			        img= BitmapFactory.decodeByteArray(decode1, 0, decode1.length);
			     	
				}
				
				user_img=(ImageView)findViewById(R.id.mine_mian_img);
				user_name=(TextView)findViewById(R.id.mine_main_name);
				user_id=(TextView)findViewById(R.id.mine_main_phone);
				divider=(TextView)findViewById(R.id.dividertxt);
				divider.setVisibility(View.VISIBLE);
				user_img.setImageBitmap(img);
			
				user_name.setText(Common.NickName);
				user_id.setText(Common.getUserId(this));
				
				
				
	}
	
			
			public void doInResume(){
				String userId = Common.getUserId(this);
				if(userId.equals("")||userId.equals("0")){
					Editor editor=sp.edit();
					
					editor.putString("lastid", "0");
					editor.commit();
					
					Intent intent=new Intent();
					intent.setClass(Personal_main.this, LoginActivity.class);
					startActivity(intent);
					finish();
					//return;
				}
				Bitmap img;
				//final Bitmap img=BitmapFactory.decodeResource(this.getResources(), R.drawable.headicon);
			    
				String headphoto=sp.getString("headphoto", "");
				if(headphoto==null||headphoto.equals("")||headphoto.equals("null"))
				{
					img=BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_register);
				}
				else{
					byte[] decode1 = Base64.decode(headphoto,Base64.DEFAULT);
			        img= BitmapFactory.decodeByteArray(decode1, 0, decode1.length);
			     	
				}
				user_img.setImageBitmap(img);
				user_name.setText(Common.NickName);
				Editor editor=sp.edit();
				editor.putString("nickname", Common.NickName);
        		//editor.putString("headphoto", Common.pic);
				editor.commit();
			}
			@Override
			protected void onResume() {
				// TODO Auto-generated method stub
				if(!Common.isVisiter()){
					doInResume();
				}
			
				
				super.onResume();
			}
			@Override
			protected void onDestroy() {
				// TODO Auto-generated method stub
				super.onDestroy();
				
			}
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
				//解决多次点击重复创建activity
		        if (System.currentTimeMillis() - lastClick <= 1000)  
		        {  
		           //Log.i("LogDemo", "点太快");
		            return;  
		        }  
		        lastClick = System.currentTimeMillis();
				switch (v.getId()) {
				case R.id.Linearlayout_headinfo:
					startActivity(new Intent(Personal_main.this,Personal_info.class));
					break;
					
				case R.id.Linearlayout_personalinfo:
					startActivity(new Intent(Personal_main.this,Personal_info.class));
					break;
					
				case R.id.Linearlayout_mygroup:
					startActivity(new Intent(Personal_main.this,MyGroupActivity.class));
					break;
					
				case R.id.Linearlayout_album:
					startActivity(new Intent(Personal_main.this,MyCommentActivity.class));
					break;
					
				case R.id.Linearlayout_offlinemap:
					startActivity(new Intent(Personal_main.this,OfflineMapActivity.class));
					break;
					
				case R.id.Linearlayout_setting:
					startActivity(new Intent(Personal_main.this,SettingsMain.class));
					break;
					
				case R.id.Linearlayout_about:
					startActivity(new Intent(Personal_main.this,AboutHappyNavi.class));
					break;
					
				default:
					break;
				}
			}











}
