package com.android.server.net;

import android.net.INetworkManagementEventObserver;
import android.net.LinkAddress;
import android.net.RouteInfo;

public class BaseNetworkObserver extends INetworkManagementEventObserver.Stub {
    @Override // android.net.INetworkManagementEventObserver
    public void interfaceStatusChanged(String iface, boolean up) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void interfaceRemoved(String iface) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void addressUpdated(String iface, LinkAddress address) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void addressRemoved(String iface, LinkAddress address) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void interfaceLinkStateChanged(String iface, boolean up) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void interfaceAdded(String iface) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void limitReached(String limitName, String iface) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void interfaceDnsServerInfo(String iface, long lifetime, String[] servers) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void routeUpdated(RouteInfo route) {
    }

    @Override // android.net.INetworkManagementEventObserver
    public void routeRemoved(RouteInfo route) {
    }
}
