package com.android.server.job.controllers;

import android.app.AppGlobals;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.format.DateUtils;
import android.util.ArraySet;
import java.io.PrintWriter;

public final class JobStatus {
    static final int CONSTRAINTS_OF_INTEREST = 699;
    static final int CONSTRAINT_APP_NOT_IDLE = 64;
    static final int CONSTRAINT_CHARGING = 1;
    static final int CONSTRAINT_CONNECTIVITY = 32;
    static final int CONSTRAINT_CONTENT_TRIGGER = 128;
    static final int CONSTRAINT_DEADLINE = 4;
    static final int CONSTRAINT_DEVICE_NOT_DOZING = 256;
    static final int CONSTRAINT_IDLE = 8;
    static final int CONSTRAINT_NOT_ROAMING = 512;
    static final int CONSTRAINT_TIMING_DELAY = 2;
    static final int CONSTRAINT_UNMETERED = 16;
    public static final long DEFAULT_TRIGGER_MAX_DELAY = 120000;
    public static final long DEFAULT_TRIGGER_UPDATE_DELAY = 10000;
    public static final long MIN_TRIGGER_MAX_DELAY = 1000;
    public static final long MIN_TRIGGER_UPDATE_DELAY = 500;
    public static final long NO_EARLIEST_RUNTIME = 0;
    public static final long NO_LATEST_RUNTIME = Long.MAX_VALUE;
    public static final int OVERRIDE_FULL = 2;
    public static final int OVERRIDE_SOFT = 1;
    static final int SOFT_OVERRIDE_CONSTRAINTS = 11;
    final String batteryName;
    final int callingUid;
    public ArraySet<String> changedAuthorities;
    public ArraySet<Uri> changedUris;
    JobInstance contentObserverJobInstance;
    public boolean dozeWhitelisted;
    private final long earliestRunTimeElapsedMillis;
    final JobInfo job;
    public int lastEvaluatedPriority;
    private final long latestRunTimeElapsedMillis;
    private final int numFailures;
    public int overrideState;
    final int requiredConstraints;
    int satisfiedConstraints;
    final String sourcePackageName;
    boolean sourcePackageValid;
    final String sourceTag;
    final int sourceUid;
    final int sourceUserId;
    final String tag;

