package com.android.server.fsm;

import android.hardware.display.HwFoldScreenState;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Flog;
import android.util.Slog;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReportMonitorProcess {
    private static final int EXPAND_ACTION = 1;
    private static float EXPAND_ACTION_ANGLE = 45.0f;
    private static float EXPAND_THRESHOLD = 173.0f;
    private static float FOLDED_THRESHOLD = 6.0f;
    private static final int FOLD_ACTION = 2;
    private static final int HINGLE_TIMEOUT = 3000;
    private static final ReportMonitorProcess INSTANCE = new ReportMonitorProcess();
    private static final int MONITOR_EXPAND_FOLD_ID = 907400101;
    private static final int MONITOR_PSTURE_TIME_ID = 907400102;
    private static final int MSG_HANDLE_ANGLE_CHANGE = 0;
    private static final int MSG_HANDLE_ANGLE_TIMEOUT = 1;
    private static final int MSG_HANDLE_DURATION = 2;
    private static final int ST_TIMEOUT = 250;
    private static final String TAG = "Fsm_ReportMonitorProcess";
    private int mAction = 1;
    private float mAngle = 0.0f;
    private long mDiffTime = 0;
    private int mDurationTime = 0;
    private long mDurationTimeBegin = 0;
    private ReportHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("Fsm_ReportMonitorProcess");
    private AtomicBoolean mIsExpandA = new AtomicBoolean(false);
    private boolean mIsInwardFoldDevice = HwFoldScreenState.isInwardFoldDevice();
    private boolean mIsSendTimeOut = false;
    private long mLastEnterMotionTime = 0;
    private long mLastExitMotionTime = 0;
    private long mSwitchTime = 0;
    private long mSwitchTimeBegin = 0;

    ReportMonitorProcess() {
        this.mHandlerThread.start();
        this.mHandler = new ReportHandler(this.mHandlerThread.getLooper());
    }

    public static ReportMonitorProcess getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportModeEvent() {
        this.mHandler.removeMessages(1);
        if (this.mAngle > FOLDED_THRESHOLD) {
            this.mDiffTime = SystemClock.uptimeMillis() - this.mLastExitMotionTime;
        }
        reportDisplayModeEvent(this.mAction, (int) this.mDiffTime, (int) this.mSwitchTime, this.mAngle);
    }

    public void updateScreenOffInitTime(long time) {
        if (this.mIsInwardFoldDevice) {
            Slog.i("Fsm_ReportMonitorProcess", "updateScreenOffInitTime, Begin:" + time);
            this.mLastExitMotionTime = time;
            this.mSwitchTimeBegin = time;
            this.mDurationTimeBegin = time;
        }
    }

    public void updateExitMotionTime(long time) {
        if (this.mIsInwardFoldDevice) {
            Slog.i("Fsm_ReportMonitorProcess", "updateExitMotionTime:" + time);
            this.mLastExitMotionTime = time;
        }
    }

    public void updateSwitchStartTime(long time) {
        if (this.mIsInwardFoldDevice) {
            Slog.i("Fsm_ReportMonitorProcess", "updateSwitchStartTime, mSwitchTimeBegin:" + time);
            this.mSwitchTimeBegin = time;
        }
    }

    public void updateSwitchEndTime(long time) {
        if (this.mIsInwardFoldDevice) {
            this.mSwitchTime = time - this.mSwitchTimeBegin;
            Slog.i("Fsm_ReportMonitorProcess", "updateSwitchEndTime, time:" + time);
            sendReportMsg();
        }
    }

    public void updateDurationStartTime(Long time) {
        if (this.mIsInwardFoldDevice) {
            Slog.i("Fsm_ReportMonitorProcess", "updateDurationStartTime, time:" + time);
            this.mDurationTimeBegin = time.longValue();
        }
    }

    public void updateDurationEndTime(long time, int sleepMode) {
        if (this.mIsInwardFoldDevice) {
            Slog.i("Fsm_ReportMonitorProcess", "updateDurationEndTime, time:" + time + " sleepMode:" + sleepMode);
            this.mDurationTime = ((int) (time - this.mDurationTimeBegin)) / 1000;
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(sleepMode)));
        }
    }

    public void handerSensorAngleEvent(float angle) {
        if (this.mIsInwardFoldDevice) {
            this.mAngle = angle;
            if (angle < FOLDED_THRESHOLD) {
                if (this.mIsExpandA.get()) {
                    this.mAction = 2;
                    Slog.i("Fsm_ReportMonitorProcess", "handerSensorAngleEvent, FOLD_ACTION");
                    this.mDiffTime = SystemClock.uptimeMillis() - this.mLastExitMotionTime;
                    this.mIsExpandA.set(false);
                }
                updateEventState();
            } else if (angle > EXPAND_THRESHOLD) {
                if (!this.mIsExpandA.get()) {
                    this.mAction = 1;
                    Slog.i("Fsm_ReportMonitorProcess", "handerSensorAngleEvent, EXPAND_ACTION");
                    this.mIsExpandA.set(true);
                }
                updateEventState();
            } else {
                if (angle > EXPAND_ACTION_ANGLE) {
                    this.mAction = 1;
                }
                if (!this.mIsSendTimeOut) {
                    sendReportMsg(3000);
                }
                this.mIsSendTimeOut = true;
            }
        }
    }

    private void sendReportMsg() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
    }

    private void sendReportMsg(int timeOut) {
        this.mHandler.removeMessages(1);
        long j = this.mLastExitMotionTime;
        long j2 = this.mLastEnterMotionTime;
        if (j < j2) {
            this.mLastExitMotionTime = j2;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), (long) timeOut);
    }

    private void updateEventState() {
        this.mHandler.removeMessages(1);
        this.mIsSendTimeOut = false;
        this.mLastEnterMotionTime = SystemClock.uptimeMillis();
    }

    private void reportDisplayModeEvent(int action, int ht, int st, float angle) {
        if (st >= ST_TIMEOUT) {
            Slog.i("Fsm_ReportMonitorProcess", "report event, display: " + action + " Hingle time:" + ht + " ms, Switch time:" + st + " ms, angle:" + angle);
            Flog.bdReport((int) MONITOR_EXPAND_FOLD_ID, "{action:" + action + ",HT:" + ht + ",ST:" + st + ",angle:" + angle + "}");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportScreenOffPostureEvent(int state) {
        Slog.i("Fsm_ReportMonitorProcess", "report sleep posture: " + state + " time:" + this.mDurationTime + "s.");
        Flog.bdReport((int) MONITOR_PSTURE_TIME_ID, "{STATUS:" + state + ",DURATION:" + this.mDurationTime + "}");
    }

    /* access modifiers changed from: private */
    public final class ReportHandler extends Handler {
        ReportHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                ReportMonitorProcess.this.reportModeEvent();
            } else if (i == 1) {
                ReportMonitorProcess.this.reportModeEvent();
            } else if (i == 2) {
                ReportMonitorProcess.this.reportScreenOffPostureEvent(((Integer) msg.obj).intValue());
            }
        }
    }
}
