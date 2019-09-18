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
    private final LinkedList<Downstream> mActiveDownstreams = new LinkedList<>();
    private final SharedLog mLog;
    private short mNextSubnetId = 0;
    private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
    private final byte[] mUniqueLocalPrefix = generateUniqueLocalPrefix();
    private NetworkState mUpstreamNetworkState;

    private static class Downstream {
        public final int mode;
        public final short subnetId;
        public final TetherInterfaceStateMachine tism;

        Downstream(TetherInterfaceStateMachine tism2, int mode2, short subnetId2) {
            this.tism = tism2;
            this.mode = mode2;
            this.subnetId = subnetId2;
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
            this.mNextSubnetId = 0;
        }
    }

    public void updateUpstreamNetworkState(NetworkState ns) {
        if (TetheringInterfaceUtils.getIPv6Interface(ns) == null) {
            stopIPv6TetheringOnAllInterfaces();
            setUpstreamNetworkState(null);
            return;
        }
        if (this.mUpstreamNetworkState != null && !ns.network.equals(this.mUpstreamNetworkState.network)) {
            stopIPv6TetheringOnAllInterfaces();
        }
        setUpstreamNetworkState(ns);
        updateIPv6TetheringInterfaces();
    }

    private void stopIPv6TetheringOnAllInterfaces() {
        Iterator<TetherInterfaceStateMachine> it = this.mNotifyList.iterator();
        while (it.hasNext()) {
            stopIPv6TetheringOn(it.next());
        }
    }

    private void setUpstreamNetworkState(NetworkState ns) {
        if (ns == null) {
            this.mUpstreamNetworkState = null;
        } else {
            NetworkState networkState = new NetworkState(null, new LinkProperties(ns.linkProperties), new NetworkCapabilities(ns.networkCapabilities), new Network(ns.network), null, null);
            this.mUpstreamNetworkState = networkState;
        }
        SharedLog sharedLog = this.mLog;
        sharedLog.log("setUpstreamNetworkState: " + toDebugString(this.mUpstreamNetworkState));
    }

    private void updateIPv6TetheringInterfaces() {
        Iterator<TetherInterfaceStateMachine> it = this.mNotifyList.iterator();
        if (it.hasNext()) {
            TetherInterfaceStateMachine sm = it.next();
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
        Downstream currentActive = this.mActiveDownstreams.peek();
        if (currentActive != null && currentActive.tism == sm) {
            LinkProperties lp = getIPv6OnlyLinkProperties(this.mUpstreamNetworkState.linkProperties);
            if (lp.hasIPv6DefaultRoute()) {
                return lp;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Downstream findDownstream(TetherInterfaceStateMachine tism) {
        Iterator it = this.mActiveDownstreams.iterator();
        while (it.hasNext()) {
            Downstream ds = (Downstream) it.next();
            if (ds.tism == tism) {
                return ds;
            }
        }
        return null;
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
        return (ip instanceof Inet6Address) && !ip.isAnyLocalAddress() && !ip.isLoopbackAddress() && !ip.isLinkLocalAddress() && !ip.isSiteLocalAddress() && !ip.isMulticastAddress();
    }

    private static LinkProperties getUniqueLocalConfig(byte[] ulp, short subnetId) {
        LinkProperties lp = new LinkProperties();
        lp.addRoute(new RouteInfo(makeUniqueLocalPrefix(ulp, 0, 48), null, null));
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
        in6addr[0] = -3;
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
