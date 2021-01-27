package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Process;
import android.service.voice.IVoiceInteractionSessionEx;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import java.util.ArrayList;
import java.util.Iterator;

public class TaskRecordBridgeEx {
    private ActivityTaskManagerServiceEx mAtmsEx;
    private ActivityTaskManagerService mService;
    protected ActivityStackEx mStackEx;
    private TaskRecord mTaskRecord;
    private TaskRecordBridge mTaskRecordBridge;

    public TaskRecordBridgeEx(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSessionEx voiceSession, IVoiceInteractorEx voiceInteractor) {
        this.mTaskRecordBridge = new TaskRecordBridge(service, taskId, info, intent, voiceSession, voiceInteractor);
        initBridgeEx();
    }

    public TaskRecordBridgeEx(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        this.mTaskRecordBridge = new TaskRecordBridge(service, taskId, info, intent, taskDescription);
        initBridgeEx();
    }

    public TaskRecordBridgeEx(ActivityTaskManagerServiceEx service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean isRootWasReset, boolean isAutoRemoveRecents, boolean isAskedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecordEx> activities, long lastTimeMoved, boolean isNeverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean isSupportsPictureInPicture, boolean isRealActivitySuspended, boolean isUserSetupComplete, int minWidth, int minHeight) {
        this.mTaskRecordBridge = new TaskRecordBridge(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, isRootWasReset, isAutoRemoveRecents, isAskedCompatMode, userId, effectiveUid, lastDescription, getActivityRecords(activities), lastTimeMoved, isNeverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, isSupportsPictureInPicture, isRealActivitySuspended, isUserSetupComplete, minWidth, minHeight);
        initBridgeEx();
    }

    private void initBridgeEx() {
        TaskRecordBridge taskRecordBridge = this.mTaskRecordBridge;
        if (taskRecordBridge != null) {
            taskRecordBridge.setTaskRecordBridgeEx(this);
            this.mService = this.mTaskRecordBridge.mService;
            ActivityStack stack = this.mTaskRecordBridge.mStack;
            if (stack != null) {
                this.mStackEx = new ActivityStackEx(stack);
            }
            if (this.mService != null) {
                this.mAtmsEx = new ActivityTaskManagerServiceEx();
                this.mAtmsEx.setActivityTaskManagerService(this.mService);
                return;
            }
            return;
        }
        this.mService = null;
        this.mStackEx = null;
    }

    private ArrayList<ActivityRecord> getActivityRecords(ArrayList<ActivityRecordEx> activityRecordExs) {
        if (activityRecordExs == null) {
            return null;
        }
        ArrayList<ActivityRecord> activityRecords = new ArrayList<>();
        Iterator<ActivityRecordEx> it = activityRecordExs.iterator();
        while (it.hasNext()) {
            activityRecords.add(it.next().getActivityRecord());
        }
        return activityRecords;
    }

    public TaskRecordBridge getTaskRecordBridge() {
        return this.mTaskRecordBridge;
    }

    public ActivityInfo getRootActivityInfo() {
        return this.mTaskRecordBridge.mRootActivityInfo;
    }

    public void setRootActivityInfo(ActivityInfo activityInfo) {
        this.mTaskRecordBridge.mRootActivityInfo = activityInfo;
    }

    public ArrayList<ActivityRecordEx> getActivities() {
        return null;
    }

    public ArrayList<ActivityRecordEx> getActivityRecordExs() {
        ArrayList<ActivityRecordEx> activityRecordExs = new ArrayList<>();
        Iterator<ActivityRecord> it = this.mTaskRecordBridge.mActivities.iterator();
        while (it.hasNext()) {
            ActivityRecordEx activityRecordEx = new ActivityRecordEx();
            activityRecordEx.setActivityRecord(it.next());
            activityRecordExs.add(activityRecordEx);
        }
        return activityRecordExs;
    }

