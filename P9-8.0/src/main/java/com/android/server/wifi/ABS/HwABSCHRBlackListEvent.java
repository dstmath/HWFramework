package com.android.server.wifi.ABS;

public class HwABSCHRBlackListEvent {
    public int mABSAddReason;
    public String mABSApBssid;
    public String mABSApSsid;
    public int mABSBlackListNum;
    public int mABSFailedTimes;
    public int mABSSuportVoWifi;
    public int mABSSwitchTimes;
    public int mABSTotalNum;

    public HwABSCHRBlackListEvent() {
        this.mABSAddReason = 0;
        this.mABSSwitchTimes = 0;
        this.mABSFailedTimes = 0;
        this.mABSTotalNum = 0;
        this.mABSBlackListNum = 0;
        this.mABSApSsid = "NA";
        this.mABSApBssid = "NA";
        this.mABSSuportVoWifi = 0;
        this.mABSAddReason = 0;
        this.mABSSwitchTimes = 0;
        this.mABSFailedTimes = 0;
        this.mABSTotalNum = 0;
        this.mABSBlackListNum = 0;
    }
}
