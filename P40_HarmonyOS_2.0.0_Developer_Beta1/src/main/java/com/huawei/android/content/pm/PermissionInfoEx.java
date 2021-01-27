package com.huawei.android.content.pm;

import android.content.pm.PermissionInfo;
import com.huawei.annotation.HwSystemApi;

public class PermissionInfoEx {
    private PermissionInfo permissionInfo;

    public PermissionInfo getPermissionInfo() {
        return this.permissionInfo;
    }

    public void setPermissionInfo(PermissionInfo permissionInfo2) {
        this.permissionInfo = permissionInfo2;
    }

    public static final int getHardRestrictedFlag() {
        return 4;
    }

    @HwSystemApi
    public int getProtection() {
        return this.permissionInfo.getProtection();
    }

    @HwSystemApi
    public String getName() {
        return this.permissionInfo.name;
    }
}
