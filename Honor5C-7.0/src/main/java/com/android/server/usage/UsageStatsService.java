package com.android.server.usage;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.ConfigurationStats;
import android.app.usage.IUsageStatsManager.Stub;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManagerInternal;
import android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.NetworkScoreManager;
import android.os.Binder;
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
import android.os.UserManager;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.location.LocationFudger;
import com.android.server.power.AbsPowerManagerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsageStatsService extends SystemService implements StatsUpdatedListener {
    static final boolean COMPRESS_TIME = false;
    static final boolean DEBUG = false;
    private static final long FLUSH_INTERVAL = 1200000;
    static final int MSG_CHECK_IDLE_STATES = 5;
    static final int MSG_CHECK_PAROLE_TIMEOUT = 6;
    static final int MSG_FLUSH_TO_DISK = 1;
    static final int MSG_FORCE_IDLE_STATE = 4;
    static final int MSG_INFORM_LISTENERS = 3;
    static final int MSG_ONE_TIME_CHECK_IDLE_STATES = 10;
    static final int MSG_PAROLE_END_TIMEOUT = 7;
    static final int MSG_PAROLE_STATE_CHANGED = 9;
    static final int MSG_REMOVE_USER = 2;
    static final int MSG_REPORT_CONTENT_PROVIDER_USAGE = 8;
    static final int MSG_REPORT_EVENT = 0;
    private static final long ONE_MINUTE = 60000;
    static final String TAG = "UsageStatsService";
    private static final long TEN_SECONDS = 10000;
    private static final long TIME_CHANGE_THRESHOLD_MILLIS = 2000;
    private static final long TWENTY_MINUTES = 1200000;
    boolean mAppIdleEnabled;
    @GuardedBy("mLock")
    private AppIdleHistory mAppIdleHistory;
    long mAppIdleParoleDurationMillis;
    long mAppIdleParoleIntervalMillis;
    boolean mAppIdleParoled;
    long mAppIdleScreenThresholdMillis;
    long mAppIdleWallclockThresholdMillis;
    AppOpsManager mAppOps;
    AppWidgetManager mAppWidgetManager;
    private IBatteryStats mBatteryStats;
    private List<String> mCarrierPrivilegedApps;
    long mCheckIdleIntervalMillis;
    IDeviceIdleController mDeviceIdleController;
    private final DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    Handler mHandler;
    private boolean mHaveCarrierPrivilegedApps;
    private long mLastAppIdleParoledTime;
    private final Object mLock;
    private ArrayList<AppIdleStateChangeListener> mPackageAccessListeners;
    PackageManager mPackageManager;
    private volatile boolean mPendingOneTimeCheckIdleStates;
    private PowerManager mPowerManager;
    long mRealTimeSnapshot;
    private boolean mScreenOn;
    private boolean mSystemServicesReady;
    long mSystemTimeSnapshot;
    private File mUsageStatsDir;
    UserManager mUserManager;
    private final SparseArray<UserUsageStatsService> mUserState;

    private final class BinderService extends Stub {
        private BinderService() {
        }

        private boolean hasPermission(String callingPackage) {
            boolean z = true;
            int callingUid = Binder.getCallingUid();
            if (callingUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                return true;
            }
            int mode = UsageStatsService.this.mAppOps.checkOp(43, callingUid, callingPackage);
            if (mode == UsageStatsService.MSG_INFORM_LISTENERS) {
                if (UsageStatsService.this.getContext().checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                    z = UsageStatsService.DEBUG;
                }
                return z;
            }
            if (mode != 0) {
                z = UsageStatsService.DEBUG;
            }
            return z;
        }

        public ParceledListSlice<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                List<UsageStats> results = UsageStatsService.this.queryUsageStats(userId, bucketType, beginTime, endTime);
                if (results != null) {
                    ParceledListSlice<UsageStats> parceledListSlice = new ParceledListSlice(results);
                    return parceledListSlice;
                }
                Binder.restoreCallingIdentity(token);
                return null;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public ParceledListSlice<ConfigurationStats> queryConfigurationStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                List<ConfigurationStats> results = UsageStatsService.this.queryConfigurationStats(userId, bucketType, beginTime, endTime);
                if (results != null) {
                    ParceledListSlice<ConfigurationStats> parceledListSlice = new ParceledListSlice(results);
                    return parceledListSlice;
                }
                Binder.restoreCallingIdentity(token);
                return null;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEvents(long beginTime, long endTime, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                UsageEvents queryEvents = UsageStatsService.this.queryEvents(userId, beginTime, endTime);
                return queryEvents;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isAppInactive(String packageName, int userId) {
            try {
                userId = ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, UsageStatsService.DEBUG, true, "isAppInactive", null);
                long token = Binder.clearCallingIdentity();
                try {
                    boolean isAppIdleFilteredOrParoled = UsageStatsService.this.isAppIdleFilteredOrParoled(packageName, userId, SystemClock.elapsedRealtime());
                    return isAppIdleFilteredOrParoled;
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public void setAppInactive(String packageName, boolean idle, int userId) {
            try {
                userId = ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, UsageStatsService.DEBUG, true, "setAppIdle", null);
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app idle state");
                long token = Binder.clearCallingIdentity();
                try {
                    if (UsageStatsService.this.getAppId(packageName) >= 0) {
                        UsageStatsService.this.setAppIdle(packageName, idle, userId);
                        Binder.restoreCallingIdentity(token);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public void whitelistAppTemporarily(String packageName, long duration, int userId) throws RemoteException {
            StringBuilder reason = new StringBuilder(32);
            reason.append("from:");
            UserHandle.formatUid(reason, Binder.getCallingUid());
            UsageStatsService.this.mDeviceIdleController.addPowerSaveTempWhitelistApp(packageName, duration, userId, reason.toString());
        }

        public void onCarrierPrivilegedAppsChanged() {
            UsageStatsService.this.getContext().enforceCallingOrSelfPermission("android.permission.BIND_CARRIER_SERVICES", "onCarrierPrivilegedAppsChanged can only be called by privileged apps.");
            UsageStatsService.this.clearCarrierPrivilegedApps();
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (UsageStatsService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump UsageStats from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            } else {
                UsageStatsService.this.dump(args, pw);
            }
        }
    }

    private class DeviceStateReceiver extends BroadcastReceiver {
        private DeviceStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!"android.os.action.CHARGING".equals(action) && !"android.os.action.DISCHARGING".equals(action) && "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
                UsageStatsService.this.onDeviceIdleModeChanged();
            }
        }
    }

    class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            UsageStatsService usageStatsService;
            String str;
            int i;
            switch (msg.what) {
                case UsageStatsService.MSG_REPORT_EVENT /*0*/:
                    UsageStatsService.this.reportEvent((Event) msg.obj, msg.arg1);
                case UsageStatsService.MSG_FLUSH_TO_DISK /*1*/:
                    UsageStatsService.this.flushToDisk();
                case UsageStatsService.MSG_REMOVE_USER /*2*/:
                    UsageStatsService.this.onUserRemoved(msg.arg1);
                case UsageStatsService.MSG_INFORM_LISTENERS /*3*/:
                    usageStatsService = UsageStatsService.this;
                    str = (String) msg.obj;
                    i = msg.arg1;
                    if (msg.arg2 != UsageStatsService.MSG_FLUSH_TO_DISK) {
                        z = UsageStatsService.DEBUG;
                    }
                    usageStatsService.informListeners(str, i, z);
                case UsageStatsService.MSG_FORCE_IDLE_STATE /*4*/:
                    usageStatsService = UsageStatsService.this;
                    str = (String) msg.obj;
                    i = msg.arg1;
                    if (msg.arg2 != UsageStatsService.MSG_FLUSH_TO_DISK) {
                        z = UsageStatsService.DEBUG;
                    }
                    usageStatsService.forceIdleState(str, i, z);
                case UsageStatsService.MSG_CHECK_IDLE_STATES /*5*/:
                    if (UsageStatsService.this.checkIdleStates(msg.arg1)) {
                        UsageStatsService.this.mHandler.sendMessageDelayed(UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_CHECK_IDLE_STATES, msg.arg1, UsageStatsService.MSG_REPORT_EVENT), UsageStatsService.this.mCheckIdleIntervalMillis);
                    }
                case UsageStatsService.MSG_CHECK_PAROLE_TIMEOUT /*6*/:
                    UsageStatsService.this.checkParoleTimeout();
                case UsageStatsService.MSG_PAROLE_END_TIMEOUT /*7*/:
                    UsageStatsService.this.setAppIdleParoled(UsageStatsService.DEBUG);
                case UsageStatsService.MSG_REPORT_CONTENT_PROVIDER_USAGE /*8*/:
                    SomeArgs args = msg.obj;
                    UsageStatsService.this.reportContentProviderUsage((String) args.arg1, (String) args.arg2, ((Integer) args.arg3).intValue());
                    args.recycle();
                case UsageStatsService.MSG_PAROLE_STATE_CHANGED /*9*/:
                    UsageStatsService.this.informParoleStateChanged();
                case UsageStatsService.MSG_ONE_TIME_CHECK_IDLE_STATES /*10*/:
                    UsageStatsService.this.mHandler.removeMessages(UsageStatsService.MSG_ONE_TIME_CHECK_IDLE_STATES);
                    UsageStatsService.this.checkIdleStates(-1);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final class LocalService extends UsageStatsManagerInternal {
        private LocalService() {
        }

        public void reportEvent(ComponentName component, int userId, int eventType) {
            if (component == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a component name");
                return;
            }
            Event event = new Event();
            event.mPackage = component.getPackageName();
            event.mClass = component.getClassName();
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = eventType;
            UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_REPORT_EVENT, userId, UsageStatsService.MSG_REPORT_EVENT, event).sendToTarget();
        }

        public void reportEvent(String packageName, int userId, int eventType) {
            if (packageName == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name");
                return;
            }
            Event event = new Event();
            event.mPackage = packageName;
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = eventType;
            UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_REPORT_EVENT, userId, UsageStatsService.MSG_REPORT_EVENT, event).sendToTarget();
        }

        public void reportConfigurationChange(Configuration config, int userId) {
            if (config == null) {
                Slog.w(UsageStatsService.TAG, "Configuration event reported with a null config");
                return;
            }
            Event event = new Event();
            event.mPackage = "android";
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = UsageStatsService.MSG_CHECK_IDLE_STATES;
            event.mConfiguration = new Configuration(config);
            UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_REPORT_EVENT, userId, UsageStatsService.MSG_REPORT_EVENT, event).sendToTarget();
        }

        public void reportContentProviderUsage(String name, String packageName, int userId) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = name;
            args.arg2 = packageName;
            args.arg3 = Integer.valueOf(userId);
            UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_REPORT_CONTENT_PROVIDER_USAGE, args).sendToTarget();
        }

        public boolean isAppIdle(String packageName, int uidForAppId, int userId) {
            return UsageStatsService.this.isAppIdleFiltered(packageName, uidForAppId, userId, SystemClock.elapsedRealtime());
        }

        public int[] getIdleUidsForUser(int userId) {
            return UsageStatsService.this.getIdleUidsForUser(userId);
        }

        public boolean isAppIdleParoleOn() {
            return UsageStatsService.this.mAppIdleParoled;
        }

        public void prepareShutdown() {
            UsageStatsService.this.shutdown();
        }

        public void addAppIdleStateChangeListener(AppIdleStateChangeListener listener) {
            UsageStatsService.this.addListener(listener);
            listener.onParoleStateChanged(isAppIdleParoleOn());
        }

        public void removeAppIdleStateChangeListener(AppIdleStateChangeListener listener) {
            UsageStatsService.this.removeListener(listener);
        }

        public byte[] getBackupPayload(int user, String key) {
            if (user == 0) {
                return UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).getBackupPayload(key);
            }
            return null;
        }

        public void applyRestoredPayload(int user, String key, byte[] payload) {
            if (user == 0) {
                UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).applyRestoredPayload(key, payload);
            }
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                UsageStatsService.this.clearCarrierPrivilegedApps();
            }
            if (("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) && !intent.getBooleanExtra("android.intent.extra.REPLACING", UsageStatsService.DEBUG)) {
                UsageStatsService.this.clearAppIdleForPackage(intent.getData().getSchemeSpecificPart(), getSendingUserId());
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        private static final String KEY_IDLE_DURATION = "idle_duration2";
        @Deprecated
        private static final String KEY_IDLE_DURATION_OLD = "idle_duration";
        private static final String KEY_PAROLE_DURATION = "parole_duration";
        private static final String KEY_PAROLE_INTERVAL = "parole_interval";
        private static final String KEY_WALLCLOCK_THRESHOLD = "wallclock_threshold";
        private final KeyValueListParser mParser;

        SettingsObserver(Handler handler) {
            super(handler);
            this.mParser = new KeyValueListParser(',');
        }

        void registerObserver() {
            UsageStatsService.this.getContext().getContentResolver().registerContentObserver(Global.getUriFor("app_idle_constants"), UsageStatsService.DEBUG, this);
        }

        public void onChange(boolean selfChange) {
            updateSettings();
            UsageStatsService.this.postOneTimeCheckIdleStates();
        }

        void updateSettings() {
            synchronized (UsageStatsService.this.mLock) {
                try {
                    this.mParser.setString(Global.getString(UsageStatsService.this.getContext().getContentResolver(), "app_idle_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(UsageStatsService.TAG, "Bad value for app idle settings: " + e.getMessage());
                }
                UsageStatsService.this.mAppIdleScreenThresholdMillis = this.mParser.getLong(KEY_IDLE_DURATION, 43200000);
                UsageStatsService.this.mAppIdleWallclockThresholdMillis = this.mParser.getLong(KEY_WALLCLOCK_THRESHOLD, 172800000);
                UsageStatsService.this.mCheckIdleIntervalMillis = Math.min(UsageStatsService.this.mAppIdleScreenThresholdMillis / 4, 28800000);
                UsageStatsService.this.mAppIdleParoleIntervalMillis = this.mParser.getLong(KEY_PAROLE_INTERVAL, UnixCalendar.DAY_IN_MILLIS);
                UsageStatsService.this.mAppIdleParoleDurationMillis = this.mParser.getLong(KEY_PAROLE_DURATION, LocationFudger.FASTEST_INTERVAL_MS);
                UsageStatsService.this.mAppIdleHistory.setThresholds(UsageStatsService.this.mAppIdleWallclockThresholdMillis, UsageStatsService.this.mAppIdleScreenThresholdMillis);
            }
        }
    }

    private class UserActionsReceiver extends BroadcastReceiver {
        private UserActionsReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            String action = intent.getAction();
            if ("android.intent.action.USER_REMOVED".equals(action)) {
                if (userId >= 0) {
                    UsageStatsService.this.mHandler.obtainMessage(UsageStatsService.MSG_REMOVE_USER, userId, UsageStatsService.MSG_REPORT_EVENT).sendToTarget();
                }
            } else if ("android.intent.action.USER_STARTED".equals(action) && userId >= 0) {
                UsageStatsService.this.postCheckIdleStates(userId);
            }
        }
    }

    public UsageStatsService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mUserState = new SparseArray();
        this.mSystemServicesReady = DEBUG;
        this.mPackageAccessListeners = new ArrayList();
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    synchronized (UsageStatsService.this.mLock) {
                        UsageStatsService.this.mAppIdleHistory.updateDisplayLocked(UsageStatsService.this.isDisplayOn(), SystemClock.elapsedRealtime());
                    }
                }
            }
        };
    }

    public void onStart() {
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mUserManager = (UserManager) getContext().getSystemService("user");
        this.mPackageManager = getContext().getPackageManager();
        this.mHandler = new H(BackgroundThread.get().getLooper());
        this.mUsageStatsDir = new File(new File(Environment.getDataDirectory(), "system"), "usagestats");
        this.mUsageStatsDir.mkdirs();
        if (this.mUsageStatsDir.exists()) {
            IntentFilter filter = new IntentFilter("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_STARTED");
            getContext().registerReceiverAsUser(new UserActionsReceiver(), UserHandle.ALL, filter, null, this.mHandler);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            getContext().registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, this.mHandler);
            this.mAppIdleEnabled = getContext().getResources().getBoolean(17956883);
            if (this.mAppIdleEnabled) {
                IntentFilter deviceStates = new IntentFilter("android.os.action.CHARGING");
                deviceStates.addAction("android.os.action.DISCHARGING");
                deviceStates.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
                getContext().registerReceiver(new DeviceStateReceiver(), deviceStates);
            }
            synchronized (this.mLock) {
                cleanUpRemovedUsersLocked();
                this.mAppIdleHistory = new AppIdleHistory(SystemClock.elapsedRealtime());
            }
            this.mRealTimeSnapshot = SystemClock.elapsedRealtime();
            this.mSystemTimeSnapshot = System.currentTimeMillis();
            publishLocalService(UsageStatsManagerInternal.class, new LocalService());
            publishBinderService("usagestats", new BinderService());
            return;
        }
        throw new IllegalStateException("Usage stats directory does not exist: " + this.mUsageStatsDir.getAbsolutePath());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
            settingsObserver.registerObserver();
            settingsObserver.updateSettings();
            this.mAppWidgetManager = (AppWidgetManager) getContext().getSystemService(AppWidgetManager.class);
            this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
            this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
            this.mPowerManager = (PowerManager) getContext().getSystemService(PowerManager.class);
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
            synchronized (this.mLock) {
                this.mAppIdleHistory.updateDisplayLocked(isDisplayOn(), SystemClock.elapsedRealtime());
            }
            if (this.mPendingOneTimeCheckIdleStates) {
                postOneTimeCheckIdleStates();
            }
            this.mSystemServicesReady = true;
        } else if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            setAppIdleParoled(true);
            postParoleEndTimeout();
        }
    }

    private boolean isDisplayOn() {
        return this.mDisplayManager.getDisplay(MSG_REPORT_EVENT).getState() == MSG_REMOVE_USER ? true : DEBUG;
    }

    public void onStatsUpdated() {
        this.mHandler.sendEmptyMessageDelayed(MSG_FLUSH_TO_DISK, TWENTY_MINUTES);
    }

    public void onStatsReloaded() {
        postOneTimeCheckIdleStates();
    }

    public void onNewUpdate(int userId) {
        initializeDefaultsForSystemApps(userId);
    }

    private void initializeDefaultsForSystemApps(int userId) {
        Slog.d(TAG, "Initializing defaults for system apps on user " + userId);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(DumpState.DUMP_MESSAGES, userId);
        int packageCount = packages.size();
        for (int i = MSG_REPORT_EVENT; i < packageCount; i += MSG_FLUSH_TO_DISK) {
            PackageInfo pi = (PackageInfo) packages.get(i);
            String packageName = pi.packageName;
            if (pi.applicationInfo != null && pi.applicationInfo.isSystemApp()) {
                this.mAppIdleHistory.reportUsageLocked(packageName, userId, elapsedRealtime);
            }
        }
    }

    void clearAppIdleForPackage(String packageName, int userId) {
        synchronized (this.mLock) {
            this.mAppIdleHistory.clearUsageLocked(packageName, userId);
        }
    }

    private void cleanUpRemovedUsersLocked() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        if (users == null || users.size() == 0) {
            throw new IllegalStateException("There can't be no users");
        }
        ArraySet<String> toDelete = new ArraySet();
        String[] fileNames = this.mUsageStatsDir.list();
        if (fileNames != null) {
            int i;
            toDelete.addAll(Arrays.asList(fileNames));
            int userCount = users.size();
            for (i = MSG_REPORT_EVENT; i < userCount; i += MSG_FLUSH_TO_DISK) {
                toDelete.remove(Integer.toString(((UserInfo) users.get(i)).id));
            }
            int deleteCount = toDelete.size();
            for (i = MSG_REPORT_EVENT; i < deleteCount; i += MSG_FLUSH_TO_DISK) {
                deleteRecursively(new File(this.mUsageStatsDir, (String) toDelete.valueAt(i)));
            }
        }
    }

    void setAppIdleParoled(boolean paroled) {
        synchronized (this.mLock) {
            if (this.mAppIdleParoled != paroled) {
                this.mAppIdleParoled = paroled;
                if (paroled) {
                    postParoleEndTimeout();
                } else {
                    this.mLastAppIdleParoledTime = checkAndGetTimeLocked();
                    postNextParoleTimeout();
                }
                postParoleStateChanged();
            }
        }
    }

    private void postNextParoleTimeout() {
        this.mHandler.removeMessages(MSG_CHECK_PAROLE_TIMEOUT);
        long timeLeft = (this.mLastAppIdleParoledTime + this.mAppIdleParoleIntervalMillis) - checkAndGetTimeLocked();
        if (timeLeft < 0) {
            timeLeft = 0;
        }
        this.mHandler.sendEmptyMessageDelayed(MSG_CHECK_PAROLE_TIMEOUT, timeLeft);
    }

    private void postParoleEndTimeout() {
        this.mHandler.removeMessages(MSG_PAROLE_END_TIMEOUT);
        this.mHandler.sendEmptyMessageDelayed(MSG_PAROLE_END_TIMEOUT, this.mAppIdleParoleDurationMillis);
    }

    private void postParoleStateChanged() {
        this.mHandler.removeMessages(MSG_PAROLE_STATE_CHANGED);
        this.mHandler.sendEmptyMessage(MSG_PAROLE_STATE_CHANGED);
    }

    void postCheckIdleStates(int userId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_CHECK_IDLE_STATES, userId, MSG_REPORT_EVENT));
    }

    void postOneTimeCheckIdleStates() {
        if (this.mDeviceIdleController == null) {
            this.mPendingOneTimeCheckIdleStates = true;
            return;
        }
        this.mHandler.sendEmptyMessage(MSG_ONE_TIME_CHECK_IDLE_STATES);
        this.mPendingOneTimeCheckIdleStates = DEBUG;
    }

    boolean checkIdleStates(int checkUserId) {
        if (!this.mAppIdleEnabled) {
            return DEBUG;
        }
        try {
            int[] runningUserIds = ActivityManagerNative.getDefault().getRunningUserIds();
            if (checkUserId != -1 && !ArrayUtils.contains(runningUserIds, checkUserId)) {
                return DEBUG;
            }
            long elapsedRealtime = SystemClock.elapsedRealtime();
            for (int i = MSG_REPORT_EVENT; i < runningUserIds.length; i += MSG_FLUSH_TO_DISK) {
                int userId = runningUserIds[i];
                if (checkUserId == -1 || checkUserId == userId) {
                    List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(DumpState.DUMP_MESSAGES, userId);
                    int packageCount = packages.size();
                    for (int p = MSG_REPORT_EVENT; p < packageCount; p += MSG_FLUSH_TO_DISK) {
                        PackageInfo pi = (PackageInfo) packages.get(p);
                        String packageName = pi.packageName;
                        boolean isIdle = isAppIdleFiltered(packageName, UserHandle.getAppId(pi.applicationInfo.uid), userId, elapsedRealtime);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_INFORM_LISTENERS, userId, isIdle ? MSG_FLUSH_TO_DISK : MSG_REPORT_EVENT, packageName));
                        if (isIdle) {
                            synchronized (this.mLock) {
                                this.mAppIdleHistory.setIdle(packageName, userId, elapsedRealtime);
                            }
                        }
                    }
                    continue;
                }
            }
            return true;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    void checkParoleTimeout() {
        synchronized (this.mLock) {
            if (!this.mAppIdleParoled) {
                if (checkAndGetTimeLocked() - this.mLastAppIdleParoledTime > this.mAppIdleParoleIntervalMillis) {
                    setAppIdleParoled(true);
                } else {
                    postNextParoleTimeout();
                }
            }
        }
    }

    private void notifyBatteryStats(String packageName, int userId, boolean idle) {
        try {
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, DumpState.DUMP_PREFERRED_XML, userId);
            if (idle) {
                this.mBatteryStats.noteEvent(15, packageName, uid);
            } else {
                this.mBatteryStats.noteEvent(16, packageName, uid);
            }
        } catch (NameNotFoundException e) {
        }
    }

    void onDeviceIdleModeChanged() {
        boolean deviceIdle = this.mPowerManager.isDeviceIdleMode();
        synchronized (this.mLock) {
            long timeSinceLastParole = checkAndGetTimeLocked() - this.mLastAppIdleParoledTime;
            if (!deviceIdle && timeSinceLastParole >= this.mAppIdleParoleIntervalMillis) {
                setAppIdleParoled(true);
            } else if (deviceIdle) {
                setAppIdleParoled(DEBUG);
            }
        }
    }

    private static void deleteRecursively(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            int length = files.length;
            for (int i = MSG_REPORT_EVENT; i < length; i += MSG_FLUSH_TO_DISK) {
                deleteRecursively(files[i]);
            }
        }
        if (!f.delete()) {
            Slog.e(TAG, "Failed to delete " + f);
        }
    }

    private UserUsageStatsService getUserDataAndInitializeIfNeededLocked(int userId, long currentTimeMillis) {
        UserUsageStatsService service = (UserUsageStatsService) this.mUserState.get(userId);
        if (service != null) {
            return service;
        }
        service = new UserUsageStatsService(getContext(), userId, new File(this.mUsageStatsDir, Integer.toString(userId)), this);
        service.init(currentTimeMillis);
        this.mUserState.put(userId, service);
        return service;
    }

    private long checkAndGetTimeLocked() {
        long actualSystemTime = System.currentTimeMillis();
        long actualRealtime = SystemClock.elapsedRealtime();
        long expectedSystemTime = (actualRealtime - this.mRealTimeSnapshot) + this.mSystemTimeSnapshot;
        long diffSystemTime = actualSystemTime - expectedSystemTime;
        if (Math.abs(diffSystemTime) > TIME_CHANGE_THRESHOLD_MILLIS) {
            Slog.i(TAG, "Time changed in UsageStats by " + (diffSystemTime / 1000) + " seconds");
            int userCount = this.mUserState.size();
            for (int i = MSG_REPORT_EVENT; i < userCount; i += MSG_FLUSH_TO_DISK) {
                ((UserUsageStatsService) this.mUserState.valueAt(i)).onTimeChanged(expectedSystemTime, actualSystemTime);
            }
            this.mRealTimeSnapshot = actualRealtime;
            this.mSystemTimeSnapshot = actualSystemTime;
        }
        return actualSystemTime;
    }

    private void convertToSystemTimeLocked(Event event) {
        event.mTimeStamp = Math.max(0, event.mTimeStamp - this.mRealTimeSnapshot) + this.mSystemTimeSnapshot;
    }

    void shutdown() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(MSG_REPORT_EVENT);
            flushToDiskLocked();
        }
    }

    void reportEvent(Event event, int userId) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            convertToSystemTimeLocked(event);
            UserUsageStatsService service = getUserDataAndInitializeIfNeededLocked(userId, timeNow);
            boolean previouslyIdle = this.mAppIdleHistory.isIdleLocked(event.mPackage, userId, elapsedRealtime);
            service.reportEvent(event);
            if (!(event.mEventType == MSG_FLUSH_TO_DISK || event.mEventType == MSG_REMOVE_USER)) {
                if (event.mEventType != MSG_CHECK_PAROLE_TIMEOUT) {
                    if (event.mEventType == MSG_PAROLE_END_TIMEOUT) {
                    }
                }
            }
            this.mAppIdleHistory.reportUsageLocked(event.mPackage, userId, elapsedRealtime);
            if (previouslyIdle) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_INFORM_LISTENERS, userId, MSG_REPORT_EVENT, event.mPackage));
                notifyBatteryStats(event.mPackage, userId, DEBUG);
            }
        }
    }

    void reportContentProviderUsage(String authority, String providerPkgName, int userId) {
        String[] packages = ContentResolver.getSyncAdapterPackagesForAuthorityAsUser(authority, userId);
        int length = packages.length;
        for (int i = MSG_REPORT_EVENT; i < length; i += MSG_FLUSH_TO_DISK) {
            String packageName = packages[i];
            try {
                PackageInfo pi = this.mPackageManager.getPackageInfoAsUser(packageName, DumpState.DUMP_DEXOPT, userId);
                if (!(pi == null || pi.applicationInfo == null || packageName.equals(providerPkgName))) {
                    forceIdleState(packageName, userId, DEBUG);
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    void forceIdleState(String packageName, int userId, boolean idle) {
        int appId = getAppId(packageName);
        if (appId >= 0) {
            synchronized (this.mLock) {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                boolean previouslyIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
                this.mAppIdleHistory.setIdleLocked(packageName, userId, idle, elapsedRealtime);
                boolean stillIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
                if (previouslyIdle != stillIdle) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_INFORM_LISTENERS, userId, stillIdle ? MSG_FLUSH_TO_DISK : MSG_REPORT_EVENT, packageName));
                    if (!stillIdle) {
                        notifyBatteryStats(packageName, userId, idle);
                    }
                }
            }
        }
    }

    void flushToDisk() {
        synchronized (this.mLock) {
            flushToDiskLocked();
        }
    }

    void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            Slog.i(TAG, "Removing user " + userId + " and all data.");
            this.mUserState.remove(userId);
            this.mAppIdleHistory.onUserRemoved(userId);
            cleanUpRemovedUsersLocked();
        }
    }

    List<UsageStats> queryUsageStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (validRange(timeNow, beginTime, endTime)) {
                List<UsageStats> queryUsageStats = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryUsageStats(bucketType, beginTime, endTime);
                return queryUsageStats;
            }
            return null;
        }
    }

    List<ConfigurationStats> queryConfigurationStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (validRange(timeNow, beginTime, endTime)) {
                List<ConfigurationStats> queryConfigurationStats = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryConfigurationStats(bucketType, beginTime, endTime);
                return queryConfigurationStats;
            }
            return null;
        }
    }

    UsageEvents queryEvents(int userId, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (validRange(timeNow, beginTime, endTime)) {
                UsageEvents queryEvents = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEvents(beginTime, endTime);
                return queryEvents;
            }
            return null;
        }
    }

    private boolean isAppIdleUnfiltered(String packageName, int userId, long elapsedRealtime) {
        boolean isIdleLocked;
        synchronized (this.mLock) {
            isIdleLocked = this.mAppIdleHistory.isIdleLocked(packageName, userId, elapsedRealtime);
        }
        return isIdleLocked;
    }

    void addListener(AppIdleStateChangeListener listener) {
        synchronized (this.mLock) {
            if (!this.mPackageAccessListeners.contains(listener)) {
                this.mPackageAccessListeners.add(listener);
            }
        }
    }

    void removeListener(AppIdleStateChangeListener listener) {
        synchronized (this.mLock) {
            this.mPackageAccessListeners.remove(listener);
        }
    }

    int getAppId(String packageName) {
        try {
            return this.mPackageManager.getApplicationInfo(packageName, 8704).uid;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    boolean isAppIdleFilteredOrParoled(String packageName, int userId, long elapsedRealtime) {
        if (this.mAppIdleParoled) {
            return DEBUG;
        }
        return isAppIdleFiltered(packageName, getAppId(packageName), userId, elapsedRealtime);
    }

    private boolean isAppIdleFiltered(String packageName, int appId, int userId, long elapsedRealtime) {
        if (packageName == null || !this.mAppIdleEnabled || appId < AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT || packageName.equals("android")) {
            return DEBUG;
        }
        if (this.mSystemServicesReady) {
            try {
                if (this.mDeviceIdleController.isPowerSaveWhitelistExceptIdleApp(packageName) || isActiveDeviceAdmin(packageName, userId) || isActiveNetworkScorer(packageName)) {
                    return DEBUG;
                }
                if (this.mAppWidgetManager != null && this.mAppWidgetManager.isBoundWidgetPackage(packageName, userId)) {
                    return DEBUG;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        if (isAppIdleUnfiltered(packageName, userId, elapsedRealtime) && !isCarrierApp(packageName)) {
            return true;
        }
        return DEBUG;
    }

    int[] getIdleUidsForUser(int userId) {
        if (!this.mAppIdleEnabled) {
            return new int[MSG_REPORT_EVENT];
        }
        long elapsedRealtime = SystemClock.elapsedRealtime();
        try {
            ParceledListSlice<ApplicationInfo> slice = AppGlobals.getPackageManager().getInstalledApplications(MSG_REPORT_EVENT, userId);
            if (slice == null) {
                return new int[MSG_REPORT_EVENT];
            }
            int i;
            int value;
            List<ApplicationInfo> apps = slice.getList();
            SparseIntArray uidStates = new SparseIntArray();
            for (i = apps.size() - 1; i >= 0; i--) {
                ApplicationInfo ai = (ApplicationInfo) apps.get(i);
                boolean idle = isAppIdleFiltered(ai.packageName, UserHandle.getAppId(ai.uid), userId, elapsedRealtime);
                int index = uidStates.indexOfKey(ai.uid);
                if (index < 0) {
                    int i2;
                    int i3 = ai.uid;
                    if (idle) {
                        i2 = DumpState.DUMP_INSTALLS;
                    } else {
                        i2 = MSG_REPORT_EVENT;
                    }
                    uidStates.put(i3, i2 + MSG_FLUSH_TO_DISK);
                } else {
                    uidStates.setValueAt(index, (idle ? DumpState.DUMP_INSTALLS : MSG_REPORT_EVENT) + (uidStates.valueAt(index) + MSG_FLUSH_TO_DISK));
                }
            }
            int numIdle = MSG_REPORT_EVENT;
            for (i = uidStates.size() - 1; i >= 0; i--) {
                value = uidStates.valueAt(i);
                if ((value & 32767) == (value >> 16)) {
                    numIdle += MSG_FLUSH_TO_DISK;
                }
            }
            int[] res = new int[numIdle];
            numIdle = MSG_REPORT_EVENT;
            for (i = uidStates.size() - 1; i >= 0; i--) {
                value = uidStates.valueAt(i);
                if ((value & 32767) == (value >> 16)) {
                    res[numIdle] = uidStates.keyAt(i);
                    numIdle += MSG_FLUSH_TO_DISK;
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    void setAppIdle(String packageName, boolean idle, int userId) {
        if (packageName != null) {
            this.mHandler.obtainMessage(MSG_FORCE_IDLE_STATE, userId, idle ? MSG_FLUSH_TO_DISK : MSG_REPORT_EVENT, packageName).sendToTarget();
        }
    }

    private boolean isActiveDeviceAdmin(String packageName, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService(DevicePolicyManager.class);
        if (dpm == null) {
            return DEBUG;
        }
        return dpm.packageHasActiveAdmins(packageName, userId);
    }

    private boolean isCarrierApp(String packageName) {
        synchronized (this.mLock) {
            if (!this.mHaveCarrierPrivilegedApps) {
                fetchCarrierPrivilegedAppsLocked();
            }
            if (this.mCarrierPrivilegedApps != null) {
                boolean contains = this.mCarrierPrivilegedApps.contains(packageName);
                return contains;
            }
            return DEBUG;
        }
    }

    void clearCarrierPrivilegedApps() {
        synchronized (this.mLock) {
            this.mHaveCarrierPrivilegedApps = DEBUG;
            this.mCarrierPrivilegedApps = null;
        }
    }

    private void fetchCarrierPrivilegedAppsLocked() {
        this.mCarrierPrivilegedApps = ((TelephonyManager) getContext().getSystemService(TelephonyManager.class)).getPackagesWithCarrierPrivileges();
        this.mHaveCarrierPrivilegedApps = true;
    }

    private boolean isActiveNetworkScorer(String packageName) {
        return packageName != null ? packageName.equals(((NetworkScoreManager) getContext().getSystemService("network_score")).getActiveScorerPackage()) : DEBUG;
    }

    void informListeners(String packageName, int userId, boolean isIdle) {
        for (AppIdleStateChangeListener listener : new ArrayList(this.mPackageAccessListeners)) {
            listener.onAppIdleStateChanged(packageName, userId, isIdle);
        }
    }

    void informParoleStateChanged() {
        for (AppIdleStateChangeListener listener : this.mPackageAccessListeners) {
            listener.onParoleStateChanged(this.mAppIdleParoled);
        }
    }

    private static boolean validRange(long currentTime, long beginTime, long endTime) {
        return (beginTime > currentTime || beginTime >= endTime) ? DEBUG : true;
    }

    private void flushToDiskLocked() {
        int userCount = this.mUserState.size();
        for (int i = MSG_REPORT_EVENT; i < userCount; i += MSG_FLUSH_TO_DISK) {
            ((UserUsageStatsService) this.mUserState.valueAt(i)).persistActiveStats();
            this.mAppIdleHistory.writeAppIdleTimesLocked(this.mUserState.keyAt(i));
        }
        this.mAppIdleHistory.writeElapsedTimeLocked();
        this.mHandler.removeMessages(MSG_FLUSH_TO_DISK);
    }

    void dump(String[] args, PrintWriter pw) {
        synchronized (this.mLock) {
            IndentingPrintWriter idpw = new IndentingPrintWriter(pw, "  ");
            ArraySet<String> argSet = new ArraySet();
            argSet.addAll(Arrays.asList(args));
            int userCount = this.mUserState.size();
            for (int i = MSG_REPORT_EVENT; i < userCount; i += MSG_FLUSH_TO_DISK) {
                idpw.printPair("user", Integer.valueOf(this.mUserState.keyAt(i)));
                idpw.println();
                idpw.increaseIndent();
                if (argSet.contains("--checkin")) {
                    ((UserUsageStatsService) this.mUserState.valueAt(i)).checkin(idpw);
                } else {
                    ((UserUsageStatsService) this.mUserState.valueAt(i)).dump(idpw);
                    idpw.println();
                    if (args.length <= 0) {
                        continue;
                    } else if ("history".equals(args[MSG_REPORT_EVENT])) {
                        this.mAppIdleHistory.dumpHistory(idpw, this.mUserState.keyAt(i));
                    } else if ("flush".equals(args[MSG_REPORT_EVENT])) {
                        flushToDiskLocked();
                        pw.println("Flushed stats to disk");
                    }
                }
                this.mAppIdleHistory.dump(idpw, this.mUserState.keyAt(i));
                idpw.decreaseIndent();
            }
            pw.println();
            pw.println("Carrier privileged apps (have=" + this.mHaveCarrierPrivilegedApps + "): " + this.mCarrierPrivilegedApps);
            pw.println();
            pw.println("Settings:");
            pw.print("  mAppIdleDurationMillis=");
            TimeUtils.formatDuration(this.mAppIdleScreenThresholdMillis, pw);
            pw.println();
            pw.print("  mAppIdleWallclockThresholdMillis=");
            TimeUtils.formatDuration(this.mAppIdleWallclockThresholdMillis, pw);
            pw.println();
            pw.print("  mCheckIdleIntervalMillis=");
            TimeUtils.formatDuration(this.mCheckIdleIntervalMillis, pw);
            pw.println();
            pw.print("  mAppIdleParoleIntervalMillis=");
            TimeUtils.formatDuration(this.mAppIdleParoleIntervalMillis, pw);
            pw.println();
            pw.print("  mAppIdleParoleDurationMillis=");
            TimeUtils.formatDuration(this.mAppIdleParoleDurationMillis, pw);
            pw.println();
            pw.println();
            pw.print("mAppIdleEnabled=");
            pw.print(this.mAppIdleEnabled);
            pw.print(" mAppIdleParoled=");
            pw.print(this.mAppIdleParoled);
            pw.print(" mScreenOn=");
            pw.println(this.mScreenOn);
            pw.print("mLastAppIdleParoledTime=");
            TimeUtils.formatDuration(this.mLastAppIdleParoledTime, pw);
            pw.println();
        }
    }
}
