package com.android.server.wifi.ABS;

public class HwAbsApInfoData implements Comparable {
    protected int mAuthType;
    protected String mBssid;
    protected int mContinuousFailureTimes;
    protected int mFailedTimes;
    protected int mInBlackList;
    protected long mLastConnectTime;
    protected int mReassociateTimes;
    protected String mSsid;
    protected int mSwitchMimoType;
    protected int mSwitchSisoType;

    public HwAbsApInfoData(String bssid, String ssid, int switchMimoType, int switchSisoType, int authType, int inBlackList, int reassociateTimes, int failedTimes, int continuousFailureTimes, long lastConnectTime) {
        this.mBssid = bssid != null ? bssid : "00:00:00:00";
        this.mSsid = ssid != null ? ssid : "null";
        this.mSwitchMimoType = switchMimoType;
        this.mSwitchSisoType = switchSisoType;
        this.mAuthType = authType;
        this.mInBlackList = inBlackList;
        this.mReassociateTimes = reassociateTimes;
        this.mFailedTimes = failedTimes;
        this.mContinuousFailureTimes = continuousFailureTimes;
        this.mLastConnectTime = lastConnectTime;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        return Long.compare(this.mLastConnectTime, ((HwAbsApInfoData) obj).mLastConnectTime);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Long.valueOf(this.mLastConnectTime).hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && (obj instanceof HwAbsApInfoData) && ((HwAbsApInfoData) obj).mLastConnectTime == this.mLastConnectTime) {
            return true;
        }
        return false;
    }
}
