package com.huawei.android.net;

import android.content.Context;
import android.net.INetworkStatsService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import android.util.Slog;
import com.huawei.android.net.IHwNetworkStats;

public class HwNetworkStats {
    private static final Singleton<IHwNetworkStats> IHWNETWORK_STATS_SINGLETON = new Singleton<IHwNetworkStats>() {
        /* class com.huawei.android.net.HwNetworkStats.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwNetworkStats create() {
            try {
                return IHwNetworkStats.Stub.asInterface(INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE)).getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwNetworkStats";

    private HwNetworkStats() {
    }

    public static IHwNetworkStats getService() {
        return IHWNETWORK_STATS_SINGLETON.get();
    }

    public static boolean setAlertPeriodType(int period) {
        if (getService() == null) {
            Slog.e(TAG, "The NetworkStatsService has been died.");
            return false;
        }
        try {
            getService().setAlertPeriodType(period);
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "Set alert period failed with RemoteException.");
            return false;
        }
    }

    public static boolean setMpTrafficEnabled(boolean enable) {
        if (getService() == null) {
            Slog.e(TAG, "The NetworkStatsService has been died.");
            return false;
        }
        try {
            return getService().setMpTrafficEnabled(enable);
        } catch (RemoteException e) {
            Slog.e(TAG, "setMpTrafficEnabled failed with RemoteException.");
            return false;
        }
    }
}
