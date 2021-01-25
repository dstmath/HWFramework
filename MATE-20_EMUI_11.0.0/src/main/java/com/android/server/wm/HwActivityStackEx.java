package com.android.server.wm;

import android.content.Context;
import android.content.res.HwPCMultiWindowCompatibility;
import android.freeform.HwFreeFormUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.text.TextUtils;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.widget.Toast;
import com.android.server.UiThread;
import com.huawei.server.HwPCFactory;
import com.huawei.server.wm.IHwActivityStackEx;

public class HwActivityStackEx implements IHwActivityStackEx {
    private static final String INCALLUI_PKG = "com.android.incallui";
    private static final int MAX_TASK_NUM = SystemProperties.getInt("ro.config.pc_mode_win_num", 8);
    private static final String TAG = "HwActivityStackEx";
    private boolean mIsHiddenFromHome = false;
    private boolean mIsStackVisible = true;
    private boolean mIsStackVisibleBeforeHidden = false;
    final ActivityTaskManagerService mService;
    final ActivityStack mStack;

    public HwActivityStackEx(ActivityStack stack, ActivityTaskManagerService service) {
        this.mStack = stack;
        this.mService = service;
    }

    public void makeStackVisible(boolean isVisible) {
        ActivityStack activityStack = this.mStack;
        if (activityStack != null && HwPCUtils.isPcDynamicStack(activityStack.getStackId())) {
            synchronized (this.mService.mGlobalLock) {
                this.mIsStackVisible = isVisible;
                if (!this.mService.mVrMananger.isVRDynamicStack(this.mStack.getStackId())) {
                    if (isVisible) {
                        this.mIsHiddenFromHome = false;
                        if (this.mStack.mTaskHistory.size() > 0) {
                            this.mService.mHwATMSEx.getHwTaskChangeController().notifyTaskMovedToFront(((TaskRecord) this.mStack.mTaskHistory.get(0)).getTaskInfo());
                        }
                    } else {
                        onTaskMovedToFront(-1);
                        this.mService.mHwATMSEx.updateUsageStatsForPCMode(this.mStack.getTopActivity(), isVisible, this.mService.mUsageStatsInternal);
                    }
                }
            }
        }
    }

    public boolean moveTaskToBackEx(int taskId) {
        if (this.mStack == null) {
            return false;
        }
        if ((!this.mService.mVrMananger.isVRDeviceConnected() || this.mService.mVrMananger.isVirtualScreenMode()) && !HwPCUtils.isExtDynamicStack(this.mStack.getStackId())) {
            return false;
        }
        this.mService.mHwATMSEx.getTaskSnapshot(taskId, false);
        makeStackVisible(false);
        if (HwPCUtils.enabledInPad() && hasFullscreenTaskInPad()) {
            makeStackVisibleInPad(true, this.mStack.mDisplayId, this.mStack.getStackId());
        }
        setStackVisibleBeforeHidden(false);
        ActivityStack activityStack = this.mStack;
        activityStack.ensureActivitiesVisibleLocked(activityStack.topRunningActivityLocked(), 0, false);
        this.mStack.adjustFocusToNextFocusableStack("minTask");
        if (HwPCUtils.isHiCarCastMode()) {
            HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwHiCarMultiWindowManager().onMoveTaskToBack(taskId);
        }
        return true;
    }

    public void moveTaskToFrontEx(TaskRecord tr) {
        if (HwPCUtils.isExtDynamicStack(this.mStack.getStackId())) {
            makeStackVisible(true);
        }
        processStackStateIfNeed(tr);
    }

    public void moveToFrontEx(String reason, TaskRecord task) {
        ActivityStack activityStack = this.mStack;
        if (activityStack != null && HwPCUtils.isExtDynamicStack(activityStack.getStackId())) {
            if (HwPCUtils.enabledInPad() && hasFullscreenTaskInPad()) {
                makeStackVisibleInPad(false, this.mStack.mDisplayId, this.mStack.getStackId());
            }
            makeStackVisible(true);
            minimalLRUTaskIfNeed();
        }
    }

