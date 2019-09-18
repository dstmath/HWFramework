package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class TimeController extends StateController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.Time";
    private static final long UNIFIED_INTERVAL = 300000;
    private final String DEADLINE_TAG = "*job.deadline*";
    private final String DELAY_TAG = "*job.delay*";
    private AlarmManager mAlarmService = null;
    private final boolean mChainedAttributionEnabled = WorkSource.isChainedBatteryAttributionEnabled(this.mContext);
    private Display mCurDisplay;
    private final AlarmManager.OnAlarmListener mDeadlineExpiredListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            Slog.d(TimeController.TAG, "Deadline-expired alarm fired");
            TimeController.this.checkExpiredDeadlinesAndResetAlarm();
        }
    };
    private long mNextDelayExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private final AlarmManager.OnAlarmListener mNextDelayExpiredListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            Slog.d(TimeController.TAG, "Delay-expired alarm fired");
            TimeController.this.checkExpiredDelaysAndResetAlarm();
        }
    };
    private long mNextJobExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private long mOldDelayExpiredElapsedMillis = 9223372036854775806L;
    private long mOldJobExpiredElapsedMillis = 9223372036854775806L;
    private PowerManager mPowerManager;
    final List<JobStatus> mTrackedJobs = new LinkedList();

    public TimeController(JobSchedulerService service) {
        super(service);
        DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService("display");
        if (displayManager != null) {
            this.mCurDisplay = displayManager.getDisplay(0);
        }
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    public void maybeStartTrackingJobLocked(JobStatus job, JobStatus lastJob) {
        ListIterator<JobStatus> it;
        long j;
        long j2;
        JobStatus jobStatus = job;
        if (jobStatus.hasTimingDelayConstraint() || jobStatus.hasDeadlineConstraint()) {
            maybeStopTrackingJobLocked(jobStatus, null, false);
            if (maybeProxyServiceLocked(jobStatus)) {
                Slog.i(TAG, "startTrack, service is proxy: " + jobStatus.getServiceComponent() + ", tag:" + jobStatus.getTag());
                return;
            }
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            if (jobStatus.hasDeadlineConstraint() && evaluateDeadlineConstraint(jobStatus, nowElapsedMillis)) {
                return;
            }
            if (!jobStatus.hasTimingDelayConstraint() || !evaluateTimingDelayConstraint(jobStatus, nowElapsedMillis) || jobStatus.hasDeadlineConstraint()) {
                boolean isInsert = false;
                ListIterator<JobStatus> it2 = this.mTrackedJobs.listIterator(this.mTrackedJobs.size());
                while (true) {
                    it = it2;
                    if (!it.hasPrevious()) {
                        break;
                    } else if (it.previous().getLatestRunTimeElapsed() < jobStatus.getLatestRunTimeElapsed()) {
                        isInsert = true;
                        break;
                    } else {
                        it2 = it;
                    }
                }
                if (isInsert) {
                    it.next();
                }
                it.add(jobStatus);
                jobStatus.setTrackingController(32);
                if (jobStatus.hasTimingDelayConstraint()) {
                    j = jobStatus.getEarliestRunTime();
                } else {
                    j = Long.MAX_VALUE;
                }
                if (jobStatus.hasDeadlineConstraint()) {
                    j2 = jobStatus.getLatestRunTimeElapsed();
                } else {
                    j2 = Long.MAX_VALUE;
                }
                maybeUpdateAlarmsLocked(j, j2, deriveWorkSource(jobStatus.getSourceUid(), jobStatus.getSourcePackageName()));
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus job, JobStatus incomingJob, boolean forUpdate) {
        if (job.clearTrackingController(32) && this.mTrackedJobs.remove(job)) {
            checkExpiredDelaysAndResetAlarm();
            checkExpiredDeadlinesAndResetAlarm();
        }
    }

    private boolean isDisplayOn() {
        boolean z = true;
        if (this.mCurDisplay != null) {
            if (this.mCurDisplay.getState() != 2) {
                z = false;
            }
            boolean screenOn = z;
            if (DEBUG) {
                Slog.i(TAG, "cur display is screen on: " + screenOn);
            }
            return screenOn;
        } else if (this.mPowerManager == null) {
            return true;
        } else {
            Slog.e(TAG, "mCurDisplay is null and using PowerManager->isScreenOn");
            return this.mPowerManager.isScreenOn();
        }
    }

    private boolean isCharging() {
        int pluggedStatus = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("plugged", 0);
        if (DEBUG) {
            Slog.i(TAG, "pluggedStatus : " + pluggedStatus);
        }
        if (pluggedStatus != 0) {
            return true;
        }
        return false;
    }

    private boolean canStopTrackingJobLocked(JobStatus job) {
        return (!job.hasTimingDelayConstraint() || (job.satisfiedConstraints & Integer.MIN_VALUE) != 0) && (!job.hasDeadlineConstraint() || (job.satisfiedConstraints & 1073741824) != 0);
    }

    private void ensureAlarmServiceLocked() {
        if (this.mAlarmService == null) {
            this.mAlarmService = (AlarmManager) this.mContext.getSystemService("alarm");
        }
    }

    /* access modifiers changed from: package-private */
    public void checkExpiredDeadlinesAndResetAlarm() {
        synchronized (this.mLock) {
            long nextExpiryTime = JobStatus.NO_LATEST_RUNTIME;
            int nextExpiryUid = 0;
            String nextExpiryPackageName = null;
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                JobStatus job = it.next();
                if (job.hasDeadlineConstraint()) {
                    if (!evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                        nextExpiryTime = job.getLatestRunTimeElapsed();
                        nextExpiryUid = job.getSourceUid();
                        nextExpiryPackageName = job.getSourcePackageName();
                        break;
                    }
                    this.mStateChangedListener.onRunJobNow(job);
                    it.remove();
                }
            }
            setDeadlineExpiredAlarmLocked(nextExpiryTime, deriveWorkSource(nextExpiryUid, nextExpiryPackageName));
        }
    }

    private boolean evaluateDeadlineConstraint(JobStatus job, long nowElapsedMillis) {
        if (job.getLatestRunTimeElapsed() > nowElapsedMillis) {
            return false;
        }
        if (job.hasTimingDelayConstraint()) {
            job.setTimingDelayConstraintSatisfied(true);
        }
        job.setDeadlineConstraintSatisfied(true);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void checkExpiredDelaysAndResetAlarm() {
        synchronized (this.mLock) {
            long nowElapsedMillis = JobSchedulerService.sElapsedRealtimeClock.millis();
            long nextDelayTime = JobStatus.NO_LATEST_RUNTIME;
            int nextDelayUid = 0;
            String nextDelayPackageName = null;
            boolean ready = false;
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                JobStatus job = it.next();
                if (job.hasTimingDelayConstraint()) {
                    if (evaluateTimingDelayConstraint(job, nowElapsedMillis)) {
                        if (canStopTrackingJobLocked(job)) {
                            it.remove();
                        }
                        if (job.isReady()) {
                            ready = true;
                        }
                    } else if (!job.isConstraintSatisfied(Integer.MIN_VALUE)) {
                        long jobDelayTime = job.getEarliestRunTime();
                        if (nextDelayTime > jobDelayTime) {
                            nextDelayTime = jobDelayTime;
                            nextDelayUid = job.getSourceUid();
                            nextDelayPackageName = job.getSourcePackageName();
                        }
                    }
                }
            }
            if (ready) {
                Slog.i(TAG, "onControllerStateChanged");
                this.mStateChangedListener.onControllerStateChanged();
            }
            setDelayExpiredAlarmLocked(nextDelayTime, deriveWorkSource(nextDelayUid, nextDelayPackageName));
        }
    }

    private WorkSource deriveWorkSource(int uid, String packageName) {
        if (this.mChainedAttributionEnabled) {
            WorkSource ws = new WorkSource();
            ws.createWorkChain().addNode(uid, packageName).addNode(1000, JobSchedulerService.TAG);
            return ws;
        }
        return packageName == null ? new WorkSource(uid) : new WorkSource(uid, packageName);
    }

    private boolean evaluateTimingDelayConstraint(JobStatus job, long nowElapsedMillis) {
        if (job.getEarliestRunTime() > nowElapsedMillis) {
            return false;
        }
        job.setTimingDelayConstraintSatisfied(true);
        return true;
    }

    private void maybeUpdateAlarmsLocked(long delayExpiredElapsed, long deadlineExpiredElapsed, WorkSource ws) {
        if (delayExpiredElapsed < this.mNextDelayExpiredElapsedMillis) {
            setDelayExpiredAlarmLocked(delayExpiredElapsed, ws);
        }
        if (deadlineExpiredElapsed < this.mNextJobExpiredElapsedMillis) {
            setDeadlineExpiredAlarmLocked(deadlineExpiredElapsed, ws);
        }
    }

    private void setDelayExpiredAlarmLocked(long alarmTimeElapsedMillis, WorkSource ws) {
        long alarmTimeElapsedMillis2 = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (isDisplayOn() || isCharging() || alarmTimeElapsedMillis2 <= earliestWakeupTimeElapsed) {
            this.mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis2;
        } else {
            this.mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis2 % 300000 != 0 ? 300000 * ((alarmTimeElapsedMillis2 / 300000) + 1) : alarmTimeElapsedMillis2;
        }
        if (this.mOldDelayExpiredElapsedMillis != this.mNextDelayExpiredElapsedMillis) {
            updateAlarmWithListenerLocked("*job.delay*", this.mNextDelayExpiredListener, this.mNextDelayExpiredElapsedMillis, ws);
            this.mOldDelayExpiredElapsedMillis = this.mNextDelayExpiredElapsedMillis;
            Slog.i(TAG, "mNextDelayExpiredElapsedMillis: " + this.mNextDelayExpiredElapsedMillis);
        }
    }

    private void setDeadlineExpiredAlarmLocked(long alarmTimeElapsedMillis, WorkSource ws) {
        long alarmTimeElapsedMillis2 = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (isDisplayOn() || isCharging() || alarmTimeElapsedMillis2 <= earliestWakeupTimeElapsed) {
            this.mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis2;
        } else {
            this.mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis2 % 300000 != 0 ? 300000 * ((alarmTimeElapsedMillis2 / 300000) + 1) : alarmTimeElapsedMillis2;
        }
        if (this.mOldJobExpiredElapsedMillis != this.mNextJobExpiredElapsedMillis) {
            updateAlarmWithListenerLocked("*job.deadline*", this.mDeadlineExpiredListener, this.mNextJobExpiredElapsedMillis, ws);
            this.mOldJobExpiredElapsedMillis = this.mNextJobExpiredElapsedMillis;
            Slog.i(TAG, "mNextJobExpiredElapsedMillis: " + this.mNextJobExpiredElapsedMillis);
        }
    }

    private long maybeAdjustAlarmTime(long proposedAlarmTimeElapsedMillis) {
        long earliestWakeupTimeElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        if (proposedAlarmTimeElapsedMillis < earliestWakeupTimeElapsed) {
            return earliestWakeupTimeElapsed;
        }
        return proposedAlarmTimeElapsedMillis;
    }

    private void updateAlarmWithListenerLocked(String tag, AlarmManager.OnAlarmListener listener, long alarmTimeElapsed, WorkSource ws) {
        String str;
        long j = alarmTimeElapsed;
        ensureAlarmServiceLocked();
        if (j == JobStatus.NO_LATEST_RUNTIME) {
            this.mAlarmService.cancel(listener);
            return;
        }
        AlarmManager.OnAlarmListener onAlarmListener = listener;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Setting ");
            str = tag;
            sb.append(str);
            sb.append(" for: ");
            sb.append(j);
            Slog.d(TAG, sb.toString());
        } else {
            str = tag;
        }
        this.mAlarmService.set(2, j, -1, 0, str, onAlarmListener, null, ws);
    }

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        pw.println("Elapsed clock: " + nowElapsed);
        pw.print("Next delay alarm in ");
        TimeUtils.formatDuration(this.mNextDelayExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.print("Next deadline alarm in ");
        TimeUtils.formatDuration(this.mNextJobExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.println();
        for (JobStatus ts : this.mTrackedJobs) {
            if (predicate.test(ts)) {
                pw.print("#");
                ts.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, ts.getSourceUid());
                pw.print(": Delay=");
                if (ts.hasTimingDelayConstraint()) {
                    TimeUtils.formatDuration(ts.getEarliestRunTime(), nowElapsed, pw);
                } else {
                    pw.print("N/A");
                }
                pw.print(", Deadline=");
                if (ts.hasDeadlineConstraint()) {
                    TimeUtils.formatDuration(ts.getLatestRunTimeElapsed(), nowElapsed, pw);
                } else {
                    pw.print("N/A");
                }
                pw.println();
            }
        }
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        long mToken = protoOutputStream.start(1146756268040L);
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        protoOutputStream.write(1112396529665L, nowElapsed);
        protoOutputStream.write(1112396529666L, this.mNextDelayExpiredElapsedMillis - nowElapsed);
        protoOutputStream.write(1112396529667L, this.mNextJobExpiredElapsedMillis - nowElapsed);
        for (JobStatus ts : this.mTrackedJobs) {
            if (predicate.test(ts)) {
                long tsToken = protoOutputStream.start(2246267895812L);
                ts.writeToShortProto(protoOutputStream, 1146756268033L);
                protoOutputStream.write(1133871366147L, ts.hasTimingDelayConstraint());
                protoOutputStream.write(1112396529668L, ts.getEarliestRunTime() - nowElapsed);
                protoOutputStream.write(1133871366149L, ts.hasDeadlineConstraint());
                protoOutputStream.write(1112396529670L, ts.getLatestRunTimeElapsed() - nowElapsed);
                protoOutputStream.end(tsToken);
                token = token;
            }
        }
        Predicate<JobStatus> predicate2 = predicate;
        long j = token;
        protoOutputStream.end(mToken);
        protoOutputStream.end(token);
    }
}
