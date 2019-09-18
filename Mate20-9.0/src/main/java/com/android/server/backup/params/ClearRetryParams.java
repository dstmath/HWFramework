package com.android.server.backup.params;

public class ClearRetryParams {
    public String packageName;
    public String transportName;

    public ClearRetryParams(String transportName2, String packageName2) {
        this.transportName = transportName2;
        this.packageName = packageName2;
    }
}
