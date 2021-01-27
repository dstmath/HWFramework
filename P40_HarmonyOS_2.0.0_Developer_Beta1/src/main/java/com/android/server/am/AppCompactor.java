package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.DeviceConfig;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.BatteryService;
import com.android.server.ServiceThread;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.job.controllers.JobStatus;
import com.android.server.slice.SliceClientPermissions;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class AppCompactor {
    private static final String COMPACT_ACTION_ANON = "anon";
    private static final int COMPACT_ACTION_ANON_FLAG = 2;
    private static final String COMPACT_ACTION_FILE = "file";
    private static final int COMPACT_ACTION_FILE_FLAG = 1;
    private static final String COMPACT_ACTION_FULL = "all";
    private static final int COMPACT_ACTION_FULL_FLAG = 3;
    private static final String COMPACT_ACTION_NONE = "";
    private static final int COMPACT_ACTION_NONE_FLAG = 4;
    static final int COMPACT_PROCESS_BFGS = 4;
    static final int COMPACT_PROCESS_FULL = 2;
    static final int COMPACT_PROCESS_MSG = 1;
    static final int COMPACT_PROCESS_PERSISTENT = 3;
    static final int COMPACT_PROCESS_SOME = 1;
    static final int COMPACT_SYSTEM_MSG = 2;
    @VisibleForTesting
    static final int DEFAULT_COMPACT_ACTION_1 = 1;
    @VisibleForTesting
    static final int DEFAULT_COMPACT_ACTION_2 = 3;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_FULL_DELTA_RSS_THROTTLE_KB = 8000;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_FULL_RSS_THROTTLE_KB = 12000;
    @VisibleForTesting
    static final String DEFAULT_COMPACT_PROC_STATE_THROTTLE = String.valueOf(12);
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_1 = 5000;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_2 = 10000;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_3 = 500;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_4 = 10000;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_5 = 600000;
    @VisibleForTesting
    static final long DEFAULT_COMPACT_THROTTLE_6 = 600000;
    @VisibleForTesting
    static final float DEFAULT_STATSD_SAMPLE_RATE = 0.1f;
    @VisibleForTesting
    static final Boolean DEFAULT_USE_COMPACTION = false;
    private static final boolean IS_WATCH = "watch".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    @VisibleForTesting
    static final String KEY_COMPACT_ACTION_1 = "compact_action_1";
    @VisibleForTesting
    static final String KEY_COMPACT_ACTION_2 = "compact_action_2";
    @VisibleForTesting
    static final String KEY_COMPACT_FULL_DELTA_RSS_THROTTLE_KB = "compact_full_delta_rss_throttle_kb";
    @VisibleForTesting
    static final String KEY_COMPACT_FULL_RSS_THROTTLE_KB = "compact_full_rss_throttle_kb";
    @VisibleForTesting
    static final String KEY_COMPACT_PROC_STATE_THROTTLE = "compact_proc_state_throttle";
    @VisibleForTesting
    static final String KEY_COMPACT_STATSD_SAMPLE_RATE = "compact_statsd_sample_rate";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_1 = "compact_throttle_1";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_2 = "compact_throttle_2";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_3 = "compact_throttle_3";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_4 = "compact_throttle_4";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_5 = "compact_throttle_5";
    @VisibleForTesting
    static final String KEY_COMPACT_THROTTLE_6 = "compact_throttle_6";
    @VisibleForTesting
    static final String KEY_USE_COMPACTION = "use_compaction";
    private final ActivityManagerService mAm;
    private int mBfgsCompactionCount;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile String mCompactActionFull;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile String mCompactActionSome;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottleBFGS;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottleFullFull;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottleFullSome;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottlePersistent;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottleSomeFull;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mCompactThrottleSomeSome;
    private Handler mCompactionHandler;
    final ServiceThread mCompactionThread;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile long mFullAnonRssThrottleKb;
    private int mFullCompactionCount;
    @GuardedBy({"mPhenoypeFlagLock"})
    @VisibleForTesting
    volatile long mFullDeltaRssThrottleKb;
    private Map<Integer, LastCompactionStats> mLastCompactionStats;
    private final DeviceConfig.OnPropertiesChangedListener mOnFlagsChangedListener;
    private final ArrayList<ProcessRecord> mPendingCompactionProcesses;
    private int mPersistentCompactionCount;
    private final Object mPhenotypeFlagLock;
    @GuardedBy({"mPhenoypeFlagLock"})
    @VisibleForTesting
    final Set<Integer> mProcStateThrottle;
    private final Random mRandom;
    private int mSomeCompactionCount;
    @GuardedBy({"mPhenotypeFlagLock"})
    @VisibleForTesting
    volatile float mStatsdSampleRate;
    private PropertyChangedCallbackForTest mTestCallback;
    @GuardedBy({"mPhenotypeFlagLock"})
    private volatile boolean mUseCompaction;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public interface PropertyChangedCallbackForTest {
        void onPropertyChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void compactSystem();

    static /* synthetic */ int access$1308(AppCompactor x0) {
        int i = x0.mSomeCompactionCount;
        x0.mSomeCompactionCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1408(AppCompactor x0) {
        int i = x0.mFullCompactionCount;
        x0.mFullCompactionCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1508(AppCompactor x0) {
        int i = x0.mPersistentCompactionCount;
        x0.mPersistentCompactionCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1608(AppCompactor x0) {
        int i = x0.mBfgsCompactionCount;
        x0.mBfgsCompactionCount = i + 1;
        return i;
    }

    public AppCompactor(ActivityManagerService am) {
        this.mPendingCompactionProcesses = new ArrayList<>();
        this.mOnFlagsChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.server.am.AppCompactor.AnonymousClass1 */

            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                synchronized (AppCompactor.this.mPhenotypeFlagLock) {
                    for (String name : properties.getKeyset()) {
                        if (AppCompactor.KEY_USE_COMPACTION.equals(name)) {
                            AppCompactor.this.updateUseCompaction();
                        } else {
                            if (!AppCompactor.KEY_COMPACT_ACTION_1.equals(name)) {
                                if (!AppCompactor.KEY_COMPACT_ACTION_2.equals(name)) {
                                    if (!AppCompactor.KEY_COMPACT_THROTTLE_1.equals(name) && !AppCompactor.KEY_COMPACT_THROTTLE_2.equals(name) && !AppCompactor.KEY_COMPACT_THROTTLE_3.equals(name)) {
                                        if (!AppCompactor.KEY_COMPACT_THROTTLE_4.equals(name)) {
                                            if (AppCompactor.KEY_COMPACT_STATSD_SAMPLE_RATE.equals(name)) {
                                                AppCompactor.this.updateStatsdSampleRate();
                                            } else if (AppCompactor.KEY_COMPACT_FULL_RSS_THROTTLE_KB.equals(name)) {
                                                AppCompactor.this.updateFullRssThrottle();
                                            } else if (AppCompactor.KEY_COMPACT_FULL_DELTA_RSS_THROTTLE_KB.equals(name)) {
                                                AppCompactor.this.updateFullDeltaRssThrottle();
                                            } else if (AppCompactor.KEY_COMPACT_PROC_STATE_THROTTLE.equals(name)) {
                                                AppCompactor.this.updateProcStateThrottle();
                                            }
                                        }
                                    }
                                    AppCompactor.this.updateCompactionThrottles();
                                }
                            }
                            AppCompactor.this.updateCompactionActions();
                        }
                    }
                }
                if (AppCompactor.this.mTestCallback != null) {
                    AppCompactor.this.mTestCallback.onPropertyChanged();
                }
            }
        };
        this.mPhenotypeFlagLock = new Object();
        this.mCompactActionSome = compactActionIntToString(1);
        this.mCompactActionFull = compactActionIntToString(3);
        this.mCompactThrottleSomeSome = DEFAULT_COMPACT_THROTTLE_1;
        this.mCompactThrottleSomeFull = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        this.mCompactThrottleFullSome = 500;
        this.mCompactThrottleFullFull = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        this.mCompactThrottleBFGS = 600000;
        this.mCompactThrottlePersistent = 600000;
        this.mUseCompaction = DEFAULT_USE_COMPACTION.booleanValue();
        this.mRandom = new Random();
        this.mStatsdSampleRate = DEFAULT_STATSD_SAMPLE_RATE;
        this.mFullAnonRssThrottleKb = DEFAULT_COMPACT_FULL_RSS_THROTTLE_KB;
        this.mFullDeltaRssThrottleKb = DEFAULT_COMPACT_FULL_DELTA_RSS_THROTTLE_KB;
        this.mLastCompactionStats = new LinkedHashMap<Integer, LastCompactionStats>() {
            /* class com.android.server.am.AppCompactor.AnonymousClass2 */

            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.util.Map$Entry] */
            /* access modifiers changed from: protected */
            @Override // java.util.LinkedHashMap
            public boolean removeEldestEntry(Map.Entry<Integer, LastCompactionStats> entry) {
                return size() > 100;
            }
        };
        this.mAm = am;
        this.mCompactionThread = new ServiceThread("CompactionThread", -2, true);
        this.mProcStateThrottle = new HashSet();
    }

    @VisibleForTesting
    AppCompactor(ActivityManagerService am, PropertyChangedCallbackForTest callback) {
        this(am);
        this.mTestCallback = callback;
    }

    public void init() {
        DeviceConfig.addOnPropertiesChangedListener("activity_manager", ActivityThread.currentApplication().getMainExecutor(), this.mOnFlagsChangedListener);
        synchronized (this.mPhenotypeFlagLock) {
            updateUseCompaction();
            updateCompactionActions();
            updateCompactionThrottles();
            updateStatsdSampleRate();
            updateFullRssThrottle();
            updateFullDeltaRssThrottle();
            updateProcStateThrottle();
        }
        Process.setThreadGroupAndCpuset(this.mCompactionThread.getThreadId(), 2);
    }

    public boolean useCompaction() {
        boolean z;
        synchronized (this.mPhenotypeFlagLock) {
            z = this.mUseCompaction;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void dump(PrintWriter pw) {
        pw.println("AppCompactor settings");
        synchronized (this.mPhenotypeFlagLock) {
            pw.println("  use_compaction=" + this.mUseCompaction);
            pw.println("  compact_action_1=" + this.mCompactActionSome);
            pw.println("  compact_action_2=" + this.mCompactActionFull);
            pw.println("  compact_throttle_1=" + this.mCompactThrottleSomeSome);
            pw.println("  compact_throttle_2=" + this.mCompactThrottleSomeFull);
            pw.println("  compact_throttle_3=" + this.mCompactThrottleFullSome);
            pw.println("  compact_throttle_4=" + this.mCompactThrottleFullFull);
            pw.println("  compact_throttle_5=" + this.mCompactThrottleBFGS);
            pw.println("  compact_throttle_6=" + this.mCompactThrottlePersistent);
            pw.println("  compact_statsd_sample_rate=" + this.mStatsdSampleRate);
            pw.println("  compact_full_rss_throttle_kb=" + this.mFullAnonRssThrottleKb);
            pw.println("  compact_full_delta_rss_throttle_kb=" + this.mFullDeltaRssThrottleKb);
            pw.println("  compact_proc_state_throttle=" + Arrays.toString(this.mProcStateThrottle.toArray(new Integer[0])));
            pw.println("  " + this.mSomeCompactionCount + " some, " + this.mFullCompactionCount + " full, " + this.mPersistentCompactionCount + " persistent, " + this.mBfgsCompactionCount + " BFGS compactions.");
            StringBuilder sb = new StringBuilder();
            sb.append("  Tracking last compaction stats for ");
            sb.append(this.mLastCompactionStats.size());
            sb.append(" processes.");
            pw.println(sb.toString());
            if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                for (Map.Entry<Integer, LastCompactionStats> entry : this.mLastCompactionStats.entrySet()) {
                    int pid = entry.getKey().intValue();
                    pw.println("    " + pid + ": " + Arrays.toString(entry.getValue().getRssAfterCompaction()));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void compactAppSome(ProcessRecord app) {
        app.reqCompactAction = 1;
        this.mPendingCompactionProcesses.add(app);
        Handler handler = this.mCompactionHandler;
        handler.sendMessage(handler.obtainMessage(1, app.setAdj, app.setProcState));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void compactAppFull(ProcessRecord app) {
        app.reqCompactAction = 2;
        this.mPendingCompactionProcesses.add(app);
        Handler handler = this.mCompactionHandler;
        handler.sendMessage(handler.obtainMessage(1, app.setAdj, app.setProcState));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void compactAppPersistent(ProcessRecord app) {
        app.reqCompactAction = 3;
        this.mPendingCompactionProcesses.add(app);
        Handler handler = this.mCompactionHandler;
        handler.sendMessage(handler.obtainMessage(1, app.curAdj, app.setProcState));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public boolean shouldCompactPersistent(ProcessRecord app, long now) {
        return app.lastCompactTime == 0 || now - app.lastCompactTime > this.mCompactThrottlePersistent;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void compactAppBfgs(ProcessRecord app) {
        app.reqCompactAction = 4;
        this.mPendingCompactionProcesses.add(app);
        Handler handler = this.mCompactionHandler;
        handler.sendMessage(handler.obtainMessage(1, app.curAdj, app.setProcState));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public boolean shouldCompactBFGS(ProcessRecord app, long now) {
        return app.lastCompactTime == 0 || now - app.lastCompactTime > this.mCompactThrottleBFGS;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
    public void compactAllSystem() {
        if (this.mUseCompaction) {
            Handler handler = this.mCompactionHandler;
            handler.sendMessage(handler.obtainMessage(2));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateUseCompaction() {
        this.mUseCompaction = DeviceConfig.getBoolean("activity_manager", KEY_USE_COMPACTION, DEFAULT_USE_COMPACTION.booleanValue());
        if (IS_WATCH) {
            this.mUseCompaction = true;
            Slog.d(ActivityManagerService.TAG, "Set mUseCompaction as true when is watch.");
        }
        if (this.mUseCompaction && !this.mCompactionThread.isAlive()) {
            this.mCompactionThread.start();
            this.mCompactionHandler = new MemCompactionHandler();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateCompactionActions() {
        int compactAction1 = DeviceConfig.getInt("activity_manager", KEY_COMPACT_ACTION_1, 1);
        int compactAction2 = DeviceConfig.getInt("activity_manager", KEY_COMPACT_ACTION_2, 3);
        this.mCompactActionSome = compactActionIntToString(compactAction1);
        this.mCompactActionFull = compactActionIntToString(compactAction2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateCompactionThrottles() {
        boolean useThrottleDefaults = false;
        String throttleSomeSomeFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_1);
        String throttleSomeFullFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_2);
        String throttleFullSomeFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_3);
        String throttleFullFullFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_4);
        String throttleBFGSFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_5);
        String throttlePersistentFlag = DeviceConfig.getProperty("activity_manager", KEY_COMPACT_THROTTLE_6);
        if (TextUtils.isEmpty(throttleSomeSomeFlag) || TextUtils.isEmpty(throttleSomeFullFlag) || TextUtils.isEmpty(throttleFullSomeFlag) || TextUtils.isEmpty(throttleFullFullFlag) || TextUtils.isEmpty(throttleBFGSFlag) || TextUtils.isEmpty(throttlePersistentFlag)) {
            useThrottleDefaults = true;
        } else {
            try {
                this.mCompactThrottleSomeSome = (long) Integer.parseInt(throttleSomeSomeFlag);
                this.mCompactThrottleSomeFull = (long) Integer.parseInt(throttleSomeFullFlag);
                this.mCompactThrottleFullSome = (long) Integer.parseInt(throttleFullSomeFlag);
                this.mCompactThrottleFullFull = (long) Integer.parseInt(throttleFullFullFlag);
                this.mCompactThrottleBFGS = (long) Integer.parseInt(throttleBFGSFlag);
                this.mCompactThrottlePersistent = (long) Integer.parseInt(throttlePersistentFlag);
            } catch (NumberFormatException e) {
                useThrottleDefaults = true;
            }
        }
        if (useThrottleDefaults) {
            this.mCompactThrottleSomeSome = DEFAULT_COMPACT_THROTTLE_1;
            this.mCompactThrottleSomeFull = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
            this.mCompactThrottleFullSome = 500;
            this.mCompactThrottleFullFull = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
            this.mCompactThrottleBFGS = 600000;
            this.mCompactThrottlePersistent = 600000;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateStatsdSampleRate() {
        this.mStatsdSampleRate = DeviceConfig.getFloat("activity_manager", KEY_COMPACT_STATSD_SAMPLE_RATE, (float) DEFAULT_STATSD_SAMPLE_RATE);
        this.mStatsdSampleRate = Math.min(1.0f, Math.max(0.0f, this.mStatsdSampleRate));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateFullRssThrottle() {
        this.mFullAnonRssThrottleKb = DeviceConfig.getLong("activity_manager", KEY_COMPACT_FULL_RSS_THROTTLE_KB, (long) DEFAULT_COMPACT_FULL_RSS_THROTTLE_KB);
        if (this.mFullAnonRssThrottleKb < 0) {
            this.mFullAnonRssThrottleKb = DEFAULT_COMPACT_FULL_RSS_THROTTLE_KB;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateFullDeltaRssThrottle() {
        this.mFullDeltaRssThrottleKb = DeviceConfig.getLong("activity_manager", KEY_COMPACT_FULL_DELTA_RSS_THROTTLE_KB, (long) DEFAULT_COMPACT_FULL_DELTA_RSS_THROTTLE_KB);
        if (this.mFullDeltaRssThrottleKb < 0) {
            this.mFullDeltaRssThrottleKb = DEFAULT_COMPACT_FULL_DELTA_RSS_THROTTLE_KB;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mPhenotypeFlagLock"})
    private void updateProcStateThrottle() {
        String procStateThrottleString = DeviceConfig.getString("activity_manager", KEY_COMPACT_PROC_STATE_THROTTLE, DEFAULT_COMPACT_PROC_STATE_THROTTLE);
        if (!parseProcStateThrottle(procStateThrottleString)) {
            Slog.w(ActivityManagerService.TAG, "Unable to parse app compact proc state throttle \"" + procStateThrottleString + "\" falling back to default.");
            if (!parseProcStateThrottle(DEFAULT_COMPACT_PROC_STATE_THROTTLE)) {
                Slog.wtf(ActivityManagerService.TAG, "Unable to parse default app compact proc state throttle " + DEFAULT_COMPACT_PROC_STATE_THROTTLE);
            }
        }
    }

    private boolean parseProcStateThrottle(String procStateThrottleString) {
        String[] procStates = TextUtils.split(procStateThrottleString, ",");
        this.mProcStateThrottle.clear();
        for (String procState : procStates) {
            try {
                this.mProcStateThrottle.add(Integer.valueOf(Integer.parseInt(procState)));
            } catch (NumberFormatException e) {
                Slog.e(ActivityManagerService.TAG, "Failed to parse default app compaction proc state: " + procState);
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    static String compactActionIntToString(int action) {
        if (action == 1) {
            return COMPACT_ACTION_FILE;
        }
        if (action == 2) {
            return COMPACT_ACTION_ANON;
        }
        if (action != 3) {
            return action != 4 ? "" : "";
        }
        return COMPACT_ACTION_FULL;
    }

    /* access modifiers changed from: private */
    public static final class LastCompactionStats {
        private final long[] mRssAfterCompaction;

        LastCompactionStats(long[] rss) {
            this.mRssAfterCompaction = rss;
        }

        /* access modifiers changed from: package-private */
        public long[] getRssAfterCompaction() {
            return this.mRssAfterCompaction;
        }
    }

    /* access modifiers changed from: private */
    public final class MemCompactionHandler extends Handler {
        private MemCompactionHandler() {
            super(AppCompactor.this.mCompactionThread.getLooper());
        }

        /* JADX INFO: Multiple debug info for r9v23 long: [D('anonRssBefore' long), D('absDelta' long)] */
        /* JADX WARNING: Code restructure failed: missing block: B:212:0x0627, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:217:0x063c, code lost:
            r7 = 64;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d3, code lost:
            if ((r7 - r5) < r61.this$0.mCompactThrottleSomeFull) goto L_0x00d5;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Removed duplicated region for block: B:212:0x0627 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:140:0x0398] */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Throwable th;
            ProcessRecord proc;
            int pendingAction;
            int pid;
            String name;
            int lastCompactAction;
            long lastCompactTime;
            LastCompactionStats lastCompactionStats;
            ProcessRecord proc2;
            int lastOomAdj;
            String action;
            long anonRssBefore;
            int procState;
            MemCompactionHandler memCompactionHandler;
            long start;
            Throwable th2;
            String str;
            String action2;
            long[] rssAfter;
            ProcessRecord proc3;
            int pendingAction2;
            long start2;
            int pid2;
            Throwable th3;
            MemCompactionHandler memCompactionHandler2;
            int i = msg.what;
            if (i == 1) {
                long start3 = SystemClock.uptimeMillis();
                int lastOomAdj2 = msg.arg1;
                int procState2 = msg.arg2;
                synchronized (AppCompactor.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        proc = (ProcessRecord) AppCompactor.this.mPendingCompactionProcesses.remove(0);
                        pendingAction = proc.reqCompactAction;
                        pid = proc.pid;
                        String name2 = proc.processName;
                        if (pendingAction != 1) {
                            if (pendingAction != 2) {
                                name = name2;
                                lastCompactAction = proc.lastCompactAction;
                                lastCompactTime = proc.lastCompactTime;
                                lastCompactionStats = (LastCompactionStats) AppCompactor.this.mLastCompactionStats.remove(Integer.valueOf(pid));
                            }
                        }
                        if (proc.setAdj <= 200) {
                            try {
                                if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping compaction as process " + name2 + " is now perceptible.");
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            } catch (Throwable th4) {
                                th = th4;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th5) {
                                        th = th5;
                                    }
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } else {
                            name = name2;
                            lastCompactAction = proc.lastCompactAction;
                            lastCompactTime = proc.lastCompactTime;
                            lastCompactionStats = (LastCompactionStats) AppCompactor.this.mLastCompactionStats.remove(Integer.valueOf(pid));
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        while (true) {
                            break;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
                if (pid != 0) {
                    if (lastCompactTime == 0) {
                        proc2 = proc;
                        lastOomAdj = lastOomAdj2;
                    } else if (pendingAction == 1) {
                        if (lastCompactAction != 1 || start3 - lastCompactTime >= AppCompactor.this.mCompactThrottleSomeSome) {
                            if (lastCompactAction == 2) {
                                proc2 = proc;
                                lastOomAdj = lastOomAdj2;
                            } else {
                                proc2 = proc;
                                lastOomAdj = lastOomAdj2;
                            }
                        }
                        if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                            Slog.d(ActivityManagerService.TAG, "Skipping some compaction for " + name + ": too soon. throttle=" + AppCompactor.this.mCompactThrottleSomeSome + SliceClientPermissions.SliceAuthority.DELIMITER + AppCompactor.this.mCompactThrottleSomeFull + " last=" + (start3 - lastCompactTime) + "ms ago");
                            return;
                        }
                        return;
                    } else {
                        proc2 = proc;
                        lastOomAdj = lastOomAdj2;
                        if (pendingAction == 2) {
                            if ((lastCompactAction == 1 && start3 - lastCompactTime < AppCompactor.this.mCompactThrottleFullSome) || (lastCompactAction == 2 && start3 - lastCompactTime < AppCompactor.this.mCompactThrottleFullFull)) {
                                if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping full compaction for " + name + ": too soon. throttle=" + AppCompactor.this.mCompactThrottleFullSome + SliceClientPermissions.SliceAuthority.DELIMITER + AppCompactor.this.mCompactThrottleFullFull + " last=" + (start3 - lastCompactTime) + "ms ago");
                                    return;
                                }
                                return;
                            }
                        } else if (pendingAction == 3) {
                            if (start3 - lastCompactTime < AppCompactor.this.mCompactThrottlePersistent) {
                                if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                    Slog.d(ActivityManagerService.TAG, "Skipping persistent compaction for " + name + ": too soon. throttle=" + AppCompactor.this.mCompactThrottlePersistent + " last=" + (start3 - lastCompactTime) + "ms ago");
                                    return;
                                }
                                return;
                            }
                        } else if (pendingAction == 4 && start3 - lastCompactTime < AppCompactor.this.mCompactThrottleBFGS) {
                            if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                Slog.d(ActivityManagerService.TAG, "Skipping bfgs compaction for " + name + ": too soon. throttle=" + AppCompactor.this.mCompactThrottleBFGS + " last=" + (start3 - lastCompactTime) + "ms ago");
                                return;
                            }
                            return;
                        }
                    }
                    if (pendingAction == 1) {
                        action = AppCompactor.this.mCompactActionSome;
                    } else if (pendingAction == 2 || pendingAction == 3 || pendingAction == 4) {
                        action = AppCompactor.this.mCompactActionFull;
                    } else {
                        action = "";
                    }
                    if (!"".equals(action)) {
                        if (!AppCompactor.this.mProcStateThrottle.contains(Integer.valueOf(procState2))) {
                            long[] rssBefore = Process.getRss(pid);
                            long anonRssBefore2 = rssBefore[2];
                            if (rssBefore[0] != 0 || rssBefore[1] != 0 || rssBefore[2] != 0 || rssBefore[3] != 0) {
                                if (action.equals(AppCompactor.COMPACT_ACTION_FULL) || action.equals(AppCompactor.COMPACT_ACTION_ANON)) {
                                    if (AppCompactor.this.mFullAnonRssThrottleKb > 0) {
                                        memCompactionHandler2 = this;
                                        procState = procState2;
                                        if (anonRssBefore2 < AppCompactor.this.mFullAnonRssThrottleKb) {
                                            if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                                Slog.d(ActivityManagerService.TAG, "Skipping full compaction for process " + name + "; anon RSS is too small: " + anonRssBefore2 + "KB.");
                                                return;
                                            }
                                            return;
                                        }
                                    } else {
                                        memCompactionHandler2 = this;
                                        procState = procState2;
                                    }
                                    if (lastCompactionStats == null || AppCompactor.this.mFullDeltaRssThrottleKb <= 0) {
                                        anonRssBefore = anonRssBefore2;
                                    } else {
                                        long[] lastRss = lastCompactionStats.getRssAfterCompaction();
                                        anonRssBefore = anonRssBefore2;
                                        long absDelta = Math.abs(rssBefore[1] - lastRss[1]) + Math.abs(rssBefore[2] - lastRss[2]) + Math.abs(rssBefore[3] - lastRss[3]);
                                        if (absDelta <= AppCompactor.this.mFullDeltaRssThrottleKb) {
                                            if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                                Slog.d(ActivityManagerService.TAG, "Skipping full compaction for process " + name + "; abs delta is too small: " + absDelta + "KB.");
                                                return;
                                            }
                                            return;
                                        }
                                    }
                                } else {
                                    anonRssBefore = anonRssBefore2;
                                    procState = procState2;
                                }
                                if (pendingAction == 1) {
                                    memCompactionHandler = this;
                                    AppCompactor.access$1308(AppCompactor.this);
                                } else if (pendingAction == 2) {
                                    memCompactionHandler = this;
                                    AppCompactor.access$1408(AppCompactor.this);
                                } else if (pendingAction == 3) {
                                    memCompactionHandler = this;
                                    AppCompactor.access$1508(AppCompactor.this);
                                } else if (pendingAction != 4) {
                                    memCompactionHandler = this;
                                } else {
                                    memCompactionHandler = this;
                                    AppCompactor.access$1608(AppCompactor.this);
                                }
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Compact ");
                                    if (pendingAction == 1) {
                                        str = "some";
                                    } else {
                                        str = "full";
                                    }
                                    sb.append(str);
                                    sb.append(": ");
                                    sb.append(name);
                                    Trace.traceBegin(64, sb.toString());
                                    long zramFreeKbBefore = Debug.getZramFreeKb();
                                    FileOutputStream fos = new FileOutputStream("/proc/" + pid + "/reclaim");
                                    fos.write(action.getBytes());
                                    fos.close();
                                    long[] rssAfter2 = Process.getRss(pid);
                                    long end = SystemClock.uptimeMillis();
                                    long time = end - start3;
                                    long zramFreeKbAfter = Debug.getZramFreeKb();
                                    EventLog.writeEvent((int) EventLogTags.AM_COMPACT, Integer.valueOf(pid), name, action, Long.valueOf(rssBefore[0]), Long.valueOf(rssBefore[1]), Long.valueOf(rssBefore[2]), Long.valueOf(rssBefore[3]), Long.valueOf(rssAfter2[0] - rssBefore[0]), Long.valueOf(rssAfter2[1] - rssBefore[1]), Long.valueOf(rssAfter2[2] - rssBefore[2]), Long.valueOf(rssAfter2[3] - rssBefore[3]), Long.valueOf(time), Integer.valueOf(lastCompactAction), Long.valueOf(lastCompactTime), Integer.valueOf(lastOomAdj), Integer.valueOf(procState), Long.valueOf(zramFreeKbBefore), Long.valueOf(zramFreeKbAfter - zramFreeKbBefore));
                                    if (AppCompactor.this.mRandom.nextFloat() < AppCompactor.this.mStatsdSampleRate) {
                                        try {
                                            start2 = end;
                                            pid2 = pid;
                                            pendingAction2 = pendingAction;
                                            rssAfter = rssAfter2;
                                            proc3 = proc2;
                                            action2 = action;
                                        } catch (Exception e) {
                                            start = 64;
                                        } catch (Throwable th7) {
                                            th2 = th7;
                                            Trace.traceEnd(64);
                                            throw th2;
                                        }
                                        try {
                                            StatsLog.write(HdmiCecKeycode.CEC_KEYCODE_F3_GREEN, pid, name, pendingAction, rssBefore[0], rssBefore[1], rssBefore[2], rssBefore[3], rssAfter2[0], rssAfter2[1], rssAfter2[2], rssAfter2[3], time, lastCompactAction, lastCompactTime, lastOomAdj, ActivityManager.processStateAmToProto(procState), zramFreeKbBefore, zramFreeKbAfter);
                                        } catch (Exception e2) {
                                            start = 64;
                                        } catch (Throwable th8) {
                                            th2 = th8;
                                            Trace.traceEnd(64);
                                            throw th2;
                                        }
                                    } else {
                                        start2 = end;
                                        pid2 = pid;
                                        pendingAction2 = pendingAction;
                                        rssAfter = rssAfter2;
                                        action2 = action;
                                        proc3 = proc2;
                                    }
                                    try {
                                        synchronized (AppCompactor.this.mAm) {
                                            try {
                                                ActivityManagerService.boostPriorityForLockedSection();
                                                try {
                                                    proc3.lastCompactTime = start2;
                                                    try {
                                                        proc3.lastCompactAction = pendingAction2;
                                                    } catch (Throwable th9) {
                                                        th3 = th9;
                                                        while (true) {
                                                            try {
                                                                break;
                                                            } catch (Throwable th10) {
                                                                th3 = th10;
                                                            }
                                                        }
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th3;
                                                    }
                                                } catch (Throwable th11) {
                                                    th3 = th11;
                                                    while (true) {
                                                        break;
                                                    }
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th3;
                                                }
                                            } catch (Throwable th12) {
                                                th3 = th12;
                                                while (true) {
                                                    break;
                                                }
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                throw th3;
                                            }
                                        }
                                    } catch (Exception e3) {
                                        start = 64;
                                    } catch (Throwable th13) {
                                        th2 = th13;
                                        Trace.traceEnd(64);
                                        throw th2;
                                    }
                                    try {
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        try {
                                            if (action2.equals(AppCompactor.COMPACT_ACTION_FULL) || action2.equals(AppCompactor.COMPACT_ACTION_ANON)) {
                                                AppCompactor.this.mLastCompactionStats.put(Integer.valueOf(pid2), new LastCompactionStats(rssAfter));
                                            }
                                            start = 64;
                                        } catch (Exception e4) {
                                            start = 64;
                                        } catch (Throwable th14) {
                                            th2 = th14;
                                            Trace.traceEnd(64);
                                            throw th2;
                                        }
                                    } catch (Exception e5) {
                                        start = 64;
                                    } catch (Throwable th15) {
                                        th2 = th15;
                                        Trace.traceEnd(64);
                                        throw th2;
                                    }
                                } catch (Exception e6) {
                                    start = 64;
                                } catch (Throwable th16) {
                                }
                                Trace.traceEnd(start);
                            } else if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                                Slog.d(ActivityManagerService.TAG, "Skipping compaction forprocess " + pid + " with no memory usage. Dead?");
                            }
                        } else if (ActivityManagerDebugConfig.DEBUG_COMPACTION) {
                            Slog.d(ActivityManagerService.TAG, "Skipping full compaction for process " + name + "; proc state is " + procState2);
                        }
                    }
                }
            } else if (i == 2) {
                Trace.traceBegin(64, "compactSystem");
                AppCompactor.this.compactSystem();
                Trace.traceEnd(64);
            }
        }
    }
}
