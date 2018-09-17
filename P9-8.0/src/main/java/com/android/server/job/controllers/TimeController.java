package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.Display;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TimeController extends StateController {
    private static final String TAG = "JobScheduler.Time";
    private static final long UNIFIED_INTERVAL = 300000;
    static TimeController mSingleton;
    private final String DEADLINE_TAG = "*job.deadline*";
    private final String DELAY_TAG = "*job.delay*";
    private AlarmManager mAlarmService = null;
    private Context mContext;
    private Display mCurDisplay;
    private final OnAlarmListener mDeadlineExpiredListener = new OnAlarmListener() {
        public void onAlarm() {
            TimeController.this.checkExpiredDeadlinesAndResetAlarm();
        }
    };
    private long mNextDelayExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private final OnAlarmListener mNextDelayExpiredListener = new OnAlarmListener() {
        public void onAlarm() {
            TimeController.this.checkExpiredDelaysAndResetAlarm();
        }
    };
    private long mNextJobExpiredElapsedMillis = JobStatus.NO_LATEST_RUNTIME;
    private long mOldDelayExpiredElapsedMillis = 9223372036854775806L;
    private long mOldJobExpiredElapsedMillis = 9223372036854775806L;
    private PowerManager mPowerManager;
    final List<JobStatus> mTrackedJobs = new LinkedList();

    public static synchronized TimeController get(JobSchedulerService jms) {
        TimeController timeController;
        synchronized (TimeController.class) {
            if (mSingleton == null) {
                mSingleton = new TimeController(jms, jms.getContext(), jms.getLock());
            }
            timeController = mSingleton;
        }
        return timeController;
    }

    TimeController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mContext = context;
        DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
        if (displayManager != null) {
            this.mCurDisplay = displayManager.getDisplay(0);
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    public void maybeStartTrackingJobLocked(JobStatus job, JobStatus lastJob) {
        if (job.hasTimingDelayConstraint() || job.hasDeadlineConstraint()) {
            maybeStopTrackingJobLocked(job, null, false);
            if (maybeProxyServiceLocked(job)) {
                Slog.i(TAG, "startTrack, service is proxy: " + job.getServiceComponent() + ", tag:" + job.getTag());
                return;
            }
            long nowElapsedMillis = SystemClock.elapsedRealtime();
            if (!job.hasDeadlineConstraint() || !evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                if (!job.hasTimingDelayConstraint() || !evaluateTimingDelayConstraint(job, nowElapsedMillis)) {
                    boolean isInsert = false;
                    ListIterator<JobStatus> it = this.mTrackedJobs.listIterator(this.mTrackedJobs.size());
                    while (it.hasPrevious()) {
                        if (((JobStatus) it.previous()).getLatestRunTimeElapsed() < job.getLatestRunTimeElapsed()) {
                            isInsert = true;
                            break;
                        }
                    }
                    if (isInsert) {
                        it.next();
                    }
                    it.add(job);
                    job.setTrackingController(32);
                    maybeUpdateAlarmsLocked(job.hasTimingDelayConstraint() ? job.getEarliestRunTime() : JobStatus.NO_LATEST_RUNTIME, job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() : JobStatus.NO_LATEST_RUNTIME, job.getSourceUid());
                }
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
        if (this.mCurDisplay != null) {
            return this.mCurDisplay.getState() == 2;
        } else if (this.mPowerManager == null) {
            return true;
        } else {
            Slog.e(TAG, "mCurDisplay is null and using PowerManager->isScreenOn");
            return this.mPowerManager.isScreenOn();
        }
    }

    private boolean isCharging() {
        if (this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("plugged", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean canStopTrackingJobLocked(JobStatus job) {
        boolean z = true;
        if (job.hasTimingDelayConstraint() && (job.satisfiedConstraints & Integer.MIN_VALUE) == 0) {
            return false;
        }
        if (job.hasDeadlineConstraint() && (job.satisfiedConstraints & 1073741824) == 0) {
            z = false;
        }
        return z;
    }

    private void ensureAlarmServiceLocked() {
        if (this.mAlarmService == null) {
            this.mAlarmService = (AlarmManager) this.mContext.getSystemService("alarm");
        }
    }

    void checkExpiredDeadlinesAndResetAlarm() {
        synchronized (this.mLock) {
            long nextExpiryTime = JobStatus.NO_LATEST_RUNTIME;
            int nextExpiryUid = 0;
            long nowElapsedMillis = SystemClock.elapsedRealtime();
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                JobStatus job = (JobStatus) it.next();
                if (job.hasDeadlineConstraint()) {
                    if (!evaluateDeadlineConstraint(job, nowElapsedMillis)) {
                        nextExpiryTime = job.getLatestRunTimeElapsed();
                        nextExpiryUid = job.getSourceUid();
                        break;
                    }
                    this.mStateChangedListener.onRunJobNow(job);
                    it.remove();
                }
            }
            setDeadlineExpiredAlarmLocked(nextExpiryTime, nextExpiryUid);
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

    void checkExpiredDelaysAndResetAlarm() {
        synchronized (this.mLock) {
            long nowElapsedMillis = SystemClock.elapsedRealtime();
            long nextDelayTime = JobStatus.NO_LATEST_RUNTIME;
            int nextDelayUid = 0;
            boolean ready = false;
            Iterator<JobStatus> it = this.mTrackedJobs.iterator();
            while (it.hasNext()) {
                JobStatus job = (JobStatus) it.next();
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
                        }
                    }
                }
            }
            if (ready) {
                this.mStateChangedListener.onControllerStateChanged();
            }
            setDelayExpiredAlarmLocked(nextDelayTime, nextDelayUid);
        }
    }

    private boolean evaluateTimingDelayConstraint(JobStatus job, long nowElapsedMillis) {
        if (job.getEarliestRunTime() > nowElapsedMillis) {
            return false;
        }
        job.setTimingDelayConstraintSatisfied(true);
        return true;
    }

    private void maybeUpdateAlarmsLocked(long delayExpiredElapsed, long deadlineExpiredElapsed, int uid) {
        if (delayExpiredElapsed < this.mNextDelayExpiredElapsedMillis) {
            setDelayExpiredAlarmLocked(delayExpiredElapsed, uid);
        }
        if (deadlineExpiredElapsed < this.mNextJobExpiredElapsedMillis) {
            setDeadlineExpiredAlarmLocked(deadlineExpiredElapsed, uid);
        }
    }

    private void setDelayExpiredAlarmLocked(long alarmTimeElapsedMillis, int uid) {
        alarmTimeElapsedMillis = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (isDisplayOn() || (isCharging() ^ 1) == 0 || alarmTimeElapsedMillis <= earliestWakeupTimeElapsed) {
            this.mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis;
        } else {
            if (alarmTimeElapsedMillis % UNIFIED_INTERVAL != 0) {
                alarmTimeElapsedMillis = ((alarmTimeElapsedMillis / UNIFIED_INTERVAL) + 1) * UNIFIED_INTERVAL;
            }
            this.mNextDelayExpiredElapsedMillis = alarmTimeElapsedMillis;
        }
        if (this.mOldDelayExpiredElapsedMillis != this.mNextDelayExpiredElapsedMillis) {
            updateAlarmWithListenerLocked("*job.delay*", this.mNextDelayExpiredListener, this.mNextDelayExpiredElapsedMillis, uid);
            this.mOldDelayExpiredElapsedMillis = this.mNextDelayExpiredElapsedMillis;
            Slog.i(TAG, "mNextDelayExpiredElapsedMillis: " + this.mNextDelayExpiredElapsedMillis);
        }
    }

    private void setDeadlineExpiredAlarmLocked(long alarmTimeElapsedMillis, int uid) {
        alarmTimeElapsedMillis = maybeAdjustAlarmTime(alarmTimeElapsedMillis);
        long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (isDisplayOn() || (isCharging() ^ 1) == 0 || alarmTimeElapsedMillis <= earliestWakeupTimeElapsed) {
            this.mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis;
        } else {
            if (alarmTimeElapsedMillis % UNIFIED_INTERVAL != 0) {
                alarmTimeElapsedMillis = ((alarmTimeElapsedMillis / UNIFIED_INTERVAL) + 1) * UNIFIED_INTERVAL;
            }
            this.mNextJobExpiredElapsedMillis = alarmTimeElapsedMillis;
        }
        if (this.mOldJobExpiredElapsedMillis != this.mNextJobExpiredElapsedMillis) {
            updateAlarmWithListenerLocked("*job.deadline*", this.mDeadlineExpiredListener, this.mNextJobExpiredElapsedMillis, uid);
            this.mOldJobExpiredElapsedMillis = this.mNextJobExpiredElapsedMillis;
            Slog.i(TAG, "mNextJobExpiredElapsedMillis: " + this.mNextJobExpiredElapsedMillis);
        }
    }

    private long maybeAdjustAlarmTime(long proposedAlarmTimeElapsedMillis) {
        long earliestWakeupTimeElapsed = SystemClock.elapsedRealtime();
        if (proposedAlarmTimeElapsedMillis < earliestWakeupTimeElapsed) {
            return earliestWakeupTimeElapsed;
        }
        return proposedAlarmTimeElapsedMillis;
    }

    private void updateAlarmWithListenerLocked(String tag, OnAlarmListener listener, long alarmTimeElapsed, int uid) {
        ensureAlarmServiceLocked();
        if (alarmTimeElapsed == JobStatus.NO_LATEST_RUNTIME) {
            this.mAlarmService.cancel(listener);
            return;
        }
        this.mAlarmService.set(2, alarmTimeElapsed, -1, 0, tag, listener, null, new WorkSource(uid));
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        long nowElapsed = SystemClock.elapsedRealtime();
        pw.print("Alarms: now=");
        pw.print(SystemClock.elapsedRealtime());
        pw.println();
        pw.print("Next delay alarm in ");
        TimeUtils.formatDuration(this.mNextDelayExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.print("Next deadline alarm in ");
        TimeUtils.formatDuration(this.mNextJobExpiredElapsedMillis, nowElapsed, pw);
        pw.println();
        pw.print("Tracking ");
        pw.print(this.mTrackedJobs.size());
        pw.println(":");
        for (JobStatus ts : this.mTrackedJobs) {
            if (ts.shouldDump(filterUid)) {
                pw.print("  #");
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
}
