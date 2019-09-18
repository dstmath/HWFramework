package com.android.server.am;

import android.freeform.HwFreeFormUtils;
import android.os.RemoteException;
import android.util.CoordinationModeUtils;
import android.util.HwPCUtils;
import android.util.SparseIntArray;
import com.huawei.server.am.IHwActivityStackSupervisorEx;
import java.util.ArrayList;

public class HwActivityStackSupervisorEx implements IHwActivityStackSupervisorEx {
    public static final String TAG = "HwActivityStackSupervisorEx";
    final ActivityManagerService mService;

    public HwActivityStackSupervisorEx(ActivityManagerService service) {
        this.mService = service;
    }

    public void adjustFocusDisplayOrder(SparseIntArray tmpOrderedDisplayIds, int displayIdForStack) {
        if (HwPCUtils.isPcCastModeInServer() && tmpOrderedDisplayIds != null) {
            int N = tmpOrderedDisplayIds.size();
            if (N > 1) {
                int tempElem = tmpOrderedDisplayIds.get(N - 1);
                if (tempElem != displayIdForStack) {
                    int i = N - 2;
                    while (true) {
                        if (i < 0) {
                            break;
                        }
                        int displayId = tmpOrderedDisplayIds.get(i);
                        if (displayId == displayIdForStack) {
                            tmpOrderedDisplayIds.put(N - 1, displayIdForStack);
                            tmpOrderedDisplayIds.put(i, tempElem);
                            break;
                        }
                        tmpOrderedDisplayIds.put(i, tempElem);
                        tempElem = displayId;
                        i--;
                    }
                }
            }
        }
    }

    public void handleFreeFormWindow(TaskRecord task) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ActivityStackSupervisor mStackSupervisor = this.mService.mStackSupervisor;
            ActivityStack freeFormStack = mStackSupervisor.getStack(5, 1);
            if (freeFormStack != null) {
                if (task.inFreeformWindowingMode()) {
                    ActivityStack fullScreenStack = mStackSupervisor.getStack(1, 1);
                    if (fullScreenStack == null) {
                        fullScreenStack = mStackSupervisor.getDefaultDisplay().createStack(1, 1, true);
                    }
                    HwFreeFormUtils.log("ams", "move freeform stack to fullscreen stack");
                    try {
                        ActivityRecord activity = freeFormStack.topRunningActivityLocked();
                        if (activity == null || activity.app == null || activity.app.thread == null) {
                            HwFreeFormUtils.log("ams", "restoreFreeFormConfig failed : no activity");
                        } else {
                            activity.app.thread.scheduleRestoreFreeFormConfig(activity.appToken);
                        }
                    } catch (RemoteException e) {
                        HwFreeFormUtils.log("ams", "scheduleRestoreFreeFormConfig error!");
                    }
                    task.setWindowingMode(1);
                    task.reparent(fullScreenStack, true, 0, false, false, "exitFreeformMode");
                } else if (task.getTopActivity() == null || !task.getTopActivity().packageName.equals(freeFormStack.getCurrentPkgUnderFreeForm())) {
                    HwFreeFormUtils.log("ams", "remove freeform stack from recent");
                    removeFreeFromStackLocked();
                } else {
                    HwFreeFormUtils.log("ams", "move freeform stack to front from recent");
                    freeFormStack.setFreeFormStackVisible(true);
                    mStackSupervisor.moveFocusableActivityStackToFrontLocked(freeFormStack.topRunningActivityLocked(), "movefreeformtofront");
                }
            }
        }
    }

    public void removeFreeFromStackLocked() {
        ActivityStack stack = this.mService.mStackSupervisor.getStack(5, 1);
        if (stack != null) {
            cleanAllTaskFromRecent(5);
            stack.setFreeFormStackVisible(false);
            stack.setCurrentPkgUnderFreeForm("");
            stack.finishAllActivitiesLocked(true);
        }
    }

    public void cleanAllTaskFromRecent(int windowMode) {
        ActivityStackSupervisor mStackSupervisor = this.mService.mStackSupervisor;
        ActivityStack stack = mStackSupervisor.getStack(windowMode, 1);
        if (stack != null) {
            ArrayList<TaskRecord> taskHistory = stack.getTaskHistory();
            int taskNdx = taskHistory.size() - 1;
            while (true) {
                int taskNdx2 = taskNdx;
                if (taskNdx2 >= 0) {
                    TaskRecord task = taskHistory.get(taskNdx2);
                    mStackSupervisor.mRecentTasks.remove(task);
                    task.removedFromRecents();
                    taskNdx = taskNdx2 - 1;
                } else {
                    return;
                }
            }
        }
    }

    public boolean shouldKeepResumedIfFreeFormExist(ActivityStack stack) {
        if (CoordinationModeUtils.isFoldable() && stack != null && this.mService.mHwAMSEx.shouldResumeCoordinationPrimaryStack() && stack.getWindowingMode() == 11) {
            return true;
        }
        if (HwFreeFormUtils.isFreeFormEnable() && stack != null) {
            ActivityStack freeFormStack = this.mService.mStackSupervisor.getStack(5, 1);
            if (freeFormStack != null && freeFormStack.getFreeFormStackVisible() && !stack.isHomeOrRecentsStack()) {
                if (!stack.inFreeformWindowingMode() && stack.mResumedActivity != null && freeFormStack.topRunningActivityLocked() != null && !freeFormStack.topRunningActivityLocked().packageName.equals(stack.mResumedActivity.packageName)) {
                    freeFormStack.setCurrentPkgUnderFreeForm(stack.mResumedActivity.packageName);
                }
                HwFreeFormUtils.log("ams", "Stack:" + stack + " is keep resumed");
                return false;
            }
        }
        return false;
    }
}
