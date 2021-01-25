package com.android.server.backup.internal;

import android.content.pm.PackageInfo;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.TransportManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.transport.TransportClient;
import java.io.File;

public class PerformClearTask implements Runnable {
    private final UserBackupManagerService mBackupManagerService;
    private final OnTaskFinishedListener mListener;
    private final PackageInfo mPackage;
    private final TransportClient mTransportClient;
    private final TransportManager mTransportManager;

    PerformClearTask(UserBackupManagerService backupManagerService, TransportClient transportClient, PackageInfo packageInfo, OnTaskFinishedListener listener) {
        this.mBackupManagerService = backupManagerService;
        this.mTransportManager = backupManagerService.getTransportManager();
        this.mTransportClient = transportClient;
        this.mPackage = packageInfo;
        this.mListener = listener;
    }

    @Override // java.lang.Runnable
    public void run() {
        StringBuilder sb;
        Exception e;
        IBackupTransport transport = null;
        try {
            new File(new File(this.mBackupManagerService.getBaseStateDir(), this.mTransportManager.getTransportDirName(this.mTransportClient.getTransportComponent())), this.mPackage.packageName).delete();
            transport = this.mTransportClient.connectOrThrow("PerformClearTask.run()");
            transport.clearBackupData(this.mPackage);
            try {
                transport.finishBackup();
            } catch (Exception e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (Exception e3) {
            Slog.e(BackupManagerService.TAG, "Transport threw clearing data for " + this.mPackage + ": " + e3.getMessage());
            if (transport != null) {
                try {
                    transport.finishBackup();
                } catch (Exception e4) {
                    e = e4;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable e5) {
            if (transport != null) {
                try {
                    transport.finishBackup();
                } catch (Exception e6) {
                    Slog.e(BackupManagerService.TAG, "Unable to mark clear operation finished: " + e6.getMessage());
                }
            }
            this.mListener.onFinished("PerformClearTask.run()");
            this.mBackupManagerService.getWakelock().release();
            throw e5;
        }
        this.mListener.onFinished("PerformClearTask.run()");
        this.mBackupManagerService.getWakelock().release();
        sb.append("Unable to mark clear operation finished: ");
        sb.append(e.getMessage());
        Slog.e(BackupManagerService.TAG, sb.toString());
        this.mListener.onFinished("PerformClearTask.run()");
        this.mBackupManagerService.getWakelock().release();
    }
}
