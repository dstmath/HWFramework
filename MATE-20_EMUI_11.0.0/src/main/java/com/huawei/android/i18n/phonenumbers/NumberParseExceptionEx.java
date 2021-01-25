package com.huawei.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.NumberParseException;

public class NumberParseExceptionEx extends Exception {
    private NumberParseException exception;

    protected NumberParseExceptionEx(NumberParseException e) {
        this.exception = e;
    }

    public NumberParseException.ErrorType getErrorType() {
        return this.exception.getErrorType();
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        return this.exception.toString();
    }
}
