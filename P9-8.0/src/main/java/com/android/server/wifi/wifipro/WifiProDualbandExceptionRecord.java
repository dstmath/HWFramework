package com.android.server.wifi.wifipro;

import android.util.Log;

public class WifiProDualbandExceptionRecord {
    private static final String TAG = "WifiProStatisticsRecord";
    public static final String WIFIPRO_DEAULT_STR = "DEAULT_STR";
    public static final short WIFIPRO_STATE_DISABLE = (short) 2;
    public static final short WIFIPRO_STATE_ENABLE = (short) 1;
    public static final short WIFIPRO_STATE_UNKNOW = (short) 0;
    public short mConnectTime_2G;
    public short mConnectTime_5G;
    public short mHandOverErrCode;
    public short mIsBluetoothConnected;
    public short mLossRate_2G;
    public short mLossRate_5G;
    public short mRSSI_2G;
    public short mRSSI_5G;
    public short mRTT_2G;
    public short mRTT_5G;
    public String mSSID_2G;
    public String mSSID_5G;
    public short mScan_Threshod_RSSI_2G;
    public short mScore_2G;
    public short mScore_5G;
    public short mSingleOrMixed;
    public short mTarget_RSSI_5G;

    public WifiProDualbandExceptionRecord() {
        resetRecord();
    }

    private void resetRecord() {
        this.mSSID_2G = "DEAULT_STR";
        this.mSSID_5G = "DEAULT_STR";
        this.mSingleOrMixed = (short) 0;
        this.mScan_Threshod_RSSI_2G = (short) 0;
        this.mTarget_RSSI_5G = (short) 0;
        this.mRSSI_2G = (short) 0;
        this.mRSSI_5G = (short) 0;
        this.mScore_2G = (short) 0;
        this.mScore_5G = (short) 0;
        this.mHandOverErrCode = (short) 0;
        this.mIsBluetoothConnected = (short) 0;
        this.mRTT_2G = (short) 0;
        this.mLossRate_2G = (short) 0;
        this.mConnectTime_2G = (short) 0;
        this.mRTT_5G = (short) 0;
        this.mLossRate_5G = (short) 0;
        this.mConnectTime_5G = (short) 0;
    }

    public void dumpAllParameter() {
        Log.i(TAG, "dumpChrStatRecord:, mSSID_2G= " + this.mSSID_2G + ", mSSID_5G= " + this.mSSID_5G + ", mSingleOrMixed= " + this.mSingleOrMixed + ", mScan_Threshod_RSSI_2G= " + this.mScan_Threshod_RSSI_2G + ", mTarget_RSSI_5G= " + this.mTarget_RSSI_5G + ", mRSSI_2G= " + this.mRSSI_2G + ", mRSSI_5G= " + this.mRSSI_5G + ", mScore_2G= " + this.mScore_2G + ", mScore_5G= " + this.mScore_5G + ", mHandOverErrCode= " + this.mHandOverErrCode + ", mIsBluetoothConnected= " + this.mIsBluetoothConnected + ", mRTT_2G= " + this.mRTT_2G + ", mLossRate_2G= " + this.mLossRate_2G + ", mConnectTime_2G= " + this.mConnectTime_2G + ", mRTT_5G= " + this.mRTT_5G + ", mLossRate_5G= " + this.mLossRate_5G + ", mConnectTime_5G= " + this.mConnectTime_5G);
    }
}
