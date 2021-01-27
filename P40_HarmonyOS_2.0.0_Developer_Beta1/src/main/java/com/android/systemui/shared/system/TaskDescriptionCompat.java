package com.android.systemui.shared.system;

import android.app.ActivityManager;

public class TaskDescriptionCompat {
    private ActivityManager.TaskDescription mTaskDescription;

    public TaskDescriptionCompat(ActivityManager.TaskDescription td) {
        this.mTaskDescription = td;
    }

    public int getPrimaryColor() {
        ActivityManager.TaskDescription taskDescription = this.mTaskDescription;
        if (taskDescription != null) {
            return taskDescription.getPrimaryColor();
        }
        return 0;
    }

    public int getBackgroundColor() {
        ActivityManager.TaskDescription taskDescription = this.mTaskDescription;
        if (taskDescription != null) {
            return taskDescription.getBackgroundColor();
        }
        return 0;
    }
}
