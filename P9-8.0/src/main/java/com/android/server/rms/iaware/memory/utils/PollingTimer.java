package com.android.server.rms.iaware.memory.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.policy.DMEServer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PollingTimer {
    private static final int MSG_POLLING_TIMER = 10;
    private static final String TAG = "AwareMem_PollingTimer";
    private AtomicLong mCurPeriod = new AtomicLong(0);
    private AtomicBoolean mIsMaxPeriodTime = new AtomicBoolean(false);
    private AtomicLong mLastPeriod = new AtomicLong(2000);
    private AtomicLong mNumPoll = new AtomicLong(1);
    private AtomicLong mOffsetPeriod = new AtomicLong(0);
    private PollingTimerHandler mPollingTimerHandler;

    private final class PollingTimerHandler extends Handler {
        public PollingTimerHandler(Looper looper) {
            super(looper);
        }

        public void removeAllMessage() {
            removeMessages(10);
        }

        public Message getMessage(int what) {
            return obtainMessage(what, 0, 0, null);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    Bundle extras = new Bundle();
                    extras.putString("appName", PollingTimer.TAG);
                    AwareLog.d(PollingTimer.TAG, "mNumPoll: " + PollingTimer.this.mNumPoll);
                    if (DataAppHandle.getInstance().isActivityLaunching()) {
                        AwareLog.i(PollingTimer.TAG, "time event abandond because of activity starting");
                    } else {
                        DMEServer.getInstance().execute(MemoryConstant.MEM_SCENE_DEFAULT, extras, 30002, SystemClock.uptimeMillis());
                        PollingTimer.this.mNumPoll.getAndIncrement();
                    }
                    synchronized (PollingTimer.class) {
                        PollingTimer.this.updateNextPeriod();
                    }
                    if (PollingTimer.this.mPollingTimerHandler != null) {
                        PollingTimer.this.mPollingTimerHandler.sendMessageDelayed(PollingTimer.this.mPollingTimerHandler.getMessage(10), PollingTimer.this.mCurPeriod.get());
                        return;
                    }
                    return;
                default:
                    AwareLog.w(PollingTimer.TAG, "illegal message: " + msg.what);
                    return;
            }
        }
    }

    public void setPollingTimerHandler(HandlerThread handlerThread) {
        if (handlerThread != null) {
            this.mPollingTimerHandler = new PollingTimerHandler(handlerThread.getLooper());
            AwareLog.d(TAG, "setHandler: object=" + handlerThread);
            return;
        }
        AwareLog.e(TAG, "setHandler: handlerThread is null!");
    }

    public void stopTimer() {
        if (this.mPollingTimerHandler != null) {
            this.mPollingTimerHandler.removeAllMessage();
        }
        this.mOffsetPeriod.set(0);
        this.mNumPoll.set(1);
        this.mLastPeriod.set(2000);
        this.mIsMaxPeriodTime.set(false);
    }

    public void resetTimer() {
        stopTimer();
        if (this.mPollingTimerHandler != null) {
            this.mPollingTimerHandler.sendMessageDelayed(this.mPollingTimerHandler.getMessage(10), this.mCurPeriod.get());
        }
    }

    public void setPollingPeriod(long pollingPeriod) {
        synchronized (PollingTimer.class) {
            this.mCurPeriod.set(pollingPeriod);
        }
        AwareLog.d(TAG, "setPollingPeriod=" + pollingPeriod);
    }

    private void updateNextPeriod() {
        if (!this.mIsMaxPeriodTime.get()) {
            this.mCurPeriod.addAndGet(this.mOffsetPeriod.get());
            this.mCurPeriod.set(this.mCurPeriod.get() <= 0 ? MemoryConstant.getDefaultTimerPeriod() : this.mCurPeriod.get());
            if (this.mCurPeriod.get() >= MemoryConstant.getMaxTimerPeriod()) {
                this.mCurPeriod.set(MemoryConstant.getMaxTimerPeriod());
                this.mNumPoll.set(1);
                this.mIsMaxPeriodTime.set(true);
            }
            long tmp = this.mNumPoll.get() % ((long) MemoryConstant.getNumTimerPeriod()) == 0 ? this.mLastPeriod.get() * 2 : this.mLastPeriod.get();
            this.mLastPeriod.set(tmp);
            this.mOffsetPeriod.set(tmp);
        }
    }
}
