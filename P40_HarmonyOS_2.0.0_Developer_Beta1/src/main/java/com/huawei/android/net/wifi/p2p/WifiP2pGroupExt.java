package com.huawei.android.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pGroup;

public class WifiP2pGroupExt {
    public static int getFrequency(WifiP2pGroup wifiP2pGroup) {
        if (wifiP2pGroup != null) {
            return wifiP2pGroup.getFrequency();
        }
        return 0;
    }
}
