package com.trackersurvey.fragment;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trackersurvey.adapter.CloudTraceAdapter;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.happynavi.DrawPath;
import com.trackersurvey.happynavi.MainActivity;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.TrailsAnalysis;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.PullToRefreshView;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.helper.CustomDialog.Builder;
import com.trackersurvey.helper.PullToRefreshView.OnHeaderRefreshListener;
import com.trackersurvey.httpconnection.PostDeleteTrail;
import com.trackersurvey.httpconnection.PostTrailDetail;
import com.trackersurvey.httpconnection.PostTrailRough;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
/**
 * 本class于2016-7-27起停用，迁移至TraceListActivity
 * */
public class CloudTrailFragment extends Fragment implements OnClickListener,OnHeaderRefreshListener{
	private RelativeLayout layout;//长按弹出的底部菜单
	private Button cancel;
	private Button download;
	private Button delete;
	private PullToRefreshView mPullToRefreshView;
	private ListView trailList;
	private TextView selectedcount;
	private TextView tiptxt;
	private TextView refreshtip;
	private Context context;
	private CloudTraceAdapter mAdapter;
	private ArrayList<TraceData> trails=new ArrayList<TraceData>();
	private ArrayList<StepData> steps=new ArrayList<StepData>();
	private ArrayList<GpsData> trailsdetail=new ArrayList<GpsData>();
	//private List<Integer> selectid=new ArrayList<Integer>();
	private ArrayList<Long> tobedeleteNo=new ArrayList<Long>();
	//private String stepstr="--";//intent传参
	private int totalDownloadCount=0;
	private int downloadedCount=0;
	private ProgressDialog proDialog = null;
	private PullRefreshBroadcastReciver pullReciver;
	private boolean isMulChoice;
	private boolean isFirstCreated=true;
	private String URL_GETTRAIL=null;
	private  final String REFRESH_ACTION="android.intent.action.REFRESH_RECEIVER";
	public  final String PULLREFRESH_ACTION="android.intent.action.PULLREFRESH_RECEIVER";
	public  final String INITREFRESH_ACTION="android.intent.action.INITREFRESH_RECEIVER";
	private TraceDBHelper helper=null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_cloudtrail, null);       
		context=getActivity();
		layout=(RelativeLayout)view.findViewById(R.id.cloud_relative);
		cancel=(Button)view.findViewById(R.id.traillist_cloudcancel);
		download=(Button)view.findViewById(R.id.traillist_clouddownload);
		delete=(Button)view.findViewById(R.id.traillist_clouddelete);
		cancel.setOnClickListener(this);
		download.setOnClickListener(this);
		delete.setOnClickListener(this);
		mPullToRefreshView = (PullToRefreshView)view.findViewById(R.id.main_pull_refresh_view);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		trailList=(ListView)view.findViewById(R.id.listview_cloudtrail);
		trailList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				showMenu(true,false);
				
				return true;
			}
			
		});
		selectedcount=(TextView)view.findViewById(R.id.traillist_cloudtxtcount);
		tiptxt=(TextView)view.findViewById(R.id.cloudtip);
		tiptxt.setOnClickListener(this);
		refreshtip=(TextView)view.findViewById(R.id.refreshtip);
		pullReciver=new PullRefreshBroadcastReciver();
		IntentFilter pullFilter=new IntentFilter();
		pullFilter.addAction(PULLREFRESH_ACTION);
		pullFilter.addAction(INITREFRESH_ACTION);
		context.registerReceiver(pullReciver, pullFilter);
		
		helper=new TraceDBHelper(context);
		showDialog(getResources().getString(R.string.tips_dlgtle_init), getResources().getString(R.string.tips_dlgmsg_inittracelist));
		
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		URL_GETTRAIL=Common.url+"reqTraceHistory.aspx";
		init();
		return view;
	}
	void init()
	{
		
		PostTrailRough trailrough=new PostTrailRough(handler,URL_GETTRAIL,Common.getUserId(context),Common.getDeviceId(context));
		trailrough.start();
	}
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		
		init();
	}
	public class PullRefreshBroadcastReciver extends BroadcastReceiver {
		 
		public PullRefreshBroadcastReciver(){
			
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(PULLREFRESH_ACTION)){
				refreshtip.setVisibility(View.VISIBLE);
			}else{
				showDialog(getResources().getString(R.string.tips_dlgtle_init), getResources().getString(R.string.tips_dlgmsg_inittracelist));
				
				init();
			}
		}
	}
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 0://获取轨迹列表成功
				dismissDialog();
				if(msg.obj!=null){
					
					final String[] tracestr=msg.obj.toString().trim().split("!");
					
					if(tracestr.length==2){
						String trace=tracestr[0];
						String step=tracestr[1];
						int lastsize=trails.size();
						trails=(ArrayList<TraceData>) GsonHelper.parseJsonToList(trace, TraceData.class);
						steps=(ArrayList<StepData>) GsonHelper.parseJsonToList(step,StepData.class);
					    Toast.makeText(context,getResources().getString(R.string.tips_cloudtracenum)+trails.size(), Toast.LENGTH_SHORT).show();	
					    //Log.i("trailadapter", "收到轨迹条数："+trails.size()+"步行："+steps.size());
					    //for(int i=0;i<trails.size();i++){
					    	//Log.i("trailadapter",trails.get(i).getstartTime());
					    //}
					    
					    if(isFirstCreated){
					    	mAdapter=new CloudTraceAdapter(context,selectedcount,trails,steps);
							trailList.setAdapter(mAdapter);
							isFirstCreated=false;
					    	
					    }else{
					    	if(lastsize==trails.size()){
					    		mAdapter.notifyDataSetChanged();
					    	}else{
					    		Log.i("trailadapter", "云端数据变化");
					    		showMenu(false, true);
					    	}
					    }
					    if(trails.size()==0){
							tiptxt.setVisibility(View.VISIBLE);
						}
					    else{
							tiptxt.setVisibility(View.INVISIBLE);
						}
					    refreshtip.setVisibility(View.GONE);
						mPullToRefreshView.onHeaderRefreshComplete("更新于:"+new Date().toLocaleString());
					}
				}
				
				//Log.i("trailadapter", "local setadapter");
			    break;
			case 1://获取列表失败
				dismissDialog();
				Toast.makeText(context, getResources().getString(R.string.tips_postfail), Toast.LENGTH_SHORT).show();	
				mPullToRefreshView.onHeaderRefreshComplete("更新失败，请稍后再试");
				break;
			case 2://获取轨迹详细成功
				//dismissDialog();
				downloadedCount++;
				if(msg.obj!=null){
					trailsdetail=(ArrayList<GpsData>) GsonHelper.parseJsonToList(msg.obj.toString().trim(), GpsData.class);
				    //Toast.makeText(DrawPath.this, "收到轨迹条数："+trails.size(), Toast.LENGTH_LONG).show();	
					if(trailsdetail.size()>0){  
						
				    	for(int i=0;i<trailsdetail.size();i++){
				    		helper.insertintoGpswithDate(trailsdetail.get(i));
				    	}
				    	
				    	if(downloadedCount==totalDownloadCount){
							dismissDialog();
							ToastUtil.show(context,getResources().getString(R.string.tips_downfinish));
							downloadedCount=0;
							//更新本地list
							Intent intent = new Intent();  
					        intent.setAction(REFRESH_ACTION);  
					        context.sendBroadcast(intent);
						}
				    	
					}
					else{
						
						ToastUtil.show(context,getResources().getString(R.string.tips_downfail_nodata));
					}
				}
				else{
					ToastUtil.show(context,getResources().getString(R.string.tips_downfail_nodata));
				}
				
			    break;
			case 3://获取详细失败
				dismissDialog();
				
				ToastUtil.show(context,getResources().getString(R.string.tips_postfail));
				break;
			case 4://删除成功
				dismissDialog();
				init();
				
				ToastUtil.show(context,getResources().getString(R.string.tips_deletesuccess));
				break;
			case 5://删除失败
				dismissDialog();
				ToastUtil.show(context, getResources().getString(R.string.tips_deletefail));
				break;
			case 10://网络错误
				dismissDialog();
				mPullToRefreshView.onHeaderRefreshComplete("更新失败，请检查网络");
				ToastUtil.show(context, getResources().getString(R.string.tips_netdisconnect));
				if(trails.size()==0){
					tiptxt.setVisibility(View.VISIBLE);
					tiptxt.setText(getResources().getString(R.string.tips_cloudnotrace_nonet));
				}else{
					tiptxt.setVisibility(View.INVISIBLE);
				}
				break;
			case 11://网络错误
				dismissDialog();
				ToastUtil.show(context,getResources().getString(R.string.tips_netdisconnect));
				break;
			case 12://网络错误
				dismissDialog();
				ToastUtil.show(context,getResources().getString(R.string.tips_netdisconnect));
				break;
			}
		}
	};
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.cloudtip://点此刷新
				showDialog(getResources().getString(R.string.tips_dlgtle_init), getResources().getString(R.string.tips_dlgmsg_inittracelist));
				init();
				break;
			case R.id.traillist_cloudcancel:
				showMenu(false,false);
				break;
			case R.id.traillist_clouddelete://删除
				if(CloudTraceAdapter.selectid.size()>0){
					CustomDialog.Builder builder = new CustomDialog.Builder(context);
					builder.setTitle(getResources().getString(R.string.tip));
					builder.setMessage(getResources().getString(R.string.tips_deletedlgmsg_trace));
					builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_deletedlgmsg));
							for(int i=0;i<CloudTraceAdapter.selectid.size();i++){
							tobedeleteNo.add(trails.get(CloudTraceAdapter.selectid.get(i)).getTraceNo());
							//将要删除的轨迹号放入list，然后打包成json
							}
							//Gson gson=new Gson();
							//String tobedelete=gson.toJson(tobedeleteNo);
							String tobedelete=GsonHelper.toJson(tobedeleteNo);
							Log.i("trailadapter","删除:"+tobedelete);
							PostDeleteTrail deletetrail=new PostDeleteTrail(handler,URL_GETTRAIL,Common.getUserId(context),tobedelete
									,Common.getDeviceId(context));
							deletetrail.start();
							
							dialog.dismiss();
						}
					});
					builder.create().show();
					
				}
				else{
					ToastUtil.show(context, getResources().getString(R.string.tips_notraceselected));
				}
				break;
			case R.id.traillist_clouddownload://下载
				if(CloudTraceAdapter.selectid.size()>0){
					showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_downmsg));
					totalDownloadCount=CloudTraceAdapter.selectid.size();
					
					for(int i=0;i<CloudTraceAdapter.selectid.size();i++){
						
						if(helper.isTraceExists(trails.get(CloudTraceAdapter.selectid.get(i)).getTraceNo(),Common.getUserId(context))){
							//轨迹已存在
							totalDownloadCount--;//下载量减一
							if(totalDownloadCount==0){
								dismissDialog();
								ToastUtil.show(context, getResources().getString(R.string.tips_traceexist));
								break;
							}
						}
						else{
							Log.i("trailadapter", "下载的轨迹名："+trails.get(CloudTraceAdapter.selectid.get(i)).getTraceName());
							helper.insertintoTrail(trails.get(CloudTraceAdapter.selectid.get(i)));
							for(int j=0;j<steps.size();j++){
								if(trails.get(CloudTraceAdapter.selectid.get(i)).getTraceNo()==steps.get(j).gettraceNo()){
									helper.insertintoSteps(steps.get(j));
									break;
								}
							}
							PostTrailDetail traildetail=new PostTrailDetail(handler,URL_GETTRAIL,
									trails.get(CloudTraceAdapter.selectid.get(i)).getUserID(),
									trails.get(CloudTraceAdapter.selectid.get(i)).getTraceNo(),
									Common.getDeviceId(context));
							traildetail.start();
						}
						
					}
					
					showMenu(false,false);
				}
				else{
					//没有选择轨迹
					ToastUtil.show(context, getResources().getString(R.string.tips_notraceselected));
				}
				break;
		}
	}

	public void showMenu(boolean isMulChoice,boolean isNew){
		CloudTraceAdapter.isMulChoice=isMulChoice;
		CloudTraceAdapter.selectid.clear();
		if(isNew){
			CloudTraceAdapter.trails=trails;
			CloudTraceAdapter.steps=steps;
			
		}
		if(isMulChoice){
			layout.setVisibility(View.VISIBLE);
		}
		else{
			layout.setVisibility(View.GONE);
		}
		selectedcount.setText("");
		CloudTraceAdapter.visiblecheck.clear();
		CloudTraceAdapter.ischeck.clear();
		
		if(isMulChoice){
		       for(int i=0;i<trails.size();i++){
		    	   CloudTraceAdapter.ischeck.put(i, false);
		    	   CloudTraceAdapter.visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}else{
		       for(int i=0;i<trails.size();i++)
		       {
		    	   CloudTraceAdapter.ischeck.put(i, false);
		    	   CloudTraceAdapter.visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
		mAdapter.notifyDataSetChanged();

	}
	/**
	 * 显示进度条对话框
	 */
	public void showDialog(String title,String message) {
		if (proDialog == null)
			proDialog = new ProgressDialog(context);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setIndeterminate(false);
		proDialog.setCancelable(true);
		proDialog.setTitle(title);
		proDialog.setMessage(message);
		proDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(null!=pullReciver){
			context.unregisterReceiver(pullReciver);
		}
	}
}
