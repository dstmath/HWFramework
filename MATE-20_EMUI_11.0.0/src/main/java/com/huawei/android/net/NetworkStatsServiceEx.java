package com.huawei.android.net;

import android.net.INetworkStatsService;
import android.os.RemoteException;
import android.os.ServiceManager;

public class NetworkStatsServiceEx {
    private INetworkStatsService mStatsService;

    private NetworkStatsServiceEx(INetworkStatsService statsService) {
        this.mStatsService = statsService;
    }

    public static NetworkStatsServiceEx getNetworkStatsService() {
        return new NetworkStatsServiceEx(INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")));
    }

    public NetworkStatsSessionEx openSession() throws RemoteException {
        INetworkStatsService iNetworkStatsService = this.mStatsService;
        if (iNetworkStatsService == null) {
            return null;
        }
        return new NetworkStatsSessionEx(iNetworkStatsService.openSession());
    }

    public void forceUpdate() throws RemoteException {
        INetworkStatsService iNetworkStatsService = this.mStatsService;
        if (iNetworkStatsService != null) {
            iNetworkStatsService.forceUpdate();
        }
    }
}
