package com.trackersurvey.happynavi;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.trackersurvey.adapter.FragmentAdapter;
import com.trackersurvey.adapter.ListBaseAdapter;
import com.trackersurvey.fragment.ShowPoiFragment;
import com.trackersurvey.fragment.ShowTraceFragment;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.helper.ViewPagerCompat;
import com.trackersurvey.model.MyCommentModel;
import com.trackersurvey.photoview.SlideListView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DrawPath extends FragmentActivity implements OnClickListener, OnTouchListener {
	private MyLinearLayout back;
	private TextView title; // 顶部文本
	private Button titleRightBtn; // 顶部确认按钮
	private ViewPagerCompat groupPager;
	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	private FragmentAdapter mFragmentAdapter;
	private ShowTraceFragment tracePage;
	private ShowPoiFragment poiPage;
	private TextView traceTxt,poiTxt;
	private LinearLayout traceLayout,poiLayout;//手动点击切换fragment
	private ImageView mTabLineIv;
	private int currentIndex;
	private int screenWidth;
	private PopupWindow mPopupWindow;
	private ListBaseAdapter listAdapter;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//SDKInitializer.initialize(this.getApplicationContext());
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.pathshow);
		
//		Resources resources = getResources();
//		Configuration configure = resources.getConfiguration();
//		DisplayMetrics dm = resources.getDisplayMetrics();
//		if(TabHost_Main.l==0){
//			configure.locale = Locale.CHINESE;
//		}
//		if(TabHost_Main.l==1){
//			configure.locale = Locale.ENGLISH;
//		}
//		resources.updateConfiguration(configure, dm);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		
		
		initView();
		initTabLineWidth();
		
		
		
	}
	@SuppressWarnings("deprecation")
	public void initView(){

		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getString(R.string.seetrace));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		titleRightBtn.setBackgroundResource(R.drawable.bg_menu);
		titleRightBtn.setText("");
		titleRightBtn.setOnClickListener(this);
		
		traceTxt = (TextView) findViewById(R.id.tv_showpath);
		poiTxt = (TextView) findViewById(R.id.tv_poi);
		traceLayout = (LinearLayout) findViewById(R.id.id_tab_showpath);
		poiLayout = (LinearLayout) findViewById(R.id.id_tab_poi);
		mTabLineIv = (ImageView) findViewById(R.id.tab_line_iv);
		groupPager = (ViewPagerCompat) findViewById(R.id.viewpager_showpath);
		tracePage= new ShowTraceFragment();
        poiPage= new ShowPoiFragment();
        mFragmentList.add(tracePage);
        mFragmentList.add(poiPage);
        mFragmentAdapter = new FragmentAdapter(
				this.getSupportFragmentManager(), mFragmentList);
        groupPager.setAdapter(mFragmentAdapter);
        groupPager.setCurrentItem(0);
        traceLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				groupPager.setCurrentItem(0);
			}
		});
        poiLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				groupPager.setCurrentItem(1);
			}
		});
        groupPager.setOnPageChangeListener(new OnPageChangeListener() {

			/**
			 * state滑动中的状态 有三种状态（0，1，2） 1：正在滑动 2：滑动完毕 0：什么都没做。
			 */
			@Override
			public void onPageScrollStateChanged(int state) {

			}

			/**
			 * position :当前页面，及你点击滑动的页面 offset:当前页面偏移的百分比
			 * offsetPixels:当前页面偏移的像素位置
			 */
			@Override
			public void onPageScrolled(int position, float offset,
					int offsetPixels) {
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabLineIv
						.getLayoutParams();

				//Log.e("offset:", offset + "");
				/**
				 * 利用currentIndex(当前所在页面)和position(下一个页面)以及offset来
				 * 设置mTabLineIv的左边距 滑动场景：
				 * 记3个页面,
				 * 从左到右分别为0,1,2 
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
					traceTxt.setTextColor(Color.BLUE);
					break;
				case 1:
					poiTxt.setTextColor(Color.BLUE);
					break;
				
				}
				currentIndex = position;
			}
		});
	}
	/**
	 * 设置滑动条的宽度为屏幕的1/3(根据Tab的个数而定)
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
	 * 重置颜色
	 */
	private void resetTextView() {
		traceTxt.setTextColor(Color.BLACK);
		poiTxt.setTextColor(Color.BLACK);
		
	}
		
	
	
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		//setmapvisibility();
		super.onResume();
		
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		//mapView.setVisibility(View.GONE);加上这两句会黑屏
		//bdMapView.setVisibility(View.GONE);
		super.onPause();
		
	}

	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_back: {
			finish();
			break;
		}
		
		case R.id.header_right_btn:
			if (mPopupWindow == null) {
				initPopupWindow();
			}
			if (!mPopupWindow.isShowing()) {
				mPopupWindow.showAsDropDown(titleRightBtn);
				mPopupWindow.update();
				// backgroundAlpha(0.7f);
			}
			break;
		
		case R.id.share_wxsession:{//分享到微信好友uid=13969553872&tid=1460716974411
			
			tracePage.uploadBeforeShare(false);
			break;
		}
		case R.id.share_wxtinmeline:{//分享到朋友圈
			
			tracePage.uploadBeforeShare(true);
			break;
		}
		
			
		default:
			break;
		}
	}
	private void initPopupWindow() {
		View contentView = LayoutInflater.from(DrawPath.this).inflate(R.layout.mark_menu, null);
		mPopupWindow = new PopupWindow(contentView);
		mPopupWindow.setWidth(400);
		mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		ListView lv_menu = (ListView) contentView.findViewById(R.id.lv_menu);
		// 准备listview的数据
		List<Map<String, Object>> menu_data = new ArrayList<Map<String, Object>>();

//		int[] menu_image = new int[] {  R.drawable.menu_delete, R.drawable.menu_share,
//				R.drawable.menu_refresh };
		int[] menu_image = new int[] {  R.drawable.menu_delete, R.drawable.menu_refresh };

		
		Map<String, Object> map2 = new HashMap<String, Object>();
		String[] menuArray = getResources().getStringArray(R.array.popmenu);
		map2.put("image", menu_image[0]);
		map2.put("item", menuArray[0]);
		Map<String, Object> map3 = new HashMap<String, Object>();
		map3.put("image", menu_image[1]);
		map3.put("item", menuArray[1]);
//		Map<String, Object> map4 = new HashMap<String, Object>();
//		map4.put("image", menu_image[2]);
//		map4.put("item", menuArray[2]);

		
		menu_data.add(map2);
		menu_data.add(map3);
//		menu_data.add(map4);

		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), menu_data, R.layout.mark_menu_items,
				new String[] { "image", "item" }, new int[] { R.id.iv_menu_item, R.id.tv_menu_item });
		lv_menu.setAdapter(adapter);

		lv_menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				
				case 0:

					mPopupWindow.dismiss();
					CustomDialog.Builder builder = new CustomDialog.Builder(DrawPath.this);
					builder.setTitle(getResources().getString(R.string.tip));
					builder.setMessage(getResources().getString(R.string.tips_deletedlgmsg_trace1));
					builder.setNegativeButton(getResources().getString(R.string.cancl),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					builder.setPositiveButton(getResources().getString(R.string.confirm),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							tracePage.deleteTrace();
						
							dialog.dismiss();
						}
					});
					builder.create().show();
					break;
				/*case 1:

					mPopupWindow.dismiss();
					if(!Common.isNetConnected){
						ToastUtil.show(DrawPath.this, getResources().getString(R.string.tips_share_nonet1));
						return;
					}
					Dialog dialog = new Dialog(DrawPath.this);
					dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#09c7f7")));
					dialog.setTitle(R.string.share_to);
					View contentView = LayoutInflater.from(DrawPath.this).inflate(
							R.layout.sharetowx, null);
					dialog.setContentView(contentView);
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();

					ImageButton shareSession = (ImageButton) contentView
							.findViewById(R.id.share_wxsession);
					shareSession.setOnClickListener(DrawPath.this);
					shareSession.setOnTouchListener(DrawPath.this);
					ImageButton shareTimeline = (ImageButton) contentView
							.findViewById(R.id.share_wxtinmeline);
					shareTimeline.setOnClickListener(DrawPath.this);
					shareTimeline.setOnTouchListener(DrawPath.this);
					break;*/
				case 1:

					mPopupWindow.dismiss();
					tracePage.refreshMarker();
					poiPage.updateUI();
					break;
				

				default:
					break;
				}
			}
		});

		mPopupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mPopupWindow.dismiss();
					return true;
				}
				return false;
			}
		});
	}
	
	


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			v.setBackgroundColor(Color.parseColor("#33ccff"));
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			v.setBackgroundColor(Color.WHITE);
		}
		return false;
	}

	
}

