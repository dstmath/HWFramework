package com.huawei.server.pm;

import android.content.pm.PackageInfo;
import android.os.ServiceManager;
import android.util.ArrayMap;
import com.android.server.pm.PackageManagerService;
import java.util.List;

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

    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            return null;
        }
        return packageManagerService.getPackageInfo(packageName, flags, userId);
    }

    public List<String> getAppsUseSideList() {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService == null) {
            return null;
        }
        return packageManagerService.getAppsUseSideList();
    }

    public void updateAppsUseSideWhitelist(ArrayMap<String, String> compressApps, ArrayMap<String, String> extendApps) {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService != null) {
            packageManagerService.updateAppsUseSideWhitelist(compressApps, extendApps);
        }
    }
}
