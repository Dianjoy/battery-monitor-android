package com.lobster.batterymonitor;

import java.io.File;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.lobster.batterymonitor.tools.Utils;
import com.lobster.batterymonitor.view.ChartView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
public class BestSetting extends Activity {
	private CheckBox checkBoxGetInfo;
	private CheckBox checkBoxGetProgress;
	private FeedbackAgent agent;// = new FeedbackAgent(this);
	private LinearLayout layout;
	private GraphicalView chart;

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
		layout = (LinearLayout) findViewById(R.id.best_setting);
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
		TextView batteryCount = (TextView) findViewById(R.id.best_setting_battery_count);
		int x = chart.getLeft();
		int y = batteryCount.getTop();
		int w = chart.getWidth();
		int h = chart.getHeight() + batteryCount.getHeight();
		Utils.savePic(BestSetting.this, dir.getAbsolutePath() + "/" + "电量统计曲线图",x,y,w,h);
		String shareTitle = this.getResources().getString(R.string.share_title);
		String shareContent = this.getResources().getString(R.string.share_content);
		Utils.shareMsg(BestSetting.this, shareTitle, shareTitle, shareContent, dir.getAbsolutePath() + "/" + "电量统计曲线图");
		File file = new File(dir.getAbsolutePath() + "/" + current );
		file.deleteOnExit();
	}
	public void createGraphicalView() {
		ChartView chartView = new ChartView(this);
		chart = chartView.getData();
		chart.setBackgroundColor(0x88222222);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = 30;
		params.leftMargin = 30;
		params.rightMargin = 30;
		params.bottomMargin = Utils.dip2px(this, 45);
		layout.addView(chart, params);
		
	}
	public void backButton(View v) {
		this.finish();
	}
}
