package com.android.server.net;

import android.net.INetworkManagementEventObserver.Stub;
import android.net.LinkAddress;
import android.net.RouteInfo;

public class BaseNetworkObserver extends Stub {
    public void interfaceStatusChanged(String iface, boolean up) {
    }

    public void interfaceRemoved(String iface) {
    }

    public void addressUpdated(String iface, LinkAddress address) {
    }

    public void addressRemoved(String iface, LinkAddress address) {
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
    }

    public void interfaceAdded(String iface) {
    }

    public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
    }

    public void limitReached(String limitName, String iface) {
    }

    public void interfaceDnsServerInfo(String iface, long lifetime, String[] servers) {
    }

    public void routeUpdated(RouteInfo route) {
    }

    public void routeRemoved(RouteInfo route) {
    }
}
