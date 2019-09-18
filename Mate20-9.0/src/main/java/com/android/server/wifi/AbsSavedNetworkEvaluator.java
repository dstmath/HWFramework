package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

public abstract class AbsSavedNetworkEvaluator {
    public boolean isWifiProEnabledOrSelfCureGoing() {
        return false;
    }

    public void resetHwSelectedCandidates() {
    }

    public boolean selectBestNetworkByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        return false;
    }

    public WifiConfiguration getLastCandidateByWifiPro(WifiConfiguration config, ScanResult scanResultCandidate) {
        return config;
    }

    public void portalNotifyChanged(boolean on, String ssid, boolean hasInternetAccess) {
    }

    public void setUserSelectPortalFlag(boolean userSelect) {
    }

    public void resetSelfCureCandidateLostCnt() {
    }

    public boolean unselectDueToFailedLastTime(ScanResult scanResult, WifiConfiguration config) {
        return false;
    }

    public boolean unselectDiscNonLocally(ScanResult scanResult, WifiConfiguration config) {
        return false;
    }
}
