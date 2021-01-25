package com.android.server.am;

import android.content.pm.ApplicationInfo;

/* access modifiers changed from: package-private */
public final class BackupRecord {
    public static final int BACKUP_FULL = 1;
    public static final int BACKUP_NORMAL = 0;
    public static final int RESTORE = 2;
    public static final int RESTORE_FULL = 3;
    ProcessRecord app;
    final ApplicationInfo appInfo;
    final int backupMode;
    String stringName;
    final int userId;

    BackupRecord(ApplicationInfo _appInfo, int _backupMode, int _userId) {
        this.appInfo = _appInfo;
        this.backupMode = _backupMode;
        this.userId = _userId;
    }

    public String toString() {
        String str = this.stringName;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackupRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.appInfo.packageName);
        sb.append(' ');
        sb.append(this.appInfo.name);
        sb.append(' ');
        sb.append(this.appInfo.backupAgentName);
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }
}
