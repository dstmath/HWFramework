package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public abstract class AbsWifiConfigManager {
    public void setSupportWapiType() {
    }

    public void updateWifiConfigByWifiPro(WifiConfiguration config) {
    }

    public boolean tryUseStaticIpForFastConnecting(int netId) {
        return false;
    }

    public void resetStaticIpConfig(int netId) {
    }

    public boolean skipEnableWithoutInternet(WifiConfiguration config) {
        return false;
    }
}
