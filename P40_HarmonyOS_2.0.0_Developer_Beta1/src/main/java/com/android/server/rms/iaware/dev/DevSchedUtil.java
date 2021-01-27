package com.android.server.rms.iaware.dev;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.pgmng.plug.PowerKit;

public class DevSchedUtil {
    public static final int INVALID_UID = -1;

    public static boolean isInvalidAppInfo(String processName, int uid, int pid) {
        return processName == null || pid < 0 || uid <= 1000;
    }

    public static String getTopFrontApp(Context context) {
        PowerKit pgSdk;
        if (context == null || (pgSdk = PowerKit.getInstance()) == null) {
            return null;
        }
        try {
            return pgSdk.getTopFrontApp(context);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int getUidByPkgName(String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return -1;
        }
        try {
            ApplicationInfo applicationInfo = IPackageManagerEx.getApplicationInfo(pkgName, 0, ActivityManagerEx.getCurrentUser());
            if (applicationInfo != null) {
                return applicationInfo.uid;
            }
            return -1;
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static boolean isStrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