    public void dump(java.io.PrintWriter r1, java.lang.String r2, boolean r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.JobStatus.dump(java.io.PrintWriter, java.lang.String, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.JobStatus.dump(java.io.PrintWriter, java.lang.String, boolean):void");
    }

    boolean setConstraintSatisfied(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.JobStatus.setConstraintSatisfied(int, boolean):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.JobStatus.setConstraintSatisfied(int, boolean):boolean");
    }

    public int getServiceToken() {
        return this.callingUid;
    }

    public boolean isSourcePackageValid() {
        return this.sourcePackageValid;
    }

    private JobStatus(JobInfo job, int callingUid, String sourcePackageName, int sourceUserId, String tag, int numFailures, long earliestRunTimeElapsedMillis, long latestRunTimeElapsedMillis) {
        String str;
        this.sourcePackageValid = true;
        this.satisfiedConstraints = 0;
        this.overrideState = 0;
        this.job = job;
        this.callingUid = callingUid;
        int tempSourceUid = -1;
        if (!(sourceUserId == -1 || sourcePackageName == null)) {
            try {
                tempSourceUid = AppGlobals.getPackageManager().getPackageUid(sourcePackageName, 0, sourceUserId);
            } catch (RemoteException e) {
            }
            if (tempSourceUid == -1) {
                this.sourcePackageValid = false;
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
        int requiredConstraints = 0;
        if (job.getNetworkType() == OVERRIDE_SOFT) {
            requiredConstraints = CONSTRAINT_CONNECTIVITY;
        }
        if (job.getNetworkType() == OVERRIDE_FULL) {
            requiredConstraints |= CONSTRAINT_UNMETERED;
        }
        if (job.getNetworkType() == 3) {
            requiredConstraints |= CONSTRAINT_NOT_ROAMING;
        }
        if (job.isRequireCharging()) {
            requiredConstraints |= OVERRIDE_SOFT;
        }
        if (earliestRunTimeElapsedMillis != NO_EARLIEST_RUNTIME) {
            requiredConstraints |= OVERRIDE_FULL;
        }
        if (latestRunTimeElapsedMillis != NO_LATEST_RUNTIME) {
            requiredConstraints |= CONSTRAINT_DEADLINE;
        }
        if (job.isRequireDeviceIdle()) {
            requiredConstraints |= CONSTRAINT_IDLE;
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
            earliestRunTimeElapsedMillis = job.hasEarlyConstraint() ? elapsedNow + job.getMinLatencyMillis() : NO_EARLIEST_RUNTIME;
            latestRunTimeElapsedMillis = job.hasLateConstraint() ? elapsedNow + job.getMaxExecutionDelayMillis() : NO_LATEST_RUNTIME;
        }
        return new JobStatus(job, callingUid, sourcePackageName, sourceUserId, tag, 0, earliestRunTimeElapsedMillis, latestRunTimeElapsedMillis);
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

    public PersistableBundle getExtras() {
        return this.job.getExtras();
    }

    public int getPriority() {
        return this.job.getPriority();
    }

    public int getFlags() {
        return this.job.getFlags();
    }

    public boolean hasConnectivityConstraint() {
        return (this.requiredConstraints & CONSTRAINT_CONNECTIVITY) != 0;
    }

    public boolean hasUnmeteredConstraint() {
        return (this.requiredConstraints & CONSTRAINT_UNMETERED) != 0;
    }

    public boolean hasNotRoamingConstraint() {
        return (this.requiredConstraints & CONSTRAINT_NOT_ROAMING) != 0;
    }

    public boolean hasChargingConstraint() {
        return (this.requiredConstraints & OVERRIDE_SOFT) != 0;
    }

    public boolean hasTimingDelayConstraint() {
        return (this.requiredConstraints & OVERRIDE_FULL) != 0;
    }

    public boolean hasDeadlineConstraint() {
        return (this.requiredConstraints & CONSTRAINT_DEADLINE) != 0;
    }

    public boolean hasIdleConstraint() {
        return (this.requiredConstraints & CONSTRAINT_IDLE) != 0;
    }

    public boolean hasContentTriggerConstraint() {
        return (this.requiredConstraints & CONSTRAINT_CONTENT_TRIGGER) != 0;
    }

    public long getTriggerContentUpdateDelay() {
        long time = this.job.getTriggerContentUpdateDelay();
        if (time < NO_EARLIEST_RUNTIME) {
            return DEFAULT_TRIGGER_UPDATE_DELAY;
        }
        return Math.max(time, MIN_TRIGGER_UPDATE_DELAY);
    }

    public long getTriggerContentMaxDelay() {
        long time = this.job.getTriggerContentMaxDelay();
        if (time < NO_EARLIEST_RUNTIME) {
            return DEFAULT_TRIGGER_MAX_DELAY;
        }
        return Math.max(time, MIN_TRIGGER_MAX_DELAY);
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
        return setConstraintSatisfied(OVERRIDE_SOFT, state);
    }

    boolean setTimingDelayConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(OVERRIDE_FULL, state);
    }

    boolean setDeadlineConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_DEADLINE, state);
    }

    boolean setIdleConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_IDLE, state);
    }

    boolean setConnectivityConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_CONNECTIVITY, state);
    }

    boolean setUnmeteredConstraintSatisfied(boolean state) {
        return setConstraintSatisfied(CONSTRAINT_UNMETERED, state);
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

    boolean isConstraintSatisfied(int constraint) {
        return (this.satisfiedConstraints & constraint) != 0;
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
        boolean notDozing = (this.satisfiedConstraints & CONSTRAINT_DEVICE_NOT_DOZING) == 0 ? (this.job.getFlags() & OVERRIDE_SOFT) != 0 : true;
        if ((isConstraintsSatisfied() || deadlineSatisfied) && notIdle) {
            return notDozing;
        }
        return false;
    }

    public boolean isConstraintsSatisfied() {
        boolean z = true;
        if (this.overrideState == OVERRIDE_FULL) {
            return true;
        }
        int req = this.requiredConstraints & CONSTRAINTS_OF_INTEREST;
        int sat = this.satisfiedConstraints & CONSTRAINTS_OF_INTEREST;
        if (this.overrideState == OVERRIDE_SOFT) {
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
        boolean z;
        String str;
        boolean z2 = true;
        StringBuilder append = new StringBuilder().append(String.valueOf(hashCode()).substring(0, 3)).append("..").append(":[").append(this.job.getService()).append(",jId=").append(this.job.getId()).append(",u").append(getUserId()).append(",R=(").append(formatRunTime(this.earliestRunTimeElapsedMillis, NO_EARLIEST_RUNTIME)).append(",").append(formatRunTime(this.latestRunTimeElapsedMillis, NO_LATEST_RUNTIME)).append(")").append(",N=").append(this.job.getNetworkType()).append(",C=").append(this.job.isRequireCharging()).append(",I=").append(this.job.isRequireDeviceIdle()).append(",U=").append(this.job.getTriggerContentUris() != null).append(",F=").append(this.numFailures).append(",P=").append(this.job.isPersisted()).append(",ANI=");
        if ((this.satisfiedConstraints & CONSTRAINT_APP_NOT_IDLE) != 0) {
            z = true;
        } else {
            z = false;
        }
        StringBuilder append2 = append.append(z).append(",DND=");
        if ((this.satisfiedConstraints & CONSTRAINT_DEVICE_NOT_DOZING) == 0) {
            z2 = false;
        }
        StringBuilder append3 = append2.append(z2);
        if (isReady()) {
            str = "(READY)";
        } else {
            str = "";
        }
        return append3.append(str).append("]").toString();
    }

    private String formatRunTime(long runtime, long defaultValue) {
        if (runtime == defaultValue) {
            return "none";
        }
        long nextRuntime = runtime - SystemClock.elapsedRealtime();
        if (nextRuntime > NO_EARLIEST_RUNTIME) {
            return DateUtils.formatElapsedTime(nextRuntime / MIN_TRIGGER_MAX_DELAY);
        }
        return "-" + DateUtils.formatElapsedTime(nextRuntime / -1000);
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
        if ((constraints & OVERRIDE_SOFT) != 0) {
            pw.print(" CHARGING");
        }
        if ((constraints & OVERRIDE_FULL) != 0) {
            pw.print(" TIMING_DELAY");
        }
        if ((constraints & CONSTRAINT_DEADLINE) != 0) {
            pw.print(" DEADLINE");
        }
        if ((constraints & CONSTRAINT_IDLE) != 0) {
            pw.print(" IDLE");
        }
        if ((constraints & CONSTRAINT_CONNECTIVITY) != 0) {
            pw.print(" CONNECTIVITY");
        }
        if ((constraints & CONSTRAINT_UNMETERED) != 0) {
            pw.print(" UNMETERED");
        }
        if ((constraints & CONSTRAINT_NOT_ROAMING) != 0) {
            pw.print(" NOT_ROAMING");
        }
        if ((constraints & CONSTRAINT_APP_NOT_IDLE) != 0) {
            pw.print(" APP_NOT_IDLE");
        }
        if ((constraints & CONSTRAINT_CONTENT_TRIGGER) != 0) {
            pw.print(" CONTENT_TRIGGER");
        }
        if ((constraints & CONSTRAINT_DEVICE_NOT_DOZING) != 0) {
            pw.print(" DEVICE_NOT_DOZING");
        }
    }
}
