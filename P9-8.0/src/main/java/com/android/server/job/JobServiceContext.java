package com.android.server.job;

import android.app.ActivityManager;
import android.app.job.IJobCallback.Stub;
import android.app.job.IJobService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobWorkItem;
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
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.server.job.controllers.JobStatus;

public final class JobServiceContext implements ServiceConnection {
    private static final boolean DEBUG = true;
    private static final long EXECUTING_TIMESLICE_MILLIS = 600000;
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
    private static final String[] VERB_STRINGS = new String[]{"VERB_BINDING", "VERB_STARTING", "VERB_EXECUTING", "VERB_STOPPING", "VERB_FINISHED"};
    @GuardedBy("mLock")
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
    int mVerb;
    private WakeLock mWakeLock;
    IJobService service;

    final class JobCallback extends Stub {
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

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    synchronized (JobServiceContext.this.mLock) {
                        if (message.obj == JobServiceContext.this.mRunningCallback) {
                            JobServiceContext.this.handleOpTimeoutLocked();
                        } else {
                            JobCallback jc = message.obj;
                            StringBuilder sb = new StringBuilder(128);
                            sb.append("Ignoring timeout of no longer active job");
                            if (jc.mStoppedReason != null) {
                                sb.append(", stopped ");
                                TimeUtils.formatDuration(SystemClock.elapsedRealtime() - jc.mStoppedTime, sb);
                                sb.append(" because: ");
                                sb.append(jc.mStoppedReason);
                            }
                            Slog.w(JobServiceContext.TAG, sb.toString());
                        }
                    }
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                    return;
            }
        }
    }

    JobServiceContext(JobSchedulerService service, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service.getContext(), service.getLock(), batteryStats, tracker, service, looper);
    }

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

    boolean executeRunnableJob(JobStatus job) {
        synchronized (this.mLock) {
            if (this.mAvailable) {
                this.mPreferredUid = -1;
                this.mRunningJob = job;
                this.mRunningCallback = new JobCallback();
                boolean isDeadlineExpired = job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() < SystemClock.elapsedRealtime() : false;
                Uri[] triggeredUris = null;
                if (job.changedUris != null) {
                    triggeredUris = new Uri[job.changedUris.size()];
                    job.changedUris.toArray(triggeredUris);
                }
                String[] triggeredAuthorities = null;
                if (job.changedAuthorities != null) {
                    triggeredAuthorities = new String[job.changedAuthorities.size()];
                    job.changedAuthorities.toArray(triggeredAuthorities);
                }
                JobInfo ji = job.getJob();
                this.mParams = new JobParameters(this.mRunningCallback, job.getJobId(), ji.getExtras(), ji.getTransientExtras(), ji.getClipData(), ji.getClipGrantFlags(), isDeadlineExpired, triggeredUris, triggeredAuthorities);
                this.mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();
                this.mVerb = 0;
                scheduleOpTimeOutLocked();
                Intent intent = new Intent().setComponent(job.getServiceComponent());
                intent.addHwFlags(32);
                if (this.mContext.bindServiceAsUser(intent, this, 5, new UserHandle(job.getUserId()))) {
                    try {
                        this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid());
                    } catch (RemoteException e) {
                    }
                    this.mJobPackageTracker.noteActive(job);
                    this.mAvailable = false;
                    this.mStoppedReason = null;
                    this.mStoppedTime = 0;
                    return true;
                }
                Slog.d(TAG, job.getServiceComponent().getShortClassName() + " unavailable.");
                if ((intent.getHwFlags() & 32) == 0) {
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
            Slog.e(TAG, "Starting new runnable but context is unavailable > Error.");
            return false;
        }
    }

    JobStatus getRunningJobLocked() {
        return this.mRunningJob;
    }

    void cancelExecutingJobLocked(int reason, String debugReason) {
        doCancelLocked(reason, debugReason);
    }

    void preemptExecutingJobLocked() {
        doCancelLocked(2, "cancelled due to preemption");
    }

    int getPreferredUid() {
        return this.mPreferredUid;
    }

    void clearPreferredUid() {
        this.mPreferredUid = -1;
    }

    long getExecutionStartTimeElapsed() {
        return this.mExecutionStartTimeElapsed;
    }

    long getTimeoutElapsed() {
        return this.mTimeoutElapsed;
    }

    boolean timeoutIfExecutingLocked(String pkgName, int userId, boolean matchJobId, int jobId) {
        JobStatus executing = getRunningJobLocked();
        if (executing == null || ((userId != -1 && userId != executing.getUserId()) || ((pkgName != null && !pkgName.equals(executing.getSourcePackageName())) || ((matchJobId && jobId != executing.getJobId()) || this.mVerb != 2)))) {
            return false;
        }
        this.mParams.setStopReason(3);
        sendStopMessageLocked("force timeout from shell");
        return true;
    }

    void doJobFinished(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, "app called jobFinished");
    }

    void doAcknowledgeStopMessage(JobCallback cb, int jobId, boolean reschedule) {
        doCallback(cb, reschedule, null);
    }

    void doAcknowledgeStartMessage(JobCallback cb, int jobId, boolean ongoing) {
        doCallback(cb, ongoing, "finished start");
    }

    JobWorkItem doDequeueWork(JobCallback cb, int jobId) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                if (this.mVerb != 3 && this.mVerb != 4) {
                    JobWorkItem work = this.mRunningJob.dequeueWorkLocked();
                    if (work == null && (this.mRunningJob.hasExecutingWorkLocked() ^ 1) != 0) {
                        doCallbackLocked(false, "last work dequeued");
                    }
                    Binder.restoreCallingIdentity(ident);
                    return work;
                }
            }
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean doCompleteWork(JobCallback cb, int jobId, int workId) {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean completeWorkLocked;
            synchronized (this.mLock) {
                assertCallerLocked(cb);
                completeWorkLocked = this.mRunningJob.completeWorkLocked(ActivityManager.getService(), workId);
            }
            return completeWorkLocked;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mLock) {
            JobStatus runningJob = this.mRunningJob;
            if (runningJob == null || (name.equals(runningJob.getServiceComponent()) ^ 1) != 0) {
                closeAndCleanupJobLocked(true, "connected for different component");
                return;
            }
            this.service = IJobService.Stub.asInterface(service);
            WakeLock wl = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, runningJob.getTag());
            wl.setWorkSource(new WorkSource(runningJob.getSourceUid()));
            wl.setReferenceCounted(false);
            wl.acquire();
            if (this.mWakeLock != null) {
                Slog.w(TAG, "Bound new job " + runningJob + " but live wakelock " + this.mWakeLock + " tag=" + this.mWakeLock.getTag());
                this.mWakeLock.release();
            }
            this.mWakeLock = wl;
            doServiceBoundLocked();
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (this.mLock) {
            closeAndCleanupJobLocked(true, "unexpectedly disconnected");
        }
    }

    private boolean verifyCallerLocked(JobCallback cb) {
        if (this.mRunningCallback == cb) {
            return true;
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
                TimeUtils.formatDuration(SystemClock.elapsedRealtime() - cb.mStoppedTime, sb);
                sb.append(" because: ");
                sb.append(cb.mStoppedReason);
            }
            throw new SecurityException(sb.toString());
        }
    }

    void doServiceBoundLocked() {
        removeOpTimeOutLocked();
        handleServiceBoundLocked();
    }

    void doCallback(JobCallback cb, boolean reschedule, String reason) {
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

    void doCallbackLocked(boolean reschedule, String reason) {
        Slog.d(TAG, "doCallback of : " + this.mRunningJob + " v:" + VERB_STRINGS[this.mVerb]);
        removeOpTimeOutLocked();
        if (this.mVerb == 1) {
            handleStartedLocked(reschedule);
        } else if (this.mVerb == 2 || this.mVerb == 3) {
            handleFinishedLocked(reschedule, reason);
        } else {
            Slog.d(TAG, "Unrecognised callback: " + this.mRunningJob);
        }
    }

    void doCancelLocked(int arg1, String debugReason) {
        if (this.mVerb == 4) {
            Slog.d(TAG, "Trying to process cancel for torn-down context, ignoring.");
            return;
        }
        this.mParams.setStopReason(arg1);
        if (arg1 == 2) {
            int uid;
            if (this.mRunningJob != null) {
                uid = this.mRunningJob.getUid();
            } else {
                uid = -1;
            }
            this.mPreferredUid = uid;
        }
        handleCancelLocked(debugReason);
    }

    private void handleServiceBoundLocked() {
        Slog.d(TAG, "handleServiceBound for " + this.mRunningJob.toShortString());
        if (this.mVerb != 0) {
            Slog.e(TAG, "Sending onStartJob for a job that isn't pending. " + VERB_STRINGS[this.mVerb]);
            closeAndCleanupJobLocked(false, "started job not pending");
        } else if (this.mCancelled) {
            Slog.d(TAG, "Job cancelled while waiting for bind to complete. " + this.mRunningJob);
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

    private void handleStartedLocked(boolean workOngoing) {
        switch (this.mVerb) {
            case 1:
                this.mVerb = 2;
                if (!workOngoing) {
                    handleFinishedLocked(false, "onStartJob returned false");
                    return;
                } else if (this.mCancelled) {
                    Slog.d(TAG, "Job cancelled while waiting for onStartJob to complete.");
                    handleCancelLocked(null);
                    return;
                } else {
                    scheduleOpTimeOutLocked();
                    return;
                }
            default:
                Slog.e(TAG, "Handling started job but job wasn't starting! Was " + VERB_STRINGS[this.mVerb] + ".");
                return;
        }
    }

    private void handleFinishedLocked(boolean reschedule, String reason) {
        switch (this.mVerb) {
            case 2:
            case 3:
                closeAndCleanupJobLocked(reschedule, reason);
                return;
            default:
                Slog.e(TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + VERB_STRINGS[this.mVerb] + ".");
                return;
        }
    }

    private void handleCancelLocked(String reason) {
        switch (this.mVerb) {
            case 0:
            case 1:
                this.mCancelled = true;
                applyStoppedReasonLocked(reason);
                return;
            case 2:
                sendStopMessageLocked(reason);
                return;
            case 3:
                return;
            default:
                Slog.e(TAG, "Cancelling a job without a valid verb: " + this.mVerb);
                return;
        }
    }

    private void handleOpTimeoutLocked() {
        switch (this.mVerb) {
            case 0:
                Slog.w(TAG, "Time-out while trying to bind " + this.mRunningJob.toShortString() + ", dropping.");
                closeAndCleanupJobLocked(false, "timed out while binding");
                return;
            case 1:
                Slog.w(TAG, new StringBuilder().append("No response from client for onStartJob ").append(this.mRunningJob).toString() != null ? this.mRunningJob.toShortString() : "<null>");
                closeAndCleanupJobLocked(false, "timed out while starting");
                return;
            case 2:
                Slog.i(TAG, new StringBuilder().append("Client timed out while executing (no jobFinished received), sending onStop: ").append(this.mRunningJob).toString() != null ? this.mRunningJob.toShortString() : "<null>");
                this.mParams.setStopReason(3);
                sendStopMessageLocked("timeout while executing");
                return;
            case 3:
                Slog.w(TAG, new StringBuilder().append("No response from client for onStopJob ").append(this.mRunningJob).toString() != null ? this.mRunningJob.toShortString() : "<null>");
                closeAndCleanupJobLocked(true, "timed out while stopping");
                return;
            default:
                Slog.e(TAG, new StringBuilder().append("Handling timeout for an invalid job state: ").append(this.mRunningJob).toString() != null ? this.mRunningJob.toShortString() : "<null>, dropping.");
                closeAndCleanupJobLocked(false, "invalid timeout");
                return;
        }
    }

    private void sendStopMessageLocked(String reason) {
        removeOpTimeOutLocked();
        if (this.mVerb != 2) {
            Slog.e(TAG, "Sending onStopJob for a job that isn't started. " + this.mRunningJob);
            closeAndCleanupJobLocked(false, reason);
            return;
        }
        try {
            this.mVerb = 3;
            scheduleOpTimeOutLocked();
            this.service.stopJob(this.mParams);
        } catch (RemoteException e) {
            Slog.e(TAG, "Error sending onStopJob to client.", e);
            closeAndCleanupJobLocked(true, "host crashed when trying to stop");
        }
    }

    private void closeAndCleanupJobLocked(boolean reschedule, String reason) {
        if (this.mVerb != 4) {
            applyStoppedReasonLocked(reason);
            JobStatus completedJob = this.mRunningJob;
            this.mJobPackageTracker.noteInactive(completedJob);
            try {
                if (this.mRunningJob != null) {
                    this.mBatteryStats.noteJobFinish(this.mRunningJob.getBatteryName(), this.mRunningJob.getSourceUid());
                }
            } catch (RemoteException e) {
            }
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            try {
                this.mContext.unbindService(this);
            } catch (Exception e2) {
                Slog.e(TAG, "Service not bind: JobServiceContext");
            }
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
            this.mStoppedTime = SystemClock.elapsedRealtime();
            if (this.mRunningCallback != null) {
                this.mRunningCallback.mStoppedReason = this.mStoppedReason;
                this.mRunningCallback.mStoppedTime = this.mStoppedTime;
            }
        }
    }

    private void scheduleOpTimeOutLocked() {
        long timeoutMillis;
        removeOpTimeOutLocked();
        switch (this.mVerb) {
            case 0:
                timeoutMillis = OP_BIND_TIMEOUT_MILLIS;
                break;
            case 2:
                timeoutMillis = 600000;
                break;
            default:
                timeoutMillis = OP_TIMEOUT_MILLIS;
                break;
        }
        Slog.d(TAG, "Scheduling time out for '" + this.mRunningJob.getServiceComponent().getShortClassName() + "' jId: " + this.mParams.getJobId() + ", in " + (timeoutMillis / 1000) + " s");
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(0, this.mRunningCallback), timeoutMillis);
        this.mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private void removeOpTimeOutLocked() {
        this.mCallbackHandler.removeMessages(0);
    }
}
