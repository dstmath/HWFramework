package com.huawei.server.wm;

import android.app.TaskInfo;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.TaskRecord;
import java.util.ArrayList;

public interface IHwTaskRecordEx {
    void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecord> arrayList);

    void updateMagicWindowTaskInfo(TaskRecord taskRecord, TaskInfo taskInfo);
}
