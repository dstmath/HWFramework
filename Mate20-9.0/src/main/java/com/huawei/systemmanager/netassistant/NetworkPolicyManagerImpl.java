package com.huawei.systemmanager.netassistant;

import android.content.Context;
import android.net.NetworkPolicyManager;

public class NetworkPolicyManagerImpl implements INetworkPolicyManager {
    private static int POLICY_ALLOW = 1;
    private static int POLICY_REJECT = 2;
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

    public int getUidPolicy(int uid) {
        if (this.mPolicyManager != null) {
            return this.mPolicyManager.getUidPolicy(uid);
        }
        return 0;
    }

    public void setUidPolicy(int uid, int policy) {
        if (this.mPolicyManager != null) {
            this.mPolicyManager.setUidPolicy(uid, policy);
        }
    }

    public int[] getUidsWithPolicy(int policy) {
        if (this.mPolicyManager != null) {
            return this.mPolicyManager.getUidsWithPolicy(policy);
        }
        return new int[0];
    }

    public void setRestrictBackground(boolean restrictBackground) {
        if (this.mPolicyManager != null) {
            this.mPolicyManager.setRestrictBackground(restrictBackground);
        }
    }

    public boolean getRestrictBackground() {
        if (this.mPolicyManager != null) {
            return this.mPolicyManager.getRestrictBackground();
        }
        return false;
    }

    public void registerListener(INetworkPolicyListenerEx listener) {
        if (this.mPolicyManager != null && listener != null) {
            this.mPolicyManager.registerListener(listener.getINetworkPolicyListener());
        }
    }

    public void unRegisterListener(INetworkPolicyListenerEx listener) {
        if (this.mPolicyManager != null && listener != null) {
            this.mPolicyManager.unregisterListener(listener.getINetworkPolicyListener());
        }
    }

    public int getBackgroundPolicy(int type) {
        if (POLICY_ALLOW == type) {
            return 4;
        }
        if (POLICY_REJECT == type) {
            return 1;
        }
        return 0;
    }
}
