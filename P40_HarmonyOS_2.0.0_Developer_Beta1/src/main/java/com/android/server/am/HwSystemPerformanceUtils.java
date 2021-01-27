package com.android.server.am;

import android.os.Binder;
import android.os.SystemProperties;
import android.os.TransactionTracker;
import android.util.Log;
import com.android.internal.os.BinderInternal;
import com.android.server.location.HwLogRecordManager;

public class HwSystemPerformanceUtils {
    private static final int BINDER_TRACK_CONFIG_ITEMS = 2;
    private static final int BINDER_TRACK_DUMP_INTERVAL = 0;
    private static final int BINDER_TRACK_DUMP_PROCESS = 1;
    private static final Object S_INSTANCE_LOCK = new Object();
    static final String TAG = "HwSystemPerformance";
    private static final int TOP_N_BINDER_STACK = 10;
    private static HwSystemPerformanceUtils sInstance;
    private int mDumpIntervalMs = 0;
    private String mTrackPackage;

    public static HwSystemPerformanceUtils getInstance() {
        HwSystemPerformanceUtils hwSystemPerformanceUtils = sInstance;
        if (hwSystemPerformanceUtils != null) {
            return hwSystemPerformanceUtils;
        }
        synchronized (S_INSTANCE_LOCK) {
            if (sInstance == null) {
                sInstance = new HwSystemPerformanceUtils();
            }
        }
        return sInstance;
    }

    public HwSystemPerformanceUtils() {
        String statConfig = SystemProperties.get("ro.config.hw_system_binder_stat", "");
        if (!statConfig.isEmpty()) {
            String[] configs = statConfig.split(HwLogRecordManager.VERTICAL_ESC_SEPARATE);
            if (configs.length == 2) {
                try {
                    this.mDumpIntervalMs = Integer.valueOf(configs[0]).intValue();
                    this.mTrackPackage = configs[1];
                } catch (NumberFormatException e) {
                    Log.w(TAG, "invalid config " + configs[0]);
                }
            }
        }
    }

    public int getDumpInterval() {
        return this.mDumpIntervalMs;
    }

    public void notifyProcessCreate(String pkg, int pid, int uid) {
        String str;
        if (this.mDumpIntervalMs > 0 && (str = this.mTrackPackage) != null && str.equals(pkg)) {
            Log.i(TAG, "notifyProcessCreate nSetTrackCalledPid " + pid);
            BinderInternal.nSetTrackCalledPid(pid);
        }
    }

    public void dumpBinderTrace() {
        TransactionTracker tracker = Binder.getTransactionTracker();
        String string = tracker.dumpTopTrace(10);
        tracker.clearTraces();
        Log.i(TAG, string);
    }
}
