package ohos.wifi;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class IpInfo implements Sequenceable {
    private int gateway;
    private int ipAddress;
    private int leaseDuration;
    private int netmask;
    private int primaryDns;
    private int secondDns;
    private int serverIp;

    public IpInfo(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        this.ipAddress = i;
        this.gateway = i2;
        this.netmask = i3;
        this.primaryDns = i4;
        this.secondDns = i5;
        this.serverIp = i6;
        this.leaseDuration = i7;
    }

    public IpInfo() {
        this(0, 0, 0, 0, 0, 0, 0);
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        if ((((((parcel.writeInt(this.ipAddress) && parcel.writeInt(this.gateway)) && parcel.writeInt(this.netmask)) && parcel.writeInt(this.primaryDns)) && parcel.writeInt(this.secondDns)) && parcel.writeInt(this.serverIp)) && parcel.writeInt(this.leaseDuration)) {
            return true;
        }
        parcel.reclaim();
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.ipAddress = parcel.readInt();
        this.gateway = parcel.readInt();
        this.netmask = parcel.readInt();
        this.primaryDns = parcel.readInt();
        this.secondDns = parcel.readInt();
        this.serverIp = parcel.readInt();
        this.leaseDuration = parcel.readInt();
        return true;
    }

    public int getIpAddress() {
        return this.ipAddress;
    }

    public int getGateway() {
        return this.gateway;
    }

    public int getNetmask() {
        return this.netmask;
    }

    public int getPrimaryDns() {
        return this.primaryDns;
    }

    public int getSecondDns() {
        return this.secondDns;
    }

    public int getServerIp() {
        return this.serverIp;
    }

    public int getLeaseDuration() {
        return this.leaseDuration;
    }
}
