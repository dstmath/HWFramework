package com.android.server.security.trustcircle.utils;

import android.text.TextUtils;
import com.android.server.display.HwUibcReceiver;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.security.trustcircle.tlv.command.auth.RET_AUTH_SYNC;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.util.Locale;

public class ByteUtil {
    public static final int BYTE_SIZE = 1;
    public static final String CHECK_TABLE = "0123456789ABCDEFabcdef";
    public static final String HEX_TABLE = "0123456789ABCDEF";
    public static final int INT_SIZE = 4;
    public static final int LONG_SIZE = 8;
    public static final int SHORT_SIZE = 2;
    public static final String TAG = "ByteUtil";

    public static String byte2HexString(byte raw) {
        return HEX_TABLE.charAt((raw & 240) >> 4) + HEX_TABLE.charAt(raw & HwUibcReceiver.CurrentPacket.INPUT_MASK);
    }

    public static String short2HexString(short raw) {
        return HEX_TABLE.charAt((61440 & raw) >> 12) + HEX_TABLE.charAt((raw & RET_AUTH_SYNC.TAG_AUTH_ID) >> 8) + HEX_TABLE.charAt((raw & 240) >> 4) + HEX_TABLE.charAt(raw & 15);
    }

    public static Byte[] hexString2ByteArray(String hexString) {
        int c = 0;
        if (!isLegalHexString(hexString)) {
            return new Byte[0];
        }
        StringBuffer sb = new StringBuffer(hexString);
        if (sb.length() % 2 != 0) {
            sb.insert(0, "0");
        }
        String hexs = sb.toString();
        Byte[] res = new Byte[(hexs.length() / 2)];
        char[] chs = hexs.toCharArray();
        int i = 0;
        while (i < chs.length) {
            try {
                res[c] = Byte.valueOf((byte) Integer.parseInt(new String(chs, i, 2), 16));
            } catch (NumberFormatException e) {
                LogHelper.e(TAG, "NumberFormatException in hexString2ByteArray");
            }
            i += 2;
            c++;
        }
        return res;
    }

    public static byte[] hexString2byteArray(String hexString) {
        return unboxByteArray(hexString2ByteArray(hexString));
    }

