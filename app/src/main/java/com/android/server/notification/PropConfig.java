package com.android.server.notification;

import android.content.Context;
import android.os.SystemProperties;

public class PropConfig {
    private static final String UNSET = "UNSET";

    public static int getInt(Context context, String propName, int resId) {
        return SystemProperties.getInt(propName, context.getResources().getInteger(resId));
    }

    public static String[] getStringArray(Context context, String propName, int resId) {
        String prop = SystemProperties.get(propName, UNSET);
        return !UNSET.equals(prop) ? prop.split(",") : context.getResources().getStringArray(resId);
    }
}
