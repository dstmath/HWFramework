package com.android.server.backup.internal;

public class BackupRequest {
    public String packageName;

    public BackupRequest(String pkgName) {
        this.packageName = pkgName;
    }

    public String toString() {
        return "BackupRequest{pkg=" + this.packageName + "}";
    }
}
