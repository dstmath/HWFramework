package com.android.server.job;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.job.IJobScheduler.Stub;
import android.app.job.JobInfo;
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
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.job.JobStore.JobStatusFunctor;
import com.android.server.job.controllers.AppIdleController;
import com.android.server.job.controllers.BatteryController;
import com.android.server.job.controllers.ConnectivityController;
import com.android.server.job.controllers.ContentObserverController;
import com.android.server.job.controllers.DeviceIdleJobsController;
import com.android.server.job.controllers.IdleController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.StateController;
import com.android.server.job.controllers.TimeController;
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
    final List<JobServiceContext> mActiveServices;
    IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    final Constants mConstants;
    List<StateController> mControllers;
    final JobHandler mHandler;
    final JobPackageTracker mJobPackageTracker;
    final JobSchedulerStub mJobSchedulerStub;
    final JobStore mJobs;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock;
    int mMaxActiveJobs;
    final ArrayList<JobStatus> mPendingJobs;
    PowerManager mPowerManager;
    boolean mReadyToRock;
    boolean mReportedActive;
    int[] mStartedUsers;
    boolean[] mTmpAssignAct;
    JobStatus[] mTmpAssignContextIdToJobMap;
    int[] mTmpAssignPreferredUidForContext;
    private final IUidObserver mUidObserver;
    final SparseIntArray mUidPriorityOverride;

    private final class Constants extends ContentObserver {
        private static final int DEFAULT_BG_CRITICAL_JOB_COUNT = 1;
        private static final int DEFAULT_BG_LOW_JOB_COUNT = 2;
        private static final int DEFAULT_BG_MODERATE_JOB_COUNT = 4;
        private static final int DEFAULT_BG_NORMAL_JOB_COUNT = 6;
        private static final int DEFAULT_FG_JOB_COUNT = 4;
        private static final float DEFAULT_HEAVY_USE_FACTOR = 0.9f;
        private static final int DEFAULT_MIN_CHARGING_COUNT = 1;
        private static final int DEFAULT_MIN_CONNECTIVITY_COUNT = 1;
        private static final int DEFAULT_MIN_CONTENT_COUNT = 1;
        private static final int DEFAULT_MIN_IDLE_COUNT = 1;
        private static final int DEFAULT_MIN_READY_JOBS_COUNT = 1;
        private static final float DEFAULT_MODERATE_USE_FACTOR = 0.5f;
        private static final String KEY_BG_CRITICAL_JOB_COUNT = "bg_critical_job_count";
        private static final String KEY_BG_LOW_JOB_COUNT = "bg_low_job_count";
        private static final String KEY_BG_MODERATE_JOB_COUNT = "bg_moderate_job_count";
        private static final String KEY_BG_NORMAL_JOB_COUNT = "bg_normal_job_count";
        private static final String KEY_FG_JOB_COUNT = "fg_job_count";
        private static final String KEY_HEAVY_USE_FACTOR = "heavy_use_factor";
        private static final String KEY_MIN_CHARGING_COUNT = "min_charging_count";
        private static final String KEY_MIN_CONNECTIVITY_COUNT = "min_connectivity_count";
        private static final String KEY_MIN_CONTENT_COUNT = "min_content_count";
        private static final String KEY_MIN_IDLE_COUNT = "min_idle_count";
        private static final String KEY_MIN_READY_JOBS_COUNT = "min_ready_jobs_count";
        private static final String KEY_MODERATE_USE_FACTOR = "moderate_use_factor";
        int BG_CRITICAL_JOB_COUNT;
        int BG_LOW_JOB_COUNT;
        int BG_MODERATE_JOB_COUNT;
        int BG_NORMAL_JOB_COUNT;
        int FG_JOB_COUNT;
        float HEAVY_USE_FACTOR;
        int MIN_CHARGING_COUNT;
        int MIN_CONNECTIVITY_COUNT;
        int MIN_CONTENT_COUNT;
        int MIN_IDLE_COUNT;
        int MIN_READY_JOBS_COUNT;
        float MODERATE_USE_FACTOR;
        private final KeyValueListParser mParser;
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            this.MIN_IDLE_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.MIN_CHARGING_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.MIN_CONNECTIVITY_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.MIN_CONTENT_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.MIN_READY_JOBS_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.HEAVY_USE_FACTOR = DEFAULT_HEAVY_USE_FACTOR;
            this.MODERATE_USE_FACTOR = DEFAULT_MODERATE_USE_FACTOR;
            this.FG_JOB_COUNT = DEFAULT_FG_JOB_COUNT;
            this.BG_NORMAL_JOB_COUNT = DEFAULT_BG_NORMAL_JOB_COUNT;
            this.BG_MODERATE_JOB_COUNT = DEFAULT_FG_JOB_COUNT;
            this.BG_LOW_JOB_COUNT = DEFAULT_BG_LOW_JOB_COUNT;
            this.BG_CRITICAL_JOB_COUNT = DEFAULT_MIN_READY_JOBS_COUNT;
            this.mParser = new KeyValueListParser(',');
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("job_scheduler_constants"), JobSchedulerService.DEBUG, this);
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
                this.MIN_IDLE_COUNT = this.mParser.getInt(KEY_MIN_IDLE_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                this.MIN_CHARGING_COUNT = this.mParser.getInt(KEY_MIN_CHARGING_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                this.MIN_CONNECTIVITY_COUNT = this.mParser.getInt(KEY_MIN_CONNECTIVITY_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                this.MIN_CONTENT_COUNT = this.mParser.getInt(KEY_MIN_CONTENT_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                this.MIN_READY_JOBS_COUNT = this.mParser.getInt(KEY_MIN_READY_JOBS_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                this.HEAVY_USE_FACTOR = this.mParser.getFloat(KEY_HEAVY_USE_FACTOR, DEFAULT_HEAVY_USE_FACTOR);
                this.MODERATE_USE_FACTOR = this.mParser.getFloat(KEY_MODERATE_USE_FACTOR, DEFAULT_MODERATE_USE_FACTOR);
                this.FG_JOB_COUNT = this.mParser.getInt(KEY_FG_JOB_COUNT, DEFAULT_FG_JOB_COUNT);
                this.BG_NORMAL_JOB_COUNT = this.mParser.getInt(KEY_BG_NORMAL_JOB_COUNT, DEFAULT_BG_NORMAL_JOB_COUNT);
                if (this.FG_JOB_COUNT + this.BG_NORMAL_JOB_COUNT > JobSchedulerService.MAX_JOB_CONTEXTS_COUNT) {
                    this.BG_NORMAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_MODERATE_JOB_COUNT = this.mParser.getInt(KEY_BG_MODERATE_JOB_COUNT, DEFAULT_FG_JOB_COUNT);
                if (this.FG_JOB_COUNT + this.BG_MODERATE_JOB_COUNT > JobSchedulerService.MAX_JOB_CONTEXTS_COUNT) {
                    this.BG_MODERATE_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_LOW_JOB_COUNT = this.mParser.getInt(KEY_BG_LOW_JOB_COUNT, DEFAULT_BG_LOW_JOB_COUNT);
                if (this.FG_JOB_COUNT + this.BG_LOW_JOB_COUNT > JobSchedulerService.MAX_JOB_CONTEXTS_COUNT) {
                    this.BG_LOW_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
                this.BG_CRITICAL_JOB_COUNT = this.mParser.getInt(KEY_BG_CRITICAL_JOB_COUNT, DEFAULT_MIN_READY_JOBS_COUNT);
                if (this.FG_JOB_COUNT + this.BG_CRITICAL_JOB_COUNT > JobSchedulerService.MAX_JOB_CONTEXTS_COUNT) {
                    this.BG_CRITICAL_JOB_COUNT = 16 - this.FG_JOB_COUNT;
                }
            }
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
        }
    }

    private class JobHandler extends Handler {
        private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor;
        private final ReadyJobQueueFunctor mReadyQueueFunctor;

        class MaybeReadyJobQueueFunctor implements JobStatusFunctor {
            int backoffCount;
            int chargingCount;
            int connectivityCount;
            int contentCount;
            int idleCount;
            List<JobStatus> runnableJobs;

            public MaybeReadyJobQueueFunctor() {
                reset();
            }

            public void process(JobStatus job) {
                if (JobHandler.this.isReadyToBeExecutedLocked(job)) {
                    try {
                        if (ActivityManagerNative.getDefault().getAppStartMode(job.getUid(), job.getJob().getService().getPackageName()) == JobSchedulerService.MSG_STOP_JOB) {
                            Slog.w(JobSchedulerService.TAG, "Aborting job " + job.getUid() + ":" + job.getJob().toString() + " -- package not allowed to start");
                            JobSchedulerService.this.mHandler.obtainMessage(JobSchedulerService.MSG_STOP_JOB, job).sendToTarget();
                            return;
                        }
                    } catch (RemoteException e) {
                    }
                    if (job.getNumFailures() > 0) {
                        this.backoffCount += JobSchedulerService.MSG_CHECK_JOB;
                    }
                    if (job.hasIdleConstraint()) {
                        this.idleCount += JobSchedulerService.MSG_CHECK_JOB;
                    }
                    if (job.hasConnectivityConstraint() || job.hasUnmeteredConstraint() || job.hasNotRoamingConstraint()) {
                        this.connectivityCount += JobSchedulerService.MSG_CHECK_JOB;
                    }
                    if (job.hasChargingConstraint()) {
                        this.chargingCount += JobSchedulerService.MSG_CHECK_JOB;
                    }
                    if (job.hasContentTriggerConstraint()) {
                        this.contentCount += JobSchedulerService.MSG_CHECK_JOB;
                    }
                    if (this.runnableJobs == null) {
                        this.runnableJobs = new ArrayList();
                    }
                    this.runnableJobs.add(job);
                } else if (JobHandler.this.areJobConstraintsNotSatisfiedLocked(job)) {
                    JobSchedulerService.this.stopJobOnServiceContextLocked(job, JobSchedulerService.MSG_CHECK_JOB);
                }
            }

            public void postProcess() {
                if (this.backoffCount <= 0 && this.idleCount < JobSchedulerService.this.mConstants.MIN_IDLE_COUNT && this.connectivityCount < JobSchedulerService.this.mConstants.MIN_CONNECTIVITY_COUNT && this.chargingCount < JobSchedulerService.this.mConstants.MIN_CHARGING_COUNT && this.contentCount < JobSchedulerService.this.mConstants.MIN_CONTENT_COUNT) {
                    if (this.runnableJobs != null && this.runnableJobs.size() >= JobSchedulerService.this.mConstants.MIN_READY_JOBS_COUNT) {
                    }
                    reset();
                }
                JobSchedulerService.this.noteJobsPending(this.runnableJobs);
                JobSchedulerService.this.mPendingJobs.addAll(this.runnableJobs);
                reset();
            }

            private void reset() {
                this.chargingCount = JobSchedulerService.MSG_JOB_EXPIRED;
                this.idleCount = JobSchedulerService.MSG_JOB_EXPIRED;
                this.backoffCount = JobSchedulerService.MSG_JOB_EXPIRED;
                this.connectivityCount = JobSchedulerService.MSG_JOB_EXPIRED;
                this.contentCount = JobSchedulerService.MSG_JOB_EXPIRED;
                this.runnableJobs = null;
            }
        }

        class ReadyJobQueueFunctor implements JobStatusFunctor {
            ArrayList<JobStatus> newReadyJobs;

            ReadyJobQueueFunctor() {
            }

            public void process(JobStatus job) {
                if (JobHandler.this.isReadyToBeExecutedLocked(job)) {
                    if (this.newReadyJobs == null) {
                        this.newReadyJobs = new ArrayList();
                    }
                    this.newReadyJobs.add(job);
                } else if (JobHandler.this.areJobConstraintsNotSatisfiedLocked(job)) {
                    JobSchedulerService.this.stopJobOnServiceContextLocked(job, JobSchedulerService.MSG_CHECK_JOB);
                }
            }

            public void postProcess() {
                if (this.newReadyJobs != null) {
                    JobSchedulerService.this.noteJobsPending(this.newReadyJobs);
                    JobSchedulerService.this.mPendingJobs.addAll(this.newReadyJobs);
                }
                this.newReadyJobs = null;
            }
        }

        public JobHandler(Looper looper) {
            super(looper);
            this.mReadyQueueFunctor = new ReadyJobQueueFunctor();
            this.mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();
        }

        public void handleMessage(Message message) {
            synchronized (JobSchedulerService.this.mLock) {
                if (JobSchedulerService.this.mReadyToRock) {
                    Object obj;
                    switch (message.what) {
                        case JobSchedulerService.MSG_JOB_EXPIRED /*0*/:
                            obj = JobSchedulerService.this.mLock;
                            synchronized (obj) {
                                break;
                            }
                            JobStatus runNow = message.obj;
                            if (!(runNow == null || JobSchedulerService.this.mPendingJobs.contains(runNow))) {
                                if (JobSchedulerService.this.mJobs.containsJob(runNow)) {
                                    JobSchedulerService.this.mJobPackageTracker.notePending(runNow);
                                    JobSchedulerService.this.mPendingJobs.add(runNow);
                                }
                                break;
                            }
                            queueReadyJobsForExecutionLockedH();
                            break;
                        case JobSchedulerService.MSG_CHECK_JOB /*1*/:
                            obj = JobSchedulerService.this.mLock;
                            synchronized (obj) {
                                break;
                            }
                            if (!JobSchedulerService.this.mReportedActive) {
                                maybeQueueReadyJobsForExecutionLockedH();
                                break;
                            } else {
                                queueReadyJobsForExecutionLockedH();
                                break;
                            }
                        case JobSchedulerService.MSG_STOP_JOB /*2*/:
                            JobSchedulerService.this.cancelJobImpl((JobStatus) message.obj, null);
                            break;
                        case JobSchedulerService.MSG_CHECK_JOB_GREEDY /*3*/:
                            synchronized (JobSchedulerService.this.mLock) {
                                queueReadyJobsForExecutionLockedH();
                                break;
                            }
                            break;
                    }
                    maybeRunPendingJobsH();
                    removeMessages(JobSchedulerService.MSG_CHECK_JOB);
                    return;
                }
            }
        }

        private void queueReadyJobsForExecutionLockedH() {
            JobSchedulerService.this.noteJobsNonpending(JobSchedulerService.this.mPendingJobs);
            JobSchedulerService.this.mPendingJobs.clear();
            JobSchedulerService.this.mJobs.forEachJob(this.mReadyQueueFunctor);
            this.mReadyQueueFunctor.postProcess();
        }

        private void maybeQueueReadyJobsForExecutionLockedH() {
            JobSchedulerService.this.noteJobsNonpending(JobSchedulerService.this.mPendingJobs);
            JobSchedulerService.this.mPendingJobs.clear();
            JobSchedulerService.this.mJobs.forEachJob(this.mMaybeQueueFunctor);
            this.mMaybeQueueFunctor.postProcess();
        }

        private boolean isReadyToBeExecutedLocked(JobStatus job) {
            boolean jobReady = job.isReady();
            boolean jobPending = JobSchedulerService.this.mPendingJobs.contains(job);
            boolean jobActive = JobSchedulerService.this.isCurrentlyActiveLocked(job);
            int userId = job.getUserId();
            boolean userStarted = ArrayUtils.contains(JobSchedulerService.this.mStartedUsers, userId);
            try {
                boolean componentPresent;
                if (AppGlobals.getPackageManager().getServiceInfo(job.getServiceComponent(), 268435456, userId) != null) {
                    componentPresent = JobSchedulerService.ENFORCE_MAX_JOBS;
                } else {
                    componentPresent = JobSchedulerService.DEBUG;
                }
                if (userStarted && componentPresent && jobReady && !jobPending && !jobActive) {
                    return JobSchedulerService.ENFORCE_MAX_JOBS;
                }
                return JobSchedulerService.DEBUG;
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        private boolean areJobConstraintsNotSatisfiedLocked(JobStatus job) {
            return !job.isReady() ? JobSchedulerService.this.isCurrentlyActiveLocked(job) : JobSchedulerService.DEBUG;
        }

        private void maybeRunPendingJobsH() {
            synchronized (JobSchedulerService.this.mLock) {
                JobSchedulerService.this.assignJobsToContextsLocked();
                JobSchedulerService.this.reportActive();
            }
        }
    }

    final class JobSchedulerStub extends Stub {
        private final SparseArray<Boolean> mPersistCache;

        JobSchedulerStub() {
            this.mPersistCache = new SparseArray();
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
            boolean booleanValue;
            synchronized (this.mPersistCache) {
                Boolean cached = (Boolean) this.mPersistCache.get(uid);
                if (cached != null) {
                    booleanValue = cached.booleanValue();
                } else {
                    booleanValue = JobSchedulerService.this.getContext().checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", pid, uid) == 0 ? JobSchedulerService.ENFORCE_MAX_JOBS : JobSchedulerService.DEBUG;
                    this.mPersistCache.put(uid, Boolean.valueOf(booleanValue));
                }
            }
            return booleanValue;
        }

        public int schedule(JobInfo job) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            enforceValidJobRequest(uid, job);
            if (!job.isPersisted() || canPersistJobs(pid, uid)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    int schedule = JobSchedulerService.this.schedule(job, uid);
                    return schedule;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.");
            }
        }

        public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) throws RemoteException {
            int callerUid = Binder.getCallingUid();
            if (packageName == null) {
                throw new NullPointerException("Must specify a package for scheduleAsPackage()");
            } else if (JobSchedulerService.this.getContext().checkCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS") != 0) {
                throw new SecurityException("Caller uid " + callerUid + " not permitted to schedule jobs for other apps");
            } else {
                if ((job.getFlags() & JobSchedulerService.MSG_CHECK_JOB) != 0) {
                    JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", JobSchedulerService.TAG);
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    int scheduleAsPackage = JobSchedulerService.this.scheduleAsPackage(job, callerUid, packageName, userId, tag);
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
                JobSchedulerService.this.cancelJobsForUid(uid, JobSchedulerService.ENFORCE_MAX_JOBS);
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
            JobSchedulerService.this.getContext().enforceCallingOrSelfPermission("android.permission.DUMP", JobSchedulerService.TAG);
            long identityToken = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.dumpInternal(pw, args);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
            new JobSchedulerShellCommand(JobSchedulerService.this).exec(this, in, out, err, args, resultReceiver);
        }
    }

    final class LocalService implements JobSchedulerInternal {

        /* renamed from: com.android.server.job.JobSchedulerService.LocalService.1 */
        class AnonymousClass1 implements JobStatusFunctor {
            final /* synthetic */ List val$pendingJobs;

            AnonymousClass1(List val$pendingJobs) {
                this.val$pendingJobs = val$pendingJobs;
            }

            public void process(JobStatus job) {
                if (job.getJob().isPeriodic() || !JobSchedulerService.this.isCurrentlyActiveLocked(job)) {
                    this.val$pendingJobs.add(job.getJob());
                }
            }
        }

        LocalService() {
        }

        public List<JobInfo> getSystemScheduledPendingJobs() {
            List<JobInfo> pendingJobs;
            synchronized (JobSchedulerService.this.mLock) {
                pendingJobs = new ArrayList();
                JobSchedulerService.this.mJobs.forEachJob(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, new AnonymousClass1(pendingJobs));
            }
            return pendingJobs;
        }
    }

    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            return uri.getSchemeSpecificPart();
        }
        return null;
    }

    public Object getLock() {
        return this.mLock;
    }

    public JobStore getJobStore() {
        return this.mJobs;
    }

    public void onStartUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.appendInt(this.mStartedUsers, userHandle);
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    public void onUnlockUser(int userHandle) {
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    public void onStopUser(int userHandle) {
        this.mStartedUsers = ArrayUtils.removeInt(this.mStartedUsers, userHandle);
    }

    public int schedule(JobInfo job, int uId) {
        return scheduleAsPackage(job, uId, null, -1, null);
    }

    public int scheduleAsPackage(JobInfo job, int uId, String packageName, int userId, String tag) {
        JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
        try {
            if (ActivityManagerNative.getDefault().getAppStartMode(uId, job.getService().getPackageName()) == MSG_STOP_JOB) {
                Slog.w(TAG, "Not scheduling job " + uId + ":" + job.toString() + " -- package not allowed to start");
                return MSG_JOB_EXPIRED;
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mLock) {
            if (packageName == null) {
                if (this.mJobs.countJobsForUid(uId) > MAX_JOBS_PER_APP) {
                    Slog.w(TAG, "Too many jobs for uid " + uId);
                    throw new IllegalStateException("Apps may not schedule more than 100 distinct jobs");
                }
            }
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uId, job.getId());
            if (toCancel != null) {
                cancelJobImpl(toCancel, jobStatus);
            }
            startTrackingJob(jobStatus, toCancel);
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
        return MSG_CHECK_JOB;
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
        }
        for (int i = MSG_JOB_EXPIRED; i < jobsForUser.size(); i += MSG_CHECK_JOB) {
            cancelJobImpl((JobStatus) jobsForUser.get(i), null);
        }
    }

    public void cancelJobsForUid(int uid, boolean forceAll) {
        synchronized (this.mLock) {
            List<JobStatus> jobsForUid = this.mJobs.getJobsByUid(uid);
        }
        for (int i = MSG_JOB_EXPIRED; i < jobsForUid.size(); i += MSG_CHECK_JOB) {
            JobStatus toRemove = (JobStatus) jobsForUid.get(i);
            if (!forceAll) {
                try {
                    if (ActivityManagerNative.getDefault().getAppStartMode(uid, toRemove.getServiceComponent().getPackageName()) != MSG_STOP_JOB) {
                    }
                } catch (RemoteException e) {
                }
            }
            cancelJobImpl(toRemove, null);
        }
    }

    public void cancelJob(int uid, int jobId) {
        synchronized (this.mLock) {
            JobStatus toCancel = this.mJobs.getJobByUidAndJobId(uid, jobId);
        }
        if (toCancel != null) {
            cancelJobImpl(toCancel, null);
        }
    }

    private void cancelJobImpl(JobStatus cancelled, JobStatus incomingJob) {
        stopTrackingJob(cancelled, incomingJob, ENFORCE_MAX_JOBS);
        synchronized (this.mLock) {
            if (this.mPendingJobs.remove(cancelled)) {
                this.mJobPackageTracker.noteNonpending(cancelled);
            }
            stopJobOnServiceContextLocked(cancelled, MSG_JOB_EXPIRED);
            reportActive();
        }
    }

    void updateUidState(int uid, int procState) {
        synchronized (this.mLock) {
            if (procState == MSG_STOP_JOB) {
                this.mUidPriorityOverride.put(uid, 40);
            } else if (procState <= 4) {
                this.mUidPriorityOverride.put(uid, 30);
            } else {
                this.mUidPriorityOverride.delete(uid);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDeviceIdleStateChanged(boolean deviceIdle) {
        synchronized (this.mLock) {
            if (deviceIdle) {
                int i = MSG_JOB_EXPIRED;
                while (true) {
                    if (i >= this.mActiveServices.size()) {
                        break;
                    }
                    JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                    JobStatus executing = jsc.getRunningJob();
                    if (executing != null && (executing.getFlags() & MSG_CHECK_JOB) == 0) {
                        jsc.cancelExecutingJob(4);
                    }
                    i += MSG_CHECK_JOB;
                }
            } else {
                if (!(!this.mReadyToRock || this.mLocalDeviceIdleController == null || this.mReportedActive)) {
                    this.mReportedActive = ENFORCE_MAX_JOBS;
                    this.mLocalDeviceIdleController.setJobsActive(ENFORCE_MAX_JOBS);
                }
                this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
            }
        }
    }

    void reportActive() {
        boolean active = this.mPendingJobs.size() > 0 ? ENFORCE_MAX_JOBS : DEBUG;
        if (this.mPendingJobs.size() <= 0) {
            for (int i = MSG_JOB_EXPIRED; i < this.mActiveServices.size(); i += MSG_CHECK_JOB) {
                JobStatus job = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJob();
                if (job != null && (job.getJob().getFlags() & MSG_CHECK_JOB) == 0 && !job.dozeWhitelisted) {
                    active = ENFORCE_MAX_JOBS;
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
        this.mLock = new Object();
        this.mJobPackageTracker = new JobPackageTracker();
        this.mActiveServices = new ArrayList();
        this.mPendingJobs = new ArrayList();
        this.mStartedUsers = EmptyArray.INT;
        this.mMaxActiveJobs = MSG_CHECK_JOB;
        this.mUidPriorityOverride = new SparseIntArray();
        this.mTmpAssignContextIdToJobMap = new JobStatus[MAX_JOB_CONTEXTS_COUNT];
        this.mTmpAssignAct = new boolean[MAX_JOB_CONTEXTS_COUNT];
        this.mTmpAssignPreferredUidForContext = new int[MAX_JOB_CONTEXTS_COUNT];
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction())) {
                    String pkgName = JobSchedulerService.this.getPackageName(intent);
                    int pkgUid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (pkgName == null || pkgUid == -1) {
                        Slog.w(JobSchedulerService.TAG, "PACKAGE_CHANGED for " + pkgName + " / uid " + pkgUid);
                        return;
                    }
                    String[] changedComponents = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                    if (changedComponents != null) {
                        int length = changedComponents.length;
                        for (int i = JobSchedulerService.MSG_JOB_EXPIRED; i < length; i += JobSchedulerService.MSG_CHECK_JOB) {
                            if (changedComponents[i].equals(pkgName)) {
                                try {
                                    int state = AppGlobals.getPackageManager().getApplicationEnabledSetting(pkgName, UserHandle.getUserId(pkgUid));
                                    if (state == JobSchedulerService.MSG_STOP_JOB || state == JobSchedulerService.MSG_CHECK_JOB_GREEDY) {
                                        JobSchedulerService.this.cancelJobsForUid(pkgUid, JobSchedulerService.ENFORCE_MAX_JOBS);
                                        return;
                                    }
                                    return;
                                } catch (Exception e) {
                                    Slog.e(JobSchedulerService.TAG, "unknown pkg:" + pkgName, e);
                                    return;
                                }
                            }
                        }
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", JobSchedulerService.DEBUG)) {
                        JobSchedulerService.this.cancelJobsForUid(intent.getIntExtra("android.intent.extra.UID", -1), JobSchedulerService.ENFORCE_MAX_JOBS);
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    JobSchedulerService.this.cancelJobsForUser(intent.getIntExtra("android.intent.extra.user_handle", JobSchedulerService.MSG_JOB_EXPIRED));
                }
            }
        };
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                JobSchedulerService.this.updateUidState(uid, procState);
            }

            public void onUidGone(int uid) throws RemoteException {
                JobSchedulerService.this.updateUidState(uid, JobSchedulerService.MAX_JOB_CONTEXTS_COUNT);
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
                JobSchedulerService.this.cancelJobsForUid(uid, JobSchedulerService.DEBUG);
            }
        };
        this.mHandler = new JobHandler(context.getMainLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mJobSchedulerStub = new JobSchedulerStub();
        this.mJobs = JobStore.initAndGet(this);
        this.mControllers = new ArrayList();
        this.mControllers.add(ConnectivityController.get(this));
        this.mControllers.add(TimeController.get(this));
        this.mControllers.add(IdleController.get(this));
        this.mControllers.add(BatteryController.get(this));
        this.mControllers.add(AppIdleController.get(this));
        this.mControllers.add(ContentObserverController.get(this));
        this.mControllers.add(DeviceIdleJobsController.get(this));
    }

    public void onStart() {
        publishLocalService(JobSchedulerInternal.class, new LocalService());
        publishBinderService("jobscheduler", this.mJobSchedulerStub);
    }

    public void onBootPhase(int phase) {
        if (SystemService.PHASE_SYSTEM_SERVICES_READY == phase) {
            this.mConstants.start(getContext().getContentResolver());
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
            getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_REMOVED"), null, null);
            this.mPowerManager = (PowerManager) getContext().getSystemService("power");
            try {
                ActivityManagerNative.getDefault().registerUidObserver(this.mUidObserver, 7);
            } catch (RemoteException e) {
            }
        } else if (phase == NetdResponseCode.InterfaceChange) {
            synchronized (this.mLock) {
                this.mReadyToRock = ENFORCE_MAX_JOBS;
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
                for (int i = MSG_JOB_EXPIRED; i < MAX_JOB_CONTEXTS_COUNT; i += MSG_CHECK_JOB) {
                    this.mActiveServices.add(new JobServiceContext(this, this.mBatteryStats, this.mJobPackageTracker, getContext().getMainLooper()));
                }
                this.mJobs.forEachJob(new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        for (int controller = JobSchedulerService.MSG_JOB_EXPIRED; controller < JobSchedulerService.this.mControllers.size(); controller += JobSchedulerService.MSG_CHECK_JOB) {
                            ((StateController) JobSchedulerService.this.mControllers.get(controller)).maybeStartTrackingJobLocked(job, null);
                        }
                    }
                });
                this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
            }
        }
    }

    private void startTrackingJob(JobStatus jobStatus, JobStatus lastJob) {
        synchronized (this.mLock) {
            boolean update = this.mJobs.add(jobStatus);
            if (this.mReadyToRock) {
                for (int i = MSG_JOB_EXPIRED; i < this.mControllers.size(); i += MSG_CHECK_JOB) {
                    StateController controller = (StateController) this.mControllers.get(i);
                    if (update) {
                        controller.maybeStopTrackingJobLocked(jobStatus, null, ENFORCE_MAX_JOBS);
                    }
                    controller.maybeStartTrackingJobLocked(jobStatus, lastJob);
                }
            }
        }
    }

    private boolean stopTrackingJob(JobStatus jobStatus, JobStatus incomingJob, boolean writeBack) {
        boolean removed;
        synchronized (this.mLock) {
            removed = this.mJobs.remove(jobStatus, writeBack);
            if (removed && this.mReadyToRock) {
                for (int i = MSG_JOB_EXPIRED; i < this.mControllers.size(); i += MSG_CHECK_JOB) {
                    ((StateController) this.mControllers.get(i)).maybeStopTrackingJobLocked(jobStatus, incomingJob, DEBUG);
                }
            }
        }
        return removed;
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason) {
        int i = MSG_JOB_EXPIRED;
        while (i < this.mActiveServices.size()) {
            JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJob();
            if (executing == null || !executing.matches(job.getUid(), job.getJobId())) {
                i += MSG_CHECK_JOB;
            } else {
                jsc.cancelExecutingJob(reason);
                return ENFORCE_MAX_JOBS;
            }
        }
        return DEBUG;
    }

    private boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i = MSG_JOB_EXPIRED; i < this.mActiveServices.size(); i += MSG_CHECK_JOB) {
            JobStatus running = ((JobServiceContext) this.mActiveServices.get(i)).getRunningJob();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return ENFORCE_MAX_JOBS;
            }
        }
        return DEBUG;
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

    private JobStatus getRescheduleJobForFailure(JobStatus failureToReschedule) {
        long delayMillis;
        long elapsedNowMillis = SystemClock.elapsedRealtime();
        JobInfo job = failureToReschedule.getJob();
        long initialBackoffMillis = job.getInitialBackoffMillis();
        int backoffAttempts = failureToReschedule.getNumFailures() + MSG_CHECK_JOB;
        switch (job.getBackoffPolicy()) {
            case MSG_JOB_EXPIRED /*0*/:
                delayMillis = initialBackoffMillis * ((long) backoffAttempts);
                break;
            default:
                delayMillis = (long) Math.scalb((float) initialBackoffMillis, backoffAttempts - 1);
                break;
        }
        JobStatus newJob = new JobStatus(failureToReschedule, elapsedNowMillis + Math.min(delayMillis, 18000000), JobStatus.NO_LATEST_RUNTIME, backoffAttempts);
        for (int ic = MSG_JOB_EXPIRED; ic < this.mControllers.size(); ic += MSG_CHECK_JOB) {
            ((StateController) this.mControllers.get(ic)).rescheduleForFailure(newJob, failureToReschedule);
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
        return new JobStatus(periodicToReschedule, newLatestRuntimeElapsed - periodicToReschedule.getJob().getFlexMillis(), newLatestRuntimeElapsed, MSG_JOB_EXPIRED);
    }

    public void onJobCompleted(JobStatus jobStatus, boolean needsReschedule) {
        if (stopTrackingJob(jobStatus, null, jobStatus.getJob().isPeriodic() ? DEBUG : ENFORCE_MAX_JOBS)) {
            if (needsReschedule) {
                startTrackingJob(getRescheduleJobForFailure(jobStatus), jobStatus);
            } else if (jobStatus.getJob().isPeriodic()) {
                startTrackingJob(getRescheduleJobForPeriodic(jobStatus), jobStatus);
            }
            reportActive();
            this.mHandler.obtainMessage(MSG_CHECK_JOB_GREEDY).sendToTarget();
            return;
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB_GREEDY).sendToTarget();
    }

    public void onControllerStateChanged() {
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    public void onRunJobNow(JobStatus jobStatus) {
        this.mHandler.obtainMessage(MSG_JOB_EXPIRED, jobStatus).sendToTarget();
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
        int override = this.mUidPriorityOverride.get(job.getSourceUid(), MSG_JOB_EXPIRED);
        if (override != 0) {
            return adjustJobPriority(override, job);
        }
        return adjustJobPriority(priority, job);
    }

    private void assignJobsToContextsLocked() {
        int memLevel;
        int i;
        try {
            memLevel = ActivityManagerNative.getDefault().getMemoryTrimLevel();
        } catch (RemoteException e) {
            memLevel = MSG_JOB_EXPIRED;
        }
        switch (memLevel) {
            case MSG_CHECK_JOB /*1*/:
                this.mMaxActiveJobs = this.mConstants.BG_MODERATE_JOB_COUNT;
                break;
            case MSG_STOP_JOB /*2*/:
                this.mMaxActiveJobs = this.mConstants.BG_LOW_JOB_COUNT;
                break;
            case MSG_CHECK_JOB_GREEDY /*3*/:
                this.mMaxActiveJobs = this.mConstants.BG_CRITICAL_JOB_COUNT;
                break;
            default:
                this.mMaxActiveJobs = this.mConstants.BG_NORMAL_JOB_COUNT;
                break;
        }
        JobStatus[] contextIdToJobMap = this.mTmpAssignContextIdToJobMap;
        boolean[] act = this.mTmpAssignAct;
        int[] preferredUidForContext = this.mTmpAssignPreferredUidForContext;
        int numActive = MSG_JOB_EXPIRED;
        int numForeground = MSG_JOB_EXPIRED;
        for (i = MSG_JOB_EXPIRED; i < MAX_JOB_CONTEXTS_COUNT; i += MSG_CHECK_JOB) {
            JobServiceContext js = (JobServiceContext) this.mActiveServices.get(i);
            JobStatus status = js.getRunningJob();
            contextIdToJobMap[i] = status;
            if (status != null) {
                numActive += MSG_CHECK_JOB;
                int i2 = status.lastEvaluatedPriority;
                if (r0 >= 40) {
                    numForeground += MSG_CHECK_JOB;
                }
            }
            act[i] = DEBUG;
            preferredUidForContext[i] = js.getPreferredUid();
        }
        i = MSG_JOB_EXPIRED;
        while (true) {
            if (i < this.mPendingJobs.size()) {
                JobStatus nextPending = (JobStatus) this.mPendingJobs.get(i);
                if (findJobContextIdFromMap(nextPending, contextIdToJobMap) == -1) {
                    int priority = evaluateJobPriorityLocked(nextPending);
                    nextPending.lastEvaluatedPriority = priority;
                    int minPriority = Integer.MAX_VALUE;
                    int minPriorityContextId = -1;
                    for (int j = MSG_JOB_EXPIRED; j < MAX_JOB_CONTEXTS_COUNT; j += MSG_CHECK_JOB) {
                        JobStatus job = contextIdToJobMap[j];
                        int preferredUid = preferredUidForContext[j];
                        if (job == null) {
                            if ((numActive < this.mMaxActiveJobs || (priority >= 40 && numForeground < this.mConstants.FG_JOB_COUNT)) && (preferredUid == nextPending.getUid() || preferredUid == -1)) {
                                minPriorityContextId = j;
                                if (minPriorityContextId != -1) {
                                    contextIdToJobMap[minPriorityContextId] = nextPending;
                                    act[minPriorityContextId] = ENFORCE_MAX_JOBS;
                                    numActive += MSG_CHECK_JOB;
                                    if (priority >= 40) {
                                        numForeground += MSG_CHECK_JOB;
                                    }
                                }
                            }
                        } else {
                            if (job.getUid() == nextPending.getUid() && evaluateJobPriorityLocked(job) < nextPending.lastEvaluatedPriority) {
                                i2 = nextPending.lastEvaluatedPriority;
                                if (minPriority > r0) {
                                    minPriority = nextPending.lastEvaluatedPriority;
                                    minPriorityContextId = j;
                                }
                            }
                        }
                    }
                    if (minPriorityContextId != -1) {
                        contextIdToJobMap[minPriorityContextId] = nextPending;
                        act[minPriorityContextId] = ENFORCE_MAX_JOBS;
                        numActive += MSG_CHECK_JOB;
                        if (priority >= 40) {
                            numForeground += MSG_CHECK_JOB;
                        }
                    }
                }
                i += MSG_CHECK_JOB;
            } else {
                this.mJobPackageTracker.noteConcurrency(numActive, numForeground);
                for (i = MSG_JOB_EXPIRED; i < MAX_JOB_CONTEXTS_COUNT; i += MSG_CHECK_JOB) {
                    boolean preservePreferredUid = DEBUG;
                    if (act[i]) {
                        if (((JobServiceContext) this.mActiveServices.get(i)).getRunningJob() != null) {
                            ((JobServiceContext) this.mActiveServices.get(i)).preemptExecutingJob();
                            preservePreferredUid = ENFORCE_MAX_JOBS;
                        } else {
                            JobStatus pendingJob = contextIdToJobMap[i];
                            int ic = MSG_JOB_EXPIRED;
                            while (true) {
                                if (ic < this.mControllers.size()) {
                                    ((StateController) this.mControllers.get(ic)).prepareForExecutionLocked(pendingJob);
                                    ic += MSG_CHECK_JOB;
                                } else {
                                    if (!((JobServiceContext) this.mActiveServices.get(i)).executeRunnableJob(pendingJob)) {
                                        Slog.d(TAG, "Error executing " + pendingJob);
                                    }
                                    if (this.mPendingJobs.remove(pendingJob)) {
                                        this.mJobPackageTracker.noteNonpending(pendingJob);
                                    }
                                }
                            }
                        }
                    }
                    if (!preservePreferredUid) {
                        ((JobServiceContext) this.mActiveServices.get(i)).clearPreferredUid();
                    }
                }
                return;
            }
        }
    }

    int findJobContextIdFromMap(JobStatus jobStatus, JobStatus[] map) {
        int i = MSG_JOB_EXPIRED;
        while (i < map.length) {
            if (map[i] != null && map[i].matches(jobStatus.getUid(), jobStatus.getJobId())) {
                return i;
            }
            i += MSG_CHECK_JOB;
        }
        return -1;
    }

    int executeRunCommand(String pkgName, int userId, int jobId, boolean force) {
        try {
            int uid = AppGlobals.getPackageManager().getPackageUid(pkgName, MSG_JOB_EXPIRED, userId);
            if (uid < 0) {
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            synchronized (this.mLock) {
                JobStatus js = this.mJobs.getJobByUidAndJobId(uid, jobId);
                if (js == null) {
                    return JobSchedulerShellCommand.CMD_ERR_NO_JOB;
                }
                js.overrideState = force ? MSG_STOP_JOB : MSG_CHECK_JOB;
                if (js.isConstraintsSatisfied()) {
                    this.mHandler.obtainMessage(MSG_CHECK_JOB_GREEDY).sendToTarget();
                    return MSG_JOB_EXPIRED;
                }
                js.overrideState = MSG_JOB_EXPIRED;
                return JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS;
            }
        } catch (RemoteException e) {
        }
    }

    private String printContextIdToJobMap(JobStatus[] map, String initial) {
        StringBuilder s = new StringBuilder(initial + ": ");
        for (int i = MSG_JOB_EXPIRED; i < map.length; i += MSG_CHECK_JOB) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dumpInternal(PrintWriter pw, String[] args) {
        int filterUid = -1;
        if (!ArrayUtils.isEmpty(args)) {
            int length;
            int opti = MSG_JOB_EXPIRED;
            while (true) {
                length = args.length;
                if (opti >= r0) {
                    break;
                }
                String arg = args[opti];
                if (!"-h".equals(arg)) {
                    if (!"-a".equals(arg)) {
                        break;
                    }
                    opti += MSG_CHECK_JOB;
                } else {
                    dumpHelp(pw);
                    return;
                }
            }
            length = args.length;
            if (opti < r0) {
                String pkg = args[opti];
                try {
                    filterUid = getContext().getPackageManager().getPackageUid(pkg, DumpState.DUMP_PREFERRED_XML);
                } catch (NameNotFoundException e) {
                    pw.println("Invalid package: " + pkg);
                    return;
                }
            }
        }
        int filterUidFinal = UserHandle.getAppId(filterUid);
        long now = SystemClock.elapsedRealtime();
        synchronized (this.mLock) {
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
                                i = JobSchedulerService.MSG_CHECK_JOB;
                            }
                            return i;
                        }
                        if (id1 >= id2) {
                            i = id1 > id2 ? JobSchedulerService.MSG_CHECK_JOB : JobSchedulerService.MSG_JOB_EXPIRED;
                        }
                        return i;
                    }
                });
                for (JobStatus job : jobs) {
                    JobStatus job2;
                    pw.print("  JOB #");
                    job2.printUniqueId(pw);
                    pw.print(": ");
                    pw.println(job2.toShortStringExceptUniqueId());
                    if (job2.shouldDump(filterUidFinal)) {
                        job2.dump(pw, "    ", ENFORCE_MAX_JOBS);
                        pw.print("    Ready: ");
                        pw.print(this.mHandler.isReadyToBeExecutedLocked(job2));
                        pw.print(" (job=");
                        pw.print(job2.isReady());
                        pw.print(" pending=");
                        pw.print(this.mPendingJobs.contains(job2));
                        pw.print(" active=");
                        pw.print(isCurrentlyActiveLocked(job2));
                        pw.print(" user=");
                        pw.print(ArrayUtils.contains(this.mStartedUsers, job2.getUserId()));
                        pw.println(")");
                    }
                }
            } else {
                pw.println("  None.");
            }
            int i = MSG_JOB_EXPIRED;
            while (true) {
                if (i >= this.mControllers.size()) {
                    break;
                }
                pw.println();
                ((StateController) this.mControllers.get(i)).dumpControllerStateLocked(pw, filterUidFinal);
                i += MSG_CHECK_JOB;
            }
            pw.println();
            pw.println("Uid priority overrides:");
            i = MSG_JOB_EXPIRED;
            while (true) {
                if (i >= this.mUidPriorityOverride.size()) {
                    break;
                }
                int uid = this.mUidPriorityOverride.keyAt(i);
                if (filterUidFinal == -1 || filterUidFinal == UserHandle.getAppId(uid)) {
                    pw.print("  ");
                    pw.print(UserHandle.formatUid(uid));
                    pw.print(": ");
                    pw.println(this.mUidPriorityOverride.valueAt(i));
                }
                i += MSG_CHECK_JOB;
            }
            pw.println();
            this.mJobPackageTracker.dump(pw, "", filterUidFinal);
            pw.println();
            if (this.mJobPackageTracker.dumpHistory(pw, "", filterUidFinal)) {
                pw.println();
            }
            pw.println("Pending queue:");
            i = MSG_JOB_EXPIRED;
            while (true) {
                if (i >= this.mPendingJobs.size()) {
                    break;
                }
                job2 = (JobStatus) this.mPendingJobs.get(i);
                pw.print("  Pending #");
                pw.print(i);
                pw.print(": ");
                pw.println(job2.toShortString());
                job2.dump(pw, "    ", DEBUG);
                int priority = evaluateJobPriorityLocked(job2);
                if (priority != 0) {
                    pw.print("    Evaluated priority: ");
                    pw.println(priority);
                }
                pw.print("    Tag: ");
                pw.println(job2.getTag());
                i += MSG_CHECK_JOB;
            }
            pw.println();
            pw.println("Active jobs:");
            i = MSG_JOB_EXPIRED;
            while (true) {
                if (i >= this.mActiveServices.size()) {
                    break;
                }
                JobServiceContext jsc = (JobServiceContext) this.mActiveServices.get(i);
                pw.print("  Slot #");
                pw.print(i);
                pw.print(": ");
                if (jsc.getRunningJob() == null) {
                    pw.println("inactive");
                } else {
                    pw.println(jsc.getRunningJob().toShortString());
                    pw.print("    Running for: ");
                    TimeUtils.formatDuration(now - jsc.getExecutionStartTimeElapsed(), pw);
                    pw.print(", timeout at: ");
                    TimeUtils.formatDuration(jsc.getTimeoutElapsed() - now, pw);
                    pw.println();
                    jsc.getRunningJob().dump(pw, "    ", DEBUG);
                    priority = evaluateJobPriorityLocked(jsc.getRunningJob());
                    if (priority != 0) {
                        pw.print("    Evaluated priority: ");
                        pw.println(priority);
                    }
                }
                i += MSG_CHECK_JOB;
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
