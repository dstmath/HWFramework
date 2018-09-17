package com.huawei.android.app;

import android.app.ActivityThread;

public class ActivityThreadEx {
    private ActivityThread mActivityThread;

    private ActivityThreadEx(ActivityThread activityThread) {
        this.mActivityThread = activityThread;
    }

    public static ActivityThreadEx currentActivityThread() {
        return new ActivityThreadEx(ActivityThread.currentActivityThread());
    }

    public Object getApplicationThread() {
        return this.mActivityThread.getApplicationThread();
    }
}
