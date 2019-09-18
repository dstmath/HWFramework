package com.huawei.nb.coordinator.helper;

public class CoordinatorSDKException extends Exception {
    private int errorCode;

    public CoordinatorSDKException(int errorCode2, String errMsg) {
        super(errMsg);
        this.errorCode = errorCode2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
