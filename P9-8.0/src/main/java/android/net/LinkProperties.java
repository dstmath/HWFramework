package android.net;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public final class LinkProperties implements Parcelable {
    public static final Creator<LinkProperties> CREATOR = new Creator<LinkProperties>() {
        public LinkProperties createFromParcel(Parcel in) {
            int i;
            LinkProperties netProp = new LinkProperties();
            String iface = in.readString();
            if (iface != null) {
                netProp.setInterfaceName(iface);
            }
            int addressCount = in.readInt();
            for (i = 0; i < addressCount; i++) {
                netProp.addLinkAddress((LinkAddress) in.readParcelable(null));
            }
            addressCount = in.readInt();
            for (i = 0; i < addressCount; i++) {
                try {
                    netProp.addDnsServer(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            netProp.setDomains(in.readString());
            netProp.setMtu(in.readInt());
            netProp.setTcpBufferSizes(in.readString());
            addressCount = in.readInt();
            for (i = 0; i < addressCount; i++) {
                netProp.addRoute((RouteInfo) in.readParcelable(null));
            }
            if (in.readByte() == (byte) 1) {
                netProp.setHttpProxy((ProxyInfo) in.readParcelable(null));
            }
            ArrayList<LinkProperties> stackedLinks = new ArrayList();
            in.readList(stackedLinks, LinkProperties.class.getClassLoader());
            for (LinkProperties stackedLink : stackedLinks) {
                netProp.addStackedLink(stackedLink);
            }
            return netProp;
        }

        public LinkProperties[] newArray(int size) {
            return new LinkProperties[size];
        }
    };
    private static final int MAX_MTU = 10000;
    private static final int MIN_MTU = 68;
    private static final int MIN_MTU_V6 = 1280;
    private ArrayList<InetAddress> mDnses = new ArrayList();
    private String mDomains;
    private ProxyInfo mHttpProxy;
    private String mIfaceName;
    private ArrayList<LinkAddress> mLinkAddresses = new ArrayList();
    private int mMtu;
    private ArrayList<RouteInfo> mRoutes = new ArrayList();
    private Hashtable<String, LinkProperties> mStackedLinks = new Hashtable();
    private String mTcpBufferSizes;

    public static class CompareResult<T> {
        public List<T> added = new ArrayList();
        public List<T> removed = new ArrayList();

        public String toString() {
            String retVal = "removed=[";
            for (T addr : this.removed) {
                retVal = retVal + addr.toString() + ",";
            }
            retVal = retVal + "] added=[";
            for (T addr2 : this.added) {
                retVal = retVal + addr2.toString() + ",";
            }
            return retVal + "]";
        }
    }

    public enum ProvisioningChange {
        STILL_NOT_PROVISIONED,
        LOST_PROVISIONING,
        GAINED_PROVISIONING,
        STILL_PROVISIONED
    }

    public static ProvisioningChange compareProvisioning(LinkProperties before, LinkProperties after) {
        if (before.isProvisioned() && after.isProvisioned()) {
            if ((!before.isIPv4Provisioned() || (after.isIPv4Provisioned() ^ 1) == 0) && (!before.isIPv6Provisioned() || (after.isIPv6Provisioned() ^ 1) == 0)) {
                return ProvisioningChange.STILL_PROVISIONED;
            }
            return ProvisioningChange.LOST_PROVISIONING;
        } else if (before.isProvisioned() && (after.isProvisioned() ^ 1) != 0) {
            return ProvisioningChange.LOST_PROVISIONING;
        } else {
            if (before.isProvisioned() || !after.isProvisioned()) {
                return ProvisioningChange.STILL_NOT_PROVISIONED;
            }
            return ProvisioningChange.GAINED_PROVISIONING;
        }
    }

    public LinkProperties(LinkProperties source) {
        ProxyInfo proxyInfo = null;
        if (source != null) {
            this.mIfaceName = source.getInterfaceName();
            for (LinkAddress l : source.getLinkAddresses()) {
                this.mLinkAddresses.add(l);
            }
            for (InetAddress i : source.getDnsServers()) {
                this.mDnses.add(i);
            }
            this.mDomains = source.getDomains();
            for (RouteInfo r : source.getRoutes()) {
                this.mRoutes.add(r);
            }
            if (source.getHttpProxy() != null) {
                proxyInfo = new ProxyInfo(source.getHttpProxy());
            }
            this.mHttpProxy = proxyInfo;
            for (LinkProperties l2 : source.mStackedLinks.values()) {
                addStackedLink(l2);
            }
            setMtu(source.getMtu());
            this.mTcpBufferSizes = source.mTcpBufferSizes;
        }
    }

    public void setInterfaceName(String iface) {
        this.mIfaceName = iface;
        ArrayList<RouteInfo> newRoutes = new ArrayList(this.mRoutes.size());
        for (RouteInfo route : this.mRoutes) {
            newRoutes.add(routeWithInterface(route));
        }
        this.mRoutes = newRoutes;
    }

    public String getInterfaceName() {
        return this.mIfaceName;
    }

    public List<String> getAllInterfaceNames() {
        List<String> interfaceNames = new ArrayList(this.mStackedLinks.size() + 1);
        if (this.mIfaceName != null) {
            interfaceNames.add(new String(this.mIfaceName));
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            interfaceNames.addAll(stacked.getAllInterfaceNames());
        }
        return interfaceNames;
    }

    public List<InetAddress> getAddresses() {
        List<InetAddress> addresses = new ArrayList();
        for (LinkAddress linkAddress : this.mLinkAddresses) {
            addresses.add(linkAddress.getAddress());
        }
        return Collections.unmodifiableList(addresses);
    }

    public List<InetAddress> getAllAddresses() {
        List<InetAddress> addresses = new ArrayList();
        for (LinkAddress linkAddress : this.mLinkAddresses) {
            addresses.add(linkAddress.getAddress());
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            addresses.addAll(stacked.getAllAddresses());
        }
        return addresses;
    }

    private int findLinkAddressIndex(LinkAddress address) {
        for (int i = 0; i < this.mLinkAddresses.size(); i++) {
            if (((LinkAddress) this.mLinkAddresses.get(i)).isSameAddressAs(address)) {
                return i;
            }
        }
        return -1;
    }

    public boolean addLinkAddress(LinkAddress address) {
        if (address == null) {
            return false;
        }
        int i = findLinkAddressIndex(address);
        if (i < 0) {
            this.mLinkAddresses.add(address);
            return true;
        } else if (((LinkAddress) this.mLinkAddresses.get(i)).equals(address)) {
            return false;
        } else {
            this.mLinkAddresses.set(i, address);
            return true;
        }
    }

    public boolean removeLinkAddress(LinkAddress toRemove) {
        int i = findLinkAddressIndex(toRemove);
        if (i < 0) {
            return false;
        }
        this.mLinkAddresses.remove(i);
        return true;
    }

    public List<LinkAddress> getLinkAddresses() {
        return Collections.unmodifiableList(this.mLinkAddresses);
    }

    public List<LinkAddress> getAllLinkAddresses() {
        List<LinkAddress> addresses = new ArrayList();
        addresses.addAll(this.mLinkAddresses);
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            addresses.addAll(stacked.getAllLinkAddresses());
        }
        return addresses;
    }

    public void setLinkAddresses(Collection<LinkAddress> addresses) {
        this.mLinkAddresses.clear();
        for (LinkAddress address : addresses) {
            addLinkAddress(address);
        }
    }

    public boolean addDnsServer(InetAddress dnsServer) {
        if (dnsServer == null || (this.mDnses.contains(dnsServer) ^ 1) == 0) {
            return false;
        }
        this.mDnses.add(dnsServer);
        return true;
    }

    public boolean removeDnsServer(InetAddress dnsServer) {
        if (dnsServer != null) {
            return this.mDnses.remove(dnsServer);
        }
        return false;
    }

    public void setDnsServers(Collection<InetAddress> dnsServers) {
        this.mDnses.clear();
        for (InetAddress dnsServer : dnsServers) {
            addDnsServer(dnsServer);
        }
    }

    public List<InetAddress> getDnsServers() {
        return Collections.unmodifiableList(this.mDnses);
    }

    public void setDomains(String domains) {
        this.mDomains = domains;
    }

    public String getDomains() {
        return this.mDomains;
    }

    public void setMtu(int mtu) {
        this.mMtu = mtu;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public void setTcpBufferSizes(String tcpBufferSizes) {
        this.mTcpBufferSizes = tcpBufferSizes;
    }

    public String getTcpBufferSizes() {
        return this.mTcpBufferSizes;
    }

    private RouteInfo routeWithInterface(RouteInfo route) {
        return new RouteInfo(route.getDestination(), route.getGateway(), this.mIfaceName, route.getType());
    }

    public boolean addRoute(RouteInfo route) {
        if (route != null) {
            String routeIface = route.getInterface();
            if (routeIface == null || (routeIface.equals(this.mIfaceName) ^ 1) == 0) {
                route = routeWithInterface(route);
                if (!this.mRoutes.contains(route)) {
                    this.mRoutes.add(route);
                    return true;
                }
            }
            throw new IllegalArgumentException("Route added with non-matching interface: " + routeIface + " vs. " + this.mIfaceName);
        }
        return false;
    }

    public boolean removeRoute(RouteInfo route) {
        if (route == null || !Objects.equals(this.mIfaceName, route.getInterface())) {
            return false;
        }
        return this.mRoutes.remove(route);
    }

    public List<RouteInfo> getRoutes() {
        return Collections.unmodifiableList(this.mRoutes);
    }

    public List<RouteInfo> getAllRoutes() {
        List<RouteInfo> routes = new ArrayList();
        routes.addAll(this.mRoutes);
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            routes.addAll(stacked.getAllRoutes());
        }
        return routes;
    }

    public void setHttpProxy(ProxyInfo proxy) {
        this.mHttpProxy = proxy;
    }

    public ProxyInfo getHttpProxy() {
        return this.mHttpProxy;
    }

    public boolean addStackedLink(LinkProperties link) {
        if (link == null || link.getInterfaceName() == null) {
            return false;
        }
        this.mStackedLinks.put(link.getInterfaceName(), link);
        return true;
    }

    public boolean removeStackedLink(String iface) {
        boolean z = false;
        if (iface == null) {
            return false;
        }
        if (((LinkProperties) this.mStackedLinks.remove(iface)) != null) {
            z = true;
        }
        return z;
    }

    public List<LinkProperties> getStackedLinks() {
        if (this.mStackedLinks.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<LinkProperties> stacked = new ArrayList();
        for (LinkProperties link : this.mStackedLinks.values()) {
            stacked.add(new LinkProperties(link));
        }
        return Collections.unmodifiableList(stacked);
    }

    public void clear() {
        this.mIfaceName = null;
        this.mLinkAddresses.clear();
        this.mDnses.clear();
        this.mDomains = null;
        this.mRoutes.clear();
        this.mHttpProxy = null;
        this.mStackedLinks.clear();
        this.mMtu = 0;
        this.mTcpBufferSizes = null;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String ifaceName = this.mIfaceName == null ? ProxyInfo.LOCAL_EXCL_LIST : "InterfaceName: " + this.mIfaceName + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        String linkAddresses = "LinkAddresses: [*] ";
        String dns = "DnsAddresses: [";
        for (InetAddress addr : this.mDnses) {
            dns = dns + addr.getHostAddress() + ",";
        }
        dns = dns + "] ";
        String domainName = "Domains: " + this.mDomains;
        String mtu = " MTU: " + this.mMtu;
        String tcpBuffSizes = ProxyInfo.LOCAL_EXCL_LIST;
        if (this.mTcpBufferSizes != null) {
            tcpBuffSizes = " TcpBufferSizes: " + this.mTcpBufferSizes;
        }
        String routes = " Routes: [*] ";
        String proxy = this.mHttpProxy == null ? ProxyInfo.LOCAL_EXCL_LIST : " HttpProxy: " + this.mHttpProxy.toString() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        String stacked = ProxyInfo.LOCAL_EXCL_LIST;
        if (this.mStackedLinks.values().size() > 0) {
            stacked = stacked + " Stacked: [";
            for (LinkProperties link : this.mStackedLinks.values()) {
                stacked = stacked + " [" + link.toString() + " ],";
            }
            stacked = stacked + "] ";
        }
        return "{" + ifaceName + linkAddresses + routes + dns + domainName + mtu + tcpBuffSizes + proxy + stacked + "}";
    }

    public boolean hasIPv4Address() {
        for (LinkAddress address : this.mLinkAddresses) {
            if (address != null && (address.getAddress() instanceof Inet4Address)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasIPv4AddressOnInterface(String iface) {
        if (Objects.equals(iface, this.mIfaceName) && hasIPv4Address()) {
            return true;
        }
        if (iface == null || !this.mStackedLinks.containsKey(iface)) {
            return false;
        }
        return ((LinkProperties) this.mStackedLinks.get(iface)).hasIPv4Address();
    }

    public boolean hasGlobalIPv6Address() {
        for (LinkAddress address : this.mLinkAddresses) {
            if (address != null && (address.getAddress() instanceof Inet6Address) && address.isGlobalPreferred()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DefaultRoute() {
        for (RouteInfo r : this.mRoutes) {
            if (r.isIPv4Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DefaultRoute() {
        for (RouteInfo r : this.mRoutes) {
            if (r.isIPv6Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DnsServer() {
        for (InetAddress ia : this.mDnses) {
            if (ia instanceof Inet4Address) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DnsServer() {
        for (InetAddress ia : this.mDnses) {
            if (ia instanceof Inet6Address) {
                return true;
            }
        }
        return false;
    }

    public boolean isIPv4Provisioned() {
        if (hasIPv4Address() && hasIPv4DefaultRoute()) {
            return hasIPv4DnsServer();
        }
        return false;
    }

    public boolean isIPv6Provisioned() {
        if (hasGlobalIPv6Address() && hasIPv6DefaultRoute()) {
            return hasIPv6DnsServer();
        }
        return false;
    }

    public boolean isProvisioned() {
        return !isIPv4Provisioned() ? isIPv6Provisioned() : true;
    }

    public boolean isReachable(InetAddress ip) {
        boolean z = true;
        RouteInfo bestRoute = RouteInfo.selectBestRoute(getAllRoutes(), ip);
        if (bestRoute == null) {
            return false;
        }
        if (ip instanceof Inet4Address) {
            return hasIPv4AddressOnInterface(bestRoute.getInterface());
        }
        if (!(ip instanceof Inet6Address)) {
            return false;
        }
        if (ip.isLinkLocalAddress()) {
            if (((Inet6Address) ip).getScopeId() == 0) {
                z = false;
            }
            return z;
        }
        if (bestRoute.hasGateway()) {
            z = hasGlobalIPv6Address();
        }
        return z;
    }

    public boolean isIdenticalInterfaceName(LinkProperties target) {
        return TextUtils.equals(getInterfaceName(), target.getInterfaceName());
    }

    public boolean isIdenticalAddresses(LinkProperties target) {
        Collection<InetAddress> targetAddresses = target.getAddresses();
        Collection<InetAddress> sourceAddresses = getAddresses();
        return sourceAddresses.size() == targetAddresses.size() ? sourceAddresses.containsAll(targetAddresses) : false;
    }

    public boolean isIdenticalDnses(LinkProperties target) {
        boolean z = false;
        Collection<InetAddress> targetDnses = target.getDnsServers();
        String targetDomains = target.getDomains();
        if (this.mDomains == null) {
            if (targetDomains != null) {
                return false;
            }
        } else if (!this.mDomains.equals(targetDomains)) {
            return false;
        }
        if (this.mDnses.size() == targetDnses.size()) {
            z = this.mDnses.containsAll(targetDnses);
        }
        return z;
    }

    public boolean isIdenticalRoutes(LinkProperties target) {
        Collection<RouteInfo> targetRoutes = target.getRoutes();
        return this.mRoutes.size() == targetRoutes.size() ? this.mRoutes.containsAll(targetRoutes) : false;
    }

    public boolean isIdenticalHttpProxy(LinkProperties target) {
        if (getHttpProxy() == null) {
            return target.getHttpProxy() == null;
        } else {
            return getHttpProxy().equals(target.getHttpProxy());
        }
    }

    public boolean isIdenticalStackedLinks(LinkProperties target) {
        if (!this.mStackedLinks.keySet().equals(target.mStackedLinks.keySet())) {
            return false;
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            if (!stacked.equals(target.mStackedLinks.get(stacked.getInterfaceName()))) {
                return false;
            }
        }
        return true;
    }

    public boolean isIdenticalMtu(LinkProperties target) {
        return getMtu() == target.getMtu();
    }

    public boolean isIdenticalTcpBufferSizes(LinkProperties target) {
        return Objects.equals(this.mTcpBufferSizes, target.mTcpBufferSizes);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinkProperties)) {
            return false;
        }
        LinkProperties target = (LinkProperties) obj;
        if (isIdenticalInterfaceName(target) && isIdenticalAddresses(target) && isIdenticalDnses(target) && isIdenticalRoutes(target) && isIdenticalHttpProxy(target) && isIdenticalStackedLinks(target) && isIdenticalMtu(target)) {
            z = isIdenticalTcpBufferSizes(target);
        }
        return z;
    }

    public boolean keyEquals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinkProperties)) {
            return false;
        }
        LinkProperties target = (LinkProperties) obj;
        if (isIdenticalInterfaceName(target) && isIdenticalAddresses(target) && isIdenticalDnses(target)) {
            z = isIdenticalRoutes(target);
        }
        return z;
    }

    public CompareResult<LinkAddress> compareAddresses(LinkProperties target) {
        CompareResult<LinkAddress> result = new CompareResult();
        result.removed = new ArrayList(this.mLinkAddresses);
        result.added.clear();
        if (target != null) {
            for (LinkAddress newAddress : target.getLinkAddresses()) {
                if (!result.removed.remove(newAddress)) {
                    result.added.add(newAddress);
                }
            }
        }
        return result;
    }

    public CompareResult<InetAddress> compareDnses(LinkProperties target) {
        CompareResult<InetAddress> result = new CompareResult();
        result.removed = new ArrayList(this.mDnses);
        result.added.clear();
        if (target != null) {
            for (InetAddress newAddress : target.getDnsServers()) {
                if (!result.removed.remove(newAddress)) {
                    result.added.add(newAddress);
                }
            }
        }
        return result;
    }

    public CompareResult<RouteInfo> compareAllRoutes(LinkProperties target) {
        CompareResult<RouteInfo> result = new CompareResult();
        result.removed = getAllRoutes();
        result.added.clear();
        if (target != null) {
            for (RouteInfo r : target.getAllRoutes()) {
                if (!result.removed.remove(r)) {
                    result.added.add(r);
                }
            }
        }
        return result;
    }

    public CompareResult<String> compareAllInterfaceNames(LinkProperties target) {
        CompareResult<String> result = new CompareResult();
        result.removed = getAllInterfaceNames();
        result.added.clear();
        if (target != null) {
            for (String r : target.getAllInterfaceNames()) {
                if (!result.removed.remove(r)) {
                    result.added.add(r);
                }
            }
        }
        return result;
    }

    public int hashCode() {
        int i;
        int i2 = 0;
        if (this.mIfaceName == null) {
            i = 0;
        } else {
            int size = (this.mRoutes.size() * 41) + ((this.mDomains == null ? 0 : this.mDomains.hashCode()) + ((this.mDnses.size() * 37) + (this.mIfaceName.hashCode() + (this.mLinkAddresses.size() * 31))));
            if (this.mHttpProxy == null) {
                i = 0;
            } else {
                i = this.mHttpProxy.hashCode();
            }
            i = (i + size) + (this.mStackedLinks.hashCode() * 47);
        }
        i += this.mMtu * 51;
        if (this.mTcpBufferSizes != null) {
            i2 = this.mTcpBufferSizes.hashCode();
        }
        return i + i2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getInterfaceName());
        dest.writeInt(this.mLinkAddresses.size());
        for (LinkAddress linkAddress : this.mLinkAddresses) {
            dest.writeParcelable(linkAddress, flags);
        }
        dest.writeInt(this.mDnses.size());
        for (InetAddress d : this.mDnses) {
            dest.writeByteArray(d.getAddress());
        }
        dest.writeString(this.mDomains);
        dest.writeInt(this.mMtu);
        dest.writeString(this.mTcpBufferSizes);
        dest.writeInt(this.mRoutes.size());
        for (RouteInfo route : this.mRoutes) {
            dest.writeParcelable(route, flags);
        }
        if (this.mHttpProxy != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(this.mHttpProxy, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeList(new ArrayList(this.mStackedLinks.values()));
    }

    public static boolean isValidMtu(int mtu, boolean ipv6) {
        if (ipv6) {
            if (mtu >= 1280 && mtu <= 10000) {
                return true;
            }
        } else if (mtu >= 68 && mtu <= 10000) {
            return true;
        }
        return false;
    }
}
