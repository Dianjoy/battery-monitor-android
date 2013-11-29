package com.dianjoy.batterymonitor;

import android.graphics.drawable.Drawable;

public class AppInfo {
	private Drawable icon;
	private String packName;
	private String name;
	public AppInfo(Drawable i, String p, String n) {
		icon = i;
		packName = p;
		name = n;
	}
	public Drawable getIcon() {
		return this.icon;
	}
	public String getPackName () {
		return this.packName;
	}
	public String getName() {
		return this.name;
	}
}