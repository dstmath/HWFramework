package com.android.server;

import android.os.ConditionVariable;
import android.os.SystemClock;

abstract class ResettableTimeout {
    private ConditionVariable mLock;
    private volatile long mOffAt;
    private volatile boolean mOffCalled;
    private Thread mThread;

    private class T extends Thread {
        private T() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            ResettableTimeout.this.mLock.open();
            while (true) {
                synchronized (this) {
                    long diff = ResettableTimeout.this.mOffAt - SystemClock.uptimeMillis();
                    if (diff <= 0) {
                        ResettableTimeout.this.mOffCalled = true;
                        ResettableTimeout.this.off();
                        ResettableTimeout.this.mThread = null;
                        return;
                    }
                    try {
                        sleep(diff);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public abstract void off();

    public abstract void on(boolean z);

    ResettableTimeout() {
        this.mLock = new ConditionVariable();
    }

    public void go(long milliseconds) {
        synchronized (this) {
            boolean alreadyOn;
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
}
