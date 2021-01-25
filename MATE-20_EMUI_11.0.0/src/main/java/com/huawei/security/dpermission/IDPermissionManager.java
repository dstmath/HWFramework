package com.huawei.security.dpermission;

public interface IDPermissionManager {
    int allocateDuid(String str, int i);

    int checkDPermission(int i, String str);

    int notifyPermissionChanged(int i, String str, int i2);

    int notifyUidPermissionChanged(int i);

    int queryDuid(String str, int i);
}
