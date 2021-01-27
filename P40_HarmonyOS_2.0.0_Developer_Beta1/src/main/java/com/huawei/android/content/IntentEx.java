package com.huawei.android.content;

import com.huawei.annotation.HwSystemApi;

public class IntentEx {
    public static final String ACTION_FM = "android.intent.action.FM";
    @HwSystemApi
    public static final String ACTION_PREFERRED_ACTIVITY_CHANGED = "android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED";
    @HwSystemApi
    public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
    public static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
    @HwSystemApi
    public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
    @HwSystemApi
    public static final String EXTRA_HW_START_MODE = "huawei.intent.extra.mode";
    @HwSystemApi
    public static final String EXTRA_PACKAGES = "android.intent.extra.PACKAGES";
    @HwSystemApi
    public static final String EXTRA_REASON = "android.intent.extra.REASON";
    @HwSystemApi
    public static final String EXTRA_REMOVED_FOR_ALL_USERS = "android.intent.extra.REMOVED_FOR_ALL_USERS";
    public static final String EXTRA_REPLACING = "android.intent.extra.REPLACING";
    public static final String EXTRA_TASK_ID = "android.intent.extra.TASK_ID";
    public static final String EXTRA_UID = "android.intent.extra.UID";
    @HwSystemApi
    public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
    @HwSystemApi
    public static final int FLAG_HW_ACTIVITY_STARTACTIVITIES = 524288;
    @HwSystemApi
    public static final int FLAG_HW_ACTIVITY_START_FROM_HOME = 4096;
    @HwSystemApi
    public static final int FLAG_HW_CANCEL_SPLIT = 8;
    public static final int FLAG_HW_SPLIT_ACTIVITY = 4;
    @HwSystemApi
    public static final int FLAG_HW_START_HOME_FOME_HOME_KEY = 32768;
    @HwSystemApi
    public static final int FLAG_IGNORE_EPHEMERAL = 512;
    @HwSystemApi
    public static final int FLAG_RECEIVER_INCLUDE_BACKGROUND = 16777216;
    @HwSystemApi
    public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 67108864;

    public static String getActionCallPrivileged() {
        return IntentExEx.ACTION_CALL_PRIVILEGED;
    }

    public static String getActionSimStateChanged() {
        return "android.intent.action.SIM_STATE_CHANGED";
    }
}
