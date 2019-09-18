package com.android.server.rms.iaware.memory.data.handle;

import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.content.AttrSegments;
import com.android.server.rms.iaware.memory.policy.DMEServer;

public class DataInputHandle extends AbsDataHandle {
    private static final long ACTIVE_EVENT_DELAY_TIME = 5000;
    private static final String TAG = "AwareMem_InputHandle";
    private static DataInputHandle sDataHandle;
    private int mLastInputEvent = 0;
    private long mLastInputTime = 0;

    public static DataInputHandle getInstance() {
        DataInputHandle dataInputHandle;
        synchronized (DataInputHandle.class) {
            if (sDataHandle == null) {
                sDataHandle = new DataInputHandle();
            }
            dataInputHandle = sDataHandle;
        }
        return dataInputHandle;
    }

    public int reportData(long timestamp, int event, AttrSegments attrSegments) {
        if (this.mDMEServer == null) {
            this.mDMEServer = DMEServer.getInstance();
        }
        if (event == 10001) {
            return handleTouchDown(timestamp, event);
        }
        if (event == 80001) {
            return handleTouchUp(timestamp, event);
        }
        AwareLog.w(TAG, "Input event invalid");
        return -1;
    }

    private int handleTouchDown(long timestamp, int event) {
        AwareLog.d(TAG, "input event touch down");
        this.mLastInputEvent = event;
        this.mLastInputTime = timestamp;
        this.mDMEServer.stopExecute(timestamp, event);
        return 0;
    }

    private int handleTouchUp(long timestamp, int event) {
        AwareLog.d(TAG, "input event touch up");
        this.mLastInputEvent = event;
        this.mLastInputTime = timestamp;
        return 0;
    }

    private DataInputHandle() {
    }

    public int getActiveStatus() {
        int lastEvent = this.mLastInputEvent;
        int i = 1;
        if (lastEvent == 10001) {
            return 1;
        }
        if (lastEvent == 80001) {
            if (ACTIVE_EVENT_DELAY_TIME < SystemClock.uptimeMillis() - this.mLastInputTime) {
                i = 2;
            }
            return i;
        }
        AwareLog.w(TAG, "LastEvent invalid");
        return -1;
    }
}
