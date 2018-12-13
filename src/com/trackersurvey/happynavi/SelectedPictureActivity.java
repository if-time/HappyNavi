/**
 * 
 */
package com.trackersurvey.happynavi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import com.trackersurvey.entity.CommentMediaFiles;
import com.trackersurvey.entity.ListItemData;
import com.trackersurvey.happynavi.SelectedPictureActivity.SamplePagerAdapter.OnBitmapNull;
import com.trackersurvey.happynavi.SelectedPictureActivity2.SamplePagerAdapter;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.model.MyCommentModel;
import com.trackersurvey.model.MyCommentModel.DownFileListener;
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
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 显示一张图片
 * @author Eaa
 * @version 2015年12月11日  下午2:11:08
 */
public class SelectedPictureActivity extends Activity {
	public static final String PIC_ID = "id";
	public static final String PIC_PATH = "path";
	public static final String THUMB_PATH = "thumbpath";
	public static final String PIC_POSITION = "pisition";
	public static final String THUMB_POSITION = "thumbposition";
	
	private ArrayList<HashMap<String, Object>> commentItems;
	private ArrayList<String> items;
	private ArrayList<String> thumbItems;
	private ArrayList<String> imagePath;
	private ArrayList<String> thumbImagePath;
	private ArrayList<String> comment, time;
	private ArrayList<Integer> feeling;
	private LinearLayout back;
	private TextView titleText; // 顶部TextView
	private Button titleButton; // 顶部确认按钮
	private String ttext; //顶部文字
	private String failed = null;
	private  int currentPosition = 0;
	private ViewPager mViewPager;
	private TextView tv_interest, tv_time;
	private ImageView iv_feeling;
	private Intent intent;
//	private Context context;
	private MyCommentModel myComment;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.gallery_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		AppManager.getAppManager().addActivity(this);
		mViewPager = (HackyViewPager)findViewById(R.id.viewPager);
		tv_interest = (TextView) findViewById(R.id.tv_interest);
		tv_time = (TextView) findViewById(R.id.tv_time);
		iv_feeling = (ImageView) findViewById(R.id.iv_feeling);
		
		myComment = new MyCommentModel(this, "album");
		myComment.setmDownFile(fileDownloaded);
		commentItems = myComment.getItems();
		
