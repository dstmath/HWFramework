package com.android.internal.logging;

import android.util.EventLog;

public class EventLogTags {
    public static final int COMMIT_SYS_CONFIG_FILE = 525000;
    public static final int SYSUI_ACTION = 524288;
    public static final int SYSUI_COUNT = 524290;
    public static final int SYSUI_HISTOGRAM = 524291;
    public static final int SYSUI_LATENCY = 36070;
    public static final int SYSUI_MULTI_ACTION = 524292;
    public static final int SYSUI_VIEW_VISIBILITY = 524287;

    private EventLogTags() {
    }

    public static void writeSysuiViewVisibility(int category, int visible) {
        EventLog.writeEvent(SYSUI_VIEW_VISIBILITY, new Object[]{Integer.valueOf(category), Integer.valueOf(visible)});
    }

    public static void writeSysuiAction(int category, String pkg) {
        EventLog.writeEvent(524288, new Object[]{Integer.valueOf(category), pkg});
    }

    public static void writeSysuiMultiAction(Object[] content) {
        EventLog.writeEvent(524292, content);
    }

    public static void writeSysuiCount(String name, int increment) {
        EventLog.writeEvent(SYSUI_COUNT, new Object[]{name, Integer.valueOf(increment)});
    }

    public static void writeSysuiHistogram(String name, int bucket) {
        EventLog.writeEvent(524291, new Object[]{name, Integer.valueOf(bucket)});
    }

    public static void writeSysuiLatency(int action, int latency) {
        EventLog.writeEvent(36070, new Object[]{Integer.valueOf(action), Integer.valueOf(latency)});
    }

    public static void writeCommitSysConfigFile(String name, long time) {
        EventLog.writeEvent(COMMIT_SYS_CONFIG_FILE, new Object[]{name, Long.valueOf(time)});
    }
}
