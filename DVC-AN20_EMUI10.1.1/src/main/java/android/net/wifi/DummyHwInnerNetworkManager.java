package android.net.wifi;

import java.util.List;

public class DummyHwInnerNetworkManager implements HwInnerNetworkManager {
    private static HwInnerNetworkManager mHwInnerNetworkManager = null;

    public static HwInnerNetworkManager getDefault() {
        if (mHwInnerNetworkManager == null) {
            mHwInnerNetworkManager = new DummyHwInnerNetworkManager();
        }
        return mHwInnerNetworkManager;
    }

    @Override // android.net.wifi.HwInnerNetworkManager
    public List<String> getApLinkedStaList() {
        return null;
    }

    @Override // android.net.wifi.HwInnerNetworkManager
    public void setSoftapMacFilter(String macFilter) {
    }

    @Override // android.net.wifi.HwInnerNetworkManager
    public void setSoftapDisassociateSta(String mac) {
    }

    @Override // android.net.wifi.HwInnerNetworkManager
    public void setAccessPointHw(String wlanIface, String softapIface) {
    }

    @Override // android.net.wifi.HwInnerNetworkManager
    public String getWiFiDnsStats(int netid) {
        return "";
    }
}
