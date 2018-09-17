package com.huawei.android.content.pm;

import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.ResolveInfo;

public class IPackageManagerEx {
    public static ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws Exception {
        return AppGlobals.getPackageManager().getLastChosenActivity(intent, resolvedType, flags);
    }
}
