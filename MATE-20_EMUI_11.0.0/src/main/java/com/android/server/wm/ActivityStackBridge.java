package com.android.server.wm;

import android.content.Intent;
import android.content.res.Configuration;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityStackBridge extends ActivityStack {
    private ActivityStackBridgeEx mActivityStackBridgeEx;

    public ActivityStackBridge(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean isOnTop) {
        super(display, stackId, supervisor, windowingMode, activityType, isOnTop);
    }

    public void setActivityStackBridgeEx(ActivityStackBridgeEx activityStackBridgeEx) {
        this.mActivityStackBridgeEx = activityStackBridgeEx;
    }

    public ActivityStackBridgeEx getActivityStackBridgeEx() {
        return this.mActivityStackBridgeEx;
    }

    public ArrayList<TaskRecordBridgeEx> getTaskRecordExHistory() {
        ArrayList<TaskRecordBridgeEx> taskRecordExList = new ArrayList<>();
        Iterator it = this.mTaskHistory.iterator();
        while (it.hasNext()) {
            TaskRecord taskRecord = (TaskRecord) it.next();
            if (taskRecord instanceof TaskRecordBridge) {
                taskRecordExList.add(((TaskRecordBridge) taskRecord).getHwTaskRecord());
            }
        }
        return taskRecordExList;
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecord starting) {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        if (starting != null) {
            activityRecordEx.setActivityRecord(starting);
        }
        ActivityStackBridgeEx activityStackBridgeEx = this.mActivityStackBridgeEx;
        if (activityStackBridgeEx != null) {
            return activityStackBridgeEx.shouldBeVisible(activityRecordEx);
        }
        return shouldBeVisibleEx(starting);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisibleEx(ActivityRecord starting) {
        return ActivityStackBridge.super.shouldBeVisible(starting);
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        return this.mActivityStackBridgeEx.getInvalidFlag(changes, newConfig, naviConfig);
    }

    public boolean isSplitActivity(Intent intent) {
        return this.mActivityStackBridgeEx.isSplitActivity(intent);
    }

    /* access modifiers changed from: protected */
    public void setKeepPortraitFR() {
        this.mActivityStackBridgeEx.setKeepPortraitFR();
    }

    public boolean isVisibleLocked(String packageName, boolean isDeepRecur) {
        return this.mActivityStackBridgeEx.isVisibleLocked(packageName, isDeepRecur);
    }

    public boolean isMwNewTaskSplitStack() {
        return this.mActivityStackBridgeEx.isMwNewTaskSplitStack();
    }

    /* access modifiers changed from: protected */
    public void setIsMwNewTaskSplitStack(boolean isMwNewTaskSplitStack) {
        this.mActivityStackBridgeEx.setIsMwNewTaskSplitStack(isMwNewTaskSplitStack);
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackTaskToTop() {
        this.mActivityStackBridgeEx.moveHomeStackTaskToTop();
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecord task) {
        TaskRecordEx taskRecordEx = null;
        if (task != null) {
            taskRecordEx = new TaskRecordEx();
            taskRecordEx.setTaskRecord(task);
        }
        this.mActivityStackBridgeEx.moveToBack(reason, taskRecordEx);
    }

    /* access modifiers changed from: package-private */
    public void moveToBackEx(String reason, TaskRecordEx task) {
        if (task != null) {
            ActivityStackBridge.super.moveToBack(reason, task.getTaskRecord());
        } else {
            ActivityStackBridge.super.moveToBack(reason, (TaskRecord) null);
        }
    }
}
