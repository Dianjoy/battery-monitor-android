package com.dianjoy.batterymonitor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

import com.dianjoy.batterymonitor.tools.Cons;
import com.dianjoy.batterymonitor.tools.DBManager;
import com.dianjoy.batterymonitor.tools.Utils;

public class ChartView {
	private Context context;
	private List<double[]> yData, xData;
	public ChartView(Context c) {
		context = c;
		xData = new ArrayList<double[]>();
		yData = new ArrayList<double[]>();
	}
	/**
	   * Sets a few of the series renderer settings.
	   * 
	   * @param renderer the renderer to set the properties to
	   * @param title the chart title
	   * @param xTitle the title for the X axis
	   * @param yTitle the title for the Y axis
	   * @param xMin the minimum value on the X axis
	   * @param xMax the maximum value on the X axis
	   * @param yMin the minimum value on the Y axis
	   * @param yMax the maximum value on the Y axis
	   * @param axesColor the axes color
	   * @param labelsColor the labels color
	   */
	  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
	      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
	      int labelsColor) {
	    renderer.setChartTitle(title);
	    renderer.setXTitle(xTitle);
	    renderer.setYTitle(yTitle);
	    renderer.setXAxisMin(xMin);
	    renderer.setXAxisMax(xMax);
	    renderer.setYAxisMin(yMin);
	    renderer.setYAxisMax(yMax);
	    renderer.setAxesColor(axesColor);
	    renderer.setLabelsColor(labelsColor);
	  }
	  public GraphicalView getData() {
		  DBManager db = new DBManager(context, "battery_message");
		  HashMap<String, Object[]> data = db.query(Cons.MAX_COUNT);
		  double[] x,y;
		  Long[] t = (Long[])data.get(Cons.BATTERY_TIME);
		  Integer[] l = (Integer[])data.get(Cons.BATTERY_LEVEL);
		  x = new double[t.length];
		  y = new double[l.length];
		  long current = System.currentTimeMillis();
		  for(int i = 0; i < t.length; i ++) {
			  x[i] = (double)(t[i] - current)/ (1000 * 3600);
			  y[i] = l[i];
		  }
		  xData.add(x);
		  yData.add(y);
		  String[] titles = {"battery"};
		  int[] colors = new int[] {Color.GREEN};
		  PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
		//  XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		  XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		  setRenderer(renderer);
		  XYSeriesRenderer rxy = new XYSeriesRenderer();
		  renderer.addSeriesRenderer(rxy);
		  rxy.setPointStyle(styles[0]);		  
		  rxy.setLineWidth(Utils.dip2px(context, 2));
		  rxy.setShowLegendItem(false);
		  renderer.setXLabels(0);
		  for(int i = 0; i < 8; i ++) {
			  renderer.addXTextLabel(-i, i+"h");
		  }
		  renderer.setYLabels(5);
		  rxy.setColor(0xee00ee00);
		  for(int i = 0; i < renderer.getSeriesRendererCount(); i ++){
			  ((XYSeriesRenderer)renderer.getSeriesRendererAt(i)).setFillPoints(true);
			  
		  }
		  String hour = (int)-x[0] + "小时" + (int)((-x[0] - (int)(-x[0])) * 60) + "分钟前";
		  hour = "最近8小时的电量统计";
		  setChartSettings(renderer, "", hour, "电池电量", -8, 0, 0, 100, Color.LTGRAY, Color.LTGRAY);
		  Typeface fontFace = Typeface.createFromAsset(context.getAssets(),
					"fonts/HelveticaNeueLTStd-Th.otf");
		  renderer.setTextTypeface(fontFace);
		  renderer.setMarginsColor(0xeeeeee);
		  renderer.setBackgroundColor(0xeeeeee);
		  renderer.setXLabelsAlign(Align.RIGHT);
		  renderer.setYLabelsAlign(Align.RIGHT);
		  renderer.setZoomButtonsVisible(false);
		  renderer.setPanLimits(new double[] { 0, 20, 0, 40 });
		  renderer.setZoomLimits(new double[] { 0, 20, 0, 40 });
		  GraphicalView view = ChartFactory.getLineChartView(context, buildDateDataset(titles, xData, yData), renderer);
		  db.closeDB();
		  return view;
	  }
	  protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<double[]> xValues,
		      List<double[]> yValues) {
		    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    int length = titles.length;
		    for (int i = 0; i < length; i++) {
		      TimeSeries series = new TimeSeries(titles[i]);
		      double[] xV = xValues.get(i);
		      double[] yV = yValues.get(i);
		      int seriesLength = xV.length;
		      for (int k = 0; k < seriesLength; k++) {
		        series.add(xV[k], yV[k]);
		      }
		      dataset.addSeries(series);
		    }
		    return dataset;
	}
	 protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		    setRenderer(renderer, colors, styles);
		    return renderer;
	 }
	 public void setRenderer(XYMultipleSeriesRenderer renderer) {
		 float size = Utils.dip2px(context, 10);
		 renderer.setAxisTitleTextSize(size);
		 renderer.setChartTitleTextSize(size);
		 renderer.setLabelsTextSize(size);
		 renderer.setLegendTextSize(size);
		 renderer.setPointSize(5f);
		 renderer.setMargins(new int[] { Utils.dip2px(context, 10), Utils.dip2px(context, 20), Utils.dip2px(context,20), Utils.dip2px(context, 10) });
	 }
	  protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		    renderer.setAxisTitleTextSize(16);
		    renderer.setChartTitleTextSize(20);
		    renderer.setLabelsTextSize(15);
		    renderer.setLegendTextSize(20);
		    renderer.setPointSize(5f);
		    renderer.setMargins(new int[] { 20, 30, 15, 20 });
		    int length = colors.length;
		    for (int i = 0; i < length; i++) {
		      XYSeriesRenderer r = new XYSeriesRenderer();
		      r.setColor(colors[i]);
		      r.setPointStyle(styles[i]);
		      renderer.addSeriesRenderer(r);
		    }
	}



}
