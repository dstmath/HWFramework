package com.android.server.security.tsmagent.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageUtil {
    public static int getVersionCode(Context context) {
        if (context == null) {
            return 0;
        }
        try {
            PackageInfo pkInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pkInfo != null) {
                return pkInfo.versionCode;
            }
            return 0;
        } catch (NameNotFoundException e) {
            HwLog.e("NameNotFoundException when getVersionCode");
            return 0;
        }
    }
}
