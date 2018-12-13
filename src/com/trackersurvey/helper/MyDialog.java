package com.trackersurvey.helper;

import com.trackersurvey.happynavi.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

public class MyDialog extends Dialog{
	
	private ImageView imgBack;
	private Context context;
	public MyDialog(Context context, int themeResId) {
		super(context, themeResId);
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.sharetowx);
	}

}
