package com.trackersurvey.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.trackersurvey.adapter.GroupAdapter;
import com.trackersurvey.entity.GroupInfo;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.SetParameter;
import com.trackersurvey.happynavi.TabHost_Main;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.PullToRefreshView;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.helper.PullToRefreshView.OnHeaderRefreshListener;
import com.trackersurvey.httpconnection.PostGroupInfo;
import com.trackersurvey.httpconnection.PostJoinOrExitGroup;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class AllGroupFragment extends Fragment implements OnClickListener,OnHeaderRefreshListener{
	private RelativeLayout layout;//长按弹出的底部菜单
	private ImageButton search;
	private Button cancel;
	private Button join;
	private PullToRefreshView mPullToRefreshView;
	private ListView groupList;
	private TextView selectedcount;
	//private TextView tiptxt;
	private EditText et_search;
	
	private Context context;
	private GroupAdapter mAdapter;
	private ArrayList<GroupInfo> groups=new ArrayList<GroupInfo>();
	private ProgressDialog proDialog = null;
	private String url_GetAllGroup = null;
	private String url_JoinGroup = null;
	private String key = null;
	private boolean isFirstCreated=true;
	
	private RefreshBroadcastReciver refreshReciver;
	private  final String ALLGROUPREFRESH_ACTION="android.intent.action.ALLGROUPREFRESH_RECEIVER";
	private final String MYGROUPREFRESH_ACTION="android.intent.action.MYGROUPREFRESH_RECEIVER";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		int l = TabHost_Main.l;
		View view = null;
		
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
		
		if(l == 0){			
			view = inflater.inflate(R.layout.fragment_allgroup, null);       
		}
		if(l == 1){
			view = inflater.inflate(R.layout.fragment_allgroup_en, null);
		}
		if(l == 2){
			view = inflater.inflate(R.layout.fragment_allgroup_en, null);
		}
		context=getActivity();
		layout=(RelativeLayout)view.findViewById(R.id.allgroup_relative);
		cancel=(Button)view.findViewById(R.id.cancel);
		search=(ImageButton)view.findViewById(R.id.bt_search);
		join=(Button)view.findViewById(R.id.joingroup);
		et_search=(EditText)view.findViewById(R.id.et_search);
		search.setOnClickListener(this);
		cancel.setOnClickListener(this);
		join.setOnClickListener(this);
		
		mPullToRefreshView = (PullToRefreshView)view.findViewById(R.id.main_pull_refresh_view);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		groupList=(ListView)view.findViewById(R.id.listview_allgroup);
		groupList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				showMenu(true,false);
				
				return true;
			}
			
		});
		selectedcount=(TextView)view.findViewById(R.id.txtcount);
		//tiptxt=(TextView)view.findViewById(R.id.tip);
		//tiptxt.setOnClickListener(this);
		
		refreshReciver=new RefreshBroadcastReciver();
		IntentFilter pullFilter=new IntentFilter();
		pullFilter.addAction(ALLGROUPREFRESH_ACTION);
		context.registerReceiver(refreshReciver, pullFilter);
		
		
		
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		url_GetAllGroup=Common.url+"group.aspx";
		url_JoinGroup=Common.url+"group.aspx";
		//init();
		return view;
	}
	void init(String key)
	{
		if(key == null || key.equals("")){
			return;
		}
		PostGroupInfo groupThread=new PostGroupInfo(handler,url_GetAllGroup,Common.getUserId(context),Common.getDeviceId(context),"Search",key);
		groupThread.start();
	}
	private class RefreshBroadcastReciver extends BroadcastReceiver {
		 
		private RefreshBroadcastReciver(){
			
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			init(key);
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
					String groupStr = msg.obj.toString().trim();
					int lastsize=groups.size();
					groups=(ArrayList<GroupInfo>) GsonHelper.parseJsonToList(groupStr, GroupInfo.class);
					if(isFirstCreated){
				    	mAdapter=new GroupAdapter(context,selectedcount,groups,"join",groupList);
						groupList.setAdapter(mAdapter);
						isFirstCreated=false;
				    	
				    }else{
				    	
				    		showMenu(false, true);
				    	
				    }
				    if(groups.size()==0){
				    	ToastUtil.show(context, getResources().getString(R.string.tips_search_nogroup));
						
				    }
//				    else{
//						tiptxt.setVisibility(View.INVISIBLE);
//					}
				    //refreshtip.setVisibility(View.GONE);
					mPullToRefreshView.onHeaderRefreshComplete("更新于:"+new Date().toLocaleString());
				}
				break;
			case 1://获取列表失败
				dismissDialog();
				Toast.makeText(context, getResources().getString(R.string.tips_postfail), Toast.LENGTH_SHORT).show();	
				mPullToRefreshView.onHeaderRefreshComplete("更新失败，请稍后再试");
				break;
			case 4://加群申请发送成功，待批复
				dismissDialog();
				
				ToastUtil.show(context,getResources().getString(R.string.tips_applygroupsuccess));
				break;
			case 5://加群失败
				dismissDialog();
				ToastUtil.show(context, getResources().getString(R.string.tips_applygroupfail));
				break;
			case 10://网络错误
				dismissDialog();
				mPullToRefreshView.onHeaderRefreshComplete("更新失败，请检查网络");
				ToastUtil.show(context, getResources().getString(R.string.tips_netdisconnect));
//				if(groups.size()==0){
//					tiptxt.setVisibility(View.VISIBLE);
//					tiptxt.setText(R.string.trytorefresh);
//				}else{
//					tiptxt.setVisibility(View.INVISIBLE);
//				}
				break;
			case 11://网络错误
				dismissDialog();
				ToastUtil.show(context,getResources().getString(R.string.tips_netdisconnect));
				break;
			}
		}
	};
	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		init(key);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.bt_search:
			//ToastUtil.show(context, et_search.getText().toString());
			key = et_search.getText().toString().trim();
			if(key == null || key.equals("")){
				ToastUtil.show(context, getResources().getString(R.string.tips_search_cannotnull));
				return;
			}else{
				showDialog(getResources().getString(R.string.tip), getResources().getString(R.string.tips_searching));
				
				init(key);
			}
			break;
