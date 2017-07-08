package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.os.Looper;
import android.util.Log;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import java.util.List;

public class HwWifiConnectivityManager extends WifiConnectivityManager {
    private static final int HW_MAX_PERIODIC_SCAN_INTERVAL_MS = 60000;
    private static final int HW_MID_PERIODIC_SCAN_INTERVAL_MS = 30000;
    private static final int HW_MIX_PERIODIC_SCAN_INTERVAL_MS = 10000;
    private static final int SCAN_COUNT_CHANGE_REASON_ADD = 0;
    private static final int SCAN_COUNT_CHANGE_REASON_MINUS = 1;
    private static final int SCAN_COUNT_CHANGE_REASON_RESET = 2;
    private static final String TAG = "HwWifiConnectivityManager";
    private Context mContext;
    private int mHwSingleScanCounter;
    private int mPeriodicSingleScanInterval;
    private WifiScanGenieController mWifiScanGenieController;

    public HwWifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiQualifiedNetworkSelector qualifiedNetworkSelector, WifiInjector wifiInjector, Looper looper) {
        super(context, stateMachine, scanner, configManager, wifiInfo, qualifiedNetworkSelector, wifiInjector, looper);
        this.mContext = context;
        this.mWifiScanGenieController = WifiScanGenieController.createWifiScanGenieControllerImpl(this.mContext);
        log("HwWifiConnectivityManager init!");
    }

    protected int getPeriodicSingleScanInterval() {
        if (this.mHwSingleScanCounter < 4) {
            this.mPeriodicSingleScanInterval = HW_MIX_PERIODIC_SCAN_INTERVAL_MS;
        } else if (this.mHwSingleScanCounter < 7) {
            this.mPeriodicSingleScanInterval = HW_MID_PERIODIC_SCAN_INTERVAL_MS;
        } else {
            this.mPeriodicSingleScanInterval = HW_MAX_PERIODIC_SCAN_INTERVAL_MS;
        }
        log("HwSingleScanCounter: " + this.mHwSingleScanCounter + ", mPeriodicSingleScanInterval : " + this.mPeriodicSingleScanInterval + " ms");
        return this.mPeriodicSingleScanInterval;
    }

    protected void handleSingleScanFailure(int reason) {
        log("handleSingleScanFailure reason " + reason);
        handleScanCountChanged(SCAN_COUNT_CHANGE_REASON_MINUS);
    }

    protected void handleSingleScanSuccess() {
    }

    protected void handleScanCountChanged(int reason) {
        if (reason == 0) {
            this.mHwSingleScanCounter += SCAN_COUNT_CHANGE_REASON_MINUS;
        } else if (SCAN_COUNT_CHANGE_REASON_MINUS == reason) {
            if (this.mHwSingleScanCounter > 0) {
                this.mHwSingleScanCounter--;
            }
        } else if (SCAN_COUNT_CHANGE_REASON_RESET == reason) {
            this.mHwSingleScanCounter = SCAN_COUNT_CHANGE_REASON_ADD;
        }
        log("handleScanCounterChanged,reason: " + reason + ", mHwSingleScanCounter: " + this.mHwSingleScanCounter);
    }

    protected boolean isSupportWifiScanGenie() {
        return true;
    }

    protected boolean isWifiScanSpecialChannels() {
        return isSupportWifiScanGenie() && this.mHwSingleScanCounter <= SCAN_COUNT_CHANGE_REASON_MINUS;
    }

    protected ScanSettings getScanGenieSettings() {
        return getHwScanSettings();
    }

    protected boolean handleForceScan() {
        return false;
    }

    private ScanSettings getHwScanSettings() {
        List<Integer> fusefrequencyList = this.mWifiScanGenieController.getScanfrequencys();
        if (fusefrequencyList == null || fusefrequencyList.size() == 0) {
            log("getHwScanSettings,fusefrequencyList is null:");
            return null;
        }
        ScanSettings settings = new ScanSettings();
        settings.band = SCAN_COUNT_CHANGE_REASON_ADD;
        settings.reportEvents = 3;
        settings.numBssidsPerScan = SCAN_COUNT_CHANGE_REASON_ADD;
        ChannelSpec[] channels = new ChannelSpec[fusefrequencyList.size()];
        for (int i = SCAN_COUNT_CHANGE_REASON_ADD; i < fusefrequencyList.size(); i += SCAN_COUNT_CHANGE_REASON_MINUS) {
            channels[i] = new ChannelSpec(((Integer) fusefrequencyList.get(i)).intValue());
        }
        settings.channels = channels;
        return settings;
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
