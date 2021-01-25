package com.huawei.hwwifiproservice;

public class WifiProEnterpriseApRecord {
    private static final String TAG = "WifiProEnterpriseApRecord";
    private int mApSecurityType;
    private String mApSsid;

    public WifiProEnterpriseApRecord(String ssid, int secType) {
        resetAllParameters(ssid, secType);
    }

    public String getApSsid() {
        return this.mApSsid;
    }

    public int getApSecurityType() {
        return this.mApSecurityType;
    }

    public String toString() {
        return "mApSsid = " + this.mApSsid + ", mApSecurityType= " + this.mApSecurityType;
    }

    private void resetAllParameters(String ssid, int secType) {
        this.mApSsid = "";
        if (ssid != null) {
            this.mApSsid = ssid;
        }
        this.mApSecurityType = secType;
    }
}
