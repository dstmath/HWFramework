package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Objects;

public final class RouteInfo implements Parcelable {
    public static final Parcelable.Creator<RouteInfo> CREATOR = new Parcelable.Creator<RouteInfo>() {
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
        if (type == 1 || type == 7 || type == 9) {
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
            this.mHasGateway = true ^ gateway.isAnyLocalAddress();
            if ((!(destination.getAddress() instanceof Inet4Address) || (gateway instanceof Inet4Address)) && (!(destination.getAddress() instanceof Inet6Address) || (gateway instanceof Inet6Address))) {
                this.mDestination = destination;
                this.mGateway = gateway;
                this.mInterface = iface;
                this.mType = type;
                this.mIsHost = isHost();
                return;
            }
            throw new IllegalArgumentException("address family mismatch in RouteInfo constructor");
        }
        throw new IllegalArgumentException("Unknown route type " + type);
    }

    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface) {
        this(destination, gateway, iface, 1);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public RouteInfo(LinkAddress destination, InetAddress gateway, String iface) {
        this(r0, gateway, iface);
        IpPrefix ipPrefix;
        if (destination == null) {
            ipPrefix = null;
        } else {
            ipPrefix = new IpPrefix(destination.getAddress(), destination.getPrefixLength());
        }
    }

    public RouteInfo(IpPrefix destination, InetAddress gateway) {
        this(destination, gateway, (String) null);
    }

    public RouteInfo(LinkAddress destination, InetAddress gateway) {
        this(destination, gateway, (String) null);
    }

    public RouteInfo(InetAddress gateway) {
        this((IpPrefix) null, gateway, (String) null);
    }

    public RouteInfo(IpPrefix destination) {
        this(destination, (InetAddress) null, (String) null);
    }

    public RouteInfo(LinkAddress destination) {
        this(destination, (InetAddress) null, (String) null);
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
        return ((this.mDestination.getAddress() instanceof Inet4Address) && this.mDestination.getPrefixLength() == 32) || ((this.mDestination.getAddress() instanceof Inet6Address) && this.mDestination.getPrefixLength() == 128);
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
        return isDefaultRoute() && (this.mDestination.getAddress() instanceof Inet4Address);
    }

    public boolean isIPv6Default() {
        return isDefaultRoute() && (this.mDestination.getAddress() instanceof Inet6Address);
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
        String val = "";
        if (this.mDestination != null) {
            val = this.mDestination.toString();
        }
        if (this.mType == 7) {
            return val + " unreachable";
        } else if (this.mType == 9) {
            return val + " throw";
        } else {
            String val2 = val + " ->";
            if (this.mGateway != null) {
                val2 = val2 + " " + this.mGateway.getHostAddress();
            }
            if (this.mInterface != null) {
                val2 = val2 + " " + this.mInterface;
            }
            if (this.mType == 1) {
                return val2;
            }
            return val2 + " unknown type " + this.mType;
        }
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
        if (!Objects.equals(this.mDestination, target.getDestination()) || !Objects.equals(this.mGateway, target.getGateway()) || !Objects.equals(this.mInterface, target.getInterface()) || this.mType != target.getType()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (this.mDestination.hashCode() * 41) + (this.mGateway == null ? 0 : this.mGateway.hashCode() * 47);
        if (this.mInterface != null) {
            i = this.mInterface.hashCode() * 67;
        }
        return hashCode + i + (this.mType * 71);
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
