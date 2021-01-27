package com.android.internal.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

public class ValidGPackageHelper {
    public static final String[] GMS_CORE_PKGS = {"com.google.android.gms", "com.google.android.gsf"};
    private static final String GMS_VERSION = SystemProperties.get("ro.com.google.gmsversion", (String) null);
    private static final boolean IS_GMS_BUILDIN = (!TextUtils.isEmpty(GMS_VERSION));
    private static final String TAG = "ValidGPackageHelper";

    private ValidGPackageHelper() {
    }

    public static boolean isValidForG(Context context) {
        if (context == null || IS_GMS_BUILDIN) {
            return true;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.i(TAG, "isValidForG: package manager is null");
            return false;
        }
        String[] strArr = GMS_CORE_PKGS;
        boolean isPrivilegedApp = true;
        boolean isMDM = true;
        for (String pkg : strArr) {
            ApplicationInfo mInfo = null;
            try {
                mInfo = pm.getApplicationInfo(pkg, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "isValidForG: " + pkg + " can not found");
            }
            if (mInfo == null) {
                return false;
            }
            isPrivilegedApp &= mInfo.isSystemApp() && mInfo.isPrivilegedApp();
            isMDM &= pm.getInstallReason(pkg, UserHandle.of(UserHandle.myUserId())) == 9;
        }
        if (isMDM || isPrivilegedApp) {
            return true;
        }
        return false;
    }
}
