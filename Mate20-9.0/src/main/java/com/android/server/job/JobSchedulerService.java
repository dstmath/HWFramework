package com.android.server.job;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.job.IJobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryStatsInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.StatsLog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.AppStateTracker;
import com.android.server.DeviceIdleController;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.controllers.BackgroundJobsController;
import com.android.server.job.controllers.BatteryController;
import com.android.server.job.controllers.ConnectivityController;
import com.android.server.job.controllers.ContentObserverController;
import com.android.server.job.controllers.DeviceIdleJobsController;
import com.android.server.job.controllers.HwTimeController;
import com.android.server.job.controllers.IdleController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.StateController;
import com.android.server.job.controllers.StorageController;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.power.IHwShutdownThread;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.utils.PriorityDump;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import libcore.util.EmptyArray;

public class JobSchedulerService extends SystemService implements StateChangedListener, JobCompletedListener {
    static final int ACTIVE_INDEX = 0;
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    public static final boolean DEBUG_STANDBY = (DEBUG);
    private static final boolean ENFORCE_MAX_JOBS = true;
    private static final int FG_JOB_CONTEXTS_COUNT = 2;
    static final int FREQUENT_INDEX = 2;
    static final String HEARTBEAT_TAG = "*job.heartbeat*";
    private static final int MAX_JOBS_PER_APP = 100;
    private static final int MAX_JOB_CONTEXTS_COUNT = 16;
    static final int MSG_CHECK_JOB = 1;
    static final int MSG_CHECK_JOB_GREEDY = 3;
    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_STOP_JOB = 2;
    static final int MSG_UID_ACTIVE = 6;
    static final int MSG_UID_GONE = 5;
    static final int MSG_UID_IDLE = 7;
    static final int MSG_UID_STATE_CHANGED = 4;
    static final int NEVER_INDEX = 4;
    static final int RARE_INDEX = 3;
    public static final String TAG = "JobScheduler";
    static final int WORKING_INDEX = 1;
    static final Comparator<JobStatus> mEnqueueTimeComparator = $$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss.INSTANCE;
    @VisibleForTesting
    public static Clock sElapsedRealtimeClock = SystemClock.elapsedRealtimeClock();
    @VisibleForTesting
    public static Clock sSystemClock = Clock.systemUTC();
    @VisibleForTesting
    public static Clock sUptimeMillisClock = SystemClock.uptimeMillisClock();
    final List<JobServiceContext> mActiveServices = new ArrayList();
    ActivityManagerInternal mActivityManagerInternal = ((ActivityManagerInternal) Preconditions.checkNotNull((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)));
    AppStateTracker mAppStateTracker;
    final SparseIntArray mBackingUpUids = new SparseIntArray();
    private final BatteryController mBatteryController;
    IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            List<JobStatus> jobsForUid;
            String action = intent.getAction();
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Receieved: " + action);
            }
            String pkgName = JobSchedulerService.this.getPackageName(intent);
            int pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
            int i = 0;
            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                if (pkgName == null || pkgUid == -1) {
                    Slog.w(JobSchedulerService.TAG, "PACKAGE_CHANGED for " + pkgName + " / uid " + pkgUid);
                    return;
                }
                String[] changedComponents = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                if (changedComponents != null) {
                    int length = changedComponents.length;
                    while (i < length) {
                        if (changedComponents[i].equals(pkgName)) {
                            if (JobSchedulerService.DEBUG) {
                                Slog.d(JobSchedulerService.TAG, "Package state change: " + pkgName);
                            }
                            try {
                                int state = AppGlobals.getPackageManager().getApplicationEnabledSetting(pkgName, UserHandle.getUserId(pkgUid));
                                if (state == 2 || state == 3) {
                                    if (JobSchedulerService.DEBUG) {
                                        Slog.d(JobSchedulerService.TAG, "Removing jobs for package " + pkgName + " in user " + userId);
                                    }
                                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid, "app disabled");
                                    return;
                                }
                                return;
                            } catch (RemoteException | IllegalArgumentException e) {
                                return;
                            }
                        } else {
                            i++;
                        }
                    }
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    int uidRemoved = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (JobSchedulerService.DEBUG) {
                        Slog.d(JobSchedulerService.TAG, "Removing jobs for uid: " + uidRemoved);
                    }
                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, uidRemoved, "app uninstalled");
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "Removing jobs for user: " + userId);
                }
                JobSchedulerService.this.cancelJobsForUser(userId);
            } else if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                if (pkgUid != -1) {
                    synchronized (JobSchedulerService.this.mLock) {
                        jobsForUid = JobSchedulerService.this.mJobs.getJobsByUid(pkgUid);
                    }
                    for (int i2 = jobsForUid.size() - 1; i2 >= 0; i2--) {
                        if (jobsForUid.get(i2).getSourcePackageName().equals(pkgName)) {
                            if (JobSchedulerService.DEBUG) {
                                Slog.d(JobSchedulerService.TAG, "Restart query: package " + pkgName + " at uid " + pkgUid + " has jobs");
                            }
                            setResultCode(-1);
                            return;
                        }
                    }
                }
            } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action) && pkgUid != -1) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "Removing jobs for pkg " + pkgName + " at uid " + pkgUid);
                }
                JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid, "app force stopped");
            }
        }
    };
    final Constants mConstants;
    final ConstantsObserver mConstantsObserver;
    private final List<StateController> mControllers;
    /* access modifiers changed from: private */
    public final DeviceIdleJobsController mDeviceIdleJobsController;
    final JobHandler mHandler;
    long mHeartbeat = 0;
    final HeartbeatAlarmListener mHeartbeatAlarm = new HeartbeatAlarmListener();
    /* access modifiers changed from: private */
    public final HwTimeController mHwTimeController;
    volatile boolean mInParole;
    private final Predicate<Integer> mIsUidActivePredicate = new Predicate() {
        public final boolean test(Object obj) {
            return JobSchedulerService.this.isUidActive(((Integer) obj).intValue());
        }
    };
    final JobPackageTracker mJobPackageTracker = new JobPackageTracker();
    final JobSchedulerStub mJobSchedulerStub;
    /* access modifiers changed from: private */
    public final Runnable mJobTimeUpdater = new Runnable() {
        public final void run() {
            JobSchedulerService.lambda$new$1(JobSchedulerService.this);
        }
    };
    final JobStore mJobs;
    long mLastHeartbeatTime = sElapsedRealtimeClock.millis();
    final SparseArray<HashMap<String, Long>> mLastJobHeartbeats = new SparseArray<>();
    DeviceIdleController.LocalService mLocalDeviceIdleController;
    PackageManagerInternal mLocalPM = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));
    final Object mLock = new Object();
    int mMaxActiveJobs = 1;
    private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();
    final long[] mNextBucketHeartbeat = {0, 0, 0, 0, JobStatus.NO_LATEST_RUNTIME};
    final ArrayList<JobStatus> mPendingJobs = new ArrayList<>();
    private final ReadyJobQueueFunctor mReadyQueueFunctor = new ReadyJobQueueFunctor();
    boolean mReadyToRock;
    boolean mReportedActive;
    final StandbyTracker mStandbyTracker;
    int[] mStartedUsers = EmptyArray.INT;
    private final StorageController mStorageController;
    private final BroadcastReceiver mTimeSetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction()) && JobSchedulerService.this.mJobs.clockNowValidToInflate(JobSchedulerService.sSystemClock.millis())) {
                Slog.i(JobSchedulerService.TAG, "RTC now valid; recalculating persisted job windows");
                context.unregisterReceiver(this);
                FgThread.getHandler().post(JobSchedulerService.this.mJobTimeUpdater);
            }
        }
    };
    boolean[] mTmpAssignAct = new boolean[16];
    JobStatus[] mTmpAssignContextIdToJobMap = new JobStatus[16];
    int[] mTmpAssignPreferredUidForContext = new int[16];
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            JobSchedulerService.this.mHandler.obtainMessage(4, uid, procState).sendToTarget();
        }

        public void onUidGone(int uid, boolean disabled) {
            JobSchedulerService.this.mHandler.obtainMessage(5, uid, disabled).sendToTarget();
        }

        public void onUidActive(int uid) throws RemoteException {
            JobSchedulerService.this.mHandler.obtainMessage(6, uid, 0).sendToTarget();
        }

        public void onUidIdle(int uid, boolean disabled) {
            JobSchedulerService.this.mHandler.obtainMessage(7, uid, disabled).sendToTarget();
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    final SparseIntArray mUidPriorityOverride = new SparseIntArray();
    final UsageStatsManagerInternal mUsageStats;

    public static class Constants {
        private static final int DEFAULT_BG_CRITICAL_JOB_COUNT = 1;
        private static final int DEFAULT_BG_LOW_JOB_COUNT = 1;
        private static final int DEFAULT_BG_MODERATE_JOB_COUNT = 4;
        private static final int DEFAULT_BG_NORMAL_JOB_COUNT = 6;
        private static final float DEFAULT_CONN_CONGESTION_DELAY_FRAC = 0.5f;
        private static final float DEFAULT_CONN_PREFETCH_RELAX_FRAC = 0.5f;
        private static final int DEFAULT_FG_JOB_COUNT = 4;
        private static final float DEFAULT_HEAVY_USE_FACTOR = 0.9f;
        private static final int DEFAULT_MAX_STANDARD_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        private static final int DEFAULT_MAX_WORK_RESCHEDULE_COUNT = Integer.MAX_VALUE;
        private static final int DEFAULT_MIN_BATTERY_NOT_LOW_COUNT = 1;
        private static final int DEFAULT_MIN_CHARGING_COUNT = 1;
        private static final int DEFAULT_MIN_CONNECTIVITY_COUNT = 1;
        private static final int DEFAULT_MIN_CONTENT_COUNT = 1;
        private static final long DEFAULT_MIN_EXP_BACKOFF_TIME = 10000;
        private static final int DEFAULT_MIN_IDLE_COUNT = 1;
        private static final long DEFAULT_MIN_LINEAR_BACKOFF_TIME = 10000;
        private static final int DEFAULT_MIN_READY_JOBS_COUNT = 1;
        private static final int DEFAULT_MIN_STORAGE_NOT_LOW_COUNT = 1;
        private static final float DEFAULT_MODERATE_USE_FACTOR = 0.5f;
        private static final int DEFAULT_STANDBY_FREQUENT_BEATS = 43;
        private static final long DEFAULT_STANDBY_HEARTBEAT_TIME = 660000;
        private static final int DEFAULT_STANDBY_RARE_BEATS = 130;
        private static final int DEFAULT_STANDBY_WORKING_BEATS = 11;
        private static final String KEY_BG_CRITICAL_JOB_COUNT = "bg_critical_job_count";
        private static final String KEY_BG_LOW_JOB_COUNT = "bg_low_job_count";
        private static final String KEY_BG_MODERATE_JOB_COUNT = "bg_moderate_job_count";
        private static final String KEY_BG_NORMAL_JOB_COUNT = "bg_normal_job_count";
        private static final String KEY_CONN_CONGESTION_DELAY_FRAC = "conn_congestion_delay_frac";
        private static final String KEY_CONN_PREFETCH_RELAX_FRAC = "conn_prefetch_relax_frac";
        private static final String KEY_FG_JOB_COUNT = "fg_job_count";
        private static final String KEY_HEAVY_USE_FACTOR = "heavy_use_factor";
        private static final String KEY_MAX_STANDARD_RESCHEDULE_COUNT = "max_standard_reschedule_count";
        private static final String KEY_MAX_WORK_RESCHEDULE_COUNT = "max_work_reschedule_count";
        private static final String KEY_MIN_BATTERY_NOT_LOW_COUNT = "min_battery_not_low_count";
        private static final String KEY_MIN_CHARGING_COUNT = "min_charging_count";
        private static final String KEY_MIN_CONNECTIVITY_COUNT = "min_connectivity_count";
        private static final String KEY_MIN_CONTENT_COUNT = "min_content_count";
        private static final String KEY_MIN_EXP_BACKOFF_TIME = "min_exp_backoff_time";
        private static final String KEY_MIN_IDLE_COUNT = "min_idle_count";
        private static final String KEY_MIN_LINEAR_BACKOFF_TIME = "min_linear_backoff_time";
        private static final String KEY_MIN_READY_JOBS_COUNT = "min_ready_jobs_count";
        private static final String KEY_MIN_STORAGE_NOT_LOW_COUNT = "min_storage_not_low_count";
        private static final String KEY_MODERATE_USE_FACTOR = "moderate_use_factor";
        private static final String KEY_STANDBY_FREQUENT_BEATS = "standby_frequent_beats";
        private static final String KEY_STANDBY_HEARTBEAT_TIME = "standby_heartbeat_time";
        private static final String KEY_STANDBY_RARE_BEATS = "standby_rare_beats";
        private static final String KEY_STANDBY_WORKING_BEATS = "standby_working_beats";
        int BG_CRITICAL_JOB_COUNT = 1;
        int BG_LOW_JOB_COUNT = 1;
        int BG_MODERATE_JOB_COUNT = 4;
        int BG_NORMAL_JOB_COUNT = 6;
        public float CONN_CONGESTION_DELAY_FRAC = 0.5f;
        public float CONN_PREFETCH_RELAX_FRAC = 0.5f;
        int FG_JOB_COUNT = 4;
        float HEAVY_USE_FACTOR = DEFAULT_HEAVY_USE_FACTOR;
        int MAX_STANDARD_RESCHEDULE_COUNT = HwBootFail.STAGE_BOOT_SUCCESS;
        int MAX_WORK_RESCHEDULE_COUNT = HwBootFail.STAGE_BOOT_SUCCESS;
        int MIN_BATTERY_NOT_LOW_COUNT = 1;
        int MIN_CHARGING_COUNT = 1;
        int MIN_CONNECTIVITY_COUNT = 1;
        int MIN_CONTENT_COUNT = 1;
        long MIN_EXP_BACKOFF_TIME = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        int MIN_IDLE_COUNT = 1;
        long MIN_LINEAR_BACKOFF_TIME = JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY;
        int MIN_READY_JOBS_COUNT = 1;
        int MIN_STORAGE_NOT_LOW_COUNT = 1;
        float MODERATE_USE_FACTOR = 0.5f;
        final int[] STANDBY_BEATS = {0, 11, 43, DEFAULT_STANDBY_RARE_BEATS};
        long STANDBY_HEARTBEAT_TIME = DEFAULT_STANDBY_HEARTBEAT_TIME;
        private final KeyValueListParser mParser = new KeyValueListParser(',');

        /* access modifiers changed from: package-private */
        public void updateConstantsLocked(String value) {
            try {
                this.mParser.setString(value);
            } catch (Exception e) {
                Slog.e(JobSchedulerService.TAG, "Bad jobscheduler settings", e);
            }
            this.MIN_IDLE_COUNT = this.mParser.getInt(KEY_MIN_IDLE_COUNT, 1);
            this.MIN_CHARGING_COUNT = this.mParser.getInt(KEY_MIN_CHARGING_COUNT, 1);
            this.MIN_BATTERY_NOT_LOW_COUNT = this.mParser.getInt(KEY_MIN_BATTERY_NOT_LOW_COUNT, 1);
            this.MIN_STORAGE_NOT_LOW_COUNT = this.mParser.getInt(KEY_MIN_STORAGE_NOT_LOW_COUNT, 1);
            this.MIN_CONNECTIVITY_COUNT = this.mParser.getInt(KEY_MIN_CONNECTIVITY_COUNT, 1);
            this.MIN_CONTENT_COUNT = this.mParser.getInt(KEY_MIN_CONTENT_COUNT, 1);
            this.MIN_READY_JOBS_COUNT = this.mParser.getInt(KEY_MIN_READY_JOBS_COUNT, 1);
            this.HEAVY_USE_FACTOR = this.mParser.getFloat(KEY_HEAVY_USE_FACTOR, DEFAULT_HEAVY_USE_FACTOR);
            this.MODERATE_USE_FACTOR = this.mParser.getFloat(KEY_MODERATE_USE_FACTOR, 0.5f);
            this.FG_JOB_COUNT = this.mParser.getInt(KEY_FG_JOB_COUNT, 4);
            this.BG_NORMAL_JOB_COUNT = this.mParser.getInt(KEY_BG_NORMAL_JOB_COUNT, 6);
            if (this.FG_JOB_COUNT + this.BG_NORMAL_JOB_COUNT > 16) {
                this.BG_NORMAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
            }
            this.BG_MODERATE_JOB_COUNT = this.mParser.getInt(KEY_BG_MODERATE_JOB_COUNT, 4);
            if (this.FG_JOB_COUNT + this.BG_MODERATE_JOB_COUNT > 16) {
                this.BG_MODERATE_JOB_COUNT = 16 - this.FG_JOB_COUNT;
            }
            this.BG_LOW_JOB_COUNT = this.mParser.getInt(KEY_BG_LOW_JOB_COUNT, 1);
            if (this.FG_JOB_COUNT + this.BG_LOW_JOB_COUNT > 16) {
                this.BG_LOW_JOB_COUNT = 16 - this.FG_JOB_COUNT;
            }
            this.BG_CRITICAL_JOB_COUNT = this.mParser.getInt(KEY_BG_CRITICAL_JOB_COUNT, 1);
            if (this.FG_JOB_COUNT + this.BG_CRITICAL_JOB_COUNT > 16) {
                this.BG_CRITICAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
            }
            this.MAX_STANDARD_RESCHEDULE_COUNT = this.mParser.getInt(KEY_MAX_STANDARD_RESCHEDULE_COUNT, HwBootFail.STAGE_BOOT_SUCCESS);
            this.MAX_WORK_RESCHEDULE_COUNT = this.mParser.getInt(KEY_MAX_WORK_RESCHEDULE_COUNT, HwBootFail.STAGE_BOOT_SUCCESS);
            this.MIN_LINEAR_BACKOFF_TIME = this.mParser.getDurationMillis(KEY_MIN_LINEAR_BACKOFF_TIME, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            this.MIN_EXP_BACKOFF_TIME = this.mParser.getDurationMillis(KEY_MIN_EXP_BACKOFF_TIME, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            this.STANDBY_HEARTBEAT_TIME = this.mParser.getDurationMillis(KEY_STANDBY_HEARTBEAT_TIME, DEFAULT_STANDBY_HEARTBEAT_TIME);
            this.STANDBY_BEATS[1] = this.mParser.getInt(KEY_STANDBY_WORKING_BEATS, 11);
            this.STANDBY_BEATS[2] = this.mParser.getInt(KEY_STANDBY_FREQUENT_BEATS, 43);
            this.STANDBY_BEATS[3] = this.mParser.getInt(KEY_STANDBY_RARE_BEATS, DEFAULT_STANDBY_RARE_BEATS);
            this.CONN_CONGESTION_DELAY_FRAC = this.mParser.getFloat(KEY_CONN_CONGESTION_DELAY_FRAC, 0.5f);
            this.CONN_PREFETCH_RELAX_FRAC = this.mParser.getFloat(KEY_CONN_PREFETCH_RELAX_FRAC, 0.5f);
        }

        /* access modifiers changed from: package-private */
        public void dump(IndentingPrintWriter pw) {
            pw.println("Settings:");
            pw.increaseIndent();
            pw.printPair(KEY_MIN_IDLE_COUNT, Integer.valueOf(this.MIN_IDLE_COUNT)).println();
            pw.printPair(KEY_MIN_CHARGING_COUNT, Integer.valueOf(this.MIN_CHARGING_COUNT)).println();
            pw.printPair(KEY_MIN_BATTERY_NOT_LOW_COUNT, Integer.valueOf(this.MIN_BATTERY_NOT_LOW_COUNT)).println();
            pw.printPair(KEY_MIN_STORAGE_NOT_LOW_COUNT, Integer.valueOf(this.MIN_STORAGE_NOT_LOW_COUNT)).println();
            pw.printPair(KEY_MIN_CONNECTIVITY_COUNT, Integer.valueOf(this.MIN_CONNECTIVITY_COUNT)).println();
            pw.printPair(KEY_MIN_CONTENT_COUNT, Integer.valueOf(this.MIN_CONTENT_COUNT)).println();
            pw.printPair(KEY_MIN_READY_JOBS_COUNT, Integer.valueOf(this.MIN_READY_JOBS_COUNT)).println();
            pw.printPair(KEY_HEAVY_USE_FACTOR, Float.valueOf(this.HEAVY_USE_FACTOR)).println();
            pw.printPair(KEY_MODERATE_USE_FACTOR, Float.valueOf(this.MODERATE_USE_FACTOR)).println();
            pw.printPair(KEY_FG_JOB_COUNT, Integer.valueOf(this.FG_JOB_COUNT)).println();
            pw.printPair(KEY_BG_NORMAL_JOB_COUNT, Integer.valueOf(this.BG_NORMAL_JOB_COUNT)).println();
            pw.printPair(KEY_BG_MODERATE_JOB_COUNT, Integer.valueOf(this.BG_MODERATE_JOB_COUNT)).println();
            pw.printPair(KEY_BG_LOW_JOB_COUNT, Integer.valueOf(this.BG_LOW_JOB_COUNT)).println();
            pw.printPair(KEY_BG_CRITICAL_JOB_COUNT, Integer.valueOf(this.BG_CRITICAL_JOB_COUNT)).println();
            pw.printPair(KEY_MAX_STANDARD_RESCHEDULE_COUNT, Integer.valueOf(this.MAX_STANDARD_RESCHEDULE_COUNT)).println();
            pw.printPair(KEY_MAX_WORK_RESCHEDULE_COUNT, Integer.valueOf(this.MAX_WORK_RESCHEDULE_COUNT)).println();
            pw.printPair(KEY_MIN_LINEAR_BACKOFF_TIME, Long.valueOf(this.MIN_LINEAR_BACKOFF_TIME)).println();
            pw.printPair(KEY_MIN_EXP_BACKOFF_TIME, Long.valueOf(this.MIN_EXP_BACKOFF_TIME)).println();
            pw.printPair(KEY_STANDBY_HEARTBEAT_TIME, Long.valueOf(this.STANDBY_HEARTBEAT_TIME)).println();
            pw.print("standby_beats={");
            pw.print(this.STANDBY_BEATS[0]);
            for (int i = 1; i < this.STANDBY_BEATS.length; i++) {
                pw.print(", ");
                pw.print(this.STANDBY_BEATS[i]);
            }
            pw.println('}');
            pw.printPair(KEY_CONN_CONGESTION_DELAY_FRAC, Float.valueOf(this.CONN_CONGESTION_DELAY_FRAC)).println();
            pw.printPair(KEY_CONN_PREFETCH_RELAX_FRAC, Float.valueOf(this.CONN_PREFETCH_RELAX_FRAC)).println();
            pw.decreaseIndent();
        }

        /* access modifiers changed from: package-private */
        public void dump(ProtoOutputStream proto, long fieldId) {
            long token = proto.start(fieldId);
            proto.write(1120986464257L, this.MIN_IDLE_COUNT);
            proto.write(1120986464258L, this.MIN_CHARGING_COUNT);
            proto.write(1120986464259L, this.MIN_BATTERY_NOT_LOW_COUNT);
            proto.write(1120986464260L, this.MIN_STORAGE_NOT_LOW_COUNT);
            proto.write(1120986464261L, this.MIN_CONNECTIVITY_COUNT);
            proto.write(1120986464262L, this.MIN_CONTENT_COUNT);
            proto.write(1120986464263L, this.MIN_READY_JOBS_COUNT);
            proto.write(1103806595080L, this.HEAVY_USE_FACTOR);
            proto.write(1103806595081L, this.MODERATE_USE_FACTOR);
            proto.write(1120986464266L, this.FG_JOB_COUNT);
            proto.write(1120986464267L, this.BG_NORMAL_JOB_COUNT);
            proto.write(1120986464268L, this.BG_MODERATE_JOB_COUNT);
            proto.write(1120986464269L, this.BG_LOW_JOB_COUNT);
            proto.write(1120986464270L, this.BG_CRITICAL_JOB_COUNT);
            proto.write(1120986464271L, this.MAX_STANDARD_RESCHEDULE_COUNT);
            proto.write(1120986464272L, this.MAX_WORK_RESCHEDULE_COUNT);
            proto.write(1112396529681L, this.MIN_LINEAR_BACKOFF_TIME);
            proto.write(1112396529682L, this.MIN_EXP_BACKOFF_TIME);
            proto.write(1112396529683L, this.STANDBY_HEARTBEAT_TIME);
            for (int period : this.STANDBY_BEATS) {
                proto.write(2220498092052L, period);
            }
            proto.write(1103806595093L, this.CONN_CONGESTION_DELAY_FRAC);
            proto.write(1103806595094L, this.CONN_PREFETCH_RELAX_FRAC);
            proto.end(token);
        }
    }

    private class ConstantsObserver extends ContentObserver {
        private ContentResolver mResolver;

        public ConstantsObserver(Handler handler) {
            super(handler);
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("job_scheduler_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (JobSchedulerService.this.mLock) {
                try {
                    JobSchedulerService.this.mConstants.updateConstantsLocked(Settings.Global.getString(this.mResolver, "job_scheduler_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(JobSchedulerService.TAG, "Bad jobscheduler settings", e);
                }
            }
            JobSchedulerService.this.setNextHeartbeatAlarm();
        }
    }

    static class DeferredJobCounter implements Consumer<JobStatus> {
        private int mDeferred = 0;

        DeferredJobCounter() {
        }

        public int numDeferred() {
            return this.mDeferred;
        }

        public void accept(JobStatus job) {
            if (job.getWhenStandbyDeferred() > 0) {
                this.mDeferred++;
            }
        }
    }

    class HeartbeatAlarmListener implements AlarmManager.OnAlarmListener {
        HeartbeatAlarmListener() {
        }

        public void onAlarm() {
            synchronized (JobSchedulerService.this.mLock) {
                long beatsElapsed = (JobSchedulerService.sElapsedRealtimeClock.millis() - JobSchedulerService.this.mLastHeartbeatTime) / JobSchedulerService.this.mConstants.STANDBY_HEARTBEAT_TIME;
                if (beatsElapsed > 0) {
                    JobSchedulerService.this.mLastHeartbeatTime += JobSchedulerService.this.mConstants.STANDBY_HEARTBEAT_TIME * beatsElapsed;
                    JobSchedulerService.this.advanceHeartbeatLocked(beatsElapsed);
                }
            }
            JobSchedulerService.this.setNextHeartbeatAlarm();
        }
    }

    private final class JobHandler extends Handler {
        public JobHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:72:0x00e9, code lost:
            return;
         */
        public void handleMessage(Message message) {
            synchronized (JobSchedulerService.this.mLock) {
                if (!JobSchedulerService.this.mReadyToRock) {
                    Slog.i(JobSchedulerService.TAG, "handleMessage mReadyToRock false");
                    return;
                }
                boolean needRemoveCheckJobMsg = false;
                switch (message.what) {
                    case 0:
                        JobStatus runNow = (JobStatus) message.obj;
                        if (runNow != null && JobSchedulerService.this.isReadyToBeExecutedLocked(runNow)) {
                            JobSchedulerService.this.mJobPackageTracker.notePending(runNow);
                            JobSchedulerService.addOrderedItem(JobSchedulerService.this.mPendingJobs, runNow, JobSchedulerService.mEnqueueTimeComparator);
                            break;
                        } else {
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                            needRemoveCheckJobMsg = true;
                            break;
                        }
                    case 1:
                        if (JobSchedulerService.this.mReportedActive) {
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                        } else {
                            JobSchedulerService.this.maybeQueueReadyJobsForExecutionLocked();
                        }
                        needRemoveCheckJobMsg = true;
                        break;
                    case 2:
                        JobSchedulerService.this.cancelJobImplLocked((JobStatus) message.obj, null, "app no longer allowed to run");
                        break;
                    case 3:
                        JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                        needRemoveCheckJobMsg = true;
                        break;
                    case 4:
                        JobSchedulerService.this.updateUidState(message.arg1, message.arg2);
                        break;
                    case 5:
                        int uid = message.arg1;
                        boolean disabled = message.arg2 != 0;
                        JobSchedulerService.this.updateUidState(uid, 18);
                        if (disabled) {
                            JobSchedulerService.this.cancelJobsForUid(uid, "uid gone");
                        }
                        synchronized (JobSchedulerService.this.mLock) {
                            JobSchedulerService.this.mDeviceIdleJobsController.setUidActiveLocked(uid, false);
                        }
                        break;
                    case 6:
                        int uid2 = message.arg1;
                        synchronized (JobSchedulerService.this.mLock) {
                            JobSchedulerService.this.mDeviceIdleJobsController.setUidActiveLocked(uid2, true);
                        }
                        break;
                    case 7:
                        int uid3 = message.arg1;
                        if (message.arg2 != 0) {
                            JobSchedulerService.this.cancelJobsForUid(uid3, "app uid idle");
                        }
                        synchronized (JobSchedulerService.this.mLock) {
                            JobSchedulerService.this.mDeviceIdleJobsController.setUidActiveLocked(uid3, false);
                        }
                        break;
                }
                JobSchedulerService.this.maybeRunPendingJobsLocked();
                if (needRemoveCheckJobMsg) {
                    removeMessages(1);
                }
            }
        }
    }

    final class JobSchedulerStub extends IJobScheduler.Stub {
        private final SparseArray<Boolean> mPersistCache = new SparseArray<>();

        JobSchedulerStub() {
        }

        private void enforceValidJobRequest(int uid, JobInfo job) {
            IPackageManager pm = AppGlobals.getPackageManager();
            ComponentName service = job.getService();
            try {
                ServiceInfo si = pm.getServiceInfo(service, 786432, UserHandle.getUserId(uid));
                if (si == null) {
                    throw new IllegalArgumentException("No such service " + service);
                } else if (si.applicationInfo.uid != uid) {
                    throw new IllegalArgumentException("uid " + uid + " cannot schedule job in " + service.getPackageName());
                } else if (!"android.permission.BIND_JOB_SERVICE".equals(si.permission)) {
                    throw new IllegalArgumentException("Scheduled service " + service + " does not require android.permission.BIND_JOB_SERVICE permission");
                }
            } catch (RemoteException e) {
            }
        }

        private boolean canPersistJobs(int pid, int uid) {
            boolean canPersist;
            synchronized (this.mPersistCache) {
                Boolean cached = this.mPersistCache.get(uid);
                if (cached != null) {
                    canPersist = cached.booleanValue();
                } else {
                    boolean canPersist2 = JobSchedulerService.this.getContext().checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", pid, uid) == 0;
                    this.mPersistCache.put(uid, Boolean.valueOf(canPersist2));
                    canPersist = canPersist2;
                }
            }
            return canPersist;
        }

        private void validateJobFlags(JobInfo job, int callingUid) {
            if ((job.getFlags() & 1) != 0) {
                JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
            }
            if ((job.getFlags() & 8) == 0) {
                return;
            }
            if (callingUid != 1000) {
                throw new SecurityException("Job has invalid flags");
            } else if (job.isPeriodic()) {
                Slog.wtf(JobSchedulerService.TAG, "Periodic jobs mustn't have FLAG_EXEMPT_FROM_APP_STANDBY. Job=" + job);
            }
        }

        public int schedule(JobInfo job) throws RemoteException {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.JOBSCHEDULER_SCHEDULE);
            }
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Scheduling job: " + job.toString());
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getUserId(uid);
            enforceValidJobRequest(uid, job);
            if (!job.isPersisted() || canPersistJobs(pid, uid)) {
                validateJobFlags(job, uid);
                long ident = Binder.clearCallingIdentity();
                try {
                    return JobSchedulerService.this.scheduleAsPackage(job, null, uid, null, userId, null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.");
            }
        }

        public int enqueue(JobInfo job, JobWorkItem work) throws RemoteException {
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Enqueueing job: " + job.toString() + " work: " + work);
            }
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getUserId(uid);
            enforceValidJobRequest(uid, job);
            if (job.isPersisted()) {
                throw new IllegalArgumentException("Can't enqueue work for persisted jobs");
            } else if (work != null) {
                validateJobFlags(job, uid);
                long ident = Binder.clearCallingIdentity();
                try {
                    return JobSchedulerService.this.scheduleAsPackage(job, work, uid, null, userId, null);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new NullPointerException("work is null");
            }
        }

        public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) throws RemoteException {
            int callerUid = Binder.getCallingUid();
            if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "Caller uid " + callerUid + " scheduling job: " + job.toString() + " on behalf of " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER);
            }
            if (packageName == null) {
                throw new NullPointerException("Must specify a package for scheduleAsPackage()");
            } else if (JobSchedulerService.this.getContext().checkCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS") == 0) {
                validateJobFlags(job, callerUid);
                long ident = Binder.clearCallingIdentity();
                try {
                    return JobSchedulerService.this.scheduleAsPackage(job, null, callerUid, packageName, userId, tag);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new SecurityException("Caller uid " + callerUid + " not permitted to schedule jobs for other apps");
            }
        }

        public List<JobInfo> getAllPendingJobs() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.getPendingJobs(uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public JobInfo getPendingJob(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.getPendingJob(uid, jobId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancelAll() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService jobSchedulerService = JobSchedulerService.this;
                jobSchedulerService.cancelJobsForUid(uid, "cancelAll() called by app, callingUid=" + uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancel(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJob(uid, jobId, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            boolean proto;
            if (DumpUtils.checkDumpAndUsageStatsPermission(JobSchedulerService.this.getContext(), JobSchedulerService.TAG, pw)) {
                int filterUid = -1;
                if (!ArrayUtils.isEmpty(args)) {
                    proto = false;
                    int opti = 0;
                    while (true) {
                        if (opti >= args.length) {
                            break;
                        }
                        String arg = args[opti];
                        if ("-h".equals(arg)) {
                            JobSchedulerService.dumpHelp(pw);
                            return;
                        }
                        if (!"-a".equals(arg)) {
                            if (PriorityDump.PROTO_ARG.equals(arg)) {
                                proto = true;
                            } else if (arg.length() > 0 && arg.charAt(0) == '-') {
                                pw.println("Unknown option: " + arg);
                                return;
                            }
                        }
                        opti++;
                    }
                    if (opti < args.length) {
                        try {
                            filterUid = JobSchedulerService.this.getContext().getPackageManager().getPackageUid(args[opti], DumpState.DUMP_CHANGES);
                        } catch (PackageManager.NameNotFoundException e) {
                            pw.println("Invalid package: " + pkg);
                            return;
                        }
                    }
                } else {
                    proto = false;
                }
                long identityToken = Binder.clearCallingIdentity();
                if (proto) {
                    try {
                        JobSchedulerService.this.dumpInternalProto(fd, filterUid);
                    } finally {
                        Binder.restoreCallingIdentity(identityToken);
                    }
                } else {
                    JobSchedulerService.this.dumpInternal(new IndentingPrintWriter(pw, "  "), filterUid);
                }
            }
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new JobSchedulerShellCommand(JobSchedulerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    final class LocalService implements JobSchedulerInternal {
        LocalService() {
        }

        public long currentHeartbeat() {
            return JobSchedulerService.this.getCurrentHeartbeat();
        }

        public long nextHeartbeatForBucket(int bucket) {
            long j;
            synchronized (JobSchedulerService.this.mLock) {
                j = JobSchedulerService.this.mNextBucketHeartbeat[bucket];
            }
            return j;
        }

        public long baseHeartbeatForApp(String packageName, int userId, int appStandbyBucket) {
            if (appStandbyBucket == 0 || appStandbyBucket >= JobSchedulerService.this.mConstants.STANDBY_BEATS.length) {
                if (JobSchedulerService.DEBUG_STANDBY) {
                    Slog.v(JobSchedulerService.TAG, "Base heartbeat forced ZERO for new job in " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + userId);
                }
                return 0;
            }
            long baseHeartbeat = JobSchedulerService.this.heartbeatWhenJobsLastRun(packageName, userId);
            if (JobSchedulerService.DEBUG_STANDBY) {
                Slog.v(JobSchedulerService.TAG, "Base heartbeat " + baseHeartbeat + " for new job in " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + userId);
            }
            return baseHeartbeat;
        }

        public void noteJobStart(String packageName, int userId) {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.setLastJobHeartbeatLocked(packageName, userId, JobSchedulerService.this.mHeartbeat);
            }
        }

        public List<JobInfo> getSystemScheduledPendingJobs() {
            List<JobInfo> pendingJobs;
            synchronized (JobSchedulerService.this.mLock) {
                pendingJobs = new ArrayList<>();
                JobSchedulerService.this.mJobs.forEachJob(1000, (Consumer<JobStatus>) new Consumer(pendingJobs) {
                    private final /* synthetic */ List f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        JobSchedulerService.LocalService.lambda$getSystemScheduledPendingJobs$0(JobSchedulerService.LocalService.this, this.f$1, (JobStatus) obj);
                    }
                });
            }
            return pendingJobs;
        }

        public static /* synthetic */ void lambda$getSystemScheduledPendingJobs$0(LocalService localService, List pendingJobs, JobStatus job) {
            if (job.getJob().isPeriodic() || !JobSchedulerService.this.isCurrentlyActiveLocked(job)) {
                pendingJobs.add(job.getJob());
            }
        }

        public boolean proxyService(int type, List<String> value) {
            boolean proxyServiceLocked;
            synchronized (JobSchedulerService.this.mLock) {
                proxyServiceLocked = JobSchedulerService.this.mHwTimeController.proxyServiceLocked(type, value);
            }
            return proxyServiceLocked;
        }

        public void cancelJobsForUid(int uid, String reason) {
            JobSchedulerService.this.cancelJobsForUid(uid, reason);
        }

        public void addBackingUpUid(int uid) {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.mBackingUpUids.put(uid, uid);
            }
        }

        public void removeBackingUpUid(int uid) {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.mBackingUpUids.delete(uid);
                if (JobSchedulerService.this.mJobs.countJobsForUid(uid) > 0) {
                    JobSchedulerService.this.mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }

        public void clearAllBackingUpUids() {
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.this.mBackingUpUids.size() > 0) {
                    JobSchedulerService.this.mBackingUpUids.clear();
                    JobSchedulerService.this.mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }

        public void reportAppUsage(String packageName, int userId) {
            JobSchedulerService.this.reportAppUsage(packageName, userId);
        }

        public JobSchedulerInternal.JobStorePersistStats getPersistStats() {
            JobSchedulerInternal.JobStorePersistStats jobStorePersistStats;
            synchronized (JobSchedulerService.this.mLock) {
                jobStorePersistStats = new JobSchedulerInternal.JobStorePersistStats(JobSchedulerService.this.mJobs.getPersistStats());
            }
            return jobStorePersistStats;
        }
    }

    final class MaybeReadyJobQueueFunctor implements Consumer<JobStatus> {
        int backoffCount;
        int batteryNotLowCount;
        int chargingCount;
        int connectivityCount;
        int contentCount;
        int idleCount;
        List<JobStatus> runnableJobs;
        int storageNotLowCount;

        public MaybeReadyJobQueueFunctor() {
            reset();
        }

        public void accept(JobStatus job) {
            if (JobSchedulerService.this.isReadyToBeExecutedLocked(job)) {
                try {
                    if (ActivityManager.getService().isAppStartModeDisabled(job.getUid(), job.getJob().getService().getPackageName())) {
                        Slog.w(JobSchedulerService.TAG, "Aborting job " + job.getUid() + ":" + job.getJob().toString() + " -- package not allowed to start");
                        JobSchedulerService.this.mHandler.obtainMessage(2, job).sendToTarget();
                        return;
                    }
                } catch (RemoteException e) {
                }
                if (job.getNumFailures() > 0) {
                    this.backoffCount++;
                }
                if (job.hasIdleConstraint()) {
                    this.idleCount++;
                }
                if (job.hasConnectivityConstraint()) {
                    this.connectivityCount++;
                }
                if (job.hasChargingConstraint()) {
                    this.chargingCount++;
                }
                if (job.hasBatteryNotLowConstraint()) {
                    this.batteryNotLowCount++;
                }
                if (job.hasStorageNotLowConstraint()) {
                    this.storageNotLowCount++;
                }
                if (job.hasContentTriggerConstraint()) {
                    this.contentCount++;
                }
                if (this.runnableJobs == null) {
                    this.runnableJobs = new ArrayList();
                }
                this.runnableJobs.add(job);
            }
        }

        public void postProcess() {
            if (this.backoffCount > 0 || this.idleCount >= JobSchedulerService.this.mConstants.MIN_IDLE_COUNT || this.connectivityCount >= JobSchedulerService.this.mConstants.MIN_CONNECTIVITY_COUNT || this.chargingCount >= JobSchedulerService.this.mConstants.MIN_CHARGING_COUNT || this.batteryNotLowCount >= JobSchedulerService.this.mConstants.MIN_BATTERY_NOT_LOW_COUNT || this.storageNotLowCount >= JobSchedulerService.this.mConstants.MIN_STORAGE_NOT_LOW_COUNT || this.contentCount >= JobSchedulerService.this.mConstants.MIN_CONTENT_COUNT || (this.runnableJobs != null && this.runnableJobs.size() >= JobSchedulerService.this.mConstants.MIN_READY_JOBS_COUNT)) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLocked: Running jobs.");
                }
                JobSchedulerService.this.noteJobsPending(this.runnableJobs);
                JobSchedulerService.this.mPendingJobs.addAll(this.runnableJobs);
                if (JobSchedulerService.this.mPendingJobs.size() > 1) {
                    JobSchedulerService.this.mPendingJobs.sort(JobSchedulerService.mEnqueueTimeComparator);
                }
            } else if (JobSchedulerService.DEBUG) {
                Slog.d(JobSchedulerService.TAG, "maybeQueueReadyJobsForExecutionLocked: Not running anything.");
            }
            reset();
        }

        private void reset() {
            this.chargingCount = 0;
            this.idleCount = 0;
            this.backoffCount = 0;
            this.connectivityCount = 0;
            this.batteryNotLowCount = 0;
            this.storageNotLowCount = 0;
            this.contentCount = 0;
            this.runnableJobs = null;
        }
    }

    final class ReadyJobQueueFunctor implements Consumer<JobStatus> {
        ArrayList<JobStatus> newReadyJobs;

        ReadyJobQueueFunctor() {
        }

        public void accept(JobStatus job) {
            if (JobSchedulerService.this.isReadyToBeExecutedLocked(job)) {
                if (JobSchedulerService.DEBUG) {
                    Slog.d(JobSchedulerService.TAG, "    queued " + job.toShortString());
                }
                if (this.newReadyJobs == null) {
                    this.newReadyJobs = new ArrayList<>();
                }
                this.newReadyJobs.add(job);
            }
        }

        public void postProcess() {
            if (this.newReadyJobs != null) {
                JobSchedulerService.this.noteJobsPending(this.newReadyJobs);
                JobSchedulerService.this.mPendingJobs.addAll(this.newReadyJobs);
                if (JobSchedulerService.this.mPendingJobs.size() > 1) {
                    JobSchedulerService.this.mPendingJobs.sort(JobSchedulerService.mEnqueueTimeComparator);
                }
            }
            this.newReadyJobs = null;
        }
    }

    final class StandbyTracker extends UsageStatsManagerInternal.AppIdleStateChangeListener {
        StandbyTracker() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            int uid = JobSchedulerService.this.mLocalPM.getPackageUid(packageName, 8192, userId);
            if (uid < 0) {
                if (JobSchedulerService.DEBUG_STANDBY) {
                    Slog.i(JobSchedulerService.TAG, "App idle state change for unknown app " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + userId);
                }
                return;
            }
            BackgroundThread.getHandler().post(new Runnable(uid, JobSchedulerService.standbyBucketToBucketIndex(bucket), packageName) {
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ String f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    JobSchedulerService.StandbyTracker.lambda$onAppIdleStateChanged$1(JobSchedulerService.StandbyTracker.this, this.f$1, this.f$2, this.f$3);
                }
            });
        }

        public static /* synthetic */ void lambda$onAppIdleStateChanged$1(StandbyTracker standbyTracker, int uid, int bucketIndex, String packageName) {
            if (JobSchedulerService.DEBUG_STANDBY) {
                Slog.i(JobSchedulerService.TAG, "Moving uid " + uid + " to bucketIndex " + bucketIndex);
            }
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.mJobs.forEachJobForSourceUid(uid, new Consumer(packageName, bucketIndex) {
                    private final /* synthetic */ String f$0;
                    private final /* synthetic */ int f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        JobSchedulerService.StandbyTracker.lambda$onAppIdleStateChanged$0(this.f$0, this.f$1, (JobStatus) obj);
                    }
                });
                JobSchedulerService.this.onControllerStateChanged();
            }
        }

        static /* synthetic */ void lambda$onAppIdleStateChanged$0(String packageName, int bucketIndex, JobStatus job) {
            if (packageName.equals(job.getSourcePackageName())) {
                job.setStandbyBucket(bucketIndex);
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            if (JobSchedulerService.DEBUG_STANDBY) {
                StringBuilder sb = new StringBuilder();
                sb.append("Global parole state now ");
                sb.append(isParoleOn ? "ON" : "OFF");
                Slog.i(JobSchedulerService.TAG, sb.toString());
            }
            JobSchedulerService.this.mInParole = isParoleOn;
        }

        public void onUserInteractionStarted(String packageName, int userId) {
            int uid = JobSchedulerService.this.mLocalPM.getPackageUid(packageName, 8192, userId);
            if (uid >= 0) {
                long sinceLast = JobSchedulerService.this.mUsageStats.getTimeSinceLastJobRun(packageName, userId);
                if (sinceLast > 172800000) {
                    sinceLast = 0;
                }
                DeferredJobCounter counter = new DeferredJobCounter();
                synchronized (JobSchedulerService.this.mLock) {
                    JobSchedulerService.this.mJobs.forEachJobForSourceUid(uid, counter);
                }
                if (counter.numDeferred() > 0 || sinceLast > 0) {
                    ((BatteryStatsInternal) LocalServices.getService(BatteryStatsInternal.class)).noteJobsDeferred(uid, counter.numDeferred(), sinceLast);
                }
            }
        }
    }

    static /* synthetic */ int lambda$static$0(JobStatus o1, JobStatus o2) {
        if (o1.enqueueTime < o2.enqueueTime) {
            return -1;
        }
        return o1.enqueueTime > o2.enqueueTime ? 1 : 0;
    }

    static <T> void addOrderedItem(ArrayList<T> array, T newItem, Comparator<T> comparator) {
        int where = Collections.binarySearch(array, newItem, comparator);
        if (where < 0) {
            where = ~where;
        }
        array.add(where, newItem);
    }

    /* access modifiers changed from: private */
    public String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            return uri.getSchemeSpecificPart();
        }
        return null;
    }

    public Context getTestableContext() {
        return getContext();
    }

    public Object getLock() {
        return this.mLock;
    }

    public JobStore getJobStore() {
        return this.mJobs;
    }

    public Constants getConstants() {
        return this.mConstants;
    }

    public void onStartUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.appendInt(this.mStartedUsers, userHandle);
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onUnlockUser(int userHandle) {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onStopUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.removeInt(this.mStartedUsers, userHandle);
    }

    /* access modifiers changed from: private */
    public boolean isUidActive(int uid) {
        return this.mAppStateTracker.isUidActiveSynced(uid);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x010b, code lost:
        return 1;
     */
    public int scheduleAsPackage(JobInfo job, JobWorkItem work, int uId, String packageName, int userId, String tag) {
        JobInfo jobInfo = job;
        JobWorkItem jobWorkItem = work;
        int i = uId;
        String str = packageName;
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(i, job.getService().getPackageName())) {
                Slog.w(TAG, "Not scheduling job " + i + ":" + job.toString() + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mLock) {
            try {
                JobStatus toCancel = this.mJobs.getJobByUidAndJobId(i, job.getId());
                if (jobWorkItem == null || toCancel == null || !toCancel.getJob().equals(jobInfo)) {
                    JobStatus jobStatus = JobStatus.createFromJobInfo(jobInfo, i, str, userId, tag);
                    jobStatus.maybeAddForegroundExemption(this.mIsUidActivePredicate);
                    if (DEBUG) {
                        Slog.d(TAG, "SCHEDULE: " + jobStatus.toShortString());
                    }
                    if (str == null) {
                        if (this.mJobs.countJobsForUid(i) > 100) {
                            Slog.w(TAG, "Too many jobs for uid " + i);
                            throw new IllegalStateException("Apps may not schedule more than 100 distinct jobs");
                        }
                    }
                    jobStatus.prepareLocked(ActivityManager.getService());
                    if (toCancel != null) {
                        cancelJobImplLocked(toCancel, jobStatus, "job rescheduled by app");
                    }
                    if (jobWorkItem != null) {
                        jobStatus.enqueueWorkLocked(ActivityManager.getService(), jobWorkItem);
                    }
                    startTrackingJobLocked(jobStatus, toCancel);
                    JobStatus jobStatus2 = jobStatus;
                    StatsLog.write_non_chained(8, i, null, jobStatus.getBatteryName(), 2, 0);
                    if (isReadyToBeExecutedLocked(jobStatus2)) {
                        this.mJobPackageTracker.notePending(jobStatus2);
                        addOrderedItem(this.mPendingJobs, jobStatus2, mEnqueueTimeComparator);
                        maybeRunPendingJobsLocked();
                    }
                } else {
                    toCancel.enqueueWorkLocked(ActivityManager.getService(), jobWorkItem);
                    toCancel.maybeAddForegroundExemption(this.mIsUidActivePredicate);
                    return 1;
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public List<JobInfo> getPendingJobs(int uid) {
        ArrayList<JobInfo> outList;
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            outList = new ArrayList<>(jobs.size());
            for (int i = jobs.size() - 1; i >= 0; i--) {
                outList.add(jobs.get(i).getJob());
            }
        }
        return outList;
    }

    public JobInfo getPendingJob(int uid, int jobId) {
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            for (int i = jobs.size() - 1; i >= 0; i--) {
                JobStatus job = jobs.get(i);
                if (job.getJobId() == jobId) {
                    JobInfo job2 = job.getJob();
                    return job2;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelJobsForUser(int userHandle) {
        synchronized (this.mLock) {
            List<JobStatus> jobsForUser = this.mJobs.getJobsByUser(userHandle);
            for (int i = 0; i < jobsForUser.size(); i++) {
                cancelJobImplLocked(jobsForUser.get(i), null, "user removed");
            }
        }
    }

    private void cancelJobsForNonExistentUsers() {
        UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        synchronized (this.mLock) {
            this.mJobs.removeJobsOfNonUsers(umi.getUserIds());
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelJobsForPackageAndUid(String pkgName, int uid, String reason) {
        if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(pkgName)) {
            Slog.wtfStack(TAG, "Can't cancel all jobs for system package");
            return;
        }
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = jobsForUid.size() - 1; i >= 0; i--) {
                JobStatus job = jobsForUid.get(i);
                if (job.getSourcePackageName().equals(pkgName)) {
                    cancelJobImplLocked(job, null, reason);
                }
            }
        }
    }

    public boolean cancelJobsForUid(int uid, String reason) {
        if (uid == 1000) {
            Slog.wtfStack(TAG, "Can't cancel all jobs for system uid");
            return false;
        }
        boolean jobsCanceled = false;
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = 0; i < jobsForUid.size(); i++) {
                cancelJobImplLocked(jobsForUid.get(i), null, reason);
                jobsCanceled = true;
            }
        }
        return jobsCanceled;
    }

    public boolean cancelJob(int uid, int jobId, int callingUid) {
        boolean z;
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uid, jobId);
            if (toCancel != null) {
                cancelJobImplLocked(toCancel, null, "cancel() called by app, callingUid=" + callingUid + " uid=" + uid + " jobId=" + jobId);
            }
            z = toCancel != null;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void cancelJobImplLocked(JobStatus cancelled, JobStatus incomingJob, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "CANCEL: " + cancelled.toShortString());
        }
        cancelled.unprepareLocked(ActivityManager.getService());
        stopTrackingJobLocked(cancelled, incomingJob, true);
        if (this.mPendingJobs.remove(cancelled)) {
            this.mJobPackageTracker.noteNonpending(cancelled);
        }
        stopJobOnServiceContextLocked(cancelled, 0, reason);
        reportActiveLocked();
    }

    /* access modifiers changed from: package-private */
    public void updateUidState(int uid, int procState) {
        synchronized (this.mLock) {
            if (procState == 2) {
                try {
                    this.mUidPriorityOverride.put(uid, 40);
                } catch (Throwable th) {
                    throw th;
                }
            } else if (procState <= 4) {
                this.mUidPriorityOverride.put(uid, 30);
            } else {
                this.mUidPriorityOverride.delete(uid);
            }
        }
    }

    public void onDeviceIdleStateChanged(boolean deviceIdle) {
        synchronized (this.mLock) {
            if (deviceIdle) {
                for (int i = 0; i < this.mActiveServices.size(); i++) {
                    JobServiceContext jsc = this.mActiveServices.get(i);
                    JobStatus executing = jsc.getRunningJobLocked();
                    if (executing != null && (executing.getFlags() & 1) == 0) {
                        jsc.cancelExecutingJobLocked(4, "cancelled due to doze");
                    }
                }
            } else if (this.mReadyToRock != 0) {
                if (this.mLocalDeviceIdleController != null && !this.mReportedActive) {
                    this.mReportedActive = true;
                    this.mLocalDeviceIdleController.setJobsActive(true);
                }
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportActiveLocked() {
        int i = 0;
        boolean active = this.mPendingJobs.size() > 0;
        if (this.mPendingJobs.size() <= 0) {
            while (true) {
                if (i >= this.mActiveServices.size()) {
                    break;
                }
                JobStatus job = this.mActiveServices.get(i).getRunningJobLocked();
                if (job != null && (job.getJob().getFlags() & 1) == 0 && !job.dozeWhitelisted && !job.uidActive) {
                    active = true;
                    break;
                }
                i++;
            }
        }
        if (this.mReportedActive != active) {
            this.mReportedActive = active;
            if (this.mLocalDeviceIdleController != null) {
                this.mLocalDeviceIdleController.setJobsActive(active);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportAppUsage(String packageName, int userId) {
    }

    public JobSchedulerService(Context context) {
        super(context);
        this.mHandler = new JobHandler(context.getMainLooper());
        this.mConstants = new Constants();
        this.mConstantsObserver = new ConstantsObserver(this.mHandler);
        this.mJobSchedulerStub = new JobSchedulerStub();
        this.mStandbyTracker = new StandbyTracker();
        this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mUsageStats.addAppIdleStateChangeListener(this.mStandbyTracker);
        publishLocalService(JobSchedulerInternal.class, new LocalService());
        this.mJobs = JobStore.initAndGet(this);
        this.mControllers = new ArrayList();
        this.mControllers.add(new ConnectivityController(this));
        this.mHwTimeController = new HwTimeController(this);
        this.mControllers.add(this.mHwTimeController);
        this.mControllers.add(new IdleController(this));
        this.mBatteryController = new BatteryController(this);
        this.mControllers.add(this.mBatteryController);
        this.mStorageController = new StorageController(this);
        this.mControllers.add(this.mStorageController);
        this.mControllers.add(new BackgroundJobsController(this));
        this.mControllers.add(new ContentObserverController(this));
        this.mDeviceIdleJobsController = new DeviceIdleJobsController(this);
        this.mControllers.add(this.mDeviceIdleJobsController);
        if (!this.mJobs.jobTimesInflatedValid()) {
            Slog.w(TAG, "!!! RTC not yet good; tracking time updates for job scheduling");
            context.registerReceiver(this.mTimeSetReceiver, new IntentFilter("android.intent.action.TIME_SET"));
        }
    }

    public static /* synthetic */ void lambda$new$1(JobSchedulerService jobSchedulerService) {
        ArrayList<JobStatus> toRemove = new ArrayList<>();
        ArrayList<JobStatus> toAdd = new ArrayList<>();
        synchronized (jobSchedulerService.mLock) {
            jobSchedulerService.getJobStore().getRtcCorrectedJobsLocked(toAdd, toRemove);
            int N = toAdd.size();
            for (int i = 0; i < N; i++) {
                JobStatus oldJob = toRemove.get(i);
                JobStatus newJob = toAdd.get(i);
                if (DEBUG) {
                    Slog.v(TAG, "  replacing " + oldJob + " with " + newJob);
                }
                jobSchedulerService.cancelJobImplLocked(oldJob, newJob, "deferred rtc calculation");
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.job.JobSchedulerService$JobSchedulerStub, android.os.IBinder] */
    public void onStart() {
        publishBinderService("jobscheduler", this.mJobSchedulerStub);
    }

    public void onBootPhase(int phase) {
        if (500 == phase) {
            this.mConstantsObserver.start(getContext().getContentResolver());
            this.mAppStateTracker = (AppStateTracker) Preconditions.checkNotNull((AppStateTracker) LocalServices.getService(AppStateTracker.class));
            setNextHeartbeatAlarm();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme("package");
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_REMOVED"), null, null);
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 15, -1, null);
            } catch (RemoteException e) {
            }
            cancelJobsForNonExistentUsers();
        } else if (phase == 600) {
            synchronized (this.mLock) {
                this.mReadyToRock = true;
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mLocalDeviceIdleController = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
                for (int i = 0; i < 16; i++) {
                    this.mActiveServices.add(new JobServiceContext(this, this.mBatteryStats, this.mJobPackageTracker, getContext().getMainLooper()));
                }
                this.mJobs.forEachJob(new Consumer() {
                    public final void accept(Object obj) {
                        JobSchedulerService.lambda$onBootPhase$2(JobSchedulerService.this, (JobStatus) obj);
                    }
                });
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    public static /* synthetic */ void lambda$onBootPhase$2(JobSchedulerService jobSchedulerService, JobStatus job) {
        for (int controller = 0; controller < jobSchedulerService.mControllers.size(); controller++) {
            jobSchedulerService.mControllers.get(controller).maybeStartTrackingJobLocked(job, null);
        }
    }

    private void startTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (!jobStatus.isPreparedLocked()) {
            Slog.wtf(TAG, "Not yet prepared when started tracking: " + jobStatus);
        }
        jobStatus.enqueueTime = sElapsedRealtimeClock.millis();
        boolean update = this.mJobs.add(jobStatus);
        if (this.mReadyToRock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                StateController controller = this.mControllers.get(i);
                if (update) {
                    controller.maybeStopTrackingJobLocked(jobStatus, null, true);
                }
                controller.maybeStartTrackingJobLocked(jobStatus, lastJob);
            }
        }
    }

    private boolean stopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean writeBack) {
        jobStatus.stopTrackingJobLocked(ActivityManager.getService(), incomingJob);
        boolean removed = this.mJobs.remove(jobStatus, writeBack);
        if (removed && this.mReadyToRock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                this.mControllers.get(i).maybeStopTrackingJobLocked(jobStatus, incomingJob, false);
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason, String debugReason) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJobLocked();
            if (executing == null || !executing.matches(job.getUid(), job.getJobId())) {
                i++;
            } else {
                jsc.cancelExecutingJobLocked(reason, debugReason);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i = 0; i < this.mActiveServices.size(); i++) {
            JobStatus running = this.mActiveServices.get(i).getRunningJobLocked();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void noteJobsPending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.notePending(jobs.get(i));
        }
    }

    /* access modifiers changed from: package-private */
    public void noteJobsNonpending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.noteNonpending(jobs.get(i));
        }
    }

    private JobStatus getRescheduleJobForFailureLocked(JobStatus failureToReschedule) {
        long backoff;
        JobStatus jobStatus = failureToReschedule;
        long elapsedNowMillis = sElapsedRealtimeClock.millis();
        JobInfo job = failureToReschedule.getJob();
        long initialBackoffMillis = job.getInitialBackoffMillis();
        int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        if (failureToReschedule.hasWorkLocked()) {
            if (backoffAttempts > this.mConstants.MAX_WORK_RESCHEDULE_COUNT) {
                Slog.w(TAG, "Not rescheduling " + jobStatus + ": attempt #" + backoffAttempts + " > work limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
                return null;
            }
        } else if (backoffAttempts > this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT) {
            Slog.w(TAG, "Not rescheduling " + jobStatus + ": attempt #" + backoffAttempts + " > std limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
            return null;
        }
        switch (job.getBackoffPolicy()) {
            case 0:
                long backoff2 = initialBackoffMillis;
                if (backoff2 < this.mConstants.MIN_LINEAR_BACKOFF_TIME) {
                    backoff2 = this.mConstants.MIN_LINEAR_BACKOFF_TIME;
                }
                backoff = backoff2 * ((long) backoffAttempts);
                break;
            case 1:
                break;
            default:
                if (DEBUG) {
                    Slog.v(TAG, "Unrecognised back-off policy, defaulting to exponential.");
                    break;
                }
                break;
        }
        long backoff3 = initialBackoffMillis;
        if (backoff3 < this.mConstants.MIN_EXP_BACKOFF_TIME) {
            backoff3 = this.mConstants.MIN_EXP_BACKOFF_TIME;
        }
        backoff = (long) Math.scalb((float) backoff3, backoffAttempts - 1);
        JobStatus jobStatus2 = jobStatus;
        int i = backoffAttempts;
        JobInfo jobInfo = job;
        int i2 = backoffAttempts;
        JobStatus newJob = new JobStatus(jobStatus2, getCurrentHeartbeat(), elapsedNowMillis + Math.min(backoff, 18000000), JobStatus.NO_LATEST_RUNTIME, i, failureToReschedule.getLastSuccessfulRunTime(), sSystemClock.millis());
        for (int ic = 0; ic < this.mControllers.size(); ic++) {
            this.mControllers.get(ic).rescheduleForFailureLocked(newJob, jobStatus);
        }
        return newJob;
    }

    private JobStatus getRescheduleJobForPeriodic(JobStatus periodicToReschedule) {
        long elapsedNow = sElapsedRealtimeClock.millis();
        long runEarly = 0;
        if (periodicToReschedule.hasDeadlineConstraint()) {
            runEarly = Math.max(periodicToReschedule.getLatestRunTimeElapsed() - elapsedNow, 0);
        }
        long flex = periodicToReschedule.getJob().getFlexMillis();
        long newLatestRuntimeElapsed = elapsedNow + runEarly + periodicToReschedule.getJob().getIntervalMillis();
        long newEarliestRunTimeElapsed = newLatestRuntimeElapsed - flex;
        if (DEBUG) {
            Slog.v(TAG, "Rescheduling executed periodic. New execution window [" + (newEarliestRunTimeElapsed / 1000) + ", " + (newLatestRuntimeElapsed / 1000) + "]s");
        }
        JobStatus jobStatus = new JobStatus(periodicToReschedule, getCurrentHeartbeat(), newEarliestRunTimeElapsed, newLatestRuntimeElapsed, 0, sSystemClock.millis(), periodicToReschedule.getLastFailedRunTime());
        return jobStatus;
    }

    /* access modifiers changed from: package-private */
    public long heartbeatWhenJobsLastRun(String packageName, int userId) {
        long heartbeat = (long) (-this.mConstants.STANDBY_BEATS[3]);
        boolean cacheHit = false;
        synchronized (this.mLock) {
            HashMap<String, Long> jobPackages = this.mLastJobHeartbeats.get(userId);
            if (jobPackages != null) {
                long cachedValue = jobPackages.getOrDefault(packageName, Long.valueOf(JobStatus.NO_LATEST_RUNTIME)).longValue();
                if (cachedValue < JobStatus.NO_LATEST_RUNTIME) {
                    cacheHit = true;
                    heartbeat = cachedValue;
                }
            }
            if (!cacheHit) {
                long timeSinceJob = this.mUsageStats.getTimeSinceLastJobRun(packageName, userId);
                if (timeSinceJob < JobStatus.NO_LATEST_RUNTIME) {
                    heartbeat = this.mHeartbeat - (timeSinceJob / this.mConstants.STANDBY_HEARTBEAT_TIME);
                }
                setLastJobHeartbeatLocked(packageName, userId, heartbeat);
            }
        }
        if (DEBUG_STANDBY) {
            Slog.v(TAG, "Last job heartbeat " + heartbeat + " for " + packageName + SliceClientPermissions.SliceAuthority.DELIMITER + userId);
        }
        return heartbeat;
    }

    /* access modifiers changed from: package-private */
    public long heartbeatWhenJobsLastRun(JobStatus job) {
        return heartbeatWhenJobsLastRun(job.getSourcePackageName(), job.getSourceUserId());
    }

    /* access modifiers changed from: package-private */
    public void setLastJobHeartbeatLocked(String packageName, int userId, long heartbeat) {
        HashMap<String, Long> jobPackages = this.mLastJobHeartbeats.get(userId);
        if (jobPackages == null) {
            jobPackages = new HashMap<>();
            this.mLastJobHeartbeats.put(userId, jobPackages);
        }
        jobPackages.put(packageName, Long.valueOf(heartbeat));
    }

    public void onJobCompletedLocked(JobStatus jobStatus, boolean needsReschedule) {
        if (DEBUG) {
            Slog.d(TAG, "Completed " + jobStatus + ", reschedule=" + needsReschedule);
        }
        JobStatus rescheduledJob = needsReschedule ? getRescheduleJobForFailureLocked(jobStatus) : null;
        if (!stopTrackingJobLocked(jobStatus, rescheduledJob, !jobStatus.getJob().isPeriodic())) {
            if (DEBUG) {
                Slog.d(TAG, "Could not find job to remove. Was job removed while executing?");
            }
            this.mHandler.obtainMessage(3).sendToTarget();
            return;
        }
        if (rescheduledJob != null) {
            try {
                rescheduledJob.prepareLocked(ActivityManager.getService());
            } catch (SecurityException e) {
                Slog.w(TAG, "Unable to regrant job permissions for " + rescheduledJob);
            }
            startTrackingJobLocked(rescheduledJob, jobStatus);
        } else if (jobStatus.getJob().isPeriodic()) {
            JobStatus rescheduledPeriodic = getRescheduleJobForPeriodic(jobStatus);
            try {
                rescheduledPeriodic.prepareLocked(ActivityManager.getService());
            } catch (SecurityException e2) {
                Slog.w(TAG, "Unable to regrant job permissions for " + rescheduledPeriodic);
            }
            startTrackingJobLocked(rescheduledPeriodic, jobStatus);
        }
        jobStatus.unprepareLocked(ActivityManager.getService());
        reportActiveLocked();
        this.mHandler.obtainMessage(3).sendToTarget();
    }

    public void onControllerStateChanged() {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public void onRunJobNow(JobStatus jobStatus) {
        this.mHandler.obtainMessage(0, jobStatus).sendToTarget();
    }

    private void stopNonReadyActiveJobsLocked() {
        for (int i = 0; i < this.mActiveServices.size(); i++) {
            JobServiceContext serviceContext = this.mActiveServices.get(i);
            JobStatus running = serviceContext.getRunningJobLocked();
            if (running != null && !running.isReady()) {
                serviceContext.cancelExecutingJobLocked(1, "cancelled due to unsatisfied constraints");
            }
        }
    }

    /* access modifiers changed from: private */
    public void queueReadyJobsForExecutionLocked() {
        Slog.d(TAG, "queuing all ready jobs for execution:");
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mReadyQueueFunctor);
        this.mReadyQueueFunctor.postProcess();
        if (DEBUG) {
            int queuedJobs = this.mPendingJobs.size();
            if (queuedJobs == 0) {
                Slog.d(TAG, "No jobs pending.");
                return;
            }
            Slog.d(TAG, queuedJobs + " jobs queued.");
        }
    }

    /* access modifiers changed from: private */
    public void maybeQueueReadyJobsForExecutionLocked() {
        Slog.d(TAG, "Maybe queuing ready jobs...");
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mMaybeQueueFunctor);
        this.mMaybeQueueFunctor.postProcess();
    }

    /* access modifiers changed from: package-private */
    public void advanceHeartbeatLocked(long beatsElapsed) {
        this.mHeartbeat += beatsElapsed;
        if (DEBUG_STANDBY) {
            Slog.v(TAG, "Advancing standby heartbeat by " + beatsElapsed + " to " + this.mHeartbeat);
        }
        boolean didAdvanceBucket = false;
        for (int i = 1; i < this.mNextBucketHeartbeat.length - 1; i++) {
            if (this.mHeartbeat >= this.mNextBucketHeartbeat[i]) {
                didAdvanceBucket = true;
            }
            while (this.mHeartbeat > this.mNextBucketHeartbeat[i]) {
                long[] jArr = this.mNextBucketHeartbeat;
                jArr[i] = jArr[i] + ((long) this.mConstants.STANDBY_BEATS[i]);
            }
            if (DEBUG_STANDBY) {
                Slog.v(TAG, "   Bucket " + i + " next heartbeat " + this.mNextBucketHeartbeat[i]);
            }
        }
        if (didAdvanceBucket) {
            if (DEBUG_STANDBY) {
                Slog.v(TAG, "Hit bucket boundary; reevaluating job runnability");
            }
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    /* access modifiers changed from: package-private */
    public void setNextHeartbeatAlarm() {
        long heartbeatLength;
        synchronized (this.mLock) {
            heartbeatLength = this.mConstants.STANDBY_HEARTBEAT_TIME;
        }
        long now = sElapsedRealtimeClock.millis();
        long nextHeartbeat = ((now + heartbeatLength) / heartbeatLength) * heartbeatLength;
        if (DEBUG_STANDBY) {
            Slog.i(TAG, "Setting heartbeat alarm for " + nextHeartbeat + " = " + TimeUtils.formatDuration(nextHeartbeat - now));
        }
        long j = nextHeartbeat;
        ((AlarmManager) getContext().getSystemService("alarm")).setExact(3, nextHeartbeat, HEARTBEAT_TAG, this.mHeartbeatAlarm, this.mHandler);
    }

    /* access modifiers changed from: private */
    public boolean isReadyToBeExecutedLocked(JobStatus job) {
        boolean jobReady = job.isReady();
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " ready=" + jobReady);
        }
        boolean componentPresent = false;
        if (!jobReady) {
            if (job.getSourcePackageName().equals("android.jobscheduler.cts.jobtestapp")) {
                Slog.v(TAG, "    NOT READY: " + job);
            }
            return false;
        }
        boolean jobExists = this.mJobs.containsJob(job);
        int userId = job.getUserId();
        boolean userStarted = ArrayUtils.contains(this.mStartedUsers, userId);
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " exists=" + jobExists + " userStarted=" + userStarted);
        }
        if (!jobExists || !userStarted) {
            return false;
        }
        boolean jobPending = this.mPendingJobs.contains(job);
        boolean jobActive = isCurrentlyActiveLocked(job);
        if (DEBUG) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " pending=" + jobPending + " active=" + jobActive);
        }
        if (jobPending || jobActive) {
            return false;
        }
        if (DEBUG_STANDBY) {
            Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " parole=" + this.mInParole + " active=" + job.uidActive + " exempt=" + job.getJob().isExemptedFromAppStandby());
        }
        if (!this.mInParole && !job.uidActive && !job.getJob().isExemptedFromAppStandby()) {
            int bucket = job.getStandbyBucket();
            if (DEBUG_STANDBY) {
                Slog.v(TAG, "  bucket=" + bucket + " heartbeat=" + this.mHeartbeat + " next=" + this.mNextBucketHeartbeat[bucket]);
            }
            if (this.mHeartbeat < this.mNextBucketHeartbeat[bucket]) {
                long appLastRan = heartbeatWhenJobsLastRun(job);
                if (bucket >= this.mConstants.STANDBY_BEATS.length || (this.mHeartbeat > appLastRan && this.mHeartbeat < ((long) this.mConstants.STANDBY_BEATS[bucket]) + appLastRan)) {
                    if (job.getWhenStandbyDeferred() == 0) {
                        if (DEBUG_STANDBY) {
                            Slog.v(TAG, "Bucket deferral: " + this.mHeartbeat + " < " + (((long) this.mConstants.STANDBY_BEATS[bucket]) + appLastRan) + " for " + job);
                        }
                        job.setWhenStandbyDeferred(sElapsedRealtimeClock.millis());
                    }
                    return false;
                } else if (DEBUG_STANDBY) {
                    Slog.v(TAG, "Bucket deferred job aged into runnability at " + this.mHeartbeat + " : " + job);
                }
            }
        }
        try {
            if (AppGlobals.getPackageManager().getServiceInfo(job.getServiceComponent(), 268435456, userId) != null) {
                componentPresent = true;
            }
            if (DEBUG) {
                Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString() + " componentPresent=" + componentPresent);
            }
            return componentPresent;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    /* access modifiers changed from: private */
    public void maybeRunPendingJobsLocked() {
        if (DEBUG) {
            Slog.d(TAG, "pending queue: " + this.mPendingJobs.size() + " jobs.");
        }
        assignJobsToContextsLocked();
        reportActiveLocked();
    }

    private int adjustJobPriority(int curPriority, JobStatus job) {
        if (curPriority >= 40) {
            return curPriority;
        }
        float factor = this.mJobPackageTracker.getLoadFactor(job);
        if (factor >= this.mConstants.HEAVY_USE_FACTOR) {
            return curPriority - 80;
        }
        if (factor >= this.mConstants.MODERATE_USE_FACTOR) {
            return curPriority - 40;
        }
        return curPriority;
    }

    private int evaluateJobPriorityLocked(JobStatus job) {
        int priority = job.getPriority();
        if (priority >= 30) {
            return adjustJobPriority(priority, job);
        }
        int override = this.mUidPriorityOverride.get(job.getSourceUid(), 0);
        if (override != 0) {
            return adjustJobPriority(override, job);
        }
        return adjustJobPriority(priority, job);
    }

    private void assignJobsToContextsLocked() {
        int memLevel;
        int memLevel2;
        int memLevel3;
        int memLevel4;
        if (DEBUG) {
            Slog.d(TAG, printPendingQueue());
        }
        int i = 0;
        try {
            memLevel = ActivityManager.getService().getMemoryTrimLevel();
        } catch (RemoteException e) {
            memLevel = 0;
        }
        switch (memLevel) {
            case 1:
                this.mMaxActiveJobs = this.mConstants.BG_MODERATE_JOB_COUNT;
                break;
            case 2:
                this.mMaxActiveJobs = this.mConstants.BG_LOW_JOB_COUNT;
                break;
            case 3:
                this.mMaxActiveJobs = this.mConstants.BG_CRITICAL_JOB_COUNT;
                break;
            default:
                this.mMaxActiveJobs = this.mConstants.BG_NORMAL_JOB_COUNT;
                break;
        }
        JobStatus[] contextIdToJobMap = this.mTmpAssignContextIdToJobMap;
        boolean[] act = this.mTmpAssignAct;
        int[] preferredUidForContext = this.mTmpAssignPreferredUidForContext;
        int numForeground = 0;
        int numActive = 0;
        int i2 = 0;
        while (true) {
            int i3 = 16;
            if (i2 < 16) {
                JobServiceContext js = this.mActiveServices.get(i2);
                JobStatus status = js.getRunningJobLocked();
                contextIdToJobMap[i2] = status;
                if (status != null) {
                    numActive++;
                    if (status.lastEvaluatedPriority >= 40) {
                        numForeground++;
                    }
                }
                act[i2] = false;
                preferredUidForContext[i2] = js.getPreferredUid();
                i2++;
            } else {
                if (DEBUG != 0) {
                    Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs initial"));
                }
                int i4 = 0;
                while (i4 < this.mPendingJobs.size()) {
                    JobStatus nextPending = this.mPendingJobs.get(i4);
                    if (findJobContextIdFromMap(nextPending, contextIdToJobMap) != -1) {
                        memLevel2 = memLevel;
                    } else {
                        int priority = evaluateJobPriorityLocked(nextPending);
                        nextPending.lastEvaluatedPriority = priority;
                        int minPriorityContextId = -1;
                        int j = i;
                        int minPriorityContextId2 = Integer.MAX_VALUE;
                        while (true) {
                            if (j < i3) {
                                JobStatus job = contextIdToJobMap[j];
                                int preferredUid = preferredUidForContext[j];
                                if (job != null) {
                                    memLevel4 = memLevel;
                                    if (job.getUid() == nextPending.getUid() && evaluateJobPriorityLocked(job) < nextPending.lastEvaluatedPriority && minPriorityContextId2 > nextPending.lastEvaluatedPriority) {
                                        minPriorityContextId = j;
                                        minPriorityContextId2 = nextPending.lastEvaluatedPriority;
                                    }
                                } else if ((numActive < this.mMaxActiveJobs || (priority >= 40 && numForeground < this.mConstants.FG_JOB_COUNT)) && (preferredUid == nextPending.getUid() || preferredUid == -1)) {
                                    memLevel2 = memLevel;
                                    memLevel3 = j;
                                } else {
                                    memLevel4 = memLevel;
                                }
                                j++;
                                memLevel = memLevel4;
                                i3 = 16;
                            } else {
                                memLevel2 = memLevel;
                                memLevel3 = minPriorityContextId;
                            }
                        }
                        if (memLevel3 != -1) {
                            contextIdToJobMap[memLevel3] = nextPending;
                            act[memLevel3] = true;
                            numActive++;
                            if (priority >= 40) {
                                numForeground++;
                            }
                        }
                    }
                    i4++;
                    memLevel = memLevel2;
                    i = 0;
                    i3 = 16;
                }
                if (DEBUG != 0) {
                    Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs final"));
                }
                this.mJobPackageTracker.noteConcurrency(numActive, numForeground);
                for (int i5 = 0; i5 < 16; i5++) {
                    boolean preservePreferredUid = false;
                    if (act[i5]) {
                        if (this.mActiveServices.get(i5).getRunningJobLocked() != null) {
                            if (DEBUG) {
                                Slog.d(TAG, "preempting job: " + this.mActiveServices.get(i5).getRunningJobLocked());
                            }
                            this.mActiveServices.get(i5).preemptExecutingJobLocked();
                            preservePreferredUid = true;
                        } else {
                            JobStatus pendingJob = contextIdToJobMap[i5];
                            if (DEBUG) {
                                Slog.d(TAG, "About to run job on context " + String.valueOf(i5) + ", job: " + pendingJob);
                            }
                            for (int ic = 0; ic < this.mControllers.size(); ic++) {
                                this.mControllers.get(ic).prepareForExecutionLocked(pendingJob);
                            }
                            if (!this.mActiveServices.get(i5).executeRunnableJob(pendingJob)) {
                                Slog.d(TAG, "Error executing " + pendingJob);
                            }
                            if (this.mPendingJobs.remove(pendingJob)) {
                                this.mJobPackageTracker.noteNonpending(pendingJob);
                            }
                        }
                    }
                    if (!preservePreferredUid) {
                        this.mActiveServices.get(i5).clearPreferredUid();
                    }
                }
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int findJobContextIdFromMap(JobStatus jobStatus, JobStatus[] map) {
        for (int i = 0; i < map.length; i++) {
            if (map[i] != null && map[i].matches(jobStatus.getUid(), jobStatus.getJobId())) {
                return i;
            }
        }
        return -1;
    }

    public static int standbyBucketToBucketIndex(int bucket) {
        if (bucket == 50) {
            return 4;
        }
        if (bucket > 30) {
            return 3;
        }
        if (bucket > 20) {
            return 2;
        }
        if (bucket > 10) {
            return 1;
        }
        return 0;
    }

    public static int standbyBucketForPackage(String packageName, int userId, long elapsedNow) {
        int bucket;
        UsageStatsManagerInternal usageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        if (usageStats != null) {
            bucket = usageStats.getAppStandbyBucket(packageName, userId, elapsedNow);
        } else {
            bucket = 0;
        }
        int bucket2 = standbyBucketToBucketIndex(bucket);
        if (DEBUG_STANDBY) {
            Slog.v(TAG, packageName + SliceClientPermissions.SliceAuthority.DELIMITER + userId + " standby bucket index: " + bucket2);
        }
        return bucket2;
    }

    /* access modifiers changed from: package-private */
    public int executeRunCommand(String pkgName, int userId, int jobId, boolean force) {
        if (DEBUG) {
            Slog.v(TAG, "executeRunCommand(): " + pkgName + SliceClientPermissions.SliceAuthority.DELIMITER + userId + " " + jobId + " f=" + force);
        }
        try {
            int uid = AppGlobals.getPackageManager().getPackageUid(pkgName, 0, userId != -1 ? userId : 0);
            if (uid < 0) {
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (js == null) {
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                js.overrideState = force ? 2 : 1;
                if (!js.isConstraintsSatisfied()) {
                    js.overrideState = 0;
                    return JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS;
                }
                queueReadyJobsForExecutionLocked();
                maybeRunPendingJobsLocked();
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int executeTimeoutCommand(PrintWriter pw, String pkgName, int userId, boolean hasJobId, int jobId) {
        int i;
        int i2;
        String str;
        PrintWriter printWriter = pw;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("executeTimeoutCommand(): ");
            str = pkgName;
            sb.append(str);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            i2 = userId;
            sb.append(i2);
            sb.append(" ");
            i = jobId;
            sb.append(i);
            Slog.v(TAG, sb.toString());
        } else {
            str = pkgName;
            i2 = userId;
            i = jobId;
        }
        synchronized (this.mLock) {
            boolean foundSome = false;
            for (int i3 = 0; i3 < this.mActiveServices.size(); i3++) {
                JobServiceContext jc = this.mActiveServices.get(i3);
                JobStatus js = jc.getRunningJobLocked();
                if (jc.timeoutIfExecutingLocked(str, i2, hasJobId, i, "shell")) {
                    printWriter.print("Timing out: ");
                    js.printUniqueId(printWriter);
                    printWriter.print(" ");
                    printWriter.println(js.getServiceComponent().flattenToShortString());
                    foundSome = true;
                }
            }
            if (!foundSome) {
                printWriter.println("No matching executing jobs found.");
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int executeCancelCommand(PrintWriter pw, String pkgName, int userId, boolean hasJobId, int jobId) {
        if (DEBUG) {
            Slog.v(TAG, "executeCancelCommand(): " + pkgName + SliceClientPermissions.SliceAuthority.DELIMITER + userId + " " + jobId);
        }
        int pkgUid = -1;
        try {
            pkgUid = AppGlobals.getPackageManager().getPackageUid(pkgName, 0, userId);
        } catch (RemoteException e) {
        }
        if (pkgUid < 0) {
            pw.println("Package " + pkgName + " not found.");
            return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
        if (!hasJobId) {
            pw.println("Canceling all jobs for " + pkgName + " in user " + userId);
            if (!cancelJobsForUid(pkgUid, "cancel shell command for package")) {
                pw.println("No matching jobs found.");
            }
        } else {
            pw.println("Canceling job " + pkgName + "/#" + jobId + " in user " + userId);
            if (!cancelJob(pkgUid, jobId, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME)) {
                pw.println("No matching job found.");
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void setMonitorBattery(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mBatteryController != null) {
                this.mBatteryController.getTracker().setMonitorBatteryLocked(enabled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getBatterySeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mBatteryController != null ? this.mBatteryController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    /* access modifiers changed from: package-private */
    public boolean getBatteryCharging() {
        boolean isOnStablePower;
        synchronized (this.mLock) {
            isOnStablePower = this.mBatteryController != null ? this.mBatteryController.getTracker().isOnStablePower() : false;
        }
        return isOnStablePower;
    }

    /* access modifiers changed from: package-private */
    public boolean getBatteryNotLow() {
        boolean isBatteryNotLow;
        synchronized (this.mLock) {
            isBatteryNotLow = this.mBatteryController != null ? this.mBatteryController.getTracker().isBatteryNotLow() : false;
        }
        return isBatteryNotLow;
    }

    /* access modifiers changed from: package-private */
    public int getStorageSeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mStorageController != null ? this.mStorageController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    /* access modifiers changed from: package-private */
    public boolean getStorageNotLow() {
        boolean isStorageNotLow;
        synchronized (this.mLock) {
            isStorageNotLow = this.mStorageController != null ? this.mStorageController.getTracker().isStorageNotLow() : false;
        }
        return isStorageNotLow;
    }

    /* access modifiers changed from: package-private */
    public long getCurrentHeartbeat() {
        long j;
        synchronized (this.mLock) {
            j = this.mHeartbeat;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public int getJobState(PrintWriter pw, String pkgName, int userId, int jobId) {
        try {
            int uid = AppGlobals.getPackageManager().getPackageUid(pkgName, 0, userId != -1 ? userId : 0);
            if (uid < 0) {
                pw.print("unknown(");
                pw.print(pkgName);
                pw.println(")");
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (DEBUG) {
                    Slog.d(TAG, "get-job-state " + uid + SliceClientPermissions.SliceAuthority.DELIMITER + jobId + ": " + js);
                }
                if (js == null) {
                    pw.print("unknown(");
                    UserHandle.formatUid(pw, uid);
                    pw.print("/jid");
                    pw.print(jobId);
                    pw.println(")");
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                boolean printed = false;
                if (this.mPendingJobs.contains(js)) {
                    pw.print("pending");
                    printed = true;
                }
                if (isCurrentlyActiveLocked(js)) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("active");
                }
                if (!ArrayUtils.contains(this.mStartedUsers, js.getUserId())) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("user-stopped");
                }
                if (this.mBackingUpUids.indexOfKey(js.getSourceUid()) >= 0) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("backing-up");
                }
                boolean componentPresent = false;
                try {
                    componentPresent = AppGlobals.getPackageManager().getServiceInfo(js.getServiceComponent(), 268435456, js.getUserId()) != null;
                } catch (RemoteException e) {
                }
                if (!componentPresent) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("no-component");
                }
                if (js.isReady()) {
                    if (printed) {
                        pw.print(" ");
                    }
                    printed = true;
                    pw.println("ready");
                }
                if (!printed) {
                    pw.print("waiting");
                }
                pw.println();
            }
        } catch (RemoteException e2) {
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int executeHeartbeatCommand(PrintWriter pw, int numBeats) {
        if (numBeats < 1) {
            pw.println(getCurrentHeartbeat());
            return 0;
        }
        pw.print("Advancing standby heartbeat by ");
        pw.println(numBeats);
        synchronized (this.mLock) {
            advanceHeartbeatLocked((long) numBeats);
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void triggerDockState(boolean idleState) {
        Intent dockIntent;
        if (idleState) {
            dockIntent = new Intent("android.intent.action.DOCK_IDLE");
        } else {
            dockIntent = new Intent("android.intent.action.DOCK_ACTIVE");
        }
        dockIntent.setPackage(PackageManagerService.PLATFORM_PACKAGE_NAME);
        dockIntent.addFlags(1342177280);
        getContext().sendBroadcastAsUser(dockIntent, UserHandle.ALL);
    }

    private String printContextIdToJobMap(JobStatus[] map, String initial) {
        StringBuilder s = new StringBuilder(initial + ": ");
        for (int i = 0; i < map.length; i++) {
            s.append("(");
            int i2 = -1;
            s.append(map[i] == null ? -1 : map[i].getJobId());
            if (map[i] != null) {
                i2 = map[i].getUid();
            }
            s.append(i2);
            s.append(")");
        }
        return s.toString();
    }

    private String printPendingQueue() {
        StringBuilder s = new StringBuilder("Pending queue: ");
        Iterator<JobStatus> it = this.mPendingJobs.iterator();
        while (it.hasNext()) {
            JobStatus js = it.next();
            s.append("(");
            s.append(js.getJob().getId());
            s.append(", ");
            s.append(js.getUid());
            s.append(") ");
        }
        return s.toString();
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Job Scheduler (jobscheduler) dump options:");
        pw.println("  [-h] [package] ...");
        pw.println("    -h: print this help");
        pw.println("  [package] is an optional package name to limit the output to.");
    }

    private static void sortJobs(List<JobStatus> jobs) {
        Collections.sort(jobs, new Comparator<JobStatus>() {
            public int compare(JobStatus o1, JobStatus o2) {
                int uid1 = o1.getUid();
                int uid2 = o2.getUid();
                int id1 = o1.getJobId();
                int id2 = o2.getJobId();
                int i = 1;
                if (uid1 != uid2) {
                    if (uid1 < uid2) {
                        i = -1;
                    }
                    return i;
                }
                if (id1 < id2) {
                    i = -1;
                } else if (id1 <= id2) {
                    i = 0;
                }
                return i;
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void dumpInternal(IndentingPrintWriter pw, int filterUid) {
        long nowUptime;
        IndentingPrintWriter indentingPrintWriter = pw;
        int filterUidFinal = UserHandle.getAppId(filterUid);
        long nowElapsed = sElapsedRealtimeClock.millis();
        long nowUptime2 = sUptimeMillisClock.millis();
        Predicate<JobStatus> predicate = new Predicate(filterUidFinal) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return JobSchedulerService.lambda$dumpInternal$3(this.f$0, (JobStatus) obj);
            }
        };
        synchronized (this.mLock) {
            try {
                this.mConstants.dump(indentingPrintWriter);
                pw.println();
                indentingPrintWriter.println("  Heartbeat:");
                indentingPrintWriter.print("    Current:    ");
                indentingPrintWriter.println(this.mHeartbeat);
                indentingPrintWriter.println("    Next");
                indentingPrintWriter.print("      ACTIVE:   ");
                int i = 0;
                indentingPrintWriter.println(this.mNextBucketHeartbeat[0]);
                indentingPrintWriter.print("      WORKING:  ");
                indentingPrintWriter.println(this.mNextBucketHeartbeat[1]);
                indentingPrintWriter.print("      FREQUENT: ");
                indentingPrintWriter.println(this.mNextBucketHeartbeat[2]);
                indentingPrintWriter.print("      RARE:     ");
                indentingPrintWriter.println(this.mNextBucketHeartbeat[3]);
                indentingPrintWriter.print("    Last heartbeat: ");
                TimeUtils.formatDuration(this.mLastHeartbeatTime, nowElapsed, indentingPrintWriter);
                pw.println();
                indentingPrintWriter.print("    Next heartbeat: ");
                TimeUtils.formatDuration(this.mLastHeartbeatTime + this.mConstants.STANDBY_HEARTBEAT_TIME, nowElapsed, indentingPrintWriter);
                pw.println();
                indentingPrintWriter.print("    In parole?: ");
                indentingPrintWriter.print(this.mInParole);
                pw.println();
                pw.println();
                indentingPrintWriter.println("Started users: " + Arrays.toString(this.mStartedUsers));
                indentingPrintWriter.print("Registered ");
                indentingPrintWriter.print(this.mJobs.size());
                indentingPrintWriter.println(" jobs:");
                if (this.mJobs.size() > 0) {
                    try {
                        List<JobStatus> jobs = this.mJobs.mJobSet.getAllJobs();
                        sortJobs(jobs);
                        Iterator<JobStatus> it = jobs.iterator();
                        while (it.hasNext()) {
                            JobStatus job = it.next();
                            indentingPrintWriter.print("  JOB #");
                            job.printUniqueId(indentingPrintWriter);
                            indentingPrintWriter.print(": ");
                            indentingPrintWriter.println(job.toShortStringExceptUniqueId());
                            if (predicate.test(job)) {
                                long nowUptime3 = nowUptime2;
                                JobStatus job2 = job;
                                List<JobStatus> jobs2 = jobs;
                                Iterator<JobStatus> it2 = it;
                                job.dump((PrintWriter) indentingPrintWriter, "    ", true, nowElapsed);
                                indentingPrintWriter.print("    Last run heartbeat: ");
                                indentingPrintWriter.print(heartbeatWhenJobsLastRun(job2));
                                pw.println();
                                indentingPrintWriter.print("    Ready: ");
                                indentingPrintWriter.print(isReadyToBeExecutedLocked(job2));
                                indentingPrintWriter.print(" (job=");
                                indentingPrintWriter.print(job2.isReady());
                                indentingPrintWriter.print(" user=");
                                indentingPrintWriter.print(ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                                indentingPrintWriter.print(" !pending=");
                                indentingPrintWriter.print(!this.mPendingJobs.contains(job2));
                                indentingPrintWriter.print(" !active=");
                                indentingPrintWriter.print(!isCurrentlyActiveLocked(job2));
                                indentingPrintWriter.print(" !backingup=");
                                indentingPrintWriter.print(this.mBackingUpUids.indexOfKey(job2.getSourceUid()) < 0);
                                indentingPrintWriter.print(" comp=");
                                boolean componentPresent = false;
                                try {
                                    componentPresent = AppGlobals.getPackageManager().getServiceInfo(job2.getServiceComponent(), 268435456, job2.getUserId()) != null;
                                } catch (RemoteException e) {
                                }
                                indentingPrintWriter.print(componentPresent);
                                indentingPrintWriter.println(")");
                                jobs = jobs2;
                                nowUptime2 = nowUptime3;
                                it = it2;
                            }
                        }
                        nowUptime = nowUptime2;
                    } catch (Throwable th) {
                        th = th;
                        int i2 = filterUid;
                        throw th;
                    }
                } else {
                    nowUptime = nowUptime2;
                    indentingPrintWriter.println("  None.");
                }
                for (int i3 = 0; i3 < this.mControllers.size(); i3++) {
                    pw.println();
                    indentingPrintWriter.println(this.mControllers.get(i3).getClass().getSimpleName() + ":");
                    pw.increaseIndent();
                    this.mControllers.get(i3).dumpControllerStateLocked(indentingPrintWriter, predicate);
                    pw.decreaseIndent();
                }
                pw.println();
                indentingPrintWriter.println("Uid priority overrides:");
                for (int i4 = 0; i4 < this.mUidPriorityOverride.size(); i4++) {
                    int uid = this.mUidPriorityOverride.keyAt(i4);
                    if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                        indentingPrintWriter.print("  ");
                        indentingPrintWriter.print(UserHandle.formatUid(uid));
                        indentingPrintWriter.print(": ");
                        indentingPrintWriter.println(this.mUidPriorityOverride.valueAt(i4));
                    }
                }
                if (this.mBackingUpUids.size() > 0) {
                    pw.println();
                    indentingPrintWriter.println("Backing up uids:");
                    boolean first = true;
                    for (int i5 = 0; i5 < this.mBackingUpUids.size(); i5++) {
                        int uid2 = this.mBackingUpUids.keyAt(i5);
                        if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid2)) {
                            if (first) {
                                indentingPrintWriter.print("  ");
                                first = false;
                            } else {
                                indentingPrintWriter.print(", ");
                            }
                            indentingPrintWriter.print(UserHandle.formatUid(uid2));
                        }
                    }
                    pw.println();
                }
                pw.println();
                this.mJobPackageTracker.dump((PrintWriter) indentingPrintWriter, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, filterUidFinal);
                pw.println();
                if (this.mJobPackageTracker.dumpHistory((PrintWriter) indentingPrintWriter, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, filterUidFinal)) {
                    pw.println();
                }
                indentingPrintWriter.println("Pending queue:");
                for (int i6 = 0; i6 < this.mPendingJobs.size(); i6++) {
                    JobStatus job3 = this.mPendingJobs.get(i6);
                    indentingPrintWriter.print("  Pending #");
                    indentingPrintWriter.print(i6);
                    indentingPrintWriter.print(": ");
                    indentingPrintWriter.println(job3.toShortString());
                    job3.dump((PrintWriter) indentingPrintWriter, "    ", false, nowElapsed);
                    int priority = evaluateJobPriorityLocked(job3);
                    if (priority != 0) {
                        indentingPrintWriter.print("    Evaluated priority: ");
                        indentingPrintWriter.println(priority);
                    }
                    indentingPrintWriter.print("    Tag: ");
                    indentingPrintWriter.println(job3.getTag());
                    indentingPrintWriter.print("    Enq: ");
                    TimeUtils.formatDuration(job3.madePending - nowUptime, indentingPrintWriter);
                    pw.println();
                }
                pw.println();
                indentingPrintWriter.println("Active jobs:");
                while (true) {
                    int i7 = i;
                    if (i7 >= this.mActiveServices.size()) {
                        break;
                    }
                    JobServiceContext jsc = this.mActiveServices.get(i7);
                    indentingPrintWriter.print("  Slot #");
                    indentingPrintWriter.print(i7);
                    indentingPrintWriter.print(": ");
                    JobStatus job4 = jsc.getRunningJobLocked();
                    if (job4 != null) {
                        indentingPrintWriter.println(job4.toShortString());
                        indentingPrintWriter.print("    Running for: ");
                        TimeUtils.formatDuration(nowElapsed - jsc.getExecutionStartTimeElapsed(), indentingPrintWriter);
                        indentingPrintWriter.print(", timeout at: ");
                        TimeUtils.formatDuration(jsc.getTimeoutElapsed() - nowElapsed, indentingPrintWriter);
                        pw.println();
                        JobStatus job5 = job4;
                        job4.dump((PrintWriter) indentingPrintWriter, "    ", false, nowElapsed);
                        int priority2 = evaluateJobPriorityLocked(jsc.getRunningJobLocked());
                        if (priority2 != 0) {
                            indentingPrintWriter.print("    Evaluated priority: ");
                            indentingPrintWriter.println(priority2);
                        }
                        indentingPrintWriter.print("    Active at ");
                        TimeUtils.formatDuration(job5.madeActive - nowUptime, indentingPrintWriter);
                        indentingPrintWriter.print(", pending for ");
                        TimeUtils.formatDuration(job5.madeActive - job5.madePending, indentingPrintWriter);
                        pw.println();
                    } else if (jsc.mStoppedReason != null) {
                        indentingPrintWriter.print("inactive since ");
                        TimeUtils.formatDuration(jsc.mStoppedTime, nowElapsed, indentingPrintWriter);
                        indentingPrintWriter.print(", stopped because: ");
                        indentingPrintWriter.println(jsc.mStoppedReason);
                    } else {
                        indentingPrintWriter.println("inactive");
                    }
                    i = i7 + 1;
                }
                if (filterUid == -1) {
                    try {
                        pw.println();
                        indentingPrintWriter.print("mReadyToRock=");
                        indentingPrintWriter.println(this.mReadyToRock);
                        indentingPrintWriter.print("mReportedActive=");
                        indentingPrintWriter.println(this.mReportedActive);
                        indentingPrintWriter.print("mMaxActiveJobs=");
                        indentingPrintWriter.println(this.mMaxActiveJobs);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                pw.println();
                indentingPrintWriter.print("PersistStats: ");
                indentingPrintWriter.println(this.mJobs.getPersistStats());
                pw.println();
            } catch (Throwable th3) {
                th = th3;
                int i8 = filterUid;
                long j = nowUptime2;
                throw th;
            }
        }
    }

    static /* synthetic */ boolean lambda$dumpInternal$3(int filterUidFinal, JobStatus js) {
        return filterUidFinal == -1 || UserHandle.getAppId(js.getUid()) == filterUidFinal || UserHandle.getAppId(js.getSourceUid()) == filterUidFinal;
    }

    /* access modifiers changed from: package-private */
    public void dumpInternalProto(FileDescriptor fd, int filterUid) {
        Object obj;
        int filterUidFinal;
        int filterUidFinal2;
        Iterator<JobServiceContext> it;
        long ajToken;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        int filterUidFinal3 = UserHandle.getAppId(filterUid);
        long nowElapsed = sElapsedRealtimeClock.millis();
        long nowUptime = sUptimeMillisClock.millis();
        Predicate<JobStatus> r8 = new Predicate(filterUidFinal3) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return JobSchedulerService.lambda$dumpInternalProto$4(this.f$0, (JobStatus) obj);
            }
        };
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                this.mConstants.dump(proto, 1146756268033L);
                proto.write(1120986464270L, this.mHeartbeat);
                int i = 0;
                proto.write(2220498092047L, this.mNextBucketHeartbeat[0]);
                proto.write(2220498092047L, this.mNextBucketHeartbeat[1]);
                proto.write(2220498092047L, this.mNextBucketHeartbeat[2]);
                proto.write(2220498092047L, this.mNextBucketHeartbeat[3]);
                proto.write(1112396529680L, this.mLastHeartbeatTime - nowUptime);
                proto.write(1112396529681L, (this.mLastHeartbeatTime + this.mConstants.STANDBY_HEARTBEAT_TIME) - nowUptime);
                proto.write(1133871366162L, this.mInParole);
                int[] iArr = this.mStartedUsers;
                int length = iArr.length;
                int i2 = 0;
                while (i2 < length) {
                    try {
                        proto.write(2220498092034L, iArr[i2]);
                        i2++;
                    } catch (Throwable th) {
                        th = th;
                        int i3 = filterUid;
                        obj = obj2;
                        int i4 = filterUidFinal3;
                        long j = nowUptime;
                        $$Lambda$JobSchedulerService$rARZcsrvtM2sYbF4SrEE2BXDQ3U r11 = r8;
                        throw th;
                    }
                }
                if (this.mJobs.size() > 0) {
                    try {
                        List<JobStatus> jobs = this.mJobs.mJobSet.getAllJobs();
                        sortJobs(jobs);
                        Iterator<JobStatus> it2 = jobs.iterator();
                        while (it2.hasNext()) {
                            JobStatus job = it2.next();
                            long rjToken = proto.start(2246267895811L);
                            long nowUptime2 = nowUptime;
                            try {
                                job.writeToShortProto(proto, 1146756268033L);
                                if (!r8.test(job)) {
                                    nowUptime = nowUptime2;
                                } else {
                                    long rjToken2 = rjToken;
                                    Iterator<JobStatus> it3 = it2;
                                    JobStatus job2 = job;
                                    List<JobStatus> jobs2 = jobs;
                                    obj = obj2;
                                    filterUidFinal = filterUidFinal3;
                                    $$Lambda$JobSchedulerService$rARZcsrvtM2sYbF4SrEE2BXDQ3U r112 = r8;
                                    try {
                                        job.dump(proto, 1146756268034L, true, nowElapsed);
                                        proto.write(1133871366147L, job2.isReady());
                                        proto.write(1133871366148L, ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                                        proto.write(1133871366149L, this.mPendingJobs.contains(job2));
                                        proto.write(1133871366150L, isCurrentlyActiveLocked(job2));
                                        proto.write(1133871366151L, this.mBackingUpUids.indexOfKey(job2.getSourceUid()) >= 0);
                                        boolean componentPresent = false;
                                        try {
                                            componentPresent = AppGlobals.getPackageManager().getServiceInfo(job2.getServiceComponent(), 268435456, job2.getUserId()) != null;
                                        } catch (RemoteException e) {
                                        }
                                        proto.write(1133871366152L, componentPresent);
                                        proto.write(1112396529673L, heartbeatWhenJobsLastRun(job2));
                                        proto.end(rjToken2);
                                        r8 = r112;
                                        jobs = jobs2;
                                        obj2 = obj;
                                        it2 = it3;
                                        nowUptime = nowUptime2;
                                        filterUidFinal3 = filterUidFinal;
                                        FileDescriptor fileDescriptor = fd;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        int i5 = filterUid;
                                        int i6 = filterUidFinal;
                                    }
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                obj = obj2;
                                int i7 = filterUidFinal3;
                                $$Lambda$JobSchedulerService$rARZcsrvtM2sYbF4SrEE2BXDQ3U r113 = r8;
                                int i8 = filterUid;
                                int i9 = i7;
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        obj = obj2;
                        int i10 = filterUidFinal3;
                        long j2 = nowUptime;
                        $$Lambda$JobSchedulerService$rARZcsrvtM2sYbF4SrEE2BXDQ3U r114 = r8;
                        int i11 = filterUid;
                        int i12 = i10;
                        throw th;
                    }
                }
                obj = obj2;
                filterUidFinal = filterUidFinal3;
                long nowUptime3 = nowUptime;
                Predicate<JobStatus> predicate = r8;
                try {
                    for (StateController controller : this.mControllers) {
                        controller.dumpControllerStateLocked(proto, 2246267895812L, predicate);
                    }
                    int i13 = 0;
                    while (i13 < this.mUidPriorityOverride.size()) {
                        try {
                            int uid = this.mUidPriorityOverride.keyAt(i13);
                            filterUidFinal2 = filterUidFinal;
                            if (filterUidFinal2 != -1) {
                                try {
                                    if (filterUidFinal2 != UserHandle.getAppId(uid)) {
                                        i13++;
                                        filterUidFinal = filterUidFinal2;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    int i14 = filterUid;
                                    int i15 = filterUidFinal2;
                                    throw th;
                                }
                            }
                            long pToken = proto.start(2246267895813L);
                            proto.write(1120986464257L, uid);
                            proto.write(1172526071810L, this.mUidPriorityOverride.valueAt(i13));
                            proto.end(pToken);
                            i13++;
                            filterUidFinal = filterUidFinal2;
                        } catch (Throwable th6) {
                            th = th6;
                            int i16 = filterUid;
                            int i17 = filterUidFinal;
                            throw th;
                        }
                    }
                    filterUidFinal2 = filterUidFinal;
                    while (true) {
                        int i18 = i;
                        try {
                            if (i18 >= this.mBackingUpUids.size()) {
                                break;
                            }
                            int uid2 = this.mBackingUpUids.keyAt(i18);
                            if (filterUidFinal2 == -1 || filterUidFinal2 == UserHandle.getAppId(uid2)) {
                                proto.write(2220498092038L, uid2);
                            }
                            i = i18 + 1;
                        } catch (Throwable th7) {
                            th = th7;
                            int i19 = filterUid;
                            int i20 = filterUidFinal2;
                            throw th;
                        }
                    }
                    this.mJobPackageTracker.dump(proto, 1146756268040L, filterUidFinal2);
                    this.mJobPackageTracker.dumpHistory(proto, 1146756268039L, filterUidFinal2);
                    Iterator<JobStatus> it4 = this.mPendingJobs.iterator();
                    while (it4.hasNext()) {
                        try {
                            JobStatus job3 = it4.next();
                            long pjToken = proto.start(2246267895817L);
                            job3.writeToShortProto(proto, 1146756268033L);
                            int filterUidFinal4 = filterUidFinal2;
                            long pjToken2 = pjToken;
                            job3.dump(proto, 1146756268034L, false, nowElapsed);
                            int priority = evaluateJobPriorityLocked(job3);
                            if (priority != 0) {
                                proto.write(1172526071811L, priority);
                            }
                            proto.write(1112396529668L, nowUptime3 - job3.madePending);
                            proto.end(pjToken2);
                            filterUidFinal2 = filterUidFinal4;
                        } catch (Throwable th8) {
                            th = th8;
                            int i21 = filterUid;
                            throw th;
                        }
                    }
                    int i22 = filterUidFinal2;
                    Iterator<JobServiceContext> it5 = this.mActiveServices.iterator();
                    while (it5.hasNext()) {
                        JobServiceContext jsc = it5.next();
                        long ajToken2 = proto.start(2246267895818L);
                        JobStatus job4 = jsc.getRunningJobLocked();
                        if (job4 == null) {
                            long ijToken = proto.start(1146756268033L);
                            ajToken = ajToken2;
                            proto.write(1112396529665L, nowElapsed - jsc.mStoppedTime);
                            if (jsc.mStoppedReason != null) {
                                proto.write(1138166333442L, jsc.mStoppedReason);
                            }
                            proto.end(ijToken);
                            it = it5;
                            JobStatus jobStatus = job4;
                        } else {
                            ajToken = ajToken2;
                            long rjToken3 = proto.start(1146756268034L);
                            job4.writeToShortProto(proto, 1146756268033L);
                            proto.write(1112396529666L, nowElapsed - jsc.getExecutionStartTimeElapsed());
                            proto.write(1112396529667L, jsc.getTimeoutElapsed() - nowElapsed);
                            it = it5;
                            JobStatus job5 = job4;
                            job4.dump(proto, 1146756268036L, false, nowElapsed);
                            int priority2 = evaluateJobPriorityLocked(jsc.getRunningJobLocked());
                            if (priority2 != 0) {
                                proto.write(1172526071813L, priority2);
                            }
                            proto.write(1112396529670L, nowUptime3 - job5.madeActive);
                            proto.write(1112396529671L, job5.madeActive - job5.madePending);
                            proto.end(rjToken3);
                        }
                        proto.end(ajToken);
                        it5 = it;
                    }
                    if (filterUid == -1) {
                        try {
                            proto.write(1133871366155L, this.mReadyToRock);
                            proto.write(1133871366156L, this.mReportedActive);
                            proto.write(1120986464269L, this.mMaxActiveJobs);
                        } catch (Throwable th9) {
                            th = th9;
                            throw th;
                        }
                    }
                    proto.flush();
                } catch (Throwable th10) {
                    th = th10;
                    int i23 = filterUid;
                    int i24 = filterUidFinal;
                    throw th;
                }
            } catch (Throwable th11) {
                th = th11;
                int i25 = filterUid;
                obj = obj2;
                int i26 = filterUidFinal3;
                long j3 = nowUptime;
                Predicate<JobStatus> predicate2 = r8;
                throw th;
            }
        }
    }

    static /* synthetic */ boolean lambda$dumpInternalProto$4(int filterUidFinal, JobStatus js) {
        return filterUidFinal == -1 || UserHandle.getAppId(js.getUid()) == filterUidFinal || UserHandle.getAppId(js.getSourceUid()) == filterUidFinal;
    }
}
