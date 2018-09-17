package com.android.internal.telephony;

public interface HwChrServiceManager {
    public static final int CALL_TYPE_ANSWER = 2;
    public static final int CALL_TYPE_DIAL = 0;
    public static final int CALL_TYPE_DISCONNECT = 3;
    public static final int CALL_TYPE_INCOMMING = 1;

    void reportCallException(String str, int i, int i2, String str2);
}