    public ActivityRecordEx getTopActivity() {
        ActivityRecord activityRecord;
        TaskRecordBridge taskRecordBridge = this.mTaskRecordBridge;
        if (taskRecordBridge == null || (activityRecord = taskRecordBridge.getTopActivity(true)) == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(activityRecord);
        return activityRecordEx;
    }

    public boolean isHwActivityRecord(ActivityRecordEx activityRecordEx) {
        ActivityRecord activityRecord = null;
        if (activityRecordEx != null) {
            activityRecord = activityRecordEx.getActivityRecord();
        }
        return activityRecord instanceof ActivityRecordBridge;
    }

    public boolean isTopActivityBeHwActivityRecord() {
        return this.mTaskRecordBridge.getTopActivity() instanceof ActivityRecordBridge;
    }

    public ActivityTaskManagerServiceEx getService() {
        TaskRecordBridge taskRecordBridge;
        if (!(this.mAtmsEx != null || (taskRecordBridge = this.mTaskRecordBridge) == null || taskRecordBridge.mService == null)) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mTaskRecordBridge.mService);
        }
        return this.mAtmsEx;
    }

    public int getUserId() {
        return this.mTaskRecordBridge.userId;
    }

    public boolean inFreeformWindowingMode() {
        return this.mTaskRecordBridge.inFreeformWindowingMode();
    }

    public int getWindowState() {
        return this.mTaskRecordBridge.getWindowState();
    }

    public boolean inSplitScreenWindowingMode() {
        return this.mTaskRecordBridge.inSplitScreenWindowingMode();
    }

    public void setWindowState(int windowState) {
    }

    public void setWindowStateEx(int windowState) {
        this.mTaskRecordBridge.setWindowStateEx(windowState);
    }

    public int getTaskId() {
        return this.mTaskRecordBridge.taskId;
    }

    public ActivityStackEx getActivityStack() {
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mTaskRecordBridge.mStack);
        return activityStackEx;
    }

    public Rect getRequestedOverrideBounds() {
        return this.mTaskRecordBridge.getRequestedOverrideBounds();
    }

    public int getNextWindowState() {
        return this.mTaskRecordBridge.mNextWindowState;
    }

    public ActivityStackEx getStackEx() {
        if (this.mTaskRecordBridge.getStack() == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mTaskRecordBridge.getStack());
        return activityStackEx;
    }

    public void setStackEx(ActivityStackEx stack) {
        if (stack != null) {
            this.mTaskRecordBridge.setStackEx(stack.getActivityStack());
        }
    }

    /* access modifiers changed from: package-private */
    public void setStack(ActivityStackEx stack) {
    }

    public int getMinWidth() {
        return this.mTaskRecordBridge.mMinWidth;
    }

    public void setMinWidth(int minWidth) {
        this.mTaskRecordBridge.mMinWidth = minWidth;
    }

    public int getMinHeight() {
        return this.mTaskRecordBridge.mMinHeight;
    }

    public void setMinHeight(int minHeight) {
        this.mTaskRecordBridge.mMinHeight = minHeight;
    }

    public Rect getLastNonFullscreenBounds() {
        return this.mTaskRecordBridge.mLastNonFullscreenBounds;
    }

    public void setLastNonFullscreenBounds(Rect rect) {
        this.mTaskRecordBridge.mLastNonFullscreenBounds = rect;
    }

    public Rect getLaunchBoundsEx() {
        return this.mTaskRecordBridge.getLaunchBoundsEx();
    }

    public Rect getLaunchBounds() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean removeActivity(ActivityRecordEx activityRecordEx, boolean isReparenting) {
        return false;
    }

    public boolean removeActivityEx(ActivityRecordEx activityRecordEx, boolean isReparenting) {
        if (activityRecordEx == null) {
            return false;
        }
        return this.mTaskRecordBridge.removeActivityEx(activityRecordEx, isReparenting);
    }

    /* access modifiers changed from: package-private */
    public void createTask(boolean isOnTop, boolean isShowForAllUsers) {
    }

    /* access modifiers changed from: protected */
    public void createTaskEx(boolean isOnTop, boolean isShowForAllUsers) {
        this.mTaskRecordBridge.createTaskEx(isOnTop, isShowForAllUsers);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
    }

    /* access modifiers changed from: protected */
    public void removeWindowContainerEx() {
        this.mTaskRecordBridge.removeWindowContainerEx();
    }

    /* access modifiers changed from: protected */
    public boolean isResizeableEx(boolean isCheckSupportsPip) {
        return this.mTaskRecordBridge.isResizeableEx(isCheckSupportsPip);
    }

    /* access modifiers changed from: protected */
    public boolean isResizeable(boolean isCheckSupportsPip) {
        return false;
    }

    public int getDefaultMinSize() {
        return this.mTaskRecordBridge.mDefaultMinSize;
    }

    public void setDefaultMinSize(int defaultMinSize) {
        this.mTaskRecordBridge.mDefaultMinSize = defaultMinSize;
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensions(Rect bounds, Rect previousBounds) {
    }

    /* access modifiers changed from: protected */
    public void adjustForMinimalTaskDimensionsEx(Rect bounds, Rect previousBounds) {
        this.mTaskRecordBridge.adjustForMinimalTaskDimensionsEx(bounds, previousBounds);
    }

    public int getStackId() {
        return this.mTaskRecordBridge.getStackId();
    }

    public Configuration getConfiguration() {
        TaskRecordBridge taskRecordBridge = this.mTaskRecordBridge;
        if (taskRecordBridge == null || taskRecordBridge.getParent() == null) {
            return null;
        }
        return this.mTaskRecordBridge.getParent().getConfiguration();
    }

    public Rect getBounds() {
        return this.mTaskRecordBridge.getBounds();
    }

    public int setBounds(Rect bounds) {
        return this.mTaskRecordBridge.setBounds(bounds);
    }

    /* access modifiers changed from: package-private */
    public void addActivityToTop(ActivityRecordEx activityRecordEx) {
    }

    /* access modifiers changed from: protected */
    public void addActivityToTopEx(ActivityRecordEx activityRecordEx) {
        if (activityRecordEx != null) {
            this.mTaskRecordBridge.addActivityToTopEx(activityRecordEx);
        }
    }

    public ApplicationInfo getApplicationInfo() {
        if (getTopActivity() != null) {
            return getService().getPackageManagerInternalLocked().getApplicationInfo(getTopActivity().getPackageName(), 0, Process.myUid(), getUserId());
        }
        return null;
    }

    public void notifyTaskProfileLocked(int taskId, int userId) {
        TaskChangeNotificationController taskChangeController = getService().getHwTaskChangeController();
        if (taskChangeController != null) {
            taskChangeController.notifyTaskProfileLocked(taskId, userId);
        }
    }

    public void scheduleReportPCWindowStateChangedLocked(TaskRecordEx task) {
        if (task != null) {
            getService().getActivityStackSupervisor().scheduleReportPCWindowStateChangedLocked(task.getTaskRecord());
        }
    }

    public void reCalculateDefaultMinimalSizeOfResizeableTasks() {
        getService().getActivityStackSupervisor().reCalculateDefaultMinimalSizeOfResizeableTasks();
    }

    public boolean isSaveBounds() {
        return false;
    }

    public void setSaveBounds(boolean isSaveBounds) {
    }

    public boolean comparemCustomRequestedOrientation(ActivityRecordEx recordEx) {
        ActivityRecordEx topActivity = getTopActivity();
        if (topActivity == null || topActivity.getActivityRecord() == null || recordEx == null || recordEx.getActivityRecord() == null || topActivity.getActivityRecord().getCustomRequestedOrientation() == recordEx.getActivityRecord().getCustomRequestedOrientation()) {
            return false;
        }
        return true;
    }

    public int getCustomRequestedOrientation() {
        ActivityRecordEx topActivity = getTopActivity();
        if (topActivity == null || topActivity.getActivityRecord() == null) {
            return 0;
        }
        return topActivity.getActivityRecord().getCustomRequestedOrientation();
    }

    public void overrideConfigOrienForFreeForm(Configuration config) {
    }

    public TaskRecordEx buildTaskRecordEx() {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(this.mTaskRecordBridge);
        return taskRecordEx;
    }

    /* access modifiers changed from: protected */
    public void updateHwOverrideConfiguration(Rect bounds) {
    }

    /* access modifiers changed from: protected */
    public void updateHwPCMultiCastOverrideConfiguration(Rect bounds) {
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(String packageName) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void activityResumedInTop() {
    }

    /* access modifiers changed from: protected */
    public ActivityStackEx getActivityStackEx() {
        ActivityStackEx activityStackEx = this.mStackEx;
        if (activityStackEx != null) {
            return activityStackEx;
        }
        return null;
    }

    public Rect aospGetRequestedOverrideBounds() {
        return this.mTaskRecordBridge.aospGetRequestedOverrideBounds();
    }

    public int aospGetNextWindowState() {
        return this.mTaskRecordBridge.mNextWindowState;
    }

    public String aospGetCallingPackage() {
        return this.mTaskRecordBridge.mCallingPackage;
    }

    public int aospGetTaskId() {
        return this.mTaskRecordBridge.taskId;
    }

    public int getDragFullMode() {
        return 0;
    }

    public void setDragFullMode(int mode) {
    }

    public void adjustProcessGlobalConfigLocked(TaskRecordBridgeEx tr, Rect rect, int windowMode) {
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null) {
            this.mService.mHwATMSEx.adjustProcessGlobalConfigLocked(tr.getTaskRecordBridge(), rect, windowMode);
        }
    }

    /* access modifiers changed from: protected */
    public HwMultiDisplayManager getHwMultiDisplayManager() {
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (activityTaskManagerService != null) {
            return HwMultiDisplayManager.getInstance(activityTaskManagerService);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void onNotifyTaskModeChange(int taskId, Rect bounds) {
        ActivityTaskManagerService activityTaskManagerService = this.mService;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.mHwATMSEx.onWindowModeChange(taskId, bounds);
        }
    }

    public ActivityStackEx aospGetStack() {
        TaskRecordBridge taskRecordBridge = this.mTaskRecordBridge;
        if (taskRecordBridge == null || taskRecordBridge.aospGetStack() == null) {
            return null;
        }
        return new ActivityStackEx(this.mTaskRecordBridge.aospGetStack());
    }

    public Bundle getMagicWindowExtras() {
        return null;
    }

    public void setMagicWindowExtras(Bundle bundle) {
    }
}
