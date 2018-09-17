package com.android.server.net;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import java.util.Arrays;

public class NetlinkTracker extends BaseNetworkObserver {
    private static final boolean DBG = false;
    private final String TAG;
    private final Callback mCallback;
    private DnsServerRepository mDnsServerRepository;
    private final String mInterfaceName;
    private final LinkProperties mLinkProperties = new LinkProperties();

    public interface Callback {
        void update();
    }

    public NetlinkTracker(String iface, Callback callback) {
        this.TAG = "NetlinkTracker/" + iface;
        this.mInterfaceName = iface;
        this.mCallback = callback;
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
        this.mDnsServerRepository = new DnsServerRepository();
    }

    private void maybeLog(String operation, String iface, LinkAddress address) {
    }

    private void maybeLog(String operation, Object o) {
    }

    public void interfaceRemoved(String iface) {
        maybeLog("interfaceRemoved", iface);
        if (this.mInterfaceName.equals(iface)) {
            clearLinkProperties();
            this.mCallback.update();
        }
    }

    public void addressUpdated(String iface, LinkAddress address) {
        if (this.mInterfaceName.equals(iface)) {
            boolean changed;
            maybeLog("addressUpdated", iface, address);
            synchronized (this) {
                changed = this.mLinkProperties.addLinkAddress(address);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    public void addressRemoved(String iface, LinkAddress address) {
        if (this.mInterfaceName.equals(iface)) {
            boolean changed;
            maybeLog("addressRemoved", iface, address);
            synchronized (this) {
                changed = this.mLinkProperties.removeLinkAddress(address);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    public void routeUpdated(RouteInfo route) {
        if (this.mInterfaceName.equals(route.getInterface())) {
            boolean changed;
            maybeLog("routeUpdated", route);
            synchronized (this) {
                changed = this.mLinkProperties.addRoute(route);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    public void routeRemoved(RouteInfo route) {
        if (this.mInterfaceName.equals(route.getInterface())) {
            boolean changed;
            maybeLog("routeRemoved", route);
            synchronized (this) {
                changed = this.mLinkProperties.removeRoute(route);
            }
            if (changed) {
                this.mCallback.update();
            }
        }
    }

    public void interfaceDnsServerInfo(String iface, long lifetime, String[] addresses) {
        if (this.mInterfaceName.equals(iface)) {
            maybeLog("interfaceDnsServerInfo", Arrays.toString(addresses));
            if (this.mDnsServerRepository.addServers(lifetime, addresses)) {
                synchronized (this) {
                    this.mDnsServerRepository.setDnsServersOn(this.mLinkProperties);
                }
                this.mCallback.update();
            }
        }
    }

    public synchronized LinkProperties getLinkProperties() {
        return new LinkProperties(this.mLinkProperties);
    }

    public synchronized void clearLinkProperties() {
        this.mDnsServerRepository = new DnsServerRepository();
        this.mLinkProperties.clear();
        this.mLinkProperties.setInterfaceName(this.mInterfaceName);
    }
}
