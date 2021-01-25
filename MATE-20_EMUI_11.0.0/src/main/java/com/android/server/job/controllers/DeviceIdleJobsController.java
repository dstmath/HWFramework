package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.pm.DumpState;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class DeviceIdleJobsController extends StateController {
    private static final long BACKGROUND_JOBS_DELAY = 3000;
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    static final int PROCESS_BACKGROUND_JOBS = 1;
    private static final String TAG = "JobScheduler.DeviceIdle";
    private final ArraySet<JobStatus> mAllowInIdleJobs = new ArraySet<>();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.job.controllers.DeviceIdleJobsController.AnonymousClass1 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            boolean z = false;
            switch (action.hashCode()) {
                case -712152692:
                    if (action.equals("android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -65633567:
                    if (action.equals("android.os.action.POWER_SAVE_WHITELIST_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 498807504:
                    if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 870701415:
                    if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0 || c == 1) {
                DeviceIdleJobsController deviceIdleJobsController = DeviceIdleJobsController.this;
                if (deviceIdleJobsController.mPowerManager != null && (DeviceIdleJobsController.this.mPowerManager.isDeviceIdleMode() || DeviceIdleJobsController.this.mPowerManager.isLightDeviceIdleMode())) {
                    z = true;
                }
                deviceIdleJobsController.updateIdleMode(z);
            } else if (c == 2) {
                synchronized (DeviceIdleJobsController.this.mLock) {
                    DeviceIdleJobsController.this.mDeviceIdleWhitelistAppIds = DeviceIdleJobsController.this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
                    if (DeviceIdleJobsController.DEBUG) {
                        Slog.d(DeviceIdleJobsController.TAG, "Got whitelist " + Arrays.toString(DeviceIdleJobsController.this.mDeviceIdleWhitelistAppIds));
                    }
                }
            } else if (c == 3) {
                synchronized (DeviceIdleJobsController.this.mLock) {
                    DeviceIdleJobsController.this.mPowerSaveTempWhitelistAppIds = DeviceIdleJobsController.this.mLocalDeviceIdleController.getPowerSaveTempWhitelistAppIds();
                    if (DeviceIdleJobsController.DEBUG) {
                        Slog.d(DeviceIdleJobsController.TAG, "Got temp whitelist " + Arrays.toString(DeviceIdleJobsController.this.mPowerSaveTempWhitelistAppIds));
                    }
                    boolean changed = false;
                    for (int i = 0; i < DeviceIdleJobsController.this.mAllowInIdleJobs.size(); i++) {
                        changed |= DeviceIdleJobsController.this.updateTaskStateLocked((JobStatus) DeviceIdleJobsController.this.mAllowInIdleJobs.valueAt(i));
                    }
                    if (changed) {
                        DeviceIdleJobsController.this.mStateChangedListener.onControllerStateChanged();
                    }
                }
            }
        }
    };
    private boolean mDeviceIdleMode;
    private final DeviceIdleUpdateFunctor mDeviceIdleUpdateFunctor = new DeviceIdleUpdateFunctor();
    private int[] mDeviceIdleWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
    private final SparseBooleanArray mForegroundUids = new SparseBooleanArray();
    private final DeviceIdleJobsDelayHandler mHandler = new DeviceIdleJobsDelayHandler(this.mContext.getMainLooper());
    private final DeviceIdleController.LocalService mLocalDeviceIdleController = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class));
    private final PowerManager mPowerManager = ((PowerManager) this.mContext.getSystemService("power"));
    private int[] mPowerSaveTempWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveTempWhitelistAppIds();

    public DeviceIdleJobsController(JobSchedulerService service) {
        super(service);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    /* access modifiers changed from: package-private */
    public void updateIdleMode(boolean enabled) {
        boolean changed = false;
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode != enabled) {
                changed = true;
            }
            this.mDeviceIdleMode = enabled;
            if (DEBUG) {
                Slog.d(TAG, "mDeviceIdleMode=" + this.mDeviceIdleMode);
            }
            if (enabled) {
                this.mHandler.removeMessages(1);
                this.mService.getJobStore().forEachJob(this.mDeviceIdleUpdateFunctor);
            } else {
                for (int i = 0; i < this.mForegroundUids.size(); i++) {
                    if (this.mForegroundUids.valueAt(i)) {
                        this.mService.getJobStore().forEachJobForSourceUid(this.mForegroundUids.keyAt(i), this.mDeviceIdleUpdateFunctor);
                    }
                }
                this.mHandler.sendEmptyMessageDelayed(1, 3000);
            }
        }
        if (changed) {
            this.mStateChangedListener.onDeviceIdleStateChanged(enabled);
        }
    }

    public void setUidActiveLocked(int uid, boolean active) {
        if (active != this.mForegroundUids.get(uid)) {
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("uid ");
                sb.append(uid);
                sb.append(" going ");
                sb.append(active ? "active" : "inactive");
                Slog.d(TAG, sb.toString());
            }
            this.mForegroundUids.put(uid, active);
            this.mDeviceIdleUpdateFunctor.mChanged = false;
            this.mService.getJobStore().forEachJobForSourceUid(uid, this.mDeviceIdleUpdateFunctor);
            if (this.mDeviceIdleUpdateFunctor.mChanged) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isWhitelistedLocked(JobStatus job) {
        return Arrays.binarySearch(this.mDeviceIdleWhitelistAppIds, UserHandle.getAppId(job.getSourceUid())) >= 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isTempWhitelistedLocked(JobStatus job) {
        return ArrayUtils.contains(this.mPowerSaveTempWhitelistAppIds, UserHandle.getAppId(job.getSourceUid()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateTaskStateLocked(JobStatus task) {
        boolean enableTask = true;
        boolean allowInIdle = (task.getFlags() & 2) != 0 && (this.mForegroundUids.get(task.getSourceUid()) || isTempWhitelistedLocked(task));
        boolean whitelisted = isWhitelistedLocked(task);
        if (this.mDeviceIdleMode && !whitelisted && !allowInIdle) {
            enableTask = false;
        }
        return task.setDeviceNotDozingConstraintSatisfied(enableTask, whitelisted);
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if ((jobStatus.getFlags() & 2) != 0) {
            this.mAllowInIdleJobs.add(jobStatus);
        }
        updateTaskStateLocked(jobStatus);
    }

    @Override // com.android.server.job.controllers.StateController
    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if ((jobStatus.getFlags() & 2) != 0) {
            this.mAllowInIdleJobs.remove(jobStatus);
        }
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        pw.println("Idle mode: " + this.mDeviceIdleMode);
        pw.println();
        this.mService.getJobStore().forEachJob(predicate, new Consumer(pw) {
            /* class com.android.server.job.controllers.$$Lambda$DeviceIdleJobsController$esscq8XD1L8ojfbmN1Aow_AVPk */
            private final /* synthetic */ IndentingPrintWriter f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DeviceIdleJobsController.this.lambda$dumpControllerStateLocked$0$DeviceIdleJobsController(this.f$1, (JobStatus) obj);
            }
        });
    }

    public /* synthetic */ void lambda$dumpControllerStateLocked$0$DeviceIdleJobsController(IndentingPrintWriter pw, JobStatus jobStatus) {
        pw.print("#");
        jobStatus.printUniqueId(pw);
        pw.print(" from ");
        UserHandle.formatUid(pw, jobStatus.getSourceUid());
        pw.print(": ");
        pw.print(jobStatus.getSourcePackageName());
        pw.print((jobStatus.satisfiedConstraints & DumpState.DUMP_APEX) != 0 ? " RUNNABLE" : " WAITING");
        if (jobStatus.dozeWhitelisted) {
            pw.print(" WHITELISTED");
        }
        if (this.mAllowInIdleJobs.contains(jobStatus)) {
            pw.print(" ALLOWED_IN_DOZE");
        }
        pw.println();
    }

    @Override // com.android.server.job.controllers.StateController
    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long token = proto.start(fieldId);
        long mToken = proto.start(1146756268037L);
        proto.write(1133871366145L, this.mDeviceIdleMode);
        this.mService.getJobStore().forEachJob(predicate, new Consumer(proto) {
            /* class com.android.server.job.controllers.$$Lambda$DeviceIdleJobsController$JMszgdQK87AK2bjaiI_rwQuTKpc */
            private final /* synthetic */ ProtoOutputStream f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DeviceIdleJobsController.this.lambda$dumpControllerStateLocked$1$DeviceIdleJobsController(this.f$1, (JobStatus) obj);
            }
        });
        proto.end(mToken);
        proto.end(token);
    }

    public /* synthetic */ void lambda$dumpControllerStateLocked$1$DeviceIdleJobsController(ProtoOutputStream proto, JobStatus jobStatus) {
        long jsToken = proto.start(2246267895810L);
        jobStatus.writeToShortProto(proto, 1146756268033L);
        proto.write(1120986464258L, jobStatus.getSourceUid());
        proto.write(1138166333443L, jobStatus.getSourcePackageName());
        proto.write(1133871366148L, (jobStatus.satisfiedConstraints & DumpState.DUMP_APEX) != 0);
        proto.write(1133871366149L, jobStatus.dozeWhitelisted);
        proto.write(1133871366150L, this.mAllowInIdleJobs.contains(jobStatus));
        proto.end(jsToken);
    }

    /* access modifiers changed from: package-private */
    public final class DeviceIdleUpdateFunctor implements Consumer<JobStatus> {
        boolean mChanged;

        DeviceIdleUpdateFunctor() {
        }

        public void accept(JobStatus jobStatus) {
            this.mChanged |= DeviceIdleJobsController.this.updateTaskStateLocked(jobStatus);
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeviceIdleJobsDelayHandler extends Handler {
        public DeviceIdleJobsDelayHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (DeviceIdleJobsController.this.mLock) {
                    DeviceIdleJobsController.this.mDeviceIdleUpdateFunctor.mChanged = false;
                    DeviceIdleJobsController.this.mService.getJobStore().forEachJob(DeviceIdleJobsController.this.mDeviceIdleUpdateFunctor);
                    if (DeviceIdleJobsController.this.mDeviceIdleUpdateFunctor.mChanged) {
                        DeviceIdleJobsController.this.mStateChangedListener.onControllerStateChanged();
                    }
                }
            }
        }
    }
}
