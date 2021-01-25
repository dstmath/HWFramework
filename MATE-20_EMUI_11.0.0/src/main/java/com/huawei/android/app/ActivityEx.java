package com.huawei.android.app;

import android.app.Activity;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityEx {
    public static int getNavigationBarColor(Activity activity) {
        return activity.mNavigationBarColor;
    }

    public static void setNavigationBarColor(Activity activity, int color) {
        activity.mNavigationBarColor = color;
    }

    public static boolean isResumed(Activity activity) {
        return activity.isResumed();
    }
}
