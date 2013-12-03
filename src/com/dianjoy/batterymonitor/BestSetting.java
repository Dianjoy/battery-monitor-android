package com.dianjoy.batterymonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dianjoy.batterymonitor.tools.Utils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
public class BestSetting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;
	private FeedbackAgent agent;// = new FeedbackAgent(this);

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
							MobclickAgent.onEvent(BestSetting.this,"screen_getinfo_on");
							Utils.setPreferenceStr(BestSetting.this, "getInfo",
									"true");
						} else {
							Utils.setPreferenceStr(BestSetting.this, "getInfo",
									"false");
							MobclickAgent.onEvent(BestSetting.this,"screen_getinfo_off");
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
							MobclickAgent.onEvent(BestSetting.this,"screen_clearn_progress_on");
							Utils.setPreferenceStr(BestSetting.this,
									"progressInfo", "true");
						} else {
							MobclickAgent.onEvent(BestSetting.this,"screen_clearn_progress_off");
							Utils.setPreferenceStr(BestSetting.this,
									"progressInfo", "false");
						}
					}
				});
		agent = new FeedbackAgent(this);
		agent.sync();
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
	public void settingWhiteList(View v) {
		Intent intent = new Intent(this, WhiteList.class);
		startActivity(intent);
	}
	public void feedBack(View v) {
	    agent.startFeedbackActivity();
	}
}