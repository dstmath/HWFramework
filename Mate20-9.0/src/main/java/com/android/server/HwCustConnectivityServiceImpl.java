package com.android.server;

import android.content.Context;
import android.provider.Settings;

public class HwCustConnectivityServiceImpl extends HwCustConnectivityService {
    public boolean isSupportWifiConnectMode(Context context) {
        if (context == null) {
            return false;
        }
        return "true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_wifi_connect_mode"));
    }
}
