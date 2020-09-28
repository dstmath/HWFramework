package com.huawei.sidetouch;

import android.util.IMonitor;
import android.util.Log;
import android.view.KeyEvent;

public class HwSideTouchDataReport {
    private static final long EVENT_COUNT_THRESHOLD = 50;
    private static final int EVEVT_ID = 907400028;
    private static final String PARAM_KEY_ALL_TIME = "allTime";
    private static final String PARAM_KEY_COUNT = "count";
    private static final String PARAM_KEY_END_TIME = "endTime";
    private static final String PARAM_KEY_END_TYPE = "endType";
    private static final String PARAM_KEY_START_TIME = "startTime";
    private static final String PARAM_KEY_START_TYPE = "startType";
    private static final String SEPARATOR = "#";
    private static final String TAG = "HwSideTouchDataReport";
    private static final long TIME_GAP_THRESHOLD = 2000;
    private static final short TYPE_KEYCODE_VOLUME_DOWN = 0;
    private static final short TYPE_KEYCODE_VOLUME_UP = 1;
    private static HwSideTouchDataReport mInstance = null;
    private StringBuilder mAllTime = new StringBuilder();
    private short mCount = 0;
    private String mEndTime = null;
    private short mEndType = 0;
    private boolean mIsDataInitialState = true;
    private long mLastEventTime = 0;
    private String mStartTime = null;
    private short mStartType = 0;

    public static synchronized HwSideTouchDataReport getInstance() {
        HwSideTouchDataReport hwSideTouchDataReport;
        synchronized (HwSideTouchDataReport.class) {
            if (mInstance == null) {
                mInstance = new HwSideTouchDataReport();
            }
            hwSideTouchDataReport = mInstance;
        }
        return hwSideTouchDataReport;
    }

    public void reportVolumeBtnKeyEvent(KeyEvent event) {
        if (event != null && event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            short keyType = 0;
            if (keyCode == 24) {
                keyType = 1;
            } else if (keyCode != 25) {
                Log.w(TAG, "reportVolumeBtnKeyEvent: keyCode(" + keyCode + ") not VOLUME_DOWN or VOLUME_UP, ignore.");
                return;
            }
            long curTime = System.currentTimeMillis();
            long j = this.mLastEventTime;
            if (j != 0 && curTime - j > TIME_GAP_THRESHOLD) {
                Log.i(TAG, "reportVolumeBtnKeyEvent: reach time gap threshold 2000");
                sendEvent();
                resetParam();
            }
            if (this.mIsDataInitialState) {
                this.mStartType = keyType;
                this.mStartTime = Long.toString(curTime);
                this.mIsDataInitialState = false;
            } else {
                this.mAllTime.append("#");
            }
            this.mEndType = keyType;
            this.mEndTime = Long.toString(curTime);
            this.mAllTime.append((int) this.mEndType);
            this.mAllTime.append("#");
            this.mAllTime.append(this.mEndTime);
            this.mLastEventTime = curTime;
            this.mCount = (short) (this.mCount + 1);
            if (((long) this.mCount) == EVENT_COUNT_THRESHOLD) {
                sendEvent();
                resetParam();
            }
        }
    }

    private void resetParam() {
        this.mStartType = 0;
        this.mStartTime = null;
        this.mEndType = 0;
        this.mEndTime = null;
        this.mCount = 0;
        this.mIsDataInitialState = true;
        this.mLastEventTime = 0;
        if (this.mAllTime.length() > 0) {
            StringBuilder sb = this.mAllTime;
            sb.delete(0, sb.length());
        }
        Log.i(TAG, "resetParam: finish.");
    }

    private void sendEvent() {
        IMonitor.EventStream stream = IMonitor.openEventStream(EVEVT_ID);
        if (stream == null) {
            Log.w(TAG, "sendEvent: stream is null, return.");
            return;
        }
        stream.setParam(PARAM_KEY_START_TYPE, this.mStartType);
        stream.setParam(PARAM_KEY_START_TIME, this.mStartTime);
        stream.setParam(PARAM_KEY_END_TYPE, this.mEndType);
        stream.setParam(PARAM_KEY_END_TIME, this.mEndTime);
        stream.setParam(PARAM_KEY_COUNT, this.mCount);
        stream.setParam(PARAM_KEY_ALL_TIME, this.mAllTime.toString());
        IMonitor.sendEvent(stream);
        IMonitor.closeEventStream(stream);
        Log.i(TAG, "sendEvent: mCount is " + ((int) this.mCount) + " , finish.");
    }
}
