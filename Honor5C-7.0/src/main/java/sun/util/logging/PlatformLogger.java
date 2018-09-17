package sun.util.logging;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class PlatformLogger {
    public static final int ALL = Integer.MIN_VALUE;
    public static final int CONFIG = 700;
    private static final Level DEFAULT_LEVEL = null;
    public static final int FINE = 500;
    public static final int FINER = 400;
    public static final int FINEST = 300;
    public static final int INFO = 800;
    public static final int OFF = Integer.MAX_VALUE;
    public static final int SEVERE = 1000;
    public static final int WARNING = 900;
    private static Map<String, WeakReference<PlatformLogger>> loggers;
    private static boolean loggingEnabled;
    private volatile JavaLoggerProxy javaLoggerProxy;
    private volatile LoggerProxy loggerProxy;

    private static abstract class LoggerProxy {
        final String name;

        abstract void doLog(Level level, String str);

        abstract void doLog(Level level, String str, Throwable th);

        abstract void doLog(Level level, String str, Object... objArr);

        abstract Level getLevel();

        abstract boolean isEnabled();

        abstract boolean isLoggable(Level level);

        abstract void setLevel(Level level);

        protected LoggerProxy(String name) {
            this.name = name;
        }
    }

    private static final class DefaultLoggerProxy extends LoggerProxy {
        private static final String formatString = null;
        private Date date;
        volatile Level effectiveLevel;
        volatile Level level;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.logging.PlatformLogger.DefaultLoggerProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.logging.PlatformLogger.DefaultLoggerProxy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.PlatformLogger.DefaultLoggerProxy.<clinit>():void");
        }

        private static PrintStream outputStream() {
            return System.err;
        }

        DefaultLoggerProxy(String name) {
            super(name);
            this.date = new Date();
            this.effectiveLevel = deriveEffectiveLevel(null);
            this.level = null;
        }

        boolean isEnabled() {
            return this.effectiveLevel != Level.OFF;
        }

        Level getLevel() {
            return this.level;
        }

        void setLevel(Level newLevel) {
            if (this.level != newLevel) {
                this.level = newLevel;
                this.effectiveLevel = deriveEffectiveLevel(newLevel);
            }
        }

        void doLog(Level level, String msg) {
            if (isLoggable(level)) {
                outputStream().print(format(level, msg, null));
            }
        }

        void doLog(Level level, String msg, Throwable thrown) {
            if (isLoggable(level)) {
                outputStream().print(format(level, msg, thrown));
            }
        }

        void doLog(Level level, String msg, Object... params) {
            if (isLoggable(level)) {
                outputStream().print(format(level, formatMessage(msg, params), null));
            }
        }

        boolean isLoggable(Level level) {
            Level effectiveLevel = this.effectiveLevel;
            if (level.intValue() < effectiveLevel.intValue() || effectiveLevel == Level.OFF) {
                return false;
            }
            return true;
        }

        private Level deriveEffectiveLevel(Level level) {
            return level == null ? PlatformLogger.DEFAULT_LEVEL : level;
        }

        private String formatMessage(String format, Object... parameters) {
            if (parameters != null) {
                try {
                    if (parameters.length != 0) {
                        if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
                            return MessageFormat.format(format, parameters);
                        }
                        return format;
                    }
                } catch (Exception e) {
                    return format;
                }
            }
            return format;
        }

        private synchronized String format(Level level, String msg, Throwable thrown) {
            String throwable;
            this.date.setTime(System.currentTimeMillis());
            throwable = "";
            if (thrown != null) {
                Writer sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                thrown.printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(formatString, this.date, getCallerInfo(), this.name, level.name(), msg, throwable);
        }

        private String getCallerInfo() {
            String str = null;
            String sourceMethodName = null;
            String logClassName = "sun.util.logging.PlatformLogger";
            boolean lookingForLogger = true;
            for (StackTraceElement frame : new Throwable().getStackTrace()) {
                String cname = frame.getClassName();
                if (lookingForLogger) {
                    if (cname.equals(logClassName)) {
                        lookingForLogger = false;
                    }
                } else if (!cname.equals(logClassName)) {
                    str = cname;
                    sourceMethodName = frame.getMethodName();
                    break;
                }
            }
            if (str != null) {
                return str + " " + sourceMethodName;
            }
            return this.name;
        }
    }

    private static final class JavaLoggerProxy extends LoggerProxy {
        private final Object javaLogger;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.logging.PlatformLogger.JavaLoggerProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.logging.PlatformLogger.JavaLoggerProxy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.PlatformLogger.JavaLoggerProxy.<clinit>():void");
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

        void doLog(Level level, String msg) {
            LoggingSupport.log(this.javaLogger, level.javaLevel, msg);
        }

        void doLog(Level level, String msg, Throwable t) {
            LoggingSupport.log(this.javaLogger, level.javaLevel, msg, t);
        }

        void doLog(Level level, String msg, Object... params) {
            if (isLoggable(level)) {
                int len = params != null ? params.length : 0;
                Object[] sparams = new String[len];
                for (int i = 0; i < len; i++) {
                    sparams[i] = String.valueOf(params[i]);
                }
                LoggingSupport.log(this.javaLogger, level.javaLevel, msg, sparams);
            }
        }

        boolean isEnabled() {
            return LoggingSupport.isLoggable(this.javaLogger, Level.OFF.javaLevel);
        }

        Level getLevel() {
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

        void setLevel(Level level) {
            Object obj = null;
            Object obj2 = this.javaLogger;
            if (level != null) {
                obj = level.javaLevel;
            }
            LoggingSupport.setLevel(obj2, obj);
        }

        boolean isLoggable(Level level) {
            return LoggingSupport.isLoggable(this.javaLogger, level.javaLevel);
        }
    }

    public enum Level {
        ;
        
        private static final int[] levelValues = null;
        Object javaLevel;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.logging.PlatformLogger.Level.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.logging.PlatformLogger.Level.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.PlatformLogger.Level.<clinit>():void");
        }

        public int intValue() {
            return levelValues[ordinal()];
        }

        static Level valueOf(int level) {
            switch (level) {
                case PlatformLogger.ALL /*-2147483648*/:
                    return ALL;
                case PlatformLogger.FINEST /*300*/:
                    return FINEST;
                case PlatformLogger.FINER /*400*/:
                    return FINER;
                case PlatformLogger.FINE /*500*/:
                    return FINE;
                case PlatformLogger.CONFIG /*700*/:
                    return CONFIG;
                case PlatformLogger.INFO /*800*/:
                    return INFO;
                case PlatformLogger.WARNING /*900*/:
                    return WARNING;
                case PlatformLogger.SEVERE /*1000*/:
                    return SEVERE;
                case PlatformLogger.OFF /*2147483647*/:
                    return OFF;
                default:
                    int i = Arrays.binarySearch(levelValues, 0, levelValues.length - 2, level);
                    Level[] values = values();
                    if (i < 0) {
                        i = (-i) - 1;
                    }
                    return values[i];
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.logging.PlatformLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.logging.PlatformLogger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.logging.PlatformLogger.<clinit>():void");
    }

    public static synchronized PlatformLogger getLogger(String name) {
        Throwable th;
        synchronized (PlatformLogger.class) {
            try {
                PlatformLogger log;
                PlatformLogger log2;
                WeakReference<PlatformLogger> ref = (WeakReference) loggers.get(name);
                if (ref != null) {
                    log = (PlatformLogger) ref.get();
                } else {
                    log = null;
                }
                if (log == null) {
                    try {
                        log2 = new PlatformLogger(name);
                        loggers.put(name, new WeakReference(log2));
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                log2 = log;
                return log2;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public static synchronized void redirectPlatformLoggers() {
        synchronized (PlatformLogger.class) {
            if (loggingEnabled || !LoggingSupport.isAvailable()) {
                return;
            }
            loggingEnabled = true;
            for (Entry<String, WeakReference<PlatformLogger>> entry : loggers.entrySet()) {
                PlatformLogger plog = (PlatformLogger) ((WeakReference) entry.getValue()).get();
                if (plog != null) {
                    plog.redirectToJavaLoggerProxy();
                }
            }
        }
    }

    private void redirectToJavaLoggerProxy() {
        DefaultLoggerProxy lp = (DefaultLoggerProxy) DefaultLoggerProxy.class.cast(this.loggerProxy);
        JavaLoggerProxy jlp = new JavaLoggerProxy(lp.name, lp.level);
        this.javaLoggerProxy = jlp;
        this.loggerProxy = jlp;
    }

    private PlatformLogger(String name) {
        if (loggingEnabled) {
            LoggerProxy javaLoggerProxy = new JavaLoggerProxy(name);
            this.javaLoggerProxy = javaLoggerProxy;
            this.loggerProxy = javaLoggerProxy;
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

    @Deprecated
    public boolean isLoggable(int levelValue) {
        return isLoggable(Level.valueOf(levelValue));
    }

    @Deprecated
    public int getLevel() {
        Level level = this.loggerProxy.getLevel();
        return level != null ? level.intValue() : 0;
    }

    @Deprecated
    public void setLevel(int newLevel) {
        this.loggerProxy.setLevel(newLevel == 0 ? null : Level.valueOf(newLevel));
    }

    public boolean isLoggable(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        JavaLoggerProxy jlp = this.javaLoggerProxy;
        return jlp != null ? jlp.isLoggable(level) : this.loggerProxy.isLoggable(level);
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
