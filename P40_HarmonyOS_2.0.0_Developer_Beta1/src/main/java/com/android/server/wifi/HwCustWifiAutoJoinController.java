package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

public class HwCustWifiAutoJoinController {
    public boolean isWifiAutoJoinPriority(Context context) {
        return false;
    }

    public int compareWifiConfigurationsKeyMgt(WifiConfiguration wcf1, WifiConfiguration wcf2) {
        return -1;
    }

    public WifiConfiguration attemptAutoJoinCust(WifiConfiguration candidate, WifiConfiguration config) {
        return null;
    }

    public boolean isDeleteReenableAutoJoin() {
        return false;
    }
}
