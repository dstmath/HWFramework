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
        for (Object obj : tethered) {
            if (!(obj instanceof String)) {
                return false;
            }
            String str = (String) obj;
            for (String regex : mWifiRegexs) {
                if (str.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }
}
