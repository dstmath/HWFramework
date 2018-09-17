package com.android.server.wm;

import android.os.Process;
import com.android.internal.annotations.GuardedBy;
import com.android.server.AnimationThread;
import com.android.server.ThreadPriorityBooster;

class WindowManagerThreadPriorityBooster extends ThreadPriorityBooster {
    private final int mAnimationThreadId = AnimationThread.get().getThreadId();
    @GuardedBy("mLock")
    private boolean mAppTransitionRunning;
    @GuardedBy("mLock")
    private boolean mBoundsAnimationRunning;
    private final Object mLock = new Object();

    WindowManagerThreadPriorityBooster() {
        super(-4, 5);
    }

    public void boost() {
        if (Process.myTid() != this.mAnimationThreadId) {
            super.boost();
        }
    }

    public void reset() {
        if (Process.myTid() != this.mAnimationThreadId) {
            super.reset();
        }
    }

    void setAppTransitionRunning(boolean running) {
        synchronized (this.mLock) {
            if (this.mAppTransitionRunning != running) {
                this.mAppTransitionRunning = running;
                updatePriorityLocked();
            }
        }
    }

    void setBoundsAnimationRunning(boolean running) {
        synchronized (this.mLock) {
            if (this.mBoundsAnimationRunning != running) {
                this.mBoundsAnimationRunning = running;
                updatePriorityLocked();
            }
        }
    }

    @GuardedBy("mLock")
    private void updatePriorityLocked() {
        int priority = (this.mAppTransitionRunning || this.mBoundsAnimationRunning) ? -10 : -4;
        setBoostToPriority(priority);
        Process.setThreadPriority(this.mAnimationThreadId, priority);
    }
}
