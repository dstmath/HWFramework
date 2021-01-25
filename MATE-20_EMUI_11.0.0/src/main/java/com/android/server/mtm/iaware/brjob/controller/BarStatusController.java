package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BarStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "BarStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String EXTRA_VISIBLE = "visible";
    private static final String INTENT_STATUS_BAR_VISIBLE_CHANGE = "com.android.systemui.statusbar.visible.change";
    private static final String TAG = "BarStatusController";
    private static BarStatusController sSingleton;
    private BroadcastReceiver mBarStatusReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.controller.BarStatusController.AnonymousClass1 */

        private void notifyChangedJobsLocked() {
            boolean changeToSatisfied = false;
            List<AwareJobStatus> changedJobs = new ArrayList<>();
            Iterator it = BarStatusController.this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                AwareJobStatus job = (AwareJobStatus) it.next();
                String value = job.getActionFilterValue("BarStatus");
                if (BarStatusController.this.mIsVisible.equals(value) && !job.isSatisfied("BarStatus")) {
                    job.setSatisfied("BarStatus", true);
                    changeToSatisfied = true;
                    changedJobs.add(job);
                } else if (!BarStatusController.this.mIsVisible.equals(value) && job.isSatisfied("BarStatus")) {
                    job.setSatisfied("BarStatus", false);
                }
            }
            if (changeToSatisfied) {
                if (BarStatusController.this.debug) {
                    AwareLog.i(BarStatusController.TAG, "iaware_brjob onControllerStateChanged");
                }
                BarStatusController.this.mStateChangedListener.onControllerStateChanged(changedJobs);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && BarStatusController.INTENT_STATUS_BAR_VISIBLE_CHANGE.equals(intent.getAction())) {
                BarStatusController.this.updateBarVisible(intent);
                synchronized (BarStatusController.this.mLock) {
                    notifyChangedJobsLocked();
                }
            }
        }
    };
    private String mIsVisible = AwareJobSchedulerConstants.BAR_STATUS_OFF;
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private BarStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (this.mContext != null) {
            ContextEx.registerReceiverAsUser(this.mContext, this.mBarStatusReceiver, UserHandleEx.SYSTEM, new IntentFilter(INTENT_STATUS_BAR_VISIBLE_CHANGE), (String) null, (Handler) null);
        }
    }

    public static BarStatusController get(AwareJobSchedulerService jms) {
        BarStatusController barStatusController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new BarStatusController(jms, jms.getContext(), jms.getLock());
            }
            barStatusController = sSingleton;
        }
        return barStatusController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("BarStatus")) {
            String value = job.getActionFilterValue("BarStatus");
            String action = job.getAction();
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob start tracking, action:" + action + ", value: " + value);
            }
            if (INTENT_STATUS_BAR_VISIBLE_CHANGE.equals(action)) {
                updateBarVisible(job.getIntent());
            }
            if (this.mIsVisible.equals(value)) {
                job.setSatisfied("BarStatus", true);
            } else {
                job.setSatisfied("BarStatus", false);
            }
            addJobLocked(this.mTrackedJobs, job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("BarStatus")) {
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    BarStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now bar status is: " + this.mIsVisible);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBarVisible(Intent intent) {
        if (intent != null) {
            this.mIsVisible = Boolean.parseBoolean(intent.getStringExtra(EXTRA_VISIBLE)) ? AwareJobSchedulerConstants.BAR_STATUS_ON : AwareJobSchedulerConstants.BAR_STATUS_OFF;
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob bar status changed, now is: " + this.mIsVisible);
            }
        }
    }
}
