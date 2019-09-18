package android.rms.iaware;

import android.net.Uri;
import android.os.SystemProperties;

public class AwareConstant {
    public static final int APP_START = 1;
    public static final int APP_START_ONFIRE = 3;
    public static final int BETA_UESR_TYPE = 3;
    public static final String CONFIG_FILES_PATH = "/data/system/iaware";
    public static final int CURRENT_USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 0);
    public static final int INVALID_TYPE_ID = -1;
    public static final int STATISTICS_TYPE_APPLICATION_STARTUP_TIME = 3;
    public static final int STATISTICS_TYPE_COLLECT_DATA = 1;
    public static final int STATISTICS_TYPE_EXECUTIVE_STRATEGY = 2;
    private static final String TAG = "AwareConstant";
    public static final int VER_MAJOR = 1;
    public static final int VER_MINOR = 0;
    public static final int WINDOW_SWITCH = 2;
    public static final int WINDOW_TYPE_ALERT = 1;
    public static final int WINDOW_TYPE_ALL = 3;
    public static final int WINDOW_TYPE_NONE = -1;
    public static final int WINDOW_TYPE_TOAST = 2;

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

    public enum FeatureType {
        FEATURE_ALL,
        FEATURE_MEMORY,
        FEATURE_CPU,
        FEATURE_APPMNG,
        FEATURE_RESOURCE,
        FEATURE_APPHIBER,
        FEATURE_APPACC,
        FEATURE_IO,
        FEATURE_DEFRAG,
        FEATURE_BROADCAST,
        FEATURE_MEMORY2,
        FEATURE_APPSTARTUP,
        FEATURE_INTELLI_REC,
        FEATURE_RECG_FAKEACTIVITY,
        FEATURE_NETWORK_TCP,
        FEATURE_APPFREEZE,
        FEATURE_IO_LIMIT,
        FEATURE_APPCLEANUP,
        FEATURE_VSYNC,
        FEATURE_DEVSCHED,
        FEATURE_ALARM_MANAGER,
        FEATURE_BLIT_PARALLEL,
        FEATURE_BROADCASTEX,
        FEATURE_SYSLOAD,
        FEATURE_APP_QUICKSTART,
        FEATURE_NETWORK_TCP_NODELAY,
        FEATURE_STARTWINDOW,
        PRELOADRESOURCE,
        FEATURE_CPU_LIMIT,
        FEATURE_INVALIDE_TYPE;

        public static FeatureType getFeatureType(int featureId) {
            FeatureType[] features = values();
            if (featureId < 0 || featureId >= features.length) {
                return FEATURE_INVALIDE_TYPE;
            }
            return features[featureId];
        }

        public static int getFeatureId(FeatureType type) {
            if (type == null) {
                return -1;
            }
            return type.ordinal();
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
        RESOURCE_BROADCAST_MPAPP_SET,
        RESOURCE_SHOW_INPUTMETHOD,
        RESOURCE_FACE_RECOGNIZE,
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

    public static final String getConfigFilesPath() {
        return CONFIG_FILES_PATH;
    }
}
