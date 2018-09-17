package com.huawei.android.net;

import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;

public class NetworkStatsServiceEx {
    private INetworkStatsService mStatsService;

    private NetworkStatsServiceEx(INetworkStatsService statsService) {
        this.mStatsService = statsService;
    }

    public NetworkStatsSessionEx openSession() throws RemoteException {
        return this.mStatsService == null ? null : new NetworkStatsSessionEx(this.mStatsService.openSession());
    }

    public static NetworkStatsServiceEx getNetworkStatsService() {
        return new NetworkStatsServiceEx(Stub.asInterface(ServiceManager.getService("netstats")));
    }
}
