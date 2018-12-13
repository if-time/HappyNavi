package com.trackersurvey.happynavi;

import java.util.ArrayList;
import java.util.List;

import com.trackersurvey.adapter.FragmentAdapter;
import com.trackersurvey.fragment.CloudTrailFragment;
import com.trackersurvey.fragment.LocalTrailFragment;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageView;

import android.widget.TextView;

import android.widget.LinearLayout;
/**
 * ��class��2016-7-27��ͣ�ã�Ǩ����TraceListActivity
 * */
public class TrailsAnalysis extends FragmentActivity  {

	//private Button search;
	private ViewPager tracePager;
	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	private FragmentAdapter mFragmentAdapter;
	private LocalTrailFragment localPage;
	private CloudTrailFragment cloudPage;
	private TextView localTxt,cloudTxt;
	private LinearLayout localLayout,cloudLayout;//�ֶ�����л�fragment
	private ImageView mTabLineIv;
	private int currentIndex;
	private int screenWidth;
//	private FragmentManager fragmentManager;
//	private RadioButton localradio;
//	private RadioButton cloudradio;
//	private RadioGroup radioGroup;
//	private LinearLayout divider;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trailsanalysis);
		AppManager.getAppManager().addActivity(this);

		if(Common.isVisiter()){
			Common.DialogForVisiter(this);
			
		}
		else{
			init();
			initTabLineWidth();
		}
	}
	@SuppressWarnings("deprecation")
	public void init(){
		//search=(Button)findViewById(R.id.trail_search);
		//fragmentManager = getFragmentManager();
		cloudTxt = (TextView) findViewById(R.id.cloudtrace);
		localTxt = (TextView) findViewById(R.id.localtrace);
		localLayout = (LinearLayout) findViewById(R.id.id_tab_local);
		cloudLayout = (LinearLayout) findViewById(R.id.id_tab_cloud);
		mTabLineIv = (ImageView) findViewById(R.id.tab_traceline_iv);
		tracePager = (ViewPager) findViewById(R.id.viewpager_trace);
//        radioGroup = (RadioGroup) findViewById(R.id.trail_tab);
//        radioGroup.setVisibility(View.VISIBLE);
//        localradio=(RadioButton) findViewById(R.id.localradio);
//        localradio.setOnCheckedChangeListener(this);
//        cloudradio=(RadioButton) findViewById(R.id.cloudradio);
//        cloudradio.setOnCheckedChangeListener(this);
//        divider=(LinearLayout) findViewById(R.id.linearlayout_divider);
//        divider.setVisibility(View.VISIBLE);
        localPage= new LocalTrailFragment();
        cloudPage= new CloudTrailFragment();
        mFragmentList.add(localPage);
        mFragmentList.add(cloudPage);
        mFragmentAdapter = new FragmentAdapter(
				this.getSupportFragmentManager(), mFragmentList);
        tracePager.setAdapter(mFragmentAdapter);
        tracePager.setCurrentItem(0);
        localLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tracePager.setCurrentItem(0);
			}
		});
        cloudLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tracePager.setCurrentItem(1);
			}
		});
        tracePager.setOnPageChangeListener(new OnPageChangeListener() {

			/**
			 * state�����е�״̬ ������״̬��0��1��2�� 1�����ڻ��� 2��������� 0��ʲô��û����
			 */
			@Override
			public void onPageScrollStateChanged(int state) {

			}

			/**
			 * position :��ǰҳ�棬������������ҳ�� offset:��ǰҳ��ƫ�Ƶİٷֱ�
			 * offsetPixels:��ǰҳ��ƫ�Ƶ�����λ��
			 */
			@Override
			public void onPageScrolled(int position, float offset,
					int offsetPixels) {
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
						.getLayoutParams();

				//Log.e("offset:", offset + "");
				/**
				 * ����currentIndex(��ǰ����ҳ��)��position(��һ��ҳ��)�Լ�offset��
				 * ����mTabLineIv����߾� ����������
				 * ��3��ҳ��,
				 * �����ҷֱ�Ϊ0,1,2 
				 * 0->1; 1->2; 2->1; 1->0
				 */

				if (currentIndex == 0 && position == 0)// 0->1
				{
					lp.leftMargin = (int) (offset * (screenWidth * 1.0 / 2) + currentIndex
							* (screenWidth / 2));

				} else if (currentIndex == 1 && position == 0) // 1->0
				{
					lp.leftMargin = (int) (-(1 - offset)
							* (screenWidth * 1.0 / 2) + currentIndex
							* (screenWidth / 2));

				} 
				mTabLineIv.setLayoutParams(lp);
			}

			@Override
			public void onPageSelected(int position) {
				resetTextView();
				switch (position) {
				case 0:
					localTxt.setTextColor(Color.BLUE);
					break;
				case 1:
					cloudTxt.setTextColor(Color.BLUE);
					break;
				
				}
				currentIndex = position;
			}
		});
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        Fragment fragment = FragmentFactory.getInstanceByIndex(1);
//        transaction.replace(R.id.trail_content, fragment);
//        transaction.commit();
        //localradio.setTextColor(Color.parseColor("#0000ff"));
       /* 
        search.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        });*/
	}
	/**
	 * ���û������Ŀ��Ϊ��Ļ��1/3(����Tab�ĸ�������)
	 */
	private void initTabLineWidth() {
		DisplayMetrics dpMetrics = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay()
				.getMetrics(dpMetrics);
		screenWidth = dpMetrics.widthPixels;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
				.getLayoutParams();
		lp.width = screenWidth / 2;
		mTabLineIv.setLayoutParams(lp);
	}

	/**
	 * ������ɫ
	 */
	private void resetTextView() {
		cloudTxt.setTextColor(Color.BLACK);
		localTxt.setTextColor(Color.BLACK);
		
	}
//	 @Override
// 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
// 		if(isChecked){
// 			switch (buttonView.getId()) {
// 			case R.id.localradio:
// 				localradio.setTextColor(Color.parseColor("#0000ff"));
// 				cloudradio.setTextColor(Color.parseColor("#000000"));
// 				localradio.setBackgroundResource(R.drawable.trail_tab1_pressed);
// 				cloudradio.setBackgroundResource(R.drawable.trail_tab2_normal);
// 				
// 				break;
// 			case R.id.cloudradio:
// 				cloudradio.setTextColor(Color.parseColor("#0000ff"));
// 				localradio.setTextColor(Color.parseColor("#000000"));
// 				localradio.setBackgroundResource(R.drawable.trail_tab1_normal);
// 				cloudradio.setBackgroundResource(R.drawable.trail_tab2_pressed);
// 				
// 			}
// 		}
//     }
//	public static class FragmentFactory {
//	    public static Fragment getInstanceByIndex(int index) {
//	        Fragment fragment = null;
//	        switch (index) {
//	            case 1:
//	                fragment = new LocalTrailFragment();
//	                break;
//	            case 2:
//	                fragment = new CloudTrailFragment();
//	                break;
//	           
//	        }
//	        return fragment;
//	    }
//	}
	
}
