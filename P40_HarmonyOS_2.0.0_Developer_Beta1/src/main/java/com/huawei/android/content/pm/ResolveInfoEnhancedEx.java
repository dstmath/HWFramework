package com.huawei.android.content.pm;

import android.content.pm.ResolveInfo;

public class ResolveInfoEnhancedEx {
    public static boolean isSystem(ResolveInfo resolveInfo) {
        return resolveInfo.system;
    }
}
