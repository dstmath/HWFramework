package com.huawei.systemmanager.netassistant;

public interface IHwNetworkPolicyManager {
    public static final int POLICY_HW_DEFAULT = 0;
    public static final int POLICY_HW_RESTRICT_MOBILE = 1;
    public static final int POLICY_HW_RESTRICT_ROAMING_MOBILE = 4;
    public static final int POLICY_HW_RESTRICT_WIFI = 2;

    void addHwUidPolicy(int i, int i2);

    void forceUpdatePolicy(boolean z);

    int getHwUidPolicy(int i);

    void removeHwUidPolicy(int i, int i2);

    void setHwUidPolicy(int i, int i2);
}
