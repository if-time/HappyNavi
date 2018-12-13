package com.trackersurvey.happynavi;

import java.util.Locale;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
public class BGRunningGuide extends Activity {
	
	private WebView webview;
	
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bgrunning_guide);
		AppManager.getAppManager().addActivity(this);
		
		
		init();
	}
	public void init(){
		webview=(WebView)findViewById(R.id.bgrwebview);
		String brand = Common.getDeviceBrand().toLowerCase(Locale.getDefault());//将品牌转为小写字母形式
    	String[] brands = getResources().getStringArray(R.array.phonebrand);
    	String url_brand = null;
    	if(brand != null){
	    	for(int i = 0;i<brands.length;i++){
	    		if(brand.equals(brands[i])){
	    			url_brand = brand;
	    			break;
	    		}
	    	}
    	}
		WebSettings settings=webview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		//settings.setUseWideViewPort(true);   
        //setLoadWithOverviewMode(true); 
		//settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); 
		if(url_brand != null){
			if(url_brand.equals("honor"))
				url_brand = "huawei";
			webview.loadUrl("http://219.218.118.176:8089/Helps/"+url_brand+".html");
		}else{
			webview.loadUrl("http://219.218.118.176:8089/Helps/Main.html");
		}
		//webview.loadUrl("http://wifimap.sinaapp.com/index.html"); 
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setBackgroundColor(Color.WHITE);
		//webview.setWebChromeClient(new MyWebChromeClient());
		webview.addJavascriptInterface(this, "help");
		webview.setWebViewClient(new WebViewClient() {
			
		    public void onPageFinished(WebView view, String url) {//当页面加载完成后再调用js函数，否则不执行。
		       
		    	super.onPageFinished(view, url);
		}
		});
	}
	@JavascriptInterface
	public void back(){
		Log.i("phonelog", "bgrunning back");
		finish();
	}
	
}
