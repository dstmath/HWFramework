package com.android.server.backup.restore;

import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.UserBackupManagerService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdbRestoreFinishedLatch implements BackupRestoreTask {
    private static final String TAG = "AdbRestoreFinishedLatch";
    private UserBackupManagerService backupManagerService;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;
    private final int mCurrentOpToken;
    final CountDownLatch mLatch = new CountDownLatch(1);

    public AdbRestoreFinishedLatch(UserBackupManagerService backupManagerService2, int currentOpToken) {
        this.backupManagerService = backupManagerService2;
        this.mCurrentOpToken = currentOpToken;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
    }

    /* access modifiers changed from: package-private */
    public void await() {
        try {
            this.mLatch.await(this.mAgentTimeoutParameters.getFullBackupAgentTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Slog.w(TAG, "Interrupted!");
        }
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void execute() {
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void operationComplete(long result) {
        this.mLatch.countDown();
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }

    @Override // com.android.server.backup.BackupRestoreTask
    public void handleCancel(boolean cancelAll) {
        Slog.w(TAG, "adb onRestoreFinished() timed out");
        this.mLatch.countDown();
        this.backupManagerService.removeOperation(this.mCurrentOpToken);
    }
}
