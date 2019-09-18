package com.android.server.wifi.wifipro.hwintelligencewifi;

import java.util.ArrayList;
import java.util.List;

public class APInfoData {
    private int mAuthType;
    private String mBssid;
    private List<CellInfoData> mCellInfos = new ArrayList();
    private boolean mInblackList;
    private boolean mIsHomeAp;
    private List<String> mNearbyAPInfos = new ArrayList();
    private String mSsid;
    private long mTime;

    public APInfoData(String bssid, String ssid, int inblacklist, int authtype, long time, int isHomeAp) {
        this.mBssid = bssid != null ? bssid : "00:00:00:00";
        this.mSsid = ssid != null ? ssid : "null";
        this.mAuthType = authtype;
        boolean z = false;
        this.mInblackList = inblacklist == 1;
        this.mTime = time;
        this.mIsHomeAp = isHomeAp == 1 ? true : z;
    }

    public boolean isHomeAp() {
        return this.mIsHomeAp;
    }

    public void setHomeAp(boolean ishomeAp) {
        this.mIsHomeAp = ishomeAp;
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
