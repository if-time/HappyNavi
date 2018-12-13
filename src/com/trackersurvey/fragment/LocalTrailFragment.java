package com.trackersurvey.fragment;


import java.util.ArrayList;

import java.util.HashMap;

import com.trackersurvey.adapter.LocalTraceAdapter;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.helper.CustomDialog.Builder;
import com.trackersurvey.httpconnection.PostEndTrail;
import com.trackersurvey.httpconnection.PostGpsData;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 本class于2016-7-27起停用，迁移至TraceListActivity
 * */
public class LocalTrailFragment extends Fragment implements OnClickListener{
	private RelativeLayout layout;//长按弹出的底部菜单
	
	private Button cancel;
	private Button upload;
	private Button delete;
	private ListView trailList;
	private TextView selectedcount;
	private TextView tiptxt;
	private Context context;
	private TraceDBHelper helper=null;
	private LocalTraceAdapter madapter;
	private ArrayList<TraceData> trails=new ArrayList<TraceData>();
	private ArrayList<TraceData> trails_upload=new ArrayList<TraceData>();
	private ArrayList<GpsData> gpsdata=new ArrayList<GpsData>();
	private TraceData tracedata=new TraceData();
	private ArrayList<StepData> steps=new ArrayList<StepData>();
	private ArrayList<StepData> steps_upload=new ArrayList<StepData>();
	private StepData stepdata=new StepData();
	//private String stepstr="--";//intent传参
	//private List<String> array=new ArrayList<String>();
	//private List<Integer> selectid=new ArrayList<Integer>();//int是基本数据类型，Integer是对int进行了封装的一个类。当需要往ArrayList，HashMap中放东西时，像int，double这种内建类型是放不进去的，因为容器都是装object的 
	//private boolean isMulChoice;
	private ProgressDialog proDialog = null;
	private RefreshBroadcastReciver refreshReceiver;
	private boolean isFirstCreated=true;
	//private String URL_GETTRAILROUGH="http://211.87.235.69/reqTraceHistory.aspx";
	private  String URL_ENDTRAIL=null;
	private  String URL_GPSDATA=null;
	public  final String REFRESH_ACTION="android.intent.action.REFRESH_RECEIVER";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_localtrail, null);       
		context=getActivity();
		//helper=new DBHelper(context);
		
		layout=(RelativeLayout)view.findViewById(R.id.local_relative);
		
		cancel=(Button)view.findViewById(R.id.traillist_localcancel);
		cancel.setOnClickListener(this);
		upload=(Button)view.findViewById(R.id.traillist_localupload);
		upload.setOnClickListener(this);
		delete=(Button)view.findViewById(R.id.traillist_localdelete);
		delete.setOnClickListener(this);
		trailList=(ListView)view.findViewById(R.id.listview_localtrail);
		selectedcount=(TextView)view.findViewById(R.id.traillist_localtxtcount);
		tiptxt=(TextView)view.findViewById(R.id.localtip);
		refreshReceiver= new RefreshBroadcastReciver();
		IntentFilter filter=new IntentFilter();//生成一个IntentFilter对象
		filter.addAction(REFRESH_ACTION);
		context.registerReceiver(refreshReceiver, filter);
		helper=new TraceDBHelper(context);
		//init();
		if (proDialog == null)
			proDialog = new ProgressDialog(context);
		trailList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				showMenu(true,false);
				
				return true;
			}
			
		});
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		 URL_ENDTRAIL=Common.url+"reqTraceNo.aspx";
		 URL_GPSDATA=Common.url+"upLocation.aspx";
		return view;
	}
	void init()
	{
		/**
		 * 查询本地数据库，得到轨迹
		 * */
		int lastsize=trails.size();
		
		trails=helper.getallTrail(Common.getUserId(context));
		steps=helper.getallSteps(Common.getUserId(context));
		
		if(trails.size()==0){
			tiptxt.setVisibility(View.VISIBLE);
		}else{
			tiptxt.setVisibility(View.INVISIBLE);
		}
		if(isFirstCreated){
			madapter=new LocalTraceAdapter(context,selectedcount,trails,steps);
			trailList.setAdapter(madapter);
			isFirstCreated=false;
		}
		else{
			if(lastsize==trails.size()){
				LocalTraceAdapter.trails=trails;//考虑正在记录的情况
				LocalTraceAdapter.steps=steps;
				madapter.notifyDataSetChanged();
			}
			else{
				Log.i("trailadapter", "本地数据变化");
				showMenu(false, true);
			}
		}
	}     
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		init();
		
	}
	
	
	private  Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 0://上传GPS成功
				//Log.i("trailadapter", "gps上传成功");
			    break;
			case 1://上传GPS失败
				//Log.i("trailadapter", "gps上传失败:"+msg.obj);
				break;
			case 2://上传轨迹列表成功
				Common.dismissDialog(proDialog);
				//Log.i("trailadapter", "上传成功:"+msg.obj);
				ToastUtil.show(context,context.getResources().getString(R.string.tips_uploadsuccess));
				break;
			case 3://上传轨迹列表失败
				Common.dismissDialog(proDialog);
				//Log.i("trailadapter", "上传失败，error:"+msg.obj);
				ToastUtil.show(context, context.getResources().getString(R.string.tips_uploadfail));
				break;
			case 10://网络错误
				Common.dismissDialog(proDialog);
				//Log.i("trailadapter", "上传失败，请检查网络连接,error:"+msg.obj);
				
				ToastUtil.show(context, context.getResources().getString(R.string.tips_netdisconnect));
				break;
			}
		}
	};
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.traillist_localcancel:
				showMenu(false,false);
				break;
			case R.id.traillist_localupload:
				if(LocalTraceAdapter.getSelected().size()>0){
					
					Common.showDialog(proDialog,context.getResources().getString(R.string.tip),context.getResources().getString(R.string.tips_uploadmsg));
					for(int i=0;i<LocalTraceAdapter.selectid.size();i++){
						    Log.i("trailadapter", "上传的轨迹名："+trails.get(LocalTraceAdapter.selectid.get(i)).getTraceName());
						    long traceNo=trails.get(LocalTraceAdapter.selectid.get(i)).getTraceNo();
							tracedata=helper.queryfromTrailbytraceNo(traceNo,Common.getUserId(context));
							if(trails.get(LocalTraceAdapter.selectid.get(i)).getSportType()==1){
								stepdata=helper.querryformstepsbyTraceNo(traceNo,Common.getUserId(context));
								steps_upload.add(stepdata);
							}
							trails_upload.add(tracedata);
							gpsdata=helper.queryfromGpsbytraceNo(traceNo,Common.getUserId(context));
							PostGpsData gpsthread=new PostGpsData(handler,URL_GPSDATA,GsonHelper.toJson(gpsdata),Common.getDeviceId(context));
							gpsthread.start();
					}
					
					String traceinfo=GsonHelper.toJson(trails_upload);
					String stepinfo="";
					if(steps_upload.size()>0){
						stepinfo=GsonHelper.toJson(steps_upload);
					}
					//Log.i("trailadapter","上传的轨迹："+traceinfo+","+stepinfo);
					PostEndTrail endTrailThread=new PostEndTrail(handler,URL_ENDTRAIL,traceinfo,stepinfo,Common.getDeviceId(context));
					endTrailThread.start();
					trails_upload.clear();
					steps_upload.clear();
					showMenu(false, false);
				}
				else{
					//没有选择轨迹
					ToastUtil.show(context,context.getResources().getString(R.string.tips_notraceselected));
				}
				break;
			case R.id.traillist_localdelete:
				if(LocalTraceAdapter.selectid.size()>0){
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
							
							
							for(int i=0;i<LocalTraceAdapter.selectid.size();i++){
								    Log.i("trailadapter", "删除的轨迹名："+trails.get(LocalTraceAdapter.selectid.get(i)).getTraceName());
									helper.deleteTrailByTraceNo(trails.get(LocalTraceAdapter.selectid.get(i)).getTraceNo(),Common.getUserId(context));
							}
							init();
							dialog.dismiss();
						}
					});
					builder.create().show();
					
				}
				else{
					//没有选择轨迹
					ToastUtil.show(context, context.getResources().getString(R.string.tips_notraceselected));
				}
				break;
		}
	}
	public class RefreshBroadcastReciver extends BroadcastReceiver {
		 
		public RefreshBroadcastReciver(){
			
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			init();
		}
	}
	
	public void showMenu(boolean isMulChoice,boolean isNew){
		LocalTraceAdapter.isMulChoice=isMulChoice;
		LocalTraceAdapter.selectid.clear();
		if(isNew){
			LocalTraceAdapter.trails=trails;
			LocalTraceAdapter.steps=steps;
			
		}
		LocalTraceAdapter.visiblecheck.clear();
		LocalTraceAdapter.ischeck.clear();
		
		if(isMulChoice){
			layout.setVisibility(View.VISIBLE);
			for(int i=0;i<trails.size();i++){
		    	   LocalTraceAdapter.ischeck.put(i, false);
		    	   LocalTraceAdapter.visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}
		else{
			layout.setVisibility(View.GONE);
			for(int i=0;i<trails.size();i++)
		       {
		    	   LocalTraceAdapter.ischeck.put(i, false);
		    	   LocalTraceAdapter.visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
		selectedcount.setText("");
		
		
		madapter.notifyDataSetChanged();
		
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
		if(null!=refreshReceiver){
			context.unregisterReceiver(refreshReceiver);
		}
	}

}
