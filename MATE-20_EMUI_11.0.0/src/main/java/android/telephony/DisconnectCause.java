package android.telephony;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.provider.CalendarContract;

@SystemApi
public final class DisconnectCause {
    public static final int ACCESS_INFORMATION_DISCARDED = 1015;
    public static final int ALREADY_DIALING = 72;
    public static final int ANSWERED_ELSEWHERE = 52;
    public static final int BEARER_CAPABILITY_NOT_AUTHORIZED = 1004;
    public static final int BEARER_CAPABILITY_UNAVAILABLE = 1021;
    public static final int BEARER_SERVICE_NOT_IMPLEMENTED = 1023;
    public static final int BUSY = 4;
    public static final int CALLING_DISABLED = 74;
    public static final int CALL_BARRED = 20;
    public static final int CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE = 1047;
    public static final int CALL_DROP_WIFI_BACKHAUL_CONGESTION = 1048;
    public static final int CALL_FAIL_DESTINATION_OUT_OF_ORDER = 1003;
    public static final int CALL_FAIL_NO_ANSWER_FROM_USER = 1002;
    public static final int CALL_FAIL_NO_USER_RESPONDING = 1046;
    public static final int CALL_PULLED = 51;
    public static final int CALL_REJECTED = 1006;
    public static final int CANT_CALL_WHILE_RINGING = 73;
    public static final int CDMA_ACCESS_BLOCKED = 35;
    public static final int CDMA_ACCESS_FAILURE = 32;
    public static final int CDMA_ALREADY_ACTIVATED = 49;
    public static final int CDMA_CALL_LOST = 41;
    public static final int CDMA_DROP = 27;
    public static final int CDMA_INTERCEPT = 28;
    public static final int CDMA_LOCKED_UNTIL_POWER_CYCLE = 26;
    public static final int CDMA_NOT_EMERGENCY = 34;
    public static final int CDMA_PREEMPTED = 33;
    public static final int CDMA_REORDER = 29;
    public static final int CDMA_RETRY_ORDER = 31;
    public static final int CDMA_SO_REJECT = 30;
    public static final int CHANNEL_UNACCEPTABLE = 1005;
    public static final int CONDITIONAL_IE_ERROR = 1036;
    public static final int CONGESTION = 5;
    public static final int CS_RESTRICTED = 22;
    public static final int CS_RESTRICTED_EMERGENCY = 24;
    public static final int CS_RESTRICTED_NORMAL = 23;
    public static final int DATA_DISABLED = 54;
    public static final int DATA_LIMIT_REACHED = 55;
    public static final int DIALED_CALL_FORWARDING_WHILE_ROAMING = 57;
    public static final int DIALED_MMI = 39;
    public static final int DIAL_LOW_BATTERY = 62;
    public static final int DIAL_MODIFIED_TO_DIAL = 48;
    public static final int DIAL_MODIFIED_TO_DIAL_VIDEO = 66;
    public static final int DIAL_MODIFIED_TO_SS = 47;
    public static final int DIAL_MODIFIED_TO_USSD = 46;
    public static final int DIAL_VIDEO_MODIFIED_TO_DIAL = 69;
    public static final int DIAL_VIDEO_MODIFIED_TO_DIAL_VIDEO = 70;
    public static final int DIAL_VIDEO_MODIFIED_TO_SS = 67;
    public static final int DIAL_VIDEO_MODIFIED_TO_USSD = 68;
    public static final int EMERGENCY_CALL_CS_REDIAL = 500;
    public static final int EMERGENCY_ONLY = 37;
    public static final int EMERGENCY_PERM_FAILURE = 64;
    public static final int EMERGENCY_TEMP_FAILURE = 63;
    public static final int ERROR_UNSPECIFIED = 36;
    public static final int EXITED_ECM = 42;
    public static final int FACILITY_REJECTED = 1009;
    public static final int FDN_BLOCKED = 21;
    public static final int HO_NOT_FEASIBLE = 1001;
    public static final int HUAWEI_BASE_VALUE = 1000;
    public static final int ICC_ERROR = 19;
    public static final int IMEI_NOT_ACCEPTED = 58;
    public static final int IMS_ACCESS_BLOCKED = 60;
    public static final int IMS_MERGED_SUCCESSFULLY = 45;
    public static final int IMS_SIP_ALTERNATE_EMERGENCY_CALL = 71;
    public static final int INCOMING_CALLS_BARRED_WITHIN_CUG = 1020;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int INCOMING_REJECTED_BY_REMOTE = 1049;
    public static final int INCOMPATIBLE_DESTINATION = 1029;
    public static final int INFORMATION_ELEMENT_NON_EXISTENT = 1035;
    public static final int INTERWORKING_UNSPECIFIED = 1040;
    public static final int INVALID_CREDENTIALS = 10;
    public static final int INVALID_MANDATORY_INFORMATION = 1032;
    public static final int INVALID_NUMBER = 7;
    public static final int INVALID_TRANSACTION_IDENTIFIER = 1027;
    public static final int INVALID_TRANSIT_NW_SELECTION = 1030;
    public static final int LIMIT_EXCEEDED = 15;
    public static final int LOCAL = 3;
    public static final int LOST_SIGNAL = 14;
    public static final int LOW_BATTERY = 61;
    public static final int MAXIMUM_NUMBER_OF_CALLS_REACHED = 53;
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 1037;
    public static final int MESSAGE_TYPE_NON_IMPLEMENTED = 1033;
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 1034;
    public static final int MMI = 6;
    public static final int NETWORK_OUT_OF_ORDER = 1012;
    public static final int NORMAL = 2;
    public static final int NORMAL_UNSPECIFIED = 65;
    public static final int NOT_DISCONNECTED = 0;
    public static final int NOT_VALID = -1;
    public static final int NO_CIRCUIT_AVAIL = 1043;
    public static final int NO_PHONE_NUMBER_SUPPLIED = 38;
    public static final int NO_ROUTE_TO_DESTINAON = 1044;
    public static final int NUMBER_CHANGED = 1007;
    public static final int NUMBER_UNREACHABLE = 8;
    public static final int ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE = 1025;
    public static final int OPERATOR_DETERMINED_BARRING = 1045;
    public static final int OTASP_PROVISIONING_IN_PROCESS = 76;
    public static final int OUTGOING_CANCELED = 44;
    public static final int OUTGOING_FAILURE = 43;
    public static final int OUT_OF_NETWORK = 11;
    public static final int OUT_OF_SERVICE = 18;
    public static final int POWER_OFF = 17;
    public static final int PREEMPTION = 1008;
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 1039;
    public static final int QOS_UNAVAILABLE = 1018;
    public static final int RECOVERY_ON_TIMER_EXPIRED = 1038;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 1016;
    public static final int REQUESTED_FACILITY_NOT_IMPLEMENTED = 1024;
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 1019;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 1017;
    public static final int RESP_TO_STATUS_ENQUIRY = 1010;
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 1031;
    public static final int SERVER_ERROR = 12;
    public static final int SERVER_UNREACHABLE = 9;
    public static final int SERVICE_OPTION_NOT_AVAILABLE = 1022;
    public static final int SERVICE_OR_OPTION_NOT_IMPLEMENTED = 1026;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 1014;
    public static final int TEMPORARY_FAILURE = 1013;
    public static final int TIMED_OUT = 13;
    public static final int TOO_MANY_ONGOING_CALLS = 75;
    public static final int UNOBTAINABLE_NUMBER = 25;
    public static final int USER_NOT_MEMBER_OF_CUG = 1028;
    public static final int VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED = 50;
    public static final int VOICEMAIL_NUMBER_MISSING = 40;
    public static final int WIFI_LOST = 59;

