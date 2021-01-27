package com.huawei.server.wm;

import com.android.server.wm.ActivityRecord;
import com.android.server.wm.TaskRecord;

public interface IHwActivityStackEx {
    boolean getHiddenFromHome();

    boolean getStackVisible();

    boolean getStackVisibleBeforeHidden();

    boolean isInCallActivityStack();

    void makeStackVisible(boolean z);

    boolean moveTaskToBackEx(int i);

    void moveTaskToFrontEx(TaskRecord taskRecord);

    void moveToFrontEx(String str, TaskRecord taskRecord);

    void resetOtherStacksVisible(boolean z);

    void setHiddenFromHome(boolean z);

    void setStackVisibleBeforeHidden(boolean z);

    boolean shouldSkipPausing(ActivityRecord activityRecord, ActivityRecord activityRecord2, int i);
}
