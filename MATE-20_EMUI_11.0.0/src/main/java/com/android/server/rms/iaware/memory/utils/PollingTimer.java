package com.android.server.rms.iaware.memory.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.policy.DmeServer;
import com.huawei.server.rme.hyperhold.Swap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PollingTimer {
    private static final long DEFAULT_LAST_PERIOD = 2000;
    private static final Object LOCK = new Object();
    private static final int MSG_POLLING_TIMER = 10;
    private static final String TAG = "AwareMem_PollingTimer";
    private AtomicLong mCurPeriod = new AtomicLong(0);
    private AtomicBoolean mIsMaxPeriodTime = new AtomicBoolean(false);
    private AtomicLong mLastPeriod = new AtomicLong(DEFAULT_LAST_PERIOD);
    private AtomicLong mNumPoll = new AtomicLong(1);
    private AtomicLong mOffsetPeriod = new AtomicLong(0);
    private PollingTimerHandler mPollingTimerHandler;

    public void setPollingTimerHandler(HandlerThread handlerThread) {
        if (handlerThread != null) {
            this.mPollingTimerHandler = new PollingTimerHandler(handlerThread.getLooper());
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "setHandler: object=" + handlerThread);
                return;
            }
            return;
        }
        AwareLog.w(TAG, "setHandler: handlerThread is null!");
    }

    public void stopTimer() {
        PollingTimerHandler pollingTimerHandler = this.mPollingTimerHandler;
        if (pollingTimerHandler != null) {
            pollingTimerHandler.removeAllMessage();
        }
        this.mOffsetPeriod.set(0);
        this.mNumPoll.set(1);
        this.mLastPeriod.set(DEFAULT_LAST_PERIOD);
        this.mIsMaxPeriodTime.set(false);
    }

    public void resetTimer() {
        stopTimer();
        PollingTimerHandler pollingTimerHandler = this.mPollingTimerHandler;
        if (pollingTimerHandler != null) {
            pollingTimerHandler.sendMessageDelayed(pollingTimerHandler.getMessage(10), this.mCurPeriod.get());
        }
    }

    public void setPollingPeriod(long pollingPeriod) {
        synchronized (LOCK) {
            this.mCurPeriod.set(pollingPeriod);
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setPollingPeriod=" + pollingPeriod);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNextPeriod() {
        long tmp;
        if (!this.mIsMaxPeriodTime.get()) {
            this.mCurPeriod.addAndGet(this.mOffsetPeriod.get());
            this.mCurPeriod.set(this.mCurPeriod.get() <= 0 ? MemoryConstant.getDefaultTimerPeriod() : this.mCurPeriod.get());
            if (this.mCurPeriod.get() >= MemoryConstant.getMaxTimerPeriod()) {
                this.mCurPeriod.set(MemoryConstant.getMaxTimerPeriod());
                this.mNumPoll.set(1);
                this.mIsMaxPeriodTime.set(true);
            }
            if (this.mNumPoll.get() % ((long) MemoryConstant.getNumTimerPeriod()) == 0) {
                tmp = this.mLastPeriod.get() * 2;
            } else {
                tmp = this.mLastPeriod.get();
            }
            this.mLastPeriod.set(tmp);
            this.mOffsetPeriod.set(tmp);
        }
    }

    /* access modifiers changed from: private */
    public final class PollingTimerHandler extends Handler {
        PollingTimerHandler(Looper looper) {
            super(looper);
        }

        public void removeAllMessage() {
            removeMessages(10);
        }

        public Message getMessage(int what) {
            return obtainMessage(what, 0, 0, null);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 10) {
                AwareLog.w(PollingTimer.TAG, "illegal message: " + msg.what);
            } else if (!Swap.getInstance().isSwapEnabled()) {
                Bundle extras = DataAppHandle.getInstance().createBundleFromAppInfo();
                extras.putString("appName", PollingTimer.TAG);
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d(PollingTimer.TAG, "mNumPoll: " + PollingTimer.this.mNumPoll);
                }
                if (DataAppHandle.getInstance().isActivityLaunching()) {
                    AwareLog.i(PollingTimer.TAG, "time event abandond because of activity starting");
                } else {
                    DmeServer.getInstance().execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, 30002, SystemClock.uptimeMillis());
                    PollingTimer.this.mNumPoll.getAndIncrement();
                }
                synchronized (PollingTimer.LOCK) {
                    PollingTimer.this.updateNextPeriod();
                }
                if (PollingTimer.this.mPollingTimerHandler != null) {
                    PollingTimer.this.mPollingTimerHandler.sendMessageDelayed(PollingTimer.this.mPollingTimerHandler.getMessage(10), PollingTimer.this.mCurPeriod.get());
                }
            } else if (PollingTimer.this.mPollingTimerHandler == null) {
                AwareLog.e(PollingTimer.TAG, "mPollingTimerHandler is null");
            } else {
                PollingTimer.this.mPollingTimerHandler.sendMessageDelayed(PollingTimer.this.mPollingTimerHandler.getMessage(10), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            }
        }
    }
}
