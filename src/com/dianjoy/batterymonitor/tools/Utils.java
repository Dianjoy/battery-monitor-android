package com.dianjoy.batterymonitor.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
	public static String getPreferenceStr(Context context, String name) {
		return getPreferenceStr(context, name, "");
	}

	public static String getPreferenceStr(Context context, String name,
			String defValue) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		return preferences.getString(name, defValue);
	}

	public static void setPreferenceStr(Context context, String name,
			String value) {
		SharedPreferences preferences = context.getSharedPreferences(
				"preferences", 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
	public static void setPreferenceStr(Context context, String name, String value, String preName) {
		SharedPreferences preferences = context.getSharedPreferences(
				preName, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();
	}
	public static String getPreferenceStr(Context context, String name, String preName, String defValue) {
		SharedPreferences preferences = context.getSharedPreferences(
				preName, 0);
		return preferences.getString(name, defValue);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
