package com.huawei.android.net;

import android.content.Context;
import android.net.NetworkPolicyManager;

public class NetworkPolicyManagerEx {
    public static void registerListener(Context context, INetworkPolicyListenerEx listenerEx) {
        NetworkPolicyManager networkPolicyManager = (NetworkPolicyManager) context.getSystemService(NetworkPolicyManager.class);
        if (networkPolicyManager != null && listenerEx != null) {
            networkPolicyManager.registerListener(listenerEx.getListener());
        }
    }
}
