package com.android.server.backup.keyvalue;

import android.app.IBackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.IBackupCallback;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.ConditionVariable;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.WorkSource;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.DataChangedJournal;
import com.android.server.backup.KeyValueBackupJob;
import com.android.server.backup.TransportManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.internal.Operation;
import com.android.server.backup.remote.RemoteCall;
import com.android.server.backup.remote.RemoteCallable;
import com.android.server.backup.remote.RemoteResult;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.pm.DumpState;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyValueBackupTask implements BackupRestoreTask, Runnable {
    private static final String BLANK_STATE_FILE_NAME = "blank_state";
    @VisibleForTesting
    public static final String NEW_STATE_FILE_SUFFIX = ".new";
    private static final String PM_PACKAGE = "@pm@";
    @VisibleForTesting
    public static final String STAGING_FILE_SUFFIX = ".data";
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static final int THREAD_PRIORITY = 10;
    private IBackupAgent mAgent;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private ParcelFileDescriptor mBackupData;
    private File mBackupDataFile;
    private final UserBackupManagerService mBackupManagerService;
    private final File mBlankStateFile;
    private final ConditionVariable mCancelAcknowledged = new ConditionVariable(false);
    private volatile boolean mCancelled = false;
    private final int mCurrentOpToken;
    private PackageInfo mCurrentPackage;
    private final File mDataDirectory;
    private PerformFullTransportBackupTask mFullBackupTask;
    private boolean mHasDataToBackup;
    private final DataChangedJournal mJournal;
    private ParcelFileDescriptor mNewState;
    private File mNewStateFile;
    private final boolean mNonIncremental;
    private final List<String> mOriginalQueue;
    private final PackageManager mPackageManager;
    private volatile RemoteCall mPendingCall;
    private final List<String> mPendingFullBackups;
    private final List<String> mQueue;
    private final Object mQueueLock;
    private final KeyValueBackupReporter mReporter;
    private ParcelFileDescriptor mSavedState;
    private File mSavedStateFile;
    private final File mStateDirectory;
    private final OnTaskFinishedListener mTaskFinishedListener;
    private final TransportClient mTransportClient;
    private final TransportManager mTransportManager;
    private final int mUserId;
    private final boolean mUserInitiated;

    @Retention(RetentionPolicy.SOURCE)
    private @interface StateTransaction {
        public static final int COMMIT_NEW = 0;
        public static final int DISCARD_ALL = 2;
        public static final int DISCARD_NEW = 1;
    }

    public static KeyValueBackupTask start(UserBackupManagerService backupManagerService, TransportClient transportClient, String transportDirName, List<String> queue, DataChangedJournal dataChangedJournal, IBackupObserver observer, IBackupManagerMonitor monitor, OnTaskFinishedListener listener, List<String> pendingFullBackups, boolean userInitiated, boolean nonIncremental) {
        KeyValueBackupTask task = new KeyValueBackupTask(backupManagerService, transportClient, transportDirName, queue, dataChangedJournal, new KeyValueBackupReporter(backupManagerService, observer, monitor), listener, pendingFullBackups, userInitiated, nonIncremental);
        Thread thread = new Thread(task, "key-value-backup-" + THREAD_COUNT.incrementAndGet());
        thread.start();
        KeyValueBackupReporter.onNewThread(thread.getName());
        return task;
    }

    @VisibleForTesting
    public KeyValueBackupTask(UserBackupManagerService backupManagerService, TransportClient transportClient, String transportDirName, List<String> queue, DataChangedJournal journal, KeyValueBackupReporter reporter, OnTaskFinishedListener taskFinishedListener, List<String> pendingFullBackups, boolean userInitiated, boolean nonIncremental) {
        this.mBackupManagerService = backupManagerService;
        this.mTransportManager = backupManagerService.getTransportManager();
        this.mPackageManager = backupManagerService.getPackageManager();
        this.mTransportClient = transportClient;
        this.mOriginalQueue = queue;
        this.mQueue = new ArrayList(queue);
        this.mJournal = journal;
        this.mReporter = reporter;
        this.mTaskFinishedListener = taskFinishedListener;
        this.mPendingFullBackups = pendingFullBackups;
        this.mUserInitiated = userInitiated;
        this.mNonIncremental = nonIncremental;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        this.mStateDirectory = new File(backupManagerService.getBaseStateDir(), transportDirName);
        this.mDataDirectory = this.mBackupManagerService.getDataDir();
        this.mCurrentOpToken = backupManagerService.generateRandomIntegerToken();
        this.mQueueLock = this.mBackupManagerService.getQueueLock();
        this.mBlankStateFile = new File(this.mStateDirectory, BLANK_STATE_FILE_NAME);
        this.mUserId = backupManagerService.getUserId();
    }

    private void registerTask() {
        this.mBackupManagerService.putOperation(this.mCurrentOpToken, new Operation(0, this, 2));
    }

    private void unregisterTask() {
        this.mBackupManagerService.removeOperation(this.mCurrentOpToken);
    }

    @Override // java.lang.Runnable
    public void run() {
        Process.setThreadPriority(10);
        this.mHasDataToBackup = false;
        int status = 0;
        try {
            startTask();
            while (!this.mQueue.isEmpty() && !this.mCancelled) {
                String packageName = this.mQueue.remove(0);
                try {
                    if ("@pm@".equals(packageName)) {
                        backupPm();
                    } else {
                        backupPackage(packageName);
                    }
                } catch (AgentException e) {
                    if (e.isTransitory()) {
                        this.mBackupManagerService.dataChangedImpl(packageName);
                    }
                }
            }
        } catch (TaskException e2) {
            if (e2.isStateCompromised()) {
                this.mBackupManagerService.resetBackupState(this.mStateDirectory);
            }
            revertTask();
            status = e2.getStatus();
        }
        finishTask(status);
    }

    private int sendDataToTransport(PackageInfo packageInfo) throws AgentException, TaskException {
        try {
            return sendDataToTransport();
        } catch (IOException e) {
            this.mReporter.onAgentDataError(packageInfo.packageName, e);
            throw TaskException.causedBy(e);
        }
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void execute() {
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void operationComplete(long unusedResult) {
    }

    private void startTask() throws TaskException {
        if (!this.mBackupManagerService.isBackupOperationInProgress()) {
            this.mFullBackupTask = createFullBackupTask(this.mPendingFullBackups);
            registerTask();
            if (!this.mQueue.isEmpty() || !this.mPendingFullBackups.isEmpty()) {
                if (this.mQueue.remove("@pm@") || !this.mNonIncremental) {
                    this.mQueue.add(0, "@pm@");
                } else {
                    this.mReporter.onSkipPm();
                }
                this.mReporter.onQueueReady(this.mQueue);
                File pmState = new File(this.mStateDirectory, "@pm@");
                try {
                    IBackupTransport transport = this.mTransportClient.connectOrThrow("KVBT.startTask()");
                    String transportName = transport.name();
                    this.mReporter.onTransportReady(transportName);
                    if (pmState.length() <= 0) {
                        this.mReporter.onInitializeTransport(transportName);
                        this.mBackupManagerService.resetBackupState(this.mStateDirectory);
                        int status = transport.initializeDevice();
                        this.mReporter.onTransportInitialized(status);
                        if (status != 0) {
                            throw TaskException.stateCompromised();
                        }
                    }
                } catch (TaskException e) {
                    throw e;
                } catch (Exception e2) {
                    this.mReporter.onInitializeTransportError(e2);
                    throw TaskException.stateCompromised();
                }
            } else {
                this.mReporter.onEmptyQueueAtStart();
            }
        } else {
            this.mReporter.onSkipBackup();
            throw TaskException.create();
        }
    }

    private PerformFullTransportBackupTask createFullBackupTask(List<String> packages) {
        return new PerformFullTransportBackupTask(this.mBackupManagerService, this.mTransportClient, null, (String[]) packages.toArray(new String[packages.size()]), false, null, new CountDownLatch(1), this.mReporter.getObserver(), this.mReporter.getMonitor(), this.mTaskFinishedListener, this.mUserInitiated);
    }

    private void backupPm() throws TaskException {
        this.mReporter.onStartPackageBackup("@pm@");
        this.mCurrentPackage = new PackageInfo();
        PackageInfo packageInfo = this.mCurrentPackage;
        packageInfo.packageName = "@pm@";
        try {
            extractPmAgentData(packageInfo);
            cleanUpAgentForTransportStatus(sendDataToTransport(this.mCurrentPackage));
        } catch (AgentException | TaskException e) {
            this.mReporter.onExtractPmAgentDataError(e);
            cleanUpAgentForError(e);
            throw TaskException.stateCompromised(e);
        }
    }

    private void backupPackage(String packageName) throws AgentException, TaskException {
        this.mReporter.onStartPackageBackup(packageName);
        this.mCurrentPackage = getPackageForBackup(packageName);
        try {
            extractAgentData(this.mCurrentPackage);
            cleanUpAgentForTransportStatus(sendDataToTransport(this.mCurrentPackage));
        } catch (AgentException | TaskException e) {
            cleanUpAgentForError(e);
            throw e;
        }
    }

    private PackageInfo getPackageForBackup(String packageName) throws AgentException {
        try {
            PackageInfo packageInfo = this.mPackageManager.getPackageInfoAsUser(packageName, DumpState.DUMP_HWFEATURES, this.mUserId);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (!AppBackupUtils.appIsEligibleForBackup(applicationInfo, this.mUserId)) {
                this.mReporter.onPackageNotEligibleForBackup(packageName);
                throw AgentException.permanent();
            } else if (AppBackupUtils.appGetsFullBackup(packageInfo)) {
                this.mReporter.onPackageEligibleForFullBackup(packageName);
                throw AgentException.permanent();
            } else if (!AppBackupUtils.appIsStopped(applicationInfo)) {
                return packageInfo;
            } else {
                this.mReporter.onPackageStopped(packageName);
                throw AgentException.permanent();
            }
        } catch (PackageManager.NameNotFoundException e) {
            this.mReporter.onAgentUnknown(packageName);
            throw AgentException.permanent(e);
        }
    }

    private IBackupAgent bindAgent(PackageInfo packageInfo) throws AgentException {
        String packageName = packageInfo.packageName;
        try {
            IBackupAgent agent = this.mBackupManagerService.bindToAgentSynchronous(packageInfo.applicationInfo, 0);
            if (agent != null) {
                return agent;
            }
            this.mReporter.onAgentError(packageName);
            throw AgentException.transitory();
        } catch (SecurityException e) {
            this.mReporter.onBindAgentError(packageName, e);
            throw AgentException.transitory(e);
        }
    }

    private void finishTask(int status) {
        for (String packageName : this.mQueue) {
            this.mBackupManagerService.dataChangedImpl(packageName);
        }
        DataChangedJournal dataChangedJournal = this.mJournal;
        if (dataChangedJournal != null && !dataChangedJournal.delete()) {
            this.mReporter.onJournalDeleteFailed(this.mJournal);
        }
        long currentToken = this.mBackupManagerService.getCurrentToken();
        if (this.mHasDataToBackup && status == 0 && currentToken == 0) {
            try {
                this.mBackupManagerService.setCurrentToken(this.mTransportClient.connectOrThrow("KVBT.finishTask()").getCurrentRestoreSet());
                this.mBackupManagerService.writeRestoreTokens();
            } catch (Exception e) {
                this.mReporter.onSetCurrentTokenError(e);
            }
        }
        synchronized (this.mQueueLock) {
            this.mBackupManagerService.setBackupRunning(false);
            if (status == -1001) {
                this.mReporter.onTransportNotInitialized();
                try {
                    triggerTransportInitializationLocked();
                } catch (Exception e2) {
                    this.mReporter.onPendingInitializeTransportError(e2);
                    status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                }
            }
        }
        unregisterTask();
        this.mReporter.onTaskFinished();
        if (this.mCancelled) {
            this.mCancelAcknowledged.open();
        }
        if (this.mCancelled || status != 0 || this.mFullBackupTask == null || this.mPendingFullBackups.isEmpty()) {
            PerformFullTransportBackupTask performFullTransportBackupTask = this.mFullBackupTask;
            if (performFullTransportBackupTask != null) {
                performFullTransportBackupTask.unregisterTask();
            }
            this.mTaskFinishedListener.onFinished("KVBT.finishTask()");
            this.mReporter.onBackupFinished(getBackupFinishedStatus(this.mCancelled, status));
            this.mBackupManagerService.getWakelock().release();
            return;
        }
        this.mReporter.onStartFullBackup(this.mPendingFullBackups);
        new Thread(this.mFullBackupTask, "full-transport-requested").start();
    }

    private int getBackupFinishedStatus(boolean cancelled, int transportStatus) {
        if (cancelled) {
            return -2003;
        }
        if (transportStatus == -1005 || transportStatus == -1002 || transportStatus == 0) {
            return 0;
        }
        return JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
    }

    @GuardedBy({"mQueueLock"})
    private void triggerTransportInitializationLocked() throws Exception {
        this.mBackupManagerService.getPendingInits().add(this.mTransportClient.connectOrThrow("KVBT.triggerTransportInitializationLocked").name());
        deletePmStateFile();
        this.mBackupManagerService.backupNow();
    }

    private void deletePmStateFile() {
        new File(this.mStateDirectory, "@pm@").delete();
    }

    private void extractPmAgentData(PackageInfo packageInfo) throws AgentException, TaskException {
        Preconditions.checkArgument(packageInfo.packageName.equals("@pm@"));
        this.mAgent = IBackupAgent.Stub.asInterface(this.mBackupManagerService.makeMetadataAgent().onBind());
        extractAgentData(packageInfo, this.mAgent);
    }

    private void extractAgentData(PackageInfo packageInfo) throws AgentException, TaskException {
        this.mBackupManagerService.setWorkSource(new WorkSource(packageInfo.applicationInfo.uid));
        try {
            this.mAgent = bindAgent(packageInfo);
            extractAgentData(packageInfo, this.mAgent);
        } finally {
            this.mBackupManagerService.setWorkSource(null);
        }
    }

    private void extractAgentData(PackageInfo packageInfo, IBackupAgent agent) throws AgentException, TaskException {
        String packageName = packageInfo.packageName;
        this.mReporter.onExtractAgentData(packageName);
        this.mSavedStateFile = new File(this.mStateDirectory, packageName);
        File file = this.mDataDirectory;
        this.mBackupDataFile = new File(file, packageName + STAGING_FILE_SUFFIX);
        File file2 = this.mStateDirectory;
        this.mNewStateFile = new File(file2, packageName + NEW_STATE_FILE_SUFFIX);
        this.mReporter.onAgentFilesReady(this.mBackupDataFile);
        try {
            this.mSavedState = ParcelFileDescriptor.open(this.mNonIncremental ? this.mBlankStateFile : this.mSavedStateFile, 402653184);
            this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataFile, 1006632960);
            this.mNewState = ParcelFileDescriptor.open(this.mNewStateFile, 1006632960);
            if (this.mUserId == 0 && !SELinux.restorecon(this.mBackupDataFile)) {
                this.mReporter.onRestoreconFailed(this.mBackupDataFile);
            }
            IBackupTransport transport = this.mTransportClient.connectOrThrow("KVBT.extractAgentData()");
            checkAgentResult(packageInfo, remoteCall(new RemoteCallable(agent, transport.getBackupQuota(packageName, false), transport.getTransportFlags()) {
                /* class com.android.server.backup.keyvalue.$$Lambda$KeyValueBackupTask$NN2H32cNizGxrUxqHgqPqGldNsA */
                private final /* synthetic */ IBackupAgent f$1;
                private final /* synthetic */ long f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r5;
                }

                @Override // com.android.server.backup.remote.RemoteCallable
                public final void call(Object obj) {
                    KeyValueBackupTask.this.lambda$extractAgentData$0$KeyValueBackupTask(this.f$1, this.f$2, this.f$3, (IBackupCallback) obj);
                }
            }, this.mAgentTimeoutParameters.getKvBackupAgentTimeoutMillis(), "doBackup()"));
        } catch (Exception e) {
            this.mReporter.onCallAgentDoBackupError(packageName, false, e);
            if (0 != 0) {
                throw AgentException.transitory(e);
            }
            throw TaskException.create();
        }
    }

    public /* synthetic */ void lambda$extractAgentData$0$KeyValueBackupTask(IBackupAgent agent, long quota, int transportFlags, IBackupCallback callback) throws RemoteException {
        agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, quota, callback, transportFlags);
    }

    private void checkAgentResult(PackageInfo packageInfo, RemoteResult result) throws AgentException, TaskException {
        boolean z = true;
        if (result == RemoteResult.FAILED_THREAD_INTERRUPTED) {
            this.mCancelled = true;
            this.mReporter.onAgentCancelled(packageInfo);
            throw TaskException.create();
        } else if (result == RemoteResult.FAILED_CANCELLED) {
            this.mReporter.onAgentCancelled(packageInfo);
            throw TaskException.create();
        } else if (result != RemoteResult.FAILED_TIMED_OUT) {
            Preconditions.checkState(result.isPresent());
            long resultCode = result.get();
            if (resultCode != -1) {
                if (resultCode != 0) {
                    z = false;
                }
                Preconditions.checkState(z);
                return;
            }
            this.mReporter.onAgentResultError(packageInfo);
            throw AgentException.transitory();
        } else {
            this.mReporter.onAgentTimedOut(packageInfo);
            throw AgentException.transitory();
        }
    }

    private void agentFail(IBackupAgent agent, String message) {
        try {
            agent.fail(message);
        } catch (Exception e) {
            this.mReporter.onFailAgentError(this.mCurrentPackage.packageName);
        }
    }

    private String SHA1Checksum(byte[] input) {
        try {
            byte[] checksum = MessageDigest.getInstance("SHA-1").digest(input);
            StringBuilder string = new StringBuilder(checksum.length * 2);
            for (byte item : checksum) {
                string.append(Integer.toHexString(item));
            }
            return string.toString();
        } catch (NoSuchAlgorithmException e) {
            this.mReporter.onDigestError(e);
            return "00";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        $closeResource(r4, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
        throw r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        $closeResource(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0060, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x008b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008c, code lost:
        $closeResource(r4, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008f, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0092, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0093, code lost:
        $closeResource(r4, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0096, code lost:
        throw r6;
     */
    private void writeWidgetPayloadIfAppropriate(FileDescriptor fd, String pkgName) throws IOException {
        byte[] widgetState = AppWidgetBackupBridge.getWidgetState(pkgName, this.mUserId);
        File file = this.mStateDirectory;
        File widgetFile = new File(file, pkgName + "_widget");
        boolean priorStateExists = widgetFile.exists();
        if (priorStateExists || widgetState != null) {
            this.mReporter.onWriteWidgetData(priorStateExists, widgetState);
            String newChecksum = null;
            if (widgetState != null) {
                newChecksum = SHA1Checksum(widgetState);
                if (priorStateExists) {
                    FileInputStream fin = new FileInputStream(widgetFile);
                    DataInputStream in = new DataInputStream(fin);
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
                DataOutputStream stateOut = new DataOutputStream(fout);
                stateOut.writeUTF(newChecksum);
                $closeResource(null, stateOut);
                $closeResource(null, fout);
                out.writeEntityHeader(UserBackupManagerService.KEY_WIDGET_STATE, widgetState.length);
                out.writeEntityData(widgetState, widgetState.length);
                return;
            }
            out.writeEntityHeader(UserBackupManagerService.KEY_WIDGET_STATE, -1);
            widgetFile.delete();
        }
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

    private int sendDataToTransport() throws AgentException, TaskException, IOException {
        boolean nonIncremental = true;
        Preconditions.checkState(this.mBackupData != null);
        checkBackupData(this.mCurrentPackage.applicationInfo, this.mBackupDataFile);
        String packageName = this.mCurrentPackage.packageName;
        writeWidgetPayloadIfAppropriate(this.mBackupData.getFileDescriptor(), packageName);
        if (this.mSavedStateFile.length() != 0) {
            nonIncremental = false;
        }
        int status = transportPerformBackup(this.mCurrentPackage, this.mBackupDataFile, nonIncremental);
        handleTransportStatus(status, packageName, this.mBackupDataFile.length());
        return status;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0055, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        if (r3 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005b, code lost:
        throw r5;
     */
    private int transportPerformBackup(PackageInfo packageInfo, File backupDataFile, boolean nonIncremental) throws TaskException {
        String packageName = packageInfo.packageName;
        if (backupDataFile.length() <= 0) {
            this.mReporter.onEmptyData(packageInfo);
            return 0;
        }
        this.mHasDataToBackup = true;
        try {
            ParcelFileDescriptor backupData = ParcelFileDescriptor.open(backupDataFile, 268435456);
            IBackupTransport transport = this.mTransportClient.connectOrThrow("KVBT.transportPerformBackup()");
            this.mReporter.onTransportPerformBackup(packageName);
            int status = transport.performBackup(packageInfo, backupData, getPerformBackupFlags(this.mUserInitiated, nonIncremental));
            if (status == 0) {
                status = transport.finishBackup();
            }
            if (backupData != null) {
                $closeResource(null, backupData);
            }
            if (!nonIncremental || status != -1006) {
                return status;
            }
            this.mReporter.onPackageBackupNonIncrementalAndNonIncrementalRequired(packageName);
            throw TaskException.create();
        } catch (Exception e) {
            this.mReporter.onPackageBackupTransportError(packageName, e);
            throw TaskException.causedBy(e);
        }
    }

    private void handleTransportStatus(int status, String packageName, long size) throws TaskException, AgentException {
        if (status == 0) {
            this.mReporter.onPackageBackupComplete(packageName, size);
        } else if (status == -1006) {
            this.mReporter.onPackageBackupNonIncrementalRequired(this.mCurrentPackage);
            this.mQueue.add(0, packageName);
        } else if (status == -1002) {
            this.mReporter.onPackageBackupRejected(packageName);
            throw AgentException.permanent();
        } else if (status == -1005) {
            this.mReporter.onPackageBackupQuotaExceeded(packageName);
            agentDoQuotaExceeded(this.mAgent, packageName, size);
            throw AgentException.permanent();
        } else {
            this.mReporter.onPackageBackupTransportFailure(packageName);
            throw TaskException.forStatus(status);
        }
    }

    private void agentDoQuotaExceeded(IBackupAgent agent, String packageName, long size) {
        if (agent != null) {
            try {
                remoteCall(new RemoteCallable(agent, size, this.mTransportClient.connectOrThrow("KVBT.agentDoQuotaExceeded()").getBackupQuota(packageName, false)) {
                    /* class com.android.server.backup.keyvalue.$$Lambda$KeyValueBackupTask$XyLNsBl81SJjG_2y6Nb3ueV0ZY */
                    private final /* synthetic */ IBackupAgent f$0;
                    private final /* synthetic */ long f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                        this.f$2 = r4;
                    }

                    @Override // com.android.server.backup.remote.RemoteCallable
                    public final void call(Object obj) {
                        this.f$0.doQuotaExceeded(this.f$1, this.f$2, (IBackupCallback) obj);
                    }
                }, this.mAgentTimeoutParameters.getQuotaExceededTimeoutMillis(), "doQuotaExceeded()");
            } catch (Exception e) {
                this.mReporter.onAgentDoQuotaExceededError(e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005d, code lost:
        if (r0 != null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005f, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0062, code lost:
        throw r2;
     */
    private void checkBackupData(ApplicationInfo applicationInfo, File backupDataFile) throws IOException, AgentException {
        if (applicationInfo != null && (applicationInfo.flags & 1) == 0) {
            ParcelFileDescriptor backupData = ParcelFileDescriptor.open(backupDataFile, 268435456);
            BackupDataInput backupDataInput = new BackupDataInput(backupData.getFileDescriptor());
            while (backupDataInput.readNextHeader()) {
                String key = backupDataInput.getKey();
                if (key != null) {
                    if (key.charAt(0) >= 65280) {
                        this.mReporter.onAgentIllegalKey(this.mCurrentPackage, key);
                        IBackupAgent iBackupAgent = this.mAgent;
                        agentFail(iBackupAgent, "Illegal backup key: " + key);
                        throw AgentException.permanent();
                    }
                }
                backupDataInput.skipEntityData();
            }
            $closeResource(null, backupData);
        }
    }

    private int getPerformBackupFlags(boolean userInitiated, boolean nonIncremental) {
        int incrementalFlag;
        if (nonIncremental) {
            incrementalFlag = 4;
        } else {
            incrementalFlag = 2;
        }
        return (userInitiated ? 1 : 0) | incrementalFlag;
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void handleCancel(boolean cancelAll) {
        Preconditions.checkArgument(cancelAll, "Can't partially cancel a key-value backup task");
        markCancel();
        waitCancel();
    }

    @VisibleForTesting
    public void markCancel() {
        this.mReporter.onCancel();
        this.mCancelled = true;
        RemoteCall pendingCall = this.mPendingCall;
        if (pendingCall != null) {
            pendingCall.cancel();
        }
    }

    @VisibleForTesting
    public void waitCancel() {
        this.mCancelAcknowledged.block();
    }

    private void revertTask() {
        long delay;
        this.mReporter.onRevertTask();
        try {
            delay = this.mTransportClient.connectOrThrow("KVBT.revertTask()").requestBackupTime();
        } catch (Exception e) {
            this.mReporter.onTransportRequestBackupTimeError(e);
            delay = 0;
        }
        KeyValueBackupJob.schedule(this.mBackupManagerService.getUserId(), this.mBackupManagerService.getContext(), delay, this.mBackupManagerService.getConstants());
        for (String packageName : this.mOriginalQueue) {
            this.mBackupManagerService.dataChangedImpl(packageName);
        }
    }

    private void cleanUpAgentForError(BackupException exception) {
        cleanUpAgent(1);
    }

    private void cleanUpAgentForTransportStatus(int status) {
        if (status == -1006) {
            cleanUpAgent(2);
        } else if (status == 0) {
            cleanUpAgent(0);
        } else {
            throw new AssertionError();
        }
    }

    private void cleanUpAgent(int stateTransaction) {
        applyStateTransaction(stateTransaction);
        File file = this.mBackupDataFile;
        if (file != null) {
            file.delete();
        }
        this.mBlankStateFile.delete();
        this.mSavedStateFile = null;
        this.mBackupDataFile = null;
        this.mNewStateFile = null;
        tryCloseFileDescriptor(this.mSavedState, "old state");
        tryCloseFileDescriptor(this.mBackupData, "backup data");
        tryCloseFileDescriptor(this.mNewState, "new state");
        this.mSavedState = null;
        this.mBackupData = null;
        this.mNewState = null;
        if (this.mCurrentPackage.applicationInfo != null) {
            this.mBackupManagerService.unbindAgent(this.mCurrentPackage.applicationInfo);
        }
        this.mAgent = null;
    }

    private void applyStateTransaction(int stateTransaction) {
        if (stateTransaction == 0) {
            this.mNewStateFile.renameTo(this.mSavedStateFile);
        } else if (stateTransaction == 1) {
            File file = this.mNewStateFile;
            if (file != null) {
                file.delete();
            }
        } else if (stateTransaction == 2) {
            this.mSavedStateFile.delete();
            this.mNewStateFile.delete();
        } else {
            throw new IllegalArgumentException("Unknown state transaction " + stateTransaction);
        }
    }

    private void tryCloseFileDescriptor(Closeable closeable, String logName) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                this.mReporter.onCloseFileDescriptorError(logName);
            }
        }
    }

    private RemoteResult remoteCall(RemoteCallable<IBackupCallback> remoteCallable, long timeoutMs, String logIdentifier) throws RemoteException {
        this.mPendingCall = new RemoteCall(this.mCancelled, remoteCallable, timeoutMs);
        RemoteResult result = this.mPendingCall.call();
        this.mReporter.onRemoteCallReturned(result, logIdentifier);
        this.mPendingCall = null;
        return result;
    }
}
