package com.android.server;

import android.annotation.UnsupportedAppUsage;
import android.os.ConditionVariable;
import android.os.SystemClock;

abstract class ResettableTimeout {
    @UnsupportedAppUsage
    private ConditionVariable mLock = new ConditionVariable();
    @UnsupportedAppUsage
    private volatile long mOffAt;
    private volatile boolean mOffCalled;
    private Thread mThread;

    public abstract void off();

    public abstract void on(boolean z);

    ResettableTimeout() {
    }

    public void go(long milliseconds) {
        boolean alreadyOn;
        synchronized (this) {
            this.mOffAt = SystemClock.uptimeMillis() + milliseconds;
            if (this.mThread == null) {
                alreadyOn = false;
                this.mLock.close();
                this.mThread = new T();
                this.mThread.start();
                this.mLock.block();
                this.mOffCalled = false;
            } else {
                alreadyOn = true;
                this.mThread.interrupt();
            }
            on(alreadyOn);
        }
    }

    public void cancel() {
        synchronized (this) {
            this.mOffAt = 0;
            if (this.mThread != null) {
                this.mThread.interrupt();
                this.mThread = null;
            }
            if (!this.mOffCalled) {
                this.mOffCalled = true;
                off();
            }
        }
    }

    private class T extends Thread {
        private T() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            long diff;
            ResettableTimeout.this.mLock.open();
            while (true) {
                synchronized (this) {
                    diff = ResettableTimeout.this.mOffAt - SystemClock.uptimeMillis();
                    if (diff <= 0) {
                        ResettableTimeout.this.mOffCalled = true;
                        ResettableTimeout.this.off();
                        ResettableTimeout.this.mThread = null;
                        return;
                    }
                }
                try {
                    sleep(diff);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
