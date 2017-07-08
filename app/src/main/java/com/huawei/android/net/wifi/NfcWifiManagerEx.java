package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiManager;

public class NfcWifiManagerEx {
    public static String getWpaSuppConfig(WifiManager mWifiManager) {
        return HwFrameworkFactory.getHwInnerWifiManager().getWpaSuppConfig();
    }
}
