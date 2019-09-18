package com.huawei.android.content.pm;

import android.content.pm.AbsApplicationInfo;
import android.content.pm.ApplicationInfo;

public class ApplicationInfoEx extends AbsApplicationInfo {
    private ApplicationInfo applicationInfo = new ApplicationInfo();

    public ApplicationInfoEx(ApplicationInfo applicationInfo2) {
        this.applicationInfo = applicationInfo2;
    }

    public int getHwFlags() {
        return this.applicationInfo.hwFlags;
    }

    private static boolean isDirectBootAware(ApplicationInfo applicationInfo2) {
        return applicationInfo2.isDirectBootAware();
    }
}
