package com.android.server.job.controllers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.os.UserHandle;
import com.android.server.am.ActivityManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;

public class IdleController extends StateController {
    private static final String TAG = "IdleController";
    private static volatile IdleController sController;
    private static Object sCreationLock;
    IdlenessTracker mIdleTracker;
    private long mIdleWindowSlop;
    private long mInactivityIdleThreshold;
    final ArrayList<JobStatus> mTrackedTasks;

    class IdlenessTracker extends BroadcastReceiver {
        private AlarmManager mAlarm;
        boolean mIdle;
        private PendingIntent mIdleTriggerIntent;
        boolean mScreenOn;

        public IdlenessTracker() {
            this.mAlarm = (AlarmManager) IdleController.this.mContext.getSystemService("alarm");
            this.mIdleTriggerIntent = PendingIntent.getBroadcast(IdleController.this.mContext, 0, new Intent(ActivityManagerService.ACTION_TRIGGER_IDLE).setPackage("android").setFlags(1073741824), 0);
            this.mIdle = false;
            this.mScreenOn = true;
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
            } else if (action.equals(ActivityManagerService.ACTION_TRIGGER_IDLE) && !this.mIdle && !this.mScreenOn) {
                this.mIdle = true;
                IdleController.this.reportNewIdleState(this.mIdle);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.IdleController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.job.controllers.IdleController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.IdleController.<clinit>():void");
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
        this.mTrackedTasks = new ArrayList();
        initIdleStateTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasIdleConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setIdleConstraintSatisfied(this.mIdleTracker.isIdle());
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        this.mTrackedTasks.remove(taskStatus);
    }

    void reportNewIdleState(boolean isIdle) {
        synchronized (this.mLock) {
            for (JobStatus task : this.mTrackedTasks) {
                task.setIdleConstraintSatisfied(isIdle);
            }
        }
        this.mStateChangedListener.onControllerStateChanged();
    }

    private void initIdleStateTracking() {
        this.mInactivityIdleThreshold = (long) this.mContext.getResources().getInteger(17694886);
        this.mIdleWindowSlop = (long) this.mContext.getResources().getInteger(17694887);
        this.mIdleTracker = new IdlenessTracker();
        this.mIdleTracker.startTracking();
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Idle: ");
        pw.println(this.mIdleTracker.isIdle() ? "true" : "false");
        pw.print("Tracking ");
        pw.print(this.mTrackedTasks.size());
        pw.println(":");
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedTasks.get(i);
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
