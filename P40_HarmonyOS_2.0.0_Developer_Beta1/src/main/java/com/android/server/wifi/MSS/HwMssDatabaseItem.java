package com.android.server.wifi.MSS;

public class HwMssDatabaseItem implements Comparable {
    protected String bssid;
    protected int reasonCode;
    protected String ssid;
    protected long updateTime;

    public HwMssDatabaseItem(String ssid2, String bssid2, int reasonCode2) {
        this.ssid = ssid2;
        this.bssid = bssid2;
        this.reasonCode = reasonCode2;
    }

    public HwMssDatabaseItem(String ssid2, String bssid2, int reasonCode2, long updateTime2) {
        this.ssid = ssid2;
        this.bssid = bssid2;
        this.reasonCode = reasonCode2;
        this.updateTime = updateTime2;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        HwMssDatabaseItem dataInfo = (HwMssDatabaseItem) obj;
        if (dataInfo == null) {
            return -1;
        }
        long diff = dataInfo.updateTime - this.updateTime;
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
        return Long.valueOf(this.updateTime).hashCode();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass() && (obj instanceof HwMssDatabaseItem) && this.updateTime == ((HwMssDatabaseItem) obj).updateTime) {
            return true;
        }
        return false;
    }
}
