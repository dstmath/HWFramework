package com.android.internal.util;

import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;

public class StatLogger {
    private static final String TAG = "StatLogger";
    private final int SIZE;
    @GuardedBy({"mLock"})
    private final int[] mCallsPerSecond;
    @GuardedBy({"mLock"})
    private final int[] mCountStats;
    @GuardedBy({"mLock"})
    private final long[] mDurationPerSecond;
    @GuardedBy({"mLock"})
    private final long[] mDurationStats;
    private final String[] mLabels;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final int[] mMaxCallsPerSecond;
    @GuardedBy({"mLock"})
    private final long[] mMaxDurationPerSecond;
    @GuardedBy({"mLock"})
    private final long[] mMaxDurationStats;
    @GuardedBy({"mLock"})
    private long mNextTickTime = (SystemClock.elapsedRealtime() + 1000);

    public StatLogger(String[] eventLabels) {
        this.SIZE = eventLabels.length;
        int i = this.SIZE;
        this.mCountStats = new int[i];
        this.mDurationStats = new long[i];
        this.mCallsPerSecond = new int[i];
        this.mMaxCallsPerSecond = new int[i];
        this.mDurationPerSecond = new long[i];
        this.mMaxDurationPerSecond = new long[i];
        this.mMaxDurationStats = new long[i];
        this.mLabels = eventLabels;
    }

    public long getTime() {
        return SystemClock.elapsedRealtimeNanos() / 1000;
    }

    public long logDurationStat(int eventId, long start) {
        synchronized (this.mLock) {
            long duration = getTime() - start;
            if (eventId < 0 || eventId >= this.SIZE) {
                Slog.wtf(TAG, "Invalid event ID: " + eventId);
                return duration;
            }
            int[] iArr = this.mCountStats;
            iArr[eventId] = iArr[eventId] + 1;
            long[] jArr = this.mDurationStats;
            jArr[eventId] = jArr[eventId] + duration;
            if (this.mMaxDurationStats[eventId] < duration) {
                this.mMaxDurationStats[eventId] = duration;
            }
            long nowRealtime = SystemClock.elapsedRealtime();
            if (nowRealtime > this.mNextTickTime) {
                if (this.mMaxCallsPerSecond[eventId] < this.mCallsPerSecond[eventId]) {
                    this.mMaxCallsPerSecond[eventId] = this.mCallsPerSecond[eventId];
                }
                if (this.mMaxDurationPerSecond[eventId] < this.mDurationPerSecond[eventId]) {
                    this.mMaxDurationPerSecond[eventId] = this.mDurationPerSecond[eventId];
                }
                this.mCallsPerSecond[eventId] = 0;
                this.mDurationPerSecond[eventId] = 0;
                this.mNextTickTime = 1000 + nowRealtime;
            }
            int[] iArr2 = this.mCallsPerSecond;
            iArr2[eventId] = iArr2[eventId] + 1;
            long[] jArr2 = this.mDurationPerSecond;
            jArr2[eventId] = jArr2[eventId] + duration;
            return duration;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        dump(new IndentingPrintWriter(pw, "  ").setIndent(prefix));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0046: APUT  
      (r9v1 java.lang.Object[])
      (3 ??[int, float, short, byte, char])
      (wrap: java.lang.Double : 0x0042: INVOKE  (r11v3 java.lang.Double) = (r11v2 double) type: STATIC call: java.lang.Double.valueOf(double):java.lang.Double)
     */
    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Stats:");
            pw.increaseIndent();
            for (int i = 0; i < this.SIZE; i++) {
                int count = this.mCountStats[i];
                double durationMs = ((double) this.mDurationStats[i]) / 1000.0d;
                Object[] objArr = new Object[7];
                objArr[0] = this.mLabels[i];
                objArr[1] = Integer.valueOf(count);
                objArr[2] = Double.valueOf(durationMs);
                objArr[3] = Double.valueOf(count == 0 ? 0.0d : durationMs / ((double) count));
                objArr[4] = Integer.valueOf(this.mMaxCallsPerSecond[i]);
                objArr[5] = Double.valueOf(((double) this.mMaxDurationPerSecond[i]) / 1000.0d);
                objArr[6] = Double.valueOf(((double) this.mMaxDurationStats[i]) / 1000.0d);
                pw.println(String.format("%s: count=%d, total=%.1fms, avg=%.3fms, max calls/s=%d max dur/s=%.1fms max time=%.1fms", objArr));
            }
            pw.decreaseIndent();
        }
    }

    public void dumpProto(ProtoOutputStream proto, long fieldId) {
        synchronized (this.mLock) {
            long outer = proto.start(fieldId);
            for (int i = 0; i < this.mLabels.length; i++) {
                long inner = proto.start(2246267895809L);
                proto.write(1120986464257L, i);
                proto.write(1138166333442L, this.mLabels[i]);
                proto.write(1120986464259L, this.mCountStats[i]);
                proto.write(1112396529668L, this.mDurationStats[i]);
                proto.end(inner);
            }
            proto.end(outer);
        }
    }
}
