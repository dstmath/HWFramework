package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class LinkProperties implements Parcelable {
    public static final Parcelable.Creator<LinkProperties> CREATOR = new Parcelable.Creator<LinkProperties>() {
        public LinkProperties createFromParcel(Parcel in) {
            LinkProperties netProp = new LinkProperties();
            String iface = in.readString();
            if (iface != null) {
                netProp.setInterfaceName(iface);
            }
            int addressCount = in.readInt();
            for (int i = 0; i < addressCount; i++) {
                netProp.addLinkAddress((LinkAddress) in.readParcelable(null));
            }
            int addressCount2 = in.readInt();
            for (int i2 = 0; i2 < addressCount2; i2++) {
                try {
                    netProp.addDnsServer(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e) {
                }
            }
            int addressCount3 = in.readInt();
            for (int i3 = 0; i3 < addressCount3; i3++) {
                try {
                    netProp.addValidatedPrivateDnsServer(InetAddress.getByAddress(in.createByteArray()));
                } catch (UnknownHostException e2) {
                }
            }
            netProp.setUsePrivateDns(in.readBoolean());
            netProp.setPrivateDnsServerName(in.readString());
            netProp.setDomains(in.readString());
            netProp.setMtu(in.readInt());
            netProp.setTcpBufferSizes(in.readString());
            int addressCount4 = in.readInt();
            for (int i4 = 0; i4 < addressCount4; i4++) {
                netProp.addRoute((RouteInfo) in.readParcelable(null));
            }
            if (in.readByte() == 1) {
                netProp.setHttpProxy((ProxyInfo) in.readParcelable(null));
            }
            ArrayList<LinkProperties> stackedLinks = new ArrayList<>();
            in.readList(stackedLinks, LinkProperties.class.getClassLoader());
            Iterator<LinkProperties> it = stackedLinks.iterator();
            while (it.hasNext()) {
                netProp.addStackedLink(it.next());
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
    private ArrayList<InetAddress> mDnses = new ArrayList<>();
    private String mDomains;
    private ProxyInfo mHttpProxy;
    private String mIfaceName;
    private ArrayList<LinkAddress> mLinkAddresses = new ArrayList<>();
    private int mMtu;
    private String mPrivateDnsServerName;
    private ArrayList<RouteInfo> mRoutes = new ArrayList<>();
    private Hashtable<String, LinkProperties> mStackedLinks = new Hashtable<>();
    private String mTcpBufferSizes;
    private boolean mUsePrivateDns;
    private ArrayList<InetAddress> mValidatedPrivateDnses = new ArrayList<>();

    public static class CompareResult<T> {
        public final List<T> added = new ArrayList();
        public final List<T> removed = new ArrayList();

        public CompareResult() {
        }

        public CompareResult(Collection<T> oldItems, Collection<T> newItems) {
            if (oldItems != null) {
                this.removed.addAll(oldItems);
            }
            if (newItems != null) {
                for (T newItem : newItems) {
                    if (!this.removed.remove(newItem)) {
                        this.added.add(newItem);
                    }
                }
            }
        }

        public String toString() {
            String retVal = "removed=[";
            for (T addr : this.removed) {
                retVal = retVal + addr.toString() + ",";
            }
            String retVal2 = retVal + "] added=[";
            for (T addr2 : this.added) {
                retVal2 = retVal2 + addr2.toString() + ",";
            }
            return retVal2 + "]";
        }
    }

    public enum ProvisioningChange {
        STILL_NOT_PROVISIONED,
        LOST_PROVISIONING,
        GAINED_PROVISIONING,
        STILL_PROVISIONED
    }

    public static ProvisioningChange compareProvisioning(LinkProperties before, LinkProperties after) {
        if (!before.isProvisioned() || !after.isProvisioned()) {
            if (before.isProvisioned() && !after.isProvisioned()) {
                return ProvisioningChange.LOST_PROVISIONING;
            }
            if (before.isProvisioned() || !after.isProvisioned()) {
                return ProvisioningChange.STILL_NOT_PROVISIONED;
            }
            return ProvisioningChange.GAINED_PROVISIONING;
        } else if ((!before.isIPv4Provisioned() || after.isIPv4Provisioned()) && (!before.isIPv6Provisioned() || after.isIPv6Provisioned())) {
            return ProvisioningChange.STILL_PROVISIONED;
        } else {
            return ProvisioningChange.LOST_PROVISIONING;
        }
    }

    public LinkProperties() {
    }

    public LinkProperties(LinkProperties source) {
        if (source != null) {
            this.mIfaceName = source.getInterfaceName();
            for (LinkAddress l : source.getLinkAddresses()) {
                this.mLinkAddresses.add(l);
            }
            for (InetAddress i : source.getDnsServers()) {
                this.mDnses.add(i);
            }
            for (InetAddress i2 : source.getValidatedPrivateDnsServers()) {
                this.mValidatedPrivateDnses.add(i2);
            }
            this.mUsePrivateDns = source.mUsePrivateDns;
            this.mPrivateDnsServerName = source.mPrivateDnsServerName;
            this.mDomains = source.getDomains();
            for (RouteInfo r : source.getRoutes()) {
                this.mRoutes.add(r);
            }
            this.mHttpProxy = source.getHttpProxy() == null ? null : new ProxyInfo(source.getHttpProxy());
            for (LinkProperties l2 : source.mStackedLinks.values()) {
                addStackedLink(l2);
            }
            setMtu(source.getMtu());
            this.mTcpBufferSizes = source.mTcpBufferSizes;
        }
    }

    public void setInterfaceName(String iface) {
        this.mIfaceName = iface;
        ArrayList<RouteInfo> newRoutes = new ArrayList<>(this.mRoutes.size());
        Iterator<RouteInfo> it = this.mRoutes.iterator();
        while (it.hasNext()) {
            newRoutes.add(routeWithInterface(it.next()));
        }
        this.mRoutes = newRoutes;
    }

    public String getInterfaceName() {
        return this.mIfaceName;
    }

    public List<String> getAllInterfaceNames() {
        List<String> interfaceNames = new ArrayList<>(this.mStackedLinks.size() + 1);
        if (this.mIfaceName != null) {
            interfaceNames.add(new String(this.mIfaceName));
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            interfaceNames.addAll(stacked.getAllInterfaceNames());
        }
        return interfaceNames;
    }

    public List<InetAddress> getAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            addresses.add(it.next().getAddress());
        }
        return Collections.unmodifiableList(addresses);
    }

    public List<InetAddress> getAllAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            addresses.add(it.next().getAddress());
        }
        for (LinkProperties stacked : this.mStackedLinks.values()) {
            addresses.addAll(stacked.getAllAddresses());
        }
        return addresses;
    }

    private int findLinkAddressIndex(LinkAddress address) {
        for (int i = 0; i < this.mLinkAddresses.size(); i++) {
            if (this.mLinkAddresses.get(i).isSameAddressAs(address)) {
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
        } else if (this.mLinkAddresses.get(i).equals(address)) {
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
        List<LinkAddress> addresses = new ArrayList<>();
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
        if (dnsServer == null || this.mDnses.contains(dnsServer)) {
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

    public void setUsePrivateDns(boolean usePrivateDns) {
        this.mUsePrivateDns = usePrivateDns;
    }

    public boolean isPrivateDnsActive() {
        return this.mUsePrivateDns;
    }

    public void setPrivateDnsServerName(String privateDnsServerName) {
        this.mPrivateDnsServerName = privateDnsServerName;
    }

    public String getPrivateDnsServerName() {
        return this.mPrivateDnsServerName;
    }

    public boolean addValidatedPrivateDnsServer(InetAddress dnsServer) {
        if (dnsServer == null || this.mValidatedPrivateDnses.contains(dnsServer)) {
            return false;
        }
        this.mValidatedPrivateDnses.add(dnsServer);
        return true;
    }

    public boolean removeValidatedPrivateDnsServer(InetAddress dnsServer) {
        if (dnsServer != null) {
            return this.mValidatedPrivateDnses.remove(dnsServer);
        }
        return false;
    }

    public void setValidatedPrivateDnsServers(Collection<InetAddress> dnsServers) {
        this.mValidatedPrivateDnses.clear();
        for (InetAddress dnsServer : dnsServers) {
            addValidatedPrivateDnsServer(dnsServer);
        }
    }

    public List<InetAddress> getValidatedPrivateDnsServers() {
        return Collections.unmodifiableList(this.mValidatedPrivateDnses);
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
            if (routeIface == null || routeIface.equals(this.mIfaceName)) {
                RouteInfo route2 = routeWithInterface(route);
                if (!this.mRoutes.contains(route2)) {
                    this.mRoutes.add(route2);
                    return true;
                }
            } else {
                throw new IllegalArgumentException("Route added with non-matching interface: " + routeIface + " vs. " + this.mIfaceName);
            }
        }
        return false;
    }

    public boolean removeRoute(RouteInfo route) {
        return route != null && Objects.equals(this.mIfaceName, route.getInterface()) && this.mRoutes.remove(route);
    }

    public List<RouteInfo> getRoutes() {
        return Collections.unmodifiableList(this.mRoutes);
    }

    public void ensureDirectlyConnectedRoutes() {
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            addRoute(new RouteInfo(it.next(), (InetAddress) null, this.mIfaceName));
        }
    }

    public List<RouteInfo> getAllRoutes() {
        List<RouteInfo> routes = new ArrayList<>();
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
        if (this.mStackedLinks.remove(iface) != null) {
            z = true;
        }
        return z;
    }

    public List<LinkProperties> getStackedLinks() {
        if (this.mStackedLinks.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<LinkProperties> stacked = new ArrayList<>();
        for (LinkProperties link : this.mStackedLinks.values()) {
            stacked.add(new LinkProperties(link));
        }
        return Collections.unmodifiableList(stacked);
    }

    public void clear() {
        this.mIfaceName = null;
        this.mLinkAddresses.clear();
        this.mDnses.clear();
        this.mUsePrivateDns = false;
        this.mPrivateDnsServerName = null;
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
        String ifaceName;
        String proxy;
        if (this.mIfaceName == null) {
            ifaceName = "";
        } else {
            ifaceName = "InterfaceName: " + this.mIfaceName + " ";
        }
        String dns = "DnsAddresses: [";
        while (this.mDnses.iterator().hasNext()) {
            dns = dns + r4.next().getHostAddress() + ",";
        }
        String dns2 = dns + "] ";
        String usePrivateDns = "UsePrivateDns: " + this.mUsePrivateDns + " ";
        String privateDnsServerName = "PrivateDnsServerName: " + this.mPrivateDnsServerName + " ";
        if (!this.mValidatedPrivateDnses.isEmpty()) {
            String validatedPrivateDns = "ValidatedPrivateDnsAddresses: [";
            while (this.mValidatedPrivateDnses.iterator().hasNext()) {
                validatedPrivateDns = validatedPrivateDns + r7.next().getHostAddress() + ",";
            }
            String validatedPrivateDns2 = validatedPrivateDns + "] ";
        }
        String domainName = "Domains: " + this.mDomains;
        String mtu = " MTU: " + this.mMtu;
        String tcpBuffSizes = "";
        if (this.mTcpBufferSizes != null) {
            tcpBuffSizes = " TcpBufferSizes: " + this.mTcpBufferSizes;
        }
        if (this.mHttpProxy == null) {
            proxy = "";
        } else {
            proxy = " HttpProxy: " + this.mHttpProxy.toString() + " ";
        }
        String stacked = "";
        if (this.mStackedLinks.values().size() > 0) {
            String stacked2 = stacked + " Stacked: [";
            Iterator<LinkProperties> it = this.mStackedLinks.values().iterator();
            while (it.hasNext()) {
                stacked2 = stacked2 + " [" + it.next().toString() + " ],";
            }
            stacked = stacked2 + "] ";
        }
        return "{" + ifaceName + "LinkAddresses: [*] " + " Routes: [*] " + dns2 + usePrivateDns + privateDnsServerName + domainName + mtu + tcpBuffSizes + proxy + stacked + "}";
    }

    public boolean hasIPv4Address() {
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            LinkAddress address = it.next();
            if (address != null && (address.getAddress() instanceof Inet4Address)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasIPv4AddressOnInterface(String iface) {
        return (Objects.equals(iface, this.mIfaceName) && hasIPv4Address()) || (iface != null && this.mStackedLinks.containsKey(iface) && this.mStackedLinks.get(iface).hasIPv4Address());
    }

    public boolean hasGlobalIPv6Address() {
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            LinkAddress address = it.next();
            if (address != null && (address.getAddress() instanceof Inet6Address) && address.isGlobalPreferred()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DefaultRoute() {
        Iterator<RouteInfo> it = this.mRoutes.iterator();
        while (it.hasNext()) {
            if (it.next().isIPv4Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DefaultRoute() {
        Iterator<RouteInfo> it = this.mRoutes.iterator();
        while (it.hasNext()) {
            if (it.next().isIPv6Default()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv4DnsServer() {
        Iterator<InetAddress> it = this.mDnses.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Inet4Address) {
                return true;
            }
        }
        return false;
    }

    public boolean hasIPv6DnsServer() {
        Iterator<InetAddress> it = this.mDnses.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Inet6Address) {
                return true;
            }
        }
        return false;
    }

    public boolean isIPv4Provisioned() {
        return hasIPv4Address() && hasIPv4DefaultRoute() && hasIPv4DnsServer();
    }

    public boolean isIPv6Provisioned() {
        return hasGlobalIPv6Address() && hasIPv6DefaultRoute() && hasIPv6DnsServer();
    }

    public boolean isProvisioned() {
        return isIPv4Provisioned() || isIPv6Provisioned();
    }

    public boolean isReachable(InetAddress ip) {
        RouteInfo bestRoute = RouteInfo.selectBestRoute(getAllRoutes(), ip);
        boolean z = false;
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
            if (((Inet6Address) ip).getScopeId() != 0) {
                z = true;
            }
            return z;
        }
        if (!bestRoute.hasGateway() || hasGlobalIPv6Address()) {
            z = true;
        }
        return z;
    }

    public boolean isIdenticalInterfaceName(LinkProperties target) {
        return TextUtils.equals(getInterfaceName(), target.getInterfaceName());
    }

    public boolean isIdenticalAddresses(LinkProperties target) {
        Collection<InetAddress> targetAddresses = target.getAddresses();
        Collection<InetAddress> sourceAddresses = getAddresses();
        if (sourceAddresses.size() == targetAddresses.size()) {
            return sourceAddresses.containsAll(targetAddresses);
        }
        return false;
    }

    public boolean isIdenticalDnses(LinkProperties target) {
        Collection<InetAddress> targetDnses = target.getDnsServers();
        String targetDomains = target.getDomains();
        boolean z = false;
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

    public boolean isIdenticalPrivateDns(LinkProperties target) {
        return isPrivateDnsActive() == target.isPrivateDnsActive() && TextUtils.equals(getPrivateDnsServerName(), target.getPrivateDnsServerName());
    }

    public boolean isIdenticalValidatedPrivateDnses(LinkProperties target) {
        Collection<InetAddress> targetDnses = target.getValidatedPrivateDnsServers();
        if (this.mValidatedPrivateDnses.size() == targetDnses.size()) {
            return this.mValidatedPrivateDnses.containsAll(targetDnses);
        }
        return false;
    }

    public boolean isIdenticalRoutes(LinkProperties target) {
        Collection<RouteInfo> targetRoutes = target.getRoutes();
        if (this.mRoutes.size() == targetRoutes.size()) {
            return this.mRoutes.containsAll(targetRoutes);
        }
        return false;
    }

    public boolean isIdenticalHttpProxy(LinkProperties target) {
        if (getHttpProxy() == null) {
            return target.getHttpProxy() == null;
        }
        return getHttpProxy().equals(target.getHttpProxy());
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
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinkProperties)) {
            return false;
        }
        LinkProperties target = (LinkProperties) obj;
        if (!isIdenticalInterfaceName(target) || !isIdenticalAddresses(target) || !isIdenticalDnses(target) || !isIdenticalPrivateDns(target) || !isIdenticalValidatedPrivateDnses(target) || !isIdenticalRoutes(target) || !isIdenticalHttpProxy(target) || !isIdenticalStackedLinks(target) || !isIdenticalMtu(target) || !isIdenticalTcpBufferSizes(target)) {
            z = false;
        }
        return z;
    }

    public boolean keyEquals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LinkProperties)) {
            return false;
        }
        LinkProperties target = (LinkProperties) obj;
        if (!isIdenticalInterfaceName(target) || !isIdenticalAddresses(target) || !isIdenticalDnses(target) || !isIdenticalRoutes(target)) {
            z = false;
        }
        return z;
    }

    public CompareResult<LinkAddress> compareAddresses(LinkProperties target) {
        return new CompareResult<>(this.mLinkAddresses, target != null ? target.getLinkAddresses() : null);
    }

    public CompareResult<InetAddress> compareDnses(LinkProperties target) {
        return new CompareResult<>(this.mDnses, target != null ? target.getDnsServers() : null);
    }

    public CompareResult<InetAddress> compareValidatedPrivateDnses(LinkProperties target) {
        return new CompareResult<>(this.mValidatedPrivateDnses, target != null ? target.getValidatedPrivateDnsServers() : null);
    }

    public CompareResult<RouteInfo> compareAllRoutes(LinkProperties target) {
        return new CompareResult<>(getAllRoutes(), target != null ? target.getAllRoutes() : null);
    }

    public CompareResult<String> compareAllInterfaceNames(LinkProperties target) {
        return new CompareResult<>(getAllInterfaceNames(), target != null ? target.getAllInterfaceNames() : null);
    }

    public int hashCode() {
        int i;
        int i2 = 0;
        if (this.mIfaceName == null) {
            i = 0;
        } else {
            i = this.mIfaceName.hashCode() + (this.mLinkAddresses.size() * 31) + (this.mDnses.size() * 37) + (this.mValidatedPrivateDnses.size() * 61) + (this.mDomains == null ? 0 : this.mDomains.hashCode()) + (this.mRoutes.size() * 41) + (this.mHttpProxy == null ? 0 : this.mHttpProxy.hashCode()) + (this.mStackedLinks.hashCode() * 47);
        }
        int hashCode = i + (this.mMtu * 51) + (this.mTcpBufferSizes == null ? 0 : this.mTcpBufferSizes.hashCode()) + (this.mUsePrivateDns ? 57 : 0);
        if (this.mPrivateDnsServerName != null) {
            i2 = this.mPrivateDnsServerName.hashCode();
        }
        return hashCode + i2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getInterfaceName());
        dest.writeInt(this.mLinkAddresses.size());
        Iterator<LinkAddress> it = this.mLinkAddresses.iterator();
        while (it.hasNext()) {
            dest.writeParcelable(it.next(), flags);
        }
        dest.writeInt(this.mDnses.size());
        Iterator<InetAddress> it2 = this.mDnses.iterator();
        while (it2.hasNext()) {
            dest.writeByteArray(it2.next().getAddress());
        }
        dest.writeInt(this.mValidatedPrivateDnses.size());
        Iterator<InetAddress> it3 = this.mValidatedPrivateDnses.iterator();
        while (it3.hasNext()) {
            dest.writeByteArray(it3.next().getAddress());
        }
        dest.writeBoolean(this.mUsePrivateDns);
        dest.writeString(this.mPrivateDnsServerName);
        dest.writeString(this.mDomains);
        dest.writeInt(this.mMtu);
        dest.writeString(this.mTcpBufferSizes);
        dest.writeInt(this.mRoutes.size());
        Iterator<RouteInfo> it4 = this.mRoutes.iterator();
        while (it4.hasNext()) {
            dest.writeParcelable(it4.next(), flags);
        }
        if (this.mHttpProxy != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(this.mHttpProxy, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeList(new ArrayList<>(this.mStackedLinks.values()));
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
