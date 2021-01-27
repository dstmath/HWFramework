package com.huawei.systemmanager.netassistant;

import android.content.Context;
import android.net.NetworkPolicyManager;
import android.util.Slog;

public class NetworkPolicyManagerImpl implements INetworkPolicyManager {
    private static int POLICY_ALLOW = 1;
    private static int POLICY_REJECT = 2;
    private static final String TAG = "NetworkPolicyManagerImpl";
    private static NetworkPolicyManagerImpl sInstance;
    private NetworkPolicyManager mPolicyManager;

    private NetworkPolicyManagerImpl(Context context) {
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    public static synchronized INetworkPolicyManager getInstance(Context context) {
        NetworkPolicyManagerImpl networkPolicyManagerImpl;
        synchronized (NetworkPolicyManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new NetworkPolicyManagerImpl(context);
            }
            networkPolicyManagerImpl = sInstance;
        }
        return networkPolicyManagerImpl;
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public int getUidPolicy(int uid) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null) {
            return networkPolicyManager.getUidPolicy(uid);
        }
        return 0;
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public void setUidPolicy(int uid, int policy) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null) {
            networkPolicyManager.setUidPolicy(uid, policy);
        }
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public int[] getUidsWithPolicy(int policy) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null) {
            return networkPolicyManager.getUidsWithPolicy(policy);
        }
        return new int[0];
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public void setRestrictBackground(boolean restrictBackground) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null) {
            networkPolicyManager.setRestrictBackground(restrictBackground);
        }
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public boolean getRestrictBackground() {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null) {
            return networkPolicyManager.getRestrictBackground();
        }
        return false;
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public void registerListener(INetworkPolicyListenerEx listener) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null && listener != null) {
            networkPolicyManager.registerListener(listener.getINetworkPolicyListener());
        }
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public void unRegisterListener(INetworkPolicyListenerEx listener) {
        NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
        if (networkPolicyManager != null && listener != null) {
            networkPolicyManager.unregisterListener(listener.getINetworkPolicyListener());
        }
    }

    @Override // com.huawei.systemmanager.netassistant.INetworkPolicyManager
    public int getBackgroundPolicy(int type) {
        if (type == POLICY_ALLOW) {
            return 4;
        }
        if (type == POLICY_REJECT) {
            return 1;
        }
        Slog.i(TAG, "type is not equals others");
        return 0;
    }
}
