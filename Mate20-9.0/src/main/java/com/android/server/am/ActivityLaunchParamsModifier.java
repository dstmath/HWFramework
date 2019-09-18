package com.android.server.am;

import android.app.ActivityOptions;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import com.android.server.am.LaunchParamsController;

public class ActivityLaunchParamsModifier implements LaunchParamsController.LaunchParamsModifier {
    private final ActivityStackSupervisor mSupervisor;

    ActivityLaunchParamsModifier(ActivityStackSupervisor activityStackSupervisor) {
        this.mSupervisor = activityStackSupervisor;
    }

    public int onCalculate(TaskRecord task, ActivityInfo.WindowLayout layout, ActivityRecord activity, ActivityRecord source, ActivityOptions options, LaunchParamsController.LaunchParams currentParams, LaunchParamsController.LaunchParams outParams) {
        if (activity == null || !this.mSupervisor.canUseActivityOptionsLaunchBounds(options) || (!activity.isResizeable() && (task == null || !task.isResizeable()))) {
            return 0;
        }
        Rect bounds = options.getLaunchBounds();
        if (bounds == null || bounds.isEmpty()) {
            return 0;
        }
        if (!(source == null || source.getTask() == null || !source.getTask().inFreeformWindowingMode())) {
            outParams.mWindowingMode = 5;
        }
        outParams.mBounds.set(bounds);
        return 1;
    }
}
