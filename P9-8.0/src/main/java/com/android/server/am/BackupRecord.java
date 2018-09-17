package com.android.server.am;

import android.content.pm.ApplicationInfo;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;

final class BackupRecord {
    public static final int BACKUP_FULL = 1;
    public static final int BACKUP_NORMAL = 0;
    public static final int RESTORE = 2;
    public static final int RESTORE_FULL = 3;
    ProcessRecord app;
    final ApplicationInfo appInfo;
    final int backupMode;
    final Serv stats;
    String stringName;

    BackupRecord(Serv _agentStats, ApplicationInfo _appInfo, int _backupMode) {
        this.stats = _agentStats;
        this.appInfo = _appInfo;
        this.backupMode = _backupMode;
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackupRecord{").append(Integer.toHexString(System.identityHashCode(this))).append(' ').append(this.appInfo.packageName).append(' ').append(this.appInfo.name).append(' ').append(this.appInfo.backupAgentName).append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }
}
