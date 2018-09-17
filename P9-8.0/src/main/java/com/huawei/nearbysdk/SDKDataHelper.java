package com.huawei.nearbysdk;

import java.util.List;

public class SDKDataHelper {
    protected static final int AUTH_HOTSPOT = 57345;
    protected static final int CONTENT = 57603;
    private static final byte EA = Byte.MIN_VALUE;
    protected static final int HOTSPOT_VERSION = 57601;
    private static final int LENGTH_FIELD_EA_MAXIMUM = 4;
    private static final int MAX_TYPE_SIZE = 268435455;
    protected static final int PARSE_ERROR = -1;
    protected static final int SESSION_IV = 57602;
    private static final String TAG = "SDKDataHelper";

    public static class UnPackagedEA {
        private int mOffset;
        private int mValue;

        public int getValue() {
            return this.mValue;
        }

        public int getOffset() {
            return this.mOffset;
        }

        UnPackagedEA(int value, int offset) {
            this.mValue = value;
            this.mOffset = offset;
        }
    }

    public static int parseDataToParam(byte[] data, List<SDKTlvData> params) {
        SDKTlvData methodTlv = new SDKTlvData();
        unPackageTLV(data, methodTlv);
        HwLog.d(TAG, "data.length = " + data.length);
        int length = methodTlv.getData().length;
        int offset = 0;
        while (offset < length) {
            byte[] temp = new byte[(length - offset)];
            int copyLength = length - offset;
            HwLog.d(TAG, "arraycopy copylength = " + copyLength + "|src.length = " + length + "|temp.length = " + temp.length + "|arraycopy offset = " + offset);
            NearbySDKUtils.arraycopy(methodTlv.getData(), offset, temp, 0, copyLength);
            SDKTlvData param = new SDKTlvData();
            int res = unPackageTLV(temp, param);
            if (res <= 0) {
                return -1;
            }
            offset += res;
            params.add(param);
        }
        return methodTlv.getType();
    }

    public static byte[] packageTLV(int cmd, byte[] data) {
        byte[] byteArrCmd = addEA(cmd);
        byte[] result;
        if (byteArrCmd == null) {
            HwLog.e(TAG, "error, packageTLV fail");
            return null;
        } else if (data == null) {
            result = new byte[(byteArrCmd.length + 1)];
            NearbySDKUtils.arraycopy(byteArrCmd, 0, result, 0, byteArrCmd.length);
            result[byteArrCmd.length] = EA;
            return result;
        } else {
            byte[] byteArrLength = addEA(data.length);
            if (byteArrLength == null) {
                HwLog.e(TAG, "error, packageTLV fail");
                return null;
            }
            result = new byte[((byteArrCmd.length + data.length) + byteArrLength.length)];
            NearbySDKUtils.arraycopy(byteArrCmd, 0, result, 0, byteArrCmd.length);
            NearbySDKUtils.arraycopy(byteArrLength, 0, result, byteArrCmd.length, byteArrLength.length);
            NearbySDKUtils.arraycopy(data, 0, result, byteArrCmd.length + byteArrLength.length, data.length);
            return result;
        }
    }

    public static int unPackageTLV(byte[] receiveData, SDKTlvData data) {
        if (data == null || receiveData == null) {
            return 0;
        }
        UnPackagedEA type = removeEA(receiveData, 0);
        data.setType(type.getValue());
        UnPackagedEA length = removeEA(receiveData, type.getOffset());
        byte[] msg = new byte[length.getValue()];
        NearbySDKUtils.arraycopy(receiveData, length.getOffset(), msg, 0, length.getValue());
        data.setData(msg);
        return length.getValue() + length.getOffset();
    }

    private static boolean hasEA(byte value) {
        return (value & -128) == -128;
    }

    public static byte[] addEA(int value) {
        if (value > MAX_TYPE_SIZE) {
            HwLog.e(TAG, "error, value too large");
            return null;
        }
        byte[] byteArr = new byte[4];
        int i = 0;
        while (i < 4) {
            byteArr[i] = (byte) (value & 127);
            value >>= 7;
            if (value == 0) {
                byteArr[i] = (byte) (byteArr[i] | -128);
                i++;
                break;
            }
            i++;
        }
        byte[] outByteArr = new byte[i];
        NearbySDKUtils.arraycopy(byteArr, 0, outByteArr, 0, i);
        return outByteArr;
    }

    public static UnPackagedEA removeEA(byte[] data, int begin) {
        int value = 0;
        int end = begin + 4;
        if (end > data.length) {
            end = data.length;
        }
        int offset = begin;
        while (offset < end) {
            value |= (data[offset] & 127) << ((offset - begin) * 7);
            if (hasEA(data[offset])) {
                offset++;
                break;
            }
            offset++;
        }
        return new UnPackagedEA(value, offset);
    }
}
