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

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        HwMSSDatabaseItem dataInfo = (HwMSSDatabaseItem) obj;
        if (dataInfo == null) {
            return -1;
        }
        long diff = dataInfo.updatetime - this.updatetime;
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Long.valueOf(this.updatetime).hashCode();
    }

    @Override // java.lang.Object
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
