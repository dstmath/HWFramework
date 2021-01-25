package android.app.mtm.iaware.appmng;

import com.huawei.annotation.HwSystemApi;
import java.util.Locale;

@HwSystemApi
public class AppMngConstant {
    public static final int ACTION_SYSTEM_MANAGER_CLEAN = 0;
    public static final int ACTION_SYSTEM_MANAGER_GET_LIST = 1;
    public static final String APP_START_ZFLAG_KEY = "Z";
    public static final String APP_START_ZFLAG_VALUE = "1";
    public static final int POLICY_IGNORE = 2;
    public static final int POLICY_LIST_KEEP = 0;
    public static final int POLICY_LIST_REMOVE = 1;
    public static final int POLICY_LIST_UNKOWN = -1;
    public static final int POLICY_SMART = 0;
    public static final int POLICY_STRICT = 1;
    public static final int RSN_ALW_COMMON = 4;
    public static final int RSN_ALW_ONEAPP = 1;
    public static final int RSN_ALW_SYSCALL = 2;
    public static final int RSN_ALW_USER = 3;
    public static final int RSN_FBD = 0;
    public static final int RSN_MAX = 5;
    public static final String TAG_SPECIAL_REASON = "spec";
    public static final int TRI_STAT_NOT_SMT_ALV = 2;
    public static final int TRI_STAT_NOT_SMT_NOTALV = 0;
    public static final int TRI_STAT_SMT_ALV_CUST = 4;
    public static final int TRI_STAT_SMT_ALV_NCUST = 3;
    public static final int TRI_STAT_SMT_NOTALV = 1;
    public static final int TRI_STAT_UNINIT = -1;

    public interface EnumWithDesc {
        String getDesc();

        int ordinal();
    }

    public enum AppMngFeature implements EnumWithDesc {
        APP_START("app_start"),
        APP_CLEAN("app_clean"),
        APP_IOLIMIT("app_iolimit"),
        APP_FREEZE("app_freeze"),
        APP_ALARM("app_alarm"),
        BROADCAST("broadcast"),
        APP_CPULIMIT("app_cpulimit"),
        COMMON("common");
        
        private String mDesc;

        private AppMngFeature(String desc) {
            this.mDesc = desc;
        }

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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
        MEMORY_ENOUGH("memory enough", "mem"),
        PROCESS_LIST("decide by processlist", "processlist"),
        POLICY_DEGRADE("policy degrade by processlist", "degrade"),
        CLOUD_PUSH_LIST("decide by cloud push list", "cloudpushlist"),
        CACHED_KEY_APP("decide by cached key app", "cachedkeyapp");
        
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

    public enum AppCleanSource implements EnumWithDesc {
        SYSTEM_MANAGER("system_manager"),
        MEMORY("memory"),
        POWER_GENIE("power_genie"),
        CRASH("crash"),
        COMPACT("compact"),
        SMART_CLEAN("smart_clean"),
        MEMORY_REPAIR("memory_repair"),
        THERMAL("thermal"),
        MEMORY_REPAIR_VSS("memory_repair_vss"),
        SYSTEM_MEMORY_REPAIR("system_memory_repair"),
        CACHED_MEMORY("cached_memory");
        
        private String mDesc;

        private AppCleanSource(String desc) {
            this.mDesc = desc;
        }

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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
        IOLIMIT("iolimit"),
        CAMERA_IOLIMIT("camera_iolimit");
        
        private String mDesc;

        private AppIoLimitSource(String desc) {
            this.mDesc = desc;
        }

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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

    public enum AppCpuLimitSource implements EnumWithDesc {
        CPULIMIT("cpulimit");
        
        private String mDesc;

        private AppCpuLimitSource(String desc) {
            this.mDesc = desc;
        }

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
        public String getDesc() {
            return this.mDesc;
        }

        public static EnumWithDesc fromString(String desc) {
            if (desc == null || "all".equals(desc) || "allbroad".equals(desc)) {
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
        LIST("L"),
        CLOUD_PUSH_LIST("CL");
        
        private String mDesc;

        private AppStartReason(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }
    }

    public enum BroadcastSource implements EnumWithDesc {
        BROADCAST_FILTER("broadcast_filter");
        
        private String mDesc;

        private BroadcastSource(String desc) {
            this.mDesc = desc;
        }

        @Override // android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc
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
}
