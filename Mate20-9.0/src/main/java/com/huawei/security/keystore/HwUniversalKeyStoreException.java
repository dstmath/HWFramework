package com.huawei.security.keystore;

public class HwUniversalKeyStoreException extends Exception {
    private final int mErrorCode;

    public HwUniversalKeyStoreException(int errorCode, String message) {
        super(message);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
