package com.huawei.systemmanager.netassistant;

import android.content.Context;
import android.net.HwNetworkPolicyManager;

class HwNetworkPolicyManagerImpl implements IHwNetworkPolicyManager {
    private static HwNetworkPolicyManagerImpl sInstance;
    private HwNetworkPolicyManager mPolicyManager;

    public HwNetworkPolicyManagerImpl(Context context) {
        this.mPolicyManager = HwNetworkPolicyManager.from(context);
    }

    public static synchronized IHwNetworkPolicyManager getInstance(Context context) {
        synchronized (HwNetworkPolicyManagerImpl.class) {
            HwNetworkPolicyManagerImpl tmp = new HwNetworkPolicyManagerImpl(context);
            if (tmp.mPolicyManager == null) {
                return null;
            }
            return tmp;
        }
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkPolicyManager
    public void setHwUidPolicy(int uid, int policy) {
        HwNetworkPolicyManager hwNetworkPolicyManager = this.mPolicyManager;
        if (hwNetworkPolicyManager != null) {
            hwNetworkPolicyManager.setHwUidPolicy(uid, policy);
        }
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkPolicyManager
    public int getHwUidPolicy(int uid) {
        HwNetworkPolicyManager hwNetworkPolicyManager = this.mPolicyManager;
        if (hwNetworkPolicyManager != null) {
            return hwNetworkPolicyManager.getHwUidPolicy(uid);
        }
        return 0;
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkPolicyManager
    public void removeHwUidPolicy(int uid, int policy) {
        HwNetworkPolicyManager hwNetworkPolicyManager = this.mPolicyManager;
        if (hwNetworkPolicyManager != null) {
            hwNetworkPolicyManager.removeHwUidPolicy(uid, policy);
        }
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkPolicyManager
    public void addHwUidPolicy(int uid, int policy) {
        HwNetworkPolicyManager hwNetworkPolicyManager = this.mPolicyManager;
        if (hwNetworkPolicyManager != null) {
            hwNetworkPolicyManager.addHwUidPolicy(uid, policy);
        }
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkPolicyManager
    public void forceUpdatePolicy(boolean isRoaming) {
        HwNetworkPolicyManager hwNetworkPolicyManager = this.mPolicyManager;
        if (hwNetworkPolicyManager != null) {
            hwNetworkPolicyManager.forceUpdatePolicy(isRoaming);
        }
    }
}
