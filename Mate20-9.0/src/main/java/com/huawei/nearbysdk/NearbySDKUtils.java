package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyConfig;

public class NearbySDKUtils {
    private static final int DataInt = 1;
    private static final int InstantMessageInt = 3;
    private static final int StreamingInt = 4;
    private static final int TokenInt = 2;

    public static NearbyConfig.BusinessTypeEnum getEnumFromInt(int businessType) {
        NearbyConfig.BusinessTypeEnum businessTypeEnum = NearbyConfig.BusinessTypeEnum.Token;
        switch (businessType) {
            case 1:
                return NearbyConfig.BusinessTypeEnum.Data;
            case 2:
                return NearbyConfig.BusinessTypeEnum.Token;
            case 3:
                return NearbyConfig.BusinessTypeEnum.InstantMessage;
            case 4:
                return NearbyConfig.BusinessTypeEnum.Streaming;
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
        int lengthSum = 0;
        for (byte[] barr : args) {
            if (barr == null) {
                barr = new byte[0];
            }
            lengthSum += barr.length;
        }
        byte[] res = new byte[lengthSum];
        int offset = 0;
        for (byte[] barr2 : args) {
            arraycopy(barr2, 0, res, offset, barr2.length);
            offset += barr2.length;
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
