/*
 * @Title DownloadTask.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-7 ����10:11:05
 * @version 1.0
 */
package com.trackersurvey.service;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import com.trackersurvey.db.ThreadDAO;
import com.trackersurvey.db.ThreadDAOImpl;
import com.trackersurvey.entity.FileInfo;
import com.trackersurvey.entity.ThreadInfo;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;



/** 
 * ����������
 * @author Yann
 * @date 2015-8-7 ����10:11:05
 */
public class DownloadTask
{
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinised = 0;
	public boolean isPause = false;
	private int mThreadCount = 1;  // �߳�����
	private List<DownloadThread> mDownloadThreadList = null; // �̼߳���
	
	/** 
	 *@param mContext
	 *@param mFileInfo
	 */
	public DownloadTask(Context mContext, FileInfo mFileInfo, int count)
	{
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = count;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void downLoad()
	{
		//mDao.deleteAllThread();
		// ��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		
		if (0 == threads.size())
		{
			// ����ÿ���߳����س���
			int len = mFileInfo.getLength() / mThreadCount;
			for (int i = 0; i < mThreadCount; i++)
			{
				// ��ʼ���߳���Ϣ����
				threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
						len * i, (i + 1) * len - 1, 0);
				
				if (mThreadCount - 1 == i)  // �������һ���߳����س��Ȳ�������������
				{
					threadInfo.setEnd(mFileInfo.getLength());
				}
				
				// ��ӵ��̼߳�����
				threads.add(threadInfo);
				mDao.insertThread(threadInfo);
			}
		}

		mDownloadThreadList = new ArrayList<DownloadTask.DownloadThread>();
		// ��������߳̽�������
		for (ThreadInfo info : threads)
		{
			DownloadThread thread = new DownloadThread(info);
			thread.start();
			// ��ӵ��̼߳�����
			mDownloadThreadList.add(thread);
		}
	}
	
	/** 
	 * �����߳�
	 * @author Yann
	 * @date 2015-8-8 ����11:18:55
	 */ 
	private class DownloadThread extends Thread
	{
		private ThreadInfo mThreadInfo = null;
		public boolean isFinished = false;  // �߳��Ƿ�ִ�����

		/** 
		 *@param mInfo
		 */
		public DownloadThread(ThreadInfo mInfo)
		{
			this.mThreadInfo = mInfo;
		}
		
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			InputStream inputStream = null;
			
			try
			{
				URL url = new URL(mThreadInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				connection.setAllowUserInteraction(true);
				// ��������λ��
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();//��ʼλ��+�����ص��ļ�����
				connection.setRequestProperty("Range",
						"bytes=" + start + "-" + mThreadInfo.getEnd());
				Log.i("phonelog", mThreadInfo.getId() +",start:"+start+",end:"+mThreadInfo.getEnd());
				//connection.setRequestProperty("Connection", "Keep-Alive");
				File file = new File(DownloadService.DOWNLOAD_PATH,
						mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent = new Intent();
				intent.setAction(DownloadService.ACTION_UPDATE);
				mFinised += mThreadInfo.getFinished();
				Log.i("phonelog", mThreadInfo.getId() + "finished = " + mThreadInfo.getFinished());
				// ��ʼ����
				if(start<mThreadInfo.getEnd()){
					if (connection.getResponseCode() ==HttpStatus.SC_PARTIAL_CONTENT )//HttpStatus.SC_PARTIAL_CONTENT
					{
						// ��ȡ����
						inputStream = connection.getInputStream();
						byte buf[] = new byte[1024 << 2];
						int len = -1;
						long time = System.currentTimeMillis();
						while ((len = inputStream.read(buf)) != -1)
						{
							// д���ļ�
							raf.write(buf, 0, len);
							// �ۼ������ļ���ɽ���
							mFinised += len;
							// �ۼ�ÿ���߳���ɵĽ���
							mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
	//						if((mThreadInfo.getEnd()-mThreadInfo.getStart())<=mThreadInfo.getFinished()){
	//							Log.i("phonelog", mThreadInfo.getId() + "�����ˣ�finished = " + mThreadInfo.getFinished()
	//							+"---total:"+mFinised);
	//							break;
	//						}
							if (System.currentTimeMillis() - time > 1000)
							{
								time = System.currentTimeMillis();
								int f = mFinised * 100 / mFileInfo.getLength();
								if (f > mFileInfo.getFinished())
								{
									intent.putExtra("finished", f);
									intent.putExtra("id", mFileInfo.getId());
									mContext.sendBroadcast(intent);
									
								}
								Log.i("phonelog", mThreadInfo.getId() + "�����У�finished = " + mThreadInfo.getFinished()
										+"---total:"+mFinised);
								
							}
							
							// ��������ͣʱ���������ؽ���
							if (isPause)
							{
								mDao.updateThread(mThreadInfo.getUrl(),	
										mThreadInfo.getId(), 
										mThreadInfo.getFinished());
								
								Log.i("phonelog", mThreadInfo.getId() + "��ͣ��finished = " + mThreadInfo.getFinished());
								
								return;
							}
						}
						
						// ��ʶ�߳�ִ�����
						isFinished = true;
						checkAllThreadFinished();
					}
					else{
						Log.i("phonelog",mThreadInfo.getId() +"�����쳣,������:"+connection.getResponseCode());
					}
				}
				else{
					// ��ʶ�߳�ִ�����
					isFinished = true;
					checkAllThreadFinished();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Intent intent = new Intent(DownloadService.ACTION_ERROR);
				intent.putExtra("fileInfo", mFileInfo);
				mContext.sendBroadcast(intent);
				Log.i("phonelog","�����쳣e----"+e.getMessage());
			}
			finally
			{
				try
				{
					mDao.updateThread(mThreadInfo.getUrl(),	
							mThreadInfo.getId(), 
							mThreadInfo.getFinished());
					
					Log.i("phonelog", mThreadInfo.getId() + "������ȣ�finished = " + mThreadInfo.getFinished());
					
					if (connection != null)
					{
						connection.disconnect();
					}
					if (raf != null)
					{
						raf.close();
					}
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
					Log.i("phonelog","�����쳣e2----"+e2.getMessage());
				}
			}
		}
	}
	
	/** 
	 * �ж����е��߳��Ƿ�ִ�����
	 * @return void
	 * @author Yann
	 * @date 2015-8-9 ����1:19:41
	 */ 
	private synchronized void checkAllThreadFinished()
	{
		boolean allFinished = true;
		
		// �����̼߳��ϣ��ж��߳��Ƿ�ִ�����
		for (DownloadThread thread : mDownloadThreadList)
		{
			if (!thread.isFinished)
			{
				allFinished = false;
				break;
			}
		}
		
		if (allFinished)
		{
			// ɾ�����ؼ�¼
			mDao.deleteThread(mFileInfo.getUrl());
			// ���͹㲥֪��UI�����������
			Intent intent = new Intent(DownloadService.ACTION_FINISHED);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.sendBroadcast(intent);
			Log.i("phonelog","�������");
		}
	}
}
