package gov.nist.core;

public class Debug {
    public static boolean debug = false;
    public static boolean parserDebug = false;
    static StackLogger stackLogger;

    public static void setStackLogger(StackLogger stackLogger2) {
        stackLogger = stackLogger2;
    }

    public static void println(String s) {
        StackLogger stackLogger2;
        if ((parserDebug || debug) && (stackLogger2 = stackLogger) != null) {
            stackLogger2.logDebug(s + Separators.RETURN);
        }
    }

    public static void printStackTrace(Exception ex) {
        StackLogger stackLogger2;
        if ((parserDebug || debug) && (stackLogger2 = stackLogger) != null) {
            stackLogger2.logError("Stack Trace", ex);
        }
    }

    public static void logError(String message, Exception ex) {
        StackLogger stackLogger2;
        if ((parserDebug || debug) && (stackLogger2 = stackLogger) != null) {
            stackLogger2.logError(message, ex);
        }
    }
}
