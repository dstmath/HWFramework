package com.android.server.am;

/* access modifiers changed from: package-private */
public final class AppErrorResult {
    boolean mHasResult = false;
    int mResult;

    AppErrorResult() {
    }

    public void set(int res) {
        synchronized (this) {
            this.mHasResult = true;
            this.mResult = res;
            notifyAll();
        }
    }

    public int get() {
        synchronized (this) {
            while (!this.mHasResult) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return this.mResult;
    }
}
