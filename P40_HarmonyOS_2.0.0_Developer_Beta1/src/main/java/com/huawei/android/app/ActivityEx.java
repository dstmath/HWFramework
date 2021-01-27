package com.huawei.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.UserHandle;
import com.huawei.annotation.HwSystemApi;

public class ActivityEx {
    @HwSystemApi
    public static int getNavigationBarColor(Activity activity) {
        return activity.mNavigationBarColor;
    }

    @HwSystemApi
    public static void setNavigationBarColor(Activity activity, int color) {
        activity.mNavigationBarColor = color;
    }

    @HwSystemApi
    public static boolean isResumed(Activity activity) {
        return activity.isResumed();
    }

    public static void startActivityForResultAsUser(Activity activity, Intent intent, int requestCode, UserHandle user) {
        if (PackageManagerEx.hasSystemSignaturePermission(activity)) {
            activity.startActivityForResultAsUser(intent, requestCode, user);
        }
    }

    public static void startActivityAsUser(Activity activity, Intent intent, UserHandle user) {
        if (PackageManagerEx.hasSystemSignaturePermission(activity)) {
            activity.startActivityAsUser(intent, user);
        }
    }
}
