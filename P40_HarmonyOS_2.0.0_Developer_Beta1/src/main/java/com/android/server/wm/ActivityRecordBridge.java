package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.IApplicationToken;

public class ActivityRecordBridge extends ActivityRecord {
    private ActivityRecordBridgeEx mActivityRecordEx;

    public ActivityRecordBridge(ActivityTaskManagerService service, WindowProcessController caller, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo activityInfo, Configuration configuration, ActivityRecord resultTo, String resultWho, int reqCode, boolean isComponentSpecified, boolean isRootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        super(service, caller, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, activityInfo, configuration, resultTo, resultWho, reqCode, isComponentSpecified, isRootVoiceInteraction, supervisor, options, sourceRecord);
    }

    public void setActivityRecordEx(ActivityRecordBridgeEx activityRecordEx) {
        this.mActivityRecordEx = activityRecordEx;
    }

    public IApplicationToken.Stub getAppToken() {
        return this.appToken;
    }

    /* access modifiers changed from: protected */
    public void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        this.mActivityRecordEx.scheduleMultiWindowModeChanged(overrideConfig);
    }

    /* access modifiers changed from: protected */
    public void aospScheduleMultiWindowModeChanged(Configuration overrideConfig) {
        ActivityRecordBridge.super.scheduleMultiWindowModeChanged(overrideConfig);
    }

    public boolean isFullScreenVideoInLandscape() {
        return this.mActivityRecordEx.isFullScreenVideoInLandscape();
    }

    public void setFullScreenVideoInLandscape(boolean isLandscape) {
        this.mActivityRecordEx.setFullScreenVideoInLandscape(isLandscape);
    }

    public boolean isFromFullscreenToMagicWin() {
        return this.mActivityRecordEx.isFromFullscreenToMagicWin();
    }

    public void setFromFullscreenToMagicWin(boolean isFromFullscreenToMagicWin) {
        this.mActivityRecordEx.setFromFullscreenToMagicWin(isFromFullscreenToMagicWin);
    }

    public boolean isFinishAllRightBottom() {
        return this.mActivityRecordEx.isFinishAllRightBottom();
    }

    public void setFinishAllRightBottom(boolean isFinishAllRightBottom) {
        this.mActivityRecordEx.setFinishAllRightBottom(isFinishAllRightBottom);
    }

    public boolean isAniRunningBelow() {
        return this.mActivityRecordEx.isAniRunningBelow();
    }

    public void setIsAniRunningBelow(boolean isAniRunningBelow) {
        this.mActivityRecordEx.setIsAniRunningBelow(isAniRunningBelow);
    }

    public void setIsStartFromLauncher(boolean isStartFromLauncher) {
        this.mActivityRecordEx.setIsStartFromLauncher(isStartFromLauncher);
    }

    public boolean isStartFromLauncher() {
        return this.mActivityRecordEx.isStartFromLauncher();
    }

    public Rect getLastBound() {
        return this.mActivityRecordEx.getLastBound();
    }

    public void setLastBound(Rect lastBound) {
        this.mActivityRecordEx.setLastBound(lastBound);
    }

    public int getMagicWindowPageType() {
        return this.mActivityRecordEx.getMagicWindowPageType();
    }

    public void setMagicWindowPageType(int magicWindowPageType) {
        this.mActivityRecordEx.setMagicWindowPageType(magicWindowPageType);
    }

    public long getCreateTime() {
        return this.mActivityRecordEx.getCreateTime();
    }

    public void setCreateTime(long time) {
        this.mActivityRecordEx.setCreateTime(time);
    }

    public void resize() {
        this.mActivityRecordEx.resize();
    }

    public int getLastActivityHash() {
        return this.mActivityRecordEx.getLastActivityHash();
    }

    public void setLastActivityHash(int hashValue) {
        this.mActivityRecordEx.setLastActivityHash(hashValue);
    }

    public int getCustomRequestedOrientation() {
        return this.mActivityRecordEx.getCustomRequestedOrientation();
    }

    /* access modifiers changed from: protected */
    public void computeBounds(Rect outBounds, Rect containingAppBounds) {
        this.mActivityRecordEx.computeBounds(outBounds, containingAppBounds);
    }

    /* access modifiers changed from: protected */
    public void aospComputeBounds(Rect outBounds, Rect containingAppBounds) {
        ActivityRecordBridge.super.computeBounds(outBounds, containingAppBounds);
    }

    public int getStackIdFromTask() {
        if (this.task != null) {
            return this.task.getStackId();
        }
        return -1;
    }

    public boolean isTaskEmpty() {
        return this.task == null;
    }

    public int aospGetTaskId() {
        if (this.task != null) {
            return this.task.taskId;
        }
        return -1;
    }

    public void schedulePCWindowStateChanged() {
        this.mActivityRecordEx.schedulePCWindowStateChanged();
    }

    public int getWindowState() {
        return this.mActivityRecordEx.getWindowState();
    }

    public void setRequestedOrientation(int requestedOrientation) {
        this.mActivityRecordEx.setRequestedOrientation(requestedOrientation);
    }

    public void setAospRequestedOrientation(int requestedOrientation) {
        ActivityRecordBridge.super.setRequestedOrientation(requestedOrientation);
    }

    /* access modifiers changed from: protected */
    public boolean isForceRotationMode(String packageName, Intent intent) {
        return this.mActivityRecordEx.isForceRotationMode(packageName, intent);
    }

    /* access modifiers changed from: protected */
    public int overrideRealConfigChanged(ActivityInfo activityInfo) {
        return this.mActivityRecordEx.overrideRealConfigChanged(activityInfo);
    }

    /* access modifiers changed from: protected */
    public int getConfigurationChanges(Configuration lastReportedConfig) {
        return this.mActivityRecordEx.getConfigurationChanges(lastReportedConfig);
    }

    /* access modifiers changed from: protected */
    public int getAospConfigurationChanges(Configuration lastReportedConfig) {
        return ActivityRecordBridge.super.getConfigurationChanges(lastReportedConfig);
    }

    public String getPackageName() {
        return this.mActivityRecordEx.getPackageName();
    }

    /* access modifiers changed from: protected */
    public boolean isSplitBaseActivity() {
        return this.mActivityRecordEx.isSplitBaseActivity();
    }

    public boolean isSameTask() {
        if (this.task != null && this.task.getTopActivity() == this) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void initSplitMode(Intent intent) {
        this.mActivityRecordEx.initSplitMode(intent);
    }

    /* access modifiers changed from: protected */
    public boolean isSplitMode() {
        return this.mActivityRecordEx.isSplitMode();
    }

    public boolean isDelayFinished() {
        return this.mActivityRecordEx.isDelayFinished();
    }

    public void setIsDelayFinished(boolean isFinished) {
        this.mActivityRecordEx.setIsDelayFinished(isFinished);
    }

    public Bundle getMagicWindowExtras() {
        return this.mActivityRecordEx.getMagicWindowExtras();
    }

    public void setMagicWindowExtras(Bundle bundle) {
        this.mActivityRecordEx.setMagicWindowExtras(bundle);
    }
}
