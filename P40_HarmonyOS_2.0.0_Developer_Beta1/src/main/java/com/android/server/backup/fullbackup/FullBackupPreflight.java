package com.android.server.backup.fullbackup;

import android.app.IBackupAgent;
import android.content.pm.PackageInfo;

public interface FullBackupPreflight {
    long getExpectedSizeOrErrorCode();

    int preflightFullBackup(PackageInfo packageInfo, IBackupAgent iBackupAgent);
}
