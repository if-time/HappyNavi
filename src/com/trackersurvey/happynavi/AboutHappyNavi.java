package com.trackersurvey.happynavi;

import java.util.Locale;

import com.trackersurvey.entity.FileInfo;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostCheckVersion;
import com.trackersurvey.service.DownloadService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutHappyNavi extends Activity implements OnClickListener,OnTouchListener{
	private MyLinearLayout back;
	private TextView title;
	private Button titleRightBtn;
	
	private MyLinearLayout helpDoc;
	private MyLinearLayout advice;
	private MyLinearLayout checkUpdate;
	private TextView appVersion;
	
	private long lastClick; //用户上次单击时间
	private String version = null;
	private String url_checkupdate=null;
	private ProgressDialog proDialog = null;
	private Intent updateService=null;
	private int l = TabHost_Main.l;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		if(l==0){			
			setContentView(R.layout.about_layout);
		}
		if(l==1){
			setContentView(R.layout.about_layout_en);
		}
		if(l==2){
			setContentView(R.layout.about_layout_en);
		}
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
//		String[] a = new String[1];
//		String b = a[2];//模拟bug
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.item_about));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setVisibility(View.INVISIBLE);
		
		appVersion = (TextView) findViewById(R.id.about_version);
		helpDoc=(MyLinearLayout) findViewById(R.id.Linearlayout_helpdoc);
		helpDoc.setOnClickListener(this);
		helpDoc.setOnTouchListener(this);
		
		advice=(MyLinearLayout) findViewById(R.id.Linearlayout_advice);
		advice.setOnClickListener(this);
		advice.setOnTouchListener(this);
		
		checkUpdate=(MyLinearLayout) findViewById(R.id.Linearlayout_checkupdate);
		checkUpdate.setOnClickListener(this);
		checkUpdate.setOnTouchListener(this);
		
		if(Common.version == null ||Common.version.equals("")){
			version = Common.getAppVersionName(getApplicationContext());
		}else{
			version = Common.version;
		}
		appVersion.setText(getResources().getString(R.string.app_name)+" V"+version);
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		url_checkupdate=Common.url+"request.aspx";
		if (proDialog == null){
			proDialog = new ProgressDialog(this);
		}
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
		case R.id.title_back:
			finish();
			break;
		case R.id.Linearlayout_helpdoc:
			if(Common.isNetConnected){
				startActivity(new Intent(AboutHappyNavi.this,UserGuide.class));
				
			}else{
				ToastUtil.show(AboutHappyNavi.this, getResources().getString(R.string.tips_netdisconnect));
				
			}
			break;
		case R.id.Linearlayout_advice:
			startActivity(new Intent(AboutHappyNavi.this,Advice.class));
			break;
		case R.id.Linearlayout_checkupdate:
			if(version !=null && !version.equals("")){
				Common.showDialog(proDialog,getResources().getString(R.string.tip),getResources().getString(R.string.tips_updatedlgmsg));
				PostCheckVersion checkversion=new PostCheckVersion(handler_checkversion, url_checkupdate,Common.getDeviceId(getApplicationContext()),version);
				checkversion.start();
			}else{
				ToastUtil.show(AboutHappyNavi.this, getResources().getString(R.string.tips_errorversion));
			}
			break;
		}
	}
		//检查更新
		private Handler handler_checkversion = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:// 有更新
					final String[] updatestr=msg.obj.toString().trim().split("&");
					Common.dismissDialog(proDialog);
					//Log.i("LogDemo","有更新,"+ url);
					if(updatestr.length>=5){
						String version=updatestr[0];
						String time=updatestr[1];
						String url=updatestr[2];
						String detail=updatestr[3];
						String size=updatestr[4];
						Log.i("LogDemo","有更新,"+version+size+time+ url+detail);
						CustomDialog.Builder builder = new CustomDialog.Builder(AboutHappyNavi.this);
						builder.setTitle(getResources().getString(R.string.tips_updatedlg_tle));
						builder.setMessage(getResources().getString(R.string.tips_updatedlg_msg1)+"\n"
								+getResources().getString(R.string.tips_updatedlg_msg2)+version+"\n"
								+getResources().getString(R.string.tips_updatedlg_msg3)+size+"\n"
								+getResources().getString(R.string.tips_updatedlg_msg4)+time+"\n"
								+getResources().getString(R.string.tips_updatedlg_msg5)+detail+"\n"
								+getResources().getString(R.string.tips_updatedlg_msg6));
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
								dialog.dismiss();
								Common.fileInfo=new FileInfo(0, updatestr[2]
					            		
					            		,"HappyNavi"+updatestr[0]+".apk", 0, 0);//User/userDownApk.aspx
								// 通知Service开始下载
								updateService = new Intent(AboutHappyNavi.this, DownloadService.class);
								updateService.setAction(DownloadService.ACTION_START);
								updateService.putExtra("fileInfo", Common.fileInfo);
								startService(updateService);
								Common.isUpdationg=true;
								ToastUtil.show(getApplicationContext(), getResources().getString(R.string.tips_gotodownnewapk));
								
							}
						});
						builder.create().show();
					}
					else{
						ToastUtil.show(AboutHappyNavi.this,getResources().getString(R.string.tips_postfail));
					}
					break;
				case 1://已是最新.
					Common.dismissDialog(proDialog);
					ToastUtil.show(AboutHappyNavi.this,getResources().getString(R.string.tips_update_alreadynew));
					//Log.i("LogDemo", "已是最新,"+msg.obj.toString().trim());
					break;
				case 2://失败
					Common.dismissDialog(proDialog);
					//Log.i("LogDemo","失败,"+ msg.obj.toString().trim());
					ToastUtil.show(AboutHappyNavi.this, getResources().getString(R.string.tips_postfail));
					break;
				case 10://异常
					Common.dismissDialog(proDialog);
					//Log.i("LogDemo", "异常,"+msg.obj.toString().trim());
					ToastUtil.show(AboutHappyNavi.this,getResources().getString(R.string.tips_netdisconnect));
					break;
				}
			}
		};
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			if(updateService!=null){
				stopService(updateService);
			}
		}
}
