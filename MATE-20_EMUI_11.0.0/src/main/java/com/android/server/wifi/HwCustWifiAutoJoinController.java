package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

public class HwCustWifiAutoJoinController {
    public boolean isWifiAutoJoinPriority(Context mContext) {
        return false;
    }

    public int compareWifiConfigurationsKeyMgt(WifiConfiguration a, WifiConfiguration b) {
        return -1;
    }

    public WifiConfiguration attemptAutoJoinCust(WifiConfiguration candidate, WifiConfiguration config) {
        return null;
    }

    public boolean isDeleteReenableAutoJoin() {
        return false;
    }
}
