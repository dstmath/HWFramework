package com.android.server;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.pm.PackageManagerService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class ZramWriteback extends JobService {
    private static final String BDEV_SYS = "/sys/block/zram%d/backing_dev";
    private static final boolean DEBUG = false;
    private static final String FIRST_WB_DELAY_PROP = "ro.zram.first_wb_delay_mins";
    private static final String IDLE_SYS = "/sys/block/zram%d/idle";
    private static final String IDLE_SYS_ALL_PAGES = "all";
    private static final String MARK_IDLE_DELAY_PROP = "ro.zram.mark_idle_delay_mins";
    private static final int MARK_IDLE_JOB_ID = 811;
    private static final int MAX_ZRAM_DEVICES = 256;
    private static final String PERIODIC_WB_DELAY_PROP = "ro.zram.periodic_wb_delay_hours";
    private static final String TAG = "ZramWriteback";
    private static final int WB_STATS_MAX_FILE_SIZE = 128;
    private static final String WB_STATS_SYS = "/sys/block/zram%d/bd_stat";
    private static final String WB_SYS = "/sys/block/zram%d/writeback";
    private static final String WB_SYS_IDLE_PAGES = "idle";
    private static final int WRITEBACK_IDLE_JOB_ID = 812;
    private static int sZramDeviceId = 0;
    private static final ComponentName sZramWriteback = new ComponentName(PackageManagerService.PLATFORM_PACKAGE_NAME, ZramWriteback.class.getName());

    private void markPagesAsIdle() {
        String idlePath = String.format(IDLE_SYS, Integer.valueOf(sZramDeviceId));
        try {
            FileUtils.stringToFile(new File(idlePath), IDLE_SYS_ALL_PAGES);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write to " + idlePath);
        }
    }

    private void flushIdlePages() {
        String wbPath = String.format(WB_SYS, Integer.valueOf(sZramDeviceId));
        try {
            FileUtils.stringToFile(new File(wbPath), WB_SYS_IDLE_PAGES);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to write to " + wbPath);
        }
    }

    private int getWrittenPageCount() {
        String wbStatsPath = String.format(WB_STATS_SYS, Integer.valueOf(sZramDeviceId));
        try {
            return Integer.parseInt(FileUtils.readTextFile(new File(wbStatsPath), 128, "").trim().split("\\s+")[2], 10);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read writeback stats from " + wbStatsPath);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void markAndFlushPages() {
        int pageCount = getWrittenPageCount();
        flushIdlePages();
        markPagesAsIdle();
        if (pageCount != -1) {
            Slog.i(TAG, "Total pages written to disk is " + (getWrittenPageCount() - pageCount));
        }
    }

    private static boolean isWritebackEnabled() {
        try {
            if (!"none".equals(FileUtils.readTextFile(new File(String.format(BDEV_SYS, Integer.valueOf(sZramDeviceId))), 128, "").trim())) {
                return true;
            }
            Slog.w(TAG, "Writeback device is not set");
            return false;
        } catch (IOException e) {
            Slog.w(TAG, "Writeback is not enabled on zram");
        }
    }

    /* access modifiers changed from: private */
    public static void schedNextWriteback(Context context) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new JobInfo.Builder(WRITEBACK_IDLE_JOB_ID, sZramWriteback).setMinimumLatency(TimeUnit.HOURS.toMillis((long) SystemProperties.getInt(PERIODIC_WB_DELAY_PROP, 24))).setRequiresDeviceIdle(true).build());
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(final JobParameters params) {
        if (!isWritebackEnabled()) {
            jobFinished(params, false);
            return false;
        } else if (params.getJobId() == MARK_IDLE_JOB_ID) {
            markPagesAsIdle();
            jobFinished(params, false);
            return false;
        } else {
            new Thread("ZramWriteback_WritebackIdlePages") {
                /* class com.android.server.ZramWriteback.AnonymousClass1 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    ZramWriteback.this.markAndFlushPages();
                    ZramWriteback.schedNextWriteback(ZramWriteback.this);
                    ZramWriteback.this.jobFinished(params, false);
                }
            }.start();
            return true;
        }
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void scheduleZramWriteback(Context context) {
        int markIdleDelay = SystemProperties.getInt(MARK_IDLE_DELAY_PROP, 20);
        int firstWbDelay = SystemProperties.getInt(FIRST_WB_DELAY_PROP, 180);
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        js.schedule(new JobInfo.Builder(MARK_IDLE_JOB_ID, sZramWriteback).setMinimumLatency(TimeUnit.MINUTES.toMillis((long) markIdleDelay)).setOverrideDeadline(TimeUnit.MINUTES.toMillis((long) markIdleDelay)).build());
        js.schedule(new JobInfo.Builder(WRITEBACK_IDLE_JOB_ID, sZramWriteback).setMinimumLatency(TimeUnit.MINUTES.toMillis((long) firstWbDelay)).setRequiresDeviceIdle(true).build());
    }
}
