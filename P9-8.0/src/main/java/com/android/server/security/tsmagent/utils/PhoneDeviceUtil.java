package com.android.server.security.tsmagent.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.UUID;

public class PhoneDeviceUtil {
    private static String GLOBAL_DEVICE_ID = "";

    public static String getDeviceID(Context context) {
        String deviceID = null;
        if (!TextUtils.isEmpty(GLOBAL_DEVICE_ID)) {
            return GLOBAL_DEVICE_ID;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm != null) {
            deviceID = tm.getDeviceId();
        }
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = getLocalMacAddress(context);
            if (!TextUtils.isEmpty(deviceID)) {
                deviceID = deviceID.replaceAll(":", "");
            }
        }
        if (TextUtils.isEmpty(deviceID)) {
            HwLog.i("getDeviceID getUUID");
            deviceID = getNumUUID();
        }
        GLOBAL_DEVICE_ID = deviceID;
        return deviceID;
    }

    public static String getLocalMacAddress(Context context) {
        String macStr = "";
        try {
            WifiManager wifi = (WifiManager) context.getSystemService("wifi");
            if (wifi == null) {
                return macStr;
            }
            WifiInfo info = wifi.getConnectionInfo();
            if (info == null) {
                return macStr;
            }
            macStr = info.getMacAddress();
            return macStr;
        } catch (SecurityException e) {
            HwLog.e("can not get getLocalMacAddress");
        }
    }

    public static String getNumUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (uuid.length() > 15) {
            return uuid.substring(0, 16);
        }
        return "0000000000000000".substring(15 - uuid.length()) + uuid;
    }
}
