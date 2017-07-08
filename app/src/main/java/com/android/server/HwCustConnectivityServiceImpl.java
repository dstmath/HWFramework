package com.android.server;

import android.os.SystemProperties;

public class HwCustConnectivityServiceImpl extends HwCustConnectivityService {
    public boolean isSupportWifiConnectMode() {
        return SystemProperties.getBoolean("ro.config.hw_wifi_connect_mode", false);
    }
}
