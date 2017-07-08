package com.huawei.android.net.wifi.p2p;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import com.android.server.wifi.p2p.HwWifiP2pManagerEx;

public class NfcWifiP2pManagerEx {
    public static void addP2PValidDevice(WifiP2pManager mWifiP2pManager, Channel c, String deviceAddress, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().addP2PValidDevice(c, deviceAddress, listener);
    }

    public static void removeP2PValidDevice(WifiP2pManager mWifiP2pManager, Channel c, String deviceAddress, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().removeP2PValidDevice(c, deviceAddress, listener);
    }

    public static void beam_connect(WifiP2pManager mWifiP2pManager, Channel c, WifiP2pConfig config, ActionListener listener) {
        HwWifiP2pManagerEx.getDefault().beam_connect(c, config, listener);
    }
}
