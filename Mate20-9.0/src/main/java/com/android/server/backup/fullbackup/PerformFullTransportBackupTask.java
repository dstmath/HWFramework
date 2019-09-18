package com.android.server.backup.fullbackup;

import android.app.IBackupAgent;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.FullBackupJob;
import com.android.server.backup.TransportManager;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.internal.Operation;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.transport.TransportNotAvailableException;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.backup.utils.BackupObserverUtils;
import com.android.server.job.JobSchedulerShellCommand;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PerformFullTransportBackupTask extends FullBackupTask implements BackupRestoreTask {
    private static final String TAG = "PFTBT";
    /* access modifiers changed from: private */
    public BackupManagerService backupManagerService;
    /* access modifiers changed from: private */
    public final BackupAgentTimeoutParameters mAgentTimeoutParameters;
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
    private final OnTaskFinishedListener mListener;
    IBackupManagerMonitor mMonitor;
    ArrayList<PackageInfo> mPackages;
    private final TransportClient mTransportClient;
    boolean mUpdateSchedule;
    boolean mUserInitiated;

    class SinglePackageBackupPreflight implements BackupRestoreTask, FullBackupPreflight {
        private final int mCurrentOpToken;
        final CountDownLatch mLatch = new CountDownLatch(1);
        final long mQuota;
        final AtomicLong mResult = new AtomicLong(-1003);
        final TransportClient mTransportClient;
        private final int mTransportFlags;

        SinglePackageBackupPreflight(TransportClient transportClient, long quota, int currentOpToken, int transportFlags) {
            this.mTransportClient = transportClient;
            this.mQuota = quota;
            this.mCurrentOpToken = currentOpToken;
            this.mTransportFlags = transportFlags;
        }

        public int preflightFullBackup(PackageInfo pkg, IBackupAgent agent) {
            int result;
            long fullBackupAgentTimeoutMillis = PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis();
            try {
                PerformFullTransportBackupTask.this.backupManagerService.prepareOperationTimeout(this.mCurrentOpToken, fullBackupAgentTimeoutMillis, this, 0);
                PerformFullTransportBackupTask.this.backupManagerService.addBackupTrace("preflighting");
                agent.doMeasureFullBackup(this.mQuota, this.mCurrentOpToken, PerformFullTransportBackupTask.this.backupManagerService.getBackupManagerBinder(), this.mTransportFlags);
                this.mLatch.await(fullBackupAgentTimeoutMillis, TimeUnit.MILLISECONDS);
                long totalSize = this.mResult.get();
                if (totalSize < 0) {
                    return (int) totalSize;
                }
                result = this.mTransportClient.connectOrThrow("PFTBT$SPBP.preflightFullBackup()").checkFullBackupSize(totalSize);
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
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        public void handleCancel(boolean cancelAll) {
            this.mResult.set(-1003);
            this.mLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        public long getExpectedSizeOrErrorCode() {
            try {
                this.mLatch.await(PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
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
        private final int mTransportFlags;
        final /* synthetic */ PerformFullTransportBackupTask this$0;

        SinglePackageBackupRunner(PerformFullTransportBackupTask this$02, ParcelFileDescriptor output, PackageInfo target, TransportClient transportClient, long quota, int currentOpToken, int transportFlags) throws IOException {
            PerformFullTransportBackupTask performFullTransportBackupTask = this$02;
            this.this$0 = performFullTransportBackupTask;
            this.mOutput = ParcelFileDescriptor.dup(output.getFileDescriptor());
            this.mTarget = target;
            this.mCurrentOpToken = currentOpToken;
            this.mEphemeralToken = performFullTransportBackupTask.backupManagerService.generateRandomIntegerToken();
            SinglePackageBackupPreflight singlePackageBackupPreflight = new SinglePackageBackupPreflight(transportClient, quota, this.mEphemeralToken, transportFlags);
            this.mPreflight = singlePackageBackupPreflight;
            this.mQuota = quota;
            this.mTransportFlags = transportFlags;
            registerTask();
        }

        /* access modifiers changed from: package-private */
        public void registerTask() {
            synchronized (this.this$0.backupManagerService.getCurrentOpLock()) {
                this.this$0.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 0));
            }
        }

        /* access modifiers changed from: package-private */
        public void unregisterTask() {
            synchronized (this.this$0.backupManagerService.getCurrentOpLock()) {
                this.this$0.backupManagerService.getCurrentOperations().remove(this.mCurrentOpToken);
            }
        }

        public void run() {
            String str;
            FullBackupEngine fullBackupEngine = new FullBackupEngine(this.this$0.backupManagerService, new FileOutputStream(this.mOutput.getFileDescriptor()), this.mPreflight, this.mTarget, false, this, this.mQuota, this.mCurrentOpToken, this.mTransportFlags);
            this.mEngine = fullBackupEngine;
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
                }
            } catch (Exception e2) {
                try {
                    Slog.e(PerformFullTransportBackupTask.TAG, "Exception during full package backup of " + this.mTarget.packageName);
                } finally {
                    unregisterTask();
                    this.mBackupLatch.countDown();
                    try {
                        this.mOutput.close();
                    } catch (IOException e3) {
                        str = "Error closing transport pipe in runner";
                        Slog.w(PerformFullTransportBackupTask.TAG, str);
                    }
                }
            } catch (Throwable th) {
                this.mPreflightLatch.countDown();
                throw th;
            }
        }

        public void sendQuotaExceeded(long backupDataBytes, long quotaBytes) {
            this.mEngine.sendQuotaExceeded(backupDataBytes, quotaBytes);
        }

        /* access modifiers changed from: package-private */
        public long getPreflightResultBlocking() {
            try {
                this.mPreflightLatch.await(this.this$0.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
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

        /* access modifiers changed from: package-private */
        public int getBackupResultBlocking() {
            try {
                this.mBackupLatch.await(this.this$0.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
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
            Slog.w(PerformFullTransportBackupTask.TAG, "Full backup cancel of " + this.mTarget.packageName);
            this.this$0.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.this$0.mMonitor, 4, this.this$0.mCurrentPackage, 2, null);
            this.mIsCancelled = true;
            this.this$0.backupManagerService.handleCancel(this.mEphemeralToken, cancelAll);
            this.this$0.backupManagerService.tearDownAgentAndKill(this.mTarget.applicationInfo);
            this.mPreflightLatch.countDown();
            this.mBackupLatch.countDown();
            this.this$0.backupManagerService.removeOperation(this.mCurrentOpToken);
        }
    }

    public static PerformFullTransportBackupTask newWithCurrentTransport(BackupManagerService backupManagerService2, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, boolean userInitiated, String caller) {
        TransportManager transportManager = backupManagerService2.getTransportManager();
        TransportClient transportClient = transportManager.getCurrentTransportClient(caller);
        PerformFullTransportBackupTask performFullTransportBackupTask = new PerformFullTransportBackupTask(backupManagerService2, transportClient, observer, whichPackages, updateSchedule, runningJob, latch, backupObserver, monitor, new OnTaskFinishedListener(transportClient) {
            private final /* synthetic */ TransportClient f$1;

            {
                this.f$1 = r2;
            }

            public final void onFinished(String str) {
                TransportManager.this.disposeOfTransportClient(this.f$1, str);
            }
        }, userInitiated);
        return performFullTransportBackupTask;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PerformFullTransportBackupTask(BackupManagerService backupManagerService2, TransportClient transportClient, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, OnTaskFinishedListener listener, boolean userInitiated) {
        super(observer);
        String[] strArr = whichPackages;
        this.backupManagerService = backupManagerService2;
        this.mTransportClient = transportClient;
        this.mUpdateSchedule = updateSchedule;
        this.mLatch = latch;
        this.mJob = runningJob;
        this.mPackages = new ArrayList<>(strArr.length);
        this.mBackupObserver = backupObserver;
        this.mMonitor = monitor;
        this.mListener = listener != null ? listener : OnTaskFinishedListener.NOP;
        this.mUserInitiated = userInitiated;
        this.mCurrentOpToken = backupManagerService2.generateRandomIntegerToken();
        this.mBackupRunnerOpToken = backupManagerService2.generateRandomIntegerToken();
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        if (backupManagerService2.isBackupOperationInProgress()) {
            Slog.d(TAG, "Skipping full backup. A backup is already in progress.");
            this.mCancelAll = true;
            return;
        }
        registerTask();
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            String pkg = strArr[i];
            try {
                PackageManager pm = backupManagerService2.getPackageManager();
                PackageInfo info = pm.getPackageInfo(pkg, 134217728);
                this.mCurrentPackage = info;
                PackageManager packageManager = pm;
                if (!AppBackupUtils.appIsEligibleForBackup(info.applicationInfo, pm)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 9, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else if (!AppBackupUtils.appGetsFullBackup(info)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 10, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else if (AppBackupUtils.appIsStopped(info.applicationInfo)) {
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 11, this.mCurrentPackage, 3, null);
                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, pkg, -2001);
                } else {
                    this.mPackages.add(info);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.i(TAG, "Requested package " + pkg + " not found; ignoring");
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 12, this.mCurrentPackage, 3, null);
            }
            i++;
            strArr = whichPackages;
            IFullBackupRestoreObserver iFullBackupRestoreObserver = observer;
            BackupManagerService backupManagerService3 = backupManagerService2;
            TransportClient transportClient2 = transportClient;
            boolean z = updateSchedule;
        }
    }

    private void registerTask() {
        synchronized (this.backupManagerService.getCurrentOpLock()) {
            Slog.d(TAG, "backupmanager pftbt token=" + Integer.toHexString(this.mCurrentOpToken));
            this.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 2));
        }
    }

    public void unregisterTask() {
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }

    public void execute() {
    }

    public void handleCancel(boolean cancelAll) {
        synchronized (this.mCancelLock) {
            if (!cancelAll) {
                try {
                    Slog.wtf(TAG, "Expected cancelAll to be true.");
                } catch (RemoteException | TransportNotAvailableException e) {
                    Slog.w(TAG, "Error calling cancelFullBackup() on transport: " + e);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (this.mCancelAll) {
                Slog.d(TAG, "Ignoring duplicate cancel call.");
                return;
            }
            this.mCancelAll = true;
            if (this.mIsDoingBackup) {
                this.backupManagerService.handleCancel(this.mBackupRunnerOpToken, cancelAll);
                this.mTransportClient.getConnectedTransport("PFTBT.handleCancel()").cancelFullBackup();
            }
        }
    }

    public void operationComplete(long result) {
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x029d, code lost:
        r37 = r2;
        r36 = r3;
        r2 = r34 + ((long) r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x02a6, code lost:
        if (r10.mBackupObserver == null) goto L_0x02b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02aa, code lost:
        if (r14 <= 0) goto L_0x02b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02ac, code lost:
        r12 = r30;
        com.android.server.backup.utils.BackupObserverUtils.sendBackupOnUpdate(r10.mBackupObserver, r12, new android.app.backup.BackupProgress(r14, r2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x02b9, code lost:
        r12 = r30;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02bb, code lost:
        r38 = r4;
        r3 = r2;
        r2 = r37;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x035e, code lost:
        if (r39 != 0) goto L_0x0364;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x0360, code lost:
        if (r2 == 0) goto L_0x0364;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0362, code lost:
        r0 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x0364, code lost:
        r0 = r39;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x0366, code lost:
        if (r0 == 0) goto L_0x0386;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:?, code lost:
        android.util.Slog.e(TAG, "Error " + r0 + " backing up " + r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x038a, code lost:
        r42 = r9.requestFullBackupTime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:?, code lost:
        r8 = new java.lang.StringBuilder();
        r8.append("Transport suggested backoff=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x0398, code lost:
        r46 = r2;
        r44 = r3;
        r2 = r42;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:?, code lost:
        r8.append(r2);
        android.util.Slog.i(TAG, r8.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x03a8, code lost:
        r3 = r2;
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x03ab, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x03ac, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x03af, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x03b0, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x03b3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x03b4, code lost:
        r3 = r42;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x03b9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:182:0x03ba, code lost:
        r3 = r42;
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x04bc, code lost:
        com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r10.mBackupObserver, r12, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
        android.util.Slog.w(TAG, "Transport failed; aborting backup: " + r2);
        android.util.EventLog.writeEvent(com.android.server.EventLogTags.FULL_BACKUP_TRANSPORT_FAILURE, new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x04e1, code lost:
        r8 = com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:?, code lost:
        r10.backupManagerService.tearDownAgentAndKill(r5.applicationInfo);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x04ec, code lost:
        if (r10.mCancelAll == false) goto L_0x04f0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x04ee, code lost:
        r8 = -2003;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x04f0, code lost:
        android.util.Slog.i(TAG, "Full backup completed with status: " + r11);
        com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r10.mBackupObserver, r8);
        cleanUpPipes(r13);
        cleanUpPipes(r1);
        unregisterTask();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x0517, code lost:
        if (r10.mJob == null) goto L_0x051e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x0519, code lost:
        r10.mJob.finishBackupPass();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x051e, code lost:
        r14 = r10.backupManagerService.getQueueLock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x0524, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:?, code lost:
        r10.backupManagerService.setRunningFullBackupTask(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x052b, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x052c, code lost:
        r10.mListener.onFinished("PFTBT.run()");
        r10.mLatch.countDown();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x053a, code lost:
        if (r10.mUpdateSchedule == false) goto L_0x0541;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x053c, code lost:
        r10.backupManagerService.scheduleNextFullBackupJob(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x0541, code lost:
        android.util.Slog.i(TAG, "Full data backup pass finished.");
        r10.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0551, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0555, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0556, code lost:
        r27 = -1000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x055a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:295:0x068d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x077c  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0780  */
    /* JADX WARNING: Removed duplicated region for block: B:340:0x07a9  */
    /* JADX WARNING: Removed duplicated region for block: B:343:0x07b5 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x07cc  */
    /* JADX WARNING: Removed duplicated region for block: B:359:0x07ef  */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x081b  */
    /* JADX WARNING: Removed duplicated region for block: B:365:0x0827 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:370:0x083e  */
    public void run() {
        int backupRunStatus;
        ParcelFileDescriptor[] transportPipes;
        int backupRunStatus2;
        int monitoringEvent;
        int backupRunStatus3;
        long backoff;
        int backupRunStatus4;
        int backupRunStatus5;
        long backoff2;
        PackageInfo currentPackage;
        String packageName;
        long backoff3;
        Object obj;
        String packageName2;
        int i;
        PackageInfo currentPackage2;
        int N;
        char c;
        byte[] buffer;
        long quota;
        byte[] buffer2;
        String packageName3;
        IBackupTransport transport;
        int backupPackageStatus;
        boolean z;
        PackageInfo currentPackage3;
        int backupPackageStatus2;
        long totalRead;
        int nRead;
        FileOutputStream out;
        FileInputStream in;
        long backoff4;
        int i2;
        PackageInfo currentPackage4;
        ParcelFileDescriptor[] enginePipes;
        int N2;
        ParcelFileDescriptor[] enginePipes2 = null;
        ParcelFileDescriptor[] transportPipes2 = null;
        long backoff5 = 0;
        char c2 = 0;
        int backupRunStatus6 = 0;
        SinglePackageBackupRunner singlePackageBackupRunner = null;
        try {
            if (!this.backupManagerService.isEnabled()) {
                backupRunStatus3 = 0;
            } else if (!this.backupManagerService.isProvisioned()) {
                backupRunStatus3 = 0;
            } else {
                IBackupTransport transport2 = this.mTransportClient.connect("PFTBT.run()");
                if (transport2 == null) {
                    try {
                        Slog.w(TAG, "Transport not present; full data backup not performed");
                        backupRunStatus6 = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 15, this.mCurrentPackage, 1, null);
                        if (this.mCancelAll) {
                            backupRunStatus6 = -2003;
                        }
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus6);
                        cleanUpPipes(null);
                        cleanUpPipes(null);
                        unregisterTask();
                        if (this.mJob != null) {
                            this.mJob.finishBackupPass();
                        }
                        synchronized (this.backupManagerService.getQueueLock()) {
                            this.backupManagerService.setRunningFullBackupTask(null);
                        }
                        this.mListener.onFinished("PFTBT.run()");
                        this.mLatch.countDown();
                        if (this.mUpdateSchedule) {
                            this.backupManagerService.scheduleNextFullBackupJob(0);
                        }
                        Slog.i(TAG, "Full data backup pass finished.");
                        this.backupManagerService.getWakelock().release();
                        return;
                    } catch (Exception e) {
                        e = e;
                        int i3 = backupRunStatus6;
                        try {
                            Slog.w(TAG, "Exception trying full transport backup", e);
                            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                            if (this.mCancelAll) {
                            }
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                            cleanUpPipes(transportPipes2);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            ParcelFileDescriptor[] parcelFileDescriptorArr = transportPipes2;
                        } catch (Throwable th) {
                            th = th;
                            transportPipes = transportPipes2;
                            backupRunStatus = -1000;
                            if (this.mCancelAll) {
                                backupRunStatus = -2003;
                            }
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                            cleanUpPipes(transportPipes);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                                this.mJob.finishBackupPass();
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                                this.backupManagerService.setRunningFullBackupTask(null);
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                                this.backupManagerService.scheduleNextFullBackupJob(backoff5);
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            throw th;
                        }
                    }
                } else {
                    int N3 = this.mPackages.size();
                    byte[] buffer3 = new byte[8192];
                    int i4 = 0;
                    while (true) {
                        int i5 = i4;
                        if (i5 >= N3) {
                            backoff = backoff5;
                            backupRunStatus4 = backupRunStatus6;
                            transportPipes = transportPipes2;
                            break;
                        }
                        try {
                            this.mBackupRunner = singlePackageBackupRunner;
                            currentPackage = this.mPackages.get(i5);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Initiating full-data transport backup of ");
                            packageName = currentPackage.packageName;
                            sb.append(packageName);
                            sb.append(" token: ");
                            sb.append(this.mCurrentOpToken);
                            Slog.i(TAG, sb.toString());
                            EventLog.writeEvent(EventLogTags.FULL_BACKUP_PACKAGE, packageName);
                            transportPipes = ParcelFileDescriptor.createPipe();
                        } catch (Exception e2) {
                            e = e2;
                            long j = backoff5;
                            int i6 = backupRunStatus6;
                            Slog.w(TAG, "Exception trying full transport backup", e);
                            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                            cleanUpPipes(transportPipes2);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                                this.mJob.finishBackupPass();
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                                this.backupManagerService.setRunningFullBackupTask(null);
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                                this.backupManagerService.scheduleNextFullBackupJob(backoff5);
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            ParcelFileDescriptor[] parcelFileDescriptorArr2 = transportPipes2;
                        } catch (Throwable th2) {
                            th = th2;
                            long j2 = backoff5;
                            backupRunStatus = backupRunStatus6;
                            transportPipes = transportPipes2;
                            if (this.mCancelAll) {
                            }
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                            cleanUpPipes(transportPipes);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            throw th;
                        }
                        try {
                            boolean z2 = this.mUserInitiated;
                            Object obj2 = this.mCancelLock;
                            synchronized (obj2) {
                                try {
                                    if (this.mCancelAll) {
                                        try {
                                            break;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            obj = obj2;
                                            backoff3 = backoff5;
                                            byte[] bArr = buffer3;
                                            int i7 = i5;
                                            PackageInfo packageInfo = currentPackage;
                                            int i8 = N3;
                                            boolean z3 = z2;
                                            backupRunStatus = backupRunStatus6;
                                            String str = packageName;
                                            IBackupTransport iBackupTransport = transport2;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                }
                                            }
                                            throw th;
                                        }
                                    } else {
                                        int backupPackageStatus3 = transport2.performFullBackup(currentPackage, transportPipes[c2], z2 ? 1 : 0);
                                        if (backupPackageStatus3 == 0) {
                                            try {
                                                backoff4 = backoff5;
                                                try {
                                                    i2 = i5;
                                                    currentPackage4 = currentPackage;
                                                    quota = transport2.getBackupQuota(currentPackage.packageName, true);
                                                    try {
                                                        enginePipes = ParcelFileDescriptor.createPipe();
                                                        try {
                                                            N2 = N3;
                                                        } catch (Throwable th5) {
                                                            th = th5;
                                                            obj = obj2;
                                                            boolean z4 = z2;
                                                            int i9 = i2;
                                                            backupRunStatus = backupRunStatus6;
                                                            backoff3 = backoff4;
                                                            int i10 = N3;
                                                            byte[] bArr2 = buffer3;
                                                            long j3 = quota;
                                                            String str2 = packageName;
                                                            IBackupTransport iBackupTransport2 = transport2;
                                                            enginePipes2 = enginePipes;
                                                            PackageInfo packageInfo2 = currentPackage4;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        obj = obj2;
                                                        boolean z5 = z2;
                                                        int i11 = i2;
                                                        backupRunStatus = backupRunStatus6;
                                                        backoff3 = backoff4;
                                                        int i12 = N3;
                                                        byte[] bArr3 = buffer3;
                                                        long j4 = quota;
                                                        String str3 = packageName;
                                                        IBackupTransport iBackupTransport3 = transport2;
                                                        PackageInfo packageInfo22 = currentPackage4;
                                                        while (true) {
                                                            break;
                                                        }
                                                        throw th;
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    obj = obj2;
                                                    int i13 = i5;
                                                    boolean z6 = z2;
                                                    backupRunStatus = backupRunStatus6;
                                                    backoff3 = backoff4;
                                                    int i14 = N3;
                                                    byte[] bArr4 = buffer3;
                                                    PackageInfo packageInfo3 = currentPackage;
                                                    String str4 = packageName;
                                                    IBackupTransport iBackupTransport4 = transport2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } catch (Throwable th8) {
                                                th = th8;
                                                obj = obj2;
                                                backoff3 = backoff5;
                                                int i15 = i5;
                                                int i16 = N3;
                                                boolean z7 = z2;
                                                backupRunStatus = backupRunStatus6;
                                                byte[] bArr5 = buffer3;
                                                PackageInfo packageInfo4 = currentPackage;
                                                String str5 = packageName;
                                                IBackupTransport iBackupTransport5 = transport2;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                            try {
                                                r1 = r1;
                                                obj = obj2;
                                                backupRunStatus = backupRunStatus6;
                                                backoff3 = backoff4;
                                                SinglePackageBackupRunner singlePackageBackupRunner2 = r1;
                                                currentPackage2 = currentPackage4;
                                                i = i2;
                                                buffer = buffer3;
                                                N = N2;
                                                packageName2 = packageName;
                                                c = 1;
                                                boolean z8 = z2;
                                                try {
                                                    SinglePackageBackupRunner singlePackageBackupRunner3 = new SinglePackageBackupRunner(this, enginePipes[1], currentPackage4, this.mTransportClient, quota, this.mBackupRunnerOpToken, transport2.getTransportFlags());
                                                    this.mBackupRunner = singlePackageBackupRunner2;
                                                    enginePipes[1].close();
                                                    enginePipes[1] = null;
                                                    this.mIsDoingBackup = true;
                                                    enginePipes2 = enginePipes;
                                                } catch (Throwable th9) {
                                                    th = th9;
                                                    long j5 = quota;
                                                    byte[] bArr6 = buffer;
                                                    IBackupTransport iBackupTransport6 = transport2;
                                                    enginePipes2 = enginePipes;
                                                    PackageInfo packageInfo5 = currentPackage2;
                                                    String str6 = packageName2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th;
                                                }
                                            } catch (Throwable th10) {
                                                th = th10;
                                                obj = obj2;
                                                boolean z9 = z2;
                                                int i17 = i2;
                                                backupRunStatus = backupRunStatus6;
                                                backoff3 = backoff4;
                                                int i18 = N2;
                                                byte[] bArr7 = buffer3;
                                                long j6 = quota;
                                                String str7 = packageName;
                                                IBackupTransport iBackupTransport7 = transport2;
                                                enginePipes2 = enginePipes;
                                                PackageInfo packageInfo6 = currentPackage4;
                                                while (true) {
                                                    break;
                                                }
                                                throw th;
                                            }
                                        } else {
                                            obj = obj2;
                                            backoff3 = backoff5;
                                            buffer = buffer3;
                                            i = i5;
                                            currentPackage2 = currentPackage;
                                            N = N3;
                                            boolean z10 = z2;
                                            backupRunStatus = backupRunStatus6;
                                            packageName2 = packageName;
                                            c = 1;
                                            quota = Long.MAX_VALUE;
                                        }
                                        try {
                                            if (backupPackageStatus3 == 0) {
                                                try {
                                                    transportPipes[0].close();
                                                    transportPipes[0] = null;
                                                    new Thread(this.mBackupRunner, "package-backup-bridge").start();
                                                    FileInputStream in2 = new FileInputStream(enginePipes2[0].getFileDescriptor());
                                                    FileOutputStream out2 = new FileOutputStream(transportPipes[c].getFileDescriptor());
                                                    IBackupTransport transport3 = transport2;
                                                    long preflightResult = this.mBackupRunner.getPreflightResultBlocking();
                                                    if (preflightResult < 0) {
                                                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 16, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_PREFLIGHT_ERROR", preflightResult));
                                                        backupPackageStatus2 = (int) preflightResult;
                                                        FileInputStream fileInputStream = in2;
                                                        FileOutputStream fileOutputStream = out2;
                                                        buffer2 = buffer;
                                                        packageName3 = packageName2;
                                                        transport = transport3;
                                                        totalRead = 0;
                                                    } else {
                                                        long totalRead2 = 0;
                                                        int backupPackageStatus4 = backupPackageStatus3;
                                                        while (true) {
                                                            nRead = in2.read(buffer);
                                                            if (nRead > 0) {
                                                                out2.write(buffer, 0, nRead);
                                                                synchronized (this.mCancelLock) {
                                                                    try {
                                                                        if (!this.mCancelAll) {
                                                                            transport = transport3;
                                                                            try {
                                                                                backupPackageStatus4 = transport.sendBackupData(nRead);
                                                                            } catch (Throwable th11) {
                                                                                th = th11;
                                                                                FileInputStream fileInputStream2 = in2;
                                                                                String str8 = packageName2;
                                                                                while (true) {
                                                                                    try {
                                                                                        break;
                                                                                    } catch (Throwable th12) {
                                                                                        th = th12;
                                                                                    }
                                                                                }
                                                                                throw th;
                                                                            }
                                                                        } else {
                                                                            transport = transport3;
                                                                        }
                                                                        try {
                                                                        } catch (Throwable th13) {
                                                                            th = th13;
                                                                            int i19 = backupPackageStatus4;
                                                                            FileInputStream fileInputStream3 = in2;
                                                                            String str9 = packageName2;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } catch (Throwable th14) {
                                                                        th = th14;
                                                                        FileInputStream fileInputStream4 = in2;
                                                                        String str10 = packageName2;
                                                                        IBackupTransport iBackupTransport8 = transport3;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        throw th;
                                                                    }
                                                                }
                                                            } else {
                                                                in = in2;
                                                                packageName3 = packageName2;
                                                                transport = transport3;
                                                                out = out2;
                                                                totalRead = totalRead2;
                                                            }
                                                            if (nRead <= 0) {
                                                                break;
                                                            } else if (backupPackageStatus4 != 0) {
                                                                break;
                                                            } else {
                                                                totalRead2 = totalRead;
                                                                int i20 = nRead;
                                                                transport3 = transport;
                                                                packageName2 = packageName3;
                                                                in2 = in;
                                                                out2 = out;
                                                            }
                                                        }
                                                        if (backupPackageStatus4 == -1005) {
                                                            StringBuilder sb2 = new StringBuilder();
                                                            backupPackageStatus2 = backupPackageStatus4;
                                                            sb2.append("Package hit quota limit in-flight ");
                                                            sb2.append(packageName3);
                                                            sb2.append(": ");
                                                            sb2.append(totalRead);
                                                            sb2.append(" of ");
                                                            sb2.append(quota);
                                                            Slog.w(TAG, sb2.toString());
                                                            int i21 = nRead;
                                                            buffer2 = buffer;
                                                            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 18, this.mCurrentPackage, 1, null);
                                                            this.mBackupRunner.sendQuotaExceeded(totalRead, quota);
                                                        } else {
                                                            backupPackageStatus2 = backupPackageStatus4;
                                                            buffer2 = buffer;
                                                        }
                                                    }
                                                    int backupRunnerResult = this.mBackupRunner.getBackupResultBlocking();
                                                    synchronized (this.mCancelLock) {
                                                        try {
                                                            this.mIsDoingBackup = false;
                                                            if (!this.mCancelAll) {
                                                                if (backupRunnerResult == 0) {
                                                                    try {
                                                                        int finishResult = transport.finishBackup();
                                                                        if (backupPackageStatus2 == 0) {
                                                                            backupPackageStatus2 = finishResult;
                                                                        }
                                                                    } catch (Throwable th15) {
                                                                        th = th15;
                                                                        int i22 = backupRunnerResult;
                                                                        long j7 = totalRead;
                                                                        while (true) {
                                                                            try {
                                                                                break;
                                                                            } catch (Throwable th16) {
                                                                                th = th16;
                                                                            }
                                                                        }
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    transport.cancelFullBackup();
                                                                }
                                                            }
                                                        } catch (Throwable th17) {
                                                            th = th17;
                                                            int i23 = backupRunnerResult;
                                                            long j8 = totalRead;
                                                            while (true) {
                                                                break;
                                                            }
                                                            throw th;
                                                        }
                                                    }
                                                } catch (Exception e3) {
                                                    e = e3;
                                                    transportPipes2 = transportPipes;
                                                    backoff5 = backoff3;
                                                } catch (Throwable th18) {
                                                    th = th18;
                                                    backoff5 = backoff3;
                                                    if (this.mCancelAll) {
                                                    }
                                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                                                    cleanUpPipes(transportPipes);
                                                    cleanUpPipes(enginePipes2);
                                                    unregisterTask();
                                                    if (this.mJob != null) {
                                                    }
                                                    synchronized (this.backupManagerService.getQueueLock()) {
                                                    }
                                                    this.mListener.onFinished("PFTBT.run()");
                                                    this.mLatch.countDown();
                                                    if (this.mUpdateSchedule) {
                                                    }
                                                    Slog.i(TAG, "Full data backup pass finished.");
                                                    this.backupManagerService.getWakelock().release();
                                                    throw th;
                                                }
                                            } else {
                                                buffer2 = buffer;
                                                transport = transport2;
                                                packageName3 = packageName2;
                                                backupPackageStatus = backupPackageStatus3;
                                                backoff5 = backoff3;
                                            }
                                            try {
                                                if (this.mUpdateSchedule != 0) {
                                                    this.backupManagerService.enqueueFullBackup(packageName3, System.currentTimeMillis());
                                                }
                                                if (backupPackageStatus == -1002) {
                                                    BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                                                    Slog.i(TAG, "Transport rejected backup of " + packageName3 + ", skipping");
                                                    z = true;
                                                    EventLog.writeEvent(EventLogTags.FULL_BACKUP_AGENT_FAILURE, new Object[]{packageName3, "transport rejected"});
                                                    if (this.mBackupRunner != null) {
                                                        currentPackage3 = currentPackage2;
                                                        this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                                    } else {
                                                        currentPackage3 = currentPackage2;
                                                    }
                                                } else {
                                                    currentPackage3 = currentPackage2;
                                                    z = true;
                                                    if (backupPackageStatus == -1005) {
                                                        BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -1005);
                                                        Slog.i(TAG, "Transport quota exceeded for package: " + packageName3);
                                                        EventLog.writeEvent(EventLogTags.FULL_BACKUP_QUOTA_EXCEEDED, packageName3);
                                                        this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                                    } else if (backupPackageStatus == -1003) {
                                                        BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -1003);
                                                        Slog.w(TAG, "Application failure for package: " + packageName3);
                                                        EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, packageName3);
                                                        this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                                    } else if (backupPackageStatus == -2003) {
                                                        BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -2003);
                                                        Slog.w(TAG, "Backup cancelled. package=" + packageName3 + ", cancelAll=" + this.mCancelAll);
                                                        EventLog.writeEvent(EventLogTags.FULL_BACKUP_CANCELLED, packageName3);
                                                        this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                                    } else if (backupPackageStatus != 0) {
                                                        break;
                                                    } else {
                                                        BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, 0);
                                                        EventLog.writeEvent(EventLogTags.FULL_BACKUP_SUCCESS, packageName3);
                                                        this.backupManagerService.logBackupComplete(packageName3);
                                                    }
                                                }
                                                cleanUpPipes(transportPipes);
                                                cleanUpPipes(enginePipes2);
                                                if (currentPackage3.applicationInfo != null) {
                                                    Slog.i(TAG, "Unbinding agent in " + packageName3);
                                                    this.backupManagerService.addBackupTrace("unbinding " + packageName3);
                                                    try {
                                                        this.backupManagerService.getActivityManager().unbindBackupAgent(currentPackage3.applicationInfo);
                                                    } catch (RemoteException e4) {
                                                    }
                                                }
                                                i4 = i + 1;
                                                transport2 = transport;
                                                boolean z11 = z;
                                                transportPipes2 = transportPipes;
                                                N3 = N;
                                                backupRunStatus6 = backupRunStatus;
                                                buffer3 = buffer2;
                                                c2 = 0;
                                                singlePackageBackupRunner = null;
                                            } catch (Exception e5) {
                                                e = e5;
                                                transportPipes2 = transportPipes;
                                                Slog.w(TAG, "Exception trying full transport backup", e);
                                                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                                if (this.mCancelAll) {
                                                }
                                                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                                cleanUpPipes(transportPipes2);
                                                cleanUpPipes(enginePipes2);
                                                unregisterTask();
                                                if (this.mJob != null) {
                                                }
                                                synchronized (this.backupManagerService.getQueueLock()) {
                                                }
                                                this.mListener.onFinished("PFTBT.run()");
                                                this.mLatch.countDown();
                                                if (this.mUpdateSchedule) {
                                                }
                                                Slog.i(TAG, "Full data backup pass finished.");
                                                this.backupManagerService.getWakelock().release();
                                                ParcelFileDescriptor[] parcelFileDescriptorArr22 = transportPipes2;
                                            } catch (Throwable th19) {
                                                th = th19;
                                                if (this.mCancelAll) {
                                                }
                                                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                                                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                                                cleanUpPipes(transportPipes);
                                                cleanUpPipes(enginePipes2);
                                                unregisterTask();
                                                if (this.mJob != null) {
                                                }
                                                synchronized (this.backupManagerService.getQueueLock()) {
                                                }
                                                this.mListener.onFinished("PFTBT.run()");
                                                this.mLatch.countDown();
                                                if (this.mUpdateSchedule) {
                                                }
                                                Slog.i(TAG, "Full data backup pass finished.");
                                                this.backupManagerService.getWakelock().release();
                                                throw th;
                                            }
                                        } catch (Throwable th20) {
                                            th = th20;
                                            byte[] bArr8 = buffer;
                                            IBackupTransport iBackupTransport9 = transport2;
                                            PackageInfo packageInfo7 = currentPackage2;
                                            String str11 = packageName2;
                                            long j9 = quota;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    }
                                } catch (Throwable th21) {
                                    th = th21;
                                    obj = obj2;
                                    backoff3 = backoff5;
                                    byte[] bArr9 = buffer3;
                                    int i24 = i5;
                                    PackageInfo packageInfo8 = currentPackage;
                                    int i25 = N3;
                                    boolean z12 = z2;
                                    backupRunStatus = backupRunStatus6;
                                    String str12 = packageName;
                                    IBackupTransport iBackupTransport10 = transport2;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            }
                            backoff = backoff5;
                            backupRunStatus4 = backupRunStatus6;
                            break;
                        } catch (Exception e6) {
                            e = e6;
                            long j10 = backoff5;
                            int i26 = backupRunStatus6;
                            transportPipes2 = transportPipes;
                            Slog.w(TAG, "Exception trying full transport backup", e);
                            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                            if (this.mCancelAll) {
                            }
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                            cleanUpPipes(transportPipes2);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            ParcelFileDescriptor[] parcelFileDescriptorArr222 = transportPipes2;
                        } catch (Throwable th22) {
                            th = th22;
                            long j11 = backoff5;
                            backupRunStatus = backupRunStatus6;
                            if (this.mCancelAll) {
                            }
                            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                            cleanUpPipes(transportPipes);
                            cleanUpPipes(enginePipes2);
                            unregisterTask();
                            if (this.mJob != null) {
                            }
                            synchronized (this.backupManagerService.getQueueLock()) {
                            }
                            this.mListener.onFinished("PFTBT.run()");
                            this.mLatch.countDown();
                            if (this.mUpdateSchedule) {
                            }
                            Slog.i(TAG, "Full data backup pass finished.");
                            this.backupManagerService.getWakelock().release();
                            throw th;
                        }
                    }
                    if (this.mCancelAll) {
                        backupRunStatus5 = -2003;
                    } else {
                        backupRunStatus5 = backupRunStatus4;
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus5);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus5);
                    cleanUpPipes(transportPipes);
                    cleanUpPipes(enginePipes2);
                    unregisterTask();
                    if (this.mJob != null) {
                        this.mJob.finishBackupPass();
                    }
                    synchronized (this.backupManagerService.getQueueLock()) {
                        try {
                            this.backupManagerService.setRunningFullBackupTask(null);
                        } catch (Throwable th23) {
                            th = th23;
                            long j12 = backoff;
                            while (true) {
                                throw th;
                            }
                        }
                    }
                    this.mListener.onFinished("PFTBT.run()");
                    this.mLatch.countDown();
                    if (this.mUpdateSchedule) {
                        backoff2 = backoff;
                        this.backupManagerService.scheduleNextFullBackupJob(backoff2);
                    } else {
                        backoff2 = backoff;
                    }
                    Slog.i(TAG, "Full data backup pass finished.");
                    this.backupManagerService.getWakelock().release();
                    long j13 = backoff2;
                    int i27 = backupRunStatus5;
                }
            }
            try {
                Slog.i(TAG, "full backup requested but enabled=" + this.backupManagerService.isEnabled() + " provisioned=" + this.backupManagerService.isProvisioned() + "; ignoring");
                if (this.backupManagerService.isProvisioned()) {
                    monitoringEvent = 13;
                } else {
                    monitoringEvent = 14;
                }
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, monitoringEvent, null, 3, null);
                this.mUpdateSchedule = false;
                int backupRunStatus7 = -2001;
                if (this.mCancelAll) {
                    backupRunStatus7 = -2003;
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus7);
                cleanUpPipes(null);
                cleanUpPipes(null);
                unregisterTask();
                if (this.mJob != null) {
                    this.mJob.finishBackupPass();
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                    this.backupManagerService.setRunningFullBackupTask(null);
                }
                this.mListener.onFinished("PFTBT.run()");
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    this.backupManagerService.scheduleNextFullBackupJob(0);
                }
                Slog.i(TAG, "Full data backup pass finished.");
                this.backupManagerService.getWakelock().release();
            } catch (Exception e7) {
                e = e7;
                Slog.w(TAG, "Exception trying full transport backup", e);
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                if (this.mCancelAll) {
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                cleanUpPipes(transportPipes2);
                cleanUpPipes(enginePipes2);
                unregisterTask();
                if (this.mJob != null) {
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                }
                this.mListener.onFinished("PFTBT.run()");
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                }
                Slog.i(TAG, "Full data backup pass finished.");
                this.backupManagerService.getWakelock().release();
                ParcelFileDescriptor[] parcelFileDescriptorArr2222 = transportPipes2;
            } catch (Throwable th24) {
                th = th24;
                transportPipes = null;
                if (this.mCancelAll) {
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
                cleanUpPipes(transportPipes);
                cleanUpPipes(enginePipes2);
                unregisterTask();
                if (this.mJob != null) {
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                }
                this.mListener.onFinished("PFTBT.run()");
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                }
                Slog.i(TAG, "Full data backup pass finished.");
                this.backupManagerService.getWakelock().release();
                throw th;
            }
        } catch (Exception e8) {
            e = e8;
            Slog.w(TAG, "Exception trying full transport backup", e);
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
            if (this.mCancelAll) {
            }
            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
            cleanUpPipes(transportPipes2);
            cleanUpPipes(enginePipes2);
            unregisterTask();
            if (this.mJob != null) {
            }
            synchronized (this.backupManagerService.getQueueLock()) {
            }
            this.mListener.onFinished("PFTBT.run()");
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
            }
            Slog.i(TAG, "Full data backup pass finished.");
            this.backupManagerService.getWakelock().release();
            ParcelFileDescriptor[] parcelFileDescriptorArr22222 = transportPipes2;
        } catch (Throwable th25) {
            th = th25;
            backupRunStatus = 0;
            transportPipes = null;
            if (this.mCancelAll) {
            }
            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus);
            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus);
            cleanUpPipes(transportPipes);
            cleanUpPipes(enginePipes2);
            unregisterTask();
            if (this.mJob != null) {
            }
            synchronized (this.backupManagerService.getQueueLock()) {
            }
            this.mListener.onFinished("PFTBT.run()");
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
            }
            Slog.i(TAG, "Full data backup pass finished.");
            this.backupManagerService.getWakelock().release();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanUpPipes(ParcelFileDescriptor[] pipes) {
        if (pipes != null) {
            if (pipes[0] != null) {
                ParcelFileDescriptor fd = pipes[0];
                pipes[0] = null;
                try {
                    fd.close();
                } catch (IOException e) {
                    Slog.w(TAG, "Unable to close pipe!");
                }
            }
            if (pipes[1] != null) {
                ParcelFileDescriptor fd2 = pipes[1];
                pipes[1] = null;
                try {
                    fd2.close();
                } catch (IOException e2) {
                    Slog.w(TAG, "Unable to close pipe!");
                }
            }
        }
    }
}
