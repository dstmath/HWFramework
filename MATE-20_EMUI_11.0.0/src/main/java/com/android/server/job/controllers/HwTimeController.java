package com.android.server.job.controllers;

import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.pg.PGManagerInternal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class HwTimeController extends TimeController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.HwTime";
    private final List<JobStatus> mProxyJobs = new LinkedList();

    public HwTimeController(JobSchedulerService service) {
        super(service);
    }

    @Override // com.android.server.job.controllers.TimeController, com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus job, JobStatus incomingJob, boolean forUpdate) {
        super.maybeStopTrackingJobLocked(job, incomingJob, forUpdate);
        this.mProxyJobs.remove(job);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.job.controllers.AbsStateController
    public boolean maybeProxyServiceLocked(JobStatus job) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null || !pgm.isServiceProxy(job.getServiceComponent(), job.getSourcePackageName())) {
            return false;
        }
        this.mProxyJobs.add(job);
        return true;
    }

    @Override // com.android.server.job.controllers.TimeController, com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        long nowElapsed = SystemClock.elapsedRealtime();
        super.dumpControllerStateLocked(pw, predicate);
        pw.println();
        pw.print("ProxyJob ");
        pw.print(this.mProxyJobs.size());
        pw.println(":");
        for (JobStatus ts : this.mProxyJobs) {
            pw.print("  #");
            ts.printUniqueId(pw);
            pw.print(" from ");
            UserHandle.formatUid(pw, ts.getSourceUid());
            pw.print(": Delay=");
            if (ts.hasTimingDelayConstraint()) {
                TimeUtils.formatDuration(ts.getEarliestRunTime(), nowElapsed, pw);
            } else {
                pw.print("N/A");
            }
            pw.print(", Deadline=");
            if (ts.hasDeadlineConstraint()) {
                TimeUtils.formatDuration(ts.getLatestRunTimeElapsed(), nowElapsed, pw);
            } else {
                pw.print("N/A");
            }
            pw.println();
        }
    }

    @Override // com.android.server.job.controllers.AbsStateController
    public boolean proxyServiceLocked(int type, List<String> services) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null) {
            return false;
        }
        if (type == 0) {
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            boolean resetAlarm = false;
            while (it.hasNext()) {
                JobStatus job = it.next();
                if (pgm.isServiceMatchList(job.getServiceComponent(), job.getSourcePackageName(), services)) {
                    it.remove();
                    this.mProxyJobs.add(job);
                    resetAlarm = true;
                }
            }
            if (resetAlarm) {
                checkExpiredDelaysAndResetAlarm();
                checkExpiredDeadlinesAndResetAlarm();
            }
            return true;
        } else if (type == 1) {
            List<JobStatus> proxyJobs = new LinkedList<>();
            proxyJobs.addAll(this.mProxyJobs);
            boolean ready = false;
            for (JobStatus job2 : proxyJobs) {
                if (pgm.isServiceMatchList(job2.getServiceComponent(), job2.getSourcePackageName(), services)) {
                    this.mProxyJobs.remove(job2);
                    maybeStartTrackingJobLocked(job2, null);
                    if (job2.isReady()) {
                        if (DEBUG) {
                            Slog.i(TAG, "do unproxy, this job is ready: " + job2);
                        }
                        ready = true;
                    }
                }
            }
            if (ready) {
                this.mStateChangedListener.onControllerStateChanged();
            }
            return true;
        } else if (type != 2) {
            return false;
        } else {
            List<JobStatus> proxyJobs2 = new LinkedList<>();
            proxyJobs2.addAll(this.mProxyJobs);
            this.mProxyJobs.clear();
            boolean ready2 = false;
            for (JobStatus job3 : proxyJobs2) {
                maybeStartTrackingJobLocked(job3, null);
                if (job3.isReady()) {
                    if (DEBUG) {
                        Slog.i(TAG, "do unproxy all, this job is ready: " + job3);
                    }
                    ready2 = true;
                }
            }
            if (ready2) {
                this.mStateChangedListener.onControllerStateChanged();
            }
            return true;
        }
    }
}
