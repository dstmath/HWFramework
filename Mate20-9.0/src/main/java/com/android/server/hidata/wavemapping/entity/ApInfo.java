package com.android.server.hidata.wavemapping.entity;

public class ApInfo {
    private String mac = "";
    private int srcType;
    private String ssid = "";
    private String uptime = "";

    public ApInfo(String ssid2, String mac2, String uptime2, int srcType2) {
        this.ssid = ssid2;
        this.uptime = uptime2;
        this.mac = mac2;
        this.srcType = srcType2;
    }

    public ApInfo(String ssid2, String mac2, String uptime2) {
        this.ssid = ssid2;
        this.uptime = uptime2;
        this.mac = mac2;
    }

    public ApInfo(String ssid2, String uptime2) {
        this.ssid = ssid2;
        this.uptime = uptime2;
    }

    public ApInfo() {
    }

    public int getSrcType() {
        return this.srcType;
    }

    public void setSrcType(int srcType2) {
        this.srcType = srcType2;
    }

    public String getUptime() {
        return this.uptime;
    }

    public void setUptime(String uptime2) {
        this.uptime = uptime2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac2) {
        this.mac = mac2;
    }

    public String toString() {
        return "ApInfo{ssid='" + this.ssid + '\'' + ", mac='" + this.mac + '\'' + '}';
    }
}
