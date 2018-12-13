package com.trackersurvey.helper;

import java.util.ArrayList;

import com.trackersurvey.happynavi.R;

//import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

//@SuppressLint("NewApi")
/**
 * �����б��ؼ�
 */
public class DropDownListView extends LinearLayout {

	private TextView tv_behavior;
	private ImageView imageView;
	private PopupWindow popupWindow = null;
	private ArrayList<String> dataList = new ArrayList<String>();
	private View mView;
	private ListView lv_behavior;
	private int selectedPosition = 4;
	
	public DropDownListView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public DropDownListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public DropDownListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView();
	}

	public void initView() {
		String infServie = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater layoutInflater;
		layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
		View view = layoutInflater.inflate(R.layout.dropdownlist, this, true);
		tv_behavior = (TextView) findViewById(R.id.text);
		imageView = (ImageView) findViewById(R.id.btn);
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (popupWindow == null) {
					showPopWindow();
				} else {
					closePopWindow();
				}
			}
		});
	}

	/**
	 * �������б���
	 */
	private void showPopWindow() {
		// ����popupWindow�Ĳ����ļ�
		String infServie = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater layoutInflater;
		layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
		View contentView = layoutInflater.inflate(R.layout.dropdownlist_popupwindow, null, false);
		
		lv_behavior = (ListView) contentView.findViewById(R.id.lv_behavior);
		lv_behavior.setAdapter(new DropDownListAdapter(getContext(), dataList));
		popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(this);
	}

	/**
	 * �ر������б���
	 */
	private void closePopWindow() {
		popupWindow.dismiss();
		popupWindow = null;
	}

	/**
	 * ��������
	 * 
	 * @param list
	 */
	public void setItemsData(ArrayList<String> list) {
		dataList = list;
		tv_behavior.setText(list.get(4).toString());
	}
	public int getSelectedPosition(){
		return selectedPosition;
	}
	/**
	 * ����������
	 */
	class DropDownListAdapter extends BaseAdapter {

		Context mContext;
		ArrayList<String> mData;
		LayoutInflater inflater;

		public DropDownListAdapter(Context ctx, ArrayList<String> data) {
			mContext = ctx;
			mData = data;
			inflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			// �Զ�����ͼ
			ListItemView listItemView = null;
			if (convertView == null) {
				// ��ȡlist_item�����ļ�����ͼ
				convertView = inflater.inflate(R.layout.dropdownlist_item, null);

				listItemView = new ListItemView();
				// ��ȡ�ؼ�����
				listItemView.tv = (TextView) convertView.findViewById(R.id.tv_behavior_item);

				listItemView.layout = (LinearLayout) convertView.findViewById(R.id.layout_container);
				// ���ÿؼ�����convertView
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			// ��������
			listItemView.tv.setText(mData.get(position).toString());
			
			listItemView.layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					selectedPosition = position;
					tv_behavior.setText(mData.get(position).toString());
					closePopWindow();

				}
			});
			return convertView;
		}
	}

	private static class ListItemView {
		TextView tv;
		LinearLayout layout;
	}
}
