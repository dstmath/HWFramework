package com.android.server.am;

import android.app.ActivityManager;
import android.app.WindowConfiguration;
import android.util.SparseArray;
import com.android.server.am.TaskRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

class RunningTasks {
    private static final Comparator<TaskRecord> LAST_ACTIVE_TIME_COMPARATOR = $$Lambda$RunningTasks$BGar3HlUsTw0HzSmfkEWly0moY.INSTANCE;
    private final TaskRecord.TaskActivitiesReport mTmpReport = new TaskRecord.TaskActivitiesReport();
    private final TreeSet<TaskRecord> mTmpSortedSet = new TreeSet<>(LAST_ACTIVE_TIME_COMPARATOR);
    private final ArrayList<TaskRecord> mTmpStackTasks = new ArrayList<>();

    RunningTasks() {
    }

    /* access modifiers changed from: package-private */
    public void getTasks(int maxNum, List<ActivityManager.RunningTaskInfo> list, @WindowConfiguration.ActivityType int ignoreActivityType, @WindowConfiguration.WindowingMode int ignoreWindowingMode, SparseArray<ActivityDisplay> activityDisplays, int callingUid, boolean allowed) {
        if (maxNum > 0) {
            this.mTmpSortedSet.clear();
            int numDisplays = activityDisplays.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                ActivityDisplay display = activityDisplays.valueAt(displayNdx);
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    this.mTmpStackTasks.clear();
                    stack.getRunningTasks(this.mTmpStackTasks, ignoreActivityType, ignoreWindowingMode, callingUid, allowed);
                    this.mTmpSortedSet.addAll(this.mTmpStackTasks);
                }
            }
            SparseArray<ActivityDisplay> sparseArray = activityDisplays;
            Iterator<TaskRecord> iter = this.mTmpSortedSet.iterator();
            int maxNum2 = maxNum;
            while (iter.hasNext() && maxNum2 != 0) {
                list.add(createRunningTaskInfo(iter.next()));
                maxNum2--;
            }
            List<ActivityManager.RunningTaskInfo> list2 = list;
        }
    }

    private ActivityManager.RunningTaskInfo createRunningTaskInfo(TaskRecord task) {
        task.getNumRunningActivities(this.mTmpReport);
        ActivityManager.RunningTaskInfo ci = new ActivityManager.RunningTaskInfo();
        ci.id = task.taskId;
        ci.stackId = task.getStackId();
        ci.baseActivity = this.mTmpReport.base.intent.getComponent();
        ci.topActivity = this.mTmpReport.top.intent.getComponent();
        ci.lastActiveTime = task.lastActiveTime;
        ci.description = task.lastDescription;
        ci.numActivities = this.mTmpReport.numActivities;
        ci.numRunning = this.mTmpReport.numRunning;
        ci.supportsSplitScreenMultiWindow = task.supportsSplitScreenWindowingMode();
        ci.resizeMode = task.mResizeMode;
        ci.configuration.setTo(task.getConfiguration());
        return ci;
    }
}
