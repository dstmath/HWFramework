package com.android.server.wifi.rxlisten;

public class RxListenUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    public static final int MSG_GAMEACTIVITY_CHANGED = 2;
    public static final int MSG_INITIALIZE = 1;

    private RxListenUtils() {
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
