package com.android.server.backup.fullbackup;

import android.app.IBackupAgent;
import android.app.backup.BackupProgress;
import android.app.backup.IBackupCallback;
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
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.FullBackupJob;
import com.android.server.backup.TransportManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.internal.Operation;
import com.android.server.backup.remote.RemoteCall;
import com.android.server.backup.remote.RemoteCallable;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.transport.TransportNotAvailableException;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.backup.utils.BackupObserverUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.pm.DumpState;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PerformFullTransportBackupTask extends FullBackupTask implements BackupRestoreTask {
    private static final String TAG = "PFTBT";
    private UserBackupManagerService backupManagerService;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
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
    private IBackupManagerMonitor mMonitor;
    ArrayList<PackageInfo> mPackages;
    private final TransportClient mTransportClient;
    boolean mUpdateSchedule;
    private final int mUserId;
    boolean mUserInitiated;

    public static PerformFullTransportBackupTask newWithCurrentTransport(UserBackupManagerService backupManagerService2, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, boolean userInitiated, String caller) {
        TransportManager transportManager = backupManagerService2.getTransportManager();
        TransportClient transportClient = transportManager.getCurrentTransportClient(caller);
        return new PerformFullTransportBackupTask(backupManagerService2, transportClient, observer, whichPackages, updateSchedule, runningJob, latch, backupObserver, monitor, new OnTaskFinishedListener(transportClient) {
            /* class com.android.server.backup.fullbackup.$$Lambda$PerformFullTransportBackupTask$ymLoQLrsEpmGaMrcudrdAgsU1Zk */
            private final /* synthetic */ TransportClient f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.backup.internal.OnTaskFinishedListener
            public final void onFinished(String str) {
                TransportManager.this.disposeOfTransportClient(this.f$1, str);
            }
        }, userInitiated);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PerformFullTransportBackupTask(UserBackupManagerService backupManagerService2, TransportClient transportClient, IFullBackupRestoreObserver observer, String[] whichPackages, boolean updateSchedule, FullBackupJob runningJob, CountDownLatch latch, IBackupObserver backupObserver, IBackupManagerMonitor monitor, OnTaskFinishedListener listener, boolean userInitiated) {
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
        this.mUserId = backupManagerService2.getUserId();
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
                PackageInfo info = backupManagerService2.getPackageManager().getPackageInfoAsUser(pkg, DumpState.DUMP_HWFEATURES, this.mUserId);
                this.mCurrentPackage = info;
                if (!AppBackupUtils.appIsEligibleForBackup(info.applicationInfo, this.mUserId)) {
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

    @Override // com.android.server.backup.BackupRestoreTask
    public void execute() {
    }

    @Override // com.android.server.backup.BackupRestoreTask
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
                this.backupManagerService.handleCancel(this.mBackupRunnerOpToken, cancelAll);
                try {
                    this.mTransportClient.getConnectedTransport("PFTBT.handleCancel()").cancelFullBackup();
                } catch (RemoteException | TransportNotAvailableException e) {
                    Slog.w(TAG, "Error calling cancelFullBackup() on transport: " + e);
                }
            }
        }
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void operationComplete(long result) {
    }

    /* JADX INFO: Multiple debug info for r3v49 'packageName'  java.lang.String: [D('in' java.io.FileInputStream), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r6v29 'backoff'  long: [D('quota' long), D('backoff' long)] */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x0389, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x04cb, code lost:
        com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r35.mBackupObserver, r3, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
        android.util.Slog.w(com.android.server.backup.fullbackup.PerformFullTransportBackupTask.TAG, "Transport failed; aborting backup: " + r1);
        android.util.EventLog.writeEvent((int) com.android.server.EventLogTags.FULL_BACKUP_TRANSPORT_FAILURE, new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:?, code lost:
        r35.backupManagerService.tearDownAgentAndKill(r4.applicationInfo);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x04fb, code lost:
        if (r35.mCancelAll == false) goto L_0x0501;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x04fd, code lost:
        r2 = -2003;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x0501, code lost:
        r2 = -1000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x0502, code lost:
        android.util.Slog.i(com.android.server.backup.fullbackup.PerformFullTransportBackupTask.TAG, "Full backup completed with status: " + r2);
        com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r35.mBackupObserver, r2);
        cleanUpPipes(r5);
        cleanUpPipes(r31);
        unregisterTask();
        r9 = r35.mJob;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x052a, code lost:
        if (r9 == null) goto L_0x0531;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x052c, code lost:
        r9.finishBackupPass(r35.mUserId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x0531, code lost:
        r9 = r35.backupManagerService.getQueueLock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x0537, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:?, code lost:
        r35.backupManagerService.setRunningFullBackupTask(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0540, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x0541, code lost:
        r35.mListener.onFinished("PFTBT.run()");
        r35.mLatch.countDown();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x054f, code lost:
        if (r35.mUpdateSchedule == false) goto L_0x0556;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x0551, code lost:
        r35.backupManagerService.scheduleNextFullBackupJob(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x0556, code lost:
        android.util.Slog.i(com.android.server.backup.fullbackup.PerformFullTransportBackupTask.TAG, "Full data backup pass finished.");
        r35.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x0566, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x0567, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x0568, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x056a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x056b, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x056e, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x056f, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x0570, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x0571, code lost:
        r8 = r31;
        r1 = r0;
        r27 = -1000;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x0578, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x0579, code lost:
        r8 = r31;
        r1 = r0;
        r2 = r5;
        r3 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:261:0x061a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x061b, code lost:
        r1 = r0;
        r12 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:263:0x0620, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:264:0x0621, code lost:
        r1 = r0;
        r2 = r5;
        r3 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x06dc, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x07d5  */
    /* JADX WARNING: Removed duplicated region for block: B:342:0x07d8  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0801  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x080d A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x084a  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x084f  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0879  */
    /* JADX WARNING: Removed duplicated region for block: B:370:0x0885 A[SYNTHETIC] */
    @Override // java.lang.Runnable
    public void run() {
        int backupRunStatus;
        long backoff;
        ParcelFileDescriptor[] enginePipes;
        ParcelFileDescriptor[] transportPipes;
        Throwable th;
        int backupRunStatus2;
        FullBackupJob fullBackupJob;
        Exception e;
        int backupRunStatus3;
        FullBackupJob fullBackupJob2;
        int monitoringEvent;
        long backoff2;
        int backupRunStatus4;
        ParcelFileDescriptor[] transportPipes2;
        int backupRunStatus5;
        long backoff3;
        PackageInfo currentPackage;
        String packageName;
        int flags;
        Object obj;
        long backoff4;
        Object obj2;
        Throwable th2;
        int backupPackageStatus;
        PackageInfo currentPackage2;
        int N;
        String packageName2;
        byte[] buffer;
        char c;
        long backoff5;
        ParcelFileDescriptor[] enginePipes2;
        String packageName3;
        int backupPackageStatus2;
        PackageInfo currentPackage3;
        FileInputStream in;
        FileOutputStream out;
        long preflightResult;
        int backupPackageStatus3;
        FileInputStream in2;
        long totalRead;
        int backupPackageStatus4;
        int backupPackageStatus5;
        Throwable th3;
        ParcelFileDescriptor[] enginePipes3;
        int backupRunStatus6;
        ParcelFileDescriptor[] enginePipes4 = null;
        ParcelFileDescriptor[] transportPipes3 = null;
        long backoff6 = 0;
        int backupRunStatus7 = 0;
        SinglePackageBackupRunner singlePackageBackupRunner = null;
        try {
            try {
                Slog.i(TAG, "full backup requested but enabled=" + this.backupManagerService.isEnabled() + " setupComplete=" + this.backupManagerService.isSetupComplete() + "; ignoring");
                if (this.backupManagerService.isSetupComplete()) {
                    monitoringEvent = 13;
                } else {
                    monitoringEvent = 14;
                }
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, monitoringEvent, null, 3, null);
                this.mUpdateSchedule = false;
                int backupRunStatus8 = -2001;
                if (this.mCancelAll) {
                    backupRunStatus8 = -2003;
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus8);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus8);
                cleanUpPipes(null);
                cleanUpPipes(null);
                unregisterTask();
                FullBackupJob fullBackupJob3 = this.mJob;
                if (fullBackupJob3 != null) {
                    fullBackupJob3.finishBackupPass(this.mUserId);
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
            } catch (Exception e2) {
                enginePipes = null;
                e = e2;
                try {
                    Slog.w(TAG, "Exception trying full transport backup", e);
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                    if (!this.mCancelAll) {
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                    cleanUpPipes(transportPipes3);
                    cleanUpPipes(enginePipes);
                    unregisterTask();
                    fullBackupJob2 = this.mJob;
                    if (fullBackupJob2 != null) {
                    }
                    synchronized (this.backupManagerService.getQueueLock()) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    transportPipes = transportPipes3;
                    backoff = backoff6;
                    backupRunStatus = -1000;
                    if (!this.mCancelAll) {
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                    cleanUpPipes(transportPipes);
                    cleanUpPipes(enginePipes);
                    unregisterTask();
                    fullBackupJob = this.mJob;
                    if (fullBackupJob != null) {
                    }
                    synchronized (this.backupManagerService.getQueueLock()) {
                    }
                }
            } catch (Throwable th5) {
                enginePipes = null;
                transportPipes = null;
                backoff = 0;
                th = th5;
                if (!this.mCancelAll) {
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                cleanUpPipes(transportPipes);
                cleanUpPipes(enginePipes);
                unregisterTask();
                fullBackupJob = this.mJob;
                if (fullBackupJob != null) {
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                }
            }
        } catch (Exception e3) {
            enginePipes = null;
            e = e3;
            Slog.w(TAG, "Exception trying full transport backup", e);
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
            if (!this.mCancelAll) {
                backupRunStatus3 = -2003;
            } else {
                backupRunStatus3 = -1000;
            }
            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
            cleanUpPipes(transportPipes3);
            cleanUpPipes(enginePipes);
            unregisterTask();
            fullBackupJob2 = this.mJob;
            if (fullBackupJob2 != null) {
                fullBackupJob2.finishBackupPass(this.mUserId);
            }
            synchronized (this.backupManagerService.getQueueLock()) {
                this.backupManagerService.setRunningFullBackupTask(null);
            }
            this.mListener.onFinished("PFTBT.run()");
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
                this.backupManagerService.scheduleNextFullBackupJob(backoff6);
            }
            Slog.i(TAG, "Full data backup pass finished.");
            this.backupManagerService.getWakelock().release();
            return;
        } catch (Throwable th6) {
            backupRunStatus = 0;
            enginePipes = null;
            transportPipes = null;
            backoff = 0;
            th = th6;
            if (!this.mCancelAll) {
                backupRunStatus2 = -2003;
            } else {
                backupRunStatus2 = backupRunStatus;
            }
            Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
            BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
            cleanUpPipes(transportPipes);
            cleanUpPipes(enginePipes);
            unregisterTask();
            fullBackupJob = this.mJob;
            if (fullBackupJob != null) {
                fullBackupJob.finishBackupPass(this.mUserId);
            }
            synchronized (this.backupManagerService.getQueueLock()) {
                this.backupManagerService.setRunningFullBackupTask(null);
            }
            this.mListener.onFinished("PFTBT.run()");
            this.mLatch.countDown();
            if (this.mUpdateSchedule) {
                this.backupManagerService.scheduleNextFullBackupJob(backoff);
            }
            Slog.i(TAG, "Full data backup pass finished.");
            this.backupManagerService.getWakelock().release();
            throw th;
        }
        if (!this.backupManagerService.isEnabled()) {
            backupRunStatus = 0;
        } else if (!this.backupManagerService.isSetupComplete()) {
            backupRunStatus = 0;
        } else {
            IBackupTransport transport = this.mTransportClient.connect("PFTBT.run()");
            if (transport == null) {
                try {
                    Slog.w(TAG, "Transport not present; full data backup not performed");
                    backupRunStatus7 = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 15, this.mCurrentPackage, 1, null);
                    if (this.mCancelAll) {
                        backupRunStatus6 = -2003;
                    } else {
                        backupRunStatus6 = -1000;
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus6);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus6);
                    cleanUpPipes(null);
                    cleanUpPipes(null);
                    unregisterTask();
                    FullBackupJob fullBackupJob4 = this.mJob;
                    if (fullBackupJob4 != null) {
                        fullBackupJob4.finishBackupPass(this.mUserId);
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
                } catch (Exception e4) {
                    enginePipes = null;
                    e = e4;
                    Slog.w(TAG, "Exception trying full transport backup", e);
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                    if (!this.mCancelAll) {
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                    cleanUpPipes(transportPipes3);
                    cleanUpPipes(enginePipes);
                    unregisterTask();
                    fullBackupJob2 = this.mJob;
                    if (fullBackupJob2 != null) {
                    }
                    synchronized (this.backupManagerService.getQueueLock()) {
                    }
                } catch (Throwable th7) {
                    enginePipes = null;
                    transportPipes = null;
                    backoff = 0;
                    backupRunStatus = backupRunStatus7;
                    th = th7;
                    if (!this.mCancelAll) {
                    }
                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                    cleanUpPipes(transportPipes);
                    cleanUpPipes(enginePipes);
                    unregisterTask();
                    fullBackupJob = this.mJob;
                    if (fullBackupJob != null) {
                    }
                    synchronized (this.backupManagerService.getQueueLock()) {
                    }
                }
            } else {
                int N2 = this.mPackages.size();
                byte[] buffer2 = new byte[8192];
                int i = 0;
                long backoff7 = 0;
                while (true) {
                    if (i >= N2) {
                        backoff2 = backoff7;
                        backupRunStatus4 = backupRunStatus7;
                        transportPipes2 = transportPipes3;
                        break;
                    }
                    try {
                        this.mBackupRunner = singlePackageBackupRunner;
                        currentPackage = this.mPackages.get(i);
                        packageName = currentPackage.packageName;
                        Slog.i(TAG, "Initiating full-data transport backup of " + packageName + " token: " + this.mCurrentOpToken);
                        EventLog.writeEvent((int) EventLogTags.FULL_BACKUP_PACKAGE, packageName);
                        transportPipes2 = ParcelFileDescriptor.createPipe();
                    } catch (Exception e5) {
                        enginePipes = enginePipes4;
                        backoff6 = backoff7;
                        e = e5;
                        Slog.w(TAG, "Exception trying full transport backup", e);
                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                        if (!this.mCancelAll) {
                        }
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                        cleanUpPipes(transportPipes3);
                        cleanUpPipes(enginePipes);
                        unregisterTask();
                        fullBackupJob2 = this.mJob;
                        if (fullBackupJob2 != null) {
                        }
                        synchronized (this.backupManagerService.getQueueLock()) {
                        }
                    } catch (Throwable th8) {
                        backupRunStatus = backupRunStatus7;
                        enginePipes = enginePipes4;
                        transportPipes = transportPipes3;
                        backoff = backoff7;
                        th = th8;
                        if (!this.mCancelAll) {
                        }
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                        cleanUpPipes(transportPipes);
                        cleanUpPipes(enginePipes);
                        unregisterTask();
                        fullBackupJob = this.mJob;
                        if (fullBackupJob != null) {
                        }
                        synchronized (this.backupManagerService.getQueueLock()) {
                        }
                    }
                    try {
                        flags = this.mUserInitiated ? 1 : 0;
                        obj = this.mCancelLock;
                        backoff2 = backoff7;
                        backupRunStatus4 = backupRunStatus7;
                        break;
                    } catch (Exception e6) {
                        enginePipes = enginePipes4;
                        transportPipes3 = transportPipes2;
                        backoff6 = backoff7;
                        e = e6;
                        Slog.w(TAG, "Exception trying full transport backup", e);
                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                        if (!this.mCancelAll) {
                        }
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                        cleanUpPipes(transportPipes3);
                        cleanUpPipes(enginePipes);
                        unregisterTask();
                        fullBackupJob2 = this.mJob;
                        if (fullBackupJob2 != null) {
                        }
                        synchronized (this.backupManagerService.getQueueLock()) {
                        }
                    } catch (Throwable th9) {
                        backupRunStatus = backupRunStatus7;
                        transportPipes = transportPipes2;
                        enginePipes = enginePipes4;
                        backoff = backoff7;
                        th = th9;
                        if (!this.mCancelAll) {
                        }
                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                        cleanUpPipes(transportPipes);
                        cleanUpPipes(enginePipes);
                        unregisterTask();
                        fullBackupJob = this.mJob;
                        if (fullBackupJob != null) {
                        }
                        synchronized (this.backupManagerService.getQueueLock()) {
                        }
                    }
                    synchronized (obj) {
                        try {
                        } catch (Throwable th10) {
                            backoff4 = backoff7;
                            obj2 = obj;
                            backupRunStatus = backupRunStatus7;
                            transportPipes = transportPipes2;
                            enginePipes = enginePipes4;
                            th2 = th10;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th11) {
                                    th2 = th11;
                                }
                            }
                            throw th2;
                        }
                        if (this.mCancelAll) {
                            try {
                                break;
                            } catch (Throwable th12) {
                                backoff4 = backoff7;
                                obj2 = obj;
                                backupRunStatus = backupRunStatus7;
                                enginePipes = enginePipes4;
                                transportPipes = transportPipes2;
                                th2 = th12;
                                while (true) {
                                    break;
                                }
                                throw th2;
                            }
                        } else {
                            try {
                                backupPackageStatus = transport.performFullBackup(currentPackage, transportPipes2[0], flags);
                                if (backupPackageStatus == 0) {
                                    try {
                                        backoff4 = backoff7;
                                        backoff5 = transport.getBackupQuota(currentPackage.packageName, true);
                                        try {
                                            enginePipes3 = ParcelFileDescriptor.createPipe();
                                            try {
                                                currentPackage2 = currentPackage;
                                                backupRunStatus = backupRunStatus7;
                                                buffer = buffer2;
                                                N = N2;
                                                packageName2 = packageName;
                                                c = 1;
                                                obj2 = obj;
                                            } catch (Throwable th13) {
                                                obj2 = obj;
                                                backupRunStatus = backupRunStatus7;
                                                th2 = th13;
                                                transportPipes = transportPipes2;
                                                enginePipes = enginePipes3;
                                                while (true) {
                                                    break;
                                                }
                                                throw th2;
                                            }
                                        } catch (Throwable th14) {
                                            obj2 = obj;
                                            backupRunStatus = backupRunStatus7;
                                            enginePipes = enginePipes4;
                                            transportPipes = transportPipes2;
                                            th2 = th14;
                                            while (true) {
                                                break;
                                            }
                                            throw th2;
                                        }
                                        try {
                                            this.mBackupRunner = new SinglePackageBackupRunner(enginePipes3[1], currentPackage2, this.mTransportClient, backoff5, this.mBackupRunnerOpToken, transport.getTransportFlags());
                                            enginePipes3[1].close();
                                            enginePipes3[1] = null;
                                            this.mIsDoingBackup = true;
                                            enginePipes4 = enginePipes3;
                                        } catch (Throwable th15) {
                                            th2 = th15;
                                            transportPipes = transportPipes2;
                                            enginePipes = enginePipes3;
                                            while (true) {
                                                break;
                                            }
                                            throw th2;
                                        }
                                    } catch (Throwable th16) {
                                        backoff4 = backoff7;
                                        obj2 = obj;
                                        backupRunStatus = backupRunStatus7;
                                        enginePipes = enginePipes4;
                                        transportPipes = transportPipes2;
                                        th2 = th16;
                                        while (true) {
                                            break;
                                        }
                                        throw th2;
                                    }
                                } else {
                                    currentPackage2 = currentPackage;
                                    backoff4 = backoff7;
                                    N = N2;
                                    obj2 = obj;
                                    backupRunStatus = backupRunStatus7;
                                    packageName2 = packageName;
                                    c = 1;
                                    buffer = buffer2;
                                    backoff5 = Long.MAX_VALUE;
                                }
                            } catch (Throwable th17) {
                                backoff4 = backoff7;
                                obj2 = obj;
                                backupRunStatus = backupRunStatus7;
                                transportPipes = transportPipes2;
                                enginePipes = enginePipes4;
                                th2 = th17;
                                while (true) {
                                    break;
                                }
                                throw th2;
                            }
                            try {
                                try {
                                    if (this.mUpdateSchedule) {
                                        this.backupManagerService.enqueueFullBackup(packageName3, System.currentTimeMillis());
                                    }
                                    if (backupPackageStatus2 == -1002) {
                                        BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                                        Slog.i(TAG, "Transport rejected backup of " + packageName3 + ", skipping");
                                        EventLog.writeEvent((int) EventLogTags.FULL_BACKUP_AGENT_FAILURE, packageName3, "transport rejected");
                                        if (this.mBackupRunner != null) {
                                            currentPackage3 = currentPackage2;
                                            this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                            enginePipes = enginePipes2;
                                        } else {
                                            currentPackage3 = currentPackage2;
                                            enginePipes = enginePipes2;
                                        }
                                    } else {
                                        currentPackage3 = currentPackage2;
                                        if (backupPackageStatus2 == -1005) {
                                            BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -1005);
                                            Slog.i(TAG, "Transport quota exceeded for package: " + packageName3);
                                            EventLog.writeEvent((int) EventLogTags.FULL_BACKUP_QUOTA_EXCEEDED, packageName3);
                                            this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                            enginePipes = enginePipes2;
                                        } else if (backupPackageStatus2 == -1003) {
                                            BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -1003);
                                            Slog.w(TAG, "Application failure for package: " + packageName3);
                                            EventLog.writeEvent((int) EventLogTags.BACKUP_AGENT_FAILURE, packageName3);
                                            this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                            enginePipes = enginePipes2;
                                        } else if (backupPackageStatus2 == -2003) {
                                            BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, -2003);
                                            Slog.w(TAG, "Backup cancelled. package=" + packageName3 + ", cancelAll=" + this.mCancelAll);
                                            EventLog.writeEvent((int) EventLogTags.FULL_BACKUP_CANCELLED, packageName3);
                                            this.backupManagerService.tearDownAgentAndKill(currentPackage3.applicationInfo);
                                            enginePipes = enginePipes2;
                                        } else if (backupPackageStatus2 != 0) {
                                            break;
                                        } else {
                                            enginePipes = enginePipes2;
                                            try {
                                                BackupObserverUtils.sendBackupOnPackageResult(this.mBackupObserver, packageName3, 0);
                                                EventLog.writeEvent((int) EventLogTags.FULL_BACKUP_SUCCESS, packageName3);
                                                this.backupManagerService.logBackupComplete(packageName3);
                                            } catch (Exception e7) {
                                                e = e7;
                                                transportPipes3 = transportPipes;
                                                backoff6 = backoff;
                                                Slog.w(TAG, "Exception trying full transport backup", e);
                                                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                                if (!this.mCancelAll) {
                                                }
                                                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                                cleanUpPipes(transportPipes3);
                                                cleanUpPipes(enginePipes);
                                                unregisterTask();
                                                fullBackupJob2 = this.mJob;
                                                if (fullBackupJob2 != null) {
                                                }
                                                synchronized (this.backupManagerService.getQueueLock()) {
                                                }
                                            } catch (Throwable th18) {
                                                th = th18;
                                                if (!this.mCancelAll) {
                                                }
                                                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                                cleanUpPipes(transportPipes);
                                                cleanUpPipes(enginePipes);
                                                unregisterTask();
                                                fullBackupJob = this.mJob;
                                                if (fullBackupJob != null) {
                                                }
                                                synchronized (this.backupManagerService.getQueueLock()) {
                                                }
                                            }
                                        }
                                    }
                                    cleanUpPipes(transportPipes);
                                    cleanUpPipes(enginePipes);
                                    if (currentPackage3.applicationInfo != null) {
                                        Slog.i(TAG, "Unbinding agent in " + packageName3);
                                        try {
                                            this.backupManagerService.getActivityManager().unbindBackupAgent(currentPackage3.applicationInfo);
                                        } catch (RemoteException e8) {
                                        }
                                    }
                                    i++;
                                    transportPipes3 = transportPipes;
                                    enginePipes4 = enginePipes;
                                    backoff7 = backoff;
                                    buffer2 = buffer;
                                    N2 = N;
                                    backupRunStatus7 = backupRunStatus;
                                    singlePackageBackupRunner = null;
                                } catch (Exception e9) {
                                    enginePipes = enginePipes2;
                                    e = e9;
                                    transportPipes3 = transportPipes;
                                    backoff6 = backoff;
                                    Slog.w(TAG, "Exception trying full transport backup", e);
                                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                    cleanUpPipes(transportPipes3);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob2 = this.mJob;
                                    if (fullBackupJob2 != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                } catch (Throwable th19) {
                                    enginePipes = enginePipes2;
                                    th = th19;
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                    cleanUpPipes(transportPipes);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob = this.mJob;
                                    if (fullBackupJob != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                }
                            } catch (Throwable th20) {
                                enginePipes = enginePipes4;
                                transportPipes = transportPipes2;
                                th2 = th20;
                                while (true) {
                                    break;
                                }
                                throw th2;
                            }
                            if (backupPackageStatus == 0) {
                                try {
                                    transportPipes2[0].close();
                                    transportPipes2[0] = null;
                                    new Thread(this.mBackupRunner, "package-backup-bridge").start();
                                    in = new FileInputStream(enginePipes4[0].getFileDescriptor());
                                    out = new FileOutputStream(transportPipes2[c].getFileDescriptor());
                                    preflightResult = this.mBackupRunner.getPreflightResultBlocking();
                                    transportPipes = transportPipes2;
                                    int backupRunnerResult = this.mBackupRunner.getBackupResultBlocking();
                                    synchronized (this.mCancelLock) {
                                        try {
                                            this.mIsDoingBackup = false;
                                            if (!this.mCancelAll) {
                                                if (backupRunnerResult == 0) {
                                                    int finishResult = transport.finishBackup();
                                                    if (backupPackageStatus3 == 0) {
                                                        backupPackageStatus3 = finishResult;
                                                    }
                                                } else {
                                                    transport.cancelFullBackup();
                                                }
                                            }
                                        } catch (Throwable th21) {
                                            th = th21;
                                            while (true) {
                                                throw th;
                                            }
                                        }
                                    }
                                    if (backupPackageStatus3 == 0 && backupRunnerResult != 0) {
                                        backupPackageStatus3 = backupRunnerResult;
                                    }
                                    if (backupPackageStatus3 != 0) {
                                        Slog.e(TAG, "Error " + backupPackageStatus3 + " backing up " + packageName3);
                                    }
                                    backoff = transport.requestFullBackupTime();
                                } catch (Exception e10) {
                                    e = e10;
                                    transportPipes3 = transportPipes2;
                                    backoff6 = backoff4;
                                    enginePipes = enginePipes4;
                                    Slog.w(TAG, "Exception trying full transport backup", e);
                                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                    cleanUpPipes(transportPipes3);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob2 = this.mJob;
                                    if (fullBackupJob2 != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                } catch (Throwable th22) {
                                    transportPipes = transportPipes2;
                                    th = th22;
                                    backoff = backoff4;
                                    enginePipes = enginePipes4;
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                    cleanUpPipes(transportPipes);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob = this.mJob;
                                    if (fullBackupJob != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                }
                                if (preflightResult < 0) {
                                    try {
                                        enginePipes2 = enginePipes4;
                                    } catch (Exception e11) {
                                        e = e11;
                                        transportPipes3 = transportPipes;
                                        backoff6 = backoff4;
                                        enginePipes = enginePipes4;
                                        Slog.w(TAG, "Exception trying full transport backup", e);
                                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                        if (!this.mCancelAll) {
                                        }
                                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                        cleanUpPipes(transportPipes3);
                                        cleanUpPipes(enginePipes);
                                        unregisterTask();
                                        fullBackupJob2 = this.mJob;
                                        if (fullBackupJob2 != null) {
                                        }
                                        synchronized (this.backupManagerService.getQueueLock()) {
                                        }
                                    } catch (Throwable th23) {
                                        th = th23;
                                        backoff = backoff4;
                                        enginePipes = enginePipes4;
                                        if (!this.mCancelAll) {
                                        }
                                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                        cleanUpPipes(transportPipes);
                                        cleanUpPipes(enginePipes);
                                        unregisterTask();
                                        fullBackupJob = this.mJob;
                                        if (fullBackupJob != null) {
                                        }
                                        synchronized (this.backupManagerService.getQueueLock()) {
                                        }
                                    }
                                    try {
                                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 16, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_PREFLIGHT_ERROR", preflightResult));
                                        backupPackageStatus3 = (int) preflightResult;
                                        packageName3 = packageName2;
                                    } catch (Exception e12) {
                                        e = e12;
                                        transportPipes3 = transportPipes;
                                        backoff6 = backoff4;
                                        enginePipes = enginePipes2;
                                        Slog.w(TAG, "Exception trying full transport backup", e);
                                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                        if (!this.mCancelAll) {
                                        }
                                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                        cleanUpPipes(transportPipes3);
                                        cleanUpPipes(enginePipes);
                                        unregisterTask();
                                        fullBackupJob2 = this.mJob;
                                        if (fullBackupJob2 != null) {
                                        }
                                        synchronized (this.backupManagerService.getQueueLock()) {
                                        }
                                    } catch (Throwable th24) {
                                        th = th24;
                                        backoff = backoff4;
                                        enginePipes = enginePipes2;
                                        if (!this.mCancelAll) {
                                        }
                                        Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                        BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                        cleanUpPipes(transportPipes);
                                        cleanUpPipes(enginePipes);
                                        unregisterTask();
                                        fullBackupJob = this.mJob;
                                        if (fullBackupJob != null) {
                                        }
                                        synchronized (this.backupManagerService.getQueueLock()) {
                                        }
                                    }
                                } else {
                                    enginePipes2 = enginePipes4;
                                    long totalRead2 = 0;
                                    int backupPackageStatus6 = 0;
                                    while (true) {
                                        int nRead = in.read(buffer);
                                        if (nRead > 0) {
                                            out.write(buffer, backupPackageStatus6, nRead);
                                            synchronized (this.mCancelLock) {
                                                try {
                                                    if (!this.mCancelAll) {
                                                        try {
                                                            backupPackageStatus = transport.sendBackupData(nRead);
                                                        } catch (Throwable th25) {
                                                            th3 = th25;
                                                            while (true) {
                                                                try {
                                                                    break;
                                                                } catch (Throwable th26) {
                                                                    th3 = th26;
                                                                }
                                                            }
                                                            throw th3;
                                                        }
                                                    }
                                                } catch (Throwable th27) {
                                                    th3 = th27;
                                                    while (true) {
                                                        break;
                                                    }
                                                    throw th3;
                                                }
                                            }
                                            totalRead = totalRead2 + ((long) nRead);
                                            if (this.mBackupObserver == null || preflightResult <= 0) {
                                                in2 = in;
                                                packageName3 = packageName2;
                                            } else {
                                                in2 = in;
                                                packageName3 = packageName2;
                                                BackupObserverUtils.sendBackupOnUpdate(this.mBackupObserver, packageName3, new BackupProgress(preflightResult, totalRead));
                                            }
                                            backupPackageStatus4 = backupPackageStatus;
                                        } else {
                                            in2 = in;
                                            packageName3 = packageName2;
                                            backupPackageStatus4 = backupPackageStatus;
                                            totalRead = totalRead2;
                                        }
                                        if (nRead <= 0 || backupPackageStatus4 != 0) {
                                            break;
                                        }
                                        backupPackageStatus = backupPackageStatus4;
                                        packageName2 = packageName3;
                                        totalRead2 = totalRead;
                                        in = in2;
                                        backupPackageStatus6 = 0;
                                    }
                                    if (backupPackageStatus4 == -1005) {
                                        backupPackageStatus5 = backupPackageStatus4;
                                        Slog.w(TAG, "Package hit quota limit in-flight " + packageName3 + ": " + totalRead + " of " + backoff5);
                                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 18, this.mCurrentPackage, 1, null);
                                        this.mBackupRunner.sendQuotaExceeded(totalRead, backoff5);
                                    } else {
                                        backupPackageStatus5 = backupPackageStatus4;
                                    }
                                    backupPackageStatus3 = backupPackageStatus5;
                                }
                                try {
                                    Slog.i(TAG, "Transport suggested backoff=" + backoff);
                                    backupPackageStatus2 = backupPackageStatus3;
                                } catch (Exception e13) {
                                    e = e13;
                                    transportPipes3 = transportPipes;
                                    backoff6 = backoff;
                                    enginePipes = enginePipes2;
                                    Slog.w(TAG, "Exception trying full transport backup", e);
                                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 19, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_EXCEPTION_FULL_BACKUP", Log.getStackTraceString(e)));
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus3);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus3);
                                    cleanUpPipes(transportPipes3);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob2 = this.mJob;
                                    if (fullBackupJob2 != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                } catch (Throwable th28) {
                                    th = th28;
                                    enginePipes = enginePipes2;
                                    if (!this.mCancelAll) {
                                    }
                                    Slog.i(TAG, "Full backup completed with status: " + backupRunStatus2);
                                    BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus2);
                                    cleanUpPipes(transportPipes);
                                    cleanUpPipes(enginePipes);
                                    unregisterTask();
                                    fullBackupJob = this.mJob;
                                    if (fullBackupJob != null) {
                                    }
                                    synchronized (this.backupManagerService.getQueueLock()) {
                                    }
                                }
                            } else {
                                enginePipes2 = enginePipes4;
                                transportPipes = transportPipes2;
                                packageName3 = packageName2;
                                backupPackageStatus2 = backupPackageStatus;
                                backoff = backoff4;
                            }
                        }
                    }
                }
                if (this.mCancelAll) {
                    backupRunStatus5 = -2003;
                } else {
                    backupRunStatus5 = backupRunStatus4;
                }
                Slog.i(TAG, "Full backup completed with status: " + backupRunStatus5);
                BackupObserverUtils.sendBackupFinished(this.mBackupObserver, backupRunStatus5);
                cleanUpPipes(transportPipes2);
                cleanUpPipes(enginePipes4);
                unregisterTask();
                FullBackupJob fullBackupJob5 = this.mJob;
                if (fullBackupJob5 != null) {
                    fullBackupJob5.finishBackupPass(this.mUserId);
                }
                synchronized (this.backupManagerService.getQueueLock()) {
                    try {
                        this.backupManagerService.setRunningFullBackupTask(null);
                    } catch (Throwable th29) {
                        th = th29;
                        while (true) {
                            throw th;
                        }
                    }
                }
                this.mListener.onFinished("PFTBT.run()");
                this.mLatch.countDown();
                if (this.mUpdateSchedule) {
                    backoff3 = backoff2;
                    this.backupManagerService.scheduleNextFullBackupJob(backoff3);
                } else {
                    backoff3 = backoff2;
                }
                Slog.i(TAG, "Full data backup pass finished.");
                this.backupManagerService.getWakelock().release();
            }
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

    /* access modifiers changed from: package-private */
    public class SinglePackageBackupPreflight implements BackupRestoreTask, FullBackupPreflight {
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

        @Override // com.android.server.backup.fullbackup.FullBackupPreflight
        public int preflightFullBackup(PackageInfo pkg, IBackupAgent agent) {
            Exception e;
            long fullBackupAgentTimeoutMillis = PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis();
            try {
                PerformFullTransportBackupTask.this.backupManagerService.prepareOperationTimeout(this.mCurrentOpToken, fullBackupAgentTimeoutMillis, this, 0);
                agent.doMeasureFullBackup(this.mQuota, this.mCurrentOpToken, PerformFullTransportBackupTask.this.backupManagerService.getBackupManagerBinder(), this.mTransportFlags);
                this.mLatch.await(fullBackupAgentTimeoutMillis, TimeUnit.MILLISECONDS);
                long totalSize = this.mResult.get();
                if (totalSize < 0) {
                    return (int) totalSize;
                }
                int result = this.mTransportClient.connectOrThrow("PFTBT$SPBP.preflightFullBackup()").checkFullBackupSize(totalSize);
                if (result == -1005) {
                    try {
                        RemoteCall.execute(new RemoteCallable(agent, totalSize) {
                            /* class com.android.server.backup.fullbackup.$$Lambda$PerformFullTransportBackupTask$SinglePackageBackupPreflight$hWbC3_rWMPrteAdbbM5aSW2SKD0 */
                            private final /* synthetic */ IBackupAgent f$1;
                            private final /* synthetic */ long f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            @Override // com.android.server.backup.remote.RemoteCallable
                            public final void call(Object obj) {
                                PerformFullTransportBackupTask.SinglePackageBackupPreflight.this.lambda$preflightFullBackup$0$PerformFullTransportBackupTask$SinglePackageBackupPreflight(this.f$1, this.f$2, (IBackupCallback) obj);
                            }
                        }, PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getQuotaExceededTimeoutMillis());
                    } catch (Exception e2) {
                        e = e2;
                    }
                }
                return result;
            } catch (Exception e3) {
                e = e3;
                Slog.w(PerformFullTransportBackupTask.TAG, "Exception preflighting " + pkg.packageName + ": " + e.getMessage());
                return -1003;
            }
        }

        public /* synthetic */ void lambda$preflightFullBackup$0$PerformFullTransportBackupTask$SinglePackageBackupPreflight(IBackupAgent agent, long totalSize, IBackupCallback callback) throws RemoteException {
            agent.doQuotaExceeded(totalSize, this.mQuota, callback);
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void execute() {
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void operationComplete(long result) {
            this.mResult.set(result);
            this.mLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void handleCancel(boolean cancelAll) {
            this.mResult.set(-1003);
            this.mLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }

        @Override // com.android.server.backup.fullbackup.FullBackupPreflight
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

        SinglePackageBackupRunner(ParcelFileDescriptor output, PackageInfo target, TransportClient transportClient, long quota, int currentOpToken, int transportFlags) throws IOException {
            this.mOutput = ParcelFileDescriptor.dup(output.getFileDescriptor());
            this.mTarget = target;
            this.mCurrentOpToken = currentOpToken;
            this.mEphemeralToken = PerformFullTransportBackupTask.this.backupManagerService.generateRandomIntegerToken();
            this.mPreflight = new SinglePackageBackupPreflight(transportClient, quota, this.mEphemeralToken, transportFlags);
            this.mQuota = quota;
            this.mTransportFlags = transportFlags;
            registerTask();
        }

        /* access modifiers changed from: package-private */
        public void registerTask() {
            synchronized (PerformFullTransportBackupTask.this.backupManagerService.getCurrentOpLock()) {
                PerformFullTransportBackupTask.this.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 0));
            }
        }

        /* access modifiers changed from: package-private */
        public void unregisterTask() {
            synchronized (PerformFullTransportBackupTask.this.backupManagerService.getCurrentOpLock()) {
                PerformFullTransportBackupTask.this.backupManagerService.getCurrentOperations().remove(this.mCurrentOpToken);
            }
        }

        /* JADX INFO: finally extract failed */
        @Override // java.lang.Runnable
        public void run() {
            this.mEngine = new FullBackupEngine(PerformFullTransportBackupTask.this.backupManagerService, new FileOutputStream(this.mOutput.getFileDescriptor()), this.mPreflight, this.mTarget, false, this, this.mQuota, this.mCurrentOpToken, this.mTransportFlags);
            try {
                if (!this.mIsCancelled) {
                    this.mPreflightResult = this.mEngine.preflightCheck();
                }
                try {
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
                    Slog.e(PerformFullTransportBackupTask.TAG, "Exception during full package backup of " + this.mTarget.packageName);
                    unregisterTask();
                    this.mBackupLatch.countDown();
                    this.mOutput.close();
                } catch (Throwable th) {
                    unregisterTask();
                    this.mBackupLatch.countDown();
                    try {
                        this.mOutput.close();
                    } catch (IOException e3) {
                        Slog.w(PerformFullTransportBackupTask.TAG, "Error closing transport pipe in runner");
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                this.mPreflightLatch.countDown();
                throw th2;
            }
        }

        public void sendQuotaExceeded(long backupDataBytes, long quotaBytes) {
            this.mEngine.sendQuotaExceeded(backupDataBytes, quotaBytes);
        }

        /* access modifiers changed from: package-private */
        public long getPreflightResultBlocking() {
            try {
                this.mPreflightLatch.await(PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
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
                this.mBackupLatch.await(PerformFullTransportBackupTask.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
                if (this.mIsCancelled) {
                    return -2003;
                }
                return this.mBackupResult;
            } catch (InterruptedException e) {
                return -1003;
            }
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void execute() {
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void operationComplete(long result) {
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void handleCancel(boolean cancelAll) {
            Slog.w(PerformFullTransportBackupTask.TAG, "Full backup cancel of " + this.mTarget.packageName);
            PerformFullTransportBackupTask performFullTransportBackupTask = PerformFullTransportBackupTask.this;
            performFullTransportBackupTask.mMonitor = BackupManagerMonitorUtils.monitorEvent(performFullTransportBackupTask.mMonitor, 4, PerformFullTransportBackupTask.this.mCurrentPackage, 2, null);
            this.mIsCancelled = true;
            PerformFullTransportBackupTask.this.backupManagerService.handleCancel(this.mEphemeralToken, cancelAll);
            PerformFullTransportBackupTask.this.backupManagerService.tearDownAgentAndKill(this.mTarget.applicationInfo);
            this.mPreflightLatch.countDown();
            this.mBackupLatch.countDown();
            PerformFullTransportBackupTask.this.backupManagerService.removeOperation(this.mCurrentOpToken);
        }
    }
}
