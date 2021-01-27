package com.huawei.ace.runtime;

public class ALog {
    private static ILogger logger;

    private ALog() {
    }

    public static void setLogger(ILogger iLogger) {
        logger = iLogger;
    }

    public static boolean isDebuggable() {
        return logger.isDebuggable();
    }

    public static void d(String str, String str2) {
        ILogger iLogger = logger;
        if (iLogger != null) {
            iLogger.d(str, str2);
        }
    }

    public static void i(String str, String str2) {
        ILogger iLogger = logger;
        if (iLogger != null) {
            iLogger.i(str, str2);
        }
    }

    public static void w(String str, String str2) {
        ILogger iLogger = logger;
        if (iLogger != null) {
            iLogger.w(str, str2);
        }
    }

    public static void e(String str, String str2) {
        ILogger iLogger = logger;
        if (iLogger != null) {
            iLogger.e(str, str2);
        }
    }
}
