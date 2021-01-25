package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManager;
import android.accounts.AccountManagerInternal;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ISyncAdapter;
import android.content.ISyncAdapterUnsyncableAccountCallback;
import android.content.ISyncContext;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PeriodicSync;
import android.content.ServiceConnection;
import android.content.SyncActivityTooManyDeletes;
import android.content.SyncAdapterType;
import android.content.SyncAdaptersCache;
import android.content.SyncInfo;
import android.content.SyncResult;
import android.content.SyncStatusInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ProviderInfo;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.format.Time;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.function.QuadConsumer;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.accounts.AccountManagerService;
import com.android.server.backup.AccountSyncSettingsBackupHelper;
import com.android.server.content.SyncManager;
import com.android.server.content.SyncStorageEngine;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class SyncManager {
    private static final boolean DEBUG_ACCOUNT_ACCESS = false;
    private static final int DELAY_RETRY_SYNC_IN_PROGRESS_IN_SECONDS = 10;
    private static final boolean ENABLE_SUSPICIOUS_CHECK = Build.IS_DEBUGGABLE;
    private static final String HANDLE_SYNC_ALARM_WAKE_LOCK = "SyncManagerHandleSyncAlarm";
    private static final AccountAndUser[] INITIAL_ACCOUNTS_ARRAY = new AccountAndUser[0];
    private static final long LOCAL_SYNC_DELAY = SystemProperties.getLong("sync.local_sync_delay", 30000);
    private static final int MAX_SYNC_JOB_ID = 110000;
    private static final int MIN_SYNC_JOB_ID = 100000;
    private static final int SYNC_ADAPTER_CONNECTION_FLAGS = 21;
    private static final long SYNC_DELAY_ON_CONFLICT = 10000;
    private static final long SYNC_DELAY_ON_LOW_STORAGE = 3600000;
    private static final String SYNC_LOOP_WAKE_LOCK = "SyncLoopWakeLock";
    private static final int SYNC_MONITOR_PROGRESS_THRESHOLD_BYTES = 10;
    private static final long SYNC_MONITOR_WINDOW_LENGTH_MILLIS = 60000;
    private static final int SYNC_OP_STATE_INVALID = 1;
    private static final int SYNC_OP_STATE_INVALID_NO_ACCOUNT_ACCESS = 2;
    private static final int SYNC_OP_STATE_VALID = 0;
    private static final String SYNC_WAKE_LOCK_PREFIX = "*sync*/";
    static final String TAG = "SyncManager";
    @GuardedBy({"SyncManager.class"})
    private static SyncManager sInstance;
    private static final Comparator<SyncOperation> sOpDumpComparator = $$Lambda$SyncManager$bVs0A6OYdmGkOiq_lbp5MiBwelw.INSTANCE;
    private static final Comparator<SyncOperation> sOpRuntimeComparator = $$Lambda$SyncManager$68MEyNkTh36YmYoFlURJoRa_cY.INSTANCE;
    private final AccountManager mAccountManager;
    private final AccountManagerInternal mAccountManagerInternal;
    private final BroadcastReceiver mAccountsUpdatedReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            SyncManager.this.updateRunningAccounts(new SyncStorageEngine.EndPoint(null, null, getSendingUserId()));
        }
    };
    protected final ArrayList<ActiveSyncContext> mActiveSyncContexts = Lists.newArrayList();
    private final IBatteryStats mBatteryStats;
    private ConnectivityManager mConnManagerDoNotUseDirectly;
    private BroadcastReceiver mConnectivityIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean wasConnected = SyncManager.this.mDataConnectionIsConnected;
            SyncManager syncManager = SyncManager.this;
            syncManager.mDataConnectionIsConnected = syncManager.readDataConnectionState();
            if (SyncManager.this.mDataConnectionIsConnected && !wasConnected) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Reconnection detected: clearing all backoffs");
                }
                SyncManager.this.clearAllBackoffs("network reconnect");
            }
        }
    };
    private final SyncManagerConstants mConstants;
    private Context mContext;
    private volatile boolean mDataConnectionIsConnected = false;
    private volatile boolean mDeviceIsIdle = false;
    private JobScheduler mJobScheduler;
    private JobSchedulerInternal mJobSchedulerInternal;
    private final SyncLogger mLogger;
    private final NotificationManager mNotificationMgr;
    private final BroadcastReceiver mOtherIntentsReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction())) {
                SyncManager.this.mSyncStorageEngine.setClockValid();
            }
        }
    };
    private final PackageManagerInternal mPackageManagerInternal;
    private final PowerManager mPowerManager;
    private volatile boolean mProvisioned;
    private final Random mRand;
    private volatile boolean mReportedSyncActive = false;
    private volatile AccountAndUser[] mRunningAccounts = INITIAL_ACCOUNTS_ARRAY;
    private BroadcastReceiver mShutdownIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.w("SyncManager", "Writing sync state before shutdown...");
            SyncManager.this.getSyncStorageEngine().writeAllState();
            SyncManager.this.mLogger.log(SyncManager.this.getJobStats());
            SyncManager.this.mLogger.log("Shutting down.");
        }
    };
    private final BroadcastReceiver mStorageIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DEVICE_STORAGE_LOW".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is low.");
                }
                SyncManager.this.mStorageIsLow = true;
                SyncManager.this.cancelActiveSync(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, null, "storage low");
            } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is ok.");
                }
                SyncManager.this.mStorageIsLow = false;
                SyncManager.this.rescheduleSyncs(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, "storage ok");
            }
        }
    };
    private volatile boolean mStorageIsLow = false;
    protected final SyncAdaptersCache mSyncAdapters;
    private final SyncHandler mSyncHandler;
    private volatile PowerManager.WakeLock mSyncManagerWakeLock;
    private SyncStorageEngine mSyncStorageEngine;
    private final HandlerThread mThread;
    @GuardedBy({"mUnlockedUsers"})
    private final SparseBooleanArray mUnlockedUsers = new SparseBooleanArray();
    private BroadcastReceiver mUserIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.content.SyncManager.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (userId != -10000) {
                if ("android.intent.action.USER_REMOVED".equals(action)) {
                    SyncManager.this.onUserRemoved(userId);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    SyncManager.this.onUserUnlocked(userId);
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    SyncManager.this.onUserStopped(userId);
                }
            }
        }
    };
    private final UserManager mUserManager;

    /* access modifiers changed from: package-private */
    public interface OnReadyCallback {
        void onReady();
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [boolean, byte] */
    /* JADX WARNING: Unknown variable types count: 1 */
    static /* synthetic */ boolean access$1576(SyncManager x0, int x1) {
        ?? r0 = (byte) ((x0.mProvisioned ? 1 : 0) | x1);
        x0.mProvisioned = r0;
        return r0;
    }

    private boolean isJobIdInUseLockedH(int jobId, List<JobInfo> pendingJobs) {
        for (JobInfo job : pendingJobs) {
            if (job.getId() == jobId) {
                return true;
            }
        }
        Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            if (it.next().mSyncOperation.jobId == jobId) {
                return true;
            }
        }
        return false;
    }

    private int getUnusedJobIdH() {
        int newJobId;
        do {
            newJobId = this.mRand.nextInt(10000) + MIN_SYNC_JOB_ID;
        } while (isJobIdInUseLockedH(newJobId, this.mJobSchedulerInternal.getSystemScheduledPendingJobs()));
        return newJobId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<SyncOperation> getAllPendingSyncs() {
        verifyJobScheduler();
        List<JobInfo> pendingJobs = this.mJobSchedulerInternal.getSystemScheduledPendingJobs();
        List<SyncOperation> pendingSyncs = new ArrayList<>(pendingJobs.size());
        for (JobInfo job : pendingJobs) {
            SyncOperation op = SyncOperation.maybeCreateFromJobExtras(job.getExtras());
            if (op != null) {
                pendingSyncs.add(op);
            }
        }
        return pendingSyncs;
    }

    private List<UserInfo> getAllUsers() {
        return this.mUserManager.getUsers();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean containsAccountAndUser(AccountAndUser[] accounts, Account account, int userId) {
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].userId == userId && accounts[i].account.equals(account)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRunningAccounts(SyncStorageEngine.EndPoint target) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_ACCOUNTS_UPDATED");
        }
        Message m = this.mSyncHandler.obtainMessage(9);
        m.obj = target;
        m.sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeStaleAccounts() {
        for (UserInfo user : this.mUserManager.getUsers(true)) {
            if (!user.partial) {
                this.mSyncStorageEngine.removeStaleAccounts(AccountManagerService.getSingleton().getAccounts(user.id, this.mContext.getOpPackageName()), user.id);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAllBackoffs(String why) {
        this.mSyncStorageEngine.clearAllBackoffsLocked();
        rescheduleSyncs(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, why);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean readDataConnectionState() {
        NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getJobStats() {
        String str;
        JobSchedulerInternal js = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
        StringBuilder sb = new StringBuilder();
        sb.append("JobStats: ");
        if (js == null) {
            str = "(JobSchedulerInternal==null)";
        } else {
            str = js.getPersistStats().toString();
        }
        sb.append(str);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ConnectivityManager getConnectivityManager() {
        ConnectivityManager connectivityManager;
        synchronized (this) {
            if (this.mConnManagerDoNotUseDirectly == null) {
                this.mConnManagerDoNotUseDirectly = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            }
            connectivityManager = this.mConnManagerDoNotUseDirectly;
        }
        return connectivityManager;
    }

    private void cleanupJobs() {
        this.mSyncHandler.postAtFrontOfQueue(new Runnable() {
            /* class com.android.server.content.SyncManager.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                List<SyncOperation> ops = SyncManager.this.getAllPendingSyncs();
                Set<String> cleanedKeys = new HashSet<>();
                for (SyncOperation opx : ops) {
                    if (!cleanedKeys.contains(opx.key)) {
                        cleanedKeys.add(opx.key);
                        for (SyncOperation opy : ops) {
                            if (opx != opy && opx.key.equals(opy.key)) {
                                SyncManager.this.mLogger.log("Removing duplicate sync: ", opy);
                                SyncManager syncManager = SyncManager.this;
                                syncManager.cancelJob(opy, "cleanupJobs() x=" + opx + " y=" + opy);
                            }
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void verifyJobScheduler() {
        Throwable th;
        if (this.mJobScheduler == null) {
            long token = Binder.clearCallingIdentity();
            try {
                if (Log.isLoggable("SyncManager", 2)) {
                    try {
                        Log.d("SyncManager", "initializing JobScheduler object.");
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                this.mJobScheduler = (JobScheduler) this.mContext.getSystemService("jobscheduler");
                this.mJobSchedulerInternal = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
                List<JobInfo> pendingJobs = this.mJobScheduler.getAllPendingJobs();
                int numPersistedPeriodicSyncs = 0;
                int numPersistedOneshotSyncs = 0;
                for (JobInfo job : pendingJobs) {
                    SyncOperation op = SyncOperation.maybeCreateFromJobExtras(job.getExtras());
                    if (op != null) {
                        if (op.isPeriodic) {
                            numPersistedPeriodicSyncs++;
                        } else {
                            numPersistedOneshotSyncs++;
                            this.mSyncStorageEngine.markPending(op.target, true);
                        }
                    }
                }
                String summary = "Loaded persisted syncs: " + numPersistedPeriodicSyncs + " periodic syncs, " + numPersistedOneshotSyncs + " oneshot syncs, " + pendingJobs.size() + " total system server jobs, " + getJobStats();
                Slog.i("SyncManager", summary);
                this.mLogger.log(summary);
                cleanupJobs();
                if (ENABLE_SUSPICIOUS_CHECK && numPersistedPeriodicSyncs == 0 && likelyHasPeriodicSyncs()) {
                    Slog.wtf("SyncManager", "Device booted with no persisted periodic syncs: " + summary);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }
    }

    private boolean likelyHasPeriodicSyncs() {
        try {
            return this.mSyncStorageEngine.getAuthorityCount() >= 6;
        } catch (Throwable th) {
            return false;
        }
    }

    private JobScheduler getJobScheduler() {
        verifyJobScheduler();
        return this.mJobScheduler;
    }

    public SyncManager(Context context, boolean factoryTest) {
        synchronized (SyncManager.class) {
            if (sInstance == null) {
                sInstance = this;
            } else {
                Slog.wtf("SyncManager", "SyncManager instantiated multiple times");
            }
        }
        this.mContext = context;
        this.mLogger = SyncLogger.getInstance();
        SyncStorageEngine.init(context, BackgroundThread.get().getLooper());
        this.mSyncStorageEngine = SyncStorageEngine.getSingleton();
        this.mSyncStorageEngine.setOnSyncRequestListener(new SyncStorageEngine.OnSyncRequestListener() {
            /* class com.android.server.content.SyncManager.AnonymousClass8 */

            @Override // com.android.server.content.SyncStorageEngine.OnSyncRequestListener
            public void onSyncRequest(SyncStorageEngine.EndPoint info, int reason, Bundle extras, int syncExemptionFlag, int callingUid, int callingPid) {
                SyncManager.this.scheduleSync(info.account, info.userId, reason, info.provider, extras, -2, syncExemptionFlag, callingUid, callingPid, null);
            }
        });
        this.mSyncStorageEngine.setPeriodicSyncAddedListener(new SyncStorageEngine.PeriodicSyncAddedListener() {
            /* class com.android.server.content.SyncManager.AnonymousClass9 */

            @Override // com.android.server.content.SyncStorageEngine.PeriodicSyncAddedListener
            public void onPeriodicSyncAdded(SyncStorageEngine.EndPoint target, Bundle extras, long pollFrequency, long flex) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        });
        this.mSyncStorageEngine.setOnAuthorityRemovedListener(new SyncStorageEngine.OnAuthorityRemovedListener() {
            /* class com.android.server.content.SyncManager.AnonymousClass10 */

            @Override // com.android.server.content.SyncStorageEngine.OnAuthorityRemovedListener
            public void onAuthorityRemoved(SyncStorageEngine.EndPoint removedAuthority) {
                SyncManager.this.removeSyncsForAuthority(removedAuthority, "onAuthorityRemoved");
            }
        });
        this.mSyncAdapters = new SyncAdaptersCache(this.mContext);
        this.mThread = new HandlerThread("SyncManager", 10);
        this.mThread.start();
        this.mSyncHandler = new SyncHandler(this.mThread.getLooper());
        this.mSyncAdapters.setListener(new RegisteredServicesCacheListener<SyncAdapterType>() {
            /* class com.android.server.content.SyncManager.AnonymousClass11 */

            public void onServiceChanged(SyncAdapterType type, int userId, boolean removed) {
                if (!removed) {
                    SyncManager.this.scheduleSync(null, -1, -3, type.authority, null, -2, 0, Process.myUid(), -1, null);
                }
            }
        }, this.mSyncHandler);
        this.mRand = new Random(System.currentTimeMillis());
        this.mConstants = new SyncManagerConstants(context);
        context.registerReceiver(this.mConnectivityIntentReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        IntentFilter intentFilter = new IntentFilter("android.intent.action.DEVICE_STORAGE_LOW");
        intentFilter.addAction("android.intent.action.DEVICE_STORAGE_OK");
        context.registerReceiver(this.mStorageIntentReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
        intentFilter2.setPriority(100);
        context.registerReceiver(this.mShutdownIntentReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.intent.action.USER_REMOVED");
        intentFilter3.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter3.addAction("android.intent.action.USER_STOPPED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter3, null, null);
        context.registerReceiver(this.mOtherIntentsReceiver, new IntentFilter("android.intent.action.TIME_SET"));
        if (!factoryTest) {
            this.mNotificationMgr = (NotificationManager) context.getSystemService("notification");
        } else {
            this.mNotificationMgr = null;
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mAccountManager = (AccountManager) this.mContext.getSystemService("account");
        this.mAccountManagerInternal = (AccountManagerInternal) LocalServices.getService(AccountManagerInternal.class);
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mAccountManagerInternal.addOnAppPermissionChangeListener(new AccountManagerInternal.OnAppPermissionChangeListener() {
            /* class com.android.server.content.$$Lambda$SyncManager$HhiSFjEoPA_Hnv3xYZGfwkalc68 */

            public final void onAppPermissionChanged(Account account, int i) {
                SyncManager.this.lambda$new$0$SyncManager(account, i);
            }
        });
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mSyncManagerWakeLock = this.mPowerManager.newWakeLock(1, SYNC_LOOP_WAKE_LOCK);
        this.mSyncManagerWakeLock.setReferenceCounted(false);
        this.mProvisioned = isDeviceProvisioned();
        if (!this.mProvisioned) {
            final ContentResolver resolver = context.getContentResolver();
            ContentObserver provisionedObserver = new ContentObserver(null) {
                /* class com.android.server.content.SyncManager.AnonymousClass12 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    SyncManager syncManager = SyncManager.this;
                    SyncManager.access$1576(syncManager, syncManager.isDeviceProvisioned() ? 1 : 0);
                    if (SyncManager.this.mProvisioned) {
                        resolver.unregisterContentObserver(this);
                    }
                }
            };
            synchronized (this.mSyncHandler) {
                resolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, provisionedObserver);
                this.mProvisioned |= isDeviceProvisioned();
                if (this.mProvisioned) {
                    resolver.unregisterContentObserver(provisionedObserver);
                }
            }
        }
        if (!factoryTest) {
            this.mContext.registerReceiverAsUser(this.mAccountsUpdatedReceiver, UserHandle.ALL, new IntentFilter("android.accounts.LOGIN_ACCOUNTS_CHANGED"), null, null);
        }
        whiteListExistingSyncAdaptersIfNeeded();
        this.mLogger.log("Sync manager initialized: " + Build.FINGERPRINT);
    }

    public /* synthetic */ void lambda$new$0$SyncManager(Account account, int uid) {
        if (this.mAccountManagerInternal.hasAccountAccess(account, uid)) {
            scheduleSync(account, UserHandle.getUserId(uid), -2, null, null, 3, 0, Process.myUid(), -2, null);
        }
    }

    public /* synthetic */ void lambda$onStartUser$1$SyncManager(int userId) {
        this.mLogger.log("onStartUser: user=", Integer.valueOf(userId));
    }

    public void onStartUser(int userId) {
        this.mSyncHandler.post(new Runnable(userId) {
            /* class com.android.server.content.$$Lambda$SyncManager$CjX_2uO4O4xJPQnKzeqvGwd87Dc */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                SyncManager.this.lambda$onStartUser$1$SyncManager(this.f$1);
            }
        });
    }

    public void onUnlockUser(int userId) {
        synchronized (this.mUnlockedUsers) {
            this.mUnlockedUsers.put(userId, true);
        }
        this.mSyncHandler.post(new Runnable(userId) {
            /* class com.android.server.content.$$Lambda$SyncManager$6ygkGdDnrSLmR9G8Pz_n9zy2A */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                SyncManager.this.lambda$onUnlockUser$2$SyncManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onUnlockUser$2$SyncManager(int userId) {
        this.mLogger.log("onUnlockUser: user=", Integer.valueOf(userId));
    }

    public void onStopUser(int userId) {
        synchronized (this.mUnlockedUsers) {
            this.mUnlockedUsers.put(userId, false);
        }
        this.mSyncHandler.post(new Runnable(userId) {
            /* class com.android.server.content.$$Lambda$SyncManager$4nklbtZnJuPLOkU32f34xZoiug */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                SyncManager.this.lambda$onStopUser$3$SyncManager(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onStopUser$3$SyncManager(int userId) {
        this.mLogger.log("onStopUser: user=", Integer.valueOf(userId));
    }

    private boolean isUserUnlocked(int userId) {
        boolean z;
        synchronized (this.mUnlockedUsers) {
            z = this.mUnlockedUsers.get(userId);
        }
        return z;
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            this.mConstants.start();
        }
    }

    private void whiteListExistingSyncAdaptersIfNeeded() {
        SyncManager syncManager = this;
        if (syncManager.mSyncStorageEngine.shouldGrantSyncAdaptersAccountAccess()) {
            List<UserInfo> users = syncManager.mUserManager.getUsers(true);
            int userCount = users.size();
            int i = 0;
            while (i < userCount) {
                UserHandle userHandle = users.get(i).getUserHandle();
                int userId = userHandle.getIdentifier();
                for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> service : syncManager.mSyncAdapters.getAllServices(userId)) {
                    String packageName = service.componentName.getPackageName();
                    Account[] accountsByTypeAsUser = syncManager.mAccountManager.getAccountsByTypeAsUser(((SyncAdapterType) service.type).accountType, userHandle);
                    int length = accountsByTypeAsUser.length;
                    int i2 = 0;
                    while (i2 < length) {
                        Account account = accountsByTypeAsUser[i2];
                        if (!syncManager.canAccessAccount(account, packageName, userId)) {
                            syncManager.mAccountManager.updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", service.uid, true);
                        }
                        i2++;
                        syncManager = this;
                    }
                    syncManager = this;
                }
                i++;
                syncManager = this;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    private long jitterize(long minValue, long maxValue) {
        Random random = new Random(SystemClock.elapsedRealtime());
        long spread = maxValue - minValue;
        if (spread <= 2147483647L) {
            return ((long) random.nextInt((int) spread)) + minValue;
        }
        throw new IllegalArgumentException("the difference between the maxValue and the minValue must be less than 2147483647");
    }

    public SyncStorageEngine getSyncStorageEngine() {
        return this.mSyncStorageEngine;
    }

    private int getIsSyncable(Account account, int userId, String providerName) {
        int isSyncable = this.mSyncStorageEngine.getIsSyncable(account, userId, providerName);
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(userId);
        if (userInfo == null || !userInfo.isRestricted()) {
            return isSyncable;
        }
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(providerName, account.type), userId);
        if (syncAdapterInfo == null) {
            return 0;
        }
        try {
            PackageInfo pInfo = AppGlobals.getPackageManager().getPackageInfo(syncAdapterInfo.componentName.getPackageName(), 0, userId);
            if (pInfo == null || pInfo.restrictedAccountType == null || !pInfo.restrictedAccountType.equals(account.type)) {
                return 0;
            }
            return isSyncable;
        } catch (RemoteException e) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAuthorityPendingState(SyncStorageEngine.EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                getSyncStorageEngine().markPending(info, true);
                return;
            }
        }
        getSyncStorageEngine().markPending(info, false);
    }

    public void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState, int syncExemptionFlag, int callingUid, int callingPid, String callingPackage) {
        scheduleSync(requestedAccount, userId, reason, requestedAuthority, extras, targetSyncState, 0, true, syncExemptionFlag, callingUid, callingPid, callingPackage);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:88:0x030e, code lost:
        if (r12.mSyncStorageEngine.getSyncAutomatically(r11.account, r11.userId, r7) != false) goto L_0x0318;
     */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x031f  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x034d  */
    private void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState, long minDelayMillis, boolean checkIfAccountReady, int syncExemptionFlag, int callingUid, int callingPid, String callingPackage) {
        Bundle extras2;
        AccountAndUser[] accounts;
        int source;
        int i;
        boolean z;
        Bundle extras3;
        AccountAndUser[] accounts2;
        int i2;
        SyncManager syncManager;
        SyncManager syncManager2;
        AccountAndUser account;
        int isSyncable;
        String authority;
        boolean syncAllowed;
        Bundle extras4;
        AccountAndUser account2;
        long j;
        SyncManager syncManager3;
        SyncManager syncManager4 = this;
        int i3 = userId;
        String str = requestedAuthority;
        long j2 = minDelayMillis;
        if (extras == null) {
            extras2 = new Bundle();
        } else {
            extras2 = extras;
        }
        extras2.size();
        boolean z2 = false;
        if (Log.isLoggable("SyncManager", 2)) {
            syncManager4.mLogger.log("scheduleSync: account=", requestedAccount, " u", Integer.valueOf(userId), " authority=", str, " reason=", Integer.valueOf(reason), " extras=", extras2, " cuid=", Integer.valueOf(callingUid), " cpid=", Integer.valueOf(callingPid), " cpkg=", callingPackage, " mdm=", Long.valueOf(minDelayMillis), " ciar=", Boolean.valueOf(checkIfAccountReady), " sef=", Integer.valueOf(syncExemptionFlag));
        }
        if (requestedAccount == null) {
            accounts = syncManager4.mRunningAccounts;
        } else if (i3 != -1) {
            accounts = new AccountAndUser[]{new AccountAndUser(requestedAccount, i3)};
        } else {
            AccountAndUser[] accountAndUserArr = syncManager4.mRunningAccounts;
            AccountAndUser[] accounts3 = null;
            for (AccountAndUser runningAccount : accountAndUserArr) {
                if (requestedAccount.equals(runningAccount.account)) {
                    accounts3 = (AccountAndUser[]) ArrayUtils.appendElement(AccountAndUser.class, accounts3, runningAccount);
                }
            }
            accounts = accounts3;
        }
        if (ArrayUtils.isEmpty(accounts)) {
            syncManager4.mLogger.log("scheduleSync: no accounts configured, dropping");
            return;
        }
        boolean uploadOnly = extras2.getBoolean("upload", false);
        boolean manualSync = extras2.getBoolean("force", false);
        if (manualSync) {
            extras2.putBoolean("ignore_backoff", true);
            extras2.putBoolean("ignore_settings", true);
        }
        boolean ignoreSettings = extras2.getBoolean("ignore_settings", false);
        if (uploadOnly) {
            source = 1;
        } else if (manualSync) {
            source = 3;
        } else if (str == null) {
            source = 2;
        } else if (extras2.containsKey("feed")) {
            source = 5;
        } else {
            source = 0;
        }
        int length = accounts.length;
        int i4 = 0;
        while (i4 < length) {
            AccountAndUser account3 = accounts[i4];
            if (i3 < 0 || account3.userId < 0 || i3 == account3.userId) {
                HashSet<String> syncableAuthorities = new HashSet<>();
                for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapter : syncManager4.mSyncAdapters.getAllServices(account3.userId)) {
                    syncableAuthorities.add(((SyncAdapterType) syncAdapter.type).authority);
                }
                if (str != null) {
                    boolean hasSyncAdapter = syncableAuthorities.contains(str);
                    syncableAuthorities.clear();
                    if (hasSyncAdapter) {
                        syncableAuthorities.add(str);
                    }
                }
                Iterator<String> it = syncableAuthorities.iterator();
                while (it.hasNext()) {
                    String authority2 = it.next();
                    int isSyncable2 = syncManager4.computeSyncable(account3.account, account3.userId, authority2, !checkIfAccountReady);
                    if (isSyncable2 == 0) {
                        syncableAuthorities = syncableAuthorities;
                    } else {
                        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = syncManager4.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority2, account3.account.type), account3.userId);
                        if (syncAdapterInfo == null) {
                            syncableAuthorities = syncableAuthorities;
                        } else {
                            int owningUid = syncAdapterInfo.uid;
                            if (isSyncable2 == 3) {
                                syncManager4.mLogger.log("scheduleSync: Not scheduling sync operation: isSyncable == SYNCABLE_NO_ACCOUNT_ACCESS");
                                Bundle finalExtras = new Bundle(extras2);
                                String packageName = syncAdapterInfo.componentName.getPackageName();
                                if (!syncManager4.wasPackageEverLaunched(packageName, i3)) {
                                    syncableAuthorities = syncableAuthorities;
                                } else {
                                    j2 = minDelayMillis;
                                    i3 = userId;
                                    syncManager4.mAccountManagerInternal.requestAccountAccess(account3.account, packageName, i3, new RemoteCallback(new RemoteCallback.OnResultListener(account3, userId, reason, authority2, finalExtras, targetSyncState, j2, syncExemptionFlag, callingUid, callingPid, callingPackage) {
                                        /* class com.android.server.content.$$Lambda$SyncManager$BRGYMUC9QC6JWVXAvsoEZC6Zc */
                                        private final /* synthetic */ AccountAndUser f$1;
                                        private final /* synthetic */ int f$10;
                                        private final /* synthetic */ String f$11;
                                        private final /* synthetic */ int f$2;
                                        private final /* synthetic */ int f$3;
                                        private final /* synthetic */ String f$4;
                                        private final /* synthetic */ Bundle f$5;
                                        private final /* synthetic */ int f$6;
                                        private final /* synthetic */ long f$7;
                                        private final /* synthetic */ int f$8;
                                        private final /* synthetic */ int f$9;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                            this.f$3 = r4;
                                            this.f$4 = r5;
                                            this.f$5 = r6;
                                            this.f$6 = r7;
                                            this.f$7 = r8;
                                            this.f$8 = r10;
                                            this.f$9 = r11;
                                            this.f$10 = r12;
                                            this.f$11 = r13;
                                        }

                                        public final void onResult(Bundle bundle) {
                                            SyncManager.this.lambda$scheduleSync$4$SyncManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, bundle);
                                        }
                                    }));
                                    syncManager4 = this;
                                    length = length;
                                    accounts = accounts;
                                    syncableAuthorities = syncableAuthorities;
                                    account3 = account3;
                                    i4 = i4;
                                    extras2 = extras2;
                                }
                            } else {
                                boolean allowParallelSyncs = ((SyncAdapterType) syncAdapterInfo.type).allowParallelSyncs();
                                boolean isAlwaysSyncable = ((SyncAdapterType) syncAdapterInfo.type).isAlwaysSyncable();
                                if (checkIfAccountReady || isSyncable2 >= 0 || !isAlwaysSyncable) {
                                    syncManager2 = this;
                                    account = account3;
                                    isSyncable = isSyncable2;
                                } else {
                                    syncManager2 = this;
                                    account = account3;
                                    syncManager2.mSyncStorageEngine.setIsSyncable(account.account, account.userId, authority2, 1, callingUid, callingPid);
                                    isSyncable = 1;
                                }
                                if (targetSyncState != -2 && targetSyncState != isSyncable) {
                                    account3 = account;
                                    syncManager4 = syncManager2;
                                    length = length;
                                    accounts = accounts;
                                    syncableAuthorities = syncableAuthorities;
                                    i4 = i4;
                                    extras2 = extras2;
                                    j2 = minDelayMillis;
                                } else if (((SyncAdapterType) syncAdapterInfo.type).supportsUploading() || !uploadOnly) {
                                    if (isSyncable < 0 || ignoreSettings) {
                                        authority = authority2;
                                    } else {
                                        if (syncManager2.mSyncStorageEngine.getMasterSyncAutomatically(account.userId)) {
                                            authority = authority2;
                                        } else {
                                            authority = authority2;
                                        }
                                        syncAllowed = false;
                                        if (syncAllowed) {
                                            syncManager2.mLogger.log("scheduleSync: sync of ", " ", " ", authority, " is not allowed, dropping request");
                                            account3 = account;
                                            syncManager4 = syncManager2;
                                            length = length;
                                            accounts = accounts;
                                            syncableAuthorities = syncableAuthorities;
                                            i4 = i4;
                                            extras2 = extras2;
                                            j2 = minDelayMillis;
                                        } else {
                                            syncManager2.mSyncStorageEngine.getDelayUntilTime(new SyncStorageEngine.EndPoint(account.account, authority, account.userId));
                                            String owningPackage = syncAdapterInfo.componentName.getPackageName();
                                            if (isSyncable != -1) {
                                                syncManager3 = syncManager2;
                                                extras4 = extras2;
                                                if (targetSyncState != -2) {
                                                    if (targetSyncState != isSyncable) {
                                                        syncManager3.mLogger.log("scheduleSync: not handling ", account, " ", authority);
                                                        j = minDelayMillis;
                                                        account2 = account;
                                                    }
                                                }
                                                syncManager3.mLogger.log("scheduleSync: scheduling sync ", " ", " ", authority);
                                                j = minDelayMillis;
                                                account2 = account;
                                                syncManager3.postScheduleSyncMessage(new SyncOperation(account.account, account.userId, owningUid, owningPackage, reason, source, authority, extras4, allowParallelSyncs, syncExemptionFlag), j);
                                            } else if (checkIfAccountReady) {
                                                extras4 = extras2;
                                                sendOnUnsyncableAccount(syncManager2.mContext, syncAdapterInfo, account.userId, new OnReadyCallback(account, reason, authority, new Bundle(extras2), targetSyncState, minDelayMillis, syncExemptionFlag, callingUid, callingPid, callingPackage) {
                                                    /* class com.android.server.content.$$Lambda$SyncManager$XKEiBZ17uDgUCTwf_kh9_pH7usQ */
                                                    private final /* synthetic */ AccountAndUser f$1;
                                                    private final /* synthetic */ String f$10;
                                                    private final /* synthetic */ int f$2;
                                                    private final /* synthetic */ String f$3;
                                                    private final /* synthetic */ Bundle f$4;
                                                    private final /* synthetic */ int f$5;
                                                    private final /* synthetic */ long f$6;
                                                    private final /* synthetic */ int f$7;
                                                    private final /* synthetic */ int f$8;
                                                    private final /* synthetic */ int f$9;

                                                    {
                                                        this.f$1 = r2;
                                                        this.f$2 = r3;
                                                        this.f$3 = r4;
                                                        this.f$4 = r5;
                                                        this.f$5 = r6;
                                                        this.f$6 = r7;
                                                        this.f$7 = r9;
                                                        this.f$8 = r10;
                                                        this.f$9 = r11;
                                                        this.f$10 = r12;
                                                    }

                                                    @Override // com.android.server.content.SyncManager.OnReadyCallback
                                                    public final void onReady() {
                                                        SyncManager.this.lambda$scheduleSync$5$SyncManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10);
                                                    }
                                                });
                                                syncManager3 = this;
                                                j = minDelayMillis;
                                                account2 = account;
                                            } else {
                                                extras4 = extras2;
                                                Bundle newExtras = new Bundle();
                                                newExtras.putBoolean("initialize", true);
                                                syncManager3 = this;
                                                syncManager3.mLogger.log("scheduleSync: schedule initialisation sync ", " ", " ", authority);
                                                syncManager3.postScheduleSyncMessage(new SyncOperation(account.account, account.userId, owningUid, owningPackage, reason, source, authority, newExtras, allowParallelSyncs, syncExemptionFlag), minDelayMillis);
                                                j = minDelayMillis;
                                                account2 = account;
                                            }
                                            j2 = j;
                                            length = length;
                                            accounts = accounts;
                                            syncableAuthorities = syncableAuthorities;
                                            account3 = account2;
                                            extras2 = extras4;
                                            i4 = i4;
                                            syncManager4 = syncManager3;
                                            i3 = userId;
                                        }
                                    }
                                    syncAllowed = true;
                                    if (syncAllowed) {
                                    }
                                } else {
                                    account3 = account;
                                    syncManager4 = syncManager2;
                                    length = length;
                                    accounts = accounts;
                                    syncableAuthorities = syncableAuthorities;
                                    i4 = i4;
                                    extras2 = extras2;
                                    j2 = minDelayMillis;
                                }
                            }
                        }
                    }
                }
                i = i4;
                i2 = length;
                accounts2 = accounts;
                extras3 = extras2;
                syncManager = syncManager4;
                z = false;
            } else {
                z = z2;
                i = i4;
                i2 = length;
                accounts2 = accounts;
                extras3 = extras2;
                syncManager = syncManager4;
            }
            i4 = i + 1;
            str = requestedAuthority;
            j2 = j2;
            length = i2;
            accounts = accounts2;
            extras2 = extras3;
            z2 = z;
            syncManager4 = syncManager;
            i3 = userId;
        }
    }

    public /* synthetic */ void lambda$scheduleSync$4$SyncManager(AccountAndUser account, int userId, int reason, String authority, Bundle finalExtras, int targetSyncState, long minDelayMillis, int syncExemptionFlag, int callingUid, int callingPid, String callingPackage, Bundle result) {
        if (result == null) {
            return;
        }
        if (result.getBoolean("booleanResult")) {
            scheduleSync(account.account, userId, reason, authority, finalExtras, targetSyncState, minDelayMillis, true, syncExemptionFlag, callingUid, callingPid, callingPackage);
        }
    }

    public /* synthetic */ void lambda$scheduleSync$5$SyncManager(AccountAndUser account, int reason, String authority, Bundle finalExtras, int targetSyncState, long minDelayMillis, int syncExemptionFlag, int callingUid, int callingPid, String callingPackage) {
        scheduleSync(account.account, account.userId, reason, authority, finalExtras, targetSyncState, minDelayMillis, false, syncExemptionFlag, callingUid, callingPid, callingPackage);
    }

    public int computeSyncable(Account account, int userId, String authority, boolean checkAccountAccess) {
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo;
        int status = getIsSyncable(account, userId, authority);
        if (status == 0 || (syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority, account.type), userId)) == null) {
            return 0;
        }
        int owningUid = syncAdapterInfo.uid;
        String owningPackage = syncAdapterInfo.componentName.getPackageName();
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(owningUid, owningPackage)) {
                Slog.w("SyncManager", "Not scheduling job " + syncAdapterInfo.uid + ":" + syncAdapterInfo.componentName + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        if (!checkAccountAccess || canAccessAccount(account, owningPackage, owningUid)) {
            return status;
        }
        Log.w("SyncManager", "Access to " + SyncLogger.logSafe(account) + " denied for package " + owningPackage + " in UID " + syncAdapterInfo.uid);
        return 3;
    }

    private boolean canAccessAccount(Account account, String packageName, int uid) {
        if (this.mAccountManager.hasAccountAccess(account, packageName, UserHandle.getUserHandleForUid(uid))) {
            return true;
        }
        try {
            this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, DumpState.DUMP_DEXOPT, UserHandle.getUserId(uid));
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeSyncsForAuthority(SyncStorageEngine.EndPoint info, String why) {
        this.mLogger.log("removeSyncsForAuthority: ", info, why);
        verifyJobScheduler();
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.matchesSpec(info)) {
                this.mLogger.log("canceling: ", op);
                cancelJob(op, why);
            }
        }
    }

    public void removePeriodicSync(SyncStorageEngine.EndPoint target, Bundle extras, String why) {
        Message m = this.mSyncHandler.obtainMessage(14, Pair.create(target, why));
        m.setData(extras);
        m.sendToTarget();
    }

    public void updateOrAddPeriodicSync(SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras) {
        this.mSyncHandler.obtainMessage(13, new UpdatePeriodicSyncMessagePayload(target, pollFrequency, flex, extras)).sendToTarget();
    }

    public List<PeriodicSync> getPeriodicSyncs(SyncStorageEngine.EndPoint target) {
        List<SyncOperation> ops = getAllPendingSyncs();
        List<PeriodicSync> periodicSyncs = new ArrayList<>();
        for (SyncOperation op : ops) {
            if (op.isPeriodic) {
                if (op.target.matchesSpec(target)) {
                    periodicSyncs.add(new PeriodicSync(op.target.account, op.target.provider, op.extras, op.periodMillis / 1000, op.flexMillis / 1000));
                }
            }
        }
        return periodicSyncs;
    }

    public void scheduleLocalSync(Account account, int userId, int reason, String authority, int syncExemptionFlag, int callingUid, int callingPid, String callingPackage) {
        Bundle extras = new Bundle();
        extras.putBoolean("upload", true);
        scheduleSync(account, userId, reason, authority, extras, -2, LOCAL_SYNC_DELAY, true, syncExemptionFlag, callingUid, callingPid, callingPackage);
    }

    public SyncAdapterType[] getSyncAdapterTypes(int userId) {
        Collection<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> serviceInfos = this.mSyncAdapters.getAllServices(userId);
        SyncAdapterType[] types = new SyncAdapterType[serviceInfos.size()];
        int i = 0;
        for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
            types[i] = (SyncAdapterType) serviceInfo.type;
            i++;
        }
        return types;
    }

    public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        return this.mSyncAdapters.getSyncAdapterPackagesForAuthority(authority, userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSyncFinishedOrCanceledMessage(ActiveSyncContext syncContext, SyncResult syncResult) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_SYNC_FINISHED");
        }
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 1;
        msg.obj = new SyncFinishedOrCancelledMessagePayload(syncContext, syncResult);
        this.mSyncHandler.sendMessage(msg);
    }

    private void sendCancelSyncsMessage(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_CANCEL");
        }
        this.mLogger.log("sendCancelSyncsMessage() ep=", info, " why=", why);
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 6;
        msg.setData(extras);
        msg.obj = info;
        this.mSyncHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postMonitorSyncProgressMessage(ActiveSyncContext activeSyncContext) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "posting MESSAGE_SYNC_MONITOR in 60s");
        }
        activeSyncContext.mBytesTransferredAtLastPoll = getTotalBytesTransferredByUid(activeSyncContext.mSyncAdapterUid);
        activeSyncContext.mLastPolledTimeElapsed = SystemClock.elapsedRealtime();
        this.mSyncHandler.sendMessageDelayed(this.mSyncHandler.obtainMessage(8, activeSyncContext), 60000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postScheduleSyncMessage(SyncOperation syncOperation, long minDelayMillis) {
        this.mSyncHandler.obtainMessage(12, new ScheduleSyncMessagePayload(syncOperation, minDelayMillis)).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long getTotalBytesTransferredByUid(int uid) {
        return TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);
    }

    /* access modifiers changed from: private */
    public class SyncFinishedOrCancelledMessagePayload {
        public final ActiveSyncContext activeSyncContext;
        public final SyncResult syncResult;

        SyncFinishedOrCancelledMessagePayload(ActiveSyncContext syncContext, SyncResult syncResult2) {
            this.activeSyncContext = syncContext;
            this.syncResult = syncResult2;
        }
    }

    /* access modifiers changed from: private */
    public class UpdatePeriodicSyncMessagePayload {
        public final Bundle extras;
        public final long flex;
        public final long pollFrequency;
        public final SyncStorageEngine.EndPoint target;

        UpdatePeriodicSyncMessagePayload(SyncStorageEngine.EndPoint target2, long pollFrequency2, long flex2, Bundle extras2) {
            this.target = target2;
            this.pollFrequency = pollFrequency2;
            this.flex = flex2;
            this.extras = extras2;
        }
    }

    /* access modifiers changed from: private */
    public static class ScheduleSyncMessagePayload {
        final long minDelayMillis;
        final SyncOperation syncOperation;

        ScheduleSyncMessagePayload(SyncOperation syncOperation2, long minDelayMillis2) {
            this.syncOperation = syncOperation2;
            this.minDelayMillis = minDelayMillis2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearBackoffSetting(SyncStorageEngine.EndPoint target, String why) {
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if (backoff == null || ((Long) backoff.first).longValue() != -1 || ((Long) backoff.second).longValue() != -1) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Clearing backoffs for " + target);
            }
            this.mSyncStorageEngine.setBackoff(target, -1, -1);
            rescheduleSyncs(target, why);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void increaseBackoffSetting(SyncStorageEngine.EndPoint target) {
        long newDelayInMs;
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> previousSettings = this.mSyncStorageEngine.getBackoff(target);
        long newDelayInMs2 = -1;
        if (previousSettings != null) {
            if (now >= ((Long) previousSettings.first).longValue()) {
                newDelayInMs2 = (long) (((float) ((Long) previousSettings.second).longValue()) * this.mConstants.getRetryTimeIncreaseFactor());
            } else if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Still in backoff, do not increase it. Remaining: " + ((((Long) previousSettings.first).longValue() - now) / 1000) + " seconds.");
                return;
            } else {
                return;
            }
        }
        if (newDelayInMs2 <= 0) {
            long initialRetryMs = (long) (this.mConstants.getInitialSyncRetryTimeInSeconds() * 1000);
            newDelayInMs2 = jitterize(initialRetryMs, (long) (((double) initialRetryMs) * 1.1d));
        }
        long maxSyncRetryTimeInSeconds = (long) this.mConstants.getMaxSyncRetryTimeInSeconds();
        if (newDelayInMs2 > maxSyncRetryTimeInSeconds * 1000) {
            newDelayInMs = 1000 * maxSyncRetryTimeInSeconds;
        } else {
            newDelayInMs = newDelayInMs2;
        }
        long backoff = now + newDelayInMs;
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Backoff until: " + backoff + ", delayTime: " + newDelayInMs);
        }
        this.mSyncStorageEngine.setBackoff(target, backoff, newDelayInMs);
        rescheduleSyncs(target, "increaseBackoffSetting");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rescheduleSyncs(SyncStorageEngine.EndPoint target, String why) {
        this.mLogger.log("rescheduleSyncs() ep=", target, " why=", why);
        int count = 0;
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(target)) {
                count++;
                cancelJob(op, why);
                postScheduleSyncMessage(op, 0);
            }
        }
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Rescheduled " + count + " syncs for " + target);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDelayUntilTime(SyncStorageEngine.EndPoint target, long delayUntilSeconds) {
        long newDelayUntilTime;
        long delayUntil = 1000 * delayUntilSeconds;
        long absoluteNow = System.currentTimeMillis();
        if (delayUntil > absoluteNow) {
            newDelayUntilTime = SystemClock.elapsedRealtime() + (delayUntil - absoluteNow);
        } else {
            newDelayUntilTime = 0;
        }
        this.mSyncStorageEngine.setDelayUntilTime(target, newDelayUntilTime);
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Delay Until time set to " + newDelayUntilTime + " for " + target);
        }
        rescheduleSyncs(target, "delayUntil newDelayUntilTime: " + newDelayUntilTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAdapterDelayed(SyncStorageEngine.EndPoint target) {
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if ((backoff == null || ((Long) backoff.first).longValue() == -1 || ((Long) backoff.first).longValue() <= now) && this.mSyncStorageEngine.getDelayUntilTime(target) <= now) {
            return false;
        }
        return true;
    }

    public void cancelActiveSync(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
        sendCancelSyncsMessage(info, extras, why);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleSyncOperationH(SyncOperation syncOperation) {
        scheduleSyncOperationH(syncOperation, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleSyncOperationH(SyncOperation syncOperation, long minDelay) {
        boolean z;
        DeviceIdleController.LocalService dic;
        long now;
        boolean isLoggable;
        long backoffDelay;
        long minDelay2 = minDelay;
        boolean isLoggable2 = Log.isLoggable("SyncManager", 2);
        if (syncOperation == null) {
            Slog.e("SyncManager", "Can't schedule null sync operation.");
            return;
        }
        if (!syncOperation.ignoreBackoff()) {
            Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(syncOperation.target);
            if (backoff == null) {
                Slog.e("SyncManager", "Couldn't find backoff values for " + SyncLogger.logSafe(syncOperation.target));
                backoff = new Pair<>(-1L, -1L);
            }
            long now2 = SystemClock.elapsedRealtime();
            if (((Long) backoff.first).longValue() == -1) {
                backoffDelay = 0;
            } else {
                backoffDelay = ((Long) backoff.first).longValue() - now2;
            }
            long delayUntil = this.mSyncStorageEngine.getDelayUntilTime(syncOperation.target);
            long delayUntilDelay = delayUntil > now2 ? delayUntil - now2 : 0;
            Slog.d("SyncManager", "backoff delay:" + backoffDelay + " delayUntil delay:" + delayUntilDelay + " minDelay" + minDelay2);
            minDelay2 = Math.max(minDelay2, Math.max(backoffDelay, delayUntilDelay));
        }
        if (minDelay2 < 0) {
            minDelay2 = 0;
        }
        if (!syncOperation.isPeriodic) {
            int inheritedSyncExemptionFlag = 0;
            Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
            while (it.hasNext()) {
                if (it.next().mSyncOperation.key.equals(syncOperation.key)) {
                    if (isLoggable2) {
                        Log.v("SyncManager", "Duplicate sync is already running. Not scheduling " + syncOperation);
                        return;
                    }
                    return;
                }
            }
            int duplicatesCount = 0;
            long now3 = SystemClock.elapsedRealtime();
            syncOperation.expectedRuntime = now3 + minDelay2;
            List<SyncOperation> pending = getAllPendingSyncs();
            SyncOperation syncToRun = syncOperation;
            for (SyncOperation op : pending) {
                if (!op.isPeriodic) {
                    if (op.key.equals(syncOperation.key)) {
                        now = now3;
                        isLoggable = isLoggable2;
                        if (syncToRun.expectedRuntime > op.expectedRuntime) {
                            syncToRun = op;
                        }
                        duplicatesCount++;
                    } else {
                        isLoggable = isLoggable2;
                        now = now3;
                    }
                    isLoggable2 = isLoggable;
                    now3 = now;
                }
            }
            if (duplicatesCount > 1) {
                Slog.e("SyncManager", "FATAL ERROR! File a bug if you see this.");
            }
            if (syncOperation != syncToRun && minDelay2 == 0 && syncToRun.syncExemptionFlag < syncOperation.syncExemptionFlag) {
                syncToRun = syncOperation;
                inheritedSyncExemptionFlag = Math.max(0, syncToRun.syncExemptionFlag);
            }
            for (SyncOperation op2 : pending) {
                if (!op2.isPeriodic && op2.key.equals(syncOperation.key) && op2 != syncToRun) {
                    if (isLoggable2) {
                        Slog.v("SyncManager", "Cancelling duplicate sync " + op2);
                    }
                    inheritedSyncExemptionFlag = Math.max(inheritedSyncExemptionFlag, op2.syncExemptionFlag);
                    cancelJob(op2, "scheduleSyncOperationH-duplicate");
                }
            }
            if (syncToRun != syncOperation) {
                Slog.i("SyncManager", "Not scheduling because a duplicate exists:" + syncOperation + " expectedRuntime: " + syncToRun.expectedRuntime);
                return;
            } else if (inheritedSyncExemptionFlag > 0) {
                syncOperation.syncExemptionFlag = inheritedSyncExemptionFlag;
            }
        }
        if (syncOperation.jobId == -1) {
            syncOperation.jobId = getUnusedJobIdH();
        }
        Slog.d("SyncManager", "scheduling sync operation " + syncOperation.toString());
        JobInfo.Builder b = new JobInfo.Builder(syncOperation.jobId, new ComponentName(this.mContext, SyncJobService.class)).setExtras(syncOperation.toJobInfoExtras()).setRequiredNetworkType(syncOperation.isNotAllowedOnMetered() ? 2 : 1).setPersisted(true).setPriority(syncOperation.findPriority()).setFlags(syncOperation.isAppStandbyExempted() ? 8 : 0);
        if (syncOperation.isPeriodic) {
            b.setPeriodic(syncOperation.periodMillis, syncOperation.flexMillis);
            z = true;
        } else {
            if (minDelay2 > 0) {
                b.setMinimumLatency(minDelay2);
            }
            z = true;
            getSyncStorageEngine().markPending(syncOperation.target, true);
        }
        if (syncOperation.extras.getBoolean("require_charging")) {
            b.setRequiresCharging(z);
        }
        if (syncOperation.syncExemptionFlag == 2 && (dic = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class)) != null) {
            dic.addPowerSaveTempWhitelistApp(1000, syncOperation.owningPackage, (long) (this.mConstants.getKeyExemptionTempWhitelistDurationInSeconds() * 1000), UserHandle.getUserId(syncOperation.owningUid), false, "sync by top app");
        }
        UsageStatsManagerInternal usmi = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        if (usmi != null) {
            usmi.reportSyncScheduled(syncOperation.owningPackage, UserHandle.getUserId(syncOperation.owningUid), syncOperation.isAppStandbyExempted());
        }
        getJobScheduler().scheduleAsPackage(b.build(), syncOperation.owningPackage, syncOperation.target.userId, syncOperation.wakeLockName());
    }

    public void clearScheduledSyncOperations(SyncStorageEngine.EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                cancelJob(op, "clearScheduledSyncOperations");
                getSyncStorageEngine().markPending(op.target, false);
            }
        }
        this.mSyncStorageEngine.setBackoff(info, -1, -1);
    }

    public void cancelScheduledSyncOperation(SyncStorageEngine.EndPoint info, Bundle extras) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info) && syncExtrasEquals(extras, op.extras, false)) {
                cancelJob(op, "cancelScheduledSyncOperation");
            }
        }
        setAuthorityPendingState(info);
        if (!this.mSyncStorageEngine.isSyncPending(info)) {
            this.mSyncStorageEngine.setBackoff(info, -1, -1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeRescheduleSync(SyncResult syncResult, SyncOperation operation) {
        boolean isLoggable = Log.isLoggable("SyncManager", 3);
        if (isLoggable) {
            Log.d("SyncManager", "encountered error(s) during the sync: " + syncResult + ", " + operation);
        }
        if (operation.extras.getBoolean("ignore_backoff", false)) {
            operation.extras.remove("ignore_backoff");
        }
        if (!operation.extras.getBoolean("do_not_retry", false) || syncResult.syncAlreadyInProgress) {
            if (operation.extras.getBoolean("upload", false) && !syncResult.syncAlreadyInProgress) {
                operation.extras.remove("upload");
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation as a two-way sync because an upload-only sync encountered an error: " + operation);
                }
                scheduleSyncOperationH(operation);
            } else if (syncResult.tooManyRetries) {
                if (isLoggable) {
                    Log.d("SyncManager", "not retrying sync operation because it retried too many times: " + operation);
                }
            } else if (syncResult.madeSomeProgress()) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation because even though it had an error it achieved some success");
                }
                scheduleSyncOperationH(operation);
            } else if (syncResult.syncAlreadyInProgress) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation that failed because there was already a sync in progress: " + operation);
                }
                scheduleSyncOperationH(operation, 10000);
            } else if (syncResult.hasSoftError()) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation because it encountered a soft error: " + operation);
                }
                scheduleSyncOperationH(operation);
            } else {
                Log.e("SyncManager", "not retrying sync operation because the error is a hard error: " + SyncLogger.logSafe(operation));
            }
        } else if (isLoggable) {
            Log.d("SyncManager", "not retrying sync operation because SYNC_EXTRAS_DO_NOT_RETRY was specified " + operation);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserUnlocked(int userId) {
        AccountManagerService.getSingleton().validateAccounts(userId);
        this.mSyncAdapters.invalidateCache(userId);
        updateRunningAccounts(new SyncStorageEngine.EndPoint(null, null, userId));
        for (Account account : AccountManagerService.getSingleton().getAccounts(userId, this.mContext.getOpPackageName())) {
            scheduleSync(account, userId, -8, null, null, -1, 0, Process.myUid(), -3, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserStopped(int userId) {
        updateRunningAccounts(null);
        cancelActiveSync(new SyncStorageEngine.EndPoint(null, null, userId), null, "onUserStopped");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserRemoved(int userId) {
        this.mLogger.log("onUserRemoved: u", Integer.valueOf(userId));
        updateRunningAccounts(null);
        this.mSyncStorageEngine.removeStaleAccounts(null, userId);
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.userId == userId) {
                cancelJob(op, "user removed u" + userId);
            }
        }
    }

    static Intent getAdapterBindIntent(Context context, ComponentName syncAdapterComponent, int userId) {
        Intent intent = new Intent();
        intent.setAction("android.content.SyncAdapter");
        intent.setComponent(syncAdapterComponent);
        intent.putExtra("android.intent.extra.client_label", 17041349);
        intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(context, 0, new Intent("android.settings.SYNC_SETTINGS"), 0, null, UserHandle.of(userId)));
        return intent;
    }

    /* access modifiers changed from: package-private */
    public class ActiveSyncContext extends ISyncContext.Stub implements ServiceConnection, IBinder.DeathRecipient {
        private static final int SOURCE_USER = 3;
        boolean mBound;
        long mBytesTransferredAtLastPoll;
        String mEventName;
        final long mHistoryRowId;
        boolean mIsLinkedToDeath = false;
        long mLastPolledTimeElapsed;
        final long mStartTime;
        ISyncAdapter mSyncAdapter;
        final int mSyncAdapterUid;
        SyncInfo mSyncInfo;
        final SyncOperation mSyncOperation;
        final PowerManager.WakeLock mSyncWakeLock;
        long mTimeoutStartTime;

        public ActiveSyncContext(SyncOperation syncOperation, long historyRowId, int syncAdapterUid) {
            this.mSyncAdapterUid = syncAdapterUid;
            this.mSyncOperation = syncOperation;
            this.mHistoryRowId = historyRowId;
            this.mSyncAdapter = null;
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mTimeoutStartTime = this.mStartTime;
            this.mSyncWakeLock = SyncManager.this.mSyncHandler.getSyncWakeLock(this.mSyncOperation);
            this.mSyncWakeLock.setWorkSource(new WorkSource(syncAdapterUid));
            this.mSyncWakeLock.acquire();
        }

        public void sendHeartbeat() {
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003f: APUT  (r2v2 java.lang.Object[]), (3 ??[int, float, short, byte, char]), (r3v4 java.lang.Object) */
        public void onFinished(SyncResult result) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "onFinished: " + this);
            }
            SyncLogger syncLogger = SyncManager.this.mLogger;
            Object[] objArr = new Object[4];
            objArr[0] = "onFinished result=";
            objArr[1] = result;
            objArr[2] = " endpoint=";
            SyncOperation syncOperation = this.mSyncOperation;
            objArr[3] = syncOperation == null ? "null" : syncOperation.target;
            syncLogger.log(objArr);
            SyncManager.this.sendSyncFinishedOrCanceledMessage(this, result);
        }

        public void toString(StringBuilder sb, boolean logSafe) {
            sb.append("startTime ");
            sb.append(this.mStartTime);
            sb.append(", mTimeoutStartTime ");
            sb.append(this.mTimeoutStartTime);
            sb.append(", mHistoryRowId ");
            sb.append(this.mHistoryRowId);
            sb.append(", syncOperation ");
            SyncOperation syncOperation = this.mSyncOperation;
            String str = syncOperation;
            if (logSafe) {
                str = SyncLogger.logSafe(syncOperation);
            }
            sb.append(str);
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Message msg = SyncManager.this.mSyncHandler.obtainMessage();
            msg.what = 4;
            msg.obj = new ServiceConnectionData(this, service);
            SyncManager.this.mSyncHandler.sendMessage(msg);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Message msg = SyncManager.this.mSyncHandler.obtainMessage();
            msg.what = 5;
            msg.obj = new ServiceConnectionData(this, null);
            SyncManager.this.mSyncHandler.sendMessage(msg);
        }

        /* access modifiers changed from: package-private */
        public boolean bindToSyncAdapter(ComponentName serviceComponent, int userId, SyncOperation op) {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "bindToSyncAdapter: " + serviceComponent + ", connection " + this);
            }
            Intent intent = SyncManager.getAdapterBindIntent(SyncManager.this.mContext, serviceComponent, userId);
            this.mBound = true;
            intent.addHwFlags(64);
            if (op.syncSource == 3) {
                intent.addHwFlags(128);
            }
            boolean bindResult = SyncManager.this.mContext.bindServiceAsUser(intent, this, 21, new UserHandle(this.mSyncOperation.target.userId));
            SyncManager.this.mLogger.log("bindService() returned=", Boolean.valueOf(this.mBound), " for ", this);
            if (!bindResult) {
                this.mBound = false;
            } else {
                try {
                    this.mEventName = this.mSyncOperation.wakeLockName();
                    SyncManager.this.mBatteryStats.noteSyncStart(this.mEventName, this.mSyncAdapterUid);
                } catch (RemoteException e) {
                }
            }
            return bindResult;
        }

        /* access modifiers changed from: protected */
        public void close() {
            Log.d("SyncManager", "unBindFromSyncAdapter: connection " + this);
            if (this.mBound) {
                this.mBound = false;
                SyncManager.this.mLogger.log("unbindService for ", this);
                SyncManager.this.mContext.unbindService(this);
                try {
                    SyncManager.this.mBatteryStats.noteSyncFinish(this.mEventName, this.mSyncAdapterUid);
                } catch (RemoteException e) {
                }
            }
            this.mSyncWakeLock.release();
            this.mSyncWakeLock.setWorkSource(null);
        }

        @Override // java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, false);
            return sb.toString();
        }

        public String toSafeString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, true);
            return sb.toString();
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            SyncManager.this.sendSyncFinishedOrCanceledMessage(this, null);
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, boolean dumpAll) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        dumpSyncState(ipw, new SyncAdapterStateFetcher());
        this.mConstants.dump(pw, "");
        dumpSyncAdapters(ipw);
        if (dumpAll) {
            ipw.println("Detailed Sync History");
            this.mLogger.dumpAll(pw);
        }
    }

    static String formatTime(long time) {
        if (time == 0) {
            return "N/A";
        }
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }

    static /* synthetic */ int lambda$static$6(SyncOperation op1, SyncOperation op2) {
        int res = Integer.compare(op1.target.userId, op2.target.userId);
        if (res != 0) {
            return res;
        }
        Comparator<String> stringComparator = String.CASE_INSENSITIVE_ORDER;
        int res2 = stringComparator.compare(op1.target.account.type, op2.target.account.type);
        if (res2 != 0) {
            return res2;
        }
        int res3 = stringComparator.compare(op1.target.account.name, op2.target.account.name);
        if (res3 != 0) {
            return res3;
        }
        int res4 = stringComparator.compare(op1.target.provider, op2.target.provider);
        if (res4 != 0) {
            return res4;
        }
        int res5 = Integer.compare(op1.reason, op2.reason);
        if (res5 != 0) {
            return res5;
        }
        int res6 = Long.compare(op1.periodMillis, op2.periodMillis);
        if (res6 != 0) {
            return res6;
        }
        int res7 = Long.compare(op1.expectedRuntime, op2.expectedRuntime);
        if (res7 != 0) {
            return res7;
        }
        int res8 = Long.compare((long) op1.jobId, (long) op2.jobId);
        if (res8 != 0) {
            return res8;
        }
        return 0;
    }

    static /* synthetic */ int lambda$static$7(SyncOperation op1, SyncOperation op2) {
        int res = Long.compare(op1.expectedRuntime, op2.expectedRuntime);
        if (res != 0) {
            return res;
        }
        return sOpDumpComparator.compare(op1, op2);
    }

    private static <T> int countIf(Collection<T> col, Predicate<T> p) {
        int ret = 0;
        for (T item : col) {
            if (p.test(item)) {
                ret++;
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void dumpPendingSyncs(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        List<SyncOperation> pendingSyncs = getAllPendingSyncs();
        pw.print("Pending Syncs: ");
        pw.println(countIf(pendingSyncs, $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc.INSTANCE));
        Collections.sort(pendingSyncs, sOpRuntimeComparator);
        int count = 0;
        for (SyncOperation op : pendingSyncs) {
            if (!op.isPeriodic) {
                pw.println(op.dump(null, false, buckets, false));
                count++;
            }
        }
        pw.println();
    }

    static /* synthetic */ boolean lambda$dumpPendingSyncs$8(SyncOperation op) {
        return !op.isPeriodic;
    }

    /* access modifiers changed from: protected */
    public void dumpPeriodicSyncs(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        List<SyncOperation> pendingSyncs = getAllPendingSyncs();
        pw.print("Periodic Syncs: ");
        pw.println(countIf(pendingSyncs, $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw.INSTANCE));
        Collections.sort(pendingSyncs, sOpDumpComparator);
        int count = 0;
        for (SyncOperation op : pendingSyncs) {
            if (op.isPeriodic) {
                pw.println(op.dump(null, false, buckets, false));
                count++;
            }
        }
        pw.println();
    }

    public static StringBuilder formatDurationHMS(StringBuilder sb, long duration) {
        long duration2 = duration / 1000;
        if (duration2 < 0) {
            sb.append('-');
            duration2 = -duration2;
        }
        long seconds = duration2 % 60;
        long duration3 = duration2 / 60;
        long minutes = duration3 % 60;
        long duration4 = duration3 / 60;
        long hours = duration4 % 24;
        long duration5 = duration4 / 24;
        boolean print = false;
        if (duration5 > 0) {
            sb.append(duration5);
            sb.append('d');
            print = true;
        }
        if (!printTwoDigitNumber(sb, seconds, 's', printTwoDigitNumber(sb, minutes, 'm', printTwoDigitNumber(sb, hours, 'h', print)))) {
            sb.append("0s");
        }
        return sb;
    }

    private static boolean printTwoDigitNumber(StringBuilder sb, long value, char unit, boolean always) {
        if (!always && value == 0) {
            return false;
        }
        if (always && value < 10) {
            sb.append('0');
        }
        sb.append(value);
        sb.append(unit);
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0218: APUT  (r15v1 java.lang.Object[]), (3 ??[int, float, short, byte, char]), (r5v12 java.lang.String) */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x04d2, code lost:
        r0 = th;
     */
    public void dumpSyncState(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        boolean unlocked;
        PrintTable table;
        ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses;
        SyncManager syncManager = this;
        StringBuilder sb = new StringBuilder();
        pw.print("Data connected: ");
        pw.println(syncManager.mDataConnectionIsConnected);
        pw.print("Battery saver: ");
        PowerManager powerManager = syncManager.mPowerManager;
        pw.println(powerManager != null && powerManager.isPowerSaveMode());
        pw.print("Background network restriction: ");
        ConnectivityManager cm = getConnectivityManager();
        int status = cm == null ? -1 : cm.getRestrictBackgroundStatus();
        if (status == 1) {
            pw.println(" disabled");
        } else if (status == 2) {
            pw.println(" whitelisted");
        } else if (status != 3) {
            pw.print("Unknown(");
            pw.print(status);
            pw.println(")");
        } else {
            pw.println(" enabled");
        }
        pw.print("Auto sync: ");
        List<UserInfo> users = getAllUsers();
        if (users != null) {
            for (UserInfo user : users) {
                pw.print("u" + user.id + "=" + syncManager.mSyncStorageEngine.getMasterSyncAutomatically(user.id) + " ");
            }
            pw.println();
        }
        pw.print("Memory low: ");
        pw.println(syncManager.mStorageIsLow);
        pw.print("Device idle: ");
        pw.println(syncManager.mDeviceIsIdle);
        pw.print("Reported active: ");
        pw.println(syncManager.mReportedSyncActive);
        pw.print("Clock valid: ");
        pw.println(syncManager.mSyncStorageEngine.isClockValid());
        AccountAndUser[] accounts = AccountManagerService.getSingleton().getAllAccounts();
        pw.print("Accounts: ");
        if (accounts != INITIAL_ACCOUNTS_ARRAY) {
            pw.println(accounts.length);
        } else {
            pw.println("not known yet");
        }
        long now = SystemClock.elapsedRealtime();
        pw.print("Now: ");
        pw.print(now);
        pw.println(" (" + formatTime(System.currentTimeMillis()) + ")");
        sb.setLength(0);
        pw.print("Uptime: ");
        pw.print(formatDurationHMS(sb, now));
        pw.println();
        pw.print("Time spent syncing: ");
        sb.setLength(0);
        pw.print(formatDurationHMS(sb, syncManager.mSyncHandler.mSyncTimeTracker.timeSpentSyncing()));
        pw.print(", sync ");
        pw.print(syncManager.mSyncHandler.mSyncTimeTracker.mLastWasSyncing ? "" : "not ");
        pw.println("in progress");
        pw.println();
        pw.println("Active Syncs: " + syncManager.mActiveSyncContexts.size());
        PackageManager pm = syncManager.mContext.getPackageManager();
        Iterator<ActiveSyncContext> it = syncManager.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            ActiveSyncContext activeSyncContext = it.next();
            pw.print("  ");
            sb.setLength(0);
            pw.print(formatDurationHMS(sb, now - activeSyncContext.mStartTime));
            pw.print(" - ");
            pw.print(activeSyncContext.mSyncOperation.dump(pm, false, buckets, false));
            pw.println();
        }
        pw.println();
        dumpPendingSyncs(pw, buckets);
        dumpPeriodicSyncs(pw, buckets);
        pw.println("Sync Status");
        ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses2 = new ArrayList<>();
        syncManager.mSyncStorageEngine.resetTodayStats(false);
        int i = 0;
        for (int length = accounts.length; i < length; length = length) {
            AccountAndUser account = accounts[i];
            synchronized (syncManager.mUnlockedUsers) {
                unlocked = syncManager.mUnlockedUsers.get(account.userId);
            }
            Object[] objArr = new Object[4];
            objArr[0] = "XXXXXXXXX";
            objArr[1] = Integer.valueOf(account.userId);
            objArr[2] = account.account.type;
            objArr[3] = unlocked ? "" : " (locked)";
            pw.printf("Account %s u%d %s%s\n", objArr);
            pw.println("=======================================================================");
            PrintTable table2 = new PrintTable(16);
            table2.set(0, 0, "Authority", "Syncable", "Enabled", "Stats", "Loc", "Poll", "Per", "Feed", "User", "Othr", "Tot", "Fail", "Can", "Time", "Last Sync", "Backoff");
            List<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> sorted = Lists.newArrayList();
            sorted.addAll(syncManager.mSyncAdapters.getAllServices(account.userId));
            Collections.sort(sorted, new Comparator<RegisteredServicesCache.ServiceInfo<SyncAdapterType>>() {
                /* class com.android.server.content.SyncManager.AnonymousClass13 */

                public int compare(RegisteredServicesCache.ServiceInfo<SyncAdapterType> lhs, RegisteredServicesCache.ServiceInfo<SyncAdapterType> rhs) {
                    return ((SyncAdapterType) lhs.type).authority.compareTo(((SyncAdapterType) rhs.type).authority);
                }
            });
            Iterator<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> it2 = sorted.iterator();
            while (it2.hasNext()) {
                RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterType = it2.next();
                if (!((SyncAdapterType) syncAdapterType.type).accountType.equals(account.account.type)) {
                    sorted = sorted;
                    it2 = it2;
                } else {
                    int row = table2.getNumRows();
                    Pair<SyncStorageEngine.AuthorityInfo, SyncStatusInfo> syncAuthoritySyncStatus = syncManager.mSyncStorageEngine.getCopyOfAuthorityWithSyncStatus(new SyncStorageEngine.EndPoint(account.account, ((SyncAdapterType) syncAdapterType.type).authority, account.userId));
                    SyncStorageEngine.AuthorityInfo settings = (SyncStorageEngine.AuthorityInfo) syncAuthoritySyncStatus.first;
                    SyncStatusInfo status2 = (SyncStatusInfo) syncAuthoritySyncStatus.second;
                    statuses2.add(Pair.create(settings.target, status2));
                    String authority = settings.target.provider;
                    if (authority.length() > 50) {
                        authority = authority.substring(authority.length() - 50);
                    }
                    table2.set(row, 0, authority, Integer.valueOf(settings.syncable), Boolean.valueOf(settings.enabled));
                    QuadConsumer<String, SyncStatusInfo.Stats, Function<Integer, String>, Integer> c = new QuadConsumer(sb, table2) {
                        /* class com.android.server.content.$$Lambda$SyncManager$9EoLpTk5JrHZn9RuS0lqCVrpRw */
                        private final /* synthetic */ StringBuilder f$0;
                        private final /* synthetic */ SyncManager.PrintTable f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
                            SyncManager.lambda$dumpSyncState$10(this.f$0, this.f$1, (String) obj, (SyncStatusInfo.Stats) obj2, (Function) obj3, (Integer) obj4);
                        }
                    };
                    c.accept("Total", status2.totalStats, $$Lambda$SyncManager$pdoEVnuSkmOrvULQ9M7IclU5vw.INSTANCE, Integer.valueOf(row));
                    c.accept("Today", status2.todayStats, new Function() {
                        /* class com.android.server.content.$$Lambda$SyncManager$EMXCZP9LDjgUTYbLsEoVu9Ccntw */

                        @Override // java.util.function.Function
                        public final Object apply(Object obj) {
                            return SyncManager.this.zeroToEmpty(((Integer) obj).intValue());
                        }
                    }, Integer.valueOf(row + 1));
                    c.accept("Yestr", status2.yesterdayStats, new Function() {
                        /* class com.android.server.content.$$Lambda$SyncManager$EMXCZP9LDjgUTYbLsEoVu9Ccntw */

                        @Override // java.util.function.Function
                        public final Object apply(Object obj) {
                            return SyncManager.this.zeroToEmpty(((Integer) obj).intValue());
                        }
                    }, Integer.valueOf(row + 2));
                    if (settings.delayUntil > now) {
                        int row1 = row + 1;
                        table2.set(row, 15, "D: " + ((settings.delayUntil - now) / 1000));
                        if (settings.backoffTime > now) {
                            int row12 = row1 + 1;
                            table2.set(row1, 15, "B: " + ((settings.backoffTime - now) / 1000));
                            int i2 = row12 + 1;
                            table = table2;
                            table.set(row12, 15, Long.valueOf(settings.backoffDelay / 1000));
                        } else {
                            table = table2;
                        }
                    } else {
                        table = table2;
                    }
                    int row13 = row;
                    if (status2.lastSuccessTime != 0) {
                        int row14 = row13 + 1;
                        table.set(row13, 14, SyncStorageEngine.SOURCES[status2.lastSuccessSource] + " SUCCESS");
                        row13 = row14 + 1;
                        table.set(row14, 14, formatTime(status2.lastSuccessTime));
                    }
                    if (status2.lastFailureTime != 0) {
                        int row15 = row13 + 1;
                        table.set(row13, 14, SyncStorageEngine.SOURCES[status2.lastFailureSource] + " FAILURE");
                        int row16 = row15 + 1;
                        statuses = statuses2;
                        table.set(row15, 14, formatTime(status2.lastFailureTime));
                        int i3 = row16 + 1;
                        table.set(row16, 14, status2.lastFailureMesg);
                    } else {
                        statuses = statuses2;
                    }
                    syncManager = this;
                    table2 = table;
                    statuses2 = statuses;
                    sorted = sorted;
                    it2 = it2;
                    users = users;
                    accounts = accounts;
                    pm = pm;
                    sb = sb;
                    account = account;
                }
            }
            table2.writeTo(pw);
            i++;
            syncManager = this;
        }
        dumpSyncHistory(pw);
        pw.println();
        pw.println("Per Adapter History");
        pw.println("(SERVER is now split up to FEED and OTHER)");
        int i4 = 0;
        for (ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses3 = statuses2; i4 < statuses3.size(); statuses3 = statuses3) {
            Pair<SyncStorageEngine.EndPoint, SyncStatusInfo> event = statuses3.get(i4);
            pw.print("  ");
            pw.print("XXXXXXXXX");
            pw.print('/');
            pw.print(((SyncStorageEngine.EndPoint) event.first).account.type);
            pw.print(" u");
            pw.print(((SyncStorageEngine.EndPoint) event.first).userId);
            pw.print(" [");
            pw.print(((SyncStorageEngine.EndPoint) event.first).provider);
            pw.print("]");
            pw.println();
            pw.println("    Per source last syncs:");
            for (int j = 0; j < SyncStorageEngine.SOURCES.length; j++) {
                pw.print("      ");
                pw.print(String.format("%8s", SyncStorageEngine.SOURCES[j]));
                pw.print("  Success: ");
                pw.print(formatTime(((SyncStatusInfo) event.second).perSourceLastSuccessTimes[j]));
                pw.print("  Failure: ");
                pw.println(formatTime(((SyncStatusInfo) event.second).perSourceLastFailureTimes[j]));
            }
            pw.println("    Last syncs:");
            for (int j2 = 0; j2 < ((SyncStatusInfo) event.second).getEventCount(); j2++) {
                pw.print("      ");
                pw.print(formatTime(((SyncStatusInfo) event.second).getEventTime(j2)));
                pw.print(' ');
                pw.print(((SyncStatusInfo) event.second).getEvent(j2));
                pw.println();
            }
            if (((SyncStatusInfo) event.second).getEventCount() == 0) {
                pw.println("      N/A");
            }
            i4++;
        }
        return;
        while (true) {
        }
    }

    static /* synthetic */ void lambda$dumpSyncState$10(StringBuilder sb, PrintTable table, String label, SyncStatusInfo.Stats stats, Function filter, Integer r) {
        sb.setLength(0);
        table.set(r.intValue(), 3, label, filter.apply(Integer.valueOf(stats.numSourceLocal)), filter.apply(Integer.valueOf(stats.numSourcePoll)), filter.apply(Integer.valueOf(stats.numSourcePeriodic)), filter.apply(Integer.valueOf(stats.numSourceFeed)), filter.apply(Integer.valueOf(stats.numSourceUser)), filter.apply(Integer.valueOf(stats.numSourceOther)), filter.apply(Integer.valueOf(stats.numSyncs)), filter.apply(Integer.valueOf(stats.numFailures)), filter.apply(Integer.valueOf(stats.numCancels)), formatDurationHMS(sb, stats.totalElapsedTime));
    }

    /* access modifiers changed from: private */
    public String zeroToEmpty(int value) {
        return value != 0 ? Integer.toString(value) : "";
    }

    private void dumpTimeSec(PrintWriter pw, long time) {
        pw.print(time / 1000);
        pw.print('.');
        pw.print((time / 100) % 10);
        pw.print('s');
    }

    private void dumpDayStatistic(PrintWriter pw, SyncStorageEngine.DayStats ds) {
        pw.print("Success (");
        pw.print(ds.successCount);
        if (ds.successCount > 0) {
            pw.print(" for ");
            dumpTimeSec(pw, ds.successTime);
            pw.print(" avg=");
            dumpTimeSec(pw, ds.successTime / ((long) ds.successCount));
        }
        pw.print(") Failure (");
        pw.print(ds.failureCount);
        if (ds.failureCount > 0) {
            pw.print(" for ");
            dumpTimeSec(pw, ds.failureTime);
            pw.print(" avg=");
            dumpTimeSec(pw, ds.failureTime / ((long) ds.failureCount));
        }
        pw.println(")");
    }

    /* access modifiers changed from: protected */
    public void dumpSyncHistory(PrintWriter pw) {
        dumpRecentHistory(pw);
        dumpDayStatistics(pw);
    }

    /* JADX INFO: Multiple debug info for r4v5 long: [D('eventTime' long), D('N' int)] */
    /* JADX INFO: Multiple debug info for r12v8 long: [D('N' int), D('elapsedTime' long)] */
    /* JADX INFO: Multiple debug info for r3v22 char[]: [D('authorityMap' java.util.Map<java.lang.String, com.android.server.content.SyncManager$AuthoritySyncStats>), D('chars' char[])] */
    /* JADX INFO: Multiple debug info for r11v15 java.lang.Object: [D('maxLength' int), D('name' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r8v11 long: [D('N' int), D('elapsedTime' long)] */
    /* JADX INFO: Multiple debug info for r2v42 long: [D('authoritySyncStats' com.android.server.content.SyncManager$AuthoritySyncStats), D('elapsedTime' long)] */
    /* JADX INFO: Multiple debug info for r8v20 int: [D('times' int), D('timesStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r9v20 long: [D('maxAuthority' int), D('elapsedTime' long)] */
    private void dumpRecentHistory(PrintWriter pw) {
        int maxAccount;
        int maxAuthority;
        int N;
        String str;
        String str2;
        Map<String, Long> lastTimeMap;
        String str3;
        String authorityName;
        String accountKey;
        int maxAccount2;
        int maxAuthority2;
        String accountKey2;
        String diffString;
        String authorityName2;
        String str4;
        String str5;
        String diffString2;
        Map<String, Long> lastTimeMap2;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items;
        String authorityName3;
        String accountKey3;
        long totalElapsedTime;
        AuthoritySyncStats authoritySyncStats;
        SyncManager syncManager = this;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items2 = syncManager.mSyncStorageEngine.getSyncHistory();
        if (items2 != null && items2.size() > 0) {
            Map<String, AuthoritySyncStats> authorityMap = Maps.newHashMap();
            long totalElapsedTime2 = 0;
            long totalTimes = 0;
            int N2 = items2.size();
            int maxAuthority3 = 0;
            int maxAccount3 = 0;
            Iterator<SyncStorageEngine.SyncHistoryItem> it = items2.iterator();
            while (it.hasNext()) {
                SyncStorageEngine.SyncHistoryItem item = it.next();
                SyncStorageEngine.AuthorityInfo authorityInfo = syncManager.mSyncStorageEngine.getAuthority(item.authorityId);
                if (authorityInfo != null) {
                    String authorityName4 = authorityInfo.target.provider;
                    StringBuilder sb = new StringBuilder();
                    items = items2;
                    sb.append(authorityInfo.target.account.name);
                    sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                    sb.append(authorityInfo.target.account.type);
                    sb.append(" u");
                    sb.append(authorityInfo.target.userId);
                    accountKey3 = sb.toString();
                    authorityName3 = authorityName4;
                } else {
                    items = items2;
                    authorityName3 = "Unknown";
                    accountKey3 = "Unknown";
                }
                int length = authorityName3.length();
                if (length > maxAuthority3) {
                    maxAuthority3 = length;
                }
                int length2 = accountKey3.length();
                if (length2 > maxAccount3) {
                    maxAccount3 = length2;
                }
                long elapsedTime = item.elapsedTime;
                long totalElapsedTime3 = totalElapsedTime2 + elapsedTime;
                long totalTimes2 = totalTimes + 1;
                AuthoritySyncStats authoritySyncStats2 = authorityMap.get(authorityName3);
                if (authoritySyncStats2 == null) {
                    totalElapsedTime = totalElapsedTime3;
                    authoritySyncStats = new AuthoritySyncStats(authorityName3);
                    authorityMap.put(authorityName3, authoritySyncStats);
                } else {
                    totalElapsedTime = totalElapsedTime3;
                    authoritySyncStats = authoritySyncStats2;
                }
                authoritySyncStats.elapsedTime += elapsedTime;
                authoritySyncStats.times++;
                Map<String, AccountSyncStats> accountMap = authoritySyncStats.accountMap;
                AccountSyncStats accountSyncStats = accountMap.get(accountKey3);
                if (accountSyncStats == null) {
                    accountSyncStats = new AccountSyncStats(accountKey3);
                    accountMap.put(accountKey3, accountSyncStats);
                }
                accountSyncStats.elapsedTime += elapsedTime;
                accountSyncStats.times++;
                maxAuthority3 = maxAuthority3;
                it = it;
                maxAccount3 = maxAccount3;
                items2 = items;
                totalElapsedTime2 = totalElapsedTime;
                totalTimes = totalTimes2;
            }
            ArrayList<SyncStorageEngine.SyncHistoryItem> items3 = items2;
            if (totalElapsedTime2 > 0) {
                pw.println();
                pw.printf("Detailed Statistics (Recent history):  %d (# of times) %ds (sync time)\n", Long.valueOf(totalTimes), Long.valueOf(totalElapsedTime2 / 1000));
                List<AuthoritySyncStats> sortedAuthorities = new ArrayList<>(authorityMap.values());
                Collections.sort(sortedAuthorities, new Comparator<AuthoritySyncStats>() {
                    /* class com.android.server.content.SyncManager.AnonymousClass14 */

                    public int compare(AuthoritySyncStats lhs, AuthoritySyncStats rhs) {
                        int compare = Integer.compare(rhs.times, lhs.times);
                        if (compare == 0) {
                            return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                        }
                        return compare;
                    }
                });
                int maxLength = Math.max(maxAuthority3, maxAccount3 + 3);
                char[] chars = new char[(maxLength + 4 + 2 + 10 + 11)];
                Arrays.fill(chars, '-');
                String timeStr = new String(chars);
                str2 = " u";
                String accountFormat = String.format("  %%-%ds: %%-9s  %%-11s\n", Integer.valueOf(maxLength + 2));
                str = SliceClientPermissions.SliceAuthority.DELIMITER;
                String accountFormat2 = String.format("    %%-%ds:   %%-9s  %%-11s\n", Integer.valueOf(maxLength));
                pw.println(timeStr);
                Iterator<AuthoritySyncStats> it2 = sortedAuthorities.iterator();
                while (it2.hasNext()) {
                    AuthoritySyncStats authoritySyncStats3 = it2.next();
                    String name = authoritySyncStats3.name;
                    long elapsedTime2 = authoritySyncStats3.elapsedTime;
                    int times = authoritySyncStats3.times;
                    String timeStr2 = String.format("%ds/%d%%", Long.valueOf(elapsedTime2 / 1000), Long.valueOf((elapsedTime2 * 100) / totalElapsedTime2));
                    String format = String.format("%d/%d%%", Integer.valueOf(times), Long.valueOf(((long) (times * 100)) / totalTimes));
                    pw.printf(accountFormat, name, format, timeStr2);
                    List<AccountSyncStats> sortedAccounts = new ArrayList<>(authoritySyncStats3.accountMap.values());
                    Collections.sort(sortedAccounts, new Comparator<AccountSyncStats>() {
                        /* class com.android.server.content.SyncManager.AnonymousClass15 */

                        public int compare(AccountSyncStats lhs, AccountSyncStats rhs) {
                            int compare = Integer.compare(rhs.times, lhs.times);
                            if (compare == 0) {
                                return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                            }
                            return compare;
                        }
                    });
                    for (AccountSyncStats stats : sortedAccounts) {
                        long elapsedTime3 = stats.elapsedTime;
                        int times2 = stats.times;
                        String timeStr3 = String.format("%ds/%d%%", Long.valueOf(elapsedTime3 / 1000), Long.valueOf((elapsedTime3 * 100) / totalElapsedTime2));
                        String timesStr = String.format("%d/%d%%", Integer.valueOf(times2), Long.valueOf(((long) (times2 * 100)) / totalTimes));
                        pw.printf(accountFormat2, "XXXXXXXXX", timesStr, timeStr3);
                        accountFormat = accountFormat;
                        sortedAccounts = sortedAccounts;
                        name = name;
                        format = timesStr;
                        authoritySyncStats3 = authoritySyncStats3;
                    }
                    pw.println(timeStr);
                    timeStr = timeStr;
                    accountFormat2 = accountFormat2;
                    maxLength = maxLength;
                    sortedAuthorities = sortedAuthorities;
                    N2 = N2;
                    maxAuthority3 = maxAuthority3;
                    it2 = it2;
                    maxAccount3 = maxAccount3;
                    accountFormat = accountFormat;
                }
                N = N2;
                maxAuthority = maxAuthority3;
                maxAccount = maxAccount3;
            } else {
                N = N2;
                maxAuthority = maxAuthority3;
                maxAccount = maxAccount3;
                str2 = " u";
                str = SliceClientPermissions.SliceAuthority.DELIMITER;
            }
            pw.println();
            pw.println("Recent Sync History");
            String str6 = "(SERVER is now split up to FEED and OTHER)";
            pw.println(str6);
            StringBuilder sb2 = new StringBuilder();
            sb2.append("  %-");
            int maxAccount4 = maxAccount;
            sb2.append(maxAccount4);
            sb2.append("s  %-");
            int maxAuthority4 = maxAuthority;
            sb2.append(maxAuthority4);
            sb2.append("s %s\n");
            String format2 = sb2.toString();
            Map<String, Long> lastTimeMap3 = Maps.newHashMap();
            PackageManager pm = syncManager.mContext.getPackageManager();
            int i = 0;
            while (i < N) {
                SyncStorageEngine.SyncHistoryItem item2 = items3.get(i);
                SyncStorageEngine.AuthorityInfo authorityInfo2 = syncManager.mSyncStorageEngine.getAuthority(item2.authorityId);
                if (authorityInfo2 != null) {
                    String authorityName5 = authorityInfo2.target.provider;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(authorityInfo2.target.account.name);
                    str4 = str;
                    sb3.append(str4);
                    maxAccount2 = maxAccount4;
                    sb3.append(authorityInfo2.target.account.type);
                    diffString = str2;
                    sb3.append(diffString);
                    maxAuthority2 = maxAuthority4;
                    sb3.append(authorityInfo2.target.userId);
                    accountKey2 = sb3.toString();
                    authorityName2 = authorityName5;
                } else {
                    maxAccount2 = maxAccount4;
                    maxAuthority2 = maxAuthority4;
                    diffString = str2;
                    str4 = str;
                    authorityName2 = "Unknown";
                    accountKey2 = "Unknown";
                }
                N = N;
                items3 = items3;
                long elapsedTime4 = item2.elapsedTime;
                Time time = new Time();
                long eventTime = item2.eventTime;
                time.set(eventTime);
                String key = authorityName2 + str4 + accountKey2;
                Long lastEventTime = lastTimeMap3.get(key);
                if (lastEventTime == null) {
                    diffString2 = "";
                    str5 = diffString;
                } else {
                    long diff = (lastEventTime.longValue() - eventTime) / 1000;
                    if (diff < 60) {
                        str5 = diffString;
                        diffString2 = String.valueOf(diff);
                    } else if (diff < 3600) {
                        str5 = diffString;
                        diffString2 = String.format("%02d:%02d", Long.valueOf(diff / 60), Long.valueOf(diff % 60));
                    } else {
                        str5 = diffString;
                        long sec = diff % 3600;
                        diffString2 = String.format("%02d:%02d:%02d", Long.valueOf(diff / 3600), Long.valueOf(sec / 60), Long.valueOf(sec % 60));
                    }
                }
                lastTimeMap3.put(key, Long.valueOf(eventTime));
                pw.printf("  #%-3d: %s %8s  %5.1fs  %8s", Integer.valueOf(i + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source], Float.valueOf(((float) elapsedTime4) / 1000.0f), diffString2);
                pw.printf(format2, "XXXXXXXXX", authorityName2, SyncOperation.reasonToString(pm, item2.reason));
                if (item2.event == 1) {
                    if (item2.upstreamActivity == 0 && item2.downstreamActivity == 0) {
                        lastTimeMap2 = lastTimeMap3;
                        if (item2.mesg != null && !SyncStorageEngine.MESG_SUCCESS.equals(item2.mesg)) {
                            pw.printf("    mesg=%s\n", item2.mesg);
                        }
                        i++;
                        lastTimeMap3 = lastTimeMap2;
                        str = str4;
                        format2 = format2;
                        totalElapsedTime2 = totalElapsedTime2;
                        str6 = str6;
                        totalTimes = totalTimes;
                        maxAuthority4 = maxAuthority2;
                        maxAccount4 = maxAccount2;
                        str2 = str5;
                    }
                }
                lastTimeMap2 = lastTimeMap3;
                pw.printf("    event=%d upstreamActivity=%d downstreamActivity=%d\n", Integer.valueOf(item2.event), Long.valueOf(item2.upstreamActivity), Long.valueOf(item2.downstreamActivity));
                pw.printf("    mesg=%s\n", item2.mesg);
                i++;
                lastTimeMap3 = lastTimeMap2;
                str = str4;
                format2 = format2;
                totalElapsedTime2 = totalElapsedTime2;
                str6 = str6;
                totalTimes = totalTimes;
                maxAuthority4 = maxAuthority2;
                maxAccount4 = maxAccount2;
                str2 = str5;
            }
            int N3 = N;
            String str7 = str2;
            Map<String, Long> lastTimeMap4 = lastTimeMap3;
            pw.println();
            pw.println("Recent Sync History Extras");
            pw.println(str6);
            int i2 = 0;
            while (i2 < N3) {
                SyncStorageEngine.SyncHistoryItem item3 = items3.get(i2);
                Bundle extras = item3.extras;
                if (extras == null) {
                    lastTimeMap = lastTimeMap4;
                    N3 = N3;
                    items3 = items3;
                    str3 = str7;
                } else if (extras.size() == 0) {
                    lastTimeMap = lastTimeMap4;
                    N3 = N3;
                    items3 = items3;
                    str3 = str7;
                } else {
                    SyncStorageEngine.AuthorityInfo authorityInfo3 = syncManager.mSyncStorageEngine.getAuthority(item3.authorityId);
                    if (authorityInfo3 != null) {
                        authorityName = authorityInfo3.target.provider;
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(authorityInfo3.target.account.name);
                        sb4.append(str);
                        sb4.append(authorityInfo3.target.account.type);
                        str3 = str7;
                        sb4.append(str3);
                        sb4.append(authorityInfo3.target.userId);
                        accountKey = sb4.toString();
                    } else {
                        str3 = str7;
                        authorityName = "Unknown";
                        accountKey = "Unknown";
                    }
                    Time time2 = new Time();
                    N3 = N3;
                    items3 = items3;
                    long eventTime2 = item3.eventTime;
                    time2.set(eventTime2);
                    lastTimeMap = lastTimeMap4;
                    pw.printf("  #%-3d: %s %8s ", Integer.valueOf(i2 + 1), formatTime(eventTime2), SyncStorageEngine.SOURCES[item3.source]);
                    pw.printf(format2, "XXXXXXXXX", authorityName, extras);
                }
                i2++;
                syncManager = this;
                str7 = str3;
                lastTimeMap4 = lastTimeMap;
            }
        }
    }

    private void dumpDayStatistics(PrintWriter pw) {
        SyncStorageEngine.DayStats ds;
        int delta;
        SyncStorageEngine.DayStats[] dses = this.mSyncStorageEngine.getDayStatistics();
        if (dses != null && dses[0] != null) {
            pw.println();
            pw.println("Sync Statistics");
            pw.print("  Today:  ");
            dumpDayStatistic(pw, dses[0]);
            int today = dses[0].day;
            int i = 1;
            while (i <= 6 && i < dses.length && (ds = dses[i]) != null && (delta = today - ds.day) <= 6) {
                pw.print("  Day-");
                pw.print(delta);
                pw.print(":  ");
                dumpDayStatistic(pw, ds);
                i++;
            }
            int weekDay = today;
            while (i < dses.length) {
                SyncStorageEngine.DayStats aggr = null;
                weekDay -= 7;
                while (true) {
                    if (i >= dses.length) {
                        break;
                    }
                    SyncStorageEngine.DayStats ds2 = dses[i];
                    if (ds2 == null) {
                        i = dses.length;
                        break;
                    } else if (weekDay - ds2.day > 6) {
                        break;
                    } else {
                        i++;
                        if (aggr == null) {
                            aggr = new SyncStorageEngine.DayStats(weekDay);
                        }
                        aggr.successCount += ds2.successCount;
                        aggr.successTime += ds2.successTime;
                        aggr.failureCount += ds2.failureCount;
                        aggr.failureTime += ds2.failureTime;
                    }
                }
                if (aggr != null) {
                    pw.print("  Week-");
                    pw.print((today - weekDay) / 7);
                    pw.print(": ");
                    dumpDayStatistic(pw, aggr);
                }
            }
        }
    }

    private void dumpSyncAdapters(IndentingPrintWriter pw) {
        pw.println();
        List<UserInfo> users = getAllUsers();
        if (users != null) {
            for (UserInfo user : users) {
                pw.println("Sync adapters for " + user + ":");
                pw.increaseIndent();
                for (RegisteredServicesCache.ServiceInfo<?> info : this.mSyncAdapters.getAllServices(user.id)) {
                    pw.println(info);
                }
                pw.decreaseIndent();
                pw.println();
            }
        }
    }

    /* access modifiers changed from: private */
    public static class AuthoritySyncStats {
        Map<String, AccountSyncStats> accountMap;
        long elapsedTime;
        String name;
        int times;

        private AuthoritySyncStats(String name2) {
            this.accountMap = Maps.newHashMap();
            this.name = name2;
        }
    }

    /* access modifiers changed from: private */
    public static class AccountSyncStats {
        long elapsedTime;
        String name;
        int times;

        private AccountSyncStats(String name2) {
            this.name = name2;
        }
    }

    static void sendOnUnsyncableAccount(Context context, RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo, int userId, OnReadyCallback onReadyCallback) {
        OnUnsyncableAccountCheck connection = new OnUnsyncableAccountCheck(syncAdapterInfo, onReadyCallback);
        if (context.bindServiceAsUser(getAdapterBindIntent(context, syncAdapterInfo.componentName, userId), connection, 21, UserHandle.of(userId))) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(context, connection) {
                /* class com.android.server.content.$$Lambda$SyncManager$zZUXjdGLFQgHtMQ3vq0EWHvir8 */
                private final /* synthetic */ Context f$0;
                private final /* synthetic */ SyncManager.OnUnsyncableAccountCheck f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.unbindService(this.f$1);
                }
            }, 5000);
        } else {
            connection.onReady();
        }
    }

    /* access modifiers changed from: private */
    public static class OnUnsyncableAccountCheck implements ServiceConnection {
        static final long SERVICE_BOUND_TIME_MILLIS = 5000;
        private final OnReadyCallback mOnReadyCallback;
        private final RegisteredServicesCache.ServiceInfo<SyncAdapterType> mSyncAdapterInfo;

        OnUnsyncableAccountCheck(RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo, OnReadyCallback onReadyCallback) {
            this.mSyncAdapterInfo = syncAdapterInfo;
            this.mOnReadyCallback = onReadyCallback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onReady() {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mOnReadyCallback.onReady();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                ISyncAdapter.Stub.asInterface(service).onUnsyncableAccount(new ISyncAdapterUnsyncableAccountCallback.Stub() {
                    /* class com.android.server.content.SyncManager.OnUnsyncableAccountCheck.AnonymousClass1 */

                    public void onUnsyncableAccountDone(boolean isReady) {
                        if (isReady) {
                            OnUnsyncableAccountCheck.this.onReady();
                        }
                    }
                });
            } catch (RemoteException e) {
                Slog.e("SyncManager", "Could not call onUnsyncableAccountDone " + this.mSyncAdapterInfo, e);
                onReady();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    /* access modifiers changed from: private */
    public class SyncTimeTracker {
        boolean mLastWasSyncing;
        private long mTimeSpentSyncing;
        long mWhenSyncStarted;

        private SyncTimeTracker() {
            this.mLastWasSyncing = false;
            this.mWhenSyncStarted = 0;
        }

        public synchronized void update() {
            boolean isSyncInProgress = !SyncManager.this.mActiveSyncContexts.isEmpty();
            if (isSyncInProgress != this.mLastWasSyncing) {
                long now = SystemClock.elapsedRealtime();
                if (isSyncInProgress) {
                    this.mWhenSyncStarted = now;
                } else {
                    this.mTimeSpentSyncing += now - this.mWhenSyncStarted;
                }
                this.mLastWasSyncing = isSyncInProgress;
            }
        }

        public synchronized long timeSpentSyncing() {
            if (!this.mLastWasSyncing) {
                return this.mTimeSpentSyncing;
            }
            return this.mTimeSpentSyncing + (SystemClock.elapsedRealtime() - this.mWhenSyncStarted);
        }
    }

    /* access modifiers changed from: package-private */
    public class ServiceConnectionData {
        public final ActiveSyncContext activeSyncContext;
        public final IBinder adapter;

        ServiceConnectionData(ActiveSyncContext activeSyncContext2, IBinder adapter2) {
            this.activeSyncContext = activeSyncContext2;
            this.adapter = adapter2;
        }
    }

    private static SyncManager getInstance() {
        SyncManager syncManager;
        synchronized (SyncManager.class) {
            if (sInstance == null) {
                Slog.wtf("SyncManager", "sInstance == null");
            }
            syncManager = sInstance;
        }
        return syncManager;
    }

    public static boolean readyToSync(int userId) {
        SyncManager instance = getInstance();
        return instance != null && SyncJobService.isReady() && instance.mProvisioned && instance.isUserUnlocked(userId);
    }

    public static void sendMessage(Message message) {
        SyncManager instance = getInstance();
        if (instance != null) {
            instance.mSyncHandler.sendMessage(message);
        }
    }

    /* access modifiers changed from: package-private */
    public class SyncHandler extends Handler {
        private static final int MESSAGE_ACCOUNTS_UPDATED = 9;
        private static final int MESSAGE_CANCEL = 6;
        private static final int MESSAGE_MONITOR_SYNC = 8;
        static final int MESSAGE_REMOVE_PERIODIC_SYNC = 14;
        static final int MESSAGE_SCHEDULE_SYNC = 12;
        private static final int MESSAGE_SERVICE_CONNECTED = 4;
        private static final int MESSAGE_SERVICE_DISCONNECTED = 5;
        static final int MESSAGE_START_SYNC = 10;
        static final int MESSAGE_STOP_SYNC = 11;
        private static final int MESSAGE_SYNC_FINISHED = 1;
        static final int MESSAGE_UPDATE_PERIODIC_SYNC = 13;
        public final SyncTimeTracker mSyncTimeTracker = new SyncTimeTracker();
        private final HashMap<String, PowerManager.WakeLock> mWakeLocks = Maps.newHashMap();

        public SyncHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SyncManager.this.mSyncManagerWakeLock.acquire();
            try {
                handleSyncMessage(msg);
            } finally {
                SyncManager.this.mSyncManagerWakeLock.release();
            }
        }

        private void handleSyncMessage(Message msg) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            try {
                SyncManager.this.mDataConnectionIsConnected = SyncManager.this.readDataConnectionState();
                int i = msg.what;
                boolean applyBackoff = true;
                if (i == 1) {
                    SyncFinishedOrCancelledMessagePayload payload = (SyncFinishedOrCancelledMessagePayload) msg.obj;
                    if (!SyncManager.this.isSyncStillActiveH(payload.activeSyncContext)) {
                        Log.d("SyncManager", "handleSyncHandlerMessage: dropping since the sync is no longer active: " + payload.activeSyncContext);
                    } else {
                        Slog.i("SyncManager", "syncFinished" + payload.activeSyncContext.mSyncOperation);
                        SyncJobService.callJobFinished(payload.activeSyncContext.mSyncOperation.jobId, false, "sync finished");
                        runSyncFinishedOrCanceledH(payload.syncResult, payload.activeSyncContext);
                    }
                } else if (i == 4) {
                    ServiceConnectionData msgData = (ServiceConnectionData) msg.obj;
                    Log.i("SyncManager", "handleSyncHandlerMessage: MESSAGE_SERVICE_CONNECTED: " + msgData.activeSyncContext);
                    if (SyncManager.this.isSyncStillActiveH(msgData.activeSyncContext)) {
                        runBoundToAdapterH(msgData.activeSyncContext, msgData.adapter);
                    } else {
                        Slog.i("SyncManager", "old message, isSyncStillActiveH return.");
                    }
                } else if (i == 5) {
                    ActiveSyncContext currentSyncContext = ((ServiceConnectionData) msg.obj).activeSyncContext;
                    Log.i("SyncManager", "handleSyncHandlerMessage: MESSAGE_SERVICE_DISCONNECTED: " + currentSyncContext);
                    if (SyncManager.this.isSyncStillActiveH(currentSyncContext)) {
                        try {
                            if (currentSyncContext.mSyncAdapter != null) {
                                SyncManager.this.mLogger.log("Calling cancelSync for SERVICE_DISCONNECTED ", currentSyncContext, " adapter=", currentSyncContext.mSyncAdapter);
                                currentSyncContext.mSyncAdapter.cancelSync(currentSyncContext);
                                SyncManager.this.mLogger.log("Canceled");
                            }
                        } catch (RemoteException e) {
                            SyncManager.this.mLogger.log("RemoteException ", Log.getStackTraceString(e));
                        }
                        SyncResult syncResult = new SyncResult();
                        syncResult.stats.numIoExceptions++;
                        SyncJobService.callJobFinished(currentSyncContext.mSyncOperation.jobId, false, "service disconnected");
                        runSyncFinishedOrCanceledH(syncResult, currentSyncContext);
                    }
                } else if (i != 6) {
                    switch (i) {
                        case 8:
                            ActiveSyncContext monitoredSyncContext = (ActiveSyncContext) msg.obj;
                            if (isLoggable) {
                                Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_MONITOR_SYNC: " + monitoredSyncContext.mSyncOperation.target);
                            }
                            if (!isSyncNotUsingNetworkH(monitoredSyncContext)) {
                                SyncManager.this.postMonitorSyncProgressMessage(monitoredSyncContext);
                                break;
                            } else {
                                Log.w("SyncManager", String.format("Detected sync making no progress for %s. cancelling.", SyncLogger.logSafe(monitoredSyncContext)));
                                SyncJobService.callJobFinished(monitoredSyncContext.mSyncOperation.jobId, false, "no network activity");
                                runSyncFinishedOrCanceledH(null, monitoredSyncContext);
                                break;
                            }
                        case 9:
                            if (Log.isLoggable("SyncManager", 2)) {
                                Slog.v("SyncManager", "handleSyncHandlerMessage: MESSAGE_ACCOUNTS_UPDATED");
                            }
                            updateRunningAccountsH((SyncStorageEngine.EndPoint) msg.obj);
                            break;
                        case 10:
                            startSyncH((SyncOperation) msg.obj);
                            break;
                        case 11:
                            SyncOperation op = (SyncOperation) msg.obj;
                            if (isLoggable) {
                                Slog.v("SyncManager", "Stop sync received.");
                            }
                            ActiveSyncContext asc = findActiveSyncContextH(op.jobId);
                            if (asc != null) {
                                runSyncFinishedOrCanceledH(null, asc);
                                boolean reschedule = msg.arg1 != 0;
                                if (msg.arg2 == 0) {
                                    applyBackoff = false;
                                }
                                Slog.d("SyncManager", "Stopping sync. Reschedule: " + reschedule + "Backoff: " + applyBackoff);
                                if (applyBackoff) {
                                    SyncManager.this.increaseBackoffSetting(op.target);
                                }
                                if (reschedule) {
                                    deferStoppedSyncH(op, 0);
                                }
                                break;
                            }
                            break;
                        case 12:
                            ScheduleSyncMessagePayload syncPayload = (ScheduleSyncMessagePayload) msg.obj;
                            SyncManager.this.scheduleSyncOperationH(syncPayload.syncOperation, syncPayload.minDelayMillis);
                            break;
                        case 13:
                            UpdatePeriodicSyncMessagePayload data = (UpdatePeriodicSyncMessagePayload) msg.obj;
                            updateOrAddPeriodicSyncH(data.target, data.pollFrequency, data.flex, data.extras);
                            break;
                        case 14:
                            Pair<SyncStorageEngine.EndPoint, String> args = (Pair) msg.obj;
                            removePeriodicSyncH((SyncStorageEngine.EndPoint) args.first, msg.getData(), (String) args.second);
                            break;
                    }
                } else {
                    SyncStorageEngine.EndPoint endpoint = (SyncStorageEngine.EndPoint) msg.obj;
                    Bundle extras = msg.peekData();
                    if (isLoggable) {
                        Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_CANCEL: " + endpoint + " bundle: " + extras);
                    }
                    cancelActiveSyncH(endpoint, extras, "MESSAGE_CANCEL");
                }
            } finally {
                this.mSyncTimeTracker.update();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private PowerManager.WakeLock getSyncWakeLock(SyncOperation operation) {
            String wakeLockKey = operation.wakeLockName();
            PowerManager.WakeLock wakeLock = this.mWakeLocks.get(wakeLockKey);
            if (wakeLock != null) {
                return wakeLock;
            }
            PowerManager.WakeLock wakeLock2 = SyncManager.this.mPowerManager.newWakeLock(1, SyncManager.SYNC_WAKE_LOCK_PREFIX + wakeLockKey);
            wakeLock2.setReferenceCounted(false);
            this.mWakeLocks.put(wakeLockKey, wakeLock2);
            return wakeLock2;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001a: APUT  (r1v1 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r3v2 java.lang.String) */
        private void deferSyncH(SyncOperation op, long delay, String why) {
            SyncLogger syncLogger = SyncManager.this.mLogger;
            Object[] objArr = new Object[8];
            objArr[0] = "deferSyncH() ";
            objArr[1] = op.isPeriodic ? "periodic " : "";
            objArr[2] = "sync.  op=";
            objArr[3] = op;
            objArr[4] = " delay=";
            objArr[5] = Long.valueOf(delay);
            objArr[6] = " why=";
            objArr[7] = why;
            syncLogger.log(objArr);
            SyncJobService.callJobFinished(op.jobId, false, why);
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
                return;
            }
            SyncManager.this.cancelJob(op, "deferSyncH()");
            SyncManager.this.scheduleSyncOperationH(op, delay);
        }

        private void deferStoppedSyncH(SyncOperation op, long delay) {
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
            } else {
                SyncManager.this.scheduleSyncOperationH(op, delay);
            }
        }

        private void deferActiveSyncH(ActiveSyncContext asc, String why) {
            SyncOperation op = asc.mSyncOperation;
            runSyncFinishedOrCanceledH(null, asc);
            deferSyncH(op, 10000, why);
        }

        private void startSyncH(SyncOperation op) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            if (isLoggable) {
                Slog.v("SyncManager", op.toString());
            }
            SyncManager.this.mSyncStorageEngine.setClockValid();
            SyncJobService.markSyncStarted(op.jobId);
            if (SyncManager.this.mStorageIsLow) {
                deferSyncH(op, 3600000, "storage low");
                return;
            }
            if (op.isPeriodic) {
                for (SyncOperation syncOperation : SyncManager.this.getAllPendingSyncs()) {
                    if (syncOperation.sourcePeriodicId == op.jobId) {
                        SyncJobService.callJobFinished(op.jobId, false, "periodic sync, pending");
                        return;
                    }
                }
                Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
                while (it.hasNext()) {
                    if (it.next().mSyncOperation.sourcePeriodicId == op.jobId) {
                        SyncJobService.callJobFinished(op.jobId, false, "periodic sync, already running");
                        return;
                    }
                }
                if (SyncManager.this.isAdapterDelayed(op.target)) {
                    deferSyncH(op, 0, "backing off");
                    return;
                }
            }
            Iterator<ActiveSyncContext> it2 = SyncManager.this.mActiveSyncContexts.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ActiveSyncContext asc = it2.next();
                if (asc.mSyncOperation.isConflict(op)) {
                    if (asc.mSyncOperation.findPriority() >= op.findPriority()) {
                        if (isLoggable) {
                            Slog.v("SyncManager", "Rescheduling sync due to conflict " + op.toString());
                        }
                        deferSyncH(op, 10000, "delay on conflict");
                        return;
                    }
                    if (isLoggable) {
                        Slog.v("SyncManager", "Pushing back running sync due to a higher priority sync");
                    }
                    deferActiveSyncH(asc, "preempted");
                }
            }
            int syncOpState = computeSyncOpState(op);
            if (syncOpState == 1 || syncOpState == 2) {
                int i = op.jobId;
                SyncJobService.callJobFinished(i, false, "invalid op state: " + syncOpState);
                return;
            }
            if (!dispatchSyncOperation(op)) {
                SyncJobService.callJobFinished(op.jobId, false, "dispatchSyncOperation() failed");
            }
            SyncManager.this.setAuthorityPendingState(op.target);
        }

        private ActiveSyncContext findActiveSyncContextH(int jobId) {
            Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
            while (it.hasNext()) {
                ActiveSyncContext asc = it.next();
                SyncOperation op = asc.mSyncOperation;
                if (op != null && op.jobId == jobId) {
                    return asc;
                }
            }
            return null;
        }

        private void updateRunningAccountsH(SyncStorageEngine.EndPoint syncTargets) {
            AccountAndUser[] oldAccounts = SyncManager.this.mRunningAccounts;
            SyncManager.this.mRunningAccounts = AccountManagerService.getSingleton().getRunningAccounts();
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Accounts list: ");
                for (AccountAndUser acc : SyncManager.this.mRunningAccounts) {
                    Slog.v("SyncManager", acc.toString());
                }
            }
            if (SyncManager.this.mLogger.enabled()) {
                SyncManager.this.mLogger.log("updateRunningAccountsH: ", Arrays.toString(SyncManager.this.mRunningAccounts));
            }
            SyncManager.this.removeStaleAccounts();
            AccountAndUser[] accounts = SyncManager.this.mRunningAccounts;
            Iterator<ActiveSyncContext> it = new ArrayList<>(SyncManager.this.mActiveSyncContexts).iterator();
            while (it.hasNext()) {
                ActiveSyncContext currentSyncContext = it.next();
                if (!SyncManager.this.containsAccountAndUser(accounts, currentSyncContext.mSyncOperation.target.account, currentSyncContext.mSyncOperation.target.userId)) {
                    Log.d("SyncManager", "canceling sync since the account is no longer running");
                    SyncManager.this.sendSyncFinishedOrCanceledMessage(currentSyncContext, null);
                }
            }
            if (syncTargets != null) {
                AccountAndUser[] accountAndUserArr = SyncManager.this.mRunningAccounts;
                int length = accountAndUserArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    AccountAndUser aau = accountAndUserArr[i];
                    if (!SyncManager.this.containsAccountAndUser(oldAccounts, aau.account, aau.userId)) {
                        if (Log.isLoggable("SyncManager", 3)) {
                            Log.d("SyncManager", "Account " + aau.account + " added, checking sync restore data");
                        }
                        AccountSyncSettingsBackupHelper.accountAdded(SyncManager.this.mContext, syncTargets.userId);
                    } else {
                        i++;
                    }
                }
            }
            AccountAndUser[] allAccounts = AccountManagerService.getSingleton().getAllAccounts();
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (!SyncManager.this.containsAccountAndUser(allAccounts, op.target.account, op.target.userId)) {
                    SyncManager.this.mLogger.log("canceling: ", op);
                    SyncManager.this.cancelJob(op, "updateRunningAccountsH()");
                }
            }
            if (syncTargets != null) {
                SyncManager.this.scheduleSync(syncTargets.account, syncTargets.userId, -2, syncTargets.provider, null, -1, 0, Process.myUid(), -4, null);
            }
        }

        private void maybeUpdateSyncPeriodH(SyncOperation syncOperation, long pollFrequencyMillis, long flexMillis) {
            if (pollFrequencyMillis != syncOperation.periodMillis || flexMillis != syncOperation.flexMillis) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "updating period " + syncOperation + " to " + pollFrequencyMillis + " and flex to " + flexMillis);
                }
                SyncOperation newOp = new SyncOperation(syncOperation, pollFrequencyMillis, flexMillis);
                newOp.jobId = syncOperation.jobId;
                SyncManager.this.scheduleSyncOperationH(newOp);
            }
        }

        private void updateOrAddPeriodicSyncH(SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras) {
            SyncOperation op;
            int syncOpState;
            if (target.account != null) {
                boolean isLoggable = Log.isLoggable("SyncManager", 2);
                SyncManager.this.verifyJobScheduler();
                long pollFrequencyMillis = pollFrequency * 1000;
                long flexMillis = flex * 1000;
                if (isLoggable) {
                    Slog.v("SyncManager", "Addition to periodic syncs requested: " + target + " period: " + pollFrequency + " flexMillis: " + flex + " extras: " + extras.toString());
                }
                for (SyncOperation op2 : SyncManager.this.getAllPendingSyncs()) {
                    if (op2.isPeriodic && op2.target.matchesSpec(target)) {
                        if (SyncManager.syncExtrasEquals(op2.extras, extras, true)) {
                            maybeUpdateSyncPeriodH(op2, pollFrequencyMillis, flexMillis);
                            return;
                        }
                    }
                }
                if (isLoggable) {
                    Slog.v("SyncManager", "Adding new periodic sync: " + target + " period: " + pollFrequency + " flexMillis: " + flex + " extras: " + extras.toString());
                }
                RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(target.provider, target.account.type), target.userId);
                if (!(syncAdapterInfo == null || (syncOpState = computeSyncOpState((op = new SyncOperation(target, syncAdapterInfo.uid, syncAdapterInfo.componentName.getPackageName(), -4, 4, extras, ((SyncAdapterType) syncAdapterInfo.type).allowParallelSyncs(), true, -1, pollFrequencyMillis, flexMillis, 0)))) == 1)) {
                    if (syncOpState != 2) {
                        SyncManager.this.scheduleSyncOperationH(op);
                        SyncManager.this.mSyncStorageEngine.reportChange(1, target.userId);
                        return;
                    }
                    String packageName = op.owningPackage;
                    int userId = UserHandle.getUserId(op.owningUid);
                    if (SyncManager.this.wasPackageEverLaunched(packageName, userId)) {
                        SyncManager.this.mAccountManagerInternal.requestAccountAccess(op.target.account, packageName, userId, new RemoteCallback(new RemoteCallback.OnResultListener(target, pollFrequency, flex, extras) {
                            /* class com.android.server.content.$$Lambda$SyncManager$SyncHandler$7vThHsPImW4qB6AnVEnnD3dGhM */
                            private final /* synthetic */ SyncStorageEngine.EndPoint f$1;
                            private final /* synthetic */ long f$2;
                            private final /* synthetic */ long f$3;
                            private final /* synthetic */ Bundle f$4;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r5;
                                this.f$4 = r7;
                            }

                            public final void onResult(Bundle bundle) {
                                SyncManager.SyncHandler.this.lambda$updateOrAddPeriodicSyncH$0$SyncManager$SyncHandler(this.f$1, this.f$2, this.f$3, this.f$4, bundle);
                            }
                        }));
                    }
                }
            }
        }

        public /* synthetic */ void lambda$updateOrAddPeriodicSyncH$0$SyncManager$SyncHandler(SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras, Bundle result) {
            if (result != null && result.getBoolean("booleanResult")) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        }

        private void removePeriodicSyncInternalH(SyncOperation syncOperation, String why) {
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.sourcePeriodicId == syncOperation.jobId || op.jobId == syncOperation.jobId) {
                    ActiveSyncContext asc = findActiveSyncContextH(syncOperation.jobId);
                    if (asc != null) {
                        SyncJobService.callJobFinished(syncOperation.jobId, false, "removePeriodicSyncInternalH");
                        runSyncFinishedOrCanceledH(null, asc);
                    }
                    SyncManager.this.mLogger.log("removePeriodicSyncInternalH-canceling: ", op);
                    SyncManager.this.cancelJob(op, why);
                }
            }
        }

        private void removePeriodicSyncH(SyncStorageEngine.EndPoint target, Bundle extras, String why) {
            SyncManager.this.verifyJobScheduler();
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.isPeriodic && op.target.matchesSpec(target) && SyncManager.syncExtrasEquals(op.extras, extras, true)) {
                    removePeriodicSyncInternalH(op, why);
                }
            }
        }

        private boolean isSyncNotUsingNetworkH(ActiveSyncContext activeSyncContext) {
            long deltaBytesTransferred = SyncManager.this.getTotalBytesTransferredByUid(activeSyncContext.mSyncAdapterUid) - activeSyncContext.mBytesTransferredAtLastPoll;
            if (Log.isLoggable("SyncManager", 3)) {
                long remainder = deltaBytesTransferred % 1048576;
                Log.d("SyncManager", String.format("Time since last update: %ds. Delta transferred: %dMBs,%dKBs,%dBs", Long.valueOf((SystemClock.elapsedRealtime() - activeSyncContext.mLastPolledTimeElapsed) / 1000), Long.valueOf(deltaBytesTransferred / 1048576), Long.valueOf(remainder / 1024), Long.valueOf(remainder % 1024)));
            }
            return deltaBytesTransferred <= 10;
        }

        private int computeSyncOpState(SyncOperation op) {
            NetworkInfo networkInfo;
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            SyncStorageEngine.EndPoint target = op.target;
            if (!SyncManager.this.containsAccountAndUser(SyncManager.this.mRunningAccounts, target.account, target.userId)) {
                if (isLoggable) {
                    Slog.v("SyncManager", "    Dropping sync operation: account doesn't exist.");
                }
                return 1;
            }
            int state = SyncManager.this.computeSyncable(target.account, target.userId, target.provider, true);
            if (state == 3) {
                if (isLoggable) {
                    Slog.v("SyncManager", "    Dropping sync operation: isSyncable == SYNCABLE_NO_ACCOUNT_ACCESS");
                }
                return 2;
            } else if (state == 0) {
                if (isLoggable) {
                    Slog.v("SyncManager", "    Dropping sync operation: isSyncable == NOT_SYNCABLE");
                }
                return 1;
            } else {
                boolean syncEnabled = SyncManager.this.mSyncStorageEngine.getMasterSyncAutomatically(target.userId) && SyncManager.this.mSyncStorageEngine.getSyncAutomatically(target.account, target.userId, target.provider);
                boolean ignoreSystemConfiguration = op.isIgnoreSettings() || state < 0;
                if (!syncEnabled && !ignoreSystemConfiguration) {
                    if (isLoggable) {
                        Slog.v("SyncManager", "    Dropping sync operation: disallowed by settings/network.");
                    }
                    return 1;
                } else if (HwDeviceManager.disallowOp(42) && !op.isManual() && state >= 0 && (networkInfo = SyncManager.this.getConnectivityManager().getActiveNetworkInfo()) != null && networkInfo.isRoaming() && networkInfo.getType() == 0) {
                    Slog.v("SyncManager", "    Dropping auto sync operation for " + target.provider + ": disallowed by MDM disable auto sync when roaming.");
                    return 1;
                } else if (target.account == null || !HwDeviceManager.disallowOp(25, target.account.type) || op.isManual() || state < 0) {
                    return 0;
                } else {
                    Slog.v("SyncManager", "    Dropping auto sync operation for " + target.provider + ": disallowed by MDM.");
                    return 1;
                }
            }
        }

        private boolean dispatchSyncOperation(SyncOperation op) {
            UsageStatsManagerInternal usmi;
            Slog.i("SyncManager", "dispatchSyncOperation:we are going to sync " + op);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "num active syncs: " + SyncManager.this.mActiveSyncContexts.size());
                Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
                while (it.hasNext()) {
                    Slog.v("SyncManager", it.next().toString());
                }
            }
            if (op.isAppStandbyExempted() && (usmi = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class)) != null) {
                usmi.reportExemptedSyncStart(op.owningPackage, UserHandle.getUserId(op.owningUid));
            }
            SyncStorageEngine.EndPoint info = op.target;
            SyncAdapterType syncAdapterType = SyncAdapterType.newKey(info.provider, info.account.type);
            RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(syncAdapterType, info.userId);
            if (syncAdapterInfo == null) {
                SyncManager.this.mLogger.log("dispatchSyncOperation() failed: no sync adapter info for ", syncAdapterType);
                Log.i("SyncManager", "can't find a sync adapter for " + syncAdapterType + ", removing settings for it");
                SyncManager.this.mSyncStorageEngine.removeAuthority(info);
                return false;
            }
            int targetUid = syncAdapterInfo.uid;
            ComponentName targetComponent = syncAdapterInfo.componentName;
            ActiveSyncContext activeSyncContext = new ActiveSyncContext(op, insertStartSyncEvent(op), targetUid);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "dispatchSyncOperation: starting " + activeSyncContext);
            }
            activeSyncContext.mSyncInfo = SyncManager.this.mSyncStorageEngine.addActiveSync(activeSyncContext);
            SyncManager.this.mActiveSyncContexts.add(activeSyncContext);
            SyncManager.this.postMonitorSyncProgressMessage(activeSyncContext);
            if (activeSyncContext.bindToSyncAdapter(targetComponent, info.userId, op)) {
                return true;
            }
            SyncManager.this.mLogger.log("dispatchSyncOperation() failed: bind failed. target: ", targetComponent);
            Slog.e("SyncManager", "Bind attempt failed - target: " + targetComponent);
            closeActiveSyncContext(activeSyncContext);
            return false;
        }

        private void runBoundToAdapterH(ActiveSyncContext activeSyncContext, IBinder syncAdapter) {
            SyncOperation syncOperation = activeSyncContext.mSyncOperation;
            try {
                activeSyncContext.mIsLinkedToDeath = true;
                syncAdapter.linkToDeath(activeSyncContext, 0);
                SyncManager.this.mLogger.log("Sync start: account=XXXXXXXXX", " authority=", syncOperation.target.provider, " reason=", SyncOperation.reasonToString(null, syncOperation.reason), " extras=", SyncOperation.extrasToString(syncOperation.extras), " adapter=", activeSyncContext.mSyncAdapter);
                activeSyncContext.mSyncAdapter = ISyncAdapter.Stub.asInterface(syncAdapter);
                Slog.i("SyncManager", "run startSync:" + syncOperation);
                activeSyncContext.mSyncAdapter.startSync(activeSyncContext, syncOperation.target.provider, syncOperation.target.account, syncOperation.extras);
                SyncManager.this.mLogger.log("Sync is running now...");
            } catch (RemoteException remoteExc) {
                SyncManager.this.mLogger.log("Sync failed with RemoteException: ", remoteExc.toString());
                Log.i("SyncManager", "maybeStartNextSync: caught a RemoteException, rescheduling", remoteExc);
                closeActiveSyncContext(activeSyncContext);
                SyncManager.this.increaseBackoffSetting(syncOperation.target);
                SyncManager.this.scheduleSyncOperationH(syncOperation);
            } catch (RuntimeException exc) {
                SyncManager.this.mLogger.log("Sync failed with RuntimeException: ", exc.toString());
                closeActiveSyncContext(activeSyncContext);
                Slog.e("SyncManager", "Caught RuntimeException while starting the sync " + SyncLogger.logSafe(syncOperation), exc);
            }
        }

        private void cancelActiveSyncH(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
            Iterator<ActiveSyncContext> it = new ArrayList<>(SyncManager.this.mActiveSyncContexts).iterator();
            while (it.hasNext()) {
                ActiveSyncContext activeSyncContext = it.next();
                if (activeSyncContext != null && activeSyncContext.mSyncOperation.target.matchesSpec(info)) {
                    if (extras == null || SyncManager.syncExtrasEquals(activeSyncContext.mSyncOperation.extras, extras, false)) {
                        SyncJobService.callJobFinished(activeSyncContext.mSyncOperation.jobId, false, why);
                        runSyncFinishedOrCanceledH(null, activeSyncContext);
                    }
                }
            }
        }

        private void reschedulePeriodicSyncH(SyncOperation syncOperation) {
            SyncOperation periodicSync = null;
            Iterator<SyncOperation> it = SyncManager.this.getAllPendingSyncs().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                SyncOperation op = it.next();
                if (op.isPeriodic && syncOperation.matchesPeriodicOperation(op)) {
                    periodicSync = op;
                    break;
                }
            }
            if (periodicSync != null) {
                SyncManager.this.scheduleSyncOperationH(periodicSync);
            }
        }

        private void runSyncFinishedOrCanceledH(SyncResult syncResult, ActiveSyncContext activeSyncContext) {
            int downstreamActivity;
            int upstreamActivity;
            String historyMessage;
            int upstreamActivity2;
            int downstreamActivity2;
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            SyncOperation syncOperation = activeSyncContext.mSyncOperation;
            SyncStorageEngine.EndPoint info = syncOperation.target;
            if (activeSyncContext.mIsLinkedToDeath) {
                activeSyncContext.mSyncAdapter.asBinder().unlinkToDeath(activeSyncContext, 0);
                activeSyncContext.mIsLinkedToDeath = false;
            }
            long elapsedTime = SystemClock.elapsedRealtime() - activeSyncContext.mStartTime;
            SyncManager.this.mLogger.log("runSyncFinishedOrCanceledH() op=", syncOperation, " result=", syncResult);
            if (syncResult != null) {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [finished]: " + syncOperation + ", result " + syncResult);
                }
                closeActiveSyncContext(activeSyncContext);
                if (!syncOperation.isPeriodic) {
                    SyncManager.this.cancelJob(syncOperation, "runSyncFinishedOrCanceledH()-finished");
                }
                if (!syncResult.hasError()) {
                    historyMessage = SyncStorageEngine.MESG_SUCCESS;
                    downstreamActivity2 = 0;
                    upstreamActivity2 = 0;
                    SyncManager.this.clearBackoffSetting(syncOperation.target, "sync success");
                    if (syncOperation.isDerivedFromFailedPeriodicSync()) {
                        reschedulePeriodicSyncH(syncOperation);
                    }
                } else {
                    Log.d("SyncManager", "failed sync operation ");
                    syncOperation.retries++;
                    if (syncOperation.retries > SyncManager.this.mConstants.getMaxRetriesWithAppStandbyExemption()) {
                        syncOperation.syncExemptionFlag = 0;
                    }
                    SyncManager.this.increaseBackoffSetting(syncOperation.target);
                    if (!syncOperation.isPeriodic) {
                        SyncManager.this.maybeRescheduleSync(syncResult, syncOperation);
                    } else {
                        SyncManager.this.postScheduleSyncMessage(syncOperation.createOneTimeSyncOperation(), 0);
                    }
                    historyMessage = ContentResolver.syncErrorToString(syncResultToErrorNumber(syncResult));
                    downstreamActivity2 = 0;
                    upstreamActivity2 = 0;
                }
                SyncManager.this.setDelayUntilTime(syncOperation.target, syncResult.delayUntil);
                downstreamActivity = downstreamActivity2;
                upstreamActivity = upstreamActivity2;
            } else {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [canceled]: " + syncOperation);
                }
                if (!syncOperation.isPeriodic) {
                    SyncManager.this.cancelJob(syncOperation, "runSyncFinishedOrCanceledH()-canceled");
                }
                if (activeSyncContext.mSyncAdapter != null) {
                    try {
                        SyncManager.this.mLogger.log("Calling cancelSync for runSyncFinishedOrCanceled ", activeSyncContext, "  adapter=", activeSyncContext.mSyncAdapter);
                        activeSyncContext.mSyncAdapter.cancelSync(activeSyncContext);
                        SyncManager.this.mLogger.log("Canceled");
                    } catch (RemoteException e) {
                        SyncManager.this.mLogger.log("RemoteException ", Log.getStackTraceString(e));
                    }
                }
                historyMessage = SyncStorageEngine.MESG_CANCELED;
                closeActiveSyncContext(activeSyncContext);
                downstreamActivity = 0;
                upstreamActivity = 0;
            }
            stopSyncEvent(activeSyncContext.mHistoryRowId, syncOperation, historyMessage, upstreamActivity, downstreamActivity, elapsedTime);
            if (syncResult == null || !syncResult.tooManyDeletions) {
                SyncManager.this.mNotificationMgr.cancelAsUser(Integer.toString(info.account.hashCode() ^ info.provider.hashCode()), 18, new UserHandle(info.userId));
            } else {
                installHandleTooManyDeletesNotification(info.account, info.provider, syncResult.stats.numDeletes, info.userId);
            }
            if (syncResult != null && syncResult.fullSyncRequested) {
                SyncManager.this.scheduleSyncOperationH(new SyncOperation(info.account, info.userId, syncOperation.owningUid, syncOperation.owningPackage, syncOperation.reason, syncOperation.syncSource, info.provider, new Bundle(), syncOperation.allowParallelSyncs, syncOperation.syncExemptionFlag));
            }
        }

        private void closeActiveSyncContext(ActiveSyncContext activeSyncContext) {
            activeSyncContext.close();
            SyncManager.this.mActiveSyncContexts.remove(activeSyncContext);
            SyncManager.this.mSyncStorageEngine.removeActiveSync(activeSyncContext.mSyncInfo, activeSyncContext.mSyncOperation.target.userId);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "removing all MESSAGE_MONITOR_SYNC & MESSAGE_SYNC_EXPIRED for " + activeSyncContext.toString());
            }
            SyncManager.this.mSyncHandler.removeMessages(8, activeSyncContext);
            SyncManager.this.mLogger.log("closeActiveSyncContext: ", activeSyncContext);
        }

        private int syncResultToErrorNumber(SyncResult syncResult) {
            if (syncResult.syncAlreadyInProgress) {
                return 1;
            }
            if (syncResult.stats.numAuthExceptions > 0) {
                return 2;
            }
            if (syncResult.stats.numIoExceptions > 0) {
                return 3;
            }
            if (syncResult.stats.numParseExceptions > 0) {
                return 4;
            }
            if (syncResult.stats.numConflictDetectedExceptions > 0) {
                return 5;
            }
            if (syncResult.tooManyDeletions) {
                return 6;
            }
            if (syncResult.tooManyRetries) {
                return 7;
            }
            if (syncResult.databaseError) {
                return 8;
            }
            throw new IllegalStateException("we are not in an error state, " + syncResult);
        }

        private void installHandleTooManyDeletesNotification(Account account, String authority, long numDeletes, int userId) {
            ProviderInfo providerInfo;
            if (SyncManager.this.mNotificationMgr != null && (providerInfo = SyncManager.this.mContext.getPackageManager().resolveContentProvider(authority, 0)) != null) {
                CharSequence authorityName = providerInfo.loadLabel(SyncManager.this.mContext.getPackageManager());
                Intent clickIntent = new Intent(SyncManager.this.mContext, SyncActivityTooManyDeletes.class);
                clickIntent.putExtra("account", account);
                clickIntent.putExtra("authority", authority);
                clickIntent.putExtra("provider", authorityName.toString());
                clickIntent.putExtra("numDeletes", numDeletes);
                if (!isActivityAvailable(clickIntent)) {
                    Log.w("SyncManager", "No activity found to handle too many deletes.");
                    return;
                }
                UserHandle user = new UserHandle(userId);
                PendingIntent pendingIntent = PendingIntent.getActivityAsUser(SyncManager.this.mContext, 0, clickIntent, 268435456, null, user);
                CharSequence tooManyDeletesDescFormat = SyncManager.this.mContext.getResources().getText(17039916);
                Context contextForUser = SyncManager.this.getContextForUser(user);
                Notification notification = new Notification.Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setSmallIcon(17303542).setTicker(SyncManager.this.mContext.getString(17039914)).setWhen(System.currentTimeMillis()).setColor(contextForUser.getColor(17170460)).setContentTitle(contextForUser.getString(17039915)).setContentText(String.format(tooManyDeletesDescFormat.toString(), authorityName)).setContentIntent(pendingIntent).build();
                notification.flags |= 2;
                SyncManager.this.mNotificationMgr.notifyAsUser(Integer.toString(account.hashCode() ^ authority.hashCode()), 18, notification, user);
            }
        }

        private boolean isActivityAvailable(Intent intent) {
            List<ResolveInfo> list = SyncManager.this.mContext.getPackageManager().queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((list.get(i).activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }

        public long insertStartSyncEvent(SyncOperation syncOperation) {
            long now = System.currentTimeMillis();
            EventLog.writeEvent(2720, syncOperation.toEventLog(0));
            return SyncManager.this.mSyncStorageEngine.insertStartSyncEvent(syncOperation, now);
        }

        public void stopSyncEvent(long rowId, SyncOperation syncOperation, String resultMessage, int upstreamActivity, int downstreamActivity, long elapsedTime) {
            EventLog.writeEvent(2720, syncOperation.toEventLog(1));
            SyncManager.this.mSyncStorageEngine.stopSyncEvent(rowId, elapsedTime, resultMessage, (long) downstreamActivity, (long) upstreamActivity, syncOperation.target.userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSyncStillActiveH(ActiveSyncContext activeSyncContext) {
        Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            if (it.next() == activeSyncContext) {
                return true;
            }
        }
        return false;
    }

    public static boolean syncExtrasEquals(Bundle b1, Bundle b2, boolean includeSyncSettings) {
        if (b1 == b2) {
            return true;
        }
        if (includeSyncSettings && b1.size() != b2.size()) {
            return false;
        }
        Bundle bigger = b1.size() > b2.size() ? b1 : b2;
        Bundle smaller = b1.size() > b2.size() ? b2 : b1;
        for (String key : bigger.keySet()) {
            if ((includeSyncSettings || !isSyncSetting(key)) && (!smaller.containsKey(key) || !Objects.equals(bigger.get(key), smaller.get(key)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSyncSetting(String key) {
        if (!key.equals("expedited") && !key.equals("ignore_settings") && !key.equals("ignore_backoff") && !key.equals("do_not_retry") && !key.equals("force") && !key.equals("upload") && !key.equals("deletions_override") && !key.equals("discard_deletions") && !key.equals("expected_upload") && !key.equals("expected_download") && !key.equals("sync_priority") && !key.equals("allow_metered") && !key.equals("initialize")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public static class PrintTable {
        private final int mCols;
        private ArrayList<String[]> mTable = Lists.newArrayList();

        PrintTable(int cols) {
            this.mCols = cols;
        }

        /* access modifiers changed from: package-private */
        public void set(int row, int col, Object... values) {
            String str;
            if (values.length + col <= this.mCols) {
                for (int i = this.mTable.size(); i <= row; i++) {
                    String[] list = new String[this.mCols];
                    this.mTable.add(list);
                    for (int j = 0; j < this.mCols; j++) {
                        list[j] = "";
                    }
                }
                String[] rowArray = this.mTable.get(row);
                for (int i2 = 0; i2 < values.length; i2++) {
                    Object value = values[i2];
                    int i3 = col + i2;
                    if (value == null) {
                        str = "";
                    } else {
                        str = value.toString();
                    }
                    rowArray[i3] = str;
                }
                return;
            }
            throw new IndexOutOfBoundsException("Table only has " + this.mCols + " columns. can't set " + values.length + " at column " + col);
        }

        /* access modifiers changed from: package-private */
        public void writeTo(PrintWriter out) {
            int i;
            String[] formats = new String[this.mCols];
            int totalLength = 0;
            int col = 0;
            while (true) {
                i = this.mCols;
                if (col >= i) {
                    break;
                }
                int maxLength = 0;
                Iterator<String[]> it = this.mTable.iterator();
                while (it.hasNext()) {
                    int length = it.next()[col].toString().length();
                    if (length > maxLength) {
                        maxLength = length;
                    }
                }
                totalLength += maxLength;
                formats[col] = String.format("%%-%ds", Integer.valueOf(maxLength));
                col++;
            }
            formats[i - 1] = "%s";
            printRow(out, formats, this.mTable.get(0));
            int totalLength2 = totalLength + ((this.mCols - 1) * 2);
            for (int i2 = 0; i2 < totalLength2; i2++) {
                out.print("-");
            }
            out.println();
            int mTableSize = this.mTable.size();
            for (int i3 = 1; i3 < mTableSize; i3++) {
                printRow(out, formats, this.mTable.get(i3));
            }
        }

        private void printRow(PrintWriter out, String[] formats, Object[] row) {
            int rowLength = row.length;
            for (int j = 0; j < rowLength; j++) {
                out.printf(String.format(formats[j], row[j].toString()), new Object[0]);
                out.print("  ");
            }
            out.println();
        }

        public int getNumRows() {
            return this.mTable.size();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            return this.mContext;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelJob(SyncOperation op, String why) {
        if (op == null) {
            Slog.wtf("SyncManager", "Null sync operation detected.");
            return;
        }
        if (op.isPeriodic) {
            this.mLogger.log("Removing periodic sync ", op, " for ", why);
        }
        getJobScheduler().cancel(op.jobId);
    }

    public void resetTodayStats() {
        this.mSyncStorageEngine.resetTodayStats(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean wasPackageEverLaunched(String packageName, int userId) {
        try {
            return this.mPackageManagerInternal.wasPackageEverLaunched(packageName, userId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
