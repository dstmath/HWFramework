package com.android.server.rms;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.rms.utils.Utils;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.rms.utils.Interrupt;
import com.android.server.wifipro.WifiProCommonDefs;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CompactJobService extends JobService {
    private static final int COMPACT_PERIOD_INTERVAL;
    private static final int FAILED_PERIOD_INTERVAL;
    private static final int HOUR_TO_MINUTE = 60;
    private static final int JOB_ID = 1684366962;
    private static final int MINUTE_TO_SECOND = 60;
    private static final int SECOND_TO_MILLISECOND = 1000;
    private static final String TAG = "RMS.CompactJobService";
    private static final List<IDefraggler> defragglerLists = new ArrayList(16);
    private static ComponentName sCompactServiceName = new ComponentName("android", CompactJobService.class.getName());
    private static LocalLog sHistory = new LocalLog(20);
    private static AtomicInteger sTimes = new AtomicInteger(0);
    private final Interrupt mInterrupt = new Interrupt();

    static {
        int i;
        int i2;
        if (Utils.DEBUG) {
            i = Utils.getCompactPeriodInterval();
        } else {
            i = 28800000;
        }
        COMPACT_PERIOD_INTERVAL = i;
        if (Utils.DEBUG) {
            i2 = WifiProCommonDefs.QUERY_TIMEOUT_MS;
        } else {
            i2 = Constant.MILLISEC_TO_HOURS;
        }
        FAILED_PERIOD_INTERVAL = i2;
    }

    public static void schedule(Context context) {
        if (context != null) {
            ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).build());
        }
    }

    private static void delaySchedule(Context context, long delay) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setMinimumLatency(delay).build());
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters params) {
        Log.w(TAG, "onIdleStart");
        if (defragglerLists.size() <= 0) {
            return false;
        }
        Executors.newSingleThreadExecutor().execute(new Runnable(params) {
            /* class com.android.server.rms.$$Lambda$CompactJobService$y5cwN11eckCfP4jdjN7FEOhZs */
            private final /* synthetic */ JobParameters f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CompactJobService.this.lambda$onStartJob$0$CompactJobService(this.f$1);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$onStartJob$0$CompactJobService(JobParameters jobParams) {
        boolean isFinished = true;
        Iterator<IDefraggler> it = defragglerLists.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            IDefraggler defraggler = it.next();
            if (this.mInterrupt.checkInterruptAndReset()) {
                isFinished = false;
                break;
            }
            defraggler.compact("background compact", null);
        }
        this.mInterrupt.reset();
        sTimes.getAndIncrement();
        LocalLog localLog = sHistory;
        localLog.log("do compact " + isFinished + " times = " + sTimes.get());
        jobFinished(jobParams, false);
        delaySchedule(this, (long) (isFinished ? COMPACT_PERIOD_INTERVAL : FAILED_PERIOD_INTERVAL));
    }

    @Override // android.app.job.JobService
    @SuppressLint({"PreferForInArrayList"})
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "onIdleStop");
        this.mInterrupt.trigger();
        for (IDefraggler defraggler : defragglerLists) {
            defraggler.interrupt();
        }
        return false;
    }

    protected static void dumpLog(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CompactJobService dump");
        sHistory.dump(fd, pw, args);
    }

    public static void addDefragglers(IDefraggler defraggler) {
        if (defraggler != null) {
            defragglerLists.add(defraggler);
        }
    }
}
