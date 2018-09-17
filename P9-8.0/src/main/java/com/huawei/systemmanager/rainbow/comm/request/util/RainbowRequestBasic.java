package com.huawei.systemmanager.rainbow.comm.request.util;

public class RainbowRequestBasic {
    private static final String COMMON_APP_RIGHT_LIST = "arList";
    private static final String COMMON_PKG_NAME = "aPN";
    private static final String COMMON_RIGHT_LIST = "rL";
    private static final String HOST_NAME_URL = "https://cloudsafe.hicloud.com/";

    public interface BackgroundConfigJsonField {
        public static final String IS_CONTROLLED = "isC";
        public static final String IS_KEY_TASK = "isK";
        public static final String IS_PROTECTED = "isP";
    }

    public interface BasicCloudField {
        public static final String CONFIG_SERVRE_RESULT_CODE = "resultCode";
        public static final String PHONE_EMUI = "emui";
        public static final String PHONE_IMEI = "imei";
        public static final String PHONE_OS_VERSION = "os";
        public static final String PHONE_SYSTEM = "systemid";
        public static final String PHONE_TYPE = "model";
        public static final String SERVRE_RESULT_CODE = "resultcode";
    }

    public interface CheckVersionField {
        public static final String CHECK_VERSION_BACKGROUND_WHITE = "v2_0005";
        public static final String CHECK_VERSION_COMPETITOR = "wbList_0034";
        public static final String CHECK_VERSION_COMPONENTS = "components";
        public static final String CHECK_VERSION_CONTROL_BLACK = "wbList_0009";
        public static final String CHECK_VERSION_CONTROL_WHITE = "wbList_0010";
        public static final int CHECK_VERSION_MAX_UPDATE_DAY = 1000;
        public static final String CHECK_VERSION_MESSAGE_SAFE = "messageSafe";
        public static final int CHECK_VERSION_MIN_UPDATE_DAY = 1;
        public static final String CHECK_VERSION_NAME = "name";
        public static final String CHECK_VERSION_NOTIFICATION = "notificationVer";
        public static final String CHECK_VERSION_PHONE = "wbList_0030";
        public static final String CHECK_VERSION_PUSH = "wbList_0011";
        public static final String CHECK_VERSION_RECRIGHT = "recRight";
        public static final String CHECK_VERSION_RIGHT = "right";
        public static final String CHECK_VERSION_SERVER_URL = "url";
        public static final String CHECK_VERSION_SMART_CONTROL = "smartControl";
        public static final String CHECK_VERSION_STARTUP = "startupVer";
        public static final String CHECK_VERSION_UNIFIED_POWER_APPS = "dozeVer";
        public static final String CHECK_VERSION_UPDATE_CYCLE = "pollingCycle";
        public static final String CHECK_VERSION_VERSION = "version";
    }

    public interface CloudUrls {
        public static final String GET_APPS_PERMISSIONS = "getApkRight.do";
        public static final String GET_WHITE_BLACK_APPLIST = "v2/getBlackList.do";
        public static final String POST_CHECK_VERSION = "checkVersion.do";
        public static final String POST_DEVICE_TOKEN = "v2/registerDT.do";
        public static final String RECOMMEND_APPS_RIGHTS = "getRecApkRight.do";
    }

    public interface GetApkRightField {
        public static final String APP_DELETE_LIST = "apDelList";
        public static final String APP_IS_TRUST = "isT";
        public static final String APP_LISTS = "arList";
        public static final String APP_PACKAGENAME = "aPN";
        public static final String APP_RIGHTS_LIST_VERSION = "rlVer";
        public static final String APP_RIGHT_LIST = "rL";
        public static final String INCREASE_SUPPORT = "IncSupport";
        public static final String PERMISSION_ID = "rID";
        public static final String PERMISSION_POLICY = "rT";
    }

    public interface GetAppListField {
        public static final String WHITE_BLACK_LIST = "blackList";
        public static final String WHITE_BLACK_LIST_TYPE = "blType";
        public static final String WHITE_BLACK_LIST_VERSION = "blVer";
        public static final String WHITE_BLACK_PACKAGENAME = "aPN";
    }

    public interface MessageSafeConfigJsonField {
        public static final String MESSAGE_NUMBER = "messageNo";
        public static final String PARTNER = "partner";
        public static final String SECURE_LINK = "secureLink";
        public static final String UPDATE_STATUS = "status";
    }

    public interface NotificationConfigJsonField {
        public static final String CAN_FORBIDDEN = "fbd";
        public static final String HEADSUB_CFG = "hCfg";
        public static final String IS_CONTROLLED = "isC";
        public static final String LOCKSCREEN_CFG = "lCfg";
        public static final String NOTIFICATIOIN_CFG = "nCfg";
        public static final String STATUSBAR_CFG = "sCfg";
    }

    public interface RecApkRightField {
        public static final String COMMON_APP_VERSION = "arV";
        public static final String REC_IN_APP_LIST = "apL";
        public static final String REC_IN_APP_PKG_NAME = "aPN";
        public static final String REC_IN_APP_VERSION = "arV";
        public static final String REC_OUT_APP_PKG_NAME = "aPN";
        public static final String REC_OUT_APP_VERSION = "arV";
        public static final String REC_OUT_RIGHT_LIST = "rL";
        public static final String REC_OUT_RIGHT_RESULT_LIST = "arList";
    }

    public interface SmartControlConfigJsonField {
        public static final String IS_AUTO_AWAKE = "isWake";
        public static final String IS_AUTO_START = "isAuto";
        public static final String IS_BACKGROUND = "isBg";
        public static final String IS_SHOW = "isS";
        public static final String OPERATION = "isOpr";
    }

    public interface StartupConfigJsonField {
        public static final String IS_CONTROLLED = "isC";
        public static final String IS_PROVIDER = "isP";
        public static final String IS_RECEIVER = "isR";
    }

    public interface UnifiedPowerAppsConfigJsonField {
        public static final String IS_PROTECTED = "isP";
        public static final String IS_SHOW = "isS";
    }

    public static String getUrlForCommon(String subUrlPath) {
        return "https://cloudsafe.hicloud.com/servicesupport/cloudsafe/" + subUrlPath;
    }
}
