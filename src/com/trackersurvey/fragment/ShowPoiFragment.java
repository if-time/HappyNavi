package com.trackersurvey.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.trackersurvey.adapter.ListBaseAdapter;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.TabHost_Main;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.model.MyCommentModel;
import com.trackersurvey.photoview.SlideListView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import android.widget.AbsListView.OnScrollListener;

public class ShowPoiFragment extends Fragment{
	
	
	private SlideListView lView; // ����������б�
	private PoiEventReceiver poiReceiver;
	// ������������ģ��
	//private MyCommentModel ShowTraceFragment.myComment;
	// listView��������
	private ListBaseAdapter listAdapter;
	private Context context;
	private ArrayList<HashMap<String, Object>> items;
	public final String UPDATEUI_ACTION="android.intent.action.UPDATEUI_RECEIVER";
	public final String INITADAPTER_ACTION="android.intent.action.ADAPTER_RECEIVER";
	private static MyCommentModel myComment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_showpoi, null);      
		
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
		
		context=getActivity();
		myComment = new MyCommentModel(context, "mark");
		
		Common.createFileDir();
		//initModel();
		
		items = new ArrayList<HashMap<String,Object>>();
		lView = (SlideListView) view.findViewById(R.id.poilistview);
		initAdapter();
		
		poiReceiver = new PoiEventReceiver();
		IntentFilter pullFilter=new IntentFilter();
		pullFilter.addAction(INITADAPTER_ACTION);
		pullFilter.addAction(UPDATEUI_ACTION);
		context.registerReceiver(poiReceiver, pullFilter);
		return view;
	}
	
	private class PoiEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(UPDATEUI_ACTION)){
				updateUI();
			}
		}
		
	}
	



	/**
	 * ��ʼ��������������ListView��item��������ļ���
	 */
	private void initAdapter() {
		listAdapter = new ListBaseAdapter(context, ShowTraceFragment.myComment,items,"mark");

//		myComment = new MyCommentModel(context, "album");
		
		//����ɾ����ť
		listAdapter.setDeleCommListener(new ListBaseAdapter.DeleCommListener() {
			@Override
			public void clickDelete(String dateTime, int position) {
				// TODO Auto-generated method stub
				deleteEvent(dateTime, position);
			}
		});
		initListView();

	}
	private void initListView(){
		// �ҵ����� �����е�ListView
//				lView.initSlideMode(SlideListView.MOD_RIGHT);

				lView.setAdapter(listAdapter);

				lView.setOnScrollListener(new OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
//						if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
//								|| scrollState == OnScrollListener.SCROLL_STATE_FLING) {
//							if ((view.getLastVisiblePosition() == view.getCount() - 1)
//									&&ShowTraceFragment.myComment.cloudMore()) {
//								Log.i("Eaa", "�Զ���������,size="+view.getCount() );
//								ShowTraceFragment.myComment.autoAddtoList();
//							}
//						}
					}
					
					@Override
					public void onScroll(AbsListView view, int firstVisibleItem,
							int visibleItemCount, int totalItemCount) {
						// TODO Auto-generated method stub
						
					}
				});
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		// Log.i("Eaa", "onResume");
		super.onResume();
	}
	
	/**
	 * �����Ի������û�ѡ���Ƿ�ȷ��ɾ��һ������
	 * @param dateTime
	 * @param position
	 */
	private void deleteEvent(final String dateTime, final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(
						getResources().getString(
								R.string.tips_deletedlgmsg_album));
		builder.setPositiveButton(getResources().getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								deleteComment(dateTime, position);
							}
						});
		builder.setNegativeButton(getResources().getString(R.string.cancl),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
		builder.create().show();

	}
	
	/**
	 * ˢ�½���
	 */
	public void updateUI(){
		
		listAdapter.setItems(ShowTraceFragment.myComment.getItems());
		listAdapter.notifyDataSetChanged();
		Log.i("itemsss", "ShowPoiFragment:"+ShowTraceFragment.myComment.getItems().toString());

	}
	
	/**
	 * ֪ͨģ��ɾ��һ������
	 */
	private void deleteComment(String dateTime, int listPosition) {
		ShowTraceFragment.myComment.deleteComment(dateTime, listPosition);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("mark", "PoiFragment destory");
		//ShowTraceFragment.myComment.stopModel();
		//ShowTraceFragment.myComment = null;
		listAdapter = null;
		if(null!=poiReceiver){
			context.unregisterReceiver(poiReceiver);
		}
		super.onDestroy();
	}
	public int getItemsNum(){
		int itemsNum = items.size();
		return itemsNum ;
	}
	public ArrayList<HashMap<String, Object>> getItems(){
		return items;
	}
}


