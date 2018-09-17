package com.android.server.wifi;

import android.net.wifi.WifiConfiguration;

public class SoftApModeConfiguration {
    final WifiConfiguration mConfig;
    final int mTargetMode;

    SoftApModeConfiguration(int targetMode, WifiConfiguration config) {
        this.mTargetMode = targetMode;
        this.mConfig = config;
    }

    public int getTargetMode() {
        return this.mTargetMode;
    }

    public WifiConfiguration getWifiConfiguration() {
        return this.mConfig;
    }
}
