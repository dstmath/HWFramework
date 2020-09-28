package com.huawei.uifirst.fastview.common;

import android.os.Handler;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;

public class StatusDecouplingImpl {
    private static final boolean DEBUG_ENABLE = false;
    private static final int DELAY_TIME_DEFAULT = 5000;
    protected static final int LOG_DEBUG = 2;
    protected static final int LOG_ERROR = 4;
    protected static final int LOG_INFO = 1;
    protected static final int LOG_WARNING = 3;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_INTERMEDIATE = 2;
    public static final int STATE_UNKNOWN = 3;
    protected static final String TAG = "StatusDecouplingPolicy";
    protected int mDelayTime = DELAY_TIME_DEFAULT;
    protected Handler mHandler = new Handler();
    protected boolean mIsDesiredState = false;
    protected boolean mIsTaskAdditionNeeded = false;
    protected boolean mIsTaskInProcessing = false;
    protected boolean mIsUserClickEnable = false;
    protected boolean mIsUserClickSkipped = false;
    protected String mModelName = BuildConfig.FLAVOR;
    protected Runnable mTimeOutRunnable = new Runnable() {
        /* class com.huawei.uifirst.fastview.common.StatusDecouplingImpl.AnonymousClass1 */

        public void run() {
            StatusDecouplingImpl.this.locolLog(3, "timeOutRunnable run");
            StatusDecouplingImpl.this.timeoutHandle();
        }
    };

    public boolean getDesireState() {
        return this.mIsDesiredState;
    }

    public void setDesireState(boolean isOpenState) {
        this.mIsDesiredState = isOpenState;
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

    public void onHandleUpdateState(boolean isOpenState) {
    }

    public void locolLog(int logLevel, String logInfo) {
        String logString = this.mModelName + " : " + logInfo;
        if (logLevel != 1 && logLevel != 2) {
            if (logLevel == 3) {
                Log.w(TAG, logString);
            } else if (logLevel == 4) {
                Log.e(TAG, logString);
            }
        }
    }

    public void checkTaskAdditionNeeded(int deviceActualState) {
        boolean isDevActualState;
        boolean isOldTaskInProcessing = this.mIsTaskInProcessing;
        if (deviceActualState == 0) {
            setTaskInProcessing(false);
            isDevActualState = false;
        } else if (deviceActualState == 1) {
            setTaskInProcessing(false);
            isDevActualState = true;
        } else if (deviceActualState != 2) {
            setTaskInProcessing(false);
            isDevActualState = false;
        } else {
            setTaskInProcessing(true);
            isDevActualState = false;
        }
        if (isOldTaskInProcessing && !this.mIsTaskInProcessing) {
            if (this.mIsUserClickSkipped) {
                if (this.mIsDesiredState == isDevActualState) {
                    this.mIsUserClickSkipped = false;
                    this.mIsTaskAdditionNeeded = false;
                    this.mIsUserClickEnable = false;
                } else {
                    this.mIsTaskAdditionNeeded = true;
                }
            } else if (this.mIsDesiredState == isDevActualState) {
                this.mIsUserClickEnable = false;
            } else {
                exitPolicy();
            }
            Log.d(TAG, "checkTaskAdditionNeeded mIsTaskNeeded=" + this.mIsTaskAdditionNeeded + ",deviceState=" + deviceActualState + ",ClickSkipped=" + this.mIsUserClickSkipped + ",UserEn=" + this.mIsUserClickEnable);
        }
    }

    public void taskAddition(boolean isOpenState) {
    }

    public void timeoutHandle() {
    }

    public void exitPolicy() {
    }
}
