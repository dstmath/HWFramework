package com.huawei.internal.telephony.uicc;

import android.graphics.Bitmap;
import com.android.internal.telephony.uicc.IccUtils;

public class IccUtilsEx {
    static final String LOG_TAG = "IccUtils";

    public static String bcdToString(byte[] data, int offset, int length) {
        return IccUtils.bcdToString(data, offset, length);
    }

    public static String cdmaBcdToString(byte[] data, int offset, int length) {
        return IccUtils.cdmaBcdToString(data, offset, length);
    }

    public static int gsmBcdByteToInt(byte b) {
        return IccUtils.gsmBcdByteToInt(b);
    }

    public static int cdmaBcdByteToInt(byte b) {
        return IccUtils.cdmaBcdByteToInt(b);
    }

    public static String adnStringFieldToString(byte[] data, int offset, int length) {
        return IccUtils.adnStringFieldToString(data, offset, length);
    }

    public static byte[] hexStringToBytes(String s) {
        return IccUtils.hexStringToBytes(s);
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
}
