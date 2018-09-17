package android.telephony;

import android.provider.CalendarContract;

public class DisconnectCause {
    public static final int ACCESS_INFORMATION_DISCARDED = 74;
    public static final int ANSWERED_ELSEWHERE = 52;
    public static final int BEARER_CAPABILITY_NOT_AUTHORIZED = 63;
    public static final int BEARER_CAPABILITY_UNAVAILABLE = 80;
    public static final int BEARER_SERVICE_NOT_IMPLEMENTED = 82;
    public static final int BUSY = 4;
    public static final int CALL_BARRED = 20;
    public static final int CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE = 106;
    public static final int CALL_DROP_WIFI_BACKHAUL_CONGESTION = 107;
    public static final int CALL_FAIL_DESTINATION_OUT_OF_ORDER = 62;
    public static final int CALL_FAIL_NO_ANSWER_FROM_USER = 61;
    public static final int CALL_FAIL_NO_USER_RESPONDING = 105;
    public static final int CALL_PULLED = 51;
    public static final int CALL_REJECTED = 65;
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
    public static final int CHANNEL_UNACCEPTABLE = 64;
    public static final int CONDITIONAL_IE_ERROR = 95;
    public static final int CONGESTION = 5;
    public static final int CS_RESTRICTED = 22;
    public static final int CS_RESTRICTED_EMERGENCY = 24;
    public static final int CS_RESTRICTED_NORMAL = 23;
    public static final int DATA_DISABLED = 54;
    public static final int DATA_LIMIT_REACHED = 55;
    public static final int DIALED_CALL_FORWARDING_WHILE_ROAMING = 57;
    public static final int DIALED_MMI = 39;
    public static final int DIALED_ON_WRONG_SLOT = 56;
    public static final int DIAL_MODIFIED_TO_DIAL = 48;
    public static final int DIAL_MODIFIED_TO_SS = 47;
    public static final int DIAL_MODIFIED_TO_USSD = 46;
    public static final int EMERGENCY_CALL_CS_REDIAL = 500;
    public static final int EMERGENCY_ONLY = 37;
    public static final int EMERGENCY_PERM_FAILURE = 101;
    public static final int EMERGENCY_TEMP_FAILURE = 100;
    public static final int ERROR_UNSPECIFIED = 36;
    public static final int EXITED_ECM = 42;
    public static final int FACILITY_REJECTED = 68;
    public static final int FDN_BLOCKED = 21;
    public static final int HO_NOT_FEASIBLE = 60;
    public static final int HUAWEI_BASE_VALUE = 59;
    public static final int ICC_ERROR = 19;
    public static final int IMEI_NOT_ACCEPTED = 58;
    public static final int IMS_MERGED_SUCCESSFULLY = 45;
    public static final int INCOMING_CALLS_BARRED_WITHIN_CUG = 79;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int INCOMPATIBLE_DESTINATION = 88;
    public static final int INFORMATION_ELEMENT_NON_EXISTENT = 94;
    public static final int INTERWORKING_UNSPECIFIED = 99;
    public static final int INVALID_CREDENTIALS = 10;
    public static final int INVALID_MANDATORY_INFORMATION = 91;
    public static final int INVALID_NUMBER = 7;
    public static final int INVALID_TRANSACTION_IDENTIFIER = 86;
    public static final int INVALID_TRANSIT_NW_SELECTION = 89;
    public static final int LIMIT_EXCEEDED = 15;
    public static final int LOCAL = 3;
    public static final int LOST_SIGNAL = 14;
    public static final int MAXIMUM_NUMBER_OF_CALLS_REACHED = 53;
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 96;
    public static final int MESSAGE_TYPE_NON_IMPLEMENTED = 92;
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 93;
    public static final int MMI = 6;
    public static final int NETWORK_OUT_OF_ORDER = 71;
    public static final int NORMAL = 2;
    public static final int NORMAL_UNSPECIFIED = 70;
    public static final int NOT_DISCONNECTED = 0;
    public static final int NOT_VALID = -1;
    public static final int NO_CIRCUIT_AVAIL = 102;
    public static final int NO_PHONE_NUMBER_SUPPLIED = 38;
    public static final int NO_ROUTE_TO_DESTINAON = 103;
    public static final int NUMBER_CHANGED = 66;
    public static final int NUMBER_UNREACHABLE = 8;
    public static final int ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE = 84;
    public static final int OPERATOR_DETERMINED_BARRING = 104;
    public static final int OUTGOING_CANCELED = 44;
    public static final int OUTGOING_FAILURE = 43;
    public static final int OUT_OF_NETWORK = 11;
    public static final int OUT_OF_SERVICE = 18;
    public static final int POWER_OFF = 17;
    public static final int PREEMPTION = 67;
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 98;
    public static final int QOS_UNAVAILABLE = 77;
    public static final int RECOVERY_ON_TIMER_EXPIRED = 97;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 75;
    public static final int REQUESTED_FACILITY_NOT_IMPLEMENTED = 83;
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 78;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 76;
    public static final int RESP_TO_STATUS_ENQUIRY = 69;
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 90;
    public static final int SERVER_ERROR = 12;
    public static final int SERVER_UNREACHABLE = 9;
    public static final int SERVICE_OPTION_NOT_AVAILABLE = 81;
    public static final int SERVICE_OR_OPTION_NOT_IMPLEMENTED = 85;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 73;
    public static final int TEMPORARY_FAILURE = 72;
    public static final int TIMED_OUT = 13;
    public static final int UNOBTAINABLE_NUMBER = 25;
    public static final int USER_NOT_MEMBER_OF_CUG = 87;
    public static final int VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED = 50;
    public static final int VOICEMAIL_NUMBER_MISSING = 40;
    public static final int WIFI_LOST = 59;

    private DisconnectCause() {
    }

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
            case 56:
                return "DIALED_ON_WRONG_SLOT";
            case 57:
                return "DIALED_CALL_FORWARDING_WHILE_ROAMING";
            case 58:
                return "IMEI_NOT_ACCEPTED";
            case 59:
                return "WIFI_LOST";
            case 60:
                return "HO_NOT_FEASIBLE";
            case 106:
                return "CALL_DROP_IWLAN_TO_LTE_UNAVAILABLE";
            case 107:
                return "CALL_DROP_WIFI_BACKHAUL_CONGESTION";
            default:
                return "INVALID: " + cause;
        }
    }
}
