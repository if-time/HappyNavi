package com.trackersurvey.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.trackersurvey.adapter.GroupAdapter;
import com.trackersurvey.happynavi.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created by ZY on 2016/5/30.
 */
public class ImageLoader {
    private ListView mListView;
    private ImageView mImageView;
    private String mUrl;
    private LruCache<String,Bitmap> mCaches;
    private Set<BmpAsyncTask> mTasks; //������ڼ��ص��첽����
    public ImageLoader(){
    	int maxMemory = (int) Runtime.getRuntime().maxMemory();//��ȡӦ������ʹ�õ�����ڴ�
        int cacheSize = maxMemory/4;
        mCaches = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {//ÿ�δ��뻺��ʱ���ø÷���
                return value.getByteCount();
            }
        };
        
        mTasks = new HashSet<BmpAsyncTask>();
    }
    public ImageLoader(ListView listView){
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//��ȡӦ������ʹ�õ�����ڴ�
        int cacheSize = maxMemory/4;
        mCaches = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {//ÿ�δ��뻺��ʱ���ø÷���
                return value.getByteCount();
            }
        };
        mListView = listView;
        mTasks = new HashSet<BmpAsyncTask>();
    }
    //���ͼƬ���ڴ滺��
    public void addBmpToCache(String url,Bitmap bmp){
        if(getBmpFromCache(url)==null) {
            mCaches.put(url, bmp);
        }
    }
    //�ӻ����л�ȡͼƬ
    public Bitmap getBmpFromCache(String url){
        return mCaches.get(url);
    }
    //��SD���л�ȡͼƬ
    public Bitmap getBmpFromSDCard(String filename){

        return BitmapFactory.decodeFile(Common.GROUPHEAD_PATH+filename);
    }
    private Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl)) {//��ֹlistview����convertview���õ��µ�ͼƬ����,ԭ��ʱ�����⣬���ܺ�һ��ͼƬ����һ��ͼƬ�ȼ����꣬������
                //������Ϣ������mimageview��bmp����Ӧ��
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };
    public void showImageByThread(ImageView imageView,final String url){
        mImageView=imageView;
        mUrl=url;
        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bmp = getBmpFromUrl(url);
//                try {
//                    Thread.sleep(1000);//ģ�������ӳ٣����ڷ���ͼƬ��������
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                Message msg = Message.obtain();
                msg.obj=bmp;
                mHandler.sendMessage(msg);
            }
        }.start();
    }
    //ͨ���첽�������ͼƬ
    public void showImageByAsyncTask(ImageView imageView,final String url){

        Bitmap bitmap;
        bitmap = getBmpFromCache(url);//�ȴӻ����л�ȡ������У�ֱ���ã�û�У��첽����

        if(bitmap==null) {
            new BmpAsyncTask(imageView,url).execute(url);
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }
    //��adapter�е�getviewʹ�ã����������и�ͼƬʱ��ʹ��Ĭ��ͼƬ��������ֻ���ʱ���ڲ�ִ��loadImagesNonScroll���µ��Ѽ��ص�ͼƬȴ����ʾ
    public void showImageIfExist(ImageView imageView,final String url){

        Bitmap bitmap;
        bitmap = getBmpFromCache(url);//�ȴӻ����л�ȡ������У�ֱ���ã�û�У�ʹ��Ĭ��ͼƬ

        if(bitmap==null) {
            imageView.setImageResource(R.drawable.ic_group);
        }else{
            imageView.setImageBitmap(bitmap);
        }
    }
    //������������ô˷��������ؿɼ���item
    public void loadImagesNonScroll(int start, int end){
        for(int i = start;i<end;i++){
            Bitmap bitmap;
            String url = GroupAdapter.URLArray[i];
            String name = GroupAdapter.IconName[i];
            bitmap = getBmpFromCache(url);//�ȴӻ����л�ȡ������У�ֱ���ã�û�У���sd������

            if(bitmap==null) {
                bitmap = getBmpFromSDCard(name);
                if(bitmap == null) {//sd����Ҳû�У��������ȡ
                    BmpAsyncTask task = new BmpAsyncTask(url, name);
                    task.execute(url);
                    mTasks.add(task);
                }else{
                    Log.i("mylog", "get from sd card" );
                    addBmpToCache(url,bitmap);
                    ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                    imageView.setImageBitmap(bitmap);
                }
            }else{
                Log.i("mylog", "get from cache" );
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    //�������� ���ص���ͼƬ
    public void loadOneImage(ImageView imageView,final String url,String name){
    	Bitmap bitmap;
        
       
        bitmap = getBmpFromCache(url);//�ȴӻ����л�ȡ������У�ֱ���ã�û�У���sd������

        if(bitmap==null) {
            bitmap = getBmpFromSDCard(name);
            if(bitmap == null) {//sd����Ҳû�У��������ȡ
                BmpAsyncTask task = new BmpAsyncTask(imageView,url,false);
                task.execute(url);
                mTasks.add(task);
            }else{
                Log.i("mylog", "get from sd card" );
                addBmpToCache(url,bitmap);
                
                imageView.setImageBitmap(bitmap);
            }
        }else{
            Log.i("mylog", "get from cache" );
            
            imageView.setImageBitmap(bitmap);
        }
    }
    //����ʱȡ�������첽����
    public void cancelAllTasks(){
        if(mTasks != null){
            for(BmpAsyncTask task : mTasks){
                task.cancel(false);
            }
        }
    }
    public class BmpAsyncTask extends AsyncTask<String,Void,Bitmap>{
        //����ʹ���ⲿmImageView��mUrl��
        private ImageView mImageView;
        private String mUrl;
        private String mIconName;
        private boolean isScrollTask;
        private boolean isFromList = true;
        public BmpAsyncTask(ImageView imageView,final String url){
            mImageView=imageView;
            mUrl=url;
            isScrollTask=false;
        }
        public BmpAsyncTask(ImageView imageView,final String url,boolean isFromList){
            mImageView=imageView;
            mUrl=url;
            isScrollTask=false;
            this.isFromList=isFromList;
        }
        public BmpAsyncTask(final String url,final String name){
            mUrl=url;
            mIconName = name;
            isScrollTask=true;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap=getBmpFromUrl(params[0]);
            if(bitmap!=null){
                addBmpToCache(params[0],bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(isScrollTask){
                ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
                if(imageView!=null&&bitmap!=null) {
                    imageView.setImageBitmap(bitmap);
                    try {
                        saveFile(bitmap,mIconName);
                        Log.i("mylog","save "+mIconName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("mylog", "save error" );
                    }
                }
                mTasks.remove(this);
            }else {
            	if(isFromList){
	                if (mImageView.getTag().equals(mUrl)&&bitmap!=null) {
	                    mImageView.setImageBitmap(bitmap);
	                }
            	}else{
            		if(bitmap != null){
            			mImageView.setImageBitmap(bitmap);
            		}
            	}
            }
        }
    }
    public Bitmap getBmpFromUrl(String url){
        Bitmap bmp=null;
        InputStream is=null;
        try {
            is = new URL(url).openStream();
            bmp= BitmapFactory.decodeStream(is);
            return  bmp;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(is!=null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return  null;
    }
    public  void saveFile(Bitmap bmp,String fileName)throws IOException{
    	   
        File myBmpFile = new File(Common.GROUPHEAD_PATH+fileName);
        
        BufferedOutputStream  bos = new BufferedOutputStream(new FileOutputStream(myBmpFile));
        bmp.compress(Bitmap.CompressFormat.JPEG,80,bos);
        bos.flush();
        bos.close();
    }
}

