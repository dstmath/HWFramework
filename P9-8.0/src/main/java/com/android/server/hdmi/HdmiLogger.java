package com.android.server.hdmi;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import java.util.HashMap;

final class HdmiLogger {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final long ERROR_LOG_DURATTION_MILLIS = 20000;
    private static final boolean IS_USER_BUILD = "user".equals(Build.TYPE);
    private static final String TAG = "HDMI";
    private static final ThreadLocal<HdmiLogger> sLogger = new ThreadLocal();
    private final HashMap<String, Pair<Long, Integer>> mErrorTimingCache = new HashMap();
    private final HashMap<String, Pair<Long, Integer>> mWarningTimingCache = new HashMap();

    private HdmiLogger() {
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
        return timing == null || curTime - ((Long) timing.first).longValue() > ERROR_LOG_DURATTION_MILLIS;
    }
}
