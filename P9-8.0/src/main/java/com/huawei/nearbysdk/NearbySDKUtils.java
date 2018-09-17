package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;

public class NearbySDKUtils {
    private static final int DataInt = 1;
    private static final int InstantMessageInt = 3;
    private static final int StreamingInt = 4;
    private static final int TokenInt = 2;

    public static BusinessTypeEnum getEnumFromInt(int businessType) {
        BusinessTypeEnum businessTypeEnum = BusinessTypeEnum.Token;
        switch (businessType) {
            case 1:
                return BusinessTypeEnum.Data;
            case 2:
                return BusinessTypeEnum.Token;
            case 3:
                return BusinessTypeEnum.InstantMessage;
            case 4:
                return BusinessTypeEnum.Streaming;
            default:
                return businessTypeEnum;
        }
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static byte[] jointByteArrays(byte[]... args) {
        if (args == null || args.length == 0) {
            return new byte[0];
        }
        if (args.length == 1) {
            return args[0];
        }
        byte[] barr;
        int lengthSum = 0;
        for (byte[] barr2 : args) {
            if (barr2 == null) {
                barr2 = new byte[0];
            }
            lengthSum += barr2.length;
        }
        byte[] res = new byte[lengthSum];
        int offset = 0;
        for (byte[] barr22 : args) {
            arraycopy(barr22, 0, res, offset, barr22.length);
            offset += barr22.length;
        }
        return res;
    }

    public static byte[] Int2Byte(int value) {
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            buffer[i] = (byte) ((value >> (32 - ((i + 1) * 8))) & 255);
        }
        return buffer;
    }

    public static int Byte2Int(byte[] buffer) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (buffer[i] & 255);
        }
        return value;
    }
}