		intent = getIntent();
		failed = intent.getStringExtra("failed");
		//Log.i("bitmap","imagePath="+imagePath);
		items = new ArrayList<String>();
		thumbItems = new ArrayList<String>();
		if (intent.hasExtra(PIC_PATH)) {
			imagePath = intent.getStringArrayListExtra(PIC_PATH);
			Log.i("bitmap", "imagePath=" + imagePath);
			int size = imagePath.size();
			for (int i = 0; i < size; i++) {
				items.add(imagePath.get(i));
			}
		}
		if (intent.hasExtra(THUMB_PATH)) {
			thumbImagePath = intent.getStringArrayListExtra(THUMB_PATH);
			Log.i("bitmapthumb", "thumbPath=" + thumbImagePath);
			int size = thumbImagePath.size();
			for (int i = 0; i < size; i++) {
				thumbItems.add(thumbImagePath.get(i));
			}
		}
		Log.i("thumbbb", ""+thumbItems+"failed="+failed);
		
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
//		SamplePagerAdapter spa_thumb = new SamplePagerAdapter(thumbItems);
//		spa_thumb.notifyDataSetChanged();
		spa.setOnBitmapNull(new OnBitmapNull() {
			@Override
			public void isNull() {
				// TODO Auto-generated method stub
				cantDisplayImg();
			}
		});
//		if(spa.getCount()<spa_thumb.getCount()){
//			mViewPager.setAdapter(spa_thumb);
//			
//		}else{
//			mViewPager.setAdapter(spa);
//		}
		//如果本地没有原图，在这里下载原图
//		if(failed!=null){
//			
//			Log.i("commentItems", "commentItems:"+commentItems);
//			int position = intent.getIntExtra(PIC_POSITION, 0);
//			int listPosition = intent.getIntExtra("listPosition", 0);
//			CommentMediaFiles[] lid = ((ListItemData)commentItems.get(listPosition).get("listItem")).getFiles();
//			int clickedType = lid[position].getType();
//			ArrayList<String> pathes = new ArrayList<String>();
//			for(int i = 0;i<lid.length;i++){
//				int type = lid[i].getType();
//				
//				//ProgressBar pb = (ProgressBar) findViewById(R.id.down_img_pb);
//				//pb.setVisibility(View.VISIBLE);
//				
//				downFile(listPosition, i, type);
//				
//				//pb.setClickable(false);
//				
//				if(type==clickedType){
//					String pathName = lid[i].getFileName();
//					pathes.add(pathName);
//				}
//				if(pathes.get(i)!=null){
//					thumbItems.set(i, pathes.get(i));
//				}
//			}
//			Log.i("commentItems", "pathes:"+pathes);
//			spa_thumb.notifyDataSetChanged();
//			//imagePath = pathes;
//			spa_thumb = new SamplePagerAdapter(thumbItems);
//			mViewPager.setAdapter(spa_thumb);
//		}
		//如果某原图下载失败，就插入其缩略图
		if(failed!=null){
			for(int i = 0;i<thumbItems.size();i++){
				if(items.get(i)==null){
					items.set(i, thumbItems.get(i));
				}
			}
			Log.i("thumbItems", "thumbItems:"+thumbItems.toString());
			ToastUtil.show(SelectedPictureActivity.this, getResources().getString(R.string.picture_failed));
		}
		mViewPager.setAdapter(spa);
		if(intent.hasExtra(PIC_POSITION)){
			int position = intent.getIntExtra(PIC_POSITION, 0);
			
			//将当前item中的评论字符串传递到这里
			comment = new ArrayList<String>();
			time = new ArrayList<String>();
			feeling = new ArrayList<Integer>();
			comment = intent.getStringArrayListExtra("tv_comment");
			time = intent.getStringArrayListExtra("time");
			feeling = intent.getIntegerArrayListExtra("feeling");
			try {
				tv_interest.setText(comment.get(position));
				tv_time.setText(time.get(position));
				switch (feeling.get(position)) {
				case 0:
					iv_feeling.setImageResource(R.drawable.happy);
					break;
				case 1:
					iv_feeling.setImageResource(R.drawable.general);
					break;
				case 2:
					iv_feeling.setImageResource(R.drawable.unhappy);
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mViewPager.setCurrentItem(position);
			currentPosition  = position;
		}
		
		//滑动后更改顶栏提示数字
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				Log.i("bitmap", "position"+arg0);
//				if(items.size()!=0){
//					ttext = (arg0+1)+"/"+items.size();					
//				}else{
//					ttext = (arg0+1)+"/"+thumbItems.size();
//				}
				ttext = (arg0+1)+"/"+items.size();
//				ttext = time.get(arg0);
				setTitle(ttext);
				//滑动后显示对应的评论
				try {
					tv_interest.setText(comment.get(arg0));
					tv_time.setText(time.get(arg0));
					switch (feeling.get(arg0)) {
					case 0:
						iv_feeling.setImageResource(R.drawable.happy);
						break;
					case 1:
						iv_feeling.setImageResource(R.drawable.general);
						break;
					case 2:
						iv_feeling.setImageResource(R.drawable.unhappy);
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
//		if(failed==null){
//			ttext = (currentPosition+1)+"/"+items.size();			
//		}else{
//			ttext = (currentPosition+1)+"/"+thumbItems.size();
//		}
		ttext = (currentPosition+1)+"/"+items.size();
		//ttext = time.get(currentPosition);
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
		 AlertDialog.Builder builder = new AlertDialog.Builder(SelectedPictureActivity.this);
		 builder.setTitle(R.string.tip);
		 builder.setMessage(R.string.cantdisplayimg);
		 builder.setIcon(R.drawable.advice);
		 builder.setPositiveButton(R.string.confirm, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				SelectedPictureActivity.this.finish();
			}
		});
		 builder.create().show();
	 }
	 
	 private void downFile(int listPosition, int filePosition, int type){
		 
		 myComment.downloadFile(listPosition, filePosition, type);
	 }
	 
	 DownFileListener fileDownloaded = new DownFileListener() {
		
		@Override
		public void onFileDownload(int msg, int listPosition, int filePosition) {
			// TODO Auto-generated method stub
			if (msg != 0) {
				modelTips(msg);
			}
			
		}
	};
	 
	
	 /**
		 * 根据Model操作的回调，弹出Toast提醒用户
		 */
		private void modelTips(int msg) {
			switch (msg) {
			case -2:
				Toast.makeText(this,
						R.string.tips_postfail,
						Toast.LENGTH_SHORT).show();
				break;
			case -1:
				Toast.makeText(this,
						R.string.tips_postfail,
						Toast.LENGTH_SHORT).show();
				break;
			case 1:// 获取数据不成功但连接了
					// Log.i("Eaa", "获取云端评论时提示" + "服务器忙，请稍后再试");
				Toast.makeText(this,
						R.string.tips_postfail,
						Toast.LENGTH_SHORT).show();
				break;
			case 8:
				// 查询有误
				Toast.makeText(this,
						R.string.tips_postfail,
						Toast.LENGTH_SHORT).show();
				break;
			case 10:// 连接失败
				Toast.makeText(
						this,
						R.string.tips_netdisconnect, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}
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
