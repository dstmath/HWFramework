package com.android.server.rms.iaware.memory.data.handle;

import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.policy.DmeServer;
import com.android.server.rms.memrepair.SystemAppMemRepairMng;

public class DataInputHandle extends AbsDataHandle {
    private static final long ACTIVE_EVENT_DELAY_TIME = 5000;
    private static final Object LOCK = new Object();
    private static final String TAG = "AwareMem_InputHandle";
    private static DataInputHandle sDataHandle;
    private int mLastInputEvent = 0;
    private long mLastInputTime = 0;

    private DataInputHandle() {
    }

    public static DataInputHandle getInstance() {
        DataInputHandle dataInputHandle;
        synchronized (LOCK) {
            if (sDataHandle == null) {
                sDataHandle = new DataInputHandle();
            }
            dataInputHandle = sDataHandle;
        }
        return dataInputHandle;
    }

    @Override // com.android.server.rms.iaware.memory.data.handle.AbsDataHandle
    public int reportData(long timeStamp, int event, AttrSegments attrSegments) {
        if (this.mDmeServer == null) {
            this.mDmeServer = DmeServer.getInstance();
        }
        if (event == 10001) {
            return handleTouchDown(timeStamp, event);
        }
        if (event == 80001) {
            return handleTouchUp(timeStamp, event);
        }
        AwareLog.w(TAG, "Input event invalid");
        return -1;
    }

    private int handleTouchDown(long timeStamp, int event) {
        AwareLog.d(TAG, "input event touch down");
        this.mLastInputEvent = event;
        this.mLastInputTime = timeStamp;
        this.mDmeServer.stopExecute(timeStamp, event);
        SystemAppMemRepairMng.getInstance().interrupt(true);
        return 0;
    }

    private int handleTouchUp(long timeStamp, int event) {
        AwareLog.d(TAG, "input event touch up");
        this.mLastInputEvent = event;
        this.mLastInputTime = timeStamp;
        return 0;
    }

    public int getActiveStatus() {
        int lastEvent = this.mLastInputEvent;
        if (lastEvent == 10001) {
            return 1;
        }
        if (lastEvent == 80001) {
            if (SystemClock.uptimeMillis() - this.mLastInputTime > ACTIVE_EVENT_DELAY_TIME) {
                return 2;
            }
            return 1;
        }
        AwareLog.w(TAG, "LastEvent invalid");
        return -1;
    }
}
