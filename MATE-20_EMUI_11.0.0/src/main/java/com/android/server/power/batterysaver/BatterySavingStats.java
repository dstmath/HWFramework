package com.android.server.power.batterysaver;

import android.os.BatteryManagerInternal;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BatterySavingStats {
    private static final boolean DEBUG = false;
    private static final int STATE_CHARGING = -2;
    private static final int STATE_NOT_INITIALIZED = -1;
    private static final String TAG = "BatterySavingStats";
    private BatteryManagerInternal mBatteryManagerInternal;
    @GuardedBy({"mLock"})
    private int mBatterySaverEnabledCount = 0;
    @GuardedBy({"mLock"})
    private int mCurrentState = -1;
    @GuardedBy({"mLock"})
    private boolean mIsBatterySaverEnabled;
    @GuardedBy({"mLock"})
    private long mLastBatterySaverDisabledTime = 0;
    @GuardedBy({"mLock"})
    private long mLastBatterySaverEnabledTime = 0;
    private final Object mLock;
    @GuardedBy({"mLock"})
    @VisibleForTesting
    final ArrayMap<Integer, Stat> mStats = new ArrayMap<>();

    /* access modifiers changed from: package-private */
    public interface BatterySaverState {
        public static final int ADAPTIVE = 2;
        public static final int BITS = 2;
        public static final int MASK = 3;
        public static final int OFF = 0;
        public static final int ON = 1;
        public static final int SHIFT = 0;

        static int fromIndex(int index) {
            return (index >> 0) & 3;
        }
    }

    /* access modifiers changed from: package-private */
    public interface InteractiveState {
        public static final int BITS = 1;
        public static final int INTERACTIVE = 1;
        public static final int MASK = 1;
        public static final int NON_INTERACTIVE = 0;
        public static final int SHIFT = 2;

        static int fromIndex(int index) {
            return (index >> 2) & 1;
        }
    }

    /* access modifiers changed from: package-private */
    public interface DozeState {
        public static final int BITS = 2;
        public static final int DEEP = 2;
        public static final int LIGHT = 1;
        public static final int MASK = 3;
        public static final int NOT_DOZING = 0;
        public static final int SHIFT = 3;

        static int fromIndex(int index) {
            return (index >> 3) & 3;
        }
    }

    /* access modifiers changed from: package-private */
    public static class Stat {
        public int endBatteryLevel;
        public int endBatteryPercent;
        public long endTime;
        public int startBatteryLevel;
        public int startBatteryPercent;
        public long startTime;
        public int totalBatteryDrain;
        public int totalBatteryDrainPercent;
        public long totalTimeMillis;

        Stat() {
        }

        public long totalMinutes() {
            return this.totalTimeMillis / 60000;
        }

        public double drainPerHour() {
            long j = this.totalTimeMillis;
            if (j == 0) {
                return 0.0d;
            }
            return ((double) this.totalBatteryDrain) / (((double) j) / 3600000.0d);
        }

        public double drainPercentPerHour() {
            long j = this.totalTimeMillis;
            if (j == 0) {
                return 0.0d;
            }
            return ((double) this.totalBatteryDrainPercent) / (((double) j) / 3600000.0d);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public String toStringForTest() {
            return "{" + totalMinutes() + "m," + this.totalBatteryDrain + "," + String.format("%.2f", Double.valueOf(drainPerHour())) + "uA/H," + String.format("%.2f", Double.valueOf(drainPercentPerHour())) + "%}";
        }
    }

    @VisibleForTesting
    public BatterySavingStats(Object lock) {
        this.mLock = lock;
        this.mBatteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
    }

    private BatteryManagerInternal getBatteryManagerInternal() {
        if (this.mBatteryManagerInternal == null) {
            this.mBatteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            if (this.mBatteryManagerInternal == null) {
                Slog.wtf(TAG, "BatteryManagerInternal not initialized");
            }
        }
        return this.mBatteryManagerInternal;
    }

    @VisibleForTesting
    static int statesToIndex(int batterySaverState, int interactiveState, int dozeState) {
        return (batterySaverState & 3) | ((interactiveState & 1) << 2) | ((dozeState & 3) << 3);
    }

    @VisibleForTesting
    static String stateToString(int state) {
        if (state == -2) {
            return "Charging";
        }
        if (state == -1) {
            return "NotInitialized";
        }
        return "BS=" + BatterySaverState.fromIndex(state) + ",I=" + InteractiveState.fromIndex(state) + ",D=" + DozeState.fromIndex(state);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Stat getStat(int stateIndex) {
        Stat stat;
        synchronized (this.mLock) {
            stat = this.mStats.get(Integer.valueOf(stateIndex));
            if (stat == null) {
                stat = new Stat();
                this.mStats.put(Integer.valueOf(stateIndex), stat);
            }
        }
        return stat;
    }

    private Stat getStat(int batterySaverState, int interactiveState, int dozeState) {
        return getStat(statesToIndex(batterySaverState, interactiveState, dozeState));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long injectCurrentTime() {
        return SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int injectBatteryLevel() {
        BatteryManagerInternal bmi = getBatteryManagerInternal();
        if (bmi == null) {
            return 0;
        }
        return bmi.getBatteryChargeCounter();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int injectBatteryPercent() {
        BatteryManagerInternal bmi = getBatteryManagerInternal();
        if (bmi == null) {
            return 0;
        }
        return bmi.getBatteryLevel();
    }

    public void transitionState(int batterySaverState, int interactiveState, int dozeState) {
        synchronized (this.mLock) {
            transitionStateLocked(statesToIndex(batterySaverState, interactiveState, dozeState));
        }
    }

    public void startCharging() {
        synchronized (this.mLock) {
            transitionStateLocked(-2);
        }
    }

    @GuardedBy({"mLock"})
    private void transitionStateLocked(int newState) {
        if (this.mCurrentState != newState) {
            long now = injectCurrentTime();
            int batteryLevel = injectBatteryLevel();
            int batteryPercent = injectBatteryPercent();
            boolean newBatterySaverEnabled = false;
            boolean oldBatterySaverEnabled = BatterySaverState.fromIndex(this.mCurrentState) != 0;
            if (BatterySaverState.fromIndex(newState) != 0) {
                newBatterySaverEnabled = true;
            }
            if (oldBatterySaverEnabled != newBatterySaverEnabled) {
                this.mIsBatterySaverEnabled = newBatterySaverEnabled;
                if (newBatterySaverEnabled) {
                    this.mBatterySaverEnabledCount++;
                    this.mLastBatterySaverEnabledTime = injectCurrentTime();
                } else {
                    this.mLastBatterySaverDisabledTime = injectCurrentTime();
                }
            }
            endLastStateLocked(now, batteryLevel, batteryPercent);
            startNewStateLocked(newState, now, batteryLevel, batteryPercent);
        }
    }

    @GuardedBy({"mLock"})
    private void endLastStateLocked(long now, int batteryLevel, int batteryPercent) {
        int i = this.mCurrentState;
        if (i >= 0) {
            Stat stat = getStat(i);
            stat.endBatteryLevel = batteryLevel;
            stat.endBatteryPercent = batteryPercent;
            stat.endTime = now;
            long deltaTime = stat.endTime - stat.startTime;
            int deltaDrain = stat.startBatteryLevel - stat.endBatteryLevel;
            int deltaPercent = stat.startBatteryPercent - stat.endBatteryPercent;
            stat.totalTimeMillis += deltaTime;
            stat.totalBatteryDrain += deltaDrain;
            stat.totalBatteryDrainPercent += deltaPercent;
            EventLogTags.writeBatterySavingStats(BatterySaverState.fromIndex(this.mCurrentState), InteractiveState.fromIndex(this.mCurrentState), DozeState.fromIndex(this.mCurrentState), deltaTime, deltaDrain, deltaPercent, stat.totalTimeMillis, stat.totalBatteryDrain, stat.totalBatteryDrainPercent);
        }
    }

    @GuardedBy({"mLock"})
    private void startNewStateLocked(int newState, long now, int batteryLevel, int batteryPercent) {
        this.mCurrentState = newState;
        int i = this.mCurrentState;
        if (i >= 0) {
            Stat stat = getStat(i);
            stat.startBatteryLevel = batteryLevel;
            stat.startBatteryPercent = batteryPercent;
            stat.startTime = now;
            stat.endTime = 0;
        }
    }

    public void dump(PrintWriter pw, String indent) {
        String str;
        synchronized (this.mLock) {
            try {
                pw.print(indent);
                pw.println("Battery saving stats:");
                StringBuilder sb = new StringBuilder();
                str = indent;
                try {
                    sb.append(str);
                    sb.append("  ");
                    String indent2 = sb.toString();
                    long now = System.currentTimeMillis();
                    long nowElapsed = injectCurrentTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    pw.print(indent2);
                    pw.print("Battery Saver is currently: ");
                    pw.println(this.mIsBatterySaverEnabled ? "ON" : "OFF");
                    if (this.mLastBatterySaverEnabledTime > 0) {
                        pw.print(indent2);
                        pw.print("  ");
                        pw.print("Last ON time: ");
                        pw.print(sdf.format(new Date((now - nowElapsed) + this.mLastBatterySaverEnabledTime)));
                        pw.print(" ");
                        TimeUtils.formatDuration(this.mLastBatterySaverEnabledTime, nowElapsed, pw);
                        pw.println();
                    }
                    if (this.mLastBatterySaverDisabledTime > 0) {
                        pw.print(indent2);
                        pw.print("  ");
                        pw.print("Last OFF time: ");
                        pw.print(sdf.format(new Date((now - nowElapsed) + this.mLastBatterySaverDisabledTime)));
                        pw.print(" ");
                        TimeUtils.formatDuration(this.mLastBatterySaverDisabledTime, nowElapsed, pw);
                        pw.println();
                    }
                    pw.print(indent2);
                    pw.print("  ");
                    pw.print("Times enabled: ");
                    pw.println(this.mBatterySaverEnabledCount);
                    pw.println();
                    pw.print(indent2);
                    pw.println("Drain stats:");
                    pw.print(indent2);
                    pw.println("                   Battery saver OFF                          ON");
                    dumpLineLocked(pw, indent2, 0, "NonIntr", 0, "NonDoze");
                    dumpLineLocked(pw, indent2, 1, "   Intr", 0, "       ");
                    dumpLineLocked(pw, indent2, 0, "NonIntr", 2, "Deep   ");
                    dumpLineLocked(pw, indent2, 1, "   Intr", 2, "       ");
                    dumpLineLocked(pw, indent2, 0, "NonIntr", 1, "Light  ");
                    dumpLineLocked(pw, indent2, 1, "   Intr", 1, "       ");
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                str = indent;
                throw th;
            }
        }
    }

    private void dumpLineLocked(PrintWriter pw, String indent, int interactiveState, String interactiveLabel, int dozeState, String dozeLabel) {
        pw.print(indent);
        pw.print(dozeLabel);
        pw.print(" ");
        pw.print(interactiveLabel);
        pw.print(": ");
        Stat offStat = getStat(0, interactiveState, dozeState);
        Stat onStat = getStat(1, interactiveState, dozeState);
        pw.println(String.format("%6dm %6dmAh(%3d%%) %8.1fmAh/h     %6dm %6dmAh(%3d%%) %8.1fmAh/h", Long.valueOf(offStat.totalMinutes()), Integer.valueOf(offStat.totalBatteryDrain / 1000), Integer.valueOf(offStat.totalBatteryDrainPercent), Double.valueOf(offStat.drainPerHour() / 1000.0d), Long.valueOf(onStat.totalMinutes()), Integer.valueOf(onStat.totalBatteryDrain / 1000), Integer.valueOf(onStat.totalBatteryDrainPercent), Double.valueOf(onStat.drainPerHour() / 1000.0d)));
    }
}
