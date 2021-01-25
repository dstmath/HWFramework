package com.huawei.nb.searchmanager.utils.logger;

public class DSLog {
    private static final Object LOCK = new Object();
    public static final int SHOW_LINE_NUMBER = 4;
    public static final int SHOW_METHOD = 2;
    public static final int SHOW_NOTHING = 0;
    public static final int SHOW_THREAD = 1;
    private static boolean hasInitialized = false;

    public static void init(String str) {
        init(str, 0);
    }

    public static void init(String str, int i) {
        synchronized (LOCK) {
            if (!hasInitialized) {
                hasInitialized = true;
                Settings init = AndroidLogger.init(str);
                init.logAdapter(new AndroidLogAdapter());
                init.methodOffset(1);
                init.methodCount(1);
                if ((i & 1) == 0) {
                    init.hideThreadInfo();
                }
                if ((i & 2) == 0) {
                    init.hideMethodInfo();
                }
                if ((i & 4) == 0) {
                    init.hideLineNumber();
                }
            }
        }
    }

    public static void d(String str, Object... objArr) {
        AndroidLogger.d(str, objArr);
    }

    public static void dt(String str, String str2, Object... objArr) {
        AndroidLogger.d(buildMessage(str, str2), objArr);
    }

    public static void e(String str, Object... objArr) {
        AndroidLogger.e(str, objArr);
    }

    public static void e(Throwable th, String str, Object... objArr) {
        AndroidLogger.e(th, str, objArr);
    }

    public static void et(String str, String str2, Object... objArr) {
        AndroidLogger.e(buildMessage(str, str2), objArr);
    }

    public static void et(String str, Throwable th, String str2, Object... objArr) {
        AndroidLogger.e(th, buildMessage(str, str2), objArr);
    }

    public static void w(String str, Object... objArr) {
        AndroidLogger.w(str, objArr);
    }

    public static void wt(String str, String str2, Object... objArr) {
        AndroidLogger.w(buildMessage(str, str2), objArr);
    }

    public static void i(String str, Object... objArr) {
        AndroidLogger.i(str, objArr);
    }

    public static void it(String str, String str2, Object... objArr) {
        AndroidLogger.i(buildMessage(str, str2), objArr);
    }

    public static void v(String str, Object... objArr) {
        AndroidLogger.v(str, objArr);
    }

    public static void vt(String str, String str2, Object... objArr) {
        AndroidLogger.v(buildMessage(str, str2), objArr);
    }

    public static void ke(String str, Object... objArr) {
        AndroidLogger.e(str, objArr);
    }

    public static void ke(Throwable th, String str, Object... objArr) {
        AndroidLogger.e(th, str, objArr);
    }

    public static void ket(String str, String str2, Object... objArr) {
        AndroidLogger.e(buildMessage(str, str2), objArr);
    }

    private static String buildMessage(String str, String str2) {
        if (str == null) {
            return str2;
        }
        return str + ": " + str2;
    }
}
