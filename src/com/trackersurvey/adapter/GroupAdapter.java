package com.trackersurvey.adapter;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import com.trackersurvey.entity.GroupInfo;
import com.trackersurvey.happynavi.GroupInfoActivity;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ImageLoader;
import com.trackersurvey.helper.ToastUtil;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter implements ListView.OnScrollListener{
	private Context context;
	private LayoutInflater mInflater;
	public  ArrayList<GroupInfo> groups;
	private ImageLoader mLoader;
	public  HashMap<Integer, Integer> visiblecheck ;//������¼�Ƿ���ʾcheckBox
	public  HashMap<Integer, Boolean> ischeck;
	public  List<Integer> selectid;
	public  boolean isMulChoice;
	
	private TextView txtcount;
	private String handleType;//��Ⱥ����Ⱥ
	private int mStart,mEnd;//������������Ļ�Ͽɼ���item��ֹpostion
    public static String[] URLArray;//��������ͼƬ��url
    public static String[] IconName;//��������ͼƬ������

    private boolean isFirstShow;//�Ƿ��ǵ�һ������
	public GroupAdapter(Context context,TextView txtcount,
			ArrayList<GroupInfo> groups,String handleType,ListView listView){
		this.context=context;
		this.txtcount=txtcount;
		this.groups=groups;
		this.handleType = handleType;
		mInflater=LayoutInflater.from(context); //(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLoader=new ImageLoader(listView);
        URLArray = new String[groups.size()];
        IconName = new String[groups.size()];
        for(int i=0;i<URLArray.length;i++){
            URLArray[i] = groups.get(i).getPhotoUrl();
            IconName[i] = groups.get(i).getPhotoName();
        }
        //Ϊlistview���û�������
        listView.setOnScrollListener(this);
        isFirstShow = true;
		//mView = new HashMap<Integer, View>();
		visiblecheck = new HashMap<Integer, Integer>();
		ischeck      = new HashMap<Integer, Boolean>();
		selectid = new ArrayList<Integer>();
		if(isMulChoice){
		       for(int i=0;i<groups.size();i++){
		              ischeck.put(i, false);
		              visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}else{
		       for(int i=0;i<groups.size();i++)
		       {
		            ischeck.put(i, false);
		            visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
	}
	public void refresh(boolean isMulChoice,boolean isNew,ArrayList<GroupInfo> groups){
		this.isMulChoice = isMulChoice;
		this.selectid.clear();
		if(isNew){
			this.groups=groups;
			
		}
		
		
		this.visiblecheck.clear();
		this.ischeck.clear();
		
		if(isMulChoice){
		       for(int i=0;i<groups.size();i++){
		    	   this.ischeck.put(i, false);
		    	   this.visiblecheck.put(i, CheckBox.VISIBLE);
		       }
		}else{
		       for(int i=0;i<groups.size();i++)
		       {
		    	   this.ischeck.put(i, false);
		    	   this.visiblecheck.put(i, CheckBox.INVISIBLE);
		       }
		}
		
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return groups.size();//����ʹ��Ĭ�Ϸ���
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return groups.get(position);//����ʹ��Ĭ�Ϸ���
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
			convertView = mInflater.inflate(R.layout.grouplistitem, null);
			holder.groupPhoto=(ImageView) convertView.findViewById(R.id.grouplistitem_grouppic);
			holder.groupName=(TextView)convertView.findViewById(R.id.tv_itemgroupname);
			holder.groupDetail=(TextView)convertView.findViewById(R.id.tv_itemgroupdetail);
			holder.checkbox=(CheckBox)convertView.findViewById(R.id.grouplistitem_check);
			// Ϊview���ñ�ǩ
			convertView.setTag(holder);
			
		}else{
			
				// ȡ��holder
			holder = (ViewHolder) convertView.getTag();
			
		}
			String url = groups.get(position).getPhotoUrl();
			holder.groupPhoto.setTag(url);
			mLoader.showImageIfExist(holder.groupPhoto, url);
			holder.groupName.setText(groups.get(position).getGroupName());
			holder.groupDetail.setText(groups.get(position).getGroupDetail());
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
						   //Toast.makeText(context, "�����"+array.get(position), Toast.LENGTH_LONG).show();
						//Gson gson=new Gson();
						if(!Common.isNetConnected){
							ToastUtil.show(context, context.getResources().getString(R.string.tips_netdisconnect));
							return;
						}
						String groupinfo=GsonHelper.toJson(groups.get(pos));
						
						Intent intent=new Intent();
						intent.putExtra("handletype", handleType);
						intent.putExtra("groupinfo", groupinfo);
						
						intent.setClass(context, GroupInfoActivity.class);
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
	public  HashMap<Integer, Integer> getVisible(){
		return visiblecheck;
	}
	public  HashMap<Integer, Boolean> getIsCheck(){
		return ischeck;
	}
	public  List<Integer> getSelected(){
		return selectid;
	}
	public  boolean getIsMulti(){
		return isMulChoice;
	}
	public final class ViewHolder {
		ImageView groupPhoto;
		TextView groupName;
		TextView groupDetail;
		CheckBox checkbox;
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if(scrollState == SCROLL_STATE_IDLE){//��������
            //����ͼƬ
            mLoader.loadImagesNonScroll(mStart,mEnd);
        }else{//����ʱֹͣ���أ���ֹ����ʱ�����첽�����ػ�UI���µĿ���
            mLoader.cancelAllTasks();
        }
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		mStart=firstVisibleItem;
        mEnd=firstVisibleItem+visibleItemCount;
        if(isFirstShow && visibleItemCount>0){
            mLoader.loadImagesNonScroll(mStart,mEnd);
            isFirstShow = false;
        }
	}
}