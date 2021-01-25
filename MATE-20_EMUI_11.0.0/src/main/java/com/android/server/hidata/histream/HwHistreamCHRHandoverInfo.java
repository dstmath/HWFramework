package com.android.server.hidata.histream;

import com.android.server.hidata.wavemapping.chr.entity.HistAppQoeChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;

public class HwHistreamCHRHandoverInfo {
    public int mApType = -1;
    public String mApkName = Constant.USERDB_APP_NAME_NONE;
    public int mCallId = -1;
    public int mCellFreq = 1;
    public int mCellQuality = -1;
    public int mCellRat = -1;
    public int mCellRxTup = -1;
    public int mCellSig = -1;
    public int mCellSinr = -1;
    public int mEventId = -1;
    public int mEventType = -1;
    public int mRttBef = -1;
    public int mScenario = -1;
    public int mStreamQoeAft = -1;
    public int mStreamQoeBef = -1;
    public int mSwitchCauseBef = -1;
    public int mTupBef = -1;
    public HistAppQoeChrInfo mWavemappingInfo = null;
    public int mWifiChAft = -1;
    public int mWifiChBef = -1;
    public int mWifiChLoad = -1;
    public int mWifiRssiAft = -1;
    public int mWifiRssiBef = -1;
    public int mWifiRxTup1Bef = -1;
    public int mWifiRxTup2Bef = -1;
    public int mWifiRxTupAft = -1;
    public int mWifiSnr = -1;
    public String mWifiSsidAft = Constant.USERDB_APP_NAME_NONE;
    public String mWifiSsidBef = Constant.USERDB_APP_NAME_NONE;
    public int mWifiTxFail1Bef = -1;
    public int mWifiTxFail2Bef = -1;

    public void printCHRHandoverInfo() {
        HwHiStreamUtils.logD(false, "printCHRHandoverInfo mEventId = %{public}d mCallId = %{public}d mApkName = %{public}s mScenario = %{public}d mEventType = %{public}d mWifiSsidAft = %{public}s mWifiRssiAft = %{public}d mWifiChAft = %{public}d mWifiRxTupAft = %{public}d mWifiSsidBef = %{public}s mWifiRssiBef = %{public}d mWifiChBef = %{public}d mWifiRxTup1Bef = %{public}d mWifiRxTup2Bef = %{public}d mApType = %{public}d mWifiTxFail1Bef = %{public}d mWifiTxFail2Bef = %{public}d mWifiChLoad = %{public}d mCellRat = %{public}d mCellSig = %{public}d mCellFreq = %{public}d mCellRxTup = %{public}d mSwitchCauseBef = %{public}d mStreamQoeBef = %{public}d mStreamQoeAft = %{public}d", Integer.valueOf(this.mEventId), Integer.valueOf(this.mCallId), this.mApkName, Integer.valueOf(this.mScenario), Integer.valueOf(this.mEventType), this.mWifiSsidAft, Integer.valueOf(this.mWifiRssiAft), Integer.valueOf(this.mWifiChAft), Integer.valueOf(this.mWifiRxTupAft), this.mWifiSsidBef, Integer.valueOf(this.mWifiRssiBef), Integer.valueOf(this.mWifiChBef), Integer.valueOf(this.mWifiRxTup1Bef), Integer.valueOf(this.mWifiRxTup2Bef), Integer.valueOf(this.mApType), Integer.valueOf(this.mWifiTxFail1Bef), Integer.valueOf(this.mWifiTxFail2Bef), Integer.valueOf(this.mWifiChLoad), Integer.valueOf(this.mCellRat), Integer.valueOf(this.mCellSig), Integer.valueOf(this.mCellFreq), Integer.valueOf(this.mCellRxTup), Integer.valueOf(this.mSwitchCauseBef), Integer.valueOf(this.mStreamQoeBef), Integer.valueOf(this.mStreamQoeAft));
    }
}
