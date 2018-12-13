package com.trackersurvey.happynavi;


import java.util.Locale;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.httpconnection.PostOnOffline;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class TabHost_Main extends TabActivity {
	 	private TabHost tabHost;
	    private Intent map,trail,questionnaire,home,mine;
	    //private int[] img_init=new int[]{R.drawable.foot11,R.drawable.ic_graph,R.drawable.ic_question,R.drawable.ic_i};
        //private int[] img_click=new int[]{R.drawable.foot12,R.drawable.ic_graph,R.drawable.ic_question,R.drawable.ic_i};
	    
        private int[] img_init=new int[]{R.drawable.home_dark,R.drawable.map_dark,R.drawable.history_dark,R.drawable.document_dark,R.drawable.user_dark};
        private int[] img_click=new int[]{R.drawable.home_light,R.drawable.map_light,R.drawable.history_light,R.drawable.doc_light,R.drawable.user_light};
	    
        private boolean isExit;
        private static SharedPreferences sp;
        private static String language;
        public static int l;
	    /**
	     *    <!-- android:tabStripEnabled="false"去掉选项下划线 -->貌似在2.2的系统上才有效，低于这个系统的可以参考这个文章
	     *    
	     *    http://blog.csdn.net/west8623/article/details/7481895
	     */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
//	        sp = getSharedPreferences("com.trackersurvey.happynavi_preferences", 0);
	        
	        // 中英文切换：0-中文；1-英文
	        sp = getSharedPreferences("languageSet", 0);
			language = sp.getString("language", "0");
			Log.i("lllll", language);
			l = Integer.parseInt(language);
			Resources resources = getResources();
			Configuration configure = resources.getConfiguration();
			DisplayMetrics dm = resources.getDisplayMetrics();
			if(l==0){
				configure.locale = Locale.CHINESE;
			}
			if(l==1){
				configure.locale = Locale.ENGLISH;
			}
			if(l==2) {
				configure.locale = new Locale("cs", "CZ");
			}
			resources.updateConfiguration(configure, dm);
	        setContentView(R.layout.tabhost_main);
	        
	      //activity加添至list
	        AppManager.getAppManager().addActivity(this);
	        tabHost=getTabHost();
	        
	        home=new Intent(this,Administrator.class);
	        tabHost.addTab(tabHost.newTabSpec("home").setIndicator(getResources().getString(R.string.mainhome), 
	        		getResources().getDrawable(R.drawable.home_dark)).setContent(home));
	        
	        resources.updateConfiguration(configure, dm);
	        map=new Intent(this, MainActivity.class);
	        tabHost.addTab(tabHost.newTabSpec("map").setIndicator(getResources().getString(R.string.map), 
	        		getResources().getDrawable(R.drawable.map_dark)).setContent(map));
	        
	        resources.updateConfiguration(configure, dm);
	        trail=new Intent(this, TraceListActivity.class);
	        tabHost.addTab(tabHost.newTabSpec("find").setIndicator(getResources().getString(R.string.trace), 
	        		getResources().getDrawable(R.drawable.history_dark)).setContent(trail));
	        
	        resources.updateConfiguration(configure, dm);
	        questionnaire=new Intent(this,Questionnaire.class);
	        tabHost.addTab(tabHost.newTabSpec("information").setIndicator(getResources().getString(R.string.questionnaire_tab), 
	        		getResources().getDrawable(R.drawable.document_dark)).setContent(questionnaire));
	        
	        resources.updateConfiguration(configure, dm);
	        mine=new Intent(this, Personal_main.class);
	        tabHost.addTab(tabHost.newTabSpec("mine").setIndicator(getResources().getString(R.string.mine), 
	        		getResources().getDrawable(R.drawable.user_dark)).setContent(mine));
	        
	        tabHost.setCurrentTabByTag("map");//设置当前选中标签
	        updateTab(tabHost);//初始化Tab的颜色，和字体的颜色  
	        tabHost.setOnTabChangedListener(new OnTabChangedListener()); // 选择监听器  
	        
	    }
	    class OnTabChangedListener implements OnTabChangeListener { 

	        @Override 
	        public void onTabChanged(String tabId) { 
	            tabHost.setCurrentTabByTag(tabId); 
	            //Log.i("LogDemo","tabid " + tabId); 
	            //Log.i("LogDemo","curreny after: " + tabHost.getCurrentTabTag()); 
	            updateTab(tabHost); 
	        } 
	    } 
	 
	    @SuppressLint({ "InlinedApi", "NewApi" })
		private void updateTab(final TabHost tabHost) { 
	    	TabWidget tw = tabHost.getTabWidget(); 
	        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) { 
	        	TextView tv=(TextView)tw.getChildAt(i).findViewById(android.R.id.title);     
	        	ImageView iv=(ImageView)tw.getChildAt(i).findViewById(android.R.id.icon);     
	        	iv.setPadding(0, 0, 0, 0);  
	        	iv.getLayoutParams();
	        	//iv.getLayoutParams().height =70;
	        	//iv.getLayoutParams().width =70;
				iv.getLayoutParams().height =LayoutParams.WRAP_CONTENT; //60;//通过给它的属性赋值的方法可以解决问题
	            iv.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
	        	tv.setPadding(0, 0, 0, 0);
	        	if(TabHost_Main.l == 0){
	        		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
	        	}
	        	if(TabHost_Main.l == 1){
	        		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10);
	        	}
		        //tv.setTypeface(Typeface.SERIF, 0);  
	            if (tabHost.getCurrentTab() == i) {//选中  
	            	View view=tw.getChildAt(i);
	            	view.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bottom));//选中后的背景 
	                iv.setImageResource(img_click[i]);//选中后的背景  
	                tv.setTextColor(Color.parseColor("#4277EF"));
	           
	            } else {//不选中  
	                iv.setImageResource(img_init[i]);//非选择的背景  
	                tv.setTextColor(this.getResources().getColorStateList( 
	                        android.R.color.background_dark)); 
	                View view=tw.getChildAt(i);
	            	 view.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bottom));//选中后的背景 
	            } 
	        } 
	    }
	    @Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			// TODO Auto-generated method stub
			if(event.getKeyCode()== KeyEvent.KEYCODE_BACK&&event.getAction() == KeyEvent.ACTION_DOWN)//两个条件缺一不可
			{
				exit();  
	            return true; 
			}
			else{
			return super.dispatchKeyEvent(event);
			}
		}
	   
		
	 public void exit(){  
	    //退出提醒对话框 
	        CustomDialog.Builder builder = new CustomDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.tip));
			if(Common.isRecording){
				builder.setMessage(getResources().getString(R.string.exitdlg0));
			}else{
				
				builder.setMessage(getResources().getString(R.string.exitdlg));
			
			}
			builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					
					
					dialog.dismiss();
				}
			});
			builder.setPositiveButton(getResources().getString(R.string.exit),new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					Common.sendOffline(Common.getDeviceId(getApplicationContext()),getApplicationContext());
		        	AppManager.getAppManager().AppExit(getApplicationContext());
					
					
				}
			});
			builder.create().show();
	    }  
	 Handler mHandler = new Handler() {  		  
	        @Override  
	        public void handleMessage(Message msg) {  
	            // TODO Auto-generated method stub  
	            super.handleMessage(msg);  
	            isExit = false;  
	        }  
	  
	    }; 
	
	}