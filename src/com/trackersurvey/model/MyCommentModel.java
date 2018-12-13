package com.trackersurvey.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Comment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.db.PointOfInterestDBHelper;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.DownThumbData;
import com.trackersurvey.entity.InterestMarkerData;
import com.trackersurvey.entity.CommentMediaFiles;
import com.trackersurvey.entity.ListItemData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.fragment.ShowTraceFragment;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.httpconnection.DeleteCloudComment;
import com.trackersurvey.httpconnection.GetAlbum;
import com.trackersurvey.httpconnection.GetCloudPicture;
import com.trackersurvey.httpconnection.GetComment;
import com.trackersurvey.httpconnection.GetThumbPic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

public class MyCommentModel {
	private Context context;
	private ArrayList<HashMap<String, Object>> items; // �����б�Ԫ����
	private DownCommentListener mDownComment;
	private DownFileListener mDownFile;
	private DeleteCommentListener mDeleteComment;
	private ChangeBackgroudListener mChangeBackgroud;
	private DownThumbFileListener mDownThumbFile;

	// ���ݿ������
	private PhotoDBHelper dbHelper;
	private PointOfInterestDBHelper poiHelper;
	private Cursor cursor = null;

	private int numOfUE = 0; // ���û����ص�������
	private String bgImageName = "bgImage.jpg";
	private  final int listOnView = 10; // �б�������ʼ���ֵ
	private  boolean cloudMore = true; // �ƶ��Ƿ��и�������
	private  int listOneTime = 5; // һ�������б�����

	//�Ƿ�����list������ݣ�����ǣ�������Ĳ���ȡ��
	private boolean isAddingComment = false;
	//dbHelper�Ƿ����
	private boolean isDBReady = false;
	private String from = null;//�ж���˭�����˴���
	private String startTime;
	private String endTime;
	public ArrayList<HashMap<String, Object>> getItems() {
		return items;
	}

	/**
	 * ��ȡ��һ��ı�����Ƭ
	 * 
	 * @return
	 */
	public String getFirst() {
		return bgImageName;
	}

	/**
	 * ��ȡ���һ����ʾ
	 * 
	 * @return
	 */
	public boolean cloudMore() {
		return cloudMore;
	}

	/**
	 * ��ȡһ������
	 * 
	 * @param position
	 * @return
	 */
	public InterestMarkerData getComment(int position) {
		return ((ListItemData) items.get(position).get("listItem")).getEvent();
	}

	/**
	 * ��ȡһ���ļ�
	 */
	public CommentMediaFiles getFile(int listPosition, int gridPosition) {
		return ((ListItemData) items.get(listPosition).get("listItem"))
				.getFiles()[gridPosition];
	}

	/**
	 * ������������
	 * 
	 * @param mDownComment
	 */
	public void setmDownComment(DownCommentListener mDownComment) {
		this.mDownComment = mDownComment;
	}

	/**
	 * �����ļ�����
	 * 
	 * @param mDownFile
	 */
	public void setmDownFile(DownFileListener mDownFile) {
		this.mDownFile = mDownFile;
	}

	/**
	 * ��������ɾ��
	 * 
	 * @param mDeleteComment
	 */
	public void setmDeleteComment(DeleteCommentListener mDeleteComment) {
		this.mDeleteComment = mDeleteComment;
	}

	/**
	 * ���������ı�
	 * 
	 * @param mChangeBackgroud
	 */
	public void setmChangeBackgroud(ChangeBackgroudListener mChangeBackgroud) {
		this.mChangeBackgroud = mChangeBackgroud;
	}

	/**
	 * ��������ͼ����
	 */
	public void setmDownThumbFile(DownThumbFileListener mDownThumb) {
		this.mDownThumbFile = mDownThumb;
	}

	/**
	 * ����ģ��ʱ��ʼ������
	 */
	public MyCommentModel(Context context,String from) {
		this.context = context;
		items = new ArrayList<HashMap<String, Object>>();
		selectDB();
		this.from = from;
		if(from.equals("album")){
			initItems();
		}else if(from.equals("mark")){
			
		}
	}

