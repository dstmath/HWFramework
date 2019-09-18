package com.android.server.wifi;

public class PreconfiguredNetwork {
    private int eapMethod;
    private String ssid;

    public PreconfiguredNetwork(String ssid2, int eapMethod2) {
        this.ssid = ssid2;
        this.eapMethod = eapMethod2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public int getEapMethod() {
        return this.eapMethod;
    }

    public void setEapMethod(int eapMethod2) {
        this.eapMethod = eapMethod2;
    }

    public String toString() {
        return "PreconfiguredNetwork{ssid='" + this.ssid + '\'' + ", eapMethod=" + this.eapMethod + '}';
    }
}
