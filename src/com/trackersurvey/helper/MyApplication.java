package com.trackersurvey.helper;

import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

public class MyApplication extends Application {  
	  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        CrashReport.initCrashReport(getApplicationContext(), "900038432", false);//bugly
        
        //CrashHandler catchHandler = CrashHandler.getInstance();  //�Զ���bug�ϱ�
        //catchHandler.init(getApplicationContext());  
        
        
    }  
}  
