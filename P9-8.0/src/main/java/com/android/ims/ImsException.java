package com.android.ims;

public class ImsException extends Exception {
    private int mCode;

    public ImsException(String message, int code) {
        super(message + ", code = " + code);
        this.mCode = code;
    }

    public ImsException(String message, Throwable cause, int code) {
        super(message, cause);
        this.mCode = code;
    }

    public int getCode() {
        return this.mCode;
    }
}
