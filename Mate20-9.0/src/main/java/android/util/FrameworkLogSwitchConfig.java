package android.util;

public class FrameworkLogSwitchConfig {
    /* access modifiers changed from: private */
    public static boolean FWK_DEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(FrameworkTagConstant.FWK_MODULE_TAG[0], 3)));
    /* access modifiers changed from: private */
    public static boolean FWK_FLOW;
    private static FrameworkLogSwitchInfo[] FwkLogSwitchArray = new FrameworkLogSwitchInfo[18];

    static class FrameworkLogSwitchInfo {
        /* access modifiers changed from: private */
        public boolean debug_switch;
        /* access modifiers changed from: private */
        public boolean flow_switch;

        public FrameworkLogSwitchInfo(boolean debug, boolean flow) {
            this.debug_switch = debug;
            this.flow_switch = flow;
        }

        public FrameworkLogSwitchInfo(String module_tag) {
            boolean z = true;
            this.debug_switch = FrameworkLogSwitchConfig.FWK_DEBUG || (Log.HWModuleLog && Log.isLoggable(module_tag, 3));
            if (!FrameworkLogSwitchConfig.FWK_FLOW && (!Log.HWModuleLog || !Log.isLoggable(module_tag, 4))) {
                z = false;
            }
            this.flow_switch = z;
        }
    }

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

    public static boolean getModuleLogSwitch(int module_tag, LOG_SWITCH log_switch) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        switch (log_switch) {
            case DEBUG:
                return FwkLogSwitchArray[module_index].debug_switch;
            case FLOW:
                return FwkLogSwitchArray[module_index].flow_switch;
            default:
                return false;
        }
    }

    public static boolean setModuleLogSwitchON(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        boolean unused = FwkLogSwitchArray[module_index].debug_switch = true;
        boolean unused2 = FwkLogSwitchArray[module_index].flow_switch = true;
        return true;
    }

    public static boolean setModuleLogSwitchOFF(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        boolean unused = FwkLogSwitchArray[module_index].debug_switch = false;
        boolean unused2 = FwkLogSwitchArray[module_index].flow_switch = false;
        return true;
    }
}
