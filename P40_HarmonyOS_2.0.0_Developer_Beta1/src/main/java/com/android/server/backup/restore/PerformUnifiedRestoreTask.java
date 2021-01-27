package com.android.server.backup.restore;

import android.app.IBackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.app.backup.RestoreDescription;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.BackupUtils;
import com.android.server.backup.PackageManagerBackupAgent;
import com.android.server.backup.TransportManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.keyvalue.KeyValueBackupTask;
import com.android.server.backup.transport.TransportClient;
import com.android.server.backup.utils.AppBackupUtils;
import com.android.server.backup.utils.BackupManagerMonitorUtils;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libcore.io.IoUtils;

public class PerformUnifiedRestoreTask implements BackupRestoreTask {
    private UserBackupManagerService backupManagerService;
    private List<PackageInfo> mAcceptSet;
    private IBackupAgent mAgent;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    ParcelFileDescriptor mBackupData;
    private File mBackupDataName;
    private int mCount;
    private PackageInfo mCurrentPackage;
    private boolean mDidLaunch;
    private final int mEphemeralOpToken;
    private boolean mFinished;
    private boolean mIsSystemRestore;
    private final OnTaskFinishedListener mListener;
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
    private final TransportClient mTransportClient;
    private final TransportManager mTransportManager;
    private final int mUserId;
    private byte[] mWidgetData;

