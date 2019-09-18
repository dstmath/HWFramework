package com.android.server.hidata.histream;

import com.android.server.hidata.appqoe.HwAPPQoEUtils;

public class HwHistreamCHRStatisticsInfo {
    public String mApkName = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mCallId = -1;
    public int mCallInCellularDur = 0;
    public int mCallInWiFiDur = 0;
    public long mCallStartTime = 0;
    public int mCellLv1Cnt = 0;
    public int mCellLv2Cnt = 0;
    public int mCellLv3Cnt = 0;
    public int mCelluarRxTup1 = -1;
    public int mCelluarRxTup2 = -1;
    public int mCelluarTxTup = -1;
    public AppTraffic[] mCellularTraffic;
    public int mCurrNetwork = -1;
    public int mHicureEnCnt = 0;
    public int mHicureSucCnt = 0;
    public long mLastBadQoeTime = 0;
    public long mLastCellTime = 0;
    public int mLastHandoverCause = -1;
    public long mLastMplinkTime = 0;
    public int mLastQoe = -1;
    public long mLastUploadTime = 0;
    public long mLastWifiTime = 0;
    public int mMplinkDisFailCnt = 0;
    public int mMplinkDisStallCnt = 0;
    public int mMplinkDisWifiGoodCnt = 0;
    public int mMplinkDur = 0;
    public int mMplinkEnCnt = 0;
    public int mMplinkEnFailChQoeCnt = 0;
    public int mMplinkEnFailCnt = 0;
    public int mMplinkEnFailCoexistCnt = 0;
    public int mMplinkEnFailEnvironCnt = 0;
    public int mMplinkEnFailHistoryQoeCnt = 0;
    public int mMplinkEnFailPingPongCnt = 0;
    public int mMplinkEnTraf = 0;
    public long mMplinkStartTraffic = 0;
    public int mNum = 0;
    public int mScenario = -1;
    public int mStallSwitch0Cnt = 0;
    public int mStallSwitch1Cnt = 0;
    public int mStallSwitchAbove1Cnt = 0;
    public int mStallSwitchCnt = 0;
    public long mStartCellularTraffic = 0;
    public int mStartInCellularCnt = 0;
    public int mStartInWiFiCnt = 0;
    public int mSwitch2CellCnt = 0;
    public int mSwitch2WifiCnt = 0;
    public int mTafficPtr = 0;
    public int mTrfficCell = 0;
    public int mUid = -1;
    public int mUserType = 0;
    public int mVipSwitchCnt = 0;
    public int mWiFiLv1Cnt = 0;
    public int mWiFiLv2Cnt = 0;
    public int mWiFiLv3Cnt = 0;
    public int mWifiRxTup1 = -1;
    public int mWifiRxTup2 = -1;
    public AppTraffic[] mWifiTraffic;
    public int mWifiTxTup = -1;

    public static class AppTraffic {
        public int rx = -1;
        public int tx = -1;
    }

    public HwHistreamCHRStatisticsInfo(String apkName, int scenario, int uid) {
        this.mApkName = apkName;
        this.mScenario = scenario;
        this.mUid = uid;
        this.mNum = 1;
        this.mWifiTraffic = new AppTraffic[]{new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic()};
        this.mCellularTraffic = new AppTraffic[]{new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic(), new AppTraffic()};
    }

    public HwHistreamCHRStatisticsInfo(int scenario) {
        this.mScenario = scenario;
    }

