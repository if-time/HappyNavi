package com.trackersurvey.offlinemap;

import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.trackersurvey.happynavi.R;


public class OfflineChild implements OnClickListener, OnLongClickListener {
	private Context mContext;

	private TextView mOffLineCityName;// ���߰�����

	private TextView mOffLineCitySize;// ���߰���С

	private ImageView mDownloadImage;// �������Image

	private TextView mDownloadProgress;

	private OfflineMapManager amapManager;

	private OfflineMapCity mMapCity;// �������س���

	Dialog dialog;// ���������ĶԻ���

	private boolean mIsDownloading = false;

	private boolean isProvince = false;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int completeCode = (Integer) msg.obj;
			switch (msg.what) {
			case OfflineMapStatus.LOADING:
				
				
				displyaLoadingStatus(completeCode);
				
				
				break;
			case OfflineMapStatus.PAUSE:
				displayPauseStatus(completeCode);
				break;
			case OfflineMapStatus.STOP:
				break;
			case OfflineMapStatus.SUCCESS:
				displaySuccessStatus();
				break;
			case OfflineMapStatus.UNZIP:
				displayUnZIPStatus(completeCode);
				break;
			case OfflineMapStatus.ERROR:
				displayExceptionStatus();
				break;
			case OfflineMapStatus.WAITING:
				displayWaitingStatus(completeCode);
				break;
			case OfflineMapStatus.CHECKUPDATES:
				displayDefault();
				break;
				
			case OfflineMapStatus.EXCEPTION_AMAP:
			case OfflineMapStatus.EXCEPTION_NETWORK_LOADING:
			case OfflineMapStatus.EXCEPTION_SDCARD:
				displayExceptionStatus();
				break;
				
//			case OfflineMapStatus.NEW_VERSION:
//				displayHasNewVersion();
//				break;

			}
		}

	};

	public boolean isProvince() {
		return isProvince;
	}

	public void setProvince(boolean isProvince) {
		this.isProvince = isProvince;
	}

	public OfflineChild(Context context, OfflineMapManager offlineMapManager) {
		mContext = context;
		initView();
		amapManager = offlineMapManager;
		// mOfflineMapManager = new OfflineMapManager(mContext, this);
	}

	public String getCityName() {
		if (mMapCity != null) {
			return mMapCity.getCity();
		}
		return null;
	}

	public View getOffLineChildView() {
		return mOffLineChildView;
	}

	private View mOffLineChildView;

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mOffLineChildView = inflater.inflate(R.layout.offlinemap_child, null);
		mOffLineCityName = (TextView) mOffLineChildView.findViewById(R.id.name);
		mOffLineCitySize = (TextView) mOffLineChildView
				.findViewById(R.id.name_size);
		mDownloadImage = (ImageView) mOffLineChildView
				.findViewById(R.id.download_status_image);
		mDownloadProgress = (TextView) mOffLineChildView
				.findViewById(R.id.download_progress_status);

		mOffLineChildView.setOnClickListener(this);
		mOffLineChildView.setOnLongClickListener(this);

	}

	public void setOffLineCity(OfflineMapCity mapCity) {
		if (mapCity != null) {
			mMapCity = mapCity;
			mOffLineCityName.setText(mapCity.getCity());
			double size = ((int) (mapCity.getSize() / 1024.0 / 1024.0 * 100)) / 100.0;
			mOffLineCitySize.setText(String.valueOf(size) + " M");

			notifyViewDisplay(mMapCity.getState(), mMapCity.getcompleteCode(),
					mIsDownloading);
		}
	}

	/**
	 * ������ʾ״̬ �ڱ���������ؽ��ȷ����ı�ʱ�ᱻ����
	 * 
	 * @param status
	 * @param completeCode
	 * @param isDownloading
	 */
	private void notifyViewDisplay(int status, int completeCode,
			boolean isDownloading) {
		if (mMapCity != null) {
			mMapCity.setState(status);
			mMapCity.setCompleteCode(completeCode);
		}
		Message msg = new Message();
		msg.what = status;
		msg.obj = completeCode;
		handler.sendMessage(msg);

	}

	/**
	 * ��ԭʼ��״̬��δ���أ���ʾ���ذ�ť
	 */
	private void displayDefault() {
		mDownloadProgress.setVisibility(View.INVISIBLE);
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_download);
	}
	
	/**
	 * ��ʾ�и���
	 */
	private void displayHasNewVersion() {
		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_download);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.downhasnew));
	}

	/**
	 * �ȴ���
	 * 
	 * @param completeCode
	 */
	private void displayWaitingStatus(int completeCode) {
		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_start);
		mDownloadProgress.setTextColor(Color.GREEN);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.waiting));
	}
	
	/**
	 * ���س����쳣
	 * 
	 * @param completeCode
	 */
	private void displayExceptionStatus() {
		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_start);
		mDownloadProgress.setTextColor(Color.RED);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.downhaserror));
	}

	/**
	 * ��ͣ
	 * 
	 * @param completeCode
	 */
	private void displayPauseStatus(int completeCode) {
		if (mMapCity != null) {
			completeCode = mMapCity.getcompleteCode();
		}

		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_start);
		mDownloadProgress.setTextColor(Color.RED);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.pausing)+":" + completeCode + "%");

	}

	/**
	 * ���سɹ�
	 */
	private void displaySuccessStatus() {
		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.GONE);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.installsuccess));

		mDownloadProgress.setTextColor(mContext.getResources().getColor(
				R.color.gary));
	}

	/**
	 * ���ڽ�ѹ
	 */
	private void displayUnZIPStatus(int completeCode) {
		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadImage.setVisibility(View.GONE);
		mDownloadProgress.setText(mContext.getResources().getString(R.string.decompressing)+": " + completeCode + "%");
		mDownloadProgress.setTextColor(mContext.getResources().getColor(
				R.color.gary));
	}

	/**
	 * 
	 * @param completeCode
	 */
	private void displyaLoadingStatus(int completeCode) {
		// todo
		if (mMapCity == null) {
			return;
		}

		mDownloadProgress.setVisibility(View.VISIBLE);
		mDownloadProgress.setText(mMapCity.getcompleteCode() + "%");
		mDownloadImage.setVisibility(View.VISIBLE);
		mDownloadImage.setImageResource(R.drawable.offlinearrow_stop);
		mDownloadProgress.setTextColor(Color.BLUE);
	}

	private synchronized void pauseDownload() {
		amapManager.pause();
//		amapManager.pauseByName(getCityName());
		//��ͣ����֮�󣬿�ʼ��һ���ȴ��е�����
		amapManager.restart();
	}

	/**
	 * ������������
	 */
	private synchronized boolean startDownload() {
		try {
			if (isProvince) {
				amapManager.downloadByProvinceName(mMapCity.getCity());
			} else {
				amapManager.downloadByCityName(mMapCity.getCity());
			}
			return true;
		} catch (AMapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			Toast.makeText(mContext, e.getErrorMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public void onClick(View view) {
		
//		if(mMapCity.getCity() .equals( "����")) {
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i =0; i< 100;i++) {
//						try {
//							amapManager.downloadByCityName("����");
//							TimeUnit.MILLISECONDS.sleep(500);
//							amapManager.downloadByCityName("�Ϻ�");
//							TimeUnit.MILLISECONDS.sleep(500);
//						} catch (AMapException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				
//				}
//			}).start();
//			return;
//		}
		
//		// ����Ƶ������¼������ⲻ�ϴӷ�ʼ���غ���ͣ����
//		mOffLineChildView.setEnabled(false);
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				mOffLineChildView.setEnabled(true);
//			}
//		},100);// ���ʱ��θոպ�

		int completeCode = -1, status = -1;
		if (mMapCity != null) {
			status = mMapCity.getState();
			completeCode = mMapCity.getcompleteCode();

			switch (status) {
			case OfflineMapStatus.UNZIP:
			case OfflineMapStatus.SUCCESS:
				// ��ѹ�к��ڳɹ�ɶ����
				break;
			case OfflineMapStatus.LOADING:
//			case OfflineMapStatus.WAITING:
				pauseDownload();
				// �������е�ʱ��������ʾҪ��ͣ
				displayPauseStatus(completeCode);
				break;
			case OfflineMapStatus.PAUSE:
			case OfflineMapStatus.CHECKUPDATES:
			case OfflineMapStatus.ERROR:
			case OfflineMapStatus.WAITING:
//			case OfflineMapStatus.NEW_VERSION:
			default:
				if(startDownload())
					displayWaitingStatus(completeCode);
				else 
					displayExceptionStatus();
//					Toast.makeText(mContext, "SD���ռ䲻����", 1000).show();
				// ����ͣ�е������ʾҪ��ʼ����
				// ��Ĭ��״̬�������ʾ��ʼ����
				// �ڵȴ��е������ʾҪ��ʼ����
				// Ҫ��ʼ����״̬��Ϊ�ȴ��У��ٻص��л��Լ��޸�
				break;
			}
			
			Log.e("zxy-child", mMapCity.getCity() + " " + mMapCity.getState());

		}

	}

	/**
	 * ����������ʾ�� ɾ����ȡ��������
	 * ����synchronized ������dialog��û�йرյ�ʱ���ٴΣ����󵯳���bug
	 */
	public synchronized void showDeleteDialog(final String name) {
		AlertDialog.Builder builder = new Builder(mContext);

		builder.setTitle(name);
		builder.setSingleChoiceItems(new String[] { mContext.getResources().getString(R.string.delete) }, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dialog.dismiss();
						if (amapManager == null) {
							return;
						}
						switch (arg1) {
						case 0:
							amapManager.remove(name);
							break;

						default:
							break;
						}

						// amapManager.log();

					}
				});
		builder.setNegativeButton(mContext.getResources().getString(R.string.cancl), null);
		dialog = builder.create();
		dialog.show();
	}

	/**
	 * ����������ʾ�� ɾ���͸���
	 */
	public void showDeleteUpdateDialog(final String name) {
		AlertDialog.Builder builder = new Builder(mContext);

		builder.setTitle(name);
		builder.setSingleChoiceItems(new String[] { mContext.getResources().getString(R.string.delete),
				mContext.getResources().getString(R.string.checkupdate) }, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						dialog.dismiss();
						if (amapManager == null) {
							return;
						}
						switch (arg1) {
						case 0:
							amapManager.remove(name);
							break;
						case 1:
							try {
								amapManager.updateOfflineCityByName(name);
							} catch (AMapException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						default:
							break;
						}

					}
				});
		builder.setNegativeButton(mContext.getResources().getString(R.string.cancl), null);
		dialog = builder.create();
		dialog.show();
	}

	public boolean onLongClick(View arg0) {
		
//		if (mMapCity.getState() == OfflineMapStatus.LOADING) {
//			amapManager.restart();
//			return false;
//		} 
		
		Log.d("amap-longclick",
				mMapCity.getCity() + " : " + mMapCity.getState());
		if (mMapCity.getState() == OfflineMapStatus.SUCCESS) {
			showDeleteUpdateDialog(mMapCity.getCity());
		} else if (mMapCity.getState() != OfflineMapStatus.CHECKUPDATES) {
			showDeleteDialog(mMapCity.getCity());
		} 
		return false;
	}

}
