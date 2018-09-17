package com.android.server.security.trustcircle.utils;

import android.text.TextUtils;
import com.android.server.PPPOEStateMachine;
import com.android.server.display.Utils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.security.trustcircle.tlv.core.TLVTree;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {
    public static final int BYTE_SIZE = 1;
    public static final String CHECK_TABLE = "0123456789ABCDEFabcdef";
    public static final String HEX_TABLE = "0123456789ABCDEF";
    public static final int INT_SIZE = 4;
    public static final int LONG_SIZE = 8;
    public static final int SHORT_SIZE = 2;
    public static final String TAG = "ByteUtil";

    public static String byte2HexString(byte raw) {
        return HEX_TABLE.charAt((raw & 240) >> INT_SIZE) + HEX_TABLE.charAt(raw & 15);
    }

    public static String short2HexString(short raw) {
        return HEX_TABLE.charAt((61440 & raw) >> 12) + HEX_TABLE.charAt((raw & 3840) >> LONG_SIZE) + HEX_TABLE.charAt((raw & 240) >> INT_SIZE) + HEX_TABLE.charAt(raw & 15);
    }

    public static Byte[] hexString2ByteArray(String hexString) {
        if (!isLegalHexString(hexString)) {
            return new Byte[0];
        }
        StringBuffer sb = new StringBuffer(hexString);
        if (sb.length() % SHORT_SIZE != 0) {
            sb.insert(0, PPPOEStateMachine.PHASE_DEAD);
        }
        String hexs = sb.toString();
        Byte[] res = new Byte[(hexs.length() / SHORT_SIZE)];
        char[] chs = hexs.toCharArray();
        int i = 0;
        int c = 0;
        while (i < chs.length) {
            res[c] = Byte.valueOf((byte) Integer.parseInt(new String(chs, i, SHORT_SIZE), 16));
            i += SHORT_SIZE;
            c += BYTE_SIZE;
        }
        return res;
    }

    public static byte[] hexString2byteArray(String hexString) {
        if (!isLegalHexString(hexString)) {
            return new byte[0];
        }
        StringBuffer sb = new StringBuffer(hexString);
        if (sb.length() % SHORT_SIZE != 0) {
            sb.insert(0, PPPOEStateMachine.PHASE_DEAD);
        }
        String hexs = sb.toString();
        byte[] res = new byte[(hexs.length() / SHORT_SIZE)];
        char[] chs = hexs.toCharArray();
        int i = 0;
        int c = 0;
        while (i < chs.length) {
            res[c] = (byte) Integer.parseInt(new String(chs, i, SHORT_SIZE), 16);
            i += SHORT_SIZE;
            c += BYTE_SIZE;
        }
        return res;
    }

    public static boolean isLegalHexString(String input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        int length = input.length();
        if (length <= BYTE_SIZE || length % SHORT_SIZE != 0) {
            return false;
        }
        int i = 0;
        while (i < length) {
            String s = input.substring(i, i + BYTE_SIZE);
            if (CHECK_TABLE.contains(s)) {
                i += BYTE_SIZE;
            } else {
                LogHelper.e(TAG, "input string is not hex string, at position:" + (i + BYTE_SIZE) + "value: " + s);
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
        int length = raw.length;
        for (int i = 0; i < length; i += BYTE_SIZE) {
            hex.append(byte2HexString(raw[i].byteValue()));
        }
        return hex.toString();
    }

    public static Integer byteArray2Int(Byte[] raw) {
        if (raw != null && raw.length != 0 && raw.length == INT_SIZE) {
            return Integer.valueOf(Integer.parseInt(byteArray2HexString(raw), 16));
        }
        LogHelper.e(TAG, "error: wrong int byte array size: " + (raw == null ? PPPOEStateMachine.PHASE_DEAD : Integer.valueOf(raw.length)));
        return Integer.valueOf(-1);
    }

    public static Short byteArray2Short(Byte[] raw) {
        if (raw != null && raw.length != 0 && raw.length == SHORT_SIZE) {
            return Short.valueOf(Short.parseShort(byteArray2HexString(raw), 16));
        }
        LogHelper.e(TAG, "error: wrong short byte array size " + (raw == null ? PPPOEStateMachine.PHASE_DEAD : Integer.valueOf(raw.length)));
        return Short.valueOf((short) -1);
    }

    public static Long byteArray2Long(Byte[] raw) {
        if (raw != null && raw.length != 0 && raw.length == LONG_SIZE) {
            return Long.valueOf(Long.parseLong(byteArray2HexString(raw), 16));
        }
        LogHelper.e(TAG, "error: wrong long byte array size " + (raw == null ? PPPOEStateMachine.PHASE_DEAD : Integer.valueOf(raw.length)));
        return Long.valueOf(-1);
    }

    public static <T> TLVTree byteArray2Tree(Byte[] raw, T t) {
        if (!(t instanceof TLVTree)) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(unboxByteArray(raw));
        buffer.order(ByteOrder.BIG_ENDIAN);
        TLVTree tLVTree = null;
        try {
            tLVTree = (TLVTree) t.getClass().newInstance();
        } catch (ReflectiveOperationException e) {
            LogHelper.e(TAG, "exception when newInstance \n" + e.getMessage());
        }
        tLVTree.parse(buffer);
        return tLVTree;
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
            for (int i = 0; i < bytes.length; i += BYTE_SIZE) {
                sb.append(bytes[i]);
                if (i != bytes.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        } else {
            sb.append(obj.toString() + " ");
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
            byte[] bytes = (byte[]) obj;
            int length = bytes.length;
            for (int i = 0; i < length; i += BYTE_SIZE) {
                sb.append(bytes[i]).append(" ");
            }
        } else {
            sb.append(obj.toString() + " ");
        }
        return sb.toString();
    }

    public static <T> T byteArray2Type(Byte[] raw, T t) {
        if (raw == null || raw.length == 0 || t == null) {
            LogHelper.e(TAG, "byteArray2Type : byte array is " + (raw == null ? HwCertification.SIGNATURE_DEFAULT : "empty"));
            return null;
        } else if (t instanceof Integer) {
            return byteArray2Int(raw);
        } else {
            if (t instanceof Long) {
                return byteArray2Long(raw);
            }
            if (t instanceof Short) {
                return byteArray2Short(raw);
            }
            if (t instanceof Byte[]) {
                return raw;
            }
            if (t instanceof TLVTree) {
                return byteArray2Tree(raw, t);
            }
            LogHelper.e(TAG, "byteArray2Type: unsupported type " + t.getClass().getSimpleName());
            return null;
        }
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
        if (t instanceof TLVTree) {
            return TLVTree.class;
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
            return null;
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
            LogHelper.e(TAG, "type2HexString: unsupported type " + t.getClass().getSimpleName());
            return null;
        }
    }

    public static String int2StrictHexString(int src) {
        StringBuffer sb = new StringBuffer(Integer.toHexString(src));
        while (sb.length() < LONG_SIZE) {
            sb.insert(0, PPPOEStateMachine.PHASE_DEAD);
        }
        return sb.toString();
    }

    public static String long2StrictHexString(long src) {
        StringBuffer sb = new StringBuffer(Long.toHexString(src));
        while (sb.length() < 16) {
            sb.insert(0, PPPOEStateMachine.PHASE_DEAD);
        }
        return sb.toString();
    }

    public static <T> int getSizeofType(T t) {
        if (t == null) {
            return 0;
        }
        Class clazz = t.getClass();
        if (clazz == Integer.class) {
            return INT_SIZE;
        }
        if (clazz == Long.class) {
            return LONG_SIZE;
        }
        if (clazz == Short.class) {
            return SHORT_SIZE;
        }
        if (clazz == Byte[].class) {
            return ((byte[]) t).length;
        }
        return -1;
    }

    public static byte[] unboxByteArray(Byte[] oBytes) {
        if (oBytes == null) {
            return null;
        }
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i += BYTE_SIZE) {
            bytes[i] = oBytes[i].byteValue();
        }
        return bytes;
    }

    public static Byte[] boxbyteArray(byte[] bytesPrim) {
        if (bytesPrim == null) {
            return null;
        }
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        int length = bytesPrim.length;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + BYTE_SIZE;
            bytes[i2] = Byte.valueOf(bytesPrim[i]);
            i += BYTE_SIZE;
            i2 = i3;
        }
        return bytes;
    }

    public static String byteArray2ServerHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i += BYTE_SIZE) {
            String hex = Integer.toHexString(bytes[i] & Utils.MAXINUM_TEMPERATURE);
            if (hex.length() == BYTE_SIZE) {
                sb.append(PPPOEStateMachine.PHASE_DEAD);
            }
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }

    public static String byteArray2ServerHexString(Byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i += BYTE_SIZE) {
            String hex = Integer.toHexString(bytes[i].byteValue() & Utils.MAXINUM_TEMPERATURE);
            if (hex.length() == BYTE_SIZE) {
                sb.append(PPPOEStateMachine.PHASE_DEAD);
            }
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }

    public static Byte[] serverHexString2ByteArray(String string) {
        return hexString2ByteArray(string);
    }

    public static Byte[] longToByteArray(long value) {
        long temp = value;
        Byte[] b = new Byte[LONG_SIZE];
        for (int i = 0; i < b.length; i += BYTE_SIZE) {
            b[i] = Byte.valueOf(new Long(255 & temp).byteValue());
            temp >>= 8;
        }
        return b;
    }

    public static long byteArrayToLongDirect(Byte[] b) {
        long s0 = (long) (b[0].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s1 = (long) (b[BYTE_SIZE].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s2 = (long) (b[SHORT_SIZE].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s3 = (long) (b[3].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s4 = (long) (b[INT_SIZE].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s5 = (long) (b[5].byteValue() & Utils.MAXINUM_TEMPERATURE);
        long s6 = (long) (b[6].byteValue() & Utils.MAXINUM_TEMPERATURE);
        return ((((((s0 | (s1 << LONG_SIZE)) | (s2 << 16)) | (s3 << 24)) | (s4 << 32)) | (s5 << 40)) | (s6 << 48)) | (((long) (b[7].byteValue() & Utils.MAXINUM_TEMPERATURE)) << 56);
    }
}
