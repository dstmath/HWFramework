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
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
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
    /* access modifiers changed from: private */
    public static final File KERNEL_COUNTER_FILE = new File("/proc/uid_procstat/set");
    static final int MSG_FLUSH_TO_DISK = 1;
    static final int MSG_REMOVE_USER = 2;
    static final int MSG_REPORT_EVENT = 0;
    static final int MSG_UID_STATE_CHANGED = 3;
    static final String TAG = "UsageStatsService";
    private static final long TEN_SECONDS = 10000;
    private static final long TIME_CHANGE_THRESHOLD_MILLIS = 2000;
    private static final long TWENTY_MINUTES = 1200000;
    AppOpsManager mAppOps;
    AppStandbyController mAppStandby;
    AppTimeLimitController mAppTimeLimit;
    IDeviceIdleController mDeviceIdleController;
    DevicePolicyManagerInternal mDpmInternal;
    Handler mHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    PackageManager mPackageManager;
    PackageManagerInternal mPackageManagerInternal;
    PackageMonitor mPackageMonitor;
    long mRealTimeSnapshot;
    private UsageStatsManagerInternal.AppIdleStateChangeListener mStandbyChangeListener = new UsageStatsManagerInternal.AppIdleStateChangeListener() {
        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            UsageEvents.Event event = new UsageEvents.Event();
            event.mEventType = 11;
            event.mBucketAndReason = (bucket << 16) | (65535 & reason);
            event.mPackage = packageName;
            event.mTimeStamp = SystemClock.elapsedRealtime();
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void onParoleStateChanged(boolean isParoleOn) {
        }
    };
    long mSystemTimeSnapshot;
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            UsageStatsService.this.mHandler.obtainMessage(3, uid, procState).sendToTarget();
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidGone(int uid, boolean disabled) {
            onUidStateChanged(uid, 19, 0);
        }

        public void onUidActive(int uid) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    /* access modifiers changed from: private */
    public final SparseIntArray mUidToKernelCounter = new SparseIntArray();
    private File mUsageStatsDir;
    UserManager mUserManager;
    private final SparseArray<UserUsageStatsService> mUserState = new SparseArray<>();

    private final class BinderService extends IUsageStatsManager.Stub {
        private BinderService() {
        }

        private boolean hasPermission(String callingPackage) {
            int callingUid = Binder.getCallingUid();
            boolean z = true;
            if (callingUid == 1000) {
                return true;
            }
            int mode = UsageStatsService.this.mAppOps.noteOp(43, callingUid, callingPackage);
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

        private boolean hasObserverPermission(String callingPackage) {
            int callingUid = Binder.getCallingUid();
            DevicePolicyManagerInternal dpmInternal = UsageStatsService.this.getDpmInternal();
            boolean z = true;
            if (callingUid == 1000 || (dpmInternal != null && dpmInternal.isActiveAdminWithPolicy(callingUid, -1))) {
                return true;
            }
            if (UsageStatsService.this.getContext().checkCallingPermission("android.permission.OBSERVE_APP_USAGE") != 0) {
                z = false;
            }
            return z;
        }

        private void checkCallerIsSystemOrSameApp(String pkg) {
            if (!isCallingUidSystem()) {
                checkCallerIsSameApp(pkg);
            }
        }

        private void checkCallerIsSameApp(String pkg) {
            int callingUid = Binder.getCallingUid();
            if (UsageStatsService.this.mPackageManagerInternal.getPackageUid(pkg, 0, UserHandle.getUserId(callingUid)) != callingUid) {
                throw new SecurityException("Calling uid " + pkg + " cannot query eventsfor package " + pkg);
            }
        }

        private boolean isCallingUidSystem() {
            return Binder.getCallingUid() == 1000;
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
            String str = callingPackage;
            checkCallerIsSameApp(str);
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEventsForPackage(callingUserId, beginTime, endTime, str);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEventsForUser(long beginTime, long endTime, int userId, String callingPackage) {
            if (!hasPermission(callingPackage)) {
                return null;
            }
            int i = userId;
            if (i != UserHandle.getCallingUserId()) {
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "No permission to query usage stats for this user");
            }
            boolean obfuscateInstantApps = UsageStatsService.this.shouldObfuscateInstantAppsForCaller(Binder.getCallingUid(), UserHandle.getCallingUserId());
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEvents(i, beginTime, endTime, obfuscateInstantApps);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public UsageEvents queryEventsForPackageForUser(long beginTime, long endTime, int userId, String pkg, String callingPackage) {
            String str = callingPackage;
            if (!hasPermission(str)) {
                return null;
            }
            int i = userId;
            if (i != UserHandle.getCallingUserId()) {
                UsageStatsService.this.getContext().enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "No permission to query usage stats for this user");
            }
            checkCallerIsSystemOrSameApp(pkg);
            long token = Binder.clearCallingIdentity();
            try {
                return UsageStatsService.this.queryEventsForPackage(i, beginTime, endTime, str);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isAppInactive(String packageName, int userId) {
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "isAppInactive", null);
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
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, true, "setAppInactive", null);
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
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, false, "getAppStandbyBucket", null);
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
            int i;
            long token;
            String str = packageName;
            int i2 = bucket;
            UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app standby state");
            if (i2 < 10 || i2 > 50) {
                throw new IllegalArgumentException("Cannot set the standby bucket to " + i2);
            }
            int callingUid = Binder.getCallingUid();
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, true, "setAppStandbyBucket", null);
                boolean shellCaller = callingUid == 0 || callingUid == 2000;
                if (UserHandle.isCore(callingUid)) {
                    i = 1024;
                } else {
                    i = 1280;
                }
                int reason = i;
                long token2 = Binder.clearCallingIdentity();
                try {
                    int packageUid = UsageStatsService.this.mPackageManagerInternal.getPackageUid(str, DumpState.DUMP_CHANGES, userId2);
                    if (packageUid == callingUid) {
                        int i3 = userId2;
                        long j = token2;
                        throw new IllegalArgumentException("Cannot set your own standby bucket");
                    } else if (packageUid >= 0) {
                        int i4 = callingUid;
                        int i5 = userId2;
                        token = token2;
                        try {
                            UsageStatsService.this.mAppStandby.setAppStandbyBucket(str, userId2, i2, reason, SystemClock.elapsedRealtime(), shellCaller);
                            Binder.restoreCallingIdentity(token);
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } else {
                        int i6 = userId2;
                        token = token2;
                        throw new IllegalArgumentException("Cannot set standby bucket for non existent package (" + str + ")");
                    }
                } catch (Throwable th2) {
                    th = th2;
                    int i7 = callingUid;
                    int i8 = userId2;
                    token = token2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (RemoteException re) {
                int i9 = callingUid;
                throw re.rethrowFromSystemServer();
            }
        }

        public ParceledListSlice<AppStandbyInfo> getAppStandbyBuckets(String callingPackageName, int userId) {
            ParceledListSlice<AppStandbyInfo> parceledListSlice;
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getAppStandbyBucket", null);
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
            int i;
            UsageStatsService.this.getContext().enforceCallingPermission("android.permission.CHANGE_APP_IDLE_STATE", "No permission to change app standby state");
            int callingUid = Binder.getCallingUid();
            try {
                int userId2 = ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, true, "setAppStandbyBucket", null);
                boolean shellCaller = callingUid == 0 || callingUid == 2000;
                if (shellCaller) {
                    i = 1024;
                } else {
                    i = 1280;
                }
                int reason = i;
                long token = Binder.clearCallingIdentity();
                try {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    for (AppStandbyInfo bucketInfo : appBuckets.getList()) {
                        String packageName = bucketInfo.mPackageName;
                        int bucket = bucketInfo.mStandbyBucket;
                        if (bucket < 10 || bucket > 50) {
                            String str = packageName;
                            AppStandbyInfo appStandbyInfo = bucketInfo;
                            throw new IllegalArgumentException("Cannot set the standby bucket to " + bucket);
                        } else if (UsageStatsService.this.mPackageManagerInternal.getPackageUid(packageName, DumpState.DUMP_CHANGES, userId2) != callingUid) {
                            String str2 = packageName;
                            AppStandbyInfo appStandbyInfo2 = bucketInfo;
                            UsageStatsService.this.mAppStandby.setAppStandbyBucket(packageName, userId2, bucket, reason, elapsedRealtime, shellCaller);
                        } else {
                            int i2 = bucket;
                            String str3 = packageName;
                            AppStandbyInfo appStandbyInfo3 = bucketInfo;
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
            UsageEvents.Event event = new UsageEvents.Event();
            event.mPackage = packageName;
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 9;
            event.mAction = action;
            event.mContentType = contentType;
            event.mContentAnnotations = annotations;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void registerAppUsageObserver(int observerId, String[] packages, long timeLimitMs, PendingIntent callbackIntent, String callingPackage) {
            long token;
            String[] strArr = packages;
            if (!hasObserverPermission(callingPackage)) {
                throw new SecurityException("Caller doesn't have OBSERVE_APP_USAGE permission");
            } else if (strArr == null || strArr.length == 0) {
                throw new IllegalArgumentException("Must specify at least one package");
            } else if (callbackIntent != null) {
                int callingUid = Binder.getCallingUid();
                int userId = UserHandle.getUserId(callingUid);
                long token2 = Binder.clearCallingIdentity();
                try {
                    String[] strArr2 = strArr;
                    token = token2;
                    try {
                        UsageStatsService.this.registerAppUsageObserver(callingUid, observerId, strArr2, timeLimitMs, callbackIntent, userId);
                        Binder.restoreCallingIdentity(token);
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    token = token2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } else {
                throw new NullPointerException("callbackIntent can't be null");
            }
        }

        public void unregisterAppUsageObserver(int observerId, String callingPackage) {
            if (hasObserverPermission(callingPackage)) {
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
    }

    class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            UserUsageStatsService service;
            int i = 1;
            switch (msg.what) {
                case 0:
                    UsageEvents.Event event = (UsageEvents.Event) msg.obj;
                    int userId = msg.arg1;
                    synchronized (UsageStatsService.this.mLock) {
                        service = UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(userId, UsageStatsService.this.checkAndGetTimeLocked());
                    }
                    if (event.mEventType == 1 && service.mLastEvent == 1) {
                        String foregroundPackage = event.getPackageName();
                        if (foregroundPackage != null && !foregroundPackage.equals(service.mLastForegroundedPackage)) {
                            Slog.d(UsageStatsService.TAG, "LastForegroundedPackage " + service.mLastForegroundedPackage + " is not moved to background, so let's report a backgroundEvent to compensate!");
                            UsageEvents.Event backgroundEvent = new UsageEvents.Event();
                            backgroundEvent.mPackage = service.mLastForegroundedPackage;
                            backgroundEvent.mTimeStamp = SystemClock.elapsedRealtime();
                            backgroundEvent.mEventType = 2;
                            UsageStatsService.this.reportEvent(backgroundEvent, userId);
                        }
                    }
                    UsageStatsService.this.reportEvent(event, userId);
                    return;
                case 1:
                    UsageStatsService.this.flushToDisk();
                    return;
                case 2:
                    UsageStatsService.this.onUserRemoved(msg.arg1);
                    return;
                case 3:
                    int uid = msg.arg1;
                    if (msg.arg2 <= 2) {
                        i = 0;
                    }
                    int newCounter = i;
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
                default:
                    super.handleMessage(msg);
                    return;
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
            UsageEvents.Event event = new UsageEvents.Event();
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
            UsageEvents.Event event = new UsageEvents.Event();
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
            UsageEvents.Event event = new UsageEvents.Event();
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
            UsageEvents.Event event = new UsageEvents.Event();
            event.mPackage = PackageManagerService.PLATFORM_PACKAGE_NAME;
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 5;
            event.mConfiguration = new Configuration(config);
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportInterruptiveNotification(String packageName, String channelId, int userId) {
            if (packageName == null || channelId == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name or a channel ID");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event();
            event.mPackage = packageName.intern();
            event.mNotificationChannelId = channelId.intern();
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 12;
            UsageStatsService.this.mHandler.obtainMessage(0, userId, 0, event).sendToTarget();
        }

        public void reportShortcutUsage(String packageName, String shortcutId, int userId) {
            if (packageName == null || shortcutId == null) {
                Slog.w(UsageStatsService.TAG, "Event reported without a package name or a shortcut ID");
                return;
            }
            UsageEvents.Event event = new UsageEvents.Event();
            event.mPackage = packageName.intern();
            event.mShortcutId = shortcutId.intern();
            event.mTimeStamp = SystemClock.elapsedRealtime();
            event.mEventType = 8;
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
                try {
                    byte[] backupPayload = UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).getBackupPayload(key);
                    return backupPayload;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        public void applyRestoredPayload(int user, String key, byte[] payload) {
            synchronized (UsageStatsService.this.mLock) {
                if (user == 0) {
                    try {
                        UsageStatsService.this.getUserDataAndInitializeIfNeededLocked(user, UsageStatsService.this.checkAndGetTimeLocked()).applyRestoredPayload(key, payload);
                    } catch (Throwable th) {
                        throw th;
                    }
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

        public void reportExemptedSyncScheduled(String packageName, int userId) {
            UsageStatsService.this.mAppStandby.postReportExemptedSyncScheduled(packageName, userId);
        }

        public void reportExemptedSyncStart(String packageName, int userId) {
            UsageStatsService.this.mAppStandby.postReportExemptedSyncStart(packageName, userId);
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
                    UsageStatsService.this.mHandler.obtainMessage(2, userId, 0).sendToTarget();
                }
            } else if ("android.intent.action.USER_STARTED".equals(action) && userId >= 0) {
                UsageStatsService.this.mAppStandby.postCheckIdleStates(userId);
            }
        }
    }

    public UsageStatsService(Context context) {
        super(context);
    }

    /* JADX WARNING: type inference failed for: r4v2, types: [com.android.server.usage.UsageStatsService$BinderService, android.os.IBinder] */
    public void onStart() {
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mUserManager = (UserManager) getContext().getSystemService("user");
        this.mPackageManager = getContext().getPackageManager();
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mHandler = new H(BackgroundThread.get().getLooper());
        this.mAppStandby = new AppStandbyController(getContext(), BackgroundThread.get().getLooper());
        this.mAppTimeLimit = new AppTimeLimitController(new AppTimeLimitController.OnLimitReachedListener() {
            public final void onLimitReached(int i, int i2, long j, long j2, PendingIntent pendingIntent) {
                UsageStatsService.lambda$onStart$0(UsageStatsService.this, i, i2, j, j2, pendingIntent);
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

    public static /* synthetic */ void lambda$onStart$0(UsageStatsService usageStatsService, int observerId, int userId, long timeLimit, long timeElapsed, PendingIntent callbackIntent) {
        Intent intent = new Intent();
        intent.putExtra("android.app.usage.extra.OBSERVER_ID", observerId);
        intent.putExtra("android.app.usage.extra.TIME_LIMIT", timeLimit);
        intent.putExtra("android.app.usage.extra.TIME_USED", timeElapsed);
        try {
            callbackIntent.send(usageStatsService.getContext(), 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Slog.w(TAG, "Couldn't deliver callback: " + callbackIntent);
        }
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mAppStandby.onBootPhase(phase);
            getDpmInternal();
            this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
            if (KERNEL_COUNTER_FILE.exists()) {
                try {
                    ActivityManager.getService().registerUidObserver(this.mUidObserver, 3, -1, null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Slog.w(TAG, "Missing procfs interface: " + KERNEL_COUNTER_FILE);
            }
        }
    }

    /* access modifiers changed from: private */
    public DevicePolicyManagerInternal getDpmInternal() {
        if (this.mDpmInternal == null) {
            this.mDpmInternal = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        }
        return this.mDpmInternal;
    }

    public void onStatsUpdated() {
        this.mHandler.sendEmptyMessageDelayed(1, 1200000);
    }

    public void onStatsReloaded() {
        this.mAppStandby.postOneTimeCheckIdleStates();
    }

    public void onNewUpdate(int userId) {
        this.mAppStandby.initializeDefaultsForSystemApps(userId);
    }

    /* access modifiers changed from: private */
    public boolean shouldObfuscateInstantAppsForCaller(int callingUid, int userId) {
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
            int i2 = toDelete.size();
            for (int i3 = 0; i3 < i2; i3++) {
                deleteRecursively(new File(this.mUsageStatsDir, toDelete.valueAt(i3)));
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
    public UserUsageStatsService getUserDataAndInitializeIfNeededLocked(int userId, long currentTimeMillis) {
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
    public long checkAndGetTimeLocked() {
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
        event.mTimeStamp = Math.max(0, event.mTimeStamp - this.mRealTimeSnapshot) + this.mSystemTimeSnapshot;
    }

    /* access modifiers changed from: package-private */
    public void shutdown() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(0);
            flushToDiskLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void reportEvent(UsageEvents.Event event, int userId) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            convertToSystemTimeLocked(event);
            if (event.getPackageName() != null && this.mPackageManagerInternal.isPackageEphemeral(userId, event.getPackageName())) {
                event.mFlags |= 1;
            }
            getUserDataAndInitializeIfNeededLocked(userId, timeNow).reportEvent(event);
            this.mAppStandby.reportEvent(event, elapsedRealtime, userId);
            switch (event.mEventType) {
                case 1:
                    this.mAppTimeLimit.moveToForeground(event.getPackageName(), event.getClassName(), userId);
                    break;
                case 2:
                    this.mAppTimeLimit.moveToBackground(event.getPackageName(), event.getClassName(), userId);
                    break;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flushToDisk() {
        synchronized (this.mLock) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        return r0;
     */
    public List<UsageStats> queryUsageStats(int userId, int bucketType, long beginTime, long endTime, boolean obfuscateInstantApps) {
        int i = userId;
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            List<UsageStats> list = getUserDataAndInitializeIfNeededLocked(i, timeNow).queryUsageStats(bucketType, beginTime, endTime);
            if (list == null) {
                return null;
            }
            if (obfuscateInstantApps) {
                for (int i2 = list.size() - 1; i2 >= 0; i2--) {
                    UsageStats stats = list.get(i2);
                    if (this.mPackageManagerInternal.isPackageEphemeral(i, stats.mPackageName)) {
                        list.set(i2, stats.getObfuscatedForInstantApp());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<ConfigurationStats> queryConfigurationStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            List<ConfigurationStats> queryConfigurationStats = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryConfigurationStats(bucketType, beginTime, endTime);
            return queryConfigurationStats;
        }
    }

    /* access modifiers changed from: package-private */
    public List<EventStats> queryEventStats(int userId, int bucketType, long beginTime, long endTime) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            List<EventStats> queryEventStats = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEventStats(bucketType, beginTime, endTime);
            return queryEventStats;
        }
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEvents(int userId, long beginTime, long endTime, boolean shouldObfuscateInstantApps) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            UsageEvents queryEvents = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEvents(beginTime, endTime, shouldObfuscateInstantApps);
            return queryEvents;
        }
    }

    /* access modifiers changed from: package-private */
    public UsageEvents queryEventsForPackage(int userId, long beginTime, long endTime, String packageName) {
        synchronized (this.mLock) {
            long timeNow = checkAndGetTimeLocked();
            if (!validRange(timeNow, beginTime, endTime)) {
                return null;
            }
            UsageEvents queryEventsForPackage = getUserDataAndInitializeIfNeededLocked(userId, timeNow).queryEventsForPackage(beginTime, endTime, packageName);
            return queryEventsForPackage;
        }
    }

    private static boolean validRange(long currentTime, long beginTime, long endTime) {
        return beginTime <= currentTime && beginTime < endTime;
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
        boolean compact;
        boolean checkin;
        synchronized (this.mLock) {
            IndentingPrintWriter idpw = new IndentingPrintWriter(pw, "  ");
            String pkg = null;
            if (args != null) {
                compact = false;
                checkin = false;
                int i = 0;
                while (true) {
                    if (i >= args.length) {
                        break;
                    }
                    String arg = args[i];
                    if (!"--checkin".equals(arg)) {
                        if (!"-c".equals(arg)) {
                            if (!"flush".equals(arg)) {
                                if (!"is-app-standby-enabled".equals(arg)) {
                                    if (arg != null && !arg.startsWith("-")) {
                                        pkg = arg;
                                        break;
                                    }
                                } else {
                                    pw.println(this.mAppStandby.mAppIdleEnabled);
                                    return;
                                }
                            } else {
                                flushToDiskLocked();
                                pw.println("Flushed stats to disk");
                                return;
                            }
                        } else {
                            compact = true;
                        }
                    } else {
                        checkin = true;
                    }
                    i++;
                }
            } else {
                compact = false;
                checkin = false;
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
            this.mAppTimeLimit.dump(pw);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerAppUsageObserver(int callingUid, int observerId, String[] packages, long timeLimitMs, PendingIntent callbackIntent, int userId) {
        this.mAppTimeLimit.addObserver(callingUid, observerId, packages, timeLimitMs, callbackIntent, userId);
    }

    /* access modifiers changed from: package-private */
    public void unregisterAppUsageObserver(int callingUid, int observerId, int userId) {
        this.mAppTimeLimit.removeObserver(callingUid, observerId, userId);
    }
}
