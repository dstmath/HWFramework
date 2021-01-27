package com.android.server.connectivity.tethering;

import android.net.LinkProperties;
import android.net.NetworkState;
import android.net.RouteInfo;
import android.net.util.InterfaceSet;
import android.os.SystemProperties;
import android.util.Log;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public final class TetheringInterfaceUtils {
    private static final String TAG = "TetheringInterfaceUtils";

    public static InterfaceSet getTetheringInterfaces(NetworkState ns) {
        if (ns == null) {
            return null;
        }
        String if4 = getInterfaceForDestination(ns.linkProperties, Inet4Address.ANY);
        String if6 = getIPv6Interface(ns);
        if (if4 == null && if6 == null) {
            return null;
        }
        return new InterfaceSet(if4, if6);
    }

    private static boolean canTetherForOperatorEntry(NetworkState ns) {
        return (ns == null || ns.network == null || ns.linkProperties == null || ns.networkCapabilities == null || !ns.networkCapabilities.hasTransport(0)) ? false : true;
    }

    private static boolean canTetherForNotOperatorEntry(NetworkState ns) {
        return canTetherForOperatorEntry(ns) && ns.linkProperties.hasIpv6DnsServer() && ns.linkProperties.hasGlobalIpv6Address();
    }

    private static boolean isIOTVersion() {
        return SystemProperties.get("ro.hw.vendor").contains("iot") && "cn".equals(SystemProperties.get("ro.hw.country"));
    }

    public static String getIPv6Interface(NetworkState ns) {
        boolean canTether;
        boolean operatorEntry = isIOTVersion();
        if (operatorEntry) {
            canTether = canTetherForOperatorEntry(ns);
        } else {
            canTether = canTetherForNotOperatorEntry(ns);
        }
        if (!(!operatorEntry || ns == null || ns.network == null || ns.linkProperties == null)) {
            Log.i(TAG, "operatorEntry=" + operatorEntry + ",canTether = " + canTether + ",ns.linkProperties.hasIpv6DnsServer() = " + ns.linkProperties.hasIpv6DnsServer() + ", ns.linkProperties.hasGlobalIpv6Address() = " + ns.linkProperties.hasGlobalIpv6Address());
        }
        if (canTether) {
            return getInterfaceForDestination(ns.linkProperties, Inet6Address.ANY);
        }
        return null;
    }

    private static String getInterfaceForDestination(LinkProperties lp, InetAddress dst) {
        RouteInfo ri;
        if (lp != null) {
            ri = RouteInfo.selectBestRoute(lp.getAllRoutes(), dst);
        } else {
            ri = null;
        }
        if (ri != null) {
            return ri.getInterface();
        }
        return null;
    }
}
