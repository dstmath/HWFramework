package com.android.server.backup;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IBackupAgent;
import android.app.PackageInstallObserver;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupProgress;
import android.app.backup.FullBackup;
import android.app.backup.FullBackupDataOutput;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.IRestoreSession.Stub;
import android.app.backup.RestoreDescription;
import android.app.backup.RestoreSet;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Environment.UserEnvironment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.os.storage.IMountService;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.backup.IObbBackupService;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.EventLogTags;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.backup.PackageManagerBackupAgent.Metadata;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.wm.WindowManagerService.H;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import libcore.io.IoUtils;

public class BackupManagerService {
    static final String BACKUP_ENABLE_FILE = "backup_enabled";
    static final String BACKUP_FILE_HEADER_MAGIC = "ANDROID BACKUP\n";
    static final int BACKUP_FILE_VERSION = 4;
    static final String BACKUP_MANIFEST_FILENAME = "_manifest";
    static final int BACKUP_MANIFEST_VERSION = 1;
    static final String BACKUP_METADATA_FILENAME = "_meta";
    static final int BACKUP_METADATA_VERSION = 1;
    static final int BACKUP_PW_FILE_VERSION = 2;
    static final int BACKUP_WIDGET_METADATA_TOKEN = 33549569;
    static final int BUSY_BACKOFF_FUZZ = 7200000;
    static final long BUSY_BACKOFF_MIN_MILLIS = 3600000;
    static final boolean COMPRESS_FULL_BACKUPS = true;
    static final int CURRENT_ANCESTRAL_RECORD_VERSION = 1;
    static final boolean DEBUG = false;
    static final boolean DEBUG_BACKUP_TRACE = true;
    static final boolean DEBUG_SCHEDULING = false;
    static final String ENCRYPTION_ALGORITHM_NAME = "AES-256";
    static final String INIT_SENTINEL_FILE_NAME = "_need_init_";
    static final String KEY_WIDGET_STATE = "\uffed\uffedwidget";
    static final long MIN_FULL_BACKUP_INTERVAL = 86400000;
    static final boolean MORE_DEBUG = false;
    static final int MSG_BACKUP_RESTORE_STEP = 20;
    private static final int MSG_FULL_CONFIRMATION_TIMEOUT = 9;
    static final int MSG_OP_COMPLETE = 21;
    private static final int MSG_REQUEST_BACKUP = 15;
    private static final int MSG_RESTORE_TIMEOUT = 8;
    private static final int MSG_RETRY_CLEAR = 12;
    private static final int MSG_RETRY_INIT = 11;
    private static final int MSG_RUN_ADB_BACKUP = 2;
    private static final int MSG_RUN_ADB_RESTORE = 10;
    private static final int MSG_RUN_BACKUP = 1;
    private static final int MSG_RUN_CLEAR = 4;
    private static final int MSG_RUN_FULL_TRANSPORT_BACKUP = 14;
    private static final int MSG_RUN_GET_RESTORE_SETS = 6;
    private static final int MSG_RUN_INITIALIZE = 5;
    private static final int MSG_RUN_RESTORE = 3;
    private static final int MSG_TIMEOUT = 7;
    private static final int MSG_WIDGET_BROADCAST = 13;
    static final int OP_ACKNOWLEDGED = 1;
    static final int OP_PENDING = 0;
    static final int OP_TIMEOUT = -1;
    static final String PACKAGE_MANAGER_SENTINEL = "@pm@";
    static final int PBKDF2_HASH_ROUNDS = 10000;
    static final int PBKDF2_KEY_SIZE = 256;
    static final int PBKDF2_SALT_SIZE = 512;
    static final String PBKDF_CURRENT = "PBKDF2WithHmacSHA1";
    static final String PBKDF_FALLBACK = "PBKDF2WithHmacSHA1And8bit";
    private static final String RUN_BACKUP_ACTION = "android.app.backup.intent.RUN";
    private static final String RUN_INITIALIZE_ACTION = "android.app.backup.intent.INIT";
    static final int SCHEDULE_FILE_VERSION = 1;
    static final String SERVICE_ACTION_TRANSPORT_HOST = "android.backup.TRANSPORT_HOST";
    static final String SETTINGS_PACKAGE = "com.android.providers.settings";
    static final String SHARED_BACKUP_AGENT_PACKAGE = "com.android.sharedstoragebackup";
    private static final String TAG = "BackupManagerService";
    static final long TIMEOUT_BACKUP_INTERVAL = 30000;
    static final long TIMEOUT_FULL_BACKUP_INTERVAL = 300000;
    static final long TIMEOUT_FULL_CONFIRMATION = 60000;
    static final long TIMEOUT_INTERVAL = 10000;
    static final long TIMEOUT_RESTORE_FINISHED_INTERVAL = 30000;
    static final long TIMEOUT_RESTORE_INTERVAL = 60000;
    static final long TIMEOUT_SHARED_BACKUP_INTERVAL = 1800000;
    private static final long TRANSPORT_RETRY_INTERVAL = 3600000;
    static Trampoline sInstance;
    ActiveRestoreSession mActiveRestoreSession;
    private IActivityManager mActivityManager;
    final Object mAgentConnectLock;
    private AlarmManager mAlarmManager;
    Set<String> mAncestralPackages;
    long mAncestralToken;
    boolean mAutoRestore;
    BackupHandler mBackupHandler;
    IBackupManager mBackupManagerBinder;
    final SparseArray<HashSet<String>> mBackupParticipants;
    volatile boolean mBackupRunning;
    final List<String> mBackupTrace;
    File mBaseStateDir;
    BroadcastReceiver mBroadcastReceiver;
    final Object mClearDataLock;
    volatile boolean mClearingData;
    IBackupAgent mConnectedAgent;
    volatile boolean mConnecting;
    Context mContext;
    final Object mCurrentOpLock;
    final SparseArray<Operation> mCurrentOperations;
    long mCurrentToken;
    String mCurrentTransport;
    File mDataDir;
    boolean mEnabled;
    private File mEverStored;
    HashSet<String> mEverStoredApps;
    @GuardedBy("mQueueLock")
    ArrayList<FullBackupEntry> mFullBackupQueue;
    File mFullBackupScheduleFile;
    Runnable mFullBackupScheduleWriter;
    final SparseArray<FullParams> mFullConfirmations;
    HandlerThread mHandlerThread;
    File mJournal;
    File mJournalDir;
    volatile long mLastBackupPass;
    private IMountService mMountService;
    private PackageManager mPackageManager;
    IPackageManager mPackageManagerBinder;
    private String mPasswordHash;
    private File mPasswordHashFile;
    private byte[] mPasswordSalt;
    private int mPasswordVersion;
    private File mPasswordVersionFile;
    HashMap<String, BackupRequest> mPendingBackups;
    HashSet<String> mPendingInits;
    private PowerManager mPowerManager;
    boolean mProvisioned;
    ContentObserver mProvisionedObserver;
    final Object mQueueLock;
    private final SecureRandom mRng;
    PendingIntent mRunBackupIntent;
    BroadcastReceiver mRunBackupReceiver;
    PendingIntent mRunInitIntent;
    BroadcastReceiver mRunInitReceiver;
    @GuardedBy("mQueueLock")
    PerformFullTransportBackupTask mRunningFullBackupTask;
    File mTokenFile;
    final Random mTokenGenerator;
    final ArrayMap<String, TransportConnection> mTransportConnections;
    final ArrayMap<String, String> mTransportNames;
    final Intent mTransportServiceIntent;
    final ArraySet<ComponentName> mTransportWhitelist;
    final ArrayMap<String, IBackupTransport> mTransports;
    WakeLock mWakelock;

    /* renamed from: com.android.server.backup.BackupManagerService.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ long val$latency;

        AnonymousClass3(long val$latency) {
            this.val$latency = val$latency;
        }

        public void run() {
            FullBackupJob.schedule(BackupManagerService.this.mContext, this.val$latency);
        }
    }

    /* renamed from: com.android.server.backup.BackupManagerService.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ long val$deferTime;

        AnonymousClass4(long val$deferTime) {
            this.val$deferTime = val$deferTime;
        }

        public void run() {
            FullBackupJob.schedule(BackupManagerService.this.mContext, this.val$deferTime);
        }
    }

    /* renamed from: com.android.server.backup.BackupManagerService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ String val$packageName;
        final /* synthetic */ HashSet val$targets;

        AnonymousClass5(String val$packageName, HashSet val$targets) {
            this.val$packageName = val$packageName;
            this.val$targets = val$targets;
        }

        public void run() {
            BackupManagerService.this.dataChangedImpl(this.val$packageName, this.val$targets);
        }
    }

    class ActiveRestoreSession extends Stub {
        private static final String TAG = "RestoreSession";
        boolean mEnded;
        private String mPackageName;
        RestoreSet[] mRestoreSets;
        private IBackupTransport mRestoreTransport;
        boolean mTimedOut;

        class EndRestoreRunnable implements Runnable {
            BackupManagerService mBackupManager;
            ActiveRestoreSession mSession;

            EndRestoreRunnable(BackupManagerService manager, ActiveRestoreSession session) {
                this.mBackupManager = manager;
                this.mSession = session;
            }

            public void run() {
                synchronized (this.mSession) {
                    this.mSession.mRestoreTransport = null;
                    this.mSession.mEnded = BackupManagerService.DEBUG_BACKUP_TRACE;
                }
                this.mBackupManager.clearRestoreSession(this.mSession);
            }
        }

        ActiveRestoreSession(String packageName, String transport) {
            this.mRestoreTransport = null;
            this.mRestoreSets = null;
            this.mEnded = BackupManagerService.MORE_DEBUG;
            this.mTimedOut = BackupManagerService.MORE_DEBUG;
            this.mPackageName = packageName;
            this.mRestoreTransport = BackupManagerService.this.getTransport(transport);
        }

        public void markTimedOut() {
            this.mTimedOut = BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        public synchronized int getAvailableRestoreSets(IRestoreObserver observer) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreSets");
            if (observer == null) {
                throw new IllegalArgumentException("Observer must not be null");
            } else if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return BackupManagerService.OP_TIMEOUT;
            } else {
                long oldId = Binder.clearCallingIdentity();
                try {
                    if (this.mRestoreTransport == null) {
                        Slog.w(TAG, "Null transport getting restore sets");
                        return BackupManagerService.OP_TIMEOUT;
                    }
                    BackupManagerService.this.mBackupHandler.removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                    BackupManagerService.this.mWakelock.acquire();
                    BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(BackupManagerService.MSG_RUN_GET_RESTORE_SETS, new RestoreGetSetsParams(BackupManagerService.this, this.mRestoreTransport, this, observer)));
                    Binder.restoreCallingIdentity(oldId);
                    return BackupManagerService.OP_PENDING;
                } catch (Exception e) {
                    Slog.e(TAG, "Error in getAvailableRestoreSets", e);
                    return BackupManagerService.OP_TIMEOUT;
                } finally {
                    Binder.restoreCallingIdentity(oldId);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized int restoreAll(long token, IRestoreObserver observer) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
            if (BackupManagerService.DEBUG) {
                Slog.d(TAG, "restoreAll token=" + Long.toHexString(token) + " observer=" + observer);
            }
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return BackupManagerService.OP_TIMEOUT;
            } else if (this.mRestoreTransport == null || this.mRestoreSets == null) {
                Slog.e(TAG, "Ignoring restoreAll() with no restore set");
                return BackupManagerService.OP_TIMEOUT;
            } else if (this.mPackageName != null) {
                Slog.e(TAG, "Ignoring restoreAll() on single-package session");
                return BackupManagerService.OP_TIMEOUT;
            } else {
                try {
                    String dirName = this.mRestoreTransport.transportDirName();
                    synchronized (BackupManagerService.this.mQueueLock) {
                        int i = BackupManagerService.OP_PENDING;
                        while (true) {
                            if (i >= this.mRestoreSets.length) {
                                Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                                return BackupManagerService.OP_TIMEOUT;
                            } else if (token == this.mRestoreSets[i].token) {
                                break;
                            } else {
                                i += BackupManagerService.SCHEDULE_FILE_VERSION;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to contact transport for restore");
                    return BackupManagerService.OP_TIMEOUT;
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized int restoreSome(long token, IRestoreObserver observer, String[] packages) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
            if (BackupManagerService.DEBUG) {
                StringBuilder b = new StringBuilder(DumpState.DUMP_PACKAGES);
                b.append("restoreSome token=");
                b.append(Long.toHexString(token));
                b.append(" observer=");
                b.append(observer.toString());
                b.append(" packages=");
                if (packages == null) {
                    b.append("null");
                } else {
                    b.append('{');
                    boolean first = BackupManagerService.DEBUG_BACKUP_TRACE;
                    int length = packages.length;
                    for (int i = BackupManagerService.OP_PENDING; i < length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                        String s = packages[i];
                        if (first) {
                            first = BackupManagerService.MORE_DEBUG;
                        } else {
                            b.append(", ");
                        }
                        b.append(s);
                    }
                    b.append('}');
                }
                Slog.d(TAG, b.toString());
            }
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return BackupManagerService.OP_TIMEOUT;
            } else if (this.mRestoreTransport == null || this.mRestoreSets == null) {
                Slog.e(TAG, "Ignoring restoreAll() with no restore set");
                return BackupManagerService.OP_TIMEOUT;
            } else if (this.mPackageName != null) {
                Slog.e(TAG, "Ignoring restoreAll() on single-package session");
                return BackupManagerService.OP_TIMEOUT;
            } else {
                try {
                    String dirName = this.mRestoreTransport.transportDirName();
                    synchronized (BackupManagerService.this.mQueueLock) {
                        int i2 = BackupManagerService.OP_PENDING;
                        while (true) {
                            if (i2 >= this.mRestoreSets.length) {
                                Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                                return BackupManagerService.OP_TIMEOUT;
                            } else if (token == this.mRestoreSets[i2].token) {
                                break;
                            } else {
                                i2 += BackupManagerService.SCHEDULE_FILE_VERSION;
                            }
                        }
                        BackupManagerService.this.mBackupHandler.removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                        long oldId = Binder.clearCallingIdentity();
                        BackupManagerService.this.mWakelock.acquire();
                        Message msg = BackupManagerService.this.mBackupHandler.obtainMessage(BackupManagerService.MSG_RUN_RESTORE);
                        msg.obj = new RestoreParams(BackupManagerService.this, this.mRestoreTransport, dirName, observer, token, packages, packages.length > BackupManagerService.SCHEDULE_FILE_VERSION ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG);
                        BackupManagerService.this.mBackupHandler.sendMessage(msg);
                        Binder.restoreCallingIdentity(oldId);
                        return BackupManagerService.OP_PENDING;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to contact transport for restore");
                    return BackupManagerService.OP_TIMEOUT;
                }
            }
        }

        public synchronized int restorePackage(String packageName, IRestoreObserver observer) {
            if (BackupManagerService.DEBUG) {
                Slog.v(TAG, "restorePackage pkg=" + packageName + " obs=" + observer);
            }
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return BackupManagerService.OP_TIMEOUT;
            } else if (this.mPackageName == null || this.mPackageName.equals(packageName)) {
                try {
                    PackageInfo app = BackupManagerService.this.mPackageManager.getPackageInfo(packageName, BackupManagerService.OP_PENDING);
                    if (BackupManagerService.this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) != BackupManagerService.OP_TIMEOUT || app.applicationInfo.uid == Binder.getCallingUid()) {
                        long oldId = Binder.clearCallingIdentity();
                        try {
                            long token = BackupManagerService.this.getAvailableRestoreToken(packageName);
                            if (BackupManagerService.DEBUG) {
                                Slog.v(TAG, "restorePackage pkg=" + packageName + " token=" + Long.toHexString(token));
                            }
                            if (token == 0) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.w(TAG, "No data available for this package; not restoring");
                                }
                                return BackupManagerService.OP_TIMEOUT;
                            }
                            String dirName = this.mRestoreTransport.transportDirName();
                            BackupManagerService.this.mBackupHandler.removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                            BackupManagerService.this.mWakelock.acquire();
                            Message msg = BackupManagerService.this.mBackupHandler.obtainMessage(BackupManagerService.MSG_RUN_RESTORE);
                            msg.obj = new RestoreParams(BackupManagerService.this, this.mRestoreTransport, dirName, observer, token, app);
                            BackupManagerService.this.mBackupHandler.sendMessage(msg);
                            return BackupManagerService.OP_PENDING;
                        } catch (RemoteException e) {
                            Slog.e(TAG, "Unable to contact transport for restore");
                            return BackupManagerService.OP_TIMEOUT;
                        } finally {
                            Binder.restoreCallingIdentity(oldId);
                        }
                    } else {
                        Slog.w(TAG, "restorePackage: bad packageName=" + packageName + " or calling uid=" + Binder.getCallingUid());
                        throw new SecurityException("No permission to restore other packages");
                    }
                } catch (NameNotFoundException e2) {
                    Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                    return BackupManagerService.OP_TIMEOUT;
                }
            } else {
                Slog.e(TAG, "Ignoring attempt to restore pkg=" + packageName + " on session for package " + this.mPackageName);
                return BackupManagerService.OP_TIMEOUT;
            }
        }

        public synchronized void endRestoreSession() {
            if (BackupManagerService.DEBUG) {
                Slog.d(TAG, "endRestoreSession");
            }
            if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
            } else if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else {
                BackupManagerService.this.mBackupHandler.post(new EndRestoreRunnable(BackupManagerService.this, this));
            }
        }
    }

    interface BackupRestoreTask {
        void execute();

        void handleTimeout();

        void operationComplete(long j);
    }

    class AdbRestoreFinishedLatch implements BackupRestoreTask {
        static final String TAG = "AdbRestoreFinishedLatch";
        final CountDownLatch mLatch;

        AdbRestoreFinishedLatch() {
            this.mLatch = new CountDownLatch(BackupManagerService.SCHEDULE_FILE_VERSION);
        }

        void await() {
            try {
                boolean latched = this.mLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupted!");
            }
        }

        public void execute() {
        }

        public void operationComplete(long result) {
            this.mLatch.countDown();
        }

        public void handleTimeout() {
            if (BackupManagerService.DEBUG) {
                Slog.w(TAG, "adb onRestoreFinished() timed out");
            }
            this.mLatch.countDown();
        }
    }

    private class BackupHandler extends Handler {
        public BackupHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BackupManagerService.SCHEDULE_FILE_VERSION /*1*/:
                    BackupManagerService.this.mLastBackupPass = System.currentTimeMillis();
                    IBackupTransport transport = BackupManagerService.this.getTransport(BackupManagerService.this.mCurrentTransport);
                    if (transport == null) {
                        Slog.v(BackupManagerService.TAG, "Backup requested but no transport available");
                        synchronized (BackupManagerService.this.mQueueLock) {
                            BackupManagerService.this.mBackupRunning = BackupManagerService.MORE_DEBUG;
                            break;
                        }
                        BackupManagerService.this.mWakelock.release();
                        return;
                    }
                    ArrayList<BackupRequest> queue = new ArrayList();
                    File oldJournal = BackupManagerService.this.mJournal;
                    synchronized (BackupManagerService.this.mQueueLock) {
                        if (BackupManagerService.this.mPendingBackups.size() > 0) {
                            for (BackupRequest add : BackupManagerService.this.mPendingBackups.values()) {
                                queue.add(add);
                            }
                            if (BackupManagerService.DEBUG) {
                                Slog.v(BackupManagerService.TAG, "clearing pending backups");
                            }
                            BackupManagerService.this.mPendingBackups.clear();
                            BackupManagerService.this.mJournal = null;
                            break;
                        }
                        break;
                    }
                    boolean staged = BackupManagerService.DEBUG_BACKUP_TRACE;
                    if (queue.size() > 0) {
                        try {
                            sendMessage(obtainMessage(BackupManagerService.MSG_BACKUP_RESTORE_STEP, new PerformBackupTask(BackupManagerService.this, transport, transport.transportDirName(), queue, oldJournal, null, null, BackupManagerService.MORE_DEBUG)));
                        } catch (RemoteException e) {
                            Slog.e(BackupManagerService.TAG, "Transport became unavailable attempting backup");
                            staged = BackupManagerService.MORE_DEBUG;
                        }
                    } else {
                        Slog.v(BackupManagerService.TAG, "Backup requested but nothing pending");
                        staged = BackupManagerService.MORE_DEBUG;
                    }
                    if (!staged) {
                        synchronized (BackupManagerService.this.mQueueLock) {
                            BackupManagerService.this.mBackupRunning = BackupManagerService.MORE_DEBUG;
                            break;
                        }
                        BackupManagerService.this.mWakelock.release();
                    }
                case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                    FullBackupParams params = msg.obj;
                    new Thread(new PerformAdbBackupTask(BackupManagerService.this, params.fd, params.observer, params.includeApks, params.includeObbs, params.includeShared, params.doWidgets, params.curPassword, params.encryptPassword, params.allApps, params.includeSystem, params.doCompress, params.packages, params.latch), "adb-backup").start();
                case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                    RestoreParams params2 = msg.obj;
                    Slog.d(BackupManagerService.TAG, "MSG_RUN_RESTORE observer=" + params2.observer);
                    sendMessage(obtainMessage(BackupManagerService.MSG_BACKUP_RESTORE_STEP, new PerformUnifiedRestoreTask(BackupManagerService.this, params2.transport, params2.observer, params2.token, params2.pkgInfo, params2.pmToken, params2.isSystemRestore, params2.filterSet)));
                case BackupManagerService.MSG_RUN_CLEAR /*4*/:
                    ClearParams params3 = msg.obj;
                    new PerformClearTask(BackupManagerService.this, params3.transport, params3.packageInfo).run();
                case BackupManagerService.MSG_RUN_INITIALIZE /*5*/:
                    HashSet<String> hashSet;
                    synchronized (BackupManagerService.this.mQueueLock) {
                        hashSet = new HashSet(BackupManagerService.this.mPendingInits);
                        BackupManagerService.this.mPendingInits.clear();
                        break;
                    }
                    new PerformInitializeTask(BackupManagerService.this, hashSet).run();
                case BackupManagerService.MSG_RUN_GET_RESTORE_SETS /*6*/:
                    RestoreSet[] restoreSetArr = null;
                    RestoreGetSetsParams params4 = msg.obj;
                    try {
                        restoreSetArr = params4.transport.getAvailableRestoreSets();
                        synchronized (params4.session) {
                            params4.session.mRestoreSets = restoreSetArr;
                            break;
                        }
                        if (restoreSetArr == null) {
                            EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                        }
                        if (params4.observer != null) {
                            try {
                                params4.observer.restoreSetsAvailable(restoreSetArr);
                            } catch (RemoteException e2) {
                                Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                            } catch (Throwable e3) {
                                Slog.e(BackupManagerService.TAG, "Restore observer threw", e3);
                            }
                        }
                        removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                        sendEmptyMessageDelayed(BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.TIMEOUT_RESTORE_INTERVAL);
                        BackupManagerService.this.mWakelock.release();
                    } catch (Exception e4) {
                        try {
                            Slog.e(BackupManagerService.TAG, "Error from transport getting set list");
                            if (params4.observer != null) {
                                try {
                                    params4.observer.restoreSetsAvailable(restoreSetArr);
                                } catch (RemoteException e5) {
                                    Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                                } catch (Throwable e32) {
                                    Slog.e(BackupManagerService.TAG, "Restore observer threw", e32);
                                }
                            }
                            removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                            sendEmptyMessageDelayed(BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.TIMEOUT_RESTORE_INTERVAL);
                            BackupManagerService.this.mWakelock.release();
                        } catch (Throwable th) {
                            if (params4.observer != null) {
                                try {
                                    params4.observer.restoreSetsAvailable(restoreSetArr);
                                } catch (RemoteException e6) {
                                    Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                                } catch (Throwable e322) {
                                    Slog.e(BackupManagerService.TAG, "Restore observer threw", e322);
                                }
                            }
                            removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
                            sendEmptyMessageDelayed(BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.TIMEOUT_RESTORE_INTERVAL);
                            BackupManagerService.this.mWakelock.release();
                        }
                    }
                case BackupManagerService.MSG_TIMEOUT /*7*/:
                    BackupManagerService.this.handleTimeout(msg.arg1, msg.obj);
                case BackupManagerService.MSG_RESTORE_TIMEOUT /*8*/:
                    synchronized (BackupManagerService.this) {
                        if (BackupManagerService.this.mActiveRestoreSession != null) {
                            Slog.w(BackupManagerService.TAG, "Restore session timed out; aborting");
                            BackupManagerService.this.mActiveRestoreSession.markTimedOut();
                            ActiveRestoreSession activeRestoreSession = BackupManagerService.this.mActiveRestoreSession;
                            activeRestoreSession.getClass();
                            post(new EndRestoreRunnable(BackupManagerService.this, BackupManagerService.this.mActiveRestoreSession));
                        }
                        break;
                    }
                case BackupManagerService.MSG_FULL_CONFIRMATION_TIMEOUT /*9*/:
                    synchronized (BackupManagerService.this.mFullConfirmations) {
                        FullParams params5 = (FullParams) BackupManagerService.this.mFullConfirmations.get(msg.arg1);
                        if (params5 == null) {
                            Slog.d(BackupManagerService.TAG, "couldn't find params for token " + msg.arg1);
                            break;
                        }
                        Slog.i(BackupManagerService.TAG, "Full backup/restore timed out waiting for user confirmation");
                        BackupManagerService.this.signalFullBackupRestoreCompletion(params5);
                        BackupManagerService.this.mFullConfirmations.delete(msg.arg1);
                        if (params5.observer != null) {
                            try {
                                params5.observer.onTimeout();
                            } catch (RemoteException e7) {
                            }
                        }
                        break;
                    }
                case BackupManagerService.MSG_RUN_ADB_RESTORE /*10*/:
                    FullRestoreParams params6 = msg.obj;
                    new Thread(new PerformAdbRestoreTask(BackupManagerService.this, params6.fd, params6.curPassword, params6.encryptPassword, params6.observer, params6.latch), "adb-restore").start();
                case BackupManagerService.MSG_RETRY_INIT /*11*/:
                    synchronized (BackupManagerService.this.mQueueLock) {
                        BackupManagerService.this.recordInitPendingLocked(msg.arg1 != 0 ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG, (String) msg.obj);
                        BackupManagerService.this.mAlarmManager.set(BackupManagerService.OP_PENDING, System.currentTimeMillis(), BackupManagerService.this.mRunInitIntent);
                        break;
                    }
                case BackupManagerService.MSG_RETRY_CLEAR /*12*/:
                    ClearRetryParams params7 = msg.obj;
                    BackupManagerService.this.clearBackupData(params7.transportName, params7.packageName);
                case BackupManagerService.MSG_WIDGET_BROADCAST /*13*/:
                    BackupManagerService.this.mContext.sendBroadcastAsUser(msg.obj, UserHandle.SYSTEM);
                case BackupManagerService.MSG_RUN_FULL_TRANSPORT_BACKUP /*14*/:
                    new Thread(msg.obj, "transport-backup").start();
                case BackupManagerService.MSG_REQUEST_BACKUP /*15*/:
                    BackupParams params8 = msg.obj;
                    ArrayList<BackupRequest> kvQueue = new ArrayList();
                    for (String packageName : params8.kvPackages) {
                        kvQueue.add(new BackupRequest(packageName));
                    }
                    BackupManagerService.this.mBackupRunning = BackupManagerService.DEBUG_BACKUP_TRACE;
                    BackupManagerService.this.mWakelock.acquire();
                    sendMessage(obtainMessage(BackupManagerService.MSG_BACKUP_RESTORE_STEP, new PerformBackupTask(BackupManagerService.this, params8.transport, params8.dirName, kvQueue, null, params8.observer, params8.fullPackages, BackupManagerService.DEBUG_BACKUP_TRACE)));
                case BackupManagerService.MSG_BACKUP_RESTORE_STEP /*20*/:
                    try {
                        msg.obj.execute();
                    } catch (ClassCastException e8) {
                        Slog.e(BackupManagerService.TAG, "Invalid backup task in flight, obj=" + msg.obj);
                    }
                case BackupManagerService.MSG_OP_COMPLETE /*21*/:
                    try {
                        Pair<BackupRestoreTask, Long> taskWithResult = msg.obj;
                        ((BackupRestoreTask) taskWithResult.first).operationComplete(((Long) taskWithResult.second).longValue());
                    } catch (ClassCastException e9) {
                        Slog.e(BackupManagerService.TAG, "Invalid completion in flight, obj=" + msg.obj);
                    }
                default:
            }
        }
    }

    class BackupParams {
        public String dirName;
        public ArrayList<String> fullPackages;
        public ArrayList<String> kvPackages;
        public IBackupObserver observer;
        public IBackupTransport transport;
        public boolean userInitiated;

        BackupParams(IBackupTransport transport, String dirName, ArrayList<String> kvPackages, ArrayList<String> fullPackages, IBackupObserver observer, boolean userInitiated) {
            this.transport = transport;
            this.dirName = dirName;
            this.kvPackages = kvPackages;
            this.fullPackages = fullPackages;
            this.observer = observer;
            this.userInitiated = userInitiated;
        }
    }

    class BackupRequest {
        public String packageName;

        BackupRequest(String pkgName) {
            this.packageName = pkgName;
        }

        public String toString() {
            return "BackupRequest{pkg=" + this.packageName + "}";
        }
    }

    enum BackupState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.BackupManagerService.BackupState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.BackupManagerService.BackupState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.BackupManagerService.BackupState.<clinit>():void");
        }
    }

    class ClearDataObserver extends IPackageDataObserver.Stub {
        final /* synthetic */ BackupManagerService this$0;

        ClearDataObserver(BackupManagerService this$0) {
            this.this$0 = this$0;
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            synchronized (this.this$0.mClearDataLock) {
                this.this$0.mClearingData = BackupManagerService.MORE_DEBUG;
                this.this$0.mClearDataLock.notifyAll();
            }
        }
    }

    class ClearParams {
        public PackageInfo packageInfo;
        final /* synthetic */ BackupManagerService this$0;
        public IBackupTransport transport;

        ClearParams(BackupManagerService this$0, IBackupTransport _transport, PackageInfo _info) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.packageInfo = _info;
        }
    }

    class ClearRetryParams {
        public String packageName;
        final /* synthetic */ BackupManagerService this$0;
        public String transportName;

        ClearRetryParams(BackupManagerService this$0, String transport, String pkg) {
            this.this$0 = this$0;
            this.transportName = transport;
            this.packageName = pkg;
        }
    }

    static class FileMetadata {
        String domain;
        String installerPackageName;
        long mode;
        long mtime;
        String packageName;
        String path;
        long size;
        int type;

        FileMetadata() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append("FileMetadata{");
            sb.append(this.packageName);
            sb.append(',');
            sb.append(this.type);
            sb.append(',');
            sb.append(this.domain);
            sb.append(':');
            sb.append(this.path);
            sb.append(',');
            sb.append(this.size);
            sb.append('}');
            return sb.toString();
        }
    }

    class FullBackupEngine {
        IBackupAgent mAgent;
        File mFilesDir;
        boolean mIncludeApks;
        File mManifestFile;
        File mMetadataFile;
        OutputStream mOutput;
        PackageInfo mPkg;
        FullBackupPreflight mPreflightHook;
        BackupRestoreTask mTimeoutMonitor;
        final /* synthetic */ BackupManagerService this$0;

        class FullBackupRunner implements Runnable {
            IBackupAgent mAgent;
            PackageInfo mPackage;
            ParcelFileDescriptor mPipe;
            boolean mSendApk;
            int mToken;
            byte[] mWidgetData;
            boolean mWriteManifest;
            final /* synthetic */ FullBackupEngine this$1;

            FullBackupRunner(FullBackupEngine this$1, PackageInfo pack, IBackupAgent agent, ParcelFileDescriptor pipe, int token, boolean sendApk, boolean writeManifest, byte[] widgetData) throws IOException {
                this.this$1 = this$1;
                this.mPackage = pack;
                this.mWidgetData = widgetData;
                this.mAgent = agent;
                this.mPipe = ParcelFileDescriptor.dup(pipe.getFileDescriptor());
                this.mToken = token;
                this.mSendApk = sendApk;
                this.mWriteManifest = writeManifest;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                try {
                    FullBackupDataOutput output = new FullBackupDataOutput(this.mPipe);
                    if (this.mWriteManifest) {
                        boolean writeWidgetData = this.mWidgetData != null ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                        this.this$1.writeAppManifest(this.mPackage, this.this$1.mManifestFile, this.mSendApk, writeWidgetData);
                        FullBackup.backupToTar(this.mPackage.packageName, null, null, this.this$1.mFilesDir.getAbsolutePath(), this.this$1.mManifestFile.getAbsolutePath(), output);
                        this.this$1.mManifestFile.delete();
                        if (writeWidgetData) {
                            this.this$1.writeMetadata(this.mPackage, this.this$1.mMetadataFile, this.mWidgetData);
                            FullBackup.backupToTar(this.mPackage.packageName, null, null, this.this$1.mFilesDir.getAbsolutePath(), this.this$1.mMetadataFile.getAbsolutePath(), output);
                            this.this$1.mMetadataFile.delete();
                        }
                    }
                    if (this.mSendApk) {
                        this.this$1.writeApkToBackup(this.mPackage, output);
                    }
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Calling doFullBackup() on " + this.mPackage.packageName);
                    }
                    this.this$1.this$0.prepareOperationTimeout(this.mToken, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this.this$1.mTimeoutMonitor);
                    this.mAgent.doFullBackup(this.mPipe, this.mToken, this.this$1.this$0.mBackupManagerBinder);
                    try {
                        this.mPipe.close();
                    } catch (IOException e) {
                    }
                } catch (IOException e2) {
                    Slog.e(BackupManagerService.TAG, "Error running full backup for " + this.mPackage.packageName);
                } catch (RemoteException e3) {
                    Slog.e(BackupManagerService.TAG, "Remote agent vanished during full backup of " + this.mPackage.packageName);
                    try {
                        this.mPipe.close();
                    } catch (IOException e4) {
                    }
                } catch (Throwable th) {
                    try {
                        this.mPipe.close();
                    } catch (IOException e5) {
                    }
                }
            }
        }

        FullBackupEngine(BackupManagerService this$0, OutputStream output, FullBackupPreflight preflightHook, PackageInfo pkg, boolean alsoApks, BackupRestoreTask timeoutMonitor) {
            this.this$0 = this$0;
            this.mOutput = output;
            this.mPreflightHook = preflightHook;
            this.mPkg = pkg;
            this.mIncludeApks = alsoApks;
            this.mTimeoutMonitor = timeoutMonitor;
            this.mFilesDir = new File("/data/system");
            this.mManifestFile = new File(this.mFilesDir, BackupManagerService.BACKUP_MANIFEST_FILENAME);
            this.mMetadataFile = new File(this.mFilesDir, BackupManagerService.BACKUP_METADATA_FILENAME);
        }

        public int preflightCheck() throws RemoteException {
            if (this.mPreflightHook == null) {
                return BackupManagerService.OP_PENDING;
            }
            if (initializeAgent()) {
                return this.mPreflightHook.preflightFullBackup(this.mPkg, this.mAgent);
            }
            Slog.w(BackupManagerService.TAG, "Unable to bind to full agent for " + this.mPkg.packageName);
            return -1003;
        }

        public int backupOnePackage() throws RemoteException {
            boolean z = BackupManagerService.MORE_DEBUG;
            int result = -1003;
            if (initializeAgent()) {
                ParcelFileDescriptor[] parcelFileDescriptorArr = null;
                try {
                    boolean sendApk;
                    byte[] widgetBlob;
                    int token;
                    PackageInfo packageInfo;
                    IBackupAgent iBackupAgent;
                    ParcelFileDescriptor parcelFileDescriptor;
                    FullBackupRunner runner;
                    parcelFileDescriptorArr = ParcelFileDescriptor.createPipe();
                    ApplicationInfo app = this.mPkg.applicationInfo;
                    boolean isSharedStorage = this.mPkg.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                    if (this.mIncludeApks && !isSharedStorage) {
                        if ((app.privateFlags & BackupManagerService.MSG_RUN_CLEAR) == 0) {
                            sendApk = (app.flags & BackupManagerService.SCHEDULE_FILE_VERSION) != 0 ? (app.flags & DumpState.DUMP_PACKAGES) != 0 ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG : BackupManagerService.DEBUG_BACKUP_TRACE;
                            widgetBlob = AppWidgetBackupBridge.getWidgetState(this.mPkg.packageName, BackupManagerService.OP_PENDING);
                            token = this.this$0.generateToken();
                            packageInfo = this.mPkg;
                            iBackupAgent = this.mAgent;
                            parcelFileDescriptor = parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION];
                            if (!isSharedStorage) {
                                z = BackupManagerService.DEBUG_BACKUP_TRACE;
                            }
                            runner = new FullBackupRunner(this, packageInfo, iBackupAgent, parcelFileDescriptor, token, sendApk, z, widgetBlob);
                            parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                            parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                            new Thread(runner, "app-data-runner").start();
                            this.this$0.routeSocketDataToOutput(parcelFileDescriptorArr[BackupManagerService.OP_PENDING], this.mOutput);
                            if (this.this$0.waitUntilOperationComplete(token)) {
                                Slog.e(BackupManagerService.TAG, "Full backup failed on package " + this.mPkg.packageName);
                            } else {
                                result = BackupManagerService.OP_PENDING;
                            }
                            this.mOutput.flush();
                            if (parcelFileDescriptorArr != null) {
                                if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                                    parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                                }
                                if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                                    parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                                }
                            }
                        }
                    }
                    sendApk = BackupManagerService.MORE_DEBUG;
                    widgetBlob = AppWidgetBackupBridge.getWidgetState(this.mPkg.packageName, BackupManagerService.OP_PENDING);
                    token = this.this$0.generateToken();
                    packageInfo = this.mPkg;
                    iBackupAgent = this.mAgent;
                    parcelFileDescriptor = parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION];
                    if (isSharedStorage) {
                        z = BackupManagerService.DEBUG_BACKUP_TRACE;
                    }
                    runner = new FullBackupRunner(this, packageInfo, iBackupAgent, parcelFileDescriptor, token, sendApk, z, widgetBlob);
                    parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                    parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                    new Thread(runner, "app-data-runner").start();
                    this.this$0.routeSocketDataToOutput(parcelFileDescriptorArr[BackupManagerService.OP_PENDING], this.mOutput);
                    if (this.this$0.waitUntilOperationComplete(token)) {
                        result = BackupManagerService.OP_PENDING;
                    } else {
                        Slog.e(BackupManagerService.TAG, "Full backup failed on package " + this.mPkg.packageName);
                    }
                    try {
                        this.mOutput.flush();
                        if (parcelFileDescriptorArr != null) {
                            if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                                parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                            }
                            if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                                parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                            }
                        }
                    } catch (IOException e) {
                        Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                        result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        tearDown();
                        return result;
                    }
                } catch (IOException e2) {
                    Slog.e(BackupManagerService.TAG, "Error backing up " + this.mPkg.packageName + ": " + e2.getMessage());
                    result = -1003;
                    try {
                        this.mOutput.flush();
                        if (parcelFileDescriptorArr != null) {
                            if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                                parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                            }
                            if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                                parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                            }
                        }
                    } catch (IOException e3) {
                        Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                        result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        tearDown();
                        return result;
                    }
                } catch (Throwable th) {
                    try {
                        this.mOutput.flush();
                        if (parcelFileDescriptorArr != null) {
                            if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                                parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                            }
                            if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                                parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                            }
                        }
                    } catch (IOException e4) {
                        Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                    }
                }
            } else {
                Slog.w(BackupManagerService.TAG, "Unable to bind to full agent for " + this.mPkg.packageName);
            }
            tearDown();
            return result;
        }

        public void sendQuotaExceeded(long backupDataBytes, long quotaBytes) {
            if (initializeAgent()) {
                try {
                    this.mAgent.doQuotaExceeded(backupDataBytes, quotaBytes);
                } catch (RemoteException e) {
                    Slog.e(BackupManagerService.TAG, "Remote exception while telling agent about quota exceeded");
                }
            }
        }

        private boolean initializeAgent() {
            if (this.mAgent == null) {
                this.mAgent = this.this$0.bindToAgentSynchronous(this.mPkg.applicationInfo, BackupManagerService.SCHEDULE_FILE_VERSION);
            }
            if (this.mAgent != null) {
                return BackupManagerService.DEBUG_BACKUP_TRACE;
            }
            return BackupManagerService.MORE_DEBUG;
        }

        private void writeApkToBackup(PackageInfo pkg, FullBackupDataOutput output) {
            String appSourceDir = pkg.applicationInfo.getBaseCodePath();
            FullBackup.backupToTar(pkg.packageName, "a", null, new File(appSourceDir).getParent(), appSourceDir, output);
            File obbDir = new UserEnvironment(BackupManagerService.OP_PENDING).buildExternalStorageAppObbDirs(pkg.packageName)[BackupManagerService.OP_PENDING];
            if (obbDir != null) {
                File[] obbFiles = obbDir.listFiles();
                if (obbFiles != null) {
                    String obbDirName = obbDir.getAbsolutePath();
                    int length = obbFiles.length;
                    for (int i = BackupManagerService.OP_PENDING; i < length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                        FullBackup.backupToTar(pkg.packageName, "obb", null, obbDirName, obbFiles[i].getAbsolutePath(), output);
                    }
                }
            }
        }

        private void writeAppManifest(PackageInfo pkg, File manifestFile, boolean withApk, boolean withWidgets) throws IOException {
            StringBuilder builder = new StringBuilder(DumpState.DUMP_PREFERRED);
            StringBuilderPrinter printer = new StringBuilderPrinter(builder);
            printer.println(Integer.toString(BackupManagerService.SCHEDULE_FILE_VERSION));
            printer.println(pkg.packageName);
            printer.println(Integer.toString(pkg.versionCode));
            printer.println(Integer.toString(VERSION.SDK_INT));
            String installerName = this.this$0.mPackageManager.getInstallerPackageName(pkg.packageName);
            if (installerName == null) {
                installerName = "";
            }
            printer.println(installerName);
            printer.println(withApk ? "1" : "0");
            if (pkg.signatures == null) {
                printer.println("0");
            } else {
                printer.println(Integer.toString(pkg.signatures.length));
                Signature[] signatureArr = pkg.signatures;
                int length = signatureArr.length;
                for (int i = BackupManagerService.OP_PENDING; i < length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                    printer.println(signatureArr[i].toCharsString());
                }
            }
            FileOutputStream outstream = new FileOutputStream(manifestFile);
            outstream.write(builder.toString().getBytes());
            outstream.close();
            manifestFile.setLastModified(0);
        }

        private void writeMetadata(PackageInfo pkg, File destination, byte[] widgetData) throws IOException {
            StringBuilder b = new StringBuilder(BackupManagerService.PBKDF2_SALT_SIZE);
            StringBuilderPrinter printer = new StringBuilderPrinter(b);
            printer.println(Integer.toString(BackupManagerService.SCHEDULE_FILE_VERSION));
            printer.println(pkg.packageName);
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destination));
            DataOutputStream out = new DataOutputStream(bout);
            bout.write(b.toString().getBytes());
            if (widgetData != null && widgetData.length > 0) {
                out.writeInt(BackupManagerService.BACKUP_WIDGET_METADATA_TOKEN);
                out.writeInt(widgetData.length);
                out.write(widgetData);
            }
            bout.flush();
            out.close();
            destination.setLastModified(0);
        }

        private void tearDown() {
            if (this.mPkg != null) {
                this.this$0.tearDownAgentAndKill(this.mPkg.applicationInfo);
            }
        }
    }

    class FullBackupEntry implements Comparable<FullBackupEntry> {
        long lastBackup;
        String packageName;
        final /* synthetic */ BackupManagerService this$0;

        FullBackupEntry(BackupManagerService this$0, String pkg, long when) {
            this.this$0 = this$0;
            this.packageName = pkg;
            this.lastBackup = when;
        }

        public /* bridge */ /* synthetic */ int compareTo(Object other) {
            return compareTo((FullBackupEntry) other);
        }

        public int compareTo(FullBackupEntry other) {
            if (this.lastBackup < other.lastBackup) {
                return BackupManagerService.OP_TIMEOUT;
            }
            if (this.lastBackup > other.lastBackup) {
                return BackupManagerService.SCHEDULE_FILE_VERSION;
            }
            return BackupManagerService.OP_PENDING;
        }
    }

    class FullBackupObbConnection implements ServiceConnection {
        volatile IObbBackupService mService;
        final /* synthetic */ BackupManagerService this$0;

        FullBackupObbConnection(BackupManagerService this$0) {
            this.this$0 = this$0;
            this.mService = null;
        }

        public void establish() {
            this.this$0.mContext.bindServiceAsUser(new Intent().setComponent(new ComponentName(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, "com.android.sharedstoragebackup.ObbBackupService")), this, BackupManagerService.SCHEDULE_FILE_VERSION, UserHandle.SYSTEM);
        }

        public void tearDown() {
            this.this$0.mContext.unbindService(this);
        }

        public boolean backupObbs(PackageInfo pkg, OutputStream out) {
            boolean success = BackupManagerService.MORE_DEBUG;
            waitForConnection();
            ParcelFileDescriptor[] parcelFileDescriptorArr = null;
            try {
                parcelFileDescriptorArr = ParcelFileDescriptor.createPipe();
                int token = this.this$0.generateToken();
                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, null);
                this.mService.backupObbs(pkg.packageName, parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION], token, this.this$0.mBackupManagerBinder);
                this.this$0.routeSocketDataToOutput(parcelFileDescriptorArr[BackupManagerService.OP_PENDING], out);
                success = this.this$0.waitUntilOperationComplete(token);
                try {
                    out.flush();
                    if (parcelFileDescriptorArr != null) {
                        if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                            parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                        }
                        if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                            parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                        }
                    }
                } catch (IOException e) {
                    Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e);
                }
            } catch (Exception e2) {
                Slog.w(BackupManagerService.TAG, "Unable to back up OBBs for " + pkg, e2);
                try {
                    out.flush();
                    if (parcelFileDescriptorArr != null) {
                        if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                            parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                        }
                        if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                            parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                        }
                    }
                } catch (IOException e3) {
                    Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e3);
                }
            } catch (Throwable th) {
                try {
                    out.flush();
                    if (parcelFileDescriptorArr != null) {
                        if (parcelFileDescriptorArr[BackupManagerService.OP_PENDING] != null) {
                            parcelFileDescriptorArr[BackupManagerService.OP_PENDING].close();
                        }
                        if (parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                            parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                        }
                    }
                } catch (IOException e32) {
                    Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e32);
                }
            }
            return success;
        }

        public void restoreObbFile(String pkgName, ParcelFileDescriptor data, long fileSize, int type, String path, long mode, long mtime, int token, IBackupManager callbackBinder) {
            waitForConnection();
            try {
                this.mService.restoreObbFile(pkgName, data, fileSize, type, path, mode, mtime, token, callbackBinder);
            } catch (Exception e) {
                Slog.w(BackupManagerService.TAG, "Unable to restore OBBs for " + pkgName, e);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void waitForConnection() {
            synchronized (this) {
                while (true) {
                    if (this.mService == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this) {
                this.mService = IObbBackupService.Stub.asInterface(service);
                notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this) {
                this.mService = null;
                notifyAll();
            }
        }
    }

    class FullParams {
        public String curPassword;
        public String encryptPassword;
        public ParcelFileDescriptor fd;
        public final AtomicBoolean latch;
        public IFullBackupRestoreObserver observer;
        final /* synthetic */ BackupManagerService this$0;

        FullParams(BackupManagerService this$0) {
            this.this$0 = this$0;
            this.latch = new AtomicBoolean(BackupManagerService.MORE_DEBUG);
        }
    }

    class FullBackupParams extends FullParams {
        public boolean allApps;
        public boolean doCompress;
        public boolean doWidgets;
        public boolean includeApks;
        public boolean includeObbs;
        public boolean includeShared;
        public boolean includeSystem;
        public String[] packages;
        final /* synthetic */ BackupManagerService this$0;

        FullBackupParams(BackupManagerService this$0, ParcelFileDescriptor output, boolean saveApks, boolean saveObbs, boolean saveShared, boolean alsoWidgets, boolean doAllApps, boolean doSystem, boolean compress, String[] pkgList) {
            this.this$0 = this$0;
            super(this$0);
            this.fd = output;
            this.includeApks = saveApks;
            this.includeObbs = saveObbs;
            this.includeShared = saveShared;
            this.doWidgets = alsoWidgets;
            this.allApps = doAllApps;
            this.includeSystem = doSystem;
            this.doCompress = compress;
            this.packages = pkgList;
        }
    }

    interface FullBackupPreflight {
        long getExpectedSizeOrErrorCode();

        int preflightFullBackup(PackageInfo packageInfo, IBackupAgent iBackupAgent);
    }

    abstract class FullBackupTask implements Runnable {
        IFullBackupRestoreObserver mObserver;
        final /* synthetic */ BackupManagerService this$0;

        FullBackupTask(BackupManagerService this$0, IFullBackupRestoreObserver observer) {
            this.this$0 = this$0;
            this.mObserver = observer;
        }

        final void sendStartBackup() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onStartBackup();
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full backup observer went away: startBackup");
                    this.mObserver = null;
                }
            }
        }

        final void sendOnBackupPackage(String name) {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onBackupPackage(name);
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full backup observer went away: backupPackage");
                    this.mObserver = null;
                }
            }
        }

        final void sendEndBackup() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onEndBackup();
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full backup observer went away: endBackup");
                    this.mObserver = null;
                }
            }
        }
    }

    abstract class RestoreEngine {
        public static final int SUCCESS = 0;
        static final String TAG = "RestoreEngine";
        public static final int TARGET_FAILURE = -2;
        public static final int TRANSPORT_FAILURE = -3;
        private AtomicInteger mResult;
        private AtomicBoolean mRunning;
        final /* synthetic */ BackupManagerService this$0;

        RestoreEngine(BackupManagerService this$0) {
            this.this$0 = this$0;
            this.mRunning = new AtomicBoolean(BackupManagerService.MORE_DEBUG);
            this.mResult = new AtomicInteger(SUCCESS);
        }

        public boolean isRunning() {
            return this.mRunning.get();
        }

        public void setRunning(boolean stillRunning) {
            synchronized (this.mRunning) {
                this.mRunning.set(stillRunning);
                this.mRunning.notifyAll();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int waitForResult() {
            synchronized (this.mRunning) {
                while (true) {
                    if (isRunning()) {
                        try {
                            this.mRunning.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            return getResult();
        }

        public int getResult() {
            return this.mResult.get();
        }

        public void setResult(int result) {
            this.mResult.set(result);
        }
    }

    class FullRestoreEngine extends RestoreEngine {
        private static final /* synthetic */ int[] -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$backup$BackupManagerService$RestorePolicy;
        IBackupAgent mAgent;
        String mAgentPackage;
        boolean mAllowApks;
        boolean mAllowObbs;
        byte[] mBuffer;
        long mBytes;
        final HashSet<String> mClearedPackages;
        final RestoreDeleteObserver mDeleteObserver;
        final RestoreInstallObserver mInstallObserver;
        final HashMap<String, Signature[]> mManifestSignatures;
        BackupRestoreTask mMonitorTask;
        FullBackupObbConnection mObbConnection;
        IFullBackupRestoreObserver mObserver;
        PackageInfo mOnlyPackage;
        final HashMap<String, String> mPackageInstallers;
        final HashMap<String, RestorePolicy> mPackagePolicies;
        ParcelFileDescriptor[] mPipes;
        ApplicationInfo mTargetApp;
        byte[] mWidgetData;
        final /* synthetic */ BackupManagerService this$0;

        class RestoreDeleteObserver extends IPackageDeleteObserver.Stub {
            final AtomicBoolean mDone;
            int mResult;
            final /* synthetic */ FullRestoreEngine this$1;

            RestoreDeleteObserver(FullRestoreEngine this$1) {
                this.this$1 = this$1;
                this.mDone = new AtomicBoolean();
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(BackupManagerService.MORE_DEBUG);
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (true) {
                        if (this.mDone.get()) {
                        } else {
                            try {
                                this.mDone.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }

            public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mDone.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mDone.notifyAll();
                }
            }
        }

        class RestoreFileRunnable implements Runnable {
            IBackupAgent mAgent;
            FileMetadata mInfo;
            ParcelFileDescriptor mSocket;
            int mToken;
            final /* synthetic */ FullRestoreEngine this$1;

            RestoreFileRunnable(FullRestoreEngine this$1, IBackupAgent agent, FileMetadata info, ParcelFileDescriptor socket, int token) throws IOException {
                this.this$1 = this$1;
                this.mAgent = agent;
                this.mInfo = info;
                this.mToken = token;
                this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFile(this.mSocket, this.mInfo.size, this.mInfo.type, this.mInfo.domain, this.mInfo.path, this.mInfo.mode, this.mInfo.mtime, this.mToken, this.this$1.this$0.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreInstallObserver extends PackageInstallObserver {
            final AtomicBoolean mDone;
            String mPackageName;
            int mResult;
            final /* synthetic */ FullRestoreEngine this$1;

            RestoreInstallObserver(FullRestoreEngine this$1) {
                this.this$1 = this$1;
                this.mDone = new AtomicBoolean();
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(BackupManagerService.MORE_DEBUG);
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (true) {
                        if (this.mDone.get()) {
                        } else {
                            try {
                                this.mDone.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }

            int getResult() {
                return this.mResult;
            }

            public void onPackageInstalled(String packageName, int returnCode, String msg, Bundle extras) {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mPackageName = packageName;
                    this.mDone.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mDone.notifyAll();
                }
            }
        }

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues;
            }
            int[] iArr = new int[RestorePolicy.values().length];
            try {
                iArr[RestorePolicy.ACCEPT.ordinal()] = BackupManagerService.SCHEDULE_FILE_VERSION;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RestorePolicy.ACCEPT_IF_APK.ordinal()] = BackupManagerService.MSG_RUN_ADB_BACKUP;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[RestorePolicy.IGNORE.ordinal()] = BackupManagerService.MSG_RUN_RESTORE;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = iArr;
            return iArr;
        }

        public FullRestoreEngine(BackupManagerService this$0, BackupRestoreTask monitorTask, IFullBackupRestoreObserver observer, PackageInfo onlyPackage, boolean allowApks, boolean allowObbs) {
            this.this$0 = this$0;
            super(this$0);
            this.mObbConnection = null;
            this.mPackagePolicies = new HashMap();
            this.mPackageInstallers = new HashMap();
            this.mManifestSignatures = new HashMap();
            this.mClearedPackages = new HashSet();
            this.mPipes = null;
            this.mWidgetData = null;
            this.mInstallObserver = new RestoreInstallObserver(this);
            this.mDeleteObserver = new RestoreDeleteObserver(this);
            this.mMonitorTask = monitorTask;
            this.mObserver = observer;
            this.mOnlyPackage = onlyPackage;
            this.mAllowApks = allowApks;
            this.mAllowObbs = allowObbs;
            this.mBuffer = new byte[DumpState.DUMP_VERSION];
            this.mBytes = 0;
        }

        public IBackupAgent getAgent() {
            return this.mAgent;
        }

        public byte[] getWidgetData() {
            return this.mWidgetData;
        }

        public boolean restoreOneFile(InputStream instream, boolean mustKillAgent) {
            if (isRunning()) {
                boolean z;
                FileMetadata info = readTarHeaders(instream);
                if (info != null) {
                    String pkg = info.packageName;
                    if (!pkg.equals(this.mAgentPackage)) {
                        if (this.mOnlyPackage == null || pkg.equals(this.mOnlyPackage.packageName)) {
                            if (!this.mPackagePolicies.containsKey(pkg)) {
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                            }
                            if (this.mAgent != null) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.d("RestoreEngine", "Saw new package; finalizing old one");
                                }
                                tearDownPipes();
                                tearDownAgent(this.mTargetApp);
                                this.mTargetApp = null;
                                this.mAgentPackage = null;
                            }
                        } else {
                            Slog.w("RestoreEngine", "Expected data for " + this.mOnlyPackage + " but saw " + pkg);
                            setResult(-3);
                            setRunning(BackupManagerService.MORE_DEBUG);
                            return BackupManagerService.MORE_DEBUG;
                        }
                    }
                    if (info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME)) {
                        this.mPackagePolicies.put(pkg, readAppManifest(info, instream));
                        this.mPackageInstallers.put(pkg, info.installerPackageName);
                        skipTarPadding(info.size, instream);
                        sendOnRestorePackage(pkg);
                    } else if (info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME)) {
                        readMetadata(info, instream);
                        skipTarPadding(info.size, instream);
                    } else {
                        boolean okay = BackupManagerService.DEBUG_BACKUP_TRACE;
                        switch (-getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues()[((RestorePolicy) this.mPackagePolicies.get(pkg)).ordinal()]) {
                            case BackupManagerService.SCHEDULE_FILE_VERSION /*1*/:
                                if (info.domain.equals("a")) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d("RestoreEngine", "apk present but ACCEPT");
                                    }
                                    okay = BackupManagerService.MORE_DEBUG;
                                    break;
                                }
                                break;
                            case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                                if (!info.domain.equals("a")) {
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    okay = BackupManagerService.MORE_DEBUG;
                                    break;
                                }
                                Object obj;
                                if (BackupManagerService.DEBUG) {
                                    Slog.d("RestoreEngine", "APK file; installing");
                                }
                                okay = installApk(info, (String) this.mPackageInstallers.get(pkg), instream);
                                HashMap hashMap = this.mPackagePolicies;
                                if (okay) {
                                    obj = RestorePolicy.ACCEPT;
                                } else {
                                    obj = RestorePolicy.IGNORE;
                                }
                                hashMap.put(pkg, obj);
                                skipTarPadding(info.size, instream);
                                return BackupManagerService.DEBUG_BACKUP_TRACE;
                            case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                                okay = BackupManagerService.MORE_DEBUG;
                                break;
                            default:
                                Slog.e("RestoreEngine", "Invalid policy from manifest");
                                okay = BackupManagerService.MORE_DEBUG;
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                break;
                        }
                        if (!isRestorableFile(info)) {
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                        if (BackupManagerService.DEBUG && okay && this.mAgent != null) {
                            Slog.i("RestoreEngine", "Reusing existing agent instance");
                        }
                        if (okay && this.mAgent == null) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d("RestoreEngine", "Need to launch agent for " + pkg);
                            }
                            try {
                                this.mTargetApp = this.this$0.mPackageManager.getApplicationInfo(pkg, BackupManagerService.OP_PENDING);
                                if (!this.mClearedPackages.contains(pkg)) {
                                    if (this.mTargetApp.backupAgentName == null) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d("RestoreEngine", "Clearing app data preparatory to full restore");
                                        }
                                        this.this$0.clearApplicationDataSynchronous(pkg);
                                    }
                                    this.mClearedPackages.add(pkg);
                                }
                                setUpPipes();
                                this.mAgent = this.this$0.bindToAgentSynchronous(this.mTargetApp, BackupManagerService.MSG_RUN_RESTORE);
                                this.mAgentPackage = pkg;
                            } catch (IOException e) {
                            } catch (NameNotFoundException e2) {
                            }
                            try {
                                if (this.mAgent == null) {
                                    Slog.e("RestoreEngine", "Unable to create agent for " + pkg);
                                    okay = BackupManagerService.MORE_DEBUG;
                                    tearDownPipes();
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                }
                            } catch (IOException e3) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.w("RestoreEngine", "io exception on restore socket read: " + e3.getMessage());
                                }
                                setResult(-3);
                                info = null;
                            }
                        }
                        if (okay && !pkg.equals(this.mAgentPackage)) {
                            Slog.e("RestoreEngine", "Restoring data for " + pkg + " but agent is for " + this.mAgentPackage);
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                        if (okay) {
                            boolean agentSuccess = BackupManagerService.DEBUG_BACKUP_TRACE;
                            long toCopy = info.size;
                            int token = this.this$0.generateToken();
                            try {
                                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this.mMonitorTask);
                                if (info.domain.equals("obb")) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d("RestoreEngine", "Restoring OBB file for " + pkg + " : " + info.path);
                                    }
                                    this.mObbConnection.restoreObbFile(pkg, this.mPipes[BackupManagerService.OP_PENDING], info.size, info.type, info.path, info.mode, info.mtime, token, this.this$0.mBackupManagerBinder);
                                } else if (this.mTargetApp.processName.equals("system")) {
                                    Slog.d("RestoreEngine", "system process agent - spinning a thread");
                                    new Thread(new RestoreFileRunnable(this, this.mAgent, info, this.mPipes[BackupManagerService.OP_PENDING], token), "restore-sys-runner").start();
                                } else {
                                    this.mAgent.doRestoreFile(this.mPipes[BackupManagerService.OP_PENDING], info.size, info.type, info.domain, info.path, info.mode, info.mtime, token, this.this$0.mBackupManagerBinder);
                                }
                            } catch (IOException e4) {
                                Slog.d("RestoreEngine", "Couldn't establish restore");
                                agentSuccess = BackupManagerService.MORE_DEBUG;
                                okay = BackupManagerService.MORE_DEBUG;
                            } catch (RemoteException e5) {
                                Slog.e("RestoreEngine", "Agent crashed during full restore");
                                agentSuccess = BackupManagerService.MORE_DEBUG;
                                okay = BackupManagerService.MORE_DEBUG;
                            }
                            if (okay) {
                                boolean pipeOkay = BackupManagerService.DEBUG_BACKUP_TRACE;
                                FileOutputStream fileOutputStream = new FileOutputStream(this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION].getFileDescriptor());
                                while (toCopy > 0) {
                                    int nRead = instream.read(this.mBuffer, BackupManagerService.OP_PENDING, toCopy > ((long) this.mBuffer.length) ? this.mBuffer.length : (int) toCopy);
                                    if (nRead >= 0) {
                                        this.mBytes += (long) nRead;
                                    }
                                    if (nRead <= 0) {
                                        skipTarPadding(info.size, instream);
                                        agentSuccess = this.this$0.waitUntilOperationComplete(token);
                                    } else {
                                        toCopy -= (long) nRead;
                                        if (pipeOkay) {
                                            try {
                                                fileOutputStream.write(this.mBuffer, BackupManagerService.OP_PENDING, nRead);
                                            } catch (IOException e32) {
                                                Slog.e("RestoreEngine", "Failed to write to restore pipe: " + e32.getMessage());
                                                pipeOkay = BackupManagerService.MORE_DEBUG;
                                            }
                                        }
                                    }
                                }
                                skipTarPadding(info.size, instream);
                                agentSuccess = this.this$0.waitUntilOperationComplete(token);
                            }
                            if (!agentSuccess) {
                                Slog.w("RestoreEngine", "Agent failure; ending restore");
                                this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
                                tearDownPipes();
                                tearDownAgent(this.mTargetApp);
                                this.mAgent = null;
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                if (this.mOnlyPackage != null) {
                                    setResult(-2);
                                    setRunning(BackupManagerService.MORE_DEBUG);
                                    return BackupManagerService.MORE_DEBUG;
                                }
                            }
                        }
                        if (!okay) {
                            long bytesToConsume = (info.size + 511) & -512;
                            while (bytesToConsume > 0) {
                                long nRead2 = (long) instream.read(this.mBuffer, BackupManagerService.OP_PENDING, bytesToConsume > ((long) this.mBuffer.length) ? this.mBuffer.length : (int) bytesToConsume);
                                if (nRead2 >= 0) {
                                    this.mBytes += nRead2;
                                }
                                if (nRead2 > 0) {
                                    bytesToConsume -= nRead2;
                                }
                            }
                        }
                    }
                }
                if (info == null) {
                    tearDownPipes();
                    setRunning(BackupManagerService.MORE_DEBUG);
                    if (mustKillAgent) {
                        tearDownAgent(this.mTargetApp);
                    }
                }
                if (info != null) {
                    z = BackupManagerService.DEBUG_BACKUP_TRACE;
                } else {
                    z = BackupManagerService.MORE_DEBUG;
                }
                return z;
            }
            Slog.w("RestoreEngine", "Restore engine used after halting");
            return BackupManagerService.MORE_DEBUG;
        }

        void setUpPipes() throws IOException {
            this.mPipes = ParcelFileDescriptor.createPipe();
        }

        void tearDownPipes() {
            if (this.mPipes != null) {
                try {
                    this.mPipes[BackupManagerService.OP_PENDING].close();
                    this.mPipes[BackupManagerService.OP_PENDING] = null;
                    this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                    this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                } catch (IOException e) {
                    Slog.w("RestoreEngine", "Couldn't close agent pipes", e);
                }
                this.mPipes = null;
            }
        }

        void tearDownAgent(ApplicationInfo app) {
            if (this.mAgent != null) {
                this.this$0.tearDownAgentAndKill(app);
                this.mAgent = null;
            }
        }

        void handleTimeout() {
            tearDownPipes();
            setResult(-2);
            setRunning(BackupManagerService.MORE_DEBUG);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        boolean installApk(FileMetadata info, String installerPackage, InputStream instream) {
            boolean okay = BackupManagerService.DEBUG_BACKUP_TRACE;
            if (BackupManagerService.DEBUG) {
                Slog.d("RestoreEngine", "Installing from backup: " + info.packageName);
            }
            File apkFile = new File(this.this$0.mDataDir, info.packageName);
            try {
                FileOutputStream apkStream = new FileOutputStream(apkFile);
                byte[] buffer = new byte[DumpState.DUMP_VERSION];
                long size = info.size;
                while (size > 0) {
                    long toRead;
                    if (((long) buffer.length) < size) {
                        toRead = (long) buffer.length;
                    } else {
                        toRead = size;
                    }
                    int didRead = instream.read(buffer, BackupManagerService.OP_PENDING, (int) toRead);
                    if (didRead >= 0) {
                        this.mBytes += (long) didRead;
                    }
                    apkStream.write(buffer, BackupManagerService.OP_PENDING, didRead);
                    size -= (long) didRead;
                }
                apkStream.close();
                apkFile.setReadable(BackupManagerService.DEBUG_BACKUP_TRACE, BackupManagerService.MORE_DEBUG);
                Uri packageUri = Uri.fromFile(apkFile);
                this.mInstallObserver.reset();
                this.this$0.mPackageManager.installPackage(packageUri, this.mInstallObserver, 34, installerPackage);
                this.mInstallObserver.waitForCompletion();
                if (this.mInstallObserver.getResult() != BackupManagerService.SCHEDULE_FILE_VERSION) {
                    if (this.mPackagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                        okay = BackupManagerService.MORE_DEBUG;
                    }
                } else {
                    BackupManagerService backupManagerService;
                    boolean uninstall = BackupManagerService.MORE_DEBUG;
                    if (this.mInstallObserver.mPackageName.equals(info.packageName)) {
                        try {
                            backupManagerService = this.this$0;
                            PackageInfo pkg = r0.mPackageManager.getPackageInfo(info.packageName, 64);
                            if ((pkg.applicationInfo.flags & DumpState.DUMP_VERSION) == 0) {
                                Slog.w("RestoreEngine", "Restore stream contains apk of package " + info.packageName + " but it disallows backup/restore");
                                okay = BackupManagerService.MORE_DEBUG;
                            } else {
                                if (BackupManagerService.signaturesMatch((Signature[]) this.mManifestSignatures.get(info.packageName), pkg)) {
                                    int i = pkg.applicationInfo.uid;
                                    if (r0 < BackupManagerService.PBKDF2_HASH_ROUNDS) {
                                        if (pkg.applicationInfo.backupAgentName == null) {
                                            Slog.w("RestoreEngine", "Installed app " + info.packageName + " has restricted uid and no agent");
                                            okay = BackupManagerService.MORE_DEBUG;
                                        }
                                    }
                                } else {
                                    Slog.w("RestoreEngine", "Installed app " + info.packageName + " signatures do not match restore manifest");
                                    okay = BackupManagerService.MORE_DEBUG;
                                    uninstall = BackupManagerService.DEBUG_BACKUP_TRACE;
                                }
                            }
                        } catch (NameNotFoundException e) {
                            Slog.w("RestoreEngine", "Install of package " + info.packageName + " succeeded but now not found");
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                    } else {
                        String str = info.packageName;
                        Slog.w("RestoreEngine", "Restore stream claimed to include apk for " + r0 + " but apk was really " + this.mInstallObserver.mPackageName);
                        okay = BackupManagerService.MORE_DEBUG;
                        uninstall = BackupManagerService.DEBUG_BACKUP_TRACE;
                    }
                    if (uninstall) {
                        this.mDeleteObserver.reset();
                        backupManagerService = this.this$0;
                        r0.mPackageManager.deletePackage(this.mInstallObserver.mPackageName, this.mDeleteObserver, BackupManagerService.OP_PENDING);
                        this.mDeleteObserver.waitForCompletion();
                    }
                }
                apkFile.delete();
                return okay;
            } catch (IOException e2) {
                Slog.e("RestoreEngine", "Unable to transcribe restored apk for install");
                return BackupManagerService.MORE_DEBUG;
            } catch (Throwable th) {
                apkFile.delete();
            }
        }

        void skipTarPadding(long size, InputStream instream) throws IOException {
            long partial = (size + 512) % 512;
            if (partial > 0) {
                int needed = 512 - ((int) partial);
                if (readExactly(instream, new byte[needed], BackupManagerService.OP_PENDING, needed) == needed) {
                    this.mBytes += (long) needed;
                    return;
                }
                throw new IOException("Unexpected EOF in padding");
            }
        }

        void readMetadata(FileMetadata info, InputStream instream) throws IOException {
            if (info.size > 65536) {
                throw new IOException("Metadata too big; corrupt? size=" + info.size);
            }
            byte[] buffer = new byte[((int) info.size)];
            if (((long) readExactly(instream, buffer, BackupManagerService.OP_PENDING, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                String[] str = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
                int offset = extractLine(buffer, BackupManagerService.OP_PENDING, str);
                int version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                if (version == BackupManagerService.SCHEDULE_FILE_VERSION) {
                    offset = extractLine(buffer, offset, str);
                    String pkg = str[BackupManagerService.OP_PENDING];
                    if (info.packageName.equals(pkg)) {
                        ByteArrayInputStream bin = new ByteArrayInputStream(buffer, offset, buffer.length - offset);
                        DataInputStream in = new DataInputStream(bin);
                        while (bin.available() > 0) {
                            int token = in.readInt();
                            int size = in.readInt();
                            if (size <= DumpState.DUMP_INSTALLS) {
                                switch (token) {
                                    case BackupManagerService.BACKUP_WIDGET_METADATA_TOKEN /*33549569*/:
                                        this.mWidgetData = new byte[size];
                                        in.read(this.mWidgetData);
                                        break;
                                    default:
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i("RestoreEngine", "Ignoring metadata blob " + Integer.toHexString(token) + " for " + info.packageName);
                                        }
                                        in.skipBytes(size);
                                        break;
                                }
                            }
                            throw new IOException("Datum " + Integer.toHexString(token) + " too big; corrupt? size=" + info.size);
                        }
                        return;
                    }
                    Slog.w("RestoreEngine", "Metadata mismatch: package " + info.packageName + " but widget data for " + pkg);
                    return;
                }
                Slog.w("RestoreEngine", "Unsupported metadata version " + version);
                return;
            }
            throw new IOException("Unexpected EOF in widget data");
        }

        RestorePolicy readAppManifest(FileMetadata info, InputStream instream) throws IOException {
            if (info.size > 65536) {
                throw new IOException("Restore manifest too big; corrupt? size=" + info.size);
            }
            byte[] buffer = new byte[((int) info.size)];
            if (((long) readExactly(instream, buffer, BackupManagerService.OP_PENDING, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                RestorePolicy policy = RestorePolicy.IGNORE;
                String[] str = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
                try {
                    int offset = extractLine(buffer, BackupManagerService.OP_PENDING, str);
                    int version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                    if (version == BackupManagerService.SCHEDULE_FILE_VERSION) {
                        offset = extractLine(buffer, offset, str);
                        String manifestPackage = str[BackupManagerService.OP_PENDING];
                        if (manifestPackage.equals(info.packageName)) {
                            offset = extractLine(buffer, offset, str);
                            version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            offset = extractLine(buffer, offset, str);
                            Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            offset = extractLine(buffer, offset, str);
                            info.installerPackageName = str[BackupManagerService.OP_PENDING].length() > 0 ? str[BackupManagerService.OP_PENDING] : null;
                            offset = extractLine(buffer, offset, str);
                            boolean hasApk = str[BackupManagerService.OP_PENDING].equals("1");
                            offset = extractLine(buffer, offset, str);
                            int numSigs = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            if (numSigs > 0) {
                                Object sigs = new Signature[numSigs];
                                for (int i = BackupManagerService.OP_PENDING; i < numSigs; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                                    offset = extractLine(buffer, offset, str);
                                    sigs[i] = new Signature(str[BackupManagerService.OP_PENDING]);
                                }
                                this.mManifestSignatures.put(info.packageName, sigs);
                                try {
                                    PackageInfo pkgInfo = this.this$0.mPackageManager.getPackageInfo(info.packageName, 64);
                                    if ((DumpState.DUMP_VERSION & pkgInfo.applicationInfo.flags) != 0) {
                                        int i2 = pkgInfo.applicationInfo.uid;
                                        if (r0 < BackupManagerService.PBKDF2_HASH_ROUNDS) {
                                            if (pkgInfo.applicationInfo.backupAgentName == null) {
                                                Slog.w("RestoreEngine", "Package " + info.packageName + " is system level with no agent");
                                                if (policy == RestorePolicy.ACCEPT_IF_APK && !hasApk) {
                                                    Slog.i("RestoreEngine", "Cannot restore package " + info.packageName + " without the matching .apk");
                                                }
                                            }
                                        }
                                        if (BackupManagerService.signaturesMatch(sigs, pkgInfo)) {
                                            i2 = pkgInfo.versionCode;
                                            if (r0 >= version) {
                                                Slog.i("RestoreEngine", "Sig + version match; taking data");
                                                policy = RestorePolicy.ACCEPT;
                                            } else if (this.mAllowApks) {
                                                Slog.i("RestoreEngine", "Data version " + version + " is newer than installed version " + pkgInfo.versionCode + " - requiring apk");
                                                policy = RestorePolicy.ACCEPT_IF_APK;
                                            } else {
                                                Slog.i("RestoreEngine", "Data requires newer version " + version + "; ignoring");
                                                policy = RestorePolicy.IGNORE;
                                            }
                                        } else {
                                            Slog.w("RestoreEngine", "Restore manifest signatures do not match installed application for " + info.packageName);
                                        }
                                        Slog.i("RestoreEngine", "Cannot restore package " + info.packageName + " without the matching .apk");
                                    } else {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i("RestoreEngine", "Restore manifest from " + info.packageName + " but allowBackup=false");
                                        }
                                        Slog.i("RestoreEngine", "Cannot restore package " + info.packageName + " without the matching .apk");
                                    }
                                } catch (NameNotFoundException e) {
                                    if (this.mAllowApks) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i("RestoreEngine", "Package " + info.packageName + " not installed; requiring apk in dataset");
                                        }
                                        policy = RestorePolicy.ACCEPT_IF_APK;
                                    } else {
                                        policy = RestorePolicy.IGNORE;
                                    }
                                }
                            } else {
                                Slog.i("RestoreEngine", "Missing signature on backed-up package " + info.packageName);
                            }
                        } else {
                            Slog.i("RestoreEngine", "Expected package " + info.packageName + " but restore manifest claims " + manifestPackage);
                        }
                    } else {
                        Slog.i("RestoreEngine", "Unknown restore manifest version " + version + " for package " + info.packageName);
                    }
                } catch (NumberFormatException e2) {
                    Slog.w("RestoreEngine", "Corrupt restore manifest for package " + info.packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w("RestoreEngine", e3.getMessage());
                }
                return policy;
            }
            throw new IOException("Unexpected EOF in manifest");
        }

        int extractLine(byte[] buffer, int offset, String[] outStr) throws IOException {
            int end = buffer.length;
            if (offset >= end) {
                throw new IOException("Incomplete data");
            }
            int pos = offset;
            while (pos < end && buffer[pos] != BackupManagerService.MSG_RUN_ADB_RESTORE) {
                pos += BackupManagerService.SCHEDULE_FILE_VERSION;
            }
            outStr[BackupManagerService.OP_PENDING] = new String(buffer, offset, pos - offset);
            return pos + BackupManagerService.SCHEDULE_FILE_VERSION;
        }

        void dumpFileMetadata(FileMetadata info) {
        }

        FileMetadata readTarHeaders(InputStream instream) throws IOException {
            IOException e;
            byte[] block = new byte[BackupManagerService.PBKDF2_SALT_SIZE];
            FileMetadata fileMetadata = null;
            if (readTarHeader(instream, block)) {
                try {
                    FileMetadata info = new FileMetadata();
                    try {
                        info.size = extractRadix(block, 124, BackupManagerService.MSG_RETRY_CLEAR, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.mtime = extractRadix(block, 136, BackupManagerService.MSG_RETRY_CLEAR, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.mode = extractRadix(block, 100, BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.path = extractString(block, 345, 155);
                        String path = extractString(block, BackupManagerService.OP_PENDING, 100);
                        if (path.length() > 0) {
                            if (info.path.length() > 0) {
                                info.path += '/';
                            }
                            info.path += path;
                        }
                        int typeChar = block[156];
                        if (typeChar == 120) {
                            boolean gotHeader = readPaxExtendedHeader(instream, info);
                            if (gotHeader) {
                                gotHeader = readTarHeader(instream, block);
                            }
                            if (gotHeader) {
                                typeChar = block[156];
                            } else {
                                throw new IOException("Bad or missing pax header");
                            }
                        }
                        switch (typeChar) {
                            case BackupManagerService.OP_PENDING /*0*/:
                                return null;
                            case H.NOTIFY_APP_TRANSITION_CANCELLED /*48*/:
                                info.type = BackupManagerService.SCHEDULE_FILE_VERSION;
                                break;
                            case H.NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED /*53*/:
                                info.type = BackupManagerService.MSG_RUN_ADB_BACKUP;
                                if (info.size != 0) {
                                    Slog.w("RestoreEngine", "Directory entry with nonzero size in header");
                                    info.size = 0;
                                    break;
                                }
                                break;
                            default:
                                Slog.e("RestoreEngine", "Unknown tar entity type: " + typeChar);
                                throw new IOException("Unknown entity type " + typeChar);
                        }
                        if ("shared/".regionMatches(BackupManagerService.OP_PENDING, info.path, BackupManagerService.OP_PENDING, "shared/".length())) {
                            info.path = info.path.substring("shared/".length());
                            info.packageName = BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE;
                            info.domain = "shared";
                            if (BackupManagerService.DEBUG) {
                                Slog.i("RestoreEngine", "File in shared storage: " + info.path);
                            }
                        } else if ("apps/".regionMatches(BackupManagerService.OP_PENDING, info.path, BackupManagerService.OP_PENDING, "apps/".length())) {
                            info.path = info.path.substring("apps/".length());
                            int slash = info.path.indexOf(47);
                            if (slash < 0) {
                                throw new IOException("Illegal semantic path in " + info.path);
                            }
                            info.packageName = info.path.substring(BackupManagerService.OP_PENDING, slash);
                            info.path = info.path.substring(slash + BackupManagerService.SCHEDULE_FILE_VERSION);
                            if (!(info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME) || info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME))) {
                                slash = info.path.indexOf(47);
                                if (slash < 0) {
                                    throw new IOException("Illegal semantic path in non-manifest " + info.path);
                                }
                                info.domain = info.path.substring(BackupManagerService.OP_PENDING, slash);
                                info.path = info.path.substring(slash + BackupManagerService.SCHEDULE_FILE_VERSION);
                            }
                        }
                        fileMetadata = info;
                    } catch (IOException e2) {
                        e = e2;
                        fileMetadata = info;
                        if (BackupManagerService.DEBUG) {
                            Slog.e("RestoreEngine", "Parse error in header: " + e.getMessage());
                        }
                        throw e;
                    }
                } catch (IOException e3) {
                    e = e3;
                    if (BackupManagerService.DEBUG) {
                        Slog.e("RestoreEngine", "Parse error in header: " + e.getMessage());
                    }
                    throw e;
                }
            }
            return fileMetadata;
        }

        private boolean isRestorableFile(FileMetadata info) {
            if ("c".equals(info.domain)) {
                return BackupManagerService.MORE_DEBUG;
            }
            if (("r".equals(info.domain) && info.path.startsWith("no_backup/")) || info.path.contains("..") || info.path.contains("//")) {
                return BackupManagerService.MORE_DEBUG;
            }
            return BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        private void HEXLOG(byte[] block) {
            int offset = BackupManagerService.OP_PENDING;
            int todo = block.length;
            StringBuilder buf = new StringBuilder(64);
            while (todo > 0) {
                Object[] objArr = new Object[BackupManagerService.SCHEDULE_FILE_VERSION];
                objArr[BackupManagerService.OP_PENDING] = Integer.valueOf(offset);
                buf.append(String.format("%04x   ", objArr));
                int numThisLine = todo > 16 ? 16 : todo;
                for (int i = BackupManagerService.OP_PENDING; i < numThisLine; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                    objArr = new Object[BackupManagerService.SCHEDULE_FILE_VERSION];
                    objArr[BackupManagerService.OP_PENDING] = Byte.valueOf(block[offset + i]);
                    buf.append(String.format("%02x ", objArr));
                }
                Slog.i("hexdump", buf.toString());
                buf.setLength(BackupManagerService.OP_PENDING);
                todo -= numThisLine;
                offset += numThisLine;
            }
        }

        int readExactly(InputStream in, byte[] buffer, int offset, int size) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
            int soFar = BackupManagerService.OP_PENDING;
            while (soFar < size) {
                int nRead = in.read(buffer, offset + soFar, size - soFar);
                if (nRead <= 0) {
                    break;
                }
                soFar += nRead;
            }
            return soFar;
        }

        boolean readTarHeader(InputStream instream, byte[] block) throws IOException {
            int got = readExactly(instream, block, BackupManagerService.OP_PENDING, BackupManagerService.PBKDF2_SALT_SIZE);
            if (got == 0) {
                return BackupManagerService.MORE_DEBUG;
            }
            if (got < BackupManagerService.PBKDF2_SALT_SIZE) {
                throw new IOException("Unable to read full block header");
            }
            this.mBytes += 512;
            return BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        boolean readPaxExtendedHeader(InputStream instream, FileMetadata info) throws IOException {
            if (info.size > 32768) {
                Slog.w("RestoreEngine", "Suspiciously large pax header size " + info.size + " - aborting");
                throw new IOException("Sanity failure: pax header size " + info.size);
            }
            byte[] data = new byte[(((int) ((info.size + 511) >> BackupManagerService.MSG_FULL_CONFIRMATION_TIMEOUT)) * BackupManagerService.PBKDF2_SALT_SIZE)];
            if (readExactly(instream, data, BackupManagerService.OP_PENDING, data.length) < data.length) {
                throw new IOException("Unable to read full pax header");
            }
            this.mBytes += (long) data.length;
            int contentSize = (int) info.size;
            int offset = BackupManagerService.OP_PENDING;
            do {
                int eol = offset + BackupManagerService.SCHEDULE_FILE_VERSION;
                while (eol < contentSize && data[eol] != 32) {
                    eol += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
                if (eol >= contentSize) {
                    throw new IOException("Invalid pax data");
                }
                int linelen = (int) extractRadix(data, offset, eol - offset, BackupManagerService.MSG_RUN_ADB_RESTORE);
                int key = eol + BackupManagerService.SCHEDULE_FILE_VERSION;
                eol = (offset + linelen) + BackupManagerService.OP_TIMEOUT;
                int value = key + BackupManagerService.SCHEDULE_FILE_VERSION;
                while (data[value] != 61 && value <= eol) {
                    value += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
                if (value > eol) {
                    throw new IOException("Invalid pax declaration");
                }
                String keyStr = new String(data, key, value - key, "UTF-8");
                String valStr = new String(data, value + BackupManagerService.SCHEDULE_FILE_VERSION, (eol - value) + BackupManagerService.OP_TIMEOUT, "UTF-8");
                if ("path".equals(keyStr)) {
                    info.path = valStr;
                } else if ("size".equals(keyStr)) {
                    info.size = Long.parseLong(valStr);
                } else if (BackupManagerService.DEBUG) {
                    Slog.i("RestoreEngine", "Unhandled pax key: " + key);
                }
                offset += linelen;
            } while (offset < contentSize);
            return BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long extractRadix(byte[] data, int offset, int maxChars, int radix) throws IOException {
            long value = 0;
            int end = offset + maxChars;
            int i = offset;
            while (i < end) {
                byte b = data[i];
                if (!(b == null || b == 32)) {
                    if (b < 48 || b > (radix + 48) + BackupManagerService.OP_TIMEOUT) {
                        throw new IOException("Invalid number in header: '" + ((char) b) + "' for radix " + radix);
                    }
                    value = (((long) radix) * value) + ((long) (b - 48));
                    i += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
            }
            return value;
        }

        String extractString(byte[] data, int offset, int maxChars) throws IOException {
            int end = offset + maxChars;
            int eos = offset;
            while (eos < end && data[eos] != null) {
                eos += BackupManagerService.SCHEDULE_FILE_VERSION;
            }
            return new String(data, offset, eos - offset, "US-ASCII");
        }

        void sendStartRestore() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onStartRestore();
                } catch (RemoteException e) {
                    Slog.w("RestoreEngine", "full restore observer went away: startRestore");
                    this.mObserver = null;
                }
            }
        }

        void sendOnRestorePackage(String name) {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onRestorePackage(name);
                } catch (RemoteException e) {
                    Slog.w("RestoreEngine", "full restore observer went away: restorePackage");
                    this.mObserver = null;
                }
            }
        }

        void sendEndRestore() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onEndRestore();
                } catch (RemoteException e) {
                    Slog.w("RestoreEngine", "full restore observer went away: endRestore");
                    this.mObserver = null;
                }
            }
        }
    }

    class FullRestoreParams extends FullParams {
        final /* synthetic */ BackupManagerService this$0;

        FullRestoreParams(BackupManagerService this$0, ParcelFileDescriptor input) {
            this.this$0 = this$0;
            super(this$0);
            this.fd = input;
        }
    }

    public static final class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
            BackupManagerService.sInstance = new Trampoline(context);
        }

        public void onStart() {
            publishBinderService("backup", BackupManagerService.sInstance);
        }

        public void onUnlockUser(int userId) {
            boolean z = BackupManagerService.DEBUG_BACKUP_TRACE;
            if (userId == 0) {
                BackupManagerService.sInstance.initialize(userId);
                if (!BackupManagerService.backupSettingMigrated(userId)) {
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Backup enable apparently not migrated");
                    }
                    ContentResolver r = BackupManagerService.sInstance.mContext.getContentResolver();
                    int enableState = Secure.getIntForUser(r, BackupManagerService.BACKUP_ENABLE_FILE, BackupManagerService.OP_TIMEOUT, userId);
                    if (enableState >= 0) {
                        if (BackupManagerService.DEBUG) {
                            boolean z2;
                            String str = BackupManagerService.TAG;
                            StringBuilder append = new StringBuilder().append("Migrating enable state ");
                            if (enableState != 0) {
                                z2 = BackupManagerService.DEBUG_BACKUP_TRACE;
                            } else {
                                z2 = BackupManagerService.MORE_DEBUG;
                            }
                            Slog.i(str, append.append(z2).toString());
                        }
                        if (enableState == 0) {
                            z = BackupManagerService.MORE_DEBUG;
                        }
                        BackupManagerService.writeBackupEnableState(z, userId);
                        Secure.putStringForUser(r, BackupManagerService.BACKUP_ENABLE_FILE, null, userId);
                    } else if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Backup not yet configured; retaining null enable state");
                    }
                }
                try {
                    BackupManagerService.sInstance.setBackupEnabled(BackupManagerService.readBackupEnableState(userId));
                } catch (RemoteException e) {
                }
            }
        }
    }

    class Operation {
        public BackupRestoreTask callback;
        public int state;
        final /* synthetic */ BackupManagerService this$0;

        Operation(BackupManagerService this$0, int initialState, BackupRestoreTask callbackObj) {
            this.this$0 = this$0;
            this.state = initialState;
            this.callback = callbackObj;
        }
    }

    class PerformAdbBackupTask extends FullBackupTask implements BackupRestoreTask {
        boolean mAllApps;
        FullBackupEngine mBackupEngine;
        boolean mCompress;
        String mCurrentPassword;
        PackageInfo mCurrentTarget;
        DeflaterOutputStream mDeflater;
        boolean mDoWidgets;
        String mEncryptPassword;
        boolean mIncludeApks;
        boolean mIncludeObbs;
        boolean mIncludeShared;
        boolean mIncludeSystem;
        final AtomicBoolean mLatch;
        ParcelFileDescriptor mOutputFile;
        ArrayList<String> mPackages;
        final /* synthetic */ BackupManagerService this$0;

        PerformAdbBackupTask(BackupManagerService this$0, ParcelFileDescriptor fd, IFullBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, String curPassword, String encryptPassword, boolean doAllApps, boolean doSystem, boolean doCompress, String[] packages, AtomicBoolean latch) {
            ArrayList arrayList;
            this.this$0 = this$0;
            super(this$0, observer);
            this.mLatch = latch;
            this.mOutputFile = fd;
            this.mIncludeApks = includeApks;
            this.mIncludeObbs = includeObbs;
            this.mIncludeShared = includeShared;
            this.mDoWidgets = doWidgets;
            this.mAllApps = doAllApps;
            this.mIncludeSystem = doSystem;
            if (packages == null) {
                arrayList = new ArrayList();
            } else {
                arrayList = new ArrayList(Arrays.asList(packages));
            }
            this.mPackages = arrayList;
            this.mCurrentPassword = curPassword;
            if (encryptPassword == null || "".equals(encryptPassword)) {
                this.mEncryptPassword = curPassword;
            } else {
                this.mEncryptPassword = encryptPassword;
            }
            this.mCompress = doCompress;
        }

        void addPackagesToSet(TreeMap<String, PackageInfo> set, List<String> pkgNames) {
            for (String pkgName : pkgNames) {
                if (!set.containsKey(pkgName)) {
                    try {
                        set.put(pkgName, this.this$0.mPackageManager.getPackageInfo(pkgName, 64));
                    } catch (NameNotFoundException e) {
                        Slog.w(BackupManagerService.TAG, "Unknown package " + pkgName + ", skipping");
                    }
                }
            }
        }

        private OutputStream emitAesBackupHeader(StringBuilder headerbuf, OutputStream ofstream) throws Exception {
            byte[] newUserSalt = this.this$0.randomBytes(BackupManagerService.PBKDF2_SALT_SIZE);
            SecretKey userKey = this.this$0.buildPasswordKey(BackupManagerService.PBKDF_CURRENT, this.mEncryptPassword, newUserSalt, BackupManagerService.PBKDF2_HASH_ROUNDS);
            byte[] masterPw = new byte[32];
            this.this$0.mRng.nextBytes(masterPw);
            byte[] checksumSalt = this.this$0.randomBytes(BackupManagerService.PBKDF2_SALT_SIZE);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec masterKeySpec = new SecretKeySpec(masterPw, "AES");
            c.init(BackupManagerService.SCHEDULE_FILE_VERSION, masterKeySpec);
            OutputStream finalOutput = new CipherOutputStream(ofstream, c);
            headerbuf.append(BackupManagerService.ENCRYPTION_ALGORITHM_NAME);
            headerbuf.append('\n');
            headerbuf.append(this.this$0.byteArrayToHex(newUserSalt));
            headerbuf.append('\n');
            headerbuf.append(this.this$0.byteArrayToHex(checksumSalt));
            headerbuf.append('\n');
            headerbuf.append(BackupManagerService.PBKDF2_HASH_ROUNDS);
            headerbuf.append('\n');
            Cipher mkC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mkC.init(BackupManagerService.SCHEDULE_FILE_VERSION, userKey);
            byte[] IV = mkC.getIV();
            headerbuf.append(this.this$0.byteArrayToHex(IV));
            headerbuf.append('\n');
            IV = c.getIV();
            byte[] mk = masterKeySpec.getEncoded();
            byte[] checksum = this.this$0.makeKeyChecksum(BackupManagerService.PBKDF_CURRENT, masterKeySpec.getEncoded(), checksumSalt, BackupManagerService.PBKDF2_HASH_ROUNDS);
            int length = IV.length;
            int length2 = mk.length;
            ByteArrayOutputStream blob = new ByteArrayOutputStream(((r0 + r0) + checksum.length) + BackupManagerService.MSG_RUN_RESTORE);
            DataOutputStream dataOutputStream = new DataOutputStream(blob);
            dataOutputStream.writeByte(IV.length);
            dataOutputStream.write(IV);
            dataOutputStream.writeByte(mk.length);
            dataOutputStream.write(mk);
            dataOutputStream.writeByte(checksum.length);
            dataOutputStream.write(checksum);
            dataOutputStream.flush();
            byte[] encryptedMk = mkC.doFinal(blob.toByteArray());
            headerbuf.append(this.this$0.byteArrayToHex(encryptedMk));
            headerbuf.append('\n');
            return finalOutput;
        }

        private void finalizeBackup(OutputStream out) {
            try {
                out.write(new byte[DumpState.DUMP_PROVIDERS]);
            } catch (IOException e) {
                Slog.w(BackupManagerService.TAG, "Error attempting to finalize backup stream");
            }
        }

        public void run() {
            int i;
            PackageInfo pkg;
            Throwable e;
            Slog.i(BackupManagerService.TAG, "--- Performing full-dataset adb backup ---");
            TreeMap<String, PackageInfo> packagesToBackup = new TreeMap();
            FullBackupObbConnection fullBackupObbConnection = new FullBackupObbConnection(this.this$0);
            fullBackupObbConnection.establish();
            sendStartBackup();
            if (this.mAllApps) {
                List<PackageInfo> allPackages = this.this$0.mPackageManager.getInstalledPackages(64);
                for (i = BackupManagerService.OP_PENDING; i < allPackages.size(); i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                    pkg = (PackageInfo) allPackages.get(i);
                    if (this.mIncludeSystem || (pkg.applicationInfo.flags & BackupManagerService.SCHEDULE_FILE_VERSION) == 0) {
                        packagesToBackup.put(pkg.packageName, pkg);
                    }
                }
            }
            if (this.mDoWidgets) {
                List<String> pkgs = AppWidgetBackupBridge.getWidgetParticipants(BackupManagerService.OP_PENDING);
                if (pkgs != null) {
                    addPackagesToSet(packagesToBackup, pkgs);
                }
            }
            if (this.mPackages != null) {
                addPackagesToSet(packagesToBackup, this.mPackages);
            }
            Iterator<Entry<String, PackageInfo>> iter = packagesToBackup.entrySet().iterator();
            while (iter.hasNext()) {
                pkg = (PackageInfo) ((Entry) iter.next()).getValue();
                if (!BackupManagerService.appIsEligibleForBackup(pkg.applicationInfo) || BackupManagerService.appIsStopped(pkg.applicationInfo) || BackupManagerService.appIsKeyValueOnly(pkg)) {
                    iter.remove();
                }
            }
            ArrayList<PackageInfo> backupQueue = new ArrayList(packagesToBackup.values());
            OutputStream fileOutputStream = new FileOutputStream(this.mOutputFile.getFileDescriptor());
            OutputStream outputStream = null;
            try {
                boolean encrypting = (this.mEncryptPassword == null || this.mEncryptPassword.length() <= 0) ? BackupManagerService.MORE_DEBUG : BackupManagerService.DEBUG_BACKUP_TRACE;
                if (!this.this$0.deviceIsEncrypted() || encrypting) {
                    OutputStream finalOutput = fileOutputStream;
                    if (this.this$0.backupPasswordMatches(this.mCurrentPassword)) {
                        OutputStream finalOutput2;
                        StringBuilder stringBuilder = new StringBuilder(DumpState.DUMP_PROVIDERS);
                        stringBuilder.append(BackupManagerService.BACKUP_FILE_HEADER_MAGIC);
                        stringBuilder.append(BackupManagerService.MSG_RUN_CLEAR);
                        stringBuilder.append(this.mCompress ? "\n1\n" : "\n0\n");
                        if (encrypting) {
                            try {
                                finalOutput2 = emitAesBackupHeader(stringBuilder, fileOutputStream);
                            } catch (Exception e2) {
                                e = e2;
                                Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                                try {
                                    this.mOutputFile.close();
                                } catch (IOException e3) {
                                }
                                synchronized (this.this$0.mCurrentOpLock) {
                                    this.this$0.mCurrentOperations.clear();
                                }
                                synchronized (this.mLatch) {
                                    this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                    this.mLatch.notifyAll();
                                }
                                sendEndBackup();
                                fullBackupObbConnection.tearDown();
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                }
                                this.this$0.mWakelock.release();
                                return;
                            } catch (RemoteException e4) {
                                Slog.e(BackupManagerService.TAG, "App died during full backup");
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e5) {
                                        synchronized (this.this$0.mCurrentOpLock) {
                                        }
                                        this.this$0.mCurrentOperations.clear();
                                        synchronized (this.mLatch) {
                                        }
                                        this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                        this.mLatch.notifyAll();
                                        sendEndBackup();
                                        fullBackupObbConnection.tearDown();
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                        }
                                        this.this$0.mWakelock.release();
                                        return;
                                    }
                                }
                                this.mOutputFile.close();
                                synchronized (this.this$0.mCurrentOpLock) {
                                }
                                this.this$0.mCurrentOperations.clear();
                                synchronized (this.mLatch) {
                                }
                                this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatch.notifyAll();
                                sendEndBackup();
                                fullBackupObbConnection.tearDown();
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                }
                                this.this$0.mWakelock.release();
                            }
                        } else {
                            stringBuilder.append("none\n");
                            finalOutput2 = finalOutput;
                        }
                        try {
                            fileOutputStream.write(stringBuilder.toString().getBytes("UTF-8"));
                            if (this.mCompress) {
                                fileOutputStream = new DeflaterOutputStream(finalOutput2, new Deflater(BackupManagerService.MSG_FULL_CONFIRMATION_TIMEOUT), BackupManagerService.DEBUG_BACKUP_TRACE);
                            } else {
                                finalOutput = finalOutput2;
                            }
                            outputStream = finalOutput;
                            if (this.mIncludeShared) {
                                try {
                                    backupQueue.add(this.this$0.mPackageManager.getPackageInfo(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, BackupManagerService.OP_PENDING));
                                } catch (NameNotFoundException e6) {
                                    Slog.e(BackupManagerService.TAG, "Unable to find shared-storage backup handler");
                                }
                            }
                            int N = backupQueue.size();
                            i = BackupManagerService.OP_PENDING;
                            while (i < N) {
                                pkg = (PackageInfo) backupQueue.get(i);
                                boolean isSharedStorage = pkg.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                                this.mBackupEngine = new FullBackupEngine(this.this$0, outputStream, null, pkg, this.mIncludeApks, this);
                                sendOnBackupPackage(isSharedStorage ? "Shared storage" : pkg.packageName);
                                this.mCurrentTarget = pkg;
                                this.mBackupEngine.backupOnePackage();
                                if (!this.mIncludeObbs || fullBackupObbConnection.backupObbs(pkg, outputStream)) {
                                    i += BackupManagerService.SCHEDULE_FILE_VERSION;
                                } else {
                                    throw new RuntimeException("Failure writing OBB stack for " + pkg);
                                }
                            }
                            finalizeBackup(outputStream);
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e7) {
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                                this.this$0.mCurrentOperations.clear();
                            }
                            synchronized (this.mLatch) {
                                this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatch.notifyAll();
                            }
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            this.this$0.mWakelock.release();
                        } catch (Exception e8) {
                            e = e8;
                            finalOutput = finalOutput2;
                            Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                            this.mOutputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                                this.this$0.mCurrentOperations.clear();
                            }
                            synchronized (this.mLatch) {
                                this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatch.notifyAll();
                            }
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            this.this$0.mWakelock.release();
                            return;
                        } catch (RemoteException e42) {
                            Slog.e(BackupManagerService.TAG, "App died during full backup");
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e52) {
                                    synchronized (this.this$0.mCurrentOpLock) {
                                    }
                                    this.this$0.mCurrentOperations.clear();
                                    synchronized (this.mLatch) {
                                    }
                                    this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                    this.mLatch.notifyAll();
                                    sendEndBackup();
                                    fullBackupObbConnection.tearDown();
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                    }
                                    this.this$0.mWakelock.release();
                                    return;
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                            }
                            this.this$0.mCurrentOperations.clear();
                            synchronized (this.mLatch) {
                            }
                            this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatch.notifyAll();
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            this.this$0.mWakelock.release();
                        } catch (Throwable th) {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e9) {
                                    synchronized (this.this$0.mCurrentOpLock) {
                                    }
                                    this.this$0.mCurrentOperations.clear();
                                    synchronized (this.mLatch) {
                                    }
                                    this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                    this.mLatch.notifyAll();
                                    sendEndBackup();
                                    fullBackupObbConnection.tearDown();
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                    }
                                    this.this$0.mWakelock.release();
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                            }
                            this.this$0.mCurrentOperations.clear();
                            synchronized (this.mLatch) {
                            }
                            this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatch.notifyAll();
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            this.this$0.mWakelock.release();
                        }
                        return;
                    }
                    if (BackupManagerService.DEBUG) {
                        Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                    }
                    try {
                        this.mOutputFile.close();
                    } catch (IOException e10) {
                    }
                    synchronized (this.this$0.mCurrentOpLock) {
                        this.this$0.mCurrentOperations.clear();
                    }
                    synchronized (this.mLatch) {
                        this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                        this.mLatch.notifyAll();
                    }
                    sendEndBackup();
                    fullBackupObbConnection.tearDown();
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                    }
                    this.this$0.mWakelock.release();
                    return;
                }
                Slog.e(BackupManagerService.TAG, "Unencrypted backup of encrypted device; aborting");
                try {
                    this.mOutputFile.close();
                } catch (IOException e11) {
                }
                synchronized (this.this$0.mCurrentOpLock) {
                    this.this$0.mCurrentOperations.clear();
                }
                synchronized (this.mLatch) {
                    this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mLatch.notifyAll();
                }
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                }
                this.this$0.mWakelock.release();
            } catch (RemoteException e422) {
                Slog.e(BackupManagerService.TAG, "App died during full backup");
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e522) {
                        synchronized (this.this$0.mCurrentOpLock) {
                        }
                        this.this$0.mCurrentOperations.clear();
                        synchronized (this.mLatch) {
                        }
                        this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                        this.mLatch.notifyAll();
                        sendEndBackup();
                        fullBackupObbConnection.tearDown();
                        if (BackupManagerService.DEBUG) {
                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                        }
                        this.this$0.mWakelock.release();
                        return;
                    }
                }
                this.mOutputFile.close();
                synchronized (this.this$0.mCurrentOpLock) {
                }
                this.this$0.mCurrentOperations.clear();
                synchronized (this.mLatch) {
                }
                this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                this.mLatch.notifyAll();
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                }
                this.this$0.mWakelock.release();
            } catch (Throwable e12) {
                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e12);
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e13) {
                        synchronized (this.this$0.mCurrentOpLock) {
                        }
                        this.this$0.mCurrentOperations.clear();
                        synchronized (this.mLatch) {
                        }
                        this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                        this.mLatch.notifyAll();
                        sendEndBackup();
                        fullBackupObbConnection.tearDown();
                        if (BackupManagerService.DEBUG) {
                            Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                        }
                        this.this$0.mWakelock.release();
                        return;
                    }
                }
                this.mOutputFile.close();
                synchronized (this.this$0.mCurrentOpLock) {
                }
                this.this$0.mCurrentOperations.clear();
                synchronized (this.mLatch) {
                }
                this.mLatch.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                this.mLatch.notifyAll();
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                }
                this.this$0.mWakelock.release();
            }
        }

        public void execute() {
        }

        public void operationComplete(long result) {
        }

        public void handleTimeout() {
            PackageInfo target = this.mCurrentTarget;
            if (BackupManagerService.DEBUG) {
                Slog.w(BackupManagerService.TAG, "adb backup timeout of " + target);
            }
            if (target != null) {
                this.this$0.tearDownAgentAndKill(this.mCurrentTarget.applicationInfo);
            }
        }
    }

    class PerformAdbRestoreTask implements Runnable {
        private static final /* synthetic */ int[] -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$backup$BackupManagerService$RestorePolicy;
        IBackupAgent mAgent;
        String mAgentPackage;
        long mBytes;
        final HashSet<String> mClearedPackages;
        String mCurrentPassword;
        String mDecryptPassword;
        final RestoreDeleteObserver mDeleteObserver;
        ParcelFileDescriptor mInputFile;
        final RestoreInstallObserver mInstallObserver;
        AtomicBoolean mLatchObject;
        final HashMap<String, Signature[]> mManifestSignatures;
        FullBackupObbConnection mObbConnection;
        IFullBackupRestoreObserver mObserver;
        final HashMap<String, String> mPackageInstallers;
        final HashMap<String, RestorePolicy> mPackagePolicies;
        ParcelFileDescriptor[] mPipes;
        ApplicationInfo mTargetApp;
        byte[] mWidgetData;
        final /* synthetic */ BackupManagerService this$0;

        class RestoreDeleteObserver extends IPackageDeleteObserver.Stub {
            final AtomicBoolean mDone;
            int mResult;
            final /* synthetic */ PerformAdbRestoreTask this$1;

            RestoreDeleteObserver(PerformAdbRestoreTask this$1) {
                this.this$1 = this$1;
                this.mDone = new AtomicBoolean();
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(BackupManagerService.MORE_DEBUG);
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (true) {
                        if (this.mDone.get()) {
                        } else {
                            try {
                                this.mDone.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }

            public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mDone.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mDone.notifyAll();
                }
            }
        }

        class RestoreFileRunnable implements Runnable {
            IBackupAgent mAgent;
            FileMetadata mInfo;
            ParcelFileDescriptor mSocket;
            int mToken;
            final /* synthetic */ PerformAdbRestoreTask this$1;

            RestoreFileRunnable(PerformAdbRestoreTask this$1, IBackupAgent agent, FileMetadata info, ParcelFileDescriptor socket, int token) throws IOException {
                this.this$1 = this$1;
                this.mAgent = agent;
                this.mInfo = info;
                this.mToken = token;
                this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFile(this.mSocket, this.mInfo.size, this.mInfo.type, this.mInfo.domain, this.mInfo.path, this.mInfo.mode, this.mInfo.mtime, this.mToken, this.this$1.this$0.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreFinishedRunnable implements Runnable {
            final IBackupAgent mAgent;
            final int mToken;
            final /* synthetic */ PerformAdbRestoreTask this$1;

            RestoreFinishedRunnable(PerformAdbRestoreTask this$1, IBackupAgent agent, int token) {
                this.this$1 = this$1;
                this.mAgent = agent;
                this.mToken = token;
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFinished(this.mToken, this.this$1.this$0.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreInstallObserver extends PackageInstallObserver {
            final AtomicBoolean mDone;
            String mPackageName;
            int mResult;
            final /* synthetic */ PerformAdbRestoreTask this$1;

            RestoreInstallObserver(PerformAdbRestoreTask this$1) {
                this.this$1 = this$1;
                this.mDone = new AtomicBoolean();
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(BackupManagerService.MORE_DEBUG);
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (true) {
                        if (this.mDone.get()) {
                        } else {
                            try {
                                this.mDone.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }

            int getResult() {
                return this.mResult;
            }

            public void onPackageInstalled(String packageName, int returnCode, String msg, Bundle extras) {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mPackageName = packageName;
                    this.mDone.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mDone.notifyAll();
                }
            }
        }

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues;
            }
            int[] iArr = new int[RestorePolicy.values().length];
            try {
                iArr[RestorePolicy.ACCEPT.ordinal()] = BackupManagerService.SCHEDULE_FILE_VERSION;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RestorePolicy.ACCEPT_IF_APK.ordinal()] = BackupManagerService.MSG_RUN_ADB_BACKUP;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[RestorePolicy.IGNORE.ordinal()] = BackupManagerService.MSG_RUN_RESTORE;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = iArr;
            return iArr;
        }

        PerformAdbRestoreTask(BackupManagerService this$0, ParcelFileDescriptor fd, String curPassword, String decryptPassword, IFullBackupRestoreObserver observer, AtomicBoolean latch) {
            this.this$0 = this$0;
            this.mObbConnection = null;
            this.mPipes = null;
            this.mWidgetData = null;
            this.mPackagePolicies = new HashMap();
            this.mPackageInstallers = new HashMap();
            this.mManifestSignatures = new HashMap();
            this.mClearedPackages = new HashSet();
            this.mInstallObserver = new RestoreInstallObserver(this);
            this.mDeleteObserver = new RestoreDeleteObserver(this);
            this.mInputFile = fd;
            this.mCurrentPassword = curPassword;
            this.mDecryptPassword = decryptPassword;
            this.mObserver = observer;
            this.mLatchObject = latch;
            this.mAgent = null;
            this.mAgentPackage = null;
            this.mTargetApp = null;
            this.mObbConnection = new FullBackupObbConnection(this$0);
            this.mClearedPackages.add("android");
            this.mClearedPackages.add(BackupManagerService.SETTINGS_PACKAGE);
        }

        public void run() {
            InputStream rawInStream;
            Throwable th;
            Slog.i(BackupManagerService.TAG, "--- Performing full-dataset restore ---");
            this.mObbConnection.establish();
            sendStartRestore();
            if (Environment.getExternalStorageState().equals("mounted")) {
                this.mPackagePolicies.put(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, RestorePolicy.ACCEPT);
            }
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;
            try {
                if (this.this$0.backupPasswordMatches(this.mCurrentPassword)) {
                    DataInputStream dataInputStream2;
                    boolean compressed;
                    InputStream preCompressStream;
                    boolean okay;
                    this.mBytes = 0;
                    byte[] buffer = new byte[DumpState.DUMP_VERSION];
                    InputStream fileInputStream2 = new FileInputStream(this.mInputFile.getFileDescriptor());
                    try {
                        dataInputStream2 = new DataInputStream(fileInputStream2);
                        compressed = BackupManagerService.MORE_DEBUG;
                        preCompressStream = fileInputStream2;
                        okay = BackupManagerService.MORE_DEBUG;
                    } catch (IOException e) {
                        rawInStream = fileInputStream2;
                        try {
                            Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e2) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e2);
                                    synchronized (this.this$0.mCurrentOpLock) {
                                        this.this$0.mCurrentOperations.clear();
                                    }
                                    synchronized (this.mLatchObject) {
                                        this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                        this.mLatchObject.notifyAll();
                                    }
                                    this.mObbConnection.tearDown();
                                    sendEndRestore();
                                    Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                                    this.this$0.mWakelock.release();
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                                this.this$0.mCurrentOperations.clear();
                            }
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            this.this$0.mWakelock.release();
                        } catch (Throwable th2) {
                            th = th2;
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e22) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e22);
                                    synchronized (this.this$0.mCurrentOpLock) {
                                        this.this$0.mCurrentOperations.clear();
                                    }
                                    synchronized (this.mLatchObject) {
                                        this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                        this.mLatchObject.notifyAll();
                                    }
                                    this.mObbConnection.tearDown();
                                    sendEndRestore();
                                    Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                                    this.this$0.mWakelock.release();
                                    throw th;
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                                this.this$0.mCurrentOperations.clear();
                            }
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            this.this$0.mWakelock.release();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rawInStream = fileInputStream2;
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                        if (dataInputStream != null) {
                            dataInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        this.mInputFile.close();
                        synchronized (this.this$0.mCurrentOpLock) {
                            this.this$0.mCurrentOperations.clear();
                        }
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatchObject.notifyAll();
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        this.this$0.mWakelock.release();
                        throw th;
                    }
                    try {
                        byte[] streamHeader = new byte[BackupManagerService.BACKUP_FILE_HEADER_MAGIC.length()];
                        dataInputStream2.readFully(streamHeader);
                        if (Arrays.equals(BackupManagerService.BACKUP_FILE_HEADER_MAGIC.getBytes("UTF-8"), streamHeader)) {
                            String s = readHeaderLine(fileInputStream2);
                            int archiveVersion = Integer.parseInt(s);
                            if (archiveVersion <= BackupManagerService.MSG_RUN_CLEAR) {
                                boolean pbkdf2Fallback = archiveVersion == BackupManagerService.SCHEDULE_FILE_VERSION ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                                compressed = Integer.parseInt(readHeaderLine(fileInputStream2)) != 0 ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                                s = readHeaderLine(fileInputStream2);
                                if (s.equals("none")) {
                                    okay = BackupManagerService.DEBUG_BACKUP_TRACE;
                                } else {
                                    if (this.mDecryptPassword != null) {
                                        if (this.mDecryptPassword.length() > 0) {
                                            preCompressStream = decodeAesHeaderAndInitialize(s, pbkdf2Fallback, fileInputStream2);
                                            if (preCompressStream != null) {
                                                okay = BackupManagerService.DEBUG_BACKUP_TRACE;
                                            }
                                        }
                                    }
                                    Slog.w(BackupManagerService.TAG, "Archive is encrypted but no password given");
                                }
                            } else {
                                Slog.w(BackupManagerService.TAG, "Wrong header version: " + s);
                            }
                        } else {
                            Slog.w(BackupManagerService.TAG, "Didn't read the right header magic");
                        }
                        if (okay) {
                            InputStream in = compressed ? new InflaterInputStream(preCompressStream) : preCompressStream;
                            do {
                            } while (restoreOneFile(in, buffer));
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                            if (dataInputStream2 != null) {
                                try {
                                    dataInputStream2.close();
                                } catch (IOException e222) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e222);
                                }
                            }
                            if (fileInputStream2 != null) {
                                fileInputStream2.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.this$0.mCurrentOpLock) {
                                this.this$0.mCurrentOperations.clear();
                            }
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            this.this$0.mWakelock.release();
                            dataInputStream = dataInputStream2;
                            rawInStream = fileInputStream2;
                        }
                        Slog.w(BackupManagerService.TAG, "Invalid restore data; aborting.");
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                        if (dataInputStream2 != null) {
                            try {
                                dataInputStream2.close();
                            } catch (IOException e2222) {
                                Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e2222);
                            }
                        }
                        if (fileInputStream2 != null) {
                            fileInputStream2.close();
                        }
                        this.mInputFile.close();
                        synchronized (this.this$0.mCurrentOpLock) {
                            this.this$0.mCurrentOperations.clear();
                        }
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatchObject.notifyAll();
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        this.this$0.mWakelock.release();
                        return;
                    } catch (IOException e3) {
                        dataInputStream = dataInputStream2;
                        fileInputStream = fileInputStream2;
                        Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                        if (dataInputStream != null) {
                            dataInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        this.mInputFile.close();
                        synchronized (this.this$0.mCurrentOpLock) {
                            this.this$0.mCurrentOperations.clear();
                        }
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatchObject.notifyAll();
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        this.this$0.mWakelock.release();
                    } catch (Throwable th4) {
                        th = th4;
                        dataInputStream = dataInputStream2;
                        fileInputStream = fileInputStream2;
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                        if (dataInputStream != null) {
                            dataInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        this.mInputFile.close();
                        synchronized (this.this$0.mCurrentOpLock) {
                            this.this$0.mCurrentOperations.clear();
                        }
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mLatchObject.notifyAll();
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        this.this$0.mWakelock.release();
                        throw th;
                    }
                }
                if (BackupManagerService.DEBUG) {
                    Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                }
                tearDownPipes();
                tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                try {
                    this.mInputFile.close();
                } catch (IOException e22222) {
                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e22222);
                }
                synchronized (this.this$0.mCurrentOpLock) {
                    this.this$0.mCurrentOperations.clear();
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                sendEndRestore();
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.this$0.mWakelock.release();
            } catch (IOException e4) {
                Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                tearDownPipes();
                tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                this.mInputFile.close();
                synchronized (this.this$0.mCurrentOpLock) {
                    this.this$0.mCurrentOperations.clear();
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(BackupManagerService.DEBUG_BACKUP_TRACE);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                sendEndRestore();
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                this.this$0.mWakelock.release();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        String readHeaderLine(InputStream in) throws IOException {
            StringBuilder buffer = new StringBuilder(80);
            while (true) {
                int c = in.read();
                if (c >= 0 && c != BackupManagerService.MSG_RUN_ADB_RESTORE) {
                    buffer.append((char) c);
                }
            }
            return buffer.toString();
        }

        InputStream attemptMasterKeyDecryption(String algorithm, byte[] userSalt, byte[] ckSalt, int rounds, String userIvHex, String masterKeyBlobHex, InputStream rawInStream, boolean doLog) {
            try {
                Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKey userKey = this.this$0.buildPasswordKey(algorithm, this.mDecryptPassword, userSalt, rounds);
                IvParameterSpec ivSpec = new IvParameterSpec(this.this$0.hexToByteArray(userIvHex));
                c.init(BackupManagerService.MSG_RUN_ADB_BACKUP, new SecretKeySpec(userKey.getEncoded(), "AES"), ivSpec);
                byte[] mkBlob = c.doFinal(this.this$0.hexToByteArray(masterKeyBlobHex));
                int len = mkBlob[BackupManagerService.OP_PENDING];
                byte[] IV = Arrays.copyOfRange(mkBlob, BackupManagerService.SCHEDULE_FILE_VERSION, len + BackupManagerService.SCHEDULE_FILE_VERSION);
                int offset = len + BackupManagerService.SCHEDULE_FILE_VERSION;
                int offset2 = offset + BackupManagerService.SCHEDULE_FILE_VERSION;
                len = mkBlob[offset];
                byte[] mk = Arrays.copyOfRange(mkBlob, offset2, offset2 + len);
                offset = offset2 + len;
                offset2 = offset + BackupManagerService.SCHEDULE_FILE_VERSION;
                if (Arrays.equals(this.this$0.makeKeyChecksum(algorithm, mk, ckSalt, rounds), Arrays.copyOfRange(mkBlob, offset2, offset2 + mkBlob[offset]))) {
                    ivSpec = new IvParameterSpec(IV);
                    c.init(BackupManagerService.MSG_RUN_ADB_BACKUP, new SecretKeySpec(mk, "AES"), ivSpec);
                    return new CipherInputStream(rawInStream, c);
                } else if (!doLog) {
                    return null;
                } else {
                    Slog.w(BackupManagerService.TAG, "Incorrect password");
                    return null;
                }
            } catch (InvalidAlgorithmParameterException e) {
                if (!doLog) {
                    return null;
                }
                Slog.e(BackupManagerService.TAG, "Needed parameter spec unavailable!", e);
                return null;
            } catch (BadPaddingException e2) {
                if (!doLog) {
                    return null;
                }
                Slog.w(BackupManagerService.TAG, "Incorrect password");
                return null;
            } catch (IllegalBlockSizeException e3) {
                if (!doLog) {
                    return null;
                }
                Slog.w(BackupManagerService.TAG, "Invalid block size in master key");
                return null;
            } catch (NoSuchAlgorithmException e4) {
                if (!doLog) {
                    return null;
                }
                Slog.e(BackupManagerService.TAG, "Needed decryption algorithm unavailable!");
                return null;
            } catch (NoSuchPaddingException e5) {
                if (!doLog) {
                    return null;
                }
                Slog.e(BackupManagerService.TAG, "Needed padding mechanism unavailable!");
                return null;
            } catch (InvalidKeyException e6) {
                if (!doLog) {
                    return null;
                }
                Slog.w(BackupManagerService.TAG, "Illegal password; aborting");
                return null;
            }
        }

        InputStream decodeAesHeaderAndInitialize(String encryptionName, boolean pbkdf2Fallback, InputStream rawInStream) {
            try {
                if (encryptionName.equals(BackupManagerService.ENCRYPTION_ALGORITHM_NAME)) {
                    byte[] userSalt = this.this$0.hexToByteArray(readHeaderLine(rawInStream));
                    byte[] ckSalt = this.this$0.hexToByteArray(readHeaderLine(rawInStream));
                    int rounds = Integer.parseInt(readHeaderLine(rawInStream));
                    String userIvHex = readHeaderLine(rawInStream);
                    String masterKeyBlobHex = readHeaderLine(rawInStream);
                    InputStream result = attemptMasterKeyDecryption(BackupManagerService.PBKDF_CURRENT, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, BackupManagerService.MORE_DEBUG);
                    if (result == null && pbkdf2Fallback) {
                        return attemptMasterKeyDecryption(BackupManagerService.PBKDF_FALLBACK, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, BackupManagerService.DEBUG_BACKUP_TRACE);
                    }
                    return result;
                }
                Slog.w(BackupManagerService.TAG, "Unsupported encryption method: " + encryptionName);
                return null;
            } catch (NumberFormatException e) {
                Slog.w(BackupManagerService.TAG, "Can't parse restore data header");
                return null;
            } catch (IOException e2) {
                Slog.w(BackupManagerService.TAG, "Can't read input header");
                return null;
            }
        }

        boolean restoreOneFile(InputStream instream, byte[] buffer) {
            FileMetadata info;
            boolean z;
            try {
                info = readTarHeaders(instream);
                if (info != null) {
                    String pkg = info.packageName;
                    if (!pkg.equals(this.mAgentPackage)) {
                        if (!this.mPackagePolicies.containsKey(pkg)) {
                            this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                        }
                        if (this.mAgent != null) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Saw new package; finalizing old one");
                            }
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, BackupManagerService.DEBUG_BACKUP_TRACE);
                            this.mTargetApp = null;
                            this.mAgentPackage = null;
                        }
                    }
                    if (info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME)) {
                        this.mPackagePolicies.put(pkg, readAppManifest(info, instream));
                        this.mPackageInstallers.put(pkg, info.installerPackageName);
                        skipTarPadding(info.size, instream);
                        sendOnRestorePackage(pkg);
                    } else if (info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME)) {
                        readMetadata(info, instream);
                        skipTarPadding(info.size, instream);
                    } else {
                        boolean okay = BackupManagerService.DEBUG_BACKUP_TRACE;
                        switch (-getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues()[((RestorePolicy) this.mPackagePolicies.get(pkg)).ordinal()]) {
                            case BackupManagerService.SCHEDULE_FILE_VERSION /*1*/:
                                if (info.domain.equals("a")) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "apk present but ACCEPT");
                                    }
                                    okay = BackupManagerService.MORE_DEBUG;
                                    break;
                                }
                                break;
                            case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                                if (!info.domain.equals("a")) {
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    okay = BackupManagerService.MORE_DEBUG;
                                    break;
                                }
                                Object obj;
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "APK file; installing");
                                }
                                okay = installApk(info, (String) this.mPackageInstallers.get(pkg), instream);
                                HashMap hashMap = this.mPackagePolicies;
                                if (okay) {
                                    obj = RestorePolicy.ACCEPT;
                                } else {
                                    obj = RestorePolicy.IGNORE;
                                }
                                hashMap.put(pkg, obj);
                                skipTarPadding(info.size, instream);
                                return BackupManagerService.DEBUG_BACKUP_TRACE;
                            case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                                okay = BackupManagerService.MORE_DEBUG;
                                break;
                            default:
                                Slog.e(BackupManagerService.TAG, "Invalid policy from manifest");
                                okay = BackupManagerService.MORE_DEBUG;
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                break;
                        }
                        if (info.path.contains("..") || info.path.contains("//")) {
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                        if (BackupManagerService.DEBUG && okay && this.mAgent != null) {
                            Slog.i(BackupManagerService.TAG, "Reusing existing agent instance");
                        }
                        if (okay && this.mAgent == null) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Need to launch agent for " + pkg);
                            }
                            try {
                                this.mTargetApp = this.this$0.mPackageManager.getApplicationInfo(pkg, BackupManagerService.OP_PENDING);
                                if (!this.mClearedPackages.contains(pkg)) {
                                    if (this.mTargetApp.backupAgentName == null) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d(BackupManagerService.TAG, "Clearing app data preparatory to full restore");
                                        }
                                        this.this$0.clearApplicationDataSynchronous(pkg);
                                    } else if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "backup agent (" + this.mTargetApp.backupAgentName + ") => no clear");
                                    }
                                    this.mClearedPackages.add(pkg);
                                } else if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "We've initialized this app already; no clear required");
                                }
                                setUpPipes();
                                this.mAgent = this.this$0.bindToAgentSynchronous(this.mTargetApp, BackupManagerService.MSG_RUN_RESTORE);
                                this.mAgentPackage = pkg;
                            } catch (IOException e) {
                            } catch (NameNotFoundException e2) {
                            }
                            if (this.mAgent == null) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Unable to create agent for " + pkg);
                                }
                                okay = BackupManagerService.MORE_DEBUG;
                                tearDownPipes();
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                            }
                        }
                        if (okay && !pkg.equals(this.mAgentPackage)) {
                            Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg + " but agent is for " + this.mAgentPackage);
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                        if (okay) {
                            boolean agentSuccess = BackupManagerService.DEBUG_BACKUP_TRACE;
                            long toCopy = info.size;
                            int token = this.this$0.generateToken();
                            try {
                                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, null);
                                if (info.domain.equals("obb")) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Restoring OBB file for " + pkg + " : " + info.path);
                                    }
                                    this.mObbConnection.restoreObbFile(pkg, this.mPipes[BackupManagerService.OP_PENDING], info.size, info.type, info.path, info.mode, info.mtime, token, this.this$0.mBackupManagerBinder);
                                } else {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Invoking agent to restore file " + info.path);
                                    }
                                    if (this.mTargetApp.processName.equals("system")) {
                                        Slog.d(BackupManagerService.TAG, "system process agent - spinning a thread");
                                        new Thread(new RestoreFileRunnable(this, this.mAgent, info, this.mPipes[BackupManagerService.OP_PENDING], token), "restore-sys-runner").start();
                                    } else {
                                        this.mAgent.doRestoreFile(this.mPipes[BackupManagerService.OP_PENDING], info.size, info.type, info.domain, info.path, info.mode, info.mtime, token, this.this$0.mBackupManagerBinder);
                                    }
                                }
                            } catch (IOException e3) {
                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                agentSuccess = BackupManagerService.MORE_DEBUG;
                                okay = BackupManagerService.MORE_DEBUG;
                            } catch (RemoteException e4) {
                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                agentSuccess = BackupManagerService.MORE_DEBUG;
                                okay = BackupManagerService.MORE_DEBUG;
                            }
                            if (okay) {
                                boolean pipeOkay = BackupManagerService.DEBUG_BACKUP_TRACE;
                                FileOutputStream fileOutputStream = new FileOutputStream(this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION].getFileDescriptor());
                                while (toCopy > 0) {
                                    int nRead = instream.read(buffer, BackupManagerService.OP_PENDING, toCopy > ((long) buffer.length) ? buffer.length : (int) toCopy);
                                    if (nRead >= 0) {
                                        this.mBytes += (long) nRead;
                                    }
                                    if (nRead <= 0) {
                                        skipTarPadding(info.size, instream);
                                        agentSuccess = this.this$0.waitUntilOperationComplete(token);
                                    } else {
                                        toCopy -= (long) nRead;
                                        if (pipeOkay) {
                                            try {
                                                fileOutputStream.write(buffer, BackupManagerService.OP_PENDING, nRead);
                                            } catch (Throwable e5) {
                                                Slog.e(BackupManagerService.TAG, "Failed to write to restore pipe", e5);
                                                pipeOkay = BackupManagerService.MORE_DEBUG;
                                            }
                                        }
                                    }
                                }
                                skipTarPadding(info.size, instream);
                                agentSuccess = this.this$0.waitUntilOperationComplete(token);
                            }
                            if (!agentSuccess) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Agent failure restoring " + pkg + "; now ignoring");
                                }
                                this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
                                tearDownPipes();
                                tearDownAgent(this.mTargetApp, BackupManagerService.MORE_DEBUG);
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                            }
                        }
                        if (!okay) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "[discarding file content]");
                            }
                            long bytesToConsume = (info.size + 511) & -512;
                            while (bytesToConsume > 0) {
                                int toRead;
                                if (bytesToConsume > ((long) buffer.length)) {
                                    toRead = buffer.length;
                                } else {
                                    toRead = (int) bytesToConsume;
                                }
                                long nRead2 = (long) instream.read(buffer, BackupManagerService.OP_PENDING, toRead);
                                if (nRead2 >= 0) {
                                    this.mBytes += nRead2;
                                }
                                if (nRead2 > 0) {
                                    bytesToConsume -= nRead2;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e52) {
                if (BackupManagerService.DEBUG) {
                    Slog.w(BackupManagerService.TAG, "io exception on restore socket read", e52);
                }
                info = null;
            }
            if (info != null) {
                z = BackupManagerService.DEBUG_BACKUP_TRACE;
            } else {
                z = BackupManagerService.MORE_DEBUG;
            }
            return z;
        }

        void setUpPipes() throws IOException {
            this.mPipes = ParcelFileDescriptor.createPipe();
        }

        void tearDownPipes() {
            if (this.mPipes != null) {
                try {
                    this.mPipes[BackupManagerService.OP_PENDING].close();
                    this.mPipes[BackupManagerService.OP_PENDING] = null;
                    this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                    this.mPipes[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                } catch (IOException e) {
                    Slog.w(BackupManagerService.TAG, "Couldn't close agent pipes", e);
                }
                this.mPipes = null;
            }
        }

        void tearDownAgent(ApplicationInfo app, boolean doRestoreFinished) {
            if (this.mAgent != null) {
                if (doRestoreFinished) {
                    try {
                        int token = this.this$0.generateToken();
                        AdbRestoreFinishedLatch latch = new AdbRestoreFinishedLatch();
                        this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, latch);
                        if (this.mTargetApp.processName.equals("system")) {
                            new Thread(new RestoreFinishedRunnable(this, this.mAgent, token), "restore-sys-finished-runner").start();
                        } else {
                            this.mAgent.doRestoreFinished(token, this.this$0.mBackupManagerBinder);
                        }
                        latch.await();
                    } catch (RemoteException e) {
                        Slog.d(BackupManagerService.TAG, "Lost app trying to shut down");
                    }
                }
                this.this$0.mActivityManager.unbindBackupAgent(app);
                if (app.uid < BackupManagerService.PBKDF2_HASH_ROUNDS || app.packageName.equals("com.android.backupconfirm")) {
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Not killing after full restore");
                    }
                    this.mAgent = null;
                }
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Killing host process");
                }
                this.this$0.mActivityManager.killApplicationProcess(app.processName, app.uid);
                this.mAgent = null;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        boolean installApk(FileMetadata info, String installerPackage, InputStream instream) {
            boolean okay = BackupManagerService.DEBUG_BACKUP_TRACE;
            if (BackupManagerService.DEBUG) {
                Slog.d(BackupManagerService.TAG, "Installing from backup: " + info.packageName);
            }
            File apkFile = new File(this.this$0.mDataDir, info.packageName);
            try {
                FileOutputStream apkStream = new FileOutputStream(apkFile);
                byte[] buffer = new byte[DumpState.DUMP_VERSION];
                long size = info.size;
                while (size > 0) {
                    long toRead;
                    if (((long) buffer.length) < size) {
                        toRead = (long) buffer.length;
                    } else {
                        toRead = size;
                    }
                    int didRead = instream.read(buffer, BackupManagerService.OP_PENDING, (int) toRead);
                    if (didRead >= 0) {
                        this.mBytes += (long) didRead;
                    }
                    apkStream.write(buffer, BackupManagerService.OP_PENDING, didRead);
                    size -= (long) didRead;
                }
                apkStream.close();
                apkFile.setReadable(BackupManagerService.DEBUG_BACKUP_TRACE, BackupManagerService.MORE_DEBUG);
                Uri packageUri = Uri.fromFile(apkFile);
                this.mInstallObserver.reset();
                this.this$0.mPackageManager.installPackage(packageUri, this.mInstallObserver, 34, installerPackage);
                this.mInstallObserver.waitForCompletion();
                if (this.mInstallObserver.getResult() != BackupManagerService.SCHEDULE_FILE_VERSION) {
                    if (this.mPackagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                        okay = BackupManagerService.MORE_DEBUG;
                    }
                } else {
                    BackupManagerService backupManagerService;
                    boolean uninstall = BackupManagerService.MORE_DEBUG;
                    if (this.mInstallObserver.mPackageName.equals(info.packageName)) {
                        try {
                            backupManagerService = this.this$0;
                            PackageInfo pkg = r0.mPackageManager.getPackageInfo(info.packageName, 64);
                            if ((pkg.applicationInfo.flags & DumpState.DUMP_VERSION) == 0) {
                                Slog.w(BackupManagerService.TAG, "Restore stream contains apk of package " + info.packageName + " but it disallows backup/restore");
                                okay = BackupManagerService.MORE_DEBUG;
                            } else {
                                if (BackupManagerService.signaturesMatch((Signature[]) this.mManifestSignatures.get(info.packageName), pkg)) {
                                    int i = pkg.applicationInfo.uid;
                                    if (r0 < BackupManagerService.PBKDF2_HASH_ROUNDS) {
                                        if (pkg.applicationInfo.backupAgentName == null) {
                                            Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " has restricted uid and no agent");
                                            okay = BackupManagerService.MORE_DEBUG;
                                        }
                                    }
                                } else {
                                    Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " signatures do not match restore manifest");
                                    okay = BackupManagerService.MORE_DEBUG;
                                    uninstall = BackupManagerService.DEBUG_BACKUP_TRACE;
                                }
                            }
                        } catch (NameNotFoundException e) {
                            Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                            okay = BackupManagerService.MORE_DEBUG;
                        }
                    } else {
                        String str = info.packageName;
                        Slog.w(BackupManagerService.TAG, "Restore stream claimed to include apk for " + r0 + " but apk was really " + this.mInstallObserver.mPackageName);
                        okay = BackupManagerService.MORE_DEBUG;
                        uninstall = BackupManagerService.DEBUG_BACKUP_TRACE;
                    }
                    if (uninstall) {
                        this.mDeleteObserver.reset();
                        backupManagerService = this.this$0;
                        r0.mPackageManager.deletePackage(this.mInstallObserver.mPackageName, this.mDeleteObserver, BackupManagerService.OP_PENDING);
                        this.mDeleteObserver.waitForCompletion();
                    }
                }
                apkFile.delete();
                return okay;
            } catch (IOException e2) {
                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                return BackupManagerService.MORE_DEBUG;
            } catch (Throwable th) {
                apkFile.delete();
            }
        }

        void skipTarPadding(long size, InputStream instream) throws IOException {
            long partial = (size + 512) % 512;
            if (partial > 0) {
                int needed = 512 - ((int) partial);
                if (readExactly(instream, new byte[needed], BackupManagerService.OP_PENDING, needed) == needed) {
                    this.mBytes += (long) needed;
                    return;
                }
                throw new IOException("Unexpected EOF in padding");
            }
        }

        void readMetadata(FileMetadata info, InputStream instream) throws IOException {
            if (info.size > 65536) {
                throw new IOException("Metadata too big; corrupt? size=" + info.size);
            }
            byte[] buffer = new byte[((int) info.size)];
            if (((long) readExactly(instream, buffer, BackupManagerService.OP_PENDING, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                String[] str = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
                int offset = extractLine(buffer, BackupManagerService.OP_PENDING, str);
                int version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                if (version == BackupManagerService.SCHEDULE_FILE_VERSION) {
                    offset = extractLine(buffer, offset, str);
                    String pkg = str[BackupManagerService.OP_PENDING];
                    if (info.packageName.equals(pkg)) {
                        ByteArrayInputStream bin = new ByteArrayInputStream(buffer, offset, buffer.length - offset);
                        DataInputStream in = new DataInputStream(bin);
                        while (bin.available() > 0) {
                            int token = in.readInt();
                            int size = in.readInt();
                            if (size <= DumpState.DUMP_INSTALLS) {
                                switch (token) {
                                    case BackupManagerService.BACKUP_WIDGET_METADATA_TOKEN /*33549569*/:
                                        this.mWidgetData = new byte[size];
                                        in.read(this.mWidgetData);
                                        break;
                                    default:
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i(BackupManagerService.TAG, "Ignoring metadata blob " + Integer.toHexString(token) + " for " + info.packageName);
                                        }
                                        in.skipBytes(size);
                                        break;
                                }
                            }
                            throw new IOException("Datum " + Integer.toHexString(token) + " too big; corrupt? size=" + info.size);
                        }
                        return;
                    }
                    Slog.w(BackupManagerService.TAG, "Metadata mismatch: package " + info.packageName + " but widget data for " + pkg);
                    return;
                }
                Slog.w(BackupManagerService.TAG, "Unsupported metadata version " + version);
                return;
            }
            throw new IOException("Unexpected EOF in widget data");
        }

        RestorePolicy readAppManifest(FileMetadata info, InputStream instream) throws IOException {
            if (info.size > 65536) {
                throw new IOException("Restore manifest too big; corrupt? size=" + info.size);
            }
            byte[] buffer = new byte[((int) info.size)];
            if (((long) readExactly(instream, buffer, BackupManagerService.OP_PENDING, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                RestorePolicy policy = RestorePolicy.IGNORE;
                String[] str = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
                try {
                    int offset = extractLine(buffer, BackupManagerService.OP_PENDING, str);
                    int version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                    if (version == BackupManagerService.SCHEDULE_FILE_VERSION) {
                        offset = extractLine(buffer, offset, str);
                        String manifestPackage = str[BackupManagerService.OP_PENDING];
                        if (manifestPackage.equals(info.packageName)) {
                            offset = extractLine(buffer, offset, str);
                            version = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            offset = extractLine(buffer, offset, str);
                            Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            offset = extractLine(buffer, offset, str);
                            info.installerPackageName = str[BackupManagerService.OP_PENDING].length() > 0 ? str[BackupManagerService.OP_PENDING] : null;
                            offset = extractLine(buffer, offset, str);
                            boolean hasApk = str[BackupManagerService.OP_PENDING].equals("1");
                            offset = extractLine(buffer, offset, str);
                            int numSigs = Integer.parseInt(str[BackupManagerService.OP_PENDING]);
                            if (numSigs > 0) {
                                Object sigs = new Signature[numSigs];
                                for (int i = BackupManagerService.OP_PENDING; i < numSigs; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                                    offset = extractLine(buffer, offset, str);
                                    sigs[i] = new Signature(str[BackupManagerService.OP_PENDING]);
                                }
                                this.mManifestSignatures.put(info.packageName, sigs);
                                try {
                                    PackageInfo pkgInfo = this.this$0.mPackageManager.getPackageInfo(info.packageName, 64);
                                    if ((DumpState.DUMP_VERSION & pkgInfo.applicationInfo.flags) != 0) {
                                        int i2 = pkgInfo.applicationInfo.uid;
                                        if (r0 < BackupManagerService.PBKDF2_HASH_ROUNDS) {
                                            if (pkgInfo.applicationInfo.backupAgentName == null) {
                                                Slog.w(BackupManagerService.TAG, "Package " + info.packageName + " is system level with no agent");
                                                if (policy == RestorePolicy.ACCEPT_IF_APK && !hasApk) {
                                                    Slog.i(BackupManagerService.TAG, "Cannot restore package " + info.packageName + " without the matching .apk");
                                                }
                                            }
                                        }
                                        if (BackupManagerService.signaturesMatch(sigs, pkgInfo)) {
                                            i2 = pkgInfo.versionCode;
                                            if (r0 >= version) {
                                                Slog.i(BackupManagerService.TAG, "Sig + version match; taking data");
                                                policy = RestorePolicy.ACCEPT;
                                            } else {
                                                Slog.d(BackupManagerService.TAG, "Data version " + version + " is newer than installed version " + pkgInfo.versionCode + " - requiring apk");
                                                policy = RestorePolicy.ACCEPT_IF_APK;
                                            }
                                        } else {
                                            Slog.w(BackupManagerService.TAG, "Restore manifest signatures do not match installed application for " + info.packageName);
                                        }
                                        Slog.i(BackupManagerService.TAG, "Cannot restore package " + info.packageName + " without the matching .apk");
                                    } else {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i(BackupManagerService.TAG, "Restore manifest from " + info.packageName + " but allowBackup=false");
                                        }
                                        Slog.i(BackupManagerService.TAG, "Cannot restore package " + info.packageName + " without the matching .apk");
                                    }
                                } catch (NameNotFoundException e) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.i(BackupManagerService.TAG, "Package " + info.packageName + " not installed; requiring apk in dataset");
                                    }
                                    policy = RestorePolicy.ACCEPT_IF_APK;
                                }
                            } else {
                                Slog.i(BackupManagerService.TAG, "Missing signature on backed-up package " + info.packageName);
                            }
                        } else {
                            Slog.i(BackupManagerService.TAG, "Expected package " + info.packageName + " but restore manifest claims " + manifestPackage);
                        }
                    } else {
                        Slog.i(BackupManagerService.TAG, "Unknown restore manifest version " + version + " for package " + info.packageName);
                    }
                } catch (NumberFormatException e2) {
                    Slog.w(BackupManagerService.TAG, "Corrupt restore manifest for package " + info.packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(BackupManagerService.TAG, e3.getMessage());
                }
                return policy;
            }
            throw new IOException("Unexpected EOF in manifest");
        }

        int extractLine(byte[] buffer, int offset, String[] outStr) throws IOException {
            int end = buffer.length;
            if (offset >= end) {
                throw new IOException("Incomplete data");
            }
            int pos = offset;
            while (pos < end && buffer[pos] != BackupManagerService.MSG_RUN_ADB_RESTORE) {
                pos += BackupManagerService.SCHEDULE_FILE_VERSION;
            }
            outStr[BackupManagerService.OP_PENDING] = new String(buffer, offset, pos - offset);
            return pos + BackupManagerService.SCHEDULE_FILE_VERSION;
        }

        void dumpFileMetadata(FileMetadata info) {
            char c = 'x';
            char c2 = 'w';
            char c3 = 'r';
            if (BackupManagerService.DEBUG) {
                char c4;
                StringBuilder b = new StringBuilder(DumpState.DUMP_PACKAGES);
                if (info.type == BackupManagerService.MSG_RUN_ADB_BACKUP) {
                    c4 = 'd';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 256) != 0) {
                    c4 = 'r';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 128) != 0) {
                    c4 = 'w';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 64) != 0) {
                    c4 = 'x';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 32) != 0) {
                    c4 = 'r';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 16) != 0) {
                    c4 = 'w';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 8) != 0) {
                    c4 = 'x';
                } else {
                    c4 = '-';
                }
                b.append(c4);
                if ((info.mode & 4) == 0) {
                    c3 = '-';
                }
                b.append(c3);
                if ((info.mode & 2) == 0) {
                    c2 = '-';
                }
                b.append(c2);
                if ((info.mode & 1) == 0) {
                    c = '-';
                }
                b.append(c);
                Object[] objArr = new Object[BackupManagerService.SCHEDULE_FILE_VERSION];
                objArr[BackupManagerService.OP_PENDING] = Long.valueOf(info.size);
                b.append(String.format(" %9d ", objArr));
                b.append(new SimpleDateFormat("MMM dd HH:mm:ss ").format(new Date(info.mtime)));
                b.append(info.packageName);
                b.append(" :: ");
                b.append(info.domain);
                b.append(" :: ");
                b.append(info.path);
                Slog.i(BackupManagerService.TAG, b.toString());
            }
        }

        FileMetadata readTarHeaders(InputStream instream) throws IOException {
            IOException e;
            byte[] block = new byte[BackupManagerService.PBKDF2_SALT_SIZE];
            FileMetadata fileMetadata = null;
            if (readTarHeader(instream, block)) {
                try {
                    FileMetadata info = new FileMetadata();
                    try {
                        info.size = extractRadix(block, 124, BackupManagerService.MSG_RETRY_CLEAR, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.mtime = extractRadix(block, 136, BackupManagerService.MSG_RETRY_CLEAR, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.mode = extractRadix(block, 100, BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.MSG_RESTORE_TIMEOUT);
                        info.path = extractString(block, 345, 155);
                        String path = extractString(block, BackupManagerService.OP_PENDING, 100);
                        if (path.length() > 0) {
                            if (info.path.length() > 0) {
                                info.path += '/';
                            }
                            info.path += path;
                        }
                        int typeChar = block[156];
                        if (typeChar == 120) {
                            boolean gotHeader = readPaxExtendedHeader(instream, info);
                            if (gotHeader) {
                                gotHeader = readTarHeader(instream, block);
                            }
                            if (gotHeader) {
                                typeChar = block[156];
                            } else {
                                throw new IOException("Bad or missing pax header");
                            }
                        }
                        switch (typeChar) {
                            case BackupManagerService.OP_PENDING /*0*/:
                                if (BackupManagerService.DEBUG) {
                                    Slog.w(BackupManagerService.TAG, "Saw type=0 in tar header block, info=" + info);
                                }
                                return null;
                            case H.NOTIFY_APP_TRANSITION_CANCELLED /*48*/:
                                info.type = BackupManagerService.SCHEDULE_FILE_VERSION;
                                break;
                            case H.NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED /*53*/:
                                info.type = BackupManagerService.MSG_RUN_ADB_BACKUP;
                                if (info.size != 0) {
                                    Slog.w(BackupManagerService.TAG, "Directory entry with nonzero size in header");
                                    info.size = 0;
                                    break;
                                }
                                break;
                            default:
                                Slog.e(BackupManagerService.TAG, "Unknown tar entity type: " + typeChar);
                                throw new IOException("Unknown entity type " + typeChar);
                        }
                        if ("shared/".regionMatches(BackupManagerService.OP_PENDING, info.path, BackupManagerService.OP_PENDING, "shared/".length())) {
                            info.path = info.path.substring("shared/".length());
                            info.packageName = BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE;
                            info.domain = "shared";
                            if (BackupManagerService.DEBUG) {
                                Slog.i(BackupManagerService.TAG, "File in shared storage: " + info.path);
                            }
                        } else if ("apps/".regionMatches(BackupManagerService.OP_PENDING, info.path, BackupManagerService.OP_PENDING, "apps/".length())) {
                            info.path = info.path.substring("apps/".length());
                            int slash = info.path.indexOf(47);
                            if (slash < 0) {
                                throw new IOException("Illegal semantic path in " + info.path);
                            }
                            info.packageName = info.path.substring(BackupManagerService.OP_PENDING, slash);
                            info.path = info.path.substring(slash + BackupManagerService.SCHEDULE_FILE_VERSION);
                            if (!(info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME) || info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME))) {
                                slash = info.path.indexOf(47);
                                if (slash < 0) {
                                    throw new IOException("Illegal semantic path in non-manifest " + info.path);
                                }
                                info.domain = info.path.substring(BackupManagerService.OP_PENDING, slash);
                                info.path = info.path.substring(slash + BackupManagerService.SCHEDULE_FILE_VERSION);
                            }
                        }
                        fileMetadata = info;
                    } catch (IOException e2) {
                        e = e2;
                        fileMetadata = info;
                        if (BackupManagerService.DEBUG) {
                            Slog.e(BackupManagerService.TAG, "Parse error in header: " + e.getMessage());
                            HEXLOG(block);
                        }
                        throw e;
                    }
                } catch (IOException e3) {
                    e = e3;
                    if (BackupManagerService.DEBUG) {
                        Slog.e(BackupManagerService.TAG, "Parse error in header: " + e.getMessage());
                        HEXLOG(block);
                    }
                    throw e;
                }
            }
            return fileMetadata;
        }

        private void HEXLOG(byte[] block) {
            int offset = BackupManagerService.OP_PENDING;
            int todo = block.length;
            StringBuilder buf = new StringBuilder(64);
            while (todo > 0) {
                Object[] objArr = new Object[BackupManagerService.SCHEDULE_FILE_VERSION];
                objArr[BackupManagerService.OP_PENDING] = Integer.valueOf(offset);
                buf.append(String.format("%04x   ", objArr));
                int numThisLine = todo > 16 ? 16 : todo;
                for (int i = BackupManagerService.OP_PENDING; i < numThisLine; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                    objArr = new Object[BackupManagerService.SCHEDULE_FILE_VERSION];
                    objArr[BackupManagerService.OP_PENDING] = Byte.valueOf(block[offset + i]);
                    buf.append(String.format("%02x ", objArr));
                }
                Slog.i("hexdump", buf.toString());
                buf.setLength(BackupManagerService.OP_PENDING);
                todo -= numThisLine;
                offset += numThisLine;
            }
        }

        int readExactly(InputStream in, byte[] buffer, int offset, int size) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
            int soFar = BackupManagerService.OP_PENDING;
            while (soFar < size) {
                int nRead = in.read(buffer, offset + soFar, size - soFar);
                if (nRead <= 0) {
                    break;
                }
                soFar += nRead;
            }
            return soFar;
        }

        boolean readTarHeader(InputStream instream, byte[] block) throws IOException {
            int got = readExactly(instream, block, BackupManagerService.OP_PENDING, BackupManagerService.PBKDF2_SALT_SIZE);
            if (got == 0) {
                return BackupManagerService.MORE_DEBUG;
            }
            if (got < BackupManagerService.PBKDF2_SALT_SIZE) {
                throw new IOException("Unable to read full block header");
            }
            this.mBytes += 512;
            return BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        boolean readPaxExtendedHeader(InputStream instream, FileMetadata info) throws IOException {
            if (info.size > 32768) {
                Slog.w(BackupManagerService.TAG, "Suspiciously large pax header size " + info.size + " - aborting");
                throw new IOException("Sanity failure: pax header size " + info.size);
            }
            byte[] data = new byte[(((int) ((info.size + 511) >> BackupManagerService.MSG_FULL_CONFIRMATION_TIMEOUT)) * BackupManagerService.PBKDF2_SALT_SIZE)];
            if (readExactly(instream, data, BackupManagerService.OP_PENDING, data.length) < data.length) {
                throw new IOException("Unable to read full pax header");
            }
            this.mBytes += (long) data.length;
            int contentSize = (int) info.size;
            int offset = BackupManagerService.OP_PENDING;
            do {
                int eol = offset + BackupManagerService.SCHEDULE_FILE_VERSION;
                while (eol < contentSize && data[eol] != 32) {
                    eol += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
                if (eol >= contentSize) {
                    throw new IOException("Invalid pax data");
                }
                int linelen = (int) extractRadix(data, offset, eol - offset, BackupManagerService.MSG_RUN_ADB_RESTORE);
                int key = eol + BackupManagerService.SCHEDULE_FILE_VERSION;
                eol = (offset + linelen) + BackupManagerService.OP_TIMEOUT;
                int value = key + BackupManagerService.SCHEDULE_FILE_VERSION;
                while (data[value] != 61 && value <= eol) {
                    value += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
                if (value > eol) {
                    throw new IOException("Invalid pax declaration");
                }
                String keyStr = new String(data, key, value - key, "UTF-8");
                String valStr = new String(data, value + BackupManagerService.SCHEDULE_FILE_VERSION, (eol - value) + BackupManagerService.OP_TIMEOUT, "UTF-8");
                if ("path".equals(keyStr)) {
                    info.path = valStr;
                } else if ("size".equals(keyStr)) {
                    info.size = Long.parseLong(valStr);
                } else if (BackupManagerService.DEBUG) {
                    Slog.i(BackupManagerService.TAG, "Unhandled pax key: " + key);
                }
                offset += linelen;
            } while (offset < contentSize);
            return BackupManagerService.DEBUG_BACKUP_TRACE;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long extractRadix(byte[] data, int offset, int maxChars, int radix) throws IOException {
            long value = 0;
            int end = offset + maxChars;
            int i = offset;
            while (i < end) {
                byte b = data[i];
                if (!(b == null || b == 32)) {
                    if (b < 48 || b > (radix + 48) + BackupManagerService.OP_TIMEOUT) {
                        throw new IOException("Invalid number in header: '" + ((char) b) + "' for radix " + radix);
                    }
                    value = (((long) radix) * value) + ((long) (b - 48));
                    i += BackupManagerService.SCHEDULE_FILE_VERSION;
                }
            }
            return value;
        }

        String extractString(byte[] data, int offset, int maxChars) throws IOException {
            int end = offset + maxChars;
            int eos = offset;
            while (eos < end && data[eos] != null) {
                eos += BackupManagerService.SCHEDULE_FILE_VERSION;
            }
            return new String(data, offset, eos - offset, "US-ASCII");
        }

        void sendStartRestore() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onStartRestore();
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full restore observer went away: startRestore");
                    this.mObserver = null;
                }
            }
        }

        void sendOnRestorePackage(String name) {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onRestorePackage(name);
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full restore observer went away: restorePackage");
                    this.mObserver = null;
                }
            }
        }

        void sendEndRestore() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onEndRestore();
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "full restore observer went away: endRestore");
                    this.mObserver = null;
                }
            }
        }
    }

    class PerformBackupTask implements BackupRestoreTask {
        private static final /* synthetic */ int[] -com-android-server-backup-BackupManagerService$BackupStateSwitchesValues = null;
        private static final String TAG = "PerformBackupTask";
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$backup$BackupManagerService$BackupState;
        IBackupAgent mAgentBinder;
        ParcelFileDescriptor mBackupData;
        File mBackupDataName;
        PackageInfo mCurrentPackage;
        BackupState mCurrentState;
        boolean mFinished;
        File mJournal;
        ParcelFileDescriptor mNewState;
        File mNewStateName;
        IBackupObserver mObserver;
        ArrayList<BackupRequest> mOriginalQueue;
        ArrayList<String> mPendingFullBackups;
        ArrayList<BackupRequest> mQueue;
        ParcelFileDescriptor mSavedState;
        File mSavedStateName;
        File mStateDir;
        int mStatus;
        IBackupTransport mTransport;
        boolean mUserInitiated;
        final /* synthetic */ BackupManagerService this$0;

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$BackupStateSwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$BackupStateSwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$BackupStateSwitchesValues;
            }
            int[] iArr = new int[BackupState.values().length];
            try {
                iArr[BackupState.FINAL.ordinal()] = BackupManagerService.SCHEDULE_FILE_VERSION;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[BackupState.INITIAL.ordinal()] = BackupManagerService.MSG_RUN_ADB_BACKUP;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[BackupState.RUNNING_QUEUE.ordinal()] = BackupManagerService.MSG_RUN_RESTORE;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$BackupStateSwitchesValues = iArr;
            return iArr;
        }

        public PerformBackupTask(BackupManagerService this$0, IBackupTransport transport, String dirName, ArrayList<BackupRequest> queue, File journal, IBackupObserver observer, ArrayList<String> pendingFullBackups, boolean userInitiated) {
            this.this$0 = this$0;
            this.mTransport = transport;
            this.mOriginalQueue = queue;
            this.mJournal = journal;
            this.mObserver = observer;
            this.mPendingFullBackups = pendingFullBackups;
            this.mUserInitiated = userInitiated;
            this.mStateDir = new File(this$0.mBaseStateDir, dirName);
            this.mCurrentState = BackupState.INITIAL;
            this.mFinished = BackupManagerService.MORE_DEBUG;
            this$0.addBackupTrace("STATE => INITIAL");
        }

        public void execute() {
            switch (-getcom-android-server-backup-BackupManagerService$BackupStateSwitchesValues()[this.mCurrentState.ordinal()]) {
                case BackupManagerService.SCHEDULE_FILE_VERSION /*1*/:
                    if (this.mFinished) {
                        Slog.e(TAG, "Duplicate finish");
                    } else {
                        finalizeBackup();
                    }
                    this.mFinished = BackupManagerService.DEBUG_BACKUP_TRACE;
                case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                    beginBackup();
                case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                    invokeNextAgent();
                default:
            }
        }

        void beginBackup() {
            this.this$0.clearBackupTrace();
            StringBuilder b = new StringBuilder(BackupManagerService.PBKDF2_KEY_SIZE);
            b.append("beginBackup: [");
            for (BackupRequest req : this.mOriginalQueue) {
                b.append(' ');
                b.append(req.packageName);
            }
            b.append(" ]");
            this.this$0.addBackupTrace(b.toString());
            this.mAgentBinder = null;
            this.mStatus = BackupManagerService.OP_PENDING;
            if (this.mOriginalQueue.isEmpty() && this.mPendingFullBackups.isEmpty()) {
                Slog.w(TAG, "Backup begun with an empty queue - nothing to do.");
                this.this$0.addBackupTrace("queue empty at begin");
                BackupManagerService.sendBackupFinished(this.mObserver, BackupManagerService.OP_PENDING);
                executeNextState(BackupState.FINAL);
                return;
            }
            this.mQueue = (ArrayList) this.mOriginalQueue.clone();
            for (int i = BackupManagerService.OP_PENDING; i < this.mQueue.size(); i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                if (BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(((BackupRequest) this.mQueue.get(i)).packageName)) {
                    this.mQueue.remove(i);
                    break;
                }
            }
            if (BackupManagerService.DEBUG) {
                Slog.v(TAG, "Beginning backup of " + this.mQueue.size() + " targets");
            }
            File pmState = new File(this.mStateDir, BackupManagerService.PACKAGE_MANAGER_SENTINEL);
            try {
                String transportName = this.mTransport.transportDirName();
                EventLog.writeEvent(EventLogTags.BACKUP_START, transportName);
                if (this.mStatus == 0 && pmState.length() <= 0) {
                    Slog.i(TAG, "Initializing (wiping) backup state and transport storage");
                    this.this$0.addBackupTrace("initializing transport " + transportName);
                    this.this$0.resetBackupState(this.mStateDir);
                    this.mStatus = this.mTransport.initializeDevice();
                    this.this$0.addBackupTrace("transport.initializeDevice() == " + this.mStatus);
                    if (this.mStatus == 0) {
                        EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE, new Object[BackupManagerService.OP_PENDING]);
                    } else {
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                        Slog.e(TAG, "Transport error in initializeDevice()");
                    }
                }
                if (this.mStatus == 0) {
                    this.mStatus = invokeAgentForBackup(BackupManagerService.PACKAGE_MANAGER_SENTINEL, IBackupAgent.Stub.asInterface(new PackageManagerBackupAgent(this.this$0.mPackageManager).onBind()), this.mTransport);
                    this.this$0.addBackupTrace("PMBA invoke: " + this.mStatus);
                    this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
                }
                if (this.mStatus == JobSchedulerShellCommand.CMD_ERR_NO_JOB) {
                    EventLog.writeEvent(EventLogTags.BACKUP_RESET, this.mTransport.transportDirName());
                }
                this.this$0.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    this.this$0.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Error in backup thread", e);
                this.this$0.addBackupTrace("Exception in backup thread: " + e);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                this.this$0.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    this.this$0.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            } catch (Throwable th) {
                this.this$0.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    this.this$0.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            }
        }

        void invokeNextAgent() {
            this.mStatus = BackupManagerService.OP_PENDING;
            this.this$0.addBackupTrace("invoke q=" + this.mQueue.size());
            if (this.mQueue.isEmpty()) {
                executeNextState(BackupState.FINAL);
                return;
            }
            BackupRequest request = (BackupRequest) this.mQueue.get(BackupManagerService.OP_PENDING);
            this.mQueue.remove(BackupManagerService.OP_PENDING);
            Slog.d(TAG, "starting key/value backup of " + request);
            this.this$0.addBackupTrace("launch agent for " + request.packageName);
            BackupState nextState;
            try {
                this.mCurrentPackage = this.this$0.mPackageManager.getPackageInfo(request.packageName, 64);
                if (!BackupManagerService.appIsEligibleForBackup(this.mCurrentPackage.applicationInfo)) {
                    Slog.i(TAG, "Package " + request.packageName + " no longer supports backup; skipping");
                    this.this$0.addBackupTrace("skipping - not eligible, completion is noop");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    this.this$0.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            this.this$0.dataChangedImpl(request.packageName);
                            this.mStatus = BackupManagerService.OP_PENDING;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = BackupManagerService.OP_PENDING;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        this.this$0.addBackupTrace("expecting completion/timeout callback");
                    }
                } else if (BackupManagerService.appGetsFullBackup(this.mCurrentPackage)) {
                    Slog.i(TAG, "Package " + request.packageName + " requests full-data rather than key/value; skipping");
                    this.this$0.addBackupTrace("skipping - fullBackupOnly, completion is noop");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    this.this$0.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            this.this$0.dataChangedImpl(request.packageName);
                            this.mStatus = BackupManagerService.OP_PENDING;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = BackupManagerService.OP_PENDING;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        this.this$0.addBackupTrace("expecting completion/timeout callback");
                    }
                } else if (BackupManagerService.appIsStopped(this.mCurrentPackage.applicationInfo)) {
                    this.this$0.addBackupTrace("skipping - stopped");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    this.this$0.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            this.this$0.dataChangedImpl(request.packageName);
                            this.mStatus = BackupManagerService.OP_PENDING;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = BackupManagerService.OP_PENDING;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        this.this$0.addBackupTrace("expecting completion/timeout callback");
                    }
                } else {
                    try {
                        boolean z;
                        this.this$0.mWakelock.setWorkSource(new WorkSource(this.mCurrentPackage.applicationInfo.uid));
                        IBackupAgent agent = this.this$0.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, BackupManagerService.OP_PENDING);
                        BackupManagerService backupManagerService = this.this$0;
                        StringBuilder append = new StringBuilder().append("agent bound; a? = ");
                        if (agent != null) {
                            z = BackupManagerService.DEBUG_BACKUP_TRACE;
                        } else {
                            z = BackupManagerService.MORE_DEBUG;
                        }
                        backupManagerService.addBackupTrace(append.append(z).toString());
                        if (agent != null) {
                            this.mAgentBinder = agent;
                            this.mStatus = invokeAgentForBackup(request.packageName, agent, this.mTransport);
                        } else {
                            this.mStatus = -1003;
                        }
                    } catch (SecurityException ex) {
                        Slog.d(TAG, "error in bind/backup", ex);
                        this.mStatus = -1003;
                        this.this$0.addBackupTrace("agent SE");
                    }
                    this.this$0.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            this.this$0.dataChangedImpl(request.packageName);
                            this.mStatus = BackupManagerService.OP_PENDING;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = BackupManagerService.OP_PENDING;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        this.this$0.addBackupTrace("expecting completion/timeout callback");
                    }
                }
            } catch (NameNotFoundException e) {
                Slog.d(TAG, "Package does not exist; skipping");
                this.this$0.addBackupTrace("no such package");
                this.mStatus = -1004;
                this.this$0.mWakelock.setWorkSource(null);
                if (this.mStatus != 0) {
                    nextState = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.this$0.dataChangedImpl(request.packageName);
                        this.mStatus = BackupManagerService.OP_PENDING;
                        if (this.mQueue.isEmpty()) {
                            nextState = BackupState.FINAL;
                        }
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = BackupManagerService.OP_PENDING;
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                } else {
                    this.this$0.addBackupTrace("expecting completion/timeout callback");
                }
            } catch (Throwable th) {
                this.this$0.mWakelock.setWorkSource(null);
                if (this.mStatus != 0) {
                    nextState = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.this$0.dataChangedImpl(request.packageName);
                        this.mStatus = BackupManagerService.OP_PENDING;
                        if (this.mQueue.isEmpty()) {
                            nextState = BackupState.FINAL;
                        }
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = BackupManagerService.OP_PENDING;
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                } else {
                    this.this$0.addBackupTrace("expecting completion/timeout callback");
                }
            }
        }

        void finalizeBackup() {
            this.this$0.addBackupTrace("finishing");
            if (!(this.mJournal == null || this.mJournal.delete())) {
                Slog.e(TAG, "Unable to remove backup journal file " + this.mJournal);
            }
            if (this.this$0.mCurrentToken == 0 && this.mStatus == 0) {
                this.this$0.addBackupTrace("success; recording token");
                try {
                    this.this$0.mCurrentToken = this.mTransport.getCurrentRestoreSet();
                    this.this$0.writeRestoreTokens();
                } catch (RemoteException e) {
                    this.this$0.addBackupTrace("transport threw returning token");
                }
            }
            synchronized (this.this$0.mQueueLock) {
                this.this$0.mBackupRunning = BackupManagerService.MORE_DEBUG;
                if (this.mStatus == JobSchedulerShellCommand.CMD_ERR_NO_JOB) {
                    this.this$0.addBackupTrace("init required; rerunning");
                    try {
                        String name = this.this$0.getTransportName(this.mTransport);
                        if (name != null) {
                            this.this$0.mPendingInits.add(name);
                        } else if (BackupManagerService.DEBUG) {
                            Slog.w(TAG, "Couldn't find name of transport " + this.mTransport + " for init");
                        }
                    } catch (Exception e2) {
                        Slog.w(TAG, "Failed to query transport name heading for init", e2);
                    }
                    clearMetadata();
                    this.this$0.backupNow();
                }
            }
            this.this$0.clearBackupTrace();
            if (this.mStatus != 0 || this.mPendingFullBackups == null || this.mPendingFullBackups.isEmpty()) {
                switch (this.mStatus) {
                    case JobSchedulerShellCommand.CMD_ERR_NO_JOB /*-1001*/:
                        BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        break;
                    case BackupManagerService.OP_PENDING /*0*/:
                        BackupManagerService.sendBackupFinished(this.mObserver, BackupManagerService.OP_PENDING);
                        break;
                    default:
                        BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        break;
                }
            }
            Slog.d(TAG, "Starting full backups for: " + this.mPendingFullBackups);
            String[] fullBackups = (String[]) this.mPendingFullBackups.toArray(new String[this.mPendingFullBackups.size()]);
            PerformFullTransportBackupTask task = new PerformFullTransportBackupTask(this.this$0, null, fullBackups, BackupManagerService.MORE_DEBUG, null, new CountDownLatch(BackupManagerService.SCHEDULE_FILE_VERSION), this.mObserver, this.mUserInitiated);
            this.this$0.mWakelock.acquire();
            new Thread(task, "full-transport-requested").start();
            Slog.i(BackupManagerService.TAG, "K/V backup pass finished.");
            this.this$0.mWakelock.release();
        }

        void clearMetadata() {
            File pmState = new File(this.mStateDir, BackupManagerService.PACKAGE_MANAGER_SENTINEL);
            if (pmState.exists()) {
                pmState.delete();
            }
        }

        int invokeAgentForBackup(String packageName, IBackupAgent agent, IBackupTransport transport) {
            if (BackupManagerService.DEBUG) {
                Slog.d(TAG, "invokeAgentForBackup on " + packageName);
            }
            this.this$0.addBackupTrace("invoking " + packageName);
            this.mSavedStateName = new File(this.mStateDir, packageName);
            this.mBackupDataName = new File(this.this$0.mDataDir, packageName + ".data");
            this.mNewStateName = new File(this.mStateDir, packageName + ".new");
            this.mSavedState = null;
            this.mBackupData = null;
            this.mNewState = null;
            int token = this.this$0.generateToken();
            try {
                if (packageName.equals(BackupManagerService.PACKAGE_MANAGER_SENTINEL)) {
                    this.mCurrentPackage = new PackageInfo();
                    this.mCurrentPackage.packageName = packageName;
                }
                this.mSavedState = ParcelFileDescriptor.open(this.mSavedStateName, 402653184);
                this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
                if (!SELinux.restorecon(this.mBackupDataName)) {
                    Slog.e(TAG, "SELinux restorecon failed on " + this.mBackupDataName);
                }
                this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
                this.this$0.addBackupTrace("setting timeout");
                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_RESTORE_FINISHED_INTERVAL, this);
                this.this$0.addBackupTrace("calling agent doBackup()");
                agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, token, this.this$0.mBackupManagerBinder);
                this.this$0.addBackupTrace("invoke success");
                return BackupManagerService.OP_PENDING;
            } catch (Exception e) {
                Slog.e(TAG, "Error invoking for backup on " + packageName);
                this.this$0.addBackupTrace("exception: " + e);
                Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = packageName;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = e.toString();
                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, objArr);
                agentErrorCleanup();
                return -1003;
            }
        }

        public void failAgent(IBackupAgent agent, String message) {
            try {
                agent.fail(message);
            } catch (Exception e) {
                Slog.w(TAG, "Error conveying failure to " + this.mCurrentPackage.packageName);
            }
        }

        private String SHA1Checksum(byte[] input) {
            try {
                byte[] checksum = MessageDigest.getInstance("SHA-1").digest(input);
                StringBuffer sb = new StringBuffer(checksum.length * BackupManagerService.MSG_RUN_ADB_BACKUP);
                for (int i = BackupManagerService.OP_PENDING; i < checksum.length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                    sb.append(Integer.toHexString(checksum[i]));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                Slog.e(TAG, "Unable to use SHA-1!");
                return "00";
            }
        }

        private void writeWidgetPayloadIfAppropriate(FileDescriptor fd, String pkgName) throws IOException {
            Throwable th;
            Throwable th2;
            Throwable th3;
            Throwable th4;
            byte[] widgetState = AppWidgetBackupBridge.getWidgetState(pkgName, BackupManagerService.OP_PENDING);
            File widgetFile = new File(this.mStateDir, pkgName + "_widget");
            boolean priorStateExists = widgetFile.exists();
            if (priorStateExists || widgetState != null) {
                String str = null;
                if (widgetState != null) {
                    str = SHA1Checksum(widgetState);
                    if (priorStateExists) {
                        th = null;
                        FileInputStream fileInputStream = null;
                        DataInputStream dataInputStream = null;
                        try {
                            FileInputStream fin = new FileInputStream(widgetFile);
                            try {
                                DataInputStream in = new DataInputStream(fin);
                                try {
                                    String priorChecksum = in.readUTF();
                                    if (in != null) {
                                        try {
                                            in.close();
                                        } catch (Throwable th5) {
                                            th = th5;
                                        }
                                    }
                                    if (fin != null) {
                                        try {
                                            fin.close();
                                        } catch (Throwable th6) {
                                            th2 = th6;
                                            if (th != null) {
                                                if (th != th2) {
                                                    th.addSuppressed(th2);
                                                    th2 = th;
                                                }
                                            }
                                        }
                                    }
                                    th2 = th;
                                    if (th2 != null) {
                                        throw th2;
                                    } else if (Objects.equals(str, priorChecksum)) {
                                        return;
                                    }
                                } catch (Throwable th7) {
                                    th2 = th7;
                                    dataInputStream = in;
                                    fileInputStream = fin;
                                    if (dataInputStream != null) {
                                        try {
                                            dataInputStream.close();
                                        } catch (Throwable th8) {
                                            th4 = th8;
                                            if (th != null) {
                                                if (th != th4) {
                                                    th.addSuppressed(th4);
                                                    th4 = th;
                                                }
                                            }
                                        }
                                    }
                                    th4 = th;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (Throwable th9) {
                                            th = th9;
                                            if (th4 != null) {
                                                if (th4 != th) {
                                                    th4.addSuppressed(th);
                                                    th = th4;
                                                }
                                            }
                                        }
                                    }
                                    th = th4;
                                    if (th != null) {
                                        throw th;
                                    }
                                    throw th2;
                                }
                            } catch (Throwable th10) {
                                th2 = th10;
                                fileInputStream = fin;
                                if (dataInputStream != null) {
                                    dataInputStream.close();
                                }
                                th4 = th;
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                th = th4;
                                if (th != null) {
                                    throw th2;
                                }
                                throw th;
                            }
                        } catch (Throwable th11) {
                            th2 = th11;
                            if (dataInputStream != null) {
                                dataInputStream.close();
                            }
                            th4 = th;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            th = th4;
                            if (th != null) {
                                throw th;
                            }
                            throw th2;
                        }
                    }
                }
                BackupDataOutput out = new BackupDataOutput(fd);
                if (widgetState != null) {
                    th = null;
                    FileOutputStream fileOutputStream = null;
                    DataOutputStream dataOutputStream = null;
                    try {
                        FileOutputStream fout = new FileOutputStream(widgetFile);
                        try {
                            DataOutputStream stateOut = new DataOutputStream(fout);
                            try {
                                stateOut.writeUTF(str);
                                if (stateOut != null) {
                                    try {
                                        stateOut.close();
                                    } catch (Throwable th12) {
                                        th = th12;
                                    }
                                }
                                if (fout != null) {
                                    try {
                                        fout.close();
                                    } catch (Throwable th13) {
                                        th2 = th13;
                                        if (th != null) {
                                            if (th != th2) {
                                                th.addSuppressed(th2);
                                                th2 = th;
                                            }
                                        }
                                    }
                                }
                                th2 = th;
                                if (th2 != null) {
                                    throw th2;
                                }
                                out.writeEntityHeader(BackupManagerService.KEY_WIDGET_STATE, widgetState.length);
                                out.writeEntityData(widgetState, widgetState.length);
                            } catch (Throwable th14) {
                                th2 = th14;
                                dataOutputStream = stateOut;
                                fileOutputStream = fout;
                                if (dataOutputStream != null) {
                                    try {
                                        dataOutputStream.close();
                                    } catch (Throwable th15) {
                                        th4 = th15;
                                        if (th != null) {
                                            if (th != th4) {
                                                th.addSuppressed(th4);
                                                th4 = th;
                                            }
                                        }
                                    }
                                }
                                th4 = th;
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (Throwable th16) {
                                        th = th16;
                                        if (th4 != null) {
                                            if (th4 != th) {
                                                th4.addSuppressed(th);
                                                th = th4;
                                            }
                                        }
                                    }
                                }
                                th = th4;
                                if (th != null) {
                                    throw th;
                                }
                                throw th2;
                            }
                        } catch (Throwable th17) {
                            th2 = th17;
                            fileOutputStream = fout;
                            if (dataOutputStream != null) {
                                dataOutputStream.close();
                            }
                            th4 = th;
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            th = th4;
                            if (th != null) {
                                throw th2;
                            }
                            throw th;
                        }
                    } catch (Throwable th18) {
                        th2 = th18;
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        th4 = th;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        th = th4;
                        if (th != null) {
                            throw th;
                        }
                        throw th2;
                    }
                }
                out.writeEntityHeader(BackupManagerService.KEY_WIDGET_STATE, BackupManagerService.OP_TIMEOUT);
                widgetFile.delete();
            }
        }

        public void operationComplete(long unusedResult) {
            if (this.mBackupData == null) {
                this.this$0.addBackupTrace("late opComplete; curPkg = " + (this.mCurrentPackage != null ? this.mCurrentPackage.packageName : "[none]"));
                return;
            }
            int i;
            BackupState nextState;
            String pkgName = this.mCurrentPackage.packageName;
            long filepos = this.mBackupDataName.length();
            FileDescriptor fd = this.mBackupData.getFileDescriptor();
            ParcelFileDescriptor readFd;
            try {
                if (this.mCurrentPackage.applicationInfo != null) {
                    if ((this.mCurrentPackage.applicationInfo.flags & BackupManagerService.SCHEDULE_FILE_VERSION) == 0) {
                        readFd = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                        BackupDataInput in = new BackupDataInput(readFd.getFileDescriptor());
                        while (in.readNextHeader()) {
                            String key = in.getKey();
                            if (key == null || key.charAt(BackupManagerService.OP_PENDING) < '\uff00') {
                                in.skipEntityData();
                            } else {
                                failAgent(this.mAgentBinder, "Illegal backup key: " + key);
                                this.this$0.addBackupTrace("illegal key " + key + " from " + pkgName);
                                String[] strArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                                strArr[BackupManagerService.OP_PENDING] = pkgName;
                                strArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "bad key";
                                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, strArr);
                                this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
                                BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, -1003);
                                agentErrorCleanup();
                                if (readFd != null) {
                                    readFd.close();
                                }
                                return;
                            }
                        }
                        if (readFd != null) {
                            readFd.close();
                        }
                    }
                }
                writeWidgetPayloadIfAppropriate(fd, pkgName);
            } catch (IOException e) {
                Slog.w(TAG, "Unable to save widget state for " + pkgName);
                try {
                    Os.ftruncate(fd, filepos);
                } catch (ErrnoException e2) {
                    Slog.w(TAG, "Unable to roll back!");
                }
            } catch (Throwable th) {
                if (readFd != null) {
                    readFd.close();
                }
            }
            this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
            clearAgentState();
            this.this$0.addBackupTrace("operation complete");
            ParcelFileDescriptor parcelFileDescriptor = null;
            this.mStatus = BackupManagerService.OP_PENDING;
            long j = 0;
            try {
                j = this.mBackupDataName.length();
                if (j > 0) {
                    if (this.mStatus == 0) {
                        parcelFileDescriptor = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                        this.this$0.addBackupTrace("sending data to transport");
                        this.mStatus = this.mTransport.performBackup(this.mCurrentPackage, parcelFileDescriptor, this.mUserInitiated ? BackupManagerService.SCHEDULE_FILE_VERSION : BackupManagerService.OP_PENDING);
                    }
                    this.this$0.addBackupTrace("data delivered: " + this.mStatus);
                    if (this.mStatus == 0) {
                        this.this$0.addBackupTrace("finishing op on transport");
                        this.mStatus = this.mTransport.finishBackup();
                        this.this$0.addBackupTrace("finished: " + this.mStatus);
                    } else {
                        i = this.mStatus;
                        if (r0 == -1002) {
                            this.this$0.addBackupTrace("transport rejected package");
                        }
                    }
                } else {
                    this.this$0.addBackupTrace("no data to send");
                }
                if (this.mStatus == 0) {
                    this.mBackupDataName.delete();
                    this.mNewStateName.renameTo(this.mSavedStateName);
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, BackupManagerService.OP_PENDING);
                    strArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                    strArr[BackupManagerService.OP_PENDING] = pkgName;
                    strArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Long.valueOf(j);
                    EventLog.writeEvent(EventLogTags.BACKUP_PACKAGE, strArr);
                    this.this$0.logBackupComplete(pkgName);
                } else {
                    i = this.mStatus;
                    if (r0 == -1002) {
                        this.mBackupDataName.delete();
                        this.mNewStateName.delete();
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                        EventLogTags.writeBackupAgentFailure(pkgName, "Transport rejected");
                    } else {
                        i = this.mStatus;
                        if (r0 == -1005) {
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, -1005);
                            EventLog.writeEvent(EventLogTags.BACKUP_QUOTA_EXCEEDED, pkgName);
                        } else {
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                        }
                    }
                }
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Exception e4) {
                BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                Slog.e(TAG, "Transport error backing up " + pkgName, e4);
                EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Throwable th2) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e6) {
                    }
                }
            }
            if (this.mStatus != 0) {
                i = this.mStatus;
                if (r0 != -1002) {
                    i = this.mStatus;
                    if (r0 == -1005) {
                        if (this.mAgentBinder != null) {
                            try {
                                this.mAgentBinder.doQuotaExceeded(j, this.mTransport.getBackupQuota(this.mCurrentPackage.packageName, BackupManagerService.MORE_DEBUG));
                            } catch (RemoteException e7) {
                                Slog.e(TAG, "Unable to contact backup agent for quota exceeded");
                            }
                        }
                        nextState = this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE;
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                }
            }
            nextState = this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE;
            executeNextState(nextState);
        }

        public void handleTimeout() {
            Slog.e(TAG, "Timeout backing up " + this.mCurrentPackage.packageName);
            Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
            objArr[BackupManagerService.OP_PENDING] = this.mCurrentPackage.packageName;
            objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "timeout";
            EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, objArr);
            this.this$0.addBackupTrace("timeout of " + this.mCurrentPackage.packageName);
            agentErrorCleanup();
            this.this$0.dataChangedImpl(this.mCurrentPackage.packageName);
        }

        void revertAndEndBackup() {
            long delay;
            this.this$0.addBackupTrace("transport error; reverting");
            try {
                delay = this.mTransport.requestBackupTime();
            } catch (Exception e) {
                Slog.w(TAG, "Unable to contact transport for recommended backoff");
                delay = 0;
            }
            KeyValueBackupJob.schedule(this.this$0.mContext, delay);
            for (BackupRequest request : this.mOriginalQueue) {
                this.this$0.dataChangedImpl(request.packageName);
            }
        }

        void agentErrorCleanup() {
            BackupState backupState;
            this.mBackupDataName.delete();
            this.mNewStateName.delete();
            clearAgentState();
            if (this.mQueue.isEmpty()) {
                backupState = BackupState.FINAL;
            } else {
                backupState = BackupState.RUNNING_QUEUE;
            }
            executeNextState(backupState);
        }

        void clearAgentState() {
            try {
                if (this.mSavedState != null) {
                    this.mSavedState.close();
                }
            } catch (IOException e) {
            }
            try {
                if (this.mBackupData != null) {
                    this.mBackupData.close();
                }
            } catch (IOException e2) {
            }
            try {
                if (this.mNewState != null) {
                    this.mNewState.close();
                }
            } catch (IOException e3) {
            }
            synchronized (this.this$0.mCurrentOpLock) {
                this.this$0.mCurrentOperations.clear();
                this.mNewState = null;
                this.mBackupData = null;
                this.mSavedState = null;
            }
            if (this.mCurrentPackage.applicationInfo != null) {
                this.this$0.addBackupTrace("unbinding " + this.mCurrentPackage.packageName);
                try {
                    this.this$0.mActivityManager.unbindBackupAgent(this.mCurrentPackage.applicationInfo);
                } catch (RemoteException e4) {
                }
            }
        }

        void executeNextState(BackupState nextState) {
            this.this$0.addBackupTrace("executeNextState => " + nextState);
            this.mCurrentState = nextState;
            this.this$0.mBackupHandler.sendMessage(this.this$0.mBackupHandler.obtainMessage(BackupManagerService.MSG_BACKUP_RESTORE_STEP, this));
        }
    }

    class PerformClearTask implements Runnable {
        PackageInfo mPackage;
        IBackupTransport mTransport;
        final /* synthetic */ BackupManagerService this$0;

        PerformClearTask(BackupManagerService this$0, IBackupTransport transport, PackageInfo packageInfo) {
            this.this$0 = this$0;
            this.mTransport = transport;
            this.mPackage = packageInfo;
        }

        public void run() {
            try {
                new File(new File(this.this$0.mBaseStateDir, this.mTransport.transportDirName()), this.mPackage.packageName).delete();
                this.mTransport.clearBackupData(this.mPackage);
            } catch (RemoteException e) {
            } catch (Exception e2) {
                Slog.e(BackupManagerService.TAG, "Transport threw attempting to clear data for " + this.mPackage);
            } finally {
                try {
                    this.mTransport.finishBackup();
                } catch (RemoteException e3) {
                }
                this.this$0.mWakelock.release();
            }
        }
    }

    class PerformFullTransportBackupTask extends FullBackupTask {
        static final String TAG = "PFTBT";
        IBackupObserver mBackupObserver;
        FullBackupJob mJob;
        AtomicBoolean mKeepRunning;
        CountDownLatch mLatch;
        ArrayList<PackageInfo> mPackages;
        boolean mUpdateSchedule;
        boolean mUserInitiated;
        final /* synthetic */ BackupManagerService this$0;

        class SinglePackageBackupPreflight implements BackupRestoreTask, FullBackupPreflight {
            final CountDownLatch mLatch;
            final AtomicLong mResult;
            final IBackupTransport mTransport;
            final /* synthetic */ PerformFullTransportBackupTask this$1;

            public SinglePackageBackupPreflight(PerformFullTransportBackupTask this$1, IBackupTransport transport) {
                this.this$1 = this$1;
                this.mResult = new AtomicLong(-1003);
                this.mLatch = new CountDownLatch(BackupManagerService.SCHEDULE_FILE_VERSION);
                this.mTransport = transport;
            }

            public int preflightFullBackup(PackageInfo pkg, IBackupAgent agent) {
                int result;
                try {
                    int token = this.this$1.this$0.generateToken();
                    this.this$1.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this);
                    this.this$1.this$0.addBackupTrace("preflighting");
                    agent.doMeasureFullBackup(token, this.this$1.this$0.mBackupManagerBinder);
                    this.mLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                    long totalSize = this.mResult.get();
                    if (totalSize < 0) {
                        return (int) totalSize;
                    }
                    result = this.mTransport.checkFullBackupSize(totalSize);
                    if (result == -1005) {
                        agent.doQuotaExceeded(totalSize, this.mTransport.getBackupQuota(pkg.packageName, BackupManagerService.DEBUG_BACKUP_TRACE));
                    }
                    return result;
                } catch (Exception e) {
                    Slog.w(PerformFullTransportBackupTask.TAG, "Exception preflighting " + pkg.packageName + ": " + e.getMessage());
                    result = -1003;
                }
            }

            public void execute() {
            }

            public void operationComplete(long result) {
                this.mResult.set(result);
                this.mLatch.countDown();
            }

            public void handleTimeout() {
                this.mResult.set(-1003);
                this.mLatch.countDown();
            }

            public long getExpectedSizeOrErrorCode() {
                try {
                    this.mLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                    return this.mResult.get();
                } catch (InterruptedException e) {
                    return -1;
                }
            }
        }

        class SinglePackageBackupRunner implements Runnable, BackupRestoreTask {
            final CountDownLatch mBackupLatch;
            private volatile int mBackupResult;
            private FullBackupEngine mEngine;
            final ParcelFileDescriptor mOutput;
            final FullBackupPreflight mPreflight;
            final CountDownLatch mPreflightLatch;
            private volatile int mPreflightResult;
            final PackageInfo mTarget;
            final /* synthetic */ PerformFullTransportBackupTask this$1;

            SinglePackageBackupRunner(PerformFullTransportBackupTask this$1, ParcelFileDescriptor output, PackageInfo target, IBackupTransport transport) throws IOException {
                this.this$1 = this$1;
                this.mOutput = ParcelFileDescriptor.dup(output.getFileDescriptor());
                this.mTarget = target;
                this.mPreflight = new SinglePackageBackupPreflight(this$1, transport);
                this.mPreflightLatch = new CountDownLatch(BackupManagerService.SCHEDULE_FILE_VERSION);
                this.mBackupLatch = new CountDownLatch(BackupManagerService.SCHEDULE_FILE_VERSION);
                this.mPreflightResult = -1003;
                this.mBackupResult = -1003;
            }

            public void run() {
                this.mEngine = new FullBackupEngine(this.this$1.this$0, new FileOutputStream(this.mOutput.getFileDescriptor()), this.mPreflight, this.mTarget, BackupManagerService.MORE_DEBUG, this);
                try {
                    this.mPreflightResult = this.mEngine.preflightCheck();
                    this.mPreflightLatch.countDown();
                    if (this.mPreflightResult == 0) {
                        this.mBackupResult = this.mEngine.backupOnePackage();
                    }
                    this.mBackupLatch.countDown();
                    try {
                        this.mOutput.close();
                    } catch (IOException e) {
                        Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                    }
                } catch (Exception e2) {
                    try {
                        Slog.e(PerformFullTransportBackupTask.TAG, "Exception during full package backup of " + this.mTarget.packageName);
                        try {
                            this.mOutput.close();
                        } catch (IOException e3) {
                            Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                        }
                    } finally {
                        this.mBackupLatch.countDown();
                        try {
                            this.mOutput.close();
                        } catch (IOException e4) {
                            Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                        }
                    }
                } catch (Throwable th) {
                    this.mPreflightLatch.countDown();
                }
            }

            public void sendQuotaExceeded(long backupDataBytes, long quotaBytes) {
                this.mEngine.sendQuotaExceeded(backupDataBytes, quotaBytes);
            }

            long getPreflightResultBlocking() {
                try {
                    this.mPreflightLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                    if (this.mPreflightResult == 0) {
                        return this.mPreflight.getExpectedSizeOrErrorCode();
                    }
                    return (long) this.mPreflightResult;
                } catch (InterruptedException e) {
                    return -1003;
                }
            }

            int getBackupResultBlocking() {
                try {
                    this.mBackupLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                    return this.mBackupResult;
                } catch (InterruptedException e) {
                    return -1003;
                }
            }

            public void execute() {
            }

            public void operationComplete(long result) {
            }

            public void handleTimeout() {
                if (BackupManagerService.DEBUG) {
                    Slog.w(PerformFullTransportBackupTask.TAG, "Full backup timeout of " + this.mTarget.packageName);
                }
                this.this$1.this$0.tearDownAgentAndKill(this.mTarget.applicationInfo);
            }
        }

        PerformFullTransportBackupTask(BackupManagerService this$0, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, boolean userInitiated) {
            this.this$0 = this$0;
            super(this$0, observer);
            this.mUpdateSchedule = updateSchedule;
            this.mLatch = latch;
            this.mKeepRunning = new AtomicBoolean(BackupManagerService.DEBUG_BACKUP_TRACE);
            this.mJob = runningJob;
            this.mPackages = new ArrayList(whichPackages.length);
            this.mBackupObserver = backupObserver;
            this.mUserInitiated = userInitiated;
            int length = whichPackages.length;
            for (int i = BackupManagerService.OP_PENDING; i < length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                String pkg = whichPackages[i];
                try {
                    PackageInfo info = this$0.mPackageManager.getPackageInfo(pkg, 64);
                    if (!BackupManagerService.appIsEligibleForBackup(info.applicationInfo)) {
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else if (!BackupManagerService.appGetsFullBackup(info)) {
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else if (BackupManagerService.appIsStopped(info.applicationInfo)) {
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else {
                        this.mPackages.add(info);
                    }
                } catch (NameNotFoundException e) {
                    Slog.i(TAG, "Requested package " + pkg + " not found; ignoring");
                }
            }
        }

        public void setRunning(boolean running) {
            this.mKeepRunning.set(running);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            ParcelFileDescriptor[] parcelFileDescriptorArr = null;
            ParcelFileDescriptor[] parcelFileDescriptorArr2 = null;
            long backoff = 0;
            try {
                if (this.this$0.mEnabled) {
                    if (this.this$0.mProvisioned) {
                        IBackupTransport transport = this.this$0.getTransport(this.this$0.mCurrentTransport);
                        if (transport == null) {
                            Slog.w(TAG, "Transport not present; full data backup not performed");
                            if (BackupManagerService.DEBUG) {
                                Slog.i(TAG, "Full backup completed with status: " + JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            }
                            BackupManagerService.sendBackupFinished(this.mBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            cleanUpPipes(null);
                            cleanUpPipes(null);
                            if (this.mJob != null) {
                                this.mJob.finishBackupPass();
                            }
                            synchronized (this.this$0.mQueueLock) {
                                this.this$0.mRunningFullBackupTask = null;
                            }
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                                this.this$0.scheduleNextFullBackupJob(0);
                            }
                            Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                            this.this$0.mWakelock.release();
                            return;
                        }
                        int N = this.mPackages.size();
                        byte[] buffer = new byte[DumpState.DUMP_PREFERRED_XML];
                        for (int i = BackupManagerService.OP_PENDING; i < N; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                            PackageInfo currentPackage = (PackageInfo) this.mPackages.get(i);
                            String packageName = currentPackage.packageName;
                            if (BackupManagerService.DEBUG) {
                                Slog.i(TAG, "Initiating full-data transport backup of " + packageName);
                            }
                            EventLog.writeEvent(EventLogTags.FULL_BACKUP_PACKAGE, packageName);
                            parcelFileDescriptorArr2 = ParcelFileDescriptor.createPipe();
                            int backupPackageStatus = transport.performFullBackup(currentPackage, parcelFileDescriptorArr2[BackupManagerService.OP_PENDING], this.mUserInitiated ? BackupManagerService.SCHEDULE_FILE_VERSION : BackupManagerService.OP_PENDING);
                            if (backupPackageStatus == 0) {
                                parcelFileDescriptorArr2[BackupManagerService.OP_PENDING].close();
                                parcelFileDescriptorArr2[BackupManagerService.OP_PENDING] = null;
                                parcelFileDescriptorArr = ParcelFileDescriptor.createPipe();
                                SinglePackageBackupRunner backupRunner = new SinglePackageBackupRunner(this, parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION], currentPackage, transport);
                                parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION].close();
                                parcelFileDescriptorArr[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                                new Thread(backupRunner, "package-backup-bridge").start();
                                FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptorArr[BackupManagerService.OP_PENDING].getFileDescriptor());
                                FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptorArr2[BackupManagerService.SCHEDULE_FILE_VERSION].getFileDescriptor());
                                long totalRead = 0;
                                long preflightResult = backupRunner.getPreflightResultBlocking();
                                if (preflightResult < 0) {
                                    backupPackageStatus = (int) preflightResult;
                                } else {
                                    while (true) {
                                        if (!this.mKeepRunning.get()) {
                                            break;
                                        }
                                        int nRead = fileInputStream.read(buffer);
                                        if (nRead > 0) {
                                            fileOutputStream.write(buffer, BackupManagerService.OP_PENDING, nRead);
                                            backupPackageStatus = transport.sendBackupData(nRead);
                                            totalRead += (long) nRead;
                                            if (this.mBackupObserver != null && preflightResult > 0) {
                                                BackupManagerService.sendBackupOnUpdate(this.mBackupObserver, packageName, new BackupProgress(preflightResult, totalRead));
                                            }
                                        }
                                        if (nRead > 0 && backupPackageStatus == 0) {
                                        }
                                    }
                                    if (BackupManagerService.DEBUG_SCHEDULING) {
                                        Slog.i(TAG, "Full backup task told to stop");
                                    }
                                    if (backupPackageStatus == -1005) {
                                        long quota = transport.getBackupQuota(packageName, BackupManagerService.DEBUG_BACKUP_TRACE);
                                        Slog.w(TAG, "Package hit quota limit in-flight " + packageName + ": " + totalRead + " of " + quota);
                                        backupRunner.sendQuotaExceeded(totalRead, quota);
                                    }
                                }
                                if (this.mKeepRunning.get()) {
                                    int finishResult = transport.finishBackup();
                                    if (backupPackageStatus == 0) {
                                        backupPackageStatus = finishResult;
                                    }
                                } else {
                                    backupPackageStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                    transport.cancelFullBackup();
                                }
                                if (backupPackageStatus == 0) {
                                    int backupRunnerResult = backupRunner.getBackupResultBlocking();
                                    if (backupRunnerResult != 0) {
                                        backupPackageStatus = backupRunnerResult;
                                    }
                                }
                                if (backupPackageStatus != 0) {
                                    Slog.e(TAG, "Error " + backupPackageStatus + " backing up " + packageName);
                                }
                                backoff = transport.requestFullBackupTime();
                                if (BackupManagerService.DEBUG_SCHEDULING) {
                                    Slog.i(TAG, "Transport suggested backoff=" + backoff);
                                }
                            }
                            if (this.mUpdateSchedule) {
                                this.this$0.enqueueFullBackup(packageName, System.currentTimeMillis());
                            }
                            if (backupPackageStatus == -1002) {
                                BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, packageName, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                                if (BackupManagerService.DEBUG) {
                                    Slog.i(TAG, "Transport rejected backup of " + packageName + ", skipping");
                                }
                                String[] strArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                                strArr[BackupManagerService.OP_PENDING] = packageName;
                                strArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "transport rejected";
                                EventLog.writeEvent(EventLogTags.FULL_BACKUP_AGENT_FAILURE, strArr);
                            } else if (backupPackageStatus == -1005) {
                                BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, packageName, -1005);
                                if (BackupManagerService.DEBUG) {
                                    Slog.i(TAG, "Transport quota exceeded for package: " + packageName);
                                    EventLog.writeEvent(EventLogTags.FULL_BACKUP_QUOTA_EXCEEDED, packageName);
                                }
                            } else if (backupPackageStatus == -1003) {
                                BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, packageName, -1003);
                                Slog.w(TAG, "Application failure for package: " + packageName);
                                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, packageName);
                                this.this$0.tearDownAgentAndKill(currentPackage.applicationInfo);
                            } else if (backupPackageStatus != 0) {
                                BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, packageName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                Slog.w(TAG, "Transport failed; aborting backup: " + backupPackageStatus);
                                EventLog.writeEvent(EventLogTags.FULL_BACKUP_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                                if (BackupManagerService.DEBUG) {
                                    Slog.i(TAG, "Full backup completed with status: " + JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                }
                                BackupManagerService.sendBackupFinished(this.mBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                                cleanUpPipes(parcelFileDescriptorArr2);
                                cleanUpPipes(parcelFileDescriptorArr);
                                if (this.mJob != null) {
                                    this.mJob.finishBackupPass();
                                }
                                synchronized (this.this$0.mQueueLock) {
                                    this.this$0.mRunningFullBackupTask = null;
                                }
                                this.mLatch.countDown();
                                if (this.mUpdateSchedule) {
                                    this.this$0.scheduleNextFullBackupJob(backoff);
                                }
                                Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                                this.this$0.mWakelock.release();
                                return;
                            } else {
                                BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, packageName, BackupManagerService.OP_PENDING);
                                EventLog.writeEvent(EventLogTags.FULL_BACKUP_SUCCESS, packageName);
                                this.this$0.logBackupComplete(packageName);
                            }
                            cleanUpPipes(parcelFileDescriptorArr2);
                            cleanUpPipes(parcelFileDescriptorArr);
                        }
                        if (BackupManagerService.DEBUG) {
                            Slog.i(TAG, "Full backup completed with status: " + BackupManagerService.OP_PENDING);
                        }
                        BackupManagerService.sendBackupFinished(this.mBackupObserver, BackupManagerService.OP_PENDING);
                        cleanUpPipes(parcelFileDescriptorArr2);
                        cleanUpPipes(parcelFileDescriptorArr);
                        if (this.mJob != null) {
                            this.mJob.finishBackupPass();
                        }
                        synchronized (this.this$0.mQueueLock) {
                            this.this$0.mRunningFullBackupTask = null;
                        }
                        this.mLatch.countDown();
                        if (this.mUpdateSchedule) {
                            this.this$0.scheduleNextFullBackupJob(backoff);
                        }
                        Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                        this.this$0.mWakelock.release();
                        return;
                    }
                }
                if (BackupManagerService.DEBUG) {
                    Slog.i(TAG, "full backup requested but e=" + this.this$0.mEnabled + " p=" + this.this$0.mProvisioned + "; ignoring");
                }
                this.mUpdateSchedule = BackupManagerService.MORE_DEBUG;
                if (BackupManagerService.DEBUG) {
                    Slog.i(TAG, "Full backup completed with status: " + -2001);
                }
                BackupManagerService.sendBackupFinished(this.mBackupObserver, -2001);
                cleanUpPipes(null);
                cleanUpPipes(null);
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (this.this$0.mQueueLock) {
                    this.this$0.mRunningFullBackupTask = null;
                }
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    this.this$0.scheduleNextFullBackupJob(0);
                }
                Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                this.this$0.mWakelock.release();
            } catch (Exception e) {
                Slog.w(TAG, "Exception trying full transport backup", e);
                if (BackupManagerService.DEBUG) {
                    Slog.i(TAG, "Full backup completed with status: " + JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                }
                BackupManagerService.sendBackupFinished(this.mBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                cleanUpPipes(parcelFileDescriptorArr2);
                cleanUpPipes(parcelFileDescriptorArr);
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (this.this$0.mQueueLock) {
                }
                this.this$0.mRunningFullBackupTask = null;
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    this.this$0.scheduleNextFullBackupJob(backoff);
                }
                Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                this.this$0.mWakelock.release();
            } catch (Throwable th) {
                if (BackupManagerService.DEBUG) {
                    Slog.i(TAG, "Full backup completed with status: " + JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                }
                BackupManagerService.sendBackupFinished(this.mBackupObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                cleanUpPipes(parcelFileDescriptorArr2);
                cleanUpPipes(parcelFileDescriptorArr);
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (this.this$0.mQueueLock) {
                }
                this.this$0.mRunningFullBackupTask = null;
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    this.this$0.scheduleNextFullBackupJob(backoff);
                }
                Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                this.this$0.mWakelock.release();
            }
        }

        void cleanUpPipes(ParcelFileDescriptor[] pipes) {
            if (pipes != null) {
                ParcelFileDescriptor fd;
                if (pipes[BackupManagerService.OP_PENDING] != null) {
                    fd = pipes[BackupManagerService.OP_PENDING];
                    pipes[BackupManagerService.OP_PENDING] = null;
                    try {
                        fd.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "Unable to close pipe!");
                    }
                }
                if (pipes[BackupManagerService.SCHEDULE_FILE_VERSION] != null) {
                    fd = pipes[BackupManagerService.SCHEDULE_FILE_VERSION];
                    pipes[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                    try {
                        fd.close();
                    } catch (IOException e2) {
                        Slog.w(TAG, "Unable to close pipe!");
                    }
                }
            }
        }
    }

    class PerformInitializeTask implements Runnable {
        HashSet<String> mQueue;
        final /* synthetic */ BackupManagerService this$0;

        PerformInitializeTask(BackupManagerService this$0, HashSet<String> transportNames) {
            this.this$0 = this$0;
            this.mQueue = transportNames;
        }

        public void run() {
            try {
                for (String transportName : this.mQueue) {
                    IBackupTransport transport = this.this$0.getTransport(transportName);
                    if (transport == null) {
                        Slog.e(BackupManagerService.TAG, "Requested init for " + transportName + " but not found");
                    } else {
                        Slog.i(BackupManagerService.TAG, "Initializing (wiping) backup transport storage: " + transportName);
                        EventLog.writeEvent(EventLogTags.BACKUP_START, transport.transportDirName());
                        long startRealtime = SystemClock.elapsedRealtime();
                        int status = transport.initializeDevice();
                        if (status == 0) {
                            status = transport.finishBackup();
                        }
                        if (status == 0) {
                            Slog.i(BackupManagerService.TAG, "Device init successful");
                            int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                            EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE, new Object[BackupManagerService.OP_PENDING]);
                            this.this$0.resetBackupState(new File(this.this$0.mBaseStateDir, transport.transportDirName()));
                            Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                            objArr[BackupManagerService.OP_PENDING] = Integer.valueOf(BackupManagerService.OP_PENDING);
                            objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(millis);
                            EventLog.writeEvent(EventLogTags.BACKUP_SUCCESS, objArr);
                            synchronized (this.this$0.mQueueLock) {
                                this.this$0.recordInitPendingLocked(BackupManagerService.MORE_DEBUG, transportName);
                            }
                        } else {
                            Slog.e(BackupManagerService.TAG, "Transport error in initializeDevice()");
                            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                            synchronized (this.this$0.mQueueLock) {
                                this.this$0.recordInitPendingLocked(BackupManagerService.DEBUG_BACKUP_TRACE, transportName);
                            }
                            long delay = transport.requestBackupTime();
                            Slog.w(BackupManagerService.TAG, "Init failed on " + transportName + " resched in " + delay);
                            this.this$0.mAlarmManager.set(BackupManagerService.OP_PENDING, System.currentTimeMillis() + delay, this.this$0.mRunInitIntent);
                        }
                    }
                }
                this.this$0.mWakelock.release();
            } catch (RemoteException e) {
                this.this$0.mWakelock.release();
            } catch (Exception e2) {
                Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e2);
                this.this$0.mWakelock.release();
            } catch (Throwable th) {
                this.this$0.mWakelock.release();
            }
        }
    }

    class PerformUnifiedRestoreTask implements BackupRestoreTask {
        private static final /* synthetic */ int[] -com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$backup$BackupManagerService$UnifiedRestoreState;
        private List<PackageInfo> mAcceptSet;
        private IBackupAgent mAgent;
        ParcelFileDescriptor mBackupData;
        private File mBackupDataName;
        private int mCount;
        private PackageInfo mCurrentPackage;
        private boolean mDidLaunch;
        private boolean mFinished;
        private boolean mIsSystemRestore;
        ParcelFileDescriptor mNewState;
        private File mNewStateName;
        private IRestoreObserver mObserver;
        private PackageManagerBackupAgent mPmAgent;
        private int mPmToken;
        private RestoreDescription mRestoreDescription;
        private File mSavedStateName;
        private File mStageName;
        private long mStartRealtime;
        private UnifiedRestoreState mState;
        File mStateDir;
        private int mStatus;
        private PackageInfo mTargetPackage;
        private long mToken;
        private IBackupTransport mTransport;
        private byte[] mWidgetData;
        final /* synthetic */ BackupManagerService this$0;

        class EngineThread implements Runnable {
            FullRestoreEngine mEngine;
            FileInputStream mEngineStream;
            final /* synthetic */ PerformUnifiedRestoreTask this$1;

            EngineThread(PerformUnifiedRestoreTask this$1, FullRestoreEngine engine, ParcelFileDescriptor engineSocket) {
                this.this$1 = this$1;
                this.mEngine = engine;
                engine.setRunning(BackupManagerService.DEBUG_BACKUP_TRACE);
                this.mEngineStream = new FileInputStream(engineSocket.getFileDescriptor(), BackupManagerService.DEBUG_BACKUP_TRACE);
            }

            public boolean isRunning() {
                return this.mEngine.isRunning();
            }

            public int waitForResult() {
                return this.mEngine.waitForResult();
            }

            public void run() {
                while (this.mEngine.isRunning()) {
                    try {
                        this.mEngine.restoreOneFile(this.mEngineStream, BackupManagerService.MORE_DEBUG);
                    } finally {
                        IoUtils.closeQuietly(this.mEngineStream);
                    }
                }
            }

            public void handleTimeout() {
                IoUtils.closeQuietly(this.mEngineStream);
                this.mEngine.handleTimeout();
            }
        }

        class StreamFeederThread extends RestoreEngine implements Runnable, BackupRestoreTask {
            final String TAG;
            FullRestoreEngine mEngine;
            ParcelFileDescriptor[] mEnginePipes;
            EngineThread mEngineThread;
            ParcelFileDescriptor[] mTransportPipes;
            final /* synthetic */ PerformUnifiedRestoreTask this$1;

            public StreamFeederThread(PerformUnifiedRestoreTask this$1) throws IOException {
                this.this$1 = this$1;
                super(this$1.this$0);
                this.TAG = "StreamFeederThread";
                this.mTransportPipes = ParcelFileDescriptor.createPipe();
                this.mEnginePipes = ParcelFileDescriptor.createPipe();
                setRunning(BackupManagerService.DEBUG_BACKUP_TRACE);
            }

            public void run() {
                PerformUnifiedRestoreTask performUnifiedRestoreTask;
                boolean z;
                UnifiedRestoreState nextState = UnifiedRestoreState.RUNNING_QUEUE;
                int status = BackupManagerService.OP_PENDING;
                EventLog.writeEvent(EventLogTags.FULL_RESTORE_PACKAGE, this.this$1.mCurrentPackage.packageName);
                this.mEngine = new FullRestoreEngine(this.this$1.this$0, this, null, this.this$1.mCurrentPackage, BackupManagerService.MORE_DEBUG, BackupManagerService.MORE_DEBUG);
                this.mEngineThread = new EngineThread(this.this$1, this.mEngine, this.mEnginePipes[BackupManagerService.OP_PENDING]);
                ParcelFileDescriptor eWriteEnd = this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION];
                ParcelFileDescriptor tReadEnd = this.mTransportPipes[BackupManagerService.OP_PENDING];
                ParcelFileDescriptor tWriteEnd = this.mTransportPipes[BackupManagerService.SCHEDULE_FILE_VERSION];
                int bufferSize = DumpState.DUMP_VERSION;
                byte[] buffer = new byte[DumpState.DUMP_VERSION];
                FileOutputStream engineOut = new FileOutputStream(eWriteEnd.getFileDescriptor());
                FileInputStream fileInputStream = new FileInputStream(tReadEnd.getFileDescriptor());
                new Thread(this.mEngineThread, "unified-restore-engine").start();
                while (status == 0) {
                    try {
                        int result = this.this$1.mTransport.getNextFullRestoreDataChunk(tWriteEnd);
                        if (result > 0) {
                            if (result > bufferSize) {
                                bufferSize = result;
                                buffer = new byte[result];
                            }
                            int toCopy = result;
                            while (toCopy > 0) {
                                int n = fileInputStream.read(buffer, BackupManagerService.OP_PENDING, toCopy);
                                engineOut.write(buffer, BackupManagerService.OP_PENDING, n);
                                toCopy -= n;
                            }
                        } else if (result == BackupManagerService.OP_TIMEOUT) {
                            status = BackupManagerService.OP_PENDING;
                            break;
                        } else {
                            Slog.e("StreamFeederThread", "Error " + result + " streaming restore for " + this.this$1.mCurrentPackage.packageName);
                            EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                            status = result;
                        }
                    } catch (IOException e) {
                        Slog.e("StreamFeederThread", "Unable to route data for restore");
                        Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                        objArr[BackupManagerService.OP_PENDING] = this.this$1.mCurrentPackage.packageName;
                        objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "I/O error on pipes";
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                        status = -1003;
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.OP_PENDING]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.OP_PENDING]);
                        performUnifiedRestoreTask = this.this$1;
                        if (this.mEngine.getAgent() != null) {
                            z = BackupManagerService.DEBUG_BACKUP_TRACE;
                        } else {
                            z = BackupManagerService.MORE_DEBUG;
                        }
                        performUnifiedRestoreTask.mDidLaunch = z;
                        try {
                            this.this$1.mTransport.abortFullRestore();
                        } catch (RemoteException e2) {
                            status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        }
                        this.this$1.this$0.clearApplicationDataSynchronous(this.this$1.mCurrentPackage.packageName);
                        if (status == -1000) {
                            nextState = UnifiedRestoreState.FINAL;
                        } else {
                            nextState = UnifiedRestoreState.RUNNING_QUEUE;
                        }
                        this.this$1.executeNextState(nextState);
                        setRunning(BackupManagerService.MORE_DEBUG);
                        return;
                    } catch (RemoteException e3) {
                        Slog.e("StreamFeederThread", "Transport failed during restore");
                        EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                        status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.OP_PENDING]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.OP_PENDING]);
                        performUnifiedRestoreTask = this.this$1;
                        if (this.mEngine.getAgent() != null) {
                            z = BackupManagerService.DEBUG_BACKUP_TRACE;
                        } else {
                            z = BackupManagerService.MORE_DEBUG;
                        }
                        performUnifiedRestoreTask.mDidLaunch = z;
                        try {
                            this.this$1.mTransport.abortFullRestore();
                        } catch (RemoteException e4) {
                            status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        }
                        this.this$1.this$0.clearApplicationDataSynchronous(this.this$1.mCurrentPackage.packageName);
                        if (status == -1000) {
                            nextState = UnifiedRestoreState.FINAL;
                        } else {
                            nextState = UnifiedRestoreState.RUNNING_QUEUE;
                        }
                        this.this$1.executeNextState(nextState);
                        setRunning(BackupManagerService.MORE_DEBUG);
                        return;
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.OP_PENDING]);
                        IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.OP_PENDING]);
                        this.this$1.mDidLaunch = this.mEngine.getAgent() != null ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                        if (status == 0) {
                            nextState = UnifiedRestoreState.RESTORE_FINISHED;
                            this.this$1.mAgent = this.mEngine.getAgent();
                            this.this$1.mWidgetData = this.mEngine.getWidgetData();
                        } else {
                            try {
                                this.this$1.mTransport.abortFullRestore();
                            } catch (RemoteException e5) {
                                status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                            }
                            this.this$1.this$0.clearApplicationDataSynchronous(this.this$1.mCurrentPackage.packageName);
                            if (status == -1000) {
                                nextState = UnifiedRestoreState.FINAL;
                            } else {
                                nextState = UnifiedRestoreState.RUNNING_QUEUE;
                            }
                        }
                        this.this$1.executeNextState(nextState);
                        setRunning(BackupManagerService.MORE_DEBUG);
                    }
                }
                IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.OP_PENDING]);
                IoUtils.closeQuietly(this.mTransportPipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.OP_PENDING]);
                this.this$1.mDidLaunch = this.mEngine.getAgent() != null ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                if (status == 0) {
                    nextState = UnifiedRestoreState.RESTORE_FINISHED;
                    this.this$1.mAgent = this.mEngine.getAgent();
                    this.this$1.mWidgetData = this.mEngine.getWidgetData();
                } else {
                    try {
                        this.this$1.mTransport.abortFullRestore();
                    } catch (RemoteException e6) {
                        status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    }
                    this.this$1.this$0.clearApplicationDataSynchronous(this.this$1.mCurrentPackage.packageName);
                    if (status == -1000) {
                        nextState = UnifiedRestoreState.FINAL;
                    } else {
                        nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    }
                }
                this.this$1.executeNextState(nextState);
                setRunning(BackupManagerService.MORE_DEBUG);
            }

            public void execute() {
            }

            public void operationComplete(long result) {
            }

            public void handleTimeout() {
                if (BackupManagerService.DEBUG) {
                    Slog.w("StreamFeederThread", "Full-data restore target timed out; shutting down");
                }
                this.mEngineThread.handleTimeout();
                IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION]);
                this.mEnginePipes[BackupManagerService.SCHEDULE_FILE_VERSION] = null;
                IoUtils.closeQuietly(this.mEnginePipes[BackupManagerService.OP_PENDING]);
                this.mEnginePipes[BackupManagerService.OP_PENDING] = null;
            }
        }

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues;
            }
            int[] iArr = new int[UnifiedRestoreState.values().length];
            try {
                iArr[UnifiedRestoreState.FINAL.ordinal()] = BackupManagerService.SCHEDULE_FILE_VERSION;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[UnifiedRestoreState.INITIAL.ordinal()] = BackupManagerService.MSG_RUN_ADB_BACKUP;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_FINISHED.ordinal()] = BackupManagerService.MSG_RUN_RESTORE;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_FULL.ordinal()] = BackupManagerService.MSG_RUN_CLEAR;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_KEYVALUE.ordinal()] = BackupManagerService.MSG_RUN_INITIALIZE;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[UnifiedRestoreState.RUNNING_QUEUE.ordinal()] = BackupManagerService.MSG_RUN_GET_RESTORE_SETS;
            } catch (NoSuchFieldError e6) {
            }
            -com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues = iArr;
            return iArr;
        }

        PerformUnifiedRestoreTask(BackupManagerService this$0, IBackupTransport transport, IRestoreObserver observer, long restoreSetToken, PackageInfo targetPackage, int pmToken, boolean isFullSystemRestore, String[] filterSet) {
            this.this$0 = this$0;
            this.mState = UnifiedRestoreState.INITIAL;
            this.mStartRealtime = SystemClock.elapsedRealtime();
            this.mTransport = transport;
            this.mObserver = observer;
            this.mToken = restoreSetToken;
            this.mPmToken = pmToken;
            this.mTargetPackage = targetPackage;
            this.mIsSystemRestore = isFullSystemRestore;
            this.mFinished = BackupManagerService.MORE_DEBUG;
            this.mDidLaunch = BackupManagerService.MORE_DEBUG;
            if (targetPackage != null) {
                this.mAcceptSet = new ArrayList();
                this.mAcceptSet.add(targetPackage);
                return;
            }
            if (filterSet == null) {
                filterSet = packagesToNames(PackageManagerBackupAgent.getStorableApplications(this$0.mPackageManager));
                if (BackupManagerService.DEBUG) {
                    Slog.i(BackupManagerService.TAG, "Full restore; asking about " + filterSet.length + " apps");
                }
            }
            this.mAcceptSet = new ArrayList(filterSet.length);
            boolean hasSystem = BackupManagerService.MORE_DEBUG;
            boolean hasSettings = BackupManagerService.MORE_DEBUG;
            for (int i = BackupManagerService.OP_PENDING; i < filterSet.length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                try {
                    PackageInfo info = this$0.mPackageManager.getPackageInfo(filterSet[i], BackupManagerService.OP_PENDING);
                    if ("android".equals(info.packageName)) {
                        hasSystem = BackupManagerService.DEBUG_BACKUP_TRACE;
                    } else if (BackupManagerService.SETTINGS_PACKAGE.equals(info.packageName)) {
                        hasSettings = BackupManagerService.DEBUG_BACKUP_TRACE;
                    } else if (BackupManagerService.appIsEligibleForBackup(info.applicationInfo)) {
                        this.mAcceptSet.add(info);
                    }
                } catch (NameNotFoundException e) {
                }
            }
            if (hasSystem) {
                try {
                    this.mAcceptSet.add(BackupManagerService.OP_PENDING, this$0.mPackageManager.getPackageInfo("android", BackupManagerService.OP_PENDING));
                } catch (NameNotFoundException e2) {
                }
            }
            if (hasSettings) {
                try {
                    this.mAcceptSet.add(this$0.mPackageManager.getPackageInfo(BackupManagerService.SETTINGS_PACKAGE, BackupManagerService.OP_PENDING));
                } catch (NameNotFoundException e3) {
                }
            }
        }

        private String[] packagesToNames(List<PackageInfo> apps) {
            int N = apps.size();
            String[] names = new String[N];
            for (int i = BackupManagerService.OP_PENDING; i < N; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                names[i] = ((PackageInfo) apps.get(i)).packageName;
            }
            return names;
        }

        public void execute() {
            switch (-getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues()[this.mState.ordinal()]) {
                case BackupManagerService.SCHEDULE_FILE_VERSION /*1*/:
                    if (this.mFinished) {
                        Slog.e(BackupManagerService.TAG, "Duplicate finish");
                    } else {
                        finalizeRestore();
                    }
                    this.mFinished = BackupManagerService.DEBUG_BACKUP_TRACE;
                case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                    startRestore();
                case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                    restoreFinished();
                case BackupManagerService.MSG_RUN_CLEAR /*4*/:
                    restoreFull();
                case BackupManagerService.MSG_RUN_INITIALIZE /*5*/:
                    restoreKeyValue();
                case BackupManagerService.MSG_RUN_GET_RESTORE_SETS /*6*/:
                    dispatchNextRestore();
                default:
            }
        }

        private void startRestore() {
            sendStartRestore(this.mAcceptSet.size());
            if (this.mIsSystemRestore) {
                AppWidgetBackupBridge.restoreStarting(BackupManagerService.OP_PENDING);
            }
            try {
                this.mStateDir = new File(this.this$0.mBaseStateDir, this.mTransport.transportDirName());
                PackageInfo pmPackage = new PackageInfo();
                pmPackage.packageName = BackupManagerService.PACKAGE_MANAGER_SENTINEL;
                this.mAcceptSet.add(BackupManagerService.OP_PENDING, pmPackage);
                this.mStatus = this.mTransport.startRestore(this.mToken, (PackageInfo[]) this.mAcceptSet.toArray(new PackageInfo[BackupManagerService.OP_PENDING]));
                if (this.mStatus != 0) {
                    Slog.e(BackupManagerService.TAG, "Transport error " + this.mStatus + "; no restore possible");
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                    return;
                }
                RestoreDescription desc = this.mTransport.nextRestorePackage();
                if (desc == null) {
                    Slog.e(BackupManagerService.TAG, "No restore metadata available; halting");
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                } else if (BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(desc.getPackageName())) {
                    this.mCurrentPackage = new PackageInfo();
                    this.mCurrentPackage.packageName = BackupManagerService.PACKAGE_MANAGER_SENTINEL;
                    this.mPmAgent = new PackageManagerBackupAgent(this.this$0.mPackageManager, null);
                    this.mAgent = IBackupAgent.Stub.asInterface(this.mPmAgent.onBind());
                    initiateOneRestore(this.mCurrentPackage, BackupManagerService.OP_PENDING);
                    this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT);
                    if (!this.mPmAgent.hasMetadata()) {
                        Slog.e(BackupManagerService.TAG, "No restore metadata available, so not restoring");
                        Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                        objArr[BackupManagerService.OP_PENDING] = BackupManagerService.PACKAGE_MANAGER_SENTINEL;
                        objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Package manager restore metadata missing";
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                        this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_BACKUP_RESTORE_STEP, this);
                        executeNextState(UnifiedRestoreState.FINAL);
                    }
                } else {
                    Slog.e(BackupManagerService.TAG, "Required metadata but got " + desc.getPackageName());
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                }
            } catch (RemoteException e) {
                Slog.e(BackupManagerService.TAG, "Unable to contact transport for restore");
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_BACKUP_RESTORE_STEP, this);
                executeNextState(UnifiedRestoreState.FINAL);
            }
        }

        private void dispatchNextRestore() {
            Object[] objArr;
            UnifiedRestoreState nextState = UnifiedRestoreState.FINAL;
            try {
                this.mRestoreDescription = this.mTransport.nextRestorePackage();
                String packageName = this.mRestoreDescription != null ? this.mRestoreDescription.getPackageName() : null;
                if (packageName == null) {
                    Slog.e(BackupManagerService.TAG, "Failure getting next package name");
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                    nextState = UnifiedRestoreState.FINAL;
                } else if (this.mRestoreDescription == RestoreDescription.NO_MORE_PACKAGES) {
                    if (BackupManagerService.DEBUG) {
                        Slog.v(BackupManagerService.TAG, "No more packages; finishing restore");
                    }
                    int millis = (int) (SystemClock.elapsedRealtime() - this.mStartRealtime);
                    objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                    objArr[BackupManagerService.OP_PENDING] = Integer.valueOf(this.mCount);
                    objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(millis);
                    EventLog.writeEvent(EventLogTags.RESTORE_SUCCESS, objArr);
                    executeNextState(UnifiedRestoreState.FINAL);
                } else {
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Next restore package: " + this.mRestoreDescription);
                    }
                    sendOnRestorePackage(packageName);
                    Metadata metaInfo = this.mPmAgent.getRestoredMetadata(packageName);
                    if (metaInfo == null) {
                        Slog.e(BackupManagerService.TAG, "No metadata for " + packageName);
                        objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                        objArr[BackupManagerService.OP_PENDING] = packageName;
                        objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Package metadata missing";
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                        executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                        return;
                    }
                    try {
                        this.mCurrentPackage = this.this$0.mPackageManager.getPackageInfo(packageName, 64);
                        if (metaInfo.versionCode > this.mCurrentPackage.versionCode) {
                            if ((this.mCurrentPackage.applicationInfo.flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0) {
                                String message = "Version " + metaInfo.versionCode + " > installed version " + this.mCurrentPackage.versionCode;
                                Slog.w(BackupManagerService.TAG, "Package " + packageName + ": " + message);
                                objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                                objArr[BackupManagerService.OP_PENDING] = packageName;
                                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = message;
                                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                                return;
                            } else if (BackupManagerService.DEBUG) {
                                Slog.v(BackupManagerService.TAG, "Version " + metaInfo.versionCode + " > installed " + this.mCurrentPackage.versionCode + " but restoreAnyVersion");
                            }
                        }
                        this.mWidgetData = null;
                        int type = this.mRestoreDescription.getDataType();
                        if (type == BackupManagerService.SCHEDULE_FILE_VERSION) {
                            nextState = UnifiedRestoreState.RESTORE_KEYVALUE;
                        } else if (type == BackupManagerService.MSG_RUN_ADB_BACKUP) {
                            nextState = UnifiedRestoreState.RESTORE_FULL;
                        } else {
                            Slog.e(BackupManagerService.TAG, "Unrecognized restore type " + type);
                            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                            return;
                        }
                        executeNextState(nextState);
                    } catch (NameNotFoundException e) {
                        Slog.e(BackupManagerService.TAG, "Package not present: " + packageName);
                        objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                        objArr[BackupManagerService.OP_PENDING] = packageName;
                        objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Package missing on device";
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                        executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                    }
                }
            } catch (RemoteException e2) {
                Slog.e(BackupManagerService.TAG, "Can't get next target from transport; ending restore");
                EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                nextState = UnifiedRestoreState.FINAL;
            } finally {
                executeNextState(nextState);
            }
        }

        private void restoreKeyValue() {
            String packageName = this.mCurrentPackage.packageName;
            if (this.mCurrentPackage.applicationInfo.backupAgentName == null || "".equals(this.mCurrentPackage.applicationInfo.backupAgentName)) {
                Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = packageName;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Package has no agent";
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                return;
            }
            Metadata metaInfo = this.mPmAgent.getRestoredMetadata(packageName);
            if (BackupUtils.signaturesMatch(metaInfo.sigHashes, this.mCurrentPackage)) {
                this.mAgent = this.this$0.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, BackupManagerService.OP_PENDING);
                if (this.mAgent == null) {
                    Slog.w(BackupManagerService.TAG, "Can't find backup agent for " + packageName);
                    objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                    objArr[BackupManagerService.OP_PENDING] = packageName;
                    objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Restore agent missing";
                    EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                    return;
                }
                this.mDidLaunch = BackupManagerService.DEBUG_BACKUP_TRACE;
                try {
                    initiateOneRestore(this.mCurrentPackage, metaInfo.versionCode);
                    this.mCount += BackupManagerService.SCHEDULE_FILE_VERSION;
                } catch (Exception e) {
                    Slog.e(BackupManagerService.TAG, "Error when attempting restore: " + e.toString());
                    keyValueAgentErrorCleanup();
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                }
                return;
            }
            Slog.w(BackupManagerService.TAG, "Signature mismatch restoring " + packageName);
            objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
            objArr[BackupManagerService.OP_PENDING] = packageName;
            objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "Signature mismatch";
            EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }

        void initiateOneRestore(PackageInfo app, int appVersionCode) {
            String packageName = app.packageName;
            if (BackupManagerService.DEBUG) {
                Slog.d(BackupManagerService.TAG, "initiateOneRestore packageName=" + packageName);
            }
            this.mBackupDataName = new File(this.this$0.mDataDir, packageName + ".restore");
            this.mStageName = new File(this.this$0.mDataDir, packageName + ".stage");
            this.mNewStateName = new File(this.mStateDir, packageName + ".new");
            this.mSavedStateName = new File(this.mStateDir, packageName);
            boolean staging = packageName.equals("android") ? BackupManagerService.MORE_DEBUG : BackupManagerService.DEBUG_BACKUP_TRACE;
            File downloadFile = staging ? this.mStageName : this.mBackupDataName;
            int token = this.this$0.generateToken();
            try {
                ParcelFileDescriptor stage = ParcelFileDescriptor.open(downloadFile, 1006632960);
                if (this.mTransport.getRestoreData(stage) != 0) {
                    Slog.e(BackupManagerService.TAG, "Error getting restore data for " + packageName);
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[BackupManagerService.OP_PENDING]);
                    stage.close();
                    downloadFile.delete();
                    executeNextState(UnifiedRestoreState.FINAL);
                    return;
                }
                if (staging) {
                    stage.close();
                    stage = ParcelFileDescriptor.open(downloadFile, 268435456);
                    this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
                    BackupDataInput in = new BackupDataInput(stage.getFileDescriptor());
                    BackupDataOutput out = new BackupDataOutput(this.mBackupData.getFileDescriptor());
                    byte[] buffer = new byte[DumpState.DUMP_PREFERRED_XML];
                    while (in.readNextHeader()) {
                        String key = in.getKey();
                        int size = in.getDataSize();
                        if (key.equals(BackupManagerService.KEY_WIDGET_STATE)) {
                            if (BackupManagerService.DEBUG) {
                                Slog.i(BackupManagerService.TAG, "Restoring widget state for " + packageName);
                            }
                            this.mWidgetData = new byte[size];
                            in.readEntityData(this.mWidgetData, BackupManagerService.OP_PENDING, size);
                        } else {
                            if (size > buffer.length) {
                                buffer = new byte[size];
                            }
                            in.readEntityData(buffer, BackupManagerService.OP_PENDING, size);
                            out.writeEntityHeader(key, size);
                            out.writeEntityData(buffer, size);
                        }
                    }
                    this.mBackupData.close();
                }
                stage.close();
                this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_RESTORE_INTERVAL, this);
                this.mAgent.doRestore(this.mBackupData, appVersionCode, this.mNewState, token, this.this$0.mBackupManagerBinder);
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Unable to call app for restore: " + packageName, e);
                Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = packageName;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = e.toString();
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                keyValueAgentErrorCleanup();
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            }
        }

        private void restoreFull() {
            try {
                new Thread(new StreamFeederThread(this), "unified-stream-feeder").start();
            } catch (IOException e) {
                Slog.e(BackupManagerService.TAG, "Unable to construct pipes for stream restore!");
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            }
        }

        private void restoreFinished() {
            try {
                int token = this.this$0.generateToken();
                this.this$0.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_RESTORE_FINISHED_INTERVAL, this);
                this.mAgent.doRestoreFinished(token, this.this$0.mBackupManagerBinder);
            } catch (Exception e) {
                String packageName = this.mCurrentPackage.packageName;
                Slog.e(BackupManagerService.TAG, "Unable to finalize restore of " + packageName);
                Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = packageName;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = e.toString();
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
                keyValueAgentErrorCleanup();
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            }
        }

        private void finalizeRestore() {
            try {
                this.mTransport.finishRestore();
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Error finishing restore", e);
            }
            if (this.mObserver != null) {
                try {
                    this.mObserver.restoreFinished(this.mStatus);
                } catch (RemoteException e2) {
                    Slog.d(BackupManagerService.TAG, "Restore observer died at restoreFinished");
                }
            }
            this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_RESTORE_TIMEOUT);
            if (this.mPmToken > 0) {
                try {
                    this.this$0.mPackageManagerBinder.finishPackageInstall(this.mPmToken, this.mDidLaunch);
                } catch (RemoteException e3) {
                }
            } else {
                this.this$0.mBackupHandler.sendEmptyMessageDelayed(BackupManagerService.MSG_RESTORE_TIMEOUT, BackupManagerService.TIMEOUT_RESTORE_INTERVAL);
            }
            AppWidgetBackupBridge.restoreFinished(BackupManagerService.OP_PENDING);
            if (this.mIsSystemRestore && this.mPmAgent != null) {
                this.this$0.mAncestralPackages = this.mPmAgent.getRestoredPackages();
                this.this$0.mAncestralToken = this.mToken;
                this.this$0.writeRestoreTokens();
            }
            Slog.i(BackupManagerService.TAG, "Restore complete.");
            this.this$0.mWakelock.release();
        }

        void keyValueAgentErrorCleanup() {
            this.this$0.clearApplicationDataSynchronous(this.mCurrentPackage.packageName);
            keyValueAgentCleanup();
        }

        void keyValueAgentCleanup() {
            this.mBackupDataName.delete();
            this.mStageName.delete();
            try {
                if (this.mBackupData != null) {
                    this.mBackupData.close();
                }
            } catch (IOException e) {
            }
            try {
                if (this.mNewState != null) {
                    this.mNewState.close();
                }
            } catch (IOException e2) {
            }
            this.mNewState = null;
            this.mBackupData = null;
            this.mNewStateName.delete();
            if (this.mCurrentPackage.applicationInfo != null) {
                try {
                    this.this$0.mActivityManager.unbindBackupAgent(this.mCurrentPackage.applicationInfo);
                    boolean killAfterRestore = this.mCurrentPackage.applicationInfo.uid >= BackupManagerService.PBKDF2_HASH_ROUNDS ? this.mRestoreDescription.getDataType() != BackupManagerService.MSG_RUN_ADB_BACKUP ? (DumpState.DUMP_INSTALLS & this.mCurrentPackage.applicationInfo.flags) != 0 ? BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG : BackupManagerService.DEBUG_BACKUP_TRACE : BackupManagerService.MORE_DEBUG;
                    if (this.mTargetPackage == null && killAfterRestore) {
                        if (BackupManagerService.DEBUG) {
                            Slog.d(BackupManagerService.TAG, "Restore complete, killing host process of " + this.mCurrentPackage.applicationInfo.processName);
                        }
                        this.this$0.mActivityManager.killApplicationProcess(this.mCurrentPackage.applicationInfo.processName, this.mCurrentPackage.applicationInfo.uid);
                    }
                } catch (RemoteException e3) {
                }
            }
            this.this$0.mBackupHandler.removeMessages(BackupManagerService.MSG_TIMEOUT, this);
            synchronized (this.this$0.mCurrentOpLock) {
                this.this$0.mCurrentOperations.clear();
            }
        }

        public void operationComplete(long unusedResult) {
            UnifiedRestoreState nextState;
            switch (-getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues()[this.mState.ordinal()]) {
                case BackupManagerService.MSG_RUN_ADB_BACKUP /*2*/:
                    nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    break;
                case BackupManagerService.MSG_RUN_RESTORE /*3*/:
                    int size = (int) this.mBackupDataName.length();
                    Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                    objArr[BackupManagerService.OP_PENDING] = this.mCurrentPackage.packageName;
                    objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(size);
                    EventLog.writeEvent(EventLogTags.RESTORE_PACKAGE, objArr);
                    keyValueAgentCleanup();
                    if (this.mWidgetData != null) {
                        this.this$0.restoreWidgetData(this.mCurrentPackage.packageName, this.mWidgetData);
                    }
                    nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    break;
                case BackupManagerService.MSG_RUN_CLEAR /*4*/:
                case BackupManagerService.MSG_RUN_INITIALIZE /*5*/:
                    nextState = UnifiedRestoreState.RESTORE_FINISHED;
                    break;
                default:
                    Slog.e(BackupManagerService.TAG, "Unexpected restore callback into state " + this.mState);
                    keyValueAgentErrorCleanup();
                    nextState = UnifiedRestoreState.FINAL;
                    break;
            }
            executeNextState(nextState);
        }

        public void handleTimeout() {
            Slog.e(BackupManagerService.TAG, "Timeout restoring application " + this.mCurrentPackage.packageName);
            Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
            objArr[BackupManagerService.OP_PENDING] = this.mCurrentPackage.packageName;
            objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = "restore timeout";
            EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, objArr);
            keyValueAgentErrorCleanup();
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }

        void executeNextState(UnifiedRestoreState nextState) {
            this.mState = nextState;
            this.this$0.mBackupHandler.sendMessage(this.this$0.mBackupHandler.obtainMessage(BackupManagerService.MSG_BACKUP_RESTORE_STEP, this));
        }

        void sendStartRestore(int numPackages) {
            if (this.mObserver != null) {
                try {
                    this.mObserver.restoreStarting(numPackages);
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "Restore observer went away: startRestore");
                    this.mObserver = null;
                }
            }
        }

        void sendOnRestorePackage(String name) {
            if (this.mObserver != null && this.mObserver != null) {
                try {
                    this.mObserver.onUpdate(this.mCount, name);
                } catch (RemoteException e) {
                    Slog.d(BackupManagerService.TAG, "Restore observer died in onUpdate");
                    this.mObserver = null;
                }
            }
        }

        void sendEndRestore() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.restoreFinished(this.mStatus);
                } catch (RemoteException e) {
                    Slog.w(BackupManagerService.TAG, "Restore observer went away: endRestore");
                    this.mObserver = null;
                }
            }
        }
    }

    class ProvisionedObserver extends ContentObserver {
        final /* synthetic */ BackupManagerService this$0;

        public ProvisionedObserver(BackupManagerService this$0, Handler handler) {
            this.this$0 = this$0;
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean wasProvisioned = this.this$0.mProvisioned;
            boolean isProvisioned = this.this$0.deviceIsProvisioned();
            BackupManagerService backupManagerService = this.this$0;
            if (wasProvisioned) {
                isProvisioned = BackupManagerService.DEBUG_BACKUP_TRACE;
            }
            backupManagerService.mProvisioned = isProvisioned;
            synchronized (this.this$0.mQueueLock) {
                if (this.this$0.mProvisioned && !wasProvisioned) {
                    if (this.this$0.mEnabled) {
                        KeyValueBackupJob.schedule(this.this$0.mContext);
                        this.this$0.scheduleNextFullBackupJob(0);
                    }
                }
            }
        }
    }

    class RestoreGetSetsParams {
        public IRestoreObserver observer;
        public ActiveRestoreSession session;
        final /* synthetic */ BackupManagerService this$0;
        public IBackupTransport transport;

        RestoreGetSetsParams(BackupManagerService this$0, IBackupTransport _transport, ActiveRestoreSession _session, IRestoreObserver _observer) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.session = _session;
            this.observer = _observer;
        }
    }

    class RestoreParams {
        public String dirName;
        public String[] filterSet;
        public boolean isSystemRestore;
        public IRestoreObserver observer;
        public PackageInfo pkgInfo;
        public int pmToken;
        final /* synthetic */ BackupManagerService this$0;
        public long token;
        public IBackupTransport transport;

        RestoreParams(BackupManagerService this$0, IBackupTransport _transport, String _dirName, IRestoreObserver _obs, long _token, PackageInfo _pkg) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.token = _token;
            this.pkgInfo = _pkg;
            this.pmToken = BackupManagerService.OP_PENDING;
            this.isSystemRestore = BackupManagerService.MORE_DEBUG;
            this.filterSet = null;
        }

        RestoreParams(BackupManagerService this$0, IBackupTransport _transport, String _dirName, IRestoreObserver _obs, long _token, String _pkgName, int _pmToken) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = _pmToken;
            this.isSystemRestore = BackupManagerService.MORE_DEBUG;
            String[] strArr = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
            strArr[BackupManagerService.OP_PENDING] = _pkgName;
            this.filterSet = strArr;
        }

        RestoreParams(BackupManagerService this$0, IBackupTransport _transport, String _dirName, IRestoreObserver _obs, long _token) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = BackupManagerService.OP_PENDING;
            this.isSystemRestore = BackupManagerService.DEBUG_BACKUP_TRACE;
            this.filterSet = null;
        }

        RestoreParams(BackupManagerService this$0, IBackupTransport _transport, String _dirName, IRestoreObserver _obs, long _token, String[] _filterSet, boolean _isSystemRestore) {
            this.this$0 = this$0;
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = BackupManagerService.OP_PENDING;
            this.isSystemRestore = _isSystemRestore;
            this.filterSet = _filterSet;
        }
    }

    enum RestorePolicy {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.BackupManagerService.RestorePolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.BackupManagerService.RestorePolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.BackupManagerService.RestorePolicy.<clinit>():void");
        }
    }

    private class RunBackupReceiver extends BroadcastReceiver {
        final /* synthetic */ BackupManagerService this$0;

        /* synthetic */ RunBackupReceiver(BackupManagerService this$0, RunBackupReceiver runBackupReceiver) {
            this(this$0);
        }

        private RunBackupReceiver(BackupManagerService this$0) {
            this.this$0 = this$0;
        }

        public void onReceive(Context context, Intent intent) {
            if (BackupManagerService.RUN_BACKUP_ACTION.equals(intent.getAction())) {
                synchronized (this.this$0.mQueueLock) {
                    if (this.this$0.mPendingInits.size() > 0) {
                        try {
                            this.this$0.mAlarmManager.cancel(this.this$0.mRunInitIntent);
                            this.this$0.mRunInitIntent.send();
                        } catch (CanceledException e) {
                            Slog.e(BackupManagerService.TAG, "Run init intent cancelled");
                        }
                    } else if (!this.this$0.mEnabled || !this.this$0.mProvisioned) {
                        Slog.w(BackupManagerService.TAG, "Backup pass but e=" + this.this$0.mEnabled + " p=" + this.this$0.mProvisioned);
                    } else if (this.this$0.mBackupRunning) {
                        Slog.i(BackupManagerService.TAG, "Backup time but one already running");
                    } else {
                        if (BackupManagerService.DEBUG) {
                            Slog.v(BackupManagerService.TAG, "Running a backup pass");
                        }
                        this.this$0.mBackupRunning = BackupManagerService.DEBUG_BACKUP_TRACE;
                        this.this$0.mWakelock.acquire();
                        this.this$0.mBackupHandler.sendMessage(this.this$0.mBackupHandler.obtainMessage(BackupManagerService.SCHEDULE_FILE_VERSION));
                    }
                }
            }
        }
    }

    private class RunInitializeReceiver extends BroadcastReceiver {
        final /* synthetic */ BackupManagerService this$0;

        /* synthetic */ RunInitializeReceiver(BackupManagerService this$0, RunInitializeReceiver runInitializeReceiver) {
            this(this$0);
        }

        private RunInitializeReceiver(BackupManagerService this$0) {
            this.this$0 = this$0;
        }

        public void onReceive(Context context, Intent intent) {
            if (BackupManagerService.RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
                synchronized (this.this$0.mQueueLock) {
                    if (BackupManagerService.DEBUG) {
                        Slog.v(BackupManagerService.TAG, "Running a device init");
                    }
                    this.this$0.mWakelock.acquire();
                    this.this$0.mBackupHandler.sendMessage(this.this$0.mBackupHandler.obtainMessage(BackupManagerService.MSG_RUN_INITIALIZE));
                }
            }
        }
    }

    class TransportConnection implements ServiceConnection {
        ServiceInfo mTransport;
        final /* synthetic */ BackupManagerService this$0;

        public TransportConnection(BackupManagerService this$0, ServiceInfo transport) {
            this.this$0 = this$0;
            this.mTransport = transport;
        }

        public void onServiceConnected(ComponentName component, IBinder service) {
            Object[] objArr;
            if (BackupManagerService.DEBUG) {
                Slog.v(BackupManagerService.TAG, "Connected to transport " + component);
            }
            String name = component.flattenToShortString();
            try {
                IBackupTransport transport = IBackupTransport.Stub.asInterface(service);
                this.this$0.registerTransport(transport.name(), name, transport);
                objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = name;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(BackupManagerService.SCHEDULE_FILE_VERSION);
                EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, objArr);
            } catch (RemoteException e) {
                Slog.e(BackupManagerService.TAG, "Unable to register transport " + component);
                objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
                objArr[BackupManagerService.OP_PENDING] = name;
                objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(BackupManagerService.OP_PENDING);
                EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, objArr);
            }
        }

        public void onServiceDisconnected(ComponentName component) {
            if (BackupManagerService.DEBUG) {
                Slog.v(BackupManagerService.TAG, "Disconnected from transport " + component);
            }
            String name = component.flattenToShortString();
            Object[] objArr = new Object[BackupManagerService.MSG_RUN_ADB_BACKUP];
            objArr[BackupManagerService.OP_PENDING] = name;
            objArr[BackupManagerService.SCHEDULE_FILE_VERSION] = Integer.valueOf(BackupManagerService.OP_PENDING);
            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_LIFECYCLE, objArr);
            this.this$0.registerTransport(null, name, null);
        }
    }

    enum UnifiedRestoreState {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.BackupManagerService.UnifiedRestoreState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.BackupManagerService.UnifiedRestoreState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.BackupManagerService.UnifiedRestoreState.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.BackupManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.BackupManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.BackupManagerService.<clinit>():void");
    }

    static Trampoline getInstance() {
        return sInstance;
    }

    int generateToken() {
        int token;
        do {
            synchronized (this.mTokenGenerator) {
                token = this.mTokenGenerator.nextInt();
            }
        } while (token < 0);
        return token;
    }

    public static boolean appIsEligibleForBackup(ApplicationInfo app) {
        if ((app.flags & DumpState.DUMP_VERSION) == 0) {
            return MORE_DEBUG;
        }
        if ((app.uid >= PBKDF2_HASH_ROUNDS || app.backupAgentName != null) && !app.packageName.equals(SHARED_BACKUP_AGENT_PACKAGE)) {
            return DEBUG_BACKUP_TRACE;
        }
        return MORE_DEBUG;
    }

    private static boolean appIsStopped(ApplicationInfo app) {
        return (app.flags & 2097152) != 0 ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
    }

    private static boolean appGetsFullBackup(PackageInfo pkg) {
        boolean z = DEBUG_BACKUP_TRACE;
        if (pkg.applicationInfo.backupAgentName == null) {
            return DEBUG_BACKUP_TRACE;
        }
        if ((pkg.applicationInfo.flags & 67108864) == 0) {
            z = MORE_DEBUG;
        }
        return z;
    }

    private static boolean appIsKeyValueOnly(PackageInfo pkg) {
        boolean z = MORE_DEBUG;
        if (SETTINGS_PACKAGE.equals(pkg.packageName)) {
            return MORE_DEBUG;
        }
        if (!appGetsFullBackup(pkg)) {
            z = DEBUG_BACKUP_TRACE;
        }
        return z;
    }

    void addBackupTrace(String s) {
        synchronized (this.mBackupTrace) {
            this.mBackupTrace.add(s);
        }
    }

    void clearBackupTrace() {
        synchronized (this.mBackupTrace) {
            this.mBackupTrace.clear();
        }
    }

    public BackupManagerService(Context context, Trampoline parent) {
        FileInputStream fileInputStream;
        DataInputStream dataInputStream;
        FileInputStream fin;
        DataInputStream in;
        byte[] salt;
        IntentFilter filter;
        Intent backupIntent;
        Intent initIntent;
        String transport;
        List<ResolveInfo> hosts;
        String str;
        StringBuilder append;
        String str2;
        int i;
        Throwable th;
        this.mBackupParticipants = new SparseArray();
        this.mPendingBackups = new HashMap();
        this.mQueueLock = new Object();
        this.mAgentConnectLock = new Object();
        this.mBackupTrace = new ArrayList();
        this.mClearDataLock = new Object();
        this.mTransportServiceIntent = new Intent(SERVICE_ACTION_TRANSPORT_HOST);
        this.mTransportNames = new ArrayMap();
        this.mTransports = new ArrayMap();
        this.mTransportConnections = new ArrayMap();
        this.mCurrentOperations = new SparseArray();
        this.mCurrentOpLock = new Object();
        this.mTokenGenerator = new Random();
        this.mFullConfirmations = new SparseArray();
        this.mRng = new SecureRandom();
        this.mEverStoredApps = new HashSet();
        this.mAncestralPackages = null;
        this.mAncestralToken = 0;
        this.mCurrentToken = 0;
        this.mPendingInits = new HashSet();
        this.mFullBackupScheduleWriter = new Runnable() {
            public void run() {
                synchronized (BackupManagerService.this.mQueueLock) {
                    try {
                        ByteArrayOutputStream bufStream = new ByteArrayOutputStream(DumpState.DUMP_PREFERRED);
                        DataOutputStream bufOut = new DataOutputStream(bufStream);
                        bufOut.writeInt(BackupManagerService.SCHEDULE_FILE_VERSION);
                        int N = BackupManagerService.this.mFullBackupQueue.size();
                        bufOut.writeInt(N);
                        for (int i = BackupManagerService.OP_PENDING; i < N; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
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
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                PackageInfo app;
                TransportConnection conn;
                String action = intent.getAction();
                boolean replacing = BackupManagerService.MORE_DEBUG;
                boolean z = BackupManagerService.MORE_DEBUG;
                Bundle extras = intent.getExtras();
                String[] strArr = null;
                if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
                    if (!"android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        if (!"android.intent.action.PACKAGE_CHANGED".equals(action)) {
                            if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                                z = BackupManagerService.DEBUG_BACKUP_TRACE;
                                strArr = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                            } else {
                                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                                    z = BackupManagerService.MORE_DEBUG;
                                    strArr = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                                }
                            }
                            if (strArr == null && strArr.length != 0) {
                                int uid = extras.getInt("android.intent.extra.UID");
                                if (z) {
                                    synchronized (BackupManagerService.this.mBackupParticipants) {
                                        if (replacing) {
                                            BackupManagerService.this.removePackageParticipantsLocked(strArr, uid);
                                        }
                                        BackupManagerService.this.addPackageParticipantsLocked(strArr);
                                    }
                                    long now = System.currentTimeMillis();
                                    int length = strArr.length;
                                    for (int i = BackupManagerService.OP_PENDING; i < length; i += BackupManagerService.SCHEDULE_FILE_VERSION) {
                                        String packageName = strArr[i];
                                        app = BackupManagerService.this.mPackageManager.getPackageInfo(packageName, BackupManagerService.OP_PENDING);
                                        if (BackupManagerService.appGetsFullBackup(app)) {
                                            if (BackupManagerService.appIsEligibleForBackup(app.applicationInfo)) {
                                                BackupManagerService.this.enqueueFullBackup(packageName, now);
                                                BackupManagerService.this.scheduleNextFullBackupJob(0);
                                                synchronized (BackupManagerService.this.mTransports) {
                                                    conn = (TransportConnection) BackupManagerService.this.mTransportConnections.get(packageName);
                                                    if (conn == null) {
                                                        BackupManagerService.this.bindTransport(conn.mTransport);
                                                    } else {
                                                        BackupManagerService.this.checkForTransportAndBind(app);
                                                    }
                                                }
                                            }
                                        }
                                        try {
                                            synchronized (BackupManagerService.this.mQueueLock) {
                                                BackupManagerService.this.dequeueFullBackupLocked(packageName);
                                            }
                                            BackupManagerService.this.writeFullBackupScheduleAsync();
                                            synchronized (BackupManagerService.this.mTransports) {
                                                conn = (TransportConnection) BackupManagerService.this.mTransportConnections.get(packageName);
                                                if (conn == null) {
                                                    BackupManagerService.this.checkForTransportAndBind(app);
                                                } else {
                                                    BackupManagerService.this.bindTransport(conn.mTransport);
                                                }
                                            }
                                        } catch (NameNotFoundException e) {
                                            if (BackupManagerService.DEBUG) {
                                                Slog.w(BackupManagerService.TAG, "Can't resolve new app " + packageName);
                                            }
                                        }
                                    }
                                    BackupManagerService.this.dataChangedImpl(BackupManagerService.PACKAGE_MANAGER_SENTINEL);
                                } else if (!replacing) {
                                    synchronized (BackupManagerService.this.mBackupParticipants) {
                                        BackupManagerService.this.removePackageParticipantsLocked(strArr, uid);
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
                Uri uri = intent.getData();
                if (uri != null) {
                    String pkgName = uri.getSchemeSpecificPart();
                    if (pkgName != null) {
                        strArr = new String[BackupManagerService.SCHEDULE_FILE_VERSION];
                        strArr[BackupManagerService.OP_PENDING] = pkgName;
                    }
                    if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                        try {
                            String[] components = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                            boolean tryBind = BackupManagerService.DEBUG_BACKUP_TRACE;
                            synchronized (BackupManagerService.this.mTransports) {
                                conn = (TransportConnection) BackupManagerService.this.mTransportConnections.get(pkgName);
                                if (conn != null) {
                                    ServiceInfo svc = conn.mTransport;
                                    ComponentName componentName = new ComponentName(svc.packageName, svc.name);
                                    if (svc.packageName.equals(pkgName)) {
                                        String className = componentName.getClassName();
                                        boolean isTransport = BackupManagerService.MORE_DEBUG;
                                        int i2 = BackupManagerService.OP_PENDING;
                                        while (true) {
                                            int length2 = components.length;
                                            if (i2 >= r0) {
                                                break;
                                            }
                                            if (className.equals(components[i2])) {
                                                break;
                                            }
                                            i2 += BackupManagerService.SCHEDULE_FILE_VERSION;
                                        }
                                        if (!isTransport) {
                                            tryBind = BackupManagerService.MORE_DEBUG;
                                        }
                                    }
                                }
                            }
                            if (tryBind) {
                                app = BackupManagerService.this.mPackageManager.getPackageInfo(pkgName, BackupManagerService.OP_PENDING);
                                BackupManagerService.this.checkForTransportAndBind(app);
                            }
                        } catch (NameNotFoundException e2) {
                        }
                        return;
                    }
                    z = "android.intent.action.PACKAGE_ADDED".equals(action);
                    replacing = extras.getBoolean("android.intent.extra.REPLACING", BackupManagerService.MORE_DEBUG);
                    if (strArr == null) {
                    }
                }
            }
        };
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mPackageManagerBinder = AppGlobals.getPackageManager();
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        this.mBackupManagerBinder = Trampoline.asInterface(parent.asBinder());
        this.mHandlerThread = new HandlerThread("backup", MSG_RUN_ADB_RESTORE);
        this.mHandlerThread.start();
        this.mBackupHandler = new BackupHandler(this.mHandlerThread.getLooper());
        ContentResolver resolver = context.getContentResolver();
        this.mProvisioned = Global.getInt(resolver, "device_provisioned", OP_PENDING) != 0 ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
        this.mAutoRestore = Secure.getInt(resolver, "backup_auto_restore", SCHEDULE_FILE_VERSION) != 0 ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
        this.mProvisionedObserver = new ProvisionedObserver(this, this.mBackupHandler);
        resolver.registerContentObserver(Global.getUriFor("device_provisioned"), MORE_DEBUG, this.mProvisionedObserver);
        this.mBaseStateDir = new File(Environment.getDataDirectory(), "backup");
        this.mBaseStateDir.mkdirs();
        if (!SELinux.restorecon(this.mBaseStateDir)) {
            Slog.e(TAG, "SELinux restorecon failed on " + this.mBaseStateDir);
        }
        this.mDataDir = new File(Environment.getDownloadCacheDirectory(), "backup_stage");
        this.mPasswordVersion = SCHEDULE_FILE_VERSION;
        this.mPasswordVersionFile = new File(this.mBaseStateDir, "pwversion");
        if (this.mPasswordVersionFile.exists()) {
            fileInputStream = null;
            dataInputStream = null;
            try {
                fin = new FileInputStream(this.mPasswordVersionFile);
                try {
                    in = new DataInputStream(fin);
                    try {
                        this.mPasswordVersion = in.readInt();
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                Slog.w(TAG, "Error closing pw version files");
                            }
                        }
                        if (fin != null) {
                            fin.close();
                        }
                    } catch (IOException e2) {
                        dataInputStream = in;
                        fileInputStream = fin;
                        try {
                            Slog.e(TAG, "Unable to read backup pw version");
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e3) {
                                    Slog.w(TAG, "Error closing pw version files");
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                            if (this.mPasswordHashFile.exists()) {
                                fileInputStream = null;
                                dataInputStream = null;
                                try {
                                    fin = new FileInputStream(this.mPasswordHashFile);
                                    try {
                                        in = new DataInputStream(new BufferedInputStream(fin));
                                        try {
                                            salt = new byte[in.readInt()];
                                            in.readFully(salt);
                                            this.mPasswordHash = in.readUTF();
                                            this.mPasswordSalt = salt;
                                            if (in != null) {
                                                try {
                                                    in.close();
                                                } catch (IOException e4) {
                                                    Slog.w(TAG, "Unable to close streams");
                                                }
                                            }
                                            if (fin != null) {
                                                fin.close();
                                            }
                                        } catch (IOException e5) {
                                            dataInputStream = in;
                                            fileInputStream = fin;
                                            try {
                                                Slog.e(TAG, "Unable to read saved backup pw hash");
                                                if (dataInputStream != null) {
                                                    try {
                                                        dataInputStream.close();
                                                    } catch (IOException e6) {
                                                        Slog.w(TAG, "Unable to close streams");
                                                    }
                                                }
                                                if (fileInputStream != null) {
                                                    fileInputStream.close();
                                                }
                                                this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                                                filter = new IntentFilter();
                                                filter.addAction(RUN_BACKUP_ACTION);
                                                context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                                                this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                                                filter = new IntentFilter();
                                                filter.addAction(RUN_INITIALIZE_ACTION);
                                                context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                                                backupIntent = new Intent(RUN_BACKUP_ACTION);
                                                backupIntent.addFlags(1073741824);
                                                this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                                                initIntent = new Intent(RUN_INITIALIZE_ACTION);
                                                backupIntent.addFlags(1073741824);
                                                this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                                                this.mJournalDir = new File(this.mBaseStateDir, "pending");
                                                this.mJournalDir.mkdirs();
                                                this.mJournal = null;
                                                this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                                                initPackageTracking();
                                                synchronized (this.mBackupParticipants) {
                                                    addPackageParticipantsLocked(null);
                                                }
                                                this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                                                transport = Secure.getString(context.getContentResolver(), "backup_transport");
                                                if (TextUtils.isEmpty(transport)) {
                                                    transport = null;
                                                }
                                                this.mCurrentTransport = transport;
                                                if (DEBUG) {
                                                    Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                                                }
                                                hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                                                if (DEBUG) {
                                                    str = TAG;
                                                    append = new StringBuilder().append("Found transports: ");
                                                    if (hosts == null) {
                                                        str2 = "null";
                                                    } else {
                                                        str2 = Integer.valueOf(hosts.size());
                                                    }
                                                    Slog.v(str, append.append(str2).toString());
                                                }
                                                if (hosts != null) {
                                                    for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                                                        tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                                                    }
                                                }
                                                parseLeftoverJournals();
                                                this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
                                            } catch (Throwable th2) {
                                                th = th2;
                                                if (dataInputStream != null) {
                                                    try {
                                                        dataInputStream.close();
                                                    } catch (IOException e7) {
                                                        Slog.w(TAG, "Unable to close streams");
                                                        throw th;
                                                    }
                                                }
                                                if (fileInputStream != null) {
                                                    fileInputStream.close();
                                                }
                                                throw th;
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            dataInputStream = in;
                                            fileInputStream = fin;
                                            if (dataInputStream != null) {
                                                dataInputStream.close();
                                            }
                                            if (fileInputStream != null) {
                                                fileInputStream.close();
                                            }
                                            throw th;
                                        }
                                    } catch (IOException e8) {
                                        fileInputStream = fin;
                                        Slog.e(TAG, "Unable to read saved backup pw hash");
                                        if (dataInputStream != null) {
                                            dataInputStream.close();
                                        }
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                                        filter = new IntentFilter();
                                        filter.addAction(RUN_BACKUP_ACTION);
                                        context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                                        this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                                        filter = new IntentFilter();
                                        filter.addAction(RUN_INITIALIZE_ACTION);
                                        context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                                        backupIntent = new Intent(RUN_BACKUP_ACTION);
                                        backupIntent.addFlags(1073741824);
                                        this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                                        initIntent = new Intent(RUN_INITIALIZE_ACTION);
                                        backupIntent.addFlags(1073741824);
                                        this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                                        this.mJournalDir = new File(this.mBaseStateDir, "pending");
                                        this.mJournalDir.mkdirs();
                                        this.mJournal = null;
                                        this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                                        initPackageTracking();
                                        synchronized (this.mBackupParticipants) {
                                            addPackageParticipantsLocked(null);
                                        }
                                        this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                                        transport = Secure.getString(context.getContentResolver(), "backup_transport");
                                        if (TextUtils.isEmpty(transport)) {
                                            transport = null;
                                        }
                                        this.mCurrentTransport = transport;
                                        if (DEBUG) {
                                            Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                                        }
                                        hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                                        if (DEBUG) {
                                            str = TAG;
                                            append = new StringBuilder().append("Found transports: ");
                                            if (hosts == null) {
                                                str2 = Integer.valueOf(hosts.size());
                                            } else {
                                                str2 = "null";
                                            }
                                            Slog.v(str, append.append(str2).toString());
                                        }
                                        if (hosts != null) {
                                            for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                                                tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                                            }
                                        }
                                        parseLeftoverJournals();
                                        this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
                                    } catch (Throwable th4) {
                                        th = th4;
                                        fileInputStream = fin;
                                        if (dataInputStream != null) {
                                            dataInputStream.close();
                                        }
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        throw th;
                                    }
                                } catch (IOException e9) {
                                    Slog.e(TAG, "Unable to read saved backup pw hash");
                                    if (dataInputStream != null) {
                                        dataInputStream.close();
                                    }
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                                    filter = new IntentFilter();
                                    filter.addAction(RUN_BACKUP_ACTION);
                                    context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                                    this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                                    filter = new IntentFilter();
                                    filter.addAction(RUN_INITIALIZE_ACTION);
                                    context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                                    backupIntent = new Intent(RUN_BACKUP_ACTION);
                                    backupIntent.addFlags(1073741824);
                                    this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                                    initIntent = new Intent(RUN_INITIALIZE_ACTION);
                                    backupIntent.addFlags(1073741824);
                                    this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                                    this.mJournalDir = new File(this.mBaseStateDir, "pending");
                                    this.mJournalDir.mkdirs();
                                    this.mJournal = null;
                                    this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                                    initPackageTracking();
                                    synchronized (this.mBackupParticipants) {
                                        addPackageParticipantsLocked(null);
                                    }
                                    this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                                    transport = Secure.getString(context.getContentResolver(), "backup_transport");
                                    if (TextUtils.isEmpty(transport)) {
                                        transport = null;
                                    }
                                    this.mCurrentTransport = transport;
                                    if (DEBUG) {
                                        Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                                    }
                                    hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                                    if (DEBUG) {
                                        str = TAG;
                                        append = new StringBuilder().append("Found transports: ");
                                        if (hosts == null) {
                                            str2 = Integer.valueOf(hosts.size());
                                        } else {
                                            str2 = "null";
                                        }
                                        Slog.v(str, append.append(str2).toString());
                                    }
                                    if (hosts != null) {
                                        for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                                            tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                                        }
                                    }
                                    parseLeftoverJournals();
                                    this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
                                }
                            }
                            this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                            filter = new IntentFilter();
                            filter.addAction(RUN_BACKUP_ACTION);
                            context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                            this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                            filter = new IntentFilter();
                            filter.addAction(RUN_INITIALIZE_ACTION);
                            context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                            backupIntent = new Intent(RUN_BACKUP_ACTION);
                            backupIntent.addFlags(1073741824);
                            this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                            initIntent = new Intent(RUN_INITIALIZE_ACTION);
                            backupIntent.addFlags(1073741824);
                            this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                            this.mJournalDir = new File(this.mBaseStateDir, "pending");
                            this.mJournalDir.mkdirs();
                            this.mJournal = null;
                            this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                            initPackageTracking();
                            synchronized (this.mBackupParticipants) {
                                addPackageParticipantsLocked(null);
                            }
                            this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                            transport = Secure.getString(context.getContentResolver(), "backup_transport");
                            if (TextUtils.isEmpty(transport)) {
                                transport = null;
                            }
                            this.mCurrentTransport = transport;
                            if (DEBUG) {
                                Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                            }
                            hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                            if (DEBUG) {
                                str = TAG;
                                append = new StringBuilder().append("Found transports: ");
                                if (hosts == null) {
                                    str2 = "null";
                                } else {
                                    str2 = Integer.valueOf(hosts.size());
                                }
                                Slog.v(str, append.append(str2).toString());
                            }
                            if (hosts != null) {
                                for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                                    tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                                }
                            }
                            parseLeftoverJournals();
                            this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
                        } catch (Throwable th5) {
                            th = th5;
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e10) {
                                    Slog.w(TAG, "Error closing pw version files");
                                    throw th;
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        dataInputStream = in;
                        fileInputStream = fin;
                        if (dataInputStream != null) {
                            dataInputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e11) {
                    fileInputStream = fin;
                    Slog.e(TAG, "Unable to read backup pw version");
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                    if (this.mPasswordHashFile.exists()) {
                        fileInputStream = null;
                        dataInputStream = null;
                        fin = new FileInputStream(this.mPasswordHashFile);
                        in = new DataInputStream(new BufferedInputStream(fin));
                        salt = new byte[in.readInt()];
                        in.readFully(salt);
                        this.mPasswordHash = in.readUTF();
                        this.mPasswordSalt = salt;
                        if (in != null) {
                            in.close();
                        }
                        if (fin != null) {
                            fin.close();
                        }
                    }
                    this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                    filter = new IntentFilter();
                    filter.addAction(RUN_BACKUP_ACTION);
                    context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                    this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                    filter = new IntentFilter();
                    filter.addAction(RUN_INITIALIZE_ACTION);
                    context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                    backupIntent = new Intent(RUN_BACKUP_ACTION);
                    backupIntent.addFlags(1073741824);
                    this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                    initIntent = new Intent(RUN_INITIALIZE_ACTION);
                    backupIntent.addFlags(1073741824);
                    this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                    this.mJournalDir = new File(this.mBaseStateDir, "pending");
                    this.mJournalDir.mkdirs();
                    this.mJournal = null;
                    this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                    initPackageTracking();
                    synchronized (this.mBackupParticipants) {
                        addPackageParticipantsLocked(null);
                    }
                    this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                    transport = Secure.getString(context.getContentResolver(), "backup_transport");
                    if (TextUtils.isEmpty(transport)) {
                        transport = null;
                    }
                    this.mCurrentTransport = transport;
                    if (DEBUG) {
                        Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                    }
                    hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                    if (DEBUG) {
                        str = TAG;
                        append = new StringBuilder().append("Found transports: ");
                        if (hosts == null) {
                            str2 = Integer.valueOf(hosts.size());
                        } else {
                            str2 = "null";
                        }
                        Slog.v(str, append.append(str2).toString());
                    }
                    if (hosts != null) {
                        for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                            tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                        }
                    }
                    parseLeftoverJournals();
                    this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
                } catch (Throwable th7) {
                    th = th7;
                    fileInputStream = fin;
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e12) {
                Slog.e(TAG, "Unable to read backup pw version");
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                if (this.mPasswordHashFile.exists()) {
                    fileInputStream = null;
                    dataInputStream = null;
                    fin = new FileInputStream(this.mPasswordHashFile);
                    in = new DataInputStream(new BufferedInputStream(fin));
                    salt = new byte[in.readInt()];
                    in.readFully(salt);
                    this.mPasswordHash = in.readUTF();
                    this.mPasswordSalt = salt;
                    if (in != null) {
                        in.close();
                    }
                    if (fin != null) {
                        fin.close();
                    }
                }
                this.mRunBackupReceiver = new RunBackupReceiver(this, null);
                filter = new IntentFilter();
                filter.addAction(RUN_BACKUP_ACTION);
                context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
                this.mRunInitReceiver = new RunInitializeReceiver(this, null);
                filter = new IntentFilter();
                filter.addAction(RUN_INITIALIZE_ACTION);
                context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
                backupIntent = new Intent(RUN_BACKUP_ACTION);
                backupIntent.addFlags(1073741824);
                this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
                initIntent = new Intent(RUN_INITIALIZE_ACTION);
                backupIntent.addFlags(1073741824);
                this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
                this.mJournalDir = new File(this.mBaseStateDir, "pending");
                this.mJournalDir.mkdirs();
                this.mJournal = null;
                this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                initPackageTracking();
                synchronized (this.mBackupParticipants) {
                    addPackageParticipantsLocked(null);
                }
                this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                transport = Secure.getString(context.getContentResolver(), "backup_transport");
                if (TextUtils.isEmpty(transport)) {
                    transport = null;
                }
                this.mCurrentTransport = transport;
                if (DEBUG) {
                    Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
                }
                hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
                if (DEBUG) {
                    str = TAG;
                    append = new StringBuilder().append("Found transports: ");
                    if (hosts == null) {
                        str2 = "null";
                    } else {
                        str2 = Integer.valueOf(hosts.size());
                    }
                    Slog.v(str, append.append(str2).toString());
                }
                if (hosts != null) {
                    for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                        tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
                    }
                }
                parseLeftoverJournals();
                this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
            }
        }
        this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
        if (this.mPasswordHashFile.exists()) {
            fileInputStream = null;
            dataInputStream = null;
            fin = new FileInputStream(this.mPasswordHashFile);
            in = new DataInputStream(new BufferedInputStream(fin));
            salt = new byte[in.readInt()];
            in.readFully(salt);
            this.mPasswordHash = in.readUTF();
            this.mPasswordSalt = salt;
            if (in != null) {
                in.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
        this.mRunBackupReceiver = new RunBackupReceiver(this, null);
        filter = new IntentFilter();
        filter.addAction(RUN_BACKUP_ACTION);
        context.registerReceiver(this.mRunBackupReceiver, filter, "android.permission.BACKUP", null);
        this.mRunInitReceiver = new RunInitializeReceiver(this, null);
        filter = new IntentFilter();
        filter.addAction(RUN_INITIALIZE_ACTION);
        context.registerReceiver(this.mRunInitReceiver, filter, "android.permission.BACKUP", null);
        backupIntent = new Intent(RUN_BACKUP_ACTION);
        backupIntent.addFlags(1073741824);
        this.mRunBackupIntent = PendingIntent.getBroadcast(context, SCHEDULE_FILE_VERSION, backupIntent, OP_PENDING);
        initIntent = new Intent(RUN_INITIALIZE_ACTION);
        backupIntent.addFlags(1073741824);
        this.mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, OP_PENDING);
        this.mJournalDir = new File(this.mBaseStateDir, "pending");
        this.mJournalDir.mkdirs();
        this.mJournal = null;
        this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
        initPackageTracking();
        synchronized (this.mBackupParticipants) {
            addPackageParticipantsLocked(null);
        }
        this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
        transport = Secure.getString(context.getContentResolver(), "backup_transport");
        if (TextUtils.isEmpty(transport)) {
            transport = null;
        }
        this.mCurrentTransport = transport;
        if (DEBUG) {
            Slog.v(TAG, "Starting with transport " + this.mCurrentTransport);
        }
        hosts = this.mPackageManager.queryIntentServicesAsUser(this.mTransportServiceIntent, OP_PENDING, OP_PENDING);
        if (DEBUG) {
            str = TAG;
            append = new StringBuilder().append("Found transports: ");
            if (hosts == null) {
                str2 = "null";
            } else {
                str2 = Integer.valueOf(hosts.size());
            }
            Slog.v(str, append.append(str2).toString());
        }
        if (hosts != null) {
            for (i = OP_PENDING; i < hosts.size(); i += SCHEDULE_FILE_VERSION) {
                tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
            }
        }
        parseLeftoverJournals();
        this.mWakelock = this.mPowerManager.newWakeLock(SCHEDULE_FILE_VERSION, "*backup*");
    }

    private void initPackageTracking() {
        RandomAccessFile randomAccessFile;
        IOException e;
        Throwable th;
        StringBuilder append;
        IntentFilter filter;
        IntentFilter sdFilter;
        this.mTokenFile = new File(this.mBaseStateDir, "ancestral");
        try {
            randomAccessFile = new RandomAccessFile(this.mTokenFile, "r");
            if (randomAccessFile.readInt() == SCHEDULE_FILE_VERSION) {
                this.mAncestralToken = randomAccessFile.readLong();
                this.mCurrentToken = randomAccessFile.readLong();
                int numPackages = randomAccessFile.readInt();
                if (numPackages >= 0) {
                    this.mAncestralPackages = new HashSet();
                    for (int i = OP_PENDING; i < numPackages; i += SCHEDULE_FILE_VERSION) {
                        String pkgName = randomAccessFile.readUTF();
                        this.mAncestralPackages.add(pkgName);
                    }
                }
            }
            randomAccessFile.close();
        } catch (FileNotFoundException e2) {
            Slog.v(TAG, "No ancestral data");
        } catch (IOException e3) {
            Slog.w(TAG, "Unable to read token file", e3);
        }
        this.mEverStored = new File(this.mBaseStateDir, "processed");
        File file = new File(this.mBaseStateDir, "processed.new");
        if (file.exists()) {
            file.delete();
        }
        if (this.mEverStored.exists()) {
            RandomAccessFile randomAccessFile2 = null;
            RandomAccessFile randomAccessFile3 = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "rws");
                try {
                    RandomAccessFile in = new RandomAccessFile(this.mEverStored, "r");
                    while (true) {
                        try {
                            String pkg = in.readUTF();
                            try {
                                this.mPackageManager.getPackageInfo(pkg, OP_PENDING);
                                this.mEverStoredApps.add(pkg);
                                randomAccessFile.writeUTF(pkg);
                            } catch (NameNotFoundException e4) {
                            }
                        } catch (EOFException e5) {
                            randomAccessFile3 = in;
                            randomAccessFile2 = randomAccessFile;
                        } catch (IOException e6) {
                            e3 = e6;
                            randomAccessFile3 = in;
                            randomAccessFile2 = randomAccessFile;
                        } catch (Throwable th2) {
                            th = th2;
                            randomAccessFile3 = in;
                            randomAccessFile2 = randomAccessFile;
                        }
                    }
                } catch (EOFException e7) {
                    randomAccessFile2 = randomAccessFile;
                    if (!file.renameTo(this.mEverStored)) {
                        append = new StringBuilder().append("Error renaming ");
                        Slog.e(TAG, r22.append(file).append(" to ").append(this.mEverStored).toString());
                    }
                    if (randomAccessFile2 != null) {
                        try {
                            randomAccessFile2.close();
                        } catch (IOException e8) {
                        }
                    }
                    if (randomAccessFile3 != null) {
                        try {
                            randomAccessFile3.close();
                        } catch (IOException e9) {
                        }
                    }
                    synchronized (this.mQueueLock) {
                        this.mFullBackupQueue = readFullBackupSchedule();
                    }
                    filter = new IntentFilter();
                    filter.addAction("android.intent.action.PACKAGE_ADDED");
                    filter.addAction("android.intent.action.PACKAGE_REMOVED");
                    filter.addAction("android.intent.action.PACKAGE_CHANGED");
                    filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                    this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
                    sdFilter = new IntentFilter();
                    sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
                    sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
                    this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
                } catch (IOException e10) {
                    e3 = e10;
                    randomAccessFile2 = randomAccessFile;
                    try {
                        Slog.e(TAG, "Error in processed file", e3);
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (IOException e11) {
                            }
                        }
                        if (randomAccessFile3 != null) {
                            try {
                                randomAccessFile3.close();
                            } catch (IOException e12) {
                            }
                        }
                        synchronized (this.mQueueLock) {
                            this.mFullBackupQueue = readFullBackupSchedule();
                        }
                        filter = new IntentFilter();
                        filter.addAction("android.intent.action.PACKAGE_ADDED");
                        filter.addAction("android.intent.action.PACKAGE_REMOVED");
                        filter.addAction("android.intent.action.PACKAGE_CHANGED");
                        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
                        sdFilter = new IntentFilter();
                        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
                        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
                        this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
                    } catch (Throwable th3) {
                        th = th3;
                        if (randomAccessFile2 != null) {
                            try {
                                randomAccessFile2.close();
                            } catch (IOException e13) {
                            }
                        }
                        if (randomAccessFile3 != null) {
                            try {
                                randomAccessFile3.close();
                            } catch (IOException e14) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    randomAccessFile2 = randomAccessFile;
                    if (randomAccessFile2 != null) {
                        randomAccessFile2.close();
                    }
                    if (randomAccessFile3 != null) {
                        randomAccessFile3.close();
                    }
                    throw th;
                }
            } catch (EOFException e15) {
                if (file.renameTo(this.mEverStored)) {
                    append = new StringBuilder().append("Error renaming ");
                    Slog.e(TAG, r22.append(file).append(" to ").append(this.mEverStored).toString());
                }
                if (randomAccessFile2 != null) {
                    randomAccessFile2.close();
                }
                if (randomAccessFile3 != null) {
                    randomAccessFile3.close();
                }
                synchronized (this.mQueueLock) {
                    this.mFullBackupQueue = readFullBackupSchedule();
                }
                filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_ADDED");
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addAction("android.intent.action.PACKAGE_CHANGED");
                filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
                sdFilter = new IntentFilter();
                sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
                sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
                this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
            } catch (IOException e16) {
                e3 = e16;
                Slog.e(TAG, "Error in processed file", e3);
                if (randomAccessFile2 != null) {
                    randomAccessFile2.close();
                }
                if (randomAccessFile3 != null) {
                    randomAccessFile3.close();
                }
                synchronized (this.mQueueLock) {
                    this.mFullBackupQueue = readFullBackupSchedule();
                }
                filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_ADDED");
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addAction("android.intent.action.PACKAGE_CHANGED");
                filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
                this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
                sdFilter = new IntentFilter();
                sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
                sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
                this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
            }
        }
        synchronized (this.mQueueLock) {
            this.mFullBackupQueue = readFullBackupSchedule();
        }
        filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        sdFilter = new IntentFilter();
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, sdFilter);
    }

    private ArrayList<FullBackupEntry> readFullBackupSchedule() {
        BufferedInputStream bufStream;
        Exception e;
        Object fstream;
        ArrayList<FullBackupEntry> arrayList;
        Throwable th;
        boolean changed = MORE_DEBUG;
        ArrayList<FullBackupEntry> arrayList2 = null;
        List<PackageInfo> apps = PackageManagerBackupAgent.getStorableApplications(this.mPackageManager);
        if (this.mFullBackupScheduleFile.exists()) {
            AutoCloseable autoCloseable = null;
            BufferedInputStream bufStream2 = null;
            DataInputStream in = null;
            try {
                InputStream fileInputStream = new FileInputStream(this.mFullBackupScheduleFile);
                try {
                    bufStream = new BufferedInputStream(fileInputStream);
                } catch (Exception e2) {
                    e = e2;
                    fstream = fileInputStream;
                    try {
                        Slog.e(TAG, "Unable to read backup schedule", e);
                        this.mFullBackupScheduleFile.delete();
                        arrayList2 = null;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream2);
                        IoUtils.closeQuietly(autoCloseable);
                        if (arrayList2 == null) {
                            changed = DEBUG_BACKUP_TRACE;
                            arrayList = new ArrayList(apps.size());
                            for (PackageInfo info : apps) {
                                if (appGetsFullBackup(info)) {
                                    if (appIsEligibleForBackup(info.applicationInfo)) {
                                        arrayList.add(new FullBackupEntry(this, info.packageName, 0));
                                    }
                                }
                            }
                        }
                        if (changed) {
                            writeFullBackupScheduleAsync();
                        }
                        return arrayList2;
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream2);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fstream = fileInputStream;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream2);
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
                try {
                    DataInputStream dataInputStream = new DataInputStream(bufStream);
                    try {
                        int version = dataInputStream.readInt();
                        if (version != SCHEDULE_FILE_VERSION) {
                            Slog.e(TAG, "Unknown backup schedule version " + version);
                            IoUtils.closeQuietly(dataInputStream);
                            IoUtils.closeQuietly(bufStream);
                            IoUtils.closeQuietly(fileInputStream);
                            return null;
                        }
                        int N = dataInputStream.readInt();
                        arrayList = new ArrayList(N);
                        try {
                            HashSet<String> foundApps = new HashSet(N);
                            for (int i = OP_PENDING; i < N; i += SCHEDULE_FILE_VERSION) {
                                String pkgName = dataInputStream.readUTF();
                                long lastBackup = dataInputStream.readLong();
                                foundApps.add(pkgName);
                                try {
                                    PackageInfo pkg = this.mPackageManager.getPackageInfo(pkgName, OP_PENDING);
                                    if (appGetsFullBackup(pkg)) {
                                        if (appIsEligibleForBackup(pkg.applicationInfo)) {
                                            arrayList.add(new FullBackupEntry(this, pkgName, lastBackup));
                                        }
                                    }
                                    if (DEBUG) {
                                        Slog.i(TAG, "Package " + pkgName + " no longer eligible for full backup");
                                    }
                                } catch (NameNotFoundException e3) {
                                    if (DEBUG) {
                                        Slog.i(TAG, "Package " + pkgName + " not installed; dropping from full backup");
                                    }
                                }
                            }
                            for (PackageInfo app : apps) {
                                if (appGetsFullBackup(app)) {
                                    if (appIsEligibleForBackup(app.applicationInfo)) {
                                        if (!foundApps.contains(app.packageName)) {
                                            arrayList.add(new FullBackupEntry(this, app.packageName, 0));
                                            changed = DEBUG_BACKUP_TRACE;
                                        }
                                    }
                                }
                            }
                            Collections.sort(arrayList);
                            IoUtils.closeQuietly(dataInputStream);
                            IoUtils.closeQuietly(bufStream);
                            IoUtils.closeQuietly(fileInputStream);
                            arrayList2 = arrayList;
                        } catch (Exception e4) {
                            e = e4;
                            in = dataInputStream;
                            bufStream2 = bufStream;
                            autoCloseable = fileInputStream;
                            arrayList2 = arrayList;
                        } catch (Throwable th4) {
                            th = th4;
                            in = dataInputStream;
                            bufStream2 = bufStream;
                            fstream = fileInputStream;
                            arrayList2 = arrayList;
                        }
                    } catch (Exception e5) {
                        e = e5;
                        in = dataInputStream;
                        bufStream2 = bufStream;
                        fstream = fileInputStream;
                        Slog.e(TAG, "Unable to read backup schedule", e);
                        this.mFullBackupScheduleFile.delete();
                        arrayList2 = null;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream2);
                        IoUtils.closeQuietly(autoCloseable);
                        if (arrayList2 == null) {
                            changed = DEBUG_BACKUP_TRACE;
                            arrayList = new ArrayList(apps.size());
                            for (PackageInfo info2 : apps) {
                                if (appGetsFullBackup(info2)) {
                                    if (appIsEligibleForBackup(info2.applicationInfo)) {
                                        arrayList.add(new FullBackupEntry(this, info2.packageName, 0));
                                    }
                                }
                            }
                        }
                        if (changed) {
                            writeFullBackupScheduleAsync();
                        }
                        return arrayList2;
                    } catch (Throwable th5) {
                        th = th5;
                        in = dataInputStream;
                        bufStream2 = bufStream;
                        fstream = fileInputStream;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream2);
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    bufStream2 = bufStream;
                    fstream = fileInputStream;
                    Slog.e(TAG, "Unable to read backup schedule", e);
                    this.mFullBackupScheduleFile.delete();
                    arrayList2 = null;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream2);
                    IoUtils.closeQuietly(autoCloseable);
                    if (arrayList2 == null) {
                        changed = DEBUG_BACKUP_TRACE;
                        arrayList = new ArrayList(apps.size());
                        for (PackageInfo info22 : apps) {
                            if (appGetsFullBackup(info22)) {
                                if (appIsEligibleForBackup(info22.applicationInfo)) {
                                    arrayList.add(new FullBackupEntry(this, info22.packageName, 0));
                                }
                            }
                        }
                    }
                    if (changed) {
                        writeFullBackupScheduleAsync();
                    }
                    return arrayList2;
                } catch (Throwable th6) {
                    th = th6;
                    bufStream2 = bufStream;
                    fstream = fileInputStream;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream2);
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
                Slog.e(TAG, "Unable to read backup schedule", e);
                this.mFullBackupScheduleFile.delete();
                arrayList2 = null;
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(bufStream2);
                IoUtils.closeQuietly(autoCloseable);
                if (arrayList2 == null) {
                    changed = DEBUG_BACKUP_TRACE;
                    arrayList = new ArrayList(apps.size());
                    for (PackageInfo info222 : apps) {
                        if (appGetsFullBackup(info222)) {
                            if (appIsEligibleForBackup(info222.applicationInfo)) {
                                arrayList.add(new FullBackupEntry(this, info222.packageName, 0));
                            }
                        }
                    }
                }
                if (changed) {
                    writeFullBackupScheduleAsync();
                }
                return arrayList2;
            }
        }
        if (arrayList2 == null) {
            changed = DEBUG_BACKUP_TRACE;
            arrayList = new ArrayList(apps.size());
            for (PackageInfo info2222 : apps) {
                if (appGetsFullBackup(info2222)) {
                    if (appIsEligibleForBackup(info2222.applicationInfo)) {
                        arrayList.add(new FullBackupEntry(this, info2222.packageName, 0));
                    }
                }
            }
        }
        if (changed) {
            writeFullBackupScheduleAsync();
        }
        return arrayList2;
    }

    private void writeFullBackupScheduleAsync() {
        this.mBackupHandler.removeCallbacks(this.mFullBackupScheduleWriter);
        this.mBackupHandler.post(this.mFullBackupScheduleWriter);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseLeftoverJournals() {
        Exception e;
        Throwable th;
        int i = OP_PENDING;
        File[] listFiles = this.mJournalDir.listFiles();
        int length = listFiles.length;
        while (i < length) {
            File f = listFiles[i];
            if (this.mJournal == null || f.compareTo(this.mJournal) != 0) {
                RandomAccessFile randomAccessFile = null;
                try {
                    Slog.i(TAG, "Found stale backup journal, scheduling");
                    RandomAccessFile in = new RandomAccessFile(f, "r");
                    while (true) {
                        try {
                            dataChangedImpl(in.readUTF());
                        } catch (EOFException e2) {
                            randomAccessFile = in;
                        } catch (Exception e3) {
                            e = e3;
                            randomAccessFile = in;
                        } catch (Throwable th2) {
                            th = th2;
                            randomAccessFile = in;
                        }
                    }
                } catch (EOFException e4) {
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e5) {
                        }
                    }
                    f.delete();
                    i += SCHEDULE_FILE_VERSION;
                } catch (Exception e6) {
                    e = e6;
                    try {
                        Slog.e(TAG, "Can't read " + f, e);
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e7) {
                            }
                        }
                        f.delete();
                        i += SCHEDULE_FILE_VERSION;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
            } else {
                i += SCHEDULE_FILE_VERSION;
            }
        }
    }

    private SecretKey buildPasswordKey(String algorithm, String pw, byte[] salt, int rounds) {
        return buildCharArrayKey(algorithm, pw.toCharArray(), salt, rounds);
    }

    private SecretKey buildCharArrayKey(String algorithm, char[] pwArray, byte[] salt, int rounds) {
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(pwArray, salt, rounds, PBKDF2_KEY_SIZE));
        } catch (InvalidKeySpecException e) {
            Slog.e(TAG, "Invalid key spec for PBKDF2!");
            return null;
        } catch (NoSuchAlgorithmException e2) {
            Slog.e(TAG, "PBKDF2 unavailable!");
            return null;
        }
    }

    private String buildPasswordHash(String algorithm, String pw, byte[] salt, int rounds) {
        SecretKey key = buildPasswordKey(algorithm, pw, salt, rounds);
        if (key != null) {
            return byteArrayToHex(key.getEncoded());
        }
        return null;
    }

    private String byteArrayToHex(byte[] data) {
        StringBuilder buf = new StringBuilder(data.length * MSG_RUN_ADB_BACKUP);
        for (int i = OP_PENDING; i < data.length; i += SCHEDULE_FILE_VERSION) {
            buf.append(Byte.toHexString(data[i], DEBUG_BACKUP_TRACE));
        }
        return buf.toString();
    }

    private byte[] hexToByteArray(String digits) {
        int bytes = digits.length() / MSG_RUN_ADB_BACKUP;
        if (bytes * MSG_RUN_ADB_BACKUP != digits.length()) {
            throw new IllegalArgumentException("Hex string must have an even number of digits");
        }
        byte[] result = new byte[bytes];
        for (int i = OP_PENDING; i < digits.length(); i += MSG_RUN_ADB_BACKUP) {
            result[i / MSG_RUN_ADB_BACKUP] = (byte) Integer.parseInt(digits.substring(i, i + MSG_RUN_ADB_BACKUP), 16);
        }
        return result;
    }

    private byte[] makeKeyChecksum(String algorithm, byte[] pwBytes, byte[] salt, int rounds) {
        char[] mkAsChar = new char[pwBytes.length];
        for (int i = OP_PENDING; i < pwBytes.length; i += SCHEDULE_FILE_VERSION) {
            mkAsChar[i] = (char) pwBytes[i];
        }
        return buildCharArrayKey(algorithm, mkAsChar, salt, rounds).getEncoded();
    }

    private byte[] randomBytes(int bits) {
        byte[] array = new byte[(bits / MSG_RESTORE_TIMEOUT)];
        this.mRng.nextBytes(array);
        return array;
    }

    boolean passwordMatchesSaved(String algorithm, String candidatePw, int rounds) {
        if (this.mPasswordHash == null) {
            if (candidatePw == null || "".equals(candidatePw)) {
                return DEBUG_BACKUP_TRACE;
            }
        } else if (candidatePw != null && candidatePw.length() > 0) {
            if (this.mPasswordHash.equalsIgnoreCase(buildPasswordHash(algorithm, candidatePw, this.mPasswordSalt, rounds))) {
                return DEBUG_BACKUP_TRACE;
            }
        }
        return MORE_DEBUG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setBackupPassword(String currentPw, String newPw) {
        DataOutputStream pwOut;
        Throwable th;
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupPassword");
        int i = this.mPasswordVersion;
        boolean pbkdf2Fallback = r0 < MSG_RUN_ADB_BACKUP ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
        if (!passwordMatchesSaved(PBKDF_CURRENT, currentPw, PBKDF2_HASH_ROUNDS)) {
            if (pbkdf2Fallback) {
            }
            return MORE_DEBUG;
        }
        this.mPasswordVersion = MSG_RUN_ADB_BACKUP;
        FileOutputStream pwFout = null;
        DataOutputStream dataOutputStream = null;
        try {
            FileOutputStream pwFout2 = new FileOutputStream(this.mPasswordVersionFile);
            try {
                pwOut = new DataOutputStream(pwFout2);
            } catch (IOException e) {
                pwFout = pwFout2;
                try {
                    Slog.e(TAG, "Unable to write backup pw version; password not changed");
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.close();
                        } catch (IOException e2) {
                            Slog.w(TAG, "Unable to close pw version record");
                            return MORE_DEBUG;
                        }
                    }
                    if (pwFout != null) {
                        pwFout.close();
                    }
                    return MORE_DEBUG;
                } catch (Throwable th2) {
                    th = th2;
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.close();
                        } catch (IOException e3) {
                            Slog.w(TAG, "Unable to close pw version record");
                            throw th;
                        }
                    }
                    if (pwFout != null) {
                        pwFout.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                pwFout = pwFout2;
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (pwFout != null) {
                    pwFout.close();
                }
                throw th;
            }
            try {
                pwOut.writeInt(this.mPasswordVersion);
                if (pwOut != null) {
                    try {
                        pwOut.close();
                    } catch (IOException e4) {
                        Slog.w(TAG, "Unable to close pw version record");
                    }
                }
                if (pwFout2 != null) {
                    pwFout2.close();
                }
                if (newPw == null || newPw.isEmpty()) {
                    if (this.mPasswordHashFile.exists()) {
                        if (!this.mPasswordHashFile.delete()) {
                            Slog.e(TAG, "Unable to clear backup password");
                            return MORE_DEBUG;
                        }
                    }
                    this.mPasswordHash = null;
                    this.mPasswordSalt = null;
                    return DEBUG_BACKUP_TRACE;
                }
                try {
                    byte[] salt = randomBytes(PBKDF2_SALT_SIZE);
                    String newPwHash = buildPasswordHash(PBKDF_CURRENT, newPw, salt, PBKDF2_HASH_ROUNDS);
                    OutputStream pwf = null;
                    OutputStream buffer = null;
                    DataOutputStream dataOutputStream2 = null;
                    try {
                        OutputStream fileOutputStream = new FileOutputStream(this.mPasswordHashFile);
                        try {
                            OutputStream buffer2 = new BufferedOutputStream(fileOutputStream);
                            try {
                                DataOutputStream out = new DataOutputStream(buffer2);
                                try {
                                    out.writeInt(salt.length);
                                    out.write(salt);
                                    out.writeUTF(newPwHash);
                                    out.flush();
                                    this.mPasswordHash = newPwHash;
                                    this.mPasswordSalt = salt;
                                    if (out != null) {
                                        out.close();
                                    }
                                    if (buffer2 != null) {
                                        buffer2.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                    }
                                    return DEBUG_BACKUP_TRACE;
                                } catch (Throwable th4) {
                                    th = th4;
                                    dataOutputStream2 = out;
                                    buffer = buffer2;
                                    pwf = fileOutputStream;
                                    if (dataOutputStream2 != null) {
                                        dataOutputStream2.close();
                                    }
                                    if (buffer != null) {
                                        buffer.close();
                                    }
                                    if (pwf != null) {
                                        pwf.close();
                                    }
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                buffer = buffer2;
                                pwf = fileOutputStream;
                                if (dataOutputStream2 != null) {
                                    dataOutputStream2.close();
                                }
                                if (buffer != null) {
                                    buffer.close();
                                }
                                if (pwf != null) {
                                    pwf.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            pwf = fileOutputStream;
                            if (dataOutputStream2 != null) {
                                dataOutputStream2.close();
                            }
                            if (buffer != null) {
                                buffer.close();
                            }
                            if (pwf != null) {
                                pwf.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        if (dataOutputStream2 != null) {
                            dataOutputStream2.close();
                        }
                        if (buffer != null) {
                            buffer.close();
                        }
                        if (pwf != null) {
                            pwf.close();
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    Slog.e(TAG, "Unable to set backup password");
                    return MORE_DEBUG;
                }
            } catch (IOException e6) {
                dataOutputStream = pwOut;
                pwFout = pwFout2;
                Slog.e(TAG, "Unable to write backup pw version; password not changed");
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (pwFout != null) {
                    pwFout.close();
                }
                return MORE_DEBUG;
            } catch (Throwable th8) {
                th = th8;
                dataOutputStream = pwOut;
                pwFout = pwFout2;
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (pwFout != null) {
                    pwFout.close();
                }
                throw th;
            }
        } catch (IOException e7) {
            Slog.e(TAG, "Unable to write backup pw version; password not changed");
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (pwFout != null) {
                pwFout.close();
            }
            return MORE_DEBUG;
        }
    }

    public boolean hasBackupPassword() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "hasBackupPassword");
        if (this.mPasswordHash == null || this.mPasswordHash.length() <= 0) {
            return MORE_DEBUG;
        }
        return DEBUG_BACKUP_TRACE;
    }

    private boolean backupPasswordMatches(String currentPw) {
        if (hasBackupPassword()) {
            boolean pbkdf2Fallback = this.mPasswordVersion < MSG_RUN_ADB_BACKUP ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
            if (!(passwordMatchesSaved(PBKDF_CURRENT, currentPw, PBKDF2_HASH_ROUNDS) || (pbkdf2Fallback && passwordMatchesSaved(PBKDF_FALLBACK, currentPw, PBKDF2_HASH_ROUNDS)))) {
                if (DEBUG) {
                    Slog.w(TAG, "Backup password mismatch; aborting");
                }
                return MORE_DEBUG;
            }
        }
        return DEBUG_BACKUP_TRACE;
    }

    void recordInitPendingLocked(boolean isPending, String transportName) {
        this.mBackupHandler.removeMessages(MSG_RETRY_INIT);
        try {
            IBackupTransport transport = getTransport(transportName);
            if (transport != null) {
                File initPendingFile = new File(new File(this.mBaseStateDir, transport.transportDirName()), INIT_SENTINEL_FILE_NAME);
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
                return;
            }
        } catch (RemoteException e2) {
        }
        if (isPending) {
            this.mPendingInits.add(transportName);
            this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(MSG_RETRY_INIT, isPending ? SCHEDULE_FILE_VERSION : OP_PENDING, OP_PENDING, transportName), TRANSPORT_RETRY_INTERVAL);
        }
    }

    void resetBackupState(File stateFileDir) {
        synchronized (this.mQueueLock) {
            this.mEverStoredApps.clear();
            this.mEverStored.delete();
            this.mCurrentToken = 0;
            writeRestoreTokens();
            File[] listFiles = stateFileDir.listFiles();
            int length = listFiles.length;
            for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
                File sf = listFiles[i];
                if (!sf.getName().equals(INIT_SENTINEL_FILE_NAME)) {
                    sf.delete();
                }
            }
        }
        synchronized (this.mBackupParticipants) {
            int N = this.mBackupParticipants.size();
            for (int i2 = OP_PENDING; i2 < N; i2 += SCHEDULE_FILE_VERSION) {
                HashSet<String> participants = (HashSet) this.mBackupParticipants.valueAt(i2);
                if (participants != null) {
                    for (String packageName : participants) {
                        dataChangedImpl(packageName);
                    }
                }
            }
        }
    }

    private void registerTransport(String name, String component, IBackupTransport transport) {
        synchronized (this.mTransports) {
            if (DEBUG) {
                Slog.v(TAG, "Registering transport " + component + "::" + name + " = " + transport);
            }
            if (transport != null) {
                this.mTransports.put(name, transport);
                this.mTransportNames.put(component, name);
                try {
                    File stateDir = new File(this.mBaseStateDir, transport.transportDirName());
                    stateDir.mkdirs();
                    if (new File(stateDir, INIT_SENTINEL_FILE_NAME).exists()) {
                        synchronized (this.mQueueLock) {
                            this.mPendingInits.add(name);
                            this.mAlarmManager.set(OP_PENDING, System.currentTimeMillis() + TIMEOUT_RESTORE_INTERVAL, this.mRunInitIntent);
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to register transport as " + name);
                    this.mTransportNames.remove(component);
                    this.mTransports.remove(name);
                }
                return;
            }
            this.mTransports.remove(this.mTransportNames.get(component));
            this.mTransportNames.remove(component);
        }
    }

    void checkForTransportAndBind(PackageInfo pkgInfo) {
        List<ResolveInfo> hosts = this.mPackageManager.queryIntentServicesAsUser(new Intent(this.mTransportServiceIntent).setPackage(pkgInfo.packageName), OP_PENDING, OP_PENDING);
        if (hosts != null) {
            int N = hosts.size();
            for (int i = OP_PENDING; i < N; i += SCHEDULE_FILE_VERSION) {
                tryBindTransport(((ResolveInfo) hosts.get(i)).serviceInfo);
            }
        }
    }

    boolean tryBindTransport(ServiceInfo info) {
        try {
            if ((this.mPackageManager.getPackageInfo(info.packageName, OP_PENDING).applicationInfo.privateFlags & MSG_RESTORE_TIMEOUT) != 0) {
                return bindTransport(info);
            }
            Slog.w(TAG, "Transport package " + info.packageName + " not privileged");
            return MORE_DEBUG;
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Problem resolving transport package " + info.packageName);
        }
    }

    boolean bindTransport(ServiceInfo transport) {
        ComponentName svcName = new ComponentName(transport.packageName, transport.name);
        if (this.mTransportWhitelist.contains(svcName)) {
            TransportConnection connection;
            Intent intent = new Intent(this.mTransportServiceIntent);
            intent.setComponent(svcName);
            synchronized (this.mTransports) {
                connection = (TransportConnection) this.mTransportConnections.get(transport.packageName);
                if (connection == null) {
                    connection = new TransportConnection(this, transport);
                    this.mTransportConnections.put(transport.packageName, connection);
                } else {
                    this.mContext.unbindService(connection);
                }
            }
            return this.mContext.bindServiceAsUser(intent, connection, SCHEDULE_FILE_VERSION, UserHandle.SYSTEM);
        }
        Slog.w(TAG, "Proposed transport " + svcName + " not whitelisted; ignoring");
        return MORE_DEBUG;
    }

    void addPackageParticipantsLocked(String[] packageNames) {
        List<PackageInfo> targetApps = allAgentPackages();
        if (packageNames != null) {
            int length = packageNames.length;
            for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
                addPackageParticipantsLockedInner(packageNames[i], targetApps);
            }
            return;
        }
        addPackageParticipantsLockedInner(null, targetApps);
    }

    private void addPackageParticipantsLockedInner(String packageName, List<PackageInfo> targetPkgs) {
        for (PackageInfo pkg : targetPkgs) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                int uid = pkg.applicationInfo.uid;
                HashSet<String> set = (HashSet) this.mBackupParticipants.get(uid);
                if (set == null) {
                    set = new HashSet();
                    this.mBackupParticipants.put(uid, set);
                }
                set.add(pkg.packageName);
                dataChangedImpl(pkg.packageName);
            }
        }
    }

    void removePackageParticipantsLocked(String[] packageNames, int oldUid) {
        if (packageNames == null) {
            Slog.w(TAG, "removePackageParticipants with null list");
            return;
        }
        int length = packageNames.length;
        for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
            String pkg = packageNames[i];
            HashSet<String> set = (HashSet) this.mBackupParticipants.get(oldUid);
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

    List<PackageInfo> allAgentPackages() {
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackages(64);
        int a = packages.size() + OP_TIMEOUT;
        while (a >= 0) {
            PackageInfo pkg = (PackageInfo) packages.get(a);
            try {
                ApplicationInfo app = pkg.applicationInfo;
                if ((app.flags & DumpState.DUMP_VERSION) == 0 || app.backupAgentName == null || (app.flags & 67108864) != 0) {
                    packages.remove(a);
                    a += OP_TIMEOUT;
                } else {
                    app = this.mPackageManager.getApplicationInfo(pkg.packageName, DumpState.DUMP_PROVIDERS);
                    pkg.applicationInfo.sharedLibraryFiles = app.sharedLibraryFiles;
                    a += OP_TIMEOUT;
                }
            } catch (NameNotFoundException e) {
                packages.remove(a);
            }
        }
        return packages;
    }

    void logBackupComplete(String packageName) {
        Throwable th;
        if (!packageName.equals(PACKAGE_MANAGER_SENTINEL)) {
            synchronized (this.mEverStoredApps) {
                if (this.mEverStoredApps.add(packageName)) {
                    RandomAccessFile randomAccessFile = null;
                    try {
                        RandomAccessFile out = new RandomAccessFile(this.mEverStored, "rws");
                        try {
                            out.seek(out.length());
                            out.writeUTF(packageName);
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                }
                            }
                            randomAccessFile = out;
                        } catch (IOException e2) {
                            randomAccessFile = out;
                            try {
                                Slog.e(TAG, "Can't log backup of " + packageName + " to " + this.mEverStored);
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (IOException e3) {
                                    }
                                }
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (IOException e4) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            randomAccessFile = out;
                            if (randomAccessFile != null) {
                                randomAccessFile.close();
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        Slog.e(TAG, "Can't log backup of " + packageName + " to " + this.mEverStored);
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    void removeEverBackedUp(String packageName) {
        Throwable th;
        if (DEBUG) {
            Slog.v(TAG, "Removing backed-up knowledge of " + packageName);
        }
        synchronized (this.mEverStoredApps) {
            File tempKnownFile = new File(this.mBaseStateDir, "processed.new");
            RandomAccessFile randomAccessFile = null;
            try {
                RandomAccessFile known = new RandomAccessFile(tempKnownFile, "rws");
            } catch (IOException e) {
                e = e;
                try {
                    IOException e2;
                    Slog.w(TAG, "Error rewriting " + this.mEverStored, e2);
                    this.mEverStoredApps.clear();
                    tempKnownFile.delete();
                    this.mEverStored.delete();
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            }
            try {
                this.mEverStoredApps.remove(packageName);
                for (String s : this.mEverStoredApps) {
                    known.writeUTF(s);
                }
                known.close();
                randomAccessFile = null;
                if (!tempKnownFile.renameTo(this.mEverStored)) {
                    throw new IOException("Can't rename " + tempKnownFile + " to " + this.mEverStored);
                }
            } catch (IOException e5) {
                e2 = e5;
                randomAccessFile = known;
                Slog.w(TAG, "Error rewriting " + this.mEverStored, e2);
                this.mEverStoredApps.clear();
                tempKnownFile.delete();
                this.mEverStored.delete();
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = known;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        }
    }

    void writeRestoreTokens() {
        try {
            RandomAccessFile af = new RandomAccessFile(this.mTokenFile, "rwd");
            af.writeInt(SCHEDULE_FILE_VERSION);
            af.writeLong(this.mAncestralToken);
            af.writeLong(this.mCurrentToken);
            if (this.mAncestralPackages == null) {
                af.writeInt(OP_TIMEOUT);
            } else {
                af.writeInt(this.mAncestralPackages.size());
                if (DEBUG) {
                    Slog.v(TAG, "Ancestral packages:  " + this.mAncestralPackages.size());
                }
                for (String pkgName : this.mAncestralPackages) {
                    af.writeUTF(pkgName);
                }
            }
            af.close();
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write token file:", e);
        }
    }

    private IBackupTransport getTransport(String transportName) {
        IBackupTransport transport;
        synchronized (this.mTransports) {
            transport = (IBackupTransport) this.mTransports.get(transportName);
            if (transport == null) {
                Slog.w(TAG, "Requested unavailable transport: " + transportName);
            }
        }
        return transport;
    }

    private String getTransportName(IBackupTransport transport) {
        synchronized (this.mTransports) {
            int N = this.mTransports.size();
            for (int i = OP_PENDING; i < N; i += SCHEDULE_FILE_VERSION) {
                if (((IBackupTransport) this.mTransports.valueAt(i)).equals(transport)) {
                    String str = (String) this.mTransports.keyAt(i);
                    return str;
                }
            }
            return null;
        }
    }

    IBackupAgent bindToAgentSynchronous(ApplicationInfo app, int mode) {
        IBackupAgent agent = null;
        synchronized (this.mAgentConnectLock) {
            this.mConnecting = DEBUG_BACKUP_TRACE;
            this.mConnectedAgent = null;
            try {
                if (this.mActivityManager.bindBackupAgent(app.packageName, mode, OP_PENDING)) {
                    Slog.d(TAG, "awaiting agent for " + app);
                    long timeoutMark = System.currentTimeMillis() + TIMEOUT_INTERVAL;
                    while (this.mConnecting && this.mConnectedAgent == null && System.currentTimeMillis() < timeoutMark) {
                        try {
                            this.mAgentConnectLock.wait(5000);
                        } catch (InterruptedException e) {
                            Slog.w(TAG, "Interrupted: " + e);
                            this.mActivityManager.clearPendingBackup();
                            return null;
                        }
                    }
                    if (this.mConnecting) {
                        Slog.w(TAG, "Timeout waiting for agent " + app);
                        this.mActivityManager.clearPendingBackup();
                        return null;
                    }
                    if (DEBUG) {
                        Slog.i(TAG, "got agent " + this.mConnectedAgent);
                    }
                    agent = this.mConnectedAgent;
                }
            } catch (RemoteException e2) {
            }
            return agent;
        }
    }

    void clearApplicationDataSynchronous(String packageName) {
        try {
            if ((this.mPackageManager.getPackageInfo(packageName, OP_PENDING).applicationInfo.flags & 64) != 0) {
                ClearDataObserver observer = new ClearDataObserver(this);
                synchronized (this.mClearDataLock) {
                    this.mClearingData = DEBUG_BACKUP_TRACE;
                    try {
                        this.mActivityManager.clearApplicationUserData(packageName, observer, OP_PENDING);
                    } catch (RemoteException e) {
                    }
                    long timeoutMark = System.currentTimeMillis() + TIMEOUT_INTERVAL;
                    while (this.mClearingData && System.currentTimeMillis() < timeoutMark) {
                        try {
                            this.mClearDataLock.wait(5000);
                        } catch (InterruptedException e2) {
                            this.mClearingData = MORE_DEBUG;
                        }
                    }
                }
            }
        } catch (NameNotFoundException e3) {
            Slog.w(TAG, "Tried to clear data for " + packageName + " but not found");
        }
    }

    public long getAvailableRestoreToken(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreToken");
        long token = this.mAncestralToken;
        synchronized (this.mQueueLock) {
            if (this.mEverStoredApps.contains(packageName)) {
                token = this.mCurrentToken;
            }
        }
        return token;
    }

    public int requestBackup(String[] packages, IBackupObserver observer) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "requestBackup");
        if (packages == null || packages.length < SCHEDULE_FILE_VERSION) {
            Slog.e(TAG, "No packages named for backup request");
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            throw new IllegalArgumentException("No packages are provided for backup");
        }
        IBackupTransport transport = getTransport(this.mCurrentTransport);
        if (transport == null) {
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
        ArrayList<String> fullBackupList = new ArrayList();
        ArrayList<String> kvBackupList = new ArrayList();
        int length = packages.length;
        for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
            String packageName = packages[i];
            try {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 64);
                if (!appIsEligibleForBackup(packageInfo.applicationInfo)) {
                    sendBackupOnPackageResult(observer, packageName, -2001);
                } else if (appGetsFullBackup(packageInfo)) {
                    fullBackupList.add(packageInfo.packageName);
                } else {
                    kvBackupList.add(packageInfo.packageName);
                }
            } catch (NameNotFoundException e) {
                sendBackupOnPackageResult(observer, packageName, -2002);
            }
        }
        Object[] objArr = new Object[MSG_RUN_RESTORE];
        objArr[OP_PENDING] = Integer.valueOf(packages.length);
        objArr[SCHEDULE_FILE_VERSION] = Integer.valueOf(kvBackupList.size());
        objArr[MSG_RUN_ADB_BACKUP] = Integer.valueOf(fullBackupList.size());
        EventLog.writeEvent(EventLogTags.BACKUP_REQUESTED, objArr);
        try {
            String dirName = transport.transportDirName();
            Message msg = this.mBackupHandler.obtainMessage(MSG_REQUEST_BACKUP);
            msg.obj = new BackupParams(transport, dirName, kvBackupList, fullBackupList, observer, DEBUG_BACKUP_TRACE);
            this.mBackupHandler.sendMessage(msg);
            return OP_PENDING;
        } catch (RemoteException e2) {
            Slog.e(TAG, "Transport became unavailable while attempting backup");
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
    }

    void prepareOperationTimeout(int token, long interval, BackupRestoreTask callback) {
        synchronized (this.mCurrentOpLock) {
            this.mCurrentOperations.put(token, new Operation(this, OP_PENDING, callback));
            this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(MSG_TIMEOUT, token, OP_PENDING, callback), interval);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean waitUntilOperationComplete(int token) {
        int finalState = OP_PENDING;
        synchronized (this.mCurrentOpLock) {
            while (true) {
                Operation op = (Operation) this.mCurrentOperations.get(token);
                if (op == null) {
                    break;
                } else if (op.state == 0) {
                    try {
                        this.mCurrentOpLock.wait();
                    } catch (InterruptedException e) {
                    }
                } else {
                    finalState = op.state;
                }
            }
        }
        this.mBackupHandler.removeMessages(MSG_TIMEOUT);
        if (finalState == SCHEDULE_FILE_VERSION) {
            return DEBUG_BACKUP_TRACE;
        }
        return MORE_DEBUG;
    }

    void handleTimeout(int token, Object obj) {
        synchronized (this.mCurrentOpLock) {
            Operation op = (Operation) this.mCurrentOperations.get(token);
            int state = op != null ? op.state : OP_TIMEOUT;
            if (state == SCHEDULE_FILE_VERSION) {
                op = null;
                this.mCurrentOperations.delete(token);
            } else if (state == 0) {
                if (DEBUG) {
                    Slog.v(TAG, "TIMEOUT: token=" + Integer.toHexString(token));
                }
                op.state = OP_TIMEOUT;
            }
            this.mCurrentOpLock.notifyAll();
        }
        if (op != null && op.callback != null) {
            op.callback.handleTimeout();
        }
    }

    private void routeSocketDataToOutput(ParcelFileDescriptor inPipe, OutputStream out) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(inPipe.getFileDescriptor()));
        byte[] buffer = new byte[DumpState.DUMP_VERSION];
        while (true) {
            int chunkTotal = in.readInt();
            if (chunkTotal > 0) {
                while (chunkTotal > 0) {
                    int toRead;
                    if (chunkTotal > buffer.length) {
                        toRead = buffer.length;
                    } else {
                        toRead = chunkTotal;
                    }
                    int nRead = in.read(buffer, OP_PENDING, toRead);
                    out.write(buffer, OP_PENDING, nRead);
                    chunkTotal -= nRead;
                }
            } else {
                return;
            }
        }
    }

    void tearDownAgentAndKill(ApplicationInfo app) {
        if (app != null) {
            try {
                this.mActivityManager.unbindBackupAgent(app);
                if (app.uid >= PBKDF2_HASH_ROUNDS && !app.packageName.equals("com.android.backupconfirm")) {
                    this.mActivityManager.killApplicationProcess(app.processName, app.uid);
                }
            } catch (RemoteException e) {
                Slog.d(TAG, "Lost app trying to shut down");
            }
        }
    }

    boolean deviceIsEncrypted() {
        boolean z = DEBUG_BACKUP_TRACE;
        try {
            if (this.mMountService.getEncryptionState() == SCHEDULE_FILE_VERSION) {
                z = MORE_DEBUG;
            } else if (this.mMountService.getPasswordType() == SCHEDULE_FILE_VERSION) {
                z = MORE_DEBUG;
            }
            return z;
        } catch (Exception e) {
            Slog.e(TAG, "Unable to communicate with mount service: " + e.getMessage());
            return DEBUG_BACKUP_TRACE;
        }
    }

    void scheduleNextFullBackupJob(long transportMinLatency) {
        synchronized (this.mQueueLock) {
            if (this.mFullBackupQueue.size() > 0) {
                long timeSinceLast = System.currentTimeMillis() - ((FullBackupEntry) this.mFullBackupQueue.get(OP_PENDING)).lastBackup;
                this.mBackupHandler.postDelayed(new AnonymousClass3(Math.max(transportMinLatency, timeSinceLast < MIN_FULL_BACKUP_INTERVAL ? MIN_FULL_BACKUP_INTERVAL - timeSinceLast : 0)), 2500);
            } else if (DEBUG_SCHEDULING) {
                Slog.i(TAG, "Full backup queue empty; not scheduling");
            }
        }
    }

    void dequeueFullBackupLocked(String packageName) {
        for (int i = this.mFullBackupQueue.size() + OP_TIMEOUT; i >= 0; i += OP_TIMEOUT) {
            if (packageName.equals(((FullBackupEntry) this.mFullBackupQueue.get(i)).packageName)) {
                this.mFullBackupQueue.remove(i);
            }
        }
    }

    void enqueueFullBackup(String packageName, long lastBackedUp) {
        FullBackupEntry newEntry = new FullBackupEntry(this, packageName, lastBackedUp);
        synchronized (this.mQueueLock) {
            dequeueFullBackupLocked(packageName);
            int which = OP_TIMEOUT;
            if (lastBackedUp > 0) {
                which = this.mFullBackupQueue.size() + OP_TIMEOUT;
                while (which >= 0) {
                    if (((FullBackupEntry) this.mFullBackupQueue.get(which)).lastBackup <= lastBackedUp) {
                        this.mFullBackupQueue.add(which + SCHEDULE_FILE_VERSION, newEntry);
                        break;
                    }
                    which += OP_TIMEOUT;
                }
            }
            if (which < 0) {
                this.mFullBackupQueue.add(OP_PENDING, newEntry);
            }
        }
        writeFullBackupScheduleAsync();
    }

    private boolean fullBackupAllowable(IBackupTransport transport) {
        if (transport == null) {
            Slog.w(TAG, "Transport not present; full data backup not performed");
            return MORE_DEBUG;
        }
        try {
            if (new File(new File(this.mBaseStateDir, transport.transportDirName()), PACKAGE_MANAGER_SENTINEL).length() > 0) {
                return DEBUG_BACKUP_TRACE;
            }
            if (DEBUG) {
                Slog.i(TAG, "Full backup requested but dataset not yet initialized");
            }
            return MORE_DEBUG;
        } catch (Exception e) {
            Slog.w(TAG, "Unable to contact transport");
            return MORE_DEBUG;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean beginFullBackup(FullBackupJob scheduledJob) {
        long now = System.currentTimeMillis();
        FullBackupEntry entry = null;
        long latency = MIN_FULL_BACKUP_INTERVAL;
        if (!this.mEnabled || !this.mProvisioned) {
            return MORE_DEBUG;
        }
        if (this.mPowerManager.isPowerSaveMode()) {
            if (DEBUG) {
                Slog.i(TAG, "Deferring scheduled full backups in battery saver mode");
            }
            FullBackupJob.schedule(this.mContext, 14400000);
            return MORE_DEBUG;
        }
        if (DEBUG_SCHEDULING) {
            Slog.i(TAG, "Beginning scheduled full backup operation");
        }
        synchronized (this.mQueueLock) {
            if (this.mRunningFullBackupTask != null) {
                Slog.e(TAG, "Backup triggered but one already/still running!");
                return MORE_DEBUG;
            }
            boolean z = DEBUG_BACKUP_TRACE;
            while (true) {
                if (this.mFullBackupQueue.size() != 0) {
                    boolean headBusy = MORE_DEBUG;
                    if (!fullBackupAllowable(getTransport(this.mCurrentTransport))) {
                        z = MORE_DEBUG;
                        latency = 14400000;
                    }
                    if (z) {
                        entry = (FullBackupEntry) this.mFullBackupQueue.get(OP_PENDING);
                        long timeSinceRun = now - entry.lastBackup;
                        z = timeSinceRun >= MIN_FULL_BACKUP_INTERVAL ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
                        if (!z) {
                            break;
                        }
                        try {
                            PackageInfo appInfo = this.mPackageManager.getPackageInfo(entry.packageName, OP_PENDING);
                            if (appGetsFullBackup(appInfo)) {
                                if ((appInfo.applicationInfo.privateFlags & DumpState.DUMP_PREFERRED) == 0) {
                                    headBusy = this.mActivityManager.isAppForeground(appInfo.applicationInfo.uid);
                                } else {
                                    headBusy = MORE_DEBUG;
                                }
                                if (headBusy) {
                                    long nextEligible = (System.currentTimeMillis() + TRANSPORT_RETRY_INTERVAL) + ((long) this.mTokenGenerator.nextInt(BUSY_BACKOFF_FUZZ));
                                    if (DEBUG_SCHEDULING) {
                                        Slog.i(TAG, "Full backup time but " + entry.packageName + " is busy; deferring to " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(nextEligible)));
                                    }
                                    enqueueFullBackup(entry.packageName, nextEligible - MIN_FULL_BACKUP_INTERVAL);
                                }
                            } else {
                                this.mFullBackupQueue.remove(OP_PENDING);
                                headBusy = DEBUG_BACKUP_TRACE;
                            }
                        } catch (NameNotFoundException e) {
                            z = this.mFullBackupQueue.size() > SCHEDULE_FILE_VERSION ? DEBUG_BACKUP_TRACE : MORE_DEBUG;
                        } catch (RemoteException e2) {
                        }
                    }
                    if (!headBusy) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    void endFullBackup() {
        synchronized (this.mQueueLock) {
            if (this.mRunningFullBackupTask != null) {
                if (DEBUG_SCHEDULING) {
                    Slog.i(TAG, "Telling running backup to stop");
                }
                this.mRunningFullBackupTask.setRunning(MORE_DEBUG);
            }
        }
    }

    static boolean signaturesMatch(Signature[] storedSigs, PackageInfo target) {
        if (target == null) {
            return MORE_DEBUG;
        }
        if ((target.applicationInfo.flags & SCHEDULE_FILE_VERSION) != 0) {
            return DEBUG_BACKUP_TRACE;
        }
        Signature[] deviceSigs = target.signatures;
        if ((storedSigs == null || storedSigs.length == 0) && (deviceSigs == null || deviceSigs.length == 0)) {
            return DEBUG_BACKUP_TRACE;
        }
        if (storedSigs == null || deviceSigs == null) {
            return MORE_DEBUG;
        }
        int nStored = storedSigs.length;
        int nDevice = deviceSigs.length;
        for (int i = OP_PENDING; i < nStored; i += SCHEDULE_FILE_VERSION) {
            boolean match = MORE_DEBUG;
            for (int j = OP_PENDING; j < nDevice; j += SCHEDULE_FILE_VERSION) {
                if (storedSigs[i].equals(deviceSigs[j])) {
                    match = DEBUG_BACKUP_TRACE;
                    break;
                }
            }
            if (!match) {
                return MORE_DEBUG;
            }
        }
        return DEBUG_BACKUP_TRACE;
    }

    void restoreWidgetData(String packageName, byte[] widgetData) {
        AppWidgetBackupBridge.restoreWidgetState(packageName, widgetData, OP_PENDING);
    }

    private void dataChangedImpl(String packageName) {
        dataChangedImpl(packageName, dataChangedTargets(packageName));
    }

    private void dataChangedImpl(String packageName, HashSet<String> targets) {
        if (targets == null) {
            Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "'" + " uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mQueueLock) {
            if (targets.contains(packageName)) {
                if (this.mPendingBackups.put(packageName, new BackupRequest(packageName)) == null) {
                    writeToJournalLocked(packageName);
                }
            }
        }
        KeyValueBackupJob.schedule(this.mContext);
    }

    private HashSet<String> dataChangedTargets(String packageName) {
        if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == OP_TIMEOUT) {
            HashSet<String> hashSet;
            synchronized (this.mBackupParticipants) {
                hashSet = (HashSet) this.mBackupParticipants.get(Binder.getCallingUid());
            }
            return hashSet;
        }
        HashSet<String> targets = new HashSet();
        if (PACKAGE_MANAGER_SENTINEL.equals(packageName)) {
            targets.add(PACKAGE_MANAGER_SENTINEL);
        } else {
            synchronized (this.mBackupParticipants) {
                int N = this.mBackupParticipants.size();
                for (int i = OP_PENDING; i < N; i += SCHEDULE_FILE_VERSION) {
                    HashSet<String> s = (HashSet) this.mBackupParticipants.valueAt(i);
                    if (s != null) {
                        targets.addAll(s);
                    }
                }
            }
        }
        return targets;
    }

    private void writeToJournalLocked(String str) {
        IOException e;
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        try {
            if (this.mJournal == null) {
                this.mJournal = File.createTempFile("journal", null, this.mJournalDir);
            }
            RandomAccessFile out = new RandomAccessFile(this.mJournal, "rws");
            try {
                out.seek(out.length());
                out.writeUTF(str);
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e2) {
                    }
                }
                randomAccessFile = out;
            } catch (IOException e3) {
                e = e3;
                randomAccessFile = out;
                try {
                    Slog.e(TAG, "Can't write " + str + " to backup journal", e);
                    this.mJournal = null;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = out;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Slog.e(TAG, "Can't write " + str + " to backup journal", e);
            this.mJournal = null;
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    public void dataChanged(String packageName) {
        if (UserHandle.getCallingUserId() == 0) {
            HashSet<String> targets = dataChangedTargets(packageName);
            if (targets == null) {
                Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "'" + " uid=" + Binder.getCallingUid());
            } else {
                this.mBackupHandler.post(new AnonymousClass5(packageName, targets));
            }
        }
    }

    public void clearBackupData(String transportName, String packageName) {
        if (DEBUG) {
            Slog.v(TAG, "clearBackupData() of " + packageName + " on " + transportName);
        }
        try {
            HashSet<String> apps;
            PackageInfo info = this.mPackageManager.getPackageInfo(packageName, 64);
            if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == OP_TIMEOUT) {
                apps = (HashSet) this.mBackupParticipants.get(Binder.getCallingUid());
            } else {
                apps = new HashSet();
                int N = this.mBackupParticipants.size();
                for (int i = OP_PENDING; i < N; i += SCHEDULE_FILE_VERSION) {
                    HashSet<String> s = (HashSet) this.mBackupParticipants.valueAt(i);
                    if (s != null) {
                        apps.addAll(s);
                    }
                }
            }
            if (apps.contains(packageName)) {
                this.mBackupHandler.removeMessages(MSG_RETRY_CLEAR);
                synchronized (this.mQueueLock) {
                    IBackupTransport transport = getTransport(transportName);
                    if (transport == null) {
                        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(MSG_RETRY_CLEAR, new ClearRetryParams(this, transportName, packageName)), TRANSPORT_RETRY_INTERVAL);
                        return;
                    }
                    long oldId = Binder.clearCallingIdentity();
                    this.mWakelock.acquire();
                    this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(MSG_RUN_CLEAR, new ClearParams(this, transport, info)));
                    Binder.restoreCallingIdentity(oldId);
                }
            }
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "No such package '" + packageName + "' - not clearing backup data");
        }
    }

    public void backupNow() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "backupNow");
        if (this.mPowerManager.isPowerSaveMode()) {
            if (DEBUG) {
                Slog.v(TAG, "Not running backup while in battery save mode");
            }
            KeyValueBackupJob.schedule(this.mContext);
            return;
        }
        if (DEBUG) {
            Slog.v(TAG, "Scheduling immediate backup pass");
        }
        synchronized (this.mQueueLock) {
            try {
                this.mRunBackupIntent.send();
            } catch (CanceledException e) {
                Slog.e(TAG, "run-backup intent cancelled!");
            }
            KeyValueBackupJob.cancel(this.mContext);
        }
    }

    boolean deviceIsProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", OP_PENDING) != 0) {
            return DEBUG_BACKUP_TRACE;
        }
        return MORE_DEBUG;
    }

    public void fullBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, String[] pkgList) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "fullBackup");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Backup supported only for the device owner");
        } else if (doAllApps || includeShared || !(pkgList == null || pkgList.length == 0)) {
            long oldId = Binder.clearCallingIdentity();
            try {
                if (deviceIsProvisioned()) {
                    if (DEBUG) {
                        Slog.v(TAG, "Requesting full backup: apks=" + includeApks + " obb=" + includeObbs + " shared=" + includeShared + " all=" + doAllApps + " system=" + includeSystem + " pkgs=" + pkgList);
                    }
                    Slog.i(TAG, "Beginning full backup...");
                    FullBackupParams params = new FullBackupParams(this, fd, includeApks, includeObbs, includeShared, doWidgets, doAllApps, includeSystem, compress, pkgList);
                    int token = generateToken();
                    synchronized (this.mFullConfirmations) {
                        this.mFullConfirmations.put(token, params);
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "Starting backup confirmation UI, token=" + token);
                    }
                    if (startConfirmationUi(token, "fullback")) {
                        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), OP_PENDING, OP_PENDING);
                        startConfirmationTimeout(token, params);
                        if (DEBUG) {
                            Slog.d(TAG, "Waiting for full backup completion...");
                        }
                        waitForCompletion(params);
                        try {
                            fd.close();
                        } catch (IOException e) {
                        }
                        Binder.restoreCallingIdentity(oldId);
                        Slog.d(TAG, "Full backup processing complete.");
                        return;
                    }
                    Slog.e(TAG, "Unable to launch full backup confirmation");
                    this.mFullConfirmations.delete(token);
                    try {
                        fd.close();
                    } catch (IOException e2) {
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.d(TAG, "Full backup processing complete.");
                    return;
                }
                Slog.i(TAG, "Full backup not supported before setup");
            } finally {
                try {
                    fd.close();
                } catch (IOException e3) {
                }
                Binder.restoreCallingIdentity(oldId);
                Slog.d(TAG, "Full backup processing complete.");
            }
        } else {
            throw new IllegalArgumentException("Backup requested but neither shared nor any apps named");
        }
    }

    public void fullTransportBackup(String[] pkgNames) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "fullTransportBackup");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Restore supported only for the device owner");
        }
        if (fullBackupAllowable(getTransport(this.mCurrentTransport))) {
            if (DEBUG) {
                Slog.d(TAG, "fullTransportBackup()");
            }
            long oldId = Binder.clearCallingIdentity();
            try {
                CountDownLatch latch = new CountDownLatch(SCHEDULE_FILE_VERSION);
                PerformFullTransportBackupTask task = new PerformFullTransportBackupTask(this, null, pkgNames, MORE_DEBUG, null, latch, null, MORE_DEBUG);
                this.mWakelock.acquire();
                new Thread(task, "full-transport-master").start();
                while (true) {
                    try {
                        latch.await();
                        break;
                    } catch (InterruptedException e) {
                    }
                }
                long now = System.currentTimeMillis();
                int length = pkgNames.length;
                for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
                    enqueueFullBackup(pkgNames[i], now);
                }
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        } else {
            Slog.i(TAG, "Full backup not currently possible -- key/value backup not yet run?");
        }
        if (DEBUG) {
            Slog.d(TAG, "Done with full transport backup.");
        }
    }

    public void fullRestore(ParcelFileDescriptor fd) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "fullRestore");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Restore supported only for the device owner");
        }
        long oldId = Binder.clearCallingIdentity();
        try {
            if (deviceIsProvisioned()) {
                Slog.i(TAG, "Beginning full restore...");
                FullRestoreParams params = new FullRestoreParams(this, fd);
                int token = generateToken();
                synchronized (this.mFullConfirmations) {
                    this.mFullConfirmations.put(token, params);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Starting restore confirmation UI, token=" + token);
                }
                if (startConfirmationUi(token, "fullrest")) {
                    this.mPowerManager.userActivity(SystemClock.uptimeMillis(), OP_PENDING, OP_PENDING);
                    startConfirmationTimeout(token, params);
                    if (DEBUG) {
                        Slog.d(TAG, "Waiting for full restore completion...");
                    }
                    waitForCompletion(params);
                    try {
                        fd.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "Error trying to close fd after full restore: " + e);
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.i(TAG, "Full restore processing complete.");
                    return;
                }
                Slog.e(TAG, "Unable to launch full restore confirmation");
                this.mFullConfirmations.delete(token);
                try {
                    fd.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "Error trying to close fd after full restore: " + e2);
                }
                Binder.restoreCallingIdentity(oldId);
                Slog.i(TAG, "Full restore processing complete.");
                return;
            }
            Slog.i(TAG, "Full restore not permitted before setup");
        } finally {
            try {
                fd.close();
            } catch (IOException e22) {
                Slog.w(TAG, "Error trying to close fd after full restore: " + e22);
            }
            Binder.restoreCallingIdentity(oldId);
            Slog.i(TAG, "Full restore processing complete.");
        }
    }

    boolean startConfirmationUi(int token, String action) {
        try {
            Intent confIntent = new Intent(action);
            confIntent.setClassName("com.android.backupconfirm", "com.android.backupconfirm.BackupRestoreConfirmation");
            confIntent.putExtra("conftoken", token);
            confIntent.addFlags(268435456);
            this.mContext.startActivityAsUser(confIntent, UserHandle.SYSTEM);
            return DEBUG_BACKUP_TRACE;
        } catch (ActivityNotFoundException e) {
            return MORE_DEBUG;
        }
    }

    void startConfirmationTimeout(int token, FullParams params) {
        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(MSG_FULL_CONFIRMATION_TIMEOUT, token, OP_PENDING, params), TIMEOUT_RESTORE_INTERVAL);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void waitForCompletion(FullParams params) {
        synchronized (params.latch) {
            while (true) {
                if (params.latch.get()) {
                } else {
                    try {
                        params.latch.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    void signalFullBackupRestoreCompletion(FullParams params) {
        synchronized (params.latch) {
            params.latch.set(DEBUG_BACKUP_TRACE);
            params.latch.notifyAll();
        }
    }

    public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encPpassword, IFullBackupRestoreObserver observer) {
        if (DEBUG) {
            Slog.d(TAG, "acknowledgeFullBackupOrRestore : token=" + token + " allow=" + allow);
        }
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "acknowledgeFullBackupOrRestore");
        long oldId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mFullConfirmations) {
                FullParams params = (FullParams) this.mFullConfirmations.get(token);
                if (params != null) {
                    this.mBackupHandler.removeMessages(MSG_FULL_CONFIRMATION_TIMEOUT, params);
                    this.mFullConfirmations.delete(token);
                    if (allow) {
                        int verb;
                        if (params instanceof FullBackupParams) {
                            verb = MSG_RUN_ADB_BACKUP;
                        } else {
                            verb = MSG_RUN_ADB_RESTORE;
                        }
                        params.observer = observer;
                        params.curPassword = curPassword;
                        params.encryptPassword = encPpassword;
                        this.mWakelock.acquire();
                        this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(verb, params));
                    } else {
                        Slog.w(TAG, "User rejected full backup/restore operation");
                        signalFullBackupRestoreCompletion(params);
                    }
                } else {
                    Slog.w(TAG, "Attempted to ack full backup/restore with invalid token");
                }
            }
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    private static boolean backupSettingMigrated(int userId) {
        return new File(new File(Environment.getDataDirectory(), "backup"), BACKUP_ENABLE_FILE).exists();
    }

    private static boolean readBackupEnableState(int userId) {
        Throwable th;
        Throwable th2 = null;
        File enableFile = new File(new File(Environment.getDataDirectory(), "backup"), BACKUP_ENABLE_FILE);
        if (enableFile.exists()) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream fin = new FileInputStream(enableFile);
                try {
                    boolean z;
                    if (fin.read() != 0) {
                        z = DEBUG_BACKUP_TRACE;
                    } else {
                        z = MORE_DEBUG;
                    }
                    if (fin != null) {
                        try {
                            fin.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 == null) {
                        return z;
                    }
                    try {
                        throw th2;
                    } catch (IOException e) {
                        fileInputStream = fin;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = fin;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            Slog.e(TAG, "Cannot read enable state; assuming disabled");
                            return MORE_DEBUG;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (th2 == null) {
                    throw th;
                }
                throw th2;
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "isBackupEnabled() => false due to absent settings file");
        }
        return MORE_DEBUG;
    }

    private static void writeBackupEnableState(boolean enable, int userId) {
        Exception e;
        Object obj;
        Throwable th;
        File base = new File(Environment.getDataDirectory(), "backup");
        File enableFile = new File(base, BACKUP_ENABLE_FILE);
        File stage = new File(base, "backup_enabled-stage");
        AutoCloseable autoCloseable = null;
        try {
            FileOutputStream fout = new FileOutputStream(stage);
            try {
                fout.write(enable ? SCHEDULE_FILE_VERSION : OP_PENDING);
                fout.close();
                stage.renameTo(enableFile);
                IoUtils.closeQuietly(fout);
                FileOutputStream fileOutputStream = fout;
            } catch (IOException e2) {
                e = e2;
                obj = fout;
                try {
                    Slog.e(TAG, "Unable to record backup enable state; reverting to disabled: " + e.getMessage());
                    Secure.putStringForUser(sInstance.mContext.getContentResolver(), BACKUP_ENABLE_FILE, null, userId);
                    enableFile.delete();
                    stage.delete();
                    IoUtils.closeQuietly(autoCloseable);
                } catch (Throwable th2) {
                    th = th2;
                    IoUtils.closeQuietly(autoCloseable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                obj = fout;
                IoUtils.closeQuietly(autoCloseable);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Slog.e(TAG, "Unable to record backup enable state; reverting to disabled: " + e.getMessage());
            Secure.putStringForUser(sInstance.mContext.getContentResolver(), BACKUP_ENABLE_FILE, null, userId);
            enableFile.delete();
            stage.delete();
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public void setBackupEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupEnabled");
        Slog.i(TAG, "Backup enabled => " + enable);
        long oldId = Binder.clearCallingIdentity();
        try {
            boolean wasEnabled = this.mEnabled;
            synchronized (this) {
                writeBackupEnableState(enable, OP_PENDING);
                this.mEnabled = enable;
            }
            synchronized (this.mQueueLock) {
                if (enable && !wasEnabled) {
                    if (this.mProvisioned) {
                        KeyValueBackupJob.schedule(this.mContext);
                        scheduleNextFullBackupJob(0);
                    }
                }
                if (!enable) {
                    KeyValueBackupJob.cancel(this.mContext);
                    if (wasEnabled && this.mProvisioned) {
                        synchronized (this.mTransports) {
                            HashSet<String> allTransports = new HashSet(this.mTransports.keySet());
                        }
                        for (String transport : allTransports) {
                            recordInitPendingLocked(DEBUG_BACKUP_TRACE, transport);
                        }
                        this.mAlarmManager.set(OP_PENDING, System.currentTimeMillis(), this.mRunInitIntent);
                    }
                }
            }
            Binder.restoreCallingIdentity(oldId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    public void setAutoRestore(boolean doAutoRestore) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setAutoRestore");
        Slog.i(TAG, "Auto restore => " + doAutoRestore);
        long oldId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                Secure.putInt(this.mContext.getContentResolver(), "backup_auto_restore", doAutoRestore ? SCHEDULE_FILE_VERSION : OP_PENDING);
                this.mAutoRestore = doAutoRestore;
            }
        } finally {
            Binder.restoreCallingIdentity(oldId);
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
        return this.mCurrentTransport;
    }

    public String[] listAllTransports() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "listAllTransports");
        ArrayList<String> known = new ArrayList();
        for (Entry<String, IBackupTransport> entry : this.mTransports.entrySet()) {
            if (entry.getValue() != null) {
                known.add((String) entry.getKey());
            }
        }
        if (known.size() <= 0) {
            return null;
        }
        String[] list = new String[known.size()];
        known.toArray(list);
        return list;
    }

    public String[] getTransportWhitelist() {
        String[] whitelist = new String[this.mTransportWhitelist.size()];
        for (int i = this.mTransportWhitelist.size() + OP_TIMEOUT; i >= 0; i += OP_TIMEOUT) {
            whitelist[i] = ((ComponentName) this.mTransportWhitelist.valueAt(i)).flattenToShortString();
        }
        return whitelist;
    }

    public String selectBackupTransport(String transport) {
        String prevTransport;
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "selectBackupTransport");
        synchronized (this.mTransports) {
            long oldId = Binder.clearCallingIdentity();
            try {
                prevTransport = this.mCurrentTransport;
                this.mCurrentTransport = transport;
                Secure.putString(this.mContext.getContentResolver(), "backup_transport", transport);
                Slog.v(TAG, "selectBackupTransport() set " + this.mCurrentTransport + " returning " + prevTransport);
                Binder.restoreCallingIdentity(oldId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(oldId);
            }
        }
        return prevTransport;
    }

    public Intent getConfigurationIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getConfigurationIntent");
        synchronized (this.mTransports) {
            IBackupTransport transport = (IBackupTransport) this.mTransports.get(transportName);
            if (transport != null) {
                try {
                    Intent intent = transport.configurationIntent();
                    return intent;
                } catch (RemoteException e) {
                }
            }
            return null;
        }
    }

    public String getDestinationString(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDestinationString");
        synchronized (this.mTransports) {
            IBackupTransport transport = (IBackupTransport) this.mTransports.get(transportName);
            if (transport != null) {
                try {
                    String text = transport.currentDestinationString();
                    return text;
                } catch (RemoteException e) {
                }
            }
            return null;
        }
    }

    public Intent getDataManagementIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementIntent");
        synchronized (this.mTransports) {
            IBackupTransport transport = (IBackupTransport) this.mTransports.get(transportName);
            if (transport != null) {
                try {
                    Intent intent = transport.dataManagementIntent();
                    return intent;
                } catch (RemoteException e) {
                }
            }
            return null;
        }
    }

    public String getDataManagementLabel(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementLabel");
        synchronized (this.mTransports) {
            IBackupTransport transport = (IBackupTransport) this.mTransports.get(transportName);
            if (transport != null) {
                try {
                    String text = transport.dataManagementLabel();
                    return text;
                } catch (RemoteException e) {
                }
            }
            return null;
        }
    }

    public void agentConnected(String packageName, IBinder agentBinder) {
        synchronized (this.mAgentConnectLock) {
            if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                Slog.d(TAG, "agentConnected pkg=" + packageName + " agent=" + agentBinder);
                this.mConnectedAgent = IBackupAgent.Stub.asInterface(agentBinder);
                this.mConnecting = MORE_DEBUG;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " claiming agent connected");
            }
            this.mAgentConnectLock.notifyAll();
        }
    }

    public void agentDisconnected(String packageName) {
        synchronized (this.mAgentConnectLock) {
            if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                this.mConnectedAgent = null;
                this.mConnecting = MORE_DEBUG;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " claiming agent disconnected");
            }
            this.mAgentConnectLock.notifyAll();
        }
    }

    public void restoreAtInstall(String packageName, int token) {
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid() + " attemping install-time restore");
            return;
        }
        boolean skip = MORE_DEBUG;
        long restoreSet = getAvailableRestoreToken(packageName);
        if (DEBUG) {
            Slog.v(TAG, "restoreAtInstall pkg=" + packageName + " token=" + Integer.toHexString(token) + " restoreSet=" + Long.toHexString(restoreSet));
        }
        if (restoreSet == 0) {
            skip = DEBUG_BACKUP_TRACE;
        }
        IBackupTransport transport = getTransport(this.mCurrentTransport);
        if (transport == null) {
            if (DEBUG) {
                Slog.w(TAG, "No transport");
            }
            skip = DEBUG_BACKUP_TRACE;
        }
        if (!this.mAutoRestore) {
            if (DEBUG) {
                Slog.w(TAG, "Non-restorable state: auto=" + this.mAutoRestore);
            }
            skip = DEBUG_BACKUP_TRACE;
        }
        if (!skip) {
            try {
                String dirName = transport.transportDirName();
                this.mWakelock.acquire();
                Message msg = this.mBackupHandler.obtainMessage(MSG_RUN_RESTORE);
                msg.obj = new RestoreParams(this, transport, dirName, null, restoreSet, packageName, token);
                this.mBackupHandler.sendMessage(msg);
            } catch (RemoteException e) {
                Slog.e(TAG, "Unable to contact transport");
                skip = DEBUG_BACKUP_TRACE;
            }
        }
        if (skip) {
            if (DEBUG) {
                Slog.v(TAG, "Finishing install immediately");
            }
            try {
                this.mPackageManagerBinder.finishPackageInstall(token, MORE_DEBUG);
            } catch (RemoteException e2) {
            }
        }
    }

    public IRestoreSession beginRestoreSession(String packageName, String transport) {
        if (DEBUG) {
            Slog.v(TAG, "beginRestoreSession: pkg=" + packageName + " transport=" + transport);
        }
        boolean needPermission = DEBUG_BACKUP_TRACE;
        if (transport == null) {
            transport = this.mCurrentTransport;
            if (packageName != null) {
                try {
                    if (this.mPackageManager.getPackageInfo(packageName, OP_PENDING).applicationInfo.uid == Binder.getCallingUid()) {
                        needPermission = MORE_DEBUG;
                    }
                } catch (NameNotFoundException e) {
                    Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                    throw new IllegalArgumentException("Package " + packageName + " not found");
                }
            }
        }
        if (needPermission) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "beginRestoreSession");
        } else if (DEBUG) {
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
                this.mActiveRestoreSession = new ActiveRestoreSession(packageName, transport);
                this.mBackupHandler.sendEmptyMessageDelayed(MSG_RESTORE_TIMEOUT, TIMEOUT_RESTORE_INTERVAL);
                return this.mActiveRestoreSession;
            }
        }
    }

    void clearRestoreSession(ActiveRestoreSession currentSession) {
        synchronized (this) {
            if (currentSession != this.mActiveRestoreSession) {
                Slog.e(TAG, "ending non-current restore session");
            } else {
                if (DEBUG) {
                    Slog.v(TAG, "Clearing restore session and halting timeout");
                }
                this.mActiveRestoreSession = null;
                this.mBackupHandler.removeMessages(MSG_RESTORE_TIMEOUT);
            }
        }
    }

    public void opComplete(int token, long result) {
        synchronized (this.mCurrentOpLock) {
            Operation op = (Operation) this.mCurrentOperations.get(token);
            if (op != null) {
                if (op.state == OP_TIMEOUT) {
                    op = null;
                    this.mCurrentOperations.delete(token);
                } else {
                    op.state = SCHEDULE_FILE_VERSION;
                }
            }
            this.mCurrentOpLock.notifyAll();
        }
        if (op != null && op.callback != null) {
            this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(MSG_OP_COMPLETE, Pair.create(op.callback, Long.valueOf(result))));
        }
    }

    public boolean isAppEligibleForBackup(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "isAppEligibleForBackup");
        try {
            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 64);
            if (!appIsEligibleForBackup(packageInfo.applicationInfo) || appIsStopped(packageInfo.applicationInfo)) {
                return MORE_DEBUG;
            }
            IBackupTransport transport = getTransport(this.mCurrentTransport);
            if (transport != null) {
                try {
                    return transport.isAppEligibleForBackup(packageInfo, appGetsFullBackup(packageInfo));
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to contact transport");
                }
            }
            return DEBUG_BACKUP_TRACE;
        } catch (NameNotFoundException e2) {
            return MORE_DEBUG;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        long identityToken = Binder.clearCallingIdentity();
        if (args != null) {
            int i = OP_PENDING;
            int length = args.length;
            while (i < length) {
                String arg = args[i];
                if ("-h".equals(arg)) {
                    pw.println("'dumpsys backup' optional arguments:");
                    pw.println("  -h       : this help text");
                    pw.println("  a[gents] : dump information about defined backup agents");
                    Binder.restoreCallingIdentity(identityToken);
                    return;
                }
                try {
                    if ("agents".startsWith(arg)) {
                        dumpAgents(pw);
                        return;
                    }
                    i += SCHEDULE_FILE_VERSION;
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            }
        }
        dumpInternal(pw);
        Binder.restoreCallingIdentity(identityToken);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dumpInternal(PrintWriter pw) {
        synchronized (this.mQueueLock) {
            pw.println("Backup Manager is " + (this.mEnabled ? "enabled" : "disabled") + " / " + (!this.mProvisioned ? "not " : "") + "provisioned / " + (this.mPendingInits.size() == 0 ? "not " : "") + "pending init");
            pw.println("Auto-restore is " + (this.mAutoRestore ? "enabled" : "disabled"));
            if (this.mBackupRunning) {
                pw.println("Backup currently running");
            }
            pw.println("Last backup pass started: " + this.mLastBackupPass + " (now = " + System.currentTimeMillis() + ')');
            pw.println("  next scheduled: " + KeyValueBackupJob.nextScheduled());
            pw.println("Transport whitelist:");
            for (ComponentName transport : this.mTransportWhitelist) {
                pw.print("    ");
                pw.println(transport.flattenToShortString());
            }
            pw.println("Available transports:");
            if (listAllTransports() != null) {
                String[] listAllTransports = listAllTransports();
                int length = listAllTransports.length;
                for (int i = OP_PENDING; i < length; i += SCHEDULE_FILE_VERSION) {
                    String t = listAllTransports[i];
                    pw.println((t.equals(this.mCurrentTransport) ? "  * " : "    ") + t);
                    try {
                        IBackupTransport transport2 = getTransport(t);
                        File dir = new File(this.mBaseStateDir, transport2.transportDirName());
                        pw.println("       destination: " + transport2.currentDestinationString());
                        pw.println("       intent: " + transport2.configurationIntent());
                        File[] listFiles = dir.listFiles();
                        int length2 = listFiles.length;
                        for (int i2 = OP_PENDING; i2 < length2; i2 += SCHEDULE_FILE_VERSION) {
                            File f = listFiles[i2];
                            pw.println("       " + f.getName() + " - " + f.length() + " state bytes");
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "Error in transport", e);
                        pw.println("        Error: " + e);
                    }
                }
            }
            pw.println("Pending init: " + this.mPendingInits.size());
            for (String s : this.mPendingInits) {
                pw.println("    " + s);
            }
            synchronized (this.mBackupTrace) {
                if (!this.mBackupTrace.isEmpty()) {
                    pw.println("Most recent backup trace:");
                    for (String s2 : this.mBackupTrace) {
                        pw.println("   " + s2);
                    }
                }
            }
            pw.print("Ancestral: ");
            pw.println(Long.toHexString(this.mAncestralToken));
            pw.print("Current:   ");
            pw.println(Long.toHexString(this.mCurrentToken));
            int N = this.mBackupParticipants.size();
            pw.println("Participants:");
            for (int i3 = OP_PENDING; i3 < N; i3 += SCHEDULE_FILE_VERSION) {
                int uid = this.mBackupParticipants.keyAt(i3);
                pw.print("  uid: ");
                pw.println(uid);
                for (String app : (HashSet) this.mBackupParticipants.valueAt(i3)) {
                    pw.println("    " + app);
                }
            }
            pw.println("Ancestral packages: " + (this.mAncestralPackages == null ? "none" : Integer.valueOf(this.mAncestralPackages.size())));
            if (this.mAncestralPackages != null) {
                for (String pkg : this.mAncestralPackages) {
                    pw.println("    " + pkg);
                }
            }
            pw.println("Ever backed up: " + this.mEverStoredApps.size());
            for (String pkg2 : this.mEverStoredApps) {
                pw.println("    " + pkg2);
            }
            pw.println("Pending key/value backup: " + this.mPendingBackups.size());
            for (BackupRequest req : this.mPendingBackups.values()) {
                pw.println("    " + req);
            }
            pw.println("Full backup queue:" + this.mFullBackupQueue.size());
            for (FullBackupEntry entry : this.mFullBackupQueue) {
                pw.print("    ");
                pw.print(entry.lastBackup);
                pw.print(" : ");
                pw.println(entry.packageName);
            }
        }
    }

    private static void sendBackupOnUpdate(IBackupObserver observer, String packageName, BackupProgress progress) {
        if (observer != null) {
            try {
                observer.onUpdate(packageName, progress);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Backup observer went away: onUpdate");
                }
            }
        }
    }

    private static void sendBackupOnPackageResult(IBackupObserver observer, String packageName, int status) {
        if (observer != null) {
            try {
                observer.onResult(packageName, status);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Backup observer went away: onResult");
                }
            }
        }
    }

    private static void sendBackupFinished(IBackupObserver observer, int status) {
        if (observer != null) {
            try {
                observer.backupFinished(status);
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "Backup observer went away: backupFinished");
                }
            }
        }
    }
}
