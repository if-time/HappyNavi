package com.trackersurvey.happynavi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import com.trackersurvey.adapter.ImageAdapter;
import com.trackersurvey.adapter.ImageAdapter.OnCheckBoxClickListener;
import com.trackersurvey.adapter.ImageAdapter.OnPictureClickListener;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.photoview.ImageWorker;
import com.trackersurvey.photoview.SelectedTreeMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ��ȡ��������ͼƬ��ͼƬ��ʽ��ʾ������ѡ����ű���ͼƬ
 * 
 * @author Eaa
 *
 */
public class PictureBrowser extends Activity implements OnClickListener {

	// �����еĸ������
	private GridView gView; // ��ʾͼƬ��GridView
	private Spinner tvDirName; // ���½��ļ������ְ�ť
	private TextView tvPreview; // ���½�Ԥ����ť
	private LinearLayout titleBack; // �������󲿻��˰�ť
	private TextView titleText; // ����������
	private Button titleRightButton; // �������Ҳ�ȷ�ϰ�ť

	private ArrayList<HashMap<String, Object>> imageItem; // gridView �����
	private int itemNo = 0; // ѡ�е�ͼƬ����

	public final static String RESULT_URIS = "result_uris";
	public final static String INTENT_CLAZZ = "clazz";
	private ImageWorker imageWorker;// ����ͼƬ���첽�߳���
	private TreeMap<Long, Uri> selectedTree;// �����ѡ�е�ͼƬ��id��uri����
	private int selectFold = 0;
	private boolean hasViedo = false;

	private SelectedTreeMap selectedTreeMap = new SelectedTreeMap();
	// �����ͼƬ���ļ������Ͷ�Ӧ·��
	private ArrayList<PicFolder> picFolders;
	private String[] dirs;

	private ImageAdapter adapter;
	private LoadLocalPicture cursorTask;// ��ȡ����ͼƬ���ݵ��첽�߳���
	private AlphaAnimation inAlphaAni;// ÿ��ͼƬ����ʱ�������Ե�Ч������
	private AlphaAnimation outAlphaAni;// ÿ��ͼƬ����ʱ�������Ե�Ч������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_browser);
		AppManager.getAppManager().addActivity(this);
		tvDirName = (Spinner) findViewById(R.id.pbbuttomDirName);
		tvPreview = (TextView) findViewById(R.id.pbbuttomHint);
		gView = (GridView) findViewById(R.id.gridBrowserPicture);
		titleRightButton = (Button) findViewById(R.id.pbbuttomPreview);
		titleRightButton.setVisibility(View.GONE);
		titleRightButton.setOnClickListener(this);
		imageItem = new ArrayList<HashMap<String, Object>>();
		gView.setColumnWidth((Common.winWidth - 8) / 3);
		selectedTree = new TreeMap<Long, Uri>();
		picFolders = new ArrayList<PictureBrowser.PicFolder>();
		itemNo = 0;
		if (getIntent().getIntExtra("hasVideo", -1) >= 0) {
			itemNo++;
			hasViedo = true;
		}
		init();

	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		// ѡ����ѡ���ͼƬ
		Intent intent = getIntent();
		if (intent.hasExtra(RESULT_URIS)) {
			selectedTree = ((SelectedTreeMap) intent
					.getParcelableExtra(RESULT_URIS)).getTreeMap();
			if (selectedTree.size() > 0) {
				titleRightButton.setVisibility(View.VISIBLE);
				titleRightButton.setBackgroundColor(0x8866cc00);
			}
		}

		imageWorker = new ImageWorker(this);
		// ���bitmap��GridView��ÿһ��itemĬ��ʱ��ͼƬ
		Bitmap b = Bitmap.createBitmap(new int[] { 0x00000000 }, 1, 1,
				Bitmap.Config.ARGB_8888);
		// Bitmap b = BitmapFactory.decodeResource(getResources(),
		// R.drawable.ic_launcher);

