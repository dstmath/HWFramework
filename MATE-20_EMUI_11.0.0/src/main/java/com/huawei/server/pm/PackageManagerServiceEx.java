package com.huawei.server.pm;

import android.os.ServiceManager;
import com.android.server.pm.PackageManagerService;

public class PackageManagerServiceEx {
    private PackageManagerService mPms;

    public PackageManagerServiceEx() {
        Object obj = ServiceManager.getService("package");
        if (obj instanceof PackageManagerService) {
            this.mPms = (PackageManagerService) obj;
        } else {
            this.mPms = null;
        }
    }

    public String getNameForUid(int uid) {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            return null;
        }
        return packageManagerService.getNameForUid(uid);
    }

    public boolean isPmsNull() {
        return this.mPms == null;
    }
}
