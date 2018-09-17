package com.google.android.mms;

public class MmsException extends Exception {
    private static final long serialVersionUID = -7323249827281485390L;

    public MmsException(String message) {
        super(message);
    }

    public MmsException(Throwable cause) {
        super(cause);
    }

    public MmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
