package android.util;

public class FrameworkLogSwitchConfig {
    public static boolean FWK_DEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(FrameworkTagConstant.FWK_MODULE_TAG[0], 3)));
    public static boolean FWK_FLOW;
    public static FrameworkLogSwitchInfo[] FwkLogSwitchArray = new FrameworkLogSwitchInfo[18];

    public enum LOG_SWITCH {
        DEBUG,
        FLOW
    }

    static {
        boolean z = true;
        int i = 0;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(FrameworkTagConstant.FWK_MODULE_TAG[0], 4))) {
            z = false;
        }
        FWK_FLOW = z;
        int i2 = 0;
        String[] strArr = FrameworkTagConstant.FWK_MODULE_TAG;
        int length = strArr.length;
        while (i < length) {
            FwkLogSwitchArray[i2] = new FrameworkLogSwitchInfo(strArr[i]);
            i++;
            i2++;
        }
    }

    public static class FrameworkLogSwitchInfo {
        private boolean debug_switch;
        private boolean flow_switch;

        public FrameworkLogSwitchInfo(boolean debug, boolean flow) {
            this.debug_switch = debug;
            this.flow_switch = flow;
        }

        public FrameworkLogSwitchInfo(String module_tag) {
            boolean z = false;
            this.debug_switch = FrameworkLogSwitchConfig.FWK_DEBUG || (Log.HWModuleLog && Log.isLoggable(module_tag, 3));
            if (FrameworkLogSwitchConfig.FWK_FLOW || (Log.HWModuleLog && Log.isLoggable(module_tag, 4))) {
                z = true;
            }
            this.flow_switch = z;
        }
    }

    public static boolean getModuleLogSwitch(int module_tag, LOG_SWITCH log_switch) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$android$util$FrameworkLogSwitchConfig$LOG_SWITCH[log_switch.ordinal()];
        if (i == 1) {
            return FwkLogSwitchArray[module_index].debug_switch;
        }
        if (i != 2) {
            return false;
        }
        return FwkLogSwitchArray[module_index].flow_switch;
    }

    /* renamed from: android.util.FrameworkLogSwitchConfig$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$util$FrameworkLogSwitchConfig$LOG_SWITCH = new int[LOG_SWITCH.values().length];

        static {
            try {
                $SwitchMap$android$util$FrameworkLogSwitchConfig$LOG_SWITCH[LOG_SWITCH.DEBUG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$util$FrameworkLogSwitchConfig$LOG_SWITCH[LOG_SWITCH.FLOW.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static boolean setModuleLogSwitchON(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        FwkLogSwitchArray[module_index].debug_switch = true;
        FwkLogSwitchArray[module_index].flow_switch = true;
        return true;
    }

    public static boolean setModuleLogSwitchOFF(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        FwkLogSwitchArray[module_index].debug_switch = false;
        FwkLogSwitchArray[module_index].flow_switch = false;
        return true;
    }
}
