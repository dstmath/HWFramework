package org.apache.commons.logging.impl;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

@Deprecated
public class SimpleLog implements Log, Serializable {
    protected static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS zzz";
    public static final int LOG_LEVEL_ALL = 0;
    public static final int LOG_LEVEL_DEBUG = 2;
    public static final int LOG_LEVEL_ERROR = 5;
    public static final int LOG_LEVEL_FATAL = 6;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_OFF = 7;
    public static final int LOG_LEVEL_TRACE = 1;
    public static final int LOG_LEVEL_WARN = 4;
    protected static DateFormat dateFormatter = null;
    protected static String dateTimeFormat = null;
    protected static boolean showDateTime = false;
    protected static boolean showLogName = false;
    protected static boolean showShortName = false;
    protected static final Properties simpleLogProps = null;
    protected static final String systemPrefix = "org.apache.commons.logging.simplelog.";
    protected int currentLogLevel;
    protected String logName;
    private String shortLogName;

    /* renamed from: org.apache.commons.logging.impl.SimpleLog.1 */
    static class AnonymousClass1 implements PrivilegedAction {
        final /* synthetic */ String val$name;

        AnonymousClass1(String val$name) {
            this.val$name = val$name;
        }

        public Object run() {
            ClassLoader threadCL = SimpleLog.getContextClassLoader();
            if (threadCL != null) {
                return threadCL.getResourceAsStream(this.val$name);
            }
            return ClassLoader.getSystemResourceAsStream(this.val$name);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.commons.logging.impl.SimpleLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.commons.logging.impl.SimpleLog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.logging.impl.SimpleLog.<clinit>():void");
    }

    private static String getStringProperty(String name) {
        String str = null;
        try {
            str = System.getProperty(name);
        } catch (SecurityException e) {
        }
        return str == null ? simpleLogProps.getProperty(name) : str;
    }

    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : prop;
    }

    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return prop == null ? dephault : "true".equalsIgnoreCase(prop);
    }

    public SimpleLog(String name) {
        this.logName = null;
        this.shortLogName = null;
        this.logName = name;
        setLevel(LOG_LEVEL_INFO);
        String lvl = getStringProperty("org.apache.commons.logging.simplelog.log." + this.logName);
        int i = String.valueOf(name).lastIndexOf(".");
        while (lvl == null && i > -1) {
            name = name.substring(LOG_LEVEL_ALL, i);
            lvl = getStringProperty("org.apache.commons.logging.simplelog.log." + name);
            i = String.valueOf(name).lastIndexOf(".");
        }
        if (lvl == null) {
            lvl = getStringProperty("org.apache.commons.logging.simplelog.defaultlog");
        }
        if ("all".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_ALL);
        } else if ("trace".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_TRACE);
        } else if ("debug".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_DEBUG);
        } else if ("info".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_INFO);
        } else if ("warn".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_WARN);
        } else if ("error".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_ERROR);
        } else if ("fatal".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_FATAL);
        } else if ("off".equalsIgnoreCase(lvl)) {
            setLevel(LOG_LEVEL_OFF);
        }
    }

    public void setLevel(int currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }

    public int getLevel() {
        return this.currentLogLevel;
    }

    protected void log(int type, Object message, Throwable t) {
        StringBuffer buf = new StringBuffer();
        if (showDateTime) {
            buf.append(dateFormatter.format(new Date()));
            buf.append(" ");
        }
        switch (type) {
            case LOG_LEVEL_TRACE /*1*/:
                buf.append("[TRACE] ");
                break;
            case LOG_LEVEL_DEBUG /*2*/:
                buf.append("[DEBUG] ");
                break;
            case LOG_LEVEL_INFO /*3*/:
                buf.append("[INFO] ");
                break;
            case LOG_LEVEL_WARN /*4*/:
                buf.append("[WARN] ");
                break;
            case LOG_LEVEL_ERROR /*5*/:
                buf.append("[ERROR] ");
                break;
            case LOG_LEVEL_FATAL /*6*/:
                buf.append("[FATAL] ");
                break;
        }
        if (showShortName) {
            if (this.shortLogName == null) {
                this.shortLogName = this.logName.substring(this.logName.lastIndexOf(".") + LOG_LEVEL_TRACE);
                this.shortLogName = this.shortLogName.substring(this.shortLogName.lastIndexOf("/") + LOG_LEVEL_TRACE);
            }
            buf.append(String.valueOf(this.shortLogName)).append(" - ");
        } else if (showLogName) {
            buf.append(String.valueOf(this.logName)).append(" - ");
        }
        buf.append(String.valueOf(message));
        if (t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");
            StringWriter sw = new StringWriter(1024);
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }
        write(buf);
    }

    protected void write(StringBuffer buffer) {
        System.err.println(buffer.toString());
    }

    protected boolean isLevelEnabled(int logLevel) {
        return logLevel >= this.currentLogLevel;
    }

    public final void debug(Object message) {
        if (isLevelEnabled(LOG_LEVEL_DEBUG)) {
            log(LOG_LEVEL_DEBUG, message, null);
        }
    }

    public final void debug(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_DEBUG)) {
            log(LOG_LEVEL_DEBUG, message, t);
        }
    }

    public final void trace(Object message) {
        if (isLevelEnabled(LOG_LEVEL_TRACE)) {
            log(LOG_LEVEL_TRACE, message, null);
        }
    }

    public final void trace(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_TRACE)) {
            log(LOG_LEVEL_TRACE, message, t);
        }
    }

    public final void info(Object message) {
        if (isLevelEnabled(LOG_LEVEL_INFO)) {
            log(LOG_LEVEL_INFO, message, null);
        }
    }

    public final void info(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_INFO)) {
            log(LOG_LEVEL_INFO, message, t);
        }
    }

    public final void warn(Object message) {
        if (isLevelEnabled(LOG_LEVEL_WARN)) {
            log(LOG_LEVEL_WARN, message, null);
        }
    }

    public final void warn(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_WARN)) {
            log(LOG_LEVEL_WARN, message, t);
        }
    }

    public final void error(Object message) {
        if (isLevelEnabled(LOG_LEVEL_ERROR)) {
            log(LOG_LEVEL_ERROR, message, null);
        }
    }

    public final void error(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_ERROR)) {
            log(LOG_LEVEL_ERROR, message, t);
        }
    }

    public final void fatal(Object message) {
        if (isLevelEnabled(LOG_LEVEL_FATAL)) {
            log(LOG_LEVEL_FATAL, message, null);
        }
    }

    public final void fatal(Object message, Throwable t) {
        if (isLevelEnabled(LOG_LEVEL_FATAL)) {
            log(LOG_LEVEL_FATAL, message, t);
        }
    }

    public final boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    public final boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    public final boolean isFatalEnabled() {
        return isLevelEnabled(LOG_LEVEL_FATAL);
    }

    public final boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    public final boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    public final boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = (ClassLoader) Thread.class.getMethod("getContextClassLoader", (Class[]) null).invoke(Thread.currentThread(), (Object[]) null);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e2) {
            if (!(e2.getTargetException() instanceof SecurityException)) {
                throw new LogConfigurationException("Unexpected InvocationTargetException", e2.getTargetException());
            }
        } catch (NoSuchMethodException e3) {
        }
        if (classLoader == null) {
            return SimpleLog.class.getClassLoader();
        }
        return classLoader;
    }

    private static InputStream getResourceAsStream(String name) {
        return (InputStream) AccessController.doPrivileged(new AnonymousClass1(name));
    }
}
