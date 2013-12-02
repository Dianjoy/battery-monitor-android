package com.dianjoy.batterymonitor;

import com.dianjoy.batterymonitor.tools.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Setting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;
	private TextView textBat;
	private TextView hours;
	private TextView minutes;
	private TextView status;
	private LinearLayout linearLayot;
	private LinearLayout count;

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
        		Intent intent = new Intent(Setting.this,ChartActivity.class);
        		startActivity(intent);
        		
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
		BatteryReceiver batteryReceiver = new BatteryReceiver();

		// 注册receiver
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
	public void getBatteryMessage() {
		
	}
	/**
	 * 广播接受者
	 */
	class BatteryReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// 判断它是否是为电量变化的Broadcast Action
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 获取当前电量
				int level = intent.getIntExtra("level", 0);
				Log.i("battery", level + " battery level");
				// 电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				textBat.setText(((level * 100) / scale) + "%");
				int battery_level = (level * 100) / scale;
				int battery_status = intent.getIntExtra("status", -1);
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
						// 电源充电 1000mA
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
					status.setText("充电中");
				} else if (battery_status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					if (Utils.getPreferenceStr(Setting.this,
							WiFiService.BATTERY_DISCHARGE_TIME, "").equals("")) {
						float stime = (battery_level / 100f) * 2000 / 100f;
						stime = ((int) (stime * 10)) / 10f;
						if (String.valueOf((int) stime).equals("0")) {
							status.setText("可用"
									+ String.valueOf((stime - (int) stime) * 60)
											.substring(
													0,
													String.valueOf(
															(stime - (int) stime) * 60)
															.indexOf("."))
									+ "分钟");
						} else {
							status.setText("可用" + String.valueOf((int) stime)
									+ "小时");
						}
					} else {
						float timeTemp = Float.valueOf(Utils.getPreferenceStr(
								Setting.this,
								WiFiService.BATTERY_DISCHARGE_TIME, "0"));
						status.setText("可用" + String.valueOf((int) timeTemp)
								+ "小时");
						minutes.setText(String
								.valueOf((timeTemp - (int) timeTemp) * 60)
								.substring(
										0,
										String.valueOf(
												(timeTemp - (int) timeTemp) * 60)
												.indexOf("."))
								+ "M");
					}

				} else if (battery_status == BatteryManager.BATTERY_STATUS_FULL) {
					status.setText("充电完成");
				}
			}
		}
	}
}
