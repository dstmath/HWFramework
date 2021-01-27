package com.huawei.server.sidetouch;

import android.content.Context;
import android.os.SystemClock;
import android.util.IMonitor;
import android.util.Log;
import android.view.KeyEvent;
import com.huawei.android.app.HiEvent;
import com.huawei.android.app.HiView;

public class HwSideTouchDataReport {
    private static final int CONFIG_EVEVT_ID = 991310175;
    private static final int COUNT_EVEVT_ID = 991310176;
    private static final long COUNT_REPORT_INTERVAL = 28800000;
    private static final long EVENT_COUNT_THRESHOLD = 50;
    private static final int EVEVT_ID = 907400028;
    private static final String PARAM_KEY_ALL_TIME = "allTime";
    private static final String PARAM_KEY_COUNT = "count";
    private static final String PARAM_KEY_END_TIME = "endTime";
    private static final String PARAM_KEY_END_TYPE = "endType";
    private static final String PARAM_KEY_START_TIME = "startTime";
    private static final String PARAM_KEY_START_TYPE = "startType";
    private static final String SEPARATOR = "#";
    private static final long SOLID_VOLUME_EVENT_INTERVAL = 1500;
    private static final String TAG = "HwSideTouchDataReport";
    private static final long TIME_GAP_THRESHOLD = 2000;
    private static final short TYPE_KEYCODE_VOLUME_DOWN = 0;
    private static final short TYPE_KEYCODE_VOLUME_UP = 1;
    private static HwSideTouchDataReport sInstance = null;
    private StringBuilder mAllTime = new StringBuilder();
    private Context mContext;
    private short mCount = TYPE_KEYCODE_VOLUME_DOWN;
    private String mEndTime = null;
    private short mEndType = TYPE_KEYCODE_VOLUME_DOWN;
    private int mInCallSideVolumeCount;
    private int mInCallSolidVolumeCount;
    private boolean mIsDataInitialState = true;
    private long mLastEventTime = 0;
    private long mLastReportCountTime;
    private long mLastSolidVolumeTime;
    private int mSideLandCount;
    private int mSideVolumeCount;
    private int mSolidLandCount;
    private int mSolidVolumeCount;
    private String mStartTime = null;
    private short mStartType = TYPE_KEYCODE_VOLUME_DOWN;

    private HwSideTouchDataReport(Context context) {
        this.mContext = context;
    }

    public static synchronized HwSideTouchDataReport getInstance(Context context) {
        HwSideTouchDataReport hwSideTouchDataReport;
        synchronized (HwSideTouchDataReport.class) {
            if (sInstance == null) {
                sInstance = new HwSideTouchDataReport(context);
            }
            hwSideTouchDataReport = sInstance;
        }
        return hwSideTouchDataReport;
    }

    public void reportVolumeBtnKeyEvent(KeyEvent event) {
        if (event != null && event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            short keyType = TYPE_KEYCODE_VOLUME_DOWN;
            if (keyCode == 24) {
                keyType = TYPE_KEYCODE_VOLUME_UP;
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
                this.mAllTime.append(SEPARATOR);
            }
            this.mEndType = keyType;
            this.mEndTime = Long.toString(curTime);
            this.mAllTime.append((int) this.mEndType);
            this.mAllTime.append(SEPARATOR);
            this.mAllTime.append(this.mEndTime);
            this.mLastEventTime = curTime;
            this.mCount = (short) (this.mCount + TYPE_KEYCODE_VOLUME_UP);
            if (((long) this.mCount) == EVENT_COUNT_THRESHOLD) {
                sendEvent();
                resetParam();
            }
        }
    }

    public void reportSideTouchConfigChanged(boolean isEnabled) {
        HiView.report(new HiEvent((int) CONFIG_EVEVT_ID).putInt("config", isEnabled ? 1 : 0));
    }

    public void reportVolumeCount(boolean isSideVolume, boolean isInCall, boolean isLandscape) {
        long now = SystemClock.elapsedRealtime();
        if (isSideVolume) {
            this.mSideVolumeCount++;
            if (isInCall) {
                this.mInCallSideVolumeCount++;
            }
            if (isLandscape) {
                this.mSideLandCount++;
            }
        } else {
            if (now - this.mLastSolidVolumeTime > SOLID_VOLUME_EVENT_INTERVAL) {
                this.mSolidVolumeCount++;
                if (isInCall) {
                    this.mInCallSolidVolumeCount++;
                }
                if (isLandscape) {
                    this.mSolidLandCount++;
                }
            }
            this.mLastSolidVolumeTime = now;
        }
        if (now - this.mLastReportCountTime > COUNT_REPORT_INTERVAL) {
            HiView.report(new HiEvent((int) COUNT_EVEVT_ID).putInt("sideCount", this.mSideVolumeCount).putInt("solidCount", this.mSolidVolumeCount).putInt("inCallSideCount", this.mInCallSideVolumeCount).putInt("inCallSolidCount", this.mInCallSolidVolumeCount).putInt("sideLandCount", this.mSideLandCount).putInt("solidLandCount", this.mSolidLandCount));
            this.mLastReportCountTime = now;
            this.mSolidLandCount = 0;
            this.mSideLandCount = 0;
            this.mInCallSolidVolumeCount = 0;
            this.mInCallSideVolumeCount = 0;
            this.mSolidVolumeCount = 0;
            this.mSideVolumeCount = 0;
        }
    }

    private void resetParam() {
        this.mStartType = TYPE_KEYCODE_VOLUME_DOWN;
        this.mStartTime = null;
        this.mEndType = TYPE_KEYCODE_VOLUME_DOWN;
        this.mEndTime = null;
        this.mCount = TYPE_KEYCODE_VOLUME_DOWN;
        this.mIsDataInitialState = true;
        this.mLastEventTime = 0;
        if (this.mAllTime.length() > 0) {
            StringBuilder sb = this.mAllTime;
            sb.delete(0, sb.length());
        }
        Log.i(TAG, "resetParam: finish.");
    }

    private void sendEvent() {
        IMonitor.EventStream stream = IMonitor.openEventStream((int) EVEVT_ID);
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
