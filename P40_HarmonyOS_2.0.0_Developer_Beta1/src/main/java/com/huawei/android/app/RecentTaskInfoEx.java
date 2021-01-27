package com.huawei.android.app;

import android.app.ActivityManager;
import android.content.ComponentName;

public class RecentTaskInfoEx {
    private ActivityManager.RecentTaskInfo mRecentTaskInfo = null;

    public RecentTaskInfoEx() {
    }

    public RecentTaskInfoEx(ActivityManager.RecentTaskInfo info) {
        this.mRecentTaskInfo = info;
    }

    public ComponentName getRealActivity() {
        ActivityManager.RecentTaskInfo recentTaskInfo = this.mRecentTaskInfo;
        if (recentTaskInfo != null) {
            return recentTaskInfo.realActivity;
        }
        return null;
    }

    public int getUserId() {
        ActivityManager.RecentTaskInfo recentTaskInfo = this.mRecentTaskInfo;
        if (recentTaskInfo != null) {
            return recentTaskInfo.userId;
        }
        return 0;
    }
}
