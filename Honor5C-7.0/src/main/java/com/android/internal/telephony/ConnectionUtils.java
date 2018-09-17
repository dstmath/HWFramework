package com.android.internal.telephony;

public class ConnectionUtils {
    public static void setConnectTime(Connection connection, long connectTime) {
        connection.mConnectTime = connectTime;
    }

    public static void setConnectTimeReal(Connection connection, long connectTimeReal) {
        connection.mConnectTimeReal = connectTimeReal;
    }

    public static void setDuration(Connection connection, long duration) {
        connection.mDuration = duration;
    }
}