//		case R.id.tip://点此刷新
//			showDialog(getResources().getString(R.string.tip), getResources().getString(R.string.tips_initgroup));
//			init();
//			break;
		case R.id.cancel:
			showMenu(false,false);
			break;
		case R.id.joingroup:
			final List<Integer> selectid = mAdapter.getSelected();
			final int size = selectid.size();
			if(size > 0){
				showDialog(getResources().getString(R.string.tip),getResources().getString(R.string.tips_handling));
				ArrayList<String> tobeJoinedID=new ArrayList<String>();
				
				for(int i = 0;i < size; i++){
					tobeJoinedID.add(groups.get(selectid.get(i)).getGroupID());
				}
				String tobeJoined=GsonHelper.toJson(tobeJoinedID);
				Log.i("trailadapter", "tobeJoined:"+tobeJoined);
				PostJoinOrExitGroup joinThread=new PostJoinOrExitGroup(handler,url_JoinGroup,
						Common.getUserId(context),tobeJoined,
						Common.getDeviceId(context),"JoinGroups");
				joinThread.start();
			}else{
				ToastUtil.show(context, getResources().getString(R.string.tips_pleasechoosegroup));
			}
			break;
		}
		
	}
	public void showMenu(boolean isMulChoice,boolean isNew){
		mAdapter.refresh(isMulChoice, isNew, groups);
		
		if(isMulChoice){
			layout.setVisibility(View.VISIBLE);
		}
		else{
			layout.setVisibility(View.GONE);
		}
		selectedcount.setText("");
		
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
		if(null!=refreshReciver){
			context.unregisterReceiver(refreshReciver);
		}
	}
}
