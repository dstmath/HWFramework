package com.android.server.wifi.MSS;

public class HwMSSDatabaseItem implements Comparable {
    public String bssid;
    public int reasoncode;
    public String ssid;
    public long updatetime;

    public HwMSSDatabaseItem(String ssid2, String bssid2, int reasoncode2) {
        this.ssid = ssid2;
        this.bssid = bssid2;
        this.reasoncode = reasoncode2;
    }

    public HwMSSDatabaseItem(String ssid2, String bssid2, int reasoncode2, long updatetime2) {
        this.ssid = ssid2;
        this.bssid = bssid2;
        this.reasoncode = reasoncode2;
        this.updatetime = updatetime2;
    }

    public int compareTo(Object obj) {
        HwMSSDatabaseItem dataInfo = (HwMSSDatabaseItem) obj;
        int i = -1;
        if (dataInfo == null) {
            return -1;
        }
        long diff = dataInfo.updatetime - this.updatetime;
        if (diff > 0) {
            i = 1;
        } else if (diff >= 0) {
            i = 0;
        }
        return i;
    }

    public int hashCode() {
        return Long.valueOf(this.updatetime).hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && this.updatetime == ((HwMSSDatabaseItem) obj).updatetime) {
            return true;
        }
        return false;
    }
}
