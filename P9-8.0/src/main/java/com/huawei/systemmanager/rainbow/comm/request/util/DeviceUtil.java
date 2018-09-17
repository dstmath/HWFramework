package com.huawei.systemmanager.rainbow.comm.request.util;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class DeviceUtil {
    private static String mEMUIVersion = "";
    private static String mPhoneImei = "";

    public static String getTelephoneIMEIFromSys(Context context) {
        if (TextUtils.isEmpty(mPhoneImei)) {
            mPhoneImei = ((TelephonyManager) context.getSystemService("phone")).getImei();
        }
        return mPhoneImei;
    }

    public static String getTelephoneEMUIVersion() {
        if (TextUtils.isEmpty(mEMUIVersion)) {
            mEMUIVersion = SystemProperties.get("ro.build.version.emui", " ");
        }
        return mEMUIVersion;
    }
}
