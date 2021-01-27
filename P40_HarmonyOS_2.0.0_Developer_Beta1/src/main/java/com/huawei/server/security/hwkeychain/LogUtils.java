package com.huawei.server.security.hwkeychain;

import android.util.Log;
import java.lang.reflect.Field;

public class LogUtils {
    private static final ThreadLocal<StringBuilder> BUILDER_BUFFER = new ThreadLocal<StringBuilder>() {
        /* class com.huawei.server.security.hwkeychain.LogUtils.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public StringBuilder initialValue() {
            return new StringBuilder();
        }
    };
    private static final String DIVIDER_CHARACTER = "-";
    private static final String LOG_SEPARATOR = ":";
    private static final String NULL_STRING = "null";
    private static final String REPLACEMENT = ("[\t" + System.lineSeparator() + "]");
    private static final String TAG = "HwKeychainServiceModule";
    private static boolean sIsHwDebug;
    private static boolean sIsHwInfo;

    static {
        boolean z;
        boolean z2 = false;
        sIsHwDebug = false;
        sIsHwInfo = true;
        try {
            Field fieldHwLog = Log.class.getField("HWLog");
            Field fieldHwModuleLog = Log.class.getField("HWModuleLog");
            Field fieldHwInfoLog = Log.class.getField("HWINFO");
            sIsHwDebug = fieldHwLog.getBoolean(null);
            sIsHwInfo = fieldHwInfoLog.getBoolean(null);
            boolean isHwModuleDebug = fieldHwModuleLog.getBoolean(null);
            if (!sIsHwDebug) {
                if (!isHwModuleDebug || !Log.isLoggable(TAG, 3)) {
                    z = false;
                    sIsHwDebug = z;
                    if (sIsHwInfo || (isHwModuleDebug && Log.isLoggable(TAG, 4))) {
                        z2 = true;
                    }
                    sIsHwInfo = z2;
                    Log.i(TAG, "isDebug=" + sIsHwDebug + " isModuleDebug=" + isHwModuleDebug + " isInfo=" + sIsHwInfo);
                }
            }
            z = true;
            sIsHwDebug = z;
            z2 = true;
            sIsHwInfo = z2;
            Log.i(TAG, "isDebug=" + sIsHwDebug + " isModuleDebug=" + isHwModuleDebug + " isInfo=" + sIsHwInfo);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException");
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "IllegalAccessException");
        } catch (NoSuchFieldException e3) {
            Log.e(TAG, "NoSuchFieldException");
        }
    }

    private LogUtils() {
    }

    public static void info(String tag, String logContent) {
        if (sIsHwInfo) {
            Log.i(TAG, tag + LOG_SEPARATOR + buildLogContents(logContent));
        }
    }

    public static void error(String tag, String logContent) {
        Log.e(TAG, tag + LOG_SEPARATOR + logContent);
    }

    public static void debug(String tag, String logContent) {
        if (sIsHwDebug) {
            Log.d(TAG, tag + LOG_SEPARATOR + buildLogContents(logContent));
        }
    }

    public static void warn(String tag, String content) {
        Log.w(TAG, tag + LOG_SEPARATOR + content);
    }

    private static String buildLogContents(String content) {
        if (content == null || content.length() == 0) {
            return NULL_STRING;
        }
        StringBuilder builder = getClearStringBuilder();
        builder.append(content);
        return builder.toString().replaceAll(REPLACEMENT, DIVIDER_CHARACTER);
    }

    private static StringBuilder getClearStringBuilder() {
        StringBuilder builder = BUILDER_BUFFER.get();
        if (builder == null) {
            return new StringBuilder();
        }
        builder.setLength(0);
        return builder;
    }
}
