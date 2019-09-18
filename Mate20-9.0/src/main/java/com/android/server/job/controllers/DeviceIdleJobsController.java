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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    static final int PROCESS_BACKGROUND_JOBS = 1;
    private static final String TAG = "JobScheduler.DeviceIdle";
    /* access modifiers changed from: private */
    public final ArraySet<JobStatus> mAllowInIdleJobs = new ArraySet<>();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004d  */
        /* JADX WARNING: Removed duplicated region for block: B:39:0x00ba  */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x00f9  */
        /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            boolean z = false;
            if (hashCode == -712152692) {
                if (action.equals("android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED")) {
                    c = 3;
                    switch (c) {
                        case 0:
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == -65633567) {
                if (action.equals("android.os.action.POWER_SAVE_WHITELIST_CHANGED")) {
                    c = 2;
                    switch (c) {
                        case 0:
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 498807504) {
                if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                    c = 0;
                    switch (c) {
                        case 0:
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } else if (hashCode == 870701415 && action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                c = 1;
                switch (c) {
                    case 0:
                    case 1:
                        DeviceIdleJobsController deviceIdleJobsController = DeviceIdleJobsController.this;
                        if (DeviceIdleJobsController.this.mPowerManager != null && (DeviceIdleJobsController.this.mPowerManager.isDeviceIdleMode() || DeviceIdleJobsController.this.mPowerManager.isLightDeviceIdleMode())) {
                            z = true;
                        }
                        deviceIdleJobsController.updateIdleMode(z);
                        return;
                    case 2:
                        synchronized (DeviceIdleJobsController.this.mLock) {
                            int[] unused = DeviceIdleJobsController.this.mDeviceIdleWhitelistAppIds = DeviceIdleJobsController.this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
                            if (DeviceIdleJobsController.DEBUG) {
                                Slog.d(DeviceIdleJobsController.TAG, "Got whitelist " + Arrays.toString(DeviceIdleJobsController.this.mDeviceIdleWhitelistAppIds));
                            }
                        }
                        return;
                    case 3:
                        synchronized (DeviceIdleJobsController.this.mLock) {
                            int[] unused2 = DeviceIdleJobsController.this.mPowerSaveTempWhitelistAppIds = DeviceIdleJobsController.this.mLocalDeviceIdleController.getPowerSaveTempWhitelistAppIds();
                            if (DeviceIdleJobsController.DEBUG) {
                                Slog.d(DeviceIdleJobsController.TAG, "Got temp whitelist " + Arrays.toString(DeviceIdleJobsController.this.mPowerSaveTempWhitelistAppIds));
                            }
                            boolean changed = false;
                            while (true) {
                                int i = z;
                                if (i < DeviceIdleJobsController.this.mAllowInIdleJobs.size()) {
                                    changed |= DeviceIdleJobsController.this.updateTaskStateLocked((JobStatus) DeviceIdleJobsController.this.mAllowInIdleJobs.valueAt(i));
                                    z = i + 1;
                                } else if (changed) {
                                    DeviceIdleJobsController.this.mStateChangedListener.onControllerStateChanged();
                                }
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    };
    private boolean mDeviceIdleMode;
    /* access modifiers changed from: private */
    public final DeviceIdleUpdateFunctor mDeviceIdleUpdateFunctor = new DeviceIdleUpdateFunctor();
    /* access modifiers changed from: private */
    public int[] mDeviceIdleWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
    private final SparseBooleanArray mForegroundUids = new SparseBooleanArray();
    private final DeviceIdleJobsDelayHandler mHandler = new DeviceIdleJobsDelayHandler(this.mContext.getMainLooper());
    /* access modifiers changed from: private */
    public final DeviceIdleController.LocalService mLocalDeviceIdleController = ((DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class));
    /* access modifiers changed from: private */
    public final PowerManager mPowerManager = ((PowerManager) this.mContext.getSystemService("power"));
    /* access modifiers changed from: private */
    public int[] mPowerSaveTempWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveTempWhitelistAppIds();

    final class DeviceIdleJobsDelayHandler extends Handler {
        public DeviceIdleJobsDelayHandler(Looper looper) {
            super(looper);
        }

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

    final class DeviceIdleUpdateFunctor implements Consumer<JobStatus> {
        boolean mChanged;

        DeviceIdleUpdateFunctor() {
        }

        public void accept(JobStatus jobStatus) {
            this.mChanged |= DeviceIdleJobsController.this.updateTaskStateLocked(jobStatus);
        }
    }

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
                this.mHandler.sendEmptyMessageDelayed(1, BACKGROUND_JOBS_DELAY);
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
    public boolean updateTaskStateLocked(JobStatus task) {
        boolean enableTask = false;
        boolean allowInIdle = (task.getFlags() & 2) != 0 && (this.mForegroundUids.get(task.getSourceUid()) || isTempWhitelistedLocked(task));
        boolean whitelisted = isWhitelistedLocked(task);
        if (!this.mDeviceIdleMode || whitelisted || allowInIdle) {
            enableTask = true;
        }
        return task.setDeviceNotDozingConstraintSatisfied(enableTask, whitelisted);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if ((jobStatus.getFlags() & 2) != 0) {
            this.mAllowInIdleJobs.add(jobStatus);
        }
        updateTaskStateLocked(jobStatus);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
        if ((jobStatus.getFlags() & 2) != 0) {
            this.mAllowInIdleJobs.remove(jobStatus);
        }
    }

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
        pw.println("Idle mode: " + this.mDeviceIdleMode);
        pw.println();
        this.mService.getJobStore().forEachJob(predicate, (Consumer<JobStatus>) new Consumer(pw) {
            private final /* synthetic */ IndentingPrintWriter f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                DeviceIdleJobsController.lambda$dumpControllerStateLocked$0(DeviceIdleJobsController.this, this.f$1, (JobStatus) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$dumpControllerStateLocked$0(DeviceIdleJobsController deviceIdleJobsController, IndentingPrintWriter pw, JobStatus jobStatus) {
        pw.print("#");
        jobStatus.printUniqueId(pw);
        pw.print(" from ");
        UserHandle.formatUid(pw, jobStatus.getSourceUid());
        pw.print(": ");
        pw.print(jobStatus.getSourcePackageName());
        pw.print((jobStatus.satisfiedConstraints & DumpState.DUMP_HANDLE) != 0 ? " RUNNABLE" : " WAITING");
        if (jobStatus.dozeWhitelisted) {
            pw.print(" WHITELISTED");
        }
        if (deviceIdleJobsController.mAllowInIdleJobs.contains(jobStatus)) {
            pw.print(" ALLOWED_IN_DOZE");
        }
        pw.println();
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
        long token = proto.start(fieldId);
        long mToken = proto.start(1146756268037L);
        proto.write(1133871366145L, this.mDeviceIdleMode);
        this.mService.getJobStore().forEachJob(predicate, (Consumer<JobStatus>) new Consumer(proto) {
            private final /* synthetic */ ProtoOutputStream f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                DeviceIdleJobsController.lambda$dumpControllerStateLocked$1(DeviceIdleJobsController.this, this.f$1, (JobStatus) obj);
            }
        });
        proto.end(mToken);
        proto.end(token);
    }

    public static /* synthetic */ void lambda$dumpControllerStateLocked$1(DeviceIdleJobsController deviceIdleJobsController, ProtoOutputStream proto, JobStatus jobStatus) {
        long jsToken = proto.start(2246267895810L);
        jobStatus.writeToShortProto(proto, 1146756268033L);
        proto.write(1120986464258L, jobStatus.getSourceUid());
        proto.write(1138166333443L, jobStatus.getSourcePackageName());
        proto.write(1133871366148L, (jobStatus.satisfiedConstraints & DumpState.DUMP_HANDLE) != 0);
        proto.write(1133871366149L, jobStatus.dozeWhitelisted);
        proto.write(1133871366150L, deviceIdleJobsController.mAllowInIdleJobs.contains(jobStatus));
        proto.end(jsToken);
    }
}
