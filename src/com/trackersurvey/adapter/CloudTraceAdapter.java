package com.trackersurvey.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.happynavi.DrawPath;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ToastUtil;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 本class于2016-7-27起停用，迁移至TraceListAdapter
 * */
public  class CloudTraceAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater mInflater;
	public static ArrayList<TraceData> trails;
	public static ArrayList<StepData> steps;
	//private HashMap<Integer, View> mView ;
	public static HashMap<Integer, Integer> visiblecheck ;//用来记录是否显示checkBox
	public static HashMap<Integer, Boolean> ischeck;
	public static List<Integer> selectid;
	public static boolean isMulChoice;
	private String stepstr="--";//intent传参
	private TextView txtcount;
	int[] imageId=new int[]{R.drawable.ic_walking,
			
			R.drawable.ic_cycling,R.drawable.ic_rollerblading,
			R.drawable.ic_driving,R.drawable.ic_train,R.drawable.others,
			
			};
	
	public CloudTraceAdapter(Context context,TextView txtcount,ArrayList<TraceData> trails,ArrayList<StepData> steps){
		this.context=context;
		this.txtcount=txtcount;
		CloudTraceAdapter.trails=trails;
		CloudTraceAdapter.steps=steps;
		mInflater=LayoutInflater.from(context); //(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//mView = new HashMap<Integer, View>();
		visiblecheck = new HashMap<Integer, Integer>();
		ischeck      = new HashMap<Integer, Boolean>();
		selectid = new ArrayList<Integer>();
		if(isMulChoice){
		       for(int i=0;i<trails.size();i++){
		              ischeck.put(i, false);
		              visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}else{
		       for(int i=0;i<trails.size();i++)
		       {
		            ischeck.put(i, false);
		            visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return trails.size();//不能使用默认返回
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return trails.get(position);//不能使用默认返回
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView( int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder = null;
		//View view = mView.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.traillistitems, null);
			holder.tracepic=(ImageView)convertView.findViewById(R.id.traillistitem_tracepic);
			holder.traceName=(TextView)convertView.findViewById(R.id.traillistitem_tracename);
			holder.startTime=(TextView)convertView.findViewById(R.id.traillistitem_starttime);
			holder.distance=(TextView)convertView.findViewById(R.id.traillistitem_distance);
			holder.holdTime=(TextView)convertView.findViewById(R.id.traillistitem_holdtime);
			holder.stepcounts=(TextView)convertView.findViewById(R.id.traillistitem_step);
			holder.lablestep=(TextView)convertView.findViewById(R.id.lable_step);
			holder.checkbox=(CheckBox)convertView.findViewById(R.id.traillistitem_check);
			// 为view设置标签
			convertView.setTag(holder);
			
		}else{
			
				// 取出holder
				holder = (ViewHolder) convertView.getTag();
			
		}
		try{
			holder.tracepic.setBackgroundResource(imageId[trails.get(position).getSportType()-1]);
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		holder.traceName.setText(trails.get(position).getTraceName());
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date d1=new Date();
			try{
				 d1 = df.parse(trails.get(position).getStartTime());
			}catch(Exception e){
				
			}
			holder.startTime.setText(df.format(d1));
			
			double km=trails.get(position).getDistance();
			
			holder.distance.setText(Common.transformDistance(km)+context.getResources().getString(R.string.disunit));
			
			long duration=trails.get(position).getDuration();
			
			holder.holdTime.setText(Common.transformTime(duration));
			boolean hasSteps=false;
			if(trails.get(position).getSportType()==1){
				for(int i=0;i<steps.size();i++){
					if(trails.get(position).getTraceNo()==steps.get(i).gettraceNo()){
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
						   //Toast.makeText(context, "点击了"+array.get(position), Toast.LENGTH_LONG).show();
						//Gson gson=new Gson();
						if(!Common.isNetConnected){
							ToastUtil.show(context, context.getResources().getString(R.string.tips_netdisconnect));
							return;
						}
						String trail=GsonHelper.toJson(trails.get(pos));
						stepstr="--";
						if(trails.get(pos).getSportType()==1){
							for(int i=0;i<steps.size();i++){
								if(trails.get(pos).getTraceNo()==steps.get(i).gettraceNo()){
									
									stepstr=GsonHelper.toJson(steps.get(i));
									break;
								}
							}
						}
						Intent intent=new Intent();
						intent.putExtra("trail", trail);
						intent.putExtra("step", stepstr);
						intent.putExtra("isonline", true);
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
	public static HashMap<Integer, Integer> getVisible(){
		return visiblecheck;
	}
	public static HashMap<Integer, Boolean> getIsCheck(){
		return ischeck;
	}
	public static List<Integer> getSelected(){
		return selectid;
	}
	public static boolean getIsMulti(){
		return isMulChoice;
	}
	public final class ViewHolder {
		ImageView tracepic;
		TextView traceName;
		TextView startTime;
		TextView distance;
		TextView holdTime;
		TextView stepcounts;
		TextView lablestep;
		CheckBox checkbox;
	}
}