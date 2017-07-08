package android.telephony;

public class DisconnectCause {
    public static final int ACCESS_INFORMATION_DISCARDED = 65;
    public static final int BEARER_CAPABILITY_NOT_AUTHORIZED = 54;
    public static final int BEARER_CAPABILITY_UNAVAILABLE = 71;
    public static final int BEARER_SERVICE_NOT_IMPLEMENTED = 73;
    public static final int BUSY = 4;
    public static final int CALL_BARRED = 20;
    public static final int CALL_FAIL_DESTINATION_OUT_OF_ORDER = 53;
    public static final int CALL_FAIL_NO_ANSWER_FROM_USER = 52;
    public static final int CALL_FAIL_NO_USER_RESPONDING = 97;
    public static final int CALL_REJECTED = 56;
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
    public static final int CHANNEL_UNACCEPTABLE = 55;
    public static final int CONDITIONAL_IE_ERROR = 86;
    public static final int CONGESTION = 5;
    public static final int CS_RESTRICTED = 22;
    public static final int CS_RESTRICTED_EMERGENCY = 24;
    public static final int CS_RESTRICTED_NORMAL = 23;
    public static final int DIALED_MMI = 39;
    public static final int DIAL_MODIFIED_TO_DIAL = 48;
    public static final int DIAL_MODIFIED_TO_SS = 47;
    public static final int DIAL_MODIFIED_TO_USSD = 46;
    public static final int EMERGENCY_CALL_CS_REDIAL = 100;
    public static final int EMERGENCY_ONLY = 37;
    public static final int EMERGENCY_PERM_FAILURE = 93;
    public static final int EMERGENCY_TEMP_FAILURE = 92;
    public static final int ERROR_UNSPECIFIED = 36;
    public static final int EXITED_ECM = 42;
    public static final int FACILITY_REJECTED = 59;
    public static final int FDN_BLOCKED = 21;
    public static final int HO_NOT_FEASIBLE = 51;
    public static final int ICC_ERROR = 19;
    public static final int IMS_MERGED_SUCCESSFULLY = 45;
    public static final int INCOMING_CALLS_BARRED_WITHIN_CUG = 70;
    public static final int INCOMING_MISSED = 1;
    public static final int INCOMING_REJECTED = 16;
    public static final int INCOMPATIBLE_DESTINATION = 79;
    public static final int INFORMATION_ELEMENT_NON_EXISTENT = 85;
    public static final int INTERWORKING_UNSPECIFIED = 90;
    public static final int INVALID_CREDENTIALS = 10;
    public static final int INVALID_MANDATORY_INFORMATION = 82;
    public static final int INVALID_NUMBER = 7;
    public static final int INVALID_TRANSACTION_IDENTIFIER = 77;
    public static final int INVALID_TRANSIT_NW_SELECTION = 80;
    public static final int LIMIT_EXCEEDED = 15;
    public static final int LOCAL = 3;
    public static final int LOST_SIGNAL = 14;
    public static final int MAXIMUM_VALID_VALUE = 51;
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 87;
    public static final int MESSAGE_TYPE_NON_IMPLEMENTED = 83;
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 84;
    public static final int MINIMUM_VALID_VALUE = 0;
    public static final int MMI = 6;
    public static final int NETWORK_OUT_OF_ORDER = 62;
    public static final int NORMAL = 2;
    public static final int NORMAL_UNSPECIFIED = 61;
    public static final int NOT_DISCONNECTED = 0;
    public static final int NOT_VALID = -1;
    public static final int NO_CIRCUIT_AVAIL = 94;
    public static final int NO_PHONE_NUMBER_SUPPLIED = 38;
    public static final int NO_ROUTE_TO_DESTINAON = 95;
    public static final int NUMBER_CHANGED = 57;
    public static final int NUMBER_UNREACHABLE = 8;
    public static final int ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE = 75;
    public static final int OPERATOR_DETERMINED_BARRING = 96;
    public static final int OUTGOING_CANCELED = 44;
    public static final int OUTGOING_FAILURE = 43;
    public static final int OUT_OF_NETWORK = 11;
    public static final int OUT_OF_SERVICE = 18;
    public static final int POWER_OFF = 17;
    public static final int PREEMPTION = 58;
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 89;
    public static final int QOS_UNAVAILABLE = 68;
    public static final int RECOVERY_ON_TIMER_EXPIRED = 88;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 66;
    public static final int REQUESTED_FACILITY_NOT_IMPLEMENTED = 74;
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 69;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 67;
    public static final int RESP_TO_STATUS_ENQUIRY = 60;
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 81;
    public static final int SERVER_ERROR = 12;
    public static final int SERVER_UNREACHABLE = 9;
    public static final int SERVICE_OPTION_NOT_AVAILABLE = 72;
    public static final int SERVICE_OR_OPTION_NOT_IMPLEMENTED = 76;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 64;
    public static final int TEMPORARY_FAILURE = 63;
    public static final int TIMED_OUT = 13;
    public static final int UNOBTAINABLE_NUMBER = 25;
    public static final int USER_NOT_MEMBER_OF_CUG = 78;
    public static final int VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED = 50;
    public static final int VOICEMAIL_NUMBER_MISSING = 40;

