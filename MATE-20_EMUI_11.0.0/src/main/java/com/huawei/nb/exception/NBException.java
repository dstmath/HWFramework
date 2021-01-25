package com.huawei.nb.exception;

public class NBException extends RuntimeException {
    public NBException() {
    }

    public NBException(String str) {
        super(str);
    }

    public NBException(Throwable th) {
        super(th);
    }
}
