package com.huawei.networkit.grs.common;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtils {
    private static final String TAG = "PackageUtils";
    private static String USER_AGENT = null;
    private static final String VERSION = "1.0.13.300";

    public static String getVersionName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "", e);
            return "";
        }
    }
}
