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
        this.mRSSI_VALUE = (short) 0;
        this.mOTA_PacketDropRate = (short) 0;
        this.mRttAvg = (short) 0;
        this.mTcpInSegs = (short) 0;
        this.mTcpOutSegs = (short) 0;
        this.mTcpRetransSegs = (short) 0;
        this.mWIFI_NetSpeed = (short) 0;
        this.mIPQLevel = (short) 0;
        this.mRO_APSsid = "";
        this.mMobileSignalLevel = (short) 0;
        this.mRATType = (short) 0;
        this.mHistoryQuilityRO_Rate = (short) 0;
        this.mHighDataRateRO_Rate = (short) 0;
        this.mCreditScoreRO_Rate = (short) 0;
        this.mRO_Duration = (short) 0;
    }
}
