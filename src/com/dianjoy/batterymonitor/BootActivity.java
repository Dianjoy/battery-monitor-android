package com.dianjoy.batterymonitor;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;

public class BootActivity extends UmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boot);
		startMainActivity();
	}
	public void startMainActivity() {
		new Timer().schedule(
				new TimerTask(){
					public void run(){
						Intent intent = new Intent(BootActivity.this, Setting.class);
						BootActivity.this.startActivity(intent);
						BootActivity.this.finish();
					}
				}, 2000l);
				

	}
}
