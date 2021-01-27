package com.huawei.systemmanager.netassistant;

public interface INetworkPolicyManager {
    int getBackgroundPolicy(int i);

    boolean getRestrictBackground();

    int getUidPolicy(int i);

    int[] getUidsWithPolicy(int i);

    void registerListener(INetworkPolicyListenerEx iNetworkPolicyListenerEx);

    void setRestrictBackground(boolean z);

    void setUidPolicy(int i, int i2);

    void unRegisterListener(INetworkPolicyListenerEx iNetworkPolicyListenerEx);
}
