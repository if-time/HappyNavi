package com.trackersurvey.photoview;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.channels.ClosedByInterruptException;
import java.util.HashMap;
import java.util.Iterator;

import com.trackersurvey.helper.Common;
import com.trackersurvey.happynavi.PictureBrowser;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

/**
 * �ֻ�����ͼƬ�첽���ش����� ͼƬ�ļ�������Ӱ��ܴ�ʹ�������ú������� ����ͼƬ���ӿ���Ӧ���ٶȣ�������ܡ�
 */
public class ImageWorker {

	// ���ֵ���õ��Ǽ���ͼƬ�Ķ���Ч���ļ��ʱ�䣬�ﵽ�������Ե�Ч��
	private static final int FADE_IN_TIME = 10;

	private boolean mExitTasksEarly = false;// �ж�ͼƬ���������Ƿ���ǰ�˳�
	private boolean mPauseWork = false;// ����ͼƬ�߳��Ƿ����
	private final Object mPauseWorkLock = new Object();// �����������������Ϊ���ж��Ƿ����ͼƬ�ļ���

	private final int colWidth = (Common.winWidth-8)/3;
	
	protected final Resources mResources;
	private final ContentResolver mContentResolver;// ���ݽ�����
	private final BitmapFactory.Options mOptions;
	// ���ڻ���ͼƬ��ÿһ�������ͼƬ��Ӧһ��Long���͵�idֵ��SoftReference��Ӧ��ͼƬ��������
	private final HashMap<Long, SoftReference<BitmapDrawable>> bitmapCache = new HashMap<Long, SoftReference<BitmapDrawable>>();

	private Bitmap mLoadBitmap;// GridView��Ĭ�ϵı���ͼƬ

	// ������
	public ImageWorker(Context context) {
		this.mResources = context.getResources();
		this.mContentResolver = context.getContentResolver();
		mOptions = new BitmapFactory.Options();
		// ����ͼƬΪԭ����1/9��һ��Ӧ���м���ͼƬ�������ͼƬ�����ţ���ֹ�ڴ���������⡣
		mOptions.inSampleSize = 3;
	}

