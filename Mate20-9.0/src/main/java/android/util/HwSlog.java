package android.util;

import android.common.HwFrameworkFactory;

public final class HwSlog {
    public static final int DEBUG_HIERACHY_CODE = 1000;
    public static boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    public static boolean HW_DEBUG = Log.HWLog;
    public static boolean HW_DEBUG_STATES = HW_DEBUG;
    private static final String TAG = "CoreServices";

    private HwSlog() {
    }

    public static int v(String tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slogv(tag, msg);
    }

    public static int d(String tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slogd(tag, msg);
    }

    public static boolean handleLogRequest(String[] args) {
        return HwFrameworkFactory.getHwFlogManager().handleLogRequest(args);
    }
}
