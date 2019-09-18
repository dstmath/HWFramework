package com.huawei.uifirst.fastview.common;

import android.os.Handler;
import android.util.Log;

public class StatusDecoupling {
    protected static final boolean DEBUG_ENABLE = false;
    protected static final int LOG_DEBUG = 2;
    protected static final int LOG_ERROR = 4;
    protected static final int LOG_INFO = 1;
    protected static final int LOG_WARNING = 3;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_INTERMEDIATE = 2;
    public static final int STATE_UNKNOWN = 3;
    protected static final String TAG = "StatusDecouplingPolicy";
    protected int mDelayTime = 5000;
    protected boolean mDesiredState = false;
    protected Handler mHandler = new Handler();
    protected boolean mIsTaskAdditionNeeded = false;
    protected boolean mIsTaskInProcessing = false;
    protected String mModelName = "";
    protected Runnable mTimeOutRunnable = new Runnable() {
        public void run() {
            StatusDecoupling.this.locolLog(3, "timeOutRunnable run");
            StatusDecoupling.this.timeoutHandle();
        }
    };
    protected boolean mUserClickEnable = false;
    protected boolean mUserClickSkipped = false;

    public boolean getDesireState() {
        return this.mDesiredState;
    }

    public void setDesireState(boolean realState) {
        this.mDesiredState = realState;
    }

    public boolean getTaskProcessingState() {
        return this.mIsTaskInProcessing;
    }

    public void setTaskInProcessing(boolean isInProcessing) {
        locolLog(2, "setTaskInProcessing isInProcessing = " + isInProcessing);
        this.mIsTaskInProcessing = isInProcessing;
        if (this.mIsTaskInProcessing) {
            this.mHandler.removeCallbacks(this.mTimeOutRunnable);
            this.mHandler.postDelayed(this.mTimeOutRunnable, (long) this.mDelayTime);
            return;
        }
        this.mHandler.removeCallbacks(this.mTimeOutRunnable);
    }

    public void onHandleClick(Object arg) {
    }

    public void onDeviceStateChanged(Object arg) {
    }

    public void onHandleUpdateState(Object arg) {
    }

    public void locolLog(int logLevel, String logInfo) {
        String logString = this.mModelName + " : " + logInfo;
        switch (logLevel) {
            case 3:
                Log.v(TAG, logString);
                return;
            case 4:
                Log.e(TAG, logString);
                return;
            default:
                return;
        }
    }

    public void checkTaskAdditionNeeded(int deviceActualState) {
        boolean mDevActualState;
        boolean wasTaskInProcessing = this.mIsTaskInProcessing;
        switch (deviceActualState) {
            case 0:
                setTaskInProcessing(false);
                mDevActualState = false;
                break;
            case 1:
                setTaskInProcessing(false);
                mDevActualState = true;
                break;
            case 2:
                setTaskInProcessing(true);
                mDevActualState = false;
                break;
            default:
                setTaskInProcessing(false);
                mDevActualState = false;
                break;
        }
        if (wasTaskInProcessing && !this.mIsTaskInProcessing) {
            if (this.mUserClickSkipped) {
                if (this.mDesiredState == mDevActualState) {
                    this.mUserClickSkipped = false;
                    this.mIsTaskAdditionNeeded = false;
                    this.mUserClickEnable = false;
                } else {
                    this.mIsTaskAdditionNeeded = true;
                }
            } else if (this.mDesiredState == mDevActualState) {
                this.mUserClickEnable = false;
            } else {
                exitPolicy();
            }
            Log.d(TAG, "checkTaskAdditionNeeded  mIsTaskNeeded=" + this.mIsTaskAdditionNeeded + ",deviceState=" + deviceActualState + ",ClickSkipped=" + this.mUserClickSkipped + ",UserEn=" + this.mUserClickEnable);
        }
    }

    public void taskAddition(boolean desireState) {
    }

    public void timeoutHandle() {
    }

    public void exitPolicy() {
    }
}
