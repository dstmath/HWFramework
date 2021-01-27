package com.android.server.wifi;

import android.content.Context;
import java.util.concurrent.atomic.AtomicInteger;

public class HwCustWifiStateMachineReference {
    public boolean isShowWifiAuthenticationFailurerNotification() {
        return false;
    }

    public void handleWifiAuthenticationFailureEvent(Context context, ClientModeImpl wsm) {
    }

    public String getHwCustCountryCode() {
        return null;
    }

    public int getHwCustWifiBand() {
        return -1;
    }

    public boolean setHwCustCountryCode(WifiNative wn) {
        return false;
    }

    public boolean setHwCustWifiBand(WifiNative wn, AtomicInteger fb) {
        return false;
    }

    public boolean isPpsConfigFileNotExists() {
        return false;
    }

    public void writePpsConfigFile() {
    }
}
