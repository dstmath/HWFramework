package com.android.internal.logging;

import android.util.EventLog;

public class EventLogTags {
    public static final int SYSUI_ACTION = 524288;
    public static final int SYSUI_COUNT = 524290;
    public static final int SYSUI_HISTOGRAM = 524291;
    public static final int SYSUI_MULTI_ACTION = 524292;
    public static final int SYSUI_VIEW_VISIBILITY = 524287;

    private EventLogTags() {
    }

    public static void writeSysuiViewVisibility(int category, int visible) {
        EventLog.writeEvent((int) SYSUI_VIEW_VISIBILITY, Integer.valueOf(category), Integer.valueOf(visible));
    }

    public static void writeSysuiAction(int category, String pkg) {
        EventLog.writeEvent(524288, Integer.valueOf(category), pkg);
    }

    public static void writeSysuiMultiAction(Object[] content) {
        EventLog.writeEvent(524292, content);
    }

    public static void writeSysuiCount(String name, int increment) {
        EventLog.writeEvent((int) SYSUI_COUNT, name, Integer.valueOf(increment));
    }

    public static void writeSysuiHistogram(String name, int bucket) {
        EventLog.writeEvent(524291, name, Integer.valueOf(bucket));
    }
}
