package com.huawei.nb.utils.logger;

public final class ODMFLogger {
    private static Printer printer = new LoggerPrinter();

    private ODMFLogger() {
    }

    public static Settings init(String tag) {
        printer = new LoggerPrinter();
        return printer.init(tag);
    }

    public static void e(String message, Object... args) {
        printer.e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        printer.e(throwable, message, args);
    }

    public static void i(String message, Object... args) {
        printer.i(message, args);
    }
}
