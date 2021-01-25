package com.android.server.wifi.hotspot2;

import android.text.TextUtils;

public class ANQPNetworkKey {
    private final int mAnqpDomainID;
    private final long mBSSID;
    private final long mHESSID;
    private final String mSSID;

    public ANQPNetworkKey(String ssid, long bssid, long hessid, int anqpDomainID) {
        this.mSSID = ssid;
        this.mBSSID = bssid;
        this.mHESSID = hessid;
        this.mAnqpDomainID = anqpDomainID;
    }

    public static ANQPNetworkKey buildKey(String ssid, long bssid, long hessid, int anqpDomainId) {
        if (anqpDomainId == 0) {
            return new ANQPNetworkKey(ssid, bssid, 0, 0);
        }
        if (hessid != 0) {
            return new ANQPNetworkKey(null, 0, hessid, anqpDomainId);
        }
        return new ANQPNetworkKey(ssid, 0, 0, anqpDomainId);
    }

    public int hashCode() {
        long j = this.mHESSID;
        if (j != 0) {
            return (int) (((((j >>> 32) * 31) + j) * 31) + ((long) this.mAnqpDomainID));
        }
        if (this.mBSSID == 0) {
            return (this.mSSID.hashCode() * 31) + this.mAnqpDomainID;
        }
        long j2 = this.mBSSID;
        return (int) (((((long) (this.mSSID.hashCode() * 31)) + (j2 >>> 32)) * 31) + j2);
    }

    public boolean equals(Object thatObject) {
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof ANQPNetworkKey)) {
            return false;
        }
        ANQPNetworkKey that = (ANQPNetworkKey) thatObject;
        if (TextUtils.equals(that.mSSID, this.mSSID) && that.mBSSID == this.mBSSID && that.mHESSID == this.mHESSID && that.mAnqpDomainID == this.mAnqpDomainID) {
            return true;
        }
        return false;
    }

    public String toString() {
        if (this.mHESSID != 0) {
            return Utils.macToString(this.mHESSID) + ":" + this.mAnqpDomainID;
        } else if (this.mBSSID != 0) {
            return Utils.macToString(this.mBSSID) + ":<" + this.mSSID + ">";
        } else {
            return "<" + this.mSSID + ">:" + this.mAnqpDomainID;
        }
    }
}
