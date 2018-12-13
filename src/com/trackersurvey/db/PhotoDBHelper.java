/**
 * 
 */
package com.trackersurvey.db;


import com.trackersurvey.entity.CommentMediaFiles;
import com.trackersurvey.entity.InterestMarkerData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Eaa
 * @version 2015��11��30�� ����4:29:47
 */
public class PhotoDBHelper {
	public static final int DBREAD = 1;
	public static final int DBWRITE = 2;

	private static final String DB_NAME = "happyNavi.db";
	private static final int DB_VERSION = 3;

	private static final String USEREVENT_TABLE = "UserEvent";
	public  static final String[] COLUMNS_UE = { "CreateTime", "Longitude",
			"Latitude", "Altitude", "PlaceName", "Context", "TraceNo",
			"FileNum", "Video", "Audio", "UserID","Feeling","Behaviour",
			"Duration","Companion","Relationship"};

	private static final String FILE_TABLE = "EventFile";
	public static final String[] COLUMNS_FILE = { "FileID", "FileName",
			"CreateTime", "FileType", "ThumbnailName" };

	private DBOpenHelper helper;
	private SQLiteDatabase dbRead;
	private SQLiteDatabase dbWrite;

	private static class DBOpenHelper extends SQLiteOpenHelper {
		private static final String CREATE_USEREVENT = "create table "
				+ USEREVENT_TABLE + "(" + COLUMNS_UE[0] + " datetime,"
				+ COLUMNS_UE[1] + " real not null," + COLUMNS_UE[2]
				+ " real not null," + COLUMNS_UE[3] + " real not null,"
				+ COLUMNS_UE[4] + " text ," + COLUMNS_UE[5] + " text , "
				+ COLUMNS_UE[6] + " integer not null," + COLUMNS_UE[7]
				+ " integer not null, " + COLUMNS_UE[8] + " integer,"
				+ COLUMNS_UE[9] + " integer," + COLUMNS_UE[10] + " text, "
				+ COLUMNS_UE[11] + " integer default 0, "
				+ COLUMNS_UE[12] + " integer default 0,"
				+ COLUMNS_UE[13] + " integer default 0,"
				+ COLUMNS_UE[14] + " integer default 0,"
				+ COLUMNS_UE[15] + " integer default 0,"
				+ " PRIMARY KEY(" + COLUMNS_UE[0] + "," + COLUMNS_UE[10]
				+ "));";
		private static final String CREATE_EVENTFILE = "CREATE TABLE "
				+ FILE_TABLE + "(" + COLUMNS_FILE[0] + " integer ,"
				+ COLUMNS_FILE[1] + " TEXT ," + COLUMNS_FILE[2]
				+ " datetime NOT NULL," + COLUMNS_FILE[3]
				+ " INTEGER NOT NULL," + COLUMNS_FILE[4]
				+ " TEXT , PRIMARY KEY(" + COLUMNS_FILE[0] + ","
				+ COLUMNS_FILE[2] + "),FOREIGN KEY(" + COLUMNS_FILE[1]
				+ ") REFERENCES " + USEREVENT_TABLE + "(" + COLUMNS_UE[0]
				+ "));";

		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(CREATE_USEREVENT);
			db.execSQL(CREATE_EVENTFILE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			//db.execSQL("drop table if exists " + USEREVENT_TABLE);// ɾ���ɰ���
			//onCreate(db); // �������
			if(oldVersion == 1){
				db.execSQL("ALTER TABLE "+USEREVENT_TABLE+" RENAME TO "+USEREVENT_TABLE+"_TEMP");
				db.execSQL(CREATE_USEREVENT);
				db.execSQL("INSERT INTO "+USEREVENT_TABLE+"("+COLUMNS_UE[0]+","
						+COLUMNS_UE[1]+","+COLUMNS_UE[2]+","+COLUMNS_UE[3]+","
						+COLUMNS_UE[4]+","+COLUMNS_UE[5]+","+COLUMNS_UE[6]+","
						+COLUMNS_UE[7]+","+COLUMNS_UE[8]+","+COLUMNS_UE[9]+","
						+COLUMNS_UE[10]+")"+" SELECT "+COLUMNS_UE[0]+","
						+COLUMNS_UE[1]+","+COLUMNS_UE[2]+","+COLUMNS_UE[3]+","
						+COLUMNS_UE[4]+","+COLUMNS_UE[5]+","+COLUMNS_UE[6]+","
						+COLUMNS_UE[7]+","+COLUMNS_UE[8]+","+COLUMNS_UE[9]+","
						+COLUMNS_UE[10]+" FROM "+USEREVENT_TABLE+"_TEMP");
				db.execSQL("DROP TABLE "+USEREVENT_TABLE+"_TEMP");
			}else if(oldVersion == 2){
				db.execSQL("drop table if exists " + USEREVENT_TABLE);
				db.execSQL(CREATE_USEREVENT);
			}
		}

	}

