package com.android.server.wifi.p2p;

import com.android.server.wifi.WifiInjector;

public class HwWifiP2pMonitorExt implements IHwWifiP2pMonitorExt {
    private static final String TAG = "HwWifiP2pMonitor";
    private WifiInjector mWifiInjector = null;
    private IHwWifiP2pMonitorInner mWifiP2pMonitorInner;

    HwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        this.mWifiP2pMonitorInner = wifiP2pMonitor;
        this.mWifiInjector = wifiInjector;
    }

    public static HwWifiP2pMonitorExt createHwWifiP2pMonitorExt(IHwWifiP2pMonitorInner wifiP2pMonitor, WifiInjector wifiInjector) {
        return new HwWifiP2pMonitorExt(wifiP2pMonitor, wifiInjector);
    }

    public void broadcastHwP2pDeviceFound(String iface, byte[] hwInfo) {
        this.mWifiP2pMonitorInner.sendMessageEx(iface, 147577, hwInfo);
    }
}
