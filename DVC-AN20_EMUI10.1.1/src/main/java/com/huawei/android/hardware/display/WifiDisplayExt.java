package com.huawei.android.hardware.display;

import android.hardware.display.WifiDisplay;
import com.huawei.uikit.effect.BuildConfig;

public class WifiDisplayExt {
    private WifiDisplay mWifiDisplay;

    public WifiDisplay getWifiDisplay() {
        return this.mWifiDisplay;
    }

    public void setWifiDisplay(WifiDisplay wifiDisplay) {
        this.mWifiDisplay = wifiDisplay;
    }

    public String getDeviceName() {
        WifiDisplay wifiDisplay = this.mWifiDisplay;
        if (wifiDisplay != null) {
            return wifiDisplay.getDeviceName();
        }
        return BuildConfig.FLAVOR;
    }
}
