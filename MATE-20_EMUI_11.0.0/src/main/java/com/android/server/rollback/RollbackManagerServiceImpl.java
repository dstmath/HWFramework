package com.android.server.rollback;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.content.rollback.IRollbackManager;
import android.content.rollback.PackageRollbackInfo;
import android.content.rollback.RollbackInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.Watchdog;
import com.android.server.pm.DumpState;
import com.android.server.pm.Installer;
import com.android.server.rollback.RollbackManagerServiceImpl;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RollbackManagerServiceImpl extends IRollbackManager.Stub {
    private static final long DEFAULT_ROLLBACK_LIFETIME_DURATION_MILLIS = TimeUnit.DAYS.toMillis(14);
    private static final long HANDLER_THREAD_TIMEOUT_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final String TAG = "RollbackManager";
    private final SparseBooleanArray mAllocatedRollbackIds = new SparseBooleanArray();
    private final AppDataRollbackHelper mAppDataRollbackHelper;
    private final Context mContext;
    private final HandlerThread mHandlerThread;
    private final Installer mInstaller;
    private final Object mLock = new Object();
    private final Set<NewRollback> mNewRollbacks = new ArraySet();
    private final RollbackPackageHealthObserver mPackageHealthObserver;
    private final Random mRandom = new SecureRandom();
    private long mRelativeBootTime = calculateRelativeBootTime();
    private long mRollbackLifetimeDurationInMillis = DEFAULT_ROLLBACK_LIFETIME_DURATION_MILLIS;
    private final RollbackStore mRollbackStore;
    private List<RollbackData> mRollbacks;

    RollbackManagerServiceImpl(Context context) {
        this.mContext = context;
        this.mInstaller = new Installer(this.mContext);
        this.mInstaller.onStart();
        this.mHandlerThread = new HandlerThread("RollbackManagerServiceHandler");
        this.mHandlerThread.start();
        Watchdog.getInstance().addThread(getHandler(), HANDLER_THREAD_TIMEOUT_DURATION_MILLIS);
        this.mRollbackStore = new RollbackStore(new File(Environment.getDataDirectory(), "rollback"));
        this.mPackageHealthObserver = new RollbackPackageHealthObserver(this.mContext);
        this.mAppDataRollbackHelper = new AppDataRollbackHelper(this.mInstaller);
        getHandler().post(new Runnable() {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$2_NDf9EpLcTKkJVpkadZhudKips */

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$new$0$RollbackManagerServiceImpl();
            }
        });
        new SessionCallback();
        for (UserInfo userInfo : UserManager.get(this.mContext).getUsers(true)) {
            registerUserCallbacks(userInfo.getUserHandle());
        }
        IntentFilter enableRollbackFilter = new IntentFilter();
        enableRollbackFilter.addAction("android.intent.action.PACKAGE_ENABLE_ROLLBACK");
        try {
            enableRollbackFilter.addDataType("application/vnd.android.package-archive");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "addDataType", e);
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.rollback.RollbackManagerServiceImpl.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.PACKAGE_ENABLE_ROLLBACK".equals(intent.getAction())) {
                    int token = intent.getIntExtra("android.content.pm.extra.ENABLE_ROLLBACK_TOKEN", -1);
                    int installFlags = intent.getIntExtra("android.content.pm.extra.ENABLE_ROLLBACK_INSTALL_FLAGS", 0);
                    int[] installedUsers = intent.getIntArrayExtra("android.content.pm.extra.ENABLE_ROLLBACK_INSTALLED_USERS");
                    int user = intent.getIntExtra("android.content.pm.extra.ENABLE_ROLLBACK_USER", 0);
                    RollbackManagerServiceImpl.this.getHandler().post(new Runnable(installFlags, new File(intent.getData().getPath()), installedUsers, user, token) {
                        /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$1$TqXV32QQcmn2mAeooJgWwLsvfE */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ File f$2;
                        private final /* synthetic */ int[] f$3;
                        private final /* synthetic */ int f$4;
                        private final /* synthetic */ int f$5;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            RollbackManagerServiceImpl.AnonymousClass1.this.lambda$onReceive$0$RollbackManagerServiceImpl$1(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                        }
                    });
                    abortBroadcast();
                }
            }

            public /* synthetic */ void lambda$onReceive$0$RollbackManagerServiceImpl$1(int installFlags, File newPackageCodePath, int[] installedUsers, int user, int token) {
                int ret = 1;
                if (!RollbackManagerServiceImpl.this.enableRollback(installFlags, newPackageCodePath, installedUsers, user, token)) {
                    ret = -1;
                }
                ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setEnableRollbackCode(token, ret);
            }
        }, enableRollbackFilter, null, getHandler());
        IntentFilter enableRollbackTimedOutFilter = new IntentFilter();
        enableRollbackTimedOutFilter.addAction("android.intent.action.CANCEL_ENABLE_ROLLBACK");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.rollback.RollbackManagerServiceImpl.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.CANCEL_ENABLE_ROLLBACK".equals(intent.getAction())) {
                    int token = intent.getIntExtra("android.content.pm.extra.ENABLE_ROLLBACK_TOKEN", -1);
                    synchronized (RollbackManagerServiceImpl.this.mLock) {
                        for (NewRollback rollback : RollbackManagerServiceImpl.this.mNewRollbacks) {
                            if (rollback.hasToken(token)) {
                                rollback.isCancelled = true;
                                return;
                            }
                        }
                    }
                }
            }
        }, enableRollbackTimedOutFilter, null, getHandler());
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.rollback.RollbackManagerServiceImpl.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int newUserId;
                if ("android.intent.action.USER_ADDED".equals(intent.getAction()) && (newUserId = intent.getIntExtra("android.intent.extra.user_handle", -1)) != -1) {
                    RollbackManagerServiceImpl.this.registerUserCallbacks(UserHandle.of(newUserId));
                }
            }
        }, new IntentFilter("android.intent.action.USER_ADDED"), null, getHandler());
        registerTimeChangeReceiver();
    }

    private void registerUserCallbacks(UserHandle user) {
        Context context = getContextAsUser(user);
        if (context == null) {
            Log.e(TAG, "Unable to register user callbacks for user " + user);
            return;
        }
        context.getPackageManager().getPackageInstaller().registerSessionCallback(new SessionCallback(), getHandler());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_FULLY_REMOVED");
        filter.addDataScheme("package");
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.rollback.RollbackManagerServiceImpl.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.PACKAGE_REPLACED".equals(action)) {
                    RollbackManagerServiceImpl.this.onPackageReplaced(intent.getData().getSchemeSpecificPart());
                }
                if ("android.intent.action.PACKAGE_FULLY_REMOVED".equals(action)) {
                    RollbackManagerServiceImpl.this.onPackageFullyRemoved(intent.getData().getSchemeSpecificPart());
                }
            }
        }, filter, null, getHandler());
    }

    public ParceledListSlice getAvailableRollbacks() {
        ParceledListSlice parceledListSlice;
        enforceManageRollbacks("getAvailableRollbacks");
        if (!Thread.currentThread().equals(this.mHandlerThread)) {
            LinkedBlockingQueue<Boolean> result = new LinkedBlockingQueue<>();
            getHandler().post(new Runnable(result) {
                /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$oLwS8G_DUyKmAeNKhLfFpV3VJTA */
                private final /* synthetic */ LinkedBlockingQueue f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RollbackManagerServiceImpl.lambda$getAvailableRollbacks$1(this.f$0);
                }
            });
            try {
                result.take();
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for handler thread in getAvailableRollbacks");
            }
            synchronized (this.mLock) {
                ensureRollbackDataLoadedLocked();
                List<RollbackInfo> rollbacks = new ArrayList<>();
                for (int i = 0; i < this.mRollbacks.size(); i++) {
                    RollbackData data = this.mRollbacks.get(i);
                    if (data.state == 1) {
                        rollbacks.add(data.info);
                    }
                }
                parceledListSlice = new ParceledListSlice(rollbacks);
            }
            return parceledListSlice;
        }
        Log.wtf(TAG, "Calling getAvailableRollbacks from mHandlerThread causes a deadlock");
        throw new IllegalStateException("Cannot call RollbackManager#getAvailableRollbacks from the handler thread!");
    }

    public ParceledListSlice<RollbackInfo> getRecentlyExecutedRollbacks() {
        ParceledListSlice<RollbackInfo> parceledListSlice;
        enforceManageRollbacks("getRecentlyCommittedRollbacks");
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            List<RollbackInfo> rollbacks = new ArrayList<>();
            for (int i = 0; i < this.mRollbacks.size(); i++) {
                RollbackData data = this.mRollbacks.get(i);
                if (data.state == 3) {
                    rollbacks.add(data.info);
                }
            }
            parceledListSlice = new ParceledListSlice<>(rollbacks);
        }
        return parceledListSlice;
    }

    public void commitRollback(int rollbackId, ParceledListSlice causePackages, String callerPackageName, IntentSender statusReceiver) {
        enforceManageRollbacks("executeRollback");
        ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).checkPackage(Binder.getCallingUid(), callerPackageName);
        getHandler().post(new Runnable(rollbackId, causePackages, callerPackageName, statusReceiver) {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$aG_9_cawiXbCo0CF5aX0ns2oy8 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ ParceledListSlice f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ IntentSender f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$commitRollback$2$RollbackManagerServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$commitRollback$2$RollbackManagerServiceImpl(int rollbackId, ParceledListSlice causePackages, String callerPackageName, IntentSender statusReceiver) {
        commitRollbackInternal(rollbackId, causePackages.getList(), callerPackageName, statusReceiver);
    }

    private void registerTimeChangeReceiver() {
        BroadcastReceiver timeChangeIntentReceiver = new BroadcastReceiver() {
            /* class com.android.server.rollback.RollbackManagerServiceImpl.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                long oldRelativeBootTime = RollbackManagerServiceImpl.this.mRelativeBootTime;
                RollbackManagerServiceImpl.this.mRelativeBootTime = RollbackManagerServiceImpl.calculateRelativeBootTime();
                long timeDifference = RollbackManagerServiceImpl.this.mRelativeBootTime - oldRelativeBootTime;
                synchronized (RollbackManagerServiceImpl.this.mLock) {
                    RollbackManagerServiceImpl.this.ensureRollbackDataLoadedLocked();
                    for (RollbackData data : RollbackManagerServiceImpl.this.mRollbacks) {
                        data.timestamp = data.timestamp.plusMillis(timeDifference);
                        RollbackManagerServiceImpl.this.saveRollbackData(data);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        this.mContext.registerReceiver(timeChangeIntentReceiver, filter, null, getHandler());
    }

    public static long calculateRelativeBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f7, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f9, code lost:
        if (r0 != null) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0101, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0102, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x010b, code lost:
        throw r0;
     */
    private void commitRollbackInternal(int rollbackId, List<VersionedPackage> causePackages, String callerPackageName, IntentSender statusReceiver) {
        String installerPackageName;
        Log.i(TAG, "Initiating rollback");
        RollbackData data = getRollbackForId(rollbackId);
        if (data != null) {
            boolean z = true;
            if (data.state == 1) {
                try {
                    int i = 0;
                    PackageManager pm = this.mContext.createPackageContext(callerPackageName, 0).getPackageManager();
                    try {
                        PackageInstaller packageInstaller = pm.getPackageInstaller();
                        PackageInstaller.SessionParams parentParams = new PackageInstaller.SessionParams(1);
                        parentParams.setRequestDowngrade(true);
                        parentParams.setMultiPackage();
                        if (data.isStaged()) {
                            parentParams.setStaged();
                        }
                        int parentSessionId = packageInstaller.createSession(parentParams);
                        PackageInstaller.Session parentSession = packageInstaller.openSession(parentSessionId);
                        Iterator it = data.info.getPackages().iterator();
                        while (it.hasNext()) {
                            PackageRollbackInfo info = (PackageRollbackInfo) it.next();
                            int i2 = z ? 1 : 0;
                            int i3 = z ? 1 : 0;
                            int i4 = z ? 1 : 0;
                            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(i2);
                            if (!info.isApex() && (installerPackageName = pm.getInstallerPackageName(info.getPackageName())) != null) {
                                params.setInstallerPackageName(installerPackageName);
                            }
                            params.setRequestDowngrade(z);
                            params.setRequiredInstalledVersionCode(info.getVersionRolledBackFrom().getLongVersionCode());
                            if (data.isStaged()) {
                                params.setStaged();
                            }
                            if (info.isApex()) {
                                params.setInstallAsApex();
                            }
                            int sessionId = packageInstaller.createSession(params);
                            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
                            File[] packageCodePaths = RollbackStore.getPackageCodePaths(data, info.getPackageName());
                            if (packageCodePaths == null) {
                                sendFailure(statusReceiver, 1, "Backup copy of package inaccessible");
                                return;
                            }
                            int length = packageCodePaths.length;
                            while (i < length) {
                                File packageCodePath = packageCodePaths[i];
                                ParcelFileDescriptor fd = ParcelFileDescriptor.open(packageCodePath, 268435456);
                                long token = Binder.clearCallingIdentity();
                                try {
                                    session.write(packageCodePath.getName(), 0, packageCodePath.length(), fd);
                                    if (fd != null) {
                                        fd.close();
                                    }
                                    i++;
                                    it = it;
                                    info = info;
                                } finally {
                                    Binder.restoreCallingIdentity(token);
                                }
                            }
                            parentSession.addChildSessionId(sessionId);
                            it = it;
                            i = 0;
                            z = true;
                        }
                        LocalIntentReceiver receiver = new LocalIntentReceiver(new Consumer(data, statusReceiver, parentSessionId, causePackages) {
                            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$oAkfsZ2q5BUu35KwHn4M46EMuGw */
                            private final /* synthetic */ RollbackData f$1;
                            private final /* synthetic */ IntentSender f$2;
                            private final /* synthetic */ int f$3;
                            private final /* synthetic */ List f$4;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                                this.f$4 = r5;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                RollbackManagerServiceImpl.this.lambda$commitRollbackInternal$4$RollbackManagerServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, (Intent) obj);
                            }
                        });
                        synchronized (this.mLock) {
                            data.state = 3;
                            data.restoreUserDataInProgress = true;
                        }
                        parentSession.commit(receiver.getIntentSender());
                        return;
                    } catch (IOException e) {
                        Log.e(TAG, "Rollback failed", e);
                        sendFailure(statusReceiver, 1, "IOException: " + e.toString());
                        return;
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    sendFailure(statusReceiver, 1, "Invalid callerPackageName");
                    return;
                }
            }
        }
        sendFailure(statusReceiver, 2, "Rollback unavailable");
    }

    public /* synthetic */ void lambda$commitRollbackInternal$4$RollbackManagerServiceImpl(RollbackData data, IntentSender statusReceiver, int parentSessionId, List causePackages, Intent result) {
        getHandler().post(new Runnable(result, data, statusReceiver, parentSessionId, causePackages) {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$YVdIiq4wvEBANvFTdY79W4LaS8 */
            private final /* synthetic */ Intent f$1;
            private final /* synthetic */ RollbackData f$2;
            private final /* synthetic */ IntentSender f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ List f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$commitRollbackInternal$3$RollbackManagerServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    public /* synthetic */ void lambda$commitRollbackInternal$3$RollbackManagerServiceImpl(Intent result, RollbackData data, IntentSender statusReceiver, int parentSessionId, List causePackages) {
        if (result.getIntExtra("android.content.pm.extra.STATUS", 1) != 0) {
            synchronized (this.mLock) {
                data.state = 1;
                data.restoreUserDataInProgress = false;
            }
            sendFailure(statusReceiver, 3, "Rollback downgrade install failed: " + result.getStringExtra("android.content.pm.extra.STATUS_MESSAGE"));
            return;
        }
        synchronized (this.mLock) {
            if (!data.isStaged()) {
                data.restoreUserDataInProgress = false;
            }
            data.info.setCommittedSessionId(parentSessionId);
            data.info.getCausePackages().addAll(causePackages);
        }
        RollbackStore rollbackStore = this.mRollbackStore;
        RollbackStore.deletePackageCodePaths(data);
        saveRollbackData(data);
        sendSuccess(statusReceiver);
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.ROLLBACK_COMMITTED"), UserHandle.SYSTEM, "android.permission.MANAGE_ROLLBACKS");
    }

    public void reloadPersistedData() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.TEST_MANAGE_ROLLBACKS", "reloadPersistedData");
        synchronized (this.mLock) {
            this.mRollbacks = null;
        }
        getHandler().post(new Runnable() {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$p7U0gtaH93R3VtUt6jx4Xkt2Avs */

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$reloadPersistedData$5$RollbackManagerServiceImpl();
            }
        });
    }

    public /* synthetic */ void lambda$reloadPersistedData$5$RollbackManagerServiceImpl() {
        lambda$onBootCompleted$7$RollbackManagerServiceImpl();
        lambda$new$0$RollbackManagerServiceImpl();
    }

    public void expireRollbackForPackage(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.TEST_MANAGE_ROLLBACKS", "expireRollbackForPackage");
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            Iterator<RollbackData> iter = this.mRollbacks.iterator();
            while (iter.hasNext()) {
                RollbackData data = iter.next();
                Iterator it = data.info.getPackages().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (((PackageRollbackInfo) it.next()).getPackageName().equals(packageName)) {
                        iter.remove();
                        deleteRollback(data);
                        break;
                    }
                }
            }
        }
    }

    public void onUnlockUser(int userId) {
        getHandler().post(new Runnable(userId) {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$5wr7eOUmDTfGrVye83nSq68E9AA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$onUnlockUser$6$RollbackManagerServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onUnlockUser$6$RollbackManagerServiceImpl(int userId) {
        List<RollbackData> rollbacks;
        synchronized (this.mLock) {
            rollbacks = new ArrayList<>(this.mRollbacks);
        }
        for (RollbackData rd : this.mAppDataRollbackHelper.commitPendingBackupAndRestoreForUser(userId, rollbacks)) {
            saveRollbackData(rd);
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: updateRollbackLifetimeDurationInMillis */
    private void lambda$onBootCompleted$7$RollbackManagerServiceImpl() {
        this.mRollbackLifetimeDurationInMillis = DeviceConfig.getLong("rollback_boot", "rollback_lifetime_in_millis", DEFAULT_ROLLBACK_LIFETIME_DURATION_MILLIS);
        if (this.mRollbackLifetimeDurationInMillis < 0) {
            this.mRollbackLifetimeDurationInMillis = DEFAULT_ROLLBACK_LIFETIME_DURATION_MILLIS;
        }
    }

    public void onBootCompleted() {
        getHandler().post(new Runnable() {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$pS5jbfXLgvSVqxzjSkJaMnydaOY */

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$onBootCompleted$7$RollbackManagerServiceImpl();
            }
        });
        scheduleExpiration(0);
        getHandler().post(new Runnable() {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$V7__18jactj68mqbmRTGjsuUOik */

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$onBootCompleted$8$RollbackManagerServiceImpl();
            }
        });
    }

    public /* synthetic */ void lambda$onBootCompleted$8$RollbackManagerServiceImpl() {
        List<RollbackData> enabling = new ArrayList<>();
        List<RollbackData> restoreInProgress = new ArrayList<>();
        Set<String> apexPackageNames = new HashSet<>();
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            for (RollbackData data : this.mRollbacks) {
                if (data.isStaged()) {
                    if (data.state == 0) {
                        enabling.add(data);
                    } else if (data.restoreUserDataInProgress) {
                        restoreInProgress.add(data);
                    }
                    for (PackageRollbackInfo info : data.info.getPackages()) {
                        if (info.isApex()) {
                            apexPackageNames.add(info.getPackageName());
                        }
                    }
                }
            }
        }
        for (RollbackData data2 : enabling) {
            PackageInstaller.SessionInfo session = this.mContext.getPackageManager().getPackageInstaller().getSessionInfo(data2.stagedSessionId);
            if (session != null) {
                if (session.isStagedSessionApplied()) {
                    makeRollbackAvailable(data2);
                } else if (session.isStagedSessionFailed()) {
                    deleteRollback(data2);
                }
            }
        }
        for (RollbackData data3 : restoreInProgress) {
            PackageInstaller.SessionInfo session2 = this.mContext.getPackageManager().getPackageInstaller().getSessionInfo(data3.stagedSessionId);
            if (session2 != null && (session2.isStagedSessionApplied() || session2.isStagedSessionFailed())) {
                synchronized (this.mLock) {
                    data3.restoreUserDataInProgress = false;
                }
                saveRollbackData(data3);
            }
        }
        for (String apexPackageName : apexPackageNames) {
            onPackageReplaced(apexPackageName);
        }
        this.mPackageHealthObserver.onBootCompletedAsync();
    }

    /* access modifiers changed from: public */
    /* renamed from: ensureRollbackDataLoaded */
    private void lambda$new$0$RollbackManagerServiceImpl() {
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
        }
    }

    private void ensureRollbackDataLoadedLocked() {
        if (this.mRollbacks == null) {
            loadAllRollbackDataLocked();
        }
    }

    private void loadAllRollbackDataLocked() {
        this.mRollbacks = this.mRollbackStore.loadAllRollbackData();
        for (RollbackData data : this.mRollbacks) {
            this.mAllocatedRollbackIds.put(data.info.getRollbackId(), true);
        }
    }

    private void onPackageReplaced(String packageName) {
        VersionedPackage installedVersion = getInstalledPackageVersion(packageName);
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            Iterator<RollbackData> iter = this.mRollbacks.iterator();
            while (iter.hasNext()) {
                RollbackData data = iter.next();
                if (data.state == 1 || data.state == 0) {
                    Iterator it = data.info.getPackages().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        PackageRollbackInfo info = (PackageRollbackInfo) it.next();
                        if (info.getPackageName().equals(packageName) && !packageVersionsEqual(info.getVersionRolledBackFrom(), installedVersion)) {
                            iter.remove();
                            deleteRollback(data);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void onPackageFullyRemoved(String packageName) {
        expireRollbackForPackage(packageName);
    }

    private void sendFailure(IntentSender statusReceiver, int status, String message) {
        Log.e(TAG, message);
        try {
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.rollback.extra.STATUS", status);
            fillIn.putExtra("android.content.rollback.extra.STATUS_MESSAGE", message);
            statusReceiver.sendIntent(this.mContext, 0, fillIn, null, null);
        } catch (IntentSender.SendIntentException e) {
        }
    }

    private void sendSuccess(IntentSender statusReceiver) {
        try {
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.rollback.extra.STATUS", 0);
            statusReceiver.sendIntent(this.mContext, 0, fillIn, null, null);
        } catch (IntentSender.SendIntentException e) {
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: runExpiration */
    private void lambda$scheduleExpiration$9$RollbackManagerServiceImpl() {
        Instant now = Instant.now();
        Instant oldest = null;
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            Iterator<RollbackData> iter = this.mRollbacks.iterator();
            while (iter.hasNext()) {
                RollbackData data = iter.next();
                if (data.state == 1) {
                    if (!now.isBefore(data.timestamp.plusMillis(this.mRollbackLifetimeDurationInMillis))) {
                        iter.remove();
                        deleteRollback(data);
                    } else if (oldest == null || oldest.isAfter(data.timestamp)) {
                        oldest = data.timestamp;
                    }
                }
            }
        }
        if (oldest != null) {
            scheduleExpiration(now.until(oldest.plusMillis(this.mRollbackLifetimeDurationInMillis), ChronoUnit.MILLIS));
        }
    }

    private void scheduleExpiration(long duration) {
        getHandler().postDelayed(new Runnable() {
            /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$CAasF8x0yNQCLBmx5TOpEjeyeEM */

            @Override // java.lang.Runnable
            public final void run() {
                RollbackManagerServiceImpl.this.lambda$scheduleExpiration$9$RollbackManagerServiceImpl();
            }
        }, duration);
    }

    private Handler getHandler() {
        return this.mHandlerThread.getThreadHandler();
    }

    private boolean sessionMatchesForEnableRollback(PackageInstaller.SessionInfo session, int installFlags, File newPackageCodePath) {
        if (session == null || session.resolvedBaseCodePath == null || !newPackageCodePath.equals(new File(session.resolvedBaseCodePath).getParentFile()) || installFlags != session.installFlags) {
            return false;
        }
        return true;
    }

    private Context getContextAsUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0113, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x011c, code lost:
        r0 = th;
     */
    private boolean enableRollback(int installFlags, File newPackageCodePath, int[] installedUsers, int user, int token) {
        PackageInstaller.SessionInfo packageSession;
        PackageInstaller.SessionInfo packageSession2;
        NewRollback newRollback;
        Context context = getContextAsUser(UserHandle.of(user));
        if (context == null) {
            Log.e(TAG, "Unable to create context for install session user.");
            return false;
        }
        PackageInstaller.SessionInfo parentSession = null;
        PackageInstaller.SessionInfo packageSession3 = null;
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        Iterator<PackageInstaller.SessionInfo> it = installer.getAllSessions().iterator();
        while (true) {
            if (!it.hasNext()) {
                packageSession = packageSession3;
                packageSession2 = parentSession;
                break;
            }
            PackageInstaller.SessionInfo info = it.next();
            if (info.isMultiPackage()) {
                int[] childSessionIds = info.getChildSessionIds();
                int length = childSessionIds.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    PackageInstaller.SessionInfo child = installer.getSessionInfo(childSessionIds[i]);
                    if (sessionMatchesForEnableRollback(child, installFlags, newPackageCodePath)) {
                        parentSession = info;
                        packageSession3 = child;
                        break;
                    }
                    i++;
                }
            } else if (sessionMatchesForEnableRollback(info, installFlags, newPackageCodePath)) {
                packageSession = info;
                packageSession2 = info;
                break;
            }
        }
        if (packageSession2 != null) {
            if (packageSession != null) {
                RollbackData rd = null;
                synchronized (this.mLock) {
                    ensureRollbackDataLoadedLocked();
                    int i2 = 0;
                    while (true) {
                        if (i2 >= this.mRollbacks.size()) {
                            break;
                        }
                        RollbackData data = this.mRollbacks.get(i2);
                        if (data.apkSessionId == packageSession2.getSessionId()) {
                            rd = data;
                            break;
                        }
                        i2++;
                    }
                }
                if (rd != null) {
                    try {
                        String packageName = PackageParser.parsePackageLite(new File(packageSession.resolvedBaseCodePath), 0).packageName;
                        for (PackageRollbackInfo info2 : rd.info.getPackages()) {
                            if (info2.getPackageName().equals(packageName)) {
                                info2.getInstalledUsers().addAll(IntArray.wrap(installedUsers));
                                return true;
                            }
                        }
                        Log.e(TAG, "Unable to find package in apk session");
                        return false;
                    } catch (PackageParser.PackageParserException e) {
                        Log.e(TAG, "Unable to parse new package", e);
                        return false;
                    }
                } else {
                    synchronized (this.mLock) {
                        newRollback = getNewRollbackForPackageSessionLocked(packageSession.getSessionId());
                        if (newRollback == null) {
                            newRollback = createNewRollbackLocked(packageSession2);
                            this.mNewRollbacks.add(newRollback);
                        }
                    }
                    newRollback.addToken(token);
                    return enableRollbackForPackageSession(newRollback.data, packageSession, installedUsers);
                }
            }
        }
        Log.e(TAG, "Unable to find session for enabled rollback.");
        return false;
        while (true) {
        }
        while (true) {
        }
    }

    private boolean enableRollbackForPackageSession(RollbackData data, PackageInstaller.SessionInfo session, int[] installedUsers) {
        int installFlags = session.installFlags;
        if ((262144 & installFlags) == 0) {
            Log.e(TAG, "Rollback is not enabled.");
            return false;
        } else if ((installFlags & 2048) != 0) {
            Log.e(TAG, "Rollbacks not supported for instant app install");
            return false;
        } else if (session.resolvedBaseCodePath == null) {
            Log.e(TAG, "Session code path has not been resolved.");
            return false;
        } else {
            try {
                PackageParser.PackageLite newPackage = PackageParser.parsePackageLite(new File(session.resolvedBaseCodePath), 0);
                String packageName = newPackage.packageName;
                Log.i(TAG, "Enabling rollback for install of " + packageName + ", session:" + session.sessionId);
                String installerPackageName = session.getInstallerPackageName();
                if (!enableRollbackAllowed(installerPackageName, packageName)) {
                    Log.e(TAG, "Installer " + installerPackageName + " is not allowed to enable rollback on " + packageName);
                    return false;
                }
                VersionedPackage newVersion = new VersionedPackage(packageName, newPackage.versionCode);
                boolean isApex = (131072 & installFlags) != 0;
                this.mContext.getPackageManager();
                try {
                    PackageInfo pkgInfo = getPackageInfo(packageName);
                    PackageRollbackInfo packageRollbackInfo = new PackageRollbackInfo(newVersion, new VersionedPackage(packageName, pkgInfo.getLongVersionCode()), new IntArray(), new ArrayList(), isApex, IntArray.wrap(installedUsers), new SparseLongArray());
                    try {
                        ApplicationInfo appInfo = pkgInfo.applicationInfo;
                        RollbackStore.backupPackageCodePath(data, packageName, appInfo.sourceDir);
                        if (!ArrayUtils.isEmpty(appInfo.splitSourceDirs)) {
                            for (String sourceDir : appInfo.splitSourceDirs) {
                                RollbackStore.backupPackageCodePath(data, packageName, sourceDir);
                            }
                        }
                        synchronized (this.mLock) {
                            data.info.getPackages().add(packageRollbackInfo);
                        }
                        return true;
                    } catch (IOException e) {
                        Log.e(TAG, "Unable to copy package for rollback for " + packageName, e);
                        return false;
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    Log.e(TAG, packageName + " is not installed");
                    return false;
                }
            } catch (PackageParser.PackageParserException e3) {
                Log.e(TAG, "Unable to parse new package", e3);
                return false;
            }
        }
    }

    public void snapshotAndRestoreUserData(String packageName, int[] userIds, int appId, long ceDataInode, String seInfo, int token) {
        if (Binder.getCallingUid() == 1000) {
            getHandler().post(new Runnable(packageName, userIds, appId, ceDataInode, seInfo, token) {
                /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$o7MYzpkOoXbj0yHHTqdCNjmpt8U */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ int[] f$2;
                private final /* synthetic */ int f$3;
                private final /* synthetic */ long f$4;
                private final /* synthetic */ String f$5;
                private final /* synthetic */ int f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r7;
                    this.f$6 = r8;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RollbackManagerServiceImpl.this.lambda$snapshotAndRestoreUserData$10$RollbackManagerServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                }
            });
            return;
        }
        throw new SecurityException("snapshotAndRestoreUserData may only be called by the system.");
    }

    public /* synthetic */ void lambda$snapshotAndRestoreUserData$10$RollbackManagerServiceImpl(String packageName, int[] userIds, int appId, long ceDataInode, String seInfo, int token) {
        snapshotUserDataInternal(packageName);
        restoreUserDataInternal(packageName, userIds, appId, ceDataInode, seInfo, token);
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).finishPackageInstall(token, false);
    }

    private void snapshotUserDataInternal(String packageName) {
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            for (int i = 0; i < this.mRollbacks.size(); i++) {
                RollbackData data = this.mRollbacks.get(i);
                if (data.state == 0) {
                    Iterator it = data.info.getPackages().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        PackageRollbackInfo info = (PackageRollbackInfo) it.next();
                        if (info.getPackageName().equals(packageName)) {
                            this.mAppDataRollbackHelper.snapshotAppData(data.info.getRollbackId(), info);
                            saveRollbackData(data);
                            break;
                        }
                    }
                }
            }
            for (NewRollback rollback : this.mNewRollbacks) {
                PackageRollbackInfo info2 = getPackageRollbackInfo(rollback.data, packageName);
                if (info2 != null) {
                    this.mAppDataRollbackHelper.snapshotAppData(rollback.data.info.getRollbackId(), info2);
                    saveRollbackData(rollback.data);
                }
            }
        }
    }

    private void restoreUserDataInternal(String packageName, int[] userIds, int appId, long ceDataInode, String seInfo, int token) {
        Throwable th;
        int i;
        PackageRollbackInfo info = null;
        RollbackData rollbackData = null;
        synchronized (this.mLock) {
            try {
                ensureRollbackDataLoadedLocked();
                int i2 = 0;
                while (true) {
                    if (i2 >= this.mRollbacks.size()) {
                        break;
                    }
                    RollbackData data = this.mRollbacks.get(i2);
                    if (data.restoreUserDataInProgress) {
                        try {
                            info = getPackageRollbackInfo(data, packageName);
                            if (info != null) {
                                rollbackData = data;
                                break;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    i2++;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (rollbackData != null) {
            for (int userId : userIds) {
                if (this.mAppDataRollbackHelper.restoreAppData(rollbackData.info.getRollbackId(), info, userId, appId, seInfo)) {
                    saveRollbackData(rollbackData);
                }
            }
        }
    }

    public boolean notifyStagedSession(int sessionId) {
        if (Binder.getCallingUid() == 1000) {
            LinkedBlockingQueue<Boolean> result = new LinkedBlockingQueue<>();
            getHandler().post(new Runnable(sessionId, result) {
                /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$GitEUZMj6F_TZMXHx8fkTXAcvdo */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ LinkedBlockingQueue f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RollbackManagerServiceImpl.this.lambda$notifyStagedSession$11$RollbackManagerServiceImpl(this.f$1, this.f$2);
                }
            });
            try {
                return result.take().booleanValue();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for notifyStagedSession response");
                return false;
            }
        } else {
            throw new SecurityException("notifyStagedSession may only be called by the system.");
        }
    }

    public /* synthetic */ void lambda$notifyStagedSession$11$RollbackManagerServiceImpl(int sessionId, LinkedBlockingQueue result) {
        NewRollback newRollback;
        PackageInstaller installer = this.mContext.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionInfo session = installer.getSessionInfo(sessionId);
        boolean z = false;
        if (session == null) {
            Log.e(TAG, "No matching install session for: " + sessionId);
            result.offer(false);
            return;
        }
        synchronized (this.mLock) {
            newRollback = createNewRollbackLocked(session);
        }
        if (session.isMultiPackage()) {
            int[] childSessionIds = session.getChildSessionIds();
            for (int childSessionId : childSessionIds) {
                PackageInstaller.SessionInfo childSession = installer.getSessionInfo(childSessionId);
                if (childSession == null) {
                    Log.e(TAG, "No matching child install session for: " + childSessionId);
                    result.offer(false);
                    return;
                } else if (!enableRollbackForPackageSession(newRollback.data, childSession, new int[0])) {
                    Log.e(TAG, "Unable to enable rollback for session: " + sessionId);
                    result.offer(false);
                    return;
                }
            }
        } else if (!enableRollbackForPackageSession(newRollback.data, session, new int[0])) {
            Log.e(TAG, "Unable to enable rollback for session: " + sessionId);
            result.offer(false);
            return;
        }
        if (completeEnableRollback(newRollback, true) != null) {
            z = true;
        }
        result.offer(Boolean.valueOf(z));
    }

    public void notifyStagedApkSession(int originalSessionId, int apkSessionId) {
        if (Binder.getCallingUid() == 1000) {
            getHandler().post(new Runnable(originalSessionId, apkSessionId) {
                /* class com.android.server.rollback.$$Lambda$RollbackManagerServiceImpl$ohlyqMiNlQtoY5XHz6vC79CRKAA */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RollbackManagerServiceImpl.this.lambda$notifyStagedApkSession$12$RollbackManagerServiceImpl(this.f$1, this.f$2);
                }
            });
            return;
        }
        throw new SecurityException("notifyStagedApkSession may only be called by the system.");
    }

    public /* synthetic */ void lambda$notifyStagedApkSession$12$RollbackManagerServiceImpl(int originalSessionId, int apkSessionId) {
        RollbackData rd = null;
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            int i = 0;
            while (true) {
                if (i >= this.mRollbacks.size()) {
                    break;
                }
                RollbackData data = this.mRollbacks.get(i);
                if (data.stagedSessionId == originalSessionId) {
                    data.apkSessionId = apkSessionId;
                    rd = data;
                    break;
                }
                i++;
            }
        }
        if (rd != null) {
            saveRollbackData(rd);
        }
    }

    private boolean enableRollbackAllowed(String installerPackageName, String packageName) {
        if (installerPackageName == null) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        boolean manageRollbacksGranted = pm.checkPermission("android.permission.MANAGE_ROLLBACKS", installerPackageName) == 0;
        boolean testManageRollbacksGranted = pm.checkPermission("android.permission.TEST_MANAGE_ROLLBACKS", installerPackageName) == 0;
        if ((!isModule(packageName) || !manageRollbacksGranted) && !testManageRollbacksGranted) {
            return false;
        }
        return true;
    }

    private boolean isModule(String packageName) {
        try {
            if (this.mContext.getPackageManager().getModuleInfo(packageName, 0) != null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private VersionedPackage getInstalledPackageVersion(String packageName) {
        this.mContext.getPackageManager();
        try {
            return new VersionedPackage(packageName, getPackageInfo(packageName).getLongVersionCode());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private PackageInfo getPackageInfo(String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            return pm.getPackageInfo(packageName, DumpState.DUMP_CHANGES);
        } catch (PackageManager.NameNotFoundException e) {
            return pm.getPackageInfo(packageName, 1073741824);
        }
    }

    private boolean packageVersionsEqual(VersionedPackage a, VersionedPackage b) {
        return a != null && b != null && a.getPackageName().equals(b.getPackageName()) && a.getLongVersionCode() == b.getLongVersionCode();
    }

    public class SessionCallback extends PackageInstaller.SessionCallback {
        private SessionCallback() {
            RollbackManagerServiceImpl.this = r1;
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onCreated(int sessionId) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onBadgingChanged(int sessionId) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onActiveChanged(int sessionId, boolean active) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onProgressChanged(int sessionId, float progress) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onFinished(int sessionId, boolean success) {
            NewRollback newRollback;
            RollbackData rollback;
            synchronized (RollbackManagerServiceImpl.this.mLock) {
                newRollback = RollbackManagerServiceImpl.this.getNewRollbackForPackageSessionLocked(sessionId);
                if (newRollback != null) {
                    RollbackManagerServiceImpl.this.mNewRollbacks.remove(newRollback);
                }
            }
            if (newRollback != null && (rollback = RollbackManagerServiceImpl.this.completeEnableRollback(newRollback, success)) != null && !rollback.isStaged()) {
                RollbackManagerServiceImpl.this.makeRollbackAvailable(rollback);
            }
        }
    }

    private RollbackData completeEnableRollback(NewRollback newRollback, boolean success) {
        RollbackData data = newRollback.data;
        if (!success) {
            deleteRollback(data);
            return null;
        } else if (newRollback.isCancelled) {
            Log.e(TAG, "Rollback has been cancelled by PackageManager");
            deleteRollback(data);
            return null;
        } else if (data.info.getPackages().size() != newRollback.packageSessionIds.length) {
            Log.e(TAG, "Failed to enable rollback for all packages in session.");
            deleteRollback(data);
            return null;
        } else {
            saveRollbackData(data);
            synchronized (this.mLock) {
                ensureRollbackDataLoadedLocked();
                this.mRollbacks.add(data);
            }
            return data;
        }
    }

    private void makeRollbackAvailable(RollbackData data) {
        synchronized (this.mLock) {
            data.state = 1;
            data.timestamp = Instant.now();
        }
        saveRollbackData(data);
        List<String> packages = new ArrayList<>();
        for (int i = 0; i < data.info.getPackages().size(); i++) {
            packages.add(((PackageRollbackInfo) data.info.getPackages().get(i)).getPackageName());
        }
        this.mPackageHealthObserver.startObservingHealth(packages, this.mRollbackLifetimeDurationInMillis);
        scheduleExpiration(this.mRollbackLifetimeDurationInMillis);
    }

    private RollbackData getRollbackForId(int rollbackId) {
        synchronized (this.mLock) {
            ensureRollbackDataLoadedLocked();
            for (int i = 0; i < this.mRollbacks.size(); i++) {
                RollbackData data = this.mRollbacks.get(i);
                if (data.info.getRollbackId() == rollbackId) {
                    return data;
                }
            }
            return null;
        }
    }

    private static PackageRollbackInfo getPackageRollbackInfo(RollbackData data, String packageName) {
        for (PackageRollbackInfo info : data.info.getPackages()) {
            if (info.getPackageName().equals(packageName)) {
                return info;
            }
        }
        return null;
    }

    private int allocateRollbackIdLocked() {
        int n = 0;
        while (true) {
            int rollbackId = this.mRandom.nextInt(2147483646) + 1;
            if (!this.mAllocatedRollbackIds.get(rollbackId, false)) {
                this.mAllocatedRollbackIds.put(rollbackId, true);
                return rollbackId;
            }
            int n2 = n + 1;
            if (n < 32) {
                n = n2;
            } else {
                throw new IllegalStateException("Failed to allocate rollback ID");
            }
        }
    }

    private void deleteRollback(RollbackData rollbackData) {
        for (PackageRollbackInfo info : rollbackData.info.getPackages()) {
            IntArray installedUsers = info.getInstalledUsers();
            for (int i = 0; i < installedUsers.size(); i++) {
                this.mAppDataRollbackHelper.destroyAppDataSnapshot(rollbackData.info.getRollbackId(), info, installedUsers.get(i));
            }
        }
        this.mRollbackStore.deleteRollbackData(rollbackData);
    }

    private void saveRollbackData(RollbackData rollbackData) {
        try {
            this.mRollbackStore.saveRollbackData(rollbackData);
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to save rollback info for: " + rollbackData.info.getRollbackId(), ioe);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        synchronized (this.mLock) {
            for (RollbackData data : this.mRollbacks) {
                RollbackInfo info = data.info;
                ipw.println(info.getRollbackId() + ":");
                ipw.increaseIndent();
                ipw.println("-state: " + data.getStateAsString());
                ipw.println("-timestamp: " + data.timestamp);
                if (data.stagedSessionId != -1) {
                    ipw.println("-stagedSessionId: " + data.stagedSessionId);
                }
                ipw.println("-packages:");
                ipw.increaseIndent();
                for (PackageRollbackInfo pkg : info.getPackages()) {
                    ipw.println(pkg.getPackageName() + " " + pkg.getVersionRolledBackFrom().getLongVersionCode() + " -> " + pkg.getVersionRolledBackTo().getLongVersionCode());
                }
                ipw.decreaseIndent();
                if (data.state == 3) {
                    ipw.println("-causePackages:");
                    ipw.increaseIndent();
                    for (VersionedPackage cPkg : info.getCausePackages()) {
                        ipw.println(cPkg.getPackageName() + " " + cPkg.getLongVersionCode());
                    }
                    ipw.decreaseIndent();
                    ipw.println("-committedSessionId: " + info.getCommittedSessionId());
                }
                ipw.decreaseIndent();
            }
        }
    }

    private void enforceManageRollbacks(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_ROLLBACKS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.TEST_MANAGE_ROLLBACKS") != 0) {
            throw new SecurityException(message + " requires android.permission.MANAGE_ROLLBACKS or android.permission.TEST_MANAGE_ROLLBACKS");
        }
    }

    public static class NewRollback {
        public final RollbackData data;
        public boolean isCancelled = false;
        private final IntArray mTokens = new IntArray();
        public final int[] packageSessionIds;

        NewRollback(RollbackData data2, int[] packageSessionIds2) {
            this.data = data2;
            this.packageSessionIds = packageSessionIds2;
        }

        public void addToken(int token) {
            this.mTokens.add(token);
        }

        public boolean hasToken(int token) {
            return this.mTokens.indexOf(token) != -1;
        }
    }

    public NewRollback createNewRollbackLocked(PackageInstaller.SessionInfo parentSession) {
        RollbackData data;
        int rollbackId = allocateRollbackIdLocked();
        int parentSessionId = parentSession.getSessionId();
        if (parentSession.isStaged()) {
            data = this.mRollbackStore.createStagedRollback(rollbackId, parentSessionId);
        } else {
            data = this.mRollbackStore.createNonStagedRollback(rollbackId);
        }
        return new NewRollback(data, parentSession.isMultiPackage() ? parentSession.getChildSessionIds() : new int[]{parentSessionId});
    }

    public NewRollback getNewRollbackForPackageSessionLocked(int packageSessionId) {
        for (NewRollback newRollbackData : this.mNewRollbacks) {
            int[] iArr = newRollbackData.packageSessionIds;
            int length = iArr.length;
            int i = 0;
            while (true) {
                if (i < length) {
                    if (iArr[i] == packageSessionId) {
                        return newRollbackData;
                    }
                    i++;
                }
            }
        }
        return null;
    }
}
