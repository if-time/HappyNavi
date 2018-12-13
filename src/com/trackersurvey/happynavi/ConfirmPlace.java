/**
 * 
 */
package com.trackersurvey.happynavi;

import java.util.ArrayList;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewDebug.IntToString;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 输入或选择地点
 * 
 * @author Eaa
 * @version 2015年12月9日 下午4:33:46
 */
public class ConfirmPlace extends Activity {
	private static final int REQUEST_PLACE =3;
	
	// 标题栏按钮
	private ImageButton backoff; // 后退
	private Button confirm; // 确认
	private TextView  headText;
	private EditText inputPlace;   //输入的地点名字
	private ListView placeList;  //地名列表
	private String placeName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.selectplace_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.placetitle);
		AppManager.getAppManager().addActivity(this);
		inputPlace =(EditText) findViewById(R.id.editplace);
		Intent commentInten = getIntent();
		String value = commentInten.getStringExtra("place");
		inputPlace.setText(value);
		inputPlace.selectAll();
		
		//headText = (TextView) findViewById(R.id.header_text);
		//headText.setText("地点");

		backoff = (ImageButton) findViewById(R.id.header_left_btn);
		// 点击返回按钮，结束本Activity
		backoff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConfirmPlace.this.finish();
			}
		});

		confirm = (Button) findViewById(R.id.header_right_btn);
		// 点击确认按钮,返回地点
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				placeName = inputPlace.getText().toString().trim();
				Intent intent = new Intent(ConfirmPlace.this,CommentActivity.class);
				intent.putExtra("place", placeName);
				//Log.i("placeCon",REQUEST_PLACE+" "+placeName);
				setResult(RESULT_OK, intent);
				ConfirmPlace.this.finish();
			}
		});

		
		placeList  = (ListView) findViewById(R.id.listViewPlace);
		ArrayList<String> places = Common.getAddressList();
		if(null !=places){
			PlaceListAdapter plAdapter =new  PlaceListAdapter(places);
			placeList.setAdapter(plAdapter);
		}
		
		placeList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String place = ((TextView)(view.findViewById(R.id.list_place_text))).getText().toString();
				inputPlace.setText(place);
			}
		});
		
	}
	
	
	class PlaceListAdapter extends BaseAdapter{

		private ArrayList<String> items;
		public PlaceListAdapter(ArrayList<String> items) {
			// TODO Auto-generated constructor stub
			this.items = items;
		}
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView placeText = null;
			if(null == convertView){
				convertView = View.inflate(getApplicationContext(), R.layout.list_item_place, null);
				placeText = (TextView) convertView.findViewById(R.id.list_place_text);
				convertView.setTag(placeText);
				
			}else{
				placeText = (TextView) convertView.getTag();
			}
			
			placeText.setText(items.get(position));
			
			return convertView;
		}
		
	}
	
	
}