	/**
	 * ��ʼ���б�����
	 */
	private void initItems() {
		isAddingComment = true;
		items.removeAll(items);
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		
		cursor = dbHelper.selectEvent(null, PhotoDBHelper.COLUMNS_UE[10] + "="
				+ Common.getUserId(context), null, null, null, "datetime("
				+ PhotoDBHelper.COLUMNS_UE[0] + ") desc");
		
		// �������б���
		listAddBackgroud(bgImageName);
		// �������ݿ�����Ŀ
		numOfUE = cursor.getCount();
		if (numOfUE > 0) {
			while (items.size() - 1 <= listOnView && !cursor.isLast()) {
				items.add(listAddGrid());
			}
		}
		
		// ����ĩ����ʾ
		listAddHint();
		//cursor.close();
		isAddingComment = false;
	}
	/**
	 * ����ʱ������
	 * */
	public void setTimeRegion(String from,String to){
		startTime = from;
		endTime = to;
		Log.i("starttoend", "start3:"+from+"; end3:"+to);
	}
	public void initMarkerItemsOnline(){
		
		downloadComment(Common.currentTime());
//		downloadAlbum(Common.currentTime());
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		cursor = dbHelper.selectEvent(null, PhotoDBHelper.COLUMNS_UE[10] + "="
				+ Common.getUserId(context), null, null, null, "datetime("
				+ PhotoDBHelper.COLUMNS_UE[0] + ") desc");
		Log.i("itemsss", "cursor:"+cursor.getCount());
		Log.i("itemsss", "MyCommentModel���items:"+items.toString());
		Log.i("initMarkerItemsOnline", "initMarkerItemsOnline()����");
		Log.i("starttoend", "start2:"+startTime+"; end2:"+endTime);
		
	}
	public void initMarkerItemsFromDB(){
		initItemsByTime(startTime,endTime);
	}
/**
 * ��ѯ�̶�ʱ����ڵı�ǣ����ۣ���Ϣ
 * 
 * P.S.���ݿ�������Ϣʱ�ſɲ�ѯ
 * */
	private void initItemsByTime(String from,String to){
		items.removeAll(items);
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		cursor = dbHelper.selectEvent(null, PhotoDBHelper.COLUMNS_UE[10] + "="
				+ Common.getUserId(context)+" and datetime("
						+ PhotoDBHelper.COLUMNS_UE[0] + ") between '"+from+
						"' and '"+to+"'", null, null, null, null);
		
		numOfUE = cursor.getCount();
		if (numOfUE > 0) {
			while (!cursor.isLast()) {
				items.add(listAddGrid());
			}
		}
		Log.i("mark", "cursor.getCount() : "+cursor.getCount()+",items size : "+items.size());
	}
	/**
	 * �����ݿ��ѯ���� 
	 */
	private void selectDB() {
		dbHelper = new PhotoDBHelper(context, PhotoDBHelper.DBREAD);
		poiHelper = new PointOfInterestDBHelper(context);
		isDBReady = true;
	}

	/**
	 * �����ݿ��ѯ���ݲ�����items
	 * 
	 * @return
	 */
	private HashMap<String, Object> listAddGrid() {
		cursor.moveToNext();
		InterestMarkerData event = new InterestMarkerData();
		String eventTime = cursor.getString(0);
		event.setCreatetime(eventTime);
		event.setLongitude(cursor.getDouble(1));
		event.setLatitude(cursor.getDouble(2));
		event.setAltitude(cursor.getDouble(3));
		event.setPlaceName(cursor.getString(4));
		event.setContent(cursor.getString(5));
		event.setTraceNo(cursor.getLong(6));
		event.setFileNum(cursor.getInt(7));
		event.setVideoCount(cursor.getInt(8));
		event.setAudioCount(cursor.getInt(9));
		event.setUserId(cursor.getString(10));
		event.setFeeling(cursor.getInt(11));
		event.setBehaviour(cursor.getInt(12));
		event.setDuration(cursor.getInt(13));
		event.setCompanion(cursor.getInt(14));
		event.setRelationship(cursor.getInt(15));
		Cursor fileCursor = dbHelper.selectFiles(null, "datetime("
				+ PhotoDBHelper.COLUMNS_FILE[2] + ")=datetime('" + eventTime
				+ "')", null, null, null, null);
		CommentMediaFiles files[] = new CommentMediaFiles[fileCursor.getCount()];
		 Log.i("album", "���� = " + cursor.getString(5)+",traceNo:"+cursor.getLong(6)
		 		+",filenum:"+cursor.getInt(7)+ ",filesdb :" + files.length);
		int index = 0;
		while (fileCursor.moveToNext()) {
			files[index] = new CommentMediaFiles(fileCursor.getInt(0),
					fileCursor.getString(1), fileCursor.getString(2),
					fileCursor.getInt(3), fileCursor.getString(4));
			// Log.i("Eaa_fileCursor", "" + index);
			index++;
		}
		HashMap<String, Object> listItem = new HashMap<String, Object>();
		ListItemData data = new ListItemData(event, files);        //��Ȥ���б��������͵�����data
		listItem.put("listItem", data);
		fileCursor.close();
		return listItem;
	}

	/**
	 * ��List������������
	 */
	private void listAddBackgroud(String path) {
		HashMap<String, Object> listItem = new HashMap<String, Object>();
		listItem.put("listItem", path);
		items.add(0, listItem);
	}

	/**
	 * ��List����ĩ����ʾ
	 */
	private void listAddHint() {
		String hint = cloudMore ? "more" : "nomore";
		// ����һ����ʾ��
		HashMap<String, Object> listItem = new HashMap<String, Object>();
		listItem.put("listItem", hint);
		items.add(listItem);
	}

	/**
	 * �ر����е�����
	 */
	public void stopModel() {
		/*
		 * ���������closeDB,�ں�̨��������ʱ�˳�activity������Ļص��е���dbHelper�ᵼ�µ����ѹرյ�dbHelper��
		 * Ӧ�ñ�����������ΪdbHelper�ںܶ�ط�ʹ�ã�����������Ҳ������ʵ�λ�ùرգ�Ϊ��Ӧ�ò�������ע�͵���һ�С�
		 */
//		dbHelper.closeDB();
		
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		isDBReady = false;
		dbHelper.closeDB();
		cloudMore = true;
		Log.i("album", "stopModel :close cursor,db");
	}

