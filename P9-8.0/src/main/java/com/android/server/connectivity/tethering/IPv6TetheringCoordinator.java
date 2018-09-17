package com.android.server.connectivity.tethering;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkState;
import android.net.RouteInfo;
import android.net.util.NetworkConstants;
import android.net.util.SharedLog;
import android.util.Log;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class IPv6TetheringCoordinator {
    private static final boolean DBG = false;
    private static final String TAG = IPv6TetheringCoordinator.class.getSimpleName();
    private static final boolean VDBG = false;
    private final LinkedList<Downstream> mActiveDownstreams = new LinkedList();
    private final SharedLog mLog;
    private short mNextSubnetId = (short) 0;
    private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
    private final byte[] mUniqueLocalPrefix = generateUniqueLocalPrefix();
    private NetworkState mUpstreamNetworkState;

    private static class Downstream {
        public final int mode;
        public final short subnetId;
        public final TetherInterfaceStateMachine tism;

        Downstream(TetherInterfaceStateMachine tism, int mode, short subnetId) {
            this.tism = tism;
            this.mode = mode;
            this.subnetId = subnetId;
        }
    }

    public IPv6TetheringCoordinator(ArrayList<TetherInterfaceStateMachine> notifyList, SharedLog log) {
        this.mNotifyList = notifyList;
        this.mLog = log.forSubComponent(TAG);
    }

    public void addActiveDownstream(TetherInterfaceStateMachine downstream, int mode) {
        if (findDownstream(downstream) == null) {
            if (this.mActiveDownstreams.offer(new Downstream(downstream, mode, this.mNextSubnetId))) {
                this.mNextSubnetId = (short) Math.max(0, this.mNextSubnetId + 1);
            }
            updateIPv6TetheringInterfaces();
        }
    }

    public void removeActiveDownstream(TetherInterfaceStateMachine downstream) {
        stopIPv6TetheringOn(downstream);
        if (this.mActiveDownstreams.remove(findDownstream(downstream))) {
            updateIPv6TetheringInterfaces();
        }
        if (this.mNotifyList.isEmpty()) {
            if (!this.mActiveDownstreams.isEmpty()) {
                Log.wtf(TAG, "Tethering notify list empty, IPv6 downstreams non-empty.");
            }
            this.mNextSubnetId = (short) 0;
        }
    }

    public void updateUpstreamNetworkState(NetworkState ns) {
        if (canTetherIPv6(ns, this.mLog)) {
            if (!(this.mUpstreamNetworkState == null || (ns.network.equals(this.mUpstreamNetworkState.network) ^ 1) == 0)) {
                stopIPv6TetheringOnAllInterfaces();
            }
            setUpstreamNetworkState(ns);
            updateIPv6TetheringInterfaces();
            return;
        }
        stopIPv6TetheringOnAllInterfaces();
        setUpstreamNetworkState(null);
    }

    private void stopIPv6TetheringOnAllInterfaces() {
        for (TetherInterfaceStateMachine sm : this.mNotifyList) {
            stopIPv6TetheringOn(sm);
        }
    }

    private void setUpstreamNetworkState(NetworkState ns) {
        if (ns == null) {
            this.mUpstreamNetworkState = null;
        } else {
            this.mUpstreamNetworkState = new NetworkState(null, new LinkProperties(ns.linkProperties), new NetworkCapabilities(ns.networkCapabilities), new Network(ns.network), null, null);
        }
        this.mLog.log("setUpstreamNetworkState: " + toDebugString(this.mUpstreamNetworkState));
    }

    private void updateIPv6TetheringInterfaces() {
        Iterator sm$iterator = this.mNotifyList.iterator();
        if (sm$iterator.hasNext()) {
            TetherInterfaceStateMachine sm = (TetherInterfaceStateMachine) sm$iterator.next();
            sm.sendMessage(TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE, 0, 0, getInterfaceIPv6LinkProperties(sm));
        }
    }

    private LinkProperties getInterfaceIPv6LinkProperties(TetherInterfaceStateMachine sm) {
        if (sm.interfaceType() == 2) {
            return null;
        }
        Downstream ds = findDownstream(sm);
        if (ds == null) {
            return null;
        }
        if (ds.mode == 3) {
            return getUniqueLocalConfig(this.mUniqueLocalPrefix, ds.subnetId);
        }
        if (this.mUpstreamNetworkState == null || this.mUpstreamNetworkState.linkProperties == null) {
            return null;
        }
        Downstream currentActive = (Downstream) this.mActiveDownstreams.peek();
        if (currentActive != null && currentActive.tism == sm) {
            LinkProperties lp = getIPv6OnlyLinkProperties(this.mUpstreamNetworkState.linkProperties);
            if (lp.hasIPv6DefaultRoute()) {
                return lp;
            }
        }
        return null;
    }

    Downstream findDownstream(TetherInterfaceStateMachine tism) {
        for (Downstream ds : this.mActiveDownstreams) {
            if (ds.tism == tism) {
                return ds;
            }
        }
        return null;
    }

    private static boolean canTetherIPv6(NetworkState ns, SharedLog sharedLog) {
        boolean canTether;
        if (ns == null || ns.network == null || ns.linkProperties == null || ns.networkCapabilities == null || !ns.linkProperties.hasIPv6DefaultRoute()) {
            canTether = false;
        } else {
            canTether = ns.networkCapabilities.hasTransport(0);
        }
        if (!(ns == null || ns.linkProperties == null)) {
            Log.d(TAG, "canTether:" + canTether + ", hasIPv6DefaultRoute: " + ns.linkProperties.hasIPv6DefaultRoute() + ", hasGlobalIPv6Address: " + ns.linkProperties.hasGlobalIPv6Address() + ", isProvisioned: " + ns.linkProperties.isProvisioned());
        }
        RouteInfo v4default = null;
        RouteInfo v6default = null;
        if (canTether) {
            for (RouteInfo r : ns.linkProperties.getAllRoutes()) {
                if (r.isIPv4Default()) {
                    v4default = r;
                } else if (r.isIPv6Default()) {
                    v6default = r;
                }
                if (v4default != null && v6default != null) {
                    break;
                }
            }
        }
        boolean outcome = canTether;
        if (ns == null) {
            sharedLog.log("No available upstream.");
        } else {
            String str = "IPv6 tethering is %s for upstream: %s";
            Object[] objArr = new Object[2];
            objArr[0] = outcome ? "available" : "not available";
            objArr[1] = toDebugString(ns);
            sharedLog.log(String.format(str, objArr));
        }
        return outcome;
    }

    private static LinkProperties getIPv6OnlyLinkProperties(LinkProperties lp) {
        LinkProperties v6only = new LinkProperties();
        if (lp == null) {
            return v6only;
        }
        v6only.setInterfaceName(lp.getInterfaceName());
        v6only.setMtu(lp.getMtu());
        for (LinkAddress linkAddr : lp.getLinkAddresses()) {
            if (linkAddr != null && linkAddr.getAddress() != null && (linkAddr.getAddress() instanceof Inet6Address) && linkAddr.getPrefixLength() == 64) {
                v6only.addLinkAddress(linkAddr);
            }
        }
        for (RouteInfo routeInfo : lp.getRoutes()) {
            IpPrefix destination = routeInfo.getDestination();
            if ((destination.getAddress() instanceof Inet6Address) && destination.getPrefixLength() <= 64) {
                v6only.addRoute(routeInfo);
            }
        }
        for (InetAddress dnsServer : lp.getDnsServers()) {
            if (isIPv6GlobalAddress(dnsServer)) {
                v6only.addDnsServer(dnsServer);
            }
        }
        v6only.setDomains(lp.getDomains());
        return v6only;
    }

    private static boolean isIPv6GlobalAddress(InetAddress ip) {
        return (!(ip instanceof Inet6Address) || (ip.isAnyLocalAddress() ^ 1) == 0 || (ip.isLoopbackAddress() ^ 1) == 0 || (ip.isLinkLocalAddress() ^ 1) == 0 || (ip.isSiteLocalAddress() ^ 1) == 0) ? false : ip.isMulticastAddress() ^ 1;
    }

    private static LinkProperties getUniqueLocalConfig(byte[] ulp, short subnetId) {
        LinkProperties lp = new LinkProperties();
        lp.addRoute(new RouteInfo(makeUniqueLocalPrefix(ulp, (short) 0, 48), null, null));
        lp.addLinkAddress(new LinkAddress(makeUniqueLocalPrefix(ulp, subnetId, 64).getAddress(), 64));
        lp.setMtu(NetworkConstants.ETHER_MTU);
        return lp;
    }

    private static IpPrefix makeUniqueLocalPrefix(byte[] in6addr, short subnetId, int prefixlen) {
        byte[] bytes = Arrays.copyOf(in6addr, in6addr.length);
        bytes[7] = (byte) (subnetId >> 8);
        bytes[8] = (byte) subnetId;
        return new IpPrefix(bytes, prefixlen);
    }

    private static byte[] generateUniqueLocalPrefix() {
        byte[] ulp = new byte[6];
        new Random().nextBytes(ulp);
        byte[] in6addr = Arrays.copyOf(ulp, 16);
        in6addr[0] = (byte) -3;
        return in6addr;
    }

    private static String toDebugString(NetworkState ns) {
        if (ns == null) {
            return "NetworkState{null}";
        }
        return String.format("NetworkState{%s, %s, %s}", new Object[]{ns.network, ns.networkCapabilities, ns.linkProperties});
    }

    private static void stopIPv6TetheringOn(TetherInterfaceStateMachine sm) {
        sm.sendMessage(TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE, 0, 0, null);
    }
}
