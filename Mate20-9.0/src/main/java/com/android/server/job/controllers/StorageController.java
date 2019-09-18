package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.job.JobSchedulerService;
import com.android.server.storage.DeviceStorageMonitorService;
import java.util.function.Predicate;

public final class StorageController extends StateController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.Storage";
    private final StorageTracker mStorageTracker = new StorageTracker();
    private final ArraySet<JobStatus> mTrackedTasks = new ArraySet<>();

    public final class StorageTracker extends BroadcastReceiver {
        private int mLastStorageSeq = -1;
        private boolean mStorageLow;

        public StorageTracker() {
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
            filter.addAction("android.intent.action.DEVICE_STORAGE_OK");
            StorageController.this.mContext.registerReceiver(this, filter);
        }

        public boolean isStorageNotLow() {
            return !this.mStorageLow;
        }

        public int getSeq() {
            return this.mLastStorageSeq;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        @VisibleForTesting
        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            this.mLastStorageSeq = intent.getIntExtra(DeviceStorageMonitorService.EXTRA_SEQUENCE, this.mLastStorageSeq);
            if ("android.intent.action.DEVICE_STORAGE_LOW".equals(action)) {
                if (StorageController.DEBUG) {
                    Slog.d(StorageController.TAG, "Available storage too low to do work. @ " + JobSchedulerService.sElapsedRealtimeClock.millis());
                }
                this.mStorageLow = true;
            } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(action)) {
                if (StorageController.DEBUG) {
                    Slog.d(StorageController.TAG, "Available stoage high enough to do work. @ " + JobSchedulerService.sElapsedRealtimeClock.millis());
                }
                this.mStorageLow = false;
                StorageController.this.maybeReportNewStorageState();
            }
        }
    }

    @VisibleForTesting
    public StorageTracker getTracker() {
        return this.mStorageTracker;
    }

    public StorageController(JobSchedulerService service) {
        super(service);
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

    /* access modifiers changed from: private */
    public void maybeReportNewStorageState() {
        boolean storageNotLow = this.mStorageTracker.isStorageNotLow();
        boolean reportChange = false;
        synchronized (this.mLock) {
            for (int i = this.mTrackedTasks.size() - 1; i >= 0; i--) {
                if (this.mTrackedTasks.valueAt(i).setStorageNotLowConstraintSatisfied(storageNotLow) != storageNotLow) {
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

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        pw.println("Not low: " + this.mStorageTracker.isStorageNotLow());
        pw.println("Sequence: " + this.mStorageTracker.getSeq());
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

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        ProtoOutputStream protoOutputStream = proto;
        long token = proto.start(fieldId);
        long mToken = protoOutputStream.start(1146756268039L);
        protoOutputStream.write(1133871366145L, this.mStorageTracker.isStorageNotLow());
        protoOutputStream.write(1120986464258L, this.mStorageTracker.getSeq());
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = this.mTrackedTasks.valueAt(i);
            if (predicate.test(js)) {
                long jsToken = protoOutputStream.start(2246267895811L);
                js.writeToShortProto(protoOutputStream, 1146756268033L);
                protoOutputStream.write(1120986464258L, js.getSourceUid());
                protoOutputStream.end(jsToken);
            }
        }
        Predicate<JobStatus> predicate2 = predicate;
        protoOutputStream.end(mToken);
        protoOutputStream.end(token);
    }
}
