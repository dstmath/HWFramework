package ohos.wifi;

import ohos.annotation.SystemApi;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

@SystemApi
public final class HotspotConfig implements Sequenceable {
    private int band;
    private int maxConn;
    private String preSharedKey;
    private int securityType;
    private String ssid;

    public HotspotConfig(String str, int i, int i2, String str2, int i3) {
        this.ssid = str;
        this.securityType = i;
        this.band = i2;
        this.preSharedKey = str2;
        this.maxConn = i3;
    }

    public HotspotConfig() {
        this(null, 0, 0, null, 0);
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String str) {
        this.ssid = str;
    }

    public int getSecurityType() {
        return this.securityType;
    }

    public void setSecurityType(int i) {
        this.securityType = i;
    }

    public int getBand() {
        return this.band;
    }

    public void setBand(int i) {
        this.band = i;
    }

    public String getPreSharedKey() {
        return this.preSharedKey;
    }

    public void setPreSharedKey(String str) {
        this.preSharedKey = str;
    }

    public int getMaxConn() {
        return this.maxConn;
    }

    public void setMaxConn(int i) {
        this.maxConn = i;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if ((((parcel.writeString(this.ssid) && parcel.writeInt(this.securityType)) && parcel.writeInt(this.band)) && parcel.writeString(this.preSharedKey)) && parcel.writeInt(this.maxConn)) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.ssid = parcel.readString();
        this.securityType = parcel.readInt();
        this.band = parcel.readInt();
        this.preSharedKey = parcel.readString();
        this.maxConn = parcel.readInt();
        return true;
    }
}
