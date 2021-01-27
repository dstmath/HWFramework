package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import java.util.List;

public interface IHwScanRequestProxyEx {
    boolean getAllowHiLinkScanResultsBroadcast();

    void sendHilinkscanResultBroadcast();

    void sendWifiCategoryChangeBroadcast();

    void setAllowHiLinkScanResultsBroadcast(boolean z);

    void startScanForHiddenNetwork(int i, WifiConfiguration wifiConfiguration);

    void updateScanResultByWifiPro(List<ScanResult> list);
}
