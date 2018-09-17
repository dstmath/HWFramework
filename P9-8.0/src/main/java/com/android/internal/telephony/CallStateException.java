package com.android.internal.telephony;

public class CallStateException extends Exception {
    public static final int ERROR_DISCONNECTED = 1;
    public static final int ERROR_INVALID = -1;
    public static final int ERROR_POWER_OFF = 2;
    private int mError = -1;

    public CallStateException(String string) {
        super(string);
    }

    public CallStateException(int error, String string) {
        super(string);
        this.mError = error;
    }

    public int getError() {
        return this.mError;
    }
}
