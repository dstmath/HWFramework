package com.huawei.wallet.sdk.business.buscard.base.util;

public class AppletCardException extends Exception {
    private int errCode;

    public AppletCardException(int errCode2) {
        this.errCode = errCode2;
    }

    public AppletCardException(int errCode2, String errMsg) {
        super(errMsg);
        this.errCode = errCode2;
    }

    public int getErrCode() {
        return this.errCode;
    }
}
