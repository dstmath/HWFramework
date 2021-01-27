package com.android.server;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Binder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.internal.os.AppIdToPackageMap;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.BinderCallsStats;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.CachedDeviceState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BinderCallsStatsService extends Binder {
    private static final String PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING = "persist.sys.binder_calls_detailed_tracking";
    private static final String TAG = "BinderCallsStatsService";
    private final BinderCallsStats mBinderCallsStats;
    private SettingsObserver mSettingsObserver;
    private final AuthorizedWorkSourceProvider mWorkSourceProvider;

    /* access modifiers changed from: package-private */
    public static class AuthorizedWorkSourceProvider implements BinderInternal.WorkSourceProvider {
        private ArraySet<Integer> mAppIdWhitelist = new ArraySet<>();

        AuthorizedWorkSourceProvider() {
        }

        public int resolveWorkSourceUid(int untrustedWorkSourceUid) {
            int callingUid = getCallingUid();
            if (!this.mAppIdWhitelist.contains(Integer.valueOf(UserHandle.getAppId(callingUid)))) {
                return callingUid;
            }
            return untrustedWorkSourceUid != -1 ? untrustedWorkSourceUid : callingUid;
        }

        public void systemReady(Context context) {
            this.mAppIdWhitelist = createAppidWhitelist(context);
        }

        public void dump(PrintWriter pw, AppIdToPackageMap packageMap) {
            pw.println("AppIds of apps that can set the work source:");
            Iterator<Integer> it = this.mAppIdWhitelist.iterator();
            while (it.hasNext()) {
                pw.println("\t- " + packageMap.mapAppId(it.next().intValue()));
            }
        }

        /* access modifiers changed from: protected */
        public int getCallingUid() {
            return Binder.getCallingUid();
        }

        private ArraySet<Integer> createAppidWhitelist(Context context) {
            ArraySet<Integer> whitelist = new ArraySet<>();
            whitelist.add(Integer.valueOf(UserHandle.getAppId(Process.myUid())));
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getPackagesHoldingPermissions(new String[]{"android.permission.UPDATE_DEVICE_STATS"}, 786432);
            int packagesSize = packages.size();
            for (int i = 0; i < packagesSize; i++) {
                PackageInfo pkgInfo = packages.get(i);
                try {
                    whitelist.add(Integer.valueOf(UserHandle.getAppId(pm.getPackageUid(pkgInfo.packageName, 786432))));
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(BinderCallsStatsService.TAG, "Cannot find uid for package name " + pkgInfo.packageName, e);
                }
            }
            return whitelist;
        }
    }

    /* access modifiers changed from: private */
    public static class SettingsObserver extends ContentObserver {
        private static final String SETTINGS_DETAILED_TRACKING_KEY = "detailed_tracking";
        private static final String SETTINGS_ENABLED_KEY = "enabled";
        private static final String SETTINGS_MAX_CALL_STATS_KEY = "max_call_stats_count";
        private static final String SETTINGS_SAMPLING_INTERVAL_KEY = "sampling_interval";
        private static final String SETTINGS_TRACK_DIRECT_CALLING_UID_KEY = "track_calling_uid";
        private static final String SETTINGS_TRACK_SCREEN_INTERACTIVE_KEY = "track_screen_state";
        private static final String SETTINGS_UPLOAD_DATA_KEY = "upload_data";
        private final BinderCallsStats mBinderCallsStats;
        private final Context mContext;
        private boolean mEnabled;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private final Uri mUri = Settings.Global.getUriFor("binder_calls_stats");
        private final AuthorizedWorkSourceProvider mWorkSourceProvider;

        SettingsObserver(Context context, BinderCallsStats binderCallsStats, AuthorizedWorkSourceProvider workSourceProvider) {
            super(BackgroundThread.getHandler());
            this.mContext = context;
            context.getContentResolver().registerContentObserver(this.mUri, false, this, 0);
            this.mBinderCallsStats = binderCallsStats;
            this.mWorkSourceProvider = workSourceProvider;
            onChange();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mUri.equals(uri)) {
                onChange();
            }
        }

        public void onChange() {
            if (SystemProperties.get(BinderCallsStatsService.PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING).isEmpty()) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mContext.getContentResolver(), "binder_calls_stats"));
                } catch (IllegalArgumentException e) {
                    Slog.e(BinderCallsStatsService.TAG, "Bad binder call stats settings", e);
                }
                this.mBinderCallsStats.setDetailedTracking(this.mParser.getBoolean(SETTINGS_DETAILED_TRACKING_KEY, true));
                this.mBinderCallsStats.setSamplingInterval(this.mParser.getInt(SETTINGS_SAMPLING_INTERVAL_KEY, 1000));
                this.mBinderCallsStats.setMaxBinderCallStats(this.mParser.getInt(SETTINGS_MAX_CALL_STATS_KEY, (int) NetworkConstants.ETHER_MTU));
                this.mBinderCallsStats.setTrackScreenInteractive(this.mParser.getBoolean(SETTINGS_TRACK_SCREEN_INTERACTIVE_KEY, false));
                this.mBinderCallsStats.setTrackDirectCallerUid(this.mParser.getBoolean(SETTINGS_TRACK_DIRECT_CALLING_UID_KEY, true));
                boolean enabled = this.mParser.getBoolean(SETTINGS_ENABLED_KEY, true);
                if (this.mEnabled != enabled) {
                    if (enabled) {
                        Binder.setObserver(this.mBinderCallsStats);
                        Binder.setProxyTransactListener(new Binder.PropagateWorkSourceTransactListener());
                        Binder.setWorkSourceProvider(this.mWorkSourceProvider);
                    } else {
                        Binder.setObserver(null);
                        Binder.setProxyTransactListener(null);
                        Binder.setWorkSourceProvider($$Lambda$BinderCallsStatsService$SettingsObserver$bif9uA0lzoT6htcKe6MNsrH_ha4.INSTANCE);
                    }
                    this.mEnabled = enabled;
                    this.mBinderCallsStats.reset();
                    this.mBinderCallsStats.setAddDebugEntries(enabled);
                }
            }
        }
    }

    public static class Internal {
        private final BinderCallsStats mBinderCallsStats;

        Internal(BinderCallsStats binderCallsStats) {
            this.mBinderCallsStats = binderCallsStats;
        }

        public void reset() {
            this.mBinderCallsStats.reset();
        }

        public ArrayList<BinderCallsStats.ExportedCallStat> getExportedCallStats() {
            return this.mBinderCallsStats.getExportedCallStats();
        }

        public ArrayMap<String, Integer> getExportedExceptionStats() {
            return this.mBinderCallsStats.getExportedExceptionStats();
        }
    }

    public static class LifeCycle extends SystemService {
        private BinderCallsStats mBinderCallsStats;
        private BinderCallsStatsService mService;
        private AuthorizedWorkSourceProvider mWorkSourceProvider;

        public LifeCycle(Context context) {
            super(context);
        }

        @Override // com.android.server.SystemService
        public void onStart() {
            this.mBinderCallsStats = new BinderCallsStats(new BinderCallsStats.Injector());
            this.mWorkSourceProvider = new AuthorizedWorkSourceProvider();
            this.mService = new BinderCallsStatsService(this.mBinderCallsStats, this.mWorkSourceProvider);
            publishLocalService(Internal.class, new Internal(this.mBinderCallsStats));
            publishBinderService("binder_calls_stats", this.mService);
            if (SystemProperties.getBoolean(BinderCallsStatsService.PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, false)) {
                Slog.i(BinderCallsStatsService.TAG, "Enabled CPU usage tracking for binder calls. Controlled by persist.sys.binder_calls_detailed_tracking or via dumpsys binder_calls_stats --enable-detailed-tracking");
                this.mBinderCallsStats.setDetailedTracking(true);
            }
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            if (500 == phase) {
                this.mBinderCallsStats.setDeviceState((CachedDeviceState.Readonly) getLocalService(CachedDeviceState.Readonly.class));
                this.mWorkSourceProvider.systemReady(getContext());
                this.mService.systemReady(getContext());
            }
        }
    }

    BinderCallsStatsService(BinderCallsStats binderCallsStats, AuthorizedWorkSourceProvider workSourceProvider) {
        this.mBinderCallsStats = binderCallsStats;
        this.mWorkSourceProvider = workSourceProvider;
    }

    public void systemReady(Context context) {
        this.mSettingsObserver = new SettingsObserver(context, this.mBinderCallsStats, this.mWorkSourceProvider);
    }

    public void reset() {
        Slog.i(TAG, "Resetting stats");
        this.mBinderCallsStats.reset();
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean verbose = false;
        if (args != null) {
            boolean verbose2 = false;
            for (String arg : args) {
                if ("-a".equals(arg)) {
                    verbose2 = true;
                } else if ("--reset".equals(arg)) {
                    reset();
                    pw.println("binder_calls_stats reset.");
                    return;
                } else if ("--enable".equals(arg)) {
                    Binder.setObserver(this.mBinderCallsStats);
                    return;
                } else if ("--disable".equals(arg)) {
                    Binder.setObserver(null);
                    return;
                } else if ("--no-sampling".equals(arg)) {
                    this.mBinderCallsStats.setSamplingInterval(1);
                    return;
                } else if ("--enable-detailed-tracking".equals(arg)) {
                    SystemProperties.set(PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, "1");
                    this.mBinderCallsStats.setDetailedTracking(true);
                    pw.println("Detailed tracking enabled");
                    return;
                } else if ("--disable-detailed-tracking".equals(arg)) {
                    SystemProperties.set(PERSIST_SYS_BINDER_CALLS_DETAILED_TRACKING, "");
                    this.mBinderCallsStats.setDetailedTracking(false);
                    pw.println("Detailed tracking disabled");
                    return;
                } else if ("--dump-worksource-provider".equals(arg)) {
                    this.mWorkSourceProvider.dump(pw, AppIdToPackageMap.getSnapshot());
                    return;
                } else if ("-h".equals(arg)) {
                    pw.println("binder_calls_stats commands:");
                    pw.println("  --reset: Reset stats");
                    pw.println("  --enable: Enable tracking binder calls");
                    pw.println("  --disable: Disables tracking binder calls");
                    pw.println("  --no-sampling: Tracks all calls");
                    pw.println("  --enable-detailed-tracking: Enables detailed tracking");
                    pw.println("  --disable-detailed-tracking: Disables detailed tracking");
                    return;
                } else {
                    pw.println("Unknown option: " + arg);
                }
            }
            verbose = verbose2;
        }
        this.mBinderCallsStats.dump(pw, AppIdToPackageMap.getSnapshot(), verbose);
    }
}
