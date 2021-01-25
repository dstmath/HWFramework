package com.huawei.coauth.tlv;

public class TlvTransformException extends Exception {
    private String errorMsg;

    public TlvTransformException(String msg) {
        super(msg);
        this.errorMsg = msg;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public void setErrorMsg(String errorMsg2) {
        this.errorMsg = errorMsg2;
    }
}
