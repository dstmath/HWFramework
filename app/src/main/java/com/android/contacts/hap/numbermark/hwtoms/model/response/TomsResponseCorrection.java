package com.android.contacts.hap.numbermark.hwtoms.model.response;

public class TomsResponseCorrection {
    private String errorCode;

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String toString() {
        return "[errorCode = " + this.errorCode + "]";
    }
}
