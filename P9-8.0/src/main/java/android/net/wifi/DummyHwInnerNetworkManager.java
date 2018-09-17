package android.net.wifi;

import android.net.ProxyInfo;
import java.util.List;

public class DummyHwInnerNetworkManager implements HwInnerNetworkManager {
    private static HwInnerNetworkManager mHwInnerNetworkManager = null;

    public static HwInnerNetworkManager getDefault() {
        if (mHwInnerNetworkManager == null) {
            mHwInnerNetworkManager = new DummyHwInnerNetworkManager();
        }
        return mHwInnerNetworkManager;
    }

    public List<String> getApLinkedStaList() {
        return null;
    }

    public void setSoftapMacFilter(String macFilter) {
    }

    public void setSoftapDisassociateSta(String mac) {
    }

    public void setAccessPointHw(String wlanIface, String softapIface) {
    }

    public String getWiFiDnsStats(int netid) {
        return ProxyInfo.LOCAL_EXCL_LIST;
    }
}
