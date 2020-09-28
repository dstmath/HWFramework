package com.huawei.ims;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ImsExceptionEx extends Exception {
    private int mCode;

    public ImsExceptionEx() {
    }

    public ImsExceptionEx(String message, int code) {
        super(message + "(" + code + ")");
        this.mCode = code;
    }

    public ImsExceptionEx(String message, Throwable cause, int code) {
        super(message, cause);
        this.mCode = code;
    }

    public int getCode() {
        return this.mCode;
    }
}
