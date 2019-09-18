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
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class CompactJobService extends JobService {
    /* access modifiers changed from: private */
    public static int COMPACT_PERIOD_INTERVAL = (Utils.DEBUG ? Utils.getCompactPeriodInterval() : 28800000);
    /* access modifiers changed from: private */
    public static final int FAILED_PERIOD_INTERVAL = (Utils.DEBUG ? 30000 : Constant.MILLISEC_TO_HOURS);
    private static final int JOB_ID = 1684366962;
    static final String TAG = "RMS.CompactJobService";
    private static ComponentName sCompactServiceName = new ComponentName("android", CompactJobService.class.getName());
    /* access modifiers changed from: private */
    public static final ArrayList<IDefraggler> sDefragglers = new ArrayList<>();
    /* access modifiers changed from: private */
    public static LocalLog sHistory = new LocalLog(20);
    /* access modifiers changed from: private */
    public static AtomicInteger sTimes = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public final Interrupt mInterrupt = new Interrupt();

    public static void schedule(Context context) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).build());
    }

    /* access modifiers changed from: private */
    public static void delay_schedule(Context context, long delay) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setMinimumLatency(delay).build());
    }

    public boolean onStartJob(JobParameters params) {
        Log.w(TAG, "onIdleStart");
        if (sDefragglers.size() <= 0) {
            return false;
        }
        final JobParameters jobParams = params;
        new Thread("CompactJobService_Handler") {
            @SuppressLint({"PreferForInArrayList"})
            public void run() {
                boolean bFinished = true;
                Iterator it = CompactJobService.sDefragglers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    IDefraggler defraggler = (IDefraggler) it.next();
                    if (CompactJobService.this.mInterrupt.checkInterruptAndReset()) {
                        bFinished = false;
                        break;
                    }
                    defraggler.compact("background compact", null);
                }
                CompactJobService.this.mInterrupt.reset();
                CompactJobService.sTimes.getAndIncrement();
                LocalLog access$300 = CompactJobService.sHistory;
                access$300.log("do compact " + bFinished + " times = " + CompactJobService.sTimes.get());
                CompactJobService.this.jobFinished(jobParams, false);
                CompactJobService.delay_schedule(CompactJobService.this, (long) (bFinished ? CompactJobService.COMPACT_PERIOD_INTERVAL : CompactJobService.FAILED_PERIOD_INTERVAL));
            }
        }.start();
        return true;
    }

    @SuppressLint({"PreferForInArrayList"})
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "onIdleStop");
        this.mInterrupt.trigger();
        Iterator<IDefraggler> it = sDefragglers.iterator();
        while (it.hasNext()) {
            it.next().interrupt();
        }
        return false;
    }

    protected static void dumpLog(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CompactJobService dump");
        sHistory.dump(fd, pw, args);
    }

    public static void addDefragglers(IDefraggler defraggler) {
        sDefragglers.add(defraggler);
    }
}
