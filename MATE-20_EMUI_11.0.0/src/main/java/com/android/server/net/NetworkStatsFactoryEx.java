package com.android.server.net;

import android.net.NetworkStats;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class NetworkStatsFactoryEx {
    private static final String BPF_STATS_FILE_PATH = "/sys/fs/bpf/map_netd_app_uid_stats_map";
    private static final int EBPF_RROC_UID_INIT_SIZE = 24;
    private static final int HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE = 201;
    private static final int NETWORK_STATS_DEFAULT_INIT_VALUE = -1;
    private static final String TAG = "NetworkStatsFactoryEx";
    private boolean mIsUseBpfStats;
    private IBinder mNetworkManager;
    private final NetworkStats mProcAndUidPersistSnapshot;
    private Set<Integer> mUidsRemoved;

    @VisibleForTesting
    public static native int nativeReadUidAndProcNetworkStatsDetail(NetworkStats networkStats);

    /* access modifiers changed from: private */
    public static class Holder {
        private static final NetworkStatsFactoryEx INSTANCE = new NetworkStatsFactoryEx();

        private Holder() {
        }
    }

    private NetworkStatsFactoryEx() {
        this(new File(BPF_STATS_FILE_PATH).exists());
    }

    private NetworkStatsFactoryEx(boolean isUseBpfStats) {
        this.mUidsRemoved = new HashSet();
        this.mIsUseBpfStats = isUseBpfStats;
        this.mProcAndUidPersistSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), -1);
    }

    public static NetworkStatsFactoryEx create() {
        return Holder.INSTANCE;
    }

    public NetworkStats readPidNetworkStatsDetail(int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        StrictMode.allowThreadDiskReads();
        if (this.mIsUseBpfStats) {
            return readNetworkStatsUidAndProcDetail(limitUid, limitIfaces, limitTag);
        }
        return null;
    }

    private NetworkStats readNetworkStatsUidAndProcDetail(int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        NetworkStats result;
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), (int) EBPF_RROC_UID_INIT_SIZE);
        synchronized (this.mProcAndUidPersistSnapshot) {
            try {
                requestSwapActiveProcessMapLocked();
                if (nativeReadUidAndProcNetworkStatsDetail(stats) == 0) {
                    this.mProcAndUidPersistSnapshot.setElapsedRealtime(stats.getElapsedRealtime());
                    this.mProcAndUidPersistSnapshot.combineAllValues(stats);
                    result = this.mProcAndUidPersistSnapshot.clone();
                    result.filter(limitUid, limitIfaces, limitTag);
                } else {
                    throw new IOException("Failed to read proc and uid iface stats.");
                }
            } catch (RemoteException | ServiceManager.ServiceNotFoundException | ServiceSpecificException e) {
                Slog.w(TAG, "Failed to request swap active locked.");
                throw new IOException("Failed to request swap active locked.");
            } catch (Throwable th) {
                throw th;
            }
        }
        return result;
    }

    private IBinder getNetworkManager() {
        if (this.mNetworkManager == null) {
            this.mNetworkManager = ServiceManager.getService("network_management");
        }
        return this.mNetworkManager;
    }

    @GuardedBy({"mProcAndUidPersistSnapshot"})
    private void requestSwapActiveProcessMapLocked() throws RemoteException, ServiceSpecificException, ServiceManager.ServiceNotFoundException {
        if (this.mIsUseBpfStats) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeString("requestSwap");
                transactParcel(data, reply);
                closeParcel(data, reply);
            } catch (RemoteException e) {
                throw e;
            } catch (Throwable th) {
                closeParcel(data, reply);
                throw th;
            }
        }
    }

    public void removeUids(int[] uids) {
        if (this.mNetworkManager != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                Integer[] args = new Integer[uids.length];
                for (int i = 0; i < uids.length; i++) {
                    args[i] = Integer.valueOf(uids[i]);
                }
                data.writeString("removeUids");
                data.writeArray(args);
                transactParcel(data, reply);
            } catch (RemoteException e) {
                Slog.e(TAG, "Stats data may be dirty caused by removing uids failed.");
            } catch (Throwable th) {
                closeParcel(data, reply);
                throw th;
            }
            closeParcel(data, reply);
        }
    }

    private void transactParcel(Parcel data, Parcel reply) throws RemoteException {
        IBinder networkManager = getNetworkManager();
        if (networkManager == null) {
            Slog.e(TAG, "missing network manager.");
        } else {
            networkManager.transact(HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE, data, reply, 0);
        }
    }

    private void closeParcel(Parcel data, Parcel reply) {
        if (data != null) {
            data.recycle();
        }
        if (reply != null) {
            reply.recycle();
        }
    }

    public void filterUidsRemoved(NetworkStats stats) {
        if (!this.mUidsRemoved.isEmpty()) {
            int size = this.mUidsRemoved.size();
            int[] uids = new int[size];
            Integer[] removedUids = (Integer[]) this.mUidsRemoved.toArray(new Integer[size]);
            for (int i = 0; i < size; i++) {
                Integer uid = removedUids[i];
                if (uid != null) {
                    uids[i] = uid.intValue();
                }
            }
            stats.removeUids(uids);
        }
    }

    public void removeUids(int[] uids, NetworkStats stats) {
        stats.removeUids(uids);
        for (int i : uids) {
            this.mUidsRemoved.add(Integer.valueOf(i));
        }
    }
}
