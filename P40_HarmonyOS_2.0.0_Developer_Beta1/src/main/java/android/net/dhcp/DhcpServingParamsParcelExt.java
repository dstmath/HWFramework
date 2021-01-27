package android.net.dhcp;

import android.net.LinkAddress;
import android.net.shared.Inet4AddressUtils;
import com.google.android.collect.Sets;
import java.net.Inet4Address;
import java.util.Collection;
import java.util.Set;

public class DhcpServingParamsParcelExt extends DhcpServingParamsParcel {
    public static final int MTU_UNSET = 0;

    public DhcpServingParamsParcelExt setServerAddr(LinkAddress serverAddr) {
        this.serverAddr = Inet4AddressUtils.inet4AddressToIntHTH((Inet4Address) serverAddr.getAddress());
        this.serverAddrPrefixLength = serverAddr.getPrefixLength();
        return this;
    }

    public DhcpServingParamsParcelExt setDefaultRouters(Set<Inet4Address> defaultRouters) {
        this.defaultRouters = toIntArray(defaultRouters);
        return this;
    }

    public DhcpServingParamsParcelExt setDefaultRouters(Inet4Address... defaultRouters) {
        return setDefaultRouters(Sets.newArraySet(defaultRouters));
    }

    public DhcpServingParamsParcelExt setNoDefaultRouter() {
        return setDefaultRouters(new Inet4Address[0]);
    }

    public DhcpServingParamsParcelExt setDnsServers(Set<Inet4Address> dnsServers) {
        this.dnsServers = toIntArray(dnsServers);
        return this;
    }

    public DhcpServingParamsParcelExt setDnsServers(Inet4Address... dnsServers) {
        return setDnsServers(Sets.newArraySet(dnsServers));
    }

    public DhcpServingParamsParcelExt setNoDnsServer() {
        return setDnsServers(new Inet4Address[0]);
    }

    public DhcpServingParamsParcelExt setExcludedAddrs(Set<Inet4Address> excludedAddrs) {
        this.excludedAddrs = toIntArray(excludedAddrs);
        return this;
    }

    public DhcpServingParamsParcelExt setExcludedAddrs(Inet4Address... excludedAddrs) {
        return setExcludedAddrs(Sets.newArraySet(excludedAddrs));
    }

    public DhcpServingParamsParcelExt setDhcpLeaseTimeSecs(long dhcpLeaseTimeSecs) {
        this.dhcpLeaseTimeSecs = dhcpLeaseTimeSecs;
        return this;
    }

    public DhcpServingParamsParcelExt setLinkMtu(int linkMtu) {
        this.linkMtu = linkMtu;
        return this;
    }

    public DhcpServingParamsParcelExt setMetered(boolean metered) {
        this.metered = metered;
        return this;
    }

    private static int[] toIntArray(Collection<Inet4Address> addrs) {
        int[] res = new int[addrs.size()];
        int i = 0;
        for (Inet4Address addr : addrs) {
            res[i] = Inet4AddressUtils.inet4AddressToIntHTH(addr);
            i++;
        }
        return res;
    }
}
