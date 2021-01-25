package com.huawei.security.dpermission;

import android.view.textclassifier.Log;

public class DefaultDPermissionManagerImpl implements IDPermissionManager {
    private static final String TAG = DefaultDPermissionManagerImpl.class.getSimpleName();

    public DefaultDPermissionManagerImpl() {
        Log.w(TAG, "DPermissionManager default impl called!");
    }

    @Override // com.huawei.security.dpermission.IDPermissionManager
    public int allocateDuid(String deviceId, int rUid) {
        return 0;
    }

    @Override // com.huawei.security.dpermission.IDPermissionManager
    public int queryDuid(String deviceId, int rUid) {
        return 0;
    }

    @Override // com.huawei.security.dpermission.IDPermissionManager
    public int checkDPermission(int dUid, String permissionName) {
        return 0;
    }

    @Override // com.huawei.security.dpermission.IDPermissionManager
    public int notifyUidPermissionChanged(int uid) {
        return 0;
    }

    @Override // com.huawei.security.dpermission.IDPermissionManager
    public int notifyPermissionChanged(int uid, String permissionName, int status) {
        return 0;
    }
}
