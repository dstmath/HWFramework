package android_maps_conflict_avoidance.com.google.debug;

import java.util.Hashtable;

public class Log {
    private static final String[] LEVEL_NAMES = new String[]{"ALL", "FINEST", "FINER", "FINE", "CONFIG", "INFO", "WARNING", "SEVERE", "NONE"};
    private static final Logger logger = ((Logger) DebugUtil.newInstance(logger()));
    private static final Hashtable timers = new Hashtable();

    private Log() {
    }

    private static Class logger() {
        String name = "android_maps_conflict_avoidance.com.google.debug.StdoutLogger";
        try {
            if (DebugUtil.isAntPropertyExpanded("android_maps_conflict_avoidance.com.google.debug.StdoutLogger")) {
                return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
            }
            String sysName = System.getProperty("LOGGER");
            if (sysName != null) {
                return Class.forName(sysName);
            }
            System.err.println("WARNING: Missing logger class - using default logger com.google.debug.StdoutLogger");
            System.err.println("         For Ant: Specify the logger class using the LOGGER property");
            System.err.println("         For Bolide: Specify the logger class using constant injection");
            System.err.println("         For J2SE:  Specify the logger class via the LOGGER system property");
            System.err.println("         See JavaDoc or source of com.google.debug.Log.");
            return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
        } catch (ClassNotFoundException e) {
            throw new Error("Missing logger class com.google.debug.StdoutLogger");
        }
    }

    public static void logThrowable(Object message, Throwable exception) {
        xlogThrowable(message, exception, null, null, -1);
    }

    public static void xlogThrowable(Object message, Throwable exception, String className, String methodName, int lineNumber) {
        xlogThrowable(message, exception, 5, className, methodName, lineNumber);
    }

    public static void xlogThrowable(Object message, Throwable exception, int logLevel, String className, String methodName, int lineNumber) {
        logger.logThrowable(message, exception, logLevel, className, methodName, lineNumber);
    }
}
