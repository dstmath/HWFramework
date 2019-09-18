package com.unionpay.tsmservice.widget;

public class KeyboardDrawableErrorException extends Exception {
    public KeyboardDrawableErrorException() {
    }

    public KeyboardDrawableErrorException(String str) {
        super(str);
    }

    public KeyboardDrawableErrorException(String str, Throwable th) {
        super(str, th);
    }

    public KeyboardDrawableErrorException(Throwable th) {
        super(th);
    }
}
