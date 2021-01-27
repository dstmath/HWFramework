package com.android.server.backup.restore;

import android.app.IBackupAgent;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.Signature;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.FileMetadata;
import com.android.server.backup.KeyValueAdbRestoreEngine;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.fullbackup.FullBackupObbConnection;
import com.android.server.backup.utils.BytesReadListener;
import com.android.server.backup.utils.FullBackupRestoreObserverUtils;
import com.android.server.backup.utils.RestoreUtils;
import com.android.server.backup.utils.TarBackupReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class FullRestoreEngine extends RestoreEngine {
    private IBackupAgent mAgent;
    private String mAgentPackage;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    final boolean mAllowApks;
    private final boolean mAllowObbs;
    private long mAppVersion;
    private final UserBackupManagerService mBackupManagerService;
    final byte[] mBuffer;
    private long mBytes;
    private final HashSet<String> mClearedPackages = new HashSet<>();
    private final RestoreDeleteObserver mDeleteObserver = new RestoreDeleteObserver();
    final int mEphemeralOpToken;
    final boolean mIsAdbRestore;
    private final HashMap<String, Signature[]> mManifestSignatures = new HashMap<>();
    final IBackupManagerMonitor mMonitor;
    private final BackupRestoreTask mMonitorTask;
    private FullBackupObbConnection mObbConnection = null;
    private IFullBackupRestoreObserver mObserver;
    final PackageInfo mOnlyPackage;
    private final HashMap<String, String> mPackageInstallers = new HashMap<>();
    private final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap<>();
    private ParcelFileDescriptor[] mPipes = null;
    @GuardedBy({"mPipesLock"})
    private boolean mPipesClosed;
    private final Object mPipesLock = new Object();
    private ApplicationInfo mTargetApp;
    private final int mUserId;
    private byte[] mWidgetData = null;

    static /* synthetic */ long access$014(FullRestoreEngine x0, long x1) {
        long j = x0.mBytes + x1;
        x0.mBytes = j;
        return j;
    }

    public FullRestoreEngine(UserBackupManagerService backupManagerService, BackupRestoreTask monitorTask, IFullBackupRestoreObserver observer, IBackupManagerMonitor monitor, PackageInfo onlyPackage, boolean allowApks, boolean allowObbs, int ephemeralOpToken, boolean isAdbRestore) {
        this.mBackupManagerService = backupManagerService;
        this.mEphemeralOpToken = ephemeralOpToken;
        this.mMonitorTask = monitorTask;
        this.mObserver = observer;
        this.mMonitor = monitor;
        this.mOnlyPackage = onlyPackage;
        this.mAllowApks = allowApks;
        this.mAllowObbs = allowObbs;
        this.mBuffer = new byte[32768];
        this.mBytes = 0;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        this.mIsAdbRestore = isAdbRestore;
        this.mUserId = backupManagerService.getUserId();
    }

    public IBackupAgent getAgent() {
        return this.mAgent;
    }

    public byte[] getWidgetData() {
        return this.mWidgetData;
    }

    /* JADX WARNING: Removed duplicated region for block: B:183:0x048c A[Catch:{ IOException -> 0x0566 }] */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x050f A[Catch:{ IOException -> 0x05b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x051d A[Catch:{ IOException -> 0x05b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:219:0x0562 A[Catch:{ IOException -> 0x05b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:224:0x057a A[Catch:{ IOException -> 0x05b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x05b8  */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x05fc  */
    /* JADX WARNING: Removed duplicated region for block: B:256:0x060d  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0610 A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:266:? A[RETURN, SYNTHETIC] */
    public boolean restoreOneFile(InputStream instream, boolean mustKillAgent, byte[] buffer, PackageInfo onlyPackage, boolean allowApks, int token, IBackupManagerMonitor monitor) {
        FileMetadata info;
        boolean z;
        IOException e;
        FileMetadata info2;
        FileMetadata info3;
        boolean okay;
        boolean okay2;
        long timeout;
        String pkg;
        TarBackupReader tarBackupReader;
        long toCopy;
        boolean okay3;
        boolean agentSuccess;
        boolean agentSuccess2;
        boolean agentSuccess3;
        boolean agentSuccess4;
        boolean okay4;
        RestorePolicy restorePolicy;
        if (!isRunning()) {
            Slog.w(BackupManagerService.TAG, "Restore engine used after halting");
            return false;
        }
        BytesReadListener bytesReadListener = new BytesReadListener() {
            /* class com.android.server.backup.restore.FullRestoreEngine.AnonymousClass1 */

            @Override // com.android.server.backup.utils.BytesReadListener
            public void onBytesRead(long bytesRead) {
                FullRestoreEngine.access$014(FullRestoreEngine.this, bytesRead);
            }
        };
        TarBackupReader tarBackupReader2 = new TarBackupReader(instream, bytesReadListener, monitor);
        try {
            FileMetadata info4 = tarBackupReader2.readTarHeaders();
            if (info4 != null) {
                String pkg2 = info4.packageName;
                if (!pkg2.equals(this.mAgentPackage)) {
                    if (onlyPackage != null) {
                        try {
                            if (!pkg2.equals(onlyPackage.packageName)) {
                                Slog.w(BackupManagerService.TAG, "Expected data for " + onlyPackage + " but saw " + pkg2);
                                setResult(-3);
                                setRunning(false);
                                return false;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                            setResult(-3);
                            info = null;
                            if (info == null) {
                            }
                            if (info != null) {
                            }
                        }
                    }
                    if (!this.mPackagePolicies.containsKey(pkg2)) {
                        this.mPackagePolicies.put(pkg2, RestorePolicy.IGNORE);
                    }
                    if (this.mAgent != null) {
                        Slog.d(BackupManagerService.TAG, "Saw new package; finalizing old one");
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp, this.mIsAdbRestore);
                        this.mTargetApp = null;
                        this.mAgentPackage = null;
                    }
                }
                if (info4.path.equals(UserBackupManagerService.BACKUP_MANIFEST_FILENAME)) {
                    try {
                        Signature[] signatures = tarBackupReader2.readAppManifestAndReturnSignatures(info4);
                        this.mAppVersion = info4.version;
                        try {
                            RestorePolicy restorePolicy2 = tarBackupReader2.chooseRestorePolicy(this.mBackupManagerService.getPackageManager(), allowApks, info4, signatures, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class), this.mUserId);
                            info2 = info4;
                            this.mManifestSignatures.put(info2.packageName, signatures);
                            this.mPackagePolicies.put(pkg2, restorePolicy2);
                            this.mPackageInstallers.put(pkg2, info2.installerPackageName);
                            tarBackupReader2.skipTarPadding(info2.size);
                            this.mObserver = FullBackupRestoreObserverUtils.sendOnRestorePackage(this.mObserver, pkg2);
                        } catch (IOException e3) {
                            e = e3;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                            setResult(-3);
                            info = null;
                            if (info == null) {
                            }
                            if (info != null) {
                            }
                        }
                    } catch (IOException e4) {
                        e = e4;
                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                        setResult(-3);
                        info = null;
                        if (info == null) {
                        }
                        if (info != null) {
                        }
                    }
                } else {
                    info2 = info4;
                    try {
                        if (info2.path.equals(UserBackupManagerService.BACKUP_METADATA_FILENAME)) {
                            tarBackupReader2.readMetadata(info2);
                            this.mWidgetData = tarBackupReader2.getWidgetData();
                            tarBackupReader2.getMonitor();
                            try {
                                tarBackupReader2.skipTarPadding(info2.size);
                                info = info2;
                            } catch (IOException e5) {
                                e = e5;
                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                setResult(-3);
                                info = null;
                                if (info == null) {
                                }
                                if (info != null) {
                                }
                            }
                            if (info == null) {
                                tearDownPipes();
                                z = false;
                                setRunning(false);
                                if (mustKillAgent) {
                                    tearDownAgent(this.mTargetApp, this.mIsAdbRestore);
                                }
                            } else {
                                z = false;
                            }
                            if (info != null) {
                                return true;
                            }
                            return z;
                        }
                        boolean okay5 = true;
                        int i = AnonymousClass2.$SwitchMap$com$android$server$backup$restore$RestorePolicy[this.mPackagePolicies.get(pkg2).ordinal()];
                        int i2 = 3;
                        if (i == 1) {
                            info3 = info2;
                            okay5 = false;
                        } else if (i == 2) {
                            try {
                                if (info2.domain.equals("a")) {
                                    Slog.d(BackupManagerService.TAG, "APK file; installing");
                                    try {
                                        boolean isSuccessfullyInstalled = RestoreUtils.installApk(instream, this.mBackupManagerService.getContext(), this.mDeleteObserver, this.mManifestSignatures, this.mPackagePolicies, info2, this.mPackageInstallers.get(pkg2), bytesReadListener, this.mUserId);
                                        HashMap<String, RestorePolicy> hashMap = this.mPackagePolicies;
                                        if (isSuccessfullyInstalled) {
                                            restorePolicy = RestorePolicy.ACCEPT;
                                        } else {
                                            restorePolicy = RestorePolicy.IGNORE;
                                        }
                                        hashMap.put(pkg2, restorePolicy);
                                        tarBackupReader2.skipTarPadding(info2.size);
                                        return true;
                                    } catch (IOException e6) {
                                        e = e6;
                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                        setResult(-3);
                                        info = null;
                                        if (info == null) {
                                        }
                                        if (info != null) {
                                        }
                                    }
                                } else {
                                    info3 = info2;
                                    this.mPackagePolicies.put(pkg2, RestorePolicy.IGNORE);
                                    okay5 = false;
                                }
                            } catch (IOException e7) {
                                e = e7;
                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                setResult(-3);
                                info = null;
                                if (info == null) {
                                }
                                if (info != null) {
                                }
                            }
                        } else if (i != 3) {
                            Slog.e(BackupManagerService.TAG, "Invalid policy from manifest");
                            okay5 = false;
                            this.mPackagePolicies.put(pkg2, RestorePolicy.IGNORE);
                            info3 = info2;
                        } else if (info2.domain.equals("a")) {
                            Slog.d(BackupManagerService.TAG, "apk present but ACCEPT");
                            okay5 = false;
                            info3 = info2;
                        } else {
                            info3 = info2;
                        }
                        try {
                            if (!isRestorableFile(info3) || !isCanonicalFilePath(info3.path)) {
                                okay = false;
                            } else {
                                okay = okay5;
                            }
                            if (okay && this.mAgent == null) {
                                try {
                                    this.mTargetApp = this.mBackupManagerService.getPackageManager().getApplicationInfoAsUser(pkg2, 0, this.mUserId);
                                    if (!this.mClearedPackages.contains(pkg2)) {
                                        boolean forceClear = shouldForceClearAppDataOnFullRestore(this.mTargetApp.packageName);
                                        if (this.mTargetApp.backupAgentName == null || forceClear) {
                                            Slog.d(BackupManagerService.TAG, "Clearing app data preparatory to full restore");
                                            this.mBackupManagerService.clearApplicationDataBeforeRestore(pkg2);
                                        }
                                        this.mClearedPackages.add(pkg2);
                                    }
                                    setUpPipes();
                                    UserBackupManagerService userBackupManagerService = this.mBackupManagerService;
                                    ApplicationInfo applicationInfo = this.mTargetApp;
                                    if ("k".equals(info3.domain)) {
                                        i2 = 0;
                                    }
                                    this.mAgent = userBackupManagerService.bindToAgentSynchronous(applicationInfo, i2);
                                    this.mAgentPackage = pkg2;
                                } catch (PackageManager.NameNotFoundException | IOException e8) {
                                }
                                if (this.mAgent == null) {
                                    Slog.e(BackupManagerService.TAG, "Unable to create agent for " + pkg2);
                                    okay = false;
                                    tearDownPipes();
                                    this.mPackagePolicies.put(pkg2, RestorePolicy.IGNORE);
                                }
                            }
                            if (okay && !pkg2.equals(this.mAgentPackage)) {
                                Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg2 + " but agent is for " + this.mAgentPackage);
                                okay = false;
                            }
                            if (okay) {
                                long toCopy2 = info3.size;
                                if (pkg2.equals(UserBackupManagerService.SHARED_BACKUP_AGENT_PACKAGE)) {
                                    timeout = this.mAgentTimeoutParameters.getSharedBackupAgentTimeoutMillis();
                                } else {
                                    timeout = this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis();
                                }
                                try {
                                    this.mBackupManagerService.prepareOperationTimeout(token, timeout, this.mMonitorTask, 1);
                                    if ("obb".equals(info3.domain)) {
                                        try {
                                            Slog.d(BackupManagerService.TAG, "Restoring OBB file for " + pkg2 + " : " + info3.path);
                                            agentSuccess4 = true;
                                            okay4 = okay;
                                            try {
                                                toCopy = toCopy2;
                                                try {
                                                    tarBackupReader = tarBackupReader2;
                                                    try {
                                                        this.mObbConnection.restoreObbFile(pkg2, this.mPipes[0], info3.size, info3.type, info3.path, info3.mode, info3.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                                        pkg = pkg2;
                                                    } catch (IOException e9) {
                                                        pkg = pkg2;
                                                        try {
                                                            Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                            agentSuccess = false;
                                                            okay3 = false;
                                                            if (okay3) {
                                                            }
                                                            if (!agentSuccess2) {
                                                            }
                                                            if (!okay2) {
                                                            }
                                                            info = info2;
                                                        } catch (IOException e10) {
                                                            e = e10;
                                                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                                            setResult(-3);
                                                            info = null;
                                                            if (info == null) {
                                                            }
                                                            if (info != null) {
                                                            }
                                                        }
                                                        if (info == null) {
                                                        }
                                                        if (info != null) {
                                                        }
                                                    } catch (RemoteException e11) {
                                                        pkg = pkg2;
                                                        try {
                                                            Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                            agentSuccess = false;
                                                            okay3 = false;
                                                            if (okay3) {
                                                            }
                                                            if (!agentSuccess2) {
                                                            }
                                                            if (!okay2) {
                                                            }
                                                            info = info2;
                                                        } catch (IOException e12) {
                                                            e = e12;
                                                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                                            setResult(-3);
                                                            info = null;
                                                            if (info == null) {
                                                            }
                                                            if (info != null) {
                                                            }
                                                        }
                                                        if (info == null) {
                                                        }
                                                        if (info != null) {
                                                        }
                                                    }
                                                } catch (IOException e13) {
                                                    tarBackupReader = tarBackupReader2;
                                                    pkg = pkg2;
                                                    Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                    agentSuccess = false;
                                                    okay3 = false;
                                                    if (okay3) {
                                                    }
                                                    if (!agentSuccess2) {
                                                    }
                                                    if (!okay2) {
                                                    }
                                                    info = info2;
                                                    if (info == null) {
                                                    }
                                                    if (info != null) {
                                                    }
                                                } catch (RemoteException e14) {
                                                    tarBackupReader = tarBackupReader2;
                                                    pkg = pkg2;
                                                    Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                    agentSuccess = false;
                                                    okay3 = false;
                                                    if (okay3) {
                                                    }
                                                    if (!agentSuccess2) {
                                                    }
                                                    if (!okay2) {
                                                    }
                                                    info = info2;
                                                    if (info == null) {
                                                    }
                                                    if (info != null) {
                                                    }
                                                }
                                            } catch (IOException e15) {
                                                toCopy = toCopy2;
                                                tarBackupReader = tarBackupReader2;
                                                pkg = pkg2;
                                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                agentSuccess = false;
                                                okay3 = false;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess2) {
                                                }
                                                if (!okay2) {
                                                }
                                                info = info2;
                                                if (info == null) {
                                                }
                                                if (info != null) {
                                                }
                                            } catch (RemoteException e16) {
                                                toCopy = toCopy2;
                                                tarBackupReader = tarBackupReader2;
                                                pkg = pkg2;
                                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                agentSuccess = false;
                                                okay3 = false;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess2) {
                                                }
                                                if (!okay2) {
                                                }
                                                info = info2;
                                                if (info == null) {
                                                }
                                                if (info != null) {
                                                }
                                            }
                                        } catch (IOException e17) {
                                            toCopy = toCopy2;
                                            tarBackupReader = tarBackupReader2;
                                            pkg = pkg2;
                                            Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                            agentSuccess = false;
                                            okay3 = false;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess2) {
                                            }
                                            if (!okay2) {
                                            }
                                            info = info2;
                                            if (info == null) {
                                            }
                                            if (info != null) {
                                            }
                                        } catch (RemoteException e18) {
                                            toCopy = toCopy2;
                                            tarBackupReader = tarBackupReader2;
                                            pkg = pkg2;
                                            Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                            agentSuccess = false;
                                            okay3 = false;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess2) {
                                            }
                                            if (!okay2) {
                                            }
                                            info = info2;
                                            if (info == null) {
                                            }
                                            if (info != null) {
                                            }
                                        }
                                    } else {
                                        agentSuccess4 = true;
                                        okay4 = okay;
                                        toCopy = toCopy2;
                                        tarBackupReader = tarBackupReader2;
                                        try {
                                            if ("k".equals(info3.domain)) {
                                                Slog.d(BackupManagerService.TAG, "Restoring key-value file for " + pkg2 + " : " + info3.path);
                                                info3.version = this.mAppVersion;
                                                new Thread(new KeyValueAdbRestoreEngine(this.mBackupManagerService, this.mBackupManagerService.getDataDir(), info3, this.mPipes[0], this.mAgent, token), "restore-key-value-runner").start();
                                                pkg = pkg2;
                                            } else if (this.mTargetApp.processName.equals("system")) {
                                                Slog.d(BackupManagerService.TAG, "system process agent - spinning a thread");
                                                new Thread(new RestoreFileRunnable(this.mBackupManagerService, this.mAgent, info3, this.mPipes[0], token), "restore-sys-runner").start();
                                                pkg = pkg2;
                                            } else {
                                                try {
                                                    pkg = pkg2;
                                                    try {
                                                        this.mAgent.doRestoreFile(this.mPipes[0], info3.size, info3.type, info3.domain, info3.path, info3.mode, info3.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                                    } catch (IOException e19) {
                                                        Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                        agentSuccess = false;
                                                        okay3 = false;
                                                        if (okay3) {
                                                        }
                                                        if (!agentSuccess2) {
                                                        }
                                                        if (!okay2) {
                                                        }
                                                        info = info2;
                                                        if (info == null) {
                                                        }
                                                        if (info != null) {
                                                        }
                                                    } catch (RemoteException e20) {
                                                        Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                        agentSuccess = false;
                                                        okay3 = false;
                                                        if (okay3) {
                                                        }
                                                        if (!agentSuccess2) {
                                                        }
                                                        if (!okay2) {
                                                        }
                                                        info = info2;
                                                        if (info == null) {
                                                        }
                                                        if (info != null) {
                                                        }
                                                    }
                                                } catch (IOException e21) {
                                                    pkg = pkg2;
                                                    Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                    agentSuccess = false;
                                                    okay3 = false;
                                                    if (okay3) {
                                                    }
                                                    if (!agentSuccess2) {
                                                    }
                                                    if (!okay2) {
                                                    }
                                                    info = info2;
                                                    if (info == null) {
                                                    }
                                                    if (info != null) {
                                                    }
                                                } catch (RemoteException e22) {
                                                    pkg = pkg2;
                                                    Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                    agentSuccess = false;
                                                    okay3 = false;
                                                    if (okay3) {
                                                    }
                                                    if (!agentSuccess2) {
                                                    }
                                                    if (!okay2) {
                                                    }
                                                    info = info2;
                                                    if (info == null) {
                                                    }
                                                    if (info != null) {
                                                    }
                                                }
                                            }
                                        } catch (IOException e23) {
                                            pkg = pkg2;
                                            Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                            agentSuccess = false;
                                            okay3 = false;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess2) {
                                            }
                                            if (!okay2) {
                                            }
                                            info = info2;
                                            if (info == null) {
                                            }
                                            if (info != null) {
                                            }
                                        } catch (RemoteException e24) {
                                            pkg = pkg2;
                                            Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                            agentSuccess = false;
                                            okay3 = false;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess2) {
                                            }
                                            if (!okay2) {
                                            }
                                            info = info2;
                                            if (info == null) {
                                            }
                                            if (info != null) {
                                            }
                                        }
                                    }
                                    okay3 = okay4;
                                    agentSuccess = agentSuccess4;
                                } catch (IOException e25) {
                                    toCopy = toCopy2;
                                    tarBackupReader = tarBackupReader2;
                                    pkg = pkg2;
                                    Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                    agentSuccess = false;
                                    okay3 = false;
                                    if (okay3) {
                                    }
                                    if (!agentSuccess2) {
                                    }
                                    if (!okay2) {
                                    }
                                    info = info2;
                                    if (info == null) {
                                    }
                                    if (info != null) {
                                    }
                                } catch (RemoteException e26) {
                                    toCopy = toCopy2;
                                    tarBackupReader = tarBackupReader2;
                                    pkg = pkg2;
                                    Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                    agentSuccess = false;
                                    okay3 = false;
                                    if (okay3) {
                                    }
                                    if (!agentSuccess2) {
                                    }
                                    if (!okay2) {
                                    }
                                    info = info2;
                                    if (info == null) {
                                    }
                                    if (info != null) {
                                    }
                                }
                                if (okay3) {
                                    FileOutputStream pipe = new FileOutputStream(this.mPipes[1].getFileDescriptor());
                                    boolean pipeOkay = true;
                                    long toCopy3 = toCopy;
                                    while (true) {
                                        if (toCopy3 <= 0) {
                                            okay2 = okay3;
                                            break;
                                        }
                                        int nRead = instream.read(buffer, 0, toCopy3 > ((long) buffer.length) ? buffer.length : (int) toCopy3);
                                        if (nRead >= 0) {
                                            agentSuccess3 = agentSuccess;
                                            okay2 = okay3;
                                            this.mBytes += (long) nRead;
                                        } else {
                                            agentSuccess3 = agentSuccess;
                                            okay2 = okay3;
                                        }
                                        if (nRead <= 0) {
                                            break;
                                        }
                                        toCopy3 -= (long) nRead;
                                        if (pipeOkay) {
                                            try {
                                                pipe.write(buffer, 0, nRead);
                                            } catch (IOException e27) {
                                                Slog.e(BackupManagerService.TAG, "Failed to write to restore pipe: " + e27.getMessage());
                                                pipeOkay = false;
                                            }
                                        }
                                        okay3 = okay2;
                                        agentSuccess = agentSuccess3;
                                    }
                                    try {
                                        tarBackupReader.skipTarPadding(info3.size);
                                    } catch (IOException e28) {
                                        e = e28;
                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                        setResult(-3);
                                        info = null;
                                        if (info == null) {
                                        }
                                        if (info != null) {
                                        }
                                    }
                                    try {
                                        agentSuccess2 = this.mBackupManagerService.waitUntilOperationComplete(token);
                                    } catch (IOException e29) {
                                        e = e29;
                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                        setResult(-3);
                                        info = null;
                                        if (info == null) {
                                        }
                                        if (info != null) {
                                        }
                                    }
                                } else {
                                    okay2 = okay3;
                                    agentSuccess2 = agentSuccess;
                                }
                                if (!agentSuccess2) {
                                    Slog.w(BackupManagerService.TAG, "Agent failure restoring " + pkg + "; ending restore");
                                    this.mBackupManagerService.getBackupHandler().removeMessages(18);
                                    tearDownPipes();
                                    tearDownAgent(this.mTargetApp, false);
                                    this.mAgent = null;
                                    this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                    if (onlyPackage != null) {
                                        setResult(-2);
                                        setRunning(false);
                                        return false;
                                    }
                                }
                            } else {
                                okay2 = okay;
                            }
                            if (!okay2) {
                                long bytesToConsume = (info3.size + 511) & -512;
                                while (true) {
                                    if (bytesToConsume <= 0) {
                                        info2 = info3;
                                        break;
                                    }
                                    long nRead2 = (long) instream.read(buffer, 0, bytesToConsume > ((long) buffer.length) ? buffer.length : (int) bytesToConsume);
                                    if (nRead2 >= 0) {
                                        info2 = info3;
                                        this.mBytes += nRead2;
                                    } else {
                                        info2 = info3;
                                    }
                                    if (nRead2 <= 0) {
                                        break;
                                    }
                                    bytesToConsume -= nRead2;
                                    info3 = info2;
                                }
                            } else {
                                info2 = info3;
                            }
                        } catch (IOException e30) {
                            e = e30;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                            setResult(-3);
                            info = null;
                            if (info == null) {
                            }
                            if (info != null) {
                            }
                        }
                    } catch (IOException e31) {
                        e = e31;
                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                        setResult(-3);
                        info = null;
                        if (info == null) {
                        }
                        if (info != null) {
                        }
                    }
                }
            } else {
                info2 = info4;
            }
            info = info2;
        } catch (IOException e32) {
            e = e32;
            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
            setResult(-3);
            info = null;
            if (info == null) {
            }
            if (info != null) {
            }
        }
        if (info == null) {
        }
        if (info != null) {
        }
    }

    /* renamed from: com.android.server.backup.restore.FullRestoreEngine$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$backup$restore$RestorePolicy = new int[RestorePolicy.values().length];

        static {
            try {
                $SwitchMap$com$android$server$backup$restore$RestorePolicy[RestorePolicy.IGNORE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$backup$restore$RestorePolicy[RestorePolicy.ACCEPT_IF_APK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$backup$restore$RestorePolicy[RestorePolicy.ACCEPT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private void setUpPipes() throws IOException {
        synchronized (this.mPipesLock) {
            this.mPipes = ParcelFileDescriptor.createPipe();
            this.mPipesClosed = false;
        }
    }

    private void tearDownPipes() {
        synchronized (this.mPipesLock) {
            if (!this.mPipesClosed && this.mPipes != null) {
                try {
                    this.mPipes[0].close();
                    this.mPipes[1].close();
                    this.mPipesClosed = true;
                } catch (IOException e) {
                    Slog.w(BackupManagerService.TAG, "Couldn't close agent pipes", e);
                }
            }
        }
    }

    private void tearDownAgent(ApplicationInfo app, boolean doRestoreFinished) {
        if (this.mAgent != null) {
            if (doRestoreFinished) {
                try {
                    int token = this.mBackupManagerService.generateRandomIntegerToken();
                    long fullBackupAgentTimeoutMillis = this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis();
                    AdbRestoreFinishedLatch latch = new AdbRestoreFinishedLatch(this.mBackupManagerService, token);
                    this.mBackupManagerService.prepareOperationTimeout(token, fullBackupAgentTimeoutMillis, latch, 1);
                    if (this.mTargetApp.processName.equals("system")) {
                        new Thread(new AdbRestoreFinishedRunnable(this.mAgent, token, this.mBackupManagerService), "restore-sys-finished-runner").start();
                    } else {
                        this.mAgent.doRestoreFinished(token, this.mBackupManagerService.getBackupManagerBinder());
                    }
                    latch.await();
                } catch (RemoteException e) {
                    Slog.d(BackupManagerService.TAG, "Lost app trying to shut down");
                }
            }
            this.mBackupManagerService.tearDownAgentAndKill(app);
            this.mAgent = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void handleTimeout() {
        tearDownPipes();
        setResult(-2);
        setRunning(false);
    }

    private static boolean isRestorableFile(FileMetadata info) {
        if ("c".equals(info.domain)) {
            return false;
        }
        if (!"r".equals(info.domain) || !info.path.startsWith("no_backup/")) {
            return true;
        }
        return false;
    }

    private static boolean isCanonicalFilePath(String path) {
        if (path.contains("..") || path.contains("//")) {
            return false;
        }
        return true;
    }

    private boolean shouldForceClearAppDataOnFullRestore(String packageName) {
        String packageListString = Settings.Secure.getStringForUser(this.mBackupManagerService.getContext().getContentResolver(), "packages_to_clear_data_before_full_restore", this.mUserId);
        if (TextUtils.isEmpty(packageListString)) {
            return false;
        }
        return Arrays.asList(packageListString.split(";")).contains(packageName);
    }

    /* access modifiers changed from: package-private */
    public void sendOnRestorePackage(String name) {
        IFullBackupRestoreObserver iFullBackupRestoreObserver = this.mObserver;
        if (iFullBackupRestoreObserver != null) {
            try {
                iFullBackupRestoreObserver.onRestorePackage(name);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "full restore observer went away: restorePackage");
                this.mObserver = null;
            }
        }
    }
}
