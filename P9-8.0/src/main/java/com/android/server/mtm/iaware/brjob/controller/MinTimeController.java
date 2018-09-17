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

public class MinTimeController extends AwareStateController {
    private static final String CONDITION_HOUR = "MinTime#Hour";
    private static final String CONDITION_MINUTE = "MinTime#Minute";
    private static final long HOUR_PERIOD_INTERVAL = 3600000;
    private static final long MINUTE_PERIOD_INTERVAL = 60000;
    private static final String TAG = "MinTimeController";
    private static MinTimeController mSingleton;
    private static Object sCreationLock = new Object();
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList();

    public static MinTimeController get(AwareJobSchedulerService jms) {
        MinTimeController minTimeController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new MinTimeController(jms, jms.getContext(), jms.getLock());
            }
            minTimeController = mSingleton;
        }
        return minTimeController;
    }

    private MinTimeController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("MinTime#Hour") || job.hasConstraint("MinTime#Minute")) {
                String filter;
                long intervalUnit;
                String condition = "";
                if (job.hasConstraint("MinTime#Hour")) {
                    filter = job.getActionFilterValue("MinTime#Hour");
                    intervalUnit = 3600000;
                    condition = "MinTime#Hour";
                } else {
                    filter = job.getActionFilterValue("MinTime#Minute");
                    intervalUnit = 60000;
                    condition = "MinTime#Minute";
                }
                if (TextUtils.isEmpty(filter)) {
                    AwareLog.w(TAG, "iaware_brjob min time on hour state value is null.");
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
                try {
                    if (StartupByBroadcastRecorder.getInstance().minTimeInterval(receiverPkg, action, ((long) Integer.parseInt(filter)) * intervalUnit)) {
                        job.setSatisfied(condition, true);
                    } else {
                        job.setSatisfied(condition, false);
                    }
                    addJobLocked(this.mTrackedJobs, job);
                } catch (NumberFormatException e) {
                    AwareLog.e(TAG, "iaware_brjob MinTime value format is error, setShouldRunByError.");
                    job.setSatisfied(condition, false);
                }
            }
        }
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("MinTime#Hour") || job.hasConstraint("MinTime#Minute")) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob stop tracking begin");
                }
                this.mTrackedJobs.remove(job);
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    MinTimeController tracked job num: " + this.mTrackedJobs.size());
        }
    }
}
