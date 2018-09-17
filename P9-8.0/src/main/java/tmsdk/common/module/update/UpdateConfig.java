package tmsdk.common.module.update;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public final class UpdateConfig {
    public static final String ADBLOCKING_REPORT_WHITE_NAME = (intToString(10017) + ".dat");
    public static final String ADB_DES_LIST_NAME = (intToString(40002) + ".dat");
    public static final String AMS_DATA = (intToString(100003) + ".dat");
    public static final String ANTITHEFT_WEB_SERVER_NUM = (intToString(40008) + ".dat");
    public static final String APP_USAGE_PRE_NAME = (intToString(40545) + ".dat");
    public static final String BLACKLIST_KILL_PROCESS_NAME = (intToString(10008) + ".dat");
    public static final String CAMERA_SOFTWARE_LIST_NAME = (intToString(10012) + ".dat");
    public static final String CAMERA_SOFTWARE_NEW_LIST_PATH_NAME = (intToString(10020) + ".dat");
    public static final String DEEPCLEAN_SDCARD_SCAN_RULE_NAME = (intToString(40248) + ".dat");
    public static final String DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2_SDK = (intToString(40415) + ".dat");
    public static final String DEEPCLEAN_SOFTWARE_LIST_NAME = (intToString(10013) + ".dat");
    public static final String DEEPCLEAN_SOFT_PATH_LIST_NAME = (intToString(10014) + ".dat");
    public static final String DEEPCLEAN_SOFT_SCAN_RULE_NAME = (intToString(40003) + ".dat");
    public static final String DEEP_CLEAN_APPGROUP_DESC = (intToString(40350) + ".dat");
    public static final String DEEP_CLEAN_MEDIA_SRC_PATH_NAME = (intToString(10016) + ".dat");
    public static final String DEEP_CLEAN_WHITE_LIST_PATH_NAME = (intToString(10019) + ".dat");
    public static final String DeepCleanSdcardScanRuleWord = (intToString(40102) + ".dat");
    public static final String DeepCleanSoftScanRuleWord = (intToString(40101) + ".dat");
    public static final int[] FILE_IDS = new int[]{50001, 60001, 80001, 20001, 10001, 10002, 10007, 10008, 10011, 10009, 10010, 10012, 10013, 10014, 10018, 10015, 10017, 10016, 40458, 70003, 30001, 90003, 40427, 10019, 10020, 10021, 40001, 40002, 40248, 40006, 40007, 40008, 100003, 40011, 40012, 40013, 40101, 40102, 40201, 100004, 40015, 60002, 40205, 40203, 60003, 40461, 70002, 40291, 40350, 40545};
    public static final String[] FILE_NAMES = new String[]{LOCATION_NAME, SMS_CHECKER_NAME, TRUST_URLS_NAME, TRAFFIC_MONITOR_CONFIG_NAME, WHITELIST_COMMON_NAME, WHITELIST_UNUSUAL_NAME, PRIVACYLOCKLIST_USUAL_NAME, BLACKLIST_KILL_PROCESS_NAME, WHITELIST_KILL_PROCESS_NAME, WHITELIST_AUTO_BOOT_NAME, WHITELIST_PERMISSION_CONTROL_NAME, CAMERA_SOFTWARE_LIST_NAME, DEEPCLEAN_SOFTWARE_LIST_NAME, DEEPCLEAN_SOFT_PATH_LIST_NAME, PERMIS_MONITOR_LIST_NAME, NOTKILLLIST_KILL_PROCESSES_NAME, ADBLOCKING_REPORT_WHITE_NAME, DEEP_CLEAN_MEDIA_SRC_PATH_NAME, NUM_MARK_NAME, VIRUS_BASE_NAME, SYSTEM_SCAN_CONFIG_NAME, PAY_LIST_NAME, WHITELIST_CLOUDSCAN_NAME, DEEP_CLEAN_WHITE_LIST_PATH_NAME, CAMERA_SOFTWARE_NEW_LIST_PATH_NAME, ROOT_PHONE_WHITE_LIST_PATH_NAME, H_LIST_NAME, ADB_DES_LIST_NAME, DEEPCLEAN_SDCARD_SCAN_RULE_NAME, PROCESSMANAGER_WHITE_LIST_NAME, TMSLITE_COMMISION_LIST_NAME, ANTITHEFT_WEB_SERVER_NUM, AMS_DATA, QUICK_PANEL_SUP, PCLinkedSoftCommunicateFlow, WeixinTrashClean, DeepCleanSoftScanRuleWord, DeepCleanSdcardScanRuleWord, KingUserPermision, SkinConfig, SoftLock_LockFunc, POSEIDON, GAME_SPEED_UP_LIST_NAME, QUICKPANEL_WIFIMANAGE, POSEIDONV2, YELLOW_PAGEV2_LARGE, VIRUS_BASE_EN_NAME, RUBBISH_ENGLISH_TABLE_NAME, DEEP_CLEAN_APPGROUP_DESC, APP_USAGE_PRE_NAME};
    public static final String GAME_SPEED_UP_LIST_NAME = (intToString(40205) + ".dat");
    public static final String H_LIST_NAME = (intToString(40001) + ".dat");
    private static final String JN = (intToString(40459) + ".sdb");
    private static final String JO = (intToString(40606) + ".sdb");
    public static final String KingUserPermision = (intToString(40201) + ".dat");
    public static final String LOCATION_NAME = (intToString(50001) + ".sdb");
    public static final String NOTKILLLIST_KILL_PROCESSES_NAME = (intToString(10015) + ".dat");
    public static final String NUM_MARK_NAME = (intToString(40458) + ".sdb");
    public static final String PATCH_SUFIX = ".patch";
    public static final String PAY_LIST_NAME = (intToString(90003) + ".dat");
    public static final String PCLinkedSoftCommunicateFlow = (intToString(40012) + ".dat");
    public static final String PERMIS_MONITOR_LIST_NAME = (intToString(10018) + ".dat");
    public static final String POSEIDON = (intToString(60002) + ".dat");
    public static final String POSEIDONV2 = (intToString(60003) + ".dat");
    public static final String PRIVACYLOCKLIST_USUAL_NAME = (intToString(10007) + ".dat");
    public static final String PROCESSMANAGER_WHITE_LIST_NAME = (intToString(40006) + ".dat");
    public static final String QUICKPANEL_WIFIMANAGE = (intToString(40203) + ".dat");
    public static final String QUICK_PANEL_SUP = (intToString(40011) + ".dat");
    public static final String ROOT_PHONE_WHITE_LIST_PATH_NAME = (intToString(10021) + ".dat");
    public static final String RUBBISH_ENGLISH_TABLE_NAME = (intToString(40291) + ".dat");
    public static final String SMS_CHECKER_NAME = (intToString(60001) + ".sys");
    public static final String SYSTEM_SCAN_CONFIG_NAME = (intToString(30001) + ".dat");
    public static final String SkinConfig = (intToString(100004) + ".dat");
    public static final String SoftLock_LockFunc = (intToString(40015) + ".dat");
    public static final String SpaceMangerList = (intToString(40006) + ".dat");
    public static final String TMSLITE_COMMISION_LIST_NAME = (intToString(40007) + ".dat");
    public static final String TRAFFIC_MONITOR_CONFIG_NAME = (intToString(20001) + ".dat");
    public static final String TRUST_URLS_NAME = (intToString(80001) + ".dat");
    public static final long UPDATA_FLAG_NUM_MARK = 536870912;
    public static final long UPDATE_ADBLOCKING_REPORT_WHITE_LIST = 134217728;
    public static final long[] UPDATE_FLAGS = new long[]{2, 4, 16, 32, 512, UPDATE_FLAG_WHITELIST_UNUSUAL, UPDATE_FLAG_PRIVACYLOCKLIST_USUAL, UPDATE_FLAG_BLACKLIST_PROCESS, UPDATE_FLAG_WHITELIST_PROCESS, UPDATE_FLAG_WHITELIST_AUTO_BOOT, UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL, UPDATE_FLAG_CAMERA_SOFTWARE_LIST, UPDATE_FLAG_DEEPCLEAN_SOFTWARE_LIST, UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST, UPDATE_FLAG_PERMIS_MONITOR_LIST, UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES, UPDATE_ADBLOCKING_REPORT_WHITE_LIST, UPDATE_FLAG_DEEP_CLEAN_MEDIA_SRC_PATH, UPDATA_FLAG_NUM_MARK, UPDATE_FLAG_VIRUS_BASE, UPDATE_FLAG_SYSTEM_SCAN_CONFIG, 4294967296L, UPDATE_FLAG_VIRUSKILLER_CLOUDSCAN_WHITE, UPDATE_FLAG_DEEP_CLEAN_WHITE_LIST, UPDATE_FLAG_CAMERA_SOFTWARE_NEW_LIST, UPDATE_FLAG_ROOT_PHONE_WHITE_LIST, UPDATE_FLAG_H_LIST, UPDATE_FLAG_ADB_DES_LIST, UPDATE_FLAG_DEEPCLEAN_SDCARD_SCAN_RULE, UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST, UPDATE_FLAG_TMSLITE_COMMISION_LIST, UPDATE_FLAG_ANTITHEFT_WEB_SERVER_NUM, UPDATE_FLAG_AMS_DATA, UPDATE_FLAG_QUICK_PANEL_SUP, UPDATE_FLAG_PCLinkedSoftCommunicateFlow, UPDATE_FLAG_WeixinTrashClean, UPDATE_FLAG_EFN_DeepCleanSoftScanRuleWord, UPDATE_FLAG_DeepCleanSdcardScanRuleWord, UPDATE_FLAG_KingUserPermision, UPDATE_FLAG_SkinConfig, UPDATE_FLAG_SoftLock_LockFunc, UPDATE_FLAG_POSEIDON, UPDATE_FLAG_GAME_SPEED_UP, UPDATE_FLAG_QUICKPANEL_WIFIMANAGE, UPDATE_FLAG_POSEIDONV2, UPDATE_FLAG_YELLOW_PAGEV2_Large, UPDATE_FLAG_VIRUS_BASE_ENG, UPDATE_FLAG_ENGLISH_TABLE, UPDATE_FLAG_DEEP_CLEAN_APPGROUP_DESC, Long.MIN_VALUE};
    public static final long UPDATE_FLAG_ADB_DES_LIST = 274877906944L;
    public static final long UPDATE_FLAG_ALL = 33554432;
    public static final long UPDATE_FLAG_AMS_DATA = 35184372088832L;
    public static final long UPDATE_FLAG_ANTITHEFT_WEB_SERVER_NUM = 17592186044416L;
    public static final long UPDATE_FLAG_APP_LIST = 67108864;
    public static final long UPDATE_FLAG_APP_USAGE_PRE = Long.MIN_VALUE;
    public static final long UPDATE_FLAG_BLACKLIST_PROCESS = 8192;
    public static final long UPDATE_FLAG_CAMERA_SOFTWARE_LIST = 131072;
    public static final long UPDATE_FLAG_CAMERA_SOFTWARE_NEW_LIST = 34359738368L;
    public static final long UPDATE_FLAG_DEEPCLEAN_ONE_KEY_PATH_LIST = 2199023255552L;
    public static final long UPDATE_FLAG_DEEPCLEAN_SDCARD_SCAN_RULE = 1099511627776L;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFTWARE_LIST = 262144;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST = 524288;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFT_SCAN_RULE = 549755813888L;
    public static final long UPDATE_FLAG_DEEP_CLEAN_APPGROUP_DESC = 576460752303423488L;
    public static final long UPDATE_FLAG_DEEP_CLEAN_MEDIA_SRC_PATH = 268435456;
    public static final long UPDATE_FLAG_DEEP_CLEAN_WHITE_LIST = 17179869184L;
    public static final long UPDATE_FLAG_DeepCleanSdcardScanRuleWord = 1125899906842624L;
    public static final long UPDATE_FLAG_EFN_DeepCleanSoftScanRuleWord = 562949953421312L;
    public static final long UPDATE_FLAG_ENGLISH_TABLE = 4611686018427387904L;
    public static final long UPDATE_FLAG_GAME_SPEED_UP = 36028797018963968L;
    public static final long UPDATE_FLAG_H_LIST = 137438953472L;
    public static final long UPDATE_FLAG_KingUserPermision = 2251799813685248L;
    public static final long UPDATE_FLAG_LOCATION = 2;
    public static final long UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES = 2097152;
    public static final long UPDATE_FLAG_PAY_LIST = 4294967296L;
    public static final long UPDATE_FLAG_PCLinkedSoftCommunicateFlow = 140737488355328L;
    public static final long UPDATE_FLAG_PERMIS_MONITOR_LIST = 1048576;
    public static final long UPDATE_FLAG_POSEIDON = 18014398509481984L;
    public static final long UPDATE_FLAG_POSEIDONV2 = 144115188075855872L;
    public static final long UPDATE_FLAG_PRIVACYLOCKLIST_USUAL = 4096;
    public static final long UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST = 4398046511104L;
    public static final long UPDATE_FLAG_QUICKPANEL_WIFIMANAGE = 72057594037927936L;
    public static final long UPDATE_FLAG_QUICK_PANEL_SUP = 70368744177664L;
    public static final long UPDATE_FLAG_ROOT_PHONE_WHITE_LIST = 68719476736L;
    public static final long UPDATE_FLAG_SMS_CHECKER = 4;
    public static final long UPDATE_FLAG_SYSTEM_SCAN_CONFIG = 2147483648L;
    public static final long UPDATE_FLAG_SkinConfig = 4503599627370496L;
    public static final long UPDATE_FLAG_SoftLock_LockFunc = 9007199254740992L;
    public static final long UPDATE_FLAG_TMSLITE_COMMISION_LIST = 8796093022208L;
    public static final long UPDATE_FLAG_TRAFFIC_MONITOR_CONFIG = 32;
    public static final long UPDATE_FLAG_TRUST_URLS = 16;
    public static final long UPDATE_FLAG_VIRUSKILLER_CLOUDSCAN_WHITE = 8589934592L;
    public static final long UPDATE_FLAG_VIRUS_BASE = 1073741824;
    public static final long UPDATE_FLAG_VIRUS_BASE_ENG = 2305843009213693952L;
    public static final long UPDATE_FLAG_WHITELIST_AUTO_BOOT = 32768;
    public static final long UPDATE_FLAG_WHITELIST_COMMON = 512;
    public static final long UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL = 65536;
    public static final long UPDATE_FLAG_WHITELIST_PROCESS = 16384;
    public static final long UPDATE_FLAG_WHITELIST_UNUSUAL = 1024;
    public static final long UPDATE_FLAG_WeixinTrashClean = 281474976710656L;
    public static final long UPDATE_FLAG_YELLOW_PAGEV2_Large = 1152921504606846976L;
    public static final int UPDATE_TYPE_ENGINE_UPDATE = 1;
    public static final int UPDATE_TYPE_NORMAL_UPDATE = 0;
    public static final String VIRUS_BASE_EN_NAME = (intToString(70002) + ".amf");
    public static final String VIRUS_BASE_NAME = (intToString(70003) + ".amf");
    public static final String WHITELIST_AUTO_BOOT_NAME = (intToString(10009) + ".dat");
    public static final String WHITELIST_CLOUDSCAN_NAME = (intToString(40427) + ".dat");
    public static final String WHITELIST_COMMON_NAME = (intToString(10001) + ".dat");
    public static final String WHITELIST_KILL_PROCESS_NAME = (intToString(10011) + ".dat");
    public static final String WHITELIST_PERMISSION_CONTROL_NAME = (intToString(10010) + ".dat");
    public static final String WHITELIST_UNUSUAL_NAME = (intToString(10002) + ".dat");
    public static final String WeixinTrashClean = (intToString(40013) + ".dat");
    public static final String WeixinTrashCleanNew = (intToString(40233) + ".dat");
    public static final String YELLOW_PAGE = (intToString(100009) + ".sdb");
    public static final String YELLOW_PAGEV2_LARGE = (intToString(40461) + ".sdb");
    public static final Map<String, String> sDeprecatedNameMap = new HashMap();

    static {
        sDeprecatedNameMap.put(LOCATION_NAME, "nldb.sdb");
        sDeprecatedNameMap.put(SMS_CHECKER_NAME, "rule_store.sys");
        sDeprecatedNameMap.put(TRUST_URLS_NAME, "trusturls.dat");
        sDeprecatedNameMap.put(TRAFFIC_MONITOR_CONFIG_NAME, "net_interface_type_traffic_stat.dat");
        sDeprecatedNameMap.put(WHITELIST_COMMON_NAME, "whitelist_common.dat");
        sDeprecatedNameMap.put(WHITELIST_UNUSUAL_NAME, "whitelist_unusual.dat");
        sDeprecatedNameMap.put(PRIVACYLOCKLIST_USUAL_NAME, "privacylocklist_usual.dat");
        sDeprecatedNameMap.put(BLACKLIST_KILL_PROCESS_NAME, "blacklist_kill_processes.dat");
        sDeprecatedNameMap.put(WHITELIST_KILL_PROCESS_NAME, "whitelist_kill_process.dat");
        sDeprecatedNameMap.put(WHITELIST_AUTO_BOOT_NAME, "whitelist_auto_root.dat");
        sDeprecatedNameMap.put(WHITELIST_PERMISSION_CONTROL_NAME, "whitelist_permission_control.dat");
        sDeprecatedNameMap.put(CAMERA_SOFTWARE_LIST_NAME, "camera_software_list.dat");
        sDeprecatedNameMap.put(DEEPCLEAN_SOFTWARE_LIST_NAME, "deepclean_software_list.dat");
        sDeprecatedNameMap.put(DEEPCLEAN_SOFT_PATH_LIST_NAME, "deepclean_soft_path_list.dat");
        sDeprecatedNameMap.put(PERMIS_MONITOR_LIST_NAME, "permis_monitor_list.dat");
        sDeprecatedNameMap.put(NOTKILLLIST_KILL_PROCESSES_NAME, "notkilllist_kill_processes.dat");
        sDeprecatedNameMap.put(ADBLOCKING_REPORT_WHITE_NAME, "adblocking_report_white.dat");
        sDeprecatedNameMap.put(DEEP_CLEAN_MEDIA_SRC_PATH_NAME, "deep_clean_media_src_path.dat");
        sDeprecatedNameMap.put(NUM_MARK_NAME, "mark_v1.sdb");
        sDeprecatedNameMap.put(VIRUS_BASE_NAME, "qv_base.amf");
        sDeprecatedNameMap.put(SYSTEM_SCAN_CONFIG_NAME, "system_scan.dat");
        sDeprecatedNameMap.put(PAY_LIST_NAME, "pay_list.dat");
        sDeprecatedNameMap.put(DEEP_CLEAN_WHITE_LIST_PATH_NAME, "deep_clean_white_list.dat");
        sDeprecatedNameMap.put(CAMERA_SOFTWARE_NEW_LIST_PATH_NAME, "camera_software_new_list.dat");
        sDeprecatedNameMap.put(ROOT_PHONE_WHITE_LIST_PATH_NAME, "root_phone_white_list.dat");
        sDeprecatedNameMap.put(H_LIST_NAME, "h_list.dat");
        sDeprecatedNameMap.put(ADB_DES_LIST_NAME, "adb_des_list.dat");
        sDeprecatedNameMap.put(DEEPCLEAN_SOFT_SCAN_RULE_NAME, "deepclean_soft_scan_rule.dat");
        sDeprecatedNameMap.put(DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2_SDK, "deepclean_sdcard_scan_rule.dat");
        sDeprecatedNameMap.put(PROCESSMANAGER_WHITE_LIST_NAME, "processmanager_white_list.dat");
        sDeprecatedNameMap.put(TMSLITE_COMMISION_LIST_NAME, "tmslite_commision_list.dat");
    }

    private UpdateConfig() {
    }

    private static String C(long j) {
        return (2 == j || UPDATA_FLAG_NUM_MARK == j || UPDATE_FLAG_YELLOW_PAGEV2_Large == j) ? ".sdb" : 4 == j ? ".sys" : (UPDATE_FLAG_VIRUS_BASE == j || UPDATE_FLAG_VIRUS_BASE_ENG == j) ? ".amf" : ".dat";
    }

    public static int getFileIdByFileName(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf <= 0) {
            return 0;
        }
        try {
            return Integer.parseInt(str.substring(0, lastIndexOf));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getFileIdByFlag(long j) {
        for (int i = 0; i < UPDATE_FLAGS.length; i++) {
            if (j == UPDATE_FLAGS[i]) {
                return FILE_IDS[i];
            }
        }
        return 0;
    }

    public static String getFileNameByFileId(int i) {
        for (int i2 = 0; i2 < FILE_IDS.length; i2++) {
            if (i == FILE_IDS[i2]) {
                return FILE_NAMES[i2];
            }
        }
        return null;
    }

    public static String getFileNameByFlag(long j) {
        for (int i = 0; i < UPDATE_FLAGS.length; i++) {
            if (j == UPDATE_FLAGS[i]) {
                return FILE_NAMES[i];
            }
        }
        return null;
    }

    public static String getFileNameIdByFlag(long j) {
        for (int i = 0; i < UPDATE_FLAGS.length; i++) {
            if (j == UPDATE_FLAGS[i]) {
                return Integer.toString(FILE_IDS[i]) + C(j);
            }
        }
        return null;
    }

    public static long getFlagByFileId(int i) {
        for (int i2 = 0; i2 < FILE_IDS.length; i2++) {
            if (i == FILE_IDS[i2]) {
                return UPDATE_FLAGS[i2];
            }
        }
        return 0;
    }

    public static long getFlagByFileName(String str) {
        for (int i = 0; i < FILE_NAMES.length; i++) {
            if (str.equals(FILE_NAMES[i])) {
                return UPDATE_FLAGS[i];
            }
        }
        return -1;
    }

    public static int getLargeMarkFileId() {
        return 40459;
    }

    public static String getLargeMarkFileName() {
        return JN;
    }

    public static String intToString(int i) {
        try {
            return Integer.toString(i);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isUpdatableAssetFile(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (Object equals : FILE_NAMES) {
            if (str.equals(equals)) {
                return true;
            }
        }
        return false;
    }

    public static long prepareCheckFlag(long -l_2_J) {
        if ((UPDATE_FLAG_ALL & -l_2_J) == 0) {
            return (UPDATE_FLAG_APP_LIST & -l_2_J) != 0 ? ((((((((((((((((((((((((512 | -l_2_J) | UPDATE_FLAG_WHITELIST_UNUSUAL) | UPDATE_FLAG_PRIVACYLOCKLIST_USUAL) | UPDATE_FLAG_BLACKLIST_PROCESS) | UPDATE_FLAG_WHITELIST_PROCESS) | UPDATE_FLAG_WHITELIST_AUTO_BOOT) | UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL) | UPDATE_FLAG_CAMERA_SOFTWARE_LIST) | UPDATE_FLAG_DEEPCLEAN_SOFTWARE_LIST) | UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST) | UPDATE_FLAG_PERMIS_MONITOR_LIST) | UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES) | UPDATE_ADBLOCKING_REPORT_WHITE_LIST) | UPDATE_FLAG_DEEP_CLEAN_MEDIA_SRC_PATH) | 4294967296L) | UPDATE_FLAG_VIRUSKILLER_CLOUDSCAN_WHITE) | UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST) | UPDATE_FLAG_DEEP_CLEAN_WHITE_LIST) | UPDATE_FLAG_YELLOW_PAGEV2_Large) | UPDATE_FLAG_CAMERA_SOFTWARE_NEW_LIST) | UPDATE_FLAG_ROOT_PHONE_WHITE_LIST) | UPDATE_FLAG_TMSLITE_COMMISION_LIST) | UPDATE_FLAG_GAME_SPEED_UP) | UPDATE_FLAG_DEEP_CLEAN_APPGROUP_DESC) | Long.MIN_VALUE : -l_2_J;
        } else {
            -l_2_J = 0;
            for (long j : UPDATE_FLAGS) {
                -l_2_J |= j;
            }
            return -l_2_J;
        }
    }
}
