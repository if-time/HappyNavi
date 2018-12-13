package com.trackersurvey.happynavi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

public class StartPage extends Activity {
	private ImageView iv_start;
	protected boolean _active = true;
	protected int _splashTime = 5000;
	private ImageView iv_version;
	private TextView tv_version;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startpage);
		iv_start = (ImageView) findViewById(R.id.iv_start);
		//iv_version = (ImageView) findViewById(R.id.iv_version);
		tv_version = (TextView) findViewById(R.id.tv_version);
//		Handler x = new Handler();
//		x.postDelayed(new splashhandler(), 2000);
		 
		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(2000);
		iv_start.startAnimation(animation);
		//iv_version.setAnimation(animation);
		tv_version.setAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				iv_start.setBackgroundResource(R.drawable.startpage);
				//iv_version.setBackgroundResource(R.drawable.version);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(StartPage.this, LoginActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
				finish();
			}
		});
	}
//	class splashhandler implements Runnable{
//        public void run() {
//            startActivity(new Intent(getApplication(),LoginActivity.class));
//            StartPage.this.finish();
//        }
//	}
}
