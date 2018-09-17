package com.android.server.backup;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Random;

public class KeyValueBackupJob extends JobService {
    static final long BATCH_INTERVAL = 14400000;
    private static final int FUZZ_MILLIS = 600000;
    private static final int JOB_ID = 20537;
    private static final long MAX_DEFERRAL = 86400000;
    private static final String TAG = "KeyValueBackupJob";
    private static ComponentName sKeyValueJobService = new ComponentName("android", KeyValueBackupJob.class.getName());
    private static long sNextScheduled = 0;
    private static boolean sScheduled = false;

    public static void schedule(Context ctx) {
        schedule(ctx, 0);
    }

    public static void schedule(Context ctx, long delay) {
        synchronized (KeyValueBackupJob.class) {
            if (!sScheduled) {
                if (delay <= 0) {
                    delay = BATCH_INTERVAL + ((long) new Random().nextInt(600000));
                }
                if (BackupManagerService.DEBUG_SCHEDULING) {
                    Slog.v(TAG, "Scheduling k/v pass in " + ((delay / 1000) / 60) + " minutes");
                }
                ((JobScheduler) ctx.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sKeyValueJobService).setMinimumLatency(delay).setRequiredNetworkType(1).setRequiresCharging(true).setOverrideDeadline(86400000).build());
                sNextScheduled = System.currentTimeMillis() + delay;
                sScheduled = true;
            }
        }
    }

    public static void cancel(Context ctx) {
        synchronized (KeyValueBackupJob.class) {
            ((JobScheduler) ctx.getSystemService("jobscheduler")).cancel(JOB_ID);
            sNextScheduled = 0;
            sScheduled = false;
        }
    }

    public static long nextScheduled() {
        long j;
        synchronized (KeyValueBackupJob.class) {
            j = sNextScheduled;
        }
        return j;
    }

    public boolean onStartJob(JobParameters params) {
        synchronized (KeyValueBackupJob.class) {
            sNextScheduled = 0;
            sScheduled = false;
        }
        try {
            BackupManagerService.getInstance().backupNow();
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
