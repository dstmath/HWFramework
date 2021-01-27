package com.huawei.android.app;

import android.app.ActivityOptions;
import android.os.Bundle;

public class ActivityOptionsEx {
    private ActivityOptions mActivityOptions = null;

    public static int getLaunchWindowingMode(ActivityOptions aoopt) {
        return aoopt.getLaunchWindowingMode();
    }

    public int getLaunchWindowingMode() {
        ActivityOptions activityOptions = this.mActivityOptions;
        if (activityOptions != null) {
            return activityOptions.getLaunchWindowingMode();
        }
        return 0;
    }

    public void setLaunchWindowingMode(int windowingMode) {
        ActivityOptions activityOptions = this.mActivityOptions;
        if (activityOptions != null) {
            activityOptions.setLaunchWindowingMode(windowingMode);
        }
    }

    public static void setLaunchWindowingMode(ActivityOptions options, int windowingMode) {
        if (options != null) {
            options.setLaunchWindowingMode(windowingMode);
        }
    }

    public static void setLaunchTaskId(ActivityOptions options, int taskId) {
        if (options != null) {
            options.setLaunchTaskId(taskId);
        }
    }

    public static ActivityOptionsEx makeBasic() {
        ActivityOptionsEx aoe = new ActivityOptionsEx();
        aoe.setActivityOptions(ActivityOptions.makeBasic());
        return aoe;
    }

    public void setActivityOptions(ActivityOptions activityOptions) {
        this.mActivityOptions = activityOptions;
    }

    public ActivityOptions getActivityOptions() {
        return this.mActivityOptions;
    }

    public Bundle toBundle() {
        ActivityOptions activityOptions = this.mActivityOptions;
        if (activityOptions != null) {
            return activityOptions.toBundle();
        }
        return null;
    }
}
