package com.trackersurvey.helper;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * �����ͼ���������ƺ�viewpager�������Ƴ�ͻ����дcanScroll����
 * 
 * */
public class ViewPagerCompat extends ViewPager{

	public ViewPagerCompat(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public ViewPagerCompat(Context context, AttributeSet attrs){
		super(context,attrs);
	}
	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		// TODO Auto-generated method stub
		if(v.getClass().getName().equals("com.amap.api.maps.MapView")){
			return true;
		}
		return super.canScroll(v, checkV, dx, x, y);
	}

}
