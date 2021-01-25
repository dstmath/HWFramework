package com.huawei.coauthservice.identitymgr.utils;

public final class HwDeviceUtils {
    private static final int CUT_LEN = 4;
    private static final int MINE_CUT_LEN = 3;
    private static final int MINE_LEN = 7;
    private static final int TEXT_LEN = 8;

    private HwDeviceUtils() {
    }

    public static String maskDeviceId(String deviceId) {
        return maskString(deviceId, 8, 4);
    }

    public static String maskDeviceIp(String deviceIp) {
        return maskStringLeft(deviceIp, 7, 3);
    }

    public static String maskString(String value) {
        return maskString(value, 8, 4);
    }

    private static String maskString(String value, int minLen, int cutLen) {
        if (value == null || value.length() < minLen) {
            return "*";
        }
        String left = value.substring(0, cutLen);
        String right = value.substring(value.length() - cutLen);
        return left + "****" + right;
    }

    private static String maskStringLeft(String value, int minLen, int cutLen) {
        if (value == null || value.length() < minLen) {
            return "*";
        }
        String right = value.substring(value.length() - cutLen);
        return "****" + right;
    }
}
