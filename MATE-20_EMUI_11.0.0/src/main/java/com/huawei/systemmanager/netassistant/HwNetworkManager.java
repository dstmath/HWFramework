package com.huawei.systemmanager.netassistant;

import android.content.Context;
import android.net.NetworkStats;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.android.net.NetworkStatsEx;

public class HwNetworkManager {
    private static final int STATS_PER_UID = 1;

    public static IHwNetworkPolicyManager getHwNetworkPolicyManager(Context context) {
        return HwNetworkPolicyManagerImpl.getInstance(context);
    }

    public static INetworkPolicyManager getNetworkPolicyManager(Context context) {
        return NetworkPolicyManagerImpl.getInstance(context);
    }

    public static NetworkStatsEx getNetworkStatsTethering() throws RemoteException {
        INetworkManagementService networkManagement = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        NetworkStats networkStats = null;
        if (networkManagement != null) {
            networkStats = networkManagement.getNetworkStatsTethering(1);
        }
        if (networkStats == null) {
            return null;
        }
        return new NetworkStatsEx(networkStats);
    }
}
