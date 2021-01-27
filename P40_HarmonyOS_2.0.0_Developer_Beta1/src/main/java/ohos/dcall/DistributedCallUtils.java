package ohos.dcall;

import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.PacMap;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;

public class DistributedCallUtils {
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_OFFHOOK = 2;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CALL_STATE_UNKNOWN = -1;
    public static final String DISTRIBUTED_CALL_ABILITY_DESCRIPTOR = "OHOS.Dcall.DistributedCallAbility";
    public static final int LOG_ID_DCALL = 218111744;
    public static final int MAX_REJECT_MESSAGE = 4;
    public static final int MSG_ADD_CALL_OBSERVER = 2006;
    public static final int MSG_ANSWER_CALL = 1;
    public static final int MSG_CALL_BASE = 2000;
    public static final int MSG_CHECK_VIDEO_CALLING_ENABLED = 2009;
    public static final int MSG_DCALL_ABILITY_BASE = 0;
    public static final int MSG_DCALL_BASE = 0;
    public static final int MSG_DIAL = 2002;
    public static final int MSG_DIAL_WITH_EXTRAS = 14;
    public static final int MSG_DISCONNECT_CALL = 2;
    public static final int MSG_DISPLAY_CALL_SCREEN = 2003;
    public static final int MSG_DISTRIBUTE_CALL_EVENT = 13;
    public static final int MSG_GET_CALL_STATE = 2005;
    public static final int MSG_GET_PREDEFINED_REJECT_MESSAGE = 12;
    public static final int MSG_HAS_CALL = 2001;
    public static final int MSG_HOLD_CALL = 10;
    public static final int MSG_INIT_DIAL_ENV = 15;
    public static final int MSG_INPUT_DIALER_SPECIAL_CODE = 2010;
    public static final int MSG_IS_NEW_CALL_ALLOWED = 7;
    public static final int MSG_MUTE_RINGER = 2004;
    public static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 1;
    public static final int MSG_ON_CALL_COMPLETED = 9;
    public static final int MSG_ON_CALL_CREATED = 2;
    public static final int MSG_ON_CALL_DELETED = 3;
    public static final int MSG_ON_CALL_EVENT_CHANGED = 10;
    public static final int MSG_ON_INFO_CHANGED = 7;
    public static final int MSG_ON_IS_NEW_CALL_ALLOWED_CHANGED = 4;
    public static final int MSG_ON_POST_DIAL_WAIT = 8;
    public static final int MSG_ON_RINGTONE_MUTED = 5;
    public static final int MSG_ON_STATE_CHANGED = 6;
    public static final int MSG_POST_DIAL_DTMF_CONTINUE = 5;
    public static final int MSG_REJECT_CALL = 6;
    public static final int MSG_REMOVE_CALL_OBSERVER = 2007;
    public static final int MSG_SET_AUDIO_DEVICE = 9;
    public static final int MSG_SET_MUTED = 8;
    public static final int MSG_START_DTMF_TONE = 3;
    public static final int MSG_STOP_DTMF_TONE = 4;
    public static final int MSG_UNHOLD_CALL = 11;
    private static final String OHOS_ALREADY_DIALING = "ALREADY_DIALING";
    private static final String OHOS_CALLING_DISABLED = "CALLING_DISABLED";
    private static final String OHOS_CALL_BARRED = "CALL_BARRED";
    private static final String OHOS_CALL_PULLED = "CALL_PULLED";
    private static final String OHOS_CANT_CALL_WHILE_RINGING = "CANT_CALL_WHILE_RINGING";
    private static final String OHOS_CDMA_ALREADY_ACTIVATED = "CDMA_ALREADY_ACTIVATED";
    private static final String OHOS_CDMA_NOT_EMERGENCY = "CDMA_NOT_EMERGENCY";
    private static final String OHOS_CDMA_REORDER = "CDMA_REORDER";
    private static final String OHOS_CS_RESTRICTED = "CS_RESTRICTED";
    private static final String OHOS_CS_RESTRICTED_EMERGENCY = "CS_RESTRICTED_EMERGENCY";
    private static final String OHOS_CS_RESTRICTED_NORMAL = "CS_RESTRICTED_NORMAL";
    private static final String OHOS_DATA_DISABLED = "DATA_DISABLED";
    private static final String OHOS_DATA_LIMIT_REACHED = "DATA_LIMIT_REACHED";
    private static final String OHOS_DIALED_CALL_FORWARDING_WHILE_ROAMING = "DIALED_CALL_FORWARDING_WHILE_ROAMING";
    private static final String OHOS_DIAL_LOW_BATTERY = "DIAL_LOW_BATTERY";
    private static final String OHOS_DIAL_MODIFIED_TO_DIAL = "DIAL_MODIFIED_TO_DIAL";
    private static final String OHOS_DIAL_MODIFIED_TO_DIAL_VIDEO = "DIAL_MODIFIED_TO_DIAL_VIDEO";
    private static final String OHOS_DIAL_MODIFIED_TO_SS = "DIAL_MODIFIED_TO_SS";
    private static final String OHOS_DIAL_MODIFIED_TO_USSD = "DIAL_MODIFIED_TO_USSD";
    private static final String OHOS_DIAL_VIDEO_MODIFIED_TO_DIAL = "DIAL_VIDEO_MODIFIED_TO_DIAL";
    private static final String OHOS_DIAL_VIDEO_MODIFIED_TO_DIAL_VIDEO = "DIAL_VIDEO_MODIFIED_TO_DIAL_VIDEO";
    private static final String OHOS_DIAL_VIDEO_MODIFIED_TO_SS = "DIAL_VIDEO_MODIFIED_TO_SS";
    private static final String OHOS_DIAL_VIDEO_MODIFIED_TO_USSD = "DIAL_VIDEO_MODIFIED_TO_USSD";
    public static final int OHOS_DISCONNECT_CODE_ALL_VOICE_CALL_NOT_ALLOW = 27;
    public static final int OHOS_DISCONNECT_CODE_CALL_BARRED_FUCTION_OPEN = 46;
    public static final int OHOS_DISCONNECT_CODE_CALL_FUNCTION_DISABLED = 32;
    public static final int OHOS_DISCONNECT_CODE_CANNOT_MODIFY_CALL_FORWARDING_WHILE_ROAMING = 36;
    public static final int OHOS_DISCONNECT_CODE_CDMA_NOT_SUPPORTED_NUMBER = 47;
    public static final int OHOS_DISCONNECT_CODE_CELLULAR_DATA_DISABLED = 38;
    public static final int OHOS_DISCONNECT_CODE_DIALING = 33;
    public static final int OHOS_DISCONNECT_CODE_EMERGENCY_CALL_NOT_ALLOW = 28;
    public static final int OHOS_DISCONNECT_CODE_EMERGENCY_ONLY = 25;
    public static final int OHOS_DISCONNECT_CODE_FDN_NUMBER_NOT_ALLOW = 49;
    public static final int OHOS_DISCONNECT_CODE_IMEI_REJECTED = 35;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_CALL_MISSED = 62;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_CALL_REJECTED = 63;
    public static final int OHOS_DISCONNECT_CODE_INCOMING_REJECTED_BY_REMOTE = 64;
    public static final int OHOS_DISCONNECT_CODE_INVALID_PHONE_NUMBER = 23;
    public static final int OHOS_DISCONNECT_CODE_LOCAL_HANGUP = 60;
    public static final int OHOS_DISCONNECT_CODE_LOW_BATTERY = 59;
    public static final int OHOS_DISCONNECT_CODE_LOW_BATTERY_DIAL_FAILED = 45;
    public static final int OHOS_DISCONNECT_CODE_MORE_THAN_TWO_CALLS = 31;
    public static final int OHOS_DISCONNECT_CODE_NETWORK_ACCESS_BLOCKED = 26;
    public static final int OHOS_DISCONNECT_CODE_NETWORK_BUSY = 48;
    public static final int OHOS_DISCONNECT_CODE_NORMAL_VOICE_CALL_NOT_ALLOW = 29;
    public static final int OHOS_DISCONNECT_CODE_NOT_EMERGENCY = 44;
    public static final int OHOS_DISCONNECT_CODE_OTASP_CONFIGURING = 30;
    public static final int OHOS_DISCONNECT_CODE_OUTGOING_CALL_FAILED = 58;
    public static final int OHOS_DISCONNECT_CODE_OUTGOING_USER_CANCELED = 39;
    public static final int OHOS_DISCONNECT_CODE_OUT_OF_SERVICE = 24;
    public static final int OHOS_DISCONNECT_CODE_RADIO_OFF = 21;
    public static final int OHOS_DISCONNECT_CODE_REACHED_CELLULAR_DATA_LIMIT = 37;
    public static final int OHOS_DISCONNECT_CODE_REACHED_MAX_CALL_NUMBER = 40;
    public static final int OHOS_DISCONNECT_CODE_REMOTE_HANGUP = 61;
    public static final int OHOS_DISCONNECT_CODE_RINGING = 22;
    public static final int OHOS_DISCONNECT_CODE_SWITCH_TO_ANOTHER_DEVICE = 41;
    public static final int OHOS_DISCONNECT_CODE_UNKNOWN = -1;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_NUMBER_TO_DIAL = 57;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_SS = 54;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_USSD = 55;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_CHANGED_TO_VOICE_CALL = 56;
    public static final int OHOS_DISCONNECT_CODE_VIDEO_CALL_NOT_ALLOWED_UNDER_TTY_MODE = 42;
    public static final int OHOS_DISCONNECT_CODE_VOICEMAIL_NO_PHONE_NUMBER = 43;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_NUMBER_TO_DIAL = 52;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_SS = 51;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_USSD = 50;
    public static final int OHOS_DISCONNECT_CODE_VOICE_CALL_CHANGED_TO_VIDEO_CALL = 53;
    public static final int OHOS_DISCONNECT_CODE_WIFI_INTERRUPTED = 34;
    private static final String OHOS_EMERGENCY_ONLY = "EMERGENCY_ONLY";
    private static final String OHOS_FDN_BLOCKED = "FDN_BLOCKED";
    private static final String OHOS_IMEI_NOT_ACCEPTED = "IMEI_NOT_ACCEPTED";
    private static final String OHOS_IMS_ACCESS_BLOCKED = "IMS_ACCESS_BLOCKED";
    private static final String OHOS_INCOMING_MISSED = "INCOMING_MISSED";
    private static final String OHOS_INCOMING_REJECTED = "INCOMING_REJECTED";
    private static final String OHOS_INCOMING_REJECTED_BY_REMOTE = "INCOMING_REJECTED_BY_REMOTE";
    private static final String OHOS_LOCAL = "LOCAL";
    private static final String OHOS_LOW_BATTERY = "LOW_BATTERY";
    private static final String OHOS_MAXIMUM_NUMBER_OF_CALLS_REACHED = "MAXIMUM_NUMBER_OF_CALLS_REACHED";
    private static final String OHOS_NORMAL = "NORMAL";
    private static final String OHOS_NORMAL_UNSPECIFIED = "NORMAL_UNSPECIFIED";
    private static final String OHOS_NO_PHONE_NUMBER_SUPPLIED = "NO_PHONE_NUMBER_SUPPLIED";
    private static final String OHOS_OTASP_PROVISIONING_IN_PROCESS = "OTASP_PROVISIONING_IN_PROCESS";
    private static final String OHOS_OUTGOING_CANCELED = "OUTGOING_CANCELED";
    private static final String OHOS_OUTGOING_FAILURE = "OUTGOING_FAILURE";
    private static final String OHOS_OUT_OF_SERVICE = "OUT_OF_SERVICE";
    private static final String OHOS_POWER_OFF = "POWER_OFF";
    private static final String OHOS_TOO_MANY_ONGOING_CALLS = "TOO_MANY_ONGOING_CALLS";
    private static final String OHOS_UNKNOWN = "OHOS_UNKOWN";
    private static final String OHOS_VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED = "VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED";
    private static final String OHOS_VOICEMAIL_NUMBER_MISSING = "VOICEMAIL_NUMBER_MISSING";
    private static final String OHOS_WIFI_LOST = "WIFI_LOST";
    static final String PRE_CONNECT_ABILITY = "PRE_CONNECT_ABILITY";
    static final String PRE_CONNECT_ABILITY_CALL_ABILITY_NAME = "CALL_ABILITY_NAME";
    static final String PRE_CONNECT_ABILITY_CALL_COMPONENT_NAME = "CALL_COMPONENT_NAME";
    static final String PRE_CONNECT_ABILITY_CALL_TYPE = "CALL_TYPE";
    static final String PRE_ON_CALL_CREATED = "PRE_ON_CALL_CREATED";
    static final String PRE_ON_CALL_CREATED_CALL_ABILITY_NAME = "CALL_ABILITY_NAME";
    static final String PRE_ON_CALL_CREATED_CALL_COMPONENT_NAME = "CALL_COMPONENT_NAME";
    static final String PRE_ON_CALL_CREATED_CALL_TYPE = "CALL_TYPE";
    public static final int RESULT_ERROR = -1;
    public static final int RESULT_PERMISSION_DENY = -2;
    public static final int RESULT_SUCCESS = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LOG_ID_DCALL, "DistributedCallUtils");

    public static final String msgCodeToString(int i) {
        switch (i) {
            case 1:
                return "MSG_ON_CALL_AUDIO_STATE_CHANGED";
            case 2:
                return "MSG_ON_CALL_CREATED";
            case 3:
                return "MSG_ON_CALL_DELETED";
            case 4:
                return "MSG_ON_IS_NEW_CALL_ALLOWED_CHANGED";
            case 5:
                return "MSG_ON_RINGTONE_MUTED";
            case 6:
                return "MSG_ON_STATE_CHANGED";
            case 7:
                return "MSG_ON_INFO_CHANGED";
            case 8:
                return "MSG_ON_POST_DIAL_WAIT";
            case 9:
                return "MSG_ON_CALL_COMPLETED";
            case 10:
                return "MSG_ON_CALL_EVENT_CHANGED";
            default:
                return "Unsupported Msg";
        }
    }

    public static int toOhosDisconnectCode(String str) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        int ohosReStrictedDisconnectCode = toOhosReStrictedDisconnectCode(str);
        if (ohosReStrictedDisconnectCode != -1) {
            return ohosReStrictedDisconnectCode;
        }
        int ohosErrorDisconnectCode = toOhosErrorDisconnectCode(str);
        if (ohosErrorDisconnectCode != -1) {
            return ohosErrorDisconnectCode;
        }
        return toOhosOtherDisconnectCode(str);
    }

    private static int toOhosOtherDisconnectCode(String str) {
        if (str.endsWith(OHOS_OUTGOING_CANCELED)) {
            return 39;
        }
        if (str.endsWith(OHOS_CALL_PULLED)) {
            return 41;
        }
        if (str.endsWith(OHOS_DIAL_MODIFIED_TO_USSD)) {
            return 50;
        }
        if (str.endsWith(OHOS_DIAL_MODIFIED_TO_SS)) {
            return 51;
        }
        if (str.endsWith(OHOS_DIAL_MODIFIED_TO_DIAL)) {
            return 52;
        }
        if (str.endsWith(OHOS_DIAL_MODIFIED_TO_DIAL_VIDEO)) {
            return 53;
        }
        if (str.endsWith(OHOS_DIAL_VIDEO_MODIFIED_TO_SS)) {
            return 54;
        }
        if (str.endsWith(OHOS_DIAL_VIDEO_MODIFIED_TO_USSD)) {
            return 55;
        }
        if (str.endsWith(OHOS_DIAL_VIDEO_MODIFIED_TO_DIAL)) {
            return 56;
        }
        if (str.endsWith(OHOS_DIAL_VIDEO_MODIFIED_TO_DIAL_VIDEO)) {
            return 57;
        }
        if (str.endsWith(OHOS_LOCAL)) {
            return 60;
        }
        if (str.endsWith(OHOS_NORMAL) || str.endsWith(OHOS_NORMAL_UNSPECIFIED)) {
            return 61;
        }
        if (str.endsWith(OHOS_INCOMING_MISSED)) {
            return 62;
        }
        if (str.endsWith(OHOS_INCOMING_REJECTED)) {
            return 63;
        }
        return str.endsWith(OHOS_INCOMING_REJECTED_BY_REMOTE) ? 64 : -1;
    }

    private static int toOhosReStrictedDisconnectCode(String str) {
        if (str.endsWith(OHOS_CALL_BARRED)) {
            return 46;
        }
        if (str.endsWith(OHOS_CDMA_NOT_EMERGENCY)) {
            return 44;
        }
        if (str.endsWith(OHOS_FDN_BLOCKED)) {
            return 49;
        }
        if (str.endsWith(OHOS_CS_RESTRICTED)) {
            return 27;
        }
        if (str.endsWith(OHOS_CS_RESTRICTED_EMERGENCY)) {
            return 28;
        }
        if (str.endsWith(OHOS_CS_RESTRICTED_NORMAL)) {
            return 29;
        }
        if (str.endsWith(OHOS_EMERGENCY_ONLY)) {
            return 25;
        }
        if (str.endsWith(OHOS_VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED)) {
            return 42;
        }
        if (str.endsWith(OHOS_POWER_OFF)) {
            return 21;
        }
        if (str.endsWith(OHOS_MAXIMUM_NUMBER_OF_CALLS_REACHED)) {
            return 40;
        }
        if (str.endsWith(OHOS_DATA_DISABLED)) {
            return 38;
        }
        if (str.endsWith(OHOS_DATA_LIMIT_REACHED)) {
            return 37;
        }
        if (str.endsWith(OHOS_DIALED_CALL_FORWARDING_WHILE_ROAMING)) {
            return 36;
        }
        if (str.endsWith(OHOS_IMEI_NOT_ACCEPTED)) {
            return 35;
        }
        if (str.endsWith(OHOS_WIFI_LOST)) {
            return 34;
        }
        return str.endsWith(OHOS_IMS_ACCESS_BLOCKED) ? 26 : -1;
    }

    private static int toOhosErrorDisconnectCode(String str) {
        if (str.endsWith(OHOS_CDMA_ALREADY_ACTIVATED)) {
            return 47;
        }
        if (str.endsWith(OHOS_CDMA_REORDER)) {
            return 48;
        }
        if (str.endsWith(OHOS_NO_PHONE_NUMBER_SUPPLIED)) {
            return 23;
        }
        if (str.endsWith(OHOS_OUTGOING_FAILURE)) {
            return 58;
        }
        if (str.endsWith(OHOS_OUT_OF_SERVICE)) {
            return 24;
        }
        if (str.endsWith(OHOS_DIAL_LOW_BATTERY)) {
            return 45;
        }
        if (str.endsWith(OHOS_LOW_BATTERY)) {
            return 59;
        }
        if (str.endsWith(OHOS_ALREADY_DIALING)) {
            return 33;
        }
        if (str.endsWith(OHOS_CANT_CALL_WHILE_RINGING)) {
            return 22;
        }
        if (str.endsWith(OHOS_CALLING_DISABLED)) {
            return 32;
        }
        if (str.endsWith(OHOS_TOO_MANY_ONGOING_CALLS)) {
            return 31;
        }
        if (str.endsWith(OHOS_OTASP_PROVISIONING_IN_PROCESS)) {
            return 30;
        }
        return str.endsWith(OHOS_VOICEMAIL_NUMBER_MISSING) ? 43 : -1;
    }

    static PacMap readPacMapFromParcel(Parcel parcel) {
        PacMap pacMap = new PacMap();
        if (parcel == null) {
            HiLog.error(TAG, "readPacMapFromParcel: parcel is null.", new Object[0]);
            return pacMap;
        }
        try {
            pacMap.putAll(convertToSpecificMap(parcel.readMap()));
        } catch (ParcelException unused) {
            HiLog.error(TAG, "readPacMapFromParcel: got ParcelException.", new Object[0]);
        }
        return pacMap;
    }

    static Map<String, Object> convertToSpecificMap(Map<?, ?> map) {
        HashMap hashMap = new HashMap();
        if (map == null) {
            HiLog.error(TAG, "convertToSpecificMap: sourceMap is null.", new Object[0]);
            return hashMap;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String) {
                hashMap.put((String) String.class.cast(entry.getKey()), entry.getValue());
            }
        }
        return hashMap;
    }
}
