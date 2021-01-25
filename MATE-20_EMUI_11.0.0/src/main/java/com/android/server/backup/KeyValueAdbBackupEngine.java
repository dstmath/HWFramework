package com.android.server.backup;

import android.app.IBackupAgent;
import android.app.backup.FullBackup;
import android.app.backup.FullBackupDataOutput;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SELinux;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.backup.fullbackup.AppMetadataBackupWriter;
import com.android.server.backup.remote.ServiceBackupCallback;
import com.android.server.backup.utils.FullBackupUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import libcore.io.IoUtils;

public class KeyValueAdbBackupEngine {
    private static final String BACKUP_KEY_VALUE_BACKUP_DATA_FILENAME_SUFFIX = ".data";
    private static final String BACKUP_KEY_VALUE_BLANK_STATE_FILENAME = "blank_state";
    private static final String BACKUP_KEY_VALUE_DIRECTORY_NAME = "key_value_dir";
    private static final String BACKUP_KEY_VALUE_NEW_STATE_FILENAME_SUFFIX = ".new";
    private static final boolean DEBUG = false;
    private static final String TAG = "KeyValueAdbBackupEngine";
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private ParcelFileDescriptor mBackupData;
    private final File mBackupDataName;
    private UserBackupManagerService mBackupManagerService;
    private final File mBlankStateName = new File(this.mStateDir, BACKUP_KEY_VALUE_BLANK_STATE_FILENAME);
    private final PackageInfo mCurrentPackage;
    private final File mDataDir;
    private final File mManifestFile;
    private ParcelFileDescriptor mNewState;
    private final File mNewStateName;
    private final OutputStream mOutput;
    private final PackageManager mPackageManager;
    private ParcelFileDescriptor mSavedState;
    private final File mStateDir;

    public KeyValueAdbBackupEngine(OutputStream output, PackageInfo packageInfo, UserBackupManagerService backupManagerService, PackageManager packageManager, File baseStateDir, File dataDir) {
        this.mOutput = output;
        this.mCurrentPackage = packageInfo;
        this.mBackupManagerService = backupManagerService;
        this.mPackageManager = packageManager;
        this.mDataDir = dataDir;
        this.mStateDir = new File(baseStateDir, BACKUP_KEY_VALUE_DIRECTORY_NAME);
        this.mStateDir.mkdirs();
        String pkg = this.mCurrentPackage.packageName;
        File file = this.mDataDir;
        this.mBackupDataName = new File(file, pkg + ".data");
        File file2 = this.mStateDir;
        this.mNewStateName = new File(file2, pkg + ".new");
        this.mManifestFile = new File(this.mDataDir, UserBackupManagerService.BACKUP_MANIFEST_FILENAME);
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
    }

    public void backupOnePackage() throws IOException {
        ApplicationInfo targetApp = this.mCurrentPackage.applicationInfo;
        try {
            prepareBackupFiles(this.mCurrentPackage.packageName);
            IBackupAgent agent = bindToAgent(targetApp);
            if (agent == null) {
                Slog.e(TAG, "Failed binding to BackupAgent for package " + this.mCurrentPackage.packageName);
                cleanup();
            } else if (!invokeAgentForAdbBackup(this.mCurrentPackage.packageName, agent)) {
                Slog.e(TAG, "Backup Failed for package " + this.mCurrentPackage.packageName);
                cleanup();
            } else {
                writeBackupData();
                cleanup();
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "Failed creating files for package " + this.mCurrentPackage.packageName + " will ignore package. " + e);
        } catch (Throwable th) {
            cleanup();
            throw th;
        }
    }

    private void prepareBackupFiles(String packageName) throws FileNotFoundException {
        this.mSavedState = ParcelFileDescriptor.open(this.mBlankStateName, 402653184);
        this.mBackupData = ParcelFileDescriptor.open(this.mBackupDataName, 1006632960);
        if (!SELinux.restorecon(this.mBackupDataName)) {
            Slog.e(TAG, "SELinux restorecon failed on " + this.mBackupDataName);
        }
        this.mNewState = ParcelFileDescriptor.open(this.mNewStateName, 1006632960);
    }

    private IBackupAgent bindToAgent(ApplicationInfo targetApp) {
        try {
            return this.mBackupManagerService.bindToAgentSynchronous(targetApp, 0);
        } catch (SecurityException e) {
            Slog.e(TAG, "error in binding to agent for package " + targetApp.packageName + ". " + e);
            return null;
        }
    }

