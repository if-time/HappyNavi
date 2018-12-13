package com.trackersurvey.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.fragment.ShowPoiFragment;
import com.trackersurvey.happynavi.DrawPath;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.SetParameter;
import com.trackersurvey.happynavi.TabHost_Main;
import com.trackersurvey.happynavi.TraceListActivity;
import com.trackersurvey.happynavi.TraceListActivity.TraceListItems;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.model.MyCommentModel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public  class TraceListAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater mInflater;
	//public  ArrayList<TraceData> trails;
	public ArrayList<TraceListItems> traceItems;
	public  ArrayList<StepData> steps;
	//private HashMap<Integer, View> mView ;
	public  HashMap<Integer, Integer> visiblecheck ;//用来记录是否显示checkBox
	public  HashMap<Integer, Boolean> ischeck;
	public  List<Integer> selectid;
	public  boolean isMulChoice;
	private String stepstr="--";//intent传参
	private TextView txtcount;
	private int currentPosition;
	int[] imageId=new int[]{R.drawable.ic_walking,
			
			R.drawable.ic_cycling,R.drawable.ic_rollerblading,
			R.drawable.ic_driving,R.drawable.ic_train,R.drawable.others,
			
			};
	
	public TraceListAdapter(Context context,TextView txtcount,ArrayList<TraceListItems> traceItems,ArrayList<StepData> steps){
		this.context=context;
		this.txtcount=txtcount;
		this.traceItems = traceItems;
		this.steps=steps;
		mInflater=LayoutInflater.from(context); //(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//mView = new HashMap<Integer, View>();
		visiblecheck = new HashMap<Integer, Integer>();
		ischeck      = new HashMap<Integer, Boolean>();
		selectid = new ArrayList<Integer>();
		if(isMulChoice){
		       for(int i=0;i<traceItems.size();i++){
		              ischeck.put(i, false);
		              visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}else{
		       for(int i=0;i<traceItems.size();i++)
		       {
		            ischeck.put(i, false);
		            visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
	}
	public void setDataSource(ArrayList<TraceListItems> traceItems,ArrayList<StepData> steps){
		this.traceItems = traceItems;
		this.steps=steps;
		
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return traceItems.size();//不能使用默认返回
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return traceItems.get(position);//不能使用默认返回
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView( int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		currentPosition = position;
		ViewHolder holder = null;
		//View view = mView.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			
			int l = TabHost_Main.l;
			if(l==0){
				Log.i("language", "zzzzzzzzzzzzzzzzzzzzzzzzzzzz");
				convertView = mInflater.inflate(R.layout.traillistitems, null);
			}
			if(l==1){
				Log.i("language", "eeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				convertView = mInflater.inflate(R.layout.traillistitems_en, null);
			}
			if(l==2) {
				Log.i("language", "ccccccccccccccccccccccccccccc");
				convertView = mInflater.inflate(R.layout.traillistitems_en, null);
			}
			
			holder.tracepic=(ImageView)convertView.findViewById(R.id.traillistitem_tracepic);
			holder.isLocalimg=(ImageView)convertView.findViewById(R.id.traillistitem_img_islocal);
			holder.isCloudimg=(ImageView)convertView.findViewById(R.id.traillistitem_img_iscloud);
			holder.traceName=(TextView)convertView.findViewById(R.id.traillistitem_tracename);
			holder.startTime=(TextView)convertView.findViewById(R.id.traillistitem_starttime);
			holder.distance=(TextView)convertView.findViewById(R.id.traillistitem_distance);
			holder.holdTime=(TextView)convertView.findViewById(R.id.traillistitem_holdtime);
			holder.stepcounts=(TextView)convertView.findViewById(R.id.traillistitem_step);
			holder.lablestep=(TextView)convertView.findViewById(R.id.lable_step);
			holder.checkbox=(CheckBox)convertView.findViewById(R.id.traillistitem_check);
			holder.interestPointNum = (TextView) convertView.findViewById(R.id.interestpoint_num);
			// 为view设置标签
			convertView.setTag(holder);
			
		}else{
			
				// 取出holder
				holder = (ViewHolder) convertView.getTag();
			
		}
		try{
			holder.tracepic.setBackgroundResource(imageId[traceItems.get(position).getTraceData().getSportType()-1]);
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		if(traceItems.get(position).getIsLocal()){
			holder.isLocalimg.setVisibility(View.VISIBLE);
			
		}else{
			holder.isLocalimg.setVisibility(View.INVISIBLE);
		}
		if(traceItems.get(position).getIsCloud()){
			holder.isCloudimg.setVisibility(View.VISIBLE);
			
		}else{
			holder.isCloudimg.setVisibility(View.INVISIBLE);
		}
		holder.traceName.setText(traceItems.get(position).getTraceData().getTraceName());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date d1=new Date();
			try{
				 d1 = df.parse(traceItems.get(position).getTraceData().getStartTime());
			}catch(Exception e){
				
			}
			holder.startTime.setText(df.format(d1));
			
			double km=traceItems.get(position).getTraceData().getDistance();
			
			holder.distance.setText(Common.transformDistance(km)+context.getResources().getString(R.string.disunit));
			
			long duration=traceItems.get(position).getTraceData().getDuration();
			
			holder.holdTime.setText(Common.transformTime(duration));
			
			int poiNum = traceItems.get(position).getTraceData().getPoiCount();
			
			holder.interestPointNum.setText(""+poiNum);
			
			boolean hasSteps=false;
			if(traceItems.get(position).getTraceData().getSportType()==1){
				for(int i=0;i<steps.size();i++){
					if(traceItems.get(position).getTraceData().getTraceNo()==steps.get(i).gettraceNo()){
						stepstr=steps.get(i).getsteps()+"";
						holder.stepcounts.setText(stepstr);
						//Log.i("trailadapter", "step:"+steps.get(i).getsteps()+",traceNo:"+steps.get(i).gettraceNo());
						holder.lablestep.setVisibility(View.VISIBLE);
						holder.stepcounts.setVisibility(View.VISIBLE);
						hasSteps=true;
						break;
					}
				}
			}
			if(!hasSteps){
				holder.lablestep.setVisibility(View.GONE);
				holder.stepcounts.setVisibility(View.GONE);
			}
			holder.checkbox.setVisibility(visiblecheck.get(position));
			holder.checkbox.setChecked(ischeck.get(position));
			final CheckBox chbox=holder.checkbox;
			final int pos=position;
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 if(isMulChoice){
						   if(chbox.isChecked()){
							   chbox.setChecked(false);
							   ischeck.put(pos, false);
							   for(int i=0; i<selectid.size(); i++){
								   if(selectid.get(i) == pos){
									   selectid.remove(i);
								   }
							   }
						   }else{
							   chbox.setChecked(true);
							   ischeck.put(pos, true);
						       selectid.add(pos);
						   }
						   txtcount.setText(context.getResources().getString(R.string.totaltxt1)+selectid.size()+context.getResources().getString(R.string.totaltxt2));
					}else {
						  
						String trail=GsonHelper.toJson(traceItems.get(pos).getTraceData());
						stepstr="--";
						if(traceItems.get(pos).getTraceData().getSportType()==1){
							for(int i=0;i<steps.size();i++){
								if(traceItems.get(pos).getTraceData().getTraceNo()==steps.get(i).gettraceNo()){
									
									stepstr=GsonHelper.toJson(steps.get(i));
									break;
								}
							}
						}
						Intent intent=new Intent();
						intent.putExtra("trail", trail);
						intent.putExtra("step", stepstr);
						intent.putExtra("isonline", !traceItems.get(pos).getIsLocal());
						intent.setClass(context, DrawPath.class);
						context.startActivity(intent);
					}
				}
				
			});
			convertView.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					
					
					return false;
				}
				
			});
			//mView.put(position, view);
			
		
		return convertView;
	}
	public void resetList(boolean isMulti){
		selectid.clear();
		visiblecheck.clear();
		ischeck.clear();
		for(int i = 0;i < traceItems.size();i++){
			ischeck.put(i, false);
			if(isMulti){
				visiblecheck.put(i, CheckBox.VISIBLE);
			}else{
				visiblecheck.put(i, CheckBox.INVISIBLE);
			}
		}
		
	}
	
	public  HashMap<Integer, Integer> getVisible(){
		return visiblecheck;
	}
	public  HashMap<Integer, Boolean> getIsCheck(){
		return ischeck;
	}
	public  List<Integer> getSelected(){
		return selectid;
	}
	public void setIsMulti(boolean isMulChoice){
		this.isMulChoice = isMulChoice;
	}
	public  boolean getIsMulti(){
		return isMulChoice;
	}
	public final class ViewHolder {
		ImageView tracepic;
		ImageView isLocalimg;
		ImageView isCloudimg;
		TextView traceName;
		TextView startTime;
		TextView distance;
		TextView holdTime;
		TextView stepcounts;
		TextView lablestep;
		CheckBox checkbox;
		TextView interestPointNum;
	}
	public int getPosition(){
		return currentPosition;
	}
}