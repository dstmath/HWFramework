package com.android.server;

import android.app.ActivityThread;
import android.common.HwActivityThread;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.wifipro.WifiProCommonUtils;
import dalvik.system.VMDebug;
import java.util.Map;

public final class HwMapleHelper {
    private static final int BETA_USER_TYPE = 3;
    private static final boolean DEBUG = false;
    private static final boolean MAPLE_PROCESS = ActivityThread.sIsMygote;
    private static final int MESSAGE_DELAY = 300000;
    private static final int MESSAGE_DURATION = 3600000;
    private static final int MSG_ID_REPORT_RT = 1;
    private static final String PROCESS_NAME = "system_server";
    private static final String TAG = "HwMapleHelper";
    private static final int USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 0);
    private static HwMapleHelper instanceHelper = null;
    private MapleHelperHandler mMapleHelperHandler;

    private HwMapleHelper() {
        this.mMapleHelperHandler = null;
        this.mMapleHelperHandler = new MapleHelperHandler(BackgroundThread.getHandler().getLooper());
        this.mMapleHelperHandler.sendEmptyMessageDelayed(1, HwLocationLockManager.CHECK_LOCATION_INTERVAL);
    }

    public static boolean isSupportMapleHelper() {
        return MAPLE_PROCESS && USER_TYPE == 3;
    }

    public static void startMapleHelper() {
        if (instanceHelper == null) {
            instanceHelper = new HwMapleHelper();
        }
    }

    private static class MapleHelperHandler extends Handler {
        private static final int LOG_LIMIT = 4000;
        private HwActivityThread mHwActivityThread;
        private HwFrameworkMonitor mMonitor;
        private long mStartTime;

        MapleHelperHandler(Looper looper) {
            super(looper);
            this.mHwActivityThread = null;
            this.mMonitor = null;
            this.mHwActivityThread = HwFrameworkFactory.getHwActivityThread();
            this.mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
            this.mStartTime = System.currentTimeMillis();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Slog.w(HwMapleHelper.TAG, "can not handle the msg:" + msg.what);
                return;
            }
            if (!handleScheduleReportData()) {
                Slog.e(HwMapleHelper.TAG, "fail to report runtime infomation!!!");
            }
            sendEmptyMessageDelayed(1, WifiProCommonUtils.RECHECK_DELAYED_MS);
        }

        private boolean handleScheduleReportData() {
            return doReportRuntime(HwMapleHelper.PROCESS_NAME, this.mStartTime);
        }

        private String codeToString(int code) {
            return Integer.toString(code);
        }

        public boolean doReportRuntime(final String procName, final long startTime) {
            if (procName == null || startTime <= 0) {
                return false;
            }
            BackgroundThread.get().getLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                /* class com.android.server.HwMapleHelper.MapleHelperHandler.AnonymousClass1 */

                @Override // android.os.MessageQueue.IdleHandler
                public boolean queueIdle() {
                    MapleHelperHandler.this.doReportRuntimeByIdleHandler(procName, startTime);
                    return false;
                }
            });
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void doReportRuntimeByIdleHandler(String procName, long startTime) {
            Map<String, String> stats = VMDebug.getRuntimeStats();
            if (stats != null && stats.size() != 8) {
                String threadsLocalWaterLine = stats.get("mpl.ref.threads-local-water-line");
                String cyclePattern = stats.get("mpl.mem.cycle-pattern");
                if (threadsLocalWaterLine.length() > 4000) {
                    threadsLocalWaterLine = threadsLocalWaterLine.substring(0, 4000);
                }
                if (cyclePattern.length() > 4000) {
                    cyclePattern = cyclePattern.substring(0, 4000);
                }
                Bundle data = new Bundle();
                try {
                    data.putString("proc_name", procName);
                    data.putInt("stat_duration", (int) ((System.currentTimeMillis() - startTime) / 1000));
                    data.putInt("circref_rcycl_cnt", Integer.valueOf(stats.get("mpl.mem.gc-count")).intValue());
                    data.putInt("circref_rcycl_max_duration", Integer.valueOf(stats.get("mpl.mem.gc-max-time")).intValue() / 1000000);
                    data.putInt("mem_leak_avrg", Integer.valueOf(stats.get("mpl.mem.leak-avg")).intValue());
                    data.putInt("mem_leak_peak", Integer.valueOf(stats.get("mpl.mem.leak-peak")).intValue());
                    data.putFloat("mem_alloc_space_util", Float.valueOf(stats.get("mpl.mem.allocation-utilization")).floatValue());
                    data.putInt("mem_alloc_abnormal", Integer.valueOf(stats.get("mpl.mem.allocation-abnormal-count")).intValue());
                    data.putInt("rc_abnormal", Integer.valueOf(stats.get("mpl.mem.rc-abnormal-count")).intValue());
                    data.putInt("global_water_line", Integer.valueOf(stats.get("mpl.ref.global-water-line")).intValue());
                    data.putInt("weak_water_line", Integer.valueOf(stats.get("mpl.ref.weak-water-line")).intValue());
                    data.putString("threads_local_water_line", threadsLocalWaterLine);
                    data.putInt("native_table_size", Integer.valueOf(stats.get("mpl.ref.native-table-size")).intValue());
                    data.putInt("consum_mpl_files", Integer.valueOf(stats.get("mpl.mem.consum-mpl-files")).intValue());
                    data.putInt("consum_class_locator", Integer.valueOf(stats.get("mpl.mem.consum-class-locator")).intValue());
                    data.putInt("reflect_manage_heap", Integer.valueOf(stats.get("mpl.mem.reflect-manage-heap")).intValue());
                    data.putInt("gc_manage_heap", Integer.valueOf(stats.get("mpl.mem.gc-manage-heap")).intValue());
                    data.putString("cycle_pattern", cyclePattern);
                    HwFrameworkMonitor hwFrameworkMonitor = this.mMonitor;
                    if (hwFrameworkMonitor != null && hwFrameworkMonitor.monitor(942030001, data)) {
                        Slog.i(HwMapleHelper.TAG, "upload bigdata success for: " + procName);
                    }
                } catch (NumberFormatException e) {
                    Slog.e(HwMapleHelper.TAG, "upload bigdata decode failed: " + procName);
                }
            }
        }
    }
}
