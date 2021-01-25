package com.huawei.security.dpermission;

import android.support.annotation.RequiresPermission;

public class DistributedPermissionManager {
    private static final Object LOCK = new Object();
    public static final String PERMISSION_MANAGE_DISTRIBUTED_PERMISSION = "com.huawei.permission.MANAGE_DISTRIBUTED_PERMISSION";
    private static volatile DistributedPermissionManager sInstance;
    private DistributedPermissionManagerImpl mImpl = DistributedPermissionManagerImpl.getDefault();

    private DistributedPermissionManager() {
    }

    public static DistributedPermissionManager getDefault() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DistributedPermissionManager();
                }
            }
        }
        return sInstance;
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int allocateDuid(String deviceId, int rUid) {
        return this.mImpl.allocateDuid(deviceId, rUid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int queryDuid(String deviceId, int rUid) {
        return this.mImpl.queryDuid(deviceId, rUid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int notifyDeviceStatusChanged(String deviceId, int status) {
        return this.mImpl.notifyDeviceStatusChanged(deviceId, status);
    }
}
