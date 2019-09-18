package com.huawei.nb.utils.logger;

public class DSLog {
    private static final Object LOCK = new Object();
    public static final int SHOW_LINE_NUMBER = 4;
    public static final int SHOW_METHOD = 2;
    public static final int SHOW_NOTHING = 0;
    public static final int SHOW_THREAD = 1;
    private static boolean hasInitialized = false;

    public static void init(String tag) {
        init(tag, 0);
    }

    public static void init(String tag, int flags) {
        synchronized (LOCK) {
            if (!hasInitialized) {
                hasInitialized = true;
                Settings settings = AndroidLogger.init(tag);
                settings.logAdapter(new AndroidLogAdapter());
                settings.methodOffset(1);
                settings.methodCount(1);
                Settings odmfSettings = ODMFLogger.init(tag);
                odmfSettings.logAdapter(new ODMFLogAdapter());
                odmfSettings.methodOffset(1);
                odmfSettings.methodCount(1);
                if ((flags & 1) == 0) {
                    settings.hideThreadInfo();
                    odmfSettings.hideThreadInfo();
                }
                if ((flags & 2) == 0) {
                    settings.hideMethodInfo();
                    odmfSettings.hideMethodInfo();
                }
                if ((flags & 4) == 0) {
                    settings.hideLineNumber();
                    odmfSettings.hideLineNumber();
                }
            }
        }
    }

    public static void d(String message, Object... args) {
        AndroidLogger.d(message, args);
    }

    public static void dt(String tag, String message, Object... args) {
        AndroidLogger.d(buildMessage(tag, message), args);
    }

    public static void e(String message, Object... args) {
        AndroidLogger.e(message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        AndroidLogger.e(throwable, message, args);
    }

    public static void et(String tag, String message, Object... args) {
        AndroidLogger.e(buildMessage(tag, message), args);
    }

    public static void et(String tag, Throwable throwable, String message, Object... args) {
        AndroidLogger.e(throwable, buildMessage(tag, message), args);
    }

    public static void w(String message, Object... args) {
        AndroidLogger.w(message, args);
    }

    public static void wt(String tag, String message, Object... args) {
        AndroidLogger.w(buildMessage(tag, message), args);
    }

    public static void i(String message, Object... args) {
        AndroidLogger.i(message, args);
    }

    public static void it(String tag, String message, Object... args) {
        AndroidLogger.i(buildMessage(tag, message), args);
    }

    public static void v(String message, Object... args) {
        AndroidLogger.v(message, args);
    }

    public static void vt(String tag, String message, Object... args) {
        AndroidLogger.v(buildMessage(tag, message), args);
    }

    public static void k(String message, Object... args) {
        ODMFLogger.e(message, args);
    }

    public static void ki(String message, Object... args) {
        AndroidLogger.i(message, args);
        ODMFLogger.i(message, args);
    }

    public static void ke(String message, Object... args) {
        AndroidLogger.e(message, args);
        ODMFLogger.e(message, args);
    }

    public static void ke(Throwable throwable, String message, Object... args) {
        AndroidLogger.e(throwable, message, args);
        ODMFLogger.e(throwable, message, args);
    }

    public static void ket(String tag, String message, Object... args) {
        AndroidLogger.e(buildMessage(tag, message), args);
        ODMFLogger.e(buildMessage(tag, message), args);
    }

    public static void ket(String tag, Throwable throwable, String message, Object... args) {
        AndroidLogger.e(throwable, buildMessage(tag, message), args);
        ODMFLogger.e(throwable, buildMessage(tag, message), args);
    }

    private static String buildMessage(String tag, String message) {
        return tag == null ? message : tag + ": " + message;
    }
}
