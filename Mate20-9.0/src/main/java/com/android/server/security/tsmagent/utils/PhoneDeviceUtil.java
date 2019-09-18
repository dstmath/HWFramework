package com.android.server.security.tsmagent.utils;

import android.content.Context;
import java.util.UUID;

public class PhoneDeviceUtil {
    private static String GLOBAL_DEVICE_ID = "";

    public static String getNumUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (uuid.length() > 15) {
            return uuid.substring(0, 16);
        }
        return "0000000000000000".substring(15 - uuid.length()) + uuid;
    }

    public static String getSerialNumber(Context context) {
        return "deprecated";
    }
}
