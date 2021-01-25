package com.android.server.job.controllers;

import android.app.job.JobInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public final class ContentObserverController extends StateController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int MAX_URIS_REPORTED = 50;
    private static final String TAG = "JobScheduler.ContentObserver";
    private static final int URIS_URGENT_THRESHOLD = 40;
    final Handler mHandler = new Handler(this.mContext.getMainLooper());
    final SparseArray<ArrayMap<JobInfo.TriggerContentUri, ObserverInstance>> mObservers = new SparseArray<>();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet<>();

    public ContentObserverController(JobSchedulerService service) {
        super(service);
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasContentTriggerConstraint()) {
            if (taskStatus.contentObserverJobInstance == null) {
                taskStatus.contentObserverJobInstance = new JobInstance(taskStatus);
            }
            if (DEBUG) {
                Slog.i(TAG, "Tracking content-trigger job " + taskStatus);
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
                    taskStatus.contentObserverJobInstance.mChangedAuthorities = new ArraySet<>();
                }
                Iterator<String> it = taskStatus.changedAuthorities.iterator();
                while (it.hasNext()) {
                    taskStatus.contentObserverJobInstance.mChangedAuthorities.add(it.next());
                }
                if (taskStatus.changedUris != null) {
                    if (taskStatus.contentObserverJobInstance.mChangedUris == null) {
                        taskStatus.contentObserverJobInstance.mChangedUris = new ArraySet<>();
                    }
                    Iterator<Uri> it2 = taskStatus.changedUris.iterator();
                    while (it2.hasNext()) {
                        taskStatus.contentObserverJobInstance.mChangedUris.add(it2.next());
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

    @Override // com.android.server.job.controllers.StateController
    public void prepareForExecutionLocked(JobStatus taskStatus) {
        if (taskStatus.hasContentTriggerConstraint() && taskStatus.contentObserverJobInstance != null) {
            taskStatus.changedUris = taskStatus.contentObserverJobInstance.mChangedUris;
            taskStatus.changedAuthorities = taskStatus.contentObserverJobInstance.mChangedAuthorities;
            taskStatus.contentObserverJobInstance.mChangedUris = null;
            taskStatus.contentObserverJobInstance.mChangedAuthorities = null;
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(4)) {
            this.mTrackedTasks.remove(taskStatus);
            if (taskStatus.contentObserverJobInstance != null) {
                taskStatus.contentObserverJobInstance.unscheduleLocked();
                if (incomingJob == null) {
                    taskStatus.contentObserverJobInstance.detachLocked();
                    taskStatus.contentObserverJobInstance = null;
                } else if (!(taskStatus.contentObserverJobInstance == null || taskStatus.contentObserverJobInstance.mChangedAuthorities == null)) {
                    if (incomingJob.contentObserverJobInstance == null) {
                        incomingJob.contentObserverJobInstance = new JobInstance(incomingJob);
                    }
                    incomingJob.contentObserverJobInstance.mChangedAuthorities = taskStatus.contentObserverJobInstance.mChangedAuthorities;
                    incomingJob.contentObserverJobInstance.mChangedUris = taskStatus.contentObserverJobInstance.mChangedUris;
                    taskStatus.contentObserverJobInstance.mChangedAuthorities = null;
                    taskStatus.contentObserverJobInstance.mChangedUris = null;
                }
            }
            if (DEBUG) {
                Slog.i(TAG, "No longer tracking job " + taskStatus);
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void rescheduleForFailureLocked(JobStatus newJob, JobStatus failureToReschedule) {
        if (failureToReschedule.hasContentTriggerConstraint() && newJob.hasContentTriggerConstraint()) {
            newJob.changedAuthorities = failureToReschedule.changedAuthorities;
            newJob.changedUris = failureToReschedule.changedUris;
        }
    }

    /* access modifiers changed from: package-private */
    public final class ObserverInstance extends ContentObserver {
        final ArraySet<JobInstance> mJobs = new ArraySet<>();
        final JobInfo.TriggerContentUri mUri;
        final int mUserId;

        public ObserverInstance(Handler handler, JobInfo.TriggerContentUri uri, int userId) {
            super(handler);
            this.mUri = uri;
            this.mUserId = userId;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (ContentObserverController.DEBUG) {
                Slog.i(ContentObserverController.TAG, "onChange(self=" + selfChange + ") for " + uri + " when mUri=" + this.mUri + " mUserId=" + this.mUserId);
            }
            synchronized (ContentObserverController.this.mLock) {
                int N = this.mJobs.size();
                for (int i = 0; i < N; i++) {
                    JobInstance inst = this.mJobs.valueAt(i);
                    if (inst.mChangedUris == null) {
                        inst.mChangedUris = new ArraySet<>();
                    }
                    if (inst.mChangedUris.size() < 50) {
                        inst.mChangedUris.add(uri);
                    }
                    if (inst.mChangedAuthorities == null) {
                        inst.mChangedAuthorities = new ArraySet<>();
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

        @Override // java.lang.Runnable
        public void run() {
            this.mInstance.trigger();
        }
    }

    final class JobInstance {
        ArraySet<String> mChangedAuthorities;
        ArraySet<Uri> mChangedUris;
        final Runnable mExecuteRunner;
        final JobStatus mJobStatus;
        final ArrayList<ObserverInstance> mMyObservers = new ArrayList<>();
        final Runnable mTimeoutRunner;
        boolean mTriggerPending;

        JobInstance(JobStatus jobStatus) {
            this.mJobStatus = jobStatus;
            this.mExecuteRunner = new TriggerRunnable(this);
            this.mTimeoutRunner = new TriggerRunnable(this);
            JobInfo.TriggerContentUri[] uris = jobStatus.getJob().getTriggerContentUris();
            int sourceUserId = jobStatus.getSourceUserId();
            ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser = ContentObserverController.this.mObservers.get(sourceUserId);
            if (observersOfUser == null) {
                observersOfUser = new ArrayMap<>();
                ContentObserverController.this.mObservers.put(sourceUserId, observersOfUser);
            }
            if (uris != null) {
                for (JobInfo.TriggerContentUri uri : uris) {
                    ObserverInstance obs = observersOfUser.get(uri);
                    if (obs == null) {
                        obs = new ObserverInstance(ContentObserverController.this.mHandler, uri, jobStatus.getSourceUserId());
                        observersOfUser.put(uri, obs);
                        boolean andDescendants = (uri.getFlags() & 1) != 0;
                        if (ContentObserverController.DEBUG) {
                            Slog.v(ContentObserverController.TAG, "New observer " + obs + " for " + uri.getUri() + " andDescendants=" + andDescendants + " sourceUserId=" + sourceUserId);
                        }
                        ContentObserverController.this.mContext.getContentResolver().registerContentObserver(uri.getUri(), andDescendants, obs, sourceUserId);
                    } else if (ContentObserverController.DEBUG) {
                        Slog.v(ContentObserverController.TAG, "Reusing existing observer " + obs + " for " + uri.getUri() + " andDescendants=" + ((uri.getFlags() & 1) == 0 ? false : true));
                    }
                    obs.mJobs.add(this);
                    this.mMyObservers.add(obs);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void trigger() {
            boolean reportChange = false;
            synchronized (ContentObserverController.this.mLock) {
                if (this.mTriggerPending) {
                    if (this.mJobStatus.setContentTriggerConstraintSatisfied(true)) {
                        reportChange = true;
                    }
                    unscheduleLocked();
                }
            }
            if (reportChange) {
                ContentObserverController.this.mStateChangedListener.onControllerStateChanged();
            }
        }

        /* access modifiers changed from: package-private */
        public void scheduleLocked() {
            if (!this.mTriggerPending) {
                this.mTriggerPending = true;
                ContentObserverController.this.mHandler.postDelayed(this.mTimeoutRunner, this.mJobStatus.getTriggerContentMaxDelay());
            }
            ContentObserverController.this.mHandler.removeCallbacks(this.mExecuteRunner);
            if (this.mChangedUris.size() >= 40) {
                ContentObserverController.this.mHandler.post(this.mExecuteRunner);
            } else {
                ContentObserverController.this.mHandler.postDelayed(this.mExecuteRunner, this.mJobStatus.getTriggerContentUpdateDelay());
            }
        }

        /* access modifiers changed from: package-private */
        public void unscheduleLocked() {
            if (this.mTriggerPending) {
                ContentObserverController.this.mHandler.removeCallbacks(this.mExecuteRunner);
                ContentObserverController.this.mHandler.removeCallbacks(this.mTimeoutRunner);
                this.mTriggerPending = false;
            }
        }

        /* access modifiers changed from: package-private */
        public void detachLocked() {
            int N = this.mMyObservers.size();
            for (int i = 0; i < N; i++) {
                ObserverInstance obs = this.mMyObservers.get(i);
                obs.mJobs.remove(this);
                if (obs.mJobs.size() == 0) {
                    if (ContentObserverController.DEBUG) {
                        Slog.i(ContentObserverController.TAG, "Unregistering observer " + obs + " for " + obs.mUri.getUri());
                    }
                    ContentObserverController.this.mContext.getContentResolver().unregisterContentObserver(obs);
                    ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observerOfUser = ContentObserverController.this.mObservers.get(obs.mUserId);
                    if (observerOfUser != null) {
                        observerOfUser.remove(obs.mUri);
                    }
                }
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        int N;
        int N2;
        ContentObserverController contentObserverController = this;
        Predicate<JobStatus> predicate2 = predicate;
        for (int i = 0; i < contentObserverController.mTrackedTasks.size(); i++) {
            JobStatus js = contentObserverController.mTrackedTasks.valueAt(i);
            if (predicate2.test(js)) {
                pw.print("#");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.println();
            }
        }
        pw.println();
        int N3 = contentObserverController.mObservers.size();
        if (N3 > 0) {
            pw.println("Observers:");
            pw.increaseIndent();
            int userIdx = 0;
            while (userIdx < N3) {
                ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser = contentObserverController.mObservers.get(contentObserverController.mObservers.keyAt(userIdx));
                int numbOfObserversPerUser = observersOfUser.size();
                int observerIdx = 0;
                while (observerIdx < numbOfObserversPerUser) {
                    ObserverInstance obs = observersOfUser.valueAt(observerIdx);
                    int M = obs.mJobs.size();
                    boolean shouldDump = false;
                    int j = 0;
                    while (true) {
                        if (j >= M) {
                            break;
                        } else if (predicate2.test(obs.mJobs.valueAt(j).mJobStatus)) {
                            shouldDump = true;
                            break;
                        } else {
                            j++;
                        }
                    }
                    if (!shouldDump) {
                        N = N3;
                    } else {
                        JobInfo.TriggerContentUri trigger = observersOfUser.keyAt(observerIdx);
                        pw.print(trigger.getUri());
                        pw.print(" 0x");
                        pw.print(Integer.toHexString(trigger.getFlags()));
                        pw.print(" (");
                        pw.print(System.identityHashCode(obs));
                        pw.println("):");
                        pw.increaseIndent();
                        pw.println("Jobs:");
                        pw.increaseIndent();
                        int j2 = 0;
                        while (j2 < M) {
                            JobInstance inst = obs.mJobs.valueAt(j2);
                            pw.print("#");
                            inst.mJobStatus.printUniqueId(pw);
                            pw.print(" from ");
                            UserHandle.formatUid(pw, inst.mJobStatus.getSourceUid());
                            if (inst.mChangedAuthorities != null) {
                                pw.println(":");
                                pw.increaseIndent();
                                if (inst.mTriggerPending) {
                                    pw.print("Trigger pending: update=");
                                    N2 = N3;
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentUpdateDelay(), pw);
                                    pw.print(", max=");
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentMaxDelay(), pw);
                                    pw.println();
                                } else {
                                    N2 = N3;
                                }
                                pw.println("Changed Authorities:");
                                for (int k = 0; k < inst.mChangedAuthorities.size(); k++) {
                                    pw.println(inst.mChangedAuthorities.valueAt(k));
                                }
                                if (inst.mChangedUris != null) {
                                    pw.println("          Changed URIs:");
                                    for (int k2 = 0; k2 < inst.mChangedUris.size(); k2++) {
                                        pw.println(inst.mChangedUris.valueAt(k2));
                                    }
                                }
                                pw.decreaseIndent();
                            } else {
                                N2 = N3;
                                pw.println();
                            }
                            j2++;
                            trigger = trigger;
                            N3 = N2;
                        }
                        N = N3;
                        pw.decreaseIndent();
                        pw.decreaseIndent();
                    }
                    observerIdx++;
                    predicate2 = predicate;
                    N3 = N;
                }
                userIdx++;
                contentObserverController = this;
                predicate2 = predicate;
            }
            pw.decreaseIndent();
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        int numbOfObserversPerUser;
        int userId;
        long oToken;
        int userIdx;
        ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser;
        long token;
        long mToken;
        long oToken2;
        int userIdx2;
        ContentObserverController contentObserverController = this;
        Predicate<JobStatus> predicate2 = predicate;
        long token2 = proto.start(fieldId);
        long mToken2 = proto.start(1146756268036L);
        for (int i = 0; i < contentObserverController.mTrackedTasks.size(); i++) {
            JobStatus js = contentObserverController.mTrackedTasks.valueAt(i);
            if (predicate2.test(js)) {
                long jsToken = proto.start(2246267895809L);
                js.writeToShortProto(proto, 1146756268033L);
                proto.write(1120986464258L, js.getSourceUid());
                proto.end(jsToken);
            }
        }
        int n = contentObserverController.mObservers.size();
        int userIdx3 = 0;
        while (userIdx3 < n) {
            long oToken3 = proto.start(2246267895810L);
            int userId2 = contentObserverController.mObservers.keyAt(userIdx3);
            proto.write(1120986464257L, userId2);
            ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser2 = contentObserverController.mObservers.get(userId2);
            int numbOfObserversPerUser2 = observersOfUser2.size();
            int observerIdx = 0;
            while (observerIdx < numbOfObserversPerUser2) {
                ObserverInstance obs = observersOfUser2.valueAt(observerIdx);
                int m = obs.mJobs.size();
                boolean shouldDump = false;
                int j = 0;
                while (true) {
                    if (j >= m) {
                        numbOfObserversPerUser = numbOfObserversPerUser2;
                        userId = userId2;
                        break;
                    }
                    numbOfObserversPerUser = numbOfObserversPerUser2;
                    userId = userId2;
                    if (predicate2.test(obs.mJobs.valueAt(j).mJobStatus)) {
                        shouldDump = true;
                        break;
                    }
                    j++;
                    numbOfObserversPerUser2 = numbOfObserversPerUser;
                    userId2 = userId;
                }
                if (!shouldDump) {
                    token = token2;
                    mToken = mToken2;
                    userIdx = userIdx3;
                    oToken = oToken3;
                    observersOfUser = observersOfUser2;
                } else {
                    token = token2;
                    mToken = mToken2;
                    long tToken = proto.start(2246267895810L);
                    JobInfo.TriggerContentUri trigger = observersOfUser2.keyAt(observerIdx);
                    Uri u = trigger.getUri();
                    if (u != null) {
                        proto.write(1138166333441L, u.toString());
                    }
                    Uri u2 = u;
                    proto.write(1120986464258L, trigger.getFlags());
                    int j2 = 0;
                    while (j2 < m) {
                        long jToken = proto.start(2246267895811L);
                        JobInstance inst = obs.mJobs.valueAt(j2);
                        inst.mJobStatus.writeToShortProto(proto, 1146756268033L);
                        proto.write(1120986464258L, inst.mJobStatus.getSourceUid());
                        if (inst.mChangedAuthorities == null) {
                            proto.end(jToken);
                            userIdx2 = userIdx3;
                            oToken2 = oToken3;
                            u2 = u2;
                        } else {
                            if (inst.mTriggerPending) {
                                userIdx2 = userIdx3;
                                oToken2 = oToken3;
                                proto.write(1112396529667L, inst.mJobStatus.getTriggerContentUpdateDelay());
                                proto.write(1112396529668L, inst.mJobStatus.getTriggerContentMaxDelay());
                            } else {
                                userIdx2 = userIdx3;
                                oToken2 = oToken3;
                            }
                            for (int k = 0; k < inst.mChangedAuthorities.size(); k++) {
                                proto.write(2237677961221L, inst.mChangedAuthorities.valueAt(k));
                            }
                            if (inst.mChangedUris != null) {
                                u2 = u2;
                                for (int k2 = 0; k2 < inst.mChangedUris.size(); k2++) {
                                    u2 = inst.mChangedUris.valueAt(k2);
                                    if (u2 != null) {
                                        proto.write(2237677961222L, u2.toString());
                                    }
                                }
                            } else {
                                u2 = u2;
                            }
                            proto.end(jToken);
                        }
                        j2++;
                        m = m;
                        observersOfUser2 = observersOfUser2;
                        userIdx3 = userIdx2;
                        oToken3 = oToken2;
                    }
                    userIdx = userIdx3;
                    oToken = oToken3;
                    observersOfUser = observersOfUser2;
                    proto.end(tToken);
                }
                observerIdx++;
                predicate2 = predicate;
                mToken2 = mToken;
                numbOfObserversPerUser2 = numbOfObserversPerUser;
                n = n;
                userId2 = userId;
                token2 = token;
                observersOfUser2 = observersOfUser;
                userIdx3 = userIdx;
                oToken3 = oToken;
            }
            proto.end(oToken3);
            userIdx3++;
            contentObserverController = this;
            predicate2 = predicate;
            token2 = token2;
        }
        proto.end(mToken2);
        proto.end(token2);
    }
}
