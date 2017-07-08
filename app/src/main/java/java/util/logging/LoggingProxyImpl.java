package java.util.logging;

import java.util.List;
import sun.util.logging.LoggingProxy;

class LoggingProxyImpl implements LoggingProxy {
    static final LoggingProxy INSTANCE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.logging.LoggingProxyImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.logging.LoggingProxyImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LoggingProxyImpl.<clinit>():void");
    }

    private LoggingProxyImpl() {
    }

    public Object getLogger(String name) {
        return Logger.getPlatformLogger(name);
    }

    public Object getLevel(Object logger) {
        return ((Logger) logger).getLevel();
    }

    public void setLevel(Object logger, Object newLevel) {
        ((Logger) logger).setLevel((Level) newLevel);
    }

    public boolean isLoggable(Object logger, Object level) {
        return ((Logger) logger).isLoggable((Level) level);
    }

    public void log(Object logger, Object level, String msg) {
        ((Logger) logger).log((Level) level, msg);
    }

    public void log(Object logger, Object level, String msg, Throwable t) {
        ((Logger) logger).log((Level) level, msg, t);
    }

    public void log(Object logger, Object level, String msg, Object... params) {
        ((Logger) logger).log((Level) level, msg, params);
    }

    public List<String> getLoggerNames() {
        return LogManager.getLoggingMXBean().getLoggerNames();
    }

    public String getLoggerLevel(String loggerName) {
        return LogManager.getLoggingMXBean().getLoggerLevel(loggerName);
    }

    public void setLoggerLevel(String loggerName, String levelName) {
        LogManager.getLoggingMXBean().setLoggerLevel(loggerName, levelName);
    }

    public String getParentLoggerName(String loggerName) {
        return LogManager.getLoggingMXBean().getParentLoggerName(loggerName);
    }

    public Object parseLevel(String levelName) {
        Level level = Level.findLevel(levelName);
        if (level != null) {
            return level;
        }
        throw new IllegalArgumentException("Unknown level \"" + levelName + "\"");
    }

    public String getLevelName(Object level) {
        return ((Level) level).getLevelName();
    }

    public int getLevelValue(Object level) {
        return ((Level) level).intValue();
    }

    public String getProperty(String key) {
        return LogManager.getLogManager().getProperty(key);
    }
}
