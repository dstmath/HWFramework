package com.huawei.nb.utils.logger;

public final class AndroidLogger {
    private static final Printer LOG_PRINTER = new LoggerPrinter();

    private AndroidLogger() {
    }

    public static Settings init(String str) {
        return LOG_PRINTER.init(str);
    }

    public static void v(String str, Object... objArr) {
        LOG_PRINTER.v(str, objArr);
    }

    public static void d(String str, Object... objArr) {
        LOG_PRINTER.d(str, objArr);
    }

    public static void i(String str, Object... objArr) {
        LOG_PRINTER.i(str, objArr);
    }

    public static void w(String str, Object... objArr) {
        LOG_PRINTER.w(str, objArr);
    }

    public static void e(String str, Object... objArr) {
        LOG_PRINTER.e(null, str, objArr);
    }

    public static void e(Throwable th, String str, Object... objArr) {
        LOG_PRINTER.e(th, str, objArr);
    }
}
