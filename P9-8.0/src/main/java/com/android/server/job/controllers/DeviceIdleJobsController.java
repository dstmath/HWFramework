package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.UserHandle;
import com.android.internal.util.ArrayUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore.JobStatusFunctor;
import java.io.PrintWriter;

public final class DeviceIdleJobsController extends StateController {
    private static final boolean LOG_DEBUG = false;
    private static final String LOG_TAG = "DeviceIdleJobsController";
    private static DeviceIdleJobsController sController;
    private static Object sCreationLock = new Object();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
                boolean z;
                DeviceIdleJobsController deviceIdleJobsController = DeviceIdleJobsController.this;
                if (DeviceIdleJobsController.this.mPowerManager == null) {
                    z = false;
                } else if (DeviceIdleJobsController.this.mPowerManager.isDeviceIdleMode()) {
                    z = true;
                } else {
                    z = DeviceIdleJobsController.this.mPowerManager.isLightDeviceIdleMode();
                }
                deviceIdleJobsController.updateIdleMode(z);
            } else if ("android.os.action.POWER_SAVE_WHITELIST_CHANGED".equals(action)) {
                DeviceIdleJobsController.this.updateWhitelist();
            }
        }
    };
    private boolean mDeviceIdleMode;
    private int[] mDeviceIdleWhitelistAppIds;
    private final JobSchedulerService mJobSchedulerService;
    private final LocalService mLocalDeviceIdleController;
    private final PowerManager mPowerManager;
    final JobStatusFunctor mUpdateFunctor = new JobStatusFunctor() {
        public void process(JobStatus jobStatus) {
            DeviceIdleJobsController.this.updateTaskStateLocked(jobStatus);
        }
    };

    public static DeviceIdleJobsController get(JobSchedulerService service) {
        DeviceIdleJobsController deviceIdleJobsController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new DeviceIdleJobsController(service, service.getContext(), service.getLock());
            }
            deviceIdleJobsController = sController;
        }
        return deviceIdleJobsController;
    }

    private DeviceIdleJobsController(JobSchedulerService jobSchedulerService, Context context, Object lock) {
        super(jobSchedulerService, context, lock);
        this.mJobSchedulerService = jobSchedulerService;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mLocalDeviceIdleController = (LocalService) LocalServices.getService(LocalService.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    void updateIdleMode(boolean enabled) {
        boolean changed = false;
        if (this.mDeviceIdleWhitelistAppIds == null) {
            updateWhitelist();
        }
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode != enabled) {
                changed = true;
            }
            this.mDeviceIdleMode = enabled;
            this.mJobSchedulerService.getJobStore().forEachJob(this.mUpdateFunctor);
        }
        if (changed) {
            this.mStateChangedListener.onDeviceIdleStateChanged(enabled);
        }
    }

    void updateWhitelist() {
        synchronized (this.mLock) {
            if (this.mLocalDeviceIdleController != null) {
                this.mDeviceIdleWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
            }
        }
    }

    boolean isWhitelistedLocked(JobStatus job) {
        if (this.mDeviceIdleWhitelistAppIds == null || !ArrayUtils.contains(this.mDeviceIdleWhitelistAppIds, UserHandle.getAppId(job.getSourceUid()))) {
            return false;
        }
        return true;
    }

    private void updateTaskStateLocked(JobStatus task) {
        boolean whitelisted = isWhitelistedLocked(task);
        task.setDeviceNotDozingConstraintSatisfied(this.mDeviceIdleMode ? whitelisted : true, whitelisted);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        updateTaskStateLocked(jobStatus);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(final PrintWriter pw, final int filterUid) {
        pw.println(LOG_TAG);
        this.mJobSchedulerService.getJobStore().forEachJob(new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                if (jobStatus.shouldDump(filterUid)) {
                    pw.print("  #");
                    jobStatus.printUniqueId(pw);
                    pw.print(" from ");
                    UserHandle.formatUid(pw, jobStatus.getSourceUid());
                    pw.print(": ");
                    pw.print(jobStatus.getSourcePackageName());
                    pw.print((jobStatus.satisfiedConstraints & 33554432) != 0 ? " RUNNABLE" : " WAITING");
                    if (jobStatus.dozeWhitelisted) {
                        pw.print(" WHITELISTED");
                    }
                    pw.println();
                }
            }
        });
    }
}