	/**
	 * @param context
	 * @param operation
	 *            DBREADֻ�� DBWRITE��д
	 */
	public PhotoDBHelper(Context context, int operation) {
		helper = new DBOpenHelper(context);
		switch (operation) {
		case DBREAD:
			dbRead = helper.getReadableDatabase(); // ���ֻ�����ݿ�
			break;
		case DBWRITE:
			dbWrite = helper.getWritableDatabase(); // ��ö�д�����ݿ�
			break;
		}
	}

	public int insertEvent(InterestMarkerData event) {

		try {
			ContentValues values = new ContentValues();
			values.put(COLUMNS_UE[0], event.getCreatetime());
			values.put(COLUMNS_UE[1], event.getLongitude());
			values.put(COLUMNS_UE[2], event.getLatitude());
			values.put(COLUMNS_UE[3], event.getAltitude());
			values.put(COLUMNS_UE[4], event.getPlaceName());
			values.put(COLUMNS_UE[5], event.getContent());
			values.put(COLUMNS_UE[6], event.getTraceNo());
			values.put(COLUMNS_UE[7], event.getFileNum());
			values.put(COLUMNS_UE[8], event.getVideoCount());
			values.put(COLUMNS_UE[9], event.getAudioCount());
			values.put(COLUMNS_UE[10], event.getUserId());
			values.put(COLUMNS_UE[11], event.getFeeling());
			values.put(COLUMNS_UE[12], event.getBehaviour());
			values.put(COLUMNS_UE[13], event.getDuration());
			values.put(COLUMNS_UE[14], event.getCompanion());
			values.put(COLUMNS_UE[15], event.getRelationship());
			dbWrite.insert(USEREVENT_TABLE, null, values);
		} catch (SQLException e) {
			return -1;
		}
		return 0;
	}

	/**
	 * ɾ���¼���ͬʱɾ���¼��ļ�
	 * @param where
	 * @return
	 */
	public int deleteEvent(String where) {
		try {
			dbWrite.delete(USEREVENT_TABLE, where, null);
			deleteFiles(where);
		} catch (SQLException e) {
			return -1;
		}
		return 0;
	}

	// ���û����в�ѯ
	public Cursor selectEvent(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = dbRead.query(USEREVENT_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * ���뵽file��
	 * @param files
	 * @return
	 */
	public int inserFile(CommentMediaFiles files) {
		try {
			ContentValues values = new ContentValues();
			values.put(COLUMNS_FILE[0], files.getFileNo());
			values.put(COLUMNS_FILE[1], files.getFileName());
			values.put(COLUMNS_FILE[2], files.getDateTime());
			values.put(COLUMNS_FILE[3], files.getType());
			values.put(COLUMNS_FILE[4], files.getThumbnailName());
			dbWrite.insert(FILE_TABLE, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			return -1;
		}
		return 0;
	}

	/**
	 * ��ѯ�ļ�
	 * 
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public Cursor selectFiles(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = dbRead.query(FILE_TABLE, columns, selection,
				selectionArgs, groupBy, having, orderBy);
		return cursor;
	}

	/**
	 * ɾ����ӦCreateTime�������ļ�
	 * @param where
	 * @return
	 */
	public int deleteFiles(String where) {
		try {
			return dbWrite.delete(FILE_TABLE, where, null);
		} catch (SQLException e) {
			return -1;
		}
	}
	
	/**
	 * ����file��
	 * @param values
	 * @param where
	 * @return
	 */
	public int updateFile(ContentValues values,String where){
		try{
			return dbWrite.update(FILE_TABLE, values, where, null);
		}catch(SQLException e){
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * 
	 * @param dateTime  Ҫɾ�����¼��Ĵ���ʱ��
	 * @param userId    Ҫɾ�����¼��������û�
	 * @return
	 */
	public int deleteEvent(String dateTime,String userId){
		try{
			String UEwhereClause = COLUMNS_UE[0]+"=? and "+COLUMNS_UE[10]+"=?";
			String[] UEwhereArgs = {dateTime,userId};
			dbWrite.delete(USEREVENT_TABLE, UEwhereClause,UEwhereArgs );
			
			String FileWhere = COLUMNS_FILE[2]+"=?";
			String[] FileArgs = {dateTime};
			
			dbWrite.delete(FILE_TABLE, FileWhere, FileArgs);
		}catch(SQLException e){
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	public int deleteEvent(String startTime,String endTime,String userId){
		try{
			String UEwhereClause = COLUMNS_UE[0]+" between ? and ? and "+COLUMNS_UE[10]+"=?";
			String[] UEwhereArgs = {startTime,endTime,userId};
			dbWrite.delete(USEREVENT_TABLE, UEwhereClause,UEwhereArgs );
			
			String FileWhere = COLUMNS_FILE[2]+" between ? and ? ";
			String[] FileArgs = {startTime,endTime};
			
			dbWrite.delete(FILE_TABLE, FileWhere, FileArgs);
		}catch(SQLException e){
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}

	/*
	 * �ر����ݿ�
	 */
	public void closeDB() {
		if (dbWrite != null) {
			dbWrite.close();
		}
		if (dbRead != null) {
			dbRead.close();
		}
	}
}
