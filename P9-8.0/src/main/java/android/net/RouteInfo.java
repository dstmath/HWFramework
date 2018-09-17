package android.net;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Objects;

public final class RouteInfo implements Parcelable {
    public static final Creator<RouteInfo> CREATOR = new Creator<RouteInfo>() {
        public RouteInfo createFromParcel(Parcel in) {
            IpPrefix dest = (IpPrefix) in.readParcelable(null);
            InetAddress gateway = null;
            try {
                gateway = InetAddress.getByAddress(in.createByteArray());
            } catch (UnknownHostException e) {
            }
            return new RouteInfo(dest, gateway, in.readString(), in.readInt());
        }

        public RouteInfo[] newArray(int size) {
            return new RouteInfo[size];
        }
    };
    public static final int RTN_THROW = 9;
    public static final int RTN_UNICAST = 1;
    public static final int RTN_UNREACHABLE = 7;
    private final IpPrefix mDestination;
    private final InetAddress mGateway;
    private final boolean mHasGateway;
    private final String mInterface;
    private final boolean mIsHost;
    private final int mType;

    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface, int type) {
        switch (type) {
            case 1:
            case 7:
            case 9:
                if (destination == null) {
                    if (gateway == null) {
                        throw new IllegalArgumentException("Invalid arguments passed in: " + gateway + "," + destination);
                    } else if (gateway instanceof Inet4Address) {
                        destination = new IpPrefix(Inet4Address.ANY, 0);
                    } else {
                        destination = new IpPrefix(Inet6Address.ANY, 0);
                    }
                }
                if (gateway == null) {
                    if (destination.getAddress() instanceof Inet4Address) {
                        gateway = Inet4Address.ANY;
                    } else {
                        gateway = Inet6Address.ANY;
                    }
                }
                this.mHasGateway = gateway.isAnyLocalAddress() ^ 1;
                if ((!(destination.getAddress() instanceof Inet4Address) || (gateway instanceof Inet4Address)) && (!(destination.getAddress() instanceof Inet6Address) || (gateway instanceof Inet6Address))) {
                    this.mDestination = destination;
                    this.mGateway = gateway;
                    this.mInterface = iface;
                    this.mType = type;
                    this.mIsHost = isHost();
                    return;
                }
                throw new IllegalArgumentException("address family mismatch in RouteInfo constructor");
            default:
                throw new IllegalArgumentException("Unknown route type " + type);
        }
    }

    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface) {
        this(destination, gateway, iface, 1);
    }

    public RouteInfo(LinkAddress destination, InetAddress gateway, String iface) {
        IpPrefix ipPrefix = null;
        if (destination != null) {
            ipPrefix = new IpPrefix(destination.getAddress(), destination.getPrefixLength());
        }
        this(ipPrefix, gateway, iface);
    }

    public RouteInfo(IpPrefix destination, InetAddress gateway) {
        this(destination, gateway, null);
    }

    public RouteInfo(LinkAddress destination, InetAddress gateway) {
        this(destination, gateway, null);
    }

    public RouteInfo(InetAddress gateway) {
        this((IpPrefix) null, gateway, null);
    }

    public RouteInfo(IpPrefix destination) {
        this(destination, null, null);
    }

    public RouteInfo(LinkAddress destination) {
        this(destination, null, null);
    }

    public RouteInfo(IpPrefix destination, int type) {
        this(destination, null, null, type);
    }

    public static RouteInfo makeHostRoute(InetAddress host, String iface) {
        return makeHostRoute(host, null, iface);
    }

    public static RouteInfo makeHostRoute(InetAddress host, InetAddress gateway, String iface) {
        if (host == null) {
            return null;
        }
        if (host instanceof Inet4Address) {
            return new RouteInfo(new IpPrefix(host, 32), gateway, iface);
        }
        return new RouteInfo(new IpPrefix(host, 128), gateway, iface);
    }

    private boolean isHost() {
        if ((this.mDestination.getAddress() instanceof Inet4Address) && this.mDestination.getPrefixLength() == 32) {
            return true;
        }
        if (!(this.mDestination.getAddress() instanceof Inet6Address)) {
            return false;
        }
        if (this.mDestination.getPrefixLength() != 128) {
            return false;
        }
        return true;
    }

    public IpPrefix getDestination() {
        return this.mDestination;
    }

    public LinkAddress getDestinationLinkAddress() {
        return new LinkAddress(this.mDestination.getAddress(), this.mDestination.getPrefixLength());
    }

    public InetAddress getGateway() {
        return this.mGateway;
    }

    public String getInterface() {
        return this.mInterface;
    }

    public int getType() {
        return this.mType;
    }

    public boolean isDefaultRoute() {
        return this.mType == 1 && this.mDestination.getPrefixLength() == 0;
    }

    public boolean isIPv4Default() {
        return isDefaultRoute() ? this.mDestination.getAddress() instanceof Inet4Address : false;
    }

    public boolean isIPv6Default() {
        return isDefaultRoute() ? this.mDestination.getAddress() instanceof Inet6Address : false;
    }

    public boolean isHostRoute() {
        return this.mIsHost;
    }

    public boolean hasGateway() {
        return this.mHasGateway;
    }

    public boolean matches(InetAddress destination) {
        return this.mDestination.contains(destination);
    }

    public static RouteInfo selectBestRoute(Collection<RouteInfo> routes, InetAddress dest) {
        if (routes == null || dest == null) {
            return null;
        }
        RouteInfo bestRoute = null;
        for (RouteInfo route : routes) {
            if (NetworkUtils.addressTypeMatches(route.mDestination.getAddress(), dest) && ((bestRoute == null || bestRoute.mDestination.getPrefixLength() < route.mDestination.getPrefixLength()) && route.matches(dest))) {
                bestRoute = route;
            }
        }
        return bestRoute;
    }

    public String toString() {
        String val = ProxyInfo.LOCAL_EXCL_LIST;
        if (this.mDestination != null) {
            val = this.mDestination.toString();
        }
        if (this.mType == 7) {
            return val + " unreachable";
        }
        if (this.mType == 9) {
            return val + " throw";
        }
        val = val + " ->";
        if (this.mGateway != null) {
            val = val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mGateway.getHostAddress();
        }
        if (this.mInterface != null) {
            val = val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mInterface;
        }
        if (this.mType != 1) {
            return val + " unknown type " + this.mType;
        }
        return val;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RouteInfo)) {
            return false;
        }
        RouteInfo target = (RouteInfo) obj;
        if (!Objects.equals(this.mDestination, target.getDestination()) || !Objects.equals(this.mGateway, target.getGateway()) || !Objects.equals(this.mInterface, target.getInterface())) {
            z = false;
        } else if (this.mType != target.getType()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.mGateway == null ? 0 : this.mGateway.hashCode() * 47) + (this.mDestination.hashCode() * 41);
        if (this.mInterface != null) {
            i = this.mInterface.hashCode() * 67;
        }
        return (hashCode + i) + (this.mType * 71);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDestination, flags);
        dest.writeByteArray(this.mGateway == null ? null : this.mGateway.getAddress());
        dest.writeString(this.mInterface);
        dest.writeInt(this.mType);
    }
}
