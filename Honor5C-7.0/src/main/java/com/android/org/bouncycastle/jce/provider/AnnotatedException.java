package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.jce.exception.ExtException;

public class AnnotatedException extends Exception implements ExtException {
    private Throwable _underlyingException;

    public AnnotatedException(String string, Throwable e) {
        super(string);
        this._underlyingException = e;
    }

    public AnnotatedException(String string) {
        this(string, null);
    }

    Throwable getUnderlyingException() {
        return this._underlyingException;
    }

    public Throwable getCause() {
        return this._underlyingException;
    }
}
