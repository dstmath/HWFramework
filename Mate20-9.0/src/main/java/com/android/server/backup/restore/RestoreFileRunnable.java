package com.android.server.backup.restore;

import android.app.IBackupAgent;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.FileMetadata;
import java.io.IOException;

class RestoreFileRunnable implements Runnable {
    private final IBackupAgent mAgent;
    private final BackupManagerService mBackupManagerService;
    private final FileMetadata mInfo;
    private final ParcelFileDescriptor mSocket;
    private final int mToken;

    RestoreFileRunnable(BackupManagerService backupManagerService, IBackupAgent agent, FileMetadata info, ParcelFileDescriptor socket, int token) throws IOException {
        this.mAgent = agent;
        this.mInfo = info;
        this.mToken = token;
        this.mSocket = ParcelFileDescriptor.dup(socket.getFileDescriptor());
        this.mBackupManagerService = backupManagerService;
    }

    public void run() {
        try {
            this.mAgent.doRestoreFile(this.mSocket, this.mInfo.size, this.mInfo.type, this.mInfo.domain, this.mInfo.path, this.mInfo.mode, this.mInfo.mtime, this.mToken, this.mBackupManagerService.getBackupManagerBinder());
        } catch (RemoteException e) {
        }
    }
}
