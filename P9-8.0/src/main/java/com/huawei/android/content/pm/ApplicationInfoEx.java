package com.huawei.android.content.pm;

import android.content.pm.AbsApplicationInfo;
import android.content.pm.ApplicationInfo;

public class ApplicationInfoEx extends AbsApplicationInfo {
    private ApplicationInfo applicationInfo = new ApplicationInfo();

    public ApplicationInfoEx(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public int getHwFlags() {
        return this.applicationInfo.hwFlags;
    }
}
