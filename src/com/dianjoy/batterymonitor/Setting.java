package com.dianjoy.batterymonitor;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dianjoy.batterymonitor.tools.Cons;
import com.dianjoy.batterymonitor.tools.DBManager;
import com.dianjoy.batterymonitor.tools.Utils;
import com.umeng.analytics.MobclickAgent;

public class Setting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;
	private TextView textBat;
	private TextView hours;
	private TextView minutes;
	private TextView status;
	private LinearLayout linearLayot;
	private LinearLayout count;
	private Context context;
	private BatteryReceiver batteryReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		Intent startIntentService = new Intent();
		startIntentService.setClassName(this,
				"com.dianjoy.batterymonitor.WiFiService");
		startService(startIntentService);
		linearLayot = (LinearLayout) findViewById(R.id.bestSetting);
		count = (LinearLayout)findViewById(R.id.count);
		linearLayot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intentFilter = new Intent(Setting.this,
						BestSetting.class);
				startActivity(intentFilter);
			}
		});
        count.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        	//	Intent intent = new Intent(Setting.this,ChartActivity.class);
        	//	startActivity(intent);
        		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/shengdian");
        		if(!dir.exists()) {
        			dir.mkdirs();
        		}
        		long current = System.currentTimeMillis();
        		Utils.savePic(Setting.this, dir.getAbsolutePath() + "/" + current + ".png");
        		Utils.shareMsg(context, "非常省电", "非常省电beta", "我安装了非常省电", dir.getAbsolutePath() + "/" + current + ".png");
        		
        	}
        });
		checkBoxGetInfo = (CheckBox) findViewById(R.id.getInfo);
		checkBoxGetProgress = (CheckBox) findViewById(R.id.getProgressInfo);
		textBat = (TextView) findViewById(R.id.battery);
		hours = (TextView) findViewById(R.id.hours);
		minutes = (TextView) findViewById(R.id.minutes);
		textBat = (TextView) findViewById(R.id.battery);
		status = (TextView) findViewById(R.id.status);
		if (Utils.getPreferenceStr(this, "getInfo", "false").equals("true")) {
			this.checkBoxGetInfo.setChecked(true);
		}
		if (Utils.getPreferenceStr(this, "progressInfo", "false")
				.equals("true")) {
			this.checkBoxGetProgress.setChecked(true);
		}
		Typeface fontFace = Typeface.createFromAsset(getAssets(),
				"fonts/HelveticaNeueLTStd-Th.otf");
		TextView text = (TextView) findViewById(R.id.battery);
		text.setTypeface(fontFace);
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		batteryReceiver = new BatteryReceiver();

		//注册receiver
		registerReceiver(batteryReceiver, intentFilter);
		checkBoxGetInfo
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							Utils.setPreferenceStr(Setting.this, "getInfo",
									"true");
						} else {
							Utils.setPreferenceStr(Setting.this, "getInfo",
									"false");
						}
					}
				});

		checkBoxGetProgress
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							Utils.setPreferenceStr(Setting.this,
									"progressInfo", "true");
						} else {
							Utils.setPreferenceStr(Setting.this,
									"progressInfo", "false");
						}
					}
				});
		context = this;
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	public boolean checkStatus(String s, int l, long t) {
		if (s.equals(Cons.BATTERY_DISCHARGE) && t != 0 && l != 0) {
			return true;
		}else {
			return false;
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(batteryReceiver);
	}
	public double getBatteryMessage() {
		int count = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_COUNT, Cons.BATTERY_PRE, "0"));
		int begin = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_COUNT_BEGIN, Cons.BATTERY_PRE, "0"));
		String preStatus = Cons.BATTERY_CHARGE;
		long preTime = 0;
		int preLevel = 0;
		Vector<Double> values = new Vector<Double>();
		for (int i = begin; i < count; i ++) {
			int offset = (begin + i) % Cons.MAX_COUNT;
			String status = Utils.getPreferenceStr(context, Cons.BATTERY_STATUSES + offset, Cons.BATTERY_PRE, Cons.BATTERY_DISCHARGE);
			long time = Long.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_TIME + offset, Cons.BATTERY_PRE, "0"));
			int level = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_LEVEL + offset, Cons.BATTERY_PRE, "0"));
			if (checkStatus(preStatus, preLevel, preTime) && checkStatus(status, level, time)) {
				
				double value = (double)(level - preLevel) /(time - preTime); //
				if(time - preTime  < 15 * 1000l * 60 * 2)
					values.add(value);
			}
			if (checkStatus(status, level, time)){
				preStatus = status;
				preLevel = level;
				preTime = time;
			}
		}
		double averageLevel = 0;
		for(int i = 0; i < values.size(); i ++) {
			averageLevel += values.get(i);
		}
		averageLevel /= values.size();
		return averageLevel;
		
	}
	/**
	 * 电池状态广播
	 */
	class BatteryReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// Broadcast Action
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 电量变化广播
				String use = context.getResources().getString(R.string.use);
				String hourText = context.getResources().getString(R.string.hour);
				String miniteText = context.getResources().getString(R.string.minute);
				int level = intent.getIntExtra("level", 0);
				Log.i("battery", level + " battery level");
				// 获取当前电量
				int scale = intent.getIntExtra("scale", 100);
				textBat.setText(((level * 100) / scale) + "%");
				int battery_level = (level * 100) / scale;
				DBManager db = new DBManager(context, "battery_message");
				int battery_status = intent.getIntExtra("status", -1);
			//	Toast.makeText(context, "battery " +  level + " " + battery_status, Toast.LENGTH_SHORT).show();
				if (battery_status == BatteryManager.BATTERY_STATUS_CHARGING) {
					int plugeed = intent.getIntExtra("plugged", -1);
					if (plugeed == BatteryManager.BATTERY_PLUGGED_USB) {
						// USB充电 500mA
						float stime = ((100 - battery_level) * 2000)
								/ (500 * 100f);
						stime = ((int) (stime * 10)) / 10f;
						hours.setText(String.valueOf((int) stime) + "H");
						minutes.setText(String.valueOf(
								(stime - (int) stime) * 60).substring(
								0,
								String.valueOf((stime - (int) stime) * 60)
										.indexOf("."))
								+ "M");
					} else if (plugeed == BatteryManager.BATTERY_PLUGGED_AC) {
						// 直流电 1000mA
						float stime = ((100 - battery_level) * 2000)
								/ (1000 * 100f);
						stime = ((int) (stime * 10)) / 10f;
						hours.setText(String.valueOf((int) stime) + "H");
						minutes.setText(String.valueOf(
								(stime - (int) stime) * 60).substring(
								0,
								String.valueOf((stime - (int) stime) * 60)
										.indexOf("."))
								+ "M");

					}
					status.setText(context.getResources().getString(R.string.charging));
				} else if (battery_status == BatteryManager.BATTERY_STATUS_FULL) {
					status.setText(context.getResources().getString(R.string.charge_full));
					int a = BatteryManager.BATTERY_HEALTH_UNKNOWN;
				}else {//if (battery_status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
				
					double averageLevel = db.queryRate();
				//	Toast.makeText(context, "battery " +  averageLevel + battery_status, Toast.LENGTH_SHORT).show();
					if (averageLevel < 0) {
						 int minute = (int)(-battery_level / averageLevel /(1000l * 60));
						 int hour = minute / 60;
						 String text;
						 if (hour == 0) {
							 text = use + minute + miniteText;
						 }else {
							 text = use + hour + hourText + minute % 60 + miniteText;
						 }
						 status.setText(text);
					}
					else if (Utils.getPreferenceStr(Setting.this,
							WiFiService.BATTERY_DISCHARGE_TIME, "").equals("")) {
						float stime = (battery_level / 100f) * 2000 / 100f;
						stime = ((int) (stime * 10)) / 10f;
						if (String.valueOf((int) stime).equals("0")) {
							status.setText(use
									+ String.valueOf((stime - (int) stime) * 60)
											.substring(
													0,
													String.valueOf(
															(stime - (int) stime) * 60)
															.indexOf("."))
									+ hourText);
						} else {
							status.setText(use + String.valueOf((int) stime)
									+ hourText);
						}
					} else {
						float timeTemp = Float.valueOf(Utils.getPreferenceStr(
								Setting.this,
								WiFiService.BATTERY_DISCHARGE_TIME, "0"));
						status.setText(use + String.valueOf((int) timeTemp)
								+ hourText);
						minutes.setText(String
								.valueOf((timeTemp - (int) timeTemp) * 60)
								.substring(
										0,
										String.valueOf(
												(timeTemp - (int) timeTemp) * 60)
												.indexOf("."))
								+ "M");
					}

				} 
				db.closeDB();
			}
		}
	}
}
