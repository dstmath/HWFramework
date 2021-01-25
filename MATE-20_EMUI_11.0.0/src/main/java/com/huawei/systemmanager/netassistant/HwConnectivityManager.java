package com.huawei.systemmanager.netassistant;

import android.net.ConnectivityManager;
import com.huawei.android.net.ConnectivityManagerEx;

public class HwConnectivityManager {
    public static String getTetherStateChangedAction() {
        return ConnectivityManagerEx.ACTION_TETHER_STATE_CHANGED;
    }

    public static String getExtraActiveTether() {
        return ConnectivityManagerEx.EXTRA_ACTIVE_TETHER;
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
