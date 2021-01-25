package com.huawei.android.hardware.display;

import android.content.Intent;
import android.hardware.display.WifiDisplayStatus;

public class WifiDisplayStatusExt {
    private WifiDisplayStatus mWifiDisplayStatus;

    public void init(Intent intent) {
        WifiDisplayStatus wifiDisplayStatus = null;
        if (intent != null) {
            if (intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS") != null) {
                wifiDisplayStatus = (WifiDisplayStatus) intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS");
            }
            this.mWifiDisplayStatus = wifiDisplayStatus;
            return;
        }
        this.mWifiDisplayStatus = null;
    }

    public WifiDisplayExt getActiveDisplay() {
        WifiDisplayStatus wifiDisplayStatus = this.mWifiDisplayStatus;
        if (wifiDisplayStatus == null || wifiDisplayStatus.getActiveDisplay() == null) {
            return null;
        }
        WifiDisplayExt wifiDisplayExt = new WifiDisplayExt();
        wifiDisplayExt.setWifiDisplay(this.mWifiDisplayStatus.getActiveDisplay());
        return wifiDisplayExt;
    }

    public boolean isEmpty() {
        return this.mWifiDisplayStatus == null;
    }
}
