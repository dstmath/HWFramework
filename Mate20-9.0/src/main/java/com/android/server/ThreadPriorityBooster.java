package com.android.server;

import android.os.Process;

public class ThreadPriorityBooster {
    private static final boolean ENABLE_LOCK_GUARD = false;
    private volatile int mBoostToPriority;
    private final int mLockGuardIndex;
    private final ThreadLocal<PriorityState> mThreadState = new ThreadLocal<PriorityState>() {
        /* access modifiers changed from: protected */
        public PriorityState initialValue() {
            return new PriorityState();
        }
    };

    private static class PriorityState {
        int prevPriority;
        int regionCounter;

        private PriorityState() {
        }
    }

    public ThreadPriorityBooster(int boostToPriority, int lockGuardIndex) {
        this.mBoostToPriority = boostToPriority;
        this.mLockGuardIndex = lockGuardIndex;
    }

    public void boost() {
        int tid = Process.myTid();
        int prevPriority = Process.getThreadPriority(tid);
        PriorityState state = this.mThreadState.get();
        if (state.regionCounter == 0) {
            state.prevPriority = prevPriority;
            if (prevPriority > this.mBoostToPriority) {
                Process.setThreadPriority(tid, this.mBoostToPriority);
            }
        }
        state.regionCounter++;
    }

    public void reset() {
        PriorityState state = this.mThreadState.get();
        state.regionCounter--;
        int currentPriority = Process.getThreadPriority(Process.myTid());
        if (state.regionCounter == 0 && state.prevPriority != currentPriority) {
            Process.setThreadPriority(Process.myTid(), state.prevPriority);
        }
    }

    /* access modifiers changed from: protected */
    public void setBoostToPriority(int priority) {
        this.mBoostToPriority = priority;
        int tid = Process.myTid();
        int prevPriority = Process.getThreadPriority(tid);
        if (this.mThreadState.get().regionCounter != 0 && prevPriority != priority) {
            Process.setThreadPriority(tid, priority);
        }
    }
}
