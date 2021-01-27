package com.android.server.mtm.iaware.brjob.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.IntentExt;
import com.huawei.android.net.INetworkPolicyListenerEx;
import com.huawei.android.net.NetworkPolicyManagerEx;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ConnectivityController extends AwareStateController {
    private static final String CONDITION_NETWORK = "NetworkStatus";
    private static final String CONDITION_WIFI = "WifiStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String TAG = "ConnectivityController";
    private static ConnectivityController sSingleton;
    private ConnectivityManager mConnManager;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.controller.ConnectivityController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ConnectivityController.this.debug) {
                    AwareLog.i(ConnectivityController.TAG, "iaware_brjob receive connectivity change :" + intent);
                }
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    int type = intent.getIntExtra("networkType", -1);
                    if (ConnectivityController.this.debug) {
                        AwareLog.i(ConnectivityController.TAG, "iaware_brjob connectivity change, type:" + type);
                    }
                    if (type == 0 || type == 1) {
                        NetworkInfo info = null;
                        Object obj = IntentExt.getExtra(intent, "networkInfo");
                        if (obj instanceof NetworkInfo) {
                            info = (NetworkInfo) obj;
                        }
                        if (ConnectivityController.this.debug) {
                            AwareLog.i(ConnectivityController.TAG, "iaware_brjob connectivity change, info:" + info);
                        }
                        ConnectivityController.this.updateTrackedJobs(info);
                    }
                }
            }
        }
    };
    private INetworkPolicyListenerEx mNetPolicyListener = new INetworkPolicyListenerEx() {
        /* class com.android.server.mtm.iaware.brjob.controller.ConnectivityController.AnonymousClass2 */

        public void onUidRulesChanged(int uid, int uidRules) {
            if (ConnectivityController.this.debug) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onUidRulesChanged, do nothing");
            }
        }

        public void onMeteredIfacesChanged(String[] meteredIfaces) {
            if (ConnectivityController.this.debug) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onMeteredIfacesChanged");
            }
            NetworkInfo info = ConnectivityController.this.mConnManager != null ? ConnectivityController.this.mConnManager.getActiveNetworkInfo() : null;
            if (ConnectivityController.this.debug) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onMeteredIfacesChanged info: " + info);
            }
            ConnectivityController.this.updateTrackedJobs(info);
        }

        public void onRestrictBackgroundChanged(boolean restrictBackground) {
            if (ConnectivityController.this.debug) {
                AwareLog.d(ConnectivityController.TAG, "iaware_brjob onRestrictBackgroundChanged");
            }
            NetworkInfo info = ConnectivityController.this.mConnManager != null ? ConnectivityController.this.mConnManager.getActiveNetworkInfo() : null;
            if (ConnectivityController.this.debug) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onRestrictBackgroundChanged info: " + info);
            }
            ConnectivityController.this.updateTrackedJobs(info);
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) {
            if (ConnectivityController.this.debug) {
                AwareLog.i(ConnectivityController.TAG, "iaware_brjob onUidPoliciesChanged, do nothing ");
            }
        }
    };
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private ConnectivityController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (context != null) {
            this.mConnManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
            ContextEx.registerReceiverAsUser(this.mContext, this.mConnectivityReceiver, UserHandleEx.SYSTEM, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), (String) null, (Handler) null);
            NetworkPolicyManagerEx.registerListener(this.mContext, this.mNetPolicyListener);
        }
    }

    public static ConnectivityController get(AwareJobSchedulerService jms) {
        ConnectivityController connectivityController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new ConnectivityController(jms, jms.getContext(), jms.getLock());
            }
            connectivityController = sSingleton;
        }
        return connectivityController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("NetworkStatus") || job.hasConstraint("WifiStatus")) {
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob, start tracking.");
                }
                NetworkInfo info = getNetworkInfo();
                if (job.hasConstraint("NetworkStatus")) {
                    String netValue = job.getActionFilterValue("NetworkStatus");
                    if (this.debug) {
                        AwareLog.i(TAG, "iaware_brjob, netValue: " + netValue);
                    }
                    job.setSatisfied("NetworkStatus", isNetworkSatisfied(netValue, info));
                }
                if (job.hasConstraint("WifiStatus")) {
                    String wifiValue = job.getActionFilterValue("WifiStatus");
                    if (this.debug) {
                        AwareLog.i(TAG, "iaware_brjob, wifiValue: " + wifiValue);
                    }
                    job.setSatisfied("WifiStatus", isWifiSatisfied(wifiValue, info));
                }
                addJobLocked(this.mTrackedJobs, job);
            }
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("NetworkStatus") || job.hasConstraint("WifiStatus")) {
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob stop tracking begin");
                }
                this.mTrackedJobs.remove(job);
            }
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        Object obj;
        if (pw != null) {
            pw.println("    ConnectivityController tracked job num: " + this.mTrackedJobs.size());
            StringBuilder sb = new StringBuilder();
            sb.append("        now network info is ");
            ConnectivityManager connectivityManager = this.mConnManager;
            if (connectivityManager == null) {
                obj = "[connMngr = null]";
            } else {
                obj = connectivityManager.getActiveNetworkInfo();
            }
            sb.append(obj);
            pw.println(sb.toString());
        }
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager connectivityManager = this.mConnManager;
        NetworkInfo info = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob getNetworkInfo: " + info);
        }
        if (info == null) {
            AwareLog.w(TAG, "iaware_brjob network info is null.");
        }
        return info;
    }

    private boolean isWifiSatisfied(String wifiValue, NetworkInfo info) {
        if (info == null) {
            return "WIFIDSCON".equals(wifiValue);
        }
        if (wifiValue == null) {
            return false;
        }
        char c = 65535;
        int hashCode = wifiValue.hashCode();
        if (hashCode != -102597506) {
            if (hashCode == 2060225069 && wifiValue.equals("WIFICON")) {
                c = 0;
            }
        } else if (wifiValue.equals("WIFIDSCON")) {
            c = 1;
        }
        if (c != 0) {
            if (c == 1) {
                if (info.getType() == 1 && !info.isConnected()) {
                    return true;
                }
                if (info.getType() != 1 && info.isConnected()) {
                    return true;
                }
            }
        } else if (info.getType() == 1 && info.isConnected()) {
            return true;
        }
        return false;
    }

    private boolean isNetworkSatisfied(String netValue, NetworkInfo info) {
        if (info == null) {
            return "MOBILEDATADSCON".equals(netValue);
        }
        if (netValue == null) {
            return false;
        }
        char c = 65535;
        int hashCode = netValue.hashCode();
        if (hashCode != -1376426346) {
            if (hashCode != 64897) {
                if (hashCode == 105243303 && netValue.equals("MOBILEDATADSCON")) {
                    c = 1;
                }
            } else if (netValue.equals(AwareJobSchedulerConstants.NETWORK_STATUS_ALL)) {
                c = 2;
            }
        } else if (netValue.equals("MOBILEDATACON")) {
            c = 0;
        }
        if (c != 0) {
            if (c != 1) {
                if (c == 2 && ((info.getType() == 0 || info.getType() == 1) && info.isConnected())) {
                    return true;
                }
            } else if (info.getType() == 0 && !info.isConnected()) {
                return true;
            } else {
                if (info.getType() != 0 && info.isConnected()) {
                    return true;
                }
            }
        } else if (info.getType() == 0 && info.isConnected()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedJobs(NetworkInfo info) {
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
                if (this.debug) {
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
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob updateJobSatisfiedLocked, netValue: " + netValue + ", wifiValue: " + wifiValue + ", changed: " + changed + ", info: " + info);
        }
        return changed;
    }
}
