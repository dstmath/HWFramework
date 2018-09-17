package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.system.OsConstants;
import android.util.Pair;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;

public class LinkAddress implements Parcelable {
    public static final Creator<LinkAddress> CREATOR = new Creator<LinkAddress>() {
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

    static int scopeForUnicastAddress(InetAddress addr) {
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
        if (this.address == null || !(this.address instanceof Inet6Address)) {
            return false;
        }
        if ((this.address.getAddress()[0] & -2) == -4) {
            z = true;
        }
        return z;
    }

    private void init(InetAddress address, int prefixLength, int flags, int scope) {
        if (address == null || address.isMulticastAddress() || prefixLength < 0 || (((address instanceof Inet4Address) && prefixLength > 32) || prefixLength > 128)) {
            throw new IllegalArgumentException("Bad LinkAddress params " + address + "/" + prefixLength);
        }
        this.address = address;
        this.prefixLength = prefixLength;
        this.flags = flags;
        this.scope = scope;
    }

    public LinkAddress(InetAddress address, int prefixLength, int flags, int scope) {
        init(address, prefixLength, flags, scope);
    }

    public LinkAddress(InetAddress address, int prefixLength) {
        this(address, prefixLength, 0, 0);
        this.scope = scopeForUnicastAddress(address);
    }

    public LinkAddress(InterfaceAddress interfaceAddress) {
        this(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength());
    }

    public LinkAddress(String address) {
        this(address, 0, 0);
        this.scope = scopeForUnicastAddress(this.address);
    }

    public LinkAddress(String address, int flags, int scope) {
        Pair<InetAddress, Integer> ipAndMask = NetworkUtils.parseIpAndMask(address);
        init((InetAddress) ipAndMask.first, ((Integer) ipAndMask.second).intValue(), flags, scope);
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
        return ((this.address.hashCode() + (this.prefixLength * 11)) + (this.flags * 19)) + (this.scope * 43);
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
        if (this.scope == OsConstants.RT_SCOPE_UNIVERSE && (isIPv6ULA() ^ 1) != 0 && ((long) (this.flags & (OsConstants.IFA_F_DADFAILED | OsConstants.IFA_F_DEPRECATED))) == 0) {
            return ((long) (this.flags & OsConstants.IFA_F_TENTATIVE)) == 0 || ((long) (this.flags & OsConstants.IFA_F_OPTIMISTIC)) != 0;
        } else {
            return false;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.address.getAddress());
        dest.writeInt(this.prefixLength);
        dest.writeInt(this.flags);
        dest.writeInt(this.scope);
    }
}
