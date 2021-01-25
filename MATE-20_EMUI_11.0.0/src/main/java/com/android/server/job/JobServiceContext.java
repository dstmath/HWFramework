package com.android.server.job;

import android.app.ActivityManager;
import android.app.job.IJobCallback;
import android.app.job.IJobService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobWorkItem;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.EventLog;
import android.util.HwLog;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsManager;
import java.lang.annotation.RCUnownedThisRef;

public final class JobServiceContext implements ServiceConnection {
    private static final boolean DEBUG = JobSchedulerService.DEBUG;
    private static final boolean DEBUG_STANDBY = JobSchedulerService.DEBUG_STANDBY;
    public static final long EXECUTING_TIMESLICE_MILLIS = 600000;
    private static final int MSG_TIMEOUT = 0;
    public static final int NO_PREFERRED_UID = -1;
    private static final long OP_BIND_TIMEOUT_MILLIS = 18000;
    private static final long OP_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "JobServiceContext";
    static final int VERB_BINDING = 0;
    static final int VERB_EXECUTING = 2;
    static final int VERB_FINISHED = 4;
    static final int VERB_STARTING = 1;
    static final int VERB_STOPPING = 3;
    private static final String[] VERB_STRINGS = {"VERB_BINDING", "VERB_STARTING", "VERB_EXECUTING", "VERB_STOPPING", "VERB_FINISHED"};
    @GuardedBy({"mLock"})
    private boolean mAvailable;
    private final IBatteryStats mBatteryStats;
    private final Handler mCallbackHandler;
    private boolean mCancelled;
    private final JobCompletedListener mCompletedListener;
    private final Context mContext;
    private long mExecutionStartTimeElapsed;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;
    private JobParameters mParams;
    private int mPreferredUid;
    private JobCallback mRunningCallback;
    private JobStatus mRunningJob;
    public String mStoppedReason;
    public long mStoppedTime;
    private long mTimeoutElapsed;
    @VisibleForTesting
    int mVerb;
    private PowerManager.WakeLock mWakeLock;
    IJobService service;

    /* access modifiers changed from: package-private */
    @RCUnownedThisRef
    public final class JobCallback extends IJobCallback.Stub {
        public String mStoppedReason;
        public long mStoppedTime;

        JobCallback() {
        }

        public void acknowledgeStartMessage(int jobId, boolean ongoing) {
            JobServiceContext.this.doAcknowledgeStartMessage(this, jobId, ongoing);
        }

        public void acknowledgeStopMessage(int jobId, boolean reschedule) {
            JobServiceContext.this.doAcknowledgeStopMessage(this, jobId, reschedule);
        }

        public JobWorkItem dequeueWork(int jobId) {
            return JobServiceContext.this.doDequeueWork(this, jobId);
        }

        public boolean completeWork(int jobId, int workId) {
            return JobServiceContext.this.doCompleteWork(this, jobId, workId);
        }

        public void jobFinished(int jobId, boolean reschedule) {
            JobServiceContext.this.doJobFinished(this, jobId, reschedule);
        }
    }

