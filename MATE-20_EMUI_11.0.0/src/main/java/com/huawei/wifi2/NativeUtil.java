package com.huawei.wifi2;

import android.text.TextUtils;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import libcore.util.HexEncoding;

public class NativeUtil {
    private static final byte[] ANY_MAC_BYTES = {0, 0, 0, 0, 0, 0};
    private static final String ANY_MAC_STR = "any";
    private static final int MAC_LENGTH = 6;
    private static final int MAC_STR_LENGTH = 17;
    private static final String QUOTE = "\"";
    public static final int SCAN_TYPE_HIGH_ACCURACY = 2;
    public static final int SCAN_TYPE_LOW_LATENCY = 0;
    public static final int SCAN_TYPE_LOW_POWER = 1;
    private static final int SSID_BYTES_MAX_LEN = 32;
    private static final String TAG = "NativeUtil";
    private static final int VALUE = 2;

    public static String macAddressFromByteArray(byte[] macArray) throws IllegalArgumentException {
        if (macArray == null) {
            throw new IllegalArgumentException("null mac bytes");
        } else if (macArray.length == MAC_LENGTH) {
            StringBuilder sb = new StringBuilder((int) MAC_STR_LENGTH);
            for (int i = 0; i < macArray.length; i++) {
                if (i != 0) {
                    sb.append(":");
                }
                sb.append(new String(HexEncoding.encode(macArray, i, 1)));
            }
            return sb.toString().toLowerCase(Locale.ENGLISH);
        } else {
            throw new IllegalArgumentException("invalid macArray length: " + macArray.length);
        }
    }

    public static byte[] byteArrayFromArrayList(ArrayList<Byte> bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        byte[] byteArray = new byte[bytes.size()];
        int index = 0;
        Iterator<Byte> it = bytes.iterator();
        while (it.hasNext()) {
            byteArray[index] = it.next().byteValue();
            index++;
        }
        return byteArray;
    }

    public static ArrayList<Byte> byteArrayToArrayList(byte[] bytes) {
        ArrayList<Byte> byteList = new ArrayList<>();
        for (byte b : bytes) {
            byteList.add(Byte.valueOf(b));
        }
        return byteList;
    }

    public static byte[] hexStringToByteArray(String hexStr) throws IllegalArgumentException {
        if (hexStr != null) {
            return HexEncoding.decode(hexStr.toCharArray(), false);
        }
        throw new IllegalArgumentException("null hex string");
    }

    public static ArrayList<Byte> stringToByteArrayList(String str) throws IllegalArgumentException {
        if (str != null) {
            try {
                ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(str));
                byte[] byteArray = new byte[encoded.remaining()];
                encoded.get(byteArray);
                return byteArrayToArrayList(byteArray);
            } catch (CharacterCodingException cce) {
                throw new IllegalArgumentException("cannot be utf-8 encoded", cce);
            }
        } else {
            throw new IllegalArgumentException("null string");
        }
    }

    public static String stringFromByteArrayList(ArrayList<Byte> byteArrayList) throws IllegalArgumentException {
        if (byteArrayList != null) {
            byte[] byteArray = new byte[byteArrayList.size()];
            int index = 0;
            Iterator<Byte> it = byteArrayList.iterator();
            while (it.hasNext()) {
                byteArray[index] = it.next().byteValue();
                index++;
            }
            return new String(byteArray, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("null byte array list");
    }

    public static ArrayList<Byte> decodeSsid(String ssidStr) throws IllegalArgumentException {
        ArrayList<Byte> ssidBytes = hexOrQuotedStringToBytes(ssidStr);
        if (ssidBytes.size() <= SSID_BYTES_MAX_LEN) {
            return ssidBytes;
        }
        throw new IllegalArgumentException("ssid bytes size out of range: " + ssidBytes.size());
    }

    public static String removeEnclosingQuotes(String quotedStr) {
        int length = quotedStr.length();
        if (length >= 2 && quotedStr.charAt(0) == '\"' && quotedStr.charAt(length - 1) == '\"') {
            return quotedStr.substring(1, length - 1);
        }
        return quotedStr;
    }

    public static String addEnclosingQuotes(String str) {
        return QUOTE + str + QUOTE;
    }

    public static byte[] macAddressToByteArray(String macStr) throws IllegalArgumentException {
        if (TextUtils.isEmpty(macStr) || "any".equals(macStr)) {
            return ANY_MAC_BYTES;
        }
        String cleanMac = macStr.replace(":", "");
        if (cleanMac.length() == 12) {
            return HexEncoding.decode(cleanMac.toCharArray(), false);
        }
        throw new IllegalArgumentException("invalid mac string length: " + cleanMac);
    }

    public static ArrayList<Byte> hexOrQuotedStringToBytes(String str) throws IllegalArgumentException {
        if (str != null) {
            int length = str.length();
            if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
                return stringToByteArrayList(str.substring(1, str.length() - 1));
            }
            return byteArrayToArrayList(hexStringToByteArray(str));
        }
        throw new IllegalArgumentException("null string");
    }
}
