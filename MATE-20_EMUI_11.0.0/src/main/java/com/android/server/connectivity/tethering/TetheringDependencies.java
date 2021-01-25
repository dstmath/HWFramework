package com.android.server.connectivity.tethering;

import android.content.Context;
import android.net.NetworkRequest;
import android.net.ip.IpServer;
import android.net.util.SharedLog;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import com.android.internal.util.StateMachine;
import com.android.server.connectivity.MockableSystemProperties;
import java.util.ArrayList;

public class TetheringDependencies {
    public OffloadHardwareInterface getOffloadHardwareInterface(Handler h, SharedLog log) {
        return new OffloadHardwareInterface(h, log);
    }

    public UpstreamNetworkMonitor getUpstreamNetworkMonitor(Context ctx, StateMachine target, SharedLog log, int what) {
        return new UpstreamNetworkMonitor(ctx, target, log, what);
    }

    public IPv6TetheringCoordinator getIPv6TetheringCoordinator(ArrayList<IpServer> notifyList, SharedLog log) {
        return new IPv6TetheringCoordinator(notifyList, log);
    }

    public IpServer.Dependencies getIpServerDependencies() {
        return new IpServer.Dependencies();
    }

    public boolean isTetheringSupported() {
        return true;
    }

    public NetworkRequest getDefaultNetworkRequest() {
        return null;
    }

    public EntitlementManager getEntitlementManager(Context ctx, StateMachine target, SharedLog log, int what, MockableSystemProperties systemProperties) {
        return new EntitlementManager(ctx, target, log, what, systemProperties);
    }

    public int getDefaultDataSubscriptionId() {
        return SubscriptionManager.getDefaultDataSubscriptionId();
    }
}
