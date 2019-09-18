package com.android.server.job.controllers;

import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.AppStateTracker;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore;
import com.android.server.pm.DumpState;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BackgroundJobsController extends StateController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    static final int KNOWN_ACTIVE = 1;
    static final int KNOWN_INACTIVE = 2;
    private static final String TAG = "JobScheduler.Background";
    static final int UNKNOWN = 0;
    private final AppStateTracker mAppStateTracker = ((AppStateTracker) Preconditions.checkNotNull((AppStateTracker) LocalServices.getService(AppStateTracker.class)));
    private final AppStateTracker.Listener mForceAppStandbyListener = new AppStateTracker.Listener() {
        public void updateAllJobs() {
            synchronized (BackgroundJobsController.this.mLock) {
                BackgroundJobsController.this.updateAllJobRestrictionsLocked();
            }
        }

        public void updateJobsForUid(int uid, boolean isActive) {
            synchronized (BackgroundJobsController.this.mLock) {
                BackgroundJobsController.this.updateJobRestrictionsForUidLocked(uid, isActive);
            }
        }

        public void updateJobsForUidPackage(int uid, String packageName, boolean isActive) {
            synchronized (BackgroundJobsController.this.mLock) {
                BackgroundJobsController.this.updateJobRestrictionsForUidLocked(uid, isActive);
            }
        }
    };

    private final class UpdateJobFunctor implements Consumer<JobStatus> {
        final int activeState;
        boolean mChanged = false;
        int mCheckedCount = 0;
        int mTotalCount = 0;

        public UpdateJobFunctor(int newActiveState) {
            this.activeState = newActiveState;
        }

        public void accept(JobStatus jobStatus) {
            this.mTotalCount++;
            this.mCheckedCount++;
            if (BackgroundJobsController.this.updateSingleJobRestrictionLocked(jobStatus, this.activeState)) {
                this.mChanged = true;
            }
        }
    }

    public BackgroundJobsController(JobSchedulerService service) {
        super(service);
        this.mAppStateTracker.addListener(this.mForceAppStandbyListener);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        updateSingleJobRestrictionLocked(jobStatus, 0);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        this.mAppStateTracker.dump(pw);
        pw.println();
        this.mService.getJobStore().forEachJob(predicate, (Consumer<JobStatus>) new Consumer(pw) {
            private final /* synthetic */ IndentingPrintWriter f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                BackgroundJobsController.lambda$dumpControllerStateLocked$0(BackgroundJobsController.this, this.f$1, (JobStatus) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$dumpControllerStateLocked$0(BackgroundJobsController backgroundJobsController, IndentingPrintWriter pw, JobStatus jobStatus) {
        int uid = jobStatus.getSourceUid();
        String sourcePkg = jobStatus.getSourcePackageName();
        pw.print("#");
        jobStatus.printUniqueId(pw);
        pw.print(" from ");
        UserHandle.formatUid(pw, uid);
        pw.print(backgroundJobsController.mAppStateTracker.isUidActive(uid) ? " active" : " idle");
        if (backgroundJobsController.mAppStateTracker.isUidPowerSaveWhitelisted(uid) || backgroundJobsController.mAppStateTracker.isUidTempPowerSaveWhitelisted(uid)) {
            pw.print(", whitelisted");
        }
        pw.print(": ");
        pw.print(sourcePkg);
        pw.print(" [RUN_ANY_IN_BACKGROUND ");
        pw.print(backgroundJobsController.mAppStateTracker.isRunAnyInBackgroundAppOpsAllowed(uid, sourcePkg) ? "allowed]" : "disallowed]");
        if ((jobStatus.satisfiedConstraints & DumpState.DUMP_CHANGES) != 0) {
            pw.println(" RUNNABLE");
        } else {
            pw.println(" WAITING");
        }
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long token = proto.start(fieldId);
        long mToken = proto.start(1146756268033L);
        this.mAppStateTracker.dumpProto(proto, 1146756268033L);
        this.mService.getJobStore().forEachJob(predicate, (Consumer<JobStatus>) new Consumer(proto) {
            private final /* synthetic */ ProtoOutputStream f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                BackgroundJobsController.lambda$dumpControllerStateLocked$1(BackgroundJobsController.this, this.f$1, (JobStatus) obj);
            }
        });
        proto.end(mToken);
        proto.end(token);
    }

    public static /* synthetic */ void lambda$dumpControllerStateLocked$1(BackgroundJobsController backgroundJobsController, ProtoOutputStream proto, JobStatus jobStatus) {
        long jsToken = proto.start(2246267895810L);
        jobStatus.writeToShortProto(proto, 1146756268033L);
        int sourceUid = jobStatus.getSourceUid();
        proto.write(1120986464258L, sourceUid);
        String sourcePkg = jobStatus.getSourcePackageName();
        proto.write(1138166333443L, sourcePkg);
        proto.write(1133871366148L, backgroundJobsController.mAppStateTracker.isUidActive(sourceUid));
        boolean z = true;
        proto.write(1133871366149L, backgroundJobsController.mAppStateTracker.isUidPowerSaveWhitelisted(sourceUid) || backgroundJobsController.mAppStateTracker.isUidTempPowerSaveWhitelisted(sourceUid));
        proto.write(1133871366150L, backgroundJobsController.mAppStateTracker.isRunAnyInBackgroundAppOpsAllowed(sourceUid, sourcePkg));
        if ((jobStatus.satisfiedConstraints & DumpState.DUMP_CHANGES) == 0) {
            z = false;
        }
        proto.write(1133871366151L, z);
        proto.end(jsToken);
    }

    /* access modifiers changed from: private */
    public void updateAllJobRestrictionsLocked() {
        updateJobRestrictionsLocked(-1, 0);
    }

    /* access modifiers changed from: private */
    public void updateJobRestrictionsForUidLocked(int uid, boolean isActive) {
        updateJobRestrictionsLocked(uid, isActive ? 1 : 2);
    }

    private void updateJobRestrictionsLocked(int filterUid, int newActiveState) {
        UpdateJobFunctor updateTrackedJobs = new UpdateJobFunctor(newActiveState);
        long time = 0;
        long start = DEBUG ? SystemClock.elapsedRealtimeNanos() : 0;
        JobStore store = this.mService.getJobStore();
        if (filterUid > 0) {
            store.forEachJobForSourceUid(filterUid, updateTrackedJobs);
        } else {
            store.forEachJob(updateTrackedJobs);
        }
        if (DEBUG) {
            time = SystemClock.elapsedRealtimeNanos() - start;
        }
        if (DEBUG) {
            Slog.d(TAG, String.format("Job status updated: %d/%d checked/total jobs, %d us", new Object[]{Integer.valueOf(updateTrackedJobs.mCheckedCount), Integer.valueOf(updateTrackedJobs.mTotalCount), Long.valueOf(time / 1000)}));
        }
        if (updateTrackedJobs.mChanged) {
            this.mStateChangedListener.onControllerStateChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateSingleJobRestrictionLocked(JobStatus jobStatus, int activeState) {
        boolean isActive;
        int uid = jobStatus.getSourceUid();
        boolean canRun = !this.mAppStateTracker.areJobsRestricted(uid, jobStatus.getSourcePackageName(), (jobStatus.getInternalFlags() & 1) != 0);
        if (activeState == 0) {
            isActive = this.mAppStateTracker.isUidActive(uid);
        } else {
            isActive = activeState == 1;
        }
        return jobStatus.setBackgroundNotRestrictedConstraintSatisfied(canRun) | jobStatus.setUidActive(isActive);
    }
}
