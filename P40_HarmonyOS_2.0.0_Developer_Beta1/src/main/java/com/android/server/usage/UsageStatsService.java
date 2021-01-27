package com.android.server.usage;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManagerInternal;
import android.app.usage.AppStandbyInfo;
import android.app.usage.ConfigurationStats;
import android.app.usage.EventStats;
import android.app.usage.IUsageStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usage.AppTimeLimitController;
import com.android.server.usage.UserUsageStatsService;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UsageStatsService extends SystemService implements UserUsageStatsService.StatsUpdatedListener {
    static final boolean COMPRESS_TIME = false;
    static final boolean DEBUG = false;
    private static final boolean ENABLE_KERNEL_UPDATES = true;
    public static final boolean ENABLE_TIME_CHANGE_CORRECTION = SystemProperties.getBoolean("persist.debug.time_correction", true);
    private static final long FLUSH_INTERVAL = 1200000;
    private static final File KERNEL_COUNTER_FILE = new File("/proc/uid_procstat/set");
    static final int MSG_FLUSH_TO_DISK = 1;
    static final int MSG_REMOVE_USER = 2;
    static final int MSG_REPORT_EVENT = 0;
    static final int MSG_REPORT_EVENT_TO_ALL_USERID = 4;
    static final int MSG_UID_STATE_CHANGED = 3;
    static final String TAG = "UsageStatsService";
    private static final long TEN_SECONDS = 10000;
    private static final long TIME_CHANGE_THRESHOLD_MILLIS = 2000;
    private static final char TOKEN_DELIMITER = '/';
    private static final long TWENTY_MINUTES = 1200000;
    AppOpsManager mAppOps;
    AppStandbyController mAppStandby;
    AppTimeLimitController mAppTimeLimit;
    IDeviceIdleController mDeviceIdleController;
    DevicePolicyManagerInternal mDpmInternal;
    Handler mHandler;
    private final Object mLock = new Object();
    PackageManager mPackageManager;
    PackageManagerInternal mPackageManagerInternal;
    PackageMonitor mPackageMonitor;
    long mRealTimeSnapshot;
    private UsageStatsManagerInternal.AppIdleStateChangeListener mStandbyChangeListener = new UsageStatsManagerInternal.AppIdleStateChangeListener() {
        /* class com.android.server.usage.UsageStatsService.AnonymousClass1 */

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            UsageEvents.Event event = new UsageEvents.Event(11, SystemClock.elapsedRealtime());
            event.mBucketAndReason = (bucket << 16) | (65535 & reason);
            event.mPackage = packageName;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void onParoleStateChanged(boolean isParoleOn) {
        }
    };
    long mSystemTimeSnapshot;
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.usage.UsageStatsService.AnonymousClass3 */

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            UsageStatsService.this.mHandler.obtainMessage(3, uid, procState).sendToTarget();
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidGone(int uid, boolean disabled) {
            onUidStateChanged(uid, 21, 0);
        }

        public void onUidActive(int uid) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    private final SparseIntArray mUidToKernelCounter = new SparseIntArray();
    final SparseArray<ArraySet<String>> mUsageReporters = new SparseArray<>();
    int mUsageSource;
    private File mUsageStatsDir;
    UserManager mUserManager;
    private final SparseArray<UserUsageStatsService> mUserState = new SparseArray<>();
    final SparseArray<ActivityData> mVisibleActivities = new SparseArray<>();

    /* access modifiers changed from: private */
    public static class ActivityData {
        private final String mTaskRootClass;
        private final String mTaskRootPackage;

        private ActivityData(String taskRootPackage, String taskRootClass) {
            this.mTaskRootPackage = taskRootPackage;
            this.mTaskRootClass = taskRootClass;
        }
    }

    public UsageStatsService(Context context) {
        super(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: com.android.server.usage.UsageStatsService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v8, types: [com.android.server.usage.UsageStatsService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mUserManager = (UserManager) getContext().getSystemService("user");
        this.mPackageManager = getContext().getPackageManager();
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mHandler = new H(BackgroundThread.get().getLooper());
        this.mAppStandby = new AppStandbyController(getContext(), BackgroundThread.get().getLooper());
        this.mAppTimeLimit = new AppTimeLimitController(new AppTimeLimitController.TimeLimitCallbackListener() {
            /* class com.android.server.usage.UsageStatsService.AnonymousClass2 */

            @Override // com.android.server.usage.AppTimeLimitController.TimeLimitCallbackListener
            public void onLimitReached(int observerId, int userId, long timeLimit, long timeElapsed, PendingIntent callbackIntent) {
                if (callbackIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra("android.app.usage.extra.OBSERVER_ID", observerId);
                    intent.putExtra("android.app.usage.extra.TIME_LIMIT", timeLimit);
                    intent.putExtra("android.app.usage.extra.TIME_USED", timeElapsed);
                    try {
                        callbackIntent.send(UsageStatsService.this.getContext(), 0, intent);
                    } catch (PendingIntent.CanceledException e) {
                        Slog.w(UsageStatsService.TAG, "Couldn't deliver callback: " + callbackIntent);
                    }
                }
            }

            @Override // com.android.server.usage.AppTimeLimitController.TimeLimitCallbackListener
            public void onSessionEnd(int observerId, int userId, long timeElapsed, PendingIntent callbackIntent) {
                if (callbackIntent != null) {
                    Intent intent = new Intent();
                    intent.putExtra("android.app.usage.extra.OBSERVER_ID", observerId);
                    intent.putExtra("android.app.usage.extra.TIME_USED", timeElapsed);
                    try {
                        callbackIntent.send(UsageStatsService.this.getContext(), 0, intent);
                    } catch (PendingIntent.CanceledException e) {
                        Slog.w(UsageStatsService.TAG, "Couldn't deliver callback: " + callbackIntent);
                    }
                }
            }
        }, this.mHandler.getLooper());
        this.mAppStandby.addListener(this.mStandbyChangeListener);
        this.mUsageStatsDir = new File(new File(Environment.getDataDirectory(), "system"), "usagestats");
        this.mUsageStatsDir.mkdirs();
        if (this.mUsageStatsDir.exists()) {
            IntentFilter filter = new IntentFilter("android.intent.action.USER_REMOVED");
            filter.addAction("android.intent.action.USER_STARTED");
            getContext().registerReceiverAsUser(new UserActionsReceiver(), UserHandle.ALL, filter, null, this.mHandler);
            synchronized (this.mLock) {
                cleanUpRemovedUsersLocked();
            }
            this.mRealTimeSnapshot = SystemClock.elapsedRealtime();
            this.mSystemTimeSnapshot = System.currentTimeMillis();
            publishLocalService(UsageStatsManagerInternal.class, new LocalService());
            publishBinderService("usagestats", new BinderService());
            getUserDataAndInitializeIfNeededLocked(0, this.mSystemTimeSnapshot);
            return;
        }
        throw new IllegalStateException("Usage stats directory does not exist: " + this.mUsageStatsDir.getAbsolutePath());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        this.mAppStandby.onBootPhase(phase);
        if (phase == 500) {
            getDpmInternal();
            this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            if (KERNEL_COUNTER_FILE.exists()) {
                try {
                    ActivityManager.getService().registerUidObserver(this.mUidObserver, 3, -1, (String) null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Slog.w(TAG, "Missing procfs interface: " + KERNEL_COUNTER_FILE);
            }
            readUsageSourceSetting();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DevicePolicyManagerInternal getDpmInternal() {
        if (this.mDpmInternal == null) {
            this.mDpmInternal = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        }
        return this.mDpmInternal;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readUsageSourceSetting() {
        synchronized (this.mLock) {
            this.mUsageSource = Settings.Global.getInt(getContext().getContentResolver(), "app_time_limit_usage_source", 1);
        }
    }

    private class UserActionsReceiver extends BroadcastReceiver {
        private UserActionsReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            String action = intent.getAction();
            if ("android.intent.action.USER_REMOVED".equals(action)) {
                if (userId >= 0) {
                    UsageStatsService.this.mHandler.obtainMessage(2, userId, 0).sendToTarget();
                }
            } else if ("android.intent.action.USER_STARTED".equals(action) && userId >= 0) {
                UsageStatsService.this.mAppStandby.postCheckIdleStates(userId);
            }
        }
    }

    @Override // com.android.server.usage.UserUsageStatsService.StatsUpdatedListener
    public void onStatsUpdated() {
        this.mHandler.sendEmptyMessageDelayed(1, 1200000);
    }

    @Override // com.android.server.usage.UserUsageStatsService.StatsUpdatedListener
    public void onStatsReloaded() {
        this.mAppStandby.postOneTimeCheckIdleStates();
    }

    @Override // com.android.server.usage.UserUsageStatsService.StatsUpdatedListener
    public void onNewUpdate(int userId) {
        this.mAppStandby.initializeDefaultsForSystemApps(userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldObfuscateInstantAppsForCaller(int callingUid, int userId) {
        return !this.mPackageManagerInternal.canAccessInstantApps(callingUid, userId);
    }

    private void cleanUpRemovedUsersLocked() {
        List<UserInfo> users = this.mUserManager.getUsers(true);
        if (users == null || users.size() == 0) {
            throw new IllegalStateException("There can't be no users");
        }
        ArraySet<String> toDelete = new ArraySet<>();
        String[] fileNames = this.mUsageStatsDir.list();
        if (fileNames != null) {
            toDelete.addAll(Arrays.asList(fileNames));
            int userCount = users.size();
            for (int i = 0; i < userCount; i++) {
                toDelete.remove(Integer.toString(users.get(i).id));
            }
            int deleteCount = toDelete.size();
            for (int i2 = 0; i2 < deleteCount; i2++) {
                deleteRecursively(new File(this.mUsageStatsDir, toDelete.valueAt(i2)));
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserUsageStatsService getUserDataAndInitializeIfNeededLocked(int userId, long currentTimeMillis) {
        UserUsageStatsService service = this.mUserState.get(userId);
        if (service != null) {
            return service;
        }
        UserUsageStatsService service2 = new UserUsageStatsService(getContext(), userId, new File(this.mUsageStatsDir, Integer.toString(userId)), this);
        service2.init(currentTimeMillis);
        this.mUserState.put(userId, service2);
        return service2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long checkAndGetTimeLocked() {
        long actualSystemTime = System.currentTimeMillis();
        long actualRealtime = SystemClock.elapsedRealtime();
        long expectedSystemTime = (actualRealtime - this.mRealTimeSnapshot) + this.mSystemTimeSnapshot;
        long diffSystemTime = actualSystemTime - expectedSystemTime;
        if (Math.abs(diffSystemTime) > TIME_CHANGE_THRESHOLD_MILLIS && ENABLE_TIME_CHANGE_CORRECTION) {
            Slog.i(TAG, "Time changed in UsageStats by " + (diffSystemTime / 1000) + " seconds");
            int userCount = this.mUserState.size();
            for (int i = 0; i < userCount; i++) {
                this.mUserState.valueAt(i).onTimeChanged(expectedSystemTime, actualSystemTime);
            }
            this.mRealTimeSnapshot = actualRealtime;
            this.mSystemTimeSnapshot = actualSystemTime;
        }
        return actualSystemTime;
    }

    private void convertToSystemTimeLocked(UsageEvents.Event event) {
        event.mTimeStamp = Math.max(0L, event.mTimeStamp - this.mRealTimeSnapshot) + this.mSystemTimeSnapshot;
    }

    /* access modifiers changed from: package-private */
    public void shutdown() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(0);
            UsageEvents.Event event = new UsageEvents.Event(26, SystemClock.elapsedRealtime());
            event.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
            reportEventToAllUserId(event);
            flushToDiskLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareForPossibleShutdown() {
        UsageEvents.Event event = new UsageEvents.Event(26, SystemClock.elapsedRealtime());
        event.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
        this.mHandler.obtainMessage(4, event).sendToTarget();
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: package-private */
    public void reportEvent(UsageEvents.Event event, int userId) {
        ArraySet<String> tokens;
        int size;
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            convertToSystemTimeLocked(event);
            if (event.mPackage != null && this.mPackageManagerInternal.isPackageEphemeral(userId, event.mPackage)) {
                event.mFlags |= 1;
            }
            int i = event.mEventType;
            if (i != 1) {
                if (i != 2) {
                    if (i != 23) {
                        if (i == 24) {
                            event.mEventType = 23;
                        }
                    }
                    ActivityData prevData = (ActivityData) this.mVisibleActivities.removeReturnOld(event.mInstanceId);
                    if (prevData != null) {
                        synchronized (this.mUsageReporters) {
                            tokens = (ArraySet) this.mUsageReporters.removeReturnOld(event.mInstanceId);
                        }
                        if (tokens != null) {
                            synchronized (tokens) {
                                int size2 = tokens.size();
                                int i2 = 0;
                                while (i2 < size2) {
                                    try {
                                        this.mAppTimeLimit.noteUsageStop(buildFullToken(event.mPackage, tokens.valueAt(i2)), userId);
                                        size = size2;
                                    } catch (IllegalArgumentException iae) {
                                        StringBuilder sb = new StringBuilder();
                                        size = size2;
                                        sb.append("Failed to stop usage for during reporter death: ");
                                        sb.append(iae);
                                        Slog.w(TAG, sb.toString());
                                    }
                                    i2++;
                                    size2 = size;
                                }
                            }
                        }
                        if (event.mTaskRootPackage == null) {
                            event.mTaskRootPackage = prevData.mTaskRootPackage;
                            event.mTaskRootClass = prevData.mTaskRootClass;
                        }
                        try {
                            if (this.mUsageSource != 2) {
                                this.mAppTimeLimit.noteUsageStop(event.mTaskRootPackage, userId);
                            } else {
                                this.mAppTimeLimit.noteUsageStop(event.mPackage, userId);
                            }
                        } catch (IllegalArgumentException iae2) {
                            Slog.w(TAG, "Failed to note usage stop", iae2);
                        }
                    } else {
                        return;
                    }
                } else if (event.mTaskRootPackage == null) {
                    ActivityData prevData2 = this.mVisibleActivities.get(event.mInstanceId);
                    if (prevData2 == null) {
                        Slog.w(TAG, "Unexpected activity event reported! (" + event.mPackage + SliceClientPermissions.SliceAuthority.DELIMITER + event.mClass + " event : " + event.mEventType + " instanceId : " + event.mInstanceId + ")");
                    } else {
                        event.mTaskRootPackage = prevData2.mTaskRootPackage;
                        event.mTaskRootClass = prevData2.mTaskRootClass;
                    }
                }
            } else if (this.mVisibleActivities.get(event.mInstanceId) == null) {
                this.mVisibleActivities.put(event.mInstanceId, new ActivityData(event.mTaskRootPackage, event.mTaskRootClass));
                try {
                    if (this.mUsageSource != 2) {
                        this.mAppTimeLimit.noteUsageStart(event.mTaskRootPackage, userId);
                    } else {
                        this.mAppTimeLimit.noteUsageStart(event.mPackage, userId);
                    }
                } catch (IllegalArgumentException iae3) {
                    Slog.e(TAG, "Failed to note usage start", iae3);
                }
            }
            getUserDataAndInitializeIfNeededLocked(userId, timeNow).reportEvent(event);
            this.mAppStandby.reportEvent(event, elapsedRealtime, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void reportEventToAllUserId(UsageEvents.Event event) {
        synchronized (this.mLock) {
            int userCount = this.mUserState.size();
            for (int i = 0; i < userCount; i++) {
                reportEvent(new UsageEvents.Event(event), this.mUserState.keyAt(i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flushToDisk() {
        synchronized (this.mLock) {
            reportEventToAllUserId(new UsageEvents.Event(25, SystemClock.elapsedRealtime()));
            flushToDiskLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            Slog.i(TAG, "Removing user " + userId + " and all data.");
            this.mUserState.remove(userId);
            this.mAppStandby.onUserRemoved(userId);
            this.mAppTimeLimit.onUserRemoved(userId);
            cleanUpRemovedUsersLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public List<UsageStats> queryUsageStats(int userId, int bucketType, long beginTime, long endTime, boolean obfuscateInstantApps) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            List<UsageStats> list = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryUsageStats(bucketType, beginTime, endTime);
            if (list == null) {
                return null;
            }
            if (obfuscateInstantApps) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    UsageStats stats = list.get(i);
                    if (this.mPackageManagerInternal.isPackageEphemeral(userId, stats.mPackageName)) {
                        list.set(i, stats.getObfuscatedForInstantApp());
                    }
                }
            }
            return list;
        }
    }

    /* access modifiers changed from: package-private */
    public List<ConfigurationStats> queryConfigurationStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            return getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryConfigurationStats(bucketType, beginTime, endTime);
        }
    }

    /* access modifiers changed from: package-private */
    public List<EventStats> queryEventStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            return getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEventStats(bucketType, beginTime, endTime);
        }
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEvents(int userId, long beginTime, long endTime, boolean shouldObfuscateInstantApps) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            return getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEvents(beginTime, endTime, shouldObfuscateInstantApps);
        }
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEventsForPackage(int userId, long beginTime, long endTime, String packageName, boolean includeTaskRoot) {
        synchronized (this.mLock) {
            try {
                long timeNow = checkAndGetTimeLocked();
                if (!validRange(timeNow, beginTime, endTime)) {
                    return null;
                }
                return getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEventsForPackage(beginTime, endTime, packageName, includeTaskRoot);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private static boolean validRange(long currentTime, long beginTime, long endTime) {
        return beginTime <= currentTime && beginTime < endTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String buildFullToken(String packageName, String token) {
        StringBuilder sb = new StringBuilder(packageName.length() + token.length() + 1);
        sb.append(packageName);
        sb.append(TOKEN_DELIMITER);
        sb.append(token);
        return sb.toString();
    }

    private void flushToDiskLocked() {
        int userCount = this.mUserState.size();
        for (int i = 0; i < userCount; i++) {
            this.mUserState.valueAt(i).persistActiveStats();
            this.mAppStandby.flushToDisk(this.mUserState.keyAt(i));
        }
        this.mAppStandby.flushDurationsToDisk();
        this.mHandler.removeMessages(1);
    }

    /* access modifiers changed from: package-private */
    public void dump(String[] args, PrintWriter pw) {
        synchronized (this.mLock) {
            IndentingPrintWriter idpw = new IndentingPrintWriter(pw, "  ");
            boolean checkin = false;
            boolean compact = false;
            String pkg = null;
            if (args != null) {
                int i = 0;
                while (true) {
                    if (i >= args.length) {
                        break;
                    }
                    String arg = args[i];
                    if ("--checkin".equals(arg)) {
                        checkin = true;
                    } else if ("-c".equals(arg)) {
                        compact = true;
                    } else if ("flush".equals(arg)) {
                        flushToDiskLocked();
                        pw.println("Flushed stats to disk");
                        return;
                    } else if ("is-app-standby-enabled".equals(arg)) {
                        pw.println(this.mAppStandby.mAppIdleEnabled);
                        return;
                    } else if ("apptimelimit".equals(arg)) {
                        if (i + 1 >= args.length) {
                            this.mAppTimeLimit.dump(null, pw);
                        } else {
                            this.mAppTimeLimit.dump((String[]) Arrays.copyOfRange(args, i + 1, args.length), pw);
                        }
                        return;
                    } else if ("file".equals(arg)) {
                        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
                        if (i + 1 >= args.length) {
                            int numUsers = this.mUserState.size();
                            for (int user = 0; user < numUsers; user++) {
                                ipw.println("user=" + this.mUserState.keyAt(user));
                                ipw.increaseIndent();
                                this.mUserState.valueAt(user).dumpFile(ipw, null);
                                ipw.decreaseIndent();
                            }
                        } else {
                            try {
                                int user2 = Integer.valueOf(args[i + 1]).intValue();
                                if (this.mUserState.indexOfKey(user2) < 0) {
                                    ipw.println("the specified user does not exist.");
                                    return;
                                }
                                this.mUserState.get(user2).dumpFile(ipw, (String[]) Arrays.copyOfRange(args, i + 2, args.length));
                            } catch (NumberFormatException e) {
                                ipw.println("invalid user specified.");
                                return;
                            }
                        }
                        return;
                    } else if (!"database-info".equals(arg)) {
                        if (arg != null && !arg.startsWith("-")) {
                            pkg = arg;
                            break;
                        }
                    } else {
                        IndentingPrintWriter ipw2 = new IndentingPrintWriter(pw, "  ");
                        if (i + 1 >= args.length) {
                            int numUsers2 = this.mUserState.size();
                            for (int user3 = 0; user3 < numUsers2; user3++) {
                                ipw2.println("user=" + this.mUserState.keyAt(user3));
                                ipw2.increaseIndent();
                                this.mUserState.valueAt(user3).dumpDatabaseInfo(ipw2);
                                ipw2.decreaseIndent();
                            }
                        } else {
                            try {
                                int user4 = Integer.valueOf(args[i + 1]).intValue();
                                if (this.mUserState.indexOfKey(user4) < 0) {
                                    ipw2.println("the specified user does not exist.");
                                    return;
                                }
                                this.mUserState.get(user4).dumpDatabaseInfo(ipw2);
                            } catch (NumberFormatException e2) {
                                ipw2.println("invalid user specified.");
                                return;
                            }
                        }
                        return;
                    }
                    i++;
                }
            }
            int userCount = this.mUserState.size();
            for (int i2 = 0; i2 < userCount; i2++) {
                int userId = this.mUserState.keyAt(i2);
                idpw.printPair("user", Integer.valueOf(userId));
                idpw.println();
                idpw.increaseIndent();
                if (checkin) {
                    this.mUserState.valueAt(i2).checkin(idpw);
                } else {
                    this.mUserState.valueAt(i2).dump(idpw, pkg, compact);
                    idpw.println();
                }
                this.mAppStandby.dumpUser(idpw, userId, pkg);
                idpw.decreaseIndent();
            }
            if (pkg == null) {
                pw.println();
                this.mAppStandby.dumpState(args, pw);
            }
            idpw.println();
            idpw.printPair("Usage Source", UsageStatsManager.usageSourceToString(this.mUsageSource));
            idpw.println();
            this.mAppTimeLimit.dump(null, pw);
        }
    }

    class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                int newCounter = 1;
                if (i == 1) {
                    UsageStatsService.this.flushToDisk();
                } else if (i == 2) {
                    UsageStatsService.this.onUserRemoved(msg.arg1);
                } else if (i == 3) {
                    int uid = msg.arg1;
                    if (msg.arg2 <= 2) {
                        newCounter = 0;
                    }
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
                } else if (i != 4) {
                    super.handleMessage(msg);
                } else {
                    UsageStatsService.this.reportEventToAllUserId((UsageEvents.Event) msg.obj);
                }
            } else {
                UsageStatsService.this.reportEvent((UsageEvents.Event) msg.obj, msg.arg1);
            }
        }
    }

    private final class BinderService extends IUsageStatsManager.Stub {
        private BinderService() {
        }

        private boolean hasPermission(String callingPackage) {
            int callingUid = Binder.getCallingUid();
            if (callingUid == 1000) {
                return true;
            }
            int mode = UsageStatsService.this.mAppOps.noteOp(43, callingUid, callingPackage);
            if (mode == 3) {
                if (UsageStatsService.this.getContext().checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") == 0) {
                    return true;
                }
                return false;
            } else if (mode == 0) {
                return true;
            } else {
                return false;
            }
        }

        private boolean hasObserverPermission() {
            int callingUid = Binder.getCallingUid();
            DevicePolicyManagerInternal dpmInternal = UsageStatsService.this.getDpmInternal();
            if (callingUid == 1000 || ((dpmInternal != null && dpmInternal.isActiveAdminWithPolicy(callingUid, -1)) || UsageStatsService.this.getContext().checkCallingPermission("android.permission.OBSERVE_APP_USAGE") == 0)) {
                return true;
            }
            return false;
        }

        private boolean hasPermissions(String callingPackage, String... permissions) {
            if (Binder.getCallingUid() == 1000) {
                return true;
            }
            boolean hasPermissions = true;
            Context context = UsageStatsService.this.getContext();
            for (int i = 0; i < permissions.length; i++) {
                hasPermissions = hasPermissions && context.checkCallingPermission(permissions[i]) == 0;
            }
            return hasPermissions;
        }

        private void checkCallerIsSystemOrSameApp(String pkg) {
            if (!isCallingUidSystem()) {
                checkCallerIsSameApp(pkg);
            }
        }

        private void checkCallerIsSameApp(String pkg) {
            int callingUid = Binder.getCallingUid();
            if (UsageStatsService.this.mPackageManagerInternal.getPackageUid(pkg, 0, UserHandle.getUserId(callingUid)) != callingUid) {
                throw new SecurityException("Calling uid " + callingUid + " cannot query eventsfor package " + pkg);
            }
        }

        private boolean isCallingUidSystem() {
            return UserHandle.getAppId(Binder.getCallingUid()) == 1000;
        }

        public ParceledListSlice<UsageStats> queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.USAGESTATES_QUERYUSAGESTATS);
            if (!hasPermission(callingPackage)) {
                return null;
            }
            boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), UserHandle.getCallingUserId());
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                List<UsageStats> results = UsageStatsService.this.queryUsageStats(userId, bucketType, beginTime, endTime, obfuscateInstantApps);
                if (results != null) {
                    return new ParceledListSlice<>(results);
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
                    return new ParceledListSlice<>(results);
                }
                Binder.restoreCallingIdentity(token);
                return null;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public ParceledListSlice<EventStats> queryEventStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            int userId = UserHandle.getCallingUserId();
            long token = Binder.clearCallingIdentity();
            try {
                List<EventStats> results = UsageStatsService.this.queryEventStats(userId, bucketType, beginTime, endTime);
                if (results != null) {
                    return new ParceledListSlice<>(results);
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
                return UsageStatsService.this.queryEvents(userId, beginTime, endTime, obfuscateInstantApps);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEventsForPackage(long beginTime, long endTime, String callingPackage) {
            int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
            checkCallerIsSameApp(callingPackage);
            boolean includeTaskRoot = hasPermission(callingPackage);
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEventsForPackage(callingUserId, beginTime, endTime, callingPackage, includeTaskRoot);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEventsForUser(long beginTime, long endTime, int userId, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            if (userId != UserHandle.getCallingUserId()) {
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "No permission to query usage stats for this user");
            }
            boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), UserHandle.getCallingUserId());
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEvents(userId, beginTime, endTime, obfuscateInstantApps);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEventsForPackageForUser(long beginTime, long endTime, int userId, String pkg, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            if (userId != UserHandle.getCallingUserId()) {
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "No permission to query usage stats for this user");
            }
            checkCallerIsSystemOrSameApp(pkg);
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEventsForPackage(userId, beginTime, endTime, pkg, true);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isAppInactive(String packageName, int userId) {
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "isAppInactive", (String) null);
                boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), userId2);
                long token = Binder.clearCallingIdentity();
                try {
                    return UsageStatsService.this.mAppStandby.isAppIdleFilteredOrParoled(packageName, userId2, SystemClock.elapsedRealtime(), obfuscateInstantApps);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public void setAppInactive(String packageName, boolean idle, int userId) {
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setAppInactive", (String) null);
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app idle state");
                long token = Binder.clearCallingIdentity();
                try {
                    if (UsageStatsService.this.mAppStandby.getAppId(packageName) >= 0) {
                        UsageStatsService.this.mAppStandby.setAppIdleAsync(packageName, idle, userId2);
                        Binder.restoreCallingIdentity(token);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public int getAppStandbyBucket(String packageName, String callingPackage, int userId) {
            int callingUid = Binder.getCallingUid();
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, false, "getAppStandbyBucket", (String) null);
                int packageUid = UsageStatsService.this.mPackageManagerInternal.getPackageUid(packageName, 0, userId2);
                if (packageUid != callingUid && !hasPermission(callingPackage)) {
                    throw new SecurityException("Don't have permission to query app standby bucket");
                } else if (packageUid >= 0) {
                    boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(callingUid, userId2);
                    long token = Binder.clearCallingIdentity();
                    try {
                        return UsageStatsService.this.mAppStandby.getAppStandbyBucket(packageName, userId2, SystemClock.elapsedRealtime(), obfuscateInstantApps);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new IllegalArgumentException("Cannot get standby bucket for non existent package (" + packageName + ")");
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public void setAppStandbyBucket(String packageName, int bucket, int userId) {
            int reason;
            UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app standby state");
            if (bucket < 10 || bucket > 50) {
                throw new IllegalArgumentException("Cannot set the standby bucket to " + bucket);
            }
            int callingUid = Binder.getCallingUid();
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, true, "setAppStandbyBucket", (String) null);
                boolean shellCaller = callingUid == 0 || callingUid == 2000;
                if (UserHandle.isCore(callingUid)) {
                    reason = 1024;
                } else {
                    reason = 1280;
                }
                long token = Binder.clearCallingIdentity();
                try {
                    int packageUid = UsageStatsService.this.mPackageManagerInternal.getPackageUid(packageName, 4980736, userId2);
                    if (packageUid == callingUid) {
                        throw new IllegalArgumentException("Cannot set your own standby bucket");
                    } else if (packageUid >= 0) {
                        UsageStatsService.this.mAppStandby.setAppStandbyBucket(packageName, userId2, bucket, reason, SystemClock.elapsedRealtime(), shellCaller);
                    } else {
                        throw new IllegalArgumentException("Cannot set standby bucket for non existent package (" + packageName + ")");
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public ParceledListSlice<AppStandbyInfo> getAppStandbyBuckets(String callingPackageName, int userId) {
            ParceledListSlice<AppStandbyInfo> parceledListSlice;
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getAppStandbyBucket", (String) null);
                if (hasPermission(callingPackageName)) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        List<AppStandbyInfo> standbyBucketList = UsageStatsService.this.mAppStandby.getAppStandbyBuckets(userId2);
                        if (standbyBucketList == null) {
                            parceledListSlice = ParceledListSlice.emptyList();
                        } else {
                            parceledListSlice = new ParceledListSlice<>(standbyBucketList);
                        }
                        return parceledListSlice;
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new SecurityException("Don't have permission to query app standby bucket");
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public void setAppStandbyBuckets(ParceledListSlice appBuckets, int userId) {
            UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app standby state");
            int callingUid = Binder.getCallingUid();
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, true, "setAppStandbyBucket", (String) null);
                boolean shellCaller = callingUid == 0 || callingUid == 2000;
                int reason = shellCaller ? 1024 : 1280;
                long token = Binder.clearCallingIdentity();
                try {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    for (AppStandbyInfo bucketInfo : appBuckets.getList()) {
                        String packageName = bucketInfo.mPackageName;
                        int bucket = bucketInfo.mStandbyBucket;
                        if (bucket < 10 || bucket > 50) {
                            throw new IllegalArgumentException("Cannot set the standby bucket to " + bucket);
                        } else if (UsageStatsService.this.mPackageManagerInternal.getPackageUid(packageName, (int) DumpState.DUMP_CHANGES, userId2) != callingUid) {
                            UsageStatsService.this.mAppStandby.setAppStandbyBucket(packageName, userId2, bucket, reason, elapsedRealtime, shellCaller);
                        } else {
                            throw new IllegalArgumentException("Cannot set your own standby bucket");
                        }
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
            UsageStatsService.this.mAppStandby.clearCarrierPrivilegedApps();
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(UsageStatsService.this.getContext(), UsageStatsService.TAG, pw)) {
                UsageStatsService.this.dump(args, pw);
            }
        }

        public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) {
            if (packageName == null) {
                Slog.w(UsageStatsService.TAG, "Event report user selecting a null package");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(9, SystemClock.elapsedRealtime());
            event.mPackage = packageName;
            event.mAction = action;
            event.mContentType = contentType;
            event.mContentAnnotations = annotations;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void registerAppUsageObserver(int observerId, String[] packages, long timeLimitMs, PendingIntent callbackIntent, String callingPackage) {
            if (!hasObserverPermission()) {
                throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
            } else if (packages == null || packages.length == 0) {
                throw new IllegalArgumentException("Must specify at least one package");
            } else if (callbackIntent != null) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.registerAppUsageObserver(callingUid, observerId, packages, timeLimitMs, callbackIntent, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new NullPointerException("callbackIntent can't be null");
            }
        }

        public void unregisterAppUsageObserver(int observerId, String callingPackage) {
            if (hasObserverPermission()) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.unregisterAppUsageObserver(callingUid, observerId, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
            }
        }

        public void registerUsageSessionObserver(int sessionObserverId, String[] observed, long timeLimitMs, long sessionThresholdTimeMs, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent, String callingPackage) {
            if (!hasObserverPermission()) {
                throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
            } else if (observed == null || observed.length == 0) {
                throw new IllegalArgumentException("Must specify at least one observed entity");
            } else if (limitReachedCallbackIntent != null) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.registerUsageSessionObserver(callingUid, sessionObserverId, observed, timeLimitMs, sessionThresholdTimeMs, limitReachedCallbackIntent, sessionEndCallbackIntent, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new NullPointerException("limitReachedCallbackIntent can't be null");
            }
        }

        public void unregisterUsageSessionObserver(int sessionObserverId, String callingPackage) {
            if (hasObserverPermission()) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.unregisterUsageSessionObserver(callingUid, sessionObserverId, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
            }
        }

        public void registerAppUsageLimitObserver(int observerId, String[] packages, long timeLimitMs, long timeUsedMs, PendingIntent callbackIntent, String callingPackage) {
            if (!hasPermissions(callingPackage, "android.permission.SUSPEND_APPS", "android.permission.OBSERVE_APP_USAGE")) {
                throw new SecurityException("Caller doesn't have both SUSPEND_APPS and OBSERVE_APP_USAGE permissions");
            } else if (packages == null || packages.length == 0) {
                throw new IllegalArgumentException("Must specify at least one package");
            } else if (callbackIntent != null || timeUsedMs >= timeLimitMs) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.registerAppUsageLimitObserver(callingUid, observerId, packages, timeLimitMs, timeUsedMs, callbackIntent, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new NullPointerException("callbackIntent can't be null");
            }
        }

        public void unregisterAppUsageLimitObserver(int observerId, String callingPackage) {
            if (hasPermissions(callingPackage, "android.permission.SUSPEND_APPS", "android.permission.OBSERVE_APP_USAGE")) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token = Binder.clearCallingIdentity();
                try {
                    UsageStatsService.this.unregisterAppUsageLimitObserver(callingUid, observerId, userId);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Caller doesn't have both SUSPEND_APPS and OBSERVE_APP_USAGE permissions");
            }
        }

        public void reportUsageStart(IBinder activity, String token, String callingPackage) {
            reportPastUsageStart(activity, token, 0, callingPackage);
        }

        public void reportPastUsageStart(IBinder activity, String token, long timeAgoMs, String callingPackage) {
            ArraySet<String> tokens;
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            long binderToken = Binder.clearCallingIdentity();
            try {
                synchronized (UsageStatsService.this.mUsageReporters) {
                    tokens = UsageStatsService.this.mUsageReporters.get(activity.hashCode());
                    if (tokens == null) {
                        tokens = new ArraySet<>();
                        UsageStatsService.this.mUsageReporters.put(activity.hashCode(), tokens);
                    }
                }
                synchronized (tokens) {
                    if (!tokens.add(token)) {
                        throw new IllegalArgumentException(token + " for " + callingPackage + " is already reported as started for this activity");
                    }
                }
                UsageStatsService.this.mAppTimeLimit.noteUsageStart(UsageStatsService.this.buildFullToken(callingPackage, token), userId, timeAgoMs);
            } finally {
                Binder.restoreCallingIdentity(binderToken);
            }
        }

        public void reportUsageStop(IBinder activity, String token, String callingPackage) {
            ArraySet<String> tokens;
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            long binderToken = Binder.clearCallingIdentity();
            try {
                synchronized (UsageStatsService.this.mUsageReporters) {
                    tokens = UsageStatsService.this.mUsageReporters.get(activity.hashCode());
                    if (tokens == null) {
                        throw new IllegalArgumentException("Unknown reporter trying to stop token " + token + " for " + callingPackage);
                    }
                }
                synchronized (tokens) {
                    if (!tokens.remove(token)) {
                        throw new IllegalArgumentException(token + " for " + callingPackage + " is already reported as stopped for this activity");
                    }
                }
                UsageStatsService.this.mAppTimeLimit.noteUsageStop(UsageStatsService.this.buildFullToken(callingPackage, token), userId);
            } finally {
                Binder.restoreCallingIdentity(binderToken);
            }
        }

        public int getUsageSource() {
            int i;
            if (hasObserverPermission()) {
                synchronized (UsageStatsService.this.mLock) {
                    i = UsageStatsService.this.mUsageSource;
                }
                return i;
            }
            throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
        }

        public void forceUsageSourceSettingRead() {
            UsageStatsService.this.readUsageSourceSetting();
        }
    }

    /* access modifiers changed from: package-private */
    public void registerAppUsageObserver(int callingUid, int observerId, String[] packages, long timeLimitMs, PendingIntent callbackIntent, int userId) {
        this.mAppTimeLimit.addAppUsageObserver(callingUid, observerId, packages, timeLimitMs, callbackIntent, userId);
    }

    /* access modifiers changed from: package-private */
    public void unregisterAppUsageObserver(int callingUid, int observerId, int userId) {
        this.mAppTimeLimit.removeAppUsageObserver(callingUid, observerId, userId);
    }

    /* access modifiers changed from: package-private */
    public void registerUsageSessionObserver(int callingUid, int observerId, String[] observed, long timeLimitMs, long sessionThresholdTime, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent, int userId) {
        this.mAppTimeLimit.addUsageSessionObserver(callingUid, observerId, observed, timeLimitMs, sessionThresholdTime, limitReachedCallbackIntent, sessionEndCallbackIntent, userId);
    }

    /* access modifiers changed from: package-private */
    public void unregisterUsageSessionObserver(int callingUid, int sessionObserverId, int userId) {
        this.mAppTimeLimit.removeUsageSessionObserver(callingUid, sessionObserverId, userId);
    }

    /* access modifiers changed from: package-private */
    public void registerAppUsageLimitObserver(int callingUid, int observerId, String[] packages, long timeLimitMs, long timeUsedMs, PendingIntent callbackIntent, int userId) {
        this.mAppTimeLimit.addAppUsageLimitObserver(callingUid, observerId, packages, timeLimitMs, timeUsedMs, callbackIntent, userId);
    }

    /* access modifiers changed from: package-private */
    public void unregisterAppUsageLimitObserver(int callingUid, int observerId, int userId) {
        this.mAppTimeLimit.removeAppUsageLimitObserver(callingUid, observerId, userId);
    }

    private final class LocalService extends UsageStatsManagerInternal {
        private LocalService() {
        }

        public void reportEvent(ComponentName component, int userId, int eventType, int instanceId, ComponentName taskRoot) {
            if (component == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a component name");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(eventType, SystemClock.elapsedRealtime());
            event.mPackage = component.getPackageName();
            event.mClass = component.getClassName();
            event.mInstanceId = instanceId;
            if (taskRoot == null) {
                event.mTaskRootPackage = null;
                event.mTaskRootClass = null;
            } else {
                event.mTaskRootPackage = taskRoot.getPackageName();
                event.mTaskRootClass = taskRoot.getClassName();
            }
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportEvent(String packageName, int userId, int eventType) {
            if (packageName == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name, eventType:" + eventType);
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(eventType, SystemClock.elapsedRealtime());
            event.mPackage = packageName;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportConfigurationChange(Configuration config, int userId) {
            if (config == null) {
                Slog.w(UsageStatsService.TAG, "Configuration event reported with a null config");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(5, SystemClock.elapsedRealtime());
            event.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
            event.mConfiguration = new Configuration(config);
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportInterruptiveNotification(String packageName, String channelId, int userId) {
            if (packageName == null || channelId == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name or a channel ID");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(12, SystemClock.elapsedRealtime());
            event.mPackage = packageName.intern();
            event.mNotificationChannelId = channelId.intern();
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportShortcutUsage(String packageName, String shortcutId, int userId) {
            if (packageName == null || shortcutId == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name or a shortcut ID");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event(8, SystemClock.elapsedRealtime());
            event.mPackage = packageName.intern();
            event.mShortcutId = shortcutId.intern();
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportContentProviderUsage(String name, String packageName, int userId) {
            UsageStatsService.this.mAppStandby.postReportContentProviderUsage(name, packageName, userId);
        }

        public boolean isAppIdle(String packageName, int uidForAppId, int userId) {
            return UsageStatsService.this.mAppStandby.isAppIdleFiltered(packageName, uidForAppId, userId, SystemClock.elapsedRealtime());
        }

        public int getAppStandbyBucket(String packageName, int userId, long nowElapsed) {
            return UsageStatsService.this.mAppStandby.getAppStandbyBucket(packageName, userId, nowElapsed, false);
        }

        public int[] getIdleUidsForUser(int userId) {
            return UsageStatsService.this.mAppStandby.getIdleUidsForUser(userId);
        }

        public boolean isAppIdleParoleOn() {
            return UsageStatsService.this.mAppStandby.isParoledOrCharging();
        }

        public void prepareShutdown() {
            UsageStatsService.this.shutdown();
        }

        public void prepareForPossibleShutdown() {
            UsageStatsService.this.prepareForPossibleShutdown();
        }

        public void addAppIdleStateChangeListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
            UsageStatsService.this.mAppStandby.addListener(listener);
            listener.onParoleStateChanged(isAppIdleParoleOn());
        }

        public void removeAppIdleStateChangeListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
            UsageStatsService.this.mAppStandby.removeListener(listener);
        }

        public byte[] getBackupPayload(int user, String key) {
            synchronized (UsageStatsService.this.mLock) {
                if (user != 0) {
                    return null;
                }
                return UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).getBackupPayload(key);
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

        public void setLastJobRunTime(String packageName, int userId, long elapsedRealtime) {
            UsageStatsService.this.mAppStandby.setLastJobRunTime(packageName, userId, elapsedRealtime);
        }

        public long getTimeSinceLastJobRun(String packageName, int userId) {
            return UsageStatsService.this.mAppStandby.getTimeSinceLastJobRun(packageName, userId);
        }

        public void reportAppJobState(String packageName, int userId, int numDeferredJobs, long timeSinceLastJobRun) {
        }

        public void onActiveAdminAdded(String packageName, int userId) {
            UsageStatsService.this.mAppStandby.addActiveDeviceAdmin(packageName, userId);
        }

        public void setActiveAdminApps(Set<String> packageNames, int userId) {
            UsageStatsService.this.mAppStandby.setActiveAdminApps(packageNames, userId);
        }

        public void onAdminDataAvailable() {
            UsageStatsService.this.mAppStandby.onAdminDataAvailable();
        }

        public void reportSyncScheduled(String packageName, int userId, boolean exempted) {
            UsageStatsService.this.mAppStandby.postReportSyncScheduled(packageName, userId, exempted);
        }

        public void reportExemptedSyncStart(String packageName, int userId) {
            UsageStatsService.this.mAppStandby.postReportExemptedSyncStart(packageName, userId);
        }

        public UsageStatsManagerInternal.AppUsageLimitData getAppUsageLimit(String packageName, UserHandle user) {
            return UsageStatsService.this.mAppTimeLimit.getAppUsageLimit(packageName, user);
        }
    }
}
