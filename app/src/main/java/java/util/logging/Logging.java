package java.util.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

class Logging implements LoggingMXBean {
    private static String EMPTY_STRING;
    private static LogManager logManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.logging.Logging.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.logging.Logging.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.Logging.<clinit>():void");
    }

    Logging() {
    }

    public List<String> getLoggerNames() {
        Enumeration loggers = logManager.getLoggerNames();
        ArrayList<String> array = new ArrayList();
        while (loggers.hasMoreElements()) {
            array.add((String) loggers.nextElement());
        }
        return array;
    }

    public String getLoggerLevel(String loggerName) {
        Logger l = logManager.getLogger(loggerName);
        if (l == null) {
            return null;
        }
        Level level = l.getLevel();
        if (level == null) {
            return EMPTY_STRING;
        }
        return level.getLevelName();
    }

    public void setLoggerLevel(String loggerName, String levelName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }
        Logger logger = logManager.getLogger(loggerName);
        if (logger == null) {
            throw new IllegalArgumentException("Logger " + loggerName + "does not exist");
        }
        Level level = null;
        if (levelName != null) {
            level = Level.findLevel(levelName);
            if (level == null) {
                throw new IllegalArgumentException("Unknown level \"" + levelName + "\"");
            }
        }
        logger.setLevel(level);
    }

    public String getParentLoggerName(String loggerName) {
        Logger l = logManager.getLogger(loggerName);
        if (l == null) {
            return null;
        }
        Logger p = l.getParent();
        if (p == null) {
            return EMPTY_STRING;
        }
        return p.getName();
    }
}
