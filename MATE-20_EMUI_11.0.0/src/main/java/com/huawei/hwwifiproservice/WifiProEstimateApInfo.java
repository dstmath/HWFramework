package com.huawei.hwwifiproservice;

import com.android.server.wifi.hwUtil.StringUtilEx;

public class WifiProEstimateApInfo {
    private String mApBssid = null;
    private int mApRssi = 0;
    private String mApSsid = null;
    private int mAuthType = 0;
    private boolean mIs5gAp = true;
    private int mIsDualbandAp = 0;
    private int mRetHistoryScore = 0;
    private int mRetRssiTH = 0;

    WifiProEstimateApInfo() {
    }

    public String toString() {
        return "mApBssid:" + StringUtilEx.safeDisplayBssid(this.mApBssid) + ", mApSsid:" + StringUtilEx.safeDisplaySsid(this.mApSsid) + ", mAuthType:" + this.mAuthType + ", mApRssi:" + this.mApRssi + ", mRetRssiTH:" + this.mRetRssiTH + ", mRetHistoryScore:" + this.mRetHistoryScore + ", mIsDualbandAp:" + this.mIsDualbandAp + ", mIs5gAp:" + this.mIs5gAp;
    }

    public String getApBssid() {
        return this.mApBssid;
    }

    public void setApBssid(String bssid) {
        this.mApBssid = bssid;
    }

    public void setEstimateApSsid(String ssid) {
        this.mApSsid = ssid;
    }

    public String getApSsid() {
        return this.mApSsid;
    }

    public int getApAuthType() {
        return this.mAuthType;
    }

    public void setApAuthType(int authType) {
        this.mAuthType = authType;
    }

    public int getApRssi() {
        return this.mApRssi;
    }

    public void setApRssi(int apRssi) {
        this.mApRssi = apRssi;
    }

    public int getRetRssiTH() {
        return this.mRetRssiTH;
    }

    public void setRetRssiTH(int rssiTH) {
        this.mRetRssiTH = rssiTH;
    }

    public int getRetHistoryScore() {
        return this.mRetHistoryScore;
    }

    public void setRetHistoryScore(int retScore) {
        this.mRetHistoryScore = retScore;
    }

    public int getDualbandAPType() {
        return this.mIsDualbandAp;
    }

    public void setDualbandAPType(int dualbandAp) {
        this.mIsDualbandAp = dualbandAp;
    }

    public boolean is5GAP() {
        return this.mIs5gAp;
    }

    public void set5GAP(boolean is5gAp) {
        this.mIs5gAp = is5gAp;
    }
}
