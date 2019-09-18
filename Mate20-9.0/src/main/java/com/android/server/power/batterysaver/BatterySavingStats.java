package com.android.server.power.batterysaver;

import android.metrics.LogMaker;
import android.os.BatteryManagerInternal;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
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
    @GuardedBy("mLock")
    private int mBatterySaverEnabledCount;
    @GuardedBy("mLock")
    private int mCurrentState;
    @GuardedBy("mLock")
    private boolean mIsBatterySaverEnabled;
    @GuardedBy("mLock")
    private long mLastBatterySaverDisabledTime;
    @GuardedBy("mLock")
    private long mLastBatterySaverEnabledTime;
    private final Object mLock;
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger;
    private final MetricsLoggerHelper mMetricsLoggerHelper;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    @VisibleForTesting
    public boolean mSendTronLog;
    @GuardedBy("mLock")
    @VisibleForTesting
    final ArrayMap<Integer, Stat> mStats;

    interface BatterySaverState {
        public static final int BITS = 1;
        public static final int MASK = 1;
        public static final int OFF = 0;
        public static final int ON = 1;
        public static final int SHIFT = 0;

        static int fromIndex(int index) {
            return (index >> 0) & 1;
        }
    }

    interface DozeState {
        public static final int BITS = 2;
        public static final int DEEP = 2;
        public static final int LIGHT = 1;
        public static final int MASK = 3;
        public static final int NOT_DOZING = 0;
        public static final int SHIFT = 2;

        static int fromIndex(int index) {
            return (index >> 2) & 3;
        }
    }

    interface InteractiveState {
        public static final int BITS = 1;
        public static final int INTERACTIVE = 1;
        public static final int MASK = 1;
        public static final int NON_INTERACTIVE = 0;
        public static final int SHIFT = 1;

        static int fromIndex(int index) {
            return (index >> 1) & 1;
        }
    }

    @VisibleForTesting
    class MetricsLoggerHelper {
        private static final int STATE_CHANGE_DETECT_MASK = 3;
        private int mLastState = -1;
        private int mStartBatteryLevel;
        private int mStartPercent;
        private long mStartTime;

        MetricsLoggerHelper() {
        }

        public void transitionStateLocked(int newState, long now, int batteryLevel, int batteryPercent) {
            int i = newState;
            long j = now;
            boolean stateChanging = false;
            if (((this.mLastState >= 0) ^ (i >= 0)) || ((this.mLastState ^ i) & 3) != 0) {
                stateChanging = true;
            }
            if (stateChanging) {
                if (this.mLastState >= 0) {
                    reportLocked(this.mLastState, j - this.mStartTime, this.mStartBatteryLevel, this.mStartPercent, batteryLevel, batteryPercent);
                }
                this.mStartTime = j;
                this.mStartBatteryLevel = batteryLevel;
                this.mStartPercent = batteryPercent;
            } else {
                int i2 = batteryLevel;
                int i3 = batteryPercent;
            }
            this.mLastState = i;
        }

        /* access modifiers changed from: package-private */
        public void reportLocked(int state, long deltaTimeMs, int startBatteryLevelUa, int startBatteryLevelPercent, int endBatteryLevelUa, int endBatteryLevelPercent) {
            if (BatterySavingStats.this.mSendTronLog) {
                int i = 0;
                boolean batterySaverOn = BatterySaverState.fromIndex(state) != 0;
                boolean interactive = InteractiveState.fromIndex(state) != 0;
                LogMaker subtype = new LogMaker(1302).setSubtype(batterySaverOn ? 1 : 0);
                if (interactive) {
                    i = 1;
                }
                BatterySavingStats.this.mMetricsLogger.write(subtype.addTaggedData(1303, Integer.valueOf(i)).addTaggedData(1304, Long.valueOf(deltaTimeMs)).addTaggedData(1305, Integer.valueOf(startBatteryLevelUa)).addTaggedData(1307, Integer.valueOf(startBatteryLevelPercent)).addTaggedData(1306, Integer.valueOf(endBatteryLevelUa)).addTaggedData(1308, Integer.valueOf(endBatteryLevelPercent)));
            }
        }
    }

    static class Stat {
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
            if (this.totalTimeMillis == 0) {
                return 0.0d;
            }
            return ((double) this.totalBatteryDrain) / (((double) this.totalTimeMillis) / 3600000.0d);
        }

        public double drainPercentPerHour() {
            if (this.totalTimeMillis == 0) {
                return 0.0d;
            }
            return ((double) this.totalBatteryDrainPercent) / (((double) this.totalTimeMillis) / 3600000.0d);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public String toStringForTest() {
            return "{" + totalMinutes() + "m," + this.totalBatteryDrain + "," + String.format("%.2f", new Object[]{Double.valueOf(drainPerHour())}) + "uA/H," + String.format("%.2f", new Object[]{Double.valueOf(drainPercentPerHour())}) + "%}";
        }
    }

    @VisibleForTesting
    public BatterySavingStats(Object lock, MetricsLogger metricsLogger) {
        this.mCurrentState = -1;
        this.mStats = new ArrayMap<>();
        this.mBatterySaverEnabledCount = 0;
        this.mLastBatterySaverEnabledTime = 0;
        this.mLastBatterySaverDisabledTime = 0;
        this.mMetricsLoggerHelper = new MetricsLoggerHelper();
        this.mLock = lock;
        this.mBatteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
        this.mMetricsLogger = metricsLogger;
    }

    public BatterySavingStats(Object lock) {
        this(lock, new MetricsLogger());
    }

    public void setSendTronLog(boolean send) {
        synchronized (this.mLock) {
            this.mSendTronLog = send;
        }
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
        return (batterySaverState & 1) | ((interactiveState & 1) << 1) | ((dozeState & 3) << 2);
    }

    @VisibleForTesting
    static String stateToString(int state) {
        switch (state) {
            case -2:
                return "Charging";
            case -1:
                return "NotInitialized";
            default:
                return "BS=" + BatterySaverState.fromIndex(state) + ",I=" + InteractiveState.fromIndex(state) + ",D=" + DozeState.fromIndex(state);
        }
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

    @GuardedBy("mLock")
    private void transitionStateLocked(int newState) {
        if (this.mCurrentState != newState) {
            long now = injectCurrentTime();
            int batteryLevel = injectBatteryLevel();
            int batteryPercent = injectBatteryPercent();
            boolean z = false;
            boolean oldBatterySaverEnabled = BatterySaverState.fromIndex(this.mCurrentState) != 0;
            if (BatterySaverState.fromIndex(newState) != 0) {
                z = true;
            }
            boolean newBatterySaverEnabled = z;
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
            int i = newState;
            long j = now;
            int i2 = batteryLevel;
            int i3 = batteryPercent;
            startNewStateLocked(i, j, i2, i3);
            this.mMetricsLoggerHelper.transitionStateLocked(i, j, i2, i3);
        }
    }

    @GuardedBy("mLock")
    private void endLastStateLocked(long now, int batteryLevel, int batteryPercent) {
        if (this.mCurrentState >= 0) {
            Stat stat = getStat(this.mCurrentState);
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

    @GuardedBy("mLock")
    private void startNewStateLocked(int newState, long now, int batteryLevel, int batteryPercent) {
        this.mCurrentState = newState;
        if (this.mCurrentState >= 0) {
            Stat stat = getStat(this.mCurrentState);
            stat.startBatteryLevel = batteryLevel;
            stat.startBatteryPercent = batteryPercent;
            stat.startTime = now;
            stat.endTime = 0;
        }
    }

    public void dump(PrintWriter pw, String indent) {
        String str;
        PrintWriter printWriter = pw;
        synchronized (this.mLock) {
            try {
                pw.print(indent);
                printWriter.println("Battery saving stats:");
                StringBuilder sb = new StringBuilder();
                str = indent;
                try {
                    sb.append(str);
                    sb.append("  ");
                    String indent2 = sb.toString();
                    long now = System.currentTimeMillis();
                    long nowElapsed = injectCurrentTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    printWriter.print(indent2);
                    printWriter.print("Battery Saver is currently: ");
                    printWriter.println(this.mIsBatterySaverEnabled ? "ON" : "OFF");
                    if (this.mLastBatterySaverEnabledTime > 0) {
                        printWriter.print(indent2);
                        printWriter.print("  ");
                        printWriter.print("Last ON time: ");
                        printWriter.print(sdf.format(new Date((now - nowElapsed) + this.mLastBatterySaverEnabledTime)));
                        printWriter.print(" ");
                        TimeUtils.formatDuration(this.mLastBatterySaverEnabledTime, nowElapsed, printWriter);
                        pw.println();
                    }
                    if (this.mLastBatterySaverDisabledTime > 0) {
                        printWriter.print(indent2);
                        printWriter.print("  ");
                        printWriter.print("Last OFF time: ");
                        printWriter.print(sdf.format(new Date((now - nowElapsed) + this.mLastBatterySaverDisabledTime)));
                        printWriter.print(" ");
                        TimeUtils.formatDuration(this.mLastBatterySaverDisabledTime, nowElapsed, printWriter);
                        pw.println();
                    }
                    printWriter.print(indent2);
                    printWriter.print("  ");
                    printWriter.print("Times enabled: ");
                    printWriter.println(this.mBatterySaverEnabledCount);
                    pw.println();
                    printWriter.print(indent2);
                    printWriter.println("Drain stats:");
                    printWriter.print(indent2);
                    printWriter.println("                   Battery saver OFF                          ON");
                    dumpLineLocked(printWriter, indent2, 0, "NonIntr", 0, "NonDoze");
                    dumpLineLocked(printWriter, indent2, 1, "   Intr", 0, "       ");
                    dumpLineLocked(printWriter, indent2, 0, "NonIntr", 2, "Deep   ");
                    dumpLineLocked(printWriter, indent2, 1, "   Intr", 2, "       ");
                    dumpLineLocked(printWriter, indent2, 0, "NonIntr", 1, "Light  ");
                    dumpLineLocked(printWriter, indent2, 1, "   Intr", 1, "       ");
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                str = indent;
                String indent3 = str;
                throw th;
            }
        }
    }

    private void dumpLineLocked(PrintWriter pw, String indent, int interactiveState, String interactiveLabel, int dozeState, String dozeLabel) {
        PrintWriter printWriter = pw;
        int i = interactiveState;
        int i2 = dozeState;
        pw.print(indent);
        printWriter.print(dozeLabel);
        printWriter.print(" ");
        printWriter.print(interactiveLabel);
        printWriter.print(": ");
        Stat offStat = getStat(0, i, i2);
        Stat onStat = getStat(1, i, i2);
        printWriter.println(String.format("%6dm %6dmAh(%3d%%) %8.1fmAh/h     %6dm %6dmAh(%3d%%) %8.1fmAh/h", new Object[]{Long.valueOf(offStat.totalMinutes()), Integer.valueOf(offStat.totalBatteryDrain / 1000), Integer.valueOf(offStat.totalBatteryDrainPercent), Double.valueOf(offStat.drainPerHour() / 1000.0d), Long.valueOf(onStat.totalMinutes()), Integer.valueOf(onStat.totalBatteryDrain / 1000), Integer.valueOf(onStat.totalBatteryDrainPercent), Double.valueOf(onStat.drainPerHour() / 1000.0d)}));
    }
}
