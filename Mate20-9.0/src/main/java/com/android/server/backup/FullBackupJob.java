package com.android.server.backup;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import com.android.server.pm.PackageManagerService;

public class FullBackupJob extends JobService {
    private static final boolean DEBUG = true;
    private static final int JOB_ID = 20536;
    private static final String TAG = "FullBackupJob";
    private static ComponentName sIdleService = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, FullBackupJob.class.getName());
    JobParameters mParams;

    public static void schedule(Context ctx, long minDelay, BackupManagerConstants constants) {
        JobScheduler js = (JobScheduler) ctx.getSystemService("jobscheduler");
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, sIdleService);
        synchronized (constants) {
            builder.setRequiresDeviceIdle(true).setRequiredNetworkType(constants.getFullBackupRequiredNetworkType()).setRequiresCharging(constants.getFullBackupRequireCharging());
        }
        if (minDelay > 0) {
            builder.setMinimumLatency(minDelay);
        }
        js.schedule(builder.build());
    }

    public void finishBackupPass() {
        if (this.mParams != null) {
            jobFinished(this.mParams, false);
            this.mParams = null;
        }
    }

    public boolean onStartJob(JobParameters params) {
        this.mParams = params;
        return BackupManagerService.getInstance().beginFullBackup(this);
    }

    public boolean onStopJob(JobParameters params) {
        if (this.mParams != null) {
            this.mParams = null;
            BackupManagerService.getInstance().endFullBackup();
        }
        return false;
    }
}
