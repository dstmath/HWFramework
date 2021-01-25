package com.huawei.nb.coordinator.helper;

public class CoordinatorSDKException extends Exception {
    private int errorCode;

    public CoordinatorSDKException(int i, String str) {
        super(str);
        this.errorCode = i;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
