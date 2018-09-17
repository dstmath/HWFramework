package com.android.server.wifi.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AppGlobals;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import com.android.server.LocalServices;
import java.util.List;

public class WifiPermissionsWrapper {
    private static final String TAG = "WifiPermissionsWrapper";
    private final Context mContext;

    public WifiPermissionsWrapper(Context context) {
        this.mContext = context;
    }

    public int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    public int getCallingUserId(int uid) {
        return UserHandle.getUserId(uid);
    }

    public String getTopPkgName() {
        String topTaskPkg = " ";
        List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (tasks.isEmpty()) {
            return topTaskPkg;
        }
        return ((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName();
    }

    public int getUidPermission(String permissionType, int uid) {
        return ActivityManager.checkUidPermission(permissionType, uid);
    }

    public DevicePolicyManagerInternal getDevicePolicyManagerInternal() {
        return (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
    }

    public int getOverrideWifiConfigPermission(int uid) throws RemoteException {
        return AppGlobals.getPackageManager().checkUidPermission("android.permission.OVERRIDE_WIFI_CONFIG", uid);
    }
}
