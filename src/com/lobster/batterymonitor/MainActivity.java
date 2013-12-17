package com.lobster.batterymonitor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends UmentActivity {
	private Button btnSetting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent startIntentService = new Intent();
		startIntentService.setClassName(this,
				"com.dianjoy.batterymonitor.WiFiService");
		startService(startIntentService);
		// MobileManagerOp.setMobileData(this,true);
		btnSetting = (Button) findViewById(R.id.setting);
		btnSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
              Intent intentFilter=new Intent(MainActivity.this,Setting.class);
              startActivity(intentFilter);
			}
		});
	}

	@Override
	protected final void onStart() {
		super.onStart();

	}


}
