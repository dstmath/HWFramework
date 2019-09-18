package com.android.server.wifi.wifipro;

public class WifiProRoveOutParaRecord {
    private static final String TAG = "WifiProRoveOutParaRecord";
    public short mCreditScoreRO_Rate;
    public short mHighDataRateRO_Rate;
    public short mHistoryQuilityRO_Rate;
    public short mIPQLevel;
    public short mMobileSignalLevel;
    public short mOTA_PacketDropRate;
    public short mRATType;
    public String mRO_APSsid;
    public short mRO_Duration;
    public short mRSSI_VALUE;
    public short mRttAvg;
    public short mTcpInSegs;
    public short mTcpOutSegs;
    public short mTcpRetransSegs;
    public short mWIFI_NetSpeed;

    public WifiProRoveOutParaRecord() {
        resetAllParameters();
    }

    public void resetAllParameters() {
        this.mRSSI_VALUE = 0;
        this.mOTA_PacketDropRate = 0;
        this.mRttAvg = 0;
        this.mTcpInSegs = 0;
        this.mTcpOutSegs = 0;
        this.mTcpRetransSegs = 0;
        this.mWIFI_NetSpeed = 0;
        this.mIPQLevel = 0;
        this.mRO_APSsid = "";
        this.mMobileSignalLevel = 0;
        this.mRATType = 0;
        this.mHistoryQuilityRO_Rate = 0;
        this.mHighDataRateRO_Rate = 0;
        this.mCreditScoreRO_Rate = 0;
        this.mRO_Duration = 0;
    }
}