    private DisconnectCause() {
    }

    @UnsupportedAppUsage
    public static String toString(int cause) {
        switch (cause) {
            case 0:
                return "NOT_DISCONNECTED";
            case 1:
                return "INCOMING_MISSED";
            case 2:
                return "NORMAL";
            case 3:
                return CalendarContract.ACCOUNT_TYPE_LOCAL;
            case 4:
                return "BUSY";
            case 5:
                return "CONGESTION";
            case 6:
            case 56:
            default:
                return toHwCauseStr(cause);
            case 7:
                return "INVALID_NUMBER";
            case 8:
                return "NUMBER_UNREACHABLE";
            case 9:
                return "SERVER_UNREACHABLE";
            case 10:
                return "INVALID_CREDENTIALS";
            case 11:
                return "OUT_OF_NETWORK";
            case 12:
                return "SERVER_ERROR";
            case 13:
                return "TIMED_OUT";
            case 14:
                return "LOST_SIGNAL";
            case 15:
                return "LIMIT_EXCEEDED";
            case 16:
                return "INCOMING_REJECTED";
            case 17:
                return "POWER_OFF";
            case 18:
                return "OUT_OF_SERVICE";
            case 19:
                return "ICC_ERROR";
            case 20:
                return "CALL_BARRED";
            case 21:
                return "FDN_BLOCKED";
            case 22:
                return "CS_RESTRICTED";
            case 23:
                return "CS_RESTRICTED_NORMAL";
            case 24:
                return "CS_RESTRICTED_EMERGENCY";
            case 25:
                return "UNOBTAINABLE_NUMBER";
            case 26:
                return "CDMA_LOCKED_UNTIL_POWER_CYCLE";
            case 27:
                return "CDMA_DROP";
            case 28:
                return "CDMA_INTERCEPT";
            case 29:
                return "CDMA_REORDER";
            case 30:
                return "CDMA_SO_REJECT";
            case 31:
                return "CDMA_RETRY_ORDER";
            case 32:
                return "CDMA_ACCESS_FAILURE";
            case 33:
                return "CDMA_PREEMPTED";
            case 34:
                return "CDMA_NOT_EMERGENCY";
            case 35:
                return "CDMA_ACCESS_BLOCKED";
            case 36:
                return "ERROR_UNSPECIFIED";
            case 37:
                return "EMERGENCY_ONLY";
            case 38:
                return "NO_PHONE_NUMBER_SUPPLIED";
            case 39:
                return "DIALED_MMI";
            case 40:
                return "VOICEMAIL_NUMBER_MISSING";
            case 41:
                return "CDMA_CALL_LOST";
            case 42:
                return "EXITED_ECM";
            case 43:
                return "OUTGOING_FAILURE";
            case 44:
                return "OUTGOING_CANCELED";
            case 45:
                return "IMS_MERGED_SUCCESSFULLY";
            case 46:
                return "DIAL_MODIFIED_TO_USSD";
            case 47:
                return "DIAL_MODIFIED_TO_SS";
            case 48:
                return "DIAL_MODIFIED_TO_DIAL";
            case 49:
                return "CDMA_ALREADY_ACTIVATED";
            case 50:
                return "VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED";
            case 51:
                return "CALL_PULLED";
            case 52:
                return "ANSWERED_ELSEWHERE";
            case 53:
                return "MAXIMUM_NUMER_OF_CALLS_REACHED";
            case 54:
                return "DATA_DISABLED";
            case 55:
                return "DATA_LIMIT_REACHED";
            case 57:
                return "DIALED_CALL_FORWARDING_WHILE_ROAMING";
            case 58:
                return "IMEI_NOT_ACCEPTED";
            case 59:
                return "WIFI_LOST";
            case 60:
                return "IMS_ACCESS_BLOCKED";
            case 61:
                return "LOW_BATTERY";
            case 62:
                return "DIAL_LOW_BATTERY";
            case 63:
                return "EMERGENCY_TEMP_FAILURE";
            case 64:
                return "EMERGENCY_PERM_FAILURE";
            case 65:
                return "NORMAL_UNSPECIFIED";
            case 66:
                return "DIAL_MODIFIED_TO_DIAL_VIDEO";
            case 67:
                return "DIAL_VIDEO_MODIFIED_TO_SS";
            case 68:
                return "DIAL_VIDEO_MODIFIED_TO_USSD";
            case 69:
                return "DIAL_VIDEO_MODIFIED_TO_DIAL";
            case 70:
                return "DIAL_VIDEO_MODIFIED_TO_DIAL_VIDEO";
            case 71:
                return "IMS_SIP_ALTERNATE_EMERGENCY_CALL";
        }
    }

    private static String toHwCauseStr(int cause) {
        if (cause == 1001) {
            return "HO_NOT_FEASIBLE";
        }
        switch (cause) {
            case 72:
                return "ALREADY_DIALING";
            case 73:
                return "CANT_CALL_WHILE_RINGING";
            case 74:
                return "CALLING_DISABLED";
            case 75:
                return "TOO_MANY_ONGOING_CALLS";
            case 76:
                return "OTASP_PROVISIONING_IN_PROCESS";
            default:
                switch (cause) {
                    case 1047:
                        return "CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE";
                    case 1048:
                        return "CALL_DROP_WIFI_BACKHAUL_CONGESTION";
                    case 1049:
                        return "INCOMING_REJECTED_BY_REMOTE";
                    default:
                        return "INVALID: " + cause;
                }
        }
    }
}
