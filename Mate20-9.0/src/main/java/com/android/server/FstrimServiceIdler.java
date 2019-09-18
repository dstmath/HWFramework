package com.android.server;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Slog;
import com.android.server.am.ProcessList;
import com.android.server.pm.PackageManagerService;
import java.util.Calendar;

public class FstrimServiceIdler extends JobService {
    private static int FSTRIM_JOB_ID = 909;
    private static int HOUR_IN_MILLIS = ProcessList.PSS_MAX_INTERVAL;
    private static int IMMED_FSTRIM_JOB_ID = 919;
    private static int MINUTE_IN_MILLIS = 60000;
    private static int PRE_FSTRIM_JOB_ID = 809;
    private static int PRE_IMMED_FSTRIM_JOB_ID = 819;
    private static final String TAG = "FstrimServiceIdler";
    private static int mFstrimJobId = FSTRIM_JOB_ID;
    /* access modifiers changed from: private */
    public static boolean mFstrimPending = false;
    private static boolean mScreenOn = true;
    /* access modifiers changed from: private */
    public static ComponentName sIdleService = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, FstrimServiceIdler.class.getName());
    /* access modifiers changed from: private */
    public Runnable mFinishCallback = new Runnable() {
        public void run() {
            Slog.i(FstrimServiceIdler.TAG, "Got fstrim service completion callback");
            synchronized (FstrimServiceIdler.this.mFinishCallback) {
                if (FstrimServiceIdler.this.mStarted) {
                    FstrimServiceIdler.this.jobFinished(FstrimServiceIdler.this.mJobParams, false);
                    boolean unused = FstrimServiceIdler.this.mStarted = false;
                }
            }
            synchronized (FstrimServiceIdler.sIdleService) {
                boolean unused2 = FstrimServiceIdler.mFstrimPending = false;
            }
            FstrimServiceIdler.schedulePreFstrim(FstrimServiceIdler.this);
        }
    };
    /* access modifiers changed from: private */
    public JobParameters mJobParams;
    /* access modifiers changed from: private */
    public boolean mStarted;

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00b4, code lost:
        if (PRE_FSTRIM_JOB_ID != r0) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b6, code lost:
        mFstrimJobId = FSTRIM_JOB_ID;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00bb, code lost:
        mFstrimJobId = IMMED_FSTRIM_JOB_ID;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00c1, code lost:
        if (mScreenOn != false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00c3, code lost:
        scheduleFstrim(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c6, code lost:
        jobFinished(r8.mJobParams, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00cb, code lost:
        return false;
     */
    public boolean onStartJob(JobParameters params) {
        this.mJobParams = params;
        int jobId = params.getJobId();
        Slog.i(TAG, "Scheduled Job " + jobId);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
        boolean z = true;
        if (PRE_FSTRIM_JOB_ID == jobId || PRE_IMMED_FSTRIM_JOB_ID == jobId) {
            synchronized (sIdleService) {
                if (mFstrimPending) {
                    return false;
                }
                mFstrimPending = true;
            }
        } else if (FSTRIM_JOB_ID != jobId && IMMED_FSTRIM_JOB_ID != jobId) {
            return false;
        } else {
            StorageManagerService ms = StorageManagerService.sSelf;
            if (ms != null) {
                if (ms.lastMaintenance() + ((long) (HOUR_IN_MILLIS * 1)) > System.currentTimeMillis()) {
                    Slog.i(TAG, "last maintenance was done in an hour, cancel Job " + jobId);
                    synchronized (sIdleService) {
                        mFstrimPending = false;
                    }
                    schedulePreFstrim(this);
                    return false;
                } else if (!mScreenOn) {
                    synchronized (this.mFinishCallback) {
                        this.mStarted = true;
                    }
                    ms.runIdleMaint(this.mFinishCallback);
                } else {
                    Slog.i(TAG, "Screen is on, igore this schedule Job " + jobId);
                    return false;
                }
            }
            if (ms == null) {
                z = false;
            }
            return z;
        }
    }

    public boolean onStopJob(JobParameters params) {
        int jobId = params.getJobId();
        if (FSTRIM_JOB_ID == jobId || IMMED_FSTRIM_JOB_ID == jobId) {
            synchronized (this.mFinishCallback) {
                this.mStarted = false;
            }
        }
        return false;
    }

    public static void setScreenOn(boolean isOn) {
        mScreenOn = isOn;
    }

    public static void scheduleFstrim(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        if (mFstrimPending && tm != null) {
            JobInfo.Builder builder = new JobInfo.Builder(mFstrimJobId, sIdleService);
            builder.setRequiresCharging(true);
            builder.setMinimumLatency((long) (30 * MINUTE_IN_MILLIS));
            tm.schedule(builder.build());
        }
    }

    public static void cancelFstrim(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        if (mFstrimPending && tm != null) {
            tm.cancel(mFstrimJobId);
        }
    }

    public static void schedulePreFstrim(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        long timeToNight = midnight().getTimeInMillis() - System.currentTimeMillis();
        Slog.i(TAG, "current time is " + System.currentTimeMillis());
        Slog.i(TAG, "timeToNight is " + timeToNight);
        if (tm != null) {
            StorageManagerService ms = StorageManagerService.sSelf;
            JobInfo.Builder builder = new JobInfo.Builder(PRE_FSTRIM_JOB_ID, sIdleService);
            builder.setRequiresCharging(true);
            builder.setMinimumLatency(timeToNight);
            tm.schedule(builder.build());
            if (ms.lastMaintenance() + ((long) (24 * HOUR_IN_MILLIS)) <= System.currentTimeMillis()) {
                JobInfo.Builder builder2 = new JobInfo.Builder(PRE_IMMED_FSTRIM_JOB_ID, sIdleService);
                builder2.setRequiresCharging(true);
                tm.schedule(builder2.build());
                return;
            }
            tm.cancel(PRE_IMMED_FSTRIM_JOB_ID);
        }
    }

    private static Calendar midnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (calendar.get(11) >= 23) {
            calendar.add(5, 1);
        }
        calendar.set(11, 23);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar;
    }
}
