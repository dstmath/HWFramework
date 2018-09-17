package com.android.server.hdmi;

import android.os.SystemClock;
import android.util.Pair;
import android.util.Slog;
import java.util.HashMap;

final class HdmiLogger {
    private static final boolean DEBUG = false;
    private static final long ERROR_LOG_DURATTION_MILLIS = 20000;
    private static final boolean IS_USER_BUILD = false;
    private static final String TAG = "HDMI";
    private static final ThreadLocal<HdmiLogger> sLogger = null;
    private final HashMap<String, Pair<Long, Integer>> mErrorTimingCache;
    private final HashMap<String, Pair<Long, Integer>> mWarningTimingCache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.hdmi.HdmiLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.hdmi.HdmiLogger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiLogger.<clinit>():void");
    }

    private HdmiLogger() {
        this.mWarningTimingCache = new HashMap();
        this.mErrorTimingCache = new HashMap();
    }

    static final void warning(String logMessage, Object... objs) {
        getLogger().warningInternal(toLogString(logMessage, objs));
    }

    private void warningInternal(String logMessage) {
        String log = updateLog(this.mWarningTimingCache, logMessage);
        if (!log.isEmpty()) {
            Slog.w(TAG, log);
        }
    }

    static final void error(String logMessage, Object... objs) {
        getLogger().errorInternal(toLogString(logMessage, objs));
    }

    private void errorInternal(String logMessage) {
        String log = updateLog(this.mErrorTimingCache, logMessage);
        if (!log.isEmpty()) {
            Slog.e(TAG, log);
        }
    }

    static final void debug(String logMessage, Object... objs) {
        getLogger().debugInternal(toLogString(logMessage, objs));
    }

    private void debugInternal(String logMessage) {
        if (DEBUG) {
            Slog.d(TAG, logMessage);
        }
    }

    private static final String toLogString(String logMessage, Object[] objs) {
        if (objs.length > 0) {
            return String.format(logMessage, objs);
        }
        return logMessage;
    }

    private static HdmiLogger getLogger() {
        HdmiLogger logger = (HdmiLogger) sLogger.get();
        if (logger != null) {
            return logger;
        }
        logger = new HdmiLogger();
        sLogger.set(logger);
        return logger;
    }

    private static String updateLog(HashMap<String, Pair<Long, Integer>> cache, String logMessage) {
        long curTime = SystemClock.uptimeMillis();
        Pair<Long, Integer> timing = (Pair) cache.get(logMessage);
        if (shouldLogNow(timing, curTime)) {
            String log = buildMessage(logMessage, timing);
            cache.put(logMessage, new Pair(Long.valueOf(curTime), Integer.valueOf(1)));
            return log;
        }
        increaseLogCount(cache, logMessage);
        return "";
    }

    private static String buildMessage(String message, Pair<Long, Integer> timing) {
        return "[" + (timing == null ? 1 : ((Integer) timing.second).intValue()) + "]:" + message;
    }

    private static void increaseLogCount(HashMap<String, Pair<Long, Integer>> cache, String message) {
        Pair<Long, Integer> timing = (Pair) cache.get(message);
        if (timing != null) {
            cache.put(message, new Pair((Long) timing.first, Integer.valueOf(((Integer) timing.second).intValue() + 1)));
        }
    }

    private static boolean shouldLogNow(Pair<Long, Integer> timing, long curTime) {
        return (timing == null || curTime - ((Long) timing.first).longValue() > ERROR_LOG_DURATTION_MILLIS) ? true : IS_USER_BUILD;
    }
}
