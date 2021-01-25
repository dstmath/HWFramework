package com.huawei.android.content.pm;

import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;

public class ResolveInfoEx {
    public static ComponentInfo getComponentInfo(ResolveInfo receiver) {
        if (receiver == null) {
            return null;
        }
        return receiver.getComponentInfo();
    }

    public static String getComponentName(ResolveInfo receiver) throws IllegalStateException {
        if (receiver == null || receiver.getComponentInfo() == null) {
            return null;
        }
        return receiver.getComponentInfo().getComponentName().getClassName();
    }
}
