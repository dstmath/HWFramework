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
        HwHiStreamUtils.logD(false, "printCHRStallInfo mEventId = %{public}d mAPKName = %{public}s mScenario = %{public}d mRAT = %{public}d mUlTup = %{public}d mDlTup = %{public}d mApRtt = %{public}d mNetRtt = %{public}d mCellSig = %{public}d mCellRsrq = %{public}d mCellSinr = %{public}d mNeiborApRssi = %{public}d mWifiSnr = %{public}d mWifiChload = %{public}d mNetDlTup = %{public}d", Integer.valueOf(this.mEventId), this.mAPKName, Integer.valueOf(this.mScenario), Integer.valueOf(this.mRAT), Integer.valueOf(this.mUlTup), Integer.valueOf(this.mDlTup), Integer.valueOf(this.mApRtt), Integer.valueOf(this.mNetRtt), Integer.valueOf(this.mCellSig), Integer.valueOf(this.mCellRsrq), Integer.valueOf(this.mCellSinr), Integer.valueOf(this.mNeiborApRssi), Integer.valueOf(this.mWifiSnr), Integer.valueOf(this.mWifiChload), Integer.valueOf(this.mNetDlTup));
    }
}
