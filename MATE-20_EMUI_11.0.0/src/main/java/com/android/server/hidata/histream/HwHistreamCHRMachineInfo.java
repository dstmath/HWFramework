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
        HwHiStreamUtils.logD(false, "printCHRMachineInfo mApkName = %{public}s mScenario = %{public}d mRxTup1Bef = %{public}d mRxTup2Bef = %{public}d mScenario = %{public}d mChLoad = %{public}d mTxFail1Bef = %{public}d mTxFail2Bef = %{public}d mStreamQoe = %{public}d mWechatVideoQoe = %{public}d mRAT = %{public}d mWifiRssi = %{public}d mWifiSnr = %{public}d mCellSig = %{public}d mCellQuality = %{public}d mCellSinr = %{public}d mNetDlTup = %{public}d mNetRtt = %{public}d", this.mApkName, Integer.valueOf(this.mScenario), Integer.valueOf(this.mRxTup1Bef), Integer.valueOf(this.mRxTup2Bef), Integer.valueOf(this.mScenario), Integer.valueOf(this.mChLoad), Integer.valueOf(this.mTxFail1Bef), Integer.valueOf(this.mTxFail2Bef), Integer.valueOf(this.mStreamQoe), Integer.valueOf(this.mWechatVideoQoe), Integer.valueOf(this.mRAT), Integer.valueOf(this.mWifiRssi), Integer.valueOf(this.mWifiSnr), Integer.valueOf(this.mCellSig), Integer.valueOf(this.mCellQuality), Integer.valueOf(this.mCellSinr), Integer.valueOf(this.mNetDlTup), Integer.valueOf(this.mNetRtt));
    }
}
