package com.huawei.nb.exception;

public class NBIllegalArgumentException extends NBException {
    public NBIllegalArgumentException() {
    }

    public NBIllegalArgumentException(String str) {
        super(str);
    }

    public NBIllegalArgumentException(Throwable th) {
        super(th);
    }
}
