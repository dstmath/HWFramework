package com.huawei.android.net;

import android.net.INetworkStatsService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import com.huawei.android.net.IHwNetworkStats;

public class HwNetworkStats {
    private static final Singleton<IHwNetworkStats> IHwNetworkStatsSingleton = new Singleton<IHwNetworkStats>() {
        /* access modifiers changed from: protected */
        public IHwNetworkStats create() {
            try {
                return IHwNetworkStats.Stub.asInterface(INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")).getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwNetworkStats";

    public static IHwNetworkStats getService() {
        return (IHwNetworkStats) IHwNetworkStatsSingleton.get();
    }
}
