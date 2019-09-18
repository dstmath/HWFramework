package com.android.server.wifi.util;

import android.net.wifi.WifiSsid;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import libcore.util.HexEncoding;

public class NativeUtil {
    public static final byte[] ANY_MAC_BYTES = {0, 0, 0, 0, 0, 0};
    private static final String ANY_MAC_STR = "any";
    private static final int MAC_LENGTH = 6;
    private static final int MAC_OUI_LENGTH = 3;
    private static final int MAC_STR_LENGTH = 17;
    private static final String TAG = "NativeUtil";

    public static ArrayList<Byte> stringToByteArrayList(String str) {
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

    public static String stringFromByteArrayList(ArrayList<Byte> byteArrayList) {
        if (byteArrayList != null) {
            byte[] byteArray = new byte[byteArrayList.size()];
            int i = 0;
            Iterator<Byte> it = byteArrayList.iterator();
            while (it.hasNext()) {
                byteArray[i] = it.next().byteValue();
                i++;
            }
            return new String(byteArray, StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("null byte array list");
    }

    public static byte[] stringToByteArray(String str) {
        if (str != null) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("null string");
    }

    public static String stringFromByteArray(byte[] byteArray) {
        if (byteArray != null) {
            return new String(byteArray);
        }
        throw new IllegalArgumentException("null byte array");
    }

    public static byte[] macAddressToByteArray(String macStr) {
        if (TextUtils.isEmpty(macStr) || "any".equals(macStr)) {
            return ANY_MAC_BYTES;
        }
        String cleanMac = macStr.replace(":", "");
        if (cleanMac.length() == 12) {
            return HexEncoding.decode(cleanMac.toCharArray(), false);
        }
        throw new IllegalArgumentException("invalid mac string length: " + cleanMac);
    }

    public static String macAddressFromByteArray(byte[] macArray) {
        if (macArray == null) {
            throw new IllegalArgumentException("null mac bytes");
        } else if (macArray.length == 6) {
            StringBuilder sb = new StringBuilder(17);
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

    public static byte[] macAddressOuiToByteArray(String macStr) {
        if (macStr != null) {
            String cleanMac = macStr.replace(":", "");
            if (cleanMac.length() == 6) {
                return HexEncoding.decode(cleanMac.toCharArray(), false);
            }
            throw new IllegalArgumentException("invalid mac oui string length: " + cleanMac);
        }
        throw new IllegalArgumentException("null mac string");
    }

    public static Long macAddressToLong(byte[] macArray) {
        if (macArray == null) {
            throw new IllegalArgumentException("null mac bytes");
        } else if (macArray.length == 6) {
            try {
                return Long.valueOf(ByteBufferReader.readInteger(ByteBuffer.wrap(macArray), ByteOrder.BIG_ENDIAN, macArray.length));
            } catch (IllegalArgumentException | BufferUnderflowException e) {
                throw new IllegalArgumentException("invalid macArray");
            }
        } else {
            throw new IllegalArgumentException("invalid macArray length: " + macArray.length);
        }
    }

    public static String removeEnclosingQuotes(String quotedStr) {
        int length = quotedStr.length();
        if (length >= 2 && quotedStr.charAt(0) == '\"' && quotedStr.charAt(length - 1) == '\"') {
            return quotedStr.substring(1, length - 1);
        }
        return quotedStr;
    }

    public static String addEnclosingQuotes(String str) {
        return "\"" + str + "\"";
    }

    public static ArrayList<Byte> hexOrQuotedStringToBytes(String str) {
        if (str != null) {
            int length = str.length();
            if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
                return stringToByteArrayList(str.substring(1, str.length() - 1));
            }
            return byteArrayToArrayList(hexStringToByteArray(str));
        }
        throw new IllegalArgumentException("null string");
    }

    public static String bytesToHexOrQuotedString(ArrayList<Byte> bytes) {
        if (bytes != null) {
            byte[] byteArray = byteArrayFromArrayList(bytes);
            if (!bytes.contains((byte) 0)) {
                try {
                    CharBuffer decoded = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(byteArray));
                    return "\"" + decoded.toString() + "\"";
                } catch (CharacterCodingException e) {
                    String ssidDecodeStr = WifiSsid.decodeWithCharsets(byteArray);
                    if (ssidDecodeStr != null) {
                        return ssidDecodeStr;
                    }
                }
            }
            return hexStringFromByteArray(byteArray);
        }
        throw new IllegalArgumentException("null ssid bytes");
    }

    public static String quotedAsciiStringToHex(String ascii, String charsetName) {
        if (TextUtils.isEmpty(ascii)) {
            Log.d(TAG, "quotedAsciiStringToHex: Invalid param.");
            return null;
        }
        try {
            return hexStringFromByteArray(removeEnclosingQuotes(ascii).getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "quotedAsciiStringToHex: Unsupported encoding exception.");
            return null;
        }
    }

    public static ArrayList<Byte> decodeSsid(String ssidStr) {
        return hexOrQuotedStringToBytes(ssidStr);
    }

    public static String encodeSsid(ArrayList<Byte> ssidBytes) {
        return bytesToHexOrQuotedString(ssidBytes);
    }

    public static ArrayList<Byte> byteArrayToArrayList(byte[] bytes) {
        ArrayList<Byte> byteList = new ArrayList<>();
        for (byte valueOf : bytes) {
            byteList.add(Byte.valueOf(valueOf));
        }
        return byteList;
    }

    public static byte[] byteArrayFromArrayList(ArrayList<Byte> bytes) {
        byte[] byteArray = new byte[bytes.size()];
        int i = 0;
        Iterator<Byte> it = bytes.iterator();
        while (it.hasNext()) {
            byteArray[i] = it.next().byteValue();
            i++;
        }
        return byteArray;
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        if (hexStr != null) {
            return HexEncoding.decode(hexStr.toCharArray(), false);
        }
        throw new IllegalArgumentException("null hex string");
    }

    public static String hexStringFromByteArray(byte[] bytes) {
        if (bytes != null) {
            return new String(HexEncoding.encode(bytes)).toLowerCase(Locale.ENGLISH);
        }
        throw new IllegalArgumentException("null hex bytes");
    }

    public static String wpsDevTypeStringFromByteArray(byte[] devType) {
        byte[] a = devType;
        int x = ((a[0] & 255) << 8) | (a[1] & Constants.BYTE_MASK);
        String y = new String(HexEncoding.encode(Arrays.copyOfRange(devType, 2, 6)));
        int z = ((a[6] & 255) << 8) | (a[7] & Constants.BYTE_MASK);
        return String.format(Locale.ENGLISH, "%d-%s-%d", new Object[]{Integer.valueOf(x), y, Integer.valueOf(z)});
    }
}