    private DisconnectCause() {
    }

    public static String toString(int cause) {
        switch (cause) {
            case NOT_DISCONNECTED /*0*/:
                return "NOT_DISCONNECTED";
            case INCOMING_MISSED /*1*/:
                return "INCOMING_MISSED";
            case NORMAL /*2*/:
                return "NORMAL";
            case LOCAL /*3*/:
                return "LOCAL";
            case BUSY /*4*/:
                return "BUSY";
            case CONGESTION /*5*/:
                return "CONGESTION";
            case INVALID_NUMBER /*7*/:
                return "INVALID_NUMBER";
            case NUMBER_UNREACHABLE /*8*/:
                return "NUMBER_UNREACHABLE";
            case SERVER_UNREACHABLE /*9*/:
                return "SERVER_UNREACHABLE";
            case INVALID_CREDENTIALS /*10*/:
                return "INVALID_CREDENTIALS";
            case OUT_OF_NETWORK /*11*/:
                return "OUT_OF_NETWORK";
            case SERVER_ERROR /*12*/:
                return "SERVER_ERROR";
            case TIMED_OUT /*13*/:
                return "TIMED_OUT";
            case LOST_SIGNAL /*14*/:
                return "LOST_SIGNAL";
            case LIMIT_EXCEEDED /*15*/:
                return "LIMIT_EXCEEDED";
            case INCOMING_REJECTED /*16*/:
                return "INCOMING_REJECTED";
            case POWER_OFF /*17*/:
                return "POWER_OFF";
            case OUT_OF_SERVICE /*18*/:
                return "OUT_OF_SERVICE";
            case ICC_ERROR /*19*/:
                return "ICC_ERROR";
            case CALL_BARRED /*20*/:
                return "CALL_BARRED";
            case FDN_BLOCKED /*21*/:
                return "FDN_BLOCKED";
            case CS_RESTRICTED /*22*/:
                return "CS_RESTRICTED";
            case CS_RESTRICTED_NORMAL /*23*/:
                return "CS_RESTRICTED_NORMAL";
            case CS_RESTRICTED_EMERGENCY /*24*/:
                return "CS_RESTRICTED_EMERGENCY";
            case UNOBTAINABLE_NUMBER /*25*/:
                return "UNOBTAINABLE_NUMBER";
            case CDMA_LOCKED_UNTIL_POWER_CYCLE /*26*/:
                return "CDMA_LOCKED_UNTIL_POWER_CYCLE";
            case CDMA_DROP /*27*/:
                return "CDMA_DROP";
            case CDMA_INTERCEPT /*28*/:
                return "CDMA_INTERCEPT";
            case CDMA_REORDER /*29*/:
                return "CDMA_REORDER";
            case CDMA_SO_REJECT /*30*/:
                return "CDMA_SO_REJECT";
            case CDMA_RETRY_ORDER /*31*/:
                return "CDMA_RETRY_ORDER";
            case CDMA_ACCESS_FAILURE /*32*/:
                return "CDMA_ACCESS_FAILURE";
            case CDMA_PREEMPTED /*33*/:
                return "CDMA_PREEMPTED";
            case CDMA_NOT_EMERGENCY /*34*/:
                return "CDMA_NOT_EMERGENCY";
            case CDMA_ACCESS_BLOCKED /*35*/:
                return "CDMA_ACCESS_BLOCKED";
            case ERROR_UNSPECIFIED /*36*/:
                return "ERROR_UNSPECIFIED";
            case EMERGENCY_ONLY /*37*/:
                return "EMERGENCY_ONLY";
            case NO_PHONE_NUMBER_SUPPLIED /*38*/:
                return "NO_PHONE_NUMBER_SUPPLIED";
            case DIALED_MMI /*39*/:
                return "DIALED_MMI";
            case VOICEMAIL_NUMBER_MISSING /*40*/:
                return "VOICEMAIL_NUMBER_MISSING";
            case CDMA_CALL_LOST /*41*/:
                return "CDMA_CALL_LOST";
            case EXITED_ECM /*42*/:
                return "EXITED_ECM";
            case OUTGOING_FAILURE /*43*/:
                return "OUTGOING_FAILURE";
            case OUTGOING_CANCELED /*44*/:
                return "OUTGOING_CANCELED";
            case IMS_MERGED_SUCCESSFULLY /*45*/:
                return "IMS_MERGED_SUCCESSFULLY";
            case DIAL_MODIFIED_TO_USSD /*46*/:
                return "DIAL_MODIFIED_TO_USSD";
            case DIAL_MODIFIED_TO_SS /*47*/:
                return "DIAL_MODIFIED_TO_SS";
            case DIAL_MODIFIED_TO_DIAL /*48*/:
                return "DIAL_MODIFIED_TO_DIAL";
            case CDMA_ALREADY_ACTIVATED /*49*/:
                return "CDMA_ALREADY_ACTIVATED";
            case VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED /*50*/:
                return "VIDEO_CALL_NOT_ALLOWED_WHILE_TTY_ENABLED";
            case MAXIMUM_VALID_VALUE /*51*/:
                return "HO_NOT_FEASIBLE";
            default:
                return "INVALID: " + cause;
        }
    }
}
