package com.android.server.notification;

import java.util.concurrent.TimeUnit;

public abstract class RankingReconsideration implements Runnable {
    private static final int CANCELLED = 3;
    private static final int DONE = 2;
    private static final long IMMEDIATE = 0;
    private static final int RUNNING = 1;
    private static final int START = 0;
    private long mDelay;
    protected String mKey;
    private int mState;

    public abstract void applyChangesLocked(NotificationRecord notificationRecord);

    public abstract void work();

    public RankingReconsideration(String key) {
        this(key, IMMEDIATE);
    }

    public RankingReconsideration(String key, long delay) {
        this.mDelay = delay;
        this.mKey = key;
        this.mState = 0;
    }

    public String getKey() {
        return this.mKey;
    }

    public void run() {
        if (this.mState == 0) {
            this.mState = RUNNING;
            work();
            this.mState = DONE;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(this.mDelay, TimeUnit.MILLISECONDS);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.mState != 0) {
            return false;
        }
        this.mState = CANCELLED;
        return true;
    }

    public boolean isCancelled() {
        return this.mState == CANCELLED;
    }

    public boolean isDone() {
        return this.mState == DONE;
    }
}
