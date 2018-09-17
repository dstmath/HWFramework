package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public abstract class AbsWifiSupplicantControl {
    public void setWifiConfigurationWapi(WifiConfiguration config, int netId) {
    }

    public boolean isVariablesWapi(WifiConfiguration config, int netId) {
        return false;
    }
}