    JobServiceContext(JobSchedulerService service2, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service2.getContext(), service2.getLock(), batteryStats, tracker, service2, looper);
    }

    @VisibleForTesting
    JobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker, JobCompletedListener completedListener, Looper looper) {
        this.mContext = context;
        this.mLock = lock;
        this.mBatteryStats = batteryStats;
        this.mJobPackageTracker = tracker;
        this.mCallbackHandler = new JobServiceHandler(looper);
        this.mCompletedListener = completedListener;
        this.mAvailable = true;
        this.mVerb = 4;
        this.mPreferredUid = -1;
    }

    /* JADX WARN: Type inference failed for: r7v0, types: [com.android.server.job.JobServiceContext$JobCallback, android.os.IBinder] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean executeRunnableJob(JobStatus job) {
        Uri[] triggeredUris;
        String[] triggeredAuthorities;
        boolean binding;
        synchronized (this.mLock) {
            if (!this.mAvailable) {
                Slog.e(TAG, "Starting new runnable but context is unavailable > Error.");
                return false;
            }
            this.mPreferredUid = -1;
            this.mRunningJob = job;
            this.mRunningCallback = new JobCallback();
            boolean isDeadlineExpired = job.hasDeadlineConstraint() && job.getLatestRunTimeElapsed() < JobSchedulerService.sElapsedRealtimeClock.millis();
            if (job.changedUris != null) {
                Uri[] triggeredUris2 = new Uri[job.changedUris.size()];
                job.changedUris.toArray(triggeredUris2);
                triggeredUris = triggeredUris2;
            } else {
                triggeredUris = null;
            }
            if (job.changedAuthorities != null) {
                String[] triggeredAuthorities2 = new String[job.changedAuthorities.size()];
                job.changedAuthorities.toArray(triggeredAuthorities2);
                triggeredAuthorities = triggeredAuthorities2;
            } else {
                triggeredAuthorities = null;
            }
            JobInfo ji = job.getJob();
            this.mParams = new JobParameters(this.mRunningCallback, job.getJobId(), ji.getExtras(), ji.getTransientExtras(), ji.getClipData(), ji.getClipGrantFlags(), isDeadlineExpired, triggeredUris, triggeredAuthorities, job.network);
            this.mExecutionStartTimeElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
            long whenDeferred = job.getWhenStandbyDeferred();
            if (whenDeferred > 0) {
                long deferral = this.mExecutionStartTimeElapsed - whenDeferred;
                EventLog.writeEvent((int) EventLogTags.JOB_DEFERRED_EXECUTION, deferral);
                if (DEBUG_STANDBY) {
                    StringBuilder sb = new StringBuilder(128);
                    sb.append("Starting job deferred for standby by ");
                    TimeUtils.formatDuration(deferral, sb);
                    sb.append(" ms : ");
                    sb.append(job.toShortString());
                    Slog.v(TAG, sb.toString());
                }
            }
            job.clearPersistedUtcTimes();
            this.mVerb = 0;
            scheduleOpTimeOutLocked();
            Intent intent = new Intent().setComponent(job.getServiceComponent());
            intent.addHwFlags(32);
            try {
                binding = this.mContext.bindServiceAsUser(intent, this, LightsManager.LIGHT_ID_AMBIENTLIGHT, new UserHandle(job.getUserId()));
            } catch (SecurityException e) {
                Slog.w(TAG, "Job service " + job.getServiceComponent().getShortClassName() + " cannot be executed: " + e.getMessage());
                binding = false;
            }
            if (!binding) {
                Slog.e(TAG, job.getServiceComponent().getShortClassName() + " unavailable.");
                if ((intent.getHwFlags() & 32) == 0) {
                    closeAndCleanupJobLocked(false, "failed while binding");
                    return false;
                }
                this.mRunningJob = null;
                this.mRunningCallback = null;
                this.mParams = null;
                this.mExecutionStartTimeElapsed = 0;
                this.mVerb = 4;
                removeOpTimeOutLocked();
                return false;
            }
            this.mJobPackageTracker.noteActive(job);
            try {
                this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid(), job.getStandbyBucket(), job.getJobId());
            } catch (RemoteException e2) {
            }
            String jobPackage = job.getSourcePackageName();
            int jobUserId = job.getSourceUserId();
            ((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class)).setLastJobRunTime(jobPackage, jobUserId, this.mExecutionStartTimeElapsed);
            ((JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class)).noteJobStart(jobPackage, jobUserId);
            this.mAvailable = false;
            this.mStoppedReason = null;
            this.mStoppedTime = 0;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public JobStatus getRunningJobLocked() {
        return this.mRunningJob;
    }

    private String getRunningJobNameLocked() {
        JobStatus jobStatus = this.mRunningJob;
        return jobStatus != null ? jobStatus.toShortString() : "<null>";
    }

    private void logJobTimeOutLocked() {
        JobStatus jobStatus = this.mRunningJob;
        if (jobStatus != null && jobStatus.getServiceComponent() != null) {
            HwLog.dubaie("DUBAI_TAG_JOBSCHEDULER_TIMEOUT", "name=" + this.mRunningJob.getSourcePackageName() + " tag=" + this.mRunningJob.getServiceComponent().getShortClassName());
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void cancelExecutingJobLocked(int reason, String debugReason) {
        doCancelLocked(reason, debugReason);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void preemptExecutingJobLocked() {
        doCancelLocked(2, "cancelled due to preemption");
    }

    /* access modifiers changed from: package-private */
    public int getPreferredUid() {
        return this.mPreferredUid;
    }

    /* access modifiers changed from: package-private */
    public void clearPreferredUid() {
        this.mPreferredUid = -1;
    }

    /* access modifiers changed from: package-private */
    public long getExecutionStartTimeElapsed() {
        return this.mExecutionStartTimeElapsed;
    }

    /* access modifiers changed from: package-private */
    public long getTimeoutElapsed() {
        return this.mTimeoutElapsed;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public boolean timeoutIfExecutingLocked(String pkgName, int userId, boolean matchJobId, int jobId, String reason) {
        JobStatus executing = getRunningJobLocked();
        if (executing == null) {
            return false;
        }
        if (userId != -1 && userId != executing.getUserId()) {
            return false;
        }
        if (pkgName != null && !pkgName.equals(executing.getSourcePackageName())) {
            return false;
        }
        if ((matchJobId && jobId != executing.getJobId()) || this.mVerb != 2) {
            return false;
        }
        this.mParams.setStopReason(3, reason);
        sendStopMessageLocked("force timeout from shell");
        return true;
    }

    /* access modifiers changed from: package-private */
    public void doJobFinished(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, "app called jobFinished");
    }

    /* access modifiers changed from: package-private */
    public void doAcknowledgeStopMessage(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, null);
    }

    /* access modifiers changed from: package-private */
    public void doAcknowledgeStartMessage(JobCallback cb, int jobId, boolean ongoing) {
        doCallback(cb, ongoing, "finished start");
    }

    /* access modifiers changed from: package-private */
    public JobWorkItem doDequeueWork(JobCallback cb, int jobId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                if (this.mVerb != 3) {
                    if (this.mVerb != 4) {
                        JobWorkItem work = this.mRunningJob.dequeueWorkLocked();
                        if (work == null && !this.mRunningJob.hasExecutingWorkLocked()) {
                            doCallbackLocked(false, "last work dequeued");
                        }
                        Binder.restoreCallingIdentity(ident);
                        return work;
                    }
                }
                return null;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean doCompleteWork(JobCallback cb, int jobId, int workId) {
        boolean completeWorkLocked;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                completeWorkLocked = this.mRunningJob.completeWorkLocked(ActivityManager.getService(), workId);
            }
            return completeWorkLocked;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override // android.content.ServiceConnection
    public void onServiceConnected(ComponentName name, IBinder service2) {
        synchronized (this.mLock) {
            JobStatus runningJob = this.mRunningJob;
            if (runningJob != null) {
                if (name.equals(runningJob.getServiceComponent())) {
                    this.service = IJobService.Stub.asInterface(service2);
                    PowerManager.WakeLock wl = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, runningJob.getTag());
                    wl.setWorkSource(deriveWorkSource(runningJob));
                    wl.setReferenceCounted(false);
                    wl.acquire();
                    if (this.mWakeLock != null) {
                        Slog.w(TAG, "Bound new job " + runningJob + " but live wakelock " + this.mWakeLock + " tag=" + this.mWakeLock.getTag());
                        this.mWakeLock.release();
                    }
                    this.mWakeLock = wl;
                    doServiceBoundLocked();
                    return;
                }
            }
            closeAndCleanupJobLocked(true, "connected for different component");
        }
    }

    private WorkSource deriveWorkSource(JobStatus runningJob) {
        int jobUid = runningJob.getSourceUid();
        if (!WorkSource.isChainedBatteryAttributionEnabled(this.mContext)) {
            return new WorkSource(jobUid);
        }
        WorkSource workSource = new WorkSource();
        workSource.createWorkChain().addNode(jobUid, (String) null).addNode(1000, JobSchedulerService.TAG);
        return workSource;
    }

    @Override // android.content.ServiceConnection
    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mLock) {
            closeAndCleanupJobLocked(true, "unexpectedly disconnected");
        }
    }

    private boolean verifyCallerLocked(JobCallback cb) {
        if (this.mRunningCallback == cb) {
            return true;
        }
        if (!DEBUG) {
            return false;
        }
        Slog.d(TAG, "Stale callback received, ignoring.");
        return false;
    }

    private void assertCallerLocked(JobCallback cb) {
        if (!verifyCallerLocked(cb)) {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Caller no longer running");
            if (cb.mStoppedReason != null) {
                sb.append(", last stopped ");
                TimeUtils.formatDuration(JobSchedulerService.sElapsedRealtimeClock.millis() - cb.mStoppedTime, sb);
                sb.append(" because: ");
                sb.append(cb.mStoppedReason);
            }
            throw new SecurityException(sb.toString());
        }
    }

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what != 0) {
                Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                return;
            }
            synchronized (JobServiceContext.this.mLock) {
                if (message.obj == JobServiceContext.this.mRunningCallback) {
                    JobServiceContext.this.handleOpTimeoutLocked();
                } else {
                    JobCallback jc = (JobCallback) message.obj;
                    StringBuilder sb = new StringBuilder(128);
                    sb.append("Ignoring timeout of no longer active job");
                    if (jc.mStoppedReason != null) {
                        sb.append(", stopped ");
                        TimeUtils.formatDuration(JobSchedulerService.sElapsedRealtimeClock.millis() - jc.mStoppedTime, sb);
                        sb.append(" because: ");
                        sb.append(jc.mStoppedReason);
                    }
                    Slog.w(JobServiceContext.TAG, sb.toString());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void doServiceBoundLocked() {
        removeOpTimeOutLocked();
        handleServiceBoundLocked();
    }

    /* access modifiers changed from: package-private */
    public void doCallback(JobCallback cb, boolean reschedule, String reason) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (verifyCallerLocked(cb)) {
                    doCallbackLocked(reschedule, reason);
                    Binder.restoreCallingIdentity(ident);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void doCallbackLocked(boolean reschedule, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "doCallback of : " + this.mRunningJob + " v:" + VERB_STRINGS[this.mVerb]);
        }
        removeOpTimeOutLocked();
        int i = this.mVerb;
        if (i == 1) {
            handleStartedLocked(reschedule);
        } else if (i == 2 || i == 3) {
            handleFinishedLocked(reschedule, reason);
        } else if (DEBUG) {
            Slog.d(TAG, "Unrecognised callback: " + this.mRunningJob);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void doCancelLocked(int arg1, String debugReason) {
        int i;
        if (this.mVerb != 4) {
            this.mParams.setStopReason(arg1, debugReason);
            if (arg1 == 2) {
                JobStatus jobStatus = this.mRunningJob;
                if (jobStatus != null) {
                    i = jobStatus.getUid();
                } else {
                    i = -1;
                }
                this.mPreferredUid = i;
            }
            handleCancelLocked(debugReason);
        } else if (DEBUG) {
            Slog.d(TAG, "Trying to process cancel for torn-down context, ignoring.");
        }
    }

    @GuardedBy({"mLock"})
    private void handleServiceBoundLocked() {
        if (DEBUG) {
            Slog.d(TAG, "handleServiceBound for " + getRunningJobNameLocked());
        }
        if (this.mVerb != 0) {
            Slog.e(TAG, "Sending onStartJob for a job that isn't pending. " + VERB_STRINGS[this.mVerb]);
            closeAndCleanupJobLocked(false, "started job not pending");
        } else if (this.mCancelled) {
            if (DEBUG) {
                Slog.d(TAG, "Job cancelled while waiting for bind to complete. " + this.mRunningJob);
            }
            closeAndCleanupJobLocked(true, "cancelled while waiting for bind");
        } else {
            try {
                this.mVerb = 1;
                scheduleOpTimeOutLocked();
                this.service.startJob(this.mParams);
            } catch (Exception e) {
                Slog.e(TAG, "Error sending onStart message to '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' ", e);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void handleStartedLocked(boolean workOngoing) {
        if (this.mVerb != 1) {
            Slog.e(TAG, "Handling started job but job wasn't starting! Was " + VERB_STRINGS[this.mVerb] + ".");
            return;
        }
        this.mVerb = 2;
        if (!workOngoing) {
            handleFinishedLocked(false, "onStartJob returned false");
        } else if (this.mCancelled) {
            if (DEBUG) {
                Slog.d(TAG, "Job cancelled while waiting for onStartJob to complete.");
            }
            handleCancelLocked(null);
        } else {
            scheduleOpTimeOutLocked();
        }
    }

    @GuardedBy({"mLock"})
    private void handleFinishedLocked(boolean reschedule, String reason) {
        int i = this.mVerb;
        if (i == 2 || i == 3) {
            closeAndCleanupJobLocked(reschedule, reason);
            return;
        }
        Slog.e(TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + VERB_STRINGS[this.mVerb] + ".");
    }

    @GuardedBy({"mLock"})
    private void handleCancelLocked(String reason) {
        if (JobSchedulerService.DEBUG) {
            Slog.d(TAG, "Handling cancel for: " + this.mRunningJob.getJobId() + " " + VERB_STRINGS[this.mVerb]);
        }
        int i = this.mVerb;
        if (i == 0 || i == 1) {
            this.mCancelled = true;
            applyStoppedReasonLocked(reason);
        } else if (i == 2) {
            sendStopMessageLocked(reason);
        } else if (i != 3) {
            Slog.e(TAG, "Cancelling a job without a valid verb: " + this.mVerb);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void handleOpTimeoutLocked() {
        int i = this.mVerb;
        if (i == 0) {
            Slog.w(TAG, "Time-out while trying to bind " + getRunningJobNameLocked() + ", dropping.");
            closeAndCleanupJobLocked(false, "timed out while binding");
        } else if (i == 1) {
            Slog.w(TAG, "No response from client for onStartJob " + getRunningJobNameLocked());
            closeAndCleanupJobLocked(false, "timed out while starting");
        } else if (i == 2) {
            Slog.i(TAG, "Client timed out while executing (no jobFinished received), sending onStop: " + getRunningJobNameLocked());
            logJobTimeOutLocked();
            this.mParams.setStopReason(3, "client timed out");
            sendStopMessageLocked("timeout while executing");
        } else if (i != 3) {
            Slog.e(TAG, "Handling timeout for an invalid job state: " + getRunningJobNameLocked() + ", dropping.");
            closeAndCleanupJobLocked(false, "invalid timeout");
        } else {
            Slog.w(TAG, "No response from client for onStopJob " + getRunningJobNameLocked());
            closeAndCleanupJobLocked(true, "timed out while stopping");
        }
    }

    @GuardedBy({"mLock"})
    private void sendStopMessageLocked(String reason) {
        removeOpTimeOutLocked();
        if (this.mVerb != 2) {
            Slog.e(TAG, "Sending onStopJob for a job that isn't started. " + this.mRunningJob);
            closeAndCleanupJobLocked(false, reason);
            return;
        }
        try {
            applyStoppedReasonLocked(reason);
            this.mVerb = 3;
            scheduleOpTimeOutLocked();
            this.service.stopJob(this.mParams);
        } catch (RemoteException e) {
            Slog.e(TAG, "Error sending onStopJob to client.", e);
            closeAndCleanupJobLocked(true, "host crashed when trying to stop");
        }
    }

    @GuardedBy({"mLock"})
    private void closeAndCleanupJobLocked(boolean reschedule, String reason) {
        if (this.mVerb != 4) {
            applyStoppedReasonLocked(reason);
            JobStatus completedJob = this.mRunningJob;
            this.mJobPackageTracker.noteInactive(completedJob, this.mParams.getStopReason(), reason);
            try {
                this.mBatteryStats.noteJobFinish(this.mRunningJob.getBatteryName(), this.mRunningJob.getSourceUid(), this.mParams.getStopReason(), this.mRunningJob.getStandbyBucket(), this.mRunningJob.getJobId());
            } catch (RemoteException e) {
            }
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                wakeLock.release();
            }
            this.mContext.unbindService(this);
            this.mWakeLock = null;
            this.mRunningJob = null;
            this.mRunningCallback = null;
            this.mParams = null;
            this.mVerb = 4;
            this.mCancelled = false;
            this.service = null;
            this.mAvailable = true;
            removeOpTimeOutLocked();
            this.mCompletedListener.onJobCompletedLocked(completedJob, reschedule);
        }
    }

    private void applyStoppedReasonLocked(String reason) {
        if (reason != null && this.mStoppedReason == null) {
            this.mStoppedReason = reason;
            this.mStoppedTime = JobSchedulerService.sElapsedRealtimeClock.millis();
            JobCallback jobCallback = this.mRunningCallback;
            if (jobCallback != null) {
                jobCallback.mStoppedReason = this.mStoppedReason;
                jobCallback.mStoppedTime = this.mStoppedTime;
            }
        }
    }

    private void scheduleOpTimeOutLocked() {
        long timeoutMillis;
        removeOpTimeOutLocked();
        int i = this.mVerb;
        if (i == 0) {
            timeoutMillis = OP_BIND_TIMEOUT_MILLIS;
        } else if (i != 2) {
            timeoutMillis = OP_TIMEOUT_MILLIS;
        } else {
            timeoutMillis = 600000;
        }
        if (DEBUG) {
            Slog.d(TAG, "Scheduling time out for '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' jId: " + this.mParams.getJobId() + ", in " + (timeoutMillis / 1000) + " s");
        }
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(0, this.mRunningCallback), timeoutMillis);
        this.mTimeoutElapsed = JobSchedulerService.sElapsedRealtimeClock.millis() + timeoutMillis;
    }

    private void removeOpTimeOutLocked() {
        this.mCallbackHandler.removeMessages(0);
    }
}
