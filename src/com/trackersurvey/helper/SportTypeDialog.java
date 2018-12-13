package com.trackersurvey.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trackersurvey.happynavi.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SportTypeDialog extends Dialog {
	private EditText traceName;
	//private TextView justtrail;
	//private CheckBox justtrailchecked;
	private Button start;
	private Button cancel;
	private GridView gridview;
	private Context context;
	private Activity activity;
	private View itemview=null;//gridview����item��view
	private String tracename="";
	private int postion=-1;//��¼�����ѡ��λ��
	//private int isopen;//0��ʾ������1��ʾ������
	public SportTypeDialog(Context context) {
		
		this(context, android.R.style.Theme_Dialog);
		this.context=context;
	}

	public SportTypeDialog(Context context, int theme) {
		super(context, theme);
		this.context=context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sporttypedialog);
		
		this.activity=(Activity)context;
		traceName=(EditText)findViewById(R.id.sportdialog_sportname);
		String str=Common.currentDate()+context.getResources().getString(R.string.trace);
		traceName.setText(str);
		traceName.setSelection(str.length());
		start=(Button)findViewById(R.id.sportdialog_starttrail);
		
		cancel=(Button)findViewById(R.id.sportdialog_canceltrail);
		gridview=(GridView)findViewById(R.id.grid_sporttype);
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));//��������ֵı���ɫ��Ϊ͸��
		int[] imageId=new int[]{R.drawable.ic_walking,
				
				R.drawable.ic_cycling,R.drawable.ic_rollerblading,
				R.drawable.ic_driving,R.drawable.ic_train,R.drawable.others,
				
				};
		String[] title=context.getResources().getStringArray(R.array.sporttype);
		List<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
		for(int i=0;i<imageId.length;i++){
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("image", imageId[i]);
			map.put("title", title[i]);
			listItems.add(map);
			
		}
		SimpleAdapter adapter=new SimpleAdapter(context,listItems,R.layout.sportdialogitems,new String[]{"title","image"},
				new int[]{R.id.sporttype_title,R.id.sporttype_img});
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// TODO Auto-generated method stub
				postion=pos+1;
				for(int i=0;i<gridview.getChildCount();i++){
					 itemview=gridview.getChildAt(i);
					itemview.setBackgroundColor(Color.parseColor("#ffffff"));
				}
				view.setBackgroundColor(Color.parseColor("#99cc33"));
				itemview=view;
				
				
			}
		});
	
		
		start.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(postion==-1){
					ToastUtil.show(context, context.getResources().getString(R.string.tips_choosetype));
					return;
				}
				if(traceName.length()==0){
					tracename=context.getResources().getString(R.string.trace)+Common.currentDay();
				}
				else{
					tracename=traceName.getText().toString().trim();
				}
				if(postion==1){//����
					if(!Common.checkGPS(context)){
						setGPS(1);
					}
					else{
						SportTypeDialog.this.dismiss();
					}
				}
				else{
					if(!Common.checkGPS(context)){
						setGPS(0);
					}
					SportTypeDialog.this.dismiss();
				}
				
			}
			
		});
		cancel.setOnClickListener(new android.view.View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				postion=-1;
				SportTypeDialog.this.dismiss();
			}
			
		});
	}
	public String gettraceName(){
		return tracename;
	}
	public int getposition(){
		return postion;
	}
	
	
	private void setGPS(int type) {
		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.tips_gpsdlgtle));
		if(type==0){
			builder.setMessage(context.getResources().getString(R.string.tips_gpsdlgmsg10)
	        		+"\n"+context.getResources().getString(R.string.tips_gpsdlgmsg2));
		}
		else{
			builder.setMessage(context.getResources().getString(R.string.tips_gpsdlgmsg11)
	        		+"\n"+context.getResources().getString(R.string.tips_gpsdlgmsg2));
		}
		builder.setPositiveButton(context.getResources().getString(R.string.confirm),
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        // ת���ֻ����ý��棬�û�����GPS
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(intent, 0); // ������ɺ󷵻ص�ԭ���Ľ���
                        arg0.dismiss();
                    	//ǿ�ƴ�GPS ����һ��
                    	/*Intent GPSIntent = new Intent();  
                        GPSIntent.setClassName("com.android.settings",  
                                "com.android.settings.widget.SettingsAppWidgetProvider");  
                        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");  
                        GPSIntent.setData(Uri.parse("custom:3"));  
                        try {
                        	Log.i("phonelog", "gps open...");
                            PendingIntent.getBroadcast(MainActivity.this, 0, GPSIntent, 0).send();  
                        } catch (CanceledException e) { 
                        	Log.i("phonelog", "gps open fail");
                            e.printStackTrace();  
                            
                        }  */
                    	//ǿ�ƴ�GPS ��������
                    	//Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, true);
                    	//ǿ�ƴ�GPS ��������
                    	//turnGPSOn();
                    }
                    
                });
		builder.setNegativeButton(context.getResources().getString(R.string.cancl), new android.content.DialogInterface.OnClickListener() {
             
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                
            }
        } );
        builder.create().show();
        
    }
	public void turnGPSOn(){
		//Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		//intent.putExtra("enabled", true);
		//this.sendBroadcast(intent);
		String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(!provider.contains("gps")){
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			context.sendBroadcast(poke);
		}
	
	}
}
