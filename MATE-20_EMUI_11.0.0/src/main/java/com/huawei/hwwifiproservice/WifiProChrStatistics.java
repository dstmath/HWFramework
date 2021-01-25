package com.huawei.hwwifiproservice;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class WifiProChrStatistics {
    private static final String TAG = "WifiProChrStatistics";
    private static final int WIFI_SWITCH_TYPES = 3;
    private static WifiProChrStatistics chrStat;
    public int mApEvaluateTrigCnt;
    public int mAutoOpenFailCnt;
    public int mAutoOpenSuccCnt;
    public int mEvaluateConnCnt;
    public int mInternetAPCnt;
    public int mNoInternetAPCnt;
    public int mPortalAPCnt;
    public int mScanGenieCnt;
    public int mScanGenieFailCnt;
    public int mScanGenieSuccCnt;
    public List<Integer> mWifiSwitchCnt = new ArrayList();
    public List<Integer> mWifiSwitchSuccCnt = new ArrayList();
    public int mWifiproOpenCloseCnt;

    private WifiProChrStatistics() {
        resetStatistics();
    }

    public static WifiProChrStatistics getInstance() {
        if (chrStat == null) {
            chrStat = new WifiProChrStatistics();
        }
        return chrStat;
    }

    public Bundle getStatBundle() {
        Bundle openCloseEvent = new Bundle();
        openCloseEvent.putInt("WifiproOpenCloseCnt", this.mWifiproOpenCloseCnt);
        Bundle scanGenieEvent = new Bundle();
        scanGenieEvent.putInt("ScanGenieCnt", this.mScanGenieCnt);
        scanGenieEvent.putInt("ScanGenieSuccCnt", this.mScanGenieSuccCnt);
        scanGenieEvent.putInt("ScanGenieFailCnt", this.mScanGenieFailCnt);
        Bundle apEvaluateEvent = new Bundle();
        apEvaluateEvent.putInt("ApEvaluateTrigCnt", this.mApEvaluateTrigCnt);
        apEvaluateEvent.putInt("InternetAPCnt", this.mInternetAPCnt);
        apEvaluateEvent.putInt("NoInternetAPCnt", this.mNoInternetAPCnt);
        apEvaluateEvent.putInt("PortalAPCnt", this.mPortalAPCnt);
        apEvaluateEvent.putInt("EvaluateConnCnt", this.mEvaluateConnCnt);
        Bundle autoOpenEvent = new Bundle();
        autoOpenEvent.putInt("AutoOpenSuccCnt", this.mAutoOpenSuccCnt);
        autoOpenEvent.putInt("AutoOpenFailCnt", this.mAutoOpenFailCnt);
        Bundle wifiSwitchCntEvent = new Bundle();
        wifiSwitchCntEvent.putInt("SsidSwitch", this.mWifiSwitchCnt.get(0).intValue());
        wifiSwitchCntEvent.putInt("BssidSwitch", this.mWifiSwitchCnt.get(1).intValue());
        wifiSwitchCntEvent.putInt("DualBandSwitch", this.mWifiSwitchCnt.get(2).intValue());
        Bundle wifiSwitchSuccCntEvent = new Bundle();
        wifiSwitchSuccCntEvent.putInt("SsidSwitch", this.mWifiSwitchSuccCnt.get(0).intValue());
        wifiSwitchSuccCntEvent.putInt("BssidSwitch", this.mWifiSwitchSuccCnt.get(1).intValue());
        wifiSwitchSuccCntEvent.putInt("DualBandSwitch", this.mWifiSwitchSuccCnt.get(2).intValue());
        Bundle allEvent = new Bundle();
        allEvent.putBundle("OpenClose", openCloseEvent);
        allEvent.putBundle("ScanGenie", scanGenieEvent);
        allEvent.putBundle("ApEvaluate", apEvaluateEvent);
        allEvent.putBundle("AutoOpen", autoOpenEvent);
        allEvent.putBundle("SwitchCnt", wifiSwitchCntEvent);
        allEvent.putBundle("SwitchSuccCnt", wifiSwitchSuccCntEvent);
        return allEvent;
    }

    public final void resetStatistics() {
        this.mWifiproOpenCloseCnt = 0;
        this.mScanGenieCnt = 0;
        this.mScanGenieSuccCnt = 0;
        this.mScanGenieFailCnt = 0;
        this.mApEvaluateTrigCnt = 0;
        this.mInternetAPCnt = 0;
        this.mNoInternetAPCnt = 0;
        this.mPortalAPCnt = 0;
        this.mEvaluateConnCnt = 0;
        this.mAutoOpenSuccCnt = 0;
        this.mAutoOpenFailCnt = 0;
        this.mWifiSwitchCnt.clear();
        this.mWifiSwitchSuccCnt.clear();
        for (int i = 0; i < 3; i++) {
            this.mWifiSwitchCnt.add(0);
        }
        for (int i2 = 0; i2 < 3; i2++) {
            this.mWifiSwitchSuccCnt.add(0);
        }
    }
}
