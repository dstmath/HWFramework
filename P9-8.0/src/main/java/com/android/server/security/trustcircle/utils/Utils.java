package com.android.server.security.trustcircle.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings.Global;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String getProperty(Context context, String key) {
        if (context == null) {
            return "";
        }
        return Global.getString(context.getContentResolver(), key);
    }

    public static void setProperty(Context context, String key, String value) {
        if (context != null) {
            Global.putString(context.getContentResolver(), key, value);
        }
    }

    public static int getCurrentUserId() {
        int userHandle = -10000;
        try {
            return ActivityManager.getCurrentUser();
        } catch (Exception e) {
            LogHelper.e(TAG, "error: exception in getCurrentUserId");
            return userHandle;
        }
    }
}
