package com.android.server.wm;

import android.app.TaskInfo;
import com.huawei.server.wm.IHwTaskRecordEx;
import java.util.ArrayList;
import java.util.Iterator;

public class HwTaskRecordExBridge implements IHwTaskRecordEx {
    private HwTaskRecordExBridgeEx mHwTaskRecordExBridgeEx;

    public void setHwTaskRecordExBridgeEx(HwTaskRecordExBridgeEx hwTaskRecordExBridgeEx) {
        this.mHwTaskRecordExBridgeEx = hwTaskRecordExBridgeEx;
    }

    public void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecord> mActivities) {
        new ArrayList();
        ArrayList<ActivityRecordEx> activityRecordExes = getActivityRecordExs(mActivities);
        if (activityRecordExes != null) {
            this.mHwTaskRecordExBridgeEx.forceNewConfigWhenReuseActivity(activityRecordExes);
        }
    }

    private ArrayList<ActivityRecordEx> getActivityRecordExs(ArrayList<ActivityRecord> activityRecords) {
        if (activityRecords == null) {
            return null;
        }
        ArrayList<ActivityRecordEx> activityRecordExs = new ArrayList<>();
        Iterator<ActivityRecord> it = activityRecords.iterator();
        while (it.hasNext()) {
            ActivityRecordEx activityRecordEx = new ActivityRecordEx();
            activityRecordEx.setActivityRecord(it.next());
            activityRecordExs.add(activityRecordEx);
        }
        return activityRecordExs;
    }

    public void updateMagicWindowTaskInfo(TaskRecord taskRecord, TaskInfo info) {
        if (taskRecord != null) {
            TaskRecordEx taskRecordEx = new TaskRecordEx();
            taskRecordEx.setTaskRecord(taskRecord);
            this.mHwTaskRecordExBridgeEx.updateMagicWindowTaskInfo(taskRecordEx, info);
        }
    }
}
