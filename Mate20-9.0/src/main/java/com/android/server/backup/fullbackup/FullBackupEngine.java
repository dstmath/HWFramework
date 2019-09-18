package com.android.server.backup.fullbackup;

import android.app.IBackupAgent;
import android.app.backup.FullBackup;
import android.app.backup.FullBackupDataOutput;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import android.util.StringBuilderPrinter;
import com.android.internal.util.Preconditions;
import com.android.server.AppWidgetBackupBridge;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.utils.FullBackupUtils;
import com.android.server.job.JobSchedulerShellCommand;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FullBackupEngine {
    /* access modifiers changed from: private */
    public BackupManagerService backupManagerService;
    IBackupAgent mAgent;
    /* access modifiers changed from: private */
    public final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    File mFilesDir = new File("/data/system");
    boolean mIncludeApks;
    File mManifestFile = new File(this.mFilesDir, BackupManagerService.BACKUP_MANIFEST_FILENAME);
    File mMetadataFile = new File(this.mFilesDir, BackupManagerService.BACKUP_METADATA_FILENAME);
    private final int mOpToken;
    OutputStream mOutput;
    PackageInfo mPkg;
    FullBackupPreflight mPreflightHook;
    /* access modifiers changed from: private */
    public final long mQuota;
    BackupRestoreTask mTimeoutMonitor;
    /* access modifiers changed from: private */
    public final int mTransportFlags;

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
            long timeout;
            try {
                FullBackupDataOutput output = new FullBackupDataOutput(this.mPipe, -1, FullBackupEngine.this.mTransportFlags);
                if (this.mWriteManifest) {
                    boolean writeWidgetData = this.mWidgetData != null;
                    FullBackupUtils.writeAppManifest(this.mPackage, FullBackupEngine.this.backupManagerService.getPackageManager(), FullBackupEngine.this.mManifestFile, this.mSendApk, writeWidgetData);
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
                if (this.mPackage.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE)) {
                    timeout = FullBackupEngine.this.mAgentTimeoutParameters.getSharedBackupAgentTimeoutMillis();
                } else {
                    timeout = FullBackupEngine.this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis();
                }
                Slog.d(BackupManagerService.TAG, "Calling doFullBackup() on " + this.mPackage.packageName);
                FullBackupEngine.this.backupManagerService.prepareOperationTimeout(this.mToken, timeout, FullBackupEngine.this.mTimeoutMonitor, 0);
                this.mAgent.doFullBackup(this.mPipe, FullBackupEngine.this.mQuota, this.mToken, FullBackupEngine.this.backupManagerService.getBackupManagerBinder(), FullBackupEngine.this.mTransportFlags);
                try {
                    this.mPipe.close();
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                Slog.e(BackupManagerService.TAG, "Error running full backup for " + this.mPackage.packageName);
                this.mPipe.close();
            } catch (RemoteException e3) {
                Slog.e(BackupManagerService.TAG, "Remote agent vanished during full backup of " + this.mPackage.packageName);
                this.mPipe.close();
            } catch (Throwable th) {
                Throwable th2 = th;
                try {
                    this.mPipe.close();
                } catch (IOException e4) {
                }
                throw th2;
            }
        }
    }

    public FullBackupEngine(BackupManagerService backupManagerService2, OutputStream output, FullBackupPreflight preflightHook, PackageInfo pkg, boolean alsoApks, BackupRestoreTask timeoutMonitor, long quota, int opToken, int transportFlags) {
        this.backupManagerService = backupManagerService2;
        this.mOutput = output;
        this.mPreflightHook = preflightHook;
        this.mPkg = pkg;
        this.mIncludeApks = alsoApks;
        this.mTimeoutMonitor = timeoutMonitor;
        this.mQuota = quota;
        this.mOpToken = opToken;
        this.mTransportFlags = transportFlags;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
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

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00f9 A[Catch:{ IOException -> 0x010e }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0123 A[Catch:{ IOException -> 0x0136 }] */
    public int backupOnePackage() throws RemoteException {
        int i;
        int result = -1003;
        if (initializeAgent()) {
            ParcelFileDescriptor[] pipes = null;
            try {
                ParcelFileDescriptor[] pipes2 = ParcelFileDescriptor.createPipe();
                try {
                    ApplicationInfo app = this.mPkg.applicationInfo;
                    boolean isSharedStorage = this.mPkg.packageName.equals(BackupManagerService.SHARED_BACKUP_AGENT_PACKAGE);
                    FullBackupRunner runner = new FullBackupRunner(this.mPkg, this.mAgent, pipes2[1], this.mOpToken, this.mIncludeApks && !isSharedStorage && (app.privateFlags & 4) == 0 && ((app.flags & 1) == 0 || (app.flags & 128) != 0), !isSharedStorage, AppWidgetBackupBridge.getWidgetState(this.mPkg.packageName, 0));
                    pipes2[1].close();
                    pipes2[1] = null;
                    new Thread(runner, "app-data-runner").start();
                    FullBackupUtils.routeSocketDataToOutput(pipes2[0], this.mOutput);
                    if (!this.backupManagerService.waitUntilOperationComplete(this.mOpToken)) {
                        Slog.e(BackupManagerService.TAG, "Full backup failed on package " + this.mPkg.packageName);
                    } else {
                        result = 0;
                    }
                } catch (IOException e) {
                    e = e;
                    pipes = pipes2;
                    try {
                        Slog.e(BackupManagerService.TAG, "Error backing up " + this.mPkg.packageName + ": " + e.getMessage());
                        tearDown();
                        return result;
                    } catch (Throwable th) {
                        th = th;
                        pipes2 = pipes;
                        Throwable th2 = th;
                        try {
                            this.mOutput.flush();
                            if (pipes2 != null) {
                                if (pipes2[0] != null) {
                                    pipes2[0].close();
                                }
                                if (pipes2[1] != null) {
                                    pipes2[1].close();
                                }
                            }
                        } catch (IOException e2) {
                            Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                        }
                        throw th2;
                    }
                    try {
                        this.mOutput.flush();
                        if (pipes != null) {
                        }
                        result = -1003;
                    } catch (IOException e3) {
                        Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                        i = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                        result = i;
                        tearDown();
                        return result;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Throwable th22 = th;
                    this.mOutput.flush();
                    if (pipes2 != null) {
                    }
                    throw th22;
                }
                try {
                    this.mOutput.flush();
                    if (pipes2 != null) {
                        if (pipes2[0] != null) {
                            pipes2[0].close();
                        }
                        if (pipes2[1] != null) {
                            pipes2[1].close();
                        }
                    }
                } catch (IOException e4) {
                    Slog.w(BackupManagerService.TAG, "Error bringing down backup stack");
                    i = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    result = i;
                    tearDown();
                    return result;
                }
            } catch (IOException e5) {
                e = e5;
                Slog.e(BackupManagerService.TAG, "Error backing up " + this.mPkg.packageName + ": " + e.getMessage());
                this.mOutput.flush();
                if (pipes != null) {
                    if (pipes[0] != null) {
                        pipes[0].close();
                    }
                    if (pipes[1] != null) {
                        pipes[1].close();
                    }
                }
                result = -1003;
                tearDown();
                return result;
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
            this.mAgent = this.backupManagerService.bindToAgentSynchronous(this.mPkg.applicationInfo, 1);
        }
        if (this.mAgent != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void writeApkToBackup(PackageInfo pkg, FullBackupDataOutput output) {
        PackageInfo packageInfo = pkg;
        String appSourceDir = packageInfo.applicationInfo.getBaseCodePath();
        FullBackup.backupToTar(packageInfo.packageName, "a", null, new File(appSourceDir).getParent(), appSourceDir, output);
        File obbDir = new Environment.UserEnvironment(0).buildExternalStorageAppObbDirs(packageInfo.packageName)[0];
        if (obbDir != null) {
            File[] obbFiles = obbDir.listFiles();
            if (obbFiles != null) {
                String obbDirName = obbDir.getAbsolutePath();
                for (File obb : obbFiles) {
                    FullBackup.backupToTar(packageInfo.packageName, "obb", null, obbDirName, obb.getAbsolutePath(), output);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void writeMetadata(PackageInfo pkg, File destination, byte[] widgetData) throws IOException {
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
            this.backupManagerService.tearDownAgentAndKill(this.mPkg.applicationInfo);
        }
    }
}
