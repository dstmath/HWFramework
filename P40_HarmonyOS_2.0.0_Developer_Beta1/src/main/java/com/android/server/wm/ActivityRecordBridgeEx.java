package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.HwMwUtils;
import android.util.Slog;

public class ActivityRecordBridgeEx {
    private ActivityRecordBridge mActivityRecordBridge;
    private ActivityTaskManagerServiceEx mAtmServiceEx;

    public ActivityRecordBridgeEx(ActivityTaskManagerServiceEx serviceEx, WindowProcessControllerEx callerEx, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo activityInfo, Configuration configuration, ActivityRecordEx resultToEx, String resultWho, int reqCode, boolean isComponentSpecified, boolean isRootVoiceInteraction, ActivityStackSupervisorEx supervisorEx, ActivityOptions options, ActivityRecordEx sourceRecordEx) {
        this.mActivityRecordBridge = new ActivityRecordBridge(serviceEx.getActivityTaskManagerService(), callerEx != null ? callerEx.getWindowProcessController() : null, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, activityInfo, configuration, resultToEx != null ? resultToEx.getActivityRecord() : null, resultWho, reqCode, isComponentSpecified, isRootVoiceInteraction, supervisorEx != null ? supervisorEx.getActivityStackSupervisor() : null, options, sourceRecordEx != null ? sourceRecordEx.getActivityRecord() : null);
        this.mAtmServiceEx = serviceEx;
        this.mActivityRecordBridge.setActivityRecordEx(this);
    }

    public ActivityRecordBridge getActivityRecordBridge() {
        return this.mActivityRecordBridge;
    }

    public boolean isFullScreenVideoInLandscape() {
        return false;
    }

    public void setFullScreenVideoInLandscape(boolean isLandscape) {
    }

    public boolean isFromFullscreenToMagicWin() {
        return false;
    }

    public void setFromFullscreenToMagicWin(boolean isFromFullscreenToMagicWin) {
    }

    public boolean isFinishAllRightBottom() {
        return false;
    }

    public void setFinishAllRightBottom(boolean isFinishAllRightBottom) {
    }

    public boolean isAniRunningBelow() {
        return false;
    }

    public void setIsAniRunningBelow(boolean isAniRunningBelow) {
    }

    public Rect getLastBound() {
        return null;
    }

    public void setLastBound(Rect lastBound) {
    }

    public int getMagicWindowPageType() {
        return 0;
    }

    public void setMagicWindowPageType(int magicWindowPageType) {
    }

    public long getCreateTime() {
        return 0;
    }

    public void setCreateTime(long time) {
    }

    public void resize() {
    }

    public int getLastActivityHash() {
        return 0;
    }

    public void setLastActivityHash(int hashValue) {
    }

    public boolean isStartFromLauncher() {
        return false;
    }

    public void setIsStartFromLauncher(boolean isStartFromLauncher) {
    }

