package android.net.util;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PrefixUtils {
    public static final IpPrefix DEFAULT_WIFI_P2P_PREFIX = pfx("192.168.49.0/24");
    private static final IpPrefix[] MIN_NON_FORWARDABLE_PREFIXES = {pfx("127.0.0.0/8"), pfx("169.254.0.0/16"), pfx("::/3"), pfx("fe80::/64"), pfx("fc00::/7"), pfx("ff02::/8")};

    public static Set<IpPrefix> getNonForwardablePrefixes() {
        HashSet<IpPrefix> prefixes = new HashSet<>();
        addNonForwardablePrefixes(prefixes);
        return prefixes;
    }

    public static void addNonForwardablePrefixes(Set<IpPrefix> prefixes) {
        Collections.addAll(prefixes, MIN_NON_FORWARDABLE_PREFIXES);
    }

    public static Set<IpPrefix> localPrefixesFrom(LinkProperties lp) {
        HashSet<IpPrefix> localPrefixes = new HashSet<>();
        if (lp == null) {
            return localPrefixes;
        }
        for (LinkAddress addr : lp.getAllLinkAddresses()) {
            if (!addr.getAddress().isLinkLocalAddress()) {
                localPrefixes.add(asIpPrefix(addr));
            }
        }
        return localPrefixes;
    }

    public static IpPrefix asIpPrefix(LinkAddress addr) {
        return new IpPrefix(addr.getAddress(), addr.getPrefixLength());
    }

    public static IpPrefix ipAddressAsPrefix(InetAddress ip) {
        int bitLength;
        if (ip instanceof Inet4Address) {
            bitLength = 32;
        } else {
            bitLength = 128;
        }
        return new IpPrefix(ip, bitLength);
    }

    private static IpPrefix pfx(String prefixStr) {
        return new IpPrefix(prefixStr);
    }
}
