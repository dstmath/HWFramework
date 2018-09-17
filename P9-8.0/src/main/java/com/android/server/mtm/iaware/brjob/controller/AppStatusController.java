package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AppStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "AppStatus";
    private static final String TAG = "AppStatusController";
    private static AppStatusController mSingleton;
    private static Object sCreationLock = new Object();
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList();

    public static AppStatusController get(AwareJobSchedulerService jms) {
        AppStatusController appStatusController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new AppStatusController(jms, jms.getContext(), jms.getLock());
            }
            appStatusController = mSingleton;
        }
        return appStatusController;
    }

    private AppStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("AppStatus")) {
            if (AwareJobSchedulerConstants.APP_STATUS_RUN.equals(job.getActionFilterValue("AppStatus"))) {
                job.setSatisfied("AppStatus", false);
            } else {
                job.setSatisfied("AppStatus", true);
            }
            addJobLocked(this.mTrackedJobs, job);
        }
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("AppStatus")) {
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    AppStatusController tracked job num: " + this.mTrackedJobs.size());
        }
    }
}
