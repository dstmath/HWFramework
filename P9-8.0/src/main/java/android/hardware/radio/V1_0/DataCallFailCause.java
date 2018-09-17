package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class DataCallFailCause {
    public static final int ACTIVATION_REJECT_GGSN = 30;
    public static final int ACTIVATION_REJECT_UNSPECIFIED = 31;
    public static final int APN_TYPE_CONFLICT = 112;
    public static final int AUTH_FAILURE_ON_EMERGENCY_CALL = 122;
    public static final int COMPANION_IFACE_IN_USE = 118;
    public static final int CONDITIONAL_IE_ERROR = 100;
    public static final int DATA_REGISTRATION_FAIL = -2;
    public static final int EMERGENCY_IFACE_ONLY = 116;
    public static final int EMM_ACCESS_BARRED = 115;
    public static final int EMM_ACCESS_BARRED_INFINITE_RETRY = 121;
    public static final int ERROR_UNSPECIFIED = 65535;
    public static final int ESM_INFO_NOT_RECEIVED = 53;
    public static final int FEATURE_NOT_SUPP = 40;
    public static final int FILTER_SEMANTIC_ERROR = 44;
    public static final int FILTER_SYTAX_ERROR = 45;
    public static final int IFACE_AND_POL_FAMILY_MISMATCH = 120;
    public static final int IFACE_MISMATCH = 117;
    public static final int INSUFFICIENT_RESOURCES = 26;
    public static final int INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN = 114;
    public static final int INVALID_MANDATORY_INFO = 96;
    public static final int INVALID_PCSCF_ADDR = 113;
    public static final int INVALID_TRANSACTION_ID = 81;
    public static final int IP_ADDRESS_MISMATCH = 119;
    public static final int MAX_ACTIVE_PDP_CONTEXT_REACHED = 65;
    public static final int MESSAGE_INCORRECT_SEMANTIC = 95;
    public static final int MESSAGE_TYPE_UNSUPPORTED = 97;
    public static final int MISSING_UKNOWN_APN = 27;
    public static final int MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE = 101;
    public static final int MSG_TYPE_NONCOMPATIBLE_STATE = 98;
    public static final int MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED = 55;
    public static final int NAS_SIGNALLING = 14;
    public static final int NETWORK_FAILURE = 38;
    public static final int NONE = 0;
    public static final int NSAPI_IN_USE = 35;
    public static final int OEM_DCFAILCAUSE_1 = 4097;
    public static final int OEM_DCFAILCAUSE_10 = 4106;
    public static final int OEM_DCFAILCAUSE_11 = 4107;
    public static final int OEM_DCFAILCAUSE_12 = 4108;
    public static final int OEM_DCFAILCAUSE_13 = 4109;
    public static final int OEM_DCFAILCAUSE_14 = 4110;
    public static final int OEM_DCFAILCAUSE_15 = 4111;
    public static final int OEM_DCFAILCAUSE_2 = 4098;
    public static final int OEM_DCFAILCAUSE_3 = 4099;
    public static final int OEM_DCFAILCAUSE_4 = 4100;
    public static final int OEM_DCFAILCAUSE_5 = 4101;
    public static final int OEM_DCFAILCAUSE_6 = 4102;
    public static final int OEM_DCFAILCAUSE_7 = 4103;
    public static final int OEM_DCFAILCAUSE_8 = 4104;
    public static final int OEM_DCFAILCAUSE_9 = 4105;
    public static final int ONLY_IPV4_ALLOWED = 50;
    public static final int ONLY_IPV6_ALLOWED = 51;
    public static final int ONLY_SINGLE_BEARER_ALLOWED = 52;
    public static final int OPERATOR_BARRED = 8;
    public static final int PDN_CONN_DOES_NOT_EXIST = 54;
    public static final int PDP_WITHOUT_ACTIVE_TFT = 46;
    public static final int PREF_RADIO_TECH_CHANGED = -4;
    public static final int PROTOCOL_ERRORS = 111;
    public static final int QOS_NOT_ACCEPTED = 37;
    public static final int RADIO_POWER_OFF = -5;
    public static final int REGULAR_DEACTIVATION = 36;
    public static final int SERVICE_OPTION_NOT_SUBSCRIBED = 33;
    public static final int SERVICE_OPTION_NOT_SUPPORTED = 32;
    public static final int SERVICE_OPTION_OUT_OF_ORDER = 34;
    public static final int SIGNAL_LOST = -3;
    public static final int TETHERED_CALL_ACTIVE = -6;
    public static final int TFT_SEMANTIC_ERROR = 41;
    public static final int TFT_SYTAX_ERROR = 42;
    public static final int UMTS_REACTIVATION_REQ = 39;
    public static final int UNKNOWN_INFO_ELEMENT = 99;
    public static final int UNKNOWN_PDP_ADDRESS_TYPE = 28;
    public static final int UNKNOWN_PDP_CONTEXT = 43;
    public static final int UNSUPPORTED_APN_IN_CURRENT_PLMN = 66;
    public static final int USER_AUTHENTICATION = 29;
    public static final int VOICE_REGISTRATION_FAIL = -1;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 8) {
            return "OPERATOR_BARRED";
        }
        if (o == 14) {
            return "NAS_SIGNALLING";
        }
        if (o == 26) {
            return "INSUFFICIENT_RESOURCES";
        }
        if (o == 27) {
            return "MISSING_UKNOWN_APN";
        }
        if (o == 28) {
            return "UNKNOWN_PDP_ADDRESS_TYPE";
        }
        if (o == 29) {
            return "USER_AUTHENTICATION";
        }
        if (o == 30) {
            return "ACTIVATION_REJECT_GGSN";
        }
        if (o == 31) {
            return "ACTIVATION_REJECT_UNSPECIFIED";
        }
        if (o == 32) {
            return "SERVICE_OPTION_NOT_SUPPORTED";
        }
        if (o == 33) {
            return "SERVICE_OPTION_NOT_SUBSCRIBED";
        }
        if (o == 34) {
            return "SERVICE_OPTION_OUT_OF_ORDER";
        }
        if (o == 35) {
            return "NSAPI_IN_USE";
        }
        if (o == 36) {
            return "REGULAR_DEACTIVATION";
        }
        if (o == 37) {
            return "QOS_NOT_ACCEPTED";
        }
        if (o == 38) {
            return "NETWORK_FAILURE";
        }
        if (o == 39) {
            return "UMTS_REACTIVATION_REQ";
        }
        if (o == 40) {
            return "FEATURE_NOT_SUPP";
        }
        if (o == 41) {
            return "TFT_SEMANTIC_ERROR";
        }
        if (o == 42) {
            return "TFT_SYTAX_ERROR";
        }
        if (o == 43) {
            return "UNKNOWN_PDP_CONTEXT";
        }
        if (o == 44) {
            return "FILTER_SEMANTIC_ERROR";
        }
        if (o == 45) {
            return "FILTER_SYTAX_ERROR";
        }
        if (o == 46) {
            return "PDP_WITHOUT_ACTIVE_TFT";
        }
        if (o == 50) {
            return "ONLY_IPV4_ALLOWED";
        }
        if (o == 51) {
            return "ONLY_IPV6_ALLOWED";
        }
        if (o == 52) {
            return "ONLY_SINGLE_BEARER_ALLOWED";
        }
        if (o == 53) {
            return "ESM_INFO_NOT_RECEIVED";
        }
        if (o == 54) {
            return "PDN_CONN_DOES_NOT_EXIST";
        }
        if (o == 55) {
            return "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED";
        }
        if (o == 65) {
            return "MAX_ACTIVE_PDP_CONTEXT_REACHED";
        }
        if (o == 66) {
            return "UNSUPPORTED_APN_IN_CURRENT_PLMN";
        }
        if (o == 81) {
            return "INVALID_TRANSACTION_ID";
        }
        if (o == 95) {
            return "MESSAGE_INCORRECT_SEMANTIC";
        }
        if (o == 96) {
            return "INVALID_MANDATORY_INFO";
        }
        if (o == 97) {
            return "MESSAGE_TYPE_UNSUPPORTED";
        }
        if (o == 98) {
            return "MSG_TYPE_NONCOMPATIBLE_STATE";
        }
        if (o == 99) {
            return "UNKNOWN_INFO_ELEMENT";
        }
        if (o == 100) {
            return "CONDITIONAL_IE_ERROR";
        }
        if (o == 101) {
            return "MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE";
        }
        if (o == 111) {
            return "PROTOCOL_ERRORS";
        }
        if (o == 112) {
            return "APN_TYPE_CONFLICT";
        }
        if (o == 113) {
            return "INVALID_PCSCF_ADDR";
        }
        if (o == 114) {
            return "INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN";
        }
        if (o == 115) {
            return "EMM_ACCESS_BARRED";
        }
        if (o == 116) {
            return "EMERGENCY_IFACE_ONLY";
        }
        if (o == 117) {
            return "IFACE_MISMATCH";
        }
        if (o == 118) {
            return "COMPANION_IFACE_IN_USE";
        }
        if (o == 119) {
            return "IP_ADDRESS_MISMATCH";
        }
        if (o == 120) {
            return "IFACE_AND_POL_FAMILY_MISMATCH";
        }
        if (o == 121) {
            return "EMM_ACCESS_BARRED_INFINITE_RETRY";
        }
        if (o == 122) {
            return "AUTH_FAILURE_ON_EMERGENCY_CALL";
        }
        if (o == OEM_DCFAILCAUSE_1) {
            return "OEM_DCFAILCAUSE_1";
        }
        if (o == OEM_DCFAILCAUSE_2) {
            return "OEM_DCFAILCAUSE_2";
        }
        if (o == OEM_DCFAILCAUSE_3) {
            return "OEM_DCFAILCAUSE_3";
        }
        if (o == OEM_DCFAILCAUSE_4) {
            return "OEM_DCFAILCAUSE_4";
        }
        if (o == OEM_DCFAILCAUSE_5) {
            return "OEM_DCFAILCAUSE_5";
        }
        if (o == OEM_DCFAILCAUSE_6) {
            return "OEM_DCFAILCAUSE_6";
        }
        if (o == OEM_DCFAILCAUSE_7) {
            return "OEM_DCFAILCAUSE_7";
        }
        if (o == OEM_DCFAILCAUSE_8) {
            return "OEM_DCFAILCAUSE_8";
        }
        if (o == OEM_DCFAILCAUSE_9) {
            return "OEM_DCFAILCAUSE_9";
        }
        if (o == OEM_DCFAILCAUSE_10) {
            return "OEM_DCFAILCAUSE_10";
        }
        if (o == OEM_DCFAILCAUSE_11) {
            return "OEM_DCFAILCAUSE_11";
        }
        if (o == OEM_DCFAILCAUSE_12) {
            return "OEM_DCFAILCAUSE_12";
        }
        if (o == OEM_DCFAILCAUSE_13) {
            return "OEM_DCFAILCAUSE_13";
        }
        if (o == OEM_DCFAILCAUSE_14) {
            return "OEM_DCFAILCAUSE_14";
        }
        if (o == OEM_DCFAILCAUSE_15) {
            return "OEM_DCFAILCAUSE_15";
        }
        if (o == -1) {
            return "VOICE_REGISTRATION_FAIL";
        }
        if (o == -2) {
            return "DATA_REGISTRATION_FAIL";
        }
        if (o == -3) {
            return "SIGNAL_LOST";
        }
        if (o == -4) {
            return "PREF_RADIO_TECH_CHANGED";
        }
        if (o == -5) {
            return "RADIO_POWER_OFF";
        }
        if (o == -6) {
            return "TETHERED_CALL_ACTIVE";
        }
        if (o == 65535) {
            return "ERROR_UNSPECIFIED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        if ((o & 0) == 0) {
            list.add("NONE");
            flipped = 0;
        }
        if ((o & 8) == 8) {
            list.add("OPERATOR_BARRED");
            flipped |= 8;
        }
        if ((o & 14) == 14) {
            list.add("NAS_SIGNALLING");
            flipped |= 14;
        }
        if ((o & 26) == 26) {
            list.add("INSUFFICIENT_RESOURCES");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("MISSING_UKNOWN_APN");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("UNKNOWN_PDP_ADDRESS_TYPE");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("USER_AUTHENTICATION");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("ACTIVATION_REJECT_GGSN");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("ACTIVATION_REJECT_UNSPECIFIED");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("SERVICE_OPTION_NOT_SUPPORTED");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("SERVICE_OPTION_NOT_SUBSCRIBED");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("SERVICE_OPTION_OUT_OF_ORDER");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("NSAPI_IN_USE");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("REGULAR_DEACTIVATION");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("QOS_NOT_ACCEPTED");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("NETWORK_FAILURE");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("UMTS_REACTIVATION_REQ");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("FEATURE_NOT_SUPP");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("TFT_SEMANTIC_ERROR");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("TFT_SYTAX_ERROR");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("UNKNOWN_PDP_CONTEXT");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("FILTER_SEMANTIC_ERROR");
            flipped |= 44;
        }
        if ((o & 45) == 45) {
            list.add("FILTER_SYTAX_ERROR");
            flipped |= 45;
        }
        if ((o & 46) == 46) {
            list.add("PDP_WITHOUT_ACTIVE_TFT");
            flipped |= 46;
        }
        if ((o & 50) == 50) {
            list.add("ONLY_IPV4_ALLOWED");
            flipped |= 50;
        }
        if ((o & 51) == 51) {
            list.add("ONLY_IPV6_ALLOWED");
            flipped |= 51;
        }
        if ((o & 52) == 52) {
            list.add("ONLY_SINGLE_BEARER_ALLOWED");
            flipped |= 52;
        }
        if ((o & 53) == 53) {
            list.add("ESM_INFO_NOT_RECEIVED");
            flipped |= 53;
        }
        if ((o & 54) == 54) {
            list.add("PDN_CONN_DOES_NOT_EXIST");
            flipped |= 54;
        }
        if ((o & 55) == 55) {
            list.add("MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED");
            flipped |= 55;
        }
        if ((o & 65) == 65) {
            list.add("MAX_ACTIVE_PDP_CONTEXT_REACHED");
            flipped |= 65;
        }
        if ((o & 66) == 66) {
            list.add("UNSUPPORTED_APN_IN_CURRENT_PLMN");
            flipped |= 66;
        }
        if ((o & 81) == 81) {
            list.add("INVALID_TRANSACTION_ID");
            flipped |= 81;
        }
        if ((o & 95) == 95) {
            list.add("MESSAGE_INCORRECT_SEMANTIC");
            flipped |= 95;
        }
        if ((o & 96) == 96) {
            list.add("INVALID_MANDATORY_INFO");
            flipped |= 96;
        }
        if ((o & 97) == 97) {
            list.add("MESSAGE_TYPE_UNSUPPORTED");
            flipped |= 97;
        }
        if ((o & 98) == 98) {
            list.add("MSG_TYPE_NONCOMPATIBLE_STATE");
            flipped |= 98;
        }
        if ((o & 99) == 99) {
            list.add("UNKNOWN_INFO_ELEMENT");
            flipped |= 99;
        }
        if ((o & 100) == 100) {
            list.add("CONDITIONAL_IE_ERROR");
            flipped |= 100;
        }
        if ((o & 101) == 101) {
            list.add("MSG_AND_PROTOCOL_STATE_UNCOMPATIBLE");
            flipped |= 101;
        }
        if ((o & 111) == 111) {
            list.add("PROTOCOL_ERRORS");
            flipped |= 111;
        }
        if ((o & 112) == 112) {
            list.add("APN_TYPE_CONFLICT");
            flipped |= 112;
        }
        if ((o & 113) == 113) {
            list.add("INVALID_PCSCF_ADDR");
            flipped |= 113;
        }
        if ((o & 114) == 114) {
            list.add("INTERNAL_CALL_PREEMPT_BY_HIGH_PRIO_APN");
            flipped |= 114;
        }
        if ((o & 115) == 115) {
            list.add("EMM_ACCESS_BARRED");
            flipped |= 115;
        }
        if ((o & 116) == 116) {
            list.add("EMERGENCY_IFACE_ONLY");
            flipped |= 116;
        }
        if ((o & 117) == 117) {
            list.add("IFACE_MISMATCH");
            flipped |= 117;
        }
        if ((o & 118) == 118) {
            list.add("COMPANION_IFACE_IN_USE");
            flipped |= 118;
        }
        if ((o & 119) == 119) {
            list.add("IP_ADDRESS_MISMATCH");
            flipped |= 119;
        }
        if ((o & 120) == 120) {
            list.add("IFACE_AND_POL_FAMILY_MISMATCH");
            flipped |= 120;
        }
        if ((o & 121) == 121) {
            list.add("EMM_ACCESS_BARRED_INFINITE_RETRY");
            flipped |= 121;
        }
        if ((o & 122) == 122) {
            list.add("AUTH_FAILURE_ON_EMERGENCY_CALL");
            flipped |= 122;
        }
        if ((o & OEM_DCFAILCAUSE_1) == OEM_DCFAILCAUSE_1) {
            list.add("OEM_DCFAILCAUSE_1");
            flipped |= OEM_DCFAILCAUSE_1;
        }
        if ((o & OEM_DCFAILCAUSE_2) == OEM_DCFAILCAUSE_2) {
            list.add("OEM_DCFAILCAUSE_2");
            flipped |= OEM_DCFAILCAUSE_2;
        }
        if ((o & OEM_DCFAILCAUSE_3) == OEM_DCFAILCAUSE_3) {
            list.add("OEM_DCFAILCAUSE_3");
            flipped |= OEM_DCFAILCAUSE_3;
        }
        if ((o & OEM_DCFAILCAUSE_4) == OEM_DCFAILCAUSE_4) {
            list.add("OEM_DCFAILCAUSE_4");
            flipped |= OEM_DCFAILCAUSE_4;
        }
        if ((o & OEM_DCFAILCAUSE_5) == OEM_DCFAILCAUSE_5) {
            list.add("OEM_DCFAILCAUSE_5");
            flipped |= OEM_DCFAILCAUSE_5;
        }
        if ((o & OEM_DCFAILCAUSE_6) == OEM_DCFAILCAUSE_6) {
            list.add("OEM_DCFAILCAUSE_6");
            flipped |= OEM_DCFAILCAUSE_6;
        }
        if ((o & OEM_DCFAILCAUSE_7) == OEM_DCFAILCAUSE_7) {
            list.add("OEM_DCFAILCAUSE_7");
            flipped |= OEM_DCFAILCAUSE_7;
        }
        if ((o & OEM_DCFAILCAUSE_8) == OEM_DCFAILCAUSE_8) {
            list.add("OEM_DCFAILCAUSE_8");
            flipped |= OEM_DCFAILCAUSE_8;
        }
        if ((o & OEM_DCFAILCAUSE_9) == OEM_DCFAILCAUSE_9) {
            list.add("OEM_DCFAILCAUSE_9");
            flipped |= OEM_DCFAILCAUSE_9;
        }
        if ((o & OEM_DCFAILCAUSE_10) == OEM_DCFAILCAUSE_10) {
            list.add("OEM_DCFAILCAUSE_10");
            flipped |= OEM_DCFAILCAUSE_10;
        }
        if ((o & OEM_DCFAILCAUSE_11) == OEM_DCFAILCAUSE_11) {
            list.add("OEM_DCFAILCAUSE_11");
            flipped |= OEM_DCFAILCAUSE_11;
        }
        if ((o & OEM_DCFAILCAUSE_12) == OEM_DCFAILCAUSE_12) {
            list.add("OEM_DCFAILCAUSE_12");
            flipped |= OEM_DCFAILCAUSE_12;
        }
        if ((o & OEM_DCFAILCAUSE_13) == OEM_DCFAILCAUSE_13) {
            list.add("OEM_DCFAILCAUSE_13");
            flipped |= OEM_DCFAILCAUSE_13;
        }
        if ((o & OEM_DCFAILCAUSE_14) == OEM_DCFAILCAUSE_14) {
            list.add("OEM_DCFAILCAUSE_14");
            flipped |= OEM_DCFAILCAUSE_14;
        }
        if ((o & OEM_DCFAILCAUSE_15) == OEM_DCFAILCAUSE_15) {
            list.add("OEM_DCFAILCAUSE_15");
            flipped |= OEM_DCFAILCAUSE_15;
        }
        if ((o & -1) == -1) {
            list.add("VOICE_REGISTRATION_FAIL");
            flipped |= -1;
        }
        if ((o & -2) == -2) {
            list.add("DATA_REGISTRATION_FAIL");
            flipped |= -2;
        }
        if ((o & -3) == -3) {
            list.add("SIGNAL_LOST");
            flipped |= -3;
        }
        if ((o & -4) == -4) {
            list.add("PREF_RADIO_TECH_CHANGED");
            flipped |= -4;
        }
        if ((o & -5) == -5) {
            list.add("RADIO_POWER_OFF");
            flipped |= -5;
        }
        if ((o & -6) == -6) {
            list.add("TETHERED_CALL_ACTIVE");
            flipped |= -6;
        }
        if ((o & 65535) == 65535) {
            list.add("ERROR_UNSPECIFIED");
            flipped |= 65535;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
