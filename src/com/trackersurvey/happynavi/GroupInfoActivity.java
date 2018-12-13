package com.trackersurvey.happynavi;

import java.util.ArrayList;

import com.trackersurvey.entity.GroupInfo;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ImageLoader;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostJoinOrExitGroup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupInfoActivity extends Activity{
	private MyLinearLayout back;
	private TextView title;
	private Button titleRightBtn;
	private ImageView iv_groupphoto;
	private TextView tv_groupname;
	private TextView tv_createman;
	private TextView tv_managerids;
	private TextView tv_groupdetail;
	private TextView tv_membernums;
	private TextView tv_createtime;
	
	//private Button GroupButton;
	private String url_JoinGroup = null;
	private String url_ExitGroup = null;
	private String handleType = null;
	private  final String ALLGROUPREFRESH_ACTION="android.intent.action.ALLGROUPREFRESH_RECEIVER";
	private final String MYGROUPREFRESH_ACTION="android.intent.action.MYGROUPREFRESH_RECEIVER";
	
	private ImageLoader mLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.groupinfo);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		
		init();
		
	}
	public void init(){
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.groupinfo));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setVisibility(View.VISIBLE);
		iv_groupphoto = (ImageView) findViewById(R.id.iv_groupimage);
		tv_groupname = (TextView) findViewById(R.id.tv_groupname);
		tv_createman = (TextView) findViewById(R.id.tv_createman);
		tv_managerids = (TextView) findViewById(R.id.tv_managerids);
		tv_groupdetail = (TextView) findViewById(R.id.tv_groupdetail);
		tv_membernums = (TextView) findViewById(R.id.tv_membernums);
		tv_createtime = (TextView) findViewById(R.id.tv_createtime);
		//GroupButton = (Button) findViewById(R.id.handlegroup);
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		url_ExitGroup=Common.url+"group.aspx";
		url_JoinGroup=Common.url+"group.aspx";
		Intent intent=getIntent();
		handleType=intent.getStringExtra("handletype");
		String groupStr=intent.getStringExtra("groupinfo");
		if(handleType == null || handleType.equals("") ||
				groupStr == null || groupStr.equals("")){
			ToastUtil.show(this, getResources().getString(R.string.tips_errorversion));
			
			return;
		}
		if(handleType.equals("join")){
			titleRightBtn.setText(R.string.joingroup);
		}else{
			titleRightBtn.setText(R.string.exitgroup);
		}
		final GroupInfo groupInfo = GsonHelper.parseJson(groupStr, GroupInfo.class);
		Log.i("trailadapter", groupStr);
		if(groupInfo != null){
			mLoader = new ImageLoader();
			mLoader.loadOneImage(iv_groupphoto, groupInfo.getPhotoUrl(),groupInfo.getPhotoName());
			tv_groupname.setText(groupInfo.getGroupName());
			tv_createman.setText(groupInfo.getCreateMan());
			String[] ManagerIDs = groupInfo.getManagerIDs();
			StringBuffer ManagerIDs_Str = new StringBuffer();
			for(int i = 0; i < ManagerIDs.length; i++){
				ManagerIDs_Str.append(ManagerIDs[i]);
				ManagerIDs_Str.append(" ");
			}
			tv_managerids.setText(ManagerIDs_Str);
			tv_groupdetail.setText(groupInfo.getGroupDetail());
			tv_createtime.setText(groupInfo.getCreateTime());
			tv_membernums.setText(groupInfo.getMemberNums()+"");
			titleRightBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(handleType.equals("join")){
						ArrayList<String> tobeJoinedID=new ArrayList<String>();
						tobeJoinedID.add(groupInfo.getGroupID());
						
						String tobeJoined=GsonHelper.toJson(tobeJoinedID);
						Log.i("trailadapter", "tobeJoined:"+tobeJoined);
						PostJoinOrExitGroup joinThread=new PostJoinOrExitGroup(handler,url_JoinGroup,
								Common.getUserId(GroupInfoActivity.this),tobeJoined,
								Common.getDeviceId(GroupInfoActivity.this),"JoinGroups");
						joinThread.start();
					}else if(handleType.equals("quit")){
						CustomDialog.Builder builder = new CustomDialog.Builder(GroupInfoActivity.this);
						builder.setTitle(getResources().getString(R.string.tip));
						builder.setMessage(getResources().getString(R.string.tips_quitgroupdlg_msg));
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
								
								ArrayList<String> tobeExitID=new ArrayList<String>();
								tobeExitID.add(groupInfo.getGroupID());
								
								String tobeExit=GsonHelper.toJson(tobeExitID);
								Log.i("trailadapter", "tobeexit:"+tobeExit);
								PostJoinOrExitGroup exitThread=new PostJoinOrExitGroup(handler,url_ExitGroup,
										Common.getUserId(GroupInfoActivity.this),tobeExit,
										Common.getDeviceId(GroupInfoActivity.this),"QuitGroups");
								exitThread.start();
								dialog.dismiss();
							}
						});
						builder.create().show();
						
					}
				}
			});
			
		}
	}
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 4://
				if(handleType.equals("join")){
					ToastUtil.show(GroupInfoActivity.this,getResources().getString(R.string.tips_applygroupsuccess));
					
				}else{
					Intent intent = new Intent();  
			        intent.setAction(ALLGROUPREFRESH_ACTION);  
			        sendBroadcast(intent);
			        intent.setAction(MYGROUPREFRESH_ACTION);
			        sendBroadcast(intent);
					ToastUtil.show(GroupInfoActivity.this,getResources().getString(R.string.tips_success));
					finish();
				}
				break;
			case 5://
				
				ToastUtil.show(GroupInfoActivity.this, getResources().getString(R.string.tips_postfail));
				break;
			case 11://ÍøÂç´íÎó
				
				ToastUtil.show(GroupInfoActivity.this,getResources().getString(R.string.tips_netdisconnect));
				break;
			}
		}
	};
}
