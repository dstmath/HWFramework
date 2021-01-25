package com.huawei.nb.utils.logger;

public final class ODMFLogger {
    private static Printer printer = new LoggerPrinter();

    private ODMFLogger() {
    }

    public static Settings init(String str) {
        printer = new LoggerPrinter();
        return printer.init(str);
    }

    public static void e(String str, Object... objArr) {
        printer.e(null, str, objArr);
    }

    public static void e(Throwable th, String str, Object... objArr) {
        printer.e(th, str, objArr);
    }

    public static void i(String str, Object... objArr) {
        printer.i(str, objArr);
    }
}
