package com.huawei.device.connectivitychrlog;

public class ByteConvert {
    public static byte[] longToBytes(long n) {
        return new byte[]{(byte) ((int) (n & 255)), (byte) ((int) ((n >> 8) & 255)), (byte) ((int) ((n >> 16) & 255)), (byte) ((int) ((n >> 24) & 255)), (byte) ((int) ((n >> 32) & 255)), (byte) ((int) ((n >> 40) & 255)), (byte) ((int) ((n >> 48) & 255)), (byte) ((int) ((n >> 56) & 255))};
    }

    public static long bytesToLong(byte[] array) {
        return ((((((((((long) array[0]) & 255) << 56) | ((((long) array[1]) & 255) << 48)) | ((((long) array[2]) & 255) << 40)) | ((((long) array[3]) & 255) << 32)) | ((((long) array[4]) & 255) << 24)) | ((((long) array[5]) & 255) << 16)) | ((((long) array[6]) & 255) << 8)) | ((((long) array[7]) & 255) << 0);
    }

    public static long littleEndianbytesToLong(byte[] array) {
        return ((((((((((long) array[7]) & 255) << 56) | ((((long) array[6]) & 255) << 48)) | ((((long) array[8]) & 255) << 40)) | ((((long) array[7]) & 255) << 32)) | ((((long) array[6]) & 255) << 24)) | ((((long) array[5]) & 255) << 16)) | ((((long) array[4]) & 255) << 8)) | ((((long) array[0]) & 255) << 0);
    }

    public static byte[] intToBytes(int n) {
        return new byte[]{(byte) (n & 255), (byte) ((n >> 8) & 255), (byte) ((n >> 16) & 255), (byte) ((n >> 24) & 255)};
    }

    public static int bytesToInt(byte[] b) {
        return (((b[3] & 255) | ((b[2] & 255) << 8)) | ((b[1] & 255) << 16)) | ((b[0] & 255) << 24);
    }

    public static int littleEndianBytesToInt(byte[] b) {
        return (((b[0] & 255) | ((b[1] & 255) << 8)) | ((b[2] & 255) << 16)) | ((b[3] & 255) << 24);
    }

    public static byte[] shortToBytes(short n) {
        return new byte[]{(byte) (n & 255), (byte) ((n >> 8) & 255)};
    }

    public static short bytesToShort(byte[] b) {
        return (short) ((b[1] & 255) | ((b[0] & 255) << 8));
    }

    public static short littleEndianBytesToShort(byte[] b) {
        return (short) ((b[0] & 255) | ((b[1] & 255) << 8));
    }

    public static String littleEndianBytesToString(byte[] b, int len) {
        int data;
        if (1 == len) {
            data = b[0];
        } else if (2 == len) {
            data = littleEndianBytesToShort(b);
        } else if (4 == len) {
            data = littleEndianBytesToInt(b);
        } else {
            data = 0;
        }
        return convertDecimalToHex(data);
    }

    public static byte[] littleEndianBytesToBigEndianBytes(byte[] data, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[i] = data[(len - i) - 1];
        }
        return b;
    }

    public static String convertDecimalToHex(int value) {
        String hexValue = Integer.toHexString(value);
        if (hexValue.equals("") || hexValue.length() == 0) {
            return null;
        }
        return "0x" + hexValue;
    }
}
