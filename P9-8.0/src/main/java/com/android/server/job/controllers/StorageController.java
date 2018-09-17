package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.ArraySet;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import com.android.server.storage.DeviceStorageMonitorService;
import java.io.PrintWriter;

public final class StorageController extends StateController {
    private static final String TAG = "JobScheduler.Stor";
    private static volatile StorageController sController;
    private static final Object sCreationLock = new Object();
    private StorageTracker mStorageTracker = new StorageTracker();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet();

    public final class StorageTracker extends BroadcastReceiver {
        private int mLastBatterySeq = -1;
        private boolean mStorageLow;

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
            filter.addAction("android.intent.action.DEVICE_STORAGE_OK");
            StorageController.this.mContext.registerReceiver(this, filter);
        }

        public boolean isStorageNotLow() {
            return this.mStorageLow ^ 1;
        }

        public int getSeq() {
            return this.mLastBatterySeq;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            this.mLastBatterySeq = intent.getIntExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mLastBatterySeq);
            if ("android.intent.action.DEVICE_STORAGE_LOW".equals(action)) {
                this.mStorageLow = true;
            } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(action)) {
                this.mStorageLow = false;
                StorageController.this.maybeReportNewStorageState();
            }
        }
    }

    public static StorageController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new StorageController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public StorageTracker getTracker() {
        return this.mStorageTracker;
    }

    public static StorageController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new StorageController(stateChangedListener, context, new Object());
    }

    private StorageController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mStorageTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (taskStatus.hasStorageNotLowConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setTrackingController(16);
            taskStatus.setStorageNotLowConstraintSatisfied(this.mStorageTracker.isStorageNotLow());
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.clearTrackingController(16)) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    private void maybeReportNewStorageState() {
        boolean storageNotLow = this.mStorageTracker.isStorageNotLow();
        boolean reportChange = false;
        synchronized (this.mLock) {
            for (int i = this.mTrackedTasks.size() - 1; i >= 0; i--) {
                if (((JobStatus) this.mTrackedTasks.valueAt(i)).setStorageNotLowConstraintSatisfied(storageNotLow) != storageNotLow) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
        if (storageNotLow) {
            this.mStateChangedListener.onRunJobNow(null);
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Storage: not low = ");
        pw.print(this.mStorageTracker.isStorageNotLow());
        pw.print(", seq=");
        pw.println(this.mStorageTracker.getSeq());
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
