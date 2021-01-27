package com.android.server.content;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.slice.SliceClientPermissions;

public class SyncJobService extends JobService {
    private static final String TAG = "SyncManager";
    @GuardedBy({"sLock"})
    private static SyncJobService sInstance;
    @GuardedBy({"sLock"})
    private static final SparseArray<JobParameters> sJobParamsMap = new SparseArray<>();
    @GuardedBy({"sLock"})
    private static final SparseLongArray sJobStartUptimes = new SparseLongArray();
    private static final Object sLock = new Object();
    private static final SyncLogger sLogger = SyncLogger.getInstance();
    @GuardedBy({"sLock"})
    private static final SparseBooleanArray sStartedSyncs = new SparseBooleanArray();

    private void updateInstance() {
        synchronized (SyncJobService.class) {
            sInstance = this;
        }
    }

    private static SyncJobService getInstance() {
        SyncJobService syncJobService;
        synchronized (sLock) {
            if (sInstance == null) {
                Slog.wtf("SyncManager", "sInstance == null");
            }
            syncJobService = sInstance;
        }
        return syncJobService;
    }

    public static boolean isReady() {
        boolean z;
        synchronized (sLock) {
            z = sInstance != null;
        }
        return z;
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters params) {
        updateInstance();
        sLogger.purgeOldLogs();
        SyncOperation op = SyncOperation.maybeCreateFromJobExtras(params.getExtras());
        if (op == null) {
            Slog.wtf("SyncManager", "Got invalid job " + params.getJobId());
            return false;
        }
        boolean readyToSync = SyncManager.readyToSync(op.target.userId);
        sLogger.log("onStartJob() jobid=", Integer.valueOf(params.getJobId()), " op=", op, " readyToSync", Boolean.valueOf(readyToSync));
        if (!readyToSync) {
            jobFinished(params, !op.isPeriodic);
            return true;
        }
        boolean isLoggable = Log.isLoggable("SyncManager", 2);
        synchronized (sLock) {
            int jobId = params.getJobId();
            sJobParamsMap.put(jobId, params);
            sStartedSyncs.delete(jobId);
            sJobStartUptimes.put(jobId, SystemClock.uptimeMillis());
        }
        Message m = Message.obtain();
        m.what = 10;
        if (isLoggable) {
            Slog.v("SyncManager", "Got start job message " + op.target);
        }
        m.obj = op;
        SyncManager.sendMessage(m);
        return true;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters params) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "onStopJob called " + params.getJobId() + ", reason: " + params.getStopReason());
        }
        SyncOperation op = SyncOperation.maybeCreateFromJobExtras(params.getExtras());
        if (op == null) {
            Slog.wtf("SyncManager", "Got invalid job " + params.getJobId());
            return false;
        }
        boolean readyToSync = SyncManager.readyToSync(op.target.userId);
        SyncLogger syncLogger = sLogger;
        int i = 1;
        syncLogger.log("onStopJob() ", syncLogger.jobParametersToString(params), " readyToSync=", Boolean.valueOf(readyToSync));
        synchronized (sLock) {
            int jobId = params.getJobId();
            sJobParamsMap.remove(jobId);
            long startUptime = sJobStartUptimes.get(jobId);
            long nowUptime = SystemClock.uptimeMillis();
            if (nowUptime - startUptime > 60000 && readyToSync && !sStartedSyncs.get(jobId)) {
                wtf("Job " + jobId + " didn't start:  startUptime=" + startUptime + " nowUptime=" + nowUptime + " params=" + jobParametersToString(params));
            }
            sStartedSyncs.delete(jobId);
            sJobStartUptimes.delete(jobId);
        }
        Message m = Message.obtain();
        m.what = 11;
        m.obj = op;
        m.arg1 = params.getStopReason() != 0 ? 1 : 0;
        if (params.getStopReason() != 3) {
            i = 0;
        }
        m.arg2 = i;
        SyncManager.sendMessage(m);
        return false;
    }

    public static void callJobFinished(int jobId, boolean needsReschedule, String why) {
        SyncJobService instance = getInstance();
        if (instance != null) {
            instance.callJobFinishedInner(jobId, needsReschedule, why);
        }
    }

    public void callJobFinishedInner(int jobId, boolean needsReschedule, String why) {
        synchronized (sLock) {
            JobParameters params = sJobParamsMap.get(jobId);
            sLogger.log("callJobFinished()", " jobid=", Integer.valueOf(jobId), " needsReschedule=", Boolean.valueOf(needsReschedule), " ", sLogger.jobParametersToString(params), " why=", why);
            if (params != null) {
                jobFinished(params, needsReschedule);
                sJobParamsMap.remove(jobId);
            } else {
                Slog.e("SyncManager", "Job params not found for " + String.valueOf(jobId));
            }
        }
    }

    public static void markSyncStarted(int jobId) {
        synchronized (sLock) {
            sStartedSyncs.put(jobId, true);
        }
    }

    public static String jobParametersToString(JobParameters params) {
        if (params == null) {
            return "job:null";
        }
        return "job:#" + params.getJobId() + ":sr=[" + params.getStopReason() + SliceClientPermissions.SliceAuthority.DELIMITER + params.getDebugStopReason() + "]:" + SyncOperation.maybeCreateFromJobExtras(params.getExtras());
    }

    private static void wtf(String message) {
        sLogger.log(message);
        Slog.wtf("SyncManager", message);
    }
}
