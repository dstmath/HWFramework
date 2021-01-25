package com.android.server.am;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.util.RingBuffer;
import java.io.PrintWriter;

public class OomAdjProfiler {
    private static final boolean PROFILING_DISABLED = true;
    @GuardedBy({"this"})
    private long mLastSystemServerCpuTimeMs;
    @GuardedBy({"this"})
    private boolean mOnBattery;
    @GuardedBy({"this"})
    private CpuTimes mOomAdjRunTime = new CpuTimes();
    @GuardedBy({"this"})
    final RingBuffer<CpuTimes> mOomAdjRunTimesHist = new RingBuffer<>(CpuTimes.class, 10);
    @GuardedBy({"this"})
    private long mOomAdjStartTimeMs;
    @GuardedBy({"this"})
    private boolean mOomAdjStarted;
    private final ProcessCpuTracker mProcessCpuTracker = new ProcessCpuTracker(false);
    @GuardedBy({"this"})
    private boolean mScreenOff;
    @GuardedBy({"this"})
    private CpuTimes mSystemServerCpuTime = new CpuTimes();
    @GuardedBy({"this"})
    private boolean mSystemServerCpuTimeUpdateScheduled;
    @GuardedBy({"this"})
    final RingBuffer<CpuTimes> mSystemServerCpuTimesHist = new RingBuffer<>(CpuTimes.class, 10);

    /* access modifiers changed from: package-private */
    public void batteryPowerChanged(boolean onBattery) {
    }

    /* access modifiers changed from: package-private */
    public void onWakefulnessChanged(int wakefulness) {
    }

    /* access modifiers changed from: package-private */
    public void oomAdjStarted() {
    }

    /* access modifiers changed from: package-private */
    public void oomAdjEnded() {
    }

    private void scheduleSystemServerCpuTimeUpdate() {
    }

    private void updateSystemServerCpuTime(boolean onBattery, boolean screenOff) {
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        synchronized (this) {
            if (!this.mSystemServerCpuTime.isEmpty()) {
                this.mOomAdjRunTimesHist.append(this.mOomAdjRunTime);
                this.mSystemServerCpuTimesHist.append(this.mSystemServerCpuTime);
                this.mOomAdjRunTime = new CpuTimes();
                this.mSystemServerCpuTime = new CpuTimes();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw) {
    }

    /* access modifiers changed from: private */
    public class CpuTimes {
        private long mOnBatteryScreenOffTimeMs;
        private long mOnBatteryTimeMs;

        private CpuTimes() {
        }

        public void addCpuTimeMs(long cpuTimeMs) {
            addCpuTimeMs(cpuTimeMs, OomAdjProfiler.this.mOnBattery, OomAdjProfiler.this.mScreenOff);
        }

        public void addCpuTimeMs(long cpuTimeMs, boolean onBattery, boolean screenOff) {
            if (onBattery) {
                this.mOnBatteryTimeMs += cpuTimeMs;
                if (screenOff) {
                    this.mOnBatteryScreenOffTimeMs += cpuTimeMs;
                }
            }
        }

        public boolean isEmpty() {
            return this.mOnBatteryTimeMs == 0 && this.mOnBatteryScreenOffTimeMs == 0;
        }

        public String toString() {
            return "[" + this.mOnBatteryTimeMs + "," + this.mOnBatteryScreenOffTimeMs + "]";
        }
    }
}
