package com.android.server.backup.internal;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;

public class RunBackupReceiver extends BroadcastReceiver {
    private BackupManagerService backupManagerService;

    public RunBackupReceiver(BackupManagerService backupManagerService2) {
        this.backupManagerService = backupManagerService2;
    }

    public void onReceive(Context context, Intent intent) {
        if (BackupManagerService.RUN_BACKUP_ACTION.equals(intent.getAction())) {
            synchronized (this.backupManagerService.getQueueLock()) {
                if (this.backupManagerService.getPendingInits().size() > 0) {
                    try {
                        this.backupManagerService.getAlarmManager().cancel(this.backupManagerService.getRunInitIntent());
                        this.backupManagerService.getRunInitIntent().send();
                    } catch (PendingIntent.CanceledException e) {
                        Slog.e(BackupManagerService.TAG, "Run init intent cancelled");
                    }
                } else if (!this.backupManagerService.isEnabled() || !this.backupManagerService.isProvisioned()) {
                    Slog.w(BackupManagerService.TAG, "Backup pass but e=" + this.backupManagerService.isEnabled() + " p=" + this.backupManagerService.isProvisioned());
                } else if (!this.backupManagerService.isBackupRunning()) {
                    Slog.v(BackupManagerService.TAG, "Running a backup pass");
                    this.backupManagerService.setBackupRunning(true);
                    this.backupManagerService.getWakelock().acquire();
                    this.backupManagerService.getBackupHandler().sendMessage(this.backupManagerService.getBackupHandler().obtainMessage(1));
                } else {
                    Slog.i(BackupManagerService.TAG, "Backup time but one already running");
                }
            }
        }
    }
}
