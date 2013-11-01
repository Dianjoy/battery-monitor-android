package com.dianjoy.batterymonitor;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class BestSetting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_best_setting);

		checkBoxGetInfo = (CheckBox) findViewById(R.id.getInfo);
		checkBoxGetProgress = (CheckBox) findViewById(R.id.getProgressInfo);
		if (Utils.getPreferenceStr(this, "getInfo", "false").equals("true")) {
			this.checkBoxGetInfo.setChecked(true);
		}
		if (Utils.getPreferenceStr(this, "progressInfo", "false")
				.equals("true")) {
			this.checkBoxGetProgress.setChecked(true);
		}
		checkBoxGetInfo
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							Utils.setPreferenceStr(BestSetting.this, "getInfo",
									"true");
						} else {
							Utils.setPreferenceStr(BestSetting.this, "getInfo",
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
							Utils.setPreferenceStr(BestSetting.this,
									"progressInfo", "true");
						} else {
							Utils.setPreferenceStr(BestSetting.this,
									"progressInfo", "false");
						}
					}
				});
	}
}
