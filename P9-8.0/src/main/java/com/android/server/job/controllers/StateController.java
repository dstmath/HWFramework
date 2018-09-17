package com.android.server.job.controllers;

import android.content.Context;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;

public abstract class StateController extends AbsStateController {
    protected static final boolean DEBUG = false;
    protected final Context mContext;
    protected final Object mLock;
    protected final StateChangedListener mStateChangedListener;

    public abstract void dumpControllerStateLocked(PrintWriter printWriter, int i);

    public abstract void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus jobStatus2);

    public abstract void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus jobStatus2, boolean z);

    public StateController(StateChangedListener stateChangedListener, Context context, Object lock) {
        this.mStateChangedListener = stateChangedListener;
        this.mContext = context;
        this.mLock = lock;
    }

    public void prepareForExecutionLocked(JobStatus jobStatus) {
    }

    public void rescheduleForFailureLocked(JobStatus newJob, JobStatus failureToReschedule) {
    }
}