    public void resetOtherStacksVisible(boolean isVisible) {
        if (this.mStack != null && HwPCUtils.enabledInPad() && HwPCUtils.isPcDynamicStack(this.mStack.getStackId()) && HwPCUtils.isPcCastModeInServer() && hasFullscreenTaskInPad()) {
            makeStackVisibleInPad(isVisible, this.mStack.mDisplayId, this.mStack.getStackId());
        }
    }

    private boolean hasFullscreenTaskInPad() {
        for (int i = this.mStack.mTaskHistory.size() - 1; i >= 0; i--) {
            int windowState = ((TaskRecord) this.mStack.mTaskHistory.get(i)).getWindowState();
            if (HwPCMultiWindowCompatibility.isLayoutFullscreen(windowState) || HwPCMultiWindowCompatibility.isLayoutMaximized(windowState)) {
                return true;
            }
        }
        return false;
    }

    private void makeStackVisibleInPad(boolean isVisible, int displayId, int currentStackId) {
        ActivityDisplay activityDisplay = null;
        int i = this.mService.mRootActivityContainer.getChildCount() - 1;
        while (true) {
            if (i < 0) {
                break;
            } else if (this.mService.mRootActivityContainer.getChildAt(i).mDisplayId == displayId) {
                activityDisplay = this.mService.mRootActivityContainer.getChildAt(i);
                break;
            } else {
                i--;
            }
        }
        if (activityDisplay == null) {
            HwPCUtils.log(TAG, "Display with displayId=" + displayId + " not found.");
            return;
        }
        for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = activityDisplay.getChildAt(stackNdx);
            if (stack.getStackId() != currentStackId) {
                if (!isVisible || (stack.mHwActivityStackEx.getStackVisibleBeforeHidden() && !stack.mHwActivityStackEx.isInCallActivityStack())) {
                    if (!isVisible && stack.mHwActivityStackEx.getStackVisible()) {
                        HwPCUtils.log(TAG, "makeStackVisibleInPad stack=" + stack + " make invisible because the top activity is fullscreen ,StackVisibleBeforeHidden=" + stack.mHwActivityStackEx.getStackVisibleBeforeHidden());
                        stack.mHwActivityStackEx.setStackVisibleBeforeHidden(true);
                    }
                    stack.mHwActivityStackEx.makeStackVisible(isVisible);
                    if (isVisible) {
                        stack.mHwActivityStackEx.setStackVisibleBeforeHidden(false);
                    }
                } else {
                    HwPCUtils.log(TAG, "makeStackVisibleInPad stack=" + stack + " Skipping: is invisible before launch fullscreen activity or this stack contains InCallUI activity ,StackVisibleBeforeHidden=" + stack.mHwActivityStackEx.getStackVisibleBeforeHidden());
                }
            }
        }
    }

    public boolean getStackVisibleBeforeHidden() {
        return this.mIsStackVisibleBeforeHidden;
    }

    public void setStackVisibleBeforeHidden(boolean isVisible) {
        this.mIsStackVisibleBeforeHidden = isVisible;
    }

    public boolean isInCallActivityStack() {
        ActivityStack activityStack = this.mStack;
        if (activityStack == null) {
            return false;
        }
        for (int i = activityStack.mTaskHistory.size() - 1; i >= 0; i--) {
            ActivityRecord topActivity = ((TaskRecord) this.mStack.mTaskHistory.get(i)).getTopActivity();
            if (topActivity != null) {
                String pkgName = topActivity.packageName;
                if (pkgName == null || !pkgName.startsWith(INCALLUI_PKG)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private void minimalLRUTaskIfNeed() {
        final String title;
        if (HwPCUtils.isExtDynamicStack(this.mStack.getStackId())) {
            int visibleNum = 0;
            TaskRecord lastVisibleTask = null;
            int N = this.mStack.getDisplay().getChildCount();
            for (int i = 0; i < N; i++) {
                ActivityStack stack = this.mStack.getDisplay().getChildAt(i);
                if (stack.shouldBeVisible((ActivityRecord) null)) {
                    if (lastVisibleTask == null && stack.topTask() != null) {
                        lastVisibleTask = stack.topTask();
                    }
                    if (lastVisibleTask != null) {
                        visibleNum++;
                        if (visibleNum > MAX_TASK_NUM && lastVisibleTask.mStack != null) {
                            HwPCUtils.log(TAG, "max task num, minimial the task: " + lastVisibleTask.taskId);
                            this.mService.mHwATMSEx.moveTaskBackwards(lastVisibleTask.taskId);
                            final Context context = HwPCUtils.getDisplayContext(this.mService.mContext, lastVisibleTask.mStack.mDisplayId);
                            if (context != null) {
                                ActivityRecord ar = lastVisibleTask.getRootActivity();
                                if (ar == null || ar.info == null) {
                                    title = null;
                                } else {
                                    title = ar.info.loadLabel(context.getPackageManager()).toString();
                                }
                                if (!TextUtils.isEmpty(title)) {
                                    UiThread.getHandler().post(new Runnable() {
                                        /* class com.android.server.wm.HwActivityStackEx.AnonymousClass1 */

                                        @Override // java.lang.Runnable
                                        public void run() {
                                            Context context = context;
                                            Toast.makeText(context, context.getString(33685972, title), 0).show();
                                        }
                                    });
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    private void processStackStateIfNeed(TaskRecord tr) {
        ActivityStack stack;
        if (HwFreeFormUtils.isFreeFormEnable() && (stack = this.mService.mStackSupervisor.mRootActivityContainer.getStack(5, 1)) != null) {
            if (tr.getWindowingMode() == 5) {
                stack.setFreeFormStackVisible(true);
            } else if (this.mStack.getWindowingMode() == 1) {
                if (!(tr.getTopActivity() == null || tr.topRunningActivityLocked().packageName == null || stack.topRunningActivityLocked() == null)) {
                    String pkgName = tr.topRunningActivityLocked().packageName;
                    if (pkgName.equals(stack.topRunningActivityLocked().packageName)) {
                        HwFreeFormUtils.log(TAG, "keep freeform for move the same app as freeform-app :" + pkgName + " to front in fullscreen");
                    } else if (!stack.getCurrentPkgUnderFreeForm().equals(pkgName) && !tr.isActivityTypeHome()) {
                        HwFreeFormUtils.log(TAG, "remove freeform for move other pkg :" + pkgName + " to front in fullscreen");
                        stack.setFreeFormStackVisible(false);
                        stack.setCurrentPkgUnderFreeForm("");
                        stack.finishAllActivitiesLocked(true);
                    }
                }
            }
        }
    }

    public boolean getStackVisible() {
        return this.mIsStackVisible;
    }

    public boolean getHiddenFromHome() {
        return this.mIsHiddenFromHome;
    }

    public void setHiddenFromHome(boolean isHidden) {
        this.mIsHiddenFromHome = isHidden;
    }

    private void onTaskMovedToFront(int taskId) {
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.onTaskMovedToFront(taskId);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "onTaskMovedToFront RemoteException");
            }
        }
    }

    public boolean shouldSkipPausing(ActivityRecord resumed, ActivityRecord resuming, int staskId) {
        if (resumed == null || resuming == null) {
            return false;
        }
        if (HwMwUtils.ENABLED && resumed.inHwMagicWindowingMode()) {
            return !HwMwUtils.performPolicy(14, new Object[]{resumed.appToken, resuming.appToken}).getBoolean("CAN_PAUSE", true);
        }
        if (!resumed.inCoordinationPrimaryWindowingMode() || !resuming.inCoordinationSecondaryWindowingMode()) {
            return false;
        }
        return true;
    }
}
