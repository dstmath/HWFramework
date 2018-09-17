package com.android.internal.net;

import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ProcFileReader;
import com.android.server.NetworkManagementSocketTagger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.util.Objects;
import libcore.io.IoUtils;

public class NetworkStatsFactory {
    private static final String CLATD_INTERFACE_PREFIX = "v4-";
    private static final int IPV4V6_HEADER_DELTA = 20;
    private static final boolean SANITY_CHECK_NATIVE = false;
    private static final String TAG = "NetworkStatsFactory";
    private static final boolean USE_NATIVE_PARSING = true;
    @GuardedBy("sStackedIfaces")
    private static final ArrayMap<String, String> sStackedIfaces = new ArrayMap();
    private final File mStatsXtIfaceAll;
    private final File mStatsXtIfaceFmt;
    private final File mStatsXtProcAndUid;
    private final File mStatsXtProcUid;
    private final File mStatsXtUid;

    public static native int nativeReadNetworkStatsDetail(NetworkStats networkStats, String str, int i, String[] strArr, int i2);

    public static void noteStackedIface(String stackedIface, String baseIface) {
        synchronized (sStackedIfaces) {
            if (baseIface != null) {
                sStackedIfaces.put(stackedIface, baseIface);
            } else {
                sStackedIfaces.remove(stackedIface);
            }
        }
    }

    public NetworkStatsFactory() {
        this(new File("/proc/"));
    }

