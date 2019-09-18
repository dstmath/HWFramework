package com.android.server.backup.internal;

import android.content.pm.IPackageDataObserver;
import com.android.server.backup.BackupManagerService;

public class ClearDataObserver extends IPackageDataObserver.Stub {
    private BackupManagerService backupManagerService;

    public ClearDataObserver(BackupManagerService backupManagerService2) {
        this.backupManagerService = backupManagerService2;
    }

    public void onRemoveCompleted(String packageName, boolean succeeded) {
        synchronized (this.backupManagerService.getClearDataLock()) {
            this.backupManagerService.setClearingData(false);
            this.backupManagerService.getClearDataLock().notifyAll();
        }
    }
}
