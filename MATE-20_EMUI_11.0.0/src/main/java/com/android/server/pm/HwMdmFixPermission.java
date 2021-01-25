package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PermissionInfo;
import android.hdm.HwDeviceManager;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.pm.permission.PermissionManagerServiceInternal;
import java.util.List;

public class HwMdmFixPermission {
    private static final HwMdmFixPermission INSTANCE = new HwMdmFixPermission();
    private static final String TAG = "HwMdmFixPermission";

    private HwMdmFixPermission() {
    }

    public static HwMdmFixPermission getInstance() {
        return INSTANCE;
    }

    public void fixMdmRuntimePermission(String packageName, String permName, IHwPackageManagerServiceExInner pmsEx, int flag) {
        if (flag == -17) {
            Slog.i(TAG, "no need to update the permission flag, perm name is " + permName);
            return;
        }
        List<String> fixApps = HwDeviceManager.getList(56);
        if (fixApps != null && !fixApps.isEmpty()) {
            if (fixApps.contains(packageName)) {
                IHwPackageManagerInner pmsInner = pmsEx.getIPmsInner();
                if (isSysComponentOrPersistentPlatformSignedPrivApp(packageName, pmsInner)) {
                    Slog.i(TAG, "package is isSysComponentOrPersistentPlatformSignedPrivApp : " + packageName);
                    return;
                }
                PermissionManagerServiceInternal permsInner = pmsInner.getPermissionManager();
                if (permsInner == null) {
                    Slog.e(TAG, "permission manager inner service is null");
                    return;
                }
                int oldFlags = permsInner.getPermissionFlags(permName, packageName, 1000, 0);
                if ((oldFlags & 16) != 0) {
                    Slog.i(TAG, "the permission is already fixed, perm name is " + permName);
                    return;
                }
                PermissionInfo permInfo = permsInner.getPermissionInfo(permName, packageName, 0, 1000);
                if (permInfo != null) {
                    if (permInfo.getProtection() == 1) {
                        if (permsInner.checkPermission(permName, packageName, 1000, 0) == -1) {
                            Slog.i(TAG, permName + " is denied");
                            return;
                        } else if ((oldFlags & 65536) != 0) {
                            Slog.i(TAG, permName + "is FLAG_PERMISSION_ONE_TIME");
                            return;
                        } else {
                            permsInner.updatePermissionFlags(permName, packageName, 16, 16, 1000, 0, true, (PermissionManagerServiceInternal.PermissionCallback) null);
                            Slog.i(TAG, "premInfo.name is " + permInfo.name + " is fix to packge " + packageName);
                            return;
                        }
                    }
                }
                Slog.i(TAG, "permInfo is null or not PROTECTION_DANGEROUS, perm name is " + permName);
            }
        }
    }

    private boolean isSysComponentOrPersistentPlatformSignedPrivApp(String pkgName, IHwPackageManagerInner pmsInner) {
        ApplicationInfo appInfo = pmsInner.getApplicationInfo(pkgName, 0, 0);
        if (appInfo == null) {
            Slog.e(TAG, "not found the application info for package " + pkgName);
            return true;
        } else if (UserHandle.getAppId(appInfo.uid) < 10000) {
            return true;
        } else {
            if (appInfo.isPrivilegedApp() && (appInfo.flags & 8) != 0 && pmsInner.checkSignaturesInner("android", pkgName) == 0) {
                return true;
            }
            return false;
        }
    }
}
