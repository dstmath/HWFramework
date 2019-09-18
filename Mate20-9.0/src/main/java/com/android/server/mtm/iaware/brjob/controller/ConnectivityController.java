package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.INetworkPolicyListener;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ConnectivityController extends AwareStateController {
    private static final String CONDITION_NETWORK = "NetworkStatus";
    private static final String CONDITION_WIFI = "WifiStatus";
    private static final String TAG = "ConnectivityController";
    private static ConnectivityController mSingleton;
    private static Object sCreationLock = new Object();
    /* access modifiers changed from: private */
    public ConnectivityManager mConnManager;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ConnectivityController.this.DEBUG) {
                    AwareLog.i(ConnectivityController.TAG, "iaware_brjob receive connectivity change :" + intent);
                }
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    int type = intent.getIntExtra("networkType", -1);
                    if (ConnectivityController.this.DEBUG) {
                        AwareLog.i(ConnectivityController.TAG, "iaware_brjob connectivity change, type:" + type);
                    }
                    if (type == 0 || type == 1) {
                        NetworkInfo info = null;
                        try {
                            info = (NetworkInfo) intent.getExtra("networkInfo");
                        } catch (ClassCastException e) {
                            if (ConnectivityController.this.DEBUG) {
                                AwareLog.e(ConnectivityController.TAG, "iaware_brjob get NetworkInfo from intent error.");
                            }
                        }
                        if (ConnectivityController.this.DEBUG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("iaware_brjob connectivity change, info:");
                            sb.append(info == null ? "null" : info);
                            AwareLog.i(ConnectivityController.TAG, sb.toString());
                        }
                        ConnectivityController.this.updateTrackedJobs(info);
                    }
                }
            }
        }
    };
    private INetworkPolicyListener mNetPolicyListener = new INetworkPolicyListener.Stub() {
        public void onUidRulesChanged(int uid, int uidRules) {
            if (ConnectivityController.this.DEBUG) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onUidRulesChanged, do nothing ");
            }
        }

        public void onMeteredIfacesChanged(String[] meteredIfaces) {
            if (ConnectivityController.this.DEBUG) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onMeteredIfacesChanged");
            }
            NetworkInfo info = ConnectivityController.this.mConnManager != null ? ConnectivityController.this.mConnManager.getActiveNetworkInfo() : null;
            if (ConnectivityController.this.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob onMeteredIfacesChanged info: ");
                sb.append(info == null ? "null" : info);
                AwareLog.i(ConnectivityController.TAG, sb.toString());
            }
            ConnectivityController.this.updateTrackedJobs(info);
        }

        public void onRestrictBackgroundChanged(boolean restrictBackground) {
            if (ConnectivityController.this.DEBUG) {
                AwareLog.d(ConnectivityController.TAG, "iaware_brjob onRestrictBackgroundChanged");
            }
            NetworkInfo info = ConnectivityController.this.mConnManager != null ? ConnectivityController.this.mConnManager.getActiveNetworkInfo() : null;
            if (ConnectivityController.this.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob onRestrictBackgroundChanged info: ");
                sb.append(info == null ? "null" : info);
                AwareLog.i(ConnectivityController.TAG, sb.toString());
            }
            ConnectivityController.this.updateTrackedJobs(info);
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) {
            if (ConnectivityController.this.DEBUG) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onUidPoliciesChanged, do nothing ");
            }
        }

        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) {
        }
    };
    private NetworkPolicyManager mNetPolicyManager;
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    public static ConnectivityController get(AwareJobSchedulerService jms) {
        ConnectivityController connectivityController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new ConnectivityController(jms, jms.getContext(), jms.getLock());
            }
            connectivityController = mSingleton;
        }
        return connectivityController;
    }

    private ConnectivityController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (context != null) {
            this.mConnManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
            this.mNetPolicyManager = (NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class);
            this.mContext.registerReceiverAsUser(this.mConnectivityReceiver, UserHandle.SYSTEM, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), null, null);
            this.mNetPolicyManager.registerListener(this.mNetPolicyListener);
        }
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("NetworkStatus") || job.hasConstraint("WifiStatus")) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob, start tracking.");
                }
                NetworkInfo info = getNetworkInfo();
                if (job.hasConstraint("NetworkStatus")) {
                    String netValue = job.getActionFilterValue("NetworkStatus");
                    if (this.DEBUG) {
                        AwareLog.i(TAG, "iaware_brjob, netValue: " + netValue);
                    }
                    boolean satisfied = false;
                    if (info == null) {
                        if ("MOBILEDATADSCON".equals(netValue)) {
                            satisfied = true;
                        }
                    } else if (isNetworkSatisfied(netValue, info)) {
                        satisfied = true;
                    } else {
                        satisfied = false;
                    }
                    job.setSatisfied("NetworkStatus", satisfied);
                }
                if (job.hasConstraint("WifiStatus")) {
                    String wifiValue = job.getActionFilterValue("WifiStatus");
                    if (this.DEBUG) {
                        AwareLog.i(TAG, "iaware_brjob, wifiValue: " + wifiValue);
                    }
                    boolean satisfied2 = false;
                    if (info == null) {
                        if ("WIFIDSCON".equals(wifiValue)) {
                            satisfied2 = true;
                        }
                    } else if (isWifiSatisfied(wifiValue, info)) {
                        satisfied2 = true;
                    } else {
                        satisfied2 = false;
                    }
                    job.setSatisfied("WifiStatus", satisfied2);
                }
                addJobLocked(this.mTrackedJobs, job);
            }
        }
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("NetworkStatus") || job.hasConstraint("WifiStatus")) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob stop tracking begin");
                }
                this.mTrackedJobs.remove(job);
            }
        }
    }

    public void dump(PrintWriter pw) {
        Object obj;
        if (pw != null) {
            pw.println("    ConnectivityController tracked job num: " + this.mTrackedJobs.size());
            StringBuilder sb = new StringBuilder();
            sb.append("        now network info is ");
            if (this.mConnManager == null) {
                obj = "[connMngr = null]";
            } else {
                obj = this.mConnManager.getActiveNetworkInfo();
            }
            sb.append(obj);
            pw.println(sb.toString());
        }
    }

    private NetworkInfo getNetworkInfo() {
        NetworkInfo info = this.mConnManager != null ? this.mConnManager.getActiveNetworkInfo() : null;
        if (this.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("iaware_brjob getNetworkInfo: ");
            sb.append(info == null ? "null" : info);
            AwareLog.i(TAG, sb.toString());
        }
        if (info == null) {
            AwareLog.w(TAG, "iaware_brjob network info is null.");
        }
        return info;
    }

    private boolean isWifiSatisfied(String wifiValue, NetworkInfo info) {
        if ("WIFICON".equals(wifiValue)) {
            if (info.getType() == 1 && info.isConnected()) {
                return true;
            }
        } else if ("WIFIDSCON".equals(wifiValue)) {
            if (info.getType() == 1 && !info.isConnected()) {
                return true;
            }
            if (info.getType() != 1 && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkSatisfied(String netValue, NetworkInfo info) {
        if ("MOBILEDATACON".equals(netValue)) {
            if (info.getType() == 0 && info.isConnected()) {
                return true;
            }
        } else if ("MOBILEDATADSCON".equals(netValue)) {
            if (info.getType() == 0 && !info.isConnected()) {
                return true;
            }
            if (info.getType() != 0 && info.isConnected()) {
                return true;
            }
        } else if (AwareJobSchedulerConstants.NETWORK_STATUS_ALL.equals(netValue) && ((info.getType() == 0 || info.getType() == 1) && info.isConnected())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateTrackedJobs(NetworkInfo info) {
        synchronized (this.mLock) {
            boolean changed = false;
            List<AwareJobStatus> changedJobs = new ArrayList<>();
            int listSize = this.mTrackedJobs.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = this.mTrackedJobs.get(i);
                changed = updateJobSatisfiedLocked(job, info);
                if (changed) {
                    changedJobs.add(job);
                }
            }
            if (changed) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob onControllerStateChanged");
                }
                this.mStateChangedListener.onControllerStateChanged(changedJobs);
            }
        }
    }

    private boolean updateJobSatisfiedLocked(AwareJobStatus job, NetworkInfo info) {
        String netValue = job.getActionFilterValue("NetworkStatus");
        String wifiValue = job.getActionFilterValue("WifiStatus");
        boolean changed = false;
        if (info == null) {
            if ("MOBILEDATADSCON".equals(netValue) && !job.isSatisfied("NetworkStatus")) {
                job.setSatisfied("NetworkStatus", true);
                changed = true;
            }
            if ("WIFIDSCON".equals(wifiValue) && !job.isSatisfied("WifiStatus")) {
                job.setSatisfied("WifiStatus", true);
                changed = true;
            }
        } else {
            if (isNetworkSatisfied(netValue, info) && !job.isSatisfied("NetworkStatus")) {
                job.setSatisfied("NetworkStatus", true);
                changed = true;
            }
            if (isWifiSatisfied(wifiValue, info) && !job.isSatisfied("WifiStatus")) {
                job.setSatisfied("WifiStatus", true);
                changed = true;
            }
        }
        if (this.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("iaware_brjob updateJobSatisfiedLocked, netValue: ");
            sb.append(netValue);
            sb.append(", wifiValue: ");
            sb.append(wifiValue);
            sb.append(", changed: ");
            sb.append(changed);
            sb.append(", info: ");
            sb.append(info == null ? "null" : info);
            AwareLog.i(TAG, sb.toString());
        }
        return changed;
    }
}
