package com.android.server.hidata.histream;

import com.android.server.hidata.appqoe.HwAPPQoEUtils;

public class HwHistreamCHRMachineInfo {
    public String mApkName = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mCellQuality = -1;
    public int mCellSig = -1;
    public int mCellSinr = -1;
    public int mChLoad = -1;
    public int mNetDlTup = -1;
    public int mNetRtt = -1;
    public int mRAT = -1;
    public int mRxTup1Bef = -1;
    public int mRxTup2Bef = -1;
    public int mScenario = -1;
    public int mStreamQoe = -1;
    public int mTxFail1Bef = -1;
    public int mTxFail2Bef = -1;
    public int mWechatVideoQoe = -1;
    public int mWifiRssi = -1;
    public int mWifiSnr = -1;

    public void printCHRMachineInfo() {
        HwHiStreamUtils.logD("printCHRMachineInfo mApkName = " + this.mApkName + " mScenario = " + this.mScenario + " mRxTup1Bef = " + this.mRxTup1Bef + " mRxTup2Bef = " + this.mRxTup2Bef + " mScenario = " + this.mScenario + " mChLoad = " + this.mChLoad + " mTxFail1Bef = " + this.mTxFail1Bef + " mTxFail2Bef = " + this.mTxFail2Bef + " mStreamQoe = " + this.mStreamQoe + " mWechatVideoQoe = " + this.mWechatVideoQoe + " mRAT = " + this.mRAT + " mWifiRssi = " + this.mWifiRssi + " mWifiSnr = " + this.mWifiSnr + " mWifiSnr = " + this.mWifiSnr + " mCellSig = " + this.mCellSig + " mCellQuality = " + this.mCellQuality + " mCellSinr = " + this.mCellSinr + " mNetDlTup = " + this.mNetDlTup + " mNetRtt = " + this.mNetRtt);
    }
}
