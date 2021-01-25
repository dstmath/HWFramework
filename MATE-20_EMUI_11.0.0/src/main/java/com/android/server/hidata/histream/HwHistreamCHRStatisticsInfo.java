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
        HwHiStreamUtils.logD(false, "printCHRStatisticsInfo mNum = %{public}d mScenario = %{public}d mStartInWiFiCnt = %{public}d mStartInCellularCnt = %{public}d mCallInCellularDur = %{public}d mCallInWiFiDur = %{public}d mCellLv1Cnt = %{public}d mCellLv2Cnt = %{public}d mCellLv3Cnt = %{public}d mWiFiLv1Cnt = %{public}d mWiFiLv2Cnt = %{public}d mWiFiLv3Cnt = %{public}d mTrfficCell = %{public}d mVipSwitchCnt = %{public}d mStallSwitchCnt = %{public}d mStallSwitch0Cnt = %{public}d mStallSwitch1Cnt = %{public}d mStallSwitchAbove1Cnt = %{public}d mSwitch2CellCnt = %{public}d mSwitch2WifiCnt = %{public}d mMplinkDur = %{public}d mMplinkEnCnt = %{public}d mMplinkDisStallCnt = %{public}d mMplinkDisWifiGoodCnt = %{public}d mMplinkEnFailCnt = %{public}d mMplinkDisFailCnt = %{public}d mMplinkEnTraf = %{public}d mMplinkEnFailEnvironCnt = %{public}d mMplinkEnFailCoexistCnt = %{public}d mMplinkEnFailPingPongCnt = %{public}d mMplinkEnFailHistoryQoeCnt = %{public}d mMplinkEnFailChQoeCnt = %{public}d mHicureEnCnt = %{public}d mHicureSucCnt = %{public}d", Integer.valueOf(this.mNum), Integer.valueOf(this.mScenario), Integer.valueOf(this.mStartInWiFiCnt), Integer.valueOf(this.mStartInCellularCnt), Integer.valueOf(this.mCallInCellularDur), Integer.valueOf(this.mCallInWiFiDur), Integer.valueOf(this.mCellLv1Cnt), Integer.valueOf(this.mCellLv2Cnt), Integer.valueOf(this.mCellLv3Cnt), Integer.valueOf(this.mWiFiLv1Cnt), Integer.valueOf(this.mWiFiLv2Cnt), Integer.valueOf(this.mWiFiLv3Cnt), Integer.valueOf(this.mTrfficCell), Integer.valueOf(this.mVipSwitchCnt), Integer.valueOf(this.mStallSwitchCnt), Integer.valueOf(this.mStallSwitch0Cnt), Integer.valueOf(this.mStallSwitch1Cnt), Integer.valueOf(this.mStallSwitchAbove1Cnt), Integer.valueOf(this.mSwitch2CellCnt), Integer.valueOf(this.mSwitch2WifiCnt), Integer.valueOf(this.mMplinkDur), Integer.valueOf(this.mMplinkEnCnt), Integer.valueOf(this.mMplinkDisStallCnt), Integer.valueOf(this.mMplinkDisWifiGoodCnt), Integer.valueOf(this.mMplinkEnFailCnt), Integer.valueOf(this.mMplinkDisFailCnt), Integer.valueOf(this.mMplinkEnTraf), Integer.valueOf(this.mMplinkEnFailEnvironCnt), Integer.valueOf(this.mMplinkEnFailCoexistCnt), Integer.valueOf(this.mMplinkEnFailPingPongCnt), Integer.valueOf(this.mMplinkEnFailHistoryQoeCnt), Integer.valueOf(this.mMplinkEnFailChQoeCnt), Integer.valueOf(this.mHicureEnCnt), Integer.valueOf(this.mHicureSucCnt));
    }
}
