package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StaticIpConfiguration implements Parcelable {
    public static Parcelable.Creator<StaticIpConfiguration> CREATOR = new Parcelable.Creator<StaticIpConfiguration>() {
        public StaticIpConfiguration createFromParcel(Parcel in) {
            StaticIpConfiguration s = new StaticIpConfiguration();
            StaticIpConfiguration.readFromParcel(s, in);
            return s;
        }

        public StaticIpConfiguration[] newArray(int size) {
            return new StaticIpConfiguration[size];
        }
    };
    public final ArrayList<InetAddress> dnsServers;
    public String domains;
    public InetAddress gateway;
    public LinkAddress ipAddress;

    public StaticIpConfiguration() {
        this.dnsServers = new ArrayList<>();
    }

    public StaticIpConfiguration(StaticIpConfiguration source) {
        this();
        if (source != null) {
            this.ipAddress = source.ipAddress;
            this.gateway = source.gateway;
            this.dnsServers.addAll(source.dnsServers);
            this.domains = source.domains;
        }
    }

    public void clear() {
        this.ipAddress = null;
        this.gateway = null;
        this.dnsServers.clear();
        this.domains = null;
    }

    public List<RouteInfo> getRoutes(String iface) {
        List<RouteInfo> routes = new ArrayList<>(3);
        if (this.ipAddress != null) {
            RouteInfo connectedRoute = new RouteInfo(this.ipAddress, (InetAddress) null, iface);
            routes.add(connectedRoute);
            if (this.gateway != null && !connectedRoute.matches(this.gateway)) {
                routes.add(RouteInfo.makeHostRoute(this.gateway, iface));
            }
        }
        if (this.gateway != null) {
            routes.add(new RouteInfo((IpPrefix) null, this.gateway, iface));
        }
        return routes;
    }

    public LinkProperties toLinkProperties(String iface) {
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(iface);
        if (this.ipAddress != null) {
            lp.addLinkAddress(this.ipAddress);
        }
        for (RouteInfo route : getRoutes(iface)) {
            lp.addRoute(route);
        }
        Iterator<InetAddress> it = this.dnsServers.iterator();
        while (it.hasNext()) {
            lp.addDnsServer(it.next());
        }
        lp.setDomains(this.domains);
        return lp;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("IP address ");
        if (this.ipAddress != null) {
            str.append("xxx.xxx.xxx.xxx/xx");
            str.append(" ");
        }
        str.append("Gateway ");
        if (this.gateway != null) {
            str.append("xxx.xxx.xxx.xxx/xx");
            str.append(" ");
        }
        str.append(" DNS servers: [");
        Iterator<InetAddress> it = this.dnsServers.iterator();
        while (it.hasNext()) {
            it.next();
            str.append(" ");
            str.append("xxx.xxx.xxx.xxx/xx");
        }
        str.append(" ] Domains ");
        if (this.domains != null) {
            str.append("xxx.xxx.xxx.xxx/xx");
        }
        return str.toString();
    }

    public int hashCode() {
        int i = 0;
        int result = 47 * ((47 * ((47 * 13) + (this.ipAddress == null ? 0 : this.ipAddress.hashCode()))) + (this.gateway == null ? 0 : this.gateway.hashCode()));
        if (this.domains != null) {
            i = this.domains.hashCode();
        }
        return (47 * (result + i)) + this.dnsServers.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StaticIpConfiguration)) {
            return false;
        }
        StaticIpConfiguration other = (StaticIpConfiguration) obj;
        if (other == null || !Objects.equals(this.ipAddress, other.ipAddress) || !Objects.equals(this.gateway, other.gateway) || !this.dnsServers.equals(other.dnsServers) || !Objects.equals(this.domains, other.domains)) {
            z = false;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.ipAddress, flags);
        NetworkUtils.parcelInetAddress(dest, this.gateway, flags);
        dest.writeInt(this.dnsServers.size());
        Iterator<InetAddress> it = this.dnsServers.iterator();
        while (it.hasNext()) {
            NetworkUtils.parcelInetAddress(dest, it.next(), flags);
        }
        dest.writeString(this.domains);
    }

    protected static void readFromParcel(StaticIpConfiguration s, Parcel in) {
        s.ipAddress = (LinkAddress) in.readParcelable(null);
        s.gateway = NetworkUtils.unparcelInetAddress(in);
        s.dnsServers.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            s.dnsServers.add(NetworkUtils.unparcelInetAddress(in));
        }
        s.domains = in.readString();
    }
}
