package com.huawei.android.net.wifi.p2p;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pManager;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;

public class WifiP2pManagerCommonEx {
    public static final String EXTRA_WIFI_P2P_CONNECT_STATE = "extraState";
    public static final String EXTRA_WIFI_RPT_STATE = "wifi_rpt_state";
    public static final String WIFI_P2P_CONNECT_STATE_CHANGED_ACTION = "android.net.wifi.p2p.CONNECT_STATE_CHANGE";
    public static final int WIFI_P2P_STATE_CONNECTED = 2;
    public static final int WIFI_P2P_STATE_CONNECTIING = 1;
    public static final int WIFI_P2P_STATE_CONNECTION_FAIL = 3;
    public static final String WIFI_P2P_VALID_DEVICE = "avlidDevice";
    public static final String WIFI_RPT_STATE_CHANGED_ACTION = "com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED";
    public static final int WIFI_RPT_STATE_CREATE_GO_UNTETHERD = 6;
    public static final int WIFI_RPT_STATE_DISABLED = 0;
    public static final int WIFI_RPT_STATE_DISABLING = 2;
    public static final int WIFI_RPT_STATE_ENABLED = 1;
    public static final int WIFI_RPT_STATE_ENABLING = 3;
    public static final int WIFI_RPT_STATE_START_FAIL = 5;
    public static final int WIFI_RPT_STATE_STOP_FAIL = 4;
    public static final int WIFI_RPT_STATE_UNKNOWN = -1;

    public static void createGroupWifiRepeater(WifiP2pManager.Channel c, WifiConfiguration wifiConfig, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().createGroupWifiRepeater(c, wifiConfig, listener);
    }

    public static WifiConfiguration getWifiRepeaterConfiguration() {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().getWifiRepeaterConfiguration();
    }

    public static String getGroupConfigInfo() {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().getGroupConfigInfo();
    }

    public static boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().setWifiRepeaterConfiguration(wifiConfig);
    }

    public static void magiclinkConnect(WifiP2pManager.Channel c, String config, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkConnect(c, config, listener);
    }

    public static void magiclinkCreateGroup(WifiP2pManager.Channel c, String frequency, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkCreateGroup(c, frequency, listener);
    }

    public static void magiclinkRemoveGcGroup(WifiP2pManager.Channel c, String iface, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().magiclinkRemoveGcGroup(c, iface, listener);
    }

    public static boolean releaseIPAddr(String ifName) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().releaseIPAddr(ifName);
    }

    public static boolean configIPAddr(String ifName, String ipAddr, String server) {
        return HwFrameworkFactory.getHwInnerWifiP2pManager().configIPAddr(ifName, ipAddr, server);
    }

    public static void discoverPeers(WifiP2pManager.Channel c, int channelId, WifiP2pManager.ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().discoverPeers(c, channelId, listener);
    }
}
