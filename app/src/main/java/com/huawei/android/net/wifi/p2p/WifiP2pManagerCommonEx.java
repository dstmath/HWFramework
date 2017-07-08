package com.huawei.android.net.wifi.p2p;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;

public class WifiP2pManagerCommonEx {
    public static final String EXTRA_WIFI_P2P_CONNECT_STATE = "extraState";
    public static final String WIFI_P2P_CONNECT_STATE_CHANGED_ACTION = "android.net.wifi.p2p.CONNECT_STATE_CHANGE";
    public static final int WIFI_P2P_STATE_CONNECTED = 2;
    public static final int WIFI_P2P_STATE_CONNECTIING = 1;
    public static final int WIFI_P2P_STATE_CONNECTION_FAIL = 3;
    public static final String WIFI_P2P_VALID_DEVICE = "avlidDevice";

    public static void createGroupWifiRepeater(Channel c, WifiConfiguration wifiConfig, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().createGroupWifiRepeater(c, wifiConfig, listener);
    }

    public static WifiConfiguration getWifiRepeaterConfiguration() {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().getWifiRepeaterConfiguration();
    }

    public static boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().setWifiRepeaterConfiguration(wifiConfig);
    }

    public static void magiclinkConnect(Channel c, String config, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkConnect(c, config, listener);
    }

    public static void magiclinkCreateGroup(Channel c, String frequency, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkCreateGroup(c, frequency, listener);
    }

    public static void magiclinkRemoveGcGroup(Channel c, String iface, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkRemoveGcGroup(c, iface, listener);
    }

    public static boolean releaseIPAddr(String ifName) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().releaseIPAddr(ifName);
    }

    public static boolean configIPAddr(String ifName, String ipAddr, String server) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().configIPAddr(ifName, ipAddr, server);
    }
}
