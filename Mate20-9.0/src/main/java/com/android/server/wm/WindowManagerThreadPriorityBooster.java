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
    private final int mSurfaceAnimationThreadId = SurfaceAnimationThread.get().getThreadId();

    WindowManagerThreadPriorityBooster() {
        super(-4, 5);
    }

    public void boost() {
        int myTid = Process.myTid();
        if (myTid != this.mAnimationThreadId && myTid != this.mSurfaceAnimationThreadId) {
            super.boost();
        }
    }

    public void reset() {
        int myTid = Process.myTid();
        if (myTid != this.mAnimationThreadId && myTid != this.mSurfaceAnimationThreadId) {
            super.reset();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppTransitionRunning(boolean running) {
        synchronized (this.mLock) {
            if (this.mAppTransitionRunning != running) {
                this.mAppTransitionRunning = running;
                updatePriorityLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setBoundsAnimationRunning(boolean running) {
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
        Process.setThreadPriority(this.mSurfaceAnimationThreadId, priority);
    }
}
