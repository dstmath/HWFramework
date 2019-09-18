package com.huawei.android.net;

import android.net.INetworkStatsService;
import android.os.RemoteException;
import android.os.ServiceManager;

public class NetworkStatsServiceEx {
    private INetworkStatsService mStatsService;

    private NetworkStatsServiceEx(INetworkStatsService statsService) {
        this.mStatsService = statsService;
    }

    public NetworkStatsSessionEx openSession() throws RemoteException {
        if (this.mStatsService == null) {
            return null;
        }
        return new NetworkStatsSessionEx(this.mStatsService.openSession());
    }

    public void forceUpdate() throws RemoteException {
        if (this.mStatsService != null) {
            this.mStatsService.forceUpdate();
        }
    }

    public static NetworkStatsServiceEx getNetworkStatsService() {
        return new NetworkStatsServiceEx(INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")));
    }
}
