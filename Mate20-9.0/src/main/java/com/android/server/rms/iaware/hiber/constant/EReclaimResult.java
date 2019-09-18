package com.android.server.rms.iaware.hiber.constant;

public enum EReclaimResult {
    SEND_PRO_TO_NATIVE_ERR(-4),
    RECLAIM_BE_INTERRUPT(-3),
    HAS_BEEN_RECLAIMED(-2),
    OTHER_ERR(-1),
    ALL_SUCCESS(0);
    
    private int nCode;

    private EReclaimResult(int code) {
        this.nCode = code;
    }

    public int getValue() {
        return this.nCode;
    }
}
