package com.huawei.systemmanager.netassistant;

import android.net.ConnectivityManager;

public class HwConnectivityManager {
    public static String getTetherStateChangedAction() {
        return "android.net.conn.TETHER_STATE_CHANGED";
    }

    public static String getExtraActiveTether() {
        return "tetherArray";
    }

    public static String getExtraErroredTether() {
        return "erroredArray";
    }

    public static boolean isTetheredWifiRegexsMatches(Object[] tethered, ConnectivityManager cm) {
        if (cm == null || tethered == null) {
            return false;
        }
        String[] mWifiRegexs = cm.getTetherableWifiRegexs();
        for (Object o : tethered) {
            String s = (String) o;
            for (String regex : mWifiRegexs) {
                if (s.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }
}
