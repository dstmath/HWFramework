package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.UserHandle;
import android.util.ArraySet;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import com.android.server.storage.DeviceStorageMonitorService;
import java.io.PrintWriter;

public final class BatteryController extends StateController {
    private static final String TAG = "JobScheduler.Batt";
    private static volatile BatteryController sController;
    private static final Object sCreationLock = new Object();
    private ChargingTracker mChargeTracker = new ChargingTracker();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet();

    public final class ChargingTracker extends BroadcastReceiver {
        private boolean mBatteryHealthy;
        private boolean mCharging;
        private int mLastBatterySeq = -1;
        private BroadcastReceiver mMonitor;

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction("android.intent.action.BATTERY_OKAY");
            filter.addAction("android.os.action.CHARGING");
            filter.addAction("android.os.action.DISCHARGING");
            BatteryController.this.mContext.registerReceiver(this, filter);
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            this.mBatteryHealthy = batteryManagerInternal.getBatteryLevelLow() ^ 1;
            this.mCharging = batteryManagerInternal.isPowered(7);
        }

        public void setMonitorBatteryLocked(boolean enabled) {
            if (enabled) {
                if (this.mMonitor == null) {
                    this.mMonitor = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            ChargingTracker.this.onReceive(context, intent);
                        }
                    };
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.intent.action.BATTERY_CHANGED");
                    BatteryController.this.mContext.registerReceiver(this.mMonitor, filter);
                }
            } else if (this.mMonitor != null) {
                BatteryController.this.mContext.unregisterReceiver(this.mMonitor);
                this.mMonitor = null;
            }
        }

        public boolean isOnStablePower() {
            return this.mCharging ? this.mBatteryHealthy : false;
        }

        public boolean isBatteryNotLow() {
            return this.mBatteryHealthy;
        }

        public boolean isMonitoring() {
            return this.mMonitor != null;
        }

        public int getSeq() {
            return this.mLastBatterySeq;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            synchronized (BatteryController.this.mLock) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_LOW".equals(action)) {
                    this.mBatteryHealthy = false;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.intent.action.BATTERY_OKAY".equals(action)) {
                    this.mBatteryHealthy = true;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.os.action.CHARGING".equals(action)) {
                    this.mCharging = true;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.os.action.DISCHARGING".equals(action)) {
                    this.mCharging = false;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                }
                this.mLastBatterySeq = intent.getIntExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mLastBatterySeq);
            }
        }
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
        this.mChargeTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasPowerConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setTrackingController(1);
            taskStatus.setChargingConstraintSatisfied(this.mChargeTracker.isOnStablePower());
            taskStatus.setBatteryNotLowConstraintSatisfied(this.mChargeTracker.isBatteryNotLow());
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(1)) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    private void maybeReportNewChargingStateLocked() {
        boolean stablePower = this.mChargeTracker.isOnStablePower();
        boolean batteryNotLow = this.mChargeTracker.isBatteryNotLow();
        boolean reportChange = false;
        for (int i = this.mTrackedTasks.size() - 1; i >= 0; i--) {
            JobStatus ts = (JobStatus) this.mTrackedTasks.valueAt(i);
            if (ts.setChargingConstraintSatisfied(stablePower) != stablePower) {
                reportChange = true;
            }
            if (ts.setBatteryNotLowConstraintSatisfied(batteryNotLow) != batteryNotLow) {
                reportChange = true;
            }
        }
        if (stablePower || batteryNotLow) {
            this.mStateChangedListener.onRunJobNow(null);
        } else if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Battery: stable power = ");
        pw.print(this.mChargeTracker.isOnStablePower());
        pw.print(", not low = ");
        pw.println(this.mChargeTracker.isBatteryNotLow());
        if (this.mChargeTracker.isMonitoring()) {
            pw.print("MONITORING: seq=");
            pw.println(this.mChargeTracker.getSeq());
        }
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