    public NetworkStatsFactory(File procRoot) {
        this.mStatsXtIfaceAll = new File(procRoot, "net/xt_qtaguid/iface_stat_all");
        this.mStatsXtIfaceFmt = new File(procRoot, "net/xt_qtaguid/iface_stat_fmt");
        this.mStatsXtUid = new File(procRoot, "net/xt_qtaguid/stats");
        this.mStatsXtProcUid = new File(procRoot, "net/comm/stats");
        this.mStatsXtProcAndUid = new File(procRoot, "net/xt_qtaguid/stats_pid");
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a0 A:{Splitter: B:1:0x0014, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0081 A:{Splitter: B:3:0x0020, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Missing block: B:14:0x0081, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:15:0x0082, code:
            r3 = r4;
     */
    /* JADX WARNING: Missing block: B:18:0x008b, code:
            throw new java.net.ProtocolException("problem parsing stats", r1);
     */
    /* JADX WARNING: Missing block: B:19:0x008c, code:
            r7 = th;
     */
    /* JADX WARNING: Missing block: B:27:0x00a0, code:
            r1 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NetworkStats readNetworkStatsSummaryDev() throws IOException {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable reader = null;
        try {
            ProcFileReader reader2 = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceAll));
            while (reader2.hasMoreData()) {
                try {
                    entry.iface = reader2.nextString();
                    entry.uid = -1;
                    entry.set = -1;
                    entry.tag = 0;
                    boolean active = reader2.nextInt() != 0;
                    entry.rxBytes = reader2.nextLong();
                    entry.rxPackets = reader2.nextLong();
                    entry.txBytes = reader2.nextLong();
                    entry.txPackets = reader2.nextLong();
                    if (active) {
                        entry.rxBytes += reader2.nextLong();
                        entry.rxPackets += reader2.nextLong();
                        entry.txBytes += reader2.nextLong();
                        entry.txPackets += reader2.nextLong();
                    }
                    stats.addValues(entry);
                    reader2.finishLine();
                } catch (NullPointerException e) {
                } catch (Throwable th) {
                    Throwable th2 = th;
                    Object reader3 = reader2;
                    IoUtils.closeQuietly(reader);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th2;
                }
            }
            IoUtils.closeQuietly(reader2);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException e2) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0061 A:{Splitter: B:7:0x002a, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x007e A:{Splitter: B:5:0x001e, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Missing block: B:13:0x0061, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:14:0x0062, code:
            r2 = r3;
     */
    /* JADX WARNING: Missing block: B:17:0x006b, code:
            throw new java.net.ProtocolException("problem parsing stats", r0);
     */
    /* JADX WARNING: Missing block: B:18:0x006c, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:25:0x007e, code:
            r0 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NetworkStats readNetworkStatsSummaryXt() throws IOException {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (!this.mStatsXtIfaceFmt.exists()) {
            return null;
        }
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable reader = null;
        try {
            ProcFileReader reader2 = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceFmt));
            try {
                reader2.finishLine();
                while (reader2.hasMoreData()) {
                    entry.iface = reader2.nextString();
                    entry.uid = -1;
                    entry.set = -1;
                    entry.tag = 0;
                    entry.rxBytes = reader2.nextLong();
                    entry.rxPackets = reader2.nextLong();
                    entry.txBytes = reader2.nextLong();
                    entry.txPackets = reader2.nextLong();
                    stats.addValues(entry);
                    reader2.finishLine();
                }
                IoUtils.closeQuietly(reader2);
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            } catch (NullPointerException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                Object reader3 = reader2;
                IoUtils.closeQuietly(reader);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th2;
            }
        } catch (NullPointerException e2) {
        }
    }

    public NetworkStats readNetworkStatsDetail() throws IOException {
        return readNetworkStatsDetail(-1, null, -1, null);
    }

    public NetworkStats readNetworkStatsDetail(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        int i;
        NetworkStats stats = readNetworkStatsDetailInternal(limitUid, limitIfaces, limitTag, lastStats);
        Entry entry = null;
        synchronized (sStackedIfaces) {
            int size = sStackedIfaces.size();
            for (i = 0; i < size; i++) {
                String stackedIface = (String) sStackedIfaces.keyAt(i);
                String baseIface = (String) sStackedIfaces.valueAt(i);
                if (stackedIface.startsWith(CLATD_INTERFACE_PREFIX)) {
                    Entry adjust = new Entry(baseIface, 0, 0, 0, 0, 0, 0, 0, 0);
                    for (int j = 0; j < stats.size(); j++) {
                        entry = stats.getValues(j, entry);
                        if (Objects.equals(entry.iface, stackedIface)) {
                            adjust.rxBytes -= entry.rxBytes + (entry.rxPackets * 20);
                            adjust.txBytes -= entry.txBytes + (entry.txPackets * 20);
                            adjust.rxPackets -= entry.rxPackets;
                            adjust.txPackets -= entry.txPackets;
                        }
                    }
                    stats.combineValues(adjust);
                }
            }
        }
        for (i = 0; i < stats.size(); i++) {
            entry = stats.getValues(i, entry);
            if (entry.iface != null && (entry.iface.startsWith(CLATD_INTERFACE_PREFIX) ^ 1) == 0) {
                entry.rxBytes = entry.rxPackets * 20;
                entry.txBytes = entry.txPackets * 20;
                entry.rxPackets = 0;
                entry.txPackets = 0;
                stats.combineValues(entry);
            }
        }
        return stats;
    }

    private NetworkStats readNetworkStatsDetailInternal(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        NetworkStats stats;
        if (lastStats != null) {
            stats = lastStats;
            lastStats.setElapsedRealtime(SystemClock.elapsedRealtime());
        } else {
            stats = new NetworkStats(SystemClock.elapsedRealtime(), -1);
        }
        if (nativeReadNetworkStatsDetail(stats, this.mStatsXtUid.getAbsolutePath(), limitUid, limitIfaces, limitTag) == 0) {
            return stats;
        }
        Slog.w(TAG, "Failed to parse network stats! stats: " + stats.toString() + "path: " + this.mStatsXtUid.getAbsolutePath());
        throw new IOException("Failed to parse network stats");
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0058 A:{PHI: r2 , Splitter: B:3:0x0022, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00d9 A:{Splitter: B:1:0x0018, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Missing block: B:11:0x0058, code:
            r0 = e;
     */
    /* JADX WARNING: Missing block: B:12:0x0059, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:15:0x0073, code:
            throw new java.net.ProtocolException("problem parsing idx " + r2, r0);
     */
    /* JADX WARNING: Missing block: B:16:0x0074, code:
            r8 = th;
     */
    /* JADX WARNING: Missing block: B:38:0x00d9, code:
            r0 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static NetworkStats javaReadNetworkStatsDetail(File detailPath, int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        Entry entry = new Entry();
        int idx = 1;
        int lastIdx = 1;
        AutoCloseable reader = null;
        try {
            ProcFileReader reader2 = new ProcFileReader(new FileInputStream(detailPath));
            try {
                reader2.finishLine();
                while (reader2.hasMoreData()) {
                    idx = reader2.nextInt();
                    if (idx != lastIdx + 1) {
                        throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                    }
                    lastIdx = idx;
                    entry.iface = reader2.nextString();
                    entry.tag = NetworkManagementSocketTagger.kernelToTag(reader2.nextString());
                    entry.uid = reader2.nextInt();
                    entry.set = reader2.nextInt();
                    entry.rxBytes = reader2.nextLong();
                    entry.rxPackets = reader2.nextLong();
                    entry.txBytes = reader2.nextLong();
                    entry.txPackets = reader2.nextLong();
                    if ((limitIfaces == null || ArrayUtils.contains((Object[]) limitIfaces, entry.iface)) && ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag))) {
                        stats.addValues(entry);
                    }
                    reader2.finishLine();
                }
                IoUtils.closeQuietly(reader2);
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            } catch (NullPointerException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                Object reader3 = reader2;
                IoUtils.closeQuietly(reader);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th2;
            }
        } catch (NullPointerException e2) {
        }
    }

    public NetworkStats readNetworkStatsProcDetail(String iface) {
        NumberFormatException e;
        RuntimeException e2;
        IOException e3;
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (this.mStatsXtProcUid.exists()) {
            NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
            Entry entry = new Entry();
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(this.mStatsXtProcUid), "UTF-8"));
                while (true) {
                    try {
                        String line = reader2.readLine();
                        if (line != null) {
                            String[] procArr = line.trim().split("\\s+");
                            if (procArr.length >= 4) {
                                int i;
                                StringBuffer procName = new StringBuffer();
                                int i2 = 0;
                                while (true) {
                                    i = i2;
                                    if (i >= procArr.length - 3) {
                                        break;
                                    }
                                    procName.append(procArr[i]).append(" ");
                                    i2 = i + 1;
                                }
                                entry.iface = iface;
                                entry.proc = procName.toString().trim();
                                entry.set = -1;
                                entry.tag = 0;
                                entry.rxPackets = 0;
                                entry.txPackets = 0;
                                i2 = i + 1;
                                try {
                                    entry.uid = Integer.parseInt(procArr[i]);
                                    i = i2 + 1;
                                    try {
                                        entry.rxBytes = Long.parseLong(procArr[i2]);
                                        entry.txBytes = Long.parseLong(procArr[i]);
                                        stats.addValues(entry);
                                    } catch (NumberFormatException e4) {
                                        e = e4;
                                        i2 = i;
                                        Slog.e(TAG, "problem parsing line:" + line, e);
                                    }
                                } catch (NumberFormatException e5) {
                                    e = e5;
                                    Slog.e(TAG, "problem parsing line:" + line, e);
                                }
                            }
                        } else {
                            if (reader2 != null) {
                                try {
                                    reader2.close();
                                } catch (IOException ex) {
                                    Slog.e(TAG, "close reader exception", ex);
                                }
                            }
                            StrictMode.setThreadPolicy(savedPolicy);
                        }
                    } catch (RuntimeException e6) {
                        e2 = e6;
                        reader = reader2;
                    } catch (IOException e7) {
                        e3 = e7;
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
            } catch (RuntimeException e8) {
                e2 = e8;
                try {
                    Slog.e(TAG, "problem parsing proc stats", e2);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex2) {
                            Slog.e(TAG, "close reader exception", ex2);
                        }
                    }
                    StrictMode.setThreadPolicy(savedPolicy);
                    return stats;
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex22) {
                            Slog.e(TAG, "close reader exception", ex22);
                        }
                    }
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (IOException e9) {
                e3 = e9;
                Slog.e(TAG, "problem parsing proc stats", e3);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex222) {
                        Slog.e(TAG, "close reader exception", ex222);
                    }
                }
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            }
            return stats;
        }
        Slog.w(TAG, "mStatsXtProcUid: " + this.mStatsXtProcUid.getAbsolutePath() + " does not exist!");
        return null;
    }

    /* JADX WARNING: Missing block: B:28:0x0123, code:
            if (com.android.internal.util.ArrayUtils.contains((java.lang.Object[]) r24, (java.lang.Object) r10.iface) != false) goto L_0x0125;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NetworkStats javaReadNetworkStatsUidAndProcDetail(int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        NullPointerException e;
        Throwable th;
        NumberFormatException e2;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (this.mStatsXtProcAndUid.exists()) {
            long begin = SystemClock.elapsedRealtime();
            NetworkStats networkStats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
            Entry entry = new Entry();
            int lastIdx = 1;
            AutoCloseable reader = null;
            try {
                ProcFileReader reader2 = new ProcFileReader(new FileInputStream(this.mStatsXtProcAndUid));
                try {
                    reader2.finishLine();
                    while (reader2.hasMoreData()) {
                        int idx = reader2.nextInt();
                        if (idx != lastIdx + 1) {
                            throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                        }
                        lastIdx = idx;
                        entry.iface = reader2.nextString();
                        entry.proc = reader2.nextString();
                        entry.actUid = reader2.nextInt();
                        entry.tag = NetworkManagementSocketTagger.kernelToTag(reader2.nextString());
                        entry.uid = reader2.nextInt();
                        entry.set = reader2.nextInt();
                        entry.rxBytes = reader2.nextLong();
                        entry.rxPackets = reader2.nextLong();
                        entry.txBytes = reader2.nextLong();
                        entry.txPackets = reader2.nextLong();
                        if (limitIfaces != null) {
                        }
                        if ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag)) {
                            networkStats.addValues(entry);
                        }
                        reader2.finishLine();
                    }
                    IoUtils.closeQuietly(reader2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    Slog.i(TAG, "javaReadNetworkStatsUidAndProcDetail. cost time = " + (SystemClock.elapsedRealtime() - begin));
                    return networkStats;
                } catch (NullPointerException e3) {
                    e = e3;
                    reader = reader2;
                    try {
                        throw new ProtocolException("problem parsing xt_qprocuid stats", e);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(reader);
                        StrictMode.setThreadPolicy(savedPolicy);
                        throw th;
                    }
                } catch (NumberFormatException e4) {
                    e2 = e4;
                    reader = reader2;
                    throw new ProtocolException("problem parsing xt_qprocuid stats", e2);
                } catch (Throwable th3) {
                    th = th3;
                    Object reader3 = reader2;
                    IoUtils.closeQuietly(reader);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (NullPointerException e5) {
                e = e5;
                throw new ProtocolException("problem parsing xt_qprocuid stats", e);
            } catch (NumberFormatException e6) {
                e2 = e6;
                throw new ProtocolException("problem parsing xt_qprocuid stats", e2);
            }
        }
        Slog.w(TAG, "mStatsXtProcAndUid: " + this.mStatsXtProcAndUid.getAbsolutePath() + " does not exist!");
        return null;
    }

    public void assertEquals(NetworkStats expected, NetworkStats actual) {
        if (expected.size() != actual.size()) {
            throw new AssertionError("Expected size " + expected.size() + ", actual size " + actual.size());
        }
        Entry expectedRow = null;
        Entry actualRow = null;
        int i = 0;
        while (i < expected.size()) {
            expectedRow = expected.getValues(i, expectedRow);
            actualRow = actual.getValues(i, actualRow);
            if (expectedRow.equals(actualRow)) {
                i++;
            } else {
                throw new AssertionError("Expected row " + i + ": " + expectedRow + ", actual row " + actualRow);
            }
        }
    }
}
