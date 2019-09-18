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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int MAX_URIS_REPORTED = 50;
    private static final String TAG = "JobScheduler.ContentObserver";
    private static final int URIS_URGENT_THRESHOLD = 40;
    final Handler mHandler = new Handler(this.mContext.getMainLooper());
    final SparseArray<ArrayMap<JobInfo.TriggerContentUri, ObserverInstance>> mObservers = new SparseArray<>();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet<>();

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
                    boolean andDescendants = true;
                    if (obs == null) {
                        obs = new ObserverInstance(ContentObserverController.this.mHandler, uri, jobStatus.getSourceUserId());
                        observersOfUser.put(uri, obs);
                        andDescendants = (uri.getFlags() & 1) == 0 ? false : andDescendants;
                        if (ContentObserverController.DEBUG) {
                            Slog.v(ContentObserverController.TAG, "New observer " + obs + " for " + uri.getUri() + " andDescendants=" + andDescendants + " sourceUserId=" + sourceUserId);
                        }
                        ContentObserverController.this.mContext.getContentResolver().registerContentObserver(uri.getUri(), andDescendants, obs, sourceUserId);
                    } else if (ContentObserverController.DEBUG) {
                        andDescendants = (uri.getFlags() & 1) == 0 ? false : andDescendants;
                        Slog.v(ContentObserverController.TAG, "Reusing existing observer " + obs + " for " + uri.getUri() + " andDescendants=" + andDescendants);
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

    final class ObserverInstance extends ContentObserver {
        final ArraySet<JobInstance> mJobs = new ArraySet<>();
        final JobInfo.TriggerContentUri mUri;
        final int mUserId;

        public ObserverInstance(Handler handler, JobInfo.TriggerContentUri uri, int userId) {
            super(handler);
            this.mUri = uri;
            this.mUserId = userId;
        }

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

        public void run() {
            this.mInstance.trigger();
        }
    }

    public ContentObserverController(JobSchedulerService service) {
        super(service);
    }

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

    public void rescheduleForFailureLocked(JobStatus newJob, JobStatus failureToReschedule) {
        if (failureToReschedule.hasContentTriggerConstraint() && newJob.hasContentTriggerConstraint()) {
            newJob.changedAuthorities = failureToReschedule.changedAuthorities;
            newJob.changedUris = failureToReschedule.changedUris;
        }
    }

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        IndentingPrintWriter indentingPrintWriter = pw;
        Predicate<JobStatus> predicate2 = predicate;
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = this.mTrackedTasks.valueAt(i);
            if (predicate2.test(js)) {
                indentingPrintWriter.print("#");
                js.printUniqueId(indentingPrintWriter);
                indentingPrintWriter.print(" from ");
                UserHandle.formatUid(indentingPrintWriter, js.getSourceUid());
                pw.println();
            }
        }
        pw.println();
        int N = this.mObservers.size();
        if (N > 0) {
            indentingPrintWriter.println("Observers:");
            pw.increaseIndent();
            int userIdx = 0;
            while (userIdx < N) {
                ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser = this.mObservers.get(this.mObservers.keyAt(userIdx));
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
                    if (shouldDump) {
                        JobInfo.TriggerContentUri trigger = observersOfUser.keyAt(observerIdx);
                        indentingPrintWriter.print(trigger.getUri());
                        indentingPrintWriter.print(" 0x");
                        indentingPrintWriter.print(Integer.toHexString(trigger.getFlags()));
                        indentingPrintWriter.print(" (");
                        indentingPrintWriter.print(System.identityHashCode(obs));
                        indentingPrintWriter.println("):");
                        pw.increaseIndent();
                        indentingPrintWriter.println("Jobs:");
                        pw.increaseIndent();
                        int j2 = 0;
                        while (j2 < M) {
                            JobInstance inst = obs.mJobs.valueAt(j2);
                            indentingPrintWriter.print("#");
                            inst.mJobStatus.printUniqueId(indentingPrintWriter);
                            indentingPrintWriter.print(" from ");
                            UserHandle.formatUid(indentingPrintWriter, inst.mJobStatus.getSourceUid());
                            if (inst.mChangedAuthorities != null) {
                                indentingPrintWriter.println(":");
                                pw.increaseIndent();
                                if (inst.mTriggerPending) {
                                    indentingPrintWriter.print("Trigger pending: update=");
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentUpdateDelay(), indentingPrintWriter);
                                    indentingPrintWriter.print(", max=");
                                    TimeUtils.formatDuration(inst.mJobStatus.getTriggerContentMaxDelay(), indentingPrintWriter);
                                    pw.println();
                                }
                                indentingPrintWriter.println("Changed Authorities:");
                                for (int k = 0; k < inst.mChangedAuthorities.size(); k++) {
                                    indentingPrintWriter.println(inst.mChangedAuthorities.valueAt(k));
                                }
                                if (inst.mChangedUris != null) {
                                    indentingPrintWriter.println("          Changed URIs:");
                                    for (int k2 = 0; k2 < inst.mChangedUris.size(); k2++) {
                                        indentingPrintWriter.println(inst.mChangedUris.valueAt(k2));
                                    }
                                }
                                pw.decreaseIndent();
                            } else {
                                pw.println();
                            }
                            j2++;
                            Predicate<JobStatus> predicate3 = predicate;
                        }
                        pw.decreaseIndent();
                        pw.decreaseIndent();
                    }
                    observerIdx++;
                    predicate2 = predicate;
                }
                userIdx++;
                predicate2 = predicate;
            }
            pw.decreaseIndent();
        }
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        int userId;
        int numbOfObserversPerUser;
        long oToken;
        ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser;
        long mToken;
        long token;
        long oToken2;
        Uri u;
        ContentObserverController contentObserverController = this;
        ProtoOutputStream protoOutputStream = proto;
        Predicate<JobStatus> predicate2 = predicate;
        long token2 = proto.start(fieldId);
        long mToken2 = protoOutputStream.start(1146756268036L);
        for (int i = 0; i < contentObserverController.mTrackedTasks.size(); i++) {
            JobStatus js = contentObserverController.mTrackedTasks.valueAt(i);
            if (predicate2.test(js)) {
                long jsToken = protoOutputStream.start(2246267895809L);
                js.writeToShortProto(protoOutputStream, 1146756268033L);
                protoOutputStream.write(1120986464258L, js.getSourceUid());
                protoOutputStream.end(jsToken);
            }
        }
        int n = contentObserverController.mObservers.size();
        int userIdx = 0;
        while (userIdx < n) {
            int n2 = n;
            long oToken3 = protoOutputStream.start(2246267895810L);
            int userId2 = contentObserverController.mObservers.keyAt(userIdx);
            protoOutputStream.write(1120986464257L, userId2);
            ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> observersOfUser2 = contentObserverController.mObservers.get(userId2);
            int numbOfObserversPerUser2 = observersOfUser2.size();
            int observerIdx = 0;
            while (observerIdx < numbOfObserversPerUser2) {
                ObserverInstance obs = observersOfUser2.valueAt(observerIdx);
                int m = obs.mJobs.size();
                boolean shouldDump = false;
                int j = 0;
                while (true) {
                    int j2 = j;
                    if (j2 >= m) {
                        userId = userId2;
                        numbOfObserversPerUser = numbOfObserversPerUser2;
                        break;
                    }
                    userId = userId2;
                    numbOfObserversPerUser = numbOfObserversPerUser2;
                    if (predicate2.test(obs.mJobs.valueAt(j2).mJobStatus) != 0) {
                        shouldDump = true;
                        break;
                    }
                    j = j2 + 1;
                    userId2 = userId;
                    numbOfObserversPerUser2 = numbOfObserversPerUser;
                }
                if (!shouldDump) {
                    token = token2;
                    mToken = mToken2;
                    oToken = oToken3;
                    observersOfUser = observersOfUser2;
                } else {
                    token = token2;
                    mToken = mToken2;
                    long tToken = protoOutputStream.start(2246267895810L);
                    JobInfo.TriggerContentUri trigger = observersOfUser2.keyAt(observerIdx);
                    Uri u2 = trigger.getUri();
                    if (u2 != null) {
                        protoOutputStream.write(1138166333441L, u2.toString());
                    }
                    observersOfUser = observersOfUser2;
                    protoOutputStream.write(1120986464258L, trigger.getFlags());
                    int j3 = 0;
                    while (j3 < m) {
                        long jToken = protoOutputStream.start(2246267895811L);
                        JobInstance inst = obs.mJobs.valueAt(j3);
                        ObserverInstance obs2 = obs;
                        int m2 = m;
                        inst.mJobStatus.writeToShortProto(protoOutputStream, 1146756268033L);
                        protoOutputStream.write(1120986464258L, inst.mJobStatus.getSourceUid());
                        if (inst.mChangedAuthorities == null) {
                            protoOutputStream.end(jToken);
                            oToken2 = oToken3;
                        } else {
                            if (inst.mTriggerPending) {
                                u = u2;
                                oToken2 = oToken3;
                                protoOutputStream.write(1112396529667L, inst.mJobStatus.getTriggerContentUpdateDelay());
                                protoOutputStream.write(1112396529668L, inst.mJobStatus.getTriggerContentMaxDelay());
                            } else {
                                u = u2;
                                oToken2 = oToken3;
                            }
                            for (int k = 0; k < inst.mChangedAuthorities.size(); k++) {
                                protoOutputStream.write(2237677961221L, inst.mChangedAuthorities.valueAt(k));
                            }
                            if (inst.mChangedUris != null) {
                                int k2 = 0;
                                while (k2 < inst.mChangedUris.size()) {
                                    Uri u3 = inst.mChangedUris.valueAt(k2);
                                    if (u3 != null) {
                                        protoOutputStream.write(2237677961222L, u3.toString());
                                    }
                                    k2++;
                                    u = u3;
                                }
                            }
                            protoOutputStream.end(jToken);
                            u2 = u;
                        }
                        j3++;
                        obs = obs2;
                        m = m2;
                        oToken3 = oToken2;
                    }
                    Uri uri = u2;
                    oToken = oToken3;
                    ObserverInstance observerInstance = obs;
                    int i2 = m;
                    protoOutputStream.end(tToken);
                }
                observerIdx++;
                userId2 = userId;
                numbOfObserversPerUser2 = numbOfObserversPerUser;
                token2 = token;
                mToken2 = mToken;
                observersOfUser2 = observersOfUser;
                oToken3 = oToken;
                predicate2 = predicate;
            }
            long j4 = mToken2;
            int i3 = userId2;
            ArrayMap<JobInfo.TriggerContentUri, ObserverInstance> arrayMap = observersOfUser2;
            int i4 = numbOfObserversPerUser2;
            protoOutputStream.end(oToken3);
            userIdx++;
            n = n2;
            token2 = token2;
            contentObserverController = this;
            predicate2 = predicate;
        }
        int i5 = n;
        protoOutputStream.end(mToken2);
        protoOutputStream.end(token2);
    }
}
