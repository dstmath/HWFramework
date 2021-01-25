package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener;
import android.app.IAlarmListener;
import android.app.IAlarmManager;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelableException;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.ThreadLocalWorkSource;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.system.Os;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwLog;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.LongArrayQueue;
import android.util.MutableBoolean;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.LocalLog;
import com.android.internal.util.StatLogger;
import com.android.server.AlarmManagerInternal;
import com.android.server.AlarmManagerService;
import com.android.server.AppStateTracker;
import com.android.server.DeviceIdleController;
import com.android.server.am.HwActivityManagerServiceUtil;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.AppStandbyController;
import com.android.server.utils.PriorityDump;
import com.huawei.android.app.IHwAlarmManager;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.function.Predicate;

public class AlarmManagerService extends AbsAlarmManagerService {
    static final int ACTIVE_INDEX = 0;
    static final boolean DEBUG_ALARM_CLOCK = false;
    static final boolean DEBUG_BATCH = false;
    static final boolean DEBUG_BG_LIMIT = false;
    static final boolean DEBUG_LISTENER_CALLBACK = false;
    static final boolean DEBUG_STANDBY = false;
    static final boolean DEBUG_VALIDATE = false;
    static final boolean DEBUG_WAKELOCK = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int FREQUENT_INDEX = 2;
    private static final long INVALID_VALUE = -1;
    static final int IS_WAKEUP_MASK = 5;
    static final long MILLIS_IN_DAY = 86400000;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    static final int NEVER_INDEX = 4;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = new Intent("android.app.action.NEXT_ALARM_CLOCK_CHANGED").addFlags(553648128);
    private static final String NORMAL_MODE = "normal";
    static final int PRIO_NORMAL = 2;
    static final int PRIO_TICK = 0;
    static final int PRIO_WAKEUP = 1;
    static final int RARE_INDEX = 3;
    static final boolean RECORD_ALARMS_IN_HISTORY = true;
    static final boolean RECORD_DEVICE_IDLE_ALARMS = false;
    private static final int RTC_MASK = 2;
    private static final int RTC_WAKEUP_MASK = 1;
    private static final String SYSTEM_UI_SELF_PERMISSION = "android.permission.systemui.IDENTITY";
    static final String TAG = "AlarmManager";
    static final int TICK_HISTORY_DEPTH = 10;
    static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    static final int TIME_CHANGED_MASK = 65536;
    static final int TYPE_NONWAKEUP_MASK = 1;
    static final boolean WAKEUP_STATS = false;
    static final int WORKING_INDEX = 1;
    static final boolean localLOGV = false;
    static final BatchTimeOrder sBatchOrder = new BatchTimeOrder();
    static final IncreasingTimeOrder sIncreasingTimeOrder = new IncreasingTimeOrder();
    final long RECENT_WAKEUP_PERIOD;
    final ArrayList<Batch> mAlarmBatches;
    final Comparator<Alarm> mAlarmDispatchComparator;
    SparseIntArray mAlarmsPerUid;
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches;
    AppOpsManager mAppOps;
    private boolean mAppStandbyParole;
    private AppStateTracker mAppStateTracker;
    AppWakeupHistory mAppWakeupHistory;
    private final Intent mBackgroundIntent;
    int mBroadcastRefCount;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats;
    ClockReceiver mClockReceiver;
    Constants mConstants;
    int mCurrentSeq;
    PendingIntent mDateChangeSender;
    final DeliveryTracker mDeliveryTracker;
    private final AppStateTracker.Listener mForceAppStandbyListener;
    AlarmHandler mHandler;
    private final SparseArray<AlarmManager.AlarmClockInfo> mHandlerSparseAlarmClockArray;
    HwInnerAlarmManagerService mHwInnerService;
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight;
    private final ArrayList<AlarmManagerInternal.InFlightListener> mInFlightListeners;
    protected final Injector mInjector;
    boolean mInteractive;
    private final boolean mIsAlarmDataOnlyMode;
    boolean mIsCancelRemoveAction;
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch;
    private long mLastTickAdded;
    private long mLastTickReceived;
    private long mLastTickRemoved;
    private long mLastTickSet;
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    private long mLastTrigger;
    private long mLastWakeup;
    @GuardedBy({"mLock"})
    private int mListenerCount;
    @GuardedBy({"mLock"})
    private int mListenerFinishCount;
    DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock;
    final LocalLog mLog;
    long mMaxDelayTime;
    private final SparseArray<AlarmManager.AlarmClockInfo> mNextAlarmClockForUser;
    protected boolean mNextAlarmClockMayChange;
    private long mNextNonWakeUpSetAt;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    private int mNextTickHistory;
    Alarm mNextWakeFromIdle;
    private long mNextWakeUpSetAt;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms;
    int mNumTimeChanged;
    PendingIntent.CancelListener mOperationCancelListener;
    SparseArray<ArrayList<Alarm>> mPendingBackgroundAlarms;
    Alarm mPendingIdleUntil;
    ArrayList<Alarm> mPendingNonWakeupAlarms;
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser;
    ArrayList<Alarm> mPendingWhileIdleAlarms;
    final HashMap<String, PriorityClass> mPriorities;
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups;
    @GuardedBy({"mLock"})
    private int mSendCount;
    @GuardedBy({"mLock"})
    private int mSendFinishCount;
    private final IBinder mService;
    long mStartCurrentDelayTime;
    private final StatLogger mStatLogger;
    int mSystemUiUid;
    private final long[] mTickHistory;
    Intent mTimeTickIntent;
    IAlarmListener mTimeTickTrigger;
    private final SparseArray<AlarmManager.AlarmClockInfo> mTmpSparseAlarmClockArray;
    long mTotalDelayTime;
    private UsageStatsManagerInternal mUsageStatsManagerInternal;
    final SparseBooleanArray mUseAllowWhileIdleShortTime;
    PowerManager.WakeLock mWakeLock;

    interface Stats {
        public static final int REBATCH_ALL_ALARMS = 0;
        public static final int REORDER_ALARMS_FOR_STANDBY = 1;
    }

    /* access modifiers changed from: private */
    public static native void close(long j);

    /* access modifiers changed from: private */
    public static native long getNextAlarm(long j, int i);

    protected static native void hwSetClockRTC(long j, long j2, long j3);

    /* access modifiers changed from: private */
    public static native long init();

    public static /* synthetic */ boolean lambda$nSJw2tKfoL3YIrKDtszoL44jcSM(AlarmManagerService alarmManagerService, Alarm alarm) {
        return alarmManagerService.isBackgroundRestricted(alarm);
    }

    /* access modifiers changed from: private */
    public static native int set(long j, int i, long j2, long j3);

    /* access modifiers changed from: private */
    public static native int setKernelTime(long j, long j2);

    /* access modifiers changed from: private */
    public static native int setKernelTimezone(long j, int i);

    /* access modifiers changed from: private */
    public static native int waitForAlarm(long j);

    static /* synthetic */ int access$2608(AlarmManagerService x0) {
        int i = x0.mListenerFinishCount;
        x0.mListenerFinishCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$2708(AlarmManagerService x0) {
        int i = x0.mSendFinishCount;
        x0.mSendFinishCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$3008(AlarmManagerService x0) {
        int i = x0.mSendCount;
        x0.mSendCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$3108(AlarmManagerService x0) {
        int i = x0.mListenerCount;
        x0.mListenerCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$3308(AlarmManagerService x0) {
        int i = x0.mNextTickHistory;
        x0.mNextTickHistory = i + 1;
        return i;
    }

    static final class IdleDispatchEntry {
        long argRealtime;
        long elapsedRealtime;
        String op;
        String pkg;
        String tag;
        int uid;

        IdleDispatchEntry() {
        }
    }

    /* access modifiers changed from: private */
    public static class AppWakeupHistory {
        private ArrayMap<Pair<String, Integer>, LongArrayQueue> mPackageHistory = new ArrayMap<>();
        private long mWindowSize;

        AppWakeupHistory(long windowSize) {
            this.mWindowSize = windowSize;
        }

        /* access modifiers changed from: package-private */
        public void recordAlarmForPackage(String packageName, int userId, long nowElapsed) {
            Pair<String, Integer> packageUser = Pair.create(packageName, Integer.valueOf(userId));
            LongArrayQueue history = this.mPackageHistory.get(packageUser);
            if (history == null) {
                history = new LongArrayQueue();
                this.mPackageHistory.put(packageUser, history);
            }
            if (history.size() == 0 || history.peekLast() < nowElapsed) {
                history.addLast(nowElapsed);
            }
            snapToWindow(history);
        }

        /* access modifiers changed from: package-private */
        public void removeForUser(int userId) {
            for (int i = this.mPackageHistory.size() - 1; i >= 0; i--) {
                if (((Integer) this.mPackageHistory.keyAt(i).second).intValue() == userId) {
                    this.mPackageHistory.removeAt(i);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeForPackage(String packageName, int userId) {
            this.mPackageHistory.remove(Pair.create(packageName, Integer.valueOf(userId)));
        }

        private void snapToWindow(LongArrayQueue history) {
            while (history.peekFirst() + this.mWindowSize < history.peekLast()) {
                history.removeFirst();
            }
        }

        /* access modifiers changed from: package-private */
        public int getTotalWakeupsInWindow(String packageName, int userId) {
            LongArrayQueue history = this.mPackageHistory.get(Pair.create(packageName, Integer.valueOf(userId)));
            if (history == null) {
                return 0;
            }
            return history.size();
        }

        /* access modifiers changed from: package-private */
        public long getLastWakeupForPackage(String packageName, int userId, int positionFromEnd) {
            int i;
            LongArrayQueue history = this.mPackageHistory.get(Pair.create(packageName, Integer.valueOf(userId)));
            if (history != null && (i = history.size() - positionFromEnd) >= 0) {
                return history.get(i);
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String prefix, long nowElapsed) {
            dump(new IndentingPrintWriter(pw, "  ").setIndent(prefix), nowElapsed);
        }

        /* access modifiers changed from: package-private */
        public void dump(IndentingPrintWriter pw, long nowElapsed) {
            pw.println("App Alarm history:");
            pw.increaseIndent();
            for (int i = 0; i < this.mPackageHistory.size(); i++) {
                Pair<String, Integer> packageUser = this.mPackageHistory.keyAt(i);
                LongArrayQueue timestamps = this.mPackageHistory.valueAt(i);
                pw.print((String) packageUser.first);
                pw.print(", u");
                pw.print(packageUser.second);
                pw.print(": ");
                int lastIdx = Math.max(0, timestamps.size() - 100);
                for (int j = timestamps.size() - 1; j >= lastIdx; j--) {
                    TimeUtils.formatDuration(timestamps.get(j), nowElapsed, pw);
                    pw.print(", ");
                }
                pw.println();
            }
            pw.decreaseIndent();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class Constants extends ContentObserver {
        private static final long DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME = 540000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        private static final boolean DEFAULT_APP_STANDBY_QUOTAS_ENABLED = true;
        private static final long DEFAULT_APP_STANDBY_WINDOW = 3600000;
        private static final long DEFAULT_LISTENER_TIMEOUT = 5000;
        private static final int DEFAULT_MAX_ALARMS_PER_UID = 500;
        private static final long DEFAULT_MAX_INTERVAL = 31536000000L;
        private static final long DEFAULT_MIN_FUTURITY = 5000;
        private static final long DEFAULT_MIN_INTERVAL = 60000;
        @VisibleForTesting
        static final String KEY_ALLOW_WHILE_IDLE_LONG_TIME = "allow_while_idle_long_time";
        @VisibleForTesting
        static final String KEY_ALLOW_WHILE_IDLE_SHORT_TIME = "allow_while_idle_short_time";
        @VisibleForTesting
        static final String KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION = "allow_while_idle_whitelist_duration";
        @VisibleForTesting
        static final String KEY_APP_STANDBY_QUOTAS_ENABLED = "app_standby_quotas_enabled";
        private static final String KEY_APP_STANDBY_WINDOW = "app_standby_window";
        @VisibleForTesting
        static final String KEY_LISTENER_TIMEOUT = "listener_timeout";
        @VisibleForTesting
        static final String KEY_MAX_ALARMS_PER_UID = "max_alarms_per_uid";
        @VisibleForTesting
        static final String KEY_MAX_INTERVAL = "max_interval";
        @VisibleForTesting
        static final String KEY_MIN_FUTURITY = "min_futurity";
        @VisibleForTesting
        static final String KEY_MIN_INTERVAL = "min_interval";
        public long ALLOW_WHILE_IDLE_LONG_TIME = DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME;
        public long ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        public long[] APP_STANDBY_MIN_DELAYS = new long[this.DEFAULT_APP_STANDBY_DELAYS.length];
        public int[] APP_STANDBY_QUOTAS = new int[this.DEFAULT_APP_STANDBY_QUOTAS.length];
        public boolean APP_STANDBY_QUOTAS_ENABLED = true;
        public long APP_STANDBY_WINDOW = 3600000;
        private final long[] DEFAULT_APP_STANDBY_DELAYS = {0, 360000, 1800000, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT, 864000000};
        private final int[] DEFAULT_APP_STANDBY_QUOTAS = {720, 10, 2, 1, 0};
        private final String[] KEYS_APP_STANDBY_DELAY = {"standby_active_delay", "standby_working_delay", "standby_frequent_delay", "standby_rare_delay", "standby_never_delay"};
        @VisibleForTesting
        final String[] KEYS_APP_STANDBY_QUOTAS = {"standby_active_quota", "standby_working_quota", "standby_frequent_quota", "standby_rare_quota", "standby_never_quota"};
        public long LISTENER_TIMEOUT = 5000;
        public int MAX_ALARMS_PER_UID = 500;
        public long MAX_INTERVAL = 31536000000L;
        public long MIN_FUTURITY = 5000;
        public long MIN_INTERVAL = 60000;
        private long mLastAllowWhileIdleWhitelistDuration = -1;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            updateAllowWhileIdleWhitelistDurationLocked();
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("alarm_manager_constants"), false, this);
            updateConstants();
        }

        public void updateAllowWhileIdleWhitelistDurationLocked() {
            long j = this.mLastAllowWhileIdleWhitelistDuration;
            long j2 = this.ALLOW_WHILE_IDLE_WHITELIST_DURATION;
            if (j != j2) {
                this.mLastAllowWhileIdleWhitelistDuration = j2;
                BroadcastOptions opts = BroadcastOptions.makeBasic();
                opts.setTemporaryAppWhitelistDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION);
                AlarmManagerService.this.mIdleOptions = opts.toBundle();
            }
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (AlarmManagerService.this.mLock) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(AlarmManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, 5000);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, 60000);
                this.MAX_INTERVAL = this.mParser.getLong(KEY_MAX_INTERVAL, 31536000000L);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, 5000);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, (long) DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, 10000);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, 5000);
                this.APP_STANDBY_MIN_DELAYS[0] = this.mParser.getDurationMillis(this.KEYS_APP_STANDBY_DELAY[0], this.DEFAULT_APP_STANDBY_DELAYS[0]);
                for (int i = 1; i < this.KEYS_APP_STANDBY_DELAY.length; i++) {
                    this.APP_STANDBY_MIN_DELAYS[i] = this.mParser.getDurationMillis(this.KEYS_APP_STANDBY_DELAY[i], Math.max(this.APP_STANDBY_MIN_DELAYS[i - 1], this.DEFAULT_APP_STANDBY_DELAYS[i]));
                }
                this.APP_STANDBY_QUOTAS_ENABLED = this.mParser.getBoolean(KEY_APP_STANDBY_QUOTAS_ENABLED, true);
                this.APP_STANDBY_WINDOW = this.mParser.getLong(KEY_APP_STANDBY_WINDOW, 3600000);
                if (this.APP_STANDBY_WINDOW > 3600000) {
                    Slog.w(AlarmManagerService.TAG, "Cannot exceed the app_standby_window size of 3600000");
                    this.APP_STANDBY_WINDOW = 3600000;
                } else if (this.APP_STANDBY_WINDOW < 3600000) {
                    Slog.w(AlarmManagerService.TAG, "Using a non-default app_standby_window of " + this.APP_STANDBY_WINDOW);
                }
                this.APP_STANDBY_QUOTAS[0] = this.mParser.getInt(this.KEYS_APP_STANDBY_QUOTAS[0], this.DEFAULT_APP_STANDBY_QUOTAS[0]);
                for (int i2 = 1; i2 < this.KEYS_APP_STANDBY_QUOTAS.length; i2++) {
                    this.APP_STANDBY_QUOTAS[i2] = this.mParser.getInt(this.KEYS_APP_STANDBY_QUOTAS[i2], Math.min(this.APP_STANDBY_QUOTAS[i2 - 1], this.DEFAULT_APP_STANDBY_QUOTAS[i2]));
                }
                this.MAX_ALARMS_PER_UID = this.mParser.getInt(KEY_MAX_ALARMS_PER_UID, 500);
                if (this.MAX_ALARMS_PER_UID < 500) {
                    Slog.w(AlarmManagerService.TAG, "Cannot set max_alarms_per_uid lower than 500");
                    this.MAX_ALARMS_PER_UID = 500;
                }
                updateAllowWhileIdleWhitelistDurationLocked();
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String prefix) {
            dump(new IndentingPrintWriter(pw, "  ").setIndent(prefix));
        }

        /* access modifiers changed from: package-private */
        public void dump(IndentingPrintWriter pw) {
            pw.println("Settings:");
            pw.increaseIndent();
            pw.print(KEY_MIN_FUTURITY);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_FUTURITY, pw);
            pw.println();
            pw.print(KEY_MIN_INTERVAL);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_INTERVAL, pw);
            pw.println();
            pw.print(KEY_MAX_INTERVAL);
            pw.print("=");
            TimeUtils.formatDuration(this.MAX_INTERVAL, pw);
            pw.println();
            pw.print(KEY_LISTENER_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LISTENER_TIMEOUT, pw);
            pw.println();
            pw.print(KEY_ALLOW_WHILE_IDLE_SHORT_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_SHORT_TIME, pw);
            pw.println();
            pw.print(KEY_ALLOW_WHILE_IDLE_LONG_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_LONG_TIME, pw);
            pw.println();
            pw.print(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION, pw);
            pw.println();
            pw.print(KEY_MAX_ALARMS_PER_UID);
            pw.print("=");
            pw.println(this.MAX_ALARMS_PER_UID);
            int i = 0;
            while (true) {
                String[] strArr = this.KEYS_APP_STANDBY_DELAY;
                if (i >= strArr.length) {
                    break;
                }
                pw.print(strArr[i]);
                pw.print("=");
                TimeUtils.formatDuration(this.APP_STANDBY_MIN_DELAYS[i], pw);
                pw.println();
                i++;
            }
            pw.print(KEY_APP_STANDBY_QUOTAS_ENABLED);
            pw.print("=");
            pw.println(this.APP_STANDBY_QUOTAS_ENABLED);
            pw.print(KEY_APP_STANDBY_WINDOW);
            pw.print("=");
            TimeUtils.formatDuration(this.APP_STANDBY_WINDOW, pw);
            pw.println();
            int i2 = 0;
            while (true) {
                String[] strArr2 = this.KEYS_APP_STANDBY_QUOTAS;
                if (i2 < strArr2.length) {
                    pw.print(strArr2[i2]);
                    pw.print("=");
                    pw.println(this.APP_STANDBY_QUOTAS[i2]);
                    i2++;
                } else {
                    pw.decreaseIndent();
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dumpProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1112396529665L, this.MIN_FUTURITY);
            proto.write(1112396529666L, this.MIN_INTERVAL);
            proto.write(1112396529671L, this.MAX_INTERVAL);
            proto.write(1112396529667L, this.LISTENER_TIMEOUT);
            proto.write(1112396529668L, this.ALLOW_WHILE_IDLE_SHORT_TIME);
            proto.write(1112396529669L, this.ALLOW_WHILE_IDLE_LONG_TIME);
            proto.write(1112396529670L, this.ALLOW_WHILE_IDLE_WHITELIST_DURATION);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public final class PriorityClass {
        int priority = 2;
        int seq;

        PriorityClass() {
            this.seq = AlarmManagerService.this.mCurrentSeq - 1;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class WakeupEvent {
        public String action;
        public int uid;
        public long when;

        public WakeupEvent(long theTime, int theUid, String theAction) {
            this.when = theTime;
            this.uid = theUid;
            this.action = theAction;
        }
    }

    public final class Batch {
        public final ArrayList<Alarm> alarms = new ArrayList<>();
        long end;
        int flags;
        long start;

        Batch(Alarm seed) {
            this.start = seed.whenElapsed;
            this.end = AlarmManagerService.clampPositive(seed.maxWhenElapsed);
            this.flags = seed.flags;
            this.alarms.add(seed);
            if (seed.listener == AlarmManagerService.this.mTimeTickTrigger) {
                AlarmManagerService.this.mLastTickAdded = AlarmManagerService.this.mInjector.getCurrentTimeMillis();
            }
        }

        /* access modifiers changed from: package-private */
        public int size() {
            return this.alarms.size();
        }

        /* access modifiers changed from: package-private */
        public Alarm get(int index) {
            return this.alarms.get(index);
        }

        /* access modifiers changed from: package-private */
        public boolean canHold(long whenElapsed, long maxWhen) {
            return this.end >= whenElapsed && this.start <= maxWhen;
        }

        /* access modifiers changed from: package-private */
        public boolean add(Alarm alarm) {
            boolean newStart = false;
            int index = Collections.binarySearch(this.alarms, alarm, AlarmManagerService.sIncreasingTimeOrder);
            if (index < 0) {
                index = (0 - index) - 1;
            }
            this.alarms.add(index, alarm);
            if (alarm.listener == AlarmManagerService.this.mTimeTickTrigger) {
                AlarmManagerService alarmManagerService = AlarmManagerService.this;
                alarmManagerService.mLastTickAdded = alarmManagerService.mInjector.getCurrentTimeMillis();
            }
            if (alarm.whenElapsed > this.start) {
                this.start = alarm.whenElapsed;
                newStart = true;
            }
            if (alarm.maxWhenElapsed < this.end) {
                this.end = alarm.maxWhenElapsed;
            }
            this.flags |= alarm.flags;
            return newStart;
        }

        static /* synthetic */ boolean lambda$remove$0(Alarm alarm, Alarm a) {
            return a == alarm;
        }

        /* access modifiers changed from: package-private */
        public boolean remove(Alarm alarm) {
            return remove(new Predicate() {
                /* class com.android.server.$$Lambda$AlarmManagerService$Batch$Xltkj5RTKUMuFVeuavpuY7Ogzc */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.Batch.lambda$remove$0(AlarmManagerService.Alarm.this, (AlarmManagerService.Alarm) obj);
                }
            }, true);
        }

        /* access modifiers changed from: package-private */
        public boolean remove(Predicate<Alarm> predicate, boolean reOrdering) {
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = this.alarms.get(i);
                if (predicate.test(alarm)) {
                    this.alarms.remove(i);
                    if (!reOrdering) {
                        AlarmManagerService.this.decrementAlarmCount(alarm.uid, 1);
                    }
                    didRemove = true;
                    AlarmManagerService.this.hwRemoveRtcAlarm(alarm, true);
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                    if (alarm.listener == AlarmManagerService.this.mTimeTickTrigger) {
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        alarmManagerService.mLastTickRemoved = alarmManagerService.mInjector.getCurrentTimeMillis();
                    }
                    AlarmManagerService.this.mHandler.obtainMessage(8, alarm.operation).sendToTarget();
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                    i++;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
            }
            return didRemove;
        }

        /* access modifiers changed from: package-private */
        public boolean hasPackage(String packageName) {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if (this.alarms.get(i).matches(packageName)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean hasWakeups() {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if ((this.alarms.get(i).type & 1) == 0) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            StringBuilder b = new StringBuilder(40);
            b.append("Batch{");
            b.append(Integer.toHexString(hashCode()));
            b.append(" num=");
            b.append(size());
            b.append(" start=");
            b.append(this.start);
            b.append(" end=");
            b.append(this.end);
            if (this.flags != 0) {
                b.append(" flgs=0x");
                b.append(Integer.toHexString(this.flags));
            }
            b.append('}');
            return b.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, long nowElapsed, long nowRTC) {
            long token = proto.start(fieldId);
            proto.write(1112396529665L, this.start);
            proto.write(1112396529666L, this.end);
            proto.write(1120986464259L, this.flags);
            Iterator<Alarm> it = this.alarms.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(proto, 2246267895812L, nowElapsed, nowRTC);
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public static class BatchTimeOrder implements Comparator<Batch> {
        BatchTimeOrder() {
        }

        public int compare(Batch b1, Batch b2) {
            if (b1 == null || b2 == null) {
                return 0;
            }
            long when1 = b1.start;
            long when2 = b2.start;
            if (when1 > when2) {
                return 1;
            }
            if (when1 < when2) {
                return -1;
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void calculateDeliveryPriorities(ArrayList<Alarm> alarms) {
        int alarmPrio;
        int N = alarms.size();
        for (int i = 0; i < N; i++) {
            Alarm a = alarms.get(i);
            if (a.listener == this.mTimeTickTrigger) {
                alarmPrio = 0;
            } else if (a.wakeup) {
                alarmPrio = 1;
            } else {
                alarmPrio = 2;
            }
            PriorityClass packagePrio = a.priorityClass;
            String alarmPackage = a.sourcePackage;
            if (packagePrio == null) {
                packagePrio = this.mPriorities.get(alarmPackage);
            }
            if (packagePrio == null) {
                PriorityClass priorityClass = new PriorityClass();
                a.priorityClass = priorityClass;
                packagePrio = priorityClass;
                this.mPriorities.put(alarmPackage, packagePrio);
            }
            a.priorityClass = packagePrio;
            int i2 = packagePrio.seq;
            int i3 = this.mCurrentSeq;
            if (i2 != i3) {
                packagePrio.priority = alarmPrio;
                packagePrio.seq = i3;
            } else if (alarmPrio < packagePrio.priority) {
                packagePrio.priority = alarmPrio;
            }
        }
    }

    /* JADX WARN: Type inference failed for: r1v27, types: [com.android.server.AlarmManagerService$3, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @VisibleForTesting
    AlarmManagerService(Context context, Injector injector) {
        super(context);
        this.mBackgroundIntent = new Intent().addFlags(4);
        this.mLog = new LocalLog(TAG);
        this.mLock = new Object();
        this.mPendingBackgroundAlarms = new SparseArray<>();
        this.mTickHistory = new long[10];
        this.mBroadcastRefCount = 0;
        this.mAlarmsPerUid = new SparseIntArray();
        this.mPendingNonWakeupAlarms = new ArrayList<>();
        this.mInFlight = new ArrayList<>();
        this.mInFlightListeners = new ArrayList<>();
        this.mDeliveryTracker = new DeliveryTracker();
        this.mInteractive = true;
        this.mIsCancelRemoveAction = false;
        this.mLastAllowWhileIdleDispatch = new SparseLongArray();
        this.mUseAllowWhileIdleShortTime = new SparseBooleanArray();
        this.mAllowWhileIdleDispatches = new ArrayList<>();
        this.mStatLogger = new StatLogger(new String[]{"REBATCH_ALL_ALARMS", "REORDER_ALARMS_FOR_STANDBY"});
        this.mNextAlarmClockForUser = new SparseArray<>();
        this.mTmpSparseAlarmClockArray = new SparseArray<>();
        this.mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
        this.mHandlerSparseAlarmClockArray = new SparseArray<>();
        this.mIsAlarmDataOnlyMode = SystemProperties.getBoolean("persist.sys.alarm_data_only", false);
        this.mPriorities = new HashMap<>();
        this.mCurrentSeq = 0;
        this.mRecentWakeups = new LinkedList<>();
        this.RECENT_WAKEUP_PERIOD = 86400000;
        this.mAlarmDispatchComparator = new Comparator<Alarm>() {
            /* class com.android.server.AlarmManagerService.AnonymousClass1 */

            public int compare(Alarm lhs, Alarm rhs) {
                if (lhs.priorityClass.priority < rhs.priorityClass.priority) {
                    return -1;
                }
                if (lhs.priorityClass.priority > rhs.priorityClass.priority) {
                    return 1;
                }
                if (lhs.whenElapsed < rhs.whenElapsed) {
                    return -1;
                }
                if (lhs.whenElapsed > rhs.whenElapsed) {
                    return 1;
                }
                return 0;
            }
        };
        this.mAlarmBatches = new ArrayList<>();
        this.mPendingIdleUntil = null;
        this.mNextWakeFromIdle = null;
        this.mPendingWhileIdleAlarms = new ArrayList<>();
        this.mBroadcastStats = new SparseArray<>();
        this.mNumDelayedAlarms = 0;
        this.mTotalDelayTime = 0;
        this.mMaxDelayTime = 0;
        this.mService = new IAlarmManager.Stub() {
            /* class com.android.server.AlarmManagerService.AnonymousClass3 */

            public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) {
                int flags2;
                int callingUid = Binder.getCallingUid();
                AlarmManagerService.this.mAppOps.checkPackage(callingUid, callingPackage);
                if (interval == 0 || directReceiver == null) {
                    if (workSource != null) {
                        AlarmManagerService.this.getContext().enforcePermission("android.permission.UPDATE_DEVICE_STATS", Binder.getCallingPid(), callingUid, "AlarmManager.set");
                    }
                    int flags3 = flags & -11;
                    if (callingUid != 1000) {
                        flags3 &= -17;
                    }
                    if (windowLength == 0) {
                        flags3 |= 1;
                    }
                    if (alarmClock != null) {
                        flags2 = flags3 | 3;
                    } else if ((workSource != null || (callingUid >= 10000 && !UserHandle.isSameApp(callingUid, AlarmManagerService.this.mSystemUiUid) && (AlarmManagerService.this.mAppStateTracker == null || !AlarmManagerService.this.mAppStateTracker.isUidPowerSaveUserWhitelisted(callingUid)))) && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.email") && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.huawei.email") && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.android.exchange") && !AlarmManagerService.this.isContainsAppUidInWorksource(workSource, "com.google.android.gm")) {
                        flags2 = flags3;
                    } else {
                        flags2 = (flags3 | 8) & -5;
                    }
                    AlarmManagerService.this.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags2, workSource, alarmClock, callingUid, callingPackage);
                    return;
                }
                throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
            }

            public boolean setTime(long millis) {
                AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setTime");
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "setTime,uid is " + uid + ",pid is " + pid);
                return AlarmManagerService.this.setTimeImpl(millis);
            }

            public void setTimeZone(String tz) {
                AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME_ZONE", "setTimeZone");
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "setTimeZoneImpl,uid is " + uid + ",pid is " + pid + ",tz = " + tz);
                long oldId = Binder.clearCallingIdentity();
                try {
                    AlarmManagerService.this.setTimeZoneImpl(tz);
                } finally {
                    Binder.restoreCallingIdentity(oldId);
                }
            }

            public void remove(PendingIntent operation, IAlarmListener listener) {
                if (operation == null && listener == null) {
                    Slog.w(AlarmManagerService.TAG, "remove() with no intent or listener");
                    return;
                }
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.removeLocked(operation, listener);
                }
                AlarmManagerService.this.mHandler.obtainMessage(8, operation).sendToTarget();
            }

            public long getNextWakeFromIdleTime() {
                return AlarmManagerService.this.getNextWakeFromIdleTimeImpl();
            }

            public AlarmManager.AlarmClockInfo getNextAlarmClock(int userId) {
                return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getNextAlarmClock", null));
            }

            public long currentNetworkTimeMillis() {
                NtpTrustedTime time = NtpTrustedTime.getInstance(AlarmManagerService.this.getContext());
                if (time.hasCache()) {
                    return time.currentTimeMillis();
                }
                throw new ParcelableException(new DateTimeException("Missing NTP fix"));
            }

            /* access modifiers changed from: protected */
            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (DumpUtils.checkDumpAndUsageStatsPermission(AlarmManagerService.this.getContext(), AlarmManagerService.TAG, pw)) {
                    if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                        AlarmManagerService.this.dumpImpl(pw);
                    } else {
                        AlarmManagerService.this.dumpProto(fd);
                    }
                }
            }

            /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.AlarmManagerService$3 */
            /* JADX WARN: Multi-variable type inference failed */
            public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
                new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
            }

            /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.AlarmManagerService$HwInnerAlarmManagerService, android.os.IBinder] */
            /* JADX WARNING: Unknown variable types count: 1 */
            public IBinder getHwInnerService() {
                return AlarmManagerService.this.mHwInnerService;
            }
        };
        this.mForceAppStandbyListener = new AppStateTracker.Listener() {
            /* class com.android.server.AlarmManagerService.AnonymousClass6 */

            @Override // com.android.server.AppStateTracker.Listener
            public void unblockAllUnrestrictedAlarms() {
                synchronized (AlarmManagerService.this.mLock) {
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "unblockAllUnrestrictedAlarms");
                    AlarmManagerService.this.sendAllUnrestrictedPendingBackgroundAlarmsLocked();
                }
            }

            @Override // com.android.server.AppStateTracker.Listener
            public void unblockAlarmsForUid(int uid) {
                synchronized (AlarmManagerService.this.mLock) {
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "unblockAlarmsForUid uid:" + uid);
                    AlarmManagerService.this.sendPendingBackgroundAlarmsLocked(uid, null);
                }
            }

