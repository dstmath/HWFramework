package com.android.server.job;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.IUidObserver.Stub;
import android.app.job.IJobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.JobStore.JobStatusFunctor;
import com.android.server.job.controllers.AppIdleController;
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
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import libcore.util.EmptyArray;

public class JobSchedulerService extends AbsJobSchedulerService implements StateChangedListener, JobCompletedListener {
    public static final boolean DEBUG = false;
    private static final boolean ENFORCE_MAX_JOBS = true;
    private static final int FG_JOB_CONTEXTS_COUNT = 2;
    private static final int MAX_JOBS_PER_APP = 100;
    private static final int MAX_JOB_CONTEXTS_COUNT = 16;
    static final int MSG_CHECK_JOB = 1;
    static final int MSG_CHECK_JOB_GREEDY = 3;
    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_STOP_JOB = 2;
    static final String TAG = "JobSchedulerService";
    static final Comparator<JobStatus> mEnqueueTimeComparator = new -$Lambda$MZyz9fgevtnL7iKUFvjeGfWQ-E8();
    final List<JobServiceContext> mActiveServices = new ArrayList();
    final SparseIntArray mBackingUpUids = new SparseIntArray();
    BatteryController mBatteryController;
    IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0145 A:{Splitter: B:12:0x003d, ExcHandler: android.os.RemoteException (e android.os.RemoteException)} */
        /* JADX WARNING: Missing block: B:67:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String pkgName;
            int pkgUid;
            if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                pkgName = JobSchedulerService.this.getPackageName(intent);
                pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (pkgName == null || pkgUid == -1) {
                    Slog.w(JobSchedulerService.TAG, "PACKAGE_CHANGED for " + pkgName + " / uid " + pkgUid);
                    return;
                }
                String[] changedComponents = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                if (changedComponents != null) {
                    int i = 0;
                    int length = changedComponents.length;
                    while (i < length) {
                        if (changedComponents[i].equals(pkgName)) {
                            try {
                                int state = AppGlobals.getPackageManager().getApplicationEnabledSetting(pkgName, UserHandle.getUserId(pkgUid));
                                if (state == 2 || state == 3) {
                                    JobSchedulerService.this.cancelJobsForUid(pkgUid, "app package state changed");
                                    return;
                                }
                                return;
                            } catch (RemoteException e) {
                            }
                        } else {
                            i++;
                        }
                    }
                }
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    JobSchedulerService.this.cancelJobsForUid(intent.getIntExtra("android.intent.extra.UID", -1), "app uninstalled");
                }
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                JobSchedulerService.this.cancelJobsForUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
            } else if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                pkgName = intent.getData().getSchemeSpecificPart();
                if (pkgUid != -1) {
                    List<JobStatus> jobsForUid;
                    synchronized (JobSchedulerService.this.mLock) {
                        jobsForUid = JobSchedulerService.this.mJobs.getJobsByUid(pkgUid);
                    }
                    for (int i2 = jobsForUid.size() - 1; i2 >= 0; i2--) {
                        if (((JobStatus) jobsForUid.get(i2)).getSourcePackageName().equals(pkgName)) {
                            setResultCode(-1);
                            return;
                        }
                    }
                }
            } else if ("android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                pkgName = intent.getData().getSchemeSpecificPart();
                if (pkgUid != -1) {
                    JobSchedulerService.this.cancelJobsForPackageAndUid(pkgName, pkgUid);
                }
            }
        }
    };
    final Constants mConstants;
    List<StateController> mControllers;
    final JobHandler mHandler;
    final JobPackageTracker mJobPackageTracker = new JobPackageTracker();
    final JobSchedulerStub mJobSchedulerStub;
    final JobStore mJobs;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock = new Object();
    int mMaxActiveJobs = 1;
    private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();
    final ArrayList<JobStatus> mPendingJobs = new ArrayList();
    PowerManager mPowerManager;
    private final ReadyJobQueueFunctor mReadyQueueFunctor = new ReadyJobQueueFunctor();
    boolean mReadyToRock;
    boolean mReportedActive;
    int[] mStartedUsers = EmptyArray.INT;
    StorageController mStorageController;
    boolean[] mTmpAssignAct = new boolean[16];
    JobStatus[] mTmpAssignContextIdToJobMap = new JobStatus[16];
    int[] mTmpAssignPreferredUidForContext = new int[16];
    private final IUidObserver mUidObserver = new Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
            JobSchedulerService.this.updateUidState(uid, procState);
        }

        public void onUidGone(int uid, boolean disabled) throws RemoteException {
            JobSchedulerService.this.updateUidState(uid, 17);
            if (disabled) {
                JobSchedulerService.this.cancelJobsForUid(uid, "uid gone");
            }
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
            if (disabled) {
                JobSchedulerService.this.cancelJobsForUid(uid, "app uid idle");
            }
        }
    };
    final SparseIntArray mUidPriorityOverride = new SparseIntArray();

    private final class Constants extends ContentObserver {
        private static final int DEFAULT_BG_CRITICAL_JOB_COUNT = 1;
        private static final int DEFAULT_BG_LOW_JOB_COUNT = 1;
        private static final int DEFAULT_BG_MODERATE_JOB_COUNT = 4;
        private static final int DEFAULT_BG_NORMAL_JOB_COUNT = 6;
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
        private static final String KEY_BG_CRITICAL_JOB_COUNT = "bg_critical_job_count";
        private static final String KEY_BG_LOW_JOB_COUNT = "bg_low_job_count";
        private static final String KEY_BG_MODERATE_JOB_COUNT = "bg_moderate_job_count";
        private static final String KEY_BG_NORMAL_JOB_COUNT = "bg_normal_job_count";
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
        int BG_CRITICAL_JOB_COUNT = 1;
        int BG_LOW_JOB_COUNT = 1;
        int BG_MODERATE_JOB_COUNT = 4;
        int BG_NORMAL_JOB_COUNT = 6;
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
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("job_scheduler_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (JobSchedulerService.this.mLock) {
                try {
                    this.mParser.setString(Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(JobSchedulerService.TAG, "Bad device idle settings", e);
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
                this.MIN_LINEAR_BACKOFF_TIME = this.mParser.getLong(KEY_MIN_LINEAR_BACKOFF_TIME, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                this.MIN_EXP_BACKOFF_TIME = this.mParser.getLong(KEY_MIN_EXP_BACKOFF_TIME, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            }
            return;
        }

        void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_MIN_IDLE_COUNT);
            pw.print("=");
            pw.print(this.MIN_IDLE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CHARGING_COUNT);
            pw.print("=");
            pw.print(this.MIN_CHARGING_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_BATTERY_NOT_LOW_COUNT);
            pw.print("=");
            pw.print(this.MIN_BATTERY_NOT_LOW_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_STORAGE_NOT_LOW_COUNT);
            pw.print("=");
            pw.print(this.MIN_STORAGE_NOT_LOW_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CONNECTIVITY_COUNT);
            pw.print("=");
            pw.print(this.MIN_CONNECTIVITY_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_CONTENT_COUNT);
            pw.print("=");
            pw.print(this.MIN_CONTENT_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_READY_JOBS_COUNT);
            pw.print("=");
            pw.print(this.MIN_READY_JOBS_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_HEAVY_USE_FACTOR);
            pw.print("=");
            pw.print(this.HEAVY_USE_FACTOR);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MODERATE_USE_FACTOR);
            pw.print("=");
            pw.print(this.MODERATE_USE_FACTOR);
            pw.println();
            pw.print("    ");
            pw.print(KEY_FG_JOB_COUNT);
            pw.print("=");
            pw.print(this.FG_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_NORMAL_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_NORMAL_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_MODERATE_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_MODERATE_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_LOW_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_LOW_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_BG_CRITICAL_JOB_COUNT);
            pw.print("=");
            pw.print(this.BG_CRITICAL_JOB_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_STANDARD_RESCHEDULE_COUNT);
            pw.print("=");
            pw.print(this.MAX_STANDARD_RESCHEDULE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MAX_WORK_RESCHEDULE_COUNT);
            pw.print("=");
            pw.print(this.MAX_WORK_RESCHEDULE_COUNT);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_LINEAR_BACKOFF_TIME);
            pw.print("=");
            pw.print(this.MIN_LINEAR_BACKOFF_TIME);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_EXP_BACKOFF_TIME);
            pw.print("=");
            pw.print(this.MIN_EXP_BACKOFF_TIME);
            pw.println();
        }
    }

    private final class JobHandler extends Handler {
        public JobHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.this.mReadyToRock) {
                    switch (message.what) {
                        case 0:
                            JobStatus runNow = message.obj;
                            if (runNow != null && JobSchedulerService.this.isReadyToBeExecutedLocked(runNow)) {
                                JobSchedulerService.this.mJobPackageTracker.notePending(runNow);
                                JobSchedulerService.addOrderedItem(JobSchedulerService.this.mPendingJobs, runNow, JobSchedulerService.mEnqueueTimeComparator);
                                break;
                            }
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                            break;
                            break;
                        case 1:
                            if (!JobSchedulerService.this.mReportedActive) {
                                JobSchedulerService.this.maybeQueueReadyJobsForExecutionLocked();
                                break;
                            } else {
                                JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                                break;
                            }
                        case 2:
                            JobSchedulerService.this.cancelJobImplLocked((JobStatus) message.obj, null, "app no longer allowed to run");
                            break;
                        case 3:
                            JobSchedulerService.this.queueReadyJobsForExecutionLocked();
                            break;
                    }
                    JobSchedulerService.this.maybeRunPendingJobsLocked();
                    removeMessages(1);
                    return;
                }
            }
        }
    }

    final class JobSchedulerStub extends IJobScheduler.Stub {
        private final SparseArray<Boolean> mPersistCache = new SparseArray();

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
                Boolean cached = (Boolean) this.mPersistCache.get(uid);
                if (cached != null) {
                    canPersist = cached.booleanValue();
                } else {
                    canPersist = JobSchedulerService.this.getContext().checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", pid, uid) == 0;
                    this.mPersistCache.put(uid, Boolean.valueOf(canPersist));
                }
            }
            return canPersist;
        }

        public int schedule(JobInfo job) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            enforceValidJobRequest(uid, job);
            if (!job.isPersisted() || canPersistJobs(pid, uid)) {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, null, uid, null, -1, null);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.");
            }
        }

        public int enqueue(JobInfo job, JobWorkItem work) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            enforceValidJobRequest(uid, job);
            if (job.isPersisted()) {
                throw new IllegalArgumentException("Can't enqueue work for persisted jobs");
            } else if (work == null) {
                throw new NullPointerException("work is null");
            } else {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, work, uid, null, -1, null);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) throws RemoteException {
            int callerUid = Binder.getCallingUid();
            if (packageName == null) {
                throw new NullPointerException("Must specify a package for scheduleAsPackage()");
            } else if (JobSchedulerService.this.getContext().checkCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS") != 0) {
                throw new SecurityException("Caller uid " + callerUid + " not permitted to schedule jobs for other apps");
            } else {
                if ((job.getFlags() & 1) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, null, callerUid, packageName, userId, tag);
                    return scheduleAsPackage;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public List<JobInfo> getAllPendingJobs() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                List<JobInfo> pendingJobs = JobSchedulerService.this.getPendingJobs(uid);
                return pendingJobs;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public JobInfo getPendingJob(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobInfo pendingJob = JobSchedulerService.this.getPendingJob(uid, jobId);
                return pendingJob;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancelAll() throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJobsForUid(uid, "cancelAll() called by app");
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void cancel(int jobId) throws RemoteException {
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJob(uid, jobId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(JobSchedulerService.this.getContext(), JobSchedulerService.TAG, pw)) {
                long identityToken = Binder.clearCallingIdentity();
                try {
                    JobSchedulerService.this.dumpInternal(pw, args);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new JobSchedulerShellCommand(JobSchedulerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    final class LocalService implements JobSchedulerInternal {
        LocalService() {
        }

        public List<JobInfo> getSystemScheduledPendingJobs() {
            final List<JobInfo> pendingJobs;
            synchronized (JobSchedulerService.this.mLock) {
                pendingJobs = new ArrayList();
                JobSchedulerService.this.mJobs.forEachJob(1000, new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        if (job.getJob().isPeriodic() || (JobSchedulerService.this.isCurrentlyActiveLocked(job) ^ 1) != 0) {
                            pendingJobs.add(job.getJob());
                        }
                    }
                });
            }
            return pendingJobs;
        }

        public boolean proxyService(int type, List<String> value) {
            boolean proxyServiceLocked;
            synchronized (JobSchedulerService.this.mLock) {
                proxyServiceLocked = HwTimeController.get(JobSchedulerService.this).proxyServiceLocked(type, value);
            }
            return proxyServiceLocked;
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
    }

    final class MaybeReadyJobQueueFunctor implements JobStatusFunctor {
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

        public void process(JobStatus job) {
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
                JobSchedulerService.this.noteJobsPending(this.runnableJobs);
                JobSchedulerService.this.mPendingJobs.addAll(this.runnableJobs);
                if (JobSchedulerService.this.mPendingJobs.size() > 1) {
                    JobSchedulerService.this.mPendingJobs.sort(JobSchedulerService.mEnqueueTimeComparator);
                }
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

    final class ReadyJobQueueFunctor implements JobStatusFunctor {
        ArrayList<JobStatus> newReadyJobs;

        ReadyJobQueueFunctor() {
        }

        public void process(JobStatus job) {
            if (JobSchedulerService.this.isReadyToBeExecutedLocked(job)) {
                if (this.newReadyJobs == null) {
                    this.newReadyJobs = new ArrayList();
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

    static /* synthetic */ int lambda$-com_android_server_job_JobSchedulerService_22305(JobStatus o1, JobStatus o2) {
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

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        return uri != null ? uri.getSchemeSpecificPart() : null;
    }

