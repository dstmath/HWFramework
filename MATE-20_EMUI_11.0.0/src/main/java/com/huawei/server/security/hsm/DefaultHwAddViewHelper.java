package com.huawei.server.security.hsm;

import android.content.Context;

public class DefaultHwAddViewHelper {
    public static final int ACTIVITY_BG_MODE = 4;
    public static final int ACTIVITY_LS_MODE = 8;
    public static final int TOAST_MODE = 2;
    private static DefaultHwAddViewHelper sInstance;

    public static DefaultHwAddViewHelper getInstance(Context context) {
        DefaultHwAddViewHelper defaultHwAddViewHelper;
        synchronized (DefaultHwAddViewHelper.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwAddViewHelper();
            }
            defaultHwAddViewHelper = sInstance;
        }
        return defaultHwAddViewHelper;
    }

    public boolean addViewPermissionCheck(String packageName, int type, int calleruid) {
        return true;
    }
}
