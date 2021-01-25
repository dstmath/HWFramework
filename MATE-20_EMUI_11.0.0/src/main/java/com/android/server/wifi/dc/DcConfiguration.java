package com.android.server.wifi.dc;

import java.util.Arrays;

public class DcConfiguration {
    private final String[] ALLOWED_AUTH_TYPE = {"WPA2", "WPA/WPA2", "Open", "WPA2/WPA3"};
    private String mAuthType = null;
    private String mBssid = null;
    private int mFrequency = 0;
    private String mIfac = null;
    private boolean mIsSavedNetwork = false;
    private String mPreSharedKey = "";
    private int mRssi = 0;
    private String mSsid = null;

    public void setInterface(String iface) {
        this.mIfac = iface;
    }

    public String getInterface() {
        return this.mIfac;
    }

    public void setSsid(String ssid) {
        this.mSsid = ssid;
    }

    public String getSsid() {
        return this.mSsid;
    }

    public void setAuthType(String authType) {
        this.mAuthType = authType;
    }

    public String getAuthType() {
        return this.mAuthType;
    }

    public void setBssid(String bssid) {
        this.mBssid = bssid;
    }

    public String getBssid() {
        return this.mBssid;
    }

    public void setPreSharedKey(String preSharedKey) {
        this.mPreSharedKey = preSharedKey;
    }

    public String getPreSharedKey() {
        return this.mPreSharedKey;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    public int getFrequency() {
        return this.mFrequency;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void setIsSavedNetworkFlag(boolean isSavedNetwork) {
        this.mIsSavedNetwork = isSavedNetwork;
    }

    public boolean isSavedNetwork() {
        return this.mIsSavedNetwork;
    }

    public boolean isAuthTypeAllowed() {
        return Arrays.asList(this.ALLOWED_AUTH_TYPE).contains(this.mAuthType);
    }

    public String getConfigKey() {
        if (this.mAuthType.equalsIgnoreCase("Open")) {
            return this.mSsid + "NONE";
        }
        return this.mSsid + "WPA_PSK";
    }
}
