package com.android.server;

import android.os.Binder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.BinderCallsStats;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class BinderCallsStatsService extends Binder {
    private static final String PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING = "persist.sys.binder_calls_detailed_tracking";
    private static final String TAG = "BinderCallsStatsService";

    public static void start() {
        ServiceManager.addService("binder_calls_stats", new BinderCallsStatsService());
        if (SystemProperties.getBoolean(PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, false)) {
            Slog.i(TAG, "Enabled CPU usage tracking for binder calls. Controlled by persist.sys.binder_calls_detailed_tracking or via dumpsys binder_calls_stats --enable-detailed-tracking");
            BinderCallsStats.getInstance().setDetailedTracking(true);
        }
    }

    public static void reset() {
        Slog.i(TAG, "Resetting stats");
        BinderCallsStats.getInstance().reset();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (!"-a".equals(arg)) {
                    if ("--reset".equals(arg)) {
                        reset();
                        pw.println("binder_calls_stats reset.");
                        return;
                    } else if ("--enable-detailed-tracking".equals(arg)) {
                        SystemProperties.set(PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, "1");
                        BinderCallsStats.getInstance().setDetailedTracking(true);
                        pw.println("Detailed tracking enabled");
                        return;
                    } else if ("--disable-detailed-tracking".equals(arg)) {
                        SystemProperties.set(PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        BinderCallsStats.getInstance().setDetailedTracking(false);
                        pw.println("Detailed tracking disabled");
                        return;
                    } else if ("-h".equals(arg)) {
                        pw.println("binder_calls_stats commands:");
                        pw.println("  --reset: Reset stats");
                        pw.println("  --enable-detailed-tracking: Enables detailed tracking");
                        pw.println("  --disable-detailed-tracking: Disables detailed tracking");
                        return;
                    } else {
                        pw.println("Unknown option: " + arg);
                    }
                }
            }
        }
        BinderCallsStats.getInstance().dump(pw);
    }
}
