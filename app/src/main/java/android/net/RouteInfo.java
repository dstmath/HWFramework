package android.net;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.security.keymaster.KeymasterDefs;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Objects;

public final class RouteInfo implements Parcelable {
    public static final Creator<RouteInfo> CREATOR = null;
    public static final int RTN_THROW = 9;
    public static final int RTN_UNICAST = 1;
    public static final int RTN_UNREACHABLE = 7;
    private final IpPrefix mDestination;
    private final InetAddress mGateway;
    private final boolean mHasGateway;
    private final String mInterface;
    private final boolean mIsHost;
    private final int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.RouteInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.RouteInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.RouteInfo.<clinit>():void");
    }

    public RouteInfo(IpPrefix destination, InetAddress gateway, String iface, int type) {
        boolean z = false;
        switch (type) {
            case RTN_UNICAST /*1*/:
            case RTN_UNREACHABLE /*7*/:
            case RTN_THROW /*9*/:
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
                if (!gateway.isAnyLocalAddress()) {
                    z = true;
                }
                this.mHasGateway = z;
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
        this(destination, gateway, iface, RTN_UNICAST);
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
        return new RouteInfo(new IpPrefix(host, (int) KeymasterDefs.KM_ALGORITHM_HMAC), gateway, iface);
    }

    private boolean isHost() {
        if ((this.mDestination.getAddress() instanceof Inet4Address) && this.mDestination.getPrefixLength() == 32) {
            return true;
        }
        if (!(this.mDestination.getAddress() instanceof Inet6Address)) {
            return false;
        }
        if (this.mDestination.getPrefixLength() != KeymasterDefs.KM_ALGORITHM_HMAC) {
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
        return this.mType == RTN_UNICAST && this.mDestination.getPrefixLength() == 0;
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
        if (this.mType == RTN_UNREACHABLE) {
            return val + " unreachable";
        }
        if (this.mType == RTN_THROW) {
            return val + " throw";
        }
        val = val + " ->";
        if (this.mGateway != null) {
            val = val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mGateway.getHostAddress();
        }
        if (this.mInterface != null) {
            val = val + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mInterface;
        }
        if (this.mType != RTN_UNICAST) {
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
        byte[] gatewayBytes = null;
        dest.writeParcelable(this.mDestination, flags);
        if (this.mGateway != null) {
            gatewayBytes = this.mGateway.getAddress();
        }
        dest.writeByteArray(gatewayBytes);
        dest.writeString(this.mInterface);
        dest.writeInt(this.mType);
    }
}
