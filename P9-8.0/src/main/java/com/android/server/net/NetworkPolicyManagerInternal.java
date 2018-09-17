package com.android.server.net;

public abstract class NetworkPolicyManagerInternal {
    public abstract boolean isUidNetworkingBlocked(int i, String str);

    public abstract boolean isUidRestrictedOnMeteredNetworks(int i);

    public abstract void resetUserState(int i);
}
