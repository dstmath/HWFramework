package com.android.server.connectivity.tethering;

import android.content.Context;
import android.net.INetd;
import android.net.ip.RouterAdvertisementDaemon;
import android.net.util.InterfaceParams;
import android.net.util.NetdService;
import android.net.util.SharedLog;
import android.os.Handler;
import com.android.internal.util.StateMachine;
import java.util.ArrayList;

public class TetheringDependencies {
    public OffloadHardwareInterface getOffloadHardwareInterface(Handler h, SharedLog log) {
        return new OffloadHardwareInterface(h, log);
    }

    public UpstreamNetworkMonitor getUpstreamNetworkMonitor(Context ctx, StateMachine target, SharedLog log, int what) {
        return new UpstreamNetworkMonitor(ctx, target, log, what);
    }

    public IPv6TetheringCoordinator getIPv6TetheringCoordinator(ArrayList<TetherInterfaceStateMachine> notifyList, SharedLog log) {
        return new IPv6TetheringCoordinator(notifyList, log);
    }

    public RouterAdvertisementDaemon getRouterAdvertisementDaemon(InterfaceParams ifParams) {
        return new RouterAdvertisementDaemon(ifParams);
    }

    public InterfaceParams getInterfaceParams(String ifName) {
        return InterfaceParams.getByName(ifName);
    }

    public INetd getNetdService() {
        return NetdService.getInstance();
    }

    public boolean isTetheringSupported() {
        return true;
    }
}
