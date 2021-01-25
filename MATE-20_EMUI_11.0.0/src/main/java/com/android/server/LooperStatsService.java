package com.android.server;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.internal.os.AppIdToPackageMap;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.CachedDeviceState;
import com.android.internal.os.LooperStats;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LooperStatsService extends Binder {
    private static final String DEBUG_SYS_LOOPER_STATS_ENABLED = "debug.sys.looper_stats_enabled";
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_ENTRIES_SIZE_CAP = 1500;
    private static final int DEFAULT_SAMPLING_INTERVAL = 1000;
    private static final boolean DEFAULT_TRACK_SCREEN_INTERACTIVE = false;
    private static final String LOOPER_STATS_SERVICE_NAME = "looper_stats";
    private static final String SETTINGS_ENABLED_KEY = "enabled";
    private static final String SETTINGS_SAMPLING_INTERVAL_KEY = "sampling_interval";
    private static final String SETTINGS_TRACK_SCREEN_INTERACTIVE_KEY = "track_screen_state";
    private static final String TAG = "LooperStatsService";
    private final Context mContext;
    private boolean mEnabled;
    private final LooperStats mStats;
    private boolean mTrackScreenInteractive;

    private LooperStatsService(Context context, LooperStats stats) {
        this.mEnabled = false;
        this.mTrackScreenInteractive = false;
        this.mContext = context;
        this.mStats = stats;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFromSettings() {
        KeyValueListParser parser = new KeyValueListParser(',');
        try {
            parser.setString(Settings.Global.getString(this.mContext.getContentResolver(), LOOPER_STATS_SERVICE_NAME));
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "Bad looper_stats settings", e);
        }
        setSamplingInterval(parser.getInt(SETTINGS_SAMPLING_INTERVAL_KEY, 1000));
        setTrackScreenInteractive(parser.getBoolean(SETTINGS_TRACK_SCREEN_INTERACTIVE_KEY, false));
        setEnabled(SystemProperties.getBoolean(DEBUG_SYS_LOOPER_STATS_ENABLED, parser.getBoolean(SETTINGS_ENABLED_KEY, true)));
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new LooperShellCommand().exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            AppIdToPackageMap packageMap = AppIdToPackageMap.getSnapshot();
            pw.print("Start time: ");
            pw.println(DateFormat.format("yyyy-MM-dd HH:mm:ss", this.mStats.getStartTimeMillis()));
            pw.print("On battery time (ms): ");
            pw.println(this.mStats.getBatteryTimeMillis());
            List<LooperStats.ExportedEntry> entries = this.mStats.getEntries();
            entries.sort(Comparator.comparing($$Lambda$LooperStatsService$Byo6QAxZpVXDCMtjrcYJc6YLAks.INSTANCE).thenComparing($$Lambda$LooperStatsService$Vzysuo2tO86qjfcWeh1Rdb47NQQ.INSTANCE).thenComparing($$Lambda$LooperStatsService$XjYmSR91xdWG1XgtGj9GBZZbjk.INSTANCE).thenComparing($$Lambda$LooperStatsService$XtFJEDeyYRT79ZkVP96XkHribxg.INSTANCE));
            pw.println(String.join(",", Arrays.asList("work_source_uid", "thread_name", "handler_class", "message_name", "is_interactive", "message_count", "recorded_message_count", "total_latency_micros", "max_latency_micros", "total_cpu_micros", "max_cpu_micros", "recorded_delay_message_count", "total_delay_millis", "max_delay_millis", "exception_count")));
            for (LooperStats.ExportedEntry entry : entries) {
                if (!entry.messageName.startsWith("__DEBUG_")) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", packageMap.mapUid(entry.workSourceUid), entry.threadName, entry.handlerClassName, entry.messageName, Boolean.valueOf(entry.isInteractive), Long.valueOf(entry.messageCount), Long.valueOf(entry.recordedMessageCount), Long.valueOf(entry.totalLatencyMicros), Long.valueOf(entry.maxLatencyMicros), Long.valueOf(entry.cpuUsageMicros), Long.valueOf(entry.maxCpuUsageMicros), Long.valueOf(entry.recordedDelayMessageCount), Long.valueOf(entry.delayMillis), Long.valueOf(entry.maxDelayMillis), Long.valueOf(entry.exceptionCount));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            this.mStats.reset();
            this.mStats.setAddDebugEntries(enabled);
            Looper.setObserver(enabled ? this.mStats : null);
        }
    }

    private void setTrackScreenInteractive(boolean enabled) {
        if (this.mTrackScreenInteractive != enabled) {
            this.mTrackScreenInteractive = enabled;
            this.mStats.reset();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSamplingInterval(int samplingInterval) {
        if (samplingInterval > 0) {
            this.mStats.setSamplingInterval(samplingInterval);
            return;
        }
        Slog.w(TAG, "Ignored invalid sampling interval (value must be positive): " + samplingInterval);
    }

    public static class Lifecycle extends SystemService {
        private final LooperStatsService mService = new LooperStatsService(getContext(), this.mStats);
        private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mService);
        private final LooperStats mStats = new LooperStats(1000, 1500);

        public Lifecycle(Context context) {
            super(context);
        }

        @Override // com.android.server.SystemService
        public void onStart() {
            publishLocalService(LooperStats.class, this.mStats);
            publishBinderService(LooperStatsService.LOOPER_STATS_SERVICE_NAME, this.mService);
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (500 == phase) {
                this.mService.initFromSettings();
                getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(LooperStatsService.LOOPER_STATS_SERVICE_NAME), false, this.mSettingsObserver, 0);
                this.mStats.setDeviceState((CachedDeviceState.Readonly) getLocalService(CachedDeviceState.Readonly.class));
            }
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final LooperStatsService mService;

        SettingsObserver(LooperStatsService service) {
            super(BackgroundThread.getHandler());
            this.mService = service;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            this.mService.initFromSettings();
        }
    }

    private class LooperShellCommand extends ShellCommand {
        private LooperShellCommand() {
        }

        public int onCommand(String cmd) {
            if ("enable".equals(cmd)) {
                LooperStatsService.this.setEnabled(true);
                return 0;
            } else if ("disable".equals(cmd)) {
                LooperStatsService.this.setEnabled(false);
                return 0;
            } else if ("reset".equals(cmd)) {
                LooperStatsService.this.mStats.reset();
                return 0;
            } else if (!LooperStatsService.SETTINGS_SAMPLING_INTERVAL_KEY.equals(cmd)) {
                return handleDefaultCommands(cmd);
            } else {
                LooperStatsService.this.setSamplingInterval(Integer.parseUnsignedInt(getNextArgRequired()));
                return 0;
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("looper_stats commands:");
            pw.println("  enable: Enable collecting stats.");
            pw.println("  disable: Disable collecting stats.");
            pw.println("  sampling_interval: Change the sampling interval.");
            pw.println("  reset: Reset stats.");
        }
    }
}
