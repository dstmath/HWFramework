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
        HwHiStreamUtils.logD("printCHRHandoverInfo mEventId = " + this.mEventId + " mCallId = " + this.mCallId + " mApkName = " + this.mApkName + " mScenario = " + this.mScenario + " mEventType = " + this.mEventType + " mWifiSsidAft = " + this.mWifiSsidAft + " mWifiRssiAft = " + this.mWifiRssiAft + " mWifiChAft = " + this.mWifiChAft + " mWifiRxTupAft = " + this.mWifiRxTupAft + " mWifiSsidBef = " + this.mWifiSsidBef + " mWifiRssiBef = " + this.mWifiRssiBef + " mWifiChBef = " + this.mWifiChBef + " mWifiRxTup1Bef = " + this.mWifiRxTup1Bef + " mWifiRxTup2Bef = " + this.mWifiRxTup2Bef + " mApType = " + this.mApType + " mWifiTxFail1Bef = " + this.mWifiTxFail1Bef + " mWifiTxFail2Bef = " + this.mWifiTxFail2Bef + " mWifiChLoad = " + this.mWifiChLoad + " mCellRat = " + this.mCellRat + " mCellSig = " + this.mCellSig + " mCellFreq = " + this.mCellFreq + " mCellRxTup = " + this.mCellRxTup + " mSwitchCauseBef = " + this.mSwitchCauseBef + " mStreamQoeBef = " + this.mStreamQoeBef + " mStreamQoeAft = " + this.mStreamQoeAft);
    }
}
