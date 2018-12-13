package com.trackersurvey.db;

import java.util.ArrayList;

import com.trackersurvey.entity.LanguageData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LanguageDBHelper {
	private static final String DATABASE_NAME = "LANGUAGE_DB"; // ���ݿ�����
	private static final int DATABASE_VERSION = 1; // ���ݿ�汾��
	private static final String TABLE1_NAME = "LANGUAGE"; // ����
	private static final String[] COLUMNS1 = { "Language" };
	private DBOpenHelper dbhelper = null;
	private ArrayList<String> language;
	
	private static class DBOpenHelper extends SQLiteOpenHelper {
		// ����1�ű�
		private static final String CREATE_TABLE1 = "create table " + TABLE1_NAME + "(" + COLUMNS1[0]
				+ " integer NOT NULL ," + COLUMNS1[1] + " text)";
		// private static final String CREATE_TABLE5="create table
		// "+TABLE5_NAME+"("+COLUMNS5[0]+
		// " integer NOT NULL ,"+COLUMNS5[1]+" text)";

		private static DBOpenHelper helper = null;

		public DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

		}

		public static DBOpenHelper getInstence(Context context) {
			if (null == helper) {
				helper = new DBOpenHelper(context);
			}
			return helper;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(CREATE_TABLE1);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("drop table if exists " + TABLE1_NAME);// ɾ���ɰ���
		}
	}
	public LanguageDBHelper(Context context){
		dbhelper = DBOpenHelper.getInstence(context);
	}
	public void insertLanguage(LanguageData data){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(COLUMNS1[0], data.getLanguage());
			values.put(COLUMNS1[1], data.getCode());
			db.insert(TABLE1_NAME, null, values);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			db.close();
		}
		db.close();
	}
	public void delete(){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		db.execSQL("delete from LANGUAGE");
		db.close();
	}
	private ArrayList<String> getLanguage(){
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		language = new ArrayList<String>();
		Cursor cursor = db.rawQuery("select * from DURATION", null);
		int row = cursor.getCount();
		if(row!=0){
			cursor.moveToFirst();
			for(int i = 0;i<cursor.getCount();i++){
				//ȡ����Ϊ1���е�����
				language.add(cursor.getString(1));
				//Log.i("cursor", cursor.getString(1));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return language;
	}
	public Cursor select(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy){
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE1_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}
}
