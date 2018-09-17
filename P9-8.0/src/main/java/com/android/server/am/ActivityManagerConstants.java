package com.android.server.am;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.server.connectivity.LingerMonitor;
import java.io.PrintWriter;

final class ActivityManagerConstants extends ContentObserver {
    private static final long DEFAULT_BACKGROUND_SETTLE_TIME = 60000;
    private static final long DEFAULT_BG_START_TIMEOUT = 15000;
    private static final long DEFAULT_CONTENT_PROVIDER_RETAIN_TIME = 20000;
    private static final long DEFAULT_CPU_MIN_CHECK_DURATION;
    private static final long DEFAULT_FGSERVICE_MIN_REPORT_TIME = 3000;
    private static final long DEFAULT_FGSERVICE_MIN_SHOWN_TIME = 2000;
    private static final long DEFAULT_FGSERVICE_SCREEN_ON_AFTER_TIME = 5000;
    private static final long DEFAULT_FGSERVICE_SCREEN_ON_BEFORE_TIME = 1000;
    private static final long DEFAULT_FULL_PSS_LOWERED_INTERVAL = 120000;
    private static final long DEFAULT_FULL_PSS_MIN_INTERVAL = 600000;
    private static final long DEFAULT_GC_MIN_INTERVAL = 60000;
    private static final long DEFAULT_GC_TIMEOUT = 5000;
    private static final int DEFAULT_MAX_CACHED_PROCESSES = SystemProperties.getInt("ro.sys.fw.bg_apps_limit", 32);
    private static final long DEFAULT_MAX_SERVICE_INACTIVITY = 1800000;
    private static final long DEFAULT_POWER_CHECK_DELAY = ((long) (((ActivityManagerDebugConfig.DEBUG_POWER_QUICK ? 2 : 15) * 60) * 1000));
    private static final long DEFAULT_SERVICE_MIN_RESTART_TIME_BETWEEN = 10000;
    private static final long DEFAULT_SERVICE_RESET_RUN_DURATION = 60000;
    private static final long DEFAULT_SERVICE_RESTART_DURATION = 1000;
    private static final int DEFAULT_SERVICE_RESTART_DURATION_FACTOR = 4;
    private static final long DEFAULT_SERVICE_USAGE_INTERACTION_TIME = 1800000;
    private static final long DEFAULT_USAGE_STATS_INTERACTION_INTERVAL = 86400000;
    private static final long DEFAULT_WAKE_LOCK_MIN_CHECK_DURATION;
    private static final String KEY_BACKGROUND_SETTLE_TIME = "background_settle_time";
    static final String KEY_BG_START_TIMEOUT = "service_bg_start_timeout";
    private static final String KEY_CONTENT_PROVIDER_RETAIN_TIME = "content_provider_retain_time";
    private static final String KEY_CPU_MIN_CHECK_DURATION = "cpu_min_check_duration";
    private static final String KEY_FGSERVICE_MIN_REPORT_TIME = "fgservice_min_report_time";
    private static final String KEY_FGSERVICE_MIN_SHOWN_TIME = "fgservice_min_shown_time";
    private static final String KEY_FGSERVICE_SCREEN_ON_AFTER_TIME = "fgservice_screen_on_after_time";
    private static final String KEY_FGSERVICE_SCREEN_ON_BEFORE_TIME = "fgservice_screen_on_before_time";
    private static final String KEY_FULL_PSS_LOWERED_INTERVAL = "full_pss_lowered_interval";
    private static final String KEY_FULL_PSS_MIN_INTERVAL = "full_pss_min_interval";
    private static final String KEY_GC_MIN_INTERVAL = "gc_min_interval";
    private static final String KEY_GC_TIMEOUT = "gc_timeout";
    private static final String KEY_MAX_CACHED_PROCESSES = "max_cached_processes";
    static final String KEY_MAX_SERVICE_INACTIVITY = "service_max_inactivity";
    private static final String KEY_POWER_CHECK_DELAY = "power_check_delay";
    static final String KEY_SERVICE_MIN_RESTART_TIME_BETWEEN = "service_min_restart_time_between";
    static final String KEY_SERVICE_RESET_RUN_DURATION = "service_reset_run_duration";
    static final String KEY_SERVICE_RESTART_DURATION = "service_restart_duration";
    static final String KEY_SERVICE_RESTART_DURATION_FACTOR = "service_restart_duration_factor";
    private static final String KEY_SERVICE_USAGE_INTERACTION_TIME = "service_usage_interaction_time";
    private static final String KEY_USAGE_STATS_INTERACTION_INTERVAL = "usage_stats_interaction_interval";
    private static final String KEY_WAKE_LOCK_MIN_CHECK_DURATION = "wake_lock_min_check_duration";
    public long BACKGROUND_SETTLE_TIME = LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS;
    public long BG_START_TIMEOUT = DEFAULT_BG_START_TIMEOUT;
    long CONTENT_PROVIDER_RETAIN_TIME = DEFAULT_CONTENT_PROVIDER_RETAIN_TIME;
    long CPU_MIN_CHECK_DURATION = DEFAULT_CPU_MIN_CHECK_DURATION;
    public int CUR_MAX_CACHED_PROCESSES;
    public int CUR_MAX_EMPTY_PROCESSES;
    public int CUR_TRIM_CACHED_PROCESSES;
    public int CUR_TRIM_EMPTY_PROCESSES;
    public long FGSERVICE_MIN_REPORT_TIME = DEFAULT_FGSERVICE_MIN_REPORT_TIME;
    public long FGSERVICE_MIN_SHOWN_TIME = DEFAULT_FGSERVICE_MIN_SHOWN_TIME;
    public long FGSERVICE_SCREEN_ON_AFTER_TIME = 5000;
    public long FGSERVICE_SCREEN_ON_BEFORE_TIME = 1000;
    long FULL_PSS_LOWERED_INTERVAL = 120000;
    long FULL_PSS_MIN_INTERVAL = 600000;
    long GC_MIN_INTERVAL = LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS;
    long GC_TIMEOUT = 5000;
    public int MAX_CACHED_PROCESSES = DEFAULT_MAX_CACHED_PROCESSES;
    public long MAX_SERVICE_INACTIVITY = HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME;
    long POWER_CHECK_DELAY = DEFAULT_POWER_CHECK_DELAY;
    public long SERVICE_MIN_RESTART_TIME_BETWEEN = 10000;
    public long SERVICE_RESET_RUN_DURATION = LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS;
    public long SERVICE_RESTART_DURATION = 1000;
    public int SERVICE_RESTART_DURATION_FACTOR = 4;
    long SERVICE_USAGE_INTERACTION_TIME = HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME;
    long USAGE_STATS_INTERACTION_INTERVAL = 86400000;
    long WAKE_LOCK_MIN_CHECK_DURATION = DEFAULT_WAKE_LOCK_MIN_CHECK_DURATION;
    private int mOverrideMaxCachedProcesses = -1;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    private ContentResolver mResolver;
    private final ActivityManagerService mService;

