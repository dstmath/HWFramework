package com.android.server.wifi.ABS;

public class HwAbsChrBlackListEvent {
    protected int mAbsAddReason;
    protected String mAbsApBssid;
    protected String mAbsApSsid;
    protected int mAbsBlackListNum;
    protected int mAbsFailedTimes;
    protected int mAbsSuportVowifi;
    protected int mAbsSwitchTimes;
    protected int mAbsTotalNum;

    public HwAbsChrBlackListEvent() {
        this.mAbsAddReason = 0;
        this.mAbsSwitchTimes = 0;
        this.mAbsFailedTimes = 0;
        this.mAbsTotalNum = 0;
        this.mAbsBlackListNum = 0;
        this.mAbsApSsid = "NA";
        this.mAbsApBssid = "NA";
        this.mAbsSuportVowifi = 0;
        this.mAbsAddReason = 0;
        this.mAbsSwitchTimes = 0;
        this.mAbsFailedTimes = 0;
        this.mAbsTotalNum = 0;
        this.mAbsBlackListNum = 0;
    }
}
