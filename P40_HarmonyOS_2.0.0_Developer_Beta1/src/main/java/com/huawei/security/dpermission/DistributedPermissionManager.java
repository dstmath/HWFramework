package com.huawei.security.dpermission;

import android.support.annotation.RequiresPermission;
import com.huawei.security.dpermission.permissionusingremind.OnPermissionUsingReminder;

public class DistributedPermissionManager {
    private static final Object LOCK = new Object();
    public static final String PERMISSION_MANAGE_DISTRIBUTED_PERMISSION = "com.huawei.permission.MANAGE_DISTRIBUTED_PERMISSION";
    public static final String PERMISSION_USED_STATS = "ohos.permission.PERMISSION_USED_STATS";
    public static final String PERMISSION_USING_REMIND = "ohos.permission.PERMISSION_USING_REMIND";
    private static volatile DistributedPermissionManager sInstance;
    private DistributedPermissionManagerImpl mImpl = DistributedPermissionManagerImpl.getDefault();

    public interface IRequestPermissionsResult {
        void onCancel(String str, String[] strArr);

        void onResult(String str, String[] strArr, int[] iArr);

        void onTimeOut(String str, String[] strArr);
    }

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
    public int allocateDuid(String nodeId, int rUid) {
        return this.mImpl.allocateDuid(nodeId, rUid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int queryDuid(String nodeId, int rUid) {
        return this.mImpl.queryDuid(nodeId, rUid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int notifyDeviceStatusChanged(String nodeId, int status) {
        return this.mImpl.notifyDeviceStatusChanged(nodeId, status);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public boolean isTargetDevice(String nodeId, int uid) {
        return this.mImpl.isTargetDevice(nodeId, uid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public void addTargetDevice(String nodeId, int uid) {
        this.mImpl.addTargetDevice(nodeId, uid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int notifySyncPermission(String nodeId, int uid, String packageName) {
        return this.mImpl.notifySyncPermission(nodeId, uid, packageName);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public int waitDuidReady(String nodeId, int rUid, int timeout) {
        return this.mImpl.waitDuidReady(nodeId, rUid, timeout);
    }

    public int verifyPermissionFromRemote(String permission, String nodeId, String appIdInfo) {
        return this.mImpl.verifyPermissionFromRemote(permission, nodeId, appIdInfo);
    }

    public int verifySelfPermissionFromRemote(String permission, String nodeId) {
        return this.mImpl.verifySelfPermissionFromRemote(permission, nodeId);
    }

    public boolean canRequestPermissionFromRemote(String permission, String nodeId) {
        return this.mImpl.canRequestPermissionFromRemote(permission, nodeId);
    }

    public void requestPermissionsFromRemote(String[] permissions, IRequestPermissionsResult callback, String nodeId, String bundleName, int reasonResId) {
        this.mImpl.requestPermissionsFromRemote(permissions, callback, nodeId, bundleName, reasonResId);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public void grantSensitivePermissionToRemoteApp(String permission, String nodeId, int ruid) {
        this.mImpl.grantSensitivePermissionToRemoteApp(permission, nodeId, ruid);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public String processZ2aMessage(String command, String payload) {
        return this.mImpl.processZ2aMessage(command, payload);
    }

    public int verifyPermissionAndState(String permissionName, String appIdInfo) {
        return this.mImpl.verifyPermissionAndState(permissionName, appIdInfo);
    }

    @RequiresPermission(PERMISSION_USED_STATS)
    public void addPermissionRecord(String permissionName, String deviceId, int uid, int successCount, int failCount) {
        this.mImpl.addPermissionRecord(permissionName, deviceId, uid, successCount, failCount);
    }

    @RequiresPermission(PERMISSION_USED_STATS)
    public String getPermissionRecord(String data) {
        return this.mImpl.getPermissionRecord(data);
    }

    @RequiresPermission(PERMISSION_USED_STATS)
    public void getPermissionRecordAsync(String data, IPermissionRecordQueryCallback callback) {
        this.mImpl.getPermissionRecordAsync(data, callback);
    }

    @RequiresPermission(PERMISSION_USING_REMIND)
    public int registerOnPermissionUsingReminder(OnPermissionUsingReminder reminder) {
        return this.mImpl.registerOnPermissionUsingReminder(reminder);
    }

    @RequiresPermission(PERMISSION_USING_REMIND)
    public int unregisterOnPermissionUsingReminder(OnPermissionUsingReminder reminder) {
        return this.mImpl.unregisterOnPermissionUsingReminder(reminder);
    }

    @RequiresPermission(PERMISSION_USING_REMIND)
    public void startUsingPermission(String permissionName, String appIdInfo) {
        this.mImpl.startUsingPermission(permissionName, appIdInfo);
    }

    @RequiresPermission(PERMISSION_USING_REMIND)
    public void stopUsingPermission(String permissionName, String appIdInfo) {
        this.mImpl.stopUsingPermission(permissionName, appIdInfo);
    }

    @RequiresPermission(PERMISSION_USING_REMIND)
    public int checkDPermissionAndStartUsing(String permissionName, String appIdInfo) {
        return this.mImpl.checkDPermissionAndStartUsing(permissionName, appIdInfo);
    }

    @RequiresPermission(PERMISSION_USED_STATS)
    public int checkDPermissionAndUse(String permissionName, String appIdInfo) {
        return this.mImpl.checkDPermissionAndUse(permissionName, appIdInfo);
    }

    public int checkPermission(String permissionName, String nodeId, int pid, int uid) {
        return this.mImpl.checkPermission(permissionName, nodeId, pid, uid);
    }

    @RequiresPermission(PERMISSION_USED_STATS)
    public int postPermissionEvent(String event) {
        return this.mImpl.postPermissionEvent(event);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public String getPermissionUsagesInfo(String packageName, String[] permissions) {
        return this.mImpl.getPermissionUsagesInfo(packageName, permissions);
    }

    @RequiresPermission(PERMISSION_MANAGE_DISTRIBUTED_PERMISSION)
    public String getBundleLabelInfo(int dUid) {
        return this.mImpl.getBundleLabelInfo(dUid);
    }
}