	/**
	 * ����ͼƬ
	 * 
	 * @param origId
	 *            ÿ������ͼƬ��Ӧһ��idֵ
	 * @param imageView
	 */
	public void loadImage(long origId, ImageView imageView) {
		BitmapDrawable bitmapDrawable = null;
		// �ȴӻ����м���ͼƬ������������У�����ͼƬ���ɡ�
		// ���������û�У������жϵ�ǰ�����Ƿ���ͣ��û����ͣ��ʹ��loadBitmapTask�첽�����̼߳���ͼƬ
		if (bitmapCache.containsKey(origId)) {
			bitmapDrawable = bitmapCache.get(origId).get();
		}
		if (bitmapDrawable != null) {
			imageView.setImageDrawable(bitmapDrawable);
		} else if (cancelPotentialWork(origId, imageView)) {
			final LoadBitmapTask loadBitmapTask = new LoadBitmapTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources,
					mLoadBitmap, loadBitmapTask);
			imageView.setImageDrawable(asyncDrawable);
			// SERIAL_EXECUTOR �����̣߳���֤�߳�˳������ִ��
			loadBitmapTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, origId);
		}
	}

	/**
	 * �����ṩ�����������GridView��ÿ��itemĬ�ϵ�ͼƬ
	 */
	public void setLoadBitmap(Bitmap mLoadBitmap) {
		this.mLoadBitmap = mLoadBitmap;
	}

	/**
	 * ����ͼƬ���� ����ͼƬ�������Ե�Ч��
	 *
	 * @param imageView
	 * @param drawable
	 */
	private void setImageDrawable(ImageView imageView, Drawable drawable) {
		final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
				new ColorDrawable(android.R.color.transparent), drawable });
		imageView.setImageDrawable(td);
		td.startTransition(FADE_IN_TIME);
	}

	/**
	 * ȡ�����������в�����ͣ������
	 *
	 * @param origId
	 * @param imageView
	 * @return
	 */
	private static boolean cancelPotentialWork(long origId, ImageView imageView) {
		final LoadBitmapTask loadBitmapTask = getBitmapWorkerTask(imageView);

		if (loadBitmapTask != null) {
			final long bitmapOrigId = loadBitmapTask.origId;
			if (bitmapOrigId == origId) {
				loadBitmapTask.cancel(true);
			} else {
				// The same work is already in progress.
				return false;
			}
		}
		return true;
	}
	public void clearCache(){
		if(bitmapCache != null){
			Iterator iter = bitmapCache.keySet().iterator();  
			while (iter.hasNext()) {  
				Object key = iter.next();  
				SoftReference<BitmapDrawable> softObj = bitmapCache.get(key);
				if(softObj != null){
					BitmapDrawable bitmapDrawable = softObj.get();
					if(bitmapDrawable != null){
						Bitmap bitmap = bitmapDrawable.getBitmap();
						if(bitmap != null){
							bitmap.recycle();  
						}
					}
				}
			}  
			Log.i("bitmap", "PictureBrowser  recyle:"+bitmapCache.size());
		}
	}
	/**
	 * ͼƬ�첽�����߳���-�����߳�
	 */
	private class LoadBitmapTask extends AsyncTask<Long, Void, BitmapDrawable> {
		private long origId;
		// ָ��Imageview�������ã���ͼƬ������HashMap<Long,
		// SoftReference<BitmapDrawable>>
		// bitmapCache�С�
		private WeakReference<ImageView> imageViewReference;

		public LoadBitmapTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected BitmapDrawable doInBackground(Long... params) {
			origId = params[0];
			Bitmap bitmap = null;
			BitmapDrawable drawable = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			if (bitmapCache != null && !isCancelled()
					&& getAttachedImageView() != null & !mExitTasksEarly) {
				// �����Ǹ���ͼƬ��idֵ��ѯ�ֻ����ص�ͼƬ����ȡͼƬ������ͼ��MICRO_KIND ����96 x 96��С��ͼƬ
				try{	
					bitmap = MediaStore.Images.Thumbnails.getThumbnail(
						mContentResolver, origId,
						MediaStore.Images.Thumbnails.MINI_KIND, mOptions);
				
					bitmap = ThumbnailUtils.extractThumbnail(bitmap,colWidth, colWidth);
				}catch(OutOfMemoryError e){
	            	e.printStackTrace();
	            }catch(Exception e){
	            	e.printStackTrace();
	            }
			}

			if (bitmap != null) {
				drawable = new BitmapDrawable(mResources, bitmap);
				bitmapCache.put(origId, new SoftReference<BitmapDrawable>(
						drawable));
			}
			return drawable;
		}

		@Override
		protected void onPostExecute(BitmapDrawable drawable) {
			if (isCancelled() || mExitTasksEarly) {
				drawable = null;
			}

			final ImageView imageView = getAttachedImageView();
			if (drawable != null && imageView != null) {
				setImageDrawable(imageView, drawable);
			}
		}

		@Override
		protected void onCancelled(BitmapDrawable drawable) {
			super.onCancelled(drawable);
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * �������������ص�ImageView�� ���ImageView �ڵ������ǵ�ǰ���� �򷵻ص�ǰImageView,���򷵻�null��
		 * 
		 * @return
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final LoadBitmapTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
			if (this == bitmapWorkerTask) {
				return imageView;
			}
			return null;
		}
	}

	/**
	 * �洢�첽��ϢͼƬ��Դ��
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<LoadBitmapTask> bitmapWorkerTaskReference;// ������

		public AsyncDrawable(Resources res, Bitmap bitmap,
				LoadBitmapTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<LoadBitmapTask>(
					bitmapWorkerTask);
		}

		public LoadBitmapTask getLoadBitmapTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * ����ͼƬ��Դ�ڴ�ŵ��첽�̣߳�������ڣ��򷵻أ������ڣ�����null��
	 *
	 * @param imageView
	 *            ��ǰ����첽��ԴͼƬ��ImageView
	 * @return
	 */
	private static LoadBitmapTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getLoadBitmapTask();
			}
		}
		return null;
	}

	/**
	 * �����첽�����Ƿ���ͣ��falseΪ������trueΪ��ͣ��
	 * 
	 * @param pauseWork
	 */
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	/**
	 * �˳��߳�
	 * 
	 * @param exitTasksEarly
	 */
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
		setPauseWork(false);// �������Ϊfalse��ʹ���˳��������š��������ΪtrueҲ�ǿ��еģ�Ҳû�����⣬���Դﵽͬ����Ч�������ǿ��ԱȽ�����Ϊtrue��false������
	}
}