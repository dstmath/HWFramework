package com.huawei.android.app;

import android.util.HiLog;

public final class HmfLog {
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int HW_LOG_ID_HBS = 0;
    public static final int HW_LOG_ID_IGRAPHICS = 4;
    public static final int HW_LOG_ID_MAX = 4;
    public static final int HW_LOG_ID_MSDP = 2;
    public static final int HW_LOG_ID_ODMF = 1;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    private HmfLog() {
    }

    public static int v(int bufferId, String tag, String msg) {
        if (bufferId < 0 || bufferId > 4) {
            return -1;
        }
        return print_hwlogging_native(0, 2, tag, msg);
    }

    public static int d(int bufferId, String tag, String msg) {
        if (bufferId < 0 || bufferId > 4) {
            return -1;
        }
        return print_hwlogging_native(0, 3, tag, msg);
    }

    public static int i(int bufferId, String tag, String msg) {
        if (bufferId < 0 || bufferId > 4) {
            return -1;
        }
        return print_hwlogging_native(0, 4, tag, msg);
    }

    public static int w(int bufferId, String tag, String msg) {
        if (bufferId < 0 || bufferId > 4) {
            return -1;
        }
        return print_hwlogging_native(0, 5, tag, msg);
    }

    public static int e(int bufferId, String tag, String msg) {
        if (bufferId < 0 || bufferId > 4) {
            return -1;
        }
        return print_hwlogging_native(0, 6, tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        return HiLog.isLoggable(0, tag, level);
    }

    public static int print_hwlogging_native(int bufId, int priority, String tag, String msg) {
        return HiLog.print_hwlogging_native(bufId, priority, tag, msg);
    }
}
