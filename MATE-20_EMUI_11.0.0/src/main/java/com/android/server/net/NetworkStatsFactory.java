package com.android.server.net;

import android.net.INetd;
import android.net.NetworkStats;
import android.net.util.NetdService;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ProcFileReader;
import com.android.server.NetworkManagementSocketTagger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import libcore.io.IoUtils;

public class NetworkStatsFactory {
    private static final boolean SANITY_CHECK_NATIVE = false;
    private static final String TAG = "NetworkStatsFactory";
    private static final boolean USE_NATIVE_PARSING = true;
    private static final ConcurrentHashMap<String, String> sStackedIfaces = new ConcurrentHashMap<>();
    private INetd mNetdService;
    private NetworkStatsFactoryEx mNetworkStatsFactoryEx;
    @GuardedBy({"mPersistSnapshot"})
    private final NetworkStats mPersistSnapshot;
    private final File mStatsXtIfaceAll;
    private final File mStatsXtIfaceFmt;
    private final File mStatsXtUid;
    private boolean mUseBpfStats;

    @VisibleForTesting
    public static native int nativeReadNetworkStatsDetail(NetworkStats networkStats, String str, int i, String[] strArr, int i2, boolean z);

    @VisibleForTesting
    public static native int nativeReadNetworkStatsDev(NetworkStats networkStats);

    public static void noteStackedIface(String stackedIface, String baseIface) {
        if (stackedIface != null && baseIface != null) {
            sStackedIfaces.put(stackedIface, baseIface);
        }
    }

    public static String[] augmentWithStackedInterfaces(String[] requiredIfaces) {
        if (requiredIfaces == NetworkStats.INTERFACES_ALL) {
            return null;
        }
        HashSet<String> relatedIfaces = new HashSet<>(Arrays.asList(requiredIfaces));
        for (Map.Entry<String, String> entry : sStackedIfaces.entrySet()) {
            if (relatedIfaces.contains(entry.getKey())) {
                relatedIfaces.add(entry.getValue());
            } else if (relatedIfaces.contains(entry.getValue())) {
                relatedIfaces.add(entry.getKey());
            }
        }
        return (String[]) relatedIfaces.toArray(new String[relatedIfaces.size()]);
    }

    public static void apply464xlatAdjustments(NetworkStats baseTraffic, NetworkStats stackedTraffic, boolean useBpfStats) {
        NetworkStats.apply464xlatAdjustments(baseTraffic, stackedTraffic, sStackedIfaces, useBpfStats);
    }

    @VisibleForTesting
    public static void clearStackedIfaces() {
        sStackedIfaces.clear();
    }

    public NetworkStatsFactory() {
        this(new File("/proc/"), new File("/sys/fs/bpf/map_netd_app_uid_stats_map").exists());
    }

    @VisibleForTesting
    public NetworkStatsFactory(File procRoot, boolean useBpfStats) {
        this.mStatsXtIfaceAll = new File(procRoot, "net/xt_qtaguid/iface_stat_all");
        this.mStatsXtIfaceFmt = new File(procRoot, "net/xt_qtaguid/iface_stat_fmt");
        this.mStatsXtUid = new File(procRoot, "net/xt_qtaguid/stats");
        this.mUseBpfStats = useBpfStats;
        this.mPersistSnapshot = new NetworkStats(SystemClock.elapsedRealtime(), -1);
        this.mNetworkStatsFactoryEx = NetworkStatsFactoryEx.create();
    }

    public NetworkStats readBpfNetworkStatsDev() throws IOException {
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        if (nativeReadNetworkStatsDev(stats) == 0) {
            return stats;
        }
        throw new IOException("Failed to parse bpf iface stats");
    }

