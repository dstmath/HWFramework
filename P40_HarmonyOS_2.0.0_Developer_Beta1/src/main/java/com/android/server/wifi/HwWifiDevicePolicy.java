package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

public interface HwWifiDevicePolicy {
    boolean isWifiRestricted(WifiConfiguration wifiConfiguration, boolean z);

    void registerBroadcasts(Context context);
}
