package com.android.server.am;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.dex.ArtManagerInternal;
import android.content.pm.dex.PackageOptimizationInfo;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.StatsLog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.server.LocalServices;
import com.android.server.am.ActivityMetricsLogger;
import com.android.server.am.MemoryStatUtil;
import java.util.ArrayList;

class ActivityMetricsLogger {
    private static final long INVALID_START_TIME = -1;
    private static final int MSG_CHECK_VISIBILITY = 0;
    private static final String TAG = "ActivityManager";
    private static final String[] TRON_WINDOW_STATE_VARZ_STRINGS = {"window_time_0", "window_time_1", "window_time_2", "window_time_3"};
    private static final int WINDOW_STATE_ASSISTANT = 3;
    private static final int WINDOW_STATE_FREEFORM = 2;
    private static final int WINDOW_STATE_INVALID = -1;
    private static final int WINDOW_STATE_SIDE_BY_SIDE = 1;
    private static final int WINDOW_STATE_STANDARD = 0;
    private static boolean sIsMygote = (System.getenv("MAPLE_RUNTIME") != null);
    private ArtManagerInternal mArtManagerInternal;
    private final Context mContext;
    private int mCurrentTransitionDelayMs;
    private int mCurrentTransitionDeviceUptime;
    private long mCurrentTransitionStartTime = -1;
    private final H mHandler;
    private long mLastLogTimeSecs = (SystemClock.elapsedRealtime() / 1000);
    private long mLastTransitionStartTime = -1;
    private final SparseArray<WindowingModeTransitionInfo> mLastWindowingModeTransitionInfo = new SparseArray<>();
    private boolean mLoggedTransitionStarting;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final ActivityStackSupervisor mSupervisor;
    private int mWindowState = 0;
    private final SparseArray<WindowingModeTransitionInfo> mWindowingModeTransitionInfo = new SparseArray<>();

