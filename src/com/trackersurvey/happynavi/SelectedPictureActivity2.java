/**
 * 
 */
package com.trackersurvey.happynavi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import com.trackersurvey.adapter.ListBaseAdapter;
import com.trackersurvey.happynavi.SelectedPictureActivity2.SamplePagerAdapter.OnBitmapNull;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.photoview.HackyViewPager;
import com.trackersurvey.photoview.PhotoView;
import com.trackersurvey.photoview.SelectedTreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 显示一张图片
 * @author Eaa
 * @version 2015年12月11日  下午2:11:08
 */
public class SelectedPictureActivity2 extends Activity {
	public static final String PIC_ID = "id";
	public static final String PIC_PATH = "path";
	public static final String THUMB_PATH = "thumbpath";
	public static final String PIC_POSITION = "pisition";
	public static final String THUMB_POSITION = "thumbposition";
	
	private ArrayList<String> items;
	private ArrayList<String> imagePath;
	private ArrayList<String> comment, time;
	private ArrayList<Integer> feeling;
	private LinearLayout back;
	private TextView titleText; // 顶部TextView
	private Button titleButton; // 顶部确认按钮
	private String ttext; //顶部文字
	private  int currentPosition = 0;
	private ViewPager mViewPager;
	private Intent intent,intent2;
	private Context context;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.gallery_layout2);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		mViewPager = (HackyViewPager)findViewById(R.id.viewPager);
		
		intent = getIntent();
		
		//Log.i("bitmap","imagePath="+imagePath);
		items = new ArrayList<String>();
		if (intent.hasExtra(PIC_PATH)) {
			imagePath = intent.getStringArrayListExtra(PIC_PATH);
			Log.i("bitmap", "imagePath=" + imagePath);
			int size = imagePath.size();
			for (int i = 0; i < size; i++) {
				items.add(imagePath.get(i));
			}
		}
		
		if (intent.hasExtra(PIC_ID)) {
			SelectedTreeMap stm = intent.getParcelableExtra(PIC_ID);
			TreeMap<Long, Uri> selectTree = stm.getTreeMap();
			Set<Long> set = selectTree.keySet();
			for (Long key : set) {
				Uri uri = selectTree.get(key);
				String[] projection = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(uri, projection, null,
						null, null);
				cursor.moveToFirst();
				String picPath = cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();
				items.add(picPath);
			}
		}
		mViewPager.setBackgroundColor(Color.BLACK);
		SamplePagerAdapter spa = new SamplePagerAdapter(items);
		spa.setOnBitmapNull(new OnBitmapNull() {
			@Override
			public void isNull() {
				// TODO Auto-generated method stub
				cantDisplayImg();
			}
		});
		mViewPager.setAdapter(spa);
		if(intent.hasExtra(PIC_POSITION)){
			int position = intent.getIntExtra(PIC_POSITION, 0);
			
			mViewPager.setCurrentItem(position);
			currentPosition  = position;
		}
		
		//滑动后更改顶栏提示数字
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				Log.i("bitmap", "position"+arg0);
				ttext = (arg0+1)+"/"+items.size();
				setTitle(ttext);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		back=(LinearLayout)findViewById(R.id.title_back);
		back.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
		// 顶部标题
		ttext = (currentPosition+1)+"/"+items.size();
		titleText = (TextView) findViewById(R.id.header_text);
		titleText.setTextColor(Color.WHITE);
		titleText.setText(ttext);
		// 右部按钮不可见
		titleButton = (Button) findViewById(R.id.header_right_btn);
		titleButton.setVisibility(View.INVISIBLE);

	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	@Override
	protected void onDestroy() {
		int ccount = mViewPager.getChildCount();
		for(int i=0;i<ccount;i++){
			((PhotoView)mViewPager.getChildAt(i)).recycleBm();
		}
		mViewPager = null;
		//Log.d("Eaa","onDestory" );
		super.onDestroy();
	}
	
	/**
	 * 设置顶部文字图片位置提示
	 * @param text
	 */
	protected  void setTitle(String text) {
		titleText.setText(text);
	}
	
	
	 /**
	  * 无法显示照片的提示
	  * @param context
	  */
	 public void cantDisplayImg(){
		 AlertDialog.Builder builder = new AlertDialog.Builder(SelectedPictureActivity2.this);
		 builder.setTitle(R.string.tip);
		 builder.setMessage(R.string.cantdisplayimg);
		 builder.setIcon(R.drawable.advice);
		 builder.setPositiveButton(R.string.confirm, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				SelectedPictureActivity2.this.finish();
			}
		});
		 builder.create().show();
	 }
	
	static class SamplePagerAdapter extends PagerAdapter {
		private ArrayList<String> items;
		private int scale = 1; 
		private int bitmapWidth;
		private int bitmapHeight;
		private OnBitmapNull bitmapNull;
		
		public SamplePagerAdapter(ArrayList<String> pathes){
			this.items = pathes;
		}
		
		public void setScale(int scale) {
			this.scale = scale;
		}
		
		@Override
		public int getCount() {
			return items.size();
		}
		
		interface OnBitmapNull{
			void isNull();
		}
		
		public void setOnBitmapNull(OnBitmapNull bitmapNull){
			this.bitmapNull = bitmapNull;
		}
		
 
		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final String path = items.get(position);
			final PhotoView photoView = new PhotoView(container.getContext());
			photoView.setBackgroundColor(Color.BLACK);
			photoView.setBackgroundColor(Color.BLACK);
			
			Options opts =new  BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			Bitmap mbmp = BitmapFactory.decodeFile(path,opts);
			/*设置inJustDecodeBounds为true后，decodeFile并不分配空间，但可计算出原始图片的长度和宽度，即opts.width和opts.height。
			 * */
		
			final int imgWidth = opts.outWidth;
			final int imgHeight = opts.outHeight;
			Log.i("bitmap","Widht="+imgWidth+" Height="+imgHeight);
			int scaleX = 1;
			int scaleY = 1;
			try{
			 scaleX  = imgWidth/Common.decodeImgWidth;  
             scaleY  = imgHeight/Common.decodeImgHeight;  
             Log.i("bitmap","scaleX="+scaleX+" scaleY="+scaleY);
             if(scaleX>=scaleY && scaleX>1){  
                 scale = scaleX;  
             }  
             if(scaleY>scaleX && scaleY>1){  
                 scale = scaleY;  
             }  
             
             if(scale>5) {
             	scale =5;
             }
			}catch(ArithmeticException e){
				scale = 5;
			}
            
            opts.inSampleSize = scale;
            opts.inJustDecodeBounds = false;
            Log.i("bitmap","scale="+scale);
            try{
            	mbmp = BitmapFactory.decodeFile(path,opts);
            }catch(OutOfMemoryError e){
            	
            }
            if(null == mbmp){
            	this.bitmapNull.isNull();
            	return photoView;
            }
            bitmapWidth = mbmp.getWidth();
            bitmapHeight = mbmp.getHeight();
            
        	photoView.setImageBitmap(mbmp);
			
			photoView.setOnMatrixChangeListener(new com.trackersurvey.photoview.PhotoViewAttacher.OnMatrixChangedListener() {
				
				@Override
				public void onMatrixChanged(RectF rect) {
					// TODO Auto-generated method stub
					float rectWidth  = rect.right -rect.left;
					float rectHeight = rect.bottom-rect.top;
					//Log.i("Eaa_gap", "rectW="+rectWidth+"|rectH="+rectHeight);
					//Log.i("Eaa_point",rect.left+"|"+rect.right+"|"+rect.top+"|"+rect.bottom);
					
					if(rectWidth>imgWidth/scale*2 &&rectHeight>imgHeight/scale*2){
						if(scale>3){
							Log.e("bitmap","scale="+scale);
							float zoom = (float)rectWidth/bitmapWidth;
							System.out.println(zoom);
							setScale(scale-1);
							setBitmap(photoView, zoom, path,rect.centerX(),rect.centerY());
						}
					}
				}
			});
				
			
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			return photoView;
		}
		
		
		

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((PhotoView)object).recycleBm();
			container.removeView((View) object);
			 Log.i("bitmap","destroyItem pos="+position);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		void setBitmap(PhotoView pv,float zoom,String path,float centX,float centY){
			Options opts = new BitmapFactory.Options();
			opts.inSampleSize = this.scale;
			Bitmap mbmp = null;
			try{
				 mbmp = BitmapFactory.decodeFile(path,opts);
			}catch(OutOfMemoryError e){
            	
            }
			if(null == mbmp){
				this.bitmapNull.isNull();
				return;
			}
			Log.i("bitmap","setbitmap Widht="+mbmp.getWidth()+" Height="+mbmp.getHeight());
			pv.recycleBm();
			bitmapWidth = mbmp.getWidth();
			bitmapHeight = mbmp.getHeight();
			pv.setImageBitmap(mbmp);
			pv.setTo(zoom*scale, centX,centY);
		}
		
	}
}
