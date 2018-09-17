package com.android.server.job.controllers;

import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobInfo.TriggerContentUri;
import android.app.job.JobWorkItem;
import android.content.ClipData;
import android.content.ComponentName;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.job.GrantedUriPermissions;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public final class JobStatus {
    static final int CONNECTIVITY_MASK = 830472192;
    static final int CONSTRAINTS_OF_INTEREST = -1249902577;
    static final int CONSTRAINT_APP_NOT_IDLE = 134217728;
    static final int CONSTRAINT_BATTERY_NOT_LOW = 2;
    static final int CONSTRAINT_CHARGING = 1;
    static final int CONSTRAINT_CONNECTIVITY = 268435456;
    static final int CONSTRAINT_CONTENT_TRIGGER = 67108864;
    static final int CONSTRAINT_DEADLINE = 1073741824;
    static final int CONSTRAINT_DEVICE_NOT_DOZING = 33554432;
    static final int CONSTRAINT_IDLE = 4;
    static final int CONSTRAINT_METERED = 8388608;
    static final int CONSTRAINT_NOT_ROAMING = 16777216;
    static final int CONSTRAINT_STORAGE_NOT_LOW = 8;
    static final int CONSTRAINT_TIMING_DELAY = Integer.MIN_VALUE;
    static final int CONSTRAINT_UNMETERED = 536870912;
    static final boolean DEBUG_PREPARE = true;
    public static final long DEFAULT_TRIGGER_MAX_DELAY = 120000;
    public static final long DEFAULT_TRIGGER_UPDATE_DELAY = 10000;
    public static final long MIN_TRIGGER_MAX_DELAY = 1000;
    public static final long MIN_TRIGGER_UPDATE_DELAY = 500;
    public static final long NO_EARLIEST_RUNTIME = 0;
    public static final long NO_LATEST_RUNTIME = Long.MAX_VALUE;
    public static final int OVERRIDE_FULL = 2;
    public static final int OVERRIDE_SOFT = 1;
    static final int SOFT_OVERRIDE_CONSTRAINTS = -2147483633;
    static final String TAG = "JobSchedulerService";
    public static final int TRACKING_BATTERY = 1;
    public static final int TRACKING_CONNECTIVITY = 2;
    public static final int TRACKING_CONTENT = 4;
    public static final int TRACKING_IDLE = 8;
    public static final int TRACKING_STORAGE = 16;
    public static final int TRACKING_TIME = 32;
    final String batteryName;
    final int callingUid;
    public ArraySet<String> changedAuthorities;
    public ArraySet<Uri> changedUris;
    JobInstance contentObserverJobInstance;
    public boolean dozeWhitelisted;
    private final long earliestRunTimeElapsedMillis;
    public long enqueueTime;
    public ArrayList<JobWorkItem> executingWork;
    final JobInfo job;
    public int lastEvaluatedPriority;
    private final long latestRunTimeElapsedMillis;
    public long madeActive;
    public long madePending;
    public int nextPendingWorkId;
    private final int numFailures;
    public int overrideState;
    public ArrayList<JobWorkItem> pendingWork;
    private boolean prepared;
    final int requiredConstraints;
    int satisfiedConstraints;
    final String sourcePackageName;
    final String sourceTag;
    final int sourceUid;
    final int sourceUserId;
    final String tag;
    private int trackingControllers;
    private Throwable unpreparedPoint;
    private GrantedUriPermissions uriPerms;

    public int getServiceToken() {
        return this.callingUid;
    }

    private JobStatus(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String tag, int numFailures, long earliestRunTimeElapsedMillis, long latestRunTimeElapsedMillis) {
        String str;
        this.unpreparedPoint = null;
        this.satisfiedConstraints = 0;
        this.nextPendingWorkId = 1;
        this.overrideState = 0;
        this.job = job;
        this.callingUid = callingUid;
        int tempSourceUid = -1;
        if (!(sourceUserId == -1 || sourcePackageName == null)) {
            try {
                tempSourceUid = AppGlobals.getPackageManager().getPackageUid(sourcePackageName, 0, sourceUserId);
            } catch (RemoteException e) {
            }
        }
        if (tempSourceUid == -1) {
            this.sourceUid = callingUid;
            this.sourceUserId = UserHandle.getUserId(callingUid);
            this.sourcePackageName = job.getService().getPackageName();
            this.sourceTag = null;
        } else {
            this.sourceUid = tempSourceUid;
            this.sourceUserId = sourceUserId;
            this.sourcePackageName = sourcePackageName;
            this.sourceTag = tag;
        }
        if (this.sourceTag != null) {
            str = this.sourceTag + ":" + job.getService().getPackageName();
        } else {
            str = job.getService().flattenToShortString();
        }
        this.batteryName = str;
        this.tag = "*job*/" + this.batteryName;
        this.earliestRunTimeElapsedMillis = earliestRunTimeElapsedMillis;
        this.latestRunTimeElapsedMillis = latestRunTimeElapsedMillis;
        this.numFailures = numFailures;
        int requiredConstraints = job.getConstraintFlags();
        switch (job.getNetworkType()) {
            case 0:
                break;
            case 1:
                requiredConstraints |= CONSTRAINT_CONNECTIVITY;
                break;
            case 2:
                requiredConstraints |= CONSTRAINT_UNMETERED;
                break;
            case 3:
                requiredConstraints |= CONSTRAINT_NOT_ROAMING;
                break;
            case 4:
                requiredConstraints |= CONSTRAINT_METERED;
                break;
            default:
                Slog.w(TAG, "Unrecognized networking constraint " + job.getNetworkType());
                break;
        }
        if (earliestRunTimeElapsedMillis != 0) {
            requiredConstraints |= Integer.MIN_VALUE;
        }
        if (latestRunTimeElapsedMillis != NO_LATEST_RUNTIME) {
            requiredConstraints |= CONSTRAINT_DEADLINE;
        }
        if (job.getTriggerContentUris() != null) {
            requiredConstraints |= CONSTRAINT_CONTENT_TRIGGER;
        }
        this.requiredConstraints = requiredConstraints;
    }

    public JobStatus(JobStatus jobStatus) {
        this(jobStatus.getJob(), jobStatus.getUid(), jobStatus.getSourcePackageName(), jobStatus.getSourceUserId(), jobStatus.getSourceTag(), jobStatus.getNumFailures(), jobStatus.getEarliestRunTime(), jobStatus.getLatestRunTimeElapsed());
    }

    public JobStatus(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String sourceTag, long earliestRunTimeElapsedMillis, long latestRunTimeElapsedMillis) {
        this(job, callingUid, sourcePackageName, sourceUserId, sourceTag, 0, earliestRunTimeElapsedMillis, latestRunTimeElapsedMillis);
    }

    public JobStatus(JobStatus rescheduling, long newEarliestRuntimeElapsedMillis, long newLatestRuntimeElapsedMillis, int backoffAttempt) {
        this(rescheduling.job, rescheduling.getUid(), rescheduling.getSourcePackageName(), rescheduling.getSourceUserId(), rescheduling.getSourceTag(), backoffAttempt, newEarliestRuntimeElapsedMillis, newLatestRuntimeElapsedMillis);
    }

    public static JobStatus createFromJobInfo(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String tag) {
        long latestRunTimeElapsedMillis;
        long earliestRunTimeElapsedMillis;
        long elapsedNow = SystemClock.elapsedRealtime();
        if (job.isPeriodic()) {
            latestRunTimeElapsedMillis = elapsedNow + job.getIntervalMillis();
            earliestRunTimeElapsedMillis = latestRunTimeElapsedMillis - job.getFlexMillis();
        } else {
            earliestRunTimeElapsedMillis = job.hasEarlyConstraint() ? elapsedNow + job.getMinLatencyMillis() : 0;
            latestRunTimeElapsedMillis = job.hasLateConstraint() ? elapsedNow + job.getMaxExecutionDelayMillis() : NO_LATEST_RUNTIME;
        }
        return new JobStatus(job, callingUid, sourcePackageName, sourceUserId, tag, 0, earliestRunTimeElapsedMillis, latestRunTimeElapsedMillis);
    }

    public void enqueueWorkLocked(IActivityManager am, JobWorkItem work) {
        if (this.pendingWork == null) {
            this.pendingWork = new ArrayList();
        }
        work.setWorkId(this.nextPendingWorkId);
        this.nextPendingWorkId++;
        if (work.getIntent() != null && GrantedUriPermissions.checkGrantFlags(work.getIntent().getFlags())) {
            work.setGrants(GrantedUriPermissions.createFromIntent(am, work.getIntent(), this.sourceUid, this.sourcePackageName, this.sourceUserId, toShortString()));
        }
        this.pendingWork.add(work);
    }

    public JobWorkItem dequeueWorkLocked() {
        if (this.pendingWork == null || this.pendingWork.size() <= 0) {
            return null;
        }
        JobWorkItem work = (JobWorkItem) this.pendingWork.remove(0);
        if (work != null) {
            if (this.executingWork == null) {
                this.executingWork = new ArrayList();
            }
            this.executingWork.add(work);
            work.bumpDeliveryCount();
        }
        return work;
    }

    public boolean hasWorkLocked() {
        return (this.pendingWork == null || this.pendingWork.size() <= 0) ? hasExecutingWorkLocked() : true;
    }

    public boolean hasExecutingWorkLocked() {
        return this.executingWork != null && this.executingWork.size() > 0;
    }

    private static void ungrantWorkItem(IActivityManager am, JobWorkItem work) {
        if (work.getGrants() != null) {
            ((GrantedUriPermissions) work.getGrants()).revoke(am);
        }
    }

    public boolean completeWorkLocked(IActivityManager am, int workId) {
        if (this.executingWork != null) {
            int N = this.executingWork.size();
            for (int i = 0; i < N; i++) {
                JobWorkItem work = (JobWorkItem) this.executingWork.get(i);
                if (work.getWorkId() == workId) {
                    this.executingWork.remove(i);
                    ungrantWorkItem(am, work);
                    return true;
                }
            }
        }
        return false;
    }

    private static void ungrantWorkList(IActivityManager am, ArrayList<JobWorkItem> list) {
        if (list != null) {
            int N = list.size();
            for (int i = 0; i < N; i++) {
                ungrantWorkItem(am, (JobWorkItem) list.get(i));
            }
        }
    }

    public void stopTrackingJobLocked(IActivityManager am, JobStatus incomingJob) {
        if (incomingJob != null) {
            if (this.executingWork != null && this.executingWork.size() > 0) {
                incomingJob.pendingWork = this.executingWork;
            }
            if (incomingJob.pendingWork == null) {
                incomingJob.pendingWork = this.pendingWork;
            } else if (this.pendingWork != null && this.pendingWork.size() > 0) {
                incomingJob.pendingWork.addAll(this.pendingWork);
            }
            this.pendingWork = null;
            this.executingWork = null;
            incomingJob.nextPendingWorkId = this.nextPendingWorkId;
            return;
        }
        ungrantWorkList(am, this.pendingWork);
        this.pendingWork = null;
        ungrantWorkList(am, this.executingWork);
        this.executingWork = null;
    }

    public void prepareLocked(IActivityManager am) {
        if (this.prepared) {
            Slog.wtf(TAG, "Already prepared: " + this);
            return;
        }
        this.prepared = true;
        this.unpreparedPoint = null;
        ClipData clip = this.job.getClipData();
        if (clip != null) {
            this.uriPerms = GrantedUriPermissions.createFromClip(am, clip, this.sourceUid, this.sourcePackageName, this.sourceUserId, this.job.getClipGrantFlags(), toShortString());
        }
    }

    public void unprepareLocked(IActivityManager am) {
        if (this.prepared) {
            this.prepared = false;
            this.unpreparedPoint = new Throwable().fillInStackTrace();
            if (this.uriPerms != null) {
                this.uriPerms.revoke(am);
                this.uriPerms = null;
            }
            return;
        }
        Slog.wtf(TAG, "Hasn't been prepared: " + this);
        if (this.unpreparedPoint != null) {
            Slog.e(TAG, "Was already unprepared at ", this.unpreparedPoint);
        }
    }

    public boolean isPreparedLocked() {
        return this.prepared;
    }

    public JobInfo getJob() {
        return this.job;
    }

    public int getJobId() {
        return this.job.getId();
    }

    public void printUniqueId(PrintWriter pw) {
        UserHandle.formatUid(pw, this.callingUid);
        pw.print("/");
        pw.print(this.job.getId());
    }

    public int getNumFailures() {
        return this.numFailures;
    }

    public ComponentName getServiceComponent() {
        return this.job.getService();
    }

    public String getSourcePackageName() {
        return this.sourcePackageName;
    }

    public int getSourceUid() {
        return this.sourceUid;
    }

    public int getSourceUserId() {
        return this.sourceUserId;
    }

    public int getUserId() {
        return UserHandle.getUserId(this.callingUid);
    }

    public String getSourceTag() {
        return this.sourceTag;
    }

    public int getUid() {
        return this.callingUid;
    }

    public String getBatteryName() {
        return this.batteryName;
    }

    public String getTag() {
        return this.tag;
    }

    public int getPriority() {
        return this.job.getPriority();
    }

    public int getFlags() {
        return this.job.getFlags();
    }

    public boolean hasConnectivityConstraint() {
        return (this.requiredConstraints & CONNECTIVITY_MASK) != 0;
    }

    public boolean needsAnyConnectivity() {
        return (this.requiredConstraints & CONSTRAINT_CONNECTIVITY) != 0;
    }

    public boolean needsUnmeteredConnectivity() {
        return (this.requiredConstraints & CONSTRAINT_UNMETERED) != 0;
    }

    public boolean needsMeteredConnectivity() {
        return (this.requiredConstraints & CONSTRAINT_METERED) != 0;
    }

    public boolean needsNonRoamingConnectivity() {
        return (this.requiredConstraints & CONSTRAINT_NOT_ROAMING) != 0;
    }

    public boolean hasChargingConstraint() {
        return (this.requiredConstraints & 1) != 0;
    }

    public boolean hasBatteryNotLowConstraint() {
        return (this.requiredConstraints & 2) != 0;
    }

    public boolean hasPowerConstraint() {
        return (this.requiredConstraints & 3) != 0;
    }

    public boolean hasStorageNotLowConstraint() {
        return (this.requiredConstraints & 8) != 0;
    }

    public boolean hasTimingDelayConstraint() {
        return (this.requiredConstraints & Integer.MIN_VALUE) != 0;
    }

    public boolean hasDeadlineConstraint() {
        return (this.requiredConstraints & CONSTRAINT_DEADLINE) != 0;
    }

    public boolean hasIdleConstraint() {
        return (this.requiredConstraints & 4) != 0;
    }

    public boolean hasContentTriggerConstraint() {
        return (this.requiredConstraints & CONSTRAINT_CONTENT_TRIGGER) != 0;
    }

    public long getTriggerContentUpdateDelay() {
        long time = this.job.getTriggerContentUpdateDelay();
        if (time < 0) {
            return DEFAULT_TRIGGER_UPDATE_DELAY;
        }
        return Math.max(time, 500);
    }

    public long getTriggerContentMaxDelay() {
        long time = this.job.getTriggerContentMaxDelay();
        if (time < 0) {
            return DEFAULT_TRIGGER_MAX_DELAY;
        }
        return Math.max(time, 1000);
    }

    public boolean isPersisted() {
        return this.job.isPersisted();
    }

    public long getEarliestRunTime() {
        return this.earliestRunTimeElapsedMillis;
    }

    public long getLatestRunTimeElapsed() {
        return this.latestRunTimeElapsedMillis;
    }

    boolean setChargingConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(1, state);
    }

    boolean setBatteryNotLowConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(2, state);
    }

    boolean setStorageNotLowConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(8, state);
    }

    boolean setTimingDelayConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(Integer.MIN_VALUE, state);
    }

    boolean setDeadlineConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_DEADLINE, state);
    }

    boolean setIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(4, state);
    }

    boolean setConnectivityConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_CONNECTIVITY, state);
    }

    boolean setUnmeteredConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_UNMETERED, state);
    }

    boolean setMeteredConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_METERED, state);
    }

    boolean setNotRoamingConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_NOT_ROAMING, state);
    }

    boolean setAppNotIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_APP_NOT_IDLE, state);
    }

    boolean setContentTriggerConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_CONTENT_TRIGGER, state);
    }

    boolean setDeviceNotDozingConstraintSatisfied(boolean state, boolean whitelisted) {
        this.dozeWhitelisted = whitelisted;
        return setConstraintSatisfied(CONSTRAINT_DEVICE_NOT_DOZING, state);
    }

    boolean setConstraintSatisfied(int constraint, boolean state) {
        if (((this.satisfiedConstraints & constraint) != 0) == state) {
            return false;
        }
        int i = this.satisfiedConstraints & (~constraint);
        if (!state) {
            constraint = 0;
        }
        this.satisfiedConstraints = i | constraint;
        return true;
    }

    boolean isConstraintSatisfied(int constraint) {
        return (this.satisfiedConstraints & constraint) != 0;
    }

    boolean clearTrackingController(int which) {
        if ((this.trackingControllers & which) == 0) {
            return false;
        }
        this.trackingControllers &= ~which;
        return true;
    }

    void setTrackingController(int which) {
        this.trackingControllers |= which;
    }

    public boolean shouldDump(int filterUid) {
        if (filterUid == -1 || UserHandle.getAppId(getUid()) == filterUid || UserHandle.getAppId(getSourceUid()) == filterUid) {
            return true;
        }
        return false;
    }

    public boolean isReady() {
        boolean deadlineSatisfied = (this.job.isPeriodic() || !hasDeadlineConstraint()) ? false : (this.satisfiedConstraints & CONSTRAINT_DEADLINE) != 0;
        boolean notIdle = (this.satisfiedConstraints & CONSTRAINT_APP_NOT_IDLE) != 0;
        boolean notDozing = (this.satisfiedConstraints & CONSTRAINT_DEVICE_NOT_DOZING) == 0 ? (this.job.getFlags() & 1) != 0 : true;
        if ((isConstraintsSatisfied() || deadlineSatisfied) && notIdle) {
            return notDozing;
        }
        return false;
    }

    public boolean isConstraintsSatisfied() {
        boolean z = true;
        if (this.overrideState == 2) {
            return true;
        }
        int req = this.requiredConstraints & CONSTRAINTS_OF_INTEREST;
        int sat = this.satisfiedConstraints & CONSTRAINTS_OF_INTEREST;
        if (this.overrideState == 1) {
            sat |= this.requiredConstraints & SOFT_OVERRIDE_CONSTRAINTS;
        }
        if ((sat & req) != req) {
            z = false;
        }
        return z;
    }

    public boolean matches(int uid, int jobId) {
        return this.job.getId() == jobId && this.callingUid == uid;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("JobStatus{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" #");
        UserHandle.formatUid(sb, this.callingUid);
        sb.append("/");
        sb.append(this.job.getId());
        sb.append(' ');
        sb.append(this.batteryName);
        sb.append(" u=");
        sb.append(getUserId());
        sb.append(" s=");
        sb.append(getSourceUid());
        if (!(this.earliestRunTimeElapsedMillis == 0 && this.latestRunTimeElapsedMillis == NO_LATEST_RUNTIME)) {
            long now = SystemClock.elapsedRealtime();
            sb.append(" TIME=");
            formatRunTime(sb, this.earliestRunTimeElapsedMillis, 0, now);
            sb.append(":");
            formatRunTime(sb, this.latestRunTimeElapsedMillis, (long) NO_LATEST_RUNTIME, now);
        }
        if (this.job.getNetworkType() != 0) {
            sb.append(" NET=");
            sb.append(this.job.getNetworkType());
        }
        if (this.job.isRequireCharging()) {
            sb.append(" CHARGING");
        }
        if (this.job.isRequireBatteryNotLow()) {
            sb.append(" BATNOTLOW");
        }
        if (this.job.isRequireStorageNotLow()) {
            sb.append(" STORENOTLOW");
        }
        if (this.job.isRequireDeviceIdle()) {
            sb.append(" IDLE");
        }
        if (this.job.isPersisted()) {
            sb.append(" PERSISTED");
        }
        if ((this.satisfiedConstraints & CONSTRAINT_APP_NOT_IDLE) == 0) {
            sb.append(" WAIT:APP_NOT_IDLE");
        }
        if ((this.satisfiedConstraints & CONSTRAINT_DEVICE_NOT_DOZING) == 0) {
            sb.append(" WAIT:DEV_NOT_DOZING");
        }
        if (this.job.getTriggerContentUris() != null) {
            sb.append(" URIS=");
            sb.append(Arrays.toString(this.job.getTriggerContentUris()));
        }
        if (this.numFailures != 0) {
            sb.append(" failures=");
            sb.append(this.numFailures);
        }
        if (isReady()) {
            sb.append(" READY");
        }
        sb.append("}");
        return sb.toString();
    }

    private void formatRunTime(PrintWriter pw, long runtime, long defaultValue, long now) {
        if (runtime == defaultValue) {
            pw.print("none");
        } else {
            TimeUtils.formatDuration(runtime - now, pw);
        }
    }

    private void formatRunTime(StringBuilder sb, long runtime, long defaultValue, long now) {
        if (runtime == defaultValue) {
            sb.append("none");
        } else {
            TimeUtils.formatDuration(runtime - now, sb);
        }
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" #");
        UserHandle.formatUid(sb, this.callingUid);
        sb.append("/");
        sb.append(this.job.getId());
        sb.append(' ');
        sb.append(this.batteryName);
        return sb.toString();
    }

    public String toShortStringExceptUniqueId() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.batteryName);
        return sb.toString();
    }

    void dumpConstraints(PrintWriter pw, int constraints) {
        if ((constraints & 1) != 0) {
            pw.print(" CHARGING");
        }
        if ((constraints & 2) != 0) {
            pw.print(" BATTERY_NOT_LOW");
        }
        if ((constraints & 8) != 0) {
            pw.print(" STORAGE_NOT_LOW");
        }
        if ((Integer.MIN_VALUE & constraints) != 0) {
            pw.print(" TIMING_DELAY");
        }
        if ((CONSTRAINT_DEADLINE & constraints) != 0) {
            pw.print(" DEADLINE");
        }
        if ((constraints & 4) != 0) {
            pw.print(" IDLE");
        }
        if ((CONSTRAINT_CONNECTIVITY & constraints) != 0) {
            pw.print(" CONNECTIVITY");
        }
        if ((CONSTRAINT_UNMETERED & constraints) != 0) {
            pw.print(" UNMETERED");
        }
        if ((CONSTRAINT_NOT_ROAMING & constraints) != 0) {
            pw.print(" NOT_ROAMING");
        }
        if ((CONSTRAINT_METERED & constraints) != 0) {
            pw.print(" METERED");
        }
        if ((CONSTRAINT_APP_NOT_IDLE & constraints) != 0) {
            pw.print(" APP_NOT_IDLE");
        }
        if ((CONSTRAINT_CONTENT_TRIGGER & constraints) != 0) {
            pw.print(" CONTENT_TRIGGER");
        }
        if ((CONSTRAINT_DEVICE_NOT_DOZING & constraints) != 0) {
            pw.print(" DEVICE_NOT_DOZING");
        }
    }

    private void dumpJobWorkItem(PrintWriter pw, String prefix, JobWorkItem work, int index) {
        pw.print(prefix);
        pw.print("  #");
        pw.print(index);
        pw.print(": #");
        pw.print(work.getWorkId());
        pw.print(" ");
        pw.print(work.getDeliveryCount());
        pw.print("x ");
        pw.println(work.getIntent());
        if (work.getGrants() != null) {
            pw.print(prefix);
            pw.println("  URI grants:");
            ((GrantedUriPermissions) work.getGrants()).dump(pw, prefix + "    ");
        }
    }

    public void dump(PrintWriter pw, String prefix, boolean full, long elapsedRealtimeMillis) {
        int i;
        pw.print(prefix);
        UserHandle.formatUid(pw, this.callingUid);
        pw.print(" tag=");
        pw.println(this.tag);
        pw.print(prefix);
        pw.print("Source: uid=");
        UserHandle.formatUid(pw, getSourceUid());
        pw.print(" user=");
        pw.print(getSourceUserId());
        pw.print(" pkg=");
        pw.println(getSourcePackageName());
        if (full) {
            pw.print(prefix);
            pw.println("JobInfo:");
            pw.print(prefix);
            pw.print("  Service: ");
            pw.println(this.job.getService().flattenToShortString());
            if (this.job.isPeriodic()) {
                pw.print(prefix);
                pw.print("  PERIODIC: interval=");
                TimeUtils.formatDuration(this.job.getIntervalMillis(), pw);
                pw.print(" flex=");
                TimeUtils.formatDuration(this.job.getFlexMillis(), pw);
                pw.println();
            }
            if (this.job.isPersisted()) {
                pw.print(prefix);
                pw.println("  PERSISTED");
            }
            if (this.job.getPriority() != 0) {
                pw.print(prefix);
                pw.print("  Priority: ");
                pw.println(this.job.getPriority());
            }
            if (this.job.getFlags() != 0) {
                pw.print(prefix);
                pw.print("  Flags: ");
                pw.println(Integer.toHexString(this.job.getFlags()));
            }
            pw.print(prefix);
            pw.print("  Requires: charging=");
            pw.print(this.job.isRequireCharging());
            pw.print(" batteryNotLow=");
            pw.print(this.job.isRequireBatteryNotLow());
            pw.print(" deviceIdle=");
            pw.println(this.job.isRequireDeviceIdle());
            if (this.job.getTriggerContentUris() != null) {
                pw.print(prefix);
                pw.println("  Trigger content URIs:");
                for (TriggerContentUri trig : this.job.getTriggerContentUris()) {
                    pw.print(prefix);
                    pw.print("    ");
                    pw.print(Integer.toHexString(trig.getFlags()));
                    pw.print(' ');
                    pw.println(trig.getUri());
                }
                if (this.job.getTriggerContentUpdateDelay() >= 0) {
                    pw.print(prefix);
                    pw.print("  Trigger update delay: ");
                    TimeUtils.formatDuration(this.job.getTriggerContentUpdateDelay(), pw);
                    pw.println();
                }
                if (this.job.getTriggerContentMaxDelay() >= 0) {
                    pw.print(prefix);
                    pw.print("  Trigger max delay: ");
                    TimeUtils.formatDuration(this.job.getTriggerContentMaxDelay(), pw);
                    pw.println();
                }
            }
            if (!(this.job.getExtras() == null || (this.job.getExtras().maybeIsEmpty() ^ 1) == 0)) {
                PersistableBundle temp = this.job.getExtras();
                if (temp.get("accountName") != null) {
                    temp.putString("accountName", "XXXXXXXXX");
                }
                pw.print(prefix);
                pw.print("  Extras: ");
                pw.println(temp.toShortString());
            }
            if (!(this.job.getTransientExtras() == null || (this.job.getTransientExtras().maybeIsEmpty() ^ 1) == 0)) {
                pw.print(prefix);
                pw.print("  Transient extras: ");
                pw.println(this.job.getTransientExtras().toShortString());
            }
            if (this.job.getClipData() != null) {
                pw.print(prefix);
                pw.print("  Clip data: ");
                StringBuilder b = new StringBuilder(128);
                this.job.getClipData().toShortString(b);
                pw.println(b);
            }
            if (this.uriPerms != null) {
                pw.print(prefix);
                pw.println("  Granted URI permissions:");
                this.uriPerms.dump(pw, prefix + "  ");
            }
            if (this.job.getNetworkType() != 0) {
                pw.print(prefix);
                pw.print("  Network type: ");
                pw.println(this.job.getNetworkType());
            }
            if (this.job.getMinLatencyMillis() != 0) {
                pw.print(prefix);
                pw.print("  Minimum latency: ");
                TimeUtils.formatDuration(this.job.getMinLatencyMillis(), pw);
                pw.println();
            }
            if (this.job.getMaxExecutionDelayMillis() != 0) {
                pw.print(prefix);
                pw.print("  Max execution delay: ");
                TimeUtils.formatDuration(this.job.getMaxExecutionDelayMillis(), pw);
                pw.println();
            }
            pw.print(prefix);
            pw.print("  Backoff: policy=");
            pw.print(this.job.getBackoffPolicy());
            pw.print(" initial=");
            TimeUtils.formatDuration(this.job.getInitialBackoffMillis(), pw);
            pw.println();
            if (this.job.hasEarlyConstraint()) {
                pw.print(prefix);
                pw.println("  Has early constraint");
            }
            if (this.job.hasLateConstraint()) {
                pw.print(prefix);
                pw.println("  Has late constraint");
            }
        }
        pw.print(prefix);
        pw.print("Required constraints:");
        dumpConstraints(pw, this.requiredConstraints);
        pw.println();
        if (full) {
            pw.print(prefix);
            pw.print("Satisfied constraints:");
            dumpConstraints(pw, this.satisfiedConstraints);
            pw.println();
            pw.print(prefix);
            pw.print("Unsatisfied constraints:");
            dumpConstraints(pw, this.requiredConstraints & (~this.satisfiedConstraints));
            pw.println();
            if (this.dozeWhitelisted) {
                pw.print(prefix);
                pw.println("Doze whitelisted: true");
            }
        }
        if (this.trackingControllers != 0) {
            pw.print(prefix);
            pw.print("Tracking:");
            if ((this.trackingControllers & 1) != 0) {
                pw.print(" BATTERY");
            }
            if ((this.trackingControllers & 2) != 0) {
                pw.print(" CONNECTIVITY");
            }
            if ((this.trackingControllers & 4) != 0) {
                pw.print(" CONTENT");
            }
            if ((this.trackingControllers & 8) != 0) {
                pw.print(" IDLE");
            }
            if ((this.trackingControllers & 16) != 0) {
                pw.print(" STORAGE");
            }
            if ((this.trackingControllers & 32) != 0) {
                pw.print(" TIME");
            }
            pw.println();
        }
        if (this.changedAuthorities != null) {
            pw.print(prefix);
            pw.println("Changed authorities:");
            for (i = 0; i < this.changedAuthorities.size(); i++) {
                pw.print(prefix);
                pw.print("  ");
                pw.println((String) this.changedAuthorities.valueAt(i));
            }
            if (this.changedUris != null) {
                pw.print(prefix);
                pw.println("Changed URIs:");
                for (i = 0; i < this.changedUris.size(); i++) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.println(this.changedUris.valueAt(i));
                }
            }
        }
        if (this.pendingWork != null && this.pendingWork.size() > 0) {
            pw.print(prefix);
            pw.println("Pending work:");
            for (i = 0; i < this.pendingWork.size(); i++) {
                dumpJobWorkItem(pw, prefix, (JobWorkItem) this.pendingWork.get(i), i);
            }
        }
        if (this.executingWork != null && this.executingWork.size() > 0) {
            pw.print(prefix);
            pw.println("Executing work:");
            for (i = 0; i < this.executingWork.size(); i++) {
                dumpJobWorkItem(pw, prefix, (JobWorkItem) this.executingWork.get(i), i);
            }
        }
        pw.print(prefix);
        pw.print("Enqueue time: ");
        TimeUtils.formatDuration(this.enqueueTime, elapsedRealtimeMillis, pw);
        pw.println();
        pw.print(prefix);
        pw.print("Run time: earliest=");
        formatRunTime(pw, this.earliestRunTimeElapsedMillis, 0, elapsedRealtimeMillis);
        pw.print(", latest=");
        formatRunTime(pw, this.latestRunTimeElapsedMillis, (long) NO_LATEST_RUNTIME, elapsedRealtimeMillis);
        pw.println();
        if (this.numFailures != 0) {
            pw.print(prefix);
            pw.print("Num failures: ");
            pw.println(this.numFailures);
        }
    }
}
