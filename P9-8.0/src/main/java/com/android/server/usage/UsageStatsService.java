package com.android.server.usage;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.app.IUidObserver.Stub;
import android.app.admin.DevicePolicyManager;
import android.app.usage.ConfigurationStats;
import android.app.usage.IUsageStatsManager;
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
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.net.NetworkScoreManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
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
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.location.LocationFudger;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsageStatsService extends SystemService implements StatsUpdatedListener {
    static final boolean COMPRESS_TIME = false;
    static final boolean DEBUG = false;
    private static final boolean ENABLE_KERNEL_UPDATES = true;
    public static final boolean ENABLE_TIME_CHANGE_CORRECTION = SystemProperties.getBoolean("persist.debug.time_correction", true);
    private static final long FLUSH_INTERVAL = 1200000;
    private static final File KERNEL_COUNTER_FILE = new File("/proc/uid_procstat/set");
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
    @GuardedBy("mAppIdleLock")
    private AppIdleHistory mAppIdleHistory;
    private final Object mAppIdleLock = new Object();
    long mAppIdleParoleDurationMillis;
    long mAppIdleParoleIntervalMillis;
    long mAppIdleScreenThresholdMillis;
    boolean mAppIdleTempParoled;
    long mAppIdleWallclockThresholdMillis;
    AppOpsManager mAppOps;
    AppWidgetManager mAppWidgetManager;
    private IBatteryStats mBatteryStats;
    @GuardedBy("mAppIdleLock")
    private List<String> mCarrierPrivilegedApps;
    boolean mCharging;
    long mCheckIdleIntervalMillis;
    IDeviceIdleController mDeviceIdleController;
    private final DisplayListener mDisplayListener = new DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                boolean displayOn = UsageStatsService.this.isDisplayOn();
                synchronized (UsageStatsService.this.mAppIdleLock) {
                    UsageStatsService.this.mAppIdleHistory.updateDisplay(displayOn, SystemClock.elapsedRealtime());
                }
            }
        }
    };
    private DisplayManager mDisplayManager;
    Handler mHandler;
    @GuardedBy("mAppIdleLock")
    private boolean mHaveCarrierPrivilegedApps;
    private long mLastAppIdleParoledTime;
    private final Object mLock = new Object();
    @GuardedBy("mAppIdleLock")
    private ArrayList<AppIdleStateChangeListener> mPackageAccessListeners = new ArrayList();
    PackageManager mPackageManager;
    PackageManagerInternal mPackageManagerInternal;
    private volatile boolean mPendingOneTimeCheckIdleStates;
    private PowerManager mPowerManager;
    long mRealTimeSnapshot;
    private boolean mSystemServicesReady = false;
    long mSystemTimeSnapshot;
    private final IUidObserver mUidObserver = new Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            int newCounter = procState <= 2 ? 0 : 1;
            synchronized (UsageStatsService.this.mUidToKernelCounter) {
                if (newCounter != UsageStatsService.this.mUidToKernelCounter.get(uid, 0)) {
                    UsageStatsService.this.mUidToKernelCounter.put(uid, newCounter);
                    try {
                        FileUtils.stringToFile(UsageStatsService.KERNEL_COUNTER_FILE, uid + " " + newCounter);
                    } catch (IOException e) {
                        Slog.w(UsageStatsService.TAG, "Failed to update counter set: " + e);
                    }
                }
            }
            return;
        }

        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
        }

        public void onUidGone(int uid, boolean disabled) throws RemoteException {
            onUidStateChanged(uid, 18, 0);
        }

        public void onUidActive(int uid) throws RemoteException {
        }
    };
    private final SparseIntArray mUidToKernelCounter = new SparseIntArray();
    private File mUsageStatsDir;
    UserManager mUserManager;
    private final SparseArray<UserUsageStatsService> mUserState = new SparseArray();

    private final class BinderService extends IUsageStatsManager.Stub {
        /* synthetic */ BinderService(UsageStatsService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        private boolean hasPermission(String callingPackage) {
            boolean z = true;
            int callingUid = Binder.getCallingUid();
            if (callingUid == 1000) {
                return true;
            }
            int mode = UsageStatsService.this.mAppOps.checkOp(43, callingUid, callingPackage);
            if (mode == 3) {
                if (UsageStatsService.this.getContext().checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                    z = false;
                }
                return z;
            }
            if (mode != 0) {
                z = false;
            }
            return z;
        }

        public ParceledListSlice<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), UserHandle.getCallingUserId());
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                List<UsageStats> results = UsageStatsService.this.queryUsageStats(userId, bucketType, beginTime, endTime, obfuscateInstantApps);
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
            boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), UserHandle.getCallingUserId());
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                UsageEvents queryEvents = UsageStatsService.this.queryEvents(userId, beginTime, endTime, obfuscateInstantApps);
                return queryEvents;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isAppInactive(String packageName, int userId) {
            try {
                userId = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "isAppInactive", null);
                boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), userId);
                long token = Binder.clearCallingIdentity();
                try {
                    boolean isAppIdleFilteredOrParoled = UsageStatsService.this.isAppIdleFilteredOrParoled(packageName, userId, SystemClock.elapsedRealtime(), obfuscateInstantApps);
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
                userId = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setAppInactive", null);
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app idle state");
                long token = Binder.clearCallingIdentity();
                try {
                    if (UsageStatsService.this.getAppId(packageName) >= 0) {
                        UsageStatsService.this.setAppIdleAsync(packageName, idle, userId);
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
            if (DumpUtils.checkDumpAndUsageStatsPermission(UsageStatsService.this.getContext(), UsageStatsService.TAG, pw)) {
                UsageStatsService.this.dump(args, pw);
            }
        }

        public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) {
            if (packageName == null) {
                Slog.w(UsageStatsService.TAG, "Event report user selecting a null package");
                return;
            }
            Event event = new Event();
            event.mPackage = packageName;
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 9;
            event.mAction = action;
            event.mContentType = contentType;
            event.mContentAnnotations = annotations;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }
    }

    private class DeviceStateReceiver extends BroadcastReceiver {
        /* synthetic */ DeviceStateReceiver(UsageStatsService this$0, DeviceStateReceiver -this1) {
            this();
        }

        private DeviceStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                UsageStatsService usageStatsService = UsageStatsService.this;
                if (intent.getIntExtra("plugged", 0) != 0) {
                    z = true;
                }
                usageStatsService.setChargingState(z);
            } else if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
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
                case 0:
                    UsageStatsService.this.reportEvent((Event) msg.obj, msg.arg1);
                    return;
                case 1:
                    UsageStatsService.this.flushToDisk();
                    return;
                case 2:
                    UsageStatsService.this.onUserRemoved(msg.arg1);
                    return;
                case 3:
                    usageStatsService = UsageStatsService.this;
                    str = (String) msg.obj;
                    i = msg.arg1;
                    if (msg.arg2 != 1) {
                        z = false;
                    }
                    usageStatsService.informListeners(str, i, z);
                    return;
                case 4:
                    usageStatsService = UsageStatsService.this;
                    str = (String) msg.obj;
                    i = msg.arg1;
                    if (msg.arg2 != 1) {
                        z = false;
                    }
                    usageStatsService.forceIdleState(str, i, z);
                    return;
                case 5:
                    if (UsageStatsService.this.checkIdleStates(msg.arg1)) {
                        UsageStatsService.this.mHandler.sendMessageDelayed(UsageStatsService.this.mHandler.obtainMessage(5, msg.arg1, 0), UsageStatsService.this.mCheckIdleIntervalMillis);
                        return;
                    }
                    return;
                case 6:
                    UsageStatsService.this.checkParoleTimeout();
                    return;
                case 7:
                    UsageStatsService.this.setAppIdleParoled(false);
                    return;
                case 8:
                    SomeArgs args = msg.obj;
                    UsageStatsService.this.reportContentProviderUsage((String) args.arg1, (String) args.arg2, ((Integer) args.arg3).intValue());
                    args.recycle();
                    return;
                case 9:
                    UsageStatsService.this.informParoleStateChanged();
                    return;
                case 10:
                    UsageStatsService.this.mHandler.removeMessages(10);
                    UsageStatsService.this.checkIdleStates(-1);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    private final class LocalService extends UsageStatsManagerInternal {
        /* synthetic */ LocalService(UsageStatsService this$0, LocalService -this1) {
            this();
        }

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
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportEvent(ComponentName component, int userId, int eventType, int displayId) {
            if (component == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a component name");
                return;
            }
            Event event = new Event();
            event.mPackage = component.getPackageName();
            event.mClass = component.getClassName();
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = eventType;
            event.mDisplayId = displayId;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
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
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportConfigurationChange(Configuration config, int userId) {
            if (config == null) {
                Slog.w(UsageStatsService.TAG, "Configuration event reported with a null config");
                return;
            }
            Event event = new Event();
            event.mPackage = "android";
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 5;
            event.mConfiguration = new Configuration(config);
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportShortcutUsage(String packageName, String shortcutId, int userId) {
            if (packageName == null || shortcutId == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name or a shortcut ID");
                return;
            }
            Event event = new Event();
            event.mPackage = packageName.intern();
            event.mShortcutId = shortcutId.intern();
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 8;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportContentProviderUsage(String name, String packageName, int userId) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = name;
            args.arg2 = packageName;
            args.arg3 = Integer.valueOf(userId);
            UsageStatsService.this.mHandler.obtainMessage(8, args).sendToTarget();
        }

        public boolean isAppIdle(String packageName, int uidForAppId, int userId) {
            return UsageStatsService.this.isAppIdleFiltered(packageName, uidForAppId, userId, SystemClock.elapsedRealtime());
        }

        public int[] getIdleUidsForUser(int userId) {
            return UsageStatsService.this.getIdleUidsForUser(userId);
        }

        public boolean isAppIdleParoleOn() {
            return UsageStatsService.this.isParoledOrCharging();
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
            synchronized (UsageStatsService.this.mLock) {
                if (user == 0) {
                    byte[] backupPayload = UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).getBackupPayload(key);
                    return backupPayload;
                }
                return null;
            }
        }

        public void applyRestoredPayload(int user, String key, byte[] payload) {
            synchronized (UsageStatsService.this.mLock) {
                if (user == 0) {
                    UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).applyRestoredPayload(key, payload);
                }
            }
        }

        public List<UsageStats> queryUsageStatsForUser(int userId, int intervalType, long beginTime, long endTime, boolean obfuscateInstantApps) {
            return UsageStatsService.this.queryUsageStats(userId, intervalType, beginTime, endTime, obfuscateInstantApps);
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        /* synthetic */ PackageReceiver(UsageStatsService this$0, PackageReceiver -this1) {
            this();
        }

        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                UsageStatsService.this.clearCarrierPrivilegedApps();
            }
            if (("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) && (intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1) != 0) {
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
        private final KeyValueListParser mParser = new KeyValueListParser(',');

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void registerObserver() {
            UsageStatsService.this.getContext().getContentResolver().registerContentObserver(Global.getUriFor("app_idle_constants"), false, this);
        }

        public void onChange(boolean selfChange) {
            updateSettings();
            UsageStatsService.this.postOneTimeCheckIdleStates();
        }

        void updateSettings() {
            synchronized (UsageStatsService.this.mAppIdleLock) {
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
            return;
        }
    }

    private class UserActionsReceiver extends BroadcastReceiver {
        /* synthetic */ UserActionsReceiver(UsageStatsService this$0, UserActionsReceiver -this1) {
            this();
        }

        private UserActionsReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            String action = intent.getAction();
            if ("android.intent.action.USER_REMOVED".equals(action)) {
                if (userId >= 0) {
                    UsageStatsService.this.mHandler.obtainMessage(2, userId, 0).sendToTarget();
                }
            } else if ("android.intent.action.USER_STARTED".equals(action) && userId >= 0) {
                UsageStatsService.this.postCheckIdleStates(userId);
            }
        }
    }

    public UsageStatsService(Context context) {
        super(context);
    }

    public void onStart() {
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mUserManager = (UserManager) getContext().getSystemService("user");
        this.mPackageManager = getContext().getPackageManager();
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mHandler = new H(BackgroundThread.get().getLooper());
        this.mUsageStatsDir = new File(new File(Environment.getDataDirectory(), "system"), "usagestats");
        this.mUsageStatsDir.mkdirs();
        if (this.mUsageStatsDir.exists()) {
            IntentFilter filter = new IntentFilter("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_STARTED");
            getContext().registerReceiverAsUser(new UserActionsReceiver(this, null), UserHandle.ALL, filter, null, this.mHandler);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            getContext().registerReceiverAsUser(new PackageReceiver(this, null), UserHandle.ALL, packageFilter, null, this.mHandler);
            this.mAppIdleEnabled = getContext().getResources().getBoolean(17956941);
            if (this.mAppIdleEnabled) {
                IntentFilter deviceStates = new IntentFilter("android.intent.action.BATTERY_CHANGED");
                deviceStates.addAction("android.os.action.DISCHARGING");
                deviceStates.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
                getContext().registerReceiver(new DeviceStateReceiver(this, null), deviceStates);
            }
            synchronized (this.mLock) {
                cleanUpRemovedUsersLocked();
            }
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory = new AppIdleHistory(SystemClock.elapsedRealtime());
            }
            this.mRealTimeSnapshot = SystemClock.elapsedRealtime();
            this.mSystemTimeSnapshot = System.currentTimeMillis();
            -wrap2(UsageStatsManagerInternal.class, new LocalService(this, null));
            publishBinderService("usagestats", new BinderService(this, null));
            return;
        }
        throw new IllegalStateException("Usage stats directory does not exist: " + this.mUsageStatsDir.getAbsolutePath());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
            settingsObserver.registerObserver();
            settingsObserver.updateSettings();
            this.mAppWidgetManager = (AppWidgetManager) getContext().getSystemService(AppWidgetManager.class);
            this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
            this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
            this.mPowerManager = (PowerManager) getContext().getSystemService(PowerManager.class);
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.updateDisplay(isDisplayOn(), SystemClock.elapsedRealtime());
            }
            if (this.mPendingOneTimeCheckIdleStates) {
                postOneTimeCheckIdleStates();
            }
            if (KERNEL_COUNTER_FILE.exists()) {
                try {
                    ActivityManager.getService().registerUidObserver(this.mUidObserver, 3, -1, null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
            Slog.w(TAG, "Missing procfs interface: " + KERNEL_COUNTER_FILE);
            this.mSystemServicesReady = true;
        } else if (phase == 1000) {
            BatteryManager tBatteryManager = (BatteryManager) getContext().getSystemService(BatteryManager.class);
            if (tBatteryManager != null) {
                setChargingState(tBatteryManager.isCharging());
            } else {
                setChargingState(false);
            }
        }
    }

    private boolean isDisplayOn() {
        return this.mDisplayManager.getDisplay(0).getState() == 2;
    }

    public void onStatsUpdated() {
        this.mHandler.sendEmptyMessageDelayed(1, 1200000);
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
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, userId);
        int packageCount = packages.size();
        synchronized (this.mAppIdleLock) {
            for (int i = 0; i < packageCount; i++) {
                PackageInfo pi = (PackageInfo) packages.get(i);
                String packageName = pi.packageName;
                if (pi.applicationInfo != null && pi.applicationInfo.isSystemApp()) {
                    this.mAppIdleHistory.reportUsage(packageName, userId, elapsedRealtime);
                }
            }
        }
    }

    private boolean shouldObfuscateInstantAppsForCaller(int callingUid, int userId) {
        return this.mPackageManagerInternal.canAccessInstantApps(callingUid, userId) ^ 1;
    }

    void clearAppIdleForPackage(String packageName, int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.clearUsage(packageName, userId);
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
            for (i = 0; i < userCount; i++) {
                toDelete.remove(Integer.toString(((UserInfo) users.get(i)).id));
            }
            int deleteCount = toDelete.size();
            for (i = 0; i < deleteCount; i++) {
                deleteRecursively(new File(this.mUsageStatsDir, (String) toDelete.valueAt(i)));
            }
        }
    }

    void setChargingState(boolean charging) {
        synchronized (this.mAppIdleLock) {
            if (this.mCharging != charging) {
                this.mCharging = charging;
                postParoleStateChanged();
            }
        }
    }

    void setAppIdleParoled(boolean paroled) {
        synchronized (this.mAppIdleLock) {
            long now = System.currentTimeMillis();
            if (this.mAppIdleTempParoled != paroled) {
                this.mAppIdleTempParoled = paroled;
                if (paroled) {
                    postParoleEndTimeout();
                } else {
                    this.mLastAppIdleParoledTime = now;
                    postNextParoleTimeout(now);
                }
                postParoleStateChanged();
            }
        }
    }

    boolean isParoledOrCharging() {
        boolean z;
        synchronized (this.mAppIdleLock) {
            z = !this.mAppIdleTempParoled ? this.mCharging : true;
        }
        return z;
    }

    private void postNextParoleTimeout(long now) {
        this.mHandler.removeMessages(6);
        long timeLeft = (this.mLastAppIdleParoledTime + this.mAppIdleParoleIntervalMillis) - now;
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

    void postCheckIdleStates(int userId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, userId, 0));
    }

    void postOneTimeCheckIdleStates() {
        if (this.mDeviceIdleController == null) {
            this.mPendingOneTimeCheckIdleStates = true;
            return;
        }
        this.mHandler.sendEmptyMessage(10);
        this.mPendingOneTimeCheckIdleStates = false;
    }

    boolean checkIdleStates(int checkUserId) {
        if (!this.mAppIdleEnabled) {
            return false;
        }
        try {
            int[] runningUserIds = ActivityManager.getService().getRunningUserIds();
            if (checkUserId != -1 && (ArrayUtils.contains(runningUserIds, checkUserId) ^ 1) != 0) {
                return false;
            }
            long elapsedRealtime = SystemClock.elapsedRealtime();
            for (int userId : runningUserIds) {
                if (checkUserId == -1 || checkUserId == userId) {
                    List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, userId);
                    int packageCount = packages.size();
                    for (int p = 0; p < packageCount; p++) {
                        PackageInfo pi = (PackageInfo) packages.get(p);
                        String packageName = pi.packageName;
                        boolean isIdle = isAppIdleFiltered(packageName, UserHandle.getAppId(pi.applicationInfo.uid), userId, elapsedRealtime);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, userId, isIdle ? 1 : 0, packageName));
                        if (isIdle) {
                            synchronized (this.mAppIdleLock) {
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
        boolean setParoled = false;
        synchronized (this.mAppIdleLock) {
            long now = System.currentTimeMillis();
            if (!this.mAppIdleTempParoled) {
                if (now - this.mLastAppIdleParoledTime > this.mAppIdleParoleIntervalMillis) {
                    setParoled = true;
                } else {
                    postNextParoleTimeout(now);
                }
            }
        }
        if (setParoled) {
            setAppIdleParoled(true);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0028 A:{Splitter: B:0:0x0000, ExcHandler: android.content.pm.PackageManager.NameNotFoundException (e android.content.pm.PackageManager$NameNotFoundException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void notifyBatteryStats(String packageName, int userId, boolean idle) {
        try {
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, 8192, userId);
            if (this.mBatteryStats == null) {
                Slog.e(TAG, "mBatteryStats is null");
                return;
            }
            if (idle) {
                this.mBatteryStats.noteEvent(15, packageName, uid);
            } else {
                this.mBatteryStats.noteEvent(16, packageName, uid);
            }
        } catch (NameNotFoundException e) {
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001c, code:
            setAppIdleParoled(r1);
     */
    /* JADX WARNING: Missing block: B:11:0x001f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onDeviceIdleModeChanged() {
        boolean deviceIdle = this.mPowerManager.isDeviceIdleMode();
        synchronized (this.mAppIdleLock) {
            long timeSinceLastParole = System.currentTimeMillis() - this.mLastAppIdleParoledTime;
            boolean paroled;
            if (!deviceIdle && timeSinceLastParole >= this.mAppIdleParoleIntervalMillis) {
                paroled = true;
            } else if (deviceIdle) {
                paroled = false;
            }
        }
    }

    private static void deleteRecursively(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (File subFile : files) {
                deleteRecursively(subFile);
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
        if (Math.abs(diffSystemTime) > TIME_CHANGE_THRESHOLD_MILLIS && ENABLE_TIME_CHANGE_CORRECTION) {
            Slog.i(TAG, "Time changed in UsageStats by " + (diffSystemTime / 1000) + " seconds");
            int userCount = this.mUserState.size();
            for (int i = 0; i < userCount; i++) {
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
            this.mHandler.removeMessages(0);
            flushToDiskLocked();
        }
    }

    /* JADX WARNING: Missing block: B:27:0x006b, code:
            if (r14.mEventType != 7) goto L_0x0060;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void reportEvent(Event event, int userId) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            convertToSystemTimeLocked(event);
            if (event.getPackageName() != null && this.mPackageManagerInternal.isPackageEphemeral(userId, event.getPackageName())) {
                event.mFlags |= 1;
            }
            getUserDataAndInitializeIfNeededLocked(userId, timeNow).reportEvent(event);
            synchronized (this.mAppIdleLock) {
                boolean previouslyIdle = this.mAppIdleHistory.isIdle(event.mPackage, userId, elapsedRealtime);
                if (!(event.mEventType == 1 || event.mEventType == 2)) {
                    if (event.mEventType != 6) {
                    }
                }
                this.mAppIdleHistory.reportUsage(event.mPackage, userId, elapsedRealtime);
                if (previouslyIdle) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(3, userId, 0, event.mPackage));
                    notifyBatteryStats(event.mPackage, userId, false);
                }
            }
        }
    }

    void reportContentProviderUsage(String authority, String providerPkgName, int userId) {
        for (String packageName : ContentResolver.getSyncAdapterPackagesForAuthorityAsUser(authority, userId)) {
            try {
                PackageInfo pi = this.mPackageManager.getPackageInfoAsUser(packageName, DumpState.DUMP_DEXOPT, userId);
                if (!(pi == null || pi.applicationInfo == null || packageName.equals(providerPkgName))) {
                    setAppIdleAsync(packageName, false, userId);
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    void forceIdleState(String packageName, int userId, boolean idle) {
        int appId = getAppId(packageName);
        if (appId >= 0) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            boolean previouslyIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.setIdle(packageName, userId, idle, elapsedRealtime);
            }
            boolean stillIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
            if (previouslyIdle != stillIdle) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(3, userId, stillIdle ? 1 : 0, packageName));
                if (!stillIdle) {
                    notifyBatteryStats(packageName, userId, idle);
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
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.onUserRemoved(userId);
            }
            cleanUpRemovedUsersLocked();
        }
    }

    /* JADX WARNING: Missing block: B:23:0x004b, code:
            return r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    List<UsageStats> queryUsageStats(int userId, int bucketType, long beginTime, long endTime, boolean obfuscateInstantApps) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (validRange(timeNow, beginTime, endTime)) {
                List<UsageStats> list = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryUsageStats(bucketType, beginTime, endTime);
                if (list == null) {
                    return null;
                } else if (obfuscateInstantApps) {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        UsageStats stats = (UsageStats) list.get(i);
                        if (this.mPackageManagerInternal.isPackageEphemeral(userId, stats.mPackageName)) {
                            list.set(i, stats.getObfuscatedForInstantApp());
                        }
                    }
                }
            } else {
                return null;
            }
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

    UsageEvents queryEvents(int userId, long beginTime, long endTime, boolean shouldObfuscateInstantApps) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (validRange(timeNow, beginTime, endTime)) {
                UsageEvents queryEvents = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEvents(beginTime, endTime, shouldObfuscateInstantApps);
                return queryEvents;
            }
            return null;
        }
    }

    private boolean isAppIdleUnfiltered(String packageName, int userId, long elapsedRealtime) {
        boolean isIdle;
        synchronized (this.mAppIdleLock) {
            isIdle = this.mAppIdleHistory.isIdle(packageName, userId, elapsedRealtime);
        }
        return isIdle;
    }

    void addListener(AppIdleStateChangeListener listener) {
        synchronized (this.mAppIdleLock) {
            if (!this.mPackageAccessListeners.contains(listener)) {
                this.mPackageAccessListeners.add(listener);
            }
        }
    }

    void removeListener(AppIdleStateChangeListener listener) {
        synchronized (this.mAppIdleLock) {
            this.mPackageAccessListeners.remove(listener);
        }
    }

    int getAppId(String packageName) {
        try {
            return this.mPackageManager.getApplicationInfo(packageName, 4194816).uid;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    boolean isAppIdleFilteredOrParoled(String packageName, int userId, long elapsedRealtime, boolean shouldObfuscateInstantApps) {
        if (isParoledOrCharging()) {
            return false;
        }
        if (shouldObfuscateInstantApps && this.mPackageManagerInternal.isPackageEphemeral(userId, packageName)) {
            return false;
        }
        return isAppIdleFiltered(packageName, getAppId(packageName), userId, elapsedRealtime);
    }

    private boolean isAppIdleFiltered(String packageName, int appId, int userId, long elapsedRealtime) {
        if (packageName == null || !this.mAppIdleEnabled || appId < 10000 || packageName.equals("android")) {
            return false;
        }
        if (this.mSystemServicesReady) {
            try {
                if (this.mDeviceIdleController.isPowerSaveWhitelistExceptIdleApp(packageName) || isActiveDeviceAdmin(packageName, userId) || isActiveNetworkScorer(packageName)) {
                    return false;
                }
                if ((this.mAppWidgetManager != null && this.mAppWidgetManager.isBoundWidgetPackage(packageName, userId)) || isDeviceProvisioningPackage(packageName)) {
                    return false;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        if (isAppIdleUnfiltered(packageName, userId, elapsedRealtime) && !isCarrierApp(packageName)) {
            return true;
        }
        return false;
    }

    int[] getIdleUidsForUser(int userId) {
        if (!this.mAppIdleEnabled) {
            return new int[0];
        }
        long elapsedRealtime = SystemClock.elapsedRealtime();
        try {
            ParceledListSlice<ApplicationInfo> slice = AppGlobals.getPackageManager().getInstalledApplications(0, userId);
            if (slice == null) {
                return new int[0];
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
                        i2 = 65536;
                    } else {
                        i2 = 0;
                    }
                    uidStates.put(i3, i2 + 1);
                } else {
                    uidStates.setValueAt(index, (idle ? 65536 : 0) + (uidStates.valueAt(index) + 1));
                }
            }
            int numIdle = 0;
            for (i = uidStates.size() - 1; i >= 0; i--) {
                value = uidStates.valueAt(i);
                if ((value & 32767) == (value >> 16)) {
                    numIdle++;
                }
            }
            int[] res = new int[numIdle];
            numIdle = 0;
            for (i = uidStates.size() - 1; i >= 0; i--) {
                value = uidStates.valueAt(i);
                if ((value & 32767) == (value >> 16)) {
                    res[numIdle] = uidStates.keyAt(i);
                    numIdle++;
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    void setAppIdleAsync(String packageName, boolean idle, int userId) {
        if (packageName != null) {
            this.mHandler.obtainMessage(4, userId, idle ? 1 : 0, packageName).sendToTarget();
        }
    }

    private boolean isActiveDeviceAdmin(String packageName, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService(DevicePolicyManager.class);
        if (dpm == null) {
            return false;
        }
        return dpm.packageHasActiveAdmins(packageName, userId);
    }

    private boolean isDeviceProvisioningPackage(String packageName) {
        String deviceProvisioningPackage = getContext().getResources().getString(17039775);
        return deviceProvisioningPackage != null ? deviceProvisioningPackage.equals(packageName) : false;
    }

    private boolean isCarrierApp(String packageName) {
        synchronized (this.mAppIdleLock) {
            if (!this.mHaveCarrierPrivilegedApps) {
                fetchCarrierPrivilegedAppsLA();
            }
            if (this.mCarrierPrivilegedApps != null) {
                boolean contains = this.mCarrierPrivilegedApps.contains(packageName);
                return contains;
            }
            return false;
        }
    }

    void clearCarrierPrivilegedApps() {
        synchronized (this.mAppIdleLock) {
            this.mHaveCarrierPrivilegedApps = false;
            this.mCarrierPrivilegedApps = null;
        }
    }

    @GuardedBy("mAppIdleLock")
    private void fetchCarrierPrivilegedAppsLA() {
        this.mCarrierPrivilegedApps = ((TelephonyManager) getContext().getSystemService(TelephonyManager.class)).getPackagesWithCarrierPrivileges();
        this.mHaveCarrierPrivilegedApps = true;
    }

    private boolean isActiveNetworkScorer(String packageName) {
        return packageName != null ? packageName.equals(((NetworkScoreManager) getContext().getSystemService("network_score")).getActiveScorerPackage()) : false;
    }

    void informListeners(String packageName, int userId, boolean isIdle) {
        for (AppIdleStateChangeListener listener : new ArrayList(this.mPackageAccessListeners)) {
            listener.onAppIdleStateChanged(packageName, userId, isIdle);
        }
    }

    void informParoleStateChanged() {
        boolean paroled = isParoledOrCharging();
        for (AppIdleStateChangeListener listener : this.mPackageAccessListeners) {
            listener.onParoleStateChanged(paroled);
        }
    }

    private static boolean validRange(long currentTime, long beginTime, long endTime) {
        return beginTime <= currentTime && beginTime < endTime;
    }

    private void flushToDiskLocked() {
        int userCount = this.mUserState.size();
        for (int i = 0; i < userCount; i++) {
            ((UserUsageStatsService) this.mUserState.valueAt(i)).persistActiveStats();
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.writeAppIdleTimes(this.mUserState.keyAt(i));
            }
        }
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.writeAppIdleDurations();
        }
        this.mHandler.removeMessages(1);
    }

    void dump(String[] args, PrintWriter pw) {
        synchronized (this.mLock) {
            IndentingPrintWriter idpw = new IndentingPrintWriter(pw, "  ");
            ArraySet<String> argSet = new ArraySet();
            argSet.addAll(Arrays.asList(args));
            int userCount = this.mUserState.size();
            for (int i = 0; i < userCount; i++) {
                idpw.printPair("user", Integer.valueOf(this.mUserState.keyAt(i)));
                idpw.println();
                idpw.increaseIndent();
                if (argSet.contains("--checkin")) {
                    ((UserUsageStatsService) this.mUserState.valueAt(i)).checkin(idpw);
                } else {
                    ((UserUsageStatsService) this.mUserState.valueAt(i)).dump(idpw);
                    idpw.println();
                    if (args.length > 0) {
                        if ("history".equals(args[0])) {
                            synchronized (this.mAppIdleLock) {
                                this.mAppIdleHistory.dumpHistory(idpw, this.mUserState.keyAt(i));
                            }
                        } else if ("flush".equals(args[0])) {
                            flushToDiskLocked();
                            pw.println("Flushed stats to disk");
                        }
                    }
                }
                synchronized (this.mAppIdleLock) {
                    this.mAppIdleHistory.dump(idpw, this.mUserState.keyAt(i));
                }
                idpw.decreaseIndent();
            }
            pw.println();
            synchronized (this.mAppIdleLock) {
                pw.println("Carrier privileged apps (have=" + this.mHaveCarrierPrivilegedApps + "): " + this.mCarrierPrivilegedApps);
            }
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
            pw.print(" mAppIdleTempParoled=");
            pw.print(this.mAppIdleTempParoled);
            pw.print(" mCharging=");
            pw.print(this.mCharging);
            pw.print(" mLastAppIdleParoledTime=");
            TimeUtils.formatDuration(this.mLastAppIdleParoledTime, pw);
            pw.println();
        }
    }
}
