package com.android.server;

import android.os.ConditionVariable;
import android.os.SystemClock;

abstract class ResettableTimeout {
    /* access modifiers changed from: private */
    public ConditionVariable mLock = new ConditionVariable();
    /* access modifiers changed from: private */
    public volatile long mOffAt;
    /* access modifiers changed from: private */
    public volatile boolean mOffCalled;
    /* access modifiers changed from: private */
    public Thread mThread;

    private class T extends Thread {
        private T() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
            sleep(r0);
         */
        public void run() {
            ResettableTimeout.this.mLock.open();
            while (true) {
                synchronized (this) {
                    long diff = ResettableTimeout.this.mOffAt - SystemClock.uptimeMillis();
                    if (diff <= 0) {
                        boolean unused = ResettableTimeout.this.mOffCalled = true;
                        ResettableTimeout.this.off();
                        Thread unused2 = ResettableTimeout.this.mThread = null;
                        return;
                    }
                }
            }
            while (true) {
            }
        }
    }

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
}
