package com.android.server.wifi.grs.utils;

import android.content.Context;

public class ContextUtil {
    private static final String MESSAGE = "grs context == null";
    private static Context sContext;

    private ContextUtil() {
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        if (context != null) {
            sContext = context.getApplicationContext();
            return;
        }
        throw new NullPointerException(MESSAGE);
    }
}
