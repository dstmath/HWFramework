package com.android.internal.logging;

import android.content.Context;
import android.metrics.LogMaker;
import android.os.Build;
import android.util.LogException;

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

    protected void saveLog(Object[] rep) {
        EventLogTags.writeSysuiMultiAction(rep);
    }

    public void write(LogMaker content) {
        if (content.getType() == 0) {
            content.setType(4);
        }
        saveLog(content.serialize());
    }

    public void visible(int category) throws IllegalArgumentException {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiViewVisibility(category, 100);
        saveLog(new LogMaker(category).setType(1).serialize());
    }

    public void hidden(int category) throws IllegalArgumentException {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiViewVisibility(category, 0);
        saveLog(new LogMaker(category).setType(2).serialize());
    }

    public void visibility(int category, boolean visibile) throws IllegalArgumentException {
        if (visibile) {
            visible(category);
        } else {
            hidden(category);
        }
    }

    public void visibility(int category, int vis) throws IllegalArgumentException {
        boolean z = false;
        if (vis == 0) {
            z = true;
        }
        visibility(category, z);
    }

    public void action(int category) {
        EventLogTags.writeSysuiAction(category, LogException.NO_VALUE);
        saveLog(new LogMaker(category).setType(4).serialize());
    }

    public void action(int category, int value) {
        EventLogTags.writeSysuiAction(category, Integer.toString(value));
        saveLog(new LogMaker(category).setType(4).setSubtype(value).serialize());
    }

    public void action(int category, boolean value) {
        EventLogTags.writeSysuiAction(category, Boolean.toString(value));
        saveLog(new LogMaker(category).setType(4).setSubtype(value ? 1 : 0).serialize());
    }

    public void action(int category, String pkg) {
        if (Build.IS_DEBUGGABLE && category == 0) {
            throw new IllegalArgumentException("Must define metric category");
        }
        EventLogTags.writeSysuiAction(category, pkg);
        saveLog(new LogMaker(category).setType(4).setPackageName(pkg).serialize());
    }

    public void count(String name, int value) {
        EventLogTags.writeSysuiCount(name, value);
        saveLog(new LogMaker(803).setCounterName(name).setCounterValue(value).serialize());
    }

    public void histogram(String name, int bucket) {
        EventLogTags.writeSysuiHistogram(name, bucket);
        saveLog(new LogMaker(804).setCounterName(name).setCounterBucket(bucket).setCounterValue(1).serialize());
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
        boolean z = false;
        if (vis == 0) {
            z = true;
        }
        visibility(context, category, z);
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
