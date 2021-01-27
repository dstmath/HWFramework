package huawei.android.utils;

import android.util.FrameworkLogSwitchConfig;
import android.util.FrameworkTagConstant;
import android.util.HwSlog;
import android.util.Log;
import android.util.Slog;

public class HwCoreServicesLog {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "CoreServices";

    /* access modifiers changed from: private */
    public enum DEBUG_FLOW {
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
        if (arg == null) {
            return null;
        }
        for (DEBUG_FLOW value : values) {
            if (value.name().equals(arg)) {
                return value;
            }
        }
        return null;
    }

    public static boolean handleLogRequest(String[] args) {
        DEBUG_FLOW debugFlow;
        if (args.length < 1 || (debugFlow = scanArg(args[0], DEBUG_FLOW.values())) == null) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$huawei$android$utils$HwCoreServicesLog$DEBUG_FLOW[debugFlow.ordinal()];
        if (i == 1) {
            HwSlog.HW_DEBUG_STATES = true;
            return true;
        } else if (i == 2) {
            HwSlog.HW_DEBUG = true;
            HwSlog.HWFLOW = true;
            HwSlog.HW_DEBUG_STATES = true;
            return true;
        } else if (i != 3) {
            return false;
        } else {
            HwSlog.HW_DEBUG = false;
            HwSlog.HWFLOW = false;
            HwSlog.HW_DEBUG_STATES = false;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: huawei.android.utils.HwCoreServicesLog$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$huawei$android$utils$HwCoreServicesLog$DEBUG_FLOW = new int[DEBUG_FLOW.values().length];

        static {
            try {
                $SwitchMap$huawei$android$utils$HwCoreServicesLog$DEBUG_FLOW[DEBUG_FLOW.HW_DEBUG_STATES.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$huawei$android$utils$HwCoreServicesLog$DEBUG_FLOW[DEBUG_FLOW.ENABLE_LOG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$huawei$android$utils$HwCoreServicesLog$DEBUG_FLOW[DEBUG_FLOW.DISABLE_LOG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }
}
