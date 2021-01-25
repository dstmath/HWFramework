package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.BatteryManagerInternal;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.storage.DeviceStorageMonitorService;
import java.util.function.Predicate;

public final class BatteryController extends StateController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.Battery";
    private ChargingTracker mChargeTracker = new ChargingTracker();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet<>();

    @VisibleForTesting
    public ChargingTracker getTracker() {
        return this.mChargeTracker;
    }

    public BatteryController(JobSchedulerService service) {
        super(service);
        this.mChargeTracker.startTracking();
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasPowerConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setTrackingController(1);
            taskStatus.setChargingConstraintSatisfied(this.mChargeTracker.isOnStablePower());
            taskStatus.setBatteryNotLowConstraintSatisfied(this.mChargeTracker.isBatteryNotLow());
            taskStatus.setHwBatteryLevJobAllowedConstraintSatisfied(this.mChargeTracker.isHwBatteryLevJobAllowed());
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(1)) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeReportNewChargingStateLocked() {
        boolean stablePower = this.mChargeTracker.isOnStablePower();
        boolean batteryNotLow = this.mChargeTracker.isBatteryNotLow();
        boolean hwBatteryLevJobAllowed = this.mChargeTracker.isHwBatteryLevJobAllowed();
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewChargingStateLocked: " + stablePower);
        }
        boolean reportChange = false;
        for (int i = this.mTrackedTasks.size() - 1; i >= 0; i--) {
            JobStatus ts = this.mTrackedTasks.valueAt(i);
            if (ts.setChargingConstraintSatisfied(stablePower) != stablePower) {
                reportChange = true;
            }
            if (ts.setBatteryNotLowConstraintSatisfied(batteryNotLow) != batteryNotLow) {
                reportChange = true;
            }
            if (ts.setHwBatteryLevJobAllowedConstraintSatisfied(hwBatteryLevJobAllowed) != hwBatteryLevJobAllowed) {
                reportChange = true;
            }
        }
        if (stablePower || batteryNotLow || hwBatteryLevJobAllowed) {
            this.mStateChangedListener.onRunJobNow(null);
        } else if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
    }

    public final class ChargingTracker extends BroadcastReceiver {
        private boolean mBatteryHealthy;
        private boolean mCharging;
        private boolean mHwBatteryLevJobAllowed;
        private int mLastBatterySeq = -1;
        private BroadcastReceiver mMonitor;

        public ChargingTracker() {
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction("android.intent.action.BATTERY_OKAY");
            filter.addAction("com.huawei.intent.action.BATTERY_LEV_JOB_ALLOWED");
            filter.addAction("com.huawei.intent.action.BATTERY_LEV_JOB_NOT_ALLOWED");
            filter.addAction("android.os.action.CHARGING");
            filter.addAction("android.os.action.DISCHARGING");
            BatteryController.this.mContext.registerReceiver(this, filter);
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            boolean z = true;
            this.mBatteryHealthy = !batteryManagerInternal.getBatteryLevelLow();
            this.mCharging = batteryManagerInternal.isPowered(7);
            if (BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED <= 0 || batteryManagerInternal.getBatteryLevel() < BatteryManager.HW_BATTERY_LEV_JOB_ALLOWED) {
                z = false;
            }
            this.mHwBatteryLevJobAllowed = z;
        }

        public void setMonitorBatteryLocked(boolean enabled) {
            if (enabled) {
                if (this.mMonitor == null) {
                    this.mMonitor = new BroadcastReceiver() {
                        /* class com.android.server.job.controllers.BatteryController.ChargingTracker.AnonymousClass1 */

                        @Override // android.content.BroadcastReceiver
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

        public boolean isHwBatteryLevJobAllowed() {
            return this.mHwBatteryLevJobAllowed;
        }

        public boolean isOnStablePower() {
            return this.mCharging && this.mBatteryHealthy;
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

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        @VisibleForTesting
        public void onReceiveInternal(Intent intent) {
            synchronized (BatteryController.this.mLock) {
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_LOW".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Battery life too low to do work. @ " + JobSchedulerService.sElapsedRealtimeClock.millis());
                    }
                    this.mBatteryHealthy = false;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.intent.action.BATTERY_OKAY".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Battery life healthy enough to do work. @ " + JobSchedulerService.sElapsedRealtimeClock.millis());
                    }
                    this.mBatteryHealthy = true;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.os.action.CHARGING".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Received charging intent, fired @ " + JobSchedulerService.sElapsedRealtimeClock.millis());
                    }
                    this.mCharging = true;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("android.os.action.DISCHARGING".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Disconnected from power.");
                    }
                    this.mCharging = false;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("com.huawei.intent.action.BATTERY_LEV_JOB_ALLOWED".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Battery level is above job allowed level");
                    }
                    this.mHwBatteryLevJobAllowed = true;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                } else if ("com.huawei.intent.action.BATTERY_LEV_JOB_NOT_ALLOWED".equals(action)) {
                    if (BatteryController.DEBUG) {
                        Slog.d(BatteryController.TAG, "Battery level is below job allowed level");
                    }
                    this.mHwBatteryLevJobAllowed = false;
                    BatteryController.this.maybeReportNewChargingStateLocked();
                }
                this.mLastBatterySeq = intent.getIntExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mLastBatterySeq);
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        pw.println("Stable power: " + this.mChargeTracker.isOnStablePower());
        pw.println("Not low: " + this.mChargeTracker.isBatteryNotLow());
        if (this.mChargeTracker.isMonitoring()) {
            pw.print("MONITORING: seq=");
            pw.println(this.mChargeTracker.getSeq());
        }
        pw.println();
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = this.mTrackedTasks.valueAt(i);
            if (predicate.test(js)) {
                pw.print("#");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.println();
            }
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long token = proto.start(fieldId);
        long mToken = proto.start(1146756268034L);
        proto.write(1133871366145L, this.mChargeTracker.isOnStablePower());
        proto.write(1133871366146L, this.mChargeTracker.isBatteryNotLow());
        proto.write(1133871366147L, this.mChargeTracker.isMonitoring());
        proto.write(1120986464260L, this.mChargeTracker.getSeq());
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = this.mTrackedTasks.valueAt(i);
            if (predicate.test(js)) {
                long jsToken = proto.start(2246267895813L);
                js.writeToShortProto(proto, 1146756268033L);
                proto.write(1120986464258L, js.getSourceUid());
                proto.end(jsToken);
            }
        }
        proto.end(mToken);
        proto.end(token);
    }
}
