package com.android.server;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManagerInternal;
import java.util.concurrent.TimeUnit;

public class PruneInstantAppsJobService extends JobService {
    private static final boolean DEBUG = false;
    private static final int JOB_ID = 765123;
    private static final long PRUNE_INSTANT_APPS_PERIOD_MILLIS = TimeUnit.DAYS.toMillis(1);

    public static void schedule(Context context) {
        ((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(new Builder(JOB_ID, new ComponentName(context.getPackageName(), PruneInstantAppsJobService.class.getName())).setRequiresDeviceIdle(true).setPeriodic(PRUNE_INSTANT_APPS_PERIOD_MILLIS).build());
    }

    public boolean onStartJob(JobParameters params) {
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).pruneInstantApps();
        jobFinished(params, false);
        return true;
    }

    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
