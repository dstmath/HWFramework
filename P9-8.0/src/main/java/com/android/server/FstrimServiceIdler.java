package com.android.server;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Slog;
import java.util.Calendar;

public class FstrimServiceIdler extends JobService {
    private static int FSTRIM_JOB_ID = 909;
    private static int HOUR_IN_MILLIS = 3600000;
    private static int MINUTE_IN_MILLIS = 60000;
    private static int PRE_FSTRIM_JOB_ID = 809;
    private static final String TAG = "FstrimServiceIdler";
    private static int mFstrimJobId = FSTRIM_JOB_ID;
    private static boolean mFstrimPending = false;
    private static boolean mScreenOn = true;
    private static ComponentName sIdleService = new ComponentName("android", FstrimServiceIdler.class.getName());
    private Runnable mFinishCallback = new Runnable() {
        public void run() {
            Slog.i(FstrimServiceIdler.TAG, "Got fstrim service completion callback");
            synchronized (FstrimServiceIdler.this.mFinishCallback) {
                if (FstrimServiceIdler.this.mStarted) {
                    FstrimServiceIdler.this.jobFinished(FstrimServiceIdler.this.mJobParams, false);
                    FstrimServiceIdler.this.mStarted = false;
                }
            }
            FstrimServiceIdler.mFstrimPending = false;
            FstrimServiceIdler.schedulePreFstrim(FstrimServiceIdler.this);
        }
    };
    private JobParameters mJobParams;
    private boolean mStarted;

    /* JADX WARNING: Missing block: B:15:0x0039, code:
            mFstrimJobId = (FSTRIM_JOB_ID + r0) - PRE_FSTRIM_JOB_ID;
     */
    /* JADX WARNING: Missing block: B:16:0x0043, code:
            if (mScreenOn != false) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:17:0x0045, code:
            scheduleFstrim(r10);
     */
    /* JADX WARNING: Missing block: B:18:0x0048, code:
            jobFinished(r10.mJobParams, false);
     */
    /* JADX WARNING: Missing block: B:19:0x004d, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:41:0x009a, code:
            r1.runIdleMaintenance(r10.mFinishCallback, r0 - FSTRIM_JOB_ID);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onStartJob(JobParameters params) {
        boolean z = true;
        this.mJobParams = params;
        int jobId = params.getJobId();
        Slog.i(TAG, "Scheduled Job " + jobId);
        if (PRE_FSTRIM_JOB_ID == jobId || PRE_FSTRIM_JOB_ID + 10 == jobId) {
            synchronized (this.mFinishCallback) {
                if (mFstrimPending) {
                    return false;
                }
                mFstrimPending = true;
            }
        } else if (FSTRIM_JOB_ID != jobId && FSTRIM_JOB_ID + 10 != jobId) {
            return false;
        } else {
            StorageManagerService ms = StorageManagerService.sSelf;
            if (ms != null) {
                synchronized (this.mFinishCallback) {
                    if (ms.lastMaintenance() + ((long) (HOUR_IN_MILLIS * 1)) > System.currentTimeMillis()) {
                        Slog.i(TAG, "last maintenance was done in an hour, cancel Job " + jobId);
                        mFstrimPending = false;
                        schedulePreFstrim(this);
                        return false;
                    }
                    this.mStarted = true;
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
        if (FSTRIM_JOB_ID == jobId || FSTRIM_JOB_ID + 10 == jobId) {
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
            Builder builder = new Builder(mFstrimJobId, sIdleService);
            builder.setRequiresCharging(true);
            builder.setMinimumLatency((long) (MINUTE_IN_MILLIS * 30));
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
        if (tm != null) {
            StorageManagerService ms = StorageManagerService.sSelf;
            Builder builder = new Builder(PRE_FSTRIM_JOB_ID, sIdleService);
            builder.setRequiresCharging(true);
            builder.setMinimumLatency(timeToNight);
            tm.schedule(builder.build());
            if (ms.lastMaintenance() + ((long) (HOUR_IN_MILLIS * 72)) <= System.currentTimeMillis()) {
                builder = new Builder(PRE_FSTRIM_JOB_ID + 10, sIdleService);
                builder.setRequiresCharging(true);
                tm.schedule(builder.build());
                return;
            }
            tm.cancel(PRE_FSTRIM_JOB_ID + 10);
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
