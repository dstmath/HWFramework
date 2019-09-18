package sun.util.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlatformLogger {
    private static final int ALL = Integer.MIN_VALUE;
    private static final int CONFIG = 700;
    /* access modifiers changed from: private */
    public static final Level DEFAULT_LEVEL = Level.INFO;
    private static final int FINE = 500;
    private static final int FINER = 400;
    private static final int FINEST = 300;
    private static final int INFO = 800;
    private static final int OFF = Integer.MAX_VALUE;
    private static final int SEVERE = 1000;
    private static final int WARNING = 900;
    private static Map<String, WeakReference<PlatformLogger>> loggers = new HashMap();
    private static boolean loggingEnabled = ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
        public Boolean run() {
            return Boolean.valueOf((System.getProperty("java.util.logging.config.class") == null && System.getProperty("java.util.logging.config.file") == null) ? false : true);
        }
    })).booleanValue();
    private volatile JavaLoggerProxy javaLoggerProxy;
    private volatile LoggerProxy loggerProxy;

    private static final class DefaultLoggerProxy extends LoggerProxy {
        private static final String formatString = LoggingSupport.getSimpleFormat(false);
        private Date date = new Date();
        volatile Level effectiveLevel = deriveEffectiveLevel(null);
        volatile Level level = null;

        private static PrintStream outputStream() {
            return System.err;
        }

        DefaultLoggerProxy(String name) {
            super(name);
        }

        /* access modifiers changed from: package-private */
        public boolean isEnabled() {
            return this.effectiveLevel != Level.OFF;
        }

        /* access modifiers changed from: package-private */
        public Level getLevel() {
            return this.level;
        }

        /* access modifiers changed from: package-private */
        public void setLevel(Level newLevel) {
            if (this.level != newLevel) {
                this.level = newLevel;
                this.effectiveLevel = deriveEffectiveLevel(newLevel);
            }
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level2, String msg) {
            if (isLoggable(level2)) {
                outputStream().print(format(level2, msg, null));
            }
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level2, String msg, Throwable thrown) {
            if (isLoggable(level2)) {
                outputStream().print(format(level2, msg, thrown));
            }
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level2, String msg, Object... params) {
            if (isLoggable(level2)) {
                outputStream().print(format(level2, formatMessage(msg, params), null));
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isLoggable(Level level2) {
            Level effectiveLevel2 = this.effectiveLevel;
            return level2.intValue() >= effectiveLevel2.intValue() && effectiveLevel2 != Level.OFF;
        }

        private Level deriveEffectiveLevel(Level level2) {
            return level2 == null ? PlatformLogger.DEFAULT_LEVEL : level2;
        }

        private String formatMessage(String format, Object... parameters) {
            if (parameters != null) {
                try {
                    if (parameters.length != 0) {
                        if (format.indexOf("{0") < 0 && format.indexOf("{1") < 0 && format.indexOf("{2") < 0) {
                            if (format.indexOf("{3") < 0) {
                                return format;
                            }
                        }
                        return MessageFormat.format(format, parameters);
                    }
                } catch (Exception e) {
                    return format;
                }
            }
            return format;
        }

        private synchronized String format(Level level2, String msg, Throwable thrown) {
            String throwable;
            this.date.setTime(System.currentTimeMillis());
            throwable = "";
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter((Writer) sw);
                pw.println();
                thrown.printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(formatString, this.date, getCallerInfo(), this.name, level2.name(), msg, throwable);
        }

        private String getCallerInfo() {
            String sourceClassName = null;
            String sourceMethodName = null;
            boolean lookingForLogger = true;
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            int length = stackTrace.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                StackTraceElement frame = stackTrace[i];
                String cname = frame.getClassName();
                if (lookingForLogger) {
                    if (cname.equals("sun.util.logging.PlatformLogger")) {
                        lookingForLogger = false;
                    }
                } else if (!cname.equals("sun.util.logging.PlatformLogger")) {
                    sourceClassName = cname;
                    sourceMethodName = frame.getMethodName();
                    break;
                }
                i++;
            }
            if (sourceClassName == null) {
                return this.name;
            }
            return sourceClassName + " " + sourceMethodName;
        }
    }

    private static final class JavaLoggerProxy extends LoggerProxy {
        private final Object javaLogger;

        static {
            for (Level level : Level.values()) {
                level.javaLevel = LoggingSupport.parseLevel(level.name());
            }
        }

        JavaLoggerProxy(String name) {
            this(name, null);
        }

        JavaLoggerProxy(String name, Level level) {
            super(name);
            this.javaLogger = LoggingSupport.getLogger(name);
            if (level != null) {
                LoggingSupport.setLevel(this.javaLogger, level.javaLevel);
            }
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level, String msg) {
            LoggingSupport.log(this.javaLogger, level.javaLevel, msg);
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level, String msg, Throwable t) {
            LoggingSupport.log(this.javaLogger, level.javaLevel, msg, t);
        }

        /* access modifiers changed from: package-private */
        public void doLog(Level level, String msg, Object... params) {
            if (isLoggable(level)) {
                int len = params != null ? params.length : 0;
                Object[] sparams = new String[len];
                for (int i = 0; i < len; i++) {
                    sparams[i] = String.valueOf(params[i]);
                }
                LoggingSupport.log(this.javaLogger, level.javaLevel, msg, sparams);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isEnabled() {
            return LoggingSupport.isLoggable(this.javaLogger, Level.OFF.javaLevel);
        }

        /* access modifiers changed from: package-private */
        public Level getLevel() {
            Object javaLevel = LoggingSupport.getLevel(this.javaLogger);
            if (javaLevel == null) {
                return null;
            }
            try {
                return Level.valueOf(LoggingSupport.getLevelName(javaLevel));
            } catch (IllegalArgumentException e) {
                return Level.valueOf(LoggingSupport.getLevelValue(javaLevel));
            }
        }

        /* access modifiers changed from: package-private */
        public void setLevel(Level level) {
            LoggingSupport.setLevel(this.javaLogger, level == null ? null : level.javaLevel);
        }

        /* access modifiers changed from: package-private */
        public boolean isLoggable(Level level) {
            return LoggingSupport.isLoggable(this.javaLogger, level.javaLevel);
        }
    }

    public enum Level {
        ALL,
        FINEST,
        FINER,
        FINE,
        CONFIG,
        INFO,
        WARNING,
        SEVERE,
        OFF;
        
        private static final int[] LEVEL_VALUES = null;
        Object javaLevel;

        static {
            LEVEL_VALUES = new int[]{Integer.MIN_VALUE, 300, 400, 500, PlatformLogger.CONFIG, PlatformLogger.INFO, PlatformLogger.WARNING, 1000, Integer.MAX_VALUE};
        }

        public int intValue() {
            return LEVEL_VALUES[ordinal()];
        }

        static Level valueOf(int level) {
            if (level == Integer.MIN_VALUE) {
                return ALL;
            }
            if (level == 300) {
                return FINEST;
            }
            if (level == 400) {
                return FINER;
            }
            if (level == 500) {
                return FINE;
            }
            if (level == PlatformLogger.CONFIG) {
                return CONFIG;
            }
            if (level == PlatformLogger.INFO) {
                return INFO;
            }
            if (level == PlatformLogger.WARNING) {
                return WARNING;
            }
            if (level == 1000) {
                return SEVERE;
            }
            if (level == Integer.MAX_VALUE) {
                return OFF;
            }
            int i = Arrays.binarySearch(LEVEL_VALUES, 0, LEVEL_VALUES.length - 2, level);
            return values()[i >= 0 ? i : (-i) - 1];
        }
    }

    private static abstract class LoggerProxy {
        final String name;

        /* access modifiers changed from: package-private */
        public abstract void doLog(Level level, String str);

        /* access modifiers changed from: package-private */
        public abstract void doLog(Level level, String str, Throwable th);

        /* access modifiers changed from: package-private */
        public abstract void doLog(Level level, String str, Object... objArr);

        /* access modifiers changed from: package-private */
        public abstract Level getLevel();

        /* access modifiers changed from: package-private */
        public abstract boolean isEnabled();

        /* access modifiers changed from: package-private */
        public abstract boolean isLoggable(Level level);

        /* access modifiers changed from: package-private */
        public abstract void setLevel(Level level);

        protected LoggerProxy(String name2) {
            this.name = name2;
        }
    }

    public static synchronized PlatformLogger getLogger(String name) {
        PlatformLogger log;
        synchronized (PlatformLogger.class) {
            log = null;
            WeakReference<PlatformLogger> ref = loggers.get(name);
            if (ref != null) {
                log = ref.get();
            }
            if (log == null) {
                log = new PlatformLogger(name);
                loggers.put(name, new WeakReference(log));
            }
        }
        return log;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003c, code lost:
        return;
     */
    public static synchronized void redirectPlatformLoggers() {
        synchronized (PlatformLogger.class) {
            if (!loggingEnabled) {
                if (LoggingSupport.isAvailable()) {
                    loggingEnabled = true;
                    for (Map.Entry<String, WeakReference<PlatformLogger>> entry : loggers.entrySet()) {
                        PlatformLogger plog = entry.getValue().get();
                        if (plog != null) {
                            plog.redirectToJavaLoggerProxy();
                        }
                    }
                }
            }
        }
    }

    private void redirectToJavaLoggerProxy() {
        DefaultLoggerProxy lp = DefaultLoggerProxy.class.cast(this.loggerProxy);
        JavaLoggerProxy jlp = new JavaLoggerProxy(lp.name, lp.level);
        this.javaLoggerProxy = jlp;
        this.loggerProxy = jlp;
    }

    private PlatformLogger(String name) {
        if (loggingEnabled) {
            JavaLoggerProxy javaLoggerProxy2 = new JavaLoggerProxy(name);
            this.javaLoggerProxy = javaLoggerProxy2;
            this.loggerProxy = javaLoggerProxy2;
            return;
        }
        this.loggerProxy = new DefaultLoggerProxy(name);
    }

    public boolean isEnabled() {
        return this.loggerProxy.isEnabled();
    }

    public String getName() {
        return this.loggerProxy.name;
    }

    public boolean isLoggable(Level level) {
        if (level != null) {
            JavaLoggerProxy jlp = this.javaLoggerProxy;
            return jlp != null ? jlp.isLoggable(level) : this.loggerProxy.isLoggable(level);
        }
        throw new NullPointerException();
    }

    public Level level() {
        return this.loggerProxy.getLevel();
    }

    public void setLevel(Level newLevel) {
        this.loggerProxy.setLevel(newLevel);
    }

    public void severe(String msg) {
        this.loggerProxy.doLog(Level.SEVERE, msg);
    }

    public void severe(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.SEVERE, msg, t);
    }

    public void severe(String msg, Object... params) {
        this.loggerProxy.doLog(Level.SEVERE, msg, params);
    }

    public void warning(String msg) {
        this.loggerProxy.doLog(Level.WARNING, msg);
    }

    public void warning(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.WARNING, msg, t);
    }

    public void warning(String msg, Object... params) {
        this.loggerProxy.doLog(Level.WARNING, msg, params);
    }

    public void info(String msg) {
        this.loggerProxy.doLog(Level.INFO, msg);
    }

    public void info(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.INFO, msg, t);
    }

    public void info(String msg, Object... params) {
        this.loggerProxy.doLog(Level.INFO, msg, params);
    }

    public void config(String msg) {
        this.loggerProxy.doLog(Level.CONFIG, msg);
    }

    public void config(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.CONFIG, msg, t);
    }

    public void config(String msg, Object... params) {
        this.loggerProxy.doLog(Level.CONFIG, msg, params);
    }

    public void fine(String msg) {
        this.loggerProxy.doLog(Level.FINE, msg);
    }

    public void fine(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.FINE, msg, t);
    }

    public void fine(String msg, Object... params) {
        this.loggerProxy.doLog(Level.FINE, msg, params);
    }

    public void finer(String msg) {
        this.loggerProxy.doLog(Level.FINER, msg);
    }

    public void finer(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.FINER, msg, t);
    }

    public void finer(String msg, Object... params) {
        this.loggerProxy.doLog(Level.FINER, msg, params);
    }

    public void finest(String msg) {
        this.loggerProxy.doLog(Level.FINEST, msg);
    }

    public void finest(String msg, Throwable t) {
        this.loggerProxy.doLog(Level.FINEST, msg, t);
    }

    public void finest(String msg, Object... params) {
        this.loggerProxy.doLog(Level.FINEST, msg, params);
    }
}
