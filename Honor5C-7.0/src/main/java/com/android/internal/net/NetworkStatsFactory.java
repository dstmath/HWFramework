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
    private static final boolean SANITY_CHECK_NATIVE = false;
    private static final String TAG = "NetworkStatsFactory";
    private static final boolean USE_NATIVE_PARSING = true;
    @GuardedBy("sStackedIfaces")
    private static final ArrayMap<String, String> sStackedIfaces = null;
    private final File mStatsXtIfaceAll;
    private final File mStatsXtIfaceFmt;
    private final File mStatsXtProcUid;
    private final File mStatsXtUid;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.net.NetworkStatsFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.net.NetworkStatsFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.net.NetworkStatsFactory.<clinit>():void");
    }

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
    }

    public NetworkStats readNetworkStatsSummaryDev() throws IOException {
        NullPointerException e;
        NumberFormatException e2;
        Object reader;
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable autoCloseable = null;
        try {
            ProcFileReader reader2 = new ProcFileReader(new FileInputStream(this.mStatsXtIfaceAll));
            while (reader2.hasMoreData()) {
                try {
                    entry.iface = reader2.nextString();
                    entry.uid = -1;
                    entry.set = -1;
                    entry.tag = 0;
                    boolean active = reader2.nextInt() != 0 ? USE_NATIVE_PARSING : SANITY_CHECK_NATIVE;
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
                } catch (NullPointerException e3) {
                    e = e3;
                    autoCloseable = reader2;
                } catch (NumberFormatException e4) {
                    e2 = e4;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
            IoUtils.closeQuietly(reader2);
            StrictMode.setThreadPolicy(savedPolicy);
            return stats;
        } catch (NullPointerException e5) {
            e = e5;
            try {
                throw new ProtocolException("problem parsing stats", e);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(autoCloseable);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing stats", e2);
        }
    }

    public NetworkStats readNetworkStatsSummaryXt() throws IOException {
        NullPointerException e;
        Throwable th;
        NumberFormatException e2;
        Object reader;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        if (!this.mStatsXtIfaceFmt.exists()) {
            return null;
        }
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 6);
        Entry entry = new Entry();
        AutoCloseable autoCloseable = null;
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
            } catch (NullPointerException e3) {
                e = e3;
                autoCloseable = reader2;
                try {
                    throw new ProtocolException("problem parsing stats", e);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(autoCloseable);
                    StrictMode.setThreadPolicy(savedPolicy);
                    throw th;
                }
            } catch (NumberFormatException e4) {
                e2 = e4;
                reader = reader2;
                throw new ProtocolException("problem parsing stats", e2);
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                IoUtils.closeQuietly(autoCloseable);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (NullPointerException e5) {
            e = e5;
            throw new ProtocolException("problem parsing stats", e);
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing stats", e2);
        }
    }

    public NetworkStats readNetworkStatsDetail() throws IOException {
        return readNetworkStatsDetail(-1, null, -1, null);
    }

    public NetworkStats readNetworkStatsDetail(int limitUid, String[] limitIfaces, int limitTag, NetworkStats lastStats) throws IOException {
        Entry entry;
        NetworkStats stats = readNetworkStatsDetailInternal(limitUid, limitIfaces, limitTag, lastStats);
        synchronized (sStackedIfaces) {
            int i;
            int size = sStackedIfaces.size();
            for (i = 0; i < size; i++) {
                String stackedIface = (String) sStackedIfaces.keyAt(i);
                Entry adjust = new Entry((String) sStackedIfaces.valueAt(i), 0, 0, 0, 0, 0, 0, 0, 0);
                entry = null;
                for (int j = 0; j < stats.size(); j++) {
                    entry = stats.getValues(j, entry);
                    if (Objects.equals(entry.iface, stackedIface)) {
                        adjust.txBytes -= entry.txBytes;
                        adjust.txPackets -= entry.txPackets;
                    }
                }
                stats.combineValues(adjust);
            }
        }
        entry = null;
        for (i = 0; i < stats.size(); i++) {
            entry = stats.getValues(i, entry);
            if (entry.iface != null && entry.iface.startsWith("clat")) {
                entry.rxBytes = entry.rxPackets * 20;
                entry.rxPackets = 0;
                entry.txBytes = 0;
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

    public static NetworkStats javaReadNetworkStatsDetail(File detailPath, int limitUid, String[] limitIfaces, int limitTag) throws IOException {
        NullPointerException e;
        NumberFormatException e2;
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 24);
        Entry entry = new Entry();
        int idx = 1;
        int lastIdx = 1;
        AutoCloseable autoCloseable = null;
        try {
            ProcFileReader reader = new ProcFileReader(new FileInputStream(detailPath));
            try {
                reader.finishLine();
                while (reader.hasMoreData()) {
                    idx = reader.nextInt();
                    if (idx != lastIdx + 1) {
                        throw new ProtocolException("inconsistent idx=" + idx + " after lastIdx=" + lastIdx);
                    }
                    lastIdx = idx;
                    entry.iface = reader.nextString();
                    entry.tag = NetworkManagementSocketTagger.kernelToTag(reader.nextString());
                    entry.uid = reader.nextInt();
                    entry.set = reader.nextInt();
                    entry.rxBytes = reader.nextLong();
                    entry.rxPackets = reader.nextLong();
                    entry.txBytes = reader.nextLong();
                    entry.txPackets = reader.nextLong();
                    if ((limitIfaces == null || ArrayUtils.contains((Object[]) limitIfaces, entry.iface)) && ((limitUid == -1 || limitUid == entry.uid) && (limitTag == -1 || limitTag == entry.tag))) {
                        stats.addValues(entry);
                    }
                    reader.finishLine();
                }
                IoUtils.closeQuietly(reader);
                StrictMode.setThreadPolicy(savedPolicy);
                return stats;
            } catch (NullPointerException e3) {
                e = e3;
                autoCloseable = reader;
            } catch (NumberFormatException e4) {
                e2 = e4;
                autoCloseable = reader;
            } catch (Throwable th2) {
                th = th2;
                Object reader2 = reader;
            }
        } catch (NullPointerException e5) {
            e = e5;
            try {
                throw new ProtocolException("problem parsing idx " + idx, e);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(autoCloseable);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (NumberFormatException e6) {
            e2 = e6;
            throw new ProtocolException("problem parsing idx " + idx, e2);
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
            BufferedReader bufferedReader = null;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.mStatsXtProcUid), "UTF-8"));
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        String[] procArr = line.trim().split("\\s+");
                        int length = procArr.length;
                        if (r0 >= 4) {
                            StringBuffer procName = new StringBuffer();
                            int i = 0;
                            while (true) {
                                if (i >= procArr.length - 3) {
                                    break;
                                }
                                procName.append(procArr[i]).append(" ");
                                i++;
                            }
                            entry.iface = iface;
                            entry.proc = procName.toString().trim();
                            entry.set = -1;
                            entry.tag = 0;
                            entry.rxPackets = 0;
                            entry.txPackets = 0;
                            int i2 = i + 1;
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
                    } catch (RuntimeException e6) {
                        e2 = e6;
                        bufferedReader = reader;
                    } catch (IOException e7) {
                        e3 = e7;
                        bufferedReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = reader;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        Slog.e(TAG, "close reader exception", ex);
                    }
                }
                StrictMode.setThreadPolicy(savedPolicy);
            } catch (RuntimeException e8) {
                e2 = e8;
                try {
                    Slog.e(TAG, "problem parsing proc stats", e2);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex2) {
                            Slog.e(TAG, "close reader exception", ex2);
                        }
                    }
                    StrictMode.setThreadPolicy(savedPolicy);
                    return stats;
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
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
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
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
