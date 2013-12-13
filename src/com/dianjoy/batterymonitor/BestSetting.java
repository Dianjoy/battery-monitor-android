package com.dianjoy.batterymonitor;

import java.io.File;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.dianjoy.batterymonitor.tools.Utils;
import com.dianjoy.batterymonitor.view.ChartView;
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
		this.createGraphicalView();
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
	public void share(View v) {
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/shengdian");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		long current = System.currentTimeMillis();
		Utils.savePic(BestSetting.this, dir.getAbsolutePath() + "/" + current + ".png");
		String shareTitle = this.getResources().getString(R.string.share_title);
		String shareContent = this.getResources().getString(R.string.share_content);
		Utils.shareMsg(BestSetting.this, shareTitle, shareTitle, shareContent, dir.getAbsolutePath() + "/" + current + ".png");
	}
	public void createGraphicalView() {
		ChartView chartView = new ChartView(this);
		GraphicalView chart = chartView.getData();
		chart.setBackgroundColor(0x88222222);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = 30;
		params.leftMargin = 30;
		params.rightMargin = 30;
		params.bottomMargin = Utils.dip2px(this, 45);
		LinearLayout  layout = (LinearLayout) findViewById(R.id.best_setting);
		layout.addView(chart, params);
		
	}
	public void backButton(View v) {
		this.finish();
	}
}
