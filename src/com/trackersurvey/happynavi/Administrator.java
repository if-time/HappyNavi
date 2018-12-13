package com.trackersurvey.happynavi;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.MyWebChromeClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;


public class Administrator extends Activity implements OnClickListener{
	private WebView webview;
	private MyLinearLayout l_goBack;
	private MyLinearLayout l_goForward;
	private Button goBack;
	private Button goForward;
	
	private Button refresh;
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		AppManager.getAppManager().addActivity(this);
		
		l_goBack=(MyLinearLayout)findViewById(R.id.rect_goback);
		l_goBack.setOnClickListener(this);
		l_goForward=(MyLinearLayout)findViewById(R.id.rect_goforward);
		l_goForward.setOnClickListener(this);
		goBack=(Button)findViewById(R.id.goback);
		goBack.setOnClickListener(this);
		goForward=(Button)findViewById(R.id.goforward);
		goForward.setOnClickListener(this);
		refresh=(Button)findViewById(R.id.header_refresh);
		refresh.setOnClickListener(this);
		
		webview=(WebView)findViewById(R.id.webview_admin);
		WebSettings settings=webview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		//settings.setUseWideViewPort(true);   
        //setLoadWithOverviewMode(true); 
		//settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); 
		webview.loadUrl("http://m.weibo.cn/u/5307052650");
		//webview.loadUrl("http://wifimap.sinaapp.com/index.html"); 
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setBackgroundColor(Color.WHITE);
		webview.setWebChromeClient(new MyWebChromeClient());
		//webview.addJavascriptInterface(this, "qusetionnaire");
		webview.setWebViewClient(new WebViewClient() {
		    public void onPageFinished(WebView view, String url) {//当页面加载完成后再调用js函数，否则不执行。
		       /*
		    	 webview.loadUrl("javascript:getUserID('"+Common.userId+"')");
		    	 Log.i("trailadapter", "发送用户名");
		    	*/
		super.onPageFinished(view, url);
		}
		});
		l_goBack.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					goBack.setBackgroundResource(R.drawable.arrow_back_t);
					break;
				case MotionEvent.ACTION_UP:
					goBack.setBackgroundResource(R.drawable.arrow_back_f);
					break;
				default:break;

				}
				return false;
			}
		});
		l_goForward.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					goForward.setBackgroundResource(R.drawable.arrow_forward_t);
					break;
				case MotionEvent.ACTION_UP:
					goForward.setBackgroundResource(R.drawable.arrow_forward_f);
					break;
				default:break;

				}
				return false;
			}
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.rect_goback:
			if(webview.canGoBack()){
				webview.goBack();
				}
			break;
		case R.id.rect_goforward:
			if(webview.canGoForward()){
				webview.goForward();
				}
			break;
		case R.id.header_refresh:
			webview.reload();
			break;
		}
	}
}

