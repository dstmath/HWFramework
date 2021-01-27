package com.android.server.backup.internal;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import com.android.server.backup.KeyValueBackupJob;
import com.android.server.backup.UserBackupManagerService;

public class SetupObserver extends ContentObserver {
    private final Context mContext;
    private final UserBackupManagerService mUserBackupManagerService;
    private final int mUserId;

    public SetupObserver(UserBackupManagerService userBackupManagerService, Handler handler) {
        super(handler);
        this.mUserBackupManagerService = userBackupManagerService;
        this.mContext = userBackupManagerService.getContext();
        this.mUserId = userBackupManagerService.getUserId();
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange) {
        boolean previousSetupComplete = this.mUserBackupManagerService.isSetupComplete();
        boolean resolvedSetupComplete = previousSetupComplete || UserBackupManagerService.getSetupCompleteSettingForUser(this.mContext, this.mUserId);
        this.mUserBackupManagerService.setSetupComplete(resolvedSetupComplete);
        synchronized (this.mUserBackupManagerService.getQueueLock()) {
            if (resolvedSetupComplete && !previousSetupComplete) {
                if (this.mUserBackupManagerService.isEnabled()) {
                    KeyValueBackupJob.schedule(this.mUserBackupManagerService.getUserId(), this.mContext, this.mUserBackupManagerService.getConstants());
                    this.mUserBackupManagerService.scheduleNextFullBackupJob(0);
                }
            }
        }
    }
}
