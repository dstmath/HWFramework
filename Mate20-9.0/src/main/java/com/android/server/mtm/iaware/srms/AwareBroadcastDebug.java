package com.android.server.mtm.iaware.srms;

public class AwareBroadcastDebug {
    private static boolean debug = true;
    private static boolean debug_detail = false;
    private static boolean filter_debug = false;

    public static void enableDebug() {
        debug = true;
        debug_detail = true;
    }

    public static void disableDebug() {
        debug = false;
        debug_detail = false;
    }

    public static boolean getDebug() {
        return debug;
    }

    public static boolean getDebugDetail() {
        return debug_detail;
    }

    public static void enableFilterDebug() {
        filter_debug = true;
    }

    public static void disableFilterDebug() {
        filter_debug = false;
    }

    public static boolean getFilterDebug() {
        return filter_debug;
    }
}
