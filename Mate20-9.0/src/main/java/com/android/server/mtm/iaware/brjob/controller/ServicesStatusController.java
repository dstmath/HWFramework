package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServicesStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "ServicesStatus";
    private static final String TAG = "ServicesStatusController";
    private static ServicesStatusController mSingleton;
    private static Object sCreationLock = new Object();
    /* access modifiers changed from: private */
    public boolean mIsRoaming = false;
    private BroadcastReceiver mServcicesStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ServicesStatusController.this.isViceCardServicesStatusBroadcast(intent)) {
                    if (ServicesStatusController.this.DEBUG) {
                        AwareLog.i(ServicesStatusController.TAG, "iaware_brjob br is vice card service, not change state");
                    }
                    return;
                }
                if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                    boolean changed = false;
                    ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                    if (!(serviceState.getRoaming() == ServicesStatusController.this.mIsRoaming && serviceState.getState() == ServicesStatusController.this.mServicesState)) {
                        changed = true;
                    }
                    boolean unused = ServicesStatusController.this.mIsRoaming = serviceState.getRoaming();
                    int unused2 = ServicesStatusController.this.mServicesState = serviceState.getState();
                    if (changed) {
                        if (ServicesStatusController.this.DEBUG) {
                            AwareLog.i(ServicesStatusController.TAG, "iaware_brjob service changed, state: " + ServicesStatusController.this.mServicesState + ", isRoaming: " + ServicesStatusController.this.mIsRoaming);
                        }
                        ServicesStatusController.this.updateJobs();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mServicesState = 1;
    private TelephonyManager mTelephonyManager;
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    public static ServicesStatusController get(AwareJobSchedulerService jms) {
        ServicesStatusController servicesStatusController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new ServicesStatusController(jms, jms.getContext(), jms.getLock());
            }
            servicesStatusController = mSingleton;
        }
        return servicesStatusController;
    }

    private ServicesStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (this.mContext != null) {
            this.mContext.registerReceiverAsUser(this.mServcicesStatusReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.SERVICE_STATE"), null, null);
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("ServicesStatus")) {
            String value = job.getActionFilterValue("ServicesStatus");
            String action = job.getAction();
            Intent intent = job.getIntent();
            if (action == null || intent == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob action: ");
                sb.append(action == null ? "null" : action);
                sb.append(", intent: ");
                sb.append(intent == null ? "null" : intent);
                AwareLog.w(TAG, sb.toString());
                job.setSatisfied("ServicesStatus", false);
            } else if (isViceCardServicesStatusBroadcast(intent)) {
                job.setSatisfied("ServicesStatus", true);
            } else {
                resetServicesState();
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob start tracking, action:" + action + ", value: " + value + ", is roaming:" + this.mIsRoaming + ", service state: " + this.mServicesState);
                }
                checkSatisfiedLocked(job, value);
                addJobLocked(this.mTrackedJobs, job);
            }
        }
    }

    private boolean checkSatisfiedLocked(AwareJobStatus job, String filterValue) {
        boolean changeToSatisfied = false;
        String[] values = filterValue.split("[:]");
        boolean match = false;
        for (int i = 0; i < values.length; i++) {
            if (AwareJobSchedulerConstants.SERVICES_STATUS_ROAMING.equals(values[i])) {
                match = this.mIsRoaming;
            } else {
                boolean z = true;
                if (AwareJobSchedulerConstants.SERVICES_STATUS_CONNECTED.equals(values[i])) {
                    if (this.mServicesState != 0) {
                        z = false;
                    }
                    match = z;
                } else if (AwareJobSchedulerConstants.SERVICES_STATUS_DISCONNECTED.equals(values[i])) {
                    if (this.mServicesState == 0) {
                        z = false;
                    }
                    match = z;
                }
            }
            if (match) {
                break;
            }
        }
        if (match && !job.isSatisfied("ServicesStatus")) {
            changeToSatisfied = true;
        }
        job.setSatisfied("ServicesStatus", match);
        return changeToSatisfied;
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("ServicesStatus")) {
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    ServicesStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now services state: " + this.mServicesState + ", isRoaming: " + this.mIsRoaming);
        }
    }

    /* access modifiers changed from: private */
    public void updateJobs() {
        synchronized (this.mLock) {
            boolean changeToSatisfied = false;
            List<AwareJobStatus> changedJobs = new ArrayList<>();
            Iterator<AwareJobStatus> it = this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                AwareJobStatus job = it.next();
                changeToSatisfied = checkSatisfiedLocked(job, job.getActionFilterValue("ServicesStatus"));
                if (changeToSatisfied) {
                    changedJobs.add(job);
                }
            }
            if (changeToSatisfied) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob onControllerStateChanged");
                }
                this.mStateChangedListener.onControllerStateChanged(changedJobs);
            }
        }
    }

    private void resetServicesState() {
        ServiceState state = this.mTelephonyManager != null ? this.mTelephonyManager.getServiceState() : null;
        if (state != null) {
            this.mIsRoaming = state.getRoaming();
            this.mServicesState = state.getState();
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob resetServicesState mServicesState: " + this.mServicesState + ", mIsRoaming : " + this.mIsRoaming);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isViceCardServicesStatusBroadcast(Intent intent) {
        if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
            int subKey = intent.getIntExtra("subscription", -1);
            int defaultSubId = SubscriptionManager.getDefaultSubId();
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob is Vice Card ServicesStatus Broadcast, subkey : " + subKey + ", defaultSubId : " + defaultSubId);
            }
            if (subKey == -1 || subKey != defaultSubId) {
                return true;
            }
        }
        return false;
    }
}
