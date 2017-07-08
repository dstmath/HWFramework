package com.android.internal.telephony;

public class CallStateException extends Exception {
    public static final int ERROR_DISCONNECTED = 1;
    public static final int ERROR_INVALID = -1;
    private int mError;

    public CallStateException() {
        this.mError = ERROR_INVALID;
    }

    public CallStateException(String string) {
        super(string);
        this.mError = ERROR_INVALID;
    }

    public CallStateException(int error, String string) {
        super(string);
        this.mError = ERROR_INVALID;
        this.mError = error;
    }

    public int getError() {
        return this.mError;
    }
}
