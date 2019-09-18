package com.android.internal.os;

import android.os.Binder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BinderCallsStats {
    private static final int CALL_SESSIONS_POOL_SIZE = 100;
    private static final BinderCallsStats sInstance = new BinderCallsStats();
    private final Queue<CallSession> mCallSessionsPool = new ConcurrentLinkedQueue();
    private volatile boolean mDetailedTracking = false;
    private final Object mLock = new Object();
    private long mStartTime = System.currentTimeMillis();
    @GuardedBy("mLock")
    private final SparseArray<UidEntry> mUidEntries = new SparseArray<>();

    public static class CallSession {
        CallStat mCallStat = new CallStat();
        int mCallingUId;
        long mStarted;
    }

    private static class CallStat {
        long callCount;
        String className;
        int msg;
        long time;

        CallStat() {
        }

        CallStat(String className2, int msg2) {
            this.className = className2;
            this.msg = msg2;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            CallStat callStat = (CallStat) o;
            if (this.msg != callStat.msg || !this.className.equals(callStat.className)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * this.className.hashCode()) + this.msg;
        }

        public String toString() {
            return this.className + "/" + this.msg;
        }
    }

    private static class UidEntry {
        long callCount;
        Map<CallStat, CallStat> mCallStats = new ArrayMap();
        long time;
        int uid;

        UidEntry(int uid2) {
            this.uid = uid2;
        }

        public String toString() {
            return "UidEntry{time=" + this.time + ", callCount=" + this.callCount + ", mCallStats=" + this.mCallStats + '}';
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (this.uid != ((UidEntry) o).uid) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.uid;
        }
    }

    private BinderCallsStats() {
    }

    @VisibleForTesting
    public BinderCallsStats(boolean detailedTracking) {
        this.mDetailedTracking = detailedTracking;
    }

    public CallSession callStarted(Binder binder, int code) {
        return callStarted(binder.getClass().getName(), code);
    }

    private CallSession callStarted(String className, int code) {
        CallSession s = this.mCallSessionsPool.poll();
        if (s == null) {
            s = new CallSession();
        }
        s.mCallStat.className = className;
        s.mCallStat.msg = code;
        s.mStarted = getThreadTimeMicro();
        return s;
    }

    public void callEnded(CallSession s) {
        Preconditions.checkNotNull(s);
        long duration = this.mDetailedTracking ? getThreadTimeMicro() - s.mStarted : 1;
        s.mCallingUId = Binder.getCallingUid();
        synchronized (this.mLock) {
            UidEntry uidEntry = this.mUidEntries.get(s.mCallingUId);
            if (uidEntry == null) {
                uidEntry = new UidEntry(s.mCallingUId);
                this.mUidEntries.put(s.mCallingUId, uidEntry);
            }
            if (this.mDetailedTracking) {
                CallStat callStat = uidEntry.mCallStats.get(s.mCallStat);
                if (callStat == null) {
                    callStat = new CallStat(s.mCallStat.className, s.mCallStat.msg);
                    uidEntry.mCallStats.put(callStat, callStat);
                }
                callStat.callCount++;
                callStat.time += duration;
            }
            uidEntry.time += duration;
            uidEntry.callCount++;
        }
        if (this.mCallSessionsPool.size() < 100) {
            this.mCallSessionsPool.add(s);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bf, code lost:
        if (r31.mDetailedTracking == false) goto L_0x01e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00c1, code lost:
        r2.println("Raw data (uid,call_desc,time):");
        r10.sort(com.android.internal.os.$$Lambda$BinderCallsStats$JdIS98lVGLAIfkEkC976rVyBc_U.INSTANCE);
        r0 = new java.lang.StringBuilder();
        r11 = r10.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d8, code lost:
        if (r11.hasNext() == false) goto L_0x0137;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00da, code lost:
        r14 = r11.next();
        r5 = new java.util.ArrayList<>(r14.mCallStats.keySet());
        r5.sort(com.android.internal.os.$$Lambda$BinderCallsStats$8JB19VSNkNr7RqU7ZTJ6NGkFXVU.INSTANCE);
        r6 = r5.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00fa, code lost:
        if (r6.hasNext() == false) goto L_0x0133;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00fc, code lost:
        r15 = r6.next();
        r0.setLength(0);
        r0.append("    ");
        r0.append(r14.uid);
        r0.append(",");
        r0.append(r15);
        r0.append(',');
        r0.append(r15.time);
        r2.println(r0);
        r5 = r5;
        r6 = r6;
        r1 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0133, code lost:
        r1 = r31;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0137, code lost:
        r32.println();
        r2.println("Per UID Summary(UID: time, % of total_time, calls_count):");
        r1 = new java.util.ArrayList<>(r3.entrySet());
        r1.sort(com.android.internal.os.$$Lambda$BinderCallsStats$BeSOWJ8AoyB7S9CtX6IPAXHyNQ.INSTANCE);
        r5 = r1.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0155, code lost:
        if (r5.hasNext() == false) goto L_0x01b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0157, code lost:
        r6 = r5.next();
        r26 = r6;
        r2.println(java.lang.String.format("  %7d: %11d %3.0f%% %8d", new java.lang.Object[]{r6.getKey(), r6.getValue(), java.lang.Double.valueOf((((double) r6.getValue().longValue()) * 100.0d) / ((double) r7)), (java.lang.Long) r4.get(r6.getKey())}));
        r0 = r0;
        r1 = r1;
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x01b0, code lost:
        r23 = r0;
        r24 = r1;
        r32.println();
        r2.println(java.lang.String.format("  Summary: total_time=%d, calls_count=%d, avg_call_time=%.0f", new java.lang.Object[]{java.lang.Long.valueOf(r7), java.lang.Long.valueOf(r12), java.lang.Double.valueOf(((double) r7) / ((double) r12))}));
        r29 = r3;
        r30 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x01e2, code lost:
        r2.println("Per UID Summary(UID: calls_count, % of total calls_count):");
        r0 = new java.util.ArrayList<>(r3.entrySet());
        r0.sort(com.android.internal.os.$$Lambda$BinderCallsStats$jhdszMKzG9FSuIQ4Vz9B0exXKPk.INSTANCE);
        r1 = r0.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x01fd, code lost:
        if (r1.hasNext() == false) goto L_0x0253;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x01ff, code lost:
        r5 = r1.next();
        r2.println(java.lang.String.format("    %7d: %8d %3.0f%%", new java.lang.Object[]{r5.getKey(), r4.get(r5.getKey()), java.lang.Double.valueOf((((double) r5.getValue().longValue()) * 100.0d) / ((double) r7))}));
        r0 = r0;
        r1 = r1;
        r3 = r3;
        r4 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0253, code lost:
        r29 = r3;
        r30 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        return;
     */
    public void dump(PrintWriter pw) {
        UidEntry e;
        long totalCallsTime;
        long totalCallsTime2;
        long j;
        long j2;
        BinderCallsStats binderCallsStats = this;
        PrintWriter printWriter = pw;
        Map<Integer, Long> uidTimeMap = new HashMap<>();
        Map<Integer, Long> uidCallCountMap = new HashMap<>();
        long totalCallsTime3 = 0;
        printWriter.print("Start time: ");
        printWriter.println(DateFormat.format("yyyy-MM-dd HH:mm:ss", binderCallsStats.mStartTime));
        int uidEntriesSize = binderCallsStats.mUidEntries.size();
        List<UidEntry> entries = new ArrayList<>();
        synchronized (binderCallsStats.mLock) {
            long totalCallsCount = 0;
            int i = 0;
            while (i < uidEntriesSize) {
                try {
                    e = binderCallsStats.mUidEntries.valueAt(i);
                    entries.add(e);
                    totalCallsTime = totalCallsTime3 + e.time;
                } catch (Throwable th) {
                    th = th;
                    HashMap hashMap = uidTimeMap;
                    HashMap hashMap2 = uidCallCountMap;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    throw th;
                }
                try {
                    Long totalTimePerUid = (Long) uidTimeMap.get(Integer.valueOf(e.uid));
                    Integer valueOf = Integer.valueOf(e.uid);
                    if (totalTimePerUid == null) {
                        j = e.time;
                        totalCallsTime2 = totalCallsTime;
                    } else {
                        totalCallsTime2 = totalCallsTime;
                        try {
                            j = totalTimePerUid.longValue() + e.time;
                        } catch (Throwable th3) {
                            th = th3;
                            HashMap hashMap3 = uidTimeMap;
                            HashMap hashMap4 = uidCallCountMap;
                            long j3 = totalCallsTime2;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                    uidTimeMap.put(valueOf, Long.valueOf(j));
                    Long totalCallsPerUid = (Long) uidCallCountMap.get(Integer.valueOf(e.uid));
                    Integer valueOf2 = Integer.valueOf(e.uid);
                    if (totalCallsPerUid == null) {
                        j2 = e.callCount;
                        Long l = totalTimePerUid;
                    } else {
                        Long l2 = totalTimePerUid;
                        j2 = totalCallsPerUid.longValue() + e.callCount;
                    }
                    uidCallCountMap.put(valueOf2, Long.valueOf(j2));
                    totalCallsCount += e.callCount;
                    i++;
                    totalCallsTime3 = totalCallsTime2;
                    binderCallsStats = this;
                } catch (Throwable th4) {
                    th = th4;
                    long j4 = totalCallsTime;
                    HashMap hashMap5 = uidTimeMap;
                    HashMap hashMap6 = uidCallCountMap;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            try {
            } catch (Throwable th5) {
                th = th5;
                Map<Integer, Long> map = uidTimeMap;
                Map<Integer, Long> map2 = uidCallCountMap;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    static /* synthetic */ int lambda$dump$0(UidEntry o1, UidEntry o2) {
        if (o1.time < o2.time) {
            return 1;
        }
        if (o1.time > o2.time) {
            return -1;
        }
        return 0;
    }

    static /* synthetic */ int lambda$dump$1(CallStat o1, CallStat o2) {
        if (o1.time < o2.time) {
            return 1;
        }
        if (o1.time > o2.time) {
            return -1;
        }
        return 0;
    }

    private long getThreadTimeMicro() {
        if (this.mDetailedTracking) {
            return SystemClock.currentThreadTimeMicro();
        }
        return 0;
    }

    public static BinderCallsStats getInstance() {
        return sInstance;
    }

    public void setDetailedTracking(boolean enabled) {
        if (enabled != this.mDetailedTracking) {
            reset();
            this.mDetailedTracking = enabled;
        }
    }

    public void reset() {
        synchronized (this.mLock) {
            this.mUidEntries.clear();
            this.mStartTime = System.currentTimeMillis();
        }
    }
}
