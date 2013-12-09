package com.dianjoy.batterymonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.dianjoy.batterymonitor.tools.Cons;
import com.dianjoy.batterymonitor.tools.DBManager;
import com.dianjoy.batterymonitor.tools.Utils;

public class WiFiService extends Service {
	private boolean screenStatus = false;
	private static Method mReflectScreenState;
	private static int wifiStatus = 0;
	private static int mobileStatus = 0;
	private IntentFilter screenFilter;
	private AudioManager audioManager;
	private BluetoothAdapter bluetoothAdapter;
	private IntentFilter batteryFilter;
	private int battery_level;
	private int first_charge_battery_level = -1;
	private int old_charge_battery_level = -1;
	private int first_discharge_battery_level = -1;
	private int old_discharge_battery_level = -1;
	private long last_charge_time;
	private long last_discharge_time;
	private boolean isFirstCharge = true;
	private boolean isFirstDisCharge = true;
	public static final String BATTERY_STATUS = "battery_status";
	public static final String WIFI_STATUS = "wifi_status";
	public static final String MOBILE_STATUS = "mobile_status";
	public static final String BATTERY_CHARGE_TIME = "battery_charge_time";
	public static final String BATTERY_DISCHARGE_TIME = "battery_discharge_time";
	public static final String BATTERY_CHARGE = "charge";
	public static final String BATTERY_DISCHARGE = "discharge";
	public static final String BATTERY_LEVEL = "battery_level";
	public static final String BATTERY_CHARGE_FULL = "charge_full";
	public static final String BATTERY_PRE = "battery_message";
	public static final String BATTERY_COUNT = "battery_count"; //
	public static final String BATTERY_TIME = "battery_time";
	public static final String BATTERY_STATUSES = "battery_status";
	public static final String BATTERY_COUNT_BEGIN = "battery_count_begin"; // 
	public static final int MAX_COUNT = 96;
	private Context context;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("tag", "service is restart");
		if (Utils.getPreferenceStr(WiFiService.this, "progressInfo").equals(
				"true")) {
			clearProgress();
		}
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		Log.i("tag", "service is destory");
		unregisterReceiver(screenReceiver);
		unregisterReceiver(batteryReceiver);
		unregisterReceiver(batteryChangeReceiver);
	}

	@Override
	public final void onCreate() {
		super.onCreate();
		Log.i("tag", "service is create");
		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		screenFilter = new IntentFilter();
		screenFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenReceiver, screenFilter);

		batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryFilter);
		registerReceiver(batteryChangeReceiver, batteryFilter);
		//db = new DBManager(this, "battery_message");
		context = this;
		getAPNType(WiFiService.this);
		monitor();
		getBatteryMessage();
	}

	private void getBatteryMessage() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				batteryFilter = new IntentFilter();
				batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				registerReceiver(batteryReceiver, batteryFilter);
				unregisterReceiver(batteryReceiver);

			}

		}, 0, 15 * 60 * 1000l);
	}

	private void monitor() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (Utils.getPreferenceStr(WiFiService.this, "getInfo").equals(
						"true")) {
					if (!isScreenOn()) {
						if (wifiStatus == 1) {
							new WiFiManagerOp(WiFiService.this).openWifi();
						}
						if (mobileStatus == 1) {
							MobileManagerOp.setMobileData(WiFiService.this,
									true);
						}
						try {
							Thread.sleep(60 * 1000l);
							if (!isScreenOn()) {
								if (wifiStatus == 1) {
									new WiFiManagerOp(WiFiService.this)
											.closeWifi();
								}
								if (mobileStatus == 1) {
									MobileManagerOp.setMobileData(
											WiFiService.this, false);
								}
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}, 0, 15 * 60 * 1000l);
	}

	/**
	 * screen锟角凤拷锟阶刺�
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
	 * 缃戠粶绫诲瀷 1-WIFI 2 绉诲姩wap 3绉诲姩net
	 * 
	 * @param context
	 * @return
	 */
	private final int getAPNType(Context context) {
		mobileStatus = 0;
		wifiStatus = 0;
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
				screenStatus = false;
				// 锟斤拷锟斤拷
				openNetwork();
				if (Utils.getPreferenceStr(WiFiService.this, "progressInfo")
						.equals("true")) {
					clearProgress();
				}

			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				screenStatus = true;
				if (isHeadsetHold() || isBlueToothHold()) {
					wifiStatus = 0;
					mobileStatus = 0;
					return;
				}
				if (Utils.getPreferenceStr(WiFiService.this, "progressInfo")
						.equals("true")) {
					clearProgress();
				}
				if (Utils.getPreferenceStr(context, BATTERY_STATUS,
						BATTERY_DISCHARGE).equals(BATTERY_CHARGE)) {// 锟斤拷锟绞憋拷锟斤拷锟斤拷锟绞★拷锟斤拷锟斤拷
					getAPNType(WiFiService.this);
					closeNetwork();
				} // yan.gao
			}
		}
	};
	/**
	 * reset the wifi and mobile status.
	 */
    public void restoreWifiAndMobile() {
    	WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    	int wifiStatus = Integer.valueOf(Utils.getPreferenceStr(context, WIFI_STATUS, WifiManager.WIFI_STATE_DISABLED + ""));
    	if (wifi != null) {
    		if (wifiStatus == WifiManager.WIFI_STATE_DISABLED && wifiStatus == WifiManager.WIFI_STATE_DISABLING) {
    			wifi.setWifiEnabled(false);
    		}else{
    			wifi.setWifiEnabled(true);
    		}
    	}
    	int mobileStatus = Integer.valueOf(Utils.getPreferenceStr(context, MOBILE_STATUS, -1+""));
    	if  (mobileStatus == 2 || mobileStatus == 3) {
    		MobileManagerOp.setMobileData(this, true);
    	}else{
    		MobileManagerOp.setMobileData(this, false);
    	}
    	
    }
    /**
     * remember the mobile and wifi state.
     */
    public void rememWifiAndMobile() {
    	int type = this.getAPNType(this);
        Utils.setPreferenceStr(context, MOBILE_STATUS, type+"");
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if(wifi != null) {
        	Utils.setPreferenceStr(context, WIFI_STATUS, wifi.getWifiState()+"");
        }
       
    }
    /**
     * connect wifi if the wifi connect failed after 1 minute.
     * close the wifi。
     */
    public void closeWifi() {
    	Timer connectWifi = new Timer();
    	new WiFiManagerOp(this).openWifi();
    	TimerTask task = new TimerTask() {
    		@Override
    		public void run() {
    			if(getAPNType(context) != 1) { //wifi连接不成功
    				new WiFiManagerOp(context).closeWifi();
    			}
    		}
    	};
    	connectWifi.schedule(task, 60 * 1000l);
    	
    }
	private void closeNetwork() {
		Log.i("close", "network is close");
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (screenStatus) {
					Log.i("yayyayya", "sfdsfsdf");
					new WiFiManagerOp(WiFiService.this).closeWifi();
					MobileManagerOp.setMobileData(WiFiService.this, false);
				}
			}
		}, 90 * 1000);
	}

	private void openNetwork() {
		Log.i("close", "network is open");
		if (wifiStatus == 1) {
			Log.i("wifi", "me is wifi");
			new WiFiManagerOp(WiFiService.this).openWifi();
		}
		if (mobileStatus == 1) {
			Log.i("mobile", "me is mobile");
			MobileManagerOp.setMobileData(WiFiService.this, true);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (getAPNType(this) == -1) {
			closeNetwork();
		}
	}

	private void clearProgress() {
		Log.i("meme1", String.valueOf(getAvailMemory(this)));
		// getAvailMemory(this);
		int systemVer = android.os.Build.VERSION.SDK_INT;
		ActivityManager activityManger = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger
				.getRunningAppProcesses();
		if (list != null)
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
				String[] pkgList = apinfo.pkgList;
				if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					for (int j = 0; j < pkgList.length; j++) {
						if (!pkgList[j].equals("com.dianjoy.batterymonitor")) {
							if (systemVer > 8) {
								activityManger
										.killBackgroundProcesses(pkgList[j]);
							} else {
								activityManger.restartPackage(pkgList[j]);
							}
						}

					}
				}
			}
		Log.i("meme1", String.valueOf(getAvailMemory(this)));
	}

	private long getAvailMemory(Context context) {
		// 
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem / (1024 * 1024);
	}

	private long getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}
			// 锟斤拷锟较低筹拷锟斤拷诖锟�
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();

		} catch (IOException e) {
		}
		return initial_memory / (1024 * 1024);
	}

	// 锟叫讹拷锟角凤拷锟叫讹拷锟斤拷锟斤拷
	public boolean isHeadsetHold() {
		return audioManager.isWiredHeadsetOn();
	}

	// 锟叫讹拷锟斤拷锟斤拷锟角凤拷锟�
	public boolean isBlueToothHold() {
		return bluetoothAdapter.isEnabled();
	}

	// 锟斤拷氐锟斤拷锟斤拷募锟斤拷锟姐播
	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				DBManager db = new DBManager(context, "battery_message");
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				if (rawlevel >= 0 && scale > 0) {
					battery_level = (rawlevel * 100) / scale;
				}else{
					return;
				}
				int status = intent.getIntExtra("status", -1);
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					db.add(battery_level, System.currentTimeMillis(), Cons.BATTERY_CHARGE);
				} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					db.add(battery_level, System.currentTimeMillis(), Cons.BATTERY_DISCHARGE);
					Utils.setPreferenceStr(context, WiFiService.BATTERY_STATUS, WiFiService.BATTERY_DISCHARGE);
				} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
					db.add(battery_level, System.currentTimeMillis(), Cons.BATTERY_CHARGE_FULL);
					Utils.setPreferenceStr(context, WiFiService.BATTERY_STATUS, WiFiService.BATTERY_CHARGE_FULL);
				}
				db.autoDelete();
				db.closeDB();
			}
		}
	};
	BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra("status", -1);
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					Utils.setPreferenceStr(context, BATTERY_STATUS, BATTERY_CHARGE);
				} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					Utils.setPreferenceStr(context, WiFiService.BATTERY_STATUS, WiFiService.BATTERY_DISCHARGE);
				} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
					Utils.setPreferenceStr(context, WiFiService.BATTERY_STATUS, WiFiService.BATTERY_CHARGE_FULL);
				}
		
			}
		}
	};
}
