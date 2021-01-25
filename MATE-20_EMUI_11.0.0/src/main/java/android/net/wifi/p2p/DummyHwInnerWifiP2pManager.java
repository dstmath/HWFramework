package android.net.wifi.p2p;

import android.net.wifi.WifiConfiguration;

public class DummyHwInnerWifiP2pManager implements HwInnerWifiP2pManager {
    private static HwInnerWifiP2pManager mInstance = new DummyHwInnerWifiP2pManager();

    public HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public WifiConfiguration getWifiRepeaterConfiguration() {
        return null;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public String getGroupConfigInfo() {
        return null;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return false;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public boolean releaseIPAddr(String ifName) {
        return false;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public boolean configIPAddr(String ifName, String ipAddr, String server) {
        return false;
    }

    @Override // android.net.wifi.p2p.HwInnerWifiP2pManager
    public boolean disableP2pGcDhcp(String tag, int type) {
        return false;
    }
}