    public Object getLock() {
        return this.mLock;
    }

    public JobStore getJobStore() {
        return this.mJobs;
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

    /* JADX WARNING: Missing block: B:39:0x00d1, code:
            return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int scheduleAsPackage(JobInfo job, JobWorkItem work, int uId, String packageName, int userId, String tag) {
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(uId, job.getService().getPackageName())) {
                Slog.w(TAG, "Not scheduling job " + uId + ":" + job.toString() + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uId, job.getId());
            if (work == null || toCancel == null || !toCancel.getJob().equals(job)) {
                JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
                if (packageName != null || this.mJobs.countJobsForUid(uId) <= 100) {
                    jobStatus.prepareLocked(ActivityManager.getService());
                    if (toCancel != null) {
                        cancelJobImplLocked(toCancel, jobStatus, "job rescheduled by app");
                    }
                    if (work != null) {
                        jobStatus.enqueueWorkLocked(ActivityManager.getService(), work);
                    }
                    startTrackingJobLocked(jobStatus, toCancel);
                    if (isReadyToBeExecutedLocked(jobStatus)) {
                        this.mJobPackageTracker.notePending(jobStatus);
                        addOrderedItem(this.mPendingJobs, jobStatus, mEnqueueTimeComparator);
                        maybeRunPendingJobsLocked();
                    }
                } else {
                    Slog.w(TAG, "Too many jobs for uid " + uId);
                    throw new IllegalStateException("Apps may not schedule more than 100 distinct jobs");
                }
            }
            toCancel.enqueueWorkLocked(ActivityManager.getService(), work);
            return 1;
        }
    }

    public List<JobInfo> getPendingJobs(int uid) {
        ArrayList<JobInfo> outList;
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            outList = new ArrayList(jobs.size());
            for (int i = jobs.size() - 1; i >= 0; i--) {
                outList.add(((JobStatus) jobs.get(i)).getJob());
            }
        }
        return outList;
    }

    public JobInfo getPendingJob(int uid, int jobId) {
        synchronized (this.mLock) {
            List<JobStatus> jobs = this.mJobs.getJobsByUid(uid);
            for (int i = jobs.size() - 1; i >= 0; i--) {
                JobStatus job = (JobStatus) jobs.get(i);
                if (job.getJobId() == jobId) {
                    JobInfo job2 = job.getJob();
                    return job2;
                }
            }
            return null;
        }
    }

    void cancelJobsForUser(int userHandle) {
        synchronized (this.mLock) {
            List<JobStatus> jobsForUser = this.mJobs.getJobsByUser(userHandle);
            for (int i = 0; i < jobsForUser.size(); i++) {
                cancelJobImplLocked((JobStatus) jobsForUser.get(i), null, "user removed");
            }
        }
    }

    private void cancelJobsForNonExistentUsers() {
        UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        synchronized (this.mLock) {
            this.mJobs.removeJobsOfNonUsers(umi.getUserIds());
        }
    }

    void cancelJobsForPackageAndUid(String pkgName, int uid) {
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = jobsForUid.size() - 1; i >= 0; i--) {
                JobStatus job = (JobStatus) jobsForUid.get(i);
                if (job.getSourcePackageName().equals(pkgName)) {
                    cancelJobImplLocked(job, null, "app force stopped");
                }
            }
        }
    }

    public void cancelJobsForUid(int uid, String reason) {
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
            for (int i = 0; i < jobsForUid.size(); i++) {
                cancelJobImplLocked((JobStatus) jobsForUid.get(i), null, reason);
            }
        }
    }

    public void cancelJob(int uid, int jobId) {
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uid, jobId);
            if (toCancel != null) {
                cancelJobImplLocked(toCancel, null, "cancel() called by app");
            }
        }
    }

    private void cancelJobImplLocked(JobStatus cancelled, JobStatus incomingJob, String reason) {
        cancelled.unprepareLocked(ActivityManager.getService());
        stopTrackingJobLocked(cancelled, incomingJob, true);
        if (this.mPendingJobs.remove(cancelled)) {
            this.mJobPackageTracker.noteNonpending(cancelled);
        }
        stopJobOnServiceContextLocked(cancelled, 0, reason);
        reportActiveLocked();
    }

    void updateUidState(int uid, int procState) {
        synchronized (this.mLock) {
            if (procState == 2) {
                this.mUidPriorityOverride.put(uid, 40);
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
                    JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                    JobStatus executing = jsc.getRunningJobLocked();
                    if (executing != null && (executing.getFlags() & 1) == 0) {
                        jsc.cancelExecutingJobLocked(4, "cancelled due to doze");
                    }
                }
            } else if (this.mReadyToRock) {
                if (!(this.mLocalDeviceIdleController == null || this.mReportedActive)) {
                    this.mReportedActive = true;
                    this.mLocalDeviceIdleController.setJobsActive(true);
                }
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    void reportActiveLocked() {
        boolean active = this.mPendingJobs.size() > 0;
        if (this.mPendingJobs.size() <= 0) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                JobStatus job = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked();
                if (job != null && (job.getJob().getFlags() & 1) == 0 && (job.dozeWhitelisted ^ 1) != 0) {
                    active = true;
                    break;
                }
            }
        }
        if (this.mReportedActive != active) {
            this.mReportedActive = active;
            if (this.mLocalDeviceIdleController != null) {
                this.mLocalDeviceIdleController.setJobsActive(active);
            }
        }
    }

    public JobSchedulerService(Context context) {
        super(context);
        this.mHandler = new JobHandler(context.getMainLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mJobSchedulerStub = new JobSchedulerStub();
        this.mJobs = JobStore.initAndGet(this);
        this.mControllers = new ArrayList();
        this.mControllers.add(ConnectivityController.get(this));
        this.mControllers.add(HwTimeController.get(this));
        this.mControllers.add(IdleController.get(this));
        this.mBatteryController = BatteryController.get(this);
        this.mControllers.add(this.mBatteryController);
        this.mStorageController = StorageController.get(this);
        this.mControllers.add(this.mStorageController);
        this.mControllers.add(AppIdleController.get(this));
        this.mControllers.add(ContentObserverController.get(this));
        this.mControllers.add(DeviceIdleJobsController.get(this));
    }

    public void onStart() {
        publishLocalService(JobSchedulerInternal.class, new LocalService());
        publishBinderService("jobscheduler", this.mJobSchedulerStub);
    }

    public void onBootPhase(int phase) {
        if (500 == phase) {
            this.mConstants.start(getContext().getContentResolver());
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_REMOVED"), null, null);
            this.mPowerManager = (PowerManager) getContext().getSystemService("power");
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 7, -1, null);
            } catch (RemoteException e) {
            }
            cancelJobsForNonExistentUsers();
        } else if (phase == 600) {
            synchronized (this.mLock) {
                this.mReadyToRock = true;
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
                for (int i = 0; i < 16; i++) {
                    this.mActiveServices.add(new JobServiceContext(this, this.mBatteryStats, this.mJobPackageTracker, getContext().getMainLooper()));
                }
                this.mJobs.forEachJob(new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        for (int controller = 0; controller < JobSchedulerService.this.mControllers.size(); controller++) {
                            ((StateController) JobSchedulerService.this.mControllers.get(controller)).maybeStartTrackingJobLocked(job, null);
                        }
                    }
                });
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    private void startTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (!jobStatus.isPreparedLocked()) {
            Slog.wtf(TAG, "Not yet prepared when started tracking: " + jobStatus);
        }
        jobStatus.enqueueTime = SystemClock.elapsedRealtime();
        boolean update = this.mJobs.add(jobStatus);
        if (this.mReadyToRock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                StateController controller = (StateController) this.mControllers.get(i);
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
                ((StateController) this.mControllers.get(i)).maybeStopTrackingJobLocked(jobStatus, incomingJob, false);
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason, String debugReason) {
        int i = 0;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
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

    private boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i = 0; i < this.mActiveServices.size(); i++) {
            JobStatus running = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    void noteJobsPending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.notePending((JobStatus) jobs.get(i));
        }
    }

    void noteJobsNonpending(List<JobStatus> jobs) {
        for (int i = jobs.size() - 1; i >= 0; i--) {
            this.mJobPackageTracker.noteNonpending((JobStatus) jobs.get(i));
        }
    }

    private JobStatus getRescheduleJobForFailureLocked(JobStatus failureToReschedule) {
        long delayMillis;
        long elapsedNowMillis = SystemClock.elapsedRealtime();
        JobInfo job = failureToReschedule.getJob();
        long initialBackoffMillis = job.getInitialBackoffMillis();
        int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        if (failureToReschedule.hasWorkLocked()) {
            if (backoffAttempts > this.mConstants.MAX_WORK_RESCHEDULE_COUNT) {
                Slog.w(TAG, "Not rescheduling " + failureToReschedule + ": attempt #" + backoffAttempts + " > work limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
                return null;
            }
        } else if (backoffAttempts > this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT) {
            Slog.w(TAG, "Not rescheduling " + failureToReschedule + ": attempt #" + backoffAttempts + " > std limit " + this.mConstants.MAX_STANDARD_RESCHEDULE_COUNT);
            return null;
        }
        long backoff;
        switch (job.getBackoffPolicy()) {
            case 0:
                backoff = initialBackoffMillis;
                if (initialBackoffMillis < this.mConstants.MIN_LINEAR_BACKOFF_TIME) {
                    backoff = this.mConstants.MIN_LINEAR_BACKOFF_TIME;
                }
                delayMillis = backoff * ((long) backoffAttempts);
                break;
            default:
                backoff = initialBackoffMillis;
                if (initialBackoffMillis < this.mConstants.MIN_EXP_BACKOFF_TIME) {
                    backoff = this.mConstants.MIN_EXP_BACKOFF_TIME;
                }
                delayMillis = (long) Math.scalb((float) backoff, backoffAttempts - 1);
                break;
        }
        JobStatus newJob = new JobStatus(failureToReschedule, elapsedNowMillis + Math.min(delayMillis, 18000000), JobStatus.NO_LATEST_RUNTIME, backoffAttempts);
        for (int ic = 0; ic < this.mControllers.size(); ic++) {
            ((StateController) this.mControllers.get(ic)).rescheduleForFailureLocked(newJob, failureToReschedule);
        }
        return newJob;
    }

    private JobStatus getRescheduleJobForPeriodic(JobStatus periodicToReschedule) {
        long elapsedNow = SystemClock.elapsedRealtime();
        long runEarly = 0;
        if (periodicToReschedule.hasDeadlineConstraint()) {
            runEarly = Math.max(periodicToReschedule.getLatestRunTimeElapsed() - elapsedNow, 0);
        }
        long newLatestRuntimeElapsed = (elapsedNow + runEarly) + periodicToReschedule.getJob().getIntervalMillis();
        return new JobStatus(periodicToReschedule, newLatestRuntimeElapsed - periodicToReschedule.getJob().getFlexMillis(), newLatestRuntimeElapsed, 0);
    }

    public void onJobCompletedLocked(JobStatus jobStatus, boolean needsReschedule) {
        JobStatus rescheduledJob = needsReschedule ? getRescheduleJobForFailureLocked(jobStatus) : null;
        if (stopTrackingJobLocked(jobStatus, rescheduledJob, jobStatus.getJob().isPeriodic() ^ 1)) {
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
            return;
        }
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
            JobServiceContext serviceContext = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus running = serviceContext.getRunningJobLocked();
            if (!(running == null || (running.isReady() ^ 1) == 0)) {
                serviceContext.cancelExecutingJobLocked(1, "cancelled due to unsatisfied constraints");
            }
        }
    }

    private void queueReadyJobsForExecutionLocked() {
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mReadyQueueFunctor);
        this.mReadyQueueFunctor.postProcess();
    }

    private void maybeQueueReadyJobsForExecutionLocked() {
        noteJobsNonpending(this.mPendingJobs);
        this.mPendingJobs.clear();
        stopNonReadyActiveJobsLocked();
        this.mJobs.forEachJob(this.mMaybeQueueFunctor);
        this.mMaybeQueueFunctor.postProcess();
    }

    private boolean isReadyToBeExecutedLocked(JobStatus job) {
        if (!job.isReady()) {
            return false;
        }
        boolean jobExists = this.mJobs.containsJob(job);
        int userId = job.getUserId();
        boolean userStarted = ArrayUtils.contains(this.mStartedUsers, userId);
        if (!jobExists || (userStarted ^ 1) != 0) {
            return false;
        }
        boolean jobPending = this.mPendingJobs.contains(job);
        boolean jobActive = isCurrentlyActiveLocked(job);
        if (jobPending || jobActive) {
            return false;
        }
        try {
            boolean componentPresent;
            if (AppGlobals.getPackageManager().getServiceInfo(job.getServiceComponent(), 268435456, userId) != null) {
                componentPresent = true;
            } else {
                componentPresent = false;
            }
            return componentPresent;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    private void maybeRunPendingJobsLocked() {
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

    /* JADX WARNING: Removed duplicated region for block: B:81:0x00d3 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0134  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void assignJobsToContextsLocked() {
        int memLevel;
        int i;
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
        int numActive = 0;
        int numForeground = 0;
        for (i = 0; i < 16; i++) {
            JobServiceContext js = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus status = js.getRunningJobLocked();
            contextIdToJobMap[i] = status;
            if (status != null) {
                numActive++;
                if (status.lastEvaluatedPriority >= 40) {
                    numForeground++;
                }
            }
            act[i] = false;
            preferredUidForContext[i] = js.getPreferredUid();
        }
        for (i = 0; i < this.mPendingJobs.size(); i++) {
            JobStatus nextPending = (JobStatus) this.mPendingJobs.get(i);
            if (findJobContextIdFromMap(nextPending, contextIdToJobMap) == -1) {
                int priority = evaluateJobPriorityLocked(nextPending);
                nextPending.lastEvaluatedPriority = priority;
                int minPriority = HwBootFail.STAGE_BOOT_SUCCESS;
                int minPriorityContextId = -1;
                for (int j = 0; j < 16; j++) {
                    JobStatus job = contextIdToJobMap[j];
                    int preferredUid = preferredUidForContext[j];
                    if (job == null) {
                        if ((numActive < this.mMaxActiveJobs || (priority >= 40 && numForeground < this.mConstants.FG_JOB_COUNT)) && (preferredUid == nextPending.getUid() || preferredUid == -1)) {
                            minPriorityContextId = j;
                            if (minPriorityContextId == -1) {
                                contextIdToJobMap[minPriorityContextId] = nextPending;
                                act[minPriorityContextId] = true;
                                numActive++;
                                if (priority >= 40) {
                                    numForeground++;
                                }
                            }
                        }
                    } else {
                        if (job.getUid() == nextPending.getUid() && evaluateJobPriorityLocked(job) < nextPending.lastEvaluatedPriority && minPriority > nextPending.lastEvaluatedPriority) {
                            minPriority = nextPending.lastEvaluatedPriority;
                            minPriorityContextId = j;
                        }
                    }
                }
                if (minPriorityContextId == -1) {
                }
            }
        }
        this.mJobPackageTracker.noteConcurrency(numActive, numForeground);
        for (i = 0; i < 16; i++) {
            boolean preservePreferredUid = false;
            if (act[i]) {
                if (((JobServiceContext) this.mActiveServices.get(i)).getRunningJobLocked() != null) {
                    ((JobServiceContext) this.mActiveServices.get(i)).preemptExecutingJobLocked();
                    preservePreferredUid = true;
                } else {
                    JobStatus pendingJob = contextIdToJobMap[i];
                    for (int ic = 0; ic < this.mControllers.size(); ic++) {
                        ((StateController) this.mControllers.get(ic)).prepareForExecutionLocked(pendingJob);
                    }
                    if (!((JobServiceContext) this.mActiveServices.get(i)).executeRunnableJob(pendingJob)) {
                        Slog.d(TAG, "Error executing " + pendingJob);
                    }
                    if (this.mPendingJobs.remove(pendingJob)) {
                        this.mJobPackageTracker.noteNonpending(pendingJob);
                    }
                }
            }
            if (!preservePreferredUid) {
                ((JobServiceContext) this.mActiveServices.get(i)).clearPreferredUid();
            }
        }
    }

    int findJobContextIdFromMap(JobStatus jobStatus, JobStatus[] map) {
        int i = 0;
        while (i < map.length) {
            if (map[i] != null && map[i].matches(jobStatus.getUid(), jobStatus.getJobId())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    int executeRunCommand(String pkgName, int userId, int jobId, boolean force) {
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (userId == -1) {
                userId = 0;
            }
            int uid = packageManager.getPackageUid(pkgName, 0, userId);
            if (uid < 0) {
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (js == null) {
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                js.overrideState = force ? 2 : 1;
                if (js.isConstraintsSatisfied()) {
                    queueReadyJobsForExecutionLocked();
                    maybeRunPendingJobsLocked();
                } else {
                    js.overrideState = 0;
                    return JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS;
                }
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    int executeTimeoutCommand(PrintWriter pw, String pkgName, int userId, boolean hasJobId, int jobId) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mActiveServices.size(); i++) {
                ((JobServiceContext) this.mActiveServices.get(i)).timeoutIfExecutingLocked(pkgName, userId, hasJobId, jobId);
            }
            if (!false) {
                pw.println("No matching executing jobs found.");
            }
        }
        return 0;
    }

    void setMonitorBattery(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mBatteryController != null) {
                this.mBatteryController.getTracker().setMonitorBatteryLocked(enabled);
            }
        }
    }

    int getBatterySeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mBatteryController != null ? this.mBatteryController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    boolean getBatteryCharging() {
        boolean isOnStablePower;
        synchronized (this.mLock) {
            isOnStablePower = this.mBatteryController != null ? this.mBatteryController.getTracker().isOnStablePower() : false;
        }
        return isOnStablePower;
    }

    boolean getBatteryNotLow() {
        boolean isBatteryNotLow;
        synchronized (this.mLock) {
            isBatteryNotLow = this.mBatteryController != null ? this.mBatteryController.getTracker().isBatteryNotLow() : false;
        }
        return isBatteryNotLow;
    }

    int getStorageSeq() {
        int seq;
        synchronized (this.mLock) {
            seq = this.mStorageController != null ? this.mStorageController.getTracker().getSeq() : -1;
        }
        return seq;
    }

    boolean getStorageNotLow() {
        boolean isStorageNotLow;
        synchronized (this.mLock) {
            isStorageNotLow = this.mStorageController != null ? this.mStorageController.getTracker().isStorageNotLow() : false;
        }
        return isStorageNotLow;
    }

    int getJobState(PrintWriter pw, String pkgName, int userId, int jobId) {
        try {
            IPackageManager packageManager = AppGlobals.getPackageManager();
            if (userId == -1) {
                userId = 0;
            }
            int uid = packageManager.getPackageUid(pkgName, 0, userId);
            if (uid < 0) {
                pw.print("unknown(");
                pw.print(pkgName);
                pw.println(")");
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
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

    private String printContextIdToJobMap(JobStatus[] map, String initial) {
        StringBuilder s = new StringBuilder(initial + ": ");
        for (int i = 0; i < map.length; i++) {
            s.append("(").append(map[i] == null ? -1 : map[i].getJobId()).append(map[i] == null ? -1 : map[i].getUid()).append(")");
        }
        return s.toString();
    }

    private String printPendingQueue() {
        StringBuilder s = new StringBuilder("Pending queue: ");
        Iterator<JobStatus> it = this.mPendingJobs.iterator();
        while (it.hasNext()) {
            JobStatus js = (JobStatus) it.next();
            s.append("(").append(js.getJob().getId()).append(", ").append(js.getUid()).append(") ");
        }
        return s.toString();
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Job Scheduler (jobscheduler) dump options:");
        pw.println("  [-h] [package] ...");
        pw.println("    -h: print this help");
        pw.println("  [package] is an optional package name to limit the output to.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x005b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dumpInternal(PrintWriter pw, String[] args) {
        int filterUid = -1;
        if (!ArrayUtils.isEmpty(args)) {
            int opti = 0;
            while (opti < args.length) {
                String arg = args[opti];
                if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                } else if ("-a".equals(arg)) {
                    opti++;
                } else {
                    if (arg.length() > 0 && arg.charAt(0) == '-') {
                        pw.println("Unknown option: " + arg);
                        return;
                    }
                    if (opti < args.length) {
                        String pkg = args[opti];
                        try {
                            filterUid = getContext().getPackageManager().getPackageUid(pkg, DumpState.DUMP_CHANGES);
                        } catch (NameNotFoundException e) {
                            pw.println("Invalid package: " + pkg);
                            return;
                        }
                    }
                }
            }
            if (opti < args.length) {
            }
        }
        int filterUidFinal = UserHandle.getAppId(filterUid);
        long nowElapsed = SystemClock.elapsedRealtime();
        long nowUptime = SystemClock.uptimeMillis();
        synchronized (this.mLock) {
            JobStatus job;
            int i;
            int uid;
            int priority;
            this.mConstants.dump(pw);
            pw.println();
            pw.println("Started users: " + Arrays.toString(this.mStartedUsers));
            pw.print("Registered ");
            pw.print(this.mJobs.size());
            pw.println(" jobs:");
            if (this.mJobs.size() > 0) {
                List<JobStatus> jobs = this.mJobs.mJobSet.getAllJobs();
                Collections.sort(jobs, new Comparator<JobStatus>() {
                    public int compare(JobStatus o1, JobStatus o2) {
                        int i = -1;
                        int uid1 = o1.getUid();
                        int uid2 = o2.getUid();
                        int id1 = o1.getJobId();
                        int id2 = o2.getJobId();
                        if (uid1 != uid2) {
                            if (uid1 >= uid2) {
                                i = 1;
                            }
                            return i;
                        }
                        if (id1 >= id2) {
                            i = id1 > id2 ? 1 : 0;
                        }
                        return i;
                    }
                });
                for (JobStatus job2 : jobs) {
                    pw.print("  JOB #");
                    job2.printUniqueId(pw);
                    pw.print(": ");
                    pw.println(job2.toShortStringExceptUniqueId());
                    if (job2.shouldDump(filterUidFinal)) {
                        job2.dump(pw, "    ", true, nowElapsed);
                        pw.print("    Ready: ");
                        pw.print(isReadyToBeExecutedLocked(job2));
                        pw.print(" (job=");
                        pw.print(job2.isReady());
                        pw.print(" user=");
                        pw.print(ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                        pw.print(" !pending=");
                        pw.print(this.mPendingJobs.contains(job2) ^ 1);
                        pw.print(" !active=");
                        pw.print(isCurrentlyActiveLocked(job2) ^ 1);
                        pw.print(" !backingup=");
                        pw.print(this.mBackingUpUids.indexOfKey(job2.getSourceUid()) < 0);
                        pw.print(" comp=");
                        boolean componentPresent = false;
                        try {
                            componentPresent = AppGlobals.getPackageManager().getServiceInfo(job2.getServiceComponent(), 268435456, job2.getUserId()) != null;
                        } catch (RemoteException e2) {
                        }
                        pw.print(componentPresent);
                        pw.println(")");
                    }
                }
            } else {
                pw.println("  None.");
            }
            for (i = 0; i < this.mControllers.size(); i++) {
                pw.println();
                ((StateController) this.mControllers.get(i)).dumpControllerStateLocked(pw, filterUidFinal);
            }
            pw.println();
            pw.println("Uid priority overrides:");
            for (i = 0; i < this.mUidPriorityOverride.size(); i++) {
                uid = this.mUidPriorityOverride.keyAt(i);
                if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                    pw.print("  ");
                    pw.print(UserHandle.formatUid(uid));
                    pw.print(": ");
                    pw.println(this.mUidPriorityOverride.valueAt(i));
                }
            }
            if (this.mBackingUpUids.size() > 0) {
                pw.println();
                pw.println("Backing up uids:");
                boolean first = true;
                for (i = 0; i < this.mBackingUpUids.size(); i++) {
                    uid = this.mBackingUpUids.keyAt(i);
                    if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                        if (first) {
                            pw.print("  ");
                            first = false;
                        } else {
                            pw.print(", ");
                        }
                        pw.print(UserHandle.formatUid(uid));
                    }
                }
                pw.println();
            }
            pw.println();
            this.mJobPackageTracker.dump(pw, "", filterUidFinal);
            pw.println();
            if (this.mJobPackageTracker.dumpHistory(pw, "", filterUidFinal)) {
                pw.println();
            }
            pw.println("Pending queue:");
            for (i = 0; i < this.mPendingJobs.size(); i++) {
                job2 = (JobStatus) this.mPendingJobs.get(i);
                pw.print("  Pending #");
                pw.print(i);
                pw.print(": ");
                pw.println(job2.toShortString());
                job2.dump(pw, "    ", false, nowElapsed);
                priority = evaluateJobPriorityLocked(job2);
                if (priority != 0) {
                    pw.print("    Evaluated priority: ");
                    pw.println(priority);
                }
                pw.print("    Tag: ");
                pw.println(job2.getTag());
                pw.print("    Enq: ");
                TimeUtils.formatDuration(job2.madePending - nowUptime, pw);
                pw.println();
            }
            pw.println();
            pw.println("Active jobs:");
            for (i = 0; i < this.mActiveServices.size(); i++) {
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                pw.print("  Slot #");
                pw.print(i);
                pw.print(": ");
                job2 = jsc.getRunningJobLocked();
                if (job2 != null) {
                    pw.println(job2.toShortString());
                    pw.print("    Running for: ");
                    TimeUtils.formatDuration(nowElapsed - jsc.getExecutionStartTimeElapsed(), pw);
                    pw.print(", timeout at: ");
                    TimeUtils.formatDuration(jsc.getTimeoutElapsed() - nowElapsed, pw);
                    pw.println();
                    job2.dump(pw, "    ", false, nowElapsed);
                    priority = evaluateJobPriorityLocked(jsc.getRunningJobLocked());
                    if (priority != 0) {
                        pw.print("    Evaluated priority: ");
                        pw.println(priority);
                    }
                    pw.print("    Active at ");
                    TimeUtils.formatDuration(job2.madeActive - nowUptime, pw);
                    pw.print(", pending for ");
                    TimeUtils.formatDuration(job2.madeActive - job2.madePending, pw);
                    pw.println();
                } else if (jsc.mStoppedReason != null) {
                    pw.print("inactive since ");
                    TimeUtils.formatDuration(jsc.mStoppedTime, nowElapsed, pw);
                    pw.print(", stopped because: ");
                    pw.println(jsc.mStoppedReason);
                } else {
                    pw.println("inactive");
                }
            }
            if (filterUid == -1) {
                pw.println();
                pw.print("mReadyToRock=");
                pw.println(this.mReadyToRock);
                pw.print("mReportedActive=");
                pw.println(this.mReportedActive);
                pw.print("mMaxActiveJobs=");
                pw.println(this.mMaxActiveJobs);
            }
        }
        pw.println();
    }
}
