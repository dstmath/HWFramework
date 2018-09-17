package com.android.server.backup;

import android.app.ActivityManager;
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
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.IRestoreSession.Stub;
import android.app.backup.ISelectBackupTransportCallback;
import android.app.backup.RestoreDescription;
import android.app.backup.RestoreSet;
import android.app.backup.SelectBackupTransportCallback;
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
import android.os.storage.IStorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.AtomicFile;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.backup.IObbBackupService;
import com.android.internal.util.DumpUtils;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.EventLogTags;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.backup.PackageManagerBackupAgent.Metadata;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.job.controllers.JobStatus;
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
import java.util.ArrayDeque;
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
import java.util.Queue;
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
    static final int BACKUP_FILE_VERSION = 5;
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
    static final boolean DEBUG;
    static final boolean DEBUG_BACKUP_TRACE = true;
    static final boolean DEBUG_SCHEDULING = DEBUG;
    static final String ENCRYPTION_ALGORITHM_NAME = "AES-256";
    static final String INIT_SENTINEL_FILE_NAME = "_need_init_";
    static final String KEY_WIDGET_STATE = "￭￭widget";
    static final long MIN_FULL_BACKUP_INTERVAL = 86400000;
    static final boolean MORE_DEBUG = false;
    private static final int MSG_BACKUP_OPERATION_TIMEOUT = 17;
    static final int MSG_BACKUP_RESTORE_STEP = 20;
    private static final int MSG_FULL_CONFIRMATION_TIMEOUT = 9;
    static final int MSG_OP_COMPLETE = 21;
    private static final int MSG_REQUEST_BACKUP = 15;
    private static final int MSG_RESTORE_OPERATION_TIMEOUT = 18;
    private static final int MSG_RESTORE_SESSION_TIMEOUT = 8;
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
    private static final int MSG_SCHEDULE_BACKUP_PACKAGE = 16;
    private static final int MSG_WIDGET_BROADCAST = 13;
    static final int OP_ACKNOWLEDGED = 1;
    static final int OP_PENDING = 0;
    static final int OP_TIMEOUT = -1;
    private static final int OP_TYPE_BACKUP = 2;
    static final int OP_TYPE_BACKUP_WAIT = 0;
    static final int OP_TYPE_RESTORE_WAIT = 1;
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
    static final int TAR_HEADER_LENGTH_FILESIZE = 12;
    static final int TAR_HEADER_LENGTH_MODE = 8;
    static final int TAR_HEADER_LENGTH_MODTIME = 12;
    static final int TAR_HEADER_LENGTH_PATH = 100;
    static final int TAR_HEADER_LENGTH_PATH_PREFIX = 155;
    static final int TAR_HEADER_LONG_RADIX = 8;
    static final int TAR_HEADER_OFFSET_FILESIZE = 124;
    static final int TAR_HEADER_OFFSET_MODE = 100;
    static final int TAR_HEADER_OFFSET_MODTIME = 136;
    static final int TAR_HEADER_OFFSET_PATH = 0;
    static final int TAR_HEADER_OFFSET_PATH_PREFIX = 345;
    static final int TAR_HEADER_OFFSET_TYPE_CHAR = 156;
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
    final SparseArray<AdbParams> mAdbBackupRestoreConfirmations = new SparseArray();
    final Object mAgentConnectLock = new Object();
    private AlarmManager mAlarmManager;
    Set<String> mAncestralPackages = null;
    long mAncestralToken = 0;
    boolean mAutoRestore;
    BackupHandler mBackupHandler;
    IBackupManager mBackupManagerBinder;
    final SparseArray<HashSet<String>> mBackupParticipants = new SparseArray();
    volatile boolean mBackupRunning;
    final List<String> mBackupTrace = new ArrayList();
    File mBaseStateDir;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String pkgName;
            String action = intent.getAction();
            boolean replacing = false;
            boolean added = false;
            Bundle extras = intent.getExtras();
            String[] pkgList = null;
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                Uri uri = intent.getData();
                if (uri != null) {
                    pkgName = uri.getSchemeSpecificPart();
                    if (pkgName != null) {
                        pkgList = new String[]{pkgName};
                    }
                    if ("android.intent.action.PACKAGE_CHANGED".equals(action)) {
                        BackupManagerService.this.mBackupHandler.post(new com.android.server.backup.-$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA.AnonymousClass3(this, pkgName, intent.getStringArrayExtra("android.intent.extra.changed_component_name_list")));
                        return;
                    }
                    added = "android.intent.action.PACKAGE_ADDED".equals(action);
                    replacing = extras.getBoolean("android.intent.extra.REPLACING", false);
                } else {
                    return;
                }
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(action)) {
                added = true;
                pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
            } else if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                added = false;
                pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
            }
            if (pkgList != null && pkgList.length != 0) {
                int uid = extras.getInt("android.intent.extra.UID");
                int i;
                int i2;
                if (added) {
                    synchronized (BackupManagerService.this.mBackupParticipants) {
                        if (replacing) {
                            BackupManagerService.this.removePackageParticipantsLocked(pkgList, uid);
                        }
                        BackupManagerService.this.addPackageParticipantsLocked(pkgList);
                    }
                    long now = System.currentTimeMillis();
                    i = 0;
                    int length = pkgList.length;
                    while (true) {
                        i2 = i;
                        if (i2 >= length) {
                            BackupManagerService.this.dataChangedImpl(BackupManagerService.PACKAGE_MANAGER_SENTINEL);
                            break;
                        }
                        String packageName = pkgList[i2];
                        try {
                            PackageInfo app = BackupManagerService.this.mPackageManager.getPackageInfo(packageName, 0);
                            if (BackupManagerService.appGetsFullBackup(app)) {
                                if (BackupManagerService.appIsEligibleForBackup(app.applicationInfo, BackupManagerService.this.mPackageManager)) {
                                    BackupManagerService.this.enqueueFullBackup(packageName, now);
                                    BackupManagerService.this.scheduleNextFullBackupJob(0);
                                    BackupManagerService.this.mBackupHandler.post(new com.android.server.backup.-$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA.AnonymousClass1(this, packageName));
                                    i = i2 + 1;
                                }
                            }
                            synchronized (BackupManagerService.this.mQueueLock) {
                                BackupManagerService.this.dequeueFullBackupLocked(packageName);
                            }
                            BackupManagerService.this.writeFullBackupScheduleAsync();
                            BackupManagerService.this.mBackupHandler.post(new com.android.server.backup.-$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA.AnonymousClass1(this, packageName));
                        } catch (NameNotFoundException e) {
                            if (BackupManagerService.DEBUG) {
                                Slog.w(BackupManagerService.TAG, "Can't resolve new app " + packageName);
                            }
                        }
                        i = i2 + 1;
                    }
                } else {
                    if (!replacing) {
                        synchronized (BackupManagerService.this.mBackupParticipants) {
                            BackupManagerService.this.removePackageParticipantsLocked(pkgList, uid);
                        }
                    }
                    for (String pkgName2 : pkgList) {
                        BackupManagerService.this.mBackupHandler.post(new com.android.server.backup.-$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA.AnonymousClass2(this, pkgName2));
                    }
                }
            }
        }

        /* synthetic */ void lambda$-com_android_server_backup_BackupManagerService$3_85227(String pkgName, String[] components) {
            BackupManagerService.this.mTransportManager.onPackageChanged(pkgName, components);
        }

        /* synthetic */ void lambda$-com_android_server_backup_BackupManagerService$3_87929(String packageName) {
            BackupManagerService.this.mTransportManager.onPackageAdded(packageName);
        }

        /* synthetic */ void lambda$-com_android_server_backup_BackupManagerService$3_89142(String pkgName) {
            BackupManagerService.this.mTransportManager.onPackageRemoved(pkgName);
        }
    };
    final Object mClearDataLock = new Object();
    volatile boolean mClearingData;
    IBackupAgent mConnectedAgent;
    volatile boolean mConnecting;
    Context mContext;
    final Object mCurrentOpLock = new Object();
    @GuardedBy("mCurrentOpLock")
    final SparseArray<Operation> mCurrentOperations = new SparseArray();
    long mCurrentToken = 0;
    File mDataDir;
    boolean mEnabled;
    private File mEverStored;
    HashSet<String> mEverStoredApps = new HashSet();
    @GuardedBy("mQueueLock")
    ArrayList<FullBackupEntry> mFullBackupQueue;
    File mFullBackupScheduleFile;
    Runnable mFullBackupScheduleWriter = new Runnable() {
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
            return;
        }
    };
    HandlerThread mHandlerThread;
    @GuardedBy("mPendingRestores")
    private boolean mIsRestoreInProgress;
    File mJournal;
    File mJournalDir;
    volatile long mLastBackupPass;
    private PackageManager mPackageManager;
    IPackageManager mPackageManagerBinder;
    private String mPasswordHash;
    private File mPasswordHashFile;
    private byte[] mPasswordSalt;
    private int mPasswordVersion;
    private File mPasswordVersionFile;
    HashMap<String, BackupRequest> mPendingBackups = new HashMap();
    HashSet<String> mPendingInits = new HashSet();
    @GuardedBy("mPendingRestores")
    private final Queue<PerformUnifiedRestoreTask> mPendingRestores = new ArrayDeque();
    private PowerManager mPowerManager;
    boolean mProvisioned;
    ContentObserver mProvisionedObserver;
    final Object mQueueLock = new Object();
    private final SecureRandom mRng = new SecureRandom();
    PendingIntent mRunBackupIntent;
    BroadcastReceiver mRunBackupReceiver;
    PendingIntent mRunInitIntent;
    BroadcastReceiver mRunInitReceiver;
    @GuardedBy("mQueueLock")
    PerformFullTransportBackupTask mRunningFullBackupTask;
    private IStorageManager mStorageManager;
    File mTokenFile;
    final Random mTokenGenerator = new Random();
    private TransportBoundListener mTransportBoundListener = new TransportBoundListener() {
        public boolean onTransportBound(IBackupTransport transport) {
            String name = null;
            try {
                name = transport.name();
                File stateDir = new File(BackupManagerService.this.mBaseStateDir, transport.transportDirName());
                stateDir.mkdirs();
                if (new File(stateDir, BackupManagerService.INIT_SENTINEL_FILE_NAME).exists()) {
                    synchronized (BackupManagerService.this.mQueueLock) {
                        BackupManagerService.this.mPendingInits.add(name);
                        BackupManagerService.this.mAlarmManager.set(0, System.currentTimeMillis() + LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS, BackupManagerService.this.mRunInitIntent);
                    }
                }
                return true;
            } catch (Exception e) {
                Slog.w(BackupManagerService.TAG, "Failed to regiser transport: " + name);
                return false;
            }
        }
    };
    private final TransportManager mTransportManager;
    WakeLock mWakelock;

    class ActiveRestoreSession extends Stub {
        private static final String TAG = "RestoreSession";
        boolean mEnded = false;
        private String mPackageName;
        RestoreSet[] mRestoreSets = null;
        private IBackupTransport mRestoreTransport = null;
        boolean mTimedOut = false;

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
                    this.mSession.mEnded = true;
                }
                this.mBackupManager.clearRestoreSession(this.mSession);
            }
        }

        ActiveRestoreSession(String packageName, String transport) {
            this.mPackageName = packageName;
            this.mRestoreTransport = BackupManagerService.this.mTransportManager.getTransportBinder(transport);
        }

        public void markTimedOut() {
            this.mTimedOut = true;
        }

        /* JADX WARNING: Missing block: B:25:0x004e, code:
            return -1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreSets");
            if (observer == null) {
                throw new IllegalArgumentException("Observer must not be null");
            } else if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else {
                long oldId = Binder.clearCallingIdentity();
                try {
                    if (this.mRestoreTransport == null) {
                        Slog.w(TAG, "Null transport getting restore sets");
                    } else {
                        BackupManagerService.this.mBackupHandler.removeMessages(8);
                        BackupManagerService.this.mWakelock.acquire();
                        BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(6, new RestoreGetSetsParams(this.mRestoreTransport, this, observer, monitor)));
                        Binder.restoreCallingIdentity(oldId);
                        return 0;
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Error in getAvailableRestoreSets", e);
                    return -1;
                } finally {
                    Binder.restoreCallingIdentity(oldId);
                }
            }
        }

        public synchronized int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
            if (BackupManagerService.DEBUG) {
                Slog.d(TAG, "restoreAll token=" + Long.toHexString(token) + " observer=" + observer);
            }
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else if (this.mRestoreTransport == null || this.mRestoreSets == null) {
                Slog.e(TAG, "Ignoring restoreAll() with no restore set");
                return -1;
            } else if (this.mPackageName != null) {
                Slog.e(TAG, "Ignoring restoreAll() on single-package session");
                return -1;
            } else {
                try {
                    String dirName = this.mRestoreTransport.transportDirName();
                    synchronized (BackupManagerService.this.mQueueLock) {
                        for (RestoreSet restoreSet : this.mRestoreSets) {
                            if (token == restoreSet.token) {
                                BackupManagerService.this.mBackupHandler.removeMessages(8);
                                long oldId = Binder.clearCallingIdentity();
                                BackupManagerService.this.mWakelock.acquire();
                                Message msg = BackupManagerService.this.mBackupHandler.obtainMessage(3);
                                msg.obj = new RestoreParams(this.mRestoreTransport, dirName, observer, monitor, token);
                                BackupManagerService.this.mBackupHandler.sendMessage(msg);
                                Binder.restoreCallingIdentity(oldId);
                                return 0;
                            }
                        }
                        Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                        return -1;
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Unable to get transport dir for restore: " + e.getMessage());
                    return -1;
                }
            }
        }

        public synchronized int restoreSome(long token, IRestoreObserver observer, IBackupManagerMonitor monitor, String[] packages) {
            BackupManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
            if (BackupManagerService.DEBUG) {
                StringBuilder b = new StringBuilder(128);
                b.append("restoreSome token=");
                b.append(Long.toHexString(token));
                b.append(" observer=");
                b.append(observer.toString());
                b.append(" monitor=");
                if (monitor == null) {
                    b.append("null");
                } else {
                    b.append(monitor.toString());
                }
                b.append(" packages=");
                if (packages == null) {
                    b.append("null");
                } else {
                    b.append('{');
                    boolean first = true;
                    for (String s : packages) {
                        if (first) {
                            first = false;
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
                return -1;
            } else if (this.mRestoreTransport == null || this.mRestoreSets == null) {
                Slog.e(TAG, "Ignoring restoreAll() with no restore set");
                return -1;
            } else if (this.mPackageName != null) {
                Slog.e(TAG, "Ignoring restoreAll() on single-package session");
                return -1;
            } else {
                try {
                    String dirName = this.mRestoreTransport.transportDirName();
                    synchronized (BackupManagerService.this.mQueueLock) {
                        for (RestoreSet restoreSet : this.mRestoreSets) {
                            if (token == restoreSet.token) {
                                BackupManagerService.this.mBackupHandler.removeMessages(8);
                                long oldId = Binder.clearCallingIdentity();
                                BackupManagerService.this.mWakelock.acquire();
                                Message msg = BackupManagerService.this.mBackupHandler.obtainMessage(3);
                                msg.obj = new RestoreParams(this.mRestoreTransport, dirName, observer, monitor, token, packages, packages.length > 1);
                                BackupManagerService.this.mBackupHandler.sendMessage(msg);
                                Binder.restoreCallingIdentity(oldId);
                                return 0;
                            }
                        }
                        Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                        return -1;
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Unable to get transport name for restoreSome: " + e.getMessage());
                    return -1;
                }
            }
        }

        public synchronized int restorePackage(String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor) {
            if (BackupManagerService.DEBUG) {
                Slog.v(TAG, "restorePackage pkg=" + packageName + " obs=" + observer + "monitor=" + monitor);
            }
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else if (this.mPackageName == null || this.mPackageName.equals(packageName)) {
                try {
                    PackageInfo app = BackupManagerService.this.mPackageManager.getPackageInfo(packageName, 0);
                    if (BackupManagerService.this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) != -1 || app.applicationInfo.uid == Binder.getCallingUid()) {
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
                                return -1;
                            }
                            String dirName = this.mRestoreTransport.transportDirName();
                            BackupManagerService.this.mBackupHandler.removeMessages(8);
                            BackupManagerService.this.mWakelock.acquire();
                            Message msg = BackupManagerService.this.mBackupHandler.obtainMessage(3);
                            msg.obj = new RestoreParams(this.mRestoreTransport, dirName, observer, monitor, token, app);
                            BackupManagerService.this.mBackupHandler.sendMessage(msg);
                            return 0;
                        } catch (Exception e) {
                            Slog.e(TAG, "Unable to get transport dir for restorePackage: " + e.getMessage());
                            return -1;
                        } finally {
                            Binder.restoreCallingIdentity(oldId);
                        }
                    } else {
                        Slog.w(TAG, "restorePackage: bad packageName=" + packageName + " or calling uid=" + Binder.getCallingUid());
                        throw new SecurityException("No permission to restore other packages");
                    }
                } catch (NameNotFoundException e2) {
                    Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                    return -1;
                }
            } else {
                Slog.e(TAG, "Ignoring attempt to restore pkg=" + packageName + " on session for package " + this.mPackageName);
                return -1;
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

    class AdbParams {
        public String curPassword;
        public String encryptPassword;
        public ParcelFileDescriptor fd;
        public final AtomicBoolean latch = new AtomicBoolean(false);
        public IFullBackupRestoreObserver observer;

        AdbParams() {
        }
    }

    class AdbBackupParams extends AdbParams {
        public boolean allApps;
        public boolean doCompress;
        public boolean doWidgets;
        public boolean includeApks;
        public boolean includeKeyValue;
        public boolean includeObbs;
        public boolean includeShared;
        public boolean includeSystem;
        public String[] packages;

        AdbBackupParams(ParcelFileDescriptor output, boolean saveApks, boolean saveObbs, boolean saveShared, boolean alsoWidgets, boolean doAllApps, boolean doSystem, boolean compress, boolean doKeyValue, String[] pkgList) {
            super();
            this.fd = output;
            this.includeApks = saveApks;
            this.includeObbs = saveObbs;
            this.includeShared = saveShared;
            this.doWidgets = alsoWidgets;
            this.allApps = doAllApps;
            this.includeSystem = doSystem;
            this.doCompress = compress;
            this.includeKeyValue = doKeyValue;
            this.packages = pkgList;
        }
    }

    interface BackupRestoreTask {
        void execute();

        void handleCancel(boolean z);

        void operationComplete(long j);
    }

    class AdbRestoreFinishedLatch implements BackupRestoreTask {
        static final String TAG = "AdbRestoreFinishedLatch";
        private final int mCurrentOpToken;
        final CountDownLatch mLatch = new CountDownLatch(1);

        AdbRestoreFinishedLatch(int currentOpToken) {
            this.mCurrentOpToken = currentOpToken;
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
            BackupManagerService.this.removeOperation(this.mCurrentOpToken);
        }

        public void handleCancel(boolean cancelAll) {
            if (BackupManagerService.DEBUG) {
                Slog.w(TAG, "adb onRestoreFinished() timed out");
            }
            this.mLatch.countDown();
            BackupManagerService.this.removeOperation(this.mCurrentOpToken);
        }
    }

    class AdbRestoreParams extends AdbParams {
        AdbRestoreParams(ParcelFileDescriptor input) {
            super();
            this.fd = input;
        }
    }

    private class BackupHandler extends Handler {
        public BackupHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    BackupManagerService.this.mLastBackupPass = System.currentTimeMillis();
                    IBackupTransport transport = BackupManagerService.this.mTransportManager.getCurrentTransportBinder();
                    if (transport == null) {
                        Slog.v(BackupManagerService.TAG, "Backup requested but no transport available");
                        synchronized (BackupManagerService.this.mQueueLock) {
                            BackupManagerService.this.mBackupRunning = false;
                        }
                        BackupManagerService.this.mWakelock.release();
                        return;
                    }
                    ArrayList<BackupRequest> queue = new ArrayList();
                    File oldJournal = BackupManagerService.this.mJournal;
                    synchronized (BackupManagerService.this.mQueueLock) {
                        if (BackupManagerService.this.mPendingBackups.size() > 0) {
                            for (BackupRequest b : BackupManagerService.this.mPendingBackups.values()) {
                                queue.add(b);
                            }
                            if (BackupManagerService.DEBUG) {
                                Slog.v(BackupManagerService.TAG, "clearing pending backups");
                            }
                            BackupManagerService.this.mPendingBackups.clear();
                            BackupManagerService.this.mJournal = null;
                        }
                    }
                    boolean staged = true;
                    if (queue.size() > 0) {
                        try {
                            sendMessage(obtainMessage(20, new PerformBackupTask(transport, transport.transportDirName(), queue, oldJournal, null, null, Collections.emptyList(), false, false)));
                        } catch (Throwable e) {
                            Slog.e(BackupManagerService.TAG, "Transport became unavailable attempting backup or error initializing backup task", e);
                            staged = false;
                        }
                    } else {
                        Slog.v(BackupManagerService.TAG, "Backup requested but nothing pending");
                        staged = false;
                    }
                    if (!staged) {
                        synchronized (BackupManagerService.this.mQueueLock) {
                            BackupManagerService.this.mBackupRunning = false;
                        }
                        BackupManagerService.this.mWakelock.release();
                        return;
                    }
                    return;
                case 2:
                    AdbBackupParams params = msg.obj;
                    new Thread(new PerformAdbBackupTask(params.fd, params.observer, params.includeApks, params.includeObbs, params.includeShared, params.doWidgets, params.curPassword, params.encryptPassword, params.allApps, params.includeSystem, params.doCompress, params.includeKeyValue, params.packages, params.latch), "adb-backup").start();
                    return;
                case 3:
                    RestoreParams params2 = msg.obj;
                    Slog.d(BackupManagerService.TAG, "MSG_RUN_RESTORE observer=" + params2.observer);
                    PerformUnifiedRestoreTask task = new PerformUnifiedRestoreTask(params2.transport, params2.observer, params2.monitor, params2.token, params2.pkgInfo, params2.pmToken, params2.isSystemRestore, params2.filterSet);
                    synchronized (BackupManagerService.this.mPendingRestores) {
                        if (BackupManagerService.this.mIsRestoreInProgress) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Restore in progress, queueing.");
                            }
                            BackupManagerService.this.mPendingRestores.add(task);
                        } else {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Starting restore.");
                            }
                            BackupManagerService.this.mIsRestoreInProgress = true;
                            sendMessage(obtainMessage(20, task));
                        }
                    }
                    return;
                case 4:
                    ClearParams params3 = msg.obj;
                    new PerformClearTask(params3.transport, params3.packageInfo).run();
                    return;
                case 5:
                    HashSet<String> hashSet;
                    synchronized (BackupManagerService.this.mQueueLock) {
                        hashSet = new HashSet(BackupManagerService.this.mPendingInits);
                        BackupManagerService.this.mPendingInits.clear();
                    }
                    new PerformInitializeTask(hashSet).run();
                    return;
                case 6:
                    RestoreSet[] sets = null;
                    RestoreGetSetsParams params4 = msg.obj;
                    try {
                        sets = params4.transport.getAvailableRestoreSets();
                        synchronized (params4.session) {
                            params4.session.mRestoreSets = sets;
                        }
                        if (sets == null) {
                            EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                        }
                        if (params4.observer != null) {
                            try {
                                params4.observer.restoreSetsAvailable(sets);
                            } catch (RemoteException e2) {
                                Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                            } catch (Exception e3) {
                                Slog.e(BackupManagerService.TAG, "Restore observer threw: " + e3.getMessage());
                            }
                        }
                        removeMessages(8);
                        sendEmptyMessageDelayed(8, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
                        BackupManagerService.this.mWakelock.release();
                        return;
                    } catch (Exception e32) {
                        try {
                            Slog.e(BackupManagerService.TAG, "Error from transport getting set list: " + e32.getMessage());
                            if (params4.observer != null) {
                                try {
                                    params4.observer.restoreSetsAvailable(sets);
                                } catch (RemoteException e4) {
                                    Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                                } catch (Exception e322) {
                                    Slog.e(BackupManagerService.TAG, "Restore observer threw: " + e322.getMessage());
                                }
                            }
                            removeMessages(8);
                            sendEmptyMessageDelayed(8, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
                            BackupManagerService.this.mWakelock.release();
                            return;
                        } catch (Throwable th) {
                            if (params4.observer != null) {
                                try {
                                    params4.observer.restoreSetsAvailable(sets);
                                } catch (RemoteException e5) {
                                    Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                                } catch (Exception e3222) {
                                    Slog.e(BackupManagerService.TAG, "Restore observer threw: " + e3222.getMessage());
                                }
                            }
                            removeMessages(8);
                            sendEmptyMessageDelayed(8, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
                            BackupManagerService.this.mWakelock.release();
                        }
                    }
                case 8:
                    synchronized (BackupManagerService.this) {
                        if (BackupManagerService.this.mActiveRestoreSession != null) {
                            Slog.w(BackupManagerService.TAG, "Restore session timed out; aborting");
                            BackupManagerService.this.mActiveRestoreSession.markTimedOut();
                            ActiveRestoreSession activeRestoreSession = BackupManagerService.this.mActiveRestoreSession;
                            activeRestoreSession.getClass();
                            post(new EndRestoreRunnable(BackupManagerService.this, BackupManagerService.this.mActiveRestoreSession));
                        }
                    }
                    return;
                case 9:
                    synchronized (BackupManagerService.this.mAdbBackupRestoreConfirmations) {
                        AdbParams params5 = (AdbParams) BackupManagerService.this.mAdbBackupRestoreConfirmations.get(msg.arg1);
                        if (params5 != null) {
                            Slog.i(BackupManagerService.TAG, "Full backup/restore timed out waiting for user confirmation");
                            BackupManagerService.this.signalAdbBackupRestoreCompletion(params5);
                            BackupManagerService.this.mAdbBackupRestoreConfirmations.delete(msg.arg1);
                            if (params5.observer != null) {
                                try {
                                    params5.observer.onTimeout();
                                } catch (RemoteException e6) {
                                }
                            }
                        } else {
                            Slog.d(BackupManagerService.TAG, "couldn't find params for token " + msg.arg1);
                        }
                    }
                    return;
                case 10:
                    AdbRestoreParams params6 = msg.obj;
                    new Thread(new PerformAdbRestoreTask(params6.fd, params6.curPassword, params6.encryptPassword, params6.observer, params6.latch), "adb-restore").start();
                    return;
                case 11:
                    synchronized (BackupManagerService.this.mQueueLock) {
                        BackupManagerService.this.recordInitPendingLocked(msg.arg1 != 0, (String) msg.obj);
                        BackupManagerService.this.mAlarmManager.set(0, System.currentTimeMillis(), BackupManagerService.this.mRunInitIntent);
                    }
                    return;
                case 12:
                    ClearRetryParams params7 = msg.obj;
                    BackupManagerService.this.clearBackupData(params7.transportName, params7.packageName);
                    return;
                case 13:
                    BackupManagerService.this.mContext.sendBroadcastAsUser(msg.obj, UserHandle.SYSTEM);
                    return;
                case 14:
                    new Thread(msg.obj, "transport-backup").start();
                    return;
                case 15:
                    BackupParams params8 = msg.obj;
                    ArrayList<BackupRequest> kvQueue = new ArrayList();
                    for (String packageName : params8.kvPackages) {
                        kvQueue.add(new BackupRequest(packageName));
                    }
                    BackupManagerService.this.mBackupRunning = true;
                    BackupManagerService.this.mWakelock.acquire();
                    sendMessage(obtainMessage(20, new PerformBackupTask(params8.transport, params8.dirName, kvQueue, null, params8.observer, params8.monitor, params8.fullPackages, true, params8.nonIncrementalBackup)));
                    return;
                case 16:
                    BackupManagerService.this.dataChangedImpl(msg.obj);
                    return;
                case 17:
                case 18:
                    Slog.d(BackupManagerService.TAG, "Timeout message received for token=" + Integer.toHexString(msg.arg1));
                    BackupManagerService.this.handleCancel(msg.arg1, false);
                    return;
                case 20:
                    try {
                        msg.obj.execute();
                        return;
                    } catch (ClassCastException e7) {
                        Slog.e(BackupManagerService.TAG, "Invalid backup task in flight, obj=" + msg.obj);
                        return;
                    }
                case 21:
                    try {
                        Pair<BackupRestoreTask, Long> taskWithResult = msg.obj;
                        ((BackupRestoreTask) taskWithResult.first).operationComplete(((Long) taskWithResult.second).longValue());
                        return;
                    } catch (ClassCastException e8) {
                        Slog.e(BackupManagerService.TAG, "Invalid completion in flight, obj=" + msg.obj);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    class BackupParams {
        public String dirName;
        public ArrayList<String> fullPackages;
        public ArrayList<String> kvPackages;
        public IBackupManagerMonitor monitor;
        public boolean nonIncrementalBackup;
        public IBackupObserver observer;
        public IBackupTransport transport;
        public boolean userInitiated;

        BackupParams(IBackupTransport transport, String dirName, ArrayList<String> kvPackages, ArrayList<String> fullPackages, IBackupObserver observer, IBackupManagerMonitor monitor, boolean userInitiated, boolean nonIncrementalBackup) {
            this.transport = transport;
            this.dirName = dirName;
            this.kvPackages = kvPackages;
            this.fullPackages = fullPackages;
            this.observer = observer;
            this.monitor = monitor;
            this.userInitiated = userInitiated;
            this.nonIncrementalBackup = nonIncrementalBackup;
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
        INITIAL,
        RUNNING_QUEUE,
        FINAL
    }

    class ClearDataObserver extends IPackageDataObserver.Stub {
        ClearDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            synchronized (BackupManagerService.this.mClearDataLock) {
                BackupManagerService.this.mClearingData = false;
                BackupManagerService.this.mClearDataLock.notifyAll();
            }
        }
    }

    class ClearParams {
        public PackageInfo packageInfo;
        public IBackupTransport transport;

        ClearParams(IBackupTransport _transport, PackageInfo _info) {
            this.transport = _transport;
            this.packageInfo = _info;
        }
    }

    class ClearRetryParams {
        public String packageName;
        public String transportName;

        ClearRetryParams(String transport, String pkg) {
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
            StringBuilder sb = new StringBuilder(128);
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
        File mFilesDir = new File("/data/system");
        boolean mIncludeApks;
        File mManifestFile = new File(this.mFilesDir, BackupManagerService.BACKUP_MANIFEST_FILENAME);
        File mMetadataFile = new File(this.mFilesDir, BackupManagerService.BACKUP_METADATA_FILENAME);
        private final int mOpToken;
        OutputStream mOutput;
        PackageInfo mPkg;
        FullBackupPreflight mPreflightHook;
        private final long mQuota;
        BackupRestoreTask mTimeoutMonitor;

        class FullBackupRunner implements Runnable {
            IBackupAgent mAgent;
            PackageInfo mPackage;
            ParcelFileDescriptor mPipe;
            boolean mSendApk;
            int mToken;
            byte[] mWidgetData;
            boolean mWriteManifest;

            FullBackupRunner(PackageInfo pack, IBackupAgent agent, ParcelFileDescriptor pipe, int token, boolean sendApk, boolean writeManifest, byte[] widgetData) throws IOException {
                this.mPackage = pack;
                this.mWidgetData = widgetData;
                this.mAgent = agent;
                this.mPipe = ParcelFileDescriptor.dup(pipe.getFileDescriptor());
                this.mToken = token;
                this.mSendApk = sendApk;
                this.mWriteManifest = writeManifest;
            }

            public void run() {
                try {
                    FullBackupDataOutput output = new FullBackupDataOutput(this.mPipe);
                    if (this.mWriteManifest) {
                        boolean writeWidgetData = this.mWidgetData != null;
                        BackupManagerService.writeAppManifest(this.mPackage, BackupManagerService.this.mPackageManager, FullBackupEngine.this.mManifestFile, this.mSendApk, writeWidgetData);
                        FullBackup.backupToTar(this.mPackage.packageName, null, null, FullBackupEngine.this.mFilesDir.getAbsolutePath(), FullBackupEngine.this.mManifestFile.getAbsolutePath(), output);
                        FullBackupEngine.this.mManifestFile.delete();
                        if (writeWidgetData) {
                            FullBackupEngine.this.writeMetadata(this.mPackage, FullBackupEngine.this.mMetadataFile, this.mWidgetData);
                            FullBackup.backupToTar(this.mPackage.packageName, null, null, FullBackupEngine.this.mFilesDir.getAbsolutePath(), FullBackupEngine.this.mMetadataFile.getAbsolutePath(), output);
                            FullBackupEngine.this.mMetadataFile.delete();
                        }
                    }
                    if (this.mSendApk) {
                        FullBackupEngine.this.writeApkToBackup(this.mPackage, output);
                    }
                    long timeout = this.mPackage.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE) ? 1800000 : BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL;
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Calling doFullBackup() on " + this.mPackage.packageName);
                    }
                    BackupManagerService.this.prepareOperationTimeout(this.mToken, timeout, FullBackupEngine.this.mTimeoutMonitor, 0);
                    this.mAgent.doFullBackup(this.mPipe, FullBackupEngine.this.mQuota, this.mToken, BackupManagerService.this.mBackupManagerBinder);
                    try {
                        this.mPipe.close();
                    } catch (IOException e) {
                    }
                } catch (IOException e2) {
                    Slog.e(BackupManagerService.TAG, "Error running full backup for " + this.mPackage.packageName);
                    try {
                        this.mPipe.close();
                    } catch (IOException e3) {
                    }
                } catch (RemoteException e4) {
                    Slog.e(BackupManagerService.TAG, "Remote agent vanished during full backup of " + this.mPackage.packageName);
                    try {
                        this.mPipe.close();
                    } catch (IOException e5) {
                    }
                } catch (Throwable th) {
                    try {
                        this.mPipe.close();
                    } catch (IOException e6) {
                    }
                    throw th;
                }
            }
        }

        FullBackupEngine(OutputStream output, FullBackupPreflight preflightHook, PackageInfo pkg, boolean alsoApks, BackupRestoreTask timeoutMonitor, long quota, int opToken) {
            this.mOutput = output;
            this.mPreflightHook = preflightHook;
            this.mPkg = pkg;
            this.mIncludeApks = alsoApks;
            this.mTimeoutMonitor = timeoutMonitor;
            this.mQuota = quota;
            this.mOpToken = opToken;
        }

        public int preflightCheck() throws RemoteException {
            if (this.mPreflightHook == null) {
                return 0;
            }
            if (initializeAgent()) {
                return this.mPreflightHook.preflightFullBackup(this.mPkg, this.mAgent);
            }
            Slog.w(BackupManagerService.TAG, "Unable to bind to full agent for " + this.mPkg.packageName);
            return -1003;
        }

        public int backupOnePackage() throws RemoteException {
            int result = -1003;
            if (initializeAgent()) {
                try {
                    ParcelFileDescriptor[] pipes = ParcelFileDescriptor.createPipe();
                    ApplicationInfo app = this.mPkg.applicationInfo;
                    boolean isSharedStorage = this.mPkg.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                    boolean sendApk = (this.mIncludeApks && (isSharedStorage ^ 1) != 0 && (app.privateFlags & 4) == 0) ? (app.flags & 1) != 0 ? (app.flags & 128) != 0 : true : false;
                    FullBackupRunner runner = new FullBackupRunner(this.mPkg, this.mAgent, pipes[1], this.mOpToken, sendApk, isSharedStorage ^ 1, AppWidgetBackupBridge.getWidgetState(this.mPkg.packageName, 0));
                    pipes[1].close();
                    pipes[1] = null;
                    new Thread(runner, "app-data-runner").start();
                    BackupManagerService.routeSocketDataToOutput(pipes[0], this.mOutput);
                    if (BackupManagerService.this.waitUntilOperationComplete(this.mOpToken)) {
                        result = 0;
                    } else {
                        Slog.e(BackupManagerService.TAG, "Full backup failed on package " + this.mPkg.packageName);
                    }
                    try {
                        this.mOutput.flush();
                        if (pipes != null) {
                            if (pipes[0] != null) {
                                pipes[0].close();
                            }
                            if (pipes[1] != null) {
                                pipes[1].close();
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
                        if (null != null) {
                            if (null[0] != null) {
                                null[0].close();
                            }
                            if (null[1] != null) {
                                null[1].close();
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
                        if (null != null) {
                            if (null[0] != null) {
                                null[0].close();
                            }
                            if (null[1] != null) {
                                null[1].close();
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
                this.mAgent = BackupManagerService.this.bindToAgentSynchronous(this.mPkg.applicationInfo, 1);
            }
            if (this.mAgent != null) {
                return true;
            }
            return false;
        }

        private void writeApkToBackup(PackageInfo pkg, FullBackupDataOutput output) {
            String appSourceDir = pkg.applicationInfo.getBaseCodePath();
            FullBackup.backupToTar(pkg.packageName, "a", null, new File(appSourceDir).getParent(), appSourceDir, output);
            File obbDir = new UserEnvironment(0).buildExternalStorageAppObbDirs(pkg.packageName)[0];
            if (obbDir != null) {
                File[] obbFiles = obbDir.listFiles();
                if (obbFiles != null) {
                    String obbDirName = obbDir.getAbsolutePath();
                    for (File obb : obbFiles) {
                        FullBackup.backupToTar(pkg.packageName, "obb", null, obbDirName, obb.getAbsolutePath(), output);
                    }
                }
            }
        }

        private void writeMetadata(PackageInfo pkg, File destination, byte[] widgetData) throws IOException {
            StringBuilder b = new StringBuilder(512);
            StringBuilderPrinter printer = new StringBuilderPrinter(b);
            printer.println(Integer.toString(1));
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
                BackupManagerService.this.tearDownAgentAndKill(this.mPkg.applicationInfo);
            }
        }
    }

    class FullBackupEntry implements Comparable<FullBackupEntry> {
        long lastBackup;
        String packageName;

        FullBackupEntry(String pkg, long when) {
            this.packageName = pkg;
            this.lastBackup = when;
        }

        public int compareTo(FullBackupEntry other) {
            if (this.lastBackup < other.lastBackup) {
                return -1;
            }
            if (this.lastBackup > other.lastBackup) {
                return 1;
            }
            return 0;
        }
    }

    class FullBackupObbConnection implements ServiceConnection {
        volatile IObbBackupService mService = null;

        FullBackupObbConnection() {
        }

        public void establish() {
            BackupManagerService.this.mContext.bindServiceAsUser(new Intent().setComponent(new ComponentName(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, "com.android.sharedstoragebackup.ObbBackupService")), this, 1, UserHandle.SYSTEM);
        }

        public void tearDown() {
            BackupManagerService.this.mContext.unbindService(this);
        }

        public boolean backupObbs(PackageInfo pkg, OutputStream out) {
            boolean success = false;
            waitForConnection();
            ParcelFileDescriptor[] pipes = null;
            try {
                pipes = ParcelFileDescriptor.createPipe();
                int token = BackupManagerService.this.generateToken();
                BackupManagerService.this.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, null, 0);
                this.mService.backupObbs(pkg.packageName, pipes[1], token, BackupManagerService.this.mBackupManagerBinder);
                BackupManagerService.routeSocketDataToOutput(pipes[0], out);
                success = BackupManagerService.this.waitUntilOperationComplete(token);
                try {
                    out.flush();
                    if (pipes != null) {
                        if (pipes[0] != null) {
                            pipes[0].close();
                        }
                        if (pipes[1] != null) {
                            pipes[1].close();
                        }
                    }
                } catch (IOException e) {
                    Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e);
                }
            } catch (Exception e2) {
                Slog.w(BackupManagerService.TAG, "Unable to back up OBBs for " + pkg, e2);
                try {
                    out.flush();
                    if (pipes != null) {
                        if (pipes[0] != null) {
                            pipes[0].close();
                        }
                        if (pipes[1] != null) {
                            pipes[1].close();
                        }
                    }
                } catch (IOException e3) {
                    Slog.w(BackupManagerService.TAG, "I/O error closing down OBB backup", e3);
                }
            } catch (Throwable th) {
                try {
                    out.flush();
                    if (pipes != null) {
                        if (pipes[0] != null) {
                            pipes[0].close();
                        }
                        if (pipes[1] != null) {
                            pipes[1].close();
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

        private void waitForConnection() {
            synchronized (this) {
                while (this.mService == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
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

    interface FullBackupPreflight {
        long getExpectedSizeOrErrorCode();

        int preflightFullBackup(PackageInfo packageInfo, IBackupAgent iBackupAgent);
    }

    abstract class FullBackupTask implements Runnable {
        IFullBackupRestoreObserver mObserver;

        FullBackupTask(IFullBackupRestoreObserver observer) {
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
        private AtomicInteger mResult = new AtomicInteger(0);
        private AtomicBoolean mRunning = new AtomicBoolean(false);

        RestoreEngine() {
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

        public int waitForResult() {
            synchronized (this.mRunning) {
                while (isRunning()) {
                    try {
                        this.mRunning.wait();
                    } catch (InterruptedException e) {
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
        final HashSet<String> mClearedPackages = new HashSet();
        final RestoreDeleteObserver mDeleteObserver = new RestoreDeleteObserver();
        private final int mEphemeralOpToken;
        final RestoreInstallObserver mInstallObserver = new RestoreInstallObserver();
        final HashMap<String, Signature[]> mManifestSignatures = new HashMap();
        IBackupManagerMonitor mMonitor;
        BackupRestoreTask mMonitorTask;
        FullBackupObbConnection mObbConnection = null;
        IFullBackupRestoreObserver mObserver;
        PackageInfo mOnlyPackage;
        final HashMap<String, String> mPackageInstallers = new HashMap();
        final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap();
        ParcelFileDescriptor[] mPipes = null;
        ApplicationInfo mTargetApp;
        byte[] mWidgetData = null;

        class RestoreDeleteObserver extends IPackageDeleteObserver.Stub {
            final AtomicBoolean mDone = new AtomicBoolean();
            int mResult;

            RestoreDeleteObserver() {
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(false);
                }
            }

            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (!this.mDone.get()) {
                        try {
                            this.mDone.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mDone.set(true);
                    this.mDone.notifyAll();
                }
            }
        }

        class RestoreFileRunnable implements Runnable {
            IBackupAgent mAgent;
            FileMetadata mInfo;
            ParcelFileDescriptor mSocket;
            int mToken;

            RestoreFileRunnable(IBackupAgent agent, FileMetadata info, ParcelFileDescriptor socket, int token) throws IOException {
                this.mAgent = agent;
                this.mInfo = info;
                this.mToken = token;
                this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFile(this.mSocket, this.mInfo.size, this.mInfo.type, this.mInfo.domain, this.mInfo.path, this.mInfo.mode, this.mInfo.mtime, this.mToken, BackupManagerService.this.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreInstallObserver extends PackageInstallObserver {
            final AtomicBoolean mDone = new AtomicBoolean();
            String mPackageName;
            int mResult;

            RestoreInstallObserver() {
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(false);
                }
            }

            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (!this.mDone.get()) {
                        try {
                            this.mDone.wait();
                        } catch (InterruptedException e) {
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
                    this.mDone.set(true);
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
                iArr[RestorePolicy.ACCEPT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RestorePolicy.ACCEPT_IF_APK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[RestorePolicy.IGNORE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = iArr;
            return iArr;
        }

        public FullRestoreEngine(BackupRestoreTask monitorTask, IFullBackupRestoreObserver observer, IBackupManagerMonitor monitor, PackageInfo onlyPackage, boolean allowApks, boolean allowObbs, int ephemeralOpToken) {
            super();
            this.mEphemeralOpToken = ephemeralOpToken;
            this.mMonitorTask = monitorTask;
            this.mObserver = observer;
            this.mMonitor = monitor;
            this.mOnlyPackage = onlyPackage;
            this.mAllowApks = allowApks;
            this.mAllowObbs = allowObbs;
            this.mBuffer = new byte[32768];
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
                FileMetadata info;
                boolean z;
                try {
                    info = readTarHeaders(instream);
                    if (info != null) {
                        String pkg = info.packageName;
                        if (!pkg.equals(this.mAgentPackage)) {
                            if (this.mOnlyPackage != null) {
                                if (!pkg.equals(this.mOnlyPackage.packageName)) {
                                    Slog.w("RestoreEngine", "Expected data for " + this.mOnlyPackage + " but saw " + pkg);
                                    setResult(-3);
                                    setRunning(false);
                                    return false;
                                }
                            }
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
                            boolean okay = true;
                            switch (-getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues()[((RestorePolicy) this.mPackagePolicies.get(pkg)).ordinal()]) {
                                case 1:
                                    if (info.domain.equals("a")) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d("RestoreEngine", "apk present but ACCEPT");
                                        }
                                        okay = false;
                                        break;
                                    }
                                    break;
                                case 2:
                                    if (!info.domain.equals("a")) {
                                        this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                        okay = false;
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
                                    return true;
                                case 3:
                                    okay = false;
                                    break;
                                default:
                                    Slog.e("RestoreEngine", "Invalid policy from manifest");
                                    okay = false;
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    break;
                            }
                            if (!isRestorableFile(info)) {
                                okay = false;
                            }
                            if (okay && this.mAgent == null) {
                                try {
                                    this.mTargetApp = BackupManagerService.this.mPackageManager.getApplicationInfo(pkg, 0);
                                    if (!this.mClearedPackages.contains(pkg)) {
                                        if (this.mTargetApp.backupAgentName == null) {
                                            if (BackupManagerService.DEBUG) {
                                                Slog.d("RestoreEngine", "Clearing app data preparatory to full restore");
                                            }
                                            BackupManagerService.this.clearApplicationDataSynchronous(pkg);
                                        }
                                        this.mClearedPackages.add(pkg);
                                    }
                                    setUpPipes();
                                    this.mAgent = BackupManagerService.this.bindToAgentSynchronous(this.mTargetApp, 3);
                                    this.mAgentPackage = pkg;
                                } catch (IOException e) {
                                } catch (NameNotFoundException e2) {
                                }
                                if (this.mAgent == null) {
                                    Slog.e("RestoreEngine", "Unable to create agent for " + pkg);
                                    okay = false;
                                    tearDownPipes();
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                }
                            }
                            if (okay) {
                                if ((pkg.equals(this.mAgentPackage) ^ 1) != 0) {
                                    Slog.e("RestoreEngine", "Restoring data for " + pkg + " but agent is for " + this.mAgentPackage);
                                    okay = false;
                                }
                            }
                            if (okay) {
                                boolean agentSuccess = true;
                                long toCopy = info.size;
                                try {
                                    BackupManagerService.this.prepareOperationTimeout(this.mEphemeralOpToken, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this.mMonitorTask, 1);
                                    if (info.domain.equals("obb")) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d("RestoreEngine", "Restoring OBB file for " + pkg + " : " + info.path);
                                        }
                                        this.mObbConnection.restoreObbFile(pkg, this.mPipes[0], info.size, info.type, info.path, info.mode, info.mtime, this.mEphemeralOpToken, BackupManagerService.this.mBackupManagerBinder);
                                    } else if (this.mTargetApp.processName.equals("system")) {
                                        Slog.d("RestoreEngine", "system process agent - spinning a thread");
                                        new Thread(new RestoreFileRunnable(this.mAgent, info, this.mPipes[0], this.mEphemeralOpToken), "restore-sys-runner").start();
                                    } else {
                                        this.mAgent.doRestoreFile(this.mPipes[0], info.size, info.type, info.domain, info.path, info.mode, info.mtime, this.mEphemeralOpToken, BackupManagerService.this.mBackupManagerBinder);
                                    }
                                } catch (IOException e3) {
                                    Slog.d("RestoreEngine", "Couldn't establish restore");
                                    agentSuccess = false;
                                    okay = false;
                                } catch (RemoteException e4) {
                                    Slog.e("RestoreEngine", "Agent crashed during full restore");
                                    agentSuccess = false;
                                    okay = false;
                                }
                                if (okay) {
                                    boolean pipeOkay = true;
                                    FileOutputStream fileOutputStream = new FileOutputStream(this.mPipes[1].getFileDescriptor());
                                    while (toCopy > 0) {
                                        int nRead = instream.read(this.mBuffer, 0, toCopy > ((long) this.mBuffer.length) ? this.mBuffer.length : (int) toCopy);
                                        if (nRead >= 0) {
                                            this.mBytes += (long) nRead;
                                        }
                                        if (nRead <= 0) {
                                            skipTarPadding(info.size, instream);
                                            agentSuccess = BackupManagerService.this.waitUntilOperationComplete(this.mEphemeralOpToken);
                                        } else {
                                            toCopy -= (long) nRead;
                                            if (pipeOkay) {
                                                try {
                                                    fileOutputStream.write(this.mBuffer, 0, nRead);
                                                } catch (IOException e5) {
                                                    Slog.e("RestoreEngine", "Failed to write to restore pipe: " + e5.getMessage());
                                                    pipeOkay = false;
                                                }
                                            }
                                        }
                                    }
                                    skipTarPadding(info.size, instream);
                                    agentSuccess = BackupManagerService.this.waitUntilOperationComplete(this.mEphemeralOpToken);
                                }
                                if (!agentSuccess) {
                                    Slog.w("RestoreEngine", "Agent failure; ending restore");
                                    BackupManagerService.this.mBackupHandler.removeMessages(18);
                                    tearDownPipes();
                                    tearDownAgent(this.mTargetApp);
                                    this.mAgent = null;
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    if (this.mOnlyPackage != null) {
                                        setResult(-2);
                                        setRunning(false);
                                        return false;
                                    }
                                }
                            }
                            if (!okay) {
                                long bytesToConsume = (info.size + 511) & -512;
                                while (bytesToConsume > 0) {
                                    long nRead2 = (long) instream.read(this.mBuffer, 0, bytesToConsume > ((long) this.mBuffer.length) ? this.mBuffer.length : (int) bytesToConsume);
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
                } catch (IOException e52) {
                    if (BackupManagerService.DEBUG) {
                        Slog.w("RestoreEngine", "io exception on restore socket read: " + e52.getMessage());
                    }
                    setResult(-3);
                    info = null;
                }
                if (info == null) {
                    tearDownPipes();
                    setRunning(false);
                    if (mustKillAgent) {
                        tearDownAgent(this.mTargetApp);
                    }
                }
                if (info != null) {
                    z = true;
                } else {
                    z = false;
                }
                return z;
            }
            Slog.w("RestoreEngine", "Restore engine used after halting");
            return false;
        }

        void setUpPipes() throws IOException {
            this.mPipes = ParcelFileDescriptor.createPipe();
        }

        void tearDownPipes() {
            synchronized (this) {
                if (this.mPipes != null) {
                    try {
                        this.mPipes[0].close();
                        this.mPipes[0] = null;
                        this.mPipes[1].close();
                        this.mPipes[1] = null;
                    } catch (IOException e) {
                        Slog.w("RestoreEngine", "Couldn't close agent pipes", e);
                    }
                    this.mPipes = null;
                }
            }
            return;
        }

        void tearDownAgent(ApplicationInfo app) {
            if (this.mAgent != null) {
                BackupManagerService.this.tearDownAgentAndKill(app);
                this.mAgent = null;
            }
        }

        void handleTimeout() {
            tearDownPipes();
            setResult(-2);
            setRunning(false);
        }

        boolean installApk(FileMetadata info, String installerPackage, InputStream instream) {
            boolean okay = true;
            if (BackupManagerService.DEBUG) {
                Slog.d("RestoreEngine", "Installing from backup: " + info.packageName);
            }
            File apkFile = new File(BackupManagerService.this.mDataDir, info.packageName);
            try {
                FileOutputStream apkStream = new FileOutputStream(apkFile);
                byte[] buffer = new byte[32768];
                long size = info.size;
                while (size > 0) {
                    int didRead = instream.read(buffer, 0, (int) (((long) buffer.length) < size ? (long) buffer.length : size));
                    if (didRead >= 0) {
                        this.mBytes += (long) didRead;
                    }
                    apkStream.write(buffer, 0, didRead);
                    size -= (long) didRead;
                }
                apkStream.close();
                apkFile.setReadable(true, false);
                Uri packageUri = Uri.fromFile(apkFile);
                this.mInstallObserver.reset();
                BackupManagerService.this.mPackageManager.installPackage(packageUri, this.mInstallObserver, 34, installerPackage);
                this.mInstallObserver.waitForCompletion();
                if (this.mInstallObserver.getResult() != 1) {
                    if (this.mPackagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                        okay = false;
                    }
                } else {
                    boolean uninstall = false;
                    if (this.mInstallObserver.mPackageName.equals(info.packageName)) {
                        try {
                            PackageInfo pkg = BackupManagerService.this.mPackageManager.getPackageInfo(info.packageName, 64);
                            if ((pkg.applicationInfo.flags & 32768) == 0) {
                                Slog.w("RestoreEngine", "Restore stream contains apk of package " + info.packageName + " but it disallows backup/restore");
                                okay = false;
                            } else if (!BackupManagerService.signaturesMatch((Signature[]) this.mManifestSignatures.get(info.packageName), pkg)) {
                                Slog.w("RestoreEngine", "Installed app " + info.packageName + " signatures do not match restore manifest");
                                okay = false;
                                uninstall = true;
                            } else if (pkg.applicationInfo.uid < 10000 && pkg.applicationInfo.backupAgentName == null) {
                                Slog.w("RestoreEngine", "Installed app " + info.packageName + " has restricted uid and no agent");
                                okay = false;
                            }
                        } catch (NameNotFoundException e) {
                            Slog.w("RestoreEngine", "Install of package " + info.packageName + " succeeded but now not found");
                            okay = false;
                        }
                    } else {
                        Slog.w("RestoreEngine", "Restore stream claimed to include apk for " + info.packageName + " but apk was really " + this.mInstallObserver.mPackageName);
                        okay = false;
                        uninstall = true;
                    }
                    if (uninstall) {
                        this.mDeleteObserver.reset();
                        BackupManagerService.this.mPackageManager.deletePackage(this.mInstallObserver.mPackageName, this.mDeleteObserver, 0);
                        this.mDeleteObserver.waitForCompletion();
                    }
                }
                apkFile.delete();
                return okay;
            } catch (IOException e2) {
                Slog.e("RestoreEngine", "Unable to transcribe restored apk for install");
                apkFile.delete();
                return false;
            } catch (Throwable th) {
                apkFile.delete();
                throw th;
            }
        }

        void skipTarPadding(long size, InputStream instream) throws IOException {
            long partial = (size + 512) % 512;
            if (partial > 0) {
                int needed = 512 - ((int) partial);
                if (readExactly(instream, new byte[needed], 0, needed) == needed) {
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
            if (((long) readExactly(instream, buffer, 0, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                String[] str = new String[1];
                int offset = extractLine(buffer, 0, str);
                int version = Integer.parseInt(str[0]);
                if (version == 1) {
                    offset = extractLine(buffer, offset, str);
                    String pkg = str[0];
                    if (info.packageName.equals(pkg)) {
                        ByteArrayInputStream bin = new ByteArrayInputStream(buffer, offset, buffer.length - offset);
                        DataInputStream in = new DataInputStream(bin);
                        while (bin.available() > 0) {
                            int token = in.readInt();
                            int size = in.readInt();
                            if (size <= 65536) {
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
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 47, null, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName), "android.app.backup.extra.LOG_WIDGET_PACKAGE_NAME", pkg));
                    return;
                }
                Slog.w("RestoreEngine", "Unsupported metadata version " + version);
                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 48, null, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName), "android.app.backup.extra.LOG_EVENT_PACKAGE_VERSION", version));
                return;
            }
            throw new IOException("Unexpected EOF in widget data");
        }

        RestorePolicy readAppManifest(FileMetadata info, InputStream instream) throws IOException {
            if (info.size > 65536) {
                throw new IOException("Restore manifest too big; corrupt? size=" + info.size);
            }
            byte[] buffer = new byte[((int) info.size)];
            if (((long) readExactly(instream, buffer, 0, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                RestorePolicy policy = RestorePolicy.IGNORE;
                String[] str = new String[1];
                try {
                    int offset = extractLine(buffer, 0, str);
                    int version = Integer.parseInt(str[0]);
                    if (version == 1) {
                        offset = extractLine(buffer, offset, str);
                        String manifestPackage = str[0];
                        if (manifestPackage.equals(info.packageName)) {
                            offset = extractLine(buffer, offset, str);
                            version = Integer.parseInt(str[0]);
                            offset = extractLine(buffer, offset, str);
                            Integer.parseInt(str[0]);
                            offset = extractLine(buffer, offset, str);
                            info.installerPackageName = str[0].length() > 0 ? str[0] : null;
                            offset = extractLine(buffer, offset, str);
                            boolean hasApk = str[0].equals("1");
                            offset = extractLine(buffer, offset, str);
                            int numSigs = Integer.parseInt(str[0]);
                            if (numSigs > 0) {
                                Object sigs = new Signature[numSigs];
                                for (int i = 0; i < numSigs; i++) {
                                    offset = extractLine(buffer, offset, str);
                                    sigs[i] = new Signature(str[0]);
                                }
                                this.mManifestSignatures.put(info.packageName, sigs);
                                try {
                                    PackageInfo pkgInfo = BackupManagerService.this.mPackageManager.getPackageInfo(info.packageName, 64);
                                    if ((32768 & pkgInfo.applicationInfo.flags) == 0) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i("RestoreEngine", "Restore manifest from " + info.packageName + " but allowBackup=false");
                                        }
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 39, pkgInfo, 3, null);
                                    } else if (pkgInfo.applicationInfo.uid < 10000 && pkgInfo.applicationInfo.backupAgentName == null) {
                                        Slog.w("RestoreEngine", "Package " + info.packageName + " is system level with no agent");
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 38, pkgInfo, 2, null);
                                    } else if (!BackupManagerService.signaturesMatch(sigs, pkgInfo)) {
                                        Slog.w("RestoreEngine", "Restore manifest signatures do not match installed application for " + info.packageName);
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 37, pkgInfo, 3, null);
                                    } else if ((pkgInfo.applicationInfo.flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
                                        Slog.i("RestoreEngine", "Package has restoreAnyVersion; taking data");
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 34, pkgInfo, 3, null);
                                        policy = RestorePolicy.ACCEPT;
                                    } else if (pkgInfo.versionCode >= version) {
                                        Slog.i("RestoreEngine", "Sig + version match; taking data");
                                        policy = RestorePolicy.ACCEPT;
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 35, pkgInfo, 3, null);
                                    } else if (this.mAllowApks) {
                                        Slog.i("RestoreEngine", "Data version " + version + " is newer than installed version " + pkgInfo.versionCode + " - requiring apk");
                                        policy = RestorePolicy.ACCEPT_IF_APK;
                                    } else {
                                        Slog.i("RestoreEngine", "Data requires newer version " + version + "; ignoring");
                                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 36, pkgInfo, 3, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_OLD_VERSION", version));
                                        policy = RestorePolicy.IGNORE;
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
                                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 40, null, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName), "android.app.backup.extra.LOG_POLICY_ALLOW_APKS", this.mAllowApks));
                                }
                                if (policy == RestorePolicy.ACCEPT_IF_APK && (hasApk ^ 1) != 0) {
                                    Slog.i("RestoreEngine", "Cannot restore package " + info.packageName + " without the matching .apk");
                                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 41, null, 3, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName));
                                }
                            } else {
                                Slog.i("RestoreEngine", "Missing signature on backed-up package " + info.packageName);
                                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 42, null, 3, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName));
                            }
                        } else {
                            Slog.i("RestoreEngine", "Expected package " + info.packageName + " but restore manifest claims " + manifestPackage);
                            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 43, null, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName), "android.app.backup.extra.LOG_MANIFEST_PACKAGE_NAME", manifestPackage));
                        }
                    } else {
                        Slog.i("RestoreEngine", "Unknown restore manifest version " + version + " for package " + info.packageName);
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 44, null, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName), "android.app.backup.extra.LOG_EVENT_PACKAGE_VERSION", version));
                    }
                } catch (NumberFormatException e2) {
                    Slog.w("RestoreEngine", "Corrupt restore manifest for package " + info.packageName);
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 46, null, 3, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", info.packageName));
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
            while (pos < end && buffer[pos] != (byte) 10) {
                pos++;
            }
            outStr[0] = new String(buffer, offset, pos - offset);
            return pos + 1;
        }

        void dumpFileMetadata(FileMetadata info) {
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x00a2  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        FileMetadata readTarHeaders(InputStream instream) throws IOException {
            IOException e;
            byte[] block = new byte[512];
            FileMetadata fileMetadata = null;
            if (readTarHeader(instream, block)) {
                try {
                    FileMetadata info = new FileMetadata();
                    try {
                        info.size = extractRadix(block, BackupManagerService.TAR_HEADER_OFFSET_FILESIZE, 12, 8);
                        info.mtime = extractRadix(block, 136, 12, 8);
                        info.mode = extractRadix(block, 100, 8, 8);
                        info.path = extractString(block, BackupManagerService.TAR_HEADER_OFFSET_PATH_PREFIX, BackupManagerService.TAR_HEADER_LENGTH_PATH_PREFIX);
                        String path = extractString(block, 0, 100);
                        if (path.length() > 0) {
                            if (info.path.length() > 0) {
                                info.path += '/';
                            }
                            info.path += path;
                        }
                        int typeChar = block[BackupManagerService.TAR_HEADER_OFFSET_TYPE_CHAR];
                        if (typeChar == 120) {
                            boolean gotHeader = readPaxExtendedHeader(instream, info);
                            if (gotHeader) {
                                gotHeader = readTarHeader(instream, block);
                            }
                            if (gotHeader) {
                                typeChar = block[BackupManagerService.TAR_HEADER_OFFSET_TYPE_CHAR];
                            } else {
                                throw new IOException("Bad or missing pax header");
                            }
                        }
                        switch (typeChar) {
                            case 0:
                                return null;
                            case 48:
                                info.type = 1;
                                break;
                            case 53:
                                info.type = 2;
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
                        if ("shared/".regionMatches(0, info.path, 0, "shared/".length())) {
                            info.path = info.path.substring("shared/".length());
                            info.packageName = BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE;
                            info.domain = "shared";
                            if (BackupManagerService.DEBUG) {
                                Slog.i("RestoreEngine", "File in shared storage: " + info.path);
                            }
                        } else if ("apps/".regionMatches(0, info.path, 0, "apps/".length())) {
                            info.path = info.path.substring("apps/".length());
                            int slash = info.path.indexOf(47);
                            if (slash < 0) {
                                throw new IOException("Illegal semantic path in " + info.path);
                            }
                            info.packageName = info.path.substring(0, slash);
                            info.path = info.path.substring(slash + 1);
                            if (!(info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME) || (info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME) ^ 1) == 0)) {
                                slash = info.path.indexOf(47);
                                if (slash < 0) {
                                    throw new IOException("Illegal semantic path in non-manifest " + info.path);
                                }
                                info.domain = info.path.substring(0, slash);
                                info.path = info.path.substring(slash + 1);
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
                    }
                    throw e;
                }
            }
            return fileMetadata;
        }

        private boolean isRestorableFile(FileMetadata info) {
            if ("c".equals(info.domain)) {
                return false;
            }
            if (("r".equals(info.domain) && info.path.startsWith("no_backup/")) || info.path.contains("..") || info.path.contains("//")) {
                return false;
            }
            return true;
        }

        private void HEXLOG(byte[] block) {
            int offset = 0;
            int todo = block.length;
            StringBuilder buf = new StringBuilder(64);
            while (todo > 0) {
                buf.append(String.format("%04x   ", new Object[]{Integer.valueOf(offset)}));
                int numThisLine = todo > 16 ? 16 : todo;
                for (int i = 0; i < numThisLine; i++) {
                    buf.append(String.format("%02x ", new Object[]{Byte.valueOf(block[offset + i])}));
                }
                Slog.i("hexdump", buf.toString());
                buf.setLength(0);
                todo -= numThisLine;
                offset += numThisLine;
            }
        }

        int readExactly(InputStream in, byte[] buffer, int offset, int size) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
            int soFar = 0;
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
            int got = readExactly(instream, block, 0, 512);
            if (got == 0) {
                return false;
            }
            if (got < 512) {
                throw new IOException("Unable to read full block header");
            }
            this.mBytes += 512;
            return true;
        }

        boolean readPaxExtendedHeader(InputStream instream, FileMetadata info) throws IOException {
            if (info.size > 32768) {
                Slog.w("RestoreEngine", "Suspiciously large pax header size " + info.size + " - aborting");
                throw new IOException("Sanity failure: pax header size " + info.size);
            }
            byte[] data = new byte[(((int) ((info.size + 511) >> 9)) * 512)];
            if (readExactly(instream, data, 0, data.length) < data.length) {
                throw new IOException("Unable to read full pax header");
            }
            this.mBytes += (long) data.length;
            int contentSize = (int) info.size;
            int offset = 0;
            do {
                int eol = offset + 1;
                while (eol < contentSize && data[eol] != (byte) 32) {
                    eol++;
                }
                if (eol >= contentSize) {
                    throw new IOException("Invalid pax data");
                }
                int linelen = (int) extractRadix(data, offset, eol - offset, 10);
                int key = eol + 1;
                eol = (offset + linelen) - 1;
                int value = key + 1;
                while (data[value] != (byte) 61 && value <= eol) {
                    value++;
                }
                if (value > eol) {
                    throw new IOException("Invalid pax declaration");
                }
                String keyStr = new String(data, key, value - key, "UTF-8");
                String valStr = new String(data, value + 1, (eol - value) - 1, "UTF-8");
                if ("path".equals(keyStr)) {
                    info.path = valStr;
                } else if ("size".equals(keyStr)) {
                    info.size = Long.parseLong(valStr);
                } else if (BackupManagerService.DEBUG) {
                    Slog.i("RestoreEngine", "Unhandled pax key: " + key);
                }
                offset += linelen;
            } while (offset < contentSize);
            return true;
        }

        long extractRadix(byte[] data, int offset, int maxChars, int radix) throws IOException {
            long value = 0;
            int end = offset + maxChars;
            int i = offset;
            while (i < end) {
                byte b = data[i];
                if (b == (byte) 0 || b == (byte) 32) {
                    break;
                } else if (b < (byte) 48 || b > (radix + 48) - 1) {
                    throw new IOException("Invalid number in header: '" + ((char) b) + "' for radix " + radix);
                } else {
                    value = (((long) radix) * value) + ((long) (b - 48));
                    i++;
                }
            }
            return value;
        }

        String extractString(byte[] data, int offset, int maxChars) throws IOException {
            int end = offset + maxChars;
            int eos = offset;
            while (eos < end && data[eos] != (byte) 0) {
                eos++;
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

    public static final class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
            BackupManagerService.sInstance = new Trampoline(context);
        }

        public void onStart() {
            publishBinderService("backup", BackupManagerService.sInstance);
        }

        public void onUnlockUser(int userId) {
            boolean z = true;
            if (userId == 0) {
                BackupManagerService.sInstance.initialize(userId);
                if (!BackupManagerService.backupSettingMigrated(userId)) {
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Backup enable apparently not migrated");
                    }
                    ContentResolver r = BackupManagerService.sInstance.mContext.getContentResolver();
                    int enableState = Secure.getIntForUser(r, BackupManagerService.BACKUP_ENABLE_FILE, -1, userId);
                    if (enableState >= 0) {
                        if (BackupManagerService.DEBUG) {
                            boolean z2;
                            String str = BackupManagerService.TAG;
                            StringBuilder append = new StringBuilder().append("Migrating enable state ");
                            if (enableState != 0) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            Slog.i(str, append.append(z2).toString());
                        }
                        if (enableState == 0) {
                            z = false;
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
        final BackupRestoreTask callback;
        int state;
        final int type;

        Operation(int initialState, BackupRestoreTask callbackObj, int type) {
            this.state = initialState;
            this.callback = callbackObj;
            this.type = type;
        }
    }

    class PerformAdbBackupTask extends FullBackupTask implements BackupRestoreTask {
        boolean mAllApps;
        FullBackupEngine mBackupEngine;
        boolean mCompress;
        private final int mCurrentOpToken;
        String mCurrentPassword;
        PackageInfo mCurrentTarget;
        DeflaterOutputStream mDeflater;
        boolean mDoWidgets;
        String mEncryptPassword;
        boolean mIncludeApks;
        boolean mIncludeObbs;
        boolean mIncludeShared;
        boolean mIncludeSystem;
        boolean mKeyValue;
        final AtomicBoolean mLatch;
        ParcelFileDescriptor mOutputFile;
        ArrayList<String> mPackages;

        PerformAdbBackupTask(ParcelFileDescriptor fd, IFullBackupRestoreObserver observer, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, String curPassword, String encryptPassword, boolean doAllApps, boolean doSystem, boolean doCompress, boolean doKeyValue, String[] packages, AtomicBoolean latch) {
            ArrayList arrayList;
            super(observer);
            this.mCurrentOpToken = BackupManagerService.this.generateToken();
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
            this.mKeyValue = doKeyValue;
        }

        void addPackagesToSet(TreeMap<String, PackageInfo> set, List<String> pkgNames) {
            for (String pkgName : pkgNames) {
                if (!set.containsKey(pkgName)) {
                    try {
                        set.put(pkgName, BackupManagerService.this.mPackageManager.getPackageInfo(pkgName, 64));
                    } catch (NameNotFoundException e) {
                        Slog.w(BackupManagerService.TAG, "Unknown package " + pkgName + ", skipping");
                    }
                }
            }
        }

        private OutputStream emitAesBackupHeader(StringBuilder headerbuf, OutputStream ofstream) throws Exception {
            byte[] newUserSalt = BackupManagerService.this.randomBytes(512);
            SecretKey userKey = BackupManagerService.this.buildPasswordKey(BackupManagerService.PBKDF_CURRENT, this.mEncryptPassword, newUserSalt, 10000);
            byte[] masterPw = new byte[32];
            BackupManagerService.this.mRng.nextBytes(masterPw);
            byte[] checksumSalt = BackupManagerService.this.randomBytes(512);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec masterKeySpec = new SecretKeySpec(masterPw, "AES");
            c.init(1, masterKeySpec);
            OutputStream finalOutput = new CipherOutputStream(ofstream, c);
            headerbuf.append(BackupManagerService.ENCRYPTION_ALGORITHM_NAME);
            headerbuf.append(10);
            headerbuf.append(BackupManagerService.this.byteArrayToHex(newUserSalt));
            headerbuf.append(10);
            headerbuf.append(BackupManagerService.this.byteArrayToHex(checksumSalt));
            headerbuf.append(10);
            headerbuf.append(10000);
            headerbuf.append(10);
            Cipher mkC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mkC.init(1, userKey);
            headerbuf.append(BackupManagerService.this.byteArrayToHex(mkC.getIV()));
            headerbuf.append(10);
            byte[] IV = c.getIV();
            byte[] mk = masterKeySpec.getEncoded();
            byte[] checksum = BackupManagerService.this.makeKeyChecksum(BackupManagerService.PBKDF_CURRENT, masterKeySpec.getEncoded(), checksumSalt, 10000);
            ByteArrayOutputStream blob = new ByteArrayOutputStream(((IV.length + mk.length) + checksum.length) + 3);
            DataOutputStream dataOutputStream = new DataOutputStream(blob);
            dataOutputStream.writeByte(IV.length);
            dataOutputStream.write(IV);
            dataOutputStream.writeByte(mk.length);
            dataOutputStream.write(mk);
            dataOutputStream.writeByte(checksum.length);
            dataOutputStream.write(checksum);
            dataOutputStream.flush();
            headerbuf.append(BackupManagerService.this.byteArrayToHex(mkC.doFinal(blob.toByteArray())));
            headerbuf.append(10);
            return finalOutput;
        }

        private void finalizeBackup(OutputStream out) {
            try {
                out.write(new byte[1024]);
            } catch (IOException e) {
                Slog.w(BackupManagerService.TAG, "Error attempting to finalize backup stream");
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:153:0x03b4 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:158:0x03ce  */
        /* JADX WARNING: Removed duplicated region for block: B:124:0x0340 A:{Splitter: B:98:0x0256, ExcHandler: android.os.RemoteException (e android.os.RemoteException), PHI: r7 } */
        /* JADX WARNING: Removed duplicated region for block: B:133:0x035e A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:203:0x04c3 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:124:0x0340 A:{Splitter: B:98:0x0256, ExcHandler: android.os.RemoteException (e android.os.RemoteException), PHI: r7 } */
        /* JADX WARNING: Removed duplicated region for block: B:176:0x0410 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:124:0x0340 A:{Splitter: B:98:0x0256, ExcHandler: android.os.RemoteException (e android.os.RemoteException), PHI: r7 } */
        /* JADX WARNING: Missing block: B:126:?, code:
            android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "App died during full backup");
     */
        /* JADX WARNING: Missing block: B:127:0x034a, code:
            if (r7 != null) goto L_0x034c;
     */
        /* JADX WARNING: Missing block: B:129:?, code:
            r7.flush();
            r7.close();
     */
        /* JADX WARNING: Missing block: B:130:0x0352, code:
            r40.mOutputFile.close();
     */
        /* JADX WARNING: Missing block: B:132:0x035d, code:
            monitor-enter(r40.mLatch);
     */
        /* JADX WARNING: Missing block: B:134:?, code:
            r40.mLatch.set(true);
            r40.mLatch.notifyAll();
     */
        /* JADX WARNING: Missing block: B:136:0x036e, code:
            sendEndBackup();
            r0.tearDown();
     */
        /* JADX WARNING: Missing block: B:137:0x0376, code:
            if (com.android.server.backup.BackupManagerService.DEBUG != false) goto L_0x0378;
     */
        /* JADX WARNING: Missing block: B:138:0x0378, code:
            android.util.Slog.d(com.android.server.backup.BackupManagerService.TAG, "Full backup pass complete.");
     */
        /* JADX WARNING: Missing block: B:139:0x0381, code:
            r40.this$0.mWakelock.release();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            int i;
            PackageInfo pkg;
            Throwable e;
            Slog.i(BackupManagerService.TAG, "--- Performing adb backup" + (this.mKeyValue ? ", including key-value backups" : "") + " ---");
            TreeMap<String, PackageInfo> packagesToBackup = new TreeMap();
            FullBackupObbConnection fullBackupObbConnection = new FullBackupObbConnection();
            fullBackupObbConnection.establish();
            sendStartBackup();
            if (this.mAllApps) {
                List<PackageInfo> allPackages = BackupManagerService.this.mPackageManager.getInstalledPackages(64);
                for (i = 0; i < allPackages.size(); i++) {
                    pkg = (PackageInfo) allPackages.get(i);
                    if (this.mIncludeSystem || (pkg.applicationInfo.flags & 1) == 0) {
                        packagesToBackup.put(pkg.packageName, pkg);
                    }
                }
            }
            if (this.mDoWidgets) {
                List<String> pkgs = AppWidgetBackupBridge.getWidgetParticipants(0);
                if (pkgs != null) {
                    addPackagesToSet(packagesToBackup, pkgs);
                }
            }
            if (this.mPackages != null) {
                addPackagesToSet(packagesToBackup, this.mPackages);
            }
            ArrayList<PackageInfo> keyValueBackupQueue = new ArrayList();
            Iterator<Entry<String, PackageInfo>> iter = packagesToBackup.entrySet().iterator();
            while (iter.hasNext()) {
                pkg = (PackageInfo) ((Entry) iter.next()).getValue();
                if (!BackupManagerService.appIsEligibleForBackup(pkg.applicationInfo, BackupManagerService.this.mPackageManager) || BackupManagerService.appIsStopped(pkg.applicationInfo)) {
                    iter.remove();
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Package " + pkg.packageName + " is not eligible for backup, removing.");
                    }
                } else if (BackupManagerService.appIsKeyValueOnly(pkg)) {
                    iter.remove();
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Package " + pkg.packageName + " is key-value.");
                    }
                    keyValueBackupQueue.add(pkg);
                }
            }
            ArrayList<PackageInfo> arrayList = new ArrayList(packagesToBackup.values());
            OutputStream fileOutputStream = new FileOutputStream(this.mOutputFile.getFileDescriptor());
            OutputStream out = null;
            try {
                boolean encrypting = this.mEncryptPassword != null && this.mEncryptPassword.length() > 0;
                if (!BackupManagerService.this.deviceIsEncrypted() || (encrypting ^ 1) == 0) {
                    OutputStream finalOutput = fileOutputStream;
                    if (BackupManagerService.this.backupPasswordMatches(this.mCurrentPassword)) {
                        OutputStream finalOutput2;
                        StringBuilder stringBuilder = new StringBuilder(1024);
                        stringBuilder.append(BackupManagerService.BACKUP_FILE_HEADER_MAGIC);
                        stringBuilder.append(5);
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
                                synchronized (this.mLatch) {
                                    this.mLatch.set(true);
                                    this.mLatch.notifyAll();
                                }
                                sendEndBackup();
                                fullBackupObbConnection.tearDown();
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                }
                                BackupManagerService.this.mWakelock.release();
                                return;
                            } catch (RemoteException e4) {
                            }
                        } else {
                            stringBuilder.append("none\n");
                            finalOutput2 = finalOutput;
                        }
                        try {
                            fileOutputStream.write(stringBuilder.toString().getBytes("UTF-8"));
                            if (this.mCompress) {
                                fileOutputStream = new DeflaterOutputStream(finalOutput2, new Deflater(9), true);
                            } else {
                                finalOutput = finalOutput2;
                            }
                            out = finalOutput;
                            if (this.mIncludeShared) {
                                try {
                                    arrayList.add(BackupManagerService.this.mPackageManager.getPackageInfo(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, 0));
                                } catch (NameNotFoundException e5) {
                                    Slog.e(BackupManagerService.TAG, "Unable to find shared-storage backup handler");
                                }
                            }
                            int N = arrayList.size();
                            i = 0;
                            while (i < N) {
                                pkg = (PackageInfo) arrayList.get(i);
                                if (BackupManagerService.DEBUG) {
                                    Slog.i(BackupManagerService.TAG, "--- Performing full backup for package " + pkg.packageName + " ---");
                                }
                                boolean isSharedStorage = pkg.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                                this.mBackupEngine = new FullBackupEngine(out, null, pkg, this.mIncludeApks, this, JobStatus.NO_LATEST_RUNTIME, this.mCurrentOpToken);
                                sendOnBackupPackage(isSharedStorage ? "Shared storage" : pkg.packageName);
                                this.mCurrentTarget = pkg;
                                this.mBackupEngine.backupOnePackage();
                                if (!this.mIncludeObbs || fullBackupObbConnection.backupObbs(pkg, out)) {
                                    i++;
                                } else {
                                    throw new RuntimeException("Failure writing OBB stack for " + pkg);
                                }
                            }
                            if (this.mKeyValue) {
                                for (PackageInfo keyValuePackage : keyValueBackupQueue) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.i(BackupManagerService.TAG, "--- Performing key-value backup for package " + keyValuePackage.packageName + " ---");
                                    }
                                    KeyValueAdbBackupEngine kvBackupEngine = new KeyValueAdbBackupEngine(out, keyValuePackage, BackupManagerService.this, BackupManagerService.this.mPackageManager, BackupManagerService.this.mBaseStateDir, BackupManagerService.this.mDataDir);
                                    sendOnBackupPackage(keyValuePackage.packageName);
                                    kvBackupEngine.backupOnePackage();
                                }
                            }
                            finalizeBackup(out);
                            if (out != null) {
                                try {
                                    out.flush();
                                    out.close();
                                } catch (IOException e6) {
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.mLatch) {
                                this.mLatch.set(true);
                                this.mLatch.notifyAll();
                            }
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            BackupManagerService.this.mWakelock.release();
                        } catch (Exception e7) {
                            e = e7;
                            finalOutput = finalOutput2;
                            Slog.e(BackupManagerService.TAG, "Unable to emit archive header", e);
                            this.mOutputFile.close();
                            synchronized (this.mLatch) {
                            }
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                            }
                            BackupManagerService.this.mWakelock.release();
                            return;
                        } catch (RemoteException e42) {
                        } catch (Throwable th) {
                            if (out != null) {
                                try {
                                    out.flush();
                                    out.close();
                                } catch (IOException e8) {
                                    synchronized (this.mLatch) {
                                    }
                                }
                            }
                            this.mOutputFile.close();
                            synchronized (this.mLatch) {
                                this.mLatch.set(true);
                                this.mLatch.notifyAll();
                                sendEndBackup();
                                fullBackupObbConnection.tearDown();
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                                }
                                BackupManagerService.this.mWakelock.release();
                            }
                        }
                        return;
                    }
                    if (BackupManagerService.DEBUG) {
                        Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                    }
                    try {
                        this.mOutputFile.close();
                    } catch (IOException e9) {
                    }
                    synchronized (this.mLatch) {
                        this.mLatch.set(true);
                        this.mLatch.notifyAll();
                    }
                    sendEndBackup();
                    fullBackupObbConnection.tearDown();
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                    }
                    BackupManagerService.this.mWakelock.release();
                    return;
                }
                Slog.e(BackupManagerService.TAG, "Unencrypted backup of encrypted device; aborting");
                try {
                    this.mOutputFile.close();
                } catch (IOException e10) {
                }
                synchronized (this.mLatch) {
                    this.mLatch.set(true);
                    this.mLatch.notifyAll();
                }
                sendEndBackup();
                fullBackupObbConnection.tearDown();
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                }
                BackupManagerService.this.mWakelock.release();
            } catch (RemoteException e422) {
            } catch (Throwable e11) {
                Slog.e(BackupManagerService.TAG, "Internal exception during full backup", e11);
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException e12) {
                        synchronized (this.mLatch) {
                            this.mLatch.set(true);
                            this.mLatch.notifyAll();
                            sendEndBackup();
                            fullBackupObbConnection.tearDown();
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Full backup pass complete.");
                            }
                            BackupManagerService.this.mWakelock.release();
                            return;
                        }
                    }
                }
                this.mOutputFile.close();
                synchronized (this.mLatch) {
                }
            }
        }

        public void execute() {
        }

        public void operationComplete(long result) {
        }

        public void handleCancel(boolean cancelAll) {
            PackageInfo target = this.mCurrentTarget;
            if (BackupManagerService.DEBUG) {
                Slog.w(BackupManagerService.TAG, "adb backup cancel of " + target);
            }
            if (target != null) {
                BackupManagerService.this.tearDownAgentAndKill(this.mCurrentTarget.applicationInfo);
            }
            BackupManagerService.this.removeOperation(this.mCurrentOpToken);
        }
    }

    class PerformAdbRestoreTask implements Runnable {
        private static final /* synthetic */ int[] -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$server$backup$BackupManagerService$RestorePolicy;
        IBackupAgent mAgent;
        String mAgentPackage;
        long mBytes;
        final HashSet<String> mClearedPackages = new HashSet();
        String mCurrentPassword;
        String mDecryptPassword;
        final RestoreDeleteObserver mDeleteObserver = new RestoreDeleteObserver();
        ParcelFileDescriptor mInputFile;
        final RestoreInstallObserver mInstallObserver = new RestoreInstallObserver();
        AtomicBoolean mLatchObject;
        final HashMap<String, Signature[]> mManifestSignatures = new HashMap();
        FullBackupObbConnection mObbConnection = null;
        IFullBackupRestoreObserver mObserver;
        final HashMap<String, String> mPackageInstallers = new HashMap();
        PackageManagerBackupAgent mPackageManagerBackupAgent;
        final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap();
        ParcelFileDescriptor[] mPipes = null;
        ApplicationInfo mTargetApp;
        byte[] mWidgetData = null;

        class RestoreDeleteObserver extends IPackageDeleteObserver.Stub {
            final AtomicBoolean mDone = new AtomicBoolean();
            int mResult;

            RestoreDeleteObserver() {
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(false);
                }
            }

            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (!this.mDone.get()) {
                        try {
                            this.mDone.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }

            public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                synchronized (this.mDone) {
                    this.mResult = returnCode;
                    this.mDone.set(true);
                    this.mDone.notifyAll();
                }
            }
        }

        class RestoreFileRunnable implements Runnable {
            IBackupAgent mAgent;
            FileMetadata mInfo;
            ParcelFileDescriptor mSocket;
            int mToken;

            RestoreFileRunnable(IBackupAgent agent, FileMetadata info, ParcelFileDescriptor socket, int token) throws IOException {
                this.mAgent = agent;
                this.mInfo = info;
                this.mToken = token;
                this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFile(this.mSocket, this.mInfo.size, this.mInfo.type, this.mInfo.domain, this.mInfo.path, this.mInfo.mode, this.mInfo.mtime, this.mToken, BackupManagerService.this.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreFinishedRunnable implements Runnable {
            final IBackupAgent mAgent;
            final int mToken;

            RestoreFinishedRunnable(IBackupAgent agent, int token) {
                this.mAgent = agent;
                this.mToken = token;
            }

            public void run() {
                try {
                    this.mAgent.doRestoreFinished(this.mToken, BackupManagerService.this.mBackupManagerBinder);
                } catch (RemoteException e) {
                }
            }
        }

        class RestoreInstallObserver extends PackageInstallObserver {
            final AtomicBoolean mDone = new AtomicBoolean();
            String mPackageName;
            int mResult;

            RestoreInstallObserver() {
            }

            public void reset() {
                synchronized (this.mDone) {
                    this.mDone.set(false);
                }
            }

            public void waitForCompletion() {
                synchronized (this.mDone) {
                    while (!this.mDone.get()) {
                        try {
                            this.mDone.wait();
                        } catch (InterruptedException e) {
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
                    this.mDone.set(true);
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
                iArr[RestorePolicy.ACCEPT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[RestorePolicy.ACCEPT_IF_APK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[RestorePolicy.IGNORE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$RestorePolicySwitchesValues = iArr;
            return iArr;
        }

        PerformAdbRestoreTask(ParcelFileDescriptor fd, String curPassword, String decryptPassword, IFullBackupRestoreObserver observer, AtomicBoolean latch) {
            this.mInputFile = fd;
            this.mCurrentPassword = curPassword;
            this.mDecryptPassword = decryptPassword;
            this.mObserver = observer;
            this.mLatchObject = latch;
            this.mAgent = null;
            this.mPackageManagerBackupAgent = new PackageManagerBackupAgent(BackupManagerService.this.mPackageManager);
            this.mAgentPackage = null;
            this.mTargetApp = null;
            this.mObbConnection = new FullBackupObbConnection();
            this.mClearedPackages.add("android");
            this.mClearedPackages.add(BackupManagerService.SETTINGS_PACKAGE);
        }

        /* JADX WARNING: Removed duplicated region for block: B:96:0x02b7 A:{SYNTHETIC, Splitter: B:96:0x02b7} */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x02bc A:{Catch:{ IOException -> 0x03ce }} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x02cf A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:85:0x023e A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x0226 A:{SYNTHETIC, Splitter: B:78:0x0226} */
        /* JADX WARNING: Removed duplicated region for block: B:81:0x022b A:{Catch:{ IOException -> 0x03bb }} */
        /* JADX WARNING: Removed duplicated region for block: B:85:0x023e A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:96:0x02b7 A:{SYNTHETIC, Splitter: B:96:0x02b7} */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x02bc A:{Catch:{ IOException -> 0x03ce }} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x02cf A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:103:0x02cf A:{SYNTHETIC} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            InputStream rawInStream;
            Throwable th;
            Slog.i(BackupManagerService.TAG, "--- Performing full-dataset restore ---");
            this.mObbConnection.establish();
            sendStartRestore();
            if (Environment.getExternalStorageState().equals("mounted")) {
                this.mPackagePolicies.put(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE, RestorePolicy.ACCEPT);
            }
            FileInputStream rawInStream2 = null;
            DataInputStream rawDataIn = null;
            try {
                if (BackupManagerService.this.backupPasswordMatches(this.mCurrentPassword)) {
                    DataInputStream dataInputStream;
                    boolean compressed;
                    InputStream preCompressStream;
                    boolean okay;
                    this.mBytes = 0;
                    byte[] buffer = new byte[32768];
                    InputStream fileInputStream = new FileInputStream(this.mInputFile.getFileDescriptor());
                    try {
                        dataInputStream = new DataInputStream(fileInputStream);
                        compressed = false;
                        preCompressStream = fileInputStream;
                        okay = false;
                    } catch (IOException e) {
                        rawInStream2 = fileInputStream;
                        try {
                            Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, true);
                            if (rawDataIn != null) {
                                try {
                                    rawDataIn.close();
                                } catch (IOException e2) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e2);
                                    synchronized (this.mLatchObject) {
                                    }
                                    this.mObbConnection.tearDown();
                                    sendEndRestore();
                                    Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                                    BackupManagerService.this.mWakelock.release();
                                }
                            }
                            if (rawInStream2 != null) {
                                rawInStream2.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(true);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            BackupManagerService.this.mWakelock.release();
                        } catch (Throwable th2) {
                            th = th2;
                            tearDownPipes();
                            tearDownAgent(this.mTargetApp, true);
                            if (rawDataIn != null) {
                                try {
                                    rawDataIn.close();
                                } catch (IOException e22) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e22);
                                    synchronized (this.mLatchObject) {
                                    }
                                    this.mObbConnection.tearDown();
                                    sendEndRestore();
                                    Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                                    BackupManagerService.this.mWakelock.release();
                                    throw th;
                                }
                            }
                            if (rawInStream2 != null) {
                                rawInStream2.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(true);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            BackupManagerService.this.mWakelock.release();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        rawInStream2 = fileInputStream;
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, true);
                        if (rawDataIn != null) {
                        }
                        if (rawInStream2 != null) {
                        }
                        this.mInputFile.close();
                        synchronized (this.mLatchObject) {
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        BackupManagerService.this.mWakelock.release();
                        throw th;
                    }
                    try {
                        byte[] streamHeader = new byte[BackupManagerService.BACKUP_FILE_HEADER_MAGIC.length()];
                        dataInputStream.readFully(streamHeader);
                        if (Arrays.equals(BackupManagerService.BACKUP_FILE_HEADER_MAGIC.getBytes("UTF-8"), streamHeader)) {
                            String s = readHeaderLine(fileInputStream);
                            int archiveVersion = Integer.parseInt(s);
                            if (archiveVersion <= 5) {
                                boolean pbkdf2Fallback = archiveVersion == 1;
                                compressed = Integer.parseInt(readHeaderLine(fileInputStream)) != 0;
                                s = readHeaderLine(fileInputStream);
                                if (s.equals("none")) {
                                    okay = true;
                                } else if (this.mDecryptPassword == null || this.mDecryptPassword.length() <= 0) {
                                    Slog.w(BackupManagerService.TAG, "Archive is encrypted but no password given");
                                } else {
                                    preCompressStream = decodeAesHeaderAndInitialize(s, pbkdf2Fallback, fileInputStream);
                                    if (preCompressStream != null) {
                                        okay = true;
                                    }
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
                            tearDownAgent(this.mTargetApp, true);
                            if (dataInputStream != null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e222) {
                                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e222);
                                }
                            }
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            this.mInputFile.close();
                            synchronized (this.mLatchObject) {
                                this.mLatchObject.set(true);
                                this.mLatchObject.notifyAll();
                            }
                            this.mObbConnection.tearDown();
                            sendEndRestore();
                            Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                            BackupManagerService.this.mWakelock.release();
                            rawDataIn = dataInputStream;
                            rawInStream2 = fileInputStream;
                        }
                        Slog.w(BackupManagerService.TAG, "Invalid restore data; aborting.");
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, true);
                        if (dataInputStream != null) {
                            try {
                                dataInputStream.close();
                            } catch (IOException e2222) {
                                Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e2222);
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        this.mInputFile.close();
                        synchronized (this.mLatchObject) {
                            this.mLatchObject.set(true);
                            this.mLatchObject.notifyAll();
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        BackupManagerService.this.mWakelock.release();
                        return;
                    } catch (IOException e3) {
                        rawDataIn = dataInputStream;
                        rawInStream2 = fileInputStream;
                    } catch (Throwable th4) {
                        th = th4;
                        rawDataIn = dataInputStream;
                        rawInStream2 = fileInputStream;
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, true);
                        if (rawDataIn != null) {
                        }
                        if (rawInStream2 != null) {
                        }
                        this.mInputFile.close();
                        synchronized (this.mLatchObject) {
                        }
                        this.mObbConnection.tearDown();
                        sendEndRestore();
                        Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                        BackupManagerService.this.mWakelock.release();
                        throw th;
                    }
                }
                if (BackupManagerService.DEBUG) {
                    Slog.w(BackupManagerService.TAG, "Backup password mismatch; aborting");
                }
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                try {
                    this.mInputFile.close();
                } catch (IOException e22222) {
                    Slog.w(BackupManagerService.TAG, "Close of restore data pipe threw", e22222);
                }
                synchronized (this.mLatchObject) {
                    this.mLatchObject.set(true);
                    this.mLatchObject.notifyAll();
                }
                this.mObbConnection.tearDown();
                sendEndRestore();
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                BackupManagerService.this.mWakelock.release();
            } catch (IOException e4) {
                Slog.e(BackupManagerService.TAG, "Unable to read restore input");
                tearDownPipes();
                tearDownAgent(this.mTargetApp, true);
                if (rawDataIn != null) {
                }
                if (rawInStream2 != null) {
                }
                this.mInputFile.close();
                synchronized (this.mLatchObject) {
                }
                this.mObbConnection.tearDown();
                sendEndRestore();
                Slog.d(BackupManagerService.TAG, "Full restore pass complete.");
                BackupManagerService.this.mWakelock.release();
            }
        }

        String readHeaderLine(InputStream in) throws IOException {
            StringBuilder buffer = new StringBuilder(80);
            while (true) {
                int c = in.read();
                if (c >= 0 && c != 10) {
                    buffer.append((char) c);
                }
            }
            return buffer.toString();
        }

        InputStream attemptMasterKeyDecryption(String algorithm, byte[] userSalt, byte[] ckSalt, int rounds, String userIvHex, String masterKeyBlobHex, InputStream rawInStream, boolean doLog) {
            try {
                Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKey userKey = BackupManagerService.this.buildPasswordKey(algorithm, this.mDecryptPassword, userSalt, rounds);
                c.init(2, new SecretKeySpec(userKey.getEncoded(), "AES"), new IvParameterSpec(BackupManagerService.this.hexToByteArray(userIvHex)));
                byte[] mkBlob = c.doFinal(BackupManagerService.this.hexToByteArray(masterKeyBlobHex));
                int len = mkBlob[0];
                byte[] IV = Arrays.copyOfRange(mkBlob, 1, len + 1);
                int offset = len + 1;
                int offset2 = offset + 1;
                len = mkBlob[offset];
                byte[] mk = Arrays.copyOfRange(mkBlob, offset2, offset2 + len);
                offset = offset2 + len;
                offset2 = offset + 1;
                if (Arrays.equals(BackupManagerService.this.makeKeyChecksum(algorithm, mk, ckSalt, rounds), Arrays.copyOfRange(mkBlob, offset2, offset2 + mkBlob[offset]))) {
                    IvParameterSpec ivSpec = new IvParameterSpec(IV);
                    c.init(2, new SecretKeySpec(mk, "AES"), ivSpec);
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
                    byte[] userSalt = BackupManagerService.this.hexToByteArray(readHeaderLine(rawInStream));
                    byte[] ckSalt = BackupManagerService.this.hexToByteArray(readHeaderLine(rawInStream));
                    int rounds = Integer.parseInt(readHeaderLine(rawInStream));
                    String userIvHex = readHeaderLine(rawInStream);
                    String masterKeyBlobHex = readHeaderLine(rawInStream);
                    InputStream result = attemptMasterKeyDecryption(BackupManagerService.PBKDF_CURRENT, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, false);
                    if (result != null || !pbkdf2Fallback) {
                        return result;
                    }
                    return attemptMasterKeyDecryption(BackupManagerService.PBKDF_FALLBACK, userSalt, ckSalt, rounds, userIvHex, masterKeyBlobHex, rawInStream, true);
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
                            tearDownAgent(this.mTargetApp, true);
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
                        boolean okay = true;
                        switch (-getcom-android-server-backup-BackupManagerService$RestorePolicySwitchesValues()[((RestorePolicy) this.mPackagePolicies.get(pkg)).ordinal()]) {
                            case 1:
                                if (info.domain.equals("a")) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "apk present but ACCEPT");
                                    }
                                    okay = false;
                                    break;
                                }
                                break;
                            case 2:
                                if (!info.domain.equals("a")) {
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    okay = false;
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
                                return true;
                            case 3:
                                okay = false;
                                break;
                            default:
                                Slog.e(BackupManagerService.TAG, "Invalid policy from manifest");
                                okay = false;
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                break;
                        }
                        if (info.path.contains("..") || info.path.contains("//")) {
                            okay = false;
                        }
                        if (BackupManagerService.DEBUG && okay && this.mAgent != null) {
                            Slog.i(BackupManagerService.TAG, "Reusing existing agent instance");
                        }
                        if (okay && this.mAgent == null) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "Need to launch agent for " + pkg);
                            }
                            try {
                                this.mTargetApp = BackupManagerService.this.mPackageManager.getApplicationInfo(pkg, 0);
                                if (!this.mClearedPackages.contains(pkg)) {
                                    if (this.mTargetApp.backupAgentName == null) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.d(BackupManagerService.TAG, "Clearing app data preparatory to full restore");
                                        }
                                        BackupManagerService.this.clearApplicationDataSynchronous(pkg);
                                    } else if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "backup agent (" + this.mTargetApp.backupAgentName + ") => no clear");
                                    }
                                    this.mClearedPackages.add(pkg);
                                } else if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "We've initialized this app already; no clear required");
                                }
                                setUpPipes();
                                this.mAgent = BackupManagerService.this.bindToAgentSynchronous(this.mTargetApp, 3);
                                this.mAgentPackage = pkg;
                            } catch (IOException e) {
                            } catch (NameNotFoundException e2) {
                            }
                            if (this.mAgent == null) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Unable to create agent for " + pkg);
                                }
                                okay = false;
                                tearDownPipes();
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                            }
                        }
                        if (okay && (pkg.equals(this.mAgentPackage) ^ 1) != 0) {
                            Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg + " but agent is for " + this.mAgentPackage);
                            okay = false;
                        }
                        if (okay) {
                            boolean agentSuccess = true;
                            long toCopy = info.size;
                            long timeout = pkg.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE) ? 1800000 : LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS;
                            int token = BackupManagerService.this.generateToken();
                            try {
                                BackupManagerService.this.prepareOperationTimeout(token, timeout, null, 1);
                                if ("obb".equals(info.domain)) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Restoring OBB file for " + pkg + " : " + info.path);
                                    }
                                    this.mObbConnection.restoreObbFile(pkg, this.mPipes[0], info.size, info.type, info.path, info.mode, info.mtime, token, BackupManagerService.this.mBackupManagerBinder);
                                } else if ("k".equals(info.domain)) {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Restoring key-value file for " + pkg + " : " + info.path);
                                    }
                                    new Thread(new KeyValueAdbRestoreEngine(BackupManagerService.this, BackupManagerService.this.mDataDir, info, this.mPipes[0], this.mAgent, token), "restore-key-value-runner").start();
                                } else {
                                    if (BackupManagerService.DEBUG) {
                                        Slog.d(BackupManagerService.TAG, "Invoking agent to restore file " + info.path);
                                    }
                                    if (this.mTargetApp.processName.equals("system")) {
                                        Slog.d(BackupManagerService.TAG, "system process agent - spinning a thread");
                                        new Thread(new RestoreFileRunnable(this.mAgent, info, this.mPipes[0], token), "restore-sys-runner").start();
                                    } else {
                                        this.mAgent.doRestoreFile(this.mPipes[0], info.size, info.type, info.domain, info.path, info.mode, info.mtime, token, BackupManagerService.this.mBackupManagerBinder);
                                    }
                                }
                            } catch (IOException e3) {
                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                agentSuccess = false;
                                okay = false;
                            } catch (RemoteException e4) {
                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                agentSuccess = false;
                                okay = false;
                            }
                            if (okay) {
                                boolean pipeOkay = true;
                                FileOutputStream fileOutputStream = new FileOutputStream(this.mPipes[1].getFileDescriptor());
                                while (toCopy > 0) {
                                    int nRead = instream.read(buffer, 0, toCopy > ((long) buffer.length) ? buffer.length : (int) toCopy);
                                    if (nRead >= 0) {
                                        this.mBytes += (long) nRead;
                                    }
                                    if (nRead <= 0) {
                                        skipTarPadding(info.size, instream);
                                        agentSuccess = BackupManagerService.this.waitUntilOperationComplete(token);
                                    } else {
                                        toCopy -= (long) nRead;
                                        if (pipeOkay) {
                                            try {
                                                fileOutputStream.write(buffer, 0, nRead);
                                            } catch (Throwable e5) {
                                                Slog.e(BackupManagerService.TAG, "Failed to write to restore pipe", e5);
                                                pipeOkay = false;
                                            }
                                        }
                                    }
                                }
                                skipTarPadding(info.size, instream);
                                agentSuccess = BackupManagerService.this.waitUntilOperationComplete(token);
                            }
                            if (!agentSuccess) {
                                if (BackupManagerService.DEBUG) {
                                    Slog.d(BackupManagerService.TAG, "Agent failure restoring " + pkg + "; now ignoring");
                                }
                                BackupManagerService.this.mBackupHandler.removeMessages(18);
                                tearDownPipes();
                                tearDownAgent(this.mTargetApp, false);
                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                            }
                        }
                        if (!okay) {
                            if (BackupManagerService.DEBUG) {
                                Slog.d(BackupManagerService.TAG, "[discarding file content]");
                            }
                            long bytesToConsume = (info.size + 511) & -512;
                            while (bytesToConsume > 0) {
                                long nRead2 = (long) instream.read(buffer, 0, bytesToConsume > ((long) buffer.length) ? buffer.length : (int) bytesToConsume);
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
                z = true;
            } else {
                z = false;
            }
            return z;
        }

        void setUpPipes() throws IOException {
            this.mPipes = ParcelFileDescriptor.createPipe();
        }

        void tearDownPipes() {
            if (this.mPipes != null) {
                try {
                    this.mPipes[0].close();
                    this.mPipes[0] = null;
                    this.mPipes[1].close();
                    this.mPipes[1] = null;
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
                        int token = BackupManagerService.this.generateToken();
                        AdbRestoreFinishedLatch latch = new AdbRestoreFinishedLatch(token);
                        BackupManagerService.this.prepareOperationTimeout(token, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, latch, 1);
                        if (this.mTargetApp.processName.equals("system")) {
                            new Thread(new RestoreFinishedRunnable(this.mAgent, token), "restore-sys-finished-runner").start();
                        } else {
                            this.mAgent.doRestoreFinished(token, BackupManagerService.this.mBackupManagerBinder);
                        }
                        latch.await();
                    } catch (RemoteException e) {
                        Slog.d(BackupManagerService.TAG, "Lost app trying to shut down");
                    }
                }
                BackupManagerService.this.mActivityManager.unbindBackupAgent(app);
                if (app.uid < 10000 || (app.packageName.equals("com.android.backupconfirm") ^ 1) == 0) {
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Not killing after full restore");
                    }
                    this.mAgent = null;
                }
                if (BackupManagerService.DEBUG) {
                    Slog.d(BackupManagerService.TAG, "Killing host process");
                }
                BackupManagerService.this.mActivityManager.killApplicationProcess(app.processName, app.uid);
                this.mAgent = null;
            }
        }

        boolean installApk(FileMetadata info, String installerPackage, InputStream instream) {
            boolean okay = true;
            if (BackupManagerService.DEBUG) {
                Slog.d(BackupManagerService.TAG, "Installing from backup: " + info.packageName);
            }
            File apkFile = new File(BackupManagerService.this.mDataDir, info.packageName);
            try {
                FileOutputStream apkStream = new FileOutputStream(apkFile);
                byte[] buffer = new byte[32768];
                long size = info.size;
                while (size > 0) {
                    int didRead = instream.read(buffer, 0, (int) (((long) buffer.length) < size ? (long) buffer.length : size));
                    if (didRead >= 0) {
                        this.mBytes += (long) didRead;
                    }
                    apkStream.write(buffer, 0, didRead);
                    size -= (long) didRead;
                }
                apkStream.close();
                apkFile.setReadable(true, false);
                Uri packageUri = Uri.fromFile(apkFile);
                this.mInstallObserver.reset();
                BackupManagerService.this.mPackageManager.installPackage(packageUri, this.mInstallObserver, 34, installerPackage);
                this.mInstallObserver.waitForCompletion();
                if (this.mInstallObserver.getResult() != 1) {
                    if (this.mPackagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                        okay = false;
                    }
                } else {
                    boolean uninstall = false;
                    if (this.mInstallObserver.mPackageName.equals(info.packageName)) {
                        try {
                            PackageInfo pkg = BackupManagerService.this.mPackageManager.getPackageInfo(info.packageName, 64);
                            if ((pkg.applicationInfo.flags & 32768) == 0) {
                                Slog.w(BackupManagerService.TAG, "Restore stream contains apk of package " + info.packageName + " but it disallows backup/restore");
                                okay = false;
                            } else if (!BackupManagerService.signaturesMatch((Signature[]) this.mManifestSignatures.get(info.packageName), pkg)) {
                                Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " signatures do not match restore manifest");
                                okay = false;
                                uninstall = true;
                            } else if (pkg.applicationInfo.uid < 10000 && pkg.applicationInfo.backupAgentName == null) {
                                Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " has restricted uid and no agent");
                                okay = false;
                            }
                        } catch (NameNotFoundException e) {
                            Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                            okay = false;
                        }
                    } else {
                        Slog.w(BackupManagerService.TAG, "Restore stream claimed to include apk for " + info.packageName + " but apk was really " + this.mInstallObserver.mPackageName);
                        okay = false;
                        uninstall = true;
                    }
                    if (uninstall) {
                        this.mDeleteObserver.reset();
                        BackupManagerService.this.mPackageManager.deletePackage(this.mInstallObserver.mPackageName, this.mDeleteObserver, 0);
                        this.mDeleteObserver.waitForCompletion();
                    }
                }
                apkFile.delete();
                return okay;
            } catch (IOException e2) {
                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                apkFile.delete();
                return false;
            } catch (Throwable th) {
                apkFile.delete();
                throw th;
            }
        }

        void skipTarPadding(long size, InputStream instream) throws IOException {
            long partial = (size + 512) % 512;
            if (partial > 0) {
                int needed = 512 - ((int) partial);
                if (readExactly(instream, new byte[needed], 0, needed) == needed) {
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
            if (((long) readExactly(instream, buffer, 0, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                String[] str = new String[1];
                int offset = extractLine(buffer, 0, str);
                int version = Integer.parseInt(str[0]);
                if (version == 1) {
                    offset = extractLine(buffer, offset, str);
                    String pkg = str[0];
                    if (info.packageName.equals(pkg)) {
                        ByteArrayInputStream bin = new ByteArrayInputStream(buffer, offset, buffer.length - offset);
                        DataInputStream in = new DataInputStream(bin);
                        while (bin.available() > 0) {
                            int token = in.readInt();
                            int size = in.readInt();
                            if (size <= 65536) {
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
            if (((long) readExactly(instream, buffer, 0, (int) info.size)) == info.size) {
                this.mBytes += info.size;
                RestorePolicy policy = RestorePolicy.IGNORE;
                String[] str = new String[1];
                try {
                    int offset = extractLine(buffer, 0, str);
                    int version = Integer.parseInt(str[0]);
                    if (version == 1) {
                        offset = extractLine(buffer, offset, str);
                        String manifestPackage = str[0];
                        if (manifestPackage.equals(info.packageName)) {
                            offset = extractLine(buffer, offset, str);
                            version = Integer.parseInt(str[0]);
                            offset = extractLine(buffer, offset, str);
                            Integer.parseInt(str[0]);
                            offset = extractLine(buffer, offset, str);
                            info.installerPackageName = str[0].length() > 0 ? str[0] : null;
                            offset = extractLine(buffer, offset, str);
                            boolean hasApk = str[0].equals("1");
                            offset = extractLine(buffer, offset, str);
                            int numSigs = Integer.parseInt(str[0]);
                            if (numSigs > 0) {
                                Object sigs = new Signature[numSigs];
                                for (int i = 0; i < numSigs; i++) {
                                    offset = extractLine(buffer, offset, str);
                                    sigs[i] = new Signature(str[0]);
                                }
                                this.mManifestSignatures.put(info.packageName, sigs);
                                try {
                                    PackageInfo pkgInfo = BackupManagerService.this.mPackageManager.getPackageInfo(info.packageName, 64);
                                    if ((32768 & pkgInfo.applicationInfo.flags) == 0) {
                                        if (BackupManagerService.DEBUG) {
                                            Slog.i(BackupManagerService.TAG, "Restore manifest from " + info.packageName + " but allowBackup=false");
                                        }
                                        Slog.i(BackupManagerService.TAG, "Cannot restore package " + info.packageName + " without the matching .apk");
                                    } else if (pkgInfo.applicationInfo.uid >= 10000 || pkgInfo.applicationInfo.backupAgentName != null) {
                                        if (!BackupManagerService.signaturesMatch(sigs, pkgInfo)) {
                                            Slog.w(BackupManagerService.TAG, "Restore manifest signatures do not match installed application for " + info.packageName);
                                        } else if ((pkgInfo.applicationInfo.flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
                                            Slog.i(BackupManagerService.TAG, "Package has restoreAnyVersion; taking data");
                                            policy = RestorePolicy.ACCEPT;
                                        } else if (pkgInfo.versionCode >= version) {
                                            Slog.i(BackupManagerService.TAG, "Sig + version match; taking data");
                                            policy = RestorePolicy.ACCEPT;
                                        } else {
                                            Slog.d(BackupManagerService.TAG, "Data version " + version + " is newer than installed version " + pkgInfo.versionCode + " - requiring apk");
                                            policy = RestorePolicy.ACCEPT_IF_APK;
                                        }
                                        if (policy == RestorePolicy.ACCEPT_IF_APK && (hasApk ^ 1) != 0) {
                                            Slog.i(BackupManagerService.TAG, "Cannot restore package " + info.packageName + " without the matching .apk");
                                        }
                                    } else {
                                        Slog.w(BackupManagerService.TAG, "Package " + info.packageName + " is system level with no agent");
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
            while (pos < end && buffer[pos] != (byte) 10) {
                pos++;
            }
            outStr[0] = new String(buffer, offset, pos - offset);
            return pos + 1;
        }

        void dumpFileMetadata(FileMetadata info) {
            char c = 'x';
            char c2 = 'w';
            char c3 = 'r';
            if (BackupManagerService.DEBUG) {
                char c4;
                StringBuilder b = new StringBuilder(128);
                if (info.type == 2) {
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
                b.append(String.format(" %9d ", new Object[]{Long.valueOf(info.size)}));
                b.append(new SimpleDateFormat("MMM dd HH:mm:ss ").format(new Date(info.mtime)));
                b.append(info.packageName);
                b.append(" :: ");
                b.append(info.domain);
                b.append(" :: ");
                b.append(info.path);
                Slog.i(BackupManagerService.TAG, b.toString());
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x00a2  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        FileMetadata readTarHeaders(InputStream instream) throws IOException {
            IOException e;
            byte[] block = new byte[512];
            FileMetadata fileMetadata = null;
            if (readTarHeader(instream, block)) {
                try {
                    FileMetadata info = new FileMetadata();
                    try {
                        info.size = extractRadix(block, BackupManagerService.TAR_HEADER_OFFSET_FILESIZE, 12, 8);
                        info.mtime = extractRadix(block, 136, 12, 8);
                        info.mode = extractRadix(block, 100, 8, 8);
                        info.path = extractString(block, BackupManagerService.TAR_HEADER_OFFSET_PATH_PREFIX, BackupManagerService.TAR_HEADER_LENGTH_PATH_PREFIX);
                        String path = extractString(block, 0, 100);
                        if (path.length() > 0) {
                            if (info.path.length() > 0) {
                                info.path += '/';
                            }
                            info.path += path;
                        }
                        int typeChar = block[BackupManagerService.TAR_HEADER_OFFSET_TYPE_CHAR];
                        if (typeChar == 120) {
                            boolean gotHeader = readPaxExtendedHeader(instream, info);
                            if (gotHeader) {
                                gotHeader = readTarHeader(instream, block);
                            }
                            if (gotHeader) {
                                typeChar = block[BackupManagerService.TAR_HEADER_OFFSET_TYPE_CHAR];
                            } else {
                                throw new IOException("Bad or missing pax header");
                            }
                        }
                        switch (typeChar) {
                            case 0:
                                if (BackupManagerService.DEBUG) {
                                    Slog.w(BackupManagerService.TAG, "Saw type=0 in tar header block, info=" + info);
                                }
                                return null;
                            case 48:
                                info.type = 1;
                                break;
                            case 53:
                                info.type = 2;
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
                        if ("shared/".regionMatches(0, info.path, 0, "shared/".length())) {
                            info.path = info.path.substring("shared/".length());
                            info.packageName = BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE;
                            info.domain = "shared";
                            if (BackupManagerService.DEBUG) {
                                Slog.i(BackupManagerService.TAG, "File in shared storage: " + info.path);
                            }
                        } else if ("apps/".regionMatches(0, info.path, 0, "apps/".length())) {
                            info.path = info.path.substring("apps/".length());
                            int slash = info.path.indexOf(47);
                            if (slash < 0) {
                                throw new IOException("Illegal semantic path in " + info.path);
                            }
                            info.packageName = info.path.substring(0, slash);
                            info.path = info.path.substring(slash + 1);
                            if (!(info.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME) || (info.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME) ^ 1) == 0)) {
                                slash = info.path.indexOf(47);
                                if (slash < 0) {
                                    throw new IOException("Illegal semantic path in non-manifest " + info.path);
                                }
                                info.domain = info.path.substring(0, slash);
                                info.path = info.path.substring(slash + 1);
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
                    }
                    throw e;
                }
            }
            return fileMetadata;
        }

        private void HEXLOG(byte[] block) {
            int offset = 0;
            int todo = block.length;
            StringBuilder buf = new StringBuilder(64);
            while (todo > 0) {
                buf.append(String.format("%04x   ", new Object[]{Integer.valueOf(offset)}));
                int numThisLine = todo > 16 ? 16 : todo;
                for (int i = 0; i < numThisLine; i++) {
                    buf.append(String.format("%02x ", new Object[]{Byte.valueOf(block[offset + i])}));
                }
                Slog.i("hexdump", buf.toString());
                buf.setLength(0);
                todo -= numThisLine;
                offset += numThisLine;
            }
        }

        int readExactly(InputStream in, byte[] buffer, int offset, int size) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
            int soFar = 0;
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
            int got = readExactly(instream, block, 0, 512);
            if (got == 0) {
                return false;
            }
            if (got < 512) {
                throw new IOException("Unable to read full block header");
            }
            this.mBytes += 512;
            return true;
        }

        boolean readPaxExtendedHeader(InputStream instream, FileMetadata info) throws IOException {
            if (info.size > 32768) {
                Slog.w(BackupManagerService.TAG, "Suspiciously large pax header size " + info.size + " - aborting");
                throw new IOException("Sanity failure: pax header size " + info.size);
            }
            byte[] data = new byte[(((int) ((info.size + 511) >> 9)) * 512)];
            if (readExactly(instream, data, 0, data.length) < data.length) {
                throw new IOException("Unable to read full pax header");
            }
            this.mBytes += (long) data.length;
            int contentSize = (int) info.size;
            int offset = 0;
            do {
                int eol = offset + 1;
                while (eol < contentSize && data[eol] != (byte) 32) {
                    eol++;
                }
                if (eol >= contentSize) {
                    throw new IOException("Invalid pax data");
                }
                int linelen = (int) extractRadix(data, offset, eol - offset, 10);
                int key = eol + 1;
                eol = (offset + linelen) - 1;
                int value = key + 1;
                while (data[value] != (byte) 61 && value <= eol) {
                    value++;
                }
                if (value > eol) {
                    throw new IOException("Invalid pax declaration");
                }
                String keyStr = new String(data, key, value - key, "UTF-8");
                String valStr = new String(data, value + 1, (eol - value) - 1, "UTF-8");
                if ("path".equals(keyStr)) {
                    info.path = valStr;
                } else if ("size".equals(keyStr)) {
                    info.size = Long.parseLong(valStr);
                } else if (BackupManagerService.DEBUG) {
                    Slog.i(BackupManagerService.TAG, "Unhandled pax key: " + key);
                }
                offset += linelen;
            } while (offset < contentSize);
            return true;
        }

        long extractRadix(byte[] data, int offset, int maxChars, int radix) throws IOException {
            long value = 0;
            int end = offset + maxChars;
            int i = offset;
            while (i < end) {
                byte b = data[i];
                if (b == (byte) 0 || b == (byte) 32) {
                    break;
                } else if (b < (byte) 48 || b > (radix + 48) - 1) {
                    throw new IOException("Invalid number in header: '" + ((char) b) + "' for radix " + radix);
                } else {
                    value = (((long) radix) * value) + ((long) (b - 48));
                    i++;
                }
            }
            return value;
        }

        String extractString(byte[] data, int offset, int maxChars) throws IOException {
            int end = offset + maxChars;
            int eos = offset;
            while (eos < end && data[eos] != (byte) 0) {
                eos++;
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
        private volatile boolean mCancelAll;
        private final Object mCancelLock = new Object();
        private final int mCurrentOpToken;
        PackageInfo mCurrentPackage;
        BackupState mCurrentState;
        private volatile int mEphemeralOpToken;
        boolean mFinished;
        private final PerformFullTransportBackupTask mFullBackupTask;
        File mJournal;
        IBackupManagerMonitor mMonitor;
        ParcelFileDescriptor mNewState;
        File mNewStateName;
        final boolean mNonIncremental;
        IBackupObserver mObserver;
        ArrayList<BackupRequest> mOriginalQueue;
        List<String> mPendingFullBackups;
        ArrayList<BackupRequest> mQueue;
        ParcelFileDescriptor mSavedState;
        File mSavedStateName;
        File mStateDir;
        int mStatus;
        IBackupTransport mTransport;
        final boolean mUserInitiated;

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$BackupStateSwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$BackupStateSwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$BackupStateSwitchesValues;
            }
            int[] iArr = new int[BackupState.values().length];
            try {
                iArr[BackupState.FINAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[BackupState.INITIAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[BackupState.RUNNING_QUEUE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-server-backup-BackupManagerService$BackupStateSwitchesValues = iArr;
            return iArr;
        }

        public PerformBackupTask(IBackupTransport transport, String dirName, ArrayList<BackupRequest> queue, File journal, IBackupObserver observer, IBackupManagerMonitor monitor, List<String> pendingFullBackups, boolean userInitiated, boolean nonIncremental) {
            this.mTransport = transport;
            this.mOriginalQueue = queue;
            this.mQueue = new ArrayList();
            this.mJournal = journal;
            this.mObserver = observer;
            this.mMonitor = monitor;
            this.mPendingFullBackups = pendingFullBackups;
            this.mUserInitiated = userInitiated;
            this.mNonIncremental = nonIncremental;
            this.mStateDir = new File(BackupManagerService.this.mBaseStateDir, dirName);
            this.mCurrentOpToken = BackupManagerService.this.generateToken();
            this.mFinished = false;
            synchronized (BackupManagerService.this.mCurrentOpLock) {
                if (BackupManagerService.this.isBackupOperationInProgress()) {
                    if (BackupManagerService.DEBUG) {
                        Slog.d(TAG, "Skipping backup since one is already in progress.");
                    }
                    this.mCancelAll = true;
                    this.mFullBackupTask = null;
                    this.mCurrentState = BackupState.FINAL;
                    BackupManagerService.this.addBackupTrace("Skipped. Backup already in progress.");
                } else {
                    this.mCurrentState = BackupState.INITIAL;
                    BackupManagerService backupManagerService = BackupManagerService.this;
                    this.mFullBackupTask = new PerformFullTransportBackupTask(null, (String[]) this.mPendingFullBackups.toArray(new String[this.mPendingFullBackups.size()]), false, null, new CountDownLatch(1), this.mObserver, this.mMonitor, this.mUserInitiated);
                    registerTask();
                    BackupManagerService.this.addBackupTrace("STATE => INITIAL");
                }
            }
        }

        private void registerTask() {
            synchronized (BackupManagerService.this.mCurrentOpLock) {
                BackupManagerService.this.mCurrentOperations.put(this.mCurrentOpToken, new Operation(0, this, 2));
            }
        }

        private void unregisterTask() {
            BackupManagerService.this.removeOperation(this.mCurrentOpToken);
        }

        @GuardedBy("mCancelLock")
        public void execute() {
            synchronized (this.mCancelLock) {
                switch (-getcom-android-server-backup-BackupManagerService$BackupStateSwitchesValues()[this.mCurrentState.ordinal()]) {
                    case 1:
                        if (this.mFinished) {
                            Slog.e(TAG, "Duplicate finish");
                        } else {
                            finalizeBackup();
                        }
                        this.mFinished = true;
                        break;
                    case 2:
                        beginBackup();
                        break;
                    case 3:
                        invokeNextAgent();
                        break;
                }
            }
        }

        void beginBackup() {
            BackupManagerService.this.clearBackupTrace();
            StringBuilder b = new StringBuilder(256);
            b.append("beginBackup: [");
            for (BackupRequest req : this.mOriginalQueue) {
                b.append(' ');
                b.append(req.packageName);
            }
            b.append(" ]");
            BackupManagerService.this.addBackupTrace(b.toString());
            this.mAgentBinder = null;
            this.mStatus = 0;
            if (this.mOriginalQueue.isEmpty() && this.mPendingFullBackups.isEmpty()) {
                Slog.w(TAG, "Backup begun with an empty queue - nothing to do.");
                BackupManagerService.this.addBackupTrace("queue empty at begin");
                BackupManagerService.sendBackupFinished(this.mObserver, 0);
                executeNextState(BackupState.FINAL);
                return;
            }
            this.mQueue = (ArrayList) this.mOriginalQueue.clone();
            boolean skipPm = this.mNonIncremental;
            for (int i = 0; i < this.mQueue.size(); i++) {
                if (BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(((BackupRequest) this.mQueue.get(i)).packageName)) {
                    this.mQueue.remove(i);
                    skipPm = false;
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
                    BackupManagerService.this.addBackupTrace("initializing transport " + transportName);
                    BackupManagerService.this.resetBackupState(this.mStateDir);
                    this.mStatus = this.mTransport.initializeDevice();
                    BackupManagerService.this.addBackupTrace("transport.initializeDevice() == " + this.mStatus);
                    if (this.mStatus == 0) {
                        EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE, new Object[0]);
                    } else {
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                        Slog.e(TAG, "Transport error in initializeDevice()");
                    }
                }
                if (skipPm) {
                    Slog.d(TAG, "Skipping backup of package metadata.");
                    executeNextState(BackupState.RUNNING_QUEUE);
                } else if (this.mStatus == 0) {
                    this.mStatus = invokeAgentForBackup(BackupManagerService.PACKAGE_MANAGER_SENTINEL, IBackupAgent.Stub.asInterface(new PackageManagerBackupAgent(BackupManagerService.this.mPackageManager).onBind()), this.mTransport);
                    BackupManagerService.this.addBackupTrace("PMBA invoke: " + this.mStatus);
                    BackupManagerService.this.mBackupHandler.removeMessages(17);
                }
                if (this.mStatus == JobSchedulerShellCommand.CMD_ERR_NO_JOB) {
                    EventLog.writeEvent(EventLogTags.BACKUP_RESET, this.mTransport.transportDirName());
                }
                BackupManagerService.this.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    BackupManagerService.this.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Error in backup thread", e);
                BackupManagerService.this.addBackupTrace("Exception in backup thread: " + e);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                BackupManagerService.this.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    BackupManagerService.this.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            } catch (Throwable th) {
                BackupManagerService.this.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    BackupManagerService.this.resetBackupState(this.mStateDir);
                    BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
            }
        }

        void invokeNextAgent() {
            this.mStatus = 0;
            BackupManagerService.this.addBackupTrace("invoke q=" + this.mQueue.size());
            if (this.mQueue.isEmpty()) {
                executeNextState(BackupState.FINAL);
                return;
            }
            BackupRequest request = (BackupRequest) this.mQueue.get(0);
            this.mQueue.remove(0);
            Slog.d(TAG, "starting key/value backup of " + request);
            BackupManagerService.this.addBackupTrace("launch agent for " + request.packageName);
            BackupState nextState;
            try {
                this.mCurrentPackage = BackupManagerService.this.mPackageManager.getPackageInfo(request.packageName, 64);
                if (!BackupManagerService.appIsEligibleForBackup(this.mCurrentPackage.applicationInfo, BackupManagerService.this.mPackageManager)) {
                    Slog.i(TAG, "Package " + request.packageName + " no longer supports backup; skipping");
                    BackupManagerService.this.addBackupTrace("skipping - not eligible, completion is noop");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    BackupManagerService.this.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            BackupManagerService.this.dataChangedImpl(request.packageName);
                            this.mStatus = 0;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = 0;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                    }
                } else if (BackupManagerService.appGetsFullBackup(this.mCurrentPackage)) {
                    Slog.i(TAG, "Package " + request.packageName + " requests full-data rather than key/value; skipping");
                    BackupManagerService.this.addBackupTrace("skipping - fullBackupOnly, completion is noop");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    BackupManagerService.this.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            BackupManagerService.this.dataChangedImpl(request.packageName);
                            this.mStatus = 0;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = 0;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                    }
                } else if (BackupManagerService.appIsStopped(this.mCurrentPackage.applicationInfo)) {
                    BackupManagerService.this.addBackupTrace("skipping - stopped");
                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                    executeNextState(BackupState.RUNNING_QUEUE);
                    BackupManagerService.this.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            BackupManagerService.this.dataChangedImpl(request.packageName);
                            this.mStatus = 0;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = 0;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                    }
                } else {
                    try {
                        boolean z;
                        BackupManagerService.this.mWakelock.setWorkSource(new WorkSource(this.mCurrentPackage.applicationInfo.uid));
                        IBackupAgent agent = BackupManagerService.this.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, 0);
                        BackupManagerService backupManagerService = BackupManagerService.this;
                        StringBuilder append = new StringBuilder().append("agent bound; a? = ");
                        if (agent != null) {
                            z = true;
                        } else {
                            z = false;
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
                        BackupManagerService.this.addBackupTrace("agent SE");
                    }
                    BackupManagerService.this.mWakelock.setWorkSource(null);
                    if (this.mStatus != 0) {
                        nextState = BackupState.RUNNING_QUEUE;
                        this.mAgentBinder = null;
                        if (this.mStatus == -1003) {
                            BackupManagerService.this.dataChangedImpl(request.packageName);
                            this.mStatus = 0;
                            if (this.mQueue.isEmpty()) {
                                nextState = BackupState.FINAL;
                            }
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        } else if (this.mStatus == -1004) {
                            this.mStatus = 0;
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                        } else {
                            revertAndEndBackup();
                            nextState = BackupState.FINAL;
                        }
                        executeNextState(nextState);
                    } else {
                        BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                    }
                }
            } catch (NameNotFoundException e) {
                Slog.d(TAG, "Package does not exist; skipping");
                BackupManagerService.this.addBackupTrace("no such package");
                this.mStatus = -1004;
                BackupManagerService.this.mWakelock.setWorkSource(null);
                if (this.mStatus != 0) {
                    nextState = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        BackupManagerService.this.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState = BackupState.FINAL;
                        }
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = 0;
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                } else {
                    BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                }
            } catch (Throwable th) {
                BackupManagerService.this.mWakelock.setWorkSource(null);
                if (this.mStatus != 0) {
                    nextState = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        BackupManagerService.this.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState = BackupState.FINAL;
                        }
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = 0;
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                } else {
                    BackupManagerService.this.addBackupTrace("expecting completion/timeout callback");
                }
            }
        }

        void finalizeBackup() {
            BackupManagerService.this.addBackupTrace("finishing");
            for (BackupRequest req : this.mQueue) {
                BackupManagerService.this.dataChangedImpl(req.packageName);
            }
            if (!(this.mJournal == null || (this.mJournal.delete() ^ 1) == 0)) {
                Slog.e(TAG, "Unable to remove backup journal file " + this.mJournal);
            }
            if (BackupManagerService.this.mCurrentToken == 0 && this.mStatus == 0) {
                BackupManagerService.this.addBackupTrace("success; recording token");
                try {
                    BackupManagerService.this.mCurrentToken = this.mTransport.getCurrentRestoreSet();
                    BackupManagerService.this.writeRestoreTokens();
                } catch (Exception e) {
                    Slog.e(TAG, "Transport threw reporting restore set: " + e.getMessage());
                    BackupManagerService.this.addBackupTrace("transport threw returning token");
                }
            }
            synchronized (BackupManagerService.this.mQueueLock) {
                BackupManagerService.this.mBackupRunning = false;
                if (this.mStatus == JobSchedulerShellCommand.CMD_ERR_NO_JOB) {
                    BackupManagerService.this.addBackupTrace("init required; rerunning");
                    try {
                        String name = BackupManagerService.this.mTransportManager.getTransportName(this.mTransport);
                        if (name != null) {
                            BackupManagerService.this.mPendingInits.add(name);
                        } else if (BackupManagerService.DEBUG) {
                            Slog.w(TAG, "Couldn't find name of transport " + this.mTransport + " for init");
                        }
                    } catch (Exception e2) {
                        Slog.w(TAG, "Failed to query transport name for init: " + e2.getMessage());
                    }
                    clearMetadata();
                    BackupManagerService.this.backupNow();
                }
            }
            BackupManagerService.this.clearBackupTrace();
            unregisterTask();
            if (this.mCancelAll || this.mStatus != 0 || this.mPendingFullBackups == null || (this.mPendingFullBackups.isEmpty() ^ 1) == 0) {
                if (!this.mCancelAll) {
                    this.mFullBackupTask.unregisterTask();
                    switch (this.mStatus) {
                        case JobSchedulerShellCommand.CMD_ERR_NO_JOB /*-1001*/:
                            BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            break;
                        case 0:
                            BackupManagerService.sendBackupFinished(this.mObserver, 0);
                            break;
                        default:
                            BackupManagerService.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            break;
                    }
                }
                if (this.mFullBackupTask != null) {
                    this.mFullBackupTask.unregisterTask();
                }
                BackupManagerService.sendBackupFinished(this.mObserver, -2003);
            } else {
                Slog.d(TAG, "Starting full backups for: " + this.mPendingFullBackups);
                BackupManagerService.this.mWakelock.acquire();
                new Thread(this.mFullBackupTask, "full-transport-requested").start();
            }
            Slog.i(BackupManagerService.TAG, "K/V backup pass finished.");
            BackupManagerService.this.mWakelock.release();
            return;
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
            BackupManagerService.this.addBackupTrace("invoking " + packageName);
            File blankStateName = new File(this.mStateDir, "blank_state");
            this.mSavedStateName = new File(this.mStateDir, packageName);
            this.mBackupDataName = new File(BackupManagerService.this.mDataDir, packageName + ".data");
            this.mNewStateName = new File(this.mStateDir, packageName + ".new");
            this.mSavedState = null;
            this.mBackupData = null;
            this.mNewState = null;
            this.mEphemeralOpToken = BackupManagerService.this.generateToken();
            try {
                if (packageName.equals(BackupManagerService.PACKAGE_MANAGER_SENTINEL)) {
                    this.mCurrentPackage = new PackageInfo();
                    this.mCurrentPackage.packageName = packageName;
                }
                this.mSavedState = ParcelFileDescriptor.open(this.mNonIncremental ? blankStateName : this.mSavedStateName, 402653184);
                this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
                if (!SELinux.restorecon(this.mBackupDataName)) {
                    Slog.e(TAG, "SELinux restorecon failed on " + this.mBackupDataName);
                }
                this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
                long quota = this.mTransport.getBackupQuota(packageName, false);
                BackupManagerService.this.addBackupTrace("setting timeout");
                BackupManagerService.this.prepareOperationTimeout(this.mEphemeralOpToken, 30000, this, 0);
                BackupManagerService.this.addBackupTrace("calling agent doBackup()");
                agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, quota, this.mEphemeralOpToken, BackupManagerService.this.mBackupManagerBinder);
                if (this.mNonIncremental) {
                    blankStateName.delete();
                }
                BackupManagerService.this.addBackupTrace("invoke success");
                return 0;
            } catch (Exception e) {
                int i;
                Slog.e(TAG, "Error invoking for backup on " + packageName + ". " + e);
                BackupManagerService.this.addBackupTrace("exception: " + e);
                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, new Object[]{packageName, e.toString()});
                errorCleanup();
                if (false) {
                    i = -1003;
                } else {
                    i = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                }
                if (this.mNonIncremental) {
                    blankStateName.delete();
                }
                return i;
            } catch (Throwable th) {
                if (this.mNonIncremental) {
                    blankStateName.delete();
                }
                throw th;
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
                StringBuffer sb = new StringBuffer(checksum.length * 2);
                for (byte toHexString : checksum) {
                    sb.append(Integer.toHexString(toHexString));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                Slog.e(TAG, "Unable to use SHA-1!");
                return "00";
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:89:0x0108 A:{SYNTHETIC, Splitter: B:89:0x0108} */
        /* JADX WARNING: Removed duplicated region for block: B:93:0x010f A:{SYNTHETIC, Splitter: B:93:0x010f} */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x0139  */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x0116  */
        /* JADX WARNING: Removed duplicated region for block: B:89:0x0108 A:{SYNTHETIC, Splitter: B:89:0x0108} */
        /* JADX WARNING: Removed duplicated region for block: B:93:0x010f A:{SYNTHETIC, Splitter: B:93:0x010f} */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x0116  */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x0139  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0086 A:{SYNTHETIC, Splitter: B:36:0x0086} */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x008d A:{SYNTHETIC, Splitter: B:40:0x008d} */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x00b7  */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0094  */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x0086 A:{SYNTHETIC, Splitter: B:36:0x0086} */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x008d A:{SYNTHETIC, Splitter: B:40:0x008d} */
        /* JADX WARNING: Removed duplicated region for block: B:44:0x0094  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x00b7  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void writeWidgetPayloadIfAppropriate(FileDescriptor fd, String pkgName) throws IOException {
            Throwable th;
            Throwable th2;
            Throwable th3;
            Throwable th4;
            byte[] widgetState = AppWidgetBackupBridge.getWidgetState(pkgName, 0);
            File widgetFile = new File(this.mStateDir, pkgName + "_widget");
            boolean priorStateExists = widgetFile.exists();
            if (priorStateExists || widgetState != null) {
                String newChecksum = null;
                if (widgetState != null) {
                    newChecksum = SHA1Checksum(widgetState);
                    if (priorStateExists) {
                        th = null;
                        FileInputStream fin = null;
                        DataInputStream in = null;
                        try {
                            FileInputStream fin2 = new FileInputStream(widgetFile);
                            try {
                                DataInputStream in2 = new DataInputStream(fin2);
                                try {
                                    String priorChecksum = in2.readUTF();
                                    if (in2 != null) {
                                        try {
                                            in2.close();
                                        } catch (Throwable th5) {
                                            th = th5;
                                        }
                                    }
                                    if (fin2 != null) {
                                        try {
                                            fin2.close();
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
                                    } else if (Objects.equals(newChecksum, priorChecksum)) {
                                        return;
                                    }
                                } catch (Throwable th7) {
                                    th2 = th7;
                                    in = in2;
                                    fin = fin2;
                                    if (in != null) {
                                    }
                                    th3 = th;
                                    if (fin != null) {
                                    }
                                    th = th3;
                                    if (th != null) {
                                    }
                                }
                            } catch (Throwable th8) {
                                th2 = th8;
                                fin = fin2;
                                if (in != null) {
                                }
                                th3 = th;
                                if (fin != null) {
                                }
                                th = th3;
                                if (th != null) {
                                }
                            }
                        } catch (Throwable th9) {
                            th2 = th9;
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Throwable th10) {
                                    th3 = th10;
                                    if (th != null) {
                                        if (th != th3) {
                                            th.addSuppressed(th3);
                                            th3 = th;
                                        }
                                    }
                                }
                            }
                            th3 = th;
                            if (fin != null) {
                                try {
                                    fin.close();
                                } catch (Throwable th11) {
                                    th = th11;
                                    if (th3 != null) {
                                        if (th3 != th) {
                                            th3.addSuppressed(th);
                                            th = th3;
                                        }
                                    }
                                }
                            }
                            th = th3;
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
                    FileOutputStream fout = null;
                    DataOutputStream stateOut = null;
                    try {
                        FileOutputStream fout2 = new FileOutputStream(widgetFile);
                        try {
                            DataOutputStream stateOut2 = new DataOutputStream(fout2);
                            try {
                                stateOut2.writeUTF(newChecksum);
                                if (stateOut2 != null) {
                                    try {
                                        stateOut2.close();
                                    } catch (Throwable th12) {
                                        th = th12;
                                    }
                                }
                                if (fout2 != null) {
                                    try {
                                        fout2.close();
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
                                stateOut = stateOut2;
                                fout = fout2;
                                if (stateOut != null) {
                                }
                                th3 = th;
                                if (fout != null) {
                                }
                                th = th3;
                                if (th != null) {
                                }
                            }
                        } catch (Throwable th15) {
                            th2 = th15;
                            fout = fout2;
                            if (stateOut != null) {
                            }
                            th3 = th;
                            if (fout != null) {
                            }
                            th = th3;
                            if (th != null) {
                            }
                        }
                    } catch (Throwable th16) {
                        th2 = th16;
                        if (stateOut != null) {
                            try {
                                stateOut.close();
                            } catch (Throwable th17) {
                                th3 = th17;
                                if (th != null) {
                                    if (th != th3) {
                                        th.addSuppressed(th3);
                                        th3 = th;
                                    }
                                }
                            }
                        }
                        th3 = th;
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (Throwable th18) {
                                th = th18;
                                if (th3 != null) {
                                    if (th3 != th) {
                                        th3.addSuppressed(th);
                                        th = th3;
                                    }
                                }
                            }
                        }
                        th = th3;
                        if (th != null) {
                            throw th;
                        }
                        throw th2;
                    }
                }
                out.writeEntityHeader(BackupManagerService.KEY_WIDGET_STATE, -1);
                widgetFile.delete();
            }
        }

        /* JADX WARNING: Missing block: B:38:0x01cb, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @GuardedBy("mCancelLock")
        public void operationComplete(long unusedResult) {
            BackupManagerService.this.removeOperation(this.mEphemeralOpToken);
            synchronized (this.mCancelLock) {
                if (this.mFinished) {
                    Slog.d(TAG, "operationComplete received after task finished.");
                    return;
                } else if (this.mBackupData == null) {
                    BackupManagerService.this.addBackupTrace("late opComplete; curPkg = " + (this.mCurrentPackage != null ? this.mCurrentPackage.packageName : "[none]"));
                    return;
                } else {
                    BackupState nextState;
                    String pkgName = this.mCurrentPackage.packageName;
                    long filepos = this.mBackupDataName.length();
                    FileDescriptor fd = this.mBackupData.getFileDescriptor();
                    ParcelFileDescriptor readFd;
                    try {
                        if (this.mCurrentPackage.applicationInfo != null && (this.mCurrentPackage.applicationInfo.flags & 1) == 0) {
                            readFd = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                            BackupDataInput in = new BackupDataInput(readFd.getFileDescriptor());
                            while (in.readNextHeader()) {
                                String key = in.getKey();
                                if (key == null || key.charAt(0) < 65280) {
                                    in.skipEntityData();
                                } else {
                                    failAgent(this.mAgentBinder, "Illegal backup key: " + key);
                                    BackupManagerService.this.addBackupTrace("illegal key " + key + " from " + pkgName);
                                    EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, new Object[]{pkgName, "bad key"});
                                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 5, this.mCurrentPackage, 3, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_ILLEGAL_KEY", key));
                                    BackupManagerService.this.mBackupHandler.removeMessages(17);
                                    BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, -1003);
                                    errorCleanup();
                                    if (readFd != null) {
                                        readFd.close();
                                    }
                                }
                            }
                            if (readFd != null) {
                                readFd.close();
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
                    BackupManagerService.this.mBackupHandler.removeMessages(17);
                    clearAgentState();
                    BackupManagerService.this.addBackupTrace("operation complete");
                    ParcelFileDescriptor backupData = null;
                    this.mStatus = 0;
                    long size = 0;
                    try {
                        size = this.mBackupDataName.length();
                        if (size > 0) {
                            if (this.mStatus == 0) {
                                backupData = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                                BackupManagerService.this.addBackupTrace("sending data to transport");
                                this.mStatus = this.mTransport.performBackup(this.mCurrentPackage, backupData, this.mUserInitiated ? 1 : 0);
                            }
                            BackupManagerService.this.addBackupTrace("data delivered: " + this.mStatus);
                            if (this.mStatus == 0) {
                                BackupManagerService.this.addBackupTrace("finishing op on transport");
                                this.mStatus = this.mTransport.finishBackup();
                                BackupManagerService.this.addBackupTrace("finished: " + this.mStatus);
                            } else if (this.mStatus == -1002) {
                                BackupManagerService.this.addBackupTrace("transport rejected package");
                            }
                        } else {
                            BackupManagerService.this.addBackupTrace("no data to send");
                            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 7, this.mCurrentPackage, 3, null);
                        }
                        if (this.mStatus == 0) {
                            this.mBackupDataName.delete();
                            this.mNewStateName.renameTo(this.mSavedStateName);
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, 0);
                            EventLog.writeEvent(EventLogTags.BACKUP_PACKAGE, new Object[]{pkgName, Long.valueOf(size)});
                            BackupManagerService.this.logBackupComplete(pkgName);
                        } else if (this.mStatus == -1002) {
                            this.mBackupDataName.delete();
                            this.mNewStateName.delete();
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                            EventLogTags.writeBackupAgentFailure(pkgName, "Transport rejected");
                        } else if (this.mStatus == -1005) {
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, -1005);
                            EventLog.writeEvent(EventLogTags.BACKUP_QUOTA_EXCEEDED, pkgName);
                        } else {
                            BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                        }
                        if (backupData != null) {
                            try {
                                backupData.close();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (Exception e4) {
                        BackupManagerService.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        Slog.e(TAG, "Transport error backing up " + pkgName, e4);
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                        this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        if (backupData != null) {
                            try {
                                backupData.close();
                            } catch (IOException e5) {
                            }
                        }
                    } catch (Throwable th2) {
                        if (backupData != null) {
                            try {
                                backupData.close();
                            } catch (IOException e6) {
                            }
                        }
                    }
                    if (this.mStatus == 0 || this.mStatus == -1002) {
                        nextState = this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE;
                    } else if (this.mStatus == -1005) {
                        if (this.mAgentBinder != null) {
                            try {
                                this.mAgentBinder.doQuotaExceeded(size, this.mTransport.getBackupQuota(this.mCurrentPackage.packageName, false));
                            } catch (Exception e42) {
                                Slog.e(TAG, "Unable to notify about quota exceeded: " + e42.getMessage());
                            }
                        }
                        nextState = this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE;
                    } else {
                        revertAndEndBackup();
                        nextState = BackupState.FINAL;
                    }
                    executeNextState(nextState);
                    return;
                }
            }
        }

        /* JADX WARNING: Missing block: B:18:0x0092, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        @GuardedBy("mCancelLock")
        public void handleCancel(boolean cancelAll) {
            BackupManagerService.this.removeOperation(this.mEphemeralOpToken);
            synchronized (this.mCancelLock) {
                if (this.mFinished) {
                    return;
                }
                String logPackageName;
                this.mCancelAll = cancelAll;
                if (this.mCurrentPackage != null) {
                    logPackageName = this.mCurrentPackage.packageName;
                } else {
                    logPackageName = "no_package_yet";
                }
                Slog.i(TAG, "Cancel backing up " + logPackageName);
                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, logPackageName);
                BackupManagerService.this.addBackupTrace("cancel of " + logPackageName + ", cancelAll=" + cancelAll);
                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 21, this.mCurrentPackage, 2, BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_CANCEL_ALL", this.mCancelAll));
                errorCleanup();
                if (cancelAll) {
                    finalizeBackup();
                } else {
                    executeNextState(this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE);
                    BackupManagerService.this.dataChangedImpl(this.mCurrentPackage.packageName);
                }
            }
        }

        void revertAndEndBackup() {
            long delay;
            BackupManagerService.this.addBackupTrace("transport error; reverting");
            try {
                delay = this.mTransport.requestBackupTime();
            } catch (Exception e) {
                Slog.w(TAG, "Unable to contact transport for recommended backoff: " + e.getMessage());
                delay = 0;
            }
            KeyValueBackupJob.schedule(BackupManagerService.this.mContext, delay);
            for (BackupRequest request : this.mOriginalQueue) {
                BackupManagerService.this.dataChangedImpl(request.packageName);
            }
        }

        void errorCleanup() {
            this.mBackupDataName.delete();
            this.mNewStateName.delete();
            clearAgentState();
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
            synchronized (BackupManagerService.this.mCurrentOpLock) {
                BackupManagerService.this.mCurrentOperations.remove(this.mEphemeralOpToken);
                this.mNewState = null;
                this.mBackupData = null;
                this.mSavedState = null;
            }
            if (this.mCurrentPackage.applicationInfo != null) {
                BackupManagerService.this.addBackupTrace("unbinding " + this.mCurrentPackage.packageName);
                try {
                    BackupManagerService.this.mActivityManager.unbindBackupAgent(this.mCurrentPackage.applicationInfo);
                } catch (RemoteException e4) {
                }
            }
        }

        void executeNextState(BackupState nextState) {
            BackupManagerService.this.addBackupTrace("executeNextState => " + nextState);
            this.mCurrentState = nextState;
            BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(20, this));
        }
    }

    class PerformClearTask implements Runnable {
        PackageInfo mPackage;
        IBackupTransport mTransport;

        PerformClearTask(IBackupTransport transport, PackageInfo packageInfo) {
            this.mTransport = transport;
            this.mPackage = packageInfo;
        }

        public void run() {
            try {
                new File(new File(BackupManagerService.this.mBaseStateDir, this.mTransport.transportDirName()), this.mPackage.packageName).delete();
                this.mTransport.clearBackupData(this.mPackage);
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Transport threw clearing data for " + this.mPackage + ": " + e.getMessage());
            } finally {
                try {
                    this.mTransport.finishBackup();
                } catch (Exception e2) {
                    Slog.e(BackupManagerService.TAG, "Unable to mark clear operation finished: " + e2.getMessage());
                }
                BackupManagerService.this.mWakelock.release();
            }
        }
    }

    class PerformFullTransportBackupTask extends FullBackupTask implements BackupRestoreTask {
        static final String TAG = "PFTBT";
        IBackupObserver mBackupObserver;
        SinglePackageBackupRunner mBackupRunner;
        private final int mBackupRunnerOpToken;
        private volatile boolean mCancelAll;
        private final Object mCancelLock = new Object();
        private final int mCurrentOpToken;
        PackageInfo mCurrentPackage;
        private volatile boolean mIsDoingBackup;
        FullBackupJob mJob;
        CountDownLatch mLatch;
        IBackupManagerMonitor mMonitor;
        ArrayList<PackageInfo> mPackages;
        private volatile IBackupTransport mTransport;
        boolean mUpdateSchedule;
        boolean mUserInitiated;

        class SinglePackageBackupPreflight implements BackupRestoreTask, FullBackupPreflight {
            private final int mCurrentOpToken;
            final CountDownLatch mLatch = new CountDownLatch(1);
            final long mQuota;
            final AtomicLong mResult = new AtomicLong(-1003);
            final IBackupTransport mTransport;

            SinglePackageBackupPreflight(IBackupTransport transport, long quota, int currentOpToken) {
                this.mTransport = transport;
                this.mQuota = quota;
                this.mCurrentOpToken = currentOpToken;
            }

            public int preflightFullBackup(PackageInfo pkg, IBackupAgent agent) {
                int result;
                try {
                    BackupManagerService.this.prepareOperationTimeout(this.mCurrentOpToken, BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, this, 0);
                    BackupManagerService.this.addBackupTrace("preflighting");
                    agent.doMeasureFullBackup(this.mQuota, this.mCurrentOpToken, BackupManagerService.this.mBackupManagerBinder);
                    this.mLatch.await(BackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL, TimeUnit.MILLISECONDS);
                    long totalSize = this.mResult.get();
                    if (totalSize < 0) {
                        return (int) totalSize;
                    }
                    result = this.mTransport.checkFullBackupSize(totalSize);
                    if (result == -1005) {
                        agent.doQuotaExceeded(totalSize, this.mQuota);
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
                BackupManagerService.this.removeOperation(this.mCurrentOpToken);
            }

            public void handleCancel(boolean cancelAll) {
                this.mResult.set(-1003);
                this.mLatch.countDown();
                BackupManagerService.this.removeOperation(this.mCurrentOpToken);
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
            final CountDownLatch mBackupLatch = new CountDownLatch(1);
            private volatile int mBackupResult = -1003;
            private final int mCurrentOpToken;
            private FullBackupEngine mEngine;
            private final int mEphemeralToken;
            private volatile boolean mIsCancelled;
            final ParcelFileDescriptor mOutput;
            final SinglePackageBackupPreflight mPreflight;
            final CountDownLatch mPreflightLatch = new CountDownLatch(1);
            private volatile int mPreflightResult = -1003;
            private final long mQuota;
            final PackageInfo mTarget;

            SinglePackageBackupRunner(ParcelFileDescriptor output, PackageInfo target, IBackupTransport transport, long quota, int currentOpToken) throws IOException {
                this.mOutput = ParcelFileDescriptor.dup(output.getFileDescriptor());
                this.mTarget = target;
                this.mCurrentOpToken = currentOpToken;
                this.mEphemeralToken = BackupManagerService.this.generateToken();
                this.mPreflight = new SinglePackageBackupPreflight(transport, quota, this.mEphemeralToken);
                this.mQuota = quota;
                registerTask();
            }

            void registerTask() {
                synchronized (BackupManagerService.this.mCurrentOpLock) {
                    BackupManagerService.this.mCurrentOperations.put(this.mCurrentOpToken, new Operation(0, this, 0));
                }
            }

            void unregisterTask() {
                synchronized (BackupManagerService.this.mCurrentOpLock) {
                    BackupManagerService.this.mCurrentOperations.remove(this.mCurrentOpToken);
                }
            }

            public void run() {
                this.mEngine = new FullBackupEngine(new FileOutputStream(this.mOutput.getFileDescriptor()), this.mPreflight, this.mTarget, false, this, this.mQuota, this.mCurrentOpToken);
                try {
                    if (!this.mIsCancelled) {
                        this.mPreflightResult = this.mEngine.preflightCheck();
                    }
                    this.mPreflightLatch.countDown();
                    if (this.mPreflightResult == 0 && !this.mIsCancelled) {
                        this.mBackupResult = this.mEngine.backupOnePackage();
                    }
                    unregisterTask();
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
                        unregisterTask();
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
                    if (this.mIsCancelled) {
                        return -2003;
                    }
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
                    if (this.mIsCancelled) {
                        return -2003;
                    }
                    return this.mBackupResult;
                } catch (InterruptedException e) {
                    return -1003;
                }
            }

            public void execute() {
            }

            public void operationComplete(long result) {
            }

            public void handleCancel(boolean cancelAll) {
                if (BackupManagerService.DEBUG) {
                    Slog.w(PerformFullTransportBackupTask.TAG, "Full backup cancel of " + this.mTarget.packageName);
                }
                PerformFullTransportBackupTask.this.mMonitor = BackupManagerService.monitorEvent(PerformFullTransportBackupTask.this.mMonitor, 4, PerformFullTransportBackupTask.this.mCurrentPackage, 2, null);
                this.mIsCancelled = true;
                BackupManagerService.this.handleCancel(this.mEphemeralToken, cancelAll);
                BackupManagerService.this.tearDownAgentAndKill(this.mTarget.applicationInfo);
                this.mPreflightLatch.countDown();
                this.mBackupLatch.countDown();
                BackupManagerService.this.removeOperation(this.mCurrentOpToken);
            }
        }

        PerformFullTransportBackupTask(IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, boolean userInitiated) {
            super(observer);
            this.mUpdateSchedule = updateSchedule;
            this.mLatch = latch;
            this.mJob = runningJob;
            this.mPackages = new ArrayList(whichPackages.length);
            this.mBackupObserver = backupObserver;
            this.mMonitor = monitor;
            this.mUserInitiated = userInitiated;
            this.mCurrentOpToken = BackupManagerService.this.generateToken();
            this.mBackupRunnerOpToken = BackupManagerService.this.generateToken();
            if (BackupManagerService.this.isBackupOperationInProgress()) {
                if (BackupManagerService.DEBUG) {
                    Slog.d(TAG, "Skipping full backup. A backup is already in progress.");
                }
                this.mCancelAll = true;
                return;
            }
            registerTask();
            for (String pkg : whichPackages) {
                try {
                    PackageInfo info = BackupManagerService.this.mPackageManager.getPackageInfo(pkg, 64);
                    this.mCurrentPackage = info;
                    if (!BackupManagerService.appIsEligibleForBackup(info.applicationInfo, BackupManagerService.this.mPackageManager)) {
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 9, this.mCurrentPackage, 3, null);
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else if (!BackupManagerService.appGetsFullBackup(info)) {
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 10, this.mCurrentPackage, 3, null);
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else if (BackupManagerService.appIsStopped(info.applicationInfo)) {
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 11, this.mCurrentPackage, 3, null);
                        BackupManagerService.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                    } else {
                        this.mPackages.add(info);
                    }
                } catch (NameNotFoundException e) {
                    Slog.i(TAG, "Requested package " + pkg + " not found; ignoring");
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 12, this.mCurrentPackage, 3, null);
                }
            }
        }

        private void registerTask() {
            synchronized (BackupManagerService.this.mCurrentOpLock) {
                Slog.d(TAG, "backupmanager pftbt token=" + Integer.toHexString(this.mCurrentOpToken));
                BackupManagerService.this.mCurrentOperations.put(this.mCurrentOpToken, new Operation(0, this, 2));
            }
        }

        private void unregisterTask() {
            BackupManagerService.this.removeOperation(this.mCurrentOpToken);
        }

        public void execute() {
        }

        public void handleCancel(boolean cancelAll) {
            synchronized (this.mCancelLock) {
                if (!cancelAll) {
                    Slog.wtf(TAG, "Expected cancelAll to be true.");
                }
                if (this.mCancelAll) {
                    Slog.d(TAG, "Ignoring duplicate cancel call.");
                    return;
                }
                this.mCancelAll = true;
                if (this.mIsDoingBackup) {
                    BackupManagerService.this.handleCancel(this.mBackupRunnerOpToken, cancelAll);
                    try {
                        this.mTransport.cancelFullBackup();
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Error calling cancelFullBackup() on transport: " + e);
                    }
                }
            }
            return;
        }

        public void operationComplete(long result) {
        }

        /* JADX WARNING: Missing block: B:105:0x0307, code:
            if (r13 != 0) goto L_0x03ee;
     */
        /* JADX WARNING: Missing block: B:106:0x0309, code:
            r34[0].close();
            r34[0] = null;
            new java.lang.Thread(r36.mBackupRunner, "package-backup-bridge").start();
            r0 = new java.io.FileInputStream(r21[0].getFileDescriptor());
            r0 = new java.io.FileOutputStream(r34[1].getFileDescriptor());
            r32 = 0;
            r30 = r36.mBackupRunner.getPreflightResultBlocking();
     */
        /* JADX WARNING: Missing block: B:107:0x034c, code:
            if (r30 >= 0) goto L_0x0561;
     */
        /* JADX WARNING: Missing block: B:108:0x034e, code:
            r36.mMonitor = com.android.server.backup.BackupManagerService.-wrap0(r36.mMonitor, 16, r36.mCurrentPackage, 3, com.android.server.backup.BackupManagerService.-wrap4(r36.this$0, null, "android.app.backup.extra.LOG_PREFLIGHT_ERROR", r30));
            r13 = (int) r30;
     */
        /* JADX WARNING: Missing block: B:109:0x0372, code:
            r17 = r36.mBackupRunner.getBackupResultBlocking();
            r6 = r36.mCancelLock;
     */
        /* JADX WARNING: Missing block: B:110:0x037e, code:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:113:?, code:
            r36.mIsDoingBackup = false;
     */
        /* JADX WARNING: Missing block: B:114:0x0388, code:
            if (r36.mCancelAll != false) goto L_0x0398;
     */
        /* JADX WARNING: Missing block: B:115:0x038a, code:
            if (r17 != 0) goto L_0x0694;
     */
        /* JADX WARNING: Missing block: B:116:0x038c, code:
            r22 = r36.mTransport.finishBackup();
     */
        /* JADX WARNING: Missing block: B:117:0x0394, code:
            if (r13 != 0) goto L_0x0398;
     */
        /* JADX WARNING: Missing block: B:118:0x0396, code:
            r13 = r22;
     */
        /* JADX WARNING: Missing block: B:120:?, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:121:0x0399, code:
            if (r13 != 0) goto L_0x039f;
     */
        /* JADX WARNING: Missing block: B:122:0x039b, code:
            if (r17 == 0) goto L_0x039f;
     */
        /* JADX WARNING: Missing block: B:123:0x039d, code:
            r13 = r17;
     */
        /* JADX WARNING: Missing block: B:124:0x039f, code:
            if (r13 == 0) goto L_0x03c8;
     */
        /* JADX WARNING: Missing block: B:125:0x03a1, code:
            android.util.Slog.e(TAG, "Error " + r13 + " backing up " + r29);
     */
        /* JADX WARNING: Missing block: B:126:0x03c8, code:
            r14 = r36.mTransport.requestFullBackupTime();
     */
        /* JADX WARNING: Missing block: B:127:0x03d2, code:
            if (com.android.server.backup.BackupManagerService.DEBUG_SCHEDULING == false) goto L_0x03ee;
     */
        /* JADX WARNING: Missing block: B:128:0x03d4, code:
            android.util.Slog.i(TAG, "Transport suggested backoff=" + r14);
     */
        /* JADX WARNING: Missing block: B:130:0x03f2, code:
            if (r36.mUpdateSchedule == false) goto L_0x0401;
     */
        /* JADX WARNING: Missing block: B:131:0x03f4, code:
            r36.this$0.enqueueFullBackup(r29, java.lang.System.currentTimeMillis());
     */
        /* JADX WARNING: Missing block: B:133:0x0403, code:
            if (r13 != com.android.server.job.JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS) goto L_0x06a0;
     */
        /* JADX WARNING: Missing block: B:134:0x0405, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
     */
        /* JADX WARNING: Missing block: B:135:0x0412, code:
            if (com.android.server.backup.BackupManagerService.DEBUG == false) goto L_0x0437;
     */
        /* JADX WARNING: Missing block: B:136:0x0414, code:
            android.util.Slog.i(TAG, "Transport rejected backup of " + r29 + ", skipping");
     */
        /* JADX WARNING: Missing block: B:137:0x0437, code:
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_AGENT_FAILURE, new java.lang.Object[]{r29, "transport rejected"});
     */
        /* JADX WARNING: Missing block: B:138:0x0448, code:
            cleanUpPipes(r34);
            cleanUpPipes(r21);
     */
        /* JADX WARNING: Missing block: B:139:0x0458, code:
            if (r8.applicationInfo == null) goto L_0x04a0;
     */
        /* JADX WARNING: Missing block: B:140:0x045a, code:
            android.util.Slog.i(TAG, "Unbinding agent in " + r29);
            r36.this$0.addBackupTrace("unbinding " + r29);
     */
        /* JADX WARNING: Missing block: B:142:?, code:
            com.android.server.backup.BackupManagerService.-get0(r36.this$0).unbindBackupAgent(r8.applicationInfo);
     */
        /* JADX WARNING: Missing block: B:148:0x04a7, code:
            r20 = move-exception;
     */
        /* JADX WARNING: Missing block: B:149:0x04a8, code:
            r16 = com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
     */
        /* JADX WARNING: Missing block: B:151:?, code:
            android.util.Slog.w(TAG, "Exception trying full transport backup", r20);
            r36.mMonitor = com.android.server.backup.BackupManagerService.-wrap0(r36.mMonitor, 19, r36.mCurrentPackage, 3, com.android.server.backup.BackupManagerService.-wrap3(r36.this$0, null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", android.util.Log.getStackTraceString(r20)));
     */
        /* JADX WARNING: Missing block: B:153:0x04df, code:
            if (r36.mCancelAll != false) goto L_0x04e1;
     */
        /* JADX WARNING: Missing block: B:154:0x04e1, code:
            r16 = -2003;
     */
        /* JADX WARNING: Missing block: B:156:0x04e5, code:
            if (com.android.server.backup.BackupManagerService.DEBUG != false) goto L_0x04e7;
     */
        /* JADX WARNING: Missing block: B:157:0x04e7, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
     */
        /* JADX WARNING: Missing block: B:158:0x0503, code:
            com.android.server.backup.BackupManagerService.-wrap20(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
        /* JADX WARNING: Missing block: B:159:0x0521, code:
            if (r36.mJob != null) goto L_0x0523;
     */
        /* JADX WARNING: Missing block: B:160:0x0523, code:
            r36.mJob.finishBackupPass();
     */
        /* JADX WARNING: Missing block: B:162:0x0530, code:
            monitor-enter(r36.this$0.mQueueLock);
     */
        /* JADX WARNING: Missing block: B:164:?, code:
            r36.this$0.mRunningFullBackupTask = null;
     */
        /* JADX WARNING: Missing block: B:166:0x0539, code:
            r36.mLatch.countDown();
     */
        /* JADX WARNING: Missing block: B:167:0x0544, code:
            if (r36.mUpdateSchedule != false) goto L_0x0546;
     */
        /* JADX WARNING: Missing block: B:168:0x0546, code:
            r36.this$0.scheduleNextFullBackupJob(r14);
     */
        /* JADX WARNING: Missing block: B:169:0x054d, code:
            android.util.Slog.i(com.android.server.backup.BackupManagerService.TAG, "Full data backup pass finished.");
            r36.this$0.mWakelock.release();
     */
        /* JADX WARNING: Missing block: B:172:?, code:
            r27 = r0.read(r18);
     */
        /* JADX WARNING: Missing block: B:173:0x056b, code:
            if (r27 <= 0) goto L_0x05b0;
     */
        /* JADX WARNING: Missing block: B:174:0x056d, code:
            r0.write(r18, 0, r27);
            r6 = r36.mCancelLock;
     */
        /* JADX WARNING: Missing block: B:175:0x057b, code:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:178:0x0580, code:
            if (r36.mCancelAll != false) goto L_0x058c;
     */
        /* JADX WARNING: Missing block: B:179:0x0582, code:
            r13 = r36.mTransport.sendBackupData(r27);
     */
        /* JADX WARNING: Missing block: B:181:?, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:182:0x058d, code:
            r32 = r32 + ((long) r27);
     */
        /* JADX WARNING: Missing block: B:183:0x0596, code:
            if (r36.mBackupObserver == null) goto L_0x05b0;
     */
        /* JADX WARNING: Missing block: B:185:0x059c, code:
            if (r30 <= 0) goto L_0x05b0;
     */
        /* JADX WARNING: Missing block: B:186:0x059e, code:
            com.android.server.backup.BackupManagerService.-wrap22(r36.mBackupObserver, r29, new android.app.backup.BackupProgress(r30, r32));
     */
        /* JADX WARNING: Missing block: B:187:0x05b0, code:
            if (r27 <= 0) goto L_0x05b4;
     */
        /* JADX WARNING: Missing block: B:188:0x05b2, code:
            if (r13 == 0) goto L_0x0563;
     */
        /* JADX WARNING: Missing block: B:190:0x05b6, code:
            if (r13 != -1005) goto L_0x0372;
     */
        /* JADX WARNING: Missing block: B:191:0x05b8, code:
            android.util.Slog.w(TAG, "Package hit quota limit in-flight " + r29 + ": " + r32 + " of " + r10);
            r36.mMonitor = com.android.server.backup.BackupManagerService.-wrap0(r36.mMonitor, 18, r36.mCurrentPackage, 1, null);
            r36.mBackupRunner.sendQuotaExceeded(r32, r10);
     */
        /* JADX WARNING: Missing block: B:194:0x0610, code:
            if (r36.mCancelAll != false) goto L_0x0612;
     */
        /* JADX WARNING: Missing block: B:195:0x0612, code:
            r16 = -2003;
     */
        /* JADX WARNING: Missing block: B:197:0x0616, code:
            if (com.android.server.backup.BackupManagerService.DEBUG != false) goto L_0x0618;
     */
        /* JADX WARNING: Missing block: B:198:0x0618, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
     */
        /* JADX WARNING: Missing block: B:199:0x0634, code:
            com.android.server.backup.BackupManagerService.-wrap20(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
        /* JADX WARNING: Missing block: B:200:0x0652, code:
            if (r36.mJob != null) goto L_0x0654;
     */
        /* JADX WARNING: Missing block: B:201:0x0654, code:
            r36.mJob.finishBackupPass();
     */
        /* JADX WARNING: Missing block: B:203:0x0661, code:
            monitor-enter(r36.this$0.mQueueLock);
     */
        /* JADX WARNING: Missing block: B:205:?, code:
            r36.this$0.mRunningFullBackupTask = null;
     */
        /* JADX WARNING: Missing block: B:207:0x066a, code:
            r36.mLatch.countDown();
     */
        /* JADX WARNING: Missing block: B:208:0x0675, code:
            if (r36.mUpdateSchedule != false) goto L_0x0677;
     */
        /* JADX WARNING: Missing block: B:209:0x0677, code:
            r36.this$0.scheduleNextFullBackupJob(r14);
     */
        /* JADX WARNING: Missing block: B:210:0x067e, code:
            android.util.Slog.i(com.android.server.backup.BackupManagerService.TAG, "Full data backup pass finished.");
            r36.this$0.mWakelock.release();
     */
        /* JADX WARNING: Missing block: B:217:?, code:
            r36.mTransport.cancelFullBackup();
     */
        /* JADX WARNING: Missing block: B:223:0x06a2, code:
            if (r13 != -1005) goto L_0x06d8;
     */
        /* JADX WARNING: Missing block: B:224:0x06a4, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, -1005);
     */
        /* JADX WARNING: Missing block: B:225:0x06b1, code:
            if (com.android.server.backup.BackupManagerService.DEBUG == false) goto L_0x0448;
     */
        /* JADX WARNING: Missing block: B:226:0x06b3, code:
            android.util.Slog.i(TAG, "Transport quota exceeded for package: " + r29);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_QUOTA_EXCEEDED, r29);
     */
        /* JADX WARNING: Missing block: B:228:0x06da, code:
            if (r13 != -1003) goto L_0x0715;
     */
        /* JADX WARNING: Missing block: B:229:0x06dc, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, -1003);
            android.util.Slog.w(TAG, "Application failure for package: " + r29);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.BACKUP_AGENT_FAILURE, r29);
            r36.this$0.tearDownAgentAndKill(r8.applicationInfo);
     */
        /* JADX WARNING: Missing block: B:231:0x0717, code:
            if (r13 != -2003) goto L_0x0761;
     */
        /* JADX WARNING: Missing block: B:232:0x0719, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, -2003);
            android.util.Slog.w(TAG, "Backup cancelled. package=" + r29 + ", cancelAll=" + r36.mCancelAll);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_CANCELLED, r29);
            r36.this$0.tearDownAgentAndKill(r8.applicationInfo);
     */
        /* JADX WARNING: Missing block: B:233:0x0761, code:
            if (r13 == 0) goto L_0x081a;
     */
        /* JADX WARNING: Missing block: B:234:0x0763, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            android.util.Slog.w(TAG, "Transport failed; aborting backup: " + r13);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_TRANSPORT_FAILURE, new java.lang.Object[0]);
     */
        /* JADX WARNING: Missing block: B:235:0x0790, code:
            r16 = com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
     */
        /* JADX WARNING: Missing block: B:236:0x0796, code:
            if (r36.mCancelAll == false) goto L_0x079a;
     */
        /* JADX WARNING: Missing block: B:237:0x0798, code:
            r16 = -2003;
     */
        /* JADX WARNING: Missing block: B:239:0x079c, code:
            if (com.android.server.backup.BackupManagerService.DEBUG == false) goto L_0x07ba;
     */
        /* JADX WARNING: Missing block: B:240:0x079e, code:
            android.util.Slog.i(TAG, "Full backup completed with status: " + r16);
     */
        /* JADX WARNING: Missing block: B:241:0x07ba, code:
            com.android.server.backup.BackupManagerService.-wrap20(r36.mBackupObserver, r16);
            cleanUpPipes(r34);
            cleanUpPipes(r21);
            unregisterTask();
     */
        /* JADX WARNING: Missing block: B:242:0x07d8, code:
            if (r36.mJob == null) goto L_0x07e1;
     */
        /* JADX WARNING: Missing block: B:243:0x07da, code:
            r36.mJob.finishBackupPass();
     */
        /* JADX WARNING: Missing block: B:244:0x07e1, code:
            r6 = r36.this$0.mQueueLock;
     */
        /* JADX WARNING: Missing block: B:245:0x07e7, code:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:247:?, code:
            r36.this$0.mRunningFullBackupTask = null;
     */
        /* JADX WARNING: Missing block: B:248:0x07ef, code:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:249:0x07f0, code:
            r36.mLatch.countDown();
     */
        /* JADX WARNING: Missing block: B:250:0x07fb, code:
            if (r36.mUpdateSchedule == false) goto L_0x0804;
     */
        /* JADX WARNING: Missing block: B:251:0x07fd, code:
            r36.this$0.scheduleNextFullBackupJob(r14);
     */
        /* JADX WARNING: Missing block: B:252:0x0804, code:
            android.util.Slog.i(com.android.server.backup.BackupManagerService.TAG, "Full data backup pass finished.");
            r36.this$0.mWakelock.release();
     */
        /* JADX WARNING: Missing block: B:253:0x0816, code:
            return;
     */
        /* JADX WARNING: Missing block: B:258:?, code:
            com.android.server.backup.BackupManagerService.-wrap21(r36.mBackupObserver, r29, 0);
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_SUCCESS, r29);
            r36.this$0.logBackupComplete(r29);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            int i;
            ParcelFileDescriptor[] enginePipes = null;
            ParcelFileDescriptor[] transportPipes = null;
            long backoff = 0;
            int backupRunStatus = 0;
            if (BackupManagerService.this.mEnabled && (BackupManagerService.this.mProvisioned ^ 1) == 0) {
                this.mTransport = BackupManagerService.this.mTransportManager.getCurrentTransportBinder();
                if (this.mTransport == null) {
                    Slog.w(TAG, "Transport not present; full data backup not performed");
                    backupRunStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 15, this.mCurrentPackage, 1, null);
                    if (this.mCancelAll) {
                        backupRunStatus = -2003;
                    }
                    if (BackupManagerService.DEBUG) {
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                    }
                    BackupManagerService.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                    cleanUpPipes(null);
                    cleanUpPipes(null);
                    unregisterTask();
                    if (this.mJob != null) {
                        this.mJob.finishBackupPass();
                    }
                    synchronized (BackupManagerService.this.mQueueLock) {
                        BackupManagerService.this.mRunningFullBackupTask = null;
                    }
                    this.mLatch.countDown();
                    if (this.mUpdateSchedule) {
                        BackupManagerService.this.scheduleNextFullBackupJob(0);
                    }
                    Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                    BackupManagerService.this.mWakelock.release();
                    return;
                }
                int N = this.mPackages.size();
                byte[] buffer = new byte[8192];
                i = 0;
                while (i < N) {
                    PackageInfo currentPackage = (PackageInfo) this.mPackages.get(i);
                    String packageName = currentPackage.packageName;
                    if (BackupManagerService.DEBUG) {
                        Slog.i(TAG, "Initiating full-data transport backup of " + packageName + " token: " + this.mCurrentOpToken);
                    }
                    EventLog.writeEvent(EventLogTags.FULL_BACKUP_PACKAGE, packageName);
                    transportPipes = ParcelFileDescriptor.createPipe();
                    int flags = this.mUserInitiated ? 1 : 0;
                    long quota = JobStatus.NO_LATEST_RUNTIME;
                    synchronized (this.mCancelLock) {
                        if (!this.mCancelAll) {
                            int backupPackageStatus = this.mTransport.performFullBackup(currentPackage, transportPipes[0], flags);
                            if (backupPackageStatus == 0) {
                                quota = this.mTransport.getBackupQuota(currentPackage.packageName, true);
                                enginePipes = ParcelFileDescriptor.createPipe();
                                this.mBackupRunner = new SinglePackageBackupRunner(enginePipes[1], currentPackage, this.mTransport, quota, this.mBackupRunnerOpToken);
                                enginePipes[1].close();
                                enginePipes[1] = null;
                                this.mIsDoingBackup = true;
                            }
                        }
                    }
                }
                if (this.mCancelAll) {
                    backupRunStatus = -2003;
                }
                if (BackupManagerService.DEBUG) {
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                }
                BackupManagerService.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                cleanUpPipes(transportPipes);
                cleanUpPipes(enginePipes);
                unregisterTask();
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (BackupManagerService.this.mQueueLock) {
                    BackupManagerService.this.mRunningFullBackupTask = null;
                }
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    BackupManagerService.this.scheduleNextFullBackupJob(backoff);
                }
                Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
                BackupManagerService.this.mWakelock.release();
                return;
            }
            int monitoringEvent;
            if (BackupManagerService.DEBUG) {
                Slog.i(TAG, "full backup requested but enabled=" + BackupManagerService.this.mEnabled + " provisioned=" + BackupManagerService.this.mProvisioned + "; ignoring");
            }
            if (BackupManagerService.this.mEnabled) {
                monitoringEvent = 14;
            } else {
                monitoringEvent = 13;
            }
            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, monitoringEvent, null, 3, null);
            this.mUpdateSchedule = false;
            backupRunStatus = -2001;
            if (this.mCancelAll) {
                backupRunStatus = -2003;
            }
            if (BackupManagerService.DEBUG) {
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
            }
            BackupManagerService.sendBackupFinished(this.mBackupObserver, backupRunStatus);
            cleanUpPipes(null);
            cleanUpPipes(null);
            unregisterTask();
            if (this.mJob != null) {
                this.mJob.finishBackupPass();
            }
            synchronized (BackupManagerService.this.mQueueLock) {
                BackupManagerService.this.mRunningFullBackupTask = null;
            }
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
                BackupManagerService.this.scheduleNextFullBackupJob(0);
            }
            Slog.i(BackupManagerService.TAG, "Full data backup pass finished.");
            BackupManagerService.this.mWakelock.release();
            return;
            i++;
        }

        void cleanUpPipes(ParcelFileDescriptor[] pipes) {
            if (pipes != null) {
                ParcelFileDescriptor fd;
                if (pipes[0] != null) {
                    fd = pipes[0];
                    pipes[0] = null;
                    try {
                        fd.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "Unable to close pipe!");
                    }
                }
                if (pipes[1] != null) {
                    fd = pipes[1];
                    pipes[1] = null;
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

        PerformInitializeTask(HashSet<String> transportNames) {
            this.mQueue = transportNames;
        }

        public void run() {
            try {
                for (String transportName : this.mQueue) {
                    IBackupTransport transport = BackupManagerService.this.mTransportManager.getTransportBinder(transportName);
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
                            EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE, new Object[0]);
                            BackupManagerService.this.resetBackupState(new File(BackupManagerService.this.mBaseStateDir, transport.transportDirName()));
                            EventLog.writeEvent(EventLogTags.BACKUP_SUCCESS, new Object[]{Integer.valueOf(0), Integer.valueOf(millis)});
                            synchronized (BackupManagerService.this.mQueueLock) {
                                BackupManagerService.this.recordInitPendingLocked(false, transportName);
                            }
                        } else {
                            Slog.e(BackupManagerService.TAG, "Transport error in initializeDevice()");
                            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                            synchronized (BackupManagerService.this.mQueueLock) {
                                BackupManagerService.this.recordInitPendingLocked(true, transportName);
                            }
                            long delay = transport.requestBackupTime();
                            Slog.w(BackupManagerService.TAG, "Init failed on " + transportName + " resched in " + delay);
                            BackupManagerService.this.mAlarmManager.set(0, System.currentTimeMillis() + delay, BackupManagerService.this.mRunInitIntent);
                        }
                    }
                }
                BackupManagerService.this.mWakelock.release();
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e);
                BackupManagerService.this.mWakelock.release();
            } catch (Throwable th) {
                BackupManagerService.this.mWakelock.release();
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
        private final int mEphemeralOpToken;
        private boolean mFinished;
        private boolean mIsSystemRestore;
        private IBackupManagerMonitor mMonitor;
        ParcelFileDescriptor mNewState;
        private File mNewStateName;
        private IRestoreObserver mObserver;
        private PackageManagerBackupAgent mPmAgent;
        private int mPmToken;
        private RestoreDescription mRestoreDescription;
        private File mSavedStateName;
        private File mStageName;
        private long mStartRealtime = SystemClock.elapsedRealtime();
        private UnifiedRestoreState mState = UnifiedRestoreState.INITIAL;
        File mStateDir;
        private int mStatus;
        private PackageInfo mTargetPackage;
        private long mToken;
        private IBackupTransport mTransport;
        private byte[] mWidgetData;

        class EngineThread implements Runnable {
            FullRestoreEngine mEngine;
            FileInputStream mEngineStream;

            EngineThread(FullRestoreEngine engine, ParcelFileDescriptor engineSocket) {
                this.mEngine = engine;
                engine.setRunning(true);
                this.mEngineStream = new FileInputStream(engineSocket.getFileDescriptor(), true);
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
                        this.mEngine.restoreOneFile(this.mEngineStream, false);
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
            final String TAG = "StreamFeederThread";
            FullRestoreEngine mEngine;
            ParcelFileDescriptor[] mEnginePipes;
            EngineThread mEngineThread;
            private final int mEphemeralOpToken;
            ParcelFileDescriptor[] mTransportPipes;

            public StreamFeederThread() throws IOException {
                super();
                this.mEphemeralOpToken = BackupManagerService.this.generateToken();
                this.mTransportPipes = ParcelFileDescriptor.createPipe();
                this.mEnginePipes = ParcelFileDescriptor.createPipe();
                setRunning(true);
            }

            public void run() {
                PerformUnifiedRestoreTask performUnifiedRestoreTask;
                boolean z;
                UnifiedRestoreState nextState = UnifiedRestoreState.RUNNING_QUEUE;
                int status = 0;
                EventLog.writeEvent(EventLogTags.FULL_RESTORE_PACKAGE, PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                this.mEngine = new FullRestoreEngine(this, null, PerformUnifiedRestoreTask.this.mMonitor, PerformUnifiedRestoreTask.this.mCurrentPackage, false, false, this.mEphemeralOpToken);
                this.mEngineThread = new EngineThread(this.mEngine, this.mEnginePipes[0]);
                ParcelFileDescriptor eWriteEnd = this.mEnginePipes[1];
                ParcelFileDescriptor tReadEnd = this.mTransportPipes[0];
                ParcelFileDescriptor tWriteEnd = this.mTransportPipes[1];
                int bufferSize = 32768;
                byte[] buffer = new byte[32768];
                FileOutputStream fileOutputStream = new FileOutputStream(eWriteEnd.getFileDescriptor());
                FileInputStream fileInputStream = new FileInputStream(tReadEnd.getFileDescriptor());
                new Thread(this.mEngineThread, "unified-restore-engine").start();
                while (status == 0) {
                    try {
                        int result = PerformUnifiedRestoreTask.this.mTransport.getNextFullRestoreDataChunk(tWriteEnd);
                        if (result > 0) {
                            if (result > bufferSize) {
                                bufferSize = result;
                                buffer = new byte[result];
                            }
                            int toCopy = result;
                            while (toCopy > 0) {
                                int n = fileInputStream.read(buffer, 0, toCopy);
                                fileOutputStream.write(buffer, 0, n);
                                toCopy -= n;
                            }
                        } else if (result == -1) {
                            status = 0;
                            break;
                        } else {
                            Slog.e("StreamFeederThread", "Error " + result + " streaming restore for " + PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                            EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                            status = result;
                        }
                    } catch (IOException e) {
                        Slog.e("StreamFeederThread", "Unable to route data for restore");
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{PerformUnifiedRestoreTask.this.mCurrentPackage.packageName, "I/O error on pipes"});
                        status = -1003;
                        IoUtils.closeQuietly(this.mEnginePipes[1]);
                        IoUtils.closeQuietly(this.mTransportPipes[0]);
                        IoUtils.closeQuietly(this.mTransportPipes[1]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[0]);
                        performUnifiedRestoreTask = PerformUnifiedRestoreTask.this;
                        if (this.mEngine.getAgent() != null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        performUnifiedRestoreTask.mDidLaunch = z;
                        try {
                            PerformUnifiedRestoreTask.this.mTransport.abortFullRestore();
                        } catch (Exception e2) {
                            Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e2.getMessage());
                            status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        }
                        BackupManagerService.this.clearApplicationDataSynchronous(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                        if (status == -1000) {
                            nextState = UnifiedRestoreState.FINAL;
                        } else {
                            nextState = UnifiedRestoreState.RUNNING_QUEUE;
                        }
                        PerformUnifiedRestoreTask.this.executeNextState(nextState);
                        setRunning(false);
                        return;
                    } catch (Exception e22) {
                        Slog.e("StreamFeederThread", "Transport failed during restore: " + e22.getMessage());
                        EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                        status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        IoUtils.closeQuietly(this.mEnginePipes[1]);
                        IoUtils.closeQuietly(this.mTransportPipes[0]);
                        IoUtils.closeQuietly(this.mTransportPipes[1]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[0]);
                        performUnifiedRestoreTask = PerformUnifiedRestoreTask.this;
                        if (this.mEngine.getAgent() != null) {
                            z = true;
                        } else {
                            z = false;
                        }
                        performUnifiedRestoreTask.mDidLaunch = z;
                        try {
                            PerformUnifiedRestoreTask.this.mTransport.abortFullRestore();
                        } catch (Exception e222) {
                            Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e222.getMessage());
                            status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        }
                        BackupManagerService.this.clearApplicationDataSynchronous(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                        if (status == -1000) {
                            nextState = UnifiedRestoreState.FINAL;
                        } else {
                            nextState = UnifiedRestoreState.RUNNING_QUEUE;
                        }
                        PerformUnifiedRestoreTask.this.executeNextState(nextState);
                        setRunning(false);
                        return;
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(this.mEnginePipes[1]);
                        IoUtils.closeQuietly(this.mTransportPipes[0]);
                        IoUtils.closeQuietly(this.mTransportPipes[1]);
                        this.mEngineThread.waitForResult();
                        IoUtils.closeQuietly(this.mEnginePipes[0]);
                        PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                        if (status == 0) {
                            nextState = UnifiedRestoreState.RESTORE_FINISHED;
                            PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                            PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                        } else {
                            try {
                                PerformUnifiedRestoreTask.this.mTransport.abortFullRestore();
                            } catch (Exception e2222) {
                                Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e2222.getMessage());
                                status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                            }
                            BackupManagerService.this.clearApplicationDataSynchronous(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                            if (status == -1000) {
                                nextState = UnifiedRestoreState.FINAL;
                            } else {
                                nextState = UnifiedRestoreState.RUNNING_QUEUE;
                            }
                        }
                        PerformUnifiedRestoreTask.this.executeNextState(nextState);
                        setRunning(false);
                    }
                }
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                IoUtils.closeQuietly(this.mTransportPipes[0]);
                IoUtils.closeQuietly(this.mTransportPipes[1]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                if (status == 0) {
                    nextState = UnifiedRestoreState.RESTORE_FINISHED;
                    PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                    PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                } else {
                    try {
                        PerformUnifiedRestoreTask.this.mTransport.abortFullRestore();
                    } catch (Exception e22222) {
                        Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e22222.getMessage());
                        status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    }
                    BackupManagerService.this.clearApplicationDataSynchronous(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                    if (status == -1000) {
                        nextState = UnifiedRestoreState.FINAL;
                    } else {
                        nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    }
                }
                PerformUnifiedRestoreTask.this.executeNextState(nextState);
                setRunning(false);
            }

            public void execute() {
            }

            public void operationComplete(long result) {
            }

            public void handleCancel(boolean cancelAll) {
                BackupManagerService.this.removeOperation(this.mEphemeralOpToken);
                if (BackupManagerService.DEBUG) {
                    Slog.w("StreamFeederThread", "Full-data restore target timed out; shutting down");
                }
                PerformUnifiedRestoreTask.this.mMonitor = BackupManagerService.monitorEvent(PerformUnifiedRestoreTask.this.mMonitor, 45, PerformUnifiedRestoreTask.this.mCurrentPackage, 2, null);
                this.mEngineThread.handleTimeout();
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                this.mEnginePipes[1] = null;
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                this.mEnginePipes[0] = null;
            }
        }

        private static /* synthetic */ int[] -getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues() {
            if (-com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues != null) {
                return -com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues;
            }
            int[] iArr = new int[UnifiedRestoreState.values().length];
            try {
                iArr[UnifiedRestoreState.FINAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[UnifiedRestoreState.INITIAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_FINISHED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_FULL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[UnifiedRestoreState.RESTORE_KEYVALUE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[UnifiedRestoreState.RUNNING_QUEUE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            -com-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues = iArr;
            return iArr;
        }

        PerformUnifiedRestoreTask(IBackupTransport transport, IRestoreObserver observer, IBackupManagerMonitor monitor, long restoreSetToken, PackageInfo targetPackage, int pmToken, boolean isFullSystemRestore, String[] filterSet) {
            this.mEphemeralOpToken = BackupManagerService.this.generateToken();
            this.mTransport = transport;
            this.mObserver = observer;
            this.mMonitor = monitor;
            this.mToken = restoreSetToken;
            this.mPmToken = pmToken;
            this.mTargetPackage = targetPackage;
            this.mIsSystemRestore = isFullSystemRestore;
            this.mFinished = false;
            this.mDidLaunch = false;
            if (targetPackage != null) {
                this.mAcceptSet = new ArrayList();
                this.mAcceptSet.add(targetPackage);
                return;
            }
            if (filterSet == null) {
                filterSet = packagesToNames(PackageManagerBackupAgent.getStorableApplications(BackupManagerService.this.mPackageManager));
                if (BackupManagerService.DEBUG) {
                    Slog.i(BackupManagerService.TAG, "Full restore; asking about " + filterSet.length + " apps");
                }
            }
            this.mAcceptSet = new ArrayList(filterSet.length);
            boolean hasSystem = false;
            boolean hasSettings = false;
            for (String packageInfo : filterSet) {
                try {
                    PackageInfo info = BackupManagerService.this.mPackageManager.getPackageInfo(packageInfo, 0);
                    if ("android".equals(info.packageName)) {
                        hasSystem = true;
                    } else if (BackupManagerService.SETTINGS_PACKAGE.equals(info.packageName)) {
                        hasSettings = true;
                    } else if (BackupManagerService.appIsEligibleForBackup(info.applicationInfo, BackupManagerService.this.mPackageManager)) {
                        this.mAcceptSet.add(info);
                    }
                } catch (NameNotFoundException e) {
                }
            }
            if (hasSystem) {
                try {
                    this.mAcceptSet.add(0, BackupManagerService.this.mPackageManager.getPackageInfo("android", 0));
                } catch (NameNotFoundException e2) {
                }
            }
            if (hasSettings) {
                try {
                    this.mAcceptSet.add(BackupManagerService.this.mPackageManager.getPackageInfo(BackupManagerService.SETTINGS_PACKAGE, 0));
                } catch (NameNotFoundException e3) {
                }
            }
        }

        private String[] packagesToNames(List<PackageInfo> apps) {
            int N = apps.size();
            String[] names = new String[N];
            for (int i = 0; i < N; i++) {
                names[i] = ((PackageInfo) apps.get(i)).packageName;
            }
            return names;
        }

        public void execute() {
            switch (-getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues()[this.mState.ordinal()]) {
                case 1:
                    if (this.mFinished) {
                        Slog.e(BackupManagerService.TAG, "Duplicate finish");
                    } else {
                        finalizeRestore();
                    }
                    this.mFinished = true;
                    return;
                case 2:
                    startRestore();
                    return;
                case 3:
                    restoreFinished();
                    return;
                case 4:
                    restoreFull();
                    return;
                case 5:
                    restoreKeyValue();
                    return;
                case 6:
                    dispatchNextRestore();
                    return;
                default:
                    return;
            }
        }

        private void startRestore() {
            sendStartRestore(this.mAcceptSet.size());
            if (this.mIsSystemRestore) {
                AppWidgetBackupBridge.restoreStarting(0);
            }
            try {
                this.mStateDir = new File(BackupManagerService.this.mBaseStateDir, this.mTransport.transportDirName());
                PackageInfo pmPackage = new PackageInfo();
                pmPackage.packageName = BackupManagerService.PACKAGE_MANAGER_SENTINEL;
                this.mAcceptSet.add(0, pmPackage);
                this.mStatus = this.mTransport.startRestore(this.mToken, (PackageInfo[]) this.mAcceptSet.toArray(new PackageInfo[0]));
                if (this.mStatus != 0) {
                    Slog.e(BackupManagerService.TAG, "Transport error " + this.mStatus + "; no restore possible");
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                    return;
                }
                RestoreDescription desc = this.mTransport.nextRestorePackage();
                if (desc == null) {
                    Slog.e(BackupManagerService.TAG, "No restore metadata available; halting");
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 22, this.mCurrentPackage, 3, null);
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                } else if (BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(desc.getPackageName())) {
                    this.mCurrentPackage = new PackageInfo();
                    this.mCurrentPackage.packageName = BackupManagerService.PACKAGE_MANAGER_SENTINEL;
                    this.mPmAgent = new PackageManagerBackupAgent(BackupManagerService.this.mPackageManager, null);
                    this.mAgent = IBackupAgent.Stub.asInterface(this.mPmAgent.onBind());
                    initiateOneRestore(this.mCurrentPackage, 0);
                    BackupManagerService.this.mBackupHandler.removeMessages(18);
                    if (!this.mPmAgent.hasMetadata()) {
                        Slog.e(BackupManagerService.TAG, "PM agent has no metadata, so not restoring");
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 24, this.mCurrentPackage, 3, null);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{BackupManagerService.PACKAGE_MANAGER_SENTINEL, "Package manager restore metadata missing"});
                        this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        BackupManagerService.this.mBackupHandler.removeMessages(20, this);
                        executeNextState(UnifiedRestoreState.FINAL);
                    }
                } else {
                    Slog.e(BackupManagerService.TAG, "Required package metadata but got " + desc.getPackageName());
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 23, this.mCurrentPackage, 3, null);
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    executeNextState(UnifiedRestoreState.FINAL);
                }
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Unable to contact transport for restore: " + e.getMessage());
                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 25, null, 1, null);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                BackupManagerService.this.mBackupHandler.removeMessages(20, this);
                executeNextState(UnifiedRestoreState.FINAL);
            }
        }

        private void dispatchNextRestore() {
            UnifiedRestoreState nextState = UnifiedRestoreState.FINAL;
            try {
                this.mRestoreDescription = this.mTransport.nextRestorePackage();
                String pkgName = this.mRestoreDescription != null ? this.mRestoreDescription.getPackageName() : null;
                if (pkgName == null) {
                    Slog.e(BackupManagerService.TAG, "Failure getting next package name");
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                    nextState = UnifiedRestoreState.FINAL;
                } else if (this.mRestoreDescription == RestoreDescription.NO_MORE_PACKAGES) {
                    if (BackupManagerService.DEBUG) {
                        Slog.v(BackupManagerService.TAG, "No more packages; finishing restore");
                    }
                    int millis = (int) (SystemClock.elapsedRealtime() - this.mStartRealtime);
                    EventLog.writeEvent(EventLogTags.RESTORE_SUCCESS, new Object[]{Integer.valueOf(this.mCount), Integer.valueOf(millis)});
                    executeNextState(UnifiedRestoreState.FINAL);
                } else {
                    if (BackupManagerService.DEBUG) {
                        Slog.i(BackupManagerService.TAG, "Next restore package: " + this.mRestoreDescription);
                    }
                    sendOnRestorePackage(pkgName);
                    Metadata metaInfo = this.mPmAgent.getRestoredMetadata(pkgName);
                    if (metaInfo == null) {
                        Slog.e(BackupManagerService.TAG, "No metadata for " + pkgName);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{pkgName, "Package metadata missing"});
                        executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                        return;
                    }
                    try {
                        this.mCurrentPackage = BackupManagerService.this.mPackageManager.getPackageInfo(pkgName, 64);
                        if (metaInfo.versionCode > this.mCurrentPackage.versionCode) {
                            if ((this.mCurrentPackage.applicationInfo.flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0) {
                                Slog.w(BackupManagerService.TAG, "Package " + pkgName + ": " + ("Source version " + metaInfo.versionCode + " > installed version " + this.mCurrentPackage.versionCode));
                                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 27, this.mCurrentPackage, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_RESTORE_VERSION", metaInfo.versionCode), "android.app.backup.extra.LOG_RESTORE_ANYWAY", false));
                                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{pkgName, message});
                                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                                return;
                            }
                            if (BackupManagerService.DEBUG) {
                                Slog.v(BackupManagerService.TAG, "Source version " + metaInfo.versionCode + " > installed version " + this.mCurrentPackage.versionCode + " but restoreAnyVersion");
                            }
                            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 27, this.mCurrentPackage, 3, BackupManagerService.this.putMonitoringExtra(BackupManagerService.this.putMonitoringExtra(null, "android.app.backup.extra.LOG_RESTORE_VERSION", metaInfo.versionCode), "android.app.backup.extra.LOG_RESTORE_ANYWAY", true));
                        }
                        this.mWidgetData = null;
                        int type = this.mRestoreDescription.getDataType();
                        if (type == 1) {
                            nextState = UnifiedRestoreState.RESTORE_KEYVALUE;
                        } else if (type == 2) {
                            nextState = UnifiedRestoreState.RESTORE_FULL;
                        } else {
                            Slog.e(BackupManagerService.TAG, "Unrecognized restore type " + type);
                            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                            return;
                        }
                        executeNextState(nextState);
                    } catch (NameNotFoundException e) {
                        Slog.e(BackupManagerService.TAG, "Package not present: " + pkgName);
                        this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 26, this.mCurrentPackage, 3, null);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{pkgName, "Package missing on device"});
                        executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                    }
                }
            } catch (Exception e2) {
                Slog.e(BackupManagerService.TAG, "Can't get next restore target from transport; halting: " + e2.getMessage());
                EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                nextState = UnifiedRestoreState.FINAL;
            } finally {
                executeNextState(nextState);
            }
        }

        private void restoreKeyValue() {
            String packageName = this.mCurrentPackage.packageName;
            if (this.mCurrentPackage.applicationInfo.backupAgentName == null || "".equals(this.mCurrentPackage.applicationInfo.backupAgentName)) {
                this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 28, this.mCurrentPackage, 2, null);
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{packageName, "Package has no agent"});
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                return;
            }
            Metadata metaInfo = this.mPmAgent.getRestoredMetadata(packageName);
            if (BackupUtils.signaturesMatch(metaInfo.sigHashes, this.mCurrentPackage)) {
                this.mAgent = BackupManagerService.this.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, 0);
                if (this.mAgent == null) {
                    Slog.w(BackupManagerService.TAG, "Can't find backup agent for " + packageName);
                    this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 30, this.mCurrentPackage, 3, null);
                    EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{packageName, "Restore agent missing"});
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                    return;
                }
                this.mDidLaunch = true;
                try {
                    initiateOneRestore(this.mCurrentPackage, metaInfo.versionCode);
                    this.mCount++;
                } catch (Exception e) {
                    Slog.e(BackupManagerService.TAG, "Error when attempting restore: " + e.toString());
                    keyValueAgentErrorCleanup();
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                }
                return;
            }
            Slog.w(BackupManagerService.TAG, "Signature mismatch restoring " + packageName);
            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 29, this.mCurrentPackage, 3, null);
            EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{packageName, "Signature mismatch"});
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }

        void initiateOneRestore(PackageInfo app, int appVersionCode) {
            String packageName = app.packageName;
            if (BackupManagerService.DEBUG) {
                Slog.d(BackupManagerService.TAG, "initiateOneRestore packageName=" + packageName);
            }
            this.mBackupDataName = new File(BackupManagerService.this.mDataDir, packageName + ".restore");
            this.mStageName = new File(BackupManagerService.this.mDataDir, packageName + ".stage");
            this.mNewStateName = new File(this.mStateDir, packageName + ".new");
            this.mSavedStateName = new File(this.mStateDir, packageName);
            boolean staging = packageName.equals("android") ^ 1;
            File downloadFile = staging ? this.mStageName : this.mBackupDataName;
            try {
                ParcelFileDescriptor stage = ParcelFileDescriptor.open(downloadFile, 1006632960);
                if (this.mTransport.getRestoreData(stage) != 0) {
                    Slog.e(BackupManagerService.TAG, "Error getting restore data for " + packageName);
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
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
                    byte[] buffer = new byte[8192];
                    while (in.readNextHeader()) {
                        String key = in.getKey();
                        int size = in.getDataSize();
                        if (key.equals(BackupManagerService.KEY_WIDGET_STATE)) {
                            if (BackupManagerService.DEBUG) {
                                Slog.i(BackupManagerService.TAG, "Restoring widget state for " + packageName);
                            }
                            this.mWidgetData = new byte[size];
                            in.readEntityData(this.mWidgetData, 0, size);
                        } else {
                            if (size > buffer.length) {
                                buffer = new byte[size];
                            }
                            in.readEntityData(buffer, 0, size);
                            out.writeEntityHeader(key, size);
                            out.writeEntityData(buffer, size);
                        }
                    }
                    this.mBackupData.close();
                }
                stage.close();
                this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
                BackupManagerService.this.prepareOperationTimeout(this.mEphemeralOpToken, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS, this, 1);
                this.mAgent.doRestore(this.mBackupData, appVersionCode, this.mNewState, this.mEphemeralOpToken, BackupManagerService.this.mBackupManagerBinder);
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Unable to call app for restore: " + packageName, e);
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{packageName, e.toString()});
                keyValueAgentErrorCleanup();
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            }
        }

        private void restoreFull() {
            try {
                new Thread(new StreamFeederThread(), "unified-stream-feeder").start();
            } catch (IOException e) {
                Slog.e(BackupManagerService.TAG, "Unable to construct pipes for stream restore!");
                executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            }
        }

        private void restoreFinished() {
            if (BackupManagerService.DEBUG) {
                Slog.d(BackupManagerService.TAG, "restoreFinished packageName=" + this.mCurrentPackage.packageName);
            }
            try {
                BackupManagerService.this.prepareOperationTimeout(this.mEphemeralOpToken, 30000, this, 1);
                this.mAgent.doRestoreFinished(this.mEphemeralOpToken, BackupManagerService.this.mBackupManagerBinder);
            } catch (Exception e) {
                Slog.e(BackupManagerService.TAG, "Unable to finalize restore of " + this.mCurrentPackage.packageName);
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{packageName, e.toString()});
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
            BackupManagerService.this.mBackupHandler.removeMessages(8);
            if (this.mPmToken > 0) {
                try {
                    BackupManagerService.this.mPackageManagerBinder.finishPackageInstall(this.mPmToken, this.mDidLaunch);
                } catch (RemoteException e3) {
                }
            } else {
                BackupManagerService.this.mBackupHandler.sendEmptyMessageDelayed(8, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
            }
            AppWidgetBackupBridge.restoreFinished(0);
            if (this.mIsSystemRestore && this.mPmAgent != null) {
                BackupManagerService.this.mAncestralPackages = this.mPmAgent.getRestoredPackages();
                BackupManagerService.this.mAncestralToken = this.mToken;
                BackupManagerService.this.writeRestoreTokens();
            }
            Slog.i(BackupManagerService.TAG, "Restore complete.");
            synchronized (BackupManagerService.this.mPendingRestores) {
                if (BackupManagerService.this.mPendingRestores.size() > 0) {
                    if (BackupManagerService.DEBUG) {
                        Slog.d(BackupManagerService.TAG, "Starting next pending restore.");
                    }
                    BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(20, (PerformUnifiedRestoreTask) BackupManagerService.this.mPendingRestores.remove()));
                } else {
                    BackupManagerService.this.mIsRestoreInProgress = false;
                }
            }
            BackupManagerService.this.mWakelock.release();
        }

        void keyValueAgentErrorCleanup() {
            BackupManagerService.this.clearApplicationDataSynchronous(this.mCurrentPackage.packageName);
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
                    BackupManagerService.this.mActivityManager.unbindBackupAgent(this.mCurrentPackage.applicationInfo);
                    boolean killAfterRestore = this.mCurrentPackage.applicationInfo.uid >= 10000 ? this.mRestoreDescription.getDataType() != 2 ? (65536 & this.mCurrentPackage.applicationInfo.flags) != 0 : true : false;
                    if (this.mTargetPackage == null && killAfterRestore) {
                        if (BackupManagerService.DEBUG) {
                            Slog.d(BackupManagerService.TAG, "Restore complete, killing host process of " + this.mCurrentPackage.applicationInfo.processName);
                        }
                        BackupManagerService.this.mActivityManager.killApplicationProcess(this.mCurrentPackage.applicationInfo.processName, this.mCurrentPackage.applicationInfo.uid);
                    }
                } catch (RemoteException e3) {
                }
            }
            BackupManagerService.this.mBackupHandler.removeMessages(18, this);
        }

        public void operationComplete(long unusedResult) {
            UnifiedRestoreState nextState;
            BackupManagerService.this.removeOperation(this.mEphemeralOpToken);
            switch (-getcom-android-server-backup-BackupManagerService$UnifiedRestoreStateSwitchesValues()[this.mState.ordinal()]) {
                case 2:
                    nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    break;
                case 3:
                    int size = (int) this.mBackupDataName.length();
                    EventLog.writeEvent(EventLogTags.RESTORE_PACKAGE, new Object[]{this.mCurrentPackage.packageName, Integer.valueOf(size)});
                    keyValueAgentCleanup();
                    if (this.mWidgetData != null) {
                        BackupManagerService.this.restoreWidgetData(this.mCurrentPackage.packageName, this.mWidgetData);
                    }
                    nextState = UnifiedRestoreState.RUNNING_QUEUE;
                    break;
                case 4:
                case 5:
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

        public void handleCancel(boolean cancelAll) {
            BackupManagerService.this.removeOperation(this.mEphemeralOpToken);
            Slog.e(BackupManagerService.TAG, "Timeout restoring application " + this.mCurrentPackage.packageName);
            this.mMonitor = BackupManagerService.monitorEvent(this.mMonitor, 31, this.mCurrentPackage, 2, null);
            EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, new Object[]{this.mCurrentPackage.packageName, "restore timeout"});
            keyValueAgentErrorCleanup();
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }

        void executeNextState(UnifiedRestoreState nextState) {
            this.mState = nextState;
            BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(20, this));
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
        public ProvisionedObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            boolean wasProvisioned = BackupManagerService.this.mProvisioned;
            boolean isProvisioned = BackupManagerService.this.deviceIsProvisioned();
            BackupManagerService backupManagerService = BackupManagerService.this;
            if (wasProvisioned) {
                isProvisioned = true;
            }
            backupManagerService.mProvisioned = isProvisioned;
            synchronized (BackupManagerService.this.mQueueLock) {
                if (BackupManagerService.this.mProvisioned && (wasProvisioned ^ 1) != 0 && BackupManagerService.this.mEnabled) {
                    KeyValueBackupJob.schedule(BackupManagerService.this.mContext);
                    BackupManagerService.this.scheduleNextFullBackupJob(0);
                }
            }
        }
    }

    class RestoreGetSetsParams {
        public IBackupManagerMonitor monitor;
        public IRestoreObserver observer;
        public ActiveRestoreSession session;
        public IBackupTransport transport;

        RestoreGetSetsParams(IBackupTransport _transport, ActiveRestoreSession _session, IRestoreObserver _observer, IBackupManagerMonitor _monitor) {
            this.transport = _transport;
            this.session = _session;
            this.observer = _observer;
            this.monitor = _monitor;
        }
    }

    class RestoreParams {
        public String dirName;
        public String[] filterSet;
        public boolean isSystemRestore;
        public IBackupManagerMonitor monitor;
        public IRestoreObserver observer;
        public PackageInfo pkgInfo;
        public int pmToken;
        public long token;
        public IBackupTransport transport;

        RestoreParams(IBackupTransport _transport, String _dirName, IRestoreObserver _obs, IBackupManagerMonitor _monitor, long _token, PackageInfo _pkg) {
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.monitor = _monitor;
            this.token = _token;
            this.pkgInfo = _pkg;
            this.pmToken = 0;
            this.isSystemRestore = false;
            this.filterSet = null;
        }

        RestoreParams(IBackupTransport _transport, String _dirName, IRestoreObserver _obs, IBackupManagerMonitor _monitor, long _token, String _pkgName, int _pmToken) {
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.monitor = _monitor;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = _pmToken;
            this.isSystemRestore = false;
            this.filterSet = new String[]{_pkgName};
        }

        RestoreParams(IBackupTransport _transport, String _dirName, IRestoreObserver _obs, IBackupManagerMonitor _monitor, long _token) {
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.monitor = _monitor;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = 0;
            this.isSystemRestore = true;
            this.filterSet = null;
        }

        RestoreParams(IBackupTransport _transport, String _dirName, IRestoreObserver _obs, IBackupManagerMonitor _monitor, long _token, String[] _filterSet, boolean _isSystemRestore) {
            this.transport = _transport;
            this.dirName = _dirName;
            this.observer = _obs;
            this.monitor = _monitor;
            this.token = _token;
            this.pkgInfo = null;
            this.pmToken = 0;
            this.isSystemRestore = _isSystemRestore;
            this.filterSet = _filterSet;
        }
    }

    enum RestorePolicy {
        IGNORE,
        ACCEPT,
        ACCEPT_IF_APK
    }

    private class RunBackupReceiver extends BroadcastReceiver {
        /* synthetic */ RunBackupReceiver(BackupManagerService this$0, RunBackupReceiver -this1) {
            this();
        }

        private RunBackupReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (BackupManagerService.RUN_BACKUP_ACTION.equals(intent.getAction())) {
                synchronized (BackupManagerService.this.mQueueLock) {
                    if (BackupManagerService.this.mPendingInits.size() > 0) {
                        try {
                            BackupManagerService.this.mAlarmManager.cancel(BackupManagerService.this.mRunInitIntent);
                            BackupManagerService.this.mRunInitIntent.send();
                        } catch (CanceledException e) {
                            Slog.e(BackupManagerService.TAG, "Run init intent cancelled");
                        }
                    } else if (!BackupManagerService.this.mEnabled || !BackupManagerService.this.mProvisioned) {
                        Slog.w(BackupManagerService.TAG, "Backup pass but e=" + BackupManagerService.this.mEnabled + " p=" + BackupManagerService.this.mProvisioned);
                    } else if (BackupManagerService.this.mBackupRunning) {
                        Slog.i(BackupManagerService.TAG, "Backup time but one already running");
                    } else {
                        if (BackupManagerService.DEBUG) {
                            Slog.v(BackupManagerService.TAG, "Running a backup pass");
                        }
                        BackupManagerService.this.mBackupRunning = true;
                        BackupManagerService.this.mWakelock.acquire();
                        BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(1));
                    }
                }
                return;
            }
            return;
        }
    }

    private class RunInitializeReceiver extends BroadcastReceiver {
        /* synthetic */ RunInitializeReceiver(BackupManagerService this$0, RunInitializeReceiver -this1) {
            this();
        }

        private RunInitializeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (BackupManagerService.RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
                synchronized (BackupManagerService.this.mQueueLock) {
                    if (BackupManagerService.DEBUG) {
                        Slog.v(BackupManagerService.TAG, "Running a device init");
                    }
                    BackupManagerService.this.mWakelock.acquire();
                    BackupManagerService.this.mBackupHandler.sendMessage(BackupManagerService.this.mBackupHandler.obtainMessage(5));
                }
            }
        }
    }

    enum UnifiedRestoreState {
        INITIAL,
        RUNNING_QUEUE,
        RESTORE_KEYVALUE,
        RESTORE_FULL,
        RESTORE_FINISHED,
        FINAL
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
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

    public static boolean appIsEligibleForBackup(ApplicationInfo app, PackageManager pm) {
        if ((app.flags & 32768) == 0) {
            return false;
        }
        if ((app.uid < 10000 && app.backupAgentName == null) || app.packageName.equals(SHARED_BACKUP_AGENT_PACKAGE) || app.isInstantApp()) {
            return false;
        }
        return appIsDisabled(app, pm) ^ 1;
    }

    private static boolean appIsStopped(ApplicationInfo app) {
        return (app.flags & DumpState.DUMP_COMPILER_STATS) != 0;
    }

    private static boolean appIsDisabled(ApplicationInfo app, PackageManager pm) {
        switch (pm.getApplicationEnabledSetting(app.packageName)) {
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private static boolean appGetsFullBackup(PackageInfo pkg) {
        boolean z = true;
        if (pkg.applicationInfo.backupAgentName == null) {
            return true;
        }
        if ((pkg.applicationInfo.flags & 67108864) == 0) {
            z = false;
        }
        return z;
    }

    private static boolean appIsKeyValueOnly(PackageInfo pkg) {
        return appGetsFullBackup(pkg) ^ 1;
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

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x03a9 A:{SYNTHETIC, Splitter: B:59:0x03a9} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x03ae A:{Catch:{ IOException -> 0x03b3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0407 A:{SYNTHETIC, Splitter: B:88:0x0407} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x040c A:{Catch:{ IOException -> 0x0410 }} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x03ee A:{SYNTHETIC, Splitter: B:80:0x03ee} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x03f3 A:{Catch:{ IOException -> 0x03f8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0407 A:{SYNTHETIC, Splitter: B:88:0x0407} */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x040c A:{Catch:{ IOException -> 0x0410 }} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x03a9 A:{SYNTHETIC, Splitter: B:59:0x03a9} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x03ae A:{Catch:{ IOException -> 0x03b3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x03c2 A:{SYNTHETIC, Splitter: B:67:0x03c2} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x03c7 A:{Catch:{ IOException -> 0x03cb }} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x03ee A:{SYNTHETIC, Splitter: B:80:0x03ee} */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x03f3 A:{Catch:{ IOException -> 0x03f8 }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x030c  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x032c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x03c2 A:{SYNTHETIC, Splitter: B:67:0x03c2} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x03c7 A:{Catch:{ IOException -> 0x03cb }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BackupManagerService(Context context, Trampoline parent) {
        FileInputStream fileInputStream;
        DataInputStream in;
        FileInputStream fin;
        DataInputStream in2;
        IntentFilter filter;
        Intent backupIntent;
        Intent initIntent;
        Set<ComponentName> transportWhitelist;
        String transport;
        String currentTransport;
        Throwable th;
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mPackageManagerBinder = AppGlobals.getPackageManager();
        this.mActivityManager = ActivityManager.getService();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        this.mBackupManagerBinder = Trampoline.asInterface(parent.asBinder());
        this.mHandlerThread = new HandlerThread("backup", 10);
        this.mHandlerThread.start();
        this.mBackupHandler = new BackupHandler(this.mHandlerThread.getLooper());
        ContentResolver resolver = context.getContentResolver();
        this.mProvisioned = Global.getInt(resolver, "device_provisioned", 0) != 0;
        this.mAutoRestore = Secure.getInt(resolver, "backup_auto_restore", 1) != 0;
        this.mProvisionedObserver = new ProvisionedObserver(this.mBackupHandler);
        resolver.registerContentObserver(Global.getUriFor("device_provisioned"), false, this.mProvisionedObserver);
        this.mBaseStateDir = new File(Environment.getDataDirectory(), "backup");
        this.mBaseStateDir.mkdirs();
        if (!SELinux.restorecon(this.mBaseStateDir)) {
            Slog.e(TAG, "SELinux restorecon failed on " + this.mBaseStateDir);
        }
        this.mDataDir = new File(Environment.getDownloadCacheDirectory(), "backup_stage");
        this.mPasswordVersion = 1;
        this.mPasswordVersionFile = new File(this.mBaseStateDir, "pwversion");
        if (this.mPasswordVersionFile.exists()) {
            fileInputStream = null;
            in = null;
            try {
                fin = new FileInputStream(this.mPasswordVersionFile);
                try {
                    in2 = new DataInputStream(fin);
                    try {
                        this.mPasswordVersion = in2.readInt();
                        if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e) {
                                Slog.w(TAG, "Error closing pw version files");
                            }
                        }
                        if (fin != null) {
                            fin.close();
                        }
                    } catch (IOException e2) {
                        in = in2;
                        fileInputStream = fin;
                        try {
                            Slog.e(TAG, "Unable to read backup pw version");
                            if (in != null) {
                            }
                            if (fileInputStream != null) {
                            }
                            this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                            if (this.mPasswordHashFile.exists()) {
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
                            this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                            initIntent = new Intent(RUN_INITIALIZE_ACTION);
                            backupIntent.addFlags(1073741824);
                            this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                            this.mJournalDir = new File(this.mBaseStateDir, "pending");
                            this.mJournalDir.mkdirs();
                            this.mJournal = null;
                            this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                            initPackageTracking();
                            synchronized (this.mBackupParticipants) {
                            }
                            transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                            transport = Secure.getString(context.getContentResolver(), "backup_transport");
                            if (TextUtils.isEmpty(transport)) {
                            }
                            currentTransport = transport;
                            if (DEBUG) {
                            }
                            this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                            this.mTransportManager.registerAllTransports();
                            this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                            this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
                        } catch (Throwable th2) {
                            th = th2;
                            if (in != null) {
                            }
                            if (fileInputStream != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        in = in2;
                        fileInputStream = fin;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e3) {
                                Slog.w(TAG, "Error closing pw version files");
                                throw th;
                            }
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    fileInputStream = fin;
                    Slog.e(TAG, "Unable to read backup pw version");
                    if (in != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                    if (this.mPasswordHashFile.exists()) {
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
                    this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                    initIntent = new Intent(RUN_INITIALIZE_ACTION);
                    backupIntent.addFlags(1073741824);
                    this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                    this.mJournalDir = new File(this.mBaseStateDir, "pending");
                    this.mJournalDir.mkdirs();
                    this.mJournal = null;
                    this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                    initPackageTracking();
                    synchronized (this.mBackupParticipants) {
                    }
                    transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                    transport = Secure.getString(context.getContentResolver(), "backup_transport");
                    if (TextUtils.isEmpty(transport)) {
                    }
                    currentTransport = transport;
                    if (DEBUG) {
                    }
                    this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                    this.mTransportManager.registerAllTransports();
                    this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                    this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
                } catch (Throwable th4) {
                    th = th4;
                    fileInputStream = fin;
                    if (in != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e5) {
                Slog.e(TAG, "Unable to read backup pw version");
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e6) {
                        Slog.w(TAG, "Error closing pw version files");
                    }
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
                if (this.mPasswordHashFile.exists()) {
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
                this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                initIntent = new Intent(RUN_INITIALIZE_ACTION);
                backupIntent.addFlags(1073741824);
                this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                this.mJournalDir = new File(this.mBaseStateDir, "pending");
                this.mJournalDir.mkdirs();
                this.mJournal = null;
                this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                initPackageTracking();
                synchronized (this.mBackupParticipants) {
                }
                transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                transport = Secure.getString(context.getContentResolver(), "backup_transport");
                if (TextUtils.isEmpty(transport)) {
                }
                currentTransport = transport;
                if (DEBUG) {
                }
                this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                this.mTransportManager.registerAllTransports();
                this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
            }
        }
        this.mPasswordHashFile = new File(this.mBaseStateDir, "pwhash");
        if (this.mPasswordHashFile.exists()) {
            fileInputStream = null;
            in = null;
            try {
                fin = new FileInputStream(this.mPasswordHashFile);
                try {
                    in2 = new DataInputStream(new BufferedInputStream(fin));
                    try {
                        byte[] salt = new byte[in2.readInt()];
                        in2.readFully(salt);
                        this.mPasswordHash = in2.readUTF();
                        this.mPasswordSalt = salt;
                        if (in2 != null) {
                            try {
                                in2.close();
                            } catch (IOException e7) {
                                Slog.w(TAG, "Unable to close streams");
                            }
                        }
                        if (fin != null) {
                            fin.close();
                        }
                    } catch (IOException e8) {
                        in = in2;
                        fileInputStream = fin;
                        try {
                            Slog.e(TAG, "Unable to read saved backup pw hash");
                            if (in != null) {
                            }
                            if (fileInputStream != null) {
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
                            this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                            initIntent = new Intent(RUN_INITIALIZE_ACTION);
                            backupIntent.addFlags(1073741824);
                            this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                            this.mJournalDir = new File(this.mBaseStateDir, "pending");
                            this.mJournalDir.mkdirs();
                            this.mJournal = null;
                            this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                            initPackageTracking();
                            synchronized (this.mBackupParticipants) {
                            }
                            transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                            transport = Secure.getString(context.getContentResolver(), "backup_transport");
                            if (TextUtils.isEmpty(transport)) {
                            }
                            currentTransport = transport;
                            if (DEBUG) {
                            }
                            this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                            this.mTransportManager.registerAllTransports();
                            this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                            this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
                        } catch (Throwable th5) {
                            th = th5;
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e9) {
                                    Slog.w(TAG, "Unable to close streams");
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
                        in = in2;
                        fileInputStream = fin;
                        if (in != null) {
                        }
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (IOException e10) {
                    fileInputStream = fin;
                    Slog.e(TAG, "Unable to read saved backup pw hash");
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e11) {
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
                    this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                    initIntent = new Intent(RUN_INITIALIZE_ACTION);
                    backupIntent.addFlags(1073741824);
                    this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                    this.mJournalDir = new File(this.mBaseStateDir, "pending");
                    this.mJournalDir.mkdirs();
                    this.mJournal = null;
                    this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                    initPackageTracking();
                    synchronized (this.mBackupParticipants) {
                    }
                    transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                    transport = Secure.getString(context.getContentResolver(), "backup_transport");
                    if (TextUtils.isEmpty(transport)) {
                    }
                    currentTransport = transport;
                    if (DEBUG) {
                    }
                    this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                    this.mTransportManager.registerAllTransports();
                    this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                    this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
                } catch (Throwable th7) {
                    th = th7;
                    fileInputStream = fin;
                    if (in != null) {
                    }
                    if (fileInputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e12) {
                Slog.e(TAG, "Unable to read saved backup pw hash");
                if (in != null) {
                }
                if (fileInputStream != null) {
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
                this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
                initIntent = new Intent(RUN_INITIALIZE_ACTION);
                backupIntent.addFlags(1073741824);
                this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
                this.mJournalDir = new File(this.mBaseStateDir, "pending");
                this.mJournalDir.mkdirs();
                this.mJournal = null;
                this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
                initPackageTracking();
                synchronized (this.mBackupParticipants) {
                }
                transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
                transport = Secure.getString(context.getContentResolver(), "backup_transport");
                if (TextUtils.isEmpty(transport)) {
                }
                currentTransport = transport;
                if (DEBUG) {
                }
                this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
                this.mTransportManager.registerAllTransports();
                this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
                this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
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
        this.mRunBackupIntent = PendingIntent.getBroadcast(context, 1, backupIntent, 0);
        initIntent = new Intent(RUN_INITIALIZE_ACTION);
        backupIntent.addFlags(1073741824);
        this.mRunInitIntent = PendingIntent.getBroadcast(context, 5, initIntent, 0);
        this.mJournalDir = new File(this.mBaseStateDir, "pending");
        this.mJournalDir.mkdirs();
        this.mJournal = null;
        this.mFullBackupScheduleFile = new File(this.mBaseStateDir, "fb-schedule");
        initPackageTracking();
        synchronized (this.mBackupParticipants) {
            addPackageParticipantsLocked(null);
        }
        transportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
        transport = Secure.getString(context.getContentResolver(), "backup_transport");
        if (TextUtils.isEmpty(transport)) {
            transport = null;
        }
        currentTransport = transport;
        if (DEBUG) {
            Slog.v(TAG, "Starting with transport " + currentTransport);
        }
        this.mTransportManager = new TransportManager(context, transportWhitelist, currentTransport, this.mTransportBoundListener, this.mHandlerThread.getLooper());
        this.mTransportManager.registerAllTransports();
        this.mBackupHandler.post(new -$Lambda$Xanl8ugndUwNPev1fXoBi5rdcFA(this));
        this.mWakelock = this.mPowerManager.newWakeLock(1, "*backup*");
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x0135 A:{SYNTHETIC, Splitter: B:46:0x0135} */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x015e A:{SYNTHETIC, Splitter: B:60:0x015e} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x013a A:{SYNTHETIC, Splitter: B:49:0x013a} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01ff A:{Catch:{ all -> 0x023f }} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0230 A:{SYNTHETIC, Splitter: B:86:0x0230} */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0235 A:{SYNTHETIC, Splitter: B:89:0x0235} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x016f A:{SYNTHETIC, Splitter: B:66:0x016f} */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0174 A:{SYNTHETIC, Splitter: B:69:0x0174} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0242 A:{SYNTHETIC, Splitter: B:95:0x0242} */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0247 A:{SYNTHETIC, Splitter: B:98:0x0247} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x017e A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initPackageTracking() {
        IOException e;
        File file;
        IntentFilter filter;
        IntentFilter sdFilter;
        Throwable th;
        this.mTokenFile = new File(this.mBaseStateDir, "ancestral");
        Throwable th2 = null;
        DataInputStream tokenStream = null;
        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(this.mTokenFile)));
            try {
                if (dataInputStream.readInt() == 1) {
                    this.mAncestralToken = dataInputStream.readLong();
                    this.mCurrentToken = dataInputStream.readLong();
                    int numPackages = dataInputStream.readInt();
                    if (numPackages >= 0) {
                        this.mAncestralPackages = new HashSet();
                        for (int i = 0; i < numPackages; i++) {
                            this.mAncestralPackages.add(dataInputStream.readUTF());
                        }
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (FileNotFoundException e2) {
                        tokenStream = dataInputStream;
                    } catch (IOException e3) {
                        e = e3;
                        tokenStream = dataInputStream;
                        Slog.w(TAG, "Unable to read token file", e);
                        this.mEverStored = new File(this.mBaseStateDir, "processed");
                        file = new File(this.mBaseStateDir, "processed.new");
                        if (file.exists()) {
                        }
                        if (this.mEverStored.exists()) {
                        }
                        synchronized (this.mQueueLock) {
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
                tokenStream = dataInputStream;
                this.mEverStored = new File(this.mBaseStateDir, "processed");
                file = new File(this.mBaseStateDir, "processed.new");
                if (file.exists()) {
                    file.delete();
                }
                if (this.mEverStored.exists()) {
                    DataOutputStream dataOutputStream;
                    DataOutputStream temp = null;
                    DataInputStream in = null;
                    try {
                        dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    } catch (EOFException e4) {
                        if (!file.renameTo(this.mEverStored)) {
                        }
                        if (temp != null) {
                        }
                        if (in != null) {
                        }
                        synchronized (this.mQueueLock) {
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
                    } catch (IOException e5) {
                        e = e5;
                        try {
                            Slog.e(TAG, "Error in processed file", e);
                            if (temp != null) {
                            }
                            if (in != null) {
                            }
                            synchronized (this.mQueueLock) {
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
                        } catch (Throwable th4) {
                            th = th4;
                            if (temp != null) {
                            }
                            if (in != null) {
                            }
                            throw th;
                        }
                    }
                    try {
                        DataInputStream in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(this.mEverStored)));
                        while (true) {
                            try {
                                String pkg = in2.readUTF();
                                try {
                                    this.mPackageManager.getPackageInfo(pkg, 0);
                                    this.mEverStoredApps.add(pkg);
                                    dataOutputStream.writeUTF(pkg);
                                } catch (NameNotFoundException e6) {
                                }
                            } catch (EOFException e7) {
                                in = in2;
                                temp = dataOutputStream;
                            } catch (IOException e8) {
                                e = e8;
                                in = in2;
                                temp = dataOutputStream;
                            } catch (Throwable th5) {
                                th = th5;
                                in = in2;
                                temp = dataOutputStream;
                            }
                        }
                    } catch (EOFException e9) {
                        temp = dataOutputStream;
                        if (file.renameTo(this.mEverStored)) {
                            Slog.e(TAG, "Error renaming " + file + " to " + this.mEverStored);
                        }
                        if (temp != null) {
                            try {
                                temp.close();
                            } catch (IOException e10) {
                            }
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e11) {
                            }
                        }
                        synchronized (this.mQueueLock) {
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
                    } catch (IOException e12) {
                        e = e12;
                        temp = dataOutputStream;
                        Slog.e(TAG, "Error in processed file", e);
                        if (temp != null) {
                            try {
                                temp.close();
                            } catch (IOException e13) {
                            }
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e14) {
                            }
                        }
                        synchronized (this.mQueueLock) {
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
                    } catch (Throwable th6) {
                        th = th6;
                        temp = dataOutputStream;
                        if (temp != null) {
                            try {
                                temp.close();
                            } catch (IOException e15) {
                            }
                        }
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e16) {
                            }
                        }
                        throw th;
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
            } catch (Throwable th7) {
                th = th7;
                tokenStream = dataInputStream;
                if (tokenStream != null) {
                }
                if (th2 == null) {
                }
            }
            Slog.v(TAG, "No ancestral data");
            this.mEverStored = new File(this.mBaseStateDir, "processed");
            file = new File(this.mBaseStateDir, "processed.new");
            if (file.exists()) {
            }
            if (this.mEverStored.exists()) {
            }
            synchronized (this.mQueueLock) {
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
        } catch (Throwable th8) {
            th = th8;
            if (tokenStream != null) {
                try {
                    tokenStream.close();
                } catch (Throwable th9) {
                    if (th2 == null) {
                        th2 = th9;
                    } else if (th2 != th9) {
                        th2.addSuppressed(th9);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (FileNotFoundException e17) {
                } catch (IOException e18) {
                    e = e18;
                    Slog.w(TAG, "Unable to read token file", e);
                    this.mEverStored = new File(this.mBaseStateDir, "processed");
                    file = new File(this.mBaseStateDir, "processed.new");
                    if (file.exists()) {
                    }
                    if (this.mEverStored.exists()) {
                    }
                    synchronized (this.mQueueLock) {
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
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0213  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0213  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<FullBackupEntry> readFullBackupSchedule() {
        Exception e;
        Object fstream;
        Throwable th;
        ArrayList<FullBackupEntry> arrayList;
        boolean changed = false;
        ArrayList<FullBackupEntry> schedule = null;
        List<PackageInfo> apps = PackageManagerBackupAgent.getStorableApplications(this.mPackageManager);
        if (this.mFullBackupScheduleFile.exists()) {
            AutoCloseable fstream2 = null;
            BufferedInputStream bufStream = null;
            DataInputStream in = null;
            try {
                BufferedInputStream bufStream2;
                InputStream fileInputStream = new FileInputStream(this.mFullBackupScheduleFile);
                try {
                    bufStream2 = new BufferedInputStream(fileInputStream);
                } catch (Exception e2) {
                    e = e2;
                    fstream2 = fileInputStream;
                    try {
                        Slog.e(TAG, "Unable to read backup schedule", e);
                        this.mFullBackupScheduleFile.delete();
                        schedule = null;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream);
                        IoUtils.closeQuietly(fstream2);
                        if (schedule == null) {
                        }
                        if (changed) {
                        }
                        return schedule;
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream);
                        IoUtils.closeQuietly(fstream2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fstream2 = fileInputStream;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream);
                    IoUtils.closeQuietly(fstream2);
                    throw th;
                }
                try {
                    DataInputStream dataInputStream = new DataInputStream(bufStream2);
                    try {
                        int version = dataInputStream.readInt();
                        if (version != 1) {
                            Slog.e(TAG, "Unknown backup schedule version " + version);
                            IoUtils.closeQuietly(dataInputStream);
                            IoUtils.closeQuietly(bufStream2);
                            IoUtils.closeQuietly(fileInputStream);
                            return null;
                        }
                        int N = dataInputStream.readInt();
                        arrayList = new ArrayList(N);
                        try {
                            HashSet<String> foundApps = new HashSet(N);
                            int i = 0;
                            while (i < N) {
                                String pkgName = dataInputStream.readUTF();
                                long lastBackup = dataInputStream.readLong();
                                foundApps.add(pkgName);
                                try {
                                    PackageInfo pkg = this.mPackageManager.getPackageInfo(pkgName, 0);
                                    if (appGetsFullBackup(pkg) && appIsEligibleForBackup(pkg.applicationInfo, this.mPackageManager)) {
                                        arrayList.add(new FullBackupEntry(pkgName, lastBackup));
                                        i++;
                                    } else {
                                        if (DEBUG) {
                                            Slog.i(TAG, "Package " + pkgName + " no longer eligible for full backup");
                                        }
                                        i++;
                                    }
                                } catch (NameNotFoundException e3) {
                                    if (DEBUG) {
                                        Slog.i(TAG, "Package " + pkgName + " not installed; dropping from full backup");
                                    }
                                }
                            }
                            for (PackageInfo app : apps) {
                                if (appGetsFullBackup(app) && appIsEligibleForBackup(app.applicationInfo, this.mPackageManager) && !foundApps.contains(app.packageName)) {
                                    arrayList.add(new FullBackupEntry(app.packageName, 0));
                                    changed = true;
                                }
                            }
                            Collections.sort(arrayList);
                            IoUtils.closeQuietly(dataInputStream);
                            IoUtils.closeQuietly(bufStream2);
                            IoUtils.closeQuietly(fileInputStream);
                            schedule = arrayList;
                        } catch (Exception e4) {
                            e = e4;
                            in = dataInputStream;
                            bufStream = bufStream2;
                            fstream2 = fileInputStream;
                            schedule = arrayList;
                            Slog.e(TAG, "Unable to read backup schedule", e);
                            this.mFullBackupScheduleFile.delete();
                            schedule = null;
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(bufStream);
                            IoUtils.closeQuietly(fstream2);
                            if (schedule == null) {
                            }
                            if (changed) {
                            }
                            return schedule;
                        } catch (Throwable th4) {
                            th = th4;
                            in = dataInputStream;
                            bufStream = bufStream2;
                            fstream2 = fileInputStream;
                            schedule = arrayList;
                            IoUtils.closeQuietly(in);
                            IoUtils.closeQuietly(bufStream);
                            IoUtils.closeQuietly(fstream2);
                            throw th;
                        }
                    } catch (Exception e5) {
                        e = e5;
                        in = dataInputStream;
                        bufStream = bufStream2;
                        fstream2 = fileInputStream;
                        Slog.e(TAG, "Unable to read backup schedule", e);
                        this.mFullBackupScheduleFile.delete();
                        schedule = null;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream);
                        IoUtils.closeQuietly(fstream2);
                        if (schedule == null) {
                        }
                        if (changed) {
                        }
                        return schedule;
                    } catch (Throwable th5) {
                        th = th5;
                        in = dataInputStream;
                        bufStream = bufStream2;
                        fstream2 = fileInputStream;
                        IoUtils.closeQuietly(in);
                        IoUtils.closeQuietly(bufStream);
                        IoUtils.closeQuietly(fstream2);
                        throw th;
                    }
                } catch (Exception e6) {
                    e = e6;
                    bufStream = bufStream2;
                    fstream2 = fileInputStream;
                    Slog.e(TAG, "Unable to read backup schedule", e);
                    this.mFullBackupScheduleFile.delete();
                    schedule = null;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream);
                    IoUtils.closeQuietly(fstream2);
                    if (schedule == null) {
                    }
                    if (changed) {
                    }
                    return schedule;
                } catch (Throwable th6) {
                    th = th6;
                    bufStream = bufStream2;
                    fstream2 = fileInputStream;
                    IoUtils.closeQuietly(in);
                    IoUtils.closeQuietly(bufStream);
                    IoUtils.closeQuietly(fstream2);
                    throw th;
                }
            } catch (Exception e7) {
                e = e7;
                Slog.e(TAG, "Unable to read backup schedule", e);
                this.mFullBackupScheduleFile.delete();
                schedule = null;
                IoUtils.closeQuietly(in);
                IoUtils.closeQuietly(bufStream);
                IoUtils.closeQuietly(fstream2);
                if (schedule == null) {
                }
                if (changed) {
                }
                return schedule;
            }
        }
        if (schedule == null) {
            changed = true;
            arrayList = new ArrayList(apps.size());
            for (PackageInfo info : apps) {
                if (appGetsFullBackup(info) && appIsEligibleForBackup(info.applicationInfo, this.mPackageManager)) {
                    arrayList.add(new FullBackupEntry(info.packageName, 0));
                }
            }
        }
        if (changed) {
            writeFullBackupScheduleAsync();
        }
        return schedule;
    }

    private void writeFullBackupScheduleAsync() {
        this.mBackupHandler.removeCallbacks(this.mFullBackupScheduleWriter);
        this.mBackupHandler.post(this.mFullBackupScheduleWriter);
    }

    private void parseLeftoverJournals() {
        File f;
        DataInputStream in;
        Exception e;
        Throwable th;
        int i = 0;
        File[] listFiles = this.mJournalDir.listFiles();
        int length = listFiles.length;
        while (i < length) {
            f = listFiles[i];
            if (this.mJournal == null || f.compareTo(this.mJournal) != 0) {
                in = null;
                try {
                    Slog.i(TAG, "Found stale backup journal, scheduling");
                    DataInputStream in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(f), 8192));
                    while (true) {
                        try {
                            dataChangedImpl(in2.readUTF());
                        } catch (EOFException e2) {
                            in = in2;
                        } catch (Exception e3) {
                            e = e3;
                            in = in2;
                        } catch (Throwable th2) {
                            th = th2;
                            in = in2;
                        }
                    }
                } catch (EOFException e4) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e5) {
                        }
                    }
                    f.delete();
                    i++;
                } catch (Exception e6) {
                    e = e6;
                    try {
                        Slog.e(TAG, "Can't read " + f, e);
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e7) {
                            }
                        }
                        f.delete();
                        i++;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
            } else {
                i++;
            }
        }
        return;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e8) {
            }
        }
        f.delete();
        throw th;
        f.delete();
        throw th;
    }

    private SecretKey buildPasswordKey(String algorithm, String pw, byte[] salt, int rounds) {
        return buildCharArrayKey(algorithm, pw.toCharArray(), salt, rounds);
    }

    private SecretKey buildCharArrayKey(String algorithm, char[] pwArray, byte[] salt, int rounds) {
        try {
            return SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(pwArray, salt, rounds, 256));
        } catch (InvalidKeySpecException e) {
            Slog.e(TAG, "Invalid key spec for PBKDF2!");
        } catch (NoSuchAlgorithmException e2) {
            Slog.e(TAG, "PBKDF2 unavailable!");
        }
        return null;
    }

    private String buildPasswordHash(String algorithm, String pw, byte[] salt, int rounds) {
        SecretKey key = buildPasswordKey(algorithm, pw, salt, rounds);
        if (key != null) {
            return byteArrayToHex(key.getEncoded());
        }
        return null;
    }

    private String byteArrayToHex(byte[] data) {
        StringBuilder buf = new StringBuilder(data.length * 2);
        for (byte toHexString : data) {
            buf.append(Byte.toHexString(toHexString, true));
        }
        return buf.toString();
    }

    private byte[] hexToByteArray(String digits) {
        int bytes = digits.length() / 2;
        if (bytes * 2 != digits.length()) {
            throw new IllegalArgumentException("Hex string must have an even number of digits");
        }
        byte[] result = new byte[bytes];
        for (int i = 0; i < digits.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(digits.substring(i, i + 2), 16);
        }
        return result;
    }

    private byte[] makeKeyChecksum(String algorithm, byte[] pwBytes, byte[] salt, int rounds) {
        char[] mkAsChar = new char[pwBytes.length];
        for (int i = 0; i < pwBytes.length; i++) {
            mkAsChar[i] = (char) pwBytes[i];
        }
        return buildCharArrayKey(algorithm, mkAsChar, salt, rounds).getEncoded();
    }

    private byte[] randomBytes(int bits) {
        byte[] array = new byte[(bits / 8)];
        this.mRng.nextBytes(array);
        return array;
    }

    boolean passwordMatchesSaved(String algorithm, String candidatePw, int rounds) {
        if (this.mPasswordHash == null) {
            if (candidatePw == null || "".equals(candidatePw)) {
                return true;
            }
        } else if (candidatePw != null && candidatePw.length() > 0) {
            if (this.mPasswordHash.equalsIgnoreCase(buildPasswordHash(algorithm, candidatePw, this.mPasswordSalt, rounds))) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c6 A:{SYNTHETIC, Splitter: B:41:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00cb A:{Catch:{ IOException -> 0x00cf }} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00dd A:{SYNTHETIC, Splitter: B:50:0x00dd} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00e2 A:{Catch:{ IOException -> 0x00e6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0174 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0179 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x017e A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c6 A:{SYNTHETIC, Splitter: B:41:0x00c6} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00cb A:{Catch:{ IOException -> 0x00cf }} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0174 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0179 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x017e A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00dd A:{SYNTHETIC, Splitter: B:50:0x00dd} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00e2 A:{Catch:{ IOException -> 0x00e6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0174 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0179 A:{Catch:{ IOException -> 0x0182 }} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x017e A:{Catch:{ IOException -> 0x0182 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setBackupPassword(String currentPw, String newPw) {
        Throwable th;
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupPassword");
        boolean pbkdf2Fallback = this.mPasswordVersion < 2;
        if (!passwordMatchesSaved(PBKDF_CURRENT, currentPw, 10000)) {
            if (((pbkdf2Fallback ? passwordMatchesSaved(PBKDF_FALLBACK, currentPw, 10000) : 0) ^ 1) != 0) {
                return false;
            }
        }
        this.mPasswordVersion = 2;
        FileOutputStream pwFout = null;
        DataOutputStream pwOut = null;
        try {
            FileOutputStream pwFout2 = new FileOutputStream(this.mPasswordVersionFile);
            try {
                DataOutputStream pwOut2 = new DataOutputStream(pwFout2);
                try {
                    pwOut2.writeInt(this.mPasswordVersion);
                    if (pwOut2 != null) {
                        try {
                            pwOut2.close();
                        } catch (IOException e) {
                            Slog.w(TAG, "Unable to close pw version record");
                        }
                    }
                    if (pwFout2 != null) {
                        pwFout2.close();
                    }
                    if (newPw != null && !newPw.isEmpty()) {
                        try {
                            byte[] salt = randomBytes(512);
                            String newPwHash = buildPasswordHash(PBKDF_CURRENT, newPw, salt, 10000);
                            OutputStream pwf = null;
                            OutputStream buffer = null;
                            DataOutputStream out = null;
                            try {
                                OutputStream buffer2;
                                DataOutputStream out2;
                                OutputStream fileOutputStream = new FileOutputStream(this.mPasswordHashFile);
                                try {
                                    buffer2 = new BufferedOutputStream(fileOutputStream);
                                    try {
                                        out2 = new DataOutputStream(buffer2);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        buffer = buffer2;
                                        pwf = fileOutputStream;
                                        if (out != null) {
                                        }
                                        if (buffer != null) {
                                        }
                                        if (pwf != null) {
                                        }
                                        throw th;
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    pwf = fileOutputStream;
                                    if (out != null) {
                                    }
                                    if (buffer != null) {
                                    }
                                    if (pwf != null) {
                                    }
                                    throw th;
                                }
                                try {
                                    out2.writeInt(salt.length);
                                    out2.write(salt);
                                    out2.writeUTF(newPwHash);
                                    out2.flush();
                                    this.mPasswordHash = newPwHash;
                                    this.mPasswordSalt = salt;
                                    if (out2 != null) {
                                        out2.close();
                                    }
                                    if (buffer2 != null) {
                                        buffer2.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                    }
                                    return true;
                                } catch (Throwable th4) {
                                    th = th4;
                                    out = out2;
                                    buffer = buffer2;
                                    pwf = fileOutputStream;
                                    if (out != null) {
                                    }
                                    if (buffer != null) {
                                    }
                                    if (pwf != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                if (out != null) {
                                    out.close();
                                }
                                if (buffer != null) {
                                    buffer.close();
                                }
                                if (pwf != null) {
                                    pwf.close();
                                }
                                throw th;
                            }
                        } catch (IOException e2) {
                            Slog.e(TAG, "Unable to set backup password");
                            return false;
                        }
                    } else if (!this.mPasswordHashFile.exists() || this.mPasswordHashFile.delete()) {
                        this.mPasswordHash = null;
                        this.mPasswordSalt = null;
                        return true;
                    } else {
                        Slog.e(TAG, "Unable to clear backup password");
                        return false;
                    }
                } catch (IOException e3) {
                    pwOut = pwOut2;
                    pwFout = pwFout2;
                    try {
                        Slog.e(TAG, "Unable to write backup pw version; password not changed");
                        if (pwOut != null) {
                        }
                        if (pwFout != null) {
                        }
                        return false;
                    } catch (Throwable th6) {
                        th = th6;
                        if (pwOut != null) {
                        }
                        if (pwFout != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    pwOut = pwOut2;
                    pwFout = pwFout2;
                    if (pwOut != null) {
                    }
                    if (pwFout != null) {
                    }
                    throw th;
                }
            } catch (IOException e4) {
                pwFout = pwFout2;
                Slog.e(TAG, "Unable to write backup pw version; password not changed");
                if (pwOut != null) {
                    try {
                        pwOut.close();
                    } catch (IOException e5) {
                        Slog.w(TAG, "Unable to close pw version record");
                        return false;
                    }
                }
                if (pwFout != null) {
                    pwFout.close();
                }
                return false;
            } catch (Throwable th8) {
                th = th8;
                pwFout = pwFout2;
                if (pwOut != null) {
                    try {
                        pwOut.close();
                    } catch (IOException e6) {
                        Slog.w(TAG, "Unable to close pw version record");
                        throw th;
                    }
                }
                if (pwFout != null) {
                    pwFout.close();
                }
                throw th;
            }
        } catch (IOException e7) {
            Slog.e(TAG, "Unable to write backup pw version; password not changed");
            if (pwOut != null) {
            }
            if (pwFout != null) {
            }
            return false;
        }
    }

    public boolean hasBackupPassword() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "hasBackupPassword");
        if (this.mPasswordHash == null || this.mPasswordHash.length() <= 0) {
            return false;
        }
        return true;
    }

    private boolean backupPasswordMatches(String currentPw) {
        if (hasBackupPassword()) {
            boolean pbkdf2Fallback = this.mPasswordVersion < 2;
            if (!passwordMatchesSaved(PBKDF_CURRENT, currentPw, 10000)) {
                int passwordMatchesSaved;
                if (pbkdf2Fallback) {
                    passwordMatchesSaved = passwordMatchesSaved(PBKDF_FALLBACK, currentPw, 10000);
                } else {
                    passwordMatchesSaved = 0;
                }
                if ((passwordMatchesSaved ^ 1) != 0) {
                    if (DEBUG) {
                        Slog.w(TAG, "Backup password mismatch; aborting");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    void recordInitPendingLocked(boolean isPending, String transportName) {
        this.mBackupHandler.removeMessages(11);
        try {
            IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
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
        } catch (Exception e2) {
            Slog.e(TAG, "Transport " + transportName + " failed to report name: " + e2.getMessage());
        }
        if (isPending) {
            this.mPendingInits.add(transportName);
            this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(11, isPending ? 1 : 0, 0, transportName), 3600000);
        }
    }

    void resetBackupState(File stateFileDir) {
        synchronized (this.mQueueLock) {
            this.mEverStoredApps.clear();
            this.mEverStored.delete();
            this.mCurrentToken = 0;
            writeRestoreTokens();
            for (File sf : stateFileDir.listFiles()) {
                if (!sf.getName().equals(INIT_SENTINEL_FILE_NAME)) {
                    sf.delete();
                }
            }
        }
        synchronized (this.mBackupParticipants) {
            int N = this.mBackupParticipants.size();
            for (int i = 0; i < N; i++) {
                HashSet<String> participants = (HashSet) this.mBackupParticipants.valueAt(i);
                if (participants != null) {
                    for (String packageName : participants) {
                        dataChangedImpl(packageName);
                    }
                }
            }
        }
    }

    void addPackageParticipantsLocked(String[] packageNames) {
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
                HashSet<String> set = (HashSet) this.mBackupParticipants.get(uid);
                if (set == null) {
                    set = new HashSet();
                    this.mBackupParticipants.put(uid, set);
                }
                set.add(pkg.packageName);
                this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(16, pkg.packageName));
            }
        }
    }

    void removePackageParticipantsLocked(String[] packageNames, int oldUid) {
        if (packageNames == null) {
            Slog.w(TAG, "removePackageParticipants with null list");
            return;
        }
        for (String pkg : packageNames) {
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
        int a = packages.size() - 1;
        while (a >= 0) {
            PackageInfo pkg = (PackageInfo) packages.get(a);
            try {
                ApplicationInfo app = pkg.applicationInfo;
                if ((app.flags & 32768) == 0 || app.backupAgentName == null || (app.flags & 67108864) != 0) {
                    packages.remove(a);
                    a--;
                } else {
                    app = this.mPackageManager.getApplicationInfo(pkg.packageName, 1024);
                    pkg.applicationInfo.sharedLibraryFiles = app.sharedLibraryFiles;
                    a--;
                }
            } catch (NameNotFoundException e) {
                packages.remove(a);
            }
        }
        return packages;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0060 A:{SYNTHETIC, Splitter: B:26:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0069 A:{SYNTHETIC, Splitter: B:31:0x0069} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void logBackupComplete(String packageName) {
        Throwable th;
        if (!packageName.equals(PACKAGE_MANAGER_SENTINEL)) {
            synchronized (this.mEverStoredApps) {
                if (this.mEverStoredApps.add(packageName)) {
                    RandomAccessFile out = null;
                    try {
                        RandomAccessFile out2 = new RandomAccessFile(this.mEverStored, "rws");
                        try {
                            out2.seek(out2.length());
                            out2.writeUTF(packageName);
                            if (out2 != null) {
                                try {
                                    out2.close();
                                } catch (IOException e) {
                                }
                            }
                            out = out2;
                        } catch (IOException e2) {
                            out = out2;
                            try {
                                Slog.e(TAG, "Can't log backup of " + packageName + " to " + this.mEverStored);
                                if (out != null) {
                                }
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                if (out != null) {
                                    try {
                                        out.close();
                                    } catch (IOException e3) {
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            out = out2;
                            if (out != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        Slog.e(TAG, "Can't log backup of " + packageName + " to " + this.mEverStored);
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e5) {
                            }
                        }
                        return;
                    }
                }
                return;
            }
        }
        return;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb A:{SYNTHETIC, Splitter: B:37:0x00bb} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x007c A:{SYNTHETIC, Splitter: B:21:0x007c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void removeEverBackedUp(String packageName) {
        IOException e;
        Throwable th;
        if (DEBUG) {
            Slog.v(TAG, "Removing backed-up knowledge of " + packageName);
        }
        synchronized (this.mEverStoredApps) {
            File tempKnownFile = new File(this.mBaseStateDir, "processed.new");
            RandomAccessFile known = null;
            try {
                RandomAccessFile known2 = new RandomAccessFile(tempKnownFile, "rws");
                try {
                    this.mEverStoredApps.remove(packageName);
                    for (String s : this.mEverStoredApps) {
                        known2.writeUTF(s);
                    }
                    known2.close();
                    known = null;
                    if (!tempKnownFile.renameTo(this.mEverStored)) {
                        throw new IOException("Can't rename " + tempKnownFile + " to " + this.mEverStored);
                    }
                } catch (IOException e2) {
                    e = e2;
                    known = known2;
                    try {
                        Slog.w(TAG, "Error rewriting " + this.mEverStored, e);
                        this.mEverStoredApps.clear();
                        tempKnownFile.delete();
                        this.mEverStored.delete();
                        if (known != null) {
                            try {
                                known.close();
                            } catch (IOException e3) {
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (known != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    known = known2;
                    if (known != null) {
                        try {
                            known.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                Slog.w(TAG, "Error rewriting " + this.mEverStored, e);
                this.mEverStoredApps.clear();
                tempKnownFile.delete();
                this.mEverStored.delete();
                if (known != null) {
                }
                return;
            }
        }
        return;
    }

    void writeRestoreTokens() {
        try {
            RandomAccessFile af = new RandomAccessFile(this.mTokenFile, "rwd");
            af.writeInt(1);
            af.writeLong(this.mAncestralToken);
            af.writeLong(this.mCurrentToken);
            if (this.mAncestralPackages == null) {
                af.writeInt(-1);
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

    private String getTransportName(IBackupTransport transport) {
        return this.mTransportManager.getTransportName(transport);
    }

    IBackupAgent bindToAgentSynchronous(ApplicationInfo app, int mode) {
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
                    if (DEBUG) {
                        Slog.i(TAG, "got agent " + this.mConnectedAgent);
                    }
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

    void clearApplicationDataSynchronous(String packageName) {
        try {
            if ((this.mPackageManager.getPackageInfo(packageName, 0).applicationInfo.flags & 64) != 0) {
                ClearDataObserver observer = new ClearDataObserver();
                synchronized (this.mClearDataLock) {
                    this.mClearingData = true;
                    try {
                        this.mActivityManager.clearApplicationUserData(packageName, observer, 0);
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
        } catch (NameNotFoundException e3) {
            Slog.w(TAG, "Tried to clear data for " + packageName + " but not found");
        }
    }

    public long getAvailableRestoreToken(String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreToken");
        long token = this.mAncestralToken;
        synchronized (this.mQueueLock) {
            if (this.mCurrentToken != 0 && this.mEverStoredApps.contains(packageName)) {
                token = this.mCurrentToken;
            }
        }
        return token;
    }

    public int requestBackup(String[] packages, IBackupObserver observer, int flags) {
        return requestBackup(packages, observer, null, flags);
    }

    public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "requestBackup");
        if (packages == null || packages.length < 1) {
            Slog.e(TAG, "No packages named for backup request");
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            monitor = monitorEvent(monitor, 49, null, 1, null);
            throw new IllegalArgumentException("No packages are provided for backup");
        }
        IBackupTransport transport = this.mTransportManager.getCurrentTransportBinder();
        if (transport == null) {
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            monitor = monitorEvent(monitor, 50, null, 1, null);
            return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
        ArrayList<String> fullBackupList = new ArrayList();
        ArrayList<String> kvBackupList = new ArrayList();
        for (String packageName : packages) {
            if (PACKAGE_MANAGER_SENTINEL.equals(packageName)) {
                kvBackupList.add(packageName);
            } else {
                try {
                    PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 64);
                    if (!appIsEligibleForBackup(packageInfo.applicationInfo, this.mPackageManager)) {
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
        }
        EventLog.writeEvent(EventLogTags.BACKUP_REQUESTED, new Object[]{Integer.valueOf(packages.length), Integer.valueOf(kvBackupList.size()), Integer.valueOf(fullBackupList.size())});
        try {
            String dirName = transport.transportDirName();
            boolean nonIncrementalBackup = (flags & 1) != 0;
            Message msg = this.mBackupHandler.obtainMessage(15);
            msg.obj = new BackupParams(transport, dirName, kvBackupList, fullBackupList, observer, monitor, true, nonIncrementalBackup);
            this.mBackupHandler.sendMessage(msg);
            return 0;
        } catch (Exception e2) {
            Slog.e(TAG, "Transport unavailable while attempting backup: " + e2.getMessage());
            sendBackupFinished(observer, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
            return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
        }
    }

    public void cancelBackups() {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "cancelBackups");
        long oldToken = Binder.clearCallingIdentity();
        try {
            List<Integer> operationsToCancel = new ArrayList();
            synchronized (this.mCurrentOpLock) {
                for (int i = 0; i < this.mCurrentOperations.size(); i++) {
                    Operation op = (Operation) this.mCurrentOperations.valueAt(i);
                    int token = this.mCurrentOperations.keyAt(i);
                    if (op.type == 2) {
                        operationsToCancel.add(Integer.valueOf(token));
                    }
                }
            }
            for (Integer token2 : operationsToCancel) {
                handleCancel(token2.intValue(), true);
            }
            KeyValueBackupJob.schedule(this.mContext, 3600000);
            FullBackupJob.schedule(this.mContext, 7200000);
            Binder.restoreCallingIdentity(oldToken);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldToken);
        }
    }

    void prepareOperationTimeout(int token, long interval, BackupRestoreTask callback, int operationType) {
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

    private void removeOperation(int token) {
        synchronized (this.mCurrentOpLock) {
            if (this.mCurrentOperations.get(token) == null) {
                Slog.w(TAG, "Duplicate remove for operation. token=" + Integer.toHexString(token));
            }
            this.mCurrentOperations.remove(token);
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            r2 = r3.state;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean waitUntilOperationComplete(int token) {
        Operation op;
        int finalState = 0;
        synchronized (this.mCurrentOpLock) {
            while (true) {
                op = (Operation) this.mCurrentOperations.get(token);
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

    void handleCancel(int token, boolean cancelAll) {
        Operation op;
        synchronized (this.mCurrentOpLock) {
            op = (Operation) this.mCurrentOperations.get(token);
            int state = op != null ? op.state : -1;
            if (state == 1) {
                if (DEBUG) {
                    Slog.w(TAG, "Operation already got an ack.Should have been removed from mCurrentOperations.");
                }
                op = null;
                this.mCurrentOperations.delete(token);
            } else if (state == 0) {
                if (DEBUG) {
                    Slog.v(TAG, "Cancel: token=" + Integer.toHexString(token));
                }
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

    private boolean isBackupOperationInProgress() {
        synchronized (this.mCurrentOpLock) {
            for (int i = 0; i < this.mCurrentOperations.size(); i++) {
                Operation op = (Operation) this.mCurrentOperations.valueAt(i);
                if (op.type == 2 && op.state == 0) {
                    return true;
                }
            }
            return false;
        }
    }

    static void routeSocketDataToOutput(ParcelFileDescriptor inPipe, OutputStream out) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(inPipe.getFileDescriptor()));
        byte[] buffer = new byte[32768];
        while (true) {
            int chunkTotal = in.readInt();
            if (chunkTotal > 0) {
                while (chunkTotal > 0) {
                    int nRead = in.read(buffer, 0, chunkTotal > buffer.length ? buffer.length : chunkTotal);
                    out.write(buffer, 0, nRead);
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
                if (app.uid >= 10000 && (app.packageName.equals("com.android.backupconfirm") ^ 1) != 0) {
                    this.mActivityManager.killApplicationProcess(app.processName, app.uid);
                }
            } catch (RemoteException e) {
                Slog.d(TAG, "Lost app trying to shut down");
            }
        }
    }

    static void writeAppManifest(PackageInfo pkg, PackageManager packageManager, File manifestFile, boolean withApk, boolean withWidgets) throws IOException {
        StringBuilder builder = new StringBuilder(4096);
        StringBuilderPrinter printer = new StringBuilderPrinter(builder);
        printer.println(Integer.toString(1));
        printer.println(pkg.packageName);
        printer.println(Integer.toString(pkg.versionCode));
        printer.println(Integer.toString(VERSION.SDK_INT));
        String installerName = packageManager.getInstallerPackageName(pkg.packageName);
        if (installerName == null) {
            installerName = "";
        }
        printer.println(installerName);
        printer.println(withApk ? "1" : "0");
        if (pkg.signatures == null) {
            printer.println("0");
        } else {
            printer.println(Integer.toString(pkg.signatures.length));
            for (Signature sig : pkg.signatures) {
                printer.println(sig.toCharsString());
            }
        }
        FileOutputStream outstream = new FileOutputStream(manifestFile);
        outstream.write(builder.toString().getBytes());
        outstream.close();
        manifestFile.setLastModified(0);
    }

    boolean deviceIsEncrypted() {
        boolean z = true;
        try {
            if (this.mStorageManager.getEncryptionState() == 1) {
                z = false;
            } else if (this.mStorageManager.getPasswordType() == 1) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Slog.e(TAG, "Unable to communicate with storagemanager service: " + e.getMessage());
            return true;
        }
    }

    void scheduleNextFullBackupJob(long transportMinLatency) {
        synchronized (this.mQueueLock) {
            if (this.mFullBackupQueue.size() > 0) {
                long timeSinceLast = System.currentTimeMillis() - ((FullBackupEntry) this.mFullBackupQueue.get(0)).lastBackup;
                final long latency = Math.max(transportMinLatency, timeSinceLast < 86400000 ? 86400000 - timeSinceLast : 0);
                this.mBackupHandler.postDelayed(new Runnable() {
                    public void run() {
                        FullBackupJob.schedule(BackupManagerService.this.mContext, latency);
                    }
                }, 2500);
            } else if (DEBUG_SCHEDULING) {
                Slog.i(TAG, "Full backup queue empty; not scheduling");
            }
        }
    }

    void dequeueFullBackupLocked(String packageName) {
        for (int i = this.mFullBackupQueue.size() - 1; i >= 0; i--) {
            if (packageName.equals(((FullBackupEntry) this.mFullBackupQueue.get(i)).packageName)) {
                this.mFullBackupQueue.remove(i);
            }
        }
    }

    void enqueueFullBackup(String packageName, long lastBackedUp) {
        FullBackupEntry newEntry = new FullBackupEntry(packageName, lastBackedUp);
        synchronized (this.mQueueLock) {
            dequeueFullBackupLocked(packageName);
            int which = -1;
            if (lastBackedUp > 0) {
                which = this.mFullBackupQueue.size() - 1;
                while (which >= 0) {
                    if (((FullBackupEntry) this.mFullBackupQueue.get(which)).lastBackup <= lastBackedUp) {
                        this.mFullBackupQueue.add(which + 1, newEntry);
                        break;
                    }
                    which--;
                }
            }
            if (which < 0) {
                this.mFullBackupQueue.add(0, newEntry);
            }
        }
        writeFullBackupScheduleAsync();
    }

    private boolean fullBackupAllowable(IBackupTransport transport) {
        if (transport == null) {
            Slog.w(TAG, "Transport not present; full data backup not performed");
            return false;
        }
        try {
            if (new File(new File(this.mBaseStateDir, transport.transportDirName()), PACKAGE_MANAGER_SENTINEL).length() > 0) {
                return true;
            }
            if (DEBUG) {
                Slog.i(TAG, "Full backup requested but dataset not yet initialized");
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "Unable to get transport name: " + e.getMessage());
            return false;
        }
    }

    boolean beginFullBackup(FullBackupJob scheduledJob) {
        long now = System.currentTimeMillis();
        FullBackupEntry entry = null;
        long latency = 86400000;
        if (!this.mEnabled || (this.mProvisioned ^ 1) != 0) {
            return false;
        }
        if (this.mPowerManager.getPowerSaveState(4).batterySaverEnabled) {
            if (DEBUG) {
                Slog.i(TAG, "Deferring scheduled full backups in battery saver mode");
            }
            FullBackupJob.schedule(this.mContext, 14400000);
            return false;
        }
        if (DEBUG_SCHEDULING) {
            Slog.i(TAG, "Beginning scheduled full backup operation");
        }
        synchronized (this.mQueueLock) {
            if (this.mRunningFullBackupTask != null) {
                Slog.e(TAG, "Backup triggered but one already/still running!");
                return false;
            }
            boolean runBackup = true;
            while (this.mFullBackupQueue.size() != 0) {
                boolean headBusy = false;
                if (!fullBackupAllowable(this.mTransportManager.getCurrentTransportBinder())) {
                    runBackup = false;
                    latency = 14400000;
                }
                if (runBackup) {
                    entry = (FullBackupEntry) this.mFullBackupQueue.get(0);
                    long timeSinceRun = now - entry.lastBackup;
                    runBackup = timeSinceRun >= 86400000;
                    if (!runBackup) {
                        latency = 86400000 - timeSinceRun;
                        break;
                    }
                    try {
                        PackageInfo appInfo = this.mPackageManager.getPackageInfo(entry.packageName, 0);
                        if (appGetsFullBackup(appInfo)) {
                            if ((appInfo.applicationInfo.privateFlags & 8192) == 0) {
                                headBusy = this.mActivityManager.isAppForeground(appInfo.applicationInfo.uid);
                            } else {
                                headBusy = false;
                            }
                            if (headBusy) {
                                long nextEligible = (System.currentTimeMillis() + 3600000) + ((long) this.mTokenGenerator.nextInt(BUSY_BACKOFF_FUZZ));
                                if (DEBUG_SCHEDULING) {
                                    Slog.i(TAG, "Full backup time but " + entry.packageName + " is busy; deferring to " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(nextEligible)));
                                }
                                enqueueFullBackup(entry.packageName, nextEligible - 86400000);
                            }
                        } else {
                            this.mFullBackupQueue.remove(0);
                            headBusy = true;
                        }
                    } catch (NameNotFoundException e) {
                        runBackup = this.mFullBackupQueue.size() > 1;
                    } catch (RemoteException e2) {
                    }
                }
                if (!headBusy) {
                    break;
                }
            }
            if (DEBUG) {
                Slog.i(TAG, "Backup queue empty; doing nothing");
            }
            runBackup = false;
            if (runBackup) {
                this.mFullBackupQueue.remove(0);
                this.mRunningFullBackupTask = new PerformFullTransportBackupTask(null, new String[]{entry.packageName}, true, scheduledJob, new CountDownLatch(1), null, null, false);
                this.mWakelock.acquire();
                new Thread(this.mRunningFullBackupTask).start();
                return true;
            }
            if (DEBUG_SCHEDULING) {
                Slog.i(TAG, "Nothing pending full backup; rescheduling +" + latency);
            }
            final long deferTime = latency;
            this.mBackupHandler.post(new Runnable() {
                public void run() {
                    FullBackupJob.schedule(BackupManagerService.this.mContext, deferTime);
                }
            });
            return false;
        }
    }

    void endFullBackup() {
        new Thread(new Runnable() {
            public void run() {
                PerformFullTransportBackupTask pftbt = null;
                synchronized (BackupManagerService.this.mQueueLock) {
                    if (BackupManagerService.this.mRunningFullBackupTask != null) {
                        pftbt = BackupManagerService.this.mRunningFullBackupTask;
                    }
                }
                if (pftbt != null) {
                    if (BackupManagerService.DEBUG_SCHEDULING) {
                        Slog.i(BackupManagerService.TAG, "Telling running backup to stop");
                    }
                    pftbt.handleCancel(true);
                }
            }
        }, "end-full-backup").start();
    }

    static boolean signaturesMatch(Signature[] storedSigs, PackageInfo target) {
        if (target == null) {
            return false;
        }
        if ((target.applicationInfo.flags & 1) != 0) {
            return true;
        }
        Signature[] deviceSigs = target.signatures;
        if ((storedSigs == null || storedSigs.length == 0) && (deviceSigs == null || deviceSigs.length == 0)) {
            return true;
        }
        if (storedSigs == null || deviceSigs == null) {
            return false;
        }
        for (Signature equals : storedSigs) {
            boolean match = false;
            for (Object equals2 : deviceSigs) {
                if (equals.equals(equals2)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    void restoreWidgetData(String packageName, byte[] widgetData) {
        AppWidgetBackupBridge.restoreWidgetState(packageName, widgetData, 0);
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
        if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1) {
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
                for (int i = 0; i < N; i++) {
                    HashSet<String> s = (HashSet) this.mBackupParticipants.valueAt(i);
                    if (s != null) {
                        targets.addAll(s);
                    }
                }
            }
        }
        return targets;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0055 A:{SYNTHETIC, Splitter: B:17:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x005e A:{SYNTHETIC, Splitter: B:22:0x005e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeToJournalLocked(String str) {
        IOException e;
        Throwable th;
        RandomAccessFile out = null;
        try {
            if (this.mJournal == null) {
                this.mJournal = File.createTempFile("journal", null, this.mJournalDir);
            }
            RandomAccessFile out2 = new RandomAccessFile(this.mJournal, "rws");
            try {
                out2.seek(out2.length());
                out2.writeUTF(str);
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (IOException e2) {
                    }
                }
                out = out2;
            } catch (IOException e3) {
                e = e3;
                out = out2;
                try {
                    Slog.e(TAG, "Can't write " + str + " to backup journal", e);
                    this.mJournal = null;
                    if (out == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            Slog.e(TAG, "Can't write " + str + " to backup journal", e);
            this.mJournal = null;
            if (out == null) {
                try {
                    out.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    public void dataChanged(final String packageName) {
        if (UserHandle.getCallingUserId() == 0) {
            final HashSet<String> targets = dataChangedTargets(packageName);
            if (targets == null) {
                Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "'" + " uid=" + Binder.getCallingUid());
            } else {
                this.mBackupHandler.post(new Runnable() {
                    public void run() {
                        BackupManagerService.this.dataChangedImpl(packageName, targets);
                    }
                });
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
            if (this.mContext.checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1) {
                apps = (HashSet) this.mBackupParticipants.get(Binder.getCallingUid());
            } else {
                apps = new HashSet();
                int N = this.mBackupParticipants.size();
                for (int i = 0; i < N; i++) {
                    HashSet<String> s = (HashSet) this.mBackupParticipants.valueAt(i);
                    if (s != null) {
                        apps.addAll(s);
                    }
                }
            }
            if (apps.contains(packageName)) {
                this.mBackupHandler.removeMessages(12);
                synchronized (this.mQueueLock) {
                    IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
                    if (transport == null) {
                        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(12, new ClearRetryParams(transportName, packageName)), 3600000);
                        return;
                    }
                    long oldId = Binder.clearCallingIdentity();
                    this.mWakelock.acquire();
                    this.mBackupHandler.sendMessage(this.mBackupHandler.obtainMessage(4, new ClearParams(transport, info)));
                    Binder.restoreCallingIdentity(oldId);
                }
            }
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "No such package '" + packageName + "' - not clearing backup data");
        }
    }

    public void backupNow() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "backupNow");
        if (this.mPowerManager.getPowerSaveState(5).batterySaverEnabled) {
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
        return;
    }

    boolean deviceIsProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    public void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean compress, boolean doKeyValue, String[] pkgList) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "adbBackup");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Backup supported only for the device owner");
        } else if (doAllApps || includeShared || !(pkgList == null || pkgList.length == 0)) {
            long oldId = Binder.clearCallingIdentity();
            try {
                if (deviceIsProvisioned()) {
                    if (DEBUG) {
                        Slog.v(TAG, "Requesting backup: apks=" + includeApks + " obb=" + includeObbs + " shared=" + includeShared + " all=" + doAllApps + " system=" + includeSystem + " includekeyvalue=" + doKeyValue + " pkgs=" + pkgList);
                    }
                    Slog.i(TAG, "Beginning adb backup...");
                    AdbBackupParams params = new AdbBackupParams(fd, includeApks, includeObbs, includeShared, doWidgets, doAllApps, includeSystem, compress, doKeyValue, pkgList);
                    int token = generateToken();
                    synchronized (this.mAdbBackupRestoreConfirmations) {
                        this.mAdbBackupRestoreConfirmations.put(token, params);
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "Starting backup confirmation UI, token=" + token);
                    }
                    if (startConfirmationUi(token, "fullback")) {
                        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
                        startConfirmationTimeout(token, params);
                        if (DEBUG) {
                            Slog.d(TAG, "Waiting for backup completion...");
                        }
                        waitForCompletion(params);
                        try {
                            fd.close();
                        } catch (IOException e) {
                        }
                        Binder.restoreCallingIdentity(oldId);
                        Slog.d(TAG, "Adb backup processing complete.");
                        return;
                    }
                    Slog.e(TAG, "Unable to launch backup confirmation UI");
                    this.mAdbBackupRestoreConfirmations.delete(token);
                    try {
                        fd.close();
                    } catch (IOException e2) {
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.d(TAG, "Adb backup processing complete.");
                    return;
                }
                Slog.i(TAG, "Backup not supported before setup");
            } finally {
                try {
                    fd.close();
                } catch (IOException e3) {
                }
                Binder.restoreCallingIdentity(oldId);
                Slog.d(TAG, "Adb backup processing complete.");
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
        if (fullBackupAllowable(this.mTransportManager.getCurrentTransportBinder())) {
            if (DEBUG) {
                Slog.d(TAG, "fullTransportBackup()");
            }
            long oldId = Binder.clearCallingIdentity();
            try {
                CountDownLatch latch = new CountDownLatch(1);
                PerformFullTransportBackupTask task = new PerformFullTransportBackupTask(null, pkgNames, false, null, latch, null, null, false);
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
                for (String pkg : pkgNames) {
                    enqueueFullBackup(pkg, now);
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

    public void adbRestore(ParcelFileDescriptor fd) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "adbRestore");
        if (UserHandle.getCallingUserId() != 0) {
            throw new IllegalStateException("Restore supported only for the device owner");
        }
        long oldId = Binder.clearCallingIdentity();
        try {
            if (deviceIsProvisioned()) {
                Slog.i(TAG, "Beginning restore...");
                AdbRestoreParams params = new AdbRestoreParams(fd);
                int token = generateToken();
                synchronized (this.mAdbBackupRestoreConfirmations) {
                    this.mAdbBackupRestoreConfirmations.put(token, params);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Starting restore confirmation UI, token=" + token);
                }
                if (startConfirmationUi(token, "fullrest")) {
                    this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 0, 0);
                    startConfirmationTimeout(token, params);
                    if (DEBUG) {
                        Slog.d(TAG, "Waiting for restore completion...");
                    }
                    waitForCompletion(params);
                    try {
                        fd.close();
                    } catch (IOException e) {
                        Slog.w(TAG, "Error trying to close fd after adb restore: " + e);
                    }
                    Binder.restoreCallingIdentity(oldId);
                    Slog.i(TAG, "adb restore processing complete.");
                    return;
                }
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
            Slog.i(TAG, "Full restore not permitted before setup");
        } finally {
            try {
                fd.close();
            } catch (IOException e22) {
                Slog.w(TAG, "Error trying to close fd after adb restore: " + e22);
            }
            Binder.restoreCallingIdentity(oldId);
            Slog.i(TAG, "adb restore processing complete.");
        }
    }

    boolean startConfirmationUi(int token, String action) {
        try {
            Intent confIntent = new Intent(action);
            confIntent.setClassName("com.android.backupconfirm", "com.android.backupconfirm.BackupRestoreConfirmation");
            confIntent.putExtra("conftoken", token);
            confIntent.addFlags(268435456);
            this.mContext.startActivityAsUser(confIntent, UserHandle.SYSTEM);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    void startConfirmationTimeout(int token, AdbParams params) {
        this.mBackupHandler.sendMessageDelayed(this.mBackupHandler.obtainMessage(9, token, 0, params), LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
    }

    void waitForCompletion(AdbParams params) {
        synchronized (params.latch) {
            while (!params.latch.get()) {
                try {
                    params.latch.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    void signalAdbBackupRestoreCompletion(AdbParams params) {
        synchronized (params.latch) {
            params.latch.set(true);
            params.latch.notifyAll();
        }
    }

    public void acknowledgeAdbBackupOrRestore(int token, boolean allow, String curPassword, String encPpassword, IFullBackupRestoreObserver observer) {
        if (DEBUG) {
            Slog.d(TAG, "acknowledgeAdbBackupOrRestore : token=" + token + " allow=" + allow);
        }
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "acknowledgeAdbBackupOrRestore");
        long oldId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mAdbBackupRestoreConfirmations) {
                AdbParams params = (AdbParams) this.mAdbBackupRestoreConfirmations.get(token);
                if (params != null) {
                    this.mBackupHandler.removeMessages(9, params);
                    this.mAdbBackupRestoreConfirmations.delete(token);
                    if (allow) {
                        int verb;
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
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    private static boolean backupSettingMigrated(int userId) {
        return new File(new File(Environment.getDataDirectory(), "backup"), BACKUP_ENABLE_FILE).exists();
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x004a A:{SYNTHETIC, Splitter: B:28:0x004a} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x005d A:{Catch:{ IOException -> 0x0050 }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x004f A:{SYNTHETIC, Splitter: B:31:0x004f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean readBackupEnableState(int userId) {
        Throwable th;
        Throwable th2 = null;
        File enableFile = new File(new File(Environment.getDataDirectory(), "backup"), BACKUP_ENABLE_FILE);
        if (enableFile.exists()) {
            FileInputStream fin = null;
            try {
                FileInputStream fin2 = new FileInputStream(enableFile);
                try {
                    boolean z;
                    if (fin2.read() != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    if (fin2 != null) {
                        try {
                            fin2.close();
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
                        fin = fin2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fin = fin2;
                    if (fin != null) {
                        try {
                            fin.close();
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
                            return false;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fin != null) {
                }
                if (th2 == null) {
                }
            }
        } else {
            if (DEBUG) {
                Slog.i(TAG, "isBackupEnabled() => false due to absent settings file");
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0035 A:{Splitter: B:1:0x001d, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0075 A:{Splitter: B:5:0x0025, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Missing block: B:9:0x0035, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:11:?, code:
            android.util.Slog.e(TAG, "Unable to record backup enable state; reverting to disabled: " + r1.getMessage());
            android.provider.Settings.Secure.putStringForUser(sInstance.mContext.getContentResolver(), BACKUP_ENABLE_FILE, null, r11);
            r2.delete();
            r6.delete();
     */
    /* JADX WARNING: Missing block: B:12:0x0069, code:
            libcore.io.IoUtils.closeQuietly(r3);
     */
    /* JADX WARNING: Missing block: B:13:0x006d, code:
            r7 = th;
     */
    /* JADX WARNING: Missing block: B:18:0x0075, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:19:0x0076, code:
            r3 = r4;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeBackupEnableState(boolean enable, int userId) {
        File base = new File(Environment.getDataDirectory(), "backup");
        File enableFile = new File(base, BACKUP_ENABLE_FILE);
        File stage = new File(base, "backup_enabled-stage");
        AutoCloseable fout = null;
        try {
            FileOutputStream fout2 = new FileOutputStream(stage);
            try {
                fout2.write(enable ? 1 : 0);
                fout2.close();
                stage.renameTo(enableFile);
                IoUtils.closeQuietly(fout2);
                FileOutputStream fileOutputStream = fout2;
            } catch (IOException e) {
            } catch (Throwable th) {
                Throwable th2 = th;
                Object fout3 = fout2;
                IoUtils.closeQuietly(fout);
                throw th2;
            }
        } catch (IOException e2) {
        }
    }

    public void setBackupEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "setBackupEnabled");
        Slog.i(TAG, "Backup enabled => " + enable);
        long oldId = Binder.clearCallingIdentity();
        try {
            boolean wasEnabled = this.mEnabled;
            synchronized (this) {
                writeBackupEnableState(enable, 0);
                this.mEnabled = enable;
            }
            synchronized (this.mQueueLock) {
                if (enable && (wasEnabled ^ 1) != 0) {
                    if (this.mProvisioned) {
                        KeyValueBackupJob.schedule(this.mContext);
                        scheduleNextFullBackupJob(0);
                    }
                }
                if (!enable) {
                    KeyValueBackupJob.cancel(this.mContext);
                    if (wasEnabled && this.mProvisioned) {
                        for (String transport : this.mTransportManager.getBoundTransportNames()) {
                            recordInitPendingLocked(true, transport);
                        }
                        this.mAlarmManager.set(0, System.currentTimeMillis(), this.mRunInitIntent);
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
                Secure.putInt(this.mContext.getContentResolver(), "backup_auto_restore", doAutoRestore ? 1 : 0);
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
        return this.mTransportManager.getCurrentTransportName();
    }

    public String[] listAllTransports() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "listAllTransports");
        return this.mTransportManager.getBoundTransportNames();
    }

    public ComponentName[] listAllTransportComponents() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "listAllTransportComponents");
        return this.mTransportManager.getAllTransportCompenents();
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

    public String selectBackupTransport(String transport) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "selectBackupTransport");
        long oldId = Binder.clearCallingIdentity();
        try {
            String prevTransport = this.mTransportManager.selectTransport(transport);
            updateStateForTransport(transport);
            Slog.v(TAG, "selectBackupTransport() set " + this.mTransportManager.getCurrentTransportName() + " returning " + prevTransport);
            return prevTransport;
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    public void selectBackupTransportAsync(final ComponentName transport, final ISelectBackupTransportCallback listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "selectBackupTransportAsync");
        long oldId = Binder.clearCallingIdentity();
        Slog.v(TAG, "selectBackupTransportAsync() called with transport " + transport.flattenToShortString());
        this.mTransportManager.ensureTransportReady(transport, new SelectBackupTransportCallback() {
            public void onSuccess(String transportName) {
                BackupManagerService.this.mTransportManager.selectTransport(transportName);
                BackupManagerService.this.updateStateForTransport(BackupManagerService.this.mTransportManager.getCurrentTransportName());
                Slog.v(BackupManagerService.TAG, "Transport successfully selected: " + transport.flattenToShortString());
                try {
                    listener.onSuccess(transportName);
                } catch (RemoteException e) {
                }
            }

            public void onFailure(int reason) {
                Slog.v(BackupManagerService.TAG, "Failed to select transport: " + transport.flattenToShortString());
                try {
                    listener.onFailure(reason);
                } catch (RemoteException e) {
                }
            }
        });
        Binder.restoreCallingIdentity(oldId);
    }

    private void updateStateForTransport(String newTransportName) {
        Secure.putString(this.mContext.getContentResolver(), "backup_transport", newTransportName);
        IBackupTransport transport = this.mTransportManager.getTransportBinder(newTransportName);
        if (transport != null) {
            try {
                this.mCurrentToken = transport.getCurrentRestoreSet();
                return;
            } catch (Exception e) {
                this.mCurrentToken = 0;
                return;
            }
        }
        this.mCurrentToken = 0;
    }

    public Intent getConfigurationIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getConfigurationIntent");
        IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
        if (transport != null) {
            try {
                return transport.configurationIntent();
            } catch (Exception e) {
                Slog.e(TAG, "Unable to get configuration intent from transport: " + e.getMessage());
            }
        }
        return null;
    }

    public String getDestinationString(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDestinationString");
        IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
        if (transport != null) {
            try {
                return transport.currentDestinationString();
            } catch (Exception e) {
                Slog.e(TAG, "Unable to get string from transport: " + e.getMessage());
            }
        }
        return null;
    }

    public Intent getDataManagementIntent(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementIntent");
        IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
        if (transport != null) {
            try {
                return transport.dataManagementIntent();
            } catch (Exception e) {
                Slog.e(TAG, "Unable to get management intent from transport: " + e.getMessage());
            }
        }
        return null;
    }

    public String getDataManagementLabel(String transportName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "getDataManagementLabel");
        IBackupTransport transport = this.mTransportManager.getTransportBinder(transportName);
        if (transport != null) {
            try {
                return transport.dataManagementLabel();
            } catch (Exception e) {
                Slog.e(TAG, "Unable to get management label from transport: " + e.getMessage());
            }
        }
        return null;
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
        if (DEBUG) {
            Slog.v(TAG, "restoreAtInstall pkg=" + packageName + " token=" + Integer.toHexString(token) + " restoreSet=" + Long.toHexString(restoreSet));
        }
        if (restoreSet == 0) {
            skip = true;
        }
        IBackupTransport transport = this.mTransportManager.getCurrentTransportBinder();
        if (transport == null) {
            if (DEBUG) {
                Slog.w(TAG, "No transport");
            }
            skip = true;
        }
        if (!this.mAutoRestore) {
            if (DEBUG) {
                Slog.w(TAG, "Non-restorable state: auto=" + this.mAutoRestore);
            }
            skip = true;
        }
        if (!skip) {
            try {
                String dirName = transport.transportDirName();
                this.mWakelock.acquire();
                Message msg = this.mBackupHandler.obtainMessage(3);
                msg.obj = new RestoreParams(transport, dirName, null, null, restoreSet, packageName, token);
                this.mBackupHandler.sendMessage(msg);
            } catch (Exception e) {
                Slog.e(TAG, "Unable to contact transport: " + e.getMessage());
                skip = true;
            }
        }
        if (skip) {
            if (DEBUG) {
                Slog.v(TAG, "Finishing install immediately");
            }
            try {
                this.mPackageManagerBinder.finishPackageInstall(token, false);
            } catch (RemoteException e2) {
            }
        }
    }

    public IRestoreSession beginRestoreSession(String packageName, String transport) {
        if (DEBUG) {
            Slog.v(TAG, "beginRestoreSession: pkg=" + packageName + " transport=" + transport);
        }
        boolean needPermission = true;
        if (transport == null) {
            transport = this.mTransportManager.getCurrentTransportName();
            if (packageName != null) {
                try {
                    if (this.mPackageManager.getPackageInfo(packageName, 0).applicationInfo.uid == Binder.getCallingUid()) {
                        needPermission = false;
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
                this.mBackupHandler.sendEmptyMessageDelayed(8, LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
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
                this.mBackupHandler.removeMessages(8);
            }
        }
    }

    public void opComplete(int token, long result) {
        Operation op;
        synchronized (this.mCurrentOpLock) {
            op = (Operation) this.mCurrentOperations.get(token);
            if (op != null) {
                if (op.state == -1) {
                    op = null;
                    this.mCurrentOperations.delete(token);
                } else if (op.state == 1) {
                    if (DEBUG) {
                        Slog.w(TAG, "Received duplicate ack for token=" + Integer.toHexString(token));
                    }
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
        try {
            PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packageName, 64);
            if (!appIsEligibleForBackup(packageInfo.applicationInfo, this.mPackageManager) || appIsStopped(packageInfo.applicationInfo)) {
                return false;
            }
            IBackupTransport transport = this.mTransportManager.getCurrentTransportBinder();
            if (transport != null) {
                try {
                    return transport.isAppEligibleForBackup(packageInfo, appGetsFullBackup(packageInfo));
                } catch (Exception e) {
                    Slog.e(TAG, "Unable to ask about eligibility: " + e.getMessage());
                }
            }
            return true;
        } catch (NameNotFoundException e2) {
            return false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            long identityToken = Binder.clearCallingIdentity();
            if (args != null) {
                int i = 0;
                try {
                    int length = args.length;
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
        synchronized (this.mQueueLock) {
            pw.println("Backup Manager is " + (this.mEnabled ? "enabled" : "disabled") + " / " + (!this.mProvisioned ? "not " : "") + "provisioned / " + (this.mPendingInits.size() == 0 ? "not " : "") + "pending init");
            pw.println("Auto-restore is " + (this.mAutoRestore ? "enabled" : "disabled"));
            if (this.mBackupRunning) {
                pw.println("Backup currently running");
            }
            pw.println("Last backup pass started: " + this.mLastBackupPass + " (now = " + System.currentTimeMillis() + ')');
            pw.println("  next scheduled: " + KeyValueBackupJob.nextScheduled());
            pw.println("Transport whitelist:");
            for (ComponentName transport : this.mTransportManager.getTransportWhitelist()) {
                pw.print("    ");
                pw.println(transport.flattenToShortString());
            }
            pw.println("Available transports:");
            if (listAllTransports() != null) {
                String[] listAllTransports = listAllTransports();
                int i = 0;
                int length = listAllTransports.length;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    String t = listAllTransports[i2];
                    pw.println((t.equals(this.mTransportManager.getCurrentTransportName()) ? "  * " : "    ") + t);
                    try {
                        IBackupTransport transport2 = this.mTransportManager.getTransportBinder(t);
                        File dir = new File(this.mBaseStateDir, transport2.transportDirName());
                        pw.println("       destination: " + transport2.currentDestinationString());
                        pw.println("       intent: " + transport2.configurationIntent());
                        for (File f : dir.listFiles()) {
                            pw.println("       " + f.getName() + " - " + f.length() + " state bytes");
                        }
                    } catch (Exception e) {
                        Slog.e(TAG, "Error in transport", e);
                        pw.println("        Error: " + e);
                    }
                    i = i2 + 1;
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
            for (int i3 = 0; i3 < N; i3++) {
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

    private Bundle putMonitoringExtra(Bundle extras, String key, String value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putString(key, value);
        return extras;
    }

    private Bundle putMonitoringExtra(Bundle extras, String key, int value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putInt(key, value);
        return extras;
    }

    private Bundle putMonitoringExtra(Bundle extras, String key, long value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putLong(key, value);
        return extras;
    }

    private Bundle putMonitoringExtra(Bundle extras, String key, boolean value) {
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putBoolean(key, value);
        return extras;
    }

    private static IBackupManagerMonitor monitorEvent(IBackupManagerMonitor monitor, int id, PackageInfo pkg, int category, Bundle extras) {
        if (monitor != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putInt("android.app.backup.extra.LOG_EVENT_ID", id);
                bundle.putInt("android.app.backup.extra.LOG_EVENT_CATEGORY", category);
                if (pkg != null) {
                    bundle.putString("android.app.backup.extra.LOG_EVENT_PACKAGE_NAME", pkg.packageName);
                    bundle.putInt("android.app.backup.extra.LOG_EVENT_PACKAGE_VERSION", pkg.versionCode);
                }
                if (extras != null) {
                    bundle.putAll(extras);
                }
                monitor.onEvent(bundle);
                return monitor;
            } catch (RemoteException e) {
                if (DEBUG) {
                    Slog.w(TAG, "backup manager monitor went away");
                }
            }
        }
        return null;
    }
}
