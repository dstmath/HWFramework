package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.content.Context;

public class WifiManagerCommonEx {
    public static int calculateSignalLevelHW(int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(rssi);
    }

    public static int calculateSignalLevelHW(int freq, int rssi) {
        return HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(freq, rssi);
    }

    public static boolean getHwMeteredHint(Context context) {
        return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(context);
    }

    public static int[] getChannelListFor5G() {
        return HwFrameworkFactory.getHwInnerWifiManager().getChannelListFor5G();
    }

    public static boolean isRSDBSupported() {
        return HwFrameworkFactory.getHwInnerWifiManager().isRSDBSupported();
    }
}
