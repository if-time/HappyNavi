package com.trackersurvey.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.trackersurvey.happynavi.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<HashMap<String, Object>> item;
	
	public DialogAdapter(Context context, ArrayList<HashMap<String, Object>> dialogListItem) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.item = dialogListItem;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return item.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return item.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListViewItemHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.dialog_tools_items, null);
			holder = new ListViewItemHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.tool_image);
			holder.textView = (TextView) convertView.findViewById(R.id.tool_text);
			convertView.setTag(holder);
		}else{
			holder = (ListViewItemHolder) convertView.getTag();
		}
		
		HashMap<String, Object> map = item.get(position);
		int iconId = (Integer) map.get("icon");
		String text = (String) map.get("text");
		holder.imageView.setImageResource(iconId);
		holder.textView.setText(text);
		
		return convertView;
	}
	
	//两个视图控件的一个封装类
	class ListViewItemHolder{
		ImageView imageView;
		TextView textView;
	}

}
