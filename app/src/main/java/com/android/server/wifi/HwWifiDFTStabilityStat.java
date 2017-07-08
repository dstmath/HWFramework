package com.android.server.wifi;

public class HwWifiDFTStabilityStat {
    public int mCloseCount;
    public int mCloseDuration;
    public int mCloseSuccCount;
    public boolean mIsScanAlwaysAvalible;
    public boolean mIsWifiNotificationOn;
    public boolean mIsWifiProON;
    public int mOPenDuration;
    public int mOPenSuccCount;
    public int mOpenCount;
    public int mScanAlwaysSwCnt;
    public int mWifiNotifationSwCnt;
    public int mWifiProSwcnt;
    public byte mWifiSleepPolicy;
    public int mWifiSleepSwCnt;
    public byte mWifiToPdp;
    public int mWifiToPdpSwCnt;

    public HwWifiDFTStabilityStat() {
        this.mOpenCount = 0;
        this.mOPenSuccCount = 0;
        this.mOPenDuration = 0;
        this.mCloseCount = 0;
        this.mCloseSuccCount = 0;
        this.mCloseDuration = 0;
        this.mIsWifiProON = false;
        this.mWifiProSwcnt = 0;
        this.mIsScanAlwaysAvalible = false;
        this.mScanAlwaysSwCnt = 0;
        this.mIsWifiNotificationOn = false;
        this.mWifiNotifationSwCnt = 0;
        this.mWifiSleepPolicy = (byte) 0;
        this.mWifiSleepSwCnt = 0;
        this.mWifiToPdp = (byte) 0;
        this.mWifiToPdpSwCnt = 0;
    }
}
