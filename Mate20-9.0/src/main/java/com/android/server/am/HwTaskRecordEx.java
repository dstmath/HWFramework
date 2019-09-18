package com.android.server.am;

import com.huawei.server.am.IHwTaskRecordEx;
import java.util.ArrayList;

public class HwTaskRecordEx implements IHwTaskRecordEx {
    public static final String TAG = "HwActivityRecordEx";

    public void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecord> mActivities) {
        for (int activityNdx = mActivities.size() - 1; activityNdx >= 0; activityNdx--) {
            mActivities.get(activityNdx).forceNewConfig = true;
            mActivities.get(activityNdx).mSkipMultiWindowChanged = true;
        }
    }
}
