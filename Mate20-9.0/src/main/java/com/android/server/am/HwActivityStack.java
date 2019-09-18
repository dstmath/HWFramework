package com.android.server.am;

import android.app.ActivityOptions;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.freeform.HwFreeFormUtils;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import com.android.server.UiThread;
import com.android.server.wm.IntelliServiceManager;
import java.util.ArrayList;

public class HwActivityStack extends ActivityStack implements IHwActivityStack {
    static final int MAX_TASK_NUM = SystemProperties.getInt("ro.config.pc_mode_win_num", 8);
    private static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    boolean mHiddenFromHome = false;
    private boolean mStackVisible = true;
    private boolean mStackVisibleBeforeHidden = false;
    private IVRSystemServiceManager mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();

    public HwActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        super(display, stackId, supervisor, windowingMode, activityType, onTop);
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        if (newConfig == null || naviConfig == null) {
            return changes;
        }
        if (mIsHwNaviBar) {
            int newChanges = naviConfig.diff(newConfig);
            if ((newChanges & 1280) == 0) {
                changes &= -1281;
            } else if ((newChanges & 128) != 0) {
                if (changes == 1280 || changes == 1024) {
                    changes &= -1025;
                }
                changes &= -257;
            }
        }
        return changes;
    }

    /* access modifiers changed from: package-private */
    public void moveHomeStackTaskToTop() {
        HwActivityStack.super.moveHomeStackTaskToTop();
        this.mService.checkIfScreenStatusRequestAndSendBroadcast();
        if (HwFreeFormUtils.isFreeFormEnable()) {
            this.mStackSupervisor.mHwActivityStackSupervisorEx.removeFreeFromStackLocked();
        }
    }

    public boolean isSplitActivity(Intent intent) {
        return (intent == null || (intent.getHwFlags() & 4) == 0) ? false : true;
    }

    public void resumeCustomActivity(ActivityRecord next) {
        if (next != null) {
            this.mService.mHwAMSEx.customActivityResuming(next.packageName);
            this.mService.customActivityResuming(next.packageName);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x004e, code lost:
        return;
     */
    public void makeStackVisible(boolean visible) {
        synchronized (this.mService) {
            this.mStackVisible = visible;
            if (!this.mVrMananger.isVRDynamicStack(getStackId())) {
                if (visible) {
                    this.mHiddenFromHome = false;
                    if (this.mTaskHistory.size() > 0) {
                        this.mService.getHwTaskChangeController().notifyTaskMovedToFront(((TaskRecord) this.mTaskHistory.get(0)).taskId);
                    }
                } else {
                    this.mService.getHwTaskChangeController().notifyTaskMovedToFront(-1);
                    this.mService.mHwAMSEx.updateUsageStatsForPCMode(getTopActivity(), visible, this.mService.mUsageStatsService);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetOtherStacksVisible(boolean visible) {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcDynamicStack(this.mStackId) && hasFullscreenTaskInPad()) {
            makeStackVisibleInPad(visible);
        }
    }

    private boolean hasFullscreenTaskInPad() {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            int WindowState = ((TaskRecord) this.mTaskHistory.get(i)).getWindowState();
            if (HwPCMultiWindowCompatibility.isLayoutFullscreen(WindowState) || HwPCMultiWindowCompatibility.isLayoutMaximized(WindowState)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInCallActivityStack() {
        for (int i = this.mTaskHistory.size() - 1; i >= 0; i--) {
            ActivityRecord topActivity = ((TaskRecord) this.mTaskHistory.get(i)).getTopActivity();
            if (topActivity != null) {
                ActivityManagerService activityManagerService = this.mService;
                if (ActivityManagerService.isInCallActivity(topActivity)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean getStackVisibleBeforeHidden() {
        return this.mStackVisibleBeforeHidden;
    }

    private void setStackVisibleBeforeHidden(boolean visible) {
        this.mStackVisibleBeforeHidden = visible;
    }

    private void makeStackVisibleInPad(boolean visible) {
        ActivityDisplay activityDisplay = (ActivityDisplay) this.mStackSupervisor.mActivityDisplays.get(this.mDisplayId);
        if (activityDisplay == null) {
            HwPCUtils.log("ActivityManager", "Display with displayId=" + this.mDisplayId + " not found.");
            return;
        }
        for (int stackNdx = activityDisplay.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            if (activityDisplay.getChildAt(stackNdx) instanceof HwActivityStack) {
                HwActivityStack stack = activityDisplay.getChildAt(stackNdx);
                if (stack.mStackId != this.mStackId) {
                    if (!visible || (stack.getStackVisibleBeforeHidden() && !stack.isInCallActivityStack())) {
                        if (!visible && stack.mStackVisible) {
                            HwPCUtils.log("ActivityManager", "makeStackVisibleInPad stack=" + stack + " make invisible because the top activity is fullscreen ,mStackVisibleBeforeHidden=" + stack.getStackVisibleBeforeHidden());
                            stack.setStackVisibleBeforeHidden(true);
                        }
                        stack.makeStackVisible(visible);
                        if (visible) {
                            stack.setStackVisibleBeforeHidden(false);
                        }
                    } else {
                        HwPCUtils.log("ActivityManager", "makeStackVisibleInPad stack=" + stack + " Skipping: is invisible before launch fullscreen activity or this stack contains InCallUI activity ,mStackVisibleBeforeHidden=" + stack.getStackVisibleBeforeHidden());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean moveTaskToBackLocked(int taskId) {
        if ((!this.mVrMananger.isVRDeviceConnected() || this.mVrMananger.isVirtualScreenMode()) && !HwPCUtils.isExtDynamicStack(this.mStackId)) {
            return HwActivityStack.super.moveTaskToBackLocked(taskId);
        }
        makeStackVisible(false);
        if (HwPCUtils.enabledInPad() && hasFullscreenTaskInPad()) {
            makeStackVisibleInPad(true);
        }
        setStackVisibleBeforeHidden(false);
        ensureActivitiesVisibleLocked(topRunningActivityLocked(), 0, false);
        adjustFocusToNextFocusableStack("minTask");
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        return true;
    }

    /* access modifiers changed from: protected */
    public void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            if (HwPCUtils.isExtDynamicStack(this.mStackId)) {
                if (HwPCUtils.enabledInPad() && hasFullscreenTaskInPad()) {
                    makeStackVisibleInPad(false);
                }
                makeStackVisible(true);
            }
            HwActivityStack.super.moveToFront(reason, task);
            minimalLRUTaskIfNeed();
        }
    }

    /* access modifiers changed from: protected */
    public void setKeepPortraitFR() {
        IntelliServiceManager.getInstance(this.mService.mContext).setKeepPortrait(true);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecord starting) {
        if (HwPCUtils.isExtDynamicStack(this.mStackId) && (!this.mStackVisible || this.mTaskHistory.isEmpty())) {
            return false;
        }
        if (HwFreeFormUtils.isFreeFormEnable()) {
            if (!inFreeformWindowingMode()) {
                ActivityStack stack = this.mStackSupervisor.getFocusedStack();
                if (stack != null && stack.inFreeformWindowingMode()) {
                    int stackIndexBehindTop = getDisplay().mStacks.indexOf(stack) - 1;
                    ActivityStack stackBehindTop = (ActivityStack) getDisplay().mStacks.get(stackIndexBehindTop);
                    if (stackIndexBehindTop >= 0 && stackBehindTop != null && stackBehindTop.mStackId == this.mStackId && !stackBehindTop.isHomeOrRecentsStack() && stack.getFreeFormStackVisible()) {
                        return true;
                    }
                }
            } else if (!this.mTaskHistory.isEmpty() && getFreeFormStackVisible()) {
                return true;
            } else {
                return false;
            }
        }
        return HwActivityStack.super.shouldBeVisible(starting);
    }

    /* access modifiers changed from: protected */
    public void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        if (HwPCUtils.isExtDynamicStack(this.mStackId)) {
            makeStackVisible(true);
        }
        processStackStateIfNeed(tr);
        HwActivityStack.super.moveTaskToFrontLocked(tr, noAnimation, options, timeTracker, reason);
        minimalLRUTaskIfNeed();
    }

    private void minimalLRUTaskIfNeed() {
        if (HwPCUtils.isExtDynamicStack(this.mStackId)) {
            int visibleNum = 0;
            TaskRecord lastVisibleTask = null;
            int N = getDisplay().getChildCount();
            for (int i = 0; i < N; i++) {
                ActivityStack stack = getDisplay().getChildAt(i);
                final String title = null;
                if (stack.shouldBeVisible(null)) {
                    if (lastVisibleTask == null && stack.topTask() != null) {
                        lastVisibleTask = stack.topTask();
                    }
                    if (lastVisibleTask != null) {
                        visibleNum++;
                        if (visibleNum > MAX_TASK_NUM && lastVisibleTask.mStack != null) {
                            HwPCUtils.log("ActivityManager", "max task num, minimial the task: " + lastVisibleTask.taskId);
                            this.mService.moveTaskBackwards(lastVisibleTask.taskId);
                            final Context context = HwPCUtils.getDisplayContext(this.mService.mContext, lastVisibleTask.mStack.mDisplayId);
                            if (context != null) {
                                ActivityRecord ar = lastVisibleTask.getRootActivity();
                                if (!(ar == null || ar.info == null)) {
                                    title = ar.info.loadLabel(context.getPackageManager()).toString();
                                }
                                if (!TextUtils.isEmpty(title)) {
                                    UiThread.getHandler().post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(context, context.getString(33685972, new Object[]{title}), 0).show();
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

    public boolean isVisibleLocked(String packageName, boolean deepRecur) {
        String str = packageName;
        int i = 0;
        if (str == null || packageName.isEmpty()) {
            return false;
        }
        TaskRecord lastTask = null;
        HwTaskRecord hwTask = null;
        int size = this.mTaskHistory.size();
        if (size <= 0) {
            return false;
        }
        int maxTaskIdx = deepRecur ? 0 : size - 1;
        int taskNdx = size - 1;
        while (taskNdx >= maxTaskIdx) {
            HwTaskRecord hwTask2 = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (hwTask2 instanceof HwTaskRecord) {
                hwTask = hwTask2;
            }
            if (hwTask != null) {
                ArrayList<ActivityRecord> activities = hwTask.getActivities();
                if (activities != null && activities.size() > 0) {
                    int numActivities = activities.size();
                    TaskRecord lastTask2 = lastTask;
                    int activityNdx = i;
                    while (true) {
                        int activityNdx2 = activityNdx;
                        if (activityNdx2 >= numActivities) {
                            lastTask = lastTask2;
                            break;
                        }
                        try {
                            ActivityRecord r = activities.get(activityNdx2);
                            if (r != null && (str.equals(r.packageName) || r.getTask() == lastTask2)) {
                                if (r.visible || r.visibleIgnoringKeyguard) {
                                    return true;
                                }
                                lastTask2 = r.getTask();
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Slog.e("ActivityManager", "IndexOutOfBoundsException: Index: +" + activityNdx2 + ", Size: " + activities.size());
                        }
                        activityNdx = activityNdx2 + 1;
                    }
                }
            }
            taskNdx--;
            i = 0;
        }
        return false;
    }

    private void processStackStateIfNeed(TaskRecord tr) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ActivityStack stack = this.mStackSupervisor.getStack(5, 1);
            if (stack != null) {
                if (getActivityType() == 2) {
                    if (tr.mCallingPackage.equals("android")) {
                        this.mStackSupervisor.mHwActivityStackSupervisorEx.removeFreeFromStackLocked();
                    } else {
                        stack.setFreeFormStackVisible(false);
                    }
                } else if (getWindowingMode() == 5) {
                    setFreeFormStackVisible(true);
                } else if (getActivityType() == 3) {
                    stack.setFreeFormStackVisible(false);
                } else if (getWindowingMode() == 1 && tr.getTopActivity() != null && tr.topRunningActivityLocked().packageName != null && stack.topRunningActivityLocked() != null) {
                    String pkgName = tr.topRunningActivityLocked().packageName;
                    if (pkgName.equals(stack.topRunningActivityLocked().packageName)) {
                        HwFreeFormUtils.log("ams", "keep freeform for move the same app as freeform-app :" + pkgName + " to front in fullscreen");
                    } else if (!stack.getCurrentPkgUnderFreeForm().equals(pkgName)) {
                        HwFreeFormUtils.log("ams", "remove freeform for move other pkg :" + pkgName + " to front in fullscreen");
                        stack.setFreeFormStackVisible(false);
                        stack.finishAllActivitiesLocked(true);
                    }
                }
            }
        }
    }
}
