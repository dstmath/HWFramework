package com.huawei.systemmanager.netassistant;

import android.net.wifi.WifiConfiguration;
import android.util.Log;

public class HwWifiConfiguration {
    private static final String TAG = "HwWifiConfiguration";

    public static boolean setWifiSecConfig(WifiConfiguration config, int nSecResult) {
        if (config == null) {
            return false;
        }
        try {
            config.cloudSecurityCheck = nSecResult;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "setWifiSecConfig failed");
            return false;
        }
    }

    public static int getWifiSecConfig(WifiConfiguration config) {
        if (config == null) {
            return 0;
        }
        try {
            return config.cloudSecurityCheck;
        } catch (Exception e) {
            Log.e(TAG, "getWifiSecConfig failed");
            return 0;
        }
    }
}
