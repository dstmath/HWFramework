package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

public interface DummyHwWifiDevicePolicy {
    boolean isWifiRestricted(WifiConfiguration wifiConfiguration, boolean z);

    void registerBroadcasts(Context context);
}
