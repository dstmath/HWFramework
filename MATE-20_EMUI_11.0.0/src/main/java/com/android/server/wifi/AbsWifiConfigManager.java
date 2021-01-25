package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public abstract class AbsWifiConfigManager {
    public void setSupportWapiType() {
    }

    public void updateInternetInfoByWifiPro(WifiConfiguration config) {
    }

    public void updateWifiConfigByWifiPro(WifiConfiguration config, boolean uiOnly) {
    }

    public boolean tryUseStaticIpForFastConnecting(int netId) {
        return false;
    }

    public void updateNetworkConnFailedInfo(int netId, int rssi, int reason) {
    }

    public void resetNetworkConnFailedInfo(int netId) {
    }

    public void updateRssiDiscNonLocally(int netid, boolean disc, int rssi, long ts) {
    }

    public void mergeHwParamsWithInternalWifiConfiguration(WifiConfiguration internalConfig, WifiConfiguration externalConfig) {
    }
}
