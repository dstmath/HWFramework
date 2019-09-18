package com.android.server.usage;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.usage.AppStandbyInfo;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManagerInternal;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkScoreManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.job.JobPackageTracker;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usage.AppIdleHistory;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import java.io.File;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class AppStandbyController {
    static final boolean COMPRESS_TIME = false;
    static final boolean DEBUG = false;
    private static final long DEFAULT_PREDICTION_TIMEOUT = 43200000;
    static final long[] ELAPSED_TIME_THRESHOLDS = {0, 43200000, 86400000, 172800000};
    static final int MSG_CHECK_IDLE_STATES = 5;
    static final int MSG_CHECK_PACKAGE_IDLE_STATE = 11;
    static final int MSG_CHECK_PAROLE_TIMEOUT = 6;
    static final int MSG_FORCE_IDLE_STATE = 4;
    static final int MSG_INFORM_LISTENERS = 3;
    static final int MSG_ONE_TIME_CHECK_IDLE_STATES = 10;
    static final int MSG_PAROLE_END_TIMEOUT = 7;
    static final int MSG_PAROLE_STATE_CHANGED = 9;
    static final int MSG_REPORT_CONTENT_PROVIDER_USAGE = 8;
    static final int MSG_REPORT_EXEMPTED_SYNC_SCHEDULED = 12;
    static final int MSG_REPORT_EXEMPTED_SYNC_START = 13;
    static final int MSG_UPDATE_STABLE_CHARGING = 14;
    private static final long ONE_DAY = 86400000;
    private static final long ONE_HOUR = 3600000;
    private static final long ONE_MINUTE = 60000;
    static final long[] SCREEN_TIME_THRESHOLDS = {0, 0, 3600000, SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT};
    private static final String TAG = "AppStandbyController";
    static final int[] THRESHOLD_BUCKETS = {10, 20, 30, 40};
    private static final long WAIT_FOR_ADMIN_DATA_TIMEOUT_MS = 10000;
    static final ArrayList<StandbyUpdateRecord> sStandbyUpdatePool = new ArrayList<>(4);
    @GuardedBy("mActiveAdminApps")
    private final SparseArray<Set<String>> mActiveAdminApps;
    private final CountDownLatch mAdminDataAvailableLatch;
    volatile boolean mAppIdleEnabled;
    /* access modifiers changed from: private */
    @GuardedBy("mAppIdleLock")
    public AppIdleHistory mAppIdleHistory;
    /* access modifiers changed from: private */
    public final Object mAppIdleLock;
    long mAppIdleParoleDurationMillis;
    long mAppIdleParoleIntervalMillis;
    long mAppIdleParoleWindowMillis;
    boolean mAppIdleTempParoled;
    long[] mAppStandbyElapsedThresholds;
    long[] mAppStandbyScreenThresholds;
    private AppWidgetManager mAppWidgetManager;
    @GuardedBy("mAppIdleLock")
    private List<String> mCarrierPrivilegedApps;
    boolean mCharging;
    boolean mChargingStable;
    long mCheckIdleIntervalMillis;
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final DeviceStateReceiver mDeviceStateReceiver;
    private final DisplayManager.DisplayListener mDisplayListener;
    long mExemptedSyncScheduledDozeTimeoutMillis;
    long mExemptedSyncScheduledNonDozeTimeoutMillis;
    long mExemptedSyncStartTimeoutMillis;
    /* access modifiers changed from: private */
    public final AppStandbyHandler mHandler;
    @GuardedBy("mAppIdleLock")
    private boolean mHaveCarrierPrivilegedApps;
    Injector mInjector;
    private long mLastAppIdleParoledTime;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    long mNotificationSeenTimeoutMillis;
    @GuardedBy("mPackageAccessListeners")
    private ArrayList<UsageStatsManagerInternal.AppIdleStateChangeListener> mPackageAccessListeners;
    private PackageManager mPackageManager;
    private boolean mPendingInitializeDefaults;
    private volatile boolean mPendingOneTimeCheckIdleStates;
    private PowerManager mPowerManager;
    long mPredictionTimeoutMillis;
    long mStableChargingThresholdMillis;
    long mStrongUsageTimeoutMillis;
    long mSyncAdapterTimeoutMillis;
    long mSystemInteractionTimeoutMillis;
    private boolean mSystemServicesReady;
    long mSystemUpdateUsageTimeoutMillis;

    class AppStandbyHandler extends Handler {
        AppStandbyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 3:
                    StandbyUpdateRecord r = (StandbyUpdateRecord) msg.obj;
                    AppStandbyController.this.informListeners(r.packageName, r.userId, r.bucket, r.reason, r.isUserInteraction);
                    r.recycle();
                    return;
                case 4:
                    AppStandbyController appStandbyController = AppStandbyController.this;
                    String str = (String) msg.obj;
                    int i = msg.arg1;
                    if (msg.arg2 == 1) {
                        z = true;
                    }
                    appStandbyController.forceIdleState(str, i, z);
                    return;
                case 5:
                    if (AppStandbyController.this.checkIdleStates(msg.arg1) && AppStandbyController.this.mAppIdleEnabled) {
                        AppStandbyController.this.mHandler.sendMessageDelayed(AppStandbyController.this.mHandler.obtainMessage(5, msg.arg1, 0), AppStandbyController.this.mCheckIdleIntervalMillis);
                        return;
                    }
                    return;
                case 6:
                    AppStandbyController.this.checkParoleTimeout();
                    return;
                case 7:
                    AppStandbyController.this.setAppIdleParoled(false);
                    return;
                case 8:
                    SomeArgs args = (SomeArgs) msg.obj;
                    AppStandbyController.this.reportContentProviderUsage((String) args.arg1, (String) args.arg2, ((Integer) args.arg3).intValue());
                    args.recycle();
                    return;
                case 9:
                    AppStandbyController.this.informParoleStateChanged();
                    return;
                case 10:
                    AppStandbyController.this.mHandler.removeMessages(10);
                    AppStandbyController.this.waitForAdminData();
                    AppStandbyController.this.checkIdleStates(-1);
                    return;
                case 11:
                    AppStandbyController.this.checkAndUpdateStandbyState((String) msg.obj, msg.arg1, msg.arg2, AppStandbyController.this.mInjector.elapsedRealtime());
                    return;
                case 12:
                    AppStandbyController.this.reportExemptedSyncScheduled((String) msg.obj, msg.arg1);
                    return;
                case 13:
                    AppStandbyController.this.reportExemptedSyncStart((String) msg.obj, msg.arg1);
                    return;
                case 14:
                    AppStandbyController.this.updateChargingStableState();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    private class DeviceStateReceiver extends BroadcastReceiver {
        private DeviceStateReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003d  */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x0043  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0049  */
        /* JADX WARNING: Removed duplicated region for block: B:23:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -54942926) {
                if (action.equals("android.os.action.DISCHARGING")) {
                    c = 1;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 870701415) {
                if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                }
            } else if (hashCode == 948344062 && action.equals("android.os.action.CHARGING")) {
                c = 0;
                switch (c) {
                    case 0:
                        AppStandbyController.this.setChargingState(true);
                        return;
                    case 1:
                        AppStandbyController.this.setChargingState(false);
                        return;
                    case 2:
                        AppStandbyController.this.onDeviceIdleModeChanged();
                        return;
                    default:
                        return;
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    }

    static class Injector {
        private IBatteryStats mBatteryStats;
        int mBootPhase;
        private final Context mContext;
        private IDeviceIdleController mDeviceIdleController;
        private DisplayManager mDisplayManager;
        private final Looper mLooper;
        private PackageManagerInternal mPackageManagerInternal;
        private PowerManager mPowerManager;

        Injector(Context context, Looper looper) {
            this.mContext = context;
            this.mLooper = looper;
        }

        /* access modifiers changed from: package-private */
        public Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: package-private */
        public Looper getLooper() {
            return this.mLooper;
        }

        /* access modifiers changed from: package-private */
        public void onBootPhase(int phase) {
            if (phase == 500) {
                this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
                this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
            }
            this.mBootPhase = phase;
        }

        /* access modifiers changed from: package-private */
        public int getBootPhase() {
            return this.mBootPhase;
        }

        /* access modifiers changed from: package-private */
        public long elapsedRealtime() {
            return SystemClock.elapsedRealtime();
        }

        /* access modifiers changed from: package-private */
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        /* access modifiers changed from: package-private */
        public boolean isAppIdleEnabled() {
            boolean buildFlag = this.mContext.getResources().getBoolean(17956949);
            boolean runtimeFlag = Settings.Global.getInt(this.mContext.getContentResolver(), "app_standby_enabled", 1) == 1 && Settings.Global.getInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1) == 1;
            if (!buildFlag || !runtimeFlag) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean isCharging() {
            return ((BatteryManager) this.mContext.getSystemService(BatteryManager.class)).isCharging();
        }

        /* access modifiers changed from: package-private */
        public boolean isPowerSaveWhitelistExceptIdleApp(String packageName) throws RemoteException {
            return this.mDeviceIdleController.isPowerSaveWhitelistExceptIdleApp(packageName);
        }

        /* access modifiers changed from: package-private */
        public File getDataSystemDirectory() {
            return Environment.getDataSystemDirectory();
        }

        /* access modifiers changed from: package-private */
        public void noteEvent(int event, String packageName, int uid) throws RemoteException {
            this.mBatteryStats.noteEvent(event, packageName, uid);
        }

        /* access modifiers changed from: package-private */
        public boolean isPackageEphemeral(int userId, String packageName) {
            return this.mPackageManagerInternal.isPackageEphemeral(userId, packageName);
        }

        /* access modifiers changed from: package-private */
        public int[] getRunningUserIds() throws RemoteException {
            return ActivityManager.getService().getRunningUserIds();
        }

        /* access modifiers changed from: package-private */
        public boolean isDefaultDisplayOn() {
            return this.mDisplayManager.getDisplay(0).getState() == 2;
        }

        /* access modifiers changed from: package-private */
        public void registerDisplayListener(DisplayManager.DisplayListener listener, Handler handler) {
            this.mDisplayManager.registerDisplayListener(listener, handler);
        }

        /* access modifiers changed from: package-private */
        public String getActiveNetworkScorer() {
            return ((NetworkScoreManager) this.mContext.getSystemService("network_score")).getActiveScorerPackage();
        }

        public boolean isBoundWidgetPackage(AppWidgetManager appWidgetManager, String packageName, int userId) {
            return appWidgetManager.isBoundWidgetPackage(packageName, userId);
        }

        /* access modifiers changed from: package-private */
        public String getAppIdleSettings() {
            return Settings.Global.getString(this.mContext.getContentResolver(), "app_idle_constants");
        }

        public boolean isDeviceIdleMode() {
            return this.mPowerManager.isDeviceIdleMode();
        }
    }

    static class Lock {
        Lock() {
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                AppStandbyController.this.clearCarrierPrivilegedApps();
            }
            if (("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                AppStandbyController.this.clearAppIdleForPackage(intent.getData().getSchemeSpecificPart(), getSendingUserId());
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        public static final long DEFAULT_EXEMPTED_SYNC_SCHEDULED_DOZE_TIMEOUT = 14400000;
        public static final long DEFAULT_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_TIMEOUT = 600000;
        public static final long DEFAULT_EXEMPTED_SYNC_START_TIMEOUT = 600000;
        public static final long DEFAULT_NOTIFICATION_TIMEOUT = 43200000;
        public static final long DEFAULT_STABLE_CHARGING_THRESHOLD = 600000;
        public static final long DEFAULT_STRONG_USAGE_TIMEOUT = 3600000;
        public static final long DEFAULT_SYNC_ADAPTER_TIMEOUT = 600000;
        public static final long DEFAULT_SYSTEM_INTERACTION_TIMEOUT = 600000;
        public static final long DEFAULT_SYSTEM_UPDATE_TIMEOUT = 7200000;
        private static final String KEY_ELAPSED_TIME_THRESHOLDS = "elapsed_thresholds";
        private static final String KEY_EXEMPTED_SYNC_SCHEDULED_DOZE_HOLD_DURATION = "exempted_sync_scheduled_d_duration";
        private static final String KEY_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_HOLD_DURATION = "exempted_sync_scheduled_nd_duration";
        private static final String KEY_EXEMPTED_SYNC_START_HOLD_DURATION = "exempted_sync_start_duration";
        @Deprecated
        private static final String KEY_IDLE_DURATION = "idle_duration2";
        @Deprecated
        private static final String KEY_IDLE_DURATION_OLD = "idle_duration";
        private static final String KEY_NOTIFICATION_SEEN_HOLD_DURATION = "notification_seen_duration";
        private static final String KEY_PAROLE_DURATION = "parole_duration";
        private static final String KEY_PAROLE_INTERVAL = "parole_interval";
        private static final String KEY_PAROLE_WINDOW = "parole_window";
        private static final String KEY_PREDICTION_TIMEOUT = "prediction_timeout";
        private static final String KEY_SCREEN_TIME_THRESHOLDS = "screen_thresholds";
        private static final String KEY_STABLE_CHARGING_THRESHOLD = "stable_charging_threshold";
        private static final String KEY_STRONG_USAGE_HOLD_DURATION = "strong_usage_duration";
        private static final String KEY_SYNC_ADAPTER_HOLD_DURATION = "sync_adapter_duration";
        private static final String KEY_SYSTEM_INTERACTION_HOLD_DURATION = "system_interaction_duration";
        private static final String KEY_SYSTEM_UPDATE_HOLD_DURATION = "system_update_usage_duration";
        @Deprecated
        private static final String KEY_WALLCLOCK_THRESHOLD = "wallclock_threshold";
        private final KeyValueListParser mParser = new KeyValueListParser(',');

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void registerObserver() {
            ContentResolver cr = AppStandbyController.this.mContext.getContentResolver();
            cr.registerContentObserver(Settings.Global.getUriFor("app_idle_constants"), false, this);
            cr.registerContentObserver(Settings.Global.getUriFor("app_standby_enabled"), false, this);
            cr.registerContentObserver(Settings.Global.getUriFor("adaptive_battery_management_enabled"), false, this);
        }

        public void onChange(boolean selfChange) {
            updateSettings();
            AppStandbyController.this.postOneTimeCheckIdleStates();
        }

        /* access modifiers changed from: package-private */
        public void updateSettings() {
            AppStandbyController.this.setAppIdleEnabled(AppStandbyController.this.mInjector.isAppIdleEnabled());
            try {
                this.mParser.setString(AppStandbyController.this.mInjector.getAppIdleSettings());
            } catch (IllegalArgumentException e) {
                Slog.e(AppStandbyController.TAG, "Bad value for app idle settings: " + e.getMessage());
            }
            synchronized (AppStandbyController.this.mAppIdleLock) {
                AppStandbyController.this.mAppIdleParoleIntervalMillis = this.mParser.getDurationMillis(KEY_PAROLE_INTERVAL, 86400000);
                AppStandbyController.this.mAppIdleParoleWindowMillis = this.mParser.getDurationMillis(KEY_PAROLE_WINDOW, DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                AppStandbyController.this.mAppIdleParoleDurationMillis = this.mParser.getDurationMillis(KEY_PAROLE_DURATION, 600000);
                String screenThresholdsValue = this.mParser.getString(KEY_SCREEN_TIME_THRESHOLDS, null);
                AppStandbyController.this.mAppStandbyScreenThresholds = parseLongArray(screenThresholdsValue, AppStandbyController.SCREEN_TIME_THRESHOLDS);
                String elapsedThresholdsValue = this.mParser.getString(KEY_ELAPSED_TIME_THRESHOLDS, null);
                AppStandbyController.this.mAppStandbyElapsedThresholds = parseLongArray(elapsedThresholdsValue, AppStandbyController.ELAPSED_TIME_THRESHOLDS);
                AppStandbyController.this.mCheckIdleIntervalMillis = Math.min(AppStandbyController.this.mAppStandbyElapsedThresholds[1] / 4, 14400000);
                AppStandbyController.this.mStrongUsageTimeoutMillis = this.mParser.getDurationMillis(KEY_STRONG_USAGE_HOLD_DURATION, 3600000);
                AppStandbyController.this.mNotificationSeenTimeoutMillis = this.mParser.getDurationMillis(KEY_NOTIFICATION_SEEN_HOLD_DURATION, 43200000);
                AppStandbyController.this.mSystemUpdateUsageTimeoutMillis = this.mParser.getDurationMillis(KEY_SYSTEM_UPDATE_HOLD_DURATION, DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                AppStandbyController.this.mPredictionTimeoutMillis = this.mParser.getDurationMillis(KEY_PREDICTION_TIMEOUT, 43200000);
                AppStandbyController.this.mSyncAdapterTimeoutMillis = this.mParser.getDurationMillis(KEY_SYNC_ADAPTER_HOLD_DURATION, 600000);
                AppStandbyController.this.mExemptedSyncScheduledNonDozeTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_HOLD_DURATION, 600000);
                AppStandbyController.this.mExemptedSyncScheduledDozeTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_SCHEDULED_DOZE_HOLD_DURATION, 14400000);
                AppStandbyController.this.mExemptedSyncStartTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_START_HOLD_DURATION, 600000);
                AppStandbyController.this.mSystemInteractionTimeoutMillis = this.mParser.getDurationMillis(KEY_SYSTEM_INTERACTION_HOLD_DURATION, 600000);
                AppStandbyController.this.mStableChargingThresholdMillis = this.mParser.getDurationMillis(KEY_STABLE_CHARGING_THRESHOLD, 600000);
            }
        }

        /* access modifiers changed from: package-private */
        public long[] parseLongArray(String values, long[] defaults) {
            if (values == null || values.isEmpty()) {
                return defaults;
            }
            String[] thresholds = values.split(SliceClientPermissions.SliceAuthority.DELIMITER);
            if (thresholds.length != AppStandbyController.THRESHOLD_BUCKETS.length) {
                return defaults;
            }
            long[] array = new long[AppStandbyController.THRESHOLD_BUCKETS.length];
            int i = 0;
            while (i < AppStandbyController.THRESHOLD_BUCKETS.length) {
                try {
                    if (!thresholds[i].startsWith("P")) {
                        if (!thresholds[i].startsWith("p")) {
                            array[i] = Long.parseLong(thresholds[i]);
                            i++;
                        }
                    }
                    array[i] = Duration.parse(thresholds[i]).toMillis();
                    i++;
                } catch (NumberFormatException | DateTimeParseException e) {
                    return defaults;
                }
            }
            return array;
        }
    }

    public static class StandbyUpdateRecord {
        int bucket;
        boolean isUserInteraction;
        String packageName;
        int reason;
        int userId;

        StandbyUpdateRecord(String pkgName, int userId2, int bucket2, int reason2, boolean isInteraction) {
            this.packageName = pkgName;
            this.userId = userId2;
            this.bucket = bucket2;
            this.reason = reason2;
            this.isUserInteraction = isInteraction;
        }

        public static StandbyUpdateRecord obtain(String pkgName, int userId2, int bucket2, int reason2, boolean isInteraction) {
            synchronized (AppStandbyController.sStandbyUpdatePool) {
                int size = AppStandbyController.sStandbyUpdatePool.size();
                if (size < 1) {
                    StandbyUpdateRecord standbyUpdateRecord = new StandbyUpdateRecord(pkgName, userId2, bucket2, reason2, isInteraction);
                    return standbyUpdateRecord;
                }
                StandbyUpdateRecord r = AppStandbyController.sStandbyUpdatePool.remove(size - 1);
                r.packageName = pkgName;
                r.userId = userId2;
                r.bucket = bucket2;
                r.reason = reason2;
                r.isUserInteraction = isInteraction;
                return r;
            }
        }

        public void recycle() {
            synchronized (AppStandbyController.sStandbyUpdatePool) {
                AppStandbyController.sStandbyUpdatePool.add(this);
            }
        }
    }

    AppStandbyController(Context context, Looper looper) {
        this(new Injector(context, looper));
    }

    AppStandbyController(Injector injector) {
        this.mAppIdleLock = new Lock();
        this.mPackageAccessListeners = new ArrayList<>();
        this.mActiveAdminApps = new SparseArray<>();
        this.mAdminDataAvailableLatch = new CountDownLatch(1);
        this.mAppStandbyScreenThresholds = SCREEN_TIME_THRESHOLDS;
        this.mAppStandbyElapsedThresholds = ELAPSED_TIME_THRESHOLDS;
        this.mSystemServicesReady = false;
        this.mNetworkRequest = new NetworkRequest.Builder().build();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                AppStandbyController.this.mConnectivityManager.unregisterNetworkCallback(this);
                AppStandbyController.this.checkParoleTimeout();
            }
        };
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    boolean displayOn = AppStandbyController.this.isDisplayOn();
                    synchronized (AppStandbyController.this.mAppIdleLock) {
                        AppStandbyController.this.mAppIdleHistory.updateDisplay(displayOn, AppStandbyController.this.mInjector.elapsedRealtime());
                    }
                }
            }
        };
        this.mInjector = injector;
        this.mContext = this.mInjector.getContext();
        this.mHandler = new AppStandbyHandler(this.mInjector.getLooper());
        this.mPackageManager = this.mContext.getPackageManager();
        this.mDeviceStateReceiver = new DeviceStateReceiver();
        IntentFilter deviceStates = new IntentFilter("android.os.action.CHARGING");
        deviceStates.addAction("android.os.action.DISCHARGING");
        deviceStates.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        this.mContext.registerReceiver(this.mDeviceStateReceiver, deviceStates);
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory = new AppIdleHistory(this.mInjector.getDataSystemDirectory(), this.mInjector.elapsedRealtime());
        }
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleEnabled(boolean enabled) {
        this.mAppIdleEnabled = enabled;
    }

    public void onBootPhase(int phase) {
        this.mInjector.onBootPhase(phase);
        if (phase == 500) {
            Slog.d(TAG, "Setting app idle enabled state");
            setAppIdleEnabled(this.mInjector.isAppIdleEnabled());
            SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
            settingsObserver.registerObserver();
            settingsObserver.updateSettings();
            this.mAppWidgetManager = (AppWidgetManager) this.mContext.getSystemService(AppWidgetManager.class);
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
            this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
            this.mInjector.registerDisplayListener(this.mDisplayListener, this.mHandler);
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.updateDisplay(isDisplayOn(), this.mInjector.elapsedRealtime());
            }
            this.mSystemServicesReady = true;
            if (this.mPendingInitializeDefaults) {
                initializeDefaultsForSystemApps(0);
            }
            if (this.mPendingOneTimeCheckIdleStates) {
                postOneTimeCheckIdleStates();
            }
        } else if (phase == 1000) {
            setChargingState(this.mInjector.isCharging());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v6, resolved type: java.lang.String[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v16, resolved type: long} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v17, resolved type: java.lang.String[]} */
    /* JADX WARNING: type inference failed for: r16v5 */
    /* JADX WARNING: type inference failed for: r15v5 */
    /* JADX WARNING: type inference failed for: r16v8 */
    /* JADX WARNING: type inference failed for: r15v8 */
    /* JADX WARNING: type inference failed for: r16v12 */
    /* JADX WARNING: type inference failed for: r15v12 */
    /* JADX WARNING: type inference failed for: r15v15 */
    /* JADX WARNING: type inference failed for: r16v15 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public void reportContentProviderUsage(String authority, String providerPkgName, int userId) {
        Object obj;
        String[] packages;
        int i;
        int i2;
        String[] packages2;
        int i3;
        int i4;
        int i5 = userId;
        if (this.mAppIdleEnabled) {
            String[] packages3 = ContentResolver.getSyncAdapterPackagesForAuthorityAsUser(authority, i5);
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            int length = packages3.length;
            int i6 = 0;
            while (i6 < length) {
                String packageName = packages3[i6];
                try {
                    PackageInfo pi = this.mPackageManager.getPackageInfoAsUser(packageName, DumpState.DUMP_DEXOPT, i5);
                    if (pi == null) {
                        PackageInfo packageInfo = pi;
                        String str = packageName;
                        i2 = length;
                        i = i6;
                        packages = packages3;
                    } else if (pi.applicationInfo == null) {
                        PackageInfo packageInfo2 = pi;
                        String str2 = packageName;
                        i2 = length;
                        i = i6;
                        packages = packages3;
                    } else if (!packageName.equals(providerPkgName)) {
                        Object obj2 = this.mAppIdleLock;
                        synchronized (obj2) {
                            try {
                                String[] packages4 = packages;
                                ? r16 = i;
                                ? r15 = obj2;
                                AppIdleHistory appIdleHistory = this.mAppIdleHistory;
                                r16 = 0;
                                obj = obj2;
                                r15 = 0;
                                long j = elapsedRealtime + this.mSyncAdapterTimeoutMillis;
                                try {
                                    packages4 = j;
                                    AppIdleHistory.AppUsageHistory appUsage = appIdleHistory.reportUsage(packageName, i5, 10, 8, 0, j);
                                    PackageInfo packageInfo3 = pi;
                                    String str3 = packageName;
                                    int i7 = length;
                                    int i8 = i6;
                                    String[] strArr = packages3;
                                    r15 = i7;
                                    r16 = i8;
                                    packages4 = strArr;
                                    maybeInformListeners(packageName, i5, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
                                    i2 = i7;
                                    i = i8;
                                    packages = strArr;
                                } catch (Throwable th) {
                                    th = th;
                                    packages2 = packages4;
                                    i3 = r16;
                                    i4 = r15;
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                PackageInfo packageInfo4 = pi;
                                String str4 = packageName;
                                i3 = i6;
                                packages2 = packages3;
                                obj = obj2;
                                i4 = length;
                                throw th;
                            }
                        }
                    } else {
                        i2 = length;
                        i = i6;
                        packages = packages3;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    String str5 = packageName;
                    i2 = length;
                    i = i6;
                    packages = packages3;
                }
                i6 = i + 1;
                String str6 = authority;
                i5 = userId;
                length = i2;
                packages3 = packages;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportExemptedSyncScheduled(String packageName, int userId) {
        int bucketToPromote;
        int usageReason;
        long durationMillis;
        Object obj;
        if (this.mAppIdleEnabled) {
            if (!this.mInjector.isDeviceIdleMode()) {
                bucketToPromote = 10;
                usageReason = 11;
                durationMillis = this.mExemptedSyncScheduledNonDozeTimeoutMillis;
            } else {
                bucketToPromote = 20;
                usageReason = 12;
                durationMillis = this.mExemptedSyncScheduledDozeTimeoutMillis;
            }
            int bucketToPromote2 = bucketToPromote;
            int usageReason2 = usageReason;
            long durationMillis2 = durationMillis;
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            Object obj2 = this.mAppIdleLock;
            synchronized (obj2) {
                try {
                    AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, bucketToPromote2, usageReason2, 0, elapsedRealtime + durationMillis2);
                    obj = obj2;
                    maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportExemptedSyncStart(String packageName, int userId) {
        if (this.mAppIdleEnabled) {
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            synchronized (this.mAppIdleLock) {
                AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, 10, 13, 0, elapsedRealtime + this.mExemptedSyncStartTimeoutMillis);
                maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setChargingState(boolean charging) {
        synchronized (this.mAppIdleLock) {
            if (this.mCharging != charging) {
                this.mCharging = charging;
                if (charging) {
                    this.mHandler.sendEmptyMessageDelayed(14, this.mStableChargingThresholdMillis);
                } else {
                    this.mHandler.removeMessages(14);
                    updateChargingStableState();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateChargingStableState() {
        synchronized (this.mAppIdleLock) {
            if (this.mChargingStable != this.mCharging) {
                this.mChargingStable = this.mCharging;
                postParoleStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleParoled(boolean paroled) {
        synchronized (this.mAppIdleLock) {
            long now = this.mInjector.currentTimeMillis();
            if (this.mAppIdleTempParoled != paroled) {
                this.mAppIdleTempParoled = paroled;
                if (paroled) {
                    postParoleEndTimeout();
                } else {
                    this.mLastAppIdleParoledTime = now;
                    postNextParoleTimeout(now, false);
                }
                postParoleStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isParoledOrCharging() {
        boolean z = true;
        if (!this.mAppIdleEnabled) {
            return true;
        }
        synchronized (this.mAppIdleLock) {
            if (!this.mAppIdleTempParoled) {
                if (!this.mChargingStable) {
                    z = false;
                }
            }
        }
        return z;
    }

    private void postNextParoleTimeout(long now, boolean forced) {
        this.mHandler.removeMessages(6);
        long timeLeft = (this.mLastAppIdleParoledTime + this.mAppIdleParoleIntervalMillis) - now;
        if (forced) {
            timeLeft += this.mAppIdleParoleWindowMillis;
        }
        if (timeLeft < 0) {
            timeLeft = 0;
        }
        this.mHandler.sendEmptyMessageDelayed(6, timeLeft);
    }

    private void postParoleEndTimeout() {
        this.mHandler.removeMessages(7);
        this.mHandler.sendEmptyMessageDelayed(7, this.mAppIdleParoleDurationMillis);
    }

    private void postParoleStateChanged() {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    /* access modifiers changed from: package-private */
    public void postCheckIdleStates(int userId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, userId, 0));
    }

    /* access modifiers changed from: package-private */
    public void postOneTimeCheckIdleStates() {
        if (this.mInjector.getBootPhase() < 500) {
            this.mPendingOneTimeCheckIdleStates = true;
            return;
        }
        this.mHandler.sendEmptyMessage(10);
        this.mPendingOneTimeCheckIdleStates = false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkIdleStates(int checkUserId) {
        int i = checkUserId;
        if (!this.mAppIdleEnabled) {
            return false;
        }
        try {
            int[] runningUserIds = this.mInjector.getRunningUserIds();
            if (i != -1 && !ArrayUtils.contains(runningUserIds, i)) {
                return false;
            }
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            int p = 0;
            while (true) {
                int i2 = p;
                if (i2 >= runningUserIds.length) {
                    return true;
                }
                int userId = runningUserIds[i2];
                if (i == -1 || i == userId) {
                    List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, userId);
                    int packageCount = packages.size();
                    int p2 = 0;
                    while (true) {
                        int p3 = p2;
                        if (p3 >= packageCount) {
                            break;
                        }
                        PackageInfo pi = packages.get(p3);
                        String packageName = pi.packageName;
                        String str = packageName;
                        PackageInfo packageInfo = pi;
                        checkAndUpdateStandbyState(packageName, userId, pi.applicationInfo.uid, elapsedRealtime);
                        p2 = p3 + 1;
                        packageCount = packageCount;
                    }
                }
                p = i2 + 1;
            }
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00bf A[Catch:{ all -> 0x00ea, all -> 0x00ef }] */
    public void checkAndUpdateStandbyState(String packageName, int userId, int uid, long elapsedRealtime) {
        int uid2;
        Object obj;
        int reason;
        int newBucket;
        int newBucket2;
        int i;
        int reason2;
        String str = packageName;
        int i2 = userId;
        long j = elapsedRealtime;
        if (uid <= 0) {
            try {
                uid2 = this.mPackageManager.getPackageUidAsUser(str, i2);
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
        } else {
            uid2 = uid;
        }
        if (isAppSpecial(str, UserHandle.getAppId(uid2), i2)) {
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.setAppStandbyBucket(str, i2, j, 5, 256);
            }
            maybeInformListeners(str, i2, j, 5, 256, false);
        } else {
            Object obj2 = this.mAppIdleLock;
            synchronized (obj2) {
                try {
                    AppIdleHistory.AppUsageHistory app = this.mAppIdleHistory.getAppUsageHistory(str, i2, j);
                    int reason3 = app.bucketingReason;
                    int oldMainReason = reason3 & JobPackageTracker.EVENT_STOP_REASON_MASK;
                    if (oldMainReason != 1024) {
                        int oldBucket = app.currentBucket;
                        int newBucket3 = Math.max(oldBucket, 10);
                        boolean predictionLate = predictionTimedOut(app, j);
                        if (oldMainReason == 256 || oldMainReason == 768 || oldMainReason == 512 || predictionLate) {
                            if (predictionLate || app.lastPredictedBucket < 10 || app.lastPredictedBucket > 40) {
                                newBucket3 = getBucketForLocked(str, i2, j);
                                reason3 = 512;
                            } else {
                                newBucket3 = app.lastPredictedBucket;
                                reason3 = UsbTerminalTypes.TERMINAL_TELE_PHONELINE;
                            }
                        }
                        long elapsedTimeAdjusted = this.mAppIdleHistory.getElapsedTime(j);
                        if (newBucket3 >= 10 && app.bucketActiveTimeoutTime > elapsedTimeAdjusted) {
                            newBucket2 = 10;
                            reason2 = app.bucketingReason;
                        } else if (newBucket3 < 20 || app.bucketWorkingSetTimeoutTime <= elapsedTimeAdjusted) {
                            reason = reason3;
                            newBucket = newBucket3;
                            if (oldBucket >= newBucket) {
                                if (!predictionLate) {
                                    obj = obj2;
                                }
                            }
                            int newBucket4 = newBucket;
                            int i3 = oldBucket;
                            int i4 = oldMainReason;
                            this.mAppIdleHistory.setAppStandbyBucket(str, i2, j, newBucket4, reason);
                            obj = obj2;
                            maybeInformListeners(str, i2, j, newBucket4, reason, false);
                        } else {
                            newBucket2 = 20;
                            if (20 == oldBucket) {
                                i = app.bucketingReason;
                            } else {
                                i = UsbTerminalTypes.TERMINAL_OUT_LFSPEAKER;
                            }
                            reason2 = i;
                        }
                        reason = reason2;
                        newBucket = newBucket2;
                        if (oldBucket >= newBucket) {
                        }
                        int newBucket42 = newBucket;
                        int i32 = oldBucket;
                        int i42 = oldMainReason;
                        this.mAppIdleHistory.setAppStandbyBucket(str, i2, j, newBucket42, reason);
                        obj = obj2;
                        maybeInformListeners(str, i2, j, newBucket42, reason, false);
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    private boolean predictionTimedOut(AppIdleHistory.AppUsageHistory app, long elapsedRealtime) {
        return app.lastPredictedTime > 0 && this.mAppIdleHistory.getElapsedTime(elapsedRealtime) - app.lastPredictedTime > this.mPredictionTimeoutMillis;
    }

    private void maybeInformListeners(String packageName, int userId, long elapsedRealtime, int bucket, int reason, boolean userStartedInteracting) {
        synchronized (this.mAppIdleLock) {
            if (this.mAppIdleHistory.shouldInformListeners(packageName, userId, elapsedRealtime, bucket)) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(3, StandbyUpdateRecord.obtain(packageName, userId, bucket, reason, userStartedInteracting)));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mAppIdleLock")
    public int getBucketForLocked(String packageName, int userId, long elapsedRealtime) {
        return THRESHOLD_BUCKETS[this.mAppIdleHistory.getThresholdIndex(packageName, userId, elapsedRealtime, this.mAppStandbyScreenThresholds, this.mAppStandbyElapsedThresholds)];
    }

    /* access modifiers changed from: package-private */
    public void checkParoleTimeout() {
        boolean setParoled = false;
        boolean waitForNetwork = false;
        NetworkInfo activeNetwork = this.mConnectivityManager.getActiveNetworkInfo();
        boolean networkActive = activeNetwork != null && activeNetwork.isConnected();
        synchronized (this.mAppIdleLock) {
            long now = this.mInjector.currentTimeMillis();
            if (!this.mAppIdleTempParoled) {
                long timeSinceLastParole = now - this.mLastAppIdleParoledTime;
                if (timeSinceLastParole <= this.mAppIdleParoleIntervalMillis) {
                    postNextParoleTimeout(now, false);
                } else if (networkActive) {
                    setParoled = true;
                } else if (timeSinceLastParole > this.mAppIdleParoleIntervalMillis + this.mAppIdleParoleWindowMillis) {
                    setParoled = true;
                } else {
                    waitForNetwork = true;
                    postNextParoleTimeout(now, true);
                }
            }
        }
        if (waitForNetwork) {
            this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback);
        }
        if (setParoled) {
            setAppIdleParoled(true);
        }
    }

    private void notifyBatteryStats(String packageName, int userId, boolean idle) {
        try {
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, 8192, userId);
            if (idle) {
                this.mInjector.noteEvent(15, packageName, uid);
            } else {
                this.mInjector.noteEvent(16, packageName, uid);
            }
        } catch (PackageManager.NameNotFoundException | RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        setAppIdleParoled(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return;
     */
    public void onDeviceIdleModeChanged() {
        boolean paroled;
        boolean deviceIdle = this.mPowerManager.isDeviceIdleMode();
        synchronized (this.mAppIdleLock) {
            long timeSinceLastParole = this.mInjector.currentTimeMillis() - this.mLastAppIdleParoledTime;
            if (!deviceIdle && timeSinceLastParole >= this.mAppIdleParoleIntervalMillis) {
                paroled = true;
            } else if (deviceIdle) {
                paroled = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0105 A[Catch:{ all -> 0x010d, all -> 0x0113 }] */
    public void reportEvent(UsageEvents.Event event, long elapsedRealtime, int userId) {
        Object obj;
        long nextCheckTime;
        int prevBucket;
        UsageEvents.Event event2 = event;
        long j = elapsedRealtime;
        int i = userId;
        if (this.mAppIdleEnabled) {
            Object obj2 = this.mAppIdleLock;
            synchronized (obj2) {
                try {
                    boolean previouslyIdle = this.mAppIdleHistory.isIdle(event2.mPackage, i, j);
                    if (!(event2.mEventType == 1 || event2.mEventType == 2 || event2.mEventType == 6 || event2.mEventType == 7 || event2.mEventType == 10 || event2.mEventType == 14)) {
                        if (event2.mEventType != 13) {
                            int i2 = i;
                            obj = obj2;
                        }
                    }
                    AppIdleHistory.AppUsageHistory appHistory = this.mAppIdleHistory.getAppUsageHistory(event2.mPackage, i, j);
                    int prevBucket2 = appHistory.currentBucket;
                    int prevBucketReason = appHistory.bucketingReason;
                    int subReason = usageEventToSubReason(event2.mEventType);
                    int reason = 768 | subReason;
                    if (event2.mEventType == 10) {
                        prevBucket = prevBucket2;
                    } else if (event2.mEventType == 14) {
                        prevBucket = prevBucket2;
                    } else {
                        if (event2.mEventType == 6) {
                            this.mAppIdleHistory.reportUsage(appHistory, event2.mPackage, 10, subReason, 0, j + this.mSystemInteractionTimeoutMillis);
                            nextCheckTime = this.mSystemInteractionTimeoutMillis;
                            prevBucket = prevBucket2;
                        } else {
                            AppIdleHistory appIdleHistory = this.mAppIdleHistory;
                            String str = event2.mPackage;
                            prevBucket = prevBucket2;
                            appIdleHistory.reportUsage(appHistory, str, 10, subReason, j, j + this.mStrongUsageTimeoutMillis);
                            nextCheckTime = this.mStrongUsageTimeoutMillis;
                        }
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11, i, -1, event2.mPackage), nextCheckTime);
                        AppIdleHistory.AppUsageHistory appUsageHistory = appHistory;
                        int i3 = prevBucket;
                        int i4 = i;
                        obj = obj2;
                        maybeInformListeners(event2.mPackage, i, j, appHistory.currentBucket, reason, (appHistory.currentBucket == 10 || prevBucket == appHistory.currentBucket || (prevBucketReason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 768) ? false : true);
                        if (previouslyIdle) {
                            notifyBatteryStats(event2.mPackage, i4, false);
                        }
                    }
                    this.mAppIdleHistory.reportUsage(appHistory, event2.mPackage, 20, subReason, 0, j + this.mNotificationSeenTimeoutMillis);
                    nextCheckTime = this.mNotificationSeenTimeoutMillis;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11, i, -1, event2.mPackage), nextCheckTime);
                    if (appHistory.currentBucket == 10) {
                    }
                    AppIdleHistory.AppUsageHistory appUsageHistory2 = appHistory;
                    int i32 = prevBucket;
                    int i42 = i;
                    obj = obj2;
                    maybeInformListeners(event2.mPackage, i, j, appHistory.currentBucket, reason, (appHistory.currentBucket == 10 || prevBucket == appHistory.currentBucket || (prevBucketReason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 768) ? false : true);
                    if (previouslyIdle) {
                    }
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    private int usageEventToSubReason(int eventType) {
        switch (eventType) {
            case 1:
                return 4;
            case 2:
                return 5;
            case 6:
                return 1;
            case 7:
                return 3;
            case 10:
                return 2;
            case 13:
                return 10;
            case 14:
                return 9;
            default:
                return 0;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
        r0 = th;
     */
    public void forceIdleState(String packageName, int userId, boolean idle) {
        int standbyBucket;
        if (this.mAppIdleEnabled) {
            int appId = getAppId(packageName);
            if (appId >= 0) {
                long elapsedRealtime = this.mInjector.elapsedRealtime();
                boolean previouslyIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
                synchronized (this.mAppIdleLock) {
                    try {
                        standbyBucket = this.mAppIdleHistory.setIdle(packageName, userId, idle, elapsedRealtime);
                    } catch (Throwable th) {
                        th = th;
                        boolean z = previouslyIdle;
                        while (true) {
                            throw th;
                        }
                    }
                }
                boolean stillIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
                if (previouslyIdle != stillIdle) {
                    boolean z2 = previouslyIdle;
                    maybeInformListeners(packageName, userId, elapsedRealtime, standbyBucket, 1024, false);
                    if (!stillIdle) {
                        notifyBatteryStats(packageName, userId, idle);
                    }
                }
            }
        }
    }

    public void setLastJobRunTime(String packageName, int userId, long elapsedRealtime) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.setLastJobRunTime(packageName, userId, elapsedRealtime);
        }
    }

    public long getTimeSinceLastJobRun(String packageName, int userId) {
        long timeSinceLastJobRun;
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        synchronized (this.mAppIdleLock) {
            timeSinceLastJobRun = this.mAppIdleHistory.getTimeSinceLastJobRun(packageName, userId, elapsedRealtime);
        }
        return timeSinceLastJobRun;
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.onUserRemoved(userId);
            synchronized (this.mActiveAdminApps) {
                this.mActiveAdminApps.remove(userId);
            }
        }
    }

    private boolean isAppIdleUnfiltered(String packageName, int userId, long elapsedRealtime) {
        boolean isIdle;
        synchronized (this.mAppIdleLock) {
            isIdle = this.mAppIdleHistory.isIdle(packageName, userId, elapsedRealtime);
        }
        return isIdle;
    }

    /* access modifiers changed from: package-private */
    public void addListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
        synchronized (this.mPackageAccessListeners) {
            if (!this.mPackageAccessListeners.contains(listener)) {
                this.mPackageAccessListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
        synchronized (this.mPackageAccessListeners) {
            this.mPackageAccessListeners.remove(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public int getAppId(String packageName) {
        try {
            return this.mPackageManager.getApplicationInfo(packageName, 4194816).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAppIdleFilteredOrParoled(String packageName, int userId, long elapsedRealtime, boolean shouldObfuscateInstantApps) {
        if (isParoledOrCharging()) {
            return false;
        }
        if (shouldObfuscateInstantApps && this.mInjector.isPackageEphemeral(userId, packageName)) {
            return false;
        }
        return isAppIdleFiltered(packageName, getAppId(packageName), userId, elapsedRealtime);
    }

    /* access modifiers changed from: package-private */
    public boolean isAppSpecial(String packageName, int appId, int userId) {
        if (packageName == null) {
            return false;
        }
        if (!this.mAppIdleEnabled || appId < 10000 || packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME)) {
            return true;
        }
        if (this.mSystemServicesReady) {
            try {
                if (this.mInjector.isPowerSaveWhitelistExceptIdleApp(packageName) || isActiveDeviceAdmin(packageName, userId) || isActiveNetworkScorer(packageName)) {
                    return true;
                }
                if ((this.mAppWidgetManager != null && this.mInjector.isBoundWidgetPackage(this.mAppWidgetManager, packageName, userId)) || isDeviceProvisioningPackage(packageName)) {
                    return true;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        if (isCarrierApp(packageName)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isAppIdleFiltered(String packageName, int appId, int userId, long elapsedRealtime) {
        if (isAppSpecial(packageName, appId, userId)) {
            return false;
        }
        return isAppIdleUnfiltered(packageName, userId, elapsedRealtime);
    }

    /* access modifiers changed from: package-private */
    public int[] getIdleUidsForUser(int userId) {
        if (!this.mAppIdleEnabled) {
            return new int[0];
        }
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        try {
            ParceledListSlice<ApplicationInfo> slice = AppGlobals.getPackageManager().getInstalledApplications(0, userId);
            if (slice == null) {
                return new int[0];
            }
            List list = slice.getList();
            SparseIntArray uidStates = new SparseIntArray();
            int i = list.size() - 1;
            while (true) {
                int i2 = i;
                if (i2 < 0) {
                    break;
                }
                ApplicationInfo ai = (ApplicationInfo) list.get(i2);
                boolean idle = isAppIdleFiltered(ai.packageName, UserHandle.getAppId(ai.uid), userId, elapsedRealtime);
                int index = uidStates.indexOfKey(ai.uid);
                int i3 = 65536;
                if (index < 0) {
                    int i4 = ai.uid;
                    if (!idle) {
                        i3 = 0;
                    }
                    uidStates.put(i4, i3 + 1);
                } else {
                    int valueAt = uidStates.valueAt(index) + 1;
                    if (!idle) {
                        i3 = 0;
                    }
                    uidStates.setValueAt(index, valueAt + i3);
                }
                i = i2 - 1;
            }
            int numIdle = 0;
            for (int i5 = uidStates.size() - 1; i5 >= 0; i5--) {
                int value = uidStates.valueAt(i5);
                if ((value & 32767) == (value >> 16)) {
                    numIdle++;
                }
            }
            int[] res = new int[numIdle];
            int numIdle2 = 0;
            for (int i6 = uidStates.size() - 1; i6 >= 0; i6--) {
                int value2 = uidStates.valueAt(i6);
                if ((value2 & 32767) == (value2 >> 16)) {
                    res[numIdle2] = uidStates.keyAt(i6);
                    numIdle2++;
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleAsync(String packageName, boolean idle, int userId) {
        if (packageName != null && this.mAppIdleEnabled) {
            this.mHandler.obtainMessage(4, userId, idle, packageName).sendToTarget();
        }
    }

    public int getAppStandbyBucket(String packageName, int userId, long elapsedRealtime, boolean shouldObfuscateInstantApps) {
        int appStandbyBucket;
        if (!this.mAppIdleEnabled || (shouldObfuscateInstantApps && this.mInjector.isPackageEphemeral(userId, packageName))) {
            return 10;
        }
        synchronized (this.mAppIdleLock) {
            appStandbyBucket = this.mAppIdleHistory.getAppStandbyBucket(packageName, userId, elapsedRealtime);
        }
        return appStandbyBucket;
    }

    public List<AppStandbyInfo> getAppStandbyBuckets(int userId) {
        ArrayList<AppStandbyInfo> appStandbyBuckets;
        synchronized (this.mAppIdleLock) {
            appStandbyBuckets = this.mAppIdleHistory.getAppStandbyBuckets(userId, this.mAppIdleEnabled);
        }
        return appStandbyBuckets;
    }

    /* access modifiers changed from: package-private */
    public void setAppStandbyBucket(String packageName, int userId, int newBucket, int reason, long elapsedRealtime) {
        setAppStandbyBucket(packageName, userId, newBucket, reason, elapsedRealtime, false);
    }

    /* access modifiers changed from: package-private */
    public void setAppStandbyBucket(String packageName, int userId, int newBucket, int reason, long elapsedRealtime, boolean resetTimeout) {
        int reason2;
        int newBucket2;
        int newBucket3;
        int i;
        int i2 = newBucket;
        long j = elapsedRealtime;
        synchronized (this.mAppIdleLock) {
            try {
                String str = packageName;
                int i3 = userId;
                try {
                    AppIdleHistory.AppUsageHistory app = this.mAppIdleHistory.getAppUsageHistory(str, i3, j);
                    boolean predicted = (reason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 1280;
                    if (app.currentBucket >= 10) {
                        if ((app.currentBucket != 50 && i2 != 50) || !predicted) {
                            if ((65280 & app.bucketingReason) != 1024 || !predicted) {
                                if (predicted) {
                                    long elapsedTimeAdjusted = this.mAppIdleHistory.getElapsedTime(j);
                                    this.mAppIdleHistory.updateLastPrediction(app, elapsedTimeAdjusted, i2);
                                    if (i2 > 10 && app.bucketActiveTimeoutTime > elapsedTimeAdjusted) {
                                        newBucket3 = 10;
                                        i = app.bucketingReason;
                                    } else if (i2 > 20 && app.bucketWorkingSetTimeoutTime > elapsedTimeAdjusted) {
                                        newBucket3 = 20;
                                        if (app.currentBucket != 20) {
                                            i = UsbTerminalTypes.TERMINAL_OUT_LFSPEAKER;
                                        } else {
                                            i = app.bucketingReason;
                                        }
                                    }
                                    newBucket2 = newBucket3;
                                    reason2 = i;
                                    this.mAppIdleHistory.setAppStandbyBucket(str, i3, j, newBucket2, reason2, resetTimeout);
                                    maybeInformListeners(str, i3, j, newBucket2, reason2, false);
                                }
                                reason2 = reason;
                                newBucket2 = i2;
                                try {
                                    this.mAppIdleHistory.setAppStandbyBucket(str, i3, j, newBucket2, reason2, resetTimeout);
                                    maybeInformListeners(str, i3, j, newBucket2, reason2, false);
                                } catch (Throwable th) {
                                    th = th;
                                    int i4 = newBucket2;
                                    int i5 = reason2;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                            }
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                String str2 = packageName;
                int i6 = userId;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isActiveDeviceAdmin(String packageName, int userId) {
        boolean z;
        synchronized (this.mActiveAdminApps) {
            Set<String> adminPkgs = this.mActiveAdminApps.get(userId);
            z = adminPkgs != null && adminPkgs.contains(packageName);
        }
        return z;
    }

    public void addActiveDeviceAdmin(String adminPkg, int userId) {
        synchronized (this.mActiveAdminApps) {
            Set<String> adminPkgs = this.mActiveAdminApps.get(userId);
            if (adminPkgs == null) {
                adminPkgs = new ArraySet<>();
                this.mActiveAdminApps.put(userId, adminPkgs);
            }
            adminPkgs.add(adminPkg);
        }
    }

    public void setActiveAdminApps(Set<String> adminPkgs, int userId) {
        synchronized (this.mActiveAdminApps) {
            if (adminPkgs == null) {
                try {
                    this.mActiveAdminApps.remove(userId);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                this.mActiveAdminApps.put(userId, adminPkgs);
            }
        }
    }

    public void onAdminDataAvailable() {
        this.mAdminDataAvailableLatch.countDown();
    }

    /* access modifiers changed from: private */
    public void waitForAdminData() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin")) {
            ConcurrentUtils.waitForCountDownNoInterrupt(this.mAdminDataAvailableLatch, 10000, "Wait for admin data");
        }
    }

    /* access modifiers changed from: package-private */
    public Set<String> getActiveAdminAppsForTest(int userId) {
        Set<String> set;
        synchronized (this.mActiveAdminApps) {
            set = this.mActiveAdminApps.get(userId);
        }
        return set;
    }

    private boolean isDeviceProvisioningPackage(String packageName) {
        String deviceProvisioningPackage = this.mContext.getResources().getString(17039794);
        return deviceProvisioningPackage != null && deviceProvisioningPackage.equals(packageName);
    }

    private boolean isCarrierApp(String packageName) {
        synchronized (this.mAppIdleLock) {
            if (!this.mHaveCarrierPrivilegedApps) {
                fetchCarrierPrivilegedAppsLocked();
            }
            if (this.mCarrierPrivilegedApps == null) {
                return false;
            }
            boolean contains = this.mCarrierPrivilegedApps.contains(packageName);
            return contains;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCarrierPrivilegedApps() {
        synchronized (this.mAppIdleLock) {
            this.mHaveCarrierPrivilegedApps = false;
            this.mCarrierPrivilegedApps = null;
        }
    }

    @GuardedBy("mAppIdleLock")
    private void fetchCarrierPrivilegedAppsLocked() {
        this.mCarrierPrivilegedApps = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getPackagesWithCarrierPrivileges();
        this.mHaveCarrierPrivilegedApps = true;
    }

    private boolean isActiveNetworkScorer(String packageName) {
        return packageName != null && packageName.equals(this.mInjector.getActiveNetworkScorer());
    }

    /* access modifiers changed from: package-private */
    public void informListeners(String packageName, int userId, int bucket, int reason, boolean userInteraction) {
        boolean idle = bucket >= 40;
        synchronized (this.mPackageAccessListeners) {
            Iterator<UsageStatsManagerInternal.AppIdleStateChangeListener> it = this.mPackageAccessListeners.iterator();
            while (it.hasNext()) {
                UsageStatsManagerInternal.AppIdleStateChangeListener next = it.next();
                UsageStatsManagerInternal.AppIdleStateChangeListener listener = next;
                next.onAppIdleStateChanged(packageName, userId, idle, bucket, reason);
                if (userInteraction) {
                    listener.onUserInteractionStarted(packageName, userId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void informParoleStateChanged() {
        boolean paroled = isParoledOrCharging();
        synchronized (this.mPackageAccessListeners) {
            Iterator<UsageStatsManagerInternal.AppIdleStateChangeListener> it = this.mPackageAccessListeners.iterator();
            while (it.hasNext()) {
                it.next().onParoleStateChanged(paroled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flushToDisk(int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.writeAppIdleTimes(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void flushDurationsToDisk() {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.writeAppIdleDurations();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDisplayOn() {
        return this.mInjector.isDefaultDisplayOn();
    }

    /* access modifiers changed from: package-private */
    public void clearAppIdleForPackage(String packageName, int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.clearUsage(packageName, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeDefaultsForSystemApps(int userId) {
        Object obj;
        int i = userId;
        if (!this.mSystemServicesReady) {
            this.mPendingInitializeDefaults = true;
            return;
        }
        Slog.d(TAG, "Initializing defaults for system apps on user " + i + ", appIdleEnabled=" + this.mAppIdleEnabled);
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, i);
        int packageCount = packages.size();
        Object obj2 = this.mAppIdleLock;
        synchronized (obj2) {
            int i2 = 0;
            while (i2 < packageCount) {
                try {
                    PackageInfo pi = packages.get(i2);
                    String packageName = pi.packageName;
                    if (pi.applicationInfo == null || !pi.applicationInfo.isSystemApp()) {
                        obj = obj2;
                    } else {
                        obj = obj2;
                        PackageInfo packageInfo = pi;
                        this.mAppIdleHistory.reportUsage(packageName, i, 10, 6, 0, elapsedRealtime + this.mSystemUpdateUsageTimeoutMillis);
                    }
                    i2++;
                    obj2 = obj;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void postReportContentProviderUsage(String name, String packageName, int userId) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = name;
        args.arg2 = packageName;
        args.arg3 = Integer.valueOf(userId);
        this.mHandler.obtainMessage(8, args).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void postReportExemptedSyncScheduled(String packageName, int userId) {
        this.mHandler.obtainMessage(12, userId, 0, packageName).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void postReportExemptedSyncStart(String packageName, int userId) {
        this.mHandler.obtainMessage(13, userId, 0, packageName).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void dumpUser(IndentingPrintWriter idpw, int userId, String pkg) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.dump(idpw, userId, pkg);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpState(String[] args, PrintWriter pw) {
        synchronized (this.mAppIdleLock) {
            pw.println("Carrier privileged apps (have=" + this.mHaveCarrierPrivilegedApps + "): " + this.mCarrierPrivilegedApps);
        }
        pw.println();
        pw.println("Settings:");
        pw.print("  mCheckIdleIntervalMillis=");
        TimeUtils.formatDuration(this.mCheckIdleIntervalMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleIntervalMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleIntervalMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleWindowMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleWindowMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleDurationMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleDurationMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncScheduledNonDozeTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncScheduledNonDozeTimeoutMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncScheduledDozeTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncScheduledDozeTimeoutMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncStartTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncStartTimeoutMillis, pw);
        pw.println();
        pw.println();
        pw.print("mAppIdleEnabled=");
        pw.print(this.mAppIdleEnabled);
        pw.print(" mAppIdleTempParoled=");
        pw.print(this.mAppIdleTempParoled);
        pw.print(" mCharging=");
        pw.print(this.mCharging);
        pw.print(" mChargingStable=");
        pw.print(this.mChargingStable);
        pw.print(" mLastAppIdleParoledTime=");
        TimeUtils.formatDuration(this.mLastAppIdleParoledTime, pw);
        pw.println();
        pw.print("mScreenThresholds=");
        pw.println(Arrays.toString(this.mAppStandbyScreenThresholds));
        pw.print("mElapsedThresholds=");
        pw.println(Arrays.toString(this.mAppStandbyElapsedThresholds));
        pw.print("mStableChargingThresholdMillis=");
        TimeUtils.formatDuration(this.mStableChargingThresholdMillis, pw);
        pw.println();
    }
}
