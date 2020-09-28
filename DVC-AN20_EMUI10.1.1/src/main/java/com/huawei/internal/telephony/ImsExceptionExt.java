package com.huawei.internal.telephony;

public class ImsExceptionExt extends Exception {
    private int mCode;

    public ImsExceptionExt() {
    }

    public ImsExceptionExt(String message) {
        super(message);
    }

    public ImsExceptionExt(String message, int code) {
        super(message + "(" + code + ")");
        this.mCode = code;
    }

    public ImsExceptionExt(String message, Throwable cause, int code) {
        super(message, cause);
        this.mCode = code;
    }

    public int getCode() {
        return this.mCode;
    }
}
