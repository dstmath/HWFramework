package com.huawei.internal.telephony;

import com.android.ims.ImsException;

public class ImsExceptionExt extends Exception {
    private int mCode;

    public ImsExceptionExt() {
    }

    public ImsExceptionExt(String message) {
        super(message);
    }

    public ImsExceptionExt(String message, int code) {
        super(message + "(" + code + ")");
        this.mCode = code;
    }

    public ImsExceptionExt(String message, Throwable cause, int code) {
        super(message, cause);
        this.mCode = code;
    }

    public static ImsExceptionExt from(Object result) {
        if (!(result instanceof ImsException)) {
            return null;
        }
        ImsExceptionExt resultEx = new ImsExceptionExt();
        resultEx.setExceptionCode(((ImsException) result).getCode());
        return resultEx;
    }

    public static boolean isImsException(Object result) {
        return result instanceof ImsException;
    }

    public int getCode() {
        return this.mCode;
    }

    private void setExceptionCode(int code) {
        this.mCode = code;
    }
}
