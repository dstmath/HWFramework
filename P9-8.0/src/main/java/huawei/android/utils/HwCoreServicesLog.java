package huawei.android.utils;

import android.util.FrameworkLogSwitchConfig;
import android.util.FrameworkLogSwitchConfig.LOG_SWITCH;
import android.util.FrameworkTagConstant;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;

public class HwCoreServicesLog {
    private static final /* synthetic */ int[] -huawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues = null;
    public static final boolean HWFLOW;
    private static final String TAG = "CoreServices";
    private static final String TAG_FLOW = "CoreServices_FLOW";
    private static final String TAG_INIT = "CoreServices_INIT";

    private enum DEBUG_FLOW {
        HW_DEBUG_STATES,
        ENABLE_LOG,
        DISABLE_LOG
    }

    private static /* synthetic */ int[] -gethuawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues() {
        if (-huawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues != null) {
            return -huawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues;
        }
        int[] iArr = new int[DEBUG_FLOW.values().length];
        try {
            iArr[DEBUG_FLOW.DISABLE_LOG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DEBUG_FLOW.ENABLE_LOG.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DEBUG_FLOW.HW_DEBUG_STATES.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -huawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static int v(String tag, String msg) {
        return HWFLOW ? Slog.v(tag, msg) : 0;
    }

    public static int d(String tag, String msg) {
        return HWFLOW ? Slog.d(tag, msg) : 0;
    }

    public static int v(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.DEBUG)) {
            return Slog.v(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int v(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.DEBUG)) {
            return Slog.v(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int d(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.DEBUG)) {
            return Slog.d(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int d(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.DEBUG)) {
            return Slog.d(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int i(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.FLOW)) {
            return Slog.i(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int i(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, LOG_SWITCH.FLOW)) {
            return Slog.i(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int e(int tag, String msg) {
        return Slog.e(FrameworkTagConstant.getModuleTagStr(tag), msg);
    }

    public static int e(int tag, String msg, Throwable tr) {
        return Slog.e(FrameworkTagConstant.getModuleTagStr(tag), msg, tr);
    }

    public static int w(int tag, String msg) {
        return Slog.w(FrameworkTagConstant.getModuleTagStr(tag), msg);
    }

    public static int w(int tag, String msg, Throwable tr) {
        return Slog.w(FrameworkTagConstant.getModuleTagStr(tag), msg, tr);
    }

    private static DEBUG_FLOW scanArg(String arg, DEBUG_FLOW[] values) {
        if (arg != null) {
            for (DEBUG_FLOW value : values) {
                if (value.name().equals(arg)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean handleLogRequest(String[] args) {
        if (args.length < 1) {
            return false;
        }
        DEBUG_FLOW debug_flow = scanArg(args[0], DEBUG_FLOW.values());
        if (debug_flow == null) {
            return false;
        }
        boolean logRequestHandled = true;
        switch (-gethuawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues()[debug_flow.ordinal()]) {
            case 1:
                HwSlog.HW_DEBUG = false;
                HwSlog.HWFLOW = false;
                HwSlog.HW_DEBUG_STATES = false;
                break;
            case 2:
                HwSlog.HW_DEBUG = true;
                HwSlog.HWFLOW = true;
                HwSlog.HW_DEBUG_STATES = true;
                break;
            case 3:
                HwSlog.HW_DEBUG_STATES = true;
                break;
            default:
                logRequestHandled = false;
                break;
        }
        return logRequestHandled;
    }
}
