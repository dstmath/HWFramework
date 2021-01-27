package com.huawei.hwwifiproservice;

public class WifiProRoveOutParaRecord {
    private static final String TAG = "WifiProRoveOutParaRecord";
    public short mCreditScoreRoRate;
    public short mHighDataRateRoRate;
    public short mHistoryQuilityRoRate;
    public short mIpqLevel;
    public short mMobileSignalLevel;
    public short mOtaPacketDropRate;
    public short mRatType;
    public String mRoApSsid;
    public short mRoDuration;
    public short mRssiValue;
    public short mRttAvg;
    public short mTcpInSegs;
    public short mTcpOutSegs;
    public short mTcpRetransSegs;
    public short mWifiNetSpeed;

    public WifiProRoveOutParaRecord() {
        resetAllParameters();
    }

    public final void resetAllParameters() {
        this.mRssiValue = 0;
        this.mOtaPacketDropRate = 0;
        this.mRttAvg = 0;
        this.mTcpInSegs = 0;
        this.mTcpOutSegs = 0;
        this.mTcpRetransSegs = 0;
        this.mWifiNetSpeed = 0;
        this.mIpqLevel = 0;
        this.mRoApSsid = "";
        this.mMobileSignalLevel = 0;
        this.mRatType = 0;
        this.mHistoryQuilityRoRate = 0;
        this.mHighDataRateRoRate = 0;
        this.mCreditScoreRoRate = 0;
        this.mRoDuration = 0;
    }
}
