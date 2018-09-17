package android.util;

import huawei.cust.HwCfgFilePolicy;

public class FrameworkLogSwitchConfig {
    private static final /* synthetic */ int[] -android-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues = null;
    public static boolean FWK_DEBUG;
    public static boolean FWK_FLOW;
    public static FrameworkLogSwitchInfo[] FwkLogSwitchArray;

    static class FrameworkLogSwitchInfo {
        private boolean debug_switch;
        private boolean flow_switch;

        public FrameworkLogSwitchInfo(boolean debug, boolean flow) {
            this.debug_switch = debug;
            this.flow_switch = flow;
        }

        public FrameworkLogSwitchInfo(String module_tag) {
            boolean z;
            boolean z2 = true;
            if (FrameworkLogSwitchConfig.FWK_DEBUG) {
                z = true;
            } else if (Log.HWModuleLog) {
                z = Log.isLoggable(module_tag, 3);
            } else {
                z = false;
            }
            this.debug_switch = z;
            if (!FrameworkLogSwitchConfig.FWK_FLOW) {
                if (Log.HWModuleLog) {
                    z2 = Log.isLoggable(module_tag, 4);
                } else {
                    z2 = false;
                }
            }
            this.flow_switch = z2;
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.FrameworkLogSwitchConfig.LOG_SWITCH.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.FrameworkLogSwitchConfig.LOG_SWITCH.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.util.FrameworkLogSwitchConfig.LOG_SWITCH.<clinit>():void");
        }
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.FrameworkLogSwitchConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.FrameworkLogSwitchConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.FrameworkLogSwitchConfig.<clinit>():void");
    }

    public FrameworkLogSwitchConfig() {
    }

    public static boolean getModuleLogSwitch(int module_tag, LOG_SWITCH log_switch) {
        int module_index = module_tag / 100;
        if (module_index >= 17) {
            return false;
        }
        switch (-getandroid-util-FrameworkLogSwitchConfig$LOG_SWITCHSwitchesValues()[log_switch.ordinal()]) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return FwkLogSwitchArray[module_index].debug_switch;
            case HwCfgFilePolicy.PC /*2*/:
                return FwkLogSwitchArray[module_index].flow_switch;
            default:
                return false;
        }
    }

    public static boolean setModuleLogSwitchON(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 17) {
            return false;
        }
        FwkLogSwitchArray[module_index].debug_switch = true;
        FwkLogSwitchArray[module_index].flow_switch = true;
        return true;
    }

    public static boolean setModuleLogSwitchOFF(int module_tag) {
        int module_index = module_tag / 100;
        if (module_index >= 17) {
            return false;
        }
        FwkLogSwitchArray[module_index].debug_switch = false;
        FwkLogSwitchArray[module_index].flow_switch = false;
        return true;
    }
}
