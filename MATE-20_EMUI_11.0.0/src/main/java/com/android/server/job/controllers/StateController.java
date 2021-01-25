package com.android.server.job.controllers;

import android.content.Context;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.util.function.Predicate;

public abstract class StateController extends AbsStateController {
    private static final String TAG = "JobScheduler.SC";
    protected final JobSchedulerService.Constants mConstants;
    protected final Context mContext;
    protected final Object mLock;
    protected final JobSchedulerService mService;
    protected final StateChangedListener mStateChangedListener;

    public abstract void dumpControllerStateLocked(ProtoOutputStream protoOutputStream, long j, Predicate<JobStatus> predicate);

    public abstract void dumpControllerStateLocked(IndentingPrintWriter indentingPrintWriter, Predicate<JobStatus> predicate);

    public abstract void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus jobStatus2);

    public abstract void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus jobStatus2, boolean z);

    StateController(JobSchedulerService service) {
        this.mService = service;
        this.mStateChangedListener = service;
        this.mContext = service.getTestableContext();
        this.mLock = service.getLock();
        this.mConstants = service.getConstants();
    }

    public void onSystemServicesReady() {
    }

    public void prepareForExecutionLocked(JobStatus jobStatus) {
    }

    public void rescheduleForFailureLocked(JobStatus newJob, JobStatus failureToReschedule) {
    }

    public void onConstantsUpdatedLocked() {
    }

    public void onAppRemovedLocked(String packageName, int uid) {
    }

    public void onUserRemovedLocked(int userId) {
    }

    public void evaluateStateLocked(JobStatus jobStatus) {
    }

    public void reevaluateStateLocked(int uid) {
    }

    /* access modifiers changed from: protected */
    public boolean wouldBeReadyWithConstraintLocked(JobStatus jobStatus, int constraint) {
        boolean jobWouldBeReady = jobStatus.wouldBeReadyWithConstraint(constraint);
        if (JobSchedulerService.DEBUG) {
            Slog.v(TAG, "wouldBeReadyWithConstraintLocked: " + jobStatus.toShortString() + " constraint=" + constraint + " readyWithConstraint=" + jobWouldBeReady);
        }
        if (!jobWouldBeReady) {
            return false;
        }
        return this.mService.areComponentsInPlaceLocked(jobStatus);
    }

    public void dumpConstants(IndentingPrintWriter pw) {
    }

    public void dumpConstants(ProtoOutputStream proto) {
    }
}
