package tmsdk.common.module.update;

import android.text.TextUtils;
import java.util.Map;

/* compiled from: Unknown */
public final class UpdateConfig {
    public static final String ADBLOCKING_REPORT_WHITE_NAME = null;
    public static final String ADB_DES_LIST_NAME = null;
    public static final String AMS_DATA = null;
    public static final String ANTITHEFT_WEB_SERVER_NUM = null;
    public static final String BLACKLIST_KILL_PROCESS_NAME = null;
    public static final String CAMERA_SOFTWARE_LIST_NAME = null;
    public static final String CAMERA_SOFTWARE_NEW_LIST_PATH_NAME = null;
    public static final String DEEPCLEAN_ONE_KEY_PATH_LIST_NAME = null;
    public static final String DEEPCLEAN_SDCARD_SCAN_RULE_NAME = null;
    public static final String DEEPCLEAN_SDCARD_SCAN_RULE_NAME_V2 = null;
    public static final String DEEPCLEAN_SOFTWARE_LIST_NAME = null;
    public static final String DEEPCLEAN_SOFT_PATH_LIST_NAME = null;
    public static final String DEEPCLEAN_SOFT_SCAN_RULE_NAME = null;
    public static final String DEEP_CLEAN_MEDIA_SRC_PATH_NAME = null;
    public static final String DEEP_CLEAN_WHITE_LIST_PATH_NAME = null;
    public static final String DeepCleanSdcardScanRuleWord = null;
    public static final String DeepCleanSoftScanRuleWord = null;
    public static final int[] FILE_IDS = null;
    public static final String[] FILE_NAMES = null;
    public static final String GAME_SPEED_UP_LIST_NAME = null;
    public static final String H_LIST_NAME = null;
    public static final String KingUserPermision = null;
    public static final String LOCATION_NAME = null;
    public static final String NOTKILLLIST_KILL_PROCESSES_NAME = null;
    public static final String NUM_MARK_LARGE50W_NAME = null;
    public static final String NUM_MARK_LARGE_NAME = null;
    public static final String NUM_MARK_NAME = null;
    public static final String PATCH_SUFIX = ".patch";
    public static final String PAY_LIST_NAME = null;
    public static final String PCLinkedSoftCommunicateFlow = null;
    public static final String PERMIS_MONITOR_LIST_NAME = null;
    public static final String POSEIDON = null;
    public static final String POSEIDONV2 = null;
    public static final String PRIVACYLOCKLIST_USUAL_NAME = null;
    public static final String PROCESSMANAGER_WHITE_LIST_NAME = null;
    public static final String QUICKPANEL_WIFIMANAGE = null;
    public static final String QUICK_PANEL_SUP = null;
    public static final String ROOT_PHONE_WHITE_LIST_PATH_NAME = null;
    public static final String RUBBISH_ENGLISH_TABLE_NAME = null;
    public static final String SMS_CHECKER_NAME = null;
    public static final String STEAL_ACCOUNT_LIST_NAME = null;
    public static final String SYSTEM_SCAN_CONFIG_NAME = null;
    public static final String SkinConfig = null;
    public static final String SoftLock_LockFunc = null;
    public static final String SpaceMangerList = null;
    public static final String TMSLITE_COMMISION_LIST_NAME = null;
    public static final String TRAFFIC_MONITOR_CONFIG_NAME = null;
    public static final String TRUST_URLS_NAME = null;
    public static final long UPDATA_FLAG_NUM_MARK = 536870912;
    public static final long UPDATE_ADBLOCKING_REPORT_WHITE_LIST = 134217728;
    public static final long[] UPDATE_FLAGS = null;
    public static final long UPDATE_FLAG_ADB_DES_LIST = 274877906944L;
    public static final long UPDATE_FLAG_ALL = 33554432;
    public static final long UPDATE_FLAG_AMS_DATA = 35184372088832L;
    public static final long UPDATE_FLAG_ANTITHEFT_WEB_SERVER_NUM = 17592186044416L;
    public static final long UPDATE_FLAG_APP_LIST = 67108864;
    public static final long UPDATE_FLAG_BLACKLIST_PROCESS = 8192;
    public static final long UPDATE_FLAG_CAMERA_SOFTWARE_LIST = 131072;
    public static final long UPDATE_FLAG_CAMERA_SOFTWARE_NEW_LIST = 34359738368L;
    public static final long UPDATE_FLAG_DEEPCLEAN_ONE_KEY_PATH_LIST = 2199023255552L;
    public static final long UPDATE_FLAG_DEEPCLEAN_SDCARD_SCAN_RULE = 1099511627776L;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFTWARE_LIST = 262144;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST = 524288;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST_V2 = 576460752303423488L;
    public static final long UPDATE_FLAG_DEEPCLEAN_SOFT_SCAN_RULE = 549755813888L;
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
    public static final long UPDATE_FLAG_NUMMARK50W_LARGE = Long.MIN_VALUE;
    public static final long UPDATE_FLAG_NUMMARK_LARGE = 288230376151711744L;
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
    public static final long UPDATE_FLAG_STEAL_ACCOUNT_LIST = 8589934592L;
    public static final long UPDATE_FLAG_SYSTEM_SCAN_CONFIG = 2147483648L;
    public static final long UPDATE_FLAG_SkinConfig = 4503599627370496L;
    public static final long UPDATE_FLAG_SoftLock_LockFunc = 9007199254740992L;
    public static final long UPDATE_FLAG_TMSLITE_COMMISION_LIST = 8796093022208L;
    public static final long UPDATE_FLAG_TRAFFIC_MONITOR_CONFIG = 32;
    public static final long UPDATE_FLAG_TRUST_URLS = 16;
    public static final long UPDATE_FLAG_VIRUS_BASE = 1073741824;
    public static final long UPDATE_FLAG_VIRUS_BASE_ENG = 2305843009213693952L;
    public static final long UPDATE_FLAG_WHITELIST_AUTO_BOOT = 32768;
    public static final long UPDATE_FLAG_WHITELIST_COMMON = 512;
    public static final long UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL = 65536;
    public static final long UPDATE_FLAG_WHITELIST_PROCESS = 16384;
    public static final long UPDATE_FLAG_WHITELIST_UNUSUAL = 1024;
    public static final long UPDATE_FLAG_WeixinTrashClean = 281474976710656L;
    public static final long UPDATE_FLAG_WeixinTrashCleanNew = 1152921504606846976L;
    public static final int UPDATE_TYPE_ENGINE_UPDATE = 1;
    public static final int UPDATE_TYPE_NORMAL_UPDATE = 0;
    public static final String VIRUS_BASE_EN_NAME = null;
    public static final String VIRUS_BASE_NAME = null;
    public static final String WHITELIST_AUTO_BOOT_NAME = null;
    public static final String WHITELIST_COMMON_NAME = null;
    public static final String WHITELIST_KILL_PROCESS_NAME = null;
    public static final String WHITELIST_PERMISSION_CONTROL_NAME = null;
    public static final String WHITELIST_UNUSUAL_NAME = null;
    public static final String WeixinTrashClean = null;
    public static final String WeixinTrashCleanNew = null;
    public static Map<String, String> sDeprecatedNameMap;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.update.UpdateConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.update.UpdateConfig.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.update.UpdateConfig.<clinit>():void");
    }

    private UpdateConfig() {
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
        for (int i = 0; i < UPDATE_FLAGS.length; i += UPDATE_TYPE_ENGINE_UPDATE) {
            if (j == UPDATE_FLAGS[i]) {
                return FILE_IDS[i];
            }
        }
        return 0;
    }

    public static String getFileNameByFileId(int i) {
        for (int i2 = 0; i2 < FILE_IDS.length; i2 += UPDATE_TYPE_ENGINE_UPDATE) {
            if (i == FILE_IDS[i2]) {
                return FILE_NAMES[i2];
            }
        }
        return null;
    }

    public static String getFileNameByFlag(long j) {
        for (int i = 0; i < UPDATE_FLAGS.length; i += UPDATE_TYPE_ENGINE_UPDATE) {
            if (j == UPDATE_FLAGS[i]) {
                return FILE_NAMES[i];
            }
        }
        return null;
    }

    public static String getFileNameIdByFlag(long j) {
        for (int i = 0; i < UPDATE_FLAGS.length; i += UPDATE_TYPE_ENGINE_UPDATE) {
            if (j == UPDATE_FLAGS[i]) {
                return Integer.toString(FILE_IDS[i]) + v(j);
            }
        }
        return null;
    }

    public static long getFlagByFileId(int i) {
        for (int i2 = 0; i2 < FILE_IDS.length; i2 += UPDATE_TYPE_ENGINE_UPDATE) {
            if (i == FILE_IDS[i2]) {
                return UPDATE_FLAGS[i2];
            }
        }
        return 0;
    }

    public static long getFlagByFileName(String str) {
        for (int i = 0; i < FILE_NAMES.length; i += UPDATE_TYPE_ENGINE_UPDATE) {
            if (str.equals(FILE_NAMES[i])) {
                return UPDATE_FLAGS[i];
            }
        }
        return -1;
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
        int length = FILE_NAMES.length;
        for (int i = 0; i < length; i += UPDATE_TYPE_ENGINE_UPDATE) {
            if (str.equals(FILE_NAMES[i])) {
                return true;
            }
        }
        return false;
    }

    public static long prepareCheckFlag(long j) {
        if ((UPDATE_FLAG_ALL & j) == 0) {
            return (UPDATE_FLAG_APP_LIST & j) != 0 ? ((((((((((((((((((((((UPDATE_FLAG_WHITELIST_COMMON | j) | UPDATE_FLAG_WHITELIST_UNUSUAL) | UPDATE_FLAG_PRIVACYLOCKLIST_USUAL) | UPDATE_FLAG_BLACKLIST_PROCESS) | UPDATE_FLAG_WHITELIST_PROCESS) | UPDATE_FLAG_WHITELIST_AUTO_BOOT) | UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL) | UPDATE_FLAG_CAMERA_SOFTWARE_LIST) | UPDATE_FLAG_DEEPCLEAN_SOFTWARE_LIST) | UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST) | UPDATE_FLAG_PERMIS_MONITOR_LIST) | UPDATE_FLAG_NOTKILLLIST_KILL_PROCESSES) | UPDATE_ADBLOCKING_REPORT_WHITE_LIST) | UPDATE_FLAG_DEEP_CLEAN_MEDIA_SRC_PATH) | UPDATE_FLAG_PAY_LIST) | UPDATE_FLAG_STEAL_ACCOUNT_LIST) | UPDATE_FLAG_PROCESSMANAGER_WHITE_LIST) | UPDATE_FLAG_DEEP_CLEAN_WHITE_LIST) | UPDATE_FLAG_WeixinTrashCleanNew) | UPDATE_FLAG_CAMERA_SOFTWARE_NEW_LIST) | UPDATE_FLAG_ROOT_PHONE_WHITE_LIST) | UPDATE_FLAG_TMSLITE_COMMISION_LIST) | UPDATE_FLAG_GAME_SPEED_UP : j;
        } else {
            long[] jArr = UPDATE_FLAGS;
            j = 0;
            for (int i = 0; i < jArr.length; i += UPDATE_TYPE_ENGINE_UPDATE) {
                j |= jArr[i];
            }
            return j;
        }
    }

    private static String v(long j) {
        return (UPDATE_FLAG_LOCATION == j || UPDATA_FLAG_NUM_MARK == j || UPDATE_FLAG_NUMMARK50W_LARGE == j || UPDATE_FLAG_NUMMARK_LARGE == j) ? ".sdb" : UPDATE_FLAG_SMS_CHECKER == j ? ".sys" : (UPDATE_FLAG_VIRUS_BASE == j || UPDATE_FLAG_VIRUS_BASE_ENG == j) ? ".amf" : ".dat";
    }
}
