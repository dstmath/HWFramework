package com.huawei.security.hccm;

public class EnrollmentException extends Exception {
    private static final String TAG = "EnrollmentException";
    private static final long serialVersionUID = -8124081436619047682L;
    private int mErrorCode;

    public EnrollmentException(String message, int errorCode) {
        super(message);
        this.mErrorCode = errorCode;
    }

    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(Throwable t) {
        super(t);
    }

    public EnrollmentException(String message, Throwable t) {
        super(message, t);
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }
}
