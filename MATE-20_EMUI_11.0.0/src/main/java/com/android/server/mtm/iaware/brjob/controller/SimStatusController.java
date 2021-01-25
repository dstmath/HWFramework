package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.rms.iaware.AwareLog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.SubscriptionManagerExt;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "SIMStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String TAG = "SimStatusController";
    private static SimStatusController sSingleton;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.controller.SimStatusController.AnonymousClass1 */

        private void notifyChangedJobsLocked() {
            boolean changeToSatisfied = false;
            List<AwareJobStatus> changedJobs = new ArrayList<>();
            Iterator it = SimStatusController.this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                AwareJobStatus job = (AwareJobStatus) it.next();
                String value = job.getActionFilterValue("SIMStatus");
                if (SimStatusController.this.matchSimStatus(value) && !job.isSatisfied("SIMStatus")) {
                    job.setSatisfied("SIMStatus", true);
                    changeToSatisfied = true;
                    changedJobs.add(job);
                } else if (!SimStatusController.this.matchSimStatus(value) && job.isSatisfied("SIMStatus")) {
                    job.setSatisfied("SIMStatus", false);
                }
            }
            if (changeToSatisfied) {
                if (SimStatusController.this.debug) {
                    AwareLog.i(SimStatusController.TAG, "iaware_brjob onControllerStateChanged");
                }
                SimStatusController.this.mStateChangedListener.onControllerStateChanged(changedJobs);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (SimStatusController.this.isViceCardSimStateBroadcast(intent)) {
                    if (SimStatusController.this.debug) {
                        AwareLog.i(SimStatusController.TAG, "iaware_brjob br is vice card sim, not change state");
                    }
                } else if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    String state = SimStatusController.this.getSimStateFromIntent(intent);
                    if (SimStatusController.this.debug) {
                        AwareLog.i(SimStatusController.TAG, "iaware_brjob onReceiver sim state:" + state);
                    }
                    if (!SimStatusController.this.mSimStatus.equals(state)) {
                        synchronized (SimStatusController.this.mLock) {
                            SimStatusController.this.mSimStatus = state;
                            notifyChangedJobsLocked();
                        }
                    }
                }
            }
        }
    };
    private String mSimStatus = "UNKNOWN";
    private TelephonyManager mTelephonyManager;
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private SimStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (this.mContext != null) {
            ContextEx.registerReceiverAsUser(this.mContext, this.mSimStateReceiver, UserHandleEx.SYSTEM, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"), (String) null, (Handler) null);
            Object obj = this.mContext.getSystemService("phone");
            if (obj instanceof TelephonyManager) {
                this.mTelephonyManager = (TelephonyManager) obj;
            }
        }
    }

    public static SimStatusController get(AwareJobSchedulerService jms) {
        SimStatusController simStatusController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new SimStatusController(jms, jms.getContext(), jms.getLock());
            }
            simStatusController = sSingleton;
        }
        return simStatusController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("SIMStatus")) {
            this.mSimStatus = getSimState();
            String filter = job.getActionFilterValue("SIMStatus");
            if (TextUtils.isEmpty(filter)) {
                AwareLog.w(TAG, "iaware_brjob sim status value is null");
                job.setSatisfied("SIMStatus", false);
                return;
            }
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob start tracking SimState, filter:" + filter + "current: " + this.mSimStatus);
            }
            if (job.getIntent() == null || !isViceCardSimStateBroadcast(job.getIntent())) {
                job.setSatisfied("SIMStatus", matchSimStatus(filter));
                addJobLocked(this.mTrackedJobs, job);
                return;
            }
            job.setSatisfied("SIMStatus", true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean matchSimStatus(String filterValue) {
        String[] values;
        for (String str : filterValue.split("[:]")) {
            if (this.mSimStatus.equals(str)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus jobStatus) {
        if (jobStatus != null && jobStatus.hasConstraint("SIMStatus")) {
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(jobStatus);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    SimStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now sim state is " + this.mSimStatus);
        }
    }

    private String getSimState() {
        if (this.mTelephonyManager == null) {
            if (this.mContext == null) {
                return "UNKNOWN";
            }
            Object obj = this.mContext.getSystemService("phone");
            if (!(obj instanceof TelephonyManager)) {
                return "UNKNOWN";
            }
            this.mTelephonyManager = (TelephonyManager) obj;
        }
        int state = this.mTelephonyManager.getSimState();
        if (state == 1) {
            return AwareJobSchedulerConstants.SIM_STATUS_ABSENT;
        }
        if (state == 2 || state == 3 || state == 4) {
            return AwareJobSchedulerConstants.SIM_STATUS_LOCKED;
        }
        if (state != 5) {
            return "UNKNOWN";
        }
        return AwareJobSchedulerConstants.SIM_STATUS_READY;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getSimStateFromIntent(Intent intent) {
        String curSimState = intent.getStringExtra("ss");
        if (curSimState == null) {
            return "UNKNOWN";
        }
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob curSimState : " + curSimState);
        }
        if (AwareJobSchedulerConstants.SIM_STATUS_READY.equals(curSimState)) {
            return AwareJobSchedulerConstants.SIM_STATUS_READY;
        }
        if (AwareJobSchedulerConstants.SIM_STATUS_LOCKED.equals(curSimState) || "INTERNAL_LOCKED".equals(curSimState)) {
            return AwareJobSchedulerConstants.SIM_STATUS_LOCKED;
        }
        if (AwareJobSchedulerConstants.SIM_STATUS_ABSENT.equals(curSimState)) {
            return AwareJobSchedulerConstants.SIM_STATUS_ABSENT;
        }
        return "UNKNOWN";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isViceCardSimStateBroadcast(Intent intent) {
        if (!"android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
            return false;
        }
        int subKey = intent.getIntExtra("subscription", -1);
        int defaultSubId = SubscriptionManagerExt.getDefaultSubId();
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob is Vice Card sim state Broadcast, subkey : " + subKey + ", defaultSubId : " + defaultSubId);
        }
        if (subKey == -1 || subKey != defaultSubId) {
            return true;
        }
        return false;
    }
}
