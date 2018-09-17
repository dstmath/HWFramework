package android.util;

public class FrameworkLogSwitchConfig {
    private static final /* synthetic */ int[] -android-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues = null;
    public static boolean FWK_DEBUG;
    public static boolean FWK_FLOW;
    public static FrameworkLogSwitchInfo[] FwkLogSwitchArray = new FrameworkLogSwitchInfo[18];

    static class FrameworkLogSwitchInfo {
        private boolean debug_switch;
        private boolean flow_switch;

        public FrameworkLogSwitchInfo(boolean debug, boolean flow) {
            this.debug_switch = debug;
            this.flow_switch = flow;
        }

        public FrameworkLogSwitchInfo(String module_tag) {
            boolean z = true;
            boolean isLoggable = !FrameworkLogSwitchConfig.FWK_DEBUG ? Log.HWModuleLog ? Log.isLoggable(module_tag, 3) : false : true;
            this.debug_switch = isLoggable;
            if (!FrameworkLogSwitchConfig.FWK_FLOW) {
                if (Log.HWModuleLog) {
                    z = Log.isLoggable(module_tag, 4);
                } else {
                    z = false;
                }
            }
            this.flow_switch = z;
        }

        public boolean isDebug_switch() {
            return this.debug_switch;
        }

        public void setDebug_switch(boolean debug_switch) {
            this.debug_switch = debug_switch;
        }

        public boolean isFlow_switch() {
            return this.flow_switch;
        }

        public void setFlow_switch(boolean info_switch) {
            this.flow_switch = info_switch;
        }
    }

    public enum LOG_SWITCH {
        DEBUG,
        FLOW
    }

    private static /* synthetic */ int[] -getandroid-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues() {
        if (-android-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues != null) {
            return -android-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues;
        }
        int[] iArr = new int[LOG_SWITCH.values().length];
        try {
            iArr[LOG_SWITCH.DEBUG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[LOG_SWITCH.FLOW.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean z;
        boolean z2 = true;
        int i = 0;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(FrameworkTagConstant.FWK_MODULE_TAG[0], 3);
        } else {
            z = false;
        }
        FWK_DEBUG = z;
        if (!Log.HWINFO) {
            if (Log.HWModuleLog) {
                z2 = Log.isLoggable(FrameworkTagConstant.FWK_MODULE_TAG[0], 4);
            } else {
                z2 = false;
            }
        }
        FWK_FLOW = z2;
        String[] strArr = FrameworkTagConstant.FWK_MODULE_TAG;
        int length = strArr.length;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            FwkLogSwitchArray[i2] = new FrameworkLogSwitchInfo(strArr[i]);
            i++;
            i2 = i3;
        }
    }

    public static boolean getModuleLogSwitch(int module_tag, LOG_SWITCH log_switch) {
        int module_index = module_tag / 100;
        if (module_index >= 18) {
            return false;
        }
        switch (-getandroid-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues()[log_switch.ordinal()]) {
            case 1:
                return FwkLogSwitchArray[module_index].debug_switch;
            case 2:
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
