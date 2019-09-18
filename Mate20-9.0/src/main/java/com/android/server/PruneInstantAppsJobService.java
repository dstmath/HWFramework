package com.android.server;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManagerInternal;
import android.os.AsyncTask;
import java.util.concurrent.TimeUnit;

public class PruneInstantAppsJobService extends JobService {
    private static final boolean DEBUG = false;
    private static final int JOB_ID = 765123;
    private static final long PRUNE_INSTANT_APPS_PERIOD_MILLIS = TimeUnit.DAYS.toMillis(1);

    public static void schedule(Context context) {
        ((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(new JobInfo.Builder(JOB_ID, new ComponentName(context.getPackageName(), PruneInstantAppsJobService.class.getName())).setRequiresDeviceIdle(true).setPeriodic(PRUNE_INSTANT_APPS_PERIOD_MILLIS).build());
    }

    public boolean onStartJob(JobParameters params) {
        AsyncTask.execute(new Runnable(params) {
            private final /* synthetic */ JobParameters f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PruneInstantAppsJobService.lambda$onStartJob$0(PruneInstantAppsJobService.this, this.f$1);
            }
        });
        return true;
    }

    public static /* synthetic */ void lambda$onStartJob$0(PruneInstantAppsJobService pruneInstantAppsJobService, JobParameters params) {
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).pruneInstantApps();
        pruneInstantAppsJobService.jobFinished(params, false);
    }

    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
