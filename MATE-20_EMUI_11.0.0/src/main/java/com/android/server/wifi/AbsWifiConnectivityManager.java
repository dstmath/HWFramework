package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import java.util.List;

public abstract class AbsWifiConnectivityManager {
    /* access modifiers changed from: protected */
    public int getPeriodicSingleScanInterval() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void handleSingleScanFailure(int reason) {
    }

    /* access modifiers changed from: protected */
    public void handleSingleScanSuccess() {
    }

    /* access modifiers changed from: protected */
    public void handleScanCountChanged(int reason) {
    }

    /* access modifiers changed from: protected */
    public boolean isSupportWifiScanGenie() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleForceScan() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiScanSpecialChannels() {
        return false;
    }

    /* access modifiers changed from: protected */
    public WifiScanner.ScanSettings getScanGenieSettings() {
        return null;
    }

    public String unselectDhcpFailedBssid(String targetBssid, String scanResultBssid, WifiConfiguration candidate) {
        return targetBssid;
    }

    /* access modifiers changed from: protected */
    public boolean isScanThisPeriod(boolean isP2pConn) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void extendWifiScanPeriodForP2p(boolean bExtend, int iTimes) {
    }

    /* access modifiers changed from: protected */
    public void resetPeriodicSingleScanInterval() {
    }

    /* access modifiers changed from: protected */
    public List<ScanResult> getScanResultsHasSameSsid(WifiScanner scanner, String bssid) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void startHourPeriodicSingleScan() {
    }

    /* access modifiers changed from: protected */
    public void scheduleHourPeriodicScanTimer(int intervalMs) {
    }

    /* access modifiers changed from: protected */
    public void stopHourPeriodicSingleScan() {
    }

    public boolean ignorePoorNetwork(WifiConfiguration candidate) {
        return false;
    }

    public boolean forceFullbandScanAgain(boolean mScreenOn, int mWifiState, boolean wasConnectAttempted, boolean isSingleScanStarted) {
        return false;
    }

    public int getWifiScanGenieMinInterval(int mPeriodicScanInterval) {
        return 20000;
    }
}
