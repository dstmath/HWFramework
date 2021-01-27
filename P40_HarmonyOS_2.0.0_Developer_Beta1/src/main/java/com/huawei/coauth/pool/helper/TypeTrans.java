package com.huawei.coauth.pool.helper;

import com.huawei.hwpartsecurity.BuildConfig;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class TypeTrans {
    private static final ByteOrder DEFAULT_ORDER = ByteOrder.BIG_ENDIAN;
    private static final int ERROR_RETURN = -1;
    private static final String ESCAPE_SEPARATOR = "\\|\\|";
    private static final int INT_LEN = 4;
    private static final int LONG_LEN = 8;
    private static final String SEPARATOR = "||";

    private TypeTrans() {
    }

    public static boolean bytesToBoolean(byte[] data) {
        if (data != null && bytesToInt(data) == 0) {
            return false;
        }
        return true;
    }

    public static byte[] booleanToBytes(boolean isTrue) {
        int valueInt;
        if (isTrue) {
            valueInt = 1;
        } else {
            valueInt = 0;
        }
        return intToBytes(valueInt);
    }

    public static int bytesToInt(byte[] data) {
        if (data == null) {
            return -1;
        }
        return ByteBuffer.wrap(data).order(DEFAULT_ORDER).getInt();
    }

    public static byte[] intToBytes(int data) {
        return ByteBuffer.allocate(4).order(DEFAULT_ORDER).putInt(data).array();
    }

    public static long bytesToLong(byte[] data) {
        if (data == null) {
            return -1;
        }
        return ByteBuffer.wrap(data).order(DEFAULT_ORDER).getLong();
    }

    public static byte[] longToBytes(long data) {
        return ByteBuffer.allocate(8).order(DEFAULT_ORDER).putLong(data).array();
    }

    public static String bytesToString(byte[] data) {
        if (data == null) {
            return BuildConfig.FLAVOR;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] stringToBytes(String data) {
        if (data == null) {
            return new byte[0];
        }
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static int[] bytesToIntArray(byte[] data) {
        if (data == null) {
            return new int[0];
        }
        int arrayLen = data.length / 4;
        ByteBuffer dataBuffer = ByteBuffer.wrap(data).order(DEFAULT_ORDER);
        int[] result = new int[arrayLen];
        for (int i = 0; i < arrayLen; i++) {
            result[i] = dataBuffer.getInt();
        }
        return result;
    }

    public static byte[] intArrayToBytes(int[] data) {
        if (data == null) {
            return new byte[0];
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(data.length * 4);
        for (int i : data) {
            dataBuffer.putInt(i);
        }
        return dataBuffer.array();
    }

    public static byte[] stringArrayToBytes(String[] data) {
        if (data == null) {
            return new byte[0];
        }
        StringBuilder builder = new StringBuilder();
        for (String str : data) {
            builder.append(str);
            builder.append(SEPARATOR);
        }
        return stringToBytes(builder.toString());
    }

    public static String[] bytesToStringArray(byte[] data) {
        return bytesToString(data).split(ESCAPE_SEPARATOR);
    }
}
