package com.huawei.internal.telephony;

public class EncodeExceptionEx extends Exception {
    public static final int ERROR_EXCEED_SIZE = 1;
    public static final int ERROR_UNENCODABLE = 0;
    private int mError = 0;

    public EncodeExceptionEx(String str, int error) {
        super(str);
        this.mError = error;
    }
}