    static {
        int i;
        int i2 = 1;
        if (ActivityManagerDebugConfig.DEBUG_POWER_QUICK) {
            i = 1;
        } else {
            i = 5;
        }
        DEFAULT_WAKE_LOCK_MIN_CHECK_DURATION = (long) ((i * 60) * 1000);
        if (!ActivityManagerDebugConfig.DEBUG_POWER_QUICK) {
            i2 = 5;
        }
        DEFAULT_CPU_MIN_CHECK_DURATION = (long) ((i2 * 60) * 1000);
    }

    public ActivityManagerConstants(ActivityManagerService service, Handler handler) {
        super(handler);
        this.mService = service;
        updateMaxCachedProcesses();
    }

    public void start(ContentResolver resolver) {
        this.mResolver = resolver;
        this.mResolver.registerContentObserver(Global.getUriFor("activity_manager_constants"), false, this);
        updateConstants();
    }

    public void setOverrideMaxCachedProcesses(int value) {
        this.mOverrideMaxCachedProcesses = value;
        updateMaxCachedProcesses();
    }

    public int getOverrideMaxCachedProcesses() {
        return this.mOverrideMaxCachedProcesses;
    }

    public static int computeEmptyProcessLimit(int totalProcessLimit) {
        int percent = SystemProperties.getInt("ro.sys.fw.empty_app_percent", -1);
        if (-1 == percent) {
            percent = SystemProperties.getInt("sys.iaware.empty_app_percent", 50);
        }
        if (percent < 0 || percent > 100) {
            percent = 50;
        }
        return (totalProcessLimit * percent) / 100;
    }

    public void onChange(boolean selfChange, Uri uri) {
        updateConstants();
    }

