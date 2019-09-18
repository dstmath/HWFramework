package com.huawei.server.am;

import com.android.server.am.ActivityRecord;
import java.util.ArrayList;

public interface IHwTaskRecordEx {
    void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecord> arrayList);
}
