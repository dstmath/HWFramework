package com.huawei.ark.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.huawei.android.content.pm.ApplicationInfoEx;

public class ArkApplicationInfo {
    public static PackageMode getPackageMode(Context context, String packageName) {
        if (context == null || packageName == null) {
            return null;
        }
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (app == null) {
                return null;
            }
            int mode = new ApplicationInfoEx(app).getHwFlags();
            if ((4194304 & mode) != 0) {
                return PackageMode.ARK;
            }
            if ((16777216 & mode) != 0) {
                return PackageMode.MIX;
            }
            if ((8388608 & mode) != 0) {
                return PackageMode.BOTH;
            }
            return PackageMode.ANDROID;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isRunningInArk() {
        return System.getenv("MAPLE_RUNTIME") != null;
    }
}