            @Override // com.android.server.AppStateTracker.Listener
            public void unblockAlarmsForUidPackage(int uid, String packageName) {
                synchronized (AlarmManagerService.this.mLock) {
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "unblockAlarmsForUidPackage uid:" + uid + ", pkg:" + packageName);
                    AlarmManagerService.this.sendPendingBackgroundAlarmsLocked(uid, packageName);
                }
            }

            @Override // com.android.server.AppStateTracker.Listener
            public void onUidForeground(int uid, boolean foreground) {
                synchronized (AlarmManagerService.this.mLock) {
                    if (foreground) {
                        AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(uid, true);
                    }
                }
            }
        };
        this.mSendCount = 0;
        this.mSendFinishCount = 0;
        this.mListenerCount = 0;
        this.mListenerFinishCount = 0;
        this.mHwInnerService = new HwInnerAlarmManagerService(this);
        this.mInjector = injector;
    }

    AlarmManagerService(Context context) {
        this(context, new Injector(context));
    }

    private long convertToElapsed(long when, int type) {
        boolean isRtc = true;
        if (!(type == 1 || type == 0)) {
            isRtc = false;
        }
        if (isRtc) {
            return when - (this.mInjector.getCurrentTimeMillis() - this.mInjector.getElapsedRealtime());
        }
        return when;
    }

    public static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < 10000) {
            futurity = 0;
        }
        return clampPositive(((long) (((double) futurity) * 0.75d)) + triggerAtTime);
    }

    static boolean addBatchLocked(ArrayList<Batch> list, Batch newBatch) {
        int index = Collections.binarySearch(list, newBatch, sBatchOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        list.add(index, newBatch);
        return index == 0;
    }

    private void insertAndBatchAlarmLocked(Alarm alarm) {
        int whichBatch;
        adjustAlarmLocked(alarm);
        if ((alarm.flags & 1) != 0) {
            whichBatch = -1;
        } else {
            whichBatch = attemptCoalesceLocked(alarm.whenElapsed, alarm.maxWhenElapsed);
        }
        if (whichBatch < 0) {
            addBatchLocked(this.mAlarmBatches, new Batch(alarm));
            return;
        }
        Batch batch = this.mAlarmBatches.get(whichBatch);
        if (batch.add(alarm)) {
            this.mAlarmBatches.remove(whichBatch);
            addBatchLocked(this.mAlarmBatches, batch);
        }
    }

    /* access modifiers changed from: package-private */
    public int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            Batch b = this.mAlarmBatches.get(i);
            if ((b.flags & 1) == 0 && b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -1;
    }

    static int getAlarmCount(ArrayList<Batch> batches) {
        int ret = 0;
        int size = batches.size();
        for (int i = 0; i < size; i++) {
            ret += batches.get(i).size();
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public boolean haveAlarmsTimeTickAlarm(ArrayList<Alarm> alarms) {
        if (alarms.size() == 0) {
            return false;
        }
        int batchSize = alarms.size();
        for (int j = 0; j < batchSize; j++) {
            if (alarms.get(j).listener == this.mTimeTickTrigger) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean haveBatchesTimeTickAlarm(ArrayList<Batch> batches) {
        int numBatches = batches.size();
        for (int i = 0; i < numBatches; i++) {
            if (haveAlarmsTimeTickAlarm(batches.get(i).alarms)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void rebatchAllAlarms() {
        synchronized (this.mLock) {
            rebatchAllAlarmsLocked(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void rebatchAllAlarmsLocked(boolean doValidate) {
        long start = this.mStatLogger.getTime();
        int oldCount = getAlarmCount(this.mAlarmBatches) + ArrayUtils.size(this.mPendingWhileIdleAlarms);
        boolean oldHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches) || haveAlarmsTimeTickAlarm(this.mPendingWhileIdleAlarms);
        ArrayList<Batch> oldSet = (ArrayList) this.mAlarmBatches.clone();
        this.mAlarmBatches.clear();
        Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
        long nowElapsed = this.mInjector.getElapsedRealtime();
        int oldBatches = oldSet.size();
        this.mIsCancelRemoveAction = true;
        for (int batchNum = 0; batchNum < oldBatches; batchNum++) {
            Batch batch = oldSet.get(batchNum);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                reAddAlarmLocked(batch.get(i), nowElapsed, doValidate);
            }
        }
        this.mIsCancelRemoveAction = false;
        if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
            Slog.wtf(TAG, "Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
            if (this.mPendingIdleUntil == null) {
                restorePendingWhileIdleAlarmsLocked();
            }
        }
        int newCount = getAlarmCount(this.mAlarmBatches) + ArrayUtils.size(this.mPendingWhileIdleAlarms);
        boolean newHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches) || haveAlarmsTimeTickAlarm(this.mPendingWhileIdleAlarms);
        if (oldCount != newCount) {
            Slog.wtf(TAG, "Rebatching: total count changed from " + oldCount + " to " + newCount);
        }
        if (oldHasTick != newHasTick) {
            Slog.wtf(TAG, "Rebatching: hasTick changed from " + oldHasTick + " to " + newHasTick);
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
        this.mStatLogger.logDurationStat(0, start);
    }

    /* access modifiers changed from: package-private */
    public boolean reorderAlarmsBasedOnStandbyBuckets(ArraySet<Pair<String, Integer>> targetPackages) {
        long start = this.mStatLogger.getTime();
        ArrayList<Alarm> rescheduledAlarms = new ArrayList<>();
        for (int batchIndex = this.mAlarmBatches.size() - 1; batchIndex >= 0; batchIndex--) {
            Batch batch = this.mAlarmBatches.get(batchIndex);
            for (int alarmIndex = batch.size() - 1; alarmIndex >= 0; alarmIndex--) {
                Alarm alarm = batch.get(alarmIndex);
                Pair<String, Integer> packageUser = Pair.create(alarm.sourcePackage, Integer.valueOf(UserHandle.getUserId(alarm.creatorUid)));
                if ((targetPackages == null || targetPackages.contains(packageUser)) && adjustDeliveryTimeBasedOnBucketLocked(alarm)) {
                    batch.remove(alarm);
                    rescheduledAlarms.add(alarm);
                }
            }
            if (batch.size() == 0) {
                this.mAlarmBatches.remove(batchIndex);
            }
        }
        for (int i = 0; i < rescheduledAlarms.size(); i++) {
            insertAndBatchAlarmLocked(rescheduledAlarms.get(i));
        }
        this.mStatLogger.logDurationStat(1, start);
        if (rescheduledAlarms.size() > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void reAddAlarmLocked(Alarm a, long nowElapsed, boolean doValidate) {
        long maxElapsed;
        a.when = a.origWhen;
        long whenElapsed = convertToElapsed(a.when, a.type);
        if (a.windowLength == 0) {
            maxElapsed = whenElapsed;
        } else if (a.windowLength > 0) {
            maxElapsed = clampPositive(a.windowLength + whenElapsed);
        } else {
            maxElapsed = maxTriggerTime(nowElapsed, whenElapsed, a.repeatInterval);
        }
        a.whenElapsed = whenElapsed;
        a.expectedWhenElapsed = whenElapsed;
        a.maxWhenElapsed = maxElapsed;
        a.expectedMaxWhenElapsed = maxElapsed;
        setImplLocked(a, true, doValidate);
    }

    static long clampPositive(long val) {
        return val >= 0 ? val : JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: package-private */
    public void sendPendingBackgroundAlarmsLocked(int uid, String packageName) {
        ArrayList<Alarm> alarmsToDeliver;
        ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.get(uid);
        if (!(alarmsForUid == null || alarmsForUid.size() == 0)) {
            if (packageName != null) {
                alarmsToDeliver = new ArrayList<>();
                for (int i = alarmsForUid.size() - 1; i >= 0; i--) {
                    if (alarmsForUid.get(i).matches(packageName)) {
                        alarmsToDeliver.add(alarmsForUid.remove(i));
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.remove(uid);
                }
            } else {
                alarmsToDeliver = alarmsForUid;
                this.mPendingBackgroundAlarms.remove(uid);
            }
            deliverPendingBackgroundAlarmsLocked(alarmsToDeliver, this.mInjector.getElapsedRealtime());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAllUnrestrictedPendingBackgroundAlarmsLocked() {
        ArrayList<Alarm> alarmsToDeliver = new ArrayList<>();
        findAllUnrestrictedPendingBackgroundAlarmsLockedInner(this.mPendingBackgroundAlarms, alarmsToDeliver, new Predicate() {
            /* class com.android.server.$$Lambda$AlarmManagerService$nSJw2tKfoL3YIrKDtszoL44jcSM */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AlarmManagerService.lambda$nSJw2tKfoL3YIrKDtszoL44jcSM(AlarmManagerService.this, (AlarmManagerService.Alarm) obj);
            }
        });
        if (alarmsToDeliver.size() > 0) {
            deliverPendingBackgroundAlarmsLocked(alarmsToDeliver, this.mInjector.getElapsedRealtime());
        }
    }

    @VisibleForTesting
    static void findAllUnrestrictedPendingBackgroundAlarmsLockedInner(SparseArray<ArrayList<Alarm>> pendingAlarms, ArrayList<Alarm> unrestrictedAlarms, Predicate<Alarm> isBackgroundRestricted) {
        for (int uidIndex = pendingAlarms.size() - 1; uidIndex >= 0; uidIndex--) {
            pendingAlarms.keyAt(uidIndex);
            ArrayList<Alarm> alarmsForUid = pendingAlarms.valueAt(uidIndex);
            for (int alarmIndex = alarmsForUid.size() - 1; alarmIndex >= 0; alarmIndex--) {
                Alarm alarm = alarmsForUid.get(alarmIndex);
                if (!isBackgroundRestricted.test(alarm)) {
                    unrestrictedAlarms.add(alarm);
                    alarmsForUid.remove(alarmIndex);
                }
            }
            if (alarmsForUid.size() == 0) {
                pendingAlarms.removeAt(uidIndex);
            }
        }
    }

    private void deliverPendingBackgroundAlarmsLocked(ArrayList<Alarm> alarms, long nowELAPSED) {
        ArrayList<Alarm> arrayList;
        long j;
        AlarmManagerService alarmManagerService;
        boolean hasWakeup;
        int N;
        int i;
        AlarmManagerService alarmManagerService2 = this;
        ArrayList<Alarm> arrayList2 = alarms;
        long j2 = nowELAPSED;
        int N2 = alarms.size();
        boolean hasWakeup2 = false;
        int i2 = 0;
        while (i2 < N2) {
            Alarm alarm = arrayList2.get(i2);
            if (alarm.wakeup) {
                hasWakeup = true;
            } else {
                hasWakeup = hasWakeup2;
            }
            alarm.count = 1;
            if (alarm.repeatInterval > 0) {
                alarm.count = (int) (((long) alarm.count) + ((j2 - alarm.expectedWhenElapsed) / alarm.repeatInterval));
                long delta = ((long) alarm.count) * alarm.repeatInterval;
                long nextElapsed = alarm.expectedWhenElapsed + delta;
                i = i2;
                N = N2;
                setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName, alarm.procName);
            } else {
                i = i2;
                N = N2;
            }
            i2 = i + 1;
            alarmManagerService2 = this;
            arrayList2 = alarms;
            j2 = nowELAPSED;
            hasWakeup2 = hasWakeup;
            N2 = N;
        }
        if (!hasWakeup2) {
            alarmManagerService = this;
            j = nowELAPSED;
            if (alarmManagerService.checkAllowNonWakeupDelayLocked(j)) {
                if (alarmManagerService.mPendingNonWakeupAlarms.size() == 0) {
                    alarmManagerService.mStartCurrentDelayTime = j;
                    alarmManagerService.mNextNonWakeupDeliveryTime = ((alarmManagerService.currentNonWakeupFuzzLocked(j) * 3) / 2) + j;
                }
                alarmManagerService.mPendingNonWakeupAlarms.addAll(alarms);
                alarmManagerService.mNumDelayedAlarms += alarms.size();
                return;
            }
            arrayList = alarms;
        } else {
            alarmManagerService = this;
            arrayList = alarms;
            j = nowELAPSED;
        }
        if (alarmManagerService.mPendingNonWakeupAlarms.size() > 0) {
            arrayList.addAll(alarmManagerService.mPendingNonWakeupAlarms);
            long thisDelayTime = j - alarmManagerService.mStartCurrentDelayTime;
            alarmManagerService.mTotalDelayTime += thisDelayTime;
            if (alarmManagerService.mMaxDelayTime < thisDelayTime) {
                alarmManagerService.mMaxDelayTime = thisDelayTime;
            }
            alarmManagerService.mPendingNonWakeupAlarms.clear();
        }
        calculateDeliveryPriorities(alarms);
        Collections.sort(arrayList, alarmManagerService.mAlarmDispatchComparator);
        deliverAlarmsLocked(alarms, nowELAPSED);
    }

    /* access modifiers changed from: package-private */
    public void restorePendingWhileIdleAlarmsLocked() {
        if (this.mPendingWhileIdleAlarms.size() > 0) {
            ArrayList<Alarm> alarms = this.mPendingWhileIdleAlarms;
            this.mPendingWhileIdleAlarms = new ArrayList<>();
            long nowElapsed = this.mInjector.getElapsedRealtime();
            for (int i = alarms.size() - 1; i >= 0; i--) {
                reAddAlarmLocked(alarms.get(i), nowElapsed, false);
            }
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
    }

    /* access modifiers changed from: package-private */
    public static final class InFlight {
        final int mAlarmType;
        final BroadcastStats mBroadcastStats;
        final int mCreatorUid;
        final FilterStats mFilterStats;
        final IBinder mListener;
        final PendingIntent mPendingIntent;
        final String mTag;
        final int mUid;
        final long mWhenElapsed;
        final WorkSource mWorkSource;

        InFlight(AlarmManagerService service, Alarm alarm, long nowELAPSED) {
            BroadcastStats broadcastStats;
            this.mPendingIntent = alarm.operation;
            this.mWhenElapsed = nowELAPSED;
            this.mListener = alarm.listener != null ? alarm.listener.asBinder() : null;
            this.mWorkSource = alarm.workSource;
            this.mUid = alarm.uid;
            this.mCreatorUid = alarm.creatorUid;
            this.mTag = alarm.statsTag;
            if (alarm.operation != null) {
                broadcastStats = service.getStatsLocked(alarm.operation);
            } else {
                broadcastStats = service.getStatsLocked(alarm.uid, alarm.packageName);
            }
            this.mBroadcastStats = broadcastStats;
            FilterStats fs = this.mBroadcastStats.filterStats.get(this.mTag);
            if (fs == null) {
                fs = new FilterStats(this.mBroadcastStats, this.mTag);
                this.mBroadcastStats.filterStats.put(this.mTag, fs);
            }
            fs.lastTime = nowELAPSED;
            this.mFilterStats = fs;
            this.mAlarmType = alarm.type;
        }

        /* access modifiers changed from: package-private */
        public boolean isBroadcast() {
            PendingIntent pendingIntent = this.mPendingIntent;
            return pendingIntent != null && pendingIntent.isBroadcast();
        }

        public String toString() {
            return "InFlight{pendingIntent=" + this.mPendingIntent + ", when=" + this.mWhenElapsed + ", workSource=" + this.mWorkSource + ", uid=" + this.mUid + ", creatorUid=" + this.mCreatorUid + ", tag=" + this.mTag + ", broadcastStats=" + this.mBroadcastStats + ", filterStats=" + this.mFilterStats + ", alarmType=" + this.mAlarmType + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.mUid);
            proto.write(1138166333442L, this.mTag);
            proto.write(1112396529667L, this.mWhenElapsed);
            proto.write(1159641169924L, this.mAlarmType);
            PendingIntent pendingIntent = this.mPendingIntent;
            if (pendingIntent != null) {
                pendingIntent.writeToProto(proto, 1146756268037L);
            }
            BroadcastStats broadcastStats = this.mBroadcastStats;
            if (broadcastStats != null) {
                broadcastStats.writeToProto(proto, 1146756268038L);
            }
            FilterStats filterStats = this.mFilterStats;
            if (filterStats != null) {
                filterStats.writeToProto(proto, 1146756268039L);
            }
            WorkSource workSource = this.mWorkSource;
            if (workSource != null) {
                workSource.writeToProto(proto, 1146756268040L);
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBroadcastAlarmPendingLocked(int uid) {
        int numListeners = this.mInFlightListeners.size();
        for (int i = 0; i < numListeners; i++) {
            this.mInFlightListeners.get(i).broadcastAlarmPending(uid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBroadcastAlarmCompleteLocked(int uid) {
        int numListeners = this.mInFlightListeners.size();
        for (int i = 0; i < numListeners; i++) {
            this.mInFlightListeners.get(i).broadcastAlarmComplete(uid);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class FilterStats {
        long aggregateTime;
        int count;
        long lastTime;
        @RCUnownedRef
        final BroadcastStats mBroadcastStats;
        final String mTag;
        int nesting;
        int numWakeup;
        long startTime;

        FilterStats(BroadcastStats broadcastStats, String tag) {
            this.mBroadcastStats = broadcastStats;
            this.mTag = tag;
        }

        public String toString() {
            return "FilterStats{tag=" + this.mTag + ", lastTime=" + this.lastTime + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.mTag);
            proto.write(1112396529666L, this.lastTime);
            proto.write(1112396529667L, this.aggregateTime);
            proto.write(1120986464260L, this.count);
            proto.write(1120986464261L, this.numWakeup);
            proto.write(1112396529670L, this.startTime);
            proto.write(1120986464263L, this.nesting);
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BroadcastStats {
        long aggregateTime;
        int count;
        final ArrayMap<String, FilterStats> filterStats = new ArrayMap<>();
        final String mPackageName;
        final int mUid;
        int nesting;
        int numWakeup;
        long startTime;

        BroadcastStats(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "BroadcastStats{uid=" + this.mUid + ", packageName=" + this.mPackageName + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.mUid);
            proto.write(1138166333442L, this.mPackageName);
            proto.write(1112396529667L, this.aggregateTime);
            proto.write(1120986464260L, this.count);
            proto.write(1120986464261L, this.numWakeup);
            proto.write(1112396529670L, this.startTime);
            proto.write(1120986464263L, this.nesting);
            proto.end(token);
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        this.mInjector.init();
        synchronized (this.mLock) {
            this.mHandler = new AlarmHandler();
            this.mOperationCancelListener = new PendingIntent.CancelListener() {
                /* class com.android.server.$$Lambda$AlarmManagerService$6biANsIZPXvOci6ybplaG3NF_Yk */

                public final void onCancelled(PendingIntent pendingIntent) {
                    AlarmManagerService.this.lambda$onStart$0$AlarmManagerService(pendingIntent);
                }
            };
            this.mConstants = new Constants(this.mHandler);
            this.mAppWakeupHistory = new AppWakeupHistory(AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
            Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "alarmmanagerservice onStart");
            this.mNextNonWakeup = 0;
            this.mNextWakeup = 0;
            setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));
            long systemBuildTime = Long.max(SystemProperties.getLong("ro.build.date.utc", -1) * 1000, Long.max(Environment.getRootDirectory().lastModified(), Build.TIME));
            if (NORMAL_MODE.equals(SystemProperties.get("ro.runmode", NORMAL_MODE)) && this.mInjector.getCurrentTimeMillis() < systemBuildTime) {
                Slog.i(TAG, "Current time only " + this.mInjector.getCurrentTimeMillis() + ", advancing to build time " + systemBuildTime);
                this.mInjector.setKernelTime(systemBuildTime);
            }
            this.mSystemUiUid = this.mInjector.getSystemUiUid();
            if (this.mSystemUiUid <= 0) {
                Slog.wtf(TAG, "SysUI package not found!");
            }
            this.mWakeLock = this.mInjector.getAlarmWakeLock();
            this.mTimeTickIntent = new Intent("android.intent.action.TIME_TICK").addFlags(1344274432);
            this.mTimeTickTrigger = new IAlarmListener.Stub() {
                /* class com.android.server.AlarmManagerService.AnonymousClass2 */

                public void doAlarm(IAlarmCompleteListener callback) throws RemoteException {
                    AlarmManagerService.this.mHandler.post(new Runnable(callback) {
                        /* class com.android.server.$$Lambda$AlarmManagerService$2$EoD98JN9R2METkD12gPs320c */
                        private final /* synthetic */ IAlarmCompleteListener f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AlarmManagerService.AnonymousClass2.this.lambda$doAlarm$0$AlarmManagerService$2(this.f$1);
                        }
                    });
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.mLastTickReceived = AlarmManagerService.this.mInjector.getCurrentTimeMillis();
                    }
                    AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                }

                /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.AlarmManagerService$2 */
                /* JADX WARN: Multi-variable type inference failed */
                public /* synthetic */ void lambda$doAlarm$0$AlarmManagerService$2(IAlarmCompleteListener callback) {
                    AlarmManagerService.this.getContext().sendBroadcastAsUser(AlarmManagerService.this.mTimeTickIntent, UserHandle.ALL);
                    try {
                        callback.alarmComplete(this);
                    } catch (RemoteException e) {
                    }
                }
            };
            Intent intent = new Intent("android.intent.action.DATE_CHANGED");
            intent.addFlags(538968064);
            this.mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), 0, intent, DumpState.DUMP_HANDLE, UserHandle.ALL);
            this.mClockReceiver = this.mInjector.getClockReceiver(this);
            new InteractiveStateReceiver();
            new UninstallReceiver();
            if (this.mInjector.isAlarmDriverPresent()) {
                new AlarmThread().start();
            } else {
                Slog.w(TAG, "Failed to open alarm driver. Falling back to a handler.");
            }
            try {
                ActivityManager.getService().registerUidObserver(new UidObserver(), 14, -1, (String) null);
            } catch (RemoteException e) {
            }
        }
        publishLocalService(AlarmManagerInternal.class, new LocalService());
        publishBinderService("alarm", this.mService);
    }

    public /* synthetic */ void lambda$onStart$0$AlarmManagerService(PendingIntent intent) {
        removeImpl(intent, null);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this.mLock) {
                this.mConstants.start(getContext().getContentResolver());
                this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
                this.mLocalDeviceIdleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
                this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
                this.mUsageStatsManagerInternal.addAppIdleStateChangeListener(new AppStandbyTracker());
                this.mAppStateTracker = (AppStateTracker) LocalServices.getService(AppStateTracker.class);
                this.mAppStateTracker.addListener(this.mForceAppStandbyListener);
                this.mClockReceiver.scheduleTimeTickEvent();
                this.mClockReceiver.scheduleDateChangedEvent();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            this.mInjector.close();
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setTimeImpl(long millis) {
        if (!this.mInjector.isAlarmDriverPresent()) {
            Slog.w(TAG, "Not setting time since no alarm driver is available.");
            return false;
        }
        synchronized (this.mLock) {
            long currentTimeMillis = this.mInjector.getCurrentTimeMillis();
            HwLog.dubaie("DUBAI_TAG_TIME_CHANGED", "delta=" + (millis - currentTimeMillis));
            this.mInjector.setKernelTime(millis);
            TimeZone timeZone = TimeZone.getDefault();
            int currentTzOffset = timeZone.getOffset(currentTimeMillis);
            int newTzOffset = timeZone.getOffset(millis);
            if (currentTzOffset != newTzOffset) {
                Slog.i(TAG, "Timezone offset has changed, updating kernel timezone");
                this.mInjector.setKernelTimezone(-(newTzOffset / 60000));
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setTimeZoneImpl(String tz) {
        if (!TextUtils.isEmpty(tz)) {
            TimeZone zone = TimeZone.getTimeZone(tz);
            boolean timeZoneWasChanged = false;
            synchronized (this) {
                String current = SystemProperties.get(TIMEZONE_PROPERTY);
                if (current == null || !current.equals(zone.getID())) {
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "timezone changed: " + current + ", new=" + zone.getID());
                    timeZoneWasChanged = true;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                int gmtOffset = zone.getOffset(this.mInjector.getCurrentTimeMillis());
                this.mInjector.setKernelTimezone(-(gmtOffset / 60000));
                if (!TextUtils.isEmpty(current) && timeZoneWasChanged) {
                    int oldGmtOffset = TimeZone.getTimeZone(current).getOffset(this.mInjector.getCurrentTimeMillis());
                    HwLog.dubaie("DUBAI_TAG_TIMEZONE_CHANGED", "oldId=" + current + " oldGmtOffset=" + oldGmtOffset + " newId=" + zone.getID() + " newGmtOffset=" + gmtOffset);
                }
            }
            TimeZone.setDefault(null);
            if (timeZoneWasChanged) {
                this.mClockReceiver.scheduleDateChangedEvent();
                Intent intent = new Intent("android.intent.action.TIMEZONE_CHANGED");
                intent.addFlags(555745280);
                intent.putExtra("time-zone", zone.getID());
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImpl(PendingIntent operation, IAlarmListener listener) {
        synchronized (this.mLock) {
            removeLocked(operation, listener);
        }
        removeDeskClockFromFWK(operation);
    }

    /* JADX INFO: Multiple debug info for r9v5 'triggerElapsed'  long: [D('triggerElapsed' long), D('minInterval' long)] */
    /* JADX INFO: Multiple debug info for r9v6 'minInterval'  long: [D('triggerElapsed' long), D('minInterval' long)] */
    /* access modifiers changed from: package-private */
    public void setImpl(int type, long triggerAtTime, long windowLength, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        long interval2;
        long triggerAtTime2;
        long minInterval;
        long minInterval2;
        long windowLength2;
        long maxElapsed;
        Object obj;
        int count;
        StringBuilder sb;
        long windowLength3 = windowLength;
        if (!(operation == null && directReceiver == null) && (operation == null || directReceiver == null)) {
            if (windowLength3 > AppStandbyController.SettingsObserver.DEFAULT_NOTIFICATION_TIMEOUT) {
                Slog.w(TAG, "Window length " + windowLength3 + "ms suspiciously long; limiting to 1 hour");
                windowLength3 = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
            }
            long minInterval3 = this.mConstants.MIN_INTERVAL;
            if (interval > 0 && interval < minInterval3) {
                Slog.w(TAG, "Suspiciously short interval " + interval + " millis; expanding to " + (minInterval3 / 1000) + " seconds");
                interval2 = minInterval3;
            } else if (interval > this.mConstants.MAX_INTERVAL) {
                Slog.w(TAG, "Suspiciously long interval " + interval + " millis; clamping");
                interval2 = this.mConstants.MAX_INTERVAL;
            } else {
                interval2 = interval;
            }
            if (type < 0 || type > 3) {
                throw new IllegalArgumentException("Invalid alarm type " + type);
            }
            if (triggerAtTime < 0) {
                Slog.w(TAG, "Invalid alarm trigger time! " + triggerAtTime + " from uid=" + callingUid + " pid=" + ((long) Binder.getCallingPid()));
                triggerAtTime2 = 0;
            } else {
                triggerAtTime2 = triggerAtTime;
            }
            long nowElapsed = this.mInjector.getElapsedRealtime();
            long nominalTrigger = convertToElapsed(triggerAtTime2, type);
            long minTrigger = nowElapsed + this.mConstants.MIN_FUTURITY;
            long triggerElapsed = nominalTrigger > minTrigger ? nominalTrigger : minTrigger;
            if (windowLength3 == 0) {
                windowLength2 = windowLength3;
                minInterval = minInterval3;
                minInterval2 = triggerElapsed;
                maxElapsed = triggerElapsed;
            } else if (windowLength3 < 0) {
                long maxElapsed2 = maxTriggerTime(nowElapsed, triggerElapsed, interval2);
                minInterval = minInterval3;
                minInterval2 = triggerElapsed;
                windowLength2 = maxElapsed2 - minInterval2;
                maxElapsed = maxElapsed2;
            } else {
                minInterval = minInterval3;
                minInterval2 = triggerElapsed;
                windowLength2 = windowLength3;
                maxElapsed = minInterval2 + windowLength3;
            }
            if (operation != null) {
                operation.registerCancelListener(this.mOperationCancelListener);
            }
            Object obj2 = this.mLock;
            synchronized (obj2) {
                try {
                    try {
                        count = this.mAlarmsPerUid.get(callingUid, 0);
                        sb = new StringBuilder();
                        sb.append("set(");
                        sb.append(operation);
                        sb.append(") : type=");
                        sb.append(type);
                        sb.append(" triggerAtTime=");
                        sb.append(triggerAtTime2);
                        sb.append(" win=");
                        sb.append(windowLength2);
                        sb.append(" tElapsed=");
                        sb.append(minInterval2);
                        sb.append(" maxElapsed=");
                        sb.append(maxElapsed);
                        sb.append(" interval=");
                        sb.append(interval2);
                        sb.append(" flags=0x");
                        sb.append(Integer.toHexString(flags));
                        sb.append(" at=+");
                    } catch (Throwable th) {
                        th = th;
                        obj = obj2;
                        throw th;
                    }
                    try {
                        sb.append(minInterval2 - nowElapsed);
                        sb.append("ms uid=");
                        sb.append(callingUid);
                        sb.append(" count=");
                        sb.append(count);
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, sb.toString());
                        try {
                            if (count < this.mConstants.MAX_ALARMS_PER_UID) {
                                obj = obj2;
                                try {
                                    setImplLocked(type, triggerAtTime2, minInterval2, windowLength2, maxElapsed, interval2, operation, directReceiver, listenerTag, flags, true, workSource, alarmClock, callingUid, callingPackage);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                obj = obj2;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Maximum limit of concurrent alarms ");
                                try {
                                    sb2.append(this.mConstants.MAX_ALARMS_PER_UID);
                                    sb2.append(" reached for uid: ");
                                    sb2.append(UserHandle.formatUid(callingUid));
                                    sb2.append(", callingPackage: ");
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                                try {
                                    sb2.append(callingPackage);
                                    String errorMsg = sb2.toString();
                                    this.mHandler.obtainMessage(8, operation).sendToTarget();
                                    Slog.w(TAG, errorMsg);
                                    throw new IllegalStateException(errorMsg);
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            obj = obj2;
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        obj = obj2;
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    obj = obj2;
                    throw th;
                }
            }
        } else {
            Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
        }
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        setImplLocked(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, flags, doValidate, workSource, alarmClock, callingUid, callingPackage, getProcName(Binder.getCallingPid()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008a  */
    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage, String procName) {
        PendingIntent pendingIntent;
        AlarmManagerService alarmManagerService;
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage, procName);
        try {
            try {
                if (ActivityManager.getService().isAppStartModeDisabled(callingUid, callingPackage)) {
                    Slog.w(TAG, "Not setting alarm from " + callingUid + ":" + a + " -- package not allowed to start");
                    alarmManagerService = this;
                    try {
                        pendingIntent = operation;
                        try {
                            alarmManagerService.mHandler.obtainMessage(8, pendingIntent).sendToTarget();
                        } catch (RemoteException e) {
                        }
                    } catch (RemoteException e2) {
                        pendingIntent = operation;
                        if (!alarmManagerService.mIsCancelRemoveAction) {
                        }
                        alarmManagerService.incrementAlarmCount(a.uid);
                        alarmManagerService.setImplLocked(a, false, doValidate);
                    }
                } else {
                    alarmManagerService = this;
                    pendingIntent = operation;
                    if (!alarmManagerService.mIsCancelRemoveAction) {
                        alarmManagerService.removeLocked(pendingIntent, directReceiver);
                    }
                    alarmManagerService.incrementAlarmCount(a.uid);
                    alarmManagerService.setImplLocked(a, false, doValidate);
                }
            } catch (RemoteException e3) {
                alarmManagerService = this;
                pendingIntent = operation;
                if (!alarmManagerService.mIsCancelRemoveAction) {
                }
                alarmManagerService.incrementAlarmCount(a.uid);
                alarmManagerService.setImplLocked(a, false, doValidate);
            }
        } catch (RemoteException e4) {
            alarmManagerService = this;
            pendingIntent = operation;
        }
    }

    private String getProcName(int pid) {
        try {
            List<ActivityManager.RunningAppProcessInfo> processes = ActivityManager.getService().getRunningAppProcesses();
            int size = processes != null ? processes.size() : 0;
            for (int i = 0; i < size; i++) {
                ActivityManager.RunningAppProcessInfo pi = processes.get(i);
                if (pi.pid == pid) {
                    return pi.processName;
                }
            }
            return null;
        } catch (RemoteException e) {
            Slog.w(TAG, "getRunningAppProcesses failed", e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getQuotaForBucketLocked(int bucket) {
        int index;
        if (bucket <= 10) {
            index = 0;
        } else if (bucket <= 20) {
            index = 1;
        } else if (bucket <= 30) {
            index = 2;
        } else if (bucket < 50) {
            index = 3;
        } else {
            index = 4;
        }
        return this.mConstants.APP_STANDBY_QUOTAS[index];
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getMinDelayForBucketLocked(int bucket) {
        int index;
        if (bucket == 50) {
            index = 4;
        } else if (bucket > 30) {
            index = 3;
        } else if (bucket > 20) {
            index = 2;
        } else if (bucket > 10) {
            index = 1;
        } else {
            index = 0;
        }
        return this.mConstants.APP_STANDBY_MIN_DELAYS[index];
    }

    private boolean adjustDeliveryTimeBasedOnBucketLocked(Alarm alarm) {
        long oldWhenElapsed;
        boolean z;
        long t;
        if (isExemptFromAppStandby(alarm)) {
            return false;
        }
        if (!this.mAppStandbyParole) {
            long oldWhenElapsed2 = alarm.whenElapsed;
            long oldMaxWhenElapsed = alarm.maxWhenElapsed;
            String sourcePackage = alarm.sourcePackage;
            int sourceUserId = UserHandle.getUserId(alarm.creatorUid);
            int standbyBucket = this.mUsageStatsManagerInternal.getAppStandbyBucket(sourcePackage, sourceUserId, this.mInjector.getElapsedRealtime());
            if (this.mConstants.APP_STANDBY_QUOTAS_ENABLED) {
                int wakeupsInWindow = this.mAppWakeupHistory.getTotalWakeupsInWindow(sourcePackage, sourceUserId);
                int quotaForBucket = getQuotaForBucketLocked(standbyBucket);
                boolean deferred = false;
                if (wakeupsInWindow >= quotaForBucket) {
                    if (quotaForBucket <= 0) {
                        t = this.mInjector.getElapsedRealtime() + 86400000;
                        oldWhenElapsed = oldWhenElapsed2;
                    } else {
                        oldWhenElapsed = oldWhenElapsed2;
                        t = this.mAppWakeupHistory.getLastWakeupForPackage(sourcePackage, sourceUserId, quotaForBucket) + 1 + this.mConstants.APP_STANDBY_WINDOW;
                    }
                    if (alarm.expectedWhenElapsed < t) {
                        alarm.maxWhenElapsed = t;
                        alarm.whenElapsed = t;
                        deferred = true;
                    }
                } else {
                    oldWhenElapsed = oldWhenElapsed2;
                }
                if (!deferred) {
                    alarm.whenElapsed = alarm.expectedWhenElapsed;
                    alarm.maxWhenElapsed = alarm.expectedMaxWhenElapsed;
                }
                z = true;
            } else {
                oldWhenElapsed = oldWhenElapsed2;
                z = true;
                long lastElapsed = this.mAppWakeupHistory.getLastWakeupForPackage(sourcePackage, sourceUserId, 1);
                if (lastElapsed > 0) {
                    long minElapsed = getMinDelayForBucketLocked(standbyBucket) + lastElapsed;
                    if (alarm.expectedWhenElapsed < minElapsed) {
                        alarm.maxWhenElapsed = minElapsed;
                        alarm.whenElapsed = minElapsed;
                    } else {
                        alarm.whenElapsed = alarm.expectedWhenElapsed;
                        alarm.maxWhenElapsed = alarm.expectedMaxWhenElapsed;
                    }
                }
            }
            if (oldWhenElapsed == alarm.whenElapsed && oldMaxWhenElapsed == alarm.maxWhenElapsed) {
                return false;
            }
            return z;
        } else if (alarm.whenElapsed <= alarm.expectedWhenElapsed) {
            return false;
        } else {
            alarm.whenElapsed = alarm.expectedWhenElapsed;
            alarm.maxWhenElapsed = alarm.expectedMaxWhenElapsed;
            return true;
        }
    }

    private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
        Alarm alarm;
        if (!this.mIsAlarmDataOnlyMode && isAwareAlarmManagerEnabled()) {
            modifyAlarmIfOverload(a);
        }
        if ((a.flags & 16) != 0) {
            if (this.mNextWakeFromIdle != null && a.whenElapsed > this.mNextWakeFromIdle.whenElapsed) {
                long j = this.mNextWakeFromIdle.whenElapsed;
                a.maxWhenElapsed = j;
                a.whenElapsed = j;
                a.when = j;
            }
            int fuzz = fuzzForDuration(a.whenElapsed - this.mInjector.getElapsedRealtime());
            if (fuzz > 0) {
                if (this.mRandom == null) {
                    this.mRandom = new Random();
                }
                a.whenElapsed -= (long) this.mRandom.nextInt(fuzz);
                long j2 = a.whenElapsed;
                a.maxWhenElapsed = j2;
                a.when = j2;
            }
        } else if (this.mPendingIdleUntil != null && (a.flags & 14) == 0) {
            this.mPendingWhileIdleAlarms.add(a);
            return;
        }
        adjustDeliveryTimeBasedOnBucketLocked(a);
        insertAndBatchAlarmLocked(a);
        if (a.alarmClock != null) {
            this.mNextAlarmClockMayChange = true;
        }
        boolean needRebatch = false;
        if ((a.flags & 16) != 0) {
            Alarm alarm2 = this.mPendingIdleUntil;
            if (!(alarm2 == a || alarm2 == null)) {
                Slog.wtfStack(TAG, "setImplLocked: idle until changed from " + this.mPendingIdleUntil + " to " + a);
            }
            Slog.i(TAG, "Idle alarm set, new:" + a + ", old:" + this.mPendingIdleUntil);
            this.mPendingIdleUntil = a;
            needRebatch = true;
        } else if ((a.flags & 2) != 0 && ((alarm = this.mNextWakeFromIdle) == null || alarm.whenElapsed > a.whenElapsed)) {
            this.mNextWakeFromIdle = a;
            if (this.mPendingIdleUntil != null) {
                needRebatch = true;
            }
        }
        if (!rebatching) {
            if (needRebatch) {
                rebatchAllAlarmsLocked(false);
            }
            hwSetRtcAlarm(a);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    private final class LocalService implements AlarmManagerInternal {
        private LocalService() {
        }

        @Override // com.android.server.AlarmManagerInternal
        public boolean isIdling() {
            return AlarmManagerService.this.isIdlingImpl();
        }

        @Override // com.android.server.AlarmManagerInternal
        public void removeAlarmsForUid(int uid) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeLocked(uid);
            }
        }

        @Override // com.android.server.AlarmManagerInternal
        public void registerInFlightListener(AlarmManagerInternal.InFlightListener callback) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.mInFlightListeners.add(callback);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpImpl(PrintWriter pw) {
        String str;
        SimpleDateFormat sdf;
        long nextNonWakeupRTC;
        long j;
        ArrayMap<String, BroadcastStats> uidStats;
        BroadcastStats bs;
        int i;
        synchronized (this.mLock) {
            pw.println("Current Alarm Manager state:");
            this.mConstants.dump(pw, "  ");
            pw.println();
            if (this.mAppStateTracker != null) {
                this.mAppStateTracker.dump(pw, "  ");
                pw.println();
            }
            pw.println("  App Standby Parole: " + this.mAppStandbyParole);
            pw.println();
            long nowELAPSED = this.mInjector.getElapsedRealtime();
            long nowUPTIME = SystemClock.uptimeMillis();
            long nowRTC = this.mInjector.getCurrentTimeMillis();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            pw.print("  nowRTC=");
            pw.print(nowRTC);
            pw.print("=");
            pw.print(sdf2.format(new Date(nowRTC)));
            pw.print(" nowELAPSED=");
            pw.print(nowELAPSED);
            pw.println();
            pw.print("  mLastTimeChangeClockTime=");
            pw.print(this.mLastTimeChangeClockTime);
            pw.print("=");
            pw.println(sdf2.format(new Date(this.mLastTimeChangeClockTime)));
            pw.print("  mLastTimeChangeRealtime=");
            pw.println(this.mLastTimeChangeRealtime);
            pw.print("  mLastTickReceived=");
            pw.println(sdf2.format(new Date(this.mLastTickReceived)));
            pw.print("  mLastTickSet=");
            pw.println(sdf2.format(new Date(this.mLastTickSet)));
            pw.print("  mLastTickAdded=");
            pw.println(sdf2.format(new Date(this.mLastTickAdded)));
            pw.print("  mLastTickRemoved=");
            pw.println(sdf2.format(new Date(this.mLastTickRemoved)));
            pw.println();
            pw.println("  Recent TIME_TICK history:");
            int i2 = this.mNextTickHistory;
            while (true) {
                i2--;
                if (i2 < 0) {
                    i2 = 9;
                }
                long time = this.mTickHistory[i2];
                pw.print("    ");
                if (time > 0) {
                    str = sdf2.format(new Date(nowRTC - (nowELAPSED - time)));
                } else {
                    str = "-";
                }
                pw.println(str);
                if (i2 == this.mNextTickHistory) {
                    break;
                }
            }
            SystemServiceManager ssm = (SystemServiceManager) LocalServices.getService(SystemServiceManager.class);
            if (ssm != null) {
                pw.println();
                pw.print("  RuntimeStarted=");
                pw.print(sdf2.format(new Date((nowRTC - nowELAPSED) + ssm.getRuntimeStartElapsedTime())));
                if (ssm.isRuntimeRestarted()) {
                    pw.print("  (Runtime restarted)");
                }
                pw.println();
                pw.print("  Runtime uptime (elapsed): ");
                TimeUtils.formatDuration(nowELAPSED, ssm.getRuntimeStartElapsedTime(), pw);
                pw.println();
                pw.print("  Runtime uptime (uptime): ");
                TimeUtils.formatDuration(nowUPTIME, ssm.getRuntimeStartUptime(), pw);
                pw.println();
            }
            pw.println();
            if (!this.mInteractive) {
                pw.print("  Time since non-interactive: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNonInteractiveStartTime, pw);
                pw.println();
            }
            pw.print("  Max wakeup delay: ");
            TimeUtils.formatDuration(currentNonWakeupFuzzLocked(nowELAPSED), pw);
            pw.println();
            pw.print("  Time since last dispatch: ");
            TimeUtils.formatDuration(nowELAPSED - this.mLastAlarmDeliveryTime, pw);
            pw.println();
            pw.print("  Next non-wakeup delivery time: ");
            TimeUtils.formatDuration(this.mNextNonWakeupDeliveryTime, nowELAPSED, pw);
            pw.println();
            long nextWakeupRTC = this.mNextWakeup + (nowRTC - nowELAPSED);
            long nextNonWakeupRTC2 = this.mNextNonWakeup + (nowRTC - nowELAPSED);
            pw.print("  Next non-wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextNonWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.print(this.mNextNonWakeup);
            pw.print(" = ");
            pw.println(sdf2.format(new Date(nextNonWakeupRTC2)));
            pw.print("    set at ");
            TimeUtils.formatDuration(this.mNextNonWakeUpSetAt, nowELAPSED, pw);
            pw.println();
            pw.print("  Next wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.print(this.mNextWakeup);
            pw.print(" = ");
            pw.println(sdf2.format(new Date(nextWakeupRTC)));
            pw.print("    set at ");
            TimeUtils.formatDuration(this.mNextWakeUpSetAt, nowELAPSED, pw);
            pw.println();
            pw.print("  Next kernel non-wakeup alarm: ");
            TimeUtils.formatDuration(this.mInjector.getNextAlarm(3), pw);
            pw.println();
            pw.print("  Next kernel wakeup alarm: ");
            TimeUtils.formatDuration(this.mInjector.getNextAlarm(2), pw);
            pw.println();
            pw.print("  Last wakeup: ");
            TimeUtils.formatDuration(this.mLastWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(this.mLastWakeup);
            pw.print("  Last trigger: ");
            TimeUtils.formatDuration(this.mLastTrigger, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(this.mLastTrigger);
            pw.print("  Num time change events: ");
            pw.println(this.mNumTimeChanged);
            pw.println();
            pw.println("  Next alarm clock information: ");
            TreeSet<Integer> users = new TreeSet<>();
            for (int i3 = 0; i3 < this.mNextAlarmClockForUser.size(); i3++) {
                users.add(Integer.valueOf(this.mNextAlarmClockForUser.keyAt(i3)));
            }
            for (int i4 = 0; i4 < this.mPendingSendNextAlarmClockChangedForUser.size(); i4++) {
                users.add(Integer.valueOf(this.mPendingSendNextAlarmClockChangedForUser.keyAt(i4)));
            }
            Iterator<Integer> it = users.iterator();
            while (it.hasNext()) {
                int user = it.next().intValue();
                AlarmManager.AlarmClockInfo next = this.mNextAlarmClockForUser.get(user);
                long time2 = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = this.mPendingSendNextAlarmClockChangedForUser.get(user);
                pw.print("    user:");
                pw.print(user);
                pw.print(" pendingSend:");
                pw.print(pendingSend);
                pw.print(" time:");
                pw.print(time2);
                if (time2 > 0) {
                    pw.print(" = ");
                    pw.print(sdf2.format(new Date(time2)));
                    pw.print(" = ");
                    TimeUtils.formatDuration(time2, nowRTC, pw);
                }
                pw.println();
                it = it;
                nextNonWakeupRTC2 = nextNonWakeupRTC2;
            }
            long nowRTC2 = nextNonWakeupRTC2;
            long nextWakeupRTC2 = 0;
            if (this.mAlarmBatches.size() > 0) {
                pw.println();
                pw.print("  Pending alarm batches: ");
                pw.println(this.mAlarmBatches.size());
                for (Iterator<Batch> it2 = this.mAlarmBatches.iterator(); it2.hasNext(); it2 = it2) {
                    Batch b = it2.next();
                    pw.print(b);
                    pw.println(':');
                    dumpAlarmList(pw, b.alarms, "    ", nowELAPSED, nowRTC, sdf2);
                    nextWakeupRTC = nextWakeupRTC;
                    nowRTC = nowRTC;
                    sdf2 = sdf2;
                    nextWakeupRTC2 = nextWakeupRTC2;
                    users = users;
                    nowRTC2 = nowRTC2;
                }
                sdf = sdf2;
                j = nextWakeupRTC2;
                nextNonWakeupRTC = nowRTC;
            } else {
                sdf = sdf2;
                j = 0;
                nextNonWakeupRTC = nowRTC;
            }
            pw.println();
            pw.println("  Pending user blocked background alarms: ");
            boolean blocked = false;
            int i5 = 0;
            while (i5 < this.mPendingBackgroundAlarms.size()) {
                ArrayList<Alarm> blockedAlarms = this.mPendingBackgroundAlarms.valueAt(i5);
                if (blockedAlarms == null || blockedAlarms.size() <= 0) {
                    i = i5;
                } else {
                    blocked = true;
                    i = i5;
                    dumpAlarmList(pw, blockedAlarms, "    ", nowELAPSED, nextNonWakeupRTC, sdf);
                }
                i5 = i + 1;
            }
            if (!blocked) {
                pw.println("    none");
            }
            pw.println();
            pw.print("  Pending alarms per uid: [");
            for (int i6 = 0; i6 < this.mAlarmsPerUid.size(); i6++) {
                if (i6 > 0) {
                    pw.print(", ");
                }
                UserHandle.formatUid(pw, this.mAlarmsPerUid.keyAt(i6));
                pw.print(":");
                pw.print(this.mAlarmsPerUid.valueAt(i6));
            }
            pw.println("]");
            pw.println();
            this.mAppWakeupHistory.dump(pw, "  ", nowELAPSED);
            if (this.mPendingIdleUntil != null || this.mPendingWhileIdleAlarms.size() > 0) {
                pw.println();
                pw.println("    Idle mode state:");
                pw.print("      Idling until: ");
                if (this.mPendingIdleUntil != null) {
                    pw.println(this.mPendingIdleUntil);
                    this.mPendingIdleUntil.dump(pw, "        ", nowELAPSED, nextNonWakeupRTC, sdf);
                } else {
                    pw.println("null");
                }
                pw.println("      Pending alarms:");
                dumpAlarmList(pw, this.mPendingWhileIdleAlarms, "      ", nowELAPSED, nextNonWakeupRTC, sdf);
            }
            if (this.mNextWakeFromIdle != null) {
                pw.println();
                pw.print("  Next wake from idle: ");
                pw.println(this.mNextWakeFromIdle);
                this.mNextWakeFromIdle.dump(pw, "    ", nowELAPSED, nextNonWakeupRTC, sdf);
            }
            pw.println();
            pw.print("  Past-due non-wakeup alarms: ");
            if (this.mPendingNonWakeupAlarms.size() > 0) {
                pw.println(this.mPendingNonWakeupAlarms.size());
                dumpAlarmList(pw, this.mPendingNonWakeupAlarms, "    ", nowELAPSED, nextNonWakeupRTC, sdf);
            } else {
                pw.println("(none)");
            }
            pw.print("    Number of delayed alarms: ");
            pw.print(this.mNumDelayedAlarms);
            pw.print(", total delay time: ");
            TimeUtils.formatDuration(this.mTotalDelayTime, pw);
            pw.println();
            pw.print("    Max delay time: ");
            TimeUtils.formatDuration(this.mMaxDelayTime, pw);
            pw.print(", max non-interactive time: ");
            TimeUtils.formatDuration(this.mNonInteractiveTime, pw);
            pw.println();
            pw.println();
            pw.print("  Broadcast ref count: ");
            pw.println(this.mBroadcastRefCount);
            pw.print("  PendingIntent send count: ");
            pw.println(this.mSendCount);
            pw.print("  PendingIntent finish count: ");
            pw.println(this.mSendFinishCount);
            pw.print("  Listener send count: ");
            pw.println(this.mListenerCount);
            pw.print("  Listener finish count: ");
            pw.println(this.mListenerFinishCount);
            pw.println();
            if (this.mInFlight.size() > 0) {
                pw.println("Outstanding deliveries:");
                for (int i7 = 0; i7 < this.mInFlight.size(); i7++) {
                    if (this.mInFlight.get(i7) != null) {
                        pw.print("   #");
                        pw.print(i7);
                        pw.print(": ");
                        pw.print(this.mInFlight.get(i7));
                        pw.print("##" + this.mInFlight.get(i7).mUid);
                        pw.print("##" + this.mInFlight.get(i7).mTag);
                        pw.print("\n");
                    }
                }
                pw.println();
            }
            if (this.mLastAllowWhileIdleDispatch.size() > 0) {
                pw.println("  Last allow while idle dispatch times:");
                for (int i8 = 0; i8 < this.mLastAllowWhileIdleDispatch.size(); i8++) {
                    pw.print("    UID ");
                    int uid = this.mLastAllowWhileIdleDispatch.keyAt(i8);
                    UserHandle.formatUid(pw, uid);
                    pw.print(": ");
                    long lastTime = this.mLastAllowWhileIdleDispatch.valueAt(i8);
                    TimeUtils.formatDuration(lastTime, nowELAPSED, pw);
                    long minInterval = getWhileIdleMinIntervalLocked(uid);
                    pw.print("  Next allowed:");
                    TimeUtils.formatDuration(lastTime + minInterval, nowELAPSED, pw);
                    pw.print(" (");
                    TimeUtils.formatDuration(minInterval, j, pw);
                    pw.print(")");
                    pw.println();
                }
            }
            pw.print("  mUseAllowWhileIdleShortTime: [");
            for (int i9 = 0; i9 < this.mUseAllowWhileIdleShortTime.size(); i9++) {
                if (this.mUseAllowWhileIdleShortTime.valueAt(i9)) {
                    UserHandle.formatUid(pw, this.mUseAllowWhileIdleShortTime.keyAt(i9));
                    pw.print(" ");
                }
            }
            pw.println("]");
            pw.println();
            if (this.mLog.dump(pw, "  Recent problems", "    ")) {
                pw.println();
            }
            FilterStats[] topFilters = new FilterStats[10];
            Comparator<FilterStats> comparator = new Comparator<FilterStats>() {
                /* class com.android.server.AlarmManagerService.AnonymousClass4 */

                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            for (int iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                ArrayMap<String, BroadcastStats> uidStats2 = this.mBroadcastStats.valueAt(iu);
                for (int ip = 0; ip < uidStats2.size(); ip++) {
                    BroadcastStats bs2 = uidStats2.valueAt(ip);
                    int is = 0;
                    while (is < bs2.filterStats.size()) {
                        FilterStats fs = bs2.filterStats.valueAt(is);
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, 0, len, fs, comparator) : 0;
                        if (pos < 0) {
                            uidStats = uidStats2;
                            pos = (-pos) - 1;
                        } else {
                            uidStats = uidStats2;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                bs = bs2;
                                System.arraycopy(topFilters, pos, topFilters, pos + 1, copylen);
                            } else {
                                bs = bs2;
                            }
                            topFilters[pos] = fs;
                            if (len < topFilters.length) {
                                len++;
                            }
                        } else {
                            bs = bs2;
                        }
                        is++;
                        uidStats2 = uidStats;
                        bs2 = bs;
                    }
                }
            }
            if (len > 0) {
                pw.println("  Top Alarms:");
                for (int i10 = 0; i10 < len; i10++) {
                    FilterStats fs2 = topFilters[i10];
                    pw.print("    ");
                    if (fs2.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    TimeUtils.formatDuration(fs2.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(fs2.numWakeup);
                    pw.print(" wakeups, ");
                    pw.print(fs2.count);
                    pw.print(" alarms: ");
                    UserHandle.formatUid(pw, fs2.mBroadcastStats.mUid);
                    pw.print(":");
                    pw.print(fs2.mBroadcastStats.mPackageName);
                    pw.println();
                    pw.print("      ");
                    pw.print(fs2.mTag);
                    pw.println();
                }
            }
            pw.println(" ");
            pw.println("  Alarm Stats:");
            ArrayList<FilterStats> tmpFilters = new ArrayList<>();
            for (int iu2 = 0; iu2 < this.mBroadcastStats.size(); iu2++) {
                ArrayMap<String, BroadcastStats> uidStats3 = this.mBroadcastStats.valueAt(iu2);
                for (int ip2 = 0; ip2 < uidStats3.size(); ip2++) {
                    BroadcastStats bs3 = uidStats3.valueAt(ip2);
                    pw.print("  ");
                    if (bs3.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    UserHandle.formatUid(pw, bs3.mUid);
                    pw.print(":");
                    pw.print(bs3.mPackageName);
                    pw.print(" ");
                    TimeUtils.formatDuration(bs3.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(bs3.numWakeup);
                    pw.println(" wakeups:");
                    tmpFilters.clear();
                    for (int is2 = 0; is2 < bs3.filterStats.size(); is2++) {
                        tmpFilters.add(bs3.filterStats.valueAt(is2));
                    }
                    Collections.sort(tmpFilters, comparator);
                    int i11 = 0;
                    while (i11 < tmpFilters.size()) {
                        FilterStats fs3 = tmpFilters.get(i11);
                        pw.print("    ");
                        if (fs3.nesting > 0) {
                            pw.print("*ACTIVE* ");
                        }
                        TimeUtils.formatDuration(fs3.aggregateTime, pw);
                        pw.print(" ");
                        pw.print(fs3.numWakeup);
                        pw.print(" wakes ");
                        pw.print(fs3.count);
                        pw.print(" alarms, last ");
                        TimeUtils.formatDuration(fs3.lastTime, nowELAPSED, pw);
                        pw.println(":");
                        pw.print("      ");
                        pw.print(fs3.mTag);
                        pw.println();
                        i11++;
                        topFilters = topFilters;
                        comparator = comparator;
                    }
                }
            }
            pw.println();
            this.mStatLogger.dump(pw, "  ");
            printHwWakeupBoot(pw);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpProto(FileDescriptor fd) {
        TreeSet<Integer> users;
        int i;
        AlarmManagerService alarmManagerService = this;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (alarmManagerService.mLock) {
            long nowRTC = alarmManagerService.mInjector.getCurrentTimeMillis();
            long nowElapsed = alarmManagerService.mInjector.getElapsedRealtime();
            proto.write(1112396529665L, nowRTC);
            proto.write(1112396529666L, nowElapsed);
            proto.write(1112396529667L, alarmManagerService.mLastTimeChangeClockTime);
            proto.write(1112396529668L, alarmManagerService.mLastTimeChangeRealtime);
            alarmManagerService.mConstants.dumpProto(proto, 1146756268037L);
            if (alarmManagerService.mAppStateTracker != null) {
                alarmManagerService.mAppStateTracker.dumpProto(proto, 1146756268038L);
            }
            proto.write(1133871366151L, alarmManagerService.mInteractive);
            if (!alarmManagerService.mInteractive) {
                proto.write(1112396529672L, nowElapsed - alarmManagerService.mNonInteractiveStartTime);
                proto.write(1112396529673L, alarmManagerService.currentNonWakeupFuzzLocked(nowElapsed));
                proto.write(1112396529674L, nowElapsed - alarmManagerService.mLastAlarmDeliveryTime);
                proto.write(1112396529675L, nowElapsed - alarmManagerService.mNextNonWakeupDeliveryTime);
            }
            proto.write(1112396529676L, alarmManagerService.mNextNonWakeup - nowElapsed);
            proto.write(1112396529677L, alarmManagerService.mNextWakeup - nowElapsed);
            proto.write(1112396529678L, nowElapsed - alarmManagerService.mLastWakeup);
            proto.write(1112396529679L, nowElapsed - alarmManagerService.mNextWakeUpSetAt);
            proto.write(1112396529680L, alarmManagerService.mNumTimeChanged);
            TreeSet<Integer> users2 = new TreeSet<>();
            int nextAlarmClockForUserSize = alarmManagerService.mNextAlarmClockForUser.size();
            for (int i2 = 0; i2 < nextAlarmClockForUserSize; i2++) {
                users2.add(Integer.valueOf(alarmManagerService.mNextAlarmClockForUser.keyAt(i2)));
            }
            int pendingSendNextAlarmClockChangedForUserSize = alarmManagerService.mPendingSendNextAlarmClockChangedForUser.size();
            for (int i3 = 0; i3 < pendingSendNextAlarmClockChangedForUserSize; i3++) {
                users2.add(Integer.valueOf(alarmManagerService.mPendingSendNextAlarmClockChangedForUser.keyAt(i3)));
            }
            for (Iterator<Integer> it = users2.iterator(); it.hasNext(); it = it) {
                int user = it.next().intValue();
                AlarmManager.AlarmClockInfo next = alarmManagerService.mNextAlarmClockForUser.get(user);
                long time = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = alarmManagerService.mPendingSendNextAlarmClockChangedForUser.get(user);
                long aToken = proto.start(2246267895826L);
                proto.write(1120986464257L, user);
                proto.write(1133871366146L, pendingSend);
                proto.write(1112396529667L, time);
                proto.end(aToken);
                nextAlarmClockForUserSize = nextAlarmClockForUserSize;
                nowRTC = nowRTC;
            }
            int pendingSendNextAlarmClockChangedForUserSize2 = nextAlarmClockForUserSize;
            long j = 1120986464257L;
            Iterator<Batch> it2 = alarmManagerService.mAlarmBatches.iterator();
            while (it2.hasNext()) {
                it2.next().writeToProto(proto, 2246267895827L, nowElapsed, nowRTC);
                j = j;
                pendingSendNextAlarmClockChangedForUserSize = pendingSendNextAlarmClockChangedForUserSize;
                nowElapsed = nowElapsed;
                pendingSendNextAlarmClockChangedForUserSize2 = pendingSendNextAlarmClockChangedForUserSize2;
            }
            long j2 = j;
            int i4 = 0;
            int i5 = 0;
            while (i5 < alarmManagerService.mPendingBackgroundAlarms.size()) {
                ArrayList<Alarm> blockedAlarms = alarmManagerService.mPendingBackgroundAlarms.valueAt(i5);
                if (blockedAlarms != null) {
                    Iterator<Alarm> it3 = blockedAlarms.iterator();
                    while (it3.hasNext()) {
                        it3.next().writeToProto(proto, 2246267895828L, nowElapsed, nowRTC);
                        i5 = i5;
                    }
                    i = i5;
                } else {
                    i = i5;
                }
                i5 = i + 1;
            }
            if (alarmManagerService.mPendingIdleUntil != null) {
                alarmManagerService.mPendingIdleUntil.writeToProto(proto, 1146756268053L, nowElapsed, nowRTC);
            }
            Iterator<Alarm> it4 = alarmManagerService.mPendingWhileIdleAlarms.iterator();
            while (it4.hasNext()) {
                it4.next().writeToProto(proto, 2246267895830L, nowElapsed, nowRTC);
            }
            if (alarmManagerService.mNextWakeFromIdle != null) {
                alarmManagerService.mNextWakeFromIdle.writeToProto(proto, 1146756268055L, nowElapsed, nowRTC);
            }
            Iterator<Alarm> it5 = alarmManagerService.mPendingNonWakeupAlarms.iterator();
            while (it5.hasNext()) {
                it5.next().writeToProto(proto, 2246267895832L, nowElapsed, nowRTC);
            }
            proto.write(1120986464281L, alarmManagerService.mNumDelayedAlarms);
            proto.write(1112396529690L, alarmManagerService.mTotalDelayTime);
            proto.write(1112396529691L, alarmManagerService.mMaxDelayTime);
            proto.write(1112396529692L, alarmManagerService.mNonInteractiveTime);
            proto.write(1120986464285L, alarmManagerService.mBroadcastRefCount);
            proto.write(1120986464286L, alarmManagerService.mSendCount);
            proto.write(1120986464287L, alarmManagerService.mSendFinishCount);
            proto.write(1120986464288L, alarmManagerService.mListenerCount);
            proto.write(1120986464289L, alarmManagerService.mListenerFinishCount);
            Iterator<InFlight> it6 = alarmManagerService.mInFlight.iterator();
            while (it6.hasNext()) {
                it6.next().writeToProto(proto, 2246267895842L);
            }
            int i6 = 0;
            while (i6 < alarmManagerService.mLastAllowWhileIdleDispatch.size()) {
                long token = proto.start(2246267895844L);
                int uid = alarmManagerService.mLastAllowWhileIdleDispatch.keyAt(i6);
                long lastTime = alarmManagerService.mLastAllowWhileIdleDispatch.valueAt(i6);
                proto.write(j2, uid);
                proto.write(1112396529666L, lastTime);
                proto.write(1112396529667L, lastTime + alarmManagerService.getWhileIdleMinIntervalLocked(uid));
                proto.end(token);
                i6++;
                j2 = 1120986464257L;
            }
            for (int i7 = 0; i7 < alarmManagerService.mUseAllowWhileIdleShortTime.size(); i7++) {
                if (alarmManagerService.mUseAllowWhileIdleShortTime.valueAt(i7)) {
                    proto.write(2220498092067L, alarmManagerService.mUseAllowWhileIdleShortTime.keyAt(i7));
                }
            }
            alarmManagerService.mLog.writeToProto(proto, 1146756268069L);
            FilterStats[] topFilters = new FilterStats[10];
            Comparator<FilterStats> comparator = new Comparator<FilterStats>() {
                /* class com.android.server.AlarmManagerService.AnonymousClass5 */

                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            int iu = 0;
            while (iu < alarmManagerService.mBroadcastStats.size()) {
                ArrayMap<String, BroadcastStats> uidStats = alarmManagerService.mBroadcastStats.valueAt(iu);
                int ip = i4;
                while (ip < uidStats.size()) {
                    BroadcastStats bs = uidStats.valueAt(ip);
                    int is = i4;
                    while (is < bs.filterStats.size()) {
                        FilterStats fs = bs.filterStats.valueAt(is);
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, i4, len, fs, comparator) : i4;
                        if (pos < 0) {
                            pos = (-pos) - 1;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                users = users2;
                                System.arraycopy(topFilters, pos, topFilters, pos + 1, copylen);
                            } else {
                                users = users2;
                            }
                            topFilters[pos] = fs;
                            if (len < topFilters.length) {
                                len++;
                            }
                        } else {
                            users = users2;
                        }
                        is++;
                        users2 = users;
                        i4 = 0;
                    }
                    ip++;
                    i4 = 0;
                }
                iu++;
                i4 = 0;
            }
            for (int i8 = 0; i8 < len; i8++) {
                long token2 = proto.start(2246267895846L);
                FilterStats fs2 = topFilters[i8];
                proto.write(1120986464257L, fs2.mBroadcastStats.mUid);
                proto.write(1138166333442L, fs2.mBroadcastStats.mPackageName);
                fs2.writeToProto(proto, 1146756268035L);
                proto.end(token2);
            }
            ArrayList<FilterStats> tmpFilters = new ArrayList<>();
            int iu2 = 0;
            while (iu2 < alarmManagerService.mBroadcastStats.size()) {
                ArrayMap<String, BroadcastStats> uidStats2 = alarmManagerService.mBroadcastStats.valueAt(iu2);
                int ip2 = 0;
                while (ip2 < uidStats2.size()) {
                    long token3 = proto.start(2246267895847L);
                    BroadcastStats bs2 = uidStats2.valueAt(ip2);
                    bs2.writeToProto(proto, 1146756268033L);
                    tmpFilters.clear();
                    for (int is2 = 0; is2 < bs2.filterStats.size(); is2++) {
                        tmpFilters.add(bs2.filterStats.valueAt(is2));
                    }
                    Collections.sort(tmpFilters, comparator);
                    Iterator<FilterStats> it7 = tmpFilters.iterator();
                    while (it7.hasNext()) {
                        it7.next().writeToProto(proto, 2246267895810L);
                        tmpFilters = tmpFilters;
                    }
                    proto.end(token3);
                    ip2++;
                    tmpFilters = tmpFilters;
                }
                iu2++;
                alarmManagerService = this;
            }
        }
        proto.flush();
    }

    private void logBatchesLocked(SimpleDateFormat sdf) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream(2048);
        PrintWriter pw = new PrintWriter(bs);
        long nowRTC = this.mInjector.getCurrentTimeMillis();
        long nowELAPSED = this.mInjector.getElapsedRealtime();
        int NZ = this.mAlarmBatches.size();
        for (int iz = 0; iz < NZ; iz++) {
            Batch bz = this.mAlarmBatches.get(iz);
            pw.append((CharSequence) "Batch ");
            pw.print(iz);
            pw.append((CharSequence) ": ");
            pw.println(bz);
            dumpAlarmList(pw, bz.alarms, "  ", nowELAPSED, nowRTC, sdf);
            pw.flush();
            Slog.v(TAG, bs.toString());
            bs.reset();
        }
    }

    private boolean validateConsistencyLocked() {
        return true;
    }

    private Batch findFirstWakeupBatchLocked() {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            try {
                Batch b = this.mAlarmBatches.get(i);
                if (b.hasWakeups()) {
                    return b;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Do nothing");
            }
        }
        return null;
    }

    private long findFirstWakeupEndLocked() {
        long lastWakeupEnd = -1;
        Iterator<Batch> it = this.mAlarmBatches.iterator();
        while (it.hasNext()) {
            Batch batch = it.next();
            try {
                if (batch.hasWakeups()) {
                    int size = batch.alarms.size();
                    for (int index = 0; index < size; index++) {
                        Alarm alarm = batch.alarms.get(index);
                        if (alarm.wakeup) {
                            if (lastWakeupEnd == -1) {
                                lastWakeupEnd = alarm.maxWhenElapsed > batch.start ? alarm.maxWhenElapsed : batch.start;
                            } else if (alarm.whenElapsed > lastWakeupEnd) {
                                return lastWakeupEnd;
                            } else {
                                if (alarm.maxWhenElapsed < lastWakeupEnd) {
                                    lastWakeupEnd = alarm.maxWhenElapsed > batch.start ? alarm.maxWhenElapsed : batch.start;
                                }
                            }
                        }
                    }
                    continue;
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Do nothing");
            }
        }
        return lastWakeupEnd;
    }

    /* access modifiers changed from: package-private */
    public long getNextWakeFromIdleTimeImpl() {
        long j;
        synchronized (this.mLock) {
            j = this.mNextWakeFromIdle != null ? this.mNextWakeFromIdle.whenElapsed : JobStatus.NO_LATEST_RUNTIME;
        }
        return j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isIdlingImpl() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPendingIdleUntil != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public AlarmManager.AlarmClockInfo getNextAlarmClockImpl(int userId) {
        AlarmManager.AlarmClockInfo alarmClockInfo;
        synchronized (this.mLock) {
            alarmClockInfo = this.mNextAlarmClockForUser.get(userId);
        }
        return alarmClockInfo;
    }

    /* access modifiers changed from: package-private */
    public void updateNextAlarmClockLocked() {
        if (this.mNextAlarmClockMayChange) {
            this.mNextAlarmClockMayChange = false;
            SparseArray<AlarmManager.AlarmClockInfo> nextForUser = this.mTmpSparseAlarmClockArray;
            nextForUser.clear();
            int N = this.mAlarmBatches.size();
            for (int i = 0; i < N; i++) {
                ArrayList<Alarm> alarms = this.mAlarmBatches.get(i).alarms;
                int M = alarms.size();
                for (int j = 0; j < M; j++) {
                    Alarm a = alarms.get(j);
                    if (a.alarmClock != null) {
                        int userId = UserHandle.getUserId(a.uid);
                        AlarmManager.AlarmClockInfo current = this.mNextAlarmClockForUser.get(userId);
                        if (nextForUser.get(userId) == null) {
                            nextForUser.put(userId, a.alarmClock);
                        } else if (a.alarmClock.equals(current) && current.getTriggerTime() <= nextForUser.get(userId).getTriggerTime()) {
                            nextForUser.put(userId, current);
                        }
                    }
                }
            }
            int NN = nextForUser.size();
            for (int i2 = 0; i2 < NN; i2++) {
                AlarmManager.AlarmClockInfo newAlarm = nextForUser.valueAt(i2);
                int userId2 = nextForUser.keyAt(i2);
                if (!newAlarm.equals(this.mNextAlarmClockForUser.get(userId2))) {
                    updateNextAlarmInfoForUserLocked(userId2, newAlarm);
                }
            }
            for (int i3 = this.mNextAlarmClockForUser.size() - 1; i3 >= 0; i3--) {
                int userId3 = this.mNextAlarmClockForUser.keyAt(i3);
                if (nextForUser.get(userId3) == null) {
                    updateNextAlarmInfoForUserLocked(userId3, null);
                }
            }
        }
    }

    private void updateNextAlarmInfoForUserLocked(int userId, AlarmManager.AlarmClockInfo alarmClock) {
        if (alarmClock != null) {
            this.mNextAlarmClockForUser.put(userId, alarmClock);
        } else {
            this.mNextAlarmClockForUser.remove(userId);
        }
        this.mPendingSendNextAlarmClockChangedForUser.put(userId, true);
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNextAlarmClockChanged() {
        SparseArray<AlarmManager.AlarmClockInfo> pendingUsers = this.mHandlerSparseAlarmClockArray;
        pendingUsers.clear();
        synchronized (this.mLock) {
            int N = this.mPendingSendNextAlarmClockChangedForUser.size();
            for (int i = 0; i < N; i++) {
                int userId = this.mPendingSendNextAlarmClockChangedForUser.keyAt(i);
                pendingUsers.append(userId, this.mNextAlarmClockForUser.get(userId));
            }
            this.mPendingSendNextAlarmClockChangedForUser.clear();
        }
        int N2 = pendingUsers.size();
        for (int i2 = 0; i2 < N2; i2++) {
            int userId2 = pendingUsers.keyAt(i2);
            Settings.System.putStringForUser(getContext().getContentResolver(), "next_alarm_formatted", formatNextAlarm(getContext(), pendingUsers.valueAt(i2), userId2), userId2);
            getContext().sendBroadcastAsUser(NEXT_ALARM_CLOCK_CHANGED_INTENT, new UserHandle(userId2));
        }
    }

    private static String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo info, int userId) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, userId) ? "EHm" : "Ehma");
        if (info == null) {
            return "";
        }
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    /* access modifiers changed from: package-private */
    public void rescheduleKernelAlarmsLocked() {
        long nowElapsed = this.mInjector.getElapsedRealtime();
        long nextNonWakeup = 0;
        if (this.mAlarmBatches.size() > 0) {
            Batch firstBatch = this.mAlarmBatches.get(0);
            if (this.mIsAlarmDataOnlyMode || !isAwareAlarmManagerEnabled()) {
                Batch firstWakeup = findFirstWakeupBatchLocked();
                if (firstWakeup != null) {
                    this.mNextWakeup = firstWakeup.start;
                    this.mNextWakeUpSetAt = nowElapsed;
                    setLocked(2, firstWakeup.start);
                }
                if (firstBatch != firstWakeup) {
                    nextNonWakeup = firstBatch.start;
                }
            } else {
                long firstWakeupEnd = findFirstWakeupEndLocked();
                if (firstWakeupEnd != -1) {
                    this.mNextWakeup = firstWakeupEnd;
                    this.mNextWakeUpSetAt = nowElapsed;
                    setLocked(2, firstWakeupEnd);
                }
                if (firstBatch.start != firstWakeupEnd) {
                    nextNonWakeup = firstBatch.start;
                }
            }
        }
        if (this.mPendingNonWakeupAlarms.size() > 0 && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (nextNonWakeup == 0) {
            return;
        }
        if (!"factory".equals(SystemProperties.get("ro.runmode", NORMAL_MODE)) || nextNonWakeup != this.mNextWakeup) {
            this.mNextNonWakeup = nextNonWakeup;
            this.mNextNonWakeUpSetAt = nowElapsed;
            setLocked(3, nextNonWakeup);
            return;
        }
        Flog.w((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "no need set for the time had been set by type 2");
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(PendingIntent operation, IAlarmListener directReceiver) {
        if (!(operation == null && directReceiver == null)) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(operation, directReceiver) {
                /* class com.android.server.$$Lambda$AlarmManagerService$PL9HLHMH3N2JfJ2qjsXOF25AR4 */
                private final /* synthetic */ PendingIntent f$0;
                private final /* synthetic */ IAlarmListener f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$1(this.f$0, this.f$1, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                if (b != null) {
                    didRemove |= b.remove(whichAlarms, false);
                    if (b.size() == 0) {
                        this.mAlarmBatches.remove(i);
                    }
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm alarm = this.mPendingWhileIdleAlarms.get(i2);
                if (alarm.matches(operation, directReceiver)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(alarm.uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending idle:" + alarm + " by op:" + operation + ", rec:" + directReceiver);
                    this.mHandler.obtainMessage(8, alarm.operation).sendToTarget();
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    Alarm alarm2 = alarmsForUid.get(j);
                    if (alarm2.matches(operation, directReceiver)) {
                        alarmsForUid.remove(j);
                        decrementAlarmCount(alarm2.uid, 1);
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending background:" + alarm2 + " by op:" + operation + ", rec:" + directReceiver);
                        this.mHandler.obtainMessage(8, alarm2.operation).sendToTarget();
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (didRemove) {
                boolean restorePending = false;
                Alarm alarm3 = this.mPendingIdleUntil;
                if (alarm3 != null && alarm3.matches(operation, directReceiver)) {
                    Slog.i(TAG, "Idle alarm removed");
                    this.mPendingIdleUntil = null;
                    restorePending = true;
                }
                Alarm alarm4 = this.mNextWakeFromIdle;
                if (alarm4 != null && alarm4.matches(operation, directReceiver)) {
                    this.mNextWakeFromIdle = null;
                }
                rebatchAllAlarmsLocked(true);
                if (restorePending) {
                    restorePendingWhileIdleAlarmsLocked();
                }
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$1(PendingIntent operation, IAlarmListener directReceiver, Alarm a) {
        if (!a.matches(operation, directReceiver) && !HwActivityManagerServiceUtil.isPendingIntentCanceled(a.operation)) {
            return false;
        }
        if (operation == null) {
            return true;
        }
        if (a.packageName != null && a.packageName.equals("com.google.android.gms")) {
            return true;
        }
        Slog.i(TAG, "remove alarm:" + a + " according to operation:" + Integer.toHexString(System.identityHashCode(operation)));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(int uid) {
        if (uid != 1000) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(uid) {
                /* class com.android.server.$$Lambda$AlarmManagerService$B3QlPiufJjUxYk_gYP8zaqzjvc */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$2(this.f$0, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms, false);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm a = this.mPendingWhileIdleAlarms.get(i2);
                if (a.uid == uid) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending idle:" + a + " by uid:" + uid);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    if (alarmsForUid.get(j).uid == uid) {
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending background:" + alarmsForUid.get(j) + " by uid:" + uid);
                        alarmsForUid.remove(j);
                        decrementAlarmCount(uid, 1);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            Alarm alarm = this.mNextWakeFromIdle;
            if (alarm != null && alarm.uid == uid) {
                this.mNextWakeFromIdle = null;
            }
            Alarm alarm2 = this.mPendingIdleUntil;
            if (alarm2 != null && alarm2.uid == uid) {
                Slog.wtf(TAG, "Removed app uid " + uid + " set idle-until alarm!");
                this.mPendingIdleUntil = null;
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$2(int uid, Alarm a) {
        if (a.uid != uid) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to uid:" + uid);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(String packageName) {
        if (!(packageName == null || PackageManagerService.PLATFORM_PACKAGE_NAME.equals(packageName))) {
            boolean didRemove = false;
            MutableBoolean removedNextWakeFromIdle = new MutableBoolean(false);
            Predicate<Alarm> whichAlarms = new Predicate(packageName, removedNextWakeFromIdle) {
                /* class com.android.server.$$Lambda$AlarmManagerService$iI87lOgeHXMagii4XiFcLdtBJ6Y */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ MutableBoolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.this.lambda$removeLocked$3$AlarmManagerService(this.f$1, this.f$2, (AlarmManagerService.Alarm) obj);
                }
            };
            boolean oldHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches);
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms, false);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            boolean newHasTick = haveBatchesTimeTickAlarm(this.mAlarmBatches);
            if (oldHasTick != newHasTick) {
                Slog.wtf(TAG, "removeLocked: hasTick changed from " + oldHasTick + " to " + newHasTick);
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm a = this.mPendingWhileIdleAlarms.get(i2);
                if (a.matches(packageName)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(a.uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending idle:" + a + " by pkg:" + packageName);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    Alarm alarm = alarmsForUid.get(j);
                    if (alarm.matches(packageName)) {
                        alarmsForUid.remove(j);
                        decrementAlarmCount(alarm.uid, 1);
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending background:" + alarm + " by pkg:" + packageName);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (removedNextWakeFromIdle.value) {
                this.mNextWakeFromIdle = null;
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    public /* synthetic */ boolean lambda$removeLocked$3$AlarmManagerService(String packageName, MutableBoolean removedNextWakeFromIdle, Alarm a) {
        boolean didMatch = a.matches(packageName);
        if (didMatch && a == this.mNextWakeFromIdle) {
            removedNextWakeFromIdle.value = true;
        }
        if (didMatch) {
            Slog.i(TAG, "remove " + a + " according to packageName:" + packageName);
        }
        return didMatch;
    }

    /* access modifiers changed from: package-private */
    public void removeLocked(int uid, String packageName) {
        if (uid == 1000) {
            Slog.wtf(TAG, "removeLocked: Shouldn't for UID=" + uid);
        } else if (packageName != null && !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(packageName)) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(uid, packageName) {
                /* class com.android.server.$$Lambda$AlarmManagerService$s2uh_HzGNUxv7Fzi4jjgJR2ZYno */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ String f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeLocked$4(this.f$0, this.f$1, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms, false);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm a = this.mPendingWhileIdleAlarms.get(i2);
                if (a.uid == uid && a.matches(packageName)) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending idle:" + a + " by uid:" + uid + ", pkg:" + packageName);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                ArrayList<Alarm> alarmsForUid = this.mPendingBackgroundAlarms.valueAt(i3);
                for (int j = alarmsForUid.size() - 1; j >= 0; j--) {
                    if (alarmsForUid.get(j).uid == uid && alarmsForUid.get(j).matches(packageName)) {
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending background:" + alarmsForUid.get(j) + " by uid:" + uid + ", pkg:" + packageName);
                        alarmsForUid.remove(j);
                        decrementAlarmCount(uid, 1);
                    }
                }
                if (alarmsForUid.size() == 0) {
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeLocked$4(int uid, String packageName, Alarm a) {
        if (a.uid != uid || !a.matches(packageName)) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to " + packageName + " and " + uid);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void removeForStoppedLocked(int uid) {
        if (uid != 1000) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(uid) {
                /* class com.android.server.$$Lambda$AlarmManagerService$zUTwHhQWGW0Wt9psKr9uEwmkdDE */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeForStoppedLocked$5(this.f$0, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms, false);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                Alarm a = this.mPendingWhileIdleAlarms.get(i2);
                if (a.uid == uid) {
                    this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "removeForStoppedLocked pending idle:" + a + " by uid:" + uid);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                if (this.mPendingBackgroundAlarms.keyAt(i3) == uid) {
                    ArrayList<Alarm> toRemove = this.mPendingBackgroundAlarms.valueAt(i3);
                    if (toRemove != null) {
                        decrementAlarmCount(uid, toRemove.size());
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "removeForStoppedLocked pending background size" + toRemove.size() + ", by uid:" + uid);
                    }
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeForStoppedLocked$5(int uid, Alarm a) {
        try {
            if (a.uid != uid || !ActivityManager.getService().isAppStartModeDisabled(uid, a.packageName)) {
                return false;
            }
            Slog.i(TAG, "removeForStoppedLocked " + a + " according to uid:" + uid);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeUserLocked(int userHandle) {
        if (userHandle != 0) {
            boolean didRemove = false;
            Predicate<Alarm> whichAlarms = new Predicate(userHandle) {
                /* class com.android.server.$$Lambda$AlarmManagerService$UjDsLHpWLEiLO89woFscD08eUms */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AlarmManagerService.lambda$removeUserLocked$6(this.f$0, (AlarmManagerService.Alarm) obj);
                }
            };
            for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
                Batch b = this.mAlarmBatches.get(i);
                didRemove |= b.remove(whichAlarms, false);
                if (b.size() == 0) {
                    this.mAlarmBatches.remove(i);
                }
            }
            for (int i2 = this.mPendingWhileIdleAlarms.size() - 1; i2 >= 0; i2--) {
                if (UserHandle.getUserId(this.mPendingWhileIdleAlarms.get(i2).creatorUid) == userHandle) {
                    Alarm removed = this.mPendingWhileIdleAlarms.remove(i2);
                    decrementAlarmCount(removed.uid, 1);
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending idle:" + removed + " by userHandle:" + userHandle);
                }
            }
            for (int i3 = this.mPendingBackgroundAlarms.size() - 1; i3 >= 0; i3--) {
                if (UserHandle.getUserId(this.mPendingBackgroundAlarms.keyAt(i3)) == userHandle) {
                    ArrayList<Alarm> toRemove = this.mPendingBackgroundAlarms.valueAt(i3);
                    if (toRemove != null) {
                        for (int j = 0; j < toRemove.size(); j++) {
                            decrementAlarmCount(toRemove.get(j).uid, 1);
                            Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "remove pending background:" + toRemove.get(j) + " by userHandle:" + userHandle);
                        }
                    }
                    this.mPendingBackgroundAlarms.removeAt(i3);
                }
            }
            for (int i4 = this.mLastAllowWhileIdleDispatch.size() - 1; i4 >= 0; i4--) {
                if (UserHandle.getUserId(this.mLastAllowWhileIdleDispatch.keyAt(i4)) == userHandle) {
                    this.mLastAllowWhileIdleDispatch.removeAt(i4);
                }
            }
            if (didRemove) {
                rebatchAllAlarmsLocked(true);
                rescheduleKernelAlarmsLocked();
                updateNextAlarmClockLocked();
            }
        }
    }

    static /* synthetic */ boolean lambda$removeUserLocked$6(int userHandle, Alarm a) {
        if (UserHandle.getUserId(a.creatorUid) != userHandle) {
            return false;
        }
        Slog.i(TAG, "remove " + a + " according to userHandle:" + userHandle);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void interactiveStateChangedLocked(boolean interactive) {
        if (this.mInteractive != interactive) {
            this.mInteractive = interactive;
            long nowELAPSED = this.mInjector.getElapsedRealtime();
            if (interactive) {
                if (this.mPendingNonWakeupAlarms.size() > 0) {
                    long thisDelayTime = nowELAPSED - this.mStartCurrentDelayTime;
                    this.mTotalDelayTime += thisDelayTime;
                    if (this.mMaxDelayTime < thisDelayTime) {
                        this.mMaxDelayTime = thisDelayTime;
                    }
                    deliverAlarmsLocked(this.mPendingNonWakeupAlarms, nowELAPSED);
                    this.mPendingNonWakeupAlarms.clear();
                }
                long thisDelayTime2 = this.mNonInteractiveStartTime;
                if (thisDelayTime2 > 0) {
                    long dur = nowELAPSED - thisDelayTime2;
                    if (dur > this.mNonInteractiveTime) {
                        this.mNonInteractiveTime = dur;
                    }
                }
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.$$Lambda$AlarmManagerService$BD95FVzlEgEkBY0moyqyrC2ZyWY */

                    @Override // java.lang.Runnable
                    public final void run() {
                        AlarmManagerService.this.lambda$interactiveStateChangedLocked$7$AlarmManagerService();
                    }
                });
                return;
            }
            this.mNonInteractiveStartTime = nowELAPSED;
        }
    }

    public /* synthetic */ void lambda$interactiveStateChangedLocked$7$AlarmManagerService() {
        getContext().sendBroadcastAsUser(this.mTimeTickIntent, UserHandle.ALL);
    }

    /* access modifiers changed from: package-private */
    public boolean lookForPackageLocked(String packageName) {
        for (int i = 0; i < this.mAlarmBatches.size(); i++) {
            if (this.mAlarmBatches.get(i).hasPackage(packageName)) {
                return true;
            }
        }
        for (int i2 = 0; i2 < this.mPendingWhileIdleAlarms.size(); i2++) {
            if (this.mPendingWhileIdleAlarms.get(i2).matches(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setLocked(int type, long when) {
        if (this.mInjector.isAlarmDriverPresent()) {
            this.mInjector.setAlarm(type, when);
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.removeMessages(msg.what);
        this.mHandler.sendMessageAtTime(msg, when);
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, String label, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i += -1) {
            Alarm a = list.get(i);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowELAPSED, nowRTC, sdf);
        }
    }

    private static final String labelForType(int type) {
        if (type == 0) {
            return "RTC_WAKEUP";
        }
        if (type == 1) {
            return "RTC";
        }
        if (type == 2) {
            return "ELAPSED_WAKEUP";
        }
        if (type != 3) {
            return "--unknown--";
        }
        return "ELAPSED";
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i += -1) {
            Alarm a = list.get(i);
            String label = labelForType(a.type);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowELAPSED, nowRTC, sdf);
        }
    }

    private boolean isBackgroundRestricted(Alarm alarm) {
        boolean exemptOnBatterySaver = (alarm.flags & 4) != 0;
        if (alarm.alarmClock != null) {
            return false;
        }
        if (alarm.operation != null) {
            if (alarm.operation.isActivity()) {
                return false;
            }
            if (alarm.operation.isForegroundService()) {
                exemptOnBatterySaver = true;
            }
        }
        String sourcePackage = alarm.sourcePackage;
        int sourceUid = alarm.creatorUid;
        AppStateTracker appStateTracker = this.mAppStateTracker;
        return appStateTracker != null && appStateTracker.areAlarmsRestricted(sourceUid, sourcePackage, exemptOnBatterySaver);
    }

    private long getWhileIdleMinIntervalLocked(int uid) {
        boolean ebs = true;
        boolean dozing = this.mPendingIdleUntil != null;
        AppStateTracker appStateTracker = this.mAppStateTracker;
        if (appStateTracker == null || !appStateTracker.isForceAllAppsStandbyEnabled()) {
            ebs = false;
        }
        if (!dozing && !ebs) {
            return this.mConstants.ALLOW_WHILE_IDLE_SHORT_TIME;
        }
        if (dozing) {
            return this.mConstants.ALLOW_WHILE_IDLE_LONG_TIME;
        }
        if (this.mUseAllowWhileIdleShortTime.get(uid)) {
            return this.mConstants.ALLOW_WHILE_IDLE_SHORT_TIME;
        }
        return this.mConstants.ALLOW_WHILE_IDLE_LONG_TIME;
    }

    /* access modifiers changed from: package-private */
    public boolean triggerAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        AlarmManagerService alarmManagerService;
        int i;
        String str;
        String str2;
        String str3;
        boolean z;
        int N;
        Batch batch;
        int i2;
        boolean z2;
        AlarmManagerService alarmManagerService2;
        String str4;
        String str5;
        Alarm alarm;
        AlarmManagerService alarmManagerService3 = this;
        ArrayList<Alarm> arrayList = triggerList;
        boolean hasWakeup = false;
        while (true) {
            int size = alarmManagerService3.mAlarmBatches.size();
            int i3 = 2;
            String str6 = TAG;
            boolean z3 = true;
            if (size <= 0) {
                alarmManagerService = alarmManagerService3;
                i = 1;
                str = str6;
                break;
            }
            boolean z4 = false;
            Batch batch2 = alarmManagerService3.mAlarmBatches.get(0);
            if (batch2.start > nowELAPSED) {
                alarmManagerService = alarmManagerService3;
                i = 1;
                str = str6;
                break;
            }
            alarmManagerService3.mAlarmBatches.remove(0);
            int N2 = batch2.size();
            boolean hasWakeup2 = hasWakeup;
            int i4 = 0;
            while (i4 < N2) {
                Alarm alarm2 = batch2.get(i4);
                if ((alarm2.flags & 4) != 0) {
                    str4 = str6;
                    long lastTime = alarmManagerService3.mLastAllowWhileIdleDispatch.get(alarm2.creatorUid, -1);
                    long minTime = alarmManagerService3.getWhileIdleMinIntervalLocked(alarm2.creatorUid) + lastTime;
                    if (lastTime >= 0 && nowELAPSED < minTime) {
                        alarm2.whenElapsed = minTime;
                        alarm2.expectedWhenElapsed = minTime;
                        if (alarm2.maxWhenElapsed < minTime) {
                            alarm2.maxWhenElapsed = minTime;
                        }
                        alarm2.expectedMaxWhenElapsed = alarm2.maxWhenElapsed;
                        alarmManagerService3.setImplLocked(alarm2, z3, z4);
                        alarmManagerService2 = alarmManagerService3;
                        i2 = i4;
                        batch = batch2;
                        N = N2;
                        boolean z5 = z3 ? 1 : 0;
                        Object[] objArr = z3 ? 1 : 0;
                        Object[] objArr2 = z3 ? 1 : 0;
                        z2 = z5;
                        z = z4;
                        str3 = str4;
                        i4 = i2 + 1;
                        arrayList = triggerList;
                        alarmManagerService3 = alarmManagerService2;
                        z3 = z2;
                        batch2 = batch;
                        N2 = N;
                        z4 = z;
                        str6 = str3;
                        i3 = 2;
                    }
                } else {
                    str4 = str6;
                }
                if (alarmManagerService3.isBackgroundRestricted(alarm2)) {
                    ArrayList<Alarm> alarmsForUid = alarmManagerService3.mPendingBackgroundAlarms.get(alarm2.creatorUid);
                    if (alarmsForUid == null) {
                        alarmsForUid = new ArrayList<>();
                        alarmManagerService3.mPendingBackgroundAlarms.put(alarm2.creatorUid, alarmsForUid);
                    }
                    alarmsForUid.add(alarm2);
                    alarmManagerService2 = alarmManagerService3;
                    i2 = i4;
                    batch = batch2;
                    N = N2;
                    boolean z52 = z3 ? 1 : 0;
                    Object[] objArr3 = z3 ? 1 : 0;
                    Object[] objArr22 = z3 ? 1 : 0;
                    z2 = z52;
                    z = z4;
                    str3 = str4;
                    i4 = i2 + 1;
                    arrayList = triggerList;
                    alarmManagerService3 = alarmManagerService2;
                    z3 = z2;
                    batch2 = batch;
                    N2 = N;
                    z4 = z;
                    str6 = str3;
                    i3 = 2;
                } else {
                    int i5 = z3 ? 1 : 0;
                    int i6 = z3 ? 1 : 0;
                    int i7 = z3 ? 1 : 0;
                    alarm2.count = i5;
                    arrayList.add(alarm2);
                    if ((alarm2.flags & i3) != 0) {
                        boolean z6 = alarmManagerService3.mPendingIdleUntil != null ? z3 : z4;
                        String str7 = alarm2.statsTag;
                        int i8 = z6 ? 1 : 0;
                        int i9 = z6 ? 1 : 0;
                        int i10 = z6 ? 1 : 0;
                        EventLogTags.writeDeviceIdleWakeFromIdle(i8, str7);
                    }
                    if (alarmManagerService3.mPendingIdleUntil == alarm2) {
                        str5 = str4;
                        Slog.i(str5, "Idle alarm triggered");
                        alarmManagerService3.mPendingIdleUntil = null;
                        alarmManagerService3.rebatchAllAlarmsLocked(z4);
                        restorePendingWhileIdleAlarmsLocked();
                    } else {
                        str5 = str4;
                    }
                    if (alarmManagerService3.mNextWakeFromIdle == alarm2) {
                        alarmManagerService3.mNextWakeFromIdle = null;
                        alarmManagerService3.rebatchAllAlarmsLocked(z4);
                    }
                    if (alarm2.repeatInterval > 0) {
                        alarm2.count = (int) (((long) alarm2.count) + ((nowELAPSED - alarm2.expectedWhenElapsed) / alarm2.repeatInterval));
                        long delta = ((long) alarm2.count) * alarm2.repeatInterval;
                        long nextElapsed = alarm2.expectedWhenElapsed + delta;
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "set again repeat alarm: nextElapsed = " + nextElapsed + ",repeatInterval = " + alarm2.repeatInterval);
                        i2 = i4;
                        batch = batch2;
                        N = N2;
                        z = false;
                        str3 = str5;
                        alarm = alarm2;
                        setImplLocked(alarm2.type, alarm2.when + delta, nextElapsed, alarm2.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm2.repeatInterval), alarm2.repeatInterval, alarm2.operation, null, null, alarm2.flags, true, alarm2.workSource, alarm2.alarmClock, alarm2.uid, alarm2.packageName, alarm2.procName);
                    } else {
                        i2 = i4;
                        alarm = alarm2;
                        batch = batch2;
                        N = N2;
                        z = z4;
                        str3 = str5;
                    }
                    if (alarm.wakeup) {
                        hasWakeup2 = true;
                    }
                    if (alarm.alarmClock != null) {
                        z2 = true;
                        alarmManagerService2 = this;
                        alarmManagerService2.mNextAlarmClockMayChange = true;
                    } else {
                        z2 = true;
                        alarmManagerService2 = this;
                    }
                    i4 = i2 + 1;
                    arrayList = triggerList;
                    alarmManagerService3 = alarmManagerService2;
                    z3 = z2;
                    batch2 = batch;
                    N2 = N;
                    z4 = z;
                    str6 = str3;
                    i3 = 2;
                }
            }
            arrayList = triggerList;
            hasWakeup = hasWakeup2;
        }
        alarmManagerService.mCurrentSeq += i;
        calculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, alarmManagerService.mAlarmDispatchComparator);
        List<Alarm> wakeupAlarms = new ArrayList<>();
        Iterator<Alarm> it = triggerList.iterator();
        while (it.hasNext()) {
            Alarm talarm = it.next();
            if (talarm.type != 0) {
                if (talarm.type != 2) {
                    str2 = str;
                    str = str2;
                }
            }
            wakeupAlarms.add(talarm);
            StringBuilder alarmInfo = new StringBuilder();
            if (talarm.operation != null) {
                Intent intent = resetIntentCallingIdentity(talarm.operation);
                alarmInfo.append(" tag: ");
                alarmInfo.append(talarm.statsTag);
                alarmInfo.append(" window:");
                alarmInfo.append(talarm.windowLength);
                alarmInfo.append(" originWhen:");
                alarmInfo.append(alarmManagerService.convertToElapsed(talarm.when, talarm.type));
                alarmInfo.append(" when:");
                alarmInfo.append(talarm.whenElapsed);
                alarmInfo.append(" maxWhen:");
                alarmInfo.append(talarm.maxWhenElapsed);
                if (intent != null) {
                    str2 = str;
                    Log.w(str2, "WAKEUP alarm trigger action = " + resetActionCallingIdentity(talarm.operation) + " package name is: " + talarm.packageName + alarmInfo.toString());
                } else {
                    str2 = str;
                    Log.w(str2, "WAKEUP alarm intent = null and package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " createor uid is: " + talarm.creatorUid + alarmInfo.toString());
                }
            } else {
                str2 = str;
                Log.w(str2, "WAKEUP alarm talarm.operation == null,package name is: " + talarm.packageName + " listenerTag is: " + talarm.listenerTag + " creator uid is: " + talarm.creatorUid + alarmInfo.toString());
            }
            str = str2;
        }
        if (wakeupAlarms.size() > 0) {
            Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "Alarm triggering (type 0 or 2): " + wakeupAlarms);
        }
        return hasWakeup;
    }

    public static class IncreasingTimeOrder implements Comparator<Alarm> {
        public int compare(Alarm a1, Alarm a2) {
            long when1 = a1.whenElapsed;
            long when2 = a2.whenElapsed;
            if (when1 > when2) {
                return 1;
            }
            if (when1 < when2) {
                return -1;
            }
            return 0;
        }
    }

    @VisibleForTesting
    public static class Alarm {
        public final AlarmManager.AlarmClockInfo alarmClock;
        public int count;
        public final int creatorUid;
        public long expectedMaxWhenElapsed;
        public long expectedWhenElapsed;
        public final int flags;
        public final IAlarmListener listener;
        public final String listenerTag;
        public long maxWhenElapsed;
        public final PendingIntent operation;
        public final long origWhen;
        public final String packageName;
        public PriorityClass priorityClass;
        public final String procName;
        public long repeatInterval;
        public final String sourcePackage;
        public final String statsTag;
        public final int type;
        public final int uid;
        public boolean wakeup;
        public long when;
        public long whenElapsed;
        public long windowLength;
        public final WorkSource workSource;

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmManager.AlarmClockInfo _info, int _uid, String _pkgName) {
            this(_type, _when, _whenElapsed, _windowLength, _maxWhen, _interval, _op, _rec, _listenerTag, _ws, _flags, _info, _uid, _pkgName, null);
        }

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmManager.AlarmClockInfo _info, int _uid, String _pkgName, String _procName) {
            this.type = _type;
            this.origWhen = _when;
            this.wakeup = _type == 2 || _type == 0;
            this.when = _when;
            this.whenElapsed = _whenElapsed;
            this.expectedWhenElapsed = _whenElapsed;
            this.windowLength = _windowLength;
            long clampPositive = AlarmManagerService.clampPositive(_maxWhen);
            this.expectedMaxWhenElapsed = clampPositive;
            this.maxWhenElapsed = clampPositive;
            this.repeatInterval = _interval;
            this.operation = _op;
            this.listener = _rec;
            this.listenerTag = _listenerTag;
            this.statsTag = makeTag(_op, _listenerTag, _type);
            this.workSource = _ws;
            this.flags = _flags;
            this.alarmClock = _info;
            this.uid = _uid;
            this.packageName = _pkgName;
            this.procName = _procName;
            PendingIntent pendingIntent = this.operation;
            this.sourcePackage = pendingIntent != null ? pendingIntent.getCreatorPackage() : this.packageName;
            PendingIntent pendingIntent2 = this.operation;
            this.creatorUid = pendingIntent2 != null ? pendingIntent2.getCreatorUid() : this.uid;
        }

        public static String makeTag(PendingIntent pi, String tag, int type2) {
            String alarmString = (type2 == 2 || type2 == 0) ? "*walarm*:" : "*alarm*:";
            if (pi != null) {
                return pi.getTag(alarmString);
            }
            return alarmString + tag;
        }

        public WakeupEvent makeWakeupEvent(long nowRTC) {
            String str;
            int i = this.creatorUid;
            PendingIntent pendingIntent = this.operation;
            if (pendingIntent != null) {
                str = AlarmManagerService.resetActionCallingIdentity(pendingIntent);
            } else {
                str = "<listener>:" + this.listenerTag;
            }
            return new WakeupEvent(nowRTC, i, str);
        }

        public boolean matches(PendingIntent pi, IAlarmListener rec) {
            PendingIntent pendingIntent = this.operation;
            if (pendingIntent != null) {
                return pendingIntent.equals(pi);
            }
            return rec != null && this.listener.asBinder().equals(rec.asBinder());
        }

        public boolean matches(String packageName2) {
            return packageName2.equals(this.sourcePackage);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Alarm{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" type ");
            sb.append(this.type);
            sb.append(" when ");
            sb.append(this.when);
            sb.append(" ");
            PendingIntent pendingIntent = this.operation;
            if (pendingIntent != null) {
                sb.append(pendingIntent);
            } else {
                sb.append(this.packageName);
            }
            sb.append(this.sourcePackage);
            sb.append(" procName ");
            sb.append(this.procName);
            sb.append('}');
            return sb.toString();
        }

        public void dump(PrintWriter pw, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
            int i = this.type;
            boolean isRtc = true;
            if (!(i == 1 || i == 0)) {
                isRtc = false;
            }
            pw.print(prefix);
            pw.print("tag=");
            pw.println(this.statsTag);
            pw.print(prefix);
            pw.print("type=");
            pw.print(this.type);
            pw.print(prefix);
            pw.print("wakeup=");
            pw.print(this.wakeup);
            pw.print(" expectedWhenElapsed=");
            TimeUtils.formatDuration(this.expectedWhenElapsed, nowELAPSED, pw);
            pw.print(" expectedMaxWhenElapsed=");
            TimeUtils.formatDuration(this.expectedMaxWhenElapsed, nowELAPSED, pw);
            pw.print(" whenElapsed=");
            TimeUtils.formatDuration(this.whenElapsed, nowELAPSED, pw);
            pw.print(" maxWhenElapsed=");
            TimeUtils.formatDuration(this.maxWhenElapsed, nowELAPSED, pw);
            pw.print(" when=");
            if (isRtc) {
                pw.print(sdf.format(new Date(this.when)));
            } else {
                TimeUtils.formatDuration(this.when, nowELAPSED, pw);
            }
            pw.println();
            pw.print(prefix);
            pw.print("window=");
            TimeUtils.formatDuration(this.windowLength, pw);
            pw.print(" repeatInterval=");
            pw.print(this.repeatInterval);
            pw.print(" count=");
            pw.print(this.count);
            pw.print(" flags=0x");
            pw.println(Integer.toHexString(this.flags));
            if (this.alarmClock != null) {
                pw.print(prefix);
                pw.println("Alarm clock:");
                pw.print(prefix);
                pw.print("  triggerTime=");
                pw.println(sdf.format(new Date(this.alarmClock.getTriggerTime())));
                pw.print(prefix);
                pw.print("  showIntent=");
                pw.println(this.alarmClock.getShowIntent());
            }
            pw.print(prefix);
            pw.print("operation=");
            pw.println(this.operation);
            if (this.listener != null) {
                pw.print(prefix);
                pw.print("listener=");
                pw.println(this.listener.asBinder());
            }
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId, long nowElapsed, long nowRTC) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, this.statsTag);
            proto.write(1159641169922L, this.type);
            proto.write(1112396529667L, this.whenElapsed - nowElapsed);
            proto.write(1112396529668L, this.windowLength);
            proto.write(1112396529669L, this.repeatInterval);
            proto.write(1120986464262L, this.count);
            proto.write(1120986464263L, this.flags);
            AlarmManager.AlarmClockInfo alarmClockInfo = this.alarmClock;
            if (alarmClockInfo != null) {
                alarmClockInfo.writeToProto(proto, 1146756268040L);
            }
            PendingIntent pendingIntent = this.operation;
            if (pendingIntent != null) {
                pendingIntent.writeToProto(proto, 1146756268041L);
            }
            IAlarmListener iAlarmListener = this.listener;
            if (iAlarmListener != null) {
                proto.write(1138166333450L, iAlarmListener.asBinder().toString());
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    public void recordWakeupAlarms(ArrayList<Batch> batches, long nowELAPSED, long nowRTC) {
        int numBatches = batches.size();
        for (int nextBatch = 0; nextBatch < numBatches; nextBatch++) {
            Batch b = batches.get(nextBatch);
            if (b.start <= nowELAPSED) {
                int numAlarms = b.alarms.size();
                for (int nextAlarm = 0; nextAlarm < numAlarms; nextAlarm++) {
                    this.mRecentWakeups.add(b.alarms.get(nextAlarm).makeWakeupEvent(nowRTC));
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long currentNonWakeupFuzzLocked(long nowELAPSED) {
        long timeSinceOn = nowELAPSED - this.mNonInteractiveStartTime;
        if (timeSinceOn < BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
            return JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
        }
        if (timeSinceOn < 1800000) {
            return 900000;
        }
        return AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
    }

    static int fuzzForDuration(long duration) {
        if (duration < 900000) {
            return (int) duration;
        }
        if (duration < 5400000) {
            return 900000;
        }
        return 1800000;
    }

    /* access modifiers changed from: package-private */
    public boolean checkAllowNonWakeupDelayLocked(long nowELAPSED) {
        if (this.mInteractive || this.mLastAlarmDeliveryTime <= 0) {
            return false;
        }
        if ((this.mPendingNonWakeupAlarms.size() <= 0 || this.mNextNonWakeupDeliveryTime >= nowELAPSED) && nowELAPSED - this.mLastAlarmDeliveryTime <= currentNonWakeupFuzzLocked(nowELAPSED)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        String packageName;
        this.mLastAlarmDeliveryTime = nowELAPSED;
        for (int i = 0; i < triggerList.size(); i++) {
            Alarm alarm = triggerList.get(i);
            if (alarm == null) {
                HwLog.dubaie("DUBAI_TAG_ALARM_TRIGGER", "uid=0 name=NULL type=-1 tag=NULL size=" + triggerList.size() + " worksource=0");
            } else {
                boolean allowWhileIdle = (alarm.flags & 4) != 0;
                if (alarm.wakeup) {
                    Trace.traceBegin(131072, "Dispatch wakeup alarm to " + alarm.packageName);
                } else {
                    Trace.traceBegin(131072, "Dispatch non-wakeup alarm to " + alarm.packageName);
                }
                try {
                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "sending alarm " + alarm + ",repeatInterval = " + alarm.repeatInterval + ",listenerTag =" + alarm.listenerTag);
                    if (alarm.operation == null) {
                        packageName = alarm.packageName;
                    } else {
                        packageName = alarm.operation.getTargetPackage();
                    }
                    int wsSize = alarm.workSource != null ? alarm.workSource.size() : 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append("uid=");
                    sb.append(alarm.uid);
                    sb.append(" name=");
                    sb.append(packageName);
                    sb.append(" type=");
                    sb.append(alarm.type);
                    sb.append(" tag=");
                    sb.append(alarm.statsTag);
                    sb.append(" size=");
                    sb.append(triggerList.size());
                    sb.append(" worksource=");
                    sb.append(wsSize > 0 ? 1 : 0);
                    HwLog.dubaie("DUBAI_TAG_ALARM_TRIGGER", sb.toString());
                    int wi = 0;
                    while (wi < wsSize) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("uid=");
                        sb2.append(alarm.workSource.get(wi));
                        sb2.append(" name=");
                        sb2.append(alarm.workSource.getName(wi));
                        sb2.append(" tag=");
                        sb2.append(alarm.statsTag);
                        sb2.append(" finished=");
                        sb2.append(wi == wsSize + -1 ? "1" : "0");
                        HwLog.dubaie("DUBAI_TAG_ALARM_WORKSOURCE", sb2.toString());
                        wi++;
                    }
                    ActivityManager.noteAlarmStart(alarm.operation, alarm.workSource, alarm.uid, alarm.statsTag);
                    this.mDeliveryTracker.deliverLocked(alarm, nowELAPSED, allowWhileIdle);
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Failure sending alarm.", e);
                }
                Trace.traceEnd(131072);
                decrementAlarmCount(alarm.uid, 1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isExemptFromAppStandby(Alarm a) {
        return (a.alarmClock == null && !UserHandle.isCore(a.creatorUid) && (a.flags & 8) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Injector {
        private Context mContext;
        private long mNativeData;

        Injector(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: package-private */
        public void init() {
            this.mNativeData = AlarmManagerService.init();
        }

        /* access modifiers changed from: package-private */
        public int waitForAlarm() {
            return AlarmManagerService.waitForAlarm(this.mNativeData);
        }

        /* access modifiers changed from: package-private */
        public boolean isAlarmDriverPresent() {
            return this.mNativeData != 0;
        }

        /* access modifiers changed from: package-private */
        public void setAlarm(int type, long millis) {
            long alarmSeconds;
            long alarmSeconds2;
            if (millis < 0) {
                alarmSeconds2 = 0;
                alarmSeconds = 0;
            } else {
                alarmSeconds2 = millis / 1000;
                alarmSeconds = 1000 * (millis % 1000) * 1000;
            }
            int result = AlarmManagerService.set(this.mNativeData, type, alarmSeconds2, alarmSeconds);
            if (result != 0) {
                long nowElapsed = SystemClock.elapsedRealtime();
                Slog.wtf(AlarmManagerService.TAG, "Unable to set kernel alarm, now=" + nowElapsed + " type=" + type + " @ (" + alarmSeconds2 + "," + alarmSeconds + "), ret = " + result + " = " + Os.strerror(result));
            }
        }

        /* access modifiers changed from: package-private */
        public long getNextAlarm(int type) {
            return AlarmManagerService.getNextAlarm(this.mNativeData, type);
        }

        /* access modifiers changed from: package-private */
        public void setKernelTimezone(int minutesWest) {
            AlarmManagerService.setKernelTimezone(this.mNativeData, minutesWest);
        }

        /* access modifiers changed from: package-private */
        public void setKernelTime(long millis) {
            long j = this.mNativeData;
            if (j != 0) {
                AlarmManagerService.setKernelTime(j, millis);
            }
        }

        /* access modifiers changed from: package-private */
        public void close() {
            AlarmManagerService.close(this.mNativeData);
        }

        public void hwSetClockRTC(long seconds, long nanoseconds) {
            AlarmManagerService.hwSetClockRTC(this.mNativeData, seconds, nanoseconds);
        }

        /* access modifiers changed from: package-private */
        public long getElapsedRealtime() {
            return SystemClock.elapsedRealtime();
        }

        /* access modifiers changed from: package-private */
        public long getCurrentTimeMillis() {
            return System.currentTimeMillis();
        }

        /* access modifiers changed from: package-private */
        public PowerManager.WakeLock getAlarmWakeLock() {
            return ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*alarm*");
        }

        /* access modifiers changed from: package-private */
        public int getSystemUiUid() {
            PackageManager pm = this.mContext.getPackageManager();
            try {
                ApplicationInfo sysUi = pm.getApplicationInfo(pm.getPermissionInfo(AlarmManagerService.SYSTEM_UI_SELF_PERMISSION, 0).packageName, 0);
                if ((sysUi.privateFlags & 8) != 0) {
                    return sysUi.uid;
                }
                Slog.e(AlarmManagerService.TAG, "SysUI permission android.permission.systemui.IDENTITY defined by non-privileged app " + sysUi.packageName + " - ignoring");
                return -1;
            } catch (PackageManager.NameNotFoundException e) {
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public ClockReceiver getClockReceiver(AlarmManagerService service) {
            Objects.requireNonNull(service);
            return new ClockReceiver();
        }
    }

    private class AlarmThread extends Thread {
        private int mFalseWakeups = 0;
        private int mWtfThreshold = 100;

        public AlarmThread() {
            super(AlarmManagerService.TAG);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:109:0x0283, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00fe, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x0106, code lost:
            r0 = th;
         */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x010e  */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x026d  */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            long nowRTC;
            int result;
            long lastTimeChangeClockTime;
            long expectedClockTime;
            ArrayList<Alarm> triggerList = new ArrayList<>();
            AlarmManagerService.this.hwRecordFirstTime();
            while (true) {
                int result2 = AlarmManagerService.this.mInjector.waitForAlarm();
                long nowRTC2 = AlarmManagerService.this.mInjector.getCurrentTimeMillis();
                long nowELAPSED = AlarmManagerService.this.mInjector.getElapsedRealtime();
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mLastWakeup = nowELAPSED;
                }
                if (result2 == 0) {
                    Slog.wtf(AlarmManagerService.TAG, "waitForAlarm returned 0, nowRTC = " + nowRTC2 + ", nowElapsed = " + nowELAPSED);
                }
                triggerList.clear();
                if ((result2 & 65536) != 0) {
                    synchronized (AlarmManagerService.this.mLock) {
                        lastTimeChangeClockTime = AlarmManagerService.this.mLastTimeChangeClockTime;
                        expectedClockTime = lastTimeChangeClockTime + (nowELAPSED - AlarmManagerService.this.mLastTimeChangeRealtime);
                    }
                    if (lastTimeChangeClockTime == 0 || nowRTC2 < expectedClockTime - 1000 || nowRTC2 > expectedClockTime + 1000) {
                        Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "Time changed notification from kernel; rebatching");
                        StatsLog.write(45, nowRTC2);
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        alarmManagerService.removeImpl(null, alarmManagerService.mTimeTickTrigger);
                        AlarmManagerService alarmManagerService2 = AlarmManagerService.this;
                        alarmManagerService2.removeImpl(alarmManagerService2.mDateChangeSender, null);
                        AlarmManagerService.this.rebatchAllAlarms();
                        AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                        AlarmManagerService.this.mClockReceiver.scheduleDateChangedEvent();
                        synchronized (AlarmManagerService.this.mLock) {
                            AlarmManagerService.this.mNumTimeChanged++;
                            AlarmManagerService.this.mLastTimeChangeClockTime = nowRTC2;
                            AlarmManagerService.this.mLastTimeChangeRealtime = nowELAPSED;
                        }
                        Intent intent = new Intent("android.intent.action.TIME_SET");
                        intent.addFlags(891289600);
                        AlarmManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                        nowRTC = nowELAPSED;
                        AlarmManagerService.this.hwRecordTimeChangeRTC(nowRTC2, nowELAPSED, lastTimeChangeClockTime, expectedClockTime);
                        result = result2 | 5;
                        if (result == 65536) {
                            synchronized (AlarmManagerService.this.mLock) {
                                AlarmManagerService.this.mLastTrigger = nowRTC;
                                if (AlarmManagerService.this.triggerAlarmsLocked(triggerList, nowRTC) || !AlarmManagerService.this.checkAllowNonWakeupDelayLocked(nowRTC)) {
                                    if (AlarmManagerService.this.isAwareAlarmManagerEnabled() && (result & 5) != 0) {
                                        ArrayList<Alarm> wakeupList = new ArrayList<>();
                                        Iterator<Alarm> it = triggerList.iterator();
                                        while (it.hasNext()) {
                                            Alarm alarm = it.next();
                                            if (alarm.wakeup) {
                                                wakeupList.add(alarm);
                                            }
                                        }
                                        AlarmManagerService.this.reportWakeupAlarms(wakeupList);
                                    }
                                    if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() > 0) {
                                        AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                        triggerList.addAll(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                        Collections.sort(triggerList, AlarmManagerService.this.mAlarmDispatchComparator);
                                        long thisDelayTime = nowRTC - AlarmManagerService.this.mStartCurrentDelayTime;
                                        AlarmManagerService.this.mTotalDelayTime += thisDelayTime;
                                        if (AlarmManagerService.this.mMaxDelayTime < thisDelayTime) {
                                            AlarmManagerService.this.mMaxDelayTime = thisDelayTime;
                                        }
                                        AlarmManagerService.this.mPendingNonWakeupAlarms.clear();
                                    }
                                    if (AlarmManagerService.this.mLastTimeChangeRealtime != nowRTC && triggerList.isEmpty()) {
                                        int i = this.mFalseWakeups + 1;
                                        this.mFalseWakeups = i;
                                        if (i >= this.mWtfThreshold) {
                                            Slog.wtf(AlarmManagerService.TAG, "Too many (" + this.mFalseWakeups + ") false wakeups, nowElapsed=" + nowRTC);
                                            if (this.mWtfThreshold < 100000) {
                                                this.mWtfThreshold *= 10;
                                            } else {
                                                this.mFalseWakeups = 0;
                                            }
                                        }
                                    }
                                    ArraySet<Pair<String, Integer>> triggerPackages = new ArraySet<>();
                                    for (int i2 = 0; i2 < triggerList.size(); i2++) {
                                        Alarm a = triggerList.get(i2);
                                        if (!AlarmManagerService.this.isExemptFromAppStandby(a)) {
                                            triggerPackages.add(Pair.create(a.sourcePackage, Integer.valueOf(UserHandle.getUserId(a.creatorUid))));
                                        }
                                    }
                                    AlarmManagerService.this.deliverAlarmsLocked(triggerList, nowRTC);
                                    AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(triggerPackages);
                                    AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                                    AlarmManagerService.this.updateNextAlarmClockLocked();
                                } else {
                                    Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "there are no wakeup alarms and the screen is off, we can delay what we have so far until the future");
                                    if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() == 0) {
                                        AlarmManagerService.this.mStartCurrentDelayTime = nowRTC;
                                        AlarmManagerService.this.mNextNonWakeupDeliveryTime = nowRTC + ((AlarmManagerService.this.currentNonWakeupFuzzLocked(nowRTC) * 3) / 2);
                                    }
                                    AlarmManagerService.this.mPendingNonWakeupAlarms.addAll(triggerList);
                                    AlarmManagerService.this.mNumDelayedAlarms += triggerList.size();
                                    AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                                    AlarmManagerService.this.updateNextAlarmClockLocked();
                                }
                            }
                        } else {
                            synchronized (AlarmManagerService.this.mLock) {
                                AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            }
                        }
                    } else {
                        nowRTC = nowELAPSED;
                    }
                } else {
                    nowRTC = nowELAPSED;
                }
                result = result2;
                if (result == 65536) {
                }
            }
            while (true) {
            }
            while (true) {
            }
            while (true) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWakelockWorkSource(WorkSource ws, int knownUid, String tag, boolean first) {
        try {
            this.mWakeLock.setHistoryTag(first ? tag : null);
            if (ws != null) {
                this.mWakeLock.setWorkSource(ws);
                return;
            }
            if (knownUid >= 0) {
                this.mWakeLock.setWorkSource(new WorkSource(knownUid));
                return;
            }
            this.mWakeLock.setWorkSource(null);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: private */
    public static int getAlarmAttributionUid(Alarm alarm) {
        if (alarm.workSource == null || alarm.workSource.isEmpty()) {
            return alarm.creatorUid;
        }
        return alarm.workSource.getAttributionUid();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public class AlarmHandler extends Handler {
        public static final int ALARM_EVENT = 1;
        public static final int APP_STANDBY_BUCKET_CHANGED = 5;
        public static final int APP_STANDBY_PAROLE_CHANGED = 6;
        public static final int LISTENER_TIMEOUT = 3;
        public static final int REMOVE_FOR_STOPPED = 7;
        public static final int REPORT_ALARMS_ACTIVE = 4;
        public static final int SEND_NEXT_ALARM_CLOCK_CHANGED = 2;
        public static final int UNREGISTER_CANCEL_LISTENER = 8;

        AlarmHandler() {
            super(Looper.myLooper());
        }

        public void postRemoveForStopped(int uid) {
            obtainMessage(7, uid, 0).sendToTarget();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    ArrayList<Alarm> triggerList = new ArrayList<>();
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.triggerAlarmsLocked(triggerList, AlarmManagerService.this.mInjector.getElapsedRealtime());
                        AlarmManagerService.this.updateNextAlarmClockLocked();
                    }
                    for (int i = 0; i < triggerList.size(); i++) {
                        Alarm alarm = triggerList.get(i);
                        try {
                            if (alarm.operation != null) {
                                alarm.operation.send();
                            }
                        } catch (PendingIntent.CanceledException e) {
                            if (alarm.repeatInterval > 0) {
                                AlarmManagerService.this.removeImpl(alarm.operation, null);
                            }
                        }
                        AlarmManagerService.this.decrementAlarmCount(alarm.uid, 1);
                    }
                    return;
                case 2:
                    AlarmManagerService.this.sendNextAlarmClockChanged();
                    return;
                case 3:
                    AlarmManagerService.this.mDeliveryTracker.alarmTimedOut((IBinder) msg.obj);
                    return;
                case 4:
                    if (AlarmManagerService.this.mLocalDeviceIdleController != null) {
                        DeviceIdleController.LocalService localService = AlarmManagerService.this.mLocalDeviceIdleController;
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        localService.setAlarmsActive(z);
                        return;
                    }
                    return;
                case 5:
                    synchronized (AlarmManagerService.this.mLock) {
                        ArraySet<Pair<String, Integer>> filterPackages = new ArraySet<>();
                        filterPackages.add(Pair.create((String) msg.obj, Integer.valueOf(msg.arg1)));
                        if (AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(filterPackages)) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                        }
                    }
                    return;
                case 6:
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.mAppStandbyParole = ((Boolean) msg.obj).booleanValue();
                        if (AlarmManagerService.this.reorderAlarmsBasedOnStandbyBuckets(null)) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                        }
                    }
                    return;
                case 7:
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService.this.removeForStoppedLocked(msg.arg1);
                    }
                    return;
                case 8:
                    PendingIntent pi = (PendingIntent) msg.obj;
                    if (pi != null) {
                        pi.unregisterCancelListener(AlarmManagerService.this.mOperationCancelListener);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class ClockReceiver extends BroadcastReceiver {
        public ClockReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
                Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "Received DATE_CHANGED alarm; rescheduling");
                AlarmManagerService.this.mInjector.setKernelTimezone(-(TimeZone.getTimeZone(SystemProperties.get(AlarmManagerService.TIMEZONE_PROPERTY)).getOffset(AlarmManagerService.this.mInjector.getCurrentTimeMillis()) / 60000));
                scheduleDateChangedEvent();
            }
        }

        public void scheduleTimeTickEvent() {
            long currentTime = AlarmManagerService.this.mInjector.getCurrentTimeMillis();
            long triggerAtTime = AlarmManagerService.this.mInjector.getElapsedRealtime() + ((((currentTime / 60000) + 1) * 60000) - currentTime);
            Flog.i((int) SystemService.PHASE_SYSTEM_SERVICES_READY, "scheduleTimeTickEvent triggerAtTime = " + triggerAtTime);
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.setImpl(3, triggerAtTime, 0, 0, null, alarmManagerService.mTimeTickTrigger, "TIME_TICK", 1, null, null, Process.myUid(), PackageManagerService.PLATFORM_PACKAGE_NAME);
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.mLastTickSet = currentTime;
            }
        }

        public void scheduleDateChangedEvent() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            calendar.add(5, 1);
            AlarmManagerService.this.setImpl(1, calendar.getTimeInMillis(), 0, 0, AlarmManagerService.this.mDateChangeSender, null, null, 1, null, null, Process.myUid(), PackageManagerService.PLATFORM_PACKAGE_NAME);
        }
    }

    class InteractiveStateReceiver extends BroadcastReceiver {
        public InteractiveStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.setPriority(1000);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.interactiveStateChangedLocked("android.intent.action.SCREEN_ON".equals(intent.getAction()));
            }
        }
    }

    class UninstallReceiver extends BroadcastReceiver {
        public UninstallReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme("package");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            sdFilter.addAction("android.intent.action.USER_STOPPED");
            sdFilter.addAction("android.intent.action.UID_REMOVED");
            AlarmManagerService.this.getContext().registerReceiver(this, sdFilter);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean z;
            String pkg;
            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
            synchronized (AlarmManagerService.this.mLock) {
                String[] pkgList = null;
                Slog.i(AlarmManagerService.TAG, "UninstallReceiver onReceive action:" + intent.getAction());
                String action = intent.getAction();
                int i = 0;
                switch (action.hashCode()) {
                    case -1749672628:
                        if (action.equals("android.intent.action.UID_REMOVED")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1403934493:
                        if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1072806502:
                        if (action.equals("android.intent.action.QUERY_PACKAGE_RESTART")) {
                            z = false;
                            break;
                        }
                        z = true;
                        break;
                    case -757780528:
                        if (action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -742246786:
                        if (action.equals("android.intent.action.USER_STOPPED")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 525384130:
                        if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    default:
                        z = true;
                        break;
                }
                if (z) {
                    if (z) {
                        pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    } else if (z) {
                        int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                        if (userHandle >= 0) {
                            AlarmManagerService.this.removeUserLocked(userHandle);
                            AlarmManagerService.this.mAppWakeupHistory.removeForUser(userHandle);
                        }
                        return;
                    } else if (!z) {
                        if (!z) {
                            if (!z) {
                            }
                        } else if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            return;
                        }
                        Uri data = intent.getData();
                        if (!(data == null || (pkg = data.getSchemeSpecificPart()) == null)) {
                            pkgList = new String[]{pkg};
                        }
                    } else {
                        if (uid >= 0) {
                            AlarmManagerService.this.mLastAllowWhileIdleDispatch.delete(uid);
                            AlarmManagerService.this.mUseAllowWhileIdleShortTime.delete(uid);
                        }
                        return;
                    }
                    if (pkgList != null && pkgList.length > 0) {
                        int length = pkgList.length;
                        while (i < length) {
                            String pkg2 = pkgList[i];
                            if (uid == 1001) {
                                Slog.i(AlarmManagerService.TAG, "removeLocked pkg: " + pkg2 + ", uid:" + uid);
                                AlarmManagerService.this.removeLocked(uid, pkg2);
                            } else if (uid >= 0) {
                                AlarmManagerService.this.mAppWakeupHistory.removeForPackage(pkg2, UserHandle.getUserId(uid));
                                AlarmManagerService.this.removeLocked(uid);
                            } else {
                                AlarmManagerService.this.removeLocked(pkg2);
                            }
                            AlarmManagerService.this.mPriorities.remove(pkg2);
                            for (int i2 = AlarmManagerService.this.mBroadcastStats.size() - 1; i2 >= 0; i2--) {
                                ArrayMap<String, BroadcastStats> uidStats = AlarmManagerService.this.mBroadcastStats.valueAt(i2);
                                if (!(uidStats == null || uidStats.remove(pkg2) == null || uidStats.size() > 0)) {
                                    AlarmManagerService.this.mBroadcastStats.removeAt(i2);
                                }
                            }
                            i++;
                        }
                    }
                    return;
                }
                String[] pkgList2 = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                int length2 = pkgList2.length;
                while (i < length2) {
                    if (AlarmManagerService.this.lookForPackageLocked(pkgList2[i])) {
                        setResultCode(-1);
                        return;
                    }
                    i++;
                }
            }
        }
    }

    final class UidObserver extends IUidObserver.Stub {
        UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean disabled) {
            if (disabled) {
                AlarmManagerService.this.mHandler.postRemoveForStopped(uid);
            }
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
            if (disabled) {
                AlarmManagerService.this.mHandler.postRemoveForStopped(uid);
            }
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    private final class AppStandbyTracker extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppStandbyTracker() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            AlarmManagerService.this.mHandler.removeMessages(5);
            AlarmManagerService.this.mHandler.obtainMessage(5, userId, -1, packageName).sendToTarget();
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            AlarmManagerService.this.mHandler.removeMessages(5);
            AlarmManagerService.this.mHandler.removeMessages(6);
            AlarmManagerService.this.mHandler.obtainMessage(6, Boolean.valueOf(isParoleOn)).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final BroadcastStats getStatsLocked(PendingIntent pi) {
        return getStatsLocked(pi.getCreatorUid(), pi.getCreatorPackage());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final BroadcastStats getStatsLocked(int uid, String pkgName) {
        ArrayMap<String, BroadcastStats> uidStats = this.mBroadcastStats.get(uid);
        if (uidStats == null) {
            uidStats = new ArrayMap<>();
            this.mBroadcastStats.put(uid, uidStats);
        }
        BroadcastStats bs = uidStats.get(pkgName);
        if (bs != null) {
            return bs;
        }
        BroadcastStats bs2 = new BroadcastStats(uid, pkgName);
        uidStats.put(pkgName, bs2);
        return bs2;
    }

    /* access modifiers changed from: package-private */
    public class DeliveryTracker extends IAlarmCompleteListener.Stub implements PendingIntent.OnFinished {
        DeliveryTracker() {
        }

        private InFlight removeLocked(PendingIntent pi, Intent intent) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                InFlight inflight = AlarmManagerService.this.mInFlight.get(i);
                if (inflight.mPendingIntent == pi) {
                    if (pi.isBroadcast()) {
                        AlarmManagerService.this.notifyBroadcastAlarmCompleteLocked(inflight.mUid);
                    }
                    return AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            LocalLog localLog = AlarmManagerService.this.mLog;
            localLog.w("No in-flight alarm for " + pi + " " + intent);
            return null;
        }

        private InFlight removeLocked(IBinder listener) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (AlarmManagerService.this.mInFlight.get(i).mListener == listener) {
                    return AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            LocalLog localLog = AlarmManagerService.this.mLog;
            localLog.w("No in-flight alarm for listener " + listener);
            return null;
        }

        private void updateStatsLocked(InFlight inflight) {
            long nowELAPSED = AlarmManagerService.this.mInjector.getElapsedRealtime();
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.nesting--;
            if (bs.nesting <= 0) {
                bs.nesting = 0;
                bs.aggregateTime += nowELAPSED - bs.startTime;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.nesting--;
            if (fs.nesting <= 0) {
                fs.nesting = 0;
                fs.aggregateTime += nowELAPSED - fs.startTime;
            }
            ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource, inflight.mUid, inflight.mTag);
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(4, 0).sendToTarget();
                AlarmManagerService.this.mWakeLock.release();
                if (AlarmManagerService.this.mInFlight.size() > 0) {
                    AlarmManagerService.this.mLog.w("Finished all dispatches with " + AlarmManagerService.this.mInFlight.size() + " remaining inflights");
                    for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                        AlarmManagerService.this.mLog.w("  Remaining #" + i + ": " + AlarmManagerService.this.mInFlight.get(i));
                    }
                    AlarmManagerService.this.mInFlight.clear();
                }
            } else if (AlarmManagerService.this.mInFlight.size() > 0) {
                InFlight inFlight = AlarmManagerService.this.mInFlight.get(0);
                AlarmManagerService.this.setWakelockWorkSource(inFlight.mWorkSource, inFlight.mCreatorUid, inFlight.mTag, false);
            } else {
                AlarmManagerService.this.mLog.w("Alarm wakelock still held but sent queue empty");
                AlarmManagerService.this.mWakeLock.setWorkSource(null);
            }
        }

        public void alarmComplete(IBinder who) {
            if (who == null) {
                LocalLog localLog = AlarmManagerService.this.mLog;
                localLog.w("Invalid alarmComplete: uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mHandler.removeMessages(3, who);
                    InFlight inflight = removeLocked(who);
                    if (inflight != null) {
                        updateTrackingLocked(inflight);
                        AlarmManagerService.access$2608(AlarmManagerService.this);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override // android.app.PendingIntent.OnFinished
        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.access$2708(AlarmManagerService.this);
                updateTrackingLocked(removeLocked(pi, intent));
            }
        }

        public void alarmTimedOut(IBinder who) {
            synchronized (AlarmManagerService.this.mLock) {
                InFlight inflight = removeLocked(who);
                if (inflight != null) {
                    updateTrackingLocked(inflight);
                    AlarmManagerService.access$2608(AlarmManagerService.this);
                } else {
                    LocalLog localLog = AlarmManagerService.this.mLog;
                    localLog.w("Spurious timeout of listener " + who);
                }
            }
        }

        /* JADX INFO: finally extract failed */
        /* JADX INFO: Multiple debug info for r9v7 com.android.server.AlarmManagerService$BroadcastStats: [D('packageUser' android.util.Pair<java.lang.String, java.lang.Integer>), D('bs' com.android.server.AlarmManagerService$BroadcastStats)] */
        @GuardedBy({"mLock"})
        public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
            long workSourceToken = ThreadLocalWorkSource.setUid(AlarmManagerService.getAlarmAttributionUid(alarm));
            try {
                if (alarm.operation != null) {
                    AlarmManagerService.this.hwAddFirstFlagForRtcAlarm(alarm, AlarmManagerService.this.mBackgroundIntent);
                    AlarmManagerService.this.mBackgroundIntent.addHwFlags(2048);
                    AlarmManagerService.access$3008(AlarmManagerService.this);
                    try {
                        alarm.operation.send(AlarmManagerService.this.getContext(), 0, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                        if (alarm.repeatInterval == 0) {
                            AlarmManagerService.this.mHandler.obtainMessage(8, alarm.operation).sendToTarget();
                        }
                        AlarmManagerService.this.hwRemoveRtcAlarm(alarm, false);
                    } catch (PendingIntent.CanceledException e) {
                        if (alarm.repeatInterval > 0) {
                            AlarmManagerService.this.removeImpl(alarm.operation, null);
                        }
                        AlarmManagerService.access$2708(AlarmManagerService.this);
                        ThreadLocalWorkSource.restore(workSourceToken);
                        return;
                    }
                } else {
                    AlarmManagerService.access$3108(AlarmManagerService.this);
                    if (alarm.listener == AlarmManagerService.this.mTimeTickTrigger) {
                        AlarmManagerService.this.mTickHistory[AlarmManagerService.access$3308(AlarmManagerService.this)] = nowELAPSED;
                        if (AlarmManagerService.this.mNextTickHistory >= 10) {
                            AlarmManagerService.this.mNextTickHistory = 0;
                        }
                    }
                    try {
                        alarm.listener.doAlarm(this);
                        AlarmManagerService.this.mHandler.sendMessageDelayed(AlarmManagerService.this.mHandler.obtainMessage(3, alarm.listener.asBinder()), AlarmManagerService.this.mConstants.LISTENER_TIMEOUT);
                    } catch (Exception e2) {
                        AlarmManagerService.access$2608(AlarmManagerService.this);
                        ThreadLocalWorkSource.restore(workSourceToken);
                        return;
                    }
                }
                ThreadLocalWorkSource.restore(workSourceToken);
                if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                    AlarmManagerService.this.setWakelockWorkSource(alarm.workSource, alarm.creatorUid, alarm.statsTag, true);
                    AlarmManagerService.this.mWakeLock.acquire();
                    AlarmManagerService.this.mHandler.obtainMessage(4, 1).sendToTarget();
                }
                InFlight inflight = new InFlight(AlarmManagerService.this, alarm, nowELAPSED);
                AlarmManagerService.this.mInFlight.add(inflight);
                AlarmManagerService.this.mBroadcastRefCount++;
                if (inflight.isBroadcast()) {
                    AlarmManagerService.this.notifyBroadcastAlarmPendingLocked(alarm.uid);
                }
                if (allowWhileIdle) {
                    AlarmManagerService.this.mLastAllowWhileIdleDispatch.put(alarm.creatorUid, nowELAPSED);
                    if (AlarmManagerService.this.mAppStateTracker == null || AlarmManagerService.this.mAppStateTracker.isUidInForeground(alarm.creatorUid)) {
                        AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(alarm.creatorUid, true);
                    } else {
                        AlarmManagerService.this.mUseAllowWhileIdleShortTime.put(alarm.creatorUid, false);
                    }
                }
                if (!AlarmManagerService.this.isExemptFromAppStandby(alarm)) {
                    Pair.create(alarm.sourcePackage, Integer.valueOf(UserHandle.getUserId(alarm.creatorUid)));
                    AlarmManagerService.this.mAppWakeupHistory.recordAlarmForPackage(alarm.sourcePackage, UserHandle.getUserId(alarm.creatorUid), nowELAPSED);
                }
                BroadcastStats bs = inflight.mBroadcastStats;
                bs.count++;
                if (bs.nesting == 0) {
                    bs.nesting = 1;
                    bs.startTime = nowELAPSED;
                } else {
                    bs.nesting++;
                }
                FilterStats fs = inflight.mFilterStats;
                fs.count++;
                if (fs.nesting == 0) {
                    fs.nesting = 1;
                    fs.startTime = nowELAPSED;
                } else {
                    fs.nesting++;
                }
                if (alarm.type == 2 || alarm.type == 0) {
                    bs.numWakeup++;
                    fs.numWakeup++;
                    ActivityManager.noteWakeupAlarm(alarm.operation, alarm.workSource, alarm.uid, alarm.packageName, alarm.statsTag);
                }
                String pkg = alarm.packageName;
                if (pkg == null) {
                    return;
                }
                if (!AlarmManagerService.this.mInteractive || !PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkg)) {
                    String intentStr = "";
                    if (alarm.operation != null) {
                        Intent alarmIntent = alarm.operation.getIntent();
                        if (alarmIntent != null) {
                            intentStr = alarmIntent.getAction();
                        }
                    } else if (alarm.statsTag != null) {
                        intentStr = alarm.statsTag;
                    }
                    LogPower.push(121, pkg, String.valueOf(alarm.type), String.valueOf(alarm.repeatInterval), new String[]{String.valueOf(alarm.procName), intentStr});
                }
            } catch (Throwable e3) {
                ThreadLocalWorkSource.restore(workSourceToken);
                throw e3;
            }
        }
    }

    private void incrementAlarmCount(int uid) {
        int uidIndex = this.mAlarmsPerUid.indexOfKey(uid);
        if (uidIndex >= 0) {
            SparseIntArray sparseIntArray = this.mAlarmsPerUid;
            sparseIntArray.setValueAt(uidIndex, sparseIntArray.valueAt(uidIndex) + 1);
            return;
        }
        this.mAlarmsPerUid.put(uid, 1);
    }

    /* access modifiers changed from: protected */
    public void decrementAlarmCount(int uid, int decrement) {
        int oldCount = 0;
        int uidIndex = this.mAlarmsPerUid.indexOfKey(uid);
        if (uidIndex >= 0) {
            oldCount = this.mAlarmsPerUid.valueAt(uidIndex);
            if (oldCount > decrement) {
                this.mAlarmsPerUid.setValueAt(uidIndex, oldCount - decrement);
            } else {
                this.mAlarmsPerUid.removeAt(uidIndex);
            }
        }
        if (oldCount < decrement) {
            Slog.wtf(TAG, "Attempt to decrement existing alarm count " + oldCount + " by " + decrement + " for uid " + uid);
        }
    }

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        /* access modifiers changed from: package-private */
        public IAlarmManager getBinderService() {
            return IAlarmManager.Stub.asInterface(AlarmManagerService.this.mService);
        }

        /* JADX WARNING: Removed duplicated region for block: B:18:0x0036 A[Catch:{ Exception -> 0x005d }] */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0049 A[Catch:{ Exception -> 0x005d }] */
        public int onCommand(String cmd) {
            boolean z;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            try {
                int hashCode = cmd.hashCode();
                if (hashCode != 1369384280) {
                    if (hashCode == 2023087364 && cmd.equals("set-timezone")) {
                        z = true;
                        if (!z) {
                            return getBinderService().setTime(Long.parseLong(getNextArgRequired())) ? 0 : -1;
                        } else if (!z) {
                            return handleDefaultCommands(cmd);
                        } else {
                            getBinderService().setTimeZone(getNextArgRequired());
                            return 0;
                        }
                    }
                } else if (cmd.equals("set-time")) {
                    z = false;
                    if (!z) {
                    }
                }
                z = true;
                if (!z) {
                }
            } catch (Exception e) {
                pw.println(e);
                return -1;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Alarm manager service (alarm) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  set-time TIME");
            pw.println("    Set the system clock time to TIME where TIME is milliseconds");
            pw.println("    since the Epoch.");
            pw.println("  set-timezone TZ");
            pw.println("    Set the system timezone to TZ where TZ is an Olson id.");
        }
    }

    /* access modifiers changed from: private */
    public static String resetActionCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        try {
            return operation.getIntent().getAction();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static Intent resetIntentCallingIdentity(PendingIntent operation) {
        long identity = Binder.clearCallingIdentity();
        try {
            return operation.getIntent();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public class HwInnerAlarmManagerService extends IHwAlarmManager.Stub {
        AlarmManagerService mAMS;

        HwInnerAlarmManagerService(AlarmManagerService ams) {
            this.mAMS = ams;
        }

        public int getWakeUpNum(int uid, String pkg) {
            if (Binder.getCallingUid() == 1000) {
                return AlarmManagerService.this.getWakeUpNumImpl(uid, pkg);
            }
            Slog.i(AlarmManagerService.TAG, "getWakeUpNum: permission not allowed.");
            return 0;
        }

        public long checkHasHwRTCAlarm(String packageName) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "checkHasHwRTCAlarm");
            return AlarmManagerService.this.checkHasHwRTCAlarmLock(packageName);
        }

        public void adjustHwRTCAlarm(boolean isDeskClock, boolean isBoot, int typeState) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "adjustHwRTCAlarm");
            Slog.i(AlarmManagerService.TAG, "adjustHwRTCAlarm : adjust RTC alarm");
            AlarmManagerService.this.adjustHwRTCAlarmLock(isDeskClock, isBoot, typeState);
        }

        public void setHwRTCAlarm() {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setHwRTCAlarm");
            Slog.i(AlarmManagerService.TAG, "setHwRTCAlarm : set RTC alarm");
            AlarmManagerService.this.setHwRTCAlarmLock();
        }
    }
}
