package huawei.android.utils;

import android.util.FrameworkLogSwitchConfig;
import android.util.FrameworkLogSwitchConfig.LOG_SWITCH;
import android.util.FrameworkTagConstant;
import android.util.HwSlog;
import android.util.Slog;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class HwCoreServicesLog {
    private static final /* synthetic */ int[] -huawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues = null;
    public static final boolean HWFLOW = false;
    private static final String TAG = "CoreServices";
    private static final String TAG_FLOW = "CoreServices_FLOW";
    private static final String TAG_INIT = "CoreServices_INIT";

    private enum DEBUG_FLOW {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.utils.HwCoreServicesLog.DEBUG_FLOW.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.utils.HwCoreServicesLog.DEBUG_FLOW.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.utils.HwCoreServicesLog.DEBUG_FLOW.<clinit>():void");
        }
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.utils.HwCoreServicesLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.utils.HwCoreServicesLog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.utils.HwCoreServicesLog.<clinit>():void");
    }

    public HwCoreServicesLog() {
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
            return HWFLOW;
        }
        DEBUG_FLOW debug_flow = scanArg(args[0], DEBUG_FLOW.values());
        if (debug_flow == null) {
            return HWFLOW;
        }
        boolean logRequestHandled = true;
        switch (-gethuawei-android-utils-HwCoreServicesLog$DEBUG_FLOWSwitchesValues()[debug_flow.ordinal()]) {
            case HwFragmentContainer.TRANSITION_FADE /*1*/:
                HwSlog.HW_DEBUG = HWFLOW;
                HwSlog.HWFLOW = HWFLOW;
                HwSlog.HW_DEBUG_STATES = HWFLOW;
                break;
            case HwFragmentContainer.TRANSITION_SLIDE_HORIZONTAL /*2*/:
                HwSlog.HW_DEBUG = true;
                HwSlog.HWFLOW = true;
                HwSlog.HW_DEBUG_STATES = true;
                break;
            case HwFragmentContainer.SPLITE_MODE_ALL_SEPARATE /*3*/:
                HwSlog.HW_DEBUG_STATES = true;
                break;
            default:
                logRequestHandled = HWFLOW;
                break;
        }
        return logRequestHandled;
    }
}
