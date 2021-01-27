package com.android.server.backup.internal;

import android.content.pm.IPackageDataObserver;
import com.android.server.backup.UserBackupManagerService;

public class ClearDataObserver extends IPackageDataObserver.Stub {
    private UserBackupManagerService backupManagerService;

    public ClearDataObserver(UserBackupManagerService backupManagerService2) {
        this.backupManagerService = backupManagerService2;
    }

    public void onRemoveCompleted(String packageName, boolean succeeded) {
        synchronized (this.backupManagerService.getClearDataLock()) {
            this.backupManagerService.setClearingData(false);
            this.backupManagerService.getClearDataLock().notifyAll();
        }
    }
}
