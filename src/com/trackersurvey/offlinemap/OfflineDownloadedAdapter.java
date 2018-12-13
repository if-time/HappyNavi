package com.trackersurvey.offlinemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;



/**
 * 
 * ά���Ѿ����غ����ع����е��б�
 */
public class OfflineDownloadedAdapter extends BaseAdapter {

	private OfflineMapManager mOfflineMapManager;

	private List<OfflineMapCity> cities = new ArrayList<OfflineMapCity>();

	private Context mContext;

	private OfflineChild currentOfflineChild;
	
	public OfflineDownloadedAdapter(Context context,
			OfflineMapManager offlineMapManager) {
		this.mContext = context;
		this.mOfflineMapManager = offlineMapManager;
		initCityList();

	}

	/**
	 * ���³�ʼ�����ݼ�������
	 */
	public void notifyDataChange() {
		initCityList();
	}

	private void initCityList() {
		if (cities != null) {
			for (Iterator it = cities.iterator(); it.hasNext();) {
				OfflineMapCity i = (OfflineMapCity) it.next();
				it.remove();
			}
			// arraylist�е�Ԫ�ز��������Ƴ�
//			for (OfflineMapCity mapCity : cities) {
//				cities.remove(mapCity);
//				mapCity = null;
//			}
		}

		cities.addAll(mOfflineMapManager.getDownloadOfflineMapCityList());
		cities.addAll(mOfflineMapManager.getDownloadingCityList());
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return cities.size();
	}

	@Override
	public Object getItem(int index) {
		return cities.get(index);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public int getItemViewType(int position) {

		return 0;
	}

	public final class ViewHolder {
		public OfflineChild mOfflineChild;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;
		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();

			currentOfflineChild = new OfflineChild(mContext,
					mOfflineMapManager);
			convertView = currentOfflineChild.getOffLineChildView();
			viewHolder.mOfflineChild = currentOfflineChild;
			convertView.setTag(viewHolder);
		}
		OfflineMapCity offlineMapCity = (OfflineMapCity) getItem(position);
		viewHolder.mOfflineChild.setOffLineCity(offlineMapCity);

		return convertView;

	}
}