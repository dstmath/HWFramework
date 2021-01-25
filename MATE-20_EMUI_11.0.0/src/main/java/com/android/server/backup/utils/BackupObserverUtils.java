package com.android.server.backup.utils;

import android.app.backup.BackupProgress;
import android.app.backup.IBackupObserver;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;

public class BackupObserverUtils {
    public static void sendBackupOnUpdate(IBackupObserver observer, String packageName, BackupProgress progress) {
        if (observer != null) {
            try {
                observer.onUpdate(packageName, progress);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "Backup observer went away: onUpdate");
            }
        }
    }

    public static void sendBackupOnPackageResult(IBackupObserver observer, String packageName, int status) {
        if (observer != null) {
            try {
                observer.onResult(packageName, status);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "Backup observer went away: onResult");
            }
        }
    }

    public static void sendBackupFinished(IBackupObserver observer, int status) {
        if (observer != null) {
            try {
                observer.backupFinished(status);
            } catch (RemoteException e) {
                Slog.w(BackupManagerService.TAG, "Backup observer went away: backupFinished");
            }
        }
    }
}
