package com.lobster.batterymonitor;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WiFiManagerOp {
	private WifiManager wifiManager;// �����������

	private WifiInfo wifiInfo;// Wifi��Ϣ

	private List<ScanResult> scanResultList; // ɨ����������������б�

	private List<WifiConfiguration> wifiConfigList;// ���������б�

	private WifiLock wifiLock;// Wifi��

	public WiFiManagerOp(Context context) {
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);// ��ȡWifi����

		// �õ�Wifi��Ϣ
		this.wifiInfo = wifiManager.getConnectionInfo();// �õ�������Ϣ

	}

	public boolean getWifiStatus() {
		return wifiManager.isWifiEnabled();
	}

	// ��/�ر� wifi
	public  boolean openWifi() {
		if (!wifiManager.isWifiEnabled()) {
			return wifiManager.setWifiEnabled(true);
		} else {
			return false;
		}

	}

	public boolean closeWifi() {
		if (!wifiManager.isWifiEnabled()) {
			return true;
		} else {
			return wifiManager.setWifiEnabled(false);
		}
	}

	// ��/����wifi
	// ��ʵ��WiFI�����ж�wifi�Ƿ����ɹ���������ʹ�õ���held�����ֵ���˼acquire �õ���
	public void lockWifi() {

		wifiLock.acquire();

	}

	public void unLockWifi() {
		if (!wifiLock.isHeld()) {
			wifiLock.release(); // �ͷ���Դ
		}
	}

	// �ұ�����д�ڹ��캯�����ˣ����ǿ��ǵ�����ÿ�ζ���ʹ��Wifi�����Ըɴ��Լ�����һ����������Ҫʱ���ã�������OK
	public void createWifiLock() {
		wifiLock = wifiManager.createWifiLock("flyfly"); // ����һ����ı�־
	}

	// ɨ������

	public void startScan() {
		wifiManager.startScan();

		scanResultList = wifiManager.getScanResults(); // ɨ�践�ؽ���б�

		wifiConfigList = wifiManager.getConfiguredNetworks(); // ɨ�������б�
	}

	public List<ScanResult> getWifiList() {
		return scanResultList;
	}

	public List<WifiConfiguration> getWifiConfigList() {
		return wifiConfigList;
	}

	// ��ȡɨ���б�
	public StringBuilder lookUpscan() {
		StringBuilder scanBuilder = new StringBuilder();

		for (int i = 0; i < scanResultList.size(); i++) {
			scanBuilder.append("��ţ�" + (i + 1));
			scanBuilder.append(scanResultList.get(i).toString()); // ������Ϣ
			scanBuilder.append("\n");
		}

		return scanBuilder;
	}

	// ��ȡָ���źŵ�ǿ��
	public int getLevel(int NetId) {
		return scanResultList.get(NetId).level;
	}

	// ��ȡ����Mac��ַ
	public String getMac() {
		return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (wifiInfo == null) ? null : wifiInfo.getBSSID();
	}

	public String getSSID() {
		return (wifiInfo == null) ? null : wifiInfo.getSSID();
	}

	// ���ص�ǰ���ӵ������ID
	public int getCurrentNetId() {
		return (wifiInfo == null) ? null : wifiInfo.getNetworkId();
	}

	// ����������Ϣ
	public String getwifiInfo() {
		return (wifiInfo == null) ? null : wifiInfo.toString();
	}

	// ��ȡIP��ַ
	public int getIP() {
		return (wifiInfo == null) ? null : wifiInfo.getIpAddress();
	}

	// ���һ������
	public boolean addNetWordLink(WifiConfiguration config) {
		int NetId = wifiManager.addNetwork(config);
		return wifiManager.enableNetwork(NetId, true);
	}

	// ����һ������
	public boolean disableNetWordLick(int NetId) {
		wifiManager.disableNetwork(NetId);
		return wifiManager.disconnect();
	}

	// �Ƴ�һ������
	public boolean removeNetworkLink(int NetId) {
		return wifiManager.removeNetwork(NetId);
	}

	// ����ʾSSID
	public void hiddenSSID(int NetId) {
		wifiConfigList.get(NetId).hiddenSSID = true;
	}

	// ��ʾSSID
	public void displaySSID(int NetId) {
		wifiConfigList.get(NetId).hiddenSSID = false;
	}
}
