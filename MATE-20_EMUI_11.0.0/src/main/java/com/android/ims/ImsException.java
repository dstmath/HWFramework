package com.android.ims;

@Deprecated
public class ImsException extends Exception {
    private int mCode;

    public ImsException() {
    }

    public ImsException(String message, int code) {
        super(message + "(" + code + ")");
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