    public void updateCHRStatisticsInfo(HwHistreamCHRStatisticsInfo newInfo) {
        if (newInfo != null) {
            this.mNum += newInfo.mNum;
            this.mStartInWiFiCnt += newInfo.mStartInWiFiCnt;
            this.mStartInCellularCnt += newInfo.mStartInCellularCnt;
            this.mCallInCellularDur += newInfo.mCallInCellularDur;
            this.mCallInWiFiDur += newInfo.mCallInWiFiDur;
            this.mCellLv1Cnt += newInfo.mCellLv1Cnt;
            this.mCellLv2Cnt += newInfo.mCellLv2Cnt;
            this.mCellLv3Cnt = newInfo.mCellLv3Cnt;
            this.mWiFiLv1Cnt += newInfo.mWiFiLv1Cnt;
            this.mWiFiLv2Cnt += newInfo.mWiFiLv2Cnt;
            this.mWiFiLv3Cnt = newInfo.mWiFiLv3Cnt;
            this.mTrfficCell += newInfo.mTrfficCell;
            this.mVipSwitchCnt += newInfo.mVipSwitchCnt;
            this.mStallSwitchCnt += newInfo.mStallSwitchCnt;
            this.mStallSwitch0Cnt += newInfo.mStallSwitch0Cnt;
            this.mStallSwitch1Cnt += newInfo.mStallSwitch1Cnt;
            this.mStallSwitchAbove1Cnt += newInfo.mStallSwitchAbove1Cnt;
            this.mSwitch2CellCnt += newInfo.mSwitch2CellCnt;
            this.mSwitch2WifiCnt += newInfo.mSwitch2WifiCnt;
            this.mMplinkDur += newInfo.mMplinkDur;
            this.mMplinkEnCnt += newInfo.mMplinkEnCnt;
            this.mMplinkDisStallCnt += newInfo.mMplinkDisStallCnt;
            this.mMplinkDisWifiGoodCnt += newInfo.mMplinkDisWifiGoodCnt;
            this.mMplinkEnFailCnt += newInfo.mMplinkEnFailCnt;
            this.mMplinkDisFailCnt += newInfo.mMplinkDisFailCnt;
            this.mMplinkEnTraf += newInfo.mMplinkEnTraf;
            this.mMplinkEnFailEnvironCnt += newInfo.mMplinkEnFailEnvironCnt;
            this.mMplinkEnFailCoexistCnt += newInfo.mMplinkEnFailCoexistCnt;
            this.mMplinkEnFailPingPongCnt += newInfo.mMplinkEnFailPingPongCnt;
            this.mMplinkEnFailHistoryQoeCnt += newInfo.mMplinkEnFailHistoryQoeCnt;
            this.mMplinkEnFailChQoeCnt += newInfo.mMplinkEnFailChQoeCnt;
            this.mHicureEnCnt += newInfo.mHicureEnCnt;
            this.mHicureSucCnt += newInfo.mHicureSucCnt;
        }
    }

    public void printCHRStatisticsInfo() {
        HwHiStreamUtils.logD("printCHRStatisticsInfo mNum = " + this.mNum + " mScenario = " + this.mScenario + " mStartInWiFiCnt = " + this.mStartInWiFiCnt + " mStartInCellularCnt = " + this.mStartInCellularCnt + " mCallInCellularDur = " + this.mCallInCellularDur + " mCallInWiFiDur = " + this.mCallInWiFiDur + " mCellLv1Cnt = " + this.mCellLv1Cnt + " mCellLv2Cnt = " + this.mCellLv2Cnt + " mCellLv3Cnt = " + this.mCellLv3Cnt + " mWiFiLv1Cnt = " + this.mWiFiLv1Cnt + " mWiFiLv2Cnt = " + this.mWiFiLv2Cnt + " mWiFiLv3Cnt = " + this.mWiFiLv3Cnt + " mTrfficCell = " + this.mTrfficCell + " mVipSwitchCnt = " + this.mVipSwitchCnt + " mStallSwitchCnt = " + this.mStallSwitchCnt + " mStallSwitch0Cnt = " + this.mStallSwitch0Cnt + " mStallSwitch1Cnt = " + this.mStallSwitch1Cnt + " mStallSwitchAbove1Cnt = " + this.mStallSwitchAbove1Cnt + " mSwitch2CellCnt = " + this.mSwitch2CellCnt + " mSwitch2WifiCnt = " + this.mSwitch2WifiCnt + " mMplinkDur = " + this.mMplinkDur + " mMplinkEnCnt = " + this.mMplinkEnCnt + " mMplinkDisStallCnt = " + this.mMplinkDisStallCnt + " mMplinkDisWifiGoodCnt = " + this.mMplinkDisWifiGoodCnt + " mMplinkEnFailCnt = " + this.mMplinkEnFailCnt + " mMplinkDisFailCnt = " + this.mMplinkDisFailCnt + " mMplinkEnTraf = " + this.mMplinkEnTraf + " mMplinkEnFailEnvironCnt = " + this.mMplinkEnFailEnvironCnt + " mMplinkEnFailCoexistCnt = " + this.mMplinkEnFailCoexistCnt + " mMplinkEnFailPingPongCnt = " + this.mMplinkEnFailPingPongCnt + " mMplinkEnFailHistoryQoeCnt = " + this.mMplinkEnFailHistoryQoeCnt + " mMplinkEnFailChQoeCnt = " + this.mMplinkEnFailChQoeCnt + " mHicureEnCnt = " + this.mHicureEnCnt + " mHicureSucCnt = " + this.mHicureSucCnt);
    }
}
