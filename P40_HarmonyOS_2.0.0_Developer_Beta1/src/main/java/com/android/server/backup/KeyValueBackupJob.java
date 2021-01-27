package com.android.server.backup;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.pm.PackageManagerService;
import java.util.Random;

public class KeyValueBackupJob extends JobService {
    private static final long MAX_DEFERRAL = 86400000;
    @VisibleForTesting
    public static final int MAX_JOB_ID = 52418896;
    @VisibleForTesting
    public static final int MIN_JOB_ID = 52417896;
    private static final String TAG = "KeyValueBackupJob";
    private static final String USER_ID_EXTRA_KEY = "userId";
    private static ComponentName sKeyValueJobService = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, KeyValueBackupJob.class.getName());
    @GuardedBy({"KeyValueBackupJob.class"})
    private static final SparseLongArray sNextScheduledForUserId = new SparseLongArray();
    @GuardedBy({"KeyValueBackupJob.class"})
    private static final SparseBooleanArray sScheduledForUserId = new SparseBooleanArray();

    public static void schedule(int userId, Context ctx, BackupManagerConstants constants) {
        schedule(userId, ctx, 0, constants);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b6, code lost:
        r0 = th;
     */
    public static void schedule(int userId, Context ctx, long delay, BackupManagerConstants constants) {
        Throwable th;
        long interval;
        long fuzz;
        int networkType;
        boolean needsCharging;
        long delay2;
        synchronized (KeyValueBackupJob.class) {
            try {
                if (!sScheduledForUserId.get(userId)) {
                    synchronized (constants) {
                        interval = constants.getKeyValueBackupIntervalMilliseconds();
                        fuzz = constants.getKeyValueBackupFuzzMilliseconds();
                        networkType = constants.getKeyValueBackupRequiredNetworkType();
                        needsCharging = constants.getKeyValueBackupRequireCharging();
                    }
                    if (delay <= 0) {
                        delay2 = ((long) new Random().nextInt((int) fuzz)) + interval;
                    } else {
                        delay2 = delay;
                    }
                    try {
                        Slog.v(TAG, "Scheduling k/v pass in " + ((delay2 / 1000) / 60) + " minutes");
                        JobInfo.Builder builder = new JobInfo.Builder(getJobIdForUserId(userId), sKeyValueJobService).setMinimumLatency(delay2).setRequiredNetworkType(networkType).setRequiresCharging(needsCharging).setOverrideDeadline(86400000);
                        Bundle extraInfo = new Bundle();
                        extraInfo.putInt(USER_ID_EXTRA_KEY, userId);
                        builder.setTransientExtras(extraInfo);
                        try {
                            ((JobScheduler) ctx.getSystemService("jobscheduler")).schedule(builder.build());
                            sScheduledForUserId.put(userId, true);
                            sNextScheduledForUserId.put(userId, System.currentTimeMillis() + delay2);
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                } else {
                    return;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
        while (true) {
            try {
                break;
            } catch (Throwable th5) {
                th = th5;
            }
        }
    }

    public static void cancel(int userId, Context ctx) {
        synchronized (KeyValueBackupJob.class) {
            ((JobScheduler) ctx.getSystemService("jobscheduler")).cancel(getJobIdForUserId(userId));
            clearScheduledForUserId(userId);
        }
    }

    public static long nextScheduled(int userId) {
        long j;
        synchronized (KeyValueBackupJob.class) {
            j = sNextScheduledForUserId.get(userId);
        }
        return j;
    }

    @VisibleForTesting
    public static boolean isScheduled(int userId) {
        boolean z;
        synchronized (KeyValueBackupJob.class) {
            z = sScheduledForUserId.get(userId);
        }
        return z;
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters params) {
        int userId = params.getTransientExtras().getInt(USER_ID_EXTRA_KEY);
        synchronized (KeyValueBackupJob.class) {
            clearScheduledForUserId(userId);
        }
        try {
            BackupManagerService.getInstance().backupNowForUser(userId);
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @GuardedBy({"KeyValueBackupJob.class"})
    private static void clearScheduledForUserId(int userId) {
        sScheduledForUserId.delete(userId);
        sNextScheduledForUserId.delete(userId);
    }

    private static int getJobIdForUserId(int userId) {
        return JobIdManager.getJobIdForUserId(MIN_JOB_ID, 52418896, userId);
    }
}
