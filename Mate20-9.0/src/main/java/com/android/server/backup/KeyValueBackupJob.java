package com.android.server.backup;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import java.util.Random;

public class KeyValueBackupJob extends JobService {
    private static final int JOB_ID = 20537;
    private static final long MAX_DEFERRAL = 86400000;
    private static final String TAG = "KeyValueBackupJob";
    private static ComponentName sKeyValueJobService = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, KeyValueBackupJob.class.getName());
    private static long sNextScheduled = 0;
    private static boolean sScheduled = false;

    public static void schedule(Context ctx, BackupManagerConstants constants) {
        schedule(ctx, 0, constants);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0095, code lost:
        r0 = th;
     */
    public static void schedule(Context ctx, long delay, BackupManagerConstants constants) {
        long interval;
        long fuzz;
        int networkType;
        boolean needsCharging;
        long delay2;
        synchronized (KeyValueBackupJob.class) {
            try {
                if (!sScheduled) {
                    synchronized (constants) {
                        try {
                            interval = constants.getKeyValueBackupIntervalMilliseconds();
                            fuzz = constants.getKeyValueBackupFuzzMilliseconds();
                            networkType = constants.getKeyValueBackupRequiredNetworkType();
                            needsCharging = constants.getKeyValueBackupRequireCharging();
                        } catch (Throwable th) {
                            th = th;
                        }
                    }
                    if (delay <= 0) {
                        delay2 = ((long) new Random().nextInt((int) fuzz)) + interval;
                    } else {
                        delay2 = delay;
                    }
                    try {
                        Slog.v(TAG, "Scheduling k/v pass in " + ((delay2 / 1000) / 60) + " minutes");
                        try {
                            ((JobScheduler) ctx.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(JOB_ID, sKeyValueJobService).setMinimumLatency(delay2).setRequiredNetworkType(networkType).setRequiresCharging(needsCharging).setOverrideDeadline(86400000).build());
                            sNextScheduled = System.currentTimeMillis() + delay2;
                            sScheduled = true;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        Context context = ctx;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                Context context2 = ctx;
                throw th;
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
