package com.huawei.security.hccm.common.connection.exception;

public class RAConnectionException extends Exception {
    private static final long serialVersionUID = 1;

    public RAConnectionException() {
    }

    public RAConnectionException(String message) {
        super(message);
    }

    public RAConnectionException(Throwable t) {
        super(t);
    }

    public RAConnectionException(String message, Throwable t) {
        super(message, t);
    }
}
