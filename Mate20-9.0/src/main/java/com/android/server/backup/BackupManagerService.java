package com.android.server.backup;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IBackupAgent;
import android.app.PendingIntent;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.backup.DataChangedJournal;
import com.android.server.backup.fullbackup.FullBackupEntry;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.internal.BackupHandler;
import com.android.server.backup.internal.BackupRequest;
import com.android.server.backup.internal.ClearDataObserver;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.internal.Operation;
import com.android.server.backup.internal.PerformInitializeTask;
import com.android.server.backup.internal.ProvisionedObserver;
import com.android.server.backup.internal.RunBackupReceiver;
import com.android.server.backup.internal.RunInitializeReceiver;
import com.android.server.backup.params.AdbBackupParams;
import com.android.server.backup.params.AdbParams;
import com.android.server.backup.params.AdbRestoreParams;
import com.android.server.backup.params.BackupParams;
import com.android.server.backup.params.ClearParams;
import com.android.server.backup.params.ClearRetryParams;
import com.android.server.backup.params.RestoreParams;
import com.android.server.backup.restore.ActiveRestoreSession;
import com.android.server.backup.restore.PerformUnifiedRestoreTask;
import com.android.server.backup.transport.OnTransportRegisteredListener;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.transport.TransportNotRegisteredException;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.backup.utils.BackupObserverUtils;
import com.android.server.backup.utils.SparseArrayUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.usage.AppStandbyController;
import com.google.android.collect.Sets;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class BackupManagerService implements BackupManagerServiceInterface {
    private static final String BACKUP_ENABLE_FILE = "backup_enabled";
    public static final String BACKUP_FILE_HEADER_MAGIC = "ANDROID BACKUP\n";
    public static final int BACKUP_FILE_VERSION = 5;
    public static final String BACKUP_FINISHED_ACTION = "android.intent.action.BACKUP_FINISHED";
    public static final String BACKUP_FINISHED_PACKAGE_EXTRA = "packageName";
    public static final String BACKUP_MANIFEST_FILENAME = "_manifest";
    public static final int BACKUP_MANIFEST_VERSION = 1;
    public static final String BACKUP_METADATA_FILENAME = "_meta";
    public static final int BACKUP_METADATA_VERSION = 1;
    public static final int BACKUP_WIDGET_METADATA_TOKEN = 33549569;
    private static final int BUSY_BACKOFF_FUZZ = 7200000;
    private static final long BUSY_BACKOFF_MIN_MILLIS = 3600000;
    private static final boolean COMPRESS_FULL_BACKUPS = true;
    private static final int CURRENT_ANCESTRAL_RECORD_VERSION = 1;
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_BACKUP_TRACE = true;
    public static final boolean DEBUG_SCHEDULING = true;
    private static final long INITIALIZATION_DELAY_MILLIS = 3000;
    private static final String INIT_SENTINEL_FILE_NAME = "_need_init_";
    public static final String KEY_WIDGET_STATE = "￭￭widget";
    public static final boolean MORE_DEBUG = false;
    private static final int OP_ACKNOWLEDGED = 1;
    public static final int OP_PENDING = 0;
    private static final int OP_TIMEOUT = -1;
    public static final int OP_TYPE_BACKUP = 2;
    public static final int OP_TYPE_BACKUP_WAIT = 0;
    public static final int OP_TYPE_RESTORE_WAIT = 1;
    public static final String PACKAGE_MANAGER_SENTINEL = "@pm@";
    public static final String RUN_BACKUP_ACTION = "android.app.backup.intent.RUN";
    public static final String RUN_INITIALIZE_ACTION = "android.app.backup.intent.INIT";
    private static final int SCHEDULE_FILE_VERSION = 1;
    private static final String SERVICE_ACTION_TRANSPORT_HOST = "android.backup.TRANSPORT_HOST";
    public static final String SETTINGS_PACKAGE = "com.android.providers.settings";
    public static final String SHARED_BACKUP_AGENT_PACKAGE = "com.android.sharedstoragebackup";
    public static final String TAG = "BackupManagerService";
    private static final long TIMEOUT_FULL_CONFIRMATION = 60000;
    private static final long TIMEOUT_INTERVAL = 10000;
    private static final long TRANSPORT_RETRY_INTERVAL = 3600000;
    static Trampoline sInstance;
    private ActiveRestoreSession mActiveRestoreSession;
    private IActivityManager mActivityManager;
    private final SparseArray<AdbParams> mAdbBackupRestoreConfirmations = new SparseArray<>();
    private final Object mAgentConnectLock = new Object();
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private AlarmManager mAlarmManager;
    private Set<String> mAncestralPackages = null;
    private long mAncestralToken = 0;
    private boolean mAutoRestore;
    /* access modifiers changed from: private */
    public BackupHandler mBackupHandler;
    private IBackupManager mBackupManagerBinder;
    /* access modifiers changed from: private */
    public final SparseArray<HashSet<String>> mBackupParticipants = new SparseArray<>();
    private final BackupPasswordManager mBackupPasswordManager;
    private BackupPolicyEnforcer mBackupPolicyEnforcer;
    private volatile boolean mBackupRunning;
    private final List<String> mBackupTrace = new ArrayList();
    private File mBaseStateDir;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b5, code lost:
            r11 = java.lang.System.currentTimeMillis();
            r13 = r7.length;
            r14 = 0;
         */
        public void onReceive(Context context, Intent intent) {
            long now;
            int length;
            int i;
            String action;
            Intent intent2 = intent;
            String action2 = intent.getAction();
            boolean replacing = false;
            boolean added = false;
            boolean added2 = false;
            Bundle extras = intent.getExtras();
            String[] pkgList = null;
            int i2 = 0;
            if ("android.intent.action.PACKAGE_ADDED".equals(action2) || "android.intent.action.PACKAGE_REMOVED".equals(action2) || "android.intent.action.PACKAGE_CHANGED".equals(action2)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String pkgName = uri.getSchemeSpecificPart();
                    if (pkgName != null) {
                        pkgList = new String[]{pkgName};
                    }
                    added2 = "android.intent.action.PACKAGE_CHANGED".equals(action2);
                    if (added2) {
                        BackupManagerService.this.mBackupHandler.post(new Runnable(pkgName, intent2.getStringArrayExtra("android.intent.extra.changed_component_name_list")) {
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ String[] f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                BackupManagerService.this.mTransportManager.onPackageChanged(this.f$1, this.f$2);
                            }
                        });
                        return;
                    }
                    added = "android.intent.action.PACKAGE_ADDED".equals(action2);
                    replacing = extras.getBoolean("android.intent.extra.REPLACING", false);
                } else {
                    return;
                }
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action2)) {
                added = true;
                pkgList = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action2)) {
                added = false;
                pkgList = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
            }
            boolean changed = added2;
            boolean added3 = added;
            boolean replacing2 = replacing;
            if (pkgList == null) {
            } else if (pkgList.length == 0) {
                String str = action2;
            } else {
                int uid = extras.getInt("android.intent.extra.UID");
                if (added3) {
                    synchronized (BackupManagerService.this.mBackupParticipants) {
                        if (replacing2) {
                            try {
                                BackupManagerService.this.removePackageParticipantsLocked(pkgList, uid);
                            } catch (Throwable th) {
                                th = th;
                                String str2 = action2;
                            }
                        }
                        try {
                            BackupManagerService.this.addPackageParticipantsLocked(pkgList);
                        } catch (Throwable th2) {
                            th = th2;
                            String str3 = action2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                    }
                } else {
                    if (!replacing2) {
                        synchronized (BackupManagerService.this.mBackupParticipants) {
                            BackupManagerService.this.removePackageParticipantsLocked(pkgList, uid);
                        }
                    }
                    for (String pkgName2 : pkgList) {
                        BackupManagerService.this.mBackupHandler.post(new Runnable(pkgName2) {
                            private final /* synthetic */ String f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                BackupManagerService.this.mTransportManager.onPackageRemoved(this.f$1);
                            }
                        });
                    }
                }
            }
            return;
            while (i < length) {
                String packageName = pkgList[i];
                try {
                    PackageInfo app = BackupManagerService.this.mPackageManager.getPackageInfo(packageName, i2);
                    if (!AppBackupUtils.appGetsFullBackup(app) || !AppBackupUtils.appIsEligibleForBackup(app.applicationInfo, BackupManagerService.this.mPackageManager)) {
                        action = action2;
                        synchronized (BackupManagerService.this.mQueueLock) {
                            BackupManagerService.this.dequeueFullBackupLocked(packageName);
                        }
                        BackupManagerService.this.writeFullBackupScheduleAsync();
                    } else {
                        BackupManagerService.this.enqueueFullBackup(packageName, now);
                        action = action2;
                        try {
                            BackupManagerService.this.scheduleNextFullBackupJob(0);
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                    }
                    BackupManagerService.this.mBackupHandler.post(new Runnable(packageName) {
                        private final /* synthetic */ String f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            BackupManagerService.this.mTransportManager.onPackageAdded(this.f$1);
                        }
                    });
                } catch (PackageManager.NameNotFoundException e2) {
                    action = action2;
                    Slog.w(BackupManagerService.TAG, "Can't resolve new app " + packageName);
                    i++;
                    action2 = action;
                    Intent intent3 = intent;
                    i2 = 0;
                }
                i++;
                action2 = action;
                Intent intent32 = intent;
                i2 = 0;
            }
            BackupManagerService.this.dataChangedImpl(BackupManagerService.PACKAGE_MANAGER_SENTINEL);
        }
    };
    private final Object mClearDataLock = new Object();
    private volatile boolean mClearingData;
    private IBackupAgent mConnectedAgent;
    private volatile boolean mConnecting;
    /* access modifiers changed from: private */
    public BackupManagerConstants mConstants;
    /* access modifiers changed from: private */
    public Context mContext;
    private final Object mCurrentOpLock = new Object();
    @GuardedBy("mCurrentOpLock")
    private final SparseArray<Operation> mCurrentOperations = new SparseArray<>();
    private long mCurrentToken = 0;
    private File mDataDir;
    private boolean mEnabled;
    /* access modifiers changed from: private */
    @GuardedBy("mQueueLock")
    public ArrayList<FullBackupEntry> mFullBackupQueue;
    /* access modifiers changed from: private */
    public File mFullBackupScheduleFile;
    private Runnable mFullBackupScheduleWriter = new Runnable() {
        public void run() {
            synchronized (BackupManagerService.this.mQueueLock) {
                try {
                    ByteArrayOutputStream bufStream = new ByteArrayOutputStream(4096);
                    DataOutputStream bufOut = new DataOutputStream(bufStream);
                    bufOut.writeInt(1);
                    int N = BackupManagerService.this.mFullBackupQueue.size();
                    bufOut.writeInt(N);
                    for (int i = 0; i < N; i++) {
                        FullBackupEntry entry = (FullBackupEntry) BackupManagerService.this.mFullBackupQueue.get(i);
                        bufOut.writeUTF(entry.packageName);
                        bufOut.writeLong(entry.lastBackup);
                    }
                    bufOut.flush();
                    AtomicFile af = new AtomicFile(BackupManagerService.this.mFullBackupScheduleFile);
                    FileOutputStream out = af.startWrite();
                    out.write(bufStream.toByteArray());
                    af.finishWrite(out);
                } catch (Exception e) {
                    Slog.e(BackupManagerService.TAG, "Unable to write backup schedule!", e);
                }
            }
        }
    };
    @GuardedBy("mPendingRestores")
    private boolean mIsRestoreInProgress;
    private DataChangedJournal mJournal;
    private File mJournalDir;
    private volatile long mLastBackupPass;
    final AtomicInteger mNextToken = new AtomicInteger();
    /* access modifiers changed from: private */
    public PackageManager mPackageManager;
    private IPackageManager mPackageManagerBinder;
    private HashMap<String, BackupRequest> mPendingBackups = new HashMap<>();
    private final ArraySet<String> mPendingInits = new ArraySet<>();
    @GuardedBy("mPendingRestores")
    private final Queue<PerformUnifiedRestoreTask> mPendingRestores = new ArrayDeque();
    private PowerManager mPowerManager;
    private ProcessedPackagesJournal mProcessedPackagesJournal;
    private boolean mProvisioned;
    private ContentObserver mProvisionedObserver;
    /* access modifiers changed from: private */
    public final Object mQueueLock = new Object();
    private final long mRegisterTransportsRequestedTime;
    private final SecureRandom mRng = new SecureRandom();
    private PendingIntent mRunBackupIntent;
    private BroadcastReceiver mRunBackupReceiver;
    private PendingIntent mRunInitIntent;
    private BroadcastReceiver mRunInitReceiver;
    /* access modifiers changed from: private */
    @GuardedBy("mQueueLock")
    public PerformFullTransportBackupTask mRunningFullBackupTask;
    private IStorageManager mStorageManager;
    private File mTokenFile;
    private final Random mTokenGenerator = new Random();
    /* access modifiers changed from: private */
    public final TransportManager mTransportManager;
    private PowerManager.WakeLock mWakelock;

    public static final class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
            BackupManagerService.sInstance = new Trampoline(context);
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.backup.Trampoline, android.os.IBinder] */
        public void onStart() {
            publishBinderService(BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, BackupManagerService.sInstance);
        }

        public void onUnlockUser(int userId) {
            if (userId == 0) {
                BackupManagerService.sInstance.unlockSystemUser();
            }
        }
    }

    static Trampoline getInstance() {
        return sInstance;
    }

    public BackupManagerConstants getConstants() {
        return this.mConstants;
    }

    public BackupAgentTimeoutParameters getAgentTimeoutParameters() {
        return this.mAgentTimeoutParameters;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public PackageManager getPackageManager() {
        return this.mPackageManager;
    }

    public void setPackageManager(PackageManager packageManager) {
        this.mPackageManager = packageManager;
    }

    public IPackageManager getPackageManagerBinder() {
        return this.mPackageManagerBinder;
    }

    public void setPackageManagerBinder(IPackageManager packageManagerBinder) {
        this.mPackageManagerBinder = packageManagerBinder;
    }

    public IActivityManager getActivityManager() {
        return this.mActivityManager;
    }

    public void setActivityManager(IActivityManager activityManager) {
        this.mActivityManager = activityManager;
    }

    public AlarmManager getAlarmManager() {
        return this.mAlarmManager;
    }

    public void setAlarmManager(AlarmManager alarmManager) {
        this.mAlarmManager = alarmManager;
    }

    public void setBackupManagerBinder(IBackupManager backupManagerBinder) {
        this.mBackupManagerBinder = backupManagerBinder;
    }

    public TransportManager getTransportManager() {
        return this.mTransportManager;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean isProvisioned() {
        return this.mProvisioned;
    }

    public void setProvisioned(boolean provisioned) {
        this.mProvisioned = provisioned;
    }

    public PowerManager.WakeLock getWakelock() {
        return this.mWakelock;
    }

    public void setWakelock(PowerManager.WakeLock wakelock) {
        this.mWakelock = wakelock;
    }

    public Handler getBackupHandler() {
        return this.mBackupHandler;
    }

    public void setBackupHandler(BackupHandler backupHandler) {
        this.mBackupHandler = backupHandler;
    }

    public PendingIntent getRunInitIntent() {
        return this.mRunInitIntent;
    }

    public void setRunInitIntent(PendingIntent runInitIntent) {
        this.mRunInitIntent = runInitIntent;
    }

    public HashMap<String, BackupRequest> getPendingBackups() {
        return this.mPendingBackups;
    }

    public void setPendingBackups(HashMap<String, BackupRequest> pendingBackups) {
        this.mPendingBackups = pendingBackups;
    }

    public Object getQueueLock() {
        return this.mQueueLock;
    }

    public boolean isBackupRunning() {
        return this.mBackupRunning;
    }

    public void setBackupRunning(boolean backupRunning) {
        this.mBackupRunning = backupRunning;
    }

    public long getLastBackupPass() {
        return this.mLastBackupPass;
    }

    public void setLastBackupPass(long lastBackupPass) {
        this.mLastBackupPass = lastBackupPass;
    }

    public Object getClearDataLock() {
        return this.mClearDataLock;
    }

    public boolean isClearingData() {
        return this.mClearingData;
    }

    public void setClearingData(boolean clearingData) {
        this.mClearingData = clearingData;
    }

    public boolean isRestoreInProgress() {
        return this.mIsRestoreInProgress;
    }

    public void setRestoreInProgress(boolean restoreInProgress) {
        this.mIsRestoreInProgress = restoreInProgress;
    }

    public Queue<PerformUnifiedRestoreTask> getPendingRestores() {
        return this.mPendingRestores;
    }

    public ActiveRestoreSession getActiveRestoreSession() {
        return this.mActiveRestoreSession;
    }

    public void setActiveRestoreSession(ActiveRestoreSession activeRestoreSession) {
        this.mActiveRestoreSession = activeRestoreSession;
    }

    public SparseArray<Operation> getCurrentOperations() {
        return this.mCurrentOperations;
    }

    public Object getCurrentOpLock() {
        return this.mCurrentOpLock;
    }

    public SparseArray<AdbParams> getAdbBackupRestoreConfirmations() {
        return this.mAdbBackupRestoreConfirmations;
    }

    public File getBaseStateDir() {
        return this.mBaseStateDir;
    }

    public void setBaseStateDir(File baseStateDir) {
        this.mBaseStateDir = baseStateDir;
    }

    public File getDataDir() {
        return this.mDataDir;
    }

    public void setDataDir(File dataDir) {
        this.mDataDir = dataDir;
    }

    public DataChangedJournal getJournal() {
        return this.mJournal;
    }

    public void setJournal(DataChangedJournal journal) {
        this.mJournal = journal;
    }

    public SecureRandom getRng() {
        return this.mRng;
    }

    public Set<String> getAncestralPackages() {
        return this.mAncestralPackages;
    }

    public void setAncestralPackages(Set<String> ancestralPackages) {
        this.mAncestralPackages = ancestralPackages;
    }

    public long getAncestralToken() {
        return this.mAncestralToken;
    }

    public void setAncestralToken(long ancestralToken) {
        this.mAncestralToken = ancestralToken;
    }

    public long getCurrentToken() {
        return this.mCurrentToken;
    }

    public void setCurrentToken(long currentToken) {
        this.mCurrentToken = currentToken;
    }

    public ArraySet<String> getPendingInits() {
        return this.mPendingInits;
    }

    public void clearPendingInits() {
        this.mPendingInits.clear();
    }

    public PerformFullTransportBackupTask getRunningFullBackupTask() {
        return this.mRunningFullBackupTask;
    }

    public void setRunningFullBackupTask(PerformFullTransportBackupTask runningFullBackupTask) {
        this.mRunningFullBackupTask = runningFullBackupTask;
    }

    public void unlockSystemUser() {
        Trace.traceBegin(64, "backup migrate");
        if (!backupSettingMigrated(0)) {
            Slog.i(TAG, "Backup enable apparently not migrated");
            ContentResolver r = sInstance.mContext.getContentResolver();
            int enableState = Settings.Secure.getIntForUser(r, BACKUP_ENABLE_FILE, -1, 0);
            if (enableState >= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Migrating enable state ");
                boolean z = true;
                sb.append(enableState != 0);
                Slog.i(TAG, sb.toString());
                if (enableState == 0) {
                    z = false;
                }
                writeBackupEnableState(z, 0);
                Settings.Secure.putStringForUser(r, BACKUP_ENABLE_FILE, null, 0);
            } else {
                Slog.i(TAG, "Backup not yet configured; retaining null enable state");
            }
        }
        Trace.traceEnd(64);
        Trace.traceBegin(64, "backup enable");
        try {
            sInstance.setBackupEnabled(readBackupEnableState(0));
        } catch (RemoteException e) {
        }
        Trace.traceEnd(64);
    }

    public int generateRandomIntegerToken() {
        int token = this.mTokenGenerator.nextInt();
        if (token < 0) {
            token = -token;
        }
        return (token & -256) | (this.mNextToken.incrementAndGet() & 255);
    }

    public PackageManagerBackupAgent makeMetadataAgent() {
        PackageManagerBackupAgent pmAgent = new PackageManagerBackupAgent(this.mPackageManager);
        pmAgent.attach(this.mContext);
        pmAgent.onCreate();
        return pmAgent;
    }

    public PackageManagerBackupAgent makeMetadataAgent(List<PackageInfo> packages) {
        PackageManagerBackupAgent pmAgent = new PackageManagerBackupAgent(this.mPackageManager, packages);
        pmAgent.attach(this.mContext);
        pmAgent.onCreate();
        return pmAgent;
    }

    public void addBackupTrace(String s) {
        synchronized (this.mBackupTrace) {
            this.mBackupTrace.add(s);
        }
    }

    public void clearBackupTrace() {
        synchronized (this.mBackupTrace) {
            this.mBackupTrace.clear();
        }
    }

    public static BackupManagerService create(Context context, Trampoline parent, HandlerThread backupThread) {
        Set<ComponentName> transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
        if (transportWhitelist == null) {
            transportWhitelist = Collections.emptySet();
        }
        String transport = Settings.Secure.getString(context.getContentResolver(), "backup_transport");
        if (TextUtils.isEmpty(transport)) {
            transport = null;
        }
        Slog.v(TAG, "Starting with transport " + transport);
        Context context2 = context;
        Trampoline trampoline = parent;
        HandlerThread handlerThread = backupThread;
        BackupManagerService backupManagerService = new BackupManagerService(context2, trampoline, handlerThread, new File(Environment.getDataDirectory(), BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD), new File(Environment.getDownloadCacheDirectory(), "backup_stage"), new TransportManager(context, transportWhitelist, transport));
        return backupManagerService;
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0223, code lost:
        r0 = th;
     */
    @VisibleForTesting
    BackupManagerService(Context context, Trampoline parent, HandlerThread backupThread, File baseStateDir, File dataDir, TransportManager transportManager) {
        Context context2 = context;
        this.mContext = context2;
        this.mPackageManager = context.getPackageManager();
        this.mPackageManagerBinder = AppGlobals.getPackageManager();
        this.mActivityManager = ActivityManager.getService();
        this.mAlarmManager = (AlarmManager) context2.getSystemService("alarm");
        this.mPowerManager = (PowerManager) context2.getSystemService("power");
        this.mStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        this.mBackupManagerBinder = Trampoline.asInterface(parent.asBinder());
        this.mAgentTimeoutParameters = new BackupAgentTimeoutParameters(Handler.getMain(), this.mContext.getContentResolver());
        this.mAgentTimeoutParameters.start();
        this.mBackupHandler = new BackupHandler(this, backupThread.getLooper());
        ContentResolver resolver = context.getContentResolver();
        this.mProvisioned = Settings.Global.getInt(resolver, "device_provisioned", 0) != 0;
        this.mAutoRestore = Settings.Secure.getInt(resolver, "backup_auto_restore", 1) != 0;
        this.mProvisionedObserver = new ProvisionedObserver(this, this.mBackupHandler);
        resolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mProvisionedObserver);
        this.mBaseStateDir = baseStateDir;
        this.mBaseStateDir.mkdirs();
        if (!SELinux.restorecon(this.mBaseStateDir)) {
            Slog.e(TAG, "SELinux restorecon failed on " + this.mBaseStateDir);
        }
        this.mDataDir = dataDir;
        this.mBackupPasswordManager = new BackupPasswordManager(this.mContext, this.mBaseStateDir, this.mRng);
        this.mRunBackupReceiver = new RunBackupReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RUN_BACKUP_ACTION);
        context2.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
        this.mRunInitReceiver = new RunInitializeReceiver(this);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(RUN_INITIALIZE_ACTION);
        context2.registerReceiver(this.mRunInitReceiver, filter2, "android.permission.BACKUP", null);
        Intent backupIntent = new Intent(RUN_BACKUP_ACTION);
        backupIntent.addFlags(1073741824);
        this.mRunBackupIntent = PendingIntent.getBroadcast(context2, 0, backupIntent, 0);
        Intent initIntent = new Intent(RUN_INITIALIZE_ACTION);
        initIntent.addFlags(1073741824);
        this.mRunInitIntent = PendingIntent.getBroadcast(context2, 0, initIntent, 0);
        this.mJournalDir = new File(this.mBaseStateDir, "pending");
        this.mJournalDir.mkdirs();
        this.mJournal = null;
        this.mConstants = new BackupManagerConstants(this.mBackupHandler, this.mContext.getContentResolver());
        this.mConstants.start();
        this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
        initPackageTracking();
        synchronized (this.mBackupParticipants) {
            try {
                addPackageParticipantsLocked(null);
            } catch (Throwable th) {
                th = th;
                TransportManager transportManager2 = transportManager;
                while (true) {
                    throw th;
                }
            }
        }
        this.mTransportManager = transportManager;
        this.mTransportManager.setOnTransportRegisteredListener(new OnTransportRegisteredListener() {
            public final void onTransportRegistered(String str, String str2) {
                BackupManagerService.this.onTransportRegistered(str, str2);
            }
        });
        this.mRegisterTransportsRequestedTime = SystemClock.elapsedRealtime();
        BackupHandler backupHandler = this.mBackupHandler;
        TransportManager transportManager3 = this.mTransportManager;
        Objects.requireNonNull(transportManager3);
        backupHandler.postDelayed(new Runnable() {
            public final void run() {
                TransportManager.this.registerTransports();
            }
        }, INITIALIZATION_DELAY_MILLIS);
        this.mBackupHandler.postDelayed(new Runnable() {
            public final void run() {
                BackupManagerService.this.parseLeftoverJournals();
            }
        }, INITIALIZATION_DELAY_MILLIS);
        this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
        this.mBackupPolicyEnforcer = new BackupPolicyEnforcer(context2);
    }

    private void initPackageTracking() {
        DataInputStream tokenStream;
        this.mTokenFile = new File(this.mBaseStateDir, "ancestral");
        try {
            tokenStream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.mTokenFile)));
            if (tokenStream.readInt() == 1) {
                this.mAncestralToken = tokenStream.readLong();
                this.mCurrentToken = tokenStream.readLong();
                int numPackages = tokenStream.readInt();
                if (numPackages >= 0) {
                    this.mAncestralPackages = new HashSet();
                    for (int i = 0; i < numPackages; i++) {
                        this.mAncestralPackages.add(tokenStream.readUTF());
                    }
                }
            }
            $closeResource(null, tokenStream);
        } catch (FileNotFoundException e) {
            Slog.v(TAG, "No ancestral data");
        } catch (IOException e2) {
            Slog.w(TAG, "Unable to read token file", e2);
        } catch (Throwable th) {
            $closeResource(r1, tokenStream);
            throw th;
        }
        this.mProcessedPackagesJournal = new ProcessedPackagesJournal(this.mBaseStateDir);
        this.mProcessedPackagesJournal.init();
        synchronized (this.mQueueLock) {
            this.mFullBackupQueue = readFullBackupSchedule();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0162, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0164, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0165, code lost:
        r5 = r8;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    private ArrayList<FullBackupEntry> readFullBackupSchedule() {
        FileInputStream fstream;
        Throwable th;
        int N;
        int version;
        boolean changed = false;
        ArrayList<FullBackupEntry> schedule = null;
        List<PackageInfo> apps = PackageManagerBackupAgent.getStorableApplications(this.mPackageManager);
        if (this.mFullBackupScheduleFile.exists()) {
            try {
                fstream = new FileInputStream(this.mFullBackupScheduleFile);
                Throwable th2 = null;
                try {
                    BufferedInputStream bufStream = new BufferedInputStream(fstream);
                    try {
                        DataInputStream in = new DataInputStream(bufStream);
                        try {
                            int version2 = in.readInt();
                            if (version2 != 1) {
                                Slog.e(TAG, "Unknown backup schedule version " + version2);
                                $closeResource(null, in);
                                $closeResource(null, bufStream);
                                $closeResource(null, fstream);
                                return null;
                            }
                            int N2 = in.readInt();
                            schedule = new ArrayList<>(N2);
                            HashSet hashSet = new HashSet(N2);
                            int i = 0;
                            int i2 = 0;
                            while (true) {
                                int i3 = i2;
                                if (i3 >= N2) {
                                    break;
                                }
                                try {
                                    String pkgName = in.readUTF();
                                    long lastBackup = in.readLong();
                                    String pkgName2 = pkgName;
                                    hashSet.add(pkgName2);
                                    try {
                                        PackageInfo pkg = this.mPackageManager.getPackageInfo(pkgName2, i);
                                        if (!AppBackupUtils.appGetsFullBackup(pkg) || !AppBackupUtils.appIsEligibleForBackup(pkg.applicationInfo, this.mPackageManager)) {
                                            version = version2;
                                            N = N2;
                                            long j = lastBackup;
                                            Slog.i(TAG, "Package " + pkgName2 + " no longer eligible for full backup");
                                            i2 = i3 + 1;
                                            version2 = version;
                                            N2 = N;
                                            th2 = null;
                                            i = 0;
                                        } else {
                                            version = version2;
                                            N = N2;
                                            try {
                                                schedule.add(new FullBackupEntry(pkgName2, lastBackup));
                                            } catch (PackageManager.NameNotFoundException e) {
                                                Slog.i(TAG, "Package " + pkgName2 + " not installed; dropping from full backup");
                                                i2 = i3 + 1;
                                                version2 = version;
                                                N2 = N;
                                                th2 = null;
                                                i = 0;
                                            }
                                            i2 = i3 + 1;
                                            version2 = version;
                                            N2 = N;
                                            th2 = null;
                                            i = 0;
                                        }
                                    } catch (PackageManager.NameNotFoundException e2) {
                                        version = version2;
                                        N = N2;
                                        long j2 = lastBackup;
                                        Slog.i(TAG, "Package " + pkgName2 + " not installed; dropping from full backup");
                                        i2 = i3 + 1;
                                        version2 = version;
                                        N2 = N;
                                        th2 = null;
                                        i = 0;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    th = null;
                                    th2 = null;
                                    $closeResource(th2, in);
                                    throw th;
                                }
                            }
                            int i4 = version2;
                            int i5 = N2;
                            try {
                                for (PackageInfo app : apps) {
                                    if (AppBackupUtils.appGetsFullBackup(app) && AppBackupUtils.appIsEligibleForBackup(app.applicationInfo, this.mPackageManager) && !hashSet.contains(app.packageName)) {
                                        schedule.add(new FullBackupEntry(app.packageName, 0));
                                        changed = true;
                                    }
                                }
                                Collections.sort(schedule);
                                th = null;
                            } catch (Throwable th4) {
                                th = th4;
                                th = null;
                                th2 = null;
                                $closeResource(th2, in);
                                throw th;
                            }
                            try {
                                $closeResource(null, in);
                                $closeResource(null, bufStream);
                                $closeResource(null, fstream);
                            } catch (Throwable th5) {
                                th = th5;
                                th2 = th;
                                $closeResource(th2, bufStream);
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            th = null;
                            $closeResource(th2, in);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        th = null;
                        $closeResource(th2, bufStream);
                        throw th;
                    }
                } catch (Throwable th8) {
                    Throwable th9 = th8;
                    throw th9;
                }
            } catch (Exception e3) {
                Slog.e(TAG, "Unable to read backup schedule", e3);
                this.mFullBackupScheduleFile.delete();
                schedule = null;
            }
        }
        if (schedule == null) {
            changed = true;
            schedule = new ArrayList<>(apps.size());
            for (PackageInfo info : apps) {
                if (AppBackupUtils.appGetsFullBackup(info) && AppBackupUtils.appIsEligibleForBackup(info.applicationInfo, this.mPackageManager)) {
                    schedule.add(new FullBackupEntry(info.packageName, 0));
                }
            }
        }
        if (changed) {
            writeFullBackupScheduleAsync();
        }
        return schedule;
        $closeResource(th, fstream);
        throw th;
    }

    /* access modifiers changed from: private */
    public void writeFullBackupScheduleAsync() {
        this.mBackupHandler.removeCallbacks(this.mFullBackupScheduleWriter);
        this.mBackupHandler.post(this.mFullBackupScheduleWriter);
    }

    /* access modifiers changed from: private */
    public void parseLeftoverJournals() {
        Iterator<DataChangedJournal> it = DataChangedJournal.listJournals(this.mJournalDir).iterator();
        while (it.hasNext()) {
            DataChangedJournal journal = it.next();
            if (!journal.equals(this.mJournal)) {
                try {
                    journal.forEach(new DataChangedJournal.Consumer() {
                        public final void accept(String str) {
                            BackupManagerService.lambda$parseLeftoverJournals$0(BackupManagerService.this, str);
                        }
                    });
                } catch (IOException e) {
                    Slog.e(TAG, "Can't read " + journal, e);
                }
            }
        }
    }

    public static /* synthetic */ void lambda$parseLeftoverJournals$0(BackupManagerService backupManagerService, String packageName) {
        Slog.i(TAG, "Found stale backup journal, scheduling");
        backupManagerService.dataChangedImpl(packageName);
    }

    public byte[] randomBytes(int bits) {
        byte[] array = new byte[(bits / 8)];
        this.mRng.nextBytes(array);
        return array;
    }

    public boolean setBackupPassword(String currentPw, String newPw) {
        return this.mBackupPasswordManager.setBackupPassword(currentPw, newPw);
    }

    public boolean hasBackupPassword() {
        return this.mBackupPasswordManager.hasBackupPassword();
    }

    public boolean backupPasswordMatches(String currentPw) {
        return this.mBackupPasswordManager.backupPasswordMatches(currentPw);
    }

    public void recordInitPending(boolean isPending, String transportName, String transportDirName) {
        synchronized (this.mQueueLock) {
            File initPendingFile = new File(new File(this.mBaseStateDir, transportDirName), INIT_SENTINEL_FILE_NAME);
            if (isPending) {
                this.mPendingInits.add(transportName);
                try {
                    new FileOutputStream(initPendingFile).close();
                } catch (IOException e) {
                }
            } else {
                initPendingFile.delete();
                this.mPendingInits.remove(transportName);
            }
        }
    }

    public void resetBackupState(File stateFileDir) {
        int i;
        synchronized (this.mQueueLock) {
            this.mProcessedPackagesJournal.reset();
            this.mCurrentToken = 0;
            writeRestoreTokens();
            i = 0;
            for (File sf : stateFileDir.listFiles()) {
                if (!sf.getName().equals(INIT_SENTINEL_FILE_NAME)) {
                    sf.delete();
                }
            }
        }
        synchronized (this.mBackupParticipants) {
            int N = this.mBackupParticipants.size();
            while (true) {
                int i2 = i;
                if (i2 < N) {
                    HashSet<String> participants = this.mBackupParticipants.valueAt(i2);
                    if (participants != null) {
                        Iterator<String> it = participants.iterator();
                        while (it.hasNext()) {
                            dataChangedImpl(it.next());
                        }
                    }
                    i = i2 + 1;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onTransportRegistered(String transportName, String transportDirName) {
        Slog.d(TAG, "Transport " + transportName + " registered " + (SystemClock.elapsedRealtime() - this.mRegisterTransportsRequestedTime) + "ms after first request (delay = " + INITIALIZATION_DELAY_MILLIS + "ms)");
        File stateDir = new File(this.mBaseStateDir, transportDirName);
        stateDir.mkdirs();
        if (new File(stateDir, INIT_SENTINEL_FILE_NAME).exists()) {
            synchronized (this.mQueueLock) {
                this.mPendingInits.add(transportName);
                this.mAlarmManager.set(0, System.currentTimeMillis() + 60000, this.mRunInitIntent);
            }
        }
    }

    /* access modifiers changed from: private */
    public void addPackageParticipantsLocked(String[] packageNames) {
        List<PackageInfo> targetApps = allAgentPackages();
        if (packageNames != null) {
            for (String packageName : packageNames) {
                addPackageParticipantsLockedInner(packageName, targetApps);
            }
            return;
        }
        addPackageParticipantsLockedInner(null, targetApps);
    }

    private void addPackageParticipantsLockedInner(String packageName, List<PackageInfo> targetPkgs) {
        for (PackageInfo pkg : targetPkgs) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                int uid = pkg.applicationInfo.uid;
                HashSet<String> set = this.mBackupParticipants.get(uid);
                if (set == null) {
                    set = new HashSet<>();
                    this.mBackupParticipants.put(uid, set);
                }
                set.add(pkg.packageName);
                this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(16, pkg.packageName));
            }
        }
    }

    /* access modifiers changed from: private */
    public void removePackageParticipantsLocked(String[] packageNames, int oldUid) {
        if (packageNames == null) {
            Slog.w(TAG, "removePackageParticipants with null list");
            return;
        }
        for (String pkg : packageNames) {
            HashSet<String> set = this.mBackupParticipants.get(oldUid);
            if (set != null && set.contains(pkg)) {
                removePackageFromSetLocked(set, pkg);
                if (set.isEmpty()) {
                    this.mBackupParticipants.remove(oldUid);
                }
            }
        }
    }

    private void removePackageFromSetLocked(HashSet<String> set, String packageName) {
        if (set.contains(packageName)) {
            set.remove(packageName);
            this.mPendingBackups.remove(packageName);
        }
    }

    private List<PackageInfo> allAgentPackages() {
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackages(134217728);
        for (int a = packages.size() - 1; a >= 0; a--) {
            PackageInfo pkg = packages.get(a);
            try {
                ApplicationInfo app = pkg.applicationInfo;
                if (!((app.flags & 32768) == 0 || app.backupAgentName == null)) {
                    if ((app.flags & 67108864) == 0) {
                        pkg.applicationInfo.sharedLibraryFiles = this.mPackageManager.getApplicationInfo(pkg.packageName, 1024).sharedLibraryFiles;
                    }
                }
                packages.remove(a);
            } catch (PackageManager.NameNotFoundException e) {
                packages.remove(a);
            }
        }
        return packages;
    }

    public void logBackupComplete(String packageName) {
        if (!packageName.equals(PACKAGE_MANAGER_SENTINEL)) {
            for (String receiver : this.mConstants.getBackupFinishedNotificationReceivers()) {
                Intent notification = new Intent();
                notification.setAction(BACKUP_FINISHED_ACTION);
                notification.setPackage(receiver);
                notification.addFlags(268435488);
                notification.putExtra("packageName", packageName);
                this.mContext.sendBroadcastAsUser(notification, UserHandle.OWNER);
            }
            this.mProcessedPackagesJournal.addPackage(packageName);
        }
    }

    public void writeRestoreTokens() {
        RandomAccessFile af;
        try {
            af = new RandomAccessFile(this.mTokenFile, "rwd");
            af.writeInt(1);
            af.writeLong(this.mAncestralToken);
            af.writeLong(this.mCurrentToken);
            if (this.mAncestralPackages == null) {
                af.writeInt(-1);
            } else {
                af.writeInt(this.mAncestralPackages.size());
                Slog.v(TAG, "Ancestral packages:  " + this.mAncestralPackages.size());
                for (String pkgName : this.mAncestralPackages) {
                    af.writeUTF(pkgName);
                }
            }
            $closeResource(null, af);
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write token file:", e);
        } catch (Throwable th) {
            $closeResource(r1, af);
            throw th;
        }
    }

    public IBackupAgent bindToAgentSynchronous(ApplicationInfo app, int mode) {
        IBackupAgent agent = null;
        synchronized (this.mAgentConnectLock) {
            this.mConnecting = true;
            this.mConnectedAgent = null;
            try {
                if (this.mActivityManager.bindBackupAgent(app.packageName, mode, 0)) {
                    Slog.d(TAG, "awaiting agent for " + app);
                    long timeoutMark = System.currentTimeMillis() + 10000;
                    while (this.mConnecting && this.mConnectedAgent == null && System.currentTimeMillis() < timeoutMark) {
                        try {
                            this.mAgentConnectLock.wait(5000);
                        } catch (InterruptedException e) {
                            Slog.w(TAG, "Interrupted: " + e);
                            this.mConnecting = false;
                            this.mConnectedAgent = null;
                        }
                    }
                    if (this.mConnecting) {
                        Slog.w(TAG, "Timeout waiting for agent " + app);
                        this.mConnectedAgent = null;
                    }
                    Slog.i(TAG, "got agent " + this.mConnectedAgent);
                    agent = this.mConnectedAgent;
                }
            } catch (RemoteException e2) {
            }
        }
        if (agent == null) {
            try {
                this.mActivityManager.clearPendingBackup();
            } catch (RemoteException e3) {
            }
        }
        return agent;
    }

    public void clearApplicationDataSynchronous(String packageName, boolean keepSystemState) {
        try {
            if ((this.mPackageManager.getPackageInfo(packageName, 0).applicationInfo.flags & 64) != 0) {
                ClearDataObserver observer = new ClearDataObserver(this);
                synchronized (this.mClearDataLock) {
                    this.mClearingData = true;
                    try {
                        this.mActivityManager.clearApplicationUserData(packageName, keepSystemState, observer, 0);
                    } catch (RemoteException e) {
                    }
                    long timeoutMark = System.currentTimeMillis() + 10000;
                    while (this.mClearingData && System.currentTimeMillis() < timeoutMark) {
                        try {
                            this.mClearDataLock.wait(5000);
                        } catch (InterruptedException e2) {
                            this.mClearingData = false;
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e3) {
            Slog.w(TAG, "Tried to clear data for " + packageName + " but not found");
        }
    }

    public long getAvailableRestoreToken(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreToken");
        long token = this.mAncestralToken;
        synchronized (this.mQueueLock) {
            if (this.mCurrentToken != 0 && this.mProcessedPackagesJournal.hasBeenProcessed(packageName)) {
                token = this.mCurrentToken;
            }
        }
        return token;
    }

    public int requestBackup(String[] packages, IBackupObserver observer, int flags) {
        return requestBackup(packages, observer, null, flags);
    }

    public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) {
        int logTag;
        String[] strArr = packages;
        IBackupObserver iBackupObserver = observer;
        IBackupManagerMonitor iBackupManagerMonitor = monitor;
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "requestBackup");
        if (strArr == null || strArr.length < 1) {
            Slog.e(TAG, "No packages named for backup request");
            BackupObserverUtils.sendBackupFinished(iBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            IBackupManagerMonitor monitorEvent = BackupManagerMonitorUtils.monitorEvent(iBackupManagerMonitor, 49, null, 1, null);
            throw new IllegalArgumentException("No packages are provided for backup");
        } else if (!this.mEnabled || !this.mProvisioned) {
            Slog.i(TAG, "Backup requested but e=" + this.mEnabled + " p=" + this.mProvisioned);
            BackupObserverUtils.sendBackupFinished(iBackupObserver, -2001);
            if (this.mProvisioned) {
                logTag = 13;
            } else {
                logTag = 14;
            }
            IBackupManagerMonitor monitorEvent2 = BackupManagerMonitorUtils.monitorEvent(iBackupManagerMonitor, logTag, null, 3, null);
            return -2001;
        } else {
            try {
                String transportDirName = this.mTransportManager.getTransportDirName(this.mTransportManager.getCurrentTransportName());
                TransportClient transportClient = this.mTransportManager.getCurrentTransportClientOrThrow("BMS.requestBackup()");
                $$Lambda$BackupManagerService$d1gjNfZ3ZYIuaY4s01CFoLZa4Z0 r11 = new OnTaskFinishedListener(transportClient) {
                    private final /* synthetic */ TransportClient f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onFinished(String str) {
                        BackupManagerService.this.mTransportManager.disposeOfTransportClient(this.f$1, str);
                    }
                };
                ArrayList<String> fullBackupList = new ArrayList<>();
                ArrayList<String> kvBackupList = new ArrayList<>();
                for (String packageName : strArr) {
                    if (PACKAGE_MANAGER_SENTINEL.equals(packageName)) {
                        kvBackupList.add(packageName);
                    } else {
                        try {
                            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 134217728);
                            if (!AppBackupUtils.appIsEligibleForBackup(packageInfo.applicationInfo, this.mPackageManager)) {
                                BackupObserverUtils.sendBackupOnPackageResult(iBackupObserver, packageName, -2001);
                            } else if (AppBackupUtils.appGetsFullBackup(packageInfo)) {
                                fullBackupList.add(packageInfo.packageName);
                            } else {
                                kvBackupList.add(packageInfo.packageName);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            BackupObserverUtils.sendBackupOnPackageResult(iBackupObserver, packageName, -2002);
                        }
                    }
                }
                EventLog.writeEvent(EventLogTags.BACKUP_REQUESTED, new Object[]{Integer.valueOf(strArr.length), Integer.valueOf(kvBackupList.size()), Integer.valueOf(fullBackupList.size())});
                boolean nonIncrementalBackup = (flags & 1) != 0;
                Message msg = this.mBackupHandler.obtainMessage(15);
                BackupParams backupParams = r3;
                ArrayList<String> arrayList = kvBackupList;
                ArrayList<String> arrayList2 = fullBackupList;
                $$Lambda$BackupManagerService$d1gjNfZ3ZYIuaY4s01CFoLZa4Z0 r20 = r11;
                BackupParams backupParams2 = new BackupParams(transportClient, transportDirName, kvBackupList, fullBackupList, iBackupObserver, iBackupManagerMonitor, r11, true, nonIncrementalBackup);
                msg.obj = backupParams;
                this.mBackupHandler.sendMessage(msg);
                return 0;
            } catch (TransportNotRegisteredException e2) {
                BackupObserverUtils.sendBackupFinished(iBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                IBackupManagerMonitor monitorEvent3 = BackupManagerMonitorUtils.monitorEvent(iBackupManagerMonitor, 50, null, 1, null);
                return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
        }
    }

    public void cancelBackups() {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "cancelBackups");
        long oldToken = Binder.clearCallingIdentity();
        try {
            List<Integer> operationsToCancel = new ArrayList<>();
            synchronized (this.mCurrentOpLock) {
                for (int i = 0; i < this.mCurrentOperations.size(); i++) {
                    int token = this.mCurrentOperations.keyAt(i);
                    if (this.mCurrentOperations.valueAt(i).type == 2) {
                        operationsToCancel.add(Integer.valueOf(token));
                    }
                }
            }
            for (Integer token2 : operationsToCancel) {
                handleCancel(token2.intValue(), true);
            }
            KeyValueBackupJob.schedule(this.mContext, AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT, this.mConstants);
            FullBackupJob.schedule(this.mContext, AppStandbyController.SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT, this.mConstants);
            Binder.restoreCallingIdentity(oldToken);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldToken);
            throw th;
        }
    }

    public void prepareOperationTimeout(int token, long interval, BackupRestoreTask callback, int operationType) {
        if (operationType == 0 || operationType == 1) {
            synchronized (this.mCurrentOpLock) {
                this.mCurrentOperations.put(token, new Operation(0, callback, operationType));
                this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(getMessageIdForOperationType(operationType), token, 0, callback), interval);
            }
            return;
        }
        Slog.wtf(TAG, "prepareOperationTimeout() doesn't support operation " + Integer.toHexString(token) + " of type " + operationType);
    }

    private int getMessageIdForOperationType(int operationType) {
        switch (operationType) {
            case 0:
                return 17;
            case 1:
                return 18;
            default:
                Slog.wtf(TAG, "getMessageIdForOperationType called on invalid operation type: " + operationType);
                return -1;
        }
    }

    public void removeOperation(int token) {
        synchronized (this.mCurrentOpLock) {
            if (this.mCurrentOperations.get(token) == null) {
                Slog.w(TAG, "Duplicate remove for operation. token=" + Integer.toHexString(token));
            }
            this.mCurrentOperations.remove(token);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0 = r1.state;
     */
    public boolean waitUntilOperationComplete(int token) {
        Operation op;
        int finalState = 0;
        synchronized (this.mCurrentOpLock) {
            while (true) {
                op = this.mCurrentOperations.get(token);
                if (op != null) {
                    if (op.state != 0) {
                        break;
                    }
                    try {
                        this.mCurrentOpLock.wait();
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
            }
        }
        removeOperation(token);
        if (op != null) {
            this.mBackupHandler.removeMessages(getMessageIdForOperationType(op.type));
        }
        if (finalState == 1) {
            return true;
        }
        return false;
    }

    public void handleCancel(int token, boolean cancelAll) {
        Operation op;
        synchronized (this.mCurrentOpLock) {
            op = this.mCurrentOperations.get(token);
            int state = op != null ? op.state : -1;
            if (state == 1) {
                Slog.w(TAG, "Operation already got an ack.Should have been removed from mCurrentOperations.");
                op = null;
                this.mCurrentOperations.delete(token);
            } else if (state == 0) {
                Slog.v(TAG, "Cancel: token=" + Integer.toHexString(token));
                op.state = -1;
                if (op.type == 0 || op.type == 1) {
                    this.mBackupHandler.removeMessages(getMessageIdForOperationType(op.type));
                }
            }
            this.mCurrentOpLock.notifyAll();
        }
        if (op != null && op.callback != null) {
            op.callback.handleCancel(cancelAll);
        }
    }

    public boolean isBackupOperationInProgress() {
        synchronized (this.mCurrentOpLock) {
            for (int i = 0; i < this.mCurrentOperations.size(); i++) {
                Operation op = this.mCurrentOperations.valueAt(i);
                if (op.type == 2 && op.state == 0) {
                    return true;
                }
            }
            return false;
        }
    }

    public void tearDownAgentAndKill(ApplicationInfo app) {
        if (app != null) {
            try {
                this.mActivityManager.unbindBackupAgent(app);
                if (app.uid >= 10000 && !app.packageName.equals("com.android.backupconfirm")) {
                    this.mActivityManager.killApplicationProcess(app.processName, app.uid);
                }
            } catch (RemoteException e) {
                Slog.d(TAG, "Lost app trying to shut down");
            }
        }
    }

    public boolean deviceIsEncrypted() {
        boolean z = true;
        try {
            if (this.mStorageManager.getEncryptionState() == 1 || this.mStorageManager.getPasswordType() == 1) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Slog.e(TAG, "Unable to communicate with storagemanager service: " + e.getMessage());
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
        return;
     */
    public void scheduleNextFullBackupJob(long transportMinLatency) {
        synchronized (this.mQueueLock) {
            try {
                if (this.mFullBackupQueue.size() > 0) {
                    long upcomingLastBackup = this.mFullBackupQueue.get(0).lastBackup;
                    long timeSinceLast = System.currentTimeMillis() - upcomingLastBackup;
                    long interval = this.mConstants.getFullBackupIntervalMilliseconds();
                    final long latency = Math.max(transportMinLatency, timeSinceLast < interval ? interval - timeSinceLast : 0);
                    long j = upcomingLastBackup;
                    this.mBackupHandler.postDelayed(new Runnable() {
                        public void run() {
                            FullBackupJob.schedule(BackupManagerService.this.mContext, latency, BackupManagerService.this.mConstants);
                        }
                    }, 2500);
                } else {
                    long j2 = transportMinLatency;
                    Slog.i(TAG, "Full backup queue empty; not scheduling");
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mQueueLock")
    public void dequeueFullBackupLocked(String packageName) {
        for (int i = this.mFullBackupQueue.size() - 1; i >= 0; i--) {
            if (packageName.equals(this.mFullBackupQueue.get(i).packageName)) {
                this.mFullBackupQueue.remove(i);
            }
        }
    }

    public void enqueueFullBackup(String packageName, long lastBackedUp) {
        FullBackupEntry newEntry = new FullBackupEntry(packageName, lastBackedUp);
        synchronized (this.mQueueLock) {
            dequeueFullBackupLocked(packageName);
            int which = -1;
            if (lastBackedUp > 0) {
                which = this.mFullBackupQueue.size() - 1;
                while (true) {
                    if (which < 0) {
                        break;
                    } else if (this.mFullBackupQueue.get(which).lastBackup <= lastBackedUp) {
                        this.mFullBackupQueue.add(which + 1, newEntry);
                        break;
                    } else {
                        which--;
                    }
                }
            }
            if (which < 0) {
                this.mFullBackupQueue.add(0, newEntry);
            }
        }
        writeFullBackupScheduleAsync();
    }

    private boolean fullBackupAllowable(String transportName) {
        if (!this.mTransportManager.isTransportRegistered(transportName)) {
            Slog.w(TAG, "Transport not registered; full data backup not performed");
            return false;
        }
        try {
            if (new File(new File(this.mBaseStateDir, this.mTransportManager.getTransportDirName(transportName)), PACKAGE_MANAGER_SENTINEL).length() > 0) {
                return true;
            }
            Slog.i(TAG, "Full backup requested but dataset not yet initialized");
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "Unable to get transport name: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        android.util.Slog.i(TAG, "Backup queue empty; doing nothing");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0071, code lost:
        r0 = false;
        r4 = r2;
        r2 = r1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x017a  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01ac  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x01ae  */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x028b A[LOOP:0: B:27:0x0062->B:159:0x028b, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x01dc A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00f4 A[SYNTHETIC, Splitter:B:79:0x00f4] */
    public boolean beginFullBackup(FullBackupJob scheduledJob) {
        long fullBackupInterval;
        long keyValueBackupInterval;
        Object obj;
        long latency;
        FullBackupEntry entry;
        long latency2;
        final long latency3;
        long latency4;
        boolean runBackup;
        boolean z;
        boolean headBusy;
        long nextEligible;
        SimpleDateFormat sdf;
        StringBuilder sb;
        long now = System.currentTimeMillis();
        synchronized (this.mConstants) {
            fullBackupInterval = this.mConstants.getFullBackupIntervalMilliseconds();
            keyValueBackupInterval = this.mConstants.getKeyValueBackupIntervalMilliseconds();
        }
        FullBackupEntry entry2 = null;
        long latency5 = fullBackupInterval;
        if (!this.mEnabled) {
        } else if (!this.mProvisioned) {
            long j = keyValueBackupInterval;
        } else {
            PowerSaveState result = this.mPowerManager.getPowerSaveState(4);
            if (result.batterySaverEnabled) {
                Slog.i(TAG, "Deferring scheduled full backups in battery saver mode");
                FullBackupJob.schedule(this.mContext, keyValueBackupInterval, this.mConstants);
                return false;
            }
            Slog.i(TAG, "Beginning scheduled full backup operation");
            Object obj2 = this.mQueueLock;
            synchronized (obj2) {
                try {
                    if (this.mRunningFullBackupTask != null) {
                        try {
                            Slog.e(TAG, "Backup triggered but one already/still running!");
                            return false;
                        } catch (Throwable th) {
                            th = th;
                            obj = obj2;
                            PowerSaveState powerSaveState = result;
                            long j2 = keyValueBackupInterval;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    } else {
                        boolean runBackup2 = true;
                        while (true) {
                            try {
                                if (this.mFullBackupQueue.size() == 0) {
                                    break;
                                }
                                boolean headBusy2 = false;
                                String transportName = this.mTransportManager.getCurrentTransportName();
                                if (!fullBackupAllowable(transportName)) {
                                    runBackup2 = false;
                                    latency5 = keyValueBackupInterval;
                                }
                                if (runBackup2) {
                                    try {
                                        try {
                                            entry2 = this.mFullBackupQueue.get(0);
                                            latency4 = latency5;
                                            try {
                                                long timeSinceRun = now - entry2.lastBackup;
                                                boolean runBackup3 = timeSinceRun >= fullBackupInterval;
                                                if (!runBackup3) {
                                                    long latency6 = fullBackupInterval - timeSinceRun;
                                                    entry = entry2;
                                                    runBackup2 = runBackup3;
                                                    latency = latency6;
                                                    break;
                                                }
                                                try {
                                                    long j3 = timeSinceRun;
                                                    try {
                                                        PackageInfo appInfo = this.mPackageManager.getPackageInfo(entry2.packageName, 0);
                                                        if (!AppBackupUtils.appGetsFullBackup(appInfo)) {
                                                            try {
                                                                this.mFullBackupQueue.remove(0);
                                                                headBusy2 = true;
                                                                runBackup2 = runBackup3;
                                                            } catch (PackageManager.NameNotFoundException e) {
                                                                boolean z2 = runBackup3;
                                                                String str = transportName;
                                                                runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                                if (headBusy2) {
                                                                }
                                                            } catch (RemoteException e2) {
                                                                runBackup = runBackup3;
                                                                String str2 = transportName;
                                                                runBackup2 = runBackup;
                                                                if (headBusy2) {
                                                                }
                                                            }
                                                        } else {
                                                            int privFlags = appInfo.applicationInfo.privateFlags;
                                                            if ((privFlags & 8192) == 0) {
                                                                int i = privFlags;
                                                                if (this.mActivityManager.isAppForeground(appInfo.applicationInfo.uid)) {
                                                                    z = true;
                                                                    headBusy2 = z;
                                                                    if (!headBusy2) {
                                                                        try {
                                                                            PackageInfo packageInfo = appInfo;
                                                                            runBackup = runBackup3;
                                                                        } catch (PackageManager.NameNotFoundException e3) {
                                                                            boolean z3 = runBackup3;
                                                                            boolean z4 = headBusy2;
                                                                            String str3 = transportName;
                                                                            runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                                            if (headBusy2) {
                                                                            }
                                                                        } catch (RemoteException e4) {
                                                                            runBackup = runBackup3;
                                                                            boolean z5 = headBusy2;
                                                                            String str4 = transportName;
                                                                            runBackup2 = runBackup;
                                                                            if (headBusy2) {
                                                                            }
                                                                        }
                                                                        try {
                                                                            headBusy = headBusy2;
                                                                            nextEligible = System.currentTimeMillis() + AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT + ((long) this.mTokenGenerator.nextInt(BUSY_BACKOFF_FUZZ));
                                                                            try {
                                                                                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                sb = new StringBuilder();
                                                                                String str5 = transportName;
                                                                            } catch (PackageManager.NameNotFoundException e5) {
                                                                                String str6 = transportName;
                                                                                headBusy2 = headBusy;
                                                                                runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                                                if (headBusy2) {
                                                                                }
                                                                            } catch (RemoteException e6) {
                                                                                String str7 = transportName;
                                                                                headBusy2 = headBusy;
                                                                                runBackup2 = runBackup;
                                                                                if (headBusy2) {
                                                                                }
                                                                            }
                                                                        } catch (PackageManager.NameNotFoundException e7) {
                                                                            boolean z6 = headBusy2;
                                                                            String str8 = transportName;
                                                                            runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                                            if (headBusy2) {
                                                                            }
                                                                        } catch (RemoteException e8) {
                                                                            boolean z7 = headBusy2;
                                                                            String str9 = transportName;
                                                                            runBackup2 = runBackup;
                                                                            if (headBusy2) {
                                                                            }
                                                                        }
                                                                        try {
                                                                            sb.append("Full backup time but ");
                                                                            sb.append(entry2.packageName);
                                                                            sb.append(" is busy; deferring to ");
                                                                            sb.append(sdf.format(new Date(nextEligible)));
                                                                            Slog.i(TAG, sb.toString());
                                                                            enqueueFullBackup(entry2.packageName, nextEligible - fullBackupInterval);
                                                                        } catch (PackageManager.NameNotFoundException e9) {
                                                                            headBusy2 = headBusy;
                                                                        } catch (RemoteException e10) {
                                                                            headBusy2 = headBusy;
                                                                            runBackup2 = runBackup;
                                                                            if (headBusy2) {
                                                                            }
                                                                        }
                                                                    } else {
                                                                        runBackup = runBackup3;
                                                                        headBusy = headBusy2;
                                                                        String str10 = transportName;
                                                                    }
                                                                    runBackup2 = runBackup;
                                                                    headBusy2 = headBusy;
                                                                }
                                                            }
                                                            z = false;
                                                            headBusy2 = z;
                                                            if (!headBusy2) {
                                                            }
                                                            runBackup2 = runBackup;
                                                            headBusy2 = headBusy;
                                                        }
                                                    } catch (PackageManager.NameNotFoundException e11) {
                                                        boolean z8 = runBackup3;
                                                        String str11 = transportName;
                                                        runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                        if (headBusy2) {
                                                        }
                                                    } catch (RemoteException e12) {
                                                        runBackup = runBackup3;
                                                        String str12 = transportName;
                                                        runBackup2 = runBackup;
                                                        if (headBusy2) {
                                                        }
                                                    }
                                                } catch (PackageManager.NameNotFoundException e13) {
                                                    long j4 = timeSinceRun;
                                                    boolean z9 = runBackup3;
                                                    String str13 = transportName;
                                                    runBackup2 = this.mFullBackupQueue.size() <= 1;
                                                    if (headBusy2) {
                                                    }
                                                } catch (RemoteException e14) {
                                                    long j5 = timeSinceRun;
                                                    runBackup = runBackup3;
                                                    String str14 = transportName;
                                                    runBackup2 = runBackup;
                                                    if (headBusy2) {
                                                    }
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                obj = obj2;
                                                PowerSaveState powerSaveState2 = result;
                                                long j6 = keyValueBackupInterval;
                                                long j7 = latency4;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            long j8 = latency5;
                                            obj = obj2;
                                            PowerSaveState powerSaveState3 = result;
                                            long j9 = keyValueBackupInterval;
                                            FullBackupEntry fullBackupEntry = entry2;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                        FullBackupEntry fullBackupEntry2 = entry2;
                                        long j10 = latency5;
                                        obj = obj2;
                                        PowerSaveState powerSaveState4 = result;
                                        long j11 = keyValueBackupInterval;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                } else {
                                    latency4 = latency5;
                                }
                                if (headBusy2) {
                                    entry = entry2;
                                    latency = latency4;
                                    break;
                                }
                                latency5 = latency4;
                            } catch (Throwable th6) {
                                th = th6;
                                FullBackupEntry fullBackupEntry3 = entry2;
                                obj = obj2;
                                PowerSaveState powerSaveState5 = result;
                                long j12 = keyValueBackupInterval;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        if (!runBackup2) {
                            try {
                                Slog.i(TAG, "Nothing pending full backup; rescheduling +" + latency);
                                latency2 = latency;
                                latency3 = latency;
                            } catch (Throwable th7) {
                                th = th7;
                                FullBackupEntry fullBackupEntry4 = entry;
                                obj = obj2;
                                PowerSaveState powerSaveState6 = result;
                                long j13 = keyValueBackupInterval;
                                long j14 = latency;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                            try {
                                this.mBackupHandler.post(new Runnable() {
                                    public void run() {
                                        FullBackupJob.schedule(BackupManagerService.this.mContext, latency3, BackupManagerService.this.mConstants);
                                    }
                                });
                                return false;
                            } catch (Throwable th8) {
                                th = th8;
                                FullBackupEntry fullBackupEntry5 = entry;
                                obj = obj2;
                                PowerSaveState powerSaveState7 = result;
                                long j15 = keyValueBackupInterval;
                                long j16 = latency2;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        } else {
                            long latency7 = latency;
                            try {
                                this.mFullBackupQueue.remove(0);
                                obj = obj2;
                                PowerSaveState powerSaveState8 = result;
                                long j17 = keyValueBackupInterval;
                                try {
                                    this.mRunningFullBackupTask = PerformFullTransportBackupTask.newWithCurrentTransport(this, null, new String[]{entry.packageName}, true, scheduledJob, new CountDownLatch(1), null, null, false, "BMS.beginFullBackup()");
                                    this.mWakelock.acquire();
                                    new Thread(this.mRunningFullBackupTask).start();
                                    return true;
                                } catch (Throwable th9) {
                                    th = th9;
                                    FullBackupEntry fullBackupEntry6 = entry;
                                    long j18 = latency7;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th10) {
                                th = th10;
                                obj = obj2;
                                PowerSaveState powerSaveState9 = result;
                                long j19 = keyValueBackupInterval;
                                FullBackupEntry fullBackupEntry7 = entry;
                                long j20 = latency7;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                } catch (Throwable th11) {
                    th = th11;
                    obj = obj2;
                    PowerSaveState powerSaveState10 = result;
                    long j21 = keyValueBackupInterval;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        }
        return false;
    }

    public void endFullBackup() {
        new Thread(new Runnable() {
            public void run() {
                PerformFullTransportBackupTask pftbt = null;
                synchronized (BackupManagerService.this.mQueueLock) {
                    if (BackupManagerService.this.mRunningFullBackupTask != null) {
                        pftbt = BackupManagerService.this.mRunningFullBackupTask;
                    }
                }
                if (pftbt != null) {
                    Slog.i(BackupManagerService.TAG, "Telling running backup to stop");
                    pftbt.handleCancel(true);
                }
            }
        }, "end-full-backup").start();
    }

    public void restoreWidgetData(String packageName, byte[] widgetData) {
        AppWidgetBackupBridge.restoreWidgetState(packageName, widgetData, 0);
    }

    public void dataChangedImpl(String packageName) {
        dataChangedImpl(packageName, dataChangedTargets(packageName));
    }

    /* access modifiers changed from: private */
    public void dataChangedImpl(String packageName, HashSet<String> targets) {
        if (targets == null) {
            Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "' uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mQueueLock) {
            if (targets.contains(packageName)) {
                if (this.mPendingBackups.put(packageName, new BackupRequest(packageName)) == null) {
                    writeToJournalLocked(packageName);
                }
            }
        }
        KeyValueBackupJob.schedule(this.mContext, this.mConstants);
    }

    private HashSet<String> dataChangedTargets(String packageName) {
        HashSet<String> union;
        HashSet<String> hashSet;
        if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1) {
            synchronized (this.mBackupParticipants) {
                hashSet = this.mBackupParticipants.get(Binder.getCallingUid());
            }
            return hashSet;
        } else if (PACKAGE_MANAGER_SENTINEL.equals(packageName)) {
            return Sets.newHashSet(new String[]{PACKAGE_MANAGER_SENTINEL});
        } else {
            synchronized (this.mBackupParticipants) {
                union = SparseArrayUtils.union(this.mBackupParticipants);
            }
            return union;
        }
    }

    private void writeToJournalLocked(String str) {
        try {
            if (this.mJournal == null) {
                this.mJournal = DataChangedJournal.newJournal(this.mJournalDir);
            }
            this.mJournal.addPackage(str);
        } catch (IOException e) {
            Slog.e(TAG, "Can't write " + str + " to backup journal", e);
            this.mJournal = null;
        }
    }

    public void dataChanged(final String packageName) {
        if (UserHandle.getCallingUserId() == 0) {
            final HashSet<String> targets = dataChangedTargets(packageName);
            if (targets == null) {
                Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "' uid=" + Binder.getCallingUid());
                return;
            }
            this.mBackupHandler.post(new Runnable() {
                public void run() {
                    BackupManagerService.this.dataChangedImpl(packageName, targets);
                }
            });
        }
    }

    public void initializeTransports(String[] transportNames, IBackupObserver observer) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "initializeTransport");
        Slog.v(TAG, "initializeTransport(): " + Arrays.asList(transportNames));
        long oldId = Binder.clearCallingIdentity();
        try {
            this.mWakelock.acquire();
            this.mBackupHandler.post(new PerformInitializeTask(this, transportNames, observer, new OnTaskFinishedListener() {
                public final void onFinished(String str) {
                    BackupManagerService.this.mWakelock.release();
                }
            }));
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    public void clearBackupData(String transportName, String packageName) {
        Set<String> apps;
        Slog.v(TAG, "clearBackupData() of " + packageName + " on " + transportName);
        try {
            PackageInfo info = this.mPackageManager.getPackageInfo(packageName, 134217728);
            if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1) {
                apps = this.mBackupParticipants.get(Binder.getCallingUid());
            } else {
                apps = this.mProcessedPackagesJournal.getPackagesCopy();
            }
            if (apps.contains(packageName)) {
                this.mBackupHandler.removeMessages(12);
                synchronized (this.mQueueLock) {
                    TransportClient transportClient = this.mTransportManager.getTransportClient(transportName, "BMS.clearBackupData()");
                    if (transportClient == null) {
                        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(12, new ClearRetryParams(transportName, packageName)), AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT);
                        return;
                    }
                    long oldId = Binder.clearCallingIdentity();
                    OnTaskFinishedListener listener = new OnTaskFinishedListener(transportClient) {
                        private final /* synthetic */ TransportClient f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void onFinished(String str) {
                            BackupManagerService.this.mTransportManager.disposeOfTransportClient(this.f$1, str);
                        }
                    };
                    this.mWakelock.acquire();
                    this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(4, new ClearParams(transportClient, info, listener)));
                    Binder.restoreCallingIdentity(oldId);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.d(TAG, "No such package '" + packageName + "' - not clearing backup data");
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void backupNow() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "backupNow");
        if (this.mPowerManager.getPowerSaveState(5).batterySaverEnabled) {
            Slog.v(TAG, "Not running backup while in battery save mode");
            KeyValueBackupJob.schedule(this.mContext, this.mConstants);
            return;
        }
        Slog.v(TAG, "Scheduling immediate backup pass");
        synchronized (this.mQueueLock) {
            try {
                this.mRunBackupIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Slog.e(TAG, "run-backup intent cancelled!");
            }
            KeyValueBackupJob.cancel(this.mContext);
        }
    }

    public boolean deviceIsProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, boolean doKeyValue, String[] pkgList) {
        long oldId;
        Throwable th;
        boolean z = includeShared;
        boolean z2 = doAllApps;
        String[] strArr = pkgList;
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "adbBackup");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Backup supported only for the device owner");
        } else if (z2 || z || !(strArr == null || strArr.length == 0)) {
            long oldId2 = Binder.clearCallingIdentity();
            try {
                if (!deviceIsProvisioned()) {
                    try {
                        Slog.i(TAG, "Backup not supported before setup");
                        try {
                            fd.close();
                        } catch (IOException e) {
                            IOException iOException = e;
                            Slog.e(TAG, "IO error closing output for adb backup: " + e.getMessage());
                        }
                        Binder.restoreCallingIdentity(oldId2);
                        Slog.d(TAG, "Adb backup processing complete.");
                    } catch (Throwable th2) {
                        th = th2;
                        oldId = oldId2;
                        try {
                            fd.close();
                        } catch (IOException e2) {
                            IOException iOException2 = e2;
                            Slog.e(TAG, "IO error closing output for adb backup: " + e2.getMessage());
                        }
                        Binder.restoreCallingIdentity(oldId);
                        Slog.d(TAG, "Adb backup processing complete.");
                        throw th;
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Requesting backup: apks=");
                    boolean z3 = includeApks;
                    sb.append(z3);
                    sb.append(" obb=");
                    boolean z4 = includeObbs;
                    sb.append(z4);
                    sb.append(" shared=");
                    sb.append(z);
                    sb.append(" all=");
                    sb.append(z2);
                    sb.append(" system=");
                    sb.append(includeSystem);
                    sb.append(" includekeyvalue=");
                    sb.append(doKeyValue);
                    sb.append(" pkgs=");
                    sb.append(strArr);
                    Slog.v(TAG, sb.toString());
                    Slog.i(TAG, "Beginning adb backup...");
                    r2 = r2;
                    boolean z5 = z;
                    oldId = oldId2;
                    try {
                        AdbBackupParams adbBackupParams = new AdbBackupParams(fd, z3, z4, z5, doWidgets, z2, includeSystem, compress, doKeyValue, strArr);
                        AdbBackupParams params = adbBackupParams;
                        int token = generateRandomIntegerToken();
                        synchronized (this.mAdbBackupRestoreConfirmations) {
                            this.mAdbBackupRestoreConfirmations.put(token, params);
                        }
                        Slog.d(TAG, "Starting backup confirmation UI, token=" + token);
                        if (!startConfirmationUi(token, "fullback")) {
                            Slog.e(TAG, "Unable to launch backup confirmation UI");
                            this.mAdbBackupRestoreConfirmations.delete(token);
                            try {
                                fd.close();
                            } catch (IOException e3) {
                                IOException iOException3 = e3;
                                Slog.e(TAG, "IO error closing output for adb backup: " + e3.getMessage());
                            }
                            Binder.restoreCallingIdentity(oldId);
                            Slog.d(TAG, "Adb backup processing complete.");
                            return;
                        }
                        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
                        startConfirmationTimeout(token, params);
                        Slog.d(TAG, "Waiting for backup completion...");
                        waitForCompletion(params);
                        try {
                            fd.close();
                        } catch (IOException e4) {
                            IOException iOException4 = e4;
                            Slog.e(TAG, "IO error closing output for adb backup: " + e4.getMessage());
                        }
                        Binder.restoreCallingIdentity(oldId);
                        Slog.d(TAG, "Adb backup processing complete.");
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
            } catch (Throwable th4) {
                oldId = oldId2;
                th = th4;
                fd.close();
                Binder.restoreCallingIdentity(oldId);
                Slog.d(TAG, "Adb backup processing complete.");
                throw th;
            }
        } else {
            throw new IllegalArgumentException("Backup requested but neither shared nor any apps named");
        }
    }

    public void fullTransportBackup(String[] pkgNames) {
        long oldId;
        String[] strArr = pkgNames;
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "fullTransportBackup");
        if (UserHandle.getCallingUserId() == 0) {
            if (!fullBackupAllowable(this.mTransportManager.getCurrentTransportName())) {
                Slog.i(TAG, "Full backup not currently possible -- key/value backup not yet run?");
            } else {
                Slog.d(TAG, "fullTransportBackup()");
                long oldId2 = Binder.clearCallingIdentity();
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    long oldId3 = oldId2;
                    try {
                        Runnable task = PerformFullTransportBackupTask.newWithCurrentTransport(this, null, strArr, false, null, latch, null, null, false, "BMS.fullTransportBackup()");
                        this.mWakelock.acquire();
                        new Thread(task, "full-transport-master").start();
                        while (true) {
                            try {
                                latch.await();
                                break;
                            } catch (InterruptedException e) {
                                oldId3 = oldId3;
                            }
                        }
                        long now = System.currentTimeMillis();
                        int length = strArr.length;
                        int i = 0;
                        while (i < length) {
                            try {
                                enqueueFullBackup(strArr[i], now);
                                i++;
                            } catch (Throwable th) {
                                th = th;
                                oldId = oldId3;
                                Binder.restoreCallingIdentity(oldId);
                                throw th;
                            }
                        }
                        Binder.restoreCallingIdentity(oldId3);
                    } catch (Throwable th2) {
                        th = th2;
                        oldId = oldId3;
                        Binder.restoreCallingIdentity(oldId);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    oldId = oldId2;
                    Binder.restoreCallingIdentity(oldId);
                    throw th;
                }
            }
            Slog.d(TAG, "Done with full transport backup.");
            return;
        }
        throw new IllegalStateException("Restore supported only for the device owner");
    }

    public void adbRestore(ParcelFileDescriptor fd) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "adbRestore");
        if (UserHandle.getCallingUserId() == 0) {
            long oldId = Binder.clearCallingIdentity();
            try {
                if (!deviceIsProvisioned()) {
                    Slog.i(TAG, "Full restore not permitted before setup");
                    try {
                        fd.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "Error trying to close fd after adb restore: " + e);
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.i(TAG, "adb restore processing complete.");
                    return;
                }
                Slog.i(TAG, "Beginning restore...");
                AdbRestoreParams params = new AdbRestoreParams(fd);
                int token = generateRandomIntegerToken();
                synchronized (this.mAdbBackupRestoreConfirmations) {
                    this.mAdbBackupRestoreConfirmations.put(token, params);
                }
                Slog.d(TAG, "Starting restore confirmation UI, token=" + token);
                if (!startConfirmationUi(token, "fullrest")) {
                    Slog.e(TAG, "Unable to launch restore confirmation");
                    this.mAdbBackupRestoreConfirmations.delete(token);
                    try {
                        fd.close();
                    } catch (IOException e2) {
                        Slog.w(TAG, "Error trying to close fd after adb restore: " + e2);
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.i(TAG, "adb restore processing complete.");
                    return;
                }
                this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
                startConfirmationTimeout(token, params);
                Slog.d(TAG, "Waiting for restore completion...");
                waitForCompletion(params);
                try {
                    fd.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "Error trying to close fd after adb restore: " + e3);
                }
                Binder.restoreCallingIdentity(oldId);
                Slog.i(TAG, "adb restore processing complete.");
            } catch (Throwable th) {
                try {
                    fd.close();
                } catch (IOException e4) {
                    Slog.w(TAG, "Error trying to close fd after adb restore: " + e4);
                }
                Binder.restoreCallingIdentity(oldId);
                Slog.i(TAG, "adb restore processing complete.");
                throw th;
            }
        } else {
            throw new IllegalStateException("Restore supported only for the device owner");
        }
    }

    private boolean startConfirmationUi(int token, String action) {
        try {
            Intent confIntent = new Intent(action);
            confIntent.setClassName("com.android.backupconfirm", "com.android.backupconfirm.BackupRestoreConfirmation");
            confIntent.putExtra("conftoken", token);
            confIntent.addFlags(536870912);
            this.mContext.startActivityAsUser(confIntent, UserHandle.SYSTEM);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    private void startConfirmationTimeout(int token, AdbParams params) {
        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(9, token, 0, params), 60000);
    }

    private void waitForCompletion(AdbParams params) {
        synchronized (params.latch) {
            while (!params.latch.get()) {
                try {
                    params.latch.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void signalAdbBackupRestoreCompletion(AdbParams params) {
        synchronized (params.latch) {
            params.latch.set(true);
            params.latch.notifyAll();
        }
    }

    public void acknowledgeAdbBackupOrRestore(int token, boolean allow, String curPassword, String encPpassword, IFullBackupRestoreObserver observer) {
        int verb;
        Slog.d(TAG, "acknowledgeAdbBackupOrRestore : token=" + token + " allow=" + allow);
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "acknowledgeAdbBackupOrRestore");
        long oldId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mAdbBackupRestoreConfirmations) {
                AdbParams params = this.mAdbBackupRestoreConfirmations.get(token);
                if (params != null) {
                    this.mBackupHandler.removeMessages(9, params);
                    this.mAdbBackupRestoreConfirmations.delete(token);
                    if (allow) {
                        if (params instanceof AdbBackupParams) {
                            verb = 2;
                        } else {
                            verb = 10;
                        }
                        params.observer = observer;
                        params.curPassword = curPassword;
                        params.encryptPassword = encPpassword;
                        this.mWakelock.acquire();
                        this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(verb, params));
                    } else {
                        Slog.w(TAG, "User rejected full backup/restore operation");
                        signalAdbBackupRestoreCompletion(params);
                    }
                } else {
                    Slog.w(TAG, "Attempted to ack full backup/restore with invalid token");
                }
            }
            Binder.restoreCallingIdentity(oldId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
            throw th;
        }
    }

    private static boolean backupSettingMigrated(int userId) {
        return new File(new File(Environment.getDataDirectory(), BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD), BACKUP_ENABLE_FILE).exists();
    }

    private static boolean readBackupEnableState(int userId) {
        FileInputStream fin;
        boolean z;
        File enableFile = new File(new File(Environment.getDataDirectory(), BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD), BACKUP_ENABLE_FILE);
        if (enableFile.exists()) {
            try {
                fin = new FileInputStream(enableFile);
                if (fin.read() != 0) {
                    z = true;
                } else {
                    z = false;
                }
                $closeResource(null, fin);
                return z;
            } catch (IOException e) {
                Slog.e(TAG, "Cannot read enable state; assuming disabled");
            } catch (Throwable th) {
                $closeResource(r4, fin);
                throw th;
            }
        } else {
            Slog.i(TAG, "isBackupEnabled() => false due to absent settings file");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        r8 = r6;
        r6 = r5;
        r5 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x002c, code lost:
        r5 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002d, code lost:
        r6 = null;
     */
    private static void writeBackupEnableState(boolean enable, int userId) {
        Throwable th;
        Throwable th2;
        File base = new File(Environment.getDataDirectory(), BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD);
        File enableFile = new File(base, BACKUP_ENABLE_FILE);
        File stage = new File(base, "backup_enabled-stage");
        try {
            FileOutputStream fout = new FileOutputStream(stage);
            fout.write(enable);
            fout.close();
            stage.renameTo(enableFile);
            $closeResource(null, fout);
            return;
            $closeResource(th, fout);
            throw th2;
        } catch (IOException | RuntimeException e) {
            Slog.e(TAG, "Unable to record backup enable state; reverting to disabled: " + e.getMessage());
            Settings.Secure.putStringForUser(sInstance.mContext.getContentResolver(), BACKUP_ENABLE_FILE, null, userId);
            enableFile.delete();
            stage.delete();
        }
    }

    public void setBackupEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupEnabled");
        if (enable || this.mBackupPolicyEnforcer.getMandatoryBackupTransport() == null) {
            Slog.i(TAG, "Backup enabled => " + enable);
            long oldId = Binder.clearCallingIdentity();
            try {
                boolean wasEnabled = this.mEnabled;
                synchronized (this) {
                    writeBackupEnableState(enable, 0);
                    this.mEnabled = enable;
                }
                synchronized (this.mQueueLock) {
                    if (enable && !wasEnabled) {
                        if (this.mProvisioned) {
                            KeyValueBackupJob.schedule(this.mContext, this.mConstants);
                            scheduleNextFullBackupJob(0);
                        }
                    }
                    if (!enable) {
                        KeyValueBackupJob.cancel(this.mContext);
                        if (wasEnabled && this.mProvisioned) {
                            List<String> transportNames = new ArrayList<>();
                            List<String> transportDirNames = new ArrayList<>();
                            this.mTransportManager.forEachRegisteredTransport(new Consumer(transportNames, transportDirNames) {
                                private final /* synthetic */ List f$1;
                                private final /* synthetic */ List f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final void accept(Object obj) {
                                    BackupManagerService.lambda$setBackupEnabled$4(BackupManagerService.this, this.f$1, this.f$2, (String) obj);
                                }
                            });
                            for (int i = 0; i < transportNames.size(); i++) {
                                recordInitPending(true, transportNames.get(i), transportDirNames.get(i));
                            }
                            this.mAlarmManager.set(0, System.currentTimeMillis(), this.mRunInitIntent);
                        }
                    }
                }
                Binder.restoreCallingIdentity(oldId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(oldId);
                throw th;
            }
        } else {
            Slog.w(TAG, "Cannot disable backups when the mandatory backups policy is active.");
        }
    }

    public static /* synthetic */ void lambda$setBackupEnabled$4(BackupManagerService backupManagerService, List transportNames, List transportDirNames, String name) {
        try {
            String dirName = backupManagerService.mTransportManager.getTransportDirName(name);
            transportNames.add(name);
            transportDirNames.add(dirName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "Unexpected unregistered transport", e);
        }
    }

    public void setAutoRestore(boolean doAutoRestore) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setAutoRestore");
        Slog.i(TAG, "Auto restore => " + doAutoRestore);
        long oldId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "backup_auto_restore", doAutoRestore);
                this.mAutoRestore = doAutoRestore;
            }
            Binder.restoreCallingIdentity(oldId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
            throw th;
        }
    }

    public void setBackupProvisioned(boolean available) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupProvisioned");
    }

    public boolean isBackupEnabled() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "isBackupEnabled");
        return this.mEnabled;
    }

    public String getCurrentTransport() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getCurrentTransport");
        return this.mTransportManager.getCurrentTransportName();
    }

    public String[] listAllTransports() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "listAllTransports");
        return this.mTransportManager.getRegisteredTransportNames();
    }

    public ComponentName[] listAllTransportComponents() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "listAllTransportComponents");
        return this.mTransportManager.getRegisteredTransportComponents();
    }

    public String[] getTransportWhitelist() {
        Set<ComponentName> whitelistedComponents = this.mTransportManager.getTransportWhitelist();
        String[] whitelistedTransports = new String[whitelistedComponents.size()];
        int i = 0;
        for (ComponentName component : whitelistedComponents) {
            whitelistedTransports[i] = component.flattenToShortString();
            i++;
        }
        return whitelistedTransports;
    }

    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        updateTransportAttributes(Binder.getCallingUid(), transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateTransportAttributes(int callingUid, ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        long oldId;
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "updateTransportAttributes");
        ComponentName componentName = transportComponent;
        Preconditions.checkNotNull(componentName, "transportComponent can't be null");
        String str = name;
        Preconditions.checkNotNull(str, "name can't be null");
        String str2 = currentDestinationString;
        Preconditions.checkNotNull(str2, "currentDestinationString can't be null");
        boolean z = true;
        if ((dataManagementIntent == null) != (dataManagementLabel == null)) {
            z = false;
        }
        Preconditions.checkArgument(z, "dataManagementLabel should be null iff dataManagementIntent is null");
        try {
            if (callingUid == this.mContext.getPackageManager().getPackageUid(transportComponent.getPackageName(), 0)) {
                long oldId2 = Binder.clearCallingIdentity();
                try {
                    ComponentName componentName2 = componentName;
                    oldId = oldId2;
                    try {
                        this.mTransportManager.updateTransportAttributes(componentName2, str, configurationIntent, str2, dataManagementIntent, dataManagementLabel);
                        Binder.restoreCallingIdentity(oldId);
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(oldId);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    oldId = oldId2;
                    Binder.restoreCallingIdentity(oldId);
                    throw th;
                }
            } else {
                try {
                    throw new SecurityException("Only the transport can change its description");
                } catch (PackageManager.NameNotFoundException e) {
                    e = e;
                    throw new SecurityException("Transport package not found", e);
                }
            }
        } catch (PackageManager.NameNotFoundException e2) {
            e = e2;
            int i = callingUid;
            throw new SecurityException("Transport package not found", e);
        }
    }

    @Deprecated
    public String selectBackupTransport(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "selectBackupTransport");
        if (!isAllowedByMandatoryBackupTransportPolicy(transportName)) {
            Slog.w(TAG, "Failed to select transport - disallowed by device owner policy.");
            return this.mTransportManager.getCurrentTransportName();
        }
        long oldId = Binder.clearCallingIdentity();
        try {
            String previousTransportName = this.mTransportManager.selectTransport(transportName);
            updateStateForTransport(transportName);
            Slog.v(TAG, "selectBackupTransport(transport = " + transportName + "): previous transport = " + previousTransportName);
            return previousTransportName;
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    public void selectBackupTransportAsync(ComponentName transportComponent, ISelectBackupTransportCallback listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "selectBackupTransportAsync");
        if (!isAllowedByMandatoryBackupTransportPolicy(transportComponent)) {
            if (listener != null) {
                try {
                    Slog.w(TAG, "Failed to select transport - disallowed by device owner policy.");
                    listener.onFailure(-2001);
                } catch (RemoteException e) {
                    Slog.e(TAG, "ISelectBackupTransportCallback listener not available");
                }
            }
            return;
        }
        long oldId = Binder.clearCallingIdentity();
        try {
            String transportString = transportComponent.flattenToShortString();
            Slog.v(TAG, "selectBackupTransportAsync(transport = " + transportString + ")");
            this.mBackupHandler.post(new Runnable(transportComponent, listener) {
                private final /* synthetic */ ComponentName f$1;
                private final /* synthetic */ ISelectBackupTransportCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BackupManagerService.lambda$selectBackupTransportAsync$5(BackupManagerService.this, this.f$1, this.f$2);
                }
            });
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    public static /* synthetic */ void lambda$selectBackupTransportAsync$5(BackupManagerService backupManagerService, ComponentName transportComponent, ISelectBackupTransportCallback listener) {
        String transportName = null;
        int result = backupManagerService.mTransportManager.registerAndSelectTransport(transportComponent);
        if (result == 0) {
            try {
                transportName = backupManagerService.mTransportManager.getTransportName(transportComponent);
                backupManagerService.updateStateForTransport(transportName);
            } catch (TransportNotRegisteredException e) {
                Slog.e(TAG, "Transport got unregistered");
                result = -1;
            }
        }
        if (listener == null) {
            return;
        }
        if (transportName != null) {
            try {
                listener.onSuccess(transportName);
            } catch (RemoteException e2) {
                Slog.e(TAG, "ISelectBackupTransportCallback listener not available");
            }
        } else {
            listener.onFailure(result);
        }
    }

    private boolean isAllowedByMandatoryBackupTransportPolicy(String transportName) {
        ComponentName mandatoryBackupTransport = this.mBackupPolicyEnforcer.getMandatoryBackupTransport();
        if (mandatoryBackupTransport == null) {
            return true;
        }
        try {
            return TextUtils.equals(this.mTransportManager.getTransportName(mandatoryBackupTransport), transportName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "mandatory backup transport not registered!");
            return false;
        }
    }

    private boolean isAllowedByMandatoryBackupTransportPolicy(ComponentName transport) {
        ComponentName mandatoryBackupTransport = this.mBackupPolicyEnforcer.getMandatoryBackupTransport();
        if (mandatoryBackupTransport == null) {
            return true;
        }
        return mandatoryBackupTransport.equals(transport);
    }

    private void updateStateForTransport(String newTransportName) {
        Settings.Secure.putString(this.mContext.getContentResolver(), "backup_transport", newTransportName);
        TransportClient transportClient = this.mTransportManager.getTransportClient(newTransportName, "BMS.updateStateForTransport()");
        if (transportClient != null) {
            try {
                this.mCurrentToken = transportClient.connectOrThrow("BMS.updateStateForTransport()").getCurrentRestoreSet();
            } catch (Exception e) {
                this.mCurrentToken = 0;
                Slog.w(TAG, "Transport " + newTransportName + " not available: current token = 0");
            }
            this.mTransportManager.disposeOfTransportClient(transportClient, "BMS.updateStateForTransport()");
            return;
        }
        Slog.w(TAG, "Transport " + newTransportName + " not registered: current token = 0");
        this.mCurrentToken = 0;
    }

    public Intent getConfigurationIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getConfigurationIntent");
        try {
            return this.mTransportManager.getTransportConfigurationIntent(transportName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "Unable to get configuration intent from transport: " + e.getMessage());
            return null;
        }
    }

    public String getDestinationString(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDestinationString");
        try {
            return this.mTransportManager.getTransportCurrentDestinationString(transportName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "Unable to get destination string from transport: " + e.getMessage());
            return null;
        }
    }

    public Intent getDataManagementIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementIntent");
        try {
            return this.mTransportManager.getTransportDataManagementIntent(transportName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "Unable to get management intent from transport: " + e.getMessage());
            return null;
        }
    }

    public String getDataManagementLabel(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementLabel");
        try {
            return this.mTransportManager.getTransportDataManagementLabel(transportName);
        } catch (TransportNotRegisteredException e) {
            Slog.e(TAG, "Unable to get management label from transport: " + e.getMessage());
            return null;
        }
    }

    public void agentConnected(String packageName, IBinder agentBinder) {
        synchronized (this.mAgentConnectLock) {
            if (Binder.getCallingUid() == 1000) {
                Slog.d(TAG, "agentConnected pkg=" + packageName + " agent=" + agentBinder);
                this.mConnectedAgent = IBackupAgent.Stub.asInterface(agentBinder);
                this.mConnecting = false;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " claiming agent connected");
            }
            this.mAgentConnectLock.notifyAll();
        }
    }

    public void agentDisconnected(String packageName) {
        synchronized (this.mAgentConnectLock) {
            if (Binder.getCallingUid() == 1000) {
                this.mConnectedAgent = null;
                this.mConnecting = false;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " claiming agent disconnected");
            }
            this.mAgentConnectLock.notifyAll();
        }
    }

    public void restoreAtInstall(String packageName, int token) {
        if (Binder.getCallingUid() != 1000) {
            Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " attemping install-time restore");
            return;
        }
        boolean skip = false;
        long restoreSet = getAvailableRestoreToken(packageName);
        Slog.v(TAG, "restoreAtInstall pkg=" + packageName + " token=" + Integer.toHexString(token) + " restoreSet=" + Long.toHexString(restoreSet));
        if (restoreSet == 0) {
            skip = true;
        }
        TransportClient transportClient = this.mTransportManager.getCurrentTransportClient("BMS.restoreAtInstall()");
        if (transportClient == null) {
            Slog.w(TAG, "No transport client");
            skip = true;
        }
        if (!this.mAutoRestore) {
            Slog.w(TAG, "Non-restorable state: auto=" + this.mAutoRestore);
            skip = true;
        }
        if (!skip) {
            try {
                this.mWakelock.acquire();
                OnTaskFinishedListener listener = new OnTaskFinishedListener(transportClient) {
                    private final /* synthetic */ TransportClient f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onFinished(String str) {
                        BackupManagerService.lambda$restoreAtInstall$6(BackupManagerService.this, this.f$1, str);
                    }
                };
                Message msg = this.mBackupHandler.obtainMessage(3);
                msg.obj = RestoreParams.createForRestoreAtInstall(transportClient, null, null, restoreSet, packageName, token, listener);
                this.mBackupHandler.sendMessage(msg);
            } catch (Exception e) {
                Slog.e(TAG, "Unable to contact transport: " + e.getMessage());
                skip = true;
            }
        }
        if (skip) {
            if (transportClient != null) {
                this.mTransportManager.disposeOfTransportClient(transportClient, "BMS.restoreAtInstall()");
            }
            Slog.v(TAG, "Finishing install immediately");
            try {
                this.mPackageManagerBinder.finishPackageInstall(token, false);
            } catch (RemoteException e2) {
            }
        }
    }

    public static /* synthetic */ void lambda$restoreAtInstall$6(BackupManagerService backupManagerService, TransportClient transportClient, String caller) {
        backupManagerService.mTransportManager.disposeOfTransportClient(transportClient, caller);
        backupManagerService.mWakelock.release();
    }

    public IRestoreSession beginRestoreSession(String packageName, String transport) {
        Slog.v(TAG, "beginRestoreSession: pkg=" + packageName + " transport=" + transport);
        boolean needPermission = true;
        if (transport == null) {
            transport = this.mTransportManager.getCurrentTransportName();
            if (packageName != null) {
                try {
                    if (this.mPackageManager.getPackageInfo(packageName, 0).applicationInfo.uid == Binder.getCallingUid()) {
                        needPermission = false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                    throw new IllegalArgumentException("Package " + packageName + " not found");
                }
            }
        }
        if (needPermission) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "beginRestoreSession");
        } else {
            Slog.d(TAG, "restoring self on current transport; no permission needed");
        }
        synchronized (this) {
            if (this.mActiveRestoreSession != null) {
                Slog.i(TAG, "Restore session requested but one already active");
                return null;
            } else if (this.mBackupRunning) {
                Slog.i(TAG, "Restore session requested but currently running backups");
                return null;
            } else {
                this.mActiveRestoreSession = new ActiveRestoreSession(this, packageName, transport);
                this.mBackupHandler.sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
                return this.mActiveRestoreSession;
            }
        }
    }

    public void clearRestoreSession(ActiveRestoreSession currentSession) {
        synchronized (this) {
            if (currentSession != this.mActiveRestoreSession) {
                Slog.e(TAG, "ending non-current restore session");
            } else {
                Slog.v(TAG, "Clearing restore session and halting timeout");
                this.mActiveRestoreSession = null;
                this.mBackupHandler.removeMessages(8);
            }
        }
    }

    public void opComplete(int token, long result) {
        Operation op;
        synchronized (this.mCurrentOpLock) {
            op = this.mCurrentOperations.get(token);
            if (op != null) {
                if (op.state == -1) {
                    op = null;
                    this.mCurrentOperations.delete(token);
                } else if (op.state == 1) {
                    Slog.w(TAG, "Received duplicate ack for token=" + Integer.toHexString(token));
                    op = null;
                    this.mCurrentOperations.remove(token);
                } else if (op.state == 0) {
                    op.state = 1;
                }
            }
            this.mCurrentOpLock.notifyAll();
        }
        if (op != null && op.callback != null) {
            this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(21, Pair.create(op.callback, Long.valueOf(result))));
        }
    }

    public boolean isAppEligibleForBackup(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "isAppEligibleForBackup");
        long oldToken = Binder.clearCallingIdentity();
        try {
            TransportClient transportClient = this.mTransportManager.getCurrentTransportClient("BMS.isAppEligibleForBackup");
            boolean eligible = AppBackupUtils.appIsRunningAndEligibleForBackupWithTransport(transportClient, packageName, this.mPackageManager);
            if (transportClient != null) {
                this.mTransportManager.disposeOfTransportClient(transportClient, "BMS.isAppEligibleForBackup");
            }
            return eligible;
        } finally {
            Binder.restoreCallingIdentity(oldToken);
        }
    }

    public String[] filterAppsEligibleForBackup(String[] packages) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "filterAppsEligibleForBackup");
        long oldToken = Binder.clearCallingIdentity();
        try {
            TransportClient transportClient = this.mTransportManager.getCurrentTransportClient("BMS.filterAppsEligibleForBackup");
            List<String> eligibleApps = new LinkedList<>();
            for (String packageName : packages) {
                if (AppBackupUtils.appIsRunningAndEligibleForBackupWithTransport(transportClient, packageName, this.mPackageManager)) {
                    eligibleApps.add(packageName);
                }
            }
            if (transportClient != null) {
                this.mTransportManager.disposeOfTransportClient(transportClient, "BMS.filterAppsEligibleForBackup");
            }
            return (String[]) eligibleApps.toArray(new String[eligibleApps.size()]);
        } finally {
            Binder.restoreCallingIdentity(oldToken);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            long identityToken = Binder.clearCallingIdentity();
            if (args != null) {
                try {
                    int length = args.length;
                    int i = 0;
                    while (i < length) {
                        String arg = args[i];
                        if ("-h".equals(arg)) {
                            pw.println("'dumpsys backup' optional arguments:");
                            pw.println("  -h       : this help text");
                            pw.println("  a[gents] : dump information about defined backup agents");
                            Binder.restoreCallingIdentity(identityToken);
                            return;
                        } else if ("agents".startsWith(arg)) {
                            dumpAgents(pw);
                            Binder.restoreCallingIdentity(identityToken);
                            return;
                        } else if ("transportclients".equals(arg.toLowerCase())) {
                            this.mTransportManager.dumpTransportClients(pw);
                            Binder.restoreCallingIdentity(identityToken);
                            return;
                        } else if ("transportstats".equals(arg.toLowerCase())) {
                            this.mTransportManager.dumpTransportStats(pw);
                            return;
                        } else {
                            i++;
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
            dumpInternal(pw);
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void dumpAgents(PrintWriter pw) {
        List<PackageInfo> agentPackages = allAgentPackages();
        pw.println("Defined backup agents:");
        for (PackageInfo pkg : agentPackages) {
            pw.print("  ");
            pw.print(pkg.packageName);
            pw.println(':');
            pw.print("      ");
            pw.println(pkg.applicationInfo.backupAgentName);
        }
    }

    private void dumpInternal(PrintWriter pw) {
        Set<String> processedPackages;
        String str;
        synchronized (this.mQueueLock) {
            StringBuilder sb = new StringBuilder();
            sb.append("Backup Manager is ");
            sb.append(this.mEnabled ? "enabled" : "disabled");
            sb.append(" / ");
            sb.append(!this.mProvisioned ? "not " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb.append("provisioned / ");
            sb.append(this.mPendingInits.size() == 0 ? "not " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            sb.append("pending init");
            pw.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Auto-restore is ");
            sb2.append(this.mAutoRestore ? "enabled" : "disabled");
            pw.println(sb2.toString());
            if (this.mBackupRunning) {
                pw.println("Backup currently running");
            }
            pw.println(isBackupOperationInProgress() ? "Backup in progress" : "No backups running");
            pw.println("Last backup pass started: " + this.mLastBackupPass + " (now = " + System.currentTimeMillis() + ')');
            StringBuilder sb3 = new StringBuilder();
            sb3.append("  next scheduled: ");
            sb3.append(KeyValueBackupJob.nextScheduled());
            pw.println(sb3.toString());
            pw.println("Transport whitelist:");
            for (ComponentName transport : this.mTransportManager.getTransportWhitelist()) {
                pw.print("    ");
                pw.println(transport.flattenToShortString());
            }
            pw.println("Available transports:");
            String[] transports = listAllTransports();
            if (transports != null) {
                for (String t : transports) {
                    StringBuilder sb4 = new StringBuilder();
                    if (t.equals(this.mTransportManager.getCurrentTransportName())) {
                        str = "  * ";
                    } else {
                        str = "    ";
                    }
                    sb4.append(str);
                    sb4.append(t);
                    pw.println(sb4.toString());
                    try {
                        File dir = new File(this.mBaseStateDir, this.mTransportManager.getTransportDirName(t));
                        pw.println("       destination: " + this.mTransportManager.getTransportCurrentDestinationString(t));
                        pw.println("       intent: " + this.mTransportManager.getTransportConfigurationIntent(t));
                        File[] listFiles = dir.listFiles();
                        int length = listFiles.length;
                        for (int i = 0; i < length; i++) {
                            File f = listFiles[i];
                            pw.println("       " + f.getName() + " - " + f.length() + " state bytes");
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "Error in transport", e);
                        pw.println("        Error: " + e);
                    }
                }
            }
            this.mTransportManager.dumpTransportClients(pw);
            pw.println("Pending init: " + this.mPendingInits.size());
            Iterator<String> it = this.mPendingInits.iterator();
            while (it.hasNext()) {
                pw.println("    " + it.next());
            }
            synchronized (this.mBackupTrace) {
                if (!this.mBackupTrace.isEmpty()) {
                    pw.println("Most recent backup trace:");
                    Iterator<String> it2 = this.mBackupTrace.iterator();
                    while (it2.hasNext()) {
                        pw.println("   " + it2.next());
                    }
                }
            }
            pw.print("Ancestral: ");
            pw.println(Long.toHexString(this.mAncestralToken));
            pw.print("Current:   ");
            pw.println(Long.toHexString(this.mCurrentToken));
            int N = this.mBackupParticipants.size();
            pw.println("Participants:");
            for (int i2 = 0; i2 < N; i2++) {
                int uid = this.mBackupParticipants.keyAt(i2);
                pw.print("  uid: ");
                pw.println(uid);
                Iterator<String> it3 = this.mBackupParticipants.valueAt(i2).iterator();
                while (it3.hasNext()) {
                    pw.println("    " + it3.next());
                }
            }
            StringBuilder sb5 = new StringBuilder();
            sb5.append("Ancestral packages: ");
            sb5.append(this.mAncestralPackages == null ? "none" : Integer.valueOf(this.mAncestralPackages.size()));
            pw.println(sb5.toString());
            if (this.mAncestralPackages != null) {
                Iterator<String> it4 = this.mAncestralPackages.iterator();
                while (it4.hasNext()) {
                    pw.println("    " + it4.next());
                }
            }
            pw.println("Ever backed up: " + this.mProcessedPackagesJournal.getPackagesCopy().size());
            Iterator<String> it5 = processedPackages.iterator();
            while (it5.hasNext()) {
                pw.println("    " + it5.next());
            }
            pw.println("Pending key/value backup: " + this.mPendingBackups.size());
            Iterator<BackupRequest> it6 = this.mPendingBackups.values().iterator();
            while (it6.hasNext()) {
                pw.println("    " + it6.next());
            }
            pw.println("Full backup queue:" + this.mFullBackupQueue.size());
            Iterator<FullBackupEntry> it7 = this.mFullBackupQueue.iterator();
            while (it7.hasNext()) {
                FullBackupEntry entry = it7.next();
                pw.print("    ");
                pw.print(entry.lastBackup);
                pw.print(" : ");
                pw.println(entry.packageName);
            }
        }
    }

    public IBackupManager getBackupManagerBinder() {
        return this.mBackupManagerBinder;
    }
}
