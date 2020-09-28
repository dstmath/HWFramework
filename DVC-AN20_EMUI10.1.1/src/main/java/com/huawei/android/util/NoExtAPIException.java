package com.huawei.android.util;

public class NoExtAPIException extends RuntimeException {
    private static final long serialVersionUID = -5365630128856068164L;

    public NoExtAPIException() {
    }

    public NoExtAPIException(String detailMessage) {
        super(detailMessage);
    }

    public NoExtAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NoExtAPIException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }
}
