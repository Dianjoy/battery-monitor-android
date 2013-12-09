package com.dianjoy.batterymonitor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

import com.dianjoy.batterymonitor.tools.Cons;
import com.dianjoy.batterymonitor.tools.DBManager;

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
		 // double[] x = new double[number];
		 // double[] y = new double[number];
		  /*for(int i = 0; i < number; i ++) {
			  x[i] = Double.valueOf(times[i]);
			  y[i] = Double.valueOf(counts[i]);
			  
		  }*/
		 /* int count = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_COUNT, Cons.BATTERY_PRE, "0"));
		  int begin = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_COUNT_BEGIN, Cons.BATTERY_PRE, "0"));
		  Vector<Double> times = new Vector<Double>();
		  Vector<Double> levels = new Vector<Double>();
		  long current = System.currentTimeMillis();
		  DecimalFormat df=new DecimalFormat(".##");
		  for (int i = 0; i < count; i ++) {
			  int offset = (begin + i) % Cons.MAX_COUNT;
			  String status = Utils.getPreferenceStr(context, Cons.BATTERY_STATUSES + offset, Cons.BATTERY_PRE, Cons.BATTERY_DISCHARGE);
			  long time = Long.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_TIME + offset, Cons.BATTERY_PRE, "0"));
			  int level = Integer.valueOf(Utils.getPreferenceStr(context, Cons.BATTERY_LEVEL + offset, Cons.BATTERY_PRE, "0"));
			  if (time != 0 && level != 0) {
				  double perids = (time - current)/((double)1000 * 60 * 60);
				  times.add(Double.valueOf(df.format(perids)));
				  levels.add((double)level);
			  }
		  }
		  if(times.size() == 0) {
			  times.add(-1.0);
			  levels.add(55.0);
		  }
		  double[] x = new double[times.size()];
		  double[] y = new double[times.size()];
		  for(int i = 0 ; i < times.size(); i ++) {
			  x[i] = times.get(i);
			  y[i] = levels.get(i);
		  }*/
		//  double[] x = {1, 2, 3, 5, 6,7,8,10,11,23};
		//  double[] y = {100, 90, 80 , 40, 60, 70, 86, 23, 45, 10};
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
		  int[] colors = new int[] {Color.LTGRAY};
		  PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE};
		  XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		  for(int i = 0; i < renderer.getSeriesRendererCount(); i ++){
			  ((XYSeriesRenderer)renderer.getSeriesRendererAt(i)).setFillPoints(true);
			  
		  }
		  setChartSettings(renderer, "Battery", "Time", "Battery data", -24, 0, 0, 100, Color.LTGRAY, Color.LTGRAY);
		  renderer.setXLabels(7);
		  renderer.setYLabels(5);
		  renderer.setShowGrid(true);
		  Typeface fontFace = Typeface.createFromAsset(context.getAssets(),
					"fonts/HelveticaNeueLTStd-Th.otf");
		  renderer.setTextTypeface(fontFace);
		  renderer.setMarginsColor(0x88222222);
		  renderer.setXLabelsAlign(Align.RIGHT);
		  renderer.setYLabelsAlign(Align.RIGHT);
		//  renderer.setZoomButtonsVisible(true);
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
	  protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		    renderer.setAxisTitleTextSize(16);
		    renderer.setChartTitleTextSize(20);
		    renderer.setLabelsTextSize(15);
		    renderer.setLegendTextSize(15);
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
