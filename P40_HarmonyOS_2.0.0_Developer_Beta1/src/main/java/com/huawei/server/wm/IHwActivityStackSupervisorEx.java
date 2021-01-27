package com.huawei.server.wm;

import android.util.SparseIntArray;
import com.android.server.wm.ActivityDisplay;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.TaskRecord;
import java.util.ArrayList;

public interface IHwActivityStackSupervisorEx {
    void adjustFocusDisplayOrder(SparseIntArray sparseIntArray, int i);

    ActivityStack getValidLaunchStackForPC(int i, ActivityRecord activityRecord, ActivityDisplay activityDisplay);

    void handleFreeFormWindow(TaskRecord taskRecord);

    void handlePCMultiDisplayWindow(TaskRecord taskRecord, int i);

    void onDisplayRemoved(ArrayList<ActivityStack> arrayList);

    void removeFreeFromStackLocked();

    void scheduleDisplayAdded(int i);

    void scheduleDisplayChanged(int i);

    void scheduleDisplayRemoved(int i);
}
