package com.android.server.wm;

import android.content.Intent;
import android.freeform.HwFreeFormUtils;
import android.hardware.display.DisplayManager;
import android.os.Process;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.vrsystem.IVRSystemServiceManager;
import com.android.server.HwServiceFactory;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.server.wm.IHwActivityStackSupervisorEx;
import java.util.ArrayList;

public class HwActivityStackSupervisorEx implements IHwActivityStackSupervisorEx {
    private static final String PKG_PARENT_CONTROL = "com.huawei.parentcontrol";
    public static final String TAG = "HwActivityStackSupervisorEx";
    private int mNextPcFreeStackId = 1000000008;
    private int mNextVrFreeStackId = 0;
    final ActivityTaskManagerService mService;

    public HwActivityStackSupervisorEx(ActivityTaskManagerService service) {
        this.mService = service;
        IVRSystemServiceManager iVRSystemServiceManager = this.mService.mVrMananger;
        this.mNextVrFreeStackId = 1100000000;
    }

    public void adjustFocusDisplayOrder(SparseIntArray tmpOrderedDisplayIds, int displayIdForStack) {
        int N;
        int tempElem;
        if (HwPCUtils.isPcCastModeInServer() && tmpOrderedDisplayIds != null && (N = tmpOrderedDisplayIds.size()) > 1 && (tempElem = tmpOrderedDisplayIds.get(N - 1)) != displayIdForStack) {
            for (int i = N - 2; i >= 0; i--) {
                int displayId = tmpOrderedDisplayIds.get(i);
                if (displayId == displayIdForStack) {
                    tmpOrderedDisplayIds.put(N - 1, displayIdForStack);
                    tmpOrderedDisplayIds.put(i, tempElem);
                    return;
                }
                tmpOrderedDisplayIds.put(i, tempElem);
                tempElem = displayId;
            }
        }
    }

