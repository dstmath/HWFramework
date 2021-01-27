package com.android.internal.telephony.cdma.sms;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.util.SparseIntArray;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import com.android.internal.util.HexDump;

public class UserData {
    public static final int ASCII_CR_INDEX = 13;
    public static final char[] ASCII_MAP = {' ', '!', '\"', '#', '$', '%', '&', DateFormat.QUOTE, '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', DateFormat.CAPITAL_AM_PM, 'B', 'C', 'D', DateFormat.DAY, 'F', 'G', 'H', 'I', 'J', 'K', DateFormat.STANDALONE_MONTH, DateFormat.MONTH, PhoneNumberUtils.WILD, 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', DateFormat.AM_PM, 'b', 'c', DateFormat.DATE, 'e', 'f', 'g', DateFormat.HOUR, 'i', 'j', DateFormat.HOUR_OF_DAY, 'l', DateFormat.MINUTE, 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', EpicenterTranslateClipReveal.StateProperty.TARGET_X, 'y', DateFormat.TIME_ZONE, '{', '|', '}', '~'};
    public static final int ASCII_MAP_BASE_INDEX = 32;
    public static final int ASCII_MAP_MAX_INDEX = ((ASCII_MAP.length + 32) - 1);
    public static final int ASCII_NL_INDEX = 10;
    public static final int ENCODING_7BIT_ASCII = 2;
    public static final int ENCODING_GSM_7BIT_ALPHABET = 9;
    public static final int ENCODING_GSM_DCS = 10;
    public static final int ENCODING_GSM_DCS_16BIT = 2;
    public static final int ENCODING_GSM_DCS_7BIT = 0;
    public static final int ENCODING_GSM_DCS_8BIT = 1;
    public static final int ENCODING_IA5 = 3;
    public static final int ENCODING_IS91_EXTENDED_PROTOCOL = 1;
    public static final int ENCODING_KOREAN = 6;
    public static final int ENCODING_LATIN = 8;
    public static final int ENCODING_LATIN_HEBREW = 7;
    public static final int ENCODING_OCTET = 0;
    public static final int ENCODING_SHIFT_JIS = 5;
    public static final int ENCODING_UNICODE_16 = 4;
    public static final int IS91_MSG_TYPE_CLI = 132;
    public static final int IS91_MSG_TYPE_SHORT_MESSAGE = 133;
    public static final int IS91_MSG_TYPE_SHORT_MESSAGE_FULL = 131;
    public static final int IS91_MSG_TYPE_VOICEMAIL_STATUS = 130;
    public static final int PRINTABLE_ASCII_MIN_INDEX = 32;
    static final byte UNENCODABLE_7_BIT_CHAR = 32;
    public static final SparseIntArray charToAscii = new SparseIntArray();
    public int msgEncoding;
    public boolean msgEncodingSet = false;
    public int msgType;
    public int numFields;
    public int paddingBits;
    public byte[] payload;
    public String payloadStr;
    public SmsHeader userDataHeader;

    static {
        int i = 0;
        while (true) {
            char[] cArr = ASCII_MAP;
            if (i < cArr.length) {
                charToAscii.put(cArr[i], i + 32);
                i++;
            } else {
                charToAscii.put(10, 10);
                charToAscii.put(13, 13);
                return;
            }
        }
    }

    public static byte[] stringToAscii(String str) {
        int len = str.length();
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            int charCode = charToAscii.get(str.charAt(i), -1);
            if (charCode == -1) {
                return null;
            }
            result[i] = (byte) charCode;
        }
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserData ");
        StringBuilder sb = new StringBuilder();
        sb.append("{ msgEncoding=");
        sb.append(this.msgEncodingSet ? Integer.valueOf(this.msgEncoding) : "unset");
        builder.append(sb.toString());
        builder.append(", msgType=" + this.msgType);
        builder.append(", paddingBits=" + this.paddingBits);
        builder.append(", numFields=" + this.numFields);
        builder.append(", userDataHeader=" + this.userDataHeader);
        builder.append(", payload='" + HexDump.toHexString(this.payload) + "'");
        builder.append(", payloadStr='" + this.payloadStr + "'");
        builder.append(" }");
        return builder.toString();
    }
}
