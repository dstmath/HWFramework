package com.huawei.ace.runtime;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;

public class DeviceInfoHelper {
    public static boolean isTvType(Context context) {
        Resources resources;
        Configuration configuration;
        if (context == null || (resources = context.getResources()) == null || (configuration = resources.getConfiguration()) == null || (configuration.uiMode & 15) != 4) {
            return false;
        }
        return true;
    }

    public static boolean isPhoneType(Context context) {
        return !isTvType(context) && !isWatchType(context);
    }

    public static boolean isWatchType(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        return packageManager.hasSystemFeature("android.hardware.type.watch");
    }
}
