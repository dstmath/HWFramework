package android.net.wifi.p2p;

import android.net.wifi.WifiConfiguration;

public class DummyHwInnerWifiP2pManager implements HwInnerWifiP2pManager {
    private static HwInnerWifiP2pManager mInstance = new DummyHwInnerWifiP2pManager();

    public HwInnerWifiP2pManager getDefault() {
        return mInstance;
    }

    public WifiConfiguration getWifiRepeaterConfiguration() {
        return null;
    }

    public String getGroupConfigInfo() {
        return null;
    }

    public boolean setWifiRepeaterConfiguration(WifiConfiguration wifiConfig) {
        return false;
    }

    public boolean releaseIPAddr(String ifName) {
        return false;
    }

    public boolean configIPAddr(String ifName, String ipAddr, String server) {
        return false;
    }
}
