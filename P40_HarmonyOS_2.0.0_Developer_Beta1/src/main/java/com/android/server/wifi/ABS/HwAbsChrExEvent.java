package com.android.server.wifi.ABS;

public class HwAbsChrExEvent {
    protected int mAbsApAuthType;
    protected String mAbsApBssid;
    protected int mAbsApChannel;
    protected int mAbsApRssi;
    protected String mAbsApSsid;
    protected int mSwitchType;

    public HwAbsChrExEvent() {
        this.mAbsApRssi = 0;
        this.mAbsApChannel = 0;
        this.mSwitchType = 0;
        this.mAbsApSsid = "NA";
        this.mAbsApBssid = "NA";
        this.mAbsApAuthType = 0;
        this.mAbsApRssi = 0;
        this.mAbsApChannel = 0;
        this.mSwitchType = 0;
    }
}
