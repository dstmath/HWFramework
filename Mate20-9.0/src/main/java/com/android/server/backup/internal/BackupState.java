package com.android.server.backup.internal;

enum BackupState {
    INITIAL,
    BACKUP_PM,
    RUNNING_QUEUE,
    FINAL
}