    public static boolean isLegalHexString(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        int length = input.length();
        if (length <= 1 || length % 2 != 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!CHECK_TABLE.contains(input.substring(i, i + 1))) {
                LogHelper.e(TAG, "input string is not hex string, at position:" + (i + 1) + "value: " + s);
                return false;
            }
        }
        return true;
    }

    public static Byte[] short2ByteArray(short raw) {
        return hexString2ByteArray(short2HexString(raw));
    }

    public static Byte[] int2ByteArray(int raw) {
        return hexString2ByteArray(int2StrictHexString(raw));
    }

    public static String byteArray2HexString(Byte[] raw) {
        if (raw == null || raw.length == 0) {
            return null;
        }
        StringBuilder hex = new StringBuilder();
        for (Byte byteValue : raw) {
            hex.append(byte2HexString(byteValue.byteValue()));
        }
        return hex.toString();
    }

    public static String byteArrayOri2HexString(byte[] raw) {
        return byteArray2HexString(boxbyteArray(raw));
    }

    public static Integer byteArray2Int(Byte[] raw) {
        int result = -1;
        if (raw == null || raw.length == 0 || raw.length != 4) {
            StringBuilder sb = new StringBuilder();
            sb.append("error: wrong int byte array size: ");
            sb.append(raw == null ? "0" : Integer.valueOf(raw.length));
            LogHelper.e(TAG, sb.toString());
            return -1;
        }
        try {
            result = Integer.parseInt(byteArray2HexString(raw), 16);
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "NumberFormatException in byteArray2Int");
        }
        return Integer.valueOf(result);
    }

    public static Short byteArray2Short(Byte[] raw) {
        short result = -1;
        if (raw == null || raw.length == 0 || raw.length != 2) {
            StringBuilder sb = new StringBuilder();
            sb.append("error: wrong short byte array size ");
            sb.append(raw == null ? "0" : Integer.valueOf(raw.length));
            LogHelper.e(TAG, sb.toString());
            return -1;
        }
        try {
            result = Short.parseShort(byteArray2HexString(raw), 16);
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "NumberFormatException in byteArray2Short");
        }
        return Short.valueOf(result);
    }

    public static Long byteArray2Long(Byte[] raw) {
        long result = -1;
        if (raw == null || raw.length == 0 || raw.length != 8) {
            StringBuilder sb = new StringBuilder();
            sb.append("error: wrong long byte array size ");
            sb.append(raw == null ? "0" : Integer.valueOf(raw.length));
            LogHelper.e(TAG, sb.toString());
            return -1L;
        }
        try {
            result = Long.parseLong(byteArray2HexString(raw), 16);
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "NumberFormatException in byteArray2Long");
        }
        return Long.valueOf(result);
    }

    public static <T> String getStringFormatOfTypeContent(T obj) {
        if (obj == null) {
            LogHelper.e(TAG, "getStringFormatOfTypeContent: input type is mull");
            return null;
        }
        StringBuffer sb = new StringBuffer();
        if (obj instanceof Byte[]) {
            byte[] bytes = unboxByteArray((Byte[]) obj);
            sb.append("{");
            for (int i = 0; i < bytes.length; i++) {
                sb.append(bytes[i]);
                if (i != bytes.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        } else {
            sb.append(obj.toString());
            sb.append(" ");
        }
        return sb.toString();
    }

    public static <T> String printObjContent(T obj) {
        if (obj == null) {
            LogHelper.e(TAG, "printObjContent: input type is mull");
            return null;
        }
        StringBuffer sb = new StringBuffer();
        if (obj instanceof Byte[]) {
            for (byte b : (byte[]) obj) {
                sb.append(b);
                sb.append(" ");
            }
        } else {
            sb.append(obj.toString());
            sb.append(" ");
        }
        return sb.toString();
    }

    public static <T> Class getType(T t) {
        if (t == null) {
            return null;
        }
        if (t instanceof Integer) {
            return Integer.class;
        }
        if (t instanceof Long) {
            return Long.class;
        }
        if (t instanceof Short) {
            return Short.class;
        }
        if (t instanceof Byte[]) {
            return Byte[].class;
        }
        if (t instanceof TLVTree.TLVRootTree) {
            return TLVTree.TLVRootTree.class;
        }
        if (t instanceof TLVTree.TLVChildTree) {
            return TLVTree.TLVChildTree.class;
        }
        LogHelper.e(TAG, "getType: unsupported type " + t.getClass().getSimpleName());
        return null;
    }

    public static <T> Byte[] type2ByteArray(T t) {
        if (t == null) {
            LogHelper.e(TAG, "type2ByteArray: input is null");
            return new Byte[0];
        } else if (t instanceof Byte[]) {
            return (Byte[]) t;
        } else {
            if (t instanceof Short) {
                return hexString2ByteArray(type2HexString((Short) t));
            }
            if (t instanceof Integer) {
                return hexString2ByteArray(type2HexString((Integer) t));
            }
            if (t instanceof Long) {
                return hexString2ByteArray(type2HexString((Long) t));
            }
            if (t instanceof TLVTree) {
                return ((TLVTree) t).encapsulate();
            }
            if (t instanceof String) {
                return hexString2ByteArray(type2HexString((String) t));
            }
            LogHelper.e(TAG, "type2ByteArray: unsupported type " + t.getClass().getSimpleName());
            return new Byte[0];
        }
    }

    public static <T> String type2HexString(T t) {
        if (t == null) {
            LogHelper.e(TAG, "type2HexString: input is null");
            return null;
        } else if (t instanceof Short) {
            return short2HexString(((Short) t).shortValue());
        } else {
            if (t instanceof Byte[]) {
                return byteArray2HexString((Byte[]) t);
            }
            if (t instanceof Integer) {
                return int2StrictHexString(((Integer) t).intValue());
            }
            if (t instanceof Long) {
                return long2StrictHexString(((Long) t).longValue());
            }
            if (t instanceof String) {
                return (String) t;
            }
            if (t instanceof TLVTree) {
                return TLVTree.class.getSimpleName();
            }
            LogHelper.e(TAG, "type2HexString: unsupported type " + t.getClass().getSimpleName());
            return null;
        }
    }

    public static String int2StrictHexString(int src) {
        StringBuffer sb = new StringBuffer(Integer.toHexString(src));
        while (sb.length() < 8) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    public static String long2StrictHexString(long src) {
        StringBuffer sb = new StringBuffer(Long.toHexString(src));
        while (sb.length() < 16) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    public static <T> int getSizeofType(T t) {
        if (t == null) {
            return 0;
        }
        Class clazz = t.getClass();
        if (clazz == Integer.class) {
            return 4;
        }
        if (clazz == Long.class) {
            return 8;
        }
        if (clazz == Short.class) {
            return 2;
        }
        if (clazz == Byte[].class) {
            return ((byte[]) t).length;
        }
        return -1;
    }

    public static byte[] unboxByteArray(Byte[] oBytes) {
        if (oBytes == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i].byteValue();
        }
        return bytes;
    }

    public static Byte[] boxbyteArray(byte[] bytesPrim) {
        int i = 0;
        if (bytesPrim == null) {
            return new Byte[0];
        }
        Byte[] bytes = new Byte[bytesPrim.length];
        int i2 = 0;
        int length = bytesPrim.length;
        while (i < length) {
            bytes[i2] = Byte.valueOf(bytesPrim[i]);
            i++;
            i2++;
        }
        return bytes;
    }

    public static String byteArray2ServerHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex.toLowerCase(Locale.US));
        }
        return sb.toString();
    }

    public static String byteArray2ServerHexString(Byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (Byte byteValue : bytes) {
            String hex = Integer.toHexString(byteValue.byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex.toLowerCase(Locale.US));
        }
        return sb.toString();
    }

    public static Byte[] serverHexString2ByteArray(String string) {
        return hexString2ByteArray(string);
    }

    public static Byte[] longToByteArray(long value) {
        long temp = value;
        Byte[] b = new Byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = Byte.valueOf((byte) ((int) Long.valueOf(255 & temp).longValue()));
            temp >>= 8;
        }
        return b;
    }

    public static long byteArrayToLongDirect(Byte[] b) {
        Byte[] bArr = b;
        if (bArr == null || bArr.length != 8) {
            return 0;
        }
        long s0 = (long) (bArr[0].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long s1 = (long) (bArr[1].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long s2 = (long) (bArr[2].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long s3 = (long) (bArr[3].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long s4 = (long) (bArr[4].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long s5 = (long) (bArr[5].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
        long j = s0 | (s1 << 8) | (s2 << 16) | (s3 << 24) | (s4 << 32) | (s5 << 40);
        return j | (((long) (bArr[6].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY)) << 48) | (((long) (bArr[7].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY)) << 56);
    }
}
