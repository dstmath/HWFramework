package android.net;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Objects;

public final class RouteInfo implements Parcelable {
    public static final Parcelable.Creator<RouteInfo> CREATOR = new Parcelable.Creator<RouteInfo>() {
        /* class android.net.RouteInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RouteInfo createFromParcel(Parcel in) {
            IpPrefix dest = (IpPrefix) in.readParcelable(null);
            InetAddress gateway = null;
            try {
                gateway = InetAddress.getByAddress(in.createByteArray());
            } catch (UnknownHostException e) {
            }
            return new RouteInfo(dest, gateway, in.readString(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public RouteInfo[] newArray(int size) {
            return new RouteInfo[size];
        }
    };
    @SystemApi
    public static final int RTN_THROW = 9;
    @SystemApi
    public static final int RTN_UNICAST = 1;
    @SystemApi
    public static final int RTN_UNREACHABLE = 7;
    private final IpPrefix mDestination;
    @UnsupportedAppUsage
    private final InetAddress mGateway;
    private final boolean mHasGateway;
    private final String mInterface;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final boolean mIsHost;
    private final int mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface RouteType {
    }

    @SystemApi
    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface, int type) {
        if (type == 1 || type == 7 || type == 9) {
            if (destination == null) {
                if (gateway == null) {
                    throw new IllegalArgumentException("Invalid arguments passed in: " + gateway + SmsManager.REGEX_PREFIX_DELIMITER + destination);
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

    @UnsupportedAppUsage
    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface) {
        this(destination, gateway, iface, 1);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public RouteInfo(LinkAddress destination, InetAddress gateway) {
        this(destination, gateway, (String) null);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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

    @SystemApi
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

    @UnsupportedAppUsage
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
            val = "****";
        }
        int i = this.mType;
        if (i == 7) {
            return val + " unreachable";
        } else if (i == 9) {
            return val + " throw";
        } else {
            String val2 = val + " ->";
            if (this.mGateway != null) {
                val2 = val2 + " ****";
            }
            if (this.mInterface != null) {
                val2 = val2 + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mInterface;
            }
            if (this.mType == 1) {
                return val2;
            }
            return val2 + " unknown type " + this.mType;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RouteInfo)) {
            return false;
        }
        RouteInfo target = (RouteInfo) obj;
        if (!Objects.equals(this.mDestination, target.getDestination()) || !Objects.equals(this.mGateway, target.getGateway()) || !Objects.equals(this.mInterface, target.getInterface()) || this.mType != target.getType()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = this.mDestination.hashCode() * 41;
        InetAddress inetAddress = this.mGateway;
        int i = 0;
        int hashCode2 = hashCode + (inetAddress == null ? 0 : inetAddress.hashCode() * 47);
        String str = this.mInterface;
        if (str != null) {
            i = str.hashCode() * 67;
        }
        return hashCode2 + i + (this.mType * 71);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDestination, flags);
        InetAddress inetAddress = this.mGateway;
        dest.writeByteArray(inetAddress == null ? null : inetAddress.getAddress());
        dest.writeString(this.mInterface);
        dest.writeInt(this.mType);
    }
}