    private void updateConstants() {
        String setting = Global.getString(this.mResolver, "activity_manager_constants");
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mParser.setString(setting);
            } catch (IllegalArgumentException e) {
                Slog.e("ActivityManagerConstants", "Bad activity manager config settings", e);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
            this.MAX_CACHED_PROCESSES = this.mParser.getInt(KEY_MAX_CACHED_PROCESSES, DEFAULT_MAX_CACHED_PROCESSES);
            this.BACKGROUND_SETTLE_TIME = this.mParser.getLong(KEY_BACKGROUND_SETTLE_TIME, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
            this.FGSERVICE_MIN_SHOWN_TIME = this.mParser.getLong(KEY_FGSERVICE_MIN_SHOWN_TIME, DEFAULT_FGSERVICE_MIN_SHOWN_TIME);
            this.FGSERVICE_MIN_REPORT_TIME = this.mParser.getLong(KEY_FGSERVICE_MIN_REPORT_TIME, DEFAULT_FGSERVICE_MIN_REPORT_TIME);
            this.FGSERVICE_SCREEN_ON_BEFORE_TIME = this.mParser.getLong(KEY_FGSERVICE_SCREEN_ON_BEFORE_TIME, 1000);
            this.FGSERVICE_SCREEN_ON_AFTER_TIME = this.mParser.getLong(KEY_FGSERVICE_SCREEN_ON_AFTER_TIME, 5000);
            this.CONTENT_PROVIDER_RETAIN_TIME = this.mParser.getLong(KEY_CONTENT_PROVIDER_RETAIN_TIME, DEFAULT_CONTENT_PROVIDER_RETAIN_TIME);
            this.GC_TIMEOUT = this.mParser.getLong(KEY_GC_TIMEOUT, 5000);
            this.GC_MIN_INTERVAL = this.mParser.getLong(KEY_GC_MIN_INTERVAL, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
            this.FULL_PSS_MIN_INTERVAL = this.mParser.getLong(KEY_FULL_PSS_MIN_INTERVAL, 600000);
            this.FULL_PSS_LOWERED_INTERVAL = this.mParser.getLong(KEY_FULL_PSS_LOWERED_INTERVAL, 120000);
            this.POWER_CHECK_DELAY = this.mParser.getLong(KEY_POWER_CHECK_DELAY, DEFAULT_POWER_CHECK_DELAY);
            this.WAKE_LOCK_MIN_CHECK_DURATION = this.mParser.getLong(KEY_WAKE_LOCK_MIN_CHECK_DURATION, DEFAULT_WAKE_LOCK_MIN_CHECK_DURATION);
            this.CPU_MIN_CHECK_DURATION = this.mParser.getLong(KEY_CPU_MIN_CHECK_DURATION, DEFAULT_CPU_MIN_CHECK_DURATION);
            this.SERVICE_USAGE_INTERACTION_TIME = this.mParser.getLong(KEY_SERVICE_USAGE_INTERACTION_TIME, HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME);
            this.USAGE_STATS_INTERACTION_INTERVAL = this.mParser.getLong(KEY_USAGE_STATS_INTERACTION_INTERVAL, 86400000);
            this.SERVICE_RESTART_DURATION = this.mParser.getLong(KEY_SERVICE_RESTART_DURATION, 1000);
            this.SERVICE_RESET_RUN_DURATION = this.mParser.getLong(KEY_SERVICE_RESET_RUN_DURATION, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
            this.SERVICE_RESTART_DURATION_FACTOR = this.mParser.getInt(KEY_SERVICE_RESTART_DURATION_FACTOR, 4);
            this.SERVICE_MIN_RESTART_TIME_BETWEEN = this.mParser.getLong(KEY_SERVICE_MIN_RESTART_TIME_BETWEEN, 10000);
            this.MAX_SERVICE_INACTIVITY = this.mParser.getLong(KEY_MAX_SERVICE_INACTIVITY, HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME);
            this.BG_START_TIMEOUT = this.mParser.getLong(KEY_BG_START_TIMEOUT, DEFAULT_BG_START_TIMEOUT);
            updateMaxCachedProcesses();
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    private void updateMaxCachedProcesses() {
        this.CUR_MAX_CACHED_PROCESSES = this.mOverrideMaxCachedProcesses < 0 ? this.MAX_CACHED_PROCESSES : this.mOverrideMaxCachedProcesses;
        this.CUR_MAX_EMPTY_PROCESSES = computeEmptyProcessLimit(this.CUR_MAX_CACHED_PROCESSES);
        int rawMaxEmptyProcesses = computeEmptyProcessLimit(this.MAX_CACHED_PROCESSES);
        this.CUR_TRIM_EMPTY_PROCESSES = rawMaxEmptyProcesses / 2;
        this.CUR_TRIM_CACHED_PROCESSES = (this.MAX_CACHED_PROCESSES - rawMaxEmptyProcesses) / 3;
    }

    void dump(PrintWriter pw) {
        pw.println("ACTIVITY MANAGER SETTINGS (dumpsys activity settings) activity_manager_constants:");
        pw.print("  ");
        pw.print(KEY_MAX_CACHED_PROCESSES);
        pw.print("=");
        pw.println(this.MAX_CACHED_PROCESSES);
        pw.print("  ");
        pw.print(KEY_BACKGROUND_SETTLE_TIME);
        pw.print("=");
        pw.println(this.BACKGROUND_SETTLE_TIME);
        pw.print("  ");
        pw.print(KEY_FGSERVICE_MIN_SHOWN_TIME);
        pw.print("=");
        pw.println(this.FGSERVICE_MIN_SHOWN_TIME);
        pw.print("  ");
        pw.print(KEY_FGSERVICE_MIN_REPORT_TIME);
        pw.print("=");
        pw.println(this.FGSERVICE_MIN_REPORT_TIME);
        pw.print("  ");
        pw.print(KEY_FGSERVICE_SCREEN_ON_BEFORE_TIME);
        pw.print("=");
        pw.println(this.FGSERVICE_SCREEN_ON_BEFORE_TIME);
        pw.print("  ");
        pw.print(KEY_FGSERVICE_SCREEN_ON_AFTER_TIME);
        pw.print("=");
        pw.println(this.FGSERVICE_SCREEN_ON_AFTER_TIME);
        pw.print("  ");
        pw.print(KEY_CONTENT_PROVIDER_RETAIN_TIME);
        pw.print("=");
        pw.println(this.CONTENT_PROVIDER_RETAIN_TIME);
        pw.print("  ");
        pw.print(KEY_GC_TIMEOUT);
        pw.print("=");
        pw.println(this.GC_TIMEOUT);
        pw.print("  ");
        pw.print(KEY_GC_MIN_INTERVAL);
        pw.print("=");
        pw.println(this.GC_MIN_INTERVAL);
        pw.print("  ");
        pw.print(KEY_FULL_PSS_MIN_INTERVAL);
        pw.print("=");
        pw.println(this.FULL_PSS_MIN_INTERVAL);
        pw.print("  ");
        pw.print(KEY_FULL_PSS_LOWERED_INTERVAL);
        pw.print("=");
        pw.println(this.FULL_PSS_LOWERED_INTERVAL);
        pw.print("  ");
        pw.print(KEY_POWER_CHECK_DELAY);
        pw.print("=");
        pw.println(this.POWER_CHECK_DELAY);
        pw.print("  ");
        pw.print(KEY_WAKE_LOCK_MIN_CHECK_DURATION);
        pw.print("=");
        pw.println(this.WAKE_LOCK_MIN_CHECK_DURATION);
        pw.print("  ");
        pw.print(KEY_CPU_MIN_CHECK_DURATION);
        pw.print("=");
        pw.println(this.CPU_MIN_CHECK_DURATION);
        pw.print("  ");
        pw.print(KEY_SERVICE_USAGE_INTERACTION_TIME);
        pw.print("=");
        pw.println(this.SERVICE_USAGE_INTERACTION_TIME);
        pw.print("  ");
        pw.print(KEY_USAGE_STATS_INTERACTION_INTERVAL);
        pw.print("=");
        pw.println(this.USAGE_STATS_INTERACTION_INTERVAL);
        pw.print("  ");
        pw.print(KEY_SERVICE_RESTART_DURATION);
        pw.print("=");
        pw.println(this.SERVICE_RESTART_DURATION);
        pw.print("  ");
        pw.print(KEY_SERVICE_RESET_RUN_DURATION);
        pw.print("=");
        pw.println(this.SERVICE_RESET_RUN_DURATION);
        pw.print("  ");
        pw.print(KEY_SERVICE_RESTART_DURATION_FACTOR);
        pw.print("=");
        pw.println(this.SERVICE_RESTART_DURATION_FACTOR);
        pw.print("  ");
        pw.print(KEY_SERVICE_MIN_RESTART_TIME_BETWEEN);
        pw.print("=");
        pw.println(this.SERVICE_MIN_RESTART_TIME_BETWEEN);
        pw.print("  ");
        pw.print(KEY_MAX_SERVICE_INACTIVITY);
        pw.print("=");
        pw.println(this.MAX_SERVICE_INACTIVITY);
        pw.print("  ");
        pw.print(KEY_BG_START_TIMEOUT);
        pw.print("=");
        pw.println(this.BG_START_TIMEOUT);
        pw.println();
        if (this.mOverrideMaxCachedProcesses >= 0) {
            pw.print("  mOverrideMaxCachedProcesses=");
            pw.println(this.mOverrideMaxCachedProcesses);
        }
        pw.print("  CUR_MAX_CACHED_PROCESSES=");
        pw.println(this.CUR_MAX_CACHED_PROCESSES);
        pw.print("  CUR_MAX_EMPTY_PROCESSES=");
        pw.println(this.CUR_MAX_EMPTY_PROCESSES);
        pw.print("  CUR_TRIM_EMPTY_PROCESSES=");
        pw.println(this.CUR_TRIM_EMPTY_PROCESSES);
        pw.print("  CUR_TRIM_CACHED_PROCESSES=");
        pw.println(this.CUR_TRIM_CACHED_PROCESSES);
    }
}
