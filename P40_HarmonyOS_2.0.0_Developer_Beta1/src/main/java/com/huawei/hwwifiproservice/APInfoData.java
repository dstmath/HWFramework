package com.huawei.hwwifiproservice;

import java.util.ArrayList;
import java.util.List;

public class APInfoData {
    private int mAuthType;
    private String mBssid;
    private List<CellInfoData> mCellInfos = new ArrayList();
    private int mFrequency;
    private boolean mInblackList;
    private boolean mIsHomeAp;
    private List<String> mNearbyAPInfos = new ArrayList();
    private String mSsid;
    private long mTime;

    public APInfoData(String bssid, String ssid, int inblacklist, int authtype, long time, int isHomeAp, int frequency) {
        this.mBssid = bssid != null ? bssid : "00:00:00:00";
        this.mSsid = ssid != null ? ssid : "null";
        this.mAuthType = authtype;
        boolean z = false;
        this.mInblackList = inblacklist == 1;
        this.mTime = time;
        this.mIsHomeAp = isHomeAp == 1 ? true : z;
        this.mFrequency = frequency;
    }

    public String getBssid() {
        String str = this.mBssid;
        return str != null ? str : "00:00:00:00";
    }

    public List<CellInfoData> getCellInfos() {
        return this.mCellInfos;
    }

    public void setCellInfo(List<CellInfoData> infos) {
        this.mCellInfos = infos;
    }

    public String getSsid() {
        String str = this.mSsid;
        return str != null ? str : "null";
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

    public int getFrequency() {
        return this.mFrequency;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }
}
