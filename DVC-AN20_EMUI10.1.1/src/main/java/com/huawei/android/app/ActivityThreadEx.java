package com.huawei.android.app;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import com.huawei.annotation.HwSystemApi;

public class ActivityThreadEx {
    private ActivityThread mActivityThread = ActivityThread.currentActivityThread();

    private ActivityThreadEx() {
    }

    public static ActivityThreadEx currentActivityThread() {
        if (ActivityThread.currentActivityThread() != null) {
            return new ActivityThreadEx();
        }
        return null;
    }

    public Object getApplicationThread() {
        return this.mActivityThread.getApplicationThread();
    }

    @HwSystemApi
    public void setDisplayId(int displayId) {
        this.mActivityThread.setDisplayId(displayId);
    }

    @HwSystemApi
    public int getDisplayId() {
        return this.mActivityThread.getDisplayId();
    }

    @HwSystemApi
    public static String currentPackageName() {
        return ActivityThread.currentPackageName();
    }

    @HwSystemApi
    public static String currentActivityName() {
        return ActivityThread.currentActivityName();
    }

    @HwSystemApi
    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }

    @HwSystemApi
    public Application getApplication() {
        return this.mActivityThread.getApplication();
    }

    @HwSystemApi
    public static String currentOpPackageName() {
        return ActivityThread.currentOpPackageName();
    }

    @HwSystemApi
    public Context getSystemUiContext() {
        return this.mActivityThread.getSystemUiContext();
    }
}
