package com.android.server.wifi.wifipro;

public class WifiProEnterpriseApRecord {
    private static final String TAG = "WifiProEnterpriseApRecord";
    public String apSSID;
    public int apSecurityType;

    public WifiProEnterpriseApRecord(String ssid, int secType) {
        resetAllParameters(ssid, secType);
    }

    private void resetAllParameters(String ssid, int secType) {
        this.apSSID = "";
        if (ssid != null) {
            this.apSSID = ssid;
        }
        this.apSecurityType = secType;
    }

    public String toString() {
        return "apSSID =" + this.apSSID + ", apSecurityType=" + this.apSecurityType;
    }
}
