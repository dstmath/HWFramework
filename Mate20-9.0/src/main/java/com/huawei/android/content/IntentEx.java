package com.huawei.android.content;

import com.huawei.internal.telephony.TelephonyIntentsEx;

public class IntentEx {
    public static final String ACTION_FM = "android.intent.action.FM";
    public static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
    public static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
    public static final String EXTRA_TASK_ID = "android.intent.extra.TASK_ID";
    public static final int FLAG_HW_SPLIT_ACTIVITY = 4;

    public static String getActionCallPrivileged() {
        return IntentExEx.ACTION_CALL_PRIVILEGED;
    }

    public static String getActionSimStateChanged() {
        return TelephonyIntentsEx.ACTION_SIM_STATE_CHANGED;
    }
}
