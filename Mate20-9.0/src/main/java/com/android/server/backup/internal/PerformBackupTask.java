package com.android.server.backup.internal;

import android.app.IBackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.WorkSource;
import android.system.ErrnoException;
import android.system.Os;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.EventLogTags;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.DataChangedJournal;
import com.android.server.backup.KeyValueBackupJob;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.transport.TransportUtils;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.backup.utils.BackupObserverUtils;
import com.android.server.job.JobSchedulerShellCommand;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class PerformBackupTask implements BackupRestoreTask {
    private static final String TAG = "PerformBackupTask";
    private BackupManagerService backupManagerService;
    private IBackupAgent mAgentBinder;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private ParcelFileDescriptor mBackupData;
    private File mBackupDataName;
    private volatile boolean mCancelAll;
    private final Object mCancelLock = new Object();
    private final int mCurrentOpToken;
    private PackageInfo mCurrentPackage;
    private BackupState mCurrentState;
    private volatile int mEphemeralOpToken;
    private boolean mFinished;
    private final PerformFullTransportBackupTask mFullBackupTask;
    private DataChangedJournal mJournal;
    private final OnTaskFinishedListener mListener;
    private IBackupManagerMonitor mMonitor;
    private ParcelFileDescriptor mNewState;
    private File mNewStateName;
    private final boolean mNonIncremental;
    private IBackupObserver mObserver;
    private ArrayList<BackupRequest> mOriginalQueue;
    private List<String> mPendingFullBackups;
    private ArrayList<BackupRequest> mQueue;
    private ParcelFileDescriptor mSavedState;
    private File mSavedStateName;
    private File mStateDir;
    private int mStatus;
    private final TransportClient mTransportClient;
    private final boolean mUserInitiated;

    public PerformBackupTask(BackupManagerService backupManagerService2, TransportClient transportClient, String dirName, ArrayList<BackupRequest> queue, DataChangedJournal journal, IBackupObserver observer, IBackupManagerMonitor monitor, OnTaskFinishedListener listener, List<String> pendingFullBackups, boolean userInitiated, boolean nonIncremental) {
        BackupManagerService backupManagerService3 = backupManagerService2;
        this.backupManagerService = backupManagerService3;
        TransportClient transportClient2 = transportClient;
        this.mTransportClient = transportClient2;
        this.mOriginalQueue = queue;
        this.mQueue = new ArrayList<>();
        this.mJournal = journal;
        this.mObserver = observer;
        this.mMonitor = monitor;
        this.mListener = listener != null ? listener : OnTaskFinishedListener.NOP;
        this.mPendingFullBackups = pendingFullBackups;
        this.mUserInitiated = userInitiated;
        this.mNonIncremental = nonIncremental;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        this.mStateDir = new File(backupManagerService2.getBaseStateDir(), dirName);
        this.mCurrentOpToken = backupManagerService2.generateRandomIntegerToken();
        this.mFinished = false;
        synchronized (backupManagerService2.getCurrentOpLock()) {
            if (backupManagerService2.isBackupOperationInProgress()) {
                Slog.d(TAG, "Skipping backup since one is already in progress.");
                this.mCancelAll = true;
                this.mFullBackupTask = null;
                this.mCurrentState = BackupState.FINAL;
                backupManagerService3.addBackupTrace("Skipped. Backup already in progress.");
            } else {
                this.mCurrentState = BackupState.INITIAL;
                CountDownLatch latch = new CountDownLatch(1);
                IBackupObserver iBackupObserver = this.mObserver;
                PerformFullTransportBackupTask performFullTransportBackupTask = new PerformFullTransportBackupTask(backupManagerService3, transportClient2, null, (String[]) this.mPendingFullBackups.toArray(new String[this.mPendingFullBackups.size()]), false, null, latch, iBackupObserver, this.mMonitor, this.mListener, this.mUserInitiated);
                this.mFullBackupTask = performFullTransportBackupTask;
                registerTask();
                backupManagerService3.addBackupTrace("STATE => INITIAL");
            }
        }
    }

    private void registerTask() {
        synchronized (this.backupManagerService.getCurrentOpLock()) {
            this.backupManagerService.getCurrentOperations().put(this.mCurrentOpToken, new Operation(0, this, 2));
        }
    }

    private void unregisterTask() {
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }

    @GuardedBy("mCancelLock")
    public void execute() {
        synchronized (this.mCancelLock) {
            switch (this.mCurrentState) {
                case INITIAL:
                    beginBackup();
                    break;
                case BACKUP_PM:
                    backupPm();
                    break;
                case RUNNING_QUEUE:
                    invokeNextAgent();
                    break;
                case FINAL:
                    if (this.mFinished) {
                        Slog.e(TAG, "Duplicate finish of K/V pass");
                        break;
                    } else {
                        finalizeBackup();
                        break;
                    }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0177, code lost:
        if (r10.mStatus != 0) goto L_0x0179;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0179, code lost:
        r10.backupManagerService.resetBackupState(r10.mStateDir);
        com.android.server.backup.utils.BackupObserverUtils.sendBackupFinished(r10.mObserver, com.android.server.job.JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
        executeNextState(com.android.server.backup.internal.BackupState.FINAL);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x01c7, code lost:
        if (r10.mStatus == 0) goto L_0x01ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x01ca, code lost:
        return;
     */
    private void beginBackup() {
        this.backupManagerService.clearBackupTrace();
        StringBuilder b = new StringBuilder(256);
        b.append("beginBackup: [");
        Iterator<BackupRequest> it = this.mOriginalQueue.iterator();
        while (it.hasNext()) {
            b.append(' ');
            b.append(it.next().packageName);
        }
        b.append(" ]");
        this.backupManagerService.addBackupTrace(b.toString());
        this.mAgentBinder = null;
        this.mStatus = 0;
        if (!this.mOriginalQueue.isEmpty() || !this.mPendingFullBackups.isEmpty()) {
            this.mQueue = (ArrayList) this.mOriginalQueue.clone();
            boolean skipPm = this.mNonIncremental;
            int i = 0;
            while (true) {
                if (i >= this.mQueue.size()) {
                    break;
                } else if (BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(this.mQueue.get(i).packageName)) {
                    this.mQueue.remove(i);
                    skipPm = false;
                    break;
                } else {
                    i++;
                }
            }
            Slog.v(TAG, "Beginning backup of " + this.mQueue.size() + " targets");
            File pmState = new File(this.mStateDir, BackupManagerService.PACKAGE_MANAGER_SENTINEL);
            try {
                IBackupTransport transport = this.mTransportClient.connectOrThrow("PBT.beginBackup()");
                EventLog.writeEvent(EventLogTags.BACKUP_START, transport.transportDirName());
                if (this.mStatus == 0 && pmState.length() <= 0) {
                    Slog.i(TAG, "Initializing (wiping) backup state and transport storage");
                    this.backupManagerService.addBackupTrace("initializing transport " + transportName);
                    this.backupManagerService.resetBackupState(this.mStateDir);
                    this.mStatus = transport.initializeDevice();
                    this.backupManagerService.addBackupTrace("transport.initializeDevice() == " + this.mStatus);
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
                    executeNextState(BackupState.BACKUP_PM);
                }
                this.backupManagerService.addBackupTrace("exiting prelim: " + this.mStatus);
            } catch (Exception e) {
                Slog.e(TAG, "Error in backup thread during init", e);
                this.backupManagerService.addBackupTrace("Exception in backup thread during init: " + e);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                this.backupManagerService.addBackupTrace("exiting prelim: " + this.mStatus);
            } catch (Throwable th) {
                this.backupManagerService.addBackupTrace("exiting prelim: " + this.mStatus);
                if (this.mStatus != 0) {
                    this.backupManagerService.resetBackupState(this.mStateDir);
                    BackupObserverUtils.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    executeNextState(BackupState.FINAL);
                }
                throw th;
            }
        } else {
            Slog.w(TAG, "Backup begun with an empty queue - nothing to do.");
            this.backupManagerService.addBackupTrace("queue empty at begin");
            BackupObserverUtils.sendBackupFinished(this.mObserver, 0);
            executeNextState(BackupState.FINAL);
        }
    }

    private void backupPm() {
        try {
            this.mStatus = invokeAgentForBackup(BackupManagerService.PACKAGE_MANAGER_SENTINEL, IBackupAgent.Stub.asInterface(this.backupManagerService.makeMetadataAgent().onBind()));
            BackupManagerService backupManagerService2 = this.backupManagerService;
            backupManagerService2.addBackupTrace("PMBA invoke: " + this.mStatus);
            this.backupManagerService.getBackupHandler().removeMessages(17);
            BackupManagerService backupManagerService3 = this.backupManagerService;
            backupManagerService3.addBackupTrace("exiting backupPm: " + this.mStatus);
            if (this.mStatus == 0) {
                return;
            }
        } catch (Exception e) {
            Slog.e(TAG, "Error in backup thread during pm", e);
            BackupManagerService backupManagerService4 = this.backupManagerService;
            backupManagerService4.addBackupTrace("Exception in backup thread during pm: " + e);
            this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            BackupManagerService backupManagerService5 = this.backupManagerService;
            backupManagerService5.addBackupTrace("exiting backupPm: " + this.mStatus);
            if (this.mStatus == 0) {
                return;
            }
        } catch (Throwable th) {
            BackupManagerService backupManagerService6 = this.backupManagerService;
            backupManagerService6.addBackupTrace("exiting backupPm: " + this.mStatus);
            if (this.mStatus != 0) {
                this.backupManagerService.resetBackupState(this.mStateDir);
                BackupObserverUtils.sendBackupFinished(this.mObserver, invokeAgentToObserverError(this.mStatus));
                executeNextState(BackupState.FINAL);
            }
            throw th;
        }
        this.backupManagerService.resetBackupState(this.mStateDir);
        BackupObserverUtils.sendBackupFinished(this.mObserver, invokeAgentToObserverError(this.mStatus));
        executeNextState(BackupState.FINAL);
    }

    private int invokeAgentToObserverError(int error) {
        if (error == -1003) {
            return -1003;
        }
        return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x02e2, code lost:
        r11.mStatus = 0;
        com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r11.mObserver, r1.packageName, -2002);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x02ec, code lost:
        revertAndEndBackup();
        r6 = com.android.server.backup.internal.BackupState.FINAL;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0295, code lost:
        if (r11.mStatus == -1004) goto L_0x02e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x02e0, code lost:
        if (r11.mStatus == -1004) goto L_0x02e2;
     */
    private void invokeNextAgent() {
        BackupState nextState;
        this.mStatus = 0;
        BackupManagerService backupManagerService2 = this.backupManagerService;
        backupManagerService2.addBackupTrace("invoke q=" + this.mQueue.size());
        if (this.mQueue.isEmpty()) {
            executeNextState(BackupState.FINAL);
            return;
        }
        BackupRequest request = this.mQueue.get(0);
        this.mQueue.remove(0);
        Slog.d(TAG, "starting key/value backup of " + request);
        BackupManagerService backupManagerService3 = this.backupManagerService;
        backupManagerService3.addBackupTrace("launch agent for " + request.packageName);
        try {
            PackageManager pm = this.backupManagerService.getPackageManager();
            this.mCurrentPackage = pm.getPackageInfo(request.packageName, 134217728);
            if (!AppBackupUtils.appIsEligibleForBackup(this.mCurrentPackage.applicationInfo, pm)) {
                Slog.i(TAG, "Package " + request.packageName + " no longer supports backup; skipping");
                this.backupManagerService.addBackupTrace("skipping - not eligible, completion is noop");
                BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                executeNextState(BackupState.RUNNING_QUEUE);
                this.backupManagerService.getWakelock().setWorkSource(null);
                if (this.mStatus != 0) {
                    BackupState nextState2 = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.backupManagerService.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState2 = BackupState.FINAL;
                        }
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = 0;
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, request.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState2 = BackupState.FINAL;
                    }
                    executeNextState(nextState2);
                } else {
                    this.backupManagerService.addBackupTrace("expecting completion/timeout callback");
                }
            } else if (AppBackupUtils.appGetsFullBackup(this.mCurrentPackage)) {
                Slog.i(TAG, "Package " + request.packageName + " requests full-data rather than key/value; skipping");
                this.backupManagerService.addBackupTrace("skipping - fullBackupOnly, completion is noop");
                BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                executeNextState(BackupState.RUNNING_QUEUE);
                this.backupManagerService.getWakelock().setWorkSource(null);
                if (this.mStatus != 0) {
                    BackupState nextState3 = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.backupManagerService.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState3 = BackupState.FINAL;
                        }
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = 0;
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, request.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState3 = BackupState.FINAL;
                    }
                    executeNextState(nextState3);
                } else {
                    this.backupManagerService.addBackupTrace("expecting completion/timeout callback");
                }
            } else if (AppBackupUtils.appIsStopped(this.mCurrentPackage.applicationInfo)) {
                this.backupManagerService.addBackupTrace("skipping - stopped");
                BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -2001);
                executeNextState(BackupState.RUNNING_QUEUE);
                this.backupManagerService.getWakelock().setWorkSource(null);
                if (this.mStatus != 0) {
                    BackupState nextState4 = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.backupManagerService.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState4 = BackupState.FINAL;
                        }
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                    } else if (this.mStatus == -1004) {
                        this.mStatus = 0;
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, request.packageName, -2002);
                    } else {
                        revertAndEndBackup();
                        nextState4 = BackupState.FINAL;
                    }
                    executeNextState(nextState4);
                } else {
                    this.backupManagerService.addBackupTrace("expecting completion/timeout callback");
                }
            } else {
                try {
                    this.backupManagerService.getWakelock().setWorkSource(new WorkSource(this.mCurrentPackage.applicationInfo.uid));
                    IBackupAgent agent = this.backupManagerService.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, 0);
                    BackupManagerService backupManagerService4 = this.backupManagerService;
                    StringBuilder sb = new StringBuilder();
                    sb.append("agent bound; a? = ");
                    sb.append(agent != null);
                    backupManagerService4.addBackupTrace(sb.toString());
                    if (agent != null) {
                        this.mAgentBinder = agent;
                        this.mStatus = invokeAgentForBackup(request.packageName, agent);
                    } else {
                        this.mStatus = -1003;
                    }
                } catch (SecurityException ex) {
                    Slog.d(TAG, "error in bind/backup", ex);
                    this.mStatus = -1003;
                    this.backupManagerService.addBackupTrace("agent SE");
                }
                this.backupManagerService.getWakelock().setWorkSource(null);
                if (this.mStatus != 0) {
                    nextState = BackupState.RUNNING_QUEUE;
                    this.mAgentBinder = null;
                    if (this.mStatus == -1003) {
                        this.backupManagerService.dataChangedImpl(request.packageName);
                        this.mStatus = 0;
                        if (this.mQueue.isEmpty()) {
                            nextState = BackupState.FINAL;
                        }
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                        executeNextState(nextState);
                    }
                }
                this.backupManagerService.addBackupTrace("expecting completion/timeout callback");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Slog.d(TAG, "Package does not exist; skipping");
            this.backupManagerService.addBackupTrace("no such package");
            this.mStatus = -1004;
            this.backupManagerService.getWakelock().setWorkSource(null);
            if (this.mStatus != 0) {
                nextState = BackupState.RUNNING_QUEUE;
                this.mAgentBinder = null;
                if (this.mStatus == -1003) {
                    this.backupManagerService.dataChangedImpl(request.packageName);
                    this.mStatus = 0;
                    if (this.mQueue.isEmpty()) {
                        nextState = BackupState.FINAL;
                    }
                }
            }
        } catch (Throwable th) {
            this.backupManagerService.getWakelock().setWorkSource(null);
            if (this.mStatus != 0) {
                BackupState nextState5 = BackupState.RUNNING_QUEUE;
                this.mAgentBinder = null;
                if (this.mStatus == -1003) {
                    this.backupManagerService.dataChangedImpl(request.packageName);
                    this.mStatus = 0;
                    if (this.mQueue.isEmpty()) {
                        nextState5 = BackupState.FINAL;
                    }
                    BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, this.mCurrentPackage.packageName, -1003);
                } else if (this.mStatus == -1004) {
                    this.mStatus = 0;
                    BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, request.packageName, -2002);
                } else {
                    revertAndEndBackup();
                    nextState5 = BackupState.FINAL;
                }
                executeNextState(nextState5);
            } else {
                this.backupManagerService.addBackupTrace("expecting completion/timeout callback");
            }
            throw th;
        }
    }

    private void finalizeBackup() {
        this.backupManagerService.addBackupTrace("finishing");
        Iterator<BackupRequest> it = this.mQueue.iterator();
        while (it.hasNext()) {
            this.backupManagerService.dataChangedImpl(it.next().packageName);
        }
        if (this.mJournal != null && !this.mJournal.delete()) {
            Slog.e(TAG, "Unable to remove backup journal file " + this.mJournal);
        }
        if (this.backupManagerService.getCurrentToken() == 0 && this.mStatus == 0) {
            this.backupManagerService.addBackupTrace("success; recording token");
            try {
                this.backupManagerService.setCurrentToken(this.mTransportClient.connectOrThrow("PBT.finalizeBackup()").getCurrentRestoreSet());
                this.backupManagerService.writeRestoreTokens();
            } catch (Exception e) {
                Slog.e(TAG, "Transport threw reporting restore set: " + e.getMessage());
                this.backupManagerService.addBackupTrace("transport threw returning token");
            }
        }
        synchronized (this.backupManagerService.getQueueLock()) {
            this.backupManagerService.setBackupRunning(false);
            if (this.mStatus == -1001) {
                this.backupManagerService.addBackupTrace("init required; rerunning");
                try {
                    this.backupManagerService.getPendingInits().add(this.backupManagerService.getTransportManager().getTransportName(this.mTransportClient.getTransportComponent()));
                } catch (Exception e2) {
                    Slog.w(TAG, "Failed to query transport name for init: " + e2.getMessage());
                }
                clearMetadata();
                this.backupManagerService.backupNow();
            }
        }
        this.backupManagerService.clearBackupTrace();
        unregisterTask();
        if (!this.mCancelAll && this.mStatus == 0 && this.mPendingFullBackups != null && !this.mPendingFullBackups.isEmpty()) {
            Slog.d(TAG, "Starting full backups for: " + this.mPendingFullBackups);
            this.backupManagerService.getWakelock().acquire();
            new Thread(this.mFullBackupTask, "full-transport-requested").start();
        } else if (this.mCancelAll) {
            this.mListener.onFinished("PBT.finalizeBackup()");
            if (this.mFullBackupTask != null) {
                this.mFullBackupTask.unregisterTask();
            }
            BackupObserverUtils.sendBackupFinished(this.mObserver, -2003);
        } else {
            this.mListener.onFinished("PBT.finalizeBackup()");
            this.mFullBackupTask.unregisterTask();
            int i = this.mStatus;
            if (!(i == -1005 || i == 0)) {
                switch (i) {
                    case JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS /*-1002*/:
                        break;
                    case JobSchedulerShellCommand.CMD_ERR_NO_JOB /*-1001*/:
                        BackupObserverUtils.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        break;
                    default:
                        BackupObserverUtils.sendBackupFinished(this.mObserver, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        break;
                }
            }
            BackupObserverUtils.sendBackupFinished(this.mObserver, 0);
        }
        this.mFinished = true;
        Slog.i(TAG, "K/V backup pass finished.");
        this.backupManagerService.getWakelock().release();
    }

    private void clearMetadata() {
        File pmState = new File(this.mStateDir, BackupManagerService.PACKAGE_MANAGER_SENTINEL);
        if (pmState.exists()) {
            pmState.delete();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x018b  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0194  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x019c  */
    private int invokeAgentForBackup(String packageName, IBackupAgent agent) {
        int i;
        String str = packageName;
        Slog.d(TAG, "invokeAgentForBackup on " + str);
        this.backupManagerService.addBackupTrace("invoking " + str);
        File blankStateName = new File(this.mStateDir, "blank_state");
        this.mSavedStateName = new File(this.mStateDir, str);
        this.mBackupDataName = new File(this.backupManagerService.getDataDir(), str + ".data");
        this.mNewStateName = new File(this.mStateDir, str + ".new");
        this.mSavedState = null;
        this.mBackupData = null;
        this.mNewState = null;
        boolean callingAgent = false;
        this.mEphemeralOpToken = this.backupManagerService.generateRandomIntegerToken();
        try {
            if (str.equals(BackupManagerService.PACKAGE_MANAGER_SENTINEL)) {
                this.mCurrentPackage = new PackageInfo();
                this.mCurrentPackage.packageName = str;
            }
            this.mSavedState = ParcelFileDescriptor.open(this.mNonIncremental ? blankStateName : this.mSavedStateName, 402653184);
            this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
            if (!SELinux.restorecon(this.mBackupDataName)) {
                Slog.e(TAG, "SELinux restorecon failed on " + this.mBackupDataName);
            }
            this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
            IBackupTransport transport = this.mTransportClient.connectOrThrow("PBT.invokeAgentForBackup()");
            long quota = transport.getBackupQuota(str, false);
            try {
                this.backupManagerService.addBackupTrace("setting timeout");
                this.backupManagerService.prepareOperationTimeout(this.mEphemeralOpToken, this.mAgentTimeoutParameters.getKvBackupAgentTimeoutMillis(), this, 0);
                this.backupManagerService.addBackupTrace("calling agent doBackup()");
                agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, quota, this.mEphemeralOpToken, this.backupManagerService.getBackupManagerBinder(), transport.getTransportFlags());
                if (this.mNonIncremental) {
                    blankStateName.delete();
                }
                this.backupManagerService.addBackupTrace("invoke success");
                return 0;
            } catch (Exception e) {
                e = e;
                callingAgent = true;
                try {
                    Slog.e(TAG, "Error invoking for backup on " + str + ". " + e);
                    BackupManagerService backupManagerService2 = this.backupManagerService;
                    StringBuilder sb = new StringBuilder();
                    sb.append("exception: ");
                    sb.append(e);
                    backupManagerService2.addBackupTrace(sb.toString());
                    EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, new Object[]{str, e.toString()});
                    errorCleanup();
                    if (!callingAgent) {
                    }
                    if (this.mNonIncremental) {
                    }
                    return i;
                } catch (Throwable th) {
                    e = th;
                    if (this.mNonIncremental) {
                    }
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                if (this.mNonIncremental) {
                    blankStateName.delete();
                }
                throw e;
            }
        } catch (Exception e2) {
            e = e2;
            Slog.e(TAG, "Error invoking for backup on " + str + ". " + e);
            BackupManagerService backupManagerService22 = this.backupManagerService;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("exception: ");
            sb2.append(e);
            backupManagerService22.addBackupTrace(sb2.toString());
            EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, new Object[]{str, e.toString()});
            errorCleanup();
            if (!callingAgent) {
                i = -1003;
            } else {
                i = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            }
            if (this.mNonIncremental) {
                blankStateName.delete();
            }
            return i;
        }
    }

    private void failAgent(IBackupAgent agent, String message) {
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
            for (byte hexString : checksum) {
                sb.append(Integer.toHexString(hexString));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Slog.e(TAG, "Unable to use SHA-1!");
            return "00";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004d, code lost:
        r7 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0052, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0053, code lost:
        r10 = r8;
        r8 = r7;
        r7 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005e, code lost:
        $closeResource(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0061, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008a, code lost:
        r8 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008b, code lost:
        r9 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x008f, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0090, code lost:
        r10 = r9;
        r9 = r8;
        r8 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0097, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009b, code lost:
        $closeResource(r4, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x009e, code lost:
        throw r7;
     */
    private void writeWidgetPayloadIfAppropriate(FileDescriptor fd, String pkgName) throws IOException {
        DataOutputStream stateOut;
        Throwable th;
        Throwable th2;
        DataInputStream in;
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
                    FileInputStream fin = new FileInputStream(widgetFile);
                    in = new DataInputStream(fin);
                    String priorChecksum = in.readUTF();
                    $closeResource(null, in);
                    $closeResource(null, fin);
                    if (Objects.equals(newChecksum, priorChecksum)) {
                        return;
                    }
                }
            }
            BackupDataOutput out = new BackupDataOutput(fd);
            if (widgetState != null) {
                FileOutputStream fout = new FileOutputStream(widgetFile);
                stateOut = new DataOutputStream(fout);
                stateOut.writeUTF(newChecksum);
                $closeResource(null, stateOut);
                $closeResource(null, fout);
                out.writeEntityHeader(BackupManagerService.KEY_WIDGET_STATE, widgetState.length);
                out.writeEntityData(widgetState, widgetState.length);
            } else {
                out.writeEntityHeader(BackupManagerService.KEY_WIDGET_STATE, -1);
                widgetFile.delete();
            }
            return;
        }
        return;
        $closeResource(th, stateOut);
        throw th2;
        $closeResource(th3, in);
        throw th4;
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

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008f, code lost:
        failAgent(r1.mAgentBinder, "Illegal backup key: " + r0);
        r1.backupManagerService.addBackupTrace("illegal key " + r0 + " from " + r3);
        r8 = new java.lang.Object[r11];
        r8[0] = r3;
        r8[r12] = "bad key";
        android.util.EventLog.writeEvent(com.android.server.EventLogTags.BACKUP_AGENT_FAILURE, r8);
        r1.mMonitor = com.android.server.backup.utils.BackupManagerMonitorUtils.monitorEvent(r1.mMonitor, 5, r1.mCurrentPackage, 3, com.android.server.backup.utils.BackupManagerMonitorUtils.putMonitoringExtra((android.os.Bundle) null, "android.app.backup.extra.LOG_ILLEGAL_KEY", r0));
        r1.backupManagerService.getBackupHandler().removeMessages(17);
        com.android.server.backup.utils.BackupObserverUtils.sendBackupOnPackageResult(r1.mObserver, r3, -1003);
        errorCleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00f7, code lost:
        if (r14 == null) goto L_0x00fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r14.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00fd, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0109, code lost:
        if (r14 == null) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r14.close();
     */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:116:0x0318=Splitter:B:116:0x0318, B:37:0x00fc=Splitter:B:37:0x00fc, B:155:0x039c=Splitter:B:155:0x039c} */
    @GuardedBy("mCancelLock")
    public void operationComplete(long unusedResult) {
        ParcelFileDescriptor backupData;
        BackupState nextState;
        int incrementalFlag;
        ParcelFileDescriptor readFd;
        this.backupManagerService.removeOperation(this.mEphemeralOpToken);
        synchronized (this.mCancelLock) {
            if (this.mFinished) {
                Slog.d(TAG, "operationComplete received after task finished.");
            } else if (this.mBackupData == null) {
                String pkg = this.mCurrentPackage != null ? this.mCurrentPackage.packageName : "[none]";
                this.backupManagerService.addBackupTrace("late opComplete; curPkg = " + pkg);
            } else {
                String pkgName = this.mCurrentPackage.packageName;
                long filepos = this.mBackupDataName.length();
                FileDescriptor fd = this.mBackupData.getFileDescriptor();
                int i = 2;
                char c = 1;
                try {
                    if (this.mCurrentPackage.applicationInfo != null && (this.mCurrentPackage.applicationInfo.flags & 1) == 0) {
                        readFd = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                        BackupDataInput in = new BackupDataInput(readFd.getFileDescriptor());
                        while (true) {
                            BackupDataInput in2 = in;
                            if (!in2.readNextHeader()) {
                                break;
                            }
                            String key = in2.getKey();
                            if (key != null && key.charAt(0) >= 65280) {
                                break;
                            }
                            in2.skipEntityData();
                            in = in2;
                            i = 2;
                            c = 1;
                        }
                    }
                    writeWidgetPayloadIfAppropriate(fd, pkgName);
                } catch (IOException e) {
                    IOException iOException = e;
                    Slog.w(TAG, "Unable to save widget state for " + pkgName);
                    try {
                        Os.ftruncate(fd, filepos);
                    } catch (ErrnoException e2) {
                        ErrnoException errnoException = e2;
                        Slog.w(TAG, "Unable to roll back!");
                    } catch (Throwable th) {
                        ParcelFileDescriptor backupData2 = backupData;
                        Throwable th2 = th;
                        if (backupData2 != null) {
                            try {
                                backupData2.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th2;
                    }
                } catch (Throwable th3) {
                    if (readFd != null) {
                        readFd.close();
                    }
                    throw th3;
                }
                this.backupManagerService.getBackupHandler().removeMessages(17);
                clearAgentState();
                this.backupManagerService.addBackupTrace("operation complete");
                IBackupTransport transport = this.mTransportClient.connect("PBT.operationComplete()");
                backupData = null;
                this.mStatus = 0;
                long size = 0;
                try {
                    TransportUtils.checkTransportNotNull(transport);
                    size = this.mBackupDataName.length();
                    if (size > 0) {
                        boolean isNonIncremental = this.mSavedStateName.length() == 0;
                        if (this.mStatus == 0) {
                            backupData = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
                            this.backupManagerService.addBackupTrace("sending data to transport");
                            int userInitiatedFlag = this.mUserInitiated;
                            if (isNonIncremental) {
                                incrementalFlag = 4;
                            } else {
                                incrementalFlag = 2;
                            }
                            this.mStatus = transport.performBackup(this.mCurrentPackage, backupData, (int) (userInitiatedFlag | incrementalFlag));
                        }
                        if (isNonIncremental && this.mStatus == -1006) {
                            Slog.w(TAG, "Transport requested non-incremental but already the case, error");
                            this.backupManagerService.addBackupTrace("Transport requested non-incremental but already the case, error");
                            this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        }
                        this.backupManagerService.addBackupTrace("data delivered: " + this.mStatus);
                        if (this.mStatus == 0) {
                            this.backupManagerService.addBackupTrace("finishing op on transport");
                            this.mStatus = transport.finishBackup();
                            this.backupManagerService.addBackupTrace("finished: " + this.mStatus);
                        } else if (this.mStatus == -1002) {
                            this.backupManagerService.addBackupTrace("transport rejected package");
                        }
                    } else {
                        this.backupManagerService.addBackupTrace("no data to send");
                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 7, this.mCurrentPackage, 3, null);
                    }
                    if (this.mStatus == 0) {
                        this.mBackupDataName.delete();
                        this.mNewStateName.renameTo(this.mSavedStateName);
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, pkgName, 0);
                        EventLog.writeEvent(EventLogTags.BACKUP_PACKAGE, new Object[]{pkgName, Long.valueOf(size)});
                        this.backupManagerService.logBackupComplete(pkgName);
                    } else if (this.mStatus == -1002) {
                        this.mBackupDataName.delete();
                        this.mNewStateName.delete();
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS);
                        EventLogTags.writeBackupAgentFailure(pkgName, "Transport rejected");
                    } else if (this.mStatus == -1005) {
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, pkgName, -1005);
                        EventLog.writeEvent(EventLogTags.BACKUP_QUOTA_EXCEEDED, pkgName);
                    } else if (this.mStatus == -1006) {
                        Slog.i(TAG, "Transport lost data, retrying package");
                        this.backupManagerService.addBackupTrace("Transport lost data, retrying package:" + pkgName);
                        BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 51, this.mCurrentPackage, 1, null);
                        this.mBackupDataName.delete();
                        this.mSavedStateName.delete();
                        this.mNewStateName.delete();
                        if (!BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(pkgName)) {
                            this.mQueue.add(0, new BackupRequest(pkgName));
                        }
                    } else {
                        BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                    }
                    if (backupData != null) {
                        try {
                            backupData.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (Exception e5) {
                    BackupObserverUtils.sendBackupOnPackageResult(this.mObserver, pkgName, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE);
                    Slog.e(TAG, "Transport error backing up " + pkgName, e5);
                    EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, pkgName);
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    if (backupData != null) {
                        backupData.close();
                    }
                }
                if (this.mStatus != 0) {
                    if (this.mStatus != -1002) {
                        if (this.mStatus == -1006) {
                            nextState = BackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(pkgName) ? BackupState.BACKUP_PM : BackupState.RUNNING_QUEUE;
                        } else if (this.mStatus == -1005) {
                            if (this.mAgentBinder != null) {
                                try {
                                    TransportUtils.checkTransportNotNull(transport);
                                    this.mAgentBinder.doQuotaExceeded(size, transport.getBackupQuota(this.mCurrentPackage.packageName, false));
                                } catch (Exception e6) {
                                    Slog.e(TAG, "Unable to notify about quota exceeded: " + e6.getMessage());
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
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0091, code lost:
        return;
     */
    @GuardedBy("mCancelLock")
    public void handleCancel(boolean cancelAll) {
        String logPackageName;
        this.backupManagerService.removeOperation(this.mEphemeralOpToken);
        synchronized (this.mCancelLock) {
            if (!this.mFinished) {
                this.mCancelAll = cancelAll;
                if (this.mCurrentPackage != null) {
                    logPackageName = this.mCurrentPackage.packageName;
                } else {
                    logPackageName = "no_package_yet";
                }
                Slog.i(TAG, "Cancel backing up " + logPackageName);
                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, logPackageName);
                BackupManagerService backupManagerService2 = this.backupManagerService;
                backupManagerService2.addBackupTrace("cancel of " + logPackageName + ", cancelAll=" + cancelAll);
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 21, this.mCurrentPackage, 2, BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_CANCEL_ALL", this.mCancelAll));
                errorCleanup();
                if (!cancelAll) {
                    executeNextState(this.mQueue.isEmpty() ? BackupState.FINAL : BackupState.RUNNING_QUEUE);
                    this.backupManagerService.dataChangedImpl(this.mCurrentPackage.packageName);
                } else {
                    finalizeBackup();
                }
            }
        }
    }

    private void revertAndEndBackup() {
        long delay;
        this.backupManagerService.addBackupTrace("transport error; reverting");
        try {
            delay = this.mTransportClient.connectOrThrow("PBT.revertAndEndBackup()").requestBackupTime();
        } catch (Exception e) {
            Slog.w(TAG, "Unable to contact transport for recommended backoff: " + e.getMessage());
            delay = 0;
        }
        KeyValueBackupJob.schedule(this.backupManagerService.getContext(), delay, this.backupManagerService.getConstants());
        Iterator<BackupRequest> it = this.mOriginalQueue.iterator();
        while (it.hasNext()) {
            this.backupManagerService.dataChangedImpl(it.next().packageName);
        }
    }

    private void errorCleanup() {
        this.mBackupDataName.delete();
        this.mNewStateName.delete();
        clearAgentState();
    }

    private void clearAgentState() {
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
        synchronized (this.backupManagerService.getCurrentOpLock()) {
            this.backupManagerService.getCurrentOperations().remove(this.mEphemeralOpToken);
            this.mNewState = null;
            this.mBackupData = null;
            this.mSavedState = null;
        }
        if (this.mCurrentPackage.applicationInfo != null) {
            BackupManagerService backupManagerService2 = this.backupManagerService;
            backupManagerService2.addBackupTrace("unbinding " + this.mCurrentPackage.packageName);
            try {
                this.backupManagerService.getActivityManager().unbindBackupAgent(this.mCurrentPackage.applicationInfo);
            } catch (RemoteException e4) {
            }
        }
    }

    private void executeNextState(BackupState nextState) {
        BackupManagerService backupManagerService2 = this.backupManagerService;
        backupManagerService2.addBackupTrace("executeNextState => " + nextState);
        this.mCurrentState = nextState;
        this.backupManagerService.getBackupHandler().sendMessage(this.backupManagerService.getBackupHandler().obtainMessage(20, this));
    }
}
