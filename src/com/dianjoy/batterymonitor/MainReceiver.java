package com.dianjoy.batterymonitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MainReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.i("yya", action);
		if (action.equals("android.intent.action.BOOT_COMPLETED")|| action.equals("android.intent.action.USER_PRESENT")|| action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			startService(context);
		}
	}

	/**
	 * start the custom service
	 */
	public void startService(Context context) {
		Intent intent = new Intent();
		intent.setClassName(context,"com.dianjoy.batterymonitor.WiFiService");
		context.startService(intent);
	}
}
