package com.trackersurvey.adapter;

import java.util.List;

import com.trackersurvey.entity.CityData;
import com.trackersurvey.happynavi.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Spinner适配器
 * @author zhanghao
 * 2017/7/28
 *
 */
public class MySpinnerAdapter extends BaseAdapter{

	private List<CityData> list;
	private int layoutId;
	private Context context;
	
	public MySpinnerAdapter(Context context, List<CityData> list, int layoutId) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
		this.layoutId = layoutId;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if(convertView == null){
			viewHolder = new ViewHolder();
			//加载item布局
			convertView = LayoutInflater.from(context).inflate(layoutId, null);
			//获取实例化
			viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_name);
			//保存实例化的控件
			convertView.setTag(viewHolder);
		}else{
			//重新获取实例化的控价
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		//绑定数据
        viewHolder.cityName.setText(list.get(position).getCity());
        return convertView;
	}
	
	public class ViewHolder{
		TextView cityName;
	}
	//在屏幕滑动的过程中，最顶部或最底部的Item由可见状态转变为不可见状态的时候都会回调getView方法。
	//获取原来的Item里面的控件将其重新绑上数据。具体绘制原理请各位搜索一下吧。

}
