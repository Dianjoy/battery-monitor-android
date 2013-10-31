package com.dianjoy.batterymonitor;

import com.example.wifi_test.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent startIntentService = new Intent();
		startIntentService.setClassName(this,
				"com.example.wifi_test.WiFiService");
		startService(startIntentService);
		MobileManagerOp.setMobileData(this,true);
	}
}
