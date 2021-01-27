package com.huawei.internal.telephony.uicc;

import android.graphics.Bitmap;
import com.android.internal.telephony.uicc.IccUtils;
import com.huawei.annotation.HwSystemApi;

public class IccUtilsEx {
    static final String LOG_TAG = "IccUtils";

    public static String bcdToString(byte[] data, int offset, int length) {
        return IccUtils.bcdToString(data, offset, length);
    }

    public static String cdmaBcdToString(byte[] data, int offset, int length) {
        return IccUtils.cdmaBcdToString(data, offset, length);
    }

    public static int gsmBcdByteToInt(byte bcdByte) {
        return IccUtils.gsmBcdByteToInt(bcdByte);
    }

    public static int cdmaBcdByteToInt(byte bcdByte) {
        return IccUtils.cdmaBcdByteToInt(bcdByte);
    }

    public static String adnStringFieldToString(byte[] data, int offset, int length) {
        return IccUtils.adnStringFieldToString(data, offset, length);
    }

    public static byte[] hexStringToBytes(String hexString) {
        return IccUtils.hexStringToBytes(hexString);
    }

    public static String bytesToHexString(byte[] bytes) {
        return IccUtils.bytesToHexString(bytes);
    }

    public static String networkNameToString(byte[] data, int offset, int length) {
        return IccUtils.networkNameToString(data, offset, length);
    }

    public static Bitmap parseToBnW(byte[] data, int length) {
        if (data == null || data.length == 0) {
            return null;
        }
        return IccUtils.parseToBnW(data, length);
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency) {
        if (data == null || data.length == 0) {
            return null;
        }
        return IccUtils.parseToRGB(data, length, transparency);
    }

    @HwSystemApi
    public static String getDecimalSubstring(String iccId) {
        return IccUtils.getDecimalSubstring(iccId);
    }
}
