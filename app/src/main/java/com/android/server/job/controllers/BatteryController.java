package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.UserHandle;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BatteryController extends StateController {
    private static final String TAG = "JobScheduler.Batt";
    private static volatile BatteryController sController;
    private static final Object sCreationLock = null;
    private ChargingTracker mChargeTracker;
    private List<JobStatus> mTrackedTasks;

    public class ChargingTracker extends BroadcastReceiver {
        private boolean mBatteryHealthy;
        private boolean mCharging;

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction("android.intent.action.BATTERY_OKAY");
            filter.addAction("android.os.action.CHARGING");
            filter.addAction("android.os.action.DISCHARGING");
            BatteryController.this.mContext.registerReceiver(this, filter);
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            this.mBatteryHealthy = !batteryManagerInternal.getBatteryLevelLow();
            this.mCharging = batteryManagerInternal.isPowered(7);
        }

        boolean isOnStablePower() {
            return this.mCharging ? this.mBatteryHealthy : false;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_LOW".equals(action)) {
                this.mBatteryHealthy = false;
            } else if ("android.intent.action.BATTERY_OKAY".equals(action)) {
                this.mBatteryHealthy = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.CHARGING".equals(action)) {
                this.mCharging = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.DISCHARGING".equals(action)) {
                this.mCharging = false;
                BatteryController.this.maybeReportNewChargingState();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.BatteryController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.job.controllers.BatteryController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.BatteryController.<clinit>():void");
    }

    public static BatteryController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new BatteryController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public ChargingTracker getTracker() {
        return this.mChargeTracker;
    }

    public static BatteryController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new BatteryController(stateChangedListener, context, new Object());
    }

    private BatteryController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mTrackedTasks = new ArrayList();
        this.mChargeTracker = new ChargingTracker();
        this.mChargeTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        boolean isOnStablePower = this.mChargeTracker.isOnStablePower();
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setChargingConstraintSatisfied(isOnStablePower);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    private void maybeReportNewChargingState() {
        boolean stablePower = this.mChargeTracker.isOnStablePower();
        boolean reportChange = false;
        synchronized (this.mLock) {
            for (JobStatus ts : this.mTrackedTasks) {
                if (ts.setChargingConstraintSatisfied(stablePower) != stablePower) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
        if (stablePower) {
            this.mStateChangedListener.onRunJobNow(null);
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Battery: stable power = ");
        pw.println(this.mChargeTracker.isOnStablePower());
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