    public void scheduleDisplayAdded(int displayId) {
        IHwPCManager pcManager;
        if (!handleVrDisplayAdded(displayId) && (pcManager = HwPCUtils.getHwPCManager()) != null) {
            try {
                pcManager.scheduleDisplayAdded(displayId);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "onDisplayAdded()");
            }
        }
    }

    public void scheduleDisplayRemoved(int displayId) {
        if (!handleVrDisplayRemoved(displayId)) {
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager != null) {
                try {
                    pcManager.scheduleDisplayRemoved(displayId);
                } catch (RemoteException e) {
                    HwPCUtils.log(TAG, "onDisplayRemoved()");
                }
            }
            handleDisplayRemoved(displayId);
        }
    }

    public void scheduleDisplayChanged(int displayId) {
        IHwPCManager pcManager;
        if (!handleVrDisplayChanged(displayId) && (pcManager = HwPCUtils.getHwPCManager()) != null) {
            try {
                pcManager.scheduleDisplayChanged(displayId);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "onDisplayChanged()");
            }
        }
    }

    private int getNextVrStackId() {
        while (true) {
            int i = this.mNextVrFreeStackId;
            IVRSystemServiceManager iVRSystemServiceManager = this.mService.mVrMananger;
            if (i >= 1100000000 && this.mService.mRootActivityContainer.getStack(this.mNextVrFreeStackId) == null) {
                return this.mNextVrFreeStackId;
            }
            this.mNextVrFreeStackId++;
        }
    }

    private int getNextPcStackId() {
        while (true) {
            int i = this.mNextPcFreeStackId;
            IVRSystemServiceManager iVRSystemServiceManager = this.mService.mVrMananger;
            if (i >= 1100000000) {
                this.mNextPcFreeStackId = 1000000008;
            }
            if (this.mNextPcFreeStackId >= 1000000008 && this.mService.mRootActivityContainer.getStack(this.mNextPcFreeStackId) == null) {
                return this.mNextPcFreeStackId;
            }
            this.mNextPcFreeStackId++;
        }
    }

    public ActivityStack getValidLaunchStackForPC(int displayId, ActivityRecord r, ActivityDisplay activityDisplay) {
        if (displayId != 0) {
            ActivityStackSupervisor stackSupervisor = this.mService.mStackSupervisor;
            if (stackSupervisor == null) {
                return null;
            }
            if (this.mService.mVrMananger.isValidVRDisplayId(displayId)) {
                Slog.i(TAG, "vr getValidLaunchStackOnDisplay displayid is: " + displayId);
                return HwServiceFactory.createActivityStack(activityDisplay, getNextVrStackId(), stackSupervisor, 1, 1, true);
            } else if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayId)) {
                return null;
            } else {
                return HwServiceFactory.createActivityStack(activityDisplay, getNextPcStackId(), stackSupervisor, 10, 1, true);
            }
        } else if (!HwPCUtils.isPcCastModeInServer()) {
            return null;
        } else {
            HwPCUtils.log(TAG, " create full screen stack because the stack is null when r is from pc");
            activityDisplay.getOrCreateStack(1, 0, true);
            return null;
        }
    }

    private void handleDisplayRemoved(int displayId) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) {
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
            if (activityDisplay != null) {
                int size = activityDisplay.getChildCount();
                ArrayList<ActivityStack> stacks = new ArrayList<>();
                for (int i2 = 0; i2 < size; i2++) {
                    stacks.add(activityDisplay.getChildAt(i2));
                }
                onDisplayRemoved(stacks);
                if (HwPCUtils.enabledInPad()) {
                    this.mService.mRootActivityContainer.applySleepTokens(false);
                }
            }
        }
    }

    public void onDisplayRemoved(ArrayList<ActivityStack> stacks) {
        ArrayList<Intent> intentList = new ArrayList<>();
        for (int i = stacks.size() - 1; i >= 0; i--) {
            stacks.get(i).mForceHidden = true;
        }
        ArrayList<WindowProcessController> procs = getPCProcessRecordList();
        ArrayList<Intent> needRestartIntentList = new ArrayList<>();
        while (!stacks.isEmpty()) {
            ActivityStack stack = stacks.get(0);
            TaskRecord tr = stack.topTask();
            if (tr != null) {
                if (tr instanceof TaskRecordBridge) {
                    removeProcessesActivityNotFinished(((TaskRecordBridge) tr).getActivities(), procs);
                }
                if (tr.intent != null && !stack.inPinnedWindowingMode() && 0 == 0) {
                    if (!HwPCUtils.isHiCarCastMode()) {
                        intentList.add(tr.intent);
                    } else if (stack.isTopActivityVisible()) {
                        intentList.clear();
                        intentList.add(tr.intent);
                    }
                }
                if (tr.isVisible()) {
                    stack.moveTaskToBackLocked(stack.getStackId());
                }
                stack.finishAllActivitiesLocked(true);
            } else {
                stack.finishAllActivitiesLocked(true);
            }
            stacks.remove(stack);
        }
        killPCProcessesLocked(procs);
        IHwPCManager pcManager = HwPCUtils.getHwPCManager();
        if (pcManager != null) {
            try {
                pcManager.relaunchIMEIfNecessary();
                pcManager.saveAppIntent(intentList);
                pcManager.saveNeedRestartAppIntent(needRestartIntentList);
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "fail to saveAppIntent on display removed");
            }
        }
        ((ActivityTaskManagerService) this.mService).mHwATMSEx.getPkgDisplayMaps().clear();
    }

    private void onVRdisplayRemoved(int displayId) {
        Slog.i(TAG, "onVRdisplayRemoved");
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
            Slog.e(TAG, "vr activityDisplay is null");
            return;
        }
        int size = activityDisplay.getChildCount();
        ArrayList<ActivityStack> stacks = new ArrayList<>();
        for (int i2 = 0; i2 < size; i2++) {
            stacks.add(activityDisplay.getChildAt(i2));
        }
        while (!stacks.isEmpty()) {
            ActivityStack stack = stacks.get(0);
            this.mService.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, false);
            stacks.remove(stack);
        }
        this.mService.mRootActivityContainer.removeChild(activityDisplay);
        this.mService.mRootActivityContainer.resumeFocusedStacksTopActivities();
    }

    private ArrayList<WindowProcessController> getPCProcessRecordList() {
        ArrayList<WindowProcessController> procs = new ArrayList<>();
        int NP = this.mService.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<WindowProcessController> apps = (SparseArray) this.mService.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            for (int ia = 0; ia < NA; ia++) {
                WindowProcessController proc = apps.valueAt(ia);
                if (!(proc == this.mService.mHomeProcess || proc.mDisplayId == 0 || proc.mDisplayId == -1)) {
                    if (proc.mPkgList.contains("com.huawei.works") && !proc.hasActivities()) {
                        proc.mDisplayId = 0;
                    } else if (!proc.mPkgList.contains(PKG_PARENT_CONTROL)) {
                        procs.add(proc);
                    }
                }
            }
        }
        return procs;
    }

    private void killPCProcessesLocked(ArrayList<WindowProcessController> procs) {
        int NU = procs.size();
        for (int iu = 0; iu < NU; iu++) {
            Process.killProcess(procs.get(iu).mPid);
        }
    }

    private void removeProcessesActivityNotFinished(ArrayList<ActivityRecord> activities, ArrayList<WindowProcessController> procs) {
        for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
            ActivityRecord r = activities.get(activityNdx);
            int NP = procs.size();
            int i = 0;
            while (true) {
                if (i >= NP) {
                    break;
                } else if (!"com.huawei.filemanager".equals(procs.get(i).mName) && procs.get(i).mName.equals(r.processName)) {
                    procs.remove(i);
                    break;
                } else {
                    i++;
                }
            }
        }
    }

    private boolean isDisplayIdInVrMode(int displayId) {
        DisplayManager mDisplayManager = this.mService.mRootActivityContainer.mDisplayManager;
        if (mDisplayManager == null) {
            return false;
        }
        Display display = mDisplayManager.getDisplay(displayId);
        if (display == null) {
            return this.mService.mVrMananger.isValidVRDisplayId(displayId);
        }
        DisplayInfo displayInfo = new DisplayInfo();
        if (!display.getDisplayInfo(displayInfo)) {
            return false;
        }
        return this.mService.mVrMananger.isVRDisplay(displayId, displayInfo.getNaturalWidth(), displayInfo.getNaturalHeight());
    }

    public void removeFreeFromStackLocked() {
        ActivityStack stack = this.mService.mStackSupervisor.mRootActivityContainer.getStack(5, 1);
        if (stack != null) {
            cleanAllTaskFromRecent(5);
            stack.setFreeFormStackVisible(false);
            stack.setCurrentPkgUnderFreeForm("");
            stack.finishAllActivitiesLocked(true);
        }
    }

    public void cleanAllTaskFromRecent(int windowMode) {
        ActivityStackSupervisor stackSupervisor = this.mService.mStackSupervisor;
        ActivityStack stack = stackSupervisor.mRootActivityContainer.getStack(windowMode, 1);
        if (stack != null) {
            ArrayList<TaskRecord> taskHistory = stack.getTaskHistory();
            for (int taskNdx = taskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                TaskRecord task = taskHistory.get(taskNdx);
                stackSupervisor.mRecentTasks.remove(task);
                task.removedFromRecents();
            }
        }
    }

    public void handleFreeFormWindow(TaskRecord task) {
        ActivityStack freeFormStack;
        if (HwFreeFormUtils.isFreeFormEnable()) {
            ActivityDisplay activityDisplay = this.mService.mRootActivityContainer.getDefaultDisplay();
            boolean isNull = true;
            if (activityDisplay == null) {
                freeFormStack = null;
            } else {
                freeFormStack = activityDisplay.getStack(5, 1);
            }
            if (freeFormStack != null) {
                if (!task.inFreeformWindowingMode()) {
                    HwFreeFormUtils.log(TAG, "remove freeform stack from recent");
                    removeFreeFromStackLocked();
                    return;
                }
                activityDisplay.createStack(1, 1, true);
                HwFreeFormUtils.log(TAG, "move freeform stack to fullscreen stack");
                try {
                    ActivityRecord activity = freeFormStack.topRunningActivityLocked();
                    if (!(activity == null || activity.app == null)) {
                        if (activity.app.mThread != null) {
                            isNull = false;
                        }
                    }
                    if (!isNull) {
                        activity.app.mThread.scheduleRestoreFreeFormConfig(activity.appToken);
                    } else {
                        HwFreeFormUtils.log(TAG, "restoreFreeFormConfig failed : no activity");
                    }
                } catch (RemoteException e) {
                    HwFreeFormUtils.log(TAG, "scheduleRestoreFreeFormConfig error!");
                }
                freeFormStack.setWindowingMode(1, false, false, false, true, false);
                freeFormStack.setFreeFormStackVisible(false);
                freeFormStack.setCurrentPkgUnderFreeForm("");
            }
        }
    }

    public void handlePCMultiDisplayWindow(TaskRecord task, int launchMode) {
        if (task != null && launchMode != 105 && task.getWindowingMode() == 105 && HwActivityTaskManager.isPCMultiCastMode()) {
            Slog.i(TAG, "handlePCMultiDisplayWindow toggle task to fullscreen task: " + task.taskId);
            this.mService.mHwATMSEx.hwTogglePhoneFullScreenFromLauncherOrRecent(task.taskId);
        }
    }

    private boolean handleVrDisplayAdded(int displayId) {
        Slog.i(TAG, "handleVrDisplayAdded displayId = " + displayId);
        if (isDisplayIdInVrMode(displayId)) {
            clearVrVirtualContent();
            this.mService.mVrMananger.setVRDisplayID(displayId, true);
            return true;
        } else if (this.mService.mVrMananger.isVrVirtualDisplay(displayId)) {
            this.mService.mVrMananger.addVrVirtualDisplayId(displayId);
            return true;
        } else {
            Slog.w(TAG, "handleVrDisplayAdded other mode displayId: " + displayId);
            return false;
        }
    }

    private boolean handleVrDisplayChanged(int displayId) {
        Slog.i(TAG, "handleVrDisplayChanged displayId = " + displayId);
        if (isDisplayIdInVrMode(displayId)) {
            this.mService.mVrMananger.setVRDisplayID(displayId, true);
            return true;
        }
        Slog.w(TAG, "handleVrDisplayChanged other vr mode displayId: " + displayId);
        return false;
    }

    private boolean handleVrDisplayRemoved(int displayId) {
        Slog.i(TAG, "handleVrDisplayRemoved displayId = " + displayId);
        if (isDisplayIdInVrMode(displayId)) {
            this.mService.mVrMananger.setVRDisplayID(-1, false);
            this.mService.mVrMananger.setVirtualScreenMode(false);
            onVRdisplayRemoved(displayId);
            clearVrVirtualContent();
            return true;
        } else if (this.mService.mVrMananger.isVrVirtualDisplay(displayId)) {
            clearVrVirtualContent();
            return true;
        } else {
            Slog.w(TAG, "handleVrDisplayRemoved other mode displayId: " + displayId);
            return false;
        }
    }

    private void clearVrVirtualContent() {
        this.mService.mVrMananger.clearVrVirtualDisplay();
        this.mService.mVrMananger.clearRecordedVirtualAppList();
    }
}
