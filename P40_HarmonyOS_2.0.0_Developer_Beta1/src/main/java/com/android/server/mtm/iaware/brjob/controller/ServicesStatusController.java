package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.rms.iaware.AwareLog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerExt;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServicesStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "ServicesStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String TAG = "ServicesStatusController";
    private static ServicesStatusController sSingleton;
    private boolean mIsRoaming = false;
    private BroadcastReceiver mServcicesStatusReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.controller.ServicesStatusController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ServicesStatusController.this.isViceCardServicesStatusBroadcast(intent)) {
                    if (ServicesStatusController.this.debug) {
                        AwareLog.i(ServicesStatusController.TAG, "iaware_brjob br is vice card service, not change state");
                    }
                } else if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                    boolean changed = false;
                    ServiceState serviceState = ServiceStateEx.newFromBundle(intent.getExtras());
                    if (!(serviceState.getRoaming() == ServicesStatusController.this.mIsRoaming && serviceState.getState() == ServicesStatusController.this.mServicesState)) {
                        changed = true;
                    }
                    ServicesStatusController.this.mIsRoaming = serviceState.getRoaming();
                    ServicesStatusController.this.mServicesState = serviceState.getState();
                    if (changed) {
                        if (ServicesStatusController.this.debug) {
                            AwareLog.i(ServicesStatusController.TAG, "iaware_brjob service changed, state: " + ServicesStatusController.this.mServicesState + ", isRoaming: " + ServicesStatusController.this.mIsRoaming);
                        }
                        ServicesStatusController.this.updateJobs();
                    }
                }
            }
        }
    };
    private int mServicesState = 1;
    private TelephonyManager mTelephonyManager;
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private ServicesStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (this.mContext != null) {
            ContextEx.registerReceiverAsUser(this.mContext, this.mServcicesStatusReceiver, UserHandleEx.SYSTEM, new IntentFilter("android.intent.action.SERVICE_STATE"), (String) null, (Handler) null);
            Object obj = this.mContext.getSystemService("phone");
            if (obj instanceof TelephonyManager) {
                this.mTelephonyManager = (TelephonyManager) obj;
            }
        }
    }

    public static ServicesStatusController get(AwareJobSchedulerService jms) {
        ServicesStatusController servicesStatusController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new ServicesStatusController(jms, jms.getContext(), jms.getLock());
            }
            servicesStatusController = sSingleton;
        }
        return servicesStatusController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("ServicesStatus")) {
            String value = job.getActionFilterValue("ServicesStatus");
            String action = job.getAction();
            Intent intent = job.getIntent();
            if (action == null || intent == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob action: ");
                String str = "null";
                sb.append(action == null ? str : action);
                sb.append(", intent: ");
                Intent intent2 = str;
                if (intent != null) {
                    intent2 = intent;
                }
                sb.append(intent2);
                AwareLog.w(TAG, sb.toString());
                job.setSatisfied("ServicesStatus", false);
            } else if (isViceCardServicesStatusBroadcast(intent)) {
                job.setSatisfied("ServicesStatus", true);
            } else {
                resetServicesState();
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob start tracking, action:" + action + ", value: " + value + ", is roaming:" + this.mIsRoaming + ", service state: " + this.mServicesState);
                }
                checkSatisfiedLocked(job, value);
                addJobLocked(this.mTrackedJobs, job);
            }
        }
    }

    private boolean checkSatisfiedLocked(AwareJobStatus job, String filterValue) {
        boolean changeToSatisfied = false;
        boolean match = false;
        String[] values = filterValue.split("[:]");
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                String str = values[i];
                char c = 65535;
                int hashCode = str.hashCode();
                boolean z = false;
                if (hashCode != -2087582999) {
                    if (hashCode != 1143024442) {
                        if (hashCode == 2084956153 && str.equals(AwareJobSchedulerConstants.SERVICES_STATUS_ROAMING)) {
                            c = 0;
                        }
                    } else if (str.equals(AwareJobSchedulerConstants.SERVICES_STATUS_DISCONNECTED)) {
                        c = 2;
                    }
                } else if (str.equals(AwareJobSchedulerConstants.SERVICES_STATUS_CONNECTED)) {
                    c = 1;
                }
                if (c == 0) {
                    match = this.mIsRoaming;
                } else if (c == 1) {
                    if (this.mServicesState == 0) {
                        z = true;
                    }
                    match = z;
                } else if (c == 2) {
                    if (this.mServicesState != 0) {
                        z = true;
                    }
                    match = z;
                }
                if (match) {
                    break;
                }
            }
        }
        if (match && !job.isSatisfied("ServicesStatus")) {
            changeToSatisfied = true;
        }
        job.setSatisfied("ServicesStatus", match);
        return changeToSatisfied;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("ServicesStatus")) {
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    ServicesStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now services state: " + this.mServicesState + ", isRoaming: " + this.mIsRoaming);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateJobs() {
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
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob onControllerStateChanged");
                }
                this.mStateChangedListener.onControllerStateChanged(changedJobs);
            }
        }
    }

    private void resetServicesState() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        ServiceState state = telephonyManager != null ? telephonyManager.getServiceState() : null;
        if (state != null) {
            this.mIsRoaming = state.getRoaming();
            this.mServicesState = state.getState();
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob resetServicesState mServicesState: " + this.mServicesState + ", mIsRoaming : " + this.mIsRoaming);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isViceCardServicesStatusBroadcast(Intent intent) {
        if (!"android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
            return false;
        }
        int subKey = intent.getIntExtra("subscription", -1);
        int defaultSubId = SubscriptionManagerExt.getDefaultSubId();
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob is Vice Card ServicesStatus Broadcast, subkey : " + subKey + ", defaultSubId : " + defaultSubId);
        }
        if (subKey == -1 || subKey != defaultSubId) {
            return true;
        }
        return false;
    }
}
