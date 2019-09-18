package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.system.OsConstants;
import android.util.Pair;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;

public class LinkAddress implements Parcelable {
    public static final Parcelable.Creator<LinkAddress> CREATOR = new Parcelable.Creator<LinkAddress>() {
        public LinkAddress createFromParcel(Parcel in) {
            InetAddress address = null;
            try {
                address = InetAddress.getByAddress(in.createByteArray());
            } catch (UnknownHostException e) {
            }
            return new LinkAddress(address, in.readInt(), in.readInt(), in.readInt());
        }

        public LinkAddress[] newArray(int size) {
            return new LinkAddress[size];
        }
    };
    private InetAddress address;
    private int flags;
    private int prefixLength;
    private int scope;

    private static int scopeForUnicastAddress(InetAddress addr) {
        if (addr.isAnyLocalAddress()) {
            return OsConstants.RT_SCOPE_HOST;
        }
        if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
            return OsConstants.RT_SCOPE_LINK;
        }
        if ((addr instanceof Inet4Address) || !addr.isSiteLocalAddress()) {
            return OsConstants.RT_SCOPE_UNIVERSE;
        }
        return OsConstants.RT_SCOPE_SITE;
    }

    private boolean isIPv6ULA() {
        boolean z = false;
        if (!isIPv6()) {
            return false;
        }
        if ((this.address.getAddress()[0] & -2) == -4) {
            z = true;
        }
        return z;
    }

    public boolean isIPv6() {
        return this.address instanceof Inet6Address;
    }

    public boolean isIPv4() {
        return this.address instanceof Inet4Address;
    }

    private void init(InetAddress address2, int prefixLength2, int flags2, int scope2) {
        if (address2 == null || address2.isMulticastAddress() || prefixLength2 < 0 || (((address2 instanceof Inet4Address) && prefixLength2 > 32) || prefixLength2 > 128)) {
            throw new IllegalArgumentException("Bad LinkAddress params " + address2 + "/" + prefixLength2);
        }
        this.address = address2;
        this.prefixLength = prefixLength2;
        this.flags = flags2;
        this.scope = scope2;
    }

    public LinkAddress(InetAddress address2, int prefixLength2, int flags2, int scope2) {
        init(address2, prefixLength2, flags2, scope2);
    }

    public LinkAddress(InetAddress address2, int prefixLength2) {
        this(address2, prefixLength2, 0, 0);
        this.scope = scopeForUnicastAddress(address2);
    }

    public LinkAddress(InterfaceAddress interfaceAddress) {
        this(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength());
    }

    public LinkAddress(String address2) {
        this(address2, 0, 0);
        this.scope = scopeForUnicastAddress(this.address);
    }

    public LinkAddress(String address2, int flags2, int scope2) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(address2);
        init((InetAddress) ipAndMask.first, ((Integer) ipAndMask.second).intValue(), flags2, scope2);
    }

    public String toString() {
        return this.address.getHostAddress() + "/" + this.prefixLength;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof LinkAddress)) {
            return false;
        }
        LinkAddress linkAddress = (LinkAddress) obj;
        if (this.address.equals(linkAddress.address) && this.prefixLength == linkAddress.prefixLength && this.flags == linkAddress.flags && this.scope == linkAddress.scope) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.address.hashCode() + (11 * this.prefixLength) + (19 * this.flags) + (43 * this.scope);
    }

    public boolean isSameAddressAs(LinkAddress other) {
        return this.address.equals(other.address) && this.prefixLength == other.prefixLength;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPrefixLength() {
        return this.prefixLength;
    }

    public int getNetworkPrefixLength() {
        return getPrefixLength();
    }

    public int getFlags() {
        return this.flags;
    }

    public int getScope() {
        return this.scope;
    }

    public boolean isGlobalPreferred() {
        return this.scope == OsConstants.RT_SCOPE_UNIVERSE && !isIPv6ULA() && ((long) (this.flags & (OsConstants.IFA_F_DADFAILED | OsConstants.IFA_F_DEPRECATED))) == 0 && (((long) (this.flags & OsConstants.IFA_F_TENTATIVE)) == 0 || ((long) (this.flags & OsConstants.IFA_F_OPTIMISTIC)) != 0);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeByteArray(this.address.getAddress());
        dest.writeInt(this.prefixLength);
        dest.writeInt(this.flags);
        dest.writeInt(this.scope);
    }
}
