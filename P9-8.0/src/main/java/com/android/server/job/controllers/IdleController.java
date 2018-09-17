package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArraySet;
import com.android.server.am.ActivityManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;

public final class IdleController extends StateController {
    private static final String TAG = "IdleController";
    private static volatile IdleController sController;
    private static Object sCreationLock = new Object();
    IdlenessTracker mIdleTracker;
    private long mIdleWindowSlop;
    private long mInactivityIdleThreshold;
    final ArraySet<JobStatus> mTrackedTasks = new ArraySet();

    final class IdlenessTracker extends BroadcastReceiver {
        private AlarmManager mAlarm;
        boolean mIdle = false;
        private PendingIntent mIdleTriggerIntent;
        boolean mScreenOn = true;

        public IdlenessTracker() {
            this.mAlarm = (AlarmManager) IdleController.this.mContext.getSystemService("alarm");
            this.mIdleTriggerIntent = PendingIntent.getBroadcast(IdleController.this.mContext, 0, new Intent(ActivityManagerService.ACTION_TRIGGER_IDLE).setPackage("android").setFlags(1073741824), 0);
        }

        public boolean isIdle() {
            return this.mIdle;
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.DREAMING_STARTED");
            filter.addAction("android.intent.action.DREAMING_STOPPED");
            filter.addAction(ActivityManagerService.ACTION_TRIGGER_IDLE);
            IdleController.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON") || action.equals("android.intent.action.DREAMING_STOPPED")) {
                this.mScreenOn = true;
                if (this.mIdle) {
                    this.mIdle = false;
                    IdleController.this.reportNewIdleState(this.mIdle);
                    return;
                }
                this.mAlarm.cancel(this.mIdleTriggerIntent);
            } else if (action.equals("android.intent.action.SCREEN_OFF") || action.equals("android.intent.action.DREAMING_STARTED")) {
                long when = SystemClock.elapsedRealtime() + IdleController.this.mInactivityIdleThreshold;
                this.mScreenOn = false;
                this.mAlarm.setWindow(2, when, IdleController.this.mIdleWindowSlop, this.mIdleTriggerIntent);
            } else if (action.equals(ActivityManagerService.ACTION_TRIGGER_IDLE) && !this.mIdle && (this.mScreenOn ^ 1) != 0) {
                this.mIdle = true;
                IdleController.this.reportNewIdleState(this.mIdle);
            }
        }
    }

    public static IdleController get(JobSchedulerService service) {
        IdleController idleController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new IdleController(service, service.getContext(), service.getLock());
            }
            idleController = sController;
        }
        return idleController;
    }

    private IdleController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        initIdleStateTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasIdleConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setTrackingController(8);
            taskStatus.setIdleConstraintSatisfied(this.mIdleTracker.isIdle());
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(8)) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    void reportNewIdleState(boolean isIdle) {
        synchronized (this.mLock) {
            for (int i = this.mTrackedTasks.size() - 1; i >= 0; i--) {
                ((JobStatus) this.mTrackedTasks.valueAt(i)).setIdleConstraintSatisfied(isIdle);
            }
        }
        this.mStateChangedListener.onControllerStateChanged();
    }

    private void initIdleStateTracking() {
        this.mInactivityIdleThreshold = (long) this.mContext.getResources().getInteger(17694793);
        this.mIdleWindowSlop = (long) this.mContext.getResources().getInteger(17694792);
        this.mIdleTracker = new IdlenessTracker();
        this.mIdleTracker.startTracking();
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Idle: ");
        pw.println(this.mIdleTracker.isIdle());
        pw.print("Tracking ");
        pw.print(this.mTrackedTasks.size());
        pw.println(":");
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
    }
}
