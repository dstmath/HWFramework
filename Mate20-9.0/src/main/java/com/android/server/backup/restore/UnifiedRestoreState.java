package com.android.server.backup.restore;

public enum UnifiedRestoreState {
    INITIAL,
    RUNNING_QUEUE,
    RESTORE_KEYVALUE,
    RESTORE_FULL,
    RESTORE_FINISHED,
    FINAL
}
