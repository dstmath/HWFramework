package android.rms.iaware;

import android.net.Uri;
import android.os.SystemProperties;
import android.telecom.Logging.Session;
import com.android.internal.content.NativeLibraryHelper;

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

    public static class Database {
        public static final Uri APPTYPE_URI = Uri.parse(makeUri(AwareTables.APPTYPE));
        public static final Uri ASSOCIATE_URI = Uri.parse(makeUri(AwareTables.ASSOCIATE));
        public static final String AUTHORITY = "iaware.huawei.com";
        public static final Uri CAPTURE_URI = Uri.parse(makeUri(AwareTables.CAPTURE_TABLE));
        public static final Uri CMPRECG_URI = Uri.parse(makeUri(AwareTables.CMP_RECG));
        public static final Uri HABITMODEL_URI = Uri.parse(makeUri(AwareTables.HABITMODEL_TABLE));
        public static final Uri HABITPROTECTLIST_URI = Uri.parse(makeUri(AwareTables.HABITPROTECTLIST));
        public static final Uri PKGNAME_URI = Uri.parse(makeUri(AwareTables.PKGNAME));
        public static final Uri PKGRECORD_URI = Uri.parse(makeUri(AwareTables.PKGRECORD));
        public static final Uri PKG_UPDATE_URI = Uri.parse(makeUri(AwareTables.PKG_UPDATE));
        private static final String[] TABLES = new String[]{AwareTables.USERDATA, AwareTables.ASSOCIATE, AwareTables.PKGNAME, AwareTables.PKGRECORD, AwareTables.HABITPROTECTLIST, AwareTables.APPTYPE, AwareTables.CAPTURE_TABLE, AwareTables.HABITMODEL_TABLE, AwareTables.CMP_RECG, AwareTables.PKG_UPDATE};
        public static final String UNKNOWN_MIME_TYPE = "application/octet-stream";
        public static final Uri USERDATA_URI = Uri.parse(makeUri(AwareTables.USERDATA));

        public interface AwareTables {
            public static final String APPTYPE = "AppType";
            public static final String ASSOCIATE = "Associate";
            public static final String CAPTURE_TABLE = "capture";
            public static final String CMP_RECG = "CmpRecg";
            public static final String HABITMODEL_TABLE = "HabitModel";
            public static final String HABITPROTECTLIST = "HabitProtectList";
            public static final String PKGNAME = "PkgName";
            public static final String PKGRECORD = "PkgRecord";
            public static final String PKG_UPDATE = "PkgUpdate";
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

        public interface HwCapture {
            public static final String APP_INSTALL = "appInstall";
            public static final String APP_UNINSTALL = "appUnInstall";
            public static final String CHARGING_STATE = "chargingState";
            public static final String DAY_ATTRI = "dayAttribute";
            public static final String EARLINE_CONNECTED = "earlineConnected";
            public static final String FLAG = "flag";
            public static final String ID = "id";
            public static final String NETTYPE = "netType";
            public static final String PKG_NAME = "pkgName";
            public static final String PLACE_TAG = "placeTag";
            public static final String SCREEN_STATE = "screenState";
            public static final String TIME = "timeStamp";
        }

        public interface HwCmpType {
            public static final String CLS = "cls";
            public static final String CMP_TYPE = "cmpType";
            public static final String ID = "id";
            public static final String PERCEPTION_COUNT = "perceptionCount";
            public static final String PKGNAME = "appPkgName";
            public static final String TIME = "time";
            public static final String UNPERCEPTION_COUNT = "unperceptionCount";
            public static final String USERID = "userId";
        }

        public interface HwHabitModel {
            public static final String MODEL_TYPE = "modeType";
            public static final String SCALER_DATA = "scalerData";
            public static final String TRAIN_MODEL = "trainModel";
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

        public interface HwPkgUpdate {
            public static final String FLAG = "flag";
            public static final String PKG_NAME = "pkgName";
            public static final String TIME = "time";
        }

        public interface HwUserData {
            public static final String PKG_NAME = "appPkgName";
            public static final String SWITCH_FG_TIME = "switchToFgTime";
            public static final String TIME = "time";
            public static final String USER_ID = "userID";
            public static final String _ID = "_id";
        }

        public static int getTableCount() {
            return TABLES.length;
        }

        public static String getTableNameByIdx(int idx) {
            if (idx < 0 || idx >= TABLES.length) {
                return "Unknown";
            }
            return TABLES[idx];
        }

        public static String makeUri(String table) {
            return "content://iaware.huawei.com/" + table;
        }

        public static String makeMimeType(String table, boolean haveRowId) {
            if (haveRowId) {
                return "vnd.android.cursor.item/" + table;
            }
            return "vnd.android.cursor.dir/" + table;
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
        RESOURCE_APP_FREEZE,
        RESOURCE_INSTALLER_MANAGER,
        RESOURCE_HOME,
        RESOURCE_NET_MANAGE,
        RESOURCE_USER_PRESENT,
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

    private static final String getExternalInfo() {
        return "rev";
    }

    private static final boolean checkConfigFileId(String fileId) {
        if (fileId.length() > 91) {
            return false;
        }
        return fileId.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|]*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
    }

    private static final String getDefaultConfigFileId() {
        return "iaware_config_1.0.xml";
    }

    public static final String generateConfigFileId() {
        StringBuilder builder = new StringBuilder();
        builder.append(SystemProperties.get("ro.product.model", "NXT-AL10"));
        builder.append(Session.SESSION_SEPARATION_CHAR_CHILD);
        builder.append(SystemProperties.get("ro.product.locale.region", "CN"));
        builder.append("_iaware_config_");
        builder.append(1).append(".").append(0);
        builder.append(Session.SESSION_SEPARATION_CHAR_CHILD);
        builder.append(getExternalInfo());
        builder.append(".xml");
        AwareLog.d(TAG, "Original string: '" + builder.toString() + "'");
        String fileId = builder.toString().replace(" ", NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
        if (!checkConfigFileId(fileId)) {
            AwareLog.w(TAG, "Illegal file id, use default");
            fileId = getDefaultConfigFileId();
        }
        AwareLog.d(TAG, "Final file id string: '" + fileId + "'");
        return fileId;
    }

    public static final String getCloudDownloadedFilePath() {
        return "/data/system/iaware/" + generateConfigFileId();
    }

    public static final String getConfigFilesPath() {
        return CONFIG_FILES_PATH;
    }
}
