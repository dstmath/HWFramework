package com.huawei.nb.exception;

public class NBIllegalArgumentException extends NBException {
    public NBIllegalArgumentException() {
    }

    public NBIllegalArgumentException(String message) {
        super(message);
    }

    public NBIllegalArgumentException(Throwable cause) {
        super(cause);
    }
}
