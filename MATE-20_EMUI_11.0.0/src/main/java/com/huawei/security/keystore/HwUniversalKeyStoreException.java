package com.huawei.security.keystore;

public class HwUniversalKeyStoreException extends Exception {
    private static final long serialVersionUID = 5193603475992300425L;
    private final int mErrorCode;

    public HwUniversalKeyStoreException(int errorCode, String message) {
        super(message);
        this.mErrorCode = errorCode;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
