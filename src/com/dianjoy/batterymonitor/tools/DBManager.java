package com.dianjoy.batterymonitor.tools;

import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

	private DBHelper helper;
	private SQLiteDatabase db;
	private String tableName;
	public DBManager(Context c,  String tableName) {
		helper = new DBHelper(c, tableName);
		db = helper.getWritableDatabase();
		this.tableName = tableName;
	}
	public void add(int level, long time, String status) {
		db.beginTransaction();
		db.execSQL("INSERT INTO " + tableName +" VALUES(?, ? , ?)", new Object[] {
			time, level, status
		});
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	public void delete(int id) {
		db.delete(tableName, "_id<=?", new String[]{id+""});
	}
	//auto delete the raw >= Cons.MAX_COUNT(96)
	public void autoDelete() {
		Cursor c = db.query(tableName,null,null,null,null,null,null);
		int count = c.getCount() - Cons.MAX_COUNT - 1;
		if (count >= 0) {
			db.execSQL("DELETE FROM " + tableName + " where " + Cons.BATTERY_TIME + " in ("
					+ " select " + Cons.BATTERY_TIME + " from " + tableName + " order by " + Cons.BATTERY_TIME
					 + " limit " + count + ")"); 
		}
		c.close();
	}
	public double queryRate() {
		double rate = 0;
		HashMap<String, Object[]> data = query(Cons.MAX_COUNT);
		Long time[] = (Long[])data.get(Cons.BATTERY_TIME);
		String status[] = (String[])data.get(Cons.BATTERY_STATUSES);
		Integer level[] = (Integer[])data.get(Cons.BATTERY_LEVEL);
		int count = time.length;
		Vector<Double> rates = new Vector<Double>();
		for (int i = 1; i < count; i ++) {
			if(status[i-1].equals(Cons.BATTERY_DISCHARGE) && status[i].equals(Cons.BATTERY_DISCHARGE)) {
				double less = level[i] - level[i-1];
				double r = less / (time[i] - time[i-1]);
				rates.add(r);
			}
		}
		for(int i = 0; i < rates.size(); i ++) {
			rate += rates.get(i);
		}
		return rate / rates.size();
	}
	public HashMap<String, Object[]> query(int num) {
		long current = System.currentTimeMillis();
		current -= (8 * 1000 * 3600);
		String selection = Cons.BATTERY_TIME + ">=?";
		String[] arg = {current+""};
		//Cursor c = db.query(tableName,null,selection,arg,null,null,null);
		Cursor c = db.query(tableName, null, null, null, null, null, null);
	    int count = c.getCount();
	    int arrayLength = Math.min(count, num);
	    Long time[] = new Long[arrayLength];
		Integer level[] = new Integer[arrayLength];
		String status[] = new String[arrayLength];
		arrayLength --;
		int firstLevel = 0;
		//long current = System.currentTimeMillis();
		if(c.moveToLast()) {
			do{
				time[arrayLength] = c.getLong(c.getColumnIndex(Cons.BATTERY_TIME));
				level[arrayLength] = c.getInt(c.getColumnIndex(Cons.BATTERY_LEVEL));
				status[arrayLength] = c.getString(c.getColumnIndex(Cons.BATTERY_STATUSES));
				/*if(time[arrayLength] < current) {
					time[arrayLength] = current + (10 * 60 * 1000);
					if(firstLevel == 0) {
						firstLevel = level[arrayLength];
					}
					level[arrayLength] = firstLevel;
				}*/
				arrayLength --;
			}while(c.moveToPrevious() && arrayLength >= 0);
		}
		//*********
		int mark = time.length - 1;
		while(time[mark] > current) {
			mark --;
		}
		arrayLength = time.length - mark;
		Long time2[] = new Long[arrayLength];
		Integer level2[] = new Integer[arrayLength];
	    String status2[] = new String[arrayLength];
	    for(int i = 0; i < arrayLength; i ++) {
	    	time2[i] = time[mark + i];
	    	level2[i] = level[mark + i];
	    	status2[i] = status[mark + i];
	    }
	    time2[0] = current;
	    //*******
		HashMap<String, Object[]> data = new HashMap<String, Object[]>();
		data.put(Cons.BATTERY_LEVEL, level2);
		data.put(Cons.BATTERY_TIME, time2);
		data.put(Cons.BATTERY_STATUSES, status2);
		c.close();
		return data;
		
	}
	public void closeDB() { 
		db.close();
	}
 }
