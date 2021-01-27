package com.android.server.backup.restore;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RestoreEngine {
    public static final int SUCCESS = 0;
    private static final String TAG = "RestoreEngine";
    public static final int TARGET_FAILURE = -2;
    public static final int TRANSPORT_FAILURE = -3;
    private final AtomicInteger mResult = new AtomicInteger(0);
    private final AtomicBoolean mRunning = new AtomicBoolean(false);

    public boolean isRunning() {
        return this.mRunning.get();
    }

    public void setRunning(boolean stillRunning) {
        synchronized (this.mRunning) {
            this.mRunning.set(stillRunning);
            this.mRunning.notifyAll();
        }
    }

    public int waitForResult() {
        synchronized (this.mRunning) {
            while (isRunning()) {
                try {
                    this.mRunning.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return getResult();
    }

    public int getResult() {
        return this.mResult.get();
    }

    public void setResult(int result) {
        this.mResult.set(result);
    }
}
