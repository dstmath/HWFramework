package com.android.server.wifi.tcpack;

public class TcpAckUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    public static final int MSG_CHARIOT_CHANGED = 2;
    public static final int MSG_INITIALIZE = 1;
    public static final int MSG_SCREEN_OFF = 3;
    public static final int MSG_SCREEN_ON = 4;

    private TcpAckUtils() {
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
