package com.android.server.pm.permission;

public class PermissionsStateEx {
    private PermissionsState mPermissionState;

    public PermissionsState getPermissionState() {
        return this.mPermissionState;
    }

    public void setPermissionState(PermissionsState permissionState) {
        this.mPermissionState = permissionState;
    }

    public boolean hasInstallPermission(String name) {
        return this.mPermissionState.hasInstallPermission(name);
    }

    public int revokeInstallPermission(BasePermissionEx permission) {
        return this.mPermissionState.revokeInstallPermission(permission.getBasePermission());
    }
}
