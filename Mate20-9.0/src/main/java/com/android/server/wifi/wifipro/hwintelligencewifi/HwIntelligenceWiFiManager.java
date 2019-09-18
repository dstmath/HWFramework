package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiProUIDisplayManager;
import java.util.List;

public class HwIntelligenceWiFiManager {
    private static HwIntelligenceWiFiManager mHwIntelligenceWiFiManager = null;
    private static List<ScanResult> mScanList = null;
    private HwIntelligenceStateMachine mStateMachine;

    private HwIntelligenceWiFiManager(Context context, WifiProUIDisplayManager wifiProUIDisplayManager) {
        this.mStateMachine = HwIntelligenceStateMachine.createIntelligenceStateMachine(context, wifiProUIDisplayManager);
    }

    public static HwIntelligenceWiFiManager createInstance(Context context, WifiProUIDisplayManager wifiProUIDisplayManager) {
        if (mHwIntelligenceWiFiManager == null) {
            mHwIntelligenceWiFiManager = new HwIntelligenceWiFiManager(context, wifiProUIDisplayManager);
        }
        return mHwIntelligenceWiFiManager;
    }

    public void start() {
        this.mStateMachine.onStart();
    }

    public void stop() {
        this.mStateMachine.onStop();
    }

    public static synchronized void setWiFiProScanResultList(List<ScanResult> list) {
        synchronized (HwIntelligenceWiFiManager.class) {
            Log.e(MessageUtil.TAG, "setWiFiProScanResultList");
            mScanList = list;
        }
    }

    public static synchronized List<ScanResult> getWiFiProScanResultList() {
        List<ScanResult> list;
        synchronized (HwIntelligenceWiFiManager.class) {
            list = mScanList;
        }
        return list;
    }
}
