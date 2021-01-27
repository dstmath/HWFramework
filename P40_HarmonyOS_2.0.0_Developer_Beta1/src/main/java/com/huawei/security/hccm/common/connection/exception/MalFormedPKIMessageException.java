package com.huawei.security.hccm.common.connection.exception;

public class MalFormedPKIMessageException extends Exception {
    private static final long serialVersionUID = 1;
    private int mErrorCode;

    public MalFormedPKIMessageException() {
    }

    public MalFormedPKIMessageException(String message) {
        super(message);
    }

    public MalFormedPKIMessageException(String message, int errorCode) {
        super(message);
        this.mErrorCode = errorCode;
    }

    public MalFormedPKIMessageException(Throwable t) {
        super(t);
    }

    public MalFormedPKIMessageException(String message, Throwable t) {
        super(message, t);
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
