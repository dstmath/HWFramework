package com.huawei.android.feature.install;

public class InstallException extends RuntimeException {
    private int mErrorCode;

    public InstallException(int i) {
        super("Install Error: ".concat(String.valueOf(i)));
        this.mErrorCode = i;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
