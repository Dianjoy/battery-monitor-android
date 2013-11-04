package com.dianjoy.batterymonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.umeng.analytics.MobclickAgent;

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
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

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
	public static final String BATTERY_CHARGE_TIME = "battery_charge_time";
	public static final String BATTERY_DISCHARGE_TIME = "battery_discharge_time";
	public static final String BATTERY_CHARGE = "charge";
	public static final String BATTERY_DISCHARGE = "discharge";
	public static final String BATTERY_LEVEL = "battery_level";
	public static final String BATTERY_CHARGE_FULL = "charge_full";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("start", "service is restart");
		if (Utils.getPreferenceStr(WiFiService.this, "progressInfo").equals(
				"true")) {
			clearProgress();
		}
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		unregisterReceiver(screenReceiver);
		unregisterReceiver(batteryReceiver);
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

		getAPNType(WiFiService.this);
		if (Utils.getPreferenceStr(WiFiService.this, "getInfo").equals("true")) {
			monitor();
		}
	}

	private void monitor() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!isScreenOn()) {
					if (wifiStatus == 1) {
						new WiFiManagerOp(WiFiService.this).openWifi();
					}
					if (mobileStatus == 1) {
						MobileManagerOp.setMobileData(WiFiService.this, true);
					}
					try {
						Thread.sleep(60 * 1000l);
						if (!isScreenOn()) {
							if (wifiStatus == 1) {
								new WiFiManagerOp(WiFiService.this).closeWifi();
							}
							if (mobileStatus == 1) {
								MobileManagerOp.setMobileData(WiFiService.this,
										false);
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}, 0, 15 * 60 * 1000l);
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
				// 解锁
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
				getAPNType(WiFiService.this);
				closeNetwork();
			}
		}
	};

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
				// System.out.println("pid            " + apinfo.pid);
				// System.out.println("processName" + apinfo.processName);
				// System.out.println("importance            " +
				// apinfo.importance);
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
		// 获取当前系统可用内存
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
			// 获得系统总内存
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();

		} catch (IOException e) {
		}
		return initial_memory / (1024 * 1024);
	}

	// 判断是否有耳机插入
	public boolean isHeadsetHold() {
		return audioManager.isWiredHeadsetOn();
	}

	// 判断蓝牙是否打开
	public boolean isBlueToothHold() {
		return bluetoothAdapter.isEnabled();
	}

	// 电池电量的监听广播
	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("deviceid",tm.getDeviceId()+"");
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				if (rawlevel >= 0 && scale > 0) {
					battery_level = (rawlevel * 100) / scale;
					Utils.setPreferenceStr(context, BATTERY_LEVEL,
							String.valueOf(battery_level));
					map.put("power", String.valueOf(battery_level));
				}
				int status = intent.getIntExtra("status", -1);
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					Utils.setPreferenceStr(context, BATTERY_STATUS,
							BATTERY_CHARGE);
					// 充电状态
					map.put("status", String.valueOf(1));
					int plugeed = intent.getIntExtra("plugged", -1);
					if (isFirstCharge) {
						if (plugeed == BatteryManager.BATTERY_PLUGGED_USB) {
							// USB充电 500mA
							float stime = ((100 - battery_level) * 2000)
									/ (500 * 100f);
							stime = ((int) (stime * 10)) / 10f;
							Utils.setPreferenceStr(context,
									BATTERY_CHARGE_TIME, stime + "");
						} else if (plugeed == BatteryManager.BATTERY_PLUGGED_AC) {
							// 电源充电 1000mA
							float stime = ((100 - battery_level) * 2000)
									/ (1000 * 100f);
							stime = ((int) (stime * 10)) / 10f;
							Utils.setPreferenceStr(context,
									BATTERY_CHARGE_TIME, stime + "");
						}
						isFirstCharge = false;
						first_charge_battery_level = battery_level;
					} else {
						if (first_charge_battery_level < battery_level) {
							if (battery_level - first_charge_battery_level > 1) {
								if (old_charge_battery_level < battery_level) {
									long current_time = System
											.currentTimeMillis();
									int poor_level = battery_level
											- old_charge_battery_level;
									long poor_time = current_time
											- last_charge_time;
									float stime = (((100 - battery_level) / poor_level) * poor_time)
											/ (1000 * 60 * 60f);
									stime = ((int) (stime * 10)) / 10f;
									Utils.setPreferenceStr(context,
											BATTERY_CHARGE_TIME, stime + "");
									old_charge_battery_level = battery_level;
									last_charge_time = System
											.currentTimeMillis();
								} else {
									return;
								}
							} else {
								last_charge_time = System.currentTimeMillis();
								old_charge_battery_level = battery_level;
								return;
							}
						} else {
							return;
						}
					}

				} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					// 放电状态
					map.put("status", String.valueOf(0));
					Utils.setPreferenceStr(context, BATTERY_STATUS,
							BATTERY_DISCHARGE);
					if (isFirstDisCharge) {
						// 放电100mA
						float stime = (battery_level / 100f) * 2000 / 100f;
						stime = ((int) (stime * 10)) / 10f;
						Utils.setPreferenceStr(context, BATTERY_DISCHARGE_TIME,
								stime + "");
						first_discharge_battery_level = battery_level;
						isFirstDisCharge = false;
					} else {
						if (battery_level < first_discharge_battery_level) {
							if (first_discharge_battery_level - battery_level > 1) {
								if (battery_level < old_discharge_battery_level) {
									long current_time = System
											.currentTimeMillis();
									int poor_level = old_discharge_battery_level
											- battery_level;
									long poor_time = current_time
											- last_discharge_time;
									float stime = ((battery_level / poor_level) * poor_time)
											/ (1000 * 60 * 60f);
									stime = ((int) (stime * 10)) / 10f;
									Utils.setPreferenceStr(context,
											BATTERY_DISCHARGE_TIME, stime + "");
									last_discharge_time = System
											.currentTimeMillis();
									old_discharge_battery_level = battery_level;
								} else {
									return;
								}
							} else {
								last_discharge_time = System
										.currentTimeMillis();
								old_discharge_battery_level = battery_level;
								return;
							}
						} else {
							return;
						}
					}
				} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
					Utils.setPreferenceStr(context, BATTERY_STATUS,
							BATTERY_CHARGE_FULL);
					// 放电完毕
					map.put("status", String.valueOf(2));
				}

				MobclickAgent.onEvent(WiFiService.this, "battery_power", map);
			}
		}
	};
}
