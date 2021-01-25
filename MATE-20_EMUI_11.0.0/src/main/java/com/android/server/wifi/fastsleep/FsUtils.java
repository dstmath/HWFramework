package com.android.server.wifi.fastsleep;

public class FsUtils {
    public static final int BOOL_FALSE_TO_INT = 0;
    public static final int BOOL_TRUE_TO_INT = 1;
    public static final int MSG_FOREGROUNDACTIVITY_CHANGED = 7;
    public static final int MSG_INITIALIZE = 1;
    public static final int MSG_INTERNET_CONNECT = 8;
    public static final int MSG_INTERNET_DISCONNECT = 9;
    public static final int MSG_NETWORK_CHANGED = 6;
    public static final int MSG_P2P_CONNECTED = 4;
    public static final int MSG_ROAMING_COMPLETED = 10;
    public static final int MSG_WIFI_CONNECTED = 2;
    public static final int MSG_WIFI_DISCONNECTED = 3;
    public static final int MSG_WIFI_DISPLAY_CONNECTED = 5;
    public static final int NETWORK_UNKNOWN = 101;
    public static final int NETWORK_WIFI = 100;

    private FsUtils() {
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
}
