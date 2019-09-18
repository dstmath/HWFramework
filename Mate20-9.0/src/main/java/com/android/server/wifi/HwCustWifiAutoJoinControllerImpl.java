package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

public class HwCustWifiAutoJoinControllerImpl extends HwCustWifiAutoJoinController {
    private static final boolean HWDBG = ((Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) ? true : HWDBG);
    public static final int MAX_ORDER = 100;
    public static final int MIN_ORDER = -100;
    static final String TAG = "HwCustWifiAutoJoinCtl";

    public boolean isWifiAutoJoinPriority(Context mContext) {
        return "true".equals(Settings.Global.getString(mContext.getContentResolver(), "wifi_autojoin_prio"));
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
        if (config.allowedKeyManagement.get(0)) {
            return 1;
        }
        return 0;
    }

    public boolean isDeleteReenableAutoJoin() {
        return SystemProperties.getBoolean("ro.config.delete_re-autojoin", HWDBG);
    }
}
