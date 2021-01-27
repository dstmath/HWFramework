package com.huawei.android.os;

import android.os.PowerManager;

public class PowerManagerExt {
    public static final int USER_ACTIVITY_EVENT_TOUCH = 2;

    public static void userActivity(PowerManager powerManager, long when, int event, int flags) {
        powerManager.userActivity(when, event, flags);
    }
}
