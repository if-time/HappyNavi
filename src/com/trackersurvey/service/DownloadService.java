/*
 * @Title DownloadService.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-7 ����10:03:42
 * @version 1.0
 */
package com.trackersurvey.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.trackersurvey.entity.FileInfo;
import com.trackersurvey.happynavi.MainActivity;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.ToastUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView.FindListener;
import android.widget.RemoteViews;

/** 
 * ��ע��
 * @author Yann
 * @date 2015-8-7 ����10:03:42
 */
public class DownloadService extends Service
{
	public static final String DOWNLOAD_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/downloads/";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	public static final String ACTION_ERROR = "ACTION_ERROR";
	public static final int MSG_INIT = 0;
	private String TAG = "phonelog";
	private String fileName=null;
	private Map<Integer, DownloadTask> mTasks = 
			new LinkedHashMap<Integer, DownloadTask>();
	private BroadcastReceiver updateReceiver = null; //�������½��ȵĹ㲥
	//֪ͨ��������
	private NotificationManager mNotificationManager=null;
	private Notification mNotification;
	private PendingIntent 	updatePendingIntent=null;
	/**
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// ���Activity�������Ĳ���
		if (ACTION_START.equals(intent.getAction()))
		{
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			if(fileInfo!=null){
			Log.i(TAG , "Start:" + fileInfo.toString());
			fileName=fileInfo.getFileName();
			// ������ʼ���߳�
			new InitThread(fileInfo).start();
			}
			else{
				Log.i(TAG , "Start error,file is null");
			}
		}
		else if (ACTION_STOP.equals(intent.getAction()))
		{
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i(TAG , "Stop:" + fileInfo.toString());
			
			// �Ӽ�����ȡ����������
			DownloadTask task = mTasks.get(fileInfo.getId());
			if (task != null)
			{
				task.isPause = true;
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//������¹㲥
				updateReceiver = new BroadcastReceiver()
				{
					@Override
					public void onReceive(Context context, Intent intent)
					{
						if (DownloadService.ACTION_UPDATE.equals(intent.getAction()))
						{
							int finised = intent.getIntExtra("finished", 0);
							int id = intent.getIntExtra("id", 0);
							mNotification.contentView.setTextViewText(R.id.download_view_count,getResources().getString(R.string.tips_downprogress)+"��"+finised+"%");
							mNotification.contentView.setProgressBar(R.id.download_view_progress,100,finised,false);
							mNotificationManager.notify(0, mNotification);
							Log.i("phonelog","�յ����ȹ㲥��" +id + "-finised = " + finised);
						}
						else if (DownloadService.ACTION_FINISHED.equals(intent.getAction()))
						{
							// ���ؽ���
							//FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
							//mAdapter.updateProgress(fileInfo.getId(), 0);
							Common.isUpdationg=false;
							
							mNotification.contentView.setTextViewText(R.id.download_view_count,100+"% "+getResources().getString(R.string.tips_downfinish));
							mNotification.contentView.setProgressBar(R.id.download_view_progress,100,100,false);
							mNotificationManager.notify(0, mNotification);
							mNotificationManager.cancel(0);
							ToastUtil.show(getApplicationContext(), getResources().getString(R.string.tips_newapkdown));
							Log.i("phonelog","�յ���ɹ㲥���������");
							
							Uri uri=Uri.fromFile(new File(DOWNLOAD_PATH+fileName));
							Intent installIntent = new Intent(Intent.ACTION_VIEW);
			                installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
			                updatePendingIntent = PendingIntent.getActivity(DownloadService.this, 0, installIntent, 0);
			                Notification notification = new Notification.Builder(DownloadService.this)
									.setContentTitle("100% "+getResources().getString(R.string.tips_downfinish))
									.setContentText(getResources().getString(R.string.tips_install))
									.setSmallIcon(R.drawable.ic_launcher)
									.setContentIntent(updatePendingIntent)
									
									.build();
			                notification.flags|=Notification.FLAG_AUTO_CANCEL;
			                mNotificationManager.notify(1, notification);
			                Common.isUpdationg=false;//���ظ��½���
						}
					}
				};
				// ע����¹㲥������
		        IntentFilter filter = new IntentFilter();
		        filter.addAction(DownloadService.ACTION_UPDATE);
		        filter.addAction(DownloadService.ACTION_FINISHED);
		        filter.addAction(DownloadService.ACTION_ERROR);
		        this.registerReceiver(updateReceiver, filter);
		      //��ʾ���ؽ���֪ͨ��
				mNotificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);  
				mNotification = new Notification.Builder(DownloadService.this)
						.setContentTitle(getResources().getString(R.string.tips_downnewapk))
						
						.setSmallIcon(R.drawable.ic_launcher)
						.setContent(new RemoteViews(getPackageName(),R.layout.download_notification))
						.build();
				mNotificationManager.notify(0,mNotification);
				
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(updateReceiver);
		mNotificationManager.cancelAll();
		super.onDestroy();
	}

	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what)
			{
				case MSG_INIT:
					FileInfo fileInfo = (FileInfo) msg.obj;
					Log.i(TAG, "׼������--->Init:" + fileInfo);
					// ������������
					DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 1);
					task.downLoad();
					// ������������ӵ�������
					mTasks.put(fileInfo.getId(), task);
					break;

				default:
					break;
			}
		};
	};
	
	private class InitThread extends Thread
	{
		private FileInfo mFileInfo = null;
		
		public InitThread(FileInfo mFileInfo)
		{
			this.mFileInfo = mFileInfo;
		}
		
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			
			try
			{
				// ���������ļ�
				URL url = new URL(mFileInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				int length = -1;
				
				if (connection.getResponseCode() == HttpStatus.SC_OK)
				{
					// ����ļ��ĳ���
					length = connection.getContentLength();
				}
				
				if (length <= 0)
				{
					Log.i(TAG, "file length<=0,ȡ�����ز���");
					return;
				}
				
				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists())
				{
					dir.mkdir();
				}
				
				// �ڱ��ش����ļ�
				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				// �����ļ�����
				raf.setLength(length);
				mFileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (connection != null)
				{
					connection.disconnect();
				}
				if (raf != null)
				{
					try
					{
						raf.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
