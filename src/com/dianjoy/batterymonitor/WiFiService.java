package com.dianjoy.batterymonitor;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class WiFiService extends Service {
	private static Method mReflectScreenState;
	private static int wifiStatus = 0;
	private static int mobileStatus = 0;
	private IntentFilter screenFilter;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		Log.i("tag", "service is create");
		screenFilter = new IntentFilter();
		screenFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenReceiver, screenFilter);
		registerReceiver(screenReceiver, screenFilter);
		getAPNType(WiFiService.this);
		monitor();
	}

	private void monitor() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!isScreenOn()) {
					if(wifiStatus==1){
						new WiFiManagerOp(WiFiService.this).openWifi();
					}
					if(mobileStatus==1){
						MobileManagerOp.setMobileData(WiFiService.this,true);
					}
					try {
						Thread.sleep(60 * 1000l);
					    if(!isScreenOn()){
					    	if(wifiStatus==1){
								new WiFiManagerOp(WiFiService.this).closeWifi();
							}
							if(mobileStatus==1){
								MobileManagerOp.setMobileData(WiFiService.this,false);
							}
					    }
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		},10 * 60 * 1000l, 10 * 60 * 1000l);
	}

	/**
	 * screen是否打开状态
	 * 
	 * @param pm
	 * @return
	 */
	private boolean isScreenOn() {
		boolean screenState;
		PowerManager manager = (PowerManager) WiFiService.this
				.getSystemService(Activity.POWER_SERVICE);
		try {

			try {
				mReflectScreenState = PowerManager.class.getMethod(
						"isScreenOn", new Class[] {});
			} catch (NoSuchMethodException nsme) {

			}
			screenState = (Boolean) mReflectScreenState.invoke(manager);
		} catch (Exception e) {
			screenState = false;
		}
		return screenState;
	}

	/**
	 * 获取当前的网络状态 -1：没有网络 1：WIFI网络2：wap网络3：net网络
	 * 
	 * @param context
	 * @return
	 */
	private final int getAPNType(Context context) {
		int CMNET = 3;
		int CMWAP = 2;
		int WIFI = 1;
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = CMNET;
			} else {
				netType = CMWAP;
			}
			mobileStatus = 1;
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			wifiStatus = 1;
			netType = WIFI;
		}
		return netType;
	}

	BroadcastReceiver screenReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				// 解锁
				openNetwork();

			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				// 锁屏
				closeNetwork();
				getAPNType(WiFiService.this);
			}
		}
	};

	private void closeNetwork() {
		Log.i("close","network is close");
		new WiFiManagerOp(WiFiService.this).closeWifi();
		MobileManagerOp.setMobileData(WiFiService.this, false);
	}
	private void openNetwork(){
		Log.i("close","network is open");
		if(wifiStatus==1){
			Log.i("wifi","me is wifi");
			new WiFiManagerOp(WiFiService.this).openWifi();
		}
		if(mobileStatus==1){
			Log.i("mobile","me is mobile");
			MobileManagerOp.setMobileData(WiFiService.this, true);
		}	
	}
}
