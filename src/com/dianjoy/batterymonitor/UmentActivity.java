package com.dianjoy.batterymonitor;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public class UmentActivity extends Activity {
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
