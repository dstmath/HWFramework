package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner.ScanSettings;

public abstract class AbsWifiConnectivityManager {
    protected int getPeriodicSingleScanInterval() {
        return -1;
    }

    protected void handleSingleScanFailure(int reason) {
    }

    protected void handleSingleScanSuccess() {
    }

    protected void handleScanCountChanged(int reason) {
    }

    protected boolean isSupportWifiScanGenie() {
        return false;
    }

    protected boolean handleForceScan() {
        return false;
    }

    protected boolean isWifiScanSpecialChannels() {
        return false;
    }

    protected ScanSettings getScanGenieSettings() {
        return null;
    }

    public String unselectDhcpFailedBssid(String targetBssid, String scanResultBssid, WifiConfiguration candidate) {
        return targetBssid;
    }

    protected boolean isScanThisPeriod(boolean isP2pConn) {
        return true;
    }

    protected void extendWifiScanPeriodForP2p(boolean bExtend, int iTimes) {
    }

    protected void resetPeriodicSingleScanInterval() {
    }
}
