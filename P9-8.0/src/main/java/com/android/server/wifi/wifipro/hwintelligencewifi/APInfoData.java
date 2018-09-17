package com.android.server.wifi.wifipro.hwintelligencewifi;

import java.util.ArrayList;
import java.util.List;

public class APInfoData {
    private int mAuthType;
    private String mBssid;
    private List<CellInfoData> mCellInfos = new ArrayList();
    private boolean mInblackList;
    private List<String> mNearbyAPInfos = new ArrayList();
    private String mSsid;
    private long mTime;

    public APInfoData(String bssid, String ssid, int inblacklist, int authtype, long time) {
        boolean z = true;
        if (bssid == null) {
            bssid = "00:00:00:00";
        }
        this.mBssid = bssid;
        if (ssid == null) {
            ssid = "null";
        }
        this.mSsid = ssid;
        this.mAuthType = authtype;
        if (inblacklist != 1) {
            z = false;
        }
        this.mInblackList = z;
        this.mTime = time;
    }

    public String getBssid() {
        return this.mBssid != null ? this.mBssid : "00:00:00:00";
    }

    public List<CellInfoData> getCellInfos() {
        return this.mCellInfos;
    }

    public void setCellInfo(List<CellInfoData> infos) {
        this.mCellInfos = infos;
    }

    public String getSsid() {
        return this.mSsid != null ? this.mSsid : "null";
    }

    public void setSsid(String ssid) {
        this.mSsid = ssid;
    }

    public void setNearbyAPInfos(List<String> infos) {
        this.mNearbyAPInfos = infos;
    }

    public List<String> getNearbyAPInfos() {
        return this.mNearbyAPInfos;
    }

    public long getLastTime() {
        return this.mTime;
    }

    public void setLastTime(long time) {
        this.mTime = time;
    }

    public boolean isInBlackList() {
        return this.mInblackList;
    }

    public void setBlackListFlag(boolean inBlackList) {
        this.mInblackList = inBlackList;
    }

    public int getAuthType() {
        return this.mAuthType;
    }

    public void setAuthType(int type) {
        this.mAuthType = type;
    }
}
