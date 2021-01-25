package ohos.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class LinkAddress implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "LinkAddress");
    public InetAddress address;
    public int flags;
    public int prefixLength;
    public int scope;

    public LinkAddress() {
    }

    public LinkAddress(InetAddress inetAddress, int i, int i2, int i3) {
        init(inetAddress, i, i2, i3);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LinkAddress)) {
            return false;
        }
        LinkAddress linkAddress = (LinkAddress) obj;
        if (this.address.equals(linkAddress.address) && this.prefixLength == linkAddress.prefixLength && this.flags == linkAddress.flags && this.scope == linkAddress.scope) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.address.hashCode() + (this.prefixLength * 11) + (this.flags * 19) + (this.scope * 43);
    }

    private void init(InetAddress inetAddress, int i, int i2, int i3) {
        if (inetAddress != null && i >= 0 && i <= 128 && !inetAddress.isMulticastAddress()) {
            if (!(inetAddress instanceof Inet4Address) || i <= 32) {
                this.address = inetAddress;
                this.prefixLength = i;
                this.flags = i2;
                this.scope = i3;
            }
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        try {
            this.address = InetAddress.getByAddress(parcel.readByteArray());
        } catch (UnknownHostException unused) {
            HiLog.error(LABEL, "UnknownHostException", new Object[0]);
        }
        this.prefixLength = parcel.readInt();
        this.flags = parcel.readInt();
        this.scope = parcel.readInt();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeByteArray(this.address.getAddress());
        parcel.writeInt(this.prefixLength);
        parcel.writeInt(this.flags);
        parcel.writeInt(this.scope);
        return true;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getScope() {
        return this.scope;
    }
}
