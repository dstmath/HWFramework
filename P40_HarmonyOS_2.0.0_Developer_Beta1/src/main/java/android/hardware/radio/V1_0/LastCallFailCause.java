package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class LastCallFailCause {
    public static final int ACCESS_CLASS_BLOCKED = 260;
    public static final int ACCESS_INFORMATION_DISCARDED = 43;
    public static final int ACM_LIMIT_EXCEEDED = 68;
    public static final int BEARER_CAPABILITY_NOT_AUTHORIZED = 57;
    public static final int BEARER_CAPABILITY_UNAVAILABLE = 58;
    public static final int BEARER_SERVICE_NOT_IMPLEMENTED = 65;
    public static final int BUSY = 17;
    public static final int CALL_BARRED = 240;
    public static final int CALL_REJECTED = 21;
    public static final int CDMA_ACCESS_BLOCKED = 1009;
    public static final int CDMA_ACCESS_FAILURE = 1006;
    public static final int CDMA_DROP = 1001;
    public static final int CDMA_INTERCEPT = 1002;
    public static final int CDMA_LOCKED_UNTIL_POWER_CYCLE = 1000;
    public static final int CDMA_NOT_EMERGENCY = 1008;
    public static final int CDMA_PREEMPTED = 1007;
    public static final int CDMA_REORDER = 1003;
    public static final int CDMA_RETRY_ORDER = 1005;
    public static final int CDMA_SO_REJECT = 1004;
    public static final int CHANNEL_UNACCEPTABLE = 6;
    public static final int CONDITIONAL_IE_ERROR = 100;
    public static final int CONGESTION = 34;
    public static final int DESTINATION_OUT_OF_ORDER = 27;
    public static final int DIAL_MODIFIED_TO_DIAL = 246;
    public static final int DIAL_MODIFIED_TO_SS = 245;
    public static final int DIAL_MODIFIED_TO_USSD = 244;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int FACILITY_REJECTED = 29;
    public static final int FDN_BLOCKED = 241;
    public static final int IMEI_NOT_ACCEPTED = 243;
    public static final int IMSI_UNKNOWN_IN_VLR = 242;
    public static final int INCOMING_CALLS_BARRED_WITHIN_CUG = 55;
    public static final int INCOMPATIBLE_DESTINATION = 88;
    public static final int INFORMATION_ELEMENT_NON_EXISTENT = 99;
    public static final int INTERWORKING_UNSPECIFIED = 127;
    public static final int INVALID_MANDATORY_INFORMATION = 96;
    public static final int INVALID_NUMBER_FORMAT = 28;
    public static final int INVALID_TRANSACTION_IDENTIFIER = 81;
    public static final int INVALID_TRANSIT_NW_SELECTION = 91;
    public static final int MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 101;
    public static final int MESSAGE_TYPE_NON_IMPLEMENTED = 97;
    public static final int MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE = 98;
    public static final int NETWORK_DETACH = 261;
    public static final int NETWORK_OUT_OF_ORDER = 38;
    public static final int NETWORK_REJECT = 252;
    public static final int NETWORK_RESP_TIMEOUT = 251;
    public static final int NORMAL = 16;
    public static final int NORMAL_UNSPECIFIED = 31;
    public static final int NO_ANSWER_FROM_USER = 19;
    public static final int NO_ROUTE_TO_DESTINATION = 3;
    public static final int NO_USER_RESPONDING = 18;
    public static final int NO_VALID_SIM = 249;
    public static final int NUMBER_CHANGED = 22;
    public static final int OEM_CAUSE_1 = 61441;
    public static final int OEM_CAUSE_10 = 61450;
    public static final int OEM_CAUSE_11 = 61451;
    public static final int OEM_CAUSE_12 = 61452;
    public static final int OEM_CAUSE_13 = 61453;
    public static final int OEM_CAUSE_14 = 61454;
    public static final int OEM_CAUSE_15 = 61455;
    public static final int OEM_CAUSE_2 = 61442;
    public static final int OEM_CAUSE_3 = 61443;
    public static final int OEM_CAUSE_4 = 61444;
    public static final int OEM_CAUSE_5 = 61445;
    public static final int OEM_CAUSE_6 = 61446;
    public static final int OEM_CAUSE_7 = 61447;
    public static final int OEM_CAUSE_8 = 61448;
    public static final int OEM_CAUSE_9 = 61449;
    public static final int ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE = 70;
    public static final int OPERATOR_DETERMINED_BARRING = 8;
    public static final int OUT_OF_SERVICE = 248;
    public static final int PREEMPTION = 25;
    public static final int PROTOCOL_ERROR_UNSPECIFIED = 111;
    public static final int QOS_UNAVAILABLE = 49;
    public static final int RADIO_ACCESS_FAILURE = 253;
    public static final int RADIO_INTERNAL_ERROR = 250;
    public static final int RADIO_LINK_FAILURE = 254;
    public static final int RADIO_LINK_LOST = 255;
    public static final int RADIO_OFF = 247;
    public static final int RADIO_RELEASE_ABNORMAL = 259;
    public static final int RADIO_RELEASE_NORMAL = 258;
    public static final int RADIO_SETUP_FAILURE = 257;
    public static final int RADIO_UPLINK_FAILURE = 256;
    public static final int RECOVERY_ON_TIMER_EXPIRED = 102;
    public static final int REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE = 44;
    public static final int REQUESTED_FACILITY_NOT_IMPLEMENTED = 69;
    public static final int REQUESTED_FACILITY_NOT_SUBSCRIBED = 50;
    public static final int RESOURCES_UNAVAILABLE_OR_UNSPECIFIED = 47;
    public static final int RESP_TO_STATUS_ENQUIRY = 30;
    public static final int SEMANTICALLY_INCORRECT_MESSAGE = 95;
    public static final int SERVICE_OPTION_NOT_AVAILABLE = 63;
    public static final int SERVICE_OR_OPTION_NOT_IMPLEMENTED = 79;
    public static final int SWITCHING_EQUIPMENT_CONGESTION = 42;
    public static final int TEMPORARY_FAILURE = 41;
    public static final int UNOBTAINABLE_NUMBER = 1;
    public static final int USER_NOT_MEMBER_OF_CUG = 87;

    public static final String toString(int o) {
        if (o == 1) {
            return "UNOBTAINABLE_NUMBER";
        }
        if (o == 3) {
            return "NO_ROUTE_TO_DESTINATION";
        }
        if (o == 6) {
            return "CHANNEL_UNACCEPTABLE";
        }
        if (o == 8) {
            return "OPERATOR_DETERMINED_BARRING";
        }
        if (o == 16) {
            return "NORMAL";
        }
        if (o == 17) {
            return "BUSY";
        }
        if (o == 18) {
            return "NO_USER_RESPONDING";
        }
        if (o == 19) {
            return "NO_ANSWER_FROM_USER";
        }
        if (o == 21) {
            return "CALL_REJECTED";
        }
        if (o == 22) {
            return "NUMBER_CHANGED";
        }
        if (o == 25) {
            return "PREEMPTION";
        }
        if (o == 27) {
            return "DESTINATION_OUT_OF_ORDER";
        }
        if (o == 28) {
            return "INVALID_NUMBER_FORMAT";
        }
        if (o == 29) {
            return "FACILITY_REJECTED";
        }
        if (o == 30) {
            return "RESP_TO_STATUS_ENQUIRY";
        }
        if (o == 31) {
            return "NORMAL_UNSPECIFIED";
        }
        if (o == 34) {
            return "CONGESTION";
        }
        if (o == 38) {
            return "NETWORK_OUT_OF_ORDER";
        }
        if (o == 41) {
            return "TEMPORARY_FAILURE";
        }
        if (o == 42) {
            return "SWITCHING_EQUIPMENT_CONGESTION";
        }
        if (o == 43) {
            return "ACCESS_INFORMATION_DISCARDED";
        }
        if (o == 44) {
            return "REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE";
        }
        if (o == 47) {
            return "RESOURCES_UNAVAILABLE_OR_UNSPECIFIED";
        }
        if (o == 49) {
            return "QOS_UNAVAILABLE";
        }
        if (o == 50) {
            return "REQUESTED_FACILITY_NOT_SUBSCRIBED";
        }
        if (o == 55) {
            return "INCOMING_CALLS_BARRED_WITHIN_CUG";
        }
        if (o == 57) {
            return "BEARER_CAPABILITY_NOT_AUTHORIZED";
        }
        if (o == 58) {
            return "BEARER_CAPABILITY_UNAVAILABLE";
        }
        if (o == 63) {
            return "SERVICE_OPTION_NOT_AVAILABLE";
        }
        if (o == 65) {
            return "BEARER_SERVICE_NOT_IMPLEMENTED";
        }
        if (o == 68) {
            return "ACM_LIMIT_EXCEEDED";
        }
        if (o == 69) {
            return "REQUESTED_FACILITY_NOT_IMPLEMENTED";
        }
        if (o == 70) {
            return "ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE";
        }
        if (o == 79) {
            return "SERVICE_OR_OPTION_NOT_IMPLEMENTED";
        }
        if (o == 81) {
            return "INVALID_TRANSACTION_IDENTIFIER";
        }
        if (o == 87) {
            return "USER_NOT_MEMBER_OF_CUG";
        }
        if (o == 88) {
            return "INCOMPATIBLE_DESTINATION";
        }
        if (o == 91) {
            return "INVALID_TRANSIT_NW_SELECTION";
        }
        if (o == 95) {
            return "SEMANTICALLY_INCORRECT_MESSAGE";
        }
        if (o == 96) {
            return "INVALID_MANDATORY_INFORMATION";
        }
        if (o == 97) {
            return "MESSAGE_TYPE_NON_IMPLEMENTED";
        }
        if (o == 98) {
            return "MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE";
        }
        if (o == 99) {
            return "INFORMATION_ELEMENT_NON_EXISTENT";
        }
        if (o == 100) {
            return "CONDITIONAL_IE_ERROR";
        }
        if (o == 101) {
            return "MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE";
        }
        if (o == 102) {
            return "RECOVERY_ON_TIMER_EXPIRED";
        }
        if (o == 111) {
            return "PROTOCOL_ERROR_UNSPECIFIED";
        }
        if (o == 127) {
            return "INTERWORKING_UNSPECIFIED";
        }
        if (o == 240) {
            return "CALL_BARRED";
        }
        if (o == 241) {
            return "FDN_BLOCKED";
        }
        if (o == 242) {
            return "IMSI_UNKNOWN_IN_VLR";
        }
        if (o == 243) {
            return "IMEI_NOT_ACCEPTED";
        }
        if (o == 244) {
            return "DIAL_MODIFIED_TO_USSD";
        }
        if (o == 245) {
            return "DIAL_MODIFIED_TO_SS";
        }
        if (o == 246) {
            return "DIAL_MODIFIED_TO_DIAL";
        }
        if (o == 247) {
            return "RADIO_OFF";
        }
        if (o == 248) {
            return "OUT_OF_SERVICE";
        }
        if (o == 249) {
            return "NO_VALID_SIM";
        }
        if (o == 250) {
            return "RADIO_INTERNAL_ERROR";
        }
        if (o == 251) {
            return "NETWORK_RESP_TIMEOUT";
        }
        if (o == 252) {
            return "NETWORK_REJECT";
        }
        if (o == 253) {
            return "RADIO_ACCESS_FAILURE";
        }
        if (o == 254) {
            return "RADIO_LINK_FAILURE";
        }
        if (o == 255) {
            return "RADIO_LINK_LOST";
        }
        if (o == 256) {
            return "RADIO_UPLINK_FAILURE";
        }
        if (o == 257) {
            return "RADIO_SETUP_FAILURE";
        }
        if (o == 258) {
            return "RADIO_RELEASE_NORMAL";
        }
        if (o == 259) {
            return "RADIO_RELEASE_ABNORMAL";
        }
        if (o == 260) {
            return "ACCESS_CLASS_BLOCKED";
        }
        if (o == 261) {
            return "NETWORK_DETACH";
        }
        if (o == 1000) {
            return "CDMA_LOCKED_UNTIL_POWER_CYCLE";
        }
        if (o == 1001) {
            return "CDMA_DROP";
        }
        if (o == 1002) {
            return "CDMA_INTERCEPT";
        }
        if (o == 1003) {
            return "CDMA_REORDER";
        }
        if (o == 1004) {
            return "CDMA_SO_REJECT";
        }
        if (o == 1005) {
            return "CDMA_RETRY_ORDER";
        }
        if (o == 1006) {
            return "CDMA_ACCESS_FAILURE";
        }
        if (o == 1007) {
            return "CDMA_PREEMPTED";
        }
        if (o == 1008) {
            return "CDMA_NOT_EMERGENCY";
        }
        if (o == 1009) {
            return "CDMA_ACCESS_BLOCKED";
        }
        if (o == 61441) {
            return "OEM_CAUSE_1";
        }
        if (o == 61442) {
            return "OEM_CAUSE_2";
        }
        if (o == 61443) {
            return "OEM_CAUSE_3";
        }
        if (o == 61444) {
            return "OEM_CAUSE_4";
        }
        if (o == 61445) {
            return "OEM_CAUSE_5";
        }
        if (o == 61446) {
            return "OEM_CAUSE_6";
        }
        if (o == 61447) {
            return "OEM_CAUSE_7";
        }
        if (o == 61448) {
            return "OEM_CAUSE_8";
        }
        if (o == 61449) {
            return "OEM_CAUSE_9";
        }
        if (o == 61450) {
            return "OEM_CAUSE_10";
        }
        if (o == 61451) {
            return "OEM_CAUSE_11";
        }
        if (o == 61452) {
            return "OEM_CAUSE_12";
        }
        if (o == 61453) {
            return "OEM_CAUSE_13";
        }
        if (o == 61454) {
            return "OEM_CAUSE_14";
        }
        if (o == 61455) {
            return "OEM_CAUSE_15";
        }
        if (o == 65535) {
            return "ERROR_UNSPECIFIED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("UNOBTAINABLE_NUMBER");
            flipped = 0 | 1;
        }
        if ((o & 3) == 3) {
            list.add("NO_ROUTE_TO_DESTINATION");
            flipped |= 3;
        }
        if ((o & 6) == 6) {
            list.add("CHANNEL_UNACCEPTABLE");
            flipped |= 6;
        }
        if ((o & 8) == 8) {
            list.add("OPERATOR_DETERMINED_BARRING");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("NORMAL");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("BUSY");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("NO_USER_RESPONDING");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("NO_ANSWER_FROM_USER");
            flipped |= 19;
        }
        if ((o & 21) == 21) {
            list.add("CALL_REJECTED");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("NUMBER_CHANGED");
            flipped |= 22;
        }
        if ((o & 25) == 25) {
            list.add("PREEMPTION");
            flipped |= 25;
        }
        if ((o & 27) == 27) {
            list.add("DESTINATION_OUT_OF_ORDER");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("INVALID_NUMBER_FORMAT");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("FACILITY_REJECTED");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("RESP_TO_STATUS_ENQUIRY");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("NORMAL_UNSPECIFIED");
            flipped |= 31;
        }
        if ((o & 34) == 34) {
            list.add("CONGESTION");
            flipped |= 34;
        }
        if ((o & 38) == 38) {
            list.add("NETWORK_OUT_OF_ORDER");
            flipped |= 38;
        }
        if ((o & 41) == 41) {
            list.add("TEMPORARY_FAILURE");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("SWITCHING_EQUIPMENT_CONGESTION");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("ACCESS_INFORMATION_DISCARDED");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("REQUESTED_CIRCUIT_OR_CHANNEL_NOT_AVAILABLE");
            flipped |= 44;
        }
        if ((o & 47) == 47) {
            list.add("RESOURCES_UNAVAILABLE_OR_UNSPECIFIED");
            flipped |= 47;
        }
        if ((o & 49) == 49) {
            list.add("QOS_UNAVAILABLE");
            flipped |= 49;
        }
        if ((o & 50) == 50) {
            list.add("REQUESTED_FACILITY_NOT_SUBSCRIBED");
            flipped |= 50;
        }
        if ((o & 55) == 55) {
            list.add("INCOMING_CALLS_BARRED_WITHIN_CUG");
            flipped |= 55;
        }
        if ((o & 57) == 57) {
            list.add("BEARER_CAPABILITY_NOT_AUTHORIZED");
            flipped |= 57;
        }
        if ((o & 58) == 58) {
            list.add("BEARER_CAPABILITY_UNAVAILABLE");
            flipped |= 58;
        }
        if ((o & 63) == 63) {
            list.add("SERVICE_OPTION_NOT_AVAILABLE");
            flipped |= 63;
        }
        if ((o & 65) == 65) {
            list.add("BEARER_SERVICE_NOT_IMPLEMENTED");
            flipped |= 65;
        }
        if ((o & 68) == 68) {
            list.add("ACM_LIMIT_EXCEEDED");
            flipped |= 68;
        }
        if ((o & 69) == 69) {
            list.add("REQUESTED_FACILITY_NOT_IMPLEMENTED");
            flipped |= 69;
        }
        if ((o & 70) == 70) {
            list.add("ONLY_DIGITAL_INFORMATION_BEARER_AVAILABLE");
            flipped |= 70;
        }
        if ((o & 79) == 79) {
            list.add("SERVICE_OR_OPTION_NOT_IMPLEMENTED");
            flipped |= 79;
        }
        if ((o & 81) == 81) {
            list.add("INVALID_TRANSACTION_IDENTIFIER");
            flipped |= 81;
        }
        if ((o & 87) == 87) {
            list.add("USER_NOT_MEMBER_OF_CUG");
            flipped |= 87;
        }
        if ((o & 88) == 88) {
            list.add("INCOMPATIBLE_DESTINATION");
            flipped |= 88;
        }
        if ((o & 91) == 91) {
            list.add("INVALID_TRANSIT_NW_SELECTION");
            flipped |= 91;
        }
        if ((o & 95) == 95) {
            list.add("SEMANTICALLY_INCORRECT_MESSAGE");
            flipped |= 95;
        }
        if ((o & 96) == 96) {
            list.add("INVALID_MANDATORY_INFORMATION");
            flipped |= 96;
        }
        if ((o & 97) == 97) {
            list.add("MESSAGE_TYPE_NON_IMPLEMENTED");
            flipped |= 97;
        }
        if ((o & 98) == 98) {
            list.add("MESSAGE_TYPE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE");
            flipped |= 98;
        }
        if ((o & 99) == 99) {
            list.add("INFORMATION_ELEMENT_NON_EXISTENT");
            flipped |= 99;
        }
        if ((o & 100) == 100) {
            list.add("CONDITIONAL_IE_ERROR");
            flipped |= 100;
        }
        if ((o & 101) == 101) {
            list.add("MESSAGE_NOT_COMPATIBLE_WITH_PROTOCOL_STATE");
            flipped |= 101;
        }
        if ((o & 102) == 102) {
            list.add("RECOVERY_ON_TIMER_EXPIRED");
            flipped |= 102;
        }
        if ((o & 111) == 111) {
            list.add("PROTOCOL_ERROR_UNSPECIFIED");
            flipped |= 111;
        }
        if ((o & 127) == 127) {
            list.add("INTERWORKING_UNSPECIFIED");
            flipped |= 127;
        }
        if ((o & 240) == 240) {
            list.add("CALL_BARRED");
            flipped |= 240;
        }
        if ((o & 241) == 241) {
            list.add("FDN_BLOCKED");
            flipped |= 241;
        }
        if ((o & 242) == 242) {
            list.add("IMSI_UNKNOWN_IN_VLR");
            flipped |= 242;
        }
        if ((o & 243) == 243) {
            list.add("IMEI_NOT_ACCEPTED");
            flipped |= 243;
        }
        if ((o & 244) == 244) {
            list.add("DIAL_MODIFIED_TO_USSD");
            flipped |= 244;
        }
        if ((o & 245) == 245) {
            list.add("DIAL_MODIFIED_TO_SS");
            flipped |= 245;
        }
        if ((o & 246) == 246) {
            list.add("DIAL_MODIFIED_TO_DIAL");
            flipped |= 246;
        }
        if ((o & 247) == 247) {
            list.add("RADIO_OFF");
            flipped |= 247;
        }
        if ((o & 248) == 248) {
            list.add("OUT_OF_SERVICE");
            flipped |= 248;
        }
        if ((o & 249) == 249) {
            list.add("NO_VALID_SIM");
            flipped |= 249;
        }
        if ((o & 250) == 250) {
            list.add("RADIO_INTERNAL_ERROR");
            flipped |= 250;
        }
        if ((o & 251) == 251) {
            list.add("NETWORK_RESP_TIMEOUT");
            flipped |= 251;
        }
        if ((o & 252) == 252) {
            list.add("NETWORK_REJECT");
            flipped |= 252;
        }
        if ((o & 253) == 253) {
            list.add("RADIO_ACCESS_FAILURE");
            flipped |= 253;
        }
        if ((o & 254) == 254) {
            list.add("RADIO_LINK_FAILURE");
            flipped |= 254;
        }
        if ((o & 255) == 255) {
            list.add("RADIO_LINK_LOST");
            flipped |= 255;
        }
        if ((o & 256) == 256) {
            list.add("RADIO_UPLINK_FAILURE");
            flipped |= 256;
        }
        if ((o & 257) == 257) {
            list.add("RADIO_SETUP_FAILURE");
            flipped |= 257;
        }
        if ((o & 258) == 258) {
            list.add("RADIO_RELEASE_NORMAL");
            flipped |= 258;
        }
        if ((o & 259) == 259) {
            list.add("RADIO_RELEASE_ABNORMAL");
            flipped |= 259;
        }
        if ((o & 260) == 260) {
            list.add("ACCESS_CLASS_BLOCKED");
            flipped |= 260;
        }
        if ((o & 261) == 261) {
            list.add("NETWORK_DETACH");
            flipped |= 261;
        }
        if ((o & 1000) == 1000) {
            list.add("CDMA_LOCKED_UNTIL_POWER_CYCLE");
            flipped |= 1000;
        }
        if ((o & 1001) == 1001) {
            list.add("CDMA_DROP");
            flipped |= 1001;
        }
        if ((o & 1002) == 1002) {
            list.add("CDMA_INTERCEPT");
            flipped |= 1002;
        }
        if ((o & 1003) == 1003) {
            list.add("CDMA_REORDER");
            flipped |= 1003;
        }
        if ((o & 1004) == 1004) {
            list.add("CDMA_SO_REJECT");
            flipped |= 1004;
        }
        if ((o & 1005) == 1005) {
            list.add("CDMA_RETRY_ORDER");
            flipped |= 1005;
        }
        if ((o & 1006) == 1006) {
            list.add("CDMA_ACCESS_FAILURE");
            flipped |= 1006;
        }
        if ((o & 1007) == 1007) {
            list.add("CDMA_PREEMPTED");
            flipped |= 1007;
        }
        if ((o & 1008) == 1008) {
            list.add("CDMA_NOT_EMERGENCY");
            flipped |= 1008;
        }
        if ((o & 1009) == 1009) {
            list.add("CDMA_ACCESS_BLOCKED");
            flipped |= 1009;
        }
        if ((61441 & o) == 61441) {
            list.add("OEM_CAUSE_1");
            flipped |= 61441;
        }
        if ((61442 & o) == 61442) {
            list.add("OEM_CAUSE_2");
            flipped |= 61442;
        }
        if ((61443 & o) == 61443) {
            list.add("OEM_CAUSE_3");
            flipped |= 61443;
        }
        if ((61444 & o) == 61444) {
            list.add("OEM_CAUSE_4");
            flipped |= 61444;
        }
        if ((61445 & o) == 61445) {
            list.add("OEM_CAUSE_5");
            flipped |= 61445;
        }
        if ((61446 & o) == 61446) {
            list.add("OEM_CAUSE_6");
            flipped |= 61446;
        }
        if ((61447 & o) == 61447) {
            list.add("OEM_CAUSE_7");
            flipped |= 61447;
        }
        if ((61448 & o) == 61448) {
            list.add("OEM_CAUSE_8");
            flipped |= 61448;
        }
        if ((61449 & o) == 61449) {
            list.add("OEM_CAUSE_9");
            flipped |= 61449;
        }
        if ((61450 & o) == 61450) {
            list.add("OEM_CAUSE_10");
            flipped |= 61450;
        }
        if ((61451 & o) == 61451) {
            list.add("OEM_CAUSE_11");
            flipped |= 61451;
        }
        if ((61452 & o) == 61452) {
            list.add("OEM_CAUSE_12");
            flipped |= 61452;
        }
        if ((61453 & o) == 61453) {
            list.add("OEM_CAUSE_13");
            flipped |= 61453;
        }
        if ((61454 & o) == 61454) {
            list.add("OEM_CAUSE_14");
            flipped |= 61454;
        }
        if ((61455 & o) == 61455) {
            list.add("OEM_CAUSE_15");
            flipped |= 61455;
        }
        if ((65535 & o) == 65535) {
            list.add("ERROR_UNSPECIFIED");
            flipped |= 65535;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
