package com.android.server.job.controllers;

import android.content.Context;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.TimeUtils;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import com.android.server.pg.PGManagerInternal;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HwTimeController extends TimeController {
    private static final String TAG = "JobScheduler.Time";
    private final List<JobStatus> mProxyJobs = new LinkedList();

    public static synchronized TimeController get(JobSchedulerService jms) {
        TimeController timeController;
        synchronized (HwTimeController.class) {
            if (mSingleton == null) {
                mSingleton = new HwTimeController(jms, jms.getContext(), jms.getLock());
            }
            timeController = mSingleton;
        }
        return timeController;
    }

    private HwTimeController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStopTrackingJobLocked(JobStatus job, JobStatus incomingJob, boolean forUpdate) {
        super.maybeStopTrackingJobLocked(job, incomingJob, forUpdate);
        this.mProxyJobs.remove(job);
    }

    boolean maybeProxyServiceLocked(JobStatus job) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null || !pgm.isServiceProxy(job.getServiceComponent(), job.getSourcePackageName())) {
            return false;
        }
        this.mProxyJobs.add(job);
        return true;
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        long nowElapsed = SystemClock.elapsedRealtime();
        super.dumpControllerStateLocked(pw, filterUid);
        pw.println();
        pw.print("ProxyJob ");
        pw.print(this.mProxyJobs.size());
        pw.println(":");
        for (JobStatus ts : this.mProxyJobs) {
            if (ts.shouldDump(filterUid)) {
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
    }

    public boolean proxyServiceLocked(int type, List<String> services) {
        PGManagerInternal pgm = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        if (pgm == null) {
            return false;
        }
        Iterator<JobStatus> it;
        JobStatus job;
        List<JobStatus> proxyJobs;
        if (type == 0) {
            it = this.mTrackedJobs.iterator();
            boolean resetAlarm = false;
            while (it.hasNext()) {
                job = (JobStatus) it.next();
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
            proxyJobs = new LinkedList();
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
            proxyJobs = new LinkedList();
            proxyJobs.addAll(this.mProxyJobs);
            this.mProxyJobs.clear();
            for (JobStatus job22 : proxyJobs) {
                maybeStartTrackingJobLocked(job22, null);
            }
            return true;
        }
    }
}
