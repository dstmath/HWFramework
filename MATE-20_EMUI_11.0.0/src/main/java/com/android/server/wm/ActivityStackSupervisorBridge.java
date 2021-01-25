package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Looper;

public class ActivityStackSupervisorBridge extends ActivityStackSupervisor {
    private ActivityStackSupervisorBridgeEx mActivityStackSupervisorBridgeEx;

    public ActivityStackSupervisorBridge(ActivityTaskManagerService service, Looper looper) {
        super(service, looper);
    }

    public void setActivityStackSupervisorEx(ActivityStackSupervisorBridgeEx activityStackSupervisorEx) {
        this.mActivityStackSupervisorBridgeEx = activityStackSupervisorEx;
    }

    public ActivityStackSupervisorBridgeEx getActivityStackSupervisorBridgeEx() {
        return this.mActivityStackSupervisorBridgeEx;
    }

    public RootActivityContainer getRootActivityContainer() {
        return this.mRootActivityContainer;
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntentEx(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        return ActivityStackSupervisorBridge.super.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
    }

    /* access modifiers changed from: package-private */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        return this.mActivityStackSupervisorBridgeEx.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
    }

    public boolean isInVisibleStack(String pkg) {
        return this.mActivityStackSupervisorBridgeEx.isInVisibleStack(pkg);
    }

    public void scheduleReportPCWindowStateChangedLocked(TaskRecord task) {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        if (task != null) {
            taskRecordEx.setTaskRecord(task);
        }
        this.mActivityStackSupervisorBridgeEx.scheduleReportPCWindowStateChangedLocked(taskRecordEx);
    }

    public void reCalculateDefaultMinimalSizeOfResizeableTasks() {
        ActivityStackSupervisorBridge.super.reCalculateDefaultMinimalSizeOfResizeableTasks();
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        return this.mActivityStackSupervisorBridgeEx.shouldNotKillProcWhenRemoveTask(pkg);
    }

    public void recognitionMaliciousApp(IApplicationThread caller, Intent intent, int userId) {
        this.mActivityStackSupervisorBridgeEx.recognitionMaliciousApp(caller, intent, userId);
    }

    public void handlePCWindowStateChanged() {
        this.mActivityStackSupervisorBridgeEx.handlePCWindowStateChanged();
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLocked(TaskRecord task, ActivityOptions activityOptions, boolean isOnTop) {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        if (task != null) {
            taskRecordEx.setTaskRecord(task);
        }
        return this.mActivityStackSupervisorBridgeEx.restoreRecentTaskLocked(taskRecordEx, activityOptions, isOnTop);
    }

    /* access modifiers changed from: protected */
    public boolean restoreRecentTaskLockedEx(TaskRecordEx task, ActivityOptions activityOptions, boolean isOnTop) {
        if (task == null || task.isEmpty()) {
            return false;
        }
        return ActivityStackSupervisorBridge.super.restoreRecentTaskLocked(task.getTaskRecord(), activityOptions, isOnTop);
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumed(ActivityStack stack) {
        ActivityStackEx activityStackEx = new ActivityStackEx();
        if (stack != null) {
            activityStackEx.setActivityStack(stack);
        }
        return this.mActivityStackSupervisorBridgeEx.keepStackResumed(activityStackEx);
    }

    /* access modifiers changed from: protected */
    public boolean keepStackResumedEx(ActivityStackEx stack) {
        ActivityStack activityStack = null;
        if (stack != null) {
            activityStack = stack.getActivityStack();
        }
        return ActivityStackSupervisorBridge.super.keepStackResumed(activityStack);
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisible(ActivityStack stack) {
        ActivityStackEx activityStackEx = new ActivityStackEx();
        if (stack != null) {
            activityStackEx.setActivityStack(stack);
        }
        return this.mActivityStackSupervisorBridgeEx.isStackInVisible(activityStackEx);
    }

    /* access modifiers changed from: protected */
    public boolean isStackInVisibleEx(ActivityStackEx stack) {
        ActivityStack activityStack = null;
        if (stack != null) {
            activityStack = stack.getActivityStack();
        }
        return ActivityStackSupervisorBridge.super.isStackInVisible(activityStack);
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplay(ActivityRecord activityRecord) {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        if (activityRecord != null) {
            activityRecordEx.setActivityRecord(activityRecord);
        }
        return this.mActivityStackSupervisorBridgeEx.startProcessOnExtDisplay(activityRecordEx);
    }

    /* access modifiers changed from: protected */
    public boolean startProcessOnExtDisplayEx(ActivityRecordEx activityRecordEx) {
        ActivityRecord activityRecord = null;
        if (activityRecordEx != null) {
            activityRecord = activityRecordEx.getActivityRecord();
        }
        return ActivityStackSupervisorBridge.super.startProcessOnExtDisplay(activityRecord);
    }

    /* access modifiers changed from: package-private */
    public boolean hasActivityInStackLocked(ActivityInfo activityInfo) {
        return this.mActivityStackSupervisorBridgeEx.hasActivityInStackLocked(activityInfo);
    }

    /* access modifiers changed from: protected */
    public void uploadUnSupportSplitScreenAppPackageName(String pkgName) {
        this.mActivityStackSupervisorBridgeEx.uploadUnSupportSplitScreenAppPackageName(pkgName);
    }

    /* access modifiers changed from: protected */
    public ActivityStack getTargetSplitTopStack(ActivityStack current) {
        new ActivityStackEx();
        ActivityStackEx currentEx = new ActivityStackEx();
        if (current != null) {
            currentEx.setActivityStack(current);
        }
        ActivityStackEx activityStackEx = this.mActivityStackSupervisorBridgeEx.getTargetSplitTopStack(currentEx);
        if (activityStackEx != null) {
            return activityStackEx.getActivityStack();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public ActivityStack getNextStackInSplitSecondary(ActivityStack current) {
        ActivityStackEx currentEx = new ActivityStackEx();
        if (current != null) {
            currentEx.setActivityStack(current);
        }
        ActivityStackEx activityStackEx = this.mActivityStackSupervisorBridgeEx.getNextStackInSplitSecondary(currentEx);
        if (activityStackEx != null) {
            return activityStackEx.getActivityStack();
        }
        return null;
    }
}
