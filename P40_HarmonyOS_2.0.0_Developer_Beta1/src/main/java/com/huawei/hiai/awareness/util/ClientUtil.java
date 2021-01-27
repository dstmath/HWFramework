package com.huawei.hiai.awareness.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.huawei.hiai.awareness.log.Logger;

public class ClientUtil {
    private static final String TAG = "ClientUtil";

    private ClientUtil() {
    }

    public static synchronized boolean checkAwarenessApkInstalled(Context context) {
        synchronized (ClientUtil.class) {
            if (context == null) {
                return false;
            }
            return checkApkExist(context, "com.huawei.hiai");
        }
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 8192);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "NameNotFoundException in getting ApplicationInfo for checking apk exist");
        }
        if (info != null) {
            return true;
        }
        return false;
    }
}
