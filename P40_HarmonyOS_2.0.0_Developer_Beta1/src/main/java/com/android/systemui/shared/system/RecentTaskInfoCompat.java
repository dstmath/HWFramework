package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.content.ComponentName;

public class RecentTaskInfoCompat {
    private ActivityManager.RecentTaskInfo mInfo;

    public RecentTaskInfoCompat(ActivityManager.RecentTaskInfo info) {
        this.mInfo = info;
    }

    public int getUserId() {
        return this.mInfo.userId;
    }

    public boolean supportsSplitScreenMultiWindow() {
        return this.mInfo.supportsSplitScreenMultiWindow;
    }

    public ComponentName getTopActivity() {
        return this.mInfo.topActivity;
    }

    public ActivityManager.TaskDescription getTaskDescription() {
        return this.mInfo.taskDescription;
    }
}
