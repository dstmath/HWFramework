package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public abstract class AbsWifiQualifiedNetworkSelector {
    public void handleAutoJoinCompleted(WifiConfiguration candidate) {
    }

    public void resetConnectConfig() {
    }

    public boolean networkIgnoredByStatus(WifiConfiguration config) {
        return false;
    }

    public boolean networkIgnoredByWifiPro(WifiConfiguration config) {
        return false;
    }

    public boolean networkIgnoredByInetAccessAndTry(WifiConfiguration config) {
        return false;
    }

    public boolean networkIgnoredByPortal(WifiConfiguration config) {
        return false;
    }
}
