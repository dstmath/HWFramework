package com.android.server.job.controllers;

import android.net.ConnectivityManager;
import android.net.INetworkPolicyListener;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.net.NetworkPolicyManagerInternal;
import java.util.Objects;
import java.util.function.Predicate;

public final class ConnectivityController extends StateController implements ConnectivityManager.OnNetworkActiveListener {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int MSG_DATA_SAVER_TOGGLED = 0;
    private static final int MSG_UID_RULES_CHANGES = 1;
    private static final String TAG = "JobScheduler.Connectivity";
    @GuardedBy({"mLock"})
    private final ArraySet<Network> mAvailableNetworks = new ArraySet<>();
    private final ConnectivityManager mConnManager = ((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class));
    private final Handler mHandler = new CcHandler(this.mContext.getMainLooper());
    private final INetworkPolicyListener mNetPolicyListener = new NetworkPolicyManager.Listener() {
        /* class com.android.server.job.controllers.ConnectivityController.AnonymousClass2 */

        public void onRestrictBackgroundChanged(boolean restrictBackground) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onRestrictBackgroundChanged: " + restrictBackground);
            }
            ConnectivityController.this.mHandler.obtainMessage(0).sendToTarget();
        }

        public void onUidRulesChanged(int uid, int uidRules) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onUidRulesChanged: " + uid);
            }
            ConnectivityController.this.mHandler.obtainMessage(1, uid, 0).sendToTarget();
        }
    };
    private final NetworkPolicyManager mNetPolicyManager = ((NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class));
    private final NetworkPolicyManagerInternal mNetPolicyManagerInternal = ((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class));
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        /* class com.android.server.job.controllers.ConnectivityController.AnonymousClass1 */

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onAvailable: " + network);
            }
            synchronized (ConnectivityController.this.mLock) {
                ConnectivityController.this.mAvailableNetworks.add(network);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onCapabilitiesChanged: " + network);
            }
            ConnectivityController.this.updateTrackedJobs(-1, network);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onLost: " + network);
            }
            synchronized (ConnectivityController.this.mLock) {
                ConnectivityController.this.mAvailableNetworks.remove(network);
            }
            ConnectivityController.this.updateTrackedJobs(-1, network);
        }
    };
    @GuardedBy({"mLock"})
    private final SparseArray<ArraySet<JobStatus>> mRequestedWhitelistJobs = new SparseArray<>();
    @GuardedBy({"mLock"})
    private final SparseArray<ArraySet<JobStatus>> mTrackedJobs = new SparseArray<>();

    public ConnectivityController(JobSchedulerService service) {
        super(service);
        this.mConnManager.registerNetworkCallback(new NetworkRequest.Builder().clearCapabilities().build(), this.mNetworkCallback);
        this.mNetPolicyManager.registerListener(this.mNetPolicyListener);
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (jobStatus.hasConnectivityConstraint()) {
            updateConstraintsSatisfied(jobStatus);
            ArraySet<JobStatus> jobs = this.mTrackedJobs.get(jobStatus.getSourceUid());
            if (jobs == null) {
                jobs = new ArraySet<>();
                this.mTrackedJobs.put(jobStatus.getSourceUid(), jobs);
            }
            jobs.add(jobStatus);
            jobStatus.setTrackingController(2);
        }
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if (jobStatus.clearTrackingController(2)) {
            ArraySet<JobStatus> jobs = this.mTrackedJobs.get(jobStatus.getSourceUid());
            if (jobs != null) {
                jobs.remove(jobStatus);
            }
            maybeRevokeStandbyExceptionLocked(jobStatus);
        }
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void onConstantsUpdatedLocked() {
        if (this.mConstants.USE_HEARTBEATS) {
            if (DEBUG) {
                Slog.i(TAG, "Revoking all standby exceptions");
            }
            for (int i = 0; i < this.mRequestedWhitelistJobs.size(); i++) {
                this.mNetPolicyManagerInternal.setAppIdleWhitelist(this.mRequestedWhitelistJobs.keyAt(i), false);
            }
            this.mRequestedWhitelistJobs.clear();
        }
    }

    public boolean isNetworkAvailable(JobStatus job) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mAvailableNetworks.size(); i++) {
                Network network = this.mAvailableNetworks.valueAt(i);
                NetworkCapabilities capabilities = this.mConnManager.getNetworkCapabilities(network);
                boolean satisfied = isSatisfied(job, network, capabilities, this.mConstants);
                if (DEBUG) {
                    Slog.v(TAG, "isNetworkAvailable(" + job + ") with network " + network + " and capabilities " + capabilities + ". Satisfied=" + satisfied);
                }
                if (satisfied) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void requestStandbyExceptionLocked(JobStatus job) {
        int uid = job.getSourceUid();
        boolean isExceptionRequested = isStandbyExceptionRequestedLocked(uid);
        ArraySet<JobStatus> jobs = this.mRequestedWhitelistJobs.get(uid);
        if (jobs == null) {
            jobs = new ArraySet<>();
            this.mRequestedWhitelistJobs.put(uid, jobs);
        }
        if (jobs.add(job) && !isExceptionRequested) {
            if (DEBUG) {
                Slog.i(TAG, "Requesting standby exception for UID: " + uid);
            }
            this.mNetPolicyManagerInternal.setAppIdleWhitelist(uid, true);
        } else if (DEBUG) {
            Slog.i(TAG, "requestStandbyExceptionLocked found exception already requested.");
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public boolean isStandbyExceptionRequestedLocked(int uid) {
        ArraySet jobs = this.mRequestedWhitelistJobs.get(uid);
        return jobs != null && jobs.size() > 0;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public boolean wouldBeReadyWithConnectivityLocked(JobStatus jobStatus) {
        boolean networkAvailable = isNetworkAvailable(jobStatus);
        if (DEBUG) {
            Slog.v(TAG, "wouldBeReadyWithConnectivityLocked: " + jobStatus.toShortString() + " networkAvailable=" + networkAvailable);
        }
        return networkAvailable && wouldBeReadyWithConstraintLocked(jobStatus, 268435456);
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void evaluateStateLocked(JobStatus jobStatus) {
        if (this.mConstants.USE_HEARTBEATS || !jobStatus.hasConnectivityConstraint()) {
            return;
        }
        if (wouldBeReadyWithConnectivityLocked(jobStatus)) {
            if (DEBUG) {
                Slog.i(TAG, "evaluateStateLocked finds job " + jobStatus + " would be ready.");
            }
            requestStandbyExceptionLocked(jobStatus);
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "evaluateStateLocked finds job " + jobStatus + " would not be ready.");
        }
        maybeRevokeStandbyExceptionLocked(jobStatus);
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void reevaluateStateLocked(int uid) {
        ArraySet<JobStatus> jobs;
        if (!this.mConstants.USE_HEARTBEATS && (jobs = this.mTrackedJobs.get(uid)) != null) {
            for (int i = jobs.size() - 1; i >= 0; i--) {
                evaluateStateLocked(jobs.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void maybeRevokeStandbyExceptionLocked(JobStatus job) {
        int uid = job.getSourceUid();
        if (isStandbyExceptionRequestedLocked(uid)) {
            ArraySet<JobStatus> jobs = this.mRequestedWhitelistJobs.get(uid);
            if (jobs == null) {
                Slog.wtf(TAG, "maybeRevokeStandbyExceptionLocked found null jobs array even though a standby exception has been requested.");
            } else if (jobs.remove(job) && jobs.size() <= 0) {
                revokeStandbyExceptionLocked(uid);
            } else if (DEBUG) {
                Slog.i(TAG, "maybeRevokeStandbyExceptionLocked not revoking because there are still " + jobs.size() + " jobs left.");
            }
        }
    }

    @GuardedBy({"mLock"})
    private void revokeStandbyExceptionLocked(int uid) {
        if (DEBUG) {
            Slog.i(TAG, "Revoking standby exception for UID: " + uid);
        }
        this.mNetPolicyManagerInternal.setAppIdleWhitelist(uid, false);
        this.mRequestedWhitelistJobs.remove(uid);
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void onAppRemovedLocked(String pkgName, int uid) {
        this.mTrackedJobs.delete(uid);
    }

    private static boolean isInsane(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        long estimatedBytes = jobStatus.getEstimatedNetworkBytes();
        if (estimatedBytes == -1) {
            return false;
        }
        long slowest = (long) NetworkCapabilities.minBandwidth(capabilities.getLinkDownstreamBandwidthKbps(), capabilities.getLinkUpstreamBandwidthKbps());
        if (slowest == 0) {
            return false;
        }
        long estimatedMillis = (1000 * estimatedBytes) / ((1024 * slowest) / 8);
        if (estimatedMillis <= 600000) {
            return false;
        }
        Slog.w(TAG, "Estimated " + estimatedBytes + " bytes over " + slowest + " kbps network would take " + estimatedMillis + "ms; that's insane!");
        return true;
    }

    private static boolean isCongestionDelayed(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        if (capabilities.hasCapability(20) || jobStatus.getFractionRunTime() >= constants.CONN_CONGESTION_DELAY_FRAC) {
            return false;
        }
        return true;
    }

    private static boolean isStrictSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        return jobStatus.getJob().getRequiredNetwork().networkCapabilities.satisfiedByNetworkCapabilities(capabilities);
    }

    private static boolean isRelaxedSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        if (jobStatus.getJob().isPrefetch() && new NetworkCapabilities(jobStatus.getJob().getRequiredNetwork().networkCapabilities).removeCapability(11).satisfiedByNetworkCapabilities(capabilities) && jobStatus.getFractionRunTime() > constants.CONN_PREFETCH_RELAX_FRAC) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    static boolean isSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        if (network == null || capabilities == null || isInsane(jobStatus, network, capabilities, constants) || isCongestionDelayed(jobStatus, network, capabilities, constants)) {
            return false;
        }
        if (!isStrictSatisfied(jobStatus, network, capabilities, constants) && !isRelaxedSatisfied(jobStatus, network, capabilities, constants)) {
            return false;
        }
        return true;
    }

    private boolean updateConstraintsSatisfied(JobStatus jobStatus) {
        Network network = this.mConnManager.getActiveNetworkForUid(jobStatus.getSourceUid());
        return updateConstraintsSatisfied(jobStatus, network, this.mConnManager.getNetworkCapabilities(network));
    }

    private boolean updateConstraintsSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities) {
        boolean z = true;
        NetworkInfo info = this.mConnManager.getNetworkInfoForUid(network, jobStatus.getSourceUid(), (jobStatus.getFlags() & 1) != 0);
        boolean connected = info != null && info.isConnected();
        boolean satisfied = isSatisfied(jobStatus, network, capabilities, this.mConstants);
        if (!connected || !satisfied) {
            z = false;
        }
        boolean changed = jobStatus.setConnectivityConstraintSatisfied(z);
        jobStatus.network = network;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Connectivity ");
            sb.append(changed ? "CHANGED" : "unchanged");
            sb.append(" for ");
            sb.append(jobStatus);
            sb.append(": connected=");
            sb.append(connected);
            sb.append(" satisfied=");
            sb.append(satisfied);
            Slog.i(TAG, sb.toString());
        }
        return changed;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedJobs(int filterUid, Network filterNetwork) {
        synchronized (this.mLock) {
            SparseArray<NetworkCapabilities> networkToCapabilities = new SparseArray<>();
            boolean changed = false;
            if (filterUid == -1) {
                for (int i = this.mTrackedJobs.size() - 1; i >= 0; i--) {
                    changed |= updateTrackedJobsLocked(this.mTrackedJobs.valueAt(i), filterNetwork, networkToCapabilities);
                }
            } else {
                changed = updateTrackedJobsLocked(this.mTrackedJobs.get(filterUid), filterNetwork, networkToCapabilities);
            }
            if (changed) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }

    private boolean updateTrackedJobsLocked(ArraySet<JobStatus> jobs, Network filterNetwork, SparseArray<NetworkCapabilities> networkToCapabilities) {
        boolean networkMatch = false;
        if (jobs == null || jobs.size() == 0) {
            return false;
        }
        Network network = this.mConnManager.getActiveNetworkForUid(jobs.valueAt(0).getSourceUid());
        int netId = network != null ? network.netId : -1;
        NetworkCapabilities capabilities = networkToCapabilities.get(netId);
        if (capabilities == null) {
            capabilities = this.mConnManager.getNetworkCapabilities(network);
            networkToCapabilities.put(netId, capabilities);
        }
        if (filterNetwork == null || Objects.equals(filterNetwork, network)) {
            networkMatch = true;
        }
        boolean changed = false;
        for (int i = jobs.size() - 1; i >= 0; i--) {
            JobStatus js = jobs.valueAt(i);
            if (networkMatch || !Objects.equals(js.network, network)) {
                changed |= updateConstraintsSatisfied(js, network, capabilities);
            }
        }
        return changed;
    }

    @Override // android.net.ConnectivityManager.OnNetworkActiveListener
    public void onNetworkActive() {
        synchronized (this.mLock) {
            for (int i = this.mTrackedJobs.size() - 1; i >= 0; i--) {
                ArraySet<JobStatus> jobs = this.mTrackedJobs.valueAt(i);
                for (int j = jobs.size() - 1; j >= 0; j--) {
                    JobStatus js = jobs.valueAt(j);
                    if (js.isReady()) {
                        if (DEBUG) {
                            Slog.d(TAG, "Running " + js + " due to network activity.");
                        }
                        this.mStateChangedListener.onRunJobNow(js);
                    }
                }
            }
        }
    }

    private class CcHandler extends Handler {
        CcHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (ConnectivityController.this.mLock) {
                int i = msg.what;
                if (i == 0) {
                    ConnectivityController.this.updateTrackedJobs(-1, null);
                } else if (i == 1) {
                    ConnectivityController.this.updateTrackedJobs(msg.arg1, null);
                }
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        if (this.mRequestedWhitelistJobs.size() > 0) {
            pw.print("Requested standby exceptions:");
            for (int i = 0; i < this.mRequestedWhitelistJobs.size(); i++) {
                pw.print(" ");
                pw.print(this.mRequestedWhitelistJobs.keyAt(i));
                pw.print(" (");
                pw.print(this.mRequestedWhitelistJobs.valueAt(i).size());
                pw.print(" jobs)");
            }
            pw.println();
        }
        if (this.mAvailableNetworks.size() > 0) {
            pw.println("Available networks:");
            pw.increaseIndent();
            for (int i2 = 0; i2 < this.mAvailableNetworks.size(); i2++) {
                pw.println(this.mAvailableNetworks.valueAt(i2));
            }
            pw.decreaseIndent();
        } else {
            pw.println("No available networks");
        }
        for (int i3 = 0; i3 < this.mTrackedJobs.size(); i3++) {
            ArraySet<JobStatus> jobs = this.mTrackedJobs.valueAt(i3);
            for (int j = 0; j < jobs.size(); j++) {
                JobStatus js = jobs.valueAt(j);
                if (predicate.test(js)) {
                    pw.print("#");
                    js.printUniqueId(pw);
                    pw.print(" from ");
                    UserHandle.formatUid(pw, js.getSourceUid());
                    pw.print(": ");
                    pw.print(js.getJob().getRequiredNetwork());
                    pw.println();
                }
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    @GuardedBy({"mLock"})
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long j;
        long token = proto.start(fieldId);
        long j2 = 1146756268035L;
        long mToken = proto.start(1146756268035L);
        for (int i = 0; i < this.mTrackedJobs.size(); i++) {
            ArraySet<JobStatus> jobs = this.mTrackedJobs.valueAt(i);
            int j3 = 0;
            while (j3 < jobs.size()) {
                JobStatus js = jobs.valueAt(j3);
                if (!predicate.test(js)) {
                    j = j2;
                } else {
                    long jsToken = proto.start(2246267895810L);
                    js.writeToShortProto(proto, 1146756268033L);
                    proto.write(1120986464258L, js.getSourceUid());
                    NetworkRequest rn = js.getJob().getRequiredNetwork();
                    if (rn != null) {
                        j = 1146756268035L;
                        rn.writeToProto(proto, 1146756268035L);
                    } else {
                        j = 1146756268035L;
                    }
                    proto.end(jsToken);
                }
                j3++;
                j2 = j;
            }
        }
        proto.end(mToken);
        proto.end(token);
    }
}