    public PerformUnifiedRestoreTask(UserBackupManagerService backupManagerService2, TransportClient transportClient, IRestoreObserver observer, IBackupManagerMonitor monitor, long restoreSetToken, PackageInfo targetPackage, int pmToken, boolean isFullSystemRestore, String[] filterSet, OnTaskFinishedListener listener) {
        String[] filterSet2;
        this.backupManagerService = backupManagerService2;
        this.mUserId = backupManagerService2.getUserId();
        this.mTransportManager = backupManagerService2.getTransportManager();
        this.mEphemeralOpToken = backupManagerService2.generateRandomIntegerToken();
        this.mTransportClient = transportClient;
        this.mObserver = observer;
        this.mMonitor = monitor;
        this.mToken = restoreSetToken;
        this.mPmToken = pmToken;
        this.mTargetPackage = targetPackage;
        this.mIsSystemRestore = isFullSystemRestore;
        this.mFinished = false;
        this.mDidLaunch = false;
        this.mListener = listener;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
        if (targetPackage != null) {
            this.mAcceptSet = new ArrayList();
            this.mAcceptSet.add(targetPackage);
            return;
        }
        if (filterSet == null) {
            filterSet2 = packagesToNames(PackageManagerBackupAgent.getStorableApplications(backupManagerService2.getPackageManager(), this.mUserId));
            Slog.i(BackupManagerService.TAG, "Full restore; asking about " + filterSet2.length + " apps");
        } else {
            filterSet2 = filterSet;
        }
        this.mAcceptSet = new ArrayList(filterSet2.length);
        boolean hasSettings = false;
        boolean hasSystem = false;
        for (int i = 0; i < filterSet2.length; i++) {
            try {
                PackageInfo info = backupManagerService2.getPackageManager().getPackageInfoAsUser(filterSet2[i], 0, this.mUserId);
                if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(info.packageName)) {
                    hasSystem = true;
                } else if (UserBackupManagerService.SETTINGS_PACKAGE.equals(info.packageName)) {
                    hasSettings = true;
                } else if (AppBackupUtils.appIsEligibleForBackup(info.applicationInfo, this.mUserId)) {
                    this.mAcceptSet.add(info);
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        if (hasSystem) {
            try {
                this.mAcceptSet.add(0, backupManagerService2.getPackageManager().getPackageInfoAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, this.mUserId));
            } catch (PackageManager.NameNotFoundException e2) {
            }
        }
        if (hasSettings) {
            try {
                this.mAcceptSet.add(backupManagerService2.getPackageManager().getPackageInfoAsUser(UserBackupManagerService.SETTINGS_PACKAGE, 0, this.mUserId));
            } catch (PackageManager.NameNotFoundException e3) {
            }
        }
    }

    private String[] packagesToNames(List<PackageInfo> apps) {
        int N = apps.size();
        String[] names = new String[N];
        for (int i = 0; i < N; i++) {
            names[i] = apps.get(i).packageName;
        }
        return names;
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void execute() {
        switch (this.mState) {
            case INITIAL:
                startRestore();
                return;
            case RUNNING_QUEUE:
                dispatchNextRestore();
                return;
            case RESTORE_KEYVALUE:
                restoreKeyValue();
                return;
            case RESTORE_FULL:
                restoreFull();
                return;
            case RESTORE_FINISHED:
                restoreFinished();
                return;
            case FINAL:
                if (!this.mFinished) {
                    finalizeRestore();
                } else {
                    Slog.e(BackupManagerService.TAG, "Duplicate finish");
                }
                this.mFinished = true;
                return;
            default:
                return;
        }
    }

    private void startRestore() {
        sendStartRestore(this.mAcceptSet.size());
        if (this.mIsSystemRestore) {
            AppWidgetBackupBridge.restoreStarting(this.mUserId);
        }
        try {
            this.mStateDir = new File(this.backupManagerService.getBaseStateDir(), this.mTransportManager.getTransportDirName(this.mTransportClient.getTransportComponent()));
            PackageInfo pmPackage = new PackageInfo();
            pmPackage.packageName = UserBackupManagerService.PACKAGE_MANAGER_SENTINEL;
            this.mAcceptSet.add(0, pmPackage);
            IBackupTransport transport = this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask.startRestore()");
            this.mStatus = transport.startRestore(this.mToken, (PackageInfo[]) this.mAcceptSet.toArray(new PackageInfo[0]));
            if (this.mStatus != 0) {
                Slog.e(BackupManagerService.TAG, "Transport error " + this.mStatus + "; no restore possible");
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                executeNextState(UnifiedRestoreState.FINAL);
                return;
            }
            RestoreDescription desc = transport.nextRestorePackage();
            if (desc == null) {
                Slog.e(BackupManagerService.TAG, "No restore metadata available; halting");
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 22, this.mCurrentPackage, 3, null);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                executeNextState(UnifiedRestoreState.FINAL);
            } else if (!UserBackupManagerService.PACKAGE_MANAGER_SENTINEL.equals(desc.getPackageName())) {
                Slog.e(BackupManagerService.TAG, "Required package metadata but got " + desc.getPackageName());
                this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 23, this.mCurrentPackage, 3, null);
                this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                executeNextState(UnifiedRestoreState.FINAL);
            } else {
                this.mCurrentPackage = new PackageInfo();
                this.mCurrentPackage.packageName = UserBackupManagerService.PACKAGE_MANAGER_SENTINEL;
                this.mPmAgent = this.backupManagerService.makeMetadataAgent(null);
                this.mAgent = IBackupAgent.Stub.asInterface(this.mPmAgent.onBind());
                initiateOneRestore(this.mCurrentPackage, 0);
                this.backupManagerService.getBackupHandler().removeMessages(18);
                if (!this.mPmAgent.hasMetadata()) {
                    Slog.e(BackupManagerService.TAG, "PM agent has no metadata, so not restoring");
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 24, this.mCurrentPackage, 3, null);
                    EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, UserBackupManagerService.PACKAGE_MANAGER_SENTINEL, "Package manager restore metadata missing");
                    this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    this.backupManagerService.getBackupHandler().removeMessages(20, this);
                    executeNextState(UnifiedRestoreState.FINAL);
                }
            }
        } catch (Exception e) {
            Slog.e(BackupManagerService.TAG, "Unable to contact transport for restore: " + e.getMessage());
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 25, null, 1, null);
            this.mStatus = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            this.backupManagerService.getBackupHandler().removeMessages(20, this);
            executeNextState(UnifiedRestoreState.FINAL);
        }
    }

    private void dispatchNextRestore() {
        UnifiedRestoreState nextState;
        UnifiedRestoreState nextState2 = UnifiedRestoreState.FINAL;
        try {
            this.mRestoreDescription = this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask.dispatchNextRestore()").nextRestorePackage();
            String pkgName = this.mRestoreDescription != null ? this.mRestoreDescription.getPackageName() : null;
            if (pkgName == null) {
                Slog.e(BackupManagerService.TAG, "Failure getting next package name");
                EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                nextState2 = UnifiedRestoreState.FINAL;
            } else if (this.mRestoreDescription == RestoreDescription.NO_MORE_PACKAGES) {
                Slog.v(BackupManagerService.TAG, "No more packages; finishing restore");
                EventLog.writeEvent((int) EventLogTags.RESTORE_SUCCESS, Integer.valueOf(this.mCount), Integer.valueOf((int) (SystemClock.elapsedRealtime() - this.mStartRealtime)));
                executeNextState(UnifiedRestoreState.FINAL);
            } else {
                Slog.i(BackupManagerService.TAG, "Next restore package: " + this.mRestoreDescription);
                sendOnRestorePackage(pkgName);
                PackageManagerBackupAgent.Metadata metaInfo = this.mPmAgent.getRestoredMetadata(pkgName);
                if (metaInfo == null) {
                    Slog.e(BackupManagerService.TAG, "No metadata for " + pkgName);
                    EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, pkgName, "Package metadata missing");
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                    return;
                }
                try {
                    this.mCurrentPackage = this.backupManagerService.getPackageManager().getPackageInfoAsUser(pkgName, DumpState.DUMP_HWFEATURES, this.mUserId);
                    if (metaInfo.versionCode > this.mCurrentPackage.getLongVersionCode()) {
                        if ((this.mCurrentPackage.applicationInfo.flags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0) {
                            String message = "Source version " + metaInfo.versionCode + " > installed version " + this.mCurrentPackage.getLongVersionCode();
                            Slog.w(BackupManagerService.TAG, "Package " + pkgName + ": " + message);
                            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 27, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra(BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_RESTORE_VERSION", metaInfo.versionCode), "android.app.backup.extra.LOG_RESTORE_ANYWAY", false));
                            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, pkgName, message);
                            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                            return;
                        }
                        Slog.v(BackupManagerService.TAG, "Source version " + metaInfo.versionCode + " > installed version " + this.mCurrentPackage.getLongVersionCode() + " but restoreAnyVersion");
                        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 27, this.mCurrentPackage, 3, BackupManagerMonitorUtils.putMonitoringExtra(BackupManagerMonitorUtils.putMonitoringExtra((Bundle) null, "android.app.backup.extra.LOG_RESTORE_VERSION", metaInfo.versionCode), "android.app.backup.extra.LOG_RESTORE_ANYWAY", true));
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
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(BackupManagerService.TAG, "Package not present: " + pkgName);
                    this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 26, this.mCurrentPackage, 3, null);
                    EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, pkgName, "Package missing on device");
                    executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
                }
            }
        } catch (Exception e2) {
            Slog.e(BackupManagerService.TAG, "Can't get next restore target from transport; halting: " + e2.getMessage());
            EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
            nextState2 = UnifiedRestoreState.FINAL;
        } finally {
            executeNextState(nextState2);
        }
    }

    private void restoreKeyValue() {
        String packageName = this.mCurrentPackage.packageName;
        if (this.mCurrentPackage.applicationInfo.backupAgentName == null || "".equals(this.mCurrentPackage.applicationInfo.backupAgentName)) {
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 28, this.mCurrentPackage, 2, null);
            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, packageName, "Package has no agent");
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            return;
        }
        PackageManagerBackupAgent.Metadata metaInfo = this.mPmAgent.getRestoredMetadata(packageName);
        if (!BackupUtils.signaturesMatch(metaInfo.sigHashes, this.mCurrentPackage, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class))) {
            Slog.w(BackupManagerService.TAG, "Signature mismatch restoring " + packageName);
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 29, this.mCurrentPackage, 3, null);
            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, packageName, "Signature mismatch");
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            return;
        }
        this.mAgent = this.backupManagerService.bindToAgentSynchronous(this.mCurrentPackage.applicationInfo, 0);
        if (this.mAgent == null) {
            Slog.w(BackupManagerService.TAG, "Can't find backup agent for " + packageName);
            this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 30, this.mCurrentPackage, 3, null);
            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, packageName, "Restore agent missing");
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
            return;
        }
        this.mDidLaunch = true;
        try {
            initiateOneRestore(this.mCurrentPackage, metaInfo.versionCode);
            this.mCount++;
        } catch (Exception e) {
            Slog.e(BackupManagerService.TAG, "Error when attempting restore: " + e.toString());
            keyValueAgentErrorCleanup(false);
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }
    }

    /* access modifiers changed from: package-private */
    public void initiateOneRestore(PackageInfo app, long appVersionCode) {
        ParcelFileDescriptor stage;
        String packageName = app.packageName;
        Slog.d(BackupManagerService.TAG, "initiateOneRestore packageName=" + packageName);
        File dataDir = this.backupManagerService.getDataDir();
        this.mBackupDataName = new File(dataDir, packageName + ".restore");
        File dataDir2 = this.backupManagerService.getDataDir();
        this.mStageName = new File(dataDir2, packageName + ".stage");
        File file = this.mStateDir;
        this.mNewStateName = new File(file, packageName + KeyValueBackupTask.NEW_STATE_FILE_SUFFIX);
        this.mSavedStateName = new File(this.mStateDir, packageName);
        boolean staging = packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME) ^ true;
        File downloadFile = staging ? this.mStageName : this.mBackupDataName;
        try {
            IBackupTransport transport = this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask.initiateOneRestore()");
            ParcelFileDescriptor stage2 = ParcelFileDescriptor.open(downloadFile, 1006632960);
            if (transport.getRestoreData(stage2) != 0) {
                Slog.e(BackupManagerService.TAG, "Error getting restore data for " + packageName);
                EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                stage2.close();
                downloadFile.delete();
                executeNextState(UnifiedRestoreState.FINAL);
                return;
            }
            if (staging) {
                stage2.close();
                ParcelFileDescriptor stage3 = ParcelFileDescriptor.open(downloadFile, 268435456);
                this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
                BackupDataInput in = new BackupDataInput(stage3.getFileDescriptor());
                BackupDataOutput out = new BackupDataOutput(this.mBackupData.getFileDescriptor());
                byte[] buffer = new byte[8192];
                while (in.readNextHeader()) {
                    String key = in.getKey();
                    int size = in.getDataSize();
                    if (key.equals(UserBackupManagerService.KEY_WIDGET_STATE)) {
                        Slog.i(BackupManagerService.TAG, "Restoring widget state for " + packageName);
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
                stage = stage3;
            } else {
                stage = stage2;
            }
            stage.close();
            this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 268435456);
            this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
            this.backupManagerService.prepareOperationTimeout(this.mEphemeralOpToken, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis(), this, 1);
            this.mAgent.doRestore(this.mBackupData, appVersionCode, this.mNewState, this.mEphemeralOpToken, this.backupManagerService.getBackupManagerBinder());
        } catch (Exception e) {
            Slog.e(BackupManagerService.TAG, "Unable to call app for restore: " + packageName, e);
            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, packageName, e.toString());
            keyValueAgentErrorCleanup(false);
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
        Slog.d(BackupManagerService.TAG, "restoreFinished packageName=" + this.mCurrentPackage.packageName);
        try {
            this.backupManagerService.prepareOperationTimeout(this.mEphemeralOpToken, this.mAgentTimeoutParameters.getRestoreAgentFinishedTimeoutMillis(), this, 1);
            this.mAgent.doRestoreFinished(this.mEphemeralOpToken, this.backupManagerService.getBackupManagerBinder());
        } catch (Exception e) {
            String packageName = this.mCurrentPackage.packageName;
            Slog.e(BackupManagerService.TAG, "Unable to finalize restore of " + packageName);
            EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, packageName, e.toString());
            keyValueAgentErrorCleanup(true);
            executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
        }
    }

    /* access modifiers changed from: package-private */
    public class StreamFeederThread extends RestoreEngine implements Runnable, BackupRestoreTask {
        final String TAG = "StreamFeederThread";
        FullRestoreEngine mEngine;
        ParcelFileDescriptor[] mEnginePipes;
        FullRestoreEngineThread mEngineThread;
        private final int mEphemeralOpToken;
        ParcelFileDescriptor[] mTransportPipes;

        public StreamFeederThread() throws IOException {
            this.mEphemeralOpToken = PerformUnifiedRestoreTask.this.backupManagerService.generateRandomIntegerToken();
            this.mTransportPipes = ParcelFileDescriptor.createPipe();
            this.mEnginePipes = ParcelFileDescriptor.createPipe();
            setRunning(true);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:67:0x025c, code lost:
            if (r2 == -1000) goto L_0x025e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x025e, code lost:
            r0 = com.android.server.backup.restore.UnifiedRestoreState.FINAL;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x0263, code lost:
            r0 = com.android.server.backup.restore.UnifiedRestoreState.RUNNING_QUEUE;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x02fd, code lost:
            if (r2 == -1000) goto L_0x025e;
         */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x037c  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x01fe  */
        /* JADX WARNING: Removed duplicated region for block: B:58:0x0200  */
        /* JADX WARNING: Removed duplicated region for block: B:62:0x0221 A[SYNTHETIC, Splitter:B:62:0x0221] */
        /* JADX WARNING: Removed duplicated region for block: B:74:0x02b8  */
        /* JADX WARNING: Removed duplicated region for block: B:75:0x02ba  */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x02c2 A[SYNTHETIC, Splitter:B:78:0x02c2] */
        /* JADX WARNING: Removed duplicated region for block: B:89:0x0332  */
        /* JADX WARNING: Removed duplicated region for block: B:92:0x0338 A[SYNTHETIC, Splitter:B:92:0x0338] */
        @Override // java.lang.Runnable
        public void run() {
            IBackupTransport iBackupTransport;
            UnifiedRestoreState nextState;
            int status;
            UnifiedRestoreState nextState2;
            Exception e;
            IBackupTransport transport;
            ParcelFileDescriptor eWriteEnd;
            UnifiedRestoreState unifiedRestoreState = UnifiedRestoreState.RUNNING_QUEUE;
            int status2 = 0;
            EventLog.writeEvent((int) EventLogTags.FULL_RESTORE_PACKAGE, PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
            this.mEngine = new FullRestoreEngine(PerformUnifiedRestoreTask.this.backupManagerService, this, null, PerformUnifiedRestoreTask.this.mMonitor, PerformUnifiedRestoreTask.this.mCurrentPackage, false, false, this.mEphemeralOpToken, false);
            this.mEngineThread = new FullRestoreEngineThread(this.mEngine, this.mEnginePipes[0]);
            ParcelFileDescriptor eWriteEnd2 = this.mEnginePipes[1];
            ParcelFileDescriptor[] parcelFileDescriptorArr = this.mTransportPipes;
            ParcelFileDescriptor tReadEnd = parcelFileDescriptorArr[0];
            ParcelFileDescriptor tWriteEnd = parcelFileDescriptorArr[1];
            int bufferSize = 32768;
            byte[] buffer = new byte[32768];
            FileOutputStream engineOut = new FileOutputStream(eWriteEnd2.getFileDescriptor());
            FileInputStream transportIn = new FileInputStream(tReadEnd.getFileDescriptor());
            new Thread(this.mEngineThread, "unified-restore-engine").start();
            try {
                IBackupTransport transport2 = PerformUnifiedRestoreTask.this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask$StreamFeederThread.run()");
                while (true) {
                    if (status2 != 0) {
                        break;
                    }
                    int result = transport2.getNextFullRestoreDataChunk(tWriteEnd);
                    if (result > 0) {
                        if (result > bufferSize) {
                            bufferSize = result;
                            try {
                                buffer = new byte[bufferSize];
                            } catch (IOException e2) {
                                Slog.e("StreamFeederThread", "Unable to route data for restore");
                                EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, PerformUnifiedRestoreTask.this.mCurrentPackage.packageName, "I/O error on pipes");
                                status = -1003;
                                IoUtils.closeQuietly(this.mEnginePipes[1]);
                                IoUtils.closeQuietly(this.mTransportPipes[0]);
                                IoUtils.closeQuietly(this.mTransportPipes[1]);
                                this.mEngineThread.waitForResult();
                                IoUtils.closeQuietly(this.mEnginePipes[0]);
                                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                                if (-1003 != 0) {
                                    try {
                                        PerformUnifiedRestoreTask.this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask$StreamFeederThread.run()").abortFullRestore();
                                    } catch (Exception e3) {
                                        Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e3.getMessage());
                                        status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                    }
                                    PerformUnifiedRestoreTask.this.backupManagerService.clearApplicationDataAfterRestoreFailure(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                                }
                                nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                                PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                                PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                                PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                                setRunning(false);
                            } catch (Exception e4) {
                                e = e4;
                                try {
                                    Slog.e("StreamFeederThread", "Transport failed during restore: " + e.getMessage());
                                    EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                                    status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                    IoUtils.closeQuietly(this.mEnginePipes[1]);
                                    IoUtils.closeQuietly(this.mTransportPipes[0]);
                                    IoUtils.closeQuietly(this.mTransportPipes[1]);
                                    this.mEngineThread.waitForResult();
                                    IoUtils.closeQuietly(this.mEnginePipes[0]);
                                    PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                                    if (-1000 != 0) {
                                        try {
                                            PerformUnifiedRestoreTask.this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask$StreamFeederThread.run()").abortFullRestore();
                                        } catch (Exception e5) {
                                            Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e5.getMessage());
                                            status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                        }
                                        PerformUnifiedRestoreTask.this.backupManagerService.clearApplicationDataAfterRestoreFailure(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                                    }
                                    nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                                    PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                                    PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                                    PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                                    setRunning(false);
                                } catch (Throwable th) {
                                    iBackupTransport = th;
                                    boolean z = true;
                                    IoUtils.closeQuietly(this.mEnginePipes[1]);
                                    IoUtils.closeQuietly(this.mTransportPipes[0]);
                                    IoUtils.closeQuietly(this.mTransportPipes[1]);
                                    this.mEngineThread.waitForResult();
                                    IoUtils.closeQuietly(this.mEnginePipes[0]);
                                    PerformUnifiedRestoreTask performUnifiedRestoreTask = PerformUnifiedRestoreTask.this;
                                    if (this.mEngine.getAgent() == null) {
                                    }
                                    performUnifiedRestoreTask.mDidLaunch = z;
                                    if (status2 != 0) {
                                    }
                                    PerformUnifiedRestoreTask.this.executeNextState(nextState);
                                    setRunning(false);
                                    throw iBackupTransport;
                                }
                            } catch (Throwable transport3) {
                                iBackupTransport = transport3;
                                boolean z2 = true;
                                IoUtils.closeQuietly(this.mEnginePipes[1]);
                                IoUtils.closeQuietly(this.mTransportPipes[0]);
                                IoUtils.closeQuietly(this.mTransportPipes[1]);
                                this.mEngineThread.waitForResult();
                                IoUtils.closeQuietly(this.mEnginePipes[0]);
                                PerformUnifiedRestoreTask performUnifiedRestoreTask2 = PerformUnifiedRestoreTask.this;
                                if (this.mEngine.getAgent() == null) {
                                    z2 = false;
                                }
                                performUnifiedRestoreTask2.mDidLaunch = z2;
                                if (status2 != 0) {
                                    try {
                                        PerformUnifiedRestoreTask.this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask$StreamFeederThread.run()").abortFullRestore();
                                    } catch (Exception e6) {
                                        Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e6.getMessage());
                                        status2 = -1000;
                                    }
                                    PerformUnifiedRestoreTask.this.backupManagerService.clearApplicationDataAfterRestoreFailure(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                                    if (status2 == -1000) {
                                        nextState = UnifiedRestoreState.FINAL;
                                    } else {
                                        nextState = UnifiedRestoreState.RUNNING_QUEUE;
                                    }
                                } else {
                                    nextState = UnifiedRestoreState.RESTORE_FINISHED;
                                    PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                                    PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                                }
                                PerformUnifiedRestoreTask.this.executeNextState(nextState);
                                setRunning(false);
                                throw iBackupTransport;
                            }
                        }
                        int toCopy = result;
                        while (toCopy > 0) {
                            try {
                                int n = transportIn.read(buffer, 0, toCopy);
                                engineOut.write(buffer, 0, n);
                                toCopy -= n;
                                eWriteEnd2 = eWriteEnd2;
                                transport2 = transport2;
                            } catch (IOException e7) {
                                Slog.e("StreamFeederThread", "Unable to route data for restore");
                                EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, PerformUnifiedRestoreTask.this.mCurrentPackage.packageName, "I/O error on pipes");
                                status = -1003;
                                IoUtils.closeQuietly(this.mEnginePipes[1]);
                                IoUtils.closeQuietly(this.mTransportPipes[0]);
                                IoUtils.closeQuietly(this.mTransportPipes[1]);
                                this.mEngineThread.waitForResult();
                                IoUtils.closeQuietly(this.mEnginePipes[0]);
                                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                                if (-1003 != 0) {
                                }
                                nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                                PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                                PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                                PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                                setRunning(false);
                            } catch (Exception e8) {
                                e = e8;
                                Slog.e("StreamFeederThread", "Transport failed during restore: " + e.getMessage());
                                EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                                status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                IoUtils.closeQuietly(this.mEnginePipes[1]);
                                IoUtils.closeQuietly(this.mTransportPipes[0]);
                                IoUtils.closeQuietly(this.mTransportPipes[1]);
                                this.mEngineThread.waitForResult();
                                IoUtils.closeQuietly(this.mEnginePipes[0]);
                                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                                if (-1000 != 0) {
                                }
                                nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                                PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                                PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                                PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                                setRunning(false);
                            }
                        }
                        transport = transport2;
                        eWriteEnd = eWriteEnd2;
                    } else {
                        transport = transport2;
                        eWriteEnd = eWriteEnd2;
                        if (result == -1) {
                            status2 = 0;
                            break;
                        }
                        Slog.e("StreamFeederThread", "Error " + result + " streaming restore for " + PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                        EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                        status2 = result;
                    }
                    eWriteEnd2 = eWriteEnd;
                    transport2 = transport;
                }
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                IoUtils.closeQuietly(this.mTransportPipes[0]);
                IoUtils.closeQuietly(this.mTransportPipes[1]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                if (status2 == 0) {
                    nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                    PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                    PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                } else {
                    try {
                        PerformUnifiedRestoreTask.this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask$StreamFeederThread.run()").abortFullRestore();
                    } catch (Exception e9) {
                        Slog.e("StreamFeederThread", "Transport threw from abortFullRestore: " + e9.getMessage());
                        status2 = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    }
                    PerformUnifiedRestoreTask.this.backupManagerService.clearApplicationDataAfterRestoreFailure(PerformUnifiedRestoreTask.this.mCurrentPackage.packageName);
                    nextState2 = status2 == -1000 ? UnifiedRestoreState.FINAL : UnifiedRestoreState.RUNNING_QUEUE;
                }
            } catch (IOException e10) {
                Slog.e("StreamFeederThread", "Unable to route data for restore");
                EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, PerformUnifiedRestoreTask.this.mCurrentPackage.packageName, "I/O error on pipes");
                status = -1003;
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                IoUtils.closeQuietly(this.mTransportPipes[0]);
                IoUtils.closeQuietly(this.mTransportPipes[1]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                if (-1003 != 0) {
                }
                nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                setRunning(false);
            } catch (Exception e11) {
                e = e11;
                Slog.e("StreamFeederThread", "Transport failed during restore: " + e.getMessage());
                EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                status = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                IoUtils.closeQuietly(this.mTransportPipes[0]);
                IoUtils.closeQuietly(this.mTransportPipes[1]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                PerformUnifiedRestoreTask.this.mDidLaunch = this.mEngine.getAgent() != null;
                if (-1000 != 0) {
                }
                nextState2 = UnifiedRestoreState.RESTORE_FINISHED;
                PerformUnifiedRestoreTask.this.mAgent = this.mEngine.getAgent();
                PerformUnifiedRestoreTask.this.mWidgetData = this.mEngine.getWidgetData();
                PerformUnifiedRestoreTask.this.executeNextState(nextState2);
                setRunning(false);
            } catch (Throwable th2) {
                iBackupTransport = th2;
                boolean z22 = true;
                IoUtils.closeQuietly(this.mEnginePipes[1]);
                IoUtils.closeQuietly(this.mTransportPipes[0]);
                IoUtils.closeQuietly(this.mTransportPipes[1]);
                this.mEngineThread.waitForResult();
                IoUtils.closeQuietly(this.mEnginePipes[0]);
                PerformUnifiedRestoreTask performUnifiedRestoreTask22 = PerformUnifiedRestoreTask.this;
                if (this.mEngine.getAgent() == null) {
                }
                performUnifiedRestoreTask22.mDidLaunch = z22;
                if (status2 != 0) {
                }
                PerformUnifiedRestoreTask.this.executeNextState(nextState);
                setRunning(false);
                throw iBackupTransport;
            }
            PerformUnifiedRestoreTask.this.executeNextState(nextState2);
            setRunning(false);
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void execute() {
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void operationComplete(long result) {
        }

        @Override // com.android.server.backup.BackupRestoreTask
        public void handleCancel(boolean cancelAll) {
            PerformUnifiedRestoreTask.this.backupManagerService.removeOperation(this.mEphemeralOpToken);
            Slog.w("StreamFeederThread", "Full-data restore target timed out; shutting down");
            PerformUnifiedRestoreTask performUnifiedRestoreTask = PerformUnifiedRestoreTask.this;
            performUnifiedRestoreTask.mMonitor = BackupManagerMonitorUtils.monitorEvent(performUnifiedRestoreTask.mMonitor, 45, PerformUnifiedRestoreTask.this.mCurrentPackage, 2, null);
            this.mEngineThread.handleTimeout();
            IoUtils.closeQuietly(this.mEnginePipes[1]);
            ParcelFileDescriptor[] parcelFileDescriptorArr = this.mEnginePipes;
            parcelFileDescriptorArr[1] = null;
            IoUtils.closeQuietly(parcelFileDescriptorArr[0]);
            this.mEnginePipes[0] = null;
        }
    }

    private void finalizeRestore() {
        PackageManagerBackupAgent packageManagerBackupAgent;
        try {
            this.mTransportClient.connectOrThrow("PerformUnifiedRestoreTask.finalizeRestore()").finishRestore();
        } catch (Exception e) {
            Slog.e(BackupManagerService.TAG, "Error finishing restore", e);
        }
        Exception e2 = this.mObserver;
        if (e2 != null) {
            try {
                e2.restoreFinished(this.mStatus);
            } catch (RemoteException e3) {
                Slog.d(BackupManagerService.TAG, "Restore observer died at restoreFinished");
            }
        }
        this.backupManagerService.getBackupHandler().removeMessages(8);
        if (this.mPmToken > 0) {
            try {
                this.backupManagerService.getPackageManagerBinder().finishPackageInstall(this.mPmToken, this.mDidLaunch);
            } catch (RemoteException e4) {
            }
        } else {
            this.backupManagerService.getBackupHandler().sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
        }
        AppWidgetBackupBridge.restoreFinished(this.mUserId);
        if (this.mIsSystemRestore && (packageManagerBackupAgent = this.mPmAgent) != null) {
            this.backupManagerService.setAncestralPackages(packageManagerBackupAgent.getRestoredPackages());
            this.backupManagerService.setAncestralToken(this.mToken);
            this.backupManagerService.writeRestoreTokens();
        }
        synchronized (this.backupManagerService.getPendingRestores()) {
            if (this.backupManagerService.getPendingRestores().size() > 0) {
                Slog.d(BackupManagerService.TAG, "Starting next pending restore.");
                this.backupManagerService.getBackupHandler().sendMessage(this.backupManagerService.getBackupHandler().obtainMessage(20, this.backupManagerService.getPendingRestores().remove()));
            } else {
                this.backupManagerService.setRestoreInProgress(false);
            }
        }
        Slog.i(BackupManagerService.TAG, "Restore complete.");
        this.mListener.onFinished("PerformUnifiedRestoreTask.finalizeRestore()");
    }

    /* access modifiers changed from: package-private */
    public void keyValueAgentErrorCleanup(boolean clearAppData) {
        if (clearAppData) {
            this.backupManagerService.clearApplicationDataAfterRestoreFailure(this.mCurrentPackage.packageName);
        }
        keyValueAgentCleanup();
    }

    /* access modifiers changed from: package-private */
    public void keyValueAgentCleanup() {
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
                this.backupManagerService.getActivityManager().unbindBackupAgent(this.mCurrentPackage.applicationInfo);
                boolean killAfterRestore = !UserHandle.isCore(this.mCurrentPackage.applicationInfo.uid) && (this.mRestoreDescription.getDataType() == 2 || (65536 & this.mCurrentPackage.applicationInfo.flags) != 0);
                if (this.mTargetPackage == null && killAfterRestore) {
                    Slog.d(BackupManagerService.TAG, "Restore complete, killing host process of " + this.mCurrentPackage.applicationInfo.processName);
                    this.backupManagerService.getActivityManager().killApplicationProcess(this.mCurrentPackage.applicationInfo.processName, this.mCurrentPackage.applicationInfo.uid);
                }
            } catch (RemoteException e3) {
            }
        }
        this.backupManagerService.getBackupHandler().removeMessages(18, this);
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void operationComplete(long unusedResult) {
        UnifiedRestoreState nextState;
        this.backupManagerService.removeOperation(this.mEphemeralOpToken);
        int i = AnonymousClass1.$SwitchMap$com$android$server$backup$restore$UnifiedRestoreState[this.mState.ordinal()];
        if (i == 1) {
            nextState = UnifiedRestoreState.RUNNING_QUEUE;
        } else if (i == 3 || i == 4) {
            nextState = UnifiedRestoreState.RESTORE_FINISHED;
        } else if (i != 5) {
            Slog.e(BackupManagerService.TAG, "Unexpected restore callback into state " + this.mState);
            keyValueAgentErrorCleanup(true);
            nextState = UnifiedRestoreState.FINAL;
        } else {
            EventLog.writeEvent((int) EventLogTags.RESTORE_PACKAGE, this.mCurrentPackage.packageName, Integer.valueOf((int) this.mBackupDataName.length()));
            keyValueAgentCleanup();
            if (this.mWidgetData != null) {
                this.backupManagerService.restoreWidgetData(this.mCurrentPackage.packageName, this.mWidgetData);
            }
            nextState = UnifiedRestoreState.RUNNING_QUEUE;
        }
        executeNextState(nextState);
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void handleCancel(boolean cancelAll) {
        this.backupManagerService.removeOperation(this.mEphemeralOpToken);
        Slog.e(BackupManagerService.TAG, "Timeout restoring application " + this.mCurrentPackage.packageName);
        this.mMonitor = BackupManagerMonitorUtils.monitorEvent(this.mMonitor, 31, this.mCurrentPackage, 2, null);
        EventLog.writeEvent((int) EventLogTags.RESTORE_AGENT_FAILURE, this.mCurrentPackage.packageName, "restore timeout");
        keyValueAgentErrorCleanup(true);
        executeNextState(UnifiedRestoreState.RUNNING_QUEUE);
    }

    /* access modifiers changed from: package-private */
    public void executeNextState(UnifiedRestoreState nextState) {
        this.mState = nextState;
        this.backupManagerService.getBackupHandler().sendMessage(this.backupManagerService.getBackupHandler().obtainMessage(20, this));
    }

    /* access modifiers changed from: package-private */
    public void sendStartRestore(int numPackages) {
        IRestoreObserver iRestoreObserver = this.mObserver;
        if (iRestoreObserver != null) {
            try {
                iRestoreObserver.restoreStarting(numPackages);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "Restore observer went away: startRestore");
                this.mObserver = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendOnRestorePackage(String name) {
        IRestoreObserver iRestoreObserver = this.mObserver;
        if (iRestoreObserver != null) {
            try {
                iRestoreObserver.onUpdate(this.mCount, name);
            } catch (RemoteException e) {
                Slog.d(BackupManagerService.TAG, "Restore observer died in onUpdate");
                this.mObserver = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendEndRestore() {
        IRestoreObserver iRestoreObserver = this.mObserver;
        if (iRestoreObserver != null) {
            try {
                iRestoreObserver.restoreFinished(this.mStatus);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "Restore observer went away: endRestore");
                this.mObserver = null;
            }
        }
    }
}
