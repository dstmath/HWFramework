package com.huawei.android.net.wifi;

import android.common.HwFrameworkFactory;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;

public class Wifi2ManagerEx {
    public static final String LINK_CONFIGURATION_CHANGED_ACTION = "huawei.net.slave_wifi.LINK_CONFIGURATION_CHANGED";
    public static final String NETWORK_STATE_CHANGED_ACTION = "huawei.net.slave_wifi.STATE_CHANGED";
    public static final String RSSI_CHANGED_ACTION = "huawei.net.slave_wifi.RSSI_CHANGED";
    public static final int SLAVE_WIFI_SIGNAL_LEVEL_0 = 0;
    public static final int SLAVE_WIFI_SIGNAL_LEVEL_1 = 1;
    public static final int SLAVE_WIFI_SIGNAL_LEVEL_2 = 2;
    public static final int SLAVE_WIFI_SIGNAL_LEVEL_3 = 3;
    public static final int SLAVE_WIFI_SIGNAL_LEVEL_4 = 4;
    public static final String SUPPLICANT_CONNECTION_CHANGE_ACTION = "huawei.net.slave_wifi.supplicant.CONNECTION_CHANGE";
    public static final String SUPPLICANT_STATE_CHANGED_ACTION = "huawei.net.slave_wifi.supplicant.STATE_CHANGE";
    private static final String TAG = "Wifi2ManagerEx";
    public static final String WIFI_STATE_CHANGED_ACTION = "huawei.net.slave_wifi.WIFI_STATE_CHANGED";

    public static boolean isSupportDualWifi() {
        return HwFrameworkFactory.getHwInnerWifiManager().isSupportDualWifi();
    }

    public static void setSlaveWifiNetworkSelectionPara(int signalLevel, int callerUid, int needInternet) {
        HwFrameworkFactory.getHwInnerWifiManager().setSlaveWifiNetworkSelectionPara(signalLevel, callerUid, needInternet);
    }

    public static WifiInfo getSlaveWifiConnectionInfo() {
        return HwFrameworkFactory.getHwInnerWifiManager().getSlaveWifiConnectionInfo();
    }

    public static LinkProperties getLinkPropertiesForSlaveWifi() {
        return HwFrameworkFactory.getHwInnerWifiManager().getLinkPropertiesForSlaveWifi();
    }

    public static NetworkInfo getNetworkInfoForSlaveWifi() {
        return HwFrameworkFactory.getHwInnerWifiManager().getNetworkInfoForSlaveWifi();
    }
}
