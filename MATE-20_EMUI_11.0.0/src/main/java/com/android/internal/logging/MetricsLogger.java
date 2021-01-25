package com.android.internal.logging;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.Build;
import android.util.StatsLog;

public class MetricsLogger {
    public static final int LOGTAG = 524292;
    public static final int VIEW_UNKNOWN = 0;
    private static MetricsLogger sMetricsLogger;

    private static MetricsLogger getLogger() {
        if (sMetricsLogger == null) {
            sMetricsLogger = new MetricsLogger();
        }
        return sMetricsLogger;
    }

    /* access modifiers changed from: protected */
    public void saveLog(LogMaker log) {
        EventLogTags.writeSysuiMultiAction(log.serialize());
        StatsLog.write(83, 0, log.getEntries());
    }

    @UnsupportedAppUsage
    public void write(LogMaker content) {
        if (content.getType() == 0) {
            content.setType(4);
        }
        saveLog(content);
    }

    public void count(String name, int value) {
        saveLog(new LogMaker(803).setCounterName(name).setCounterValue(value));
    }

    public void histogram(String name, int bucket) {
        saveLog(new LogMaker(804).setCounterName(name).setCounterBucket(bucket).setCounterValue(1));
    }

    public void visible(int category) throws IllegalArgumentException {
        if (!Build.IS_DEBUGGABLE || category != 0) {
            saveLog(new LogMaker(category).setType(1));
            return;
        }
        throw new IllegalArgumentException("Must define metric category");
    }

    public void hidden(int category) throws IllegalArgumentException {
        if (!Build.IS_DEBUGGABLE || category != 0) {
            saveLog(new LogMaker(category).setType(2));
            return;
        }
        throw new IllegalArgumentException("Must define metric category");
    }

    public void visibility(int category, boolean visible) throws IllegalArgumentException {
        if (visible) {
            visible(category);
        } else {
            hidden(category);
        }
    }

    public void visibility(int category, int vis) throws IllegalArgumentException {
        visibility(category, vis == 0);
    }

    public void action(int category) {
        saveLog(new LogMaker(category).setType(4));
    }

    public void action(int category, int value) {
        saveLog(new LogMaker(category).setType(4).setSubtype(value));
    }

    public void action(int category, boolean value) {
        saveLog(new LogMaker(category).setType(4).setSubtype(value ? 1 : 0));
    }

    public void action(int category, String pkg) {
        if (!Build.IS_DEBUGGABLE || category != 0) {
            saveLog(new LogMaker(category).setType(4).setPackageName(pkg));
            return;
        }
        throw new IllegalArgumentException("Must define metric category");
    }

    @Deprecated
    public static void visible(Context context, int category) throws IllegalArgumentException {
        getLogger().visible(category);
    }

    @Deprecated
    public static void hidden(Context context, int category) throws IllegalArgumentException {
        getLogger().hidden(category);
    }

    @Deprecated
    public static void visibility(Context context, int category, boolean visibile) throws IllegalArgumentException {
        getLogger().visibility(category, visibile);
    }

    @Deprecated
    public static void visibility(Context context, int category, int vis) throws IllegalArgumentException {
        visibility(context, category, vis == 0);
    }

    @Deprecated
    public static void action(Context context, int category) {
        getLogger().action(category);
    }

    @Deprecated
    public static void action(Context context, int category, int value) {
        getLogger().action(category, value);
    }

    @Deprecated
    public static void action(Context context, int category, boolean value) {
        getLogger().action(category, value);
    }

    @Deprecated
    public static void action(LogMaker content) {
        getLogger().write(content);
    }

    @Deprecated
    public static void action(Context context, int category, String pkg) {
        getLogger().action(category, pkg);
    }

    @Deprecated
    public static void count(Context context, String name, int value) {
        getLogger().count(name, value);
    }

    @Deprecated
    public static void histogram(Context context, String name, int bucket) {
        getLogger().histogram(name, bucket);
    }
}
