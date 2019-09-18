package com.android.server.backup.fullbackup;

import android.app.backup.IFullBackupRestoreObserver;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;

public abstract class FullBackupTask implements Runnable {
    IFullBackupRestoreObserver mObserver;

    FullBackupTask(IFullBackupRestoreObserver observer) {
        this.mObserver = observer;
    }

    /* access modifiers changed from: package-private */
    public final void sendStartBackup() {
        if (this.mObserver != null) {
            try {
                this.mObserver.onStartBackup();
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "full backup observer went away: startBackup");
                this.mObserver = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void sendOnBackupPackage(String name) {
        if (this.mObserver != null) {
            try {
                this.mObserver.onBackupPackage(name);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "full backup observer went away: backupPackage");
                this.mObserver = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void sendEndBackup() {
        if (this.mObserver != null) {
            try {
                this.mObserver.onEndBackup();
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "full backup observer went away: endBackup");
                this.mObserver = null;
            }
        }
    }
}
