package ohos.net;

import java.net.InetAddress;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class RouteInfo implements Sequenceable {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "RouteInfo");
    private static final int RTN_UNICAST = 1;
    private IpPrefix mDestination;
    private InetAddress mGateway;
    private boolean mHasGateway = false;
    private String mInterface;
    private int mRtnType;

    public RouteInfo() {
    }

    public RouteInfo(IpPrefix ipPrefix, InetAddress inetAddress, String str, int i) {
        this.mDestination = ipPrefix;
        this.mGateway = inetAddress;
        this.mInterface = str;
        this.mRtnType = i;
        if (inetAddress != null) {
            this.mHasGateway = !inetAddress.isAnyLocalAddress();
        }
    }

    public String getInterface() {
        return this.mInterface;
    }

    public IpPrefix getDestination() {
        return this.mDestination;
    }

    public InetAddress getGateway() {
        return this.mGateway;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        if (parcel.readString() != null) {
            IpPrefix ipPrefix = new IpPrefix();
            ipPrefix.unmarshalling(parcel);
            this.mDestination = ipPrefix;
        }
        this.mGateway = null;
        parcel.readByteArray();
        this.mInterface = parcel.readString();
        this.mRtnType = parcel.readInt();
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeSequenceable(this.mDestination);
        InetAddress inetAddress = this.mGateway;
        parcel.writeByteArray(inetAddress == null ? null : inetAddress.getAddress());
        parcel.writeString(this.mInterface);
        parcel.writeInt(this.mRtnType);
        return true;
    }

    public boolean isDefaultRoute() {
        return this.mRtnType == 1 && this.mDestination.getPrefixLength() == 0;
    }

    public boolean hasGateway() {
        return this.mHasGateway;
    }

    public boolean matches(InetAddress inetAddress) {
        if (inetAddress == null || this.mDestination.getAddress() == null) {
            return false;
        }
        return this.mDestination.getAddress().equals(inetAddress);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(RouteInfo.class.getSimpleName());
        if (this.mDestination != null) {
            sb.append(": mDestination = ");
            sb.append("***");
        }
        sb.append(", mHasGateway = ");
        sb.append(this.mHasGateway);
        if (this.mGateway != null) {
            sb.append(", mGateway = ");
            sb.append("***");
        }
        sb.append(", mInterface = ");
        sb.append(this.mInterface);
        sb.append(", mRtnType = ");
        sb.append(this.mRtnType);
        return sb.toString();
    }
}
