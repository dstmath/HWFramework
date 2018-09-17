package com.huawei.android.content;

import android.content.Intent;

public class IntentExEx {
    public static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
    public static final String ACTION_DEVICE_STORAGE_FULL = "android.intent.action.DEVICE_STORAGE_FULL";
    public static final String ACTION_DEVICE_STORAGE_NOT_FULL = "android.intent.action.DEVICE_STORAGE_NOT_FULL";
    public static final String ACTION_PRE_BOOT_COMPLETED = "android.intent.action.PRE_BOOT_COMPLETED";
    public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
    public static final int FLAG_HW_INTENT_TO_STRING_SAFELY = 16;
    public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 67108864;

    public static String getActionUserSwitched() {
        return "android.intent.action.USER_SWITCHED";
    }

    public static String getExtraKeyConfirm() {
        return "android.intent.extra.KEY_CONFIRM";
    }

    public static String getActionRequestShutdown() {
        return "com.android.internal.intent.action.REQUEST_SHUTDOWN";
    }

    public static Intent addHwFlags(Intent intent, int flags) {
        if (intent != null) {
            return intent.addHwFlags(flags);
        }
        return null;
    }

    public static Intent setHwFlags(Intent intent, int flags) {
        return intent.setHwFlags(flags);
    }

    public static int getHwFlags(Intent intent) {
        return intent.getHwFlags();
    }

    public static String getActionManageAppPermissions() {
        return "android.intent.action.MANAGE_APP_PERMISSIONS";
    }
}