	/**
	 * ������ݵ�ListView��β��
	 */
	public void autoAddtoList() {
		if (!isAddingComment) {
			Log.i("Eaa", "addComment:"+items.size());
			isAddingComment = true;
			// ��¼��ǰ��������λ��
			int addNum = 0;
			if (items.size() - 2 < numOfUE) {// -2��ԭ�� items�а������������͵ײ���more/nomore����ȥ��������������������
				while (!cursor.isLast() && addNum < listOneTime) {
					items.add(items.size() - 1, listAddGrid());//��֤���һ��Ԫ��ʼ���� hint
					addNum++;
				}
			}
			// ������ӵĲ���listOneTime������û�и�������
			if (addNum < listOneTime - 1) {
				// cloudMoreΪtrue�ƶ��и������ݣ������ƶ�
				if (cloudMore) {
					String lastTime;
					if (numOfUE > 0) {
						lastTime = ((ListItemData) items.get(items.size() - 2)
								.get("listItem")).getTime();
					} else {
						lastTime = Common.currentTime();
					}
					GetAlbum gct = new GetAlbum(requestAlbum,
							Common.URL_DOWNEVENT, Common.getUserId(context),
							lastTime,Common.getDeviceId(context),"no");
					gct.start();
					/*GetComment gct = new GetComment(requestComment,
							Common.URL_DOWNEVENT, Common.getUserId(context),
							lastTime,Common.getDeviceId(context));
					gct.start();*/
				} else {// �ƶ�û�и�������
					// �������۽�����֪ͨUI�̸߳�����ʾ�ı�
					items.get(items.size() - 1).put("listItem", "nomore");
					mDownComment.onCommentDownload(0);
					isAddingComment= false;
				}
			}else{
				mDownComment.onCommentDownload(0);
				isAddingComment= false;
			}
		}

	}

	/**
	 * ��������
	 * 
	 * @param dateTime
	 */
	public void downloadComment(String dateTime) {
		isAddingComment = true;
		GetComment downComment = new GetComment(refreshComment,
				Common.URL_DOWNEVENT, Common.getUserId(context),
				startTime, endTime, Common.getDeviceId(context));
		/*GetComment downComment = new GetComment(refreshComment,
				Common.URL_DOWNEVENT, Common.getUserId(context),
				dateTime,Common.getDeviceId(context));*/
		Log.i("Eaa", "downloadComment:" + dateTime);
		downComment.start();
		
	}
	/**
	 * �������
	 * 
	 * @param dateTime
	 */
	public void downloadAlbum(String dateTime){
		isAddingComment = true;
		GetAlbum downAlbum = new GetAlbum(refreshAlbum,
				Common.URL_DOWNEVENT, Common.getUserId(context),
				dateTime, Common.getDeviceId(context), "yes");
		Log.i("Eaa", "downloadaAlbum:"+dateTime);
		downAlbum.start();
		
	}

	/**
	 * �����ļ�
	 * 
	 * @param type
	 * @param filePosition
	 * @param listPosition
	 */
	public void downloadFile(int listPosition, int filePosition, int type) {
		RequestFile rf = new RequestFile(listPosition, filePosition, type);
		String commmentId = ((ListItemData) items.get(listPosition).get(
				"listItem")).getTime();
		GetCloudPicture gcp = new GetCloudPicture(rf, Common.URL_DOWNFILE,
				Common.getUserId(context), commmentId, "" + filePosition,
				Common.getDeviceId(context));
		Log.i("Eaa", "downloadFile:" + commmentId);
		gcp.start();
	}

	/**
	 * ��������ͼ
	 * 
	 * @param position
	 */
	public void downloadThumbFile(int position, String dateTime) {
		GetThumbPic getThumb = new GetThumbPic(new RequestThembFiles(position,
				dateTime), Common.URL_DOWNEVENT, Common.getUserId(context),
				dateTime,Common.getDeviceId(context));
		getThumb.start();
	}

	/**
	 * ɾ������
	 * 
	 * @param listPosition
	 * @param dateTime
	 */
	public boolean deleteComment(String dateTime, int listPosition) {
		DeleteCloudComment dcc = new DeleteCloudComment(context,
				new DeleteCloudEvent(listPosition), Common.URL_DELETEEVENT,
				Common.getUserId(context), dateTime,Common.getDeviceId(context));
		dcc.start();
		return false;
	}
	public boolean deleteComment(String startTime,String endTime) {
		DeleteCloudComment dcc = new DeleteCloudComment(context,
				new DeleteCloudEvent(), Common.URL_DELETEEVENT,
				Common.getUserId(context), startTime,endTime,Common.getDeviceId(context));
		dcc.start();
		return false;
	}
	/**
	 * �޸ı���
	 * 
	 * @param bgImageName
	 */
	public void changeBackgroud(String bgImageName) {
		items.remove(0);
		listAddBackgroud(bgImageName);
		mChangeBackgroud.onBackgroudChanged();
	}

	/**
	 * �������۵ļ�����
	 */
	public interface DownCommentListener {
		public void onCommentDownload(int msg);
	}

