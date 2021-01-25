package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSenderEx;
import android.content.Intent;
import android.content.pm.PackageManagerInternal;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.SparseArray;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.server.am.ProcessListEx;
import java.util.ArrayList;
import java.util.List;

public class ActivityTaskManagerServiceEx {
    private ActivityTaskManagerService mATMS;
    private ActivityStackSupervisorEx mActivityStackSupervisorEx;
    private RootActivityContainerEx mRootEx;
    private WindowManagerServiceEx mWindowManagerServiceEx;

    public ActivityTaskManagerServiceEx() {
    }

    public ActivityTaskManagerServiceEx(ActivityTaskManagerService service) {
        this.mATMS = service;
    }

    public ActivityTaskManagerService getActivityTaskManagerService() {
        return this.mATMS;
    }

    public void setActivityTaskManagerService(ActivityTaskManagerService activityTaskManagerService) {
        this.mATMS = activityTaskManagerService;
    }

    public void onMultiWindowModeChanged(boolean isModeChanged) {
        this.mATMS.onMultiWindowModeChanged(isModeChanged);
    }

    public ActivityRecord getLastResumedActivityRecord() {
        return this.mATMS.getLastResumedActivityRecord();
    }

    public Context getContext() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null) {
            return null;
        }
        return activityTaskManagerService.mContext;
    }

    public TaskRecordEx anyTaskForId(int taskId) {
        TaskRecord taskRecord;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mStackSupervisor == null || this.mATMS.mStackSupervisor.mRootActivityContainer == null || (taskRecord = this.mATMS.mStackSupervisor.mRootActivityContainer.anyTaskForId(taskId)) == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(taskRecord);
        return taskRecordEx;
    }

    public DisplayContentEx getDisplayContentEx(int displayId) {
        DisplayContent displayContent;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mWindowManager == null || this.mATMS.mWindowManager.getRoot() == null || (displayContent = this.mATMS.mWindowManager.getRoot().getDisplayContent(displayId)) == null) {
            return null;
        }
        DisplayContentEx displayContentEx = new DisplayContentEx();
        displayContentEx.setDisplayContent(displayContent);
        return displayContentEx;
    }

    public boolean isHwWindowManagerService() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mWindowManager == null) {
            return false;
        }
        return this.mATMS.mWindowManager instanceof HwWindowManagerService;
    }

    public DisplayManager getDisplayManager() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mWindowManager == null || !(this.mATMS.mWindowManager instanceof HwWindowManagerService)) {
            return null;
        }
        return this.mATMS.mWindowManager.getDisplayManager();
    }

    public Looper getLooper() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mH == null) {
            return null;
        }
        return this.mATMS.mH.getLooper();
    }

    public void moveTaskBackwards(int toBackTaskId) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null) {
            this.mATMS.mHwATMSEx.moveTaskBackwards(toBackTaskId);
        }
    }

    public void notifyPopupCamera(String shortCompName) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null) {
            this.mATMS.mHwATMSEx.notifyPopupCamera(shortCompName);
        }
    }

    public Object getGlobalLock() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null) {
            return new Object();
        }
        return activityTaskManagerService.mGlobalLock;
    }

    public ActivityDisplayEx getActivityDisplayEx(int displayId) {
        ActivityDisplay activityDisplay;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mRootActivityContainer == null || (activityDisplay = this.mATMS.mRootActivityContainer.getActivityDisplay(displayId)) == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(activityDisplay);
        return activityDisplayEx;
    }

    public ActivityDisplayEx getActivityDisplayExFromStackSupervisorByIndex(int index) {
        ActivityDisplay activityDisplay;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mStackSupervisor == null || this.mATMS.mStackSupervisor.mRootActivityContainer == null || (activityDisplay = this.mATMS.mStackSupervisor.mRootActivityContainer.getChildAt(index)) == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(activityDisplay);
        return activityDisplayEx;
    }

    public int getSizeOfActivityDisplayFromStackSupervisor() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mStackSupervisor == null || this.mATMS.mStackSupervisor.mRootActivityContainer == null) {
            return 0;
        }
        return this.mATMS.mStackSupervisor.mRootActivityContainer.getChildCount();
    }

    public WindowProcessControllerEx getProcessControllerEx(int pid, int uid) {
        WindowProcessController wpc;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || (wpc = activityTaskManagerService.getProcessController(pid, uid)) == null) {
            return null;
        }
        WindowProcessControllerEx windowProcessControllerEx = new WindowProcessControllerEx();
        windowProcessControllerEx.setWindowProcessController(wpc);
        return windowProcessControllerEx;
    }

    public SparseArray<WindowProcessControllerEx> getPidMap() {
        SparseArray<WindowProcessControllerEx> controllerExSparseArray = new SparseArray<>();
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            SparseArray<WindowProcessController> controllerSparseArray = activityTaskManagerService.mProcessMap.getPidMap();
            if (controllerSparseArray.size() == 0) {
                return controllerExSparseArray;
            }
            for (int i = 0; i < controllerSparseArray.size(); i++) {
                WindowProcessControllerEx controllerEx = new WindowProcessControllerEx();
                controllerEx.setWindowProcessController(controllerSparseArray.valueAt(i));
                controllerExSparseArray.put(i, controllerEx);
            }
        }
        return controllerExSparseArray;
    }

    public RootActivityContainerEx getRootActivityContainer() {
        ActivityTaskManagerService activityTaskManagerService;
        if (!(this.mRootEx != null || (activityTaskManagerService = this.mATMS) == null || activityTaskManagerService.mRootActivityContainer == null)) {
            this.mRootEx = new RootActivityContainerEx();
            this.mRootEx.setRootActivityContainer(this.mATMS.mRootActivityContainer);
        }
        return this.mRootEx;
    }

    public boolean moveActivityTaskToBack(IBinder token, boolean isNonRoot) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null) {
            return false;
        }
        return activityTaskManagerService.moveActivityTaskToBack(token, isNonRoot);
    }

    public ClientLifecycleManagerEx getLifecycleManager() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.getLifecycleManager() == null) {
            return null;
        }
        ClientLifecycleManagerEx clientLifecycleManagerEx = new ClientLifecycleManagerEx();
        clientLifecycleManagerEx.setClientLifecycleManager(this.mATMS.getLifecycleManager());
        return clientLifecycleManagerEx;
    }

    public ActivityDisplayEx getDefaultDisplayEx() {
        ActivityDisplay activityDisplay;
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mRootActivityContainer == null || (activityDisplay = this.mATMS.mRootActivityContainer.getDefaultDisplay()) == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(activityDisplay);
        return activityDisplayEx;
    }

    public RecentTasksEx getRecentTasksEx() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mRecentTasks == null) {
            return null;
        }
        RecentTasksEx recentTasksEx = new RecentTasksEx();
        recentTasksEx.setRecentTasks(this.mATMS.mRecentTasks);
        return recentTasksEx;
    }

    public Handler getMH() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            return activityTaskManagerService.mH;
        }
        return null;
    }

    public void removeStack(int stackId) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.removeStack(stackId);
        }
    }

    public void removeStackFromStackSupervisor(ActivityStackEx stackEx) {
        if (this.mATMS != null && stackEx != null && stackEx.getActivityStack() != null) {
            this.mATMS.mStackSupervisor.removeStack(stackEx.getActivityStack());
        }
    }

    public void removeTaskByIdLockedFromStackSupervisor(int taskId, boolean isKillProcess, boolean isRemoveFromRecents, boolean isPauseImmediately, String reason) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.mStackSupervisor.removeTaskByIdLocked(taskId, isKillProcess, isRemoveFromRecents, isPauseImmediately, reason);
        }
    }

    public int getNextTaskIdForUserLockedFromStackSupervisor(int userId) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            return activityTaskManagerService.getStackSupervisor().getNextTaskIdForUserLocked(userId);
        }
        return 0;
    }

    public void startActivityFromRecents(int taskId, Bundle bOptions) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.startActivityFromRecents(taskId, bOptions);
        }
    }

    public void setFocusedStack(int taskId) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.setFocusedStack(taskId);
        }
    }

    public static HwTaskSnapshotWrapper getTaskSnapshot(HwActivityTaskManagerServiceEx hwAtmsEx, int taskId, boolean isReducedResolution) {
        if (hwAtmsEx == null) {
            return null;
        }
        ActivityManager.TaskSnapshot tss = hwAtmsEx.getTaskSnapshot(taskId, isReducedResolution);
        HwTaskSnapshotWrapper hwTaskSnapshotWrapper = new HwTaskSnapshotWrapper();
        hwTaskSnapshotWrapper.setTaskSnapshot(tss);
        return hwTaskSnapshotWrapper;
    }

    public void scheduleIdleLockedFromStackSupervisor() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            activityTaskManagerService.getStackSupervisor().scheduleIdleLocked();
        }
    }

    public KeyguardControllerEx getKeyguardControllerFromStackSupervisor() {
        if (this.mATMS == null) {
            return null;
        }
        KeyguardControllerEx keyguardControllerEx = new KeyguardControllerEx();
        keyguardControllerEx.setKeyguardController(this.mATMS.getStackSupervisor().getKeyguardController());
        return keyguardControllerEx;
    }

    public Context getUiContext() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            return activityTaskManagerService.mUiContext;
        }
        return null;
    }

    public boolean isGameDndOn() {
        return this.mATMS.mHwATMSEx.isGameDndOn();
    }

    public IIntentSenderEx getIntentSenderLocked(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options) {
        IIntentSenderEx iIntentSenderEx = new IIntentSenderEx();
        iIntentSenderEx.setIntentSender(this.mATMS.getIntentSenderLocked(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, options));
        return iIntentSenderEx;
    }

    public Intent getHomeIntent() {
        return this.mATMS.getHomeIntent();
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mATMS.showUninstallLauncherDialog(pkgName);
    }

    public boolean isSameGroupForClone(int callingUserId, int targetUserId) {
        return this.mATMS.mUserManagerInternal.isSameGroupForClone(callingUserId, targetUserId);
    }

    public boolean isVrMode() {
        return this.mATMS.mVrMananger.isVRMode();
    }

    public boolean isInMWPortraitWhiteList(String packageName) {
        return getPackageManagerInternalLocked().isInMWPortraitWhiteList(packageName);
    }

    public ActivityStackSupervisorBridge getActivityStackSupervisor() {
        return this.mATMS.mStackSupervisor;
    }

    public boolean isHwActivityStackSupervisor() {
        return this.mATMS.mStackSupervisor instanceof ActivityStackSupervisorBridge;
    }

    public WindowManagerServiceEx getWindowManager() {
        ActivityTaskManagerService activityTaskManagerService;
        if (!(this.mWindowManagerServiceEx != null || (activityTaskManagerService = this.mATMS) == null || activityTaskManagerService.mWindowManager == null)) {
            this.mWindowManagerServiceEx = new WindowManagerServiceEx();
            this.mWindowManagerServiceEx.setWindowManagerService(this.mATMS.mWindowManager);
        }
        return this.mWindowManagerServiceEx;
    }

    public void notifyTaskCreated(int taskId, ComponentName componentName) {
        TaskChangeNotificationController controller = getHwTaskChangeController();
        if (controller != null) {
            controller.notifyTaskCreated(taskId, componentName);
        }
    }

    public Configuration getGlobalConfiguration() {
        return this.mATMS.getConfiguration();
    }

    public void notifyTaskRemoved(int taskId) {
        TaskChangeNotificationController controller = getHwTaskChangeController();
        if (controller != null) {
            controller.notifyTaskRemoved(taskId);
        }
    }

    public PackageManagerInternal getPackageManagerInternalLocked() {
        return this.mATMS.getPackageManagerInternalLocked();
    }

    public TaskChangeNotificationController getHwTaskChangeController() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mHwATMSEx == null) {
            return null;
        }
        return this.mATMS.mHwATMSEx.getHwTaskChangeController();
    }

    public boolean isAtmsNull() {
        return this.mATMS == null;
    }

    public ActivityStackSupervisorEx getActivityStackSupervisorEx() {
        ActivityTaskManagerService activityTaskManagerService;
        if (!(this.mActivityStackSupervisorEx != null || (activityTaskManagerService = this.mATMS) == null || activityTaskManagerService.mStackSupervisor == null)) {
            this.mActivityStackSupervisorEx = new ActivityStackSupervisorEx();
            this.mActivityStackSupervisorEx.setActivityStackSupervisor(this.mATMS.mStackSupervisor);
        }
        return this.mActivityStackSupervisorEx;
    }

    public final boolean post(Runnable runnable) {
        return this.mATMS.mH.post(runnable);
    }

    public int getCurrentUserId() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null) {
            return activityTaskManagerService.getCurrentUserId();
        }
        return ProcessListEx.INVALID_ADJ;
    }

    public boolean isEmpty() {
        return this.mATMS == null;
    }

    public void maximizeHwFreeForm() {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null) {
            this.mATMS.mHwATMSEx.maximizeHwFreeForm();
        }
    }

    public void dispatchFreeformBallLifeState(List<TaskRecordEx> tasks, String state) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null && tasks != null) {
            this.mATMS.mHwATMSEx.dispatchFreeformBallLifeState(getTaskRecords(tasks), state);
        }
    }

    private List<TaskRecord> getTaskRecords(List<TaskRecordEx> taskRecordExes) {
        if (taskRecordExes == null) {
            return new ArrayList(1);
        }
        List<TaskRecord> taskRecords = new ArrayList<>(1);
        for (TaskRecordEx taskRecordEx : taskRecordExes) {
            taskRecords.add(taskRecordEx.getTaskRecord());
        }
        return taskRecords;
    }

    public void updateTaskByRequestedOrientationForPCCast(int taskId, int orientation) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService != null && activityTaskManagerService.mHwATMSEx != null) {
            this.mATMS.mHwATMSEx.updateTaskByRequestedOrientationForPCCast(taskId, orientation);
        }
    }

    public void onEnteringSingleHandForMultiDisplay() {
        this.mATMS.mHwATMSEx.onEnteringSingleHandForMultiDisplay();
    }

    public boolean isVirtualDisplayId(int displayId, String castType) {
        ActivityTaskManagerService activityTaskManagerService = this.mATMS;
        if (activityTaskManagerService == null || activityTaskManagerService.mHwATMSEx == null) {
            return false;
        }
        return this.mATMS.mHwATMSEx.isVirtualDisplayId(displayId, castType);
    }
}
