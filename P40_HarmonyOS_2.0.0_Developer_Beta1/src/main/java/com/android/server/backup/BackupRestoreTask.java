package com.android.server.backup;

public interface BackupRestoreTask {
    void execute();

    void handleCancel(boolean z);

    void operationComplete(long j);
}
