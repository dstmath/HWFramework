package com.android.server.wm;

import android.app.TaskInfoEx;
import java.util.ArrayList;

public class HwTaskRecordExBridgeEx {
    private HwTaskRecordExBridge mHwTaskRecordExBridge = new HwTaskRecordExBridge();

    public HwTaskRecordExBridgeEx() {
        this.mHwTaskRecordExBridge.setHwTaskRecordExBridgeEx(this);
    }

    public HwTaskRecordExBridge getHwTaskRecordExBridge() {
        return this.mHwTaskRecordExBridge;
    }

    public void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecordEx> arrayList) {
    }

    public void updateMagicWindowTaskInfo(TaskRecordEx taskRecord, TaskInfoEx info) {
    }
}
