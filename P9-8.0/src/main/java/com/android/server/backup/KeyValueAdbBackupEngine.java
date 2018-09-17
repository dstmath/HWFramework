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
import com.android.server.job.controllers.JobStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import libcore.io.IoUtils;

class KeyValueAdbBackupEngine {
    private static final String BACKUP_KEY_VALUE_BACKUP_DATA_FILENAME_SUFFIX = ".data";
    private static final String BACKUP_KEY_VALUE_BLANK_STATE_FILENAME = "blank_state";
    private static final String BACKUP_KEY_VALUE_DIRECTORY_NAME = "key_value_dir";
    private static final String BACKUP_KEY_VALUE_NEW_STATE_FILENAME_SUFFIX = ".new";
    private static final boolean DEBUG = false;
    private static final String TAG = "KeyValueAdbBackupEngine";
    private ParcelFileDescriptor mBackupData;
    private final File mBackupDataName;
    private BackupManagerService mBackupManagerService;
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

    class KeyValueAdbBackupDataCopier implements Runnable {
        private final PackageInfo mPackage;
        private final ParcelFileDescriptor mPipe;
        private final int mToken;

        KeyValueAdbBackupDataCopier(PackageInfo pack, ParcelFileDescriptor pipe, int token) throws IOException {
            this.mPackage = pack;
            this.mPipe = ParcelFileDescriptor.dup(pipe.getFileDescriptor());
            this.mToken = token;
        }

        public void run() {
            try {
                FullBackupDataOutput output = new FullBackupDataOutput(this.mPipe);
                BackupManagerService.writeAppManifest(this.mPackage, KeyValueAdbBackupEngine.this.mPackageManager, KeyValueAdbBackupEngine.this.mManifestFile, false, false);
                FullBackup.backupToTar(this.mPackage.packageName, "k", null, KeyValueAdbBackupEngine.this.mDataDir.getAbsolutePath(), KeyValueAdbBackupEngine.this.mManifestFile.getAbsolutePath(), output);
                KeyValueAdbBackupEngine.this.mManifestFile.delete();
                FullBackup.backupToTar(this.mPackage.packageName, "k", null, KeyValueAdbBackupEngine.this.mDataDir.getAbsolutePath(), KeyValueAdbBackupEngine.this.mBackupDataName.getAbsolutePath(), output);
                try {
                    new FileOutputStream(this.mPipe.getFileDescriptor()).write(new byte[4]);
                } catch (IOException e) {
                    Slog.e(KeyValueAdbBackupEngine.TAG, "Unable to finalize backup stream!");
                }
                try {
                    KeyValueAdbBackupEngine.this.mBackupManagerService.mBackupManagerBinder.opComplete(this.mToken, 0);
                } catch (RemoteException e2) {
                }
                IoUtils.closeQuietly(this.mPipe);
            } catch (IOException e3) {
                Slog.e(KeyValueAdbBackupEngine.TAG, "Error running full backup for " + this.mPackage.packageName + ". " + e3);
                IoUtils.closeQuietly(this.mPipe);
            } catch (Throwable th) {
                IoUtils.closeQuietly(this.mPipe);
                throw th;
            }
        }
    }

    KeyValueAdbBackupEngine(OutputStream output, PackageInfo packageInfo, BackupManagerService backupManagerService, PackageManager packageManager, File baseStateDir, File dataDir) {
        this.mOutput = output;
        this.mCurrentPackage = packageInfo;
        this.mBackupManagerService = backupManagerService;
        this.mPackageManager = packageManager;
        this.mDataDir = dataDir;
        this.mStateDir = new File(baseStateDir, BACKUP_KEY_VALUE_DIRECTORY_NAME);
        this.mStateDir.mkdirs();
        String pkg = this.mCurrentPackage.packageName;
        this.mBackupDataName = new File(this.mDataDir, pkg + BACKUP_KEY_VALUE_BACKUP_DATA_FILENAME_SUFFIX);
        this.mNewStateName = new File(this.mStateDir, pkg + BACKUP_KEY_VALUE_NEW_STATE_FILENAME_SUFFIX);
        this.mManifestFile = new File(this.mDataDir, "_manifest");
    }

    void backupOnePackage() throws IOException {
        ApplicationInfo targetApp = this.mCurrentPackage.applicationInfo;
        try {
            prepareBackupFiles(this.mCurrentPackage.packageName);
            IBackupAgent agent = bindToAgent(targetApp);
            if (agent == null) {
                Slog.e(TAG, "Failed binding to BackupAgent for package " + this.mCurrentPackage.packageName);
            } else if (invokeAgentForAdbBackup(this.mCurrentPackage.packageName, agent)) {
                writeBackupData();
                cleanup();
            } else {
                Slog.e(TAG, "Backup Failed for package " + this.mCurrentPackage.packageName);
                cleanup();
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "Failed creating files for package " + this.mCurrentPackage.packageName + " will ignore package. " + e);
        } finally {
            cleanup();
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
        int token = this.mBackupManagerService.generateToken();
        try {
            this.mBackupManagerService.prepareOperationTimeout(token, 30000, null, 0);
            agent.doBackup(this.mSavedState, this.mBackupData, this.mNewState, JobStatus.NO_LATEST_RUNTIME, token, this.mBackupManagerService.mBackupManagerBinder);
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

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeBackupData() throws IOException {
        int token = this.mBackupManagerService.generateToken();
        try {
            ParcelFileDescriptor[] pipes = ParcelFileDescriptor.createPipe();
            this.mBackupManagerService.prepareOperationTimeout(token, 30000, null, 0);
            KeyValueAdbBackupDataCopier runner = new KeyValueAdbBackupDataCopier(this.mCurrentPackage, pipes[1], token);
            pipes[1].close();
            pipes[1] = null;
            new Thread(runner, "key-value-app-data-runner").start();
            BackupManagerService.routeSocketDataToOutput(pipes[0], this.mOutput);
            if (!this.mBackupManagerService.waitUntilOperationComplete(token)) {
                Slog.e(TAG, "Full backup failed on package " + this.mCurrentPackage.packageName);
            }
            this.mOutput.flush();
            if (pipes != null) {
                IoUtils.closeQuietly(pipes[0]);
                IoUtils.closeQuietly(pipes[1]);
            }
        } catch (IOException e) {
            Slog.e(TAG, "Error backing up " + this.mCurrentPackage.packageName + ": " + e);
            this.mOutput.flush();
            if (null != null) {
                IoUtils.closeQuietly(null[0]);
                IoUtils.closeQuietly(null[1]);
            }
        } catch (Throwable th) {
            this.mOutput.flush();
            if (null != null) {
                IoUtils.closeQuietly(null[0]);
                IoUtils.closeQuietly(null[1]);
            }
            throw th;
        }
    }

    private void cleanup() {
        this.mBackupManagerService.tearDownAgentAndKill(this.mCurrentPackage.applicationInfo);
        this.mBlankStateName.delete();
        this.mNewStateName.delete();
        this.mBackupDataName.delete();
    }
}
