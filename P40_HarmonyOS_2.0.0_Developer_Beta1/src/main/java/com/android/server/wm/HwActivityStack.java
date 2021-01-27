package com.android.server.wm;

import android.content.Intent;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import java.util.ArrayList;

public class HwActivityStack extends ActivityStackBridgeEx {
    private static final boolean IS_HW_NAVI_BAR = SystemPropertiesEx.getBoolean("ro.config.hw_navigationbar", false);
    private static final String TAG = "HwActivityStack";
    private boolean mIsMwNewTaskSplitStack = false;

    public HwActivityStack(ActivityDisplayEx display, int stackId, ActivityStackSupervisorEx supervisor, int windowingMode, int activityType, boolean isOnTop) {
        super(display, stackId, supervisor, windowingMode, activityType, isOnTop);
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        int retChanges = changes;
        if (newConfig == null || naviConfig == null || !IS_HW_NAVI_BAR) {
            return retChanges;
        }
        int newChanges = naviConfig.diff(newConfig);
        if ((newChanges & 1280) == 0) {
            return retChanges & -1281;
        }
        if ((newChanges & HwPackageManagerServiceEx.APP_FORCE_DARK_USER_SET_FLAG) != 0) {
            if (retChanges == 1280 || retChanges == 1024) {
                retChanges &= -1025;
            }
            return retChanges & -257;
        }
        SlogEx.i(TAG, "Get invalid flag nothing. newChanges = " + newChanges);
        return retChanges;
    }

    public boolean isSplitActivity(Intent intent) {
        return (intent == null || (IntentExEx.getHwFlags(intent) & 4) == 0) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void setKeepPortraitFR() {
        IntelliServiceManagerEx.setKeepPortrait(getService().getContext(), true);
    }

    /* access modifiers changed from: protected */
    public boolean shouldBeVisible(ActivityRecordEx starting) {
        if (HwPCUtils.isExtDynamicStack(getStackId()) && (getStackVisible() || isTaskHistoryEmpty())) {
            return false;
        }
        if (!HwFreeFormUtils.isFreeFormEnable() || !inFreeformWindowingMode()) {
            boolean result = shouldBeVisibleEx(starting);
            if (!inHwMagicWindowingMode() || result || !HwMwUtils.performPolicy(135, new Object[]{Integer.valueOf(getStackId())}).getBoolean("RESULT_STACK_VISIBILITY", result)) {
                return result;
            }
            SlogEx.i(TAG, "shouldBeVisible visbile magicwindow:" + getStackId());
            return true;
        } else if (isTaskHistoryEmpty()) {
            return false;
        } else {
            return getFreeFormStackVisible();
        }
    }

    public boolean isVisibleLocked(String packageName, boolean isDeepRecur) {
        int size;
        ArrayList<ActivityRecordEx> activities;
        if (packageName == null || packageName.isEmpty() || (size = getTaskRecordExHistory().size()) <= 0) {
            return false;
        }
        HwTaskRecord hwTask = null;
        int maxTaskIdx = isDeepRecur ? 0 : size - 1;
        TaskRecordEx lastTask = null;
        for (int taskNdx = size - 1; taskNdx >= maxTaskIdx; taskNdx--) {
            TaskRecordBridgeEx task = (TaskRecordBridgeEx) getTaskRecordExHistory().get(taskNdx);
            if (task instanceof HwTaskRecord) {
                hwTask = (HwTaskRecord) task;
            }
            if (!(hwTask == null || (activities = hwTask.getActivities()) == null || activities.size() <= 0)) {
                int numActivities = activities.size();
                for (int activityNdx = 0; activityNdx < numActivities; activityNdx++) {
                    try {
                        ActivityRecordEx activityRecord = activities.get(activityNdx);
                        if (activityRecord != null && !activityRecord.isEmpty() && (packageName.equals(activityRecord.getPackageName()) || TaskRecordEx.isSameTaskRecord(activityRecord.getTaskRecordEx(), lastTask))) {
                            if (activityRecord.isVisible() || activityRecord.isVisibleIgnoringKeyguard()) {
                                return true;
                            }
                            lastTask = activityRecord.getTaskRecordEx();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        SlogEx.e(TAG, "IndexOutOfBoundsException: Index: +" + activityNdx + ", Size: " + numActivities);
                    }
                }
                continue;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void moveToBack(String reason, TaskRecordEx task) {
        HwActivityStack.super.moveToBackEx(reason, task);
        if (inHwMagicWindowingMode()) {
            HwMwUtils.performPolicy(133, new Object[]{Integer.valueOf(getStackId())});
        }
    }

    public void setIsMwNewTaskSplitStack(boolean isMwNewTaskSplitStack) {
        this.mIsMwNewTaskSplitStack = isMwNewTaskSplitStack;
    }

    public boolean isMwNewTaskSplitStack() {
        return this.mIsMwNewTaskSplitStack;
    }
}
