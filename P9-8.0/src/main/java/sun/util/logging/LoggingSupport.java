package sun.util.logging;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.List;

public class LoggingSupport {
    private static final String DEFAULT_FORMAT = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
    private static final String FORMAT_PROP_KEY = "java.util.logging.SimpleFormatter.format";
    private static final LoggingProxy proxy = ((LoggingProxy) AccessController.doPrivileged(new PrivilegedAction<LoggingProxy>() {
        public LoggingProxy run() {
            try {
                Field f = Class.forName("java.util.logging.LoggingProxyImpl", true, null).getDeclaredField("INSTANCE");
                f.setAccessible(true);
                return (LoggingProxy) f.get(null);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (Object e2) {
                throw new AssertionError(e2);
            } catch (Object e3) {
                throw new AssertionError(e3);
            }
        }
    }));

    private LoggingSupport() {
    }

    public static boolean isAvailable() {
        return proxy != null;
    }

    private static void ensureAvailable() {
        if (proxy == null) {
            throw new AssertionError((Object) "Should not here");
        }
    }

    public static List<String> getLoggerNames() {
        ensureAvailable();
        return proxy.getLoggerNames();
    }

    public static String getLoggerLevel(String loggerName) {
        ensureAvailable();
        return proxy.getLoggerLevel(loggerName);
    }

    public static void setLoggerLevel(String loggerName, String levelName) {
        ensureAvailable();
        proxy.setLoggerLevel(loggerName, levelName);
    }

    public static String getParentLoggerName(String loggerName) {
        ensureAvailable();
        return proxy.getParentLoggerName(loggerName);
    }

    public static Object getLogger(String name) {
        ensureAvailable();
        return proxy.getLogger(name);
    }

    public static Object getLevel(Object logger) {
        ensureAvailable();
        return proxy.getLevel(logger);
    }

    public static void setLevel(Object logger, Object newLevel) {
        ensureAvailable();
        proxy.setLevel(logger, newLevel);
    }

    public static boolean isLoggable(Object logger, Object level) {
        ensureAvailable();
        return proxy.isLoggable(logger, level);
    }

    public static void log(Object logger, Object level, String msg) {
        ensureAvailable();
        proxy.log(logger, level, msg);
    }

    public static void log(Object logger, Object level, String msg, Throwable t) {
        ensureAvailable();
        proxy.log(logger, level, msg, t);
    }

    public static void log(Object logger, Object level, String msg, Object... params) {
        ensureAvailable();
        proxy.log(logger, level, msg, params);
    }

    public static Object parseLevel(String levelName) {
        ensureAvailable();
        return proxy.parseLevel(levelName);
    }

    public static String getLevelName(Object level) {
        ensureAvailable();
        return proxy.getLevelName(level);
    }

    public static int getLevelValue(Object level) {
        ensureAvailable();
        return proxy.getLevelValue(level);
    }

    public static String getSimpleFormat() {
        return getSimpleFormat(true);
    }

    static String getSimpleFormat(boolean useProxy) {
        String format = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(LoggingSupport.FORMAT_PROP_KEY);
            }
        });
        if (useProxy && proxy != null && format == null) {
            format = proxy.getProperty(FORMAT_PROP_KEY);
        }
        if (format == null) {
            return DEFAULT_FORMAT;
        }
        try {
            String.format(format, new Date(), "", "", "", "", "");
            return format;
        } catch (IllegalArgumentException e) {
            return DEFAULT_FORMAT;
        }
    }
}
