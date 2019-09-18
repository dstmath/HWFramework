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
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.FileMetadata;
import com.android.server.backup.KeyValueAdbRestoreEngine;
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
    private final BackupManagerService mBackupManagerService;
    final byte[] mBuffer;
    private long mBytes;
    private final HashSet<String> mClearedPackages = new HashSet<>();
    private final RestoreDeleteObserver mDeleteObserver = new RestoreDeleteObserver();
    final int mEphemeralOpToken;
    private final HashMap<String, Signature[]> mManifestSignatures = new HashMap<>();
    final IBackupManagerMonitor mMonitor;
    private final BackupRestoreTask mMonitorTask;
    private FullBackupObbConnection mObbConnection = null;
    private IFullBackupRestoreObserver mObserver;
    final PackageInfo mOnlyPackage;
    private final HashMap<String, String> mPackageInstallers = new HashMap<>();
    private final HashMap<String, RestorePolicy> mPackagePolicies = new HashMap<>();
    private ParcelFileDescriptor[] mPipes = null;
    private ApplicationInfo mTargetApp;
    private byte[] mWidgetData = null;

    static /* synthetic */ long access$014(FullRestoreEngine x0, long x1) {
        long j = x0.mBytes + x1;
        x0.mBytes = j;
        return j;
    }

    public FullRestoreEngine(BackupManagerService backupManagerService, BackupRestoreTask monitorTask, IFullBackupRestoreObserver observer, IBackupManagerMonitor monitor, PackageInfo onlyPackage, boolean allowApks, boolean allowObbs, int ephemeralOpToken) {
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
    }

    public IBackupAgent getAgent() {
        return this.mAgent;
    }

    public byte[] getWidgetData() {
        return this.mWidgetData;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:236:?, code lost:
        r44.skipTarPadding(r3.size);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:?, code lost:
        r6 = r1.mBackupManagerService.waitUntilOperationComplete(r58);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x0540, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x0541, code lost:
        r7 = r58;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x0594, code lost:
        r0 = e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x024e A[Catch:{ IOException -> 0x01ed }] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x029c  */
    /* JADX WARNING: Removed duplicated region for block: B:206:0x04ca A[Catch:{ IOException -> 0x059f }] */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0545 A[Catch:{ IOException -> 0x0594 }] */
    /* JADX WARNING: Removed duplicated region for block: B:244:0x054d A[Catch:{ IOException -> 0x0594 }] */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x0597 A[Catch:{ IOException -> 0x05f0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x05aa A[Catch:{ IOException -> 0x05f0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:259:0x05b6 A[Catch:{ IOException -> 0x05f0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:284:0x0635  */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x0644  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0647  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x064a  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0213 A[Catch:{ NameNotFoundException | IOException -> 0x0246 }] */
    public boolean restoreOneFile(InputStream instream, boolean mustKillAgent, byte[] buffer, PackageInfo onlyPackage, boolean allowApks, int token, IBackupManagerMonitor monitor) {
        FileMetadata info;
        FileMetadata info2;
        boolean z;
        FileMetadata info3;
        TarBackupReader tarBackupReader;
        boolean okay;
        boolean okay2;
        String pkg;
        String pkg2;
        long timeout;
        TarBackupReader tarBackupReader2;
        String pkg3;
        long toCopy;
        boolean agentSuccess;
        boolean okay3;
        long toCopy2;
        boolean okay4;
        boolean agentSuccess2;
        boolean okay5;
        RestorePolicy restorePolicy;
        InputStream inputStream = instream;
        byte[] bArr = buffer;
        PackageInfo packageInfo = onlyPackage;
        if (!isRunning()) {
            Slog.w(BackupManagerService.TAG, "Restore engine used after halting");
            return false;
        }
        AnonymousClass1 r14 = new BytesReadListener() {
            public void onBytesRead(long bytesRead) {
                FullRestoreEngine.access$014(FullRestoreEngine.this, bytesRead);
            }
        };
        IBackupManagerMonitor iBackupManagerMonitor = monitor;
        TarBackupReader tarBackupReader3 = new TarBackupReader(inputStream, r14, iBackupManagerMonitor);
        try {
            FileMetadata info4 = tarBackupReader3.readTarHeaders();
            if (info4 != null) {
                String pkg4 = info4.packageName;
                if (!pkg4.equals(this.mAgentPackage)) {
                    if (packageInfo != null) {
                        try {
                            if (!pkg4.equals(packageInfo.packageName)) {
                                Slog.w(BackupManagerService.TAG, "Expected data for " + packageInfo + " but saw " + pkg4);
                                setResult(-3);
                                setRunning(false);
                                return false;
                            }
                        } catch (IOException e) {
                            e = e;
                            int i = token;
                            PackageInfo packageInfo2 = packageInfo;
                            AnonymousClass1 r43 = r14;
                            IBackupManagerMonitor iBackupManagerMonitor2 = iBackupManagerMonitor;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                            setResult(-3);
                            info = null;
                            info2 = info;
                            if (info2 == null) {
                            }
                            return info2 != null ? true : z;
                        }
                    }
                    if (!this.mPackagePolicies.containsKey(pkg4)) {
                        this.mPackagePolicies.put(pkg4, RestorePolicy.IGNORE);
                    }
                    if (this.mAgent != null) {
                        Slog.d(BackupManagerService.TAG, "Saw new package; finalizing old one");
                        tearDownPipes();
                        tearDownAgent(this.mTargetApp);
                        this.mTargetApp = null;
                        this.mAgentPackage = null;
                    }
                }
                if (info4.path.equals(BackupManagerService.BACKUP_MANIFEST_FILENAME)) {
                    try {
                        Signature[] signatures = tarBackupReader3.readAppManifestAndReturnSignatures(info4);
                        String pkg5 = pkg4;
                        FileMetadata info5 = info4;
                        RestorePolicy restorePolicy2 = tarBackupReader3.chooseRestorePolicy(this.mBackupManagerService.getPackageManager(), allowApks, info5, signatures, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class));
                        FileMetadata info6 = info5;
                        this.mManifestSignatures.put(info6.packageName, signatures);
                        this.mPackagePolicies.put(pkg5, restorePolicy2);
                        this.mPackageInstallers.put(pkg5, info6.installerPackageName);
                        tarBackupReader3.skipTarPadding(info6.size);
                        this.mObserver = FullBackupRestoreObserverUtils.sendOnRestorePackage(this.mObserver, pkg5);
                        int i2 = token;
                        info3 = info6;
                        PackageInfo packageInfo3 = packageInfo;
                        AnonymousClass1 r432 = r14;
                    } catch (IOException e2) {
                        e = e2;
                        int i3 = token;
                        PackageInfo packageInfo4 = packageInfo;
                        AnonymousClass1 r433 = r14;
                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                        setResult(-3);
                        info = null;
                        info2 = info;
                        if (info2 == null) {
                        }
                        return info2 != null ? true : z;
                    }
                } else {
                    String pkg6 = pkg4;
                    FileMetadata info7 = info4;
                    if (info7.path.equals(BackupManagerService.BACKUP_METADATA_FILENAME)) {
                        tarBackupReader3.readMetadata(info7);
                        this.mWidgetData = tarBackupReader3.getWidgetData();
                        IBackupManagerMonitor monitor2 = tarBackupReader3.getMonitor();
                        try {
                            tarBackupReader3.skipTarPadding(info7.size);
                            int i4 = token;
                            IBackupManagerMonitor iBackupManagerMonitor3 = monitor2;
                            info3 = info7;
                            PackageInfo packageInfo5 = packageInfo;
                            AnonymousClass1 r434 = r14;
                            info = info3;
                        } catch (IOException e3) {
                            e = e3;
                            int i5 = token;
                            IBackupManagerMonitor iBackupManagerMonitor4 = monitor2;
                            PackageInfo packageInfo6 = packageInfo;
                            AnonymousClass1 r435 = r14;
                            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                            setResult(-3);
                            info = null;
                            info2 = info;
                            if (info2 == null) {
                            }
                            return info2 != null ? true : z;
                        }
                        info2 = info;
                        if (info2 == null) {
                            tearDownPipes();
                            z = false;
                            setRunning(false);
                            if (mustKillAgent) {
                                tearDownAgent(this.mTargetApp);
                            }
                        } else {
                            z = false;
                        }
                        return info2 != null ? true : z;
                    }
                    boolean okay6 = true;
                    RestorePolicy policy = this.mPackagePolicies.get(pkg6);
                    switch (policy) {
                        case IGNORE:
                            info3 = info7;
                            RestorePolicy restorePolicy3 = policy;
                            tarBackupReader = tarBackupReader3;
                            okay6 = false;
                            break;
                        case ACCEPT_IF_APK:
                            try {
                                if (!info7.domain.equals("a")) {
                                    info3 = info7;
                                    RestorePolicy restorePolicy4 = policy;
                                    tarBackupReader = tarBackupReader3;
                                    this.mPackagePolicies.put(pkg6, RestorePolicy.IGNORE);
                                    okay6 = false;
                                    break;
                                } else {
                                    Slog.d(BackupManagerService.TAG, "APK file; installing");
                                    FileMetadata info8 = info7;
                                    RestorePolicy restorePolicy5 = policy;
                                    tarBackupReader = tarBackupReader3;
                                    try {
                                        boolean isSuccessfullyInstalled = RestoreUtils.installApk(inputStream, this.mBackupManagerService.getContext(), this.mDeleteObserver, this.mManifestSignatures, this.mPackagePolicies, info8, this.mPackageInstallers.get(pkg6), r14);
                                        HashMap<String, RestorePolicy> hashMap = this.mPackagePolicies;
                                        if (isSuccessfullyInstalled) {
                                            restorePolicy = RestorePolicy.ACCEPT;
                                        } else {
                                            restorePolicy = RestorePolicy.IGNORE;
                                        }
                                        hashMap.put(pkg6, restorePolicy);
                                        tarBackupReader.skipTarPadding(info8.size);
                                        return true;
                                    } catch (IOException e4) {
                                        e = e4;
                                        int i6 = token;
                                        PackageInfo packageInfo7 = packageInfo;
                                        AnonymousClass1 r436 = r14;
                                        TarBackupReader tarBackupReader4 = tarBackupReader;
                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                        setResult(-3);
                                        info = null;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    }
                                }
                            } catch (IOException e5) {
                                e = e5;
                                int i7 = token;
                                PackageInfo packageInfo8 = packageInfo;
                                AnonymousClass1 r437 = r14;
                                IBackupManagerMonitor iBackupManagerMonitor5 = monitor;
                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                setResult(-3);
                                info = null;
                                info2 = info;
                                if (info2 == null) {
                                }
                                return info2 != null ? true : z;
                            }
                            break;
                        case ACCEPT:
                            if (info7.domain.equals("a")) {
                                Slog.d(BackupManagerService.TAG, "apk present but ACCEPT");
                                okay6 = false;
                            }
                            info3 = info7;
                            tarBackupReader = tarBackupReader3;
                            break;
                        default:
                            info3 = info7;
                            RestorePolicy restorePolicy6 = policy;
                            tarBackupReader = tarBackupReader3;
                            try {
                                Slog.e(BackupManagerService.TAG, "Invalid policy from manifest");
                                okay6 = false;
                                this.mPackagePolicies.put(pkg6, RestorePolicy.IGNORE);
                                break;
                            } catch (IOException e6) {
                                e = e6;
                                int i8 = token;
                                PackageInfo packageInfo9 = packageInfo;
                                AnonymousClass1 r438 = r14;
                                TarBackupReader tarBackupReader5 = tarBackupReader;
                                IBackupManagerMonitor iBackupManagerMonitor6 = monitor;
                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                setResult(-3);
                                info = null;
                                info2 = info;
                                if (info2 == null) {
                                }
                                return info2 != null ? true : z;
                            }
                    }
                    if (isRestorableFile(info3)) {
                        if (!isCanonicalFilePath(info3.path)) {
                        }
                        okay = okay6;
                        if (okay && this.mAgent == null) {
                            this.mTargetApp = this.mBackupManagerService.getPackageManager().getApplicationInfo(pkg6, 0);
                            if (!this.mClearedPackages.contains(pkg6)) {
                                boolean forceClear = shouldForceClearAppDataOnFullRestore(this.mTargetApp.packageName);
                                if (this.mTargetApp.backupAgentName == null || forceClear) {
                                    Slog.d(BackupManagerService.TAG, "Clearing app data preparatory to full restore");
                                    this.mBackupManagerService.clearApplicationDataSynchronous(pkg6, true);
                                }
                                this.mClearedPackages.add(pkg6);
                            }
                            setUpPipes();
                            this.mAgent = this.mBackupManagerService.bindToAgentSynchronous(this.mTargetApp, 3);
                            this.mAgentPackage = pkg6;
                            if (this.mAgent == null) {
                                Slog.e(BackupManagerService.TAG, "Unable to create agent for " + pkg6);
                                okay = false;
                                tearDownPipes();
                                this.mPackagePolicies.put(pkg6, RestorePolicy.IGNORE);
                            }
                        }
                        if (okay && !pkg6.equals(this.mAgentPackage)) {
                            Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg6 + " but agent is for " + this.mAgentPackage);
                            okay = false;
                        }
                        if (!okay) {
                            long toCopy3 = info3.size;
                            boolean isSharedStorage = pkg6.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                            if (isSharedStorage) {
                                timeout = this.mAgentTimeoutParameters.getSharedBackupAgentTimeoutMillis();
                            } else {
                                timeout = this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis();
                            }
                            try {
                                this.mBackupManagerService.prepareOperationTimeout(token, timeout, this.mMonitorTask, 1);
                                if ("obb".equals(info3.domain)) {
                                    try {
                                        Slog.d(BackupManagerService.TAG, "Restoring OBB file for " + pkg6 + " : " + info3.path);
                                        okay5 = okay;
                                        try {
                                            agentSuccess2 = true;
                                        } catch (IOException e7) {
                                            toCopy = toCopy3;
                                            boolean z2 = isSharedStorage;
                                            AnonymousClass1 r439 = r14;
                                            tarBackupReader2 = tarBackupReader;
                                            pkg3 = pkg6;
                                            try {
                                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor7 = monitor;
                                                info = info3;
                                            } catch (IOException e8) {
                                                e = e8;
                                                int i9 = token;
                                                TarBackupReader tarBackupReader6 = tarBackupReader2;
                                                PackageInfo packageInfo10 = onlyPackage;
                                                IBackupManagerMonitor iBackupManagerMonitor8 = monitor;
                                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                                setResult(-3);
                                                info = null;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            }
                                            info2 = info;
                                            if (info2 == null) {
                                            }
                                            return info2 != null ? true : z;
                                        } catch (RemoteException e9) {
                                            toCopy = toCopy3;
                                            boolean z3 = isSharedStorage;
                                            AnonymousClass1 r4310 = r14;
                                            tarBackupReader2 = tarBackupReader;
                                            pkg3 = pkg6;
                                            try {
                                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor72 = monitor;
                                                info = info3;
                                            } catch (IOException e10) {
                                                e = e10;
                                                int i10 = token;
                                                TarBackupReader tarBackupReader7 = tarBackupReader2;
                                                PackageInfo packageInfo11 = onlyPackage;
                                                Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                                setResult(-3);
                                                info = null;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            }
                                            info2 = info;
                                            if (info2 == null) {
                                            }
                                            return info2 != null ? true : z;
                                        }
                                    } catch (IOException e11) {
                                        boolean z4 = okay;
                                        toCopy = toCopy3;
                                        boolean z5 = isSharedStorage;
                                        AnonymousClass1 r4311 = r14;
                                        tarBackupReader2 = tarBackupReader;
                                        pkg3 = pkg6;
                                        Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor722 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    } catch (RemoteException e12) {
                                        boolean z6 = okay;
                                        toCopy = toCopy3;
                                        boolean z7 = isSharedStorage;
                                        AnonymousClass1 r4312 = r14;
                                        tarBackupReader2 = tarBackupReader;
                                        pkg3 = pkg6;
                                        Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor7222 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    }
                                    try {
                                        toCopy = toCopy3;
                                        try {
                                            AnonymousClass1 r4313 = r14;
                                            tarBackupReader2 = tarBackupReader;
                                            try {
                                                boolean z8 = isSharedStorage;
                                                try {
                                                    this.mObbConnection.restoreObbFile(pkg6, this.mPipes[0], info3.size, info3.type, info3.path, info3.mode, info3.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                                } catch (IOException e13) {
                                                    pkg3 = pkg6;
                                                } catch (RemoteException e14) {
                                                    pkg3 = pkg6;
                                                    Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                    agentSuccess = false;
                                                    okay4 = false;
                                                    okay3 = okay4;
                                                    if (okay3) {
                                                    }
                                                    if (!agentSuccess) {
                                                    }
                                                    okay2 = okay3;
                                                    if (!okay2) {
                                                    }
                                                    IBackupManagerMonitor iBackupManagerMonitor72222 = monitor;
                                                    info = info3;
                                                    info2 = info;
                                                    if (info2 == null) {
                                                    }
                                                    return info2 != null ? true : z;
                                                }
                                            } catch (IOException e15) {
                                                boolean z9 = isSharedStorage;
                                                pkg3 = pkg6;
                                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor722222 = monitor;
                                                info = info3;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            } catch (RemoteException e16) {
                                                boolean z10 = isSharedStorage;
                                                pkg3 = pkg6;
                                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor7222222 = monitor;
                                                info = info3;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            }
                                        } catch (IOException e17) {
                                            boolean z11 = isSharedStorage;
                                            AnonymousClass1 r4314 = r14;
                                            tarBackupReader2 = tarBackupReader;
                                            pkg3 = pkg6;
                                            Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                            agentSuccess = false;
                                            okay4 = false;
                                            okay3 = okay4;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess) {
                                            }
                                            okay2 = okay3;
                                            if (!okay2) {
                                            }
                                            IBackupManagerMonitor iBackupManagerMonitor72222222 = monitor;
                                            info = info3;
                                            info2 = info;
                                            if (info2 == null) {
                                            }
                                            return info2 != null ? true : z;
                                        } catch (RemoteException e18) {
                                            boolean z12 = isSharedStorage;
                                            AnonymousClass1 r4315 = r14;
                                            tarBackupReader2 = tarBackupReader;
                                            pkg3 = pkg6;
                                            Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                            agentSuccess = false;
                                            okay4 = false;
                                            okay3 = okay4;
                                            if (okay3) {
                                            }
                                            if (!agentSuccess) {
                                            }
                                            okay2 = okay3;
                                            if (!okay2) {
                                            }
                                            IBackupManagerMonitor iBackupManagerMonitor722222222 = monitor;
                                            info = info3;
                                            info2 = info;
                                            if (info2 == null) {
                                            }
                                            return info2 != null ? true : z;
                                        }
                                    } catch (IOException e19) {
                                        toCopy = toCopy3;
                                        boolean z13 = isSharedStorage;
                                        AnonymousClass1 r4316 = r14;
                                        tarBackupReader2 = tarBackupReader;
                                        pkg3 = pkg6;
                                        Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor7222222222 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    } catch (RemoteException e20) {
                                        toCopy = toCopy3;
                                        boolean z14 = isSharedStorage;
                                        AnonymousClass1 r4317 = r14;
                                        tarBackupReader2 = tarBackupReader;
                                        pkg3 = pkg6;
                                        Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor72222222222 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    }
                                } else {
                                    okay5 = okay;
                                    agentSuccess2 = true;
                                    toCopy = toCopy3;
                                    boolean z15 = isSharedStorage;
                                    AnonymousClass1 r4318 = r14;
                                    tarBackupReader2 = tarBackupReader;
                                    try {
                                        if ("k".equals(info3.domain)) {
                                            Slog.d(BackupManagerService.TAG, "Restoring key-value file for " + pkg6 + " : " + info3.path);
                                            KeyValueAdbRestoreEngine keyValueAdbRestoreEngine = new KeyValueAdbRestoreEngine(this.mBackupManagerService, this.mBackupManagerService.getDataDir(), info3, this.mPipes[0], this.mAgent, token);
                                            new Thread(keyValueAdbRestoreEngine, "restore-key-value-runner").start();
                                        } else if (this.mTargetApp.processName.equals("system")) {
                                            Slog.d(BackupManagerService.TAG, "system process agent - spinning a thread");
                                            RestoreFileRunnable restoreFileRunnable = new RestoreFileRunnable(this.mBackupManagerService, this.mAgent, info3, this.mPipes[0], token);
                                            new Thread(restoreFileRunnable, "restore-sys-runner").start();
                                        } else {
                                            pkg3 = pkg6;
                                            try {
                                                this.mAgent.doRestoreFile(this.mPipes[0], info3.size, info3.type, info3.domain, info3.path, info3.mode, info3.mtime, token, this.mBackupManagerService.getBackupManagerBinder());
                                                okay3 = okay5;
                                                agentSuccess = agentSuccess2;
                                            } catch (IOException e21) {
                                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor722222222222 = monitor;
                                                info = info3;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            } catch (RemoteException e22) {
                                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                                agentSuccess = false;
                                                okay4 = false;
                                                okay3 = okay4;
                                                if (okay3) {
                                                }
                                                if (!agentSuccess) {
                                                }
                                                okay2 = okay3;
                                                if (!okay2) {
                                                }
                                                IBackupManagerMonitor iBackupManagerMonitor7222222222222 = monitor;
                                                info = info3;
                                                info2 = info;
                                                if (info2 == null) {
                                                }
                                                return info2 != null ? true : z;
                                            }
                                            if (okay3) {
                                                FileOutputStream pipe = new FileOutputStream(this.mPipes[1].getFileDescriptor());
                                                boolean pipeOkay = true;
                                                long toCopy4 = toCopy;
                                                while (true) {
                                                    if (toCopy4 > 0) {
                                                        int nRead = inputStream.read(bArr, 0, toCopy4 > ((long) bArr.length) ? bArr.length : (int) toCopy4);
                                                        if (nRead >= 0) {
                                                            toCopy2 = toCopy4;
                                                            this.mBytes += (long) nRead;
                                                        } else {
                                                            toCopy2 = toCopy4;
                                                        }
                                                        if (nRead > 0) {
                                                            long toCopy5 = toCopy2 - ((long) nRead);
                                                            if (pipeOkay) {
                                                                try {
                                                                    pipe.write(bArr, 0, nRead);
                                                                } catch (IOException e23) {
                                                                    IOException iOException = e23;
                                                                    Slog.e(BackupManagerService.TAG, "Failed to write to restore pipe: " + e23.getMessage());
                                                                    pipeOkay = false;
                                                                }
                                                            }
                                                            toCopy4 = toCopy5;
                                                        }
                                                    } else {
                                                        long j = toCopy4;
                                                    }
                                                }
                                            } else {
                                                int i11 = token;
                                                TarBackupReader tarBackupReader8 = tarBackupReader2;
                                                long j2 = toCopy;
                                            }
                                            if (!agentSuccess) {
                                                StringBuilder sb = new StringBuilder();
                                                sb.append("Agent failure restoring ");
                                                pkg = pkg3;
                                                sb.append(pkg);
                                                sb.append("; ending restore");
                                                Slog.w(BackupManagerService.TAG, sb.toString());
                                                this.mBackupManagerService.getBackupHandler().removeMessages(18);
                                                tearDownPipes();
                                                tearDownAgent(this.mTargetApp);
                                                this.mAgent = null;
                                                this.mPackagePolicies.put(pkg, RestorePolicy.IGNORE);
                                                if (onlyPackage != null) {
                                                    try {
                                                        setResult(-2);
                                                        setRunning(false);
                                                        return false;
                                                    } catch (IOException e24) {
                                                        e = e24;
                                                        Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
                                                        setResult(-3);
                                                        info = null;
                                                        info2 = info;
                                                        if (info2 == null) {
                                                        }
                                                        return info2 != null ? true : z;
                                                    }
                                                }
                                            } else {
                                                pkg = pkg3;
                                                PackageInfo packageInfo12 = onlyPackage;
                                            }
                                            okay2 = okay3;
                                        }
                                    } catch (IOException e25) {
                                        pkg3 = pkg6;
                                        Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor72222222222222 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    } catch (RemoteException e26) {
                                        pkg3 = pkg6;
                                        Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                        agentSuccess = false;
                                        okay4 = false;
                                        okay3 = okay4;
                                        if (okay3) {
                                        }
                                        if (!agentSuccess) {
                                        }
                                        okay2 = okay3;
                                        if (!okay2) {
                                        }
                                        IBackupManagerMonitor iBackupManagerMonitor722222222222222 = monitor;
                                        info = info3;
                                        info2 = info;
                                        if (info2 == null) {
                                        }
                                        return info2 != null ? true : z;
                                    }
                                }
                                pkg3 = pkg6;
                                okay3 = okay5;
                                agentSuccess = agentSuccess2;
                            } catch (IOException e27) {
                                boolean z16 = okay;
                                toCopy = toCopy3;
                                boolean z17 = isSharedStorage;
                                pkg3 = pkg6;
                                AnonymousClass1 r4319 = r14;
                                tarBackupReader2 = tarBackupReader;
                                Slog.d(BackupManagerService.TAG, "Couldn't establish restore");
                                agentSuccess = false;
                                okay4 = false;
                                okay3 = okay4;
                                if (okay3) {
                                }
                                if (!agentSuccess) {
                                }
                                okay2 = okay3;
                                if (!okay2) {
                                }
                                IBackupManagerMonitor iBackupManagerMonitor7222222222222222 = monitor;
                                info = info3;
                                info2 = info;
                                if (info2 == null) {
                                }
                                return info2 != null ? true : z;
                            } catch (RemoteException e28) {
                                boolean z18 = okay;
                                toCopy = toCopy3;
                                boolean z19 = isSharedStorage;
                                pkg3 = pkg6;
                                AnonymousClass1 r4320 = r14;
                                tarBackupReader2 = tarBackupReader;
                                Slog.e(BackupManagerService.TAG, "Agent crashed during full restore");
                                agentSuccess = false;
                                okay4 = false;
                                okay3 = okay4;
                                if (okay3) {
                                }
                                if (!agentSuccess) {
                                }
                                okay2 = okay3;
                                if (!okay2) {
                                }
                                IBackupManagerMonitor iBackupManagerMonitor72222222222222222 = monitor;
                                info = info3;
                                info2 = info;
                                if (info2 == null) {
                                }
                                return info2 != null ? true : z;
                            }
                            if (okay3) {
                            }
                            if (!agentSuccess) {
                            }
                            okay2 = okay3;
                        } else {
                            int i12 = token;
                            okay2 = okay;
                            PackageInfo packageInfo13 = packageInfo;
                            pkg = pkg6;
                            AnonymousClass1 r4321 = r14;
                            TarBackupReader tarBackupReader9 = tarBackupReader;
                        }
                        if (!okay2) {
                            long bytesToConsume = (info3.size + 511) & -512;
                            while (bytesToConsume > 0) {
                                long nRead2 = (long) inputStream.read(bArr, 0, bytesToConsume > ((long) bArr.length) ? bArr.length : (int) bytesToConsume);
                                if (nRead2 >= 0) {
                                    pkg2 = pkg;
                                    this.mBytes += nRead2;
                                } else {
                                    pkg2 = pkg;
                                }
                                if (nRead2 > 0) {
                                    bytesToConsume -= nRead2;
                                    pkg = pkg2;
                                }
                            }
                        }
                    }
                    okay6 = false;
                    okay = okay6;
                    try {
                        this.mTargetApp = this.mBackupManagerService.getPackageManager().getApplicationInfo(pkg6, 0);
                        if (!this.mClearedPackages.contains(pkg6)) {
                        }
                        setUpPipes();
                        this.mAgent = this.mBackupManagerService.bindToAgentSynchronous(this.mTargetApp, 3);
                        this.mAgentPackage = pkg6;
                    } catch (PackageManager.NameNotFoundException | IOException e29) {
                    }
                    if (this.mAgent == null) {
                    }
                    Slog.e(BackupManagerService.TAG, "Restoring data for " + pkg6 + " but agent is for " + this.mAgentPackage);
                    okay = false;
                    if (!okay) {
                    }
                    if (!okay2) {
                    }
                }
            } else {
                int i13 = token;
                info3 = info4;
                PackageInfo packageInfo14 = packageInfo;
                AnonymousClass1 r4322 = r14;
            }
            IBackupManagerMonitor iBackupManagerMonitor722222222222222222 = monitor;
            info = info3;
        } catch (IOException e30) {
            e = e30;
            int i14 = token;
            PackageInfo packageInfo15 = packageInfo;
            AnonymousClass1 r4323 = r14;
            IBackupManagerMonitor iBackupManagerMonitor9 = monitor;
            Slog.w(BackupManagerService.TAG, "io exception on restore socket read: " + e.getMessage());
            setResult(-3);
            info = null;
            info2 = info;
            if (info2 == null) {
            }
            return info2 != null ? true : z;
        }
        info2 = info;
        if (info2 == null) {
        }
        return info2 != null ? true : z;
    }

    private void setUpPipes() throws IOException {
        this.mPipes = ParcelFileDescriptor.createPipe();
    }

    private void tearDownPipes() {
        synchronized (this) {
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
    }

    private void tearDownAgent(ApplicationInfo app) {
        if (this.mAgent != null) {
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
        String packageListString = Settings.Secure.getString(this.mBackupManagerService.getContext().getContentResolver(), "packages_to_clear_data_before_full_restore");
        if (TextUtils.isEmpty(packageListString)) {
            return false;
        }
        return Arrays.asList(packageListString.split(";")).contains(packageName);
    }

    /* access modifiers changed from: package-private */
    public void sendOnRestorePackage(String name) {
        if (this.mObserver != null) {
            try {
                this.mObserver.onRestorePackage(name);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "full restore observer went away: restorePackage");
                this.mObserver = null;
            }
        }
    }
}
