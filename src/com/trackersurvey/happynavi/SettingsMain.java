package com.trackersurvey.happynavi;

import java.io.File;
import java.util.Locale;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.DataCleanManager;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.ToastUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingsMain extends Activity implements OnClickListener,OnTouchListener{
	private MyLinearLayout back;
	private TextView title;
	private Button titleRightBtn;
	
	private MyLinearLayout languageSelect;
	private MyLinearLayout setParameter;
	private MyLinearLayout keepBGRun;
	private MyLinearLayout clean;
//	private MyLinearLayout logout;
	private TextView cacheSize;
	private SharedPreferences sp;
	private SharedPreferences languageSp;
	private long lastClick; //用户上次单击时间
	public static SettingsMain instance = null;
	
	private int checkedItem = 0;
	public static String languageSummaryTxt = "";
	private int l;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.setting_main);
		// 切换中英文
		//-----------------------------------------------------//
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
		//-----------------------------------------------------//
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.item_settings));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setVisibility(View.INVISIBLE);
		
		languageSelect = (MyLinearLayout) findViewById(R.id.Linearlayout_language);
		languageSelect.setOnClickListener(this);
		languageSelect.setOnTouchListener(this);
		
		setParameter=(MyLinearLayout) findViewById(R.id.Linearlayout_parameter);
		setParameter.setOnClickListener(this);
		setParameter.setOnTouchListener(this);
		
		keepBGRun=(MyLinearLayout) findViewById(R.id.Linearlayout_bgrun);
		keepBGRun.setOnClickListener(this);
		keepBGRun.setOnTouchListener(this);
		
		clean=(MyLinearLayout) findViewById(R.id.Linearlayout_clean);
		clean.setOnClickListener(this);
		clean.setOnTouchListener(this);
		
//		logout=(MyLinearLayout) findViewById(R.id.Linearlayout_logout);
//		logout.setOnClickListener(this);
//		logout.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				switch(event.getAction()){
//				case MotionEvent.ACTION_DOWN:
//					v.setBackgroundColor(getResources().getColor(R.color.red_pressed));
//					break;
//				case MotionEvent.ACTION_UP:
//					v.setBackgroundColor(getResources().getColor(R.color.red));
//					break;
//				default:
//					break;
//				}
//				return false;
//			}
//		});
		
		sp=getSharedPreferences("config", MODE_PRIVATE);
		languageSp = getSharedPreferences("languageSet", MODE_PRIVATE);
		cacheSize = (TextView) findViewById(R.id.cachesize);
		try {
			cacheSize.setText(DataCleanManager.getCacheSize(new File(Common.APPLICATION_DIR)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		checkedItem = Integer.parseInt(languageSp.getString("language", "0"));
//		String language = sp.getString("language", "0");
//		l = Integer.parseInt(language);
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
		case R.id.Linearlayout_language:
//			AlertDialog.Builder	dialog = new AlertDialog.Builder(this);
//			dialog.setTitle("语言选择");
//			dialog.setCancelable(true);
//			dialog.show();
			final String[] items = {getResources().getString(R.string.language_chinese),
					getResources().getString(R.string.language_english),
					getResources().getString(R.string.language_czech)};
			Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(getResources().getString(R.string.language_title));
			builder2.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch (which) {
					case 0:
						dialog.dismiss();
						Editor editor = languageSp.edit();
						editor.putString("language", "0");
						editor.apply();
						Personal_main.instance.finish();
						SettingsMain.instance.finish();
						Intent intent = new Intent(SettingsMain.this,TabHost_Main.class);
						startActivity(intent);
						break;
					case 1:
						dialog.dismiss();
						Editor editor2 = languageSp.edit();
						editor2.putString("language", "1");
						editor2.apply();
						Personal_main.instance.finish();
						SettingsMain.instance.finish();
						Intent intent2 = new Intent(SettingsMain.this,TabHost_Main.class);
						startActivity(intent2);
						break;
					case 2:
						dialog.dismiss();
						Editor editor3 = languageSp.edit();
						editor3.putString("language", "2");
						editor3.apply();
						Personal_main.instance.finish();
						SettingsMain.instance.finish();
						Intent intent3 = new Intent(SettingsMain.this,TabHost_Main.class);
						startActivity(intent3);
					default:
						break;
					}
					checkedItem = which;
				}
			});
			builder2.create().show(); // 创建对话框并显示
			break;
		case R.id.Linearlayout_parameter:
			startActivity(new Intent(SettingsMain.this,SetParameter.class));
			break;
		case R.id.Linearlayout_bgrun:
			if(Common.isNetConnected){
				startActivity(new Intent(SettingsMain.this,BGRunningGuide.class));
					
			}else{
				ToastUtil.show(SettingsMain.this, getResources().getString(R.string.tips_netdisconnect));
					
			}
			break;
		case R.id.Linearlayout_clean:
			CustomDialog.Builder builder = new CustomDialog.Builder(SettingsMain.this);
			builder.setTitle(getResources().getString(R.string.tip));
			builder.setMessage(getResources().getString(R.string.tips_clean));
			builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

					DataCleanManager.cleanApplicationData(getApplicationContext(), 
							Common.LOG_PATH,Common.PHOTO_PATH,
							Common.GROUPHEAD_PATH,Common.CACHEPHOTO_PATH,
							Common.DOWNLOAD_APP_PATH);
					ToastUtil.show(SettingsMain.this, getResources().getString(R.string.tips_cleanok));
					try {
						cacheSize.setText(DataCleanManager.getCacheSize(new File(Common.APPLICATION_DIR)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
//		case R.id.Linearlayout_logout:
//			String msg=getResources().getString(R.string.exitdlg1);
//			if(Common.isRecording){
//				msg=getResources().getString(R.string.exitdlg2);
//			}
//			CustomDialog.Builder builder_logout = new CustomDialog.Builder(SettingsMain.this);
//			builder_logout.setTitle(getResources().getString(R.string.exit));
//			builder_logout.setMessage(msg);
//			builder_logout.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					dialog.dismiss();
//				}
//			});
//			builder_logout.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					Common.sendOffline(Common.getDeviceId(getApplicationContext()),SettingsMain.this);
//					//Common.userId="0";
//					Common.layerid_main=0;
//					Editor editor=sp.edit();
//					
//					editor.putString("lastid", "0");
//					editor.putString("headphoto", "");
//					editor.commit();
//					
//					//AppManager.getAppManager().finishActivity(MainActivity.class);
//					Intent intent=new Intent();
//					intent.setClass(SettingsMain.this, StartPage.class);
//					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					startActivity(intent);
//					dialog.dismiss();
//				}
//			});
//			builder_logout.create().show();
			
//			break;
		}
	}
	
}
