package com.huawei.nearbysdk;

import android.os.Parcel;
import android.os.Parcelable;

public final class NearbyConfiguration implements Parcelable {
    public static final Parcelable.Creator<NearbyConfiguration> CREATOR = new Parcelable.Creator<NearbyConfiguration>() {
        public NearbyConfiguration createFromParcel(Parcel source) {
            NearbyConfiguration nearbyConfiguration = new NearbyConfiguration(source.readInt(), source.readString(), source.readString(), source.readInt(), source.readString(), source.readInt(), source.readInt());
            return nearbyConfiguration;
        }

        public NearbyConfiguration[] newArray(int size) {
            return new NearbyConfiguration[size];
        }
    };
    private static final String TAG = "NearbyConfiguration";
    public static final int WIFI_BAND_2GHZ = 1;
    public static final int WIFI_BAND_5GHZ = 2;
    public static final int WIFI_BAND_AUTO = 0;
    private int mChannelId;
    private int mTimeoutMs;
    private int mWifiBand;
    private String mWifiIp;
    private int mWifiPort;
    private String mWifiPwd;
    private String mWifiSsid;

    public NearbyConfiguration(int channelId, String wifiSsid, String wifiPwd, int wifiBand, String wifiIp, int wifiPort, int timeoutMs) {
        this.mChannelId = channelId;
        this.mWifiSsid = wifiSsid;
        this.mWifiPwd = wifiPwd;
        this.mWifiBand = wifiBand;
        this.mWifiIp = wifiIp;
        this.mWifiPort = wifiPort;
        this.mTimeoutMs = timeoutMs;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    public String getWifiSsid() {
        return this.mWifiSsid;
    }

    public String getWifiPwd() {
        return this.mWifiPwd;
    }

    public void setWifiBand(int wifiBand) {
        this.mWifiBand = wifiBand;
    }

    public int getWifiBand() {
        return this.mWifiBand;
    }

    public String getWifiIp() {
        return this.mWifiIp;
    }

    public void setWifiPort(int port) {
        this.mWifiPort = port;
    }

    public int getWifiPort() {
        return this.mWifiPort;
    }

    public int getTimeoutMs() {
        return this.mTimeoutMs;
    }

    public boolean equals(Object anObject) {
        boolean z = true;
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof NearbyConfiguration)) {
            return false;
        }
        NearbyConfiguration anDevice = (NearbyConfiguration) anObject;
        if (this.mChannelId != anDevice.mChannelId || !this.mWifiSsid.equals(anDevice.mWifiSsid) || !this.mWifiPwd.equals(anDevice.mWifiPwd) || this.mWifiBand != anDevice.mWifiBand || !this.mWifiIp.equals(anDevice.mWifiIp) || this.mWifiPort != anDevice.mWifiPort) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this.mWifiSsid == null) {
            return 0;
        }
        return this.mWifiSsid.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "NearbyConfiguration:{ChannelId=" + this.mChannelId + " WifiSsid=" + NearbyConfig.toFrontHalfString(this.mWifiSsid) + " WifiBand=" + this.mWifiBand + "}";
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(this.mChannelId);
        dest.writeString(this.mWifiSsid);
        dest.writeString(this.mWifiPwd);
        dest.writeInt(this.mWifiBand);
        dest.writeString(this.mWifiIp);
        dest.writeInt(this.mWifiPort);
        dest.writeInt(this.mTimeoutMs);
    }
}
