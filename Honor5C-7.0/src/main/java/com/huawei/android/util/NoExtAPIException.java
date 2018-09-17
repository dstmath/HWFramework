package com.huawei.android.util;

public class NoExtAPIException extends RuntimeException {
    private static final long serialVersionUID = -5365630128856068164L;

    public NoExtAPIException(String detailMessage) {
        super(detailMessage);
    }

    public NoExtAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoExtAPIException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str, cause);
    }
}
