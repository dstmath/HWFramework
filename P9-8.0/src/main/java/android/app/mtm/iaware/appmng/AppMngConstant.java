package android.app.mtm.iaware.appmng;

import java.util.Locale;

public class AppMngConstant {
    public static final int ACTION_SYSTEM_MANAGER_CLEAN = 0;
    public static final int ACTION_SYSTEM_MANAGER_GET_LIST = 1;
    public static final int POLICY_IGNORE = 2;
    public static final int POLICY_SMART = 0;
    public static final int POLICY_STRICT = 1;
    public static final int RSN_ALW_COMMON = 4;
    public static final int RSN_ALW_ONEAPP = 1;
    public static final int RSN_ALW_SYSCALL = 2;
    public static final int RSN_ALW_USER = 3;
    public static final int RSN_FBD = 0;
    public static final int RSN_MAX = 5;
    public static final String TAG_SPECIAL_REASON = "spec";
    public static final int TRI_STAT_NOTSMT_ALV = 2;
    public static final int TRI_STAT_NOTSMT_NOTALV = 0;
    public static final int TRI_STAT_SMT_ALV_CUST = 4;
    public static final int TRI_STAT_SMT_ALV_NCUST = 3;
    public static final int TRI_STAT_SMT_NOTALV = 1;
    public static final int TRI_STAT_UNINIT = -1;

    public interface EnumWithDesc {
        boolean equals(Object obj);

        String getDesc();

        int hashCode();

        int ordinal();
    }

    public enum AppCleanSource implements EnumWithDesc {
        SYSTEM_MANAGER("system_manager"),
        MEMORY("memory"),
        POWER_GENIE("power_genie"),
        CRASH("crash"),
        COMPACT("compact"),
        SMART_CLEAN("smart_clean"),
        MEMORY_REPAIR("memory_repair");
        
        private String mDesc;

        private AppCleanSource(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum AppFreezeSource implements EnumWithDesc {
        FAST_FREEZE("fast_freeze"),
        CAMERA_FREEZE("camera_freeze");
        
        private String mDesc;

        private AppFreezeSource(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum AppIoLimitSource implements EnumWithDesc {
        IOLIMIT("iolimit");
        
        private String mDesc;

        private AppIoLimitSource(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum AppMngFeature implements EnumWithDesc {
        APP_START("app_start"),
        APP_CLEAN("app_clean"),
        APP_IOLIMIT("app_iolimit"),
        APP_FREEZE("app_freeze");
        
        private String mDesc;

        private AppMngFeature(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum AppStartReason {
        DEFAULT("F"),
        SYSTEM_APP("E"),
        LIST("L");
        
        private String mDesc;

        private AppStartReason(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }
    }

    public enum AppStartSource implements EnumWithDesc {
        START_SERVICE("S"),
        BIND_SERVICE("D"),
        PROVIDER("P"),
        THIRD_BROADCAST("B"),
        SYSTEM_BROADCAST("b"),
        THIRD_ACTIVITY("A"),
        JOB_SCHEDULE("J"),
        ACCOUNT_SYNC("Y"),
        SCHEDULE_RESTART("R"),
        ALARM("M");
        
        private String mDesc;

        private AppStartSource(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum CleanReason {
        LIST("decide by list", "list"),
        TRISTATE("decide by tristate", "tri"),
        OUT_OF_SCOPE("out of scope", "out"),
        MISSING_PROCESS_INFO("missing process info", "miss"),
        CONFIG_INVALID("without valid config", "conf"),
        INVISIBLE("never switched to foreground", "invi"),
        CTS("protected by cts pattern", "cts"),
        MEMORY_ENOUGH("memory enough", "mem");
        
        private String mAbbr;
        private String mCode;

        private CleanReason(String code, String abbr) {
            this.mCode = code;
            this.mAbbr = abbr;
        }

        public String getCode() {
            return this.mCode;
        }

        public String getAbbr() {
            return this.mAbbr;
        }
    }
}
