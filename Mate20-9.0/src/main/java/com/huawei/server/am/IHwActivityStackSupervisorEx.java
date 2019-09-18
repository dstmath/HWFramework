package com.huawei.server.am;

import android.util.SparseIntArray;
import com.android.server.am.ActivityStack;
import com.android.server.am.TaskRecord;

public interface IHwActivityStackSupervisorEx {
    void adjustFocusDisplayOrder(SparseIntArray sparseIntArray, int i);

    void handleFreeFormWindow(TaskRecord taskRecord);

    void removeFreeFromStackLocked();

    boolean shouldKeepResumedIfFreeFormExist(ActivityStack activityStack);
}
