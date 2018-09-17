package com.android.server.wifi.ABS;

public class HwABSCHRExEvent {
    public int mABSApAuthType;
    public String mABSApBssid;
    public int mABSApChannel;
    public int mABSApRSSI;
    public String mABSApSsid;
    public int mSwitchType;

    public HwABSCHRExEvent() {
        this.mABSApRSSI = 0;
        this.mABSApChannel = 0;
        this.mSwitchType = 0;
        this.mABSApSsid = "NA";
        this.mABSApBssid = "NA";
        this.mABSApAuthType = 0;
        this.mABSApRSSI = 0;
        this.mABSApChannel = 0;
        this.mSwitchType = 0;
    }
}
