package com.dianjoy.batterymonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Setting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;
	private TextView textBat;
	private TextView hours;
	private TextView minutes;
	private TextView status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		Intent startIntentService = new Intent();
		startIntentService.setClassName(this,
				"com.dianjoy.batterymonitor.WiFiService");
		startService(startIntentService);
		checkBoxGetInfo = (CheckBox) findViewById(R.id.getInfo);
		checkBoxGetProgress = (CheckBox) findViewById(R.id.getProgressInfo);
		textBat = (TextView) findViewById(R.id.battery);
		hours = (TextView) findViewById(R.id.hours);
		minutes = (TextView) findViewById(R.id.minutes);
		textBat = (TextView) findViewById(R.id.battery);
		status= (TextView) findViewById(R.id.status);
		if (Utils.getPreferenceStr(this, "getInfo", "false").equals("true")) {
			this.checkBoxGetInfo.setChecked(true);
		}
		if (Utils.getPreferenceStr(this, "progressInfo", "false")
				.equals("true")) {
			this.checkBoxGetProgress.setChecked(true);
		}
		Typeface fontFace = Typeface.createFromAsset(getAssets(),
				"fonts/stxingka.ttf");
		TextView text = (TextView) findViewById(R.id.battery);
		text.setTypeface(fontFace);
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		BatteryReceiver batteryReceiver = new BatteryReceiver();

		// ע��receiver
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
	}

	/**
	 * �㲥������
	 */
	class BatteryReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// �ж����Ƿ���Ϊ�����仯��Broadcast Action
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// ��ȡ��ǰ����
				int level = intent.getIntExtra("level", 0);
				// �������̶ܿ�
				int scale = intent.getIntExtra("scale", 100);
				textBat.setText(((level * 100) / scale) + "%");
				int battery_level = (level * 100) / scale;
				if (intent.getIntExtra("status", -1) == BatteryManager.BATTERY_STATUS_CHARGING) {
					int plugeed = intent.getIntExtra("plugged", -1);
					if (plugeed == BatteryManager.BATTERY_PLUGGED_USB) {
						// USB��� 500mA
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
						// ��Դ��� 1000mA
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
					status.setText("��ǰ״̬:���");
				} else {
					status.setText("��ǰ״̬:�ŵ�");
					if (Float.valueOf(
							Utils.getPreferenceStr(Setting.this,
									WiFiService.BATTERY_CHARGE_TIME, "0.0"))
							.equals("0.0")) {
						float stime = (battery_level / 100) * 2000 / 100l;
						stime = ((int) (stime * 10)) / 10l;
						hours.setText(String.valueOf((int) stime) + "H");
						minutes.setText(String.valueOf(
								(stime - (int) stime) * 60).substring(
								0,
								String.valueOf((stime - (int) stime) * 60)
										.indexOf("."))
								+ "M");
					} else {
						float timeTemp = Float.valueOf(Utils.getPreferenceStr(
								Setting.this, WiFiService.BATTERY_CHARGE_TIME,
								"0"));
						hours.setText(String.valueOf((int) timeTemp) + "H");
						minutes.setText(String.valueOf(
								(timeTemp - (int) timeTemp) * 60).substring(
								0,
								String.valueOf((timeTemp - (int) timeTemp) * 60)
										.indexOf("."))
								+ "M");
					}

				}
			}
		}
	}
}