    private boolean invokeAgentForAdbBackup(String packageName, IBackupAgent agent) {
        int token = this.mBackupManagerService.generateRandomIntegerToken();
        try {
            this.mBackupManagerService.prepareOperationTimeout(token, this.mAgentTimeoutParameters.getKvBackupAgentTimeoutMillis(), null, 0);
            agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, (long) JobStatus.NO_LATEST_RUNTIME, new ServiceBackupCallback(this.mBackupManagerService.getBackupManagerBinder(), token), 0);
            if (this.mBackupManagerService.waitUntilOperationComplete(token)) {
                return true;
            }
            Slog.e(TAG, "Key-value backup failed on package " + packageName);
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "Error invoking agent for backup on " + packageName + ". " + e);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class KeyValueAdbBackupDataCopier implements Runnable {
        private final PackageInfo mPackage;
        private final ParcelFileDescriptor mPipe;
        private final int mToken;

        KeyValueAdbBackupDataCopier(PackageInfo pack, ParcelFileDescriptor pipe, int token) throws IOException {
            this.mPackage = pack;
            this.mPipe = ParcelFileDescriptor.dup(pipe.getFileDescriptor());
            this.mToken = token;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                FullBackupDataOutput output = new FullBackupDataOutput(this.mPipe);
                new AppMetadataBackupWriter(output, KeyValueAdbBackupEngine.this.mPackageManager).backupManifest(this.mPackage, KeyValueAdbBackupEngine.this.mManifestFile, KeyValueAdbBackupEngine.this.mDataDir, "k", null, false);
                KeyValueAdbBackupEngine.this.mManifestFile.delete();
                FullBackup.backupToTar(this.mPackage.packageName, "k", (String) null, KeyValueAdbBackupEngine.this.mDataDir.getAbsolutePath(), KeyValueAdbBackupEngine.this.mBackupDataName.getAbsolutePath(), output);
                try {
                    new FileOutputStream(this.mPipe.getFileDescriptor()).write(new byte[4]);
                } catch (IOException e) {
                    Slog.e(KeyValueAdbBackupEngine.TAG, "Unable to finalize backup stream!");
                }
                try {
                    KeyValueAdbBackupEngine.this.mBackupManagerService.getBackupManagerBinder().opComplete(this.mToken, 0);
                } catch (RemoteException e2) {
                }
            } catch (IOException e3) {
                Slog.e(KeyValueAdbBackupEngine.TAG, "Error running full backup for " + this.mPackage.packageName + ". " + e3);
            } catch (Throwable th) {
                IoUtils.closeQuietly(this.mPipe);
                throw th;
            }
            IoUtils.closeQuietly(this.mPipe);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A[RETURN, SYNTHETIC] */
    private void writeBackupData() throws IOException {
        ParcelFileDescriptor[] pipes;
        Throwable th;
        IOException e;
        ParcelFileDescriptor parcelFileDescriptor;
        int token = this.mBackupManagerService.generateRandomIntegerToken();
        long kvBackupAgentTimeoutMillis = this.mAgentTimeoutParameters.getKvBackupAgentTimeoutMillis();
        try {
            pipes = ParcelFileDescriptor.createPipe();
            try {
                this.mBackupManagerService.prepareOperationTimeout(token, kvBackupAgentTimeoutMillis, null, 0);
                KeyValueAdbBackupDataCopier runner = new KeyValueAdbBackupDataCopier(this.mCurrentPackage, pipes[1], token);
                pipes[1].close();
                pipes[1] = null;
                new Thread(runner, "key-value-app-data-runner").start();
                FullBackupUtils.routeSocketDataToOutput(pipes[0], this.mOutput);
                if (!this.mBackupManagerService.waitUntilOperationComplete(token)) {
                    Slog.e(TAG, "Full backup failed on package " + this.mCurrentPackage.packageName);
                }
                this.mOutput.flush();
                IoUtils.closeQuietly(pipes[0]);
                parcelFileDescriptor = pipes[1];
            } catch (IOException e2) {
                e = e2;
                try {
                    Slog.e(TAG, "Error backing up " + this.mCurrentPackage.packageName + ": " + e);
                    this.mOutput.flush();
                    if (pipes == null) {
                        IoUtils.closeQuietly(pipes[0]);
                        parcelFileDescriptor = pipes[1];
                        IoUtils.closeQuietly(parcelFileDescriptor);
                    }
                    return;
                } catch (Throwable th2) {
                    th = th2;
                    this.mOutput.flush();
                    if (pipes != null) {
                        IoUtils.closeQuietly(pipes[0]);
                        IoUtils.closeQuietly(pipes[1]);
                    }
                    throw th;
                }
            }
        } catch (IOException e3) {
            pipes = null;
            e = e3;
            Slog.e(TAG, "Error backing up " + this.mCurrentPackage.packageName + ": " + e);
            this.mOutput.flush();
            if (pipes == null) {
            }
        } catch (Throwable th3) {
            th = th3;
            pipes = null;
            this.mOutput.flush();
            if (pipes != null) {
            }
            throw th;
        }
        IoUtils.closeQuietly(parcelFileDescriptor);
    }

    private void cleanup() {
        this.mBackupManagerService.tearDownAgentAndKill(this.mCurrentPackage.applicationInfo);
        this.mBlankStateName.delete();
        this.mNewStateName.delete();
        this.mBackupDataName.delete();
    }
}
