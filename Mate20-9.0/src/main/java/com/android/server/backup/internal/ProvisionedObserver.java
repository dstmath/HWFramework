package com.android.server.backup.internal;

import android.database.ContentObserver;
import android.os.Handler;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.KeyValueBackupJob;

public class ProvisionedObserver extends ContentObserver {
    private BackupManagerService backupManagerService;

    public ProvisionedObserver(BackupManagerService backupManagerService2, Handler handler) {
        super(handler);
        this.backupManagerService = backupManagerService2;
    }

    public void onChange(boolean selfChange) {
        boolean wasProvisioned = this.backupManagerService.isProvisioned();
        this.backupManagerService.setProvisioned(wasProvisioned || this.backupManagerService.deviceIsProvisioned());
        synchronized (this.backupManagerService.getQueueLock()) {
            if (this.backupManagerService.isProvisioned() && !wasProvisioned && this.backupManagerService.isEnabled()) {
                KeyValueBackupJob.schedule(this.backupManagerService.getContext(), this.backupManagerService.getConstants());
                this.backupManagerService.scheduleNextFullBackupJob(0);
            }
        }
    }
}
