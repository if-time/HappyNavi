package com.trackersurvey.helper;

import com.trackersurvey.happynavi.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MapLayerDialog extends Dialog implements OnCheckedChangeListener {
	private Context context;
	private RadioGroup layerGroup;
	private RadioButton normal;
	private RadioButton satellite;
	private RadioButton bdnormap;
	private RadioButton bdsatellitemap;
	private Button yes;
	private Button cancel;
	private int where=0;//0表示从主界面切换，1表示drawpath里切换
	private final int normalmap=0;
	private final int satellitemap=1;
	private final int bdnormalmap=2;
	private final int bdsatellite=3;
	private int checkedmap=-1;
	public MapLayerDialog(Context context) {
		
		this(context, android.R.style.Theme_Dialog,0);
		this.context=context;
	}

	public MapLayerDialog(Context context, int theme,int where) {
		super(context, theme);
		this.context=context;
		this.where=where;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maplayerdialog);
		
		layerGroup=(RadioGroup)findViewById(R.id.maplayer_tab);
		normal=(RadioButton)findViewById(R.id.normalmapradio);
		
		satellite=(RadioButton)findViewById(R.id.satelliteradio);
		bdnormap=(RadioButton)findViewById(R.id.bdnormalmap);
		bdsatellitemap=(RadioButton)findViewById(R.id.bdsatellitemap);
		yes=(Button)findViewById(R.id.layer_yes);
		cancel=(Button)findViewById(R.id.layer_cancel);
		normal.setOnCheckedChangeListener(this);
		satellite.setOnCheckedChangeListener(this);
		bdnormap.setOnCheckedChangeListener(this);
		bdsatellitemap.setOnCheckedChangeListener(this);
		
		yes.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(where==0){
				Common.layerid_main=checkedmap;
				}
				else{
					Common.layerid_path=checkedmap;
				}
				MapLayerDialog.this.dismiss();
			}
			
		});
		cancel.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkedmap=-1;
				MapLayerDialog.this.dismiss();
			}
			
		});
		if(where==0){
			setcheck(Common.layerid_main);
		}
		else{
			setcheck(Common.layerid_path);
		}
		
	}
	public void setcheck(int id){
		switch(id){
		case 0:
			normal.setChecked(true);
			break;
		case 1:
			satellite.setChecked(true);
			break;
		case 2:
			bdnormap.setChecked(true);
			break;
		case 3:
			bdsatellitemap.setChecked(true);
			break;
		}
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(isChecked){
 			switch (buttonView.getId()) {
 			case R.id.normalmapradio:
 				checkedmap=normalmap;
 				
 				break;
 			case R.id.satelliteradio:
 				checkedmap=satellitemap;
 				
 				break;
 			case R.id.bdnormalmap:
 				checkedmap=bdnormalmap;
 				
 				break;
 			case R.id.bdsatellitemap:
 				checkedmap=bdsatellite;
 				
 				break;
 			}
		}
	}
	
	public int getMapType(){
		return checkedmap;
	}
}
