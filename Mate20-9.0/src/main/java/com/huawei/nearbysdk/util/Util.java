package com.huawei.nearbysdk.util;

public class Util {
    private static final int HALF_LENGTH = 2;

    private Util() {
    }

    public static String toFrontHalfString(String str) {
        String strDevice = String.valueOf(str).replace(":", "");
        return strDevice.substring(0, strDevice.length() / 2);
    }
}
