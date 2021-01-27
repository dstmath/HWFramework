package com.android.server.pm.permission;

public class PermissionManagerServiceEx {
    private PermissionManagerService permissionManagerService;

    public PermissionManagerService getPermissionManagerService() {
        return this.permissionManagerService;
    }

    public void setPermissionManagerService(PermissionManagerService permissionManagerService2) {
        this.permissionManagerService = permissionManagerService2;
    }
}
