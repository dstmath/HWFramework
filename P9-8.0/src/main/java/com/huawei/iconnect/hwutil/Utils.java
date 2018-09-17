package com.huawei.iconnect.hwutil;

public class Utils {
    public static String toMacSecureString(String address) {
        String strDevice = String.valueOf(address).replace(":", "");
        return strDevice.substring(strDevice.length() / 2);
    }
}
