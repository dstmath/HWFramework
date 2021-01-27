package com.android.server.mtm.iaware.srms;

public class AwareBroadcastDebug {
    private static boolean sDebugDetail = false;
    private static boolean sDebugStatus = true;
    private static boolean sFilterDebug = false;

    public static void enableDebug() {
        sDebugStatus = true;
        sDebugDetail = true;
    }

    public static void disableDebug() {
        sDebugStatus = false;
        sDebugDetail = false;
    }

    public static boolean getDebug() {
        return sDebugStatus;
    }

    public static boolean getDebugDetail() {
        return sDebugDetail;
    }

    public static void enableFilterDebug() {
        sFilterDebug = true;
    }

    public static void disableFilterDebug() {
        sFilterDebug = false;
    }

    public static boolean getFilterDebug() {
        return sFilterDebug;
    }
}
