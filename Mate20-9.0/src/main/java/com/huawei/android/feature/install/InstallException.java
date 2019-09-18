package com.huawei.android.feature.install;

public class InstallException extends RuntimeException {
    private int mErrorCode;

    public InstallException(int i) {
        super("Install Error: " + i);
        this.mErrorCode = i;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
