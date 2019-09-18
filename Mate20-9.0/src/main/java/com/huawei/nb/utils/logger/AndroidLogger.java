package com.huawei.nb.utils.logger;

public final class AndroidLogger {
    private static final Printer logPrinter = new LoggerPrinter();

    private AndroidLogger() {
    }

    public static Settings init(String tag) {
        return logPrinter.init(tag);
    }

    public static void v(String message, Object... args) {
        logPrinter.v(message, args);
    }

    public static void d(String message, Object... args) {
        logPrinter.d(message, args);
    }

    public static void i(String message, Object... args) {
        logPrinter.i(message, args);
    }

    public static void w(String message, Object... args) {
        logPrinter.w(message, args);
    }

    public static void e(String message, Object... args) {
        logPrinter.e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        logPrinter.e(throwable, message, args);
    }
}
