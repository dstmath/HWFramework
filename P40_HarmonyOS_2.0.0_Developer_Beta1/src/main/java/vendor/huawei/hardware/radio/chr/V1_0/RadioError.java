package vendor.huawei.hardware.radio.chr.V1_0;

import java.util.ArrayList;

public final class RadioError {
    public static final int ABORTED = 65;
    public static final int CANCELLED = 7;
    public static final int DEVICE_IN_USE = 64;
    public static final int DIAL_MODIFIED_TO_DIAL = 20;
    public static final int DIAL_MODIFIED_TO_SS = 19;
    public static final int DIAL_MODIFIED_TO_USSD = 18;
    public static final int EMPTY_RECORD = 55;
    public static final int ENCODING_ERR = 57;
    public static final int FDN_CHECK_FAILURE = 14;
    public static final int GENERIC_FAILURE = 2;
    public static final int ILLEGAL_SIM_OR_ME = 15;
    public static final int INTERNAL_ERR = 38;
    public static final int INVALID_ARGUMENTS = 44;
    public static final int INVALID_CALL_ID = 47;
    public static final int INVALID_MODEM_STATE = 46;
    public static final int INVALID_RESPONSE = 66;
    public static final int INVALID_SIM_STATE = 45;
    public static final int INVALID_SMSC_ADDRESS = 58;
    public static final int INVALID_SMS_FORMAT = 56;
    public static final int INVALID_STATE = 41;
    public static final int LCE_NOT_SUPPORTED = 36;
    public static final int MISSING_RESOURCE = 16;
    public static final int MODEM_ERR = 40;
    public static final int MODE_NOT_SUPPORTED = 13;
    public static final int NETWORK_ERR = 49;
    public static final int NETWORK_NOT_READY = 60;
    public static final int NETWORK_REJECT = 53;
    public static final int NONE = 0;
    public static final int NOT_PROVISIONED = 61;
    public static final int NO_MEMORY = 37;
    public static final int NO_NETWORK_FOUND = 63;
    public static final int NO_RESOURCES = 42;
    public static final int NO_SMS_TO_ACK = 48;
    public static final int NO_SUBSCRIPTION = 62;
    public static final int NO_SUCH_ELEMENT = 17;
    public static final int NO_SUCH_ENTRY = 59;
    public static final int OEM_ERROR_1 = 501;
    public static final int OEM_ERROR_10 = 510;
    public static final int OEM_ERROR_11 = 511;
    public static final int OEM_ERROR_12 = 512;
    public static final int OEM_ERROR_13 = 513;
    public static final int OEM_ERROR_14 = 514;
    public static final int OEM_ERROR_15 = 515;
    public static final int OEM_ERROR_16 = 516;
    public static final int OEM_ERROR_17 = 517;
    public static final int OEM_ERROR_18 = 518;
    public static final int OEM_ERROR_19 = 519;
    public static final int OEM_ERROR_2 = 502;
    public static final int OEM_ERROR_20 = 520;
    public static final int OEM_ERROR_21 = 521;
    public static final int OEM_ERROR_22 = 522;
    public static final int OEM_ERROR_23 = 523;
    public static final int OEM_ERROR_24 = 524;
    public static final int OEM_ERROR_25 = 525;
    public static final int OEM_ERROR_3 = 503;
    public static final int OEM_ERROR_4 = 504;
    public static final int OEM_ERROR_5 = 505;
    public static final int OEM_ERROR_6 = 506;
    public static final int OEM_ERROR_7 = 507;
    public static final int OEM_ERROR_8 = 508;
    public static final int OEM_ERROR_9 = 509;
    public static final int OPERATION_NOT_ALLOWED = 54;
    public static final int OP_NOT_ALLOWED_BEFORE_REG_TO_NW = 9;
    public static final int OP_NOT_ALLOWED_DURING_VOICE_CALL = 8;
    public static final int PASSWORD_INCORRECT = 3;
    public static final int RADIO_NOT_AVAILABLE = 1;
    public static final int REQUEST_NOT_SUPPORTED = 6;
    public static final int REQUEST_RATE_LIMITED = 50;
    public static final int SIM_ABSENT = 11;
    public static final int SIM_BUSY = 51;
    public static final int SIM_ERR = 43;
    public static final int SIM_FULL = 52;
    public static final int SIM_PIN2 = 4;
    public static final int SIM_PUK2 = 5;
    public static final int SMS_SEND_FAIL_RETRY = 10;
    public static final int SS_MODIFIED_TO_DIAL = 24;
    public static final int SS_MODIFIED_TO_SS = 27;
    public static final int SS_MODIFIED_TO_USSD = 25;
    public static final int SUBSCRIPTION_NOT_AVAILABLE = 12;
    public static final int SUBSCRIPTION_NOT_SUPPORTED = 26;
    public static final int SYSTEM_ERR = 39;
    public static final int USSD_MODIFIED_TO_DIAL = 21;
    public static final int USSD_MODIFIED_TO_SS = 22;
    public static final int USSD_MODIFIED_TO_USSD = 23;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "RADIO_NOT_AVAILABLE";
        }
        if (o == 2) {
            return "GENERIC_FAILURE";
        }
        if (o == 3) {
            return "PASSWORD_INCORRECT";
        }
        if (o == 4) {
            return "SIM_PIN2";
        }
        if (o == 5) {
            return "SIM_PUK2";
        }
        if (o == 6) {
            return "REQUEST_NOT_SUPPORTED";
        }
        if (o == 7) {
            return "CANCELLED";
        }
        if (o == 8) {
            return "OP_NOT_ALLOWED_DURING_VOICE_CALL";
        }
        if (o == 9) {
            return "OP_NOT_ALLOWED_BEFORE_REG_TO_NW";
        }
        if (o == 10) {
            return "SMS_SEND_FAIL_RETRY";
        }
        if (o == 11) {
            return "SIM_ABSENT";
        }
        if (o == 12) {
            return "SUBSCRIPTION_NOT_AVAILABLE";
        }
        if (o == 13) {
            return "MODE_NOT_SUPPORTED";
        }
        if (o == 14) {
            return "FDN_CHECK_FAILURE";
        }
        if (o == 15) {
            return "ILLEGAL_SIM_OR_ME";
        }
        if (o == 16) {
            return "MISSING_RESOURCE";
        }
        if (o == 17) {
            return "NO_SUCH_ELEMENT";
        }
        if (o == 18) {
            return "DIAL_MODIFIED_TO_USSD";
        }
        if (o == 19) {
            return "DIAL_MODIFIED_TO_SS";
        }
        if (o == 20) {
            return "DIAL_MODIFIED_TO_DIAL";
        }
        if (o == 21) {
            return "USSD_MODIFIED_TO_DIAL";
        }
        if (o == 22) {
            return "USSD_MODIFIED_TO_SS";
        }
        if (o == 23) {
            return "USSD_MODIFIED_TO_USSD";
        }
        if (o == 24) {
            return "SS_MODIFIED_TO_DIAL";
        }
        if (o == 25) {
            return "SS_MODIFIED_TO_USSD";
        }
        if (o == 26) {
            return "SUBSCRIPTION_NOT_SUPPORTED";
        }
        if (o == 27) {
            return "SS_MODIFIED_TO_SS";
        }
        if (o == 36) {
            return "LCE_NOT_SUPPORTED";
        }
        if (o == 37) {
            return "NO_MEMORY";
        }
        if (o == 38) {
            return "INTERNAL_ERR";
        }
        if (o == 39) {
            return "SYSTEM_ERR";
        }
        if (o == 40) {
            return "MODEM_ERR";
        }
        if (o == 41) {
            return "INVALID_STATE";
        }
        if (o == 42) {
            return "NO_RESOURCES";
        }
        if (o == 43) {
            return "SIM_ERR";
        }
        if (o == 44) {
            return "INVALID_ARGUMENTS";
        }
        if (o == 45) {
            return "INVALID_SIM_STATE";
        }
        if (o == 46) {
            return "INVALID_MODEM_STATE";
        }
        if (o == 47) {
            return "INVALID_CALL_ID";
        }
        if (o == 48) {
            return "NO_SMS_TO_ACK";
        }
        if (o == 49) {
            return "NETWORK_ERR";
        }
        if (o == 50) {
            return "REQUEST_RATE_LIMITED";
        }
        if (o == 51) {
            return "SIM_BUSY";
        }
        if (o == 52) {
            return "SIM_FULL";
        }
        if (o == 53) {
            return "NETWORK_REJECT";
        }
        if (o == 54) {
            return "OPERATION_NOT_ALLOWED";
        }
        if (o == 55) {
            return "EMPTY_RECORD";
        }
        if (o == 56) {
            return "INVALID_SMS_FORMAT";
        }
        if (o == 57) {
            return "ENCODING_ERR";
        }
        if (o == 58) {
            return "INVALID_SMSC_ADDRESS";
        }
        if (o == 59) {
            return "NO_SUCH_ENTRY";
        }
        if (o == 60) {
            return "NETWORK_NOT_READY";
        }
        if (o == 61) {
            return "NOT_PROVISIONED";
        }
        if (o == 62) {
            return "NO_SUBSCRIPTION";
        }
        if (o == 63) {
            return "NO_NETWORK_FOUND";
        }
        if (o == 64) {
            return "DEVICE_IN_USE";
        }
        if (o == 65) {
            return "ABORTED";
        }
        if (o == 66) {
            return "INVALID_RESPONSE";
        }
        if (o == 501) {
            return "OEM_ERROR_1";
        }
        if (o == 502) {
            return "OEM_ERROR_2";
        }
        if (o == 503) {
            return "OEM_ERROR_3";
        }
        if (o == 504) {
            return "OEM_ERROR_4";
        }
        if (o == 505) {
            return "OEM_ERROR_5";
        }
        if (o == 506) {
            return "OEM_ERROR_6";
        }
        if (o == 507) {
            return "OEM_ERROR_7";
        }
        if (o == 508) {
            return "OEM_ERROR_8";
        }
        if (o == 509) {
            return "OEM_ERROR_9";
        }
        if (o == 510) {
            return "OEM_ERROR_10";
        }
        if (o == 511) {
            return "OEM_ERROR_11";
        }
        if (o == 512) {
            return "OEM_ERROR_12";
        }
        if (o == 513) {
            return "OEM_ERROR_13";
        }
        if (o == 514) {
            return "OEM_ERROR_14";
        }
        if (o == 515) {
            return "OEM_ERROR_15";
        }
        if (o == 516) {
            return "OEM_ERROR_16";
        }
        if (o == 517) {
            return "OEM_ERROR_17";
        }
        if (o == 518) {
            return "OEM_ERROR_18";
        }
        if (o == 519) {
            return "OEM_ERROR_19";
        }
        if (o == 520) {
            return "OEM_ERROR_20";
        }
        if (o == 521) {
            return "OEM_ERROR_21";
        }
        if (o == 522) {
            return "OEM_ERROR_22";
        }
        if (o == 523) {
            return "OEM_ERROR_23";
        }
        if (o == 524) {
            return "OEM_ERROR_24";
        }
        if (o == 525) {
            return "OEM_ERROR_25";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("RADIO_NOT_AVAILABLE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("GENERIC_FAILURE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("PASSWORD_INCORRECT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SIM_PIN2");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SIM_PUK2");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("REQUEST_NOT_SUPPORTED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("CANCELLED");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("OP_NOT_ALLOWED_DURING_VOICE_CALL");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("OP_NOT_ALLOWED_BEFORE_REG_TO_NW");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("SMS_SEND_FAIL_RETRY");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("SIM_ABSENT");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("SUBSCRIPTION_NOT_AVAILABLE");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("MODE_NOT_SUPPORTED");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("FDN_CHECK_FAILURE");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("ILLEGAL_SIM_OR_ME");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("MISSING_RESOURCE");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("NO_SUCH_ELEMENT");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("DIAL_MODIFIED_TO_USSD");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("DIAL_MODIFIED_TO_SS");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("DIAL_MODIFIED_TO_DIAL");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("USSD_MODIFIED_TO_DIAL");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("USSD_MODIFIED_TO_SS");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("USSD_MODIFIED_TO_USSD");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("SS_MODIFIED_TO_DIAL");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("SS_MODIFIED_TO_USSD");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("SUBSCRIPTION_NOT_SUPPORTED");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("SS_MODIFIED_TO_SS");
            flipped |= 27;
        }
        if ((o & 36) == 36) {
            list.add("LCE_NOT_SUPPORTED");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("NO_MEMORY");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("INTERNAL_ERR");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("SYSTEM_ERR");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("MODEM_ERR");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("INVALID_STATE");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("NO_RESOURCES");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("SIM_ERR");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("INVALID_ARGUMENTS");
            flipped |= 44;
        }
        if ((o & 45) == 45) {
            list.add("INVALID_SIM_STATE");
            flipped |= 45;
        }
        if ((o & 46) == 46) {
            list.add("INVALID_MODEM_STATE");
            flipped |= 46;
        }
        if ((o & 47) == 47) {
            list.add("INVALID_CALL_ID");
            flipped |= 47;
        }
        if ((o & 48) == 48) {
            list.add("NO_SMS_TO_ACK");
            flipped |= 48;
        }
        if ((o & 49) == 49) {
            list.add("NETWORK_ERR");
            flipped |= 49;
        }
        if ((o & 50) == 50) {
            list.add("REQUEST_RATE_LIMITED");
            flipped |= 50;
        }
        if ((o & 51) == 51) {
            list.add("SIM_BUSY");
            flipped |= 51;
        }
        if ((o & 52) == 52) {
            list.add("SIM_FULL");
            flipped |= 52;
        }
        if ((o & 53) == 53) {
            list.add("NETWORK_REJECT");
            flipped |= 53;
        }
        if ((o & 54) == 54) {
            list.add("OPERATION_NOT_ALLOWED");
            flipped |= 54;
        }
        if ((o & 55) == 55) {
            list.add("EMPTY_RECORD");
            flipped |= 55;
        }
        if ((o & 56) == 56) {
            list.add("INVALID_SMS_FORMAT");
            flipped |= 56;
        }
        if ((o & 57) == 57) {
            list.add("ENCODING_ERR");
            flipped |= 57;
        }
        if ((o & 58) == 58) {
            list.add("INVALID_SMSC_ADDRESS");
            flipped |= 58;
        }
        if ((o & 59) == 59) {
            list.add("NO_SUCH_ENTRY");
            flipped |= 59;
        }
        if ((o & 60) == 60) {
            list.add("NETWORK_NOT_READY");
            flipped |= 60;
        }
        if ((o & 61) == 61) {
            list.add("NOT_PROVISIONED");
            flipped |= 61;
        }
        if ((o & 62) == 62) {
            list.add("NO_SUBSCRIPTION");
            flipped |= 62;
        }
        if ((o & 63) == 63) {
            list.add("NO_NETWORK_FOUND");
            flipped |= 63;
        }
        if ((o & 64) == 64) {
            list.add("DEVICE_IN_USE");
            flipped |= 64;
        }
        if ((o & 65) == 65) {
            list.add("ABORTED");
            flipped |= 65;
        }
        if ((o & 66) == 66) {
            list.add("INVALID_RESPONSE");
            flipped |= 66;
        }
        if ((o & OEM_ERROR_1) == 501) {
            list.add("OEM_ERROR_1");
            flipped |= OEM_ERROR_1;
        }
        if ((o & OEM_ERROR_2) == 502) {
            list.add("OEM_ERROR_2");
            flipped |= OEM_ERROR_2;
        }
        if ((o & OEM_ERROR_3) == 503) {
            list.add("OEM_ERROR_3");
            flipped |= OEM_ERROR_3;
        }
        if ((o & OEM_ERROR_4) == 504) {
            list.add("OEM_ERROR_4");
            flipped |= OEM_ERROR_4;
        }
        if ((o & OEM_ERROR_5) == 505) {
            list.add("OEM_ERROR_5");
            flipped |= OEM_ERROR_5;
        }
        if ((o & OEM_ERROR_6) == 506) {
            list.add("OEM_ERROR_6");
            flipped |= OEM_ERROR_6;
        }
        if ((o & OEM_ERROR_7) == 507) {
            list.add("OEM_ERROR_7");
            flipped |= OEM_ERROR_7;
        }
        if ((o & OEM_ERROR_8) == 508) {
            list.add("OEM_ERROR_8");
            flipped |= OEM_ERROR_8;
        }
        if ((o & OEM_ERROR_9) == 509) {
            list.add("OEM_ERROR_9");
            flipped |= OEM_ERROR_9;
        }
        if ((o & OEM_ERROR_10) == 510) {
            list.add("OEM_ERROR_10");
            flipped |= OEM_ERROR_10;
        }
        if ((o & OEM_ERROR_11) == 511) {
            list.add("OEM_ERROR_11");
            flipped |= OEM_ERROR_11;
        }
        if ((o & 512) == 512) {
            list.add("OEM_ERROR_12");
            flipped |= 512;
        }
        if ((o & OEM_ERROR_13) == 513) {
            list.add("OEM_ERROR_13");
            flipped |= OEM_ERROR_13;
        }
        if ((o & OEM_ERROR_14) == 514) {
            list.add("OEM_ERROR_14");
            flipped |= OEM_ERROR_14;
        }
        if ((o & OEM_ERROR_15) == 515) {
            list.add("OEM_ERROR_15");
            flipped |= OEM_ERROR_15;
        }
        if ((o & OEM_ERROR_16) == 516) {
            list.add("OEM_ERROR_16");
            flipped |= OEM_ERROR_16;
        }
        if ((o & OEM_ERROR_17) == 517) {
            list.add("OEM_ERROR_17");
            flipped |= OEM_ERROR_17;
        }
        if ((o & OEM_ERROR_18) == 518) {
            list.add("OEM_ERROR_18");
            flipped |= OEM_ERROR_18;
        }
        if ((o & OEM_ERROR_19) == 519) {
            list.add("OEM_ERROR_19");
            flipped |= OEM_ERROR_19;
        }
        if ((o & OEM_ERROR_20) == 520) {
            list.add("OEM_ERROR_20");
            flipped |= OEM_ERROR_20;
        }
        if ((o & OEM_ERROR_21) == 521) {
            list.add("OEM_ERROR_21");
            flipped |= OEM_ERROR_21;
        }
        if ((o & OEM_ERROR_22) == 522) {
            list.add("OEM_ERROR_22");
            flipped |= OEM_ERROR_22;
        }
        if ((o & OEM_ERROR_23) == 523) {
            list.add("OEM_ERROR_23");
            flipped |= OEM_ERROR_23;
        }
        if ((o & OEM_ERROR_24) == 524) {
            list.add("OEM_ERROR_24");
            flipped |= OEM_ERROR_24;
        }
        if ((o & OEM_ERROR_25) == 525) {
            list.add("OEM_ERROR_25");
            flipped |= OEM_ERROR_25;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
