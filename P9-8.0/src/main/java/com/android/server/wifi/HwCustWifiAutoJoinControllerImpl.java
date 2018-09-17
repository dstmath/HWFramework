package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.util.Log;

public class HwCustWifiAutoJoinControllerImpl extends HwCustWifiAutoJoinController {
    private static final boolean HWDBG;
    public static final int MAX_ORDER = 100;
    public static final int MIN_ORDER = -100;
    static final String TAG = "HwCustWifiAutoJoinCtl";

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : HWDBG : true;
        HWDBG = isLoggable;
    }

    public boolean isWifiAutoJoinPriority() {
        return SystemProperties.getBoolean("ro.config.wifi_autojoin_prio", HWDBG);
    }

    public WifiConfiguration attemptAutoJoinCust(WifiConfiguration candidate, WifiConfiguration config) {
        int order = compareWifiConfigurationsKeyMgt(candidate, config);
        if (HWDBG) {
            Log.d(TAG, "attemptAutoJoinCust order=" + order);
        }
        if (order > 0) {
            return config;
        }
        return candidate;
    }

    public int compareWifiConfigurationsKeyMgt(WifiConfiguration a, WifiConfiguration b) {
        int scoreA = getWifiKeyMgmtScore(a);
        int scoreB = getWifiKeyMgmtScore(b);
        if (HWDBG) {
            Log.d(TAG, "compareWifiConfigurationsKeyMgt scoreA=" + scoreA + ", scoreB=" + scoreB);
        }
        if (scoreB - scoreA > 0) {
            return 100;
        }
        return -100;
    }

    private int getWifiKeyMgmtScore(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(4) || config.allowedKeyManagement.get(1)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(2)) {
            return 2;
        }
        return config.allowedKeyManagement.get(0) ? 1 : 0;
    }

    public boolean isDeleteReenableAutoJoin() {
        return SystemProperties.getBoolean("ro.config.delete_re-autojoin", HWDBG);
    }
}
