package com.android.server;

import android.os.ConditionVariable;
import android.os.SystemClock;

abstract class ResettableTimeout {
    private ConditionVariable mLock = new ConditionVariable();
    private volatile long mOffAt;
    private volatile boolean mOffCalled;
    private Thread mThread;

    private class T extends Thread {
        /* synthetic */ T(ResettableTimeout this$0, T -this1) {
            this();
        }

        private T() {
        }

        /* JADX WARNING: Missing block: B:10:?, code:
            sleep(r0);
     */
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
                }
            }
        }
    }

    public abstract void off();

    public abstract void on(boolean z);

    ResettableTimeout() {
    }

    public void go(long milliseconds) {
        synchronized (this) {
            boolean alreadyOn;
            this.mOffAt = SystemClock.uptimeMillis() + milliseconds;
            if (this.mThread == null) {
                alreadyOn = false;
                this.mLock.close();
                this.mThread = new T(this, null);
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
