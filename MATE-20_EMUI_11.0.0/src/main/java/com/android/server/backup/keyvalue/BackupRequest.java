package com.android.server.backup.keyvalue;

import java.util.Objects;

public class BackupRequest {
    public String packageName;

    public BackupRequest(String pkgName) {
        this.packageName = pkgName;
    }

    public String toString() {
        return "BackupRequest{pkg=" + this.packageName + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BackupRequest)) {
            return false;
        }
        return Objects.equals(this.packageName, ((BackupRequest) o).packageName);
    }

    public int hashCode() {
        return Objects.hash(this.packageName);
    }
}
