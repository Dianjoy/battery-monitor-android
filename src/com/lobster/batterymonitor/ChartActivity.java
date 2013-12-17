package com.lobster.batterymonitor;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.dianjoy.batterymonitor.R;
import com.lobster.batterymonitor.view.ChartView;

public class ChartActivity extends UmentActivity {
	private LinearLayout batteryCount;
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count);
		batteryCount = (LinearLayout) findViewById(R.id.count_gridView);
		createGraphicalView();
	}
	
	public void backButton(View v) {
		this.finish();
	}
	public void createGraphicalView() {
		ChartView chartView = new ChartView(this);
		GraphicalView chart = chartView.getData();
		chart.setBackgroundColor(0x88222222);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 400);
		params.topMargin = 30;
		params.leftMargin = 30;
		params.rightMargin = 30;
		batteryCount.addView(chart, params);
		
	}
}