    public NetworkStats readNetworkStatsSummaryDev() throws IOException {
        if (this.mUseBpfStats) {
            return readBpfNetworkStatsDev();
        }
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        NetworkStats.Entry entry = new NetworkStats.Entry();
        try {
            ProcFileReader reader = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceAll));
            while (reader.hasMoreData()) {
                entry.iface = reader.nextString();
                entry.uid = -1;
                entry.set = -1;
                boolean active = false;
                entry.tag = 0;
                if (reader.nextInt() != 0) {
                    active = true;
                }
                entry.rxBytes = reader.nextLong();
                entry.rxPackets = reader.nextLong();
                entry.txBytes = reader.nextLong();
                entry.txPackets = reader.nextLong();
                if (active) {
                    entry.rxBytes += reader.nextLong();
                    entry.rxPackets += reader.nextLong();
                    entry.txBytes += reader.nextLong();
                    entry.txPackets += reader.nextLong();
                }
                stats.addValues(entry);
                reader.finishLine();
            }
            IoUtils.closeQuietly(reader);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException | NumberFormatException e) {
            throw protocolExceptionWithCause("problem parsing stats", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    public NetworkStats readNetworkStatsSummaryXt() throws IOException {
        if (this.mUseBpfStats) {
            return readBpfNetworkStatsDev();
        }
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (!this.mStatsXtIfaceFmt.exists()) {
            return null;
        }
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        NetworkStats.Entry entry = new NetworkStats.Entry();
        try {
            ProcFileReader reader = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceFmt));
            reader.finishLine();
            while (reader.hasMoreData()) {
                entry.iface = reader.nextString();
                entry.uid = -1;
                entry.set = -1;
                entry.tag = 0;
                entry.rxBytes = reader.nextLong();
                entry.rxPackets = reader.nextLong();
                entry.txBytes = reader.nextLong();
                entry.txPackets = reader.nextLong();
                stats.addValues(entry);
                reader.finishLine();
            }
            IoUtils.closeQuietly(reader);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException | NumberFormatException e) {
            throw protocolExceptionWithCause("problem parsing stats", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    public NetworkStats readNetworkStatsDetail() throws IOException {
        return readNetworkStatsDetail(-1, null, -1, null);
    }

    public NetworkStats readNetworkStatsDetail(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        NetworkStats stats = readNetworkStatsDetailInternal(limitUid, limitIfaces, limitTag, lastStats);
        stats.apply464xlatAdjustments(sStackedIfaces, this.mUseBpfStats);
        return stats;
    }

    @GuardedBy({"mPersistSnapshot"})
    private void requestSwapActiveStatsMapLocked() throws RemoteException {
        if (this.mUseBpfStats) {
            if (this.mNetdService == null) {
                this.mNetdService = NetdService.getInstance();
            }
            this.mNetdService.trafficSwapActiveStatsMap();
        }
    }

    private NetworkStats readNetworkStatsDetailInternal(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        NetworkStats stats;
        NetworkStats result;
        if (lastStats != null) {
            stats = lastStats;
            stats.setElapsedRealtime(SystemClock.elapsedRealtime());
        } else {
            stats = new NetworkStats(SystemClock.elapsedRealtime(), -1);
        }
        if (this.mUseBpfStats) {
            synchronized (this.mPersistSnapshot) {
                try {
                    requestSwapActiveStatsMapLocked();
                    if (nativeReadNetworkStatsDetail(stats, this.mStatsXtUid.getAbsolutePath(), -1, null, -1, this.mUseBpfStats) == 0) {
                        this.mNetworkStatsFactoryEx.filterUidsRemoved(stats);
                        this.mPersistSnapshot.setElapsedRealtime(stats.getElapsedRealtime());
                        this.mPersistSnapshot.combineAllValues(stats);
                        result = this.mPersistSnapshot.clone();
                        result.filter(limitUid, limitIfaces, limitTag);
                    } else {
                        throw new IOException("Failed to parse network stats");
                    }
                } catch (RemoteException e) {
                    throw new IOException(e);
                } catch (Throwable th) {
                    throw th;
                }
            }
            return result;
        } else if (nativeReadNetworkStatsDetail(stats, this.mStatsXtUid.getAbsolutePath(), limitUid, limitIfaces, limitTag, this.mUseBpfStats) == 0) {
            return stats;
        } else {
            Slog.w(TAG, "Failed to parse network stats! stats: " + stats.toString() + "path: " + this.mStatsXtUid.getAbsolutePath());
            throw new IOException("Failed to parse network stats");
        }
    }

    public void removeUids(int[] uids) {
        synchronized (this.mPersistSnapshot) {
            this.mNetworkStatsFactoryEx.removeUids(uids, this.mPersistSnapshot);
        }
    }

    @VisibleForTesting
    public static NetworkStats javaReadNetworkStatsDetail(File detailPath, int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        NetworkStats.Entry entry = new NetworkStats.Entry();
        int lastIdx = 1;
        try {
            ProcFileReader reader = new ProcFileReader(new FileInputStream(detailPath));
            reader.finishLine();
            while (reader.hasMoreData()) {
                int idx = reader.nextInt();
                if (idx == lastIdx + 1) {
                    lastIdx = idx;
                    entry.iface = reader.nextString();
                    entry.tag = NetworkManagementSocketTagger.kernelToTag(reader.nextString());
                    entry.uid = reader.nextInt();
                    entry.set = reader.nextInt();
                    entry.rxBytes = reader.nextLong();
                    entry.rxPackets = reader.nextLong();
                    entry.txBytes = reader.nextLong();
                    entry.txPackets = reader.nextLong();
                    if ((limitIfaces == null || ArrayUtils.contains(limitIfaces, entry.iface)) && ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag))) {
                        stats.addValues(entry);
                    }
                    reader.finishLine();
                } else {
                    throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                }
            }
            IoUtils.closeQuietly(reader);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException | NumberFormatException e) {
            throw protocolExceptionWithCause("problem parsing idx 1", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    public void assertEquals(NetworkStats expected, NetworkStats actual) {
        if (expected.size() == actual.size()) {
            NetworkStats.Entry expectedRow = null;
            NetworkStats.Entry actualRow = null;
            for (int i = 0; i < expected.size(); i++) {
                expectedRow = expected.getValues(i, expectedRow);
                actualRow = actual.getValues(i, actualRow);
                if (!expectedRow.equals(actualRow)) {
                    throw new AssertionError("Expected row " + i + ": " + expectedRow + ", actual row " + actualRow);
                }
            }
            return;
        }
        throw new AssertionError("Expected size " + expected.size() + ", actual size " + actual.size());
    }

    private static ProtocolException protocolExceptionWithCause(String message, Throwable cause) {
        ProtocolException pe = new ProtocolException(message);
        pe.initCause(cause);
        return pe;
    }
}
