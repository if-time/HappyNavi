package com.trackersurvey.happynavi;


import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.MyWebChromeClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class Questionnaire extends Activity {
	private WebView webview;
	private Button refresh;
	private int l = TabHost_Main.l;
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.questionnaire);
		AppManager.getAppManager().addActivity(this);
		if(Common.isVisiter()){
			Common.DialogForVisiter(this);
			
		}
		else{
			init();
		}
		
	}
	@SuppressLint("JavascriptInterface")
	public void init(){
		webview=(WebView)findViewById(R.id.webview);
		refresh=(Button)findViewById(R.id.questionnaire_refresh);
		refresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				webview.reload();
			}
		});
		
		WebSettings settings=webview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		//settings.setUseWideViewPort(true);   
        //setLoadWithOverviewMode(true); 
		//settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); 
		if(l==0){			
			webview.loadUrl("http://219.218.118.176:8089/User/TotalNaire.aspx?uid="+Common.getUserId(this));
		}
		if(l==1){
			webview.loadUrl("http://219.218.118.176:8089/User/TotalNaireEn.aspx?uid="+Common.getUserId(this));
		}
		//webview.loadUrl("http://211.87.235.102/User/TotalNaire.aspx?uid="+Common.getUserId(this)); 
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setBackgroundColor(Color.WHITE);
		webview.setWebChromeClient(new MyWebChromeClient());
		webview.addJavascriptInterface(this, "qusetionnaire");
		webview.setWebViewClient(new WebViewClient() {
		    public void onPageFinished(WebView view, String url) {//当页面加载完成后再调用js函数，否则不执行。
		       /*
		    	 webview.loadUrl("javascript:getUserID('"+Common.userId+"')");
		    	 Log.i("trailadapter", "发送用户名");
		    	*/
		super.onPageFinished(view, url);
		}
		});
	}
}
