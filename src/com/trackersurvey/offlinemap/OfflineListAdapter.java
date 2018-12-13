package com.trackersurvey.offlinemap;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.trackersurvey.happynavi.R;


public class OfflineListAdapter extends BaseExpandableListAdapter implements OnGroupCollapseListener, OnGroupExpandListener{
	

	private boolean[] isOpen;// ��¼һ��Ŀ¼�Ƿ��
	
	private List<OfflineMapProvince> provinceList = null;
//	private HashMap<Object, List<OfflineMapCity>> cityMap = null;
	private OfflineMapManager amapManager;
	private Context mContext;
	
	
	public OfflineListAdapter(List<OfflineMapProvince> provinceList,
			HashMap<Object, List<OfflineMapCity>> cityMap,
			OfflineMapManager amapManager, Context mContext) {
		this.provinceList = provinceList;
//		this.cityMap = cityMap;
		this.amapManager = amapManager;
		this.mContext = mContext;
		
		isOpen = new boolean[provinceList.size()];
	}
	
	public OfflineListAdapter(List<OfflineMapProvince> provinceList,
			OfflineMapManager amapManager, Context mContext) {
		this.provinceList = provinceList;
//		this.cityMap = cityMap;
		this.amapManager = amapManager;
		this.mContext = mContext;
		
		isOpen = new boolean[provinceList.size()];
	}

	@Override
	public int getGroupCount() {
		return provinceList.size();
	}

	/**
	 * ��ȡһ����ǩ����
	 */
	@Override
	public Object getGroup(int groupPosition) {
		return provinceList.get(groupPosition).getProvinceName();
	}

	/**
	 * ��ȡһ����ǩ��ID
	 */
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * ��ȡһ����ǩ�¶�����ǩ������
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
//		return cityMap.get(groupPosition).size();
		if(isNormalProvinceGroup(groupPosition)) {
			// ��ͨʡ�ݵĵ�һ��λ�÷�ʡ��
			return provinceList.get(groupPosition).getCityList().size() + 1;
		} 
		return provinceList.get(groupPosition).getCityList().size();
		
	}

	/**
	 * ��ȡһ����ǩ�¶�����ǩ������
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {
//		return cityMap.get(groupPosition).get(childPosition).getCity();
//		return provinceList.get(groupPosition).getCityList().get(childPosition).getCity();
		return null;
	}

	/**
	 * ��ȡ������ǩ��ID
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * ָ��λ����Ӧ������ͼ
	 */
	@Override
	public boolean hasStableIds() {
		return true;
	}

	/**
	 * ��һ����ǩ��������
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView group_text;
		ImageView group_image;
		if (convertView == null) {
			convertView = (RelativeLayout) RelativeLayout.inflate(
					mContext, R.layout.offlinemap_group, null);
		}
		group_text = (TextView) convertView.findViewById(R.id.group_text);
		group_image = (ImageView) convertView
				.findViewById(R.id.group_image);
		group_text.setText(provinceList.get(groupPosition)
				.getProvinceName());
		if (isOpen[groupPosition]) {
			group_image.setImageDrawable(mContext.getResources().getDrawable(
					R.drawable.downarrow));
		} else {
			group_image.setImageDrawable(mContext.getResources().getDrawable(
					R.drawable.rightarrow));
		}
		return convertView;
	}

	/**
	 * ��һ����ǩ�µĶ�����ǩ��������
	 */
	@Override
	public View getChildView(int groupPosition,
			int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			OfflineChild offLineChild = new OfflineChild(mContext, amapManager);
			convertView = offLineChild.getOffLineChildView();
			viewHolder.mOfflineChild = offLineChild;
			convertView.setTag(viewHolder);
		}
//		OfflineMapCity mapCity = cityMap.get(groupPosition).get(
//				childPosition);
		OfflineMapCity mapCity = null;
		
		viewHolder.mOfflineChild.setProvince(false);
		
		if(isNormalProvinceGroup(groupPosition)) {
			if(isProvinceItem(groupPosition,childPosition)) {
				// �����ʡ�ݣ�Ϊ�˷��㣬����һ�´���
				mapCity = getCicy(provinceList.get(groupPosition));
				viewHolder.mOfflineChild.setProvince(true);
			} else {
				// ��1������һ����������ʡ��
				mapCity = provinceList.get(groupPosition).getCityList().get(childPosition - 1);
			} 
		} else {
			mapCity = provinceList.get(groupPosition).getCityList().get(childPosition);
		}
		
		
		
		viewHolder.mOfflineChild.setOffLineCity(mapCity);

		return convertView;
	}


	private boolean isProvinceItem(int groupPosition, int childPosition) {
		// ��������ʡ�ݣ���������Ŀ�е�һ��
		return isNormalProvinceGroup(groupPosition) && childPosition == 0;
		
//		if(isNormalProvince(groupPosition)) {
//			return false;
//		} else {
//			if(childPosition == 0) {
//				return true;
//			}
//		}
//		return false;
	}

	/**
	 * �Ƿ�Ϊ��ͨʡ��
	 * ����ֱϽ�У���Ҫͼ���۰�
	 * @param groupPosition
	 * @return
	 */
	private boolean isNormalProvinceGroup(int groupPosition) {
//		return groupPosition != 0 && groupPosition != 1 && groupPosition != 2;
		return groupPosition > 2;
	}

	/**
	 * ��ѡ���ӽڵ��ʱ�򣬵��ø÷���
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	
	/**
	 * ��һ��ʡ�Ķ���ת��Ϊһ���еĶ���
	 */
	public OfflineMapCity getCicy(OfflineMapProvince aMapProvince) {
		OfflineMapCity aMapCity = new OfflineMapCity();
		aMapCity.setCity(aMapProvince.getProvinceName());
		aMapCity.setSize(aMapProvince.getSize());
		aMapCity.setCompleteCode(aMapProvince.getcompleteCode());
		aMapCity.setState(aMapProvince.getState());
		aMapCity.setUrl(aMapProvince.getUrl());
		return aMapCity;
	}
	
	public final class ViewHolder {
		public OfflineChild mOfflineChild;
	}

	public void onGroupCollapse(int groupPosition) {
		isOpen[groupPosition] = false;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		isOpen[groupPosition] = true;
	}
}
