package com.huawei.android.net;

import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.os.RemoteException;

public class NetworkStatsSessionEx {
    private INetworkStatsSession mNetworkStatsSession;

    public NetworkStatsSessionEx(INetworkStatsSession networkStatsSession) {
        this.mNetworkStatsSession = networkStatsSession;
    }

    public void close() throws RemoteException {
        if (this.mNetworkStatsSession != null) {
            this.mNetworkStatsSession.close();
        }
    }

    public NetworkStatsEx getSummaryForNetwork(NetworkTemplateEx template, long start, long end) throws RemoteException {
        NetworkStats networkStats = null;
        if (this.mNetworkStatsSession != null) {
            networkStats = this.mNetworkStatsSession.getSummaryForNetwork(template.getNetworkTemplate(), start, end);
        }
        if (networkStats == null) {
            return null;
        }
        return new NetworkStatsEx(networkStats);
    }

    public NetworkStatsEx getSummaryForAllUid(NetworkTemplateEx template, long start, long end, boolean includeTags) throws RemoteException {
        NetworkStats networkStats = null;
        if (this.mNetworkStatsSession != null) {
            networkStats = this.mNetworkStatsSession.getSummaryForAllUid(template.getNetworkTemplate(), start, end, includeTags);
        }
        if (networkStats == null) {
            return null;
        }
        return new NetworkStatsEx(networkStats);
    }

    public NetworkStatsEx getSummaryForAllPid(NetworkTemplateEx template, long start, long end) throws RemoteException {
        NetworkStats networkStats = null;
        if (this.mNetworkStatsSession != null) {
            networkStats = this.mNetworkStatsSession.getSummaryForAllPid(template.getNetworkTemplate(), start, end);
        }
        if (networkStats == null) {
            return null;
        }
        return new NetworkStatsEx(networkStats);
    }
}
