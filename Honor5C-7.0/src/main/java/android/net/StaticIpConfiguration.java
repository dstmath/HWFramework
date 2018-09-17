package android.net;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StaticIpConfiguration implements Parcelable {
    public static Creator<StaticIpConfiguration> CREATOR;
    public final ArrayList<InetAddress> dnsServers;
    public String domains;
    public InetAddress gateway;
    public LinkAddress ipAddress;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.StaticIpConfiguration.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.StaticIpConfiguration.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.StaticIpConfiguration.<clinit>():void");
    }

    public StaticIpConfiguration() {
        this.dnsServers = new ArrayList();
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
        List<RouteInfo> routes = new ArrayList(3);
        if (this.ipAddress != null) {
            RouteInfo connectedRoute = new RouteInfo(this.ipAddress, null, iface);
            routes.add(connectedRoute);
            if (!(this.gateway == null || connectedRoute.matches(this.gateway))) {
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
        for (InetAddress dns : this.dnsServers) {
            lp.addDnsServer(dns);
        }
        lp.setDomains(this.domains);
        return lp;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("IP address ");
        String encryption_ipAddress = "xxx.xxx.xxx.xxx/xx";
        if (this.ipAddress != null) {
            str.append(encryption_ipAddress).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        str.append("Gateway ");
        if (this.gateway != null) {
            str.append(this.gateway.getHostAddress()).append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        str.append(" DNS servers: [");
        for (InetAddress dnsServer : this.dnsServers) {
            str.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER).append(dnsServer.getHostAddress());
        }
        str.append(" ] Domains ");
        if (this.domains != null) {
            str.append(this.domains);
        }
        return str.toString();
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((this.ipAddress == null ? 0 : this.ipAddress.hashCode()) + 611) * 47) + (this.gateway == null ? 0 : this.gateway.hashCode())) * 47;
        if (this.domains != null) {
            i = this.domains.hashCode();
        }
        return ((hashCode + i) * 47) + this.dnsServers.hashCode();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StaticIpConfiguration)) {
            return false;
        }
        StaticIpConfiguration other = (StaticIpConfiguration) obj;
        if (other != null && Objects.equals(this.ipAddress, other.ipAddress) && Objects.equals(this.gateway, other.gateway) && this.dnsServers.equals(other.dnsServers)) {
            z = Objects.equals(this.domains, other.domains);
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
        for (InetAddress dnsServer : this.dnsServers) {
            NetworkUtils.parcelInetAddress(dest, dnsServer, flags);
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
