package com.android.server.backup.remote;

import android.app.backup.IBackupCallback;
import android.app.backup.IBackupManager;
import android.os.RemoteException;

public class ServiceBackupCallback extends IBackupCallback.Stub {
    private final IBackupManager mBackupManager;
    private final int mToken;

    public ServiceBackupCallback(IBackupManager backupManager, int token) {
        this.mBackupManager = backupManager;
        this.mToken = token;
    }

    public void operationComplete(long result) throws RemoteException {
        this.mBackupManager.opComplete(this.mToken, result);
    }
}
