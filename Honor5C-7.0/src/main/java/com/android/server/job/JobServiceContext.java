package com.android.server.job;

import android.app.job.IJobCallback.Stub;
import android.app.job.IJobService;
import android.app.job.JobParameters;
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
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.server.job.controllers.JobStatus;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobServiceContext extends Stub implements ServiceConnection {
    private static final boolean DEBUG = false;
    private static final long EXECUTING_TIMESLICE_MILLIS = 600000;
    private static final int MSG_CALLBACK = 1;
    private static final int MSG_CANCEL = 3;
    private static final int MSG_SERVICE_BOUND = 2;
    private static final int MSG_SHUTDOWN_EXECUTION = 4;
    private static final int MSG_TIMEOUT = 0;
    public static final int NO_PREFERRED_UID = -1;
    private static final long OP_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "JobServiceContext";
    static final int VERB_BINDING = 0;
    static final int VERB_EXECUTING = 2;
    static final int VERB_FINISHED = 4;
    static final int VERB_STARTING = 1;
    static final int VERB_STOPPING = 3;
    private static final String[] VERB_STRINGS = null;
    private static final int defaultMaxActiveJobsPerService = 0;
    @GuardedBy("mLock")
    private boolean mAvailable;
    private final IBatteryStats mBatteryStats;
    private final Handler mCallbackHandler;
    private AtomicBoolean mCancelled;
    private final JobCompletedListener mCompletedListener;
    private final Context mContext;
    private long mExecutionStartTimeElapsed;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;
    private JobParameters mParams;
    private int mPreferredUid;
    private JobStatus mRunningJob;
    private long mTimeoutElapsed;
    int mVerb;
    private WakeLock mWakeLock;
    IJobService service;

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean workOngoing = true;
            switch (message.what) {
                case JobServiceContext.VERB_BINDING /*0*/:
                    handleOpTimeoutH();
                    break;
                case JobServiceContext.VERB_STARTING /*1*/:
                    JobServiceContext.this.removeOpTimeOut();
                    if (JobServiceContext.this.mVerb != JobServiceContext.VERB_STARTING) {
                        if (JobServiceContext.this.mVerb == JobServiceContext.VERB_EXECUTING || JobServiceContext.this.mVerb == JobServiceContext.VERB_STOPPING) {
                            boolean reschedule;
                            if (message.arg2 == JobServiceContext.VERB_STARTING) {
                                reschedule = true;
                            } else {
                                reschedule = JobServiceContext.DEBUG;
                            }
                            handleFinishedH(reschedule);
                            break;
                        }
                    }
                    if (message.arg2 != JobServiceContext.VERB_STARTING) {
                        workOngoing = JobServiceContext.DEBUG;
                    }
                    handleStartedH(workOngoing);
                    break;
                case JobServiceContext.VERB_EXECUTING /*2*/:
                    JobServiceContext.this.removeOpTimeOut();
                    handleServiceBoundH();
                    break;
                case JobServiceContext.VERB_STOPPING /*3*/:
                    if (JobServiceContext.this.mVerb != JobServiceContext.VERB_FINISHED) {
                        JobServiceContext.this.mParams.setStopReason(message.arg1);
                        if (message.arg1 == JobServiceContext.VERB_EXECUTING) {
                            int uid;
                            JobServiceContext jobServiceContext = JobServiceContext.this;
                            if (JobServiceContext.this.mRunningJob != null) {
                                uid = JobServiceContext.this.mRunningJob.getUid();
                            } else {
                                uid = JobServiceContext.NO_PREFERRED_UID;
                            }
                            jobServiceContext.mPreferredUid = uid;
                        }
                        handleCancelH();
                        break;
                    }
                case JobServiceContext.VERB_FINISHED /*4*/:
                    closeAndCleanupJobH(true);
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                    break;
            }
        }

        private void handleServiceBoundH() {
            if (JobServiceContext.this.mVerb != 0) {
                Slog.e(JobServiceContext.TAG, "Sending onStartJob for a job that isn't pending. " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
                closeAndCleanupJobH(JobServiceContext.DEBUG);
            } else if (JobServiceContext.this.mCancelled.get()) {
                closeAndCleanupJobH(true);
            } else {
                try {
                    JobServiceContext.this.mVerb = JobServiceContext.VERB_STARTING;
                    JobServiceContext.this.scheduleOpTimeOut();
                    JobServiceContext.this.service.startJob(JobServiceContext.this.mParams);
                } catch (RemoteException e) {
                    Slog.e(JobServiceContext.TAG, "Error sending onStart message to '" + JobServiceContext.this.mRunningJob.getServiceComponent().getShortClassName() + "' ", e);
                }
            }
        }

        private void handleStartedH(boolean workOngoing) {
            switch (JobServiceContext.this.mVerb) {
                case JobServiceContext.VERB_STARTING /*1*/:
                    JobServiceContext.this.mVerb = JobServiceContext.VERB_EXECUTING;
                    if (!workOngoing) {
                        handleFinishedH(JobServiceContext.DEBUG);
                    } else if (JobServiceContext.this.mCancelled.get()) {
                        handleCancelH();
                    } else {
                        JobServiceContext.this.scheduleOpTimeOut();
                    }
                default:
                    Slog.e(JobServiceContext.TAG, "Handling started job but job wasn't starting! Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
            }
        }

        private void handleFinishedH(boolean reschedule) {
            switch (JobServiceContext.this.mVerb) {
                case JobServiceContext.VERB_EXECUTING /*2*/:
                case JobServiceContext.VERB_STOPPING /*3*/:
                    closeAndCleanupJobH(reschedule);
                default:
                    Slog.e(JobServiceContext.TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
            }
        }

        private void handleCancelH() {
            switch (JobServiceContext.this.mVerb) {
                case JobServiceContext.VERB_BINDING /*0*/:
                case JobServiceContext.VERB_STARTING /*1*/:
                    JobServiceContext.this.mCancelled.set(true);
                    break;
                case JobServiceContext.VERB_EXECUTING /*2*/:
                    if (!hasMessages(JobServiceContext.VERB_STARTING)) {
                        sendStopMessageH();
                        break;
                    }
                case JobServiceContext.VERB_STOPPING /*3*/:
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Cancelling a job without a valid verb: " + JobServiceContext.this.mVerb);
                    break;
            }
        }

        private void handleOpTimeoutH() {
            switch (JobServiceContext.this.mVerb) {
                case JobServiceContext.VERB_BINDING /*0*/:
                    Slog.e(JobServiceContext.TAG, "Time-out while trying to bind " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(JobServiceContext.DEBUG);
                case JobServiceContext.VERB_STARTING /*1*/:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStartJob '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(JobServiceContext.DEBUG);
                case JobServiceContext.VERB_EXECUTING /*2*/:
                    Slog.i(JobServiceContext.TAG, "Client timed out while executing (no jobFinished received). sending onStop. " + JobServiceContext.this.mRunningJob.toShortString());
                    JobServiceContext.this.mParams.setStopReason(JobServiceContext.VERB_STOPPING);
                    sendStopMessageH();
                case JobServiceContext.VERB_STOPPING /*3*/:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStopJob, '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(true);
                default:
                    Slog.e(JobServiceContext.TAG, "Handling timeout for an invalid job state: " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(JobServiceContext.DEBUG);
            }
        }

        private void sendStopMessageH() {
            JobServiceContext.this.removeOpTimeOut();
            if (JobServiceContext.this.mVerb != JobServiceContext.VERB_EXECUTING) {
                Slog.e(JobServiceContext.TAG, "Sending onStopJob for a job that isn't started. " + JobServiceContext.this.mRunningJob);
                closeAndCleanupJobH(JobServiceContext.DEBUG);
                return;
            }
            try {
                JobServiceContext.this.mVerb = JobServiceContext.VERB_STOPPING;
                JobServiceContext.this.scheduleOpTimeOut();
                JobServiceContext.this.service.stopJob(JobServiceContext.this.mParams);
            } catch (RemoteException e) {
                Slog.e(JobServiceContext.TAG, "Error sending onStopJob to client.", e);
                closeAndCleanupJobH(JobServiceContext.DEBUG);
            }
        }

        private void closeAndCleanupJobH(boolean reschedule) {
            synchronized (JobServiceContext.this.mLock) {
                if (JobServiceContext.this.mVerb == JobServiceContext.VERB_FINISHED) {
                    return;
                }
                JobStatus completedJob = JobServiceContext.this.mRunningJob;
                JobServiceContext.this.mJobPackageTracker.noteInactive(completedJob);
                try {
                    if (JobServiceContext.this.mRunningJob != null) {
                        JobServiceContext.this.mBatteryStats.noteJobFinish(JobServiceContext.this.mRunningJob.getBatteryName(), JobServiceContext.this.mRunningJob.getSourceUid());
                    }
                } catch (RemoteException e) {
                }
                if (JobServiceContext.this.mWakeLock != null) {
                    JobServiceContext.this.mWakeLock.release();
                }
                try {
                    JobServiceContext.this.mContext.unbindService(JobServiceContext.this);
                } catch (Exception e2) {
                    Slog.e(JobServiceContext.TAG, "Service not bind: JobServiceContext");
                }
                JobServiceContext.this.mWakeLock = null;
                JobServiceContext.this.mRunningJob = null;
                JobServiceContext.this.mParams = null;
                JobServiceContext.this.mVerb = JobServiceContext.VERB_FINISHED;
                JobServiceContext.this.mCancelled.set(JobServiceContext.DEBUG);
                JobServiceContext.this.service = null;
                JobServiceContext.this.mAvailable = true;
                JobServiceContext.this.removeOpTimeOut();
                removeMessages(JobServiceContext.VERB_STARTING);
                removeMessages(JobServiceContext.VERB_EXECUTING);
                removeMessages(JobServiceContext.VERB_STOPPING);
                removeMessages(JobServiceContext.VERB_FINISHED);
                JobServiceContext.this.mCompletedListener.onJobCompleted(completedJob, reschedule);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.JobServiceContext.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.job.JobServiceContext.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.JobServiceContext.<clinit>():void");
    }

    JobServiceContext(JobSchedulerService service, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service.getContext(), service.getLock(), batteryStats, tracker, service, looper);
    }

    JobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker, JobCompletedListener completedListener, Looper looper) {
        this.mCancelled = new AtomicBoolean();
        this.mContext = context;
        this.mLock = lock;
        this.mBatteryStats = batteryStats;
        this.mJobPackageTracker = tracker;
        this.mCallbackHandler = new JobServiceHandler(looper);
        this.mCompletedListener = completedListener;
        this.mAvailable = true;
        this.mVerb = VERB_FINISHED;
        this.mPreferredUid = NO_PREFERRED_UID;
    }

    boolean executeRunnableJob(JobStatus job) {
        synchronized (this.mLock) {
            if (this.mAvailable) {
                this.mPreferredUid = NO_PREFERRED_UID;
                this.mRunningJob = job;
                boolean isDeadlineExpired = job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() < SystemClock.elapsedRealtime() ? true : DEBUG : DEBUG;
                Uri[] uriArr = null;
                if (job.changedUris != null) {
                    uriArr = new Uri[job.changedUris.size()];
                    job.changedUris.toArray(uriArr);
                }
                String[] strArr = null;
                if (job.changedAuthorities != null) {
                    strArr = new String[job.changedAuthorities.size()];
                    job.changedAuthorities.toArray(strArr);
                }
                this.mParams = new JobParameters(this, job.getJobId(), job.getExtras(), isDeadlineExpired, uriArr, strArr);
                this.mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();
                this.mVerb = VERB_BINDING;
                scheduleOpTimeOut();
                Intent intent = new Intent().setComponent(job.getServiceComponent());
                if ((this.mCompletedListener instanceof JobSchedulerService) && ((JobSchedulerService) this.mCompletedListener).checkShouldFilterIntent(intent, job.getUserId())) {
                    Slog.i(TAG, job.getServiceComponent().getShortClassName() + " binding failed");
                    return DEBUG;
                } else if (this.mContext.bindServiceAsUser(intent, this, 5, new UserHandle(job.getUserId()))) {
                    try {
                        this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid());
                    } catch (RemoteException e) {
                    }
                    this.mJobPackageTracker.noteActive(job);
                    this.mAvailable = DEBUG;
                    return true;
                } else {
                    this.mRunningJob = null;
                    this.mParams = null;
                    this.mExecutionStartTimeElapsed = 0;
                    this.mVerb = VERB_FINISHED;
                    removeOpTimeOut();
                    return DEBUG;
                }
            }
            Slog.e(TAG, "Starting new runnable but context is unavailable > Error.");
            return DEBUG;
        }
    }

    JobStatus getRunningJob() {
        synchronized (this.mLock) {
            JobStatus job = this.mRunningJob;
        }
        if (job == null) {
            return null;
        }
        return new JobStatus(job);
    }

    void cancelExecutingJob(int reason) {
        this.mCallbackHandler.obtainMessage(VERB_STOPPING, reason, VERB_BINDING).sendToTarget();
    }

    void preemptExecutingJob() {
        Message m = this.mCallbackHandler.obtainMessage(VERB_STOPPING);
        m.arg1 = VERB_EXECUTING;
        m.sendToTarget();
    }

    int getPreferredUid() {
        return this.mPreferredUid;
    }

    void clearPreferredUid() {
        this.mPreferredUid = NO_PREFERRED_UID;
    }

    long getExecutionStartTimeElapsed() {
        return this.mExecutionStartTimeElapsed;
    }

    long getTimeoutElapsed() {
        return this.mTimeoutElapsed;
    }

    public void jobFinished(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(VERB_STARTING, jobId, reschedule ? VERB_STARTING : VERB_BINDING).sendToTarget();
        }
    }

    public void acknowledgeStopMessage(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(VERB_STARTING, jobId, reschedule ? VERB_STARTING : VERB_BINDING).sendToTarget();
        }
    }

    public void acknowledgeStartMessage(int jobId, boolean ongoing) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(VERB_STARTING, jobId, ongoing ? VERB_STARTING : VERB_BINDING).sendToTarget();
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mLock) {
            JobStatus runningJob = this.mRunningJob;
        }
        if (runningJob == null || !name.equals(runningJob.getServiceComponent())) {
            this.mCallbackHandler.obtainMessage(VERB_FINISHED).sendToTarget();
            return;
        }
        this.service = IJobService.Stub.asInterface(service);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(VERB_STARTING, runningJob.getTag());
        this.mWakeLock.setWorkSource(new WorkSource(runningJob.getSourceUid()));
        this.mWakeLock.setReferenceCounted(DEBUG);
        this.mWakeLock.acquire();
        this.mCallbackHandler.obtainMessage(VERB_EXECUTING).sendToTarget();
    }

    public void onServiceDisconnected(ComponentName name) {
        this.mCallbackHandler.obtainMessage(VERB_FINISHED).sendToTarget();
    }

    private boolean verifyCallingUid() {
        synchronized (this.mLock) {
            if (this.mRunningJob == null || Binder.getCallingUid() != this.mRunningJob.getUid()) {
                return DEBUG;
            }
            return true;
        }
    }

    private void scheduleOpTimeOut() {
        removeOpTimeOut();
        long timeoutMillis = this.mVerb == VERB_EXECUTING ? EXECUTING_TIMESLICE_MILLIS : OP_TIMEOUT_MILLIS;
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(VERB_BINDING), timeoutMillis);
        this.mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private void removeOpTimeOut() {
        this.mCallbackHandler.removeMessages(VERB_BINDING);
    }
}
