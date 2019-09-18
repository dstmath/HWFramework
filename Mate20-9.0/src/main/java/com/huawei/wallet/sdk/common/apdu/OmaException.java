package com.huawei.wallet.sdk.common.apdu;

public class OmaException extends Exception {
    private int errorCode;
    private String rapdu;

    public OmaException(int errorCode2, String message) {
        super(message);
        this.errorCode = errorCode2;
    }

    public String getRapdu() {
        return this.rapdu;
    }

    public void setRapdu(String rapdu2) {
        this.rapdu = rapdu2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }
}
