package com.android.server.wm;

import android.content.Intent;
import android.content.res.Configuration;
import java.util.ArrayList;

public class ActivityStackBridgeEx {
    private ActivityStackBridge mActivityStackBridge;
    private ActivityTaskManagerServiceEx mAtmsEx;

    public ActivityStackBridgeEx(ActivityDisplayEx display, int stackId, ActivityStackSupervisorEx supervisor, int windowingMode, int activityType, boolean isOnTop) {
        this.mActivityStackBridge = new ActivityStackBridge(display == null ? null : display.getActivityDisplay(), stackId, supervisor != null ? supervisor.getActivityStackSupervisor() : null, windowingMode, activityType, isOnTop);
        this.mActivityStackBridge.setActivityStackBridgeEx(this);
        if (this.mActivityStackBridge.mService != null) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mActivityStackBridge.mService);
        }
    }

    public ActivityStackBridge getHwActivityStack() {
        return this.mActivityStackBridge;
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        return 0;
    }

    public boolean isSplitActivity(Intent intent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setKeepPortraitFR() {
    }

    public void setIsMwNewTaskSplitStack(boolean isMwNewTaskSplitStack) {
    }

    public boolean isVisibleLocked(String packageName, boolean isDeepRecur) {
        return false;
    }

    public ActivityTaskManagerServiceEx getService() {
        ActivityStackBridge activityStackBridge;
        if (!(this.mAtmsEx != null || (activityStackBridge = this.mActivityStackBridge) == null || activityStackBridge.mService == null)) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mActivityStackBridge.mService);
        }
        return this.mAtmsEx;
    }

    public int getStackId() {
        return this.mActivityStackBridge.mStackId;
    }

    public boolean getHwActivityStackVisible() {
        return this.mActivityStackBridge.mHwActivityStackEx != null && !this.mActivityStackBridge.mHwActivityStackEx.getStackVisible();
    }

    public boolean isTaskHistoryEmpty() {
        return this.mActivityStackBridge.mTaskHistory.isEmpty();
    }

    public ArrayList<TaskRecordBridgeEx> getTaskRecordExHistory() {
        return this.mActivityStackBridge.getTaskRecordExHistory();
    }

    public boolean getFreeFormStackVisible() {
        return this.mActivityStackBridge.mIsFreeFormStackVisible;
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecordEx starting) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisibleEx(ActivityRecordEx starting) {
        ActivityRecord activityRecord = null;
        if (starting != null) {
            activityRecord = starting.getActivityRecord();
        }
        return this.mActivityStackBridge.shouldBeVisibleEx(activityRecord);
    }

    public boolean inFreeformWindowingMode() {
        return this.mActivityStackBridge.inFreeformWindowingMode();
    }

    public boolean inHwMagicWindowingMode() {
        return this.mActivityStackBridge.inHwMagicWindowingMode();
    }

    public boolean isMwNewTaskSplitStack() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackTaskToTop() {
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecordEx task) {
    }

    public void moveToBackEx(String reason, TaskRecordEx task) {
        this.mActivityStackBridge.moveToBackEx(reason, task);
    }

    public boolean getStackVisible() {
        return this.mActivityStackBridge.mHwActivityStackEx != null && !this.mActivityStackBridge.mHwActivityStackEx.getStackVisible();
    }
}
