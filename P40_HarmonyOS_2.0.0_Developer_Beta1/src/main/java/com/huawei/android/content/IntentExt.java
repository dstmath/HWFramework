package com.huawei.android.content;

import android.content.Intent;

public class IntentExt {
    public static final String EXTRA_REASON = "android.intent.extra.REASON";
    public static final int FLAG_HW_ALARMDELIVER_INTENT = 2048;
    public static final int FLAG_HW_ALARM_INTENT = 256;
    public static final int FLAG_HW_JOBSERVICE_FOR_CLEAN_INTENT = 8192;
    public static final int FLAG_HW_JOB_INTENT = 32;
    public static final int FLAG_HW_LOCATION_INTENT = 512;
    public static final int FLAG_HW_SYNC_INTENT = 64;
    public static final int FLAG_HW_SYNC_USER = 128;

    public static Object getExtra(Intent intent, String name) {
        if (intent == null) {
            return null;
        }
        return intent.getExtra(name);
    }

    public static Intent setHwFlags(Intent intent, int flags) {
        if (intent == null) {
            return null;
        }
        return intent.setHwFlags(flags);
    }

    public static int getHwFlags(Intent intent) {
        if (intent == null) {
            return 0;
        }
        return intent.getHwFlags();
    }
}
