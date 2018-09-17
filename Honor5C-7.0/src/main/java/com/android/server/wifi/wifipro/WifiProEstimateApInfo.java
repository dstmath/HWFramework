package com.android.server.wifi.wifipro;

public class WifiProEstimateApInfo {
    private String mApBssid;
    private int mApRssi;
    private String mApSsid;
    private int mAuthType;
    private boolean mIs5GAP;
    private int mIsDualbandAP;
    private int mRetHistoryScore;
    private int mRetRssiTH;

    WifiProEstimateApInfo() {
        this.mApBssid = null;
        this.mApSsid = null;
        this.mAuthType = 0;
        this.mRetRssiTH = 0;
        this.mApRssi = 0;
        this.mRetHistoryScore = 0;
        this.mIsDualbandAP = 0;
        this.mIs5GAP = true;
    }

    public String toString() {
        return "mApBssid:" + this.mApBssid + ", mApSsid:" + this.mApSsid + ", mAuthType:" + this.mAuthType + ", mApRssi:" + this.mApRssi + ", mRetRssiTH:" + this.mRetRssiTH + ", mRetHistoryScore:" + this.mRetHistoryScore + ", mIsDualbandAP:" + this.mIsDualbandAP + ", mIs5GAP:" + this.mIs5GAP;
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
        return this.mIsDualbandAP;
    }

    public void setDualbandAPType(int dualbandap) {
        this.mIsDualbandAP = dualbandap;
    }

    public boolean is5GAP() {
        return this.mIs5GAP;
    }

    public void set5GAP(boolean is5GAP) {
        this.mIs5GAP = is5GAP;
    }
}
