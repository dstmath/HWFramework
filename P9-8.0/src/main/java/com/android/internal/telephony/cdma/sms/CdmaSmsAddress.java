package com.android.internal.telephony.cdma.sms;

import android.util.SparseBooleanArray;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.util.HexDump;

public class CdmaSmsAddress extends SmsAddress {
    public static final int DIGIT_MODE_4BIT_DTMF = 0;
    public static final int DIGIT_MODE_8BIT_CHAR = 1;
    public static final int NUMBERING_PLAN_ISDN_TELEPHONY = 1;
    public static final int NUMBERING_PLAN_UNKNOWN = 0;
    public static final int NUMBER_MODE_DATA_NETWORK = 1;
    public static final int NUMBER_MODE_NOT_DATA_NETWORK = 0;
    private static boolean PLUS_TRANFER_IN_AP = (HwModemCapability.isCapabilitySupport(2) ^ 1);
    public static final int SMS_ADDRESS_MAX = 36;
    public static final int SMS_SUBADDRESS_MAX = 36;
    public static final int TON_ABBREVIATED = 6;
    public static final int TON_ALPHANUMERIC = 5;
    public static final int TON_INTERNATIONAL_OR_IP = 1;
    public static final int TON_NATIONAL_OR_EMAIL = 2;
    public static final int TON_NETWORK = 3;
    public static final int TON_RESERVED = 7;
    public static final int TON_SUBSCRIBER = 4;
    public static final int TON_UNKNOWN = 0;
    private static final SparseBooleanArray numericCharDialableMap = new SparseBooleanArray(numericCharsDialable.length + numericCharsSugar.length);
    private static final char[] numericCharsDialable = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '#'};
    private static final char[] numericCharsSugar = new char[]{'(', ')', ' ', '-', '+', '.', '/', '\\'};
    public int digitMode;
    public int numberMode;
    public int numberOfDigits;
    public int numberPlan;

    static {
        for (char put : numericCharsDialable) {
            numericCharDialableMap.put(put, true);
        }
        for (char put2 : numericCharsSugar) {
            numericCharDialableMap.put(put2, false);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CdmaSmsAddress ");
        builder.append("{ digitMode=").append(this.digitMode);
        builder.append(", numberMode=").append(this.numberMode);
        builder.append(", numberPlan=").append(this.numberPlan);
        builder.append(", numberOfDigits=").append(this.numberOfDigits);
        builder.append(", ton=").append(this.ton);
        builder.append(", address=\"").append(this.address).append("\"");
        builder.append(", origBytes=").append(HexDump.toHexString(this.origBytes));
        builder.append(" }");
        return builder.toString();
    }

    private static byte[] parseToDtmf(String address) {
        int digits = address.length();
        byte[] result = new byte[digits];
        for (int i = 0; i < digits; i++) {
            int val;
            char c = address.charAt(i);
            if (c >= '1' && c <= '9') {
                val = c - 48;
            } else if (c == '0') {
                val = 10;
            } else if (c == '*') {
                val = 11;
            } else if (c != '#') {
                return null;
            } else {
                val = 12;
            }
            result[i] = (byte) val;
        }
        return result;
    }

    private static String filterNumericSugar(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();
        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            int mapIndex = numericCharDialableMap.indexOfKey(c);
            if (mapIndex < 0) {
                return null;
            }
            if (numericCharDialableMap.valueAt(mapIndex)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String filterWhitespace(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();
        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            if (!(c == ' ' || c == 13 || c == 10 || c == 9)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static CdmaSmsAddress parse(String address) {
        if (!PLUS_TRANFER_IN_AP) {
            return parseForQucalcom(address);
        }
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.address = address;
        addr.ton = 0;
        byte[] origBytes = null;
        String filteredAddr = filterNumericSugar(address);
        if (filteredAddr != null) {
            origBytes = parseToDtmf(filteredAddr);
        }
        if (origBytes != null) {
            addr.digitMode = 0;
            addr.numberMode = 0;
            if (address.indexOf(43) != -1) {
                addr.ton = 1;
            }
        } else {
            origBytes = UserData.stringToAscii(filterWhitespace(address));
            if (origBytes == null) {
                return null;
            }
            addr.digitMode = 1;
            addr.numberMode = 1;
            if (address.indexOf(64) != -1) {
                addr.ton = 2;
            }
        }
        addr.origBytes = origBytes;
        addr.numberOfDigits = origBytes.length;
        return addr;
    }

    private static CdmaSmsAddress parseForQucalcom(String address) {
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.address = address;
        addr.digitMode = 0;
        addr.ton = 0;
        addr.numberMode = 0;
        addr.numberPlan = 0;
        byte[] origBytes = null;
        if (address.indexOf(43) != -1) {
            addr.digitMode = 1;
            addr.ton = 1;
            addr.numberMode = 0;
            addr.numberPlan = 1;
        }
        if (address.indexOf(64) != -1) {
            addr.digitMode = 1;
            addr.ton = 2;
            addr.numberMode = 1;
        }
        String filteredAddr = filterNumericSugar(address);
        if (addr.digitMode == 0) {
            if (filteredAddr != null) {
                origBytes = parseToDtmf(filteredAddr);
            }
            if (origBytes == null) {
                addr.digitMode = 1;
            }
        }
        if (addr.digitMode == 1) {
            if (filteredAddr == null) {
                filteredAddr = filterWhitespace(address);
            }
            origBytes = UserData.stringToAscii(filteredAddr);
            if (origBytes == null) {
                return null;
            }
        }
        addr.origBytes = origBytes;
        addr.numberOfDigits = origBytes.length;
        return addr;
    }
}
