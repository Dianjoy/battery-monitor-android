package com.dianjoy.batterymonitor.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "batterymessage.db";
	private static final int VERSION = 1;
	private String tableName;
	private String rows;
	public DBHelper (Context c, String table, String kind) {
		super(c, DB_NAME, null, VERSION);
		tableName = table;
		rows = kind;
	}
	public DBHelper (Context c, String table) {
	    super(c,DB_NAME, null, VERSION);
	    tableName = table;
	    rows = Cons.BATTERY_TIME + " datatime not null, " + Cons.BATTERY_LEVEL + " integer not null, "+ Cons.BATTERY_STATUSES +"status varchar(2) not null";
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + 
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + rows + ")");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("ALTER TABLE " + tableName + "ADD COLUMN other STRING");;
	}
}