		imageWorker.setLoadBitmap(b);
		adapter = new ImageAdapter((Common.winWidth - 8) / 3, imageWorker,
				this, opcl, ocbcl);
		gView.setAdapter(adapter);

		if (picFolders.size() > 0) {
			dirs = new String[picFolders.size()];
		} else {
			dirs = new String[1];
			dirs[0] = getResources().getString(R.string.allpic);
		}
		ArrayAdapter<String> dirAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, dirs);
		dirAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tvDirName.setAdapter(dirAdapter);

		tvDirName.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (picFolders.size() > 0) {
					selectFold = position;
					adapter.setOrigIdArray(picFolders.get(position).origIdArray);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}

		});

		loadData();
		initAnimation();
	}

	/**
	 * GridView��ÿ��itemͼƬ���س�ʼ������-�������Ե�Ч��
	 */
	private void initAnimation() {
		float fromAlpha = 0;
		float toAlpha = 1;
		int duration = 200;
		inAlphaAni = new AlphaAnimation(fromAlpha, toAlpha);
		inAlphaAni.setDuration(duration);
		inAlphaAni.setFillAfter(true);
		outAlphaAni = new AlphaAnimation(toAlpha, fromAlpha);
		outAlphaAni.setDuration(duration);
		outAlphaAni.setFillAfter(true);
	}

	/**
	 * ��������
	 */
	private void loadData() {
		cursorTask = new LoadLocalPicture(this);// ��ȡ����ͼƬ���첽�߳���
		/**
		 * �ص��ӿڡ�����ɱ���ͼƬ���ݵĻ�ȡ֮�󣬻ص�LoadLoacalPhotoCursorTask���е�OnLoadPhotoCursor�ӿ�
		 * ��onLoadPhotoSursorResult�����������ݴ��ݵ������
		 */
		cursorTask.setOnLoadPhotoCursor(new OnLoadPhotoCursor() {
			@Override
			public void onLoadPhotoSursorResult(ArrayList<PicFolder> picFolds) {
				if (isNotNull(picFolds)) {
					PictureBrowser.this.picFolders = picFolds;
					int size = picFolders.size();
					// �����ļ�������spinner�����ݺ�������
					dirs = new String[size];
					for (int i = 0; i < size; i++) {
						dirs[i] = picFolders.get(i).dirName;
					}
					ArrayAdapter<String> dirAdapter = new ArrayAdapter<String>(
							PictureBrowser.this,
							android.R.layout.simple_spinner_item, dirs);
					dirAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					tvDirName.setAdapter(dirAdapter);

					adapter.setOrigIdArray(picFolders.get(selectFold).origIdArray);
					Set<Long> stk = selectedTree.keySet();
					for (Long key : stk) {
						adapter.putSelectMap(key, true);
					}
					itemNo += stk.size();
					CharSequence text = itemNo + "/9";
					tvPreview.setText(text);
					adapter.notifyDataSetChanged();
				}
			}
		});
		cursorTask.execute();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// ѡ���ļ���
		case R.id.pbbuttomDirName:

			break;
		// ���ȷ�ϣ����ؽ����������Activity
		case R.id.pbbuttomPreview:
			selectedTreeMap.setTreeMap(selectedTree);
			Intent intent = new Intent();
			intent.putExtra(RESULT_URIS, selectedTreeMap);
			setResult(RESULT_OK, intent);
			finish();
			break;
		// ȷ��
		default:
			break;
		}
	}

	public class LoadLocalPicture extends AsyncTask<Object, Object, Object> {

		private final ContentResolver mContentResolver;
		private boolean mExitTasksEarly = false;// �˳������̵߳ı�־λ
		private OnLoadPhotoCursor onLoadPhotoCursor;// ����ص��ӿڣ���ȡ������������

		private ArrayList<PicFolder> picFolds = new ArrayList<PictureBrowser.PicFolder>();

		public LoadLocalPicture(Context mContext) {
			mContentResolver = mContext.getContentResolver();
		}

		@Override
		protected Object doInBackground(Object... params) {
			PicFolder pf = null;
			Uri extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			Cursor cursor = null;

			// ��ȡ����ͼƬ�ļ���
			String[] totalPicFold = { MediaStore.Images.Media._ID };
			cursor = MediaStore.Images.Media.query(mContentResolver, extUri,
					totalPicFold, MediaStore.Images.Media.SIZE +">=?", new String[]{20*1024 +""},
					MediaStore.Images.Media.DATE_ADDED + " desc");
			int totalPicNum = cursor.getCount();
			int currunt = 0;
			if (totalPicNum > 0) {
				cursor.moveToFirst();
				pf = new PicFolder(getResources().getString(R.string.allpic),
						-1, cursor.getLong(cursor
								.getColumnIndex(MediaStore.Images.Media._ID)),
						totalPicNum);
				do { // �Ƶ�ָ����λ�ã��������ݿ�
					long origId = cursor.getLong(0);
					pf.uriArray.add(Uri.withAppendedPath(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							origId + ""));

					pf.origIdArray.add(origId);
					cursor.moveToPosition(currunt);
					currunt++;
				} while (cursor.moveToNext() && currunt < totalPicNum
						&& !mExitTasksEarly);
				picFolds.add(pf);
			}
			cursor.close();
			// ��ȡ����ͼƬ���ļ���
			String[] projectionFold = { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Images.Media.BUCKET_ID,
					"count(" + MediaStore.Images.Media._ID + ")" };
			String selection = " 0==0) group by bucket_id --(";

			cursor = MediaStore.Images.Media.query(mContentResolver, extUri,
					projectionFold, selection, null,
					MediaStore.Images.Media.BUCKET_ID);

			while (cursor.moveToNext()) {
				String folder = cursor
						.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
				Long folderId = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
				int count = cursor.getInt(3);
				// Log.i("Eaa", folder + "|" + folderId + "|" + count);
				pf = new PicFolder(folder, folderId, cursor.getLong(cursor
						.getColumnIndex(MediaStore.Images.Media._ID)), count);
				picFolds.add(pf);

			}
			cursor.close();
			// ��ȡͼƬid
			String[] projection = { MediaStore.Images.Media._ID };
			String where = MediaStore.Images.Media.BUCKET_ID + "=?";
			for (int i = 1; i < picFolds.size(); i++) {

				String[] selectionParas = { "" + picFolds.get(i).dirId };
				cursor = MediaStore.Images.Media.query(mContentResolver,
						extUri, projection, where, selectionParas,
						MediaStore.Images.Media.DATE_ADDED + " desc");

				int columnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

				PicFolder pf1 = picFolds.get(i);
				int index = 0;
				int cursorCount = cursor.getCount();
				while (cursor.moveToNext() && index < cursorCount
						&& !mExitTasksEarly) { // �Ƶ�ָ����λ�ã��������ݿ�
					long origId = cursor.getLong(columnIndex);
					pf1.uriArray.add(Uri.withAppendedPath(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							origId + ""));

					pf1.origIdArray.add(origId);
					cursor.moveToPosition(index);
					index++;
				}
				cursor.close();// �ر����ݿ�
				System.out.println(cursorCount);
			}

			
			if (mExitTasksEarly) {
				picFolds = new ArrayList<PictureBrowser.PicFolder>();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Object o) {
			if (onLoadPhotoCursor != null && !mExitTasksEarly) {
				/**
				 * ��ѯ���֮�����ûص��ӿ��е����ݣ������ݴ��ݵ�Activity��
				 */
				onLoadPhotoCursor.onLoadPhotoSursorResult(picFolds);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled(); // To change body of overridden methods use
									// File | Settings | File Templates.
			mExitTasksEarly = true;
		}

		public void setExitTasksEarly(boolean exitTasksEarly) {
			this.mExitTasksEarly = exitTasksEarly;
		}

		public void setOnLoadPhotoCursor(OnLoadPhotoCursor onLoadPhotoCursor) {
			this.onLoadPhotoCursor = onLoadPhotoCursor;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		cursorTask.setExitTasksEarly(true);
		imageWorker.setExitTasksEarly(true);
		imageWorker.clearCache();
	}

	/**
	 * �ж�list��Ϊ��
	 * 
	 * @param list
	 * @return
	 */
	private static boolean isNotNull(ArrayList list) {
		return list != null && list.size() > 0;
	}

	public interface OnLoadPhotoCursor {
		public void onLoadPhotoSursorResult(ArrayList<PicFolder> picFolds);
	}

	/**
	 * ���GridView��ÿһ���е�ͼƬ�ļ���
	 */
	private OnPictureClickListener opcl = new ImageAdapter.OnPictureClickListener() {

		@Override
		public void onPictureClick(View view, int position, long id) {
			// TODO Auto-generated method stub
			Uri muri = Uri.withAppendedPath(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id + "");
			String[] projection = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(muri, projection, null,
					null, null);
			int index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			ArrayList<String> imagePath = new ArrayList<String>();
			imagePath.add(cursor.getString(index));
			cursor.moveToNext();
			System.out.println("cursor.getCount()=" + cursor.getCount());
			cursor.close();
			System.out.println("imagePath.size()=" + imagePath.size());
			
			
			Intent sp = new Intent(PictureBrowser.this,
					SelectedPictureActivity2.class);
			sp.putStringArrayListExtra(SelectedPictureActivity2.PIC_PATH,
					imagePath);
			startActivity(sp);
		}
	};

	/**
	 * ���GridView��ÿһ���CheckBox�ļ���
	 */
	private OnCheckBoxClickListener ocbcl = new ImageAdapter.OnCheckBoxClickListener() {

		public void onCheckBoxClick(View view, int position, long id) {
			// TODO Auto-generated method stub
			CheckBox selectBtn = (CheckBox) view.findViewById(R.id.pb_checkBox);
			boolean checked = !selectBtn.isChecked();
			// Log.i("Eaa", "item click id="+ id+" position="+position
			// +"|"+checked);
			if (checked) {
				if (itemNo < 9) {
					Uri uri = picFolders.get(selectFold).uriArray.get(position);
					selectBtn.setChecked(checked);
					// adapter�б����Ѿ��������ͼƬ��ѡ�����
					adapter.putSelectMap(id, checked);
					selectedTree.put(id, uri);
					// Log.i("Eaa", "put id"+ id);
				} else {
					Toast.makeText(PictureBrowser.this,
							getResources().getString(R.string.nomorethan9),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				selectBtn.setChecked(false);
				// adapter�б����Ѿ��������ͼƬ��ѡ�����
				adapter.putSelectMap(id, false);
				selectedTree.remove(id);
				// Log.i("Eaa", "remove id"+ id);
			}
			itemNo = selectedTree.size();
			if (hasViedo) {
				itemNo++;
			}
			if (titleRightButton.getVisibility() == View.GONE && itemNo > 0) {
				titleRightButton.setBackgroundColor(0x8866cc00);
				titleRightButton.startAnimation(inAlphaAni);
				titleRightButton.setVisibility(View.VISIBLE);
			} else if (titleRightButton.getVisibility() == View.VISIBLE
					&& itemNo == 0) {
				titleRightButton.startAnimation(outAlphaAni);
				titleRightButton.setVisibility(View.GONE);
			}

			CharSequence text = itemNo + "/9";
			tvPreview.setText(text);
		}
	};

	/**
	 * @author Eaa һ��ͼƬ�ļ���
	 */
	class PicFolder {
		ArrayList<Uri> uriArray = new ArrayList<Uri>();// ���ͼƬURI
		ArrayList<Long> origIdArray = new ArrayList<Long>();// ���ͼƬID
		String dirName;
		long dirId;
		long coverId;
		int picNum;

		public PicFolder(String dirName, long id, long cover, int picNum) {
			this.dirName = dirName;
			this.dirId = id;
			this.coverId = cover;
			this.picNum = picNum;
		}
	}
}