	/**
	 * �����ļ��ļ�����
	 */
	public interface DownFileListener {
		public void onFileDownload(int msg, int listPosition, int filePosition);
	}

	/**
	 * ��������ͼ�ļ�����
	 */
	public interface DownThumbFileListener {
		public void onThumbFileDownload(int msg, int listPosition,
				ArrayList<HashMap<String, String>> newThumbs);
	}

	/**
	 * ɾ�����۵ļ�����
	 */
	public interface DeleteCommentListener {
		public void onCommentDeleted(int msg);
	}

	/**
	 * �޸ı����ļ�����
	 */
	public interface ChangeBackgroudListener {
		public void onBackgroudChanged();
	}

	/**
	 * ����һ��������ɺ�Ļص� ���ƶ����ص�����û�е����۲��뵽���ݿ⣬����items
	 */
	private Handler requestComment = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				CloudComment cloudComment = GsonHelper.parseJson(msg.obj
						.toString().trim(), CloudComment.class);
				if (cloudComment == null) {
					mDownComment.onCommentDownload(-1);
					isAddingComment = false;
					break;
				}
				LinkedList<InterestMarkerData> events = cloudComment.getEvent();

				String more = "";
				more = cloudComment.getMore();
				if (more.equals("no")) {
					cloudMore = false;
				} else {
					cloudMore = true;
				}

				// ���һ����д�����ݿ⣬���в���ɾ������
				PhotoDBHelper writedDbHelper = new PhotoDBHelper(context,
						PhotoDBHelper.DBWRITE);
				// ͬ���ƶ˺���������
				int eventsNum = events.size();

				// ������û�еļ��в������ݿ�
				for (int i = 0; i < eventsNum; i++) {
					writedDbHelper.insertEvent(events.get(i));
					int fileNum = events.get(i).getFileNum();
					// �����ļ���
					for (int j = 0; j < fileNum; j++) {
						CommentMediaFiles ev = new CommentMediaFiles();
						ev.setDateTime(events.get(i).getCreatetime());
						ev.setFileNo(j);
						ev.setType(CommentMediaFiles.TYPE_PIC);
						writedDbHelper.inserFile(ev);
					}
				}

				// ���²�ѯ��������
				if(cursor != null && !cursor.isClosed()){
					cursor.close();
				}
				cursor = dbHelper.selectEvent(null,
						PhotoDBHelper.COLUMNS_UE[10] + "=" + Common.getUserId(context),
						null, null, null, "datetime("
								+ PhotoDBHelper.COLUMNS_UE[0] + ") desc");
				cursor.moveToPosition(numOfUE - 1);
				numOfUE = cursor.getCount();

				// ��¼��ǰ��������λ��
				// int setPosition = lView.getFirstVisiblePosition() + 1;
				int addNum = 0;
				while (!cursor.isLast() && addNum < listOneTime) {
					items.add(items.size() - 1, listAddGrid());
					addNum++;
				}

