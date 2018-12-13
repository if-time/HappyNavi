package com.trackersurvey.happynavi;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.MyWebChromeClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class UserGuide extends Activity {
	private LinearLayout back;
	private WebView webview;
	private Button refresh;
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.userguide);
		AppManager.getAppManager().addActivity(this);
		
		back=(LinearLayout)findViewById(R.id.userguide_back);
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});	
		init();
	}
	@SuppressLint("JavascriptInterface")
	public void init(){
		webview=(WebView)findViewById(R.id.webview);
		refresh=(Button)findViewById(R.id.userguide_refresh);
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
		webview.loadUrl("http://219.218.118.176:8089/Admin/SystemIntroduction.aspx");
		//webview.loadUrl("http://wifimap.sinaapp.com/index.html"); 
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setBackgroundColor(Color.WHITE);
		webview.setWebChromeClient(new MyWebChromeClient());
		webview.addJavascriptInterface(this, "userGuide");
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
