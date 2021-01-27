package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.StartupByBroadcastRecorder;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LimitNumController extends AwareStateController {
    private static final String CONDITION_DAY = "LimitNum#Day";
    private static final String CONDITION_HOUR = "LimitNum#Hour";
    private static final Object CREATE_LOCK = new Object();
    private static final long DAY_PERIOD_INTERVAL = 86400000;
    private static final long HOUR_PERIOD_INTERVAL = 3600000;
    private static final String TAG = "LimitNumController";
    private static LimitNumController sSingleton;
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private LimitNumController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public static LimitNumController get(AwareJobSchedulerService jms) {
        LimitNumController limitNumController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new LimitNumController(jms, jms.getContext(), jms.getLock());
            }
            limitNumController = sSingleton;
        }
        return limitNumController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        String condition;
        long interval;
        String filter;
        if (job != null) {
            if (job.hasConstraint("LimitNum#Day") || job.hasConstraint("LimitNum#Hour")) {
                if (job.hasConstraint("LimitNum#Day")) {
                    filter = job.getActionFilterValue("LimitNum#Day");
                    interval = DAY_PERIOD_INTERVAL;
                    condition = "LimitNum#Day";
                } else {
                    filter = job.getActionFilterValue("LimitNum#Hour");
                    interval = HOUR_PERIOD_INTERVAL;
                    condition = "LimitNum#Hour";
                }
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob maybeStartTrackingJobLocked, " + condition);
                }
                boolean z = false;
                if (TextUtils.isEmpty(filter)) {
                    AwareLog.w(TAG, "iaware_brjob limitnum state value is null.");
                    job.setSatisfied(condition, false);
                    return;
                }
                String receiverPkg = job.getReceiverPkg();
                String action = job.getAction();
                if (TextUtils.isEmpty(action)) {
                    AwareLog.w(TAG, "iaware_brjob action is null.");
                    job.setSatisfied(condition, false);
                    return;
                }
                int launchCount = StartupByBroadcastRecorder.getInstance().getStartupCountsByBroadcast(receiverPkg, action, interval);
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob StartupCounts: " + launchCount + "----" + filter);
                }
                try {
                    if (Integer.parseInt(filter) > launchCount) {
                        z = true;
                    }
                    job.setSatisfied(condition, z);
                    addJobLocked(this.mTrackedJobs, job);
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "iaware_brjob limitnum state value format is error, setShouldRunByError.");
                    job.setSatisfied(condition, false);
                }
            }
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("LimitNum#Day") || job.hasConstraint("LimitNum#Hour")) {
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob stop tracking begin");
                }
                this.mTrackedJobs.remove(job);
            }
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    LimitNumController tracked job num: " + this.mTrackedJobs.size());
            StartupByBroadcastRecorder.getInstance().dump(pw);
        }
    }
}