    private final class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                SomeArgs args = (SomeArgs) msg.obj;
                ActivityMetricsLogger.this.checkVisibility((TaskRecord) args.arg1, (ActivityRecord) args.arg2);
            }
        }
    }

    private final class WindowingModeTransitionInfo {
        /* access modifiers changed from: private */
        public int bindApplicationDelayMs;
        /* access modifiers changed from: private */
        public boolean currentTransitionProcessRunning;
        /* access modifiers changed from: private */
        public ActivityRecord launchedActivity;
        /* access modifiers changed from: private */
        public boolean loggedStartingWindowDrawn;
        /* access modifiers changed from: private */
        public boolean loggedWindowsDrawn;
        /* access modifiers changed from: private */
        public int reason;
        /* access modifiers changed from: private */
        public int startResult;
        /* access modifiers changed from: private */
        public int startingWindowDelayMs;
        /* access modifiers changed from: private */
        public int windowsDrawnDelayMs;

        private WindowingModeTransitionInfo() {
            this.startingWindowDelayMs = -1;
            this.bindApplicationDelayMs = -1;
            this.reason = 3;
        }
    }

    private final class WindowingModeTransitionInfoSnapshot {
        /* access modifiers changed from: private */
        public final ApplicationInfo applicationInfo;
        /* access modifiers changed from: private */
        public final int bindApplicationDelayMs;
        /* access modifiers changed from: private */
        public final String launchedActivityAppRecordRequiredAbi;
        /* access modifiers changed from: private */
        public final String launchedActivityLaunchToken;
        /* access modifiers changed from: private */
        public final String launchedActivityLaunchedFromPackage;
        /* access modifiers changed from: private */
        public final String launchedActivityName;
        /* access modifiers changed from: private */
        public final String packageName;
        /* access modifiers changed from: private */
        public final String processName;
        /* access modifiers changed from: private */
        public final ProcessRecord processRecord;
        /* access modifiers changed from: private */
        public final int reason;
        /* access modifiers changed from: private */
        public final int startingWindowDelayMs;
        /* access modifiers changed from: private */
        public final int type;
        /* access modifiers changed from: private */
        public final int windowsDrawnDelayMs;

        private WindowingModeTransitionInfoSnapshot(WindowingModeTransitionInfo info) {
            String str;
            this.applicationInfo = info.launchedActivity.appInfo;
            this.packageName = info.launchedActivity.packageName;
            this.launchedActivityName = info.launchedActivity.info.name;
            this.launchedActivityLaunchedFromPackage = info.launchedActivity.launchedFromPackage;
            this.launchedActivityLaunchToken = info.launchedActivity.info.launchToken;
            if (info.launchedActivity.app == null) {
                str = null;
            } else {
                str = info.launchedActivity.app.requiredAbi;
            }
            this.launchedActivityAppRecordRequiredAbi = str;
            this.reason = info.reason;
            this.startingWindowDelayMs = info.startingWindowDelayMs;
            this.bindApplicationDelayMs = info.bindApplicationDelayMs;
            this.windowsDrawnDelayMs = info.windowsDrawnDelayMs;
            this.type = ActivityMetricsLogger.this.getTransitionType(info);
            this.processRecord = ActivityMetricsLogger.this.findProcessForActivity(info.launchedActivity);
            this.processName = info.launchedActivity.processName;
        }
    }

    ActivityMetricsLogger(ActivityStackSupervisor supervisor, Context context, Looper looper) {
        this.mSupervisor = supervisor;
        this.mContext = context;
        this.mHandler = new H(looper);
    }

    /* access modifiers changed from: package-private */
    public void logWindowState() {
        long now = SystemClock.elapsedRealtime() / 1000;
        if (this.mWindowState != -1) {
            MetricsLogger.count(this.mContext, TRON_WINDOW_STATE_VARZ_STRINGS[this.mWindowState], (int) (now - this.mLastLogTimeSecs));
        }
        this.mLastLogTimeSecs = now;
        this.mWindowState = -1;
        ActivityStack stack = this.mSupervisor.getFocusedStack();
        if (stack.isActivityTypeAssistant()) {
            this.mWindowState = 3;
            return;
        }
        int windowingMode = stack.getWindowingMode();
        if (windowingMode == 2) {
            stack = this.mSupervisor.findStackBehind(stack);
            windowingMode = stack.getWindowingMode();
        }
        if (windowingMode != 1) {
            if (windowingMode != 10) {
                switch (windowingMode) {
                    case 3:
                    case 4:
                        this.mWindowState = 1;
                        break;
                    case 5:
                        break;
                    default:
                        if (windowingMode != 0) {
                            throw new IllegalStateException("Unknown windowing mode for stack=" + stack + " windowingMode=" + windowingMode);
                        }
                        break;
                }
            }
            this.mWindowState = 2;
        } else {
            this.mWindowState = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityLaunching() {
        if (!isAnyTransitionActive()) {
            if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                Slog.i("ActivityManager", "notifyActivityLaunching");
            }
            this.mCurrentTransitionStartTime = SystemClock.uptimeMillis();
            this.mLastTransitionStartTime = this.mCurrentTransitionStartTime;
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity) {
        ProcessRecord processRecord = findProcessForActivity(launchedActivity);
        boolean processSwitch = false;
        boolean processRunning = processRecord != null;
        if (processRecord == null || !hasStartedActivity(processRecord, launchedActivity)) {
            processSwitch = true;
        }
        notifyActivityLaunched(resultCode, launchedActivity, processRunning, processSwitch);
    }

    private boolean hasStartedActivity(ProcessRecord record, ActivityRecord launchedActivity) {
        ArrayList<ActivityRecord> activities = record.activities;
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activity = activities.get(i);
            if (launchedActivity != activity && !activity.stopped) {
                return true;
            }
        }
        return false;
    }

    private void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity, boolean processRunning, boolean processSwitch) {
        int windowingMode;
        if (ActivityManagerDebugConfig.DEBUG_METRICS) {
            Slog.i("ActivityManager", "notifyActivityLaunched resultCode=" + resultCode + " launchedActivity=" + launchedActivity + " processRunning=" + processRunning + " processSwitch=" + processSwitch);
        }
        boolean otherWindowModesLaunching = false;
        if (launchedActivity != null) {
            windowingMode = launchedActivity.getWindowingMode();
        } else {
            windowingMode = 0;
        }
        if (this.mCurrentTransitionStartTime != -1) {
            WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(windowingMode);
            if (launchedActivity == null || info == null) {
                if (this.mWindowingModeTransitionInfo.size() > 0 && info == null) {
                    otherWindowModesLaunching = true;
                }
                if ((!isLoggableResultCode(resultCode) || launchedActivity == null || !processSwitch || windowingMode == 0) && !otherWindowModesLaunching) {
                    reset(true);
                } else if (!otherWindowModesLaunching) {
                    if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                        Slog.i("ActivityManager", "notifyActivityLaunched successful");
                    }
                    WindowingModeTransitionInfo newInfo = new WindowingModeTransitionInfo();
                    ActivityRecord unused = newInfo.launchedActivity = launchedActivity;
                    boolean unused2 = newInfo.currentTransitionProcessRunning = processRunning;
                    int unused3 = newInfo.startResult = resultCode;
                    this.mWindowingModeTransitionInfo.put(windowingMode, newInfo);
                    this.mLastWindowingModeTransitionInfo.put(windowingMode, newInfo);
                    this.mCurrentTransitionDeviceUptime = (int) (SystemClock.uptimeMillis() / 1000);
                }
            } else {
                ActivityRecord unused4 = info.launchedActivity = launchedActivity;
            }
        }
    }

    private boolean isLoggableResultCode(int resultCode) {
        return resultCode == 0 || resultCode == 2;
    }

    /* access modifiers changed from: package-private */
    public void notifyWindowsDrawn(int windowingMode, long timestamp) {
        if (ActivityManagerDebugConfig.DEBUG_METRICS) {
            Slog.i("ActivityManager", "notifyWindowsDrawn windowingMode=" + windowingMode);
        }
        WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(windowingMode);
        if (info != null && !info.loggedWindowsDrawn) {
            int unused = info.windowsDrawnDelayMs = calculateDelay(timestamp);
            boolean unused2 = info.loggedWindowsDrawn = true;
            if (allWindowsDrawn() && this.mLoggedTransitionStarting) {
                reset(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyStartingWindowDrawn(int windowingMode, long timestamp) {
        WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(windowingMode);
        if (info != null && !info.loggedStartingWindowDrawn) {
            boolean unused = info.loggedStartingWindowDrawn = true;
            int unused2 = info.startingWindowDelayMs = calculateDelay(timestamp);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyTransitionStarting(SparseIntArray windowingModeToReason, long timestamp) {
        if (isAnyTransitionActive() && !this.mLoggedTransitionStarting) {
            if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                Slog.i("ActivityManager", "notifyTransitionStarting");
            }
            this.mCurrentTransitionDelayMs = calculateDelay(timestamp);
            this.mLoggedTransitionStarting = true;
            int windowingMode = windowingModeToReason.size() - 1;
            while (true) {
                int index = windowingMode;
                if (index < 0) {
                    break;
                }
                WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(windowingModeToReason.keyAt(index));
                if (info != null) {
                    int unused = info.reason = windowingModeToReason.valueAt(index);
                }
                windowingMode = index - 1;
            }
            if (allWindowsDrawn()) {
                reset(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyVisibilityChanged(ActivityRecord activityRecord) {
        WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(activityRecord.getWindowingMode());
        if (info != null && info.launchedActivity == activityRecord) {
            TaskRecord t = activityRecord.getTask();
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = t;
            args.arg2 = activityRecord;
            this.mHandler.obtainMessage(0, args).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void checkVisibility(TaskRecord t, ActivityRecord r) {
        synchronized (this.mSupervisor.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.get(r.getWindowingMode());
                if (info != null && !t.isVisible()) {
                    if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                        Slog.i("ActivityManager", "notifyVisibilityChanged to invisible activity=" + r);
                    }
                    logAppTransitionCancel(info);
                    this.mWindowingModeTransitionInfo.remove(r.getWindowingMode());
                    if (this.mWindowingModeTransitionInfo.size() == 0) {
                        reset(true);
                    }
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void notifyBindApplication(ProcessRecord app) {
        for (int i = this.mWindowingModeTransitionInfo.size() - 1; i >= 0; i--) {
            WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.valueAt(i);
            if (info.launchedActivity.appInfo == app.info) {
                int unused = info.bindApplicationDelayMs = calculateCurrentDelay();
            }
        }
    }

    private boolean allWindowsDrawn() {
        for (int index = this.mWindowingModeTransitionInfo.size() - 1; index >= 0; index--) {
            if (!this.mWindowingModeTransitionInfo.valueAt(index).loggedWindowsDrawn) {
                return false;
            }
        }
        return true;
    }

    private boolean isAnyTransitionActive() {
        return this.mCurrentTransitionStartTime != -1 && this.mWindowingModeTransitionInfo.size() > 0;
    }

    private void reset(boolean abort) {
        if (ActivityManagerDebugConfig.DEBUG_METRICS) {
            Slog.i("ActivityManager", "reset abort=" + abort);
        }
        if (!abort && isAnyTransitionActive()) {
            logAppTransitionMultiEvents();
        }
        this.mCurrentTransitionStartTime = -1;
        this.mCurrentTransitionDelayMs = -1;
        this.mLoggedTransitionStarting = false;
        this.mWindowingModeTransitionInfo.clear();
    }

    private int calculateCurrentDelay() {
        return (int) (SystemClock.uptimeMillis() - this.mCurrentTransitionStartTime);
    }

    private int calculateDelay(long timestamp) {
        return (int) (timestamp - this.mCurrentTransitionStartTime);
    }

    private void logAppTransitionCancel(WindowingModeTransitionInfo info) {
        int type = getTransitionType(info);
        if (type != -1) {
            LogMaker builder = new LogMaker(1144);
            builder.setPackageName(info.launchedActivity.packageName);
            builder.setType(type);
            builder.addTaggedData(871, info.launchedActivity.info.name);
            this.mMetricsLogger.write(builder);
            StatsLog.write(49, info.launchedActivity.appInfo.uid, info.launchedActivity.packageName, convertAppStartTransitionType(type), info.launchedActivity.info.name);
        }
    }

    private void logAppTransitionMultiEvents() {
        if (ActivityManagerDebugConfig.DEBUG_METRICS) {
            Slog.i("ActivityManager", "logging transition events");
        }
        int index = this.mWindowingModeTransitionInfo.size() - 1;
        while (index >= 0) {
            WindowingModeTransitionInfo info = this.mWindowingModeTransitionInfo.valueAt(index);
            if (getTransitionType(info) != -1) {
                BackgroundThread.getHandler().post(new Runnable(this.mCurrentTransitionDeviceUptime, this.mCurrentTransitionDelayMs, new WindowingModeTransitionInfoSnapshot(info)) {
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;
                    private final /* synthetic */ ActivityMetricsLogger.WindowingModeTransitionInfoSnapshot f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        ActivityMetricsLogger.this.logAppTransition(this.f$1, this.f$2, this.f$3);
                    }
                });
                info.launchedActivity.info.launchToken = null;
                index--;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void logAppTransition(int currentTransitionDeviceUptime, int currentTransitionDelayMs, WindowingModeTransitionInfoSnapshot info) {
        PackageOptimizationInfo packageOptimizationInfo;
        LogMaker builder = new LogMaker(761);
        builder.setPackageName(info.packageName);
        builder.setType(info.type);
        builder.addTaggedData(871, info.launchedActivityName);
        boolean isInstantApp = info.applicationInfo.isInstantApp();
        if (info.launchedActivityLaunchedFromPackage != null) {
            builder.addTaggedData(904, info.launchedActivityLaunchedFromPackage);
        }
        String launchToken = info.launchedActivityLaunchToken;
        if (launchToken != null) {
            builder.addTaggedData(903, launchToken);
        }
        builder.addTaggedData(905, Integer.valueOf(isInstantApp));
        builder.addTaggedData(325, Integer.valueOf(currentTransitionDeviceUptime));
        builder.addTaggedData(319, Integer.valueOf(currentTransitionDelayMs));
        builder.setSubtype(info.reason);
        if (info.startingWindowDelayMs != -1) {
            builder.addTaggedData(321, Integer.valueOf(info.startingWindowDelayMs));
        }
        if (info.bindApplicationDelayMs != -1) {
            builder.addTaggedData(945, Integer.valueOf(info.bindApplicationDelayMs));
        }
        builder.addTaggedData(322, Integer.valueOf(info.windowsDrawnDelayMs));
        ArtManagerInternal artManagerInternal = sIsMygote ? null : getArtManagerInternal();
        if (artManagerInternal == null || info.launchedActivityAppRecordRequiredAbi == null) {
            packageOptimizationInfo = PackageOptimizationInfo.createWithNoInfo();
        } else {
            packageOptimizationInfo = artManagerInternal.getPackageOptimizationInfo(info.applicationInfo, info.launchedActivityAppRecordRequiredAbi);
        }
        PackageOptimizationInfo packageOptimizationInfo2 = packageOptimizationInfo;
        builder.addTaggedData(1321, Integer.valueOf(packageOptimizationInfo2.getCompilationReason()));
        builder.addTaggedData(1320, Integer.valueOf(packageOptimizationInfo2.getCompilationFilter()));
        this.mMetricsLogger.write(builder);
        PackageOptimizationInfo packageOptimizationInfo3 = packageOptimizationInfo2;
        ArtManagerInternal artManagerInternal2 = artManagerInternal;
        StatsLog.write(48, info.applicationInfo.uid, info.packageName, convertAppStartTransitionType(info.type), info.launchedActivityName, info.launchedActivityLaunchedFromPackage, isInstantApp, (long) (currentTransitionDeviceUptime * 1000), info.reason, currentTransitionDelayMs, info.startingWindowDelayMs, info.bindApplicationDelayMs, info.windowsDrawnDelayMs, launchToken, packageOptimizationInfo2.getCompilationReason(), packageOptimizationInfo2.getCompilationFilter());
        logAppStartMemoryStateCapture(info);
    }

    private int convertAppStartTransitionType(int tronType) {
        if (tronType == 7) {
            return 3;
        }
        if (tronType == 8) {
            return 1;
        }
        if (tronType == 9) {
            return 2;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void logAppTransitionReportedDrawn(ActivityRecord r, boolean restoredFromBundle) {
        int i;
        int i2;
        ActivityRecord activityRecord = r;
        WindowingModeTransitionInfo info = this.mLastWindowingModeTransitionInfo.get(r.getWindowingMode());
        if (info != null) {
            LogMaker builder = new LogMaker(1090);
            builder.setPackageName(activityRecord.packageName);
            builder.addTaggedData(871, activityRecord.info.name);
            long startupTimeMs = SystemClock.uptimeMillis() - this.mLastTransitionStartTime;
            builder.addTaggedData(1091, Long.valueOf(startupTimeMs));
            if (restoredFromBundle) {
                i = 13;
            } else {
                i = 12;
            }
            builder.setType(i);
            builder.addTaggedData(324, Integer.valueOf(info.currentTransitionProcessRunning ? 1 : 0));
            this.mMetricsLogger.write(builder);
            int i3 = info.launchedActivity.appInfo.uid;
            String str = info.launchedActivity.packageName;
            if (restoredFromBundle) {
                i2 = 1;
            } else {
                i2 = 2;
            }
            StatsLog.write(50, i3, str, i2, info.launchedActivity.info.name, info.currentTransitionProcessRunning, startupTimeMs);
        }
    }

    /* access modifiers changed from: private */
    public int getTransitionType(WindowingModeTransitionInfo info) {
        if (info.currentTransitionProcessRunning) {
            if (info.startResult == 0) {
                return 8;
            }
            if (info.startResult == 2) {
                return 9;
            }
        } else if (info.startResult == 0) {
            return 7;
        }
        return -1;
    }

    private void logAppStartMemoryStateCapture(WindowingModeTransitionInfoSnapshot info) {
        if (info.processRecord == null) {
            if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                Slog.i("ActivityManager", "logAppStartMemoryStateCapture processRecord null");
            }
            return;
        }
        int pid = info.processRecord.pid;
        int uid = info.applicationInfo.uid;
        MemoryStatUtil.MemoryStat memoryStat = MemoryStatUtil.readMemoryStatFromFilesystem(uid, pid);
        if (memoryStat == null) {
            if (ActivityManagerDebugConfig.DEBUG_METRICS) {
                Slog.i("ActivityManager", "logAppStartMemoryStateCapture memoryStat null");
            }
            return;
        }
        String access$2600 = info.processName;
        String access$1600 = info.launchedActivityName;
        long j = memoryStat.pgfault;
        long j2 = memoryStat.pgmajfault;
        long j3 = memoryStat.rssInBytes;
        long j4 = memoryStat.cacheInBytes;
        long j5 = j4;
        MemoryStatUtil.MemoryStat memoryStat2 = memoryStat;
        StatsLog.write(55, uid, access$2600, access$1600, j, j2, j3, j5, memoryStat.swapInBytes);
    }

    /* access modifiers changed from: private */
    public ProcessRecord findProcessForActivity(ActivityRecord launchedActivity) {
        if (launchedActivity != null) {
            return (ProcessRecord) this.mSupervisor.mService.mProcessNames.get(launchedActivity.processName, launchedActivity.appInfo.uid);
        }
        return null;
    }

    private ArtManagerInternal getArtManagerInternal() {
        if (this.mArtManagerInternal == null) {
            this.mArtManagerInternal = (ArtManagerInternal) LocalServices.getService(ArtManagerInternal.class);
        }
        return this.mArtManagerInternal;
    }
}
