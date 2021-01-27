package com.android.server.pm.permission;

import android.content.pm.PermissionInfo;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import com.huawei.android.content.pm.PermissionInfoEx;

public class PermissionManagerServiceInternalEx {
    private PermissionManagerServiceInternal mPermissionManagerServiceInternal;

    public static class PermissionCallbackEx {
        private PermissionManagerServiceInternal.PermissionCallback mPermissionCallback;

        public PermissionManagerServiceInternal.PermissionCallback getPermissionCallback() {
            return this.mPermissionCallback;
        }

        public void setPermissionCallback(PermissionManagerServiceInternal.PermissionCallback permissionCallback) {
            this.mPermissionCallback = permissionCallback;
        }

        public void onGidsChanged(int appId, int userId) {
            this.mPermissionCallback.onGidsChanged(appId, userId);
        }

        public void onPermissionChanged() {
            this.mPermissionCallback.onPermissionChanged();
        }

        public void onPermissionGranted(int uid, int userId) {
            this.mPermissionCallback.onPermissionGranted(uid, userId);
        }

        public void onInstallPermissionGranted() {
            this.mPermissionCallback.onInstallPermissionGranted();
        }

        public void onPermissionRevoked(int uid, int userId) {
            this.mPermissionCallback.onPermissionRevoked(uid, userId);
        }

        public void onInstallPermissionRevoked() {
            this.mPermissionCallback.onInstallPermissionRevoked();
        }

        public void onPermissionUpdated(int[] updatedUserIds, boolean sync) {
            this.mPermissionCallback.onPermissionUpdated(updatedUserIds, sync);
        }

        public void onPermissionRemoved() {
            this.mPermissionCallback.onPermissionRemoved();
        }

        public void onInstallPermissionUpdated() {
            this.mPermissionCallback.onInstallPermissionUpdated();
        }
    }

    public PermissionManagerServiceInternal getPermissionManagerServiceInternal() {
        return this.mPermissionManagerServiceInternal;
    }

    public void setPermissionManagerServiceInternal(PermissionManagerServiceInternal permissionManagerServiceInternal) {
        this.mPermissionManagerServiceInternal = permissionManagerServiceInternal;
    }

    public void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, String message) {
        this.mPermissionManagerServiceInternal.enforceCrossUserPermission(callingUid, userId, requireFullPermission, checkShell, message);
    }

    public BasePermissionEx getPermissionTEMP(String permName) {
        BasePermission permission = this.mPermissionManagerServiceInternal.getPermissionTEMP(permName);
        if (permission == null) {
            return null;
        }
        BasePermissionEx basePermissionEx = new BasePermissionEx();
        basePermissionEx.setBasePermission(permission);
        return basePermissionEx;
    }

    public int getPermissionFlags(String permName, String packageName, int callingUid, int userId) {
        return this.mPermissionManagerServiceInternal.getPermissionFlags(permName, packageName, callingUid, userId);
    }

    public PermissionInfoEx getPermissionInfo(String permName, String packageName, int flags, int callingUid) {
        PermissionInfo permissionInfo = this.mPermissionManagerServiceInternal.getPermissionInfo(permName, packageName, flags, callingUid);
        PermissionInfoEx infoEx = new PermissionInfoEx();
        infoEx.setPermissionInfo(permissionInfo);
        return infoEx;
    }

    public int checkPermission(String permName, String packageName, int callingUid, int userId) {
        return this.mPermissionManagerServiceInternal.checkPermission(permName, packageName, callingUid, userId);
    }

    public void updatePermissionFlags(String permName, String packageName, int flagMask, int flagValues, int callingUid, int userId, boolean overridePolicy, PermissionCallbackEx callbackEx) {
        PermissionManagerServiceInternal.PermissionCallback callback = null;
        if (callbackEx != null) {
            callback = callbackEx.getPermissionCallback();
        }
        this.mPermissionManagerServiceInternal.updatePermissionFlags(permName, packageName, flagMask, flagValues, callingUid, userId, overridePolicy, callback);
    }
}