    /* access modifiers changed from: package-private */
    public void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
    }

    /* access modifiers changed from: protected */
    public void aospScheduleMultiWindowModeChanged(Configuration overrideConfig) {
        this.mActivityRecordBridge.aospScheduleMultiWindowModeChanged(overrideConfig);
    }

    /* access modifiers changed from: protected */
    public void computeBounds(Rect outBounds, Rect containingAppBounds) {
    }

    /* access modifiers changed from: protected */
    public void aospComputeBounds(Rect outBounds, Rect containingAppBounds) {
        this.mActivityRecordBridge.aospComputeBounds(outBounds, containingAppBounds);
    }

    /* access modifiers changed from: package-private */
    public void setRequestedOrientation(int requestedOrientation) {
    }

    /* access modifiers changed from: protected */
    public void setAospRequestedOrientation(int requestedOrientation) {
        this.mActivityRecordBridge.setAospRequestedOrientation(requestedOrientation);
    }

    /* access modifiers changed from: protected */
    public boolean isForceRotationMode(String packageName, Intent intent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getConfigurationChanges(Configuration lastReportedConfig) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getAospConfigurationChanges(Configuration lastReportedConfig) {
        return this.mActivityRecordBridge.getAospConfigurationChanges(lastReportedConfig);
    }

    public boolean inMultiWindowMode() {
        return this.mActivityRecordBridge.inMultiWindowMode();
    }

    public void schedulePCWindowStateChanged() {
        TaskRecord task = this.mActivityRecordBridge.task;
        WindowProcessController app = this.mActivityRecordBridge.app;
        if (task != null && task.getStack() != null && app != null && app.mThread != null) {
            try {
                app.mThread.schedulePCWindowStateChanged(this.mActivityRecordBridge.getAppToken(), task.getWindowState());
            } catch (RemoteException e) {
                Slog.d("HwActivityRecord", "on schedulePCWindowStateChanged error");
            }
        }
    }

    public int getStackIdFromTask() {
        return this.mActivityRecordBridge.getStackIdFromTask();
    }

    public boolean isTaskEmpty() {
        return this.mActivityRecordBridge.isTaskEmpty();
    }

    public int aospGetTaskId() {
        return this.mActivityRecordBridge.aospGetTaskId();
    }

    public boolean isSameTask() {
        return this.mActivityRecordBridge.isSameTask();
    }

    public int getStackId() {
        return this.mActivityRecordBridge.getStackId();
    }

    public int getWindowState() {
        return 0;
    }

    public Configuration getConfiguration() {
        return this.mActivityRecordBridge.getConfiguration();
    }

    public boolean inHwMagicWindowingMode() {
        return this.mActivityRecordBridge.inHwMagicWindowingMode();
    }

    public void onMultiWindowModeChanged(boolean isModeChanged) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = this.mAtmServiceEx;
        if (activityTaskManagerServiceEx != null) {
            activityTaskManagerServiceEx.onMultiWindowModeChanged(isModeChanged);
        }
    }

    /* access modifiers changed from: protected */
    public int overrideRealConfigChanged(ActivityInfo activityInfo) {
        return 0;
    }

    public String getPackageName() {
        return "";
    }

    public String aospGetPackageName() {
        return this.mActivityRecordBridge.packageName;
    }

    /* access modifiers changed from: protected */
    public void updateOverrideConfiguration() {
        this.mActivityRecordBridge.updateOverrideConfiguration();
    }

    /* access modifiers changed from: protected */
    public boolean isSplitBaseActivity() {
        ActivityRecord activityRecord;
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = this.mAtmServiceEx;
        ActivityRecord lastResumed = activityTaskManagerServiceEx == null ? null : activityTaskManagerServiceEx.getLastResumedActivityRecord();
        return lastResumed != null && lastResumed.isSplitMode() && lastResumed.getTaskRecord() != null && lastResumed.getTaskRecord() == this.mActivityRecordBridge.task && (activityRecord = this.mActivityRecordBridge) == ((ActivityRecordBridge) activityRecord).task.getRootActivity();
    }

    public AppWindowToken getAppWindowToken() {
        return this.mActivityRecordBridge.mAppWindowToken;
    }

    public int getCustomRequestedOrientation() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public TaskRecordEx buildTaskRecordEx() {
        if (this.mActivityRecordBridge.task == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(this.mActivityRecordBridge.task);
        return taskRecordEx;
    }

    public void resizeAppWindowToken() {
        if (getAppWindowToken() != null) {
            getAppWindowToken().resize();
        }
    }

    /* access modifiers changed from: protected */
    public ActivityTaskManagerServiceEx buildAtmsEx() {
        return this.mAtmServiceEx;
    }

    /* access modifiers changed from: protected */
    public void initSplitMode(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public boolean isSplitMode() {
        return false;
    }

    public ActivityInfo getActivityInfo() {
        return this.mActivityRecordBridge.info;
    }

    public boolean isDelayFinished() {
        return false;
    }

    public void setIsDelayFinished(boolean isFinished) {
    }

    /* access modifiers changed from: protected */
    public Bundle getBundle(int policy, int requestedOrientation) {
        return HwMwUtils.performPolicy(policy, new Object[]{this.mActivityRecordBridge.getAppToken(), Integer.valueOf(requestedOrientation)});
    }

    public void updateTaskByRequestedOrientationForPCCast(int taskId, int orientation) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = this.mAtmServiceEx;
        if (activityTaskManagerServiceEx != null) {
            activityTaskManagerServiceEx.updateTaskByRequestedOrientationForPCCast(taskId, orientation);
        }
    }

    public boolean isVirtualDisplayId(String castType) {
        ActivityRecordBridge activityRecordBridge = this.mActivityRecordBridge;
        if (activityRecordBridge == null || this.mAtmServiceEx == null) {
            return false;
        }
        return this.mAtmServiceEx.isVirtualDisplayId(activityRecordBridge.getDisplayId(), castType);
    }

    public Bundle getMagicWindowExtras() {
        return null;
    }

    public void setMagicWindowExtras(Bundle bundle) {
    }
}
