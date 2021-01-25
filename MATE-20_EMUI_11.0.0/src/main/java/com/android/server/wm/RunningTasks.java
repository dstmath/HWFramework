package com.android.server.wm;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/* access modifiers changed from: package-private */
public class RunningTasks {
    private static final Comparator<TaskRecord> LAST_ACTIVE_TIME_COMPARATOR = $$Lambda$RunningTasks$B8bQNi7MO0XIePhmkVnejRGNp0.INSTANCE;
    private final TreeSet<TaskRecord> mTmpSortedSet = new TreeSet<>(LAST_ACTIVE_TIME_COMPARATOR);
    private final ArrayList<TaskRecord> mTmpStackTasks = new ArrayList<>();

    RunningTasks() {
    }

    /* access modifiers changed from: package-private */
    public void getTasks(int maxNum, List<ActivityManager.RunningTaskInfo> list, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, ArrayList<ActivityDisplay> activityDisplays, int callingUid, boolean allowed) {
        if (maxNum > 0) {
            this.mTmpSortedSet.clear();
            int numDisplays = activityDisplays.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                ActivityDisplay display = activityDisplays.get(displayNdx);
                if (display.mDisplayId > 0) {
                    if (display.mService.mStackSupervisor.mRecentTasks.isCallerRecents(callingUid) && display.mService.mHwATMSEx.isVirtualDisplayId(display.mDisplayId, "padCast")) {
                    }
                }
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    this.mTmpStackTasks.clear();
                    stack.getRunningTasks(this.mTmpStackTasks, ignoreActivityType, ignoreWindowingMode, callingUid, allowed);
                    this.mTmpSortedSet.addAll(this.mTmpStackTasks);
                }
            }
            Iterator<TaskRecord> iter = this.mTmpSortedSet.iterator();
            int maxNum2 = maxNum;
            while (iter.hasNext()) {
                if (maxNum2 != 0) {
                    list.add(createRunningTaskInfo(iter.next()));
                    maxNum2--;
                } else {
                    return;
                }
            }
        }
    }

    private ActivityManager.RunningTaskInfo createRunningTaskInfo(TaskRecord task) {
        ActivityManager.RunningTaskInfo rti = new ActivityManager.RunningTaskInfo();
        task.fillTaskInfo(rti);
        rti.id = rti.taskId;
        return rti;
    }
}
