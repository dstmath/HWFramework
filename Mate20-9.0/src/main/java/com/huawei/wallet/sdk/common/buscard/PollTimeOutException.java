package com.huawei.wallet.sdk.common.buscard;

public class PollTimeOutException extends Exception {
    private int errorCode;

    public PollTimeOutException(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
