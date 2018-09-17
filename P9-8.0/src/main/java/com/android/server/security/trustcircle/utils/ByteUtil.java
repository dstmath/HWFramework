package com.android.server.security.trustcircle.utils;

import android.text.TextUtils;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVChildTree;
import com.android.server.security.trustcircle.tlv.core.TLVTree.TLVRootTree;
import java.util.Locale;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.ConstS32;

public class ByteUtil {
    public static final int BYTE_SIZE = 1;
    public static final String CHECK_TABLE = "0123456789ABCDEFabcdef";
    public static final String HEX_TABLE = "0123456789ABCDEF";
    public static final int INT_SIZE = 4;
    public static final int LONG_SIZE = 8;
    public static final int SHORT_SIZE = 2;
    public static final String TAG = "ByteUtil";

    public static String byte2HexString(byte raw) {
        return HEX_TABLE.charAt((raw & 240) >> 4) + HEX_TABLE.charAt(raw & 15);
    }

    public static String short2HexString(short raw) {
        return HEX_TABLE.charAt((ConstS32.HIGH_BITS_WCG_MARK & raw) >> 12) + HEX_TABLE.charAt((raw & ConstS32.HIGH_BITS_AL_MARK) >> 8) + HEX_TABLE.charAt((raw & 240) >> 4) + HEX_TABLE.charAt(raw & 15);
    }

    public static Byte[] hexString2ByteArray(String hexString) {
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
        int c = 0;
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
        int i = 0;
        while (i < length) {
            String s = input.substring(i, i + 1);
            if (CHECK_TABLE.contains(s)) {
                i++;
            } else {
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
        if (raw == null || raw.length == 0 || raw.length != 4) {
            LogHelper.e(TAG, "error: wrong int byte array size: " + (raw == null ? "0" : Integer.valueOf(raw.length)));
            return Integer.valueOf(-1);
        }
        int result = -1;
        try {
            result = Integer.parseInt(byteArray2HexString(raw), 16);
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "NumberFormatException in byteArray2Int");
        }
        return Integer.valueOf(result);
    }

    public static Short byteArray2Short(Byte[] raw) {
        if (raw == null || raw.length == 0 || raw.length != 2) {
            LogHelper.e(TAG, "error: wrong short byte array size " + (raw == null ? "0" : Integer.valueOf(raw.length)));
            return Short.valueOf((short) -1);
        }
        short result = (short) -1;
        try {
            result = Short.parseShort(byteArray2HexString(raw), 16);
        } catch (NumberFormatException e) {
            LogHelper.e(TAG, "NumberFormatException in byteArray2Short");
        }
        return Short.valueOf(result);
    }

    public static Long byteArray2Long(Byte[] raw) {
        if (raw == null || raw.length == 0 || raw.length != 8) {
            LogHelper.e(TAG, "error: wrong long byte array size " + (raw == null ? "0" : Integer.valueOf(raw.length)));
            return Long.valueOf(-1);
        }
        long result = -1;
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
            sb.append(obj.toString()).append(" ");
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
                sb.append(b).append(" ");
            }
        } else {
            sb.append(obj.toString()).append(" ");
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
        if (t instanceof TLVRootTree) {
            return TLVRootTree.class;
        }
        if (t instanceof TLVChildTree) {
            return TLVChildTree.class;
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
        int length = bytesPrim.length;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            bytes[i2] = Byte.valueOf(bytesPrim[i]);
            i++;
            i2 = i3;
        }
        return bytes;
    }

    public static String byteArray2ServerHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 255);
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
            String hex = Integer.toHexString(byteValue.byteValue() & 255);
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
        if (b == null || b.length != 8) {
            return 0;
        }
        long byteValue = ((long) (b[0].byteValue() & 255)) | (((long) (b[1].byteValue() & 255)) << 8);
        byteValue |= ((long) (b[2].byteValue() & 255)) << 16;
        byteValue |= ((long) (b[3].byteValue() & 255)) << 24;
        byteValue |= ((long) (b[4].byteValue() & 255)) << 32;
        byteValue |= ((long) (b[5].byteValue() & 255)) << 40;
        return (byteValue | (((long) (b[6].byteValue() & 255)) << 48)) | (((long) (b[7].byteValue() & 255)) << 56);
    }
}
