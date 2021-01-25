package com.huawei.hwwifiproservice;

import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;

public class WifiProDualbandExceptionRecord {
    private static final String TAG = "WifiProStatisticsRecord";
    public static final String WIFIPRO_DEAULT_STR = "DEAULT_STR";
    public static final short WIFIPRO_STATE_DISABLE = 2;
    public static final short WIFIPRO_STATE_ENABLE = 1;
    public static final short WIFIPRO_STATE_UNKNOW = 0;
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
        this.mSingleOrMixed = 0;
        this.mScan_Threshod_RSSI_2G = 0;
        this.mTarget_RSSI_5G = 0;
        this.mRSSI_2G = 0;
        this.mRSSI_5G = 0;
        this.mScore_2G = 0;
        this.mScore_5G = 0;
        this.mHandOverErrCode = 0;
        this.mIsBluetoothConnected = 0;
        this.mRTT_2G = 0;
        this.mLossRate_2G = 0;
        this.mConnectTime_2G = 0;
        this.mRTT_5G = 0;
        this.mLossRate_5G = 0;
        this.mConnectTime_5G = 0;
    }

    public void dumpAllParameter() {
        Log.i(TAG, "dumpChrStatRecord:, mSSID_2G= " + StringUtilEx.safeDisplaySsid(this.mSSID_2G) + ", mSSID_5G= " + StringUtilEx.safeDisplaySsid(this.mSSID_5G) + ", mSingleOrMixed= " + ((int) this.mSingleOrMixed) + ", mScan_Threshod_RSSI_2G= " + ((int) this.mScan_Threshod_RSSI_2G) + ", mTarget_RSSI_5G= " + ((int) this.mTarget_RSSI_5G) + ", mRSSI_2G= " + ((int) this.mRSSI_2G) + ", mRSSI_5G= " + ((int) this.mRSSI_5G) + ", mScore_2G= " + ((int) this.mScore_2G) + ", mScore_5G= " + ((int) this.mScore_5G) + ", mHandOverErrCode= " + ((int) this.mHandOverErrCode) + ", mIsBluetoothConnected= " + ((int) this.mIsBluetoothConnected) + ", mRTT_2G= " + ((int) this.mRTT_2G) + ", mLossRate_2G= " + ((int) this.mLossRate_2G) + ", mConnectTime_2G= " + ((int) this.mConnectTime_2G) + ", mRTT_5G= " + ((int) this.mRTT_5G) + ", mLossRate_5G= " + ((int) this.mLossRate_5G) + ", mConnectTime_5G= " + ((int) this.mConnectTime_5G));
    }
}
