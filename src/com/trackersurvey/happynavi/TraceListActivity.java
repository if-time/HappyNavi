package com.trackersurvey.happynavi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.trackersurvey.adapter.TraceListAdapter;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.PullToRefreshView;
import com.trackersurvey.helper.PullToRefreshView.OnHeaderRefreshListener;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostDeleteTrail;
import com.trackersurvey.httpconnection.PostEndTrail;
import com.trackersurvey.httpconnection.PostGpsData;
import com.trackersurvey.httpconnection.PostTrailDetail;
import com.trackersurvey.httpconnection.PostTrailRough;
import com.trackersurvey.model.MyCommentModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class TraceListActivity extends Activity implements OnClickListener,OnHeaderRefreshListener{
	
	private ListView traceList;
	private RelativeLayout menuLayout;//���������ĵײ��˵�
	private Button cancel;//ȡ��
	private Button downupload;//����
	private Button delete;//ɾ��
	private TextView tv_count;//��ʾѡ�˼����켣
	private TextView tv_tip;//û�й켣ʱ��ʾ�û�
	private TraceListAdapter adapter;//list������
	private PullToRefreshView mPullToRefreshView;//����ˢ�¿ؼ�
	private TraceDBHelper helper=null;
	private RefreshBroadcastReciver refreshReceiver;
	
	//TraceListItems listview������Դ
	private ArrayList<TraceListItems> traceItems=new ArrayList<TraceListItems>();
	private ArrayList<TraceData> trace_Local=new ArrayList<TraceData>();
	private ArrayList<TraceData> trace_Cloud=new ArrayList<TraceData>();
	private ArrayList<StepData> steps_Local=new ArrayList<StepData>();
	private ArrayList<StepData> steps_Cloud=new ArrayList<StepData>();
	private ArrayList<StepData> steps_Both=new ArrayList<StepData>();
	private ProgressDialog proDialog = null;
	
	private int downloadCount = 0;//���ڱ�ʶ�Ƿ���ѡ�켣���������
	private String userID,deviceID;
	private boolean isFirstCreateAdatper = true;

	private String URL_ENDTRAIL=null;
	private String URL_GPSDATA=null;
	private String URL_GETTRAIL=null;
	private  final String REFRESH_ACTION="android.intent.action.REFRESH_RECEIVER";
	private int interestNum = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracelistlayout);
		AppManager.getAppManager().addActivity(this);
		
		// �����л���Ӣ��
		Resources resources = getResources();
		Configuration configure = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TabHost_Main.l==0){
			configure.locale = Locale.CHINESE;
		}
		if(TabHost_Main.l==1){
			configure.locale = Locale.ENGLISH;
		}
		if(TabHost_Main.l==2) {
			configure.locale = new Locale("cs", "CZ");
		}
		resources.updateConfiguration(configure, dm);
		
		traceList=(ListView)findViewById(R.id.listview_trace);
		mPullToRefreshView = (PullToRefreshView)findViewById(R.id.pull_refresh_view_tracelist);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		menuLayout = (RelativeLayout)findViewById(R.id.relative_tracemenu);
		cancel = (Button) findViewById(R.id.tracelist_cancel);
		downupload = (Button) findViewById(R.id.tracelist_downupload);
		delete = (Button) findViewById(R.id.tracelist_delete);
		tv_count = (TextView) findViewById(R.id.tracelist_txtcount);
		tv_tip = (TextView) findViewById(R.id.tracelist_tip);
		cancel.setOnClickListener(this);
		downupload.setOnClickListener(this);
		delete.setOnClickListener(this);
		tv_tip.setOnClickListener(this);
		helper=new TraceDBHelper(this);
		traceList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				showMenu(true,false);
				
				return true;
			}
		});
		refreshReceiver= new RefreshBroadcastReciver();
		IntentFilter filter=new IntentFilter();//����һ��IntentFilter����
		filter.addAction(REFRESH_ACTION);
		registerReceiver(refreshReceiver, filter);
		if (proDialog == null)
			proDialog = new ProgressDialog(this);
		
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		 URL_ENDTRAIL=Common.url+"reqTraceNo.aspx";
		 URL_GPSDATA=Common.url+"upLocation.aspx";
		 URL_GETTRAIL=Common.url+"reqTraceHistory.aspx";
		 userID = Common.getUserId(this);
		 deviceID = Common.getDeviceId(this);
		 init();
	}
	private void init(){
		initLocalTrace();
		 if(Common.isNetConnected){//������״̬�²������ƶ�
			 showDialog(getResources().getString(R.string.tips_dlgtle_init),
						getResources().getString(R.string.tips_dlgmsg_inittracelist));
				
			 initCloudTrace();
		 }else{
			 initBothTrace();
			 mPullToRefreshView.onHeaderRefreshComplete("����ʧ�ܣ���������");
		 }
	}
	private void initLocalTrace(){
		trace_Local=helper.getallTrail(userID);
		if(trace_Cloud!=null){			
			for(int i = 0;i<trace_Local.size();i++){
				for(int j = 0;j<trace_Cloud.size();j++){
					if(trace_Local.get(i).getTraceNo()==trace_Cloud.get(j).getTraceNo()){
						helper.updatetrail(trace_Cloud.get(j), 
								trace_Cloud.get(j).getTraceNo(), trace_Cloud.get(j).getUserID());
						trace_Local=helper.getallTrail(userID);
					}
				}
			}
		}
		steps_Local=helper.getallSteps(userID);		
	}
	private void refreshLocalTrace(){
		trace_Local=helper.getallTrail(userID);
		steps_Local=helper.getallSteps(userID);
	}
	private void initCloudTrace(){
		PostTrailRough trailrough=new PostTrailRough(handler1,URL_GETTRAIL,userID,deviceID);
		trailrough.start();
	}
	/**
	 * �ϲ��ƶ˺ͱ��ع켣��������ĳ���켣�ڱ���or�ƶ�or���߶���
	 * */
	private void initBothTrace(){
		int lastItemsSize = traceItems.size();
		traceItems.clear();
		ArrayList<Long> dealedTraceNo=new ArrayList<Long>();//�����Ѵ�����Ĺ켣�ţ���ʵ���Ǳ��صĹ켣�ţ���
		for(int i = 0;i < trace_Local.size();i++){
			long traceNo = trace_Local.get(i).getTraceNo();
			dealedTraceNo.add(traceNo);
			boolean isCloud = false;
			for(int j = 0;j<trace_Cloud.size();j++){
				if(traceNo == trace_Cloud.get(j).getTraceNo()){//�ƶ˱��ض���
					isCloud = true;
					break;
				}
			}
			TraceListItems item = new TraceListItems();
			item.setTraceData(trace_Local.get(i));
			item.setIsLocal(true);
			item.setIsCloud(isCloud);
			traceItems.add(item);
		}
		for(int j = 0;j<trace_Cloud.size();j++){
			long traceNo = trace_Cloud.get(j).getTraceNo();
			boolean isLocal = false;
			for(int k = 0;k<dealedTraceNo.size();k++){
				if(traceNo == dealedTraceNo.get(k).longValue()){//�ڴ����ع켣ʱ�Ѵ�����ù켣������
					isLocal = true;
					break;
				}
			}
			if(!isLocal){//�ƶ��б���û��
				TraceListItems item = new TraceListItems();
				item.setTraceData(trace_Cloud.get(j));
				item.setIsLocal(false);
				item.setIsCloud(true);
				traceItems.add(item);
			}
		}
		if(traceItems.size() == 0){
			//û�й켣������ԭ��1���û��Ĺ켣�����ƶˣ�û��������Ȼ�ò�����2�����û���û�м�¼���켣��
			//����������������ֱ���ʾ��ͬ����ʾ�����֣����û��ο�
			if(!Common.isNetConnected){//ԭ��1
				tv_tip.setText(getResources().getString(R.string.tips_cloudnotrace_nonet));
			}else{//ԭ��2
				tv_tip.setText(getResources().getString(R.string.tips_localnotrace));
			}
			tv_tip.setVisibility(View.VISIBLE);
			return;
		}else{
			tv_tip.setVisibility(View.INVISIBLE);
		}
		//������Ϣ����ͬ��
		steps_Both.clear();
		ArrayList<Long> dealedStepsNo=new ArrayList<Long>();
		for(int i = 0;i<steps_Local.size();i++){
			steps_Both.add(steps_Local.get(i));
			dealedStepsNo.add(steps_Local.get(i).gettraceNo());
		}
		for(int j = 0;j<steps_Cloud.size();j++){
			long traceNo = steps_Cloud.get(j).gettraceNo();
			boolean isOnlyCloud = true;
			for(int k = 0;k<dealedStepsNo.size();k++){
				if(traceNo == dealedStepsNo.get(k).longValue()){
					isOnlyCloud = false;
					break;
				}
			}
			if(isOnlyCloud){
				steps_Both.add(steps_Cloud.get(j));
			}
		}
		Collections.sort(traceItems, new SortByTraceNo());//��ʱ������
		if(isFirstCreateAdatper){
			initAdapter();//�״μ���ʱִ��
			isFirstCreateAdatper = false;
		}else{
			if(lastItemsSize == traceItems.size()){//�����������䣬�������ڼ�¼ʱ�켣���ݵ�ˢ��
				adapter.setDataSource(traceItems, steps_Both);
				adapter.notifyDataSetChanged();
			}else{//���������仯���رղ˵�
				showMenu(false, true);
			}
		}
	}
	private void initAdapter(){
		adapter = new TraceListAdapter(this, tv_count, traceItems, steps_Both);
		traceList.setAdapter(adapter);
	}
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		
		init();
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
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!isFirstCreateAdatper && Common.isRecording){//ˢ�±��ع켣,�������ڼ�¼�Ĺ켣���ݵ�ˢ��
			initLocalTrace();
			initBothTrace();
		}
		initCloudTrace();//���������˸��º�����й켣�б�������Ϣ
		refreshLocalTrace();//���±���GPS_DB���ݿ����TRAIL���е���Ϣ
		initBothTrace();//���¹켣�б���Ϣ
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(null!=refreshReceiver){
			unregisterReceiver(refreshReceiver);
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.tracelist_cancel:
			showMenu(false,false);
			break;
		case R.id.tracelist_tip:
			init();
			break;
		case R.id.tracelist_delete://ɾ��
			final List<Integer> selectid = adapter.getSelected();
			if(selectid.size() > 0){
				CustomDialog.Builder builder = new CustomDialog.Builder(TraceListActivity.this);
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
						ArrayList<Long> tobedeleteNo=new ArrayList<Long>();//Ҫɾ�����ƶ˹켣�����һ������
						
						for(int i=0;i<selectid.size();i++){
							TraceListItems item = traceItems.get(selectid.get(i));
							    Log.i("trailadapter", "ɾ���Ĺ켣����"+item.getTraceData().getTraceName());
							if(item.getIsLocal()){//ɾ����
								helper.deleteTrailByTraceNo(item.getTraceData().getTraceNo(),userID);
							}
							if(item.getIsCloud()){//ɾ�ƶ�
								tobedeleteNo.add(item.getTraceData().getTraceNo());
							}
						}
						if(tobedeleteNo.size() > 0){
							showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_deletedlgmsg));
							
							String tobedelete=GsonHelper.toJson(tobedeleteNo);
							Log.i("trailadapter","�ƶ�ɾ��:"+tobedelete);
							PostDeleteTrail deletetrail=new PostDeleteTrail(handler1,
									URL_GETTRAIL,
									userID,
									tobedelete,
									deviceID);
							deletetrail.start();
						}else{
							init();
						}
						dialog.dismiss();
					}
				});
				builder.create().show();
			}else{
				ToastUtil.show(TraceListActivity.this, getResources().getString(R.string.tips_notraceselected));
			}
			break;
		case R.id.tracelist_downupload://����  ����->�ƶ�  ; �ƶ�->����
			final List<Integer> choosedid = adapter.getSelected();
			if(choosedid.size() > 0){
				showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_downupmsg));
				
				ArrayList<TraceData> trace_Upload=new ArrayList<TraceData>();
				ArrayList<StepData> steps_Upload=new ArrayList<StepData>();
				int bothSize = 0;//��ѡ�Ĺ켣���ƶ˺ͱ��ض��еĸ���
				for(int i=0;i<choosedid.size();i++){
					TraceListItems item = traceItems.get(choosedid.get(i));
					if(item.getIsLocal() && !item.getIsCloud()){
						//�������ƶ�û�У����ݵ��ƶ�
						TraceData traceData = new TraceData();
						StepData stepData=new StepData();
						ArrayList<GpsData> gpsData=new ArrayList<GpsData>();
						
						long traceNo = item.getTraceData().getTraceNo();
						traceData = item.getTraceData();
						if(traceData.getSportType() == 1){
							stepData=helper.querryformstepsbyTraceNo(traceNo,userID);
							steps_Upload.add(stepData);
						}
						trace_Upload.add(traceData);
						gpsData=helper.queryfromGpsbytraceNo(traceNo,userID);
						PostGpsData gpsthread=new PostGpsData(handler2,
								URL_GPSDATA,
								GsonHelper.toJson(gpsData),
								deviceID);
						gpsthread.start();
					}else if(!item.getIsLocal() && item.getIsCloud()){
						//����û�У��ƶ��У����ص�����
						downloadCount++;
						helper.insertintoTrail(item.getTraceData());
						long traceNo = item.getTraceData().getTraceNo();
						if(item.getTraceData().getSportType() == 1){
							for(int j = 0;j<steps_Cloud.size();j++){
								if(traceNo == steps_Cloud.get(j).gettraceNo()){
									helper.insertintoSteps(steps_Cloud.get(j));
								}
							}
						}
						PostTrailDetail traildetail=new PostTrailDetail(handler1,
								URL_GETTRAIL,
								item.getTraceData().getUserID(),
								item.getTraceData().getTraceNo(),
								deviceID);//2,3
						traildetail.start();
								
					}else{
						bothSize++;
					}
				}
				if(bothSize == choosedid.size()){
					//��ѡ�Ĺ켣���ѱ���
					dismissDialog();
				}
				if(trace_Upload.size() > 0){
					String traceinfo=GsonHelper.toJson(trace_Upload);
					String stepinfo="";
					if(steps_Upload.size()>0){
						stepinfo=GsonHelper.toJson(steps_Upload);
					}
					//Log.i("trailadapter","�ϴ��Ĺ켣��"+traceinfo+","+stepinfo);
					PostEndTrail endTrailThread=new PostEndTrail(handler2,
							URL_ENDTRAIL,traceinfo,stepinfo,deviceID);//2,3
					endTrailThread.start();
				}
			}else{
				ToastUtil.show(TraceListActivity.this, getResources().getString(R.string.tips_notraceselected));
			}
			break;
		}
		
	}
	// ���ڴ�������켣��Ϣ���켣��ϸ��Ϣ��ɾ���켣�ķ�������
	private Handler handler1=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 0://��ȡ�켣�б�ɹ�
				dismissDialog();
				if(msg.obj!=null){
					
					final String[] tracestr=msg.obj.toString().trim().split("!");
					Log.i("traceList", "traceList:" + msg.obj.toString().trim());
					
					if(tracestr.length==2){
						String trace=tracestr[0];
						String step=tracestr[1];
						trace_Cloud=(ArrayList<TraceData>) GsonHelper.parseJsonToList(trace, TraceData.class);
						steps_Cloud=(ArrayList<StepData>) GsonHelper.parseJsonToList(step,StepData.class);
						Log.i("traceCloud", ""+trace_Cloud.size());
						Log.i("traceCloud", ""+steps_Cloud.size());
						initBothTrace();
						Log.i("traceList", "����ִ����");
						mPullToRefreshView.onHeaderRefreshComplete("������:"+new Date().toLocaleString());
					}
				}
				break;
			
			case 1://��ȡ�б�ʧ��
				dismissDialog();
				initBothTrace();
				mPullToRefreshView.onHeaderRefreshComplete("����ʧ�ܣ����Ժ�����");
				Toast.makeText(TraceListActivity.this,R.string.tips_postfail, Toast.LENGTH_SHORT).show();	
				break;
			case 2:
				downloadCount--;
				if(msg.obj!=null){
					ArrayList<GpsData> gpsData=new ArrayList<GpsData>();
					
					gpsData=(ArrayList<GpsData>) GsonHelper.parseJsonToList(msg.obj.toString().trim(), GpsData.class);
				    //Toast.makeText(DrawPath.this, "�յ��켣������"+trails.size(), Toast.LENGTH_LONG).show();	
					if(gpsData.size()>0){  
						
				    	for(int i=0;i<gpsData.size();i++){
				    		helper.insertintoGpswithDate(gpsData.get(i));
				    	}
				    	if(downloadCount==0){
							dismissDialog();
							//ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_downfinish));
							showMenu(false, false);
							init();
				    	}
					}else{
						
						ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_downfail_nodata));
					}
				}
				else{
					ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_downfail_nodata));
				}
				break;
			case 3://��ȡ��ϸʧ��
				dismissDialog();
				
				ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_postfail));
				break;
			case 4://ɾ���ɹ�
				dismissDialog();
				init();
				
				ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_deletesuccess));
				break;
			case 5://ɾ��ʧ��
				dismissDialog();
				init();//ҲҪˢ�£���Ϊ���ع켣������ɾ��
				ToastUtil.show(TraceListActivity.this, getResources().getString(R.string.tips_deletefail));
				break;
			default:
				dismissDialog();
				mPullToRefreshView.onHeaderRefreshComplete("����ʧ�ܣ���������");
				ToastUtil.show(TraceListActivity.this, getResources().getString(R.string.tips_netdisconnect));
				break;	
			}
		}
	};
	private Handler handler2=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 0:
				break;
			case 1:
				break;
			case 2:
				dismissDialog();
				showMenu(false, false);
				init();
				
				//ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_uploadsuccess));
				break;
			case 3:
				dismissDialog();
				ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_uploadfail));
				break;
			default:
				dismissDialog();
				ToastUtil.show(TraceListActivity.this,getResources().getString(R.string.tips_netdisconnect));
				break;	
			}
		}
	};
	public void showMenu(boolean isMulChoice,boolean isNew){
		adapter.setIsMulti(isMulChoice);
		
		if(isNew){
			adapter.setDataSource(traceItems, steps_Both);
			
		}
		adapter.resetList(isMulChoice);
		
		if(isMulChoice){
			menuLayout.setVisibility(View.VISIBLE);
			
		}
		else{
			menuLayout.setVisibility(View.GONE);
			
		}
		tv_count.setText("");
		adapter.notifyDataSetChanged();
		
	}
	private class SortByTraceNo implements Comparator<TraceListItems>{

		@Override
		public int compare(TraceListItems lhs, TraceListItems rhs) {
			// TODO Auto-generated method stub
			long t1 = lhs.getTraceData().getTraceNo();
			long t2 = rhs.getTraceData().getTraceNo();
			if(t1 > t2){
				return -1;
			}
			return 1;
		}
		
	}
	public class TraceListItems {
		private TraceData trace;
		private boolean isLocal;
		private boolean isCloud;
		public TraceListItems(TraceData trace,boolean isLocal,boolean isCloud){
			this.trace = trace;
			this.isLocal = isLocal;
			this.isCloud = isCloud;
		}
		public TraceListItems(){}
		public void setTraceData(TraceData trace){
			this.trace = trace;
		}
		public void setIsLocal(boolean isLocal){
			this.isLocal = isLocal;
		}
		public void setIsCloud(boolean isCloud){
			this.isCloud = isCloud;
		}
		public TraceData getTraceData(){
			return trace;
		}
		public boolean getIsLocal(){
			return isLocal;
			
		}
		public boolean getIsCloud(){
			return isCloud;
		}
	}
	/**
	 * ��ʾ�������Ի���
	 */
	public void showDialog(String title,String message) {
		if (proDialog == null)
			proDialog = new ProgressDialog(this);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setIndeterminate(false);
		proDialog.setCancelable(true);
		proDialog.setTitle(title);
		proDialog.setMessage(message);
		proDialog.show();
	}

	/**
	 * ���ؽ������Ի���
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}
	
}
