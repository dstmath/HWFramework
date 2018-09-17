package sun.util.logging;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.List;

public class LoggingSupport {
    private static final String DEFAULT_FORMAT = "%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp %2$s%n%4$s: %5$s%6$s%n";
    private static final String FORMAT_PROP_KEY = "java.util.logging.SimpleFormatter.format";
    private static final LoggingProxy proxy = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.logging.LoggingSupport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.logging.LoggingSupport.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.LoggingSupport.<clinit>():void");
    }

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
