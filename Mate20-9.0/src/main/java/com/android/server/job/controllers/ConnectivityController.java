package com.android.server.job.controllers;

import android.net.ConnectivityManager;
import android.net.INetworkPolicyListener;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkPolicyManager;
import android.net.NetworkRequest;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import java.util.Objects;
import java.util.function.Predicate;

public final class ConnectivityController extends StateController implements ConnectivityManager.OnNetworkActiveListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.Connectivity";
    private final ConnectivityManager mConnManager = ((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class));
    private final INetworkPolicyListener mNetPolicyListener = new NetworkPolicyManager.Listener() {
        public void onUidRulesChanged(int uid, int uidRules) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onUidRulesChanged: " + uid);
            }
            ConnectivityController.this.updateTrackedJobs(uid, null);
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "Uid policy changed for " + uid);
            }
            ConnectivityController.this.updateTrackedJobs(uid, null);
        }
    };
    private final NetworkPolicyManager mNetPolicyManager = ((NetworkPolicyManager) this.mContext.getSystemService(NetworkPolicyManager.class));
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onCapabilitiesChanged: " + network);
            }
            ConnectivityController.this.updateTrackedJobs(-1, network);
        }

        public void onLost(Network network) {
            if (ConnectivityController.DEBUG) {
                Slog.v(ConnectivityController.TAG, "onLost: " + network);
            }
            ConnectivityController.this.updateTrackedJobs(-1, network);
        }
    };
    @GuardedBy("mLock")
    private final ArraySet<JobStatus> mTrackedJobs = new ArraySet<>();

    public ConnectivityController(JobSchedulerService service) {
        super(service);
        this.mConnManager.registerNetworkCallback(new NetworkRequest.Builder().clearCapabilities().build(), this.mNetworkCallback);
        this.mNetPolicyManager.registerListener(this.mNetPolicyListener);
    }

    @GuardedBy("mLock")
    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (jobStatus.hasConnectivityConstraint()) {
            updateConstraintsSatisfied(jobStatus);
            this.mTrackedJobs.add(jobStatus);
            jobStatus.setTrackingController(2);
        }
    }

    @GuardedBy("mLock")
    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if (jobStatus.clearTrackingController(2)) {
            this.mTrackedJobs.remove(jobStatus);
        }
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
        boolean z = false;
        if (capabilities.hasCapability(20)) {
            return false;
        }
        if (jobStatus.getFractionRunTime() < constants.CONN_CONGESTION_DELAY_FRAC) {
            z = true;
        }
        return z;
    }

    private static boolean isStrictSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        return jobStatus.getJob().getRequiredNetwork().networkCapabilities.satisfiedByNetworkCapabilities(capabilities);
    }

    private static boolean isRelaxedSatisfied(JobStatus jobStatus, Network network, NetworkCapabilities capabilities, JobSchedulerService.Constants constants) {
        boolean z = false;
        if (!jobStatus.getJob().isPrefetch() || !new NetworkCapabilities(jobStatus.getJob().getRequiredNetwork().networkCapabilities).removeCapability(11).satisfiedByNetworkCapabilities(capabilities)) {
            return false;
        }
        if (jobStatus.getFractionRunTime() > constants.CONN_PREFETCH_RELAX_FRAC) {
            z = true;
        }
        return z;
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
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0036  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x007d A[SYNTHETIC] */
    public void updateTrackedJobs(int filterUid, Network filterNetwork) {
        boolean uidMatch;
        NetworkCapabilities capabilities;
        NetworkCapabilities capabilities2;
        int i = filterUid;
        Network network = filterNetwork;
        synchronized (this.mLock) {
            SparseArray<Network> uidToNetwork = new SparseArray<>();
            SparseArray<NetworkCapabilities> networkToCapabilities = new SparseArray<>();
            boolean changed = false;
            boolean z = true;
            int i2 = this.mTrackedJobs.size() - 1;
            while (i2 >= 0) {
                JobStatus js = this.mTrackedJobs.valueAt(i2);
                int uid = js.getSourceUid();
                boolean networkMatch = false;
                int netId = -1;
                if (i != -1) {
                    if (i != uid) {
                        uidMatch = false;
                        if (!uidMatch) {
                            Network network2 = uidToNetwork.get(uid);
                            if (network2 == null) {
                                network2 = this.mConnManager.getActiveNetworkForUid(uid);
                                uidToNetwork.put(uid, network2);
                            }
                            if (network != null) {
                                if (!Objects.equals(network, network2)) {
                                    boolean forceUpdate = Objects.equals(js.network, network2) ^ z;
                                    if (!networkMatch || forceUpdate) {
                                        if (network2 != null) {
                                            netId = network2.netId;
                                        }
                                        capabilities = networkToCapabilities.get(netId);
                                        if (capabilities != null) {
                                            capabilities2 = this.mConnManager.getNetworkCapabilities(network2);
                                            networkToCapabilities.put(netId, capabilities2);
                                        } else {
                                            capabilities2 = capabilities;
                                        }
                                        changed |= updateConstraintsSatisfied(js, network2, capabilities2);
                                    }
                                }
                            }
                            networkMatch = z;
                            boolean forceUpdate2 = Objects.equals(js.network, network2) ^ z;
                            if (!networkMatch) {
                            }
                            if (network2 != null) {
                            }
                            capabilities = networkToCapabilities.get(netId);
                            if (capabilities != null) {
                            }
                            changed |= updateConstraintsSatisfied(js, network2, capabilities2);
                        }
                        i2--;
                        z = true;
                    }
                }
                uidMatch = z;
                if (!uidMatch) {
                }
                i2--;
                z = true;
            }
            if (changed) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }

    public void onNetworkActive() {
        synchronized (this.mLock) {
            for (int i = this.mTrackedJobs.size() - 1; i >= 0; i--) {
                JobStatus js = this.mTrackedJobs.valueAt(i);
                if (js.isReady()) {
                    if (DEBUG) {
                        Slog.d(TAG, "Running " + js + " due to network activity.");
                    }
                    this.mStateChangedListener.onRunJobNow(js);
                }
            }
        }
    }

    @GuardedBy("mLock")
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        for (int i = 0; i < this.mTrackedJobs.size(); i++) {
            JobStatus js = this.mTrackedJobs.valueAt(i);
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

    @GuardedBy("mLock")
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        long mToken = protoOutputStream.start(1146756268035L);
        for (int i = 0; i < this.mTrackedJobs.size(); i++) {
            JobStatus js = this.mTrackedJobs.valueAt(i);
            if (predicate.test(js)) {
                long jsToken = protoOutputStream.start(2246267895810L);
                js.writeToShortProto(protoOutputStream, 1146756268033L);
                protoOutputStream.write(1120986464258L, js.getSourceUid());
                NetworkRequest rn = js.getJob().getRequiredNetwork();
                if (rn != null) {
                    rn.writeToProto(protoOutputStream, 1146756268035L);
                }
                protoOutputStream.end(jsToken);
            }
        }
        Predicate<JobStatus> predicate2 = predicate;
        protoOutputStream.end(mToken);
        protoOutputStream.end(token);
    }
}
