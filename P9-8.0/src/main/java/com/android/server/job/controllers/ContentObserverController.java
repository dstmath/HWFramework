package com.android.server.job.controllers;

import android.app.job.JobInfo.TriggerContentUri;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class ContentObserverController extends StateController {
    private static final boolean DEBUG = false;
    private static final int MAX_URIS_REPORTED = 50;
    private static final String TAG = "JobScheduler.Content";
    private static final int URIS_URGENT_THRESHOLD = 40;
    private static volatile ContentObserverController sController;
    private static final Object sCreationLock = new Object();
    final Handler mHandler;
    final SparseArray<ArrayMap<TriggerContentUri, ObserverInstance>> mObservers = new SparseArray();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet();

    final class JobInstance {
        ArraySet<String> mChangedAuthorities;
        ArraySet<Uri> mChangedUris;
        final Runnable mExecuteRunner;
        final JobStatus mJobStatus;
        final ArrayList<ObserverInstance> mMyObservers = new ArrayList();
        final Runnable mTimeoutRunner;
        boolean mTriggerPending;
        final /* synthetic */ ContentObserverController this$0;

        JobInstance(ContentObserverController this$0, JobStatus jobStatus) {
            this.this$0 = this$0;
            this.mJobStatus = jobStatus;
            this.mExecuteRunner = new TriggerRunnable(this);
            this.mTimeoutRunner = new TriggerRunnable(this);
            TriggerContentUri[] uris = jobStatus.getJob().getTriggerContentUris();
            int sourceUserId = jobStatus.getSourceUserId();
            ArrayMap<TriggerContentUri, ObserverInstance> observersOfUser = (ArrayMap) this$0.mObservers.get(sourceUserId);
            if (observersOfUser == null) {
                observersOfUser = new ArrayMap();
                this$0.mObservers.put(sourceUserId, observersOfUser);
            }
            if (uris != null) {
                for (TriggerContentUri uri : uris) {
                    ObserverInstance obs = (ObserverInstance) observersOfUser.get(uri);
                    if (obs == null) {
                        obs = new ObserverInstance(this$0.mHandler, uri, jobStatus.getSourceUserId());
                        observersOfUser.put(uri, obs);
                        this$0.mContext.getContentResolver().registerContentObserver(uri.getUri(), (uri.getFlags() & 1) != 0, obs, sourceUserId);
                    }
                    obs.mJobs.add(this);
                    this.mMyObservers.add(obs);
                }
            }
        }

        void trigger() {
            boolean reportChange = false;
            synchronized (this.this$0.mLock) {
                if (this.mTriggerPending) {
                    if (this.mJobStatus.setContentTriggerConstraintSatisfied(true)) {
                        reportChange = true;
                    }
                    unscheduleLocked();
                }
            }
            if (reportChange) {
                this.this$0.mStateChangedListener.onControllerStateChanged();
            }
        }

        void scheduleLocked() {
            if (!this.mTriggerPending) {
                this.mTriggerPending = true;
                this.this$0.mHandler.postDelayed(this.mTimeoutRunner, this.mJobStatus.getTriggerContentMaxDelay());
            }
            this.this$0.mHandler.removeCallbacks(this.mExecuteRunner);
            if (this.mChangedUris.size() >= 40) {
                this.this$0.mHandler.post(this.mExecuteRunner);
            } else {
                this.this$0.mHandler.postDelayed(this.mExecuteRunner, this.mJobStatus.getTriggerContentUpdateDelay());
            }
        }

        void unscheduleLocked() {
            if (this.mTriggerPending) {
                this.this$0.mHandler.removeCallbacks(this.mExecuteRunner);
                this.this$0.mHandler.removeCallbacks(this.mTimeoutRunner);
                this.mTriggerPending = false;
            }
        }

        void detachLocked() {
            int N = this.mMyObservers.size();
            for (int i = 0; i < N; i++) {
                ObserverInstance obs = (ObserverInstance) this.mMyObservers.get(i);
                obs.mJobs.remove(this);
                if (obs.mJobs.size() == 0) {
                    this.this$0.mContext.getContentResolver().unregisterContentObserver(obs);
                    ArrayMap<TriggerContentUri, ObserverInstance> observerOfUser = (ArrayMap) this.this$0.mObservers.get(obs.mUserId);
                    if (observerOfUser != null) {
                        observerOfUser.remove(obs.mUri);
                    }
                }
            }
        }
    }

    final class ObserverInstance extends ContentObserver {
        final ArraySet<JobInstance> mJobs = new ArraySet();
        final TriggerContentUri mUri;
        final int mUserId;

        public ObserverInstance(Handler handler, TriggerContentUri uri, int userId) {
            super(handler);
            this.mUri = uri;
            this.mUserId = userId;
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (ContentObserverController.this.mLock) {
                int N = this.mJobs.size();
                for (int i = 0; i < N; i++) {
                    JobInstance inst = (JobInstance) this.mJobs.valueAt(i);
                    if (inst.mChangedUris == null) {
                        inst.mChangedUris = new ArraySet();
                    }
                    if (inst.mChangedUris.size() < 50) {
                        inst.mChangedUris.add(uri);
                    }
                    if (inst.mChangedAuthorities == null) {
                        inst.mChangedAuthorities = new ArraySet();
                    }
                    inst.mChangedAuthorities.add(uri.getAuthority());
                    inst.scheduleLocked();
                }
            }
        }
    }

    static final class TriggerRunnable implements Runnable {
        final JobInstance mInstance;

        TriggerRunnable(JobInstance instance) {
            this.mInstance = instance;
        }

        public void run() {
            this.mInstance.trigger();
        }
    }

    public static ContentObserverController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new ContentObserverController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public static ContentObserverController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new ContentObserverController(stateChangedListener, context, new Object());
    }

    private ContentObserverController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mHandler = new Handler(context.getMainLooper());
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasContentTriggerConstraint()) {
            if (taskStatus.contentObserverJobInstance == null) {
                taskStatus.contentObserverJobInstance = new JobInstance(this, taskStatus);
            }
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setTrackingController(4);
            boolean havePendingUris = false;
            if (taskStatus.contentObserverJobInstance.mChangedAuthorities != null) {
                havePendingUris = true;
            }
            if (taskStatus.changedAuthorities != null) {
                havePendingUris = true;
                if (taskStatus.contentObserverJobInstance.mChangedAuthorities == null) {
                    taskStatus.contentObserverJobInstance.mChangedAuthorities = new ArraySet();
                }
                for (String auth : taskStatus.changedAuthorities) {
                    taskStatus.contentObserverJobInstance.mChangedAuthorities.add(auth);
                }
                if (taskStatus.changedUris != null) {
                    if (taskStatus.contentObserverJobInstance.mChangedUris == null) {
                        taskStatus.contentObserverJobInstance.mChangedUris = new ArraySet();
                    }
                    for (Uri uri : taskStatus.changedUris) {
                        taskStatus.contentObserverJobInstance.mChangedUris.add(uri);
                    }
                }
                taskStatus.changedAuthorities = null;
                taskStatus.changedUris = null;
            }
            taskStatus.changedAuthorities = null;
            taskStatus.changedUris = null;
            taskStatus.setContentTriggerConstraintSatisfied(havePendingUris);
        }
        if (lastJob != null && lastJob.contentObserverJobInstance != null) {
            lastJob.contentObserverJobInstance.detachLocked();
            lastJob.contentObserverJobInstance = null;
        }
    }

    public void prepareForExecutionLocked(JobStatus taskStatus) {
        if (taskStatus.hasContentTriggerConstraint() && taskStatus.contentObserverJobInstance != null) {
            taskStatus.changedUris = taskStatus.contentObserverJobInstance.mChangedUris;
            taskStatus.changedAuthorities = taskStatus.contentObserverJobInstance.mChangedAuthorities;
            taskStatus.contentObserverJobInstance.mChangedUris = null;
            taskStatus.contentObserverJobInstance.mChangedAuthorities = null;
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(4)) {
            this.mTrackedTasks.remove(taskStatus);
            if (taskStatus.contentObserverJobInstance != null) {
                taskStatus.contentObserverJobInstance.unscheduleLocked();
                if (incomingJob == null) {
                    taskStatus.contentObserverJobInstance.detachLocked();
                    taskStatus.contentObserverJobInstance = null;
                } else if (taskStatus.contentObserverJobInstance != null && taskStatus.contentObserverJobInstance.mChangedAuthorities != null) {
                    if (incomingJob.contentObserverJobInstance == null) {
                        incomingJob.contentObserverJobInstance = new JobInstance(this, incomingJob);
                    }
                    incomingJob.contentObserverJobInstance.mChangedAuthorities = taskStatus.contentObserverJobInstance.mChangedAuthorities;
                    incomingJob.contentObserverJobInstance.mChangedUris = taskStatus.contentObserverJobInstance.mChangedUris;
                    taskStatus.contentObserverJobInstance.mChangedAuthorities = null;
                    taskStatus.contentObserverJobInstance.mChangedUris = null;
                }
            }
        }
    }

    public void rescheduleForFailureLocked(JobStatus newJob, JobStatus failureToReschedule) {
        if (failureToReschedule.hasContentTriggerConstraint() && newJob.hasContentTriggerConstraint()) {
            newJob.changedAuthorities = failureToReschedule.changedAuthorities;
            newJob.changedUris = failureToReschedule.changedUris;
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println("Content:");
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedTasks.valueAt(i);
            if (js.shouldDump(filterUid)) {
                pw.print("  #");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.println();
            }
        }
        int N = this.mObservers.size();
        if (N > 0) {
            pw.println("  Observers:");
            for (int userIdx = 0; userIdx < N; userIdx++) {
                ArrayMap<TriggerContentUri, ObserverInstance> observersOfUser = (ArrayMap) this.mObservers.get(this.mObservers.keyAt(userIdx));
                int numbOfObserversPerUser = observersOfUser.size();
                for (int observerIdx = 0; observerIdx < numbOfObserversPerUser; observerIdx++) {
                    int j;
                    ObserverInstance obs = (ObserverInstance) observersOfUser.valueAt(observerIdx);
                    int M = obs.mJobs.size();
                    boolean shouldDump = false;
                    for (j = 0; j < M; j++) {
                        if (((JobInstance) obs.mJobs.valueAt(j)).mJobStatus.shouldDump(filterUid)) {
                            shouldDump = true;
                            break;
                        }
                    }
                    if (shouldDump) {
                        pw.print("    ");
                        TriggerContentUri trigger = (TriggerContentUri) observersOfUser.keyAt(observerIdx);
                        pw.print(trigger.getUri());
                        pw.print(" 0x");
                        pw.print(Integer.toHexString(trigger.getFlags()));
                        pw.print(" (");
                        pw.print(System.identityHashCode(obs));
                        pw.println("):");
                        pw.println("      Jobs:");
                        for (j = 0; j < M; j++) {
                            JobInstance inst = (JobInstance) obs.mJobs.valueAt(j);
                            pw.print("        #");
                            inst.mJobStatus.printUniqueId(pw);
                            pw.print(" from ");
                            UserHandle.formatUid(pw, inst.mJobStatus.getSourceUid());
                            if (inst.mChangedAuthorities != null) {
                                int k;
                                pw.println(":");
                                if (inst.mTriggerPending) {
                                    pw.print("          Trigger pending: update=");
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentUpdateDelay(), pw);
                                    pw.print(", max=");
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentMaxDelay(), pw);
                                    pw.println();
                                }
                                pw.println("          Changed Authorities:");
                                for (k = 0; k < inst.mChangedAuthorities.size(); k++) {
                                    pw.print("          ");
                                    pw.println((String) inst.mChangedAuthorities.valueAt(k));
                                }
                                if (inst.mChangedUris != null) {
                                    pw.println("          Changed URIs:");
                                    for (k = 0; k < inst.mChangedUris.size(); k++) {
                                        pw.print("          ");
                                        pw.println(inst.mChangedUris.valueAt(k));
                                    }
                                }
                            } else {
                                pw.println();
                            }
                        }
                    }
                }
            }
        }
    }
}
