package com.huawei.android.pushagent.datatype.exception;

public class PushException extends Exception {
    private static final long serialVersionUID = 7592779487345488164L;
    public ErrorType type;

    public enum ErrorType {
        Err_unKnown,
        Err_Device,
        Err_Connect,
        Err_Read
    }

    public PushException() {
        this.type = ErrorType.Err_unKnown;
    }

    private PushException(String str) {
        super(str);
        this.type = ErrorType.Err_unKnown;
    }

    public PushException(Throwable th) {
        super(th);
        this.type = ErrorType.Err_unKnown;
    }

    public PushException(String str, ErrorType errorType) {
        this(str);
        this.type = errorType;
    }

    public PushException(Throwable th, ErrorType errorType) {
        this(th);
        this.type = errorType;
    }
}
