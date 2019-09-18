package com.android.server.display.hisi_wifi;

import android.os.SystemProperties;

public class WifiDisplayControllerHisiExt {
    private static String mInterface = "wlan0";

    public static boolean hisiWifiEnabled() {
        if (SystemProperties.get("ro.connectivity.chiptype").equals("hi110x")) {
            return true;
        }
        return false;
    }

    public static String getHisiWifiInface() {
        return mInterface;
    }
}
