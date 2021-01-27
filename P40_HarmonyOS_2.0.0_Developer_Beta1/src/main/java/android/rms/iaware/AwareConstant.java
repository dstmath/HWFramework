package android.rms.iaware;

import android.net.Uri;
import android.os.Process;
import android.os.SystemProperties;

public class AwareConstant {
    public static final int ACCOUNT_SYNC = 8019;
    public static final int ALARM = 8021;
    public static final int APP_START = 1;
    public static final int APP_START_ONFIRE = 3;
    public static final int BETA_UESR_TYPE = 3;
    public static final int BIND_SERVICE = 8013;
    public static final int CACHED_MEMORY = 8023;
    public static final int CAMERA_FREEZE = 8009;
    public static final int CAMERA_IOLIMIT = 8011;
    public static final String CONFIG_FILES_PATH = "/data/system/iaware";
    public static final int CPUFG_CTRL = 8022;
    public static final int CRASH = 8003;
    public static final int CURRENT_USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 0);
    public static final int FAST_FREEZE = 8008;
    public static final int GPS_STRATEGY = 1901;
    public static final int INVALID_TYPE_ID = -1;
    public static final int IOLIMIT = 8010;
    public static final int JOB_SCHEDULE = 8018;
    public static final int MEMORY = 8001;
    public static final int MEMORY_REPAIR = 8005;
    public static final int MEMORY_REPAIR_VSS = 8006;
    public static final int OVERSEA_BETA_UESR_TYPE = 5;
    public static final int POWER_GENIE = 8002;
    public static final int PROVIDER = 8014;
    public static final int SCHEDULE_RESTART = 8020;
    public static final int SMART_CLEAN = 8004;
    public static final int START_SERVICE = 8012;
    public static final int STATISTICS_TYPE_APPLICATION_STARTUP_TIME = 3;
    public static final int STATISTICS_TYPE_COLLECT_DATA = 1;
    public static final int STATISTICS_TYPE_EXECUTIVE_STRATEGY = 2;
    public static final int SYSTEM_BROADCAST = 8016;
    public static final int SYSTEM_MANAGER = 8000;
    public static final int SYSTEM_MEMORY_REPAIR = 8007;
    private static final String TAG = "AwareConstant";
    public static final int THIRD_ACTIVITY = 8017;
    public static final int THIRD_BROADCAST = 8015;
    public static final int VER_MAJOR = 1;
    public static final int VER_MINOR = 0;
    public static final String VPN_STATE = "vpn_state";
    public static final int WIFI_FEATURE = 1902;
    public static final int WINDOW_SWITCH = 2;
    public static final int WINDOW_TYPE_ALERT = 1;
    public static final int WINDOW_TYPE_ALL = 3;
    public static final int WINDOW_TYPE_NONE = -1;
    public static final int WINDOW_TYPE_TOAST = 2;

    public static final String getConfigFilesPath() {
        return CONFIG_FILES_PATH;
    }

    public enum FeatureType {
        FEATURE_ALL(0),
        FEATURE_MEMORY(1),
        FEATURE_CPU(2),
        FEATURE_APPMNG(3),
        FEATURE_RESOURCE(4),
        FEATURE_APPHIBER(5),
        FEATURE_APPACC(6),
        FEATURE_IO(7),
        FEATURE_DEFRAG(8),
        FEATURE_BROADCAST(9),
        FEATURE_MEMORY2(10),
        FEATURE_APPSTARTUP(11),
        FEATURE_INTELLI_REC(12),
        FEATURE_RECG_FAKEACTIVITY(13),
        FEATURE_NETWORK_TCP(14),
        FEATURE_APPFREEZE(15),
        FEATURE_IO_LIMIT(16),
        FEATURE_APPCLEANUP(17),
        FEATURE_VSYNC(18),
        FEATURE_DEVSCHED(19),
        FEATURE_ALARM_MANAGER(20),
        FEATURE_BLIT_PARALLEL(21),
        FEATURE_BROADCASTEX(22),
        FEATURE_SYSLOAD(23),
        FEATURE_APP_QUICKSTART(24),
        FEATURE_STARTWINDOW(26),
        PRELOADRESOURCE(27),
        FEATURE_CPU_LIMIT(28),
        FEATURE_QOS(29),
        FEATURE_SWITCH_CLEAN(30),
        FEATURE_APPSCENEMNG(31),
        FEATURE_PROFILE(32),
        FEATURE_SCENE_RECOG(33),
        FEATURE_APPACCURATE_RECG(34),
        FEATURE_NETQOS(35),
        FEATURE_START_WINDOW_CACHE(36),
        FEATURE_COMPONENT_PRELOAD(37),
        FEATURE_DISPLAY_SMOOTH(38),
        FEATURE_INVALIDE_TYPE(Process.NOBODY_UID);
        
        private final int value;

        private FeatureType(int value2) {
            this.value = value2;
        }

        public int getValue() {
            return this.value;
        }

        public static FeatureType getFeatureType(int featureId) {
            if (featureId < 0) {
                return FEATURE_INVALIDE_TYPE;
            }
            FeatureType[] values = values();
            for (FeatureType type : values) {
                if (type.getValue() == featureId) {
                    AwareLog.d(AwareConstant.TAG, "getFeatureType :" + featureId + "valid with name:" + type.name());
                    return type;
                }
            }
            return FEATURE_INVALIDE_TYPE;
        }

        public static int getFeatureId(FeatureType type) {
            if (type == null) {
                return FEATURE_INVALIDE_TYPE.getValue();
            }
            return type.getValue();
        }

        public static int getFeatureId(int featureId) {
            if (featureId < 0) {
                return FEATURE_INVALIDE_TYPE.getValue();
            }
            FeatureType[] values = values();
            for (FeatureType type : values) {
                if (type.getValue() == featureId) {
                    AwareLog.d(AwareConstant.TAG, "getFeatureId :" + featureId + "valid with name:" + type.name());
                    return featureId;
                }
            }
            return FEATURE_INVALIDE_TYPE.getValue();
        }
    }

    public enum ResourceType {
        RESOURCE_SCREEN_ON,
        RESOURCE_SCREEN_OFF,
        RESOURCE_APPASSOC,
        RESOURCE_BOOT_COMPLETED,
        RES_APP,
        RES_DEV_STATUS,
        RES_INPUT,
        RESOURCE_USERHABIT,
        RESOURCE_GAME_BOOST,
        RESOURCE_SCENE_REC,
        RESOURCE_STATUS_BAR,
        RESOURCE_MEDIA_BTN,
        RESOURCE_INSTALLER_MANAGER,
        RESOURCE_HOME,
        RESOURCE_NET_MANAGE,
        RESOURCE_USER_PRESENT,
        RESOURCE_SYSLOAD,
        RESOURCE_SHUTDOWN,
        RESOURCE_SHOW_INPUTMETHOD,
        RESOURCE_HIDE_INPUTMETHOD,
        RESOURCE_FACE_RECOGNIZE,
        RESOURCE_VPN_CONN,
        RESOURCE_WINSTATE,
        RESOURCE_TOP_ACTIVITY,
        RESOURCE_PKG_CLEAR_DATA,
        RESOURCE_SET_HM_THREAD_RTG,
        RESOURCE_INVALIDE_TYPE;

        public static ResourceType getResourceType(int resourceId) {
            ResourceType[] resources = values();
            if (resourceId < 0 || resourceId >= resources.length) {
                return RESOURCE_INVALIDE_TYPE;
            }
            return resources[resourceId];
        }

        public static int getReousrceId(ResourceType type) {
            if (type == null) {
                return -1;
            }
            return type.ordinal();
        }
    }

    public static class AppUsageDatabase {
        public static final Uri APPUSAGE_URI = Uri.parse(makeAppUsageUri(UsageTables.APPUSAGE));
        public static final String AUTHORITY = "iaware.app.usage";
        public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";

        public interface HwAppUsage {
            public static final String BACKGROUND_TIME = "backgroundTime";
            public static final String FOREGROUNG_TIME = "foregroungTime";
            public static final String PKG_NAME = "pkgName";
            public static final String USER_ID = "userID";
        }

        public interface UsageTables {
            public static final String APPUSAGE = "appusage";
        }

        public static String makeAppUsageUri(String table) {
            return "content://iaware.app.usage/" + table;
        }
    }

    public static class Database {
        public static final Uri APPTYPE_URI = Uri.parse(makeUri(AwareTables.APPTYPE));
        public static final Uri ASSOCIATE_URI = Uri.parse(makeUri(AwareTables.ASSOCIATE));
        public static final String AUTHORITY = "iaware.huawei.com";
        public static final Uri HABITPROTECTLIST_URI = Uri.parse(makeUri(AwareTables.HABITPROTECTLIST));
        public static final Uri PKGNAME_URI = Uri.parse(makeUri(AwareTables.PKGNAME));
        public static final Uri PKGRECORD_URI = Uri.parse(makeUri(AwareTables.PKGRECORD));
        public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
        public static final Uri USERDATA_URI = Uri.parse(makeUri(AwareTables.USERDATA));

        public interface AwareTables {
            public static final String APPTYPE = "AppType";
            public static final String ASSOCIATE = "Associate";
            public static final String HABITPROTECTLIST = "HabitProtectList";
            public static final String PKGNAME = "PkgName";
            public static final String PKGRECORD = "PkgRecord";
            public static final String USERDATA = "UserData";
        }

        public interface HwAppType {
            public static final String APP_TYPE = "appType";
            public static final String PKGNAME = "appPkgName";
            public static final String RECOG_TIME = "recogTime";
            public static final String RECOG_VERSION = "recogVersion";
            public static final String SOURCE = "source";
            public static final String TYPE_ATTRI = "typeAttri";
        }

        public interface HwAssociate {
            public static final String DST_PKGNAME = "dstPkgName";
            public static final String SRC_PKGNAME = "srcPkgName";
            public static final String TRANSITION_TIMES = "transitionTimes";
            public static final String USER_ID = "userID";
        }

        public interface HwHabitProtectList {
            public static final String APPTYPE = "appType";
            public static final String AVGUSEDFREQUENCY = "avgUsedFrequency";
            public static final String DELETED = "deleted";
            public static final String DELETED_TIME = "deletedTime";
            public static final String PKGNAME = "appPkgName";
            public static final String RECENTUSED = "recentUsed";
            public static final String USER_ID = "userID";
            public static final String _ID = "_id";
        }

        public interface HwPkgName {
            public static final String DELETED = "deleted";
            public static final String DELETED_TIME = "deletedTime";
            public static final String PKGNAME = "appPkgName";
            public static final String TOTAL_IN_DAY = "totalInDay";
            public static final String TOTAL_IN_NIGHT = "totalInNight";
            public static final String TOTAL_USE_TIMES = "totalUseTimes";
            public static final String USER_ID = "userID";
        }

        public interface HwPkgRecord {
            public static final String FLAG = "flag";
            public static final String PKGNAME = "appPkgName";
            public static final String USER_ID = "userID";
        }

        public interface HwUserData {
            public static final String PKG_NAME = "appPkgName";
            public static final String SWITCH_FG_TIME = "switchToFgTime";
            public static final String TIME = "time";
            public static final String USER_ID = "userID";
            public static final String _ID = "_id";
        }

        public static String makeUri(String table) {
            return "content://iaware.huawei.com/" + table;
        }
    }
}
