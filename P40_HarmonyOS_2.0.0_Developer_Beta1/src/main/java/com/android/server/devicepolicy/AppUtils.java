package com.android.server.devicepolicy;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    private static final String TAG = "AppUtils";

    public static void filterOutSystemAppList(Context context, List<String> packageNames) {
        if (!(context == null || packageNames == null)) {
            List<String> systemAppList = new ArrayList<>();
            for (String name : packageNames) {
                if (isSystemAppExcludePreInstalled(context, name)) {
                    systemAppList.add(name);
                }
            }
            if (!systemAppList.isEmpty()) {
                packageNames.removeAll(systemAppList);
            }
        }
    }

    private static boolean isSystemAppExcludePreInstalled(Context context, String packageName) {
        long id = Binder.clearCallingIdentity();
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm == null) {
            return false;
        }
        UserManager um = UserManager.get(context);
        if (um == null) {
            HwLog.e(TAG, "failed to get um");
            return false;
        }
        int userId = UserHandle.getCallingUserId();
        UserInfo primaryUser = um.getProfileParent(userId);
        if (primaryUser == null) {
            primaryUser = um.getUserInfo(userId);
        }
        Binder.restoreCallingIdentity(id);
        return isSystemAppExcludePreInstalled(pm, packageName, primaryUser.id);
    }

    private static boolean isSystemAppExcludePreInstalled(IPackageManager pm, String packageName, int userId) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 8192, userId);
            if (appInfo == null) {
                return false;
            }
            int flags = appInfo.flags;
            boolean isPreInstalled = true;
            if ((flags & 1) == 0) {
                HwLog.d(TAG, "packageName is not systemFlag");
                isPreInstalled = false;
            }
            if (!((flags & 1) == 0 || (flags & 33554432) == 0)) {
                HwLog.d(TAG, "SystemApp preInstalledFlag");
                isPreInstalled = false;
            }
            int hwFlags = appInfo.hwFlags;
            if (!((flags & 1) == 0 || (hwFlags & 33554432) == 0)) {
                isPreInstalled = false;
                HwLog.d(TAG, "packageName is not systemFlag");
            }
            if ((hwFlags & 67108864) != 0) {
                return false;
            }
            return isPreInstalled;
        } catch (RemoteException e) {
            HwLog.e(TAG, "could not get appInfo, exception is hadppened");
            return false;
        }
    }
}
