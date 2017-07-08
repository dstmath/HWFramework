package com.android.internal.telephony.cdma.sms;

import android.util.SparseBooleanArray;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.util.HexDump;

public class CdmaSmsAddress extends SmsAddress {
    public static final int DIGIT_MODE_4BIT_DTMF = 0;
    public static final int DIGIT_MODE_8BIT_CHAR = 1;
    public static final int NUMBERING_PLAN_ISDN_TELEPHONY = 1;
    public static final int NUMBERING_PLAN_UNKNOWN = 0;
    public static final int NUMBER_MODE_DATA_NETWORK = 1;
    public static final int NUMBER_MODE_NOT_DATA_NETWORK = 0;
    private static boolean PLUS_TRANFER_IN_AP = false;
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
    private static final SparseBooleanArray numericCharDialableMap = null;
    private static final char[] numericCharsDialable = null;
    private static final char[] numericCharsSugar = null;
    public int digitMode;
    public int numberMode;
    public int numberOfDigits;
    public int numberPlan;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.sms.CdmaSmsAddress.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.sms.CdmaSmsAddress.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.sms.CdmaSmsAddress.<clinit>():void");
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
        for (int i = NUMBER_MODE_NOT_DATA_NETWORK; i < digits; i += TON_INTERNATIONAL_OR_IP) {
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
        for (int i = NUMBER_MODE_NOT_DATA_NETWORK; i < len; i += TON_INTERNATIONAL_OR_IP) {
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
        for (int i = NUMBER_MODE_NOT_DATA_NETWORK; i < len; i += TON_INTERNATIONAL_OR_IP) {
            char c = address.charAt(i);
            if (!(c == ' ' || c == '\r' || c == '\n' || c == '\t')) {
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
        addr.ton = NUMBER_MODE_NOT_DATA_NETWORK;
        byte[] origBytes = null;
        String filteredAddr = filterNumericSugar(address);
        if (filteredAddr != null) {
            origBytes = parseToDtmf(filteredAddr);
        }
        if (origBytes != null) {
            addr.digitMode = NUMBER_MODE_NOT_DATA_NETWORK;
            addr.numberMode = NUMBER_MODE_NOT_DATA_NETWORK;
            if (address.indexOf(43) != -1) {
                addr.ton = TON_INTERNATIONAL_OR_IP;
            }
        } else {
            origBytes = UserData.stringToAscii(filterWhitespace(address));
            if (origBytes == null) {
                return null;
            }
            addr.digitMode = TON_INTERNATIONAL_OR_IP;
            addr.numberMode = TON_INTERNATIONAL_OR_IP;
            if (address.indexOf(64) != -1) {
                addr.ton = TON_NATIONAL_OR_EMAIL;
            }
        }
        addr.origBytes = origBytes;
        addr.numberOfDigits = origBytes.length;
        return addr;
    }

    private static CdmaSmsAddress parseForQucalcom(String address) {
        CdmaSmsAddress addr = new CdmaSmsAddress();
        addr.address = address;
        addr.digitMode = NUMBER_MODE_NOT_DATA_NETWORK;
        addr.ton = NUMBER_MODE_NOT_DATA_NETWORK;
        addr.numberMode = NUMBER_MODE_NOT_DATA_NETWORK;
        addr.numberPlan = NUMBER_MODE_NOT_DATA_NETWORK;
        byte[] bArr = null;
        if (address.indexOf(43) != -1) {
            addr.digitMode = TON_INTERNATIONAL_OR_IP;
            addr.ton = TON_INTERNATIONAL_OR_IP;
            addr.numberMode = NUMBER_MODE_NOT_DATA_NETWORK;
            addr.numberPlan = TON_INTERNATIONAL_OR_IP;
        }
        if (address.indexOf(64) != -1) {
            addr.digitMode = TON_INTERNATIONAL_OR_IP;
            addr.ton = TON_NATIONAL_OR_EMAIL;
            addr.numberMode = TON_INTERNATIONAL_OR_IP;
        }
        String filteredAddr = filterNumericSugar(address);
        if (addr.digitMode == 0) {
            if (filteredAddr != null) {
                bArr = parseToDtmf(filteredAddr);
            }
            if (bArr == null) {
                addr.digitMode = TON_INTERNATIONAL_OR_IP;
            }
        }
        if (addr.digitMode == TON_INTERNATIONAL_OR_IP) {
            if (filteredAddr == null) {
                filteredAddr = filterWhitespace(address);
            }
            bArr = UserData.stringToAscii(filteredAddr);
            if (bArr == null) {
                return null;
            }
        }
        addr.origBytes = bArr;
        addr.numberOfDigits = bArr.length;
        return addr;
    }
}
