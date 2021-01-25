package com.huawei.systemmanager.notificationmanager.restrictedlock;

import android.content.Context;

public class RestrictedLockUtilsManager {
    public static IDevicePolicyManager getDevicePolicyManager(Context context) {
        return HwDevicePolicyManagerImpl.getInstance();
    }
}
