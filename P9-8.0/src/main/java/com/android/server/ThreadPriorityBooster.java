package com.android.server;

import android.os.Process;

public class ThreadPriorityBooster {
    private volatile int mBoostToPriority;
    private final int mLockGuardIndex;
    private final ThreadLocal<PriorityState> mThreadState = new ThreadLocal<PriorityState>() {
        protected PriorityState initialValue() {
            return new PriorityState();
        }
    };

    private static class PriorityState {
        int prevPriority;
        int regionCounter;

        /* synthetic */ PriorityState(PriorityState -this0) {
            this();
        }

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
        PriorityState state = (PriorityState) this.mThreadState.get();
        if (state.regionCounter == 0) {
            state.prevPriority = prevPriority;
            if (prevPriority > this.mBoostToPriority) {
                Process.setThreadPriority(tid, this.mBoostToPriority);
            }
        }
        state.regionCounter++;
    }

    public void reset() {
        PriorityState state = (PriorityState) this.mThreadState.get();
        state.regionCounter--;
        int currentPriority = Process.getThreadPriority(Process.myTid());
        if (state.regionCounter == 0 && state.prevPriority != currentPriority) {
            Process.setThreadPriority(Process.myTid(), state.prevPriority);
        }
    }

    protected void setBoostToPriority(int priority) {
        this.mBoostToPriority = priority;
        PriorityState state = (PriorityState) this.mThreadState.get();
        int tid = Process.myTid();
        int prevPriority = Process.getThreadPriority(tid);
        if (state.regionCounter != 0 && prevPriority != priority) {
            Process.setThreadPriority(tid, priority);
        }
    }
}
