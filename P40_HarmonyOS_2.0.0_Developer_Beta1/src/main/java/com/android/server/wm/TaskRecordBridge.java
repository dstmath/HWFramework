package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.service.voice.IVoiceInteractionSessionEx;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import java.util.ArrayList;
import java.util.Iterator;

public class TaskRecordBridge extends TaskRecord {
    private TaskRecordBridgeEx mTaskRecordBridgeEx;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TaskRecordBridge(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSessionEx voiceSession, IVoiceInteractorEx voiceInteractor) {
        super(service.getActivityTaskManagerService(), taskId, info, intent, voiceSession == null ? null : voiceSession.getIVoiceInteractionSession(), voiceInteractor != null ? voiceInteractor.getVoiceInteractor() : null);
    }

    public TaskRecordBridge(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        super(service.getActivityTaskManagerService(), taskId, info, intent, taskDescription);
    }

    public TaskRecordBridge(ActivityTaskManagerServiceEx service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean isRootWasReset, boolean isAutoRemoveRecents, boolean isAskedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean isNeverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean isSupportsPictureInPicture, boolean isRealActivitySuspended, boolean isUserSetupComplete, int minWidth, int minHeight) {
        super(service.getActivityTaskManagerService(), taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, isRootWasReset, isAutoRemoveRecents, isAskedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, isNeverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, isSupportsPictureInPicture, isRealActivitySuspended, isUserSetupComplete, minWidth, minHeight);
    }

    public void setTaskRecordBridgeEx(TaskRecordBridgeEx taskRecordBridgeEx) {
        this.mTaskRecordBridgeEx = taskRecordBridgeEx;
    }

    public TaskRecordBridgeEx getHwTaskRecord() {
        return this.mTaskRecordBridgeEx;
    }

    public void setWindowStateEx(int windowState) {
        this.mWindowState = windowState;
    }

    public void setWindowState(int windowState) {
        this.mTaskRecordBridgeEx.setWindowState(windowState);
    }

    public void setStackEx(ActivityStack stack) {
        TaskRecordBridge.super.setStack(stack);
    }

    /* access modifiers changed from: package-private */
    public void setStack(ActivityStack stack) {
        ActivityStackEx activityStackEx = new ActivityStackEx();
        if (stack != null) {
            activityStackEx.setActivityStack(stack);
        }
        this.mTaskRecordBridgeEx.setStack(activityStackEx);
    }

    public Rect getLaunchBoundsEx() {
        return TaskRecordBridge.super.getLaunchBounds();
    }

    public Rect getLaunchBounds() {
        return this.mTaskRecordBridgeEx.getLaunchBounds();
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecord activityRecord, boolean isReparenting) {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        if (activityRecord != null) {
            activityRecordEx.setActivityRecord(activityRecord);
        }
        return this.mTaskRecordBridgeEx.removeActivity(activityRecordEx, isReparenting);
    }

    public boolean removeActivityEx(ActivityRecordEx activityRecordEx, boolean isReparenting) {
        if (activityRecordEx == null || activityRecordEx.getActivityRecord() == null) {
            return false;
        }
        return TaskRecordBridge.super.removeActivity(activityRecordEx.getActivityRecord(), isReparenting);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        this.mTaskRecordBridgeEx.removeWindowContainer();
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainerEx() {
        TaskRecordBridge.super.removeWindowContainer();
    }

    /* access modifiers changed from: package-private */
    public void createTask(boolean isOnTop, boolean isShowForAllUsers) {
        this.mTaskRecordBridgeEx.createTask(isOnTop, isShowForAllUsers);
    }

    /* access modifiers changed from: package-private */
    public void createTaskEx(boolean isOnTop, boolean isShowForAllUsers) {
        TaskRecordBridge.super.createTask(isOnTop, isShowForAllUsers);
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean isResizeable) {
        return this.mTaskRecordBridgeEx.isResizeable(isResizeable);
    }

    /* access modifiers changed from: protected */
    public boolean isResizeableEx(boolean isResizeable) {
        return TaskRecordBridge.super.isResizeable(isResizeable);
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensions(Rect bounds, Rect previousBounds) {
        this.mTaskRecordBridgeEx.adjustForMinimalTaskDimensions(bounds, previousBounds);
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensionsEx(Rect bounds, Rect previousBounds) {
        TaskRecordBridge.super.adjustForMinimalTaskDimensions(bounds, previousBounds);
    }

    public ArrayList<ActivityRecord> getActivities() {
        ArrayList<ActivityRecordEx> activityRecordExs = this.mTaskRecordBridgeEx.getActivities();
        ArrayList<ActivityRecord> activityRecords = new ArrayList<>();
        if (activityRecordExs != null) {
            Iterator<ActivityRecordEx> it = activityRecordExs.iterator();
            while (it.hasNext()) {
                activityRecords.add(it.next().getActivityRecord());
            }
        }
        return activityRecords;
    }

    public void overrideConfigOrienForFreeForm(Configuration config) {
        this.mTaskRecordBridgeEx.overrideConfigOrienForFreeForm(config);
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecord activityRecord) {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        if (activityRecord != null) {
            activityRecordEx.setActivityRecord(activityRecord);
        }
        this.mTaskRecordBridgeEx.addActivityToTop(activityRecordEx);
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTopEx(ActivityRecordEx activityRecordEx) {
        if (activityRecordEx != null && activityRecordEx.getActivityRecord() != null) {
            TaskRecordBridge.super.addActivityToTop(activityRecordEx.getActivityRecord());
        }
    }

    public boolean isSaveBounds() {
        return this.mTaskRecordBridgeEx.isSaveBounds();
    }

    public void setSaveBounds(boolean isSaveBounds) {
        this.mTaskRecordBridgeEx.setSaveBounds(isSaveBounds);
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
        this.mTaskRecordBridgeEx.updateHwOverrideConfiguration(bounds);
    }

    /* access modifiers changed from: protected */
    public void updateHwPCMultiCastOverrideConfiguration(Rect bounds) {
        this.mTaskRecordBridgeEx.updateHwPCMultiCastOverrideConfiguration(bounds);
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        return this.mTaskRecordBridgeEx.isMaximizedPortraitAppOnPCMode(packageName);
    }

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
        this.mTaskRecordBridgeEx.activityResumedInTop();
    }

    public ActivityStack getActivityStack() {
        return this.mStack;
    }

    public <T extends ActivityStack> T aospGetStack() {
        return (T) TaskRecordBridge.super.getStack();
    }

    public Rect aospGetRequestedOverrideBounds() {
        return TaskRecordBridge.super.getRequestedOverrideBounds();
    }

    public int getDragFullMode() {
        return this.mTaskRecordBridgeEx.getDragFullMode();
    }

    public void setDragFullMode(int mode) {
        this.mTaskRecordBridgeEx.setDragFullMode(mode);
    }

    public Bundle getMagicWindowExtras() {
        return this.mTaskRecordBridgeEx.getMagicWindowExtras();
    }

    public void setMagicWindowExtras(Bundle bundle) {
        this.mTaskRecordBridgeEx.setMagicWindowExtras(bundle);
    }
}
