package com.android.server.job.controllers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.os.UserHandle;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;

public final class ConnectivityController extends StateController implements OnNetworkActiveListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "JobScheduler.Conn";
    private static ConnectivityController mSingleton;
    private static Object sCreationLock = new Object();
    private final ConnectivityManager mConnManager = ((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class));
    private boolean mConnected = false;
    private final INetworkPolicyListener mNetPolicyListener = new Stub() {
        public void onUidRulesChanged(int uid, int uidRules) {
            ConnectivityController.this.updateTrackedJobs(uid, null);
        }

        public void onMeteredIfacesChanged(String[] meteredIfaces) {
        }

        public void onRestrictBackgroundChanged(boolean restrictBackground) {
            ConnectivityController.this.updateTrackedJobs(-1, null);
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) {
            ConnectivityController.this.updateTrackedJobs(uid, null);
        }
    };
    private final NetworkPolicyManager mNetPolicyManager = ((NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class));
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            ConnectivityController.this.updateTrackedJobs(-1, networkCapabilities);
        }

        public void onLost(Network network) {
            ConnectivityController.this.updateTrackedJobs(-1, null);
        }
    };
    @GuardedBy("mLock")
    private final ArraySet<JobStatus> mTrackedJobs = new ArraySet();
    private boolean mValidated = false;

    public static ConnectivityController get(JobSchedulerService jms) {
        ConnectivityController connectivityController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new ConnectivityController(jms, jms.getContext(), jms.getLock());
            }
            connectivityController = mSingleton;
        }
        return connectivityController;
    }

    private ConnectivityController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mConnManager.registerDefaultNetworkCallback(this.mNetworkCallback);
        this.mNetPolicyManager.registerListener(this.mNetPolicyListener);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (jobStatus.hasConnectivityConstraint()) {
            updateConstraintsSatisfied(jobStatus, null);
            this.mTrackedJobs.add(jobStatus);
            jobStatus.setTrackingController(2);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if (jobStatus.clearTrackingController(2)) {
            this.mTrackedJobs.remove(jobStatus);
        }
    }

    private boolean updateConstraintsSatisfied(JobStatus jobStatus, NetworkCapabilities capabilities) {
        boolean validated;
        int jobUid = jobStatus.getSourceUid();
        boolean ignoreBlocked = (jobStatus.getFlags() & 1) != 0;
        NetworkInfo info = this.mConnManager.getActiveNetworkInfoForUid(jobUid, ignoreBlocked);
        if (capabilities == null) {
            capabilities = this.mConnManager.getNetworkCapabilities(this.mConnManager.getActiveNetworkForUid(jobUid, ignoreBlocked));
        }
        if (capabilities != null) {
            validated = capabilities.hasCapability(16);
        } else {
            validated = false;
        }
        boolean connected = info != null ? info.isConnected() : false;
        boolean changed = ((jobStatus.setConnectivityConstraintSatisfied(connected ? validated : false) | jobStatus.setMeteredConstraintSatisfied(connected ? info.isMetered() : false)) | jobStatus.setUnmeteredConstraintSatisfied(connected ? info.isMetered() ^ 1 : false)) | jobStatus.setNotRoamingConstraintSatisfied(connected ? info.isRoaming() ^ 1 : false);
        if (jobUid == 1000) {
            this.mConnected = connected;
            this.mValidated = validated;
        }
        return changed;
    }

    private void updateTrackedJobs(int uid, NetworkCapabilities capabilities) {
        synchronized (this.mLock) {
            int changed = 0;
            for (int i = this.mTrackedJobs.size() - 1; i >= 0; i--) {
                JobStatus js = (JobStatus) this.mTrackedJobs.valueAt(i);
                if (uid == -1 || uid == js.getSourceUid()) {
                    changed |= updateConstraintsSatisfied(js, capabilities);
                }
            }
            if (changed != 0) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }

    public void onNetworkActive() {
        synchronized (this.mLock) {
            for (int i = this.mTrackedJobs.size() - 1; i >= 0; i--) {
                JobStatus js = (JobStatus) this.mTrackedJobs.valueAt(i);
                if (js.isReady()) {
                    this.mStateChangedListener.onRunJobNow(js);
                }
            }
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Connectivity: connected=");
        pw.print(this.mConnected);
        pw.print(" validated=");
        pw.println(this.mValidated);
        pw.print("Tracking ");
        pw.print(this.mTrackedJobs.size());
        pw.println(":");
        for (int i = 0; i < this.mTrackedJobs.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedJobs.valueAt(i);
            if (js.shouldDump(filterUid)) {
                pw.print("  #");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.print(": C=");
                pw.print(js.needsAnyConnectivity());
                pw.print(": M=");
                pw.print(js.needsMeteredConnectivity());
                pw.print(": UM=");
                pw.print(js.needsUnmeteredConnectivity());
                pw.print(": NR=");
                pw.println(js.needsNonRoamingConnectivity());
            }
        }
    }
}
