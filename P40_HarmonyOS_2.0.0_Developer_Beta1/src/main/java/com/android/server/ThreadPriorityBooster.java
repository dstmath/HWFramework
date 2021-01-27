package com.android.server;

import android.os.Process;

public class ThreadPriorityBooster {
    private static final boolean ENABLE_LOCK_GUARD = false;
    private static final int PRIORITY_NOT_ADJUSTED = Integer.MAX_VALUE;
    private volatile int mBoostToPriority;
    private final int mLockGuardIndex;
    private final ThreadLocal<PriorityState> mThreadState = new ThreadLocal<PriorityState>() {
        /* class com.android.server.ThreadPriorityBooster.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public PriorityState initialValue() {
            return new PriorityState();
        }
    };

    public ThreadPriorityBooster(int boostToPriority, int lockGuardIndex) {
        this.mBoostToPriority = boostToPriority;
        this.mLockGuardIndex = lockGuardIndex;
    }

    public void boost() {
        int prevPriority;
        PriorityState state = this.mThreadState.get();
        if (state.regionCounter == 0 && (prevPriority = Process.getThreadPriority(state.tid)) > this.mBoostToPriority) {
            Process.setThreadPriority(state.tid, this.mBoostToPriority);
            state.prevPriority = prevPriority;
        }
        state.regionCounter++;
    }

    public void reset() {
        PriorityState state = this.mThreadState.get();
        state.regionCounter--;
        if (state.regionCounter == 0 && state.prevPriority != PRIORITY_NOT_ADJUSTED) {
            Process.setThreadPriority(state.tid, state.prevPriority);
            state.prevPriority = PRIORITY_NOT_ADJUSTED;
        }
    }

    /* access modifiers changed from: protected */
    public void setBoostToPriority(int priority) {
        this.mBoostToPriority = priority;
        PriorityState state = this.mThreadState.get();
        if (state.regionCounter != 0 && Process.getThreadPriority(state.tid) != priority) {
            Process.setThreadPriority(state.tid, priority);
        }
    }

    /* access modifiers changed from: private */
    public static class PriorityState {
        int prevPriority;
        int regionCounter;
        final int tid;

        private PriorityState() {
            this.tid = Process.myTid();
            this.prevPriority = ThreadPriorityBooster.PRIORITY_NOT_ADJUSTED;
        }
    }
}
