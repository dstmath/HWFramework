package huawei.android.utils;

import android.util.FrameworkLogSwitchConfig;
import android.util.FrameworkTagConstant;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;

public class HwCoreServicesLog {
    public static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "CoreServices";
    private static final String TAG_FLOW = "CoreServices_FLOW";
    private static final String TAG_INIT = "CoreServices_INIT";

    private enum DEBUG_FLOW {
        HW_DEBUG_STATES,
        ENABLE_LOG,
        DISABLE_LOG
    }

    public static int v(String tag, String msg) {
        if (HWFLOW) {
            return Slog.v(tag, msg);
        }
        return 0;
    }

    public static int d(String tag, String msg) {
        if (HWFLOW) {
            return Slog.d(tag, msg);
        }
        return 0;
    }

    public static int v(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.DEBUG)) {
            return Slog.v(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int v(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.DEBUG)) {
            return Slog.v(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int d(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.DEBUG)) {
            return Slog.d(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int d(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.DEBUG)) {
            return Slog.d(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int i(int tag, String msg) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.FLOW)) {
            return Slog.i(FrameworkTagConstant.getModuleTagStr(tag), msg);
        }
        return 0;
    }

    public static int i(int tag, String msg, Throwable tr) {
        if (FrameworkLogSwitchConfig.getModuleLogSwitch(tag, FrameworkLogSwitchConfig.LOG_SWITCH.FLOW)) {
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
        switch (debug_flow) {
            case HW_DEBUG_STATES:
                HwSlog.HW_DEBUG_STATES = true;
                break;
            case ENABLE_LOG:
                HwSlog.HW_DEBUG = true;
                HwSlog.HWFLOW = true;
                HwSlog.HW_DEBUG_STATES = true;
                break;
            case DISABLE_LOG:
                HwSlog.HW_DEBUG = false;
                HwSlog.HWFLOW = false;
                HwSlog.HW_DEBUG_STATES = false;
                break;
            default:
                logRequestHandled = false;
                break;
        }
        return logRequestHandled;
    }
}
