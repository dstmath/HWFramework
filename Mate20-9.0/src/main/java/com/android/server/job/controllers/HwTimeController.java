package com.android.server.job.controllers;

import android.os.SystemClock;
import android.os.UserHandle;
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
    private static final String TAG = "JobScheduler.Time";
    private final List<JobStatus> mProxyJobs = new LinkedList();

    public HwTimeController(JobSchedulerService service) {
        super(service);
    }

    public void maybeStopTrackingJobLocked(JobStatus job, JobStatus incomingJob, boolean forUpdate) {
        super.maybeStopTrackingJobLocked(job, incomingJob, forUpdate);
        this.mProxyJobs.remove(job);
    }

    /* access modifiers changed from: package-private */
    public boolean maybeProxyServiceLocked(JobStatus job) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null || !pgm.isServiceProxy(job.getServiceComponent(), job.getSourcePackageName())) {
            return false;
        }
        this.mProxyJobs.add(job);
        return true;
    }

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

    public boolean proxyServiceLocked(int type, List<String> services) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        boolean resetAlarm = false;
        if (pgm == null) {
            return false;
        }
        if (type == 0) {
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
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
            for (JobStatus job2 : proxyJobs) {
                if (pgm.isServiceMatchList(job2.getServiceComponent(), job2.getSourcePackageName(), services)) {
                    this.mProxyJobs.remove(job2);
                    maybeStartTrackingJobLocked(job2, null);
                }
            }
            return true;
        } else if (type != 2) {
            return false;
        } else {
            List<JobStatus> proxyJobs2 = new LinkedList<>();
            proxyJobs2.addAll(this.mProxyJobs);
            this.mProxyJobs.clear();
            for (JobStatus job3 : proxyJobs2) {
                maybeStartTrackingJobLocked(job3, null);
            }
            return true;
        }
    }
}
