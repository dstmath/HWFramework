package com.android.server.hidata.histream;

import com.android.server.hidata.appqoe.HwAPPQoEUtils;

public class HwHistreamCHRStallInfo {
    public String mAPKName = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mApRtt = -1;
    public int mCellRsrq = -1;
    public int mCellSig = -1;
    public int mCellSinr = -1;
    public int mDlTup = -1;
    public int mEventId = -1;
    public int mNeiborApRssi = -1;
    public int mNetDlTup = -1;
    public int mNetRtt = -1;
    public int mRAT = -1;
    public int mScenario = -1;
    public int mUlTup = -1;
    public int mWifiChload = -1;
    public int mWifiSnr = -1;

    public void printCHRStallInfo() {
        HwHiStreamUtils.logD("printCHRStallInfo mEventId = " + this.mEventId + " mAPKName = " + this.mAPKName + " mScenario = " + this.mScenario + " mRAT = " + this.mRAT + " mUlTup = " + this.mUlTup + " mDlTup = " + this.mDlTup + " mApRtt = " + this.mApRtt + " mNetRtt = " + this.mNetRtt + " mCellSig = " + this.mCellSig + " mCellRsrq = " + this.mCellRsrq + " mCellSinr = " + this.mCellSinr + " mNeiborApRssi = " + this.mNeiborApRssi + " mWifiSnr = " + this.mWifiSnr + " mWifiChload = " + this.mWifiChload + " mNetDlTup = " + this.mNetDlTup);
    }
}
