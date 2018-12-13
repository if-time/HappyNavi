package com.trackersurvey.photoview;

import java.util.TreeMap;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

// �����SelectedTreeMap �Ĵ��룬�ǳ��򵥵�һ�����л�Ԫ�ء����ڴ���Ѿ�ѡ�е�ͼƬTreeMap<Long, Uri>
// selectedTree
public class SelectedTreeMap implements Parcelable {
	private static final long serialVersionUID = 6118012822436702146L;
	private static TreeMap<Long, Uri> treeMap = null;

	public TreeMap<Long, Uri> getTreeMap() {
		return treeMap;
	}

	public void setTreeMap(TreeMap<Long, Uri> treeMap) {
		this.treeMap = treeMap;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeMap(treeMap);
	}
	
	
	
	
	public static final Parcelable.Creator<SelectedTreeMap> CREATOR = new Parcelable.Creator<SelectedTreeMap>() {

		@Override
		public SelectedTreeMap createFromParcel(Parcel source) {
			SelectedTreeMap stm = new SelectedTreeMap();
			source.readMap(treeMap, TreeMap.class.getClassLoader());
			return stm;
		}

		@Override
		public SelectedTreeMap[] newArray(int size) {
			// TODO Auto-generated method stub
			return new SelectedTreeMap[size];
		}
	};
	
}