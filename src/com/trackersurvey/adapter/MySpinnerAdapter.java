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
 * Spinner������
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
			//����item����
			convertView = LayoutInflater.from(context).inflate(layoutId, null);
			//��ȡʵ����
			viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_name);
			//����ʵ�����Ŀؼ�
			convertView.setTag(viewHolder);
		}else{
			//���»�ȡʵ�����Ŀؼ�
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		//������
        viewHolder.cityName.setText(list.get(position).getCity());
        return convertView;
	}
	
	public class ViewHolder{
		TextView cityName;
	}
	//����Ļ�����Ĺ����У��������ײ���Item�ɿɼ�״̬ת��Ϊ���ɼ�״̬��ʱ�򶼻�ص�getView������
	//��ȡԭ����Item����Ŀؼ��������°������ݡ��������ԭ�����λ����һ�°ɡ�

}
