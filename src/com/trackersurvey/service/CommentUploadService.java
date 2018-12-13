package com.trackersurvey.service;



import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.trackersurvey.happynavi.R;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.httpconnection.MyTask;
import com.trackersurvey.httpconnection.PostCheckVersion;
import com.trackersurvey.httpconnection.PostCommentFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class CommentUploadService extends Service{
	private final IBinder binder=new CommentBinder();
	private  SharedPreferences uploadCache;
	private  boolean hasUploadComment = false;
	private  int upFileNum = 0;
	private  int uploadedNum = 0;
	public   final String SHAREDFILES = "uploadFiles";
	private BroadcastReceiver connectionReceiver = null; // 用于监听网络状态变化的广播
	
	public class CommentBinder extends Binder{
		public CommentUploadService getService(){
			return CommentUploadService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		uploadCache = getSharedPreferences("uploadCache", Activity.MODE_PRIVATE);
		connectionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				
				NetworkInfo netInfo = connectMgr.getActiveNetworkInfo();
				if(netInfo!=null && netInfo.isConnected()){
					//有网络连接
					if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
						//wifi连接
						Log.i("upfile","wifi连接，检查是否有评论未上传");						
						uploadWhenConnect();
					}
					else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){
						// connect network,读取本地sharedPreferences文件，上传之前未完成上传的部分
						
						if (!Common.isOnlyWifiUploadPic(CommentUploadService.this)) {
							Log.i("upfile","gprs连接，检查是否有评论未上传");
							uploadWhenConnect();
						}
						
					}
				}
				
			}
		};
		
		//注册监听网络连接状态广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectionReceiver, intentFilter);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("upfile", "onStartCommand");
		String createTime = intent.getStringExtra("createTime");
		if(createTime != null && !createTime.equals("")){
			
			uploadComment(Common.getUserId(getApplicationContext()), createTime);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 上传
	 * 
	 * @param createTime
	 *            评论的时间
	 * @param upFile
	 *            要上传的文件的标记，0未上传，1已上传， 如果upFile ==null，则所有文件未上传
	 */
	public void uploadComment(String userID, String createTime) {
		Log.i("upfile","from service "+ "一次上传,createTime = " + createTime);
		MyTask commentTask = new MyTask(CommentUploadService.this, userID, createTime,
				new CommentHandler(createTime),Common.getDeviceId(getApplicationContext()));
		commentTask.start();
		hasUploadComment = true;
	}
	/**
	 * 从cache文件中读取信息，上传未完成部分
	 */
	protected void uploadWhenConnect() {
		if (!hasUploadComment) {
			uploadCache = getSharedPreferences("uploadCache",
					Activity.MODE_PRIVATE);
			Map<String, ?> cache = uploadCache.getAll();
			if (!cache.isEmpty() && cache.size() > 0) {
				Entry<String, ?> entry = cache.entrySet().iterator().next();
				uploadComment((String) entry.getValue(), entry.getKey());
			} else {
				upFileWhenConnect();
			}
		}
	}
	/**
	 * @author 易 上传评论的回调
	 */
	class CommentHandler extends Handler {
		private String createTime;

		public CommentHandler(String createTime) {
			this.createTime = createTime;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				Log.i("upfile","from service "+ "result=" + msg.obj);
				uploadCache.edit().remove(createTime).commit();
				hasUploadComment = false;
				uploadWhenConnect();
				break;
			}
			case 1: {
				Log.i("upfile","from service "+ "result=" + msg.obj);
				break;
			}
			case 2: {
				Log.i("upfile", "from service "+"result=" + msg.obj);
				break;
			}
			}
		};
	}

	/**
	 * 网络连接时上传文件
	 */
	private void upFileWhenConnect() {
		Map<String, ?> fileCache = getSharedPreferences(
				SHAREDFILES, MODE_PRIVATE).getAll();
		upFileNum = fileCache.size();
		uploadedNum = 0;
		uploadFiles(1, fileCache);
	}

	/**
	 * 上传所有文件 一次只能有一个文件上传
	 * 
	 * @param uploadHost
	 */
	@SuppressWarnings("unchecked")
	private void uploadFiles(int command, Map<String, ?> cache) {
		if (!cache.isEmpty() && cache.size() > 0) {
			Iterator<?> iterator = cache.entrySet().iterator();
			Entry<String, ?> entry = null;
			switch (command) {
			case 1:
				entry = (Entry<String, ?>) iterator.next();
				break;
			case 2:
				entry = (Entry<String, ?>) iterator.next();
				if (iterator.hasNext()) {
					entry = (Entry<String, ?>) iterator.next();
				}else{
					return;
				}
				break;
			}
			String key = entry.getKey();
			String para[] = key.split(File.separator);

			String createTime = para[0];
			int fileID = Integer.parseInt(para[1]);
			int fileType = Integer.parseInt(para[2]);
			String userID = para[3];
			String fileName = (String) entry.getValue();

			uploadOneFile(key,userID, createTime, fileID, fileType, fileName,
					upFileNum);
			Log.i("upfile","from service "+"upload one file "+ userID + "|" + createTime + "|" + fileID + "|"
					+ fileName);
		} else {
			uploadedNum = 0;
			upFileNum = 0;
		}
	}

	/**
	 * 上传一个文件
	 * 
	 * @param createTime
	 * @param fileID
	 * @param fileType
	 * @param fileName
	 */
	private void uploadOneFile(String key,String userID, String createTime,
			int fileID, int fileType, String fileName, int total) {
		if(Common.URL_UPFILE == null||Common.URL_UPFILE.equals("")){
			Common.URL_UPFILE = getResources().getString(R.string.url_upfile);
		}
		PostCommentFile pcf = new PostCommentFile(CommentUploadService.this, userID,
				createTime, new FileHandler(key, fileID, total),
				Common.URL_UPFILE, fileID, fileType, fileName,Common.getDeviceId(getApplicationContext()));
		pcf.start();
	}

	class FileHandler extends Handler {
		private String fileKey;
		private int fileID;
		private int total;

		public FileHandler(String key, int fileID, int total) {
			this.fileKey = key;
			this.fileID = fileID;
			this.total = total;
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0: {
				fileUploading(++uploadedNum, total);
				SharedPreferences uploadFiles = getSharedPreferences(SHAREDFILES, MODE_PRIVATE);
				uploadFiles.edit().remove(fileKey).commit();
				Map<String, ?> fileCache = uploadFiles.getAll();
				Log.i("upfile", "from service "+"" + msg.obj+"file size="+fileCache.size());
				if (!hasUploadComment) {
					uploadFiles(1, fileCache);
				}
				break;
			}
			case 1: {
				uploadError(fileID, fileKey, getResources().getString(R.string.tips_error));
				break;
			}
			case 2: {
				uploadError(fileID, fileKey, getResources().getString(R.string.tips_fail));
				break;
			}
			default:
				break;
			}
		};
	};

	public void uploadError(final int fileID, final String key,
			final String text) {
		if (Common.checkNetworkState(getApplicationContext()) < 0) {
			Toast.makeText(getApplicationContext(), R.string.tips_uploadpic_neterror, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (hasUploadComment) {
			return;
		}
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				AlertDialog.Builder builder = new Builder(CommentUploadService.this);
				builder.setMessage(getResources().getString(R.string.tips_uploadfaildlg_msg1) 
						+ (fileID + 1) + getResources().getString(R.string.tips_uploadfaildlg_msg2) 
						+ text + getResources().getString(R.string.tips_uploadfaildlg_msg3));
				builder.setTitle(getResources().getString(R.string.tip));
				final Map<String, ?> fileCache = getSharedPreferences(
						SHAREDFILES, MODE_PRIVATE).getAll();
				builder.setPositiveButton(getResources().getString(R.string.tryagain), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						uploadFiles(1, fileCache);
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						SharedPreferences uploadFiles = getSharedPreferences(SHAREDFILES, MODE_PRIVATE);
						uploadFiles.edit().remove(key).commit();
						Map<String, ?> cache = uploadFiles.getAll();
						Log.i("upfile","from service "+ "删除：" + "file size="+fileCache.size());
						uploadedNum--;
						uploadFiles(1, cache);
						
					}
				});
				builder.setNeutralButton(getResources().getString(R.string.cancl), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						uploadFiles(2, fileCache);
						
					}
				});
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.setCanceledOnTouchOutside(false);//点击对话框外部，对话框不会消失
				dialog.setCancelable(false);//按返回键不消失
				dialog.show();
			}
		});
	}

	/**
	 * Toast显示文件上传进度
	 */
	public void fileUploading(int complete,int total) {
		Log.i("upfile","from service "+ "fileUploading " + complete + "|"+total);
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.tips_uploadinbg)+"：" + complete + "/"+total,
				Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(null != connectionReceiver){
			unregisterReceiver(connectionReceiver);
		}
	}

}