				if (!cloudMore) {
					// ɾ��ĩ����ʾ������Ը�����ʾ�ı�
					items.get(items.size() - 1).put("listItem", "nomore");
				}
				//cursor.close();
				writedDbHelper.closeDB();
				break;
			}
			default:
				break;

			}
			isAddingComment = false;
			mDownComment.onCommentDownload(msg.what);
		}
	};

	private Handler requestAlbum = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				CloudComment cloudComment = GsonHelper.parseJson(msg.obj
						.toString().trim(), CloudComment.class);
				if (cloudComment == null) {
					mDownComment.onCommentDownload(-1);
					isAddingComment = false;
					break;
				}
				LinkedList<InterestMarkerData> events = cloudComment.getEvent();

				String more = "";
				more = cloudComment.getMore();
				Log.i("cloudComment", "requestAlbum:"+cloudComment.getMore());
				if (more.equals("no")) {
					cloudMore = false;
				} else {
					cloudMore = true;
				}

				// ���һ����д�����ݿ⣬���в���ɾ������
				PhotoDBHelper writedDbHelper = new PhotoDBHelper(context,
						PhotoDBHelper.DBWRITE);
				// ͬ���ƶ˺���������
				int eventsNum = events.size();

				// ������û�еļ��в������ݿ�
				for (int i = 0; i < eventsNum; i++) {
					writedDbHelper.insertEvent(events.get(i));
					int fileNum = events.get(i).getFileNum();
					// �����ļ���
					for (int j = 0; j < fileNum; j++) {
						CommentMediaFiles ev = new CommentMediaFiles();
						ev.setDateTime(events.get(i).getCreatetime());
						ev.setFileNo(j);
						ev.setType(CommentMediaFiles.TYPE_PIC);
						writedDbHelper.inserFile(ev);
					}
				}

				// ���²�ѯ��������
				if(cursor != null && !cursor.isClosed()){
					cursor.close();
				}
				cursor = dbHelper.selectEvent(null,
						PhotoDBHelper.COLUMNS_UE[10] + "=" + Common.getUserId(context),
						null, null, null, "datetime("
								+ PhotoDBHelper.COLUMNS_UE[0] + ") desc");
				cursor.moveToPosition(numOfUE - 1);
				numOfUE = cursor.getCount();

				// ��¼��ǰ��������λ��
				// int setPosition = lView.getFirstVisiblePosition() + 1;
				int addNum = 0;
				while (!cursor.isLast() && addNum < listOneTime) {
					items.add(items.size() - 1, listAddGrid());
					addNum++;
				}

				if (!cloudMore) {
					// ɾ��ĩ����ʾ������Ը�����ʾ�ı�
					items.get(items.size() - 1).put("listItem", "nomore");
				}
				//cursor.close();
				writedDbHelper.closeDB();
				break;
			}
			default:
				break;

			}
			isAddingComment = false;
			mDownComment.onCommentDownload(msg.what);
		}
	};
	
	/**
	 * ɾ��һ�����ۺ�Ļص�
	 */
	class DeleteCloudEvent extends Handler {
		private int position = -1;

		public DeleteCloudEvent(int position) {
			this.position = position;
		}
		public DeleteCloudEvent() {
			
		}
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // ɾ���ɹ�
				if(position > -1){
					TraceDBHelper traceHelper = new TraceDBHelper(context);
					TraceData tracedata = new TraceData();
					InterestMarkerData comment = new InterestMarkerData();
					comment = ((ListItemData)items.get(position).get("listItem")).getEvent();
					long traceNo = comment.getTraceNo();
					String userID = comment.getUserId();
					tracedata = traceHelper.queryfromTrailbytraceNo(traceNo,userID);
					if(tracedata!=null){
						tracedata.setPoiCount(tracedata.getPoiCount()-1);
						traceHelper.updatetrail(tracedata, traceNo, userID);						
					}
					items.remove(position);
				}
				numOfUE--;
				break;
			}
			default:
				break;
			}
			// ֪ͨUI�߳�ɾ�����
			mDeleteComment.onCommentDeleted(msg.what);
		}
	};

	/**
	 * ˢ������������ɺ�Ļص� �����������ɹ���ͬ������
	 */
	private Handler refreshComment = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				CloudComment cloudComment = GsonHelper.parseJson(msg.obj
						.toString().trim(), CloudComment.class);
				if (cloudComment == null) {
					mDownComment.onCommentDownload(-1);
					isAddingComment = false;
					break;
				}
				LinkedList<InterestMarkerData> events = cloudComment.getEvent();
				Log.i("msg.objjj", msg.obj.toString());
				
				String more = "";
				more = cloudComment.getMore();
				if (more.equals("no")) {
					cloudMore = false;
				} else {
					cloudMore = true;
				}

				int eventsNum = events.size();
				// ͬ���ƶ˺���������
				int index = 0;
				cursor.moveToFirst();
				// ���һ����д�����ݿ⣬���в���ɾ������
				PhotoDBHelper writedDbHelper = new PhotoDBHelper(context,
						PhotoDBHelper.DBWRITE);
				String eventCTDB;
				String eventCTCloud;
				// ����������������,
				if (cursor.getCount() > 0) {
					// �ƶ�Ҳ������
					if (eventsNum > 0) {
						while (index < eventsNum) {
							eventCTDB = cursor.getString(0);
							eventCTCloud = events.get(index).getCreatetime();
							//Log.i("album", ""+eventCTDB+","+eventCTCloud);
							long leventCTDB = Common.timeStamp(eventCTDB);
							long leventCTCloud = Common.timeStamp(eventCTCloud);
							if (leventCTDB == leventCTCloud) {
								// ���ݿ��е�һ�к��ƶ�һ�����֤����һ�������Ѿ�ͬ�������ݿ���ƶ����ݽ�ǰ��һ��
								 Log.i("Eaa_equal", "" + eventCTDB);
								index++;
								if (!cursor.moveToNext()) {
									break;
								}
							} else if (leventCTDB > leventCTCloud) {
								// ���ݿ���һ�д����ƶ˵�һ�У����������Ѿ��������豸ɾ����ɾ�����ݿ�����һ��,�ƶ˲�ǰ��
								writedDbHelper
										.deleteEvent("datetime(CreateTime) = datetime('"
												+ eventCTDB + "')");
								 Log.i("Eaa_delete",
								 "getComment delete event:"
								 + eventCTDB);
								if (!cursor.moveToNext()) {
									break;
								}
							} else {
								// ���ݿ��е�һ��С���ƶ˵�һ�У��ƶ�һ�б���û�У��������,���ݿ�cursor��ǰ��
								writedDbHelper.insertEvent(events.get(index));
								 Log.i("Eaa_insert",
								 "getComment insert event:"
								 + eventCTCloud);
								// �����ļ���
								int fileNum = events.get(index).getFileNum();
								for (int j = 0; j < fileNum; j++) {
									CommentMediaFiles ev = new CommentMediaFiles();
									ev.setDateTime(events.get(index)
											.getCreatetime());
									ev.setFileNo(j);
									ev.setType(CommentMediaFiles.TYPE_PIC);
									writedDbHelper.inserFile(ev);
								}
								index++;
							}
						}
						if (!cursor.isAfterLast() && !cloudMore) {
							// ����������ݶ����ƶˣ�ɾ���������ݶ���Ĳ���

							do {
								eventCTDB = cursor.getString(0);
								writedDbHelper
										.deleteEvent("datetime(CreateTime) = datetime('"
												+ eventCTDB + "')");

							} while (cursor.moveToNext());
						}

					} else { // ����ƶ�û�����ݣ�ɾ��������������
						writedDbHelper.deleteEvent(null);
					}
				}

				// ������û�еļ��в������ݿ�
				for (int i = index; i < eventsNum; i++) {
					writedDbHelper.insertEvent(events.get(i));
					int fileNum = events.get(i).getFileNum();
					// �����ļ���
					for (int j = 0; j < fileNum; j++) {
						CommentMediaFiles ev = new CommentMediaFiles();
						ev.setDateTime(events.get(i).getCreatetime());
						ev.setFileNo(j);
						ev.setType(CommentMediaFiles.TYPE_PIC);
						writedDbHelper.inserFile(ev);
					}
				}
				
				if(from.equals("album")){
					initItems();
				}else if(from.equals("mark")){
					initItemsByTime(startTime,endTime);
					Log.i("mark","���±�עhandler");
				}
				writedDbHelper.closeDB();
				break;
			}
			default:
				break;
			}
			isAddingComment = false;
			mDownComment.onCommentDownload(msg.what);
		}

	};
	
	private Handler refreshAlbum = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				CloudComment cloudComment = GsonHelper.parseJson(msg.obj
						.toString().trim(), CloudComment.class);
				if (cloudComment == null) {
					mDownComment.onCommentDownload(-1);
					isAddingComment = false;
					break;
				}
				LinkedList<InterestMarkerData> events = cloudComment.getEvent();
				Log.i("msg.objjj", msg.obj.toString());
				
				String more = "";
				more = cloudComment.getMore();
				Log.i("cloudComment", cloudComment.getMore());
				if (more.equals("no")) {
					cloudMore = false;
				} else {
					cloudMore = true;
				}

				int eventsNum = events.size();
				// ͬ���ƶ˺���������
				int index = 0;
				cursor.moveToFirst();
				// ���һ����д�����ݿ⣬���в���ɾ������
				PhotoDBHelper writedDbHelper = new PhotoDBHelper(context,
						PhotoDBHelper.DBWRITE);
				String eventCTDB;
				String eventCTCloud;
				// ����������������,
				if (cursor.getCount() > 0) {
					// �ƶ�Ҳ������
					if (eventsNum > 0) {
						while (index < eventsNum) {
							eventCTDB = cursor.getString(0);
							eventCTCloud = events.get(index).getCreatetime();
							//Log.i("album", ""+eventCTDB+","+eventCTCloud);
							long leventCTDB = Common.timeStamp(eventCTDB);
							long leventCTCloud = Common.timeStamp(eventCTCloud);
							if (leventCTDB == leventCTCloud) {
								// ���ݿ��е�һ�к��ƶ�һ�����֤����һ�������Ѿ�ͬ�������ݿ���ƶ����ݽ�ǰ��һ��
								 Log.i("Eaa_equal", "" + eventCTDB);
								index++;
								if (!cursor.moveToNext()) {
									break;
								}
							} else if (leventCTDB > leventCTCloud) {
								// ���ݿ���һ�д����ƶ˵�һ�У����������Ѿ��������豸ɾ����ɾ�����ݿ�����һ��,�ƶ˲�ǰ��
								writedDbHelper
										.deleteEvent("datetime(CreateTime) = datetime('"
												+ eventCTDB + "')");
								 Log.i("Eaa_delete",
								 "getComment delete event:"
								 + eventCTDB);
								if (!cursor.moveToNext()) {
									break;
								}
							} else {
								// ���ݿ��е�һ��С���ƶ˵�һ�У��ƶ�һ�б���û�У��������,���ݿ�cursor��ǰ��
								writedDbHelper.insertEvent(events.get(index));
								 Log.i("Eaa_insert",
								 "getComment insert event:"
								 + eventCTCloud);
								// �����ļ���
								int fileNum = events.get(index).getFileNum();
								for (int j = 0; j < fileNum; j++) {
									CommentMediaFiles ev = new CommentMediaFiles();
									ev.setDateTime(events.get(index)
											.getCreatetime());
									ev.setFileNo(j);
									ev.setType(CommentMediaFiles.TYPE_PIC);
									writedDbHelper.inserFile(ev);
								}
								index++;
							}
						}
						if (!cursor.isAfterLast() && !cloudMore) {
							// ����������ݶ����ƶˣ�ɾ���������ݶ���Ĳ���

							do {
								eventCTDB = cursor.getString(0);
								writedDbHelper
										.deleteEvent("datetime(CreateTime) = datetime('"
												+ eventCTDB + "')");

							} while (cursor.moveToNext());
						}

					} else { // ����ƶ�û�����ݣ�ɾ��������������
						writedDbHelper.deleteEvent(null);
					}
				}

				// ������û�еļ��в������ݿ�
				for (int i = index; i < eventsNum; i++) {
					writedDbHelper.insertEvent(events.get(i));
					int fileNum = events.get(i).getFileNum();
					// �����ļ���
					for (int j = 0; j < fileNum; j++) {
						CommentMediaFiles ev = new CommentMediaFiles();
						ev.setDateTime(events.get(i).getCreatetime());
						ev.setFileNo(j);
						ev.setType(CommentMediaFiles.TYPE_PIC);
						writedDbHelper.inserFile(ev);
					}
				}
				
				if(from.equals("album")){
					initItems();
				}else if(from.equals("mark")){
					initItemsByTime(startTime,endTime);
					Log.i("mark","���±�עhandler");
				}
				writedDbHelper.closeDB();
				break;
			}
			default:
				break;
			}
			isAddingComment = false;
			mDownComment.onCommentDownload(msg.what);
		}

	};

	/**
	 * ����һ���¼���ȫ������ͼ�¼���ɺ�Ļص��� ����ɹ����������ݿ�������ͼ�ļ�¼����֪ͨUI�߳����¼�������ͼ
	 */
	private class RequestThembFiles extends Handler {
		ArrayList<HashMap<String, String>> images;
		String createTime;
		int position;

		public RequestThembFiles(int position, String createTime) {
			this.createTime = createTime;
			this.images = new ArrayList<HashMap<String, String>>();
			this.position = position;
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				Gson gson = new Gson();
				LinkedList<DownThumbData> thumbs = new LinkedList<DownThumbData>();
				try {
					java.lang.reflect.Type fileType = new TypeToken<LinkedList<DownThumbData>>() {
					}.getType();
					thumbs = gson.fromJson(msg.obj.toString().trim(), fileType);
				} catch (Exception e) {
					mDownThumbFile.onThumbFileDownload(-1, position, images);
					break;
				}
				// Log.d("Eaa", msg.obj.toString());
				if (thumbs.size() == 0) {
					return;
				}
				PhotoDBHelper writeDBHelper = new PhotoDBHelper(context,
						PhotoDBHelper.DBWRITE);
				// ��������ͼ��gridView,ͬʱ����EventFiles ���ݿ��
				images.removeAll(images);
				int index = 0;
				for (Iterator iterator = thumbs.iterator(); iterator.hasNext();) {
					DownThumbData thumbPic = (DownThumbData) iterator.next();
					int fileType = 1;
					if (thumbPic.getFileType().equals("pic")) {
						fileType = CommentMediaFiles.TYPE_PIC;
					} else if (thumbPic.getFileType().equals("video")) {
						fileType = CommentMediaFiles.TYPE_VIDEO;
					}
					// ������ͼд�������ļ���

					byte[] picByte = Base64.decode(thumbPic.getPicByte(),
							Base64.DEFAULT);
					BufferedOutputStream bos = null;
					String imageName = Common.currentTimeMill();
					File file = new File(Common.CACHEPHOTO_PATH
							+ File.separator + thumbPic.getFileId() + imageName
							+ ".jpg");
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(file);

						/**
						 * ��������ͼ��¼��EventFiles���Ӧ�ļ�
						 */
						ContentValues cv = new ContentValues();
						cv.put(PhotoDBHelper.COLUMNS_FILE[3], fileType);
						cv.put(PhotoDBHelper.COLUMNS_FILE[4],
								file.getAbsolutePath());
						// �������ݿ��е�����ͼ
						writeDBHelper
								.updateFile(cv, "datetime("
										+ PhotoDBHelper.COLUMNS_FILE[2]
										+ ")=datetime('" + createTime
										+ "') AND "
										+ PhotoDBHelper.COLUMNS_FILE[0] + " = "
										+ index);
						bos = new BufferedOutputStream(fos);
						bos.write(picByte);
						bos.close();
						picByte = null;
						index++;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String imgPaht = file.getAbsolutePath();
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("itemImage", imgPaht);
					images.add(map);
				}

				// ����items����֤ListView��ͬ������
				Cursor fileCursor = dbHelper.selectFiles(null, "datetime("
						+ PhotoDBHelper.COLUMNS_UE[0] + ")=datetime('"
						+ createTime + "')", null, null, null, null);
				CommentMediaFiles files[] = new CommentMediaFiles[fileCursor.getCount()];
				int cursorIndex = 0;
				while (fileCursor.moveToNext()) {
					files[cursorIndex] = new CommentMediaFiles(fileCursor.getInt(0),
							fileCursor.getString(1), fileCursor.getString(2),
							fileCursor.getInt(3), fileCursor.getString(4));
					cursorIndex++;
				}
				if (position < items.size()) {
					((ListItemData) (items.get(position).get("listItem")))
							.setFiles(files);
				}
				fileCursor.close();
				writeDBHelper.closeDB();
				break;
			}
			default:

			}
			mDownThumbFile.onThumbFileDownload(msg.what, position, images);
		}
	}

	/**
	 * ����һ���ļ�
	 */
	private class RequestFile extends Handler {
		private String cloudPicture = null;
		private String createTime;
		private int listPosition;
		private int fileNo;
		private int fileType;

		public RequestFile(int listPosition, int no, int type) {
			this.listPosition = listPosition;
			this.createTime = ((ListItemData) (items.get(listPosition)
					.get("listItem"))).getTime();
			this.fileNo = no;
			this.fileType = type;
		}

		public void handleMessage(android.os.Message msg) {
			if(!isDBReady){
				Log.i("album", "db closed return");
				return;
			}
			switch (msg.what) {
			case 0: { // �ɹ������Ϣ
				// ���������ļ���ͼƬ
				if (fileType == CommentMediaFiles.TYPE_PIC) {
					String fileStr = msg.obj.toString().trim();
					if (null == fileStr || "noPic".equals(fileStr)
							|| "null".equals(fileStr)) {
						mDownFile.onFileDownload(-1, listPosition, fileNo);
						return;
					}

					Log.i("Eaa", "File downloaded:");
					byte[] picByte = Base64.decode(fileStr, Base64.DEFAULT);
					BufferedOutputStream bos = null;
					PhotoDBHelper writeDBHelper = new PhotoDBHelper(context,
							PhotoDBHelper.DBWRITE);
					String imageName = Common.currentTimeMill();

					File file = new File(Common.PHOTO_PATH + imageName
							+ "_cloud.jpg");
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(file);
						bos = new BufferedOutputStream(fos);
						bos.write(picByte);
						bos.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cloudPicture = file.getAbsolutePath();
					/**
					 * �����ƶ�ͼ��¼��EventFiles���Ӧ�ļ�
					 */
					ContentValues cv = new ContentValues();
					cv.put(PhotoDBHelper.COLUMNS_FILE[1],
							file.getAbsolutePath());
					int result = writeDBHelper.updateFile(cv, "datetime("
							+ PhotoDBHelper.COLUMNS_FILE[2] + ")=datetime('"
							+ createTime + "') AND "
							+ PhotoDBHelper.COLUMNS_FILE[0] + " = " + fileNo);
					Log.i("Eaa", "datetime(" + PhotoDBHelper.COLUMNS_FILE[2]
							+ ")=datetime('" + createTime + "') AND "
							+ PhotoDBHelper.COLUMNS_FILE[0] + " = " + fileNo);

					Log.i("Eaa", "downFile result:" + result);
					CommentMediaFiles updateFile = ((ListItemData) items.get(
							listPosition).get("listItem")).getFiles()[fileNo];
					updateFile.setFileName(cloudPicture);
					// ����list
					((ListItemData) items.get(listPosition).get("listItem"))
							.setOneFile(fileNo, updateFile);

					writeDBHelper.closeDB();
					mDownFile.onFileDownload(msg.what, listPosition, fileNo);//�޸ĺ󣬵�һ�������������Ϊ��ָ�����
					break;
				}
				// ���������ļ�����Ƶ
				else if (fileType == CommentMediaFiles.TYPE_VIDEO) {
					String fileStr = msg.obj.toString().trim();
					// Log.i("Eaa_video_url", fileStr);
					if (fileStr == "noVideo" || "null".equals(fileStr)
							|| "noPic".equals(fileStr)) {
						mDownFile.onFileDownload(-2, listPosition, fileNo);
						return;
					}

					String videoName = Common.PHOTO_PATH
							+ fileStr.substring(fileStr.lastIndexOf("/") + 1,
									fileStr.length());
					HttpUtils http = new HttpUtils();
					http.download(fileStr, videoName, true, true,
							new RequestCallBack<File>() {

								@Override
								public void onSuccess(ResponseInfo<File> arg0) {
									// TODO Auto-generated method stub
									PhotoDBHelper writeDBHelper = new PhotoDBHelper(
											context, PhotoDBHelper.DBWRITE);
									// �������ݿ����ļ��ļ�¼
									ContentValues cv = new ContentValues();
									cv.put(PhotoDBHelper.COLUMNS_FILE[1],
											arg0.result.getAbsolutePath());
									writeDBHelper.updateFile(cv, "datetime("
											+ PhotoDBHelper.COLUMNS_FILE[2]
											+ ")=datetime('" + createTime
											+ "') AND "
											+ PhotoDBHelper.COLUMNS_FILE[0]
											+ " = " + fileNo);

									CommentMediaFiles updateFile = ((ListItemData) items
											.get(listPosition).get("listItem"))
											.getFiles()[fileNo];
									updateFile.setFileName(arg0.result
											.getAbsolutePath());
									// ����list
									((ListItemData) items.get(listPosition)
											.get("listItem")).setOneFile(
											fileNo, updateFile);
									writeDBHelper.closeDB();
									mDownFile.onFileDownload(0, listPosition, fileNo);
								}

								@Override
								public void onFailure(HttpException arg0,
										String arg1) {
									// TODO Auto-generated method stub
									mDownFile.onFileDownload(-2, listPosition,
											fileNo);

								}

								@Override
								public void onLoading(long total, long current,
										boolean isUploading) {
									// TODO Auto-generated method stub
									super.onLoading(total, current, isUploading);
								}
							});
					break;
				}
			}

			default:
				mDownFile.onFileDownload(msg.what, listPosition, fileNo);//�ڶ�������ʱ���������Ϊ��ָ�����
				break;

			}
			
		}

	}

	// �����ƶ˷���Json������gson��Ӧ��
	private class CloudComment {
		private LinkedList<InterestMarkerData> List;
		String More;

		public String getMore() {
			return More;
		}

		public LinkedList<InterestMarkerData> getEvent() {
			return this.List;
		}
	}

	/*private void delay(int ms){  
        try {  
            Thread.currentThread();  
            Thread.sleep(ms);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }   
     }*/ 
	
}
