package com.trackersurvey.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyLinearLayout extends LinearLayout{
	//private final static String tag = "yan";
	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			//Log.i(tag, "TestLinearLayout-dispatchTouchEvent-ACTION_DOWN...");
			break;
		case MotionEvent.ACTION_UP:
			//Log.i(tag, "TestLinearLayout-dispatchTouchEvent-ACTION_UP...");
			break;
		default:break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			//Log.i(tag, "TestLinearLayout-onInterceptTouchEvent-ACTION_DOWN...");
			break;
		case MotionEvent.ACTION_UP:
			//Log.i(tag, "TestLinearLayout-onInterceptTouchEvent-ACTION_UP...");
			break;
		default:break;
		}
		//return super.onInterceptTouchEvent(ev);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			//Log.i(tag, "TestLinearLayout-onTouchEvent-ACTION_DOWN...");
			break;
		case MotionEvent.ACTION_UP:
			//Log.i(tag, "TestLinearLayout-onTouchEvent-ACTION_UP...");
			break;
		default:break;
		}
		return super.onTouchEvent(event);
	}
	

}

