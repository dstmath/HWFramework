package com.android.server.am;

final class AppErrorResult {
    boolean mHasResult;
    int mResult;

    AppErrorResult() {
        this.mHasResult = false;
    }

    public void set(int res) {
        synchronized (this) {
            this.mHasResult = true;
            this.mResult = res;
            notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int get() {
        synchronized (this) {
            while (true) {
                if (this.mHasResult) {
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return this.mResult;
    }
}
