package com.android.internal.telephony.vsim;

import android.os.SystemProperties;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwVSimOperateTANative {
    private static final boolean IS_SUPPORT_VSIM = SystemProperties.getBoolean("ro.radio.vsim_support", false);

    public native int operTA(int i, int i2, int i3, int i4, String str, String str2, String str3, int i5, int i6);

    static {
        if (IS_SUPPORT_VSIM && HuaweiTelephonyConfigs.isHisiPlatform()) {
            System.loadLibrary("operta");
        }
    }

    public int operTANative(int cmdId, int operType, int cardType, int apnType, String challenge, String imsi, String taPath, int vsimLoc, int modemId) {
        return operTA(cmdId, operType, cardType, apnType, challenge, imsi, taPath, vsimLoc, modemId);
    }
}
